/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.comm.muxcp;

import vu.globe.rts.comm.idl.p2p.*;     // p2p.idl
import vu.globe.util.comm.idl.rawData.*; // rawData.idl
import vu.globe.idlsys.g;       // g.idl
import vu.globe.rts.std.idl.stdInf.*;   // stdInf.idl
import vu.globe.util.thread.EventScheduler;
import vu.globe.util.debug.DebugOutput;

/**
 * The connection object created by a multiplexing object (a multiplexing
 * listener or multiplexing connector). This connection object encapsulates a
 * connection object created by the listener/connector being multiplexed.
 * <p>
 * The wrapper passes on all invocations and callbacks to/from the encapsulated
 * connection. The difference between the wrapper and the encapsulated
 * connection object is that the latter can have only one callback registration.
 * The wrapper is able to have different callback registrations in succession:
 * first the multiplexer registers a callback, later the registration
 * is replaced by the callback registration of the multiplexer's client. This
 * behaviour is needed in order to allow a multiplexer listener and a
 * multiplexing connector to communicate through the connection prior to
 * passing on the connection to the multiplexer's clients.
 * <p>
 * The wrapper will produce notifications for incoming messages through its
 * *current* callback registration. As soon as the multiplexer has received
 * all the data it needs, it must inform the wrapper of this by calling
 * muxFinished.
 * Subsequent notifications from the encapsulated connections are intended for
 * the multiplexer's client, and will be saved by the wrapper. When client
 * registration takes place, additional notifications are performed for these
 * packets.
 *
 * @author Patrick Verkaik
 */

/*
  Terminology
  ===========

  Client: the client of a multiplexing object.
  User:   the client of the connection wrapper: first the multiplexing object,
  	  then the client of the multiplexing object.

  Changing the Installed Callback
  ===============================

  The connection wrapper starts out with the multiplexing object as
  its user. After the multiplexing object is finished as a user (muxFinished()),
  notifications for the client can start to come in. However, until the client
  has installed a callback, notifications to the client cannot
  be made. The wrapper saves notifications until the client installs a callback
  (in regMsgCB). When that happens, the outstanding notifications are performed
  by an event scheduler (since regMsgCB is invoked by a non-callback thread).
  After the client has installed a callback, notifications can be made directly.

  To avoid multithreaded callbacks to the client, the wrapper will refrain
  from making notifications while the event scheduler is active (even when a
  client has installed a callback). Instead of notifying directly,
  the number of outstanding notifications is updated, and the active event
  scheduler makes the notifications.

  Notifications to be performed by the scheduler are encoded in two variables:
 
      // the number of outstanding msgReceived notifications
      int _outstanding_msg_received
 
      // whether a receiveStopped notification is outstanding
      boolean _outstanding_receive_stopped


  States and transitions
  ======================
 
  MUX_CB_INSTALLED : initial state, the multiplexer has installed a callback
  NO_CB_INSTALLED  : no callback is installed
 
  SCHED_PENDING    : a scheduler event has been registered
                     (and a client callback has been installed)

  SCHED_PAUSED     : a scheduler event needs to be registered but we are
                     currently paused
                     (and a client callback has been installed)
 
  SCHED_ACTIVE     : a scheduler event is being handled
                     (and a client callback has been installed)
 
  NORMAL           : no event has been registered or is being handled
                     (and a client callback has been installed)
 
  MUX_CB_INSTALLED -> NO_CB_INSTALLED : multiplexer calls muxFinished
  NO_CB_INSTALLED  -> NORMAL  	      : client installs callback, and no
					outstanding notifications exist
  NO_CB_INSTALLED  -> SCHED_PENDING   : client installs callback, and
					outstanding notifications exist
  SCHED_PENDING    -> SCHED_ACTIVE    : scheduler starts the event handler
  SCHED_ACTIVE     -> NORMAL          : event handler terminates


  Pausing
  =======

  In principle pausing a ConnectionWrapper is implemented by pausing the
  encapsulated connection. In most cases this is sufficient. However there is
  a special case: the scheduler thread may be active (or about to become
  active) at the time that the ConnectionWrapper is pasused. If this is the
  case, the pause invocation simply waits for the scheduler thread to complete
  its work.

  Normally when the client installs a callback and there are outstanding
  notifications, the wrapper will activate the scheduler thread. However if the
  ConnectionWrapper has been paused at this time, the wrapper will not activate
  the scheduler thread yet. Instead, the scheduler thread will be activated at
  the time that the ConnectionWrapper is unpaused.


  MUX_CB_INSTALLED
  ----------------

  Actions:

    incoming msgReceived    -> notify multiplexer
    incoming receiveStopped -> notify multiplexer
    muxFinished()           -> go to NO_CB_INSTALLED
    (un)pause               -> update _should_pause
                               (un)pause encapsulated connection

  NO_CB_INSTALLED
  ---------------

  Actions:

    incoming msgReceived     -> _outstanding_msg_received++
    incoming receiveStopped  -> _outstanding_receive_stopped = true
    (un)pause                -> update _should_pause
                                (un)pause encapsulated connection

    client installs callback ->
     if outstanding notifications exist
       if (_should_pause)
         go to SCHED_PAUSED
       else
         register event
         go to SCHED_PENDING
     else
       go to NORMAL

  SCHED_PAUSED
  ------------

  Actions:

    incoming msgReceived     -> _outstanding_msg_received++
    incoming receiveStopped  -> _outstanding_receive_stopped = true

    unpause ->
      (note: pause() shouldn't occur)
      update _should_pause
      unpause encapsulated connection
      register event
      go to SCHED_PENDING


  SCHED_PENDING
  -------------

  Actions:

    incoming msgReceived     -> _outstanding_msg_received++
    incoming receiveStopped  -> _outstanding_receive_stopped = true
    event scheduled          -> go to SCHED_ACTIVE
    (un)pause                -> update _should_pause
                                (un)pause encapsulated connection
                                if (pausing) wait for NORMAL


  SCHED_ACTIVE
  ------------

  Actions:
 
    by the event handler:
 
      Perform the outstanding notifications.
 
      _outstanding_msg_received = 0;
      _outstanding_receive_stopped = false;
 
      when done go to NORMAL
 
    by other (concurrent!) threads:
      incoming msgReceived     -> _outstanding_msg_received++
      incoming receiveStopped  -> _outstanding_receive_stopped = true
      (un)pause                -> update _should_pause
                                  (un)pause encapsulated connection
                                  if (pausing) wait for NORMAL



  NORMAL
  ------

  Actions:
    incoming msgReceived    -> notify client
    incoming receiveStopped -> notify client
    (un)pause               -> update _should_pause
                               (un)pause encapsulated connection
*/

class ConnectionWrapper extends connectionCBSkel
			implements EventScheduler.EventHandler
{
  /** The encapsulated connection. */
  private final connection _encaps_connection;   // counted

  /** The encapsulated connection. */
  private final msg _encaps_msg;                 // uncounted

  /** The encapsulated connection. */
  private final comm _encaps_comm;               // uncounted

  /** The protocol id of this communication object. Set on first request. */
  private String _prot_id = null;

  /** The protocol stack of this communication object. Set on first request. */
  private String _prot_stack = null;

  // state constants

  /** Initial state constant. The multiplexer has installed a callback. */
  private static final int MUX_CB_INSTALLED = 0;

  /** State constant. No callback is installed. */
  private static final int NO_CB_INSTALLED = 1;

  /**
   * State constant. A scheduler event has been registered (and a client
   * callback has been installed).
   */
  private static final int SCHED_PENDING = 2;

  /**
   * State constant. A scheduler event needs to be registered but we are 
   * currently paused (and a client callback has been installed).
   */
  private static final int SCHED_PAUSED = 3;

  /**
   * State constant. A scheduler event is being handled (and a client callback
   * has been installed).
   */
  private static final int SCHED_ACTIVE = 4;

  /**
   * Final state constant. No event has been registered or is being handled
   * (and a client callback has been installed).
   */
  private static final int NORMAL = 5;

  /** The state of this object. One of the above state constants. */
  private int _state = MUX_CB_INSTALLED;

  /**
   * The number of outstanding msgReceived() notifications to be made to the
   * client.
   */
  private int _outstanding_msg_received = 0;

  /** Whether a receiveStopped() notification is to be made to the client. */
  private boolean _outstanding_receive_stopped = false;

  /** The current callback registration. */
  private msgCB _user_cb;

  /** The user cookie of the current callback registration. */
  private g.opaque _user_cookie;

  /**
   * True iff the client wishes callbacks to be temporarily disabled.
   */
  private boolean _should_pause = false;

  /** The event scheduler's id for our event handler registration. */
  private g.opaque _sched_id;

  /** The communication interfaces implemented by this object. */
  private static interfaces COMM_INFS;

  static {
    COMM_INFS = new interfaces (3);

    COMM_INFS.v[0] = comm.infid;
    COMM_INFS.v[1] = connection.infid;
    COMM_INFS.v[2] = msg.infid;
  }


  /**
   * Constructs a wrapper around a connection. Must be followed immediately
   * by a call to installMuxCallback.
   *
   * @param encaps    		the connection to be encapsulated. A counted
   *				Globe reference which is stored by this object
   * @exception Exception	an unexpected Globe exception
   */
  public ConnectionWrapper (connection encaps) throws Exception
  {
    _encaps_connection = encaps;
    _encaps_msg = (msg) encaps.soi.getUncountedInf (msg.infid);
    _encaps_comm = (comm) encaps.soi.getUncountedInf (comm.infid);
  }

  /**
   * Registers the multiplexing object's callback in the wrapper.
   *
   * @param mux_cb    		the multiplexing object callback
   * @param mux_cookie    	the multiplexing object callback cookie
   *
   * @exception Exception       if the encapsulated connection did not allow
   *				registration of a callback
   */
  public void installMuxCallback (msgCB mux_cb, g.opaque mux_cookie)
  					throws Exception
  {
    _user_cb = (msgCB) mux_cb.dupUncountedInf();
    _user_cookie = mux_cookie;

    // register the wrapper's callback with the encapsulated connection
    msgCB cb = (msgCB) this.getCBInf (msgCB.infid);
    _encaps_msg.regMsgCB (cb, null);
  }

  protected void cleanup() throws vu.globe.rts.std.idl.stdInf.SOIerrors
  {
    _encaps_connection.relInf();
    super.cleanup();
  }

  // comm interface methods

  public String getProtocolID()
  {
    // note: thread-safe without synchronisation
    if (_prot_id == null) {
      _prot_id = _encaps_comm.getProtocolID();
    }
    return _prot_id;
  }

  public String getProtocolStack()
  {
    // note: thread-safe without synchronisation
    if (_prot_stack == null) {
      _prot_stack = _encaps_comm.getProtocolStack();
    }
    return _prot_stack;
  }

  public String getContact()
  {
    return null;
  }

  public interfaces getCommInfs()
  {
    return COMM_INFS;
  }

  // comm interface methods

  public String getLocal()
  {
    return _encaps_connection.getLocal();
  }

  public String getRemote()
  {
    return _encaps_connection.getRemote();
  }

  public void closeConn()
  {
    _encaps_connection.closeConn();
  }

  // msg interface methods

  public void regMsgCB (msgCB client_cb, g.opaque client_cookie)
     				throws commErrors
  {

    synchronized (this) {

      // see the notes

      if (_state != NO_CB_INSTALLED)
        throw new commErrors_invOp(); // contract violation

      // install client callback
      _user_cb = (msgCB) client_cb.dupUncountedInf();
      _user_cookie = client_cookie;

      if (_outstanding_msg_received != 0 || _outstanding_receive_stopped) {
        if (_should_pause) {
          _state = SCHED_PAUSED;
        }
        else {

          _state = SCHED_PENDING;

          // register scheduler event XXX replace by one-shot event handler
          _sched_id = EventScheduler.sys_sched.installHandler (this, null);
          EventScheduler.sys_sched.regEvent (_sched_id);
        }
      }
      else
        _state = NORMAL;
    }
  }

  public void pauseMsgCB(short /* g.bool */ on) throws Exception
  {
    boolean my_on = (on == g.bool.True) ? true : false;

    // pause as early as possible; unpause as late as possible
    if (my_on) {
      _encaps_msg.pauseMsgCB(on);
    }

    synchronized (this) {
      _should_pause = my_on;

      if (_state == SCHED_PAUSED) {
        if (! my_on) {

          _state = SCHED_PENDING;

          // register scheduler event XXX replace by one-shot event handler
          _sched_id = EventScheduler.sys_sched.installHandler (this, null);
          EventScheduler.sys_sched.regEvent (_sched_id);
        }
      }
      else if (_state == SCHED_PENDING || _state == SCHED_ACTIVE) {
        if (my_on) {

          // wait for the scheduler to finish callbacks; see schedEvents()
          while (_state != NORMAL) {
            wait();
          }
        }
      }
    }

    // pause as soon as possible; unpause as late as possible
    if (! my_on) {
      _encaps_msg.pauseMsgCB(on);
    }
  }

  public void send (String remote, rawDef pkt) throws Exception
  {
    _encaps_msg.send (remote, pkt);
  }

  public msg_receive_Out receive() throws Exception
  {
    return _encaps_msg.receive();
  }

  // msgCB interface methods

  public void receiveStopped (g.opaque user)
  {
    // optimisation which avoids locking overhead
    if (_state == NORMAL) {
      _user_cb.receiveStopped (_user_cookie);
      return;
    }

    boolean perform_callback = false;
    msgCB copy_cb = null; g.opaque copy_cookie = null;

    synchronized (this) {

      // see the notes
      if (_state == MUX_CB_INSTALLED || _state == NORMAL) {

        // callback to the user is performed *after* releasing the lock
        perform_callback = true;
        copy_cb = _user_cb;
        copy_cookie = _user_cookie;
      }
      else {
	// callback will be performed by the scheduler
        _outstanding_receive_stopped = true;
      }
    }

    if (perform_callback)
      copy_cb.receiveStopped (copy_cookie);
  }

  public void msgReceived (g.opaque user)
  {
    // optimisation which avoids locking overhead
    if (_state == NORMAL) {
      _user_cb.msgReceived (_user_cookie);
      return;
    }

    boolean perform_callback = false;
    msgCB copy_cb = null; g.opaque copy_cookie = null;

    synchronized (this) {

      // see the notes
      if (_state == MUX_CB_INSTALLED || _state == NORMAL) {

	// callback to the user is performed *after* releasing the lock
        perform_callback = true;
        copy_cb = _user_cb;
        copy_cookie = _user_cookie;
      }
      else {
	// callback will be performed by the scheduler
        _outstanding_msg_received++;
      }
    }

    if (perform_callback)
      copy_cb.msgReceived (copy_cookie);
  }

  // EventHandler interface

  public void schedEvents (g.opaque cookie, int n_events)
  {
    synchronized (this) {
      DebugOutput.dassert (_state == SCHED_PENDING);
      _state = SCHED_ACTIVE;
    }

    // Perform outstanding msgReceived and receiveStopped. Take care (a) not to
    // hold a lock while making a callback, and (b) read the outstanding
    // notifications safely.

    while (true) { // until all outstanding notifications have been performed

      int copy_outstanding_msg_received;
      boolean copy_outstanding_receive_stopped;

      synchronized (this) {

	// check whether done, if so update _state and finish
        if (_outstanding_msg_received == 0 && ! _outstanding_receive_stopped) {

	  _state = NORMAL;
          notify(); // see pauseMsgCB(true)

          // remove the handler
          EventScheduler.sys_sched.removeHandler (_sched_id);
	  break;          // done
	}

	// transfer outstanding notifications
	copy_outstanding_msg_received = _outstanding_msg_received;
	_outstanding_msg_received = 0;
	copy_outstanding_receive_stopped = _outstanding_receive_stopped;
	_outstanding_receive_stopped = false;
      }

      // perform the callbacks, protect ourselves from exceptions raised by
      // the callbacks

      try {
        while (copy_outstanding_msg_received-- > 0)
          _user_cb.msgReceived (_user_cookie);

        if (copy_outstanding_receive_stopped)
          _user_cb.receiveStopped (_user_cookie);
      }
      catch (Exception exc) {
        DebugOutput.printException (DebugOutput.DBG_DEBUG, exc);
      }
      
      // note that at this point, outstanding notifications may have been
      // added to
    }
  }

  public void schedStopped (g.opaque cookie)
  {
    DebugOutput.dassert (_state == NORMAL);
  }

  // public non-Globe methods

  /**
   * Informs the wrapper that the multiplexing object has obtained all of
   * its data, and will therefore not need to be notified anymore. The wrapper
   * will immediately terminate notifications to the multiplexing object.
   * Subsequent notifications will be saved until the client registers.
   * <p>
   * The multiplexer must invoke this method *within* the last msgReceived
   * callback to the multiplexer. The msgReceived is *not* followed by a
   * receiveStopped callback.
   */
  public void muxFinished()
  {
    synchronized (this) {
      DebugOutput.dassert (_state == MUX_CB_INSTALLED);
      _state = NO_CB_INSTALLED;
      _user_cb = null;
      _user_cookie = null;
    }
  }

  /**
   * A reference to this wrapper's standard object interface. The reference
   * belongs to the multiplexer object, and must be released by it. The only
   * reason it is here is that it is a most convenient hack.
   */
  public SOInf sneaky_soi;
}

