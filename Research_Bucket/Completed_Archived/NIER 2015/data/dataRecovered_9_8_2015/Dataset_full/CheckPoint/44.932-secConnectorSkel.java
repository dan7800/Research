/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.security.skels;

import vu.globe.rts.java.*;
import vu.globe.idlsys.g;
import vu.globe.rts.comm.idl.p2p.*;
import vu.globe.svcs.gls.idl.lsClient.*;
import vu.globe.rts.security.idl.security.*;
import vu.globe.rts.security.idl.sectypes.*;
import vu.globe.rts.security.idl.secconfig.*;

import vu.globe.svcs.objsvr.idl.resource.*;

import vu.globe.util.comm.idl.rawData;

import vu.globe.rts.std.idl.configure.*;
import vu.globe.rts.lr.idl.lrsub.*;

/**
 *  Skeleton class for a 'secure connector' object. A secure connector object
 *  implements the secConnector, lrSubObject interfaces.
 *  It also implements the secConnectionCB and listenCB interfaces,
 *  from the secure connection and the listener objects' callback respectively.
 *  It also implements the secMsgCB interface so we can have direct invocation
 *  on the security subobject. Only the master and slave will use this.
 */

public abstract class secConnectorSkel extends GObject 
                implements secConnector_Inf, lrSubObject_Inf, 
                           secConnectionCB_Inf, listenCB_Inf, 
                           secMsgCB_Inf, secCertManagement_Inf,
                           timerResourceManagerCB_Inf, secSetLRID_Inf {

    public secConnectorSkel()
    {
        super();
    }

    protected void cleanup() throws vu.globe.rts.std.idl.stdInf.SOIerrors
    {
        super.cleanup();
    }

    public void init(int ctx) throws vu.globe.rts.std.idl.stdInf.SOIerrors
    {
        super.init(ctx);

        // secConnector interface state

        secConnector secConnectorInf = new secConnector(soi);
        secConnectorInf.constructor_s = this;
        secConnectorInf.regSecConnectorCB_s = this;
        secConnectorInf.getChannel_s = this;
        secConnectorInf.forwardAllowed_s = this;
        secConnectorInf.reverseAllowed_s = this;
        secConnectorInf.isRevoked_s = this;

        addInf(secConnectorInf.infid, secConnectorInf);

        // lrSubObject interface state

        lrSubObject lrSubObjectInf = new lrSubObject(soi);
        lrSubObjectInf.setObjectHandle_s = this;
        lrSubObjectInf.setPerstID_s = this;
        lrSubObjectInf.prepareDestruction_s = this;
        lrSubObjectInf.preparePassivation_s = this;
        lrSubObjectInf.completePassivation_s = this;
        lrSubObjectInf.prepareActivation_s = this;
        lrSubObjectInf.completeActivation_s = this;
        lrSubObjectInf.preparePassivationCheckpoint_s = this;
        lrSubObjectInf.passivationCheckpoint_s = this;
        lrSubObjectInf.completePassivationCheckpoint_s = this;

        addInf(lrSubObjectInf.infid, lrSubObjectInf);

        
        // secConnectionCB interface state

        secConnectionCB secConnectionCBInf = new secConnectionCB(soi);
        secConnectionCBInf.connEstablished_s = this;
        secConnectionCBInf.connClosed_s = this;

        addInf(secConnectionCBInf.infid, secConnectionCBInf);

        
        // listenCB interface state
        
        listenCB listenCBInf = new listenCB(soi);
        listenCBInf.listenStopped_s = this;
        listenCBInf.connArrived_s = this;
        
        addInf(listenCB.infid, listenCBInf);

        
        // secMsgCB interface state
        
        secMsgCB secMsgCBInf = new secMsgCB(soi);
        secMsgCBInf.msgReceived_s = this;
        secMsgCBInf.receiveStopped_s = this;

        addInf(secMsgCBInf.infid, secMsgCBInf);

        
        // secCertManagement interface state
        secCertManagement secCertManagementInf = new secCertManagement(soi);
        secCertManagementInf.getClientsList_s = this;
        secCertManagementInf.updateCredentials_s = this;
        secCertManagementInf.revokeCertificate_s = this;

        addInf(secCertManagementInf.infid, secCertManagementInf);

        
        // timerResourceManagerCB interface state
        timerResourceManagerCB timerResourceManagerCBInf =
                                            new timerResourceManagerCB(soi);
        timerResourceManagerCBInf.run_s = this;
        timerResourceManagerCBInf.cancel_s = this;

        addInf(timerResourceManagerCBInf.infid, timerResourceManagerCBInf);

        // secSetLRID interface state
        secSetLRID secSetLRIDInf = new secSetLRID(soi);
        secSetLRIDInf.setLRID_s = this;

        addInf(secSetLRIDInf.infid, secSetLRIDInf);
    }

    
    // secConnector interface methods
    
    public abstract void constructor(String init, IDLLRMgrConfig CFG);

    public abstract contactAddresses regSecConnectorCB(secConnectorCB cb, 
                                               g.opaque user) throws secErrors;

    public abstract secConnection getChannel(int method) throws secErrors;

    public abstract short /* g.bool */ forwardAllowed(int channel, int method);

    public abstract short /* g.bool */ reverseAllowed(int channel, int method);
   
    public abstract short /* g.bool */ isRevoked(int channel);

    // lrSubObject interface methods
    public abstract void setObjectHandle(g.opaque objectHandle);
    
    public abstract void setPerstID(long pid) throws subObjectErrors;
    
    public abstract void prepareDestruction(short /* g.bool */ quick)
                        throws subObjectErrors;

    public abstract void preparePassivation(short /* g.bool */ quick)
                        throws subObjectErrors;

    public abstract rawData.rawDef completePassivation() throws subObjectErrors;

    public abstract void prepareActivation(long pid, rawData.rawDef state,
                                           short /* g.bool */ recoverMode)
                        throws subObjectErrors;

    public abstract void completeActivation() throws subObjectErrors;

    public abstract void preparePassivationCheckpoint()
                        throws subObjectErrors;

    public abstract rawData.rawDef passivationCheckpoint()
                        throws subObjectErrors;

    public abstract void completePassivationCheckpoint()
                        throws subObjectErrors;


    // secConnectionCB interface methods
    
    public abstract void connEstablished(int channel, IDLGenericCerts cert,
                                         g.opaque user) throws secErrors;

    public abstract void connClosed(int channel, short /* closeReason */ reason,
                                    g.opaque user);

    // listenCB interface methods

    public abstract void listenStopped(g.opaque user);

    public abstract void connArrived(g.opaque user);


    // secMsgCB interface methods
    public abstract void msgReceived(int channel, g.opaque user);

    public abstract void receiveStopped(int channel, g.opaque user);


    // secCertManagement interface methods
    public abstract IDLClientsList getClientsList() throws Exception;
    public abstract void updateCredentials(IDLSubjectName userName,
                                           IDLPubKeyHash userKeyHash,
                                           IDLAccessCtrlBitmap forwardACB,
                                           short /* credsOpMode */ mode)
                        throws secErrors;

    public abstract void revokeCertificate(long certSerialNum,
                                           short /* g.bool */ clientCert)
                        throws secErrors;

    // timerResourceManagerCB interface methods
    public abstract void run(long timer, g.opaque user) throws Exception;
    public abstract void cancel(long timer, g.opaque user) throws Exception;

    // secSetLRID interface methods
    public abstract void setLRID(long lrid);
}
