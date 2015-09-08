/*
 * XAPool: Open Source XA JDBC Pool
 * Copyright (C) 2003 Objectweb.org
 * Initial Developer: Lutris Technologies Inc.
 * Contact: xapool-public@lists.debian-sf.objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 */
 package org.enhydra.jdbc.standard;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.transaction.Status;
import javax.transaction.TransactionManager;

/**
 * Provides a generic wrapper for JDBC 1 drivers. JDBC 1 drivers always
 * associate a single transaction at every point in time with a physical
 * connection. J2EE drivers, on the other hand, allow an XAResource (and
 * therefore an XAConnection with which it has a one to one mapping) to
 * switch between global transactions.
 * <P>
 * To accomodate this, the StandardXADataSource class maintains a list of
 * Connection objects. When the Transaction Manager associates an XID
 * with a StandardXAConnection, it looks for a physical connection which
 * is associated with that transaction.
 * <P>
 * The "current" connection (super.con and curCon) is the connection
 * currently being used by the application (i.e. getConnection has
 * been called, but not Connection.close()). The current connection
 * is removed and handed to the data source if it becomes associated
 * with a global transaction.
 */
public class StandardXAConnection
	extends StandardPooledConnection
	implements XAConnection, XAResource, Referenceable, Runnable {

	protected StandardXAStatefulConnection curCon;
	// the "current" stateful connection, null if none
	private boolean commitOnPrepare;
	// true if commit takes place during prepare call
	boolean isClosed; // true if this connection has been closed
	private int timeoutSecs; // timeout in seconds
	private long timeoutPeriod = 60000; // interval in ms to check for timeouts
	private long nextTimeout; // time when next timeout occurs
	public Thread timerThread; // the thread that checks for timeouts
	public TransactionManager transactionManager;
	public StandardXAConnectionHandle connectionHandle;
	protected StandardXADataSource xaDataSource;
	public boolean thisAutoCommit = true;

	/**
	 * Creates the first free connection.
	 */
	public StandardXAConnection(
		StandardXADataSource dataSource,
		String user,
		String password)
		throws SQLException {
		super(dataSource, user, password);
		// creates the first Connection object

		// Save the constructor parameters.
		this.xaDataSource = dataSource;
		curCon = new StandardXAStatefulConnection(dataSource, con);
		// wrap connection as a stateful connection

		// NOTE - the current connection is not made known to the data source
		// so it is not eligible for re-use. It only goes on the data source list
		// if it ever becomes associated with a global transaction.

		/*
		// get the timer thread
		if (xaDataSource.getThreadFactory() != null) {
		dataSource.log.debug("StandardXAConnection: Getting thread from factory");
		timerThread = xaDataSource.getThreadFactory().getThread(this);
		dataSource.log.debug("StandardXAConnection: Got thread from factory");
		} else {
		dataSource.log.debug("StandardXAConnection: Getting thread from new Thread()");
		timerThread = new Thread (this);	// create the backgroup thread to check for timeouts
		}
		
		timerThread.start();			// start the timer thread
		//timerThread.suspend();			// and suspend until some timeouts get set up
		*/
		dataSource.log.debug("StandardXAConnection created");
	}

	/**
	 * We are required to maintain a 1-1 mapping between an XAConnection
	 * and its corresponding XAResource. We achieve this by implementing
	 * both interfaces in the same class.
	 */
	public XAResource getXAResource() {
		return this;
	}

	/**
	 * Creates a new StandardXAConnectionHandle for use by an application.
	 * If there is already an StandardXAConnectionHandle in use then it is
	 * closed (i.e. the application has the connection withdrawn).
	 * <P>
	 * This method always returns a Connection in the free state (i.e.
	 * (not associated with an Xid). This is necessary since, unless
	 * Start (Xid, flags) gets called, the Connection must do local
	 * transaction processing.
	 */
	public synchronized Connection getConnection() throws SQLException {
		dataSource.log.debug("StandardXAConnection:getConnection");
		if (connectionHandle != null) {
			// if there's already a delegated connection
			if (!connectionHandle.isClosed()) // and it hasn't been closed
				connectionHandle.close(); // close it now
		}
		if (curCon == null) { // if there's no current connection
                    
			curCon = xaDataSource.getFreeConnection();
                        // find or create a free connection
                        con = curCon.con; // save it's Connection
		}

		// Note that we share the PreparedStatement cache across many physical
		// connections. This is OK since the connection is used in the lookup key.

		this.newConnectionHandle();
		dataSource.log.debug(
			"StandardXAConnection:getConnection return a connection");
		return connectionHandle;
	}

	protected void newConnectionHandle() {
		connectionHandle =
			new StandardXAConnectionHandle(
				this,
				dataSource.getMasterPrepStmtCache(),
				dataSource.getPreparedStmtCacheSize(),
				transactionManager);
	}

	public void setTransactionManager(TransactionManager tm) {
		this.transactionManager = tm;
	}

	/**
	 * Close this XA connection.
	 */
	public synchronized void close() throws java.sql.SQLException {
		dataSource.log.debug("StandardXAConnection:close the XAConnection");
 //                if (con != null) { // if we have a current connection
//                        con.close(); // then close it
//                        dataSource.getMasterPrepStmtCache().remove(con.toString());
//                 }
                //commenented by karthicks - in case of transacted connection curcon will be null and physical connection con will not be null
                //and physical will be part of freeconnection which would be used by some other instance of XAConnection object at this instant
                //so only the curCon and its is associated connected should be closed 
                //it will happen fro non transacted connections.
                //in case of tx connectioins else part will come to play and close any freeconnections
		if (curCon != null && !curCon.con.isClosed()) 
                { // if we have a current connection
			curCon.con.close(); // then close it
			dataSource.getMasterPrepStmtCache().remove(curCon.toString());
		} 
                else { // no "current" connection
			if (xaDataSource.freeConnections.size() > 1) {
				// if there are some free connections
				//curCon.con.setAutoCommit(thisAutoCommit);
				curCon = xaDataSource.getFreeConnection();
				// get one of the free connections
				curCon.con.close(); // close it
				dataSource.getMasterPrepStmtCache().remove(
					curCon.con.toString());
			}
		}
		curCon = null; // remove stateful connection
		con = null; // and physical connection
		xaDataSource.connectionClosed();
		// tell data source that connection's gone
		
		isClosed = true; // connection is now closed
		connectionHandle = null;
		//timerThread.resume();					// stop the timeout checking
		nextTimeout = 0; // I don't know how the above line was
		// supposed to stop the thread but we
		// should really just set the condition
		// and let the thread stop itself
	}

	/**
	 * Does most of the work of the start() call (below). Kept as
	 * a separate method so that subclasses can call it and retain
	 * the curCon property.
	 */
	public synchronized void doStart(Xid xid, int flags) throws XAException {
		dataSource.log.debug(
			"StandardXAConnection:doStart xid='"
				+ xid
				+ "' flags='"
				+ flags
				+ "'");
		if (xid == null)
			throw new XAException(XAException.XAER_INVAL);

		// should only get called after a new/free connection has been made current
                /* commented by karthick 
		if (curCon == null) {
			try {
				curCon = xaDataSource.getFreeConnection();
			} catch (Exception e) {
			}
			dataSource.log.debug("StandardXAConnection:doStart curCon is null");
			//throw new XAException (XAException.XAER_PROTO);
		}
                */

		/*
		if ((curCon.getState() != Status.STATUS_NO_TRANSACTION) && (curCon.xid != xid )){
		    dataSource.log.error("StandardXAConnection:doStart Invalid state:status="
		            + curCon.getState() + ":id=" + curCon.id);
		    throw new XAException (XAException.XAER_PROTO);
		}
		*/

		if (flags == TMRESUME
			|| flags == TMJOIN) {
			// if resuming or joining an existing transaction
			try {
				xaDataSource.processToWait();
			} catch (Exception e) {
				throw new XAException("Exception : " + e.toString());
			}
			synchronized (xaDataSource) {
                            if(curCon != null)
                            {
                                //free connections will be added if this is a new XAConnection object or previously an non transacted connection
                                // so no need to check fori contains any as a precaution
                                //commented by karthicks
                                if(!xaDataSource.freeConnections.contains(curCon))
                                {
                                    xaDataSource.freeConnections.addElement(curCon);
                                }
                            }
                            // save the current connection
			}
			curCon = xaDataSource.getConnection(xid, true);
			// must find connection handling xid
			con = curCon.con; // must use correct physical connection
		} // else {
// 			xaDataSource.getConnection(xid, false);
// 			// must NOT find connection handling xid
// 		}
                //commented by karthicks -unnecessary fetch

                //moved by karthicks
                // should only get called after a new/free which has been called in different tx earlier 
                if (curCon == null) {
                    try {
                        curCon = xaDataSource.getFreeConnection();
                        con = curCon.con;
                    } catch (Exception e) {
                        dataSource.log.error("error while gettting connection "+e,e);
                    }
                    dataSource.log.debug("StandardXAConnection:doStart curCon is null");
                    //throw new XAException (XAException.XAER_PROTO);
		}


                //on suspend all enlisted resource will get called so resetonresume will be set "true" to those StdxaconnHandle
                //on resume viceversa(deleisted resource ) will get enlisted again so start will get called and current tx will be reset
                //by -  karthicks
		StandardXAConnectionHandle xad = connectionHandle;
		try {
			xad.setGlobalTransaction(true);
                        if(flags == TMRESUME && xad.resetTxonResume)
                        {
                            xad.resetTxonResume = false;
                            if(transactionManager != null &&  xad.tx == null)
                            {
                                try
                                {
                                    connectionHandle.tx =transactionManager.getTransaction();
                                }
                                catch(javax.transaction.SystemException se)
                                {
                                    throw new XAException(se.toString());
                                }
                            }
                        }

			// delegate must use current physical connection
		} catch (SQLException e) {
			throw new XAException(e.toString());
		}

		if (timeoutSecs != 0) { // if a timeout has been defined
				curCon.timeout = System.currentTimeMillis() // set the timeout
	+timeoutSecs * 1000;
			if (nextTimeout == 0) {
				// if there are currently no timeouts set up
				nextTimeout = curCon.timeout; // set new timeout
				notify();
				//timerThread.resume();				// start checking for timeouts
			} else { // some timeouts already exist
				if (curCon.timeout < nextTimeout) {
					// if this expires sooner than next timeout
					nextTimeout = curCon.timeout; // set new timeout
				}
			}
		}

		curCon.xid = xid; // connection now associated with this XID
		curCon.timedOut = false; // forget about any old timeouts
		curCon.commitOnPrepare = commitOnPrepare;
		// tell it when to do a commit
		if (!xaDataSource.xidConnections.containsKey(xid)) {
			try {
				log.debug("StandardXAConnection:dostart before processToWait");
				xaDataSource.processToWait();
				log.debug("StandardXAConnection:dostart after processToWait");
			} catch (Exception e) {
				throw new XAException("Exception : " + e.toString());
			}
			synchronized (xaDataSource) {
				xaDataSource.xidConnections.put(xid, curCon);
				// place on allocated list
			}
		}
		curCon.setState(Status.STATUS_ACTIVE); // set new connection state
	}

	/**
	 * Associates this XAConnection with a global transaction. This
	 * is the only method which can associate the current connection
	 * with a global transaction. It acts only on the current
	 * connection which must have been previously established using
	 * getConnection.
	 */
	public synchronized void start(Xid xid, int flags) throws XAException {
		dataSource.log.debug(
			"StandardXAConnection:start associate the current connection with a global transaction");
		doStart(xid, flags); // do most of the work
                 curCon = null; // no longer owned by this object
		//con = null;						// ditto
	}

	/**
	 * Ends a connection's association with a global transaction.
	 * <P>
	 * It need not act on the current transaction. There is an
	 * interval between being returned to the pool manager and
	 * being invoked by the transaction manager during which the
	 * current connection can change.
	 * <P>
	 * Note that the only effect is to change the connection state.
	 */
	public synchronized void end(Xid xid, int flags)
		throws XAException { //not tested XS
		dataSource.log.debug("StandardXAConnection:end");
		dataSource.log.debug(
			"StandardXAConnection:end xid='" + xid + "' flags='" + flags + "'");

		if (xid == null)
			throw new XAException(XAException.XAER_INVAL);
		StandardXAStatefulConnection statecon =
			xaDataSource.getConnection(xid, true);
		// must find connection for this transaction
		int state = statecon.getState(); // get current state of connection
		if (state != Status.STATUS_ACTIVE) // must have had start() called
			throw new XAException(XAException.XAER_PROTO);
		/*System.out.println("connectionHandle.globalTransaction = false;\n"+
		        "connectionHandle.setAutoCommit(true);");
		connectionHandle.globalTransaction = false;
		try {
		    connectionHandle.setAutoCommit(true);
		} catch (SQLException sqle) {
		    dataSource.log("StandardXAConnection pb: "+sqle);
		}*/
		//        try {

                //on suspend all end of enlisted resource will get called so resetonresume will be set "true" to those StdxaconnHandle
                //on resume viceversa(deleisted resource ) will get enlisted again so start will get called and current tx will be reset
                //by -  karthicks
                if(connectionHandle.tx != null)
                {
                    connectionHandle.resetTxonResume =true;
                }
		connectionHandle.tx = null;
		connectionHandle.globalTransaction = false;

		/*          connectionHandle.setGlobalTransaction(false);
		      } catch (SQLException sqle) {
		          dataSource.log.error("StandardXAConnection:end pb "+sqle);
		      }
		*/
	}

	/**
	 * Does most of the work of a generic prepare. Kept as a
	 * separate method so that sub-classes can call it and get
	 * the StandardXAStatefulConnection back.
	 */
	public StandardXAStatefulConnection checkPreparedState(Xid xid)
		throws XAException {
		dataSource.log.debug("StandardXAConnection:checkPreparedState");
		if (xid == null)
			throw new XAException(XAException.XAER_INVAL);
		StandardXAStatefulConnection statecon =
			xaDataSource.getConnection(xid, true);
		// must find connection for this transaction

		try {
			if (statecon.commitOnPrepare) { // if early commit is required
				statecon.con.commit(); // perform the commit operation now
				statecon.setState(Status.STATUS_PREPARING);
				// heuristaclly committed
			} else {
				statecon.setState(Status.STATUS_PREPARED); // prepared
			}
		} catch (SQLException e) {
			dataSource.log.error(
				"StandardXAConnection:checkPrepareState Exception on prepare, rolling back");
			statecon.setState(Status.STATUS_NO_TRANSACTION);
			// release connection
			throw new XAException(XAException.XA_RBROLLBACK);
			// rollback will have been performed
		}

		return statecon;
	}

	/**
	 * Prepares to perform a commit. May actually perform a commit
	 * in the flag commitOnPrepare is set to true.
	 */
	public int prepare(Xid xid) throws XAException {
		dataSource.log.debug(
			"StandardXAConnection:prepare prepare to perform a commit");
		checkPreparedState(xid);
		return XA_OK;
	}

	/**
	 * Performs a commit on this resource manager's branch of
	 * the global transaction.
	 */
	public synchronized void commit(Xid xid, boolean onePhase)
		throws XAException {
		dataSource.log.debug("StandardXAConnection:commit perform a commit");
		if (xid == null)
			throw new XAException(XAException.XAER_INVAL);

		StandardXAStatefulConnection statecon =
			xaDataSource.getConnection(xid, true);
		// must find connection for this transaction
		dataSource.log.debug("StandardXAConnection:commit case(state)");

		try {
			switch (statecon.getState()) { // action depends on current state
				case Status.STATUS_PREPARING : // already commited
					break; // ...so do nothing
				case Status.STATUS_PREPARED : // ready to do commit
					try {
						dataSource.log.debug(
							"StandardXAConnection:commit try to commit a connection (STATUS_PREPARED)");
						statecon.con.commit();
						// perform the commit operation now
						dataSource.log.debug(
							"StandardXAConnection:commit commit is ok");
					} catch (SQLException e) {
						throw new XAException(XAException.XA_RBROLLBACK);
						// rollback will have been performed
					}
					break;
				case Status.STATUS_COMMITTED : // could be a 1-phase commit
				case Status.STATUS_ACTIVE :
					if (!onePhase) { // if not a one-phase commit
						throw new XAException(XAException.XAER_PROTO);
					}

					try {
						dataSource.log.debug(
							"StandardXAConnection:commit try to commit a connection (STATUS_ACTIVE)");
						statecon.con.commit();
						// perform the commit operation now
						dataSource.log.debug(
							"StandardXAConnection:commit commit is ok");
					} catch (SQLException e) {
                                            throw new XAException(XAException.XA_RBROLLBACK);
						// rollback will have been performed
					}
					break;
				default :
					{
						dataSource.log.debug(
							"StandardXAConnection:commit UNKNOWN STATUS!:"
								+ statecon.getState());
						throw new XAException(XAException.XAER_PROTO);
					}
			}
		} catch (XAException e) {
			throw e;
		} finally {
			try {
				dataSource.log.debug(
					"StandardXAConnection:commit setAutoCommit to '"
						+ thisAutoCommit
						+ "'");
				statecon.con.setAutoCommit(thisAutoCommit);
			} catch (SQLException e) {
				dataSource.log.debug(
					"StandardXAConnection:commit setAutoCommit problem");
			}
                        
			xaDataSource.freeConnection(xid, false);
		}
	}

	/**
	 * PERFORMS a rollback on this resource manager's branch of
	 * the global transaction.
	 */
	public synchronized void rollback(Xid xid) throws XAException {
		dataSource.log.debug("StandardXAConnection:rollback");
		if (xid == null)
			throw new XAException(XAException.XAER_INVAL);

		StandardXAStatefulConnection statecon =
			xaDataSource.getConnection(xid, true);
		// must find connection for this transaction

		try {
			switch (statecon.getState()) { // action depends on current state
				case Status.STATUS_PREPARING : // already commited
					throw new XAException(XAException.XA_HEURCOM);
				case Status.STATUS_PREPARED : // ready to do rollback
				case Status.STATUS_ROLLING_BACK :
				case Status.STATUS_ACTIVE :
					try {
						dataSource.log.debug(
							"StandardXAConnection:rollback try to perform the rollback operation");
						statecon.con.rollback();
						// perform the rollback operation
						dataSource.log.debug(
							"StandardXAConnection:rollback performed the rollback");
					} catch (SQLException e) {
						throw new XAException(XAException.XA_RBROLLBACK);
						// rollback will have been performed
					}
					break;
				default :
					throw new XAException(XAException.XAER_PROTO);
			}
		} catch (XAException e) {
			throw e;
		} finally {
			try {
				dataSource.log.debug(
					"StandardXAConnection:rollback setAutoCommit to '"
						+ thisAutoCommit
						+ "'");
				statecon.con.setAutoCommit(thisAutoCommit);
			} catch (SQLException e) {
				dataSource.log.debug(
					"StandardXAConnection:rollback setAutoCommit problem");
			}
			xaDataSource.freeConnection(xid, false);
		}
	}

	public boolean isSameRM(XAResource xares) throws XAException {
		dataSource.log.debug("StandardXAConnection:isSameRM");
		if (equals(xares)) { // if the same object
			dataSource.log.debug("StandardXAConnection:isSameRM isSameRM");
			return true; // then definitely the same RM
		}
		if (!(xares instanceof StandardXAConnection)) {
			// if it's not one of our wrappers
			dataSource.log.debug("StandardXAConnection:isSameRM not isSameRM");
			return false; // then it's definitely not the same RM
		}
		StandardXAConnection xac = (StandardXAConnection) xares;
		// cast to something more convenient
		if (dataSource.equals(xac.dataSource)) {
			// if they originate from same data source
			dataSource.log.debug(
				"StandardXAConnection:isSameRM isSameRM (equal datasource)");
			return true; // then they're the same RM
		} else {
			dataSource.log.debug(
				"StandardXAConnection:isSameRM not isSameRM (not equal datasource)");
			return false;
		}
	}

	/**
	 * This is called by a TM when the RM has reported a heuristic
	 * completion. It must retain the transaction context until told
	 * to forget about it.
	 */
	public void forget(Xid xid) throws XAException {
		dataSource.log.debug("StandardXAConnection:forget forget with Xid");
		if (xid == null)
			throw new XAException(XAException.XAER_INVAL);

		//StandardXAStatefulConnection statecon = xaDataSource.getConnection (xid, true);// must find connection for this transaction
		xaDataSource.freeConnection(xid, false);
		// finished with this transaction
	}

	/**
	 * Called by the transaction manager during recovery. If it was the
	 * transaction manager or another compoenent which failed then we
	 * can supply our known Xids. However if we failed then this method
	 * does nothing - we need to know about database internals to do that.
	 */
	public Xid[] recover(int flag) throws XAException {
		dataSource.log.debug(
			"StandardXAConnection:recover recover flag=" + flag);
		if (flag != TMSTARTRSCAN && flag != TMENDRSCAN && flag != TMNOFLAGS) {
			throw new XAException(XAException.XAER_INVAL);
		}

		Xid[] retval = null;
		retval = xaDataSource.recover(); // get all valid Xids
		return retval;
	}

	/**
	 * Accessor methods for timeout.
	 */
	public boolean setTransactionTimeout(int seconds) {
		timeoutSecs = seconds;
		return false;
	}

	public int getTransactionTimeout() {
		return timeoutSecs;
	}

	public void setCommitOnPrepare(boolean commitOnPrepare) {
		this.commitOnPrepare = commitOnPrepare;
	}

	public boolean getCommitOnPrepare() {
		return commitOnPrepare;
	}

	/**
	 * Periodically checks for timed out connections.
	 */
	public void run() {
		//dataSource.log.debug("StandardXAConnection:run check for timed out connections");
		while (true) { // loop forever
			/*
			if (nextTimeout == 0) {			// if there are no more timeouts scheduled
			timerThread.suspend();			// then go to sleep
			if (isClosed) return;						// exit if connection is closed
			}
			*/
			try {
				synchronized (this) {
					while (nextTimeout == 0) {
						wait();
					}
				}
			} catch (InterruptedException e) {
			}

			if (isClosed) {
				return;
			}

			try {
				Thread.sleep(timeoutPeriod); // sleep for a few seconds
				if (isClosed)
					return; // exit if connection is closed
			} catch (InterruptedException e) {
				e.printStackTrace(); // we don't expect any of these
			}

			long curTime = System.currentTimeMillis(); // get system time
			if (curTime < nextTimeout)
				continue; // check for time still to go

			// One or more transactions have timeout out.
			try {
				nextTimeout = xaDataSource.checkTimeouts(curTime);
				// check to see if connections have timeout out
			} catch (Exception e) {
				e.printStackTrace(); // we don't expect any of these
			}
		}
	}

	public Reference getReference() throws NamingException {
		// Note that we use getClass().getName() to provide the factory
		// class name. It is assumed that this class, and all of its
		// descendants are their own factories.
		dataSource.log.debug(
			"StandardXAConnection:getReference return a reference of the object");
		Reference ref =
			new Reference(getClass().getName(), getClass().getName(), null);
		ref.add(
			new StringRefAddr(
				"commitOnPrepare",
				String.valueOf(getCommitOnPrepare())));
		ref.add(
			new StringRefAddr(
				"timeoutSecs",
				Integer.toString(getTransactionTimeout())));
		return ref;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("StandardXAConnection:\n");
		sb.append("     commit on prepare =<"+this.commitOnPrepare+">\n");
		sb.append("     is closed =<"+this.isClosed + ">\n");
		sb.append("     this autoCommit =<"+this.thisAutoCommit + ">\n");
		sb.append("     listeners size =<"+this.listeners.size() + ">\n");
		sb.append("     next timeOut =<"+this.nextTimeout + ">\n");
		sb.append("     timeOut period =<"+this.timeoutPeriod + ">\n");
		sb.append("     timeOut secs =<"+this.timeoutSecs + ">\n");
		sb.append("     transaction manager=<"+this.transactionManager + ">\n");
		sb.append(this.xaDataSource.toString());
		sb.append(this.dataSource.toString());
		if (curCon != null)
			sb.append(this.curCon.toString());
		if (connectionHandle != null)
			sb.append(this.connectionHandle.toString());
		sb.append(this.con.toString());
		
		return sb.toString();
	
	}
}
