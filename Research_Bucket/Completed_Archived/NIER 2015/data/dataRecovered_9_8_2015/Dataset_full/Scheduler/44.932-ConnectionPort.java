/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.comm.muxconn;

import vu.globe.rts.comm.idl.p2p.*;   // p2p.idl
import vu.globe.rts.comm.idl.mux.*;   // mux.idl
import vu.globe.rts.comm.idl.mux;     // mux.idl
import vu.globe.util.comm.idl.rawData.*;
import vu.globe.rts.comm.muxcp.CallbackPort;
import vu.globe.util.comm.ProtAddress;
import vu.globe.rts.comm.muxconn.protocol.*;
import vu.globe.util.thread.EventScheduler;


import vu.globe.util.debug.DebugOutput;
import vu.globe.util.exc.AssertionFailedException;

import java.net.ProtocolException;


/**
 * A connection port, corresponding to a light-weight connection. Implements
 * the finite state machine for the light-weight connection. Stores local and
 * remote connection port numbers, and keeps a reference to the base connection
 * on which the light-weight connection is multiplexed. Also maintains the
 * client callback state; this is delegated to the CallbackPort superclass.
 *
 * The connection port is registered with its base connection as a 'dependent
 * base connection'. If the base connection closes, the connection port is
 * told (eventBaseCloses()).
 *
 * Thread-safe.
 *
 * @author Patrick Verkaik
 */

class ConnectionPort extends CallbackPort
{
  /** State of the finite state machine. See finite state machine spec. */
  private int _state;

  private static final int STATE_OPEN_PORT_ACKED_WAIT = 1;
  private static final int STATE_OPEN = 2;
  private static final int STATE_ERROR = 3;
  private static final int STATE_CLOSE_ACKED_WAIT = 4;
  private static final int STATE_USER_CLOSE_WAIT = 5;
  private static final int STATE_CLOSED = 6;

  /** The local connection port number. */
  private final int _local_number;

  /** The address corresponding to the local connection port number. */
  private final String _local_address;

  /** The remote connection port number, or mux.NoPort if still unknown. */
  private int _remote_number;

  /**
   * The address corresponding to the remote connection port number, or null
   * if _remote_number is still unknown.
   */
  private String _remote_address = null;

  /** The base connection carrying this port's communication. */
  private final BaseConnection _bconn;

  /** The table that stores this connection port. */
  private final ConnectionPortTable _cport_table;

  /** Bad news for caller of waitOpenPortAcked(). */
  private muxErrors _saved_open_port_acked_wait_exc = null;

  /**
   * Constructor. The connection will begin in 'open' state (if a remote_number
   * was passed or 'open port acked wait' state (if a mux.NoPort remote_number
   * was passed).
   *
   * @param local_number	the local connection port number
   * @param remote_number	the remote connection port number if the
   *				connection was initiated by the remote mux,
   *				or mux.NoPort if this connection was initiated
   *				locally 
   * @param bconn		the base connection carrying this
   *                            port's communication
   * @param cport_table		the table that stores this connection port
   * @param sched               see CallbackPort(cb_class,EventScheduler)
   */
  public ConnectionPort (int local_number, int remote_number,
    BaseConnection bconn, ConnectionPortTable cport_table,
    EventScheduler sched)
  {
    super (msgCB.class, sched, true); // all callbacks are made by sched
    _local_number = local_number;
    _remote_number = remote_number;
    _bconn = bconn;
    _cport_table = cport_table;
    _state = (remote_number != mux.NoPort) ?
      STATE_OPEN : STATE_OPEN_PORT_ACKED_WAIT;

    try {
      // for _local_address, use the base connection local endpoint and append
      // the local connection port number
      ProtAddress paddr = new ProtAddress (bconn.getLocal());
      paddr.add (MuxAddress.PROT_ID, String.valueOf (local_number));
      _local_address = paddr.toString();

      // for _remote_address use the base connection remote endpoint and append
      // the remote connection port number
      if (remote_number != mux.NoPort) {
        paddr = new ProtAddress (bconn.getRemote());
        paddr.add (MuxAddress.PROT_ID, String.valueOf (remote_number));
        _remote_address = paddr.toString();
      }
    }
    catch (ProtAddress.IllegalAddressException exc) {
      DebugOutput.printException (exc);
      throw new AssertionFailedException();
    }
  }

  protected void finalize() throws Throwable
  {
    DebugOutput.println ("");
    super.finalize();
  }


  /**
   * Returns the local connection port number allocated to this connection port.
   */
  public int getLocalNumber()
  {
    return _local_number;
  }

  /**
   * Returns the remote connection port number of this connection port.
   *
   * @return the remote connection port number or mux.NoPort if still unknown
   */
  public int getRemoteNumber()
  {
    return _remote_number;
  }

  /**
   * Implements the IDL getLocalOnPort() invocation.
   */
  public String getLocalOnPort()
  {
    return _local_address;
  }

  /**
   * Implements the IDL getRemoteOnPort() invocation.
   */
  public String getRemoteOnPort() throws muxErrors
  {
    // note: thread-safe without 'synchronized'
    if (_remote_address == null) {
      // the client cannot have obtained this port in a legal way
      throw new muxErrors_illegalPort();
    }
    return _remote_address;

  }

  /** Returns the base connection carrying this port's communication. */
  public BaseConnection getBaseConnection()
  {
    return _bconn;
  }

  /**
   * Implements the IDL receiveOnPort() invocation. Removes and returns the
   * next message waiting in this port.
   *
   * @see CallbackPort.acceptElement()
   *
   * @exception muxErrors_invOp	if the port was closed
   * @exception muxErrors_comm	an exception that was found queued
   */
  public muxMsg_receiveOnPort_Out receiveOnPort()
    throws muxErrors_invOp, muxErrors_comm
  {
    muxMsg_receiveOnPort_Out out = new muxMsg_receiveOnPort_Out();
    try {
      out.retval = (rawDef) super.acceptElement();
    }
    catch (muxErrors_invOp exc) {
      throw exc;
    }
    catch (Exception exc) {
      DebugOutput.println (this + "found a queued error", exc);
      throw new muxErrors_comm();
    }
    out.remotePort = mux.NoPort;
    out.remote = null;
    return out;
  }

  /**
   * Called by a local thread initiating this light-weight connection. Blocks
   * the thread while the connection is in state 'open port acked wait', i.e.
   * while waiting for the connection to be acked or refused, or broken.
   */
  public synchronized void waitOpenPortAcked() throws muxErrors
  {
    while (_state == STATE_OPEN_PORT_ACKED_WAIT) {
      // DebugOutput.println (this + "waitOpenPortAcked: waiting");
      try { wait(); }
      catch (InterruptedException exc) {
        DebugOutput.println ("ignored interruption");
      }
    }
    if (_saved_open_port_acked_wait_exc != null) {
      throw _saved_open_port_acked_wait_exc;
    }
  }

  /**
   * Called by the callback thread which carries the acknowledgment of a
   * locally initiated light-weight connection. Unblocks the thread that 
   * initiated the connection, and is waiting in waitOpenPortAcked().
   *
   * @param msg			the message
   *
   * @exception ProtocolException
       an 'open port acked' message was not expected or was incorrect
   */
  public synchronized void eventOpenPortAcked (ProtOpenPortAcked msg)
    throws ProtocolException
  {
    // DebugOutput.println (this + "eventOpenPortAcked");
    int msg_remote_number = msg.getSenderConnectionPort();
    if (msg_remote_number == mux.NoPort) {
      throw new ProtocolException (toString() + msg +
        ": invalid sender connection port (mux.NoPort)");
    }
    if (_state != STATE_OPEN_PORT_ACKED_WAIT) {
      throw new ProtocolException (toString() + msg + ": unexpected");
    }

    _state = STATE_OPEN;
    _remote_number = msg_remote_number;

    try {
      // see ConnectionPort constructor
      ProtAddress paddr = new ProtAddress (_bconn.getRemote());
      paddr.add (MuxAddress.PROT_ID, String.valueOf (msg_remote_number));
      _remote_address = paddr.toString();
    }
    catch (ProtAddress.IllegalAddressException exc) {
      DebugOutput.printException (exc);
      throw new AssertionFailedException();
    }

    notify(); 	// note: there can be only one thread waiting
  }

  /**
   * Called by the callback thread which carries a negative acknowledgment of a
   * locally initiated light-weight connection. Unblocks the thread that 
   * initiated the connection, and is waiting in waitOpenPortAcked().
   *
   * @param msg
   *   the message
   *
   * @exception ProtocolException
       an 'open port refused' message was not expected or was incorrect
   */
  public synchronized void eventOpenPortRefused (ProtOpenPortRefused msg)
    throws ProtocolException
  {
    String reason = msg.getReason();

    if (_state != STATE_OPEN_PORT_ACKED_WAIT) {
      throw new ProtocolException (toString() + msg + ": unexpected");
    }
    DebugOutput.println (DebugOutput.DBG_DEBUG, toString() +
      "peer refused a light-weight connection, reason: " +
       (reason != null ? reason : "none"));

    _saved_open_port_acked_wait_exc = new muxErrors_peerRefused();
    _state = STATE_CLOSED;

    // MuxConnectionOriented.connectOnPort() will remove this port from the
    // _cport_table

    notify(); 	// note: there can be only one thread waiting
  }

  /**
   * A 'data' message has arrived. Queue it.
   *
   * @param msg			the message
   *
   * @exception CallbackPort.RefusedException
   *   there was no room for the message. The finite state machine has been
   *   moved to 'error' state, and an error has been queued for the client. The
   *   caller should send a 'close' message to the remote mux.
   *
   * @exception ProtocolException
   *   a 'data' message was not expected
   *
   * @see CallbackPort.addElement()
   */
  public synchronized void eventDataMessage (ProtData msg)
    throws CallbackPort.RefusedException, ProtocolException
  {
    int msg_remote_number = msg.getSenderConnectionPort();
    rawDef data = msg.getData();

    if (_remote_number != mux.NoPort && msg_remote_number != _remote_number) {
      throw new ProtocolException (toString() + msg +
        ": incorrect sender connection port");
    }

    switch (_state) {

    case STATE_OPEN_PORT_ACKED_WAIT:
    case STATE_USER_CLOSE_WAIT:
    case STATE_CLOSED:

      throw new ProtocolException (toString() + msg + ": unexpected");

    case STATE_OPEN:

      try {
        // we are holding a lock, so cannot use super.addElement()
        super.addElementScheduled (data);
      }
      catch (CallbackPort.RefusedException exc) {

        _state = STATE_ERROR;

	// Queue the error for the client. Note: errors are allowed to exceed
	// the maximum number of queued elements.
        try {
	  // we are holding a lock, so cannot use super.addElement()
          super.addElementScheduled (exc);
        }
        catch (CallbackPort.RefusedException exc2) {
	  // I guess the port has been closed
	  DebugOutput.printException (exc2);
	}
        throw exc;
      }

      break;

    case STATE_CLOSE_ACKED_WAIT:
    case STATE_ERROR:

      // ignore
      break;

    default:
      DebugOutput.dassert (false);
    }
  }

  /**
   * The client is trying to send a message. Check that it's OK.
   *
   * @exception muxErrors
       if that is not possible in the current state
   */
  public void eventSendData() throws muxErrors
  {
    // note thread-safe without 'synchronized'

    switch (_state) {
    case STATE_OPEN_PORT_ACKED_WAIT:

      // the client cannot have obtained this port in a legal way
      throw new muxErrors_illegalPort();

    case STATE_OPEN:

      // OK
      break;

    case STATE_ERROR:
    case STATE_CLOSE_ACKED_WAIT:
    case STATE_USER_CLOSE_WAIT:
    case STATE_CLOSED:

      throw new muxErrors_comm();

    default:
      DebugOutput.dassert (false);
    }
  }


  /**
   * Pauses or resumes activity.
   * @see CallbackPort.pause(boolean)
   */
  public void pause (boolean on) throws muxErrors_invOp
  { 
    // MAINTAINER: please be very careful when changing this method. Race
    // conditions with eventUserCloses() (case STATE_USER_CLOSE_WAIT) are
    // easily introduced.

    super.pause (on);

    // see ConnectionPortTable notes.
    if (! on && _state == STATE_CLOSED) {

      // Thread safety note: a concurrent eventUserCloses() call may cause
      // two attempts to remove the port from _cport_table: below and in
      // eventUserCloses(). That's OK.

      _cport_table.remove (_local_number);
    }
  }


  /**
   * Use one of eventUserCloses(), eventRemoteCloses() or eventBaseCloses()
   * instead.
   */
  public void close()
  {
    throw new AssertionFailedException();
  }

  /**
   * Implements the client-initiated closeConnOnPort() invocation.
   *
   * @return		true iff a 'close port' message must be sent by the
   *                    caller to the remote mux (for this light-weight
   *                    connection)
   */
  public boolean eventUserCloses() throws muxErrors_illegalPort
  {
    boolean send_close;

    synchronized (this) {

      switch (_state) {

      case STATE_OPEN_PORT_ACKED_WAIT:

        // the client cannot have obtained this port in a legal way
        throw new muxErrors_illegalPort();

      case STATE_OPEN:

        send_close = true;
        _state = STATE_CLOSE_ACKED_WAIT;
        break;

      case STATE_ERROR:

        // we sent a close message when we got into this state
        send_close = false;
        _state = STATE_CLOSE_ACKED_WAIT;
        break;

      case STATE_CLOSE_ACKED_WAIT:

        // client has already called closeConnOnPort()
        send_close = false;
        break;

      case STATE_USER_CLOSE_WAIT:

        // MAINTAINER: please be very careful when changing this code. Race
        // conditions with pause() are easily introduced.

        // we sent a close message when we got into this state
        send_close = false;
        _state = STATE_CLOSED;

        // Thread safety note: from this point on, the port's 'paused' status
        // (super.isPaused()) can go from true to false, but not vice-versa.

        if (! super.isPaused()) {
          // normal case
          _cport_table.remove (_local_number);
        }
        // else: see ConnectionPortTable notes.

        break;

      case STATE_CLOSED:

        // client has already called closeConnOnPort()
        send_close = false;
        break;

      default:
        send_close = false;
        DebugOutput.dassert (false);
      }

    }
    /*
      Close callback port (leading to a receiveStopped callback on the client).
      Alternatively, we *could* omit the closePort call here, since
      eventRemoteCloses() or eventBaseCloses() also make the call, once the
      remote mux responds or the base connection is broken, resp. However,
      calling it here is better for clients that wait for the receiveStopped
      callback to be made: less latency and reduced dependence on the remote
      mux.
    */

    // discard returned messages and exceptions
    super.closePort();

    return send_close;
  }

  /**
   * A 'close port' message has been received from the remote mux.
   * 
   * @param msg         the message
   *
   * @return		true iff a 'close port' message must be sent by the
   *                    caller to the remote mux (for this port)
   *
   * @exception ProtocolException
   *   a 'close port' message was not expected or was incorrect
   */
  public boolean eventRemoteCloses (ProtClosePort msg) throws ProtocolException
  {
    int msg_remote_number = msg.getSenderConnectionPort();
    String reason = msg.getReason();

    boolean send_close;
    synchronized (this) {

      if (_remote_number != mux.NoPort && msg_remote_number != _remote_number) {
        throw new ProtocolException (toString() + msg +
          ": incorrect sender connection port");
      }

      switch (_state) {

      case STATE_OPEN_PORT_ACKED_WAIT:
      case STATE_USER_CLOSE_WAIT:
      case STATE_CLOSED:

        throw new ProtocolException (toString() + msg + ": unexpected");

      case STATE_OPEN:

        if (reason != null) {
	  // queue an error for the client
          try {
	    // we are holding a lock, so cannot use super.addElement()
            super.addElementScheduled (new Exception (reason));
          }
          catch (CallbackPort.RefusedException exc) {
	    // I guess the port has been closed
	    DebugOutput.printException (exc);
	  }
	} // else: an orderly close

        send_close = true;
        _state = STATE_USER_CLOSE_WAIT;
        break;

      case STATE_ERROR:

        // In this state, the client already has some error queued. Don't queue
	// an additional error.

        send_close = false; // already sent when we moved to STATE_ERROR
        _state = STATE_USER_CLOSE_WAIT;
        break;

      case STATE_CLOSE_ACKED_WAIT:

        send_close = false; // already sent when we moved to this state
        _state = STATE_CLOSED;

        if (! super.isPaused()) { // see ConnectionPortTable notes.
          _cport_table.remove (_local_number);
        }
        break;

      default:
        send_close = false;
        DebugOutput.dassert (false);
      }
    }

    // discard returned messages and exceptions
    super.closePort();

    return send_close;
  }

  /**
   * The base connection that carries this port's communication has been closed.
   */
  public void eventBaseCloses()
  {
    synchronized (this) {
      switch (_state) {
      case STATE_OPEN_PORT_ACKED_WAIT:

        DebugOutput.println (DebugOutput.DBG_DEBUG, toString() +
          "base connection was closed during light-weight connect");

        _saved_open_port_acked_wait_exc = new muxErrors_comm();
        this.notify(); 	// note: there can be only one thread waiting

        _state = STATE_CLOSED;
        // MuxConnectionOriented will remove this port from the _cport_table
        break;

      case STATE_OPEN:

	// queue an error for the client
        try {
	  // we are holding a lock, so cannot use super.addElement()
          super.addElementScheduled (new Exception(toString() +
            "base connection broken"));
        }
        catch (CallbackPort.RefusedException exc) {
	  // I guess the port has been closed
	  DebugOutput.printException (exc);
	}
        _state = STATE_USER_CLOSE_WAIT;
        break;

      case STATE_ERROR:

        // In this state, the client already has some error queued. Don't queue
	// an additional error.

        _state = STATE_USER_CLOSE_WAIT;
        break;

      case STATE_CLOSE_ACKED_WAIT:

        _state = STATE_CLOSED;

        if (! super.isPaused()) { // see ConnectionPortTable notes.
          _cport_table.remove (_local_number);
        }

        break;

      case STATE_USER_CLOSE_WAIT:

	// ignore
        break;

      case STATE_CLOSED:

	DebugOutput.println (DebugOutput.DBG_DEBUG, toString() +
	  "got 'base closes' while in state STATE_CLOSED");
        break;

      default:
        DebugOutput.dassert (false);
      }
    }

    // discard returned messages and exceptions
    super.closePort();
  }

  public String toString()
  {
    return "[ConnectionPort local=" + _local_address +
      " remote=" + (_remote_address != null ? _remote_address : "unknown")
      + "] ";
  }
}
