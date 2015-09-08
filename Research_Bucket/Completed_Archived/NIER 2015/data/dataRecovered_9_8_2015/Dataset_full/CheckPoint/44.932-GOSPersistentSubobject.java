/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

/**
 * The Gaia equivalent of the lrSubobject interface.
 * This interface should (CHUTEPIERRE) be implemented
 * by Pure Java semantics subobjects that want to use
 * the persistence facilities of the Globe Object Server.
 *
 * For initialisation, methods are called in the following order by the LR manager:
 *
 * 1.  (creation of the subobject)
 * either:
 * 2a. setPerstID
 * or:
 * 2a prepareActivation
 * 2b. completeActivation
 *
 * For clean-up, methods are called in the following order by the LR manager:
 *
 * 1a. prepareDestruction or:
 * 1b. preparePassivation and completePassivation
 * 2.  (destruction of the subobject)
 *
 * TODO: simplify this interface
 *
 *@author: Arno
 */
package vu.gaia.lr.n;

public interface GOSPersistentSubobject
{
    public static final int invalidPerstID=-1;
    
    /**
     * If the LR is persistent, the LR's persistence id is passed. Otherwise
     * an invalidPerstID is passed. This call is not made on an activating LR.
     * @param pid LR's persistence ID.
     * @exception GOSPersistentSubobjectException
     */
    public void setPerstID( long pid )
        throws GOSPersistentSubobjectException;
    
    /**
     * Tells the subobject that the LR is about to be destroyed, and allows the
     * subobject to clean itself up. For example a replication object may want
     * to unbind from a DSO at this point. This call is followed by destruction
     * of the subobject.
     *
     * In the case of a persistent LR, this call is not made on a passivating
     * LR.
     *
     * @param quick  legacy, please ignore.
     * @exception GOSPersistentSubobjectException
     */
    public void prepareDestruction( boolean quick )
        throws GOSPersistentSubobjectException;
    
    
    /**
     * Tells the subobject that the persistent LR is being passivated, and
     * allows the subobject to synchronise with other subobjects, and to perform
     * clean-up operations. For example a replication object may want to unbind
     * from a DSO at this point. After this call the subobject should no longer
     * allow operations on its persistent state. This call is followed by
     * 'completePassivation'.
     * @param quick  legacy, please ignore.
     * @exception GOSPersistentSubobjectException
     */
    public void preparePassivation( boolean quick )
        throws GOSPersistentSubobjectException;
    
    /**
     * Tells the subobject to finish passivating, and requests its persistent
     * state. This method is only invoked after 'preparePassivation'.
     * @exception GOSPersistentSubobjectException
     */
    public byte[] completePassivation()
        throws GOSPersistentSubobjectException;
    
    /**
     * Tells the subobject that the persistent LR is being activated, and passes
     * it its persistent state and the pid of the LR. The subobject should not
     * allow operations on the persistent state yet. This call is followed by
     * 'completeActivation'. If `recoverMode' is set, the subobject is being
     * activated in recover mode (=recoverting from a crash, not a graceful
     * shutdown).
     * @param pid PID of the LR
     * @param state   marshalled state of the semantics subobject
     * @param recoverMode whether in recover mode.
     * @exception GOSPersistentSubobjectException
     */
    public void prepareActivation( long pid, byte[] state, boolean recoverMode)
        throws GOSPersistentSubobjectException;
    
    /**
     * Allows the subobject to synchronise with other subobjects, and to complete
     * initialisation. For example a replication object may want to contact the
     * DSO at this point. After this call the subobject may allow operations on
     * its persistent state. This method is only invoked after
     * 'prepareActivation'.
     * @exception GOSPersistentSubobjectException
     */
    public void completeActivation()
        throws GOSPersistentSubobjectException;
    
    
    /**
     * Tells the subobject that the `passivation state' of the persistent LR
     * is about to be retrieved as part of a `passivation checkpoint'. The
     * passivation state is the state that the LR would normally save when it
     * is being passivated. During a passivation checkpoint, a snapshot of
     * the entire passivation state of the object server is made and saved
     * to disk. As this snapshot should be consistent and complete, the
     * subobject should stop or delay all operations that modify its
     * passivation state when this method returns.
     *
     * At this point, a replication object for example, should disable all
     * network activity.
     *
     * This method is `atomic'. There are no partial failures. If an error
     * occurs, a roll back to the original situation is made before the
     * error is passed on.
     *
     * A call to this method is eventually followed by a call to
     * `completePassivationCheckpoint()'. Between these calls a call to
     * `passivationCheckpoint()' can be made.
     * @exception GOSPersistentSubobjectException
     */
    public void preparePassivationCheckpoint()
     throws GOSPersistentSubobjectException;
    
    /**
     * Tells the subobject of a persistent LR that it should return its
     * `passivation state'. The passivation state is the subobject's state
     * that would normally be saved when the LR is being passivated.
     *
     * This method is only invoked after `preparePassivationCheckpoint()';
     * it is followed by a call to `completePassivationCheckpoint()'.
     * @return the marshalled state of the (semantics) subobject.
     * @exception GOSPersistentSubobjectException
     */
    public byte[] passivationCheckpoint()
        throws GOSPersistentSubobjectException;
    
    
    /**
     * Tells the subobject of a persistent LR that it should return to its
     * normal state and resume its operations. This method is the complement
     * of `preparePassivaitonCheckpoint()'. From the subobject's point of
     * view, the passivation checkpoint is now finished.
     *
     * At this point, a replication object for example, should re-enable
     * network activity (which it had disabled during
     * `preparePassivationCheckpoint()').
     *
     * This method is invoked after a call to `preparePassivationCheckpoint()'
     * or `passivationCheckpoint()' (which is only invoked after a call to
     * `preparePassivationCheckpoint()').
     */
    public void completePassivationCheckpoint()
        throws GOSPersistentSubobjectException;
}
