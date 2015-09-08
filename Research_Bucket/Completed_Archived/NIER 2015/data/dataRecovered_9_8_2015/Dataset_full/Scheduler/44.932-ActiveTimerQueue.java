/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.active.proto;

import vu.globe.svcs.gls.active.*;
import vu.globe.util.exc.AssertionFailedException;
import vu.globe.svcs.gls.stats.Statistics;
import vu.globe.svcs.gls.debug.Debug;
import vu.globe.util.time.Stopwatch;

/**
   The active timer's main data structure is a queue of timers. All users
   share a single queue of time-outs. A thread can be run on this
   <code>Runnable</code> object. The thread generates the
   callbacks by looking at the information in the queue. The scheduler is
   used to ensure mutually exclusion with other callback threads.
   Each timer is represented with an opaque <code>TimerState</code> object.

 
   @author Patrick Verkaik
*/

/*
   Deactivation by a user does not cause all its timers to be removed. Instead
   the thread checks for deactivation.

   The thread keeps on making callbacks for expired timers and removing
   timers belonging to deactivated users. It runs forever.
   The thread is notified whenever changes to the head occur.
*/

class ActiveTimerQueue implements Runnable
{
   public interface TimerState {};

   private Scheduler scheduler;

   // list of time-outs in order of expiry time
   private TimerStateImpl head;

   private Statistics stats;
   private Stopwatch stopwatch;

   /**
      Constructor.

      @param sched
         the system scheduler to make mutual exclusive callbacks through
   */
   public ActiveTimerQueue (Scheduler sched, Statistics stats)
   {
      scheduler = sched;
      this.stats = stats;

      if(Debug.statsTime())
         stopwatch = new Stopwatch();
   }

   /**
      Creates a new timer. The timer is associated with the given registration.
   */
   public TimerState createTimer (ActiveTimerUser user_reg)
   {
      return new TimerStateImpl (user_reg);
   }

   /**
      See <code>ActiveTimer.set</code>.
   */
   public synchronized void set (TimerState t, long millis, ActiveUID user_info)
   {
      TimerStateImpl timer = (TimerStateImpl) t;

      // remove
      if (timer.armed) {
         if (timer == head)
            notify ();
         remove (timer);
      }

      // add
      timer.setRelExpiry (millis);
      addTimer (timer);
      if (timer == head)
         notify ();
      timer.user_cookie = user_info;
   }

   /**
      See <code>ActiveTimer.cancel</code>.
   */
   public synchronized void cancel (TimerState t)
   {
      TimerStateImpl timer = (TimerStateImpl) t;
      if (! timer.armed)
         return;

      if (timer == head)
         notify ();
      remove (timer);
   }

   /**
      See <code>ActiveTimer.armed</code>.
   */
   public synchronized boolean armed (TimerState t)
   {
      TimerStateImpl timer = (TimerStateImpl) t;
      return timer.armed;
   }

   /**
      Links a timer into the list, and sets the 'armed' field.
   */
   private void addTimer (TimerStateImpl timer)
   {
      timer.armed = true;
      TimerStateImpl prev = findPredecessor (timer.absExpiry ());

      if (prev == null) {
         // no predecessor => prepend timer to head

         timer.next = head; // may be null
         if (head != null)
            head.prev = timer;
         head = timer;
         timer.prev = null;
      }
      else {
         // append timer after prev.
         TimerStateImpl next = prev.next; // possibly null

         if (next != null)
            next.prev = timer;
         prev.next = timer;

         timer.next = next; // possibly null
         timer.prev = prev;
      }
   }

   /**
      Looks for the entry with the latest time still smaller than
      <code>abs_time</code>. Returns null if there is no such entry.
   */
   private TimerStateImpl findPredecessor (long abs_time)
   {
      if (head == null || head.absExpiry () >= abs_time) {
         // all entries >= abs_time
         return null;
      }

      TimerStateImpl entry;
      for (entry = head; ; entry = entry.next) {

         // start of loop: 'entry' < abs_time

         TimerStateImpl look_ahead = entry.next;
         if (look_ahead == null || look_ahead.absExpiry () >= abs_time)
            break; // 'entry' passes the test
      }
      return entry; // never null
   }

   /**
      Unlinks a timer from the list, and clears the 'armed' field.
      Returns the removed element.
   */
   private TimerStateImpl remove (TimerStateImpl timer)
   {
      timer.armed = false;

      TimerStateImpl prev = timer.prev;
      TimerStateImpl next = timer.next;

      timer.next = timer.prev = null;

      if (prev != null)
         prev.next = next;  // possibly null
      else
         head = next; // possibly null

      if (next != null)
         next.prev = prev;  // possibly null

      return timer;
   }

   /**
      Runnable interface.
   */
   public void run ()
   {
      while (true) {
         synchronized (this) {
            if (head == null) {
               try { wait (); } // wait indefinitely
               catch (InterruptedException exc) {
                  System.err.println ("ls timer thread ignoring interruption");
                  // no harm done
               }
            }
            else {
               long wait_time = head.relExpiry ();
               if (wait_time > 0) {
                  try { wait (wait_time); }
                  catch (InterruptedException exc) {
                     System.err.println (
		     		"ls timer thread ignoring interruption");
                     // no harm done
                  }
               }
            }
         }
        
         if(Debug.statsTime())
            stopwatch.start();

         // Process entries at the head of the list which have expired or
         // belong to deactivated users. Stop when no further progress can be
         // made. Note that we do not attempt to catch all cancelled timers
         // and deactivated users: such atomicity is not guaranteed to the user.
         boolean progressing = true;
         while (progressing) {

            // The callback may not be made under the object lock (see LS doc).
            // 'expired' is set to non-null if a callback should be made.
            TimerStateImpl expired = null;
            synchronized (this) {
            
               if (head == null)
                  progressing = false;

               else if (head.user.isDeactivated ())
                  remove (head);

               else if (head.expired ())
                  expired = remove (head);
         
               else
                  progressing = false;
            }
         

            if (expired != null) {
               scheduler.schedCallBack (expired.user.getNotifiable (),
                                        expired.user_cookie);
            }
         }

         if(Debug.statsTime())
         {
            stopwatch.stop();
            stats.addActiveTimerTime(stopwatch);
         }
      }
   }

   /**
      A timer state holds an expiry time, a user registration, a user's callback
      cookie, whether the timer is still armed, and information that links it
      into a list of timer ids. The representation of the expiry time is kept
      hidden. Relative time may be negative!
   */
   private static class TimerStateImpl implements TimerState
   {
      // true while timer is set and is in the queue
      public boolean armed;
   
      // absolute expiry time.
      private long abs_expiry;
   
      public ActiveTimerUser user;
      public ActiveUID user_cookie;
   
      // list of TimerStateImpl. valid iff armed.
      public TimerStateImpl next; 
      public TimerStateImpl prev;
   
      /**
         Constructs this timer id with a user registration.
      */
      public TimerStateImpl (ActiveTimerUser user)
      {
         this.user = user;
      }
   
      public void setRelExpiry (long rel_expiry)
      {
         abs_expiry = System.currentTimeMillis () + rel_expiry;
      }
   
      public void setAbsExpiry (long abs_expiry)
      {
         this.abs_expiry = abs_expiry;
      }
   
      /**
         Returns this timer id's absolute expiry time.
      */
      public long absExpiry ()
      {
         return abs_expiry;
      }
   
      /**
         Returns this timer id's relative expiry time, possibly negative.
      */
      public long relExpiry ()
      {
         return abs_expiry - System.currentTimeMillis ();
      }
   
      /**
         Checks if this timer has expired.
      */
      public boolean expired ()
      {
         return abs_expiry <= System.currentTimeMillis ();
      }
   }
}
