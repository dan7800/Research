/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.lr.replication.std.base;

import vu.globe.svcs.objsvr.idl.persistence.*;  	// persistence.idl
import vu.globe.svcs.objsvr.idl.resource.*;     	// resource.idl
import vu.globe.svcs.objsvr.idl.resource;       	// resource.idl
import vu.globe.rts.std.idl.stdInf.*;          	// stdInf.idl
import vu.globe.idlsys.g;              	// g.idl
import vu.globe.rts.lr.idl.lrsub.*;       	// lrsub.idl
import vu.globe.rts.lr.idl.repl.*;		// repl.idl
import vu.globe.rts.comm.idl.p2p.*;		// p2p.idl
import vu.globe.rts.lr.idl.ctrl.*;		// ctrl.idl
import vu.globe.util.comm.idl.rawData.*;		// rawData.idl
import vu.globe.rts.runtime.ns.idl.nsTypes.*;	// nsTypes.idl

import vu.globe.rts.runtime.ns.nameSpaceImp.nameSpaceImp;
import vu.globe.rts.runtime.ns.nsConst;
import vu.globe.rts.java.GInterface;
import vu.globe.rts.lr.replication.skels.MinimalReplSkel;
import vu.globe.rts.lr.mgr.LRMgr;
import vu.globe.util.comm.*;
import vu.globe.rts.comm.gwrap.*;

import vu.globe.rts.lr.replication.std.ctrl.SafeCtrlCB;

import vu.globe.util.exc.AssertionFailedException;
import vu.globe.util.thread.Mutex;
import vu.globe.util.debug.DebugOutput;

import vu.globe.rts.runtime.ns.idl.context;
import vu.globe.rts.runtime.ns.nsConst;
import vu.globe.svcs.gls.idl.lsClient.*;
import vu.globe.util.idltypes.ObjectHandleOps;

import vu.globe.rts.security.idl.security.*;
import vu.globe.rts.security.secGenericConfig;

import vu.globe.rts.lr.replication.std.protocol.*;


import java.net.ProtocolException;
import java.util.*;

/**
 * Base class for replication objects. Repl may be used as a base class to any
 * replication object, whatever its replication strategy. The base class
 * carries out a number of tasks applicable to most replication objects:
 * <ul>
 * <le>The base class implements the LrSubObject interface. It does so in
 *     terms of other, abstract methods. Subclasses are responsible for
 *     filling in these abstract methods in accordance with their replication
 *     strategy.
 *
 * <le>Through the <code>safe_ctrl_cb</code> field the base class provides
 *     access to the control object, with thread-safe protection of the
 *     semantics object state. It also implements the replInit interface
 *     through which the control object installs its callback interface. 
 *
 * <le>The base class provides access to the communication subobject installed
 *     (if installed) by the LR.
 *
 * <le>The base class provides support for resource allocation in the resource
 *     manager. It also has support for persistence.
 * </ul>
 * <p>
 * The replication interface remains abstract.
 * The secConnectorCB interface remains abstract.
 *
 * Changes: included the security subobject, removed comm stuff
 *
 * Edited by: W.R. Dittmer
 */

public abstract class Repl extends MinimalReplSkel
{
    // The security subobject
    protected secConnector _secSubObj = null;       // uncounted
    
    // Map of channel to connection
    protected Map _channelToConnection = null;
   
    protected g.opaque _objectHandle = null;
    
    /** The control object's callback interface. */
    protected final SafeCtrlCB _safeCtrlCB = new SafeCtrlCB();  // uncounted

    /**
     * The persistence id of the LR that this replication object is a part of.
     * 'Invalid' if this LR is not persistent. However, undefined until
     * _pidDefined is set.
    */
    private long _pid = resource.invalidResourceID;

    /** Whether _pid is defined. */
    private boolean _pidDefined = false;

    // STATISTICS
    private long _lsmtime;

    /************************ Initialisation / Cleanup **********************/

    /**
     * May be overridden by the subclass to perform clean-up. This method is
     * called just before the replication object is destroyed.
     */
    protected void cleanup() throws SOIerrors {

        super.cleanup();

        if (_secSubObj != null) {
            _secSubObj.relInf();
        }
    }

    /**
     * May be overridden by the subclass to perform additional initialisation.
     * This method is called just after the replication object has been
     * instantiated.
     */
    protected void initState() throws SOIerrors {

        super.initState();
        SOInf secSoi = null;
        
        try {
            secSoi = lns.bind (getContext(), "../" + LRMgr.SEC_LNAME);
            _secSubObj = (secConnector) secSoi.swapInf(secConnector.infid);
        } catch (Exception exc) {
            if (secSoi != null) {
                secSoi.relInf();
            }
            // Without the security subobject, no dice
            throw new AssertionFailedException();
        }

        // Create our channelToMessenger map
        _channelToConnection = Collections.synchronizedMap(new HashMap());
    }


    /*************************** Control Object *****************************/

    // replInit interface
    public void installCallback(replCB cb) {
        _safeCtrlCB.installCallback(cb);
    }

    /************* Implementation of the LrSubObject Interface ***************/
    public void setObjectHandle(g.opaque objectHandle) {
        _objectHandle = objectHandle;
    } 
    
    public void setPerstID(long pid) throws subObjectErrors {

        // Set the persistent ID
        _pid = pid;
        _pidDefined = true;
    }

    public void prepareDestruction (short /* g.bool */ quick) 
            throws subObjectErrors {

        boolean my_quick = (quick == g.bool.True);

        // tell the connections that they will be closed.
        closeConnections(my_quick);

        // remove any resources we allocated at allocateResources
        deallocateResources(my_quick);
    }
 
    public void preparePassivation (short /* g.bool */ quick)
            throws subObjectErrors {
    
        // tell the connections that they are about to be closed
        closeConnections(quick == g.bool.True);
    }
 
    public rawDef completePassivation() {

        rawDef state = RawOps.createRaw();
        RawCursor cur = new RawCursor (state);
        marshallState(cur);
        return state;
    }
 
    public void prepareActivation(long pid, rawDef state,
                                  short /* g.bool */ recoverMode)
            throws subObjectErrors {

        _pid = pid;
        _pidDefined = true;
 
        RawCursor cur = new RawCursor(state);
        try {
            unmarshallState(cur);
        } catch (ProtocolException exc) {
            DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
            throw new subObjectErrors_invArg();
        }
    }

    public void completeActivation() throws subObjectErrors {
    
        try {
            // Open any connections with the other parties again
    	    openConnection();

            // Register our callback, which will also starts the listening
            secConnectorCB mySecConnectorCB =
                        (secConnectorCB) super.getCBInf(secConnectorCB.infid);
            _secSubObj.regSecConnectorCB(mySecConnectorCB, null);                       
        } catch (replDistrErrors_credsRevoked e) {
            DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "Repl: ERROR completeActivation: "+
                                "SLAVE CREDENTIALS REVOKED");
            throw new subObjectErrors_noAccess();
        } catch (Exception e) {
            DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "Repl: ERROR completeActivation "+
                                e.getMessage());
            throw new subObjectErrors_invOp();
        }
    }


    public void preparePassivationCheckpoint() throws subObjectErrors {
    
        pause(true);
    }

    public rawDef passivationCheckpoint() {

        rawDef state = RawOps.createRaw();
        RawCursor cur = new RawCursor (state);
        marshallState (cur);
        return state;
    }

    public void completePassivationCheckpoint() throws subObjectErrors {
        pause(false);
    }



    /**************** Implementation of replDistr interface ******************/
    
    public contactAddresses distributeObject() throws replDistrErrors {
       
        // Let the implementor allocateResources
        allocateResources();

        // Let the implementor start a connection with a replica/master
        openConnection();

        try {
            // Get the interface of the secConnectorCB
            secConnectorCB mySecConnectorCB = 
                        (secConnectorCB) super.getCBInf(secConnectorCB.infid);

            // Register our callback and get the contact info seq back
            return _secSubObj.regSecConnectorCB(mySecConnectorCB, null);
        } catch (secErrors_credsRevoked e) {
            throw new replDistrErrors_credsRevoked();
        } catch (Exception e) {
            throw new replDistrErrors_invOp();
        }
    }

    public long getLSMTime() throws replDistrErrors {
	
        long temp = 0L;

  	synchronized(this) {
	    temp = _lsmtime;
	}
	return temp;
    }

    /**************************** secConnectorCB interface *******************/
    // I think every subclass should decide on their own what to do with a new
    // connection. Let's keep it abstract here
    // public abstract void incomingConn(secConnection conn, g.opaque user)
    //        throws secErrors;

    public void listenStopped(g.opaque user) {
        // Don't know what to do when this comes in. Think that security
        // subobject pretty much handles everything.
    }

    
    /**************************** secMsgCB interface *************************/
    /** Cleans up everything we know of this channel. The subclass should
     * override this class and clean up its own stuff too.
     */
    public void receiveStopped(int channel, g.opaque user) {
       
        // Remove this connection from the hashmap
        Integer channelID = new Integer(channel);
        _channelToConnection.remove(channelID);
    }
    
    /** Fetches the message, prepares it and then calls one of the abstract
     * methods handlePlain, handleRequest or handleReply to be implemented by
     * the subclasses.
     */
    public void msgReceived(int channel, g.opaque user) {
   
        // gets the message from the channel
        Integer channelID = new Integer(channel);
       
        // get the connection of this channel
        secConnection secConn = 
                           (secConnection) _channelToConnection.get(channelID);

        if (secConn == null) {
            // This is very very weird. A message came in after a
            // receiveStopped();
            DebugOutput.println(DebugOutput.DBG_DEBUG,
                           "Repl: Received message on non-existent channel ("+
                           channel+")");
            return;
        }
       
        ReplMessage msg = null;
        try {
            // get our packet
            rawDef packet = secConn.receive();
            
            RawCursor cursor = new RawCursor(packet);
            msg = unmarshallMessage(cursor);

        } catch (Exception e) {
            DebugOutput.println(DebugOutput.DBG_NORMAL,
                            "Repl: Could not get or unmarshall message from "+
                            "channel: "+channel);
            // Well couldn't get the message or couldn't unmarshall it
            // FIXME secViolation ?
            return; // FIXME ignoring for now
        }
         
        if (msg instanceof ReplRequest) {
            handleRequest(channelID, (ReplRequest) msg);
        } else if (msg instanceof ReplReply) {
            handleReply(channelID, (ReplReply) msg);
        } else {
            handlePlain(channelID, (ReplPlainMessage) msg);
        }
    }

    /**************************** Statistics ****************************/

    /**
     *
     * This method is called by the subclasses (or their helpers)
     * of this class when the replication subobject is notified of a change to
     * the state of the distributed object (e.g. receives a state invalidate,
     * new state, or write-method invocation).
     * @see vu.globe.rts.lr.replication.std.base.Repl#getLSMTime
     *
     * Can't be protected because the current subclasses use a lot of
     * helper classes which turn out to be the place where this
     * method should be called.
     */
    public void sawUpdate() {
    
        synchronized(this) {
	    _lsmtime = System.currentTimeMillis();
	}
    }
  
    /******************** Persistence and Communication **********************/


    /**
     * Returns true iff the LR that this replication object is a part of is a
     * persistent local object. This information is not known during initState()
     * and configure() invocations.
     *
     * @exception IllegalStateException	if the result is not yet known
     */
    protected boolean isPersistent() {
    
        if (! _pidDefined) {
            throw new IllegalStateException ("pid not yet defined");
        }
        return _pid != resource.invalidResourceID;
    }

    /**
     * Returns the persistence id of the LR that this replication object is a
     * part of, or 'invalid' if this LR is not persistent. This information is
     * not known during initState() and configure() invocations.
     *
     * @exception IllegalStateException	if the result is not yet known
     */
    protected long getPID() {

        if (! _pidDefined) {
            throw new IllegalStateException ("pid not yet defined");
        }
        return _pid;
    }
  
    /**
     * Allows a subclass to perform actions for the initial creation of a
     * (persistent or transient) replica.
     */
    protected void allocateResources() throws replDistrErrors {
        // let sub class implement if needed
    };
  
    /**
     * Allows a subclass to perform actions to prepare for the final destruction
     * of a (persistent or transient) replica. Typically the subclass will
     * deallocate resources from the resource manager.
     *
     * @param quick	if true, only local operations and non-wide-area
     *			communication may be performed
     */
    protected void deallocateResources(boolean quick) throws subObjectErrors {
        // let subclass implement if needed
    }
  
    /**
     * Allows a subclass to connect to a master or a slave. To open a connection
     * the repl should call getChannel() on the security subobject.
     */
    protected abstract void openConnection() throws replDistrErrors;
  
    /**
     * Allows a subclass to perform actions to prepare for the final destruction
     * of a (persistent or transient) replica and for the passivation of a
     * persistent replica. Typically the subclass will close its communication
     * links with other replicas.
     *
     * @param quick	if true, only local operations and non-wide-area
     *			communication may be performed
     */
    protected abstract void closeConnections(boolean quick) 
            throws subObjectErrors;
  
    /**
     * Pauses or resumes activity.
     */
    protected abstract void pause(boolean on) throws subObjectErrors;
   
    /**
     * Allows the subclass to marshall its state for persistence. The subclass
     * should write its state to the current offset of the given packet, and
     * update the current offset.
     */
    protected void marshallState(RawCursor state) {

        synchronized(this) {
      	    RawBasic.writeInt64(state, _lsmtime);
        }
    }
   
    /**
     * Allows the subclass to unmarshall its state for persistence. The subclass
     * should read its state from the current offset of the given packet, and
     * update the current offset. There may be other data following the
     * subclass's state in the packet.
     */
    protected void unmarshallState(RawCursor state) throws ProtocolException {
  
        synchronized(this) {
  	    _lsmtime = RawBasic.readInt64(state);
        }
    }

    /************************ unmarshallMessage ******************************/
    /** Unmarshall the message from the current offset of the packet. The
     * current offset is updated. The message is unmarshalled according to
     * ReplMessageFact. A client whishing to adapt the message protocol should
     * override this method.
     */
    protected ReplMessage unmarshallMessage(RawCursor packet)
            throws ProtocolException {
        return ReplMessageFact.unmarshallMessage(packet);
    }

    /************************ Handle* methods ********************************/
    
    // Handle Request, handles the request
    protected abstract void handleRequest(Integer channelID, ReplRequest req);

    // Handle an incoming reply, find the matching request thread
    protected abstract void handleReply(Integer channelID, ReplReply reply);

    // Handle an incoming plain message.
    protected abstract void handlePlain(Integer channelID, 
                                        ReplPlainMessage plain);
}
