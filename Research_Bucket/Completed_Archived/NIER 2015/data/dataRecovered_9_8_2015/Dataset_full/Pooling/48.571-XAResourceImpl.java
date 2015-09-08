/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.ofbiz.minerva.pool.jdbc.xa.wrapper;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

/**
 * JTA resource implementation for JDBC 1.0 connections.  This is somewhat
 * limited in two respects.  First, it does not support two-phase commits since
 * JDBC 1.0 does not.  It will operate in the presence of two-phase commits, but
 * will throw heuristic exceptions if there is a failure during a commit or
 * rollback.  Second, it can only be associated with one transaction
 * at a time, and will throw exceptions if a second transaction tries to
 * attach before the first has called commit, rollback, or forget.
 * <P><FONT COLOR="RED"><B>Warning:</B></FONT></P> This implementation assumes
 * that forget will be called after a failed commit or rollback.  Otherwise,
 * the database connection will never be closed.</P>
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class XAResourceImpl implements XAResource {

    private Connection con;
    private XAConnectionImpl xaCon;
    private Xid current;
    private boolean active = false;
    private int timeout_ignored = 0;
    private Logger log = Logger.getLogger(XAResourceImpl.class);

    /**
     * Creates a new instance as the transactional resource for the specified
     * underlying connection.
     */
    public XAResourceImpl(Connection con) {
        this.con = con;
    }

    /**
     * Sets the XAConnection associated with this XAResource.  This is required,
     * but both classes cannot include an instance of the other in their
     * constructor!
     * @throws java.lang.IllegalStateException
     *    Occurs when this is called more than once.
     */
    void setXAConnection(XAConnectionImpl xaCon) {
        if (this.xaCon != null)
            throw new IllegalStateException();
        this.xaCon = xaCon;
    }

    public XAConnectionImpl getXAConnection() {
        return xaCon;
    }

    /**
     * Gets whether there is outstanding work on behalf of a Transaction.  If
     * there is not, then a connection that is closed will cause the
     * XAConnection to be closed or returned to a pool.  If there is, then the
     * XAConnection must be kept open until commit or rollback is called.
     */
    public boolean isTransaction() {
        return current != null;
    }

    /**
     * Closes this instance permanently.
     */
    public void close() {
        con = null;
        current = null;
        xaCon = null;
    }

    /**
     * Commits a transaction.
     * @throws XAException
     *     Occurs when the state was not correct (end never called), the
     *     transaction ID is wrong, the connection was set to Auto-Commit,
     *     or the commit on the underlying connection fails.  The error code
     *     differs depending on the exact situation.
     */
    public void commit(Xid id, boolean twoPhase) throws XAException {
        // System.out.println("commit: " + xaCon + ", current: " + current + ", xid: " + id + ", active: " + active);
        if (active && !twoPhase) // End was not called!
            System.err.println("WARNING: Connection not closed before transaction commit.\nConnection will not participate in any future transactions.\nAre you sure you want to be doing this?");
        if (current == null || !id.equals(current)) // wrong Xid
        {
            throwXAException(XAException.XAER_NOTA);
        }

        try {
            if (con.getAutoCommit()) {
                throwXAException(XAException.XA_HEURCOM);
            }
        } catch (SQLException e) {
            log.error(e);
        }

        try {
            con.commit();
        } catch (SQLException e) {
            log.error(e);
            try {
                con.rollback();
                if (!twoPhase) {
                    throwXAException(XAException.XA_RBROLLBACK);
                }
            } catch (SQLException e2) {
            }
            if (twoPhase) {
                throwXAException(XAException.XA_HEURRB); // no 2PC!
            } else {
                throwXAException(XAException.XA_RBOTHER); // no 2PC!
            }
            // Truly, neither committed nor rolled back.  Ouch!
        }
        current = null;
        if (active) {
            active = false; // No longer associated with the original transaction
        } else {
            xaCon.transactionFinished();  // No longer in use at all
        }
    }

    /**
     * Dissociates a resource from a global transaction.
     * @throws XAException
     *     Occurs when the state was not correct (end called twice), or the
     *     transaction ID is wrong.
     */
    public void end(Xid id, int flags) throws XAException {
        //System.out.println("end: " + xaCon + ", current: " + current + ", xid: " + id + ", active: " + active);
        if (!active) // End was called twice!
        {
            throwXAException(XAException.XAER_PROTO);
        }
        if (current == null || !id.equals(current)) {
            throwXAException(XAException.XAER_NOTA);
        }
        active = false;
    }

    /**
     * Indicates that no further action will be taken on behalf of this
     * transaction (after a heuristic failure).  It is assumed this will be
     * called after a failed commit or rollback.
     * @throws XAException
     *     Occurs when the state was not correct (end never called), or the
     *     transaction ID is wrong.
     */
    public void forget(Xid id) throws XAException {
        if (current == null || !id.equals(current)) {
            throwXAException(XAException.XAER_NOTA);
        }
        current = null;
        xaCon.transactionFailed();
        if (active) // End was not called!
            System.err.println("WARNING: Connection not closed before transaction forget.\nConnection will not participate in any future transactions.\nAre you sure you want to be doing this?");
    }

    /**
     * Gets the transaction timeout.
     */
    public int getTransactionTimeout() throws XAException {
        return timeout_ignored;
    }

    /**
     * Since the concept of resource managers does not really apply here (all
     * JDBC connections must be managed individually), indicates whether the
     * specified resource is the same as this one.
     */
    public boolean isSameRM(XAResource res) throws XAException {
        return res == this;
    }

    /**
     * Prepares a transaction to commit.  Since JDBC 1.0 does not support
     * 2-phase commits, this claims the commit is OK (so long as some work was
     * done on behalf of the specified transaction).
     * @throws XAException
     *     Occurs when the state was not correct (end never called), the
     *     transaction ID is wrong, or the connection was set to Auto-Commit.
     */
    public int prepare(Xid id) throws XAException {
        //System.out.println("prepare: " + xaCon + ", current: " + current + ", xid: " + id + ", active: " + active);
        if (active) // End was not called!
            System.err.println("WARNING: Connection not closed before transaction commit.\nConnection will not participate in any future transactions.\nAre you sure you want to be doing this?");
        if (current == null || !id.equals(current)) // wrong Xid
        {
            throwXAException(XAException.XAER_NOTA);
        }

        try {
            if (con.getAutoCommit()) {
                throwXAException(XAException.XA_HEURCOM);
            }
        } catch (SQLException e) {
            log.error(e);
        }

        return XA_OK;
    }

    /**
     * Returns all transaction IDs where work was done with no corresponding
     * commit, rollback, or forget.  Not really sure why this is useful in the
     * context of JDBC drivers.
     */
    public Xid[] recover(int flag) throws javax.transaction.xa.XAException {
        if (current == null)
            return new Xid[0];
        else
            return new Xid[]{current};
    }

    /**
     * Rolls back the work, assuming it was done on behalf of the specified
     * transaction.
     * @throws XAException
     *     Occurs when the state was not correct (end never called), the
     *     transaction ID is wrong, the connection was set to Auto-Commit,
     *     or the rollback on the underlying connection fails.  The error code
     *     differs depending on the exact situation.
     */
    public void rollback(Xid id) throws XAException {
        //System.out.println("rollback: " + xaCon + ", current: " + current + ", xid: " + id + ", active: " + active);
        if (active) // End was not called!
            log.error("WARNING: Connection not closed before transaction rollback. Connection will not participate in any future transactions. Are you sure you want to be doing this?");
        if (current == null || !id.equals(current)) { // wrong Xid
            throwXAException(XAException.XAER_NOTA);
        }
        try {
            if (con.getAutoCommit()) {
                throwXAException(XAException.XA_HEURCOM);
            }
        } catch (SQLException e) {
            log.error(e);
        }

        try {
            con.rollback();
        } catch (SQLException e) {
            log.error(e);
            throwXAException("Rollback failed: " + e.getMessage());
        }
        current = null;
        if (active) {
            active = false; // No longer associated with the original transaction
        } else {
            xaCon.transactionFinished(); // No longer in use at all
        }
    }

    /**
     * Sets the transaction timeout.  This is saved, but the value is not used
     * by the current implementation.
     */
    public boolean setTransactionTimeout(int timeout) throws XAException {
        timeout_ignored = timeout;
        return true;
    }

    /**
     * Associates a JDBC connection with a global transaction.  We assume that
     * end will be called followed by prepare, commit, or rollback.
     * If start is called after end but before commit or rollback, there is no
     * way to distinguish work done by different transactions on the same
     * connection).  If start is called more than once before
     * end, either it's a duplicate transaction ID or illegal transaction ID
     * (since you can't have two transactions associated with one DB
     * connection).
     * @throws XAException
     *     Occurs when the state was not correct (start called twice), the
     *     transaction ID is wrong, or the instance has already been closed.
     */
    public void start(Xid id, int flags) throws XAException {
        //System.out.println("start: " + xaCon + ", current: " + current + ", xid: " + id + ", active: " + active);
        if (active) {// Start was called twice!
            if (current != null && id.equals(current)) {
                throwXAException(XAException.XAER_DUPID);
            } else {
                throwXAException(XAException.XAER_PROTO);
            }
        }
        if (current != null && !id.equals(current)) {
            //System.out.println("current xid: " + current + ", new xid: " + id);
            throwXAException(XAException.XAER_NOTA);
        }
        if (con == null) {
            throwXAException(XAException.XA_RBOTHER);
        }
        current = id;
        active = true;
    }

    protected void throwXAException(int code) throws XAException {
        xaCon.setConnectionError(new SQLException("XAException occured with code: " + code));
        throw new XAException(code);
    }

    protected void throwXAException(String msg) throws XAException {
        xaCon.setConnectionError(new SQLException("XAException occured: " + msg));
        throw new XAException(msg);
    }
}
