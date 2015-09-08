/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.ofbiz.minerva.pool;

import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * Runs garbage collection on all available pools.  Only one GC thread is
 * created, no matter how many pools there are - it just tries to calculate
 * the next time it should run based on the figues for all the pools.  It will
 * run on any pools which are "pretty close" to their requested time.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Rev: 5462 $
 */
class PoolGCThread extends Thread {

    private HashSet pools = new HashSet();
    private static Logger log = Logger.getLogger(ObjectPool.class);

    PoolGCThread() {
        super("Minerva ObjectPool GC Thread");
        setDaemon(true);
    }

    public void run() {
        boolean trace = true;
        while (true) {
            // Don't do anything while there's nothing to do
            waitForPools();
            if (trace)
                log.debug("gc thread waited for pools");

            // Figure out how long to sleep
            long delay = getDelay();
            if (trace)
                log.debug("gc thread delay: " + delay);

            // Sleep
            if (delay > 0l) {
                try {
                    sleep(delay);
                } catch (InterruptedException ignored) {
                }
            }

            // Run garbage collection on eligible pools
            runGC();
        }
    }

    private synchronized void waitForPools() {
        while (pools.size() == 0) {
            try {
                wait();
            } catch (InterruptedException ignored) {
                // Warning level seems appropriate; shouldn't really happen
                log.warn("waitForPools interrupted");
            }
        }
    }

    private synchronized long getDelay() {
        long next = Long.MAX_VALUE;
        long now = System.currentTimeMillis();
        long current;

        for (Iterator it = pools.iterator(); it.hasNext();) {
            ObjectPool pool = (ObjectPool) it.next();
            current = pool.getNextGCMillis(now);
            if (current < next)
                next = current;
        }
        return next >= 0l ? next : 0l;
    }

    private synchronized void runGC() {
        boolean trace = true;

        if (trace)
            log.debug("GC thread running GC");

        for (Iterator it = pools.iterator(); it.hasNext();) {
            ObjectPool pool = (ObjectPool) it.next();

            if (trace)
                log.debug("GC Thread pool: " + pool.getName() + ", isTimeToGC(): " + pool.isTimeToGC());

            if (pool.isTimeToGC())
                pool.runGCandShrink();
        }
    }

    synchronized void addPool(ObjectPool pool) {
        if (log.isDebugEnabled())
            log.debug("Adding pool: " + pool.getName() + ", GC enabled: " + pool.isGCEnabled());

        if (pool.isGCEnabled())
            pools.add(pool);

        notify();
    }

    synchronized void removePool(ObjectPool pool) {
        if (log.isDebugEnabled())
            log.debug("Removing pool: " + pool.getName());

        pools.remove(pool);
    }
}

/*
vim:tabstop=3:et:shiftwidth=3
*/
