/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.lr.mgr;

// basic imports
import vu.globe.idlsys.g;
import vu.globe.rts.std.idl.stdInf.*;
import vu.globe.rts.std.StdUtil;
import vu.globe.rts.java.*;

// namespace
import vu.globe.rts.runtime.ns.idl.context;
import vu.globe.rts.runtime.ns.nameSpaceImp.nameSpaceImp;
import vu.globe.rts.runtime.ns.nsConst;
import vu.globe.rts.runtime.ns.idl.nsTypes.*;

// secconfig etc
import vu.globe.rts.security.*;
import vu.globe.rts.security.idl.sectypes.*;
import vu.globe.rts.security.idl.secconfig.*;
import vu.globe.rts.security.idl.security.*;
import vu.globe.rts.security.certs.AccessCtrlBitmap;

// configurable
import vu.globe.rts.std.idl.configure.*;

// location service
import vu.globe.svcs.gls.idl.lsClient.*;
import vu.globe.svcs.gls.idl.typedAddr.*;

// communication
import vu.globe.util.comm.idl.rawData.*;

// other interfaces
import vu.globe.rts.lr.idl.distribution.*;
import vu.globe.rts.lr.idl.lrsub.*;
import vu.globe.rts.lr.idl.repl.*;
import vu.globe.svcs.objsvr.idl.resource.*;
import vu.globe.svcs.objsvr.idl.resource;
import vu.globe.svcs.objsvr.idl.persistence.*;

import vu.globe.util.comm.*;
import vu.globe.util.debug.DebugOutput;
import vu.globe.util.exc.NotImplementedException;
import vu.globe.rts.runtime.cfg.Settings;

import java.net.ProtocolException;

import vu.globe.util.misc.MoveObjectServer; // HACK TO TRANSFER OBJECT SERVER

// METAJAVA
import java.lang.reflect.*;
import java.lang.ClassNotFoundException;
import vu.gaia.lr.n.sem.SemanticsSubobject;
import vu.gaia.lr.n.sem.GOSPersistentSemanticsSubobject;
import java.util.ArrayList;

//BVD
import java.security.ProtectionDomain;
import java.net.URL;
import vu.globe.rts.runtime.repository.NoPermissionURLClassLoader;

import java.io.*;
import vu.globe.util.comm.*;

/**
 * LR manager/compositor base class to be extended by LR managers for specific
 * applications. The base class contains distribution and persistence support.
 * It implements all required LR interfaces except for application-specific
 * interfaces. The latter must be implemented by subclasses.
 * <p>
 * The base class also takes care of creating and initialising LR subobjects.
 * It does so based on its initialisation string. See LRMgrConfig on how to
 * create a correct configuration string.
 *
 * @author Patrick Verkaik
 */

public abstract class LRMgr extends LRMgrSkel
{
    /** When activating from a crash, whether to write recovered passivation
     * state as regular passivation state to disk. That is, whether to
     * synchronize the regular passivation state on disk with the state that
     * was saved during the last passivation checkpoint.
     * If set to false, the regular passivation state will be written to disk
     * when the object server is shut down.
     */
    private static final boolean SYNC_RECOVERED_STATE_TO_DISK = false;
    
    /** The replication object. */
    protected SOInf _repl_soi;    // counted
    
    /** The optional semantics object. Null if not present. */
    protected SemanticsSubobject _sem_real;    // Pure Java Object!
    /** The control object is replaced by a dynamic proxy and a ControllingLRMgr */
    private Object _sem_proxy;   // java.lang.reflect Dynamic proxy.
    private GOSPersistentSemanticsSubobject _sem_subobj; // null if LR is transient
    
    /** The optional communication object. Null if not present. */
    private SOInf _comm_soi;    // counted
    
    // replication subobject interfaces
    private lrSubObject _repl_subobj;
    private replDistr   _replDistr_subobj;
    
    // The security object.
    protected SOInf _sec_soi;   // counted

    // security subobject interfaces
    private lrSubObject _sec_subobj;

    // security certificate management interface
    private secCertManagement _secCertManagement;

    // Setting the lr id
    private long _lrid;

    private secSetLRID _secSetLRID;
    
    /**
     * Distributed object's object handle. Persistent.
     */
    private g.opaque _ohandle;
    
    /**
     * Registered contact addresses. Empty or null if none registered. Persistent.
     */
    private contactAddresses _reg_caddrs;

    /** The config of this LR manager. */
    private IDLLRMgrConfig  _cfg;
    
    /** LS resolver interface. */
    private idManager _id_manager; // counted
    
    /** LS resolver interface. */
    private stdResolverOps _resolver_ops;
    
    // persistence
    
    /**
     * Persistence id. The id != invalidResourceID during and only during the
     * time that this LR is persistent.
     */
    private long _perst_id = resource.invalidResourceID;
    
    /**
     * The PM's 'activation record' interface. Initialised when the object
     * is first made persistent, or is activated.
     */
    private activationRecord _perst_activation; // counted
    
    /**
     * The PM's 'storage manager' interface. Initialised when the object
     * is first made persistent, or is activated.
     */
    private storageManager _storage_mgr;
    
    /**
     * If set, the object must not perform wide-area communication when
     * passivated.
     */
    private boolean _passivate_quickly = false;
    
    /**
     * If set, the object must not perform wide-area communication when
     * destroyed.
     */
    private boolean _destroy_quickly = false;


    /** 
     * Set when an error occured during activation, indicating that we should
     * not inform passivated to the lr manager.
     */
    private boolean _activationError = false;
    
    
    /** The local name of the semantics object, relative to the LR context. */
    public static final String SEM_LNAME = "sem";
    
    /** The local name of the communication object,relative to the LR context.*/
    public static final String COMM_LNAME = "comm";
   
    /** The local name of the securitu subobject, relative to LR context. */
    public static final String SEC_LNAME = "sec";
    
    /** The local name of the replication object, relative to the LR context. */
    public static final String REPL_LNAME = "repl";
    
    /** The local name of the control object, relative to the LR context. */
    public static final String CTRL_LNAME = "ctrl";
   
    // The jar so we can reconstruct ourselves later
    private rawDef _jar = null;

    /**
     * Constructor.
     */
    protected LRMgr()
    {
    }
    
    protected void cleanup() throws vu.globe.rts.std.idl.stdInf.SOIerrors
    {
        try
        {
            // if we're persistent then passivate, otherwise clean up
            if (_perst_id != resource.invalidResourceID)
            {
                
                try {
                    passivate(); 
                } finally {
                    // If there was no error during activation, this is a normal
                    // cleanup and the PerstMgr.Coordinator is waiting to hear
                    // from us. Otherwise skip this, otherwise you run into
                    // trouble later with the Coordinator waiting to hear from
                    // us, but that will not happen anymore.
                    if (!_activationError) {
                        _perst_activation.informPassivated(_perst_id);
                    }
                }
            }
            else
            {
                cleanupLR();
            }
        }
        catch (Exception exc)
        {
            DebugOutput.println(DebugOutput.DBG_NORMAL, "Ignoring exception "+
                                "during passivation or clean-up of LR "+
                                _perst_id+"-"+_lrid+": "+exc.getMessage());
            //    exc.printStackTrace();
        }
        GInterface.RelInf(_sec_soi);
        GInterface.RelInf(_repl_soi);
        GInterface.RelInf(_comm_soi);
        
        GInterface.RelInf(_perst_activation);
        GInterface.RelInf(_id_manager);
        
        super.cleanup();

        if (_activationError) {
            // Make extra clean
            System.gc();
        }
    }

    // configureLRMgr interface
    public void constructor(rawDef jar, IDLLRMgrConfig CFG) throws 
                                                            constructorErrors {

        // save config
        _cfg = CFG;     // Config canNOT be created here, because cacerts 
                        // needs to be set before the constructor is called.
                    
        // Explicitly set the lsAddr to null, if we are recovering they will 
        // be set by unmarsallMe, otherwise they remain null.
        _cfg.lsAddrs = null;
        
        _jar = jar;
        try
        {
            // HACK TO TRANSFER OBJECT SERVER
            // TODO FIXME FIXME
            /*
            if (Settings.getMasterSrcSetting() != null)
            {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "%%% LR: HACK TO CONTACT TRANSFERRED OBJECT SERVER");
                init = MoveObjectServer.translateMasterIPAddress(init, "%%% LR:   ");
            }
             */
            
            IDLBlueprint mybp = _cfg.tocreatebp;
            String repl_impl = mybp.subobjs.v[IDLSubobjectID.REPL_SUBOBJ].impl;
            String repl_init = mybp.subobjs.v[ IDLSubobjectID.REPL_SUBOBJ ].init;

            String comm_impl = mybp.subobjs.v[ IDLSubobjectID.COMM_SUBOBJ ].impl;
            String comm_init = mybp.subobjs.v[ IDLSubobjectID.COMM_SUBOBJ ].init;
            String sem_impl = mybp.subobjs.v[ IDLSubobjectID.SEM_SUBOBJ ].impl;
            String sem_init = mybp.subobjs.v[ IDLSubobjectID.SEM_SUBOBJ ].init;
            String ctrl_init = mybp.subobjs.v[ IDLSubobjectID.CTRL_SUBOBJ ].init;
            
            String sec_impl = mybp.subobjs.v[ IDLSubobjectID.SEC_SUBOBJ ].impl;
            String sec_init = mybp.subobjs.v[ IDLSubobjectID.SEC_SUBOBJ ].init;

            DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "LRMgr: ***** REPL IMPL = "+repl_impl);

            // check presence of obligatory init string attributes
            if (repl_impl.equals("")) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                                    "LRMgr: ERROR: repl_impl is empty !");
                throw new constructorErrors_invArg();
            }

            if (sec_impl.equals("")) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                                    "LRMgr: ERROR: sec_impl is empty !!");
                throw new constructorErrors_invArg();
            }
 
     /* Create and initialise the subobjects in two phases. First, instantiate
     * them as regular Globe local objects. Next, configure them. Note that
     * the semantics and communication objects are optional.
     */

            DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "LRMgr: ***** SEM IMPL = "+sem_impl);
            if (!sem_impl.equals(""))
                _sem_real = (SemanticsSubobject)
                                    createPureJavaSemanticsSubobject(sem_impl);
            if (!comm_impl.equals(""))
                _comm_soi = createSubobject(comm_impl, COMM_LNAME);
    
            // Create the security subobject
            _sec_soi = createSubobject(sec_impl, SEC_LNAME);

            // Create the replication subobject
            _repl_soi = createSubobject(repl_impl, REPL_LNAME);         

            Class[] remoteInfs = null;
            try
            {
                // _sem_real.getClass().getInterfaces() don't work to 
                // get remote infs
                remoteInfs = ReflectionUtil.getUserDefinedInterfaces( 
                                                        _sem_real.getClass());
            }
            catch( ClassNotFoundException e )
            {
                throw new constructorErrors_misc();
            }
            _sem_proxy = Proxy.newProxyInstance( _sem_real.getClass().getClassLoader(),
                    remoteInfs,
                    this );
            
            // Phase 2: configuration of the subobjects
            // ========================================
            // When necessary, we use new constructor* interfaces defined in
            // secconfig.idl. Otherwise, we use the old configure interface.
            //
            if (!comm_impl.equals("")) {
                configureSubObject(_comm_soi, comm_init);
            }
            configureSubObject(_repl_soi, repl_init);
            configureSubObject( this.soi, ctrl_init );

            secConnector secObject =
                    (secConnector) _sec_soi.getUncountedInf(secConnector.infid);
            secObject.constructor(sec_init, CFG );


            // retrieve some of the subobjects' interfaces
            _sec_subobj = 
                    (lrSubObject) _sec_soi.getUncountedInf(lrSubObject.infid);
            _secCertManagement = (secCertManagement) 
                            _sec_soi.getUncountedInf(secCertManagement.infid);
            _secSetLRID = (secSetLRID)
                            _sec_soi.getUncountedInf(secSetLRID.infid);
            _repl_subobj =
                   (lrSubObject) _repl_soi.getUncountedInf(lrSubObject.infid);
            _replDistr_subobj = (
                    replDistr) _repl_soi.getUncountedInf( replDistr.infid);
            
            // other, unrelated, initialisation: the LS resolver's interfaces
            SOInf ls = lns.bind(getContext(), nsConst.LS_CLIENT_NAME);
            
            _id_manager = (idManager) ls.swapInf(idManager.infid);
            _resolver_ops = (stdResolverOps) _id_manager.soi.getUncountedInf(
                                                        stdResolverOps.infid);
        }
        catch( constructorErrors ce )
        {
            throw ce;
        }
        catch( Exception e )
        {
            e.printStackTrace();
            DebugOutput.print( "LRMgr: configure: **** ERROR: " );
            DebugOutput.printException(DebugOutput.DBG_DEBUG, e );
            throw new constructorErrors_misc();
        }
    }
    
    /** Configures the given object, by passing the given init string to it. */
    private void configureSubObject(SOInf soi, String init)
    throws constructorErrors
    {
        try
        {
            configurable cfg = (configurable)
                                        soi.getUncountedInf(configurable.infid);
            cfg.configure(init);
        }
        catch (Exception exc)
        {
            DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
            throw new constructorErrors_misc();
        }
    }
    
    
    /**
     * Creates a subobject from the given implementation handle, and installs
     * it in the local name space under the given name.
     *
     * @return   the created subobject's standard object interface
     *
     * @exception constructorErrors_invArg
     * 		if the implementation handle is invalid
     */
    private SOInf createSubobject(String impl, String lname)
    throws constructorErrors
    {
        try
        {
            SOInf co_soi = lns.bind(getContext(), "repository/" + impl);
            SCInf sci = (SCInf) co_soi.swapInf(SCInf.infid);
            return StdUtil.createGlobeObject(sci, getContext(), lname);
        }
        catch (nsErrors_notFoundErr exc)
        {
            DebugOutput.printException(DebugOutput.DBG_DEBUGPLUS, exc);
            throw new constructorErrors_invArg();
        }
        catch (Exception exc)
        {
            DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
            throw new constructorErrors_misc();
        }
    }
    
    
    /**
     * Creates a pure Java object from the given implementation handle,
     * which is assumed to be the fully qualified name of a Java class
     */
    private Object createPureJavaSemanticsSubobject( String impl )
    throws constructorErrors
    {
        try
        {
            // BVD
            DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "lrmgr: pure java " + impl);
            // FORMAT: "PUREJAVA;vu.gaia.apps.integer.n.IntegerPersistImpl@http://www.cs.vu.nl/globe/integer.jar";
            int impl_idx = impl.indexOf( ';' );
            int url_idx = impl.indexOf( '@' );
            Class c = null;
            if (url_idx == -1)
            {
                // Load from local impl repository
                String classname = impl.substring( impl_idx+1 );
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                                    "lrmgr: loading local class " + classname);
                c = Class.forName( classname );
            }
            else
            {
                /*
                 * Remote class loading. When using remote classes one
                 * should have Java runtime-access control enabled,
                 * such that semantics subobjects are properly sandboxed.
                 * See -Z option for object servers, etc.
                 */
                String classname = impl.substring( impl_idx+1, url_idx );
                String url_str = impl.substring( url_idx+1, impl.length() );
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                                    "lrmgr: remotely loading class "+ 
                                    classname + " from " + url_str );
                URL url = new URL( url_str );

                // Loader puts class in ProtectionDomain with no permissions.
                // (assuming java.policy is properly set).
                //
                NoPermissionURLClassLoader loader = 
                                new NoPermissionURLClassLoader(new URL[] {url});
                c = loader.loadClass( classname );

                Object[] signers = c.getSigners();
                if (signers != null)
                {
                    DebugOutput.println(DebugOutput.DBG_DEBUG,
                            "lrmgr: class has " + signers.length + " singers" );
                    for (int i=0; i<signers.length; i++)
                    {
                        DebugOutput.println(DebugOutput.DBG_DEBUG,
                            "lrmgr: class signed by " + signers[i].toString() );
                    }
                }
                ProtectionDomain pd = c.getProtectionDomain();
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                                    "lrmgr: class has protection domain " + pd);
                if (pd != null)
                {
                    DebugOutput.println(DebugOutput.DBG_DEBUG, pd.toString() );
                }
            }
            return c.newInstance();
        }
        catch (Exception exc)
        {
            DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
            exc.printStackTrace();
            throw new constructorErrors_misc();
        }
    }
    
    
  /*
   * 'distributed' interface
   */
    
    public void createDistrObject()
    {
        // empty
    }
   
    // THIS IS NEEDED BY /vu/globe/svcs/objsvr/mgr/MgrOperations.java
    // So I won't remove it
    public contactAddresses getContactAddresses()
    {
        if (_reg_caddrs == null)
        {
            return new contactAddresses(0);
        }
        else
        {
            contactAddresses caddrs=new contactAddresses(_reg_caddrs.v.length);
            for (int i = 0; i < _reg_caddrs.v.length; i++)
            {
                caddrs.v[i] = _reg_caddrs.v[i];
            }
            return caddrs;
        }
    }
    
    public long getLSMTime()
    throws vu.globe.rts.lr.idl.distribution.distrError
    {
        try
        {
            long t= _replDistr_subobj.getLSMTime();
            return t;
        }
        catch( Exception e )
        {
            e.printStackTrace();
            throw new distrError_misc();
        }
    }
    
    
    public void bindDistrObject()
    {
        // empty
    }
    
    
    public void setObjectHandle(g.opaque ohandle)
    {
        _ohandle = ohandle;
    }
    
    
    public void distributeObject() throws Exception
    {
        // an invalidResourceID is passed to the subobject if this is not a
        // persistent LR
        
        if (_perst_id == resource.invalidResourceID)
        {
            DebugOutput.println(DebugOutput.DBG_DEBUGPLUS, "LRMgr: I am transient");
        }
        else
        {
            DebugOutput.println(DebugOutput.DBG_DEBUGPLUS, "LRMgr: I am persistent");
            // If persistent _sem_real implements new semSubobject Java 
            // interface with same methods as IDL lrSubobject.
            _sem_subobj = (GOSPersistentSemanticsSubobject) _sem_real;
            _sem_subobj.setPerstID( _perst_id );
        }
        // Security subobject needs to know ID
        _sec_subobj.setPerstID(_perst_id);
        
        // repl subobj needs to know for some reason... (at the moment no
        // persistency but in case someone else wants to store stuff 
        _repl_subobj.setPerstID( _perst_id );
 
        // Set the object handle
        _sec_subobj.setObjectHandle(_ohandle);
        _repl_subobj.setObjectHandle(_ohandle);
        
        // Get the wheels in motion
        _reg_caddrs = _replDistr_subobj.distributeObject();
        
        if (_reg_caddrs == null)
            return;
        
        DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
        "LR: number of caddrs registered: " + _reg_caddrs.v.length);
    }
    
    public void prepareImmediateDestruction()
    {
        _destroy_quickly = true;
    }
    
    
    // persistentObject interface
    
    public long getPersistenceID()
    {
        return _perst_id;
    }
    
    // managedPerstObject interface
    
    public void bePersistent(long pid) throws Exception
    {
        lookupPerstMgr();
        _perst_id = pid;
    }
    
    
    public void beTransient()
    {
        _perst_id = resource.invalidResourceID;
    }
    
    
    public void prepareImmediatePassivation()
    {
        _passivate_quickly = true;
    }
    
    public void activate(long pid) throws Exception
    {

        DebugOutput.println(DebugOutput.DBG_DEBUG, "LRMgr: activate()");
        lookupPerstMgr();
        
        _perst_id = pid;
        
        // restore LR state
        long state_id = _perst_activation.getState(_perst_id);
        storage storage_obj = _storage_mgr.openStorage(state_id);
        
        rawDef state;
        try
        {
            // obtain state
            state = storage_obj.read(-1); // read the entire thing
        }
        finally
        {
            storage_obj.close();
            storage_obj.relInf();
        }
        
        // The state packet of the LR is divided over the following objects, the
        // state of each of which is encoded as an octet string:
        // - this subobject
        // - security object
        // - replication object
        // - semantics object
        
        RawCursor cur = new RawCursor(state);
        rawDef my_state = RawBasic.readROctetString(cur);
        rawDef sec_state = RawBasic.readROctetString(cur);
        rawDef repl_state = RawBasic.readROctetString(cur);
        rawDef sem_state = RawBasic.readROctetString(cur);
        
        // Tell the subobjects to unmarshall state and activate.
        
        unmarshallMe(my_state);
        
        //METAJAVA
        _sem_subobj = (GOSPersistentSemanticsSubobject)_sem_real;
        
        _sem_subobj.setPerstID( _perst_id );
        byte[] sem_state_bytes = RawOps.getRaw( sem_state );
        _sem_subobj.setIncoreState( sem_state_bytes, false );


        // Inform the sec and repl of the object handle
        _sec_subobj.setObjectHandle(_ohandle);
        _sec_subobj.setObjectHandle(_ohandle);

        // Tell the security subobject and replication object to prepare for
        // activation.
        _sec_subobj.prepareActivation(_perst_id, sec_state, g.bool.False);
        _repl_subobj.prepareActivation(_perst_id, repl_state, g.bool.False);

        // Activate first the security and then the replication subobject
        try {
            _sec_subobj.completeActivation();
            _repl_subobj.completeActivation();
        } catch (Exception e) {
            _activationError = true;
            throw e;
        }
        
        // HACK TO TRANSFER OBJECT SERVER
        if (Settings.getMoveGOSSetting() != null)
        {
            transformCaddrs();
        }
        
        if (Settings.getRegisterCaddrsSetting())
        {
            registerCaddrs();
        }
    }
    
    public void activateFromCrash(long pid, rawDef state) throws Exception
    {
        DebugOutput.println(DebugOutput.DBG_DEBUG,"LRMgr: ActivateFromCrash()");
        lookupPerstMgr();
        
        _perst_id = pid;
        
        if (SYNC_RECOVERED_STATE_TO_DISK)
        {
            writePerstStateToStorage(state);
        }
        
        // SEE activate()
        
        // The state packet of the LR is divided over the following objects, the
        // state of each of which is encoded as an octet string:
        // - this subobject
        // - security object
        // - replication object
        // - semantics object
        
        RawCursor cur = new RawCursor(state);
        rawDef my_state = RawBasic.readROctetString(cur);
        rawDef sec_state = RawBasic.readROctetString(cur);
        rawDef repl_state = RawBasic.readROctetString(cur);
        rawDef sem_state = RawBasic.readROctetString(cur);
        
        // Tell the subobjects to unmarshall state and activate.
        
        unmarshallMe(my_state);
        
        //METAJAVA
        _sem_subobj = (GOSPersistentSemanticsSubobject)_sem_real;
        _sem_subobj.setPerstID( _perst_id );
        byte[] sem_state_bytes = RawOps.getRaw( sem_state );
        _sem_subobj.setIncoreState( sem_state_bytes, true );
        
        // prepare security and replication for activation
        _sec_subobj.prepareActivation(_perst_id, sec_state, g.bool.True);
        _repl_subobj.prepareActivation(_perst_id, repl_state, g.bool.True);

        // Inform the sec and repl of the object handle
        _sec_subobj.setObjectHandle(_ohandle);
        _sec_subobj.setObjectHandle(_ohandle);
        
        // Activ8
        try {
            _sec_subobj.completeActivation();
            _repl_subobj.completeActivation();
        } catch (Exception e) {
            _activationError = true;
            throw e;
        }
        
        // HACK TO TRANSFER OBJECT SERVER
        if (Settings.getMoveGOSSetting() != null)
        {
            transformCaddrs();
        }
        
        if (Settings.getRegisterCaddrsSetting())
        {
            registerCaddrs();
        }
    }
    
    public void preparePassivationCheckpoint() throws Exception
    {

        // The communication layers have already been notified.
        // Now first notify the security layer and the the repl layer
        _sec_subobj.preparePassivationCheckpoint();
        _repl_subobj.preparePassivationCheckpoint();
    }
    
    public passivationState passivationCheckpoint() throws Exception
    {
        _perst_activation.setRecreateInfo(_perst_id, "", "", _jar);

        // The persistent state of the LR is contained by this object, the
        // security, replication and the semantics object.
        
        rawDef sec_state = _sec_subobj.passivationCheckpoint();
        rawDef repl_state = _repl_subobj.passivationCheckpoint();
        byte[] sem_state_bytes = _sem_subobj.getIncoreState();
        rawDef sem_state = RawOps.createRaw();
        RawOps.setRaw( sem_state, sem_state_bytes, 0, sem_state_bytes.length);
        rawDef my_state = marshallMe();
        
        RawCursor cur = marshallState(my_state, sec_state, 
                                      repl_state, sem_state);
        
        passivationState psState = new passivationState();
        psState.perst_id = _perst_id;
        psState.state = cur.getRaw();
        return psState;
    }
    
    public void completePassivationCheckpoint() throws Exception
    {
        _sec_subobj.completePassivationCheckpoint();
        _repl_subobj.completePassivationCheckpoint();
    }
    
    private void transformCaddrs()
    throws Exception
    {
        DebugOutput.println(DebugOutput.DBG_DEBUG,
                            "%%% LR: HACK TO TRANSFER OBJECT SERVER");
        DebugOutput.println(DebugOutput.DBG_DEBUG,
                            "%%% LR: transforming contact addresses");
        
        int n_caddrs = _reg_caddrs == null ? 0 : _reg_caddrs.v.length;
        for (int i = 0; i < n_caddrs; i++)
        {
            
            // unregister the old contact address
            _resolver_ops.delete(_ohandle, _reg_caddrs.v[i], null);
            
            // transform the old contact address to a new contact address
            _reg_caddrs.v[i] = MoveObjectServer.translateMyIPAddress(
                                            _reg_caddrs.v[i],
                                            _id_manager, "%%% LR:   ");
            
            // register the new contact address
            _resolver_ops.insert(_ohandle, _reg_caddrs.v[i], null);
        }
        DebugOutput.println(DebugOutput.DBG_DEBUG,
                            "%%% LR: number of contact addresses transformed: "
                             + n_caddrs);
    }
    
    private void registerCaddrs()
    throws Exception
    {
        DebugOutput.println(DebugOutput.DBG_DEBUG,
                            "%%% LR: reregistering contact addresses");
        int n_caddrs = _reg_caddrs == null ? 0 : _reg_caddrs.v.length;
        for (int i = 0; i < n_caddrs; i++)
        {
            g.opaque ca = _reg_caddrs.v[i];
            // register the contact address
            _resolver_ops.insert(_ohandle, ca, null);
        }
        DebugOutput.println(DebugOutput.DBG_DEBUG,
                            "%%% LR: number of caddrs reregistered: "+n_caddrs);
    }
    
    
    /**
     * Passivates the object. Depending on _passivate_quickly may or may
     * not allow wide-area communication by subobjects.
     */
    private void passivate() throws Exception
    {
        if (Settings.getUnregisterCaddrsSetting())
        {
            DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "%%% LR: unregistering contact addresses");
            int n_caddrs = _reg_caddrs == null ? 0 : _reg_caddrs.v.length;
            for (int i = 0; i < n_caddrs; i++)
            {
                g.opaque ca = _reg_caddrs.v[i];
                // unregister the contact address
                _resolver_ops.delete(_ohandle, ca, null);
            }
            DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "%%% LR: number of caddrs unregistered: "+
                                n_caddrs);
        }

        _perst_activation.setRecreateInfo(_perst_id, "", "", _jar);
        
        // The persistent state of the LR is contained by this object, the
        // replication and the semantics object. Tell these subobjects to
        // passivate and marshall state.
       
        // First passivate the sec subobject, so it can tell the
        // connections it has about the passivation
        _sec_subobj.preparePassivation(_passivate_quickly ?
                                       g.bool.True : g.bool.False);
        _repl_subobj.preparePassivation(_passivate_quickly ?                                                            g.bool.True: g.bool.False);
        
        rawDef sec_state = _sec_subobj.completePassivation();
        rawDef repl_state = _repl_subobj.completePassivation();
        
        byte[] sem_state_bytes = _sem_subobj.getIncoreState();
        rawDef sem_state = RawOps.createRaw();
        RawOps.setRaw( sem_state, sem_state_bytes, 0, sem_state_bytes.length);
        rawDef my_state = marshallMe();
        
        RawCursor cur = marshallState(my_state, sec_state,
                                      repl_state, sem_state);
        
        writePerstStateToStorage(cur.getRaw());
    }
    
    
    /**
     * Marshalls the state of this object and returns it.
     */
    private RawCursor marshallState(rawDef my_state, rawDef sec_state,
                                    rawDef repl_state, rawDef sem_state)
            throws Exception
    {
        // Combine the state of each of the following subobjects in one packet,
        // each as an octet string:
        // - this subobject
        // - security object
        // - replication object
        // - semantics object
        
        rawDef state = RawOps.createRaw();
        RawCursor cur = new RawCursor(state);
        
        RawBasic.writeOctetString(cur, my_state);
        RawBasic.writeOctetString(cur, sec_state);
        RawBasic.writeOctetString(cur, repl_state);
        RawBasic.writeOctetString(cur, sem_state);
        return cur;
    }
    
    
    /**
     * Writes the given state to persistent storage.
     */
    private void writePerstStateToStorage(rawDef state)
    throws Exception
    {
        long state_id = _perst_activation.getState(_perst_id);
        storage storage_obj = _storage_mgr.openStorage(state_id);
        
        try
        {
            storage_obj.setLength(0); // overwrite entirely
            storage_obj.write(state);
        }
        finally
        {
            storage_obj.close();
            storage_obj.relInf();
        }
    }
    
    /**
     * Unmarshalls the state of this subobject.
     *
     * @exception ProtocolException
     *		if the given packet was incorrectly formatted
     * @exception lsResolverClientError_invArg
     *		if the given packet was incorrectly formatted
     * @exception Exception
     *		some other really useful exception
     */
    private void unmarshallMe(rawDef state) throws ProtocolException,
    lsResolverClientError_invArg,
    Exception
    {
        RawCursor cur = new RawCursor(state);
        
        // unmarshall my state:
        // - my lr id
        // - the object handle (an octet string)
        // - the number of contact addresses
        // - for each contact address:
        //   - the contact address (an octet string)
        //   - the jar
        setLRID(RawBasic.readInt64(cur));
        
        rawDef r = RawBasic.readROctetString(cur);
        _ohandle = _id_manager.unmarshallObjectHandle(r);
        
        int n_caddrs = RawBasic.readInt32(cur);
        _reg_caddrs = new contactAddresses(n_caddrs);
        for (int i = 0; i < n_caddrs; i++)
        {
            
            r = RawBasic.readROctetString(cur);
            _reg_caddrs.v[i] = _id_manager.unmarshallContactAddress(r);
        }
        DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
        "LR: number of caddrs unmarshalled: " + n_caddrs);

        // Set the ls addresses so the secGeneric object can use them...
        _cfg.lsAddrs = _reg_caddrs;

        // CaCerts
        int nIdentCerts = RawBasic.readInt32(cur);
        IDLIdentCerts idlICerts = null;
        if (nIdentCerts > 0) {
            idlICerts = new IDLIdentCerts(nIdentCerts);
            for (int i = 0; i < nIdentCerts; i++) {
                byte[] iCert = RawBasic.readOctetString(cur);
                idlICerts.v[i] = new IDLIdentCert(iCert.length);
                System.arraycopy(iCert, 0, idlICerts.v[i].v, 0, iCert.length);
            }
        }
            
        // Set the Ca certs in the config.
        _cfg.cacerts = idlICerts;
    }
    
    /**
     * Marshalls the state of this subobject.
     */
    private rawDef marshallMe() throws Exception
    {
        // CHECKP>>
        // The size of the raw is just to speed up checkpointing, will work
        // correctly for any size raw, just slower (Arno, 2002-10-07)
        //
        int n_caddrs = _reg_caddrs == null ? 0 : _reg_caddrs.v.length;
        rawDef state = RawOps.createRaw( 15, n_caddrs*1705 );
        RawCursor cur = new RawCursor(state);

        
        // marshall our own state:
        // - my lrid
        // - the object handle (an octet string)
        // - the number of contact addresses
        // - for each contact address:
        //   - the contact address (an octet string)
        RawBasic.writeInt64(cur, _lrid);
        
        rawDef r = _id_manager.marshallObjectHandle(_ohandle);
        // CHECKP>>
        byte[] oh_bytes = RawOps.getRaw( r );
        RawBasic.writeOctetString(cur,  oh_bytes, 0, oh_bytes.length );
        
        RawBasic.writeInt32(cur, n_caddrs);
        for (int i = 0; i < n_caddrs; i++)
        {
            
            r = _id_manager.marshallContactAddress(_reg_caddrs.v[i]);
            //CHECKP>>
            byte[] caddr_bytes = RawOps.getRaw( r );
            RawBasic.writeOctetString(cur,  caddr_bytes, 0, caddr_bytes.length );
        }
        DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
        "LR: number of caddrs marshalled: " + n_caddrs);

        // write the cacerts of _cfg.cacerts (sectypes.IDLIdentCerts)
        // IDLIdentCerts = sequence of IDLIdentCert;
        // IDLIdentCert = sequence of int8;
        int nIdentCerts = (_cfg.cacerts == null) ? 0 : _cfg.cacerts.v.length;
        RawBasic.writeInt32(cur, nIdentCerts);

        for (int i = 0; i < nIdentCerts; i++) {
            RawBasic.writeOctetString(cur, _cfg.cacerts.v[i].v, 
                                      0, _cfg.cacerts.v[i].v.length);
        }
        return state;
    }
    
    // methods for subclasses
    
    /**
     * Returns the virtual control object
     */
    public Object getControlObject()
    {
        return _sem_proxy;
    }

    // Returns the certificate management interface such that the Deputy
    // can directly talk to the security subobject's cert management inf.
    public secCertManagement getCertManagementObject()
    {
        return _secCertManagement;
    }
    
    /**
     * Cleans up the object. Depending on _destroy_quickly may or may
     * not allow wide-area communication by subobjects.
     */
    private void cleanupLR() throws Exception
    {
        // unregister contact addresses, but only if we're allowed to perform
        // wide-area communication
        
        if (! _destroy_quickly) {
            
            int n_caddrs = _reg_caddrs == null ? 0 : _reg_caddrs.v.length;
            
            for (int i = 0; i < n_caddrs; i++) {
                
                g.opaque ca = _reg_caddrs.v[i];
                // unregister the contact address
                _resolver_ops.delete(_ohandle, ca, null);
            }
            DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
            "LR: number of caddrs unregistered: " + n_caddrs);
            
        } else {
            DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "quick destruction => " +
                                "leaving addresses in LS!");
        }
        
        try {
            // tell relevant subobjects to prepare for destruction

            // First let the replication subobject know, because then it can
            // send all connections a message (if !_destroy_quickly);
            if (_repl_subobj != null) {
                _repl_subobj.prepareDestruction(_destroy_quickly ?
                                                g.bool.True: g.bool.False);
            }
            
            
            if (_sec_subobj != null) {
                _sec_subobj.prepareDestruction(_destroy_quickly ?
                                                g.bool.True: g.bool.False);
            }
            
            if (_sem_subobj != null) {
                _sem_subobj.prepareDestruction();
            }
        } catch (Exception exc) {
            DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "LR cleanupLR() ignoring exception "+
                                exc.getMessage());
            exc.printStackTrace();
        }
    }
    
    /**
     * Looks up the PM in the local name space, and sets the
     * _perst_activation and _storage_mgr fields.
     *
     * @exception nsErrors_notFoundErr
     *    if the Globe runtime did have a PM
     * @exception Exception
     *       Globe exceptions during name space access and getInf calls.
     */
    private void lookupPerstMgr() throws Exception
    {
        SOInf soi = nameSpaceImp.getLNS().bind(getContext(),
        nsConst.PERST_MGR_NAME);
        
        _perst_activation = (activationRecord) soi.swapInf(activationRecord.infid);
        _storage_mgr = (storageManager) _perst_activation.soi.getUncountedInf(
        storageManager.infid);
    }

    public void setLRID(long lrid) {

        _lrid = lrid;

        // Tell security subobject
        _secSetLRID.setLRID(lrid);        
    }
}
