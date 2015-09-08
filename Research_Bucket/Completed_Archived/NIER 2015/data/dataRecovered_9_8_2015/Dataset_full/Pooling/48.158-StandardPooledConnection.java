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

import org.enhydra.jdbc.util.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;

/**
 * Provides an implementation of javax.sql.PooledConnection which
 * is completely generic (i.e. it relies only on JDBC 1 functionality).
 *
 * This class maintains a physical database connection which is
 * passed to each StandardXAConnectionHandle when it is created. It is the
 * StandardXAConnectionHandle object which the application receives and which
 * it perceives as the java.sql.Connection object.
 *
 * StandardXAConnectionHandle objects pass PreparedStatements back to the
 * StandardPooledConnection so that they can be retained across
 * StandardXAConnectionHandle instantiations.
 */
public class StandardPooledConnection implements PooledConnection {

	protected StandardConnectionPoolDataSource dataSource;
	public Connection con; // the physical database connection
	public StandardConnectionHandle connectionHandle;
	// the last StandardConnectionHandle created
	Vector listeners; // objects listening for events on this connection
	boolean isClosed; // true if this connection has been closed
	public Logger log;

	/**
	 * Creates the physical database connection.
	 */
	public StandardPooledConnection(
		StandardConnectionPoolDataSource dataSource,
		String user,
		String password)
		throws SQLException {
		this.dataSource = dataSource;
		con = dataSource.getConnection(user, password);
		listeners = new Vector(5, 5);
	}

	/**
	 * Creates a new StandardConnectionHandle for use by an application.
	 * If there is already a StandardConnectionHandle in use then it is
	 * closed (i.e. the application has the connection withdrawn).
	 */
	synchronized public Connection getConnection()
		throws java.sql.SQLException {
		if (connectionHandle != null) {
			// if there's already a connection handle
			if (!connectionHandle.isClosed()) { // and it hasn't been closed
				connectionHandle.close(); // close it now
			}
		}
		newConnectionHandle();
		return connectionHandle;
	}

	protected void newConnectionHandle() {
		log.debug("StandardPooledConnection:newConnectionHandle");
		connectionHandle =
			new StandardConnectionHandle(
				this,
				dataSource.getMasterPrepStmtCache(),
				dataSource.getPreparedStmtCacheSize());
	}

	public void close() throws java.sql.SQLException {
		con.close();
		dataSource.getMasterPrepStmtCache().remove(con.toString());
	}

	public void addConnectionEventListener(ConnectionEventListener listener) {
		listeners.addElement(listener);
	}

	public void removeConnectionEventListener(ConnectionEventListener listener) {
		listeners.removeElement(listener);
	}

	/**
	 * Notifies all listeners that the StandardConnectionHandle created by this
	 * PooledConnection has been closed.
	 */
	void closeEvent() {
		ConnectionEvent event = new ConnectionEvent(this);
		// create the event that we'll send
		for (int i = 0; i < listeners.size(); i++) { // for each listener
			Object obj = listeners.elementAt(i); // get next listener
			ConnectionEventListener cel = (ConnectionEventListener) obj;
			//cast to something more useful
			cel.connectionClosed(event); // notify this listener
		}

	}

	/**
	 * Invoked when a fatal connection error occurs, 
	 * just before an SQLException is thrown to the application
	 *
	 * This method is automatically called when a fatal error 
	 * is detected on the base connection. The base connection 
	 * is the actual connection that backs the connection
	 * handle provided by the getConnection() method
	 */
	public void connectionErrorOccurred(ConnectionEvent event) {
		for (int i = 0; i < listeners.size(); i++) { // for each listener
			Object obj = listeners.elementAt(i); // get next listener
			ConnectionEventListener cel = (ConnectionEventListener) obj;
			//cast to something more useful
			cel.connectionErrorOccurred(event); // notify this listener
			//cel.connectionClosed (event);				// notify this listener
		}
	}

	/**
	 * Access method allowing access to the underlying physical connection.
	 */
	public Connection getPhysicalConnection() {
		return con;
	}

	public void setLogger(Logger alog) {
		log = alog;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("StandardPooledConnection:\n");
		sb.append("     is closed =<"+this.isClosed + ">\n");
		sb.append("     connection =<"+this.con + ">\n");
		
		return sb.toString();
	}
}
