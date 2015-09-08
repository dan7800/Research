/* $Id: ExperimentalWARCWriterProcessor.java,v 1.15 2006/09/26 23:47:15 paul_jack Exp $
 *
 * Created on August 1st, 2006.
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
package org.archive.crawler.writer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.crawler.datamodel.CoreAttributeConstants;
import org.archive.crawler.datamodel.CrawlURI;
import org.archive.crawler.datamodel.FetchStatusCodes;
import org.archive.crawler.event.CrawlStatusListener;
import org.archive.crawler.extractor.Link;
import org.archive.crawler.framework.WriterPoolProcessor;
import org.archive.io.WriterPoolMember;
import org.archive.io.WriterPoolSettings;
import org.archive.io.warc.ExperimentalWARCWriter;
import org.archive.io.warc.WARCConstants;
import org.archive.io.warc.WARCWriterPool;
import org.archive.uid.GeneratorFactory;
import org.archive.util.ArchiveUtils;
import org.archive.util.anvl.ANVLRecord;


/**
 * Experimental WARCWriterProcessor.
 * 
 * @author stack
 */
public class ExperimentalWARCWriterProcessor extends WriterPoolProcessor
implements CoreAttributeConstants, CrawlStatusListener,
WriterPoolSettings, FetchStatusCodes, WARCConstants {

    private static final long serialVersionUID = 188656957531675821L;

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
     * Default path list.
     */
    private static final String [] DEFAULT_PATH = {"warcs"};

    protected String [] getDefaultPath() {
        return DEFAULT_PATH;
    }
    
    /**
     * @param name Name of this writer.
     */
    public ExperimentalWARCWriterProcessor(String name) {
        super(name, "Experimental WARCWriter processor");
    }

    protected void setupPool(final AtomicInteger serialNo) {
		setPool(new WARCWriterPool(serialNo, this, getPoolMaximumActive(),
            getPoolMaximumWait()));
    }
    
    /**
     * Writes a CrawlURI and its associated data to store file.
     * 
     * Currently this method understands the following uri types: dns, http, and
     * https.
     * 
     * @param curi
     *            CrawlURI to process.
     * 
     */
    protected void innerProcess(CrawlURI curi) {
        // If failure, or we haven't fetched the resource yet, return
        if (curi.getFetchStatus() <= 0) {
            return;
        }
        
        // If no content, don't write record.
        int recordLength = (int)curi.getContentSize();
        if (recordLength <= 0) {
        	// Write nothing.
        	return;
        }
        
        String scheme = curi.getUURI().getScheme().toLowerCase();
        try {
            if ((scheme.equals("dns") &&
                    curi.getFetchStatus() == S_DNS_SUCCESS) ||
                ((scheme.equals("http") || scheme.equals("https")) &&
            		curi.getFetchStatus() > 0 && curi.isHttpTransaction()) ||
                (scheme.equals("ftp") && curi.getFetchStatus() == 200)) {
                write(scheme, curi);
            } else {
                logger.info("This writer does not write out scheme " +
                        scheme + " content");
            }
        } catch (IOException e) {
            curi.addLocalizedError(this.getName(), e, "WriteRecord: " +
                curi.toString());
            logger.log(Level.SEVERE, "Failed write of Record: " +
                curi.toString(), e);
        }
    }
    
    protected void write(final String lowerCaseScheme, final CrawlURI curi)
    throws IOException {
        WriterPoolMember writer = getPool().borrowFile();
        long position = writer.getPosition();
        // See if we need to open a new file because we've exceeed maxBytes.
        // Call to checkFileSize will open new file if we're at maximum for
        // current file.
        writer.checkSize();
        if (writer.getPosition() != position) {
            // We just closed the file because it was larger than maxBytes.
            // Add to the totalBytesWritten the size of the first record
            // in the file, if any.
            setTotalBytesWritten(getTotalBytesWritten() +
            	(writer.getPosition() - position));
            position = writer.getPosition();
        }
        
        ExperimentalWARCWriter w = (ExperimentalWARCWriter)writer;
        try {
            // Write a request, response, and metadata all in the one
            // 'transaction'.
            final URI baseid = getRecordID();
            final String timestamp =
                ArchiveUtils.get14DigitDate(curi.getLong(A_FETCH_BEGAN_TIME));
            if (lowerCaseScheme.startsWith("http")) {
                // Add named fields for ip, checksum, and relate the metadata
                // and request to the resource field.
                ANVLRecord r = new ANVLRecord();
                if (curi.getContentDigest() != null) {
                    // TODO: This is digest for content -- doesn't include
                    // response headers.
                    r.addLabelValue(NAMED_FIELD_CHECKSUM_LABEL,
                        curi.getContentDigestSchemeString());
                }
                r.addLabelValue(NAMED_FIELD_IP_LABEL, getHostAddress(curi));
                URI rid = writeResponse(w, timestamp, HTTP_RESPONSE_MIMETYPE,
                	baseid, curi, r);
                r = new ANVLRecord(1);
                r.addLabelValue(NAMED_FIELD_RELATED_LABEL, rid.toString());
                writeRequest(w, timestamp, HTTP_REQUEST_MIMETYPE,
                	baseid, curi, r);
                writeMetadata(w, timestamp, baseid, curi, r);
            } else if (lowerCaseScheme.equals("dns")) {
                String ip = curi.getString(A_DNS_SERVER_IP_LABEL);
                ANVLRecord r = null;
                if (ip != null && ip.length() > 0) {
                	r = new ANVLRecord();
                    r.addLabelValue(NAMED_FIELD_IP_LABEL, ip);
                }
                writeResponse(w, timestamp, curi.getContentType(), baseid,
                    curi, r);
            } else {
                logger.warning("No handler for scheme " + lowerCaseScheme);
            }
        } catch (IOException e) {
            // Invalidate this file (It gets a '.invalid' suffix).
            getPool().invalidateFile(writer);
            // Set the writer to null otherwise the pool accounting
            // of how many active writers gets skewed if we subsequently
            // do a returnWriter call on this object in the finally block.
            writer = null;
            throw e;
        } finally {
            if (writer != null) {
            	setTotalBytesWritten(getTotalBytesWritten() +
            	     (writer.getPosition() - position));
                getPool().returnFile(writer);
            }
        }
        checkBytesWritten();
    }
    
    protected URI writeRequest(final ExperimentalWARCWriter w,
            final String timestamp, final String mimetype,
            final URI baseid, final CrawlURI curi,
            final ANVLRecord namedFields) 
    throws IOException {
        final URI uid = qualifyRecordID(baseid, TYPE, REQUEST);
        w.writeRequestRecord(curi.toString(), timestamp, mimetype, uid,
            namedFields,
            curi.getHttpRecorder().getRecordedOutput().getReplayInputStream(),
            curi.getHttpRecorder().getRecordedOutput().getSize());
        return uid;
    }
    
    protected URI writeResponse(final ExperimentalWARCWriter w,
            final String timestamp, final String mimetype,
            final URI baseid, final CrawlURI curi,
            final ANVLRecord namedFields) 
    throws IOException {
        w.writeResponseRecord(curi.toString(), timestamp, mimetype, baseid,
            namedFields,
            curi.getHttpRecorder().getRecordedInput().getReplayInputStream(),
            curi.getHttpRecorder().getRecordedInput().getSize());
        return baseid;
    }
    
    protected URI writeMetadata(final ExperimentalWARCWriter w,
            final String timestamp,
            final URI baseid, final CrawlURI curi,
            final ANVLRecord namedFields) 
    throws IOException {
        final URI uid = qualifyRecordID(baseid, TYPE, METADATA);
        // Get some metadata from the curi.
        // TODO: Get all curi metadata.
        ANVLRecord r = new ANVLRecord();
        if (curi.isSeed()) {
            r.addLabel("seed");
        } else {
        	if (curi.forceFetch()) {
        		r.addLabel("force-fetch");
        	}
            r.addLabelValue("via", curi.flattenVia());
            r.addLabelValue("pathFromSeed", curi.getPathFromSeed());
        }
        Collection<Link> links = curi.getOutLinks();
        if (links != null || links.size() > 0) {
            for (Link link: links) {
                r.addLabelValue("outlink", link.toString());
            }
        }
        if (curi.isTruncatedFetch()) {
            String value = curi.isTimeTruncatedFetch()?
                    NAMED_FIELD_TRUNCATED_VALUE_TIME:
                curi.isLengthTruncatedFetch()?
                        NAMED_FIELD_TRUNCATED_VALUE_LEN:
                curi.isHeaderTruncatedFetch()?
                        NAMED_FIELD_TRUNCATED_VALUE_HEAD:
                NAMED_FIELD_TRUNCATED_VALUE_UNSPECIFIED;
                      
            r.addLabelValue(NAMED_FIELD_TRUNCATED, value);
        }
        
        // TODO: Other curi fields to write to metadata.
        // 
        // Credentials
        // 
        // fetch-began-time: 1154569278774
        // fetch-completed-time: 1154569281816
        //
        // Annotations.
        
        byte [] b = r.getUTF8Bytes();
        w.writeMetadataRecord(curi.toString(), timestamp, ANVLRecord.MIMETYPE,
            uid, namedFields, new ByteArrayInputStream(b), b.length);
        return uid;
    }
    
    protected URI getRecordID() throws IOException {
        URI result;
        try {
            result = GeneratorFactory.getFactory().getRecordID();
        } catch (URISyntaxException e) {
            throw new IOException(e.toString());
        }
        return result;
    }
    
    protected URI qualifyRecordID(final URI base, final String key,
            final String value)
    throws IOException {
        URI result;
        Map<String, String> qualifiers = new HashMap<String, String>(1);
        qualifiers.put(key, value);
        try {
            result = GeneratorFactory.getFactory().
                qualifyRecordID(base, qualifiers);
        } catch (URISyntaxException e) {
            throw new IOException(e.toString());
        }
        return result;
    }  

    public List getMetadata() {
        // TODO: As ANVL?
        return null;
    }
}