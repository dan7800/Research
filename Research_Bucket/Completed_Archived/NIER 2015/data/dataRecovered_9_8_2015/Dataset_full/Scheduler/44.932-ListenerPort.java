/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.comm.muxcp;
 
import vu.globe.rts.comm.idl.mux.*;
import vu.globe.rts.comm.idl.p2p.*;
import vu.globe.util.thread.EventScheduler;
import vu.globe.util.exc.AssertionFailedException;
import vu.globe.util.debug.DebugOutput;
import vu.globe.idlsys.g;

import java.util.*;
 
/**
 * The object that represents a listener port. It is created by a
 * ListenerPortTable. Thread safe.
 */
 
/*
  A MuxListener delegates all port-specific client operations to the correct
  ListenerPort, and also passes incoming connections to the ListenerPort. A
  ListenerPort is responsible for carrying out operations invoked by the
  MuxListener client, and for notifying the client of incoming connections.
  Much of this is delegated to CallbackPort, the parent class of ListenerPort.

  _exported
  ---------
  In accordance with the IDL interfaces, addConnection will only allow a
  connection to be added if the port has been exported. An _exported flag
  checks this.
*/

public class ListenerPort extends CallbackPort
{
  /** The port number allocated to this port. */
  private final int _port_number;

  /** Whether this port has been exported. */
  private boolean _exported = false;

  /**
   * Constructor. Normally only called by ListenerPortTable. 
   *
   * @param port_number		the port number allocated to this port
   */
  public ListenerPort (int port_number)
  {
    super (listenCB.class);
    _port_number = port_number;
  }

  /**
   * Constructor. Normally only called by ListenerPortTable. 
   *
   * @param port_number	the port number allocated to this port
   * @param sched	see CallbackPort(cb_class,EventScheduler,boolean)
   * @param sched_only	see CallbackPort(cb_class,EventScheduler,boolean)
   */
  public ListenerPort (int port_number, EventScheduler sched,
  		       boolean sched_only)
  {
    super (listenCB.class, sched, sched_only);
    _port_number = port_number;
  }

  /**
   * Returns the port number allocated to this port.
   */
  public int getPortNumber()
  {
    return _port_number;
  }

  /**
   * Marks this port as 'exported', meaning that it is available for incoming
   * connection requests.
   *
   * @exception muxErrors_invOp		   if the port was closed
   * @exception muxErrors_noContactPoint   if the port was already exported
   */
  public void export() throws muxErrors
  {
    // thread-safe without 'synchronized'
    if (closing())
      throw new muxErrors_invOp();
    if (_exported)
      throw new muxErrors_noContactPoint();
    _exported = true;
  }

  /**
   * Registers a user's listening callback.
   * @see CallbackPort.regCallback(Object cb, g.opaque)
   *
   * @exception muxErrors_invOp		if the port was closed, or a callback
   *					had already been installed
   */
  public void regCallback (listenCB cb, g.opaque cookie)
    throws muxErrors_invOp, muxErrors_invArg
  {
    super.regCallback (cb, cookie);
  }

  /**
   * Tries to add a connection to this port, and notifies the client.
   * <p>
   * Must only be called by a callback thread. Should not be called with locks
   * held.
   *
   * @see CallbackPort.addElement(Object)
   */
  public void addConnection (connection conn) throws RefusedException
  {
    // thread-safe without 'synchronized'

    if (! _exported) {
      throw new RefusedException ("this port has not been exported");
    }
    super.addElement (conn);
  }

  /**
   * Removes and returns the next connection waiting in this port.
   * @see CallbackPort.acceptElement()
   */
  public connection accept() throws muxErrors_invOp
  {
    try {
      return (connection) super.acceptElement();
    }
    catch (muxErrors_invOp exc) {
      throw exc;
    }
    catch (Exception exc) {
      // exception elements are not stored by this implementation
      DebugOutput.printException (exc);
      throw new AssertionFailedException();
    }
  }
}
