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

import org.enhydra.jdbc.util.Logger;
import org.enhydra.jdbc.util.JdbcUtil;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * A very simple implementation of PreparedStatement. When created
 * it is supplied with another PreparedStatement to which all
 * of this class' methods delegate their work. 
 */
public abstract class CorePreparedStatement
	extends JdbcUtil
	implements PreparedStatement {

	public PreparedStatement ps;
	// the PreparedStatement which does most of the work.

	public void setLogger(Logger alog) {
		log = alog;
	}

	public void addBatch() throws SQLException {
		//preInvoke();
		try {
			ps.addBatch();
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void clearParameters() throws SQLException {
		//preInvoke();
		try {
			ps.clearParameters();
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public boolean execute() throws SQLException {
		//preInvoke();
		try {
			return ps.execute();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;
	}

	public ResultSet executeQuery() throws SQLException {
		//preInvoke();
		try {
			return ps.executeQuery();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public int executeUpdate() throws SQLException {
		//preInvoke();
		try {
			return ps.executeUpdate();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		//preInvoke();
		try {
			return ps.getMetaData();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public void setArray(int i, Array x) throws SQLException {
		//preInvoke();
		try {
			ps.setArray(i, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setAsciiStream(int parameterIndex, InputStream x, int length)
		throws SQLException {
		//preInvoke();
		try {
			ps.setAsciiStream(parameterIndex, x, length);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setBigDecimal(int parameterIndex, BigDecimal x)
		throws SQLException {
		//preInvoke();
		try {
			ps.setBigDecimal(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setBinaryStream(int parameterIndex, InputStream x, int length)
		throws SQLException {
		//preInvoke();
		try {
			ps.setBinaryStream(parameterIndex, x, length);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setBlob(int i, Blob x) throws SQLException {
		//preInvoke();
		try {
			ps.setBlob(i, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		//preInvoke();
		try {
			ps.setBoolean(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setByte(int parameterIndex, byte x) throws SQLException {
		//preInvoke();
		try {
			ps.setByte(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setBytes(int parameterIndex, byte x[]) throws SQLException {
		//preInvoke();
		try {
			ps.setBytes(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setCharacterStream(
		int parameterIndex,
		Reader reader,
		int length)
		throws SQLException {
		//preInvoke();
		try {
			ps.setCharacterStream(parameterIndex, reader, length);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setClob(int i, Clob x) throws SQLException {
		//preInvoke();
		try {
			ps.setClob(i, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setDate(int parameterIndex, Date x) throws SQLException {
		//preInvoke();
		try {
			ps.setDate(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setDate(int parameterIndex, Date x, Calendar cal)
		throws SQLException {
		//preInvoke();
		try {
			ps.setDate(parameterIndex, x, cal);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setDouble(int parameterIndex, double x) throws SQLException {
		//preInvoke();
		try {
			ps.setDouble(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setFloat(int parameterIndex, float x) throws SQLException {
		//preInvoke();
		try {
			ps.setFloat(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setInt(int parameterIndex, int x) throws SQLException {
		//preInvoke();
		try {
			ps.setInt(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setLong(int parameterIndex, long x) throws SQLException {
		//preInvoke();
		try {
			ps.setLong(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		//preInvoke();
		try {
			ps.setNull(parameterIndex, sqlType);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setNull(int paramIndex, int sqlType, String typeName)
		throws SQLException {
		//preInvoke();
		try {
			ps.setNull(paramIndex, sqlType, typeName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setObject(int parameterIndex, Object x) throws SQLException {
		//preInvoke();
		try {
			ps.setObject(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType)
		throws SQLException {
		//preInvoke();
		try {
			ps.setObject(parameterIndex, x, targetSqlType);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setObject(
		int parameterIndex,
		Object x,
		int targetSqlType,
		int scale)
		throws SQLException {
		//preInvoke();
		try {
			ps.setObject(parameterIndex, x, targetSqlType, scale);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setRef(int i, Ref x) throws SQLException {
		//preInvoke();
		try {
			ps.setRef(i, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setShort(int parameterIndex, short x) throws SQLException {
		//preInvoke();
		try {
			ps.setShort(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setString(int parameterIndex, String x) throws SQLException {
		//preInvoke();
		try {
			ps.setString(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setTime(int parameterIndex, Time x) throws SQLException {
		//preInvoke();
		try {
			ps.setTime(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setTime(int parameterIndex, Time x, Calendar cal)
		throws SQLException {
		//preInvoke();
		try {
			ps.setTime(parameterIndex, x, cal);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setTimestamp(int parameterIndex, Timestamp x)
		throws SQLException {
		//preInvoke();
		try {
			ps.setTimestamp(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
		throws SQLException {
		//preInvoke();
		try {
			ps.setTimestamp(parameterIndex, x, cal);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setUnicodeStream(int parameterIndex, InputStream x, int length)
		throws SQLException {
		//preInvoke();
		try {
			ps.setUnicodeStream(parameterIndex, x, length);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	// From Statement

	public void close() throws SQLException {
		if (ps != null) {
			ps.close();
		}
	}

	public int[] executeBatch() throws SQLException {
		//preInvoke();
		try {
			return ps.executeBatch();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public int getMaxFieldSize() throws SQLException {
		//preInvoke();
		try {
			return ps.getMaxFieldSize();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public void setMaxFieldSize(int max) throws SQLException {
		//preInvoke();
		try {
			ps.setMaxFieldSize(max);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public int getMaxRows() throws SQLException {
		//preInvoke();
		try {
			return ps.getMaxRows();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public void setMaxRows(int max) throws SQLException {
		//preInvoke();
		try {
			ps.setMaxRows(max);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setEscapeProcessing(boolean enable) throws SQLException {
		//preInvoke();
		try {
			ps.setEscapeProcessing(enable);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public int getQueryTimeout() throws SQLException {
		//preInvoke();
		try {
			return ps.getQueryTimeout();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public void setQueryTimeout(int seconds) throws SQLException {
		//preInvoke();
		try {
			ps.setQueryTimeout(seconds);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void cancel() throws SQLException {
		//preInvoke();
		try {
			ps.cancel();
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public SQLWarning getWarnings() throws SQLException {
		//preInvoke();
		try {
			return ps.getWarnings();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public void clearWarnings() throws SQLException {
		//preInvoke();
		try {
			ps.clearWarnings();
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setCursorName(String name) throws SQLException {
		//preInvoke();
		try {
			ps.setCursorName(name);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public ResultSet getResultSet() throws SQLException {
		//preInvoke();
		try {
			return ps.getResultSet();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public int getUpdateCount() throws SQLException {
		//preInvoke();
		try {
			return ps.getUpdateCount();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public boolean getMoreResults() throws SQLException {
		//preInvoke();
		try {
			return ps.getMoreResults();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;
	}

	public void setFetchDirection(int direction) throws SQLException {
		//preInvoke();
		try {
			ps.setFetchDirection(direction);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public int getFetchDirection() throws SQLException {
		//preInvoke();
		try {
			return ps.getFetchDirection();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public void setFetchSize(int rows) throws SQLException {
		//preInvoke();
		try {
			ps.setFetchSize(rows);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public int getFetchSize() throws SQLException {
		//preInvoke();
		try {
			return ps.getFetchSize();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public int getResultSetConcurrency() throws SQLException {
		//preInvoke();
		try {
			return ps.getResultSetConcurrency();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public int getResultSetType() throws SQLException {
		//preInvoke();
		try {
			return ps.getResultSetType();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public Connection getConnection() throws SQLException {
		//preInvoke();
		try {
			return ps.getConnection();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public void clearBatch() throws SQLException {
		//preInvoke();
		try {
			ps.clearBatch();
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void addBatch(String s) throws SQLException {
		//preInvoke();
		try {
			ps.addBatch(s);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public boolean execute(String s) throws SQLException {
		//preInvoke();
		try {
			return ps.execute(s);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;
	}

	public ResultSet executeQuery(String s) throws SQLException {
		//preInvoke();
		try {
			return ps.executeQuery(s);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public int executeUpdate(String s) throws SQLException {
		//preInvoke();
		try {
			return ps.executeUpdate(s);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	/*
	* Add those following methods to compile on JDK 1.4.
	* Instead those methods are defined in the java.sql.PreparedStatement interface
	* only since JDK 1.4.
	*/
	// java.sql.Statements methods
	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        	try {
			return ps.execute(sql, autoGeneratedKeys);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;
	}
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		try {
			return ps.execute(sql, columnIndexes);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;
	}
	public boolean execute(String sql, String[] columnNames) throws SQLException {
		try {
			return ps.execute(sql, columnNames);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;
	}
	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		try {
			return ps.executeUpdate(sql, autoGeneratedKeys);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}
	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		try {
			return ps.executeUpdate(sql, columnIndexes);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}
	public int executeUpdate(String sql, String[] columnNames) throws SQLException {
		try {
			return ps.executeUpdate(sql, columnNames);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}
	public java.sql.ResultSet getGeneratedKeys() throws SQLException {
		try {
			return ps.getGeneratedKeys();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public boolean getMoreResults(int current) throws SQLException {
		try {
			return ps.getMoreResults(current);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;
	}
	public int getResultSetHoldability() throws SQLException {
		try {
			return ps.getResultSetHoldability();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}
	// java.sql.PreparedStatement methods
	public java.sql.ParameterMetaData getParameterMetaData() throws SQLException {
		try {
			return ps.getParameterMetaData();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public void setURL(int parameterIndex, java.net.URL x) throws SQLException {
		try {
			ps.setURL(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	/**
	 * Methods used to do some works before and during the catch
	 * clause, to prevent the pool that a connection is broken.
	 */
	abstract public void preInvoke() throws SQLException;
	abstract public void catchInvoke(SQLException e) throws SQLException;

}