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

import java.sql.SQLException;
import java.util.Hashtable;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import java.util.Enumeration;
import org.enhydra.jdbc.util.LRUCache;

/**
 * A data source used to create generic pooled connections (factory)
 */
public class StandardConnectionPoolDataSource
	extends StandardDataSource
	implements ConnectionPoolDataSource {

	private Hashtable masterPrepStmtCache;
	int preparedStmtCacheSize; // size of prepared statement cache
	public static final int DEFAULT_PREPAREDSTMTCACHESIZE = 16;

	/**
	 * Constructor.
	 */
	public StandardConnectionPoolDataSource() {
		super();
		masterPrepStmtCache = new Hashtable();
		preparedStmtCacheSize = DEFAULT_PREPAREDSTMTCACHESIZE;
	}

	/**
	 * Create a pooled connection using the default username and password.
	 */
	public PooledConnection getPooledConnection() throws SQLException {
		log.debug(
			"StandardConnectionPoolDataSource:getPooledConnection(0) return a pooled connection");
		return getPooledConnection(user, password);
	}

	/**
	 * Create a standard pooled connection using the supplied username and password.
	 */
	synchronized public PooledConnection getPooledConnection(
		String user,
		String password)
		throws SQLException {
		log.debug(
			"StandardConnectionPoolDataSource:getPooledConnection(2) return a pooled connection");
		StandardPooledConnection spc =
			new StandardPooledConnection(this, user, password);
		spc.setLogger(log);
		return spc;
	}

	public Hashtable getMasterPrepStmtCache() {
		return masterPrepStmtCache;
	}

	/**
	 * Gets the size of the prepared statement cache
	 */
	public int getPreparedStmtCacheSize() {
		return preparedStmtCacheSize;
	}

	/**
	 * Sets the size of the prepared statement cache
	 */
	public void setPreparedStmtCacheSize(int value) {
		preparedStmtCacheSize = value;
		if (preparedStmtCacheSize <= 0) {
			masterPrepStmtCache.clear();
		} else {
			Enumeration enum = masterPrepStmtCache.elements();
			while (enum.hasMoreElements()) {
				((LRUCache) enum.nextElement()).resize(value);
			}
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("StandardConnectionPoolDataSource:\n");
		sb.append("     master prepared stmt cache size=<"+this.masterPrepStmtCache.size()+">\n");
		sb.append("     prepared stmt cache size =<"+this.preparedStmtCacheSize+">\n");
		sb.append(super.toString());
		
		return sb.toString();
	}
}
