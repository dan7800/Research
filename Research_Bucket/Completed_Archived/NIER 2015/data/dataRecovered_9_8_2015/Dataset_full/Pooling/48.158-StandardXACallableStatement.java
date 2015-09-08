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
import java.sql.CallableStatement;
import org.enhydra.jdbc.core.CoreCallableStatement;
import javax.transaction.Transaction;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;

public class StandardXACallableStatement extends CoreCallableStatement {

	private StandardXAConnectionHandle con;
	// the StandardConnectionHandle that created this object
	private boolean closed; // true when the Statement has been closed
	private String sql;
	private int resultSetType;
	private int resultSetConcurrency;
        private int resultSetHoldability;

	/**
	 * Constructor.
	 */
	StandardXACallableStatement(
		StandardXAConnectionHandle con,
		String sql,
		int resultSetType,
		int resultSetConcurrency,
		int resultSetHoldability)
		throws SQLException {
		this.con = con;
		this.sql = sql;
		this.resultSetType = resultSetType;
		this.resultSetConcurrency = resultSetConcurrency;
		this.resultSetHoldability = resultSetHoldability;
		log = con.log;
		//cs = newStatement();
	}

	private CallableStatement newStatement() throws SQLException {
		if (resultSetType == 0 && resultSetConcurrency == 0 && resultSetHoldability == 0) {
			return con.con.prepareCall(sql);
		} else if (resultSetHoldability == 0) {
			return con.con.prepareCall(
				sql,
				resultSetType,
				resultSetConcurrency);
		} else return con.con.prepareCall(
						  sql,
						  resultSetType,
						  resultSetConcurrency,
						  resultSetHoldability);
		

	}

	/**
	 * Close this statement.
	 */
	public synchronized void close() throws SQLException {
		super.close(); // we do not reuse the Statement, we have to close it
		closed = true;
	}

	/**
	 * Pre-invokation of the delegation, in case of the Statement is
	 * closed, we throw an exception
	 */
	public synchronized void preInvoke() throws SQLException {
		if (closed)
			throw new SQLException("Prepare Statement is closed");

		Transaction ntx = null;
		if (con.tx == null) {
			try {
				try {
					ntx =
						(con.transactionManager != null)
							? con.transactionManager.getTransaction()
							: null;
					if (ntx != null) {
						con.tx = ntx;
						con.xacon.thisAutoCommit = con.getAutoCommit();
						con.setAutoCommit(false);
						try {
							con.tx.enlistResource(con.xacon.getXAResource());
							// enlist the xaResource in the transaction
							if (cs != null) {
								cs.close();
								cs = null;
							}
						} catch (RollbackException n) {
							throw new SQLException(
								"StandardXAStatement:preInvoke enlistResource exception: "
									+ n.toString());
						}
					}
					//else con.setAutoCommit(true);

				} catch (SystemException n) {
					throw new SQLException(
						"StandardXAStatement:preInvoke getTransaction exception: "
							+ n.toString());
				}
			} catch (NullPointerException n) {
				// current is null: we are not in EJBServer.
				throw new SQLException(
					"StandardXAStatement:preInvoke should not be used outside an EJBServer: "
						+ n.toString());
			}
		}
		if (cs == null) {
			cs = newStatement();
		}

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
