/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.util.thread;

import vu.globe.util.exc.AssertionFailedException;
import vu.globe.util.exc.NotSupportedException;
import vu.globe.util.debug.DebugOutput;
import java.util.*;

import vu.globe.idlsys.g;

/**
 * A scheduler of events. An event is a callback invocation. A client can make
 * a request for an event to occur by registering the event. When the event is
 * scheduled, an event handler installed by the client will be invoked by a
 * scheduler thread.
 * <p>
 * If several events are regularly registered by the same client, the client
 * should install a single event handler for the events. Any number of events
 * can be registered for that event handler. When done, the installed handler
 * should be removed. Alternatively, a client that only registers an event
 * sporadically, should pass a 'one shot' event handler with the event
 * registration. This kind event handler need not be removed by the client.
 * <p>
 * An EventScheduler dynamically adjusts the number of scheduler threads.
 * However, each event handler will be invoked by at most one thread at a time.
 *
 * @author Patrick Verkaik
 */

/*
  Notes about multiple scheduler threads, and the guardian thread
  ---------------------------------------------------------------

    Thread allocation
    -----------------

      Multiple scheduler threads were introduced to cope with blocking
      behaviour in event handlers. In particular, msSlave has this behaviour.
      I'll illustrate the problem with blocking event handlers using msSlave as
      an example:

	The Master/Slave replication objects are layered over a multiplexer
	which uses this EventScheduler to service (make callbacks for) incoming
	communication (both incoming connection requests and incoming data).

	A slave replica may set up a connection to the master replica as part
	of handling a read request from one of its clients (see the
	implementation of msSlave), i.e. this is performed by the scheduler
	thread that delivers the read request to the slave. Part of the
	connection set-up is an object ID check, which consists of a request
	message transmitted from the slave to master, followed by a reply back
	from the master to the slave which contains the object ID. After
	sending the request, the slave will wait for this reply to come in.
	However, if both incoming client and master communication to the slave
	are serviced by one thread, then a deadlock arises: the thread that is
	servicing the client's read request is now waiting for a message from
	the master, but the message from the master can only be serviced by the
	same thread.  

	Multiple threads allow communication from the client and the master to
	be serviced by different threads, and should normally prevent the above
	deadlock from happening. However, no *fixed* number of scheduler
	threads can completely prevent slaves from locking up. It is possible
	to apply the above scenario to multiple slaves, with each scheduler
	thread handling a client read request and waiting for a message from
	the master of that slave. In that case there are no threads left to
	deliver the messages from any of the masters.

      The solution chosen to deal with this problem is to allow the number of
      threads to vary dynamically. The model is that there is a pool of
      scheduler threads that will invoke event handlers that have events
      pending (are 'ready').  When a scheduler thread invokes an event handler
      it is removed from the pool (and is 'occupied'); it is returned to the
      pool after is returns from the event handler invocation.  No problem
      occurs as long as any one of the following conditions holds:

        o There are no ready event handlers (i.e. no work to be done).

        o There are scheduler threads in the pool.

        o At least one occupied scheduler thread is running (not blocked). On a
	  single-processor system there is no need to have more than one
	  scheduler thread running.  Also note that until all occupied
	  scheduler threads are blocked, an occupied scheduler thread might
	  return from the event handler and return to the pool.

      If we ignore the third condition, we can solve our problem as follows.
      When we run out of available scheduler threads (i.e. they are all
      occupied invoking event handlers), and we have a ready event handler, we
      simply create an additional scheduler thread.  A problem with this
      approach is that a huge number of scheduler threads will be created (one
      for each ready event handler).  According to the third condition above,
      it is sufficient to add only one scheduler thread, until that thread
      blocks.

      We can limit the number of scheduler threads that are created if we take
      into account the third condition. To do that, we need some way of knowing
      whether occupied scheduler threads are blocked or running. There is no
      precise way of knowing this (the Java threads package does not provide a
      way), but the following solution turns out to be sufficient.  Scheduler
      threads are created by a 'guardian thread' (of which there is only one
      per EventScheduler object), which runs at a lower priority than the
      scheduler threads. Subject to the details of the threads implementation
      of the JVM and the OS, the guardian thread should normally not be able to
      run if at least one scheduler is running, due to the lower priority of
      the guardian thread. In this way, the guardian thread can only create
      scheduler threads when all existing scheduler threads have blocked.  Note
      that the behaviour of the JVM/OS thread scheduler might not strictly
      follow priorities; therefore it remains possible that a (hopefully
      limited) number of superfluous scheduler threads are created.

    Thread deallocation
    -------------------

      The above deals with the creation of scheduler threads. It is also useful
      to destroy scheduler threads when they are not used, e.g. after a burst
      of incoming traffic has caused scheduler threads to be created. However,
      at the same time it is not a good idea to destroy unused threads too
      eagerly, since the required number of threads may increase again. With
      these considerations in mind, thread destruction is implemented as
      follows.  During some period of time, the guardian thread keeps track of
      the required number of scheduler threads (using the same criteria above
      for thread creation), which we call 'concurrency'. Based on this, it
      computes some 'target' number of threads (_target_nthreads).  Scheduler
      threads will only be destroyed if the current number of scheduler threads
      exceeds this target.

      There are several ways to compute such a target.  Here, we compute it as
      follows. The guardian thread records, in _max_concurrency, the largest
      number of scheduler threads required at the same time (i.e.  the largest
      value of 'concurrency'), during a period of GUARDIAN_PERIOD seconds.
      After GUARDIAN_PERIOD seconds have passed, this maximum is copied to
      _prev_max_concurrency, and a new period of GUARDIAN_PERIOD seconds is
      started, during which we record the maximum for the new period in
      _max_concurrency. At the end of the new period, we again copy
      _max_concurrency to _prev_max_concurrency (overwriting the old value),
      and so on. If at any time we wish to compute the minimum number of
      threads, we simply take the larger of _max_concurrency and
      _prev_max_concurrency.  Therefore this minimum corresponds to the maximum
      required number of threads (maximum value of 'concurrency') over the last
      GUARDIAN_PERIOD to 2*GUARDIAN_PERIOD seconds. Note: as the guardian
      thread runs at a low priority (see above), it is possible that longer
      periods are in fact used.
*/


public class EventScheduler
{
  /** A shared scheduler object. */
  public static final EventScheduler sys_sched = new EventScheduler ("system");

  /**
   * List of ready event handlers, each of type HandlerInfo. An event handler
   * is in the list if (a) it has at least one registered event and/or has
   * the 'invoke_sched_stopped' flag set, and (b) is not already assigned to a
   * scheduler thread.
   */
  private final LinkedList _ready_handlers = new LinkedList();

  /** The current number of scheduler threads alive. */
  private int _nthreads = 0;

  /**
   * The number of scheduler threads that should be alive. Computed by the
   * guardian thread. A scheduler thread exits if _nthreads > _target_nthreads.
   */
  private int _target_nthreads = 0;

  /** The number of scheduler threads currently invoking a handler. */
  private int _occupied_nthreads = 0;

  /** The thread that creates scheduler threads, and keeps track of periods. */
  private final GuardianThread _guardian_thread;

  /** Returned by toString(). */
  private final String _my_name;

  /**
   * Constructs an EventScheduler as EventScheduler (name, 1, false).
   */
  public EventScheduler(String name)
  {
    this (name, 5);
  }

  /**
   * Constructs an EventScheduler.
   *
   * @param name	      	the name of this EventScheduler, for debugging
   * @param cfg_min_nthreads	the minimum number of scheduler threads in this
   *				EventScheduler
   */
  public EventScheduler (String name, int cfg_min_nthreads)
  {
    _my_name = name;

    _guardian_thread = new GuardianThread (cfg_min_nthreads);
    _guardian_thread.setDaemon (true);
    _guardian_thread.setPriority (Thread.MIN_PRIORITY);
    _guardian_thread.start();
  }

  protected void finalize() throws Throwable
  {
    // Should destroy the threads here. However, this object will never be
    // finalised by Java, precisely because of these threads.
    super.finalize();
  }

  /**
   * Installs an event handler.
   *
   * @param handler    the event handler
   * @param cookie     the client's cookie for this event handler
   * @return           the scheduler's identifier for this event handler
   */
  public g.opaque installHandler (EventHandler handler, g.opaque cookie)
  {
    HandlerInfo hinfo = new HandlerInfo (handler, cookie);
    // DebugOutput.println ("EventScheduler.installHandler: " + handler);
    return hinfo;
  }

  /**
   * Removes an event handler. Asynchronous: event notification for this
   * handler may briefly continue. After the last event notification has taken
   * place, EventHandler.schedStopped() is called.
   *
   * @param id    the event handler's id, as returned by installHandler
   *
   * @exception IllegalArgumentException   if 'id' is not a valid event handler
   */
  public void removeHandler (g.opaque id)
  {
    synchronized (this) {
      HandlerInfo hinfo = (HandlerInfo) id;
      // DebugOutput.println ("EventScheduler.removeHandler: " + hinfo.handler);
      if (hinfo.invalid)
        throw new IllegalArgumentException();
      DebugOutput.dassert (! hinfo.invoke_sched_stopped);

      hinfo.invalid = true;
      hinfo.invoke_sched_stopped = true;
      if (hinfo.event_count == 0 && ! hinfo.assigned) {

        _ready_handlers.addLast (hinfo);

	// Wake up a scheduler or guardian thread.

	if (_occupied_nthreads < _nthreads) {

          // DebugOutput.println ("EventScheduler.removeHandler: " +
	  //  "waking up scheduler thread");

	  this.notify(); // wake up an unoccupied scheduler thread
	}
	else if (_ready_handlers.size() == 1) {

          // DebugOutput.println ("EventScheduler.removeHandler: " +
	  //   "waking up guardian thread");

	  _guardian_thread.wakeUp(); // note: enters nested synchronized block
	}
	else {

	  // guardian thread already awake
          // DebugOutput.println ("EventScheduler.removeHandler: " +
	  //   "NOT waking up guardian thread");
	}

        // DebugOutput.println ("EventScheduler.removeHandler: readied: "
	//   + hinfo.handler);
      }
      else {

        // DebugOutput.println ("EventScheduler.removeHandler: found readied: "
	//   + hinfo.handler);
      }
    }
  }

  /**
   * Registers an event to be scheduled.
   *
   * @param id    identifies the event handler with which the event should be
   *              scheduled. The id was returned by installHandler. 
   *
   * @exception IllegalArgumentException   if 'id' is not a valid event handler
   */
  public void regEvent (g.opaque id)
  {
    synchronized (this) {
      HandlerInfo hinfo = (HandlerInfo) id;
      // DebugOutput.println ("EventScheduler.regEvent for: " + hinfo.handler);
      if (hinfo.invalid)
        throw new IllegalArgumentException();
      DebugOutput.dassert (! hinfo.invoke_sched_stopped);

      hinfo.event_count++;
      if (hinfo.event_count == 1  && ! hinfo.assigned) {

        _ready_handlers.addLast (hinfo);

	// Wake up a scheduler or guardian thread.

	if (_occupied_nthreads < _nthreads) {

          // DebugOutput.println ("EventScheduler.regEvent: " +
	  //   "waking up scheduler thread");

	  this.notify(); // wake up an unoccupied scheduler thread
	}
	else if (_ready_handlers.size() == 1) {

          // DebugOutput.println ("EventScheduler.regEvent: " +
	  //   "waking up guardian thread");

	  _guardian_thread.wakeUp(); // note: enters nested synchronized block
	}
	else {

	  // guardian thread already awake
	  // DebugOutput.println ("EventScheduler.regEvent: " +
	  //   "NOT waking up guardian thread");

	}

        // DebugOutput.println ("EventScheduler.regEvent: readied: "
	//   + hinfo.handler);
      }
      else {
        // DebugOutput.println ("EventScheduler.regEvent: found readied: "
	//   + hinfo.handler);
      }
    }
  }

  /**
   * Registers an event to be scheduled. The event will be scheduled by
   * invoking the specified one-shot event handler. The event handler is
   * removed after the event is scheduled.
   *
   * @param handler    the one-shot event handler
   * @param cookie     the client's cookie for this event handler
   */
  public void regEvent (EventHandler handler, g.opaque cookie)
  {
    synchronized (this) {
      HandlerInfo hinfo = new HandlerInfo (handler, cookie);
      // DebugOutput.println("EventScheduler.regEvent one-shot: " + handler);
      hinfo.event_count = 1;
      _ready_handlers.addLast (hinfo);

      // Wake up a scheduler or guardian thread.

      if (_occupied_nthreads < _nthreads) {

        // DebugOutput.println ("EventScheduler.regEvent one-shot: " +
	//   "waking up scheduler thread");

        this.notify(); // wake up an unoccupied scheduler thread
      }
      else if (_ready_handlers.size() == 1) {

        // DebugOutput.println ("EventScheduler.regEvent one-shot: " +
	//   "waking up guardian thread");

        _guardian_thread.wakeUp(); // note: enters nested synchronized block
      }
      else {

        // guardian thread already awake
	// DebugOutput.println ("EventScheduler.regEvent one-shot: "
	//   "NOT waking up guardian thread");
      }
    }
  }

  public String toString()
  {
    return _my_name;
  }

  /** The event handler interface, implemented by a client's event handler. */
  public static interface EventHandler
  {
    /**
     * Notifies the client of one or more events. If this is a one-shot event
     * handler, the event handler is removed afterwards. 
     *
     * @param cookie   the cookie installed by the client with the handler
     * @param n_events the number of event notifications
     */
    void schedEvents (g.opaque cookie, int n_events);

    /**
     * Notifies the client that its event handler has been removed. No more
     * callbacks will be made. This method is not invoked for one-shot event
     * event handlers.
     *
     * @param cookie   the cookie installed by the client with the handler
     * @see removeHandler
     */
    void schedStopped (g.opaque cookie);
  }

  /**
   * Method executed by a scheduler thread. A scheduler thread terminates when
   * it sees that _nthreads exceeds _target_nthreads.
   */
  private void run_scheduler_thread ()
  {
    // for debugging
    String tname = Thread.currentThread().getName();

    HandlerInfo hinfo = null;      // a ready event handler 
    int event_count;               // hinfo's event count
    boolean invoke_sched_stopped;  // hinfo's 'invoke_sched_stopped' flag

    while (true) {

      synchronized (this) {

	if (hinfo != null) { // end work of previous loop

	  // DebugOutput.println (tname + " end work of previous loop");
          hinfo.assigned = false;
	  _occupied_nthreads--;
	  if (hinfo.event_count > 0 || hinfo.invoke_sched_stopped) {
	    // someone modified hinfo while we were outside synchronized block

	    // DebugOutput.println (tname + " was modified: " + hinfo.handler);
            _ready_handlers.addLast (hinfo);
	    // don't notify(): this thread will remain active instead
	  }
	}

	// DebugOutput.println (tname + " checking whether to wait");
	int nready = _ready_handlers.size();
        while (nready == 0 && _nthreads <= _target_nthreads) {
	  // DebugOutput.println (tname + " waiting");
          try { this.wait (); }
          catch (InterruptedException exc) {
            DebugOutput.println (" TEST sched thread ignoring interruption");
          }
	  // DebugOutput.println (tname + " done waiting");
	  nready = _ready_handlers.size();
        }

	/*
          DebugOutput.println("");
          DebugOutput.println(tname + " nready     : " + nready);
          DebugOutput.println(tname + " nthreads   : " + _nthreads);
          DebugOutput.println(tname + " target     : " + _target_nthreads);
          DebugOutput.println(tname + " occupied   : " + _occupied_nthreads);
          DebugOutput.println("");
	*/

        if (nready == 0) {
	  // terminate thread
	  // DebugOutput.dassert (_nthreads > _target_nthreads);
          // DebugOutput.println (tname + " terminating");
  	  _nthreads--;
	  return;
	}

        hinfo = (HandlerInfo) _ready_handlers.removeFirst();

	// DebugOutput.println (tname + " handling: " + hinfo.handler);
	// DebugOutput.dassert (hinfo != null && ! hinfo.assigned);
        hinfo.assigned = true;

	_occupied_nthreads++;
	if (_occupied_nthreads == _nthreads && _ready_handlers.size() > 0) {
          // DebugOutput.println (tname + " waking up guardian thread");
	  _guardian_thread.wakeUp(); // note: enters nested synchronized block
	}
	else {
          // DebugOutput.println (tname + " NOT waking up guardian thread");
	}


	// transfer hinfo contents to local variables and clear it
        event_count = hinfo.event_count;
        hinfo.event_count = 0;
        invoke_sched_stopped = hinfo.invoke_sched_stopped;
        hinfo.invoke_sched_stopped = false;

	DebugOutput.dassert (event_count != 0 || invoke_sched_stopped);
      }

      // make a callback for the events, and one for the removal
      // the object lock must not be held during a callback
      if (event_count > 0) {
        try {
          hinfo.handler.schedEvents (hinfo.cookie, event_count);
        }
        catch (Exception exc) {

          DebugOutput.println("sched thread ignoring exception from callback:");
          exc.printStackTrace ();
        }
      }
      if (invoke_sched_stopped) {
        try {
          hinfo.handler.schedStopped (hinfo.cookie);
        }
        catch (Exception exc) {

          DebugOutput.println("sched thread ignoring exception from callback:");
          exc.printStackTrace ();
        }
      }
      // loop
    }
  }

  /**
   * The thread that monitors the number of scheduler threads. It creates new
   * scheduler threads if there are ready handlers and all scheduler threads
   * are occupied. The guardian thread runs at a lower priority than scheduler
   * threads, to ensure as much as possible that scheduler threads are only
   * created if all existing scheduler threads are blocked in their callbacks.
   * It notifies scheduler threads that must terminate when there are too many
   * scheduler threads.
   * <p>
   * The guardian thread uses a separate condition variable from the scheduler
   * threads. This is to avoid it being woken up unnecessarily when regEvent()
   * etc. wish to wake up a scheduler thread: the JVM does not specify that
   * thread priority be taken into account when waking up several threads that
   * are waiting on the same condition variable.
   */
  private class GuardianThread extends Thread
  {
    /** A minimum number of scheduler threads specified by the client. */
    private final int _cfg_min_nthreads;

    /**
     * The maximum level of concurrency observed during the current period. See
     * computeConcurrency().
     */
    private int _max_concurrency = 0;

    /**
     * The maximum level of concurrency observed during the previous period.
     * See computeConcurrency().
     */
    private int _prev_max_concurrency = 0;

    /**
     * The length of each period (in seconds) over which the guardian thread
     * computes a maximum level of concurrency.
     */
    private static final int GUARDIAN_PERIOD = 3600;

    /** Time at which the current period ends. */
    private long _current_period_ends = 0;

    /** Set by wakeUp() to wake up the guardian thread. */
    private boolean _wakeup = false;

    /** Counter used to name scheduler threads, for debugging. */
    private int _next_threadnum = 0;

    /**
     * Constructor.
     * @param name		the name of this EventScheduler, for debugging
     * @param cfg_min_nthreads	minimum number of scheduler threads specified
     *				by the client
     */
    public GuardianThread (int cfg_min_nthreads)
    {
      super (_my_name + " event-guardian");
      _cfg_min_nthreads = cfg_min_nthreads;
    }

    /**
     * Called by regEvent() etc. and by scheduler threads to wake up the
     * guardian thread. The call is made if we enter the following situation:
     * (a) at least one handler is ready, and (b) all scheduler threads are
     * occupied.
     */
    public synchronized void wakeUp()
    {
      _wakeup = true;
      notify();
    }

    public void run()
    {
      // for debugging
      String tname = Thread.currentThread().getName();

      _target_nthreads = computeTargetSchedThreads();

      // Summary of the main loop of this thread:
      // if (more thread(s) needed)
      //   create at most one scheduler thread
      // else 
      //   check/update periods and notify superfluous scheduler threads
      //   sleep until current period elapses or we get woken up
      //
      // A property of the guardian thread (not explicitly coded) is that it
      // will not go to sleep if there are ready events but no unoccupied
      // threads.

      while (true) {
  
	boolean started_thread = false;

	synchronized (EventScheduler.this) {

	  /*
          DebugOutput.println (tname + " entered EventScheduler synch block");
          DebugOutput.println(tname + " nready     : "+ _ready_handlers.size());
          DebugOutput.println(tname + " nthreads   : " + _nthreads);
          DebugOutput.println(tname + " target     : " + _target_nthreads);
          DebugOutput.println(tname + " occupied   : " + _occupied_nthreads);
          DebugOutput.println(tname + " max concurr: " + _max_concurrency);
          DebugOutput.println(tname + " prev max   : " + _prev_max_concurrency);
          DebugOutput.println("");
	  */

	  int concurrency = computeConcurrency();
	  if (concurrency > _max_concurrency) {
	    _max_concurrency = concurrency;
	    _target_nthreads = computeTargetSchedThreads();
	  }

	  if (_target_nthreads > _nthreads) {
	  
            DebugOutput.println (DebugOutput.DBG_DEBUGPLUS, tname +
	      " adding sched thread (to " + _nthreads + ")");

	    // Create a scheduler thread. One thread per while loop is created
	    // (irrespective of the number of ready handlers) since a single
	    // thread may be all that is needed. If more threads are needed,
	    // they will be created in subsequent while loops.

            Runnable sched_run = new Runnable () {
              public void run () {
                run_scheduler_thread();
              }
            };

	    // note: a new thread inherits priority and daemon status from its
	    // creator thread. Daemon status is OK, priority is not.
            Thread sched_thread = new Thread (sched_run, _my_name +
	        "event-sched " + _next_threadnum++);
            sched_thread.setPriority (Thread.NORM_PRIORITY);
            sched_thread.start();

	    _nthreads++;
	    started_thread = true;
	  }
	  else {
	    // Update periods and notify any superfluous threads. We only do
	    // this when there are no scheduler threads to be created, in order
	    // to stay out of the critical path as much as possible.

	    long now = System.currentTimeMillis();
	    if (now >= _current_period_ends) {
              DebugOutput.println(DebugOutput.DBG_DEBUGPLUS, tname +
	           " **** starting new period ****");

              DebugOutput.println (DebugOutput.DBG_DEBUGPLUS, tname + " before:");
              DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
	        tname + " nready     : "+ _ready_handlers.size());
              DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
	        tname + " nthreads   : " + _nthreads);
              DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
	        tname + " target     : " + _target_nthreads);
              DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
	        tname + " occupied   : " + _occupied_nthreads);
              DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
	        tname + " max concurr: " + _max_concurrency);
              DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
	        tname + " prev max   : " + _prev_max_concurrency);
              DebugOutput.println("");

	      // update periods

	      // current period becomes previous period
	      _prev_max_concurrency = _max_concurrency;

	      // start new period
              _max_concurrency = concurrency;
	      _current_period_ends = now + GUARDIAN_PERIOD * 1000;

	      // note: _target_nthreads <= previous _target_nthreads
	      _target_nthreads = computeTargetSchedThreads();

              DebugOutput.println (DebugOutput.DBG_DEBUGPLUS, tname + " after:");
              DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
	        tname + " nready     : "+ _ready_handlers.size());
              DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
	        tname + " nthreads   : " + _nthreads);
              DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
	        tname + " target     : " + _target_nthreads);
              DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
	        tname + " occupied   : " + _occupied_nthreads);
              DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
	        tname + " max concurr: " + _max_concurrency);
              DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
	        tname + " prev max   : " + _prev_max_concurrency);
              DebugOutput.println("");
	    }

	    // notify a number of threads that should terminate
	    int reduction = _nthreads - _target_nthreads;

	    DebugOutput.dassert (reduction >= 0);

            DebugOutput.println(DebugOutput.DBG_DEBUGPLUS, tname + " terminating "
	      + reduction + " sched thread(s) (of " + _nthreads + ")");

	    // notify a number of threads that should terminate
	    for (int i = 0; i < reduction; i++) {
	      EventScheduler.this.notify();
	    }
	  }
	  /*
          DebugOutput.println (tname + " leaving EventScheduler synch block");
          DebugOutput.println(tname + " nready     : "+ _ready_handlers.size());
          DebugOutput.println(tname + " nthreads   : " + _nthreads);
          DebugOutput.println(tname + " target     : " + _target_nthreads);
          DebugOutput.println(tname + " occupied   : " + _occupied_nthreads);
          DebugOutput.println(tname + " max concurr: " + _max_concurrency);
          DebugOutput.println(tname + " prev max   : " + _prev_max_concurrency);
          DebugOutput.println("");
	  */

	} // end synchronized

	if (! started_thread && /* optimisation */ ! _wakeup) {

	  synchronized (this) {

	    // wait for the end of the current period or until woken up.
            while (true) {
	      if (_wakeup) {
	        break;
	      }
              long sleep_time =
	        _current_period_ends - System.currentTimeMillis();
              if (sleep_time <= 0) {
	        break;
	      }
              // DebugOutput.println (tname + " sleeping: " + sleep_time);
              try {
                this.wait (sleep_time);
	      }
              catch (InterruptedException exc) {
                exc.printStackTrace();
              }
              // DebugOutput.println (tname + " done sleeping");
	    }
	  } // end synchronized
        }
	_wakeup = false; // OK outside synchronized block
      } // end while
    }

    /**
     * Returns the level of concurrency at this moment. This is the minimum
     * number of scheduler threads that the guardian thread believes are needed
     * to make progress in servicing handlers. Called by the guardian thread.
     */
    private int computeConcurrency()
    {
      int concurrency;

      if (_ready_handlers.size() == 0) {
        concurrency = _occupied_nthreads;
      }
      else {

	// Since this method is called by the guardian thread (i.e. guardian
	// thread is running), and the guardian has lower priority than
	// scheduler threads, we may assume (perhaps incorrectly) that all
	// occupied scheduler threads are blocked in their callbacks.
	// Therefore, an additional scheduler thread is needed to make progress
	// in servicing handlers.

        concurrency = _occupied_nthreads + 1;
      }

      // DebugOutput.println("computed concurrency: " + concurrency);
      return concurrency;
    }

    /**
     * Computes the new value of _target_nthreads.
     */
    private int computeTargetSchedThreads()
    {
      // max (_cfg_min_nthreads, _prev_max_concurrency, _max_concurrency)
      int max = Math.max (_cfg_min_nthreads, _prev_max_concurrency);
      max = Math.max (max, _max_concurrency);
      // DebugOutput.println("computed _target_nthreads: " + max);
      return max;
    }
  } // class GuardianThread

  /** The record of an installed event handler. */
  private static class HandlerInfo extends g.opaque
  {
    /** The event handler. */
    public final EventHandler handler;
  
    /** The event handler's user-defined cookie. */
    public final g.opaque cookie;

    /** The number of registered events for this event handler. */
    public int event_count = 0;
  
    /** Whether this event handler's schedStopped() is to be invoked. */
    public boolean invoke_sched_stopped = false;

    /**
     * Set when the client invokes removeHandler(), after which the client
     * cannot perform any further operations for this handler.
     */
    public boolean invalid = false;

    /** Whether a sched thread is currently invoking this event handler. */
    public boolean assigned = false;
  
    public HandlerInfo (EventHandler handler, g.opaque cookie)
    {
      this.handler = handler;
      this.cookie = cookie;
    }
  }

  /***************************************************************************/
  /*   	                Remainder is used for testing                        */
  /***************************************************************************/

  // used by main()
  static class MyHandler implements EventHandler 
  {
    public g.opaque hinfo;

    private boolean _main_called_remove = false;
    private boolean _sched_called_stopped = false;

    private int _events_pending = 0;
    private int _events_seen = 0;

    public final String name;

    public MyHandler (String name)
    {
      this.name = name;
    }

    public synchronized void mainRemoving()
    {
      DebugOutput.dassert (! _main_called_remove);
      _main_called_remove = true;
    }

    public synchronized void mainRegingEvent()
    {
      DebugOutput.dassert (! _main_called_remove);
      _events_pending++;
    }

    private int dummy;
    public synchronized void schedEvents (g.opaque cookie, int n_events)
    {
      DebugOutput.println (this + ": schedEvents: " + n_events);
      _events_seen += n_events;
      DebugOutput.dassert (_events_seen <= _events_pending);
      for (int i = 0; i < n_events * 100000; i++) {
	dummy++;
      }
      /*
      try {
        wait (10000000);
      }
      catch (InterruptedException exc) {
        exc.printStackTrace();
      }
      */
    }

    public void schedStopped (g.opaque cookie) {
      DebugOutput.println (this + ": schedStopped");
      DebugOutput.dassert (_main_called_remove && ! _sched_called_stopped);
      for ( int i = 0; i <100000; i++) {
	dummy++;
      }
      _sched_called_stopped = true;
      /*
      try {
        Thread.sleep (10000000);
      }
      catch (InterruptedException exc) {
        exc.printStackTrace();
      }
      */
    }

    public synchronized void checkFinished()
    {
      DebugOutput.println ("Checking event scheduler: " + this);
      DebugOutput.dassert (_events_pending == _events_seen);
      DebugOutput.dassert (_main_called_remove == _sched_called_stopped);
    }

    public String toString()
    {
      return name;
    }
  }


  public static void main (String[] argv)
  {
    try {
      mainBody (argv);
    }
    catch (Exception exc) {
      DebugOutput.println ("main thread caught:");
      exc.printStackTrace();
    }
    finally {
      DebugOutput.println ("Suspending main thread");
      synchronized (EventScheduler.class) {
	try {
          EventScheduler.class.wait();
	}
	catch (InterruptedException exc) {
	  exc.printStackTrace();
	}
      }
    }
  }

  private static void mainBody (String[] argv) throws Exception
  {
    if (argv.length != 3) {
      DebugOutput.println ("usage error");
      System.exit (1);
    }
    int min_nthreads = Integer.parseInt (argv[0]);
    int nhandlers = Integer.parseInt (argv[1]);
    int max_nops = Integer.parseInt (argv[2]);
    ArrayList unstopped_handlers = new ArrayList (nhandlers);
    ArrayList allhandlers = new ArrayList (nhandlers);
    Random r = new Random();

    DebugOutput.println ("min number of sched threads: " + min_nthreads);
    DebugOutput.println ("number of handlers         : " + nhandlers);
    DebugOutput.println ("max number of operations   : " + max_nops);

    EventScheduler sched = new EventScheduler ("main", min_nthreads);
    DebugOutput.println (" main thread sleeping 1 sec");
    Thread.sleep (1000);

    for (int i = 0; i < nhandlers; i++) {

      MyHandler myhand = new MyHandler ("handler " + i);
      g.opaque hinfo = sched.installHandler (myhand, new g.opaque());
      myhand.hinfo = hinfo;
      unstopped_handlers.add (myhand);
      allhandlers.add (myhand);
    }

    for (int i = 0; i < max_nops; i++) {
      if (unstopped_handlers.size() == 0) {
        DebugOutput.println ("ran out of handlers");
	break;
      }
      int handler_no = r.nextInt (unstopped_handlers.size());
      MyHandler myhand = (MyHandler) unstopped_handlers.get (handler_no);

      int op = r.nextInt (1000);
      if (op <= 1) {
	// remove handler
	myhand.mainRemoving();
	sched.removeHandler (myhand.hinfo);
	unstopped_handlers.remove (handler_no);
      }
      else {
	// register event with handler
	myhand.mainRegingEvent();
	sched.regEvent (myhand.hinfo);
      }
    }

    while (true) {
      synchronized (sched) {
        if (sched._occupied_nthreads == 0 && sched._ready_handlers.size() == 0){
          break;
        }
      }
      DebugOutput.println ("main thread waiting for things to die down");
      try {
        Thread.sleep (4000);
      }
      catch (InterruptedException exc) {
        exc.printStackTrace();
      }
    }

    Iterator it = allhandlers.iterator();
    while (it.hasNext()) {
      MyHandler myhand = (MyHandler) it.next();
      myhand.checkFinished();
    }
    DebugOutput.println ("main thread: done.");
  }
}
