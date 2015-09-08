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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.CallableStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * A very simple implementation of Statement. When created
 * it is supplied with another Statement to which all
 * of this class' methods delegate their work. 
 */
public abstract class CoreCallableStatement
	extends JdbcUtil
	implements CallableStatement {

	// the CallableStatement which does most of the work.
	protected CallableStatement cs;

	public Array getArray(int i) throws SQLException {
		preInvoke();
		try {
			return cs.getArray(i);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public java.math.BigDecimal getBigDecimal(int i) throws SQLException {
		preInvoke();
		try {
			return cs.getBigDecimal(i);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public java.math.BigDecimal getBigDecimal(int i, int scale)
		throws SQLException {
		preInvoke();
		try {
			return cs.getBigDecimal(i);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public Blob getBlob(int i) throws SQLException {
		preInvoke();
		try {
			return cs.getBlob(i);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public boolean getBoolean(int i) throws SQLException {
		preInvoke();
		try {
			return cs.getBoolean(i);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;
	}
	public byte getByte(int i) throws SQLException {
		preInvoke();
		try {
			return cs.getByte(i);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}
	public byte[] getBytes(int i) throws SQLException {
		preInvoke();
		try {
			return cs.getBytes(i);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public Clob getClob(int i) throws SQLException {
		preInvoke();
		try {
			return cs.getClob(i);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public Date getDate(int i) throws SQLException {
		preInvoke();
		try {
			return cs.getDate(i);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public Date getDate(int i, java.util.Calendar cal) throws SQLException {
		preInvoke();
		try {
			return cs.getDate(i, cal);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public double getDouble(int i) throws SQLException {
		preInvoke();
		try {
			return cs.getDouble(i);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}
	public float getFloat(int i) throws SQLException {
		preInvoke();
		try {
			return cs.getFloat(i);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}
	public int getInt(int i) throws SQLException {
		preInvoke();
		try {
			return cs.getInt(i);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}
	public long getLong(int i) throws SQLException {
		preInvoke();
		try {
			return cs.getLong(i);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}
	public Object getObject(int i) throws SQLException {
		preInvoke();
		try {
			return cs.getObject(i);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public Object getObject(int i, java.util.Map map) throws SQLException {
		preInvoke();
		try {
			return cs.getObject(i, map);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public Ref getRef(int i) throws SQLException {
		preInvoke();
		try {
			return cs.getRef(i);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public short getShort(int i) throws SQLException {
		preInvoke();
		try {
			return cs.getShort(i);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}
	public java.lang.String getString(int i) throws SQLException {
		preInvoke();
		try {
			return cs.getString(i);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public java.sql.Time getTime(int i) throws SQLException {
		preInvoke();
		try {
			return cs.getTime(i);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public java.sql.Time getTime(int i, java.util.Calendar cal)
		throws SQLException {
		preInvoke();
		try {
			return cs.getTime(i, cal);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public java.sql.Timestamp getTimestamp(int i) throws SQLException {
		preInvoke();
		try {
			return cs.getTimestamp(i);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public java.sql.Timestamp getTimestamp(int i, java.util.Calendar cal)
		throws SQLException {
		preInvoke();
		try {
			return cs.getTimestamp(i, cal);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public void registerOutParameter(int i, int sqlType) throws SQLException {
		preInvoke();
		try {
			cs.registerOutParameter(i, sqlType);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void registerOutParameter(int i, int sqlType, int scale)
		throws SQLException {
		preInvoke();
		try {
			cs.registerOutParameter(i, sqlType, scale);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void registerOutParameter(int i, int sqlType, String typeName)
		throws SQLException {
		preInvoke();
		try {
			cs.registerOutParameter(i, sqlType, typeName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public boolean wasNull() throws SQLException {
		preInvoke();
		try {
			return cs.wasNull();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;
	}

	public void addBatch() throws SQLException {
		preInvoke();
		try {
			cs.addBatch();
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void clearParameters() throws SQLException {
		preInvoke();
		try {
			cs.clearParameters();
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public boolean execute() throws SQLException {
		preInvoke();
		try {
			return cs.execute();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;
	}

	public ResultSet executeQuery() throws SQLException {
		preInvoke();
		try {
			return cs.executeQuery();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public int executeUpdate() throws SQLException {
		preInvoke();
		try {
			return cs.executeUpdate();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		preInvoke();
		try {
			return cs.getMetaData();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public void setArray(int i, Array x) throws SQLException {
		preInvoke();
		try {
			cs.setArray(i, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setAsciiStream(int parameterIndex, InputStream x, int length)
		throws SQLException {
		preInvoke();
		try {
			cs.setAsciiStream(parameterIndex, x, length);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setBigDecimal(int parameterIndex, BigDecimal x)
		throws SQLException {
		preInvoke();
		try {
			cs.setBigDecimal(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setBinaryStream(int parameterIndex, InputStream x, int length)
		throws SQLException {
		preInvoke();
		try {
			cs.setBinaryStream(parameterIndex, x, length);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setBlob(int i, Blob x) throws SQLException {
		preInvoke();
		try {
			cs.setBlob(i, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		preInvoke();
		try {
			cs.setBoolean(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setByte(int parameterIndex, byte x) throws SQLException {
		preInvoke();
		try {
			cs.setByte(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setBytes(int parameterIndex, byte x[]) throws SQLException {
		preInvoke();
		try {
			cs.setBytes(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setCharacterStream(
		int parameterIndex,
		Reader reader,
		int length)
		throws SQLException {
		preInvoke();
		try {
			cs.setCharacterStream(parameterIndex, reader, length);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setClob(int i, Clob x) throws SQLException {
		preInvoke();
		try {
			cs.setClob(i, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setDate(int parameterIndex, Date x) throws SQLException {
		preInvoke();
		try {
			cs.setDate(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setDate(int parameterIndex, Date x, Calendar cal)
		throws SQLException {
		preInvoke();
		try {
			cs.setDate(parameterIndex, x, cal);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setDouble(int parameterIndex, double x) throws SQLException {
		preInvoke();
		try {
			cs.setDouble(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setFloat(int parameterIndex, float x) throws SQLException {
		preInvoke();
		try {
			cs.setFloat(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setInt(int parameterIndex, int x) throws SQLException {
		preInvoke();
		try {
			cs.setInt(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setLong(int parameterIndex, long x) throws SQLException {
		preInvoke();
		try {
			cs.setLong(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		preInvoke();
		try {
			cs.setNull(parameterIndex, sqlType);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setNull(int paramIndex, int sqlType, String typeName)
		throws SQLException {
		preInvoke();
		try {
			cs.setNull(paramIndex, sqlType, typeName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setObject(int parameterIndex, Object x) throws SQLException {
		preInvoke();
		try {
			cs.setObject(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType)
		throws SQLException {
		preInvoke();
		try {
			cs.setObject(parameterIndex, x, targetSqlType);
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
		preInvoke();
		try {
			cs.setObject(parameterIndex, x, targetSqlType, scale);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setRef(int i, Ref x) throws SQLException {
		preInvoke();
		try {
			cs.setRef(i, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setShort(int parameterIndex, short x) throws SQLException {
		preInvoke();
		try {
			cs.setShort(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setString(int parameterIndex, String x) throws SQLException {
		preInvoke();
		try {
			cs.setString(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setTime(int parameterIndex, Time x) throws SQLException {
		preInvoke();
		try {
			cs.setTime(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setTime(int parameterIndex, Time x, Calendar cal)
		throws SQLException {
		preInvoke();
		try {
			cs.setTime(parameterIndex, x, cal);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setTimestamp(int parameterIndex, Timestamp x)
		throws SQLException {
		preInvoke();
		try {
			cs.setTimestamp(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
		throws SQLException {
		preInvoke();
		try {
			cs.setTimestamp(parameterIndex, x, cal);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setUnicodeStream(int parameterIndex, InputStream x, int length)
		throws SQLException {
		preInvoke();
		try {
			cs.setUnicodeStream(parameterIndex, x, length);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	// From Statement

	public void close() throws SQLException {
		if (cs != null) {
			cs.close();
		}
	}

	public int[] executeBatch() throws SQLException {
		preInvoke();
		try {
			return cs.executeBatch();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public int getMaxFieldSize() throws SQLException {
		preInvoke();
		try {
			return cs.getMaxFieldSize();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public void setMaxFieldSize(int max) throws SQLException {
		preInvoke();
		try {
			cs.setMaxFieldSize(max);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public int getMaxRows() throws SQLException {
		preInvoke();
		try {
			return cs.getMaxRows();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public void setMaxRows(int max) throws SQLException {
		preInvoke();
		try {
			cs.setMaxRows(max);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setEscapeProcessing(boolean enable) throws SQLException {
		preInvoke();
		try {
			cs.setEscapeProcessing(enable);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public int getQueryTimeout() throws SQLException {
		preInvoke();
		try {
			return cs.getQueryTimeout();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public void setQueryTimeout(int seconds) throws SQLException {
		preInvoke();
		try {
			cs.setQueryTimeout(seconds);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void cancel() throws SQLException {
		preInvoke();
		try {
			cs.cancel();
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public SQLWarning getWarnings() throws SQLException {
		preInvoke();
		try {
			return cs.getWarnings();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public void clearWarnings() throws SQLException {
		preInvoke();
		try {
			cs.clearWarnings();
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void setCursorName(String name) throws SQLException {
		preInvoke();
		try {
			cs.setCursorName(name);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public ResultSet getResultSet() throws SQLException {
		preInvoke();
		try {
			return cs.getResultSet();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public int getUpdateCount() throws SQLException {
		preInvoke();
		try {
			return cs.getUpdateCount();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public boolean getMoreResults() throws SQLException {
		preInvoke();
		try {
			return cs.getMoreResults();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;
	}

	public void setFetchDirection(int direction) throws SQLException {
		preInvoke();
		try {
			cs.setFetchDirection(direction);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public int getFetchDirection() throws SQLException {
		preInvoke();
		try {
			return cs.getFetchDirection();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public void setFetchSize(int rows) throws SQLException {
		preInvoke();
		try {
			cs.setFetchSize(rows);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public int getFetchSize() throws SQLException {
		preInvoke();
		try {
			return cs.getFetchSize();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public int getResultSetConcurrency() throws SQLException {
		preInvoke();
		try {
			return cs.getResultSetConcurrency();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public int getResultSetType() throws SQLException {
		preInvoke();
		try {
			return cs.getResultSetType();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	public Connection getConnection() throws SQLException {
		preInvoke();
		try {
			return cs.getConnection();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public void clearBatch() throws SQLException {
		preInvoke();
		try {
			cs.clearBatch();
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public void addBatch(String s) throws SQLException {
		preInvoke();
		try {
			cs.addBatch(s);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	public boolean execute(String s) throws SQLException {
		preInvoke();
		try {
			return cs.execute(s);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;
	}

	public ResultSet executeQuery(String s) throws SQLException {
		preInvoke();
		try {
			return cs.executeQuery(s);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}

	public int executeUpdate(String s) throws SQLException {
		preInvoke();
		try {
			return cs.executeUpdate(s);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}

	/*
	* Add those following methods to compile on JDK 1.4.
	* Instead those methods are defined in the java.sql.CallableStatement interface
	* only since JDK 1.4.
	*/

	// java.sql.Statement methods
	public boolean execute(String sql, int autoGeneratedKeys)
		throws SQLException {
		preInvoke();
		try {
			return cs.execute(sql, autoGeneratedKeys);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;
	}
	public boolean execute(String sql, int[] columnIndexes)
		throws SQLException {
		preInvoke();
		try {
			return cs.execute(sql, columnIndexes);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;
	}
	public boolean execute(String sql, String[] columnNames)
		throws SQLException {
		preInvoke();
		try {
			return cs.execute(sql, columnNames);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;
	}
	public int executeUpdate(String sql, int autoGeneratedKeys)
		throws SQLException {
		preInvoke();
		try {
			return cs.executeUpdate(sql, autoGeneratedKeys);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}
	public int executeUpdate(String sql, int[] columnIndexes)
		throws SQLException {
		preInvoke();
		try {
			return cs.executeUpdate(sql, columnIndexes);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}
	public int executeUpdate(String sql, String[] columnNames)
		throws SQLException {
		preInvoke();
		try {
			return cs.executeUpdate(sql, columnNames);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}
	public java.sql.ResultSet getGeneratedKeys() throws SQLException {
		preInvoke();
		try {
			return cs.getGeneratedKeys();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public boolean getMoreResults(int current) throws SQLException {
		preInvoke();
		try {
			return cs.getMoreResults(current);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;

	}
	public int getResultSetHoldability() throws SQLException {
		preInvoke();
		try {
			return cs.getResultSetHoldability();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;

	}
	// java.sql.PreparedStatement methods
	public java.sql.ParameterMetaData getParameterMetaData()
		throws SQLException {
		preInvoke();
		try {
			return cs.getParameterMetaData();
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;

	}
	public void setURL(int parameterIndex, java.net.URL x)
		throws SQLException {
		preInvoke();
		try {
			cs.setURL(parameterIndex, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}

	// java.sql.CallableStatement methods
	public java.sql.Array getArray(String parameterName) throws SQLException {
		preInvoke();
		try {
			return cs.getArray(parameterName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;

	}
	public java.math.BigDecimal getBigDecimal(String parameterName)
		throws SQLException {
		preInvoke();
		try {
			return cs.getBigDecimal(parameterName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public java.sql.Blob getBlob(String parameterName) throws SQLException {
		preInvoke();
		try {
			return cs.getBlob(parameterName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public boolean getBoolean(String parameterName) throws SQLException {
		preInvoke();
		try {
			return cs.getBoolean(parameterName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return false;
	}
	public byte getByte(String parameterName) throws SQLException {
		preInvoke();
		try {
			return cs.getByte(parameterName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}
	public byte[] getBytes(String parameterName) throws SQLException {
		preInvoke();
		try {
			return cs.getBytes(parameterName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public java.sql.Clob getClob(String parameterName) throws SQLException {
		preInvoke();
		try {
			return cs.getClob(parameterName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public java.sql.Date getDate(String parameterName) throws SQLException {
		preInvoke();
		try {
			return cs.getDate(parameterName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public java.sql.Date getDate(String parameterName, java.util.Calendar cal)
		throws SQLException {
		preInvoke();
		try {
			return cs.getDate(parameterName, cal);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public double getDouble(String parameterName) throws SQLException {
		preInvoke();
		try {
			return cs.getDouble(parameterName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}
	public float getFloat(String parameterName) throws SQLException {
		preInvoke();
		try {
			return cs.getFloat(parameterName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}
	public int getInt(String parameterName) throws SQLException {
		preInvoke();
		try {
			return cs.getInt(parameterName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}
	public long getLong(String parameterName) throws SQLException {
		preInvoke();
		try {
			return cs.getLong(parameterName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}
	public Object getObject(String parameterName) throws SQLException {
		preInvoke();
		try {
			return cs.getObject(parameterName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public Object getObject(String parameterName, java.util.Map map)
		throws SQLException {
		preInvoke();
		try {
			return cs.getObject(parameterName, map);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public java.sql.Ref getRef(String parameterName) throws SQLException {
		preInvoke();
		try {
			return cs.getRef(parameterName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public short getShort(String parameterName) throws SQLException {
		preInvoke();
		try {
			return cs.getShort(parameterName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return 0;
	}
	public String getString(String parameterName) throws SQLException {
		preInvoke();
		try {
			return cs.getString(parameterName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public java.sql.Time getTime(String parameterName) throws SQLException {
		preInvoke();
		try {
			return cs.getTime(parameterName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public java.sql.Time getTime(String parameterName, java.util.Calendar cal)
		throws SQLException {
		preInvoke();
		try {
			return cs.getTime(parameterName, cal);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public java.sql.Timestamp getTimestamp(String parameterName)
		throws SQLException {
		preInvoke();
		try {
			return cs.getTimestamp(parameterName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public java.sql.Timestamp getTimestamp(
		String parameterName,
		java.util.Calendar cal)
		throws SQLException {
		preInvoke();
		try {
			return cs.getTimestamp(parameterName, cal);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public java.net.URL getURL(int parameterIndex) throws SQLException {
		preInvoke();
		try {
			return cs.getURL(parameterIndex);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public java.net.URL getURL(String parameterName) throws SQLException {
		preInvoke();
		try {
			return cs.getURL(parameterName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
		return null;
	}
	public void registerOutParameter(String parameterName, int sqlType)
		throws SQLException {
		preInvoke();
		try {
			cs.registerOutParameter(parameterName, sqlType);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void registerOutParameter(
		String parameterName,
		int sqlType,
		int scale)
		throws SQLException {
		preInvoke();
		try {
			cs.registerOutParameter(parameterName, sqlType, scale);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void registerOutParameter(
		String parameterName,
		int sqlType,
		String typeName)
		throws SQLException {
		preInvoke();
		try {
			cs.registerOutParameter(parameterName, sqlType, typeName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setAsciiStream(
		String parameterName,
		java.io.InputStream x,
		int length)
		throws SQLException {
		preInvoke();
		try {
			cs.setAsciiStream(parameterName, x, length);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setBigDecimal(String parameterName, java.math.BigDecimal x)
		throws SQLException {
		preInvoke();
		try {
			cs.setBigDecimal(parameterName, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setBinaryStream(
		String parameterName,
		java.io.InputStream x,
		int length)
		throws SQLException {
		preInvoke();
		try {
			cs.setBinaryStream(parameterName, x, length);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setBoolean(String parameterName, boolean x)
		throws SQLException {
		preInvoke();
		try {
			cs.setBoolean(parameterName, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setByte(String parameterName, byte x) throws SQLException {
		preInvoke();
		try {
			cs.setByte(parameterName, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setBytes(String parameterName, byte[] x) throws SQLException {
		preInvoke();
		try {
			cs.setBytes(parameterName, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setCharacterStream(
		String parameterName,
		java.io.Reader reader,
		int length)
		throws SQLException {
		preInvoke();
		try {
			cs.setCharacterStream(parameterName, reader, length);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setDate(String parameterName, java.sql.Date x)
		throws SQLException {
		preInvoke();
		try {
			cs.setDate(parameterName, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setDate(
		String parameterName,
		java.sql.Date x,
		java.util.Calendar cal)
		throws SQLException {
		preInvoke();
		try {
			cs.setDate(parameterName, x, cal);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setDouble(String parameterName, double x) throws SQLException {
		preInvoke();
		try {
			cs.setDouble(parameterName, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setFloat(String parameterName, float x) throws SQLException {
		preInvoke();
		try {
			cs.setFloat(parameterName, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setInt(String parameterName, int x) throws SQLException {
		preInvoke();
		try {
			cs.setInt(parameterName, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setLong(String parameterName, long x) throws SQLException {
	        preInvoke();
		try {
			cs.setLong(parameterName, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setNull(String parameterName, int sqlType)
		throws SQLException {
		preInvoke();
		try {
			cs.setNull(parameterName, sqlType);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setNull(String parameterName, int sqlType, String typeName)
		throws SQLException {
		preInvoke();
		try {
			cs.setNull(parameterName, sqlType, typeName);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setObject(String parameterName, Object x) throws SQLException {
		preInvoke();
		try {
			cs.setObject(parameterName, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setObject(String parameterName, Object x, int targetSqlType)
		throws SQLException {
		preInvoke();
		try {
			cs.setObject(parameterName, x, targetSqlType);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setObject(
		String parameterName,
		Object x,
		int targetSqlType,
		int sacle)
		throws SQLException {
		preInvoke();
		try {
			cs.setObject(parameterName, x, targetSqlType, sacle);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setShort(String parameterName, short x) throws SQLException {
		preInvoke();
		try {
			cs.setShort(parameterName, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setString(String parameterName, String x) throws SQLException {
		preInvoke();
		try {
			cs.setString(parameterName, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setTime(String parameterName, java.sql.Time x)
		throws SQLException {
		preInvoke();
		try {
			cs.setTime(parameterName, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setTime(
		String parameterName,
		java.sql.Time x,
		java.util.Calendar cal)
		throws SQLException {
		preInvoke();
		try {
			cs.setTime(parameterName, x, cal);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setTimestamp(String parameterName, java.sql.Timestamp x)
		throws SQLException {
		preInvoke();
		try {
			cs.setTimestamp(parameterName, x);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setTimestamp(
		String parameterName,
		java.sql.Timestamp x,
		java.util.Calendar cal)
		throws SQLException {
		preInvoke();
		try {
			cs.setTimestamp(parameterName, x, cal);
		} catch (SQLException e) {
			catchInvoke(e);
		}
	}
	public void setURL(String parameterName, java.net.URL x)
		throws SQLException {
		preInvoke();
		try {
			cs.setURL(parameterName, x);
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
