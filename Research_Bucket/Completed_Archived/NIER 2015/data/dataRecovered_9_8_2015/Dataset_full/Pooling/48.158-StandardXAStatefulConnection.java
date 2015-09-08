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
import javax.transaction.xa.Xid;
import javax.transaction.Status;

/**
 * Provides a wrapper for a physical database connection. Each connection can be
 * associated with an XID and can be in one of several states depending on which
 * XAResource calls have been made against a given XID. This allows a StandardXAConnection
 * to multiplex between different XIDs by selecting the appropriate stateful
 * connection.
 */
public class StandardXAStatefulConnection {

	public static int nextId; // used to allocate unique IDs
	public int id; // unique ID for this stateful connection
	public Connection con; // the phsyical database connection
	private int state; // one of the states listed below
	public StandardXADataSource dataSource; // used to log messages
	Xid xid; // global TX associated with this connection (if any)
	public boolean commitOnPrepare; // true if commit takes place on prepare
	long timeout; // time when this transaction times out
	boolean timedOut; // true if this transaction branch has timed out

	/**
	 * Creates a new stateful connection in the FREE state (NO_TRANSACTION)
	 */
	public StandardXAStatefulConnection(
		StandardXADataSource dataSource,
		Connection con) {
		this.con = con;
		this.dataSource = dataSource;
		id = ++nextId; // allocate a unique ID for logging
		this.state = Status.STATUS_NO_TRANSACTION;
		dataSource.log.debug("StandardXAStatefulConnection created");
	}

	/**
	 * Accessor methods for "state" property.
	 */
	synchronized void setState(int newState) {
		dataSource.log.debug(
			"StandardXAStatefulConnection:setState Stateful connection: "
				+ id
				+ " (state before="
				+ state
				+ ")");
		state = newState;
		dataSource.log.debug(
			"StandardXAStatefulConnection:setState Stateful connection: "
				+ id
				+ " (state after="
				+ state
				+ ")");
	}

	int getState() {
		return state;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("StandardXAStatefulConnection:\n");
		sb.append("     commit on prepare =<"+this.commitOnPrepare+ ">\n");
		sb.append("     timed out =<"+this.timedOut+ ">\n");
		sb.append("     id =<"+this.id+ ">\n");
		sb.append("     state =<"+this.state+ ">\n");
		sb.append("     time out =<"+this.timeout+ ">\n");
		sb.append("     xid =<"+this.xid+ ">\n");
				
		return sb.toString();
	}
}
