/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.db;

import java.sql.*;
import javax.sql.*;

//import org.postgresql.*;

import org.compiere.dbPort.*;

/**
 *  PostgreSQL Database Port
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB_PostgreSQL.java,v 1.16 2004/01/26 06:01:03 jjanke Exp $
 */
public class DB_PostgreSQL implements CompiereDatabase
{
	/**
	 *  PostgreSQL Database
	 */
	public DB_PostgreSQL()
	{
		try
		{
			if (s_driver == null)
				s_driver = new org.postgresql.Driver();
		}
		catch (SQLException e)
		{
			System.err.println(e);
		}
	}   //  DB_PostgreSQL

	/** Driver                  */
	private org.postgresql.Driver   s_driver = null;

	/** Default Port            */
	public static final int         DEFAULT_PORT = 5432;

	/** Statement Converter     */
	private Convert         m_convert = new Convert(Database.DB_POSTGRESQL, null);
	/** Connection String       */
	private String          m_connection;

	/**
	 *  Get Database Name
	 *  @return database short name
	 */
	public String getName()
	{
		return Database.DB_POSTGRESQL;
	}   //  getName

	/**
	 *  Get Database Description
	 *  @return database long name and version
	 */
	public String getDescription()
	{
		return s_driver.toString();
	}   //  getDescription

	/**
	 *  Get Standard JDBC Port
	 *  @return standard port
	 */
	public int getStandardPort()
	{
		return DEFAULT_PORT;
	}   //  getStandardPort

	/**
	 *  Get Database Driver
	 *  @return Driver
	 */
	public java.sql.Driver getDriver()
	{
		return s_driver;
	}   //  getDriver

	/**
	 *  Get Database Connection String.
	 *  Requirements:
	 *      - createdb -E UNICODE compiere
	 *  @param connection Connection Descriptor
	 *  @return connection String
	 */
	public String getConnectionURL (CConnection connection)
	{
		//  jdbc:postgresql://hostname:portnumber/databasename?encoding=UNICODE
		StringBuffer sb = new StringBuffer("jdbc:postgresql:");
		sb.append("//").append(connection.getDbHost())
			.append(":").append(connection.getDbPort())
			.append("/").append(connection.getDbName())
			.append("?encoding=UNICODE");
		m_connection = sb.toString();
		return m_connection;
	}   //  getConnectionString

	/**
	 *  Supports BLOB
	 *  @return true if BLOB is supported
	 */
	public boolean supportsBLOB()
	{
		return false;
	}   //  supportsBLOB

	/**
	 *  String Representation
	 *  @return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("DB_PostgreSQL[");
		sb.append(m_connection)
			.append("]");
		return sb.toString();
	}   //  toString

	/*************************************************************************/

	/**
	 *  Convert an individual Oracle Style statements to target database statement syntax
	 *
	 *  @param oraStatement
	 *  @return converted Statement
	 *  @throws Exception
	 */
	public String convertStatement (String oraStatement)
	{
		String retValue[] = m_convert.convert(oraStatement);
		if (retValue == null)
			throw new IllegalArgumentException
				("DB_PostgreSQL.convertStatement - Not Converted (" + oraStatement + ") - "
					+ m_convert.getConversionError());
		if (retValue.length != 1)
			throw new IllegalArgumentException
				("DB_PostgreSQL.convertStatement - Convert Command Number=" + retValue.length
					+ " (" + oraStatement + ") - " + m_convert.getConversionError());
		//  Diagnostics (show changed, but not if AD_Error
		if (!oraStatement.equals(retValue[0]) && retValue[0].indexOf("AD_Error") == -1)
			System.out.println("PostgreSQL =>" + retValue[0] + "<= <" + oraStatement + ">");
		//
		return retValue[0];
	}   //  convertStatement

	/*************************************************************************/

	/**
	 *  Set the RowID
	 *  @param pstmt
	 *  @param pos
	 *  @param rowID
	 *  @throws SQLException
	 */
	public void setRowID (PreparedStatement pstmt, int pos, Object rowID) throws SQLException
	{
		pstmt.setString (pos, (String)rowID);
	}   //  setRowID

	/**
	 *  Get the RowID
	 *  @param rs
	 *  @param pos
	 *  @return rowID
	 *  @throws SQLException
	 */
	public Object getRowID (java.sql.ResultSet rs, int pos) throws SQLException
	{
		return rs.getString (pos);
	}   //  getRowID

	/**
	 *  Get RowSet
	 * 	@param rs ResultSet
	 *  @return RowSet
	 *  @throws SQLException
	 */
	public RowSet getRowSet (java.sql.ResultSet rs) throws SQLException
	{
		throw new UnsupportedOperationException("PostgreSQL does not support RowSets");
	}	//	getRowSet
	
	/**
	 * 	Get Cached Connection on Server
	 *	@param connection info
	 *	@return connection or null
	 */
	public Connection getCachedConnection (CConnection connection)
	{
		throw new UnsupportedOperationException("Not supported/implemented");
	}

	/**
	 * 	Create DataSource (Client)
	 *	@param connection connection
	 *	@return data dource
	 */
	public DataSource createDataSource(CConnection connection)
	{
		throw new UnsupportedOperationException("Not supported/implemented");
	}

	/**
	 * 	Create Pooled DataSource (Server)
	 *	@param connection connection
	 *	@return data dource
	 */
	public ConnectionPoolDataSource createPoolDataSource(CConnection connection)
	{
		throw new UnsupportedOperationException("Not supported/implemented");
	}

}   //  DB_PostgreSQL
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.db;

import java.sql.*;

import javax.sql.*;

import org.compiere.dbPort.*;

/**
 *  PostgreSQL Database Port
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB_PostgreSQL.java,v 1.17 2004/08/20 23:51:59 jjanke Exp $
 */
public class DB_PostgreSQL implements CompiereDatabase
{
	/**
	 *  PostgreSQL Database
	 */
	public DB_PostgreSQL()
	{
	}   //  DB_PostgreSQL

	/** Driver                  */
	private org.postgresql.Driver   s_driver = null;

	/** Default Port            */
	public static final int         DEFAULT_PORT = 5432;

	/** Statement Converter     */
	private Convert         m_convert = new Convert(Database.DB_POSTGRESQL, null);
	/** Connection String       */
	private String          m_connection;

	/**
	 *  Get Database Name
	 *  @return database short name
	 */
	public String getName()
	{
		return Database.DB_POSTGRESQL;
	}   //  getName

	/**
	 *  Get Database Description
	 *  @return database long name and version
	 */
	public String getDescription()
	{
		return s_driver.toString();
	}   //  getDescription

	/**
	 *  Get Standard JDBC Port
	 *  @return standard port
	 */
	public int getStandardPort()
	{
		return DEFAULT_PORT;
	}   //  getStandardPort

	/**
	 *  Get and register Database Driver
	 *  @return Driver
	 */
	public java.sql.Driver getDriver() throws SQLException
	{
		if (s_driver == null)
		{
			s_driver = new org.postgresql.Driver();
			DriverManager.registerDriver (s_driver);
			DriverManager.setLoginTimeout (Database.CONNECTION_TIMEOUT);
		}
		return s_driver;
	}   //  getDriver

	/**
	 *  Get Database Connection String.
	 *  Requirements:
	 *      - createdb -E UNICODE compiere
	 *  @param connection Connection Descriptor
	 *  @return connection String
	 */
	public String getConnectionURL (CConnection connection)
	{
		//  jdbc:postgresql://hostname:portnumber/databasename?encoding=UNICODE
		StringBuffer sb = new StringBuffer("jdbc:postgresql:");
		sb.append("//").append(connection.getDbHost())
			.append(":").append(connection.getDbPort())
			.append("/").append(connection.getDbName())
			.append("?encoding=UNICODE");
		m_connection = sb.toString();
		return m_connection;
	}   //  getConnectionString

	/**
	 *  Supports BLOB
	 *  @return true if BLOB is supported
	 */
	public boolean supportsBLOB()
	{
		return false;
	}   //  supportsBLOB

	/**
	 *  String Representation
	 *  @return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("DB_PostgreSQL[");
		sb.append(m_connection)
			.append("]");
		return sb.toString();
	}   //  toString

	/**
	 * 	Get Status
	 * 	@return status info
	 */
	public String getStatus()
	{
		return "";
	}	//	getStatus

	/*************************************************************************/

	/**
	 *  Convert an individual Oracle Style statements to target database statement syntax
	 *
	 *  @param oraStatement
	 *  @return converted Statement
	 *  @throws Exception
	 */
	public String convertStatement (String oraStatement)
	{
		String retValue[] = m_convert.convert(oraStatement);
		if (retValue == null)
			throw new IllegalArgumentException
				("DB_PostgreSQL.convertStatement - Not Converted (" + oraStatement + ") - "
					+ m_convert.getConversionError());
		if (retValue.length != 1)
			throw new IllegalArgumentException
				("DB_PostgreSQL.convertStatement - Convert Command Number=" + retValue.length
					+ " (" + oraStatement + ") - " + m_convert.getConversionError());
		//  Diagnostics (show changed, but not if AD_Error
		if (!oraStatement.equals(retValue[0]) && retValue[0].indexOf("AD_Error") == -1)
			System.out.println("PostgreSQL =>" + retValue[0] + "<= <" + oraStatement + ">");
		//
		return retValue[0];
	}   //  convertStatement

	/*************************************************************************/

	/**
	 *  Set the RowID
	 *  @param pstmt
	 *  @param pos
	 *  @param rowID
	 *  @throws SQLException
	 */
	public void setRowID (PreparedStatement pstmt, int pos, Object rowID) throws SQLException
	{
		pstmt.setString (pos, (String)rowID);
	}   //  setRowID

	/**
	 *  Get the RowID
	 *  @param rs
	 *  @param pos
	 *  @return rowID
	 *  @throws SQLException
	 */
	public Object getRowID (java.sql.ResultSet rs, int pos) throws SQLException
	{
		return rs.getString (pos);
	}   //  getRowID

	/**
	 *  Get RowSet
	 * 	@param rs ResultSet
	 *  @return RowSet
	 *  @throws SQLException
	 */
	public RowSet getRowSet (java.sql.ResultSet rs) throws SQLException
	{
		throw new UnsupportedOperationException("PostgreSQL does not support RowSets");
	}	//	getRowSet
	
	/**
	 * 	Get Cached Connection on Server
	 *	@param connection info
	 *  @param autoCommit true if autocommit connection
	 *  @param transactionIsolation Connection transaction level
	 *	@return connection or null
	 */
	public Connection getCachedConnection (CConnection connection, boolean autoCommit, int transactionIsolation)
	{
		throw new UnsupportedOperationException("Not supported/implemented");
	}

	/**
	 * 	Create DataSource (Client)
	 *	@param connection connection
	 *	@return data dource
	 */
	public DataSource getDataSource(CConnection connection)
	{
		throw new UnsupportedOperationException("Not supported/implemented");
	}

	/**
	 * 	Create Pooled DataSource (Server)
	 *	@param connection connection
	 *	@return data dource
	 */
	public ConnectionPoolDataSource createPoolDataSource(CConnection connection)
	{
		throw new UnsupportedOperationException("Not supported/implemented");
	}
	
	/**
	 * 	Get Connection from Driver
	 *	@param connection info
	 *	@return connection or null
	 */
	public Connection getDriverConnection (CConnection connection) throws SQLException
	{
		getDriver();
		return DriverManager.getConnection (getConnectionURL (connection), 
			connection.getDbUid(), connection.getDbPwd());
	}	//	getDriverConnection

	/**
	 * 	Close
	 */
	public void close()
	{
		
	}	//	close

}   //  DB_PostgreSQL
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.db;

import java.sql.*;
import javax.sql.*;

//import org.postgresql.*;

import org.compiere.dbPort.*;

/**
 *  PostgreSQL Database Port
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB_PostgreSQL.java,v 1.16 2004/01/26 06:01:03 jjanke Exp $
 */
public class DB_PostgreSQL implements CompiereDatabase
{
	/**
	 *  PostgreSQL Database
	 */
	public DB_PostgreSQL()
	{
		try
		{
			if (s_driver == null)
				s_driver = new org.postgresql.Driver();
		}
		catch (SQLException e)
		{
			System.err.println(e);
		}
	}   //  DB_PostgreSQL

	/** Driver                  */
	private org.postgresql.Driver   s_driver = null;

	/** Default Port            */
	public static final int         DEFAULT_PORT = 5432;

	/** Statement Converter     */
	private Convert         m_convert = new Convert(Database.DB_POSTGRESQL, null);
	/** Connection String       */
	private String          m_connection;

	/**
	 *  Get Database Name
	 *  @return database short name
	 */
	public String getName()
	{
		return Database.DB_POSTGRESQL;
	}   //  getName

	/**
	 *  Get Database Description
	 *  @return database long name and version
	 */
	public String getDescription()
	{
		return s_driver.toString();
	}   //  getDescription

	/**
	 *  Get Standard JDBC Port
	 *  @return standard port
	 */
	public int getStandardPort()
	{
		return DEFAULT_PORT;
	}   //  getStandardPort

	/**
	 *  Get Database Driver
	 *  @return Driver
	 */
	public java.sql.Driver getDriver()
	{
		return s_driver;
	}   //  getDriver

	/**
	 *  Get Database Connection String.
	 *  Requirements:
	 *      - createdb -E UNICODE compiere
	 *  @param connection Connection Descriptor
	 *  @return connection String
	 */
	public String getConnectionURL (CConnection connection)
	{
		//  jdbc:postgresql://hostname:portnumber/databasename?encoding=UNICODE
		StringBuffer sb = new StringBuffer("jdbc:postgresql:");
		sb.append("//").append(connection.getDbHost())
			.append(":").append(connection.getDbPort())
			.append("/").append(connection.getDbName())
			.append("?encoding=UNICODE");
		m_connection = sb.toString();
		return m_connection;
	}   //  getConnectionString

	/**
	 *  Supports BLOB
	 *  @return true if BLOB is supported
	 */
	public boolean supportsBLOB()
	{
		return false;
	}   //  supportsBLOB

	/**
	 *  String Representation
	 *  @return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("DB_PostgreSQL[");
		sb.append(m_connection)
			.append("]");
		return sb.toString();
	}   //  toString

	/*************************************************************************/

	/**
	 *  Convert an individual Oracle Style statements to target database statement syntax
	 *
	 *  @param oraStatement
	 *  @return converted Statement
	 *  @throws Exception
	 */
	public String convertStatement (String oraStatement)
	{
		String retValue[] = m_convert.convert(oraStatement);
		if (retValue == null)
			throw new IllegalArgumentException
				("DB_PostgreSQL.convertStatement - Not Converted (" + oraStatement + ") - "
					+ m_convert.getConversionError());
		if (retValue.length != 1)
			throw new IllegalArgumentException
				("DB_PostgreSQL.convertStatement - Convert Command Number=" + retValue.length
					+ " (" + oraStatement + ") - " + m_convert.getConversionError());
		//  Diagnostics (show changed, but not if AD_Error
		if (!oraStatement.equals(retValue[0]) && retValue[0].indexOf("AD_Error") == -1)
			System.out.println("PostgreSQL =>" + retValue[0] + "<= <" + oraStatement + ">");
		//
		return retValue[0];
	}   //  convertStatement

	/*************************************************************************/

	/**
	 *  Set the RowID
	 *  @param pstmt
	 *  @param pos
	 *  @param rowID
	 *  @throws SQLException
	 */
	public void setRowID (PreparedStatement pstmt, int pos, Object rowID) throws SQLException
	{
		pstmt.setString (pos, (String)rowID);
	}   //  setRowID

	/**
	 *  Get the RowID
	 *  @param rs
	 *  @param pos
	 *  @return rowID
	 *  @throws SQLException
	 */
	public Object getRowID (java.sql.ResultSet rs, int pos) throws SQLException
	{
		return rs.getString (pos);
	}   //  getRowID

	/**
	 *  Get RowSet
	 * 	@param rs ResultSet
	 *  @return RowSet
	 *  @throws SQLException
	 */
	public RowSet getRowSet (java.sql.ResultSet rs) throws SQLException
	{
		throw new UnsupportedOperationException("PostgreSQL does not support RowSets");
	}	//	getRowSet
	
	/**
	 * 	Get Cached Connection on Server
	 *	@param connection info
	 *	@return connection or null
	 */
	public Connection getCachedConnection (CConnection connection)
	{
		throw new UnsupportedOperationException("Not supported/implemented");
	}

	/**
	 * 	Create DataSource (Client)
	 *	@param connection connection
	 *	@return data dource
	 */
	public DataSource createDataSource(CConnection connection)
	{
		throw new UnsupportedOperationException("Not supported/implemented");
	}

	/**
	 * 	Create Pooled DataSource (Server)
	 *	@param connection connection
	 *	@return data dource
	 */
	public ConnectionPoolDataSource createPoolDataSource(CConnection connection)
	{
		throw new UnsupportedOperationException("Not supported/implemented");
	}

}   //  DB_PostgreSQL
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.db;

import java.sql.*;

import javax.sql.*;

import org.compiere.dbPort.*;

/**
 *  PostgreSQL Database Port
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB_PostgreSQL.java,v 1.17 2004/08/20 23:51:59 jjanke Exp $
 */
public class DB_PostgreSQL implements CompiereDatabase
{
	/**
	 *  PostgreSQL Database
	 */
	public DB_PostgreSQL()
	{
	}   //  DB_PostgreSQL

	/** Driver                  */
	private org.postgresql.Driver   s_driver = null;

	/** Default Port            */
	public static final int         DEFAULT_PORT = 5432;

	/** Statement Converter     */
	private Convert         m_convert = new Convert(Database.DB_POSTGRESQL, null);
	/** Connection String       */
	private String          m_connection;

	/**
	 *  Get Database Name
	 *  @return database short name
	 */
	public String getName()
	{
		return Database.DB_POSTGRESQL;
	}   //  getName

	/**
	 *  Get Database Description
	 *  @return database long name and version
	 */
	public String getDescription()
	{
		return s_driver.toString();
	}   //  getDescription

	/**
	 *  Get Standard JDBC Port
	 *  @return standard port
	 */
	public int getStandardPort()
	{
		return DEFAULT_PORT;
	}   //  getStandardPort

	/**
	 *  Get and register Database Driver
	 *  @return Driver
	 */
	public java.sql.Driver getDriver() throws SQLException
	{
		if (s_driver == null)
		{
			s_driver = new org.postgresql.Driver();
			DriverManager.registerDriver (s_driver);
			DriverManager.setLoginTimeout (Database.CONNECTION_TIMEOUT);
		}
		return s_driver;
	}   //  getDriver

	/**
	 *  Get Database Connection String.
	 *  Requirements:
	 *      - createdb -E UNICODE compiere
	 *  @param connection Connection Descriptor
	 *  @return connection String
	 */
	public String getConnectionURL (CConnection connection)
	{
		//  jdbc:postgresql://hostname:portnumber/databasename?encoding=UNICODE
		StringBuffer sb = new StringBuffer("jdbc:postgresql:");
		sb.append("//").append(connection.getDbHost())
			.append(":").append(connection.getDbPort())
			.append("/").append(connection.getDbName())
			.append("?encoding=UNICODE");
		m_connection = sb.toString();
		return m_connection;
	}   //  getConnectionString

	/**
	 *  Supports BLOB
	 *  @return true if BLOB is supported
	 */
	public boolean supportsBLOB()
	{
		return false;
	}   //  supportsBLOB

	/**
	 *  String Representation
	 *  @return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("DB_PostgreSQL[");
		sb.append(m_connection)
			.append("]");
		return sb.toString();
	}   //  toString

	/**
	 * 	Get Status
	 * 	@return status info
	 */
	public String getStatus()
	{
		return "";
	}	//	getStatus

	/*************************************************************************/

	/**
	 *  Convert an individual Oracle Style statements to target database statement syntax
	 *
	 *  @param oraStatement
	 *  @return converted Statement
	 *  @throws Exception
	 */
	public String convertStatement (String oraStatement)
	{
		String retValue[] = m_convert.convert(oraStatement);
		if (retValue == null)
			throw new IllegalArgumentException
				("DB_PostgreSQL.convertStatement - Not Converted (" + oraStatement + ") - "
					+ m_convert.getConversionError());
		if (retValue.length != 1)
			throw new IllegalArgumentException
				("DB_PostgreSQL.convertStatement - Convert Command Number=" + retValue.length
					+ " (" + oraStatement + ") - " + m_convert.getConversionError());
		//  Diagnostics (show changed, but not if AD_Error
		if (!oraStatement.equals(retValue[0]) && retValue[0].indexOf("AD_Error") == -1)
			System.out.println("PostgreSQL =>" + retValue[0] + "<= <" + oraStatement + ">");
		//
		return retValue[0];
	}   //  convertStatement

	/*************************************************************************/

	/**
	 *  Set the RowID
	 *  @param pstmt
	 *  @param pos
	 *  @param rowID
	 *  @throws SQLException
	 */
	public void setRowID (PreparedStatement pstmt, int pos, Object rowID) throws SQLException
	{
		pstmt.setString (pos, (String)rowID);
	}   //  setRowID

	/**
	 *  Get the RowID
	 *  @param rs
	 *  @param pos
	 *  @return rowID
	 *  @throws SQLException
	 */
	public Object getRowID (java.sql.ResultSet rs, int pos) throws SQLException
	{
		return rs.getString (pos);
	}   //  getRowID

	/**
	 *  Get RowSet
	 * 	@param rs ResultSet
	 *  @return RowSet
	 *  @throws SQLException
	 */
	public RowSet getRowSet (java.sql.ResultSet rs) throws SQLException
	{
		throw new UnsupportedOperationException("PostgreSQL does not support RowSets");
	}	//	getRowSet
	
	/**
	 * 	Get Cached Connection on Server
	 *	@param connection info
	 *  @param autoCommit true if autocommit connection
	 *  @param transactionIsolation Connection transaction level
	 *	@return connection or null
	 */
	public Connection getCachedConnection (CConnection connection, boolean autoCommit, int transactionIsolation)
	{
		throw new UnsupportedOperationException("Not supported/implemented");
	}

	/**
	 * 	Create DataSource (Client)
	 *	@param connection connection
	 *	@return data dource
	 */
	public DataSource getDataSource(CConnection connection)
	{
		throw new UnsupportedOperationException("Not supported/implemented");
	}

	/**
	 * 	Create Pooled DataSource (Server)
	 *	@param connection connection
	 *	@return data dource
	 */
	public ConnectionPoolDataSource createPoolDataSource(CConnection connection)
	{
		throw new UnsupportedOperationException("Not supported/implemented");
	}
	
	/**
	 * 	Get Connection from Driver
	 *	@param connection info
	 *	@return connection or null
	 */
	public Connection getDriverConnection (CConnection connection) throws SQLException
	{
		getDriver();
		return DriverManager.getConnection (getConnectionURL (connection), 
			connection.getDbUid(), connection.getDbPwd());
	}	//	getDriverConnection

	/**
	 * 	Close
	 */
	public void close()
	{
		
	}	//	close

}   //  DB_PostgreSQL
