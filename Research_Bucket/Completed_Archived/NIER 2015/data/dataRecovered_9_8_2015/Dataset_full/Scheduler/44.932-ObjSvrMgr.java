/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.objsvr.mgr;

import vu.globe.rts.java.*;
import vu.globe.idlsys.g;
import vu.globe.rts.std.idl.stdInf.*;         // stdInf.idl

import vu.globe.rts.runtime.ns.nsConst;
import vu.globe.rts.runtime.ns.idl.*;        // ns.idl
import vu.globe.rts.runtime.ns.nameSpaceImp.nameSpaceImp;

import vu.globe.svcs.objsvr.main.ObjectServerMain;
import vu.globe.svcs.objsvr.skels.ObjSvrMgrSkel;
import vu.globe.svcs.objsvr.types.ObjSvrAddr;
import vu.globe.svcs.objsvr.idl.management.*;   // management.idl

import vu.globe.svcs.objsvr.idl.persistence.*;  // persistence.idl
import vu.globe.svcs.objsvr.idl.resource.*;     // resource.idl
import vu.globe.svcs.objsvr.idl.resource;       // resource.idl
  
import vu.globe.svcs.gls.idl.lsClient.*;          // lsClient.idl
import vu.globe.util.comm.*;
import vu.globe.util.comm.idl.rawData.*;
import vu.globe.rts.comm.gwrap.P2PDefs;
import vu.globe.rts.comm.tcp.TcpAddress;
import vu.globe.util.exc.*;
import vu.globe.util.debug.DebugOutput;

import java.net.ProtocolException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import vu.globe.rts.java.*;
import vu.globe.rts.runtime.cfg.idl.rtconfig.*;  // rtconfig.idl

import vu.globe.rts.runtime.ns.idl.ns.*;       	// ns.idl
import vu.globe.rts.runtime.ns.idl.nsTypes.*;   // nsTypes.idl
import vu.globe.rts.runtime.ns.idl.context;

import vu.globe.util.misc.MoveObjectServer; // HACK TO TRANSFER OBJECT SERVER

import vu.globe.rts.security.*;
import vu.globe.rts.security.idl.secconfig.*;

import java.util.*;

/**
 * The object server manager is intended to function as a persistent object. It
 * must be registered with the Persistence Manager before any of its
 * <code>objectServerOps</code> methods are invoked. Also, it must never be
 * unregistered.
 * <p>
 * The object server manager is a composite Globe object. It consists of an
 * <code>ObjSvrMgr</code>, a <code>MgrOperations</code> object and a
 * <code>RemoteOperations</code>. An <code>ObjSvrMgr</code> is responsible for
 * making the composition. It delegates methods in the
 * <code>objectServerOps</code> interface to the <code>MgrOperations</code>
 * object. <code>RemoteOperations</code> allows access to the object server by
 * remote clients.
 *
 * @author Patrick Verkaik
 */

/*
  Persistence notes
  =================

  The object server manager is a persistent object. Most activities related to
  persistence are coordinated by ObjSvrMgr, but are often delegated to
  MgrOperations.

  Thread safety notes
  ===================

  Passivation
  -----------

  A persistent object is not passivated until the last of its local clients
  have released its interfaces. OSM passivation begins with closing
  incoming communication, and allowing the last remote operations to complete.
  It follows that during the remainder of passivation no objectServerOps
  operations (local or remote) can be in progress. Passivation need therefore
  not be thread safe with respect to these operations.

  Activation
  ----------

  The interfaces of a persistent object are not made available to other local
  objects until activation of the PO has finished. Remote communication is not
  accepted until the very end of OSM activation. It follows that during the
  activation no objectServerOps operations (local or remote) can be in
  progress. Activation need therefore not be thread safe with respect to these
  operations.

  Becoming Persistent
  ------------------

  The OSM requires of its local clients that it is registered with the PM
  before its objectServerOps operations are invoked. bePersistent() need
  therefore not be thread safe with respect to these operations.

  Becoming Transient
  ------------------

  This is not implemented by the OSM.

  Destruction
  -----------

  A persistent object cannot be destroyed. Since becoming transient has not
  been implemented, and since transience is a prerequisite for destruction,
  destruction of the OSM is impossible.

  ObjectServerOps Operations
  ----------------------------

  All objectServerOps methods are delegated to the _mgr_ops object, which is
  thread-safe.
*/

public class ObjSvrMgr extends ObjSvrMgrSkel 
{
  /** When activating from a crash, whether to write recovered passivation
      state as regular passivation state to disk. That is, whether to
      synchronize the regular passivation state on disk with the state that
      was saved during the last passivation checkpoint.
      If set to false, the regular passivation state will be written to disk
      when the object server is shut down.
  */
  private static final boolean SYNC_RECOVERED_STATE_TO_DISK = false;

  /**
   * The object which carries out the objectServerOps operations. Set only
   * while the OSM is persistent and active.
   */
  private MgrOperations _mgr_ops;
  
  /**
   * The object which accepts and carries out incoming remote operations. Set
   * only while the OSM is persistent and active.
   */
  private RemoteOperations _remote_ops;

  /** with the security framework the OSM gets 2 contact points: one where
   * owners and server administrators (generally people) can connect to,
   * and one where objects etc. can connect to. The first uses identity
   * certificates, the latter rights certificates.
   * Make sure that the numbers are numbered starting from 0. We need this so we
   * can use it as array index.
   */
  public static final int SECURE_IDENT_CP = 0;
  public static final int SECURE_RIGHTS_CP = 1;
  public static final int N_CPs = 2;

  /** Resource id of the object server manager object. */
  private long _my_rid = resource.invalidResourceID;

  /** Resource id of the osm's contact point. */
  private long[] _cp_rid = null;

  /** The PM's 'persistent object manager' interface. */
  private perstObjectManager _pm;                // counted

  /** The PM's 'persistent resource manager' interface. */
  private perstResourceManager _perst_rsc_mgr;   // uncounted

  /** The PM's 'contact point manager' interface. */
  private contactManager _contact_mgr;           // uncounted

  /** The PM's 'activation record' interface. */
  private activationRecord _perst_activation;    // uncounted

  /** The PM's 'storage manager' interface. */
  private storageManager _storage_mgr;           // uncounted

  /** The PM's 'timer manager' interface. */
  private timerResourceManager _timer_mgr;      // uncounted

  /** The location service resolver. */
  private idManager _ls;  // counted


  /** List of timer task that we started, timerID -> lrID pairs */
  private Map _timerToLRID;

  /**
   * If set, the OSM's passivation should not cause any wide area communication
   * by local representatives.
   */
  private boolean _passivate_quickly = false;

  /**
   * A local name under which RemoteOperations may install a communication
   * object.
   */
  private static final String COMM_LNAME = "comm";

  // SECURITY++
  /**
   * The string representation of the osm contact point.
   */
  private String[] 	_osmContactPoint; // needed by MoveObjectServer


  // GObject method overrides

  protected void initState() throws SOIerrors
  {
    _osmContactPoint = new String[ N_CPs ];
    _cp_rid = new long[ N_CPs ];

    for (int i=0; i<N_CPs; i++)
    {
      _cp_rid[i] = resource.invalidResourceID;
    }

    // get persistence manager
    try {
      int ctx = getContext();
      SOInf pm_soi = nameSpaceImp.getLNS().bind (ctx, nsConst.PERST_MGR_NAME);
      _pm = (perstObjectManager) pm_soi.swapInf (perstObjectManager.infid);
      _perst_rsc_mgr = (perstResourceManager) pm_soi.getUncountedInf (
      					perstResourceManager.infid);
      _contact_mgr = (contactManager) pm_soi.getUncountedInf (
      					contactManager.infid);
      _perst_activation = (activationRecord)pm_soi.getUncountedInf (
      					activationRecord.infid);
      _storage_mgr = (storageManager) pm_soi.getUncountedInf (
				storageManager.infid);
      _timer_mgr = (timerResourceManager) pm_soi.getUncountedInf(
                                timerResourceManager.infid);

      SOInf ls_soi = lns.bind (ctx, nsConst.LS_CLIENT_NAME);
      _ls = (idManager) ls_soi.swapInf (idManager.infid);
    }
    catch (SOIerrors exc) {
      throw exc;
    }
    catch (Exception exc) {
      exc.printStackTrace();
      throw new SOIerrors_misc();
    }

    _timerToLRID = Collections.synchronizedMap(new HashMap());
  }

  protected void cleanup() throws vu.globe.rts.std.idl.stdInf.SOIerrors
  {
    // Cleanup any LRs that are scheduled to be removed because we
    // don't want them in our passivation state
    // Clear any timers that we might have set
    if (!_timerToLRID.isEmpty()) {
        Set s = _timerToLRID.keySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Long timerID = (Long) it.next();
            try {
                _timer_mgr.cancel(timerID.longValue());
                removeLR(((Long) _timerToLRID.get(timerID)).longValue());
            } catch (Exception e) {
                // Ignore
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                            "Ignoring execption cancellation timers");
            }
        }
    }
    _timerToLRID.clear();
          
    // if we're persistent then passivate
    if (_my_rid != resource.invalidResourceID) {
      try { 
         passivate();
      } catch (Exception exc) {
        DebugOutput.println (DebugOutput.DBG_NORMAL,
                            "Ignoring exception during passivation of object"+
	                    " server manager: " + exc);
	exc.printStackTrace();
      }
    }
    else {
      // Note: this situation can only occur if the OSM is released before
      // having been registered with the PM. In that case the OSM has not been
      // operational yet, and no state can be lost.
    }

    // _remote_ops uses _mgr_ops, so first release _remote_ops
    if (_remote_ops != null) {
      _remote_ops.removeClient();
      _remote_ops = null;
    }
    if (_mgr_ops != null) {
      _mgr_ops.removeClient();
      _mgr_ops = null;
    }
    GInterface.RelInf (_pm);
    GInterface.RelInf (_ls);

    super.cleanup();
  }
  
  // objectServerOps interface

  public long bind (g.opaque ohandle, String oname, rawDef jar,
                short /* g.bool */ persistent) throws Exception
  {
    try {
      // Retrieve the IDLMgrConfig from the jar, no cert[] so no check
      IDLLRMgrConfig cfg = LRSecUtil.fromJar(jar, ohandle, null, false, 
                                             ObjectServerMain.securityEnabled);
      return _mgr_ops.bind (ohandle, oname, jar, cfg, persistent);
    }
    catch (NullPointerException exc) {

      if (_mgr_ops == null)
        throw new IllegalStateException ("osm used while still transient");
      else
        throw exc;
    }
  }

  public long addBinding (g.opaque ohandle, String oname,
        rawDef jar, short /* g.bool */ persistent)
        throws Exception
  {
    try {
      // Not Cert[] so no Check
      IDLLRMgrConfig cfg = LRSecUtil.fromJar(jar, ohandle, null, false,
                                             ObjectServerMain.securityEnabled);
      return _mgr_ops.addBinding(ohandle, oname, jar, cfg, persistent);
    }
    catch (NullPointerException exc) {

      if (_mgr_ops == null)
        throw new IllegalStateException ("osm used while still transient");
      else
        throw exc;
    }
  }
 
  public long createLR (g.opaque ohandle, String oname, rawDef jar,
                short /* g.bool */ persistent) throws Exception
  {
    try {
      // No cert[] so no check
      IDLLRMgrConfig cfg = LRSecUtil.fromJar(jar, ohandle, null, false,
                                             ObjectServerMain.securityEnabled);
      return _mgr_ops.createLR (ohandle, oname, jar, cfg, persistent);
    }
    catch (NullPointerException exc) {

      if (_mgr_ops == null)
        throw new IllegalStateException ("osm used while still transient");
      else
        throw exc;
    }
  }

  public void removeLR (long lrid) throws Exception
  {
    try {
      _mgr_ops.removeLR (lrid);
    }
    catch (NullPointerException exc) {

      if (_mgr_ops == null)
        throw new IllegalStateException ("osm used while still transient");
      else
        throw exc;
    }
  }

  public void removeLRLater (long lrid, long afterMillis) throws Exception {

    // Schedule this LR to be removed afterMillis. This timertask will then be
    // cancelled.
    long timer = 0;
    timerResourceManagerCB myTimerRscCB = (timerResourceManagerCB)
                                super.getCBInf(timerResourceManagerCB.infid);
    timer = _timer_mgr.schedule(myTimerRscCB, null, 0, afterMillis);
    _timerToLRID.put(new Long(timer), new Long(lrid));
  }

  public void unbindDSO(g.opaque ohandle) throws Exception
  {
    try {
      _mgr_ops.unbindDSO(ohandle);
    }
    catch (NullPointerException exc) {

      if (_mgr_ops == null)
        throw new IllegalStateException ("osm used while still transient");
      else
        throw exc;
    }
  }

  public localReprIDList listAll() throws Exception
  {
    try {
      return _mgr_ops.listAll();
    }
    catch (NullPointerException exc) {

      if (_mgr_ops == null)
        throw new IllegalStateException ("osm used while still transient");
      else
        throw exc;
    }
  }

  public localReprIDList listDSO(g.opaque ohandle) throws Exception
  {
    try {
      return _mgr_ops.listDSO(ohandle);
    }
    catch (NullPointerException exc) {

      if (_mgr_ops == null)
        throw new IllegalStateException ("osm used while still transient");
      else
        throw exc;
    }
  }

    public localReprIDList listOwned() throws Exception
    {
        try
        {
            return _mgr_ops.listOwned( null, null );
        }
        catch (NullPointerException exc)
        {

            if (_mgr_ops == null)
                throw new IllegalStateException("osm used while still transient");
            else
                throw exc;
        }
    }



  public localReprStat statLR(long lrid) throws Exception
  {
    try {
      return _mgr_ops.statLR(lrid);
    }
    catch (NullPointerException exc) {

      if (_mgr_ops == null)
        throw new IllegalStateException ("osm used while still transient");
      else
        throw exc;
    }
  }

  // persistentObject interface
  public long getPersistenceID()
  {
    return _my_rid;
  }
 
  // managedPerstObject interface

  /** Called by the PM, as part of the creation of a persistent object. */
  public void bePersistent (long rid) throws perstError_misc
  {
    _my_rid = rid;
    _mgr_ops = new MgrOperations (getContext(), rid, _pm, _ls);

    _remote_ops = new RemoteOperations (nameSpaceImp.getLNS(), getContext(),
    				COMM_LNAME, _mgr_ops, _ls);

    try
    {
       for (int i=0; i< N_CPs; i++)
       {
          _cp_rid[i] = _contact_mgr.allocateContact(P2PDefs.TCP_MUX_SEC_STACK);
          _perst_rsc_mgr.makePersistent( rid, _cp_rid[i] );
          String contact = _contact_mgr.getAddress( _cp_rid[i] );
          DebugOutput.println(DebugOutput.DBG_DEBUG,
                              "getting address for contact point "+i+" = "+
                              contact );

          _osmContactPoint[i] = _remote_ops.open(i, contact );
        }
    }
    catch (Exception exc)
    {
      exc.printStackTrace();
      throw new perstError_misc();
    }
  }

  /** Called by the PM, as part of the destruction of a persistent object. */
  public void beTransient()
  {
    throw new NotImplementedException();
  }

  public void prepareImmediatePassivation() throws Exception
  {
    savePerstState();
    _passivate_quickly = true;
  }


  public void activate (long rid) throws Exception
  {
    _my_rid = rid;
    _mgr_ops = new MgrOperations (getContext(), rid, _pm, _ls);
    _remote_ops = new RemoteOperations (nameSpaceImp.getLNS(), getContext(),
    				COMM_LNAME, _mgr_ops, _ls);

    // restore state and contact point
    long state_id = _perst_activation.getState (_my_rid);
    storage storage_obj = _storage_mgr.openStorage (state_id);

    // unmarshall state
    try {
      RawCursor pkt = new RawCursor (storage_obj.read (-1)); // read entirely
      unmarshallState(pkt);
    }
    finally {
      storage_obj.close();
      storage_obj.relInf();
    }

    // delay incoming remote operations until the very last
    openRemoteOps();
  }

  public void activateFromCrash (long rid, rawDef state) throws Exception
  {
    DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "mgr: recovery mode - activating from crash...");

    _my_rid = rid;
    _mgr_ops = new MgrOperations (getContext(), rid, _pm, _ls);
    _remote_ops = new RemoteOperations (nameSpaceImp.getLNS(), getContext(),
    				COMM_LNAME, _mgr_ops, _ls);

    if (SYNC_RECOVERED_STATE_TO_DISK) {
      DebugOutput.println(DebugOutput.DBG_DEBUG,
                          "mgr: recovery mode - syncing passivation state");

      writePerstStateToStorage(state);
    }

    unmarshallState(new RawCursor(state));

    // delay incoming remote operations until the very last
    openRemoteOps();

    DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
                        "mgr: recovery mode - activated");
  }

  public void preparePassivationCheckpoint() throws Exception
  {
    DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "mgr: passivation checkpoint - preparing...");

    _remote_ops.pause(true);
  }

  public passivationState passivationCheckpoint() throws Exception
  {
    DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "mgr: passivation checkpoint - collecting state...");

    /*
     * Marshall passivation state.
     */

    // set impl handle and a null init string
    _perst_activation.setRecreateInfo (_my_rid,
       "JAVA;vu.globe.svcs.objsvr.mgr.ObjSvrMgrCO", null, null);

    RawCursor pkt = marshallState();

    passivationState psState = new passivationState();
    psState.perst_id = _my_rid;
    psState.state = pkt.getRaw();

    return psState;
  }

  public void completePassivationCheckpoint() throws Exception
  {
    DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "mgr: passivation checkpoint - completing...");

    _remote_ops.pause(false);
  }

  // configurable interface

  /**
   * Called by the PM, during activation.
   */
  public void configure(String data)
  {
    // passivate() set the init data to null
    if (data != null)
      throw new AssertionFailedException();
  }

  // other methods

  /**
   * `Open' the remote operations.
   */
  private void openRemoteOps()
    throws perstError_misc
  {
    try {
      for (int i=0; i<N_CPs; i++)
      {
        String contact = _contact_mgr.getAddress( _cp_rid[i] );
        _remote_ops.open( i, contact );                 // SECURITY
      }  
    }
    catch (Exception exc) {
      exc.printStackTrace();
      throw new perstError_misc();
    }
  }

  /**
   * Passivates the object server manager, and releases all LRs. 
   */
  private void passivate() throws Exception
  {
    try {
      // close comm and wait for current remote ops to finish
      _remote_ops.close();
      savePerstState();
      _mgr_ops.releaseLRs (_passivate_quickly);
    }
    finally {
      _perst_activation.informPassivated (_my_rid);
    }
  }

  /**
   * Saves the implementation handle, initialisation string and persistent
   * state.
   */
  private void savePerstState() throws Exception
  {
    // set impl handle and a null init string
    _perst_activation.setRecreateInfo (_my_rid,
    	"JAVA;vu.globe.svcs.objsvr.mgr.ObjSvrMgrCO", null, null);

    RawCursor pkt = marshallState();

    writePerstStateToStorage(pkt.getRaw());
  }

  /**
   * Marshalls the state and returns it.
   */
  private RawCursor marshallState()
    throws Exception
  {
    // marshall state
    // CHECKP>>
    // Create raw with preallocated buffer, just to speed checkpointing
    // up. Size of buffer not important, will work with any size, just slower.
    // (Arno, 2002-10-07)
    //
    int n_lrs = _mgr_ops.numberOfLRs();
    RawCursor pkt = new RawCursor( RawOps.createRaw( 15, n_lrs*5000 ));
    _mgr_ops.marshall (pkt); // thread-safe

    // marshall contact points rid
    for (int i=0; i<N_CPs; i++)
    {
      RawBasic.writeInt64( pkt, _cp_rid[i] );
      RawBasic.writeUnicode(pkt, _osmContactPoint[i] );
    }
    
    return pkt;
  }

  private void unmarshallState(RawCursor pkt)
    throws perstError_io
  {
    try {
      _mgr_ops.unmarshall (pkt);
       
      for (int i=0; i<N_CPs; i++)
      {
        // unmarshall contact point rid
        _cp_rid[i] = RawBasic.readInt64(pkt);
        _osmContactPoint[i] = RawBasic.readUnicode(pkt);

        // HACK TO TRANSFER OBJECT SERVER
        _osmContactPoint[i] = MoveObjectServer.translateMyIPAddress(
                                          _osmContactPoint[i], "%%% GOS MGR: ");
      }
    }
    catch (ProtocolException exc) {
      exc.printStackTrace();
      throw new perstError_io();
    }
  }

  /**
   * Writes the given state to persistent storage.
   */
  private void writePerstStateToStorage(rawDef state)
    throws Exception
  {
    long state_id = _perst_activation.getState (_my_rid);
    storage storage_obj = _storage_mgr.openStorage (state_id);

    try {
      // overwrite entirely
      storage_obj.setLength (0);
      storage_obj.write (state);
    }
    finally {
      storage_obj.close();
      storage_obj.relInf();
    }
  }

  // TimerResourceManager interface
  public void run(long timer, g.opaque user) throws Exception {
        // remove the lr_id
        Long timerID = new Long(timer);
        removeLR(((Long) _timerToLRID.get(timerID)).longValue());
        _timerToLRID.remove(timerID);
  }
      
  public void cancel(long timer, g.opaque user) throws Exception {
        Long timerID = new Long(timer);
        _timerToLRID.remove(timerID);
  }
}
