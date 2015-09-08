/*
 * Copyright (c) Core Developers Network LLC, All rights reserved
 */
package org.objectweb.howl.journal.spi;

import EDU.oswego.cs.dl.util.concurrent.Latch;

/**
 * A JournalEvent is produced by the Journal and passed 
 * to a Hardener for hardening.  There are some JournalEvent
 * object that are control messages to the hardener such
 * as a stop event which is to get a Hardener to stop
 * processing events from the eventQueue.
 * 
 * @version $Revision: 1.1 $ $Date: 2004/01/26 20:59:30 $
 */
public final class JournalEvent {

    static public final int ADD_EVENT_TYPE = 0;
    static public final int STOP_EVENT_TYPE = 1;
    static public final int START_CHECKPOINT_EVENT_TYPE = 2;
    static public final int CLEAR_CHECKPOINT_EVENT_TYPE = 3;

    public int eventType;
    public byte[] data;
    private Latch hardeningLatch;
    public long checkpointID;

    public void waitForHardening() throws InterruptedException {
        hardeningLatch.acquire();
    }

    public void notifyOfHardening() {
        hardeningLatch.release();
    }

    /**
     * @return
     */
    public boolean isWaitignForHardening() {
        return hardeningLatch != null;
    }

    /**
     */
    static public JournalEvent createStartCheckpointEvent(long value) {
        JournalEvent o = new JournalEvent();
        o.eventType = START_CHECKPOINT_EVENT_TYPE;
        o.hardeningLatch = new Latch();
        o.checkpointID = value;
        return o;
    }

    /**
     */
    static public JournalEvent createClearCheckpointEvent(long value) {
        JournalEvent o = new JournalEvent();
        o.eventType = CLEAR_CHECKPOINT_EVENT_TYPE;
        o.hardeningLatch = new Latch();
        o.checkpointID = value;
        return o;
    }
    
    /**
     */
    static public JournalEvent createStopEvent() {
        JournalEvent o = new JournalEvent();
        o.eventType = STOP_EVENT_TYPE;
        return o;
    }

    /**
     */
    static public JournalEvent createAddEvent(byte[] data) {
        JournalEvent o = new JournalEvent();
        o.eventType = ADD_EVENT_TYPE;
        o.data = data;
        return o;
    }

    /**
     * @param session
     * @param ru2
     * @param data2
     * @return
     */
    public static JournalEvent createAddSyncEvent(byte[] data) {
        JournalEvent o = new JournalEvent();
        o.eventType = ADD_EVENT_TYPE;
        o.data = data;
        o.hardeningLatch = new Latch();
        return o;
    }
}
