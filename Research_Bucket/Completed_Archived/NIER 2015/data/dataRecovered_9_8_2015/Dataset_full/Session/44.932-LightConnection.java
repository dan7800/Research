/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.comm.muxconn;

import vu.globe.rts.comm.skels.connectionSkel;

import vu.globe.rts.comm.idl.p2p.*;     // p2p.idl
import vu.globe.rts.comm.idl.mux.*;     // mux.idl
import vu.globe.util.comm.idl.rawData.*; // rawData.idl
import vu.globe.idlsys.g;       // g.idl
import vu.globe.rts.std.idl.stdInf.*;   // stdInf.idl
import vu.globe.rts.java.GInterface;
import vu.globe.util.debug.DebugOutput;

/**
 * The light-weight connection object created by MuxConnectionOriented. This is
   a Globe object without a class object.
 *
 * @author Patrick Verkaik
 */

/*
  No Class Object
  ---------------

  See TcpConnection.java.

*/

class LightConnection extends connectionSkel
{
  /**
   * The multiplexer object that created this light-weight connection.
   */
  private final MuxConnectionOriented _mux;

  /** Counted Globe reference to the multiplexer object, to keep it alive. */
  private final SOInf _mux_soi;

  /** The local connection port on which this connection is based. */
  private final int _port;

  /**
   * Set when the user invokes closeConn(). When set, _port should no longer be
   * be used, since the port value may be deallocated, even recycled, by _mux.
   * Once true, never becomes false.
   */
  private boolean _closing = false;


  /** The communication interfaces implemented by this object. */
  private static interfaces COMM_INFS;

  static {
    COMM_INFS = new interfaces (3);

    COMM_INFS.v[0] = comm.infid;
    COMM_INFS.v[1] = connection.infid;
    COMM_INFS.v[2] = msg.infid;
  }


  /**
   * Constructs a light-weight connection.
   */
  public LightConnection (MuxConnectionOriented mux, SOInf mux_soi, int port)
  {
    _mux = mux;
    _mux_soi = (SOInf) mux_soi.dupInf();
    _port = port;
  }

  protected void cleanup() throws vu.globe.rts.std.idl.stdInf.SOIerrors
  {
    GInterface.RelInf (_mux_soi);
    super.cleanup();
  }


  // comm interface methods

  public String getProtocolID()
  {
    return _mux.getProtocolID();
  }

  public String getProtocolStack()
  {
    return _mux.getProtocolStack();
  }

  public String getContact()
  {
    return null;
  }

  public interfaces getCommInfs()
  {
    return COMM_INFS;
  }

  // connection interface methods

  public String getLocal()
  {
    // note: closeConn() ought not to be called concurrently
    if (! _closing) {
      try {
        return _mux.getLocalOnPort (_port);
      }
      catch (muxErrors exc) {
	DebugOutput.printException (exc);
        throw new RuntimeException();
      }
    } 
    else {
      return "closed=>unavailable";
    }
  }

  public String getRemote()
  {
    // note: closeConn() ought not to be called concurrently
    if (! _closing) {
      try {
        return _mux.getRemoteOnPort (_port);
      }
      catch (muxErrors exc) {
	DebugOutput.printException (exc);
        throw new RuntimeException();
      }
    } 
    else {
      return "closed=>unavailable";
    }
  }

  public void closeConn()
  {
    // note: closeConn() ought not to be called concurrently
    if (_closing) {
      return;
    }
    _closing = true;
    try {
      _mux.closeConnOnPort (_port);
    }
    catch (muxErrors exc) {
      DebugOutput.printException (exc);
    }
  }

  // msg interface methods

  public void regMsgCB (msgCB client_cb, g.opaque client_cookie)
    throws commErrors
  {
    // note: closeConn() ought not to be called concurrently
    if (! _closing) {
      try {
        _mux.regMsgCBOnPort(_port, client_cb, client_cookie);
      }
      catch (muxErrors_invOp exc) {
	DebugOutput.printException (exc);
        throw new commErrors_invOp();
      }
      catch (muxErrors_illegalPort exc) {
	DebugOutput.printException (exc);
        throw new commErrors_invOp();
      }
      catch (muxErrors exc) {
	DebugOutput.printException (exc);
        throw new commErrors_misc();
      }
    }
    else {
      throw new commErrors_invOp();
    }
  }

  public void pauseMsgCB(short /* g.bool */ on) throws commErrors
  {
    // note: closeConn() ought not to be called concurrently
    if (! _closing || on == g.bool.False) { // unpause is allowed after close
      try {
        _mux.pauseMsgCBOnPort(_port, on);
      }
      catch (muxErrors_invOp exc) {
	DebugOutput.printException (exc);
        throw new commErrors_invOp();
      }
      catch (muxErrors_illegalPort exc) {
	DebugOutput.printException (exc);
        throw new commErrors_invOp();
      }
      catch (muxErrors exc) {
	DebugOutput.printException (exc);
        throw new commErrors_misc();
      }
    }
    else {
      throw new commErrors_invOp();
    }
  }

  public void send (String remote, rawDef pkt) throws commErrors
  {
    // DebugOutput.println ("[LightConnection.send]");
    // note: closeConn() ought not to be called concurrently
    if (! _closing) {

      try {
        _mux.sendOnPort (_port, -1, null, pkt);
      }
      catch (muxErrors_invOp exc) {
	DebugOutput.printException (exc);
        throw new commErrors_invOp();
      }
      catch (muxErrors_illegalPort exc) {
	DebugOutput.printException (exc);
        throw new commErrors_invOp();
      }
      catch (muxErrors_comm exc) {
        exc.printStackTrace();
	DebugOutput.printException (exc);
        throw new commErrors_comm();
      }
      catch (muxErrors exc) {
	DebugOutput.printException (exc);
        throw new commErrors_misc();
      }
    }
    else {
      throw new commErrors_invOp();
    }
  }

  public msg_receive_Out receive() throws commErrors
  {
    // DebugOutput.println ("[LightConnection.receive]");
    // note: closeConn() ought not to be called concurrently
    if (! _closing) {

      muxMsg_receiveOnPort_Out mux_out;

      try {
        mux_out = _mux.receiveOnPort (_port);
      }
      catch (muxErrors_illegalPort exc) {
	DebugOutput.printException (exc);
        throw new commErrors_invOp();
      }
      catch (muxErrors_comm exc) {
	DebugOutput.printException (exc);
        throw new commErrors_comm();
      }
      catch (muxErrors exc) {
	DebugOutput.printException (exc);
        throw new commErrors_misc();
      }

      msg_receive_Out out = new msg_receive_Out();
      out.remote = mux_out.remote;
      out.retval = mux_out.retval;

      return out;
    }
    else {
      throw new commErrors_invOp();
    }
  }
}
