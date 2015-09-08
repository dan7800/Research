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
package org.enhydra.jdbc.core;

import org.enhydra.jdbc.util.JdbcUtil;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Map;

/**
 * This is an implementation of java.sql.Connection which simply
 * delegates everything to an underlying physical implemention
 * of the same interface.
 */
public abstract class CoreConnection extends JdbcUtil implements Connection {
	public Connection con; // the physical database connection

	/**
	 * Constructor
	 */
	public CoreConnection(Connection con) {
		this.con = con;
	}

	public CoreConnection() {
	}

	public void clearWarnings() throws SQLException {
		preInvoke();
		try {
			con.clearWarnings();
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void close() throws SQLException {
		preInvoke();
		try {
			con.close();
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void commit() throws SQLException {
		preInvoke();
		try {
			con.commit();
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public Statement createStatement() throws SQLException {
		preInvoke();
		try {
			return con.createStatement();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public Statement createStatement(
		int resultSetType,
		int resultSetConcurrency)
		throws SQLException {
		preInvoke();
		try {
			return con.createStatement(resultSetType, resultSetConcurrency);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public boolean getAutoCommit() throws SQLException {
		preInvoke();
		try {
			return con.getAutoCommit();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;
	}

	public String getCatalog() throws SQLException {
		preInvoke();
		try {
			return con.getCatalog();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		preInvoke();
		try {
			return con.getMetaData();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public int getTransactionIsolation() throws SQLException {
		preInvoke();
		try {
			return con.getTransactionIsolation();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public Map getTypeMap() throws SQLException {
		preInvoke();
		try {
			return con.getTypeMap();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public SQLWarning getWarnings() throws SQLException {
		preInvoke();
		try {
			return con.getWarnings();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public boolean isReadOnly() throws SQLException {
		preInvoke();
		try {
			return con.isReadOnly();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;
	}

	public String nativeSQL(String sql) throws SQLException {
		preInvoke();
		try {
			return con.nativeSQL(sql);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		preInvoke();
		try {
			return con.prepareCall(sql);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		preInvoke();
		try {
			return con.prepareStatement(sql);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public PreparedStatement prepareStatement(
		String sql,
		int resultSetType,
		int resultSetConcurrency)
		throws SQLException {
		preInvoke();
		try {
			return con.prepareStatement(
				sql,
				resultSetType,
				resultSetConcurrency);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public void rollback() throws SQLException {
		preInvoke();
		try {
			con.rollback();
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		log.debug("CoreConnection:Setautocommit autoCommit was = " + con.getAutoCommit());
		log.debug("CoreConnection:Setautocommit = " + autoCommit);
		preInvoke();
		try {
			con.setAutoCommit(autoCommit);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setCatalog(String catalog) throws SQLException {
		preInvoke();
		try {
			con.setCatalog(catalog);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		preInvoke();
		try {
			con.setReadOnly(readOnly);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setTransactionIsolation(int level) throws SQLException {
		preInvoke();
		try {
			con.setTransactionIsolation(level);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setTypeMap(Map map) throws SQLException {
		preInvoke();
		try {
			con.setTypeMap(map);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	/*
	* Add those following methods to compile on JDK 1.4.
	* Instead those methods are defined in the java.sql.Connection interface
	* only since JDK 1.4.
	*/
	public Statement createStatement(
		int resultSetType,
		int resultSetConcurrency,
		int resultSetHoldability)
		throws SQLException {
		preInvoke();
		try {
         return con.createStatement(resultSetType,resultSetConcurrency,resultSetHoldability);
		} catch (SQLException e) {
			catchInvoke(e);
		}
      return null;
	}
	public int getHoldability() throws SQLException {
		preInvoke();
		try {
			return con.getHoldability();
		} catch (SQLException e) {
			catchInvoke(e);
		}
      return 0;
	}
	public CallableStatement prepareCall(
		String sql,
		int resultSetType,
		int resultSetConcurrency,
		int resultSetHoldability)
		throws SQLException {
		preInvoke();
		try {
         return con.prepareCall(sql,resultSetType,resultSetConcurrency,resultSetHoldability);
		} catch (SQLException e) {
			catchInvoke(e);
		}
      return null;
	}
	public PreparedStatement prepareStatement(
		String sql,
		int autoGeneratedKeys)
		throws SQLException {
		preInvoke();
		try {
			return con.prepareStatement(sql,autoGeneratedKeys);
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
         return prepareStatement(sql,resultSetType,resultSetConcurrency,resultSetHoldability);
		} catch (SQLException e) {
			catchInvoke(e);
		}
      return null;
	}
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
		throws SQLException {
		preInvoke();
		try {
         return con.prepareStatement(sql,columnIndexes);
		} catch (SQLException e) {
			catchInvoke(e);
		}
      return null;
	}
	public PreparedStatement prepareStatement(String sql, String[] columnNames)
		throws SQLException {
		preInvoke();
		try {
         return con.prepareStatement(sql,columnNames);
		} catch (SQLException e) {
			catchInvoke(e);
		}
      return null;
	}
	public void releaseSavepoint(java.sql.Savepoint savepoint)
		throws SQLException {
		preInvoke();
		try {
         con.releaseSavepoint(savepoint);
		} catch (SQLException e) {
			catchInvoke(e);
		}
         
	}
	public void rollback(java.sql.Savepoint savepoint) throws SQLException {
		preInvoke();
		try {
         con.rollback(savepoint);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setHoldability(int holdability) throws SQLException {
		preInvoke();
		try {
         con.setHoldability(holdability);
		} catch (SQLException e) {
			catchInvoke(e);
		}
      
	}
	public java.sql.Savepoint setSavepoint() throws SQLException {
		preInvoke();
		try {
         return con.setSavepoint();
		} catch (SQLException e) {
			catchInvoke(e);
		}
      return null;
	}
	public java.sql.Savepoint setSavepoint(String name) throws SQLException {
		preInvoke();
		try {
         return con.setSavepoint(name);
		} catch (SQLException e) {
			catchInvoke(e);
		}
      return null;
	}

	/**
	 * Methods used to do some works before and during the catch
	 * clause, to prevent the pool that a connection is broken.
	 */
	abstract public void preInvoke() throws SQLException;
	abstract public void catchInvoke(SQLException e) throws SQLException;

}
