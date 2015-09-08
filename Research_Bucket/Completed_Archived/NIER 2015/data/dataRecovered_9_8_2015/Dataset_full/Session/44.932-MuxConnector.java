/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.comm.muxcp;

import vu.globe.rts.java.*;
import vu.globe.idlsys.g;     // g.idl
import vu.globe.rts.comm.idl.mux.*;   // mux.idl
import vu.globe.rts.comm.idl.p2p.*;   // p2p.idl
import vu.globe.rts.std.idl.stdInf.*; // stdInf.idl
import vu.globe.rts.std.idl.configure.*; // configure.idl

import vu.globe.util.comm.ProtStack;
import vu.globe.util.parse.AttributeString;
import vu.globe.util.exc.*;
import vu.globe.util.debug.DebugOutput;
import vu.globe.rts.std.StdUtil;

/**
 * A multiplexing connector object (as in mux.idl). The multiplexing connector
 * is layered on top of a point-to-point connector (as in p2p.idl). The
 * multiplexer is responsible for creating the point-to-point connector. To do
 * this, a multiplexer must be provided with the implementation handle and
 * initialisation string of a point-to-point connector. The information is
 * provided through the initialisation string of the multiplexer ('configurable'
 * interface). See MuxCpConfig about the format of the initialisation string.
 */

/*
  To communicate with the remote party, MuxConnector must install a callback
  in an incoming connection. The callback is removed after the requested
  connection has been accepted or refused, so that the client
  can later install its own callback. Replacing a callback is not supported by
  the standard communication interfaces. Therefore a ConnectionWrapper is used.
  ConnectionWrappers are Globe objects which are created by a
  ConnectionWrapperFact.
*/

public class MuxConnector extends myMuxConnectorSkel
{
  /** The point-to-point connector. */
  private connector _p2p_connector;            // counted

  /** The point-to-point connector. */
  private comm _p2p_comm;                      // uncounted

  /** A factory of connection wrappers. */
  private ConnectionWrapperFact _wrapper_fact;

  /** The protocol stack of this communication object. Set on first request. */
  private String _prot_stack = null;

  /**
   * The name under which the point-to-point connector object is registered in
   * the local name space.
   */
  private static final String P2P_NAME = "p2p";

  /** The communication interfaces implemented by this object. */
  private static final interfaces COMM_INFS;

  static {
    COMM_INFS = new interfaces (2);

    COMM_INFS.v[0] = comm.infid;
    COMM_INFS.v[1] = muxConnector.infid;
  }


  protected void cleanup() throws vu.globe.rts.std.idl.stdInf.SOIerrors
  {
    GInterface.RelInf (_p2p_connector);
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
      // create the point-to-point connector
 
      AttributeString cfg = new AttributeString (config_data);
      String p2p_impl = cfg.get (MuxCpConfig.P2P_IMPLEMENTATION);
      String p2p_init = cfg.get (MuxCpConfig.P2P_INITIALISATION);
 
      SOInf co_soi = lns.bind (getContext(), "repository/" + p2p_impl);
      SCInf sci = (SCInf) co_soi.swapInf (SCInf.infid);
      SOInf p2p_soi = StdUtil.createGlobeObject (sci, getContext(), P2P_NAME);
 
      // initialise the point-to-point connector
      configurable cfg_inf = (configurable) p2p_soi.getUncountedInf (
      						configurable.infid);
      cfg_inf.configure (p2p_init);
 
      // install the point-to-point connector
      _p2p_connector = (connector) p2p_soi.swapInf (connector.infid);
      _p2p_comm = (comm) p2p_soi.soi.getUncountedInf (comm.infid);

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
        ProtStack stack = new ProtStack (_p2p_comm.getProtocolStack());
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
     return null;
  }

  public interfaces getCommInfs()
  {
    return COMM_INFS;
  }

  // muxConnector interface methods
  public connection connectOnPort (int remotePort, String remote) throws muxErrors
  {
    connection encaps = null;

    try {

      // make a mux connection
      encaps = _p2p_connector.connect (remote);

      // send a connection request for the port
      Protocol.requestConnection (remotePort, encaps);

      // Create a wrapper and install a callback handler to receive the reply
      // to the connection request. The handler will read the reply, and make
      // the required invocation to the wrapper's muxFinished method. The
      // handler will place the results in a ConnRequestInfo object.

      ConnectionWrapper wrapper = _wrapper_fact.create (encaps);
      ConnRequestInfo cinfo = new ConnRequestInfo (wrapper);

      g.opaque_pointer wrapper_cookie = new g.opaque_pointer();
      wrapper_cookie.pointer = cinfo;

      msgCB cb = (msgCB) this.getCBInf (msgCB.infid);
      wrapper.installMuxCallback (cb, wrapper_cookie);

      // wait for a reply to the connection request, and process the result
      muxErrors exc = cinfo.waitResult();

      if (exc == null) { // Ok
        return (connection) wrapper.sneaky_soi.swapInf (connection.infid);
      }
      else             // not OK
        throw exc;
    }
    catch (commErrors_illegalAddress exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUGPLUS, exc);
      if (encaps != null)
        encaps.closeConn();
      throw new muxErrors_illegalAddress();
    }
    catch (commErrors_peerRefused exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUGPLUS, exc);
      if (encaps != null)
        encaps.closeConn();
      throw new muxErrors_peerRefused();
    }
    catch (commErrors_comm exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUGPLUS, exc);
      if (encaps != null)
        encaps.closeConn();
      throw new muxErrors_comm();
    }
    catch (muxErrors exc) {
      if (encaps != null)
        encaps.closeConn();
      throw exc;
    }
    catch (Exception exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
      if (encaps != null)
        encaps.closeConn();
      throw new muxErrors_misc();
    }
  }

  public void asyncConnectOnPort (int remotePort, String remote,
               connectorCB cb, g.opaque user)
  {
    throw new NotImplementedException();
  }

  // msgCB interface methods

  /**
   * Callback handler. Handles the reply to a connection request, and calls
   * the wrapper's muxFinished method. Leaves the results in the
   * ConnRequestInfo.
   *
   * @param cookie	contains a ConnRequestInfo object
   */
  public void receiveStopped (g.opaque cookie)
  {
    g.opaque_pointer pcookie = (g.opaque_pointer) cookie;
    ConnRequestInfo cinfo = (ConnRequestInfo) pcookie.pointer;

    ConnectionWrapper wrapper = cinfo.getWrapper();
    wrapper.muxFinished(); // before passing a result to another thread

    // a mux connection has been closed
    cinfo.passErrorResult (new muxErrors_peerRefused());
  }


  /**
   * Callback handler. Handles the reply to a connection request, and calls
   * the wrapper's muxFinished method. Leaves the results in the
   * ConnRequestInfo.
   *
   * @param cookie	contains a ConnRequestInfo object
   */
  public void msgReceived (g.opaque cookie)
  {
    g.opaque_pointer pcookie = (g.opaque_pointer) cookie;
    ConnRequestInfo cinfo = (ConnRequestInfo) pcookie.pointer;

    ConnectionWrapper wrapper = cinfo.getWrapper();
    wrapper.muxFinished(); // before passing a result to another thread

    try {
      // see if the connection was granted or refused

      connection conn = (connection) wrapper.sneaky_soi.getUncountedInf (
      							connection.infid);
      if (Protocol.receiveConnectionResponse (conn))
        cinfo.passOkResult ();
      else
        cinfo.passErrorResult (new muxErrors_peerRefused());
    }
    catch (muxErrors exc) {
      cinfo.passErrorResult (exc);
    }
    catch (Exception exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
      cinfo.passErrorResult (new muxErrors_misc());
    }
  }

  /**
   * A ConnRequestInfo is used to pass information from a thread making a
   * connection request (connect()) to the callback handler thread, and
   * vice versa. A requesting thread passes a ConnectionWrapper to the handler
   * thread. The handler thread passes the results of the connection request to
   * the requesting thread.
   * <p>
   * The ConnRequestInfo object is used as a condition variable: the
   * requesting thread must wait for the results to be filled in by the
   * callback handler thread.
   */
  private class ConnRequestInfo
  {
    /** The wrapper passed by the requesting thread to the handler thread. */
    private ConnectionWrapper _wrapper;

    private boolean _done = false;
    private muxErrors _exc;

    /**
     * Constructed by the requesting thread.
     *
     * @param wrapper  the wrapper to be passed to the handler thread.
     */
    public ConnRequestInfo (ConnectionWrapper wrapper)
    {
      _wrapper = wrapper;
    }

    /**
     * Called by the handler thread. Returns the wrapper passed by the
     * requesting thread.
     */
    public ConnectionWrapper getWrapper()
    {
      return _wrapper;
    }

    /** Called by the handler thread. Makes an 'OK' result available. */
    public synchronized void passOkResult()
    {
      _done = true;
      _exc = null;
      this.notify();
    }

    /** Called by the handler thread. Makes an error result available. */
    public synchronized void passErrorResult (muxErrors exc)
    {
      _done = true;
      _exc = exc;
      this.notify();
    }

    /**
     * Called by the requesting thread. Waits for the result to become
     * available.
     *
     * @return		null, if the result is 'OK', an exception if the
     *			the result is an error
     */ 
    public synchronized muxErrors waitResult()
    {
      while (! _done)
	try { this.wait(); }
	catch (InterruptedException exc) {
	  System.err.println ("ignored interruption");
	}
      return _exc;
    }
  }
}

