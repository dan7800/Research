/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// PassivationCheckpointData.java

package vu.globe.svcs.objsvr.perstm;


import java.util.ArrayList;


/**
 * Container class for passivation checkpoint data. This data consists of
 * the passivation state of an object server, and the checkpoint status of
 * the persistent objects. The passivation state of an object server
 * consists of the state of the persistence manager and the passivation
 * state of the persistent objects.
 */
public class PassivationCheckpointData
{
  // Object server's passivation state.
  ObjSvrPassivationState gosPassivationState;

  // Status of each persistent object.
  PassivationCheckpointStatus posStatus[];


  /**
   * Instance creation.
   *
   * @param  gosPassivationState  object server's passivation state
   * @param  posStatus            array holding the status of the
   *                              persistent objects
   */
  public PassivationCheckpointData(ObjSvrPassivationState gosPassivationState,
                                   PassivationCheckpointStatus posStatus[])
  {
    this.gosPassivationState = gosPassivationState;
    this.posStatus = posStatus;
  }


  /**
   * Instance creation.
   *
   * @param  gosPassivationState  object server's passivation state
   * @param  posStatus            array holding the status of the
   *                              persistent objects
   */
  public PassivationCheckpointData(ObjSvrPassivationState gosPassivationState,
                                   ArrayList posStatus)
  {
    this.gosPassivationState = gosPassivationState;
    this.posStatus = new PassivationCheckpointStatus[posStatus.size()];
    posStatus.toArray(this.posStatus);
  }
}
