/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.objsvr.skels;

import vu.globe.rts.java.*;
import vu.globe.idlsys.g;
import vu.globe.rts.std.idl.stdInf.*;

import vu.globe.svcs.objsvr.idl.persistence.*; // persistence.idl
import vu.globe.rts.std.idl.configure.*;      // configure.idl

import vu.globe.util.comm.idl.rawData.*;

/**
   Skeleton for a persistent object. This skeleton only defines the interfaces
   related to persistence. It can be extended by, or serve as an example to,
   more specific skeletons, which implement other interfaces as well.
*/

public abstract class PerstObjectSkel extends GObject implements
	persistentObject_Inf, managedPerstObject_Inf, configurable_Inf
{
   public PerstObjectSkel()
   {
      super();
   }

   protected void cleanup() throws vu.globe.rts.std.idl.stdInf.SOIerrors
   {
      super.cleanup();
   }

   public void init (int ctx) throws vu.globe.rts.std.idl.stdInf.SOIerrors
   {
      super.init (ctx);

      // persistentObject interface
      persistentObject persistentObjectInf = new persistentObject(soi);
      persistentObjectInf.getPersistenceID_s = this;
      addInf(persistentObjectInf.infid, persistentObjectInf);

      // managedPerstObject interface
      managedPerstObject managedPerstObjectInf = new managedPerstObject(soi);
      managedPerstObjectInf.bePersistent_s = this;
      managedPerstObjectInf.beTransient_s = this;
      managedPerstObjectInf.prepareImmediatePassivation_s = this;
      managedPerstObjectInf.activate_s = this;
      managedPerstObjectInf.activateFromCrash_s = this;
      managedPerstObjectInf.preparePassivationCheckpoint_s = this;
      managedPerstObjectInf.passivationCheckpoint_s = this;
      managedPerstObjectInf.completePassivationCheckpoint_s = this;
      addInf(managedPerstObjectInf.infid, managedPerstObjectInf);

      // configurable interface
      configurable configurableInf = new configurable(soi);
      configurableInf.configure_s = this;
      addInf(configurableInf.infid, configurableInf);
   }

   // persistentObject interface
   public abstract long getPersistenceID() throws Exception;

   // managedPerstObject interface
   public abstract void bePersistent(long pid) throws Exception;
   public abstract void beTransient() throws Exception;
   public abstract void prepareImmediatePassivation() throws Exception;
   public abstract void activate(long pid) throws Exception;
   public abstract void activateFromCrash(long pid, rawDef state)
     throws Exception;
   public abstract void preparePassivationCheckpoint() throws Exception;
   public abstract passivationState passivationCheckpoint() throws Exception;
   public abstract void completePassivationCheckpoint() throws Exception;

   // configurable interface
   public abstract void configure(String data) throws Exception;
}
