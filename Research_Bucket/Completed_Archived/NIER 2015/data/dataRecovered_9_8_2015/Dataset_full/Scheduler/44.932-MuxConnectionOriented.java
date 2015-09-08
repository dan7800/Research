/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.comm.muxconn;

import vu.globe.rts.java.*;
import vu.globe.idlsys.g;       // g.idl
import vu.globe.rts.comm.idl.mux.*;     // mux.idl
import vu.globe.rts.comm.idl.mux;       // mux.idl
import vu.globe.rts.comm.idl.p2p.*;     // p2p.idl
import vu.globe.rts.std.idl.stdInf.*;   // stdInf.idl
import vu.globe.rts.std.idl.configure.*; // configure.idl
import vu.globe.util.comm.idl.rawData.*; // rawData.idl

import vu.globe.util.comm.*;
import vu.globe.rts.comm.gwrap.P2PDefs;
import vu.globe.rts.comm.muxcp.CallbackPort;
import vu.globe.rts.comm.muxcp.ListenerPort;
import vu.globe.rts.comm.muxcp.ListenerPortTable;

import vu.globe.rts.security.protocol.SecAddress;

import vu.globe.rts.comm.muxconn.protocol.*;

import vu.globe.util.comm.ProtStack;
import vu.globe.util.thread.EventScheduler;
import vu.globe.util.parse.AttributeString;
import vu.globe.util.exc.AssertionFailedException;
import vu.globe.util.exc.NotImplementedException;
import vu.globe.util.debug.DebugOutput;
import vu.globe.rts.std.StdUtil;

import java.util.*;
import java.net.ProtocolException;

/**
 * A multiplexing listener, multiplexing connector and multiplexing connection
 * object (as in mux.idl) all in one. The multiplexer is layered on top of
 * point-to-point communication objects, referred to as base communication
 * objects. The multiplexer is responsible for creating these base
 * communication objects. To do this, a multiplexer must be provided with the
 * implementation handle and initialisation string of a point-to-point
 * listener, and a point-to-point connector. The information is provided
 * through the initialisation string of the multiplexer ('configurable'
 * interface). See MuxConfig about the format of the initialisation string.
 */

/*
  XXXX 
  
  POTENTIAL PROBLEM: lack of flow control of light conns. Current solution
  is to close a light connection when its number of buffered incoming messages
  exceeds a threshold.
  
  No limit ('BACKLOG') on number of waiting incoming base connections.
*/

/*
  Light-Weight Listener   Light-Weight Connector
          | (n)               | (n)
          |                   |                   Light-Weight Connection
          |                   |                          | (n)
          |                   |                          |
          |                   |                          |
      [ muxListener ]   [ muxConnector ]         [ muxConnection ]   <- infs
     --------------------------------------------------------------
    |                                                              |
    |                  MuxConnectionOriented                       |
    |                                                              |
     --------------------------------------------------------------
          |                   |                          |
          |                   |                          |
          | (1)               | (1)                      | (n)
      Base Listener         Base Connector          Base Connection
 
  The light-weight listener and connector objects are independent Globe clients
  of this multiplexer object. Light-weight connections on the other other hand
  are also clients, but are created internally by this multiplexer object.

  Contact Point
  -------------

  Optionally exports a contact point.


  Finite State Machine for base connection
  --------------------

  See other documentation (design note if I get round to it).

  MAINTAINER NOTE
  ---------------

  For the listener port object and its table we use the muxcp multiplexer's
  implementation of these: ListenerPortTable and ListenerPort. The
  allocation of listener ports (by ListenerPortTable) is the same for both
  multiplexers. The state of a ListenerPort also happens to be the same. If
  the two muxes should ever diverge and separate ListenerPort objects are
  needed, a significant component of the muxcp ListenerPort, implemented by
  CallbackPort, can be reused by this mux. Note that CallbackPort is also
  already reused by ConnectionPort.
*/

public class MuxConnectionOriented extends myMuxConnectionOrientedSkel
{
  /** The base listener. */
  private listener _base_listener;                  // counted
  private contactExporter _base_listener_exporter;  // uncounted
  private comm _base_listener_comm;                 // uncounted

  /** The base connector. */
  private connector _base_connector;            // counted
  private comm _base_connector_comm;            // uncounted

  /** For debugging. */
  private final String _myname = getDebugName();

  /**
   * Event scheduler that is used for all callbacks made by this multiplexer,
   * i.e. by the ConnectionPorts and ListenerPorts.
   */
  private final EventScheduler _sched = new EventScheduler (_myname, 10);

  /** A thread-safe table of remote multiplexers. */
  private final RemoteMuxTable _remote_mux_table = new RemoteMuxTable();

  /**
   * A thread-safe table of connection ports. See notes in ConnectionPortTable
   * about when a connection port may be removed from the table.
   */
  protected final ConnectionPortTable _cport_table =
         new ConnectionPortTable (_sched);

  /** A thread-safe table of listener ports. */
  private final ListenerPortTable _lport_table =
         new ListenerPortTable (_sched, true);

  /**
   * A thread-safe mapping of callback cookies to BaseConnection objects.
   * A BaseConnection object is entered into this mapping when its callback
   * cookie is installed into a Globe connection object. It is removed from the
   * mapping when the Globe connection object makes its final receiveStopped()
   * callback. As a result, a BaseConnection is stored in _base_conns for as
   * long as it contains a counted reference to a Globe connection object (see
   * BaseConnection).
   */
  private final Map _base_conns = Collections.synchronizedMap (new HashMap());

  /**
   * The contact point of this multiplexer. This is the contact point at which
   * incoming base connections are listened for. It is null until exportContact
   * is called.
   */
  private String _local_mux_contact = null;

  /**
   * The protocol stack of this communication object. Set on first request by
   * the client.
   */
  private String _prot_stack = null;

  /** The communication interfaces implemented by this object. */
  private static final interfaces COMM_INFS;

  static {
    COMM_INFS = new interfaces (7);

    COMM_INFS.v[0] = comm.infid;

    COMM_INFS.v[1] = muxListener.infid;
    COMM_INFS.v[2] = contactExporter.infid;
    COMM_INFS.v[3] = muxContactExporter.infid;

    COMM_INFS.v[4] = muxConnector.infid;

    COMM_INFS.v[5] = muxConnection.infid;
    COMM_INFS.v[6] = muxMsg.infid;
  }

  private CtxNameGenerator _ctx_namer = new CtxNameGenerator();

  /**
   * The name under which the base listener object is registered in
   * the local name space.
   */
  private static final String BASE_LISTENER_NAME = "base-listener";

  /**
   * The name under which the base connector object is registered in
   * the local name space.
   */
  private static final String BASE_CONNECTOR_NAME = "base-connector";


  /** Returns the value of _myname. */
  private String getDebugName()
  {
    String name = super.toString();
    String package_part = "vu.globe.rts.comm.muxconn.";

    int index = name.indexOf (package_part);
    if (index != -1) {
      StringBuffer buf = new StringBuffer (name.substring (0, index));
      buf.append (name.substring (index + package_part.length()));
      return buf.toString();
    }
    else {
      return name;
    }
  }

  protected void cleanup() throws vu.globe.rts.std.idl.stdInf.SOIerrors
  {

    GInterface.RelInf (_base_listener);
    GInterface.RelInf (_base_connector);

    /*
      Release Globe connection refs held by any BaseConnections. Be thread
      safe, just in case threads are still active. (Note: we ignore the
      possibility that new base conns are added *subsequently*.)
    */

    BaseConnection[] bconns;
    synchronized (_base_conns) {
      bconns = new BaseConnection [_base_conns.size()];
      _base_conns.values().toArray (bconns);
      _base_conns.clear();
    }

    for (int i = 0; i < bconns.length; i++) {
      BaseConnection bconn = bconns[i];
      msg conn = bconn.removeGlobeConnection();
      GInterface.RelInf (conn);
    }

    super.cleanup();
  }


  // configurable interface
 
  public void configure (String config_data) throws configureErrors
  {
    try {

      AttributeString cfg = new AttributeString (config_data);

      /*** SUBOBJECT CREATION ***/

      /* create the base listener */
      String base_impl = cfg.get (MuxConfig.P2P_LISTENER_IMPLEMENTATION);

      SOInf co_soi = lns.bind (getContext(), "repository/" + base_impl);
      SCInf sci = (SCInf) co_soi.swapInf (SCInf.infid);
      SOInf base_soi = StdUtil.createGlobeObject (sci, getContext(),
                                                  BASE_LISTENER_NAME);
      /* install the base listener */

      _base_listener = (listener) base_soi.swapInf (listener.infid);
      _base_listener_exporter = (contactExporter) base_soi.getUncountedInf (
  						contactExporter.infid);
      _base_listener_comm = (comm) base_soi.getUncountedInf (comm.infid);

      /* create the base connector */
      base_impl = cfg.get (MuxConfig.P2P_CONNECTOR_IMPLEMENTATION);

      co_soi = lns.bind (getContext(), "repository/" + base_impl);
      sci = (SCInf) co_soi.swapInf (SCInf.infid);
      base_soi = StdUtil.createGlobeObject (sci, getContext(),
                                            BASE_CONNECTOR_NAME);
      
      /* install the base connector */

      _base_connector = (connector) base_soi.swapInf (connector.infid);
      _base_connector_comm = (comm) base_soi.getUncountedInf (comm.infid);


      /*** SUBOBJECT CONFIGURATION ***/
   
      /* configure the base listener */

      String base_init = cfg.get (MuxConfig.P2P_LISTENER_INITIALISATION);

      configurable cfg_inf = (configurable) _base_listener.soi.getUncountedInf (
      						configurable.infid);
      cfg_inf.configure (base_init);

      /* configure the base connector */
      base_init = cfg.get (MuxConfig.P2P_CONNECTOR_INITIALISATION);
      cfg_inf = (configurable) _base_connector.soi.getUncountedInf (
      					configurable.infid);
      cfg_inf.configure (base_init);
    }
    catch (configureErrors exc) {
      throw exc;
    }
    catch (Exception exc) {
      exc.printStackTrace();
      throw new configureErrors_misc();
    }
  }

  /*=====================================================================*\
   *                      comm interface methods                         *
  \*=====================================================================*/

  public String getProtocolID()
  {
    return MuxAddress.PROT_ID;
  }

  public String getProtocolStack()
  {
    // note: thread-safe without 'synchronized'
    if (_prot_stack == null) {
      try {
	String listener_stack_string = _base_listener_comm.getProtocolStack();
	String connector_stack_string = _base_connector_comm.getProtocolStack();

	if (! listener_stack_string.equals (connector_stack_string)) {
	  throw new AssertionFailedException (
	   "[MuxConnectionOriented.getProtocolStack] listener stack="
	   + listener_stack_string +
	   ", connector stack=" + connector_stack_string);
	}

        ProtStack listener_stack = new ProtStack (listener_stack_string);
        listener_stack.add (MuxAddress.PROT_ID);
        // FIXME if we are not using security this should not be added.
        // But need to get this somehow via params...
        listener_stack.add(SecAddress.PROT_ID);
        // FIXME maybe introduce: SecType:none 
        _prot_stack = listener_stack.toString();
      }
      catch (ProtStack.IllegalStackException exc) {
        throw new AssertionFailedException();
      }
    }
    return _prot_stack;
  }

  public String getContact()
  {
    return _local_mux_contact;
  }

  public interfaces getCommInfs()
  {
    return COMM_INFS;
  }

  /*=====================================================================*\
   *                 muxConnector interface methods                      *
  \*=====================================================================*/

  // see receivedOpenPortAcked(), receivedOpenPort()
  // * @see receivedOpenPortRefused()
  public connection connectOnPort (int remotePort, String remote)
    throws muxErrors
  {
    // DebugOutput.println (this + "connectOnPort");

    RemoteMux rmux = _remote_mux_table.lookupOrCreate (remote);

    BaseConnection bconn = getOrEstablishConnection (rmux);

    // allocate a local connection port
    ConnectionPort cport = _cport_table.create (bconn);
    int local_cport_number = cport.getLocalNumber();
    
    try {
      bconn.addDependentConnectionPort (cport);
    }
    catch (muxErrors_invOp exc) {
      _cport_table.remove (local_cport_number);
      throw exc;
    }

    try {
      sendOpenPort (bconn, local_cport_number, remotePort);
    }
    catch (muxErrors exc) { // sendOpenPort
      exc.printStackTrace();
      bconn.removeDependentConnectionPort (cport);
      _cport_table.remove (local_cport_number);
      throw exc;
    }

    try {
      cport.waitOpenPortAcked();
    }
    catch (muxErrors exc) { // waitOpenPortAcked()
      DebugOutput.println ("connection to " + remote + ", port " + remotePort +
        " refused");
      bconn.removeDependentConnectionPort (cport);
      _cport_table.remove (local_cport_number);
      throw exc;
    }

    try {
      return createLightConnection (local_cport_number);
    }
    catch (Exception exc) {
      bconn.removeDependentConnectionPort (cport);
      _cport_table.remove (local_cport_number);
      DebugOutput.printException (exc);
      throw new muxErrors_misc();
    }
  }

  /**
   * Creates a LightConnection object around the given local connection port
   * number, and returns the (counted) connection interface of the object.
   */
  private connection createLightConnection (int local_cport_number)
    throws Exception
  {
    LightConnection lwconn = new LightConnection (this, this.soi,
                                                  local_cport_number);

    // return a proper Globe interface
    SOInf lw_soi = lwconn.getSOI();
    StdUtil.initGlobeObject (lw_soi, this.soi.getContext(),
                               _ctx_namer.nextName ());
    return (connection) lw_soi.swapInf (connection.infid);
  }

  /**
   * Processes an incoming 'open port acked' message.
   * @see connectOnPort()
   * @see msgReceived()
   */
  private void receivedOpenPortAcked (BaseConnection bconn,
				      ProtOpenPortAcked msg)
    throws ProtocolException
  {
    // DebugOutput.println (this + "receivedOpenPortAcked");
    int local_cport_number = msg.getReceiverConnectionPort();

    ConnectionPort cport = _cport_table.lookup(local_cport_number);
    if (cport == null) {

      // closes the base connection
      throw new ProtocolException ("[MuxConnectionOriented] " + msg +
        ": unexpected message from " + bconn.getRemoteName());
    }
    // notifies waiting connectOnPort()
    // may throw ProtocolException which will close the connection
    cport.eventOpenPortAcked (msg);
  }

  /**
   * Processes an incoming 'open port refused' message.
   * @see connectOnPort()
   * @see receivedOpenPort()
   * @see receivedOpenPortAcked
   * @see msgReceived()
   */
  private void receivedOpenPortRefused (BaseConnection bconn,
    ProtOpenPortRefused msg) throws ProtocolException
  {
    int local_cport_number = msg.getReceiverConnectionPort();

    ConnectionPort cport = _cport_table.lookup (local_cport_number);
    if (cport == null) {
      // closes the base connection
      throw new ProtocolException ("[MuxConnectionOriented] " + msg +
        ": unexpected message from " + bconn.getRemoteName());
    }
    // notifies waiting connectOnPort()
    // may throw ProtocolException which will close the connection
    cport.eventOpenPortRefused (msg);
  }


  public void asyncConnectOnPort (int remotePort, String remote,
               connectorCB cb, g.opaque cookie)
  {
    throw new NotImplementedException();
  }

  /*=====================================================================*\
   *                  contactExporter interface methods                  *
  \*=====================================================================*/

  // Note that contactExporter is defined in p2p.idl, and its methods throw
  // commErrors rather than muxErrors.

  public void exportContact (String contact) throws commErrors
  {
    try {
      // note the case that contact == null
      _base_listener_exporter.exportContact (contact);
      _local_mux_contact = _base_listener_comm.getContact();

      // The underlying listener may now start buffering incoming connections.
      // We *could* delay registering a listening callback until the first
      // client registers a callback with us. However, that will keep remote
      // mux connectors waiting indefinitely when trying to connect. Therefore,
      // register it now.

      listenCB my_cb = (listenCB) this.getCBInf (listenCB.infid);
      _base_listener.regListenCB (my_cb, null);
    }
    catch (commErrors exc) {
      throw exc;
    }
    catch (Exception exc) {
      exc.printStackTrace();
      throw new commErrors_misc();
    }
  }

  // @see listenStopped
  public void closeContact()
  {
    _base_listener_exporter.closeContact();
    // to avoid unnecessary race conditions with connArrived, remaining work,
    // including closing base connections, is performed by listenStopped
  }

  /*=====================================================================*\
   *               muxContactExporter interface methods                  *
  \*=====================================================================*/

  public int exportContactOnPort (int localPort) throws muxErrors
  {
    ListenerPort port;
    if (localPort == mux.NoPort) {
      port = _lport_table.create();
    }
    else {
      port = _lport_table.lookupOrCreate (localPort);
    }

    port.export();
    return port.getPortNumber();

  }

  public void closeContactOnPort (int localPort) throws muxErrors_illegalPort
  {
    // MAINTAINER: please be very careful when changing this method. Race
    // conditions with pauseListenCBOnPort() are easily introduced.

    ListenerPort port = _lport_table.lookup (localPort);
    if (port == null)
      throw new muxErrors_illegalPort();

    closeListenPort (port);
    // Thread safety note: from this point on, the port's 'paused' status 
    // (port.isPaused()) can go from true to false, but not vice-versa. 

    if (! port.isPaused()) {
      // normal case
      _lport_table.remove (localPort);
    }
    // else: delay the _lport_table.remove (localPort) until the client calls
    //       pauseListenCBOnPort(localPort, false). (Otherwise the client will
    //       have no way of unpausing the port.)
  }

  private void closeListenPort (ListenerPort port)
  {
    // 'elts' are light-weight connections created by us (interspersed with
    // Exceptions). As we close the connections they will each invoke
    // closeConnOnPort().

    Object[] elts = port.closePort();
    for (int i = 0; i < elts.length; i++) {
      if (elts[i] instanceof connection) {
        try {
          connection conn = (connection) elts [i];
          conn.closeConn();
          conn.relInf();
	}
        catch (Exception exc) { // just in case
          DebugOutput.printException (exc);
        }
      }
      // else: ignore the Exception elements in 'elts'
    }
  }

  /**
   * Processes an incoming 'close port' message.
   * @see msgReceived()
   * @see receivedData()
   */
  private void receivedClosePort (BaseConnection bconn, ProtClosePort msg)
  		throws ProtocolException
  {
    // DebugOutput.println (this + "receivedClosePort");

    int local_cport_number = msg.getReceiverConnectionPort();
    int remote_cport_number = msg.getSenderConnectionPort();

    ConnectionPort cport = _cport_table.lookup (local_cport_number);

    if (cport == null) {
      // closes the base connection
      throw new ProtocolException ("[MuxConnectionOriented] " + msg +
        ": unexpected message from " + bconn.getRemoteName());
    }
    bconn.removeDependentConnectionPort (cport);

    // may throw ProtocolException which will close the connection
    boolean send_close = cport.eventRemoteCloses (msg);   


    if (send_close) {

      // ack close by sending another close (without error message)

      try {
        sendClosePort (bconn, local_cport_number, remote_cport_number, null);
      }
      catch (muxErrors exc) {
        DebugOutput.printException (exc);
        return;
      }
    }
  }


  /*=====================================================================*\
   *                   muxListener interface methods                     *
  \*=====================================================================*/

  public void regListenCBOnPort (int localPort, listenCB cb, g.opaque cookie)
  				throws muxErrors
  {
    ListenerPort port = _lport_table.lookupOrCreate (localPort);
    port.regCallback (cb, cookie);
  }

  public connection acceptOnPort (int localPort) throws muxErrors
  {
    ListenerPort lport = _lport_table.lookup (localPort);
    if (lport == null) {
      throw new muxErrors_illegalPort();
    }
    return lport.accept();
  }

  public void pauseListenCBOnPort(int localPort, short /* g.bool */ on)
    throws muxErrors
  {
    // MAINTAINER: please be very careful when changing this method. Race
    // conditions with closeContactOnPort() are easily introduced.

    ListenerPort port = _lport_table.lookup (localPort);
    if (port == null)
      throw new muxErrors_illegalPort();

    port.pause(on == g.bool.True);

    // see closeContactOnPort()
    if (on == g.bool.False && port.closing()) {

      // Thread safety note: a concurrent closeContactOnPort() call may cause
      // two attempts to remove localPort from _lport_table: below and in
      // closeContactOnPort(). That's OK.

      _lport_table.remove (localPort);
    }
  }


  /**
   * Processes an incoming 'open port' message.
   * @see connectOnPort()
   * @see msgReceived()
   */
  private void receivedOpenPort (BaseConnection bconn, ProtOpenPort msg)
    throws ProtocolException
  {
    // DebugOutput.println (this + "receivedOpenPort");

    int local_lport_number = msg.getReceiverListenerPort();
    int remote_cport_number = msg.getSenderConnectionPort();

    ListenerPort lport = _lport_table.lookup (local_lport_number);
    if (lport == null) {
      try {
        sendOpenPortRefused (bconn, local_lport_number, remote_cport_number,
	  "nobody listening on port " + local_lport_number);
      }
      catch (muxErrors exc) {
        DebugOutput.printException (exc);
      }
      return;
    }

    // XXX could perform lport.addConnection's checks (for max backlog etc.)
    // in advance

    // allocate a local connection port
    ConnectionPort cport = _cport_table.create (bconn, remote_cport_number);
    int local_cport_number = cport.getLocalNumber();

    try {
      bconn.addDependentConnectionPort (cport);
    }
    catch (muxErrors_invOp exc) {

      // the base connection is closed so we can't send a refuse message back
      _cport_table.remove (local_cport_number);
      DebugOutput.printException (exc);
      return;
    }

    connection lwconn;
    try {
      lwconn = createLightConnection (local_cport_number);
    }
    catch (Exception exc) {

      bconn.removeDependentConnectionPort (cport);
      _cport_table.remove (local_cport_number);

      try {
        sendOpenPortRefused (bconn, local_lport_number,
	  remote_cport_number,
	  "listening port " + local_lport_number + ": " + exc.getMessage());
      }
      catch (muxErrors exc2) {
        DebugOutput.printException (exc2);
      }
      DebugOutput.printException (exc);
      return;
    }

    try {
      // addConnection may notify the client. Take care not hold any locks.
      lport.addConnection (lwconn);
    }
    catch (CallbackPort.RefusedException exc) { // addConnection()
      bconn.removeDependentConnectionPort (cport);
      _cport_table.remove (local_cport_number);

      try {
        sendOpenPortRefused (bconn, local_lport_number,
	  remote_cport_number,
	  "listening port " + local_lport_number + ": " + exc.getMessage());
      }
      catch (muxErrors exc2) {
        DebugOutput.printException (exc2);
      }
      return;
    }

    try {
      sendOpenPortAcked (bconn, local_cport_number, remote_cport_number);
    }
    catch (muxErrors exc) { // sendOpenPortAcked()
      // note: client will later accept an unusable connection
      bconn.removeDependentConnectionPort (cport);
      _cport_table.remove (local_cport_number);
      DebugOutput.printException (exc);
      return;
    }
  }



  /*=====================================================================*\
   *                      muxMsg interface methods                       *
  \*=====================================================================*/

  public void regMsgCBOnPort(int localPort, msgCB cb, g.opaque cookie)
    throws muxErrors
  {
    ConnectionPort cport = _cport_table.lookup (localPort);
    if (cport == null) {
      throw new muxErrors_illegalPort();
    }
    cport.regCallback (cb, cookie);
  }

  public void pauseMsgCBOnPort(int localPort, short /* g.bool */ on)
    throws muxErrors
  {
    // MAINTAINER: please be very careful when changing this method. Race
    // conditions with closeConnOnPort() are easily introduced.

    ConnectionPort cport = _cport_table.lookup (localPort);
    if (cport == null)
      throw new muxErrors_illegalPort();

    cport.pause(on == g.bool.True);
  }

  // see receivedData()
  public void sendOnPort(int localPort, int remotePort, String remote,
                         rawDef pkt) throws muxErrors
  {
    // since we're connection-oriented, we should ignore remotePort and remote
    ConnectionPort cport = _cport_table.lookup (localPort);
    if (cport == null) {
      throw new muxErrors_illegalPort();
    }
    cport.eventSendData();

    int remote_number = cport.getRemoteNumber();
    BaseConnection bconn = cport.getBaseConnection();
    // may throw muxErrors
    sendData (bconn, localPort, remote_number, pkt);
  }

  public muxMsg_receiveOnPort_Out receiveOnPort(int localPort)
    throws muxErrors
  {
    ConnectionPort cport = _cport_table.lookup (localPort);
    if (cport == null) {
      throw new muxErrors_illegalPort();
    }
    return cport.receiveOnPort();
  }

  /**
   * Processes an incoming 'data' message.
   * @see sendOnPort()
   * @see msgReceived()
   */
  private void receivedData (BaseConnection bconn, ProtData msg)
  		throws ProtocolException
  {
    // DebugOutput.println (this + "receivedData");

    int local_cport_number = msg.getReceiverConnectionPort();
    int remote_cport_number = msg.getSenderConnectionPort();
    rawDef data = msg.getData();
    
    ConnectionPort cport = _cport_table.lookup (local_cport_number);
    if (cport == null) {
      try {
        sendClosePort (bconn, local_cport_number, remote_cport_number,
          "receiver's conn port " + local_cport_number + 
          " non-existent or closed");
      }
      catch (muxErrors exc) {
        DebugOutput.printException (exc);
      }
      return;
    }

    try {
      // may throw ProtocolException which will close the connection
      // eventDataMessage may notify the client. Take care not hold any locks.
      cport.eventDataMessage (msg);
    }
    catch (CallbackPort.RefusedException exc) {

      try {
        sendClosePort (bconn, local_cport_number, remote_cport_number,
          "overflow of receiver's conn port " + local_cport_number);
      }
      catch (muxErrors exc2) {
        DebugOutput.printException (exc2);
      }
      return;
    }
  }


  /*=====================================================================*\
   *                 muxConnection interface methods                     *
  \*=====================================================================*/

  public String getLocalOnPort(int localPort) throws muxErrors
  {
    ConnectionPort cport = _cport_table.lookup (localPort);
    if (cport == null) {
      throw new muxErrors_illegalPort();
    }
    return cport.getLocalOnPort();
  }

  public String getRemoteOnPort(int localPort) throws muxErrors
  {
    ConnectionPort cport = _cport_table.lookup (localPort);
    if (cport == null) {
      throw new muxErrors_illegalPort();
    }
    return cport.getRemoteOnPort();
  }

  public void closeConnOnPort(int localPort) throws muxErrors
  {
    ConnectionPort cport = _cport_table.lookup (localPort);
    if (cport == null)
      throw new muxErrors_illegalPort();

    boolean send_close = cport.eventUserCloses();

    if (send_close) {

      BaseConnection bconn = cport.getBaseConnection();
      int remote_cport_number = cport.getRemoteNumber();

      // may throw muxErrors
      sendClosePort (bconn, localPort, remote_cport_number, null);
    }
  }


  /*=====================================================================*\
   *                      getOrEstablishConnection                       *
  \*=====================================================================*/


  /*
   * This is the main primitive for initiating a base connection to a remote
   * multiplexer. See also: connArrived() and receivedOpenConn().
   */

  /**
   * Returns an established connection to a remote mux or, if none exists,
   * establishes a new connection.
   *
   * @param contact	the contact point of the remote mux
   */
  private BaseConnection getOrEstablishConnection (String contact)
    throws muxErrors
  {
    RemoteMux rmux = _remote_mux_table.lookupOrCreate (contact);
    return getOrEstablishConnection (rmux);
  }

  /**
   * Returns an established connection to a remote mux or, if none exists,
   * establishes a new connection.
   *
   * @param rmux	the remote mux
   */
  private BaseConnection getOrEstablishConnection (RemoteMux rmux)
    throws muxErrors
  {
    // may throw muxErrors
    BaseConnection bconn = rmux.getOrAnnounceConnection();

    if (bconn == null) {

      msg conn;
      try {
        connection c = _base_connector.connect (rmux.getContact());
	conn = (msg) c.soi.swapInf (msg.infid);
        sendGlobeOpenConn (conn, _local_mux_contact);
      }
      catch (muxErrors exc) {				// sendGlobeOpenConn()
        rmux.cancelAnnouncedConnection (exc);
	throw exc;
      }
      catch (commErrors_illegalAddress exc) {		// connect()
	DebugOutput.printException (exc);
        muxErrors mexc = new muxErrors_illegalAddress();
        rmux.cancelAnnouncedConnection (mexc);
	throw mexc;
      }
      catch (commErrors_peerRefused exc) {		// connect()
	DebugOutput.printException (exc);
        muxErrors mexc = new muxErrors_peerRefused();
        rmux.cancelAnnouncedConnection (mexc);
	throw mexc;
      }
      catch (commErrors_comm exc) { 			// connect()
	DebugOutput.printException (exc);
        muxErrors mexc = new muxErrors_comm();
        rmux.cancelAnnouncedConnection (mexc);
	throw mexc;
      }
      catch (Exception exc) {
	DebugOutput.printException (exc);
        muxErrors mexc = new muxErrors_misc();
        rmux.cancelAnnouncedConnection (mexc);
	throw mexc;
      }

      // any g.opaque subtype will do
      bconn = new BaseConnection (conn, rmux);
      g.opaque_pointer cookie = new g.opaque_pointer();
      _base_conns.put (cookie, bconn);

      // Call setAnnouncedConnection() before regMsgCB(), to avoid a race
      // condition with rmux.removeBaseConnection in the callback (e.g. on
      // error). It is unlikely that regMsgCB() will fail.
      rmux.setAnnouncedConnection (bconn);

      try {
	msgCB cb = (msgCB) this.getCBInf (msgCB.infid);
	conn.regMsgCB (cb, cookie);
      }
      catch (Exception exc) {

        rmux.removeBaseConnection (bconn);
	DebugOutput.printException (exc);
        muxErrors mexc = new muxErrors_misc();
        _base_conns.remove (cookie);
        sendGlobeCeaseConn (conn,
	  "[MuxConnectionOriented.getOrEstablishConnection]"+
	  " unable to register callback in base connection");
	closeConn (conn);
	throw mexc;
      }
      conn.relInf();
    }
    return bconn;
  }

  /*=====================================================================*\
   *         callback handlers of underlying comm objects                *
  \*=====================================================================*/

  // listenCB callback interface methods
 
  // @see closeContact
  public void listenStopped (g.opaque listen_cookie)
  {
    // Continues closeContact()'s work, but can also be called spontaneously by
    // the communication object.

    // Close base connections. At this point listening has stopped, so new
    // connections are no longer being created

    BaseConnection[] bconns;
    synchronized (_base_conns) {
      bconns = new BaseConnection [_base_conns.size()];
      _base_conns.values().toArray (bconns);
    }

    // cease and close
    for (int i = 0; i < bconns.length; i++) {

      try {
        BaseConnection bconn = bconns [i];
        msg conn = bconn.getGlobeConnection();
        if (conn != null) {
          sendGlobeCeaseConn (conn, "stopped listening");
          closeConn (conn);  // see receiveStopped()
	  conn.relInf();
        }
      }
      catch (Exception exc) { // just in case
        DebugOutput.printException (exc);
      }
    }

    // Safely iterate through the listen ports, by synchronising on the table.
    // Note that strictly speaking this is unnecessary, since concurrent
    // additions or removals of ports by the client are illegal (mux has been
    // closed).

    synchronized (_lport_table) {

      Iterator it = _lport_table.iterator();

      while (it.hasNext()) {
        try {
          ListenerPort port = (ListenerPort) it.next();
          closeListenPort (port);
          it.remove();
        }
        catch (Exception exc) { // just in case
          DebugOutput.printException (exc);
        }
      }
    }
  }

  // @see receivedOpenConn()
  // @see getOrEstablishConnection()
  public void connArrived (g.opaque listen_cookie)
  {
    // DebugOutput.println (this + "connArrived");

    // Add to _base_conns. Further processing happens when the 'open conn'
    // message has been received through the connection. (See receivedOpenConn).
    msg conn = null;
    g.opaque_pointer cookie = null;

    try {
      connection c = _base_listener.accept();
      conn = (msg) c.soi.swapInf (msg.infid);

      BaseConnection bconn = new BaseConnection (conn);

      // any g.opaque subtype will do
      cookie = new g.opaque_pointer();
      _base_conns.put (cookie, bconn);

      msgCB cb = (msgCB) this.getCBInf (msgCB.infid);
      conn.regMsgCB (cb, cookie);
    }
    catch (Exception exc) {
      DebugOutput.printException (exc);

      if (cookie != null) {
        _base_conns.remove (cookie);
      }

      if (conn != null) {

        sendGlobeCeaseConn (conn, null);
	closeConn (conn); // see receiveStopped()
      }
    }
    GInterface.RelInf (conn);
  }

  /**
   * Processes an incoming 'open conn' message.
   * @see connArrived()
   * @see getOrEstablishConnection()
   * @see msgReceived()
   */
  private void receivedOpenConn (BaseConnection bconn, ProtOpenConn msg)
    throws ProtocolException
  {
    // DebugOutput.println (this + "receivedOpenConn");

    // If the remote mux exports a contact point, assign the connection to the
    // RemoteMux.

    // find or create a RemoteMux
    String contact = msg.getContact();
    RemoteMux rmux;
    if (contact != null) {
      rmux = _remote_mux_table.lookupOrCreate (contact);
    }
    else {
      // remote mux does not export a contact point
      rmux = null;
    }
    // may throw ProtocolException which will close the connection
    bconn.eventOpenConnMessage (rmux);

    if (rmux != null) {
      rmux.addIncomingConnection (bconn);
    }
  }

  // msgCB interface methods
 
  public void receiveStopped (g.opaque cookie)
  {
    // This method is called after a base connection is closed, whether by us
    // or by the remote party. Performs most of the operations for when the
    // finite state machine of the base connection goes to 'closed' state, and
    // the actions associated with 'base closes' in the finite state machine of
    // the light-weight connection.

    // DebugOutput.println (this + "receiveStopped");
    BaseConnection bconn = (BaseConnection) _base_conns.remove (cookie);
    if (bconn == null) {
      // Something went wrong while setting up the connection. Should not
      // normally happen though.
      DebugOutput.println (
        "[MuxConnectionOriented.receiveStopped] unknown base connection");
      return;
    }

    RemoteMux rmux = bconn.getRemoteMux();

    if (rmux != null) {
      // may have occurred already (e.g. in closeBaseConnection())
      rmux.removeBaseConnection (bconn);
    }

    /* 
      Close the connection (in case this close was initiated remotely) and
      notify any light-weight connections layered on top of it. Note: it is
      possible that the connection has been closed already (e.g. by
      closeBaseConnection()). Also release the multiplexer's reference to the
      connection.
    */
    msg conn = bconn.removeGlobeConnection();
    DebugOutput.dassert (conn != null);
    closeConn (conn);
    conn.relInf();

    ConnectionPort[] dependent_cports = bconn.close();
    if (dependent_cports != null) {
      try {
        for (int i = 0; i < dependent_cports.length; i++) {
          ConnectionPort cport = dependent_cports[i];
	  cport.eventBaseCloses();
        }
      }
      catch (Exception exc) { // just in case
        DebugOutput.printException (exc);
      }
    }
  }

  public void msgReceived (g.opaque cookie)
  {
    // DebugOutput.println (this + "msgReceived");

    BaseConnection bconn = (BaseConnection) _base_conns.get (cookie);
    if (bconn == null) {
      // Something went wrong while setting up the connection. Should not
      // normally happen though.
      DebugOutput.println (
        "[MuxConnectionOriented.msgReceived] unknown base connection");
      return;
    }
    
    msg conn = bconn.getGlobeConnection();
    // The Globe ref that is stored in bconn is released by bconn's
    // receiveStopped() notification. This notification cannot occur before
    // or during a call to msgReceived().
    DebugOutput.dassert (conn != null);

    try {

      msg_receive_Out receive_out;
      try {
        receive_out = conn.receive();
      }
      catch (Exception exc) {

        DebugOutput.printException (exc);

        // closes the base connection
	throw new commErrors_misc();
      }

      rawDef r = receive_out.retval;
      DebugOutput.dassert (r != null);
      ProtMessage msg = ProtMessageFact.unmarshallMessage (new RawCursor (r));

      int type = msg.getType();
      switch (type) {
      case ProtMessageFact.PROT_OPEN_CONN:

        receivedOpenConn (bconn, (ProtOpenConn) msg);
	break;

      case ProtMessageFact.PROT_CEASE_CONN:

	// just print out the reason, and let further events take their course
	String reason = ((ProtCeaseConn) msg).getReason();
	if (reason != null) {
	  DebugOutput.println (DebugOutput.DBG_NORMAL, bconn.getRemoteName() +
	    " is closing connection with reason: " + reason);
	}
	break;

      default:

	// light-weight messages

        // may throw ProtocolException which will close the base connection
	bconn.eventReceivedLightMessage (msg);

        switch (type) {
        case ProtMessageFact.PROT_OPEN_PORT:
          receivedOpenPort (bconn, (ProtOpenPort) msg);
	  break;
        case ProtMessageFact.PROT_OPEN_PORT_ACKED:
          receivedOpenPortAcked (bconn, (ProtOpenPortAcked) msg);
	  break;
        case ProtMessageFact.PROT_OPEN_PORT_REFUSED:
          receivedOpenPortRefused (bconn, (ProtOpenPortRefused) msg);
	  break;
        case ProtMessageFact.PROT_CLOSE_PORT:
          receivedClosePort (bconn, (ProtClosePort) msg);
	  break;
        case ProtMessageFact.PROT_DATA:
          receivedData (bconn, (ProtData) msg);
	  break;
        default:
          // closes the base connection
	  throw new ProtocolException ("got unexpected message: " + msg);
        }
      }
    }

    // commErrors and ProtocolExceptions are fatal to the base connection

    catch (commErrors exc) {
      // from receive()

      DebugOutput.printException (exc);
      sendGlobeCeaseConn (conn, exc.getMessage());
      closeBaseConnection (bconn);
    }
    catch (ProtocolException exc) {
      // from:
      // eventReceivedLightMessage()
      // unmarshallMessage()
      // received...()
      // this method

      DebugOutput.printException (exc);
      sendGlobeCeaseConn (conn, exc.getMessage());
      closeBaseConnection (bconn);
    }
    finally {
      conn.relInf();
    }
  }

  /*=====================================================================*\
   *                      miscellaneous operations                       *
  \*=====================================================================*/

  /**
   * Performs some operations necessary to close a base connection. Note: does
   * not send a 'cease conn' message first.
   *
   * @param bconn	the base connection
   */
  private void closeBaseConnection (BaseConnection bconn)
  {
    // DebugOutput.println (this + "closeBaseConnection");

    // Unregister this base connection from the RemoteMux object as quickly as
    // possible, to avoid reuse of the base connection. Note however, that this
    // is not thread-safe (the base connection may concurrently be registering
    // with the RemoteMux object). So it is repeated in receiveStopped() in
    // a thread-safe way.
    RemoteMux rmux = bconn.getRemoteMux();
    if (rmux != null) {
      rmux.removeBaseConnection (bconn);
    }

    msg conn = bconn.getGlobeConnection(); // counted ref
    if (conn != null) {
      closeConn (conn);   // further handling in receiveStopped()
      conn.relInf();
    }
  }


  /**
   * Invokes closeConn() on a Globe connection object.
   */
  private void closeConn (msg conn)
  {
    try {
      connection c = (connection) conn.soi.getUncountedInf (connection.infid);
      c.closeConn();
    }
    catch (Exception exc) { // getUncountedInf
      DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
      throw new AssertionFailedException();
    }
  }

  /*=====================================================================*\
   *              sending messages over a BaseConnection                 *
  \*=====================================================================*/

  /*
   * All the following send... methods close the base connection (in an
   * equivalent way to closeBaseConnection()) when they encounter a
   * communication error.
   */

  /**
   * Sends an 'open port' message.
   *
   * @param bconn        	the connection to send the message over
   * @param sender_cport        message field: connection port of the sender
   * @param receiver_lport      message field: listener port of the receiver
   *
   * @exeption muxErrors_comm   a communication error, in which case the
   *				connection has been closed
   */
  private void sendOpenPort (BaseConnection bconn, int sender_cport,
          int receiver_lport) throws muxErrors
  {
    // DebugOutput.println (this + "sendOpenPort");
    rawDef r = RawOps.createRaw();
    ProtOpenPort.marshall (new RawCursor (r), sender_cport, receiver_lport);
    send (bconn, r);
  }

  /**
   * Sends an 'open port acked' message.
   *
   * @param bconn        	the connection to send the message over
   * @param sender_cport        message field: connection port of the sender
   * @param receiver_cport      message field: connection port of the receiver
   *
   * @exeption muxErrors_comm   a communication error, in which case the
   *				connection has been closed
   */
  private void sendOpenPortAcked (BaseConnection bconn, int sender_cport,
          int receiver_cport) throws muxErrors
  {
    // DebugOutput.println (this + "sendOpenPortAcked");
    rawDef r = RawOps.createRaw();
    ProtOpenPortAcked.marshall (new RawCursor (r), sender_cport,receiver_cport);
    send (bconn, r);
  }

  /**
   * Sends an 'open port refused' message.
   *
   * @param bconn        	the connection to send the message over
   * @param sender_lport        message field: listener port of the sender
   * @param receiver_cport      message field: connection port of the receiver
   * @param reason      	the reason for refusing, null if omitted
   *
   * @exeption muxErrors_comm   a communication error, in which case the
   *				connection has been closed
   */
  private void sendOpenPortRefused (BaseConnection bconn, int sender_lport,
          int receiver_cport, String reason) throws muxErrors
  {
    // DebugOutput.println (this + "sendOpenPortRefused");
    rawDef r = RawOps.createRaw();
    ProtOpenPortRefused.marshall (new RawCursor (r), sender_lport,
			receiver_cport, reason);
    send (bconn,r);
  }

  /**
   * Sends a 'data' message. The data corresponds to a message transmitted by
   * a higher layer.
   *
   * @param bconn        	the connection to send the message over
   * @param sender_cport        message field: connection port of the sender
   * @param receiver_cport      message field: connection port of the receiver
   * @param data      		message field: the data
   *
   * @exeption muxErrors_comm   a communication error, in which case the
   *				connection has been closed
   */
  private void sendData (BaseConnection bconn, int sender_cport,
    int receiver_cport, rawDef data) throws muxErrors
  {
    rawDef r = RawOps.createRaw();
    ProtData.marshall (new RawCursor (r), sender_cport,
			receiver_cport, data);
    send (bconn,r);
  }

  /**
   * Sends a 'close port' message.
   *
   * @param bconn        	the connection to send the message over
   * @param sender_cport        message field: connection port of the sender
   * @param receiver_cport      message field: connection port of the receiver
   * @param reason          	null if the port is being closed without an
   				error. A descriptive string if the port is being
				closed due to an error
   *
   * @exeption muxErrors_comm   a communication error, in which case the
   *				connection has been closed
   */
  private void sendClosePort (BaseConnection bconn, int sender_cport,
          int receiver_cport, String reason) throws muxErrors
  {
    // DebugOutput.println (this + "sendClosePort");
    rawDef r = RawOps.createRaw();
    ProtClosePort.marshall (new RawCursor (r), sender_cport, receiver_cport,
                            reason);
    send (bconn,r);
  }

  /**
   * Sends a message.
   *
   * @param bconn        	the connection to send the message over
   *
   * @exeption muxErrors_comm   a communication error, in which case the
   *				connection has been closed
   */
  private void send (BaseConnection bconn, rawDef r) throws muxErrors
  {
    // DebugOutput.println (this + "sending over: " + bconn);

    msg conn = bconn.getGlobeConnection();
    if (conn == null) {
      throw new muxErrors_comm();
    }
    try {
      sendGlobe (conn, r); // may throw muxErrors and close the Globe connection
    }
    catch (muxErrors exc) {
      // see closeBaseConnection()
      RemoteMux rmux = bconn.getRemoteMux();
      if (rmux != null) {
        rmux.removeBaseConnection (bconn);
      }
    }
    finally {
      conn.relInf();
    }
  }

  /*=====================================================================*\
   *              sending messages over a Globe connection               *
  \*=====================================================================*/

  /*
   * All sendGlobe... methods close the Globe connection when they encounter a
   * communication error. 
   */

  /**
   * Sends an 'open connection' message.
   *
   * @param conn       	the connection to send the message over
   * @param conn       	the contact point of the local multiplexer, or null if
   *			this multiplexer does not export a contact point
   *
   * @exeption muxErrors_comm   a communication error, in which case the
   *				connection has been closed
   */
  private void sendGlobeOpenConn (msg conn, String contact) throws muxErrors
  {
    // DebugOutput.println (this + "sendGlobeOpenConn");
    rawDef r = RawOps.createRaw();
    ProtOpenConn.marshall (new RawCursor (r), contact);
    sendGlobe (conn, r);
  }

  /**
   * Sends a 'cease connection' message. If a communication error occurs,
   * closes the connection but does not raise an exception.
   *
   * @param conn       	the connection to send the message over
   * @param reason      the reason for closing the connection, null for no
   *			reason
   */
  private void sendGlobeCeaseConn (msg conn, String reason)
  {
    // DebugOutput.println (this + "sendGlobeCeaseConn");

    rawDef r = RawOps.createRaw();
    ProtCeaseConn.marshall (new RawCursor (r), reason);

    try {
      sendGlobe (conn, r);
    }
    catch (muxErrors exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
    }
  }

  /**
   * Sends a message.
   *
   * @param conn        	the connection to send the message over
   *
   * @exeption muxErrors_comm   a communication error, in which case the
   *				connection has been closed
   */
  private void sendGlobe (msg conn, rawDef r) throws muxErrors
  {
    try {
      conn.send (null, r);
    }
    catch (commErrors_comm exc) {
      closeConn (conn);
      DebugOutput.printException (exc);
      throw new muxErrors_comm();
    }
    catch (Exception exc) {
      closeConn (conn);
      DebugOutput.printException (exc);
      throw new muxErrors_misc();
    }
  }

  public String toString()
  {
    return "[" + _myname + " contact=" +
           String.valueOf (_local_mux_contact) + " stack=" +
           String.valueOf (_prot_stack) + "] ";
  }

  /**
   * Generates unique name space context names for light-weight connection
   * objects.
   */
  private static class CtxNameGenerator
  {
    private static final String prefix = "lwconn";
    private int counter = 0;

    /**
     * Generates a new unique name space context name.
     */
    public synchronized String nextName ()
    {
      return prefix + String.valueOf (counter++);
    }
  }
}
