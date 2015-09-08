/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

/**
 * Deputy.java - Parent class of all Deputies.
 *
 * A subclass of Deputy is created for each object type that is implemented via
 * the n-subobject model. Such a subclass will implement all interfaces and
 * methods of the object type. An application programmer using the working 
 * object type will use this Deputy subclass to create and manage and use 
 * instances of that type. A deputy can be considered a (possibly persistent)
 * management reference for DSOs.
 *
 * Feel free to create subclasses of this class that extend its functionality,
 * i.e., facilitate the application developer's interaction with the Globe
 * middleware.
 *
 * See the definitions of the Deputy's interfaces for documentation.
 * 
 * @author  Arno Bakker
 */
package vu.gaia.lr.n;

import vu.gaia.rts.*;
import vu.globe.idlsys.g;

import vu.gaia.svcs.gns.NameService;
import vu.gaia.svcs.gns.NameServiceException;

import vu.gaia.svcs.gls.ObjectHandle;
import vu.gaia.svcs.gls.ContactAddress;
import vu.gaia.svcs.gls.CAPropertySelector;
import vu.globe.util.gen.IDGen;

import vu.gaia.svcs.objsvr.ObjectServerAddress;
import vu.gaia.svcs.objsvr.ObjectServer;
import vu.gaia.svcs.objsvr.ObjectServerException;

import vu.globe.rts.security.certs.*;
import vu.globe.rts.security.BaseKeyMaterial;
import vu.globe.rts.security.encoding.JarJar;
import vu.globe.rts.security.encoding.PEM;
import vu.globe.rts.security.idl.sectypes;
import vu.globe.rts.security.idl.sectypes.*;
import vu.globe.rts.security.idl.security.*;
import vu.globe.rts.security.idl.secconfig.*;
import vu.globe.rts.security.ReverseMethodIDMap;
import vu.globe.rts.security.CredentialEntry;
import vu.globe.rts.security.LRSecUtil;
import vu.globe.rts.comm.gwrap.P2PDefs;

import vu.globe.rts.std.idl.stdInf.SOInf;
import vu.globe.rts.lr.mgr.LRMgrConfig;
import vu.globe.rts.lr.mgr.CtrlConfig;
import vu.globe.rts.lr.replication.MasterSlave.MSConfig;
import vu.globe.rts.security.secGenericConfig;
import vu.globe.rts.lr.replication.auto.ARLRMgmtAddr;
import vu.globe.util.debug.DebugOutput;
import vu.globe.util.comm.idl.rawData.*;

import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Hashtable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import java.security.PublicKey;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateEncodingException;


public class Deputy implements DeputyMain, DeputyPersistence,
                               DeputyConfig, DeputyCertManagement

{
    // At present nonconfigurable. I assume it is default for the n-subobject
    // model.
    private static final String LRCONSTRUCTOR_IMPL = "JAVA;vu.globe.rts.lr.mgr.ControllingLRMgrCO";

    // When no DSO name is given, use this as temporary one.
    private static final String NO_DSO_NAME = "unnamed DSO";

    //
    // Non-static
    // 
    
    protected RTS               _rts=null;
    protected String            _oname=null;
    protected ObjectHandle      _oh=null;
    protected boolean           _persistentFirstReplica=false;
    protected int               _publicity=PUBLICITY_NONE;

    // list of ARLRMgmtAddrs,one entry for auto replicated,
    // many for manually replicated DSOs
    protected ArrayList         _replicas=null;

    // StdObject Interface as returned by bind, kept here to
    // enable proper garbage collection of LRs
    protected SOInf             _soi=null;
    protected Object            _proxy=null;

    // CREDS/REVOKE ADDED
    protected secCertManagement _secCertManager=null;

    // MOET
    // set by setSemantics()
    protected String                _sem_impl;
    protected ReverseMethodIDMap    _sem_revmap;

    protected ArrayList             _bps;

    // set by create()    
    protected KeyStore    _obj_ks=null;
    protected int         _sec_acb_semoffset=0;
    protected int         _sec_acb_reploffset=0;
    protected int         _sec_acb_nbits=0;
    protected IDLSignedBlueprints   _signedBPs;
    protected AccessCtrlBitmap      _public_facb=null;
    // index in _bps which indicates what type of LR should be created by
    // an address space that wants to use the object's public access
    // method.
    //
    protected int                   _public_client_bp_idx;

    /* Deputies can be used by the owner of the object or by someone else.
     * - If we're owner we use a user certificate generated in create().
     *   This is more convenient than using the root admin certificate.
     * - If we're not owner and the object is private, the owner will set our
     *   permissions at the master, who will generate a rights certificate for 
     *   us based on our public key of our identity certificate during binding.
     * - If we're not owner and the object is public, we will receive a 
     *   user rights certificate during binding.
     */
    protected IDLObjectCreds        _objCreds;   // storage for priv+chain
    protected boolean               _added_objCreds_to_secctx=false;
  
    
    // PEDIGREE
    protected IDLIdentCerts         _obj_ped_chain;

    // Second-level usability
    protected int                   _initial_bp_idx; // index in _bps
    protected int                   _addrepl_bp_idx; // index in _bps
    protected int                   _client_bp_idx;  // index in _bps

    // If security is disabled, tell us if this is a client deputy
    private boolean _safeBPS = false;
    

    /**
     * Default constructor
     */
    public Deputy()
    {
        init();
    }

   
    /**
     * Copy constructor used for ``blind binding'', that is, when you bind
     * to an object whose type you do not yet know. The idea is to first
     * create a Deputy (of class Deputy), bind() and then upgrade this
     * deputy to the class of Deputy that is associated with the object
     * type (e.g. PackageDeputy).
     * 
     * This constructor should be used only for this purpose. If you want
     * a full copy, use clone(). After this operation you can no longer use 
     * the source deputy for unbinding.
     * 
     * See also: getObjectInterfaces()
     *
     */
    public Deputy( Deputy D )
    {
    	init();
        _oname = D._oname;
        _oh = D._oh;
        _soi = D._soi;
        _proxy = D._proxy;
        _secCertManager = D._secCertManager;      // CREDS/REVOKE ADDED
        
        // The new deputy takes over, so make sure that old deputy won't
        // unbind in finalize()
        D._soi = null;
        D._proxy = null;
        D._secCertManager = null;
        
        _objCreds = D._objCreds;
        _added_objCreds_to_secctx = D._added_objCreds_to_secctx;
    }
      

    protected void finalize()
    {
        unbind();
    }
    
    
    private void init()
    {
        _rts = RTS.getInstance();
        _replicas = new ArrayList();
        _bps = new ArrayList();
    }
   

    /********** DeputyConfig interface ************/

    //
    // Second level usability
    //

    public void setSemantics(String ImplHandle) throws RTSException
    {
        setSemantics( ImplHandle, null );
    }

    public void setSemantics(String ImplHandle, String CodeBaseStr ) throws RTSException
    {
        _sem_impl = "PUREJAVA;"+ImplHandle;
        // BVD
        if (CodeBaseStr != null)
            _sem_impl += "@"+CodeBaseStr;

        // Determine the reverse method ID map for this semantics subobject.
        _sem_revmap = ReverseMethodIDMap.getFromSemantics( ImplHandle );

        setDefaultWorkingObjectType();

        if (!_rts.isSecurityEnabled()) {
            setDisableSecurity();
        }
    }

     /**  ISSUE:
     In some replication protocols, such as our current master/slave,
     there is a division of which replica can execute which methods.
     In our current protocol, the slave executes only read methods (and the
     master executes both read methods and write methods). More specifically,
     slaves cannot even execute write methods, they don't have the code to
     coordinate this update with the other replicas. This means that the
     replication protocol chosen places a restriction on the assignment
     of permissions to execute a semantics methods. A slave cannot be
     assigned permission to execute a write for a client, because it
     can't handle the write.

     I accomodate this situation by defining the default working object type
     only after the semantics subobject has been set. When defining the
     blueprints we can then take into account the properties of the
     replication protocol when assigning semantics-methods permissions
     to specific LRs.
      */
    /* protected */ public void setDefaultWorkingObjectType()
    {
        AccessCtrlBitmap[] sec_req_acbs;
        int idx=0;

        // MOET
        // Dummy required bitmaps for semantics subobject, proper permissions
        // are set in setReplicationProtocol() as much as possible.
        //
        AccessCtrlBitmap sem_req_facb = new AccessCtrlBitmap( _sem_revmap.getList().size() );
        AccessCtrlBitmap sem_req_racb = new AccessCtrlBitmap( _sem_revmap.getList().size() );

        // Init string for control subobject is the reverse method ID map
        // of the semantics subobject and the offset of both the SEC and REPL.
        
        String ctrl_init = CtrlConfig.createConfig(
                                    (secGenericConfig.NUMBER_OF_SEC_METHODS
                                     + MSConfig.NUMBER_OF_REPLICATION_METHODS),
                                    _sem_revmap.toString());
        // Initial / master
        _initial_bp_idx = idx;
        setSubobject( idx, SEM_SUBOBJ,  _sem_impl, null, sem_req_facb, sem_req_facb );
        setSubobject( idx, REPL_SUBOBJ, null, null, null, null ); // set by setReplicationProtocol()
        setSubobject( idx, COMM_SUBOBJ, null, null, null, null ); // null just for master
        setSubobject( idx, CTRL_SUBOBJ, "you", ctrl_init, null, null );
        sec_req_acbs = secGenericConfig.getSecurityRequirements( 
				                 secGenericConfig.MASTER_ROLE);
        setSubobject( idx, SEC_SUBOBJ, secGenericConfig.SEC_GENERIC_IMPL, 
		           secGenericConfig.SEC_MASTER_INIT, sec_req_acbs[0],
		           sec_req_acbs[1] );
        idx++;

      
        // client
        _client_bp_idx = idx;
        _public_client_bp_idx = idx; // subtly different
        setSubobject( idx, SEM_SUBOBJ,  _sem_impl, null, sem_req_facb, sem_req_facb );
        setSubobject( idx, REPL_SUBOBJ, null, null, null, null ); // set by setReplicationProtocol()
        setSubobject( idx, COMM_SUBOBJ, P2PDefs.LIGHT_TCP_SEC_CONR_IMPL, 
                      P2PDefs.LIGHT_TCP_SEC_CONR_INIT, null, null );
        setSubobject( idx, CTRL_SUBOBJ, "you", ctrl_init, null, null );
        sec_req_acbs = secGenericConfig.getSecurityRequirements( 
                                                secGenericConfig.CLIENT_ROLE);
        setSubobject( idx, SEC_SUBOBJ, secGenericConfig.SEC_GENERIC_IMPL,
                           secGenericConfig.SEC_CLIENT_INIT, sec_req_acbs[0],
                           sec_req_acbs[1] );
        idx++;

      
        // addReplica / slave
        _addrepl_bp_idx = idx;
        setSubobject( idx, SEM_SUBOBJ,  _sem_impl, null, sem_req_facb, sem_req_facb );
        setSubobject( idx, REPL_SUBOBJ, null, null, null, null ); // set by setReplicationProtocol()
        setSubobject( idx, COMM_SUBOBJ, P2PDefs.LIGHT_TCP_SEC_CONR_IMPL,
                      P2PDefs.LIGHT_TCP_SEC_CONR_INIT, null, null );
        setSubobject( idx, CTRL_SUBOBJ, "you", ctrl_init, null, null );
        sec_req_acbs = secGenericConfig.getSecurityRequirements(
                                                  secGenericConfig.SLAVE_ROLE);
        setSubobject( idx, SEC_SUBOBJ, secGenericConfig.SEC_GENERIC_IMPL,
                           secGenericConfig.SEC_SLAVE_INIT, sec_req_acbs[0],
                           sec_req_acbs[1] );
      
      
        idx++;

        setReplicationProtocol( REPL_MASTER_SLAVE, "" );
    }

    public void setDisableSecurity() {

        IDLBlueprint bp = null;
        DebugOutput.println(DebugOutput.DBG_NORMAL, 
                "Deputy: DISABLING TLS, AccessControl and certificates !");
            
        // change the blueprints' security subobject init string
        bp = (IDLBlueprint) _bps.get(_initial_bp_idx);
        if (bp != null) {
            bp.subobjs.v[SEC_SUBOBJ].init =
                                        secGenericConfig.SEC_OFF_MASTER_INIT;
        }
            
        bp = (IDLBlueprint) _bps.get(_client_bp_idx);
        if (bp != null) {
            bp.subobjs.v[SEC_SUBOBJ].init =
                                        secGenericConfig.SEC_OFF_CLIENT_INIT;
        }
        
        bp = (IDLBlueprint) _bps.get(_addrepl_bp_idx);
        if (bp != null) {
            bp.subobjs.v[SEC_SUBOBJ].init =
                                        secGenericConfig.SEC_OFF_SLAVE_INIT;
        }
    }



    
    public void setReplicationProtocol( String PolicyAbbrev, String PolicyParams )
    throws IllegalArgumentException
    {
        if (PolicyAbbrev.equals( REPL_CLIENT_SERVER ))
        {
            // Client/Server is implemented as a degenerate case of Master/Slave
            AccessCtrlBitmap[] req_acbs = MSConfig.getSecurityRequirements( MSConfig.CS, MSConfig.MASTER_ROLE );
            setSubobject(_initial_bp_idx, REPL_SUBOBJ, 
                         MSConfig.IMPLHANDLE_MASTER_SLAVE_REPL_MASTER, 
                         MSConfig.createMasterConfig(
                                secGenericConfig.NUMBER_OF_SEC_METHODS, true),
                         req_acbs[0], 
                         req_acbs[1]);
            req_acbs = MSConfig.getSecurityRequirements(MSConfig.CS, 
                                                        MSConfig.CLIENT_ROLE);
            setSubobject(_client_bp_idx, REPL_SUBOBJ, 
                         MSConfig.IMPLHANDLE_MASTER_SLAVE_REPL_CLIENT, 
                         MSConfig.createClientConfig(
                                 secGenericConfig.NUMBER_OF_SEC_METHODS, false),
                         req_acbs[0], 
                         req_acbs[1]);
            
            // HACK: Remove slave entry configured in setWorkingObjectType()
            _bps.remove( _addrepl_bp_idx );

        
            // Next we determine which semantics methods the server LR is 
            // allowed to AND CAN (see ISSUE above) execute on behalf of 
            // clients. In case of client/server replication
            // this is all methods.
            //
            AccessCtrlBitmap sem_server_req_facb = new AccessCtrlBitmap( _sem_revmap.getList().size() );
            AccessCtrlBitmap sem_server_req_racb = new AccessCtrlBitmap( _sem_revmap.getList().size() );
            // Server can be asked to perform any method,
            // cannot invoke one itself, though.
            sem_server_req_facb.clearAllPermissions(); 
            sem_server_req_racb.setAllPermissions(); 
            setSubobject( _initial_bp_idx, SEM_SUBOBJ, _sem_impl, null, sem_server_req_facb, sem_server_req_racb );

        }
        else if (PolicyAbbrev.equals( REPL_MASTER_SLAVE) || PolicyAbbrev.equals( REPL_MASTER_SLAVE_AUTO) )
        {
            String master_init=null;
            String slave_init = null;
            String client_init = null;
            if (PolicyAbbrev.equals( REPL_MASTER_SLAVE_AUTO))
            {
                // AUTOREPL
                if (PolicyParams == null) {
                    throw new IllegalArgumentException(
                                    "invalid protocol parameters");
                }
                master_init = MSConfig.createMasterConfig(
                                        secGenericConfig.NUMBER_OF_SEC_METHODS,
                                        false, PolicyParams);
                slave_init = MSConfig.createSlaveConfig(
                                        secGenericConfig.NUMBER_OF_SEC_METHODS,
                                        PolicyParams);
                client_init = MSConfig.createClientConfig(
                                        secGenericConfig.NUMBER_OF_SEC_METHODS,
                                        true);
            } else {
                master_init = MSConfig.createMasterConfig(
                                        secGenericConfig.NUMBER_OF_SEC_METHODS,
                                        false);
                slave_init = MSConfig.createSlaveConfig(
                                        secGenericConfig.NUMBER_OF_SEC_METHODS);
                client_init = MSConfig.createClientConfig(
                                        secGenericConfig.NUMBER_OF_SEC_METHODS,
                                        false);
            }

            
            AccessCtrlBitmap[] req_acbs = MSConfig.getSecurityRequirements( MSConfig.MS, MSConfig.MASTER_ROLE );
            setSubobject( _initial_bp_idx, REPL_SUBOBJ, 
                    MSConfig.IMPLHANDLE_MASTER_SLAVE_REPL_MASTER, 
                    master_init,
                    req_acbs[0], 
                    req_acbs[1] );
            req_acbs = MSConfig.getSecurityRequirements( MSConfig.MS, MSConfig.CLIENT_ROLE );
            setSubobject( _client_bp_idx, REPL_SUBOBJ, 
                    MSConfig.IMPLHANDLE_MASTER_SLAVE_REPL_CLIENT, 
                    client_init,
                    req_acbs[0], 
                    req_acbs[1] );
            req_acbs = MSConfig.getSecurityRequirements( MSConfig.MS, MSConfig.SLAVE_ROLE );
            setSubobject( _addrepl_bp_idx, REPL_SUBOBJ, 
                    MSConfig.IMPLHANDLE_MASTER_SLAVE_REPL_SLAVE, 
                    slave_init,
                    req_acbs[0], 
                    req_acbs[1] );

            // Next we determine which semantics methods the various LR types  
            // are allowed to AND CAN (see ISSUE above) execute on behalf of 
            // clients. In case of our master/slave replication protocol
            // the master is allowed to execute all methods, and slaves
            // can only execute read methods. Hence we only assign them
            // permissions to execute reads.
            //
            AccessCtrlBitmap sem_master_req_facb = new AccessCtrlBitmap( _sem_revmap.getList().size() );
            AccessCtrlBitmap sem_master_req_racb = new AccessCtrlBitmap( _sem_revmap.getList().size() );
            sem_master_req_facb.clearAllPermissions(); 
            sem_master_req_racb.setAllPermissions(); 
            setSubobject( _initial_bp_idx, SEM_SUBOBJ, _sem_impl, null, sem_master_req_facb, sem_master_req_racb );

            // Give permission to execute only read-only methods on behalf 
            // of clients
            AccessCtrlBitmap sem_slave_req_facb = new AccessCtrlBitmap( _sem_revmap.getList().size() );
            sem_slave_req_facb.clearAllPermissions(); // slave can't request exec of semantics methods
            AccessCtrlBitmap sem_slave_req_racb = _sem_revmap.getReadOnlyRACB();

            setSubobject( _addrepl_bp_idx, SEM_SUBOBJ, _sem_impl, null, sem_slave_req_facb, sem_slave_req_racb );

        }
        else
            throw new IllegalArgumentException("unknown policy abbreviation or policy unavailable (e.g. because security is off)");
    }



    
    /*
     * First level methods for configuring a deputy. These methods use 
     * no knowledge about replication protocols supplied with Globe or
     * relationships that exist between the configuration of subobjects
     * (e.g. bitmaps)
     */
    
    public void setSubobject( int WhichBP, int WhichSub, String ImplHandle, String InitData, AccessCtrlBitmap ReqFACB, AccessCtrlBitmap ReqRACB )
    {
        IDLBlueprint bp = null;
        if (_bps.size() > WhichBP)
            bp = (IDLBlueprint)_bps.get( WhichBP );
        if (bp == null)
        {
            // add first
            bp = new IDLBlueprint();
            bp.lrmgr_impl = LRCONSTRUCTOR_IMPL;
            bp.subobjs = new IDLSubobjectBlueprints( N_SUBOBJS );
            for (int i=0; i<N_SUBOBJS; i++)
            {
                bp.subobjs.v[i] = new IDLSubobjectBlueprint();
                bp.subobjs.v[i].requiredFACB = new IDLAccessCtrlBitmap( 0 );
                bp.subobjs.v[i].requiredRACB = new IDLAccessCtrlBitmap( 0 );
            }
            _bps.add( WhichBP, bp );
        }
        bp.subobjs.v[ WhichSub ].impl = ImplHandle;
        bp.subobjs.v[ WhichSub ].init = InitData;
        if (ReqFACB != null)
            bp.subobjs.v[ WhichSub ].requiredFACB.v = ReqFACB.toByteArray();
        if (ReqRACB != null)
            bp.subobjs.v[ WhichSub ].requiredRACB.v = ReqRACB.toByteArray();
    }
    
    
    
    public void setInitialReplicaBlueprint( int WhichBP )
    {
        _initial_bp_idx = WhichBP;
    }
    

    public void setAddReplicaBlueprint(int WhichBP)
    {
        _addrepl_bp_idx = WhichBP;
    }
    

    
    public void setInitialReplicaPersistent( boolean Val )
    {
        _persistentFirstReplica=Val;
    }

    
    public void clearBlueprints()
    {
        _bps.clear();
    }

    
    

    /********* DeputyMain interface **********/
    
    public void setObjectName( String ObjectName )
    throws NameServiceException.NameSyntaxException
    {
        // TODO: check GNS syntax here?
        _oname = ObjectName; 
    }
    
   
    public String getObjectName()
    {
        return _oname;
    }
   

    /**
     * Sets the DSO's object handle as it will be registered in the Globe 
     * Location Service.
     * Must be called before <code>create()</code>.
     */
    public void setObjectHandle( ObjectHandle OH )
    {
        // TODO: should check if _owned true, in which case OH should be a 
        // V2 OH. FIXME This was in bleeding edge, still holds ?
        _oh = OH;
    }


    public ObjectHandle getObjectHandle()
    {
        return _oh;
    }
    

    public void setPublicity( int Publicity )
    {
        _publicity = Publicity;
    }
    

    public int getPublicity()
    {
        return _publicity;
    }

    
    public void create( String ObjectName, ObjectServerAddress OSA )
    throws NameServiceException.NameSyntaxException,
            NameServiceException,
            ObjectServerException.IllegalServerAddressException,
            ObjectServerException.ServerRefusedConnectionException,
            ObjectServerException.DSOExistsException,
            ObjectServerException.UnknownImplHandleException,
            ObjectServerException.CannotCreateLRException,
            ObjectServerException.UnauthorizedOperationException,
            ObjectServerException,
            RTSException
    {
        setObjectName( ObjectName );
        create( OSA );
    }
    
    
    /** Must be totally replication protocol independent! */
    public void create( ObjectServerAddress OSA )
    throws  ObjectServerException.IllegalServerAddressException,
            ObjectServerException.ServerRefusedConnectionException,
            ObjectServerException.DSOExistsException,
            ObjectServerException.UnknownImplHandleException,
            ObjectServerException.CannotCreateLRException,
            ObjectServerException.UnauthorizedOperationException,
            ObjectServerException,
            RTSException
    {
        if (!_rts.isSecurityEnabled()) {
            _safeBPS = true;
        }
            
        String tempname=_oname;
        if (tempname == null)
            tempname = NO_DSO_NAME;
   
        // MOET        
        /*
         * Step 1: Determine access ctrl bitmap sizes.
         */
        IDLBlueprint initial_bp = (IDLBlueprint)_bps.get( _initial_bp_idx );
        int sec_num_methods = (new AccessCtrlBitmap( initial_bp.subobjs.v[ SEC_SUBOBJ ].requiredFACB.v )).length();
        int repl_num_methods = (new AccessCtrlBitmap( initial_bp.subobjs.v[ REPL_SUBOBJ ].requiredFACB.v )).length();
        int sem_num_methods = (new AccessCtrlBitmap( initial_bp.subobjs.v[ SEM_SUBOBJ ].requiredFACB.v )).length();
        // Security methods must come first, otherwise update 
        // PropBitGetBlueprintsMethod def in sectypes.idl
        _sec_acb_nbits = sec_num_methods + repl_num_methods + sem_num_methods; 
        _sec_acb_reploffset = sec_num_methods;
        _sec_acb_semoffset = sec_num_methods + repl_num_methods;
        
        /*
         * Step 2: Create requirement masks to be used in
         * blueprint selection during binding (doesn't really apply
         * to initial replicas, but anyway.
         */
        for (int i=0; i<_bps.size(); i++)
        {
            IDLBlueprint bp = (IDLBlueprint)_bps.get( i );
            AccessCtrlBitmap[] acbs = composeBitmaps( i );
            // There are no semantics bits on in requirement masks
//            acbs[0].clearRange( _sec_acb_semoffset, _sec_acb_nbits );
//            acbs[1].clearRange( _sec_acb_semoffset, _sec_acb_nbits );
            bp.requiredFACB = new IDLAccessCtrlBitmap( 0 );
            bp.requiredFACB.v = acbs[0].toByteArray();
            bp.requiredRACB = new IDLAccessCtrlBitmap( 0 );
            bp.requiredRACB.v = acbs[1].toByteArray();
        }

        
        
        /*
         * Step 3: (if security) Generate key pairs and rights certs for object
         * and its initial replica. Also generate a pedigree chain, i.e.,
         * a certificate chain that links an object to a identity certificate
         * of an owner (e.g. a person).
         */
        KeyStore initial_ks = null;
        KeyStore initial_admin_ks = null;
        long certSerialNumber = sectypes.invalidSerialNumber;
        if (_rts.isSecurityEnabled())
        {
            /* 
             * Step 3a: Create object credentials
             */
            _obj_ks = RightsCertUtil.generateObjectCredentials( _sec_acb_nbits );

            _oh = generateObjectHandle( _obj_ks );

            /*
             * 3b: Sign blueprints with object private key.
             */
            _signedBPs = new IDLSignedBlueprints( _bps.size() );
            for (int i=0; i<_bps.size(); i++)
            {
                IDLBlueprint bp = (IDLBlueprint)_bps.get( i );
                _signedBPs.v[i] = LRSecUtil.signBlueprint( _obj_ks, bp );
            }

            /*
             * Step 3c: Create pedigree for this object, i.e., link an owner 
             * with the object such that object servers, etc. can decide if 
             * they like the object based on who owns it.
             */
            BaseKeyMaterial owner_bkm = _rts.getBaseKeyMaterial();
            _obj_ped_chain = IdentCertUtil.generateObjectPedigreeChain( owner_bkm, _obj_ks );

	  
            /*
             * Step 3d: Determine the public rights for this object
             */
            // HACK, will need to be resolved when we properly define
            // public methods.
            IDLBlueprint client_bp = (IDLBlueprint)_bps.get( _public_client_bp_idx );
            _public_facb = new AccessCtrlBitmap( client_bp.requiredFACB.v );
            if (_publicity == PUBLICITY_ALL)
            {
                // Give permission to execute any method on behalf of clients
                _public_facb.setRange( _sec_acb_semoffset, _sec_acb_nbits );
            }
            else if (_publicity == PUBLICITY_ALLREAD)
            {
                // Give permission to invoke only read-only methods
                // Note: RACB/FACB are not typos!
                AccessCtrlBitmap sem_facb = _sem_revmap.getReadOnlyRACB();
                _public_facb.setFromBitmap( _sec_acb_semoffset, sem_facb );
            }
            
	  
            /* 
             * Step 3e: Create access ctrl bitmaps for initial replica
             */
            AccessCtrlBitmap[] initial_acbs = composeBitmaps( _initial_bp_idx );
            
            /*
             * 3f: Create replica credentials
             */
            initial_ks = RightsCertUtil.generateReplicaCredentials( _obj_ks, initial_acbs[0], initial_acbs[1] );

            // Store the serial number of this certificate (NOT VERY NICE)
            try {
                certSerialNumber = RightsCertUtil.getSerialNumber(
                                        initial_ks.getCertificateChain(
                                            RightsCertUtil.DEFAULT_KEY_ALIAS));
            } catch (Exception e) {
                System.err.println("ERROR: could not get serial number");
                e.printStackTrace();
            }

            // 3g: Create access ctrl bitmaps of the client that we may give.
            AccessCtrlBitmap[] client_acbs = composeBitmaps(
                                                    _public_client_bp_idx);
            
            // 3h: Create replica admin credentials. This is used for the
            // master to be able to create new client (and slave) certificates.
            // For now only client FIXME or bits of slave as well when combined
            KeyPair initial_kp = RightsCertUtil.getKeyPairFromKeyStore( initial_ks );
            Certificate[] initial_admin_chain = null;
            initial_admin_chain = RightsCertUtil.generateAdminCertificateChain( 
                                    initial_kp.getPublic(), _obj_ks,
                                    _public_facb, 
                                    new AccessCtrlBitmap( _sec_acb_nbits ), 
                                    false );
            initial_admin_ks = RightsCertUtil.copyKeyStoreSetChain( initial_ks, 
                                                        initial_admin_chain );
            
            /*
             * 3i. Create user certificate for the owner. If the owner wants
             * to use the object it is not convenient to use the object root
             * credentials in the current implementation. At present, the blueprint
             * used to construct the local LR during binding is selected based
             * on the certificate the binding user has. If we use the root
             * admin certificate, we may wind up with a blueprint for a slave
             * replica.
             *
             * We give the owner a user certificate with ALL permission.
             * The owner is also the only one allowed to do UPDATE credentials,
             * REVOKE certificate and get clients list requests.
             */
            // stupid clone() interface.
            AccessCtrlBitmap owner_facb =(AccessCtrlBitmap)_public_facb.clone();

            // Set ALL permission
            owner_facb.setRange( _sec_acb_semoffset, _sec_acb_nbits );

            // Set Owner privileged bits (Update Credentials and Revoke Certs)
            secGenericConfig.setOwnerPrivileges(owner_facb);
            
            PublicKey owner_pub = owner_bkm.getPublicKey();
            
            // Generate a keypair specifically for the use of the rights cert
            // util as not to wear the owner's key out.
            KeyPair kP = RightsCertUtil.generateKeyPair();
            
            // Create the owner chain with the new pubkey
            Certificate[] owner_chain = 
                        RightsCertUtil.generateUserCertificateChain( _obj_ks, 
                                                 owner_facb, kP.getPublic());

            // Create the idl object creds
            _objCreds = new IDLObjectCreds();
            _objCreds.priv = new IDLPrivateKey(0);
            _objCreds.priv.v = kP.getPrivate().getEncoded();
            _objCreds.chain = RightsCertUtil.javaToIDLRightsCerts(owner_chain);

            // This will be added to the sec context at doBind();
        }
        else
        {
            // No security
            _oh = generateObjectHandle( null );
        }
        
        /*
         * Step 4: Create JAR
         */
        byte[] jar_bytes =null;
        try
        {
            JarJar jar=null;
            
            // TODO: write keys to JAR in non-Java format
            // TODO: turn jar into parameters to createLR as much as possible.
            //       All depends on how management requests to object server 
            //       are protected and what authenticating it provides.
            //
            if (_rts.isSecurityEnabled()) {

                jar = LRSecUtil.createSignedJar( _obj_ks );
                LRSecUtil.writeKeyStoreToSignedJar(
                                    LRSecUtil.JAR_CREDS_KEYSTORE_FNAME, 
                                    initial_ks, jar );
                LRSecUtil.writeKeyStoreToSignedJar(
                                    LRSecUtil.JAR_ADMIN_CREDS_KEYSTORE_FNAME, 
                                    initial_admin_ks, jar);

                for (int i=0; i<_signedBPs.v.length; i++)
                {
                    String fname = (i == _initial_bp_idx) ? 
                        LRSecUtil.JAR_TOCREATEBP_FNAME : 
                        ("blueprint"+i+LRSecUtil.JAR_BLUEPRINT_FNAMEPOSTFIX);
                    ByteArrayInputStream bais = new ByteArrayInputStream(
                                                        _signedBPs.v[i].v);
                    jar.addStream(fname, bais);
                }
                LRSecUtil.writePublicFACBToJar( _public_facb, jar );

                KeyStore ped_ks = IdentCertUtil.idlIdentCertsToKeyStore(
                                                        _obj_ped_chain);
                LRSecUtil.writeKeyStoreToSignedJar(
                            LRSecUtil.JAR_PEDIGREE_KEYSTORE_FNAME,ped_ks,jar);
            } else {

                jar = new JarJar();

                for (int i=0; i<_bps.size(); i++)
                {
                    // HACK HACK, only send the to createbp and the clientBP !
                    if (i == _addrepl_bp_idx) {
                        continue;
                    }

                    IDLBlueprint bp = (IDLBlueprint)_bps.get( i );
                    String fname = (i == _initial_bp_idx) ? 
                        LRSecUtil.JAR_TOCREATEBP_FNAME : 
                        ("blueprint"+i+LRSecUtil.JAR_BLUEPRINT_FNAMEPOSTFIX);
                    ByteArrayInputStream bais = new ByteArrayInputStream(
                                LRSecUtil.idlBlueprintToString(bp).getBytes());
                    jar.addStream(fname, bais);
                }
            }

            jar.close();
            jar_bytes = jar.toByteArray();
        }
        catch( IOException e )
        {
            throw new RTSException( e );
        }

        
        /*
         * Step 5: Contact object server
         */
        
        ObjectServer os=getObjectServer( OSA );
        long lrid = os.createLR( tempname, _oh, jar_bytes, _persistentFirstReplica );
        
        ARLRMgmtAddr ma = new ARLRMgmtAddr( OSA.getHostPortPair(), lrid, certSerialNumber);
        _replicas.add( 0, ma );
        // if we want to store replica info in DSO, we could secretly bind here.
        
        /*
         * Step 6: Register object name in GNS (if set)
         */
        if (_oname != null)
        {
        
            try
            {
                // overwrite if already set
                _rts.getNameService().registerObjName( _oname, _oh, true );
            }
            catch (NameServiceException.AlreadyRegisteredException e)
            {
                throw new RTSException(e);
            }
        }
    }
    

    
    
    private ObjectHandle generateObjectHandle( KeyStore KS )
    throws RTSException
    {
        // also works when KS is null
        byte[] digest = null;
        if (KS != null) {
            digest = RightsCertUtil.getDigestOfPublicKey( KS );
        }
        ObjectHandle oh = _rts.getLocationService().genObjectHandle( digest );
        return oh;
    }
    

    private AccessCtrlBitmap[] composeBitmaps( int WhichBP )
    {
        IDLBlueprint bp = (IDLBlueprint)_bps.get( WhichBP ); // must be there
        
        AccessCtrlBitmap facb = new AccessCtrlBitmap( _sec_acb_nbits );
        AccessCtrlBitmap racb = new AccessCtrlBitmap( _sec_acb_nbits );

        // i. Security methods: 
        facb.setFromBitmap( 0, new AccessCtrlBitmap( bp.subobjs.v[ SEC_SUBOBJ ].requiredFACB.v ) );
        racb.setFromBitmap( 0, new AccessCtrlBitmap( bp.subobjs.v[ SEC_SUBOBJ ].requiredRACB.v ) );
        // ii. Replication ``methods'' (i.e. protocol messages)
        facb.setFromBitmap( _sec_acb_reploffset, new AccessCtrlBitmap( bp.subobjs.v[ REPL_SUBOBJ ].requiredFACB.v ) );
        racb.setFromBitmap( _sec_acb_reploffset, new AccessCtrlBitmap( bp.subobjs.v[ REPL_SUBOBJ ].requiredRACB.v ) );
        // iii. Semantics methods: 
        facb.setFromBitmap( _sec_acb_semoffset, new AccessCtrlBitmap( bp.subobjs.v[ SEM_SUBOBJ ].requiredFACB.v ) );
        racb.setFromBitmap( _sec_acb_semoffset, new AccessCtrlBitmap( bp.subobjs.v[ SEM_SUBOBJ ].requiredRACB.v ) );
        
        return new AccessCtrlBitmap[] { facb, racb };
    }
    
    
    private ObjectServer getObjectServer( ObjectServerAddress OSA )
    throws RTSException, ObjectServerException
    {
        ObjectServer os = null;
        if (_rts.isSecurityEnabled())
        {
            BaseKeyMaterial bkm = _rts.getBaseKeyMaterial();
            os = new ObjectServer( OSA, _obj_ks, bkm.caKeyStore );
        }
        else
            os = new ObjectServer( OSA );
        
        os.connect();
        
        return os;
    }

    
    public void delete()
    throws NameServiceException.NotRegisteredException,
            NameServiceException,
            ObjectServerException.IllegalServerAddressException,
            ObjectServerException.ServerRefusedConnectionException,
            ObjectServerException.UnknownLRException,
            ObjectServerException,
            RTSException
    {
        RTSException rtse=null;
        
        // 0. 
        unbind();
        
        // 1. Unregister from GNS, if registered
        if (_oname != null)
        {
            try
            {
                _rts.getNameService().deregisterObjName( _oname );
            }
            catch (RTSException e)
            {
                // postpone until object removed
                rtse = e;
            }
        }
        
        // 2. Delete replicas, starting with any manually created ones, and
        // ending first the first (which has index 0 in _replicas).
        for (int i=_replicas.size()-1; i>=0; i--)
        {
            ARLRMgmtAddr ma = (ARLRMgmtAddr)_replicas.get( i );
            ObjectServerAddress osa = new ObjectServerAddress( ma.objSvrAddrString );
            ObjectServer os = getObjectServer( osa );
            os.deleteLR( ma.lrid );
        }
        
        if (rtse != null)
            throw rtse;
    }
    
    

    public void addReplica( ObjectServerAddress OSA, boolean Persistent )
    throws ObjectServerException.IllegalServerAddressException,
            ObjectServerException.ServerRefusedConnectionException,
            ObjectServerException.NoContactAddressFoundException,
            ObjectServerException.NoWorkingContactAddressFoundException,
            ObjectServerException.CannotCreateLRException,
            ObjectServerException.UnauthorizedOperationException,
            ObjectServerException,
            RTSException
    {

        // check if we're auto replicated
        //if (isAutomaticallyReplicated())
        //    throw new UnsupportedOperationException(
        //              "sorry, this DSO uses automatic replication." );
        
        // You should have set the blueprint to use in addReplica commands
        // via the setAddReplicaBlueprint() method in the DeputyConfig
        // interface.
        //
        // I use the signed blueprint, as this reduces the amount of data to
        // save when making a deputy persistent.
 
        if (!_rts.isSecurityEnabled()) {
            _safeBPS = true;
        }
       
        IDLSignedBlueprint replica_sbp = null;
        IDLBlueprint replica_bp = null;
        AccessCtrlBitmap replica_facb = null;
        AccessCtrlBitmap replica_racb = null;
        
        if (_rts.isSecurityEnabled()) {
            replica_sbp = _signedBPs.v[ _addrepl_bp_idx ];
            if (replica_sbp == null) {
                throw new RTSException(
                                "deputy: You should have set the blueprint "+
                                   "to use in addReplica commands via the "+
                                   "setAddReplicaBlueprint() method in the "+
                                   "DeputyConfig interface," );
            }
            
            replica_bp = LRSecUtil.getBlueprintFromSigned(replica_sbp);
            replica_facb = new AccessCtrlBitmap(replica_bp.requiredFACB.v);
            replica_racb = new AccessCtrlBitmap(replica_bp.requiredRACB.v);
        }
        
        // Create JAR
        long certSerialNumber = sectypes.invalidSerialNumber;
        byte[] jar_bytes =null;
        try
        {
            JarJar jar = null;
            if (_rts.isSecurityEnabled())
            {
                // Generate credentials for replica
                KeyStore replica_ks = RightsCertUtil.generateReplicaCredentials( _obj_ks, replica_facb, replica_racb );
                
                // Store the serialNumber
                try {
                    certSerialNumber = RightsCertUtil.getSerialNumber(
                                            replica_ks.getCertificateChain(
                                            RightsCertUtil.DEFAULT_KEY_ALIAS));
                } catch (Exception e) {
                    // pFF can't do anything..
                    System.err.println("ERROR: Could not get serial number");
                    e.printStackTrace();
                }

                jar = LRSecUtil.createSignedJar( _obj_ks );
                LRSecUtil.writeKeyStoreToSignedJar( LRSecUtil.JAR_CREDS_KEYSTORE_FNAME, replica_ks, jar );
                for (int i=0; i<_signedBPs.v.length; i++)
                {
                    if (i == _initial_bp_idx)
                        continue;
                    String fname = (i == _addrepl_bp_idx) ? LRSecUtil.JAR_TOCREATEBP_FNAME : ("blueprint"+i+LRSecUtil.JAR_BLUEPRINT_FNAMEPOSTFIX) ;
                    ByteArrayInputStream bais = new ByteArrayInputStream( _signedBPs.v[i].v );
                    jar.addStream( fname, bais );
                }
                LRSecUtil.writePublicFACBToJar( _public_facb, jar );
                
                KeyStore ped_ks = IdentCertUtil.idlIdentCertsToKeyStore( _obj_ped_chain );
                LRSecUtil.writeKeyStoreToSignedJar( LRSecUtil.JAR_PEDIGREE_KEYSTORE_FNAME, ped_ks, jar );
            }
            else
            {
                jar = new JarJar();
                for (int i=0; i<_bps.size(); i++)
                {
                    if (i == _initial_bp_idx)
                        continue;

                    IDLBlueprint bp = (IDLBlueprint)_bps.get( i );
                    String fname = (i == _addrepl_bp_idx) ? LRSecUtil.JAR_TOCREATEBP_FNAME : ("blueprint"+i+LRSecUtil.JAR_BLUEPRINT_FNAMEPOSTFIX) ;
                    ByteArrayInputStream bais = new ByteArrayInputStream( LRSecUtil.idlBlueprintToString( bp ).getBytes() );
                    jar.addStream( fname, bais );
                }
            }

            jar_bytes = jar.toByteArray();
        }
        catch( IOException e )
        {
            throw new RTSException( e );
        }

        String tempname = _oname;
        if (tempname == null)
            tempname = NO_DSO_NAME;
        
        long lrid = -1;
        ObjectServer os = getObjectServer( OSA );
        lrid = os.bind( _oh, _oname, jar_bytes, Persistent );
        
        ARLRMgmtAddr ma = new ARLRMgmtAddr( OSA.getHostPortPair(), lrid, 
                                            certSerialNumber);
        _replicas.add( ma );
    }
    
    
    public void deleteReplica( ObjectServerAddress OSA, long certSerialNumber)
    throws IllegalArgumentException, 
            ObjectServerException.IllegalServerAddressException,
            ObjectServerException.ServerRefusedConnectionException,
            ObjectServerException.UnknownLRException,
            ObjectServerException,
            RTSException
    {
        //if (isAutomaticallyReplicated())
        //    throw new UnsupportedOperationException( "sorry, this DSO uses automatic replication." );
       
        boolean serialCheck = true;
        if (certSerialNumber == sectypes.invalidSerialNumber) {
            // Okay, so we lost the serial numbers, revoke first replica for
            // given OSA.
            serialCheck = false;
        }
        boolean deleted = false;
            
        String objSvrAddrString = OSA.getHostPortPair();
        // Find the lrid of the one to delete
        // START WITH 1 to skip the initial replica, it should be removed with
        // deleteObject not with deleteReplica.
        for (int i=1; i<_replicas.size(); i++)
        {
            ARLRMgmtAddr ma = (ARLRMgmtAddr)_replicas.get( i );
            if (ma.objSvrAddrString.equals(objSvrAddrString)) {
                if (serialCheck) {
                    if (ma.certSerialNumber == certSerialNumber) {
                        // Found it
                
                        // Revoke the certificate (at least assume master is 
                        // available)
                        try {
                            // False means NOT client
                            revokeCertificate(ma.certSerialNumber, false);
                    
                            _replicas.remove(i);
                            deleted = true;
                        } catch (Exception e) {
                            DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "Deputy: Could not revoke repl certificate");
                        }
                    } else {
                        // Serial number did not match, not the entry we want.
                        continue;
                    }
                } else {
                    // Revoke this replica's certificate
                    try {
                        // False means NOT client
                        revokeCertificate(ma.certSerialNumber, false);

                        _replicas.remove(i);
                        deleted = true;
                    } catch (Exception e) {
                        DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "Deputy: Could not revoke repl certificate");
                    }
                }
                 
                // Make sure it is gone.
                ObjectServer os = getObjectServer( OSA );
                os.deleteLR( ma.lrid );
                if (!deleted) {
                    _replicas.remove(i);    // Deleting twice IS an ERROR ! 
                }
                return;
            }
        }
        throw new IllegalArgumentException( "no replica on specified object server." );
    }
    

    // BEWARE THAT INDICES IN THIS ARRAY SHOULD MATCH WITH THE INDICES OF
    // listReplicas()
    public long[] listReplicaSerialNumbers()
    throws RTSException
    {    
        long[] serials = new long[_replicas.size()];
        for (int i=0; i < _replicas.size(); i++) {
            serials[i] = ((ARLRMgmtAddr)_replicas.get(i)).certSerialNumber;
        }
        return serials;
    }

    // BEWARE THAT INDICES IN THIS ARRAY SHOULD MATCH WITH THE INDICES OF
    // listReplicaSerialNumbers()
    public ObjectServerAddress[] listReplicas()
    throws RTSException
    {
        ObjectServerAddress[] array = new ObjectServerAddress[_replicas.size()];
        for (int i=0; i<_replicas.size(); i++)
        {
            ARLRMgmtAddr ma = (ARLRMgmtAddr)_replicas.get( i );
            try
            {
                array[i] = new ObjectServerAddress( ma.objSvrAddrString );
            }
            catch( IllegalArgumentException e )
            {
                // Shouldn't happen, we created a replica with this address
                e.printStackTrace();
            }
        }
        return array;
    }
   
    
    //
    // Note: The following methods can also be used by developers that didn't create 
    // the original DSO!
    //
    

    public void bind( String ObjectName )
    throws NameServiceException.NameSyntaxException,
            NameServiceException,
            ObjectServerException.IllegalServerAddressException,
            ObjectServerException.ServerRefusedConnectionException,
            ObjectServerException.NoContactAddressFoundException,
            ObjectServerException.NoWorkingContactAddressFoundException,
            ObjectServerException.CannotCreateLRException,
            ObjectServerException.UnauthorizedOperationException,
            ObjectServerException,
            RTSException
    {
        if (_soi != null)
            return; // rebinding is a different operation
        setObjectName( ObjectName );
        doBind( true );
    }
    
    
    
    public void bind( ObjectHandle OH )
    throws  ObjectServerException.IllegalServerAddressException,
            ObjectServerException.ServerRefusedConnectionException,
            ObjectServerException.NoContactAddressFoundException,
            ObjectServerException.NoWorkingContactAddressFoundException,
            ObjectServerException.CannotCreateLRException,
            ObjectServerException.UnauthorizedOperationException,
            ObjectServerException,
            RTSException
    {
        if (_soi != null)
            return; // rebinding is a different operation
        _oh = OH;
        doBind( false );
    }

    
    public void bind()
    throws  ObjectServerException.IllegalServerAddressException,
            ObjectServerException.ServerRefusedConnectionException,
            ObjectServerException.NoContactAddressFoundException,
            ObjectServerException.NoWorkingContactAddressFoundException,
            ObjectServerException.CannotCreateLRException,
            ObjectServerException.UnauthorizedOperationException,
            ObjectServerException,
            RTSException
    {
        if (_soi != null)
            return; // rebinding is a different operation
        doBind( true );
    }
    
    
    public void rebind()
    throws  ObjectServerException.IllegalServerAddressException,
            ObjectServerException.ServerRefusedConnectionException,
            ObjectServerException.NoContactAddressFoundException,
            ObjectServerException.NoWorkingContactAddressFoundException,
            ObjectServerException.CannotCreateLRException,
            ObjectServerException.UnauthorizedOperationException,
            ObjectServerException,
            RTSException
    {
        unbind();
        doBind( false );
    }
    
    
    private void doBind( boolean BindViaName )
    throws  ObjectServerException.IllegalServerAddressException,
            ObjectServerException.ServerRefusedConnectionException,
            ObjectServerException.NoContactAddressFoundException,
            ObjectServerException.NoWorkingContactAddressFoundException,
            ObjectServerException.CannotCreateLRException,
            ObjectServerException.UnauthorizedOperationException,
            ObjectServerException,
            RTSException
    {
        // Now is a good time to add our rights certificates to our 
        // SecurityContext, which is used by the binder to find certificates 
        // for the object to initialize the LR with.
        //
        // If we own the object, we created our own user certificate.
        // If we do not own the object and it's private, we should have had 
        // the object owner add credentials for us at the master, such that we
        // get our rights certificate during binding.
        // If we do not own the object and it's public, we'll get the certs
        // during binding. If we already got them on a previous bind, they'll 
        // be stored in _objCreds.
        // 
        // This must be done during binding, as the SecurityContext is never
        // saved. It is reconstructed dynamically here.
       
        if (_rts.isSecurityEnabled() && !_added_objCreds_to_secctx)
        {
            IDLSecurityContext ctx = _rts.getSecurityContext();
            if (_objCreds == null)
            {
                // Don't have certs yet, let's hope the object is public.
            }
            else
            {
                DebugOutput.println( DebugOutput.DBG_DEBUG, "dep: Adding user certs to security context" );
                RightsCertUtil.addCertificatesToContext( _objCreds, ctx );
                _added_objCreds_to_secctx = true;
            }
        }
        
        
        if (_oname == null && _oh ==null)
            throw new RTSException( "don't know where to bind to" );
        else if (BindViaName && _oname != null)
        {
            HashMap h = _rts.bind( _oname );
            Iterator i = h.entrySet().iterator();
            Map.Entry me = (Map.Entry)i.next();
            _oh = (ObjectHandle)me.getKey();
            _soi = (SOInf)me.getValue();
        }
        else if (_oh != null)
        {
            _soi = _rts.bind( _oh );
        }
        
        // nice hack...
        vu.globe.rts.lr.mgr.LRMgr realLR = (vu.globe.rts.lr.mgr.LRMgr)_soi.soi.init_s;

        _proxy = realLR.getControlObject();

        // CREDS/REVOKE ADDED
        _secCertManager = realLR.getCertManagementObject();
        
        // DEBUG
        //ReflectionDebug.showMethods( _proxy.getClass() );
        
        if (_rts.isSecurityEnabled())
        {
            // If we don't own this object, and it's public we will have gotten
            // certificates during binding. We'll pry these certificates from
            // the security context and put them in _objCreds
            // which is part of the persistent state of a deputy.
            _objCreds = LRSecUtil.getObjectCredsFromSecurityContext(
                                    _oh.getLegacy(), _rts.getSecurityContext());
            // must succeed
        }
    }
    
    
    public void unbind()
    {
        if (_soi == null)
            return;
        _proxy = null;
        _secCertManager = null;      // CREDS/REVOKE ADDED
        _rts.unbind( _soi );
        _soi = null;
    }
    


    public Class[] getObjectInterfaces()
    throws RTSException
    {
    	if (_proxy == null)
            throw new RTSException( "not yet bound!" );
        return _proxy.getClass().getInterfaces();
    }

     
    
    /********************* Deputy Persistence ***********************/
    
    
    /** At present, we use the facilities of Tika to make deputies persistent.
     * That is, we will store the info about a deputy in the same type of database
     * as used by Tika, even in the default Tika database if desired.
     */
    
    public void save()
    throws RTSException,java.io.FileNotFoundException,java.io.IOException
    {
        try
        {
            save( Deputy.getDefaultDatabase() );
        }
        catch( SecurityException e )
        {
            throw new RTSException( "Can't get default deputy database: " + e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    
    public void save( String DeputyDBFilename )
    throws java.io.FileNotFoundException, 
            java.io.IOException,
            RTSException
    {    
        boolean found = false;
        Hashtable depState = getPersistentDeputyState();

        // Object handle might not have been set, should be, but just to be save
        String ohString = null;
        if (_oh != null) {
            ohString = _oh.toString();
        }
        
        // Read in the lastest version of the DB, object manager could have
        // added more entries in the mean time.
        ArrayList tikadb = null;
        try {
            tikadb = readDB( DeputyDBFilename );
        } catch (java.io.FileNotFoundException e) {
            // File not found, create it our selves
            tikadb = new ArrayList();
        }

        for (int i=0; i < tikadb.size(); i++)
        {
            // Try to find our entry
            Hashtable rec = (Hashtable) tikadb.get( i );
            String name = (String) rec.get( "oname" );
            String oh = (String) rec.get( "ohandle" );

            if (name != null)
            {
                if (name.equals( _oname ))
                {
                    tikadb.set(i, depState);
                    found = true;
                    break;
                }
            }
            if (oh != null && ohString != null)
            {
                if (ohString.equals(oh))
                {
                    tikadb.set(i, depState);
                    found = true;
                    break;
                }
            }
        }

        // Add our entry if we didn't find it or file did not exist.
        if (!found) {
            tikadb.add(depState);
        }

        // Write the whole database out again.
        writeDB( tikadb, DeputyDBFilename );
    }
    
    
    /**
     * Initializes this deputy using the persistent state retrieved from
     * the default deputy database. See <code>load( String )</code>
     */
    public void load()
    throws RTSException,
            java.io.FileNotFoundException, 
            java.io.IOException,
            IllegalArgumentException
    {
        try
        {
            load( Deputy.getDefaultDatabase() );
        }
        catch( SecurityException e )
        {
            throw new RTSException( "Can't get default deputy database: " + e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    
     /**
      * Initializes this deputy using the persistent state retrieved from
      * the specified deputy database. This method assumes that
      * either the name or the object handle of the DSO have been set
      * using the above methods, as these are the keys this method will look for.
      *
      * @param DeputyDBFilename The filename of the database.
      * @exception  IllegalArgumentException    When no matching record is found in the database.
      */
    public void load( String DeputyDBFilename )
    throws java.io.FileNotFoundException, 
            java.io.IOException,
            IllegalArgumentException
    {
        // Object handle might not have been set
        String ohString = null;
        if (_oh != null) {
            ohString = _oh.toString();
        }
        ArrayList tikadb = readDB( DeputyDBFilename );
        for (int i=0; i<tikadb.size(); i++)
        {
            Hashtable rec = (Hashtable)tikadb.get( i );
            String name = (String)rec.get( "oname" );
            String oh = (String)rec.get( "ohandle" );
            if (name != null)
            {
                if (name.equals( _oname ))
                {
                    setPersistentDeputyState( rec );
                    return;
                }
            }
            if (oh != null && ohString != null)
            {
                if (ohString.equals(oh))
                {
                    setPersistentDeputyState( rec );
                    return;
                }
            }
        }
        throw new IllegalArgumentException( "No record found in " + DeputyDBFilename );
    }
    
    
    public Hashtable getPersistentDeputyState() throws RTSException
    {
        Hashtable rec = new Hashtable();
        String linename="";
        
        rec.put( "deputyclass", this.getClass().getName() );
        if (_oh != null)
        {
            rec.put( "ohandle", _oh.toString() ); // primary key
        }
        if (_oname != null)
        {
            rec.put( "oname", _oname );
        }

        if (_rts.isSecurityEnabled()) {
            if (_signedBPs != null)
            {
                for (int i=0; i<_signedBPs.v.length; i++)
                {
                    // Keep this repl. proto independent!
                    if (i == _addrepl_bp_idx)
                        linename = "addrepl-signed-blueprint"+i;
                    else if (i == _initial_bp_idx)
                        continue;
                    else 
                        linename = "other-signed-blueprint"+i;
                    IDLSignedBlueprint sbp = (IDLSignedBlueprint)_signedBPs.v[ i ];
                    if (sbp != null)
                    {
                        rec.put( linename, LRSecUtil.idlSignedBlueprintToString( sbp ) );
                    }
                }
            }
        } else if (_safeBPS) {
            // Write the _bps out
            for (int i=0; i < _bps.size(); i++)
            {
                // Keep this repl. proto independent
                if (i == _addrepl_bp_idx)
                    linename = "addrepl-blueprint"+i;
                else if (i == _initial_bp_idx)
                    continue;
                else
                    linename = "other-blueprint"+i;

                IDLBlueprint bp = (IDLBlueprint) _bps.get(i);
                if (bp != null) {
                    rec.put(linename, LRSecUtil.idlBlueprintToString(bp));
                }
            }
        }

        // Publicity
        rec.put("publicity", new Integer(_publicity).toString());
      
        for (int i=0; i<_replicas.size(); i++)
        {
            linename="other-repl"+i;
            if (i ==0)
                linename = "first-repl";
            ARLRMgmtAddr ma = (ARLRMgmtAddr)_replicas.get( i );
            rec.put( linename, ma.toString() );
        }

        // If security has been disabled all these will be null.
        try
        {
            if (_obj_ks != null)
            {
                String pem = PEM.x509KeyStoreToPEM( _obj_ks, RightsCertUtil.DEFAULT_KEY_PASSWD );
                rec.put( "obj-keystore", pem );
            }
            if (_objCreds != null)
            {
                String pem = PEM.objectCredsToPEM(_objCreds);
                rec.put( "object-creds", pem );
            }
            if (_public_facb != null)
            {
                rec.put( "public-facb", RightsCertUtil.accessCtrlBitmapToURI( _public_facb ) );
            }
            if (_obj_ped_chain != null)
            {
                String pem = PEM.x509CertChainToPEM( IdentCertUtil.idlIdentCertsToJava( _obj_ped_chain ) );
                rec.put( "obj-ped-chain", pem );
            }
        }
        catch( RTSException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            throw new RTSException( e );
        }
        
        return rec;
    }

    
    public void setPersistentDeputyState( Hashtable Rec )
    throws IllegalArgumentException
    {
        ArrayList sbpList = new ArrayList();
        int sbpList_idx = 0;
        int bpList_idx = 0;
       
        if (!_rts.isSecurityEnabled()) {
            // Clear the _bps list
            _bps.clear();
        }
        
        for (Enumeration e = Rec.keys(); e.hasMoreElements(); )
        {
            String linename = (String)e.nextElement();
            
            if (linename.equals( "deputyclass" ))
            {
                // ignored
            }
            else if (linename.equals( "oname" ))
            {
                _oname = (String)Rec.get( linename );
            }
            else if (linename.equals( "ohandle" ))
            {
                _oh = ObjectHandle.parse( (String)Rec.get( linename ) );
            }
            else if (linename.equals( "publicity" ))
            {
                _publicity = Integer.parseInt((String)Rec.get(linename) );
            }
            else if (linename.startsWith( "addrepl-signed-blueprint" ) 
                     || linename.startsWith( "other-signed-blueprint" ))
            {
                sbpList.add( LRSecUtil.stringToIDLSignedBlueprint(  (String)Rec.get( linename ) ) );
                if (linename.startsWith( "addrepl-signed-blueprint" ))
                    _addrepl_bp_idx = sbpList_idx;
                sbpList_idx++;
            }
            else if (linename.startsWith("addrepl-blueprint")
                    || linename.startsWith("other-blueprint"))
            {
                // If we read them in here, we must write them later
                if (!_rts.isSecurityEnabled()) {
                    _safeBPS = true;
                }
                
                IDLBlueprint bp = LRSecUtil.stringToIDLBlueprint(
                                                    (String) Rec.get(linename));
                _bps.add(bp);

                if (linename.startsWith("addrepl-blueprint"))
                    _addrepl_bp_idx = bpList_idx;

                bpList_idx++;
            }
            else if (linename.equals( "first-repl" ))
            {
                ARLRMgmtAddr ma = ARLRMgmtAddr.parse( (String)Rec.get( linename ) );
                _replicas.add( 0, ma );
            }
            else if (linename.startsWith( "other-repl" ))
            {
                ARLRMgmtAddr ma = ARLRMgmtAddr.parse( (String)Rec.get( linename ) );
                _replicas.add( ma );
            }
            else if (linename.equals( "obj-keystore" ))
            {
                try
                {
                    _obj_ks = PEM.pemToX509JKSKeyStore( (String)Rec.get( linename ), RightsCertUtil.DEFAULT_KEY_ALG, RightsCertUtil.DEFAULT_KEYSTORE_PASSWD, RightsCertUtil.DEFAULT_KEY_ALIAS, RightsCertUtil.DEFAULT_KEY_PASSWD );
                }
                catch( Exception x )
                {
                    throw new IllegalArgumentException( x.toString() );
                }
            }
            else if (linename.equals( "public-facb" ))
            {
                _public_facb = RightsCertUtil.uriToAccessCtrlBitmap( (String)Rec.get( linename ) );
            }
            else if (linename.equals( "obj-ped-chain" ))
            {
                try
                {
                    _obj_ped_chain = IdentCertUtil.javaToIDLIdentCerts( PEM.pemToX509Chain( (String)Rec.get( linename ) ) );
                }
                catch( Exception x )
                {
                    throw new IllegalArgumentException( x.toString() );
                }
            }
            else if (linename.equals( "object-creds" ))
            {
                try
                {
                    String pem = (String)Rec.get( linename );
                    _objCreds = PEM.pemToObjectCreds(pem);
                }
                catch( Exception x )
                {
                    throw new IllegalArgumentException( x.toString() );
                }
            }
            /* No else: dirty subclass may not have removed its entries */
        }
        
        if (_rts.isSecurityEnabled()) {
            // Recreate signed blueprints sequence, it is used in addReplica()
            _signedBPs = new IDLSignedBlueprints( sbpList.size() );
            for (int i=0; i<sbpList.size(); i++)
            {
                _signedBPs.v[i] = (IDLSignedBlueprint)sbpList.get( i );
            }
        }

        // NOTE THAT THE INITIAL BLUEPRINT IS NOT THERE ANYMORE, MAKE SURE THAT
        // IT DOES NOT MATCH INDEX OF ADD_REPL
        _initial_bp_idx = -1;
    }
    
    /**
     * Returns a string representation of this Deputy
     */
    public String toString()
    {
        try
        {
            return getPersistentDeputyState().toString();
        }
        catch( RTSException x )
        {
            return "Could not stringify deputy";
        }
    }

    
    
    
    /******************** DeputyPersistence ********************/    
    
    public static final String TAG_START = "<";
    public static final String TAG_END = ">";
    public static final String CLOSETAG_START = "</";
    public static final String NOISE_SEP="=";
    public static final String RECORD_START = TAG_START + "RECORD" + NOISE_SEP;
    public static final String RECORD_END = CLOSETAG_START + "RECORD" + NOISE_SEP;
    

    /** 
        I am so ashamed of this code, but don't want to require an XML parser,
        and the old Tika stuff don't work anymore with our keystores in PEM. 
     */
    private static ArrayList readDB( String DeputyDBFilename )
        throws java.io.FileNotFoundException, java.io.IOException, 
        IllegalArgumentException
    {
        ArrayList db = new ArrayList();
        RandomAccessFile f = new RandomAccessFile( DeputyDBFilename, "r" );
        long start = 0;
        long current = 0;
        // 16 = long in hex
        byte[] startbuf = new byte[ RECORD_START.length()+16+TAG_END.length() ];
        int count=1;
        while( true )
        {
            int nread = f.read( startbuf );
            if (nread == -1)
                break;
            else if (nread != startbuf.length)
                throw new IllegalArgumentException( "Record " + count +" broken (no start label) in " + DeputyDBFilename );
            start += nread;
            String recStart = new String( startbuf );
            String recEnd = CLOSETAG_START + recStart.substring( 1, recStart.length() );
            byte[] endbuf = new byte[ recEnd.length() ];
            current += nread;
            while( true )
            {
                f.seek( current );
                nread = f.read( endbuf );
                if (nread == -1)
                    throw new IllegalArgumentException( "Record " + count +" broken (no end label) in " + DeputyDBFilename );
                String s = new String( endbuf );
                if (s.equals( recEnd ))
                {
                    // found end
                    long length = current-start;
                    f.seek( start );
                    byte[] b = new byte[ (int)length ]; // BUG if record large 64-bit
                    nread = f.read( b );
                    if (nread != b.length)
                        throw new IllegalArgumentException( "Record " + count +" broken (can't reread record) in " + DeputyDBFilename );
                    Hashtable rec = xml2Rec( new String( b ) );
                    db.add( rec );
                    
                    start = current+recEnd.length();
                    f.seek( start );
                    break;
                }
                current++;
            }
        }
        
        return db;
    }
    
    
    private static void writeDB( ArrayList DB, String DeputyDBFilename )
        throws java.io.FileNotFoundException, java.io.IOException
    {
        File f = new File( DeputyDBFilename );
        FileOutputStream fos = new FileOutputStream( f );
        
        for (int i=0; i<DB.size(); i++)
        {
            Random r = new Random( System.currentTimeMillis() );
            String noise = Long.toHexString( StrictMath.abs( r.nextLong() ) );
            if ((noise.length() % 2) == 1)
                noise = "0" + noise;
            String start = RECORD_START+noise+TAG_END;
            String end = RECORD_END+noise+TAG_END;
            String s = rec2XML( (Hashtable)DB.get( i ) );
            fos.write( start.getBytes() );
            fos.write( s.getBytes() );
            fos.write( end.getBytes() );
        }
        fos.close();
    }
    
    
    private static String rec2XML( Hashtable rec )
    {
        String s="";
        
        Random r = new Random( System.currentTimeMillis() );
        for (Enumeration e=rec.keys(); e.hasMoreElements(); )
        {
            String noise = Long.toString( StrictMath.abs( r.nextLong() ) );
            String key = (String)e.nextElement();
            String data = (String)rec.get( key );
            s += TAG_START + key.toUpperCase()+NOISE_SEP+noise + TAG_END;
            s += data;
            s += CLOSETAG_START + key.toUpperCase()+NOISE_SEP+noise + TAG_END;
        }
        return s;
    }
    
    
    private static Hashtable xml2Rec( String XML )
    {
        Hashtable rec = new Hashtable();
        readRecurse( XML, rec );
        return rec;
    }
    
    
    private static void readRecurse( String XML, Hashtable Rec )
    {
        if (XML.equals( "" ))
            return;
        
        int sidx = XML.indexOf( TAG_START );
        int midx = XML.indexOf( NOISE_SEP );
        int eidx = XML.indexOf( TAG_END );
        String key = XML.substring( sidx+1, midx );
        String close = CLOSETAG_START + key + NOISE_SEP;
        sidx = XML.indexOf( close );
        String data = XML.substring( eidx+1, sidx );
        Rec.put( key.toLowerCase(), data );
        readRecurse( XML.substring( sidx + close.length() + (eidx-midx), XML.length() ), Rec );
    }
    
    
    public static ArrayList loadList( String DeputyDBFileName )
        throws java.io.FileNotFoundException, 
        java.io.IOException,
        ClassNotFoundException, 
        InstantiationException,
        IllegalAccessException
    {
        // Read records
        ArrayList db = readDB( DeputyDBFileName );
        
        // Turn them into deputies
        ArrayList deps = new ArrayList();
        for (int i=0; i<db.size(); i++)
        {
            Hashtable rec = (Hashtable)db.get( i );
            String className = (String)rec.get( "deputyclass" );
            Class c = Class.forName( className );
            Deputy d = (Deputy)c.newInstance();
            d.setPersistentDeputyState( rec );
            deps.add( d );
        }
        return deps;
    }
    
    
    public static void saveList( ArrayList Deputies, String DeputyDBFileName )
    throws java.io.FileNotFoundException, 
        java.io.IOException,
        RTSException
    {
        ArrayList db = new ArrayList();
        for (int i=0; i<Deputies.size(); i++)
        {
            Deputy d = (Deputy)Deputies.get( i );
            db.add( d.getPersistentDeputyState() );
        }
        if (Deputies.size() > 0) {
            writeDB( db, DeputyDBFileName );
        } else {
            try {
                new File(DeputyDBFileName).delete();
            } catch (Exception e) {
                // Dan niet...
                System.err.println("ERROR: Could not remove: "+DeputyDBFileName);
            }
        }
    }

    
    public static String getDefaultDatabase()
    {
        String homeDir=null,fileSep=null;
        try
        {
            fileSep = System.getProperty("file.separator");
            homeDir = System.getProperty("user.home");
        }
        catch( NullPointerException e )
        {
            // won't ever happen, key is non-null.
        }
        catch( IllegalArgumentException e )
        {
            // won't ever happen, key is non-null.
        }
        if (homeDir == null)
        {
            homeDir="";
        }
        return homeDir + fileSep + ".globe" + fileSep + "deputy.db";
    }

    /************* Deputy Cert Management ***********************************/
    public Vector getClientsList() throws secErrors {

        Vector v = null;
        
        if (_secCertManager == null) {
            // Not bound yet
            throw new secErrors_invOp();
        }
        IDLClientsList list = null;
        try {
            list = _secCertManager.getClientsList();
        } catch (Exception e) {
            System.err.println("ERROR: get clients list: "+e.getMessage());
            throw new secErrors_noChannel();
        }

        v = new Vector();
        CredentialEntry cE = null;
        for (int i=0; i < list.v.length; i++) {
            cE = new CredentialEntry(list.v[i]);
            v.add(cE);
        }
        return v;
    }
        
    
    public void updateCredentials(String userName, String userKeyHash,
                                  AccessCtrlBitmap forwardACB, short opMode) 
    throws secErrors {

        if (_secCertManager == null) {
            // Not bound yet
            throw new secErrors_invOp();
        }

        // transform the parameters into their IDL equivalents
        IDLSubjectName sjN = new IDLSubjectName(userName.getBytes().length);
        sjN.v = userName.getBytes();
        
        IDLPubKeyHash uKH = new IDLPubKeyHash(userKeyHash.getBytes().length);
        uKH.v = userKeyHash.getBytes();

        IDLAccessCtrlBitmap idlACB = new IDLAccessCtrlBitmap(
                                            forwardACB.toByteArray().length);
        idlACB.v = forwardACB.toByteArray();

        short secOpMode = 0;
        switch (opMode) {
            case DeputyCertManagement.CRED_ADD: {
                secOpMode = credsOpMode.add;
                break;
            } case DeputyCertManagement.CRED_REMOVE: {
                secOpMode = credsOpMode.remove;
                break;
            } case DeputyCertManagement.CRED_UPDATE: {
                secOpMode = credsOpMode.update;
                break;
            } default: {
                throw new secErrors_invOp();
            }
        }
            
        // Make the call
        try {
            _secCertManager.updateCredentials(sjN, uKH, idlACB, secOpMode);
        } catch (Exception e) {
            System.err.println("ERROR: updateCredentials: "+e.getMessage());
            throw new secErrors_noChannel();
        }
    }


    public void revokeCertificate(long certSerialNum, boolean clientCert)
            throws secErrors {

        if (_secCertManager == null) {
            // not bound yet
            throw new secErrors_invOp();
        }

        try {
            if (clientCert) {
                _secCertManager.revokeCertificate(certSerialNum, g.bool.True);
            } else {
                _secCertManager.revokeCertificate(certSerialNum, g.bool.False);
            }
        } catch (Exception e) {
            System.err.println("ERROR: revokeCertificate: "+e.getMessage());
            throw new secErrors_noChannel();
        }
    }

    public AccessCtrlBitmap getPublicFACB() {
        // WE NEED TO GIVE A COPY !! Otherwise we could screw up the real
        // public FACB
        return (AccessCtrlBitmap) _public_facb.clone();
    }

    public ReverseMethodIDMap getSemReverseMIDMap() {
        return _sem_revmap;
    }

    public int getSemanticsOffset() {
        return (MSConfig.NUMBER_OF_REPLICATION_METHODS +
                secGenericConfig.NUMBER_OF_SEC_METHODS);
    }

    public int getReplicationOffset() {
        return secGenericConfig.NUMBER_OF_SEC_METHODS;
    }

}
