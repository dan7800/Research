/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.security.base;

import vu.globe.rts.java.*;
import vu.globe.rts.comm.idl.p2p.*;
import vu.globe.rts.lr.idl.lrsub.*;
import vu.globe.rts.lr.mgr.LRMgr;
import vu.globe.rts.std.StdUtil;
import vu.globe.rts.std.idl.stdInf.*;
import vu.globe.rts.security.skels.*;
import vu.globe.rts.security.idl.security.*;
import vu.globe.rts.security.idl.sectypes.*;
import vu.globe.rts.security.idl.secconfig.*;
import vu.globe.rts.runtime.ns.nsConst;
import vu.globe.rts.runtime.ns.idl.context;
import vu.globe.rts.runtime.ns.idl.nsTypes.*;
import vu.globe.rts.runtime.ns.nameSpaceImp.nameSpaceImp;

import vu.globe.idlsys.g;

import vu.globe.util.comm.*;
import vu.globe.util.comm.idl.rawData.*;
import vu.globe.util.debug.DebugOutput;
import vu.globe.util.idltypes.ObjectHandleOps;
import vu.globe.util.exc.AssertionFailedException;

import vu.globe.svcs.gls.idl.lsClient.*;
import vu.globe.svcs.objsvr.idl.persistence.*;
import vu.globe.svcs.objsvr.idl.resource;
import vu.globe.svcs.objsvr.idl.resource.*;

import java.net.ProtocolException;
import java.util.*;

import vu.globe.util.debug.*;

/** Base class for the secConnector (security subobject). SecConnector may be
 * used as a base class to any security subobject, whatever the security
 * strategy. The base class carries out a number of tasks applicable to most
 * security subobjects:
 *
 * <ul> 
 * <le> The base class implements the lrSubObject interface. It does so in terms
 *      of other, abstract methods. Subclasses are responsible for filling in
 *      these abstract methods.
 * <le> The base class provides support for resource allocation in the resource
 *      manager. It also has support for persistence.
 * </ul>
 * <p>
 * The secConnector and secConnectionCB interfaces remain abstract
 * 
 * @author W.R. Dittmer
 */

public abstract class SecConnector extends secConnectorSkel {

    // The replication subobject's secConnectorCB interface
    protected secConnectorCB _replSubObj = null;
    protected g.opaque _replUser = null;

    // The communication object in the LR. Null if none installed in the LR.
    private SOInf _comm = null;                         // counted
    
    // Resource manager's interface.
    private SOInf _resourceSoi = null;                  // counted 

    // Persistent resource manager's interface
    private perstResourceManager _perstRscMgr = null;   // uncounted 

    // Contact manager's interface
    private contactManager _contactMgr = null;          // uncounted

    // Timer resource manager's interface
    private timerResourceManager _timerRscMgr = null;   // uncounted
        
    // The persistence id of the LR that this security subobject is part of.
    // 'Invalid' if the LR is not persistent. However, undefined until
    // _pidDefined is set.
    private long _pid = resource.invalidResourceID;

    // Tells whether _pid is defined
    private boolean _pidDefined = false;
  
    // Tells whether we are just reactivate and should not allocate cps
    protected boolean _reActivated = false;
    
    // The Globe object's object handle 
    protected g.opaque _objectHandle = null;

    // The LS resolver's idManager interface.
    protected idManager _idManager = null;              // counted
    
    // The LS resolver's resolver interface
    protected stdResolverOps _resolverOps = null;       // uncounted
    
    // The list of channelID to secure connection mappings
    protected Map _channelToConnection = null;

    // Call getNextChannelID() !
    private int _nextChannelID = 0;

    // List of channel to Certificate mappings
    protected Map _channelToAC = null;

    // Our own AccessControl map
    protected AccessControl _myAC = null;

    // Local name of the secure connection
    private static final String SECCON_LNAME = "seccon";
    
    // Things we should get via the init string: FIXME

    // secConnection Implementation
    protected String SECCON_IMPL = "JAVA;vu.globe.rts.security.protocol.SSL.SSLSecConnectionCO";

    protected String SECCON_PLAIN_IMPL = "JAVA;vu.globe.rts.security.protocol.plain.PlainSecConnectionCO";


    // Our lrid
    protected long _lrid = -1;
    
    /****************** Methods **********************************************/
    // Cleanup, may be overwritten by the subclass to perform its own cleanup.
    // This method is called just before the security object is destroyed.
    protected void cleanup() throws SOIerrors {

        if (_idManager != null) {
            _idManager.relInf();
        }

        if (_comm != null) {
            _comm.relInf();
        }

        if (_resourceSoi != null) {
            _resourceSoi.relInf();
        }
        super.cleanup();
        
    }

    // May be overwritten by the subclass to perform additional initialization.
    // This method is called just after instantiation.
    protected void initState() throws SOIerrors {

        super.initState();

        // Install the idManager and stdResolverOps
        try {
            SOInf ls = lns.bind(getContext(), nsConst.LS_CLIENT_NAME);

            _idManager = (idManager) ls.swapInf(idManager.infid);
            _resolverOps = (stdResolverOps) _idManager.soi.getUncountedInf(
                                                        stdResolverOps.infid);
        } catch (Exception e) {
            // Could not bind or getUncounted interface
            if (_idManager != null) {
                _idManager.relInf();
            }
            throw new SOIerrors_notFound();
        }
        
        // Initialize our connections list
        _channelToConnection = Collections.synchronizedMap(new HashMap());
        _nextChannelID = 1;

        // Initialize our connection to certificate list
        _channelToAC = Collections.synchronizedMap(new HashMap());
    }

    
    /****************** Implementation of the secConnector interface *********/

    // Extending class should overwrite this class but be sure to user
    // super.constructor(init, CFG) !
    // And they should initialize _myAC !!
    public void constructor(String init, IDLLRMgrConfig CFG) {
    } 

    public short /* g.bool */ forwardAllowed(int channel, int method) {
        // Check the FORWARD ACB of the party indicated by channel to see if
        // they are allowed to call method. Also check our REVERSE ACB
        // if we are supposed to be able to execute that method.

        boolean mayWe = _myAC.reverseAllowed(method);
        
        boolean canThey =
            ((AccessControl) _channelToAC.get(new Integer(channel)))
                                                .forwardAllowed(method);

        return ((mayWe && canThey) ? g.bool.True : g.bool.False);
    }

    public short /* g.bool */ reverseAllowed(int channel, int method) {
        // Check the REVERSE ACB of the party indicated by channel to see if
        // we can call method on it. Also check our FORWARD ACB if we are
        // supposed to be able to call that method.

        boolean mayWe = _myAC.forwardAllowed(method);

        boolean canThey =
            ((AccessControl) _channelToAC.get(new Integer(channel)))
                                                .reverseAllowed(method);

        return ((mayWe && canThey) ? g.bool.True : g.bool.False);
    }

    public short /* g.bool */ isRevoked(int channel) {
        
        if (((AccessControl) _channelToAC.get(new Integer(channel))).
                                                         isRevoked()) {
            return g.bool.True;
        }
        
        return g.bool.False;
    }
    
    /************* Implementation of secConnectionCB interface ***************/
    public contactAddresses regSecConnectorCB(secConnectorCB cb,
                                            g.opaque user) throws secErrors {

        contactAddresses cInfoSeq = null;
            
        // Allocate our contact point for slaves/clients to connect on
        // return the cInfoSeq, but only allocates new ones if needed
        if (!_reActivated) {
            cInfoSeq = allocateResources();
        }
            
        // Start listening (if any)
        startListener();
    
         // Register the callback
        _replSubObj = cb;
        _replUser = user;
        
        // return the contact point via the repl
        return cInfoSeq;
    }

    /****************** Implementation of the secMsgCB interface ************/
    public void msgReceived(int channel, g.opaque user) {

        // Only used in the master and slave for binding, they should override
        // this method. In any other case print out an error message
        DebugOutput.println(DebugOutput.DBG_NORMAL,
                            "SecConnector: msgReceived not implemented");
    }

    public void receiveStopped(int channel, g.opaque user) {
        // See above
        DebugOutput.println(DebugOutput.DBG_NORMAL,
                            "SecConnector: msgReceived not implemented");
    }
    

    /****************** Implementation of the lrSubObject Interface **********/
    public void setObjectHandle(g.opaque objectHandle) {
        _objectHandle = objectHandle;
    }    
 
    public void setPerstID(long pid) throws subObjectErrors {

        _pid = pid;
        _pidDefined = true;
    }
    
    public void prepareDestruction(short /* g.bool */ quick)
            throws subObjectErrors {

        boolean myQuick = (quick == g.bool.True);
        closeConnections(myQuick);
        deallocateResources(myQuick);
    }

    public void preparePassivation(short /* g.bool */ quick)
            throws subObjectErrors {

        closeConnections(quick == g.bool.True);
    }

    public rawDef completePassivation() throws subObjectErrors {

        rawDef state = RawOps.createRaw();
        RawCursor cur = new RawCursor(state);
        try {
            marshallState(cur);
        } catch (Exception e) {
            throw new subObjectErrors();
        }
        return state;
    }

    public void prepareActivation(long pid, rawDef state, 
                    short /* g.bool */ recoverMode) throws subObjectErrors {

        _pid = pid;
        _pidDefined = true;

        RawCursor cur = new RawCursor(state);

        try {
            unmarshallState(cur);
        } catch (ProtocolException e) {
            DebugOutput.printException(DebugOutput.DBG_DEBUG, e);
            throw new subObjectErrors_invArg();
        }

        _reActivated = true;
    }

    public void completeActivation() throws subObjectErrors {
        // We can't do anything here, the listening may only start when the repl
        // calls regSecConnectorCB on us.
    }

    public void preparePassivationCheckpoint() throws subObjectErrors {

        pause(true);
    }

    public rawDef passivationCheckpoint() throws subObjectErrors {

        rawDef state = RawOps.createRaw();
        RawCursor cur = new RawCursor(state);
        try {
            marshallState(cur);
        } catch (Exception e) {
            throw new subObjectErrors();
        }
        return state;
    }

    public void completePassivationCheckpoint() throws subObjectErrors {
    
        pause(false);
    }

    /***************** Resource Manager **************************************/

    // Returns an uncounted contactManager interface of the resource manager.
    public contactManager getContactManager() {

        if (_resourceSoi == null) {
            installResourceManager();
        }

        return (contactManager) _contactMgr.dupUncountedInf();
    }

    // Returns an uncounted perstResourceManager interface of the res. mgr.
    public perstResourceManager getPerstResourceManager() {

        if (_resourceSoi == null) {
            installResourceManager();
        }

        return (perstResourceManager) _perstRscMgr.dupUncountedInf();
    }


    public timerResourceManager getTimerResourceManager() {
        if (_resourceSoi == null) {
            installResourceManager();
        }

        return (timerResourceManager) _timerRscMgr.dupUncountedInf();
    }

    
    // Looks up the resource manager and its interfaces. Thread safe.
    private void installResourceManager() {

        synchronized(this) {

            if (_resourceSoi == null) {
                try {
                    // get the resource manager
                    _resourceSoi = nameSpaceImp.getLNS().bind(getContext(),
                                                    nsConst.PERST_MGR_NAME);
                    _contactMgr = (contactManager) 
                            _resourceSoi.getUncountedInf(contactManager.infid);
                    _perstRscMgr = (perstResourceManager)
                            _resourceSoi.getUncountedInf(
                                                   perstResourceManager.infid);
                    _timerRscMgr = (timerResourceManager)
                            _resourceSoi.getUncountedInf(
                                                   timerResourceManager.infid);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new AssertionFailedException("No access to " +
                                                       "resource manager");
                }
            }
        }
    }

    /**************** Persistence and Communication **************************/

    /**
     * Get the communication subobject installed by the LR, or null if
     * none was found.
     */
    protected SOInf getCommObject() {

        if (_comm == null) {
            // Retrieve it in thread safe manner
            synchronized(this) {
                // Check again if comm not exists
                if (_comm == null) {
                    try {
                        _comm = lns.bind(getContext(), 
                                        "../" + LRMgr.COMM_LNAME);
                    } catch (Exception e) {
                        throw new AssertionFailedException();
                    }
                }
            }
        }

        // Return the interface of the object, also counted !
        return (SOInf) _comm.dupInf();
    }

    /**
     * Returns true iff the LR that this replication object is a part of is a
     * persistent local object. This information is not known during initState()
     * and configure() invocations.
     *
     * @exception IllegalStateException   if the result is not yet known
     */
    protected boolean isPersistent() {
        
        if (! _pidDefined) {
            throw new IllegalStateException("pid not yet defined");
        }
        
        return (_pid != resource.invalidResourceID);
    }

    /**
     * Returns the persistence id of the LR that this replication object is a
     * part of, or 'invalid' if this LR is not persistent. This information is
     * not known during initState() and configure() invocations.
     *
     * @exception IllegalStateException   if the result is not yet known
     */
    protected long getPID() {
    
        if (! _pidDefined) {
            throw new IllegalStateException ("pid not yet defined");
        }
        
        return _pid;
    }

    /**
     * Allows a subclass to perform actions for the initialization. 
     * Typically the subclass will allocate resources such as contact points 
     * from the resource manager.
     *
     * @return    a contactAddressInfo for each allocated contact point.
     *            Null may be returned if no contact points were created.
     */
    protected abstract contactAddresses allocateResources() 
            throws secErrors;

    /**
     * Allows a subclass to perform actions to prepare for the final
     * destruction. Typically the subclass will deallocate resources from the 
     * resource manager.
     *
     * @param quick       if true, only local operations and non-wide-area
     *                    communication may be performed
     */
    protected abstract void deallocateResources(boolean quick)
            throws subObjectErrors;

    /**
     * Install and start listening on a contact point (if any)
     */
    protected abstract void startListener() throws secErrors;

    /**
     * Allows a subclass to perform actions to prepare for the final destruction
     * of a (persistent or transient) replica and for the passivation of a
     * persistent replica. Typically the subclass will close its communication
     * links with other replicas and its contact points.
     *
     * @param quick       if true, only local operations and non-wide-area
     *                    communication may be performed
     */
    protected abstract void closeConnections(boolean quick)
            throws subObjectErrors;

    /**
     * Pauses or resumes activity. The pause here should be called before pause
     * is called on the repl. Because we will want to pause the listener first.
     * The reason that the implementation is elsewhere is because we are not
     * sure that we have a listener (client case for example).
     */
    protected abstract void pause(boolean on) throws subObjectErrors;

    /**
     * Allows the subclass to marshall its state for persistence. The subclass
     * should write its state to the current offset of the given packet, and
     * update the current offset.
     */
    protected abstract void marshallState(RawCursor state)
            throws ProtocolException;


    /**
     * Allows the subclass to unmarshall its state for persistence. The subclass
     * should read its state from the current offset of the given packet, and
     * update the current offset. There may be other data following the
     * subclass's state in the packet.
     */
    protected abstract void unmarshallState(RawCursor state) 
            throws ProtocolException;
    

    // Gets the next available channelID.
    protected synchronized Integer getNextChannelID() {

        // Will return the next channelID, that is not used yet
        // It will wrap around if we reach the end of the integer.
        // We synchronize this functio because we could have multiple threads
        // asking a channelID for their connections.
    
        int nextChannel = _nextChannelID++;
        if (nextChannel <= 0) {
            _nextChannelID = 1;
            nextChannel = 1;
        }
        Integer newChannelID = new Integer(nextChannel);

        while (_channelToConnection.containsKey(newChannelID)) {

            nextChannel = _nextChannelID++;
            if (nextChannel <= 0) {
                _nextChannelID = 1;
                nextChannel = 1;
            }
            newChannelID = new Integer(nextChannel);
        }

        return newChannelID;
    }

    
    protected secConnection createSecConnection() throws secErrors {
        return createSecConnection(false);
    }

    
    // Creates a new secure connection object for us.
    protected secConnection createSecConnection(boolean plain) throws secErrors{

        // Create a new secure connection globe object and return it.
        secConnection secConn;
        SOInf secConnCOSoi;

        try {
            // fetch the implementation
            if (plain) {
                secConnCOSoi = lns.bind(getContext(), 
                                        "repository/"+SECCON_PLAIN_IMPL);
            } else {
                secConnCOSoi = lns.bind(getContext(),
                                        "repository/"+SECCON_IMPL);
            }
        } catch (Exception e){
            DebugOutput.printException(DebugOutput.DBG_DEBUG, e);
            // Tell the caller that we couldn't find the implementation
            throw new secErrors_notFound();
        }

        SCInf secConnCOSci = null;
        SOInf secConnSoi = null;
        Integer channelID = getNextChannelID();
        try {
            // Create the globe object
            secConnCOSci = (SCInf) secConnCOSoi.swapInf(SCInf.infid);
            secConnSoi = StdUtil.createGlobeObject(secConnCOSci, getContext(),
                                                   SECCON_LNAME+channelID);
            // Get the COUNTED reference to secConnection interface
            secConn = (secConnection) secConnSoi.swapInf(secConnection.infid);
            secConn.setChannelID(channelID.intValue());
        } catch (Exception e) {
            DebugOutput.printException(DebugOutput.DBG_DEBUG, e);
            if (secConnCOSci == null) {
                // Error at swapInf, nothing to release (threw SOIerrors)
            } else if (secConnSoi == null) {
                // Error at createGlobeObject (threw Exception)
                secConnCOSci.relInf();
            } else {
                // Error at swapInf on secConnSoi
                secConnSoi.relInf();
            }
            // Tell the caller that we coulnd't find/get the interfaces
            throw new secErrors_notFound();
        }
        return secConn; // Counted
    }

    // SecSetLRID inf
    public void setLRID(long lrid) {
        _lrid = lrid;
    }
}
