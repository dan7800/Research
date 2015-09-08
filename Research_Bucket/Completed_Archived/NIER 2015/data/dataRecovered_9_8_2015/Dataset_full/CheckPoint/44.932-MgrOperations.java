/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.objsvr.mgr;

import vu.globe.idlsys.g;
import vu.globe.rts.lr.idl.distribution.*;              // distribution.idl
import vu.globe.svcs.gls.idl.lsClient.*;          	// lsClient.idl
import vu.globe.rts.runtime.binding.idl.bind.*;    	// bind.idl
import vu.globe.rts.runtime.dsofact.idl.dsoFact.*; 	// dsoFact.idl

import vu.globe.svcs.objsvr.idl.management.*;      	// management.idl
import vu.globe.svcs.objsvr.idl.persistence.*;  		// persistence.idl
import vu.globe.svcs.objsvr.idl.resource;       		// resource.idl

import vu.globe.svcs.objsvr.types.*;

import vu.globe.rts.runtime.ns.nsConst;
import vu.globe.rts.runtime.ns.idl.*;        		// ns.idl
import vu.globe.rts.runtime.ns.idl.ns.*;     		// ns.idl
import vu.globe.rts.runtime.ns.nameSpaceImp.nameSpaceImp;

import vu.globe.rts.std.idl.stdInf.*;        		// stdInf.idl
import vu.globe.rts.std.StdUtil;

import vu.globe.rts.security.idl.sectypes.*;
import vu.globe.rts.security.idl.secconfig.*;
import vu.globe.rts.security.idl.security.*;
import vu.globe.rts.security.certs.RightsCertUtil;
import vu.globe.rts.security.certs.IdentCertUtil;
import vu.globe.rts.security.LRSecUtil;
import vu.gaia.rts.RTSException;
import java.security.cert.Certificate;
import java.security.KeyStore;

import vu.globe.util.comm.*;
import vu.globe.util.comm.idl.rawData.*;

import vu.globe.util.exc.NotImplementedException;
import vu.globe.util.debug.DebugOutput;

import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.net.ProtocolException;

/**
 * <code>MgrOperations</code> is part of the composite object server manager.
 * It is responsible for implementing the operations in the
 * <code>objectServerOps</code> interface. This is <i>not</i> a Globe object.
 * Thread-safe.
 *
 * @author Patrick Verkaik, Arno Bakker
 */

/*
  Thread safety notes.
  --------------------
 
  First see the thread safety notes in ObjSvrMgr.java.

  Concurrent activities (must be thread-safe):
    objectServerOps
 
  Non-concurrent activities (need not be thread-safe):
    marshalling
    unmarshalling
    releaseLRs()
    initialisation
    clean-up

  Persistence Notes
  -----------------

  The persistent state of this object consists of the allocation state of
  all LR identifiers plus the mapping of persistent LRs in _installed_lrs. The
  mapping of non-persistent LRs in _installed_lrs is *not* part of the
  persistent state of this object.

  The format is:

    'allocation state'   // defined by the table

    int64 n              // the number of persistent LRs
    array [n] of record
      int64 lr_id        // the 'lr id' of the LR
      int64 perst_id     // the persistence id of the LR
    end;
*/

class MgrOperations //used to implement objectServerOps_Inf, can't now due to
// the certificate chain of the peer that needs to be passed on some ops
// for extra checking. ..... implements objectServerOps_Inf
{
  /** The installed LRs. */
  private final LRTable _installed_lrs;

  /** The local name space. */
  private final nameSpace _lns;

  /** The context of the object server manager. */
  private final int _mgr_ctx;

  /** A thread-safe factory of binder objects. */
  private final BinderCreator _binder_creator;

  /** A thread-unsafe (!) factory of LRs. Creates the first LR in a DSO. */
  private final distrFact _lr_fact;      // counted

  /** The location service resolver. */
  private idManager _ls;  // counted

  // persistence

  /** The PM's 'persistent object manager' interface. */
  private final perstObjectManager _pm;  // counted

  /** The persistence id of the object server manager. */
  private final long _my_pid;

  /**
   * Constuctor. When the creator of this object is done, removeClient()
   * should be called.
   *
   * @param mgr_ctx     the context of the object server manager
   * @param my_pid      the persistence id of the object server manager
   * @param pm          a runtime interface of the PM
   * @param ls		a runtime interface of the location service resolver
   */
  public MgrOperations (int mgr_ctx, long my_pid, perstObjectManager pm,
			idManager ls)
  {
    _installed_lrs = new LRTable();
    _lns = nameSpaceImp.getLNS();
    _mgr_ctx = mgr_ctx;
    _my_pid = my_pid;
    _pm = (perstObjectManager) pm.dupInf();
    _ls = (idManager) ls.dupInf();

    try {
      _binder_creator = new BinderCreator (_lns, _mgr_ctx);
      _lr_fact = createLRFactory();
    }
    catch (Exception exc) {
      // unexpected Globe exception
      exc.printStackTrace();
      throw new RuntimeException (exc.getMessage());
    }
  }

  /**
   * Releases any Globe references that are not released by a call to
   * releaseLRs().
   */
  public void removeClient()
  {
    _binder_creator.removeClient();
    _lr_fact.relInf();
    _pm.relInf();
    _ls.relInf();
  }
    
  /**
   * Creates an LR factory.
   *
   * @exception Exception
   *   a Globe exception during instance creation
   */
  private distrFact createLRFactory () throws Exception
  {
    SOInf class_soi = _lns.bind (_mgr_ctx, ":/repository/JAVA;" +
			"vu.globe.rts.runtime.dsofact.DistrFactImplCO");
    SCInf sci = (SCInf) class_soi.swapInf (SCInf.infid);
    SOInf soi = StdUtil.createGlobeObject (sci, _mgr_ctx, "lrFact");
    return (distrFact) soi.swapInf (distrFact.infid);
  }


  // objectServerOps interface (not used anymore).

    public long createLR( g.opaque ohandle,
                          String oname,
                          rawDef jar,
                          IDLLRMgrConfig CFG,
                          short /* g.bool */ persistent )
    throws osvrMgrError
    {
        SOInf soi;
        try
        {
            synchronized (this)
            { // _lr_fact is not thread-safe

                soi = _lr_fact.createByHandle( ohandle, jar, CFG, 
                                        persistent == g.bool.True ? 
                                        _my_pid : resource.invalidResourceID);
            }
        }
        catch (factError_unknownImplementationHandle exc)
        {
            throw new osvrMgrError_unknownImplementationHandle();
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
            throw new osvrMgrError_instanceError();
        }
        // Other errors are just passed on.

        LRIdent lr_id = _installed_lrs.allocateIdentifier();
        long lr_id_num = lr_id.getIdl();

        // Set the lr_id in the LRMgr
        try {
            secSetLRID temp = (secSetLRID) soi.getUncountedInf(secSetLRID.infid);
            temp.setLRID(lr_id_num);
        } catch (Exception e) {
            DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "ERROR: Could not set LRID: "+e.getMessage());
            throw new osvrMgrError();
        }

        contactAddresses caddrs;
        caddrs = getContactAddresses(persistent == g.bool.True, soi, _pm);

        localReprStat status = createStatus( lr_id_num, ohandle, oname,
                                             CFG.tocreatebp, CFG.creds.chain,
                                             CFG.pedchain,
                                             persistent, caddrs);

        LREntry entry=null;
        try
        {
            entry = persistent == g.bool.True ?
                           (LREntry) new PerstLREntry(lr_id, status, soi, _pm)
                           :
                           (LREntry) new TransientLREntry(lr_id, status, soi);
        }
        catch( Exception exc )
        {
            exc.printStackTrace();
            throw new osvrMgrError_misc();
        }
        _installed_lrs.storeLocalRepr(lr_id, entry);
        return lr_id_num;
  }

  public long bind (g.opaque ohandle, String oname, rawDef jar, 
                    IDLLRMgrConfig CFG, short /* g.bool */ persistent)
                    throws osvrMgrError
  {
        SOInf soi;
        try
        {
            binder bindeR = _binder_creator.createBinder();
            if (persistent == g.bool.True)
                bindeR.setPersistence(_my_pid);

            bindeR.bindLRMgrConfig( ohandle, jar, CFG );
            soi = bindeR.getInstance();
            bindeR.relInf();
        }
        catch (bindingError_noAddressesFound exc)
        {
            throw new osvrMgrError_noAddressesFound();
        }
        catch (bindingError_exhausted exc)
        {
            throw new osvrMgrError_exhausted();
        }
        catch( Exception exc )
        {
            exc.printStackTrace();
            throw new osvrMgrError_misc();
        }

        // We don't expect a missingData or unknownName exception.
        // Other errors are just passed on.

        LRIdent lr_id = _installed_lrs.allocateIdentifier();
        long lr_id_num = lr_id.getIdl();

        // Set the lr_id in the LRMgr
        try {
            secSetLRID temp = (secSetLRID) soi.getUncountedInf(secSetLRID.infid);
            temp.setLRID(lr_id_num);
        } catch (Exception e) {
            DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "ERROR: Could not set LRID: "+e.getMessage());
            throw new osvrMgrError();
        }
        contactAddresses caddrs;
        // Do we need to set caddrs?
        caddrs = getContactAddresses(persistent == g.bool.True, soi, _pm);

        localReprStat status = createStatus( lr_id_num, ohandle, oname,
                                             CFG.tocreatebp, CFG.creds.chain,
                                             CFG.pedchain,
                                             persistent, caddrs);

        LREntry entry=null;
        try
        {
            entry = persistent == g.bool.True ?
                             (LREntry) new PerstLREntry(lr_id, status, soi, _pm)
                             :
                             (LREntry) new TransientLREntry(lr_id, status, soi);
        }
        catch( Exception exc )
        {
            exc.printStackTrace();
            throw new osvrMgrError_misc();
        }

        _installed_lrs.storeLocalRepr(lr_id, entry);
        return lr_id_num;

  }

  public long addBinding(g.opaque ohandle, String oname, rawDef jar,
        IDLLRMgrConfig CFG, short /* g.bool */ persistent) throws osvrMgrError
  {
    throw new NotImplementedException();
  }

  // Arno: should get all CAddrs, not just master
  private contactAddresses getContactAddresses(
            boolean persistent, SOInf soi, perstObjectManager pm)
    throws osvrMgrError
  {
    distributed lr = null;

    if (!persistent) {
      try {
        lr = (distributed) soi.getInf (distributed.infid);
        return lr.getContactAddresses();
      }
      catch (Exception exc) {
        DebugOutput.println(DebugOutput.DBG_DEBUG,
                            "cannot get master contact addresses:", exc);
        throw new osvrMgrError_misc();
      }
      finally {    
        if (lr != null) {
          lr.relInf();
        }
      }
    }
    else {
      persistentObject po = null;

      try {
        po = (persistentObject)soi.getInf(persistentObject.infid);
        //SOInf soInf = (SOInf)soi.dupInf();
        //po = (persistentObject)soInf.swapInf(persistentObject.infid);

        lr = (distributed) po.swapInf (distributed.infid);
        return lr.getContactAddresses();
      }
      catch (Exception exc) {
        DebugOutput.println(DebugOutput.DBG_DEBUG,
                            "cannot get master contact addresses:", exc);
        throw new osvrMgrError_misc();
      }
      finally {
        if (lr != null) {
          lr.relInf();
        }
      }
    }
  }

  public void removeLR (long lrid) throws osvrMgrError
  {
    // Actual op
    LRIdent id = new LRIdent (lrid);
    LREntry entry = _installed_lrs.removeLocalRepr (id);
    if (entry == null)
      throw new osvrMgrError_invArg();

    // if persistent, also unregisters from the PM
    entry.destroyLocalRepr (false);
  }

  public void unbindDSO(g.opaque ohandle) throws osvrMgrError
  {
    throw new NotImplementedException();
  }

  public localReprIDList listAll() throws osvrMgrError
  {
    return _installed_lrs.getIdentList();
  }

  public localReprIDList listDSO(g.opaque ohandle) throws osvrMgrError 
  {
    throw new NotImplementedException();
  }

    public localReprIDList listOwned( Certificate[] PeerIdentChain, KeyStore PedigreeCAKS )
    throws osvrMgrError
    {
        List l = new ArrayList();
        LREntry[] entries = _installed_lrs.getEntryArray();
        for (int i=0; i<entries.length; i++)
        {
            localReprStat status = entries[i].stat();

            // TODO
            // We currently use heavy crypto to determine is peer is really owner.
            // Could perhaps user more lightweight mechanism if it isn't really
            // a problem if a malicious person could get at the LRIDs of other
            // people's objects.
            //
            try
            {
                if (IdentCertUtil.peerOwnsObject( PeerIdentChain, status.ohandle, status.pedchain, PedigreeCAKS ))
                {
                     l.add( new Long( status.lrid ) );
                }
            }
            catch( RTSException exc )
            {
                DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
                exc.printStackTrace();
                throw new osvrMgrError_misc();
            }
        }

        localReprIDList lr_ids_of_owners_objs = new localReprIDList( l.size() );
        for (int i=0; i<l.size(); i++)
        {
            lr_ids_of_owners_objs.v[i] = ((Long)l.get( i )).longValue();
        }
        return lr_ids_of_owners_objs;
    }



  public localReprStat statLR (long lrid) throws osvrMgrError
  {
    LRIdent id = new LRIdent (lrid);
    LREntry entry = _installed_lrs.lookupLocalRepr (id);
    if (entry == null)
      throw new osvrMgrError_invArg();

    return entry.stat();
  }

  // other public methods

  /**
   * Tells this MgrOperations object to release all LRs. As a result transient
   * LRs are destroyed, and persistent LRs are able to passivate.
   *
   * @param quickly   if set, the LRs should not perform any wide-area
   *	      	      communication
   */
  public void releaseLRs (boolean quickly)
  {
    // Request a copy of the entry list, to avoid race conditions in traversal
    // (actually, these cannot occur).
    LREntry[] entries = _installed_lrs.getEntryArray();
    for (int i = 0; i < entries.length; i++) {

      LREntry entry = entries [i];
      _installed_lrs.removeLocalRepr (entry.getLRIdent());
      entry.releaseLocalRepr (quickly);
    }
  }

  /**
   * CHECKP>>
   * Returns the number of LRs in the object server, used by ObjSvrMgr
   * in an attempt to speed up checkpointing (Arno, 2002-10-07)
   */
  public int numberOfLRs()
  {
      return _installed_lrs.size();
  }

  /**
   * Marshalls the persistent state of this object, and writes it to the
   * given packet. The packet is written at its current offset.
   *
   * @param pkt          the pkt to write to
  */
  public void marshall (RawCursor pkt)
  {
    // write the mapping of persistent LRs (see notes about format)

    // the allocation state
    _installed_lrs.marshallAllocationState (pkt);

    // First write the per-LR infos, then write the number of LRs

    // the number of LRs will be written here later
    int n_lrs_offset = pkt.getOffset();
    // write a dummy value to skip this field
    RawBasic.writeInt64 (pkt, 0);

    // a count of the number of LRs written
    long n_lrs = 0;

    // request a copy of the entry list, to avoid race conditions in traversal
    LREntry[] entries = _installed_lrs.getEntryArray();
    for (int i = 0; i < entries.length; i++) {

      LREntry entry = entries [i];
      if (! (entry instanceof PerstLREntry))
        continue; // transient

      PerstLREntry pentry = (PerstLREntry) entry;
      pentry.marshall (pkt, _ls);
      n_lrs++;
    }

    // write the number of LRs that were written
    int end_offset = pkt.getOffset();
    pkt.setOffset (n_lrs_offset);
    RawBasic.writeInt64 (pkt, n_lrs);
    pkt.setOffset (end_offset);

    DebugOutput.println ("mgr: wrote " + n_lrs + " persistent LRs");
  }

  /**
   * Unmarshalls the persistent state of this object from the given packet.
   * The packet is read from its current offset.
   *
   * @param pkt          the pkt to read from
   * @exception ProtocolException
   *         if a the packet was too short or incorrectly encoded
  */
  public void unmarshall (RawCursor pkt) throws ProtocolException
  {
    // read the mapping of persistent LRs (see notes about format)

    if (_installed_lrs.size() != 0) {
      // the existing state should be cleared, I suppose
      throw new NotImplementedException ("state exists before unmarshalling");
    }

    // the allocation state
    _installed_lrs.unmarshallAllocationState (pkt);

    // read the number of LRs to be read
    long n_lrs = RawBasic.readInt64 (pkt);
    DebugOutput.println ("mgr: reading " + n_lrs + " persistent LRs");

    while (n_lrs-- > 0) {
      PerstLREntry entry = PerstLREntry.unmarshall (pkt, _pm, _ls);

      // Okay we need to check here if this object is still valid and should
      // really be added. It could be that there was an error while activating
      // this LR and thus does not exist.
      if (_pm.isValidEntry(entry.getPerstIdent()) == g.bool.False) {
        // Does not exist;
        DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "mgr: lr failed: "+
                        entry.getLRIdent()+" -> "+entry.getPerstIdent());
        continue;
      }
      
      _installed_lrs.storeLocalRepr (entry.getLRIdent(), entry);
    }
  }

    /**
     * Creates a LR status record for an LR. The 'installation time' field
     * is set to the current time.
     */
    public static localReprStat createStatus( long lrid,
                                              g.opaque ohandle,
                                              String oname,
                                              IDLBlueprint bp,
                                              IDLRightsCerts rightschain,
                                              IDLIdentCerts pedchain,
                                              short /* g.bool */ persistent,
                                              contactAddresses caddrs )
    {
        localReprStat status = new localReprStat();

        status.lrid = lrid;
        status.ohandle = ohandle;
        status.oname = oname;
        status.bp = bp;
        status.rightschain = rightschain;
        status.pedchain = pedchain;
        status.persistent = persistent;
        status.ctime = System.currentTimeMillis();

        status.lsmtime = System.currentTimeMillis();
        status.caddrs = caddrs;

        return status;
    }

  /**
   * Creates binder objects under the _mgr context. Thread-safe.
   */
  /*
    NOTE: the reason this is a top-level (static), rather than a nested
    class is due to a bug in JDK 1.2.?. See Sun's list of bugs (id 4218427).
  */
  private static class BinderCreator
  {
    private final SCInf _binder_fact;      // counted

    /** Used to construct a new local name for a binder object. */
    private int _counter = 0;

    /** The prefix to name binder objects with in the local name space. */
    private static final String BINDER_PREFIX = "binder";

    /** The local name space. */
    private final nameSpace _lns;

    /** The context of the object server manager. */
    private final int _mgr_ctx;

    /**
     * Constructor. When the creator of this object is
     * done, removeClient() should be called.
     *
     * @param lns		the local name space
     * @param mgr_ctx		the context of the object server manager
     * @exception Exception
     *   a Globe exception during instance creation or name space access.
     */
    public BinderCreator (nameSpace lns, int mgr_ctx) throws Exception
    {
      _lns = lns;
      _mgr_ctx = mgr_ctx;
      SOInf fact_soi = (SOInf) _lns.bind (context.NsRootCtx,
                                    nsConst.BINDER_FACT_NAME);
      _binder_fact = (SCInf) fact_soi.swapInf (SCInf.infid);
    }

    /** Releases any Globe references held. */
    public void removeClient()
    {
      _binder_fact.relInf();
    }

    /**
     * Creates a new binder instance.
     * 
     * @exception Exception
     *   a Globe exception during instance creation or name space access.
     */
    public binder createBinder() throws SOIerrors
    {
            SOInf binder_soi = null;
            try
            {
                 binder_soi = StdUtil.createGlobeObject((SCInf) _binder_fact.dupInf(),
                            _mgr_ctx, nextName());
            }
            catch (Exception e )
            {
                e.printStackTrace();
                throw new SOIerrors_misc();
            }
            return (binder) binder_soi.swapInf(binder.infid);
    }

    /**
     * Generates a new local name for a binder object.
     */
    private synchronized String nextName()
    {
      return BINDER_PREFIX + String.valueOf (_counter++);
    }
  }
}
