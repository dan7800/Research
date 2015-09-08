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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

/**
 * A very simple implementation of Statement. When created
 * it is supplied with another Statement to which all
 * of this class' methods delegate their work. 
 */
public abstract class CoreStatement extends JdbcUtil implements Statement {

	protected Statement statement;
	// the Statement which does most of the work.

	public void addBatch(String s) throws SQLException {
		//preInvoke();
		try {
			statement.addBatch(s);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void cancel() throws SQLException {
		//preInvoke();
		try {
			statement.cancel();
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void clearBatch() throws SQLException {
		//preInvoke();
		try {
			statement.clearBatch();
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void clearWarnings() throws SQLException {
		//preInvoke();
		try {
			statement.clearWarnings();
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void close() throws SQLException {
		if (statement != null) {
			statement.close();
		}
	}

	public boolean execute(String s) throws SQLException {
		//preInvoke();
		try {
			return statement.execute(s);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;
	}

	public int[] executeBatch() throws SQLException {
		//preInvoke();
		try {
			return statement.executeBatch();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public ResultSet executeQuery(String s) throws SQLException {
		//preInvoke();
		try {
			return statement.executeQuery(s);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public int executeUpdate(String s) throws SQLException {
		//preInvoke();
		try {
			return statement.executeUpdate(s);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public Connection getConnection() throws SQLException {
		//preInvoke();
		try {
			return statement.getConnection();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public int getFetchDirection() throws SQLException {
		//preInvoke();
		try {
			return statement.getFetchDirection();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public int getFetchSize() throws SQLException {
		//preInvoke();
		try {
			return statement.getFetchSize();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public ResultSet getGeneratedKeys() throws SQLException {
		//preInvoke();
		try {
			return statement.getGeneratedKeys();
		} catch (SQLException e) {
			catchInvoke(e);
		}
                return null;
	}

	public int getMaxFieldSize() throws SQLException {
		//preInvoke();
		try {
			return statement.getMaxFieldSize();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public int getMaxRows() throws SQLException {
		//preInvoke();
		try {
			return statement.getMaxRows();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public boolean getMoreResults() throws SQLException {
		//preInvoke();
		try {
			return statement.getMoreResults();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;
	}

	public int getQueryTimeout() throws SQLException {
		//preInvoke();
		try {
			return statement.getQueryTimeout();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public ResultSet getResultSet() throws SQLException {
		//preInvoke();
		try {
			return statement.getResultSet();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public int getResultSetConcurrency() throws SQLException {
		//preInvoke();
		try {
			return statement.getResultSetConcurrency();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public int getResultSetType() throws SQLException {
		//preInvoke();
		try {
			return statement.getResultSetType();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public int getUpdateCount() throws SQLException {
		//preInvoke();
		try {
			return statement.getUpdateCount();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public SQLWarning getWarnings() throws SQLException {
		//preInvoke();
		try {
			return statement.getWarnings();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public void setCursorName(String name) throws SQLException {
		//preInvoke();
		try {
			statement.setCursorName(name);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setEscapeProcessing(boolean enable) throws SQLException {
		//preInvoke();
		try {
			statement.setEscapeProcessing(enable);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setFetchDirection(int direction) throws SQLException {
		//preInvoke();
		try {
			statement.setFetchDirection(direction);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setFetchSize(int rows) throws SQLException {
		//preInvoke();
		try {
			statement.setFetchSize(rows);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setMaxFieldSize(int max) throws SQLException {
		//preInvoke();
		try {
			statement.setMaxFieldSize(max);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setMaxRows(int max) throws SQLException {
		//preInvoke();
		try {
			statement.setMaxRows(max);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setQueryTimeout(int seconds) throws SQLException {
		//preInvoke();
		try {
			statement.setQueryTimeout(seconds);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	/*
	* Add those following methods to compile on JDK 1.4.
	* Instead those methods are defined in the java.sql.Statement interface
	* only since JDK 1.4.
	*/
	public boolean execute(String sql, int autoGeneratedKeys)
		throws SQLException {
		try {
			return statement.execute(sql, autoGeneratedKeys);
		} catch (SQLException e) {
			catchInvoke(e);
		}
                return false;
	}
	public boolean execute(String sql, int[] columnIndexes)
		throws SQLException {
		try {
			return statement.execute(sql, columnIndexes);
		} catch (SQLException e) {
			catchInvoke(e);
		}
                return false;                
	}
	public boolean execute(String sql, String[] columnNames)
		throws SQLException {
		try {
			return statement.execute(sql, columnNames);
		} catch (SQLException e) {
			catchInvoke(e);
		}
                return false;                
        }
	public int executeUpdate(String sql, int autoGeneratedKeys)
		throws SQLException {
		try {
			return statement.executeUpdate(sql, autoGeneratedKeys);
		} catch (SQLException e) {
			catchInvoke(e);
		}
                return 0;
	}
	public int executeUpdate(String sql, int[] columnIndexes)
		throws SQLException {
		try {
			return statement.executeUpdate(sql, columnIndexes);
		} catch (SQLException e) {
			catchInvoke(e);
		}
                return 0;                
	}
	public int executeUpdate(String sql, String[] columnNames)
		throws SQLException {
		try {
			return statement.executeUpdate(sql, columnNames);
		} catch (SQLException e) {
			catchInvoke(e);
		}
                return 0;                
	}
	public boolean getMoreResults(int current)
		throws SQLException {
		try {
			return statement.getMoreResults(current);
		} catch (SQLException e) {
			catchInvoke(e);
		}
                return false;                
	}
	public int getResultSetHoldability()
		throws SQLException {
		try {
			return statement.getResultSetHoldability();
		} catch (SQLException e) {
			catchInvoke(e);
		}
                return 0;                
	}

	/**
	 * Methods used to do some works before and during the catch
	 * clause, to prevent the pool that a connection is broken.
	 */
	//abstract public void preInvoke() throws SQLException;
	abstract public void catchInvoke(SQLException e) throws SQLException;

}