/*
 * Copyright (c) Core Developers Network LLC, All rights reserved
 */
package org.objectweb.howl.journal.spi;

import java.io.File;
import java.io.IOException;

import EDU.oswego.cs.dl.util.concurrent.Channel;

import org.objectweb.howl.journal.ReplayListener;


/**
 * A Hardener is responsible for doing all the real Jouraling
 * IO work.
 *
 * Hopefully we will have multiple Hardener implementations which
 * vary in terms of complexity and speed.  Different applicaions
 * will have different journaling performance and recovery requirements. 
 * Picking the right Hardener of the Journal should be what what
 * is need to accomodate those diffences.
 * 
 * A Hardener should be a regular Java Bean:
 *  * public default constructor
 *  * getter and setters methds for configuration
 *  * and it must implement this Hardener interface.
 * 
 * The hardener life cycle will be:
 *  1. construction
 *  2. set methods called to configure hardener options.
 *  3. open(...) method which should open/reopen the journal
 *  4. replayFromCheckpoint(...) 
 *  5. Then any of the following in any order and/or concurrently:
 *     * run()
 *     * getNextCheckPointID()
 *     * getLastClearedCheckPointID()
 *  6. close()
 * 
 * The run method should put the hardener into a processing
 * loop in which it will receive JournalEvent objects
 * from the eventQueue object passed to it in the open(...) method
 * call.
 *   
 * @version $Revision: 1.1 $ $Date: 2004/01/26 20:59:30 $
 */
public interface Hardener extends Runnable {

    void open(File journal, Channel eventQueue) throws IOException;
    void close() throws IOException;
    long getNextCheckPointID();
    long getLastClearedCheckPointID();
    void replayFromCheckpoint(long checkpointID, ReplayListener listener) throws IOException;
}