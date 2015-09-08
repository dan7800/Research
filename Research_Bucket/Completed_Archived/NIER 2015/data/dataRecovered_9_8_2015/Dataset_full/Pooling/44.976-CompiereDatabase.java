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

//import org.compiere.util.CPreparedStatement;

/**
 *  Interface for Compiere Databases
 *
 *  @author     Jorg Janke
 *  @version    $Id: CompiereDatabase.java,v 1.14 2004/01/26 06:01:03 jjanke Exp $
 */
public interface CompiereDatabase
{
	/**
	 *  Get Database Name
	 *  @return database short name
	 */
	public String getName();

	/**
	 *  Get Database Description
	 *  @return database long name and version
	 */
	public String getDescription();

	/**
	 *  Get Database Driver
	 *  @return Driver
	 */
	public Driver getDriver();


	/**
	 *  Get Standard JDBC Port
	 *  @return standard port
	 */
	public int getStandardPort();

	/**
	 *  Get Database Connection String
	 *  @param connection Connection Descriptor
	 *  @return connection String
	 */
	public String getConnectionURL (CConnection connection);

	/**
	 *  Supports BLOB
	 *  @return true if BLOB is supported
	 */
	public boolean supportsBLOB();

	/**
	 *  String Representation
	 *  @return info
	 */
	public String toString();

	/**************************************************************************
	 *  Convert an individual Oracle Style statements to target database statement syntax
	 *
	 *  @param oraStatement oracle statement
	 *  @return converted Statement
	 *  @throws Exception
	 */
	public String convertStatement (String oraStatement);

	/**************************************************************************
	 *  Set the RowID
	 *  @param pstmt prepared statement
	 *  @param pos position
	 *  @param rowID ROWID
	 *  @throws SQLException
	 */
	public void setRowID (PreparedStatement pstmt, int pos, Object rowID) throws SQLException;

	/**
	 *  Get rhe RowID
	 *  @param rs result set
	 *  @param pos position
	 *  @return rowID ROWID
	 *  @throws SQLException
	 */
	public Object getRowID (ResultSet rs, int pos) throws SQLException;

	/**
	 *  Get RowSet
	 * 	@param rs result set
	 *  @return RowSet
	 *  @throws SQLException
	 */
	public RowSet getRowSet (ResultSet rs) throws SQLException;

	/**
	 * 	Get Cached Connection on Server
	 *	@param connection info
	 *	@return connection or null
	 */
	public Connection getCachedConnection (CConnection connection);

	/**
	 * 	Create DataSource (Client)
	 *	@param connection connection
	 *	@return data dource
	 */
	public DataSource createDataSource(CConnection connection);

	/**
	 * 	Create Pooled DataSource (Server)
	 *	@param connection connection
	 *	@return data dource
	 */
	public ConnectionPoolDataSource createPoolDataSource(CConnection connection);

}   //  CompiereDatabase

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

//import org.compiere.util.CPreparedStatement;

/**
 *  Interface for Compiere Databases
 *
 *  @author     Jorg Janke
 *  @version    $Id: CompiereDatabase.java,v 1.14 2004/01/26 06:01:03 jjanke Exp $
 */
public interface CompiereDatabase
{
	/**
	 *  Get Database Name
	 *  @return database short name
	 */
	public String getName();

	/**
	 *  Get Database Description
	 *  @return database long name and version
	 */
	public String getDescription();

	/**
	 *  Get Database Driver
	 *  @return Driver
	 */
	public Driver getDriver();


	/**
	 *  Get Standard JDBC Port
	 *  @return standard port
	 */
	public int getStandardPort();

	/**
	 *  Get Database Connection String
	 *  @param connection Connection Descriptor
	 *  @return connection String
	 */
	public String getConnectionURL (CConnection connection);

	/**
	 *  Supports BLOB
	 *  @return true if BLOB is supported
	 */
	public boolean supportsBLOB();

	/**
	 *  String Representation
	 *  @return info
	 */
	public String toString();

	/**************************************************************************
	 *  Convert an individual Oracle Style statements to target database statement syntax
	 *
	 *  @param oraStatement oracle statement
	 *  @return converted Statement
	 *  @throws Exception
	 */
	public String convertStatement (String oraStatement);

	/**************************************************************************
	 *  Set the RowID
	 *  @param pstmt prepared statement
	 *  @param pos position
	 *  @param rowID ROWID
	 *  @throws SQLException
	 */
	public void setRowID (PreparedStatement pstmt, int pos, Object rowID) throws SQLException;

	/**
	 *  Get rhe RowID
	 *  @param rs result set
	 *  @param pos position
	 *  @return rowID ROWID
	 *  @throws SQLException
	 */
	public Object getRowID (ResultSet rs, int pos) throws SQLException;

	/**
	 *  Get RowSet
	 * 	@param rs result set
	 *  @return RowSet
	 *  @throws SQLException
	 */
	public RowSet getRowSet (ResultSet rs) throws SQLException;

	/**
	 * 	Get Cached Connection on Server
	 *	@param connection info
	 *	@return connection or null
	 */
	public Connection getCachedConnection (CConnection connection);

	/**
	 * 	Create DataSource (Client)
	 *	@param connection connection
	 *	@return data dource
	 */
	public DataSource createDataSource(CConnection connection);

	/**
	 * 	Create Pooled DataSource (Server)
	 *	@param connection connection
	 *	@return data dource
	 */
	public ConnectionPoolDataSource createPoolDataSource(CConnection connection);

}   //  CompiereDatabase

