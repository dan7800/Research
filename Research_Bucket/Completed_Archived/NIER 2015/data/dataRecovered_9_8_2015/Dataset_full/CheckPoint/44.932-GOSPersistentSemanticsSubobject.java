/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

/**
 * The Gaia equivalent of the lrSubobject interface, simplified
 * for semantics subobjects. This interface should be implemented
 * by Pure Java semantics subobjects that want to use
 * the persistence facilities of the Globe Object Server.
 *
 * When an LR is first created the LR manager calls 
 * <code>setPerstID</code>.
 *
 * When this LR is removed from the object server, the LR manager
 * calls <code>prepareDestruction</code>. The subobject should 
 * now remove its persistent state from storage.
 *
 * When an LR is passivated, i.e., when an object server is doing
 * a graceful shutdown, the LR manager calls 
 * <code>getIncoreState</code> before destroying the semantics 
 * subobject. The semantics subobject returns its in-core state, 
 * that is, the part of its state not stored on persistent 
 * storage.  Note that passivation of an LR is different from 
 * removing an LR. Although in both cases the current LR instance
 * is destroyed, in the passivation case, the LR will be recreated
 * when the object server starts again. Hence, the subobject
 * should not delete any of its persistent state in its 
 * <code>finalize</code> method!
 *
 * <code>getIncoreState</code> is also called periodically as 
 * part of the object server's checkpointing mechanism.
 *
 * When an LR is reactivated after either a graceful restart or
 * after a crash, the LR manager calls <code>setPerstID</code> 
 * and <code>setIncoreState</code>. The latter method has an 
 * argument that indicates whether we are recovering from a crash
 * or a reboot. In the former case, the semantics subobject may
 * want to check that its persistent state is still intact.
 *
 */
package vu.gaia.lr.n.sem;

import vu.gaia.lr.n.GOSPersistentSubobjectException;

public interface GOSPersistentSemanticsSubobject
{
   public static final int invalidPerstID=-1; 
    
    /**
     * Tells the subobject the persistence id of the LR.
     *
     * @param pid The persistence ID. 
     * @exception GOSPersistentSubobjectException
     */
    public void setPerstID( long pid )
        throws GOSPersistentSubobjectException;
  
   /**
    * Tells the subobject that the LR is about to be destroyed, 
    * and allows the subobject to clean itself up. A semantics
    * subobject should now delete its persistent state from
    * storage. This call is followed by the destruction of the
    * subobject.
    *
    * Note that this call is not made when a persistent LR is
    * passivated.
    * 
    * @exception GOSPersistentSubobjectException
    */
    public void prepareDestruction()
        throws GOSPersistentSubobjectException;

    /**
     * Requests the subobject's in-core state.
     * 
     * @exception GOSPersistentSubobjectException
     */
    public byte[] getIncoreState()
        throws GOSPersistentSubobjectException;
    
    /**
     * Tells the subobject that the persistent LR is being
     * activated, and passes it its marshalled in-core state.  
     * If `recoverMode' is set, the subobject is being activated
     * in recover mode (=recoverting from a crash, not a graceful
     * shutdown).
     * 
     * @param state   marshalled state of the semantics subobject
     * @param recoverMode whether in recover mode.
     * @exception GOSPersistentSubobjectException
     */
    public void setIncoreState( byte[] state, boolean recoverMode)
        throws GOSPersistentSubobjectException;
}
