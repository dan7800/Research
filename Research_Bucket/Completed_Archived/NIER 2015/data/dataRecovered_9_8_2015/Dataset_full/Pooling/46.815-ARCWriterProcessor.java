/*
 * ARCWriter
 *
 * $Id: ARCWriterProcessor.java,v 1.54 2006/09/01 00:55:51 paul_jack Exp $
 *
 * Created on Jun 5, 2003
 *
 * Copyright (C) 2003 Internet Archive.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.archive.crawler.Heritrix;
import org.archive.crawler.datamodel.CoreAttributeConstants;
import org.archive.crawler.datamodel.CrawlURI;
import org.archive.crawler.datamodel.FetchStatusCodes;
import org.archive.crawler.event.CrawlStatusListener;
import org.archive.crawler.framework.WriterPoolProcessor;
import org.archive.crawler.settings.XMLSettingsHandler;
import org.archive.io.ReplayInputStream;
import org.archive.io.WriterPoolMember;
import org.archive.io.WriterPoolSettings;
import org.archive.io.arc.ARCConstants;
import org.archive.io.arc.ARCWriter;
import org.archive.io.arc.ARCWriterPool;


/**
 * Processor module for writing the results of successful fetches (and
 * perhaps someday, certain kinds of network failures) to the Internet Archive
 * ARC file format.
 *
 * Assumption is that there is only one of these ARCWriterProcessors per
 * Heritrix instance.
 *
 * @author Parker Thompson
 */
public class ARCWriterProcessor extends WriterPoolProcessor
implements CoreAttributeConstants, ARCConstants, CrawlStatusListener,
WriterPoolSettings, FetchStatusCodes {
	private static final long serialVersionUID = 1957518408532644531L;

	private final Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
     * Default path list.
     */
    private static final String [] DEFAULT_PATH = {"arcs"};

    /**
     * Calculate metadata once only.
     */
    transient private List<String> cachedMetadata = null;

    /**
     * @param name Name of this writer.
     */
    public ARCWriterProcessor(String name) {
        super(name, "ARCWriter processor");
    }
    
    protected String [] getDefaultPath() {
    	return DEFAULT_PATH;
	}

    protected void setupPool(final AtomicInteger serialNo) {
		setPool(new ARCWriterPool(serialNo, this, getPoolMaximumActive(),
            getPoolMaximumWait()));
    }
    
    /**
     * Writes a CrawlURI and its associated data to store file.
     *
     * Currently this method understands the following uri types: dns, http, 
     * and https.
     *
     * @param curi CrawlURI to process.
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
            // TODO: Since we made FetchDNS work like FetchHTTP, IF we
            // move test for success of different schemes -- DNS, HTTP(S) and 
            // soon FTP -- up into CrawlURI#isSuccess (Have it read list of
            // supported schemes from heritrix.properties and cater to each's
            // notions of 'success' appropriately), then we can collapse this
            // if/else into a lone if (curi.isSuccess).  See WARCWriter for
            // an example.
            if ((scheme.equals("dns") &&
            		curi.getFetchStatus() == S_DNS_SUCCESS)) {
            	InputStream is = curi.getHttpRecorder().getRecordedInput().
            		getReplayInputStream();
                write(curi, recordLength, is,
                    curi.getString(A_DNS_SERVER_IP_LABEL));
            } else if ((scheme.equals("http") || scheme.equals("https")) &&
            		curi.getFetchStatus() > 0 && curi.isHttpTransaction()) {
                InputStream is = curi.getHttpRecorder().getRecordedInput().
            		getReplayInputStream();
                write(curi, recordLength, is, getHostAddress(curi));
            } else if (scheme.equals("ftp") && (curi.getFetchStatus() == 200)) {
                InputStream is = curi.getHttpRecorder().getRecordedInput().
                 getReplayInputStream();
                write(curi, recordLength, is, getHostAddress(curi));
            } else {
                logger.info("This writer does not write out scheme " + scheme +
                    " content");
            }
        } catch (IOException e) {
            curi.addLocalizedError(this.getName(), e, "WriteRecord: " +
                curi.toString());
            logger.log(Level.SEVERE, "Failed write of Record: " +
                curi.toString(), e);
        }
    }
    
    protected void write(CrawlURI curi, int recordLength, InputStream in,
        String ip)
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
        
        ARCWriter w = (ARCWriter)writer;
        try {
            if (in instanceof ReplayInputStream) {
                w.write(curi.toString(), curi.getContentType(),
                    ip, curi.getLong(A_FETCH_BEGAN_TIME),
                    recordLength, (ReplayInputStream)in);
            } else {
                w.write(curi.toString(), curi.getContentType(),
                    ip, curi.getLong(A_FETCH_BEGAN_TIME),
                    recordLength, in);
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

    /**
     * Return list of metadatas to add to first arc file metadata record.
     *
     * Get xml files from settingshandle.  Currently order file is the
     * only xml file.  We're NOT adding seeds to meta data.
     *
     * @return List of strings and/or files to add to arc file as metadata or
     * null.
     */
    public synchronized List<String> getMetadata() {
        if (this.cachedMetadata != null) {
            return this.cachedMetadata;
        }
        return cacheMetadata();
    }
    
    protected synchronized List<String> cacheMetadata() {
        if (this.cachedMetadata != null) {
            return this.cachedMetadata;
        }
        
        List<String> result = null;
        if (!XMLSettingsHandler.class.isInstance(getSettingsHandler())) {
            logger.warning("Expected xml settings handler (No arcmetadata).");
            // Early return
            return result;
        }
        
        XMLSettingsHandler xsh = (XMLSettingsHandler)getSettingsHandler();
        File orderFile = xsh.getOrderFile();
        if (!orderFile.exists() || !orderFile.canRead()) {
                logger.severe("File " + orderFile.getAbsolutePath() +
                    " is does not exist or is not readable.");
        } else {
            result = new ArrayList<String>(1);
            result.add(getMetadataBody(orderFile));
        }
        this.cachedMetadata = result;
        return this.cachedMetadata;
    }

    /**
     * Write the arc metadata body content.
     *
     * Its based on the order xml file but into this base we'll add other info
     * such as machine ip.
     *
     * @param orderFile Order file.
     *
     * @return String that holds the arc metaheader body.
     */
    protected String getMetadataBody(File orderFile) {
        String result = null;
        TransformerFactory factory = TransformerFactory.newInstance();
        Templates templates = null;
        Transformer xformer = null;
        try {
            templates = factory.newTemplates(new StreamSource(
                this.getClass().getResourceAsStream("/arcMetaheaderBody.xsl")));
            xformer = templates.newTransformer();
            // Below parameter names must match what is in the stylesheet.
            xformer.setParameter("software", "Heritrix " +
                Heritrix.getVersion() + " http://crawler.archive.org");
            xformer.setParameter("ip",
                InetAddress.getLocalHost().getHostAddress());
            xformer.setParameter("hostname",
                InetAddress.getLocalHost().getHostName());
            StreamSource source = new StreamSource(
                new FileInputStream(orderFile));
            StringWriter writer = new StringWriter();
            StreamResult target = new StreamResult(writer);
            xformer.transform(source, target);
            result= writer.toString();
        } catch (TransformerConfigurationException e) {
            logger.severe("Failed transform " + e);
        } catch (FileNotFoundException e) {
            logger.severe("Failed transform, file not found " + e);
        } catch (UnknownHostException e) {
            logger.severe("Failed transform, unknown host " + e);
        } catch(TransformerException e) {
            SourceLocator locator = e.getLocator();
            int col = locator.getColumnNumber();
            int line = locator.getLineNumber();
            String publicId = locator.getPublicId();
            String systemId = locator.getSystemId();
            logger.severe("Transform error " + e + ", col " + col + ", line " +
                line + ", publicId " + publicId + ", systemId " + systemId);
        }

        return result;
    }
}