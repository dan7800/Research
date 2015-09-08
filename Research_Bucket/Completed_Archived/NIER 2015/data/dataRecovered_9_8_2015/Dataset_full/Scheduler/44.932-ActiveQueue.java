/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.active.proto;

import vu.globe.svcs.gls.active.*;
import vu.globe.util.types.Queue;
import vu.globe.util.exc.NotSupportedException;

/**
   An <code>ActiveQueue</code> is a queue which notifies once for each queue
   element ever added, even before the queue is activated. Removal of a queue
   element does not affect the number of notifications for this queue in any
   way.
   <p>
   As an <code>Active</code> object, an <code>ActiveQueue</code>
   must be activated. The user is not required to pass a registration
   ID on deactivation. Only one activation at a time is supported. The queue
   may be activated after elements have been enqueued.
   <p>
   For the purposes of deadlock avoidance, the methods of an
   <code>ActiveQueue</code> are non-blocking downcalling methods. They may be
   safely invoked, or awaited, by other non-blocking methods and callback
   methods. It is guaranteed that notification of the user will not be
   performed by these methods themselves, but will be scheduled until later.

   @author Patrick Verkaik
*/

/*
   Normally an event is passed to the scheduler the moment a queue element is
   added. However, to allow elements to be enqueued before activation, events
   first go through an event counter which buffers events that the scheduler
   needs to be told about. The buffer keeps growing until we and the scheduler
   have been activated. On activation, the buffered events are all passed to
   the scheduler.
   A lock protects access to the buffer, and to the activation state.
   Note that Queue is thread-safe.
*/

public class ActiveQueue extends Queue implements Active
{
   protected Scheduler scheduler;
   protected ActiveRID sched_reg;  // set while activated.

   /**
      Number of events still to tell the scheduler about. Effectively a buffer.
   */
   protected int n_untold = 0;

   /**
      Constructor.
 
      @param sched         the scheduler to use
   */
   public ActiveQueue (Scheduler sched)
   {
      scheduler = sched;
   }

   // Active interface

   public synchronized ActiveRID activate (Notifiable note, ActiveUID userInfo)
   {
      sched_reg = scheduler.activate (note, userInfo);
      addEvent (0);
      return null;
   }

   public ActiveRID activate (int service, Notifiable note, ActiveUID userInfo)
   {
      throw new NotSupportedException ("activate with 'service'");
   }

   public synchronized void deactivate (ActiveRID regID)
   {
      scheduler.deactivate (sched_reg);
      sched_reg = null;
   }

   /**
      Adds to the number of events to tell the scheduler about. The scheduler
      is told about buffered events, only if it has been activated.
   */
   private synchronized void addEvent (int n_events)
   {
      n_untold += n_events;

      if (sched_reg != null) {

         while (n_untold > 0) {

            // note: addEvent() is a downcalling method
            scheduler.addEvent (sched_reg);
            n_untold--;
         }
      }
   }

   // overridden Queue methods

   public void enqueue (Object obj)
   {
      super.enqueue (obj);
      addEvent (1);
   }
}
