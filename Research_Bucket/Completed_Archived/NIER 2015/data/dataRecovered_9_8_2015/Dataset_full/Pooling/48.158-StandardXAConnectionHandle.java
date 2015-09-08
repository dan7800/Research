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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.CallableStatement;
import java.util.Hashtable;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

public class StandardXAConnectionHandle extends StandardConnectionHandle {

    boolean resetTxonResume =false;
	boolean globalTransaction; // true if a global transaction is in effect
	public TransactionManager transactionManager;
	public Transaction tx;
	public StandardXAConnection xacon;
	public boolean thisAutoCommit = true;

	/**
	 * Constructor
	 */
	public StandardXAConnectionHandle(
		StandardXAConnection pooledCon,
		Hashtable preparedStatementCache,
		int preparedStmtCacheSize,
		TransactionManager tm) {
		super(pooledCon, preparedStatementCache, preparedStmtCacheSize);
		// setup StandardXAConnectionHandle
		xacon = pooledCon;
		transactionManager = tm;
		log = pooledCon.dataSource.log;

		// This will have set the Connection to the current Connection.
		// However this might change if a global transaction gets selected.
	}

	public void setTransactionManager(TransactionManager tm) {
		this.transactionManager = tm;
	}

	synchronized public void close() throws SQLException {
		Transaction ttx = tx;
		// note: ttx is used instead of tx because super.close(), call end()
		// on StdXAConnection which call tx = null;
		super.close();
		log.debug("StandardXAConnectionHandle:close");
		log.debug(
			"StandardXAConnectionHandle:close globalTransaction='"
				+ globalTransaction
				+ "' con.getAutoCommit='"
				+ con.getAutoCommit()
				+ "' ttx='"
				+ ttx
				+ "'");

		if ((!con.getAutoCommit()) && (ttx == null)) {
			log.debug(
				"StandardXAConnectionHandle:close rollback the connection");
			con.rollback();
			con.setAutoCommit(thisAutoCommit);
		} else
			log.debug("StandardXAConnectionHandle:close do nothing else");
		isReallyUsed = false;
		log.debug(
			"StandardXAConnectionHandle:close AFTER globalTransaction='"
				+ globalTransaction
				+ "' con.getAutoCommit='"
				+ con.getAutoCommit()
				+ "' ttx='"
				+ ttx
				+ "'");
	}

	/**
	 * Called by the StandardXADataSource when a global transaction
	 * gets associated with this connection.
	 */
	void setGlobalTransaction(boolean setting) throws SQLException {
		log.debug(
			"StandardXAConnectionHandle:setGlobalTransaction gTransaction='"
				+ setting
				+ "'");
		globalTransaction = setting; // set global flag
		con = pooledCon.getPhysicalConnection(); // get the real connection
		if (con == null)
			log.warn(
				"StandardXAConnectionHandle:setGlobalTransaction con is null before setupPreparedStatementCache");
		else
			log.debug(
				"StandardXAConnectionHandle:setGlobalTransaction con is *NOT* null before setupPreparedStatementCache");
		//setupPreparedStatementCache();
		if(!isClosed())
			super.setAutoCommit(!setting);
		// commits must be done by transaction manager
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		if (globalTransaction) // if taking part in a global transaction
			throw new SQLException("StandardXAConnectionHandle:setAutoCommit This connection is part of a global transaction");
		super.setAutoCommit(autoCommit);
	}

	public void commit() throws SQLException {
		if (globalTransaction) // if taking part in a global transaction
			throw new SQLException("StandardXAConnectionHandle:commit:This connection is part of a global transaction");
		super.commit();
		//tx = null;
	}

	public void rollback() throws SQLException {
		if (globalTransaction) // if taking part in a global transaction
			throw new SQLException("StandardXAConnectionHandle:rollback:This connection is part of a global transaction");
		super.rollback();
		//tx = null;
	}

	synchronized PreparedStatement checkPreparedCache(
		String sql,
		int type,
		int concurrency,
		int holdability,
		Object lookupKey)
		throws SQLException {
		PreparedStatement ret = null; // the return value
		// NOTE - We include the Connection in the lookup key. This has no
		// effect here but is needed by StandardXAConnection where the the physical
		// Connection used can vary over time depending on the global transaction.
		if (preparedStatementCache != null) {
			Object obj = preparedStatementCache.get(lookupKey);
			// see if there's a PreparedStatement already
			if (obj != null) { // if there is
				log.debug(
					"StandardXAConnectionHandle:checkPreparedCache object is found");
				ret = (PreparedStatement) obj; // use as return value
				try {
					ret.clearParameters(); // make it look like new
				} catch (SQLException e) {
					// Bad statement, so we have to create a new one
					ret = createPreparedStatement(sql, type, concurrency, holdability);
					// create new prepared statement
				}
				preparedStatementCache.remove(lookupKey);
				// make sure it cannot be re-used
				inUse.put(lookupKey, ret);
				// make sure it gets reused by later delegates
			} else { // no PreparedStatement ready
				log.debug(
					"StandardXAConnectionHandle:checkPreparedCache object is *NOT* found");
				ret = createPreparedStatement(sql, type, concurrency, holdability);
				// create new prepared statement
				inUse.put(lookupKey, ret);
				// will get saved in prepared statement cache
			}
		} else {
			log.debug(
				"StandardXAConnectionHandle:checkPreparedCache object the cache is out");
			ret = createPreparedStatement(sql, type, concurrency, holdability);
			// create new prepared statement
		}
		// We don't actually give the application a real PreparedStatement. Instead
		// they get a StandardPreparedStatement that delegates everything except
		// PreparedStatement.close();
		log.debug(
			"StandardXAConnectionHandle:checkPreparedCache pstmt='"
				+ ret.toString()
				+ "'");
		return ret;
	}



	synchronized PreparedStatement checkPreparedCache(
		String sql,
		int autogeneratedkeys,
		Object lookupKey)
		throws SQLException {
		PreparedStatement ret = null; // the return value
		// NOTE - We include the Connection in the lookup key. This has no
		// effect here but is needed by StandardXAConnection where the the physical
		// Connection used can vary over time depending on the global transaction.
		if (preparedStatementCache != null) {
			Object obj = preparedStatementCache.get(lookupKey);
			// see if there's a PreparedStatement already
			if (obj != null) { // if there is
				log.debug(
					"StandardXAConnectionHandle:checkPreparedCache object is found");
				ret = (PreparedStatement) obj; // use as return value
				try {
					ret.clearParameters(); // make it look like new
				} catch (SQLException e) {
					// Bad statement, so we have to create a new one
					ret = createPreparedStatement(sql, autogeneratedkeys);
					// create new prepared statement
				}
				preparedStatementCache.remove(lookupKey);
				// make sure it cannot be re-used
				inUse.put(lookupKey, ret);
				// make sure it gets reused by later delegates
			} else { // no PreparedStatement ready
				log.debug(
					"StandardXAConnectionHandle:checkPreparedCache object is *NOT* found");
				ret = createPreparedStatement(sql, autogeneratedkeys);
				// create new prepared statement
				inUse.put(lookupKey, ret);
				// will get saved in prepared statement cache
			}
		} else {
			log.debug(
				"StandardXAConnectionHandle:checkPreparedCache object the cache is out");
			ret = createPreparedStatement(sql, autogeneratedkeys);
			// create new prepared statement
		}
		// We don't actually give the application a real PreparedStatement. Instead
		// they get a StandardPreparedStatement that delegates everything except
		// PreparedStatement.close();
		log.debug(
			"StandardXAConnectionHandle:checkPreparedCache pstmt='"
				+ ret.toString()
				+ "'");
		return ret;
	}



	/**
	 * Creates a PreparedStatement for the given SQL. If possible, the
	 * statement is fetched from the cache.
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return prepareStatement(sql, 0, 0, 0);
	}

	public PreparedStatement prepareStatement(
		String sql,
		int resultSetType,
		int resultSetConcurrency)
		throws SQLException {
	    return prepareStatement(sql, resultSetType, resultSetConcurrency, 0);
	}

	/**
	 * Creates a PreparedStatement for the given SQL, type and concurrency.
	 * If possible, the statement is fetched from the cache.
	 */
	public PreparedStatement prepareStatement(
		String sql,
		int resultSetType,
		int resultSetConcurrency,
		int resultSetHoldability)
		throws SQLException {
		if (tx == null) {
			log.debug("StandardXAConnectionHandle:prepareStatement tx==null");
			try {
				try {
					Transaction ntx = this.getTransaction();
					if (ntx != null) {
						log.debug(
							"StandardXAConnectionHandle:prepareStatement (found a transaction)");
						tx = ntx;
						xacon.thisAutoCommit = this.getAutoCommit();
						if (this.getAutoCommit()) {
							this.setAutoCommit(false);
						}
						try {
							tx.enlistResource(xacon.getXAResource());
							// enlist the xaResource in the transaction
						} catch (RollbackException n) {
							log.debug(
								"StandardXAConnectionHandle:prepareStatemnet enlistResource exception : "
									+ n.toString());
						}
					} else {
						log.debug(
							"StandardXAConnectionHandle:prepareStatement (no transaction found)");
					}
				} catch (SystemException n) {
					n.printStackTrace();
					throw new SQLException(
						"StandardXAConnectionHandle:prepareStatement getTransaction exception: "
							+ n.toString());
				}
			} catch (NullPointerException n) {
				// current is null: we are not in EJBServer.
				n.printStackTrace();
				throw new SQLException("StandardXAConnectionHandle:prepareStatement should not be used outside an EJBServer");
			}
		} else
			log.debug("StandardXAConnectionHandle:prepareStatement tx!=null");

		// if you want to use a REAL PrepareStatement object, please
		// uncomment the 2 following lines and comment the last ones.
		//PreparedStatement ops = con.prepareStatement(sql, resultSetType, resultSetConcurrency);
		//return ops;

		isReallyUsed = true;
		return new StandardXAPreparedStatement(
			this,
			sql,
			resultSetType,
			resultSetConcurrency,
			resultSetHoldability);
	}

        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) 
	    throws SQLException {
	    if (tx == null) {
		log.debug("StandardXAConnectionHandle:prepareStatement tx==null");
		try {
		    try {
			Transaction ntx = this.getTransaction();
			if (ntx != null) {
			    log.debug(
				      "StandardXAConnectionHandle:prepareStatement (found a transaction)");
			    tx = ntx;
			    xacon.thisAutoCommit = this.getAutoCommit();
			    if (this.getAutoCommit()) {
				this.setAutoCommit(false);
			    }
			    try {
				tx.enlistResource(xacon.getXAResource());
				// enlist the xaResource in the transaction
			    } catch (RollbackException n) {
				log.debug(
					  "StandardXAConnectionHandle:prepareStatemnet enlistResource exception : "
					  + n.toString());
			    }
			} else {
			    log.debug(
				      "StandardXAConnectionHandle:prepareStatement (no transaction found)");
			}
		    } catch (SystemException n) {
			n.printStackTrace();
			throw new SQLException(
					       "StandardXAConnectionHandle:prepareStatement getTransaction exception: "
					       + n.toString());
		    }
		} catch (NullPointerException n) {
		    // current is null: we are not in EJBServer.
		    n.printStackTrace();
		    throw new SQLException("StandardXAConnectionHandle:prepareStatement should not be used outside an EJBServer");
		}
	    } else
		log.debug("StandardXAConnectionHandle:prepareStatement tx!=null");
	    
	    isReallyUsed = true;
	    return new StandardXAPreparedStatement(
						   this,
						   sql,
						   autoGeneratedKeys);
	}
    
       /**
	* not yet implemented
	*/
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
	    throw new UnsupportedOperationException();
	}

       /**
	* not yet implemented
	*/
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
	    throw new UnsupportedOperationException();
	}


	/**
	 * Creates a CallableStatement for the given SQL, result set type and concurency
	 */
	public CallableStatement prepareCall(
		String sql,
		int resultSetType,
		int resultSetConcurrency)
		throws SQLException {
		return new StandardXACallableStatement(
			this,
			sql,
			resultSetType,
			resultSetConcurrency, 0);
	}

	/**
	 * Creates a CallableStatement for the given SQL
	 */
	public CallableStatement prepareCall(String sql) throws SQLException {
		return new StandardXACallableStatement(this, sql, 0, 0, 0);
	}

        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) 
		throws SQLException {
		return new StandardXACallableStatement(
			this,
			sql,
			resultSetType,
			resultSetConcurrency,
			resultSetHoldability);
	}


	public Statement createStatement() throws SQLException {
	    return createStatement(0,0,0);
	}

	public Statement createStatement(
		int resultSetType,
		int resultSetConcurrency)
		throws SQLException {
	    return createStatement(resultSetType, resultSetConcurrency, 0);
	}

	public Statement createStatement(
		int resultSetType,
		int resultSetConcurrency,
		int resultSetHoldability)
		throws SQLException {

		if (tx == null) {
			log.debug("StandardXAConnectionHandle:createStatement tx==null");
			try {
				try {
					Transaction ntx = this.getTransaction();
					if (ntx != null) {
						log.debug(
							"StandardXAConnectionHandle:createStatement (found a transaction)");
						tx = ntx;
						xacon.thisAutoCommit = this.getAutoCommit();
						if (this.getAutoCommit()) {
							this.setAutoCommit(false);
						}
						try {
							tx.enlistResource(xacon.getXAResource());
							// enlist the xaResource in the transaction
						} catch (RollbackException n) {
							log.debug(
								"StandardXAConnectionHandle:createStatement enlistResource exception: "
									+ n.toString());
						}
					} else {
						log.debug(
							"StandardXAConnectionHandle:createStatement (no transaction found)");
					}

				} catch (SystemException n) {
					throw new SQLException(
						"StandardXAConnectionHandle:createStatement getTransaction exception: "
							+ n.toString());
				}
			} catch (NullPointerException n) {
				// current is null: we are not in EJBServer.
				throw new SQLException(
					"StandardXAConnectionHandle:createStatement should not be used outside an EJBServer: "
						+ n.toString());
			}
		}
		isReallyUsed = true;
		return new StandardXAStatement(this, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	private Transaction getTransaction() throws SystemException {
		Transaction ntx = null;
		if (transactionManager != null) {
			ntx = transactionManager.getTransaction();
		} else {
			log.debug(
				"StandardXAConnectionHandle:getTransaction (null transaction manager)");
		}

		return ntx;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("StandardXAConnectionHandle:\n");
		sb.append("     global transaction =<"+this.globalTransaction+ ">\n");
		sb.append("     is really used =<"+this.isReallyUsed+ ">\n");
		sb.append("     this autoCommit =<"+this.thisAutoCommit+ ">\n");
		sb.append("     in use size =<"+this.inUse.size()+ ">\n");
		sb.append("     master prepared stmt cache size =<"+this.masterPrepStmtCache.size()+ ">\n");
		sb.append("     transaction =<"+this.tx+ ">\n");
		sb.append("     connection =<"+this.con.toString()+ ">\n");		
		
		return sb.toString();
	}
}
