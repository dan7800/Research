/*  $Id: ExperimentalWARCWriter.java,v 1.21 2006/09/06 05:38:18 stack-sf Exp $
 *
 * Created on July 27th, 2006
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 * Heritrix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Heritrix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Heritrix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.io.warc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.archive.io.UTF8Bytes;
import org.archive.io.WriterPoolMember;
import org.archive.uid.GeneratorFactory;
import org.archive.util.ArchiveUtils;
import org.archive.util.anvl.ANVLRecord;


/**
 * <b>Experimental</b> WARC implementation.
 * 
 * Based on unreleased version 0.9 of <a 
 * href="http://archive-access.sourceforge.net//warc/warc_file_format.html">WARC
 * File Format</a> document.  Specification and implementation subject to
 * change.
 *
 * <p>Assumption is that the caller is managing access to this
 * ExperimentalWARCWriter ensuring only one thread accessing this WARC instance
 * at any one time.
 * 
 * <p>While being written, WARCs have a '.open' suffix appended.
 *
 * @author stack
 * @version $Revision: 1.21 $ $Date: 2006/09/06 05:38:18 $
 */
public class ExperimentalWARCWriter extends WriterPoolMember
implements WARCConstants {
    /**
     * Buffer to reuse writing streams.
     */
    private final byte [] readbuffer = new byte[16 * 1024];
    
    /**
     * NEWLINE as bytes.
     */
    public static byte [] CRLF_BYTES;
    static {
        try {
            CRLF_BYTES = CRLF.getBytes(DEFAULT_ENCODING);
        } catch(Exception e) {
            e.printStackTrace();
        }
    };
    
    /**
     * Formatter for the length.
     */
    private static NumberFormat RECORD_LENGTH_FORMATTER =
        new DecimalFormat(PLACEHOLDER_RECORD_LENGTH_STRING);
    
    /**
     * Metadata.
     * TODO: Exploit writing warcinfo record.  Currently unused.
     */
    private final List fileMetadata;
    
    
    /**
     * Shutdown Constructor
     * Has default access so can make instance to test utility methods.
     */
    ExperimentalWARCWriter() {
        this(null, null, "", "", true, -1, null);
    }
    
    /**
     * Constructor.
     * Takes a stream. Use with caution. There is no upperbound check on size.
     * Will just keep writing.  Only pass Streams that are bounded. 
     * @param serialNo  used to generate unique file name sequences
     * @param out Where to write.
     * @param f File the <code>out</code> is connected to.
     * @param cmprs Compress the content written.
     * @param a14DigitDate If null, we'll write current time.
     * @throws IOException
     */
    public ExperimentalWARCWriter(final AtomicInteger serialNo,
    		final OutputStream out, final File f,
    		final boolean cmprs, final String a14DigitDate,
            final List warcinfoData)
    throws IOException {
        super(serialNo, out, f, cmprs, a14DigitDate);
        // TODO: Currently unused.
        this.fileMetadata = warcinfoData;
    }
            
    /**
     * Constructor.
     *
     * @param dirs Where to drop files.
     * @param prefix File prefix to use.
     * @param cmprs Compress the records written. 
     * @param maxSize Maximum size for ARC files written.
     * @param suffix File tail to use.  If null, unused.
     * @param warcinfoData File metadata for warcinfo record.
     */
    public ExperimentalWARCWriter(final AtomicInteger serialNo,
    		final List<File> dirs, final String prefix, 
            final String suffix, final boolean cmprs,
            final int maxSize, final List warcinfoData) {
        super(serialNo, dirs, prefix, suffix, cmprs, maxSize,
        	WARC_FILE_EXTENSION);
        // TODO: Currently unused.
        this.fileMetadata = warcinfoData;
    }
    
    @Override
    protected String createFile(File file) throws IOException {
    	String filename = super.createFile(file);
    	writeWarcinfoRecord(filename);
        return filename;
    }
    
    protected void baseCharacterCheck(final char c, final String parameter)
    throws IOException {
        // TODO: Too strict?  UNICODE control characters?
        if (Character.isISOControl(c) || !Character.isValidCodePoint(c)) {
            throw new IOException("Contains illegal character 0x" +
                Integer.toHexString(c) + ": " + parameter);
        }
    }
    
    protected String checkHeaderLineParameters(final String parameter)
    throws IOException {
        for (int i = 0; i < parameter.length(); i++) {
        	final char c = parameter.charAt(i);
        	baseCharacterCheck(c, parameter);
        	if (Character.isWhitespace(c)) {
                throw new IOException("Contains disallowed white space 0x" +
                    Integer.toHexString(c) + ": " + parameter);
        	}
        }
        return parameter;
    }
    
    protected String checkHeaderLineMimetypeParameter(final String parameter)
    throws IOException {
    	StringBuilder sb = new StringBuilder(parameter.length());
    	boolean wasWhitespace = false;
        for (int i = 0; i < parameter.length(); i++) {
        	char c = parameter.charAt(i);
        	if (Character.isWhitespace(c)) {
        		// Map all to ' ' and collapse multiples into one.
        		// TODO: Make sure white space occurs in legal location --
        		// before parameter or inside quoted-string.
        		if (wasWhitespace) {
        			continue;
        		}
        		wasWhitespace = true;
        		c = ' ';
        	} else {
        		wasWhitespace = false;
        		baseCharacterCheck(c, parameter);
        	}
        	sb.append(c);
        }
        
        return sb.toString();
    }

    protected byte [] createRecordHeaderline(final String type,
    		final String url, final String create14DigitDate,
    		final String mimetype, final URI recordId,
    		final int namedFieldsLength, final long contentLength)
    throws IOException {
    	final StringBuilder sb =
    		new StringBuilder(2048/*A SWAG: TODO: Do analysis.*/);
    	sb.append(WARC_ID);
    	sb.append(HEADER_FIELD_SEPARATOR);
    	sb.append(PLACEHOLDER_RECORD_LENGTH_STRING);
    	sb.append(HEADER_FIELD_SEPARATOR);
    	sb.append(type);
    	sb.append(HEADER_FIELD_SEPARATOR);
    	sb.append(checkHeaderLineParameters(url));
    	sb.append(HEADER_FIELD_SEPARATOR);
    	sb.append(checkHeaderLineParameters(create14DigitDate));
    	sb.append(HEADER_FIELD_SEPARATOR);
    	// 0.9 of spec. has mimetype second-to-last and recordid last on
    	// header line.  Here we swap their positions and allow writing
    	// of full mimetypes rather than the curtailed type we used write into
    	// ARCs.  These two deviations to be proposed as amendments to spec 0.9.
    	sb.append(checkHeaderLineParameters(recordId.toString()));
    	sb.append(HEADER_FIELD_SEPARATOR);
    	sb.append(checkHeaderLineMimetypeParameter(mimetype));
        // Add terminating CRLF.
        sb.append(CRLF);
    	
    	long length = sb.length() + namedFieldsLength + contentLength;
    	
    	// Insert length and pad out to fixed width with zero prefix to
        // highlight 'fixed-widthness' of length.
    	int start = WARC_ID.length() + 1 /*HEADER_FIELD_SEPARATOR */;
        int end = start + PLACEHOLDER_RECORD_LENGTH_STRING.length();
    	String lenStr = RECORD_LENGTH_FORMATTER.format(length);
    	sb.replace(start, end, lenStr);

        return sb.toString().getBytes(HEADER_LINE_ENCODING);
    }

    protected void writeRecord(final String type, final String url,
    		final String create14DigitDate, final String mimetype,
    		final URI recordId, ANVLRecord namedFields,
            final InputStream contentStream, final long contentLength)
    throws IOException {
    	if (!TYPES_LIST.contains(type)) {
    		throw new IllegalArgumentException("Unknown record type: " + type);
    	}
    	if (contentLength == 0 &&
                (namedFields == null || namedFields.size() <= 0)) {
    		throw new IllegalArgumentException("Cannot have a record made " +
    		    "of a Header line only (Content and Named Fields are empty).");
    	}
    	
        preWriteRecordTasks();
        try {
        	if (namedFields == null) {
        		// Use the empty anvl record so the length of blank line on
        		// end gets counted as part of the record length.
        		namedFields = ANVLRecord.EMPTY_ANVL_RECORD;
        	}
        	
        	// Serialize metadata first so we have metadata length.
        	final byte [] namedFieldsBlock = namedFields.getUTF8Bytes();
        	// Now serialize the Header line.
            final byte [] header = createRecordHeaderline(type, url,
            	create14DigitDate, mimetype, recordId, namedFieldsBlock.length,
            	contentLength);
            write(header);
            write(namedFieldsBlock);
            if (contentStream != null && contentLength > 0) {
            	readFullyFrom(contentStream, contentLength, this.readbuffer);
            }
            
            // Write out the two blank lines at end of all records.
            // TODO: Why? Messes up skipping through file. Also not in grammar.
            write(CRLF_BYTES);
            write(CRLF_BYTES);
        } finally {
            postWriteRecordTasks();
        }
    }
    
    protected URI generateRecordId(final Map<String, String> qualifiers)
    throws IOException {
    	URI rid = null;
    	try {
    		rid = GeneratorFactory.getFactory().
    			getQualifiedRecordID(qualifiers);
    	} catch (URISyntaxException e) {
    		// Convert to IOE so can let it out.
    		throw new IOException(e.getMessage());
    	}
    	return rid;
    }
    
    protected URI generateRecordId(final String key, final String value)
    throws IOException {
    	URI rid = null;
    	try {
    		rid = GeneratorFactory.getFactory().
    			getQualifiedRecordID(key, value);
    	} catch (URISyntaxException e) {
    		// Convert to IOE so can let it out.
    		throw new IOException(e.getMessage());
    	}
    	return rid;
    }
    
    public URI writeWarcinfoRecord(String filename)
	throws IOException {
    	return writeWarcinfoRecord(filename, null);
    }
    
    public URI writeWarcinfoRecord(String filename, final String description)
        	throws IOException {
        // Strip .open suffix if present.
        if (filename.endsWith(WriterPoolMember.OCCUPIED_SUFFIX)) {
        	filename = filename.substring(0,
        		filename.length() - WriterPoolMember.OCCUPIED_SUFFIX.length());
        }
        ANVLRecord record = new ANVLRecord(2);
        record.addLabelValue(NAMED_FIELD_WARCFILENAME, filename);
        if (description != null && description.length() > 0) {
        	record.addLabelValue(NAMED_FIELD_DESCRIPTION, description);
        }
        // Add warcinfo body.
        byte [] warcinfoBody = null;
        if (this.fileMetadata == null) {
        	// TODO: What to write into a warcinfo?  What to associate?
        	warcinfoBody = "TODO: Unimplemented".getBytes();
        } else {
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	for (final Iterator i = this.fileMetadata.iterator();
        			i.hasNext();) {
        		baos.write(i.next().toString().getBytes(UTF8Bytes.UTF8));
        	}
        	warcinfoBody = baos.toByteArray();
        }
        URI uri = writeWarcinfoRecord("text/plain", record,
            new ByteArrayInputStream(warcinfoBody), warcinfoBody.length);
        // TODO: If at start of file, and we're writing compressed,
        // write out our distinctive GZIP extensions.
        return uri;
    }
    
    /**
     * Write a warcinfo to current file.
     * TODO: Write crawl metadata or pointers to crawl description.
     * @param mimetype Mimetype of the <code>fileMetadata</code> block.
     * @param namedFields Named fields. Pass <code>null</code> if none.
     * @param fileMetadata Metadata about this WARC as RDF, ANVL, etc.
     * @param fileMetadataLength Length of <code>fileMetadata</code>.
     * @throws IOException
     * @return Generated record-id made with
     * <a href="http://en.wikipedia.org/wiki/Data:_URL">data: scheme</a> and
     * the current filename.
     */
    public URI writeWarcinfoRecord(final String mimetype,
    	final ANVLRecord namedFields, final InputStream fileMetadata,
    	final long fileMetadataLength)
    throws IOException {
    	final URI recordid = generateRecordId(TYPE, WARCINFO);
    	writeWarcinfoRecord(ArchiveUtils.get14DigitDate(), mimetype, recordid,
            namedFields, fileMetadata, fileMetadataLength);
    	return recordid;
    }
    
    /**
     * Write a <code>warcinfo</code> to current file.
     * The <code>warcinfo</code> type uses its <code>recordId</code> as its URL.
     * @param recordId URI to use for this warcinfo.
     * @param create14DigitDate Record creation date as 14 digit date.
     * @param mimetype Mimetype of the <code>fileMetadata</code>.
     * @param namedFields Named fields.
     * @param fileMetadata Metadata about this WARC as RDF, ANVL, etc.
     * @param fileMetadataLength Length of <code>fileMetadata</code>.
     * @throws IOException
     */
    public void writeWarcinfoRecord(final String create14DigitDate,
        final String mimetype, final URI recordId, final ANVLRecord namedFields,
    	final InputStream fileMetadata, final long fileMetadataLength)
    throws IOException {
    	writeRecord(WARCINFO, recordId.toString(), create14DigitDate, mimetype,
        	recordId, namedFields, fileMetadata, fileMetadataLength);
    }
    
    public void writeRequestRecord(final String url,
        final String create14DigitDate, final String mimetype,
        final URI recordId,
        final ANVLRecord namedFields, final InputStream request,
        final long requestLength)
    throws IOException {
        writeRecord(REQUEST, url, create14DigitDate,
            mimetype, recordId, namedFields, request,
            requestLength);
    }
    
    public void writeResourceRecord(final String url,
            final String create14DigitDate, final String mimetype,
            final ANVLRecord namedFields, final InputStream response,
            final long responseLength)
    throws IOException {
    	writeResourceRecord(url, create14DigitDate, mimetype, getRecordID(),
    			namedFields, response, responseLength);
    }
    
    public void writeResourceRecord(final String url,
            final String create14DigitDate, final String mimetype,
            final URI recordId,
            final ANVLRecord namedFields, final InputStream response,
            final long responseLength)
    throws IOException {
        writeRecord(RESOURCE, url, create14DigitDate,
            mimetype, recordId, namedFields, response,
            responseLength);
    }

    public void writeResponseRecord(final String url,
            final String create14DigitDate, final String mimetype,
            final URI recordId,
            final ANVLRecord namedFields, final InputStream response,
            final long responseLength)
    throws IOException {
        writeRecord(RESPONSE, url, create14DigitDate,
            mimetype, recordId, namedFields, response,
            responseLength);
    }
    
    public void writeMetadataRecord(final String url,
            final String create14DigitDate, final String mimetype,
            final URI recordId,
            final ANVLRecord namedFields, final InputStream metadata,
            final long metadataLength)
    throws IOException {
        writeRecord(METADATA, url, create14DigitDate,
            mimetype, recordId, namedFields, metadata,
            metadataLength);
    }
    
    /**
     * Convenience method for getting Record-Ids.
     * @return A record ID.
     * @throws IOException
     */
    public static URI getRecordID() throws IOException {
        URI result;
        try {
            result = GeneratorFactory.getFactory().getRecordID();
        } catch (URISyntaxException e) {
            throw new IOException(e.toString());
        }
        return result;
    }
}
