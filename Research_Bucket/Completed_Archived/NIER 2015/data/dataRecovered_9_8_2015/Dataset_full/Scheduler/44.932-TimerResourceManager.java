/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.objsvr.perstm;

import vu.globe.idlsys.g;
import vu.globe.svcs.objsvr.idl.resource.*;
import vu.globe.util.debug.*;

import java.io.*;
import java.lang.*;
import java.util.*;
import java.net.*;

public class TimerResourceManager implements Runnable {

    // The thread that keeps time and notifies us when time has run out
    // Need reference so we can cleanly stop it when exitting.
    private Thread _timeThread = null;

    // timerID counter
    private long _timerID = 0;

    // Some list, sorted ?
    Vector _timerTasks = null;

    // If we should stop (not sure if needed, could call exit() instead).
    private boolean _stopped = false;

    // pause/unpause stuff
    private boolean _paused = false;
    private boolean _pausedWaiting = false;


    public TimerResourceManager() {

        // Create the Vector which holds the timer tasks
        _timerTasks = new Vector();

        // Only start thread when there is something to do
    }


    /** This is the run method of the created thread. */
    public void run() {
        long currentTime = 0;
        long waitTime = 0;
       
        DebugOutput.println(DebugOutput.DBG_DEBUG,
                    "TimerResourceManager: Starting time thread");
        synchronized(_timerTasks) {
            while (!_stopped) {

                waitTime = 0;
                try {
                    // If there are no timer tasks yet, wait(0) until notified
                    // Otherwise find out how long we must sleep
                    if (!_timerTasks.isEmpty()) {
                        currentTime = System.currentTimeMillis();
                        waitTime = ((TimerTask) _timerTasks.firstElement()).
                                    getAlarmTime() - currentTime;

                        if (waitTime < 0) {
                            DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "Adjusting waitTime from "+waitTime+" to 1");
                            waitTime = 1;
                        }
                    } else {
                        // Stop this thread, will restart when needed
                        DebugOutput.println(DebugOutput.DBG_DEBUG,
                                           "TimerResourceManager: Timer thread"+
                                           " has no more tasks, exitting...");
                        return;
                    }

                    DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "TimerResourceManager: waiting: "+waitTime);

                    // Now wait here till it is time to do a task or when there
                    // was an insertion/removal
                    _timerTasks.wait(waitTime);

                    // Check that we are not paused
                    while (_paused) {
                        _pausedWaiting = true;
                        // As long as we are paused, do not continue.
                        // We do however need to let go of the lock that
                        // we hold on TimerTasks, because pop-up threads
                        // and such might need access to us, in order for
                        // the objects they belong to to pause. If we would
                        // not do this, we would get a deadlock situation
                        // in which the timer thread is already paused but still
                        // holds the lock and pop-up threads try to acquire the
                        // lock in order for them to finish such that the object
                        // they belong to can pause as well.
                        DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "TimerResourceManager: PAUSE wait...");
                        try {
                            _timerTasks.wait();
                        } catch (Exception e) {
                                // ignore
                        }
                    }

                    // Wakey Wakey, let's see if there is something to do
                    if (_timerTasks.isEmpty()) {
                        continue;
                    }

                    // What time is it ?
                    currentTime = System.currentTimeMillis();

                    // Who needs to be do something
                    int[] finishedTimers = new int[_timerTasks.size()];
                    int fTCnt = 0;
                    
                    for (int i=0; i < _timerTasks.size(); i++) {
                        TimerTask t = (TimerTask) _timerTasks.get(i);

                        if (t.shouldRun(currentTime)) {
                            // Run it
                            boolean done = false;
                            try {
                                done = t.run();
                            } catch (Exception e) {
                                // remove it
                                done = true;
                            }
                            if (done) {
                                finishedTimers[fTCnt] = i;
                                fTCnt++;
                            }
                        } else {
                            // End of runnable tasks, time for resorting
                            break;
                        }
                    }

                    // Cleanup
                    // NOTE Delete highest to lowest ! Indices get updated after
                    // remove and thus don't match anymore with our to be 
                    // removed indices if we remove one with lower index than 
                    // next one
                    for (int j = (fTCnt-1); j >= 0; --j) {
                        _timerTasks.remove(finishedTimers[j]);
                    }

                    // Re-sort the timerTasks
                    Collections.sort(_timerTasks);
                } catch (InterruptedException e) {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                            "TimerResourceManager: thread got interrupted !");
                }
            }
        }
    }

    public long schedule(timerResourceManagerCB cb, g.opaque user,
                         long interval, long firstInterval) throws Exception {

        // Create a new timer task
        long timerID = getNextTimerID();
        TimerTask t = new TimerTask(timerID, cb, user, interval, firstInterval);

        synchronized(_timerTasks) {

            if (_timerTasks.isEmpty()) { // Don't use _timerThread==null
                                         // because reference is kept after exit
                // Start our internal thread
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                                    "TimerResourceManager: creating thread!");
                _timeThread = new Thread(this);
                _timeThread.start();
            }
                
            _timerTasks.add(t);

            // Notify timer thread of the addition
            _timerTasks.notify();
        }
        DebugOutput.println(DebugOutput.DBG_DEBUG,
            "TimerResourceManager: created task "+timerID+" starting in "+
            firstInterval+" millis with interval of "+interval+" millis");
        
        return timerID;
    }

    public void reschedule(long timer, long interval, long firstInterval) 
            throws Exception {
        synchronized(_timerTasks) {
            Iterator it = _timerTasks.iterator();
            while (it.hasNext()) {
                TimerTask t = (TimerTask) it.next();
                if (t.getTimerID() == timer) {
                    t.updateTimer(interval, firstInterval);
                    break;
                }
            }
            
            // Notify threads
            _timerTasks.notify();
        }
    }
    
    public void cancel(long timerID) throws Exception {

        TimerTask toBeRemoved = null;
        boolean found = false;
        int indexToBeRemoved = -1;
        
        synchronized(_timerTasks) {
            for (int i=0; i < _timerTasks.size(); i++) {
                toBeRemoved = (TimerTask) _timerTasks.get(i);
                if (toBeRemoved.getTimerID() == timerID) {
                    indexToBeRemoved = i;
                    found = true;
                    break;
                }
            }

            // Check that we found something
            if (found) {
                _timerTasks.remove(indexToBeRemoved);

                // Notify time thread of removal
                _timerTasks.notify(); 
            } else {
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "TimerResourceManager: could not find task to cancel: "+
                        timerID);
            }
        }
    }

    public void stop() throws Exception {
   
        TimerTask t = null;
        
        // Got to stop it
        _stopped = true;

        synchronized(_timerTasks) {
            // Tell everyone they are cancelled !
            Iterator it = _timerTasks.iterator();
            while (it.hasNext()) {
                t = (TimerTask) it.next();

                // Notify the object of this timer task of the cancellation
                t.cancel();
            }

            _timerTasks.clear();
            _timerTasks.notify();
        }
    }

    // Tell the timerThread that we are in pause mode
    // It is not allowed to run any timer tasks, during this time
    public void pause() {

        // Get a lock on the timerTasks
        synchronized(_timerTasks) {
            _paused = true;
        }
    }

    public void unpause() {
        // Do not get a lock on the timer tasks, if the timer thread is indeed
        // waiting for our unpause, it still holds the lock on TimerTasks.
        // We did the wait on the _pauseLock and not on TimerTasks, so that lock
        // was never released.
        synchronized(_timerTasks) {
            _paused = false;

            // Only notify if it is waiting on us
            if (_pausedWaiting) {
                _timerTasks.notify();
                _pausedWaiting = false;
            }
        }
    }
    
    
    private synchronized long getNextTimerID() {
        // with 64 bits, wrap around, there should be enough IDs
        _timerID++;

        if (_timerID <= 0) {
            _timerID = 1;
        }

        return _timerID;
    }

    private class TimerTask implements Comparable {

        private long _timerID;
        private long _interval = 0;
        private long _alarmTime = 0;
        private timerResourceManagerCB _cb = null;
        private g.opaque _user = null;
        private boolean _isRescheduled = false;
        

        public TimerTask(long timerID, timerResourceManagerCB cb, g.opaque user,
                         long interval, long firstInterval) throws Exception {
            _timerID = timerID;

            if (((interval > 0) && (interval <= 10)) 
                || firstInterval < 0) {
                throw new Exception("Interval value too low!");
            }
            _interval = interval;   // Sanity check, should be > than 1 sec ?
            _alarmTime = System.currentTimeMillis() + firstInterval;
            _cb = cb;
        }

        public void updateTimer(long interval, long firstInterval)
                throws Exception {

            if (((interval > 0) && (interval <= 10))
                || firstInterval < 0) {
                throw new Exception("Interval value too low!");
            }
            _interval = interval;
            _alarmTime = System.currentTimeMillis() + firstInterval;
            _isRescheduled = true;
        }
        
        
        public boolean shouldRun(long currentTime) {
            if ((_alarmTime - currentTime) <= 50) { // 50 milli granularity
                return true;
            }

            return false;
        }

        public boolean run() throws Exception {
           
            // Call the object on its callback
            _cb.run(_timerID, _user);

            if (_interval == 0) {
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                                    "TimerTask "+_timerID+" is done");
                return true;
            } else {
                // Calculate next time slot, don't use current time otherwise we
                // will drift !
                if (!_isRescheduled) {
                    _alarmTime += _interval;
                } else {
                    _isRescheduled = false;
                }
                return false;
            }
        }

        public void cancel() throws Exception {
            _cb.cancel(_timerID, _user);
        }
        
        public long getTimerID() {
            return _timerID;
        }
        
        public long getAlarmTime() {
            return _alarmTime;
        }

        // Comparable interface
        public int compareTo(Object o) throws ClassCastException {

            long oAlarmTime = ((TimerTask) o).getAlarmTime();

            if (_alarmTime < oAlarmTime) {
                return -1;
            } else if (_alarmTime == oAlarmTime) {
                return 0;
            } else {
                return 1;
            }
        }
    }
}
