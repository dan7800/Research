/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.security;

import vu.globe.idlsys.g;

import vu.globe.rts.lr.idl.lrsub.*;
import vu.globe.rts.lr.replication.std.rpc.*;

import vu.globe.rts.runtime.ns.*;
import vu.globe.rts.runtime.ns.idl.ns.*;
import vu.globe.rts.runtime.ns.idl.nsTypes.*;

import vu.globe.rts.comm.gwrap.*;
import vu.globe.rts.comm.idl.p2p;               // p2p.idl
import vu.globe.rts.comm.idl.p2p.*;
import vu.globe.util.comm.idl.rawData.*;

import vu.globe.rts.security.*;
import vu.globe.rts.security.base.*;
import vu.globe.rts.security.certs.*;
import vu.globe.rts.security.idl.security.*;
import vu.globe.rts.security.idl.secconfig.*;
import vu.globe.rts.security.idl.sectypes.*;
import vu.globe.rts.security.protocol.SecAddress;
import vu.globe.rts.security.protocol.messages.*;

import vu.globe.rts.std.idl.stdInf.*;
import vu.globe.rts.std.StdUtil;
import vu.globe.rts.std.idl.configure.*;

import vu.globe.util.comm.*;
import vu.globe.util.exc.AssertionFailedException;
import vu.globe.util.debug.DebugOutput;
import vu.globe.util.parse.AttributeString;

import vu.globe.svcs.objsvr.idl.resource;
import vu.globe.svcs.objsvr.idl.resource.*;
import vu.globe.svcs.objsvr.idl.management.*;
import vu.globe.svcs.gls.idl.lsClient.*;
import vu.globe.svcs.gls.idl.typedAddr.*;
import vu.globe.svcs.gls.types.PropertyMap;
import vu.globe.svcs.gls.types.PropertySelector;
import vu.globe.tools.tika.util.IdlTypesTools;
import vu.globe.util.idltypes.PropertyMapOps;

import vu.globe.rts.runtime.ns.nameSpaceImp.nameSpaceImp;

import java.util.*;
import java.net.ProtocolException;
import java.security.*;
import java.security.cert.Certificate;

import vu.globe.util.idltypes.PropertySelectorOps;

import java.security.Signature;

/** The main security object implementation
 * 
 * We currently support three modes:
 *
 * +-----------------------------------------------------------------------+
 * | Mode:      | listener | connector | refreshCRL | getCRL | getWholeCRL |
 * +-----------------------------------------------------------------------+
 * | Chief      |     X    |           |     X      |        |             |
 * | Sergeant   |     X    |     X     |     X      |    X   |      X      |
 * | Recruit    |          |     X     |            |    X   |             |
 * +-----------------------------------------------------------------------+
 *
 * Chief: Listens to incoming connections, does not make outgoing connections
 *        Will regularly refresh the CRLs
 *
 * Sergeant: Listens to incoming connections, only makes one connection to the
 *           Chief
 *           Will regularly get a new WHOLE (meaning replica + clients) CRL
 *
 * Recruit: Only makes outgoing connections
 *          Will get a CRL (with only revoked replicas) once
 * 
 * @author W.R. Dittmer
 */

public class secGeneric extends SecConnector {

    // The IDLLRMgr config
    private IDLLRMgrConfig _cfg = null;
        
    // The connector object to create new connections with
    private connector _connector = null;                // counted

    // The contact points we offer.
    public static final int INSECURE_CP = 0;
    public static final int SECURE_RIGHTS_CP = 1;
    public static final int AUTHENTICATING_RIGHTS_CP = 2;      // FIXME NOT USED
    public static final int NR_CPS = 2;         // THUS 2 here

    // The listener object to listen for incoming connections
    private listener[] _listener = null;                  // counted

    // The contactExporter interface of the globe listener object
    private contactExporter[] _contactExporter = null;    // uncounted

    // The comm interface of the globe listener object
    private comm[] _listenerComm = null;                  // uncounted

    // The contact point we are listening on
    private String[] _contactPoint = null;

    // The resource id for our contact point
    private long[] _cpRid = null;

    // Tells us if we are closed
    private boolean _closing = false; 
    private boolean[] _closed;

    // Contact addresses
    private contactAddresses _lsAddrs = null;

    // Binding/Plain connections:
    protected Map _plainChannelToConnection = null;
    
    // The local name of the listener object
    public static final String LISR_LNAME = "listener";

    // Things that we should get via constructor: FIXME FIXME FIXME
    private String SEC_CLIENT_CONFIG = "";
    
    // * minimum LS Lookup value
    private int LS_MIN = 1;

    // * maximum LS Lookup value
    private int LS_MAX = 10;

    // Our credentials manager, containing credentials list and CRL
    private CredsManager _credsManager = null;
    
    // Do we have a connector object
    private boolean _hasConnector = false;

    // Do we have listeners
    private boolean _hasListeners = false;

    // The following variable is VERY important. It enables or disables
    // the usage of the TLS secConnections and it will also ignore access
    // control rules, except when the replication subojbect does a getChannel,
    // because then we need to know if a channel exists that can perform that
    // method. This to prevent writes going to a slave or a slave trying to
    // register with clients. In stead of SSLSecConnections, PlainSecConnections
    // will be used.
    
    private boolean _securityEnabled = true;
   
    // switches that toggle the behaviour of the credentials manager

    // Should it fetch the new CRL at regular intervals ?
    private boolean _getCRL = false;

    // Should it fetch the COMPLETE (client+replica) CRL
    private boolean _getWholeCRL = false;

    // Should it refresh the CRL regularly ?
    private boolean _refreshCRL = false;

    // What is the interval time to refresh it ?
    private long _interval = secGenericConfig.DEFAULT_INTERVAL_TIME;

    // RpcTable contains our outgoing request for which we wait for replies
    private RpcTable _rpcTable = null;

    // object server ops interface helper
    private SOInf _objSvrSOI = null;        // counted

    // object server ops interface
    private objectServerOps _objSvrOps = null;   // uncounted

    
    /////////////////////////////// KEYS and stuff (only loaded when needed).
    private KeyStore _issuerCredsKS = null;
    private KeyStore _issuerAdminKS = null;
    private KeyStore _caCertsKS = null;
    private Certificate[] _myKeyChain = null;
    private PrivateKey _privKey = null;

    // Set with blacklisted addresses of replicas
    private Set _doNotCallList;

    // Set a object to synchronize on when the Chief is down and we
    // can't get our CRLs.
    private Boolean _pauseLock = new Boolean(false);
    private boolean _noCRLMode = false;
    private boolean _paused = false;
    private static long _ONE_MINUTE = 6000; // FIXME HACK to 6 seconds
    private long _noCRLInterval;

    
    /*************** Initialization and cleanup ******************************/
    protected void cleanup() throws SOIerrors {

        super.cleanup();

        // Close the CredsManager
        if (_credsManager != null) {
            _credsManager.stop();
            _credsManager = null;
        }
        
        // Release the connector's interface
        if (_hasConnector && _connector != null) {
            _connector.relInf();
        }

        
        // Release the listener's interface
        if (_hasListeners) {
            for (int i=0; i < NR_CPS; i++) {
                if (_listener[i] != null) {
                    _listener[i].relInf();
                }
            }
        }

        if (_objSvrSOI != null) {
            _objSvrSOI.relInf();
        }
    }

    /************** Implementation of secConnector interface *****************/
    public void constructor(String init, IDLLRMgrConfig CFG) {

        super.constructor(init, CFG);
    
        // Parse the init string and set hasConnector and hasListeners
        AttributeString attString = new AttributeString(init);

        if (attString.get(secGenericConfig.SEC_OFF_STR) != null) {
            _securityEnabled = false;
            DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "secGeneric: SECURITY HAS BEEN DISABLED !");
        }
        
        if (attString.get(secGenericConfig.CONN_ATTR_STR) != null) {
            _hasConnector = true;
        }

        if (attString.get(secGenericConfig.LISR_ATTR_STR) != null) {
            _hasListeners = true;
        }

        if (attString.get(secGenericConfig.GET_CRL_ATTR_STR) != null) {
            _getCRL = true;
        }

        if (attString.get(secGenericConfig.GET_WHOLE_CRL_ATTR_STR) != null) {
            _getWholeCRL = true;
        }

        if (attString.get(secGenericConfig.REFRESH_CRL_ATTR_STR) != null) {
            _refreshCRL = true;
        }

        if (attString.get(secGenericConfig.INTERVAL_CRL_ATTR_STR) != null) {
            _interval = new Long(attString.get(
                        secGenericConfig.INTERVAL_CRL_ATTR_STR)).longValue();
            DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "secGeneric: CRL Refresh interval set to: "+
                                _interval);
        }

        if (_hasConnector) {
            // Create the connector object
            // First get the communication subobject
            SOInf soi = super.getCommObject();  // counted

            try {
                _connector = (connector) soi.swapInf(connector.infid);
            } catch (SOIerrors e) {
                soi.relInf();   // Release counted interface
                throw new RuntimeException("SecGeneric:ERROR: could not create"+
                                           " connector");
            }
        }

        if (_hasListeners) {
            // Listeners need extra list
            _plainChannelToConnection =
                                    Collections.synchronizedMap(new HashMap());

            // Initialize a whole bunch of stuff
            _listener = new listener[NR_CPS];
            _contactExporter = new contactExporter[NR_CPS];
            _listenerComm = new comm[NR_CPS];
            _contactPoint = new String[NR_CPS];
            _cpRid = new long[NR_CPS];
            _closed = new boolean[NR_CPS];

            for (int i=0; i < NR_CPS; i++) {
                _cpRid[i] = resource.invalidResourceID;
                _closed[i] = true;
            }
        }       

        // MUST INIT THE _myAC (must do here, because in base I'm not supposed
        // to know which type of cert is used
        // FIXME Have initstring contain class instance that we load
        // dynamically?
        if (_securityEnabled) {
            try {
	        DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "secGeneric: *NOT* Checking the creds I got");
                _myAC = new X509AccessControl(CFG.creds);
            } catch (Exception e) {
                // Dude ?
	        DebugOutput.println(DebugOutput.DBG_NORMAL, 
                    "secGeneric: FATAL ERROR: Can't load own ACCESS CONTROL");
            }
        } else {
            // Security is disabled, use the dummy dummy
            _myAC = new NoAccessControl();
        }
        
        _cfg = CFG;

        // Create rpcTable
        _rpcTable = new RpcTable();
        
        // Initialize our Credentials Manager if security enabled
        if (_securityEnabled) {
            try {
                timerResourceManager tRscMgr = null;
                timerResourceManagerCB myTimerRscCB = null;
                // Only load the timerResourceManager if we actually need it
                if (_refreshCRL) {
                    tRscMgr = super.getTimerResourceManager();
                    myTimerRscCB = (timerResourceManagerCB)
                                  super.getCBInf(timerResourceManagerCB.infid);
                }
                _credsManager = new CredsManager(tRscMgr, myTimerRscCB);
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                    "secGeneric: ERROR: could not create CredsManager!!");
                throw new RuntimeException(e);
            }
        }

        _doNotCallList = Collections.synchronizedSet(new HashSet());
    }

    
    public secConnection getChannel(int method) throws secErrors {

        // You are not allowed to do a getChannel operation when you don't have
        // a connector.
        if (!_hasConnector) 
	{
            DebugOutput.println(DebugOutput.DBG_NORMAL,
                "secGeneric: ERROR: getChannel, while not having a connector!");
            throw new secErrors_invOp();
        }
        
        // method is already the right number added with all the offsets
        // of repl and sem and sec needed etc.
	DebugOutput.println(DebugOutput.DBG_DEBUG,
                            "secGeneric: Get Channel for method: "+method);

        // Check if we are allowed to request this method in the first place:
        if (!_myAC.forwardAllowed(method)) 
	{
	    DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "secGeneric: YOU ARE NOT ALLOWED TO CALL THIS"+
                                " METHOD IN THE FIRST PLACE !");
            throw new secErrors_secViolation();
        }

        // Go over each channel's certificate until you find one that allows
        // this method
        Set s = _channelToAC.keySet();

        // Synchronize ! See
        // http://java.sun.com/j2se/1.4/docs/api/java/util/Collections.html#synchronizedMap(java.util.Map)
        synchronized (_channelToAC) {
            Iterator it = s.iterator();
            while (it.hasNext()) {
                Integer channelID = (Integer) it.next();
                AccessControl ac = (AccessControl) _channelToAC.get(channelID);

                // Check if the method execution is allowed on cert's owner
                int tempMethod = method;
                if (!_securityEnabled) {
                    // make the number negative, indicating to NoAccessControl
                    // to return false if no RACB otherwise RACB.bit value.
                    try {
                        tempMethod = Integer.parseInt("-"+method);
                    } catch (NumberFormatException e) {
                        DebugOutput.println(DebugOutput.DBG_NORMAL,
                                             e.getMessage());
                    }
                }

                if (ac.reverseAllowed(tempMethod)) {
                    // Get the secure connection that belongs to this channel
                    // and return it.
                    return (secConnection) _channelToConnection.get(channelID);
                }
            }
        }
        
        // If we get here, we should ask the Location Service for a contact
        // point that allows our method

        contactAddresses cAddrs = null;
        cAddrs = lookatPreinstalledCaddrs( method );
        boolean preinstalled = (cAddrs != null);
        while( true )
        {
            if (!preinstalled)
            {
                // No preinstalled caddr, or we couldn't properly
                // connect to the preinstalled.
                //
                cAddrs = lsLookup( method );
                if (cAddrs == null || cAddrs.v.length == 0) 
                {
                    // We could not find any channel matching the request
                    throw new secErrors_noChannel();
                }
            }
            try
            {
                secConnection conn = tryCAddrs( method, cAddrs );
                return conn;
            }
            catch( secErrors_noChannel x )
            {
                if (preinstalled)
                {
                    // Preinstalled didn't work, try again with caddrs from
                    // location service
                    preinstalled = false;
                    continue;
                }
                else
                    throw x;
            }
        }
    }

    
    private secConnection tryCAddrs( int method, contactAddresses cAddrs )
    throws secErrors 
    {
        // Connect to what the LS gave us
        Integer channelID = null;
        for (int i=0; i < cAddrs.v.length; i++) {
            // Connect to what the LS gave us
            secConnection secConn = null;
            connection replica = null;
            try {
                contactAddressInfo cinfo =
                                _idManager.getContactAddressInfo(cAddrs.v[i]);
                typedAddressDef ta = cinfo.address;
                if (ta.typedAddressDef_kind() != typedAddressID.Stacked) 
		{
                    DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "secGeneric: contact address of wrong type");
                    continue;
                }
                stackedAddress sa = ((typedAddressDef_stacked) ta).stacked;
                stackedComponentDef sCD = sa.v[0];
                compVariant_unknown cVU = (compVariant_unknown) sCD.var;
                unknownComponentDef uCD = cVU.unknown;

                rawDef rawAddress = ((unknownAddress_bytes) uCD.address).bytes;
                String address = new String(RawOps.getRaw(rawAddress));

                if (_doNotCallList.contains(address)) {
                    DebugOutput.println(DebugOutput.DBG_DEBUG,
                            "secGeneric: skipping blacklisted: "+address);
                    continue;
                }
    
		DebugOutput.println(DebugOutput.DBG_DEBUG,
                                    "secGeneric: trying replica at: "+address);
                // connect to the replica
                replica = _connector.connect(address);

                if (_securityEnabled) {
                    // Create a secure connection for it
                    secConn = super.createSecConnection();
                } else {
                    // Create plain connection for it
                    secConn = super.createSecConnection(true);
                }

                short checkRootIsCA = g.bool.False;
                
                // call the contructor, let both sec and repl messages through
                // Use the IDLIdentCerts + IDLObjectCreds combo
                secConn.constructor(SecConnection.SECCON_INIT_CLIENT_BOTH,
                                    replica, _cfg.cacerts, null, _cfg.creds,
                                    checkRootIsCA);

                // Call handshake
                IDLGenericCerts peerCertChain = secConn.handshake();

                AccessControl ac = null;

                if (_securityEnabled) {
                    // Create the AccessControl stuff FIXME (SPKI/SDSI ?)
        	    DebugOutput.println(DebugOutput.DBG_DEBUG,
                              "secGeneric: Checking peer's certificate chain:");
                    ac = new X509AccessControl(peerCertChain);

                    // First check that the new connection allows the requested
                    // method because the Location Service address selection bit
                    // could be spoofed.
                    if (!ac.reverseAllowed(method)) {
                        DebugOutput.println(DebugOutput.DBG_DEBUG,
                                        "secGeneric: SPOOFED address "+
                                        "selection bits for "+address);
                        secConn.closeConn( closeReason.secViolation );

                        // Try another connection
                        continue;
                    }

                    // Now check if this connection's certificate has been
                    // revoked (not not revoked)
                    if (_credsManager.isRevoked(ac.getCertificateChain()) 
                        != secGenericConfig.NOT_REVOKED) {
                        DebugOutput.println(DebugOutput.DBG_DEBUG,
                                        "secGeneric: Tried to connect to "+
                                        "REVOKED replica on "+address);
                        secConn.closeConn(closeReason.secViolation);

                        // Add it to the blacklist.
                        _doNotCallList.add(address);
                        
                        // Try another connection 
                        continue;
                    }   
                } else {
                    // Use our dummy, use the property selection bits as RACB
                    // The RACB is not used for security checks but to be able
                    // to select the right channel.
                    ac = new NoAccessControl(cinfo.props);
                }
                
                // Register our callback
                secConnectionCB mySecConnectionCB = (secConnectionCB)
                                        super.getCBInf(secConnectionCB.infid);
                secConn.regSecConnectionCB(mySecConnectionCB, null);

                // register our secMsgCB callback, currently ALL connections
                // have this callback.
                secMsgCB mySecMsgCB = (secMsgCB) super.getCBInf(secMsgCB.infid);
                secConn.regSecMsgCB(mySecMsgCB, SecConnection.SEC_OBJ, null);

                // Add the connection to connection and certificate list
                channelID = new Integer(secConn.getChannelID());
                _channelToConnection.put(channelID, secConn);
                _channelToAC.put(channelID, ac);

                // return the connection
                return secConn;
            } catch (Exception e) {
                _channelToConnection.remove(channelID);
                if (secConn == null) {
                    replica.closeConn();
                    replica.relInf();
                } else {
                    secConn.closeConn(closeReason.errorOccured);
                }
            }
	    DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "secGeneric: Failed to connect to address #"+i);
        }

        // We exhausted our search options, there is no channel
        throw new secErrors_noChannel();
    }


    /************* Implementation of secConnectionCB interface ***************/
    public void connEstablished(int channel, IDLGenericCerts certs,
                                g.opaque user) throws secErrors {

        if (!_hasListeners) 
				    {
	    DebugOutput.println(DebugOutput.DBG_NORMAL, "secGeneric: Weird not"+
                                " listening and still getting connections!");
            secConnection secConn = (secConnection)
                                _channelToConnection.get(new Integer(channel));
            secConn.closeConn(closeReason.secViolation);
            throw new secErrors_invOp();
        }
        
	DebugOutput.println(DebugOutput.DBG_DEBUG, 
                            "secGeneric: HANDSHAKE DONE for channel: "+channel);
        // Tells the security subobject that a new connection is ready
        Integer channelID = new Integer(channel);
        secConnection conn = null;
        
        try {
            if (_plainChannelToConnection.containsKey(channelID)) {
                // Plain INSECURE stuff
                conn = (secConnection) _plainChannelToConnection.get(channelID);
                
            } else {
                conn = (secConnection) _channelToConnection.get(channelID);
                
                AccessControl ac = null;
                if (_securityEnabled) {
                    // Create the AccessControl stuff FIXME SDSI/SPKI
                    DebugOutput.println(DebugOutput.DBG_DEBUG,
                                    "secGeneric: Storing peer's chain: ");
                    ac = new X509AccessControl(certs);

                    // Now check if this connection's certificate has not been
                    // revoked
                    int rev = _credsManager.isRevoked(ac.getCertificateChain());
                    if (rev != secGenericConfig.NOT_REVOKED) {
                        if (rev == secGenericConfig.REVOKED_REPLICA) {
                            // try sending the Sergeant a self-destruct message.
                            // It can occur that the OSManager as not been
                            // loaded yet and thus the Sergeant can't
                            // remove itself. At its next request we will send
                            // it an ErrorReply so that activation will fail and
                            // thus clean itself up.
                            DebugOutput.println(DebugOutput.DBG_DEBUG,
                                            "secGeneric: Connection from "+
                                            "REVOKED REPLICA");
                            int selfDestructMID = 
                                secGenericConfig.getReverseSecMethodIDMap().
                                indexOf(secGenericConfig.METHOD_SELF_DESTRUCT);

                            SecMessage selfDestruct = 
                                    new SecSelfDestructRequest(selfDestructMID);

                            // Marshall the message
                            RawCursor cursor=new RawCursor(RawOps.createRaw());
                            selfDestruct.marshall(cursor);

                            try {
                               conn.send(cursor.getRaw(),SecConnection.SEC_OBJ);
                            } catch (Exception e) {
                               DebugOutput.println(DebugOutput.DBG_DEBUG,
                              "secGeneric: ERROR: Could not send reply");
                            }
                        } else {
                            DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "secGeneric: Connection from REVOKED CLIENT");
                        }
                        ac.setCertificateRevoked();
                    }
                } else {
                    // Use the dummy
                    ac = new NoAccessControl();
                }
   
                // Put the ac in our list
                _channelToAC.put(channelID, ac);
    
                // Tell the replication subobject about the new connection:
                _replSubObj.incomingConn(conn, _replUser);
            }

            // register our secMsgCB callback, currently ALL connections have
            // this callback.
            secMsgCB mySecMsgCB = (secMsgCB) super.getCBInf(secMsgCB.infid);
            conn.regSecMsgCB(mySecMsgCB, SecConnection.SEC_OBJ, null);
            
        } catch (Exception e) {
            DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "secGeneric: ERROR: "+e.getMessage());

            // Do as if this connection never existed:
            _channelToAC.remove(channelID);
            _channelToConnection.remove(channelID);
            _plainChannelToConnection.remove(channelID);

            // Tell the secConnection that we didn't accept it.
            throw new secErrors_misc();
        }
    }
    

    public void connClosed(int channel, short /* closeReason */ reason,
                           g.opaque user) 
    {

	DebugOutput.println(DebugOutput.DBG_DEBUG,
                            "secGeneric: ConnClosed for channel: "+channel);
        // Tells the security subobject that the connection was closed
        // If the reason is secViolation or whatever, put connection in
        // blacklist.
        secConnection rmConn;
        Integer channelID = new Integer(channel);

        // remove the connection from our list
        if (_hasListeners 
            && _plainChannelToConnection.containsKey(channelID)) {
            rmConn = (secConnection)_plainChannelToConnection.remove(channelID);
        } else {
            rmConn = (secConnection) _channelToConnection.remove(channelID);
        }

        if (rmConn != null) {
            // Release the interface
            rmConn.relInf();
        }

        // Check if we should add it to the blacklist
        switch (reason) {
            case closeReason.secViolation: {
                // put in blacklist ?
                break;
            } default: {
                // Do nothing
            }
        }

        // remove the certificate from our list
        _channelToAC.remove(channelID);

        // All threads waiting for RPC messages from this connection should
        // be notified of the closure
        _rpcTable.cancelFor(channelID, "Channel closed");

        // If we are closing notify the thread if the list(s) is(are) empty
        if (_closing) {
            // closeConn was called
            if (_channelToConnection.isEmpty()) {
                // At least that list is empty
                if (_hasListeners) {
                    // Check that the _plainChannelToConnection is empty too
                    if (_plainChannelToConnection.isEmpty()) {
                        // Notify
                        synchronized (this) {
                            this.notify();
                        }
                    }
                } else {
                    // Notify
                    synchronized (this) {
                        this.notify();
                    }
                }
            }
        }
    }


    /************* Implementation of listenCB interface **********************/
    public void listenStopped(g.opaque user) {

        // Were we supposed to listen ?
        if (!_hasListeners) {
	    DebugOutput.println(DebugOutput.DBG_NORMAL, "secGeneric: ERROR,"+
                                " not listening but getting stopped !");
            return;
        }
        
        // Our notification service informs us that we stopped listening..
        int i = ((Integer) ((g.opaque_pointer) user).pointer).intValue();
        synchronized(this) {
            _closed[i] = true;
            this.notify();
        }
    }

    public void connArrived(g.opaque user) {

        if (!_hasListeners) {
	    DebugOutput.println(DebugOutput.DBG_NORMAL, "secGeneric: ERROR,"+
                                " not listening but getting connections !");
            return;
        }
        
        // Notification that a connection was made
        // Find out which CP was the lucky one:
        int whichCP = ((Integer) ((g.opaque_pointer) user).pointer).intValue();
    
        // Allow access to sec only for plain connections. Allow both repl
        // and sec access for other connections.
        String init = SecConnection.SECCON_INIT_SERVER_BOTH;

        
        String cp = null;
        boolean plain = false;
        if (whichCP == AUTHENTICATING_RIGHTS_CP) {
            cp = "AUTHENTICATING RIGHTS CP";
        } else if (whichCP == SECURE_RIGHTS_CP) {
            cp = "SECURE RIGHTS CP";
        } else {
            cp = "INSECURE CP";
            plain = true;
            init = SecConnection.SECCON_INIT_SERVER_SEC_ONLY;
        }

	DebugOutput.println(DebugOutput.DBG_DEBUG,
                            "secGeneric: new connection on "+cp);
	        
        secConnection newSecConn = null;

        // Accept the connection
        connection newConn = null;
        Integer channelID = null;
        try {
            // Accept the connection
            newConn = _listener[whichCP].accept();

            // create a new secure connection object
            if (_securityEnabled) {
                newSecConn = super.createSecConnection(plain);
            } else {
                // Make a plain secConnection object using no TLS
                newSecConn = super.createSecConnection(true);
            }

            short checkRootIsCA = g.bool.False;

            // call our constructor
            // Use the IDLIdentCerts + IDLObjectCreds combo
            newSecConn.constructor(init, newConn, _cfg.cacerts,
                                   null, _cfg.creds, checkRootIsCA);

            // Add this entry to our table
            // There reason why we already add it here and not at the end is
            // because popup threads will wait till the callback is registered
            // before they do anything. This tiny time between having it
            // registered and adding it to the list can cause problems because
            // if we have just registered our callback and were about to insert
            // it in the list, but the pop-up thread becomes active now, it
            // can't find the connection in the list yet !
            // So we add it before any pop-up threads need access to the
            // connection list so it will always be there when they need it.
            channelID = new Integer(newSecConn.getChannelID());
            if (plain) {
                _plainChannelToConnection.put(channelID, newSecConn);
            } else {
                _channelToConnection.put(channelID, newSecConn);
            }

            // Put the CP id in the user
            g.opaque_pointer connUser = new g.opaque_pointer();
            connUser.pointer = new Integer(whichCP);

            // register our callback
            secConnectionCB mySecConnectionCB = (secConnectionCB)
                                         super.getCBInf(secConnectionCB.infid);
            newSecConn.regSecConnectionCB(mySecConnectionCB, connUser);

        } catch (Exception e) {
            if (newSecConn == null) {
                newConn.closeConn();
                newConn.relInf();
            } else {
                newSecConn.closeConn(closeReason.errorOccured);
                // remove it from the list
                if (plain) {
                    _plainChannelToConnection.remove(channelID);
                } else {
                    _channelToConnection.remove(channelID);
                }
            }
        }
    }

    /****************** Implementation of the secMsgCB interface ************/
    public void msgReceived(int channel, g.opaque user) {

        // Indicate plain connection or regular (secure) connection
        boolean plain = false;
        
        // Get the message from the channel
        Integer channelID = new Integer(channel);

        // Find the connection belonging to this channel
        // Assumption: More messages will be sent over secure channels
        // then over plain channels, so search secure channels first
        secConnection conn = (secConnection)
                                        _channelToConnection.get(channelID);

        if (conn == null) {
            // Okay try the plain connections instead
            conn = (secConnection) _plainChannelToConnection.get(channelID);

            if (conn == null) {
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                    "secGeneric: Received message on non-existent channel ("
                    +channel+")");
                return;
            }
            // Tell that it concerns a plain connection
            plain = true;
        }

        SecMessage msg = null;
        try {
            // get our packet
            rawDef packet = conn.receive();

            RawCursor cursor = new RawCursor(packet);
            msg = SecMessageFact.unmarshallMessage(cursor);
        } catch (Exception e) {
            // Well couldn't get the message or couldn't unmarshall it
            DebugOutput.println(DebugOutput.DBG_NORMAL, 
                                "secGeneric: ERROR: Could not receive or"+
                                " unmarshall message");
            // FIXME secViolation ?
            conn.closeConn(closeReason.errorOccured);
            return;
        }

        if (msg instanceof SecRequest) {
            handleRequest(conn, channelID, (SecRequest) msg, plain);
        } else if (msg instanceof SecReply) {
            handleReply((SecReply) msg);
        } else {
            // Unknown message FIXME What to do ?
            DebugOutput.println(DebugOutput.DBG_NORMAL,
                    "secGeneric: ERROR: Unknown message received on channel "
                    +channel);
            conn.closeConn(closeReason.errorOccured);
        }
        
    }

    private void handleRequest(secConnection conn, Integer channelID,
                               SecRequest msg, boolean plain) {
    
        // Check that methodID and request type match
        int methodID = 0;
        int reqMethodID = 1;
        
        if (msg instanceof SecGetBlueprintsRequest) {
            methodID = secGenericConfig.getReverseSecMethodIDMap().
                            indexOf(secGenericConfig.METHOD_GET_BLUEPRINTS);
            reqMethodID = ((SecGetBlueprintsRequest) msg).getMethodID();
            DebugOutput.println(DebugOutput.DBG_DEBUG, "-> GET BLUEPRINTS");
        } else if (msg instanceof SecGetCredentialsRequest) {
            methodID = secGenericConfig.getReverseSecMethodIDMap().
                            indexOf(secGenericConfig.METHOD_GET_CREDENTIALS);
            reqMethodID = ((SecGetCredentialsRequest) msg).getMethodID();
            DebugOutput.println(DebugOutput.DBG_DEBUG, "-> GET CREDENTIALS");
        } else if (msg instanceof SecUpdateCredentialsRequest) {
            methodID = secGenericConfig.getReverseSecMethodIDMap().
                            indexOf(secGenericConfig.METHOD_UPDATE_CREDENTIALS);
            reqMethodID = ((SecUpdateCredentialsRequest) msg).getMethodID();
            DebugOutput.println(DebugOutput.DBG_DEBUG, "-> UPDATE CREDENTIALS");
        } else if (msg instanceof SecRevokeCertificateRequest) {
            methodID = secGenericConfig.getReverseSecMethodIDMap().
                            indexOf(secGenericConfig.METHOD_REVOKE_CERTIFICATE);
            reqMethodID = ((SecRevokeCertificateRequest) msg).getMethodID();
            DebugOutput.println(DebugOutput.DBG_DEBUG, "-> REVOKE CERTIFICATE");
        } else if (msg instanceof SecGetWholeCRLRequest) {
            methodID = secGenericConfig.getReverseSecMethodIDMap().
                            indexOf(secGenericConfig.METHOD_GET_WHOLE_CRL);
            reqMethodID = ((SecGetWholeCRLRequest) msg).getMethodID();
            DebugOutput.println(DebugOutput.DBG_DEBUG, "-> GET WHOLE CRL");
        } else if (msg instanceof SecGetCRLRequest) {
            methodID = secGenericConfig.getReverseSecMethodIDMap().
                            indexOf(secGenericConfig.METHOD_GET_CRL);
            reqMethodID = ((SecGetCRLRequest) msg).getMethodID();
            DebugOutput.println(DebugOutput.DBG_DEBUG, "-> GET CRL (CLIENT)");
        } else if (msg instanceof SecSelfDestructRequest) {
            methodID = secGenericConfig.getReverseSecMethodIDMap().
                            indexOf(secGenericConfig.METHOD_SELF_DESTRUCT);
            reqMethodID = ((SecSelfDestructRequest) msg).getMethodID();
            DebugOutput.println(DebugOutput.DBG_DEBUG, "-> SELF DESTRUCT !!");
        } else if (msg instanceof SecGetClientsRequest) {
            methodID = secGenericConfig.getReverseSecMethodIDMap().
                            indexOf(secGenericConfig.METHOD_GET_CLIENTS_LIST);
            reqMethodID = ((SecGetClientsRequest) msg).getMethodID();
            DebugOutput.println(DebugOutput.DBG_DEBUG, "-> GET CLIENTS LIST");
        } else {
            DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "secGeneric: SEC VIOLATION: Unknown request");
            conn.closeConn(closeReason.secViolation);
            return;
        }

        // Don't need to compensate methodID for security stuff
        if (methodID != reqMethodID) {
            // CALL SECURITY !!!
            DebugOutput.println( DebugOutput.DBG_DEBUG,
                                "secGeneric: SEC VIOLATION: req.MID: "+
                                reqMethodID+" should be "+methodID);
            conn.closeConn(closeReason.secViolation);
            return;
        }

        if (plain) {
            // Check that we are allowed to execute the request
            if (!_myAC.reverseAllowed(methodID)) {
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "secGeneric: SEC VIOLATION: not allowed to "+
                                "execute that request");
                conn.closeConn(closeReason.secViolation);
                return;
            }
        } else {
            // Check our RACB and the other party's FACB
            if (super.forwardAllowed(channelID.intValue(), methodID) 
                == g.bool.False) {
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "secGeneric: SEC VIOLATION: not allowed to "+
                                "execute that request");
                // Check if this person is revoked
                AccessControl ac = (AccessControl) _channelToAC.get(channelID);
                if (ac.isRevoked()) {
                    // Send a error reply
                    handleRevokedChannel(conn, channelID, msg);
                }
                return;
            }
        }

        try {
            if (msg instanceof SecGetBlueprintsRequest) {
                // Handle that here
                handleGetBlueprints(conn, channelID,
                                    (SecGetBlueprintsRequest) msg);
            } else if (msg instanceof SecSelfDestructRequest) {
                // Handle that here
                handleSelfDestruct();
            } else if (msg instanceof SecGetCredentialsRequest) {
                _credsManager.handleGetCredentials(conn, channelID, 
                                               (SecGetCredentialsRequest) msg);
            } else if (msg instanceof SecUpdateCredentialsRequest) {
                _credsManager.handleUpdateCredentials(conn, channelID,
                                            (SecUpdateCredentialsRequest) msg);
            } else if (msg instanceof SecRevokeCertificateRequest) {
                _credsManager.handleRevokeCertificate(conn, channelID,
                                            (SecRevokeCertificateRequest) msg);
            } else if (msg instanceof SecGetWholeCRLRequest) {
                _credsManager.handleGetWholeCRL(conn, channelID,
                                                  (SecGetWholeCRLRequest) msg);
            } else if (msg instanceof SecGetCRLRequest) {
                _credsManager.handleGetCRL(conn, channelID,
                                                       (SecGetCRLRequest) msg);
            } else if (msg instanceof SecGetClientsRequest) {
                _credsManager.handleGetClientsList(conn, channelID,
                                                   (SecGetClientsRequest) msg);
            } 
        } catch (Exception e) {
            DebugOutput.println(DebugOutput.DBG_NORMAL,
                    "secGeneric: ERROR: Could not handle request");
        }

    }

    private void handleRevokedChannel(secConnection conn, Integer channelID,
                                      SecRequest msg) {

        // Send a SecErrorReply back.
        SecErrorReply eReply = new SecErrorReply(msg.getRpcId(),
                                            secGenericConfig.CREDS_REVOKED);

        // Marshall the message
        RawCursor cursor = new RawCursor(RawOps.createRaw());
        eReply.marshall(cursor);

        // and send it on its way
        try {
            conn.send(cursor.getRaw(), SecConnection.SEC_OBJ);
        } catch (Exception e) {
            DebugOutput.println(DebugOutput.DBG_NORMAL,
                "secGeneric: getBP: ERROR: Could not send reply");
        }

        // DO NOT Close the connection otherwise the connection close event
        // could be triggered BEFORE the handling of the sec error reply.
    }

    private void handleReply(SecReply reply) {

        if (reply instanceof SecErrorReply) {
            SecErrorReply eReply = (SecErrorReply) reply;
         
            // Wake up thread and tell about the error
            _rpcTable.passSecError(eReply.getRpcId(), eReply.getErrorArg());
        } else {
            // Wake up thread and tell about the reply
            _rpcTable.passSecReply(reply);
        }
    }

    private void handleGetBlueprints(secConnection conn, Integer channelID,
                            SecGetBlueprintsRequest msg) throws secErrors {

        // Four parts:
        // 1. object's public key
        // 2. number of signed blueprints
        // 3. signed blueprints
        // 4. Secure network address

        // Create the secMessage
        rawDef r = RawOps.createRaw();
        RawCursor c = new RawCursor(r);

        if (_securityEnabled) {
            // Use regular creds always (is that correct ?)
            if (_issuerCredsKS == null) {
                try {
                    _issuerCredsKS =
                        RightsCertUtil.idlObjectCredsToKeyStore(_cfg.creds);

                    // Object public key
                    _myKeyChain = 
                           RightsCertUtil.getChainFromKeyStore(_issuerCredsKS);
                } catch (Exception e) {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                      "secGeneric: getBP: ERROR: could not get keystore"+
                      " or chain");
                    throw new secErrors();
                }
            }

            DebugOutput.println(DebugOutput.DBG_DEBUG,
                "secGeneric: getBP: my Chain has length " + _myKeyChain.length);
            byte[] objPubBytes = _myKeyChain[_myKeyChain.length-1].
                                                getPublicKey().getEncoded();
            RawBasic.writeOctetString(c, objPubBytes, 0, objPubBytes.length);
        }

        // (UN) Signed blueprints, when security is disabled the UNsigned
        // blueprints are send as bytes inside the signed blueprints IDL.
        DebugOutput.println(DebugOutput.DBG_DEBUG,
                "secGeneric: getBP: _cfg.otherbps.length is "+
                _cfg.otherbps.v.length);

        RawBasic.writeInt32(c, _cfg.otherbps.v.length);
        for (int i=0; i < _cfg.otherbps.v.length; i++) {
            RawBasic.writeOctetString(c, _cfg.otherbps.v[i].v, 0,
                                      _cfg.otherbps.v[i].v.length);
        }
        
        // Contact addresses
        int n = _lsAddrs.v.length;
        RawBasic.writeInt32(c, n);

        for (int i = 0; i < n; i++) {
            rawDef caddrBytes;
            g.opaque caddr = _lsAddrs.v[i];

            try {
                caddrBytes = _idManager.marshallContactAddress(caddr);
            } catch (Exception exc) {
                DebugOutput.printException(DebugOutput.DBG_DEBUG, exc);
                throw new secErrors();   // FIXME which type ? _invOp ?
            }
            byte[] caddrRealBytes = RawOps.getRaw(caddrBytes);
            RawBasic.writeOctetString(c, caddrRealBytes, 
                                      0, caddrRealBytes.length);
        }

        SecMessage reply = new SecGetBlueprintsReply(msg.getRpcId(), r);

        // Marshall the message
        RawCursor cursor = new RawCursor(RawOps.createRaw());
        reply.marshall(cursor);
        
        // and send it on its way
        try {
            conn.send(cursor.getRaw(), SecConnection.SEC_OBJ);
        } catch (Exception e) {
            DebugOutput.println(DebugOutput.DBG_NORMAL,
                "secGeneric: getBP: ERROR: Could not send reply");
            conn.closeConn(closeReason.errorOccured);
            throw new secErrors();
        }
    }


    public void handleSelfDestruct() {

        if (_objSvrSOI == null) {
            try {
                // Use nsConst here for the correct path.
                // During first run of the object server all LRs are
                // in /ObjectServer/OSManger/LRx/ ... etc.
                // However after restarting the Object server the LR are in a
                // different namespace  :/runtime/perstMgr/... etc.
                // So just using getContext() and "OSManager" won't work...
                _objSvrSOI = nameSpaceImp.getLNS().bind(getContext(),
                                                        nsConst.OS_MGR_NAME);
                _objSvrOps = (objectServerOps) _objSvrSOI.getUncountedInf(
                                                        objectServerOps.infid);
            } catch (Exception e) {
                // The object server was probably not loaded yet. The
                // registerSlave request will receive an error reply such that
                // activation will fail anyway.
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                    "SecGeneric: ERROR: Could not get object server inf: "
                                +e.getMessage());
                return;
            }
        }

        try {
            // _lrid is set from the LR manager.
            _objSvrOps.removeLRLater(_lrid, 1000); // remove in 1 second
        } catch (Exception e) {
            DebugOutput.println(DebugOutput.DBG_NORMAL,
                    "secGeneric: ERROR: Could not SELF DESTRUCT !!: "+
                    e.getMessage());
        }
    }
    
    public void receiveStopped(int channel, g.opaque user) {
        // Nothing to do here, handled by connClosed.
    }


    /************** Overriding method from BASE lrSubObject inf *************/
    public void completeActivation() throws subObjectErrors {
   
        // Set the lsAddrs:
        if (_cfg.lsAddrs != null) {
            _lsAddrs = _cfg.lsAddrs;
        } else {
            DebugOutput.println(DebugOutput.DBG_NORMAL,
                    "secGeneric: reactivated but NO LS Addresses ?");
        }
            
        // Override this method to activate the TimerTask of the Credentials
        // Manager. The Credentials Manager uses the unmarshalled CRL to find
        // out the timer interval and refreshes/gets the new CRL if needed.
        if (_securityEnabled) {
            try {
                _credsManager.activate();
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "secGeneric: ERROR: can't activate creds "+
                                "manager");
                throw new subObjectErrors();
            }
        }
    }

    

    /************** Implementation of abstract methods ***********************/
    // Allocates the contact points and puts them in the LS
    protected contactAddresses allocateResources() throws secErrors {
   
        // Activate the CredentialsManager also.
        if (_securityEnabled) {
            try {
                _credsManager.activate();
            } catch (secErrors_credsRevoked e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "secGeneric: ERROR while activating "+
                                "_credsManager: CREDS REVOKED, trying to exit");
                throw e;
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                                    "secGeneric: ERROR while activating "+
                                    "_credsManager... trying to exit");
                throw new secErrors_secViolation();
            }
        }
            
        // If you don't have any listeners you don't have to allocate resources
        if (!_hasListeners) {
            return null;
        }

        String protocol = P2PDefs.TCP_MUX_SEC_STACK;

        // allocate a contact point from the resource manager
        contactManager cMgr = super.getContactManager();

        // Create the contact info sequence FIXME, currently NO
        // AUTHENTICATING_RIGHTS_CP
        contactAddressInfoSeq seq = new contactAddressInfoSeq(NR_CPS); 

        // Allocate the contact points
        for (int i=0; i < NR_CPS; i++) {
            try {
                _cpRid[i] = cMgr.allocateContact(protocol);

                if (super.isPersistent()) {
                    super.getPerstResourceManager().makePersistent(
                                                                super.getPID(),
                                                                _cpRid[i]);
                }
                ProtAddress p;
                try {
                    p = new ProtAddress(cMgr.getAddress(_cpRid[i]));
                } catch (Exception e) {
                    throw new secErrors_invOp();
                }

                // Append our secType
                if (i == AUTHENTICATING_RIGHTS_CP) {
                    p.add(SecAddress.PROT_ID,
                          ProtAddress.AUTHENTICATING_RIGHTS_PROT);
                } else if (i == SECURE_RIGHTS_CP) {
                    p.add(SecAddress.PROT_ID, ProtAddress.SECURE_RIGHTS_PROT);
                } else if (i == INSECURE_CP) {
                    p.add(SecAddress.PROT_ID, ProtAddress.INSECURE_PROT);
                }
                _contactPoint[i] = p.toString();
		DebugOutput.println(DebugOutput.DBG_DEBUG,
                   "secGeneric: ***** contact point "+i+" = "+_contactPoint[i]);
            } catch (Exception e) {
                throw new AssertionFailedException();
            }

            seq.v[i] = new contactAddressInfo();

            // Set the property map
            propertyMapDef props = null;

            AccessCtrlBitmap racb = null;
            try {
                if (_securityEnabled) {
                    IDLRightsCert my_idlcert = _cfg.creds.chain.v[0];
                    Certificate my_cert =
                                RightsCertUtil.idlRightsCertToJava(my_idlcert);
                    racb = RightsCertUtil.getRACBFromCertificate(my_cert);
                } else {
                    // Use the required bitmaps of the blueprint
                    racb = new AccessCtrlBitmap(_cfg.tocreatebp.requiredRACB.v);
                }
                racb.clear(secGenericConfig.getReverseSecMethodIDMap().
                            indexOf(secGenericConfig.METHOD_GET_BLUEPRINTS));
                racb.clear(secGenericConfig.getReverseSecMethodIDMap().
                            indexOf(secGenericConfig.METHOD_GET_CREDENTIALS));
            } catch (vu.gaia.rts.RTSException e) {
                throw new AssertionFailedException();
            }

            if (i == AUTHENTICATING_RIGHTS_CP) {
                // FIXME FIXME FIXME
                // Reverse access ctrl bitmap of repl AND AUTH_RIGHTS_MASK
                props = PropertyMapOps.bitSetToPropertyMap(racb.toBitSet());
            } else if (i == SECURE_RIGHTS_CP) {
                // FIXME FIXME FIXME
                // Reverse access ctrl bitmap of repl AND SECURE_RIGHTS MASK
                props = PropertyMapOps.bitSetToPropertyMap(racb.toBitSet());
            } else if (i == INSECURE_CP) {
                // FIXME QUICK HACK HERE TILL WE HAVE MASKS
                if (_hasListeners && _hasConnector) {
                    // Sergeant
                    props = PropertyMapOps.bitSetToPropertyMap(
                              PropertyMapOps.createGetBlueprintsPropertyMap());
                } else {
                    // Chief
                    props = PropertyMapOps.bitSetToPropertyMap(
                              PropertyMapOps.createGetAllPropertyMap());
                }
            }

            // Address ID. Achieve uniqueness by basing it on the network
            // address and properties
            byte[] netBytes = _contactPoint[i].getBytes();
            byte[] propBytes = props.v;

            // Copy the bytes into the address ID
            addressID addrID = new addressID(netBytes.length+propBytes.length);
            System.arraycopy(netBytes, 0, addrID.v, 0, netBytes.length);
            System.arraycopy(propBytes, 0, addrID.v, netBytes.length,
                             propBytes.length);

            // typed address
            stackedAddress stacked_address = new stackedAddress(1);

            unknownComponentDef ucDef = new unknownComponentDef();
            ucDef.prot = "unknown";
            byte[] addrBytes = _contactPoint[i].getBytes();
            rawDef rawAddress = RawOps.createRaw();
            RawOps.setRaw(rawAddress, addrBytes, 0, addrBytes.length);
            ucDef.address = new unknownAddress_bytes(rawAddress);

            compVariant cVU = new compVariant_unknown(ucDef);

            stackedComponentDef scDef = new stackedComponentDef();
            scDef.var = cVU;
            if (i == AUTHENTICATING_RIGHTS_CP) {
                scDef.impl = P2PDefs.AUTHENTICATING_RIGHTS;
            } else if (i == SECURE_RIGHTS_CP) {
                scDef.impl = P2PDefs.SECURE_RIGHTS;
            } else if (i == INSECURE_CP) {
                scDef.impl = "BindingNegotiator";
            }

            stacked_address.v[0] = scDef;
            typedAddressDef_stacked typed_addr =
                                new typedAddressDef_stacked(stacked_address);

            // address ID + properties + stacked address
            seq.v[i].aid = addrID;
            seq.v[i].props = props;
            seq.v[i].address = typed_addr;
        }
        
        // Create ls address
        _lsAddrs = new contactAddresses(seq.v.length);
        for (int i = 0; i < seq.v.length; i++) {
            try {
                _lsAddrs.v[i] = _idManager.createContactAddressFromCAI(seq.v[i]);
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "secGeneric: ERROR: Could not create LS addresses");
                throw new RuntimeException("Could not create LS Addresses");
            }
        } 

        return _lsAddrs;
    }

    protected void deallocateResources(boolean quick) {

        if (!_hasListeners) {
            // Nothing to do
            return;
        }

        // Deallocate the contact points
        for (int i=0; i < NR_CPS; i++) {
            try {
                if (_cpRid[i] != resource.invalidResourceID) {
                    getContactManager().deallocateContact(_cpRid[i]);
                }
            } catch (Exception e) {
                DebugOutput.printException(DebugOutput.DBG_NORMAL, e);
            }
        }

        if (_credsManager != null) {
            _credsManager.stop();
        }
        _credsManager = null;
    }

    protected void startListener() throws secErrors {

        if (!_hasListeners) {
            // Nothing to do
            return;
        }
        
        // Start all listeners
        for (int i=0; i < NR_CPS; i++) {
            // Get the listener, contactExport and comm interfaces set up.
            try {
                // setup the listener etc. interfaces
                installListener(i);

                // export our contact and start listening
                _contactExporter[i].exportContact(_contactPoint[i]);

                // Set up the listener identifier
                g.opaque_pointer user = new g.opaque_pointer();
                user.pointer = new Integer(i);
               
                // install ourselves as callback for the listener
                listenCB myListenCB = (listenCB) super.getCBInf(listenCB.infid);
                _listener[i].regListenCB(myListenCB, user);
            
            } catch (Exception e) {
                if (_listener[i] != null) {
                    _listener[i].relInf();
                    _listener[i] = null;
                }
                // Without the listener the Sergeant is not of much use
                throw new AssertionFailedException();
            }
        }

        // With all listeners ready, store them in the LS if we are not
        // recovering.
        if (_lsAddrs != null) {
            for (int i=0;  i < NR_CPS; i++) {
                try {
                    // register the contact address
                    _resolverOps.insert(super._objectHandle, _lsAddrs.v[i],
                                        null);
                } catch (Exception e) {
		    DebugOutput.println(DebugOutput.DBG_NORMAL,
                              "secGeneric: ERROR could not insert in the LS!");
                    throw new RuntimeException("Could not register listeners");
                }
            }
        }
    }

    protected void checkForRevokedConnections() {

        // If security has been disabled, skip this
        if (!_securityEnabled) {
            return;
        }

        // Prepare the SELF_DESTRUCT MESSAGE
        int selfDestructMID = secGenericConfig.getReverseSecMethodIDMap().
                                indexOf(secGenericConfig.METHOD_SELF_DESTRUCT);

        SecMessage selfDestruct = new SecSelfDestructRequest(selfDestructMID);

        // Marshall the message
        RawCursor cursor = new RawCursor(RawOps.createRaw());
        selfDestruct.marshall(cursor);
            
        // Go over each channel's certificate and check if it is revoked
        Set s = _channelToAC.keySet();

        // Synchronize ! See
        // http://java.sun.com/j2se/1.4/docs/api/java/util/Collections.html#synchronizedMap(java.util.Map)
        synchronized (_channelToAC) {
            Iterator it = s.iterator();
            while (it.hasNext()) {
                Integer channelID = (Integer) it.next();
                AccessControl ac = (AccessControl) _channelToAC.get(channelID);
                try {
                    // Now check if this connection's certificate has not been
                    // revoked
                    int rev = _credsManager.isRevoked(ac.getCertificateChain());
                    if (rev != secGenericConfig.NOT_REVOKED) {
                        secConnection conn = (secConnection) 
                                        _channelToConnection.get(channelID);
                        if (rev == secGenericConfig.REVOKED_CLIENT) {
                            DebugOutput.println(DebugOutput.DBG_DEBUG,
                                                "secGeneric: FOUND "+
                                                "REVOKED CLIENT");

                            // Make sure it is not allowed to do anything
                            ac.setCertificateRevoked();
                            
                            // NOTE, DON'T CLOSE THE CONNECTION OTHERWISE
                            // WE CAN'T TELL IT THAT ITS CREDS WERE REVOKED
                            // AT THE CLIENT'S NEXT REQUEST!
                            
                        } else {
                            DebugOutput.println(DebugOutput.DBG_DEBUG,
                                                "secGeneric: FOUND "+
                                                "REVOKED REPLICA");
                            // Tell the Sergeant to self destruct
                            if (reverseAllowed(channelID.intValue(), 
                                           selfDestructMID) == g.bool.True) {
                              conn.send(cursor.getRaw(),SecConnection.SEC_OBJ);
                            }

                            // If we close the connection now, the Self-Destruct
                            // could be lost.
                        }
                    }
                } catch (Exception e) {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "secGeneric: ERROR while closing revoked connection !");
                    e.printStackTrace();
                    // Continue removing others
                }
            }
        }
    }

    
    protected void closeConnections(boolean quick) {

        // Go over all the listeners and connections and close em
        // Wait patiently till the listeners have reported that they are done
        // and the last connection to close will notify us for the connections
        // lists.
  
        // WE NEED EVERYTHING TO BE UNPAUSED OTHERWISE WE WON'T RECEIVE
        // THE MESSAGE THAT THE CONN WAS CLOSED.
        // Acquire the lock on the pause routine.
        synchronized(_pauseLock) {
            if (_paused) {
                _noCRLMode = false;
                try {
                    pause(false);
                } catch (Exception e) {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                     "secGeneric: Could not unpause during close connection!");
                }
            }

            if (_hasListeners) {
                // Close listeners first
                for (int i=0; i < NR_CPS; i++) {
                    if (!_closed[i]) {
                        _contactExporter[i].closeContact();
                    }

                    // Wait for the listener to stop
                    if (!_closed[i]) {
                        synchronized(this) {
                            while (!_closed[i]) {
                                try {
                                    this.wait();
                                } catch (InterruptedException e) {
                                    // keep waiting
                                }
                            }
                        }
                    }
                }

                // FIXME should we remove them from the GLS here too
                // This is currently done in the LRMgr...

                // Close INSECURE connections
                Set s = _plainChannelToConnection.keySet();
                synchronized(_plainChannelToConnection) {
                    Iterator it = s.iterator();

                    while (it.hasNext()) {
                        Integer channelID = (Integer) it.next();
                        secConnection conn = (secConnection)
                                    _plainChannelToConnection.get(channelID);
                        conn.closeConn(closeReason.closeComm);
                    }
                }
            }
        
            // Close the normal connections
            Set s = _channelToConnection.keySet();
            synchronized(_channelToConnection) {
                Iterator it = s.iterator();

                while (it.hasNext()) {
                    Integer channelID = (Integer) it.next();
                    secConnection conn = (secConnection) 
                                        _channelToConnection.get(channelID);
                    conn.closeConn(closeReason.closeComm);
                }
            }

            // indicate that connections should from now on notify us if they
            // are the last ones that closed. We are only notified if both 
            // lists (if applicable) are empty.
            _closing = true;

            if (!_channelToConnection.isEmpty()) {
                synchronized(this) {
                    while (!_channelToConnection.isEmpty()) {
                        try {
                            this.wait();
                        } catch(InterruptedException e) {
                            // Ignore
                        }
                    }
                }
            }
        
            if (_hasListeners && !_plainChannelToConnection.isEmpty()) {
                synchronized(this) {
                    while (!_plainChannelToConnection.isEmpty()) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            // Ignore
                        }
                    }
                }
            }
        }
    }
    

    protected void pause(boolean on) throws subObjectErrors {

        // We make a copy of the table because of the following scenario:
        // A thread comes in with new connection, then we
        // passivate and acquire lock. New threads want to add itself to the
        // table, which it can't because synchronized. --> Deadlock.

        // NEW We need to synchronize around the pauseLock. We can have the
        // following situations:
        // _noCRLMode = false (normal situation)
        // - pause(on) allowed
        // - pause(off) allowed
        //
        // _noCRLMode = true (when Chief is gone)
        // - pause(on) allowed
        // - pause(off) return immediately
        //
        // the _noCRLMode is set/unset by the getCRL method, who will acquire
        // the lock there.
        //
        // Also catch multiple pause(on) so we only do it once.

        synchronized(_pauseLock) {

            if (_noCRLMode && !on) {
                // no CRL, don't allow pause off
                return;
            }

            if (_paused && on) {
                // Already paused, no need to do it again.
                return;
            }

            if (!_paused && !on) {
                // Already unpaused, no need to do it again.
                return;
            }

            // The security subobject will go over all the connections
            // and pause them. First pause the listener.

            if (_hasListeners) {
                // Pause the listeners
                for (int i=0; i < NR_CPS; i++) {
                    try {
                        _listener[i].pauseListenCB(on ? g.bool.True : 
                                                        g.bool.False);
                    } catch (Exception e){
                        throw new subObjectErrors_invOp();
                    }
                }
        
                // Pause the connections on the INSECURE cp
                Set s = _plainChannelToConnection.keySet();

                if (!s.isEmpty()) {
                    secConnection[] toPause = new secConnection[s.size()];

                    synchronized(_plainChannelToConnection) {
                        Iterator it = s.iterator();
                        int i=0;

                        while (it.hasNext()) {
                            Integer channelID = (Integer) it.next();
                            toPause[i] = (secConnection) 
                                    _plainChannelToConnection.get(channelID);
                            i++;
                        }
                    }

                    for (int i=0; i < toPause.length; i++) {
                        if (toPause[i] == null) {
                            continue;
                        }
                    
                        try {
                            toPause[i].pauseConn(on ? g.bool.True :
                                                      g.bool.False);
                        } catch (Exception e) {
                            throw new subObjectErrors_invOp();
                        }
                    }
                }
            }

            
            // Go over all the channels
            Set s = _channelToConnection.keySet();

            if (!s.isEmpty()) {
                secConnection[] toPause = new secConnection[s.size()];
        
                synchronized(_channelToConnection) {
                    Iterator it = s.iterator();
                    int i=0;

                    while (it.hasNext()) {
                        Integer channelID = (Integer) it.next();
                        toPause[i] = (secConnection)_channelToConnection.get(
                                                                    channelID);
                        i++;
                    }
                }

                for (int i=0; i < toPause.length; i++) {            
                    if (toPause[i] == null) {
                        continue;
                    }
            
                    try {
                        toPause[i].pauseConn(on ? g.bool.True : g.bool.False);
                    } catch (Exception e) {
                        throw new subObjectErrors_invOp();
                    }
                }
            }

            // In both lists: when a new connection arrives, it won't be
            // paused. The replica can only send requests and incoming requests
            // are paused already.
            if (on) {
                _paused = true;
            } else {
                _paused = false;
            }
        }
    }

    // Store our contact point for persistency
    protected void marshallState(RawCursor state) throws ProtocolException {

        // Store our CRL and credentials (if any)
        if (_securityEnabled) {
            _credsManager.marshallState(state);
        }

        if (!_hasListeners) {
            return;
        }
        
        // Write our contact point storage IDs
        for (int i=0; i < NR_CPS; i++) {
            RawBasic.writeInt64(state, _cpRid[i]);
        }
    }

    // Retrieve our contact point from persistent storage
    protected void unmarshallState(RawCursor state) throws ProtocolException {

        // Retrieve our CRL and credentials (if any);
        if (_securityEnabled) {
            _credsManager.unmarshallState(state);
        }

        if (!_hasListeners) {
            return;
        }
        
        // Read in our contact point resource IDs
        for (int i=0; i < NR_CPS; i++) {
            _cpRid[i] = RawBasic.readInt64(state);

            // Retrieve our contact address
            contactManager cMgr = super.getContactManager();
            try {
               ProtAddress p;
                p = new ProtAddress(cMgr.getAddress(_cpRid[i]));

                // Append our secType
                if (i == AUTHENTICATING_RIGHTS_CP) {
                    p.add(SecAddress.PROT_ID, P2PDefs.AUTHENTICATING_RIGHTS);
                } else if (i == SECURE_RIGHTS_CP) {
                    p.add(SecAddress.PROT_ID, P2PDefs.SECURE_RIGHTS);
                } else if (i == INSECURE_CP) {
                    p.add(SecAddress.PROT_ID, P2PDefs.INSECURE);
                }
                _contactPoint[i] = p.toString();
            } catch (Exception e) {
                throw new ProtocolException(e.getMessage());
            }
        }
    }
    
    
    private void installListener(int i) throws Exception {

        // Create the listener object
        SOInf listCOSoi;

        try {
            listCOSoi = lns.bind(getContext(), "repository/" +
                                               P2PDefs.LIGHT_TCP_SEC_LISR_IMPL);
        } catch (Exception e){
            DebugOutput.printException(DebugOutput.DBG_NORMAL, e);
            throw new RuntimeException(e.getMessage());
        }

        SCInf listCOSci = null;
        SOInf listSoi = null;
        configurable cfg = null;
        try {
            listCOSci = (SCInf) listCOSoi.swapInf(SCInf.infid);
            listSoi = StdUtil.createGlobeObject(listCOSci, getContext(),
                                                LISR_LNAME+i);
            // Get the configurable interface
            cfg = (configurable) listSoi.getUncountedInf(configurable.infid);

            // Init the object
            if (i == SECURE_RIGHTS_CP) {
                cfg.configure(P2PDefs.LIGHT_TCP_SEC_SECURE_RIGHTS_LISR_INIT);
            } else if (i == AUTHENTICATING_RIGHTS_CP) {
                cfg.configure(
                       P2PDefs.LIGHT_TCP_SEC_AUTHENTICATING_RIGHTS_LISR_INIT);
            } else if (i == INSECURE_CP) {
                cfg.configure(P2PDefs.LIGHT_TCP_SEC_INSECURE_LISR_INIT);
            }

            // Get the listener interface (counted)
            _listener[i] = (listener) listSoi.swapInf(listener.infid);

            // Get the contactExporter interface (uncounted)
            _contactExporter[i] = (contactExporter) listSoi.getUncountedInf(
                                                        contactExporter.infid);

            // Get the comm interface (uncounted)
            _listenerComm[i] = (comm) listSoi.getUncountedInf(comm.infid);
        } catch (Exception e) {
            if (listCOSci == null) {
                // Error at first swapInf
                listCOSoi.relInf();
            } else if (listSoi == null) {
                // Error at createGlobeObject
                listCOSci.relInf();
            } else if (cfg == null && _listener == null) {
                // Error at getUncounted, configure or swapInf
                listSoi.relInf();
            } else if (_contactExporter[i] == null) {
                _listener[i].relInf();
                _listener[i] = null;
            }   
            DebugOutput.printException(DebugOutput.DBG_NORMAL, e);
            throw new RuntimeException(e.getMessage());
        }
    }

  
    private contactAddresses lsLookup(int method) 
    {
        // Prepare here everything for the query and return the contact
        // addresses that match this query.
        contactAddresses cAddrs = null;
        queryDef query = null;

        DebugOutput.println(DebugOutput.DBG_DEBUG,
                            "secGeneric: using GLS to find contact address");
      
        // Prepare the selector
        // "0,!1,2|!2"
        // (prop 0 and not prop 1 and prop 2) or not prop 2
        String select = ""+method;
        
        propertySelector selector =
                        PropertySelectorOps.stringToPropertySelector(select);

        // prepare the query
        query = new queryDef();
        query.ohandle = _objectHandle;
        query.selector = selector;

        try 
	{
            // perform the Location Service lookup
            cAddrs = _resolverOps.lookup(query, LS_MIN, LS_MAX);
        } 
        catch (Exception e) 
	{
            // Bummer...
	    DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "secGeneric: Error while looking up !!"+e.getMessage());
        }

        return cAddrs;
    }


    /** 
     * Arno: optimization / support binding to specific contact address
     */
    private contactAddresses lookatPreinstalledCaddrs( int method )
    {
        contactAddresses cAddrs = null;
        if (_cfg.caddrs == null)
            return cAddrs;
        
        // Prepare the selector
        // "0,!1,2|!2"
        // (prop 0 and not prop 1 and prop 2) or not prop 2
        String select = ""+method;
        
        propertySelector selector =
                        PropertySelectorOps.stringToPropertySelector(select);
        
        DebugOutput.println(DebugOutput.DBG_DEBUG, "secGeneric: Considering "+
                            _cfg.caddrs.v.length + 
                            " preinstalled contact addresses for method " 
                            + method );
        try
        {
            PropertySelector jPropSel = IdlTypesTools.convertPropertySelector(
                                                                     selector);
            for (int i=0; i < _cfg.caddrs.v.length; i++)
            {
                contactAddressInfo cinfo = _idManager.getContactAddressInfo(
                                                             _cfg.caddrs.v[i]);
                PropertyMap pmap = IdlTypesTools.convertPropertyMap(
                                                                  cinfo.props);
                
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                                    "secGeneric: Considering addr with props "
                                    + pmap.getBitSet().toString());
                
                if (jPropSel.test(pmap))
                {
                    DebugOutput.println(DebugOutput.DBG_DEBUG,
                         "secGeneric: Selecting preinstalled contact address");
                    cAddrs = new contactAddresses( 1 );
                    cAddrs.v[0] = _cfg.caddrs.v[i];
                }
            }
        }
        catch( Exception e )
        {
            DebugOutput.println( DebugOutput.DBG_NORMAL, 
               "secGeneric: *** ERROR: checking preinstalled contact address");
        }
        return cAddrs;
    }


    /********* Implementation of the secCertManagement interface *************/
    public IDLClientsList getClientsList() throws secErrors {
        // Forward call to CredsManager
        if (_securityEnabled) {
            return _credsManager.getClientsList();
        } else {
            return null;
        }
    }

    public void updateCredentials(IDLSubjectName userName,
                                  IDLPubKeyHash userKeyHash,
                                  IDLAccessCtrlBitmap forwardACB,
                                  short /* credsOpMode */ mode)
        throws secErrors {
        // Forward call to CredsManager
        if (_securityEnabled) {
            _credsManager.updateCredentials(userName, userKeyHash, 
                                            forwardACB, mode);
        }
    }

    public void revokeCertificate(long certSerialNum,
                                  short /* g.bool */ clientCert)
        throws secErrors {
        // Forward call to CredsManager
        if (_securityEnabled) {
            _credsManager.revokeCertificate(certSerialNum, clientCert);
        }
    }


    /******** Implementation of the timerResourceManagerCB interface *********/
    public void run(long timer, g.opaque user) throws Exception {
        // Forward the call to the CredsManager
        if (_securityEnabled) {
            _credsManager.run(timer, user);
        }
    }
    
    public void cancel(long timer, g.opaque user) throws Exception {
        // Forward the call to the CredsManager
        if (_securityEnabled) {
            _credsManager.cancel(timer, user);
        }
    }


    /********************* Load Keystore and stuff ****************************/
    public void setKeyMaterials() throws secErrors {

        try {
            _issuerAdminKS =RightsCertUtil.idlObjectCredsToKeyStore(
                                                               _cfg.admincreds);
            _privKey = RightsCertUtil.getKeyPairFromKeyStore(
                                                   _issuerAdminKS).getPrivate();
        } catch (Exception e) {
            DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "CredsCRL: ERROR: Could not load Admin KS");
            throw new secErrors();
        }
    }


    
    /*********************** Credentials Manager *****************************/
    /** This is an internal class that handles everything related to the
     * credentials and revocation stuff. It registers a timer callback and 
     * does periodically getCRL() or periodically creates a new CRL. 
     * This object is synchronized internally (i.e. it is thread safe).
     */
    private class CredsManager {

        // The maintainer of the credentials and the CRL.
        private CredsCRL _credsCRL;
        
        // Timer ID
        private long _timerID = resource.invalidTimerID;
            
        // If we should stop (not sure if needed, could call exit() instead).
        private boolean _stopped = false;
        private boolean _activated = false;
        
        // The timerResourceManager
        private timerResourceManager   _tRscMgr = null;
        private timerResourceManagerCB _tRscCB = null;
        
        /** The init string tells us if we are in 'pulling mode' or if we
         * are the ones that have to create new CRLs and stuff.
         */
        public CredsManager(timerResourceManager tRscMgr,
                            timerResourceManagerCB myTimerRscCB) {

            _tRscMgr = tRscMgr;
            _tRscCB = myTimerRscCB;
            _credsCRL = new CredsCRL();
        }

        // This method schedules timer and needs to be a fast returner,
        // otherwise all otherthings are blocked on our message stuff.
        // Let it wait 1000 before actually fetching the CRL, then we will set
        // the real timer.
        public synchronized void activate() throws Exception {

            if (_activated) {
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "CredsManager: ERROR: Timer was already activated !");
                return;
            }

            _activated = true;
             
            // Allow everyone to get a new CRL or in Chief case set the
            // interval time only
            if (_credsCRL.isOld()) {
                getNewCRL();
            }
            
            // If we need a timer, register the callback here and store the
            // return value for future ref
            if (_refreshCRL) {
                // register our callback
                _timerID = _tRscMgr.schedule(_tRscCB, null,
                                            _credsCRL.getIntervalTime(),
                                            _credsCRL.getFirstIntervalTime());
            }
        }

        public synchronized void stop() {
            try {
                if (_refreshCRL && _timerID > 0) {
                    DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "CredsManager: Cancelling our timer");
                    _tRscMgr.cancel(_timerID);
                }
            } catch (Exception e) {
                // What to do ???
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "CredsManager: ERROR: Could not cancel timer");
            }
        }


        /** When our timer has gone off, we are called here */
        public synchronized void run(long timer, g.opaque user) 
                throws Exception {

            if (_getCRL) {
                getNewCRL();
            } else if (_refreshCRL) {
                refreshCRL();
            } else {
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                                    "CredsManager: RUN: do NOTHING");
            }
        }

        
        /** When the timerResourceManager wants to stop: */
        public synchronized void cancel(long timer, g.opaque user)
                throws Exception {
            DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "CredsManager: Timer is cancelled");
        }


        /** Handle getCredentials request */
        public synchronized void handleGetCredentials(secConnection conn,
                            Integer channelID, SecGetCredentialsRequest msg)
                throws secErrors {

            rawDef r;

            DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "CredsManager: Handle GET Credentials");

            try {
                r = _credsCRL.getCredentials(msg.getCredArg());
            } catch (Exception e) {
                conn.closeConn(closeReason.errorOccured);
                throw new secErrors();
            }
                
            SecMessage reply = new SecGetCredentialsReply(msg.getRpcId(), r);

            try {
                // and send it on its way
                sendMessage(conn, reply);
            } catch (Exception e) {
	            DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "CredsManager: ERROR sending getCreds stuff");
                conn.closeConn(closeReason.errorOccured);
                throw new secErrors();
            }   
        }

        /** Handle updateCredentials request */
        public synchronized void handleUpdateCredentials(secConnection conn,
                        Integer channelID, SecUpdateCredentialsRequest msg)
                throws secErrors {

            rawDef r;

            DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "CredsManager: Handle Update Creds");

            try {
                r = _credsCRL.updateCredentials(msg.getCredArg());
            } catch (Exception e) {
                conn.closeConn(closeReason.errorOccured);
                throw new secErrors();
            }

            // And send the reply
            SecMessage reply = new SecUpdateCredentialsReply(msg.getRpcId(),r);

            try {
                sendMessage(conn, reply);
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "CredsManager: ERROR: Could not send reply on channel "+
                        channelID);
                conn.closeConn(closeReason.errorOccured);
                throw new secErrors();
            }
        }

        /** Handle revokeCertificate request */
        public synchronized void handleRevokeCertificate(secConnection conn,
                        Integer channelID, SecRevokeCertificateRequest msg) 
                throws secErrors {

            rawDef r;

            DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "CredsManager: Handle Revoke Certificate");
            try {
                r = _credsCRL.revokeCertificate(msg.getCertArg());
            } catch (Exception e) {
                conn.closeConn(closeReason.errorOccured);
                throw new secErrors();
            }     
                
            // And send the reply
            SecMessage reply = new SecRevokeCertificateReply(msg.getRpcId(),r);

            try {
                sendMessage(conn, reply);
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "CredsManager: ERROR: Could not send reply on channel "+
                        channelID);
                conn.closeConn(closeReason.errorOccured);
                throw new secErrors();
            }
        }
        
        /** Handle getWholeCRL request */
        public synchronized void handleGetWholeCRL(secConnection conn,
                        Integer channelID, SecGetWholeCRLRequest msg)
                throws secErrors {

            rawDef r;
            
            DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "CredsManager: Handle Get WHOLE CRL");
           
            try {
                r = _credsCRL.getWholeCRL();
            } catch (Exception e) {
                conn.closeConn(closeReason.errorOccured);
                throw new secErrors();
            }
            
            // And send the reply 
            SecMessage reply = new SecGetWholeCRLReply(msg.getRpcId(), r);

            try {
                sendMessage(conn, reply);
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "CredsManager: ERROR: Could not send reply on channel "+
                        channelID);
                conn.closeConn(closeReason.errorOccured);
                throw new secErrors();
            }
        }

        /** Handle getCRL request */
        public synchronized void handleGetCRL(secConnection conn,
                        Integer channelID, SecGetCRLRequest msg) 
        throws secErrors {

            rawDef r;
            
            DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "CredsManager: Handle GET CRL");

            try {
                r = _credsCRL.getCRL();
            } catch (Exception e) {
                conn.closeConn(closeReason.errorOccured);
                throw new secErrors();
            }

            // And send the reply
            SecMessage reply = new SecGetCRLReply(msg.getRpcId(), r);

            try {
                sendMessage(conn, reply);
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "CredsManager: ERROR: Could not send reply on channel "+
                        channelID);
                conn.closeConn(closeReason.errorOccured);
                throw new secErrors();
            }
        }

        /** Handle get clients list request */
        public synchronized void handleGetClientsList(secConnection conn,
                                Integer channelID, SecGetClientsRequest msg)
        throws secErrors {
                
            rawDef r;
            try {
                r = _credsCRL.getClientsList();
            } catch (Exception e) {
                // FIXME shoudld send SecError (also above)
                conn.closeConn(closeReason.errorOccured);
                throw new secErrors();
            }

            // And send the reply
            SecMessage reply = new SecGetClientsReply(msg.getRpcId(), r);

            try {
                sendMessage(conn, reply);
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "CredsManager: ERROR: could not send reply on channel "+
                        channelID);
                conn.closeConn(closeReason.errorOccured);
                throw new secErrors();
            }
        }
       
        /** Method called by a client via the Deputy */
        public synchronized IDLClientsList getClientsList() throws secErrors {
            DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "CredsManager: getClientsList");

            // Create message, then find channel then send and wait for reply
            SecRequest getClientsList = null;
            int methodID = 0;

            try {
                methodID = secGenericConfig.getReverseSecMethodIDMap().
                           indexOf(secGenericConfig.METHOD_GET_CLIENTS_LIST);
                getClientsList = new SecGetClientsRequest(methodID);
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "CredsManager: ERROR: Could not create "+
                        "get clients list message!");
                throw new secErrors();
            }

            // Find the right connection
            secConnection conn = null;

            // Find a replica that supports this method
            try {
                conn = getChannel(methodID);
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "CredsManager: ERROR: Could not find a channel for "+
                        "GET CLIENTS LIST");
                throw new secErrors();
            }

            // Send the request and wait for the reply
            SecMessage reply = null;
            try {
                reply = sendRequestAndWait(conn, getClientsList);
            } catch (RpcException e) {
                if (secGenericConfig.CREDS_REVOKED.equals(e.getMessage())) {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "CredsManager: ERROR: our creds are revoked!");
                    throw new secErrors_credsRevoked();
                } else {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "CredsManager: ERROR: "+e.getMessage());
                    throw new secErrors_invOp();
                }
            } catch (Exception e) {
               DebugOutput.println(DebugOutput.DBG_NORMAL,
                "CredsManager: ERROR: Could not send get clients list request");
               throw new secErrors_comm();
            }
  
            if (!(reply instanceof SecGetClientsReply)) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                    "CredsManager: ERROR: Did not get GET CLIENTS reply");
                throw new secErrors();
            }

            // Restore the raw to the IDLClientsList
            rawDef rawList = ((SecGetClientsReply) reply).getClientsArg();

            // UNMARSHALL....
            // HERE HERE
            // 5 things
            // 1. number of entries
            // 2. keyhash
            // 3. bitmap
            // 4. serial number
            // 5. Name
            
            CredentialEntry cE = null;
            IDLClientsList idlCL = null; 
            RawCursor c = new RawCursor(rawList);

            try {
                int size = RawBasic.readInt32(c);
                idlCL = new IDLClientsList(size);

                for (int i=0; i < size; i++) {
                    IDLClientEntry idlCE = new IDLClientEntry();
                    idlCE.keyHash = new IDLPubKeyHash(0);
                    idlCE.keyHash.v = RawBasic.readOctetString(c);
                    idlCE.facb = new IDLAccessCtrlBitmap(0);
                    idlCE.facb.v = RawBasic.readOctetString(c);
                    idlCE.serialNumber = RawBasic.readInt64(c);
                    idlCE.name = new IDLSubjectName(0);
                    idlCE.name.v = RawBasic.readOctetString(c);
            
                    idlCL.v[i] = idlCE;
                }
            } catch (Exception e) {
                e.printStackTrace();
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                    "CredsManager: ERROR: Could not unmarshall client list");
                throw new secErrors();
            }

            return idlCL;
        }
        
        /** Method called by a client via the Deputy */
        public synchronized void updateCredentials(IDLSubjectName userName,
                                                IDLPubKeyHash userKeyHash,
                                                IDLAccessCtrlBitmap forwardACB,
                                                short /* credsOpMode */ mode)
            throws secErrors {

            // Create message, then find channel then send and wait for reply
            SecRequest updateCreds = null;
            int methodID = 0;

            DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "CredsManager: Update Credentials");

            // Marshall 4 parts
            // 1. User name (temporary !!, will be replaced by name in cert
            // 2. User's public key hash
            // 3. The new Forward AccessControlBitmap
            // 4. mode: add, remove or update (credsOpMode)
            rawDef r = RawOps.createRaw();
            RawCursor c = new RawCursor(r);
            try {
                RawBasic.writeOctetString(c, userName.v,
                                          0, userName.v.length);
                RawBasic.writeOctetString(c, userKeyHash.v,
                                          0, userKeyHash.v.length);
                RawBasic.writeOctetString(c, forwardACB.v,
                                          0, forwardACB.v.length);
                RawBasic.writeInt16(c, mode);

                methodID = secGenericConfig.getReverseSecMethodIDMap().
                           indexOf(secGenericConfig.METHOD_UPDATE_CREDENTIALS);
                updateCreds = new SecUpdateCredentialsRequest(r, methodID);
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "CredsManager: ERROR: Could not create "+
                        "update credentials message!");
                throw new secErrors();
            }

            // Find the right connection
            secConnection conn = null;

            // Find a replica that supports this method
            try {
                conn = getChannel(methodID);
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "CredsManager: ERROR: Could not find a channel for "+
                        "UPDATE Credentials!");
                throw new secErrors();
            }

            // Send the request and wait for the reply
            SecMessage reply = null;
            try {
                reply = sendRequestAndWait(conn, updateCreds);
            } catch (RpcException e) {
                if (secGenericConfig.CREDS_REVOKED.equals(e.getMessage())) {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "CredsManager: ERROR: our creds are revoked!");
                    throw new secErrors_credsRevoked();
                } else {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "CredsManager: ERROR: "+e.getMessage());
                    throw new secErrors_invOp();
                }
           } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                    "CredsManager: ERROR: Could not send update Cred request");
                throw new secErrors_comm();
            }
  
            // If we get a SecReply message and not a SecErrorReply message
            // I think that we can say the update was successful.
        }
        
        
        /**Method called by a client via the Deputy */
        public synchronized void revokeCertificate(long certSerialNum,
                                                   short clientCert)
                throws secErrors {

            DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "CredsManager: Revoke Certificate");

            // Create message, then find channel then send and wait for reply
            SecRequest revokeCert = null;
            int methodID = 0;

            // Marshall 2 parts
            // 1. Certificate Serialnumber of the to be revoked certificate
            // 2. Boolean if certificate belongs to a client
            rawDef r = RawOps.createRaw();
            RawCursor c = new RawCursor(r);
            try {
                RawBasic.writeInt64(c, certSerialNum);
                RawBasic.writeBool(c, clientCert);

                methodID = secGenericConfig.getReverseSecMethodIDMap().
                           indexOf(secGenericConfig.METHOD_REVOKE_CERTIFICATE);
                revokeCert = new SecRevokeCertificateRequest(r, methodID);
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "CredsManager: ERROR: Could not create "+
                        "revoke certificate message!");
                throw new secErrors();
            }

            // Find the right connection
            secConnection conn = null;

            // Find a replica that supports this method
            try {
                conn = getChannel(methodID);
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "CredsManager: ERROR: Could not find a channel for "+
                        "REVOKE Certificate");
                throw new secErrors();
            }

            // Send the request and wait for the reply
            SecMessage reply = null;
            try {
                reply = sendRequestAndWait(conn, revokeCert);
            } catch (RpcException e) {
                if (secGenericConfig.CREDS_REVOKED.equals(e.getMessage())) {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "CredsManager: ERROR: our creds are revoked!");
                    throw new secErrors_credsRevoked();
                } else {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "CredsManager: ERROR: "+e.getMessage());
                    throw new secErrors_invOp();
                }
           } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                    "CredsManager: ERROR: Could not send revoke cert request");
                throw new secErrors_comm();
            }
  
            // If we get a SecReply message and not a SecErrorReply message
            // I think that we can say the update was successful.
        }


        /** MARSHALLING/UNMARSHALLING of PERSISTENT STATE */
        public synchronized void marshallState(RawCursor state)
                throws ProtocolException {
            // If need to, store the CRL and credentials
            _credsCRL.marshallState(state);
        }
        
        public synchronized void unmarshallState(RawCursor state) 
                throws ProtocolException {

            // If need to, restore the CRL and credentials
            _credsCRL.unmarshallState(state);
        }


        public synchronized int isRevoked(Certificate[] chain)
                throws secErrors {
            // Ask CredsCRL if timestamp is revoked or not
            try {
                int n = _credsCRL.isRevoked(RightsCertUtil.getSerialNumber(
                                                                     chain));
                String debugString = null;
                switch(n) {
                    case secGenericConfig.NOT_REVOKED: {
                        debugString = "not revoked OK"; 
                        break;
                    } case secGenericConfig.REVOKED_CLIENT: {
                        debugString = "*** REVOKED CLIENT ***";
                        break;
                    } case secGenericConfig.REVOKED_REPLICA: {
                        debugString = "*** REVOKED REPLICA ***"; 
                        break;
                    } default: {
                        debugString = "UNKOWN RETURN VALUE !!";
                    }
                }
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                            "CredsManager: "+debugString);
                return n;
            } catch (Exception e) {
                throw new secErrors_invOp();
            }
        }

        
        /** Try and get the new CRL from Chief in case this is a Sergeant, 
         * or any if this is a Recruit. This function is called from within a
         * synchronized block.
         */
        private void getNewCRL() throws Exception {
            int methodID = 0;
            secConnection conn = null;
            SecRequest req = null;
            SecMessage reply = null;
            boolean isSergeant = false;    // setNoCRLMode only for Sergeants

            if (_getCRL && _getWholeCRL) {
                // Sergeant case
                isSergeant = true;
                
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "CredsManager: RUN: getting WHOLE CRL");

                // Find a replica that supports this method
                methodID = secGenericConfig.getReverseSecMethodIDMap().
                                indexOf(secGenericConfig.METHOD_GET_WHOLE_CRL);
                try {
                    conn = getChannel(methodID);
                } catch (Exception e) {
                    conn = null;
                }
                req = new SecGetWholeCRLRequest(methodID);
            } else if (_getCRL && !_getWholeCRL) {
                // Recruit case
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "CredsManager: RUN: get CRL");

                // Find a replica that supports this method
                methodID = secGenericConfig.getReverseSecMethodIDMap().
                                      indexOf(secGenericConfig.METHOD_GET_CRL);
                try {
                    conn = getChannel(methodID);
                } catch (Exception e) {
                    conn = null;
                }
                req = new SecGetCRLRequest(methodID);
            } else if (_refreshCRL 
                       && !_getCRL
                       && !_getWholeCRL) {
                // Check if this is a new instance of the CRL, in that case
                // we create the new one here and now, otherwise we start with
                // the old one.
                if (_credsCRL.getLastTime() == 0) {
                    try {
                        refreshCRL();
                    } catch (Exception e) {
                        DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "CredsManager: ERROR: Could not refresh CRL !");
                    }
                }
                return;
            }

            if (conn == null) {
                // FSCK could not find any channel !
                if (isSergeant) {
                    setNoCRLMode(true);
                    return;
                } else {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "CredsManager: ERROR: No channel to get CRL from !");
                    throw new secErrors_noChannel();
                }
            }

            try {
                reply = sendRequestAndWait(conn, req);
            } catch (RpcException e) {
                if (secGenericConfig.CREDS_REVOKED.equals(e.getMessage())) {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "CredsManager: ERROR: our creds are revoked!");
                    throw new secErrors_credsRevoked();
                } else {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "CredsManager: ERROR: "+e.getMessage());
                    if (isSergeant) {
                        setNoCRLMode(true);
                        return;
                    } else {
                        throw new secErrors_invOp();
                    }
                }
           } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL, 
                        "CredsManager: ERROR: Could not send get CRL request");
                if (isSergeant) {
                    setNoCRLMode(true);
                    return;
                } else {
                    throw e;
                }
            }

            if (_getWholeCRL) {
                if (! (reply instanceof SecGetWholeCRLReply)) {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "CredsManager: ERROR: Invalid reply on get WHOLE CRL");
                    if (isSergeant) {
                        setNoCRLMode(true);
                        return;
                    } else {
                        throw new secErrors_invOp();
                    }
                }
                try {
                    checkAndUpdateCRL(((SecGetWholeCRLReply)reply).getCRLArg());
                } catch (Exception e) {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "CredsManager: ERROR: Could not update CRL ! (1)");
                    if (isSergeant) {
                        setNoCRLMode(true);
                        return;
                    } else {
                        throw e;
                    }
                }
            } else {
                if (! (reply instanceof SecGetCRLReply)) {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "CredsManager: ERROR: Invalid reply on get CRL");
                    if (isSergeant) {
                        setNoCRLMode(true);
                        return;
                    } else {
                        throw new secErrors_invOp();
                    }
                }
                try {
                    checkAndUpdateCRL(((SecGetCRLReply) reply).getCRLArg());
                } catch (Exception e) {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "CredsManager: ERROR: Could not update CRL ! (2)");
                    if (isSergeant) {
                        setNoCRLMode(true);
                        return;
                    } else {
                        throw e;
                    }
                }
            }
            // Everything okay, return to normal if needed
            if (isSergeant && _noCRLMode) {
                setNoCRLMode(false);
            }

            // Using the new CRL, kick out connections that have been revoked
            checkForRevokedConnections();
        }


        /** Hash the lastest CRL, update timestamp etc. This function is also
         * called from inside a synchronized block.
         */
        private void refreshCRL() throws secErrors {
            DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "CredsManager: RUN: updating CRL");
            _credsCRL.refreshCRLs();

            // Using the new CRL, kick out connections that have been revoked
            checkForRevokedConnections();
        }


        /** Helper functions */
    
        private void checkAndUpdateCRL(rawDef idlCRL) throws secErrors {
            DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "CredsManager: CHECK and UPDATE CRL");
            _credsCRL.checkAndUpdateCRL(idlCRL);
        }

        
        // Send any message, plain message can be sent directly
        private void sendMessage(secConnection secConn, SecMessage message)
            throws commErrors_comm, commErrors_invOp {

            // Marshall the message
            RawCursor cursor = new RawCursor(RawOps.createRaw());
            message.marshall(cursor);

            // and send it on its way
            try {
                secConn.send(cursor.getRaw(), SecConnection.SEC_OBJ);
            } catch (Exception e) {
                throw new commErrors_comm();
            }
        }

        // Sends a request and waits for the reply to come back
        private SecReply sendRequestAndWait(secConnection secConn,
                                            SecRequest request)
            throws commErrors_comm, commErrors_invOp, RpcException {

            RpcEntry rpc = null;

            // create rpc entry which will set the rpcId in the request
            try {
                rpc = _rpcTable.createRpc(new Integer(secConn.getChannelID()),
                                          request);
            } catch (Exception e) {
                throw new commErrors_invOp();
            }

            // send the message
            sendMessage(secConn, request);

            // wait for this thread to be woken up
            while (true) {
                try {
                    return rpc.awaitSecReply();
                } catch (InterruptedException e) {
                    // ignoring the interrupt.
                } // If RpcException, it is passed on   
            }
        }


        private void setNoCRLMode(boolean on) {
            // Acquire the lock
            synchronized(_pauseLock) {
                try {
                    if (on) {
                        if (!_noCRLMode) {
                            DebugOutput.println(DebugOutput.DBG_DEBUG,
                              "CredsManager: ERROR: Switching to NO CRL mode!");

                            // set the CRLInterval to one minute
                            _noCRLInterval = _ONE_MINUTE;
                            _noCRLMode = on;
                            pause(on);
                        } else {
                            // Multiply current interval by 1.5 each time.
                            _noCRLInterval = (long) (_noCRLInterval * 1.5);
                        }
                        _tRscMgr.reschedule(_timerID, _noCRLInterval,
                                            _noCRLInterval);
                    } else if (_noCRLMode) {                // !on && _noCRLMode
                        // reschedule back to normal
                        DebugOutput.println(DebugOutput.DBG_DEBUG,
                            "CredsManager: Switching back to NORMAL mode!");
                        _tRscMgr.reschedule(_timerID,
                                            _credsCRL.getIntervalTime(),
                                            _credsCRL.getFirstIntervalTime());
                        _noCRLMode = on;
                        pause(on);
                    }
                } catch (Exception e) {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                            "CredsManager: CAN'T SET CRL MODE TO: "+on);
                }
            }
        }
    }

    /******************** CredsCRL CLASS **********************************/
    /** This class the contains the Credentials list and the CRL lists.
     * It will mantain the lists and take instruction from the Credentials
     * Manager. The methods are NOT synchronized as they are called from the
     * already synchronized CredsManager.
     */
    private class CredsCRL {

        // Storage of the Credentials with CredentialEntries
        private Vector _credsList = null;

        // Storage of the CRL, Stores the certificate serial number in Date
        private Set _replicaCRLList = null; // List with revoked replica certs
        private Set _clientCRLList = null;  // List with revoked client certs
        private rawDef _marshalledReplicaCRL = null;
        private rawDef _marshalledClientCRL = null;
        private rawDef _crlIssuerChain = null;
            
        // Last CRL update
        private long _lastTime = 0;

        // If CRL needs updating, needed after a restart, DEFAULT true
        private boolean _isOld = true;

        private AccessCtrlBitmap _bitTest = new AccessCtrlBitmap(0);
        

        // Constructor
        public CredsCRL() {
            // What do we need
            _credsList = new Vector();
            _replicaCRLList = new HashSet();
            _clientCRLList = new HashSet();
        }

        // Finds out the interval time which is told in the CRL
        public long getIntervalTime() {

            // Returns the interval time for the CRL, to be gotten from the CRL
            return _interval;
        }

        public long getLastTime() {
            return _lastTime;
        }

        public long getFirstIntervalTime() {
            // Return the time when the scheduled task should be run for the
            // first time, using the timestamp in the CRL and the interval.
            long nextTime = 0;
            long currentTime = System.currentTimeMillis();
            long firstTime = 0;
            
            // If _lastTime too small (or even 0), it will take a LONG time 
            // before we are caught up and this should not happen when we get
            // here.
            // 2003-09-11 16:48:40 +0200, CRLs should not be older than this
            // because this program didn't exist before this ;)
            long DEVELOP_TIME = 1063291720000L;
            if (_lastTime < DEVELOP_TIME) {
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                    "CredsCRL: WARNING: lastTime was smaller than time "+
                    "this program even existed ("+_lastTime+") !?!");
                // get it immediately
                return 1000;
            }
            
            if (_refreshCRL && !_getCRL) {
                nextTime = _lastTime + _interval;
            } else {
                // Aim for about 1000 millisecs after the CRL is updated
                nextTime = _lastTime + _interval + 1000;
            }

            firstTime = (nextTime - currentTime);

            // It could be that we were deactivated a long time.
            // replica expect us to update at a particular time, so catch up 
            // to the next time.
            while (firstTime < 20) {
                // Skip one to synchronize, could get into trouble 
                nextTime += _interval;
                firstTime = (nextTime - currentTime);
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                        "CredsCRL: WARNING: Catching up to time interval !");
            }

            return firstTime;
        }
       
        public void checkToReschedule() throws secErrors {

            // Don't know what to do here yet and if we need it ?
            // _tmrRscMgr.reschedule(_timerID, ...);

        }

        public rawDef getCredentials(rawDef rawUserChain) throws secErrors {

            if (_issuerAdminKS == null) {
                if (_cfg.admincreds != null) {
                    setKeyMaterials();
                } else {
                    // Hmm
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "This object is not allowed to generate "+
                                "credentials, go away ");
                    throw new secErrors();
                }
            }
            

            // Transform the rawUserChain to a IDLIdentCerts
            // // Two parts:
            // 1. chain length
            // 2. the chain
            
            RawCursor cursor = new RawCursor(rawUserChain);
            IDLIdentCerts identChain;
            int chainLength;
            
            try {
                chainLength = RawBasic.readInt32(cursor);
                identChain = new IDLIdentCerts(chainLength);
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                            "CredsCRL: Received user Ident chain of length: "+
                            chainLength);
                for (int i=0; i < chainLength; i++) {
                    IDLIdentCert idlICert = new IDLIdentCert(0);
                    idlICert.v = RawBasic.readOctetString(cursor);
                    identChain.v[i] = idlICert;
                }
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "CredsCRL: ERROR: Incorrect parameter");
                throw new secErrors();
            }

            if (_caCertsKS == null) {
                // First load the CA certificates
                try {
                    _caCertsKS = IdentCertUtil.idlIdentCertsToKeyStore(
                                                                _cfg.cacerts);
                } catch (Exception e) {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "CredsCRL: ERROR: Could not load CA Keystore");
                    throw new secErrors();
                }
            }
            
            PublicKey userPub = null;
            String keyHash = null;
            String name = null;
            
            // Do basic chain checking and get the user's public key, then
            // calculate the SHA-256 hash and have it returned as a hex string
            try {
                if (!IdentCertUtil.extendedChainCheck(identChain, _caCertsKS)) {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                                    "BASIC IDENT CHAIN CHECK FAILED !!!");
                    throw new secErrors(); // Will be caught and rethrown
                }
                userPub = IdentCertUtil.getUserPublicKey(identChain);
                keyHash = IdentCertUtil.getSHA256HexOfKey(identChain);
                name = IdentCertUtil.getSubjectName(identChain);
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                                    "CredsCRL: User has key hash: "+keyHash);
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                                    "Couldn't get userpub,keyhash or name");
                throw new secErrors();
            }
            
            // Find the FACB for this user if its public key is in the credsList
            // Use the public facb if it is now found.
            AccessCtrlBitmap facb = findFACB(keyHash);

            boolean found = false;
            if (facb == null) {
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                                    "CredsCRL: NOT FOUND in Creds list"+
                                    " using public FACB");
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                                    "CredsCRL: "+keyHash);
                facb = new AccessCtrlBitmap(_cfg.publicFACB.v);
            } else {
                found = true;
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                                    "CredsCRL: FOUND in Creds list");
            }

            // Create the new Certificate
            rawDef r = RawOps.createRaw();
            RawCursor c = new RawCursor(r);

            // Create the certificate for the user
            Certificate[] userChain = null;
            try {
                userChain = RightsCertUtil.generateUserCertificateChain(
                                    _issuerAdminKS, facb, userPub);
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                    "CredsCRL: ERROR generating user's cert chain");
                throw new secErrors();
            }
            
            // Marshall the chain
            RawBasic.writeInt32( c, userChain.length );

            for (int i=0; i < userChain.length; i++) {
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                                "CredsCRL: sending certificate "+i);
                try {
                    byte[] certBytes =  userChain[i].getEncoded();
                    RawBasic.writeOctetString(c, certBytes,
                                              0, certBytes.length);
                } catch (Exception e) {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "CredsCRL: ERROR: encoding certificate");
                    throw new secErrors();
                }
            }
           
            try {
                updateCreds(found, keyHash, facb, 
                            RightsCertUtil.getSerialNumber(userChain), name);
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                                "CredsCRL: ERROR: could not update creds!");
                throw new secErrors(); 
            }
            return r;
        }


        public rawDef updateCredentials(rawDef creds) throws secErrors {

            // 4 parts
            // 1. User name
            // 2. User's public key hash
            // 3. The new Forward AccessControlBitmap
            // 4. mode: add, remove or update (credsOpMode)

            RawCursor cursor = new RawCursor(creds);
            IDLSubjectName idlSubjectName;
            IDLPubKeyHash idlKeyHash;
            IDLAccessCtrlBitmap idlFACB;
            int opMode;
            
            try {
                idlSubjectName = new IDLSubjectName(0);
                idlSubjectName.v = RawBasic.readOctetString(cursor);
                    
                idlKeyHash = new IDLPubKeyHash(0);
                idlKeyHash.v = RawBasic.readOctetString(cursor);
    
                idlFACB = new IDLAccessCtrlBitmap(0);
                idlFACB.v = RawBasic.readOctetString(cursor);

                opMode = RawBasic.readInt16(cursor);
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                    "CredsCRL: ERROR: updateCreds: Incorrect parameter");
                throw new secErrors();
            }

            // Transform the pubkeyhash into a string and the idlFACB into
            // a AccessCtrlBitmap (might be null !);
            String userName= new String(idlSubjectName.v);
            String keyHash = new String(idlKeyHash.v);
            
            if (opMode == credsOpMode.add 
                || opMode == credsOpMode.update) {
                AccessCtrlBitmap facb = new AccessCtrlBitmap(idlFACB.v);
                updateCreds(keyHash, facb, userName);
            } else if (opMode == credsOpMode.remove) {
                deleteCreds(keyHash);
            } else {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                    "CredsCRL: ERROR: updateCreds: UNKNOWN opMode !");
                throw new secErrors();
            }

            // Empty raw as reply
            rawDef r = RawOps.createRaw();
            return r;
        }

        public rawDef revokeCertificate(rawDef rawCert) throws secErrors {

            // 2 parts
            // 1. Certificate serial number of the to be revoked certificate
            // 2. Boolean to indicate a client certificate

            RawCursor cursor = new RawCursor(rawCert);
            long certSerial;
            boolean clientCert;

            
            try {
                certSerial = RawBasic.readInt64(cursor);
                clientCert = RawBasic.readBool(cursor);
                DebugOutput.println(DebugOutput.DBG_DEBUG,
                  "CredsCRL: REVOKING "+certSerial+" is client = "+clientCert);
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                    "CredsCRL: ERROR: revokeCert: Incorrect parameter");
                throw new secErrors();
            }

            // Revoke the certificate
            if (clientCert) {
                _clientCRLList.add(new Long(certSerial));
            } else {
                _replicaCRLList.add(new Long(certSerial));
            }

            // Recalculate CRL list hashes, do NOT correct timestamps
            boolean newTime = false;
            updateCRLS(newTime);    // update the CRL but don't change timestamp

            // Using the new CRL, kick out connections that have been revoked
            checkForRevokedConnections();

            // Empty raw as reply
            rawDef r = RawOps.createRaw();
            return r;
        }

        public rawDef getWholeCRL() throws secErrors {

            // Grab the _marshallledReplicaCRL and the _marshalledClientCLR
            // and return the rawDef. We are skipping storing it in the
            // IDLAllCRL structure.
            rawDef r = RawOps.createRaw();
            RawCursor c = new RawCursor(r);
                
            // 2 parts
            // 1. ReplicaCRL
            RawBasic.writeOctetString(c, _marshalledReplicaCRL);

            // 2. ClientCRL
            RawBasic.writeOctetString(c, _marshalledClientCRL);

            return r;
        }

        public rawDef getCRL() throws secErrors {

            // Just return the replica CRL wrapped in another rawDef
            rawDef r = RawOps.createRaw();
            RawCursor c = new RawCursor(r);
            RawBasic.writeOctetString(c, _marshalledReplicaCRL);
            return r;
        }

        public rawDef getClientsList() throws secErrors {
            rawDef r = RawOps.createRaw();
            RawCursor c = new RawCursor(r);
            try {
                marshallCredentials(c);
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                    "CredsCRL: ERROR: Could not marshall client list");
                throw new secErrors();
            }
            return r;
        }

        
        public void refreshCRLs() throws secErrors {

            updateCRLS();       // Get into trouble if clientCRL and replicaCRL
                                // timestamps are not the same.. TRUST ME :)
        }

        public void checkAndUpdateCRL(rawDef idlCRL) throws secErrors {

            // The first time we check the CRL the timestamp is allowed to be
            // off. It can happen that a replica fetches the old crl and when it
            // is time to check the timestamp it could have passed the expire
            // time. So the first time we will allow this, next time we don't.
            boolean allowExpire = false;
            if (_lastTime == 0) {
                allowExpire = true;
            }

            rawDef idlClientCRL = null;
            rawDef idlReplicaCRL = null;
            IDLRevocationList revokedClient = null;
            IDLRevocationList revokedReplica = null;

            RawCursor c = new RawCursor(idlCRL);

            // Okay, no. 1, always ReplicaCRL
            try {
                idlReplicaCRL = RawBasic.readROctetString(c);
            } catch (ProtocolException e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                    "CredsCRL: ERROR: Could not read CRL: "+e.getMessage());
                throw new secErrors();
            }

            // Check if hashes are okay etc.
            revokedReplica = checkCRL(idlReplicaCRL, allowExpire);

            // Set the replicaCRL
            _marshalledReplicaCRL = idlReplicaCRL;
            
            if (_getWholeCRL) {
                // 2. clientCRL
                try {
                    idlClientCRL = RawBasic.readROctetString(c);
                } catch (ProtocolException e) {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "CredsCRL: ERROR: could not read CRL: "+e.getMessage());
                    throw new secErrors();
                }
                revokedClient = checkCRL(idlClientCRL, allowExpire);

                // Set the client CRL
                _marshalledClientCRL = idlClientCRL;
            }

            // Update the CRL(s) This will completely replace the existing
            // CRLs.
            _replicaCRLList.clear();
            for (int i=0; i < revokedReplica.v.length; i++) {
                Long temp = new Long(revokedReplica.v[i]);
                _replicaCRLList.add(temp);
            }

            if (_getWholeCRL) {
                _clientCRLList.clear();
                for (int i=0; i < revokedClient.v.length; i++) {
                    Long temp = new Long(revokedClient.v[i]);
                    _clientCRLList.add(temp);
                }
            }
        }
            
        public void marshallState(RawCursor state) throws ProtocolException {

            // Marshall the CRLs and the time interval if needed
            if (_refreshCRL) {
                // We also store the credentials
                if (!_getCRL) { // FIXME Should have own in init string
                    marshallCredentials(state);
                }

                // The CRL is always up to date, so no need for updateCRLS
                // here.
 
                // We need to store CRL as we create it, all the others
                // don't need to store it as they should fetch the latest
                // anyway.
                // 2 things:
                // 1. write the Client CRL List
                RawBasic.writeOctetString(state, _marshalledClientCRL);

                // 2. write the Replica CRL List
                RawBasic.writeOctetString(state, _marshalledReplicaCRL);
            }
        }


        public void unmarshallState(RawCursor state) throws ProtocolException {

            // Only unmarshall if we need to
            if (_refreshCRL) {
                if (!_getCRL) { // This is how we identify the Chief
                    // restore credentials
                    unmarshallCredentials(state);
                }
                    
                // 2 things:
                // 1. Client CRL List
                _marshalledClientCRL = RawBasic.readROctetString(state);

                // 2. Replica CRL list
                _marshalledReplicaCRL = RawBasic.readROctetString(state);

                // Recreate the CRL lists
                boolean allowExp = true;
                IDLRevocationList revokedClients = null;
                IDLRevocationList revokedReplicas = null;
                try {
                    revokedClients = checkCRL(_marshalledClientCRL, allowExp);
                    revokedReplicas = checkCRL(_marshalledReplicaCRL, allowExp);
                } catch (Exception e) {
                    // The CRLs are wrong
                    _clientCRLList.clear();
                    _replicaCRLList.clear();
                    throw new ProtocolException();
                }

                _clientCRLList.clear();
                for (int i=0; i < revokedClients.v.length; i++) {
                    Long temp = new Long(revokedClients.v[i]);
                    _clientCRLList.add(temp);
                }

                _replicaCRLList.clear();
                for (int i=0; i < revokedReplicas.v.length; i++) {
                    Long temp = new Long(revokedReplicas.v[i]);
                    _replicaCRLList.add(temp);
                }

                if (!_getCRL) {
                    catchUpCRL();
                }
            }
        }

        
        private void catchUpCRL() {
            // Make the CRL as recent as possible. Consider for example the
            // following:
            // Say the interval time is 3 days. At the start of the Chief 
            // sec ob, the timestamp will be 0 (for the first day). 
            // Then shortly after is goes down. 
            // After 10 days it is brought up again. The old
            // marshalled timestamp of the CRL still says 0. So the timestamp
            // needs to be updated to timestamp 9. The next update will occur
            // in 2 days (getFirstTimeInterval). If we were to wait for the next
            // update, Sergeants would have to wait 2 days before getting a
            // valid CRL list.
            long currentTime = System.currentTimeMillis();

            // calculate the latest timestamp, note that updateCRLs also add
            // _interval ! The lastest timestamp needs to be smaller then
            // the current time.
            while (_lastTime < currentTime) {
                _lastTime += _interval;
            }

            // _lastTime is now 2 intervals to far, subtract them
            _lastTime -= (2*_interval);
            
            // Refresh the CRLs
            try {
                updateCRLS();
            } catch (secErrors e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                                    "CredsCRL: ERROR updating CRLS !");
            }
        }
        
        private void marshallCredentials(RawCursor state)
                throws ProtocolException {

            // 5 things
            // 1. number of entries
            // 2. keyhash
            // 3. bitmap
            // 4. serial number
            // 5. name
            int size = _credsList.size();
            CredentialEntry cE = null;
            RawBasic.writeInt32(state, size);

            for (int i=0; i < size; i++) {
            
                cE = (CredentialEntry) _credsList.get(i);
                
                // Write the string and the access ctrl bitmap
                byte[] keyBytes = cE.getKeyHash().getBytes();
                byte[] acbBytes = cE.getFACB().toByteArray();
                byte[] nameBytes = cE.getName().getBytes();
                RawBasic.writeOctetString(state, keyBytes, 0, keyBytes.length);
                RawBasic.writeOctetString(state, acbBytes, 0, acbBytes.length);
                RawBasic.writeInt64(state, cE.getSerialNumber());
                RawBasic.writeOctetString(state, nameBytes,0,nameBytes.length);
            }
        }

        private void unmarshallCredentials(RawCursor state)
                throws ProtocolException {
            // 5 things
            // 1. number of entries
            // 2. keyhash
            // 3. bitmap
            // 4. serial number
            // 5. name
            
            CredentialEntry cE = null;
                        
            // Clear the list
            _credsList.clear();
            
            int size = RawBasic.readInt32(state);
            for (int i=0; i < size; i++) {
                String keyHash = new String(RawBasic.readOctetString(state));
                AccessCtrlBitmap acb = 
                       new AccessCtrlBitmap(RawBasic.readOctetString(state));
                long serial = RawBasic.readInt64(state);
                String name = new String(RawBasic.readOctetString(state));

                cE = new CredentialEntry(keyHash, acb, serial, name);
                _credsList.add(cE);
            }
        }

        // This is a multi check with multiple answers as return value:
        // not revoked
        // revoked, in client list
        // revoked, in replica list
        public int isRevoked(long serialNumber) throws secErrors {

            DebugOutput.println(DebugOutput.DBG_DEBUG,
                    "CredsCRL: CHECK REVOKE FOR: "+serialNumber);
            // Check both the client and the replica list if available
            Long serial = new Long(serialNumber);
            
            if (_clientCRLList != null
                && _clientCRLList.contains(serial)) {
                return secGenericConfig.REVOKED_CLIENT;
            }

            if (_replicaCRLList.contains(serial)) {
                return secGenericConfig.REVOKED_REPLICA;
            }

            return secGenericConfig.NOT_REVOKED;   // not found/not revoked
        }

        
        public boolean isOld() {
            // Tells if a new CRL needs to be fetched
            return _isOld;
        }


        private IDLRevocationList checkCRL(rawDef crl) throws secErrors {

            return checkCRL(crl, false);
        }

        private IDLRevocationList checkCRL(rawDef crl, boolean allowExpire)
                throws secErrors {

            // Unwrap the CRL and do some sanity checks
            long timeStamp;
            long currentTime;
            long expire;
            int nrOfEntries;
            int dataSize;
            IDLRevocationList idlRList;
            rawDef signerChain;
            byte[] sigHash;
            
            currentTime = System.currentTimeMillis();
            
            RawCursor c = new RawCursor(crl);

            try {
                // 7 parts
                // 1. the timestamp
                timeStamp = RawBasic.readInt64(c);

                // 2. the expire time
                expire = RawBasic.readInt64(c);
                
                if (_interval == 0 || _interval != expire) {

                    // Make sure the timer is not ridiculous
                    if (expire < 100) {
                        _interval = secGenericConfig.DEFAULT_INTERVAL_TIME;
                    } else {
                        _interval = expire;
                    }
                }

                // Check: timestamp should not be off by too much
                if ((timeStamp + expire) < currentTime) {
                    if (!allowExpire) {
                        DebugOutput.println(DebugOutput.DBG_NORMAL,
                           "CredsCRL: ERROR: CRL timestamp past expire time !");
                    }

                    // We allow expire when we are restarting a Sergeant/Chief
                    // This way the unmarshall can complete normally and by
                    // default we will fetch the new crl.
                    // In any other case, this will throw an exception to
                    // indicate that the received CRL was NOT OKAY.
                    if (!allowExpire) {
                        throw new secErrors();
                    } else {
                        // Replica should discard the CRL and allow to get an
                        // out of date CRL from the Chief
                        if (_getCRL) {
                            _lastTime = 0;
                        } else {
                            // Chief should use this to catch up to his regular
                            // interval that replicas expect from it.
                            _lastTime = timeStamp;
                        }
                    }
                } else {
                    _isOld = false;
                    _lastTime = timeStamp;
                }

                // 3. the number of entries in the revokation list
                nrOfEntries = RawBasic.readInt32(c);

                // 4. the revokation list
                idlRList = new IDLRevocationList(nrOfEntries);
                for (int i=0; i < nrOfEntries; i++) {
                    idlRList.v[i] = RawBasic.readInt64(c);
                }

                // 5. The issuer's chain
                signerChain = RawBasic.readROctetString(c);

                // 6. The number of bytes up to now EXCLUDING this int;
                dataSize = RawBasic.readInt32(c);
                
                // 7. The signature
                sigHash = RawBasic.readOctetString(c);
            } catch (ProtocolException e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "CredsCRL: ERROR: Could not read CRL");
                throw new secErrors();
            }

            // Verify the MD5 with RSA encryption of the CRL
            boolean sigOK = false;
            byte[] data = RawOps.getRaw(crl);
            try {
                // Check the certificate chain
                IDLRightsCerts idlChain = 
                                RightsCertUtil.rawToRightsChain(signerChain);
                
                // Use X509AccessControl to check the chain
                X509AccessControl test = new X509AccessControl(idlChain, 
                                                               _objectHandle);

                PublicKey pk = RightsCertUtil.getSignerPublicKey(idlChain);
                    
                // Get all the bytes, except for last entry which is the hash
                Signature signer =Signature.getInstance("MD5WithRSAEncryption");
                signer.initVerify(pk);
                signer.update(data, 0, dataSize);
                sigOK = signer.verify(sigHash);
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                    "CredsCRL: ERROR: Could not verify data: "+e.getMessage());
                throw new secErrors();
            }

            if (!sigOK) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                    "CredsCRL: ERROR: INCORRECT SIGNATURE on the CRL !");
                throw new secErrors();
            }
 
            return idlRList;
        }

       
        private void updateCRLS() throws secErrors {
            updateCRLS(true);
        }

        private void updateCRLS(boolean newTime) throws secErrors {
            // Recalculate hash etc.

            // Don't use currentTimeMillis otherwise the timestamp will drift.
            // This will cause problems after restarting the Chief/Sergeant.
            // Only use it the first time.
            long timestamp;
            if (_lastTime == 0) {
                timestamp = System.currentTimeMillis();
            } else {
                if (newTime) {
                    timestamp = _lastTime + _interval;
                } else {
                    timestamp = _lastTime;
                }
            }
            long expire = _interval;

            IDLRevocationList idlClientCRLList = null;
            IDLRevocationList idlReplicaCRLList = null;
            Set clientCRLS = null;
            Set replicaCRLS = null;
    
            clientCRLS = _clientCRLList;
            replicaCRLS = _replicaCRLList;
    
            idlClientCRLList = new IDLRevocationList(clientCRLS.size());
 
            Iterator it = clientCRLS.iterator();
            int i = 0;
            while (it.hasNext()) {
                idlClientCRLList.v[i] = ((Long) it.next()).longValue();
                i++;
            }

            idlReplicaCRLList = new IDLRevocationList(replicaCRLS.size());

            it = replicaCRLS.iterator();
            i=0;
            while (it.hasNext()) {
                idlReplicaCRLList.v[i] = ((Long) it.next()).longValue();
                i++;
            }
            
            _marshalledClientCRL = calculateCRLSig(timestamp, expire,
                                                   idlClientCRLList);
            _marshalledReplicaCRL = calculateCRLSig(timestamp, expire,
                                                    idlReplicaCRLList);

            _lastTime = timestamp;
        }
        

        /** Calculate the signature and return a rawDef containing
         * everything including the signature.
         */
        private rawDef calculateCRLSig(long timestamp, long expire,
                                       IDLRevocationList idlCRLList)
                throws secErrors {
    
            rawDef rawCRL = RawOps.createRaw();
            RawCursor c = new RawCursor(rawCRL);
    
            // Parts: 5
            // 1. timestamp
            RawBasic.writeInt64(c, timestamp);
    
            // 2. expire time
            RawBasic.writeInt64(c, expire);

            // 3. number of CRL entries
            RawBasic.writeInt32(c, idlCRLList.v.length);
    
            // 4. all the entries
            for (int i=0; i < idlCRLList.v.length; i++) {
                RawBasic.writeInt64(c, idlCRLList.v[i]);
            }
   
            // 5. the chain of the signer (us)
            if (_crlIssuerChain == null) {
                try {
                    _crlIssuerChain = 
                            RightsCertUtil.rightsChainToRaw(_cfg.creds.chain);
                } catch (Exception e) {
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                        "CredsCRL: ERROR: Could not convert chain to raw");
                    throw new secErrors();
                }
            }
            RawBasic.writeOctetString(c, _crlIssuerChain);
   
            byte[] rawBytes = RawOps.getRaw(rawCRL);

            // 6. write the data portion size excluding this int !
            RawBasic.writeInt32(c, rawBytes.length);
            
            byte[] sigHash;
    
            if (_issuerAdminKS == null) {
                if (_cfg.admincreds != null) {
                    setKeyMaterials();
                } else {
                    // Hmm
                    DebugOutput.println(DebugOutput.DBG_NORMAL,
                            "CredsCRL: ERROR: This object is not allowed to "+
                            "generate CRLs!");
                    throw new secErrors();
                }
            }
                        
            // Calculate the MD5 with RSA encryption of the CRL
            try {
                Signature signer =Signature.getInstance("MD5WithRSAEncryption");
                signer.initSign(_privKey);
                signer.update(rawBytes);
                sigHash = signer.sign();
            } catch (Exception e) {
                DebugOutput.println(DebugOutput.DBG_NORMAL,
                    "CredsCRL: ERROR: Could not sign data: "+e.getMessage());
                throw new secErrors();
            }

            // 7. the signature itself
            RawBasic.writeOctetString(c, sigHash, 0, sigHash.length);

            // Done
            return rawCRL;
        }
    

        private AccessCtrlBitmap findFACB(String keyHash) {
    
            // Walk through the Vector
            CredentialEntry cE = null;
            
            for (int i=0; i < _credsList.size(); i++) {
                cE = (CredentialEntry) _credsList.get(i);
    
                if (keyHash.equals(cE.getKeyHash())) {
                    return cE.getFACB();
                }
            }
    
            return null;
    
        }
    
        // Used when client connects and we get the serial number
        private void updateCreds(boolean found, String keyHash,
                                 AccessCtrlBitmap facb, long serialNumber,
                                 String name) {
    
            CredentialEntry cE = null;
            // If we already searched before and didn't find anything
            if (!found) {
                cE = new CredentialEntry(keyHash, facb, serialNumber, name);
                _credsList.add(cE);
                return;
            }

            // The credentials list contains in the beginning an entry for
            // a private user with serial number 0. This serial number will be
            // set the first time this private user gets his credentials.
            // Subsequent getCredentials request from the same private user will
            // result in new certificates with different serial numbers but with
            // the same FACB as this is identified by the public key hash which
            // is always the same. Public users will not be in the list (yet).

            for (int i=0; i < _credsList.size(); i++) {
                cE = (CredentialEntry) _credsList.get(i);
    
                // THERE CAN BE AT MOST one entry with serialNumber 0 per
                // keyhash
                if (keyHash.equals(cE.getKeyHash()) 
                    && (cE.getSerialNumber() == 0)) {
                    // Update this entry
                    cE.setSerialNumber(serialNumber);
                    cE.setFACB(facb);
                    cE.setName(name);   // Override the name given by owner 
                                        // with the name in the certificate.
                    return;
                }   
            }
    
            // did not find the entry, or none with 0 serial number,create new.
            cE = new CredentialEntry(keyHash, facb, serialNumber, name);
            _credsList.add(cE);
        }
  
        // Used when the owner updates facb
        private void updateCreds(String keyHash, AccessCtrlBitmap facb,
                                 String name) {
    
            // Revoke and remove all entries with a serialnumber
            CredentialEntry cE = null;
   
            deleteCreds(keyHash);
    
            // add the new entry
            cE = new CredentialEntry(keyHash, facb, name);
            _credsList.add(cE);
        }
    
        private void deleteCreds(String keyHash) {
            // Revoke and remove all
            CredentialEntry cE = null;
    
            int[] toRemove = new int[_credsList.size()];
            int nrRemove = 0;
    
            for (int i=0; i < _credsList.size(); i++) {
                cE = (CredentialEntry) _credsList.get(i);
    
                // If key hash matches, then revoke if that entry has serialnum
                if (keyHash.equals(cE.getKeyHash())) {
                    if (cE.getSerialNumber() != 0) {
                        _clientCRLList.add(new Long(cE.getSerialNumber()));
                    } else {
                    }
                    toRemove[nrRemove] = i;
                    nrRemove++;
                }
            }
   
            // NOTE Delete highest to lowest ! Indices get updated after remove
            // and thus don't match anymore with our to be removed indices if
            // we remove one with lower index than next one
            for (int j=(nrRemove-1); j >= 0; --j) {
                _credsList.remove(toRemove[j]);
            }

            // Update the CRLS
            boolean newTime = false;
            try {
                updateCRLS(newTime);
            } catch (Exception e) {
                // Hmmm, can't do much about this...
            }
        }
    }
}
