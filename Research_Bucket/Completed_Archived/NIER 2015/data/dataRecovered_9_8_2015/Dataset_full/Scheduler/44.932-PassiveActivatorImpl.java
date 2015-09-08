/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.active.proto;

import vu.globe.svcs.gls.active.*;
import vu.globe.util.exc.NotSupportedException;

import vu.globe.svcs.gls.stats.Statistics;
import vu.globe.svcs.gls.debug.Debug;
import vu.globe.util.time.Stopwatch;

/**
   A <code>PassiveActivator</code> implementation.
 
   @author Patrick Verkaik
*/

/*
   -  Passive.awaitEvent is actually a blocking method since old unhandled
   events may cause it to wait for another thread to handle the old
   events. We are therefore breaking a deadlock avoidance rule by invoking it
   within a callback thread (in
   run_thread()). The reason we can do this is that it would be unreasonable
   for the user to expect notifications from us to keep coming in for new events
   when the user hasn't handled old notifications. (Note: passive objects are
   typically communication objects.) The user will either have to handle events
   immediately or provide itself with
   some other trigger, such as a timer, to guarantee continued handling of old
   events. (Note passive objects have their own callback threads.) Nevertheless,
   this is one of the murkier areas of the overall design.

   -  Since passive activators may be created in large numbers, and each one
   occupies a thread, a passive activator's thread is cleaned up on
   deactivation. Note that only one activation can be made.
*/
public class PassiveActivatorImpl implements PassiveActivator
{
   private Scheduler scheduler;        

   // set while activated.
   private Notifiable user_notifiable;
   private ActiveUID user_cookie;
   private Passive passive;
   private Thread thread;

   // deactivate() sets this to true if the thread should terminate itself.
   private boolean must_deactivate;

   private Statistics stats;
   private Stopwatch stopwatch;

   /**
      Constructor.

      @param sched         the scheduler to use
   */
   public PassiveActivatorImpl (Scheduler sched, Statistics stats)
   {
      scheduler = sched;
      this.stats = stats;

      if(Debug.statsTime())
         stopwatch = new Stopwatch();
   }

   public void activate (Passive pass, Notifiable note, ActiveUID userInfo)
   {
      // note: not intended to be a fool-proof check
      if (passive != null)
         throw new IllegalStateException ("was activated before");

      passive = pass;
      user_notifiable = note;
      user_cookie = userInfo;

      must_deactivate = false;

      // prefer not to visibly implement Runnable
      Runnable runner = new Runnable () {
         public void run () {
            run_thread ();
         }
      };

      thread = new Thread (runner, "passive-thr");
      thread.setDaemon (true);
      thread.start ();
   }

   public void deactivate ()
   {
      synchronized (this) {
         must_deactivate = true;
      }
      thread.interrupt ();
   }

   public Passive getPassive ()
   {
      return passive;
   }

   /**
      Keeps waiting for the passive object to become ready, and scheduling an
      event in the scheduler. To kill the thread, 'must_deactivate' must be set
      to true, after which the thread must also be interrupted.
   */
   private void run_thread ()
   {
      while (true) {

         try {
            // blocking a callback thread: see implementation notes earlier
            passive.awaitEvent ();

            if(Debug.statsTime())
               stopwatch.start();

            // User callback. Must not be made under the object lock (see LS
            // doc). We can allow the scheduler to use this thread, since we
            // know we are not in a down call.
            scheduler.schedCallBack (user_notifiable, user_cookie);

            if(Debug.statsTime())
            {
               stopwatch.stop();
               stats.addPassiveActivatorTime(stopwatch);
            }

         }
         catch (InterruptedException exc) {
            // if due to must_deactivate then handled below
         }

         synchronized (this) {
            if (must_deactivate)
               return;
         }

      }
   }
}
