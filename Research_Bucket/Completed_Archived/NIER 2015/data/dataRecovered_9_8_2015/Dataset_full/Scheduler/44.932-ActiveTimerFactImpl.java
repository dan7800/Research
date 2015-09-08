/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.active.proto;

import vu.globe.svcs.gls.active.*;
import vu.globe.svcs.gls.active.ActiveTimerFact.ActiveTimer;
import vu.globe.util.exc.NotSupportedException;

import vu.globe.svcs.gls.stats.Statistics;

/**
   An <code>ActiveTimerFact</code> implementation.
 
   @author Patrick Verkaik
*/

/*
   The bulk of the work is performed by ActiveTimerQueue. The <code>
   ActiveTimerQueue</code> runs a timer thread which produces the callbacks
   on the timers. Note that queue modifications are necessary both as a result
   of user threads setting and clearing timers and of the timer thread handling
   expired timers.

   Each timer has two aspects: user access through the ActiveTimer interface,
   and a timer's state operated on by the ActiveTimerQueue. These two
   aspects are implemented by separate objects.
   The object representing a timer to the user is little more than a wrapper
   around an internal timer object of type TimerState, defined by the
   ActiveTimerQueue. The state of each timer is in the TimerState. An
   ActiveTimer object simply forwards each of its calls to the
   ActiveTimerQueue passing it the TimerState object.

   A more naive implementation which combines an ActiveTimer's user access with
   a TimerState's state in one object risks deadlocking due to user threads
   and the internal timer thread grabbing locks in a different order.
*/

public class ActiveTimerFactImpl implements ActiveTimerFact
{
   private ActiveTimerQueue queue;
   
   /**
      Constructor.
 
      @param sched         the scheduler to use
   */
   public ActiveTimerFactImpl (Scheduler sched, Statistics stats)
   {
      // make a queue
      queue = new ActiveTimerQueue (sched, stats);

      // start a thread
      Thread timer_thread = new Thread (queue, "timer-thread");
      timer_thread.setDaemon (true);
      timer_thread.start ();
   }
 
   // Active interface
 
   public ActiveRID activate (Notifiable note, ActiveUID userInfo)
   {
      // the user info is ingored.
      return new ActiveTimerUser (note);
   }
 
   public ActiveRID activate (int service, Notifiable note, ActiveUID userInfo)
   {
      throw new NotSupportedException ("activate with 'service'");
   }

   public void deactivate (ActiveRID regID)
   {
      ActiveTimerUser reg = (ActiveTimerUser) regID;
      reg.deactivate ();
   }

   public ActiveTimer createTimer (ActiveRID reg_id)
   {
      ActiveTimerQueue.TimerState state =
                     queue.createTimer ((ActiveTimerUser) reg_id);
      return new ActiveTimerImpl (state);
   }

   /**
      The wrapper around a timer's state. An inner class.
   */
   private class ActiveTimerImpl implements ActiveTimer
   {
      private ActiveTimerQueue.TimerState state;

      public ActiveTimerImpl (ActiveTimerQueue.TimerState t)
      {
         state = t;
      }

      public void set (long millis, ActiveUID user_info)
      {
         queue.set (state, millis, user_info);
      }
 
      public void cancel ()
      {
         queue.cancel (state);
      }
 
      public boolean armed ()
      {
         return queue.armed (state);
      }
   }
}
