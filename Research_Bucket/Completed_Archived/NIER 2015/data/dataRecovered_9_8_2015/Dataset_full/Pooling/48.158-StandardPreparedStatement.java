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
import org.enhydra.jdbc.core.CorePreparedStatement;

/**
 * A very simple implementation of PreparedStatement. When created
 * it is supplied with another PreparedStatement to which nearly all
 * of this class' methods delegate their work. 
 *
 * Close() is overridden to prevent the statement from actually
 * being closed.
 */
public class StandardPreparedStatement extends CorePreparedStatement {

	public Object key;
	// key that StandardConnectionHandle uses to return to cache
	private StandardConnectionHandle con;
	// the StandardConnectionHandle that created this object
	public boolean closed; // true when the PreparedStatement has been closed

	/**
	 * Constructor.
	 */
	StandardPreparedStatement(
		StandardConnectionHandle con,
		PreparedStatement preparedStatement,
		Object key) {
		this.con = con;
		this.key = key;
		ps = preparedStatement;
	}

	StandardPreparedStatement() {
		super();
	}

	/**
	 * Close this statement.
	 */
	public void close() throws SQLException {
		// Note no check for already closed - some servers make mistakes
		closed = true;
		if (con.preparedStmtCacheSize == 0) {
			// no cache, so we just close
			if (ps != null) {
				ps.close();
			}
		} else {
			con.returnToCache(key);
			// return the underlying statement to the cache
		}
	}

	/**
	 * Pre-invokation of the delegation, in case of the Statement is
	 * closed, we throw an exception
	 */
	public void preInvoke() throws SQLException {
		if (closed)
			throw new SQLException("Prepare Statement is closed");
	}

	/**
	 * Exception management : catch or throw the exception
	 */
	public void catchInvoke(SQLException sqlException) throws SQLException {
		//ConnectionEvent event = new ConnectionEvent(con.pooledCon);
		//con.pooledCon.connectionErrorOccurred(event);
		throw (sqlException);
	}

}
