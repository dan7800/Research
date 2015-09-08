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

import org.compiere.Compiere;
import org.compiere.util.DB;

import oracle.jdbc.*;
import oracle.jdbc.pool.*;
import oracle.jdbc.rowset.OracleCachedRowSet;

/**
 *  Oracle Database Port
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB_Oracle.java,v 1.21 2004/01/26 06:01:02 jjanke Exp $
 */
public class DB_Oracle implements CompiereDatabase
{
	/**
	 *  Oracle Database
	 */
	public DB_Oracle()
	{
	}   //  DB_Oracle

	/** Static Driver           */
	private OracleDriver        s_driver = new OracleDriver();

	/** Default Port            */
	public static final int DEFAULT_PORT = 1521;
	/** Default Connection Manager Port */
	public static final int DEFAULT_CM_PORT = 1630;

	/** Connection String       */
	private String          	m_connectionURL;


	private OracleDataSource				m_ds = null;
	private OracleConnectionPoolDataSource	m_cpds = null;
	private OracleConnectionCacheImpl		m_cache = null;


	/**
	 *  Get Database Name
	 *  @return database short name
	 */
	public String getName()
	{
		return Database.DB_ORACLE;
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
	public Driver getDriver()
	{
		return s_driver;
	}   //  getDriver

	/**
	 *  Get Database Connection String.
	 *  <pre>
	 *  Timing:
	 *  - CM with source_route not in address_list  = 28.5 sec
	 *  - CM with source_route in address_list      = 58.0 sec
	 *  - direct    = 4.3-8 sec  (no real difference if on other box)
	 *  - bequeath  = 3.4-8 sec
	 *  </pre>
	 *  @param connection Connection Descriptor
	 *  @return connection String
	 */
	public String getConnectionURL (CConnection connection)
	{
		StringBuffer sb = null;
		//  Server Connections (bequeath)
		if (connection.isBequeath())
		{
			sb = new StringBuffer ("jdbc:oracle:oci8:@");
			//  bug: does not work if there is more than one db instance - use Net8
		//	sb.append(connection.getDbName());
		}
		else        //  thin driver
		{
			sb = new StringBuffer ("jdbc:oracle:thin:@");
			//  direct connection
			if (connection.isViaFirewall())
			{
				//  (description=(address_list=
				//  ( (source_route=yes)
				//    (address=(protocol=TCP)(host=cmhost)(port=1630))
				//    (address=(protocol=TCP)(host=dev)(port=1521))
				//  (connect_data=(service_name=dev1.compiere.org)))
				sb.append("(DESCRIPTION=(ADDRESS_LIST=")
					.append("(SOURCE_ROUTE=YES)")
					.append("(ADDRESS=(PROTOCOL=TCP)(HOST=").append(connection.getFwHost())
						.append(")(PORT=").append(connection.getFwPort()).append("))")
					.append("(ADDRESS=(PROTOCOL=TCP)(HOST=").append(connection.getDbHost())
						.append(")(PORT=").append(connection.getDbPort()).append(")))")
					.append("(CONNECT_DATA=(SERVICE_NAME=").append(connection.getDbName()).append(")))");
			}
			else
			{
				//  dev:1521:dev1
				sb.append(connection.getDbHost())
					.append(":").append(connection.getDbPort())
					.append(":").append(connection.getDbName());
			}
		}
		m_connectionURL = sb.toString();
		return m_connectionURL;
	}   //  getConnectionString

	/**
	 *  Supports BLOB
	 *  @return true if BLOB is supported
	 */
	public boolean supportsBLOB()
	{
		return true;
	}   //  supportsBLOB

	/**
	 *  String Representation
	 *  @return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("DB_Oracle[");
		sb.append(m_connectionURL)
			.append("]");
		return sb.toString();
	}   //  toString

	/*************************************************************************/

	/**
	 *  Convert an individual Oracle Style statements to target database statement syntax.
	 *  @param oraStatement oracle statement
	 *  @return converted Statement oracle statement
	 *  @throws Exception
	 */
	public String convertStatement (String oraStatement)
	{
		return oraStatement;
	}   //  convertStatement

	/*************************************************************************/

	/**
	 *  Set the RowID
	 *  @param pstmt statement
	 *  @param pos position in result set
	 *  @param rowID ROWID
	 *  @throws SQLException
	 */
	public void setRowID (PreparedStatement pstmt, int pos, Object rowID) throws SQLException
	{
	//	((OraclePreparedStatement)pstmt).setROWID (pos, (ROWID)rowID);
		pstmt.setString(pos, rowID.toString());
	}   //  setRowID

	/**
	 *  Get the RowID
	 *  @param rs result set
	 *  @param pos psoition in result set
	 *  @return rowID ROWID
	 *  @throws SQLException
	 */
	public Object getRowID (ResultSet rs, int pos) throws SQLException
	{
	//	return ((OracleResultSet)rs).getROWID (pos);
		return rs.getString (pos);
	}   //  getRowID

	/**
	 *	Get RowSet
	 *	@param rs ResultSet
	 *	@return RowSet
	 *	@throws SQLException
	 */
	public RowSet getRowSet (ResultSet rs) throws SQLException
	{
		OracleCachedRowSet rowSet = new OracleCachedRowSet ();
		rowSet.populate (rs);
		return rowSet;
	}	//	getRowSet

	/**
	 * 	Create DataSource (for Clinet)
	 *	@param connection connection
	 *	@return data dource
	 */
	public DataSource createDataSource(CConnection connection)
	{
		try
		{
			m_ds = new OracleDataSource();
			m_ds.setDriverType("thin");
			m_ds.setNetworkProtocol("tcp");
			m_ds.setServerName(connection.getDbHost());
			m_ds.setDatabaseName(connection.getDbName());
			m_ds.setPortNumber(connection.getDbPort());
			m_ds.setUser(connection.getDbUid());
			m_ds.setPassword(connection.getDbPwd());
			//
			m_ds.setDescription("CompiereDS");
			m_ds.setImplicitCachingEnabled(true);
			m_ds.setExplicitCachingEnabled(true);
			m_ds.setMaxStatements(200);
			//
			//	test
			Connection con = m_ds.getConnection();
			con.close();
			//
			System.out.println(m_ds.getDataSourceName()
				+ ",ExplCache=" + m_ds.getExplicitCachingEnabled()
				+ ",ImplCache=" + m_ds.getImplicitCachingEnabled()
				+ ",MaxStatements=" + m_ds.getMaxStatements()
			//	+ ",Ref=" + ods.getReference()
			);
			//
			return m_ds;
		}
		catch (Exception e)
		{
		//	System.err.println ("DB_Oracle.getDataSource");
			e.printStackTrace();
		}
		return null;	
	}	//	getDataSource

	/**
	 * 	Create Pooled DataSource (Server)
	 *	@param connection connection
	 *	@return data dource
	 */
	public ConnectionPoolDataSource createPoolDataSource (CConnection connection)
	{
		try
		{
			m_cpds = new OracleConnectionPoolDataSource();  
			m_cpds.setDriverType("thin");
			m_cpds.setNetworkProtocol("tcp");
			m_cpds.setServerName(connection.getDbHost());
			m_cpds.setDatabaseName(connection.getDbName());
			m_cpds.setPortNumber(connection.getDbPort());
			m_cpds.setUser(connection.getDbUid());
			m_cpds.setPassword(connection.getDbPwd());
			//
			m_cpds.setDescription("CompierePoolDS");
			m_cpds.setImplicitCachingEnabled(true);
			m_cpds.setExplicitCachingEnabled(true);
			m_cpds.setMaxStatements(200);
			//	test
			PooledConnection pc = m_cpds.getPooledConnection();
			Connection conn = pc.getConnection();
			conn.close();
			//
			System.out.println(m_cpds.getDataSourceName()
				+ ",ExplCache=" + m_cpds.getExplicitCachingEnabled()
				+ ",ImplCache=" + m_cpds.getImplicitCachingEnabled()
				+ ",MaxStatements=" + m_cpds.getMaxStatements()
			//	+ ",Ref=" + m_cpds.getReference()
			);
			//
			return m_cpds;
		}
		catch (Exception e)
		{
		//	System.err.println ("DB_Oracle.getPoolDataSource");
			e.printStackTrace();
		}
		return null;	
	}	//	getPoolDataSource

	/**
	 * 	Get Cached Connection
	 *	@param connection info
	 *	@return connection or null
	 */
	public Connection getCachedConnection (CConnection connection)
	{
		try
		{
			if (m_cache == null)
			{
				if (m_cpds == null)
					createPoolDataSource(connection);	
			
				m_cache = new OracleConnectionCacheImpl(m_cpds);
				m_cache.setMinLimit(1);
				m_cache.setMaxLimit(10);
				m_cache.setCacheScheme(OracleConnectionCacheImpl.DYNAMIC_SCHEME);
			}
			//
			Connection conn = m_cache.getConnection();
			System.out.println("ConnectionCache=" + m_cache.getCacheSize() 
				+ ",Active=" + m_cache.getActiveSize());
			return conn;
		}
		catch (Exception e)
		{
		//	System.err.println ("DB_Oracle.getCachedConnection");
			e.printStackTrace();
		}	
		return null;
	}	//	getCachedConnection


	/*************************************************************************/

	/**
	 * 	Testing
	 * 	@param args ignored
	 */
	public static void main (String[] args)
	{
		Compiere.initClientLog();
		Compiere.startupServer();
		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));



		try
		{
			System.out.println("-- Sleeping --");
			Thread.sleep(60000);
		}
		catch (InterruptedException e)
		{
		}
				
		
		
		/**
		//	Connection option 1
		try
		{
			System.setProperty("oracle.jdbc.Trace", "true");
			DriverManager.registerDriver(new OracleDriver());
			Connection con = DriverManager.getConnection("jdbc:oracle:thin:@dev:1521:dev", "compiere", "compiere");
			System.out.println("Catalog=" + con.getCatalog());
			DatabaseMetaData md = con.getMetaData();
			System.out.println("URL=" + md.getURL());
			System.out.println("User=" + md.getUserName());
			//
			System.out.println("Catalog");
			ResultSet rs = md.getCatalogs();
			while (rs.next())
				System.out.println("- " + rs.getString(1));
			//
			System.out.println("Table");
			rs = md.getTables(null, "COMPIERE", null, new String[] {"TABLE"});
			while (rs.next())
				System.out.println("- User=" + rs.getString(2) + " | Table=" + rs.getString(3)
					+ " | Type=" + rs.getString(4) + " | " + rs.getString(5));
			//
			System.out.println("Column");
			rs = md.getColumns(null, "COMPIERE", "C_ORDER", null);
			while (rs.next())
				System.out.println("- Tab=" + rs.getString(3) + " | Col=" + rs.getString(4)
					+ " | Type=" + rs.getString(5) + ", " + rs.getString(6)
					+ " | Size=" + rs.getString(7) + " | " + rs.getString(8)
					+ " | Digits=" + rs.getString(9) + " | Radix=" + rs.getString(10)
					+ " | Null=" + rs.getString(11) + " | Rem=" + rs.getString(12)
					+ " | Def=" + rs.getString(13) + " | " + rs.getString(14)
					+ " | " + rs.getString(15) + " | " + rs.getString(16)
					+ " | Ord=" + rs.getString(17) + " | Null=" + rs.getString(18)
					);

			con.close();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		**/
	}	//	main
}   //  DB_Oracle
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
import java.util.*;

import javax.sql.*;

import oracle.jdbc.*;
import oracle.jdbc.pool.*;
import oracle.jdbc.rowset.*;

import org.apache.xalan.lib.sql.*;
import org.compiere.*;
import org.compiere.util.*;

/**
 *  Oracle Database Port
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB_Oracle.java,v 1.27 2004/09/09 14:16:50 jjanke Exp $
 */
public class DB_Oracle implements CompiereDatabase, OracleConnectionCacheCallback
{
	/**
	 *  Oracle Database
	 */
	public DB_Oracle()
	{
	}   //  DB_Oracle

	/** Static Driver           */
	private OracleDriver        	s_driver = null;

	/** Default Port            */
	public static final int 		DEFAULT_PORT = 1521;
	/** Default Connection Manager Port */
	public static final int 		DEFAULT_CM_PORT = 1630;
	
	/** Connection String       */
	private String          		m_connectionURL;

	/** Statement Cache			*/
	private static final int		MAX_STATEMENTS = 20;
	/** Data Source				*/
	private OracleDataSource		m_ds = null;
	
    /** Use Connection Cache	*/
	private static final boolean	USE_CACHE = false;
	/** Connection Cache		*/
	private OracleConnectionCacheManager	m_cacheMgr = null;
	/** Connection Cache Name	*/
    private static final String 	CACHE_NAME = "CompiereCCache";


	/**
	 *  Get Database Name
	 *  @return database short name
	 */
	public String getName()
	{
		return Database.DB_ORACLE;
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
	public Driver getDriver() throws SQLException
	{
		if (s_driver == null)
		{
			s_driver = new OracleDriver();
			DriverManager.registerDriver (s_driver);
			DriverManager.setLoginTimeout (Database.CONNECTION_TIMEOUT);
		}
		return s_driver;
	}   //  getDriver

	/**
	 *  Get Database Connection String.
	 *  <pre>
	 *  Timing:
	 *  - CM with source_route not in address_list  = 28.5 sec
	 *  - CM with source_route in address_list      = 58.0 sec
	 *  - direct    = 4.3-8 sec  (no real difference if on other box)
	 *  - bequeath  = 3.4-8 sec
	 *  </pre>
	 *  @param connection Connection Descriptor
	 *  @return connection String
	 */
	public String getConnectionURL (CConnection connection)
	{
		StringBuffer sb = null;
		//  Server Connections (bequeath)
		if (connection.isBequeath())
		{
			sb = new StringBuffer ("jdbc:oracle:oci8:@");
			//  bug: does not work if there is more than one db instance - use Net8
		//	sb.append(connection.getDbName());
		}
		else        //  thin driver
		{
			sb = new StringBuffer ("jdbc:oracle:thin:@");
			//  direct connection
			if (connection.isViaFirewall())
			{
				//  (description=(address_list=
				//  ( (source_route=yes)
				//    (address=(protocol=TCP)(host=cmhost)(port=1630))
				//    (address=(protocol=TCP)(host=dev)(port=1521))
				//  (connect_data=(service_name=dev1.compiere.org)))
				sb.append("(DESCRIPTION=(ADDRESS_LIST=")
					.append("(SOURCE_ROUTE=YES)")
					.append("(ADDRESS=(PROTOCOL=TCP)(HOST=").append(connection.getFwHost())
						.append(")(PORT=").append(connection.getFwPort()).append("))")
					.append("(ADDRESS=(PROTOCOL=TCP)(HOST=").append(connection.getDbHost())
						.append(")(PORT=").append(connection.getDbPort()).append(")))")
					.append("(CONNECT_DATA=(SERVICE_NAME=").append(connection.getDbName()).append(")))");
			}
			else
			{
				//  dev:1521:dev1
				sb.append(connection.getDbHost())
					.append(":").append(connection.getDbPort())
					.append(":").append(connection.getDbName());
			}
		}
		m_connectionURL = sb.toString();
		return m_connectionURL;
	}   //  getConnectionString

	/**
	 *  Supports BLOB
	 *  @return true if BLOB is supported
	 */
	public boolean supportsBLOB()
	{
		return true;
	}   //  supportsBLOB

	/**
	 *  String Representation
	 *  @return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("DB_Oracle[");
		sb.append(m_connectionURL);
		try
		{
			if (m_ds != null)
				sb.append("-").append(m_ds.getDataSourceName())
				//	.append(",ExplCache=").append(m_ds.getExplicitCachingEnabled())
					.append(",ImplCache=").append(m_ds.getImplicitCachingEnabled())
					.append(",MaxStmts=").append(m_ds.getMaxStatements());
				//	.append(",Ref=").append(m_ds.getReference());
			if (m_cacheMgr != null && m_cacheMgr.existsCache(CACHE_NAME))
				sb.append(";ConnectionActive=").append(m_cacheMgr.getNumberOfActiveConnections(CACHE_NAME))
					.append(",CacheAvailable=").append(m_cacheMgr.getNumberOfAvailableConnections(CACHE_NAME));
		}
		catch (Exception e)
		{
			sb.append("=").append(e.getLocalizedMessage());
		}
		sb.append("]");
		return sb.toString();
	}   //  toString

	/**
	 * 	Get Status
	 * 	@return status info
	 */
	public String getStatus()
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			if (m_cacheMgr != null && m_cacheMgr.existsCache(CACHE_NAME))
				sb.append("-Connections=").append(m_cacheMgr.getNumberOfActiveConnections(CACHE_NAME))
					.append(",Cache=").append(m_cacheMgr.getNumberOfAvailableConnections(CACHE_NAME));
		}
		catch (Exception e)
		{}
		return sb.toString();
	}	//	getStatus

	
	/**************************************************************************
	 *  Convert an individual Oracle Style statements to target database statement syntax.
	 *  @param oraStatement oracle statement
	 *  @return converted Statement oracle statement
	 *  @throws Exception
	 */
	public String convertStatement (String oraStatement)
	{
		return oraStatement;
	}   //  convertStatement

	
	/**************************************************************************
	 *  Set the RowID
	 *  @param pstmt statement
	 *  @param pos position in result set
	 *  @param rowID ROWID
	 *  @throws SQLException
	 */
	public void setRowID (PreparedStatement pstmt, int pos, Object rowID) throws SQLException
	{
	//	((OraclePreparedStatement)pstmt).setROWID (pos, (ROWID)rowID);
		pstmt.setString(pos, rowID.toString());
	}   //  setRowID

	/**
	 *  Get the RowID
	 *  @param rs result set
	 *  @param pos psoition in result set
	 *  @return rowID ROWID
	 *  @throws SQLException
	 */
	public Object getRowID (ResultSet rs, int pos) throws SQLException
	{
	//	return ((OracleResultSet)rs).getROWID (pos);
		return rs.getString (pos);
	}   //  getRowID

	/**
	 *	Get RowSet
	 *	@param rs ResultSet
	 *	@return RowSet
	 *	@throws SQLException
	 */
	public RowSet getRowSet (ResultSet rs) throws SQLException
	{
		OracleCachedRowSet rowSet = new OracleCachedRowSet ();
		rowSet.populate (rs);
		return rowSet;
	}	//	getRowSet

	/**
	 * 	Create DataSource
	 *	@param connection connection
	 *	@return data dource
	 */
	public DataSource getDataSource(CConnection connection)
	{
		if (m_ds != null)
			return m_ds;
		try
		{
			m_ds = new OracleDataSource();
			m_ds.setDriverType("thin");
			m_ds.setNetworkProtocol("tcp");
			m_ds.setServerName(connection.getDbHost());
			m_ds.setDatabaseName(connection.getDbName());
			m_ds.setPortNumber(connection.getDbPort());
			m_ds.setUser(connection.getDbUid());
			m_ds.setPassword(connection.getDbPwd());
			//
			m_ds.setDescription("CompiereDS");
			m_ds.setImplicitCachingEnabled(true);
		//	m_ds.setExplicitCachingEnabled(true);
			m_ds.setMaxStatements(MAX_STATEMENTS);
			//
			Properties cacheProperties = new Properties();
		//	cacheProperties.setProperty("InitialLimit", "3"); // at startup
		//	cacheProperties.setProperty("MaxStatementsLimit", "10");
			cacheProperties.setProperty("ClosestConnectionMatch", "true");
			cacheProperties.setProperty("ValidateConnection", "true");
			if (Ini.isClient())
			{
				cacheProperties.setProperty("MinLimit", "0");
			//	cacheProperties.setProperty("MaxLimit", "5");
				cacheProperties.setProperty("InactivityTimeout", "300");    //  5 Min
				cacheProperties.setProperty("AbandonedConnectionTimeout", "300");  //  5 Min
			}
			else	//	Server Settings
			{
				cacheProperties.setProperty("MinLimit", "3");
			//	cacheProperties.setProperty("MaxLimit", "5");
				cacheProperties.setProperty("InactivityTimeout", "600");    //  10 Min
				cacheProperties.setProperty("AbandonedConnectionTimeout", "600");  //  10 Min
			}
			cacheProperties.setProperty("PropertyCheckInterval", "120"); // 2 Min
			//
			if (USE_CACHE)
			{
				m_ds.setConnectionCachingEnabled(true);
				m_ds.setConnectionCacheName(CACHE_NAME);
			}
			//
			if (m_cacheMgr == null && USE_CACHE)
			{
				m_cacheMgr = OracleConnectionCacheManager.getConnectionCacheManagerInstance();
				if (!m_cacheMgr.existsCache(CACHE_NAME))
					m_cacheMgr.createCache(CACHE_NAME, m_ds, cacheProperties);
			}
			//	test
//			OracleConnection con = m_ds.getConnection();
//			con.close();
			//
			if (Log.isTraceLevel(8))
				System.out.println(toString());
			//
			return m_ds;
		}
		catch (Exception e)
		{
			System.err.println ("DB_Oracle.getDataSource");
			e.printStackTrace();
		}
		return null;	
	}	//	getDataSource


	/**
	 * 	Get Cached Connection
	 *	@param connection info
	 *  @param autoCommit true if autocommit connection
	 *  @param transactionIsolation Connection transaction level
	 *	@return connection or null
	 */
	public Connection getCachedConnection (CConnection connection, 
		boolean autoCommit, int transactionIsolation)
		throws Exception
	{
		OracleConnection conn = null;
		Exception exception = null;
		try
		{
			if (USE_CACHE && m_cacheMgr == null)
				getDataSource(connection);
			if (m_ds == null)
				getDataSource(connection);
			
		//	Properties connAttr = new Properties();
		//	connAttr.setProperty("TRANSACTION_ISOLATION", CConnection.getTransactionIsolationInfo(transactionIsolation));
		//	OracleConnection conn = (OracleConnection)m_ds.getConnection(connAttr);
			//
			//	Try 5 times max
			for (int i = 0; i < 5; i++)
			{
				try
				{
					conn = (OracleConnection)m_ds.getConnection();
					if (conn != null)
					{
						if (conn.getTransactionIsolation() != transactionIsolation)
							conn.setTransactionIsolation(transactionIsolation);
						if (conn.getAutoCommit() != autoCommit)
							conn.setAutoCommit(autoCommit);
						conn.setDefaultRowPrefetch(20);		//	10 default - reduces round trips
					}
				}
				catch (SQLException e)
				{
					exception = e;
					conn = null;
					if (e.getErrorCode() == 1017)	//	invalid username/password
						break;
				}
				try
				{
					if (conn != null && conn.isClosed())
						conn = null;
					//	OK
					if (conn != null && !conn.isClosed())
						break;
					if (i == 0)
						Thread.yield();		//	give some time
					else
						Thread.sleep(100);
				}
				catch (Exception e)
				{
					exception = e;
					conn = null;
				}
			}	//	5 tries
			if (conn == null)
			{
				System.err.println("DB_Oracle.getCachedConnection - " + exception);
				if (exception instanceof SQLException)
				{
					if (Log.isTraceLevel(8))
						System.err.println(m_ds.getReference());
					else
						System.err.println(toString());
				}
				else
					exception.printStackTrace();
			}
		//	else
		//	{
			//	System.out.println(conn + " " + getStatus());
			//	conn.registerConnectionCacheCallback(this, "test", OracleConnection.ALL_CONNECTION_CALLBACKS);
		//	}
		}
		catch (Exception e)
		{
		//	System.err.println ("DB_Oracle.getCachedConnection");
			if (!(e instanceof SQLException))
				e.printStackTrace();
			exception = e;
		}	
		if (exception != null)
			throw exception;
		return conn;
	}	//	getCachedConnection

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
		if (Log.isTraceLevel(8))
			System.out.println("DB_Oracle.close - " + toString());
		if (m_ds != null)
		{
			try
			{
				m_ds.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		if (m_cacheMgr != null)
		{
			try
			{
				if (m_cacheMgr.existsCache(CACHE_NAME))
					m_cacheMgr.purgeCache(CACHE_NAME, false);	// not active
			//	m_cache.disableCache(CACHE_NAME);
			//	m_cache.removeCache(CACHE_NAME, 0);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		m_cacheMgr = null;
		m_ds = null;
	}	//	close

	/**
	 * 	Cleanup
	 */
	public void cleanup()
	{
		if (!USE_CACHE)
			return;
		
		System.out.println("DB_Oracle.cleanup");
		try
		{
			if (m_cacheMgr == null)
				m_cacheMgr = OracleConnectionCacheManager.getConnectionCacheManagerInstance();
			String[] cacheNames = m_cacheMgr.getCacheNameList();
			for (int i = 0; i < cacheNames.length; i++)
			{
				String name = cacheNames[i];
				System.out.println("  cleanup: " + name);
				System.out.println("    Before = Active=" + m_cacheMgr.getNumberOfActiveConnections(name)
					+ ", Available=" + m_cacheMgr.getNumberOfAvailableConnections(name));
				m_cacheMgr.purgeCache(name, false);
				System.out.println("    Cached = Active=" + m_cacheMgr.getNumberOfActiveConnections(name)
					+ ", Available=" + m_cacheMgr.getNumberOfAvailableConnections(name));
				m_cacheMgr.purgeCache(name, true);
				System.out.println("    All    = Active=" + m_cacheMgr.getNumberOfActiveConnections(name)
					+ ", Available=" + m_cacheMgr.getNumberOfAvailableConnections(name));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}	//	cleanup

	/**************************************************************************
	 * 	Handle Abandoned Connection
	 *	@param cinn connection
	 *	@param userObject 
	 *	@return true if close - false for keeping it
	 */
	public boolean handleAbandonedConnection (OracleConnection conn, Object userObject)
	{
		System.out.println("--------------------handleAbandonedConnection " + conn + " - " + userObject);
		return true;	//	reclaim it
	}	//	handleAbandonedConnection

	/**
	 * 	Release Connection
	 *	@param cinn connection
	 *	@param userObject 
	 */
	public void releaseConnection (OracleConnection conn, Object userObject)
	{
		System.out.println("----------------------releaseConnection " + conn + " - " + userObject);
	}	//	releaseConnection

	
	
	/**************************************************************************
	 * 	Testing
	 * 	@param args ignored
	 * @throws SQLException
	 */
	public static void main (String[] args)
	{
		Compiere.startupClient();
		CConnection cc = CConnection.get();
		DB_Oracle db = (DB_Oracle)cc.getDatabase();
		db.cleanup();
		
		try
		{
			Connection conn = null;
		//	System.out.println("Driver=" + db.getDriverConnection(cc));
			DataSource ds = db.getDataSource(cc);
			System.out.println("DS=" + ds.getConnection());
			conn = db.getCachedConnection(cc, true, Connection.TRANSACTION_SERIALIZABLE);
			System.out.println("Cached=" + conn);
			System.out.println(db);
			//////////////////////////
			System.out.println("JAVA classpath: [\n" +
				System.getProperty("java.class.path") + "\n]");
				DatabaseMetaData dmd = conn.getMetaData();
				System.out.println("DriverVersion: ["+
				dmd.getDriverVersion()+"]");
				System.out.println("DriverMajorVersion: ["+
				dmd.getDriverMajorVersion()+"]");
				System.out.println("DriverMinorVersion: ["+
				dmd.getDriverMinorVersion()+"]");
				System.out.println("DriverName: ["+
				dmd.getDriverName()+"]");
				System.out.println("ProductName: ["+
				dmd.getDatabaseProductName() +"]");
				System.out.println("ProductVersion: [\n"+
				dmd.getDatabaseProductVersion()+"\n]"); 
			//////////////////////////
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		db.cleanup();
		
		System.out.println("--------------------------------------------------");
		try
		{
			Connection conn1 = db.getCachedConnection(cc, false, Connection.TRANSACTION_SERIALIZABLE);
			Connection conn2 = db.getCachedConnection(cc, true, Connection.TRANSACTION_READ_COMMITTED);
			Connection conn3 = db.getCachedConnection(cc, false, Connection.TRANSACTION_READ_COMMITTED);
			System.out.println("3 -> " + db);
			conn1.close();
			conn2.close();
			conn1 = db.getCachedConnection(cc, true, Connection.TRANSACTION_SERIALIZABLE);
			conn2 = db.getCachedConnection(cc, true, Connection.TRANSACTION_READ_COMMITTED);
			System.out.println("3 -> " + db);
			conn1.close();
			conn2.close();
			conn3.close();
			System.out.println("0 -> " + db);
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		
		db.cleanup();
		
	//	System.exit(0);
		System.out.println("--------------------------------------------------");
		
		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(db);


		try
		{
			System.out.println("-- Sleeping --");
			Thread.sleep(60000);
			System.out.println(db);
			db.close();
			db.cleanup();
			System.out.println(db);
		}
		catch (InterruptedException e)
		{
		}
				
		
		
		/**
		//	Connection option 1
		try
		{
			System.setProperty("oracle.jdbc.Trace", "true");
			DriverManager.registerDriver(new OracleDriver());
			Connection con = DriverManager.getConnection("jdbc:oracle:thin:@dev:1521:dev", "compiere", "compiere");
			System.out.println("Catalog=" + con.getCatalog());
			DatabaseMetaData md = con.getMetaData();
			System.out.println("URL=" + md.getURL());
			System.out.println("User=" + md.getUserName());
			//
			System.out.println("Catalog");
			ResultSet rs = md.getCatalogs();
			while (rs.next())
				System.out.println("- " + rs.getString(1));
			//
			System.out.println("Table");
			rs = md.getTables(null, "COMPIERE", null, new String[] {"TABLE"});
			while (rs.next())
				System.out.println("- User=" + rs.getString(2) + " | Table=" + rs.getString(3)
					+ " | Type=" + rs.getString(4) + " | " + rs.getString(5));
			//
			System.out.println("Column");
			rs = md.getColumns(null, "COMPIERE", "C_ORDER", null);
			while (rs.next())
				System.out.println("- Tab=" + rs.getString(3) + " | Col=" + rs.getString(4)
					+ " | Type=" + rs.getString(5) + ", " + rs.getString(6)
					+ " | Size=" + rs.getString(7) + " | " + rs.getString(8)
					+ " | Digits=" + rs.getString(9) + " | Radix=" + rs.getString(10)
					+ " | Null=" + rs.getString(11) + " | Rem=" + rs.getString(12)
					+ " | Def=" + rs.getString(13) + " | " + rs.getString(14)
					+ " | " + rs.getString(15) + " | " + rs.getString(16)
					+ " | Ord=" + rs.getString(17) + " | Null=" + rs.getString(18)
					);

			con.close();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		**/
	}	//	main
	
}   //  DB_Oracle
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

import org.compiere.Compiere;
import org.compiere.util.DB;

import oracle.jdbc.*;
import oracle.jdbc.pool.*;
import oracle.jdbc.rowset.OracleCachedRowSet;

/**
 *  Oracle Database Port
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB_Oracle.java,v 1.21 2004/01/26 06:01:02 jjanke Exp $
 */
public class DB_Oracle implements CompiereDatabase
{
	/**
	 *  Oracle Database
	 */
	public DB_Oracle()
	{
	}   //  DB_Oracle

	/** Static Driver           */
	private OracleDriver        s_driver = new OracleDriver();

	/** Default Port            */
	public static final int DEFAULT_PORT = 1521;
	/** Default Connection Manager Port */
	public static final int DEFAULT_CM_PORT = 1630;

	/** Connection String       */
	private String          	m_connectionURL;


	private OracleDataSource				m_ds = null;
	private OracleConnectionPoolDataSource	m_cpds = null;
	private OracleConnectionCacheImpl		m_cache = null;


	/**
	 *  Get Database Name
	 *  @return database short name
	 */
	public String getName()
	{
		return Database.DB_ORACLE;
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
	public Driver getDriver()
	{
		return s_driver;
	}   //  getDriver

	/**
	 *  Get Database Connection String.
	 *  <pre>
	 *  Timing:
	 *  - CM with source_route not in address_list  = 28.5 sec
	 *  - CM with source_route in address_list      = 58.0 sec
	 *  - direct    = 4.3-8 sec  (no real difference if on other box)
	 *  - bequeath  = 3.4-8 sec
	 *  </pre>
	 *  @param connection Connection Descriptor
	 *  @return connection String
	 */
	public String getConnectionURL (CConnection connection)
	{
		StringBuffer sb = null;
		//  Server Connections (bequeath)
		if (connection.isBequeath())
		{
			sb = new StringBuffer ("jdbc:oracle:oci8:@");
			//  bug: does not work if there is more than one db instance - use Net8
		//	sb.append(connection.getDbName());
		}
		else        //  thin driver
		{
			sb = new StringBuffer ("jdbc:oracle:thin:@");
			//  direct connection
			if (connection.isViaFirewall())
			{
				//  (description=(address_list=
				//  ( (source_route=yes)
				//    (address=(protocol=TCP)(host=cmhost)(port=1630))
				//    (address=(protocol=TCP)(host=dev)(port=1521))
				//  (connect_data=(service_name=dev1.compiere.org)))
				sb.append("(DESCRIPTION=(ADDRESS_LIST=")
					.append("(SOURCE_ROUTE=YES)")
					.append("(ADDRESS=(PROTOCOL=TCP)(HOST=").append(connection.getFwHost())
						.append(")(PORT=").append(connection.getFwPort()).append("))")
					.append("(ADDRESS=(PROTOCOL=TCP)(HOST=").append(connection.getDbHost())
						.append(")(PORT=").append(connection.getDbPort()).append(")))")
					.append("(CONNECT_DATA=(SERVICE_NAME=").append(connection.getDbName()).append(")))");
			}
			else
			{
				//  dev:1521:dev1
				sb.append(connection.getDbHost())
					.append(":").append(connection.getDbPort())
					.append(":").append(connection.getDbName());
			}
		}
		m_connectionURL = sb.toString();
		return m_connectionURL;
	}   //  getConnectionString

	/**
	 *  Supports BLOB
	 *  @return true if BLOB is supported
	 */
	public boolean supportsBLOB()
	{
		return true;
	}   //  supportsBLOB

	/**
	 *  String Representation
	 *  @return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("DB_Oracle[");
		sb.append(m_connectionURL)
			.append("]");
		return sb.toString();
	}   //  toString

	/*************************************************************************/

	/**
	 *  Convert an individual Oracle Style statements to target database statement syntax.
	 *  @param oraStatement oracle statement
	 *  @return converted Statement oracle statement
	 *  @throws Exception
	 */
	public String convertStatement (String oraStatement)
	{
		return oraStatement;
	}   //  convertStatement

	/*************************************************************************/

	/**
	 *  Set the RowID
	 *  @param pstmt statement
	 *  @param pos position in result set
	 *  @param rowID ROWID
	 *  @throws SQLException
	 */
	public void setRowID (PreparedStatement pstmt, int pos, Object rowID) throws SQLException
	{
	//	((OraclePreparedStatement)pstmt).setROWID (pos, (ROWID)rowID);
		pstmt.setString(pos, rowID.toString());
	}   //  setRowID

	/**
	 *  Get the RowID
	 *  @param rs result set
	 *  @param pos psoition in result set
	 *  @return rowID ROWID
	 *  @throws SQLException
	 */
	public Object getRowID (ResultSet rs, int pos) throws SQLException
	{
	//	return ((OracleResultSet)rs).getROWID (pos);
		return rs.getString (pos);
	}   //  getRowID

	/**
	 *	Get RowSet
	 *	@param rs ResultSet
	 *	@return RowSet
	 *	@throws SQLException
	 */
	public RowSet getRowSet (ResultSet rs) throws SQLException
	{
		OracleCachedRowSet rowSet = new OracleCachedRowSet ();
		rowSet.populate (rs);
		return rowSet;
	}	//	getRowSet

	/**
	 * 	Create DataSource (for Clinet)
	 *	@param connection connection
	 *	@return data dource
	 */
	public DataSource createDataSource(CConnection connection)
	{
		try
		{
			m_ds = new OracleDataSource();
			m_ds.setDriverType("thin");
			m_ds.setNetworkProtocol("tcp");
			m_ds.setServerName(connection.getDbHost());
			m_ds.setDatabaseName(connection.getDbName());
			m_ds.setPortNumber(connection.getDbPort());
			m_ds.setUser(connection.getDbUid());
			m_ds.setPassword(connection.getDbPwd());
			//
			m_ds.setDescription("CompiereDS");
			m_ds.setImplicitCachingEnabled(true);
			m_ds.setExplicitCachingEnabled(true);
			m_ds.setMaxStatements(200);
			//
			//	test
			Connection con = m_ds.getConnection();
			con.close();
			//
			System.out.println(m_ds.getDataSourceName()
				+ ",ExplCache=" + m_ds.getExplicitCachingEnabled()
				+ ",ImplCache=" + m_ds.getImplicitCachingEnabled()
				+ ",MaxStatements=" + m_ds.getMaxStatements()
			//	+ ",Ref=" + ods.getReference()
			);
			//
			return m_ds;
		}
		catch (Exception e)
		{
		//	System.err.println ("DB_Oracle.getDataSource");
			e.printStackTrace();
		}
		return null;	
	}	//	getDataSource

	/**
	 * 	Create Pooled DataSource (Server)
	 *	@param connection connection
	 *	@return data dource
	 */
	public ConnectionPoolDataSource createPoolDataSource (CConnection connection)
	{
		try
		{
			m_cpds = new OracleConnectionPoolDataSource();  
			m_cpds.setDriverType("thin");
			m_cpds.setNetworkProtocol("tcp");
			m_cpds.setServerName(connection.getDbHost());
			m_cpds.setDatabaseName(connection.getDbName());
			m_cpds.setPortNumber(connection.getDbPort());
			m_cpds.setUser(connection.getDbUid());
			m_cpds.setPassword(connection.getDbPwd());
			//
			m_cpds.setDescription("CompierePoolDS");
			m_cpds.setImplicitCachingEnabled(true);
			m_cpds.setExplicitCachingEnabled(true);
			m_cpds.setMaxStatements(200);
			//	test
			PooledConnection pc = m_cpds.getPooledConnection();
			Connection conn = pc.getConnection();
			conn.close();
			//
			System.out.println(m_cpds.getDataSourceName()
				+ ",ExplCache=" + m_cpds.getExplicitCachingEnabled()
				+ ",ImplCache=" + m_cpds.getImplicitCachingEnabled()
				+ ",MaxStatements=" + m_cpds.getMaxStatements()
			//	+ ",Ref=" + m_cpds.getReference()
			);
			//
			return m_cpds;
		}
		catch (Exception e)
		{
		//	System.err.println ("DB_Oracle.getPoolDataSource");
			e.printStackTrace();
		}
		return null;	
	}	//	getPoolDataSource

	/**
	 * 	Get Cached Connection
	 *	@param connection info
	 *	@return connection or null
	 */
	public Connection getCachedConnection (CConnection connection)
	{
		try
		{
			if (m_cache == null)
			{
				if (m_cpds == null)
					createPoolDataSource(connection);	
			
				m_cache = new OracleConnectionCacheImpl(m_cpds);
				m_cache.setMinLimit(1);
				m_cache.setMaxLimit(10);
				m_cache.setCacheScheme(OracleConnectionCacheImpl.DYNAMIC_SCHEME);
			}
			//
			Connection conn = m_cache.getConnection();
			System.out.println("ConnectionCache=" + m_cache.getCacheSize() 
				+ ",Active=" + m_cache.getActiveSize());
			return conn;
		}
		catch (Exception e)
		{
		//	System.err.println ("DB_Oracle.getCachedConnection");
			e.printStackTrace();
		}	
		return null;
	}	//	getCachedConnection


	/*************************************************************************/

	/**
	 * 	Testing
	 * 	@param args ignored
	 */
	public static void main (String[] args)
	{
		Compiere.initClientLog();
		Compiere.startupServer();
		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));



		try
		{
			System.out.println("-- Sleeping --");
			Thread.sleep(60000);
		}
		catch (InterruptedException e)
		{
		}
				
		
		
		/**
		//	Connection option 1
		try
		{
			System.setProperty("oracle.jdbc.Trace", "true");
			DriverManager.registerDriver(new OracleDriver());
			Connection con = DriverManager.getConnection("jdbc:oracle:thin:@dev:1521:dev", "compiere", "compiere");
			System.out.println("Catalog=" + con.getCatalog());
			DatabaseMetaData md = con.getMetaData();
			System.out.println("URL=" + md.getURL());
			System.out.println("User=" + md.getUserName());
			//
			System.out.println("Catalog");
			ResultSet rs = md.getCatalogs();
			while (rs.next())
				System.out.println("- " + rs.getString(1));
			//
			System.out.println("Table");
			rs = md.getTables(null, "COMPIERE", null, new String[] {"TABLE"});
			while (rs.next())
				System.out.println("- User=" + rs.getString(2) + " | Table=" + rs.getString(3)
					+ " | Type=" + rs.getString(4) + " | " + rs.getString(5));
			//
			System.out.println("Column");
			rs = md.getColumns(null, "COMPIERE", "C_ORDER", null);
			while (rs.next())
				System.out.println("- Tab=" + rs.getString(3) + " | Col=" + rs.getString(4)
					+ " | Type=" + rs.getString(5) + ", " + rs.getString(6)
					+ " | Size=" + rs.getString(7) + " | " + rs.getString(8)
					+ " | Digits=" + rs.getString(9) + " | Radix=" + rs.getString(10)
					+ " | Null=" + rs.getString(11) + " | Rem=" + rs.getString(12)
					+ " | Def=" + rs.getString(13) + " | " + rs.getString(14)
					+ " | " + rs.getString(15) + " | " + rs.getString(16)
					+ " | Ord=" + rs.getString(17) + " | Null=" + rs.getString(18)
					);

			con.close();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		**/
	}	//	main
}   //  DB_Oracle
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
import java.util.*;

import javax.sql.*;

import oracle.jdbc.*;
import oracle.jdbc.pool.*;
import oracle.jdbc.rowset.*;

import org.apache.xalan.lib.sql.*;
import org.compiere.*;
import org.compiere.util.*;

/**
 *  Oracle Database Port
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB_Oracle.java,v 1.27 2004/09/09 14:16:50 jjanke Exp $
 */
public class DB_Oracle implements CompiereDatabase, OracleConnectionCacheCallback
{
	/**
	 *  Oracle Database
	 */
	public DB_Oracle()
	{
	}   //  DB_Oracle

	/** Static Driver           */
	private OracleDriver        	s_driver = null;

	/** Default Port            */
	public static final int 		DEFAULT_PORT = 1521;
	/** Default Connection Manager Port */
	public static final int 		DEFAULT_CM_PORT = 1630;
	
	/** Connection String       */
	private String          		m_connectionURL;

	/** Statement Cache			*/
	private static final int		MAX_STATEMENTS = 20;
	/** Data Source				*/
	private OracleDataSource		m_ds = null;
	
    /** Use Connection Cache	*/
	private static final boolean	USE_CACHE = false;
	/** Connection Cache		*/
	private OracleConnectionCacheManager	m_cacheMgr = null;
	/** Connection Cache Name	*/
    private static final String 	CACHE_NAME = "CompiereCCache";


	/**
	 *  Get Database Name
	 *  @return database short name
	 */
	public String getName()
	{
		return Database.DB_ORACLE;
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
	public Driver getDriver() throws SQLException
	{
		if (s_driver == null)
		{
			s_driver = new OracleDriver();
			DriverManager.registerDriver (s_driver);
			DriverManager.setLoginTimeout (Database.CONNECTION_TIMEOUT);
		}
		return s_driver;
	}   //  getDriver

	/**
	 *  Get Database Connection String.
	 *  <pre>
	 *  Timing:
	 *  - CM with source_route not in address_list  = 28.5 sec
	 *  - CM with source_route in address_list      = 58.0 sec
	 *  - direct    = 4.3-8 sec  (no real difference if on other box)
	 *  - bequeath  = 3.4-8 sec
	 *  </pre>
	 *  @param connection Connection Descriptor
	 *  @return connection String
	 */
	public String getConnectionURL (CConnection connection)
	{
		StringBuffer sb = null;
		//  Server Connections (bequeath)
		if (connection.isBequeath())
		{
			sb = new StringBuffer ("jdbc:oracle:oci8:@");
			//  bug: does not work if there is more than one db instance - use Net8
		//	sb.append(connection.getDbName());
		}
		else        //  thin driver
		{
			sb = new StringBuffer ("jdbc:oracle:thin:@");
			//  direct connection
			if (connection.isViaFirewall())
			{
				//  (description=(address_list=
				//  ( (source_route=yes)
				//    (address=(protocol=TCP)(host=cmhost)(port=1630))
				//    (address=(protocol=TCP)(host=dev)(port=1521))
				//  (connect_data=(service_name=dev1.compiere.org)))
				sb.append("(DESCRIPTION=(ADDRESS_LIST=")
					.append("(SOURCE_ROUTE=YES)")
					.append("(ADDRESS=(PROTOCOL=TCP)(HOST=").append(connection.getFwHost())
						.append(")(PORT=").append(connection.getFwPort()).append("))")
					.append("(ADDRESS=(PROTOCOL=TCP)(HOST=").append(connection.getDbHost())
						.append(")(PORT=").append(connection.getDbPort()).append(")))")
					.append("(CONNECT_DATA=(SERVICE_NAME=").append(connection.getDbName()).append(")))");
			}
			else
			{
				//  dev:1521:dev1
				sb.append(connection.getDbHost())
					.append(":").append(connection.getDbPort())
					.append(":").append(connection.getDbName());
			}
		}
		m_connectionURL = sb.toString();
		return m_connectionURL;
	}   //  getConnectionString

	/**
	 *  Supports BLOB
	 *  @return true if BLOB is supported
	 */
	public boolean supportsBLOB()
	{
		return true;
	}   //  supportsBLOB

	/**
	 *  String Representation
	 *  @return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("DB_Oracle[");
		sb.append(m_connectionURL);
		try
		{
			if (m_ds != null)
				sb.append("-").append(m_ds.getDataSourceName())
				//	.append(",ExplCache=").append(m_ds.getExplicitCachingEnabled())
					.append(",ImplCache=").append(m_ds.getImplicitCachingEnabled())
					.append(",MaxStmts=").append(m_ds.getMaxStatements());
				//	.append(",Ref=").append(m_ds.getReference());
			if (m_cacheMgr != null && m_cacheMgr.existsCache(CACHE_NAME))
				sb.append(";ConnectionActive=").append(m_cacheMgr.getNumberOfActiveConnections(CACHE_NAME))
					.append(",CacheAvailable=").append(m_cacheMgr.getNumberOfAvailableConnections(CACHE_NAME));
		}
		catch (Exception e)
		{
			sb.append("=").append(e.getLocalizedMessage());
		}
		sb.append("]");
		return sb.toString();
	}   //  toString

	/**
	 * 	Get Status
	 * 	@return status info
	 */
	public String getStatus()
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			if (m_cacheMgr != null && m_cacheMgr.existsCache(CACHE_NAME))
				sb.append("-Connections=").append(m_cacheMgr.getNumberOfActiveConnections(CACHE_NAME))
					.append(",Cache=").append(m_cacheMgr.getNumberOfAvailableConnections(CACHE_NAME));
		}
		catch (Exception e)
		{}
		return sb.toString();
	}	//	getStatus

	
	/**************************************************************************
	 *  Convert an individual Oracle Style statements to target database statement syntax.
	 *  @param oraStatement oracle statement
	 *  @return converted Statement oracle statement
	 *  @throws Exception
	 */
	public String convertStatement (String oraStatement)
	{
		return oraStatement;
	}   //  convertStatement

	
	/**************************************************************************
	 *  Set the RowID
	 *  @param pstmt statement
	 *  @param pos position in result set
	 *  @param rowID ROWID
	 *  @throws SQLException
	 */
	public void setRowID (PreparedStatement pstmt, int pos, Object rowID) throws SQLException
	{
	//	((OraclePreparedStatement)pstmt).setROWID (pos, (ROWID)rowID);
		pstmt.setString(pos, rowID.toString());
	}   //  setRowID

	/**
	 *  Get the RowID
	 *  @param rs result set
	 *  @param pos psoition in result set
	 *  @return rowID ROWID
	 *  @throws SQLException
	 */
	public Object getRowID (ResultSet rs, int pos) throws SQLException
	{
	//	return ((OracleResultSet)rs).getROWID (pos);
		return rs.getString (pos);
	}   //  getRowID

	/**
	 *	Get RowSet
	 *	@param rs ResultSet
	 *	@return RowSet
	 *	@throws SQLException
	 */
	public RowSet getRowSet (ResultSet rs) throws SQLException
	{
		OracleCachedRowSet rowSet = new OracleCachedRowSet ();
		rowSet.populate (rs);
		return rowSet;
	}	//	getRowSet

	/**
	 * 	Create DataSource
	 *	@param connection connection
	 *	@return data dource
	 */
	public DataSource getDataSource(CConnection connection)
	{
		if (m_ds != null)
			return m_ds;
		try
		{
			m_ds = new OracleDataSource();
			m_ds.setDriverType("thin");
			m_ds.setNetworkProtocol("tcp");
			m_ds.setServerName(connection.getDbHost());
			m_ds.setDatabaseName(connection.getDbName());
			m_ds.setPortNumber(connection.getDbPort());
			m_ds.setUser(connection.getDbUid());
			m_ds.setPassword(connection.getDbPwd());
			//
			m_ds.setDescription("CompiereDS");
			m_ds.setImplicitCachingEnabled(true);
		//	m_ds.setExplicitCachingEnabled(true);
			m_ds.setMaxStatements(MAX_STATEMENTS);
			//
			Properties cacheProperties = new Properties();
		//	cacheProperties.setProperty("InitialLimit", "3"); // at startup
		//	cacheProperties.setProperty("MaxStatementsLimit", "10");
			cacheProperties.setProperty("ClosestConnectionMatch", "true");
			cacheProperties.setProperty("ValidateConnection", "true");
			if (Ini.isClient())
			{
				cacheProperties.setProperty("MinLimit", "0");
			//	cacheProperties.setProperty("MaxLimit", "5");
				cacheProperties.setProperty("InactivityTimeout", "300");    //  5 Min
				cacheProperties.setProperty("AbandonedConnectionTimeout", "300");  //  5 Min
			}
			else	//	Server Settings
			{
				cacheProperties.setProperty("MinLimit", "3");
			//	cacheProperties.setProperty("MaxLimit", "5");
				cacheProperties.setProperty("InactivityTimeout", "600");    //  10 Min
				cacheProperties.setProperty("AbandonedConnectionTimeout", "600");  //  10 Min
			}
			cacheProperties.setProperty("PropertyCheckInterval", "120"); // 2 Min
			//
			if (USE_CACHE)
			{
				m_ds.setConnectionCachingEnabled(true);
				m_ds.setConnectionCacheName(CACHE_NAME);
			}
			//
			if (m_cacheMgr == null && USE_CACHE)
			{
				m_cacheMgr = OracleConnectionCacheManager.getConnectionCacheManagerInstance();
				if (!m_cacheMgr.existsCache(CACHE_NAME))
					m_cacheMgr.createCache(CACHE_NAME, m_ds, cacheProperties);
			}
			//	test
//			OracleConnection con = m_ds.getConnection();
//			con.close();
			//
			if (Log.isTraceLevel(8))
				System.out.println(toString());
			//
			return m_ds;
		}
		catch (Exception e)
		{
			System.err.println ("DB_Oracle.getDataSource");
			e.printStackTrace();
		}
		return null;	
	}	//	getDataSource


	/**
	 * 	Get Cached Connection
	 *	@param connection info
	 *  @param autoCommit true if autocommit connection
	 *  @param transactionIsolation Connection transaction level
	 *	@return connection or null
	 */
	public Connection getCachedConnection (CConnection connection, 
		boolean autoCommit, int transactionIsolation)
		throws Exception
	{
		OracleConnection conn = null;
		Exception exception = null;
		try
		{
			if (USE_CACHE && m_cacheMgr == null)
				getDataSource(connection);
			if (m_ds == null)
				getDataSource(connection);
			
		//	Properties connAttr = new Properties();
		//	connAttr.setProperty("TRANSACTION_ISOLATION", CConnection.getTransactionIsolationInfo(transactionIsolation));
		//	OracleConnection conn = (OracleConnection)m_ds.getConnection(connAttr);
			//
			//	Try 5 times max
			for (int i = 0; i < 5; i++)
			{
				try
				{
					conn = (OracleConnection)m_ds.getConnection();
					if (conn != null)
					{
						if (conn.getTransactionIsolation() != transactionIsolation)
							conn.setTransactionIsolation(transactionIsolation);
						if (conn.getAutoCommit() != autoCommit)
							conn.setAutoCommit(autoCommit);
						conn.setDefaultRowPrefetch(20);		//	10 default - reduces round trips
					}
				}
				catch (SQLException e)
				{
					exception = e;
					conn = null;
					if (e.getErrorCode() == 1017)	//	invalid username/password
						break;
				}
				try
				{
					if (conn != null && conn.isClosed())
						conn = null;
					//	OK
					if (conn != null && !conn.isClosed())
						break;
					if (i == 0)
						Thread.yield();		//	give some time
					else
						Thread.sleep(100);
				}
				catch (Exception e)
				{
					exception = e;
					conn = null;
				}
			}	//	5 tries
			if (conn == null)
			{
				System.err.println("DB_Oracle.getCachedConnection - " + exception);
				if (exception instanceof SQLException)
				{
					if (Log.isTraceLevel(8))
						System.err.println(m_ds.getReference());
					else
						System.err.println(toString());
				}
				else
					exception.printStackTrace();
			}
		//	else
		//	{
			//	System.out.println(conn + " " + getStatus());
			//	conn.registerConnectionCacheCallback(this, "test", OracleConnection.ALL_CONNECTION_CALLBACKS);
		//	}
		}
		catch (Exception e)
		{
		//	System.err.println ("DB_Oracle.getCachedConnection");
			if (!(e instanceof SQLException))
				e.printStackTrace();
			exception = e;
		}	
		if (exception != null)
			throw exception;
		return conn;
	}	//	getCachedConnection

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
		if (Log.isTraceLevel(8))
			System.out.println("DB_Oracle.close - " + toString());
		if (m_ds != null)
		{
			try
			{
				m_ds.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		if (m_cacheMgr != null)
		{
			try
			{
				if (m_cacheMgr.existsCache(CACHE_NAME))
					m_cacheMgr.purgeCache(CACHE_NAME, false);	// not active
			//	m_cache.disableCache(CACHE_NAME);
			//	m_cache.removeCache(CACHE_NAME, 0);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		m_cacheMgr = null;
		m_ds = null;
	}	//	close

	/**
	 * 	Cleanup
	 */
	public void cleanup()
	{
		if (!USE_CACHE)
			return;
		
		System.out.println("DB_Oracle.cleanup");
		try
		{
			if (m_cacheMgr == null)
				m_cacheMgr = OracleConnectionCacheManager.getConnectionCacheManagerInstance();
			String[] cacheNames = m_cacheMgr.getCacheNameList();
			for (int i = 0; i < cacheNames.length; i++)
			{
				String name = cacheNames[i];
				System.out.println("  cleanup: " + name);
				System.out.println("    Before = Active=" + m_cacheMgr.getNumberOfActiveConnections(name)
					+ ", Available=" + m_cacheMgr.getNumberOfAvailableConnections(name));
				m_cacheMgr.purgeCache(name, false);
				System.out.println("    Cached = Active=" + m_cacheMgr.getNumberOfActiveConnections(name)
					+ ", Available=" + m_cacheMgr.getNumberOfAvailableConnections(name));
				m_cacheMgr.purgeCache(name, true);
				System.out.println("    All    = Active=" + m_cacheMgr.getNumberOfActiveConnections(name)
					+ ", Available=" + m_cacheMgr.getNumberOfAvailableConnections(name));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}	//	cleanup

	/**************************************************************************
	 * 	Handle Abandoned Connection
	 *	@param cinn connection
	 *	@param userObject 
	 *	@return true if close - false for keeping it
	 */
	public boolean handleAbandonedConnection (OracleConnection conn, Object userObject)
	{
		System.out.println("--------------------handleAbandonedConnection " + conn + " - " + userObject);
		return true;	//	reclaim it
	}	//	handleAbandonedConnection

	/**
	 * 	Release Connection
	 *	@param cinn connection
	 *	@param userObject 
	 */
	public void releaseConnection (OracleConnection conn, Object userObject)
	{
		System.out.println("----------------------releaseConnection " + conn + " - " + userObject);
	}	//	releaseConnection

	
	
	/**************************************************************************
	 * 	Testing
	 * 	@param args ignored
	 * @throws SQLException
	 */
	public static void main (String[] args)
	{
		Compiere.startupClient();
		CConnection cc = CConnection.get();
		DB_Oracle db = (DB_Oracle)cc.getDatabase();
		db.cleanup();
		
		try
		{
			Connection conn = null;
		//	System.out.println("Driver=" + db.getDriverConnection(cc));
			DataSource ds = db.getDataSource(cc);
			System.out.println("DS=" + ds.getConnection());
			conn = db.getCachedConnection(cc, true, Connection.TRANSACTION_SERIALIZABLE);
			System.out.println("Cached=" + conn);
			System.out.println(db);
			//////////////////////////
			System.out.println("JAVA classpath: [\n" +
				System.getProperty("java.class.path") + "\n]");
				DatabaseMetaData dmd = conn.getMetaData();
				System.out.println("DriverVersion: ["+
				dmd.getDriverVersion()+"]");
				System.out.println("DriverMajorVersion: ["+
				dmd.getDriverMajorVersion()+"]");
				System.out.println("DriverMinorVersion: ["+
				dmd.getDriverMinorVersion()+"]");
				System.out.println("DriverName: ["+
				dmd.getDriverName()+"]");
				System.out.println("ProductName: ["+
				dmd.getDatabaseProductName() +"]");
				System.out.println("ProductVersion: [\n"+
				dmd.getDatabaseProductVersion()+"\n]"); 
			//////////////////////////
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		db.cleanup();
		
		System.out.println("--------------------------------------------------");
		try
		{
			Connection conn1 = db.getCachedConnection(cc, false, Connection.TRANSACTION_SERIALIZABLE);
			Connection conn2 = db.getCachedConnection(cc, true, Connection.TRANSACTION_READ_COMMITTED);
			Connection conn3 = db.getCachedConnection(cc, false, Connection.TRANSACTION_READ_COMMITTED);
			System.out.println("3 -> " + db);
			conn1.close();
			conn2.close();
			conn1 = db.getCachedConnection(cc, true, Connection.TRANSACTION_SERIALIZABLE);
			conn2 = db.getCachedConnection(cc, true, Connection.TRANSACTION_READ_COMMITTED);
			System.out.println("3 -> " + db);
			conn1.close();
			conn2.close();
			conn3.close();
			System.out.println("0 -> " + db);
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		
		db.cleanup();
		
	//	System.exit(0);
		System.out.println("--------------------------------------------------");
		
		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.getConnectionRO());
		System.out.println(DB.getConnectionRW());
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));
		System.out.println(DB.createConnection(false, Connection.TRANSACTION_SERIALIZABLE));

		System.out.println(db);


		try
		{
			System.out.println("-- Sleeping --");
			Thread.sleep(60000);
			System.out.println(db);
			db.close();
			db.cleanup();
			System.out.println(db);
		}
		catch (InterruptedException e)
		{
		}
				
		
		
		/**
		//	Connection option 1
		try
		{
			System.setProperty("oracle.jdbc.Trace", "true");
			DriverManager.registerDriver(new OracleDriver());
			Connection con = DriverManager.getConnection("jdbc:oracle:thin:@dev:1521:dev", "compiere", "compiere");
			System.out.println("Catalog=" + con.getCatalog());
			DatabaseMetaData md = con.getMetaData();
			System.out.println("URL=" + md.getURL());
			System.out.println("User=" + md.getUserName());
			//
			System.out.println("Catalog");
			ResultSet rs = md.getCatalogs();
			while (rs.next())
				System.out.println("- " + rs.getString(1));
			//
			System.out.println("Table");
			rs = md.getTables(null, "COMPIERE", null, new String[] {"TABLE"});
			while (rs.next())
				System.out.println("- User=" + rs.getString(2) + " | Table=" + rs.getString(3)
					+ " | Type=" + rs.getString(4) + " | " + rs.getString(5));
			//
			System.out.println("Column");
			rs = md.getColumns(null, "COMPIERE", "C_ORDER", null);
			while (rs.next())
				System.out.println("- Tab=" + rs.getString(3) + " | Col=" + rs.getString(4)
					+ " | Type=" + rs.getString(5) + ", " + rs.getString(6)
					+ " | Size=" + rs.getString(7) + " | " + rs.getString(8)
					+ " | Digits=" + rs.getString(9) + " | Radix=" + rs.getString(10)
					+ " | Null=" + rs.getString(11) + " | Rem=" + rs.getString(12)
					+ " | Def=" + rs.getString(13) + " | " + rs.getString(14)
					+ " | " + rs.getString(15) + " | " + rs.getString(16)
					+ " | Ord=" + rs.getString(17) + " | Null=" + rs.getString(18)
					);

			con.close();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
		**/
	}	//	main
	
}   //  DB_Oracle
