/* FrontierJournal
 * 
 * Created on Oct 26, 2004
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

import java.io.File;
import java.io.IOException;

import org.archive.crawler.datamodel.CrawlURI;
import org.archive.net.UURI;

/**
 * Record of key Frontier happenings.
 * @author stack
 * @version $Date: 2005/09/07 22:35:26 $, $Revision: 1.8 $
 */
public interface FrontierJournal {
    public static final String LOGNAME_RECOVER = "recover";

    /**
     * @param curi CrawlURI that has been scheduled to be added to the
     * Frontier.
     */
    public abstract void added(CrawlURI curi);

    /**
     * @param curi CrawlURI that finished successfully.
     */
    public abstract void finishedSuccess(CrawlURI curi);

    /**
     * @param uuri UURI that finished successfully.
     */
    public abstract void finishedSuccess(UURI uuri);

    /**
     * Note that a CrawlURI was emitted for processing.
     * If not followed by a finished or rescheduled notation in
     * the journal, the CrawlURI was still in-process when the journal ended.
     * 
     * @param curi CrawlURI emitted.
     */
    public abstract void emitted(CrawlURI curi);

    /**
     * @param u UURI that finished unsuccessfully 
     */
    public abstract void finishedFailure(UURI u);
    
    /**
     * @param curi CrawlURI finished unsuccessfully.
     */
    public abstract void finishedFailure(CrawlURI curi);

    /**
     * @param curi CrawlURI that was returned to the Frontier for 
     * another try.
     */
    public abstract void rescheduled(CrawlURI curi);

    /**
     *  Flush and close any held objects.
     */
    public abstract void close();
    
    /**
     * Checkpoint.
     * @param checkpointDir Directory we're checkpointing into.
     * @throws IOException
     */
    public abstract void checkpoint(final File checkpointDir)
    throws IOException;

    /**
     * Add a line noting a serious crawl error. 
     * 
     * @param string
     */
    public abstract void seriousError(String string);
}