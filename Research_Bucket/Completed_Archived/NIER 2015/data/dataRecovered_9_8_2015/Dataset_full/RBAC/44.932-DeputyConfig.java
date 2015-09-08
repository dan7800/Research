/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

/**
 * DeputyConfig.java - interface for defining the working object type used
 * by the deputy when creating objects.
 *
 * A working object type is a specification of the functional and nonfunctional
 * aspects of an object. Concretely, it is a specification of which subobjects
 * an object uses (semantics, replication, etc.). However, e.g. a replication
 * protocol may not use one generic class in all local representatives of the 
 * object (i.e. there is not one MasterSlave class, but we have msMaster,
 * implementing the master role, msSlave, implementing the slave role and
 * msClient, implementing the client role). As a result, the specification
 * consists of a number of blueprints. A blueprint defines a specific composition
 * of subobject classes for a specific type of LR (e.g. master/slave/client).
 *
 * @author  Arno Bakker
 */
package vu.gaia.lr.n;

import vu.globe.rts.security.idl.secconfig.IDLSubobjectID;
import vu.globe.rts.security.certs.AccessCtrlBitmap;
import vu.gaia.rts.RTSException;
import java.net.URL; // BVD

public interface DeputyConfig
{
    // Public constants
    public static final int	SEM_SUBOBJ = IDLSubobjectID.SEM_SUBOBJ;
    public static final int	REPL_SUBOBJ = IDLSubobjectID.REPL_SUBOBJ;
    public static final int	COMM_SUBOBJ = IDLSubobjectID.COMM_SUBOBJ;
    public static final int	CTRL_SUBOBJ = IDLSubobjectID.CTRL_SUBOBJ;
    public static final int	SEC_SUBOBJ = IDLSubobjectID.SEC_SUBOBJ;

    public static final int N_SUBOBJS = (int)vu.globe.rts.security.idl.secconfig.N_SUBOBJECTS;

    /**
     * You must use this method to define the blueprints for each of the different
     * LRs in the working object type. With this method you can set which 
     * subobjects are in a particular blueprint and the init data for the
     * subobject. Must be called before <code>create()</code>.
     *
     * @param   WhichBP     The blueprint to modify
     * @param   WhichSub    The subobject to set within the blueprint.
     * @param   ImplHandle  The implementation handle of the subobject.
     * @param   InitData    The initialization data to be passed to the
     *                      subobject.
     * @param   ReqFACB     The (forward) access control bitmap required by 
     *                      this subobject.
     * @param   ReqRACB     The reverse access control bitmap required by
     *                      this subobject.
     */
    public void setSubobject( int WhichBP, int WhichSub, String ImplHandle, String InitData, AccessCtrlBitmap ReqFACB, AccessCtrlBitmap ReqRACB );
    
    
    /* With this method you set which of the blueprints you defined using
     * <code>setSubobject</code> should be used to create the initial replica
     * of the object.  I.e. what LR the <code>create</code> method will create.
     * Must be called before <code>create()</code>.
     */
    public void setInitialReplicaBlueprint( int WhichBP );
    

    /* With this method you set which of the blueprints you defined using
     * <code>setSubobject</code> should be used to create the new replica when 
     * you call <code>addReplica</code>.  
     * Must be called before <code>create()</code>.
     */
    public void setAddReplicaBlueprint( int WhichBP );
    
    
    /**
     * You set whether the initial replica of the DSO will be made persistent 
     * or not. In the former case, the Semantics subobject must implement the
     * GOSPersistentSemanticsSubobject interface.
     */
    public void setInitialReplicaPersistent( boolean Val );

    
   /** 
     * Deletes any blueprints defined using <code>setSubobject</code> or
     * by <code>setDefaultWorkingObjectType();
     */
    public void clearBlueprints();
    

    

    /*
     * The following methods build on the previous methods to provide an
     * even simpler interface for those cases where you don't want to
     * define your working object type and just want to use the default
     * (master/slave with security) or a slight variantion of that 
     * (client/server with security).
     */
    
    // Easy selection of default replication protocols.

    /** The client-specified policy abbrev for client/server. */
    public static final String REPL_CLIENT_SERVER = "cs";
    
    /** The client-specified policy abbrev for master/slave. */
    public static final String REPL_MASTER_SLAVE = "ms";
    
    /** The client-specified policy abbrev for master/slave with auto 
        replication */
    public static final String REPL_MASTER_SLAVE_AUTO = "ms-auto";


    /** 
     * Any changes to the default working object type should be made
     * after calling <code>setSemantics</code>. This method is normally
     * called by the object-type specific deputy that is generated by
     * GenDeputy.
     *
     * 1. setSemantics()
     * 2. (which calls setDefaultWorkingObjectType())
     * 3. either:  setReplicationProtocol( ... );
     * 3. or:      setSubobject( ... );
     */
    public void setSemantics( String ImplHandle )
    throws RTSException;
    
    /** If the code of the semantics subobject should be remotely loaded */
    public void setSemantics( String ImplHandle, String CodeURLStr ) // BVD
    throws RTSException;
    
    /** 
     * This method defines the default working object type. It uses
     * the first- and-level methods from DeputyConfig to define 
     * the blueprints for this default type. It may, and is, highly specific 
     * to the currently available Globe replication protocols. This is not
     * considered a problem as this method is second-level and the first-level
     * methods are all independent.
     *
     * This method is called by <code>setSemantics</code> resulting in a 
     * full-fledged working object type. <code>setSemantics</code> is called
     * automatically in object-type specific subclasses (e.g. IntegerDeputy) 
     * generated by GenDeputy.
     *
     * To define your own working object type, you can either override this 
     * method, or use <code>setSubobject</code> to define your own ad-hoc
     * working type. You should call <code>clearBlueprints</code> to
     * delete the predefine blueprints of the default working object type.
     */
    public void setDefaultWorkingObjectType();


    
    /**
     * Configures the DSO's initial blueprint such that the DSO will use the
     * specified replication policy.
     * @param   PolicyAbbrev    one of the policy abbreviations as defined by this class.
     * @param   PolicyParams    configuration parameters, e.g. used in automatic replication.
     * @exception  IllegalArgumentException    unknown policy abbreviation, or the policy
     * selected is not currently available (e.g. because security is disabled).
     */
    public void setReplicationProtocol( String PolicyAbbrev, String PolicyParams )
    throws IllegalArgumentException;


    /**
     * Configures the DSO to NOT use the access control bitmaps, certificates
     * and SSL/TLS connections. Suggest to use only for TESTING !
     */
    public void setDisableSecurity();
}
