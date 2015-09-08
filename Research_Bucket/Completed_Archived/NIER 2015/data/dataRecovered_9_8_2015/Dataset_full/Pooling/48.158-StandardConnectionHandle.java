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

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.sql.Connection;
import org.enhydra.jdbc.core.CoreConnection;
import org.enhydra.jdbc.util.LRUCache;

/**
 * This is an implementation of java.sql.Connection which simply
 * delegates almost everything to an underlying physical implemention
 * of the same interface.
 *
 * It relies on a StandardPooledConnection to create it and to supply the
 * physical connection and a cache of PreparedStatements. This class will
 * try to re-use PreparedStatements wherever possible and will add to the
 * cache when totally new PreparedStatements get created.
 */
public class StandardConnectionHandle extends CoreConnection {

	StandardPooledConnection pooledCon;
	// the pooled connection that created this object
	protected Hashtable masterPrepStmtCache;
	// the hashtable of caches, indexed by physical connection
	int preparedStmtCacheSize; // the size of the connection-specific cache
	protected LRUCache preparedStatementCache = null;
	// prepared statements indexed by SQL string
	public Hashtable inUse; // prepared statements that are currently in use
	private boolean closed; // set true when this connection has been closed
	public boolean isReallyUsed = false;

	/**
	 * Constructor.
	 */
	public StandardConnectionHandle(
		StandardPooledConnection pooledCon,
		Hashtable preparedStatementCache,
		int preparedStmtCacheSize) {
		super(pooledCon.getPhysicalConnection()); // get the real connection
		this.pooledCon = pooledCon; // first save parameters
		masterPrepStmtCache = preparedStatementCache;
		this.preparedStmtCacheSize = preparedStmtCacheSize;
		log = pooledCon.dataSource.log;
		setupPreparedStatementCache();
		inUse = new Hashtable(10, 0.5f);

		log.debug(
			"StandardConnectionHandle:new StandardConnectionHandle with "
				+ preparedStmtCacheSize
				+ " prepared statement");
	}

	protected void setupPreparedStatementCache() {
		log.debug("StandardConnectionHandle:setupPreparedStatementCache start");
		if (preparedStmtCacheSize == 0) {
			log.debug(
				"StandardConnectionHandle:setupPreparedStatementCache return with 0");
			preparedStatementCache = null;
			return;
		}
		if (con == null)
			log.warn("Connection is null");
		else {
			preparedStatementCache =
				(LRUCache) masterPrepStmtCache.get(con.toString());
			if (preparedStatementCache == null) {
				preparedStatementCache =
					new PreparedStatementCache(preparedStmtCacheSize);
				preparedStatementCache.setLogger(log);
				masterPrepStmtCache.put(con.toString(), preparedStatementCache);
				log.debug(
					"StandardConnectionHandle:setupPreparedStatementCache "
						+ "preparedStatementCache.size(lru)='"
						+ preparedStatementCache.LRUSize()
						+ "' "
						+ "preparedStatementCache.size(cache)='"
						+ preparedStatementCache.cacheSize()
						+ "' "
						+ "masterPrepStmtCache.size='"
						+ masterPrepStmtCache.size()
						+ "' ");
			} else preparedStatementCache.setLogger(log);
		}
		log.debug("StandardConnectionHandle:setupPreparedStatementCache end");
	}

	/**
	 * Pre-invokation of the delegation, in case of connection is
	 * closed, we throw an exception
	 */
	public void preInvoke() throws SQLException {
		if (closed)
			throw new SQLException("Connection is closed");
	}

	/**
	 * Exception management : catch or throw the exception
	 */
	public void catchInvoke(SQLException e) throws SQLException {
		//ConnectionEvent event = new ConnectionEvent (pooledCon);// create event associate with the connection
		//pooledCon.connectionErrorOccurred(event);		// ppoled have to be closed
		throw (e); // throw the exception
	}

	/**
	 * Closes this StandardConnectionHandle and prevents it
	 * from being reused. It also returns used PreparedStatements
	 * to the PreparedStatement cache and notifies all listeners.
	 */
	synchronized public void close() throws SQLException {
		log.debug("StandardConnectionHandle:close");
		// Note - we don't check to see if already closed. Some servers get confused.
		closed = true; // connection now closed
		Enumeration keys = inUse.keys(); // get any prepared statements in use
		while (keys.hasMoreElements()) { // while more prepared statements used
			Object key = keys.nextElement(); // get next key
			returnToCache(key); // return prepared statement to cache
		}
		pooledCon.closeEvent(); // notify listeners

                if (preparedStatementCache != null)
		        preparedStatementCache.cleanupAll();
                if ((preparedStatementCache != null) && (masterPrepStmtCache != null) && (log != null))
		log.debug(
			"StandardConnectionHandle:close "
				+ "preparedStatementCache.size(lru)='"
				+ preparedStatementCache.LRUSize()
				+ "' "
				+ "preparedStatementCache.size(cache)='"
				+ preparedStatementCache.cacheSize()
				+ "' "
				+ "masterPrepStmtCache.size='"
				+ masterPrepStmtCache.size()
				+ "' ");
	}

	/**
	 * Removes a prepared statement from the inUse list
	 * and returns it to the cache.
	 */
	void returnToCache(Object key, Connection theCon) {
		Object value = inUse.remove(key);
		// remove key/value from used statements
		if (value != null) {
			LRUCache theCache =
				(LRUCache) masterPrepStmtCache.get(theCon.toString());
			theCache.put(key, value); // place back in cache, ready for re-use
		}
	}

	void returnToCache(Object key) {
		returnToCache(key, con);
	}

	/**
	 * Checks to see if a prepared statement with the same concurrency
	 * has already been created. If not, then a new prepared statement
	 * is created and added to the cache.
	 *
	 * If a prepared statement is found in the cache then it is removed
	 * from the cache and placed on the "inUse" list. This ensures that
	 * if multiple threads use the same StandardConnectionHandle, or a single
	 * thread does multiple prepares using the same SQL, then DIFFERENT
	 * prepared statements will be returned.
	 */
	synchronized PreparedStatement checkPreparedCache(
		String sql,
		int type,
		int concurrency,
		int holdability)
		throws SQLException {
		log.debug(
			"StandardConnectionHandle:checkPreparedCache sql='" + sql + "'");
		PreparedStatement ret = null; // the return value
		// NOTE - We include the Connection in the lookup key. This has no
		// effect here but is needed by StandardXAConnection where the the physical
		// Connection used can vary over time depending on the global transaction.
		String lookupKey = sql + type + concurrency;
		// used to lookup statements
		if (preparedStatementCache != null) {
			Object obj = preparedStatementCache.get(lookupKey);
			// see if there's a PreparedStatement already
			if (obj != null) { // if there is
				ret = (PreparedStatement) obj; // use as return value
				try {
					ret.clearParameters(); // make it look like new
				} catch (SQLException e) {
					// Bad statement, so we have to create a new one
					ret = createPreparedStatement(sql, type, concurrency, holdability);
				}

				preparedStatementCache.remove(lookupKey);
				// make sure it cannot be re-used
				inUse.put(lookupKey, ret);
				// make sure it gets reused by later delegates
			} else { // no PreparedStatement ready
				ret = createPreparedStatement(sql, type, concurrency, holdability);
				inUse.put(lookupKey, ret);
				// will get saved in prepared statement cache
			}
		} else {
			ret = createPreparedStatement(sql, type, concurrency, holdability);
		}
		// We don't actually give the application a real PreparedStatement. Instead
		// they get a StandardPreparedStatement that delegates everything except
		// PreparedStatement.close();

		ret = new StandardPreparedStatement(this, ret, lookupKey);
		return ret;
	}


	synchronized PreparedStatement checkPreparedCache(
		String sql,
		int autogeneratedkeys)
		throws SQLException {
		log.debug(
			"StandardConnectionHandle:checkPreparedCache sql='" + sql + "'");
		PreparedStatement ret = null; // the return value
		// NOTE - We include the Connection in the lookup key. This has no
		// effect here but is needed by StandardXAConnection where the the physical
		// Connection used can vary over time depending on the global transaction.
		String lookupKey = sql + autogeneratedkeys;
		// used to lookup statements
		if (preparedStatementCache != null) {
			Object obj = preparedStatementCache.get(lookupKey);
			// see if there's a PreparedStatement already
			if (obj != null) { // if there is
				ret = (PreparedStatement) obj; // use as return value
				try {
					ret.clearParameters(); // make it look like new
				} catch (SQLException e) {
					// Bad statement, so we have to create a new one
					ret = createPreparedStatement(sql, autogeneratedkeys);
				}

				preparedStatementCache.remove(lookupKey);
				// make sure it cannot be re-used
				inUse.put(lookupKey, ret);
				// make sure it gets reused by later delegates
			} else { // no PreparedStatement ready
				ret = createPreparedStatement(sql, autogeneratedkeys);
				inUse.put(lookupKey, ret);
				// will get saved in prepared statement cache
			}
		} else {
			ret = createPreparedStatement(sql, autogeneratedkeys);
		}
		// We don't actually give the application a real PreparedStatement. Instead
		// they get a StandardPreparedStatement that delegates everything except
		// PreparedStatement.close();

		ret = new StandardPreparedStatement(this, ret, lookupKey);
		return ret;
	}



	protected PreparedStatement createPreparedStatement(
		String sql,
		int type,
		int concurrency,
		int holdability)
		throws SQLException {
		log.debug(
			"StandardConnectionHandle:createPreparedStatement type ='"
				+ type
				+ "'");
		if (type == 0 && holdability == 0) { // if no type or concurrency specified
			return con.prepareStatement(sql); // create new prepared statement
		} else if (holdability == 0) {
			return con.prepareStatement(sql, type, concurrency);
			// create new prepared statement
		} else return con.prepareStatement(sql, type, concurrency, holdability);
	}


	protected PreparedStatement createPreparedStatement(
		String sql,
		int autogeneratedkeys)
		throws SQLException {
		log.debug(
			"StandardConnectionHandle:createPreparedStatement autogeneratedkeys ='"
				+ autogeneratedkeys
				+ "'");
		return con.prepareStatement(sql, autogeneratedkeys); // create new prepared statement
	}

	/**
	 * Creates a PreparedStatement for the given SQL. If possible, the
	 * statement is fetched from the cache.
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		log.debug(
			"StandardConnectionHandle:prepareStatement sql='" + sql + "'");
		preInvoke();
		try {
			return checkPreparedCache(sql, 0, 0, 0);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	/**
	 * Creates a PreparedStatement for the given SQL, type and concurrency.
	 * If possible, the statement is fetched from the cache.
	 */
	public PreparedStatement prepareStatement(
		String sql,
		int resultSetType,
		int resultSetConcurrency)
		throws SQLException {
		preInvoke();
		try {
			return checkPreparedCache(sql, resultSetType, resultSetConcurrency, 0);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public PreparedStatement prepareStatement(
		String sql,
		int resultSetType,
		int resultSetConcurrency,
		int resultSetHoldability)
		throws SQLException {
		preInvoke();
		try {
			return checkPreparedCache(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public boolean isClosed() throws SQLException {
		return closed;
	}

	public CallableStatement prepareCall(
		String sql,
		int resultSetType,
		int resultSetConcurrency)
		throws SQLException {
		preInvoke();
		try {
			return con.prepareCall(sql, resultSetType, resultSetConcurrency);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
}
