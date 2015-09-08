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
import java.sql.Statement;
import org.enhydra.jdbc.core.CoreStatement;

public class StandardXAStatement extends CoreStatement {

	private StandardXAConnectionHandle con;
	// the StandardConnectionHandle that created this object
	private boolean closed; // true when the Statement has been closed
	private int resultSetType;
	private int resultSetConcurrency;
        private int resultSetHoldability;

	/**
	 * Constructor.
	 */
	StandardXAStatement(
		StandardXAConnectionHandle con,
		int resultSetType,
		int resultSetConcurrency,
		int resultSetHoldability)
		throws SQLException {
		this.con = con;
		this.resultSetType = resultSetType;
		this.resultSetConcurrency = resultSetConcurrency;
		this.resultSetHoldability = resultSetHoldability;
		log = con.log;
		statement = newStatement();
	}

	private Statement newStatement() throws SQLException {
		if (resultSetType == 0 && resultSetConcurrency == 0 && resultSetHoldability == 0) {
			return con.con.createStatement();
		} else if (resultSetHoldability == 0) {
			return con.con.createStatement(resultSetType, resultSetConcurrency);
		} else return con.con.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/**
	 * Close this statement.
	 */
	public synchronized void close() throws SQLException {
		super.close(); // we do not reuse the Statement, we have to close it
		closed = true;
	}


	/**
	 * Exception management : catch or throw the exception
	 */
	public void catchInvoke(SQLException sqlException) throws SQLException {
		//ConnectionEvent event = new ConnectionEvent (con.pooledCon);
		//con.pooledCon.connectionErrorOccurred(event);
		throw (sqlException);
	}

}
