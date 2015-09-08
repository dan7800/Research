/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.active.proto;

import vu.globe.svcs.gls.active.*;
import vu.globe.util.exc.AssertionFailedException;
import vu.globe.util.exc.NotSupportedException;
import vu.globe.util.thread.*;
import java.util.*;

import vu.globe.svcs.gls.config.ObjEnv;
import vu.globe.svcs.gls.config.EnvObject;
import vu.globe.svcs.gls.stats.Statistics;
import vu.globe.util.time.Stopwatch;
import vu.globe.svcs.gls.debug.Debug;

/**
   Callbacks are always scheduled in mutual exclusion. However, different
   threads may be used, depending on the situation:
   <ul>
   <li>
      The scheduler has a thread capable of handling events. This thread
      ensures that all events will get handled.
   <li>
      As an optimisation, when scheduling a callback (whether it is an event or
      not) a caller can indicate that the caller's thread may be used to also
      schedule events added by other threads. In doing so a switch to the
      scheduler's thread can be avoided.
   </ul>
   Note that the scheduler has the opportunity to handle events
   created and added during (in particular <i>by</i>) a callback efficiently
   simply by using the same thread to handle the new events as well. Also note
   that callbacks should avoid long-term blocking so using a caller's thread
   in this way shouldn't be a problem.
 
   @author Patrick Verkaik
*/

/*
   The general rule for callbacks is that no locks should be held during the
   callback that may also be required by downcalling threads. (See the LS
   documentation.) Different callbacks threads must contend to make mutually
   exclusive callbacks, and need a lock. Since the object lock is already used 
   to protect data shared by all threads (including downcalling threads), it 
   cannot be used for this purpose.

   A separate lock is therefore used to make the callbacks. This lock takes
   the shape of a mutex. (Using a Java monitor turns out to be too complex.)
   The mutex is contended for only by upcalling threads. The mutex serves two
   purposes. It not only ensures mutually exclusive callbacks, it is also used 
   by the callback threads to decide among themselves who is going to handle 
   pending events. The decision is made thus: a thread holding the mutex 
   undertakes to handle all events added while the mutex is held. This allows 
   other upcalling threads to add an event and leave the scheduling of the 
   event to a thread which is already busy (if any). Only if no other thread is 
   holding the mutex will a thread have to handle an event it has just added.

   The scheduler has an internal thread which can deal with events added by
   downcalling threads. It is awoken when a downcalling thread adds an event.
   (As a general rule, downcalling threads are never allowed to make
   callbacks.) However it is possible that a callback is already in progress
   (i.e. the mutex is locked) at an event is added by a downcalling thread. (In
   fact this downcalling thread can be that very same callback thread, since an
   upcalling thread may reverse direction and become a downcalling thread.) In 
   that case the internal thread does not need to be woken up since the 
   callback thread has undertaken to handle all events added while it holds the 
   mutex.

   Note that from the above it follows that threads that want to make a 
   non-event callback (i.e. call schedCallBack()) are still obliged to handle
   events added by other threads.
*/

public class SchedulerImpl implements Scheduler, EnvObject
{
   /**
      Vector of registrations. Each registration is of type EventRID. There
      is one registration per activated user.
   */
   private Vector registrations;

   /** All the events in all registrations added together. */
   private int event_count;

   /**
      The lock that protects mutual exclusion of callbacks. This lock may only
      be held by upcalling threads. A thread that holds the lock undertakes
      to schedule all events added during the time the lock is held by it.
   */
   private Mutex callback_lock;

   // statistics
   private Statistics stats;

   // for statistics
   private Stopwatch stopwatch;

   public SchedulerImpl ()
   {
      registrations = new Vector ();
      event_count = 0;
      callback_lock = new Mutex ();

      // prefer not to visibly implement Runnable
      Runnable runner = new Runnable () {
         public void run () {
            run_internal_thread ();
         }
      };

      Thread internal_thread = new Thread (runner, "sched");
      internal_thread.setDaemon (true);
      internal_thread.start ();

      if(Debug.statsTime())
         stopwatch = new Stopwatch();
   }

   // EnvObject interface

   public void initObject (ObjEnv env)
   {
      stats = env.getStatistics();
   }

   // Active interface

   public synchronized ActiveRID activate (Notifiable note, ActiveUID userInfo)
   {
      EventRID reg = new EventRID (note, userInfo);

      registrations.addElement (reg);
      return reg;
   }

   /**
      Not supported.
   */
   public ActiveRID activate (int service, Notifiable note, ActiveUID userInfo)
   {
      throw new NotSupportedException ("service ids");
   }

   public synchronized void deactivate (ActiveRID regID)
   {
      EventRID reg = (EventRID) regID;

      if (! registrations.removeElement (reg))
         throw new IllegalArgumentException ("regID");

      event_count -= reg.event_count;
   }

   // Scheduler interface

   /**
      Adds an event to be scheduled. The event is associated with the given
      registration.
   */
   public void addEvent (ActiveRID regID)
   {
      addEvent (regID, false);
   }

   /**
      Adds an event to be scheduled. The event is associated with the given
      registration.
      <p>
      In addition the caller's thread may be used to handle any events
      currently waiting. Downcalling threads should never invoke this method.
      They should use <code>addEvent</code> instead.
   */
   public void schedEvent (ActiveRID regID)
   {
      addEvent (regID, true);
   }

   /**
      Adds an event to be scheduled. The event is associated with the given
      registration.
      <p>
      The <code>handle_events</code> parameter indicates whether
      the caller's thread may be used to handle the events currently waiting.
      Downcalling threads should never set this parameter to true.
   */
   private void addEvent (ActiveRID regID, boolean handle_events)
   {
      synchronized (this) {
         EventRID reg = (EventRID) regID;
         reg.event_count++;
         event_count++;

         // wake up the scheduler thread but not if either the called is
         // allowing us to handle pending events or if another thread has
         // already undertaken to do so.
         if (! handle_events && ! callback_lock.testLock ())
            notify ();
      }
      if (handle_events)
         schedule ();
   }

   public void schedCallBack (Notifiable note, ActiveUID user_info)
   {
      // Take the lock, ignoring any InterruptedExceptions.
      boolean locked = false;
      while (! locked) {
         try {
	         callback_lock.lock ();
	         locked = true;
	      }
         catch (InterruptedException exc) {
            System.err.println (Thread.currentThread ().getName () +
                              " ignoring interruption");
	      }
      }

      try { note.notifyEvent (user_info); }
      catch (Exception exc) {

         System.err.println (Thread.currentThread ().getName () +
                              " ignoring exception from callback: ");
         exc.printStackTrace ();
      }
         
      callback_lock.unlock ();

      // having held the callback lock we have promised to handle events
      schedule ();
   }

   /**
      Schedules pending events, but only if no other thread has undertaken to
      do so. This method should never be called with the object lock held.
   */
   private void schedule ()
   {
      synchronized (this) {
         if (event_count == 0 || ! callback_lock.tryLock ())
            return;
      }

      // The callback lock is now held and we therefore undertake to perform
      // any events added up until the point that the callback lock is released.
      // By releasing the callback lock under the object lock we can ensure
      // that no added events are missed due to race conditions.
      while (true) {
         EventRID reg;
         synchronized (this) {
            if (event_count == 0) {
               callback_lock.unlock ();
               return;
            }

            // get and remove work unit
            reg = findReadyReg (); // never null
            reg.event_count--;
            event_count--;
         }

         // as already explained the object lock may not be held at this point
         try { reg.note.notifyEvent (reg.info); }
         catch (Exception exc) {

            System.err.println (Thread.currentThread ().getName () +
                                 " ignoring exception from callback: ");
            exc.printStackTrace ();
         }
      }
   }

   private synchronized EventRID findReadyReg ()
   {
      if (event_count == 0)
         return null;

      // currently no attempt at fairness

      Enumeration enum = registrations.elements ();
      while (enum.hasMoreElements ()) {
         EventRID reg = (EventRID) enum.nextElement ();
         if (reg.event_count > 0)
            return reg;
      }
      return null;
   }

   /**
      Method executed by the scheduler's thread. Just keeps running forever.
   */
   private void run_internal_thread ()
   {
      while (true) {
         synchronized (this) {
            while (event_count == 0) {
               try { wait (); }
               catch (InterruptedException exc) {
		            System.err.println ("ls sched thread ignoring interruption");
               }
            }
         }

         if(Debug.statsTime())
            stopwatch.start();

         // must not hold the object lock at this point
         schedule ();

         if(Debug.statsTime())
         {
            stopwatch.stop();
            stats.addSchedulerTime(stopwatch);
         }
      }
   }

}

class EventRID extends Callback implements ActiveRID
{
   public int event_count;

   public EventRID (Notifiable n, ActiveUID a)
   {
      super (n, a);
      event_count = 0;
   }
}
