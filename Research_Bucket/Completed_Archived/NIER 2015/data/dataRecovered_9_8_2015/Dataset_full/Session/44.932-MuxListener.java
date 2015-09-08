/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.comm.muxcp;

import vu.globe.rts.java.*;
import vu.globe.idlsys.g;       // g.idl
import vu.globe.rts.comm.idl.mux.*;     // mux.idl
import vu.globe.rts.comm.idl.mux;       // mux.idl
import vu.globe.rts.comm.idl.p2p.*;     // p2p.idl
import vu.globe.rts.std.idl.stdInf.*;   // stdInf.idl
import vu.globe.rts.std.idl.configure.*; // configure.idl
  
import vu.globe.util.comm.ProtStack;
import vu.globe.util.parse.AttributeString;
import vu.globe.util.exc.AssertionFailedException;
import vu.globe.util.debug.DebugOutput;
import vu.globe.rts.std.StdUtil;

import java.util.*;

/**
 * A multiplexing listener object (as in mux.idl). The multiplexing listener
 * is layered on top of a point-to-point listener (as in p2p.idl). The
 * multiplexer is responsible for creating the point-to-point listener. To do
 * this, a multiplexer must be provided with the implementation handle and
 * initialisation string of a point-to-point listener. The information is
 * provided through the initialisation string of the multiplexer ('configurable'
 * interface). See MuxCpConfig about the format of the initialisation string.
 */

/*
  The MuxListener accepts connections, and places them in the correct port.
  To find out which port a connection is intended for, some communication is
  performed with the remote party. Until the correct port is known, the
  connection waits in a set of waiting connections.
  
  A maximum is imposed on the number of connections allowed to wait: BACKLOG.
  Since (presumably) the underlying listening communication object also buffers
  incoming connections, BACKLOG is kept small.

  To communicate with the remote party, MuxListener must install a message
  callback
  in an incoming connection. The callback is removed when MuxListener has
  determined the port for which the connection is intended, so that the client
  can later install its own callback. Replacing a callback is not supported by
  the standard communication interfaces. Therefore a ConnectionWrapper is used.
  ConnectionWrappers are Globe objects which are created by a
  ConnectionWrapperFact.

  The main data structure is a table of ports. The table is responsible for
  the allocation of port numbers. Each port is represented by a ListenerPort.
  Most of the per-port work is performed by a ListenerPort (including callback
  invocations).
*/

public class MuxListener extends myMuxListenerSkel
{
  /** The point-to-point listener. */
  private listener _p2p_listener;                  // counted

  /** The point-to-point listener. */
  private contactExporter _p2p_listener_exporter;  // uncounted

  /** The point-to-point listener. */
  private comm _p2p_listener_comm;                 // uncounted


  /** The manager of the multiplexer's ports. */
  private final ListenerPortTable _ports = new ListenerPortTable();

  /**
   * A thread-safe set of connections waiting to be added to a port. Each
   * connection is of type ConnectionWrapper.
   */
  private final Set _connections = Collections.synchronizedSet (new HashSet (
  						BACKLOG + 10, 1.0F));
  /** A factory of connection wrappers. */
  private ConnectionWrapperFact _wrapper_fact;

  /** The protocol stack of this communication object. Set on first request. */
  private String _prot_stack = null;

  /** An approximate maximum number of waiting connections. */
  private static final int BACKLOG = 10;

  /**
   * The name under which the point-to-point listener object is registered in
   * the local name space.
   */
  private static final String P2P_NAME = "p2p";

  /** The communication interfaces implemented by this object. */
  private static final interfaces COMM_INFS;

  static {
    COMM_INFS = new interfaces (4);

    COMM_INFS.v[0] = comm.infid;
    COMM_INFS.v[1] = muxListener.infid;
    COMM_INFS.v[2] = contactExporter.infid;
    COMM_INFS.v[3] = muxContactExporter.infid;
  }

 
  protected void cleanup() throws vu.globe.rts.std.idl.stdInf.SOIerrors
  {
    GInterface.RelInf (_p2p_listener);
    super.cleanup();
  }

  protected void initState() throws SOIerrors
  {
    _wrapper_fact = new ConnectionWrapperFact (lns, getContext(), "wrapper");
  }

  // configurable interface
 
  public void configure (String config_data) throws configureErrors
  {
    try {
      // create the point-to-point listener
  
      AttributeString cfg = new AttributeString (config_data);
      String p2p_impl = cfg.get (MuxCpConfig.P2P_IMPLEMENTATION);
      String p2p_init = cfg.get (MuxCpConfig.P2P_INITIALISATION);
   
      SOInf co_soi = lns.bind (getContext(), "repository/" + p2p_impl);
      SCInf sci = (SCInf) co_soi.swapInf (SCInf.infid);
      SOInf p2p_soi = StdUtil.createGlobeObject (sci, getContext(), P2P_NAME);
   
      // initialise the point-to-point listener
      configurable cfg_inf = (configurable) p2p_soi.getUncountedInf (
      						configurable.infid);
      cfg_inf.configure (p2p_init);
   
      // install the point-to-point listener
      _p2p_listener = (listener) p2p_soi.swapInf (listener.infid);
      _p2p_listener_exporter = (contactExporter) p2p_soi.getUncountedInf (
  						contactExporter.infid);
      _p2p_listener_comm = (comm) p2p_soi.getUncountedInf (comm.infid);
    }
    catch (configureErrors exc) {
      throw exc;
    }
    catch (Exception exc) {
      exc.printStackTrace();
      throw new configureErrors_misc();
    }
  }

  // comm interface methods

  public String getProtocolID()
  {
    return MuxCpAddress.PROT_ID;
  }

  public String getProtocolStack()
  {
    // note: thread-safe without synchronisation
    if (_prot_stack == null) {
      try {
        ProtStack stack = new ProtStack (_p2p_listener_comm.getProtocolStack());
        stack.add (MuxCpAddress.PROT_ID);

        _prot_stack = stack.toString();
      }
      catch (ProtStack.IllegalStackException exc) {
        throw new AssertionFailedException();
      }
    }
    return _prot_stack;
  }

  public String getContact()
  {
    return _p2p_listener_comm.getContact();
  }

  public interfaces getCommInfs()
  {
    return COMM_INFS;
  }

  // muxListener interface methods

  public void regListenCBOnPort (int localPort, listenCB cb, g.opaque user)
  				throws muxErrors
  {
    ListenerPort port = _ports.lookupOrCreate (localPort);
    port.regCallback (cb, user);
  }

  public connection acceptOnPort (int localPort) throws muxErrors
  {
    ListenerPort port = _ports.lookup (localPort);
    if (port == null)
      throw new muxErrors_illegalPort();
    return port.accept();
  }

  public void pauseListenCBOnPort(int localPort, short /* g.bool */ on)
    throws muxErrors
  {
    // MAINTAINER: please be very careful when changing this method. Race
    // conditions with closeContactOnPort() are easily introduced.

    ListenerPort port = _ports.lookup (localPort);
    if (port == null)
      throw new muxErrors_illegalPort();

    port.pause(on == g.bool.True);

    // see closeContactOnPort()
    if (on == g.bool.False && port.closing()) {

      // Thread safety note: a concurrent closeContactOnPort() call may cause
      // two attempts to remove localPort from _ports: below and in
      // closeContactOnPort(). That's OK.

      _ports.remove (localPort);
    }
  }


  // contactExporter interface methods
  // Note that contactExporter is defined in p2p.idl, and its methods throw
  // commErrors rather than muxErrors.

  public void exportContact (String contact) throws commErrors
  {
    try {
      _p2p_listener_exporter.exportContact (contact);

      // The underlying listener may now start buffering incoming connections.
      // We *could* delay registering a listening callback until the first
      // client registers a callback with us. However, that will keep remote
      // mux connectors waiting indefinitely when trying to connect. Therefore,
      // register it now.

      listenCB my_cb = (listenCB) this.getCBInf (listenCB.infid);
      _p2p_listener.regListenCB (my_cb, null);
    }
    catch (commErrors exc) {
      throw exc;
    }
    catch (Exception exc) {
      exc.printStackTrace();
      throw new commErrors_misc();
    }
  }


  public void closeContact()
  {
    _p2p_listener_exporter.closeContact();

    // to avoid unnecessary race conditions with connArrived, remaining work
    // is performed by listenStopped
  }

  // muxContactExporter interface methods

  public int exportContactOnPort (int localPort) throws muxErrors
  {
    ListenerPort port;
    if (localPort == mux.NoPort)
      port = _ports.create();
    else
      port = _ports.lookupOrCreate (localPort);

    port.export();
    return port.getPortNumber();
  }

  public void closeContactOnPort (int localPort) throws muxErrors_illegalPort
  {
    // MAINTAINER: please be very careful when changing this method. Race
    // conditions with pauseListenCBOnPort() are easily introduced.

    ListenerPort port = _ports.lookup (localPort);
    if (port == null)
      throw new muxErrors_illegalPort();

    closePort (port);
    // Thread safety note: from this point on, the port's 'paused' status 
    // (port.isPaused()) can go from true to false, but not vice-versa. 

    if (! port.isPaused()) {
      // normal case
      _ports.remove (localPort);
    }
    // else: delay the _ports.remove (localPort) until the client calls
    //       pauseListenCBOnPort(localPort, false). (Otherwise the client will
    //       have no way of unpausing the port.)

  }

  private void closePort (ListenerPort port)
  {
    Object[] elts = port.closePort();
    for (int i = 0; i < elts.length; i++) {
      if (elts[i] instanceof connection) {
        connection conn = (connection) elts [i];
        try { Protocol.refuseConnection (conn); }
        catch (muxErrors exc) {
          DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
        }
        conn.closeConn();
	conn.relInf();
      }
      // else: ignore the Exception elements in elts
    }
  }

  // listenCB callback interface methods
 
  public void listenStopped (g.opaque user)
  {
    // continues closeContact()'s work

    // Refuse and close waiting connections. At this point listening has
    // stopped, so new connections are no longer being added to _connections.
    // However, connections can still be removed from _connections by
    // msgReceived.

    // move the connections out of _connections.
    ConnectionWrapper[] wrappers;
    synchronized (_connections) {
      wrappers = (ConnectionWrapper[]) _connections.toArray();
      _connections.clear();
    }

    // Refuse and close. Releasing the wrapper's sneaky soi is performed by
    // receiveStopped.

    for (int i = 0; i < wrappers.length; i++) {

      ConnectionWrapper wrapper = wrappers [i];

      try { Protocol.refuseConnection (wrapper); }
      catch (Exception exc) {
        exc.printStackTrace();
      }
      wrapper.closeConn(); // see receiveStopped()
    }

    // Safely iterate through the ports, by synchronising on the table. Note
    // that strictly speaking this is unnecessary, since concurrent additions
    // or removals of ports by the client are illegal (mux has been closed).

    synchronized (_ports) {

      Iterator it = _ports.iterator();

      while (it.hasNext()) {
        ListenerPort port = (ListenerPort) it.next();
	closePort (port);
        it.remove();
      }
    }
  }

  public void connArrived (g.opaque user)
  {
    connection encaps = null; // the encapsulated connection
    try {
      encaps = _p2p_listener.accept();

      if (_connections.size() >= BACKLOG)
	throw new muxErrors_misc();

      // create the wrapper connection, use wrapper as a cookie
      ConnectionWrapper wrapper = _wrapper_fact.create (encaps);

      g.opaque_pointer wrapper_cookie = new g.opaque_pointer();
      wrapper_cookie.pointer = wrapper;

      _connections.add (wrapper);
      msgCB cb = (msgCB) this.getCBInf (msgCB.infid);
      wrapper.installMuxCallback (cb, wrapper_cookie);
    }
    catch (Exception exc) {
      exc.printStackTrace();
      if (encaps != null) {
        try { Protocol.refuseConnection (encaps); } catch (muxErrors m) {}
        encaps.closeConn(); // see receiveStopped()
      }
    }
  }

  // msgCB interface methods
 
  public void receiveStopped (g.opaque user)
  {
    // One of the connections has been closed. Not much to be done about
    // that. Just remove it from _connections, and release the wrapper's sneaky
    // soi.

    g.opaque_pointer cookie = (g.opaque_pointer) user;
    ConnectionWrapper wrapper = (ConnectionWrapper) cookie.pointer;
    wrapper.sneaky_soi.relInf();
    _connections.remove (wrapper); // if not already removed (e.g. when closing)
  }

  public void msgReceived (g.opaque user)
  {
    // one of the connections in _connections has a message

    g.opaque_pointer cookie = (g.opaque_pointer) user;
    ConnectionWrapper wrapper = (ConnectionWrapper) cookie.pointer;

    try {
      _connections.remove (wrapper);// if not already removed (eg when closing)

      // see what port number is being requested
      int port_number = Protocol.receiveConnectionRequest (wrapper);

      wrapper.muxFinished(); // terminates connection's callbacks to the
      			     // multiplexer
  
      ListenerPort port = _ports.lookup (port_number);
      if (port == null) // possibly removed by close
	throw new muxErrors_misc();
      else {
        connection conn = (connection) wrapper.sneaky_soi.swapInf (
						connection.infid);
        // addConnection may notify the client. Take care not hold any locks.
	try {
          port.addConnection (conn);
          Protocol.grantConnection (conn);
	}
        catch (CallbackPort.RefusedException exc) { // addConnection()
          try { Protocol.refuseConnection (conn); } catch (muxErrors m) {}
          conn.closeConn();
	}
      }
    }
    catch (Exception exc) {
      exc.printStackTrace();

      _connections.remove (wrapper);
      try { Protocol.refuseConnection (wrapper); } catch (muxErrors m) {}
      wrapper.closeConn();
    }
  }
}

