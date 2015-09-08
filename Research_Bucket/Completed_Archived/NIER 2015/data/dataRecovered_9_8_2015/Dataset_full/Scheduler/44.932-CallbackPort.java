/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.comm.muxcp;
 
import vu.globe.rts.comm.idl.mux.*;
import vu.globe.rts.comm.idl.p2p.*;
import vu.globe.util.comm.idl.rawData.*;
import vu.globe.util.thread.EventScheduler;
import vu.globe.util.exc.AssertionFailedException;
import vu.globe.util.debug.DebugOutput;
import vu.globe.idlsys.g;

import java.util.*;
 
/**
 * This object implements the algorithm and administration for making
 * communication callbacks to a client, and queuing messages or connections for
 * the client. It supports one client callback registration (callback interface
 * plus callback cookie). Therefore for each client callback registration a
 * separate CallbackPort must be used.
 * <p>
 * Currently two communication callback interfaces are supported: p2p.listenCB
 * and p2p.msgCB. Currentaly known users of this object are the contact point
 * multiplexer and the connection multiplexer.
 * <p>
 * The communication callbacks follow the same pattern. See p2p.idl, and the
 * ASCII diagrams included in the source code of this object.
 * <p>
 * Thread safe.
 */

/*
  General pattern of communication callback. In this example, the communication
  object is a listener, and therefore the methods shown are from the listener
  and listenCB interfaces.

                      ---------------------
                     |       Client        |
                      ---------------------
                       ^   |             ^
        2 connArrived  |   | 3 accept    | 4 accept
          callback     |   |             |   returns conn
                       |   v             |
                      ---------------------
                     |      Comm Object    |
                      ---------------------
                        1 conn arrives


  The communication object's part of the above pattern is implemented by a
  CallbackPort as follows:

                 ------------------------------
                |            Client            |
                 ------------------------------
                   |   ^                      ^
         5 accept  |   | 8 accept             |
                   |   |   returns            | 4 connArrived
                   |   |   conn               |   callback
                   v   |                      |
                 --------  2 addElement      --------------
                |        |    (conn)        |              |
       1 conn   |        | ---------------> |              | 3 store conn
        arrives |  Comm  |                  | CallbackPort |
                | Object | 6 acceptElement  |              |
                |        | ---------------> |              |
                |        |                  |              |
                |        | 7 acceptElement  |              |
                |        |   returns conn   |              |
                |        | <--------------- |              |
                 --------                    ---------------


   For error notification, it is also possible to store an exception variable
   instead of a connection:

                 ------------------------------
                |            Client            |
                 ------------------------------
                   |   ^                      ^
         5 accept  |   | 8 accept             |
                   |   |   throws             | 4 connArrived
                   |   |   exc                |   callback
                   v   |                      |
                 --------  2 addElement      --------------
                |        |    (exc)         |              |
       1 error  |        | ---------------> |              | 3 store exc  
        occurs  |  Comm  |                  | CallbackPort |
                | Object | 6 acceptElement  |              |
                |        | ---------------> |              |
                |        |                  |              |
                |        | 7 acceptElement  |              |
                |        |   throws exc     |              |
                |        | <--------------- |              |
                 --------                    ---------------

 */
 
/*
  IMPLEMENTATION NOTES

  As a generic component, the documentation and source code apply to all
  supported communication callbacks. References to parts of the callback
  mechanism (method names, interface names, etc.) have the following notation:
  'N1/N2', where N1 is the name in the context of listenCB callbacks, and N2 is
  the name in the context of msgCB callbacks. For example,
  'connArrived/msgReceived' refers to the name of a callback method.

  Similarly, a connection or a message is referred to as a 'connection/
  message'. The term 'element' is used to indicate a connection/message or
  an exception variable (see diagram above).

  Note: 'callback' and 'notification' are used interchangeably
 
  Queue
  =====
  Until the client accepts an incoming element, it is placed in a queue of
  waiting elements. The maximum length of the queue is BACK_LOG. However an
  exception variable is allowed to exceed this maximum length.

  Notifications
  =============
  There are two kinds of callback notifications: connArrived/msgReceived and
  listenStopped/receiveStopped. Each call to addElement leads to a
  connArrived/msgReceived notification. A call to closePort leads to a single
  listenStopped/receiveStopped notification. In accordance with the IDL spec,
  no callbacks can follow listenStopped/receiveStopped: a listenStopped/
  receiveStopped notification informs the client that callbacks have ceased,
  and that the client's callback interface will be released by the
  communication object. Also, once closePort has been called, connArrived/
  msgReceived notifications may be dropped.

  The addElement method is invoked by a callback thread, and may therefore
  make callbacks to the client. The closePort and addElementScheduled methods,
  on the other hand, may be called by a downcalling thread. Therefore, they
  cannot themselves make callbacks to the client, and have to pass on the work
  to an event scheduler thread, or a callback thread that happens to be active.
  
  Callbacks may be disabled for two reasons: the client has not yet installed
  a callback interface, or the client has paused (see below) the port. Only
  when both of these conditions are false, are callbacks enabled. While
  callbacks are disabled, the port saves notifications. Once callbacks become
  enabled, any saved callback notifications are performed. The port may become
  enabled by invocations to regCallback and pause(false). Like closePort, these
  methods may be called by a downcalling thread. Therefore they too have to
  pass on the work to an event scheduler's thread or an active callback thread.

  The callback and scheduler threads are the only threads to make callbacks.
  To avoid multithreaded callbacks to the client, both the callback thread and
  the scheduler thread will refrain from making callbacks if the other thread
  happens to be making callbacks. Instead of making the callback itself, the
  thread will update the number of outstanding notifications, and the thread
  currently making callbacks will perform these notifications.
  
  Notifications to be performed by a callback or scheduler thread are encoded
  in two variables:

      // the number of outstanding connArrived/msgReceived notifications
      int _outstanding_elt_arrived 

      // whether a listenStopped/receiveStopped notification is outstanding
      boolean _outstanding_stopped


  In accordance with the semantics of the communication interfaces,
  notifications for arrived connections/messages should terminate soon after
  closePort has been called. The callback or scheduler thread will therefore
  omit outstanding connArrived/msgReceived notifications if a
  listenStopped/receiveStopped notification is outstanding.

  Pausing
  -------

  Pausing is implemented by setting the _should_pause flag, and waiting for
  callbacks to be suspended. Unpausing is implemented by clearing the
  _should_pause flag, and resuming callbacks.

  Flags
  =====

  _client_cb (not a proper flag)
  _calling_back
  _should_pause

  _closing
  --------

  Once closePort has been called, all operations other than another closePort()
  or pause(false) invoked by the client are illegal (according to the IDL
  interfaces). This is checked by setting a _closing flag.  
*/

public class CallbackPort implements EventScheduler.EventHandler
{
  /**
   * The type of communication callback to be implemented. Only listenCB and
   * msgCB are currently supported.
   */
  private final Class _cb_class;

  /**
   * The client's callback interface: a listenCB or a msgCB. Null iff a client
   * callback interface has not been installed.
   */
  private Object _client_cb;

  /** The client's callback cookie. */
  private g.opaque _client_cookie;

  /**
   * The callback or scheduler thread is making callbacks, or a scheduler event
   * has been registered.
   */
  private boolean _calling_back = false;

  /**
   * True iff the client wishes callbacks to be temporarily disabled.
   */
  private boolean _should_pause = false;

  /** Whether this port is being or has been closed. */
  private boolean _closing = false;

  /**
   * The number of outstanding connArrived/msgReceived() notifications to be
   * made to the client.
   */
  private int _outstanding_elt_arrived = 0;
 
  /**
   * Whether a listenStopped/receiveStopped() notification is to be made to
   * the client.
   */
  private boolean _outstanding_stopped = false;

  /** The scheduler object to use for scheduling events. */
  private final EventScheduler _sched;

  /**
   * If set, all callbacks are made by the scheduler thread. In other words.
   * the callback thread is never used to make callbacks if this is set.
   */
  private final boolean _sched_only;

  /**
   * A thread-unsafe queue of elements (connections/messages or exceptions)
   * waiting to accepted by the client. 
   */
  private LinkedList _elements = new LinkedList();

  /**
   * Maximum number of waiting elements in _elements. May be exceeded by
   * exception elements.
   */
  private static final int BACKLOG = 10;

  /**
   * Constructor. Equivalent to CallbackPort (cb_class, null, false).
   *
   * @param cb_class	the type of communication callback to be implemented
   *                    by this object. Only listenCB and msgCB are currenly
   *                    supported
   */
  public CallbackPort (Class cb_class)
  {
    this (cb_class, null, false);
  }

  /**
   * Constructor.
   *
   * @param cb_class	the type of communication callback to be implemented
   *                    by this object. Only listenCB and msgCB are currenly
   *                    supported
   *
   * @param sched	the scheduler object to use for scheduling events. If
   *			null, defaults to EventScheduler.sys_sched.
   *
   * @param sched_only	If set, all callbacks are made by the scheduler
   *                    thread. In other words, the callback thread is never
   *                    used to make callbacks if this is set.
   */
  public CallbackPort (Class cb_class, EventScheduler sched, boolean sched_only)
  {
    DebugOutput.dassert (cb_class == listenCB.class ||
    			cb_class == msgCB.class);
    _cb_class = cb_class;
    _sched = sched != null ? sched : EventScheduler.sys_sched;
    _sched_only = sched_only;
  }

  /**
    * Pauses or resumes activity. See the IDL spec in mux.idl for
    * pauseMsgCBOnPort() and pauseListenCBOnPort().
    */
  public void pause(boolean on) throws muxErrors_invOp
  {
    synchronized (this) {

      if (on && _closing)
        throw new muxErrors_invOp();

      if (_should_pause == on)
        return;

      // DebugOutput.println("[CallbackPort]: " + "pause = " + on);

      _should_pause = on;
      if (on) {
        // on return, callbacks must be disabled
        while (_calling_back) {
          try {
            wait(); // see handleEvents()
          }
          catch (InterruptedException exc) {
            DebugOutput.println (DebugOutput.DBG_NORMAL,
                    "[CallbackPort.pause] : ignoring interruption", exc);
          }
        }
      }
      else {
        // check whether callbacks should be made
  
        if (_client_cb != null && ! _calling_back &&
            (_outstanding_elt_arrived != 0 || _outstanding_stopped))
        {
          _calling_back = true;
          _sched.regEvent (this, null);
        }
      }
    }
  }

  /**
   * Returns whether this port has been (or is being) paused.
   */
  public boolean isPaused()
  {
    // note: thread-safe without 'synchronized'
    return _should_pause;
  }

  /**
   * Registers a client's listening/receive callback.

   * @param	a listenCB or a msgCB
   *
   * @exception muxErrors_invOp		if the port was closed, or a callback
   *					had already been installed
   */
  public void regCallback (Object cb, g.opaque cookie)
    throws muxErrors_invOp, muxErrors_invArg
  {
    synchronized (this) {

     if (_client_cb != null || _closing)
        throw new muxErrors_invOp(); // contract violation
     if (cb == null)
        throw new muxErrors_invArg();

      _client_cb = cb;
      _client_cookie = cookie;

      // check whether callbacks should be made

      if (! _should_pause &&
          (_outstanding_elt_arrived != 0 || _outstanding_stopped))
      {
        _calling_back = true;
        _sched.regEvent (this, null);
      }
    }
  }

  /**
   * Tries to add an element (connection/message or exception) to this port,
   * and notifies the client.
   * If for some reason the port did not allow the element to be
   * added (e.g. the port is closed), an exception is thrown (and the client
   * is not notified).
   * <p>
   * addElement() will make the notification itself (unless this object was
   * constructed with 'sched_only' set to true). Therefore, it may only be
   * called by a callback thread. It should not be called with locks
   * held. (In these cases use addElementScheduled instead.)
   *
   * @param elt		a connection/message of type connection/rawDef, or an
   *			Exception
   *
   * @exception RefusedException
   *	if the port did not allow the element to be added. The
   *    RefusedException's message contains the reason for the refusal
   */
  public void addElement (Object elt) throws RefusedException
  {
    addElement (elt, false);
  }

  /**
   * As addElement(Object), but may be called by a non-callback thread or with
   * a lock held. addElementScheduled() will not make the notification itself.
   * The notification will be made by the event scheduler thread. 
   */
  public void addElementScheduled (Object elt) throws RefusedException
  {
    addElement (elt, true);
  }

  /**
   * Implements addElement(Object) or addElementScheduled(elt) depending on
   * whether 'schedule_this' is false or true, respectively.
   */
  private void addElement (Object elt, boolean schedule_this)
    throws RefusedException
  {
    /* DebugOutput.println ("[CallbackPort] addElement schedule_this=" +
       schedule_this); */
    boolean is_exc = (elt instanceof Exception);

    DebugOutput.dassert ( is_exc
		 	||
                        (_cb_class == listenCB.class &&
                         elt instanceof connection)
		 	||
                        (_cb_class == msgCB.class &&
                         elt instanceof rawDef)
			 );

    boolean do_notify  = false;
    synchronized (this) {

      if (_closing) {
	throw new RefusedException ("the port has been closed");
      }

      if (! is_exc && _elements.size() >= BACKLOG) {
	throw new RefusedException ("the port has exceeded its backlog");
      }

      // enqueue elt
      _elements.addLast (elt);

      _outstanding_elt_arrived++;
      if (_client_cb != null && ! _should_pause && ! _calling_back) {
	if (schedule_this || _sched_only) {
          // register event
          // DebugOutput.println ("[CallbackPort] scheduling event");
          _sched.regEvent (this, null);
          _calling_back = true;
	}
	else {
          // DebugOutput.println ("[CallbackPort] not scheduling event");
          do_notify = true; // callbacks are made after releasing the lock
          _calling_back = true;
	}
      }
      // else: the notification will be performed by another thread
      else {
        // DebugOutput.println ("[CallbackPort] performed by other thread");
      }
    }
    if (do_notify) {
      // DebugOutput.println ("[CallbackPort] addElement calls handleEvents");
      handleEvents(); // also returns to SCHED_NORMAL
    }
    // DebugOutput.println ("[CallbackPort] addElement ends");
  }

  /**
   * Accepts/receives the next element (connection/message or exception)
   * waiting in this port. The element is removed. If the next element is an
   * exception, it is thrown.
   *
   * @return	an element or null if no element was found waiting. The type of
   * 		the returned element is connection/rawDef.
   *
   * @exception muxErrors_invOp		if the port was closed
   * @exception Exception		if the next element was an exception
   */
  public Object acceptElement() throws muxErrors_invOp, Exception
  {
    synchronized (this) {
      if (_closing)
        throw new muxErrors_invOp();
      if (_elements.size() == 0)
        return null;
      Object elt = _elements.removeFirst();
      if (elt instanceof Exception) {
        throw (Exception) elt;
      }
      return elt;
    }
  }

  /**
   * Closes this port. All pending elements are removed and
   * returned to the caller. A listenStopped/receiveStopped notification of the
   * client will be performed.
   *
   * @return	pending elements each of type connection/rawDef or Exception
   */
  public Object[] closePort()
  {
    if (_closing)
      return new Object[0];

    synchronized (this) {
      if (_closing) {
        return new Object[0];
      }
      _closing = true;

      Object[] pending = _elements.toArray();
      _elements.clear();

      _outstanding_stopped = true;

      if (_client_cb != null && ! _should_pause && ! _calling_back) {

        // register event
        _sched.regEvent (this, null);
	_calling_back = true;
      }
      return pending;
    }
  }

  /** Returns true if closePort() has been invoked, false otherwise. */
  public boolean closing()
  {
    return _closing; // note: thread-safe without 'synchronized'
  }

  // EventHandler interface
  public void schedEvents (g.opaque cookie, int n_events)
  {
    handleEvents(); // also clears _calling_back
  }

  public void schedStopped (g.opaque cookie)
  {
    // this method is not invoked for one-shot event handlers
    throw new AssertionFailedException();
  }

  /**
   * Performs outstanding connArrived/msgReceived and listenStopped/
   * receiveStopped notifications in a thread-safe way. When done, clears
   * _calling_back.
   */
  private void handleEvents()
  {
    // Take care (a) not to hold a lock while making a callback, yet (b) read
    // the outstanding notifications safely.

    // DebugOutput.println ("[CallbackPort] handleEvents");
    while (true) { // until all outstanding notifications have been performed
		   // or paused
      int copy_outstanding_elt_arrived;
      boolean copy_outstanding_stopped;
      Object client_cb;
      g.opaque client_cookie;
 
      synchronized (this) {

        DebugOutput.dassert (_calling_back);

        if (_should_pause) {

          _calling_back = false;
          notify(); // see pause (true)
          break;
        }
 
        // check whether outstanding notifications have all been performed

        if (_outstanding_elt_arrived == 0 && ! _outstanding_stopped) {
          _calling_back = false;
          // DebugOutput.println ("[CallbackPort] handleEvents ends");
          return;          // done
        }
 
        // transfer outstanding notifications
	// skip connArrived/msgReceived notifications if a listenStopped/
	// receiveStopped is outstanding

	copy_outstanding_stopped = _outstanding_stopped;
	_outstanding_stopped = false;

	copy_outstanding_elt_arrived = copy_outstanding_stopped ?
					0 : _outstanding_elt_arrived;
        _outstanding_elt_arrived = 0;

	client_cb = _client_cb;
        client_cookie = _client_cookie;

	if (copy_outstanding_stopped) {
	  // release references to the client, so this object won't get in the
	  // way of Java GC
	  _client_cb = null;
	  _client_cookie = null;
	}
      }
 
      // perform the callbacks, protect ourselves from exceptions raised by
      // the callbacks

      try {
        while (copy_outstanding_elt_arrived-- > 0) {
	  if (_cb_class == listenCB.class) {
	    // DebugOutput.println ("[CallbackPort] connArrived callback");
            ((listenCB) client_cb).connArrived (client_cookie);
	  }
	  else {
	    // DebugOutput.println ("[CallbackPort] msgReceived callback");
            ((msgCB) client_cb).msgReceived (client_cookie);
	  }
          // DebugOutput.println ("[CallbackPort] callback done");
        }	
        if (copy_outstanding_stopped) {
	  if (_cb_class == listenCB.class) {
	    // DebugOutput.println ("[CallbackPort] listenStopped callback");
	    ((listenCB) client_cb).listenStopped (client_cookie);
	  }
	  else {
	    // DebugOutput.println ("[CallbackPort] receiveStopped callback");
	    ((msgCB) client_cb).receiveStopped (client_cookie);
	  }
          // DebugOutput.println ("[CallbackPort] callback done");
        }
      }
      catch (Exception exc) {
        DebugOutput.printException (DebugOutput.DBG_DEBUG, exc);
      }
 
      // note that at this point, outstanding notifications may have been
      // added to; so loop
      // DebugOutput.println ("[CallbackPort] handleEvents loops");
    }
  }

  /** Thrown by CallbackPort.addElement(). */
  public static class RefusedException extends Exception
  {
    /**
      Constructs a RefusedException with no detail message.
    */
    public RefusedException ()
    {
      super ();
    }

    /**
      Constructs a RefusedException with the specified detail
      message. 

      @param s       the detail message.
    */
    public RefusedException (String s)
    {
      super (s);
    }
  }
}
