/*
 * Copyright (c) Core Developers Network LLC, All rights reserved
 */
package org.objectweb.howl.journal;

import java.io.File;
import java.io.IOException;

import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;

import org.objectweb.howl.journal.spi.Hardener;
import org.objectweb.howl.journal.spi.JournalEvent;
import org.objectweb.howl.journal.spi.RollingLogHardener;

/**
 * Defines the Journal interface.  Journals are used to log and read recovery
 * data at high speeds. 
 * 
 * @version $Revision: 1.1 $ $Date: 2004/01/26 20:59:30 $
 */
final public class Journal {
   
    private File journal;
    private Hardener hardener;
    private Thread hardnerThread;
    
    // events arrive at the journal via this channel.
    private LinkedQueue eventQueue = new LinkedQueue();
    // The journal keeps track of the start checkpoint event.
    private JournalEvent checkPointEvent;

    /**
     * Creates/Opens a journal on the specified file.  Uses the RollingLogHardener to
     * harden Journal events.  
     * 
     * @param journal
     * @throws IOException
     */
    public Journal(File journal) throws IOException {
        this(journal, new RollingLogHardener());
    }

    /**
     * Creates/Opens a journal on the specified file.  Uses the specified hardener to
     * harden Journal events.  
     * 
     * @param journal
     * @param hardener
     * @throws IOException
     */
    public Journal(File journal, Hardener hardener) throws IOException {
        this.journal = journal;
        this.hardener = hardener;
        hardener.open(journal, eventQueue);
    }
    
    
    /**
     * Asynchronously write an entry in the journal.  This call returns before
     * the entry has been hardened.  
     * 
     * @param entry
     */
    public void add( byte []data) throws InterruptedException {
        JournalEvent event = JournalEvent.createAddEvent(data);
        eventQueue.put(event);
    }

    /**
     * Adds the log message and waits for it to harden before returning.  Since 
     * entries are hardened in the order that they were added to the journal,
     * this call must first wait for all previously added entries to harden.
     */
    public void addAndSync(byte []data) throws InterruptedException {
        JournalEvent event = JournalEvent.createAddSyncEvent(data);
        eventQueue.put(event);
        event.waitForHardening();
    }

    /**
     * Places a check point in the journal.  This is a blocking call and does not
     * return until the checkpoint has been hardened.  At a later time the clearCheckPoint(...)
     * method should be called to indicate that all the data log before the startCheckPoint
     * is no longer needed.
     * 
     * This method should not be called again until the checkpoint is cleared.
     * 
     * @return the id of the checkpoint that was started.
     */
    synchronized public long startCheckPoint() throws InterruptedException {
        if( checkPointEvent!= null )
            throw new IllegalStateException("A check point has allready been started.");        
        checkPointEvent = JournalEvent.createStartCheckpointEvent(hardener.getNextCheckPointID());
        eventQueue.put(checkPointEvent);
        checkPointEvent.waitForHardening();
        return checkPointEvent.checkpointID;
    }

    /**
     * Clear the previously started checkpoint.  This is blocking call and does not return until
     * the event has been hardened.  
     */
    synchronized public void clearCheckPoint() throws InterruptedException {
        if( checkPointEvent== null )
            throw new IllegalStateException("A check point has not yet been started.");
        
        JournalEvent event = JournalEvent.createClearCheckpointEvent(checkPointEvent.checkpointID);
        eventQueue.put(event);
        event.waitForHardening();
        checkPointEvent=null;
    }
    
    /**
     * Get the id of the last checkpoint that was cleared.  This should aid in recovery.  You should only have to
     * replay the events that occured since the last cleared checkpoint was started to ensure data
     * integrety.
     */
    synchronized public long getLastClearedCheckPointID()  {
        return hardener.getLastClearedCheckPointID();
    }
    
    /**
     * Have the journal replay all the event that were recorded.
     */
    synchronized public void replayFromCheckpoint(long checkpointID, ReplayListener listener) throws IOException  {
        hardener.replayFromCheckpoint(checkpointID,listener);
    }

    
    /**
     * You must start journal before it will harden any events added to it.
     */
    synchronized public void start() {
        if (hardnerThread != null)
            throw new IllegalStateException("Running, cannot start.");

        hardnerThread = new Thread(hardener);
        hardnerThread.start();
    }

    /**
     * You must stop the journal for it to stop hardening messages.  This method should block until the 
     * journal stop logging messages.
     */
    synchronized public void stop() throws InterruptedException {
        if (hardnerThread == null)
            throw new IllegalStateException("Not running, cannot stop.");        
        eventQueue.put(JournalEvent.createStopEvent());        
        hardnerThread.join();
        hardnerThread = null;
    }

    /**
     * You should close the journal once you are done with it so that it can
     * release any resources it may have aquired.
     * 
     */
    synchronized void close() throws IOException {
        if (hardener == null)
            throw new IllegalStateException("Allready closed, cannot close again.");        
        hardener.close();
        hardener = null;
    }
    
}
