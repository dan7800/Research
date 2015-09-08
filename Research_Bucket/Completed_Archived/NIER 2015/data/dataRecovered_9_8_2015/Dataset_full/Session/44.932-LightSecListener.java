/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.comm.lwsecconn;

import vu.globe.rts.java.*;
import vu.globe.idlsys.g;
import vu.globe.rts.comm.skels.*;
import vu.globe.rts.comm.idl.p2p.*;       // p2p.idl
import vu.globe.rts.comm.idl.mux.*;       // mux.idl
import vu.globe.rts.comm.idl.mux;         // mux.idl
import vu.globe.rts.comm.idl.comgr.*;     // comgr.idl
import vu.globe.rts.std.idl.stdInf.*;     // stdInf.idl
import vu.globe.rts.std.idl.configure.*;  // configure.idl
import vu.globe.rts.runtime.ns.nsConst;
 
import vu.globe.util.comm.ProtStack;
import vu.globe.util.parse.AttributeString;
import vu.globe.rts.std.StdUtil;
import vu.globe.util.debug.DebugOutput;

/**
 * A light-weight listener communication object. See the README for further
 * info.
 */

/*
  When the listener is configured, we know the protocol stack, implementation
  handle and initialisation string to install a multiplexer. However we cannot
  install a multiplexer yet, because there is some missing information: the
  contact point that the multiplexer must be bound to. This is not known until
  exportContact has been called. Therefore it is not until exportContact that
  the multiplexer is created.

  Many of the calls simply delegate to the multiplexer. Similarly, the
  multiplexer's callbacks are delegated to the client (by registering the
  client's callback interface directly with the multiplexer).

  Thread safety isn't much of an issue. A couple of thread-unsafe checks for
  illegal calls exist (e.g. calling exportContact twice). However,
  regMuxListenCB has to be synchronized, to allow an exportContact call
  concurrent with a regListenCB call.
*/

public class LightSecListener extends cfgListenerSkel
{
  /**
   * The multiplexer's implementation handle with which this object is
   * configured.
   */
  private String _mux_impl;

  /**
   * The multiplexer's initialisation string with which this object is
   * configured.
   */
  private String _mux_init;

  /** The multiplexer's protocol stack with which this object is configured. */
  private String _mux_stack;

  /** The multiplexer's protocol id derived from _mux_stack. */
  private String _mux_prot_id;

  private String _sec_prot_id;
  
  /** A multiplexer interface. Valid when exportContact() has been invoked.  */
  private comm _mux_comm;                        // counted

  /** A multiplexer interface. Valid when exportContact() has been invoked.  */
  private muxListener _mux_listener;             // uncounted

  /** A multiplexer interface. Valid when exportContact() has been invoked. */
  private contactExporter _mux_p2p_exporter;     // uncounted

  /** A multiplexer interface. Valid when exportContact() has been invoked. */
  private muxContactExporter _mux_mux_exporter;  // uncounted

  /**
   * The contact point of this object. Null until exportContact() has been
   * invoked.
   */
  private String _contact_point;

  /**
   * The multiplexer port at which this object is listening. Valid when
   * exportContact() has been invoked.
   */
  private int _port = mux.NoPort;

  private String _secType;

  /** The client's registered callback interface. */
  private listenCB _client_cb;

  /** The client's cookie that goes with _client_cb. */
  private g.opaque _client_cookie;

  /** Whether the client has called exportContact(). */
  private boolean _exported_called = false;
  
  /** Whether the client has called regListenCB(). */
  private boolean _reg_listen_called = false;

  /** Whether we have called the mux's regListenCB(). */
  private boolean _mux_listening = false;

  /** The shared communication object manager. */
  private commObjMgr _comgr;               // counted

  /**
   * The name under which the multiplexer object is registered in the local
   * name space.
   */
  private static final String MUX_NAME = "mux";

  /**
   * The number of times we will attempt to install a multiplexer, before
   * giving up.
   */
  private static final int MAX_MUX_TRIES = 3;

  /** The communication interfaces implemented by this object. */
  private static final interfaces COMM_INFS;

  static {
    COMM_INFS = new interfaces (3);

    COMM_INFS.v[0] = comm.infid;
    COMM_INFS.v[1] = listener.infid;
    COMM_INFS.v[2] = contactExporter.infid;
  }

  /** The communication interfaces the multiplexer is expected to implement. */
  private static final interfaces MUX_INFS;

  static {
    MUX_INFS = new interfaces (3);

    MUX_INFS.v[0] = muxListener.infid;
    MUX_INFS.v[1] = contactExporter.infid;
    MUX_INFS.v[2] = muxContactExporter.infid;
  }

  protected void cleanup() throws vu.globe.rts.std.idl.stdInf.SOIerrors
  {
    GInterface.RelInf (_mux_comm);
    GInterface.RelInf (_comgr);
    super.cleanup();
  }

  // configurable interface
 
  public void configure (String config_data) throws configureErrors
  {
    AttributeString cfg;
    try {
      cfg = new AttributeString (config_data);
      _mux_impl = cfg.get (LightSecConfig.MUX_IMPLEMENTATION);
      _mux_init = cfg.get (LightSecConfig.MUX_INITIALISATION);
      _mux_stack = cfg.get (LightSecConfig.PROT_STACK);

      ProtStack p = new ProtStack (_mux_stack);
      _sec_prot_id = p.remove();
      _secType = cfg.get (LightSecConfig.SEC_PROT_STACK);
      _mux_prot_id = p.remove();
    }
    catch (IllegalArgumentException exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
      throw new configureErrors_invArg();
    }
    catch (ProtStack.IllegalStackException exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
      throw new configureErrors_invArg();
    }
    if (_mux_impl == null || _mux_init == null || _mux_stack == null ||
        _mux_prot_id == null)
    {
      throw new configureErrors_invArg();
    }

    if (_sec_prot_id == null) {
        throw new configureErrors_invArg();
    }
    
    // get the shared communication object manager
    try {
      SOInf soi = lns.bind (getContext(), nsConst.SHDCOMM_MGR_NAME);
      _comgr = (commObjMgr) soi.swapInf (commObjMgr.infid);
    }
    catch (Exception exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
      throw new configureErrors_misc();
    }
  }

  // comm interface methods

  public String getProtocolID()
  {
    return _mux_prot_id;
  }

  public String getProtocolStack()
  {
    return _mux_stack;
  }

  public String getContact()
  {
    return _contact_point; // possibly null
  }

  public interfaces getCommInfs()
  {
    return COMM_INFS;
  }

  // contactExporter interface methods
  
  public void exportContact (String contact) throws commErrors
  {
    if (_exported_called) // thread-unsafe check
      throw new commErrors_invOp();

    _exported_called = true;

    // parse the specified contact point (if any) -> base_address / _port
    String base_address = null;
    if (contact != null) {
      LightSecAddress la = new LightSecAddress (contact, _mux_prot_id,
                                                _sec_prot_id);
      base_address = la.getBaseAddress();
      _port = la.getPort();
      _secType = la.getSecType();
    }

    // Obtain the multiplexer and its contact point (base_address). Loop if
    // we get a 'conflict' exception.
    int i;
    for (i = 0; i < MAX_MUX_TRIES; i++) {
      try {
        base_address = getMux (contact == null ? null : base_address);
      }
      catch (commObjMgrErrors_conflict exc) {
        continue;
      }
      break;
    }

    if (i == MAX_MUX_TRIES) // something is probably seriously wrong
      throw new commErrors_misc();

    try {
      // Mux has now been fully created and installed, and its contact point
      // exported. Export a port, set our own contact point (_contact_point).
      if (contact == null) { // pick a port
        _port = _mux_mux_exporter.exportContactOnPort (mux.NoPort);
        _contact_point = new LightSecAddress(base_address, _port, _mux_prot_id,
                                             _sec_prot_id, _secType).
				toString();
      }
      else { // use specified port
        _mux_mux_exporter.exportContactOnPort (_port);
        _contact_point = contact;
      }

      // See if we can start listening
      regMuxListenCB();
    }
    catch (commErrors exc) { // note: exportContact throws commErrors!
      throw exc;
    }
    catch (muxErrors_illegalPort exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUGPLUS, exc);
      throw new commErrors_illegalAddress();
    }
    catch (muxErrors_noContactPoint exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUGPLUS, exc);
      throw new commErrors_noContactPoint();
    }
    catch (Exception exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
      throw new commErrors_misc();
    }
  }

  public void closeContact()
  {
    // Thread-unsafe check. Note that calls are not allowed to follow close,
    // so may not be concurrent either.
    if (_exported_called) {

      try { _mux_mux_exporter.closeContactOnPort (_port); }
      catch (Exception exc) {
        DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
      }
    }
  }

  // listener interface methods

  public void regListenCB (listenCB cb, g.opaque cookie) throws commErrors
  {
    if (_reg_listen_called) // thread-unsafe check
      throw new commErrors_invOp();

    _reg_listen_called = true;

    _client_cb = (listenCB) cb.dupUncountedInf();
    _client_cookie = cookie;

    try {
      regMuxListenCB();
    }
    catch (muxErrors_invOp exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUGPLUS, exc);
      throw new commErrors_invOp();
    }
    catch (muxErrors_illegalPort exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUGPLUS, exc);
      throw new commErrors_invOp(); // this object must have been closed
    }
    catch (Exception exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
      throw new commErrors_misc();
    }
  }

  public void pauseListenCB(short /* g.bool */ on) throws commErrors
  {
    if (! _exported_called) // thread-unsafe check
      throw new commErrors_invOp();

    try {
      _mux_listener.pauseListenCBOnPort(_port, on);
    }
    catch (muxErrors_illegalPort exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUGPLUS, exc);
      throw new commErrors_invOp();
    }
    catch (Exception exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
      throw new commErrors_misc();
    }
  }

  public connection accept() throws commErrors
  {
    if (! _exported_called) // thread-unsafe check
      throw new commErrors_invOp();

    try {
      return _mux_listener.acceptOnPort (_port);
    }
    catch (muxErrors_illegalPort exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUGPLUS, exc);
      throw new commErrors_invOp(); // presumably close() has been called
    }
    catch (muxErrors_comm exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUGPLUS, exc);
      throw new commErrors_comm();
    }
    catch (Exception exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
      throw new commErrors_misc();
    }
  }

  /**
   * If the client has called both regListenCB() and exportContact(), then
   * registers the client's listening callback interface with the multiplexer.
   * Otherwise (or if the client is already registered with the multiplexer),
   * a no-op.
   *
   * @exception muxErrors_illegalPort if _port was unknown
   */
  private synchronized void regMuxListenCB() throws Exception
  {
    if (_exported_called && _reg_listen_called && ! _mux_listening) {
      _mux_listener.regListenCBOnPort (_port, _client_cb, _client_cookie);
      _mux_listening = true;
    }
  }

  /**
   * Tries to find a suitable existing multiplexer in the shared communication
   * object manager. If none is found, creates one, installs it into the shared
   * communication object manager, and gets it to export a contact point.
   * Either way, the _mux... interfaces are all set.
   *
   * @param mux_contact	        the required contact point of the multiplexer,
   				null if any contact point will do
   * @return 			the contact point of the multiplexer
   *
   * @exception commObjMgrErrors_conflict
   *          during this call someone ruined a multiplexer entry created by
   *          us. The caller may try again
   */
  private String getMux (String mux_contact)
  	throws  commObjMgrErrors_conflict, commErrors_noContactPoint,
		commErrors_illegalAddress, commErrors_misc
  {
    // get or create the multiplexer entry in the shared communication object
    // manager
    int entry; boolean created;
    try {
      commObjMgr_requestOrCreateEntry_Out result
      	= _comgr.requestOrCreateEntry (MUX_INFS, _mux_stack, mux_contact);
      entry = result.retval;
      created = result.created == g.bool.True;
    }
    catch (commObjMgrErrors_conflict exc) {
      // Note: a conflict at this point indicates that a contact point was
      // specified, but already occupied by a mux with a different protocol
      // stack or set of interfaces. There is no point in the caller trying
      // again.
      DebugOutput.dassert (mux_contact != null);
      throw new commErrors_noContactPoint();
    }
    catch (Exception exc) {
      DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
      throw new commErrors_misc();
    }

    // If a mux was found get the mux from the entry, otherwise install a new
    // one in the entry and export a contact point. In both cases set mux_comm
    // and the _mux... interfaces.

    comm mux_comm;

    if (! created) {
      // Phew! Someone else did the dirty work.
      try {
        mux_comm = _comgr.getComm (entry);
        setMuxInfs (mux_comm);
      }
      catch (Exception exc) {
        DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
        throw new commErrors_misc();
      }
    }
    else {
      // Oops! We have now got ourselves into an obligation to create the
      // multiplexer. Cancel the entry if anything goes wrong

      try {
        mux_comm = createMux();
        setMuxInfs (mux_comm);

        _mux_p2p_exporter.exportContact (mux_contact);
        _comgr.install (entry, mux_comm);
      }
      catch (commErrors_illegalAddress exc) { // exportContact()
        try { _comgr.cancelInstall (entry); } catch (Exception exc2) {}
	throw exc;
      }
      catch (commErrors_noContactPoint exc) { // exportContact()
        try { _comgr.cancelInstall (entry); } catch (Exception exc2) {}
	throw exc;
      }
      catch (commObjMgrErrors_conflict exc) { // install()
	// Note: a conflict at this point indicates that during this call,
	// someone else installed a multiplexer with the same contact point,
	// possibly with the same protocol stack. The caller should try
	// again.
        try { _comgr.cancelInstall (entry); } catch (Exception exc2) {}
	throw exc;
      }
      catch (Exception exc) {
        try { _comgr.cancelInstall (entry); } catch (Exception exc2) {}
	DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
	throw new commErrors_misc();
      }
    }
    return (mux_contact != null) ? mux_contact : mux_comm.getContact();
  }

  /**
   * Instantiates and configures the multiplexer object with which this
   * light-weight listener was configured.
   *
   * @return		        the multiplexer's comm interface (counted)
   * @exception Exception	an unexpected Globe exception
   */
  private comm createMux() throws Exception
  {
    // instantiate the multiplexer
  
    SOInf co_soi = lns.bind (getContext(), "repository/" + _mux_impl);
    SCInf sci = (SCInf) co_soi.swapInf (SCInf.infid);
    SOInf mux_soi = StdUtil.createGlobeObject (sci, getContext(), MUX_NAME);

    // configure the multiplexer

    configurable cfg_inf = (configurable) mux_soi.getUncountedInf (
						configurable.infid);
    cfg_inf.configure (_mux_init);
    return (comm) mux_soi.swapInf (comm.infid);
  }

  /**
   * Sets the _mux... interfaces.
   *
   * @param mux_comm		the multiplexer's comm interface. This is a
   * 				counted Globe reference stored by this method
   * @exception Exception	an unexpected Globe exception
  */
  private void setMuxInfs (comm mux_comm) throws Exception
  {
    _mux_comm = mux_comm;
    _mux_listener = (muxListener) mux_comm.soi.getUncountedInf (
    			  muxListener.infid);
    _mux_p2p_exporter = (contactExporter) _mux_comm.soi.getUncountedInf (
    			  contactExporter.infid);
    _mux_mux_exporter = (muxContactExporter) _mux_comm.soi.getUncountedInf (
    			  muxContactExporter.infid);
  }
}

