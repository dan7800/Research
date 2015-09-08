/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// PassivationCheckpointStatus.java

package vu.globe.svcs.objsvr.perstm;

 
import vu.globe.svcs.objsvr.types.ResourceIdent;


/**
 * This class represents the checkpoint status of a persistent object.
 * During a check point, a persistent object goes through three stages:
 * 1) preparing the checkpoint, 2) performing the checkpoint (i.e.,
 * collecting passivation state), and 3) completing the checkpoint.
 */
public class PassivationCheckpointStatus
{
  // Bitmasks for the status field.
  private static final int STATUS_NULL      = 0;       // null status
  private static final int STATUS_PREPARED  = 1;       // stage 1 OK
  private static final int STATUS_PERFORMED = 2;       // stage 2 OK
  private static final int STATUS_COMPLETED = 4;       // stage 3 OK

  private int _status;                 // status field
  private ResourceIdent _rid;          // persistent object's resource id


  /**
   * Instance creation.
   *
   * @param  rid  the persistent object's resource id
   */
  public PassivationCheckpointStatus(ResourceIdent rid)
  {
    _rid = rid;
    _status = STATUS_NULL;
  }


  /**
   * Return the persistent object's resource id.
   */
  public ResourceIdent getResourceIdent()
  {
    return _rid;
  }


  /**
   * Set the status of the `prepared for passivation checkpoint' stage.
   *
   * @param  on  if set, the persistent object has successfully prepared
   *             for the passivation checkpoint
   */
  public void setPrepared(boolean on)
  {
    if (on) {
      _status |= STATUS_PREPARED;
    }
    else {
      _status &= ~STATUS_PREPARED;
    }
  }


  /**
   * Return <code>true</code> if the persistent object has successfully
   * prepared for the passivastion checkpoint.
   */
  public boolean hasPrepared()
  {
    return (STATUS_PREPARED == (_status & STATUS_PREPARED));
  }


  /**
   * Set the status of the `perform passivation checkpoint' stage.
   *
   * @param  on  if set, the persistent object has successfully performed
   *             the passivation checkpoint
   */
  public void setPerformed(boolean on)
  {
    if (on) {
      _status |= STATUS_PERFORMED;
    }
    else {
      _status &= ~STATUS_PERFORMED;
    }
  }


  /**
   * Return <code>true</code> if the persistent object has successfully
   * performed the passivastion checkpoint, i.e. collecting its passivation
   * state.
   */
  public boolean hasPerformed()
  {
    return (STATUS_PERFORMED == (_status & STATUS_PERFORMED));
  }


  /**
   * Set the status of the `complete passivation checkpoint' stage.
   *
   * @param  on  if set, the persistent object has successfully completed
   *             the passivation checkpoint
   */
  public void setCompleted(boolean on)
  {
    if (on) {
      _status |= STATUS_COMPLETED;
    }
    else {
      _status &= ~STATUS_COMPLETED;
    }
  }


  /**
   * Return <code>true</code> if the persistent object has successfully
   * completed the passivastion checkpoint.
   */
  public boolean hasCompleted()
  {
    return (STATUS_COMPLETED == (_status & STATUS_COMPLETED));
  }
}
