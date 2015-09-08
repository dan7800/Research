/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.rts.lr.replication.skels;

import vu.globe.rts.java.*;
import vu.globe.idlsys.g;
import vu.globe.rts.std.idl.data;
import vu.globe.rts.lr.idl.repl.*;
import vu.globe.rts.lr.idl.ctrl.*;

import vu.globe.util.comm.idl.rawData;

import vu.globe.rts.std.idl.configure.*;
import vu.globe.rts.lr.idl.distribution.*;
import vu.globe.rts.lr.idl.lrsub.*;
import vu.globe.rts.lr.idl.repl.*;
import vu.globe.svcs.gls.idl.lsClient.*;

import vu.globe.rts.security.idl.security.*;

/**
 * Replication object skeleton.
 *
 * Installed the secConnectorCB inf.
 *
 * Edited by: W.R. Dittmer
*/

public abstract class MinimalReplSkel extends GObject implements 
                                replInit_Inf, replication_Inf, replDistr_Inf, 
                                lrSubObject_Inf, configurable_Inf, 
                                secConnectorCB_Inf, 
                                secMsgCB_Inf {

  protected void cleanup() throws vu.globe.rts.std.idl.stdInf.SOIerrors
  {
    super.cleanup();
  }

  public void init(int ctx) throws vu.globe.rts.std.idl.stdInf.SOIerrors
  {
    super.init(ctx);

    replication replicationInf = new replication(soi);
    replicationInf.start_s = this;
    replicationInf.send_s = this;
    replicationInf.invoked_s = this;
    replicationInf.finish_s = this;
    replicationInf.secViolation_s = this;

    addInf(replicationInf.infid, replicationInf);

    replInit replInitInf = new replInit(soi);
    replInitInf.installCallback_s = this;
    addInf(replInitInf.infid, replInitInf);

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

    configurable configurableInf = new configurable(soi);
    configurableInf.configure_s = this;

    addInf(configurableInf.infid, configurableInf);

    
    replDistr replDistrInf = new replDistr(soi);
    replDistrInf.distributeObject_s = this;
    replDistrInf.getLSMTime_s = this;

    addInf(replDistrInf.infid, replDistrInf);

    secConnectorCB secConnectorCBInf = new secConnectorCB(soi);
    secConnectorCBInf.incomingConn_s = this;
    secConnectorCBInf.listenStopped_s = this;

    addInf(secConnectorCBInf.infid, secConnectorCBInf);

    secMsgCB secMsgCBInf = new secMsgCB(soi);
    secMsgCBInf.msgReceived_s = this;
    secMsgCBInf.receiveStopped_s = this;

    addInf(secMsgCBInf.infid, secMsgCBInf);
  }

  // replication interface
  public abstract short start(short op) throws replErrors;

  public abstract replication_send_Out send(short op, rawData.rawDef req,
                                           int reqMethod) throws replErrors;

  public abstract short invoked(short op) throws replErrors;

  public abstract short finish(int channel);

  public abstract short secViolation(int channel);


  // replInit interface
  public abstract void installCallback(replCB cb);


  // lrSubObject interface
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

  // configure interface
  public abstract void configure(String init);
  
  // replDistr interface
  public abstract contactAddresses distributeObject()
    throws replDistrErrors;
  
  public abstract long getLSMTime() 
    throws replDistrErrors;

  // SecConnectorCB interface
  public abstract void incomingConn(secConnection conn, g.opaque user) 
    throws secErrors;

  public abstract void listenStopped(g.opaque user);

  // secMsgCB interface methods
  public abstract void msgReceived(int channel, g.opaque user);

  public abstract void receiveStopped(int channel, g.opaque user);
}
