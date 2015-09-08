/* RecoveryJournal
 *
 * $Id: RecoveryJournal.java,v 1.33 2006/09/26 20:38:48 paul_jack Exp $
 *
 * Created on Jul 20, 2004
 *
 * Copyright (C) 2004 Internet Archive.
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
package org.archive.crawler.frontier;

import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;
import it.unimi.dsi.mg4j.util.MutableString;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.httpclient.URIException;
import org.archive.crawler.datamodel.CandidateURI;
import org.archive.crawler.datamodel.CrawlURI;
import org.archive.crawler.framework.Frontier;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.archive.util.ArchiveUtils;

import java.util.concurrent.CountDownLatch;

/**
 * Helper class for managing a simple Frontier change-events journal which is
 * useful for recovering from crawl problems.
 * 
 * By replaying the journal into a new Frontier, its state (at least with
 * respect to URIs alreadyIncluded and in pending queues) will match that of the
 * original Frontier, allowing a pseudo-resume of a previous crawl, at least as
 * far as URI visitation/coverage is concerned.
 * 
 * @author gojomo
 */
public class RecoveryJournal
implements FrontierJournal {
    private static final Logger LOGGER = Logger.getLogger(
            RecoveryJournal.class.getName());
    
    public final static String F_ADD = "F+ ";
    public final static String F_EMIT = "Fe ";
    public final static String F_RESCHEDULE = "Fr ";
    public final static String F_SUCCESS = "Fs ";
    public final static String F_FAILURE = "Ff ";
    public final static String LOG_ERROR = "E ";
    public final static String LOG_TIMESTAMP = "T ";
    public final int TIMESTAMP_INTERVAL = 10000; // timestamp every this many lines
    
    //  show recovery progress every this many lines
    private final static int PROGRESS_INTERVAL = 1000000; 
    
    // once this many URIs are queued during recovery, allow 
    // crawl to begin, while enqueuing of other URIs from log
    // continues in background
    private static final long ENOUGH_TO_START_CRAWLING = 100000;
    /**
     * Stream on which we record frontier events.
     */
    private Writer out = null;

    private long lines = 0;
    
    public static final String GZIP_SUFFIX = ".gz";
    
    /**
     * File we're writing recovery to.
     * Keep a reference in case we want to rotate it off.
     */
    private File gzipFile = null;
    
    /**
     * Allocate a buffer for accumulating lines to write and reuse it.
     */
    private MutableString accumulatingBuffer =
        new MutableString(1 + F_ADD.length() +
                128 /*curi.toString().length()*/ +
                1 +
                8 /*curi.getPathFromSeed().length()*/ +
                1 +
                128 /*curi.flattenVia().length()*/);

    
    /**
     * Create a new recovery journal at the given location
     * 
     * @param path Directory to make the recovery  journal in.
     * @param filename Name to use for recovery journal file.
     * @throws IOException
     */
    public RecoveryJournal(String path, String filename)
    throws IOException {
        this.gzipFile = new File(path, filename + GZIP_SUFFIX);
        this.out = initialize(gzipFile);
    }
    
    private Writer initialize (final File f)
    throws FileNotFoundException, IOException {
        return new OutputStreamWriter(new GZIPOutputStream(
            new FastBufferedOutputStream(new FileOutputStream(f))));
    }

    public synchronized void added(CrawlURI curi) {
        accumulatingBuffer.length(0);
        this.accumulatingBuffer.append(F_ADD).
            append(curi.toString()).
            append(" "). 
            append(curi.getPathFromSeed()).
            append(" ").
            append(curi.flattenVia());
        writeLine(accumulatingBuffer);
    }

    public void finishedSuccess(CrawlURI curi) {
        finishedSuccess(curi.toString());
    }
    
    public void finishedSuccess(UURI uuri) {
        finishedSuccess(uuri.toString());
    }
    
    protected void finishedSuccess(String uuri) {
        writeLine(F_SUCCESS, uuri);
    }

    public void emitted(CrawlURI curi) {
        writeLine(F_EMIT, curi.toString());

    }

    public void finishedFailure(CrawlURI curi) {
        finishedFailure(curi.toString());
    }
    
    public void finishedFailure(UURI uuri) {
        finishedFailure(uuri.toString());
    }
    
    public void finishedFailure(String u) {
        writeLine(F_FAILURE, u);
    }

    public void rescheduled(CrawlURI curi) {
        writeLine(F_RESCHEDULE, curi.toString());
    }

    private synchronized void writeLine(String string) {
        try {
            this.out.write("\n");
            this.out.write(string);
            noteLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void writeLine(String s1, String s2) {
        try {
            this.out.write("\n");
            this.out.write(s1);
            this.out.write(s2);
            noteLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private synchronized void writeLine(MutableString mstring) {
        if (this.out == null) {
            return;
        }
        try {
            this.out.write("\n");
            mstring.write(out);
            noteLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * @throws IOException
     */
    private void noteLine() throws IOException {
        lines++;
        if(lines % TIMESTAMP_INTERVAL == 0) {
            out.write("\n");
            out.write(LOG_TIMESTAMP);
            out.write(ArchiveUtils.getLog14Date());
        }
    }
    
    /**
     * Utility method for scanning a recovery journal and applying it to
     * a Frontier.
     * 
     * @param source Recover log path.
     * @param frontier Frontier reference.
     * @param retainFailures
     * @throws IOException
     * 
     * @see org.archive.crawler.framework.Frontier#importRecoverLog(String, boolean)
     */
    public static void importRecoverLog(final File source,
        final Frontier frontier, final boolean retainFailures)
    throws IOException {
        if (source == null) {
            throw new IllegalArgumentException("Passed source file is null.");
        }
        LOGGER.info("recovering frontier completion state from "+source);
        
        // first, fill alreadyIncluded with successes (and possibly failures),
        // and count the total lines
        final int lines =
            importCompletionInfoFromLog(source, frontier, retainFailures);
        
        LOGGER.info("finished completion state; recovering queues from " +
            source);

        // now, re-add anything that was in old frontier and not already
        // registered as finished. Do this in a separate thread that signals
        // this thread once ENOUGH_TO_START_CRAWLING URIs have been queued. 
        final CountDownLatch recoveredEnough = new CountDownLatch(1);
        new Thread(new Runnable() {
            public void run() {
                importQueuesFromLog(source, frontier, lines, recoveredEnough);
            }
        }, "queuesRecoveryThread").start();
        
        try {
            // wait until at least ENOUGH_TO_START_CRAWLING URIs queued
            recoveredEnough.await();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * Import just the SUCCESS (and possibly FAILURE) URIs from the given
     * recovery log into the frontier as considered included. 
     * 
     * @param source recovery log file to use
     * @param frontier frontier to update
     * @param retainFailures whether failure ('Ff') URIs should count as done
     * @return number of lines in recovery log (for reference)
     * @throws IOException
     */
    private static int importCompletionInfoFromLog(File source, 
            Frontier frontier, boolean retainFailures) throws IOException {
        // Scan log for all 'Fs' lines: add as 'alreadyIncluded'
        BufferedInputStream is = getBufferedInput(source);
        // create MutableString of good starting size (will grow if necessary)
        MutableString read = new MutableString(UURI.MAX_URL_LENGTH); 
        int lines = 0; 
        try {
            while (readLine(is,read)) {
                lines++;
                boolean wasSuccess = read.startsWith(F_SUCCESS);
                if (wasSuccess
						|| (retainFailures && read.startsWith(F_FAILURE))) {
                    // retrieve first (only) URL on line 
                    String s = read.subSequence(3,read.length()).toString();
                    try {
                        UURI u = UURIFactory.getInstance(s);
                        frontier.considerIncluded(u);
                        if(wasSuccess) {
                            if (frontier.getFrontierJournal() != null) {
                                frontier.getFrontierJournal().
                                    finishedSuccess(u);
                            }
                        } else {
                            // carryforward failure, in case future recovery
                            // wants to no retain them as finished 
                            if (frontier.getFrontierJournal() != null) {
                                frontier.getFrontierJournal().
                                    finishedFailure(u);
                            }
                        }
                    } catch (URIException e) {
                        e.printStackTrace();
                    }
                }
                if((lines%PROGRESS_INTERVAL)==0) {
                    // every 1 million lines, print progress
                    LOGGER.info(
                            "at line " + lines 
                            + " alreadyIncluded count = " +
                            frontier.discoveredUriCount());
                }
            }
        } catch (EOFException e) {
            // expected in some uncleanly-closed recovery logs; ignore
        } finally {
            is.close();
        }
        return lines;
    }

    /**
     * Read a line from the given bufferedinputstream into the MutableString.
     * Return true if a line was read; false if EOF. 
     * 
     * @param is
     * @param read
     * @return True if we read a line.
     * @throws IOException
     */
    private static boolean readLine(BufferedInputStream is, MutableString read)
    throws IOException {
        read.length(0);
        int c = is.read();
        while((c!=-1)&&c!='\n'&&c!='\r') {
            read.append((char)c);
            c = is.read();
        }
        if(c==-1 && read.length()==0) {
            // EOF and none read; return false
            return false;
        }
        if(c=='\n') {
            // consume LF following CR, if present
            is.mark(1);
            if(is.read()!='\r') {
                is.reset();
            }
        }
        // a line (possibly blank) was read
        return true;
    }

    /**
     * Import all ADDs from given recovery log into the frontier's queues
     * (excepting those the frontier drops as already having been included)
     * 
     * @param source recovery log file to use
     * @param frontier frontier to update
     * @param lines total lines noted in recovery log earlier
     * @param enough latch signalling 'enough' URIs queued to begin crawling
     */
    private static void importQueuesFromLog(File source, Frontier frontier,
            int lines, CountDownLatch enough) {
        BufferedInputStream is;
        // create MutableString of good starting size (will grow if necessary)
        MutableString read = new MutableString(UURI.MAX_URL_LENGTH);
        long queuedAtStart = frontier.queuedUriCount();
        long queuedDuringRecovery = 0;
        int qLines = 0;
        
        try {
            // Scan log for all 'F+' lines: if not alreadyIncluded, schedule for
            // visitation
            is = getBufferedInput(source);
            try {
                while (readLine(is,read)) {
                    qLines++;
                    if (read.startsWith(F_ADD)) {
                        UURI u;
                        CharSequence args[] = splitOnSpaceRuns(read);
                        try {
                            u = UURIFactory.getInstance(args[1].toString());
                            String pathFromSeed = (args.length > 2)?
                                args[2].toString() : "";
                            UURI via = (args.length > 3)?
                                UURIFactory.getInstance(args[3].toString()):
                                null;
                            String viaContext = (args.length > 4)?
                                    args[4].toString(): "";
                            CandidateURI caUri = new CandidateURI(u, 
                                    pathFromSeed, via, viaContext);
                            frontier.schedule(caUri);
                            
                            queuedDuringRecovery =
                                frontier.queuedUriCount() - queuedAtStart;
                            if(((queuedDuringRecovery + 1) %
                                    ENOUGH_TO_START_CRAWLING) == 0) {
                                enough.countDown();
                            }
                        } catch (URIException e) {
                            e.printStackTrace();
                        }
                    }
                    if((qLines%PROGRESS_INTERVAL)==0) {
                        // every 1 million lines, print progress
                        LOGGER.info(
                                "through line " 
                                + qLines + "/" + lines 
                                + " queued count = " +
                                frontier.queuedUriCount());
                    }
                }
            } catch (EOFException e) {
                // no problem: untidy end of recovery journal
            } finally {
            	    is.close(); 
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        LOGGER.info("finished recovering frontier from "+source+" "
                +qLines+" lines processed");
        enough.countDown();
    }

    /**
     * Return an array of the subsequences of the passed-in sequence,
     * split on space runs. 
     * 
     * @param read
     * @return CharSequence.
     */
    private static CharSequence[] splitOnSpaceRuns(CharSequence read) {
        int lastStart = 0;
        ArrayList<CharSequence> segs = new ArrayList<CharSequence>(5);
        int i;
        for(i=0;i<read.length();i++) {
            if (read.charAt(i)==' ') {
                segs.add(read.subSequence(lastStart,i));
                i++;
                while(i < read.length() && read.charAt(i)==' ') {
                    // skip any space runs
                    i++;
                }
                lastStart = i;
            }
        }
        if(lastStart<read.length()) {
            segs.add(read.subSequence(lastStart,i));
        }
        return (CharSequence[]) segs.toArray(new CharSequence[segs.size()]);        
    }

    /**
     * @param source
     * @return Recover log buffered reader.
     * @throws IOException
     */
    public static BufferedReader getBufferedReader(File source)
    throws IOException {
        boolean isGzipped = source.getName().toLowerCase().
            endsWith(GZIP_SUFFIX);
        FileInputStream fis = new FileInputStream(source);
        return new BufferedReader(isGzipped?
            new InputStreamReader(new GZIPInputStream(fis)):
            new InputStreamReader(fis));   
    }

    /**
     * Get a BufferedInputStream on the recovery file given.
     *
     * @param source file to open
     * @return Recover log buffered input stream.
     * @throws IOException
     */
    public static BufferedInputStream getBufferedInput(File source)
    throws IOException {
        boolean isGzipped = source.getName().toLowerCase().
            endsWith(GZIP_SUFFIX);
        FileInputStream fis = new FileInputStream(source);
        return isGzipped ? new BufferedInputStream(new GZIPInputStream(fis))
                : new BufferedInputStream(fis);
    }
    
    /**
     * Flush and close the underlying IO objects.
     */
    public void close() {
        if (this.out == null) {
            return;
        }
        try {
            this.out.flush();
            this.out.close();
            this.out = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void seriousError(String err) {
        writeLine("\n"+LOG_ERROR+ArchiveUtils.getLog14Date()+" "+err);
    }

    public synchronized void checkpoint(final File checkpointDir)
    throws IOException {
        if (this.out == null || !this.gzipFile.exists()) {
            return;
        }
        close();
        // Rename gzipFile with the checkpoint name as suffix.
        this.gzipFile.renameTo(new File(this.gzipFile.getParentFile(),
                this.gzipFile.getName() + "." + checkpointDir.getName()));
        // Open new gzip file.
        this.out = initialize(this.gzipFile);
    }
}
