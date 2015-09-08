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
package org.compiere.util;

import java.math.*;
import java.util.*;
import java.text.*;
import java.security.*;
import javax.swing.*;

import java.sql.*;
import javax.sql.*;
import oracle.jdbc.*;

import org.apache.log4j.Logger;

import org.compiere.Compiere;
import org.compiere.util.*;
import org.compiere.db.*;

/**
 *  General Database Interface
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB.java,v 1.22 2003/08/11 19:41:40 jjanke Exp $
 */
public final class DB
{
	/** Connection Descriptor           */
	private static CConnection      s_cc = null;
	/** Connection Cache r/o            */
	private static Connection[]		s_connections = null;
	/** Connection Cache Size           */
	private static int              s_conCacheSize = 2;   //  client
	/** Connection counter              */
	private static int              s_conCount = 0;
	/** Connection r/w                  */
	private static Connection		s_connectionRW = null;
	/**	Logger							*/
	private static Logger			s_log = Logger.getLogger (DB.class);

	/**
	 *	Client Login.
	 *  <p>
	 *  - Get Connection
	 *  - Compare User info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 * @param ctx context
	 * @param cc connection
	 * @param app_user user
	 * @param app_pwd pwd
	 * @param force ignore pwd
	 * @return  Array of Role KeyNamePair or null if error
	 * The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	protected static KeyNamePair[] login (Properties ctx,
		CConnection cc,
		String app_user, String app_pwd, boolean force)
	{
		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		//	Establish connection
		closeTarget();
		setDBTarget(cc);
		Env.setContext(ctx, "#Host", s_cc.getAppsHost());
		Env.setContext(ctx, "#Database", s_cc.getDbName());
		if (getConnectionRO() == null)
		{
			Log.saveError("NoDatabase", "");
			return null;
		}
		if (app_pwd == null)
			return null;
		//
		return loginDB (ctx, app_user, app_pwd, force);
	}   //  login

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user Principal
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx, Principal app_user)
	{
		if (app_user == null)
			return null;
		//  login w/o password as previously authorized
		return loginDB (ctx, app_user.getName(), null, false);
	}   //  app_user

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user user id
	 *  @param app_pwd password
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx,
		String app_user, String app_pwd)
	{
		if (app_pwd == null)
			return null;
		return loginDB (ctx, app_user, app_pwd, false);
	}   //  login

	/**
	 *  Actual DB login procedure.
	 *
	 *  @param ctx context
	 *  @param app_user user
	 *  @param app_pwd pwd
	 *  @param force ignore pwd
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	private static KeyNamePair[] loginDB (Properties ctx,
		String app_user, String app_pwd, boolean force)
	{
		s_log.info("login - User=" + app_user);

		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		if (app_user == null)
			return null;

		KeyNamePair[] retValue = null;
		ArrayList list = new ArrayList();
		//
		String sql = "SELECT AD_User.AD_User_ID, AD_User.Description,"
			+ " AD_Role.AD_Role_ID, AD_Role.Name "
			+ "FROM AD_User, AD_User_Roles, AD_Role "
			+ "WHERE AD_User.AD_User_ID=AD_User_Roles.AD_User_ID"
			+ " AND AD_User_Roles.AD_Role_ID=AD_Role.AD_Role_ID"
			+ " AND AD_User.Name=?"		        							//	#1
			+ " AND AD_User.IsActive='Y' AND AD_Role.IsActive='Y' AND AD_User_Roles.IsActive='Y'";
		if (app_pwd != null)
			sql += " AND (AD_User.Password=? OR AD_User.Password=?)";   	//  #2/3
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setString(1, app_user);
			if (app_pwd != null)
			{
				pstmt.setString(2, app_pwd);
				pstmt.setString(3, Secure.getDigest(app_pwd));
			}
			//	execute a query
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())		//	no record found
				if (force)
				{
					Env.setContext(ctx, "#AD_User_Name", "System");
					Env.setContext(ctx, "#AD_User_ID", "0");
					Env.setContext(ctx, "#AD_User_Description", "System Forced Login");
					Env.setContext(ctx, "#User_Level", "S  ");  	//	Format 'SCO'
					Env.setContext(ctx, "#User_Client", "0");		//	Format c1, c2, ...
					Env.setContext(ctx, "#User_Org", "0"); 		//	Format o1, o2, ...
					rs.close();
					pstmt.close();
					retValue = new KeyNamePair[] {new KeyNamePair(0, "System Administrator")};
					return retValue;
				}
				else
				{
					rs.close();
					pstmt.close();
					Log.saveError("UserPwdError", app_user, false);
					return null;
				}

			Env.setContext(ctx, "#AD_User_Name", app_user);
			Env.setContext(ctx, "#AD_User_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#SalesRep_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#AD_User_Description", rs.getString("Description"));
			//
			Ini.setProperty(Ini.P_UID, app_user);
			if (Ini.getPropertyBool(Ini.P_STORE_PWD))
				Ini.setProperty(Ini.P_PWD, app_pwd);

			do	//	read all roles
			{
				int AD_Role_ID = rs.getInt("AD_Role_ID");
				String Name = rs.getString("Name");
				KeyNamePair p = new KeyNamePair(AD_Role_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("login", ex);
			Log.saveError("DBLogin", ex.getLocalizedMessage());
			return null;
		}

		//	Change via SQL detection comes here
		Env.setContext(ctx, "#User_SecurityID", "85263");
		//
		retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# roles = " + retValue.length);
		return retValue;
	}	//	login

	/**
	 *  Load Clients.
	 *  <p>
	 *  Sets Role info in context and loads its clients
	 *
	 *  @param ctx context
	 *  @param  role    role information
	 *  @return list of valid client KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadClients (Properties ctx, KeyNamePair role)
	{
		if (ctx == null || role == null)
			throw new IllegalArgumentException("DB.loadClients - required parameter missing");

		s_log.debug("loadClients - Role=" + role.toString());

		ArrayList list = new ArrayList();
		//	get Role details
		try
		{
			String sql = "SELECT DISTINCT r.UserLevel,r.ClientList,r.OrgList,"
				+ " r.C_Currency_ID,r.AmtApproval, oa.AD_Client_ID,c.Name "
				+ "FROM AD_Role r"
				+ " INNER JOIN AD_Role_OrgAccess oa ON (r.AD_Role_ID=oa.AD_Role_ID)"
				+ " INNER JOIN AD_Client c ON (oa.AD_Client_ID=c.AD_Client_ID) "
				+ "WHERE r.AD_Role_ID=?"		//	#1
				+ " AND r.IsActive='Y' AND c.IsActive='Y'";

			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, role.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadClients - No Clients for Role=" + role.getKey());
				return null;
			}

			//  Role Info
			Env.setContext(ctx, "#AD_Role_ID", role.getKey());
			Env.setContext(ctx, "#AD_Role_Name", role.getName());
			Ini.setProperty(Ini.P_ROLE, role.getName());

			//	User Level
			Env.setContext(ctx, "#User_Level", rs.getString(1));  	//	Format 'SCO'
			//	ClientList
			Env.setContext(ctx, "#User_Client", rs.getString(2));	//	Format c1, c2, ...
			//	OrgList
			Env.setContext(ctx, "#User_Org", rs.getString(3)); 		//	Format o1, o2, ...
			//  Approval Currency / Amount
			Env.setContext(ctx, "#Approval_C_Currency_ID", rs.getInt(4));
			BigDecimal approval = rs.getBigDecimal(5);
			String approvalStr = "0";
			if (approval != null)
				approvalStr = approval.toString();
			Env.setContext(ctx, "#Approval_Amt", approvalStr);


			//  load Clients
			do
			{
				int AD_Client_ID = rs.getInt(6);
				String Name = rs.getString(7);
				KeyNamePair p = new KeyNamePair(AD_Client_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadClients", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# clients = " + retValue.length);
		return retValue;
	}   //  loadClients

	/**
	 *  Load Organizations.
	 *  <p>
	 *  Sets Client info in context and loads its organization, the role has access to
	 *
	 *  @param ctx context
	 *  @param  client    client information
	 *  @return list of valid Org KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadOrgs (Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadOrgs - required parameter missing");

		s_log.debug("loadOrgs - Client=" + client.toString());

		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Role_ID");

		ArrayList list = new ArrayList();
		int AD_Role_ID = Env.getContextAsInt(ctx,"#AD_Role_ID");
		s_log.debug("AD_Role_ID=" + AD_Role_ID);

		//	get Client details for role
		try
		{
			String sql = "SELECT c.Value,c.SMTPHost,c.IsMultiLingualDocument,c.AD_Language,"	//	1..4
				+ " o.AD_Org_ID,o.Name "				//	5..6
				+ "FROM AD_Client c"
				+ " INNER JOIN AD_Org o ON (o.AD_Client_ID=c.AD_Client_ID) "
				+ "WHERE o.AD_Client_ID=?"  			//  #1
				+ " AND o.IsSummary='N' AND o.IsActive='Y'"
				+ " AND o.AD_Org_ID IN "
				+   "(SELECT AD_Org_ID FROM AD_Role_OrgAccess WHERE AD_Role_ID=?)"; //  #2
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, client.getKey());
			pstmt.setInt(2, AD_Role_ID);
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadOrgs - No Org for Client=" + client.getKey());
				return null;
			}

			//  Client Info
			Env.setContext(ctx, "#AD_Client_ID", client.getKey());
			Env.setContext(ctx, "#AD_Client_Name", client.getName());
			Ini.setProperty(Ini.P_CLIENT, client.getName());
			//
			Env.setContext(ctx, "#Client_Value", rs.getString(1));
			Env.setContext(ctx, "#Client_SMTP", rs.getString(2));
			Env.setContext(ctx, "#IsMultiLingualDocument", rs.getString(3));

			//  load Orgs
			do
			{
				int AD_Org_ID = rs.getInt(5);
				String Name = rs.getString(6);
				KeyNamePair p = new KeyNamePair(AD_Org_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadOrgs", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# orgs = " + retValue.length);
		return retValue;
	}   //  loadOrgs

	/**
	 *	Load Preferences into Context for selected client.
	 *  <p>
	 *  Sets Org info in context and loads relevant field from
	 *	- AD_Client/Info,
	 *  - C_AcctSchema,
	 *  - C_AcctSchema_Elements
	 *	- AD_Preference
	 *  <p>
	 *  Assumes that the context is set for #AD_Client_ID, #AD_User_ID, #AD_Role_ID
	 *
	 *  @param ctx context
	 *  @param  org    org information
	 *  @param  warehouse   optional warehouse information
	 *  @param  timestamp   optional date
	 *  @param  printerName optional printer info
	 *  @returns AD_Message of error (NoValidAcctInfo) or ""
	 */
	public static String loadPreferences (Properties ctx,
		KeyNamePair org, KeyNamePair warehouse, Timestamp timestamp, String printerName)
	{
		s_log.info("loadPreferences - Org=" + org);

		if (ctx == null || org == null)
			throw new IllegalArgumentException("DB.loadPreferences - required parameter missing");
		if (Env.getContext(ctx,"#AD_Client_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Client_ID");
		if (Env.getContext(ctx,"#AD_User_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_User_ID");
		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Role_ID");


		//  Org Info - assumes that it is valid
		Env.setContext(ctx, "#AD_Org_ID", org.getKey());
		Env.setContext(ctx, "#AD_Org_Name", org.getName());
		Ini.setProperty(Ini.P_ORG, org.getName());

		//  Warehouse Info
		if (warehouse != null)
		{
			Env.setContext(ctx, "#M_Warehouse_ID", warehouse.getKey());
			Ini.setProperty(Ini.P_WAREHOUSE, warehouse.getName());
		}

		//	Date (default today)
		long today = System.currentTimeMillis();
		if (timestamp != null)
			today = timestamp.getTime();
		java.sql.Date sd = new java.sql.Date(today);
		Env.setContext(ctx, "#Date", sd.toString());	//	YYYY-MM-DD

		//	Optional Printer
		if (printerName == null)
			printerName = "";
		Env.setContext(ctx, "#Printer", printerName);
		Ini.setProperty(Ini.P_PRINTER, printerName);

		//	Other
		Env.setAutoCommit(ctx, Ini.getProperty(Ini.P_A_COMMIT).equals("Y"));
		Env.setContext(ctx, "#CompiereSys", Ini.getProperty(Ini.P_COMPIERESYS));
		Env.setContext(ctx, "#ShowAcct", Ini.getProperty(Ini.P_SHOW_ACCT));
		Env.setContext(ctx, "#ShowTrl", Ini.getProperty(Ini.P_SHOW_TRL));

		String retValue = "";
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		int AD_Org_ID =  org.getKey();
		int AD_User_ID =  Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID =  Env.getContextAsInt(ctx, "#AD_Role_ID");

		//	Other Settings
		Env.setContext(ctx, "#YYYY", "Y");
		Env.setContext(ctx, "#StdPrecision", 2);

		//	AccountSchema Info (first)
		String sql = "SELECT * "
			+ "FROM C_AcctSchema a, AD_ClientInfo c "
			+ "WHERE a.C_AcctSchema_ID=c.C_AcctSchema1_ID "
			+ "AND c.AD_Client_ID=?";
		try
		{
			int C_AcctSchema_ID = 0;
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				//  No Warning for System
				if (AD_Role_ID != 0)
					retValue = "NoValidAcctInfo";
			}
			else
			{
				//	Accounting Info
				C_AcctSchema_ID = rs.getInt("C_AcctSchema_ID");
				Env.setContext(ctx, "$C_AcctSchema_ID", C_AcctSchema_ID);
				Env.setContext(ctx, "$C_Currency_ID", rs.getInt("C_Currency_ID"));
				Env.setContext(ctx, "$HasAlias", rs.getString("HasAlias"));
			}
			rs.close();
			pstmt.close();

			//	Accounting Elements
			sql = "SELECT ElementType "
				+ "FROM C_AcctSchema_Element "
				+ "WHERE C_AcctSchema_ID=?"
				+ " AND IsActive='Y'";
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, C_AcctSchema_ID);
			rs = pstmt.executeQuery();
			while (rs.next())
				Env.setContext(ctx, "$Element_" + rs.getString("ElementType"), "Y");
			rs.close();
			pstmt.close();

			//	This reads all relevant window neutral defaults
			//	overwriting superseeded ones.  Window specific is read in Mainain
			sql = "SELECT Attribute, Value, AD_Window_ID "
				+ "FROM AD_Preference "
				+ "WHERE AD_Client_ID IN (0, @#AD_Client_ID@)"
				+ " AND AD_Org_ID IN (0, @#AD_Org_ID@)"
				+ " AND (AD_User_ID IS NULL OR AD_User_ID=0 OR AD_User_ID=@#AD_User_ID@)"
				+ " AND IsActive='Y' "
				+ "ORDER BY Attribute, AD_Client_ID, AD_User_ID DESC, AD_Org_ID";
				//	the last one overwrites - System - Client - User - Org - Window
			sql = Env.parseContext(ctx, 0, sql, false);
			if (sql.length() == 0)
				s_log.error("loadPreferences - Missing Environment");
			else
			{
				pstmt = prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					int AD_Window_ID = rs.getInt(3);
					String at = "";
					if (rs.wasNull())
						at = "P|" + rs.getString(1);
					else
						at = "P" + AD_Window_ID + "|" + rs.getString(1);
					String va = rs.getString(2);
					Env.setContext(ctx, at, va);
				}
				rs.close();
				pstmt.close();
			}
		}
		catch (SQLException ex)
		{
			s_log.error("loadPreferences (" + sql + ")", ex);
		}

		//	Default Values
		s_log.info("Default Values ...");
		sql = "SELECT t.TableName, c.ColumnName "
			+ "FROM AD_Column c "
			+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID) "
			+ "WHERE c.IsKey='Y' AND t.IsActive='Y'"
			+ " AND EXISTS (SELECT * FROM AD_Column cc "
			+ " WHERE ColumnName = 'IsDefault' AND t.AD_Table_ID=cc.AD_Table_ID AND cc.IsActive='Y')";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				loadDefault (ctx, rs.getString(1), rs.getString(2));
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("loadPreferences", e);
		}
		//
		Ini.saveProperties(Ini.isClient());
		//
		return retValue;
	}	//	loadPreferences

	/**
	 *	Load Default Value for Table into Context.
	 *
	 *  @param ctx context
	 *  @param TableName table name
	 *  @param ColumnName column name
	 */
	private static void loadDefault (Properties ctx, String TableName, String ColumnName)
	{
		String value = null;
		//
		String SQL = "SELECT " + ColumnName + " FROM " + TableName
			+ " WHERE IsDefault='Y' AND IsActive='Y' ORDER BY AD_Client_ID";
		SQL = Access.addROAccessSQL(ctx, SQL, TableName, false);
		try
		{
			PreparedStatement pstmt = prepareStatement(SQL);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())		//	overwrites system defaults
				value = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("loadDefault - " + TableName + " (" + SQL + ")", e);
			return;
		}
		//	Set Context Value
		if (value != null && value.length() != 0)
		{
			if (TableName.equals("C_DocType"))
				Env.setContext(ctx, "#C_DocTypeTarget_ID", value);
			else
				Env.setContext(ctx, "#" + ColumnName, value);
		}
	}	//	loadDefault

	/**
	 *  Load Warehouses
	 *
	 * @param ctx context
	 * @param client client
	 * @return Array of Warehouse Info
	 */
	public static KeyNamePair[] loadWarehouses(Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadWarehouses - required parameter missing");

		s_log.info("loadWarehouses - Client=" + client.toString());

		ArrayList list = new ArrayList();
		try
		{
			String sql = "SELECT M_Warehouse_ID, Name FROM M_Warehouse "
				+ "WHERE AD_Client_ID=? AND IsActive='Y'";  //  #1

			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, client.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.warn("loadWarehouses - No Warehouses for Client=" + client.getKey());
				return null;
			}

			//  load Warehousess
			do
			{
				int AD_Warehouse_ID = rs.getInt(1);
				String Name = rs.getString(2);
				KeyNamePair p = new KeyNamePair(AD_Warehouse_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadWarehouses", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.info("# warehouses = " + retValue.length);
		return retValue;
	}   //  loadWarehouses

	/*************************************************************************/

	/**
	 *  Set connection
	 *  @param cc connection
	 */
	public static void setDBTarget (CConnection cc)
	{
		if (cc == null)
			throw new IllegalArgumentException("DB.setDBTarget connection is NULL");

		if (s_cc == null)
			s_cc = cc;
		synchronized (s_cc)    //  use as mutex
		{
			s_cc = cc;
			//  Closing existing
			if (s_connections != null)
			{
				for (int i = 0; i < s_connections.length; i++)
				{
					try {
						s_connections[i].close();
					} catch (Exception e) {}
				}
			}
			s_connections = null;
		}
	}   //  setDBTarget

	/**
	 *  Is there a connection to the database ?
	 *  @return true, if connected to database
	 */
	public static boolean isConnected()
	{
		try
		{
			getConnectionRW();	//	try to get a connection
			return true;
		}
		catch (Exception e)
		{
		}
		return false;
	}   //  isConnected

	/**
	 *	Return (pooled) r/w AutoCommit, read committed connection
	 *
	 *  @return Connection (r/w)
	 */
	public static Connection getConnectionRW ()
	{
		//	check health of connection
		try
		{
			if (s_connectionRW == null)
				;
			else if (s_connectionRW.isClosed())
				s_connectionRW = null;
			else if (s_connectionRW instanceof OracleConnection && ((OracleConnection)s_connectionRW).pingDatabase(1) < 0)
				s_connectionRW = null;
			else
				 s_connectionRW.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		catch (Exception e)
		{
			s_connectionRW = null;
		}
		if (s_connectionRW == null)
			s_connectionRW = s_cc.getConnection (true, Connection.TRANSACTION_READ_COMMITTED);
		if (s_connectionRW == null)
			throw new UnsupportedOperationException("DB.getConnectionRW - @NoDBConnection@");
		return s_connectionRW;
	}   //  getConnectionRW

	/**
	 *	Return read committed, read/only connection with AutoCommit from pool
	 *  @return Connection (r/o)
	 */
	public static Connection getConnectionRO ()
	{
		try
		{
			synchronized (s_cc)    //  use as mutex as s_connection is null the first time
			{
				if (s_connections == null)
					s_connections = createConnections (Connection.TRANSACTION_READ_COMMITTED);     //  see below
			}
		}
		catch (Exception e)
		{
			s_log.error("getConnectionRO", e);
		}

		//  check health of connection
		int pos = s_conCount++;
		Connection connection = s_connections[pos % s_conCacheSize];
		try
		{
			if (connection == null)
				;
			else if (connection.isClosed())
				connection = null;
			else if (connection instanceof OracleConnection && ((OracleConnection)connection).pingDatabase(1) < 0)
				connection = null;
			else
				 connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		catch (Exception e)
		{
			connection = null;
		}
		//	Get new
		if (connection == null)
		{
			System.out.println("DB.getConnectionRO - replacing connection #" + pos % s_conCacheSize);
		//	s_log.error("getConnectionRO - replacing connection #" + pos % s_conCacheSize);
			connection = s_cc.getConnection (true, Connection.TRANSACTION_READ_COMMITTED);            //  see above
		/*	try
			{
				retValue.setReadOnly(true);     //  not supported by Oracle
			}
			catch (Exception e)
			{
				System.err.println("DB.getConnectionRO - Cannot set to R/O - " + e);
			}   */
			s_connections[pos % s_conCacheSize] = connection;
		}
		if (connection == null)
			throw new UnsupportedOperationException("DB.getConnectionRO - @NoDBConnection@");
		return connection;
	}	//	getConnectionRO

	/**
	 *	Create new Connection.
	 *  The connection must be closed explicitly by the application
	 *
	 *  @param autoCommit auto commit
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Connection connection
	 */
	public static Connection createConnection (boolean autoCommit, int trxLevel)
	{
		s_log.debug("createConnection " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", AutoCommit=" + autoCommit + ", TrxLevel=" + trxLevel);
		return s_cc.getConnection (autoCommit, trxLevel);
	}	//	createConnection

	/**
	 *	Create new set of r/o Connections.
	 *  R/O connection might not be supported by DB
	 *
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Array of Connections (size based on s_conCacheSize)
	 */
	private static Connection[] createConnections (int trxLevel)
	{
		s_log.debug("createConnections (" + s_conCacheSize + ") " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", TrxLevel=" + trxLevel);
		Connection cons[] = new Connection[s_conCacheSize];
		try
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				cons[i] = s_cc.getConnection (true, trxLevel);  //  auto commit
				if (cons[i] == null)
					System.err.println("createConnections - connection is NULL");	//	don't use log
			}
		}
		catch (Exception e)
		{
			//  Don't use Log
			System.err.println("DB.createConnections - " + e.getMessage());
		}
		return cons;
	}	//	createConnections

	/**
	 *  Get Database Driver.
	 *  Access to database specific functionality.
	 *  @return Compiere Database Driver
	 */
	public static CompiereDatabase getDatabase()
	{
		if (s_cc != null)
			return s_cc.getDatabase();
		return null;
	}   //  getDatabase

	/*************************************************************************/

	/**
	 *  Check database Version with Code version
	 *  @param ctx context
	 *  @return true if Database version (date) is the same
	 */
	public static boolean isDatabaseOK (Properties ctx)
	{
		//	Force loading Messages of current language
		Msg.getMsg(ctx, "0");

		//  Check Version
		String version = "?";
		String sql = "SELECT Version FROM AD_System";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				version = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("Problem with AD_System Table - Run system.sql script - " + e.toString());
			return false;
		}
		s_log.info("isDatabaseOK - DB_Version=" + version);
		//  Identical DB version
		if (Compiere.DB_VERSION.equals(version))
			return true;

		String AD_Message = "DatabaseVersionError";
		String title = org.compiere.Compiere.getName() + " " +  Msg.getMsg(ctx, AD_Message, true);
		//	Code assumes Database version {0}, but Database has Version {1}.
		String msg = Msg.getMsg(ctx, AD_Message);	//	complete message
		msg = MessageFormat.format(msg, new Object[] {Compiere.DB_VERSION, version});
		Object[] options = { UIManager.get("OptionPane.noButtonText"), "Migrate" };
		int no = JOptionPane.showOptionDialog (null, msg,
			title, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
			UIManager.getIcon("OptionPane.errorIcon"), options, options[0]);
		if (no == 1)
		{
			try
			{
				Class.forName("com.compiere.client.StartMaintain").newInstance();
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog (null,
					ex.getMessage() + "\nSee: http://www.compiere.com/maintain",
					title, JOptionPane.ERROR_MESSAGE);
				s_log.error("isDatabaseOK - " + ex.getMessage());
			}
		}
		return false;
	}   //  isDatabaseOK

	/*************************************************************************/

	/**
	 *	Security Context Login.
	 *  @param uid uid
	 *  @param pwd pwd
	 *  @param role role
	 *  @return true if connected
	 */
	public static boolean login_context (String uid, String pwd, String role)
	{
		if (uid == null || pwd == null || role == null || uid.length() == 0 || pwd.length() == 0 || role.length() == 0)
			throw new IllegalArgumentException("DB.login_context - required parameter missing");
		s_log.info("login_context uid=" + uid);
		if (uid == null || pwd == null || role == null)
			return false;
		//
		String SQL = "{CALL Compiere_Context.Login(?,?,?,?)}";
		boolean result = true;
		try
		{
			CallableStatement cstmt = getConnectionRO().prepareCall(SQL);
			cstmt.setString(1, uid);
			cstmt.setString(2, pwd);
			cstmt.setString(3, role);
			cstmt.setString(4, Language.getBaseAD_Language());
			result = cstmt.execute();
			cstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("login_context", e);
			result = false;
		}
		return result;
	}	//	login_context

	/*************************************************************************/

	/**
	 *	Close Target
	 */
	public static void closeTarget()
	{
		if (s_connections != null)
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				try
				{
					if (s_connections[i] != null)
						s_connections[i].close();
				}
				catch (SQLException e)
				{
					s_log.warn("close connection #" + i + " - " + e.getMessage());
				}
				s_connections[i] = null;
			}
			try
			{
				if (s_connectionRW != null)
					s_connectionRW.close();
			}
			catch (SQLException e)
			{
				s_log.error("close R/W connection", e);
			}
			s_connectionRW = null;
		}
		s_connections = null;
//		EJB.close();
	}	//	closeTarget

	/*************************************************************************/

	/**
	 *	Prepare Forward Read Only Call
	 *  @param RO_SQL sql (RO)
	 *  @return Callable Statement
	 */
	public static CallableStatement prepareCall(String RO_SQL)
	{
		if (RO_SQL == null || RO_SQL.length() == 0)
			throw new IllegalArgumentException("DB.prepareCall - required parameter missing - " + RO_SQL);
		//
		String sql = getDatabase().convertStatement(RO_SQL);
		try
		{
			return getConnectionRO().prepareCall
				(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		}
		catch (SQLException e)
		{
			s_log.error("prepareCall (" + sql + ")", e);
		}
		return null;
	}	//	prepareCall

	/*************************************************************************/

	/**
	 *	Prepare Read Only Statement
	 *  @param RO_SQL sql (RO)
	 *  @return Prepared Statement
	 */
	public static PreparedStatement prepareStatement(String RO_SQL)
	{
		return prepareStatement(RO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}	//	prepareStatement

	/**
	 *	Prepare Statement
	 *
	 *  @param SQL sql statement
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Prepared Statement r/o or r/w depending on concur
	 */
	public static PreparedStatement prepareStatement(String SQL, int resultSetType, int resultSetConcurrency)
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.prepareStatement - required parameter missing - " + SQL);
		//
		return new CompiereStatement(SQL, resultSetType, resultSetConcurrency);
		/**
		try
		{
			Connection conn = null;
			if (resultSetConcurrency == ResultSet.CONCUR_UPDATABLE)
				conn = getConnectionRW();
			else
				conn = getConnectionRO();
			return conn.prepareStatement (sql, resultSetType, resultSetConcurrency);
		}
		catch (SQLException e)
		{
			s_log.error("prepareStatement (" + sql + ")", e);
		}
		return null;
		**/
	}	//	prepareStatement

	/**
	 *	Create Read Only Statement
	 *  @return Statement
	 */
	public static Statement createStatement()
	{
		return createStatement (ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}	//	createStatement

	/**
	 *	Create Statement
	 *
	 *  @param type - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param concur - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Statement - either r/w ir r/o depending on concur
	 */
	public static Statement createStatement(int type, int concur)
	{
		try
		{
			Connection conn = null;
			if (concur == ResultSet.CONCUR_UPDATABLE)
				conn = getConnectionRW();
			else
				conn = getConnectionRO();
			return conn.createStatement(type, concur);
		}
		catch (SQLException e)
		{
			s_log.error("createStatement", e);
		}
		return null;
	}	//	createStatement

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param SQL sql
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String SQL)
	{
		return executeUpdate(SQL, false);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param SQL sql
	 * 	@param ignoreError if true, no execution error is reported
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String SQL, boolean ignoreError)
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.executeUpdate - required parameter missing - " + SQL);
		//
		String sql = getDatabase().convertStatement(SQL);
		int no = -1;
		Statement stmt = null;
		try
		{
			Connection conn = getConnectionRW();
			stmt = conn.createStatement();
			no = stmt.executeUpdate(sql);
		}
		catch (SQLException e)
		{
			if (!ignoreError)
			{
				s_log.error ("executeUpdate - " + sql, e);
				Log.saveError ("DBExecuteError", e.getLocalizedMessage ());
			}
		}
		finally
		{
			//  Always close cursor
			try
			{
				stmt.close();
			}
			catch (SQLException e2)
			{
				s_log.error("executeUpdate - cannot close statement");
			}
		}
		return no;
	}	//	execute Update


	/**
	 *	Commit - commit on RW connection.
	 *  Is not required as RW connection is AutoCommit
	 *  @param throwException if true, re-throws exception
	 *  @return true if not needed or success
	 *  @throws SQLException
	 */
	public static boolean commit (boolean throwException) throws SQLException
	{
		try
		{
			Connection conn = getConnectionRW();
		//	if (!conn.getAutoCommit())
				conn.commit();
		}
		catch (SQLException e)
		{
			s_log.error("commit", e);
			if (throwException)
				throw e;
			return false;
		}
		return true;
	}	//	commit

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @return next no
	 */
	public static int getKeyNextNo (Properties ctx, int WindowNo, String TableName)
	{
		if (ctx == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getKeyNextNo - required parameter missing");
		String CompiereSys = Env.getContext(ctx, "#CompiereSys");
		int AD_Client_ID = Env.getContextAsInt(ctx, WindowNo, "AD_Client_ID");
		//
		return getKeyNextNo (AD_Client_ID, CompiereSys, TableName);
	}   //  getKeyNextNo

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param AD_Client_ID client
	 *  @param CompiereSys compiere sys
	 *  @param TableName table name
	 *  @return next no
	 */
	public static int getKeyNextNo (int AD_Client_ID, String CompiereSys, String TableName)
	{
		if (TableName == null || CompiereSys == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getKeyNextNo - required parameter missing");
		int retValue = 0;

		//	Check CompiereSys
	//	if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
	//		throw new UnsupportedOperationException("DB.getKeyNextNo - Cannot add System records");
		//
		try
		{
			String SQL = "{CALL AD_Sequence_Next(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setString(1, TableName);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.NUMERIC);
			cstmt.executeUpdate();
			retValue = cstmt.getInt(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getKeyNextNo - Table=" + TableName + ")", e);
		}
		return retValue;
	}	//	getKeyNextNo

	/**
	 *	Get Document Number for current document.
	 *  <br>
	 *  - first search for DocType based Document No
	 *  - then Search for DocumentNo based on TableName
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @param onlyDocType Do not search for document no based on TableName
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (Properties ctx, int WindowNo, String TableName, boolean onlyDocType)
	{
		if (ctx == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getDocumentNo - required parameter missing");
		String CompiereSys = Env.getContext(ctx, "#CompiereSys");
		int AD_Client_ID = Env.getContextAsInt(ctx, WindowNo, "AD_Client_ID");

		//	Get C_DocType_ID from context - NO Defaults -
		int C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID");
		if (C_DocType_ID == 0)
			C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID");
		if (C_DocType_ID == 0)
		{
			s_log.debug("getDocumentNo - for Window=" + WindowNo
				+ " - Target=" + Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID") + "/" + Env.getContextAsInt(ctx, WindowNo, "C_DocTypeTarget_ID")
				+ " - Actual=" + Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID") + "/" + Env.getContextAsInt(ctx, WindowNo, "C_DocType_ID"));
			return getDocumentNo (AD_Client_ID, CompiereSys, TableName);
		}

		//	Check CompiereSys
		if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
			throw new UnsupportedOperationException("DB.getDocumentNo - Cannot add System records");
		//
		String retValue = getDocumentNo(AD_Client_ID, C_DocType_ID);
		if (!onlyDocType && retValue == null)
			return getDocumentNo (AD_Client_ID, CompiereSys, TableName);
		return retValue;
	}	//	getDocumentNo

	/**
	 *	Get Document Number for current document.
	 *  @param AD_Client_ID client
	 *  @param C_DocType_ID (target) document type
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (int AD_Client_ID, int C_DocType_ID)
	{
		String retValue = null;
		try
		{
			String SQL = "{CALL AD_Sequence_DocType(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setInt(1, C_DocType_ID);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.VARCHAR);
			cstmt.executeUpdate();
			retValue = cstmt.getString(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getDocumentNo - DocType=" + C_DocType_ID, e);
		}
		s_log.info("getDocumentNo - DocType=" + C_DocType_ID + " -> " + retValue);
		return retValue;
	}	//	getDocumentNo


	/**
	 *  Get Next Document No
	 *  @param AD_Client_ID client
	 *  @param CompiereSys system
	 *  @param TableName table name
	 *  @return DocumentNo
	 */
	public static String getDocumentNo (int AD_Client_ID, String CompiereSys, String TableName)
	{
		if (CompiereSys == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getDocumentNo - required parameter missing");
		//	Check CompiereSys
	//	if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
	//		throw new UnsupportedOperationException("DB.getDocumentNo - Cannot add System records");
		//
		String retValue = null;
		try
		{
			String SQL = "{CALL AD_Sequence_Doc(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setString(1, "DocumentNo_" + TableName);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.VARCHAR);
			cstmt.executeUpdate();
			retValue = cstmt.getString(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getDocumentNo  TableName=" + TableName, e);
		}
		s_log.info("getDocumentNo - TableName=" + TableName + " -> " + retValue);
		return retValue;
	}   //  getDocumentNo


	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int int_param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, String int_param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setString(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@param s_param2 parameter 2
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int int_param1, String s_param2)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			pstmt.setString(2, s_param2);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value: " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue


	/**
	 *  Convert an amount with today's spot rate
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  Amt         The amount to be converted
	 * 	@param	AD_Client_ID client
	 * 	@param	AD_Org_ID	organization
	 *  @return converted amount
	 */
	public static BigDecimal getConvertedAmt (BigDecimal Amt, int CurFrom_ID, int CurTo_ID, int AD_Client_ID, int AD_Org_ID)
	{
		return getConvertedAmt (Amt, CurFrom_ID, CurTo_ID, null, null, AD_Client_ID, AD_Org_ID);
	}   //  getConvertedAmt

	/**
	 *	Convert an amount
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  ConvDate    The Conversion date - if null - use current date
	 *  @param  RateType    The Conversion rate type - if null/empty - use Spot
	 *  @param  Amt         The amount to be converted
	 * 	@param	AD_Client_ID client
	 * 	@param	AD_Org_ID	organization
	 *  @return converted amount
	 */
	public static BigDecimal getConvertedAmt (BigDecimal Amt, int CurFrom_ID, int CurTo_ID,
		Timestamp ConvDate, String RateType, int AD_Client_ID, int AD_Org_ID)
	{
		if (Amt == null)
			throw new IllegalArgumentException("DB.getConvertedAmt - required parameter missing - Amt");
		BigDecimal retValue = null;
		if (CurFrom_ID == CurTo_ID || Amt.equals(Env.ZERO))
			return Amt;
		//
		try
		{
			String sql = "{? = call C_Currency_Convert(?,?,?,?,?, ?,?) }";
			CallableStatement cstmt = prepareCall(sql);
			//
			cstmt.registerOutParameter(1, Types.NUMERIC);
			//
			cstmt.setBigDecimal(2, Amt);					//	Amount		IN  	NUMBER
			cstmt.setInt(3, CurFrom_ID);             		//	CurFrom		IN 		NUMBER
			cstmt.setInt(4, CurTo_ID);                  	//	CurTo		IN 		NUMBER
			if (ConvDate == null)
				ConvDate = new Timestamp (System.currentTimeMillis());
			cstmt.setTimestamp(5, ConvDate);				//	ConvDate	IN 		DATE
			if (RateType == null || RateType.equals(""))
				RateType = "S";
			cstmt.setString(6, RateType);					//	RateType	IN 		CHAR
			cstmt.setInt(7, AD_Client_ID);
			cstmt.setInt(8, AD_Org_ID);
			//
			cstmt.executeUpdate();
			retValue = cstmt.getBigDecimal(1);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getConvertedAmt", e);
		}
		if (retValue == null)
			s_log.info("getConvertedAmt - not found - CurFrom=" + CurFrom_ID + ", CurTo=" + CurTo_ID
				+ ", " + ConvDate + ", " + RateType + ", Client=" + AD_Client_ID + ", Org=" + AD_Org_ID);
		return retValue;
	}	//	getConvertedAmt

	/**
	 *	Get Currency Rate
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  ConvDate    The Conversion date - if null - use current date
	 *  @param  RateType    The Conversion rate type - if null/empty - use Spot
	 * 	@param	AD_Client_ID client
	 * 	@param	AD_Org_ID	organization
	 *  @return currency Rate or null
	 */
	public static BigDecimal getConvesionRate (int CurFrom_ID, int CurTo_ID,
		Timestamp ConvDate, String RateType, int AD_Client_ID, int AD_Org_ID)
	{
		BigDecimal retValue = null;
		if (CurFrom_ID == CurTo_ID)
			return new BigDecimal(1);
		//
		try
		{
			String sql = "{? = call C_Currency_Rate(?,?,?,?,?,?)}";
			CallableStatement cstmt = prepareCall(sql);
			//
			cstmt.registerOutParameter(1, Types.NUMERIC);
			//
			cstmt.setInt(2, CurFrom_ID);             		//	CurFrom		IN 		NUMBER
			cstmt.setInt(3, CurTo_ID);                  	//	CurTo		IN 		NUMBER
			if (ConvDate == null)
				ConvDate = new Timestamp (System.currentTimeMillis());
			cstmt.setTimestamp(4, ConvDate);				//	ConvDate	IN 		DATE
			if (RateType == null || RateType.equals(""))
				RateType = "S";
			cstmt.setString(5, RateType);					//	RateType	IN 		CHAR
			cstmt.setInt(6, AD_Client_ID);
			cstmt.setInt(7, AD_Org_ID);
			//
			cstmt.executeUpdate();
			retValue = cstmt.getBigDecimal(1);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getConvesionRate", e);
		}
		if (retValue == null)
			s_log.info ("getConversionRate - not found - CurFrom=" + CurFrom_ID + ", CurTo=" + CurTo_ID
			  + ", " + ConvDate + ", " + RateType + ", Client=" + AD_Client_ID + ", Org=" + AD_Org_ID);
		return retValue;
	}	//	getConvesionRate


	/**
	 *  Get fully qualified Account Combination
	 *
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param C_AcctSchema_ID acct schema
	 *  @param Account_ID natural account
	 *  @param  base_ValidCombination_ID optional base combination to be specified
	 *  @param Alias aloas
	 *  @param AD_User_ID user
	 *  @param M_Product_ID product
	 *  @param C_BPartner_ID partner
	 *  @param AD_OrgTrx_ID trx org
	 *  @param C_LocFrom_ID loc from
	 *  @param C_LocTo_ID loc to
	 *  @param C_SRegion_ID sales region
	 *  @param C_Project_ID project
	 *  @param C_Campaign_ID campaign
	 *  @param C_Activity_ID activity
	 *  @param User1_ID user1
	 *  @param User2_ID user2
	 *  @return C_ValidCombination_ID of existing or new Combination
	 */
	public static int getValidCombination (int AD_Client_ID, int AD_Org_ID,
		int C_AcctSchema_ID, int Account_ID, int base_ValidCombination_ID, String Alias, int AD_User_ID,
		int M_Product_ID, int C_BPartner_ID, int AD_OrgTrx_ID,
		int C_LocFrom_ID, int C_LocTo_ID, int C_SRegion_ID, int C_Project_ID,
		int C_Campaign_ID, int C_Activity_ID, int User1_ID, int User2_ID)
	{
		int retValue = 0;
		StringBuffer sb = new StringBuffer ("C_ValidCombination_Get[");
		try
		{
			String sql = "{CALL C_ValidCombination_Get(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)}";
			CallableStatement cstmt = prepareCall(sql);
			//  1 - ID
			cstmt.registerOutParameter(1, Types.NUMERIC);
			sb.append("v,");
			//  --  Mandatory Accounting fields
			//	2 - AD_Client_ID
			cstmt.setInt(2, AD_Client_ID);
			sb.append("AD_Client_ID=").append(AD_Client_ID).append(",");
			//	3 - AD_Org_ID
			cstmt.setInt(3, AD_Org_ID);
			sb.append("AD_Org_ID=").append(AD_Org_ID).append(",");
			//  4- C_AcctSchema_ID
			cstmt.setInt(4, C_AcctSchema_ID);
			sb.append("C_AcctSchema_ID=").append(C_AcctSchema_ID).append(",");
			//  5 - Account_ID
			cstmt.setInt(5, Account_ID);
			sb.append("Account_ID=").append(Account_ID).append(", ");

			//  --  Optional
			//  6 - Base C_ValidCombination_ID
			if (base_ValidCombination_ID != 0)
			{
				cstmt.setInt (6, base_ValidCombination_ID);
				sb.append("BaseValidCombination_ID=").append(base_ValidCombination_ID).append(",");
			}
			else
			{
				cstmt.setNull(6, Types.NUMERIC);
				sb.append("BaseValidCombination_ID=").append("NULL,");
			}
			//  7 - MustBeFullyQualified
			cstmt.setString(7, "Y");
			sb.append("MustBeFullyQualified='Y',");
			//  8 - Alias
			if (Alias != null && Alias.length() > 0)
			{
				cstmt.setString(8, Alias);
				sb.append("Alias='").append(Alias).append("',");
			}
			else
			{
				cstmt.setNull(8, Types.VARCHAR);
				sb.append("Alias=NULL,");
			}
			//  9 - CreatedBy
			cstmt.setInt(9, AD_User_ID);
			sb.append("AD_User_ID=").append(AD_User_ID).append(", ");

			//	--  Optional Accounting fields
			//  10 - M_Product_ID
			if (M_Product_ID != 0)
			{
				cstmt.setInt(10, M_Product_ID);
				sb.append("M_Product_ID=").append(M_Product_ID).append(",");
			}
			else
			{
				cstmt.setNull(10, Types.NUMERIC);
				sb.append("M_Product_ID=NULL,");
			}
			//  11 - C_BPartner_ID
			if (C_BPartner_ID != 0)
			{
				cstmt.setInt(11, C_BPartner_ID);
				sb.append("C_BPartner_ID=").append(C_BPartner_ID).append(",");
			}
			else
			{
				cstmt.setNull(11, Types.NUMERIC);
				sb.append("C_BPartner_ID=NULL,");
			}
			//  12 - AD_OrgTrx_ID
			if (AD_OrgTrx_ID != 0)
			{
				cstmt.setInt(12, AD_OrgTrx_ID);
				sb.append("AD_OrgTrx_ID=").append(AD_OrgTrx_ID).append(",");
			}
			else
			{
				cstmt.setNull(12, Types.NUMERIC);
				sb.append("AD_OrgTrx_ID=NULL,");
			}
			//  13 - C_LocFrom_ID
			if (C_LocFrom_ID != 0)
			{
				cstmt.setInt(13, C_LocFrom_ID);
				sb.append("C_LocFrom_ID=").append(C_LocFrom_ID).append(",");
			}
			else
			{
				cstmt.setNull(13, Types.NUMERIC);
				sb.append("C_LocFrom=NULL,");
			}
			//  14 - C_LocTo_ID
			if (C_LocTo_ID != 0)
			{
				cstmt.setInt(14, (C_LocTo_ID));
				sb.append("C_LocTo_ID=").append(C_LocTo_ID).append(", ");
			}
			else
			{
				cstmt.setNull(14, Types.NUMERIC);
				sb.append("C_LocTo_ID=NULL, ");
			}
			//  15 - C_SalesRegion_ID
			if (C_SRegion_ID != 0)
			{
				cstmt.setInt(15, (C_SRegion_ID));
				sb.append("C_SalesRegion_ID=").append(C_SRegion_ID).append(",");
			}
			else
			{
				cstmt.setNull(15, Types.NUMERIC);
				sb.append("C_SalesRegion_ID=NULL,");
			}
			//  16 - C_Project_ID
			if (C_Project_ID != 0)
			{
				cstmt.setInt(16, (C_Project_ID));
				sb.append("C_Project_ID=").append(C_Project_ID).append(",");
			}
			else
			{
				cstmt.setNull(16, Types.NUMERIC);
				sb.append("C_Project_ID=NULL,");
			}
			//  17 - C_Campaign_ID
			if (C_Campaign_ID != 0)
			{
				cstmt.setInt(17, (C_Campaign_ID));
				sb.append("C_Campaign_ID=").append(C_Campaign_ID).append(",");
			}
			else
			{
				cstmt.setNull(17, Types.NUMERIC);
				sb.append("C_Campaign_ID=NULL,");
			}
			//  18 - C_Activity_ID
			if (C_Activity_ID != 0)
			{
				cstmt.setInt(18, (C_Activity_ID));
				sb.append("C_Activity_ID=").append(C_Activity_ID).append(",");
			}
			else
			{
				cstmt.setNull(18, Types.NUMERIC);
				sb.append("C_Activity_ID=NULL,");
			}
			//  19 - User1_ID
			if (User1_ID != 0)
			{
				cstmt.setInt(19, (User1_ID));
				sb.append("User1_ID=").append(User1_ID).append(",");
			}
			else
			{
				cstmt.setNull(19, Types.NUMERIC);
				sb.append("User1_ID=NULL,");
			}
			//  20 - User2_ID
			if (User2_ID != 0)
			{
				cstmt.setInt(20, (User2_ID));
				sb.append("User2_ID=").append(User2_ID).append(")");
			}
			else
			{
				cstmt.setNull(20, Types.NUMERIC);
				sb.append("User2_ID=NULL)");
			}

			//
			cstmt.executeUpdate();
			retValue = cstmt.getInt(1);     //  1 - ID
			cstmt.close();
			s_log.debug("getValidCombination " + sb.toString());
		}
		catch(SQLException e)
		{
			s_log.error("getValidCombination " + sb.toString(), e);
		}
		return retValue;
	}   //  getValidCombination

	/**
	 *  Insert Note
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param AD_User_ID user
	 *  @param AD_Table_ID table
	 *  @param Record_ID record
	 *  @param AD_MessageValue message
	 *  @param Text text
	 *  @param Reference subject
	 *  @return true if note was inserted
	 */
	public static boolean insertNote (int AD_Client_ID, int AD_Org_ID, int AD_User_ID,
		int AD_Table_ID, int Record_ID,
		String AD_MessageValue, String Text, String Reference)
	{
		if (AD_MessageValue == null || AD_MessageValue.length() == 0)
			throw new IllegalArgumentException("DB.insertNote - required parameter missing - AD_Message");

		//  Database limits
		if (Text == null)
			Text = "";
		if (Reference == null)
			Reference = "";
		//
		s_log.info("insertNote - " + AD_MessageValue + " - " + Reference);
		//
		StringBuffer sql = new StringBuffer ("INSERT INTO AD_Note (AD_Note_ID,");
		sql.append("AD_Client_ID,AD_Org_ID,IsActive, Created,CreatedBy,Updated,UpdatedBy, ")
			.append("AD_Message_ID,Text,Reference, ")
			.append("AD_User_ID,AD_Table_ID,Record_ID,Processed) ")
			.append("SELECT ");
		//
		String CompiereSys = "N";
		int AD_Note_ID = getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Note");
		sql.append(AD_Note_ID).append(",");
		//
		sql.append(AD_Client_ID).append(",")
			.append(AD_Org_ID).append(", 'Y',SysDate,")
			.append(AD_User_ID).append(",SysDate,0,");
		//	AD_Message_ID,Text,Reference,
		sql.append(" AD_Message_ID,").append(DB.TO_STRING(Text, 2000)).append(",")
			.append(DB.TO_STRING(Reference, 60)).append(", ");
		//	AD_User_ID,AD_Table_ID,Record_ID,Processed
		sql.append(AD_User_ID).append(",").append(AD_Table_ID).append(",")
			.append(Record_ID).append(",'N' ");
		//
		sql.append("FROM AD_Message WHERE Value='").append(AD_MessageValue).append("'");
		//  Create Entry
		int no = executeUpdate(sql.toString());

		//  AD_Message must exist, so if not created, it is probably
		//  due to non-existing AD_Message
		if (no == 0)
		{
			sql = new StringBuffer ("INSERT INTO AD_Note (AD_Note_ID,");
			sql.append("AD_Client_ID,AD_Org_ID,IsActive, Created,CreatedBy,Updated,UpdatedBy, ")
				.append("AD_Message_ID,Text,Reference, ")
				.append("AD_User_ID,AD_Table_ID,Record_ID,Processed) ")
				.append("SELECT ");
			//  - use AD_Note_ID from above
			sql.append(AD_Note_ID).append(",");
			//
			sql.append(AD_Client_ID).append(",")
				.append(AD_Org_ID).append(", 'Y',SysDate,")
				.append(AD_User_ID).append(",SysDate,0, ");
			//	AD_Message_ID,Text,Reference,
			sql.append("AD_Message_ID,").append(TO_STRING (AD_MessageValue + ": " + Text, 2000)).append(",")
				.append(TO_STRING(Reference,60)).append(", ");
			//	AD_User_ID,AD_Table_ID,Record_ID,Processed
			sql.append(AD_User_ID).append(",").append(AD_Table_ID).append(",")
				.append(Record_ID).append(",'N' ");
			//  Hardcoded AD_Message - AD_Message is in Text
			sql.append("FROM AD_Message WHERE Value='NoMessageFound'");
			//  Try again
			no = executeUpdate(sql.toString());
		}

		return no == 1;
	}   //  insertNote

	/*************************************************************************/

	/**
	 *	Print SQL Warnings.
	 *  <br>
	 *		Usage: DB.printWarning(rs.getWarnings(), "xx");
	 *  @param comment comment
	 *  @param warning warning
	 */
	public static void printWarning(String comment, SQLWarning warning)
	{
		if (comment == null || warning == null || comment.length() == 0)
			throw new IllegalArgumentException("DB.printException - required parameter missing");
		s_log.warn("SQL Warning: " + comment);
		if (warning == null)
			return;
		//
		SQLWarning warn = warning;
		while (warn != null)
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append(warn.getMessage());
			buffer.append("; State=").append(warn.getSQLState()).append("; ErrorCode=").append(warn.getErrorCode());
			s_log.warn(buffer.toString());
			warn = warn.getNextWarning();
		}
	}	//	printWarning

	/**
	 *  Create SQL TO Date String from Timestamp
	 *
	 *  @param  time Date to be converted
	 *  @param  dayOnly true if time set to 00:00:00
	 *
	 *  @return TO_DATE('2001-01-30 18:10:20',''YYYY-MM-DD HH24:MI:SS')
	 *      or  TO_DATE('2001-01-30',''YYYY-MM-DD')
	 */
	public static String TO_DATE (Timestamp time, boolean dayOnly)
	{
		if (time == null)
		{
			if (dayOnly)
				return "TRUNC(SysDate)";
			return "SysDate";
		}

		StringBuffer dateString = new StringBuffer("TO_DATE('");
		//  YYYY-MM-DD HH24:MI:SS.mmmm  JDBC Timestamp format
		String myDate = time.toString();
		if (dayOnly)
		{
			dateString.append(myDate.substring(0,10));
			dateString.append("','YYYY-MM-DD')");
		}
		else
		{
			dateString.append(myDate.substring(0, myDate.indexOf(".")));	//	cut off miliseconds
			dateString.append("','YYYY-MM-DD HH24:MI:SS')");
		}
		return dateString.toString();
	}   //  TO_DATE

	/**
	 *  Create SQL TO Date String from Timestamp
	 *  @param time time
	 *  @return TO_DATE String
	 */
	public static String TO_DATE (Timestamp time)
	{
		return TO_DATE(time, true);
	}   //  TO_DATE

	/**
	 *  Create SQL for formatted Date, Number
	 *
	 *  @see org.compiere.util.DisplayType
	 *  @see org.compiere.util.Env
	 *
	 *  @param  columnName  the column name in the SQL
	 *  @param  displayType Display Type
	 *  @param  AD_Language 6 character language setting (from Env.LANG_*)
	 *
	 *  @return TRIM(TO_CHAR(columnName,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.'''))
	 *      or TRIM(TO_CHAR(columnName,'TM9')) depending on DisplayType and Language
	 */
	public static String TO_CHAR (String columnName, int displayType, String AD_Language)
	{
		if (columnName == null || AD_Language == null || columnName.length() == 0)
			throw new IllegalArgumentException("DB.TO_CHAR - required parameter missing");

		StringBuffer retValue = new StringBuffer("TRIM(TO_CHAR(");
		retValue.append(columnName);

		//  Numbers
		if (DisplayType.isNumeric(displayType))
		{
			if (displayType == DisplayType.Amount)
				retValue.append(",'9G999G990D00'");
			else
				retValue.append(",'TM9'");
			//  TO_CHAR(GrandTotal,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.''')
			if (!Language.isDecimalPoint(AD_Language))      //  reversed
				retValue.append(",'NLS_NUMERIC_CHARACTERS='',.'''");
		}
		else if (DisplayType.isDate(displayType))
		{
			retValue.append(",'")
				.append(Language.getLanguage(AD_Language).getDBdatePattern())
				.append("'");
		}

		retValue.append("))");
		//
		return retValue.toString();
	}   //  TO_CHAR_Number


	/**
	 *  Package Strings for SQL command
	 *  @param txt  String with text
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt)
	{
		return TO_STRING (txt, 0);
	}   //  TO_STRING

	/**
	 *	Package Strings for SQL command.
	 *  <pre>
	 *		-	include in ' (single quotes)
	 *		-	replace ' with ''
	 *  </pre>
	 *  @param txt  String with text
	 *  @param maxLength    Maximum Length of content or 0 to ignore
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt, int maxLength)
	{
		if (txt == null)
			return "NULL";

		//  Length
		String text = txt;
		if (maxLength != 0 && text.length() > maxLength)
			text = txt.substring(0, maxLength);

		char quote = '\'';
		//  copy characters		(wee need to look through anyway)
		StringBuffer out = new StringBuffer();
		out.append(quote);		//	'
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (c == quote)
				out.append("''");
			else
				out.append(c);
		}
		out.append(quote);		//	'
		//
		return out.toString();
	}	//	TO_STRING

}	//	DB

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
package org.compiere.util;

import java.math.*;
import java.util.*;
import java.text.*;
import java.security.*;
import javax.swing.*;
import java.rmi.*;

import java.sql.*;

import javax.sql.*;
import oracle.jdbc.*;

import org.compiere.Compiere;
import org.compiere.db.*;
import org.compiere.model.*;
import org.compiere.interfaces.Server;

/**
 *  General Database Interface
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB.java,v 1.56 2004/05/13 06:08:10 jjanke Exp $
 */
public final class DB
{
	/** Connection Descriptor           */
	private static CConnection      s_cc = null;
	/** Connection Cache r/o            */
	private static Connection[]		s_connections = null;
	/** Connection Cache Size           */
	private static int              s_conCacheSize = Ini.isClient() ? 3 : 5;
	/** Connection counter              */
	private static int              s_conCount = 0;
	/** Connection r/w                  */
	private static Connection		s_connectionRW = null;
	/**	Logger							*/
	private static Logger			s_log = Logger.getCLogger (DB.class);

	/**
	 *	Client Login.
	 *  <p>
	 *  - Get Connection
	 *  - Compare User info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 * @param ctx context
	 * @param cc connection
	 * @param app_user user
	 * @param app_pwd pwd
	 * @param force ignore pwd
	 * @return  Array of Role KeyNamePair or null if error
	 * The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	protected static KeyNamePair[] login (Properties ctx,
		CConnection cc,
		String app_user, String app_pwd, boolean force)
	{
		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		//	Establish connection
		closeTarget();
		setDBTarget(cc);
		Env.setContext(ctx, "#Host", s_cc.getAppsHost());
		Env.setContext(ctx, "#Database", s_cc.getDbName());
		
		if (getConnectionRO() == null)
		{
			Log.saveError("NoDatabase", "");
			return null;
		}
		if (app_pwd == null)
			return null;
		//
		return loginDB (ctx, app_user, app_pwd, force);
	}   //  login

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user Principal
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx, Principal app_user)
	{
		if (app_user == null)
			return null;
		//  login w/o password as previously authorized
		return loginDB (ctx, app_user.getName(), null, false);
	}   //  app_user

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user user id
	 *  @param app_pwd password
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx,
		String app_user, String app_pwd)
	{
		if (app_pwd == null)
			return null;
		return loginDB (ctx, app_user, app_pwd, false);
	}   //  login

	/**
	 *  Actual DB login procedure.
	 *
	 *  @param ctx context
	 *  @param app_user user
	 *  @param app_pwd pwd
	 *  @param force ignore pwd
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	private static KeyNamePair[] loginDB (Properties ctx,
		String app_user, String app_pwd, boolean force)
	{
		s_log.info("login - User=" + app_user);

		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		if (app_user == null)
			return null;

		KeyNamePair[] retValue = null;
		ArrayList list = new ArrayList();
		//
		String sql = "SELECT AD_User.AD_User_ID, AD_User.Description,"
			+ " AD_Role.AD_Role_ID, AD_Role.Name "
			+ "FROM AD_User, AD_User_Roles, AD_Role "
			+ "WHERE AD_User.AD_User_ID=AD_User_Roles.AD_User_ID"
			+ " AND AD_User_Roles.AD_Role_ID=AD_Role.AD_Role_ID"
			+ " AND AD_User.Name=?"		        							//	#1
			+ " AND AD_User.IsActive='Y' AND AD_Role.IsActive='Y' AND AD_User_Roles.IsActive='Y'";
		if (app_pwd != null)
			sql += " AND (AD_User.Password=? OR AD_User.Password=?)";   	//  #2/3
		sql += " ORDER BY AD_Role.Name";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setString(1, app_user);
			if (app_pwd != null)
			{
				pstmt.setString(2, app_pwd);
				pstmt.setString(3, Secure.getDigest(app_pwd));
			}
			//	execute a query
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())		//	no record found
				if (force)
				{
					Env.setContext(ctx, "#AD_User_Name", "System");
					Env.setContext(ctx, "#AD_User_ID", "0");
					Env.setContext(ctx, "#AD_User_Description", "System Forced Login");
					Env.setContext(ctx, "#User_Level", "S  ");  	//	Format 'SCO'
					Env.setContext(ctx, "#User_Client", "0");		//	Format c1, c2, ...
					Env.setContext(ctx, "#User_Org", "0"); 		//	Format o1, o2, ...
					rs.close();
					pstmt.close();
					retValue = new KeyNamePair[] {new KeyNamePair(0, "System Administrator")};
					return retValue;
				}
				else
				{
					rs.close();
					pstmt.close();
					Log.saveError("UserPwdError", app_user, false);
					return null;
				}

			Env.setContext(ctx, "#AD_User_Name", app_user);
			Env.setContext(ctx, "#AD_User_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#SalesRep_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#AD_User_Description", rs.getString("Description"));
			//
			Ini.setProperty(Ini.P_UID, app_user);
			if (Ini.getPropertyBool(Ini.P_STORE_PWD))
				Ini.setProperty(Ini.P_PWD, app_pwd);

			do	//	read all roles
			{
				int AD_Role_ID = rs.getInt("AD_Role_ID");
				if (AD_Role_ID == 0)
					Env.setContext(ctx, "#SysAdmin", "Y");
				String Name = rs.getString("Name");
				KeyNamePair p = new KeyNamePair(AD_Role_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("login", ex);
			Log.saveError("DBLogin", ex);
			return null;
		}

		//	Change via SQL detection comes here
		Env.setContext(ctx, "#User_SecurityID", "85263");
		//
		retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# roles = " + retValue.length);
		return retValue;
	}	//	login

	/**
	 *  Load Clients.
	 *  <p>
	 *  Sets Role info in context and loads its clients
	 *
	 *  @param ctx context
	 *  @param  role    role information
	 *  @return list of valid client KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadClients (Properties ctx, KeyNamePair role)
	{
		if (ctx == null || role == null)
			throw new IllegalArgumentException("DB.loadClients - required parameter missing");

		s_log.debug("loadClients - Role=" + role.toString());

		ArrayList list = new ArrayList();
		//	get Role details
		try
		{
			String sql = "SELECT DISTINCT r.UserLevel,r.ClientList,r.OrgList,"
				+ " r.C_Currency_ID,r.AmtApproval, oa.AD_Client_ID,c.Name "
				+ "FROM AD_Role r"
				+ " INNER JOIN AD_Role_OrgAccess oa ON (r.AD_Role_ID=oa.AD_Role_ID)"
				+ " INNER JOIN AD_Client c ON (oa.AD_Client_ID=c.AD_Client_ID) "
				+ "WHERE r.AD_Role_ID=?"		//	#1
				+ " AND r.IsActive='Y' AND c.IsActive='Y'";

			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, role.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadClients - No Clients for Role=" + role.getKey());
				return null;
			}

			//  Role Info
			Env.setContext(ctx, "#AD_Role_ID", role.getKey());
			Env.setContext(ctx, "#AD_Role_Name", role.getName());
			Ini.setProperty(Ini.P_ROLE, role.getName());

			//	User Level
			Env.setContext(ctx, "#User_Level", rs.getString(1));  	//	Format 'SCO'
			//	ClientList
			Env.setContext(ctx, "#User_Client", rs.getString(2));	//	Format c1, c2, ...
			//	OrgList
			Env.setContext(ctx, "#User_Org", rs.getString(3)); 		//	Format o1, o2, ...
			//  Approval Currency / Amount
			Env.setContext(ctx, "#Approval_C_Currency_ID", rs.getInt(4));
			BigDecimal approval = rs.getBigDecimal(5);
			String approvalStr = "0";
			if (approval != null)
				approvalStr = approval.toString();
			Env.setContext(ctx, "#Approval_Amt", approvalStr);


			//  load Clients
			do
			{
				int AD_Client_ID = rs.getInt(6);
				String Name = rs.getString(7);
				KeyNamePair p = new KeyNamePair(AD_Client_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadClients", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# clients = " + retValue.length);
		return retValue;
	}   //  loadClients

	/**
	 *  Load Organizations.
	 *  <p>
	 *  Sets Client info in context and loads its organization, the role has access to
	 *
	 *  @param ctx context
	 *  @param  client    client information
	 *  @return list of valid Org KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadOrgs (Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadOrgs - required parameter missing");

		s_log.debug("loadOrgs - Client=" + client.toString());

		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadOrgs - Missing Comtext #AD_Role_ID");

		ArrayList list = new ArrayList();
		int AD_Role_ID = Env.getContextAsInt(ctx,"#AD_Role_ID");
		s_log.debug("AD_Role_ID=" + AD_Role_ID);
		//Begin e-evolution vpj-cd 05/09/2003 *************
		int AD_User_ID = Env.getContextAsInt(ctx,"#AD_User_ID");
		s_log.debug("AD_User_ID=" + AD_User_ID);
		//end e-evolution vpj-cd 05/09/2003 *************

		//	get Client details for role
		try
		{
			String sql = "SELECT c.Value,c.SMTPHost,c.IsMultiLingualDocument,c.AD_Language,"	//	1..4
				+ " o.AD_Org_ID,o.Name "				//	5..6
				+ "FROM AD_Client c"
				+ " INNER JOIN AD_Org o ON (o.AD_Client_ID=c.AD_Client_ID) "
				+ "WHERE o.AD_Client_ID=?"  			//  #1
				+ " AND o.IsSummary='N' AND o.IsActive='Y'"
				+ " AND o.AD_Org_ID IN "
				//Begin e-evolution vpj-cd 05/09/2003 *************
				//+   "(SELECT AD_Org_ID FROM AD_Role_OrgAccess WHERE AD_Role_ID=?)"; //  #2
				+ "(SELECT AD_Org_ID FROM AD_Role_OrgAccess WHERE AD_Role_ID=?)"  //  #2
				+ " AND (o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_User WHERE AD_User_ID=?) OR 0 IN (SELECT AD_Org_ID FROM AD_User WHERE AD_User_ID=?))" //  #3 , #4
				//end e-evolution vpj-cd 05/09/2003 *************
				+ " ORDER BY o.Name";

			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, client.getKey());
			pstmt.setInt(2, AD_Role_ID);
			//Begin e-evolution vpj-cd 05/09/2003 *************
			pstmt.setInt(3, AD_User_ID);
			pstmt.setInt(4, AD_User_ID);
			//end e-evolution vpj-cd 05/09/2003 *************
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadOrgs - No Org for Client=" + client.getKey());
				return null;
			}

			//  Client Info
			Env.setContext(ctx, "#AD_Client_ID", client.getKey());
			Env.setContext(ctx, "#AD_Client_Name", client.getName());
			Ini.setProperty(Ini.P_CLIENT, client.getName());
			//
			Env.setContext(ctx, "#Client_Value", rs.getString(1));
			Env.setContext(ctx, "#Client_SMTP", rs.getString(2));
			Env.setContext(ctx, "#IsMultiLingualDocument", rs.getString(3));

			//  load Orgs
			do
			{
				int AD_Org_ID = rs.getInt(5);
				String Name = rs.getString(6);
				KeyNamePair p = new KeyNamePair(AD_Org_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadOrgs", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# orgs = " + retValue.length);
		return retValue;
	}   //  loadOrgs

	/**
	 *	Load Preferences into Context for selected client.
	 *  <p>
	 *  Sets Org info in context and loads relevant field from
	 *	- AD_Client/Info,
	 *  - C_AcctSchema,
	 *  - C_AcctSchema_Elements
	 *	- AD_Preference
	 *  <p>
	 *  Assumes that the context is set for #AD_Client_ID, #AD_User_ID, #AD_Role_ID
	 *
	 *  @returns AD_Message of error (NoValidAcctInfo) or ""
	@param ctx context
	 *  @param  org    org information
	 *  @param  warehouse   optional warehouse information
	 *  @param  timestamp   optional date
	 *  @param  printerName optional printer info
	 *   * @return String
	 */
	public static String loadPreferences (Properties ctx,
		KeyNamePair org, KeyNamePair warehouse, Timestamp timestamp, String printerName)
	{
		s_log.info("loadPreferences - Org=" + org);

		if (ctx == null || org == null)
			throw new IllegalArgumentException("DB.loadPreferences - required parameter missing");
		if (Env.getContext(ctx,"#AD_Client_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Client_ID");
		if (Env.getContext(ctx,"#AD_User_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_User_ID");
		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Role_ID");


		//  Org Info - assumes that it is valid
		Env.setContext(ctx, "#AD_Org_ID", org.getKey());
		Env.setContext(ctx, "#AD_Org_Name", org.getName());
		Ini.setProperty(Ini.P_ORG, org.getName());

		//  Warehouse Info
		if (warehouse != null)
		{
			Env.setContext(ctx, "#M_Warehouse_ID", warehouse.getKey());
			Ini.setProperty(Ini.P_WAREHOUSE, warehouse.getName());
		}

		//	Date (default today)
		long today = System.currentTimeMillis();
		if (timestamp != null)
			today = timestamp.getTime();
		java.sql.Date sd = new java.sql.Date(today);
		Env.setContext(ctx, "#Date", sd.toString());	//	YYYY-MM-DD

		//	Optional Printer
		if (printerName == null)
			printerName = "";
		Env.setContext(ctx, "#Printer", printerName);
		Ini.setProperty(Ini.P_PRINTER, printerName);
		
		//	Load Role Info
		MRole.getDefault(ctx, true);	

		//	Other
		Env.setAutoCommit(ctx, Ini.getPropertyBool(Ini.P_A_COMMIT));
		if (MRole.getDefault(ctx, false).isShowAcct())
			Env.setContext(ctx, "#ShowAcct", Ini.getProperty(Ini.P_SHOW_ACCT));
		else
			Env.setContext(ctx, "#ShowAcct", "N");
		Env.setContext(ctx, "#ShowTrl", Ini.getProperty(Ini.P_SHOW_TRL));

		String retValue = "";
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		int AD_Org_ID =  org.getKey();
		int AD_User_ID =  Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID =  Env.getContextAsInt(ctx, "#AD_Role_ID");

		//	Other Settings
		Env.setContext(ctx, "#YYYY", "Y");
		Env.setContext(ctx, "#StdPrecision", 2);

		//	AccountSchema Info (first)
		String sql = "SELECT * "
			+ "FROM C_AcctSchema a, AD_ClientInfo c "
			+ "WHERE a.C_AcctSchema_ID=c.C_AcctSchema1_ID "
			+ "AND c.AD_Client_ID=?";
		try
		{
			int C_AcctSchema_ID = 0;
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				//  No Warning for System
				if (AD_Role_ID != 0)
					retValue = "NoValidAcctInfo";
			}
			else
			{
				//	Accounting Info
				C_AcctSchema_ID = rs.getInt("C_AcctSchema_ID");
				Env.setContext(ctx, "$C_AcctSchema_ID", C_AcctSchema_ID);
				Env.setContext(ctx, "$C_Currency_ID", rs.getInt("C_Currency_ID"));
				Env.setContext(ctx, "$HasAlias", rs.getString("HasAlias"));
			}
			rs.close();
			pstmt.close();

			//	Accounting Elements
			sql = "SELECT ElementType "
				+ "FROM C_AcctSchema_Element "
				+ "WHERE C_AcctSchema_ID=?"
				+ " AND IsActive='Y'";
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, C_AcctSchema_ID);
			rs = pstmt.executeQuery();
			while (rs.next())
				Env.setContext(ctx, "$Element_" + rs.getString("ElementType"), "Y");
			rs.close();
			pstmt.close();

			//	This reads all relevant window neutral defaults
			//	overwriting superseeded ones.  Window specific is read in Mainain
			sql = "SELECT Attribute, Value, AD_Window_ID "
				+ "FROM AD_Preference "
				+ "WHERE AD_Client_ID IN (0, @#AD_Client_ID@)"
				+ " AND AD_Org_ID IN (0, @#AD_Org_ID@)"
				+ " AND (AD_User_ID IS NULL OR AD_User_ID=0 OR AD_User_ID=@#AD_User_ID@)"
				+ " AND IsActive='Y' "
				+ "ORDER BY Attribute, AD_Client_ID, AD_User_ID DESC, AD_Org_ID";
				//	the last one overwrites - System - Client - User - Org - Window
			sql = Env.parseContext(ctx, 0, sql, false);
			if (sql.length() == 0)
				s_log.error("loadPreferences - Missing Environment");
			else
			{
				pstmt = prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					int AD_Window_ID = rs.getInt(3);
					String at = "";
					if (rs.wasNull())
						at = "P|" + rs.getString(1);
					else
						at = "P" + AD_Window_ID + "|" + rs.getString(1);
					String va = rs.getString(2);
					Env.setContext(ctx, at, va);
				}
				rs.close();
				pstmt.close();
			}
		}
		catch (SQLException ex)
		{
			s_log.error("loadPreferences (" + sql + ")", ex);
		}

		//	Default Values
		s_log.info("Default Values ...");
		sql = "SELECT t.TableName, c.ColumnName "
			+ "FROM AD_Column c "
			+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID) "
			+ "WHERE c.IsKey='Y' AND t.IsActive='Y'"
			+ " AND EXISTS (SELECT * FROM AD_Column cc "
			+ " WHERE ColumnName = 'IsDefault' AND t.AD_Table_ID=cc.AD_Table_ID AND cc.IsActive='Y')";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				loadDefault (ctx, rs.getString(1), rs.getString(2));
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("loadPreferences", e);
		}
		//
		Ini.saveProperties(Ini.isClient());
		//
		return retValue;
	}	//	loadPreferences

	/**
	 *	Load Default Value for Table into Context.
	 *
	 *  @param ctx context
	 *  @param TableName table name
	 *  @param ColumnName column name
	 */
	private static void loadDefault (Properties ctx, String TableName, String ColumnName)
	{
		String value = null;
		//
		String sql = "SELECT " + ColumnName + " FROM " + TableName	//	most specific first
			+ " WHERE IsDefault='Y' AND IsActive='Y' ORDER BY AD_Client_ID DESC, AD_Org_ID DESC";
		sql = MRole.getDefault(ctx, false).addAccessSQL(sql, 
			TableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				value = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("loadDefault - " + TableName + " (" + sql + ")", e);
			return;
		}
		//	Set Context Value
		if (value != null && value.length() != 0)
		{
			if (TableName.equals("C_DocType"))
				Env.setContext(ctx, "#C_DocTypeTarget_ID", value);
			else
				Env.setContext(ctx, "#" + ColumnName, value);
		}
	}	//	loadDefault

	/**
	 *  Load Warehouses
	 *
	 * @param ctx context
	 * @param client client
	 * @return Array of Warehouse Info
	 */
	public static KeyNamePair[] loadWarehouses(Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadWarehouses - required parameter missing");

		s_log.info("loadWarehouses - Client=" + client.toString());
		//Begin e-evolution vpj-cd 05/09/2003 *************
		int AD_User_ID = Env.getContextAsInt(ctx,"#AD_User_ID");
		s_log.debug("AD_User_ID=" + AD_User_ID);
		//end e-evolution vpj-cd 05/09/2003 *************

		ArrayList list = new ArrayList();
		try
		{
			String sql = "SELECT M_Warehouse_ID, Name FROM M_Warehouse "
				//Begin e-evolution vpj-cd 05/09/2003 *************
				//	+ "WHERE AD_Client_ID=? AND IsActive='Y'";  //  #1
				+ "WHERE AD_Client_ID=? AND IsActive='Y' "
				+ " AND (AD_Org_ID IN (SELECT AD_Org_ID FROM AD_User WHERE AD_User_ID=?) OR 0 IN (SELECT AD_Org_ID FROM AD_User WHERE AD_User_ID=?))" //#2,#3
				//end e-evolution vpj-cd 05/09/2003 *************
				+ " ORDER BY Name";
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, client.getKey());
			//Begin e-evolution vpj-cd 05/09/2003 *************
			pstmt.setInt(2, AD_User_ID);
			pstmt.setInt(3, AD_User_ID);
			//end e-evolution vpj-cd 05/09/2003 *************
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.warn("loadWarehouses - No Warehouses for Client=" + client.getKey());
				return null;
			}

			//  load Warehousess
			do
			{
				int AD_Warehouse_ID = rs.getInt(1);
				String Name = rs.getString(2);
				KeyNamePair p = new KeyNamePair(AD_Warehouse_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadWarehouses", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.info("# warehouses = " + retValue.length);
		return retValue;
	}   //  loadWarehouses

	/**************************************************************************

	/**
	 *  Set connection
	 *  @param cc connection
	 */
	public static void setDBTarget (CConnection cc)
	{
		if (cc == null)
			throw new IllegalArgumentException("DB.setDBTarget connection is NULL");

		if (s_cc == null)
			s_cc = cc;
		synchronized (s_cc)    //  use as mutex
		{
			s_cc = cc;
			//  Closing existing
			if (s_connections != null)
			{
				for (int i = 0; i < s_connections.length; i++)
				{
					try {
						s_connections[i].close();
					} catch (Exception e) {}
				}
			}
			s_connections = null;
		}
	}   //  setDBTarget

	/**
	 *  Is there a connection to the database ?
	 *  @return true, if connected to database
	 */
	public static boolean isConnected()
	{
		try
		{
			getConnectionRW();	//	try to get a connection
			return true;
		}
		catch (Exception e)
		{
		}
		return false;
	}   //  isConnected

	/**
	 *	Return (pooled) r/w AutoCommit, read committed connection.
	 *  @return Connection (r/w)
	 */
	public static Connection getConnectionRW ()
	{
		//	check health of connection
		try
		{
			if (s_connectionRW == null)
				;
			else if (s_connectionRW.isClosed())
			{
				System.out.println("DB.getConnectionRW - closed");
				s_connectionRW = null;
			}
			else if (s_connectionRW instanceof OracleConnection && ((OracleConnection)s_connectionRW).pingDatabase(1) < 0)
			{
				System.out.println("DB.getConnectionRW - no ping");
				s_connectionRW = null;
			}
			else
				 s_connectionRW.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		}
		catch (Exception e)
		{
			s_connectionRW = null;
		}
		//	Get new
		if (s_connectionRW == null)
		{
			s_connectionRW = s_cc.getConnection (true, Connection.TRANSACTION_SERIALIZABLE);
		}
		if (s_connectionRW == null)
			throw new UnsupportedOperationException("DB.getConnectionRW - @NoDBConnection@");
		return s_connectionRW;
	}   //  getConnectionRW

	/**
	 *	Return read committed, read/only from pool.
	 *  @return Connection (r/o)
	 */
	public static Connection getConnectionRO ()
	{
		try
		{
			synchronized (s_cc)    //  use as mutex as s_connection is null the first time
			{
				if (s_connections == null)
					s_connections = createConnections (Connection.TRANSACTION_READ_COMMITTED);     //  see below
			}
		}
		catch (Exception e)
		{
			s_log.error("getConnectionRO", e);
		}

		//  check health of connection
		int pos = s_conCount++;
		int connectionNo = pos % s_conCacheSize;
		Connection connection = s_connections[connectionNo];
		try
		{
			if (connection == null)
				;
			else if (connection.isClosed())
			{
			//	RowSet.close also closes connection!
			//	System.out.println("DB.getConnectionRO - closed #" + connectionNo);
				connection = null;
			}
			else if (connection instanceof OracleConnection && ((OracleConnection)connection).pingDatabase(1) < 0)
			{
				System.out.println("DB.getConnectionRO - no ping #" + connectionNo);
				connection = null;
			}
			else
				 connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		catch (Exception e)
		{
			System.out.println("DB.getConnectionRO #" + connectionNo + " - " + e.toString());
			connection = null;
		}
		//	Get new
		if (connection == null)
		{
			if (Log.isTraceLevel(8))
				s_log.debug("getConnectionRO - replacing connection #" + connectionNo);
			connection = s_cc.getConnection (true, Connection.TRANSACTION_READ_COMMITTED);            //  see above
		/*	try
			{
				retValue.setReadOnly(true);     //  not supported by Oracle
			}
			catch (Exception e)
			{
				System.err.println("DB.getConnectionRO - Cannot set to R/O - " + e);
			}   */
			s_connections[connectionNo] = connection;
		}
		if (connection == null)
			throw new UnsupportedOperationException("DB.getConnectionRO - @NoDBConnection@");
	//	System.out.println("DB.getConnectionRO - #" + connectionNo);
		return connection;
	}	//	getConnectionRO

	/**
	 *	Create new Connection.
	 *  The connection must be closed explicitly by the application
	 *
	 *  @param autoCommit auto commit
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Connection connection
	 */
	public static Connection createConnection (boolean autoCommit, int trxLevel)
	{
		s_log.debug("createConnection " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", AutoCommit=" + autoCommit + ", TrxLevel=" + trxLevel);
		return s_cc.getConnection (autoCommit, trxLevel);
	}	//	createConnection

	/**
	 *	Create new set of r/o Connections.
	 *  R/O connection might not be supported by DB
	 *
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Array of Connections (size based on s_conCacheSize)
	 */
	private static Connection[] createConnections (int trxLevel)
	{
		s_log.debug("createConnections (" + s_conCacheSize + ") " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", TrxLevel=" + trxLevel);
		Connection cons[] = new Connection[s_conCacheSize];
		try
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				cons[i] = s_cc.getConnection (true, trxLevel);  //  auto commit
				if (cons[i] == null)
					System.err.println("createConnections - connection is NULL");	//	don't use log
			}
		}
		catch (Exception e)
		{
			//  Don't use Log
			System.err.println("DB.createConnections - " + e.getMessage());
		}
		return cons;
	}	//	createConnections

	/**
	 *  Get Database Driver.
	 *  Access to database specific functionality.
	 *  @return Compiere Database Driver
	 */
	public static CompiereDatabase getDatabase()
	{
		if (s_cc != null)
			return s_cc.getDatabase();
		return null;
	}   //  getDatabase

	/**************************************************************************

	/**
	 *  Check database Version with Code version
	 *  @param ctx context
	 *  @return true if Database version (date) is the same
	 */
	public static boolean isDatabaseOK (Properties ctx)
	{
		//  Check Version
		String version = "?";
		String sql = "SELECT Version FROM AD_System";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				version = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("Problem with AD_System Table - Run system.sql script - " + e.toString());
			return false;
		}
		s_log.info("isDatabaseOK - DB_Version=" + version);
		//  Identical DB version
		if (Compiere.DB_VERSION.equals(version))
			return true;

		String AD_Message = "DatabaseVersionError";
		String title = org.compiere.Compiere.getName() + " " +  Msg.getMsg(ctx, AD_Message, true);
		//	Code assumes Database version {0}, but Database has Version {1}.
		String msg = Msg.getMsg(ctx, AD_Message);	//	complete message
		msg = MessageFormat.format(msg, new Object[] {Compiere.DB_VERSION, version});
		Object[] options = { UIManager.get("OptionPane.noButtonText"), "Migrate" };
		int no = JOptionPane.showOptionDialog (null, msg,
			title, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
			UIManager.getIcon("OptionPane.errorIcon"), options, options[0]);
		if (no == 1)
		{
			try
			{
				Class.forName("com.compiere.client.StartMaintain").newInstance();
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog (null,
					ex.getMessage() + "\nSee: http://www.compiere.com/maintain",
					title, JOptionPane.ERROR_MESSAGE);
				s_log.error("isDatabaseOK - " + ex.getMessage());
			}
		}
		return false;
	}   //  isDatabaseOK

	/**************************************************************************

	/**
	 *	Secure Context Login.
	 *  @param uid uid
	 *  @param pwd pwd
	 *  @param role role
	 *  @return true if connected
	 */
	public static boolean login_context (String uid, String pwd, String role)
	{
		if (uid == null || pwd == null || role == null || uid.length() == 0 || pwd.length() == 0 || role.length() == 0)
			throw new IllegalArgumentException("DB.login_context - required parameter missing");
		s_log.info("login_context uid=" + uid);
		if (uid == null || pwd == null || role == null)
			return false;
		//
		String SQL = "{CALL Compiere_Context.Login(?,?,?,?)}";
		boolean result = true;
		try
		{
			CallableStatement cstmt = getConnectionRO().prepareCall(SQL);
			cstmt.setString(1, uid);
			cstmt.setString(2, pwd);
			cstmt.setString(3, role);
			cstmt.setString(4, Language.getBaseAD_Language());
			result = cstmt.execute();
			cstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("login_context", e);
			result = false;
		}
		return result;
	}	//	login_context

	/**************************************************************************

	/**
	 *	Close Target
	 */
	public static void closeTarget()
	{
		if (s_connections != null)
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				try
				{
					if (s_connections[i] != null)
						s_connections[i].close();
				}
				catch (SQLException e)
				{
					s_log.warn("close connection #" + i + " - " + e.getMessage());
				}
				s_connections[i] = null;
			}
			try
			{
				if (s_connectionRW != null)
					s_connectionRW.close();
			}
			catch (SQLException e)
			{
				s_log.error("close R/W connection", e);
			}
			s_connectionRW = null;
		}
		s_connections = null;
		//
//		s_cc.getDataSource();
//		EJB.close();
	}	//	closeTarget

	/**************************************************************************

	/**
	 *	Prepare Forward Read Only Call
	 *  @param RO_SQL sql (RO)
	 *  @return Callable Statement
	 */
	public static CallableStatement prepareCall(String RO_SQL)
	{
		if (RO_SQL == null || RO_SQL.length() == 0)
			throw new IllegalArgumentException("DB.prepareCall - required parameter missing - " + RO_SQL);
		//
		String sql = getDatabase().convertStatement(RO_SQL);
		try
		{
			return getConnectionRO().prepareCall
				(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		}
		catch (SQLException e)
		{
			s_log.error("prepareCall (" + sql + ")", e);
		}
		return null;
	}	//	prepareCall

	/**************************************************************************

	/**
	 *	Prepare Read Only Statement
	 *  @param RO_SQL sql (RO)
	 *  @return Prepared Statement
	 */
	public static CPreparedStatement prepareStatement(String RO_SQL)
	{
		return prepareStatement(RO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, null);
	}	//	prepareStatement

	/**
	 *	Prepare Statement.
	 *  @param SQL sql statement
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Prepared Statement r/o or r/w depending on concur
	 */
	public static CPreparedStatement prepareStatement(String sql, 
		int resultSetType, int resultSetConcurrency)
	{
		return prepareStatement(sql, resultSetType, resultSetConcurrency, null);
	}	//	prepareStatement

	/**
	 *	Prepare Statement.
	 *  @param sql sql statement
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 * 	@param trxName transaction name
	 *  @return Prepared Statement r/o or r/w depending on concur
	 */
	public static CPreparedStatement prepareStatement(String sql, 
		int resultSetType, int resultSetConcurrency, String trxName)
	{
		if (sql == null || sql.length() == 0)
			throw new IllegalArgumentException("DB.prepareStatement - No SQL");
		//
		return new CPreparedStatement(resultSetType, resultSetConcurrency, sql, trxName);
		/**
		try
		{
			Connection conn = null;
			if (resultSetConcurrency == ResultSet.CONCUR_UPDATABLE)
				conn = getConnectionRW();
			else
				conn = getConnectionRO();
			return conn.prepareStatement (sql, resultSetType, resultSetConcurrency);
		}
		catch (SQLException e)
		{
			s_log.error("prepareStatement (" + sql + ")", e);
		}
		return null;
		**/
	}	//	prepareStatement

	/**
	 *	Create Read Only Statement
	 *  @return Statement
	 */
	public static Statement createStatement()
	{
		return createStatement (ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, null);
	}	//	createStatement

	/**
	 *	Create Statement.
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 * 	@param trxName transaction name
	 *  @return Statement - either r/w ir r/o depending on concur
	 */
	public static Statement createStatement(int resultSetType, int resultSetConcurrency, String trxName)
	{
		return new CStatement(resultSetType, resultSetConcurrency, trxName);
		/**
		try
		{
			Connection conn = null;
			if (concur == ResultSet.CONCUR_UPDATABLE)
				conn = getConnectionRW();
			else
				conn = getConnectionRO();
			return conn.createStatement(type, concur);
		}
		catch (SQLException e)
		{
			s_log.error("createStatement", e);
		}
		return null;
		**/
	}	//	createStatement

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql)
	{
		return executeUpdate(sql, false, null);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 * 	@param trxName optional transaction name
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql, String trxName)
	{
		return executeUpdate(sql, false, trxName);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 * 	@param ignoreError if true, no execution error is reported
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql, boolean ignoreError)
	{
		return executeUpdate (sql, ignoreError, null);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 * 	@param ignoreError if true, no execution error is reported
	 * 	@param trxName optional transaction name
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql, boolean ignoreError, String trxName)
	{
		if (sql == null || sql.length() == 0)
			throw new IllegalArgumentException("DB.executeUpdate - required parameter missing - " + sql);
		//
		int no = -1;
		CPreparedStatement cs = new CPreparedStatement(ResultSet.TYPE_FORWARD_ONLY, 
			ResultSet.CONCUR_UPDATABLE, sql, trxName);
		
		try
		{
			no = cs.executeUpdate();
			//	No Transaction - Commit
			if (trxName == null)
			{
				cs.commit();	//	Local commit
			//	Connection conn = cs.getConnection();
			//	if (conn != null && !conn.getAutoCommit())	//	is null for remote
			//		conn.commit();
			}
		}
		catch (SQLException e)
		{
			if (!ignoreError)
			{
				s_log.error ("executeUpdate - " + cs.getSql(), e);
				Log.saveError ("DBExecuteError", e);
			}
		}
		finally
		{
			//  Always close cursor
			try
			{
				cs.close();
			}
			catch (SQLException e2)
			{
				s_log.error("executeUpdate - cannot close statement");
			}
		}
		return no;
	}	//	executeUpdate

	/**
	 *	Execute Update and throw exxeption.
	 *  @param SQL sql
	 *  @return number of rows updated or -1 if error
	 * 	@throws SQLException
	 */
	public static int executeUpdateEx (String SQL) throws SQLException
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.executeUpdate - required parameter missing - " + SQL);
		//
		String sql = getDatabase().convertStatement(SQL);
		int no = -1;
		SQLException ex = null;
		Statement stmt = null;
		try
		{
			Connection conn = getConnectionRW();
			stmt = conn.createStatement();
			no = stmt.executeUpdate(sql);
		}
		catch (SQLException e)
		{
			ex = e;
		}
		finally
		{
			//  Always close cursor
			try
			{
				stmt.close();
			}
			catch (SQLException e2)
			{
				s_log.error("executeUpdate - cannot close statement");
			}
		}
		if (ex != null)
			throw new SQLException(ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
		return no;
	}	//	execute Update

	/**
	 *	Commit - commit on RW connection.
	 *  Is not required as RW connection is AutoCommit
	 *  @param throwException if true, re-throws exception
	 *  @return true if not needed or success
	 *  @throws SQLException
	 */
	public static boolean commit (boolean throwException) throws SQLException
	{
		try
		{
			Connection conn = getConnectionRW();
		//	if (!conn.getAutoCommit())
				conn.commit();
		}
		catch (SQLException e)
		{
			s_log.error("commit", e);
			if (throwException)
				throw e;
			return false;
		}
		return true;
	}	//	commit


	/**
	 * 	Get Row Set.
	 * 	When a Rowset is closed, it also closes the underlying connection.
	 * 	If the created RowSet is transfered by RMI, closing it makes no difference 
	 *	@param sql sql
	 *	@param local local RowSet (own connection)
	 *	@return row set or null
	 */
	public static RowSet getRowSet (String sql, boolean local)
	{
		RowSet retValue = null;
		CStatementVO info = new CStatementVO ( 
			RowSet.TYPE_SCROLL_INSENSITIVE, RowSet.CONCUR_READ_ONLY, sql);
		CPreparedStatement stmt = new CPreparedStatement(info);
		if (local)
		{
			retValue = stmt.local_getRowSet();
		}
		else
		{
			retValue = stmt.remote_getRowSet();
		}
		return retValue;
	}	//	getRowSet

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else if (Log.isTraceLevel(6))
				s_log.warn("getSQLValue - No Value " + sql);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int int_param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, String int_param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setString(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@param s_param2 parameter 2
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int int_param1, String s_param2)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			pstmt.setString(2, s_param2);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value: " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get String Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or null
	 */
	public static String getSQLValueString (String sql, int int_param1)
	{
		String retValue = null;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getString(1);
			else
				s_log.warn("getSQLValueString - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValueString - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValueString

	
	/**
	 * 	Get Array of Key Name Pairs
	 *	@param sql select with id / name as first / second column
	 *	@param optional if true (-1,"") is added 
	 *	@return array of key name pairs
	 */
	public static KeyNamePair[] getKeyNamePairs(String sql, boolean optional)
	{
		PreparedStatement pstmt = null;
		ArrayList list = new ArrayList();
		if (optional)
			list.add (new KeyNamePair(-1, ""));
		try
		{
			pstmt = DB.prepareCall(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new KeyNamePair(rs.getInt(1), rs.getString(2)));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getKeyNamePairs " + sql, e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
	//	s_log.debug("getKeyNamePairs #" + retValue.length);
		return retValue;		
	}	//	getKeyNamePairs


	/**
	 *  Get fully qualified Account Combination
	 *
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param C_AcctSchema_ID acct schema
	 *  @param Account_ID natural account
	 *  @param  base_ValidCombination_ID optional base combination to be specified
	 *  @param Alias aloas
	 *  @param AD_User_ID user
	 *  @param M_Product_ID product
	 *  @param C_BPartner_ID partner
	 *  @param AD_OrgTrx_ID trx org
	 *  @param C_LocFrom_ID loc from
	 *  @param C_LocTo_ID loc to
	 *  @param C_SRegion_ID sales region
	 *  @param C_Project_ID project
	 *  @param C_Campaign_ID campaign
	 *  @param C_Activity_ID activity
	 *  @param User1_ID user1
	 *  @param User2_ID user2
	 *  @return C_ValidCombination_ID of existing or new Combination
	 */
	public static int getValidCombination (int AD_Client_ID, int AD_Org_ID,
		int C_AcctSchema_ID, int Account_ID, int base_ValidCombination_ID, String Alias, int AD_User_ID,
		int M_Product_ID, int C_BPartner_ID, int AD_OrgTrx_ID,
		int C_LocFrom_ID, int C_LocTo_ID, int C_SRegion_ID, int C_Project_ID,
		int C_Campaign_ID, int C_Activity_ID, int User1_ID, int User2_ID)
	{
		int retValue = 0;
		StringBuffer sb = new StringBuffer ("C_ValidCombination_Get[");
		try
		{
			String sql = "{CALL C_ValidCombination_Get(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)}";
			CallableStatement cstmt = prepareCall(sql);
			//  1 - ID
			cstmt.registerOutParameter(1, Types.NUMERIC);
			sb.append("v,");
			//  --  Mandatory Accounting fields
			//	2 - AD_Client_ID
			cstmt.setInt(2, AD_Client_ID);
			sb.append("AD_Client_ID=").append(AD_Client_ID).append(",");
			//	3 - AD_Org_ID
			cstmt.setInt(3, AD_Org_ID);
			sb.append("AD_Org_ID=").append(AD_Org_ID).append(",");
			//  4- C_AcctSchema_ID
			cstmt.setInt(4, C_AcctSchema_ID);
			sb.append("C_AcctSchema_ID=").append(C_AcctSchema_ID).append(",");
			//  5 - Account_ID
			cstmt.setInt(5, Account_ID);
			sb.append("Account_ID=").append(Account_ID).append(", ");

			//  --  Optional
			//  6 - Base C_ValidCombination_ID
			if (base_ValidCombination_ID != 0)
			{
				cstmt.setInt (6, base_ValidCombination_ID);
				sb.append("BaseValidCombination_ID=").append(base_ValidCombination_ID).append(",");
			}
			else
			{
				cstmt.setNull(6, Types.NUMERIC);
				sb.append("BaseValidCombination_ID=").append("NULL,");
			}
			//  7 - MustBeFullyQualified
			cstmt.setString(7, "Y");
			sb.append("MustBeFullyQualified='Y',");
			//  8 - Alias
			if (Alias != null && Alias.length() > 0)
			{
				cstmt.setString(8, Alias);
				sb.append("Alias='").append(Alias).append("',");
			}
			else
			{
				cstmt.setNull(8, Types.VARCHAR);
				sb.append("Alias=NULL,");
			}
			//  9 - CreatedBy
			cstmt.setInt(9, AD_User_ID);
			sb.append("AD_User_ID=").append(AD_User_ID).append(", ");

			//	--  Optional Accounting fields
			//  10 - M_Product_ID
			if (M_Product_ID != 0)
			{
				cstmt.setInt(10, M_Product_ID);
				sb.append("M_Product_ID=").append(M_Product_ID).append(",");
			}
			else
			{
				cstmt.setNull(10, Types.NUMERIC);
				sb.append("M_Product_ID=NULL,");
			}
			//  11 - C_BPartner_ID
			if (C_BPartner_ID != 0)
			{
				cstmt.setInt(11, C_BPartner_ID);
				sb.append("C_BPartner_ID=").append(C_BPartner_ID).append(",");
			}
			else
			{
				cstmt.setNull(11, Types.NUMERIC);
				sb.append("C_BPartner_ID=NULL,");
			}
			//  12 - AD_OrgTrx_ID
			if (AD_OrgTrx_ID != 0)
			{
				cstmt.setInt(12, AD_OrgTrx_ID);
				sb.append("AD_OrgTrx_ID=").append(AD_OrgTrx_ID).append(",");
			}
			else
			{
				cstmt.setNull(12, Types.NUMERIC);
				sb.append("AD_OrgTrx_ID=NULL,");
			}
			//  13 - C_LocFrom_ID
			if (C_LocFrom_ID != 0)
			{
				cstmt.setInt(13, C_LocFrom_ID);
				sb.append("C_LocFrom_ID=").append(C_LocFrom_ID).append(",");
			}
			else
			{
				cstmt.setNull(13, Types.NUMERIC);
				sb.append("C_LocFrom=NULL,");
			}
			//  14 - C_LocTo_ID
			if (C_LocTo_ID != 0)
			{
				cstmt.setInt(14, (C_LocTo_ID));
				sb.append("C_LocTo_ID=").append(C_LocTo_ID).append(", ");
			}
			else
			{
				cstmt.setNull(14, Types.NUMERIC);
				sb.append("C_LocTo_ID=NULL, ");
			}
			//  15 - C_SalesRegion_ID
			if (C_SRegion_ID != 0)
			{
				cstmt.setInt(15, (C_SRegion_ID));
				sb.append("C_SalesRegion_ID=").append(C_SRegion_ID).append(",");
			}
			else
			{
				cstmt.setNull(15, Types.NUMERIC);
				sb.append("C_SalesRegion_ID=NULL,");
			}
			//  16 - C_Project_ID
			if (C_Project_ID != 0)
			{
				cstmt.setInt(16, (C_Project_ID));
				sb.append("C_Project_ID=").append(C_Project_ID).append(",");
			}
			else
			{
				cstmt.setNull(16, Types.NUMERIC);
				sb.append("C_Project_ID=NULL,");
			}
			//  17 - C_Campaign_ID
			if (C_Campaign_ID != 0)
			{
				cstmt.setInt(17, (C_Campaign_ID));
				sb.append("C_Campaign_ID=").append(C_Campaign_ID).append(",");
			}
			else
			{
				cstmt.setNull(17, Types.NUMERIC);
				sb.append("C_Campaign_ID=NULL,");
			}
			//  18 - C_Activity_ID
			if (C_Activity_ID != 0)
			{
				cstmt.setInt(18, (C_Activity_ID));
				sb.append("C_Activity_ID=").append(C_Activity_ID).append(",");
			}
			else
			{
				cstmt.setNull(18, Types.NUMERIC);
				sb.append("C_Activity_ID=NULL,");
			}
			//  19 - User1_ID
			if (User1_ID != 0)
			{
				cstmt.setInt(19, (User1_ID));
				sb.append("User1_ID=").append(User1_ID).append(",");
			}
			else
			{
				cstmt.setNull(19, Types.NUMERIC);
				sb.append("User1_ID=NULL,");
			}
			//  20 - User2_ID
			if (User2_ID != 0)
			{
				cstmt.setInt(20, (User2_ID));
				sb.append("User2_ID=").append(User2_ID).append(")");
			}
			else
			{
				cstmt.setNull(20, Types.NUMERIC);
				sb.append("User2_ID=NULL)");
			}

			//
			cstmt.executeUpdate();
			retValue = cstmt.getInt(1);     //  1 - ID
			cstmt.close();
			s_log.debug("getValidCombination " + sb.toString());
		}
		catch(SQLException e)
		{
			s_log.error("getValidCombination " + sb.toString(), e);
		}
		return retValue;
	}   //  getValidCombination
	
	
	/**************************************************************************
	 *	Get next number for Key column = 0 is Error.
	 *   * @param ctx client
	@param TableName table name
	 * 	@param trxName optionl transaction name
	 *  @return next no
	 */
	public static int getNextID (Properties ctx, String TableName, String trxName)
	{
		if (ctx == null)
			throw new IllegalArgumentException("DB.getNextID - Context missing");
		if (TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getNextID - TableName missing");
		return getNextID(Env.getAD_Client_ID(ctx), TableName, trxName);
	}	//	getNextID

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param AD_Client_ID client
	 *  @param TableName table name
	 * 	@param trxName optional Transaction Name
	 *  @return next no
	 */
	public static int getNextID (int AD_Client_ID, String TableName, String trxName)
	{
		if (isClientRemote())
		{
			Server server = CConnection.get().getServer();
			try
			{
				if (server != null)
				{
					int id = server.getNextID(AD_Client_ID, TableName, null);
					if (Log.isTraceLevel(10))
						s_log.debug("getNextID - server => " + id);
					return id;
				}
				s_log.error("getNextID - AppsServer not found - " + TableName + ", Remote=" + DB.isClientRemote());
			}
			catch (RemoteException ex)
			{
				s_log.error("getNextID - AppsServer error", ex);
			}
			//	Try locally
		}
		return MSequence.getNextID (AD_Client_ID, TableName, trxName);
	}	//	getNextID
	
	/**
	 * 	Get Document No based on Document Type
	 *	@param C_DocType_ID document type
	 * 	@param trxName optional Transaction Name
	 *	@return document no or null
	 */
	public static String getDocumentNo(int C_DocType_ID, String trxName)
	{
		if (isClientRemote())
		{
			Server server = CConnection.get().getServer();
			try
			{
				if (server != null)
				{
					String no = server.getDocumentNo (C_DocType_ID, trxName);
					if (Log.isTraceLevel(10))
						s_log.debug("getNextID - server => " + no);
					return no;
				}
				s_log.error("getDocumentNo - AppsServer not found - " + C_DocType_ID + ", Remote=" + DB.isClientRemote());
			}
			catch (RemoteException ex)
			{
				s_log.error("getDocumentNo - AppsServer error", ex);
			}
		}
		return MSequence.getDocumentNo (C_DocType_ID, trxName);
	}	//	getDocumentNo


	/**
	 * 	Get Document No from table
	 *	@param AD_Client_ID client
	 *	@param TableName table name
	 * 	@param trxName optional Transaction Name
	 *	@return document no or null
	 */
	public static String getDocumentNo(int AD_Client_ID, String TableName, String trxName)
	{
		if (isClientRemote())
		{
			Server server = CConnection.get().getServer();
			try
			{
				if (server != null)
				{
					String no = server.getDocumentNo (AD_Client_ID, TableName, trxName);
					if (Log.isTraceLevel(10))
						s_log.debug("getNextID - server => " + no);
					return no;
				}
				s_log.error("getDocumentNo - AppsServer not found - " + TableName + ", Remote=" + DB.isClientRemote());
			}
			catch (RemoteException ex)
			{
				s_log.error("getDocumentNo - AppsServer error", ex);
			}
		}
		return MSequence.getDocumentNo (AD_Client_ID, TableName, trxName);
	}	//	getDocumentNo

	/**
	 *	Get Document Number for current document.
	 *  <br>
	 *  - first search for DocType based Document No
	 *  - then Search for DocumentNo based on TableName
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @param onlyDocType Do not search for document no based on TableName
	 * 	@param trxName optional Transaction Name
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (Properties ctx, int WindowNo, 
		String TableName, boolean onlyDocType, String trxName)
	{
		if (ctx == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("getDocumentNo - required parameter missing");
		int AD_Client_ID = Env.getContextAsInt(ctx, WindowNo, "AD_Client_ID");

		//	Get C_DocType_ID from context - NO Defaults -
		int C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID");
		if (C_DocType_ID == 0)
			C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID");
		if (C_DocType_ID == 0)
		{
			s_log.debug("getDocumentNo - for Window=" + WindowNo
				+ " - Target=" + Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID") + "/" + Env.getContextAsInt(ctx, WindowNo, "C_DocTypeTarget_ID")
				+ " - Actual=" + Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID") + "/" + Env.getContextAsInt(ctx, WindowNo, "C_DocType_ID"));
			return getDocumentNo (AD_Client_ID, TableName, trxName);
		}

		String retValue = getDocumentNo (C_DocType_ID, trxName);
		if (!onlyDocType && retValue == null)
			return getDocumentNo (AD_Client_ID, TableName, trxName);
		return retValue;
	}	//	getDocumentNo

	/**
	 * 	Is this is remote client connection
	 *	@return true if client and RMI or Objects on Server
	 */
	public static boolean isClientRemote()
	{
		return Ini.isClient() 
			&& (CConnection.get().isRMIoverHTTP() || Ini.isClientObjects())
			&& CConnection.get().isAppsServerOK(false);
	}	//	isClientRemote
	
	
	
	/**************************************************************************

	/**
	 *	Print SQL Warnings.
	 *  <br>
	 *		Usage: DB.printWarning(rs.getWarnings(), "xx");
	 *  @param comment comment
	 *  @param warning warning
	 */
	public static void printWarning(String comment, SQLWarning warning)
	{
		if (comment == null || warning == null || comment.length() == 0)
			throw new IllegalArgumentException("DB.printException - required parameter missing");
		s_log.warn("SQL Warning: " + comment);
		if (warning == null)
			return;
		//
		SQLWarning warn = warning;
		while (warn != null)
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append(warn.getMessage());
			buffer.append("; State=").append(warn.getSQLState()).append("; ErrorCode=").append(warn.getErrorCode());
			s_log.warn(buffer.toString());
			warn = warn.getNextWarning();
		}
	}	//	printWarning

	/**
	 *  Create SQL TO Date String from Timestamp
	 *
	 *  @param  time Date to be converted
	 *  @param  dayOnly true if time set to 00:00:00
	 *
	 *  @return TO_DATE('2001-01-30 18:10:20',''YYYY-MM-DD HH24:MI:SS')
	 *      or  TO_DATE('2001-01-30',''YYYY-MM-DD')
	 */
	public static String TO_DATE (Timestamp time, boolean dayOnly)
	{
		if (time == null)
		{
			if (dayOnly)
				return "TRUNC(SysDate)";
			return "SysDate";
		}

		StringBuffer dateString = new StringBuffer("TO_DATE('");
		//  YYYY-MM-DD HH24:MI:SS.mmmm  JDBC Timestamp format
		String myDate = time.toString();
		if (dayOnly)
		{
			dateString.append(myDate.substring(0,10));
			dateString.append("','YYYY-MM-DD')");
		}
		else
		{
			dateString.append(myDate.substring(0, myDate.indexOf(".")));	//	cut off miliseconds
			dateString.append("','YYYY-MM-DD HH24:MI:SS')");
		}
		return dateString.toString();
	}   //  TO_DATE

	/**
	 *  Create SQL TO Date String from Timestamp
	 *  @param day day time
	 *  @return TO_DATE String
	 */
	public static String TO_DATE (Timestamp day)
	{
		return TO_DATE(day, true);
	}   //  TO_DATE

	/**
	 *  Create SQL for formatted Date, Number
	 *
	 *  @param  columnName  the column name in the SQL
	 *  @param  displayType Display Type
	 *  @param  AD_Language 6 character language setting (from Env.LANG_*)
	 *
	 *  @return TRIM(TO_CHAR(columnName,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.'''))
	 *      or TRIM(TO_CHAR(columnName,'TM9')) depending on DisplayType and Language
	@see org.compiere.util.DisplayType
	 *  @see org.compiere.util.Env
	 *
	 *   */
	public static String TO_CHAR (String columnName, int displayType, String AD_Language)
	{
		if (columnName == null || AD_Language == null || columnName.length() == 0)
			throw new IllegalArgumentException("DB.TO_CHAR - required parameter missing");

		StringBuffer retValue = new StringBuffer("TRIM(TO_CHAR(");
		retValue.append(columnName);

		//  Numbers
		if (DisplayType.isNumeric(displayType))
		{
			if (displayType == DisplayType.Amount)
				retValue.append(",'9G999G990D00'");
			else
				retValue.append(",'TM9'");
			//  TO_CHAR(GrandTotal,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.''')
			if (!Language.isDecimalPoint(AD_Language))      //  reversed
				retValue.append(",'NLS_NUMERIC_CHARACTERS='',.'''");
		}
		else if (DisplayType.isDate(displayType))
		{
			retValue.append(",'")
				.append(Language.getLanguage(AD_Language).getDBdatePattern())
				.append("'");
		}

		retValue.append("))");
		//
		return retValue.toString();
	}   //  TO_CHAR_Number


	/**
	 *  Package Strings for SQL command
	 *  @param txt  String with text
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt)
	{
		return TO_STRING (txt, 0);
	}   //  TO_STRING

	/**
	 *	Package Strings for SQL command.
	 *  <pre>
	 *		-	include in ' (single quotes)
	 *		-	replace ' with ''
	 *  </pre>
	 *  @param txt  String with text
	 *  @param maxLength    Maximum Length of content or 0 to ignore
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt, int maxLength)
	{
		if (txt == null)
			return "NULL";

		//  Length
		String text = txt;
		if (maxLength != 0 && text.length() > maxLength)
			text = txt.substring(0, maxLength);

		char quote = '\'';
		//  copy characters		(wee need to look through anyway)
		StringBuffer out = new StringBuffer();
		out.append(quote);		//	'
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (c == quote)
				out.append("''");
			else
				out.append(c);
		}
		out.append(quote);		//	'
		//
		return out.toString();
	}	//	TO_STRING

}	//	DB

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
package org.compiere.util;

import java.math.*;
import java.util.*;
import java.text.*;
import java.security.*;
import javax.swing.*;

import java.sql.*;
import javax.sql.*;
import oracle.jdbc.*;

import org.apache.log4j.Logger;

import org.compiere.Compiere;
import org.compiere.util.*;
import org.compiere.db.*;

/**
 *  General Database Interface
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB.java,v 1.4 2003/02/21 06:38:48 jjanke Exp $
 */
public final class DB
{
	/** Connection Descriptor           */
	private static CConnection      s_cc = null;
	/** Connection Cache r/o            */
	private static Connection[]		s_connections = null;
	/** Connection Cache Size           */
	private static int              s_conCacheSize = 2;   //  client
	/** Connection counter              */
	private static int              s_conCount = 0;
	/** Connection r/w                  */
	private static Connection		s_connectionRW = null;
	/**	Logger							*/
	private static Logger			s_log = Logger.getLogger (DB.class);

	/**
	 *	Client Login.
	 *  <p>
	 *  - Get Connection
	 *  - Compare User info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 * @param ctx context
	 * @param cc connection
	 * @param app_user user
	 * @param app_pwd pwd
	 * @param force ignore pwd
	 * @return  Array of Role KeyNamePair or null if error
	 * The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	protected static KeyNamePair[] login (Properties ctx,
		CConnection cc,
		String app_user, String app_pwd, boolean force)
	{
		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		//	Establish connection
		DB.closeTarget();
		setDBTarget(cc);
		Env.setContext(ctx, "#Host", s_cc.getAppsHost());
		Env.setContext(ctx, "#Database", s_cc.getDbName());
		if (DB.getConnectionRO() == null)
		{
			Log.saveError("NoDatabase", "");
			return null;
		}
		if (app_pwd == null)
			return null;
		//
		return loginDB (ctx, app_user, app_pwd, force);
	}   //  login

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user Principal
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx, Principal app_user)
	{
		if (app_user == null)
			return null;
		//  login w/o password as previously authorized
		return loginDB (ctx, app_user.getName(), null, false);
	}   //  app_user

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user user id
	 *  @param app_pwd password
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx,
		String app_user, String app_pwd)
	{
		if (app_pwd == null)
			return null;
		return loginDB (ctx, app_user, app_pwd, false);
	}   //  login

	/**
	 *  Actual DB login procedure.
	 *
	 *  @param ctx context
	 *  @param app_user user
	 *  @param app_pwd pwd
	 *  @param force ignore pwd
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	private static KeyNamePair[] loginDB (Properties ctx,
		String app_user, String app_pwd, boolean force)
	{
		s_log.info("login - User=" + app_user);

		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		if (app_user == null)
			return null;

		KeyNamePair[] retValue = null;
		ArrayList list = new ArrayList();
		//
		String sql = "SELECT AD_User.AD_User_ID, AD_User.Description,"
			+ " AD_Role.AD_Role_ID, AD_Role.Name "
			+ "FROM AD_User, AD_User_Roles, AD_Role "
			+ "WHERE AD_User.AD_User_ID=AD_User_Roles.AD_User_ID"
			+ " AND AD_User_Roles.AD_Role_ID=AD_Role.AD_Role_ID"
			+ " AND AD_User.Name=?"		        							//	#1
			+ " AND AD_User.IsActive='Y' AND AD_Role.IsActive='Y' AND AD_User_Roles.IsActive='Y'";
		if (app_pwd != null)
			sql += " AND (AD_User.Password=? OR AD_User.Password=?)";   	//  #2/3
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setString(1, app_user);
			if (app_pwd != null)
			{
				pstmt.setString(2, app_pwd);
				pstmt.setString(3, Secure.getDigest(app_pwd));
			}
			//	execute a query
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())		//	no record found
				if (force)
				{
					Env.setContext(ctx, "#AD_User_Name", "System");
					Env.setContext(ctx, "#AD_User_ID", "0");
					Env.setContext(ctx, "#AD_User_Description", "System Forced Login");
					Env.setContext(ctx, "#User_Level", "S  ");  	//	Format 'SCO'
					Env.setContext(ctx, "#User_Client", "0");		//	Format c1, c2, ...
					Env.setContext(ctx, "#User_Org", "0"); 		//	Format o1, o2, ...
					rs.close();
					pstmt.close();
					retValue = new KeyNamePair[] {new KeyNamePair(0, "System Administrator")};
					return retValue;
				}
				else
				{
					rs.close();
					pstmt.close();
					Log.saveError("UserPwdError", app_user, false);
					return null;
				}

			Env.setContext(ctx, "#AD_User_Name", app_user);
			Env.setContext(ctx, "#AD_User_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#SalesRep_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#AD_User_Description", rs.getString("Description"));
			//
			Ini.setProperty(Ini.P_UID, app_user);
			if (Ini.getPropertyBool(Ini.P_STORE_PWD))
				Ini.setProperty(Ini.P_PWD, app_pwd);

			do	//	read all roles
			{
				int AD_Role_ID = rs.getInt("AD_Role_ID");
				String Name = rs.getString("Name");
				KeyNamePair p = new KeyNamePair(AD_Role_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			Log.saveError("DBLogin", ex.getLocalizedMessage());
			return null;
		}

		//	Change via SQL detection comes here
		Env.setContext(ctx, "#User_SecurityID", "85263");
		//
		retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# roles = " + retValue.length);
		return retValue;
	}	//	login

	/**
	 *  Load Clients.
	 *  <p>
	 *  Sets Role info in context and loads its clients
	 *
	 *  @param ctx context
	 *  @param  role    role information
	 *  @return list of valid client KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadClients (Properties ctx, KeyNamePair role)
	{
		if (ctx == null || role == null)
			throw new IllegalArgumentException("DB.loadClients - required parameter missing");

		s_log.debug("loadClients - Role=" + role.toString());

		ArrayList list = new ArrayList();
		//	get Role details
		try
		{
			String sql = "SELECT r.UserLevel,r.ClientList,r.OrgList,r.C_Currency_ID,r.AmtApproval,"
				+ " ca.AD_Client_ID, c.Name "
				+ "FROM AD_Role r, AD_Role_ClientAccess ca, AD_Client c "
				+ "WHERE r.AD_Role_ID=?"    // #1
				+ " AND r.IsActive='Y' AND c.IsActive='Y'"
				+ " AND r.AD_Role_ID=ca.AD_Role_ID"
				+ " AND ca.AD_Client_ID=c.AD_Client_ID";

			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, role.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadClients - No Clients for Role=" + role.getKey());
				return null;
			}

			//  Role Info
			Env.setContext(ctx, "#AD_Role_ID", role.getKey());
			Env.setContext(ctx, "#AD_Role_Name", role.getName());
			Ini.setProperty(Ini.P_ROLE, role.getName());

			//	User Level
			Env.setContext(ctx, "#User_Level", rs.getString(1));  	//	Format 'SCO'
			//	ClientList
			Env.setContext(ctx, "#User_Client", rs.getString(2));	//	Format c1, c2, ...
			//	OrgList
			Env.setContext(ctx, "#User_Org", rs.getString(3)); 		//	Format o1, o2, ...
			//  Approval Currency / Amount
			Env.setContext(ctx, "#Approval_C_Currency_ID", rs.getInt(4));
			BigDecimal approval = rs.getBigDecimal(5);
			String approvalStr = "0";
			if (approval != null)
				approvalStr = approval.toString();
			Env.setContext(ctx, "#Approval_Amt", approvalStr);


			//  load Clients
			do
			{
				int AD_Client_ID = rs.getInt(6);
				String Name = rs.getString(7);
				KeyNamePair p = new KeyNamePair(AD_Client_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadClients", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# clients = " + retValue.length);
		return retValue;
	}   //  loadClients

	/**
	 *  Load Organizations.
	 *  <p>
	 *  Sets Client info in context and loads its organization, the role has access to
	 *
	 *  @param ctx context
	 *  @param  client    client information
	 *  @return list of valid Org KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadOrgs (Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadOrgs - required parameter missing");

		s_log.debug("loadOrgs - Client=" + client.toString());

		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Role_ID");

		ArrayList list = new ArrayList();
		int AD_Role_ID = Env.getContextAsInt(ctx,"#AD_Role_ID");
		s_log.debug("AD_Role_ID=" + AD_Role_ID);

		//	get Client details for role
		try
		{
			String sql = "SELECT c.Value,c.SMTPHost,c.IsMultiLingualDocument,c.AD_Language,"	//	1..4
				+ " o.AD_Org_ID,o.Name "				//	5..6
				+ "FROM AD_Client c"
				+ " INNER JOIN AD_Org o ON (o.AD_Client_ID=c.AD_Client_ID) "
				+ "WHERE o.AD_Client_ID=?"  			//  #1
				+ " AND o.IsSummary='N' AND o.IsActive='Y'"
				+ " AND o.AD_Org_ID IN "
				+   "(SELECT AD_Org_ID FROM AD_Role_OrgAccess WHERE AD_Role_ID=?)"; //  #2
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, client.getKey());
			pstmt.setInt(2, AD_Role_ID);
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadOrgs - No Org for Client=" + client.getKey());
				return null;
			}

			//  Client Info
			Env.setContext(ctx, "#AD_Client_ID", client.getKey());
			Env.setContext(ctx, "#AD_Client_Name", client.getName());
			Ini.setProperty(Ini.P_CLIENT, client.getName());
			//
			Env.setContext(ctx, "#Client_Value", rs.getString(1));
			Env.setContext(ctx, "#Client_SMTP", rs.getString(2));
			Env.setContext(ctx, "#IsMultiLingualDocument", rs.getString(3));

			//  load Orgs
			do
			{
				int AD_Org_ID = rs.getInt(5);
				String Name = rs.getString(6);
				KeyNamePair p = new KeyNamePair(AD_Org_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadOrgs", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# orgs = " + retValue.length);
		return retValue;
	}   //  loadOrgs

	/**
	 *	Load Preferences into Context for selected client.
	 *  <p>
	 *  Sets Org info in context and loads relevant field from
	 *	- AD_Client/Info,
	 *  - C_AcctSchema,
	 *  - C_AcctSchema_Elements
	 *	- AD_Preference
	 *  <p>
	 *  Assumes that the context is set for #AD_Client_ID, #AD_User_ID, #AD_Role_ID
	 *
	 *  @param ctx context
	 *  @param  org    org information
	 *  @param  warehouse   optional warehouse information
	 *  @param  timestamp   optional date
	 *  @param  printerName optional printer info
	 *  @returns AD_Message of error (NoValidAcctInfo) or ""
	 */
	public static String loadPreferences (Properties ctx,
		KeyNamePair org, KeyNamePair warehouse, Timestamp timestamp, String printerName)
	{
		Log.trace(Log.l3_Util, "DB.loadPreferences - Org=" + org);

		if (ctx == null || org == null)
			throw new IllegalArgumentException("DB.loadPreferences - required parameter missing");
		if (Env.getContext(ctx,"#AD_Client_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Client_ID");
		if (Env.getContext(ctx,"#AD_User_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_User_ID");
		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Role_ID");


		//  Org Info - assumes that it is valid
		Env.setContext(ctx, "#AD_Org_ID", org.getKey());
		Env.setContext(ctx, "#AD_Org_Name", org.getName());
		Ini.setProperty(Ini.P_ORG, org.getName());

		//  Warehouse Info
		if (warehouse != null)
		{
			Env.setContext(ctx, "#M_Warehouse_ID", warehouse.getKey());
			Ini.setProperty(Ini.P_WAREHOUSE, warehouse.getName());
		}

		//	Date (default today)
		long today = System.currentTimeMillis();
		if (timestamp != null)
			today = timestamp.getTime();
		java.sql.Date sd = new java.sql.Date(today);
		Env.setContext(ctx, "#Date", sd.toString());	//	YYYY-MM-DD

		//	Optional Printer
		if (printerName == null)
			printerName = "";
		Env.setContext(ctx, "#Printer", printerName);
		Ini.setProperty(Ini.P_PRINTER, printerName);

		//	Other
		Env.setAutoCommit(ctx, Ini.getProperty(Ini.P_A_COMMIT).equals("Y"));
		Env.setContext(ctx, "#CompiereSys", Ini.getProperty(Ini.P_COMPIERESYS));
		Env.setContext(ctx, "#ShowAcct", Ini.getProperty(Ini.P_SHOW_ACCT));
		Env.setContext(ctx, "#ShowTrl", Ini.getProperty(Ini.P_SHOW_TRL));

		String retValue = "";
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		int AD_Org_ID =  org.getKey();
		int AD_User_ID =  Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID =  Env.getContextAsInt(ctx, "#AD_Role_ID");

		//	Other Settings
		Env.setContext(ctx, "#YYYY", "Y");
		Env.setContext(ctx, "#StdPrecision", 2);

		//	AccountSchema Info (first)
		String sql = "SELECT * "
			+ "FROM C_AcctSchema a, AD_ClientInfo c "
			+ "WHERE a.C_AcctSchema_ID=c.C_AcctSchema1_ID "
			+ "AND c.AD_Client_ID=?";
		try
		{
			int C_AcctSchema_ID = 0;
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				//  No Warning for System
				if (AD_Role_ID != 0)
					retValue = "NoValidAcctInfo";
			}
			else
			{
				//	Accounting Info
				C_AcctSchema_ID = rs.getInt("C_AcctSchema_ID");
				Env.setContext(ctx, "$C_AcctSchema_ID", C_AcctSchema_ID);
				Env.setContext(ctx, "$C_Currency_ID", rs.getInt("C_Currency_ID"));
				Env.setContext(ctx, "$HasAlias", rs.getString("HasAlias"));
			}
			rs.close();
			pstmt.close();

			//	Accounting Elements
			sql = "SELECT ElementType "
				+ "FROM C_AcctSchema_Element "
				+ "WHERE C_AcctSchema_ID=?"
				+ " AND IsActive='Y'";
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, C_AcctSchema_ID);
			rs = pstmt.executeQuery();
			while (rs.next())
				Env.setContext(ctx, "$Element_" + rs.getString("ElementType"), "Y");
			rs.close();
			pstmt.close();

			//	This reads all relevant window neutral defaults
			//	overwriting superseeded ones.  Window specific is read in Mainain
			sql = "SELECT Attribute, Value, AD_Window_ID "
				+ "FROM AD_Preference "
				+ "WHERE AD_Client_ID IN (0, @#AD_Client_ID@)"
				+ " AND AD_Org_ID IN (0, @#AD_Org_ID@)"
				+ " AND (AD_User_ID IS NULL OR AD_User_ID=0 OR AD_User_ID=@#AD_User_ID@)"
				+ " AND IsActive='Y' "
				+ "ORDER BY Attribute, AD_Client_ID, AD_User_ID DESC, AD_Org_ID";
				//	the last one overwrites - System - Client - User - Org - Window
			sql = Env.parseContext(ctx, 0, sql, false);
			if (sql.length() == 0)
				s_log.error("loadPreferences - Missing Environment");
			else
			{
				pstmt = prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					int AD_Window_ID = rs.getInt(3);
					String at = "";
					if (rs.wasNull())
						at = "P|" + rs.getString(1);
					else
						at = "P" + AD_Window_ID + "|" + rs.getString(1);
					String va = rs.getString(2);
					Env.setContext(ctx, at, va);
				}
				rs.close();
				pstmt.close();
			}
		}
		catch (SQLException ex)
		{
			s_log.error("loadPreferences (" + sql + ")", ex);
		}

		//	Default Values
		Log.trace(Log.l4_Data, "Default Values ...");
		sql = "SELECT t.TableName, c.ColumnName "
			+ "FROM AD_Column c "
			+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID) "
			+ "WHERE c.IsKey='Y' AND t.IsActive='Y'"
			+ " AND EXISTS (SELECT * FROM AD_Column cc "
			+ " WHERE ColumnName = 'IsDefault' AND t.AD_Table_ID=cc.AD_Table_ID AND cc.IsActive='Y')";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				loadDefault (ctx, rs.getString(1), rs.getString(2));
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("loadPreferences", e);
		}
		//
		Ini.saveProperties(Ini.isClient());
		//
		return retValue;
	}	//	loadPreferences

	/**
	 *	Load Default Value for Table into Context.
	 *
	 *  @param ctx context
	 *  @param TableName table name
	 *  @param ColumnName column name
	 */
	private static void loadDefault (Properties ctx, String TableName, String ColumnName)
	{
		String value = null;
		//
		String SQL = "SELECT " + ColumnName + " FROM " + TableName
			+ " WHERE IsDefault='Y' AND IsActive='Y' ORDER BY AD_Client_ID";
		SQL = Access.addROAccessSQL(ctx, SQL, TableName, false);
		try
		{
			PreparedStatement pstmt = prepareStatement(SQL);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())		//	overwrites system defaults
				value = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("loadDefault - " + TableName + " (" + SQL + ")", e);
			return;
		}
		//	Set Context Value
		if (value != null && value.length() != 0)
		{
			if (TableName.equals("C_DocType"))
				Env.setContext(ctx, "#C_DocTypeTarget_ID", value);
			else
				Env.setContext(ctx, "#" + ColumnName, value);
		}
	}	//	loadDefault

	/**
	 *  Load Warehouses
	 *
	 * @param ctx context
	 * @param client client
	 * @return Array of Warehouse Info
	 */
	public static KeyNamePair[] loadWarehouses(Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadWarehouses - required parameter missing");

		Log.trace(Log.l3_Util, "DB.loadWarehouses - Client=" + client.toString());

		ArrayList list = new ArrayList();
		try
		{
			String sql = "SELECT M_Warehouse_ID, Name FROM M_Warehouse "
				+ "WHERE AD_Client_ID=? AND IsActive='Y'";  //  #1

			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, client.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				Log.trace(Log.l3_Util, "DB.loadWarehouses - No Warehouses for Client=" + client.getKey());
				return null;
			}

			//  load Warehousess
			do
			{
				int AD_Warehouse_ID = rs.getInt(1);
				String Name = rs.getString(2);
				KeyNamePair p = new KeyNamePair(AD_Warehouse_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadWarehouses", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		Log.trace(Log.l6_Database, "# warehouses = " + retValue.length);
		return retValue;
	}   //  loadWarehouses

	/*************************************************************************/

	/**
	 *  Set connection
	 *  @param cc connection
	 */
	public static void setDBTarget (CConnection cc)
	{
		if (cc == null)
			throw new IllegalArgumentException("DB.setDBTarget connection is NULL");

		if (s_cc == null)
			s_cc = cc;
		synchronized (s_cc)    //  use as mutex
		{
			s_cc = cc;
			//  Closing existing
			if (s_connections != null)
			{
				for (int i = 0; i < s_connections.length; i++)
				{
					try {
						s_connections[i].close();
					} catch (Exception e) {}
				}
			}
			s_connections = null;
		}
	}   //  setDBTarget

	/**
	 *  Is there a connection to the database ?
	 *  @return true, if connected to database
	 */
	public static boolean isConnected()
	{
		try
		{
			getConnectionRW();	//	try to get a connection
			return true;
		}
		catch (Exception e)
		{
		}
		return false;
	}   //  isConnected

	/**
	 *	Return (pooled) r/w AutoCommit, read committed connection
	 *
	 *  @return Connection (r/w)
	 */
	public static Connection getConnectionRW ()
	{
		//	check health of connection
		try
		{
			if (s_connectionRW == null)
				;
			else if (s_connectionRW.isClosed())
				s_connectionRW = null;
			else if (s_connectionRW instanceof OracleConnection && ((OracleConnection)s_connectionRW).pingDatabase(1) < 0)
				s_connectionRW = null;
			else
				 s_connectionRW.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		catch (Exception e)
		{
			s_connectionRW = null;
		}
		if (s_connectionRW == null)
			s_connectionRW = s_cc.getConnection (true, Connection.TRANSACTION_READ_COMMITTED);
		if (s_connectionRW == null)
			throw new UnsupportedOperationException("DB.getConnectionRW - @NoDBConnection@");
		return s_connectionRW;
	}   //  getConnectionRW

	/**
	 *	Return read committed, read/only connection with AutoCommit from pool
	 *  @return Connection (r/o)
	 */
	private static Connection getConnectionRO ()
	{
		try
		{
			synchronized (s_cc)    //  use as mutex as s_connection is null the first time
			{
				if (s_connections == null)
					s_connections = createConnections (Connection.TRANSACTION_READ_COMMITTED);     //  see below
			}
		}
		catch (Exception e)
		{
			s_log.error("getConnectionRO", e);
		}

		//  check health of connection
		int pos = s_conCount++;
		Connection connection = s_connections[pos % s_conCacheSize];
		try
		{
			if (connection == null)
				;
			else if (connection.isClosed())
				connection = null;
			else if (connection instanceof OracleConnection && ((OracleConnection)connection).pingDatabase(1) < 0)
				connection = null;
			else
				 connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		catch (Exception e)
		{
			connection = null;
		}
		//	Get new
		if (connection == null)
		{
			System.out.println("DB.getConnectionRO - replacing connection #" + pos % s_conCacheSize);
		//	s_log.error("getConnectionRO - replacing connection #" + pos % s_conCacheSize);
			connection = s_cc.getConnection (true, Connection.TRANSACTION_READ_COMMITTED);            //  see above
		/*	try
			{
				retValue.setReadOnly(true);     //  not supported by Oracle
			}
			catch (Exception e)
			{
				System.err.println("DB.getConnectionRO - Cannot set to R/O - " + e);
			}   */
			s_connections[pos % s_conCacheSize] = connection;
		}
		if (connection == null)
			throw new UnsupportedOperationException("DB.getConnectionRO - @NoDBConnection@");
		return connection;
	}	//	getConnectionRO

	/**
	 *	Create new Connection.
	 *  The connection must be closed explicitly by the application
	 *
	 *  @param autoCommit auto commit
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Connection connection
	 */
	public static Connection createConnection (boolean autoCommit, int trxLevel)
	{
		s_log.debug("createConnection " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", AutoCommit=" + autoCommit + ", TrxLevel=" + trxLevel);
		return s_cc.getConnection (autoCommit, trxLevel);
	}	//	createConnection

	/**
	 *	Create new set of r/o Connections.
	 *  R/O connection might not be supported by DB
	 *
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Array of Connections (size based on s_conCacheSize)
	 */
	private static Connection[] createConnections (int trxLevel)
	{
		s_log.debug("createConnections (" + s_conCacheSize + ") " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", TrxLevel=" + trxLevel);
		Connection cons[] = new Connection[s_conCacheSize];
		try
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				cons[i] = s_cc.getConnection (true, trxLevel);  //  auto commit
				if (cons[i] == null)
					System.err.println("DB.createConnections - connection is NULL");
			}
		}
		catch (Exception e)
		{
			//  Don't use Log
			System.err.println("DB.createConnections - " + e.getMessage());
		}
		return cons;
	}	//	createConnections

	/**
	 *  Get Database Driver.
	 *  Access to database specific functionality.
	 *  @return Compiere Database Driver
	 */
	public static CompiereDatabase getDatabase()
	{
		if (s_cc != null)
			return s_cc.getDatabase();
		return null;
	}   //  getDatabase

	/*************************************************************************/

	/**
	 *  Check database Version with Code version
	 *  @param ctx context
	 *  @return true if Database version (date) is the same
	 */
	public static boolean isDatabaseOK (Properties ctx)
	{
		//	Force loading Messages of current language
		Msg.getMsg(ctx, "0");

		//  Check Version
		String version = "?";
		String sql = "SELECT Version FROM AD_System";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				version = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("Problem with AD_System Table - Run system.sql script - " + e.toString());
		}
		Log.trace(Log.l6_Database, "DB.isDatabaseOK", "DB_Version=" + version);
		//  identical version
		if (Compiere.DB_VERSION.equals(version))
			return true;

		String AD_Message = "DatabaseVersionError";
		String title = org.compiere.Compiere.getName() + " " +  Msg.getMsg(ctx, AD_Message, true);
		//	Code assumes Database version {0}, but Database has Version {1}.
		String msg = Msg.getMsg(ctx, AD_Message);	//	complete message
		msg = MessageFormat.format(msg, new Object[] {Compiere.DB_VERSION, version});
		Object[] options = { UIManager.get("OptionPane.noButtonText"), "Migrate" };
		int no = JOptionPane.showOptionDialog (null, msg,
			title, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
			UIManager.getIcon("OptionPane.errorIcon"), options, options[0]);
		if (no == 1)
		{
			try
			{
				Class.forName("com.compiere.client.StartMaintain").newInstance();
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog (null,
					ex.getMessage() + "\nSee: http://www.compiere.com/maintain",
					title, JOptionPane.ERROR_MESSAGE);
				s_log.error("isDatabaseOK - " + ex.getMessage());
			}
		}
		return false;
	}   //  isDatabaseOK

	/*************************************************************************/

	/**
	 *	Security Context Login.
	 *  @param uid uid
	 *  @param pwd pwd
	 *  @param role role
	 *  @return true if connected
	 */
	public static boolean login_context (String uid, String pwd, String role)
	{
		if (uid == null || pwd == null || role == null || uid.length() == 0 || pwd.length() == 0 || role.length() == 0)
			throw new IllegalArgumentException("DB.login_context - required parameter missing");
		Log.trace(Log.l3_Util, "DB.login_context");
		if (uid == null || pwd == null || role == null)
			return false;
		//
		String SQL = "{CALL Compiere_Context.Login(?,?,?,?)}";
		boolean result = true;
		try
		{
			CallableStatement cstmt = getConnectionRO().prepareCall(SQL);
			cstmt.setString(1, uid);
			cstmt.setString(2, pwd);
			cstmt.setString(3, role);
			cstmt.setString(4, Language.getBaseAD_Language());
			result = cstmt.execute();
			cstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("login_context", e);
			result = false;
		}
		return result;
	}	//	login_context

	/*************************************************************************/

	/**
	 *	Close Target
	 */
	public static void closeTarget()
	{
		if (s_connections != null)
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				try
				{
					if (s_connections[i] != null)
						s_connections[i].close();
				}
				catch (SQLException e)
				{
					s_log.error("close connection #" + i, e);
				}
				s_connections[i] = null;
			}
			try
			{
				if (s_connectionRW != null)
					s_connectionRW.close();
			}
			catch (SQLException e)
			{
				s_log.error("close R/W connection", e);
			}
			s_connectionRW = null;
		}
		s_connections = null;
//		EJB.close();
	}	//	closeTarget

	/*************************************************************************/

	/**
	 *	Prepare Forward Read Only Call
	 *  @param RO_SQL sql (RO)
	 *  @return Callable Statement
	 */
	public static CallableStatement prepareCall(String RO_SQL)
	{
		if (RO_SQL == null || RO_SQL.length() == 0)
			throw new IllegalArgumentException("DB.prepareCall - required parameter missing - " + RO_SQL);
		//
		String sql = getDatabase().convertStatement(RO_SQL);
		try
		{
			return getConnectionRO().prepareCall
				(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		}
		catch (SQLException e)
		{
			s_log.error("prepareCall (" + sql + ")", e);
		}
		return null;
	}	//	prepareCall

	/*************************************************************************/

	/**
	 *	Prepare Read Only Statement
	 *  @param RO_SQL sql (RO)
	 *  @return Prepared Statement
	 */
	public static PreparedStatement prepareStatement(String RO_SQL)
	{
		return prepareStatement(RO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}	//	prepareStatement

	/**
	 *	Prepare Statement
	 *
	 *  @param SQL sql
	 *  @param type - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param concur - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Prepared Statement r/o or r/w depending on concur
	 */
	public static PreparedStatement prepareStatement(String SQL, int type, int concur)
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.prepareStatement - required parameter missing - " + SQL);
		//
		String sql = getDatabase().convertStatement(SQL);
		try
		{
			Connection conn = null;
			if (concur == ResultSet.CONCUR_UPDATABLE)
				conn = getConnectionRW();
			else
				conn = getConnectionRO();
			return conn.prepareStatement (sql, type, concur);
		}
		catch (SQLException e)
		{
			s_log.error("prepareStatement (" + sql + ")", e);
		}
		return null;
	}	//	prepareStatement

	/**
	 *	Create Read Only Statement
	 *  @return Statement
	 */
	public static Statement createStatement()
	{
		return createStatement (ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}	//	createStatement

	/**
	 *	Create Statement
	 *
	 *  @param type - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param concur - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Statement - either r/w ir r/o depending on concur
	 */
	public static Statement createStatement(int type, int concur)
	{
		try
		{
			Connection conn = null;
			if (concur == ResultSet.CONCUR_UPDATABLE)
				conn = getConnectionRW();
			else
				conn = getConnectionRO();
			return conn.createStatement(type, concur);
		}
		catch (SQLException e)
		{
			s_log.error("createStatement", e);
		}
		return null;
	}	//	createStatement

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param SQL sql
	 *  @return number of rows updated
	 */
	public static int executeUpdate (String SQL)
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.executeUpdate - required parameter missing - " + SQL);
		//
		String sql = getDatabase().convertStatement(SQL);
		int no = 0;
		Statement stmt = null;
		try
		{
			Connection conn = getConnectionRW();
			stmt = conn.createStatement();
			no = stmt.executeUpdate(sql);
		}
		catch (SQLException e)
		{
			s_log.error("executeUpdate - " + sql, e);
			Log.saveError("DBExecuteError", e.getLocalizedMessage());
		}
		finally
		{
			//  Always close cursor
			try
			{
				stmt.close();
			}
			catch (SQLException e2)
			{
				s_log.error("executeUpdate - cannot close statement");
			}
		}
		return no;
	}	//	execute Update


	/**
	 *	Commit - commit on RW connection.
	 *  Is not required as RW connection is AutoCommit
	 *  @param throwException if true, re-throws exception
	 *  @return true if not needed or success
	 *  @throws SQLException
	 */
	public static boolean commit (boolean throwException) throws SQLException
	{
		try
		{
			Connection conn = getConnectionRW();
		//	if (!conn.getAutoCommit())
				conn.commit();
		}
		catch (SQLException e)
		{
			s_log.error("commit", e);
			if (throwException)
				throw e;
			return false;
		}
		return true;
	}	//	commit

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @return next no
	 */
	public static int getKeyNextNo (Properties ctx, int WindowNo, String TableName)
	{
		if (ctx == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getKeyNextNo - required parameter missing");
		String CompiereSys = Env.getContext(ctx, "#CompiereSys");
		int AD_Client_ID = Env.getContextAsInt(ctx, WindowNo, "AD_Client_ID");
		//
		return getKeyNextNo (AD_Client_ID, CompiereSys, TableName);
	}   //  getKeyNextNo

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param AD_Client_ID client
	 *  @param CompiereSys compiere sys
	 *  @param TableName table name
	 *  @return next no
	 */
	public static int getKeyNextNo (int AD_Client_ID, String CompiereSys, String TableName)
	{
		if (TableName == null || CompiereSys == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getKeyNextNo - required parameter missing");
		int retValue = 0;

		//	Check CompiereSys
	//	if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
	//		throw new UnsupportedOperationException("DB.getKeyNextNo - Cannot add System records");
		//
		try
		{
			String SQL = "{CALL AD_Sequence_Next(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setString(1, TableName);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.NUMERIC);
			cstmt.executeUpdate();
			retValue = cstmt.getInt(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getKeyNextNo - Table=" + TableName + ")", e);
		}
		return retValue;
	}	//	getKeyNextNo

	/**
	 *	Get Document Number for current document.
	 *  <br>
	 *  - first search for DocType based Document No
	 *  - then Search for DocumentNo based on TableName
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @param onlyDocType Do not search for document no based on TableName
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (Properties ctx, int WindowNo, String TableName, boolean onlyDocType)
	{
		if (ctx == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getDocumentNo - required parameter missing");
		String CompiereSys = Env.getContext(ctx, "#CompiereSys");
		int AD_Client_ID = Env.getContextAsInt(ctx, WindowNo, "AD_Client_ID");

		//	Get C_DocType_ID from context - NO Defaults -
		int C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID");
		if (C_DocType_ID == 0)
			C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID");
		if (C_DocType_ID == 0)
			return getDocumentNo (AD_Client_ID, CompiereSys, TableName);

		//	Check CompiereSys
		if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
			throw new UnsupportedOperationException("DB.getDocumentNo - Cannot add System records");
		//
		String retValue = getDocumentNo(AD_Client_ID, C_DocType_ID);
		if (!onlyDocType && retValue == null)
			return getDocumentNo (AD_Client_ID, CompiereSys, TableName);
		return retValue;
	}	//	getDocumentNo

	/**
	 *	Get Document Number for current document.
	 *  @param AD_Client_ID client
	 *  @param C_DocType_ID (target) document type
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (int AD_Client_ID, int C_DocType_ID)
	{
		String retValue = null;
		try
		{
			String SQL = "{CALL AD_Sequence_DocType(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setInt(1, C_DocType_ID);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.VARCHAR);
			cstmt.executeUpdate();
			retValue = cstmt.getString(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getDocumentNo - DocType=" + C_DocType_ID, e);
		}
		Log.trace(Log.l5_DData, "DB.getDocumentNo", "DocType=" + C_DocType_ID + " -> " + retValue);
		return retValue;
	}	//	getDocumentNo


	/**
	 *  Get Next Document No
	 *  @param AD_Client_ID client
	 *  @param CompiereSys system
	 *  @param TableName table name
	 *  @return DocumentNo
	 */
	public static String getDocumentNo (int AD_Client_ID, String CompiereSys, String TableName)
	{
		if (CompiereSys == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getDocumentNo - required parameter missing");
		//	Check CompiereSys
	//	if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
	//		throw new UnsupportedOperationException("DB.getDocumentNo - Cannot add System records");
		//
		String retValue = null;
		try
		{
			String SQL = "{CALL AD_Sequence_Doc(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setString(1, "DocumentNo_" + TableName);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.VARCHAR);
			cstmt.executeUpdate();
			retValue = cstmt.getString(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getDocumentNo  TableName=" + TableName, e);
		}
		Log.trace(Log.l5_DData, "DB.getDocumentNo", "TableName=" + TableName + " -> " + retValue);
		return retValue;
	}   //  getDocumentNo

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			Log.error("DB.getSQLValue - " + sql, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 *  Convert an amount with today's spot rate
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  Amt         The amount to be converted
	 *  @return converted amount
	 */
	public static BigDecimal getConvertedAmt (BigDecimal Amt, int CurFrom_ID, int CurTo_ID)
	{
		return getConvertedAmt (Amt, CurFrom_ID, CurTo_ID, null, null);
	}   //  getConvertedAmt

	/**
	 *	Convert an amount
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  ConvDate    The Conversion date - if null - use current date
	 *  @param  RateType    The Conversion rate type - if null/empty - use Spot
	 *  @param  Amt         The amount to be converted
	 *  @return converted amount
	 */
	public static BigDecimal getConvertedAmt (BigDecimal Amt, int CurFrom_ID, int CurTo_ID,
		Timestamp ConvDate, String RateType)
	{
		if (Amt == null)
			throw new IllegalArgumentException("DB.getConvertedAmt - required parameter missing - Amt");
		BigDecimal retValue = null;
		if (CurFrom_ID == CurTo_ID || Amt.equals(Env.ZERO))
			return Amt;
		//
		try
		{
			String sql = "{? = call C_Currency_Convert(?,?,?,?,?) }";
			CallableStatement cstmt = prepareCall(sql);
			//
			cstmt.registerOutParameter(1, Types.NUMERIC);
			//
			cstmt.setBigDecimal(2, Amt);					//	Amount		IN  	NUMBER
			cstmt.setInt(3, CurFrom_ID);             		//	CurFrom		IN 		NUMBER
			cstmt.setInt(4, CurTo_ID);                  	//	CurTo		IN 		NUMBER
			if (ConvDate == null)
				ConvDate = new Timestamp (System.currentTimeMillis());
			cstmt.setTimestamp(5, ConvDate);				//	ConvDate	IN 		DATE
			if (RateType == null || RateType.equals(""))
				RateType = "S";
			cstmt.setString(6, RateType);					//	RateType	IN 		CHAR
			//
			cstmt.executeUpdate();
			retValue = cstmt.getBigDecimal(1);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getConvertedAmt", e);
		}
		return retValue;
	}	//	getConvertedAmt

	/**
	 *	Get Currency Rate
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  ConvDate    The Conversion date - if null - use current date
	 *  @param  RateType    The Conversion rate type - if null/empty - use Spot
	 *  @return currency Rate
	 */
	public static BigDecimal getConvesionRate (int CurFrom_ID, int CurTo_ID,
		Timestamp ConvDate, String RateType)
	{
		BigDecimal retValue = null;
		if (CurFrom_ID == CurTo_ID)
			return new BigDecimal("1");
		//
		try
		{
			String sql = "{? = call C_Currency_Rate(?,?,?,?)}";
			CallableStatement cstmt = prepareCall(sql);
			//
			cstmt.registerOutParameter(1, Types.NUMERIC);
			//
			cstmt.setInt(2, CurFrom_ID);             		//	CurFrom		IN 		NUMBER
			cstmt.setInt(3, CurTo_ID);                  	//	CurTo		IN 		NUMBER
			if (ConvDate == null)
				ConvDate = new Timestamp (System.currentTimeMillis());
			cstmt.setTimestamp(4, ConvDate);				//	ConvDate	IN 		DATE
			if (RateType == null || RateType.equals(""))
				RateType = "S";
			cstmt.setString(5, RateType);					//	RateType	IN 		CHAR
			//
			cstmt.executeUpdate();
			retValue = cstmt.getBigDecimal(1);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getConvesionRate", e);
		}
		return retValue;
	}	//	getConvesionRate


	/**
	 *  Get fully qualified Account Combination
	 *
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param C_AcctSchema_ID acct schema
	 *  @param Account_ID natural account
	 *  @param  base_ValidCombination_ID optional base combination to be specified
	 *  @param Alias aloas
	 *  @param AD_User_ID user
	 *  @param M_Product_ID product
	 *  @param C_BPartner_ID partner
	 *  @param AD_OrgTrx_ID trx org
	 *  @param C_LocFrom_ID loc from
	 *  @param C_LocTo_ID loc to
	 *  @param C_SRegion_ID sales region
	 *  @param C_Project_ID project
	 *  @param C_Campaign_ID campaign
	 *  @param C_Activity_ID activity
	 *  @param User1_ID user1
	 *  @param User2_ID user2
	 *  @return C_ValidCombination_ID of existing or new Combination
	 */
	public static int getValidCombination (int AD_Client_ID, int AD_Org_ID,
		int C_AcctSchema_ID, int Account_ID, int base_ValidCombination_ID, String Alias, int AD_User_ID,
		int M_Product_ID, int C_BPartner_ID, int AD_OrgTrx_ID,
		int C_LocFrom_ID, int C_LocTo_ID, int C_SRegion_ID, int C_Project_ID,
		int C_Campaign_ID, int C_Activity_ID, int User1_ID, int User2_ID)
	{
		int retValue = 0;
		StringBuffer sb = new StringBuffer ("C_ValidCombination_Get(");
		try
		{
			String sql = "{CALL C_ValidCombination_Get(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)}";
			CallableStatement cstmt = prepareCall(sql);
			//  1 - ID
			cstmt.registerOutParameter(1, Types.NUMERIC);
			sb.append("v,");
			//  --  Mandatory Accounting fields
			//	2 - AD_Client_ID
			cstmt.setInt(2, AD_Client_ID);
			sb.append(AD_Client_ID).append(",");
			//	3 - AD_Org_ID
			cstmt.setInt(3, AD_Org_ID);
			sb.append(AD_Org_ID).append(",");
			//  4- C_AcctSchema_ID
			cstmt.setInt(4, C_AcctSchema_ID);
			sb.append(C_AcctSchema_ID).append(",");
			//  5 - Account_ID
			cstmt.setInt(5, Account_ID);
			sb.append(Account_ID).append(", ");

			//  --  Optional
			//  6 - Base C_ValidCombination_ID
			if (base_ValidCombination_ID != 0)
			{
				cstmt.setInt (6, base_ValidCombination_ID);
				sb.append(base_ValidCombination_ID).append(",");
			}
			else
			{
				cstmt.setNull(6, Types.NUMERIC);
				sb.append("NULL,");
			}
			//  7 - MustBeFullyQualified
			cstmt.setString(7, "Y");
			sb.append("'Y',");
			//  8 - Alias
			if (Alias != null && Alias.length() > 0)
			{
				cstmt.setString(8, Alias);
				sb.append("'").append(Alias).append("';");
			}
			else
			{
				cstmt.setNull(8, Types.VARCHAR);
				sb.append("NULL,");
			}
			//  9 - CreatedBy
			cstmt.setInt(9, AD_User_ID);
			sb.append(AD_User_ID).append(", ");

			//	--  Optional Accounting fields
			//  10 - M_Product_ID
			if (M_Product_ID != 0)
			{
				cstmt.setInt(10, M_Product_ID);
				sb.append(M_Product_ID).append(",");
			}
			else
			{
				cstmt.setNull(10, Types.NUMERIC);
				sb.append("NULL,");
			}
			//  11 - C_BPartner_ID
			if (C_BPartner_ID != 0)
			{
				cstmt.setInt(11, C_BPartner_ID);
				sb.append(C_BPartner_ID).append(",");
			}
			else
			{
				cstmt.setNull(11, Types.NUMERIC);
				sb.append("NULL,");
			}
			//  12 - AD_OrgTrx_ID
			if (AD_OrgTrx_ID != 0)
			{
				cstmt.setInt(12, AD_OrgTrx_ID);
				sb.append(AD_OrgTrx_ID).append(",");
			}
			else
			{
				cstmt.setNull(12, Types.NUMERIC);
				sb.append("NULL,");
			}
			//  13 - C_LocFrom_ID
			if (C_LocFrom_ID != 0)
			{
				cstmt.setInt(13, C_LocFrom_ID);
				sb.append(C_LocFrom_ID).append(",");
			}
			else
			{
				cstmt.setNull(13, Types.NUMERIC);
				sb.append("NULL,");
			}
			//  14 - C_LocTo_ID
			if (C_LocTo_ID != 0)
			{
				cstmt.setInt(14, (C_LocTo_ID));
				sb.append(C_LocTo_ID).append(", ");
			}
			else
			{
				cstmt.setNull(14, Types.NUMERIC);
				sb.append("NULL, ");
			}
			//  15 - C_SalesRegion_ID
			if (C_SRegion_ID != 0)
			{
				cstmt.setInt(15, (C_SRegion_ID));
				sb.append(C_SRegion_ID).append(",");
			}
			else
			{
				cstmt.setNull(15, Types.NUMERIC);
				sb.append("NULL,");
			}
			//  16 - C_Project_ID
			if (C_Project_ID != 0)
			{
				cstmt.setInt(16, (C_Project_ID));
				sb.append(C_Project_ID).append(",");
			}
			else
			{
				cstmt.setNull(16, Types.NUMERIC);
				sb.append("NULL,");
			}
			//  17 - C_Campaign_ID
			if (C_Campaign_ID != 0)
			{
				cstmt.setInt(17, (C_Campaign_ID));
				sb.append(C_Campaign_ID).append(",");
			}
			else
			{
				cstmt.setNull(17, Types.NUMERIC);
				sb.append("NULL,");
			}
			//  18 - C_Activity_ID
			if (C_Activity_ID != 0)
			{
				cstmt.setInt(18, (C_Activity_ID));
				sb.append(C_Activity_ID).append(",");
			}
			else
			{
				cstmt.setNull(18, Types.NUMERIC);
				sb.append("NULL,");
			}
			//  19 - User1_ID
			if (User1_ID != 0)
			{
				cstmt.setInt(19, (User1_ID));
				sb.append(User1_ID).append(",");
			}
			else
			{
				cstmt.setNull(19, Types.NUMERIC);
				sb.append("NULL,");
			}
			//  20 - User2_ID
			if (User2_ID != 0)
			{
				cstmt.setInt(20, (User2_ID));
				sb.append(User2_ID).append(")");
			}
			else
			{
				cstmt.setNull(20, Types.NUMERIC);
				sb.append("NULL)");
			}

			//
			cstmt.executeUpdate();
			retValue = cstmt.getInt(1);     //  1 - ID
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getValidCombination " + sb.toString(), e);
		}
		return retValue;
	}   //  getValidCombination

	/**
	 *  Insert Note
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param AD_User_ID user
	 *  @param AD_Table_ID table
	 *  @param Record_ID record
	 *  @param AD_MessageValue message
	 *  @param Text text
	 *  @param Reference reference
	 *  @return true if note was inserted
	 */
	public static boolean insertNote (int AD_Client_ID, int AD_Org_ID, int AD_User_ID,
		int AD_Table_ID, int Record_ID,
		String AD_MessageValue, String Text, String Reference)
	{
		if (AD_MessageValue == null || AD_MessageValue.length() == 0)
			throw new IllegalArgumentException("DB.insertNote - required parameter missing - AD_Message");

		//  Database limits
		if (Text == null)
			Text = "";
		if (Text.length() > 2000)
			Text = Text.substring(0,1999);
		if (Reference == null)
			Reference = "";
		if (Reference.length() > 60)
			Reference = Reference.substring(0,59);
		//
		Log.trace(Log.l3_Util, "DB.insertNote - " + AD_MessageValue + " - " + Reference);
		//
		StringBuffer sql = new StringBuffer ("INSERT INTO AD_Note (AD_Note_ID,");
		sql.append("AD_Client_ID,AD_Org_ID,IsActive, Created,CreatedBy,Updated,UpdatedBy, ")
			.append("AD_Message_ID,Text,Reference, ")
			.append("AD_User_ID,AD_Table_ID,Record_ID,Processed) ")
			.append("SELECT ");
		//
		String CompiereSys = "N";
		int AD_Note_ID = getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Note");
		sql.append(AD_Note_ID).append(",");
		//
		sql.append(AD_Client_ID).append(",")
			.append(AD_Org_ID).append(", 'Y',SysDate,")
			.append(AD_User_ID).append(",SysDate,0,");
		//
		sql.append(" AD_Message_ID,'").append(Text).append("','")
			.append(Reference).append("', ");
		//
		sql.append(AD_User_ID).append(",").append(AD_Table_ID).append(",")
			.append(Record_ID).append(",'N' ");
		//
		sql.append("FROM AD_Message WHERE Value='").append(AD_MessageValue).append("'");
		//  Create Entry
		int no = executeUpdate(sql.toString());

		//  AD_Message must exist, so if not created, it is probably
		//  due to non-existing AD_Message
		if (no == 0)
		{
			sql = new StringBuffer ("INSERT INTO AD_Note (AD_Note_ID,");
			sql.append("AD_Client_ID,AD_Org_ID,IsActive, Created,CreatedBy,Updated,UpdatedBy, ")
				.append("AD_Message_ID,Text,Reference, ")
				.append("AD_User_ID,AD_Table_ID,Record_ID,Processed) ")
				.append("SELECT ");
			//  - use AD_Note_ID from above
			sql.append(AD_Note_ID).append(",");
			//
			sql.append(AD_Client_ID).append(",")
				.append(AD_Org_ID).append(", 'Y',SysDate,")
				.append(AD_User_ID).append(",SysDate,0, ");
			//
			sql.append("AD_Message_ID,'").append(AD_MessageValue).append(": ").append(Text).append("','")
				.append(Reference).append("', ");
			//
			sql.append(AD_User_ID).append(",").append(AD_Table_ID).append(",")
				.append(Record_ID).append(",'N' ");
			//  Hardcoded AD_Message - AD_Message is in Text
			sql.append("FROM AD_Message WHERE Value='NoMessageFound'");
			//  Try again
			no = executeUpdate(sql.toString());
		}

		return no == 1;
	}   //  insertNote


	/*************************************************************************/

	/**
	 *	Print SQL Warnings.
	 *  <br>
	 *		Usage: DB.printWarning(rs.getWarnings(), "xx");
	 *  @param comment comment
	 *  @param warning warning
	 */
	public static void printWarning(String comment, SQLWarning warning)
	{
		if (comment == null || warning == null || comment.length() == 0)
			throw new IllegalArgumentException("DB.printException - required parameter missing");
		s_log.warn("SQL Warning: " + comment);
		if (warning == null)
			return;
		//
		SQLWarning warn = warning;
		while (warn != null)
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append(warn.getMessage());
			buffer.append("; State=").append(warn.getSQLState()).append("; ErrorCode=").append(warn.getErrorCode());
			s_log.warn(buffer.toString());
			warn = warn.getNextWarning();
		}
	}	//	printWarning

	/**
	 *  Create SQL TO Date String from Timestamp
	 *
	 *  @param  time Date to be converted
	 *  @param  dayOnly true if time set to 00:00:00
	 *
	 *  @return TO_DATE('2001-01-30 18:10:20',''YYYY-MM-DD HH24:MI:SS')
	 *      or  TO_DATE('2001-01-30',''YYYY-MM-DD')
	 */
	public static String TO_DATE (Timestamp time, boolean dayOnly)
	{
		if (time == null)
		{
			if (dayOnly)
				return "TRUNC(SysDate)";
			return "SysDate";
		}

		StringBuffer dateString = new StringBuffer("TO_DATE('");
		//  YYYY-MM-DD HH24:MI:SS.mmmm  JDBC Timestamp format
		String myDate = time.toString();
		if (dayOnly)
		{
			dateString.append(myDate.substring(0,10));
			dateString.append("','YYYY-MM-DD')");
		}
		else
		{
			dateString.append(myDate.substring(0, myDate.indexOf(".")));	//	cut off miliseconds
			dateString.append("','YYYY-MM-DD HH24:MI:SS')");
		}
		return dateString.toString();
	}   //  TO_DATE

	/**
	 *  Create SQL TO Date String from Timestamp
	 *  @param time time
	 *  @return TO_DATE String
	 */
	public static String TO_DATE (Timestamp time)
	{
		return TO_DATE(time, true);
	}   //  TO_DATE

	/**
	 *  Create SQL for formatted Date, Number
	 *
	 *  @see org.compiere.util.DisplayType
	 *  @see org.compiere.util.Env
	 *
	 *  @param  columnName  the column name in the SQL
	 *  @param  displayType Display Type
	 *  @param  AD_Language 6 character language setting (from Env.LANG_*)
	 *
	 *  @return TRIM(TO_CHAR(columnName,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.'''))
	 *      or TRIM(TO_CHAR(columnName,'TM9')) depending on DisplayType and Language
	 */
	public static String TO_CHAR (String columnName, int displayType, String AD_Language)
	{
		if (columnName == null || AD_Language == null || columnName.length() == 0)
			throw new IllegalArgumentException("DB.TO_CHAR - required parameter missing");

		StringBuffer retValue = new StringBuffer("TRIM(TO_CHAR(");
		retValue.append(columnName);

		//  Numbers
		if (DisplayType.isNumeric(displayType))
		{
			if (displayType == DisplayType.Amount)
				retValue.append(",'9G999G990D00'");
			else
				retValue.append(",'TM9'");
			//  TO_CHAR(GrandTotal,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.''')
			if (!Language.isDecimalPoint(AD_Language))      //  reversed
				retValue.append(",'NLS_NUMERIC_CHARACTERS='',.'''");
		}
		else if (DisplayType.isDate(displayType))
		{
			retValue.append(",'")
				.append(Language.getLanguage(AD_Language).getDBdatePattern())
				.append("'");
		}

		retValue.append("))");
		//
		return retValue.toString();
	}   //  TO_CHAR_Number


	/**
	 *  Package Strings for SQL command
	 *  @param txt  String with text
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt)
	{
		return TO_STRING (txt, 0);
	}   //  TO_STRING

	/**
	 *	Package Strings for SQL command.
	 *  <pre>
	 *		-	include in '
	 *		-	replace ' with ''
	 *      -   replace \ with \\
	 *  </pre>
	 *  @param txt  String with text
	 *  @param maxLength    Maximum Length of content or 0 to ignore
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt, int maxLength)
	{
		if (txt == null)
			return "NULL";

		//  Length
		String text = txt;
		if (maxLength != 0 && text.length() > maxLength)
			text = txt.substring(0, maxLength);

		//  copy characters		(wee need to look through anyway)
		StringBuffer out = new StringBuffer("'");
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (c == '\'')
				out.append("''");
			else if (c == '\\')
				out.append("\\\\");
			else
				out.append(c);
		}
		out.append("'");
		//
		return out.toString();
	}	//	TO_STRING

}	//	DB

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
package org.compiere.util;

import java.rmi.*;
import java.security.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import javax.sql.*;
import javax.swing.*;

import oracle.jdbc.*;

import org.compiere.*;
import org.compiere.db.*;
import org.compiere.interfaces.*;
import org.compiere.model.*;
import org.compiere.process.*;

/**
 *  General Database Interface
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB.java,v 1.68 2004/09/10 02:53:28 jjanke Exp $
 */
public final class DB
{
	/** Connection Descriptor           */
	private static CConnection      s_cc = null;
	/** Connection Cache r/o            */
	private static Connection[]		s_connections = null;
	/** Connection Cache Size           */
	private static int              s_conCacheSize = Ini.isClient() ? 3 : 3;
	/** Connection counter              */
	private static int              s_conCount = 0;
	/** Connection r/w                  */
	private static Connection		s_connectionRW = null;
	/**	Logger							*/
	private static Logger			s_log = Logger.getCLogger (DB.class);

	/**
	 *	Client Login.
	 *  <p>
	 *  - Get Connection
	 *  - Compare User info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 * @param ctx context
	 * @param cc connection
	 * @param app_user user
	 * @param app_pwd pwd
	 * @param force ignore pwd
	 * @return  Array of Role KeyNamePair or null if error
	 * The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	protected static KeyNamePair[] login (Properties ctx,
		CConnection cc,
		String app_user, String app_pwd, boolean force)
	{
		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		//	Establish connection
		setDBTarget(cc);
		Env.setContext(ctx, "#Host", s_cc.getAppsHost());
		Env.setContext(ctx, "#Database", s_cc.getDbName());
		
		if (getConnectionRO() == null)
		{
			Log.saveError("NoDatabase", "");
			return null;
		}
		if (app_pwd == null)
			return null;
		//
		return loginDB (ctx, app_user, app_pwd, force);
	}   //  login

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user Principal
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx, Principal app_user)
	{
		if (app_user == null)
			return null;
		//  login w/o password as previously authorized
		return loginDB (ctx, app_user.getName(), null, false);
	}   //  app_user

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user user id
	 *  @param app_pwd password
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx,
		String app_user, String app_pwd)
	{
		if (app_pwd == null)
			return null;
		return loginDB (ctx, app_user, app_pwd, false);
	}   //  login

	/**
	 *  Actual DB login procedure.
	 *
	 *  @param ctx context
	 *  @param app_user user
	 *  @param app_pwd pwd
	 *  @param force ignore pwd
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	private static KeyNamePair[] loginDB (Properties ctx,
		String app_user, String app_pwd, boolean force)
	{
		s_log.info("login - User=" + app_user);

		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		if (app_user == null)
			return null;

		KeyNamePair[] retValue = null;
		ArrayList list = new ArrayList();
		//
		String sql = "SELECT AD_User.AD_User_ID, AD_User.Description,"
			+ " AD_Role.AD_Role_ID, AD_Role.Name "
			+ "FROM AD_User, AD_User_Roles, AD_Role "
			+ "WHERE AD_User.AD_User_ID=AD_User_Roles.AD_User_ID"
			+ " AND AD_User_Roles.AD_Role_ID=AD_Role.AD_Role_ID"
			+ " AND AD_User.Name=?"		        							//	#1
			+ " AND AD_User.IsActive='Y' AND AD_Role.IsActive='Y' AND AD_User_Roles.IsActive='Y'";
		if (app_pwd != null)
			sql += " AND (AD_User.Password=? OR AD_User.Password=?)";   	//  #2/3
		sql += " ORDER BY AD_Role.Name";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setString(1, app_user);
			if (app_pwd != null)
			{
				pstmt.setString(2, app_pwd);
				pstmt.setString(3, Secure.getDigest(app_pwd));
			}
			//	execute a query
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())		//	no record found
				if (force)
				{
					Env.setContext(ctx, "#AD_User_Name", "System");
					Env.setContext(ctx, "#AD_User_ID", "0");
					Env.setContext(ctx, "#AD_User_Description", "System Forced Login");
					Env.setContext(ctx, "#User_Level", "S  ");  	//	Format 'SCO'
					Env.setContext(ctx, "#User_Client", "0");		//	Format c1, c2, ...
					Env.setContext(ctx, "#User_Org", "0"); 		//	Format o1, o2, ...
					rs.close();
					pstmt.close();
					retValue = new KeyNamePair[] {new KeyNamePair(0, "System Administrator")};
					return retValue;
				}
				else
				{
					rs.close();
					pstmt.close();
					Log.saveError("UserPwdError", app_user, false);
					return null;
				}

			Env.setContext(ctx, "#AD_User_Name", app_user);
			Env.setContext(ctx, "#AD_User_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#SalesRep_ID", rs.getInt("AD_User_ID"));
			//
			Ini.setProperty(Ini.P_UID, app_user);
			if (Ini.getPropertyBool(Ini.P_STORE_PWD))
				Ini.setProperty(Ini.P_PWD, app_pwd);

			do	//	read all roles
			{
				int AD_Role_ID = rs.getInt("AD_Role_ID");
				if (AD_Role_ID == 0)
					Env.setContext(ctx, "#SysAdmin", "Y");
				String Name = rs.getString("Name");
				KeyNamePair p = new KeyNamePair(AD_Role_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
			pstmt = null;
			//
			retValue = new KeyNamePair[list.size()];
			list.toArray(retValue);
			s_log.debug("login - User=" + app_user + " - roles #" + retValue.length);
		}
		catch (SQLException ex)
		{
			s_log.error("login", ex);
			Log.saveError("DBLogin", ex);
			retValue = null;
		}
		//
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		return retValue;
	}	//	login

	/**
	 *  Load Clients.
	 *  <p>
	 *  Sets Role info in context and loads its clients
	 *
	 *  @param ctx context
	 *  @param  role    role information
	 *  @return list of valid client KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadClients (Properties ctx, KeyNamePair role)
	{
		if (ctx == null || role == null)
			throw new IllegalArgumentException("DB.loadClients - required parameter missing");

	//	s_log.debug("loadClients - Role: " + role.toStringX());

		ArrayList list = new ArrayList();
		KeyNamePair[] retValue = null;
		String sql = "SELECT DISTINCT r.UserLevel,"			//	1
			+ " c.AD_Client_ID,c.Name "						//	2/3 
			+ "FROM AD_Role r" 
			+ " INNER JOIN AD_Client c ON (r.AD_Client_ID=c.AD_Client_ID) "
			+ "WHERE r.AD_Role_ID=?"		//	#1
			+ " AND r.IsActive='Y' AND c.IsActive='Y'";

		PreparedStatement pstmt = null;
		//	get Role details
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, role.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadClients - No Clients for Role: " + role.toStringX());
				return null;
			}

			//  Role Info
			Env.setContext(ctx, "#AD_Role_ID", role.getKey());
			Env.setContext(ctx, "#AD_Role_Name", role.getName());
			Ini.setProperty(Ini.P_ROLE, role.getName());

			//	User Level
			Env.setContext(ctx, "#User_Level", rs.getString(1));  	//	Format 'SCO'

			//  load Clients
			do
			{
				int AD_Client_ID = rs.getInt(2);
				String Name = rs.getString(3);
				KeyNamePair p = new KeyNamePair(AD_Client_ID, Name);
				list.add(p);
			}
			while (rs.next());
			rs.close();
			pstmt.close();
			pstmt = null;
			//
			retValue = new KeyNamePair[list.size()];
			list.toArray(retValue);
			s_log.debug("loadClients - Role: " + role.toStringX() + " - clients #" + retValue.length);
		}
		catch (SQLException ex)
		{
			s_log.error("loadClients", ex);
			retValue = null;
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		return retValue;
	}   //  loadClients

	/**
	 *  Load Organizations.
	 *  <p>
	 *  Sets Client info in context and loads its organization, the role has access to
	 *
	 *  @param ctx context
	 *  @param  client    client information
	 *  @return list of valid Org KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadOrgs (Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadOrgs - required parameter missing");
		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)	//	could be number 0
			throw new UnsupportedOperationException("DB.loadOrgs - Missing Context #AD_Role_ID");
		
		int AD_Role_ID = Env.getContextAsInt(ctx,"#AD_Role_ID");
	//	s_log.debug("loadOrgs - Client: " + client.toStringX() + ", AD_Role_ID=" + AD_Role_ID);

		//	get Client details for role
		ArrayList list = new ArrayList();
		KeyNamePair[] retValue = null;
		//
		String sql = "SELECT o.AD_Org_ID,o.Name "				//	1..2
			+ "FROM AD_Role r, AD_Client c"
			+ " INNER JOIN AD_Org o ON (c.AD_Client_ID=o.AD_Client_ID OR o.AD_Org_ID=0) "
			+ "WHERE r.AD_Role_ID=?" 		//	#1
			+ " AND c.AD_Client_ID=?"		//	#2
			+ " AND o.IsSummary='N' AND o.IsActive='Y'"
			+ " AND (r.IsAccessAllOrgs='Y'"
			+ "  OR o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ra "
				+ "WHERE ra.AD_Role_ID=r.AD_Role_ID AND ra.IsActive='Y')) "
			+ "ORDER BY o.Name";
		PreparedStatement pstmt = prepareStatement(sql);
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, AD_Role_ID);
			pstmt.setInt(2, client.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadOrgs - No Org for Client: " + client.toStringX());
				return null;
			}

			//  Client Info
			Env.setContext(ctx, "#AD_Client_ID", client.getKey());
			Env.setContext(ctx, "#AD_Client_Name", client.getName());
			Ini.setProperty(Ini.P_CLIENT, client.getName());

			//  load Orgs
			do
			{
				int AD_Org_ID = rs.getInt(1);
				String Name = rs.getString(2);
				KeyNamePair p = new KeyNamePair(AD_Org_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
			pstmt = null;
			//
			retValue = new KeyNamePair[list.size()];
			list.toArray(retValue);
			s_log.debug("loadOrgs - Client: " + client.toStringX() 
				+ ", AD_Role_ID=" + AD_Role_ID + " - orgs #" + retValue.length);
		}
		catch (SQLException ex)
		{
			s_log.error("loadOrgs", ex);
			retValue = null;
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		return retValue;
	}   //  loadOrgs
	
	/**
	 *  Load Warehouses
	 *
	 * @param ctx context
	 * @param org organization
	 * @return Array of Warehouse Info
	 */
	public static KeyNamePair[] loadWarehouses(Properties ctx, KeyNamePair org)
	{
		if (ctx == null || org == null)
			throw new IllegalArgumentException("DB.loadWarehouses - required parameter missing");

	//	s_log.info("loadWarehouses - Org: " + org.toStringX());

		ArrayList list = new ArrayList();
		KeyNamePair[] retValue = null;
		String sql = "SELECT M_Warehouse_ID, Name FROM M_Warehouse "
			+ "WHERE AD_Org_ID=? AND IsActive='Y' "
			+ "ORDER BY Name";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, org.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.info("loadWarehouses - No Warehouses for Org: " + org.toStringX());
				return null;
			}

			//  load Warehousess
			do
			{
				int AD_Warehouse_ID = rs.getInt(1);
				String Name = rs.getString(2);
				KeyNamePair p = new KeyNamePair(AD_Warehouse_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
			pstmt = null;
			//
			retValue = new KeyNamePair[list.size()];
			list.toArray(retValue);
			s_log.debug("loadWarehouses - Org: " + org.toStringX()
				+ " - warehouses #" + retValue.length);
		}
		catch (SQLException ex)
		{
			s_log.error("loadWarehouses", ex);
			retValue = null;
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		return retValue;
	}   //  loadWarehouses

	
	/**
	 *	Load Preferences into Context for selected client.
	 *  <p>
	 *  Sets Org info in context and loads relevant field from
	 *	- AD_Client/Info,
	 *  - C_AcctSchema,
	 *  - C_AcctSchema_Elements
	 *	- AD_Preference
	 *  <p>
	 *  Assumes that the context is set for #AD_Client_ID, #AD_User_ID, #AD_Role_ID
	 *
	 *	@param ctx context
	 *  @param  org    org information
	 *  @param  warehouse   optional warehouse information
	 *  @param  timestamp   optional date
	 *  @param  printerName optional printer info
	 *  @returns AD_Message of error (NoValidAcctInfo) or ""
	 */
	public static String loadPreferences (Properties ctx,
		KeyNamePair org, KeyNamePair warehouse, Timestamp timestamp, String printerName)
	{
		s_log.info("loadPreferences - Org: " + org.toStringX());

		if (ctx == null || org == null)
			throw new IllegalArgumentException("DB.loadPreferences - required parameter missing");
		if (Env.getContext(ctx,"#AD_Client_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Client_ID");
		if (Env.getContext(ctx,"#AD_User_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_User_ID");
		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Role_ID");

		postMigration(ctx);
		
		//  Org Info - assumes that it is valid
		Env.setContext(ctx, "#AD_Org_ID", org.getKey());
		Env.setContext(ctx, "#AD_Org_Name", org.getName());
		Ini.setProperty(Ini.P_ORG, org.getName());

		//  Warehouse Info
		if (warehouse != null)
		{
			Env.setContext(ctx, "#M_Warehouse_ID", warehouse.getKey());
			Ini.setProperty(Ini.P_WAREHOUSE, warehouse.getName());
		}

		//	Date (default today)
		long today = System.currentTimeMillis();
		if (timestamp != null)
			today = timestamp.getTime();
		java.sql.Date sd = new java.sql.Date(today);
		Env.setContext(ctx, "#Date", sd.toString());	//	YYYY-MM-DD

		//	Optional Printer
		if (printerName == null)
			printerName = "";
		Env.setContext(ctx, "#Printer", printerName);
		Ini.setProperty(Ini.P_PRINTER, printerName);
		
		//	Load Role Info
		MRole.getDefault(ctx, true);	

		//	Other
		Env.setAutoCommit(ctx, Ini.getPropertyBool(Ini.P_A_COMMIT));
		if (MRole.getDefault(ctx, false).isShowAcct())
			Env.setContext(ctx, "#ShowAcct", Ini.getProperty(Ini.P_SHOW_ACCT));
		else
			Env.setContext(ctx, "#ShowAcct", "N");
		Env.setContext(ctx, "#ShowTrl", Ini.getProperty(Ini.P_SHOW_TRL));

		String retValue = "";
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		int AD_Org_ID =  org.getKey();
		int AD_User_ID =  Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID =  Env.getContextAsInt(ctx, "#AD_Role_ID");

		//	Other Settings
		Env.setContext(ctx, "#YYYY", "Y");
		Env.setContext(ctx, "#StdPrecision", 2);

		//	AccountSchema Info (first)
		String sql = "SELECT * "
			+ "FROM C_AcctSchema a, AD_ClientInfo c "
			+ "WHERE a.C_AcctSchema_ID=c.C_AcctSchema1_ID "
			+ "AND c.AD_Client_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			int C_AcctSchema_ID = 0;
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				//  No Warning for System
				if (AD_Role_ID != 0)
					retValue = "NoValidAcctInfo";
			}
			else
			{
				//	Accounting Info
				C_AcctSchema_ID = rs.getInt("C_AcctSchema_ID");
				Env.setContext(ctx, "$C_AcctSchema_ID", C_AcctSchema_ID);
				Env.setContext(ctx, "$C_Currency_ID", rs.getInt("C_Currency_ID"));
				Env.setContext(ctx, "$HasAlias", rs.getString("HasAlias"));
			}
			rs.close();
			pstmt.close();

			//	Accounting Elements
			sql = "SELECT ElementType "
				+ "FROM C_AcctSchema_Element "
				+ "WHERE C_AcctSchema_ID=?"
				+ " AND IsActive='Y'";
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, C_AcctSchema_ID);
			rs = pstmt.executeQuery();
			while (rs.next())
				Env.setContext(ctx, "$Element_" + rs.getString("ElementType"), "Y");
			rs.close();
			pstmt.close();

			//	This reads all relevant window neutral defaults
			//	overwriting superseeded ones.  Window specific is read in Mainain
			sql = "SELECT Attribute, Value, AD_Window_ID "
				+ "FROM AD_Preference "
				+ "WHERE AD_Client_ID IN (0, @#AD_Client_ID@)"
				+ " AND AD_Org_ID IN (0, @#AD_Org_ID@)"
				+ " AND (AD_User_ID IS NULL OR AD_User_ID=0 OR AD_User_ID=@#AD_User_ID@)"
				+ " AND IsActive='Y' "
				+ "ORDER BY Attribute, AD_Client_ID, AD_User_ID DESC, AD_Org_ID";
				//	the last one overwrites - System - Client - User - Org - Window
			sql = Env.parseContext(ctx, 0, sql, false);
			if (sql.length() == 0)
				s_log.error("loadPreferences - Missing Environment");
			else
			{
				pstmt = prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					int AD_Window_ID = rs.getInt(3);
					String at = "";
					if (rs.wasNull())
						at = "P|" + rs.getString(1);
					else
						at = "P" + AD_Window_ID + "|" + rs.getString(1);
					String va = rs.getString(2);
					Env.setContext(ctx, at, va);
				}
				rs.close();
				pstmt.close();
			}

			//	Default Values
			s_log.info("Default Values ...");
			sql = "SELECT t.TableName, c.ColumnName "
				+ "FROM AD_Column c "
				+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID) "
				+ "WHERE c.IsKey='Y' AND t.IsActive='Y'"
				+ " AND EXISTS (SELECT * FROM AD_Column cc "
				+ " WHERE ColumnName = 'IsDefault' AND t.AD_Table_ID=cc.AD_Table_ID AND cc.IsActive='Y')";
			pstmt = prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next())
				loadDefault (ctx, rs.getString(1), rs.getString(2));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException e)
		{
			s_log.error("loadPreferences", e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		//
		Ini.saveProperties(Ini.isClient());
		//	Country
		Env.setContext(ctx, "#C_Country_ID", MCountry.getDefault(ctx).getC_Country_ID());
		return retValue;
	}	//	loadPreferences

	/**
	 *	Load Default Value for Table into Context.
	 *
	 *  @param ctx context
	 *  @param TableName table name
	 *  @param ColumnName column name
	 */
	private static void loadDefault (Properties ctx, String TableName, String ColumnName)
	{
		if (TableName.startsWith("AD_Window")
			|| TableName.startsWith("AD_PrintFormat")
			|| TableName.startsWith("AD_Workflow") )
			return;
		String value = null;
		//
		String sql = "SELECT " + ColumnName + " FROM " + TableName	//	most specific first
			+ " WHERE IsDefault='Y' AND IsActive='Y' ORDER BY AD_Client_ID DESC, AD_Org_ID DESC";
		sql = MRole.getDefault(ctx, false).addAccessSQL(sql, 
			TableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				value = rs.getString(1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException e)
		{
			s_log.error("loadDefault - " + TableName + " (" + sql + ")", e);
			return;
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		//	Set Context Value
		if (value != null && value.length() != 0)
		{
			if (TableName.equals("C_DocType"))
				Env.setContext(ctx, "#C_DocTypeTarget_ID", value);
			else
				Env.setContext(ctx, "#" + ColumnName, value);
		}
	}	//	loadDefault

	/**
	 * 	Check need for post Upgrade
	 *	@return true if post upgrade ran - false if there was no need
	 */
	private static boolean postMigration (Properties ctx)
	{
		MSystem system = MSystem.get(ctx); 
		if (!system.isJustMigrated())
			return false;
		
		s_log.info("postMigration");
		//	Role update
		String sql = "SELECT * FROM AD_Role";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
			{
				MRole role = new MRole (ctx, rs);
				role.updateAccessRecords();
			}
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error ("postMigration(1)", e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		
		//	Reset Flag
		system.setIsJustMigrated(false);
		return system.save();
	}	//	checkUpgrade
	
	
	/**************************************************************************
	 *  Set connection
	 *  @param cc connection
	 */
	public static void setDBTarget (CConnection cc)
	{
		if (cc == null)
			throw new IllegalArgumentException("DB.setDBTarget connection is NULL");

		DB.closeTarget();
		//
		if (s_cc == null)
			s_cc = cc;
		synchronized (s_cc)    //  use as mutex
		{
			s_cc = cc;
			s_connections = null;
			s_connectionRW = null;
		}
		s_cc.setDataSource();
		s_log.debug("setDBTarget - " + s_cc + " - DS=" + s_cc.isDataSource());
	//	Trace.printStack();
	}   //  setDBTarget

	/**
	 *  Is there a connection to the database ?
	 *  @return true, if connected to database
	 */
	public static boolean isConnected()
	{
		try
		{
			getConnectionRW();	//	try to get a connection
			return true;
		}
		catch (Exception e)
		{
		}
		return false;
	}   //  isConnected

	/**
	 *	Return (pooled) r/w AutoCommit, read committed connection.
	 *	For Transaction control use Trx.getConnection()
	 *  @return Connection (r/w)
	 */
	public static Connection getConnectionRW ()
	{
		//	check health of connection
		try
		{
			if (s_connectionRW == null)
				;
			else if (s_connectionRW.isClosed())
			{
				System.out.println("DB.getConnectionRW - closed");
				s_connectionRW = null;
			}
			else if (s_connectionRW instanceof OracleConnection && ((OracleConnection)s_connectionRW).pingDatabase(1) < 0)
			{
				System.out.println("DB.getConnectionRW - no ping");
				s_connectionRW = null;
			}
			else
			{
				if (s_connectionRW.getTransactionIsolation() != Connection.TRANSACTION_SERIALIZABLE)
					s_connectionRW.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			}
		}
		catch (Exception e)
		{
			s_connectionRW = null;
		}
		//	Get new
		if (s_connectionRW == null)
		{
			s_connectionRW = s_cc.getConnection (true, Connection.TRANSACTION_SERIALIZABLE);
			if (Log.isTraceLevel(8))
				s_log.debug("getConnectionRW - " + s_connectionRW);
		}
		if (s_connectionRW == null)
			throw new UnsupportedOperationException("DB.getConnectionRW - @NoDBConnection@");
		//
	//	System.err.println ("DB.getConnectionRW - " + s_connectionRW); 
	//	Trace.printStack();
		return s_connectionRW;
	}   //  getConnectionRW

	/**
	 *	Return read committed, read/only from pool.
	 *  @return Connection (r/o)
	 */
	public static Connection getConnectionRO ()
	{
		try
		{
			synchronized (s_cc)    //  use as mutex as s_connection is null the first time
			{
				if (s_connections == null)
					s_connections = createConnections (Connection.TRANSACTION_READ_COMMITTED);     //  see below
			}
		}
		catch (Exception e)
		{
			s_log.error("getConnectionRO", e);
		}

		//  check health of connection
		int pos = s_conCount++;
		int connectionNo = pos % s_conCacheSize;
		Connection connection = s_connections[connectionNo];
		try
		{
			if (connection == null)
				;
			else if (connection.isClosed())
			{
			//	RowSet.close also closes connection!
			//	System.out.println("DB.getConnectionRO - closed #" + connectionNo);
				connection = null;
			}
			else if (connection instanceof OracleConnection && ((OracleConnection)connection).pingDatabase(1) < 0)
			{
				System.out.println("DB.getConnectionRO - no ping #" + connectionNo);
				connection = null;
			}
			else
			{
				if (!connection.isReadOnly())
					connection.setReadOnly(true);
				if (connection.getTransactionIsolation() != Connection.TRANSACTION_READ_COMMITTED)
					connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			}
		}
		catch (Exception e)
		{
			System.out.println("DB.getConnectionRO #" + connectionNo + " - " + e.toString());
			connection = null;
		}
		//	Get new
		if (connection == null)
		{
			if (Log.isTraceLevel(8))
				s_log.debug("getConnectionRO - replacing connection #" + connectionNo);
			connection = s_cc.getConnection (true, Connection.TRANSACTION_READ_COMMITTED); //  see above
			try
			{
				if (connection != null)
					connection.setReadOnly(true);
			}
			catch (Exception e)
			{
				System.err.println("DB.getConnectionRO - Cannot set to R/O - " + e);
			} 
			s_connections[connectionNo] = connection;
		}
		if (connection == null)
			throw new UnsupportedOperationException("DB.getConnectionRO - @NoDBConnection@");
		if (Log.isTraceLevel(10))
			s_log.debug("getConnectionRO - #" + connectionNo + " - " + connection);
	//	System.err.println ("DB.getConnectionRO - " + connection); 
		return connection;
	}	//	getConnectionRO

	/**
	 *	Create new Connection.
	 *  The connection must be closed explicitly by the application
	 *
	 *  @param autoCommit auto commit
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Connection connection
	 */
	public static Connection createConnection (boolean autoCommit, int trxLevel)
	{
		s_log.debug("createConnection " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", AutoCommit=" + autoCommit + ", TrxLevel=" + trxLevel);
		return s_cc.getConnection (autoCommit, trxLevel);
	}	//	createConnection

	/**
	 *	Create new set of r/o Connections.
	 *  R/O connection might not be supported by DB
	 *
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Array of Connections (size based on s_conCacheSize)
	 */
	private static Connection[] createConnections (int trxLevel)
	{
		s_log.debug("createConnections (" + s_conCacheSize + ") " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() 
			+ ", TrxLevel=" + CConnection.getTransactionIsolationInfo(trxLevel));
		Connection cons[] = new Connection[s_conCacheSize];
		try
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				cons[i] = s_cc.getConnection (true, trxLevel);  //  auto commit
				if (cons[i] == null)
					System.err.println("createConnections - connection is NULL");	//	don't use log
			}
		}
		catch (Exception e)
		{
			//  Don't use Log
			System.err.println("DB.createConnections - " + e.getMessage());
		}
		return cons;
	}	//	createConnections

	/**
	 *  Get Database Driver.
	 *  Access to database specific functionality.
	 *  @return Compiere Database Driver
	 */
	public static CompiereDatabase getDatabase()
	{
		if (s_cc != null)
			return s_cc.getDatabase();
		return null;
	}   //  getDatabase

	/**
	 * 	Get Database Info
	 *	@return info
	 */
	public static String getDatabaseInfo()
	{
		if (s_cc != null)
			return s_cc.toStringDetail();
		return "No DB";
	}	//	getDatabaseInfo
	
	/**************************************************************************
	 *  Check database Version with Code version
	 *  @param ctx context
	 *  @return true if Database version (date) is the same
	 */
	public static boolean isDatabaseOK (Properties ctx)
	{
		//  Check Version
		String version = "?";
		String sql = "SELECT Version FROM AD_System";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				version = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("Problem with AD_System Table - Run system.sql script - " + e.toString());
			return false;
		}
		s_log.info("isDatabaseOK - DB_Version=" + version);
		//  Identical DB version
		if (Compiere.DB_VERSION.equals(version))
			return true;

		String AD_Message = "DatabaseVersionError";
		String title = org.compiere.Compiere.getName() + " " +  Msg.getMsg(ctx, AD_Message, true);
		//	Code assumes Database version {0}, but Database has Version {1}.
		String msg = Msg.getMsg(ctx, AD_Message);	//	complete message
		msg = MessageFormat.format(msg, new Object[] {Compiere.DB_VERSION, version});
		Object[] options = { UIManager.get("OptionPane.noButtonText"), "Migrate" };
		int no = JOptionPane.showOptionDialog (null, msg,
			title, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
			UIManager.getIcon("OptionPane.errorIcon"), options, options[0]);
		if (no == 1)
		{
			try
			{
				Class.forName("com.compiere.client.StartMaintain").newInstance();
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog (null,
					ex.getMessage() + "\nSee: http://www.compiere.com/maintain",
					title, JOptionPane.ERROR_MESSAGE);
				s_log.error("isDatabaseOK - " + ex.getMessage());
			}
		}
		return false;
	}   //  isDatabaseOK

	
	/**************************************************************************
	 *	Secure Context Login.
	 *  @param uid uid
	 *  @param pwd pwd
	 *  @param role role
	 *  @return true if connected
	 */
	public static boolean login_context (String uid, String pwd, String role)
	{
		if (uid == null || pwd == null || role == null || uid.length() == 0 || pwd.length() == 0 || role.length() == 0)
			throw new IllegalArgumentException("DB.login_context - required parameter missing");
		s_log.info("login_context uid=" + uid);
		if (uid == null || pwd == null || role == null)
			return false;
		//
		String SQL = "{CALL Compiere_Context.Login(?,?,?,?)}";
		boolean result = true;
		try
		{
			CallableStatement cstmt = getConnectionRO().prepareCall(SQL);
			cstmt.setString(1, uid);
			cstmt.setString(2, pwd);
			cstmt.setString(3, role);
			cstmt.setString(4, Language.getBaseAD_Language());
			result = cstmt.execute();
			cstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("login_context", e);
			result = false;
		}
		return result;
	}	//	login_context

	
	/**************************************************************************
	 *	Close Target
	 */
	public static void closeTarget()
	{
		boolean closed = false;
		//	RO connection
		if (s_connections != null)
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				try
				{
					if (s_connections[i] != null)
					{
						closed = true;
						s_connections[i].close();
					}
				}
				catch (SQLException e)
				{
					s_log.warn("close connection #" + i + " - " + e.getMessage());
				}
				s_connections[i] = null;
			}
		}
		s_connections = null;
		//	RW connection
		try
		{
			if (s_connectionRW != null)
			{
				closed = true;
				s_connectionRW.close();
			}
		}
		catch (SQLException e)
		{
			s_log.error("close R/W connection", e);
		}
		s_connectionRW = null;
		//	CConnection
		if (s_cc != null)
		{
			closed = true;
			s_cc.setDataSource(null);
		}
		s_cc = null;
		if (closed)
			s_log.debug("closeTarget");
	}	//	closeTarget

	
	/**************************************************************************
	 *	Prepare Forward Read Only Call
	 *  @param RO_SQL sql (RO)
	 *  @return Callable Statement
	 */
	public static CallableStatement prepareCall(String RO_SQL)
	{
		if (RO_SQL == null || RO_SQL.length() == 0)
			throw new IllegalArgumentException("DB.prepareCall - required parameter missing - " + RO_SQL);
		//
		String sql = getDatabase().convertStatement(RO_SQL);
		try
		{
			return getConnectionRO().prepareCall
				(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		}
		catch (SQLException e)
		{
			s_log.error("prepareCall (" + sql + ")", e);
		}
		return null;
	}	//	prepareCall

	
	/**************************************************************************
	 *	Prepare Read Only Statement
	 *  @param RO_SQL sql (RO)
	 *  @return Prepared Statement
	 */
	public static CPreparedStatement prepareStatement (String RO_SQL)
	{
		return prepareStatement(RO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, null);
	}	//	prepareStatement

	/**
	 *	Prepare Read Only Statement
	 *  @param RO_SQL sql (RO)
	 * 	@param trxName transaction
	 *  @return Prepared Statement
	 */
	public static CPreparedStatement prepareStatement (String RO_SQL, String trxName)
	{
		return prepareStatement(RO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, trxName);
	}	//	prepareStatement
	
	/**
	 *	Prepare Statement.
	 *  @param SQL sql statement
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Prepared Statement r/o or r/w depending on concur
	 */
	public static CPreparedStatement prepareStatement(String sql, 
		int resultSetType, int resultSetConcurrency)
	{
		return prepareStatement(sql, resultSetType, resultSetConcurrency, null);
	}	//	prepareStatement

	/**
	 *	Prepare Statement.
	 *  @param sql sql statement
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 * 	@param trxName transaction name
	 *  @return Prepared Statement r/o or r/w depending on concur
	 */
	public static CPreparedStatement prepareStatement(String sql, 
		int resultSetType, int resultSetConcurrency, String trxName)
	{
		if (sql == null || sql.length() == 0)
			throw new IllegalArgumentException("DB.prepareStatement - No SQL");
		//
		return new CPreparedStatement(resultSetType, resultSetConcurrency, sql, trxName);
	}	//	prepareStatement

	/**
	 *	Create Read Only Statement
	 *  @return Statement
	 */
	public static Statement createStatement()
	{
		return createStatement (ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, null);
	}	//	createStatement

	/**
	 *	Create Statement.
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 * 	@param trxName transaction name
	 *  @return Statement - either r/w ir r/o depending on concur
	 */
	public static Statement createStatement(int resultSetType, int resultSetConcurrency, String trxName)
	{
		return new CStatement(resultSetType, resultSetConcurrency, trxName);
	}	//	createStatement

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql)
	{
		return executeUpdate(sql, false, null);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 * 	@param trxName optional transaction name
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql, String trxName)
	{
		return executeUpdate(sql, false, trxName);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 * 	@param ignoreError if true, no execution error is reported
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql, boolean ignoreError)
	{
		return executeUpdate (sql, ignoreError, null);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 * 	@param ignoreError if true, no execution error is reported
	 * 	@param trxName optional transaction name
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql, boolean ignoreError, String trxName)
	{
		if (sql == null || sql.length() == 0)
			throw new IllegalArgumentException("DB.executeUpdate - required parameter missing - " + sql);
		//
		int no = -1;
		CPreparedStatement cs = new CPreparedStatement(ResultSet.TYPE_FORWARD_ONLY, 
			ResultSet.CONCUR_UPDATABLE, sql, trxName);
		
		try
		{
			no = cs.executeUpdate();
			//	No Transaction - Commit
			if (trxName == null)
			{
				cs.commit();	//	Local commit
			//	Connection conn = cs.getConnection();
			//	if (conn != null && !conn.getAutoCommit())	//	is null for remote
			//		conn.commit();
			}
		}
		catch (SQLException e)
		{
			if (!ignoreError)
			{
				s_log.error ("executeUpdate - " + cs.getSql(), e);
				Log.saveError ("DBExecuteError", e);
			}
		}
		finally
		{
			//  Always close cursor
			try
			{
				cs.close();
			}
			catch (SQLException e2)
			{
				s_log.error("executeUpdate - cannot close statement");
			}
		}
		return no;
	}	//	executeUpdate

	/**
	 *	Execute Update and throw exception.
	 *  @param SQL sql
	 *  @return number of rows updated or -1 if error
	 * 	@throws SQLException
	 */
	public static int executeUpdateEx (String SQL, String trxName) throws SQLException
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.executeUpdate - required parameter missing - " + SQL);
		//
		String sql = getDatabase().convertStatement(SQL);
		int no = -1;
		SQLException ex = null;
		Connection conn = null;
		Statement stmt = null;
		try
		{
			Trx trx = trxName == null ? null : Trx.get(trxName, true);
			if (trx != null)
				conn = trx.getConnection();
			else
				conn = DB.getConnectionRW ();
			stmt = conn.createStatement();
			no = stmt.executeUpdate(sql);
		}
		catch (SQLException e)
		{
			ex = e;
		}
		finally
		{
			//  Always close cursor
			try
			{
				stmt.close();
			}
			catch (SQLException e2)
			{
				s_log.error("executeUpdate - cannot close statement");
			}
		}
		if (ex != null)
			throw new SQLException(ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
		return no;
	}	//	execute Update

	/**
	 *	Commit - commit on RW connection.
	 *  Is not required as RW connection is AutoCommit (exception: with transaction)
	 *  @param throwException if true, re-throws exception
	 * 	@param trxName transaction name
	 *  @return true if not needed or success
	 *  @throws SQLException
	 */
	public static boolean commit (boolean throwException, String trxName) throws SQLException
	{
		try
		{
			Connection conn = null;
			Trx trx = trxName == null ? null : Trx.get(trxName, true);
			if (trx != null)
				conn = trx.getConnection();
			else
				conn = DB.getConnectionRW ();
		//	if (!conn.getAutoCommit())
			conn.commit();
		}
		catch (SQLException e)
		{
			s_log.error("commit", e);
			if (throwException)
				throw e;
			return false;
		}
		return true;
	}	//	commit

	/**
	 *	Rollback - rollback on RW connection.
	 *  Is has no effect as RW connection is AutoCommit (exception: with transaction)
	 *  @param throwException if true, re-throws exception
	 * 	@param trxName transaction name
	 *  @return true if not needed or success
	 *  @throws SQLException
	 */
	public static boolean rollback (boolean throwException, String trxName) throws SQLException
	{
		try
		{
			Connection conn = null;
			Trx trx = trxName == null ? null : Trx.get(trxName, true);
			if (trx != null)
				conn = trx.getConnection();
			else
				conn = DB.getConnectionRW ();
		//	if (!conn.getAutoCommit())
			conn.rollback();
		}
		catch (SQLException e)
		{
			s_log.error("rollback", e);
			if (throwException)
				throw e;
			return false;
		}
		return true;
	}	//	commit

	/**
	 * 	Get Row Set.
	 * 	When a Rowset is closed, it also closes the underlying connection.
	 * 	If the created RowSet is transfered by RMI, closing it makes no difference 
	 *	@param sql sql
	 *	@param local local RowSet (own connection)
	 *	@return row set or null
	 */
	public static RowSet getRowSet (String sql, boolean local)
	{
		RowSet retValue = null;
		CStatementVO info = new CStatementVO ( 
			RowSet.TYPE_SCROLL_INSENSITIVE, RowSet.CONCUR_READ_ONLY, sql);
		CPreparedStatement stmt = new CPreparedStatement(info);
		if (local)
		{
			retValue = stmt.local_getRowSet();
		}
		else
		{
			retValue = stmt.remote_getRowSet();
		}
		return retValue;
	}	//	getRowSet

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else if (Log.isTraceLevel(6))
				s_log.warn("getSQLValue - No Value " + sql);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int int_param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, String int_param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setString(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@param s_param2 parameter 2
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int int_param1, String s_param2)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			pstmt.setString(2, s_param2);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value: " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get String Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or null
	 */
	public static String getSQLValueString (String sql, int int_param1)
	{
		String retValue = null;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getString(1);
			else
				s_log.warn("getSQLValueString - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValueString - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValueString

	
	/**
	 * 	Get Array of Key Name Pairs
	 *	@param sql select with id / name as first / second column
	 *	@param optional if true (-1,"") is added 
	 *	@return array of key name pairs
	 */
	public static KeyNamePair[] getKeyNamePairs(String sql, boolean optional)
	{
		PreparedStatement pstmt = null;
		ArrayList list = new ArrayList();
		if (optional)
			list.add (new KeyNamePair(-1, ""));
		try
		{
			pstmt = DB.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new KeyNamePair(rs.getInt(1), rs.getString(2)));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getKeyNamePairs " + sql, e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
	//	s_log.debug("getKeyNamePairs #" + retValue.length);
		return retValue;		
	}	//	getKeyNamePairs
	
	
	/**************************************************************************
	 *	Get next number for Key column = 0 is Error.
	 *   * @param ctx client
	@param TableName table name
	 * 	@param trxName optionl transaction name
	 *  @return next no
	 */
	public static int getNextID (Properties ctx, String TableName, String trxName)
	{
		if (ctx == null)
			throw new IllegalArgumentException("DB.getNextID - Context missing");
		if (TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getNextID - TableName missing");
		return getNextID(Env.getAD_Client_ID(ctx), TableName, trxName);
	}	//	getNextID

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param AD_Client_ID client
	 *  @param TableName table name
	 * 	@param trxName optional Transaction Name
	 *  @return next no
	 */
	public static int getNextID (int AD_Client_ID, String TableName, String trxName)
	{
		if (isClientRemote())
		{
			Server server = CConnection.get().getServer();
			try
			{
				if (server != null)
				{
					int id = server.getNextID(AD_Client_ID, TableName, null);
					if (Log.isTraceLevel(10))
						s_log.debug("getNextID - server => " + id);
					return id;
				}
				s_log.error("getNextID - AppsServer not found - " + TableName + ", Remote=" + DB.isClientRemote());
			}
			catch (RemoteException ex)
			{
				s_log.error("getNextID - AppsServer error", ex);
			}
			//	Try locally
		}
		return MSequence.getNextID (AD_Client_ID, TableName, trxName);
	}	//	getNextID
	
	/**
	 * 	Get Document No based on Document Type
	 *	@param C_DocType_ID document type
	 * 	@param trxName optional Transaction Name
	 *	@return document no or null
	 */
	public static String getDocumentNo(int C_DocType_ID, String trxName)
	{
		if (isClientRemote())
		{
			Server server = CConnection.get().getServer();
			try
			{
				if (server != null)
				{
					String no = server.getDocumentNo (C_DocType_ID, trxName);
					if (Log.isTraceLevel(10))
						s_log.debug("getNextID - server => " + no);
					return no;
				}
				s_log.error("getDocumentNo - AppsServer not found - " + C_DocType_ID + ", Remote=" + DB.isClientRemote());
			}
			catch (RemoteException ex)
			{
				s_log.error("getDocumentNo - AppsServer error", ex);
			}
		}
		return MSequence.getDocumentNo (C_DocType_ID, trxName);
	}	//	getDocumentNo


	/**
	 * 	Get Document No from table
	 *	@param AD_Client_ID client
	 *	@param TableName table name
	 * 	@param trxName optional Transaction Name
	 *	@return document no or null
	 */
	public static String getDocumentNo(int AD_Client_ID, String TableName, String trxName)
	{
		if (isClientRemote())
		{
			Server server = CConnection.get().getServer();
			try
			{
				if (server != null)
				{
					String no = server.getDocumentNo (AD_Client_ID, TableName, trxName);
					if (Log.isTraceLevel(10))
						s_log.debug("getNextID - server => " + no);
					return no;
				}
				s_log.error("getDocumentNo - AppsServer not found - " + TableName + ", Remote=" + DB.isClientRemote());
			}
			catch (RemoteException ex)
			{
				s_log.error("getDocumentNo - AppsServer error", ex);
			}
		}
		return MSequence.getDocumentNo (AD_Client_ID, TableName, trxName);
	}	//	getDocumentNo

	/**
	 *	Get Document Number for current document.
	 *  <br>
	 *  - first search for DocType based Document No
	 *  - then Search for DocumentNo based on TableName
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @param onlyDocType Do not search for document no based on TableName
	 * 	@param trxName optional Transaction Name
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (Properties ctx, int WindowNo, 
		String TableName, boolean onlyDocType, String trxName)
	{
		if (ctx == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("getDocumentNo - required parameter missing");
		int AD_Client_ID = Env.getContextAsInt(ctx, WindowNo, "AD_Client_ID");

		//	Get C_DocType_ID from context - NO Defaults -
		int C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID");
		if (C_DocType_ID == 0)
			C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID");
		if (C_DocType_ID == 0)
		{
			s_log.debug("getDocumentNo - for Window=" + WindowNo
				+ " - Target=" + Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID") + "/" + Env.getContextAsInt(ctx, WindowNo, "C_DocTypeTarget_ID")
				+ " - Actual=" + Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID") + "/" + Env.getContextAsInt(ctx, WindowNo, "C_DocType_ID"));
			return getDocumentNo (AD_Client_ID, TableName, trxName);
		}

		String retValue = getDocumentNo (C_DocType_ID, trxName);
		if (!onlyDocType && retValue == null)
			return getDocumentNo (AD_Client_ID, TableName, trxName);
		return retValue;
	}	//	getDocumentNo

	/**
	 * 	Is this is remote client connection
	 *	@return true if client and RMI or Objects on Server
	 */
	public static boolean isClientRemote()
	{
		return Ini.isClient() 
			&& (CConnection.get().isRMIoverHTTP() || Ini.isClientObjects())
			&& CConnection.get().isAppsServerOK(false);
	}	//	isClientRemote
	
	
	
	/**************************************************************************

	/**
	 *	Print SQL Warnings.
	 *  <br>
	 *		Usage: DB.printWarning(rs.getWarnings(), "xx");
	 *  @param comment comment
	 *  @param warning warning
	 */
	public static void printWarning(String comment, SQLWarning warning)
	{
		if (comment == null || warning == null || comment.length() == 0)
			throw new IllegalArgumentException("DB.printException - required parameter missing");
		s_log.warn("SQL Warning: " + comment);
		if (warning == null)
			return;
		//
		SQLWarning warn = warning;
		while (warn != null)
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append(warn.getMessage());
			buffer.append("; State=").append(warn.getSQLState()).append("; ErrorCode=").append(warn.getErrorCode());
			s_log.warn(buffer.toString());
			warn = warn.getNextWarning();
		}
	}	//	printWarning

	/**
	 *  Create SQL TO Date String from Timestamp
	 *
	 *  @param  time Date to be converted
	 *  @param  dayOnly true if time set to 00:00:00
	 *
	 *  @return TO_DATE('2001-01-30 18:10:20',''YYYY-MM-DD HH24:MI:SS')
	 *      or  TO_DATE('2001-01-30',''YYYY-MM-DD')
	 */
	public static String TO_DATE (Timestamp time, boolean dayOnly)
	{
		if (time == null)
		{
			if (dayOnly)
				return "TRUNC(SysDate)";
			return "SysDate";
		}

		StringBuffer dateString = new StringBuffer("TO_DATE('");
		//  YYYY-MM-DD HH24:MI:SS.mmmm  JDBC Timestamp format
		String myDate = time.toString();
		if (dayOnly)
		{
			dateString.append(myDate.substring(0,10));
			dateString.append("','YYYY-MM-DD')");
		}
		else
		{
			dateString.append(myDate.substring(0, myDate.indexOf(".")));	//	cut off miliseconds
			dateString.append("','YYYY-MM-DD HH24:MI:SS')");
		}
		return dateString.toString();
	}   //  TO_DATE

	/**
	 *  Create SQL TO Date String from Timestamp
	 *  @param day day time
	 *  @return TO_DATE String
	 */
	public static String TO_DATE (Timestamp day)
	{
		return TO_DATE(day, true);
	}   //  TO_DATE

	/**
	 *  Create SQL for formatted Date, Number
	 *
	 *  @param  columnName  the column name in the SQL
	 *  @param  displayType Display Type
	 *  @param  AD_Language 6 character language setting (from Env.LANG_*)
	 *
	 *  @return TRIM(TO_CHAR(columnName,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.'''))
	 *      or TRIM(TO_CHAR(columnName,'TM9')) depending on DisplayType and Language
	@see org.compiere.util.DisplayType
	 *  @see org.compiere.util.Env
	 *
	 *   */
	public static String TO_CHAR (String columnName, int displayType, String AD_Language)
	{
		if (columnName == null || AD_Language == null || columnName.length() == 0)
			throw new IllegalArgumentException("DB.TO_CHAR - required parameter missing");

		StringBuffer retValue = new StringBuffer("TRIM(TO_CHAR(");
		retValue.append(columnName);

		//  Numbers
		if (DisplayType.isNumeric(displayType))
		{
			if (displayType == DisplayType.Amount)
				retValue.append(",'9G999G990D00'");
			else
				retValue.append(",'TM9'");
			//  TO_CHAR(GrandTotal,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.''')
			if (!Language.isDecimalPoint(AD_Language))      //  reversed
				retValue.append(",'NLS_NUMERIC_CHARACTERS='',.'''");
		}
		else if (DisplayType.isDate(displayType))
		{
			retValue.append(",'")
				.append(Language.getLanguage(AD_Language).getDBdatePattern())
				.append("'");
		}

		retValue.append("))");
		//
		return retValue.toString();
	}   //  TO_CHAR_Number


	/**
	 *  Package Strings for SQL command
	 *  @param txt  String with text
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt)
	{
		return TO_STRING (txt, 0);
	}   //  TO_STRING

	/**
	 *	Package Strings for SQL command.
	 *  <pre>
	 *		-	include in ' (single quotes)
	 *		-	replace ' with ''
	 *  </pre>
	 *  @param txt  String with text
	 *  @param maxLength    Maximum Length of content or 0 to ignore
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt, int maxLength)
	{
		if (txt == null)
			return "NULL";

		//  Length
		String text = txt;
		if (maxLength != 0 && text.length() > maxLength)
			text = txt.substring(0, maxLength);

		char quote = '\'';
		//  copy characters		(wee need to look through anyway)
		StringBuffer out = new StringBuffer();
		out.append(quote);		//	'
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (c == quote)
				out.append("''");
			else
				out.append(c);
		}
		out.append(quote);		//	'
		//
		return out.toString();
	}	//	TO_STRING

}	//	DB

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
package org.compiere.util;

import java.math.*;
import java.util.*;
import java.text.*;
import java.security.*;
import javax.swing.*;

import java.sql.*;
import javax.sql.*;
import oracle.jdbc.*;

import org.compiere.Compiere;
import org.compiere.db.*;
import org.compiere.model.MRole;

/**
 *  General Database Interface
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB.java,v 1.34 2003/11/06 07:09:09 jjanke Exp $
 */
public final class DB
{
	/** Connection Descriptor           */
	private static CConnection      s_cc = null;
	/** Connection Cache r/o            */
	private static Connection[]		s_connections = null;
	/** Connection Cache Size           */
	private static int              s_conCacheSize = 2;   //  client
	/** Connection counter              */
	private static int              s_conCount = 0;
	/** Connection r/w                  */
	private static Connection		s_connectionRW = null;
	/**	Logger							*/
	private static Logger			s_log = Logger.getCLogger (DB.class);

	/**
	 *	Client Login.
	 *  <p>
	 *  - Get Connection
	 *  - Compare User info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 * @param ctx context
	 * @param cc connection
	 * @param app_user user
	 * @param app_pwd pwd
	 * @param force ignore pwd
	 * @return  Array of Role KeyNamePair or null if error
	 * The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	protected static KeyNamePair[] login (Properties ctx,
		CConnection cc,
		String app_user, String app_pwd, boolean force)
	{
		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		//	Establish connection
		closeTarget();
		setDBTarget(cc);
		Env.setContext(ctx, "#Host", s_cc.getAppsHost());
		Env.setContext(ctx, "#Database", s_cc.getDbName());
		if (getConnectionRO() == null)
		{
			Log.saveError("NoDatabase", "");
			return null;
		}
		if (app_pwd == null)
			return null;
		//
		return loginDB (ctx, app_user, app_pwd, force);
	}   //  login

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user Principal
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx, Principal app_user)
	{
		if (app_user == null)
			return null;
		//  login w/o password as previously authorized
		return loginDB (ctx, app_user.getName(), null, false);
	}   //  app_user

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user user id
	 *  @param app_pwd password
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx,
		String app_user, String app_pwd)
	{
		if (app_pwd == null)
			return null;
		return loginDB (ctx, app_user, app_pwd, false);
	}   //  login

	/**
	 *  Actual DB login procedure.
	 *
	 *  @param ctx context
	 *  @param app_user user
	 *  @param app_pwd pwd
	 *  @param force ignore pwd
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	private static KeyNamePair[] loginDB (Properties ctx,
		String app_user, String app_pwd, boolean force)
	{
		s_log.info("login - User=" + app_user);

		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		if (app_user == null)
			return null;

		KeyNamePair[] retValue = null;
		ArrayList list = new ArrayList();
		//
		String sql = "SELECT AD_User.AD_User_ID, AD_User.Description,"
			+ " AD_Role.AD_Role_ID, AD_Role.Name "
			+ "FROM AD_User, AD_User_Roles, AD_Role "
			+ "WHERE AD_User.AD_User_ID=AD_User_Roles.AD_User_ID"
			+ " AND AD_User_Roles.AD_Role_ID=AD_Role.AD_Role_ID"
			+ " AND AD_User.Name=?"		        							//	#1
			+ " AND AD_User.IsActive='Y' AND AD_Role.IsActive='Y' AND AD_User_Roles.IsActive='Y'";
		if (app_pwd != null)
			sql += " AND (AD_User.Password=? OR AD_User.Password=?)";   	//  #2/3
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setString(1, app_user);
			if (app_pwd != null)
			{
				pstmt.setString(2, app_pwd);
				pstmt.setString(3, Secure.getDigest(app_pwd));
			}
			//	execute a query
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())		//	no record found
				if (force)
				{
					Env.setContext(ctx, "#AD_User_Name", "System");
					Env.setContext(ctx, "#AD_User_ID", "0");
					Env.setContext(ctx, "#AD_User_Description", "System Forced Login");
					Env.setContext(ctx, "#User_Level", "S  ");  	//	Format 'SCO'
					Env.setContext(ctx, "#User_Client", "0");		//	Format c1, c2, ...
					Env.setContext(ctx, "#User_Org", "0"); 		//	Format o1, o2, ...
					rs.close();
					pstmt.close();
					retValue = new KeyNamePair[] {new KeyNamePair(0, "System Administrator")};
					return retValue;
				}
				else
				{
					rs.close();
					pstmt.close();
					Log.saveError("UserPwdError", app_user, false);
					return null;
				}

			Env.setContext(ctx, "#AD_User_Name", app_user);
			Env.setContext(ctx, "#AD_User_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#SalesRep_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#AD_User_Description", rs.getString("Description"));
			//
			Ini.setProperty(Ini.P_UID, app_user);
			if (Ini.getPropertyBool(Ini.P_STORE_PWD))
				Ini.setProperty(Ini.P_PWD, app_pwd);

			do	//	read all roles
			{
				int AD_Role_ID = rs.getInt("AD_Role_ID");
				String Name = rs.getString("Name");
				KeyNamePair p = new KeyNamePair(AD_Role_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("login", ex);
			Log.saveError("DBLogin", ex.getLocalizedMessage());
			return null;
		}

		//	Change via SQL detection comes here
		Env.setContext(ctx, "#User_SecurityID", "85263");
		//
		retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# roles = " + retValue.length);
		return retValue;
	}	//	login

	/**
	 *  Load Clients.
	 *  <p>
	 *  Sets Role info in context and loads its clients
	 *
	 *  @param ctx context
	 *  @param  role    role information
	 *  @return list of valid client KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadClients (Properties ctx, KeyNamePair role)
	{
		if (ctx == null || role == null)
			throw new IllegalArgumentException("DB.loadClients - required parameter missing");

		s_log.debug("loadClients - Role=" + role.toString());

		ArrayList list = new ArrayList();
		//	get Role details
		try
		{
			String sql = "SELECT DISTINCT r.UserLevel,r.ClientList,r.OrgList,"
				+ " r.C_Currency_ID,r.AmtApproval, oa.AD_Client_ID,c.Name "
				+ "FROM AD_Role r"
				+ " INNER JOIN AD_Role_OrgAccess oa ON (r.AD_Role_ID=oa.AD_Role_ID)"
				+ " INNER JOIN AD_Client c ON (oa.AD_Client_ID=c.AD_Client_ID) "
				+ "WHERE r.AD_Role_ID=?"		//	#1
				+ " AND r.IsActive='Y' AND c.IsActive='Y'";

			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, role.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadClients - No Clients for Role=" + role.getKey());
				return null;
			}

			//  Role Info
			Env.setContext(ctx, "#AD_Role_ID", role.getKey());
			Env.setContext(ctx, "#AD_Role_Name", role.getName());
			Ini.setProperty(Ini.P_ROLE, role.getName());

			//	User Level
			Env.setContext(ctx, "#User_Level", rs.getString(1));  	//	Format 'SCO'
			//	ClientList
			Env.setContext(ctx, "#User_Client", rs.getString(2));	//	Format c1, c2, ...
			//	OrgList
			Env.setContext(ctx, "#User_Org", rs.getString(3)); 		//	Format o1, o2, ...
			//  Approval Currency / Amount
			Env.setContext(ctx, "#Approval_C_Currency_ID", rs.getInt(4));
			BigDecimal approval = rs.getBigDecimal(5);
			String approvalStr = "0";
			if (approval != null)
				approvalStr = approval.toString();
			Env.setContext(ctx, "#Approval_Amt", approvalStr);


			//  load Clients
			do
			{
				int AD_Client_ID = rs.getInt(6);
				String Name = rs.getString(7);
				KeyNamePair p = new KeyNamePair(AD_Client_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadClients", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# clients = " + retValue.length);
		return retValue;
	}   //  loadClients

	/**
	 *  Load Organizations.
	 *  <p>
	 *  Sets Client info in context and loads its organization, the role has access to
	 *
	 *  @param ctx context
	 *  @param  client    client information
	 *  @return list of valid Org KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadOrgs (Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadOrgs - required parameter missing");

		s_log.debug("loadOrgs - Client=" + client.toString());

		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadOrgs - Missing Comtext #AD_Role_ID");

		ArrayList list = new ArrayList();
		int AD_Role_ID = Env.getContextAsInt(ctx,"#AD_Role_ID");
		s_log.debug("AD_Role_ID=" + AD_Role_ID);
		//Begin e-evolution vpj-cd 05/09/2003 *************
		int AD_User_ID = Env.getContextAsInt(ctx,"#AD_User_ID");
		s_log.debug("AD_User_ID=" + AD_User_ID);
		//end e-evolution vpj-cd 05/09/2003 *************

		//	get Client details for role
		try
		{
			String sql = "SELECT c.Value,c.SMTPHost,c.IsMultiLingualDocument,c.AD_Language,"	//	1..4
				+ " o.AD_Org_ID,o.Name "				//	5..6
				+ "FROM AD_Client c"
				+ " INNER JOIN AD_Org o ON (o.AD_Client_ID=c.AD_Client_ID) "
				+ "WHERE o.AD_Client_ID=?"  			//  #1
				+ " AND o.IsSummary='N' AND o.IsActive='Y'"
				+ " AND o.AD_Org_ID IN "
				//Begin e-evolution vpj-cd 05/09/2003 *************
				//+   "(SELECT AD_Org_ID FROM AD_Role_OrgAccess WHERE AD_Role_ID=?)"; //  #2
				+ "(SELECT AD_Org_ID FROM AD_Role_OrgAccess WHERE AD_Role_ID=?)"  //  #2
				+ " AND (o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_User WHERE AD_User_ID=?) OR 0 IN (SELECT AD_Org_ID FROM AD_User WHERE AD_User_ID=?)) "; //  #3 , #4
				//end e-evolution vpj-cd 05/09/2003 *************

			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, client.getKey());
			pstmt.setInt(2, AD_Role_ID);
			//Begin e-evolution vpj-cd 05/09/2003 *************
			pstmt.setInt(3, AD_User_ID);
			pstmt.setInt(4, AD_User_ID);
			//end e-evolution vpj-cd 05/09/2003 *************
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadOrgs - No Org for Client=" + client.getKey());
				return null;
			}

			//  Client Info
			Env.setContext(ctx, "#AD_Client_ID", client.getKey());
			Env.setContext(ctx, "#AD_Client_Name", client.getName());
			Ini.setProperty(Ini.P_CLIENT, client.getName());
			//
			Env.setContext(ctx, "#Client_Value", rs.getString(1));
			Env.setContext(ctx, "#Client_SMTP", rs.getString(2));
			Env.setContext(ctx, "#IsMultiLingualDocument", rs.getString(3));

			//  load Orgs
			do
			{
				int AD_Org_ID = rs.getInt(5);
				String Name = rs.getString(6);
				KeyNamePair p = new KeyNamePair(AD_Org_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadOrgs", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# orgs = " + retValue.length);
		return retValue;
	}   //  loadOrgs

	/**
	 *	Load Preferences into Context for selected client.
	 *  <p>
	 *  Sets Org info in context and loads relevant field from
	 *	- AD_Client/Info,
	 *  - C_AcctSchema,
	 *  - C_AcctSchema_Elements
	 *	- AD_Preference
	 *  <p>
	 *  Assumes that the context is set for #AD_Client_ID, #AD_User_ID, #AD_Role_ID
	 *
	 *  @param ctx context
	 *  @param  org    org information
	 *  @param  warehouse   optional warehouse information
	 *  @param  timestamp   optional date
	 *  @param  printerName optional printer info
	 *  @returns AD_Message of error (NoValidAcctInfo) or ""
	 */
	public static String loadPreferences (Properties ctx,
		KeyNamePair org, KeyNamePair warehouse, Timestamp timestamp, String printerName)
	{
		s_log.info("loadPreferences - Org=" + org);

		if (ctx == null || org == null)
			throw new IllegalArgumentException("DB.loadPreferences - required parameter missing");
		if (Env.getContext(ctx,"#AD_Client_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Client_ID");
		if (Env.getContext(ctx,"#AD_User_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_User_ID");
		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Role_ID");


		//  Org Info - assumes that it is valid
		Env.setContext(ctx, "#AD_Org_ID", org.getKey());
		Env.setContext(ctx, "#AD_Org_Name", org.getName());
		Ini.setProperty(Ini.P_ORG, org.getName());

		//  Warehouse Info
		if (warehouse != null)
		{
			Env.setContext(ctx, "#M_Warehouse_ID", warehouse.getKey());
			Ini.setProperty(Ini.P_WAREHOUSE, warehouse.getName());
		}

		//	Date (default today)
		long today = System.currentTimeMillis();
		if (timestamp != null)
			today = timestamp.getTime();
		java.sql.Date sd = new java.sql.Date(today);
		Env.setContext(ctx, "#Date", sd.toString());	//	YYYY-MM-DD

		//	Optional Printer
		if (printerName == null)
			printerName = "";
		Env.setContext(ctx, "#Printer", printerName);
		Ini.setProperty(Ini.P_PRINTER, printerName);
		
		//	Load Role Info
		MRole.getDefault(ctx, true);	

		//	Other
		Env.setAutoCommit(ctx, Ini.getPropertyBool(Ini.P_A_COMMIT));
		if (MRole.getDefault(ctx, false).isShowAcct())
			Env.setContext(ctx, "#ShowAcct", Ini.getProperty(Ini.P_SHOW_ACCT));
		else
			Env.setContext(ctx, "#ShowAcct", "N");
		Env.setContext(ctx, "#ShowTrl", Ini.getProperty(Ini.P_SHOW_TRL));

		String retValue = "";
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		int AD_Org_ID =  org.getKey();
		int AD_User_ID =  Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID =  Env.getContextAsInt(ctx, "#AD_Role_ID");

		//	Other Settings
		Env.setContext(ctx, "#YYYY", "Y");
		Env.setContext(ctx, "#StdPrecision", 2);

		//	AccountSchema Info (first)
		String sql = "SELECT * "
			+ "FROM C_AcctSchema a, AD_ClientInfo c "
			+ "WHERE a.C_AcctSchema_ID=c.C_AcctSchema1_ID "
			+ "AND c.AD_Client_ID=?";
		try
		{
			int C_AcctSchema_ID = 0;
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				//  No Warning for System
				if (AD_Role_ID != 0)
					retValue = "NoValidAcctInfo";
			}
			else
			{
				//	Accounting Info
				C_AcctSchema_ID = rs.getInt("C_AcctSchema_ID");
				Env.setContext(ctx, "$C_AcctSchema_ID", C_AcctSchema_ID);
				Env.setContext(ctx, "$C_Currency_ID", rs.getInt("C_Currency_ID"));
				Env.setContext(ctx, "$HasAlias", rs.getString("HasAlias"));
			}
			rs.close();
			pstmt.close();

			//	Accounting Elements
			sql = "SELECT ElementType "
				+ "FROM C_AcctSchema_Element "
				+ "WHERE C_AcctSchema_ID=?"
				+ " AND IsActive='Y'";
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, C_AcctSchema_ID);
			rs = pstmt.executeQuery();
			while (rs.next())
				Env.setContext(ctx, "$Element_" + rs.getString("ElementType"), "Y");
			rs.close();
			pstmt.close();

			//	This reads all relevant window neutral defaults
			//	overwriting superseeded ones.  Window specific is read in Mainain
			sql = "SELECT Attribute, Value, AD_Window_ID "
				+ "FROM AD_Preference "
				+ "WHERE AD_Client_ID IN (0, @#AD_Client_ID@)"
				+ " AND AD_Org_ID IN (0, @#AD_Org_ID@)"
				+ " AND (AD_User_ID IS NULL OR AD_User_ID=0 OR AD_User_ID=@#AD_User_ID@)"
				+ " AND IsActive='Y' "
				+ "ORDER BY Attribute, AD_Client_ID, AD_User_ID DESC, AD_Org_ID";
				//	the last one overwrites - System - Client - User - Org - Window
			sql = Env.parseContext(ctx, 0, sql, false);
			if (sql.length() == 0)
				s_log.error("loadPreferences - Missing Environment");
			else
			{
				pstmt = prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					int AD_Window_ID = rs.getInt(3);
					String at = "";
					if (rs.wasNull())
						at = "P|" + rs.getString(1);
					else
						at = "P" + AD_Window_ID + "|" + rs.getString(1);
					String va = rs.getString(2);
					Env.setContext(ctx, at, va);
				}
				rs.close();
				pstmt.close();
			}
		}
		catch (SQLException ex)
		{
			s_log.error("loadPreferences (" + sql + ")", ex);
		}

		//	Default Values
		s_log.info("Default Values ...");
		sql = "SELECT t.TableName, c.ColumnName "
			+ "FROM AD_Column c "
			+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID) "
			+ "WHERE c.IsKey='Y' AND t.IsActive='Y'"
			+ " AND EXISTS (SELECT * FROM AD_Column cc "
			+ " WHERE ColumnName = 'IsDefault' AND t.AD_Table_ID=cc.AD_Table_ID AND cc.IsActive='Y')";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				loadDefault (ctx, rs.getString(1), rs.getString(2));
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("loadPreferences", e);
		}
		//
		Ini.saveProperties(Ini.isClient());
		//
		return retValue;
	}	//	loadPreferences

	/**
	 *	Load Default Value for Table into Context.
	 *
	 *  @param ctx context
	 *  @param TableName table name
	 *  @param ColumnName column name
	 */
	private static void loadDefault (Properties ctx, String TableName, String ColumnName)
	{
		String value = null;
		//
		String sql = "SELECT " + ColumnName + " FROM " + TableName
			+ " WHERE IsDefault='Y' AND IsActive='Y' ORDER BY AD_Client_ID";
		sql = MRole.getDefault(ctx, false).addAccessSQL(sql, 
			TableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())		//	overwrites system defaults
				value = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("loadDefault - " + TableName + " (" + sql + ")", e);
			return;
		}
		//	Set Context Value
		if (value != null && value.length() != 0)
		{
			if (TableName.equals("C_DocType"))
				Env.setContext(ctx, "#C_DocTypeTarget_ID", value);
			else
				Env.setContext(ctx, "#" + ColumnName, value);
		}
	}	//	loadDefault

	/**
	 *  Load Warehouses
	 *
	 * @param ctx context
	 * @param client client
	 * @return Array of Warehouse Info
	 */
	public static KeyNamePair[] loadWarehouses(Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadWarehouses - required parameter missing");

		s_log.info("loadWarehouses - Client=" + client.toString());
		//Begin e-evolution vpj-cd 05/09/2003 *************
		int AD_User_ID = Env.getContextAsInt(ctx,"#AD_User_ID");
		s_log.debug("AD_User_ID=" + AD_User_ID);
		//end e-evolution vpj-cd 05/09/2003 *************

		ArrayList list = new ArrayList();
		try
		{
			String sql = "SELECT M_Warehouse_ID, Name FROM M_Warehouse "
				//Begin e-evolution vpj-cd 05/09/2003 *************
				//	+ "WHERE AD_Client_ID=? AND IsActive='Y'";  //  #1
				+ "WHERE AD_Client_ID=? AND IsActive='Y' "
				+ " AND (AD_Org_ID IN (SELECT AD_Org_ID FROM AD_User WHERE AD_User_ID=?) OR 0 IN (SELECT AD_Org_ID FROM AD_User WHERE AD_User_ID=?))"; //#2,#3
				//end e-evolution vpj-cd 05/09/2003 *************
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, client.getKey());
			//Begin e-evolution vpj-cd 05/09/2003 *************
			pstmt.setInt(2, AD_User_ID);
			pstmt.setInt(3, AD_User_ID);
			//end e-evolution vpj-cd 05/09/2003 *************
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.warn("loadWarehouses - No Warehouses for Client=" + client.getKey());
				return null;
			}

			//  load Warehousess
			do
			{
				int AD_Warehouse_ID = rs.getInt(1);
				String Name = rs.getString(2);
				KeyNamePair p = new KeyNamePair(AD_Warehouse_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadWarehouses", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.info("# warehouses = " + retValue.length);
		return retValue;
	}   //  loadWarehouses

	/*************************************************************************/

	/**
	 *  Set connection
	 *  @param cc connection
	 */
	public static void setDBTarget (CConnection cc)
	{
		if (cc == null)
			throw new IllegalArgumentException("DB.setDBTarget connection is NULL");

		if (s_cc == null)
			s_cc = cc;
		synchronized (s_cc)    //  use as mutex
		{
			s_cc = cc;
			//  Closing existing
			if (s_connections != null)
			{
				for (int i = 0; i < s_connections.length; i++)
				{
					try {
						s_connections[i].close();
					} catch (Exception e) {}
				}
			}
			s_connections = null;
		}
	}   //  setDBTarget

	/**
	 *  Is there a connection to the database ?
	 *  @return true, if connected to database
	 */
	public static boolean isConnected()
	{
		try
		{
			getConnectionRW();	//	try to get a connection
			return true;
		}
		catch (Exception e)
		{
		}
		return false;
	}   //  isConnected

	/**
	 *	Return (pooled) r/w AutoCommit, read committed connection
	 *
	 *  @return Connection (r/w)
	 */
	public static Connection getConnectionRW ()
	{
		//	check health of connection
		try
		{
			if (s_connectionRW == null)
				;
			else if (s_connectionRW.isClosed())
				s_connectionRW = null;
			else if (s_connectionRW instanceof OracleConnection && ((OracleConnection)s_connectionRW).pingDatabase(1) < 0)
				s_connectionRW = null;
			else
				 s_connectionRW.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		catch (Exception e)
		{
			s_connectionRW = null;
		}
		if (s_connectionRW == null)
			s_connectionRW = s_cc.getConnection (true, Connection.TRANSACTION_READ_COMMITTED);
		if (s_connectionRW == null)
			throw new UnsupportedOperationException("DB.getConnectionRW - @NoDBConnection@");
		return s_connectionRW;
	}   //  getConnectionRW

	/**
	 *	Return read committed, read/only connection with AutoCommit from pool
	 *  @return Connection (r/o)
	 */
	public static Connection getConnectionRO ()
	{
		try
		{
			synchronized (s_cc)    //  use as mutex as s_connection is null the first time
			{
				if (s_connections == null)
					s_connections = createConnections (Connection.TRANSACTION_READ_COMMITTED);     //  see below
			}
		}
		catch (Exception e)
		{
			s_log.error("getConnectionRO", e);
		}

		//  check health of connection
		int pos = s_conCount++;
		Connection connection = s_connections[pos % s_conCacheSize];
		try
		{
			if (connection == null)
				;
			else if (connection.isClosed())
				connection = null;
			else if (connection instanceof OracleConnection && ((OracleConnection)connection).pingDatabase(1) < 0)
				connection = null;
			else
				 connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		catch (Exception e)
		{
			connection = null;
		}
		//	Get new
		if (connection == null)
		{
			System.out.println("DB.getConnectionRO - replacing connection #" + pos % s_conCacheSize);
		//	s_log.error("getConnectionRO - replacing connection #" + pos % s_conCacheSize);
			connection = s_cc.getConnection (true, Connection.TRANSACTION_READ_COMMITTED);            //  see above
		/*	try
			{
				retValue.setReadOnly(true);     //  not supported by Oracle
			}
			catch (Exception e)
			{
				System.err.println("DB.getConnectionRO - Cannot set to R/O - " + e);
			}   */
			s_connections[pos % s_conCacheSize] = connection;
		}
		if (connection == null)
			throw new UnsupportedOperationException("DB.getConnectionRO - @NoDBConnection@");
		return connection;
	}	//	getConnectionRO

	/**
	 *	Create new Connection.
	 *  The connection must be closed explicitly by the application
	 *
	 *  @param autoCommit auto commit
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Connection connection
	 */
	public static Connection createConnection (boolean autoCommit, int trxLevel)
	{
		s_log.debug("createConnection " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", AutoCommit=" + autoCommit + ", TrxLevel=" + trxLevel);
		return s_cc.getConnection (autoCommit, trxLevel);
	}	//	createConnection

	/**
	 *	Create new set of r/o Connections.
	 *  R/O connection might not be supported by DB
	 *
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Array of Connections (size based on s_conCacheSize)
	 */
	private static Connection[] createConnections (int trxLevel)
	{
		s_log.debug("createConnections (" + s_conCacheSize + ") " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", TrxLevel=" + trxLevel);
		Connection cons[] = new Connection[s_conCacheSize];
		try
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				cons[i] = s_cc.getConnection (true, trxLevel);  //  auto commit
				if (cons[i] == null)
					System.err.println("createConnections - connection is NULL");	//	don't use log
			}
		}
		catch (Exception e)
		{
			//  Don't use Log
			System.err.println("DB.createConnections - " + e.getMessage());
		}
		return cons;
	}	//	createConnections

	/**
	 *  Get Database Driver.
	 *  Access to database specific functionality.
	 *  @return Compiere Database Driver
	 */
	public static CompiereDatabase getDatabase()
	{
		if (s_cc != null)
			return s_cc.getDatabase();
		return null;
	}   //  getDatabase

	/*************************************************************************/

	/**
	 *  Check database Version with Code version
	 *  @param ctx context
	 *  @return true if Database version (date) is the same
	 */
	public static boolean isDatabaseOK (Properties ctx)
	{
		//  Check Version
		String version = "?";
		String sql = "SELECT Version FROM AD_System";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				version = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("Problem with AD_System Table - Run system.sql script - " + e.toString());
			return false;
		}
		s_log.info("isDatabaseOK - DB_Version=" + version);
		//  Identical DB version
		if (Compiere.DB_VERSION.equals(version))
			return true;

		String AD_Message = "DatabaseVersionError";
		String title = org.compiere.Compiere.getName() + " " +  Msg.getMsg(ctx, AD_Message, true);
		//	Code assumes Database version {0}, but Database has Version {1}.
		String msg = Msg.getMsg(ctx, AD_Message);	//	complete message
		msg = MessageFormat.format(msg, new Object[] {Compiere.DB_VERSION, version});
		Object[] options = { UIManager.get("OptionPane.noButtonText"), "Migrate" };
		int no = JOptionPane.showOptionDialog (null, msg,
			title, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
			UIManager.getIcon("OptionPane.errorIcon"), options, options[0]);
		if (no == 1)
		{
			try
			{
				Class.forName("com.compiere.client.StartMaintain").newInstance();
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog (null,
					ex.getMessage() + "\nSee: http://www.compiere.com/maintain",
					title, JOptionPane.ERROR_MESSAGE);
				s_log.error("isDatabaseOK - " + ex.getMessage());
			}
		}
		return false;
	}   //  isDatabaseOK

	/*************************************************************************/

	/**
	 *	Secure Context Login.
	 *  @param uid uid
	 *  @param pwd pwd
	 *  @param role role
	 *  @return true if connected
	 */
	public static boolean login_context (String uid, String pwd, String role)
	{
		if (uid == null || pwd == null || role == null || uid.length() == 0 || pwd.length() == 0 || role.length() == 0)
			throw new IllegalArgumentException("DB.login_context - required parameter missing");
		s_log.info("login_context uid=" + uid);
		if (uid == null || pwd == null || role == null)
			return false;
		//
		String SQL = "{CALL Compiere_Context.Login(?,?,?,?)}";
		boolean result = true;
		try
		{
			CallableStatement cstmt = getConnectionRO().prepareCall(SQL);
			cstmt.setString(1, uid);
			cstmt.setString(2, pwd);
			cstmt.setString(3, role);
			cstmt.setString(4, Language.getBaseAD_Language());
			result = cstmt.execute();
			cstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("login_context", e);
			result = false;
		}
		return result;
	}	//	login_context

	/*************************************************************************/

	/**
	 *	Close Target
	 */
	public static void closeTarget()
	{
		if (s_connections != null)
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				try
				{
					if (s_connections[i] != null)
						s_connections[i].close();
				}
				catch (SQLException e)
				{
					s_log.warn("close connection #" + i + " - " + e.getMessage());
				}
				s_connections[i] = null;
			}
			try
			{
				if (s_connectionRW != null)
					s_connectionRW.close();
			}
			catch (SQLException e)
			{
				s_log.error("close R/W connection", e);
			}
			s_connectionRW = null;
		}
		s_connections = null;
//		EJB.close();
	}	//	closeTarget

	/*************************************************************************/

	/**
	 *	Prepare Forward Read Only Call
	 *  @param RO_SQL sql (RO)
	 *  @return Callable Statement
	 */
	public static CallableStatement prepareCall(String RO_SQL)
	{
		if (RO_SQL == null || RO_SQL.length() == 0)
			throw new IllegalArgumentException("DB.prepareCall - required parameter missing - " + RO_SQL);
		//
		String sql = getDatabase().convertStatement(RO_SQL);
		try
		{
			return getConnectionRO().prepareCall
				(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		}
		catch (SQLException e)
		{
			s_log.error("prepareCall (" + sql + ")", e);
		}
		return null;
	}	//	prepareCall

	/*************************************************************************/

	/**
	 *	Prepare Read Only Statement
	 *  @param RO_SQL sql (RO)
	 *  @return Prepared Statement
	 */
	public static PreparedStatement prepareStatement(String RO_SQL)
	{
		return prepareStatement(RO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}	//	prepareStatement

	/**
	 *	Prepare Statement
	 *
	 *  @param SQL sql statement
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Prepared Statement r/o or r/w depending on concur
	 */
	public static PreparedStatement prepareStatement(String SQL, int resultSetType, int resultSetConcurrency)
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.prepareStatement - required parameter missing - " + SQL);
		//
		return new CompiereStatement(SQL, resultSetType, resultSetConcurrency);
		/**
		try
		{
			Connection conn = null;
			if (resultSetConcurrency == ResultSet.CONCUR_UPDATABLE)
				conn = getConnectionRW();
			else
				conn = getConnectionRO();
			return conn.prepareStatement (sql, resultSetType, resultSetConcurrency);
		}
		catch (SQLException e)
		{
			s_log.error("prepareStatement (" + sql + ")", e);
		}
		return null;
		**/
	}	//	prepareStatement

	/**
	 *	Create Read Only Statement
	 *  @return Statement
	 */
	public static Statement createStatement()
	{
		return createStatement (ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}	//	createStatement

	/**
	 *	Create Statement
	 *
	 *  @param type - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param concur - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Statement - either r/w ir r/o depending on concur
	 */
	public static Statement createStatement(int type, int concur)
	{
		try
		{
			Connection conn = null;
			if (concur == ResultSet.CONCUR_UPDATABLE)
				conn = getConnectionRW();
			else
				conn = getConnectionRO();
			return conn.createStatement(type, concur);
		}
		catch (SQLException e)
		{
			s_log.error("createStatement", e);
		}
		return null;
	}	//	createStatement

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param SQL sql
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String SQL)
	{
		return executeUpdate(SQL, false);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param SQL sql
	 * 	@param ignoreError if true, no execution error is reported
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String SQL, boolean ignoreError)
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.executeUpdate - required parameter missing - " + SQL);
		//
		String sql = getDatabase().convertStatement(SQL);
		int no = -1;
		Statement stmt = null;
		try
		{
			Connection conn = getConnectionRW();
			stmt = conn.createStatement();
			no = stmt.executeUpdate(sql);
		}
		catch (SQLException e)
		{
			if (!ignoreError)
			{
				s_log.error ("executeUpdate - " + sql, e);
				Log.saveError ("DBExecuteError", e.getLocalizedMessage ());
			}
		}
		finally
		{
			//  Always close cursor
			try
			{
				stmt.close();
			}
			catch (SQLException e2)
			{
				s_log.error("executeUpdate - cannot close statement");
			}
		}
		return no;
	}	//	executeUpdate

	/**
	 *	Execute Update and throw exxeption.
	 *  @param SQL sql
	 *  @return number of rows updated or -1 if error
	 * 	@throws SQLException
	 */
	public static int executeUpdateEx (String SQL) throws SQLException
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.executeUpdate - required parameter missing - " + SQL);
		//
		String sql = getDatabase().convertStatement(SQL);
		int no = -1;
		SQLException ex = null;
		Statement stmt = null;
		try
		{
			Connection conn = getConnectionRW();
			stmt = conn.createStatement();
			no = stmt.executeUpdate(sql);
		}
		catch (SQLException e)
		{
			ex = e;
		}
		finally
		{
			//  Always close cursor
			try
			{
				stmt.close();
			}
			catch (SQLException e2)
			{
				s_log.error("executeUpdate - cannot close statement");
			}
		}
		if (ex != null)
			throw new SQLException(ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
		return no;
	}	//	execute Update

	/**
	 *	Commit - commit on RW connection.
	 *  Is not required as RW connection is AutoCommit
	 *  @param throwException if true, re-throws exception
	 *  @return true if not needed or success
	 *  @throws SQLException
	 */
	public static boolean commit (boolean throwException) throws SQLException
	{
		try
		{
			Connection conn = getConnectionRW();
		//	if (!conn.getAutoCommit())
				conn.commit();
		}
		catch (SQLException e)
		{
			s_log.error("commit", e);
			if (throwException)
				throw e;
			return false;
		}
		return true;
	}	//	commit


	/**
	 * 	Get Row Set
	 *	@param sql sql
	 *	@return row set or null
	 */
	public static RowSet getRowSet (String sql)
	{
		RowSet retValue = null;
		CompiereStatementVO info = new CompiereStatementVO(sql, RowSet.TYPE_SCROLL_INSENSITIVE, RowSet.CONCUR_READ_ONLY);
		CompiereStatement stmt = new CompiereStatement(info);
		retValue = stmt.remote_getRowSet();
		return retValue;
	}	//	getRowSet

	/*************************************************************************

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @return next no
	 */
	public static int getKeyNextNo (Properties ctx, int WindowNo, String TableName)
	{
		if (ctx == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getKeyNextNo - required parameter missing");
		int AD_Client_ID = Env.getAD_Client_ID(ctx);
		//
		return getKeyNextNo (AD_Client_ID, TableName);
	}   //  getKeyNextNo

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param AD_Client_ID client
	 *  @param TableName table name
	 *  @return next no
	 */
	public static int getKeyNextNo (int AD_Client_ID, String TableName)
	{
		if (TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getKeyNextNo - required parameter missing");
		int retValue = 0;

		//	Check CompiereSys
		boolean compiereSys = Ini.getPropertyBool(Ini.P_COMPIERESYS);
		if (!compiereSys && AD_Client_ID < 1000000)
			AD_Client_ID = 1000000;
		//
		try
		{
			String SQL = "{CALL AD_Sequence_Next(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setString(1, TableName);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.NUMERIC);
			cstmt.executeUpdate();
			retValue = cstmt.getInt(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getKeyNextNo - Table=" + TableName + ")", e);
		}
		return retValue;
	}	//	getKeyNextNo

	/**
	 *	Get Document Number for current document.
	 *  <br>
	 *  - first search for DocType based Document No
	 *  - then Search for DocumentNo based on TableName
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @param onlyDocType Do not search for document no based on TableName
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (Properties ctx, int WindowNo, String TableName, boolean onlyDocType)
	{
		if (ctx == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getDocumentNo - required parameter missing");
		String CompiereSys = Env.getContext(ctx, "#CompiereSys");
		int AD_Client_ID = Env.getContextAsInt(ctx, WindowNo, "AD_Client_ID");

		//	Get C_DocType_ID from context - NO Defaults -
		int C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID");
		if (C_DocType_ID == 0)
			C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID");
		if (C_DocType_ID == 0)
		{
			s_log.debug("getDocumentNo - for Window=" + WindowNo
				+ " - Target=" + Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID") + "/" + Env.getContextAsInt(ctx, WindowNo, "C_DocTypeTarget_ID")
				+ " - Actual=" + Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID") + "/" + Env.getContextAsInt(ctx, WindowNo, "C_DocType_ID"));
			return getDocumentNo (AD_Client_ID, CompiereSys, TableName);
		}

		//	Check CompiereSys
		if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
			throw new UnsupportedOperationException("DB.getDocumentNo - Cannot add System records");
		//
		String retValue = getDocumentNo(AD_Client_ID, C_DocType_ID);
		if (!onlyDocType && retValue == null)
			return getDocumentNo (AD_Client_ID, CompiereSys, TableName);
		return retValue;
	}	//	getDocumentNo

	/**
	 *	Get Document Number for current document.
	 *  @param AD_Client_ID client
	 *  @param C_DocType_ID (target) document type
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (int AD_Client_ID, int C_DocType_ID)
	{
		String retValue = null;
		try
		{
			String SQL = "{CALL AD_Sequence_DocType(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setInt(1, C_DocType_ID);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.VARCHAR);
			cstmt.executeUpdate();
			retValue = cstmt.getString(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getDocumentNo - DocType=" + C_DocType_ID, e);
		}
		s_log.info("getDocumentNo - DocType=" + C_DocType_ID + " -> " + retValue);
		return retValue;
	}	//	getDocumentNo


	/**
	 *  Get Next Document No
	 *  @param AD_Client_ID client
	 *  @param CompiereSys system
	 *  @param TableName table name
	 *  @return DocumentNo
	 */
	public static String getDocumentNo (int AD_Client_ID, String CompiereSys, String TableName)
	{
		if (CompiereSys == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getDocumentNo - required parameter missing");
		//	Check CompiereSys
	//	if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
	//		throw new UnsupportedOperationException("DB.getDocumentNo - Cannot add System records");
		//
		String retValue = null;
		try
		{
			String SQL = "{CALL AD_Sequence_Doc(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setString(1, "DocumentNo_" + TableName);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.VARCHAR);
			cstmt.executeUpdate();
			retValue = cstmt.getString(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getDocumentNo  TableName=" + TableName, e);
		}
		s_log.info("getDocumentNo - TableName=" + TableName + " -> " + retValue);
		return retValue;
	}   //  getDocumentNo


	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int int_param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, String int_param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setString(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@param s_param2 parameter 2
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int int_param1, String s_param2)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			pstmt.setString(2, s_param2);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value: " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Array of Key Name Pairs
	 *	@param sql select with id and name as first and second column
	 *	@return array of key name pairs
	 */
	public static KeyNamePair[] getKeyNamePairs(String sql)
	{
		PreparedStatement pstmt = null;
		ArrayList list = new ArrayList();
		try
		{
			pstmt = DB.prepareCall(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new KeyNamePair(rs.getInt(1), rs.getString(2)));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			Log.error("getKeyNamePairs " + sql, e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
	//	s_log.debug("getKeyNamePairs #" + retValue.length);
		return retValue;		
	}	//	getKeyNamePairs

	/**
	 *  Convert an amount with today's spot rate
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  Amt         The amount to be converted
	 * 	@param	AD_Client_ID client
	 * 	@param	AD_Org_ID	organization
	 *  @return converted amount
	 */
	public static BigDecimal getConvertedAmt (BigDecimal Amt, int CurFrom_ID, int CurTo_ID, int AD_Client_ID, int AD_Org_ID)
	{
		return getConvertedAmt (Amt, CurFrom_ID, CurTo_ID, null, null, AD_Client_ID, AD_Org_ID);
	}   //  getConvertedAmt

	/**
	 *	Convert an amount
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  ConvDate    The Conversion date - if null - use current date
	 *  @param  RateType    The Conversion rate type - if null/empty - use Spot
	 *  @param  Amt         The amount to be converted
	 * 	@param	AD_Client_ID client
	 * 	@param	AD_Org_ID	organization
	 *  @return converted amount
	 */
	public static BigDecimal getConvertedAmt (BigDecimal Amt, int CurFrom_ID, int CurTo_ID,
		Timestamp ConvDate, String RateType, int AD_Client_ID, int AD_Org_ID)
	{
		if (Amt == null)
			throw new IllegalArgumentException("DB.getConvertedAmt - required parameter missing - Amt");
		BigDecimal retValue = null;
		if (CurFrom_ID == CurTo_ID || Amt.equals(Env.ZERO))
			return Amt;
		//
		try
		{
			String sql = "{? = call C_Currency_Convert(?,?,?,?,?, ?,?) }";
			CallableStatement cstmt = prepareCall(sql);
			//
			cstmt.registerOutParameter(1, Types.NUMERIC);
			//
			cstmt.setBigDecimal(2, Amt);					//	Amount		IN  	NUMBER
			cstmt.setInt(3, CurFrom_ID);             		//	CurFrom		IN 		NUMBER
			cstmt.setInt(4, CurTo_ID);                  	//	CurTo		IN 		NUMBER
			if (ConvDate == null)
				ConvDate = new Timestamp (System.currentTimeMillis());
			cstmt.setTimestamp(5, ConvDate);				//	ConvDate	IN 		DATE
			if (RateType == null || RateType.equals(""))
				RateType = "S";
			cstmt.setString(6, RateType);					//	RateType	IN 		CHAR
			cstmt.setInt(7, AD_Client_ID);
			cstmt.setInt(8, AD_Org_ID);
			//
			cstmt.executeUpdate();
			retValue = cstmt.getBigDecimal(1);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getConvertedAmt", e);
		}
		if (retValue == null)
			s_log.info("getConvertedAmt - not found - CurFrom=" + CurFrom_ID + ", CurTo=" + CurTo_ID
				+ ", " + ConvDate + ", " + RateType + ", Client=" + AD_Client_ID + ", Org=" + AD_Org_ID);
		return retValue;
	}	//	getConvertedAmt

	/**
	 *	Get Currency Rate
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  ConvDate    The Conversion date - if null - use current date
	 *  @param  RateType    The Conversion rate type - if null/empty - use Spot
	 * 	@param	AD_Client_ID client
	 * 	@param	AD_Org_ID	organization
	 *  @return currency Rate or null
	 */
	public static BigDecimal getConvesionRate (int CurFrom_ID, int CurTo_ID,
		Timestamp ConvDate, String RateType, int AD_Client_ID, int AD_Org_ID)
	{
		BigDecimal retValue = null;
		if (CurFrom_ID == CurTo_ID)
			return new BigDecimal(1);
		//
		try
		{
			String sql = "{? = call C_Currency_Rate(?,?,?,?,?,?)}";
			CallableStatement cstmt = prepareCall(sql);
			//
			cstmt.registerOutParameter(1, Types.NUMERIC);
			//
			cstmt.setInt(2, CurFrom_ID);             		//	CurFrom		IN 		NUMBER
			cstmt.setInt(3, CurTo_ID);                  	//	CurTo		IN 		NUMBER
			if (ConvDate == null)
				ConvDate = new Timestamp (System.currentTimeMillis());
			cstmt.setTimestamp(4, ConvDate);				//	ConvDate	IN 		DATE
			if (RateType == null || RateType.equals(""))
				RateType = "S";
			cstmt.setString(5, RateType);					//	RateType	IN 		CHAR
			cstmt.setInt(6, AD_Client_ID);
			cstmt.setInt(7, AD_Org_ID);
			//
			cstmt.executeUpdate();
			retValue = cstmt.getBigDecimal(1);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getConvesionRate", e);
		}
		if (retValue == null)
			s_log.info ("getConversionRate - not found - CurFrom=" + CurFrom_ID + ", CurTo=" + CurTo_ID
			  + ", " + ConvDate + ", " + RateType + ", Client=" + AD_Client_ID + ", Org=" + AD_Org_ID);
		return retValue;
	}	//	getConvesionRate


	/**
	 *  Get fully qualified Account Combination
	 *
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param C_AcctSchema_ID acct schema
	 *  @param Account_ID natural account
	 *  @param  base_ValidCombination_ID optional base combination to be specified
	 *  @param Alias aloas
	 *  @param AD_User_ID user
	 *  @param M_Product_ID product
	 *  @param C_BPartner_ID partner
	 *  @param AD_OrgTrx_ID trx org
	 *  @param C_LocFrom_ID loc from
	 *  @param C_LocTo_ID loc to
	 *  @param C_SRegion_ID sales region
	 *  @param C_Project_ID project
	 *  @param C_Campaign_ID campaign
	 *  @param C_Activity_ID activity
	 *  @param User1_ID user1
	 *  @param User2_ID user2
	 *  @return C_ValidCombination_ID of existing or new Combination
	 */
	public static int getValidCombination (int AD_Client_ID, int AD_Org_ID,
		int C_AcctSchema_ID, int Account_ID, int base_ValidCombination_ID, String Alias, int AD_User_ID,
		int M_Product_ID, int C_BPartner_ID, int AD_OrgTrx_ID,
		int C_LocFrom_ID, int C_LocTo_ID, int C_SRegion_ID, int C_Project_ID,
		int C_Campaign_ID, int C_Activity_ID, int User1_ID, int User2_ID)
	{
		int retValue = 0;
		StringBuffer sb = new StringBuffer ("C_ValidCombination_Get[");
		try
		{
			String sql = "{CALL C_ValidCombination_Get(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)}";
			CallableStatement cstmt = prepareCall(sql);
			//  1 - ID
			cstmt.registerOutParameter(1, Types.NUMERIC);
			sb.append("v,");
			//  --  Mandatory Accounting fields
			//	2 - AD_Client_ID
			cstmt.setInt(2, AD_Client_ID);
			sb.append("AD_Client_ID=").append(AD_Client_ID).append(",");
			//	3 - AD_Org_ID
			cstmt.setInt(3, AD_Org_ID);
			sb.append("AD_Org_ID=").append(AD_Org_ID).append(",");
			//  4- C_AcctSchema_ID
			cstmt.setInt(4, C_AcctSchema_ID);
			sb.append("C_AcctSchema_ID=").append(C_AcctSchema_ID).append(",");
			//  5 - Account_ID
			cstmt.setInt(5, Account_ID);
			sb.append("Account_ID=").append(Account_ID).append(", ");

			//  --  Optional
			//  6 - Base C_ValidCombination_ID
			if (base_ValidCombination_ID != 0)
			{
				cstmt.setInt (6, base_ValidCombination_ID);
				sb.append("BaseValidCombination_ID=").append(base_ValidCombination_ID).append(",");
			}
			else
			{
				cstmt.setNull(6, Types.NUMERIC);
				sb.append("BaseValidCombination_ID=").append("NULL,");
			}
			//  7 - MustBeFullyQualified
			cstmt.setString(7, "Y");
			sb.append("MustBeFullyQualified='Y',");
			//  8 - Alias
			if (Alias != null && Alias.length() > 0)
			{
				cstmt.setString(8, Alias);
				sb.append("Alias='").append(Alias).append("',");
			}
			else
			{
				cstmt.setNull(8, Types.VARCHAR);
				sb.append("Alias=NULL,");
			}
			//  9 - CreatedBy
			cstmt.setInt(9, AD_User_ID);
			sb.append("AD_User_ID=").append(AD_User_ID).append(", ");

			//	--  Optional Accounting fields
			//  10 - M_Product_ID
			if (M_Product_ID != 0)
			{
				cstmt.setInt(10, M_Product_ID);
				sb.append("M_Product_ID=").append(M_Product_ID).append(",");
			}
			else
			{
				cstmt.setNull(10, Types.NUMERIC);
				sb.append("M_Product_ID=NULL,");
			}
			//  11 - C_BPartner_ID
			if (C_BPartner_ID != 0)
			{
				cstmt.setInt(11, C_BPartner_ID);
				sb.append("C_BPartner_ID=").append(C_BPartner_ID).append(",");
			}
			else
			{
				cstmt.setNull(11, Types.NUMERIC);
				sb.append("C_BPartner_ID=NULL,");
			}
			//  12 - AD_OrgTrx_ID
			if (AD_OrgTrx_ID != 0)
			{
				cstmt.setInt(12, AD_OrgTrx_ID);
				sb.append("AD_OrgTrx_ID=").append(AD_OrgTrx_ID).append(",");
			}
			else
			{
				cstmt.setNull(12, Types.NUMERIC);
				sb.append("AD_OrgTrx_ID=NULL,");
			}
			//  13 - C_LocFrom_ID
			if (C_LocFrom_ID != 0)
			{
				cstmt.setInt(13, C_LocFrom_ID);
				sb.append("C_LocFrom_ID=").append(C_LocFrom_ID).append(",");
			}
			else
			{
				cstmt.setNull(13, Types.NUMERIC);
				sb.append("C_LocFrom=NULL,");
			}
			//  14 - C_LocTo_ID
			if (C_LocTo_ID != 0)
			{
				cstmt.setInt(14, (C_LocTo_ID));
				sb.append("C_LocTo_ID=").append(C_LocTo_ID).append(", ");
			}
			else
			{
				cstmt.setNull(14, Types.NUMERIC);
				sb.append("C_LocTo_ID=NULL, ");
			}
			//  15 - C_SalesRegion_ID
			if (C_SRegion_ID != 0)
			{
				cstmt.setInt(15, (C_SRegion_ID));
				sb.append("C_SalesRegion_ID=").append(C_SRegion_ID).append(",");
			}
			else
			{
				cstmt.setNull(15, Types.NUMERIC);
				sb.append("C_SalesRegion_ID=NULL,");
			}
			//  16 - C_Project_ID
			if (C_Project_ID != 0)
			{
				cstmt.setInt(16, (C_Project_ID));
				sb.append("C_Project_ID=").append(C_Project_ID).append(",");
			}
			else
			{
				cstmt.setNull(16, Types.NUMERIC);
				sb.append("C_Project_ID=NULL,");
			}
			//  17 - C_Campaign_ID
			if (C_Campaign_ID != 0)
			{
				cstmt.setInt(17, (C_Campaign_ID));
				sb.append("C_Campaign_ID=").append(C_Campaign_ID).append(",");
			}
			else
			{
				cstmt.setNull(17, Types.NUMERIC);
				sb.append("C_Campaign_ID=NULL,");
			}
			//  18 - C_Activity_ID
			if (C_Activity_ID != 0)
			{
				cstmt.setInt(18, (C_Activity_ID));
				sb.append("C_Activity_ID=").append(C_Activity_ID).append(",");
			}
			else
			{
				cstmt.setNull(18, Types.NUMERIC);
				sb.append("C_Activity_ID=NULL,");
			}
			//  19 - User1_ID
			if (User1_ID != 0)
			{
				cstmt.setInt(19, (User1_ID));
				sb.append("User1_ID=").append(User1_ID).append(",");
			}
			else
			{
				cstmt.setNull(19, Types.NUMERIC);
				sb.append("User1_ID=NULL,");
			}
			//  20 - User2_ID
			if (User2_ID != 0)
			{
				cstmt.setInt(20, (User2_ID));
				sb.append("User2_ID=").append(User2_ID).append(")");
			}
			else
			{
				cstmt.setNull(20, Types.NUMERIC);
				sb.append("User2_ID=NULL)");
			}

			//
			cstmt.executeUpdate();
			retValue = cstmt.getInt(1);     //  1 - ID
			cstmt.close();
			s_log.debug("getValidCombination " + sb.toString());
		}
		catch(SQLException e)
		{
			s_log.error("getValidCombination " + sb.toString(), e);
		}
		return retValue;
	}   //  getValidCombination

	/**
	 *  Insert Note
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param AD_User_ID user
	 *  @param AD_Table_ID table
	 *  @param Record_ID record
	 *  @param AD_MessageValue message
	 *  @param Text text
	 *  @param Reference subject
	 *  @return true if note was inserted
	 */
	public static boolean insertNote (int AD_Client_ID, int AD_Org_ID, int AD_User_ID,
		int AD_Table_ID, int Record_ID,
		String AD_MessageValue, String Text, String Reference)
	{
		if (AD_MessageValue == null || AD_MessageValue.length() == 0)
			throw new IllegalArgumentException("DB.insertNote - required parameter missing - AD_Message");

		//  Database limits
		if (Text == null)
			Text = "";
		if (Reference == null)
			Reference = "";
		//
		s_log.info("insertNote - " + AD_MessageValue + " - " + Reference);
		//
		StringBuffer sql = new StringBuffer ("INSERT INTO AD_Note (AD_Note_ID,");
		sql.append("AD_Client_ID,AD_Org_ID,IsActive, Created,CreatedBy,Updated,UpdatedBy, ")
			.append("AD_Message_ID,Text,Reference, ")
			.append("AD_User_ID,AD_Table_ID,Record_ID,Processed) ")
			.append("SELECT ");
		//
		String CompiereSys = "N";
		int AD_Note_ID = getKeyNextNo(AD_Client_ID, "AD_Note");
		sql.append(AD_Note_ID).append(",");
		//
		sql.append(AD_Client_ID).append(",")
			.append(AD_Org_ID).append(", 'Y',SysDate,")
			.append(AD_User_ID).append(",SysDate,0,");
		//	AD_Message_ID,Text,Reference,
		sql.append(" AD_Message_ID,").append(DB.TO_STRING(Text, 2000)).append(",")
			.append(DB.TO_STRING(Reference, 60)).append(", ");
		//	AD_User_ID,AD_Table_ID,Record_ID,Processed
		sql.append(AD_User_ID).append(",").append(AD_Table_ID).append(",")
			.append(Record_ID).append(",'N' ");
		//
		sql.append("FROM AD_Message WHERE Value='").append(AD_MessageValue).append("'");
		//  Create Entry
		int no = executeUpdate(sql.toString());

		//  AD_Message must exist, so if not created, it is probably
		//  due to non-existing AD_Message
		if (no == 0)
		{
			sql = new StringBuffer ("INSERT INTO AD_Note (AD_Note_ID,");
			sql.append("AD_Client_ID,AD_Org_ID,IsActive, Created,CreatedBy,Updated,UpdatedBy, ")
				.append("AD_Message_ID,Text,Reference, ")
				.append("AD_User_ID,AD_Table_ID,Record_ID,Processed) ")
				.append("SELECT ");
			//  - use AD_Note_ID from above
			sql.append(AD_Note_ID).append(",");
			//
			sql.append(AD_Client_ID).append(",")
				.append(AD_Org_ID).append(", 'Y',SysDate,")
				.append(AD_User_ID).append(",SysDate,0, ");
			//	AD_Message_ID,Text,Reference,
			sql.append("AD_Message_ID,").append(TO_STRING (AD_MessageValue + ": " + Text, 2000)).append(",")
				.append(TO_STRING(Reference,60)).append(", ");
			//	AD_User_ID,AD_Table_ID,Record_ID,Processed
			sql.append(AD_User_ID).append(",").append(AD_Table_ID).append(",")
				.append(Record_ID).append(",'N' ");
			//  Hardcoded AD_Message - AD_Message is in Text
			sql.append("FROM AD_Message WHERE Value='NoMessageFound'");
			//  Try again
			no = executeUpdate(sql.toString());
		}

		return no == 1;
	}   //  insertNote

	/*************************************************************************/

	/**
	 *	Print SQL Warnings.
	 *  <br>
	 *		Usage: DB.printWarning(rs.getWarnings(), "xx");
	 *  @param comment comment
	 *  @param warning warning
	 */
	public static void printWarning(String comment, SQLWarning warning)
	{
		if (comment == null || warning == null || comment.length() == 0)
			throw new IllegalArgumentException("DB.printException - required parameter missing");
		s_log.warn("SQL Warning: " + comment);
		if (warning == null)
			return;
		//
		SQLWarning warn = warning;
		while (warn != null)
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append(warn.getMessage());
			buffer.append("; State=").append(warn.getSQLState()).append("; ErrorCode=").append(warn.getErrorCode());
			s_log.warn(buffer.toString());
			warn = warn.getNextWarning();
		}
	}	//	printWarning

	/**
	 *  Create SQL TO Date String from Timestamp
	 *
	 *  @param  time Date to be converted
	 *  @param  dayOnly true if time set to 00:00:00
	 *
	 *  @return TO_DATE('2001-01-30 18:10:20',''YYYY-MM-DD HH24:MI:SS')
	 *      or  TO_DATE('2001-01-30',''YYYY-MM-DD')
	 */
	public static String TO_DATE (Timestamp time, boolean dayOnly)
	{
		if (time == null)
		{
			if (dayOnly)
				return "TRUNC(SysDate)";
			return "SysDate";
		}

		StringBuffer dateString = new StringBuffer("TO_DATE('");
		//  YYYY-MM-DD HH24:MI:SS.mmmm  JDBC Timestamp format
		String myDate = time.toString();
		if (dayOnly)
		{
			dateString.append(myDate.substring(0,10));
			dateString.append("','YYYY-MM-DD')");
		}
		else
		{
			dateString.append(myDate.substring(0, myDate.indexOf(".")));	//	cut off miliseconds
			dateString.append("','YYYY-MM-DD HH24:MI:SS')");
		}
		return dateString.toString();
	}   //  TO_DATE

	/**
	 *  Create SQL TO Date String from Timestamp
	 *  @param time time
	 *  @return TO_DATE String
	 */
	public static String TO_DATE (Timestamp time)
	{
		return TO_DATE(time, true);
	}   //  TO_DATE

	/**
	 *  Create SQL for formatted Date, Number
	 *
	 *  @see org.compiere.util.DisplayType
	 *  @see org.compiere.util.Env
	 *
	 *  @param  columnName  the column name in the SQL
	 *  @param  displayType Display Type
	 *  @param  AD_Language 6 character language setting (from Env.LANG_*)
	 *
	 *  @return TRIM(TO_CHAR(columnName,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.'''))
	 *      or TRIM(TO_CHAR(columnName,'TM9')) depending on DisplayType and Language
	 */
	public static String TO_CHAR (String columnName, int displayType, String AD_Language)
	{
		if (columnName == null || AD_Language == null || columnName.length() == 0)
			throw new IllegalArgumentException("DB.TO_CHAR - required parameter missing");

		StringBuffer retValue = new StringBuffer("TRIM(TO_CHAR(");
		retValue.append(columnName);

		//  Numbers
		if (DisplayType.isNumeric(displayType))
		{
			if (displayType == DisplayType.Amount)
				retValue.append(",'9G999G990D00'");
			else
				retValue.append(",'TM9'");
			//  TO_CHAR(GrandTotal,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.''')
			if (!Language.isDecimalPoint(AD_Language))      //  reversed
				retValue.append(",'NLS_NUMERIC_CHARACTERS='',.'''");
		}
		else if (DisplayType.isDate(displayType))
		{
			retValue.append(",'")
				.append(Language.getLanguage(AD_Language).getDBdatePattern())
				.append("'");
		}

		retValue.append("))");
		//
		return retValue.toString();
	}   //  TO_CHAR_Number


	/**
	 *  Package Strings for SQL command
	 *  @param txt  String with text
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt)
	{
		return TO_STRING (txt, 0);
	}   //  TO_STRING

	/**
	 *	Package Strings for SQL command.
	 *  <pre>
	 *		-	include in ' (single quotes)
	 *		-	replace ' with ''
	 *  </pre>
	 *  @param txt  String with text
	 *  @param maxLength    Maximum Length of content or 0 to ignore
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt, int maxLength)
	{
		if (txt == null)
			return "NULL";

		//  Length
		String text = txt;
		if (maxLength != 0 && text.length() > maxLength)
			text = txt.substring(0, maxLength);

		char quote = '\'';
		//  copy characters		(wee need to look through anyway)
		StringBuffer out = new StringBuffer();
		out.append(quote);		//	'
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (c == quote)
				out.append("''");
			else
				out.append(c);
		}
		out.append(quote);		//	'
		//
		return out.toString();
	}	//	TO_STRING

}	//	DB

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
package org.compiere.util;

import java.math.*;
import java.util.*;
import java.text.*;
import java.security.*;
import javax.swing.*;

import java.sql.*;
import javax.sql.*;
import oracle.jdbc.*;

import org.apache.log4j.Logger;

import org.compiere.Compiere;
import org.compiere.util.*;
import org.compiere.db.*;

/**
 *  General Database Interface
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB.java,v 1.17 2003/07/22 18:49:56 jjanke Exp $
 */
public final class DB
{
	/** Connection Descriptor           */
	private static CConnection      s_cc = null;
	/** Connection Cache r/o            */
	private static Connection[]		s_connections = null;
	/** Connection Cache Size           */
	private static int              s_conCacheSize = 2;   //  client
	/** Connection counter              */
	private static int              s_conCount = 0;
	/** Connection r/w                  */
	private static Connection		s_connectionRW = null;
	/**	Logger							*/
	private static Logger			s_log = Logger.getLogger (DB.class);

	/**
	 *	Client Login.
	 *  <p>
	 *  - Get Connection
	 *  - Compare User info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 * @param ctx context
	 * @param cc connection
	 * @param app_user user
	 * @param app_pwd pwd
	 * @param force ignore pwd
	 * @return  Array of Role KeyNamePair or null if error
	 * The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	protected static KeyNamePair[] login (Properties ctx,
		CConnection cc,
		String app_user, String app_pwd, boolean force)
	{
		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		//	Establish connection
		closeTarget();
		setDBTarget(cc);
		Env.setContext(ctx, "#Host", s_cc.getAppsHost());
		Env.setContext(ctx, "#Database", s_cc.getDbName());
		if (getConnectionRO() == null)
		{
			Log.saveError("NoDatabase", "");
			return null;
		}
		if (app_pwd == null)
			return null;
		//
		return loginDB (ctx, app_user, app_pwd, force);
	}   //  login

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user Principal
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx, Principal app_user)
	{
		if (app_user == null)
			return null;
		//  login w/o password as previously authorized
		return loginDB (ctx, app_user.getName(), null, false);
	}   //  app_user

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user user id
	 *  @param app_pwd password
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx,
		String app_user, String app_pwd)
	{
		if (app_pwd == null)
			return null;
		return loginDB (ctx, app_user, app_pwd, false);
	}   //  login

	/**
	 *  Actual DB login procedure.
	 *
	 *  @param ctx context
	 *  @param app_user user
	 *  @param app_pwd pwd
	 *  @param force ignore pwd
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	private static KeyNamePair[] loginDB (Properties ctx,
		String app_user, String app_pwd, boolean force)
	{
		s_log.info("login - User=" + app_user);

		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		if (app_user == null)
			return null;

		KeyNamePair[] retValue = null;
		ArrayList list = new ArrayList();
		//
		String sql = "SELECT AD_User.AD_User_ID, AD_User.Description,"
			+ " AD_Role.AD_Role_ID, AD_Role.Name "
			+ "FROM AD_User, AD_User_Roles, AD_Role "
			+ "WHERE AD_User.AD_User_ID=AD_User_Roles.AD_User_ID"
			+ " AND AD_User_Roles.AD_Role_ID=AD_Role.AD_Role_ID"
			+ " AND AD_User.Name=?"		        							//	#1
			+ " AND AD_User.IsActive='Y' AND AD_Role.IsActive='Y' AND AD_User_Roles.IsActive='Y'";
		if (app_pwd != null)
			sql += " AND (AD_User.Password=? OR AD_User.Password=?)";   	//  #2/3
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setString(1, app_user);
			if (app_pwd != null)
			{
				pstmt.setString(2, app_pwd);
				pstmt.setString(3, Secure.getDigest(app_pwd));
			}
			//	execute a query
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())		//	no record found
				if (force)
				{
					Env.setContext(ctx, "#AD_User_Name", "System");
					Env.setContext(ctx, "#AD_User_ID", "0");
					Env.setContext(ctx, "#AD_User_Description", "System Forced Login");
					Env.setContext(ctx, "#User_Level", "S  ");  	//	Format 'SCO'
					Env.setContext(ctx, "#User_Client", "0");		//	Format c1, c2, ...
					Env.setContext(ctx, "#User_Org", "0"); 		//	Format o1, o2, ...
					rs.close();
					pstmt.close();
					retValue = new KeyNamePair[] {new KeyNamePair(0, "System Administrator")};
					return retValue;
				}
				else
				{
					rs.close();
					pstmt.close();
					Log.saveError("UserPwdError", app_user, false);
					return null;
				}

			Env.setContext(ctx, "#AD_User_Name", app_user);
			Env.setContext(ctx, "#AD_User_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#SalesRep_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#AD_User_Description", rs.getString("Description"));
			//
			Ini.setProperty(Ini.P_UID, app_user);
			if (Ini.getPropertyBool(Ini.P_STORE_PWD))
				Ini.setProperty(Ini.P_PWD, app_pwd);

			do	//	read all roles
			{
				int AD_Role_ID = rs.getInt("AD_Role_ID");
				String Name = rs.getString("Name");
				KeyNamePair p = new KeyNamePair(AD_Role_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("login", ex);
			Log.saveError("DBLogin", ex.getLocalizedMessage());
			return null;
		}

		//	Change via SQL detection comes here
		Env.setContext(ctx, "#User_SecurityID", "85263");
		//
		retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# roles = " + retValue.length);
		return retValue;
	}	//	login

	/**
	 *  Load Clients.
	 *  <p>
	 *  Sets Role info in context and loads its clients
	 *
	 *  @param ctx context
	 *  @param  role    role information
	 *  @return list of valid client KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadClients (Properties ctx, KeyNamePair role)
	{
		if (ctx == null || role == null)
			throw new IllegalArgumentException("DB.loadClients - required parameter missing");

		s_log.debug("loadClients - Role=" + role.toString());

		ArrayList list = new ArrayList();
		//	get Role details
		try
		{
			String sql = "SELECT DISTINCT r.UserLevel,r.ClientList,r.OrgList,"
				+ " r.C_Currency_ID,r.AmtApproval, oa.AD_Client_ID,c.Name "
				+ "FROM AD_Role r"
				+ " INNER JOIN AD_Role_OrgAccess oa ON (r.AD_Role_ID=oa.AD_Role_ID)"
				+ " INNER JOIN AD_Client c ON (oa.AD_Client_ID=c.AD_Client_ID) "
				+ "WHERE r.AD_Role_ID=?"		//	#1
				+ " AND r.IsActive='Y' AND c.IsActive='Y'";

			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, role.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadClients - No Clients for Role=" + role.getKey());
				return null;
			}

			//  Role Info
			Env.setContext(ctx, "#AD_Role_ID", role.getKey());
			Env.setContext(ctx, "#AD_Role_Name", role.getName());
			Ini.setProperty(Ini.P_ROLE, role.getName());

			//	User Level
			Env.setContext(ctx, "#User_Level", rs.getString(1));  	//	Format 'SCO'
			//	ClientList
			Env.setContext(ctx, "#User_Client", rs.getString(2));	//	Format c1, c2, ...
			//	OrgList
			Env.setContext(ctx, "#User_Org", rs.getString(3)); 		//	Format o1, o2, ...
			//  Approval Currency / Amount
			Env.setContext(ctx, "#Approval_C_Currency_ID", rs.getInt(4));
			BigDecimal approval = rs.getBigDecimal(5);
			String approvalStr = "0";
			if (approval != null)
				approvalStr = approval.toString();
			Env.setContext(ctx, "#Approval_Amt", approvalStr);


			//  load Clients
			do
			{
				int AD_Client_ID = rs.getInt(6);
				String Name = rs.getString(7);
				KeyNamePair p = new KeyNamePair(AD_Client_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadClients", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# clients = " + retValue.length);
		return retValue;
	}   //  loadClients

	/**
	 *  Load Organizations.
	 *  <p>
	 *  Sets Client info in context and loads its organization, the role has access to
	 *
	 *  @param ctx context
	 *  @param  client    client information
	 *  @return list of valid Org KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadOrgs (Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadOrgs - required parameter missing");

		s_log.debug("loadOrgs - Client=" + client.toString());

		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Role_ID");

		ArrayList list = new ArrayList();
		int AD_Role_ID = Env.getContextAsInt(ctx,"#AD_Role_ID");
		s_log.debug("AD_Role_ID=" + AD_Role_ID);

		//	get Client details for role
		try
		{
			String sql = "SELECT c.Value,c.SMTPHost,c.IsMultiLingualDocument,c.AD_Language,"	//	1..4
				+ " o.AD_Org_ID,o.Name "				//	5..6
				+ "FROM AD_Client c"
				+ " INNER JOIN AD_Org o ON (o.AD_Client_ID=c.AD_Client_ID) "
				+ "WHERE o.AD_Client_ID=?"  			//  #1
				+ " AND o.IsSummary='N' AND o.IsActive='Y'"
				+ " AND o.AD_Org_ID IN "
				+   "(SELECT AD_Org_ID FROM AD_Role_OrgAccess WHERE AD_Role_ID=?)"; //  #2
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, client.getKey());
			pstmt.setInt(2, AD_Role_ID);
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadOrgs - No Org for Client=" + client.getKey());
				return null;
			}

			//  Client Info
			Env.setContext(ctx, "#AD_Client_ID", client.getKey());
			Env.setContext(ctx, "#AD_Client_Name", client.getName());
			Ini.setProperty(Ini.P_CLIENT, client.getName());
			//
			Env.setContext(ctx, "#Client_Value", rs.getString(1));
			Env.setContext(ctx, "#Client_SMTP", rs.getString(2));
			Env.setContext(ctx, "#IsMultiLingualDocument", rs.getString(3));

			//  load Orgs
			do
			{
				int AD_Org_ID = rs.getInt(5);
				String Name = rs.getString(6);
				KeyNamePair p = new KeyNamePair(AD_Org_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadOrgs", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# orgs = " + retValue.length);
		return retValue;
	}   //  loadOrgs

	/**
	 *	Load Preferences into Context for selected client.
	 *  <p>
	 *  Sets Org info in context and loads relevant field from
	 *	- AD_Client/Info,
	 *  - C_AcctSchema,
	 *  - C_AcctSchema_Elements
	 *	- AD_Preference
	 *  <p>
	 *  Assumes that the context is set for #AD_Client_ID, #AD_User_ID, #AD_Role_ID
	 *
	 *  @param ctx context
	 *  @param  org    org information
	 *  @param  warehouse   optional warehouse information
	 *  @param  timestamp   optional date
	 *  @param  printerName optional printer info
	 *  @returns AD_Message of error (NoValidAcctInfo) or ""
	 */
	public static String loadPreferences (Properties ctx,
		KeyNamePair org, KeyNamePair warehouse, Timestamp timestamp, String printerName)
	{
		s_log.info("loadPreferences - Org=" + org);

		if (ctx == null || org == null)
			throw new IllegalArgumentException("DB.loadPreferences - required parameter missing");
		if (Env.getContext(ctx,"#AD_Client_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Client_ID");
		if (Env.getContext(ctx,"#AD_User_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_User_ID");
		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Role_ID");


		//  Org Info - assumes that it is valid
		Env.setContext(ctx, "#AD_Org_ID", org.getKey());
		Env.setContext(ctx, "#AD_Org_Name", org.getName());
		Ini.setProperty(Ini.P_ORG, org.getName());

		//  Warehouse Info
		if (warehouse != null)
		{
			Env.setContext(ctx, "#M_Warehouse_ID", warehouse.getKey());
			Ini.setProperty(Ini.P_WAREHOUSE, warehouse.getName());
		}

		//	Date (default today)
		long today = System.currentTimeMillis();
		if (timestamp != null)
			today = timestamp.getTime();
		java.sql.Date sd = new java.sql.Date(today);
		Env.setContext(ctx, "#Date", sd.toString());	//	YYYY-MM-DD

		//	Optional Printer
		if (printerName == null)
			printerName = "";
		Env.setContext(ctx, "#Printer", printerName);
		Ini.setProperty(Ini.P_PRINTER, printerName);

		//	Other
		Env.setAutoCommit(ctx, Ini.getProperty(Ini.P_A_COMMIT).equals("Y"));
		Env.setContext(ctx, "#CompiereSys", Ini.getProperty(Ini.P_COMPIERESYS));
		Env.setContext(ctx, "#ShowAcct", Ini.getProperty(Ini.P_SHOW_ACCT));
		Env.setContext(ctx, "#ShowTrl", Ini.getProperty(Ini.P_SHOW_TRL));

		String retValue = "";
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		int AD_Org_ID =  org.getKey();
		int AD_User_ID =  Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID =  Env.getContextAsInt(ctx, "#AD_Role_ID");

		//	Other Settings
		Env.setContext(ctx, "#YYYY", "Y");
		Env.setContext(ctx, "#StdPrecision", 2);

		//	AccountSchema Info (first)
		String sql = "SELECT * "
			+ "FROM C_AcctSchema a, AD_ClientInfo c "
			+ "WHERE a.C_AcctSchema_ID=c.C_AcctSchema1_ID "
			+ "AND c.AD_Client_ID=?";
		try
		{
			int C_AcctSchema_ID = 0;
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				//  No Warning for System
				if (AD_Role_ID != 0)
					retValue = "NoValidAcctInfo";
			}
			else
			{
				//	Accounting Info
				C_AcctSchema_ID = rs.getInt("C_AcctSchema_ID");
				Env.setContext(ctx, "$C_AcctSchema_ID", C_AcctSchema_ID);
				Env.setContext(ctx, "$C_Currency_ID", rs.getInt("C_Currency_ID"));
				Env.setContext(ctx, "$HasAlias", rs.getString("HasAlias"));
			}
			rs.close();
			pstmt.close();

			//	Accounting Elements
			sql = "SELECT ElementType "
				+ "FROM C_AcctSchema_Element "
				+ "WHERE C_AcctSchema_ID=?"
				+ " AND IsActive='Y'";
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, C_AcctSchema_ID);
			rs = pstmt.executeQuery();
			while (rs.next())
				Env.setContext(ctx, "$Element_" + rs.getString("ElementType"), "Y");
			rs.close();
			pstmt.close();

			//	This reads all relevant window neutral defaults
			//	overwriting superseeded ones.  Window specific is read in Mainain
			sql = "SELECT Attribute, Value, AD_Window_ID "
				+ "FROM AD_Preference "
				+ "WHERE AD_Client_ID IN (0, @#AD_Client_ID@)"
				+ " AND AD_Org_ID IN (0, @#AD_Org_ID@)"
				+ " AND (AD_User_ID IS NULL OR AD_User_ID=0 OR AD_User_ID=@#AD_User_ID@)"
				+ " AND IsActive='Y' "
				+ "ORDER BY Attribute, AD_Client_ID, AD_User_ID DESC, AD_Org_ID";
				//	the last one overwrites - System - Client - User - Org - Window
			sql = Env.parseContext(ctx, 0, sql, false);
			if (sql.length() == 0)
				s_log.error("loadPreferences - Missing Environment");
			else
			{
				pstmt = prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					int AD_Window_ID = rs.getInt(3);
					String at = "";
					if (rs.wasNull())
						at = "P|" + rs.getString(1);
					else
						at = "P" + AD_Window_ID + "|" + rs.getString(1);
					String va = rs.getString(2);
					Env.setContext(ctx, at, va);
				}
				rs.close();
				pstmt.close();
			}
		}
		catch (SQLException ex)
		{
			s_log.error("loadPreferences (" + sql + ")", ex);
		}

		//	Default Values
		s_log.info("Default Values ...");
		sql = "SELECT t.TableName, c.ColumnName "
			+ "FROM AD_Column c "
			+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID) "
			+ "WHERE c.IsKey='Y' AND t.IsActive='Y'"
			+ " AND EXISTS (SELECT * FROM AD_Column cc "
			+ " WHERE ColumnName = 'IsDefault' AND t.AD_Table_ID=cc.AD_Table_ID AND cc.IsActive='Y')";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				loadDefault (ctx, rs.getString(1), rs.getString(2));
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("loadPreferences", e);
		}
		//
		Ini.saveProperties(Ini.isClient());
		//
		return retValue;
	}	//	loadPreferences

	/**
	 *	Load Default Value for Table into Context.
	 *
	 *  @param ctx context
	 *  @param TableName table name
	 *  @param ColumnName column name
	 */
	private static void loadDefault (Properties ctx, String TableName, String ColumnName)
	{
		String value = null;
		//
		String SQL = "SELECT " + ColumnName + " FROM " + TableName
			+ " WHERE IsDefault='Y' AND IsActive='Y' ORDER BY AD_Client_ID";
		SQL = Access.addROAccessSQL(ctx, SQL, TableName, false);
		try
		{
			PreparedStatement pstmt = prepareStatement(SQL);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())		//	overwrites system defaults
				value = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("loadDefault - " + TableName + " (" + SQL + ")", e);
			return;
		}
		//	Set Context Value
		if (value != null && value.length() != 0)
		{
			if (TableName.equals("C_DocType"))
				Env.setContext(ctx, "#C_DocTypeTarget_ID", value);
			else
				Env.setContext(ctx, "#" + ColumnName, value);
		}
	}	//	loadDefault

	/**
	 *  Load Warehouses
	 *
	 * @param ctx context
	 * @param client client
	 * @return Array of Warehouse Info
	 */
	public static KeyNamePair[] loadWarehouses(Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadWarehouses - required parameter missing");

		s_log.info("loadWarehouses - Client=" + client.toString());

		ArrayList list = new ArrayList();
		try
		{
			String sql = "SELECT M_Warehouse_ID, Name FROM M_Warehouse "
				+ "WHERE AD_Client_ID=? AND IsActive='Y'";  //  #1

			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, client.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.warn("loadWarehouses - No Warehouses for Client=" + client.getKey());
				return null;
			}

			//  load Warehousess
			do
			{
				int AD_Warehouse_ID = rs.getInt(1);
				String Name = rs.getString(2);
				KeyNamePair p = new KeyNamePair(AD_Warehouse_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadWarehouses", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.info("# warehouses = " + retValue.length);
		return retValue;
	}   //  loadWarehouses

	/*************************************************************************/

	/**
	 *  Set connection
	 *  @param cc connection
	 */
	public static void setDBTarget (CConnection cc)
	{
		if (cc == null)
			throw new IllegalArgumentException("DB.setDBTarget connection is NULL");

		if (s_cc == null)
			s_cc = cc;
		synchronized (s_cc)    //  use as mutex
		{
			s_cc = cc;
			//  Closing existing
			if (s_connections != null)
			{
				for (int i = 0; i < s_connections.length; i++)
				{
					try {
						s_connections[i].close();
					} catch (Exception e) {}
				}
			}
			s_connections = null;
		}
	}   //  setDBTarget

	/**
	 *  Is there a connection to the database ?
	 *  @return true, if connected to database
	 */
	public static boolean isConnected()
	{
		try
		{
			getConnectionRW();	//	try to get a connection
			return true;
		}
		catch (Exception e)
		{
		}
		return false;
	}   //  isConnected

	/**
	 *	Return (pooled) r/w AutoCommit, read committed connection
	 *
	 *  @return Connection (r/w)
	 */
	public static Connection getConnectionRW ()
	{
		//	check health of connection
		try
		{
			if (s_connectionRW == null)
				;
			else if (s_connectionRW.isClosed())
				s_connectionRW = null;
			else if (s_connectionRW instanceof OracleConnection && ((OracleConnection)s_connectionRW).pingDatabase(1) < 0)
				s_connectionRW = null;
			else
				 s_connectionRW.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		catch (Exception e)
		{
			s_connectionRW = null;
		}
		if (s_connectionRW == null)
			s_connectionRW = s_cc.getConnection (true, Connection.TRANSACTION_READ_COMMITTED);
		if (s_connectionRW == null)
			throw new UnsupportedOperationException("DB.getConnectionRW - @NoDBConnection@");
		return s_connectionRW;
	}   //  getConnectionRW

	/**
	 *	Return read committed, read/only connection with AutoCommit from pool
	 *  @return Connection (r/o)
	 */
	static Connection getConnectionRO ()
	{
		try
		{
			synchronized (s_cc)    //  use as mutex as s_connection is null the first time
			{
				if (s_connections == null)
					s_connections = createConnections (Connection.TRANSACTION_READ_COMMITTED);     //  see below
			}
		}
		catch (Exception e)
		{
			s_log.error("getConnectionRO", e);
		}

		//  check health of connection
		int pos = s_conCount++;
		Connection connection = s_connections[pos % s_conCacheSize];
		try
		{
			if (connection == null)
				;
			else if (connection.isClosed())
				connection = null;
			else if (connection instanceof OracleConnection && ((OracleConnection)connection).pingDatabase(1) < 0)
				connection = null;
			else
				 connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		catch (Exception e)
		{
			connection = null;
		}
		//	Get new
		if (connection == null)
		{
			System.out.println("DB.getConnectionRO - replacing connection #" + pos % s_conCacheSize);
		//	s_log.error("getConnectionRO - replacing connection #" + pos % s_conCacheSize);
			connection = s_cc.getConnection (true, Connection.TRANSACTION_READ_COMMITTED);            //  see above
		/*	try
			{
				retValue.setReadOnly(true);     //  not supported by Oracle
			}
			catch (Exception e)
			{
				System.err.println("DB.getConnectionRO - Cannot set to R/O - " + e);
			}   */
			s_connections[pos % s_conCacheSize] = connection;
		}
		if (connection == null)
			throw new UnsupportedOperationException("DB.getConnectionRO - @NoDBConnection@");
		return connection;
	}	//	getConnectionRO

	/**
	 *	Create new Connection.
	 *  The connection must be closed explicitly by the application
	 *
	 *  @param autoCommit auto commit
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Connection connection
	 */
	public static Connection createConnection (boolean autoCommit, int trxLevel)
	{
		s_log.debug("createConnection " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", AutoCommit=" + autoCommit + ", TrxLevel=" + trxLevel);
		return s_cc.getConnection (autoCommit, trxLevel);
	}	//	createConnection

	/**
	 *	Create new set of r/o Connections.
	 *  R/O connection might not be supported by DB
	 *
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Array of Connections (size based on s_conCacheSize)
	 */
	private static Connection[] createConnections (int trxLevel)
	{
		s_log.debug("createConnections (" + s_conCacheSize + ") " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", TrxLevel=" + trxLevel);
		Connection cons[] = new Connection[s_conCacheSize];
		try
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				cons[i] = s_cc.getConnection (true, trxLevel);  //  auto commit
				if (cons[i] == null)
					System.err.println("createConnections - connection is NULL");	//	don't use log
			}
		}
		catch (Exception e)
		{
			//  Don't use Log
			System.err.println("DB.createConnections - " + e.getMessage());
		}
		return cons;
	}	//	createConnections

	/**
	 *  Get Database Driver.
	 *  Access to database specific functionality.
	 *  @return Compiere Database Driver
	 */
	public static CompiereDatabase getDatabase()
	{
		if (s_cc != null)
			return s_cc.getDatabase();
		return null;
	}   //  getDatabase

	/*************************************************************************/

	/**
	 *  Check database Version with Code version
	 *  @param ctx context
	 *  @return true if Database version (date) is the same
	 */
	public static boolean isDatabaseOK (Properties ctx)
	{
		//	Force loading Messages of current language
		Msg.getMsg(ctx, "0");

		//  Check Version
		String version = "?";
		String sql = "SELECT Version FROM AD_System";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				version = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("Problem with AD_System Table - Run system.sql script - " + e.toString());
		}
		s_log.info("isDatabaseOK - DB_Version=" + version);
		//  identical version
		if (Compiere.DB_VERSION.equals(version))
			return true;

		String AD_Message = "DatabaseVersionError";
		String title = org.compiere.Compiere.getName() + " " +  Msg.getMsg(ctx, AD_Message, true);
		//	Code assumes Database version {0}, but Database has Version {1}.
		String msg = Msg.getMsg(ctx, AD_Message);	//	complete message
		msg = MessageFormat.format(msg, new Object[] {Compiere.DB_VERSION, version});
		Object[] options = { UIManager.get("OptionPane.noButtonText"), "Migrate" };
		int no = JOptionPane.showOptionDialog (null, msg,
			title, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
			UIManager.getIcon("OptionPane.errorIcon"), options, options[0]);
		if (no == 1)
		{
			try
			{
				Class.forName("com.compiere.client.StartMaintain").newInstance();
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog (null,
					ex.getMessage() + "\nSee: http://www.compiere.com/maintain",
					title, JOptionPane.ERROR_MESSAGE);
				s_log.error("isDatabaseOK - " + ex.getMessage());
			}
		}
		return false;
	}   //  isDatabaseOK

	/*************************************************************************/

	/**
	 *	Security Context Login.
	 *  @param uid uid
	 *  @param pwd pwd
	 *  @param role role
	 *  @return true if connected
	 */
	public static boolean login_context (String uid, String pwd, String role)
	{
		if (uid == null || pwd == null || role == null || uid.length() == 0 || pwd.length() == 0 || role.length() == 0)
			throw new IllegalArgumentException("DB.login_context - required parameter missing");
		s_log.info("login_context uid=" + uid);
		if (uid == null || pwd == null || role == null)
			return false;
		//
		String SQL = "{CALL Compiere_Context.Login(?,?,?,?)}";
		boolean result = true;
		try
		{
			CallableStatement cstmt = getConnectionRO().prepareCall(SQL);
			cstmt.setString(1, uid);
			cstmt.setString(2, pwd);
			cstmt.setString(3, role);
			cstmt.setString(4, Language.getBaseAD_Language());
			result = cstmt.execute();
			cstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("login_context", e);
			result = false;
		}
		return result;
	}	//	login_context

	/*************************************************************************/

	/**
	 *	Close Target
	 */
	public static void closeTarget()
	{
		if (s_connections != null)
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				try
				{
					if (s_connections[i] != null)
						s_connections[i].close();
				}
				catch (SQLException e)
				{
					s_log.error("close connection #" + i, e);
				}
				s_connections[i] = null;
			}
			try
			{
				if (s_connectionRW != null)
					s_connectionRW.close();
			}
			catch (SQLException e)
			{
				s_log.error("close R/W connection", e);
			}
			s_connectionRW = null;
		}
		s_connections = null;
//		EJB.close();
	}	//	closeTarget

	/*************************************************************************/

	/**
	 *	Prepare Forward Read Only Call
	 *  @param RO_SQL sql (RO)
	 *  @return Callable Statement
	 */
	public static CallableStatement prepareCall(String RO_SQL)
	{
		if (RO_SQL == null || RO_SQL.length() == 0)
			throw new IllegalArgumentException("DB.prepareCall - required parameter missing - " + RO_SQL);
		//
		String sql = getDatabase().convertStatement(RO_SQL);
		try
		{
			return getConnectionRO().prepareCall
				(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		}
		catch (SQLException e)
		{
			s_log.error("prepareCall (" + sql + ")", e);
		}
		return null;
	}	//	prepareCall

	/*************************************************************************/

	/**
	 *	Prepare Read Only Statement
	 *  @param RO_SQL sql (RO)
	 *  @return Prepared Statement
	 */
	public static PreparedStatement prepareStatement(String RO_SQL)
	{
		return prepareStatement(RO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}	//	prepareStatement

	/**
	 *	Prepare Statement
	 *
	 *  @param SQL sql statement
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Prepared Statement r/o or r/w depending on concur
	 */
	public static PreparedStatement prepareStatement(String SQL, int resultSetType, int resultSetConcurrency)
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.prepareStatement - required parameter missing - " + SQL);
		//
		return new CompiereStatement(SQL, resultSetType, resultSetConcurrency);
		/**
		try
		{
			Connection conn = null;
			if (resultSetConcurrency == ResultSet.CONCUR_UPDATABLE)
				conn = getConnectionRW();
			else
				conn = getConnectionRO();
			return conn.prepareStatement (sql, resultSetType, resultSetConcurrency);
		}
		catch (SQLException e)
		{
			s_log.error("prepareStatement (" + sql + ")", e);
		}
		return null;
		**/
	}	//	prepareStatement

	/**
	 *	Create Read Only Statement
	 *  @return Statement
	 */
	public static Statement createStatement()
	{
		return createStatement (ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}	//	createStatement

	/**
	 *	Create Statement
	 *
	 *  @param type - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param concur - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Statement - either r/w ir r/o depending on concur
	 */
	public static Statement createStatement(int type, int concur)
	{
		try
		{
			Connection conn = null;
			if (concur == ResultSet.CONCUR_UPDATABLE)
				conn = getConnectionRW();
			else
				conn = getConnectionRO();
			return conn.createStatement(type, concur);
		}
		catch (SQLException e)
		{
			s_log.error("createStatement", e);
		}
		return null;
	}	//	createStatement

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param SQL sql
	 *  @return number of rows updated
	 */
	public static int executeUpdate (String SQL)
	{
		return executeUpdate(SQL, false);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param SQL sql
	 * 	@param ignoreError if true, no execution error is reported
	 *  @return number of rows updated
	 */
	public static int executeUpdate (String SQL, boolean ignoreError)
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.executeUpdate - required parameter missing - " + SQL);
		//
		String sql = getDatabase().convertStatement(SQL);
		int no = 0;
		Statement stmt = null;
		try
		{
			Connection conn = getConnectionRW();
			stmt = conn.createStatement();
			no = stmt.executeUpdate(sql);
		}
		catch (SQLException e)
		{
			if (!ignoreError)
			{
				s_log.error ("executeUpdate - " + sql, e);
				Log.saveError ("DBExecuteError", e.getLocalizedMessage ());
			}
		}
		finally
		{
			//  Always close cursor
			try
			{
				stmt.close();
			}
			catch (SQLException e2)
			{
				s_log.error("executeUpdate - cannot close statement");
			}
		}
		return no;
	}	//	execute Update


	/**
	 *	Commit - commit on RW connection.
	 *  Is not required as RW connection is AutoCommit
	 *  @param throwException if true, re-throws exception
	 *  @return true if not needed or success
	 *  @throws SQLException
	 */
	public static boolean commit (boolean throwException) throws SQLException
	{
		try
		{
			Connection conn = getConnectionRW();
		//	if (!conn.getAutoCommit())
				conn.commit();
		}
		catch (SQLException e)
		{
			s_log.error("commit", e);
			if (throwException)
				throw e;
			return false;
		}
		return true;
	}	//	commit

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @return next no
	 */
	public static int getKeyNextNo (Properties ctx, int WindowNo, String TableName)
	{
		if (ctx == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getKeyNextNo - required parameter missing");
		String CompiereSys = Env.getContext(ctx, "#CompiereSys");
		int AD_Client_ID = Env.getContextAsInt(ctx, WindowNo, "AD_Client_ID");
		//
		return getKeyNextNo (AD_Client_ID, CompiereSys, TableName);
	}   //  getKeyNextNo

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param AD_Client_ID client
	 *  @param CompiereSys compiere sys
	 *  @param TableName table name
	 *  @return next no
	 */
	public static int getKeyNextNo (int AD_Client_ID, String CompiereSys, String TableName)
	{
		if (TableName == null || CompiereSys == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getKeyNextNo - required parameter missing");
		int retValue = 0;

		//	Check CompiereSys
	//	if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
	//		throw new UnsupportedOperationException("DB.getKeyNextNo - Cannot add System records");
		//
		try
		{
			String SQL = "{CALL AD_Sequence_Next(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setString(1, TableName);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.NUMERIC);
			cstmt.executeUpdate();
			retValue = cstmt.getInt(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getKeyNextNo - Table=" + TableName + ")", e);
		}
		return retValue;
	}	//	getKeyNextNo

	/**
	 *	Get Document Number for current document.
	 *  <br>
	 *  - first search for DocType based Document No
	 *  - then Search for DocumentNo based on TableName
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @param onlyDocType Do not search for document no based on TableName
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (Properties ctx, int WindowNo, String TableName, boolean onlyDocType)
	{
		if (ctx == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getDocumentNo - required parameter missing");
		String CompiereSys = Env.getContext(ctx, "#CompiereSys");
		int AD_Client_ID = Env.getContextAsInt(ctx, WindowNo, "AD_Client_ID");

		//	Get C_DocType_ID from context - NO Defaults -
		int C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID");
		if (C_DocType_ID == 0)
			C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID");
		if (C_DocType_ID == 0)
		{
			s_log.debug("getDocumentNo - for Window=" + WindowNo
				+ " - Target=" + Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID") + "/" + Env.getContextAsInt(ctx, WindowNo, "C_DocTypeTarget_ID")
				+ " - Actual=" + Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID") + "/" + Env.getContextAsInt(ctx, WindowNo, "C_DocType_ID"));
			return getDocumentNo (AD_Client_ID, CompiereSys, TableName);
		}

		//	Check CompiereSys
		if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
			throw new UnsupportedOperationException("DB.getDocumentNo - Cannot add System records");
		//
		String retValue = getDocumentNo(AD_Client_ID, C_DocType_ID);
		if (!onlyDocType && retValue == null)
			return getDocumentNo (AD_Client_ID, CompiereSys, TableName);
		return retValue;
	}	//	getDocumentNo

	/**
	 *	Get Document Number for current document.
	 *  @param AD_Client_ID client
	 *  @param C_DocType_ID (target) document type
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (int AD_Client_ID, int C_DocType_ID)
	{
		String retValue = null;
		try
		{
			String SQL = "{CALL AD_Sequence_DocType(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setInt(1, C_DocType_ID);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.VARCHAR);
			cstmt.executeUpdate();
			retValue = cstmt.getString(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getDocumentNo - DocType=" + C_DocType_ID, e);
		}
		s_log.info("getDocumentNo - DocType=" + C_DocType_ID + " -> " + retValue);
		return retValue;
	}	//	getDocumentNo


	/**
	 *  Get Next Document No
	 *  @param AD_Client_ID client
	 *  @param CompiereSys system
	 *  @param TableName table name
	 *  @return DocumentNo
	 */
	public static String getDocumentNo (int AD_Client_ID, String CompiereSys, String TableName)
	{
		if (CompiereSys == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getDocumentNo - required parameter missing");
		//	Check CompiereSys
	//	if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
	//		throw new UnsupportedOperationException("DB.getDocumentNo - Cannot add System records");
		//
		String retValue = null;
		try
		{
			String SQL = "{CALL AD_Sequence_Doc(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setString(1, "DocumentNo_" + TableName);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.VARCHAR);
			cstmt.executeUpdate();
			retValue = cstmt.getString(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getDocumentNo  TableName=" + TableName, e);
		}
		s_log.info("getDocumentNo - TableName=" + TableName + " -> " + retValue);
		return retValue;
	}   //  getDocumentNo


	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int int_param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@param s_param2 parameter 2
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int int_param1, String s_param2)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			pstmt.setString(2, s_param2);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value: " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue


	/**
	 *  Convert an amount with today's spot rate
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  Amt         The amount to be converted
	 * 	@param	AD_Client_ID client
	 * 	@param	AD_Org_ID	organization
	 *  @return converted amount
	 */
	public static BigDecimal getConvertedAmt (BigDecimal Amt, int CurFrom_ID, int CurTo_ID, int AD_Client_ID, int AD_Org_ID)
	{
		return getConvertedAmt (Amt, CurFrom_ID, CurTo_ID, null, null, AD_Client_ID, AD_Org_ID);
	}   //  getConvertedAmt

	/**
	 *	Convert an amount
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  ConvDate    The Conversion date - if null - use current date
	 *  @param  RateType    The Conversion rate type - if null/empty - use Spot
	 *  @param  Amt         The amount to be converted
	 * 	@param	AD_Client_ID client
	 * 	@param	AD_Org_ID	organization
	 *  @return converted amount
	 */
	public static BigDecimal getConvertedAmt (BigDecimal Amt, int CurFrom_ID, int CurTo_ID,
		Timestamp ConvDate, String RateType, int AD_Client_ID, int AD_Org_ID)
	{
		if (Amt == null)
			throw new IllegalArgumentException("DB.getConvertedAmt - required parameter missing - Amt");
		BigDecimal retValue = null;
		if (CurFrom_ID == CurTo_ID || Amt.equals(Env.ZERO))
			return Amt;
		//
		try
		{
			String sql = "{? = call C_Currency_Convert(?,?,?,?,?, ?,?) }";
			CallableStatement cstmt = prepareCall(sql);
			//
			cstmt.registerOutParameter(1, Types.NUMERIC);
			//
			cstmt.setBigDecimal(2, Amt);					//	Amount		IN  	NUMBER
			cstmt.setInt(3, CurFrom_ID);             		//	CurFrom		IN 		NUMBER
			cstmt.setInt(4, CurTo_ID);                  	//	CurTo		IN 		NUMBER
			if (ConvDate == null)
				ConvDate = new Timestamp (System.currentTimeMillis());
			cstmt.setTimestamp(5, ConvDate);				//	ConvDate	IN 		DATE
			if (RateType == null || RateType.equals(""))
				RateType = "S";
			cstmt.setString(6, RateType);					//	RateType	IN 		CHAR
			cstmt.setInt(7, AD_Client_ID);
			cstmt.setInt(8, AD_Org_ID);
			//
			cstmt.executeUpdate();
			retValue = cstmt.getBigDecimal(1);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getConvertedAmt", e);
		}
		if (retValue == null)
			s_log.info("getConvertedAmt - not found - CurFrom=" + CurFrom_ID + ", CurTo=" + CurTo_ID
				+ ", " + ConvDate + ", " + RateType + ", Client=" + AD_Client_ID + ", Org=" + AD_Org_ID);
		return retValue;
	}	//	getConvertedAmt

	/**
	 *	Get Currency Rate
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  ConvDate    The Conversion date - if null - use current date
	 *  @param  RateType    The Conversion rate type - if null/empty - use Spot
	 * 	@param	AD_Client_ID client
	 * 	@param	AD_Org_ID	organization
	 *  @return currency Rate or null
	 */
	public static BigDecimal getConvesionRate (int CurFrom_ID, int CurTo_ID,
		Timestamp ConvDate, String RateType, int AD_Client_ID, int AD_Org_ID)
	{
		BigDecimal retValue = null;
		if (CurFrom_ID == CurTo_ID)
			return new BigDecimal(1);
		//
		try
		{
			String sql = "{? = call C_Currency_Rate(?,?,?,?,?,?)}";
			CallableStatement cstmt = prepareCall(sql);
			//
			cstmt.registerOutParameter(1, Types.NUMERIC);
			//
			cstmt.setInt(2, CurFrom_ID);             		//	CurFrom		IN 		NUMBER
			cstmt.setInt(3, CurTo_ID);                  	//	CurTo		IN 		NUMBER
			if (ConvDate == null)
				ConvDate = new Timestamp (System.currentTimeMillis());
			cstmt.setTimestamp(4, ConvDate);				//	ConvDate	IN 		DATE
			if (RateType == null || RateType.equals(""))
				RateType = "S";
			cstmt.setString(5, RateType);					//	RateType	IN 		CHAR
			cstmt.setInt(6, AD_Client_ID);
			cstmt.setInt(7, AD_Org_ID);
			//
			cstmt.executeUpdate();
			retValue = cstmt.getBigDecimal(1);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getConvesionRate", e);
		}
		if (retValue == null)
			s_log.info ("getConversionRate - not found - CurFrom=" + CurFrom_ID + ", CurTo=" + CurTo_ID
			  + ", " + ConvDate + ", " + RateType + ", Client=" + AD_Client_ID + ", Org=" + AD_Org_ID);
		return retValue;
	}	//	getConvesionRate


	/**
	 *  Get fully qualified Account Combination
	 *
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param C_AcctSchema_ID acct schema
	 *  @param Account_ID natural account
	 *  @param  base_ValidCombination_ID optional base combination to be specified
	 *  @param Alias aloas
	 *  @param AD_User_ID user
	 *  @param M_Product_ID product
	 *  @param C_BPartner_ID partner
	 *  @param AD_OrgTrx_ID trx org
	 *  @param C_LocFrom_ID loc from
	 *  @param C_LocTo_ID loc to
	 *  @param C_SRegion_ID sales region
	 *  @param C_Project_ID project
	 *  @param C_Campaign_ID campaign
	 *  @param C_Activity_ID activity
	 *  @param User1_ID user1
	 *  @param User2_ID user2
	 *  @return C_ValidCombination_ID of existing or new Combination
	 */
	public static int getValidCombination (int AD_Client_ID, int AD_Org_ID,
		int C_AcctSchema_ID, int Account_ID, int base_ValidCombination_ID, String Alias, int AD_User_ID,
		int M_Product_ID, int C_BPartner_ID, int AD_OrgTrx_ID,
		int C_LocFrom_ID, int C_LocTo_ID, int C_SRegion_ID, int C_Project_ID,
		int C_Campaign_ID, int C_Activity_ID, int User1_ID, int User2_ID)
	{
		int retValue = 0;
		StringBuffer sb = new StringBuffer ("C_ValidCombination_Get[");
		try
		{
			String sql = "{CALL C_ValidCombination_Get(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)}";
			CallableStatement cstmt = prepareCall(sql);
			//  1 - ID
			cstmt.registerOutParameter(1, Types.NUMERIC);
			sb.append("v,");
			//  --  Mandatory Accounting fields
			//	2 - AD_Client_ID
			cstmt.setInt(2, AD_Client_ID);
			sb.append("AD_Client_ID=").append(AD_Client_ID).append(",");
			//	3 - AD_Org_ID
			cstmt.setInt(3, AD_Org_ID);
			sb.append("AD_Org_ID=").append(AD_Org_ID).append(",");
			//  4- C_AcctSchema_ID
			cstmt.setInt(4, C_AcctSchema_ID);
			sb.append("C_AcctSchema_ID=").append(C_AcctSchema_ID).append(",");
			//  5 - Account_ID
			cstmt.setInt(5, Account_ID);
			sb.append("Account_ID=").append(Account_ID).append(", ");

			//  --  Optional
			//  6 - Base C_ValidCombination_ID
			if (base_ValidCombination_ID != 0)
			{
				cstmt.setInt (6, base_ValidCombination_ID);
				sb.append("BaseValidCombination_ID=").append(base_ValidCombination_ID).append(",");
			}
			else
			{
				cstmt.setNull(6, Types.NUMERIC);
				sb.append("BaseValidCombination_ID=").append("NULL,");
			}
			//  7 - MustBeFullyQualified
			cstmt.setString(7, "Y");
			sb.append("MustBeFullyQualified='Y',");
			//  8 - Alias
			if (Alias != null && Alias.length() > 0)
			{
				cstmt.setString(8, Alias);
				sb.append("Alias='").append(Alias).append("',");
			}
			else
			{
				cstmt.setNull(8, Types.VARCHAR);
				sb.append("Alias=NULL,");
			}
			//  9 - CreatedBy
			cstmt.setInt(9, AD_User_ID);
			sb.append("AD_User_ID=").append(AD_User_ID).append(", ");

			//	--  Optional Accounting fields
			//  10 - M_Product_ID
			if (M_Product_ID != 0)
			{
				cstmt.setInt(10, M_Product_ID);
				sb.append("M_Product_ID=").append(M_Product_ID).append(",");
			}
			else
			{
				cstmt.setNull(10, Types.NUMERIC);
				sb.append("M_Product_ID=NULL,");
			}
			//  11 - C_BPartner_ID
			if (C_BPartner_ID != 0)
			{
				cstmt.setInt(11, C_BPartner_ID);
				sb.append("C_BPartner_ID=").append(C_BPartner_ID).append(",");
			}
			else
			{
				cstmt.setNull(11, Types.NUMERIC);
				sb.append("C_BPartner_ID=NULL,");
			}
			//  12 - AD_OrgTrx_ID
			if (AD_OrgTrx_ID != 0)
			{
				cstmt.setInt(12, AD_OrgTrx_ID);
				sb.append("AD_OrgTrx_ID=").append(AD_OrgTrx_ID).append(",");
			}
			else
			{
				cstmt.setNull(12, Types.NUMERIC);
				sb.append("AD_OrgTrx_ID=NULL,");
			}
			//  13 - C_LocFrom_ID
			if (C_LocFrom_ID != 0)
			{
				cstmt.setInt(13, C_LocFrom_ID);
				sb.append("C_LocFrom_ID=").append(C_LocFrom_ID).append(",");
			}
			else
			{
				cstmt.setNull(13, Types.NUMERIC);
				sb.append("C_LocFrom=NULL,");
			}
			//  14 - C_LocTo_ID
			if (C_LocTo_ID != 0)
			{
				cstmt.setInt(14, (C_LocTo_ID));
				sb.append("C_LocTo_ID=").append(C_LocTo_ID).append(", ");
			}
			else
			{
				cstmt.setNull(14, Types.NUMERIC);
				sb.append("C_LocTo_ID=NULL, ");
			}
			//  15 - C_SalesRegion_ID
			if (C_SRegion_ID != 0)
			{
				cstmt.setInt(15, (C_SRegion_ID));
				sb.append("C_SalesRegion_ID=").append(C_SRegion_ID).append(",");
			}
			else
			{
				cstmt.setNull(15, Types.NUMERIC);
				sb.append("C_SalesRegion_ID=NULL,");
			}
			//  16 - C_Project_ID
			if (C_Project_ID != 0)
			{
				cstmt.setInt(16, (C_Project_ID));
				sb.append("C_Project_ID=").append(C_Project_ID).append(",");
			}
			else
			{
				cstmt.setNull(16, Types.NUMERIC);
				sb.append("C_Project_ID=NULL,");
			}
			//  17 - C_Campaign_ID
			if (C_Campaign_ID != 0)
			{
				cstmt.setInt(17, (C_Campaign_ID));
				sb.append("C_Campaign_ID=").append(C_Campaign_ID).append(",");
			}
			else
			{
				cstmt.setNull(17, Types.NUMERIC);
				sb.append("C_Campaign_ID=NULL,");
			}
			//  18 - C_Activity_ID
			if (C_Activity_ID != 0)
			{
				cstmt.setInt(18, (C_Activity_ID));
				sb.append("C_Activity_ID=").append(C_Activity_ID).append(",");
			}
			else
			{
				cstmt.setNull(18, Types.NUMERIC);
				sb.append("C_Activity_ID=NULL,");
			}
			//  19 - User1_ID
			if (User1_ID != 0)
			{
				cstmt.setInt(19, (User1_ID));
				sb.append("User1_ID=").append(User1_ID).append(",");
			}
			else
			{
				cstmt.setNull(19, Types.NUMERIC);
				sb.append("User1_ID=NULL,");
			}
			//  20 - User2_ID
			if (User2_ID != 0)
			{
				cstmt.setInt(20, (User2_ID));
				sb.append("User2_ID=").append(User2_ID).append(")");
			}
			else
			{
				cstmt.setNull(20, Types.NUMERIC);
				sb.append("User2_ID=NULL)");
			}

			//
			cstmt.executeUpdate();
			retValue = cstmt.getInt(1);     //  1 - ID
			cstmt.close();
			s_log.debug("getValidCombination " + sb.toString());
		}
		catch(SQLException e)
		{
			s_log.error("getValidCombination " + sb.toString(), e);
		}
		return retValue;
	}   //  getValidCombination

	/**
	 *  Insert Note
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param AD_User_ID user
	 *  @param AD_Table_ID table
	 *  @param Record_ID record
	 *  @param AD_MessageValue message
	 *  @param Text text
	 *  @param Reference subject
	 *  @return true if note was inserted
	 */
	public static boolean insertNote (int AD_Client_ID, int AD_Org_ID, int AD_User_ID,
		int AD_Table_ID, int Record_ID,
		String AD_MessageValue, String Text, String Reference)
	{
		if (AD_MessageValue == null || AD_MessageValue.length() == 0)
			throw new IllegalArgumentException("DB.insertNote - required parameter missing - AD_Message");

		//  Database limits
		if (Text == null)
			Text = "";
		if (Reference == null)
			Reference = "";
		//
		s_log.info("insertNote - " + AD_MessageValue + " - " + Reference);
		//
		StringBuffer sql = new StringBuffer ("INSERT INTO AD_Note (AD_Note_ID,");
		sql.append("AD_Client_ID,AD_Org_ID,IsActive, Created,CreatedBy,Updated,UpdatedBy, ")
			.append("AD_Message_ID,Text,Reference, ")
			.append("AD_User_ID,AD_Table_ID,Record_ID,Processed) ")
			.append("SELECT ");
		//
		String CompiereSys = "N";
		int AD_Note_ID = getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Note");
		sql.append(AD_Note_ID).append(",");
		//
		sql.append(AD_Client_ID).append(",")
			.append(AD_Org_ID).append(", 'Y',SysDate,")
			.append(AD_User_ID).append(",SysDate,0,");
		//	AD_Message_ID,Text,Reference,
		sql.append(" AD_Message_ID,").append(DB.TO_STRING(Text, 2000)).append(",")
			.append(DB.TO_STRING(Reference, 60)).append(", ");
		//	AD_User_ID,AD_Table_ID,Record_ID,Processed
		sql.append(AD_User_ID).append(",").append(AD_Table_ID).append(",")
			.append(Record_ID).append(",'N' ");
		//
		sql.append("FROM AD_Message WHERE Value='").append(AD_MessageValue).append("'");
		//  Create Entry
		int no = executeUpdate(sql.toString());

		//  AD_Message must exist, so if not created, it is probably
		//  due to non-existing AD_Message
		if (no == 0)
		{
			sql = new StringBuffer ("INSERT INTO AD_Note (AD_Note_ID,");
			sql.append("AD_Client_ID,AD_Org_ID,IsActive, Created,CreatedBy,Updated,UpdatedBy, ")
				.append("AD_Message_ID,Text,Reference, ")
				.append("AD_User_ID,AD_Table_ID,Record_ID,Processed) ")
				.append("SELECT ");
			//  - use AD_Note_ID from above
			sql.append(AD_Note_ID).append(",");
			//
			sql.append(AD_Client_ID).append(",")
				.append(AD_Org_ID).append(", 'Y',SysDate,")
				.append(AD_User_ID).append(",SysDate,0, ");
			//	AD_Message_ID,Text,Reference,
			sql.append("AD_Message_ID,").append(TO_STRING (AD_MessageValue + ": " + Text, 2000)).append(",")
				.append(TO_STRING(Reference,60)).append(", ");
			//	AD_User_ID,AD_Table_ID,Record_ID,Processed
			sql.append(AD_User_ID).append(",").append(AD_Table_ID).append(",")
				.append(Record_ID).append(",'N' ");
			//  Hardcoded AD_Message - AD_Message is in Text
			sql.append("FROM AD_Message WHERE Value='NoMessageFound'");
			//  Try again
			no = executeUpdate(sql.toString());
		}

		return no == 1;
	}   //  insertNote

	/*************************************************************************/

	/**
	 *	Print SQL Warnings.
	 *  <br>
	 *		Usage: DB.printWarning(rs.getWarnings(), "xx");
	 *  @param comment comment
	 *  @param warning warning
	 */
	public static void printWarning(String comment, SQLWarning warning)
	{
		if (comment == null || warning == null || comment.length() == 0)
			throw new IllegalArgumentException("DB.printException - required parameter missing");
		s_log.warn("SQL Warning: " + comment);
		if (warning == null)
			return;
		//
		SQLWarning warn = warning;
		while (warn != null)
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append(warn.getMessage());
			buffer.append("; State=").append(warn.getSQLState()).append("; ErrorCode=").append(warn.getErrorCode());
			s_log.warn(buffer.toString());
			warn = warn.getNextWarning();
		}
	}	//	printWarning

	/**
	 *  Create SQL TO Date String from Timestamp
	 *
	 *  @param  time Date to be converted
	 *  @param  dayOnly true if time set to 00:00:00
	 *
	 *  @return TO_DATE('2001-01-30 18:10:20',''YYYY-MM-DD HH24:MI:SS')
	 *      or  TO_DATE('2001-01-30',''YYYY-MM-DD')
	 */
	public static String TO_DATE (Timestamp time, boolean dayOnly)
	{
		if (time == null)
		{
			if (dayOnly)
				return "TRUNC(SysDate)";
			return "SysDate";
		}

		StringBuffer dateString = new StringBuffer("TO_DATE('");
		//  YYYY-MM-DD HH24:MI:SS.mmmm  JDBC Timestamp format
		String myDate = time.toString();
		if (dayOnly)
		{
			dateString.append(myDate.substring(0,10));
			dateString.append("','YYYY-MM-DD')");
		}
		else
		{
			dateString.append(myDate.substring(0, myDate.indexOf(".")));	//	cut off miliseconds
			dateString.append("','YYYY-MM-DD HH24:MI:SS')");
		}
		return dateString.toString();
	}   //  TO_DATE

	/**
	 *  Create SQL TO Date String from Timestamp
	 *  @param time time
	 *  @return TO_DATE String
	 */
	public static String TO_DATE (Timestamp time)
	{
		return TO_DATE(time, true);
	}   //  TO_DATE

	/**
	 *  Create SQL for formatted Date, Number
	 *
	 *  @see org.compiere.util.DisplayType
	 *  @see org.compiere.util.Env
	 *
	 *  @param  columnName  the column name in the SQL
	 *  @param  displayType Display Type
	 *  @param  AD_Language 6 character language setting (from Env.LANG_*)
	 *
	 *  @return TRIM(TO_CHAR(columnName,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.'''))
	 *      or TRIM(TO_CHAR(columnName,'TM9')) depending on DisplayType and Language
	 */
	public static String TO_CHAR (String columnName, int displayType, String AD_Language)
	{
		if (columnName == null || AD_Language == null || columnName.length() == 0)
			throw new IllegalArgumentException("DB.TO_CHAR - required parameter missing");

		StringBuffer retValue = new StringBuffer("TRIM(TO_CHAR(");
		retValue.append(columnName);

		//  Numbers
		if (DisplayType.isNumeric(displayType))
		{
			if (displayType == DisplayType.Amount)
				retValue.append(",'9G999G990D00'");
			else
				retValue.append(",'TM9'");
			//  TO_CHAR(GrandTotal,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.''')
			if (!Language.isDecimalPoint(AD_Language))      //  reversed
				retValue.append(",'NLS_NUMERIC_CHARACTERS='',.'''");
		}
		else if (DisplayType.isDate(displayType))
		{
			retValue.append(",'")
				.append(Language.getLanguage(AD_Language).getDBdatePattern())
				.append("'");
		}

		retValue.append("))");
		//
		return retValue.toString();
	}   //  TO_CHAR_Number


	/**
	 *  Package Strings for SQL command
	 *  @param txt  String with text
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt)
	{
		return TO_STRING (txt, 0);
	}   //  TO_STRING

	/**
	 *	Package Strings for SQL command.
	 *  <pre>
	 *		-	include in ' (single quotes)
	 *		-	replace ' with ''
	 *  </pre>
	 *  @param txt  String with text
	 *  @param maxLength    Maximum Length of content or 0 to ignore
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt, int maxLength)
	{
		if (txt == null)
			return "NULL";

		//  Length
		String text = txt;
		if (maxLength != 0 && text.length() > maxLength)
			text = txt.substring(0, maxLength);

		char quote = '\'';
		//  copy characters		(wee need to look through anyway)
		StringBuffer out = new StringBuffer();
		out.append(quote);		//	'
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (c == quote)
				out.append("''");
			else
				out.append(c);
		}
		out.append(quote);		//	'
		//
		return out.toString();
	}	//	TO_STRING

}	//	DB

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
package org.compiere.util;

import java.math.*;
import java.util.*;
import java.text.*;
import java.security.*;
import javax.swing.*;

import java.sql.*;
import javax.sql.*;
import oracle.jdbc.*;

import org.apache.log4j.Logger;

import org.compiere.Compiere;
import org.compiere.util.*;
import org.compiere.db.*;

/**
 *  General Database Interface
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB.java,v 1.22 2003/08/11 19:41:40 jjanke Exp $
 */
public final class DB
{
	/** Connection Descriptor           */
	private static CConnection      s_cc = null;
	/** Connection Cache r/o            */
	private static Connection[]		s_connections = null;
	/** Connection Cache Size           */
	private static int              s_conCacheSize = 2;   //  client
	/** Connection counter              */
	private static int              s_conCount = 0;
	/** Connection r/w                  */
	private static Connection		s_connectionRW = null;
	/**	Logger							*/
	private static Logger			s_log = Logger.getLogger (DB.class);

	/**
	 *	Client Login.
	 *  <p>
	 *  - Get Connection
	 *  - Compare User info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 * @param ctx context
	 * @param cc connection
	 * @param app_user user
	 * @param app_pwd pwd
	 * @param force ignore pwd
	 * @return  Array of Role KeyNamePair or null if error
	 * The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	protected static KeyNamePair[] login (Properties ctx,
		CConnection cc,
		String app_user, String app_pwd, boolean force)
	{
		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		//	Establish connection
		closeTarget();
		setDBTarget(cc);
		Env.setContext(ctx, "#Host", s_cc.getAppsHost());
		Env.setContext(ctx, "#Database", s_cc.getDbName());
		if (getConnectionRO() == null)
		{
			Log.saveError("NoDatabase", "");
			return null;
		}
		if (app_pwd == null)
			return null;
		//
		return loginDB (ctx, app_user, app_pwd, force);
	}   //  login

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user Principal
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx, Principal app_user)
	{
		if (app_user == null)
			return null;
		//  login w/o password as previously authorized
		return loginDB (ctx, app_user.getName(), null, false);
	}   //  app_user

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user user id
	 *  @param app_pwd password
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx,
		String app_user, String app_pwd)
	{
		if (app_pwd == null)
			return null;
		return loginDB (ctx, app_user, app_pwd, false);
	}   //  login

	/**
	 *  Actual DB login procedure.
	 *
	 *  @param ctx context
	 *  @param app_user user
	 *  @param app_pwd pwd
	 *  @param force ignore pwd
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	private static KeyNamePair[] loginDB (Properties ctx,
		String app_user, String app_pwd, boolean force)
	{
		s_log.info("login - User=" + app_user);

		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		if (app_user == null)
			return null;

		KeyNamePair[] retValue = null;
		ArrayList list = new ArrayList();
		//
		String sql = "SELECT AD_User.AD_User_ID, AD_User.Description,"
			+ " AD_Role.AD_Role_ID, AD_Role.Name "
			+ "FROM AD_User, AD_User_Roles, AD_Role "
			+ "WHERE AD_User.AD_User_ID=AD_User_Roles.AD_User_ID"
			+ " AND AD_User_Roles.AD_Role_ID=AD_Role.AD_Role_ID"
			+ " AND AD_User.Name=?"		        							//	#1
			+ " AND AD_User.IsActive='Y' AND AD_Role.IsActive='Y' AND AD_User_Roles.IsActive='Y'";
		if (app_pwd != null)
			sql += " AND (AD_User.Password=? OR AD_User.Password=?)";   	//  #2/3
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setString(1, app_user);
			if (app_pwd != null)
			{
				pstmt.setString(2, app_pwd);
				pstmt.setString(3, Secure.getDigest(app_pwd));
			}
			//	execute a query
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())		//	no record found
				if (force)
				{
					Env.setContext(ctx, "#AD_User_Name", "System");
					Env.setContext(ctx, "#AD_User_ID", "0");
					Env.setContext(ctx, "#AD_User_Description", "System Forced Login");
					Env.setContext(ctx, "#User_Level", "S  ");  	//	Format 'SCO'
					Env.setContext(ctx, "#User_Client", "0");		//	Format c1, c2, ...
					Env.setContext(ctx, "#User_Org", "0"); 		//	Format o1, o2, ...
					rs.close();
					pstmt.close();
					retValue = new KeyNamePair[] {new KeyNamePair(0, "System Administrator")};
					return retValue;
				}
				else
				{
					rs.close();
					pstmt.close();
					Log.saveError("UserPwdError", app_user, false);
					return null;
				}

			Env.setContext(ctx, "#AD_User_Name", app_user);
			Env.setContext(ctx, "#AD_User_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#SalesRep_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#AD_User_Description", rs.getString("Description"));
			//
			Ini.setProperty(Ini.P_UID, app_user);
			if (Ini.getPropertyBool(Ini.P_STORE_PWD))
				Ini.setProperty(Ini.P_PWD, app_pwd);

			do	//	read all roles
			{
				int AD_Role_ID = rs.getInt("AD_Role_ID");
				String Name = rs.getString("Name");
				KeyNamePair p = new KeyNamePair(AD_Role_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("login", ex);
			Log.saveError("DBLogin", ex.getLocalizedMessage());
			return null;
		}

		//	Change via SQL detection comes here
		Env.setContext(ctx, "#User_SecurityID", "85263");
		//
		retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# roles = " + retValue.length);
		return retValue;
	}	//	login

	/**
	 *  Load Clients.
	 *  <p>
	 *  Sets Role info in context and loads its clients
	 *
	 *  @param ctx context
	 *  @param  role    role information
	 *  @return list of valid client KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadClients (Properties ctx, KeyNamePair role)
	{
		if (ctx == null || role == null)
			throw new IllegalArgumentException("DB.loadClients - required parameter missing");

		s_log.debug("loadClients - Role=" + role.toString());

		ArrayList list = new ArrayList();
		//	get Role details
		try
		{
			String sql = "SELECT DISTINCT r.UserLevel,r.ClientList,r.OrgList,"
				+ " r.C_Currency_ID,r.AmtApproval, oa.AD_Client_ID,c.Name "
				+ "FROM AD_Role r"
				+ " INNER JOIN AD_Role_OrgAccess oa ON (r.AD_Role_ID=oa.AD_Role_ID)"
				+ " INNER JOIN AD_Client c ON (oa.AD_Client_ID=c.AD_Client_ID) "
				+ "WHERE r.AD_Role_ID=?"		//	#1
				+ " AND r.IsActive='Y' AND c.IsActive='Y'";

			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, role.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadClients - No Clients for Role=" + role.getKey());
				return null;
			}

			//  Role Info
			Env.setContext(ctx, "#AD_Role_ID", role.getKey());
			Env.setContext(ctx, "#AD_Role_Name", role.getName());
			Ini.setProperty(Ini.P_ROLE, role.getName());

			//	User Level
			Env.setContext(ctx, "#User_Level", rs.getString(1));  	//	Format 'SCO'
			//	ClientList
			Env.setContext(ctx, "#User_Client", rs.getString(2));	//	Format c1, c2, ...
			//	OrgList
			Env.setContext(ctx, "#User_Org", rs.getString(3)); 		//	Format o1, o2, ...
			//  Approval Currency / Amount
			Env.setContext(ctx, "#Approval_C_Currency_ID", rs.getInt(4));
			BigDecimal approval = rs.getBigDecimal(5);
			String approvalStr = "0";
			if (approval != null)
				approvalStr = approval.toString();
			Env.setContext(ctx, "#Approval_Amt", approvalStr);


			//  load Clients
			do
			{
				int AD_Client_ID = rs.getInt(6);
				String Name = rs.getString(7);
				KeyNamePair p = new KeyNamePair(AD_Client_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadClients", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# clients = " + retValue.length);
		return retValue;
	}   //  loadClients

	/**
	 *  Load Organizations.
	 *  <p>
	 *  Sets Client info in context and loads its organization, the role has access to
	 *
	 *  @param ctx context
	 *  @param  client    client information
	 *  @return list of valid Org KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadOrgs (Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadOrgs - required parameter missing");

		s_log.debug("loadOrgs - Client=" + client.toString());

		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Role_ID");

		ArrayList list = new ArrayList();
		int AD_Role_ID = Env.getContextAsInt(ctx,"#AD_Role_ID");
		s_log.debug("AD_Role_ID=" + AD_Role_ID);

		//	get Client details for role
		try
		{
			String sql = "SELECT c.Value,c.SMTPHost,c.IsMultiLingualDocument,c.AD_Language,"	//	1..4
				+ " o.AD_Org_ID,o.Name "				//	5..6
				+ "FROM AD_Client c"
				+ " INNER JOIN AD_Org o ON (o.AD_Client_ID=c.AD_Client_ID) "
				+ "WHERE o.AD_Client_ID=?"  			//  #1
				+ " AND o.IsSummary='N' AND o.IsActive='Y'"
				+ " AND o.AD_Org_ID IN "
				+   "(SELECT AD_Org_ID FROM AD_Role_OrgAccess WHERE AD_Role_ID=?)"; //  #2
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, client.getKey());
			pstmt.setInt(2, AD_Role_ID);
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadOrgs - No Org for Client=" + client.getKey());
				return null;
			}

			//  Client Info
			Env.setContext(ctx, "#AD_Client_ID", client.getKey());
			Env.setContext(ctx, "#AD_Client_Name", client.getName());
			Ini.setProperty(Ini.P_CLIENT, client.getName());
			//
			Env.setContext(ctx, "#Client_Value", rs.getString(1));
			Env.setContext(ctx, "#Client_SMTP", rs.getString(2));
			Env.setContext(ctx, "#IsMultiLingualDocument", rs.getString(3));

			//  load Orgs
			do
			{
				int AD_Org_ID = rs.getInt(5);
				String Name = rs.getString(6);
				KeyNamePair p = new KeyNamePair(AD_Org_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadOrgs", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# orgs = " + retValue.length);
		return retValue;
	}   //  loadOrgs

	/**
	 *	Load Preferences into Context for selected client.
	 *  <p>
	 *  Sets Org info in context and loads relevant field from
	 *	- AD_Client/Info,
	 *  - C_AcctSchema,
	 *  - C_AcctSchema_Elements
	 *	- AD_Preference
	 *  <p>
	 *  Assumes that the context is set for #AD_Client_ID, #AD_User_ID, #AD_Role_ID
	 *
	 *  @param ctx context
	 *  @param  org    org information
	 *  @param  warehouse   optional warehouse information
	 *  @param  timestamp   optional date
	 *  @param  printerName optional printer info
	 *  @returns AD_Message of error (NoValidAcctInfo) or ""
	 */
	public static String loadPreferences (Properties ctx,
		KeyNamePair org, KeyNamePair warehouse, Timestamp timestamp, String printerName)
	{
		s_log.info("loadPreferences - Org=" + org);

		if (ctx == null || org == null)
			throw new IllegalArgumentException("DB.loadPreferences - required parameter missing");
		if (Env.getContext(ctx,"#AD_Client_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Client_ID");
		if (Env.getContext(ctx,"#AD_User_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_User_ID");
		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Role_ID");


		//  Org Info - assumes that it is valid
		Env.setContext(ctx, "#AD_Org_ID", org.getKey());
		Env.setContext(ctx, "#AD_Org_Name", org.getName());
		Ini.setProperty(Ini.P_ORG, org.getName());

		//  Warehouse Info
		if (warehouse != null)
		{
			Env.setContext(ctx, "#M_Warehouse_ID", warehouse.getKey());
			Ini.setProperty(Ini.P_WAREHOUSE, warehouse.getName());
		}

		//	Date (default today)
		long today = System.currentTimeMillis();
		if (timestamp != null)
			today = timestamp.getTime();
		java.sql.Date sd = new java.sql.Date(today);
		Env.setContext(ctx, "#Date", sd.toString());	//	YYYY-MM-DD

		//	Optional Printer
		if (printerName == null)
			printerName = "";
		Env.setContext(ctx, "#Printer", printerName);
		Ini.setProperty(Ini.P_PRINTER, printerName);

		//	Other
		Env.setAutoCommit(ctx, Ini.getProperty(Ini.P_A_COMMIT).equals("Y"));
		Env.setContext(ctx, "#CompiereSys", Ini.getProperty(Ini.P_COMPIERESYS));
		Env.setContext(ctx, "#ShowAcct", Ini.getProperty(Ini.P_SHOW_ACCT));
		Env.setContext(ctx, "#ShowTrl", Ini.getProperty(Ini.P_SHOW_TRL));

		String retValue = "";
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		int AD_Org_ID =  org.getKey();
		int AD_User_ID =  Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID =  Env.getContextAsInt(ctx, "#AD_Role_ID");

		//	Other Settings
		Env.setContext(ctx, "#YYYY", "Y");
		Env.setContext(ctx, "#StdPrecision", 2);

		//	AccountSchema Info (first)
		String sql = "SELECT * "
			+ "FROM C_AcctSchema a, AD_ClientInfo c "
			+ "WHERE a.C_AcctSchema_ID=c.C_AcctSchema1_ID "
			+ "AND c.AD_Client_ID=?";
		try
		{
			int C_AcctSchema_ID = 0;
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				//  No Warning for System
				if (AD_Role_ID != 0)
					retValue = "NoValidAcctInfo";
			}
			else
			{
				//	Accounting Info
				C_AcctSchema_ID = rs.getInt("C_AcctSchema_ID");
				Env.setContext(ctx, "$C_AcctSchema_ID", C_AcctSchema_ID);
				Env.setContext(ctx, "$C_Currency_ID", rs.getInt("C_Currency_ID"));
				Env.setContext(ctx, "$HasAlias", rs.getString("HasAlias"));
			}
			rs.close();
			pstmt.close();

			//	Accounting Elements
			sql = "SELECT ElementType "
				+ "FROM C_AcctSchema_Element "
				+ "WHERE C_AcctSchema_ID=?"
				+ " AND IsActive='Y'";
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, C_AcctSchema_ID);
			rs = pstmt.executeQuery();
			while (rs.next())
				Env.setContext(ctx, "$Element_" + rs.getString("ElementType"), "Y");
			rs.close();
			pstmt.close();

			//	This reads all relevant window neutral defaults
			//	overwriting superseeded ones.  Window specific is read in Mainain
			sql = "SELECT Attribute, Value, AD_Window_ID "
				+ "FROM AD_Preference "
				+ "WHERE AD_Client_ID IN (0, @#AD_Client_ID@)"
				+ " AND AD_Org_ID IN (0, @#AD_Org_ID@)"
				+ " AND (AD_User_ID IS NULL OR AD_User_ID=0 OR AD_User_ID=@#AD_User_ID@)"
				+ " AND IsActive='Y' "
				+ "ORDER BY Attribute, AD_Client_ID, AD_User_ID DESC, AD_Org_ID";
				//	the last one overwrites - System - Client - User - Org - Window
			sql = Env.parseContext(ctx, 0, sql, false);
			if (sql.length() == 0)
				s_log.error("loadPreferences - Missing Environment");
			else
			{
				pstmt = prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					int AD_Window_ID = rs.getInt(3);
					String at = "";
					if (rs.wasNull())
						at = "P|" + rs.getString(1);
					else
						at = "P" + AD_Window_ID + "|" + rs.getString(1);
					String va = rs.getString(2);
					Env.setContext(ctx, at, va);
				}
				rs.close();
				pstmt.close();
			}
		}
		catch (SQLException ex)
		{
			s_log.error("loadPreferences (" + sql + ")", ex);
		}

		//	Default Values
		s_log.info("Default Values ...");
		sql = "SELECT t.TableName, c.ColumnName "
			+ "FROM AD_Column c "
			+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID) "
			+ "WHERE c.IsKey='Y' AND t.IsActive='Y'"
			+ " AND EXISTS (SELECT * FROM AD_Column cc "
			+ " WHERE ColumnName = 'IsDefault' AND t.AD_Table_ID=cc.AD_Table_ID AND cc.IsActive='Y')";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				loadDefault (ctx, rs.getString(1), rs.getString(2));
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("loadPreferences", e);
		}
		//
		Ini.saveProperties(Ini.isClient());
		//
		return retValue;
	}	//	loadPreferences

	/**
	 *	Load Default Value for Table into Context.
	 *
	 *  @param ctx context
	 *  @param TableName table name
	 *  @param ColumnName column name
	 */
	private static void loadDefault (Properties ctx, String TableName, String ColumnName)
	{
		String value = null;
		//
		String SQL = "SELECT " + ColumnName + " FROM " + TableName
			+ " WHERE IsDefault='Y' AND IsActive='Y' ORDER BY AD_Client_ID";
		SQL = Access.addROAccessSQL(ctx, SQL, TableName, false);
		try
		{
			PreparedStatement pstmt = prepareStatement(SQL);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())		//	overwrites system defaults
				value = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("loadDefault - " + TableName + " (" + SQL + ")", e);
			return;
		}
		//	Set Context Value
		if (value != null && value.length() != 0)
		{
			if (TableName.equals("C_DocType"))
				Env.setContext(ctx, "#C_DocTypeTarget_ID", value);
			else
				Env.setContext(ctx, "#" + ColumnName, value);
		}
	}	//	loadDefault

	/**
	 *  Load Warehouses
	 *
	 * @param ctx context
	 * @param client client
	 * @return Array of Warehouse Info
	 */
	public static KeyNamePair[] loadWarehouses(Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadWarehouses - required parameter missing");

		s_log.info("loadWarehouses - Client=" + client.toString());

		ArrayList list = new ArrayList();
		try
		{
			String sql = "SELECT M_Warehouse_ID, Name FROM M_Warehouse "
				+ "WHERE AD_Client_ID=? AND IsActive='Y'";  //  #1

			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, client.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.warn("loadWarehouses - No Warehouses for Client=" + client.getKey());
				return null;
			}

			//  load Warehousess
			do
			{
				int AD_Warehouse_ID = rs.getInt(1);
				String Name = rs.getString(2);
				KeyNamePair p = new KeyNamePair(AD_Warehouse_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadWarehouses", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.info("# warehouses = " + retValue.length);
		return retValue;
	}   //  loadWarehouses

	/*************************************************************************/

	/**
	 *  Set connection
	 *  @param cc connection
	 */
	public static void setDBTarget (CConnection cc)
	{
		if (cc == null)
			throw new IllegalArgumentException("DB.setDBTarget connection is NULL");

		if (s_cc == null)
			s_cc = cc;
		synchronized (s_cc)    //  use as mutex
		{
			s_cc = cc;
			//  Closing existing
			if (s_connections != null)
			{
				for (int i = 0; i < s_connections.length; i++)
				{
					try {
						s_connections[i].close();
					} catch (Exception e) {}
				}
			}
			s_connections = null;
		}
	}   //  setDBTarget

	/**
	 *  Is there a connection to the database ?
	 *  @return true, if connected to database
	 */
	public static boolean isConnected()
	{
		try
		{
			getConnectionRW();	//	try to get a connection
			return true;
		}
		catch (Exception e)
		{
		}
		return false;
	}   //  isConnected

	/**
	 *	Return (pooled) r/w AutoCommit, read committed connection
	 *
	 *  @return Connection (r/w)
	 */
	public static Connection getConnectionRW ()
	{
		//	check health of connection
		try
		{
			if (s_connectionRW == null)
				;
			else if (s_connectionRW.isClosed())
				s_connectionRW = null;
			else if (s_connectionRW instanceof OracleConnection && ((OracleConnection)s_connectionRW).pingDatabase(1) < 0)
				s_connectionRW = null;
			else
				 s_connectionRW.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		catch (Exception e)
		{
			s_connectionRW = null;
		}
		if (s_connectionRW == null)
			s_connectionRW = s_cc.getConnection (true, Connection.TRANSACTION_READ_COMMITTED);
		if (s_connectionRW == null)
			throw new UnsupportedOperationException("DB.getConnectionRW - @NoDBConnection@");
		return s_connectionRW;
	}   //  getConnectionRW

	/**
	 *	Return read committed, read/only connection with AutoCommit from pool
	 *  @return Connection (r/o)
	 */
	public static Connection getConnectionRO ()
	{
		try
		{
			synchronized (s_cc)    //  use as mutex as s_connection is null the first time
			{
				if (s_connections == null)
					s_connections = createConnections (Connection.TRANSACTION_READ_COMMITTED);     //  see below
			}
		}
		catch (Exception e)
		{
			s_log.error("getConnectionRO", e);
		}

		//  check health of connection
		int pos = s_conCount++;
		Connection connection = s_connections[pos % s_conCacheSize];
		try
		{
			if (connection == null)
				;
			else if (connection.isClosed())
				connection = null;
			else if (connection instanceof OracleConnection && ((OracleConnection)connection).pingDatabase(1) < 0)
				connection = null;
			else
				 connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		catch (Exception e)
		{
			connection = null;
		}
		//	Get new
		if (connection == null)
		{
			System.out.println("DB.getConnectionRO - replacing connection #" + pos % s_conCacheSize);
		//	s_log.error("getConnectionRO - replacing connection #" + pos % s_conCacheSize);
			connection = s_cc.getConnection (true, Connection.TRANSACTION_READ_COMMITTED);            //  see above
		/*	try
			{
				retValue.setReadOnly(true);     //  not supported by Oracle
			}
			catch (Exception e)
			{
				System.err.println("DB.getConnectionRO - Cannot set to R/O - " + e);
			}   */
			s_connections[pos % s_conCacheSize] = connection;
		}
		if (connection == null)
			throw new UnsupportedOperationException("DB.getConnectionRO - @NoDBConnection@");
		return connection;
	}	//	getConnectionRO

	/**
	 *	Create new Connection.
	 *  The connection must be closed explicitly by the application
	 *
	 *  @param autoCommit auto commit
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Connection connection
	 */
	public static Connection createConnection (boolean autoCommit, int trxLevel)
	{
		s_log.debug("createConnection " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", AutoCommit=" + autoCommit + ", TrxLevel=" + trxLevel);
		return s_cc.getConnection (autoCommit, trxLevel);
	}	//	createConnection

	/**
	 *	Create new set of r/o Connections.
	 *  R/O connection might not be supported by DB
	 *
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Array of Connections (size based on s_conCacheSize)
	 */
	private static Connection[] createConnections (int trxLevel)
	{
		s_log.debug("createConnections (" + s_conCacheSize + ") " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", TrxLevel=" + trxLevel);
		Connection cons[] = new Connection[s_conCacheSize];
		try
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				cons[i] = s_cc.getConnection (true, trxLevel);  //  auto commit
				if (cons[i] == null)
					System.err.println("createConnections - connection is NULL");	//	don't use log
			}
		}
		catch (Exception e)
		{
			//  Don't use Log
			System.err.println("DB.createConnections - " + e.getMessage());
		}
		return cons;
	}	//	createConnections

	/**
	 *  Get Database Driver.
	 *  Access to database specific functionality.
	 *  @return Compiere Database Driver
	 */
	public static CompiereDatabase getDatabase()
	{
		if (s_cc != null)
			return s_cc.getDatabase();
		return null;
	}   //  getDatabase

	/*************************************************************************/

	/**
	 *  Check database Version with Code version
	 *  @param ctx context
	 *  @return true if Database version (date) is the same
	 */
	public static boolean isDatabaseOK (Properties ctx)
	{
		//	Force loading Messages of current language
		Msg.getMsg(ctx, "0");

		//  Check Version
		String version = "?";
		String sql = "SELECT Version FROM AD_System";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				version = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("Problem with AD_System Table - Run system.sql script - " + e.toString());
			return false;
		}
		s_log.info("isDatabaseOK - DB_Version=" + version);
		//  Identical DB version
		if (Compiere.DB_VERSION.equals(version))
			return true;

		String AD_Message = "DatabaseVersionError";
		String title = org.compiere.Compiere.getName() + " " +  Msg.getMsg(ctx, AD_Message, true);
		//	Code assumes Database version {0}, but Database has Version {1}.
		String msg = Msg.getMsg(ctx, AD_Message);	//	complete message
		msg = MessageFormat.format(msg, new Object[] {Compiere.DB_VERSION, version});
		Object[] options = { UIManager.get("OptionPane.noButtonText"), "Migrate" };
		int no = JOptionPane.showOptionDialog (null, msg,
			title, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
			UIManager.getIcon("OptionPane.errorIcon"), options, options[0]);
		if (no == 1)
		{
			try
			{
				Class.forName("com.compiere.client.StartMaintain").newInstance();
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog (null,
					ex.getMessage() + "\nSee: http://www.compiere.com/maintain",
					title, JOptionPane.ERROR_MESSAGE);
				s_log.error("isDatabaseOK - " + ex.getMessage());
			}
		}
		return false;
	}   //  isDatabaseOK

	/*************************************************************************/

	/**
	 *	Security Context Login.
	 *  @param uid uid
	 *  @param pwd pwd
	 *  @param role role
	 *  @return true if connected
	 */
	public static boolean login_context (String uid, String pwd, String role)
	{
		if (uid == null || pwd == null || role == null || uid.length() == 0 || pwd.length() == 0 || role.length() == 0)
			throw new IllegalArgumentException("DB.login_context - required parameter missing");
		s_log.info("login_context uid=" + uid);
		if (uid == null || pwd == null || role == null)
			return false;
		//
		String SQL = "{CALL Compiere_Context.Login(?,?,?,?)}";
		boolean result = true;
		try
		{
			CallableStatement cstmt = getConnectionRO().prepareCall(SQL);
			cstmt.setString(1, uid);
			cstmt.setString(2, pwd);
			cstmt.setString(3, role);
			cstmt.setString(4, Language.getBaseAD_Language());
			result = cstmt.execute();
			cstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("login_context", e);
			result = false;
		}
		return result;
	}	//	login_context

	/*************************************************************************/

	/**
	 *	Close Target
	 */
	public static void closeTarget()
	{
		if (s_connections != null)
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				try
				{
					if (s_connections[i] != null)
						s_connections[i].close();
				}
				catch (SQLException e)
				{
					s_log.warn("close connection #" + i + " - " + e.getMessage());
				}
				s_connections[i] = null;
			}
			try
			{
				if (s_connectionRW != null)
					s_connectionRW.close();
			}
			catch (SQLException e)
			{
				s_log.error("close R/W connection", e);
			}
			s_connectionRW = null;
		}
		s_connections = null;
//		EJB.close();
	}	//	closeTarget

	/*************************************************************************/

	/**
	 *	Prepare Forward Read Only Call
	 *  @param RO_SQL sql (RO)
	 *  @return Callable Statement
	 */
	public static CallableStatement prepareCall(String RO_SQL)
	{
		if (RO_SQL == null || RO_SQL.length() == 0)
			throw new IllegalArgumentException("DB.prepareCall - required parameter missing - " + RO_SQL);
		//
		String sql = getDatabase().convertStatement(RO_SQL);
		try
		{
			return getConnectionRO().prepareCall
				(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		}
		catch (SQLException e)
		{
			s_log.error("prepareCall (" + sql + ")", e);
		}
		return null;
	}	//	prepareCall

	/*************************************************************************/

	/**
	 *	Prepare Read Only Statement
	 *  @param RO_SQL sql (RO)
	 *  @return Prepared Statement
	 */
	public static PreparedStatement prepareStatement(String RO_SQL)
	{
		return prepareStatement(RO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}	//	prepareStatement

	/**
	 *	Prepare Statement
	 *
	 *  @param SQL sql statement
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Prepared Statement r/o or r/w depending on concur
	 */
	public static PreparedStatement prepareStatement(String SQL, int resultSetType, int resultSetConcurrency)
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.prepareStatement - required parameter missing - " + SQL);
		//
		return new CompiereStatement(SQL, resultSetType, resultSetConcurrency);
		/**
		try
		{
			Connection conn = null;
			if (resultSetConcurrency == ResultSet.CONCUR_UPDATABLE)
				conn = getConnectionRW();
			else
				conn = getConnectionRO();
			return conn.prepareStatement (sql, resultSetType, resultSetConcurrency);
		}
		catch (SQLException e)
		{
			s_log.error("prepareStatement (" + sql + ")", e);
		}
		return null;
		**/
	}	//	prepareStatement

	/**
	 *	Create Read Only Statement
	 *  @return Statement
	 */
	public static Statement createStatement()
	{
		return createStatement (ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}	//	createStatement

	/**
	 *	Create Statement
	 *
	 *  @param type - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param concur - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Statement - either r/w ir r/o depending on concur
	 */
	public static Statement createStatement(int type, int concur)
	{
		try
		{
			Connection conn = null;
			if (concur == ResultSet.CONCUR_UPDATABLE)
				conn = getConnectionRW();
			else
				conn = getConnectionRO();
			return conn.createStatement(type, concur);
		}
		catch (SQLException e)
		{
			s_log.error("createStatement", e);
		}
		return null;
	}	//	createStatement

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param SQL sql
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String SQL)
	{
		return executeUpdate(SQL, false);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param SQL sql
	 * 	@param ignoreError if true, no execution error is reported
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String SQL, boolean ignoreError)
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.executeUpdate - required parameter missing - " + SQL);
		//
		String sql = getDatabase().convertStatement(SQL);
		int no = -1;
		Statement stmt = null;
		try
		{
			Connection conn = getConnectionRW();
			stmt = conn.createStatement();
			no = stmt.executeUpdate(sql);
		}
		catch (SQLException e)
		{
			if (!ignoreError)
			{
				s_log.error ("executeUpdate - " + sql, e);
				Log.saveError ("DBExecuteError", e.getLocalizedMessage ());
			}
		}
		finally
		{
			//  Always close cursor
			try
			{
				stmt.close();
			}
			catch (SQLException e2)
			{
				s_log.error("executeUpdate - cannot close statement");
			}
		}
		return no;
	}	//	execute Update


	/**
	 *	Commit - commit on RW connection.
	 *  Is not required as RW connection is AutoCommit
	 *  @param throwException if true, re-throws exception
	 *  @return true if not needed or success
	 *  @throws SQLException
	 */
	public static boolean commit (boolean throwException) throws SQLException
	{
		try
		{
			Connection conn = getConnectionRW();
		//	if (!conn.getAutoCommit())
				conn.commit();
		}
		catch (SQLException e)
		{
			s_log.error("commit", e);
			if (throwException)
				throw e;
			return false;
		}
		return true;
	}	//	commit

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @return next no
	 */
	public static int getKeyNextNo (Properties ctx, int WindowNo, String TableName)
	{
		if (ctx == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getKeyNextNo - required parameter missing");
		String CompiereSys = Env.getContext(ctx, "#CompiereSys");
		int AD_Client_ID = Env.getContextAsInt(ctx, WindowNo, "AD_Client_ID");
		//
		return getKeyNextNo (AD_Client_ID, CompiereSys, TableName);
	}   //  getKeyNextNo

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param AD_Client_ID client
	 *  @param CompiereSys compiere sys
	 *  @param TableName table name
	 *  @return next no
	 */
	public static int getKeyNextNo (int AD_Client_ID, String CompiereSys, String TableName)
	{
		if (TableName == null || CompiereSys == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getKeyNextNo - required parameter missing");
		int retValue = 0;

		//	Check CompiereSys
	//	if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
	//		throw new UnsupportedOperationException("DB.getKeyNextNo - Cannot add System records");
		//
		try
		{
			String SQL = "{CALL AD_Sequence_Next(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setString(1, TableName);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.NUMERIC);
			cstmt.executeUpdate();
			retValue = cstmt.getInt(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getKeyNextNo - Table=" + TableName + ")", e);
		}
		return retValue;
	}	//	getKeyNextNo

	/**
	 *	Get Document Number for current document.
	 *  <br>
	 *  - first search for DocType based Document No
	 *  - then Search for DocumentNo based on TableName
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @param onlyDocType Do not search for document no based on TableName
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (Properties ctx, int WindowNo, String TableName, boolean onlyDocType)
	{
		if (ctx == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getDocumentNo - required parameter missing");
		String CompiereSys = Env.getContext(ctx, "#CompiereSys");
		int AD_Client_ID = Env.getContextAsInt(ctx, WindowNo, "AD_Client_ID");

		//	Get C_DocType_ID from context - NO Defaults -
		int C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID");
		if (C_DocType_ID == 0)
			C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID");
		if (C_DocType_ID == 0)
		{
			s_log.debug("getDocumentNo - for Window=" + WindowNo
				+ " - Target=" + Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID") + "/" + Env.getContextAsInt(ctx, WindowNo, "C_DocTypeTarget_ID")
				+ " - Actual=" + Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID") + "/" + Env.getContextAsInt(ctx, WindowNo, "C_DocType_ID"));
			return getDocumentNo (AD_Client_ID, CompiereSys, TableName);
		}

		//	Check CompiereSys
		if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
			throw new UnsupportedOperationException("DB.getDocumentNo - Cannot add System records");
		//
		String retValue = getDocumentNo(AD_Client_ID, C_DocType_ID);
		if (!onlyDocType && retValue == null)
			return getDocumentNo (AD_Client_ID, CompiereSys, TableName);
		return retValue;
	}	//	getDocumentNo

	/**
	 *	Get Document Number for current document.
	 *  @param AD_Client_ID client
	 *  @param C_DocType_ID (target) document type
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (int AD_Client_ID, int C_DocType_ID)
	{
		String retValue = null;
		try
		{
			String SQL = "{CALL AD_Sequence_DocType(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setInt(1, C_DocType_ID);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.VARCHAR);
			cstmt.executeUpdate();
			retValue = cstmt.getString(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getDocumentNo - DocType=" + C_DocType_ID, e);
		}
		s_log.info("getDocumentNo - DocType=" + C_DocType_ID + " -> " + retValue);
		return retValue;
	}	//	getDocumentNo


	/**
	 *  Get Next Document No
	 *  @param AD_Client_ID client
	 *  @param CompiereSys system
	 *  @param TableName table name
	 *  @return DocumentNo
	 */
	public static String getDocumentNo (int AD_Client_ID, String CompiereSys, String TableName)
	{
		if (CompiereSys == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getDocumentNo - required parameter missing");
		//	Check CompiereSys
	//	if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
	//		throw new UnsupportedOperationException("DB.getDocumentNo - Cannot add System records");
		//
		String retValue = null;
		try
		{
			String SQL = "{CALL AD_Sequence_Doc(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setString(1, "DocumentNo_" + TableName);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.VARCHAR);
			cstmt.executeUpdate();
			retValue = cstmt.getString(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getDocumentNo  TableName=" + TableName, e);
		}
		s_log.info("getDocumentNo - TableName=" + TableName + " -> " + retValue);
		return retValue;
	}   //  getDocumentNo


	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int int_param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, String int_param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setString(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@param s_param2 parameter 2
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int int_param1, String s_param2)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			pstmt.setString(2, s_param2);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value: " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue


	/**
	 *  Convert an amount with today's spot rate
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  Amt         The amount to be converted
	 * 	@param	AD_Client_ID client
	 * 	@param	AD_Org_ID	organization
	 *  @return converted amount
	 */
	public static BigDecimal getConvertedAmt (BigDecimal Amt, int CurFrom_ID, int CurTo_ID, int AD_Client_ID, int AD_Org_ID)
	{
		return getConvertedAmt (Amt, CurFrom_ID, CurTo_ID, null, null, AD_Client_ID, AD_Org_ID);
	}   //  getConvertedAmt

	/**
	 *	Convert an amount
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  ConvDate    The Conversion date - if null - use current date
	 *  @param  RateType    The Conversion rate type - if null/empty - use Spot
	 *  @param  Amt         The amount to be converted
	 * 	@param	AD_Client_ID client
	 * 	@param	AD_Org_ID	organization
	 *  @return converted amount
	 */
	public static BigDecimal getConvertedAmt (BigDecimal Amt, int CurFrom_ID, int CurTo_ID,
		Timestamp ConvDate, String RateType, int AD_Client_ID, int AD_Org_ID)
	{
		if (Amt == null)
			throw new IllegalArgumentException("DB.getConvertedAmt - required parameter missing - Amt");
		BigDecimal retValue = null;
		if (CurFrom_ID == CurTo_ID || Amt.equals(Env.ZERO))
			return Amt;
		//
		try
		{
			String sql = "{? = call C_Currency_Convert(?,?,?,?,?, ?,?) }";
			CallableStatement cstmt = prepareCall(sql);
			//
			cstmt.registerOutParameter(1, Types.NUMERIC);
			//
			cstmt.setBigDecimal(2, Amt);					//	Amount		IN  	NUMBER
			cstmt.setInt(3, CurFrom_ID);             		//	CurFrom		IN 		NUMBER
			cstmt.setInt(4, CurTo_ID);                  	//	CurTo		IN 		NUMBER
			if (ConvDate == null)
				ConvDate = new Timestamp (System.currentTimeMillis());
			cstmt.setTimestamp(5, ConvDate);				//	ConvDate	IN 		DATE
			if (RateType == null || RateType.equals(""))
				RateType = "S";
			cstmt.setString(6, RateType);					//	RateType	IN 		CHAR
			cstmt.setInt(7, AD_Client_ID);
			cstmt.setInt(8, AD_Org_ID);
			//
			cstmt.executeUpdate();
			retValue = cstmt.getBigDecimal(1);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getConvertedAmt", e);
		}
		if (retValue == null)
			s_log.info("getConvertedAmt - not found - CurFrom=" + CurFrom_ID + ", CurTo=" + CurTo_ID
				+ ", " + ConvDate + ", " + RateType + ", Client=" + AD_Client_ID + ", Org=" + AD_Org_ID);
		return retValue;
	}	//	getConvertedAmt

	/**
	 *	Get Currency Rate
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  ConvDate    The Conversion date - if null - use current date
	 *  @param  RateType    The Conversion rate type - if null/empty - use Spot
	 * 	@param	AD_Client_ID client
	 * 	@param	AD_Org_ID	organization
	 *  @return currency Rate or null
	 */
	public static BigDecimal getConvesionRate (int CurFrom_ID, int CurTo_ID,
		Timestamp ConvDate, String RateType, int AD_Client_ID, int AD_Org_ID)
	{
		BigDecimal retValue = null;
		if (CurFrom_ID == CurTo_ID)
			return new BigDecimal(1);
		//
		try
		{
			String sql = "{? = call C_Currency_Rate(?,?,?,?,?,?)}";
			CallableStatement cstmt = prepareCall(sql);
			//
			cstmt.registerOutParameter(1, Types.NUMERIC);
			//
			cstmt.setInt(2, CurFrom_ID);             		//	CurFrom		IN 		NUMBER
			cstmt.setInt(3, CurTo_ID);                  	//	CurTo		IN 		NUMBER
			if (ConvDate == null)
				ConvDate = new Timestamp (System.currentTimeMillis());
			cstmt.setTimestamp(4, ConvDate);				//	ConvDate	IN 		DATE
			if (RateType == null || RateType.equals(""))
				RateType = "S";
			cstmt.setString(5, RateType);					//	RateType	IN 		CHAR
			cstmt.setInt(6, AD_Client_ID);
			cstmt.setInt(7, AD_Org_ID);
			//
			cstmt.executeUpdate();
			retValue = cstmt.getBigDecimal(1);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getConvesionRate", e);
		}
		if (retValue == null)
			s_log.info ("getConversionRate - not found - CurFrom=" + CurFrom_ID + ", CurTo=" + CurTo_ID
			  + ", " + ConvDate + ", " + RateType + ", Client=" + AD_Client_ID + ", Org=" + AD_Org_ID);
		return retValue;
	}	//	getConvesionRate


	/**
	 *  Get fully qualified Account Combination
	 *
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param C_AcctSchema_ID acct schema
	 *  @param Account_ID natural account
	 *  @param  base_ValidCombination_ID optional base combination to be specified
	 *  @param Alias aloas
	 *  @param AD_User_ID user
	 *  @param M_Product_ID product
	 *  @param C_BPartner_ID partner
	 *  @param AD_OrgTrx_ID trx org
	 *  @param C_LocFrom_ID loc from
	 *  @param C_LocTo_ID loc to
	 *  @param C_SRegion_ID sales region
	 *  @param C_Project_ID project
	 *  @param C_Campaign_ID campaign
	 *  @param C_Activity_ID activity
	 *  @param User1_ID user1
	 *  @param User2_ID user2
	 *  @return C_ValidCombination_ID of existing or new Combination
	 */
	public static int getValidCombination (int AD_Client_ID, int AD_Org_ID,
		int C_AcctSchema_ID, int Account_ID, int base_ValidCombination_ID, String Alias, int AD_User_ID,
		int M_Product_ID, int C_BPartner_ID, int AD_OrgTrx_ID,
		int C_LocFrom_ID, int C_LocTo_ID, int C_SRegion_ID, int C_Project_ID,
		int C_Campaign_ID, int C_Activity_ID, int User1_ID, int User2_ID)
	{
		int retValue = 0;
		StringBuffer sb = new StringBuffer ("C_ValidCombination_Get[");
		try
		{
			String sql = "{CALL C_ValidCombination_Get(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)}";
			CallableStatement cstmt = prepareCall(sql);
			//  1 - ID
			cstmt.registerOutParameter(1, Types.NUMERIC);
			sb.append("v,");
			//  --  Mandatory Accounting fields
			//	2 - AD_Client_ID
			cstmt.setInt(2, AD_Client_ID);
			sb.append("AD_Client_ID=").append(AD_Client_ID).append(",");
			//	3 - AD_Org_ID
			cstmt.setInt(3, AD_Org_ID);
			sb.append("AD_Org_ID=").append(AD_Org_ID).append(",");
			//  4- C_AcctSchema_ID
			cstmt.setInt(4, C_AcctSchema_ID);
			sb.append("C_AcctSchema_ID=").append(C_AcctSchema_ID).append(",");
			//  5 - Account_ID
			cstmt.setInt(5, Account_ID);
			sb.append("Account_ID=").append(Account_ID).append(", ");

			//  --  Optional
			//  6 - Base C_ValidCombination_ID
			if (base_ValidCombination_ID != 0)
			{
				cstmt.setInt (6, base_ValidCombination_ID);
				sb.append("BaseValidCombination_ID=").append(base_ValidCombination_ID).append(",");
			}
			else
			{
				cstmt.setNull(6, Types.NUMERIC);
				sb.append("BaseValidCombination_ID=").append("NULL,");
			}
			//  7 - MustBeFullyQualified
			cstmt.setString(7, "Y");
			sb.append("MustBeFullyQualified='Y',");
			//  8 - Alias
			if (Alias != null && Alias.length() > 0)
			{
				cstmt.setString(8, Alias);
				sb.append("Alias='").append(Alias).append("',");
			}
			else
			{
				cstmt.setNull(8, Types.VARCHAR);
				sb.append("Alias=NULL,");
			}
			//  9 - CreatedBy
			cstmt.setInt(9, AD_User_ID);
			sb.append("AD_User_ID=").append(AD_User_ID).append(", ");

			//	--  Optional Accounting fields
			//  10 - M_Product_ID
			if (M_Product_ID != 0)
			{
				cstmt.setInt(10, M_Product_ID);
				sb.append("M_Product_ID=").append(M_Product_ID).append(",");
			}
			else
			{
				cstmt.setNull(10, Types.NUMERIC);
				sb.append("M_Product_ID=NULL,");
			}
			//  11 - C_BPartner_ID
			if (C_BPartner_ID != 0)
			{
				cstmt.setInt(11, C_BPartner_ID);
				sb.append("C_BPartner_ID=").append(C_BPartner_ID).append(",");
			}
			else
			{
				cstmt.setNull(11, Types.NUMERIC);
				sb.append("C_BPartner_ID=NULL,");
			}
			//  12 - AD_OrgTrx_ID
			if (AD_OrgTrx_ID != 0)
			{
				cstmt.setInt(12, AD_OrgTrx_ID);
				sb.append("AD_OrgTrx_ID=").append(AD_OrgTrx_ID).append(",");
			}
			else
			{
				cstmt.setNull(12, Types.NUMERIC);
				sb.append("AD_OrgTrx_ID=NULL,");
			}
			//  13 - C_LocFrom_ID
			if (C_LocFrom_ID != 0)
			{
				cstmt.setInt(13, C_LocFrom_ID);
				sb.append("C_LocFrom_ID=").append(C_LocFrom_ID).append(",");
			}
			else
			{
				cstmt.setNull(13, Types.NUMERIC);
				sb.append("C_LocFrom=NULL,");
			}
			//  14 - C_LocTo_ID
			if (C_LocTo_ID != 0)
			{
				cstmt.setInt(14, (C_LocTo_ID));
				sb.append("C_LocTo_ID=").append(C_LocTo_ID).append(", ");
			}
			else
			{
				cstmt.setNull(14, Types.NUMERIC);
				sb.append("C_LocTo_ID=NULL, ");
			}
			//  15 - C_SalesRegion_ID
			if (C_SRegion_ID != 0)
			{
				cstmt.setInt(15, (C_SRegion_ID));
				sb.append("C_SalesRegion_ID=").append(C_SRegion_ID).append(",");
			}
			else
			{
				cstmt.setNull(15, Types.NUMERIC);
				sb.append("C_SalesRegion_ID=NULL,");
			}
			//  16 - C_Project_ID
			if (C_Project_ID != 0)
			{
				cstmt.setInt(16, (C_Project_ID));
				sb.append("C_Project_ID=").append(C_Project_ID).append(",");
			}
			else
			{
				cstmt.setNull(16, Types.NUMERIC);
				sb.append("C_Project_ID=NULL,");
			}
			//  17 - C_Campaign_ID
			if (C_Campaign_ID != 0)
			{
				cstmt.setInt(17, (C_Campaign_ID));
				sb.append("C_Campaign_ID=").append(C_Campaign_ID).append(",");
			}
			else
			{
				cstmt.setNull(17, Types.NUMERIC);
				sb.append("C_Campaign_ID=NULL,");
			}
			//  18 - C_Activity_ID
			if (C_Activity_ID != 0)
			{
				cstmt.setInt(18, (C_Activity_ID));
				sb.append("C_Activity_ID=").append(C_Activity_ID).append(",");
			}
			else
			{
				cstmt.setNull(18, Types.NUMERIC);
				sb.append("C_Activity_ID=NULL,");
			}
			//  19 - User1_ID
			if (User1_ID != 0)
			{
				cstmt.setInt(19, (User1_ID));
				sb.append("User1_ID=").append(User1_ID).append(",");
			}
			else
			{
				cstmt.setNull(19, Types.NUMERIC);
				sb.append("User1_ID=NULL,");
			}
			//  20 - User2_ID
			if (User2_ID != 0)
			{
				cstmt.setInt(20, (User2_ID));
				sb.append("User2_ID=").append(User2_ID).append(")");
			}
			else
			{
				cstmt.setNull(20, Types.NUMERIC);
				sb.append("User2_ID=NULL)");
			}

			//
			cstmt.executeUpdate();
			retValue = cstmt.getInt(1);     //  1 - ID
			cstmt.close();
			s_log.debug("getValidCombination " + sb.toString());
		}
		catch(SQLException e)
		{
			s_log.error("getValidCombination " + sb.toString(), e);
		}
		return retValue;
	}   //  getValidCombination

	/**
	 *  Insert Note
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param AD_User_ID user
	 *  @param AD_Table_ID table
	 *  @param Record_ID record
	 *  @param AD_MessageValue message
	 *  @param Text text
	 *  @param Reference subject
	 *  @return true if note was inserted
	 */
	public static boolean insertNote (int AD_Client_ID, int AD_Org_ID, int AD_User_ID,
		int AD_Table_ID, int Record_ID,
		String AD_MessageValue, String Text, String Reference)
	{
		if (AD_MessageValue == null || AD_MessageValue.length() == 0)
			throw new IllegalArgumentException("DB.insertNote - required parameter missing - AD_Message");

		//  Database limits
		if (Text == null)
			Text = "";
		if (Reference == null)
			Reference = "";
		//
		s_log.info("insertNote - " + AD_MessageValue + " - " + Reference);
		//
		StringBuffer sql = new StringBuffer ("INSERT INTO AD_Note (AD_Note_ID,");
		sql.append("AD_Client_ID,AD_Org_ID,IsActive, Created,CreatedBy,Updated,UpdatedBy, ")
			.append("AD_Message_ID,Text,Reference, ")
			.append("AD_User_ID,AD_Table_ID,Record_ID,Processed) ")
			.append("SELECT ");
		//
		String CompiereSys = "N";
		int AD_Note_ID = getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Note");
		sql.append(AD_Note_ID).append(",");
		//
		sql.append(AD_Client_ID).append(",")
			.append(AD_Org_ID).append(", 'Y',SysDate,")
			.append(AD_User_ID).append(",SysDate,0,");
		//	AD_Message_ID,Text,Reference,
		sql.append(" AD_Message_ID,").append(DB.TO_STRING(Text, 2000)).append(",")
			.append(DB.TO_STRING(Reference, 60)).append(", ");
		//	AD_User_ID,AD_Table_ID,Record_ID,Processed
		sql.append(AD_User_ID).append(",").append(AD_Table_ID).append(",")
			.append(Record_ID).append(",'N' ");
		//
		sql.append("FROM AD_Message WHERE Value='").append(AD_MessageValue).append("'");
		//  Create Entry
		int no = executeUpdate(sql.toString());

		//  AD_Message must exist, so if not created, it is probably
		//  due to non-existing AD_Message
		if (no == 0)
		{
			sql = new StringBuffer ("INSERT INTO AD_Note (AD_Note_ID,");
			sql.append("AD_Client_ID,AD_Org_ID,IsActive, Created,CreatedBy,Updated,UpdatedBy, ")
				.append("AD_Message_ID,Text,Reference, ")
				.append("AD_User_ID,AD_Table_ID,Record_ID,Processed) ")
				.append("SELECT ");
			//  - use AD_Note_ID from above
			sql.append(AD_Note_ID).append(",");
			//
			sql.append(AD_Client_ID).append(",")
				.append(AD_Org_ID).append(", 'Y',SysDate,")
				.append(AD_User_ID).append(",SysDate,0, ");
			//	AD_Message_ID,Text,Reference,
			sql.append("AD_Message_ID,").append(TO_STRING (AD_MessageValue + ": " + Text, 2000)).append(",")
				.append(TO_STRING(Reference,60)).append(", ");
			//	AD_User_ID,AD_Table_ID,Record_ID,Processed
			sql.append(AD_User_ID).append(",").append(AD_Table_ID).append(",")
				.append(Record_ID).append(",'N' ");
			//  Hardcoded AD_Message - AD_Message is in Text
			sql.append("FROM AD_Message WHERE Value='NoMessageFound'");
			//  Try again
			no = executeUpdate(sql.toString());
		}

		return no == 1;
	}   //  insertNote

	/*************************************************************************/

	/**
	 *	Print SQL Warnings.
	 *  <br>
	 *		Usage: DB.printWarning(rs.getWarnings(), "xx");
	 *  @param comment comment
	 *  @param warning warning
	 */
	public static void printWarning(String comment, SQLWarning warning)
	{
		if (comment == null || warning == null || comment.length() == 0)
			throw new IllegalArgumentException("DB.printException - required parameter missing");
		s_log.warn("SQL Warning: " + comment);
		if (warning == null)
			return;
		//
		SQLWarning warn = warning;
		while (warn != null)
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append(warn.getMessage());
			buffer.append("; State=").append(warn.getSQLState()).append("; ErrorCode=").append(warn.getErrorCode());
			s_log.warn(buffer.toString());
			warn = warn.getNextWarning();
		}
	}	//	printWarning

	/**
	 *  Create SQL TO Date String from Timestamp
	 *
	 *  @param  time Date to be converted
	 *  @param  dayOnly true if time set to 00:00:00
	 *
	 *  @return TO_DATE('2001-01-30 18:10:20',''YYYY-MM-DD HH24:MI:SS')
	 *      or  TO_DATE('2001-01-30',''YYYY-MM-DD')
	 */
	public static String TO_DATE (Timestamp time, boolean dayOnly)
	{
		if (time == null)
		{
			if (dayOnly)
				return "TRUNC(SysDate)";
			return "SysDate";
		}

		StringBuffer dateString = new StringBuffer("TO_DATE('");
		//  YYYY-MM-DD HH24:MI:SS.mmmm  JDBC Timestamp format
		String myDate = time.toString();
		if (dayOnly)
		{
			dateString.append(myDate.substring(0,10));
			dateString.append("','YYYY-MM-DD')");
		}
		else
		{
			dateString.append(myDate.substring(0, myDate.indexOf(".")));	//	cut off miliseconds
			dateString.append("','YYYY-MM-DD HH24:MI:SS')");
		}
		return dateString.toString();
	}   //  TO_DATE

	/**
	 *  Create SQL TO Date String from Timestamp
	 *  @param time time
	 *  @return TO_DATE String
	 */
	public static String TO_DATE (Timestamp time)
	{
		return TO_DATE(time, true);
	}   //  TO_DATE

	/**
	 *  Create SQL for formatted Date, Number
	 *
	 *  @see org.compiere.util.DisplayType
	 *  @see org.compiere.util.Env
	 *
	 *  @param  columnName  the column name in the SQL
	 *  @param  displayType Display Type
	 *  @param  AD_Language 6 character language setting (from Env.LANG_*)
	 *
	 *  @return TRIM(TO_CHAR(columnName,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.'''))
	 *      or TRIM(TO_CHAR(columnName,'TM9')) depending on DisplayType and Language
	 */
	public static String TO_CHAR (String columnName, int displayType, String AD_Language)
	{
		if (columnName == null || AD_Language == null || columnName.length() == 0)
			throw new IllegalArgumentException("DB.TO_CHAR - required parameter missing");

		StringBuffer retValue = new StringBuffer("TRIM(TO_CHAR(");
		retValue.append(columnName);

		//  Numbers
		if (DisplayType.isNumeric(displayType))
		{
			if (displayType == DisplayType.Amount)
				retValue.append(",'9G999G990D00'");
			else
				retValue.append(",'TM9'");
			//  TO_CHAR(GrandTotal,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.''')
			if (!Language.isDecimalPoint(AD_Language))      //  reversed
				retValue.append(",'NLS_NUMERIC_CHARACTERS='',.'''");
		}
		else if (DisplayType.isDate(displayType))
		{
			retValue.append(",'")
				.append(Language.getLanguage(AD_Language).getDBdatePattern())
				.append("'");
		}

		retValue.append("))");
		//
		return retValue.toString();
	}   //  TO_CHAR_Number


	/**
	 *  Package Strings for SQL command
	 *  @param txt  String with text
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt)
	{
		return TO_STRING (txt, 0);
	}   //  TO_STRING

	/**
	 *	Package Strings for SQL command.
	 *  <pre>
	 *		-	include in ' (single quotes)
	 *		-	replace ' with ''
	 *  </pre>
	 *  @param txt  String with text
	 *  @param maxLength    Maximum Length of content or 0 to ignore
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt, int maxLength)
	{
		if (txt == null)
			return "NULL";

		//  Length
		String text = txt;
		if (maxLength != 0 && text.length() > maxLength)
			text = txt.substring(0, maxLength);

		char quote = '\'';
		//  copy characters		(wee need to look through anyway)
		StringBuffer out = new StringBuffer();
		out.append(quote);		//	'
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (c == quote)
				out.append("''");
			else
				out.append(c);
		}
		out.append(quote);		//	'
		//
		return out.toString();
	}	//	TO_STRING

}	//	DB

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
package org.compiere.util;

import java.math.*;
import java.util.*;
import java.text.*;
import java.security.*;
import javax.swing.*;
import java.rmi.*;

import java.sql.*;

import javax.sql.*;
import oracle.jdbc.*;

import org.compiere.Compiere;
import org.compiere.db.*;
import org.compiere.model.*;
import org.compiere.interfaces.Server;

/**
 *  General Database Interface
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB.java,v 1.56 2004/05/13 06:08:10 jjanke Exp $
 */
public final class DB
{
	/** Connection Descriptor           */
	private static CConnection      s_cc = null;
	/** Connection Cache r/o            */
	private static Connection[]		s_connections = null;
	/** Connection Cache Size           */
	private static int              s_conCacheSize = Ini.isClient() ? 3 : 5;
	/** Connection counter              */
	private static int              s_conCount = 0;
	/** Connection r/w                  */
	private static Connection		s_connectionRW = null;
	/**	Logger							*/
	private static Logger			s_log = Logger.getCLogger (DB.class);

	/**
	 *	Client Login.
	 *  <p>
	 *  - Get Connection
	 *  - Compare User info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 * @param ctx context
	 * @param cc connection
	 * @param app_user user
	 * @param app_pwd pwd
	 * @param force ignore pwd
	 * @return  Array of Role KeyNamePair or null if error
	 * The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	protected static KeyNamePair[] login (Properties ctx,
		CConnection cc,
		String app_user, String app_pwd, boolean force)
	{
		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		//	Establish connection
		closeTarget();
		setDBTarget(cc);
		Env.setContext(ctx, "#Host", s_cc.getAppsHost());
		Env.setContext(ctx, "#Database", s_cc.getDbName());
		
		if (getConnectionRO() == null)
		{
			Log.saveError("NoDatabase", "");
			return null;
		}
		if (app_pwd == null)
			return null;
		//
		return loginDB (ctx, app_user, app_pwd, force);
	}   //  login

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user Principal
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx, Principal app_user)
	{
		if (app_user == null)
			return null;
		//  login w/o password as previously authorized
		return loginDB (ctx, app_user.getName(), null, false);
	}   //  app_user

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user user id
	 *  @param app_pwd password
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx,
		String app_user, String app_pwd)
	{
		if (app_pwd == null)
			return null;
		return loginDB (ctx, app_user, app_pwd, false);
	}   //  login

	/**
	 *  Actual DB login procedure.
	 *
	 *  @param ctx context
	 *  @param app_user user
	 *  @param app_pwd pwd
	 *  @param force ignore pwd
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	private static KeyNamePair[] loginDB (Properties ctx,
		String app_user, String app_pwd, boolean force)
	{
		s_log.info("login - User=" + app_user);

		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		if (app_user == null)
			return null;

		KeyNamePair[] retValue = null;
		ArrayList list = new ArrayList();
		//
		String sql = "SELECT AD_User.AD_User_ID, AD_User.Description,"
			+ " AD_Role.AD_Role_ID, AD_Role.Name "
			+ "FROM AD_User, AD_User_Roles, AD_Role "
			+ "WHERE AD_User.AD_User_ID=AD_User_Roles.AD_User_ID"
			+ " AND AD_User_Roles.AD_Role_ID=AD_Role.AD_Role_ID"
			+ " AND AD_User.Name=?"		        							//	#1
			+ " AND AD_User.IsActive='Y' AND AD_Role.IsActive='Y' AND AD_User_Roles.IsActive='Y'";
		if (app_pwd != null)
			sql += " AND (AD_User.Password=? OR AD_User.Password=?)";   	//  #2/3
		sql += " ORDER BY AD_Role.Name";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setString(1, app_user);
			if (app_pwd != null)
			{
				pstmt.setString(2, app_pwd);
				pstmt.setString(3, Secure.getDigest(app_pwd));
			}
			//	execute a query
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())		//	no record found
				if (force)
				{
					Env.setContext(ctx, "#AD_User_Name", "System");
					Env.setContext(ctx, "#AD_User_ID", "0");
					Env.setContext(ctx, "#AD_User_Description", "System Forced Login");
					Env.setContext(ctx, "#User_Level", "S  ");  	//	Format 'SCO'
					Env.setContext(ctx, "#User_Client", "0");		//	Format c1, c2, ...
					Env.setContext(ctx, "#User_Org", "0"); 		//	Format o1, o2, ...
					rs.close();
					pstmt.close();
					retValue = new KeyNamePair[] {new KeyNamePair(0, "System Administrator")};
					return retValue;
				}
				else
				{
					rs.close();
					pstmt.close();
					Log.saveError("UserPwdError", app_user, false);
					return null;
				}

			Env.setContext(ctx, "#AD_User_Name", app_user);
			Env.setContext(ctx, "#AD_User_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#SalesRep_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#AD_User_Description", rs.getString("Description"));
			//
			Ini.setProperty(Ini.P_UID, app_user);
			if (Ini.getPropertyBool(Ini.P_STORE_PWD))
				Ini.setProperty(Ini.P_PWD, app_pwd);

			do	//	read all roles
			{
				int AD_Role_ID = rs.getInt("AD_Role_ID");
				if (AD_Role_ID == 0)
					Env.setContext(ctx, "#SysAdmin", "Y");
				String Name = rs.getString("Name");
				KeyNamePair p = new KeyNamePair(AD_Role_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("login", ex);
			Log.saveError("DBLogin", ex);
			return null;
		}

		//	Change via SQL detection comes here
		Env.setContext(ctx, "#User_SecurityID", "85263");
		//
		retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# roles = " + retValue.length);
		return retValue;
	}	//	login

	/**
	 *  Load Clients.
	 *  <p>
	 *  Sets Role info in context and loads its clients
	 *
	 *  @param ctx context
	 *  @param  role    role information
	 *  @return list of valid client KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadClients (Properties ctx, KeyNamePair role)
	{
		if (ctx == null || role == null)
			throw new IllegalArgumentException("DB.loadClients - required parameter missing");

		s_log.debug("loadClients - Role=" + role.toString());

		ArrayList list = new ArrayList();
		//	get Role details
		try
		{
			String sql = "SELECT DISTINCT r.UserLevel,r.ClientList,r.OrgList,"
				+ " r.C_Currency_ID,r.AmtApproval, oa.AD_Client_ID,c.Name "
				+ "FROM AD_Role r"
				+ " INNER JOIN AD_Role_OrgAccess oa ON (r.AD_Role_ID=oa.AD_Role_ID)"
				+ " INNER JOIN AD_Client c ON (oa.AD_Client_ID=c.AD_Client_ID) "
				+ "WHERE r.AD_Role_ID=?"		//	#1
				+ " AND r.IsActive='Y' AND c.IsActive='Y'";

			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, role.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadClients - No Clients for Role=" + role.getKey());
				return null;
			}

			//  Role Info
			Env.setContext(ctx, "#AD_Role_ID", role.getKey());
			Env.setContext(ctx, "#AD_Role_Name", role.getName());
			Ini.setProperty(Ini.P_ROLE, role.getName());

			//	User Level
			Env.setContext(ctx, "#User_Level", rs.getString(1));  	//	Format 'SCO'
			//	ClientList
			Env.setContext(ctx, "#User_Client", rs.getString(2));	//	Format c1, c2, ...
			//	OrgList
			Env.setContext(ctx, "#User_Org", rs.getString(3)); 		//	Format o1, o2, ...
			//  Approval Currency / Amount
			Env.setContext(ctx, "#Approval_C_Currency_ID", rs.getInt(4));
			BigDecimal approval = rs.getBigDecimal(5);
			String approvalStr = "0";
			if (approval != null)
				approvalStr = approval.toString();
			Env.setContext(ctx, "#Approval_Amt", approvalStr);


			//  load Clients
			do
			{
				int AD_Client_ID = rs.getInt(6);
				String Name = rs.getString(7);
				KeyNamePair p = new KeyNamePair(AD_Client_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadClients", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# clients = " + retValue.length);
		return retValue;
	}   //  loadClients

	/**
	 *  Load Organizations.
	 *  <p>
	 *  Sets Client info in context and loads its organization, the role has access to
	 *
	 *  @param ctx context
	 *  @param  client    client information
	 *  @return list of valid Org KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadOrgs (Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadOrgs - required parameter missing");

		s_log.debug("loadOrgs - Client=" + client.toString());

		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadOrgs - Missing Comtext #AD_Role_ID");

		ArrayList list = new ArrayList();
		int AD_Role_ID = Env.getContextAsInt(ctx,"#AD_Role_ID");
		s_log.debug("AD_Role_ID=" + AD_Role_ID);
		//Begin e-evolution vpj-cd 05/09/2003 *************
		int AD_User_ID = Env.getContextAsInt(ctx,"#AD_User_ID");
		s_log.debug("AD_User_ID=" + AD_User_ID);
		//end e-evolution vpj-cd 05/09/2003 *************

		//	get Client details for role
		try
		{
			String sql = "SELECT c.Value,c.SMTPHost,c.IsMultiLingualDocument,c.AD_Language,"	//	1..4
				+ " o.AD_Org_ID,o.Name "				//	5..6
				+ "FROM AD_Client c"
				+ " INNER JOIN AD_Org o ON (o.AD_Client_ID=c.AD_Client_ID) "
				+ "WHERE o.AD_Client_ID=?"  			//  #1
				+ " AND o.IsSummary='N' AND o.IsActive='Y'"
				+ " AND o.AD_Org_ID IN "
				//Begin e-evolution vpj-cd 05/09/2003 *************
				//+   "(SELECT AD_Org_ID FROM AD_Role_OrgAccess WHERE AD_Role_ID=?)"; //  #2
				+ "(SELECT AD_Org_ID FROM AD_Role_OrgAccess WHERE AD_Role_ID=?)"  //  #2
				+ " AND (o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_User WHERE AD_User_ID=?) OR 0 IN (SELECT AD_Org_ID FROM AD_User WHERE AD_User_ID=?))" //  #3 , #4
				//end e-evolution vpj-cd 05/09/2003 *************
				+ " ORDER BY o.Name";

			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, client.getKey());
			pstmt.setInt(2, AD_Role_ID);
			//Begin e-evolution vpj-cd 05/09/2003 *************
			pstmt.setInt(3, AD_User_ID);
			pstmt.setInt(4, AD_User_ID);
			//end e-evolution vpj-cd 05/09/2003 *************
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadOrgs - No Org for Client=" + client.getKey());
				return null;
			}

			//  Client Info
			Env.setContext(ctx, "#AD_Client_ID", client.getKey());
			Env.setContext(ctx, "#AD_Client_Name", client.getName());
			Ini.setProperty(Ini.P_CLIENT, client.getName());
			//
			Env.setContext(ctx, "#Client_Value", rs.getString(1));
			Env.setContext(ctx, "#Client_SMTP", rs.getString(2));
			Env.setContext(ctx, "#IsMultiLingualDocument", rs.getString(3));

			//  load Orgs
			do
			{
				int AD_Org_ID = rs.getInt(5);
				String Name = rs.getString(6);
				KeyNamePair p = new KeyNamePair(AD_Org_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadOrgs", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# orgs = " + retValue.length);
		return retValue;
	}   //  loadOrgs

	/**
	 *	Load Preferences into Context for selected client.
	 *  <p>
	 *  Sets Org info in context and loads relevant field from
	 *	- AD_Client/Info,
	 *  - C_AcctSchema,
	 *  - C_AcctSchema_Elements
	 *	- AD_Preference
	 *  <p>
	 *  Assumes that the context is set for #AD_Client_ID, #AD_User_ID, #AD_Role_ID
	 *
	 *  @returns AD_Message of error (NoValidAcctInfo) or ""
	@param ctx context
	 *  @param  org    org information
	 *  @param  warehouse   optional warehouse information
	 *  @param  timestamp   optional date
	 *  @param  printerName optional printer info
	 *   * @return String
	 */
	public static String loadPreferences (Properties ctx,
		KeyNamePair org, KeyNamePair warehouse, Timestamp timestamp, String printerName)
	{
		s_log.info("loadPreferences - Org=" + org);

		if (ctx == null || org == null)
			throw new IllegalArgumentException("DB.loadPreferences - required parameter missing");
		if (Env.getContext(ctx,"#AD_Client_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Client_ID");
		if (Env.getContext(ctx,"#AD_User_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_User_ID");
		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Role_ID");


		//  Org Info - assumes that it is valid
		Env.setContext(ctx, "#AD_Org_ID", org.getKey());
		Env.setContext(ctx, "#AD_Org_Name", org.getName());
		Ini.setProperty(Ini.P_ORG, org.getName());

		//  Warehouse Info
		if (warehouse != null)
		{
			Env.setContext(ctx, "#M_Warehouse_ID", warehouse.getKey());
			Ini.setProperty(Ini.P_WAREHOUSE, warehouse.getName());
		}

		//	Date (default today)
		long today = System.currentTimeMillis();
		if (timestamp != null)
			today = timestamp.getTime();
		java.sql.Date sd = new java.sql.Date(today);
		Env.setContext(ctx, "#Date", sd.toString());	//	YYYY-MM-DD

		//	Optional Printer
		if (printerName == null)
			printerName = "";
		Env.setContext(ctx, "#Printer", printerName);
		Ini.setProperty(Ini.P_PRINTER, printerName);
		
		//	Load Role Info
		MRole.getDefault(ctx, true);	

		//	Other
		Env.setAutoCommit(ctx, Ini.getPropertyBool(Ini.P_A_COMMIT));
		if (MRole.getDefault(ctx, false).isShowAcct())
			Env.setContext(ctx, "#ShowAcct", Ini.getProperty(Ini.P_SHOW_ACCT));
		else
			Env.setContext(ctx, "#ShowAcct", "N");
		Env.setContext(ctx, "#ShowTrl", Ini.getProperty(Ini.P_SHOW_TRL));

		String retValue = "";
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		int AD_Org_ID =  org.getKey();
		int AD_User_ID =  Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID =  Env.getContextAsInt(ctx, "#AD_Role_ID");

		//	Other Settings
		Env.setContext(ctx, "#YYYY", "Y");
		Env.setContext(ctx, "#StdPrecision", 2);

		//	AccountSchema Info (first)
		String sql = "SELECT * "
			+ "FROM C_AcctSchema a, AD_ClientInfo c "
			+ "WHERE a.C_AcctSchema_ID=c.C_AcctSchema1_ID "
			+ "AND c.AD_Client_ID=?";
		try
		{
			int C_AcctSchema_ID = 0;
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				//  No Warning for System
				if (AD_Role_ID != 0)
					retValue = "NoValidAcctInfo";
			}
			else
			{
				//	Accounting Info
				C_AcctSchema_ID = rs.getInt("C_AcctSchema_ID");
				Env.setContext(ctx, "$C_AcctSchema_ID", C_AcctSchema_ID);
				Env.setContext(ctx, "$C_Currency_ID", rs.getInt("C_Currency_ID"));
				Env.setContext(ctx, "$HasAlias", rs.getString("HasAlias"));
			}
			rs.close();
			pstmt.close();

			//	Accounting Elements
			sql = "SELECT ElementType "
				+ "FROM C_AcctSchema_Element "
				+ "WHERE C_AcctSchema_ID=?"
				+ " AND IsActive='Y'";
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, C_AcctSchema_ID);
			rs = pstmt.executeQuery();
			while (rs.next())
				Env.setContext(ctx, "$Element_" + rs.getString("ElementType"), "Y");
			rs.close();
			pstmt.close();

			//	This reads all relevant window neutral defaults
			//	overwriting superseeded ones.  Window specific is read in Mainain
			sql = "SELECT Attribute, Value, AD_Window_ID "
				+ "FROM AD_Preference "
				+ "WHERE AD_Client_ID IN (0, @#AD_Client_ID@)"
				+ " AND AD_Org_ID IN (0, @#AD_Org_ID@)"
				+ " AND (AD_User_ID IS NULL OR AD_User_ID=0 OR AD_User_ID=@#AD_User_ID@)"
				+ " AND IsActive='Y' "
				+ "ORDER BY Attribute, AD_Client_ID, AD_User_ID DESC, AD_Org_ID";
				//	the last one overwrites - System - Client - User - Org - Window
			sql = Env.parseContext(ctx, 0, sql, false);
			if (sql.length() == 0)
				s_log.error("loadPreferences - Missing Environment");
			else
			{
				pstmt = prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					int AD_Window_ID = rs.getInt(3);
					String at = "";
					if (rs.wasNull())
						at = "P|" + rs.getString(1);
					else
						at = "P" + AD_Window_ID + "|" + rs.getString(1);
					String va = rs.getString(2);
					Env.setContext(ctx, at, va);
				}
				rs.close();
				pstmt.close();
			}
		}
		catch (SQLException ex)
		{
			s_log.error("loadPreferences (" + sql + ")", ex);
		}

		//	Default Values
		s_log.info("Default Values ...");
		sql = "SELECT t.TableName, c.ColumnName "
			+ "FROM AD_Column c "
			+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID) "
			+ "WHERE c.IsKey='Y' AND t.IsActive='Y'"
			+ " AND EXISTS (SELECT * FROM AD_Column cc "
			+ " WHERE ColumnName = 'IsDefault' AND t.AD_Table_ID=cc.AD_Table_ID AND cc.IsActive='Y')";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				loadDefault (ctx, rs.getString(1), rs.getString(2));
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("loadPreferences", e);
		}
		//
		Ini.saveProperties(Ini.isClient());
		//
		return retValue;
	}	//	loadPreferences

	/**
	 *	Load Default Value for Table into Context.
	 *
	 *  @param ctx context
	 *  @param TableName table name
	 *  @param ColumnName column name
	 */
	private static void loadDefault (Properties ctx, String TableName, String ColumnName)
	{
		String value = null;
		//
		String sql = "SELECT " + ColumnName + " FROM " + TableName	//	most specific first
			+ " WHERE IsDefault='Y' AND IsActive='Y' ORDER BY AD_Client_ID DESC, AD_Org_ID DESC";
		sql = MRole.getDefault(ctx, false).addAccessSQL(sql, 
			TableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				value = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("loadDefault - " + TableName + " (" + sql + ")", e);
			return;
		}
		//	Set Context Value
		if (value != null && value.length() != 0)
		{
			if (TableName.equals("C_DocType"))
				Env.setContext(ctx, "#C_DocTypeTarget_ID", value);
			else
				Env.setContext(ctx, "#" + ColumnName, value);
		}
	}	//	loadDefault

	/**
	 *  Load Warehouses
	 *
	 * @param ctx context
	 * @param client client
	 * @return Array of Warehouse Info
	 */
	public static KeyNamePair[] loadWarehouses(Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadWarehouses - required parameter missing");

		s_log.info("loadWarehouses - Client=" + client.toString());
		//Begin e-evolution vpj-cd 05/09/2003 *************
		int AD_User_ID = Env.getContextAsInt(ctx,"#AD_User_ID");
		s_log.debug("AD_User_ID=" + AD_User_ID);
		//end e-evolution vpj-cd 05/09/2003 *************

		ArrayList list = new ArrayList();
		try
		{
			String sql = "SELECT M_Warehouse_ID, Name FROM M_Warehouse "
				//Begin e-evolution vpj-cd 05/09/2003 *************
				//	+ "WHERE AD_Client_ID=? AND IsActive='Y'";  //  #1
				+ "WHERE AD_Client_ID=? AND IsActive='Y' "
				+ " AND (AD_Org_ID IN (SELECT AD_Org_ID FROM AD_User WHERE AD_User_ID=?) OR 0 IN (SELECT AD_Org_ID FROM AD_User WHERE AD_User_ID=?))" //#2,#3
				//end e-evolution vpj-cd 05/09/2003 *************
				+ " ORDER BY Name";
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, client.getKey());
			//Begin e-evolution vpj-cd 05/09/2003 *************
			pstmt.setInt(2, AD_User_ID);
			pstmt.setInt(3, AD_User_ID);
			//end e-evolution vpj-cd 05/09/2003 *************
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.warn("loadWarehouses - No Warehouses for Client=" + client.getKey());
				return null;
			}

			//  load Warehousess
			do
			{
				int AD_Warehouse_ID = rs.getInt(1);
				String Name = rs.getString(2);
				KeyNamePair p = new KeyNamePair(AD_Warehouse_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadWarehouses", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.info("# warehouses = " + retValue.length);
		return retValue;
	}   //  loadWarehouses

	/**************************************************************************

	/**
	 *  Set connection
	 *  @param cc connection
	 */
	public static void setDBTarget (CConnection cc)
	{
		if (cc == null)
			throw new IllegalArgumentException("DB.setDBTarget connection is NULL");

		if (s_cc == null)
			s_cc = cc;
		synchronized (s_cc)    //  use as mutex
		{
			s_cc = cc;
			//  Closing existing
			if (s_connections != null)
			{
				for (int i = 0; i < s_connections.length; i++)
				{
					try {
						s_connections[i].close();
					} catch (Exception e) {}
				}
			}
			s_connections = null;
		}
	}   //  setDBTarget

	/**
	 *  Is there a connection to the database ?
	 *  @return true, if connected to database
	 */
	public static boolean isConnected()
	{
		try
		{
			getConnectionRW();	//	try to get a connection
			return true;
		}
		catch (Exception e)
		{
		}
		return false;
	}   //  isConnected

	/**
	 *	Return (pooled) r/w AutoCommit, read committed connection.
	 *  @return Connection (r/w)
	 */
	public static Connection getConnectionRW ()
	{
		//	check health of connection
		try
		{
			if (s_connectionRW == null)
				;
			else if (s_connectionRW.isClosed())
			{
				System.out.println("DB.getConnectionRW - closed");
				s_connectionRW = null;
			}
			else if (s_connectionRW instanceof OracleConnection && ((OracleConnection)s_connectionRW).pingDatabase(1) < 0)
			{
				System.out.println("DB.getConnectionRW - no ping");
				s_connectionRW = null;
			}
			else
				 s_connectionRW.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		}
		catch (Exception e)
		{
			s_connectionRW = null;
		}
		//	Get new
		if (s_connectionRW == null)
		{
			s_connectionRW = s_cc.getConnection (true, Connection.TRANSACTION_SERIALIZABLE);
		}
		if (s_connectionRW == null)
			throw new UnsupportedOperationException("DB.getConnectionRW - @NoDBConnection@");
		return s_connectionRW;
	}   //  getConnectionRW

	/**
	 *	Return read committed, read/only from pool.
	 *  @return Connection (r/o)
	 */
	public static Connection getConnectionRO ()
	{
		try
		{
			synchronized (s_cc)    //  use as mutex as s_connection is null the first time
			{
				if (s_connections == null)
					s_connections = createConnections (Connection.TRANSACTION_READ_COMMITTED);     //  see below
			}
		}
		catch (Exception e)
		{
			s_log.error("getConnectionRO", e);
		}

		//  check health of connection
		int pos = s_conCount++;
		int connectionNo = pos % s_conCacheSize;
		Connection connection = s_connections[connectionNo];
		try
		{
			if (connection == null)
				;
			else if (connection.isClosed())
			{
			//	RowSet.close also closes connection!
			//	System.out.println("DB.getConnectionRO - closed #" + connectionNo);
				connection = null;
			}
			else if (connection instanceof OracleConnection && ((OracleConnection)connection).pingDatabase(1) < 0)
			{
				System.out.println("DB.getConnectionRO - no ping #" + connectionNo);
				connection = null;
			}
			else
				 connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		catch (Exception e)
		{
			System.out.println("DB.getConnectionRO #" + connectionNo + " - " + e.toString());
			connection = null;
		}
		//	Get new
		if (connection == null)
		{
			if (Log.isTraceLevel(8))
				s_log.debug("getConnectionRO - replacing connection #" + connectionNo);
			connection = s_cc.getConnection (true, Connection.TRANSACTION_READ_COMMITTED);            //  see above
		/*	try
			{
				retValue.setReadOnly(true);     //  not supported by Oracle
			}
			catch (Exception e)
			{
				System.err.println("DB.getConnectionRO - Cannot set to R/O - " + e);
			}   */
			s_connections[connectionNo] = connection;
		}
		if (connection == null)
			throw new UnsupportedOperationException("DB.getConnectionRO - @NoDBConnection@");
	//	System.out.println("DB.getConnectionRO - #" + connectionNo);
		return connection;
	}	//	getConnectionRO

	/**
	 *	Create new Connection.
	 *  The connection must be closed explicitly by the application
	 *
	 *  @param autoCommit auto commit
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Connection connection
	 */
	public static Connection createConnection (boolean autoCommit, int trxLevel)
	{
		s_log.debug("createConnection " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", AutoCommit=" + autoCommit + ", TrxLevel=" + trxLevel);
		return s_cc.getConnection (autoCommit, trxLevel);
	}	//	createConnection

	/**
	 *	Create new set of r/o Connections.
	 *  R/O connection might not be supported by DB
	 *
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Array of Connections (size based on s_conCacheSize)
	 */
	private static Connection[] createConnections (int trxLevel)
	{
		s_log.debug("createConnections (" + s_conCacheSize + ") " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", TrxLevel=" + trxLevel);
		Connection cons[] = new Connection[s_conCacheSize];
		try
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				cons[i] = s_cc.getConnection (true, trxLevel);  //  auto commit
				if (cons[i] == null)
					System.err.println("createConnections - connection is NULL");	//	don't use log
			}
		}
		catch (Exception e)
		{
			//  Don't use Log
			System.err.println("DB.createConnections - " + e.getMessage());
		}
		return cons;
	}	//	createConnections

	/**
	 *  Get Database Driver.
	 *  Access to database specific functionality.
	 *  @return Compiere Database Driver
	 */
	public static CompiereDatabase getDatabase()
	{
		if (s_cc != null)
			return s_cc.getDatabase();
		return null;
	}   //  getDatabase

	/**************************************************************************

	/**
	 *  Check database Version with Code version
	 *  @param ctx context
	 *  @return true if Database version (date) is the same
	 */
	public static boolean isDatabaseOK (Properties ctx)
	{
		//  Check Version
		String version = "?";
		String sql = "SELECT Version FROM AD_System";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				version = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("Problem with AD_System Table - Run system.sql script - " + e.toString());
			return false;
		}
		s_log.info("isDatabaseOK - DB_Version=" + version);
		//  Identical DB version
		if (Compiere.DB_VERSION.equals(version))
			return true;

		String AD_Message = "DatabaseVersionError";
		String title = org.compiere.Compiere.getName() + " " +  Msg.getMsg(ctx, AD_Message, true);
		//	Code assumes Database version {0}, but Database has Version {1}.
		String msg = Msg.getMsg(ctx, AD_Message);	//	complete message
		msg = MessageFormat.format(msg, new Object[] {Compiere.DB_VERSION, version});
		Object[] options = { UIManager.get("OptionPane.noButtonText"), "Migrate" };
		int no = JOptionPane.showOptionDialog (null, msg,
			title, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
			UIManager.getIcon("OptionPane.errorIcon"), options, options[0]);
		if (no == 1)
		{
			try
			{
				Class.forName("com.compiere.client.StartMaintain").newInstance();
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog (null,
					ex.getMessage() + "\nSee: http://www.compiere.com/maintain",
					title, JOptionPane.ERROR_MESSAGE);
				s_log.error("isDatabaseOK - " + ex.getMessage());
			}
		}
		return false;
	}   //  isDatabaseOK

	/**************************************************************************

	/**
	 *	Secure Context Login.
	 *  @param uid uid
	 *  @param pwd pwd
	 *  @param role role
	 *  @return true if connected
	 */
	public static boolean login_context (String uid, String pwd, String role)
	{
		if (uid == null || pwd == null || role == null || uid.length() == 0 || pwd.length() == 0 || role.length() == 0)
			throw new IllegalArgumentException("DB.login_context - required parameter missing");
		s_log.info("login_context uid=" + uid);
		if (uid == null || pwd == null || role == null)
			return false;
		//
		String SQL = "{CALL Compiere_Context.Login(?,?,?,?)}";
		boolean result = true;
		try
		{
			CallableStatement cstmt = getConnectionRO().prepareCall(SQL);
			cstmt.setString(1, uid);
			cstmt.setString(2, pwd);
			cstmt.setString(3, role);
			cstmt.setString(4, Language.getBaseAD_Language());
			result = cstmt.execute();
			cstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("login_context", e);
			result = false;
		}
		return result;
	}	//	login_context

	/**************************************************************************

	/**
	 *	Close Target
	 */
	public static void closeTarget()
	{
		if (s_connections != null)
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				try
				{
					if (s_connections[i] != null)
						s_connections[i].close();
				}
				catch (SQLException e)
				{
					s_log.warn("close connection #" + i + " - " + e.getMessage());
				}
				s_connections[i] = null;
			}
			try
			{
				if (s_connectionRW != null)
					s_connectionRW.close();
			}
			catch (SQLException e)
			{
				s_log.error("close R/W connection", e);
			}
			s_connectionRW = null;
		}
		s_connections = null;
		//
//		s_cc.getDataSource();
//		EJB.close();
	}	//	closeTarget

	/**************************************************************************

	/**
	 *	Prepare Forward Read Only Call
	 *  @param RO_SQL sql (RO)
	 *  @return Callable Statement
	 */
	public static CallableStatement prepareCall(String RO_SQL)
	{
		if (RO_SQL == null || RO_SQL.length() == 0)
			throw new IllegalArgumentException("DB.prepareCall - required parameter missing - " + RO_SQL);
		//
		String sql = getDatabase().convertStatement(RO_SQL);
		try
		{
			return getConnectionRO().prepareCall
				(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		}
		catch (SQLException e)
		{
			s_log.error("prepareCall (" + sql + ")", e);
		}
		return null;
	}	//	prepareCall

	/**************************************************************************

	/**
	 *	Prepare Read Only Statement
	 *  @param RO_SQL sql (RO)
	 *  @return Prepared Statement
	 */
	public static CPreparedStatement prepareStatement(String RO_SQL)
	{
		return prepareStatement(RO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, null);
	}	//	prepareStatement

	/**
	 *	Prepare Statement.
	 *  @param SQL sql statement
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Prepared Statement r/o or r/w depending on concur
	 */
	public static CPreparedStatement prepareStatement(String sql, 
		int resultSetType, int resultSetConcurrency)
	{
		return prepareStatement(sql, resultSetType, resultSetConcurrency, null);
	}	//	prepareStatement

	/**
	 *	Prepare Statement.
	 *  @param sql sql statement
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 * 	@param trxName transaction name
	 *  @return Prepared Statement r/o or r/w depending on concur
	 */
	public static CPreparedStatement prepareStatement(String sql, 
		int resultSetType, int resultSetConcurrency, String trxName)
	{
		if (sql == null || sql.length() == 0)
			throw new IllegalArgumentException("DB.prepareStatement - No SQL");
		//
		return new CPreparedStatement(resultSetType, resultSetConcurrency, sql, trxName);
		/**
		try
		{
			Connection conn = null;
			if (resultSetConcurrency == ResultSet.CONCUR_UPDATABLE)
				conn = getConnectionRW();
			else
				conn = getConnectionRO();
			return conn.prepareStatement (sql, resultSetType, resultSetConcurrency);
		}
		catch (SQLException e)
		{
			s_log.error("prepareStatement (" + sql + ")", e);
		}
		return null;
		**/
	}	//	prepareStatement

	/**
	 *	Create Read Only Statement
	 *  @return Statement
	 */
	public static Statement createStatement()
	{
		return createStatement (ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, null);
	}	//	createStatement

	/**
	 *	Create Statement.
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 * 	@param trxName transaction name
	 *  @return Statement - either r/w ir r/o depending on concur
	 */
	public static Statement createStatement(int resultSetType, int resultSetConcurrency, String trxName)
	{
		return new CStatement(resultSetType, resultSetConcurrency, trxName);
		/**
		try
		{
			Connection conn = null;
			if (concur == ResultSet.CONCUR_UPDATABLE)
				conn = getConnectionRW();
			else
				conn = getConnectionRO();
			return conn.createStatement(type, concur);
		}
		catch (SQLException e)
		{
			s_log.error("createStatement", e);
		}
		return null;
		**/
	}	//	createStatement

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql)
	{
		return executeUpdate(sql, false, null);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 * 	@param trxName optional transaction name
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql, String trxName)
	{
		return executeUpdate(sql, false, trxName);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 * 	@param ignoreError if true, no execution error is reported
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql, boolean ignoreError)
	{
		return executeUpdate (sql, ignoreError, null);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 * 	@param ignoreError if true, no execution error is reported
	 * 	@param trxName optional transaction name
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql, boolean ignoreError, String trxName)
	{
		if (sql == null || sql.length() == 0)
			throw new IllegalArgumentException("DB.executeUpdate - required parameter missing - " + sql);
		//
		int no = -1;
		CPreparedStatement cs = new CPreparedStatement(ResultSet.TYPE_FORWARD_ONLY, 
			ResultSet.CONCUR_UPDATABLE, sql, trxName);
		
		try
		{
			no = cs.executeUpdate();
			//	No Transaction - Commit
			if (trxName == null)
			{
				cs.commit();	//	Local commit
			//	Connection conn = cs.getConnection();
			//	if (conn != null && !conn.getAutoCommit())	//	is null for remote
			//		conn.commit();
			}
		}
		catch (SQLException e)
		{
			if (!ignoreError)
			{
				s_log.error ("executeUpdate - " + cs.getSql(), e);
				Log.saveError ("DBExecuteError", e);
			}
		}
		finally
		{
			//  Always close cursor
			try
			{
				cs.close();
			}
			catch (SQLException e2)
			{
				s_log.error("executeUpdate - cannot close statement");
			}
		}
		return no;
	}	//	executeUpdate

	/**
	 *	Execute Update and throw exxeption.
	 *  @param SQL sql
	 *  @return number of rows updated or -1 if error
	 * 	@throws SQLException
	 */
	public static int executeUpdateEx (String SQL) throws SQLException
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.executeUpdate - required parameter missing - " + SQL);
		//
		String sql = getDatabase().convertStatement(SQL);
		int no = -1;
		SQLException ex = null;
		Statement stmt = null;
		try
		{
			Connection conn = getConnectionRW();
			stmt = conn.createStatement();
			no = stmt.executeUpdate(sql);
		}
		catch (SQLException e)
		{
			ex = e;
		}
		finally
		{
			//  Always close cursor
			try
			{
				stmt.close();
			}
			catch (SQLException e2)
			{
				s_log.error("executeUpdate - cannot close statement");
			}
		}
		if (ex != null)
			throw new SQLException(ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
		return no;
	}	//	execute Update

	/**
	 *	Commit - commit on RW connection.
	 *  Is not required as RW connection is AutoCommit
	 *  @param throwException if true, re-throws exception
	 *  @return true if not needed or success
	 *  @throws SQLException
	 */
	public static boolean commit (boolean throwException) throws SQLException
	{
		try
		{
			Connection conn = getConnectionRW();
		//	if (!conn.getAutoCommit())
				conn.commit();
		}
		catch (SQLException e)
		{
			s_log.error("commit", e);
			if (throwException)
				throw e;
			return false;
		}
		return true;
	}	//	commit


	/**
	 * 	Get Row Set.
	 * 	When a Rowset is closed, it also closes the underlying connection.
	 * 	If the created RowSet is transfered by RMI, closing it makes no difference 
	 *	@param sql sql
	 *	@param local local RowSet (own connection)
	 *	@return row set or null
	 */
	public static RowSet getRowSet (String sql, boolean local)
	{
		RowSet retValue = null;
		CStatementVO info = new CStatementVO ( 
			RowSet.TYPE_SCROLL_INSENSITIVE, RowSet.CONCUR_READ_ONLY, sql);
		CPreparedStatement stmt = new CPreparedStatement(info);
		if (local)
		{
			retValue = stmt.local_getRowSet();
		}
		else
		{
			retValue = stmt.remote_getRowSet();
		}
		return retValue;
	}	//	getRowSet

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else if (Log.isTraceLevel(6))
				s_log.warn("getSQLValue - No Value " + sql);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int int_param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, String int_param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setString(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@param s_param2 parameter 2
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int int_param1, String s_param2)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			pstmt.setString(2, s_param2);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value: " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get String Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or null
	 */
	public static String getSQLValueString (String sql, int int_param1)
	{
		String retValue = null;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getString(1);
			else
				s_log.warn("getSQLValueString - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValueString - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValueString

	
	/**
	 * 	Get Array of Key Name Pairs
	 *	@param sql select with id / name as first / second column
	 *	@param optional if true (-1,"") is added 
	 *	@return array of key name pairs
	 */
	public static KeyNamePair[] getKeyNamePairs(String sql, boolean optional)
	{
		PreparedStatement pstmt = null;
		ArrayList list = new ArrayList();
		if (optional)
			list.add (new KeyNamePair(-1, ""));
		try
		{
			pstmt = DB.prepareCall(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new KeyNamePair(rs.getInt(1), rs.getString(2)));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getKeyNamePairs " + sql, e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
	//	s_log.debug("getKeyNamePairs #" + retValue.length);
		return retValue;		
	}	//	getKeyNamePairs


	/**
	 *  Get fully qualified Account Combination
	 *
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param C_AcctSchema_ID acct schema
	 *  @param Account_ID natural account
	 *  @param  base_ValidCombination_ID optional base combination to be specified
	 *  @param Alias aloas
	 *  @param AD_User_ID user
	 *  @param M_Product_ID product
	 *  @param C_BPartner_ID partner
	 *  @param AD_OrgTrx_ID trx org
	 *  @param C_LocFrom_ID loc from
	 *  @param C_LocTo_ID loc to
	 *  @param C_SRegion_ID sales region
	 *  @param C_Project_ID project
	 *  @param C_Campaign_ID campaign
	 *  @param C_Activity_ID activity
	 *  @param User1_ID user1
	 *  @param User2_ID user2
	 *  @return C_ValidCombination_ID of existing or new Combination
	 */
	public static int getValidCombination (int AD_Client_ID, int AD_Org_ID,
		int C_AcctSchema_ID, int Account_ID, int base_ValidCombination_ID, String Alias, int AD_User_ID,
		int M_Product_ID, int C_BPartner_ID, int AD_OrgTrx_ID,
		int C_LocFrom_ID, int C_LocTo_ID, int C_SRegion_ID, int C_Project_ID,
		int C_Campaign_ID, int C_Activity_ID, int User1_ID, int User2_ID)
	{
		int retValue = 0;
		StringBuffer sb = new StringBuffer ("C_ValidCombination_Get[");
		try
		{
			String sql = "{CALL C_ValidCombination_Get(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)}";
			CallableStatement cstmt = prepareCall(sql);
			//  1 - ID
			cstmt.registerOutParameter(1, Types.NUMERIC);
			sb.append("v,");
			//  --  Mandatory Accounting fields
			//	2 - AD_Client_ID
			cstmt.setInt(2, AD_Client_ID);
			sb.append("AD_Client_ID=").append(AD_Client_ID).append(",");
			//	3 - AD_Org_ID
			cstmt.setInt(3, AD_Org_ID);
			sb.append("AD_Org_ID=").append(AD_Org_ID).append(",");
			//  4- C_AcctSchema_ID
			cstmt.setInt(4, C_AcctSchema_ID);
			sb.append("C_AcctSchema_ID=").append(C_AcctSchema_ID).append(",");
			//  5 - Account_ID
			cstmt.setInt(5, Account_ID);
			sb.append("Account_ID=").append(Account_ID).append(", ");

			//  --  Optional
			//  6 - Base C_ValidCombination_ID
			if (base_ValidCombination_ID != 0)
			{
				cstmt.setInt (6, base_ValidCombination_ID);
				sb.append("BaseValidCombination_ID=").append(base_ValidCombination_ID).append(",");
			}
			else
			{
				cstmt.setNull(6, Types.NUMERIC);
				sb.append("BaseValidCombination_ID=").append("NULL,");
			}
			//  7 - MustBeFullyQualified
			cstmt.setString(7, "Y");
			sb.append("MustBeFullyQualified='Y',");
			//  8 - Alias
			if (Alias != null && Alias.length() > 0)
			{
				cstmt.setString(8, Alias);
				sb.append("Alias='").append(Alias).append("',");
			}
			else
			{
				cstmt.setNull(8, Types.VARCHAR);
				sb.append("Alias=NULL,");
			}
			//  9 - CreatedBy
			cstmt.setInt(9, AD_User_ID);
			sb.append("AD_User_ID=").append(AD_User_ID).append(", ");

			//	--  Optional Accounting fields
			//  10 - M_Product_ID
			if (M_Product_ID != 0)
			{
				cstmt.setInt(10, M_Product_ID);
				sb.append("M_Product_ID=").append(M_Product_ID).append(",");
			}
			else
			{
				cstmt.setNull(10, Types.NUMERIC);
				sb.append("M_Product_ID=NULL,");
			}
			//  11 - C_BPartner_ID
			if (C_BPartner_ID != 0)
			{
				cstmt.setInt(11, C_BPartner_ID);
				sb.append("C_BPartner_ID=").append(C_BPartner_ID).append(",");
			}
			else
			{
				cstmt.setNull(11, Types.NUMERIC);
				sb.append("C_BPartner_ID=NULL,");
			}
			//  12 - AD_OrgTrx_ID
			if (AD_OrgTrx_ID != 0)
			{
				cstmt.setInt(12, AD_OrgTrx_ID);
				sb.append("AD_OrgTrx_ID=").append(AD_OrgTrx_ID).append(",");
			}
			else
			{
				cstmt.setNull(12, Types.NUMERIC);
				sb.append("AD_OrgTrx_ID=NULL,");
			}
			//  13 - C_LocFrom_ID
			if (C_LocFrom_ID != 0)
			{
				cstmt.setInt(13, C_LocFrom_ID);
				sb.append("C_LocFrom_ID=").append(C_LocFrom_ID).append(",");
			}
			else
			{
				cstmt.setNull(13, Types.NUMERIC);
				sb.append("C_LocFrom=NULL,");
			}
			//  14 - C_LocTo_ID
			if (C_LocTo_ID != 0)
			{
				cstmt.setInt(14, (C_LocTo_ID));
				sb.append("C_LocTo_ID=").append(C_LocTo_ID).append(", ");
			}
			else
			{
				cstmt.setNull(14, Types.NUMERIC);
				sb.append("C_LocTo_ID=NULL, ");
			}
			//  15 - C_SalesRegion_ID
			if (C_SRegion_ID != 0)
			{
				cstmt.setInt(15, (C_SRegion_ID));
				sb.append("C_SalesRegion_ID=").append(C_SRegion_ID).append(",");
			}
			else
			{
				cstmt.setNull(15, Types.NUMERIC);
				sb.append("C_SalesRegion_ID=NULL,");
			}
			//  16 - C_Project_ID
			if (C_Project_ID != 0)
			{
				cstmt.setInt(16, (C_Project_ID));
				sb.append("C_Project_ID=").append(C_Project_ID).append(",");
			}
			else
			{
				cstmt.setNull(16, Types.NUMERIC);
				sb.append("C_Project_ID=NULL,");
			}
			//  17 - C_Campaign_ID
			if (C_Campaign_ID != 0)
			{
				cstmt.setInt(17, (C_Campaign_ID));
				sb.append("C_Campaign_ID=").append(C_Campaign_ID).append(",");
			}
			else
			{
				cstmt.setNull(17, Types.NUMERIC);
				sb.append("C_Campaign_ID=NULL,");
			}
			//  18 - C_Activity_ID
			if (C_Activity_ID != 0)
			{
				cstmt.setInt(18, (C_Activity_ID));
				sb.append("C_Activity_ID=").append(C_Activity_ID).append(",");
			}
			else
			{
				cstmt.setNull(18, Types.NUMERIC);
				sb.append("C_Activity_ID=NULL,");
			}
			//  19 - User1_ID
			if (User1_ID != 0)
			{
				cstmt.setInt(19, (User1_ID));
				sb.append("User1_ID=").append(User1_ID).append(",");
			}
			else
			{
				cstmt.setNull(19, Types.NUMERIC);
				sb.append("User1_ID=NULL,");
			}
			//  20 - User2_ID
			if (User2_ID != 0)
			{
				cstmt.setInt(20, (User2_ID));
				sb.append("User2_ID=").append(User2_ID).append(")");
			}
			else
			{
				cstmt.setNull(20, Types.NUMERIC);
				sb.append("User2_ID=NULL)");
			}

			//
			cstmt.executeUpdate();
			retValue = cstmt.getInt(1);     //  1 - ID
			cstmt.close();
			s_log.debug("getValidCombination " + sb.toString());
		}
		catch(SQLException e)
		{
			s_log.error("getValidCombination " + sb.toString(), e);
		}
		return retValue;
	}   //  getValidCombination
	
	
	/**************************************************************************
	 *	Get next number for Key column = 0 is Error.
	 *   * @param ctx client
	@param TableName table name
	 * 	@param trxName optionl transaction name
	 *  @return next no
	 */
	public static int getNextID (Properties ctx, String TableName, String trxName)
	{
		if (ctx == null)
			throw new IllegalArgumentException("DB.getNextID - Context missing");
		if (TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getNextID - TableName missing");
		return getNextID(Env.getAD_Client_ID(ctx), TableName, trxName);
	}	//	getNextID

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param AD_Client_ID client
	 *  @param TableName table name
	 * 	@param trxName optional Transaction Name
	 *  @return next no
	 */
	public static int getNextID (int AD_Client_ID, String TableName, String trxName)
	{
		if (isClientRemote())
		{
			Server server = CConnection.get().getServer();
			try
			{
				if (server != null)
				{
					int id = server.getNextID(AD_Client_ID, TableName, null);
					if (Log.isTraceLevel(10))
						s_log.debug("getNextID - server => " + id);
					return id;
				}
				s_log.error("getNextID - AppsServer not found - " + TableName + ", Remote=" + DB.isClientRemote());
			}
			catch (RemoteException ex)
			{
				s_log.error("getNextID - AppsServer error", ex);
			}
			//	Try locally
		}
		return MSequence.getNextID (AD_Client_ID, TableName, trxName);
	}	//	getNextID
	
	/**
	 * 	Get Document No based on Document Type
	 *	@param C_DocType_ID document type
	 * 	@param trxName optional Transaction Name
	 *	@return document no or null
	 */
	public static String getDocumentNo(int C_DocType_ID, String trxName)
	{
		if (isClientRemote())
		{
			Server server = CConnection.get().getServer();
			try
			{
				if (server != null)
				{
					String no = server.getDocumentNo (C_DocType_ID, trxName);
					if (Log.isTraceLevel(10))
						s_log.debug("getNextID - server => " + no);
					return no;
				}
				s_log.error("getDocumentNo - AppsServer not found - " + C_DocType_ID + ", Remote=" + DB.isClientRemote());
			}
			catch (RemoteException ex)
			{
				s_log.error("getDocumentNo - AppsServer error", ex);
			}
		}
		return MSequence.getDocumentNo (C_DocType_ID, trxName);
	}	//	getDocumentNo


	/**
	 * 	Get Document No from table
	 *	@param AD_Client_ID client
	 *	@param TableName table name
	 * 	@param trxName optional Transaction Name
	 *	@return document no or null
	 */
	public static String getDocumentNo(int AD_Client_ID, String TableName, String trxName)
	{
		if (isClientRemote())
		{
			Server server = CConnection.get().getServer();
			try
			{
				if (server != null)
				{
					String no = server.getDocumentNo (AD_Client_ID, TableName, trxName);
					if (Log.isTraceLevel(10))
						s_log.debug("getNextID - server => " + no);
					return no;
				}
				s_log.error("getDocumentNo - AppsServer not found - " + TableName + ", Remote=" + DB.isClientRemote());
			}
			catch (RemoteException ex)
			{
				s_log.error("getDocumentNo - AppsServer error", ex);
			}
		}
		return MSequence.getDocumentNo (AD_Client_ID, TableName, trxName);
	}	//	getDocumentNo

	/**
	 *	Get Document Number for current document.
	 *  <br>
	 *  - first search for DocType based Document No
	 *  - then Search for DocumentNo based on TableName
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @param onlyDocType Do not search for document no based on TableName
	 * 	@param trxName optional Transaction Name
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (Properties ctx, int WindowNo, 
		String TableName, boolean onlyDocType, String trxName)
	{
		if (ctx == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("getDocumentNo - required parameter missing");
		int AD_Client_ID = Env.getContextAsInt(ctx, WindowNo, "AD_Client_ID");

		//	Get C_DocType_ID from context - NO Defaults -
		int C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID");
		if (C_DocType_ID == 0)
			C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID");
		if (C_DocType_ID == 0)
		{
			s_log.debug("getDocumentNo - for Window=" + WindowNo
				+ " - Target=" + Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID") + "/" + Env.getContextAsInt(ctx, WindowNo, "C_DocTypeTarget_ID")
				+ " - Actual=" + Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID") + "/" + Env.getContextAsInt(ctx, WindowNo, "C_DocType_ID"));
			return getDocumentNo (AD_Client_ID, TableName, trxName);
		}

		String retValue = getDocumentNo (C_DocType_ID, trxName);
		if (!onlyDocType && retValue == null)
			return getDocumentNo (AD_Client_ID, TableName, trxName);
		return retValue;
	}	//	getDocumentNo

	/**
	 * 	Is this is remote client connection
	 *	@return true if client and RMI or Objects on Server
	 */
	public static boolean isClientRemote()
	{
		return Ini.isClient() 
			&& (CConnection.get().isRMIoverHTTP() || Ini.isClientObjects())
			&& CConnection.get().isAppsServerOK(false);
	}	//	isClientRemote
	
	
	
	/**************************************************************************

	/**
	 *	Print SQL Warnings.
	 *  <br>
	 *		Usage: DB.printWarning(rs.getWarnings(), "xx");
	 *  @param comment comment
	 *  @param warning warning
	 */
	public static void printWarning(String comment, SQLWarning warning)
	{
		if (comment == null || warning == null || comment.length() == 0)
			throw new IllegalArgumentException("DB.printException - required parameter missing");
		s_log.warn("SQL Warning: " + comment);
		if (warning == null)
			return;
		//
		SQLWarning warn = warning;
		while (warn != null)
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append(warn.getMessage());
			buffer.append("; State=").append(warn.getSQLState()).append("; ErrorCode=").append(warn.getErrorCode());
			s_log.warn(buffer.toString());
			warn = warn.getNextWarning();
		}
	}	//	printWarning

	/**
	 *  Create SQL TO Date String from Timestamp
	 *
	 *  @param  time Date to be converted
	 *  @param  dayOnly true if time set to 00:00:00
	 *
	 *  @return TO_DATE('2001-01-30 18:10:20',''YYYY-MM-DD HH24:MI:SS')
	 *      or  TO_DATE('2001-01-30',''YYYY-MM-DD')
	 */
	public static String TO_DATE (Timestamp time, boolean dayOnly)
	{
		if (time == null)
		{
			if (dayOnly)
				return "TRUNC(SysDate)";
			return "SysDate";
		}

		StringBuffer dateString = new StringBuffer("TO_DATE('");
		//  YYYY-MM-DD HH24:MI:SS.mmmm  JDBC Timestamp format
		String myDate = time.toString();
		if (dayOnly)
		{
			dateString.append(myDate.substring(0,10));
			dateString.append("','YYYY-MM-DD')");
		}
		else
		{
			dateString.append(myDate.substring(0, myDate.indexOf(".")));	//	cut off miliseconds
			dateString.append("','YYYY-MM-DD HH24:MI:SS')");
		}
		return dateString.toString();
	}   //  TO_DATE

	/**
	 *  Create SQL TO Date String from Timestamp
	 *  @param day day time
	 *  @return TO_DATE String
	 */
	public static String TO_DATE (Timestamp day)
	{
		return TO_DATE(day, true);
	}   //  TO_DATE

	/**
	 *  Create SQL for formatted Date, Number
	 *
	 *  @param  columnName  the column name in the SQL
	 *  @param  displayType Display Type
	 *  @param  AD_Language 6 character language setting (from Env.LANG_*)
	 *
	 *  @return TRIM(TO_CHAR(columnName,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.'''))
	 *      or TRIM(TO_CHAR(columnName,'TM9')) depending on DisplayType and Language
	@see org.compiere.util.DisplayType
	 *  @see org.compiere.util.Env
	 *
	 *   */
	public static String TO_CHAR (String columnName, int displayType, String AD_Language)
	{
		if (columnName == null || AD_Language == null || columnName.length() == 0)
			throw new IllegalArgumentException("DB.TO_CHAR - required parameter missing");

		StringBuffer retValue = new StringBuffer("TRIM(TO_CHAR(");
		retValue.append(columnName);

		//  Numbers
		if (DisplayType.isNumeric(displayType))
		{
			if (displayType == DisplayType.Amount)
				retValue.append(",'9G999G990D00'");
			else
				retValue.append(",'TM9'");
			//  TO_CHAR(GrandTotal,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.''')
			if (!Language.isDecimalPoint(AD_Language))      //  reversed
				retValue.append(",'NLS_NUMERIC_CHARACTERS='',.'''");
		}
		else if (DisplayType.isDate(displayType))
		{
			retValue.append(",'")
				.append(Language.getLanguage(AD_Language).getDBdatePattern())
				.append("'");
		}

		retValue.append("))");
		//
		return retValue.toString();
	}   //  TO_CHAR_Number


	/**
	 *  Package Strings for SQL command
	 *  @param txt  String with text
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt)
	{
		return TO_STRING (txt, 0);
	}   //  TO_STRING

	/**
	 *	Package Strings for SQL command.
	 *  <pre>
	 *		-	include in ' (single quotes)
	 *		-	replace ' with ''
	 *  </pre>
	 *  @param txt  String with text
	 *  @param maxLength    Maximum Length of content or 0 to ignore
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt, int maxLength)
	{
		if (txt == null)
			return "NULL";

		//  Length
		String text = txt;
		if (maxLength != 0 && text.length() > maxLength)
			text = txt.substring(0, maxLength);

		char quote = '\'';
		//  copy characters		(wee need to look through anyway)
		StringBuffer out = new StringBuffer();
		out.append(quote);		//	'
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (c == quote)
				out.append("''");
			else
				out.append(c);
		}
		out.append(quote);		//	'
		//
		return out.toString();
	}	//	TO_STRING

}	//	DB

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
package org.compiere.util;

import java.math.*;
import java.util.*;
import java.text.*;
import java.security.*;
import javax.swing.*;

import java.sql.*;
import javax.sql.*;
import oracle.jdbc.*;

import org.apache.log4j.Logger;

import org.compiere.Compiere;
import org.compiere.util.*;
import org.compiere.db.*;

/**
 *  General Database Interface
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB.java,v 1.4 2003/02/21 06:38:48 jjanke Exp $
 */
public final class DB
{
	/** Connection Descriptor           */
	private static CConnection      s_cc = null;
	/** Connection Cache r/o            */
	private static Connection[]		s_connections = null;
	/** Connection Cache Size           */
	private static int              s_conCacheSize = 2;   //  client
	/** Connection counter              */
	private static int              s_conCount = 0;
	/** Connection r/w                  */
	private static Connection		s_connectionRW = null;
	/**	Logger							*/
	private static Logger			s_log = Logger.getLogger (DB.class);

	/**
	 *	Client Login.
	 *  <p>
	 *  - Get Connection
	 *  - Compare User info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 * @param ctx context
	 * @param cc connection
	 * @param app_user user
	 * @param app_pwd pwd
	 * @param force ignore pwd
	 * @return  Array of Role KeyNamePair or null if error
	 * The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	protected static KeyNamePair[] login (Properties ctx,
		CConnection cc,
		String app_user, String app_pwd, boolean force)
	{
		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		//	Establish connection
		DB.closeTarget();
		setDBTarget(cc);
		Env.setContext(ctx, "#Host", s_cc.getAppsHost());
		Env.setContext(ctx, "#Database", s_cc.getDbName());
		if (DB.getConnectionRO() == null)
		{
			Log.saveError("NoDatabase", "");
			return null;
		}
		if (app_pwd == null)
			return null;
		//
		return loginDB (ctx, app_user, app_pwd, force);
	}   //  login

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user Principal
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx, Principal app_user)
	{
		if (app_user == null)
			return null;
		//  login w/o password as previously authorized
		return loginDB (ctx, app_user.getName(), null, false);
	}   //  app_user

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user user id
	 *  @param app_pwd password
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx,
		String app_user, String app_pwd)
	{
		if (app_pwd == null)
			return null;
		return loginDB (ctx, app_user, app_pwd, false);
	}   //  login

	/**
	 *  Actual DB login procedure.
	 *
	 *  @param ctx context
	 *  @param app_user user
	 *  @param app_pwd pwd
	 *  @param force ignore pwd
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	private static KeyNamePair[] loginDB (Properties ctx,
		String app_user, String app_pwd, boolean force)
	{
		s_log.info("login - User=" + app_user);

		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		if (app_user == null)
			return null;

		KeyNamePair[] retValue = null;
		ArrayList list = new ArrayList();
		//
		String sql = "SELECT AD_User.AD_User_ID, AD_User.Description,"
			+ " AD_Role.AD_Role_ID, AD_Role.Name "
			+ "FROM AD_User, AD_User_Roles, AD_Role "
			+ "WHERE AD_User.AD_User_ID=AD_User_Roles.AD_User_ID"
			+ " AND AD_User_Roles.AD_Role_ID=AD_Role.AD_Role_ID"
			+ " AND AD_User.Name=?"		        							//	#1
			+ " AND AD_User.IsActive='Y' AND AD_Role.IsActive='Y' AND AD_User_Roles.IsActive='Y'";
		if (app_pwd != null)
			sql += " AND (AD_User.Password=? OR AD_User.Password=?)";   	//  #2/3
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setString(1, app_user);
			if (app_pwd != null)
			{
				pstmt.setString(2, app_pwd);
				pstmt.setString(3, Secure.getDigest(app_pwd));
			}
			//	execute a query
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())		//	no record found
				if (force)
				{
					Env.setContext(ctx, "#AD_User_Name", "System");
					Env.setContext(ctx, "#AD_User_ID", "0");
					Env.setContext(ctx, "#AD_User_Description", "System Forced Login");
					Env.setContext(ctx, "#User_Level", "S  ");  	//	Format 'SCO'
					Env.setContext(ctx, "#User_Client", "0");		//	Format c1, c2, ...
					Env.setContext(ctx, "#User_Org", "0"); 		//	Format o1, o2, ...
					rs.close();
					pstmt.close();
					retValue = new KeyNamePair[] {new KeyNamePair(0, "System Administrator")};
					return retValue;
				}
				else
				{
					rs.close();
					pstmt.close();
					Log.saveError("UserPwdError", app_user, false);
					return null;
				}

			Env.setContext(ctx, "#AD_User_Name", app_user);
			Env.setContext(ctx, "#AD_User_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#SalesRep_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#AD_User_Description", rs.getString("Description"));
			//
			Ini.setProperty(Ini.P_UID, app_user);
			if (Ini.getPropertyBool(Ini.P_STORE_PWD))
				Ini.setProperty(Ini.P_PWD, app_pwd);

			do	//	read all roles
			{
				int AD_Role_ID = rs.getInt("AD_Role_ID");
				String Name = rs.getString("Name");
				KeyNamePair p = new KeyNamePair(AD_Role_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			Log.saveError("DBLogin", ex.getLocalizedMessage());
			return null;
		}

		//	Change via SQL detection comes here
		Env.setContext(ctx, "#User_SecurityID", "85263");
		//
		retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# roles = " + retValue.length);
		return retValue;
	}	//	login

	/**
	 *  Load Clients.
	 *  <p>
	 *  Sets Role info in context and loads its clients
	 *
	 *  @param ctx context
	 *  @param  role    role information
	 *  @return list of valid client KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadClients (Properties ctx, KeyNamePair role)
	{
		if (ctx == null || role == null)
			throw new IllegalArgumentException("DB.loadClients - required parameter missing");

		s_log.debug("loadClients - Role=" + role.toString());

		ArrayList list = new ArrayList();
		//	get Role details
		try
		{
			String sql = "SELECT r.UserLevel,r.ClientList,r.OrgList,r.C_Currency_ID,r.AmtApproval,"
				+ " ca.AD_Client_ID, c.Name "
				+ "FROM AD_Role r, AD_Role_ClientAccess ca, AD_Client c "
				+ "WHERE r.AD_Role_ID=?"    // #1
				+ " AND r.IsActive='Y' AND c.IsActive='Y'"
				+ " AND r.AD_Role_ID=ca.AD_Role_ID"
				+ " AND ca.AD_Client_ID=c.AD_Client_ID";

			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, role.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadClients - No Clients for Role=" + role.getKey());
				return null;
			}

			//  Role Info
			Env.setContext(ctx, "#AD_Role_ID", role.getKey());
			Env.setContext(ctx, "#AD_Role_Name", role.getName());
			Ini.setProperty(Ini.P_ROLE, role.getName());

			//	User Level
			Env.setContext(ctx, "#User_Level", rs.getString(1));  	//	Format 'SCO'
			//	ClientList
			Env.setContext(ctx, "#User_Client", rs.getString(2));	//	Format c1, c2, ...
			//	OrgList
			Env.setContext(ctx, "#User_Org", rs.getString(3)); 		//	Format o1, o2, ...
			//  Approval Currency / Amount
			Env.setContext(ctx, "#Approval_C_Currency_ID", rs.getInt(4));
			BigDecimal approval = rs.getBigDecimal(5);
			String approvalStr = "0";
			if (approval != null)
				approvalStr = approval.toString();
			Env.setContext(ctx, "#Approval_Amt", approvalStr);


			//  load Clients
			do
			{
				int AD_Client_ID = rs.getInt(6);
				String Name = rs.getString(7);
				KeyNamePair p = new KeyNamePair(AD_Client_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadClients", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# clients = " + retValue.length);
		return retValue;
	}   //  loadClients

	/**
	 *  Load Organizations.
	 *  <p>
	 *  Sets Client info in context and loads its organization, the role has access to
	 *
	 *  @param ctx context
	 *  @param  client    client information
	 *  @return list of valid Org KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadOrgs (Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadOrgs - required parameter missing");

		s_log.debug("loadOrgs - Client=" + client.toString());

		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Role_ID");

		ArrayList list = new ArrayList();
		int AD_Role_ID = Env.getContextAsInt(ctx,"#AD_Role_ID");
		s_log.debug("AD_Role_ID=" + AD_Role_ID);

		//	get Client details for role
		try
		{
			String sql = "SELECT c.Value,c.SMTPHost,c.IsMultiLingualDocument,c.AD_Language,"	//	1..4
				+ " o.AD_Org_ID,o.Name "				//	5..6
				+ "FROM AD_Client c"
				+ " INNER JOIN AD_Org o ON (o.AD_Client_ID=c.AD_Client_ID) "
				+ "WHERE o.AD_Client_ID=?"  			//  #1
				+ " AND o.IsSummary='N' AND o.IsActive='Y'"
				+ " AND o.AD_Org_ID IN "
				+   "(SELECT AD_Org_ID FROM AD_Role_OrgAccess WHERE AD_Role_ID=?)"; //  #2
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, client.getKey());
			pstmt.setInt(2, AD_Role_ID);
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadOrgs - No Org for Client=" + client.getKey());
				return null;
			}

			//  Client Info
			Env.setContext(ctx, "#AD_Client_ID", client.getKey());
			Env.setContext(ctx, "#AD_Client_Name", client.getName());
			Ini.setProperty(Ini.P_CLIENT, client.getName());
			//
			Env.setContext(ctx, "#Client_Value", rs.getString(1));
			Env.setContext(ctx, "#Client_SMTP", rs.getString(2));
			Env.setContext(ctx, "#IsMultiLingualDocument", rs.getString(3));

			//  load Orgs
			do
			{
				int AD_Org_ID = rs.getInt(5);
				String Name = rs.getString(6);
				KeyNamePair p = new KeyNamePair(AD_Org_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadOrgs", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# orgs = " + retValue.length);
		return retValue;
	}   //  loadOrgs

	/**
	 *	Load Preferences into Context for selected client.
	 *  <p>
	 *  Sets Org info in context and loads relevant field from
	 *	- AD_Client/Info,
	 *  - C_AcctSchema,
	 *  - C_AcctSchema_Elements
	 *	- AD_Preference
	 *  <p>
	 *  Assumes that the context is set for #AD_Client_ID, #AD_User_ID, #AD_Role_ID
	 *
	 *  @param ctx context
	 *  @param  org    org information
	 *  @param  warehouse   optional warehouse information
	 *  @param  timestamp   optional date
	 *  @param  printerName optional printer info
	 *  @returns AD_Message of error (NoValidAcctInfo) or ""
	 */
	public static String loadPreferences (Properties ctx,
		KeyNamePair org, KeyNamePair warehouse, Timestamp timestamp, String printerName)
	{
		Log.trace(Log.l3_Util, "DB.loadPreferences - Org=" + org);

		if (ctx == null || org == null)
			throw new IllegalArgumentException("DB.loadPreferences - required parameter missing");
		if (Env.getContext(ctx,"#AD_Client_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Client_ID");
		if (Env.getContext(ctx,"#AD_User_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_User_ID");
		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Role_ID");


		//  Org Info - assumes that it is valid
		Env.setContext(ctx, "#AD_Org_ID", org.getKey());
		Env.setContext(ctx, "#AD_Org_Name", org.getName());
		Ini.setProperty(Ini.P_ORG, org.getName());

		//  Warehouse Info
		if (warehouse != null)
		{
			Env.setContext(ctx, "#M_Warehouse_ID", warehouse.getKey());
			Ini.setProperty(Ini.P_WAREHOUSE, warehouse.getName());
		}

		//	Date (default today)
		long today = System.currentTimeMillis();
		if (timestamp != null)
			today = timestamp.getTime();
		java.sql.Date sd = new java.sql.Date(today);
		Env.setContext(ctx, "#Date", sd.toString());	//	YYYY-MM-DD

		//	Optional Printer
		if (printerName == null)
			printerName = "";
		Env.setContext(ctx, "#Printer", printerName);
		Ini.setProperty(Ini.P_PRINTER, printerName);

		//	Other
		Env.setAutoCommit(ctx, Ini.getProperty(Ini.P_A_COMMIT).equals("Y"));
		Env.setContext(ctx, "#CompiereSys", Ini.getProperty(Ini.P_COMPIERESYS));
		Env.setContext(ctx, "#ShowAcct", Ini.getProperty(Ini.P_SHOW_ACCT));
		Env.setContext(ctx, "#ShowTrl", Ini.getProperty(Ini.P_SHOW_TRL));

		String retValue = "";
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		int AD_Org_ID =  org.getKey();
		int AD_User_ID =  Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID =  Env.getContextAsInt(ctx, "#AD_Role_ID");

		//	Other Settings
		Env.setContext(ctx, "#YYYY", "Y");
		Env.setContext(ctx, "#StdPrecision", 2);

		//	AccountSchema Info (first)
		String sql = "SELECT * "
			+ "FROM C_AcctSchema a, AD_ClientInfo c "
			+ "WHERE a.C_AcctSchema_ID=c.C_AcctSchema1_ID "
			+ "AND c.AD_Client_ID=?";
		try
		{
			int C_AcctSchema_ID = 0;
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				//  No Warning for System
				if (AD_Role_ID != 0)
					retValue = "NoValidAcctInfo";
			}
			else
			{
				//	Accounting Info
				C_AcctSchema_ID = rs.getInt("C_AcctSchema_ID");
				Env.setContext(ctx, "$C_AcctSchema_ID", C_AcctSchema_ID);
				Env.setContext(ctx, "$C_Currency_ID", rs.getInt("C_Currency_ID"));
				Env.setContext(ctx, "$HasAlias", rs.getString("HasAlias"));
			}
			rs.close();
			pstmt.close();

			//	Accounting Elements
			sql = "SELECT ElementType "
				+ "FROM C_AcctSchema_Element "
				+ "WHERE C_AcctSchema_ID=?"
				+ " AND IsActive='Y'";
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, C_AcctSchema_ID);
			rs = pstmt.executeQuery();
			while (rs.next())
				Env.setContext(ctx, "$Element_" + rs.getString("ElementType"), "Y");
			rs.close();
			pstmt.close();

			//	This reads all relevant window neutral defaults
			//	overwriting superseeded ones.  Window specific is read in Mainain
			sql = "SELECT Attribute, Value, AD_Window_ID "
				+ "FROM AD_Preference "
				+ "WHERE AD_Client_ID IN (0, @#AD_Client_ID@)"
				+ " AND AD_Org_ID IN (0, @#AD_Org_ID@)"
				+ " AND (AD_User_ID IS NULL OR AD_User_ID=0 OR AD_User_ID=@#AD_User_ID@)"
				+ " AND IsActive='Y' "
				+ "ORDER BY Attribute, AD_Client_ID, AD_User_ID DESC, AD_Org_ID";
				//	the last one overwrites - System - Client - User - Org - Window
			sql = Env.parseContext(ctx, 0, sql, false);
			if (sql.length() == 0)
				s_log.error("loadPreferences - Missing Environment");
			else
			{
				pstmt = prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					int AD_Window_ID = rs.getInt(3);
					String at = "";
					if (rs.wasNull())
						at = "P|" + rs.getString(1);
					else
						at = "P" + AD_Window_ID + "|" + rs.getString(1);
					String va = rs.getString(2);
					Env.setContext(ctx, at, va);
				}
				rs.close();
				pstmt.close();
			}
		}
		catch (SQLException ex)
		{
			s_log.error("loadPreferences (" + sql + ")", ex);
		}

		//	Default Values
		Log.trace(Log.l4_Data, "Default Values ...");
		sql = "SELECT t.TableName, c.ColumnName "
			+ "FROM AD_Column c "
			+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID) "
			+ "WHERE c.IsKey='Y' AND t.IsActive='Y'"
			+ " AND EXISTS (SELECT * FROM AD_Column cc "
			+ " WHERE ColumnName = 'IsDefault' AND t.AD_Table_ID=cc.AD_Table_ID AND cc.IsActive='Y')";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				loadDefault (ctx, rs.getString(1), rs.getString(2));
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("loadPreferences", e);
		}
		//
		Ini.saveProperties(Ini.isClient());
		//
		return retValue;
	}	//	loadPreferences

	/**
	 *	Load Default Value for Table into Context.
	 *
	 *  @param ctx context
	 *  @param TableName table name
	 *  @param ColumnName column name
	 */
	private static void loadDefault (Properties ctx, String TableName, String ColumnName)
	{
		String value = null;
		//
		String SQL = "SELECT " + ColumnName + " FROM " + TableName
			+ " WHERE IsDefault='Y' AND IsActive='Y' ORDER BY AD_Client_ID";
		SQL = Access.addROAccessSQL(ctx, SQL, TableName, false);
		try
		{
			PreparedStatement pstmt = prepareStatement(SQL);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())		//	overwrites system defaults
				value = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("loadDefault - " + TableName + " (" + SQL + ")", e);
			return;
		}
		//	Set Context Value
		if (value != null && value.length() != 0)
		{
			if (TableName.equals("C_DocType"))
				Env.setContext(ctx, "#C_DocTypeTarget_ID", value);
			else
				Env.setContext(ctx, "#" + ColumnName, value);
		}
	}	//	loadDefault

	/**
	 *  Load Warehouses
	 *
	 * @param ctx context
	 * @param client client
	 * @return Array of Warehouse Info
	 */
	public static KeyNamePair[] loadWarehouses(Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadWarehouses - required parameter missing");

		Log.trace(Log.l3_Util, "DB.loadWarehouses - Client=" + client.toString());

		ArrayList list = new ArrayList();
		try
		{
			String sql = "SELECT M_Warehouse_ID, Name FROM M_Warehouse "
				+ "WHERE AD_Client_ID=? AND IsActive='Y'";  //  #1

			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, client.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				Log.trace(Log.l3_Util, "DB.loadWarehouses - No Warehouses for Client=" + client.getKey());
				return null;
			}

			//  load Warehousess
			do
			{
				int AD_Warehouse_ID = rs.getInt(1);
				String Name = rs.getString(2);
				KeyNamePair p = new KeyNamePair(AD_Warehouse_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadWarehouses", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		Log.trace(Log.l6_Database, "# warehouses = " + retValue.length);
		return retValue;
	}   //  loadWarehouses

	/*************************************************************************/

	/**
	 *  Set connection
	 *  @param cc connection
	 */
	public static void setDBTarget (CConnection cc)
	{
		if (cc == null)
			throw new IllegalArgumentException("DB.setDBTarget connection is NULL");

		if (s_cc == null)
			s_cc = cc;
		synchronized (s_cc)    //  use as mutex
		{
			s_cc = cc;
			//  Closing existing
			if (s_connections != null)
			{
				for (int i = 0; i < s_connections.length; i++)
				{
					try {
						s_connections[i].close();
					} catch (Exception e) {}
				}
			}
			s_connections = null;
		}
	}   //  setDBTarget

	/**
	 *  Is there a connection to the database ?
	 *  @return true, if connected to database
	 */
	public static boolean isConnected()
	{
		try
		{
			getConnectionRW();	//	try to get a connection
			return true;
		}
		catch (Exception e)
		{
		}
		return false;
	}   //  isConnected

	/**
	 *	Return (pooled) r/w AutoCommit, read committed connection
	 *
	 *  @return Connection (r/w)
	 */
	public static Connection getConnectionRW ()
	{
		//	check health of connection
		try
		{
			if (s_connectionRW == null)
				;
			else if (s_connectionRW.isClosed())
				s_connectionRW = null;
			else if (s_connectionRW instanceof OracleConnection && ((OracleConnection)s_connectionRW).pingDatabase(1) < 0)
				s_connectionRW = null;
			else
				 s_connectionRW.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		catch (Exception e)
		{
			s_connectionRW = null;
		}
		if (s_connectionRW == null)
			s_connectionRW = s_cc.getConnection (true, Connection.TRANSACTION_READ_COMMITTED);
		if (s_connectionRW == null)
			throw new UnsupportedOperationException("DB.getConnectionRW - @NoDBConnection@");
		return s_connectionRW;
	}   //  getConnectionRW

	/**
	 *	Return read committed, read/only connection with AutoCommit from pool
	 *  @return Connection (r/o)
	 */
	private static Connection getConnectionRO ()
	{
		try
		{
			synchronized (s_cc)    //  use as mutex as s_connection is null the first time
			{
				if (s_connections == null)
					s_connections = createConnections (Connection.TRANSACTION_READ_COMMITTED);     //  see below
			}
		}
		catch (Exception e)
		{
			s_log.error("getConnectionRO", e);
		}

		//  check health of connection
		int pos = s_conCount++;
		Connection connection = s_connections[pos % s_conCacheSize];
		try
		{
			if (connection == null)
				;
			else if (connection.isClosed())
				connection = null;
			else if (connection instanceof OracleConnection && ((OracleConnection)connection).pingDatabase(1) < 0)
				connection = null;
			else
				 connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		catch (Exception e)
		{
			connection = null;
		}
		//	Get new
		if (connection == null)
		{
			System.out.println("DB.getConnectionRO - replacing connection #" + pos % s_conCacheSize);
		//	s_log.error("getConnectionRO - replacing connection #" + pos % s_conCacheSize);
			connection = s_cc.getConnection (true, Connection.TRANSACTION_READ_COMMITTED);            //  see above
		/*	try
			{
				retValue.setReadOnly(true);     //  not supported by Oracle
			}
			catch (Exception e)
			{
				System.err.println("DB.getConnectionRO - Cannot set to R/O - " + e);
			}   */
			s_connections[pos % s_conCacheSize] = connection;
		}
		if (connection == null)
			throw new UnsupportedOperationException("DB.getConnectionRO - @NoDBConnection@");
		return connection;
	}	//	getConnectionRO

	/**
	 *	Create new Connection.
	 *  The connection must be closed explicitly by the application
	 *
	 *  @param autoCommit auto commit
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Connection connection
	 */
	public static Connection createConnection (boolean autoCommit, int trxLevel)
	{
		s_log.debug("createConnection " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", AutoCommit=" + autoCommit + ", TrxLevel=" + trxLevel);
		return s_cc.getConnection (autoCommit, trxLevel);
	}	//	createConnection

	/**
	 *	Create new set of r/o Connections.
	 *  R/O connection might not be supported by DB
	 *
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Array of Connections (size based on s_conCacheSize)
	 */
	private static Connection[] createConnections (int trxLevel)
	{
		s_log.debug("createConnections (" + s_conCacheSize + ") " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", TrxLevel=" + trxLevel);
		Connection cons[] = new Connection[s_conCacheSize];
		try
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				cons[i] = s_cc.getConnection (true, trxLevel);  //  auto commit
				if (cons[i] == null)
					System.err.println("DB.createConnections - connection is NULL");
			}
		}
		catch (Exception e)
		{
			//  Don't use Log
			System.err.println("DB.createConnections - " + e.getMessage());
		}
		return cons;
	}	//	createConnections

	/**
	 *  Get Database Driver.
	 *  Access to database specific functionality.
	 *  @return Compiere Database Driver
	 */
	public static CompiereDatabase getDatabase()
	{
		if (s_cc != null)
			return s_cc.getDatabase();
		return null;
	}   //  getDatabase

	/*************************************************************************/

	/**
	 *  Check database Version with Code version
	 *  @param ctx context
	 *  @return true if Database version (date) is the same
	 */
	public static boolean isDatabaseOK (Properties ctx)
	{
		//	Force loading Messages of current language
		Msg.getMsg(ctx, "0");

		//  Check Version
		String version = "?";
		String sql = "SELECT Version FROM AD_System";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				version = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("Problem with AD_System Table - Run system.sql script - " + e.toString());
		}
		Log.trace(Log.l6_Database, "DB.isDatabaseOK", "DB_Version=" + version);
		//  identical version
		if (Compiere.DB_VERSION.equals(version))
			return true;

		String AD_Message = "DatabaseVersionError";
		String title = org.compiere.Compiere.getName() + " " +  Msg.getMsg(ctx, AD_Message, true);
		//	Code assumes Database version {0}, but Database has Version {1}.
		String msg = Msg.getMsg(ctx, AD_Message);	//	complete message
		msg = MessageFormat.format(msg, new Object[] {Compiere.DB_VERSION, version});
		Object[] options = { UIManager.get("OptionPane.noButtonText"), "Migrate" };
		int no = JOptionPane.showOptionDialog (null, msg,
			title, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
			UIManager.getIcon("OptionPane.errorIcon"), options, options[0]);
		if (no == 1)
		{
			try
			{
				Class.forName("com.compiere.client.StartMaintain").newInstance();
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog (null,
					ex.getMessage() + "\nSee: http://www.compiere.com/maintain",
					title, JOptionPane.ERROR_MESSAGE);
				s_log.error("isDatabaseOK - " + ex.getMessage());
			}
		}
		return false;
	}   //  isDatabaseOK

	/*************************************************************************/

	/**
	 *	Security Context Login.
	 *  @param uid uid
	 *  @param pwd pwd
	 *  @param role role
	 *  @return true if connected
	 */
	public static boolean login_context (String uid, String pwd, String role)
	{
		if (uid == null || pwd == null || role == null || uid.length() == 0 || pwd.length() == 0 || role.length() == 0)
			throw new IllegalArgumentException("DB.login_context - required parameter missing");
		Log.trace(Log.l3_Util, "DB.login_context");
		if (uid == null || pwd == null || role == null)
			return false;
		//
		String SQL = "{CALL Compiere_Context.Login(?,?,?,?)}";
		boolean result = true;
		try
		{
			CallableStatement cstmt = getConnectionRO().prepareCall(SQL);
			cstmt.setString(1, uid);
			cstmt.setString(2, pwd);
			cstmt.setString(3, role);
			cstmt.setString(4, Language.getBaseAD_Language());
			result = cstmt.execute();
			cstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("login_context", e);
			result = false;
		}
		return result;
	}	//	login_context

	/*************************************************************************/

	/**
	 *	Close Target
	 */
	public static void closeTarget()
	{
		if (s_connections != null)
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				try
				{
					if (s_connections[i] != null)
						s_connections[i].close();
				}
				catch (SQLException e)
				{
					s_log.error("close connection #" + i, e);
				}
				s_connections[i] = null;
			}
			try
			{
				if (s_connectionRW != null)
					s_connectionRW.close();
			}
			catch (SQLException e)
			{
				s_log.error("close R/W connection", e);
			}
			s_connectionRW = null;
		}
		s_connections = null;
//		EJB.close();
	}	//	closeTarget

	/*************************************************************************/

	/**
	 *	Prepare Forward Read Only Call
	 *  @param RO_SQL sql (RO)
	 *  @return Callable Statement
	 */
	public static CallableStatement prepareCall(String RO_SQL)
	{
		if (RO_SQL == null || RO_SQL.length() == 0)
			throw new IllegalArgumentException("DB.prepareCall - required parameter missing - " + RO_SQL);
		//
		String sql = getDatabase().convertStatement(RO_SQL);
		try
		{
			return getConnectionRO().prepareCall
				(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		}
		catch (SQLException e)
		{
			s_log.error("prepareCall (" + sql + ")", e);
		}
		return null;
	}	//	prepareCall

	/*************************************************************************/

	/**
	 *	Prepare Read Only Statement
	 *  @param RO_SQL sql (RO)
	 *  @return Prepared Statement
	 */
	public static PreparedStatement prepareStatement(String RO_SQL)
	{
		return prepareStatement(RO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}	//	prepareStatement

	/**
	 *	Prepare Statement
	 *
	 *  @param SQL sql
	 *  @param type - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param concur - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Prepared Statement r/o or r/w depending on concur
	 */
	public static PreparedStatement prepareStatement(String SQL, int type, int concur)
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.prepareStatement - required parameter missing - " + SQL);
		//
		String sql = getDatabase().convertStatement(SQL);
		try
		{
			Connection conn = null;
			if (concur == ResultSet.CONCUR_UPDATABLE)
				conn = getConnectionRW();
			else
				conn = getConnectionRO();
			return conn.prepareStatement (sql, type, concur);
		}
		catch (SQLException e)
		{
			s_log.error("prepareStatement (" + sql + ")", e);
		}
		return null;
	}	//	prepareStatement

	/**
	 *	Create Read Only Statement
	 *  @return Statement
	 */
	public static Statement createStatement()
	{
		return createStatement (ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}	//	createStatement

	/**
	 *	Create Statement
	 *
	 *  @param type - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param concur - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Statement - either r/w ir r/o depending on concur
	 */
	public static Statement createStatement(int type, int concur)
	{
		try
		{
			Connection conn = null;
			if (concur == ResultSet.CONCUR_UPDATABLE)
				conn = getConnectionRW();
			else
				conn = getConnectionRO();
			return conn.createStatement(type, concur);
		}
		catch (SQLException e)
		{
			s_log.error("createStatement", e);
		}
		return null;
	}	//	createStatement

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param SQL sql
	 *  @return number of rows updated
	 */
	public static int executeUpdate (String SQL)
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.executeUpdate - required parameter missing - " + SQL);
		//
		String sql = getDatabase().convertStatement(SQL);
		int no = 0;
		Statement stmt = null;
		try
		{
			Connection conn = getConnectionRW();
			stmt = conn.createStatement();
			no = stmt.executeUpdate(sql);
		}
		catch (SQLException e)
		{
			s_log.error("executeUpdate - " + sql, e);
			Log.saveError("DBExecuteError", e.getLocalizedMessage());
		}
		finally
		{
			//  Always close cursor
			try
			{
				stmt.close();
			}
			catch (SQLException e2)
			{
				s_log.error("executeUpdate - cannot close statement");
			}
		}
		return no;
	}	//	execute Update


	/**
	 *	Commit - commit on RW connection.
	 *  Is not required as RW connection is AutoCommit
	 *  @param throwException if true, re-throws exception
	 *  @return true if not needed or success
	 *  @throws SQLException
	 */
	public static boolean commit (boolean throwException) throws SQLException
	{
		try
		{
			Connection conn = getConnectionRW();
		//	if (!conn.getAutoCommit())
				conn.commit();
		}
		catch (SQLException e)
		{
			s_log.error("commit", e);
			if (throwException)
				throw e;
			return false;
		}
		return true;
	}	//	commit

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @return next no
	 */
	public static int getKeyNextNo (Properties ctx, int WindowNo, String TableName)
	{
		if (ctx == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getKeyNextNo - required parameter missing");
		String CompiereSys = Env.getContext(ctx, "#CompiereSys");
		int AD_Client_ID = Env.getContextAsInt(ctx, WindowNo, "AD_Client_ID");
		//
		return getKeyNextNo (AD_Client_ID, CompiereSys, TableName);
	}   //  getKeyNextNo

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param AD_Client_ID client
	 *  @param CompiereSys compiere sys
	 *  @param TableName table name
	 *  @return next no
	 */
	public static int getKeyNextNo (int AD_Client_ID, String CompiereSys, String TableName)
	{
		if (TableName == null || CompiereSys == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getKeyNextNo - required parameter missing");
		int retValue = 0;

		//	Check CompiereSys
	//	if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
	//		throw new UnsupportedOperationException("DB.getKeyNextNo - Cannot add System records");
		//
		try
		{
			String SQL = "{CALL AD_Sequence_Next(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setString(1, TableName);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.NUMERIC);
			cstmt.executeUpdate();
			retValue = cstmt.getInt(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getKeyNextNo - Table=" + TableName + ")", e);
		}
		return retValue;
	}	//	getKeyNextNo

	/**
	 *	Get Document Number for current document.
	 *  <br>
	 *  - first search for DocType based Document No
	 *  - then Search for DocumentNo based on TableName
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @param onlyDocType Do not search for document no based on TableName
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (Properties ctx, int WindowNo, String TableName, boolean onlyDocType)
	{
		if (ctx == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getDocumentNo - required parameter missing");
		String CompiereSys = Env.getContext(ctx, "#CompiereSys");
		int AD_Client_ID = Env.getContextAsInt(ctx, WindowNo, "AD_Client_ID");

		//	Get C_DocType_ID from context - NO Defaults -
		int C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID");
		if (C_DocType_ID == 0)
			C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID");
		if (C_DocType_ID == 0)
			return getDocumentNo (AD_Client_ID, CompiereSys, TableName);

		//	Check CompiereSys
		if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
			throw new UnsupportedOperationException("DB.getDocumentNo - Cannot add System records");
		//
		String retValue = getDocumentNo(AD_Client_ID, C_DocType_ID);
		if (!onlyDocType && retValue == null)
			return getDocumentNo (AD_Client_ID, CompiereSys, TableName);
		return retValue;
	}	//	getDocumentNo

	/**
	 *	Get Document Number for current document.
	 *  @param AD_Client_ID client
	 *  @param C_DocType_ID (target) document type
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (int AD_Client_ID, int C_DocType_ID)
	{
		String retValue = null;
		try
		{
			String SQL = "{CALL AD_Sequence_DocType(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setInt(1, C_DocType_ID);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.VARCHAR);
			cstmt.executeUpdate();
			retValue = cstmt.getString(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getDocumentNo - DocType=" + C_DocType_ID, e);
		}
		Log.trace(Log.l5_DData, "DB.getDocumentNo", "DocType=" + C_DocType_ID + " -> " + retValue);
		return retValue;
	}	//	getDocumentNo


	/**
	 *  Get Next Document No
	 *  @param AD_Client_ID client
	 *  @param CompiereSys system
	 *  @param TableName table name
	 *  @return DocumentNo
	 */
	public static String getDocumentNo (int AD_Client_ID, String CompiereSys, String TableName)
	{
		if (CompiereSys == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getDocumentNo - required parameter missing");
		//	Check CompiereSys
	//	if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
	//		throw new UnsupportedOperationException("DB.getDocumentNo - Cannot add System records");
		//
		String retValue = null;
		try
		{
			String SQL = "{CALL AD_Sequence_Doc(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setString(1, "DocumentNo_" + TableName);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.VARCHAR);
			cstmt.executeUpdate();
			retValue = cstmt.getString(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getDocumentNo  TableName=" + TableName, e);
		}
		Log.trace(Log.l5_DData, "DB.getDocumentNo", "TableName=" + TableName + " -> " + retValue);
		return retValue;
	}   //  getDocumentNo

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			Log.error("DB.getSQLValue - " + sql, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 *  Convert an amount with today's spot rate
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  Amt         The amount to be converted
	 *  @return converted amount
	 */
	public static BigDecimal getConvertedAmt (BigDecimal Amt, int CurFrom_ID, int CurTo_ID)
	{
		return getConvertedAmt (Amt, CurFrom_ID, CurTo_ID, null, null);
	}   //  getConvertedAmt

	/**
	 *	Convert an amount
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  ConvDate    The Conversion date - if null - use current date
	 *  @param  RateType    The Conversion rate type - if null/empty - use Spot
	 *  @param  Amt         The amount to be converted
	 *  @return converted amount
	 */
	public static BigDecimal getConvertedAmt (BigDecimal Amt, int CurFrom_ID, int CurTo_ID,
		Timestamp ConvDate, String RateType)
	{
		if (Amt == null)
			throw new IllegalArgumentException("DB.getConvertedAmt - required parameter missing - Amt");
		BigDecimal retValue = null;
		if (CurFrom_ID == CurTo_ID || Amt.equals(Env.ZERO))
			return Amt;
		//
		try
		{
			String sql = "{? = call C_Currency_Convert(?,?,?,?,?) }";
			CallableStatement cstmt = prepareCall(sql);
			//
			cstmt.registerOutParameter(1, Types.NUMERIC);
			//
			cstmt.setBigDecimal(2, Amt);					//	Amount		IN  	NUMBER
			cstmt.setInt(3, CurFrom_ID);             		//	CurFrom		IN 		NUMBER
			cstmt.setInt(4, CurTo_ID);                  	//	CurTo		IN 		NUMBER
			if (ConvDate == null)
				ConvDate = new Timestamp (System.currentTimeMillis());
			cstmt.setTimestamp(5, ConvDate);				//	ConvDate	IN 		DATE
			if (RateType == null || RateType.equals(""))
				RateType = "S";
			cstmt.setString(6, RateType);					//	RateType	IN 		CHAR
			//
			cstmt.executeUpdate();
			retValue = cstmt.getBigDecimal(1);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getConvertedAmt", e);
		}
		return retValue;
	}	//	getConvertedAmt

	/**
	 *	Get Currency Rate
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  ConvDate    The Conversion date - if null - use current date
	 *  @param  RateType    The Conversion rate type - if null/empty - use Spot
	 *  @return currency Rate
	 */
	public static BigDecimal getConvesionRate (int CurFrom_ID, int CurTo_ID,
		Timestamp ConvDate, String RateType)
	{
		BigDecimal retValue = null;
		if (CurFrom_ID == CurTo_ID)
			return new BigDecimal("1");
		//
		try
		{
			String sql = "{? = call C_Currency_Rate(?,?,?,?)}";
			CallableStatement cstmt = prepareCall(sql);
			//
			cstmt.registerOutParameter(1, Types.NUMERIC);
			//
			cstmt.setInt(2, CurFrom_ID);             		//	CurFrom		IN 		NUMBER
			cstmt.setInt(3, CurTo_ID);                  	//	CurTo		IN 		NUMBER
			if (ConvDate == null)
				ConvDate = new Timestamp (System.currentTimeMillis());
			cstmt.setTimestamp(4, ConvDate);				//	ConvDate	IN 		DATE
			if (RateType == null || RateType.equals(""))
				RateType = "S";
			cstmt.setString(5, RateType);					//	RateType	IN 		CHAR
			//
			cstmt.executeUpdate();
			retValue = cstmt.getBigDecimal(1);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getConvesionRate", e);
		}
		return retValue;
	}	//	getConvesionRate


	/**
	 *  Get fully qualified Account Combination
	 *
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param C_AcctSchema_ID acct schema
	 *  @param Account_ID natural account
	 *  @param  base_ValidCombination_ID optional base combination to be specified
	 *  @param Alias aloas
	 *  @param AD_User_ID user
	 *  @param M_Product_ID product
	 *  @param C_BPartner_ID partner
	 *  @param AD_OrgTrx_ID trx org
	 *  @param C_LocFrom_ID loc from
	 *  @param C_LocTo_ID loc to
	 *  @param C_SRegion_ID sales region
	 *  @param C_Project_ID project
	 *  @param C_Campaign_ID campaign
	 *  @param C_Activity_ID activity
	 *  @param User1_ID user1
	 *  @param User2_ID user2
	 *  @return C_ValidCombination_ID of existing or new Combination
	 */
	public static int getValidCombination (int AD_Client_ID, int AD_Org_ID,
		int C_AcctSchema_ID, int Account_ID, int base_ValidCombination_ID, String Alias, int AD_User_ID,
		int M_Product_ID, int C_BPartner_ID, int AD_OrgTrx_ID,
		int C_LocFrom_ID, int C_LocTo_ID, int C_SRegion_ID, int C_Project_ID,
		int C_Campaign_ID, int C_Activity_ID, int User1_ID, int User2_ID)
	{
		int retValue = 0;
		StringBuffer sb = new StringBuffer ("C_ValidCombination_Get(");
		try
		{
			String sql = "{CALL C_ValidCombination_Get(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)}";
			CallableStatement cstmt = prepareCall(sql);
			//  1 - ID
			cstmt.registerOutParameter(1, Types.NUMERIC);
			sb.append("v,");
			//  --  Mandatory Accounting fields
			//	2 - AD_Client_ID
			cstmt.setInt(2, AD_Client_ID);
			sb.append(AD_Client_ID).append(",");
			//	3 - AD_Org_ID
			cstmt.setInt(3, AD_Org_ID);
			sb.append(AD_Org_ID).append(",");
			//  4- C_AcctSchema_ID
			cstmt.setInt(4, C_AcctSchema_ID);
			sb.append(C_AcctSchema_ID).append(",");
			//  5 - Account_ID
			cstmt.setInt(5, Account_ID);
			sb.append(Account_ID).append(", ");

			//  --  Optional
			//  6 - Base C_ValidCombination_ID
			if (base_ValidCombination_ID != 0)
			{
				cstmt.setInt (6, base_ValidCombination_ID);
				sb.append(base_ValidCombination_ID).append(",");
			}
			else
			{
				cstmt.setNull(6, Types.NUMERIC);
				sb.append("NULL,");
			}
			//  7 - MustBeFullyQualified
			cstmt.setString(7, "Y");
			sb.append("'Y',");
			//  8 - Alias
			if (Alias != null && Alias.length() > 0)
			{
				cstmt.setString(8, Alias);
				sb.append("'").append(Alias).append("';");
			}
			else
			{
				cstmt.setNull(8, Types.VARCHAR);
				sb.append("NULL,");
			}
			//  9 - CreatedBy
			cstmt.setInt(9, AD_User_ID);
			sb.append(AD_User_ID).append(", ");

			//	--  Optional Accounting fields
			//  10 - M_Product_ID
			if (M_Product_ID != 0)
			{
				cstmt.setInt(10, M_Product_ID);
				sb.append(M_Product_ID).append(",");
			}
			else
			{
				cstmt.setNull(10, Types.NUMERIC);
				sb.append("NULL,");
			}
			//  11 - C_BPartner_ID
			if (C_BPartner_ID != 0)
			{
				cstmt.setInt(11, C_BPartner_ID);
				sb.append(C_BPartner_ID).append(",");
			}
			else
			{
				cstmt.setNull(11, Types.NUMERIC);
				sb.append("NULL,");
			}
			//  12 - AD_OrgTrx_ID
			if (AD_OrgTrx_ID != 0)
			{
				cstmt.setInt(12, AD_OrgTrx_ID);
				sb.append(AD_OrgTrx_ID).append(",");
			}
			else
			{
				cstmt.setNull(12, Types.NUMERIC);
				sb.append("NULL,");
			}
			//  13 - C_LocFrom_ID
			if (C_LocFrom_ID != 0)
			{
				cstmt.setInt(13, C_LocFrom_ID);
				sb.append(C_LocFrom_ID).append(",");
			}
			else
			{
				cstmt.setNull(13, Types.NUMERIC);
				sb.append("NULL,");
			}
			//  14 - C_LocTo_ID
			if (C_LocTo_ID != 0)
			{
				cstmt.setInt(14, (C_LocTo_ID));
				sb.append(C_LocTo_ID).append(", ");
			}
			else
			{
				cstmt.setNull(14, Types.NUMERIC);
				sb.append("NULL, ");
			}
			//  15 - C_SalesRegion_ID
			if (C_SRegion_ID != 0)
			{
				cstmt.setInt(15, (C_SRegion_ID));
				sb.append(C_SRegion_ID).append(",");
			}
			else
			{
				cstmt.setNull(15, Types.NUMERIC);
				sb.append("NULL,");
			}
			//  16 - C_Project_ID
			if (C_Project_ID != 0)
			{
				cstmt.setInt(16, (C_Project_ID));
				sb.append(C_Project_ID).append(",");
			}
			else
			{
				cstmt.setNull(16, Types.NUMERIC);
				sb.append("NULL,");
			}
			//  17 - C_Campaign_ID
			if (C_Campaign_ID != 0)
			{
				cstmt.setInt(17, (C_Campaign_ID));
				sb.append(C_Campaign_ID).append(",");
			}
			else
			{
				cstmt.setNull(17, Types.NUMERIC);
				sb.append("NULL,");
			}
			//  18 - C_Activity_ID
			if (C_Activity_ID != 0)
			{
				cstmt.setInt(18, (C_Activity_ID));
				sb.append(C_Activity_ID).append(",");
			}
			else
			{
				cstmt.setNull(18, Types.NUMERIC);
				sb.append("NULL,");
			}
			//  19 - User1_ID
			if (User1_ID != 0)
			{
				cstmt.setInt(19, (User1_ID));
				sb.append(User1_ID).append(",");
			}
			else
			{
				cstmt.setNull(19, Types.NUMERIC);
				sb.append("NULL,");
			}
			//  20 - User2_ID
			if (User2_ID != 0)
			{
				cstmt.setInt(20, (User2_ID));
				sb.append(User2_ID).append(")");
			}
			else
			{
				cstmt.setNull(20, Types.NUMERIC);
				sb.append("NULL)");
			}

			//
			cstmt.executeUpdate();
			retValue = cstmt.getInt(1);     //  1 - ID
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getValidCombination " + sb.toString(), e);
		}
		return retValue;
	}   //  getValidCombination

	/**
	 *  Insert Note
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param AD_User_ID user
	 *  @param AD_Table_ID table
	 *  @param Record_ID record
	 *  @param AD_MessageValue message
	 *  @param Text text
	 *  @param Reference reference
	 *  @return true if note was inserted
	 */
	public static boolean insertNote (int AD_Client_ID, int AD_Org_ID, int AD_User_ID,
		int AD_Table_ID, int Record_ID,
		String AD_MessageValue, String Text, String Reference)
	{
		if (AD_MessageValue == null || AD_MessageValue.length() == 0)
			throw new IllegalArgumentException("DB.insertNote - required parameter missing - AD_Message");

		//  Database limits
		if (Text == null)
			Text = "";
		if (Text.length() > 2000)
			Text = Text.substring(0,1999);
		if (Reference == null)
			Reference = "";
		if (Reference.length() > 60)
			Reference = Reference.substring(0,59);
		//
		Log.trace(Log.l3_Util, "DB.insertNote - " + AD_MessageValue + " - " + Reference);
		//
		StringBuffer sql = new StringBuffer ("INSERT INTO AD_Note (AD_Note_ID,");
		sql.append("AD_Client_ID,AD_Org_ID,IsActive, Created,CreatedBy,Updated,UpdatedBy, ")
			.append("AD_Message_ID,Text,Reference, ")
			.append("AD_User_ID,AD_Table_ID,Record_ID,Processed) ")
			.append("SELECT ");
		//
		String CompiereSys = "N";
		int AD_Note_ID = getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Note");
		sql.append(AD_Note_ID).append(",");
		//
		sql.append(AD_Client_ID).append(",")
			.append(AD_Org_ID).append(", 'Y',SysDate,")
			.append(AD_User_ID).append(",SysDate,0,");
		//
		sql.append(" AD_Message_ID,'").append(Text).append("','")
			.append(Reference).append("', ");
		//
		sql.append(AD_User_ID).append(",").append(AD_Table_ID).append(",")
			.append(Record_ID).append(",'N' ");
		//
		sql.append("FROM AD_Message WHERE Value='").append(AD_MessageValue).append("'");
		//  Create Entry
		int no = executeUpdate(sql.toString());

		//  AD_Message must exist, so if not created, it is probably
		//  due to non-existing AD_Message
		if (no == 0)
		{
			sql = new StringBuffer ("INSERT INTO AD_Note (AD_Note_ID,");
			sql.append("AD_Client_ID,AD_Org_ID,IsActive, Created,CreatedBy,Updated,UpdatedBy, ")
				.append("AD_Message_ID,Text,Reference, ")
				.append("AD_User_ID,AD_Table_ID,Record_ID,Processed) ")
				.append("SELECT ");
			//  - use AD_Note_ID from above
			sql.append(AD_Note_ID).append(",");
			//
			sql.append(AD_Client_ID).append(",")
				.append(AD_Org_ID).append(", 'Y',SysDate,")
				.append(AD_User_ID).append(",SysDate,0, ");
			//
			sql.append("AD_Message_ID,'").append(AD_MessageValue).append(": ").append(Text).append("','")
				.append(Reference).append("', ");
			//
			sql.append(AD_User_ID).append(",").append(AD_Table_ID).append(",")
				.append(Record_ID).append(",'N' ");
			//  Hardcoded AD_Message - AD_Message is in Text
			sql.append("FROM AD_Message WHERE Value='NoMessageFound'");
			//  Try again
			no = executeUpdate(sql.toString());
		}

		return no == 1;
	}   //  insertNote


	/*************************************************************************/

	/**
	 *	Print SQL Warnings.
	 *  <br>
	 *		Usage: DB.printWarning(rs.getWarnings(), "xx");
	 *  @param comment comment
	 *  @param warning warning
	 */
	public static void printWarning(String comment, SQLWarning warning)
	{
		if (comment == null || warning == null || comment.length() == 0)
			throw new IllegalArgumentException("DB.printException - required parameter missing");
		s_log.warn("SQL Warning: " + comment);
		if (warning == null)
			return;
		//
		SQLWarning warn = warning;
		while (warn != null)
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append(warn.getMessage());
			buffer.append("; State=").append(warn.getSQLState()).append("; ErrorCode=").append(warn.getErrorCode());
			s_log.warn(buffer.toString());
			warn = warn.getNextWarning();
		}
	}	//	printWarning

	/**
	 *  Create SQL TO Date String from Timestamp
	 *
	 *  @param  time Date to be converted
	 *  @param  dayOnly true if time set to 00:00:00
	 *
	 *  @return TO_DATE('2001-01-30 18:10:20',''YYYY-MM-DD HH24:MI:SS')
	 *      or  TO_DATE('2001-01-30',''YYYY-MM-DD')
	 */
	public static String TO_DATE (Timestamp time, boolean dayOnly)
	{
		if (time == null)
		{
			if (dayOnly)
				return "TRUNC(SysDate)";
			return "SysDate";
		}

		StringBuffer dateString = new StringBuffer("TO_DATE('");
		//  YYYY-MM-DD HH24:MI:SS.mmmm  JDBC Timestamp format
		String myDate = time.toString();
		if (dayOnly)
		{
			dateString.append(myDate.substring(0,10));
			dateString.append("','YYYY-MM-DD')");
		}
		else
		{
			dateString.append(myDate.substring(0, myDate.indexOf(".")));	//	cut off miliseconds
			dateString.append("','YYYY-MM-DD HH24:MI:SS')");
		}
		return dateString.toString();
	}   //  TO_DATE

	/**
	 *  Create SQL TO Date String from Timestamp
	 *  @param time time
	 *  @return TO_DATE String
	 */
	public static String TO_DATE (Timestamp time)
	{
		return TO_DATE(time, true);
	}   //  TO_DATE

	/**
	 *  Create SQL for formatted Date, Number
	 *
	 *  @see org.compiere.util.DisplayType
	 *  @see org.compiere.util.Env
	 *
	 *  @param  columnName  the column name in the SQL
	 *  @param  displayType Display Type
	 *  @param  AD_Language 6 character language setting (from Env.LANG_*)
	 *
	 *  @return TRIM(TO_CHAR(columnName,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.'''))
	 *      or TRIM(TO_CHAR(columnName,'TM9')) depending on DisplayType and Language
	 */
	public static String TO_CHAR (String columnName, int displayType, String AD_Language)
	{
		if (columnName == null || AD_Language == null || columnName.length() == 0)
			throw new IllegalArgumentException("DB.TO_CHAR - required parameter missing");

		StringBuffer retValue = new StringBuffer("TRIM(TO_CHAR(");
		retValue.append(columnName);

		//  Numbers
		if (DisplayType.isNumeric(displayType))
		{
			if (displayType == DisplayType.Amount)
				retValue.append(",'9G999G990D00'");
			else
				retValue.append(",'TM9'");
			//  TO_CHAR(GrandTotal,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.''')
			if (!Language.isDecimalPoint(AD_Language))      //  reversed
				retValue.append(",'NLS_NUMERIC_CHARACTERS='',.'''");
		}
		else if (DisplayType.isDate(displayType))
		{
			retValue.append(",'")
				.append(Language.getLanguage(AD_Language).getDBdatePattern())
				.append("'");
		}

		retValue.append("))");
		//
		return retValue.toString();
	}   //  TO_CHAR_Number


	/**
	 *  Package Strings for SQL command
	 *  @param txt  String with text
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt)
	{
		return TO_STRING (txt, 0);
	}   //  TO_STRING

	/**
	 *	Package Strings for SQL command.
	 *  <pre>
	 *		-	include in '
	 *		-	replace ' with ''
	 *      -   replace \ with \\
	 *  </pre>
	 *  @param txt  String with text
	 *  @param maxLength    Maximum Length of content or 0 to ignore
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt, int maxLength)
	{
		if (txt == null)
			return "NULL";

		//  Length
		String text = txt;
		if (maxLength != 0 && text.length() > maxLength)
			text = txt.substring(0, maxLength);

		//  copy characters		(wee need to look through anyway)
		StringBuffer out = new StringBuffer("'");
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (c == '\'')
				out.append("''");
			else if (c == '\\')
				out.append("\\\\");
			else
				out.append(c);
		}
		out.append("'");
		//
		return out.toString();
	}	//	TO_STRING

}	//	DB

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
package org.compiere.util;

import java.rmi.*;
import java.security.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import javax.sql.*;
import javax.swing.*;

import oracle.jdbc.*;

import org.compiere.*;
import org.compiere.db.*;
import org.compiere.interfaces.*;
import org.compiere.model.*;
import org.compiere.process.*;

/**
 *  General Database Interface
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB.java,v 1.68 2004/09/10 02:53:28 jjanke Exp $
 */
public final class DB
{
	/** Connection Descriptor           */
	private static CConnection      s_cc = null;
	/** Connection Cache r/o            */
	private static Connection[]		s_connections = null;
	/** Connection Cache Size           */
	private static int              s_conCacheSize = Ini.isClient() ? 3 : 3;
	/** Connection counter              */
	private static int              s_conCount = 0;
	/** Connection r/w                  */
	private static Connection		s_connectionRW = null;
	/**	Logger							*/
	private static Logger			s_log = Logger.getCLogger (DB.class);

	/**
	 *	Client Login.
	 *  <p>
	 *  - Get Connection
	 *  - Compare User info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 * @param ctx context
	 * @param cc connection
	 * @param app_user user
	 * @param app_pwd pwd
	 * @param force ignore pwd
	 * @return  Array of Role KeyNamePair or null if error
	 * The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	protected static KeyNamePair[] login (Properties ctx,
		CConnection cc,
		String app_user, String app_pwd, boolean force)
	{
		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		//	Establish connection
		setDBTarget(cc);
		Env.setContext(ctx, "#Host", s_cc.getAppsHost());
		Env.setContext(ctx, "#Database", s_cc.getDbName());
		
		if (getConnectionRO() == null)
		{
			Log.saveError("NoDatabase", "");
			return null;
		}
		if (app_pwd == null)
			return null;
		//
		return loginDB (ctx, app_user, app_pwd, force);
	}   //  login

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user Principal
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx, Principal app_user)
	{
		if (app_user == null)
			return null;
		//  login w/o password as previously authorized
		return loginDB (ctx, app_user.getName(), null, false);
	}   //  app_user

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user user id
	 *  @param app_pwd password
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx,
		String app_user, String app_pwd)
	{
		if (app_pwd == null)
			return null;
		return loginDB (ctx, app_user, app_pwd, false);
	}   //  login

	/**
	 *  Actual DB login procedure.
	 *
	 *  @param ctx context
	 *  @param app_user user
	 *  @param app_pwd pwd
	 *  @param force ignore pwd
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	private static KeyNamePair[] loginDB (Properties ctx,
		String app_user, String app_pwd, boolean force)
	{
		s_log.info("login - User=" + app_user);

		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		if (app_user == null)
			return null;

		KeyNamePair[] retValue = null;
		ArrayList list = new ArrayList();
		//
		String sql = "SELECT AD_User.AD_User_ID, AD_User.Description,"
			+ " AD_Role.AD_Role_ID, AD_Role.Name "
			+ "FROM AD_User, AD_User_Roles, AD_Role "
			+ "WHERE AD_User.AD_User_ID=AD_User_Roles.AD_User_ID"
			+ " AND AD_User_Roles.AD_Role_ID=AD_Role.AD_Role_ID"
			+ " AND AD_User.Name=?"		        							//	#1
			+ " AND AD_User.IsActive='Y' AND AD_Role.IsActive='Y' AND AD_User_Roles.IsActive='Y'";
		if (app_pwd != null)
			sql += " AND (AD_User.Password=? OR AD_User.Password=?)";   	//  #2/3
		sql += " ORDER BY AD_Role.Name";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setString(1, app_user);
			if (app_pwd != null)
			{
				pstmt.setString(2, app_pwd);
				pstmt.setString(3, Secure.getDigest(app_pwd));
			}
			//	execute a query
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())		//	no record found
				if (force)
				{
					Env.setContext(ctx, "#AD_User_Name", "System");
					Env.setContext(ctx, "#AD_User_ID", "0");
					Env.setContext(ctx, "#AD_User_Description", "System Forced Login");
					Env.setContext(ctx, "#User_Level", "S  ");  	//	Format 'SCO'
					Env.setContext(ctx, "#User_Client", "0");		//	Format c1, c2, ...
					Env.setContext(ctx, "#User_Org", "0"); 		//	Format o1, o2, ...
					rs.close();
					pstmt.close();
					retValue = new KeyNamePair[] {new KeyNamePair(0, "System Administrator")};
					return retValue;
				}
				else
				{
					rs.close();
					pstmt.close();
					Log.saveError("UserPwdError", app_user, false);
					return null;
				}

			Env.setContext(ctx, "#AD_User_Name", app_user);
			Env.setContext(ctx, "#AD_User_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#SalesRep_ID", rs.getInt("AD_User_ID"));
			//
			Ini.setProperty(Ini.P_UID, app_user);
			if (Ini.getPropertyBool(Ini.P_STORE_PWD))
				Ini.setProperty(Ini.P_PWD, app_pwd);

			do	//	read all roles
			{
				int AD_Role_ID = rs.getInt("AD_Role_ID");
				if (AD_Role_ID == 0)
					Env.setContext(ctx, "#SysAdmin", "Y");
				String Name = rs.getString("Name");
				KeyNamePair p = new KeyNamePair(AD_Role_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
			pstmt = null;
			//
			retValue = new KeyNamePair[list.size()];
			list.toArray(retValue);
			s_log.debug("login - User=" + app_user + " - roles #" + retValue.length);
		}
		catch (SQLException ex)
		{
			s_log.error("login", ex);
			Log.saveError("DBLogin", ex);
			retValue = null;
		}
		//
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		return retValue;
	}	//	login

	/**
	 *  Load Clients.
	 *  <p>
	 *  Sets Role info in context and loads its clients
	 *
	 *  @param ctx context
	 *  @param  role    role information
	 *  @return list of valid client KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadClients (Properties ctx, KeyNamePair role)
	{
		if (ctx == null || role == null)
			throw new IllegalArgumentException("DB.loadClients - required parameter missing");

	//	s_log.debug("loadClients - Role: " + role.toStringX());

		ArrayList list = new ArrayList();
		KeyNamePair[] retValue = null;
		String sql = "SELECT DISTINCT r.UserLevel,"			//	1
			+ " c.AD_Client_ID,c.Name "						//	2/3 
			+ "FROM AD_Role r" 
			+ " INNER JOIN AD_Client c ON (r.AD_Client_ID=c.AD_Client_ID) "
			+ "WHERE r.AD_Role_ID=?"		//	#1
			+ " AND r.IsActive='Y' AND c.IsActive='Y'";

		PreparedStatement pstmt = null;
		//	get Role details
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, role.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadClients - No Clients for Role: " + role.toStringX());
				return null;
			}

			//  Role Info
			Env.setContext(ctx, "#AD_Role_ID", role.getKey());
			Env.setContext(ctx, "#AD_Role_Name", role.getName());
			Ini.setProperty(Ini.P_ROLE, role.getName());

			//	User Level
			Env.setContext(ctx, "#User_Level", rs.getString(1));  	//	Format 'SCO'

			//  load Clients
			do
			{
				int AD_Client_ID = rs.getInt(2);
				String Name = rs.getString(3);
				KeyNamePair p = new KeyNamePair(AD_Client_ID, Name);
				list.add(p);
			}
			while (rs.next());
			rs.close();
			pstmt.close();
			pstmt = null;
			//
			retValue = new KeyNamePair[list.size()];
			list.toArray(retValue);
			s_log.debug("loadClients - Role: " + role.toStringX() + " - clients #" + retValue.length);
		}
		catch (SQLException ex)
		{
			s_log.error("loadClients", ex);
			retValue = null;
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		return retValue;
	}   //  loadClients

	/**
	 *  Load Organizations.
	 *  <p>
	 *  Sets Client info in context and loads its organization, the role has access to
	 *
	 *  @param ctx context
	 *  @param  client    client information
	 *  @return list of valid Org KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadOrgs (Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadOrgs - required parameter missing");
		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)	//	could be number 0
			throw new UnsupportedOperationException("DB.loadOrgs - Missing Context #AD_Role_ID");
		
		int AD_Role_ID = Env.getContextAsInt(ctx,"#AD_Role_ID");
	//	s_log.debug("loadOrgs - Client: " + client.toStringX() + ", AD_Role_ID=" + AD_Role_ID);

		//	get Client details for role
		ArrayList list = new ArrayList();
		KeyNamePair[] retValue = null;
		//
		String sql = "SELECT o.AD_Org_ID,o.Name "				//	1..2
			+ "FROM AD_Role r, AD_Client c"
			+ " INNER JOIN AD_Org o ON (c.AD_Client_ID=o.AD_Client_ID OR o.AD_Org_ID=0) "
			+ "WHERE r.AD_Role_ID=?" 		//	#1
			+ " AND c.AD_Client_ID=?"		//	#2
			+ " AND o.IsSummary='N' AND o.IsActive='Y'"
			+ " AND (r.IsAccessAllOrgs='Y'"
			+ "  OR o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ra "
				+ "WHERE ra.AD_Role_ID=r.AD_Role_ID AND ra.IsActive='Y')) "
			+ "ORDER BY o.Name";
		PreparedStatement pstmt = prepareStatement(sql);
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, AD_Role_ID);
			pstmt.setInt(2, client.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadOrgs - No Org for Client: " + client.toStringX());
				return null;
			}

			//  Client Info
			Env.setContext(ctx, "#AD_Client_ID", client.getKey());
			Env.setContext(ctx, "#AD_Client_Name", client.getName());
			Ini.setProperty(Ini.P_CLIENT, client.getName());

			//  load Orgs
			do
			{
				int AD_Org_ID = rs.getInt(1);
				String Name = rs.getString(2);
				KeyNamePair p = new KeyNamePair(AD_Org_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
			pstmt = null;
			//
			retValue = new KeyNamePair[list.size()];
			list.toArray(retValue);
			s_log.debug("loadOrgs - Client: " + client.toStringX() 
				+ ", AD_Role_ID=" + AD_Role_ID + " - orgs #" + retValue.length);
		}
		catch (SQLException ex)
		{
			s_log.error("loadOrgs", ex);
			retValue = null;
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		return retValue;
	}   //  loadOrgs
	
	/**
	 *  Load Warehouses
	 *
	 * @param ctx context
	 * @param org organization
	 * @return Array of Warehouse Info
	 */
	public static KeyNamePair[] loadWarehouses(Properties ctx, KeyNamePair org)
	{
		if (ctx == null || org == null)
			throw new IllegalArgumentException("DB.loadWarehouses - required parameter missing");

	//	s_log.info("loadWarehouses - Org: " + org.toStringX());

		ArrayList list = new ArrayList();
		KeyNamePair[] retValue = null;
		String sql = "SELECT M_Warehouse_ID, Name FROM M_Warehouse "
			+ "WHERE AD_Org_ID=? AND IsActive='Y' "
			+ "ORDER BY Name";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, org.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.info("loadWarehouses - No Warehouses for Org: " + org.toStringX());
				return null;
			}

			//  load Warehousess
			do
			{
				int AD_Warehouse_ID = rs.getInt(1);
				String Name = rs.getString(2);
				KeyNamePair p = new KeyNamePair(AD_Warehouse_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
			pstmt = null;
			//
			retValue = new KeyNamePair[list.size()];
			list.toArray(retValue);
			s_log.debug("loadWarehouses - Org: " + org.toStringX()
				+ " - warehouses #" + retValue.length);
		}
		catch (SQLException ex)
		{
			s_log.error("loadWarehouses", ex);
			retValue = null;
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		return retValue;
	}   //  loadWarehouses

	
	/**
	 *	Load Preferences into Context for selected client.
	 *  <p>
	 *  Sets Org info in context and loads relevant field from
	 *	- AD_Client/Info,
	 *  - C_AcctSchema,
	 *  - C_AcctSchema_Elements
	 *	- AD_Preference
	 *  <p>
	 *  Assumes that the context is set for #AD_Client_ID, #AD_User_ID, #AD_Role_ID
	 *
	 *	@param ctx context
	 *  @param  org    org information
	 *  @param  warehouse   optional warehouse information
	 *  @param  timestamp   optional date
	 *  @param  printerName optional printer info
	 *  @returns AD_Message of error (NoValidAcctInfo) or ""
	 */
	public static String loadPreferences (Properties ctx,
		KeyNamePair org, KeyNamePair warehouse, Timestamp timestamp, String printerName)
	{
		s_log.info("loadPreferences - Org: " + org.toStringX());

		if (ctx == null || org == null)
			throw new IllegalArgumentException("DB.loadPreferences - required parameter missing");
		if (Env.getContext(ctx,"#AD_Client_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Client_ID");
		if (Env.getContext(ctx,"#AD_User_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_User_ID");
		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Role_ID");

		postMigration(ctx);
		
		//  Org Info - assumes that it is valid
		Env.setContext(ctx, "#AD_Org_ID", org.getKey());
		Env.setContext(ctx, "#AD_Org_Name", org.getName());
		Ini.setProperty(Ini.P_ORG, org.getName());

		//  Warehouse Info
		if (warehouse != null)
		{
			Env.setContext(ctx, "#M_Warehouse_ID", warehouse.getKey());
			Ini.setProperty(Ini.P_WAREHOUSE, warehouse.getName());
		}

		//	Date (default today)
		long today = System.currentTimeMillis();
		if (timestamp != null)
			today = timestamp.getTime();
		java.sql.Date sd = new java.sql.Date(today);
		Env.setContext(ctx, "#Date", sd.toString());	//	YYYY-MM-DD

		//	Optional Printer
		if (printerName == null)
			printerName = "";
		Env.setContext(ctx, "#Printer", printerName);
		Ini.setProperty(Ini.P_PRINTER, printerName);
		
		//	Load Role Info
		MRole.getDefault(ctx, true);	

		//	Other
		Env.setAutoCommit(ctx, Ini.getPropertyBool(Ini.P_A_COMMIT));
		if (MRole.getDefault(ctx, false).isShowAcct())
			Env.setContext(ctx, "#ShowAcct", Ini.getProperty(Ini.P_SHOW_ACCT));
		else
			Env.setContext(ctx, "#ShowAcct", "N");
		Env.setContext(ctx, "#ShowTrl", Ini.getProperty(Ini.P_SHOW_TRL));

		String retValue = "";
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		int AD_Org_ID =  org.getKey();
		int AD_User_ID =  Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID =  Env.getContextAsInt(ctx, "#AD_Role_ID");

		//	Other Settings
		Env.setContext(ctx, "#YYYY", "Y");
		Env.setContext(ctx, "#StdPrecision", 2);

		//	AccountSchema Info (first)
		String sql = "SELECT * "
			+ "FROM C_AcctSchema a, AD_ClientInfo c "
			+ "WHERE a.C_AcctSchema_ID=c.C_AcctSchema1_ID "
			+ "AND c.AD_Client_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			int C_AcctSchema_ID = 0;
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				//  No Warning for System
				if (AD_Role_ID != 0)
					retValue = "NoValidAcctInfo";
			}
			else
			{
				//	Accounting Info
				C_AcctSchema_ID = rs.getInt("C_AcctSchema_ID");
				Env.setContext(ctx, "$C_AcctSchema_ID", C_AcctSchema_ID);
				Env.setContext(ctx, "$C_Currency_ID", rs.getInt("C_Currency_ID"));
				Env.setContext(ctx, "$HasAlias", rs.getString("HasAlias"));
			}
			rs.close();
			pstmt.close();

			//	Accounting Elements
			sql = "SELECT ElementType "
				+ "FROM C_AcctSchema_Element "
				+ "WHERE C_AcctSchema_ID=?"
				+ " AND IsActive='Y'";
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, C_AcctSchema_ID);
			rs = pstmt.executeQuery();
			while (rs.next())
				Env.setContext(ctx, "$Element_" + rs.getString("ElementType"), "Y");
			rs.close();
			pstmt.close();

			//	This reads all relevant window neutral defaults
			//	overwriting superseeded ones.  Window specific is read in Mainain
			sql = "SELECT Attribute, Value, AD_Window_ID "
				+ "FROM AD_Preference "
				+ "WHERE AD_Client_ID IN (0, @#AD_Client_ID@)"
				+ " AND AD_Org_ID IN (0, @#AD_Org_ID@)"
				+ " AND (AD_User_ID IS NULL OR AD_User_ID=0 OR AD_User_ID=@#AD_User_ID@)"
				+ " AND IsActive='Y' "
				+ "ORDER BY Attribute, AD_Client_ID, AD_User_ID DESC, AD_Org_ID";
				//	the last one overwrites - System - Client - User - Org - Window
			sql = Env.parseContext(ctx, 0, sql, false);
			if (sql.length() == 0)
				s_log.error("loadPreferences - Missing Environment");
			else
			{
				pstmt = prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					int AD_Window_ID = rs.getInt(3);
					String at = "";
					if (rs.wasNull())
						at = "P|" + rs.getString(1);
					else
						at = "P" + AD_Window_ID + "|" + rs.getString(1);
					String va = rs.getString(2);
					Env.setContext(ctx, at, va);
				}
				rs.close();
				pstmt.close();
			}

			//	Default Values
			s_log.info("Default Values ...");
			sql = "SELECT t.TableName, c.ColumnName "
				+ "FROM AD_Column c "
				+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID) "
				+ "WHERE c.IsKey='Y' AND t.IsActive='Y'"
				+ " AND EXISTS (SELECT * FROM AD_Column cc "
				+ " WHERE ColumnName = 'IsDefault' AND t.AD_Table_ID=cc.AD_Table_ID AND cc.IsActive='Y')";
			pstmt = prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next())
				loadDefault (ctx, rs.getString(1), rs.getString(2));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException e)
		{
			s_log.error("loadPreferences", e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		//
		Ini.saveProperties(Ini.isClient());
		//	Country
		Env.setContext(ctx, "#C_Country_ID", MCountry.getDefault(ctx).getC_Country_ID());
		return retValue;
	}	//	loadPreferences

	/**
	 *	Load Default Value for Table into Context.
	 *
	 *  @param ctx context
	 *  @param TableName table name
	 *  @param ColumnName column name
	 */
	private static void loadDefault (Properties ctx, String TableName, String ColumnName)
	{
		if (TableName.startsWith("AD_Window")
			|| TableName.startsWith("AD_PrintFormat")
			|| TableName.startsWith("AD_Workflow") )
			return;
		String value = null;
		//
		String sql = "SELECT " + ColumnName + " FROM " + TableName	//	most specific first
			+ " WHERE IsDefault='Y' AND IsActive='Y' ORDER BY AD_Client_ID DESC, AD_Org_ID DESC";
		sql = MRole.getDefault(ctx, false).addAccessSQL(sql, 
			TableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				value = rs.getString(1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException e)
		{
			s_log.error("loadDefault - " + TableName + " (" + sql + ")", e);
			return;
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		//	Set Context Value
		if (value != null && value.length() != 0)
		{
			if (TableName.equals("C_DocType"))
				Env.setContext(ctx, "#C_DocTypeTarget_ID", value);
			else
				Env.setContext(ctx, "#" + ColumnName, value);
		}
	}	//	loadDefault

	/**
	 * 	Check need for post Upgrade
	 *	@return true if post upgrade ran - false if there was no need
	 */
	private static boolean postMigration (Properties ctx)
	{
		MSystem system = MSystem.get(ctx); 
		if (!system.isJustMigrated())
			return false;
		
		s_log.info("postMigration");
		//	Role update
		String sql = "SELECT * FROM AD_Role";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
			{
				MRole role = new MRole (ctx, rs);
				role.updateAccessRecords();
			}
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error ("postMigration(1)", e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		
		//	Reset Flag
		system.setIsJustMigrated(false);
		return system.save();
	}	//	checkUpgrade
	
	
	/**************************************************************************
	 *  Set connection
	 *  @param cc connection
	 */
	public static void setDBTarget (CConnection cc)
	{
		if (cc == null)
			throw new IllegalArgumentException("DB.setDBTarget connection is NULL");

		DB.closeTarget();
		//
		if (s_cc == null)
			s_cc = cc;
		synchronized (s_cc)    //  use as mutex
		{
			s_cc = cc;
			s_connections = null;
			s_connectionRW = null;
		}
		s_cc.setDataSource();
		s_log.debug("setDBTarget - " + s_cc + " - DS=" + s_cc.isDataSource());
	//	Trace.printStack();
	}   //  setDBTarget

	/**
	 *  Is there a connection to the database ?
	 *  @return true, if connected to database
	 */
	public static boolean isConnected()
	{
		try
		{
			getConnectionRW();	//	try to get a connection
			return true;
		}
		catch (Exception e)
		{
		}
		return false;
	}   //  isConnected

	/**
	 *	Return (pooled) r/w AutoCommit, read committed connection.
	 *	For Transaction control use Trx.getConnection()
	 *  @return Connection (r/w)
	 */
	public static Connection getConnectionRW ()
	{
		//	check health of connection
		try
		{
			if (s_connectionRW == null)
				;
			else if (s_connectionRW.isClosed())
			{
				System.out.println("DB.getConnectionRW - closed");
				s_connectionRW = null;
			}
			else if (s_connectionRW instanceof OracleConnection && ((OracleConnection)s_connectionRW).pingDatabase(1) < 0)
			{
				System.out.println("DB.getConnectionRW - no ping");
				s_connectionRW = null;
			}
			else
			{
				if (s_connectionRW.getTransactionIsolation() != Connection.TRANSACTION_SERIALIZABLE)
					s_connectionRW.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			}
		}
		catch (Exception e)
		{
			s_connectionRW = null;
		}
		//	Get new
		if (s_connectionRW == null)
		{
			s_connectionRW = s_cc.getConnection (true, Connection.TRANSACTION_SERIALIZABLE);
			if (Log.isTraceLevel(8))
				s_log.debug("getConnectionRW - " + s_connectionRW);
		}
		if (s_connectionRW == null)
			throw new UnsupportedOperationException("DB.getConnectionRW - @NoDBConnection@");
		//
	//	System.err.println ("DB.getConnectionRW - " + s_connectionRW); 
	//	Trace.printStack();
		return s_connectionRW;
	}   //  getConnectionRW

	/**
	 *	Return read committed, read/only from pool.
	 *  @return Connection (r/o)
	 */
	public static Connection getConnectionRO ()
	{
		try
		{
			synchronized (s_cc)    //  use as mutex as s_connection is null the first time
			{
				if (s_connections == null)
					s_connections = createConnections (Connection.TRANSACTION_READ_COMMITTED);     //  see below
			}
		}
		catch (Exception e)
		{
			s_log.error("getConnectionRO", e);
		}

		//  check health of connection
		int pos = s_conCount++;
		int connectionNo = pos % s_conCacheSize;
		Connection connection = s_connections[connectionNo];
		try
		{
			if (connection == null)
				;
			else if (connection.isClosed())
			{
			//	RowSet.close also closes connection!
			//	System.out.println("DB.getConnectionRO - closed #" + connectionNo);
				connection = null;
			}
			else if (connection instanceof OracleConnection && ((OracleConnection)connection).pingDatabase(1) < 0)
			{
				System.out.println("DB.getConnectionRO - no ping #" + connectionNo);
				connection = null;
			}
			else
			{
				if (!connection.isReadOnly())
					connection.setReadOnly(true);
				if (connection.getTransactionIsolation() != Connection.TRANSACTION_READ_COMMITTED)
					connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			}
		}
		catch (Exception e)
		{
			System.out.println("DB.getConnectionRO #" + connectionNo + " - " + e.toString());
			connection = null;
		}
		//	Get new
		if (connection == null)
		{
			if (Log.isTraceLevel(8))
				s_log.debug("getConnectionRO - replacing connection #" + connectionNo);
			connection = s_cc.getConnection (true, Connection.TRANSACTION_READ_COMMITTED); //  see above
			try
			{
				if (connection != null)
					connection.setReadOnly(true);
			}
			catch (Exception e)
			{
				System.err.println("DB.getConnectionRO - Cannot set to R/O - " + e);
			} 
			s_connections[connectionNo] = connection;
		}
		if (connection == null)
			throw new UnsupportedOperationException("DB.getConnectionRO - @NoDBConnection@");
		if (Log.isTraceLevel(10))
			s_log.debug("getConnectionRO - #" + connectionNo + " - " + connection);
	//	System.err.println ("DB.getConnectionRO - " + connection); 
		return connection;
	}	//	getConnectionRO

	/**
	 *	Create new Connection.
	 *  The connection must be closed explicitly by the application
	 *
	 *  @param autoCommit auto commit
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Connection connection
	 */
	public static Connection createConnection (boolean autoCommit, int trxLevel)
	{
		s_log.debug("createConnection " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", AutoCommit=" + autoCommit + ", TrxLevel=" + trxLevel);
		return s_cc.getConnection (autoCommit, trxLevel);
	}	//	createConnection

	/**
	 *	Create new set of r/o Connections.
	 *  R/O connection might not be supported by DB
	 *
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Array of Connections (size based on s_conCacheSize)
	 */
	private static Connection[] createConnections (int trxLevel)
	{
		s_log.debug("createConnections (" + s_conCacheSize + ") " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() 
			+ ", TrxLevel=" + CConnection.getTransactionIsolationInfo(trxLevel));
		Connection cons[] = new Connection[s_conCacheSize];
		try
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				cons[i] = s_cc.getConnection (true, trxLevel);  //  auto commit
				if (cons[i] == null)
					System.err.println("createConnections - connection is NULL");	//	don't use log
			}
		}
		catch (Exception e)
		{
			//  Don't use Log
			System.err.println("DB.createConnections - " + e.getMessage());
		}
		return cons;
	}	//	createConnections

	/**
	 *  Get Database Driver.
	 *  Access to database specific functionality.
	 *  @return Compiere Database Driver
	 */
	public static CompiereDatabase getDatabase()
	{
		if (s_cc != null)
			return s_cc.getDatabase();
		return null;
	}   //  getDatabase

	/**
	 * 	Get Database Info
	 *	@return info
	 */
	public static String getDatabaseInfo()
	{
		if (s_cc != null)
			return s_cc.toStringDetail();
		return "No DB";
	}	//	getDatabaseInfo
	
	/**************************************************************************
	 *  Check database Version with Code version
	 *  @param ctx context
	 *  @return true if Database version (date) is the same
	 */
	public static boolean isDatabaseOK (Properties ctx)
	{
		//  Check Version
		String version = "?";
		String sql = "SELECT Version FROM AD_System";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				version = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("Problem with AD_System Table - Run system.sql script - " + e.toString());
			return false;
		}
		s_log.info("isDatabaseOK - DB_Version=" + version);
		//  Identical DB version
		if (Compiere.DB_VERSION.equals(version))
			return true;

		String AD_Message = "DatabaseVersionError";
		String title = org.compiere.Compiere.getName() + " " +  Msg.getMsg(ctx, AD_Message, true);
		//	Code assumes Database version {0}, but Database has Version {1}.
		String msg = Msg.getMsg(ctx, AD_Message);	//	complete message
		msg = MessageFormat.format(msg, new Object[] {Compiere.DB_VERSION, version});
		Object[] options = { UIManager.get("OptionPane.noButtonText"), "Migrate" };
		int no = JOptionPane.showOptionDialog (null, msg,
			title, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
			UIManager.getIcon("OptionPane.errorIcon"), options, options[0]);
		if (no == 1)
		{
			try
			{
				Class.forName("com.compiere.client.StartMaintain").newInstance();
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog (null,
					ex.getMessage() + "\nSee: http://www.compiere.com/maintain",
					title, JOptionPane.ERROR_MESSAGE);
				s_log.error("isDatabaseOK - " + ex.getMessage());
			}
		}
		return false;
	}   //  isDatabaseOK

	
	/**************************************************************************
	 *	Secure Context Login.
	 *  @param uid uid
	 *  @param pwd pwd
	 *  @param role role
	 *  @return true if connected
	 */
	public static boolean login_context (String uid, String pwd, String role)
	{
		if (uid == null || pwd == null || role == null || uid.length() == 0 || pwd.length() == 0 || role.length() == 0)
			throw new IllegalArgumentException("DB.login_context - required parameter missing");
		s_log.info("login_context uid=" + uid);
		if (uid == null || pwd == null || role == null)
			return false;
		//
		String SQL = "{CALL Compiere_Context.Login(?,?,?,?)}";
		boolean result = true;
		try
		{
			CallableStatement cstmt = getConnectionRO().prepareCall(SQL);
			cstmt.setString(1, uid);
			cstmt.setString(2, pwd);
			cstmt.setString(3, role);
			cstmt.setString(4, Language.getBaseAD_Language());
			result = cstmt.execute();
			cstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("login_context", e);
			result = false;
		}
		return result;
	}	//	login_context

	
	/**************************************************************************
	 *	Close Target
	 */
	public static void closeTarget()
	{
		boolean closed = false;
		//	RO connection
		if (s_connections != null)
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				try
				{
					if (s_connections[i] != null)
					{
						closed = true;
						s_connections[i].close();
					}
				}
				catch (SQLException e)
				{
					s_log.warn("close connection #" + i + " - " + e.getMessage());
				}
				s_connections[i] = null;
			}
		}
		s_connections = null;
		//	RW connection
		try
		{
			if (s_connectionRW != null)
			{
				closed = true;
				s_connectionRW.close();
			}
		}
		catch (SQLException e)
		{
			s_log.error("close R/W connection", e);
		}
		s_connectionRW = null;
		//	CConnection
		if (s_cc != null)
		{
			closed = true;
			s_cc.setDataSource(null);
		}
		s_cc = null;
		if (closed)
			s_log.debug("closeTarget");
	}	//	closeTarget

	
	/**************************************************************************
	 *	Prepare Forward Read Only Call
	 *  @param RO_SQL sql (RO)
	 *  @return Callable Statement
	 */
	public static CallableStatement prepareCall(String RO_SQL)
	{
		if (RO_SQL == null || RO_SQL.length() == 0)
			throw new IllegalArgumentException("DB.prepareCall - required parameter missing - " + RO_SQL);
		//
		String sql = getDatabase().convertStatement(RO_SQL);
		try
		{
			return getConnectionRO().prepareCall
				(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		}
		catch (SQLException e)
		{
			s_log.error("prepareCall (" + sql + ")", e);
		}
		return null;
	}	//	prepareCall

	
	/**************************************************************************
	 *	Prepare Read Only Statement
	 *  @param RO_SQL sql (RO)
	 *  @return Prepared Statement
	 */
	public static CPreparedStatement prepareStatement (String RO_SQL)
	{
		return prepareStatement(RO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, null);
	}	//	prepareStatement

	/**
	 *	Prepare Read Only Statement
	 *  @param RO_SQL sql (RO)
	 * 	@param trxName transaction
	 *  @return Prepared Statement
	 */
	public static CPreparedStatement prepareStatement (String RO_SQL, String trxName)
	{
		return prepareStatement(RO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, trxName);
	}	//	prepareStatement
	
	/**
	 *	Prepare Statement.
	 *  @param SQL sql statement
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Prepared Statement r/o or r/w depending on concur
	 */
	public static CPreparedStatement prepareStatement(String sql, 
		int resultSetType, int resultSetConcurrency)
	{
		return prepareStatement(sql, resultSetType, resultSetConcurrency, null);
	}	//	prepareStatement

	/**
	 *	Prepare Statement.
	 *  @param sql sql statement
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 * 	@param trxName transaction name
	 *  @return Prepared Statement r/o or r/w depending on concur
	 */
	public static CPreparedStatement prepareStatement(String sql, 
		int resultSetType, int resultSetConcurrency, String trxName)
	{
		if (sql == null || sql.length() == 0)
			throw new IllegalArgumentException("DB.prepareStatement - No SQL");
		//
		return new CPreparedStatement(resultSetType, resultSetConcurrency, sql, trxName);
	}	//	prepareStatement

	/**
	 *	Create Read Only Statement
	 *  @return Statement
	 */
	public static Statement createStatement()
	{
		return createStatement (ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, null);
	}	//	createStatement

	/**
	 *	Create Statement.
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 * 	@param trxName transaction name
	 *  @return Statement - either r/w ir r/o depending on concur
	 */
	public static Statement createStatement(int resultSetType, int resultSetConcurrency, String trxName)
	{
		return new CStatement(resultSetType, resultSetConcurrency, trxName);
	}	//	createStatement

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql)
	{
		return executeUpdate(sql, false, null);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 * 	@param trxName optional transaction name
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql, String trxName)
	{
		return executeUpdate(sql, false, trxName);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 * 	@param ignoreError if true, no execution error is reported
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql, boolean ignoreError)
	{
		return executeUpdate (sql, ignoreError, null);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param sql sql
	 * 	@param ignoreError if true, no execution error is reported
	 * 	@param trxName optional transaction name
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String sql, boolean ignoreError, String trxName)
	{
		if (sql == null || sql.length() == 0)
			throw new IllegalArgumentException("DB.executeUpdate - required parameter missing - " + sql);
		//
		int no = -1;
		CPreparedStatement cs = new CPreparedStatement(ResultSet.TYPE_FORWARD_ONLY, 
			ResultSet.CONCUR_UPDATABLE, sql, trxName);
		
		try
		{
			no = cs.executeUpdate();
			//	No Transaction - Commit
			if (trxName == null)
			{
				cs.commit();	//	Local commit
			//	Connection conn = cs.getConnection();
			//	if (conn != null && !conn.getAutoCommit())	//	is null for remote
			//		conn.commit();
			}
		}
		catch (SQLException e)
		{
			if (!ignoreError)
			{
				s_log.error ("executeUpdate - " + cs.getSql(), e);
				Log.saveError ("DBExecuteError", e);
			}
		}
		finally
		{
			//  Always close cursor
			try
			{
				cs.close();
			}
			catch (SQLException e2)
			{
				s_log.error("executeUpdate - cannot close statement");
			}
		}
		return no;
	}	//	executeUpdate

	/**
	 *	Execute Update and throw exception.
	 *  @param SQL sql
	 *  @return number of rows updated or -1 if error
	 * 	@throws SQLException
	 */
	public static int executeUpdateEx (String SQL, String trxName) throws SQLException
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.executeUpdate - required parameter missing - " + SQL);
		//
		String sql = getDatabase().convertStatement(SQL);
		int no = -1;
		SQLException ex = null;
		Connection conn = null;
		Statement stmt = null;
		try
		{
			Trx trx = trxName == null ? null : Trx.get(trxName, true);
			if (trx != null)
				conn = trx.getConnection();
			else
				conn = DB.getConnectionRW ();
			stmt = conn.createStatement();
			no = stmt.executeUpdate(sql);
		}
		catch (SQLException e)
		{
			ex = e;
		}
		finally
		{
			//  Always close cursor
			try
			{
				stmt.close();
			}
			catch (SQLException e2)
			{
				s_log.error("executeUpdate - cannot close statement");
			}
		}
		if (ex != null)
			throw new SQLException(ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
		return no;
	}	//	execute Update

	/**
	 *	Commit - commit on RW connection.
	 *  Is not required as RW connection is AutoCommit (exception: with transaction)
	 *  @param throwException if true, re-throws exception
	 * 	@param trxName transaction name
	 *  @return true if not needed or success
	 *  @throws SQLException
	 */
	public static boolean commit (boolean throwException, String trxName) throws SQLException
	{
		try
		{
			Connection conn = null;
			Trx trx = trxName == null ? null : Trx.get(trxName, true);
			if (trx != null)
				conn = trx.getConnection();
			else
				conn = DB.getConnectionRW ();
		//	if (!conn.getAutoCommit())
			conn.commit();
		}
		catch (SQLException e)
		{
			s_log.error("commit", e);
			if (throwException)
				throw e;
			return false;
		}
		return true;
	}	//	commit

	/**
	 *	Rollback - rollback on RW connection.
	 *  Is has no effect as RW connection is AutoCommit (exception: with transaction)
	 *  @param throwException if true, re-throws exception
	 * 	@param trxName transaction name
	 *  @return true if not needed or success
	 *  @throws SQLException
	 */
	public static boolean rollback (boolean throwException, String trxName) throws SQLException
	{
		try
		{
			Connection conn = null;
			Trx trx = trxName == null ? null : Trx.get(trxName, true);
			if (trx != null)
				conn = trx.getConnection();
			else
				conn = DB.getConnectionRW ();
		//	if (!conn.getAutoCommit())
			conn.rollback();
		}
		catch (SQLException e)
		{
			s_log.error("rollback", e);
			if (throwException)
				throw e;
			return false;
		}
		return true;
	}	//	commit

	/**
	 * 	Get Row Set.
	 * 	When a Rowset is closed, it also closes the underlying connection.
	 * 	If the created RowSet is transfered by RMI, closing it makes no difference 
	 *	@param sql sql
	 *	@param local local RowSet (own connection)
	 *	@return row set or null
	 */
	public static RowSet getRowSet (String sql, boolean local)
	{
		RowSet retValue = null;
		CStatementVO info = new CStatementVO ( 
			RowSet.TYPE_SCROLL_INSENSITIVE, RowSet.CONCUR_READ_ONLY, sql);
		CPreparedStatement stmt = new CPreparedStatement(info);
		if (local)
		{
			retValue = stmt.local_getRowSet();
		}
		else
		{
			retValue = stmt.remote_getRowSet();
		}
		return retValue;
	}	//	getRowSet

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else if (Log.isTraceLevel(6))
				s_log.warn("getSQLValue - No Value " + sql);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int int_param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, String int_param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setString(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@param s_param2 parameter 2
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int int_param1, String s_param2)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			pstmt.setString(2, s_param2);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value: " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get String Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or null
	 */
	public static String getSQLValueString (String sql, int int_param1)
	{
		String retValue = null;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getString(1);
			else
				s_log.warn("getSQLValueString - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValueString - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValueString

	
	/**
	 * 	Get Array of Key Name Pairs
	 *	@param sql select with id / name as first / second column
	 *	@param optional if true (-1,"") is added 
	 *	@return array of key name pairs
	 */
	public static KeyNamePair[] getKeyNamePairs(String sql, boolean optional)
	{
		PreparedStatement pstmt = null;
		ArrayList list = new ArrayList();
		if (optional)
			list.add (new KeyNamePair(-1, ""));
		try
		{
			pstmt = DB.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new KeyNamePair(rs.getInt(1), rs.getString(2)));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getKeyNamePairs " + sql, e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
	//	s_log.debug("getKeyNamePairs #" + retValue.length);
		return retValue;		
	}	//	getKeyNamePairs
	
	
	/**************************************************************************
	 *	Get next number for Key column = 0 is Error.
	 *   * @param ctx client
	@param TableName table name
	 * 	@param trxName optionl transaction name
	 *  @return next no
	 */
	public static int getNextID (Properties ctx, String TableName, String trxName)
	{
		if (ctx == null)
			throw new IllegalArgumentException("DB.getNextID - Context missing");
		if (TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getNextID - TableName missing");
		return getNextID(Env.getAD_Client_ID(ctx), TableName, trxName);
	}	//	getNextID

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param AD_Client_ID client
	 *  @param TableName table name
	 * 	@param trxName optional Transaction Name
	 *  @return next no
	 */
	public static int getNextID (int AD_Client_ID, String TableName, String trxName)
	{
		if (isClientRemote())
		{
			Server server = CConnection.get().getServer();
			try
			{
				if (server != null)
				{
					int id = server.getNextID(AD_Client_ID, TableName, null);
					if (Log.isTraceLevel(10))
						s_log.debug("getNextID - server => " + id);
					return id;
				}
				s_log.error("getNextID - AppsServer not found - " + TableName + ", Remote=" + DB.isClientRemote());
			}
			catch (RemoteException ex)
			{
				s_log.error("getNextID - AppsServer error", ex);
			}
			//	Try locally
		}
		return MSequence.getNextID (AD_Client_ID, TableName, trxName);
	}	//	getNextID
	
	/**
	 * 	Get Document No based on Document Type
	 *	@param C_DocType_ID document type
	 * 	@param trxName optional Transaction Name
	 *	@return document no or null
	 */
	public static String getDocumentNo(int C_DocType_ID, String trxName)
	{
		if (isClientRemote())
		{
			Server server = CConnection.get().getServer();
			try
			{
				if (server != null)
				{
					String no = server.getDocumentNo (C_DocType_ID, trxName);
					if (Log.isTraceLevel(10))
						s_log.debug("getNextID - server => " + no);
					return no;
				}
				s_log.error("getDocumentNo - AppsServer not found - " + C_DocType_ID + ", Remote=" + DB.isClientRemote());
			}
			catch (RemoteException ex)
			{
				s_log.error("getDocumentNo - AppsServer error", ex);
			}
		}
		return MSequence.getDocumentNo (C_DocType_ID, trxName);
	}	//	getDocumentNo


	/**
	 * 	Get Document No from table
	 *	@param AD_Client_ID client
	 *	@param TableName table name
	 * 	@param trxName optional Transaction Name
	 *	@return document no or null
	 */
	public static String getDocumentNo(int AD_Client_ID, String TableName, String trxName)
	{
		if (isClientRemote())
		{
			Server server = CConnection.get().getServer();
			try
			{
				if (server != null)
				{
					String no = server.getDocumentNo (AD_Client_ID, TableName, trxName);
					if (Log.isTraceLevel(10))
						s_log.debug("getNextID - server => " + no);
					return no;
				}
				s_log.error("getDocumentNo - AppsServer not found - " + TableName + ", Remote=" + DB.isClientRemote());
			}
			catch (RemoteException ex)
			{
				s_log.error("getDocumentNo - AppsServer error", ex);
			}
		}
		return MSequence.getDocumentNo (AD_Client_ID, TableName, trxName);
	}	//	getDocumentNo

	/**
	 *	Get Document Number for current document.
	 *  <br>
	 *  - first search for DocType based Document No
	 *  - then Search for DocumentNo based on TableName
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @param onlyDocType Do not search for document no based on TableName
	 * 	@param trxName optional Transaction Name
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (Properties ctx, int WindowNo, 
		String TableName, boolean onlyDocType, String trxName)
	{
		if (ctx == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("getDocumentNo - required parameter missing");
		int AD_Client_ID = Env.getContextAsInt(ctx, WindowNo, "AD_Client_ID");

		//	Get C_DocType_ID from context - NO Defaults -
		int C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID");
		if (C_DocType_ID == 0)
			C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID");
		if (C_DocType_ID == 0)
		{
			s_log.debug("getDocumentNo - for Window=" + WindowNo
				+ " - Target=" + Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID") + "/" + Env.getContextAsInt(ctx, WindowNo, "C_DocTypeTarget_ID")
				+ " - Actual=" + Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID") + "/" + Env.getContextAsInt(ctx, WindowNo, "C_DocType_ID"));
			return getDocumentNo (AD_Client_ID, TableName, trxName);
		}

		String retValue = getDocumentNo (C_DocType_ID, trxName);
		if (!onlyDocType && retValue == null)
			return getDocumentNo (AD_Client_ID, TableName, trxName);
		return retValue;
	}	//	getDocumentNo

	/**
	 * 	Is this is remote client connection
	 *	@return true if client and RMI or Objects on Server
	 */
	public static boolean isClientRemote()
	{
		return Ini.isClient() 
			&& (CConnection.get().isRMIoverHTTP() || Ini.isClientObjects())
			&& CConnection.get().isAppsServerOK(false);
	}	//	isClientRemote
	
	
	
	/**************************************************************************

	/**
	 *	Print SQL Warnings.
	 *  <br>
	 *		Usage: DB.printWarning(rs.getWarnings(), "xx");
	 *  @param comment comment
	 *  @param warning warning
	 */
	public static void printWarning(String comment, SQLWarning warning)
	{
		if (comment == null || warning == null || comment.length() == 0)
			throw new IllegalArgumentException("DB.printException - required parameter missing");
		s_log.warn("SQL Warning: " + comment);
		if (warning == null)
			return;
		//
		SQLWarning warn = warning;
		while (warn != null)
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append(warn.getMessage());
			buffer.append("; State=").append(warn.getSQLState()).append("; ErrorCode=").append(warn.getErrorCode());
			s_log.warn(buffer.toString());
			warn = warn.getNextWarning();
		}
	}	//	printWarning

	/**
	 *  Create SQL TO Date String from Timestamp
	 *
	 *  @param  time Date to be converted
	 *  @param  dayOnly true if time set to 00:00:00
	 *
	 *  @return TO_DATE('2001-01-30 18:10:20',''YYYY-MM-DD HH24:MI:SS')
	 *      or  TO_DATE('2001-01-30',''YYYY-MM-DD')
	 */
	public static String TO_DATE (Timestamp time, boolean dayOnly)
	{
		if (time == null)
		{
			if (dayOnly)
				return "TRUNC(SysDate)";
			return "SysDate";
		}

		StringBuffer dateString = new StringBuffer("TO_DATE('");
		//  YYYY-MM-DD HH24:MI:SS.mmmm  JDBC Timestamp format
		String myDate = time.toString();
		if (dayOnly)
		{
			dateString.append(myDate.substring(0,10));
			dateString.append("','YYYY-MM-DD')");
		}
		else
		{
			dateString.append(myDate.substring(0, myDate.indexOf(".")));	//	cut off miliseconds
			dateString.append("','YYYY-MM-DD HH24:MI:SS')");
		}
		return dateString.toString();
	}   //  TO_DATE

	/**
	 *  Create SQL TO Date String from Timestamp
	 *  @param day day time
	 *  @return TO_DATE String
	 */
	public static String TO_DATE (Timestamp day)
	{
		return TO_DATE(day, true);
	}   //  TO_DATE

	/**
	 *  Create SQL for formatted Date, Number
	 *
	 *  @param  columnName  the column name in the SQL
	 *  @param  displayType Display Type
	 *  @param  AD_Language 6 character language setting (from Env.LANG_*)
	 *
	 *  @return TRIM(TO_CHAR(columnName,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.'''))
	 *      or TRIM(TO_CHAR(columnName,'TM9')) depending on DisplayType and Language
	@see org.compiere.util.DisplayType
	 *  @see org.compiere.util.Env
	 *
	 *   */
	public static String TO_CHAR (String columnName, int displayType, String AD_Language)
	{
		if (columnName == null || AD_Language == null || columnName.length() == 0)
			throw new IllegalArgumentException("DB.TO_CHAR - required parameter missing");

		StringBuffer retValue = new StringBuffer("TRIM(TO_CHAR(");
		retValue.append(columnName);

		//  Numbers
		if (DisplayType.isNumeric(displayType))
		{
			if (displayType == DisplayType.Amount)
				retValue.append(",'9G999G990D00'");
			else
				retValue.append(",'TM9'");
			//  TO_CHAR(GrandTotal,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.''')
			if (!Language.isDecimalPoint(AD_Language))      //  reversed
				retValue.append(",'NLS_NUMERIC_CHARACTERS='',.'''");
		}
		else if (DisplayType.isDate(displayType))
		{
			retValue.append(",'")
				.append(Language.getLanguage(AD_Language).getDBdatePattern())
				.append("'");
		}

		retValue.append("))");
		//
		return retValue.toString();
	}   //  TO_CHAR_Number


	/**
	 *  Package Strings for SQL command
	 *  @param txt  String with text
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt)
	{
		return TO_STRING (txt, 0);
	}   //  TO_STRING

	/**
	 *	Package Strings for SQL command.
	 *  <pre>
	 *		-	include in ' (single quotes)
	 *		-	replace ' with ''
	 *  </pre>
	 *  @param txt  String with text
	 *  @param maxLength    Maximum Length of content or 0 to ignore
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt, int maxLength)
	{
		if (txt == null)
			return "NULL";

		//  Length
		String text = txt;
		if (maxLength != 0 && text.length() > maxLength)
			text = txt.substring(0, maxLength);

		char quote = '\'';
		//  copy characters		(wee need to look through anyway)
		StringBuffer out = new StringBuffer();
		out.append(quote);		//	'
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (c == quote)
				out.append("''");
			else
				out.append(c);
		}
		out.append(quote);		//	'
		//
		return out.toString();
	}	//	TO_STRING

}	//	DB

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
package org.compiere.util;

import java.math.*;
import java.util.*;
import java.text.*;
import java.security.*;
import javax.swing.*;

import java.sql.*;
import javax.sql.*;
import oracle.jdbc.*;

import org.compiere.Compiere;
import org.compiere.db.*;
import org.compiere.model.MRole;

/**
 *  General Database Interface
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB.java,v 1.34 2003/11/06 07:09:09 jjanke Exp $
 */
public final class DB
{
	/** Connection Descriptor           */
	private static CConnection      s_cc = null;
	/** Connection Cache r/o            */
	private static Connection[]		s_connections = null;
	/** Connection Cache Size           */
	private static int              s_conCacheSize = 2;   //  client
	/** Connection counter              */
	private static int              s_conCount = 0;
	/** Connection r/w                  */
	private static Connection		s_connectionRW = null;
	/**	Logger							*/
	private static Logger			s_log = Logger.getCLogger (DB.class);

	/**
	 *	Client Login.
	 *  <p>
	 *  - Get Connection
	 *  - Compare User info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 * @param ctx context
	 * @param cc connection
	 * @param app_user user
	 * @param app_pwd pwd
	 * @param force ignore pwd
	 * @return  Array of Role KeyNamePair or null if error
	 * The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	protected static KeyNamePair[] login (Properties ctx,
		CConnection cc,
		String app_user, String app_pwd, boolean force)
	{
		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		//	Establish connection
		closeTarget();
		setDBTarget(cc);
		Env.setContext(ctx, "#Host", s_cc.getAppsHost());
		Env.setContext(ctx, "#Database", s_cc.getDbName());
		if (getConnectionRO() == null)
		{
			Log.saveError("NoDatabase", "");
			return null;
		}
		if (app_pwd == null)
			return null;
		//
		return loginDB (ctx, app_user, app_pwd, force);
	}   //  login

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user Principal
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx, Principal app_user)
	{
		if (app_user == null)
			return null;
		//  login w/o password as previously authorized
		return loginDB (ctx, app_user.getName(), null, false);
	}   //  app_user

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user user id
	 *  @param app_pwd password
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx,
		String app_user, String app_pwd)
	{
		if (app_pwd == null)
			return null;
		return loginDB (ctx, app_user, app_pwd, false);
	}   //  login

	/**
	 *  Actual DB login procedure.
	 *
	 *  @param ctx context
	 *  @param app_user user
	 *  @param app_pwd pwd
	 *  @param force ignore pwd
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	private static KeyNamePair[] loginDB (Properties ctx,
		String app_user, String app_pwd, boolean force)
	{
		s_log.info("login - User=" + app_user);

		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		if (app_user == null)
			return null;

		KeyNamePair[] retValue = null;
		ArrayList list = new ArrayList();
		//
		String sql = "SELECT AD_User.AD_User_ID, AD_User.Description,"
			+ " AD_Role.AD_Role_ID, AD_Role.Name "
			+ "FROM AD_User, AD_User_Roles, AD_Role "
			+ "WHERE AD_User.AD_User_ID=AD_User_Roles.AD_User_ID"
			+ " AND AD_User_Roles.AD_Role_ID=AD_Role.AD_Role_ID"
			+ " AND AD_User.Name=?"		        							//	#1
			+ " AND AD_User.IsActive='Y' AND AD_Role.IsActive='Y' AND AD_User_Roles.IsActive='Y'";
		if (app_pwd != null)
			sql += " AND (AD_User.Password=? OR AD_User.Password=?)";   	//  #2/3
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setString(1, app_user);
			if (app_pwd != null)
			{
				pstmt.setString(2, app_pwd);
				pstmt.setString(3, Secure.getDigest(app_pwd));
			}
			//	execute a query
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())		//	no record found
				if (force)
				{
					Env.setContext(ctx, "#AD_User_Name", "System");
					Env.setContext(ctx, "#AD_User_ID", "0");
					Env.setContext(ctx, "#AD_User_Description", "System Forced Login");
					Env.setContext(ctx, "#User_Level", "S  ");  	//	Format 'SCO'
					Env.setContext(ctx, "#User_Client", "0");		//	Format c1, c2, ...
					Env.setContext(ctx, "#User_Org", "0"); 		//	Format o1, o2, ...
					rs.close();
					pstmt.close();
					retValue = new KeyNamePair[] {new KeyNamePair(0, "System Administrator")};
					return retValue;
				}
				else
				{
					rs.close();
					pstmt.close();
					Log.saveError("UserPwdError", app_user, false);
					return null;
				}

			Env.setContext(ctx, "#AD_User_Name", app_user);
			Env.setContext(ctx, "#AD_User_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#SalesRep_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#AD_User_Description", rs.getString("Description"));
			//
			Ini.setProperty(Ini.P_UID, app_user);
			if (Ini.getPropertyBool(Ini.P_STORE_PWD))
				Ini.setProperty(Ini.P_PWD, app_pwd);

			do	//	read all roles
			{
				int AD_Role_ID = rs.getInt("AD_Role_ID");
				String Name = rs.getString("Name");
				KeyNamePair p = new KeyNamePair(AD_Role_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("login", ex);
			Log.saveError("DBLogin", ex.getLocalizedMessage());
			return null;
		}

		//	Change via SQL detection comes here
		Env.setContext(ctx, "#User_SecurityID", "85263");
		//
		retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# roles = " + retValue.length);
		return retValue;
	}	//	login

	/**
	 *  Load Clients.
	 *  <p>
	 *  Sets Role info in context and loads its clients
	 *
	 *  @param ctx context
	 *  @param  role    role information
	 *  @return list of valid client KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadClients (Properties ctx, KeyNamePair role)
	{
		if (ctx == null || role == null)
			throw new IllegalArgumentException("DB.loadClients - required parameter missing");

		s_log.debug("loadClients - Role=" + role.toString());

		ArrayList list = new ArrayList();
		//	get Role details
		try
		{
			String sql = "SELECT DISTINCT r.UserLevel,r.ClientList,r.OrgList,"
				+ " r.C_Currency_ID,r.AmtApproval, oa.AD_Client_ID,c.Name "
				+ "FROM AD_Role r"
				+ " INNER JOIN AD_Role_OrgAccess oa ON (r.AD_Role_ID=oa.AD_Role_ID)"
				+ " INNER JOIN AD_Client c ON (oa.AD_Client_ID=c.AD_Client_ID) "
				+ "WHERE r.AD_Role_ID=?"		//	#1
				+ " AND r.IsActive='Y' AND c.IsActive='Y'";

			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, role.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadClients - No Clients for Role=" + role.getKey());
				return null;
			}

			//  Role Info
			Env.setContext(ctx, "#AD_Role_ID", role.getKey());
			Env.setContext(ctx, "#AD_Role_Name", role.getName());
			Ini.setProperty(Ini.P_ROLE, role.getName());

			//	User Level
			Env.setContext(ctx, "#User_Level", rs.getString(1));  	//	Format 'SCO'
			//	ClientList
			Env.setContext(ctx, "#User_Client", rs.getString(2));	//	Format c1, c2, ...
			//	OrgList
			Env.setContext(ctx, "#User_Org", rs.getString(3)); 		//	Format o1, o2, ...
			//  Approval Currency / Amount
			Env.setContext(ctx, "#Approval_C_Currency_ID", rs.getInt(4));
			BigDecimal approval = rs.getBigDecimal(5);
			String approvalStr = "0";
			if (approval != null)
				approvalStr = approval.toString();
			Env.setContext(ctx, "#Approval_Amt", approvalStr);


			//  load Clients
			do
			{
				int AD_Client_ID = rs.getInt(6);
				String Name = rs.getString(7);
				KeyNamePair p = new KeyNamePair(AD_Client_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadClients", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# clients = " + retValue.length);
		return retValue;
	}   //  loadClients

	/**
	 *  Load Organizations.
	 *  <p>
	 *  Sets Client info in context and loads its organization, the role has access to
	 *
	 *  @param ctx context
	 *  @param  client    client information
	 *  @return list of valid Org KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadOrgs (Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadOrgs - required parameter missing");

		s_log.debug("loadOrgs - Client=" + client.toString());

		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadOrgs - Missing Comtext #AD_Role_ID");

		ArrayList list = new ArrayList();
		int AD_Role_ID = Env.getContextAsInt(ctx,"#AD_Role_ID");
		s_log.debug("AD_Role_ID=" + AD_Role_ID);
		//Begin e-evolution vpj-cd 05/09/2003 *************
		int AD_User_ID = Env.getContextAsInt(ctx,"#AD_User_ID");
		s_log.debug("AD_User_ID=" + AD_User_ID);
		//end e-evolution vpj-cd 05/09/2003 *************

		//	get Client details for role
		try
		{
			String sql = "SELECT c.Value,c.SMTPHost,c.IsMultiLingualDocument,c.AD_Language,"	//	1..4
				+ " o.AD_Org_ID,o.Name "				//	5..6
				+ "FROM AD_Client c"
				+ " INNER JOIN AD_Org o ON (o.AD_Client_ID=c.AD_Client_ID) "
				+ "WHERE o.AD_Client_ID=?"  			//  #1
				+ " AND o.IsSummary='N' AND o.IsActive='Y'"
				+ " AND o.AD_Org_ID IN "
				//Begin e-evolution vpj-cd 05/09/2003 *************
				//+   "(SELECT AD_Org_ID FROM AD_Role_OrgAccess WHERE AD_Role_ID=?)"; //  #2
				+ "(SELECT AD_Org_ID FROM AD_Role_OrgAccess WHERE AD_Role_ID=?)"  //  #2
				+ " AND (o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_User WHERE AD_User_ID=?) OR 0 IN (SELECT AD_Org_ID FROM AD_User WHERE AD_User_ID=?)) "; //  #3 , #4
				//end e-evolution vpj-cd 05/09/2003 *************

			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, client.getKey());
			pstmt.setInt(2, AD_Role_ID);
			//Begin e-evolution vpj-cd 05/09/2003 *************
			pstmt.setInt(3, AD_User_ID);
			pstmt.setInt(4, AD_User_ID);
			//end e-evolution vpj-cd 05/09/2003 *************
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadOrgs - No Org for Client=" + client.getKey());
				return null;
			}

			//  Client Info
			Env.setContext(ctx, "#AD_Client_ID", client.getKey());
			Env.setContext(ctx, "#AD_Client_Name", client.getName());
			Ini.setProperty(Ini.P_CLIENT, client.getName());
			//
			Env.setContext(ctx, "#Client_Value", rs.getString(1));
			Env.setContext(ctx, "#Client_SMTP", rs.getString(2));
			Env.setContext(ctx, "#IsMultiLingualDocument", rs.getString(3));

			//  load Orgs
			do
			{
				int AD_Org_ID = rs.getInt(5);
				String Name = rs.getString(6);
				KeyNamePair p = new KeyNamePair(AD_Org_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadOrgs", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# orgs = " + retValue.length);
		return retValue;
	}   //  loadOrgs

	/**
	 *	Load Preferences into Context for selected client.
	 *  <p>
	 *  Sets Org info in context and loads relevant field from
	 *	- AD_Client/Info,
	 *  - C_AcctSchema,
	 *  - C_AcctSchema_Elements
	 *	- AD_Preference
	 *  <p>
	 *  Assumes that the context is set for #AD_Client_ID, #AD_User_ID, #AD_Role_ID
	 *
	 *  @param ctx context
	 *  @param  org    org information
	 *  @param  warehouse   optional warehouse information
	 *  @param  timestamp   optional date
	 *  @param  printerName optional printer info
	 *  @returns AD_Message of error (NoValidAcctInfo) or ""
	 */
	public static String loadPreferences (Properties ctx,
		KeyNamePair org, KeyNamePair warehouse, Timestamp timestamp, String printerName)
	{
		s_log.info("loadPreferences - Org=" + org);

		if (ctx == null || org == null)
			throw new IllegalArgumentException("DB.loadPreferences - required parameter missing");
		if (Env.getContext(ctx,"#AD_Client_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Client_ID");
		if (Env.getContext(ctx,"#AD_User_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_User_ID");
		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Role_ID");


		//  Org Info - assumes that it is valid
		Env.setContext(ctx, "#AD_Org_ID", org.getKey());
		Env.setContext(ctx, "#AD_Org_Name", org.getName());
		Ini.setProperty(Ini.P_ORG, org.getName());

		//  Warehouse Info
		if (warehouse != null)
		{
			Env.setContext(ctx, "#M_Warehouse_ID", warehouse.getKey());
			Ini.setProperty(Ini.P_WAREHOUSE, warehouse.getName());
		}

		//	Date (default today)
		long today = System.currentTimeMillis();
		if (timestamp != null)
			today = timestamp.getTime();
		java.sql.Date sd = new java.sql.Date(today);
		Env.setContext(ctx, "#Date", sd.toString());	//	YYYY-MM-DD

		//	Optional Printer
		if (printerName == null)
			printerName = "";
		Env.setContext(ctx, "#Printer", printerName);
		Ini.setProperty(Ini.P_PRINTER, printerName);
		
		//	Load Role Info
		MRole.getDefault(ctx, true);	

		//	Other
		Env.setAutoCommit(ctx, Ini.getPropertyBool(Ini.P_A_COMMIT));
		if (MRole.getDefault(ctx, false).isShowAcct())
			Env.setContext(ctx, "#ShowAcct", Ini.getProperty(Ini.P_SHOW_ACCT));
		else
			Env.setContext(ctx, "#ShowAcct", "N");
		Env.setContext(ctx, "#ShowTrl", Ini.getProperty(Ini.P_SHOW_TRL));

		String retValue = "";
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		int AD_Org_ID =  org.getKey();
		int AD_User_ID =  Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID =  Env.getContextAsInt(ctx, "#AD_Role_ID");

		//	Other Settings
		Env.setContext(ctx, "#YYYY", "Y");
		Env.setContext(ctx, "#StdPrecision", 2);

		//	AccountSchema Info (first)
		String sql = "SELECT * "
			+ "FROM C_AcctSchema a, AD_ClientInfo c "
			+ "WHERE a.C_AcctSchema_ID=c.C_AcctSchema1_ID "
			+ "AND c.AD_Client_ID=?";
		try
		{
			int C_AcctSchema_ID = 0;
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				//  No Warning for System
				if (AD_Role_ID != 0)
					retValue = "NoValidAcctInfo";
			}
			else
			{
				//	Accounting Info
				C_AcctSchema_ID = rs.getInt("C_AcctSchema_ID");
				Env.setContext(ctx, "$C_AcctSchema_ID", C_AcctSchema_ID);
				Env.setContext(ctx, "$C_Currency_ID", rs.getInt("C_Currency_ID"));
				Env.setContext(ctx, "$HasAlias", rs.getString("HasAlias"));
			}
			rs.close();
			pstmt.close();

			//	Accounting Elements
			sql = "SELECT ElementType "
				+ "FROM C_AcctSchema_Element "
				+ "WHERE C_AcctSchema_ID=?"
				+ " AND IsActive='Y'";
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, C_AcctSchema_ID);
			rs = pstmt.executeQuery();
			while (rs.next())
				Env.setContext(ctx, "$Element_" + rs.getString("ElementType"), "Y");
			rs.close();
			pstmt.close();

			//	This reads all relevant window neutral defaults
			//	overwriting superseeded ones.  Window specific is read in Mainain
			sql = "SELECT Attribute, Value, AD_Window_ID "
				+ "FROM AD_Preference "
				+ "WHERE AD_Client_ID IN (0, @#AD_Client_ID@)"
				+ " AND AD_Org_ID IN (0, @#AD_Org_ID@)"
				+ " AND (AD_User_ID IS NULL OR AD_User_ID=0 OR AD_User_ID=@#AD_User_ID@)"
				+ " AND IsActive='Y' "
				+ "ORDER BY Attribute, AD_Client_ID, AD_User_ID DESC, AD_Org_ID";
				//	the last one overwrites - System - Client - User - Org - Window
			sql = Env.parseContext(ctx, 0, sql, false);
			if (sql.length() == 0)
				s_log.error("loadPreferences - Missing Environment");
			else
			{
				pstmt = prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					int AD_Window_ID = rs.getInt(3);
					String at = "";
					if (rs.wasNull())
						at = "P|" + rs.getString(1);
					else
						at = "P" + AD_Window_ID + "|" + rs.getString(1);
					String va = rs.getString(2);
					Env.setContext(ctx, at, va);
				}
				rs.close();
				pstmt.close();
			}
		}
		catch (SQLException ex)
		{
			s_log.error("loadPreferences (" + sql + ")", ex);
		}

		//	Default Values
		s_log.info("Default Values ...");
		sql = "SELECT t.TableName, c.ColumnName "
			+ "FROM AD_Column c "
			+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID) "
			+ "WHERE c.IsKey='Y' AND t.IsActive='Y'"
			+ " AND EXISTS (SELECT * FROM AD_Column cc "
			+ " WHERE ColumnName = 'IsDefault' AND t.AD_Table_ID=cc.AD_Table_ID AND cc.IsActive='Y')";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				loadDefault (ctx, rs.getString(1), rs.getString(2));
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("loadPreferences", e);
		}
		//
		Ini.saveProperties(Ini.isClient());
		//
		return retValue;
	}	//	loadPreferences

	/**
	 *	Load Default Value for Table into Context.
	 *
	 *  @param ctx context
	 *  @param TableName table name
	 *  @param ColumnName column name
	 */
	private static void loadDefault (Properties ctx, String TableName, String ColumnName)
	{
		String value = null;
		//
		String sql = "SELECT " + ColumnName + " FROM " + TableName
			+ " WHERE IsDefault='Y' AND IsActive='Y' ORDER BY AD_Client_ID";
		sql = MRole.getDefault(ctx, false).addAccessSQL(sql, 
			TableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())		//	overwrites system defaults
				value = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("loadDefault - " + TableName + " (" + sql + ")", e);
			return;
		}
		//	Set Context Value
		if (value != null && value.length() != 0)
		{
			if (TableName.equals("C_DocType"))
				Env.setContext(ctx, "#C_DocTypeTarget_ID", value);
			else
				Env.setContext(ctx, "#" + ColumnName, value);
		}
	}	//	loadDefault

	/**
	 *  Load Warehouses
	 *
	 * @param ctx context
	 * @param client client
	 * @return Array of Warehouse Info
	 */
	public static KeyNamePair[] loadWarehouses(Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadWarehouses - required parameter missing");

		s_log.info("loadWarehouses - Client=" + client.toString());
		//Begin e-evolution vpj-cd 05/09/2003 *************
		int AD_User_ID = Env.getContextAsInt(ctx,"#AD_User_ID");
		s_log.debug("AD_User_ID=" + AD_User_ID);
		//end e-evolution vpj-cd 05/09/2003 *************

		ArrayList list = new ArrayList();
		try
		{
			String sql = "SELECT M_Warehouse_ID, Name FROM M_Warehouse "
				//Begin e-evolution vpj-cd 05/09/2003 *************
				//	+ "WHERE AD_Client_ID=? AND IsActive='Y'";  //  #1
				+ "WHERE AD_Client_ID=? AND IsActive='Y' "
				+ " AND (AD_Org_ID IN (SELECT AD_Org_ID FROM AD_User WHERE AD_User_ID=?) OR 0 IN (SELECT AD_Org_ID FROM AD_User WHERE AD_User_ID=?))"; //#2,#3
				//end e-evolution vpj-cd 05/09/2003 *************
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, client.getKey());
			//Begin e-evolution vpj-cd 05/09/2003 *************
			pstmt.setInt(2, AD_User_ID);
			pstmt.setInt(3, AD_User_ID);
			//end e-evolution vpj-cd 05/09/2003 *************
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.warn("loadWarehouses - No Warehouses for Client=" + client.getKey());
				return null;
			}

			//  load Warehousess
			do
			{
				int AD_Warehouse_ID = rs.getInt(1);
				String Name = rs.getString(2);
				KeyNamePair p = new KeyNamePair(AD_Warehouse_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadWarehouses", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.info("# warehouses = " + retValue.length);
		return retValue;
	}   //  loadWarehouses

	/*************************************************************************/

	/**
	 *  Set connection
	 *  @param cc connection
	 */
	public static void setDBTarget (CConnection cc)
	{
		if (cc == null)
			throw new IllegalArgumentException("DB.setDBTarget connection is NULL");

		if (s_cc == null)
			s_cc = cc;
		synchronized (s_cc)    //  use as mutex
		{
			s_cc = cc;
			//  Closing existing
			if (s_connections != null)
			{
				for (int i = 0; i < s_connections.length; i++)
				{
					try {
						s_connections[i].close();
					} catch (Exception e) {}
				}
			}
			s_connections = null;
		}
	}   //  setDBTarget

	/**
	 *  Is there a connection to the database ?
	 *  @return true, if connected to database
	 */
	public static boolean isConnected()
	{
		try
		{
			getConnectionRW();	//	try to get a connection
			return true;
		}
		catch (Exception e)
		{
		}
		return false;
	}   //  isConnected

	/**
	 *	Return (pooled) r/w AutoCommit, read committed connection
	 *
	 *  @return Connection (r/w)
	 */
	public static Connection getConnectionRW ()
	{
		//	check health of connection
		try
		{
			if (s_connectionRW == null)
				;
			else if (s_connectionRW.isClosed())
				s_connectionRW = null;
			else if (s_connectionRW instanceof OracleConnection && ((OracleConnection)s_connectionRW).pingDatabase(1) < 0)
				s_connectionRW = null;
			else
				 s_connectionRW.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		catch (Exception e)
		{
			s_connectionRW = null;
		}
		if (s_connectionRW == null)
			s_connectionRW = s_cc.getConnection (true, Connection.TRANSACTION_READ_COMMITTED);
		if (s_connectionRW == null)
			throw new UnsupportedOperationException("DB.getConnectionRW - @NoDBConnection@");
		return s_connectionRW;
	}   //  getConnectionRW

	/**
	 *	Return read committed, read/only connection with AutoCommit from pool
	 *  @return Connection (r/o)
	 */
	public static Connection getConnectionRO ()
	{
		try
		{
			synchronized (s_cc)    //  use as mutex as s_connection is null the first time
			{
				if (s_connections == null)
					s_connections = createConnections (Connection.TRANSACTION_READ_COMMITTED);     //  see below
			}
		}
		catch (Exception e)
		{
			s_log.error("getConnectionRO", e);
		}

		//  check health of connection
		int pos = s_conCount++;
		Connection connection = s_connections[pos % s_conCacheSize];
		try
		{
			if (connection == null)
				;
			else if (connection.isClosed())
				connection = null;
			else if (connection instanceof OracleConnection && ((OracleConnection)connection).pingDatabase(1) < 0)
				connection = null;
			else
				 connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		catch (Exception e)
		{
			connection = null;
		}
		//	Get new
		if (connection == null)
		{
			System.out.println("DB.getConnectionRO - replacing connection #" + pos % s_conCacheSize);
		//	s_log.error("getConnectionRO - replacing connection #" + pos % s_conCacheSize);
			connection = s_cc.getConnection (true, Connection.TRANSACTION_READ_COMMITTED);            //  see above
		/*	try
			{
				retValue.setReadOnly(true);     //  not supported by Oracle
			}
			catch (Exception e)
			{
				System.err.println("DB.getConnectionRO - Cannot set to R/O - " + e);
			}   */
			s_connections[pos % s_conCacheSize] = connection;
		}
		if (connection == null)
			throw new UnsupportedOperationException("DB.getConnectionRO - @NoDBConnection@");
		return connection;
	}	//	getConnectionRO

	/**
	 *	Create new Connection.
	 *  The connection must be closed explicitly by the application
	 *
	 *  @param autoCommit auto commit
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Connection connection
	 */
	public static Connection createConnection (boolean autoCommit, int trxLevel)
	{
		s_log.debug("createConnection " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", AutoCommit=" + autoCommit + ", TrxLevel=" + trxLevel);
		return s_cc.getConnection (autoCommit, trxLevel);
	}	//	createConnection

	/**
	 *	Create new set of r/o Connections.
	 *  R/O connection might not be supported by DB
	 *
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Array of Connections (size based on s_conCacheSize)
	 */
	private static Connection[] createConnections (int trxLevel)
	{
		s_log.debug("createConnections (" + s_conCacheSize + ") " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", TrxLevel=" + trxLevel);
		Connection cons[] = new Connection[s_conCacheSize];
		try
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				cons[i] = s_cc.getConnection (true, trxLevel);  //  auto commit
				if (cons[i] == null)
					System.err.println("createConnections - connection is NULL");	//	don't use log
			}
		}
		catch (Exception e)
		{
			//  Don't use Log
			System.err.println("DB.createConnections - " + e.getMessage());
		}
		return cons;
	}	//	createConnections

	/**
	 *  Get Database Driver.
	 *  Access to database specific functionality.
	 *  @return Compiere Database Driver
	 */
	public static CompiereDatabase getDatabase()
	{
		if (s_cc != null)
			return s_cc.getDatabase();
		return null;
	}   //  getDatabase

	/*************************************************************************/

	/**
	 *  Check database Version with Code version
	 *  @param ctx context
	 *  @return true if Database version (date) is the same
	 */
	public static boolean isDatabaseOK (Properties ctx)
	{
		//  Check Version
		String version = "?";
		String sql = "SELECT Version FROM AD_System";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				version = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("Problem with AD_System Table - Run system.sql script - " + e.toString());
			return false;
		}
		s_log.info("isDatabaseOK - DB_Version=" + version);
		//  Identical DB version
		if (Compiere.DB_VERSION.equals(version))
			return true;

		String AD_Message = "DatabaseVersionError";
		String title = org.compiere.Compiere.getName() + " " +  Msg.getMsg(ctx, AD_Message, true);
		//	Code assumes Database version {0}, but Database has Version {1}.
		String msg = Msg.getMsg(ctx, AD_Message);	//	complete message
		msg = MessageFormat.format(msg, new Object[] {Compiere.DB_VERSION, version});
		Object[] options = { UIManager.get("OptionPane.noButtonText"), "Migrate" };
		int no = JOptionPane.showOptionDialog (null, msg,
			title, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
			UIManager.getIcon("OptionPane.errorIcon"), options, options[0]);
		if (no == 1)
		{
			try
			{
				Class.forName("com.compiere.client.StartMaintain").newInstance();
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog (null,
					ex.getMessage() + "\nSee: http://www.compiere.com/maintain",
					title, JOptionPane.ERROR_MESSAGE);
				s_log.error("isDatabaseOK - " + ex.getMessage());
			}
		}
		return false;
	}   //  isDatabaseOK

	/*************************************************************************/

	/**
	 *	Secure Context Login.
	 *  @param uid uid
	 *  @param pwd pwd
	 *  @param role role
	 *  @return true if connected
	 */
	public static boolean login_context (String uid, String pwd, String role)
	{
		if (uid == null || pwd == null || role == null || uid.length() == 0 || pwd.length() == 0 || role.length() == 0)
			throw new IllegalArgumentException("DB.login_context - required parameter missing");
		s_log.info("login_context uid=" + uid);
		if (uid == null || pwd == null || role == null)
			return false;
		//
		String SQL = "{CALL Compiere_Context.Login(?,?,?,?)}";
		boolean result = true;
		try
		{
			CallableStatement cstmt = getConnectionRO().prepareCall(SQL);
			cstmt.setString(1, uid);
			cstmt.setString(2, pwd);
			cstmt.setString(3, role);
			cstmt.setString(4, Language.getBaseAD_Language());
			result = cstmt.execute();
			cstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("login_context", e);
			result = false;
		}
		return result;
	}	//	login_context

	/*************************************************************************/

	/**
	 *	Close Target
	 */
	public static void closeTarget()
	{
		if (s_connections != null)
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				try
				{
					if (s_connections[i] != null)
						s_connections[i].close();
				}
				catch (SQLException e)
				{
					s_log.warn("close connection #" + i + " - " + e.getMessage());
				}
				s_connections[i] = null;
			}
			try
			{
				if (s_connectionRW != null)
					s_connectionRW.close();
			}
			catch (SQLException e)
			{
				s_log.error("close R/W connection", e);
			}
			s_connectionRW = null;
		}
		s_connections = null;
//		EJB.close();
	}	//	closeTarget

	/*************************************************************************/

	/**
	 *	Prepare Forward Read Only Call
	 *  @param RO_SQL sql (RO)
	 *  @return Callable Statement
	 */
	public static CallableStatement prepareCall(String RO_SQL)
	{
		if (RO_SQL == null || RO_SQL.length() == 0)
			throw new IllegalArgumentException("DB.prepareCall - required parameter missing - " + RO_SQL);
		//
		String sql = getDatabase().convertStatement(RO_SQL);
		try
		{
			return getConnectionRO().prepareCall
				(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		}
		catch (SQLException e)
		{
			s_log.error("prepareCall (" + sql + ")", e);
		}
		return null;
	}	//	prepareCall

	/*************************************************************************/

	/**
	 *	Prepare Read Only Statement
	 *  @param RO_SQL sql (RO)
	 *  @return Prepared Statement
	 */
	public static PreparedStatement prepareStatement(String RO_SQL)
	{
		return prepareStatement(RO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}	//	prepareStatement

	/**
	 *	Prepare Statement
	 *
	 *  @param SQL sql statement
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Prepared Statement r/o or r/w depending on concur
	 */
	public static PreparedStatement prepareStatement(String SQL, int resultSetType, int resultSetConcurrency)
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.prepareStatement - required parameter missing - " + SQL);
		//
		return new CompiereStatement(SQL, resultSetType, resultSetConcurrency);
		/**
		try
		{
			Connection conn = null;
			if (resultSetConcurrency == ResultSet.CONCUR_UPDATABLE)
				conn = getConnectionRW();
			else
				conn = getConnectionRO();
			return conn.prepareStatement (sql, resultSetType, resultSetConcurrency);
		}
		catch (SQLException e)
		{
			s_log.error("prepareStatement (" + sql + ")", e);
		}
		return null;
		**/
	}	//	prepareStatement

	/**
	 *	Create Read Only Statement
	 *  @return Statement
	 */
	public static Statement createStatement()
	{
		return createStatement (ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}	//	createStatement

	/**
	 *	Create Statement
	 *
	 *  @param type - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param concur - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Statement - either r/w ir r/o depending on concur
	 */
	public static Statement createStatement(int type, int concur)
	{
		try
		{
			Connection conn = null;
			if (concur == ResultSet.CONCUR_UPDATABLE)
				conn = getConnectionRW();
			else
				conn = getConnectionRO();
			return conn.createStatement(type, concur);
		}
		catch (SQLException e)
		{
			s_log.error("createStatement", e);
		}
		return null;
	}	//	createStatement

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param SQL sql
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String SQL)
	{
		return executeUpdate(SQL, false);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param SQL sql
	 * 	@param ignoreError if true, no execution error is reported
	 *  @return number of rows updated or -1 if error
	 */
	public static int executeUpdate (String SQL, boolean ignoreError)
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.executeUpdate - required parameter missing - " + SQL);
		//
		String sql = getDatabase().convertStatement(SQL);
		int no = -1;
		Statement stmt = null;
		try
		{
			Connection conn = getConnectionRW();
			stmt = conn.createStatement();
			no = stmt.executeUpdate(sql);
		}
		catch (SQLException e)
		{
			if (!ignoreError)
			{
				s_log.error ("executeUpdate - " + sql, e);
				Log.saveError ("DBExecuteError", e.getLocalizedMessage ());
			}
		}
		finally
		{
			//  Always close cursor
			try
			{
				stmt.close();
			}
			catch (SQLException e2)
			{
				s_log.error("executeUpdate - cannot close statement");
			}
		}
		return no;
	}	//	executeUpdate

	/**
	 *	Execute Update and throw exxeption.
	 *  @param SQL sql
	 *  @return number of rows updated or -1 if error
	 * 	@throws SQLException
	 */
	public static int executeUpdateEx (String SQL) throws SQLException
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.executeUpdate - required parameter missing - " + SQL);
		//
		String sql = getDatabase().convertStatement(SQL);
		int no = -1;
		SQLException ex = null;
		Statement stmt = null;
		try
		{
			Connection conn = getConnectionRW();
			stmt = conn.createStatement();
			no = stmt.executeUpdate(sql);
		}
		catch (SQLException e)
		{
			ex = e;
		}
		finally
		{
			//  Always close cursor
			try
			{
				stmt.close();
			}
			catch (SQLException e2)
			{
				s_log.error("executeUpdate - cannot close statement");
			}
		}
		if (ex != null)
			throw new SQLException(ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
		return no;
	}	//	execute Update

	/**
	 *	Commit - commit on RW connection.
	 *  Is not required as RW connection is AutoCommit
	 *  @param throwException if true, re-throws exception
	 *  @return true if not needed or success
	 *  @throws SQLException
	 */
	public static boolean commit (boolean throwException) throws SQLException
	{
		try
		{
			Connection conn = getConnectionRW();
		//	if (!conn.getAutoCommit())
				conn.commit();
		}
		catch (SQLException e)
		{
			s_log.error("commit", e);
			if (throwException)
				throw e;
			return false;
		}
		return true;
	}	//	commit


	/**
	 * 	Get Row Set
	 *	@param sql sql
	 *	@return row set or null
	 */
	public static RowSet getRowSet (String sql)
	{
		RowSet retValue = null;
		CompiereStatementVO info = new CompiereStatementVO(sql, RowSet.TYPE_SCROLL_INSENSITIVE, RowSet.CONCUR_READ_ONLY);
		CompiereStatement stmt = new CompiereStatement(info);
		retValue = stmt.remote_getRowSet();
		return retValue;
	}	//	getRowSet

	/*************************************************************************

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @return next no
	 */
	public static int getKeyNextNo (Properties ctx, int WindowNo, String TableName)
	{
		if (ctx == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getKeyNextNo - required parameter missing");
		int AD_Client_ID = Env.getAD_Client_ID(ctx);
		//
		return getKeyNextNo (AD_Client_ID, TableName);
	}   //  getKeyNextNo

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param AD_Client_ID client
	 *  @param TableName table name
	 *  @return next no
	 */
	public static int getKeyNextNo (int AD_Client_ID, String TableName)
	{
		if (TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getKeyNextNo - required parameter missing");
		int retValue = 0;

		//	Check CompiereSys
		boolean compiereSys = Ini.getPropertyBool(Ini.P_COMPIERESYS);
		if (!compiereSys && AD_Client_ID < 1000000)
			AD_Client_ID = 1000000;
		//
		try
		{
			String SQL = "{CALL AD_Sequence_Next(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setString(1, TableName);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.NUMERIC);
			cstmt.executeUpdate();
			retValue = cstmt.getInt(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getKeyNextNo - Table=" + TableName + ")", e);
		}
		return retValue;
	}	//	getKeyNextNo

	/**
	 *	Get Document Number for current document.
	 *  <br>
	 *  - first search for DocType based Document No
	 *  - then Search for DocumentNo based on TableName
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @param onlyDocType Do not search for document no based on TableName
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (Properties ctx, int WindowNo, String TableName, boolean onlyDocType)
	{
		if (ctx == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getDocumentNo - required parameter missing");
		String CompiereSys = Env.getContext(ctx, "#CompiereSys");
		int AD_Client_ID = Env.getContextAsInt(ctx, WindowNo, "AD_Client_ID");

		//	Get C_DocType_ID from context - NO Defaults -
		int C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID");
		if (C_DocType_ID == 0)
			C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID");
		if (C_DocType_ID == 0)
		{
			s_log.debug("getDocumentNo - for Window=" + WindowNo
				+ " - Target=" + Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID") + "/" + Env.getContextAsInt(ctx, WindowNo, "C_DocTypeTarget_ID")
				+ " - Actual=" + Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID") + "/" + Env.getContextAsInt(ctx, WindowNo, "C_DocType_ID"));
			return getDocumentNo (AD_Client_ID, CompiereSys, TableName);
		}

		//	Check CompiereSys
		if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
			throw new UnsupportedOperationException("DB.getDocumentNo - Cannot add System records");
		//
		String retValue = getDocumentNo(AD_Client_ID, C_DocType_ID);
		if (!onlyDocType && retValue == null)
			return getDocumentNo (AD_Client_ID, CompiereSys, TableName);
		return retValue;
	}	//	getDocumentNo

	/**
	 *	Get Document Number for current document.
	 *  @param AD_Client_ID client
	 *  @param C_DocType_ID (target) document type
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (int AD_Client_ID, int C_DocType_ID)
	{
		String retValue = null;
		try
		{
			String SQL = "{CALL AD_Sequence_DocType(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setInt(1, C_DocType_ID);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.VARCHAR);
			cstmt.executeUpdate();
			retValue = cstmt.getString(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getDocumentNo - DocType=" + C_DocType_ID, e);
		}
		s_log.info("getDocumentNo - DocType=" + C_DocType_ID + " -> " + retValue);
		return retValue;
	}	//	getDocumentNo


	/**
	 *  Get Next Document No
	 *  @param AD_Client_ID client
	 *  @param CompiereSys system
	 *  @param TableName table name
	 *  @return DocumentNo
	 */
	public static String getDocumentNo (int AD_Client_ID, String CompiereSys, String TableName)
	{
		if (CompiereSys == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getDocumentNo - required parameter missing");
		//	Check CompiereSys
	//	if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
	//		throw new UnsupportedOperationException("DB.getDocumentNo - Cannot add System records");
		//
		String retValue = null;
		try
		{
			String SQL = "{CALL AD_Sequence_Doc(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setString(1, "DocumentNo_" + TableName);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.VARCHAR);
			cstmt.executeUpdate();
			retValue = cstmt.getString(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getDocumentNo  TableName=" + TableName, e);
		}
		s_log.info("getDocumentNo - TableName=" + TableName + " -> " + retValue);
		return retValue;
	}   //  getDocumentNo


	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int int_param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, String int_param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setString(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@param s_param2 parameter 2
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int int_param1, String s_param2)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			pstmt.setString(2, s_param2);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value: " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Array of Key Name Pairs
	 *	@param sql select with id and name as first and second column
	 *	@return array of key name pairs
	 */
	public static KeyNamePair[] getKeyNamePairs(String sql)
	{
		PreparedStatement pstmt = null;
		ArrayList list = new ArrayList();
		try
		{
			pstmt = DB.prepareCall(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new KeyNamePair(rs.getInt(1), rs.getString(2)));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			Log.error("getKeyNamePairs " + sql, e);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
	//	s_log.debug("getKeyNamePairs #" + retValue.length);
		return retValue;		
	}	//	getKeyNamePairs

	/**
	 *  Convert an amount with today's spot rate
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  Amt         The amount to be converted
	 * 	@param	AD_Client_ID client
	 * 	@param	AD_Org_ID	organization
	 *  @return converted amount
	 */
	public static BigDecimal getConvertedAmt (BigDecimal Amt, int CurFrom_ID, int CurTo_ID, int AD_Client_ID, int AD_Org_ID)
	{
		return getConvertedAmt (Amt, CurFrom_ID, CurTo_ID, null, null, AD_Client_ID, AD_Org_ID);
	}   //  getConvertedAmt

	/**
	 *	Convert an amount
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  ConvDate    The Conversion date - if null - use current date
	 *  @param  RateType    The Conversion rate type - if null/empty - use Spot
	 *  @param  Amt         The amount to be converted
	 * 	@param	AD_Client_ID client
	 * 	@param	AD_Org_ID	organization
	 *  @return converted amount
	 */
	public static BigDecimal getConvertedAmt (BigDecimal Amt, int CurFrom_ID, int CurTo_ID,
		Timestamp ConvDate, String RateType, int AD_Client_ID, int AD_Org_ID)
	{
		if (Amt == null)
			throw new IllegalArgumentException("DB.getConvertedAmt - required parameter missing - Amt");
		BigDecimal retValue = null;
		if (CurFrom_ID == CurTo_ID || Amt.equals(Env.ZERO))
			return Amt;
		//
		try
		{
			String sql = "{? = call C_Currency_Convert(?,?,?,?,?, ?,?) }";
			CallableStatement cstmt = prepareCall(sql);
			//
			cstmt.registerOutParameter(1, Types.NUMERIC);
			//
			cstmt.setBigDecimal(2, Amt);					//	Amount		IN  	NUMBER
			cstmt.setInt(3, CurFrom_ID);             		//	CurFrom		IN 		NUMBER
			cstmt.setInt(4, CurTo_ID);                  	//	CurTo		IN 		NUMBER
			if (ConvDate == null)
				ConvDate = new Timestamp (System.currentTimeMillis());
			cstmt.setTimestamp(5, ConvDate);				//	ConvDate	IN 		DATE
			if (RateType == null || RateType.equals(""))
				RateType = "S";
			cstmt.setString(6, RateType);					//	RateType	IN 		CHAR
			cstmt.setInt(7, AD_Client_ID);
			cstmt.setInt(8, AD_Org_ID);
			//
			cstmt.executeUpdate();
			retValue = cstmt.getBigDecimal(1);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getConvertedAmt", e);
		}
		if (retValue == null)
			s_log.info("getConvertedAmt - not found - CurFrom=" + CurFrom_ID + ", CurTo=" + CurTo_ID
				+ ", " + ConvDate + ", " + RateType + ", Client=" + AD_Client_ID + ", Org=" + AD_Org_ID);
		return retValue;
	}	//	getConvertedAmt

	/**
	 *	Get Currency Rate
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  ConvDate    The Conversion date - if null - use current date
	 *  @param  RateType    The Conversion rate type - if null/empty - use Spot
	 * 	@param	AD_Client_ID client
	 * 	@param	AD_Org_ID	organization
	 *  @return currency Rate or null
	 */
	public static BigDecimal getConvesionRate (int CurFrom_ID, int CurTo_ID,
		Timestamp ConvDate, String RateType, int AD_Client_ID, int AD_Org_ID)
	{
		BigDecimal retValue = null;
		if (CurFrom_ID == CurTo_ID)
			return new BigDecimal(1);
		//
		try
		{
			String sql = "{? = call C_Currency_Rate(?,?,?,?,?,?)}";
			CallableStatement cstmt = prepareCall(sql);
			//
			cstmt.registerOutParameter(1, Types.NUMERIC);
			//
			cstmt.setInt(2, CurFrom_ID);             		//	CurFrom		IN 		NUMBER
			cstmt.setInt(3, CurTo_ID);                  	//	CurTo		IN 		NUMBER
			if (ConvDate == null)
				ConvDate = new Timestamp (System.currentTimeMillis());
			cstmt.setTimestamp(4, ConvDate);				//	ConvDate	IN 		DATE
			if (RateType == null || RateType.equals(""))
				RateType = "S";
			cstmt.setString(5, RateType);					//	RateType	IN 		CHAR
			cstmt.setInt(6, AD_Client_ID);
			cstmt.setInt(7, AD_Org_ID);
			//
			cstmt.executeUpdate();
			retValue = cstmt.getBigDecimal(1);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getConvesionRate", e);
		}
		if (retValue == null)
			s_log.info ("getConversionRate - not found - CurFrom=" + CurFrom_ID + ", CurTo=" + CurTo_ID
			  + ", " + ConvDate + ", " + RateType + ", Client=" + AD_Client_ID + ", Org=" + AD_Org_ID);
		return retValue;
	}	//	getConvesionRate


	/**
	 *  Get fully qualified Account Combination
	 *
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param C_AcctSchema_ID acct schema
	 *  @param Account_ID natural account
	 *  @param  base_ValidCombination_ID optional base combination to be specified
	 *  @param Alias aloas
	 *  @param AD_User_ID user
	 *  @param M_Product_ID product
	 *  @param C_BPartner_ID partner
	 *  @param AD_OrgTrx_ID trx org
	 *  @param C_LocFrom_ID loc from
	 *  @param C_LocTo_ID loc to
	 *  @param C_SRegion_ID sales region
	 *  @param C_Project_ID project
	 *  @param C_Campaign_ID campaign
	 *  @param C_Activity_ID activity
	 *  @param User1_ID user1
	 *  @param User2_ID user2
	 *  @return C_ValidCombination_ID of existing or new Combination
	 */
	public static int getValidCombination (int AD_Client_ID, int AD_Org_ID,
		int C_AcctSchema_ID, int Account_ID, int base_ValidCombination_ID, String Alias, int AD_User_ID,
		int M_Product_ID, int C_BPartner_ID, int AD_OrgTrx_ID,
		int C_LocFrom_ID, int C_LocTo_ID, int C_SRegion_ID, int C_Project_ID,
		int C_Campaign_ID, int C_Activity_ID, int User1_ID, int User2_ID)
	{
		int retValue = 0;
		StringBuffer sb = new StringBuffer ("C_ValidCombination_Get[");
		try
		{
			String sql = "{CALL C_ValidCombination_Get(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)}";
			CallableStatement cstmt = prepareCall(sql);
			//  1 - ID
			cstmt.registerOutParameter(1, Types.NUMERIC);
			sb.append("v,");
			//  --  Mandatory Accounting fields
			//	2 - AD_Client_ID
			cstmt.setInt(2, AD_Client_ID);
			sb.append("AD_Client_ID=").append(AD_Client_ID).append(",");
			//	3 - AD_Org_ID
			cstmt.setInt(3, AD_Org_ID);
			sb.append("AD_Org_ID=").append(AD_Org_ID).append(",");
			//  4- C_AcctSchema_ID
			cstmt.setInt(4, C_AcctSchema_ID);
			sb.append("C_AcctSchema_ID=").append(C_AcctSchema_ID).append(",");
			//  5 - Account_ID
			cstmt.setInt(5, Account_ID);
			sb.append("Account_ID=").append(Account_ID).append(", ");

			//  --  Optional
			//  6 - Base C_ValidCombination_ID
			if (base_ValidCombination_ID != 0)
			{
				cstmt.setInt (6, base_ValidCombination_ID);
				sb.append("BaseValidCombination_ID=").append(base_ValidCombination_ID).append(",");
			}
			else
			{
				cstmt.setNull(6, Types.NUMERIC);
				sb.append("BaseValidCombination_ID=").append("NULL,");
			}
			//  7 - MustBeFullyQualified
			cstmt.setString(7, "Y");
			sb.append("MustBeFullyQualified='Y',");
			//  8 - Alias
			if (Alias != null && Alias.length() > 0)
			{
				cstmt.setString(8, Alias);
				sb.append("Alias='").append(Alias).append("',");
			}
			else
			{
				cstmt.setNull(8, Types.VARCHAR);
				sb.append("Alias=NULL,");
			}
			//  9 - CreatedBy
			cstmt.setInt(9, AD_User_ID);
			sb.append("AD_User_ID=").append(AD_User_ID).append(", ");

			//	--  Optional Accounting fields
			//  10 - M_Product_ID
			if (M_Product_ID != 0)
			{
				cstmt.setInt(10, M_Product_ID);
				sb.append("M_Product_ID=").append(M_Product_ID).append(",");
			}
			else
			{
				cstmt.setNull(10, Types.NUMERIC);
				sb.append("M_Product_ID=NULL,");
			}
			//  11 - C_BPartner_ID
			if (C_BPartner_ID != 0)
			{
				cstmt.setInt(11, C_BPartner_ID);
				sb.append("C_BPartner_ID=").append(C_BPartner_ID).append(",");
			}
			else
			{
				cstmt.setNull(11, Types.NUMERIC);
				sb.append("C_BPartner_ID=NULL,");
			}
			//  12 - AD_OrgTrx_ID
			if (AD_OrgTrx_ID != 0)
			{
				cstmt.setInt(12, AD_OrgTrx_ID);
				sb.append("AD_OrgTrx_ID=").append(AD_OrgTrx_ID).append(",");
			}
			else
			{
				cstmt.setNull(12, Types.NUMERIC);
				sb.append("AD_OrgTrx_ID=NULL,");
			}
			//  13 - C_LocFrom_ID
			if (C_LocFrom_ID != 0)
			{
				cstmt.setInt(13, C_LocFrom_ID);
				sb.append("C_LocFrom_ID=").append(C_LocFrom_ID).append(",");
			}
			else
			{
				cstmt.setNull(13, Types.NUMERIC);
				sb.append("C_LocFrom=NULL,");
			}
			//  14 - C_LocTo_ID
			if (C_LocTo_ID != 0)
			{
				cstmt.setInt(14, (C_LocTo_ID));
				sb.append("C_LocTo_ID=").append(C_LocTo_ID).append(", ");
			}
			else
			{
				cstmt.setNull(14, Types.NUMERIC);
				sb.append("C_LocTo_ID=NULL, ");
			}
			//  15 - C_SalesRegion_ID
			if (C_SRegion_ID != 0)
			{
				cstmt.setInt(15, (C_SRegion_ID));
				sb.append("C_SalesRegion_ID=").append(C_SRegion_ID).append(",");
			}
			else
			{
				cstmt.setNull(15, Types.NUMERIC);
				sb.append("C_SalesRegion_ID=NULL,");
			}
			//  16 - C_Project_ID
			if (C_Project_ID != 0)
			{
				cstmt.setInt(16, (C_Project_ID));
				sb.append("C_Project_ID=").append(C_Project_ID).append(",");
			}
			else
			{
				cstmt.setNull(16, Types.NUMERIC);
				sb.append("C_Project_ID=NULL,");
			}
			//  17 - C_Campaign_ID
			if (C_Campaign_ID != 0)
			{
				cstmt.setInt(17, (C_Campaign_ID));
				sb.append("C_Campaign_ID=").append(C_Campaign_ID).append(",");
			}
			else
			{
				cstmt.setNull(17, Types.NUMERIC);
				sb.append("C_Campaign_ID=NULL,");
			}
			//  18 - C_Activity_ID
			if (C_Activity_ID != 0)
			{
				cstmt.setInt(18, (C_Activity_ID));
				sb.append("C_Activity_ID=").append(C_Activity_ID).append(",");
			}
			else
			{
				cstmt.setNull(18, Types.NUMERIC);
				sb.append("C_Activity_ID=NULL,");
			}
			//  19 - User1_ID
			if (User1_ID != 0)
			{
				cstmt.setInt(19, (User1_ID));
				sb.append("User1_ID=").append(User1_ID).append(",");
			}
			else
			{
				cstmt.setNull(19, Types.NUMERIC);
				sb.append("User1_ID=NULL,");
			}
			//  20 - User2_ID
			if (User2_ID != 0)
			{
				cstmt.setInt(20, (User2_ID));
				sb.append("User2_ID=").append(User2_ID).append(")");
			}
			else
			{
				cstmt.setNull(20, Types.NUMERIC);
				sb.append("User2_ID=NULL)");
			}

			//
			cstmt.executeUpdate();
			retValue = cstmt.getInt(1);     //  1 - ID
			cstmt.close();
			s_log.debug("getValidCombination " + sb.toString());
		}
		catch(SQLException e)
		{
			s_log.error("getValidCombination " + sb.toString(), e);
		}
		return retValue;
	}   //  getValidCombination

	/**
	 *  Insert Note
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param AD_User_ID user
	 *  @param AD_Table_ID table
	 *  @param Record_ID record
	 *  @param AD_MessageValue message
	 *  @param Text text
	 *  @param Reference subject
	 *  @return true if note was inserted
	 */
	public static boolean insertNote (int AD_Client_ID, int AD_Org_ID, int AD_User_ID,
		int AD_Table_ID, int Record_ID,
		String AD_MessageValue, String Text, String Reference)
	{
		if (AD_MessageValue == null || AD_MessageValue.length() == 0)
			throw new IllegalArgumentException("DB.insertNote - required parameter missing - AD_Message");

		//  Database limits
		if (Text == null)
			Text = "";
		if (Reference == null)
			Reference = "";
		//
		s_log.info("insertNote - " + AD_MessageValue + " - " + Reference);
		//
		StringBuffer sql = new StringBuffer ("INSERT INTO AD_Note (AD_Note_ID,");
		sql.append("AD_Client_ID,AD_Org_ID,IsActive, Created,CreatedBy,Updated,UpdatedBy, ")
			.append("AD_Message_ID,Text,Reference, ")
			.append("AD_User_ID,AD_Table_ID,Record_ID,Processed) ")
			.append("SELECT ");
		//
		String CompiereSys = "N";
		int AD_Note_ID = getKeyNextNo(AD_Client_ID, "AD_Note");
		sql.append(AD_Note_ID).append(",");
		//
		sql.append(AD_Client_ID).append(",")
			.append(AD_Org_ID).append(", 'Y',SysDate,")
			.append(AD_User_ID).append(",SysDate,0,");
		//	AD_Message_ID,Text,Reference,
		sql.append(" AD_Message_ID,").append(DB.TO_STRING(Text, 2000)).append(",")
			.append(DB.TO_STRING(Reference, 60)).append(", ");
		//	AD_User_ID,AD_Table_ID,Record_ID,Processed
		sql.append(AD_User_ID).append(",").append(AD_Table_ID).append(",")
			.append(Record_ID).append(",'N' ");
		//
		sql.append("FROM AD_Message WHERE Value='").append(AD_MessageValue).append("'");
		//  Create Entry
		int no = executeUpdate(sql.toString());

		//  AD_Message must exist, so if not created, it is probably
		//  due to non-existing AD_Message
		if (no == 0)
		{
			sql = new StringBuffer ("INSERT INTO AD_Note (AD_Note_ID,");
			sql.append("AD_Client_ID,AD_Org_ID,IsActive, Created,CreatedBy,Updated,UpdatedBy, ")
				.append("AD_Message_ID,Text,Reference, ")
				.append("AD_User_ID,AD_Table_ID,Record_ID,Processed) ")
				.append("SELECT ");
			//  - use AD_Note_ID from above
			sql.append(AD_Note_ID).append(",");
			//
			sql.append(AD_Client_ID).append(",")
				.append(AD_Org_ID).append(", 'Y',SysDate,")
				.append(AD_User_ID).append(",SysDate,0, ");
			//	AD_Message_ID,Text,Reference,
			sql.append("AD_Message_ID,").append(TO_STRING (AD_MessageValue + ": " + Text, 2000)).append(",")
				.append(TO_STRING(Reference,60)).append(", ");
			//	AD_User_ID,AD_Table_ID,Record_ID,Processed
			sql.append(AD_User_ID).append(",").append(AD_Table_ID).append(",")
				.append(Record_ID).append(",'N' ");
			//  Hardcoded AD_Message - AD_Message is in Text
			sql.append("FROM AD_Message WHERE Value='NoMessageFound'");
			//  Try again
			no = executeUpdate(sql.toString());
		}

		return no == 1;
	}   //  insertNote

	/*************************************************************************/

	/**
	 *	Print SQL Warnings.
	 *  <br>
	 *		Usage: DB.printWarning(rs.getWarnings(), "xx");
	 *  @param comment comment
	 *  @param warning warning
	 */
	public static void printWarning(String comment, SQLWarning warning)
	{
		if (comment == null || warning == null || comment.length() == 0)
			throw new IllegalArgumentException("DB.printException - required parameter missing");
		s_log.warn("SQL Warning: " + comment);
		if (warning == null)
			return;
		//
		SQLWarning warn = warning;
		while (warn != null)
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append(warn.getMessage());
			buffer.append("; State=").append(warn.getSQLState()).append("; ErrorCode=").append(warn.getErrorCode());
			s_log.warn(buffer.toString());
			warn = warn.getNextWarning();
		}
	}	//	printWarning

	/**
	 *  Create SQL TO Date String from Timestamp
	 *
	 *  @param  time Date to be converted
	 *  @param  dayOnly true if time set to 00:00:00
	 *
	 *  @return TO_DATE('2001-01-30 18:10:20',''YYYY-MM-DD HH24:MI:SS')
	 *      or  TO_DATE('2001-01-30',''YYYY-MM-DD')
	 */
	public static String TO_DATE (Timestamp time, boolean dayOnly)
	{
		if (time == null)
		{
			if (dayOnly)
				return "TRUNC(SysDate)";
			return "SysDate";
		}

		StringBuffer dateString = new StringBuffer("TO_DATE('");
		//  YYYY-MM-DD HH24:MI:SS.mmmm  JDBC Timestamp format
		String myDate = time.toString();
		if (dayOnly)
		{
			dateString.append(myDate.substring(0,10));
			dateString.append("','YYYY-MM-DD')");
		}
		else
		{
			dateString.append(myDate.substring(0, myDate.indexOf(".")));	//	cut off miliseconds
			dateString.append("','YYYY-MM-DD HH24:MI:SS')");
		}
		return dateString.toString();
	}   //  TO_DATE

	/**
	 *  Create SQL TO Date String from Timestamp
	 *  @param time time
	 *  @return TO_DATE String
	 */
	public static String TO_DATE (Timestamp time)
	{
		return TO_DATE(time, true);
	}   //  TO_DATE

	/**
	 *  Create SQL for formatted Date, Number
	 *
	 *  @see org.compiere.util.DisplayType
	 *  @see org.compiere.util.Env
	 *
	 *  @param  columnName  the column name in the SQL
	 *  @param  displayType Display Type
	 *  @param  AD_Language 6 character language setting (from Env.LANG_*)
	 *
	 *  @return TRIM(TO_CHAR(columnName,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.'''))
	 *      or TRIM(TO_CHAR(columnName,'TM9')) depending on DisplayType and Language
	 */
	public static String TO_CHAR (String columnName, int displayType, String AD_Language)
	{
		if (columnName == null || AD_Language == null || columnName.length() == 0)
			throw new IllegalArgumentException("DB.TO_CHAR - required parameter missing");

		StringBuffer retValue = new StringBuffer("TRIM(TO_CHAR(");
		retValue.append(columnName);

		//  Numbers
		if (DisplayType.isNumeric(displayType))
		{
			if (displayType == DisplayType.Amount)
				retValue.append(",'9G999G990D00'");
			else
				retValue.append(",'TM9'");
			//  TO_CHAR(GrandTotal,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.''')
			if (!Language.isDecimalPoint(AD_Language))      //  reversed
				retValue.append(",'NLS_NUMERIC_CHARACTERS='',.'''");
		}
		else if (DisplayType.isDate(displayType))
		{
			retValue.append(",'")
				.append(Language.getLanguage(AD_Language).getDBdatePattern())
				.append("'");
		}

		retValue.append("))");
		//
		return retValue.toString();
	}   //  TO_CHAR_Number


	/**
	 *  Package Strings for SQL command
	 *  @param txt  String with text
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt)
	{
		return TO_STRING (txt, 0);
	}   //  TO_STRING

	/**
	 *	Package Strings for SQL command.
	 *  <pre>
	 *		-	include in ' (single quotes)
	 *		-	replace ' with ''
	 *  </pre>
	 *  @param txt  String with text
	 *  @param maxLength    Maximum Length of content or 0 to ignore
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt, int maxLength)
	{
		if (txt == null)
			return "NULL";

		//  Length
		String text = txt;
		if (maxLength != 0 && text.length() > maxLength)
			text = txt.substring(0, maxLength);

		char quote = '\'';
		//  copy characters		(wee need to look through anyway)
		StringBuffer out = new StringBuffer();
		out.append(quote);		//	'
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (c == quote)
				out.append("''");
			else
				out.append(c);
		}
		out.append(quote);		//	'
		//
		return out.toString();
	}	//	TO_STRING

}	//	DB

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
package org.compiere.util;

import java.math.*;
import java.util.*;
import java.text.*;
import java.security.*;
import javax.swing.*;

import java.sql.*;
import javax.sql.*;
import oracle.jdbc.*;

import org.apache.log4j.Logger;

import org.compiere.Compiere;
import org.compiere.util.*;
import org.compiere.db.*;

/**
 *  General Database Interface
 *
 *  @author     Jorg Janke
 *  @version    $Id: DB.java,v 1.17 2003/07/22 18:49:56 jjanke Exp $
 */
public final class DB
{
	/** Connection Descriptor           */
	private static CConnection      s_cc = null;
	/** Connection Cache r/o            */
	private static Connection[]		s_connections = null;
	/** Connection Cache Size           */
	private static int              s_conCacheSize = 2;   //  client
	/** Connection counter              */
	private static int              s_conCount = 0;
	/** Connection r/w                  */
	private static Connection		s_connectionRW = null;
	/**	Logger							*/
	private static Logger			s_log = Logger.getLogger (DB.class);

	/**
	 *	Client Login.
	 *  <p>
	 *  - Get Connection
	 *  - Compare User info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 * @param ctx context
	 * @param cc connection
	 * @param app_user user
	 * @param app_pwd pwd
	 * @param force ignore pwd
	 * @return  Array of Role KeyNamePair or null if error
	 * The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	protected static KeyNamePair[] login (Properties ctx,
		CConnection cc,
		String app_user, String app_pwd, boolean force)
	{
		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		//	Establish connection
		closeTarget();
		setDBTarget(cc);
		Env.setContext(ctx, "#Host", s_cc.getAppsHost());
		Env.setContext(ctx, "#Database", s_cc.getDbName());
		if (getConnectionRO() == null)
		{
			Log.saveError("NoDatabase", "");
			return null;
		}
		if (app_pwd == null)
			return null;
		//
		return loginDB (ctx, app_user, app_pwd, force);
	}   //  login

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user Principal
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx, Principal app_user)
	{
		if (app_user == null)
			return null;
		//  login w/o password as previously authorized
		return loginDB (ctx, app_user.getName(), null, false);
	}   //  app_user

	/**
	 *  Client Login.
	 *  <p>
	 *  Compare User Info
	 *  <p>
	 *  Sets Conext with login info
	 *
	 *  @param ctx context
	 *  @param app_user user id
	 *  @param app_pwd password
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	public static KeyNamePair[] login (Properties ctx,
		String app_user, String app_pwd)
	{
		if (app_pwd == null)
			return null;
		return loginDB (ctx, app_user, app_pwd, false);
	}   //  login

	/**
	 *  Actual DB login procedure.
	 *
	 *  @param ctx context
	 *  @param app_user user
	 *  @param app_pwd pwd
	 *  @param force ignore pwd
	 *  @return role array or null if in error.
	 *  The error (NoDatabase, UserPwdError, DBLogin) is saved in the log
	 */
	private static KeyNamePair[] loginDB (Properties ctx,
		String app_user, String app_pwd, boolean force)
	{
		s_log.info("login - User=" + app_user);

		if (ctx == null)
			throw new IllegalArgumentException("DB.login - required parameter missing");
		if (app_user == null)
			return null;

		KeyNamePair[] retValue = null;
		ArrayList list = new ArrayList();
		//
		String sql = "SELECT AD_User.AD_User_ID, AD_User.Description,"
			+ " AD_Role.AD_Role_ID, AD_Role.Name "
			+ "FROM AD_User, AD_User_Roles, AD_Role "
			+ "WHERE AD_User.AD_User_ID=AD_User_Roles.AD_User_ID"
			+ " AND AD_User_Roles.AD_Role_ID=AD_Role.AD_Role_ID"
			+ " AND AD_User.Name=?"		        							//	#1
			+ " AND AD_User.IsActive='Y' AND AD_Role.IsActive='Y' AND AD_User_Roles.IsActive='Y'";
		if (app_pwd != null)
			sql += " AND (AD_User.Password=? OR AD_User.Password=?)";   	//  #2/3
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setString(1, app_user);
			if (app_pwd != null)
			{
				pstmt.setString(2, app_pwd);
				pstmt.setString(3, Secure.getDigest(app_pwd));
			}
			//	execute a query
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())		//	no record found
				if (force)
				{
					Env.setContext(ctx, "#AD_User_Name", "System");
					Env.setContext(ctx, "#AD_User_ID", "0");
					Env.setContext(ctx, "#AD_User_Description", "System Forced Login");
					Env.setContext(ctx, "#User_Level", "S  ");  	//	Format 'SCO'
					Env.setContext(ctx, "#User_Client", "0");		//	Format c1, c2, ...
					Env.setContext(ctx, "#User_Org", "0"); 		//	Format o1, o2, ...
					rs.close();
					pstmt.close();
					retValue = new KeyNamePair[] {new KeyNamePair(0, "System Administrator")};
					return retValue;
				}
				else
				{
					rs.close();
					pstmt.close();
					Log.saveError("UserPwdError", app_user, false);
					return null;
				}

			Env.setContext(ctx, "#AD_User_Name", app_user);
			Env.setContext(ctx, "#AD_User_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#SalesRep_ID", rs.getInt("AD_User_ID"));
			Env.setContext(ctx, "#AD_User_Description", rs.getString("Description"));
			//
			Ini.setProperty(Ini.P_UID, app_user);
			if (Ini.getPropertyBool(Ini.P_STORE_PWD))
				Ini.setProperty(Ini.P_PWD, app_pwd);

			do	//	read all roles
			{
				int AD_Role_ID = rs.getInt("AD_Role_ID");
				String Name = rs.getString("Name");
				KeyNamePair p = new KeyNamePair(AD_Role_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("login", ex);
			Log.saveError("DBLogin", ex.getLocalizedMessage());
			return null;
		}

		//	Change via SQL detection comes here
		Env.setContext(ctx, "#User_SecurityID", "85263");
		//
		retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# roles = " + retValue.length);
		return retValue;
	}	//	login

	/**
	 *  Load Clients.
	 *  <p>
	 *  Sets Role info in context and loads its clients
	 *
	 *  @param ctx context
	 *  @param  role    role information
	 *  @return list of valid client KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadClients (Properties ctx, KeyNamePair role)
	{
		if (ctx == null || role == null)
			throw new IllegalArgumentException("DB.loadClients - required parameter missing");

		s_log.debug("loadClients - Role=" + role.toString());

		ArrayList list = new ArrayList();
		//	get Role details
		try
		{
			String sql = "SELECT DISTINCT r.UserLevel,r.ClientList,r.OrgList,"
				+ " r.C_Currency_ID,r.AmtApproval, oa.AD_Client_ID,c.Name "
				+ "FROM AD_Role r"
				+ " INNER JOIN AD_Role_OrgAccess oa ON (r.AD_Role_ID=oa.AD_Role_ID)"
				+ " INNER JOIN AD_Client c ON (oa.AD_Client_ID=c.AD_Client_ID) "
				+ "WHERE r.AD_Role_ID=?"		//	#1
				+ " AND r.IsActive='Y' AND c.IsActive='Y'";

			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, role.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadClients - No Clients for Role=" + role.getKey());
				return null;
			}

			//  Role Info
			Env.setContext(ctx, "#AD_Role_ID", role.getKey());
			Env.setContext(ctx, "#AD_Role_Name", role.getName());
			Ini.setProperty(Ini.P_ROLE, role.getName());

			//	User Level
			Env.setContext(ctx, "#User_Level", rs.getString(1));  	//	Format 'SCO'
			//	ClientList
			Env.setContext(ctx, "#User_Client", rs.getString(2));	//	Format c1, c2, ...
			//	OrgList
			Env.setContext(ctx, "#User_Org", rs.getString(3)); 		//	Format o1, o2, ...
			//  Approval Currency / Amount
			Env.setContext(ctx, "#Approval_C_Currency_ID", rs.getInt(4));
			BigDecimal approval = rs.getBigDecimal(5);
			String approvalStr = "0";
			if (approval != null)
				approvalStr = approval.toString();
			Env.setContext(ctx, "#Approval_Amt", approvalStr);


			//  load Clients
			do
			{
				int AD_Client_ID = rs.getInt(6);
				String Name = rs.getString(7);
				KeyNamePair p = new KeyNamePair(AD_Client_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadClients", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# clients = " + retValue.length);
		return retValue;
	}   //  loadClients

	/**
	 *  Load Organizations.
	 *  <p>
	 *  Sets Client info in context and loads its organization, the role has access to
	 *
	 *  @param ctx context
	 *  @param  client    client information
	 *  @return list of valid Org KeyNodePairs or null if in error
	 */
	public static KeyNamePair[] loadOrgs (Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadOrgs - required parameter missing");

		s_log.debug("loadOrgs - Client=" + client.toString());

		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Role_ID");

		ArrayList list = new ArrayList();
		int AD_Role_ID = Env.getContextAsInt(ctx,"#AD_Role_ID");
		s_log.debug("AD_Role_ID=" + AD_Role_ID);

		//	get Client details for role
		try
		{
			String sql = "SELECT c.Value,c.SMTPHost,c.IsMultiLingualDocument,c.AD_Language,"	//	1..4
				+ " o.AD_Org_ID,o.Name "				//	5..6
				+ "FROM AD_Client c"
				+ " INNER JOIN AD_Org o ON (o.AD_Client_ID=c.AD_Client_ID) "
				+ "WHERE o.AD_Client_ID=?"  			//  #1
				+ " AND o.IsSummary='N' AND o.IsActive='Y'"
				+ " AND o.AD_Org_ID IN "
				+   "(SELECT AD_Org_ID FROM AD_Role_OrgAccess WHERE AD_Role_ID=?)"; //  #2
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, client.getKey());
			pstmt.setInt(2, AD_Role_ID);
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.error("loadOrgs - No Org for Client=" + client.getKey());
				return null;
			}

			//  Client Info
			Env.setContext(ctx, "#AD_Client_ID", client.getKey());
			Env.setContext(ctx, "#AD_Client_Name", client.getName());
			Ini.setProperty(Ini.P_CLIENT, client.getName());
			//
			Env.setContext(ctx, "#Client_Value", rs.getString(1));
			Env.setContext(ctx, "#Client_SMTP", rs.getString(2));
			Env.setContext(ctx, "#IsMultiLingualDocument", rs.getString(3));

			//  load Orgs
			do
			{
				int AD_Org_ID = rs.getInt(5);
				String Name = rs.getString(6);
				KeyNamePair p = new KeyNamePair(AD_Org_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadOrgs", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.debug("# orgs = " + retValue.length);
		return retValue;
	}   //  loadOrgs

	/**
	 *	Load Preferences into Context for selected client.
	 *  <p>
	 *  Sets Org info in context and loads relevant field from
	 *	- AD_Client/Info,
	 *  - C_AcctSchema,
	 *  - C_AcctSchema_Elements
	 *	- AD_Preference
	 *  <p>
	 *  Assumes that the context is set for #AD_Client_ID, #AD_User_ID, #AD_Role_ID
	 *
	 *  @param ctx context
	 *  @param  org    org information
	 *  @param  warehouse   optional warehouse information
	 *  @param  timestamp   optional date
	 *  @param  printerName optional printer info
	 *  @returns AD_Message of error (NoValidAcctInfo) or ""
	 */
	public static String loadPreferences (Properties ctx,
		KeyNamePair org, KeyNamePair warehouse, Timestamp timestamp, String printerName)
	{
		s_log.info("loadPreferences - Org=" + org);

		if (ctx == null || org == null)
			throw new IllegalArgumentException("DB.loadPreferences - required parameter missing");
		if (Env.getContext(ctx,"#AD_Client_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Client_ID");
		if (Env.getContext(ctx,"#AD_User_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_User_ID");
		if (Env.getContext(ctx,"#AD_Role_ID").length() == 0)
			throw new UnsupportedOperationException("DB.loadPreferences - Missing Comtext #AD_Role_ID");


		//  Org Info - assumes that it is valid
		Env.setContext(ctx, "#AD_Org_ID", org.getKey());
		Env.setContext(ctx, "#AD_Org_Name", org.getName());
		Ini.setProperty(Ini.P_ORG, org.getName());

		//  Warehouse Info
		if (warehouse != null)
		{
			Env.setContext(ctx, "#M_Warehouse_ID", warehouse.getKey());
			Ini.setProperty(Ini.P_WAREHOUSE, warehouse.getName());
		}

		//	Date (default today)
		long today = System.currentTimeMillis();
		if (timestamp != null)
			today = timestamp.getTime();
		java.sql.Date sd = new java.sql.Date(today);
		Env.setContext(ctx, "#Date", sd.toString());	//	YYYY-MM-DD

		//	Optional Printer
		if (printerName == null)
			printerName = "";
		Env.setContext(ctx, "#Printer", printerName);
		Ini.setProperty(Ini.P_PRINTER, printerName);

		//	Other
		Env.setAutoCommit(ctx, Ini.getProperty(Ini.P_A_COMMIT).equals("Y"));
		Env.setContext(ctx, "#CompiereSys", Ini.getProperty(Ini.P_COMPIERESYS));
		Env.setContext(ctx, "#ShowAcct", Ini.getProperty(Ini.P_SHOW_ACCT));
		Env.setContext(ctx, "#ShowTrl", Ini.getProperty(Ini.P_SHOW_TRL));

		String retValue = "";
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		int AD_Org_ID =  org.getKey();
		int AD_User_ID =  Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID =  Env.getContextAsInt(ctx, "#AD_Role_ID");

		//	Other Settings
		Env.setContext(ctx, "#YYYY", "Y");
		Env.setContext(ctx, "#StdPrecision", 2);

		//	AccountSchema Info (first)
		String sql = "SELECT * "
			+ "FROM C_AcctSchema a, AD_ClientInfo c "
			+ "WHERE a.C_AcctSchema_ID=c.C_AcctSchema1_ID "
			+ "AND c.AD_Client_ID=?";
		try
		{
			int C_AcctSchema_ID = 0;
			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				//  No Warning for System
				if (AD_Role_ID != 0)
					retValue = "NoValidAcctInfo";
			}
			else
			{
				//	Accounting Info
				C_AcctSchema_ID = rs.getInt("C_AcctSchema_ID");
				Env.setContext(ctx, "$C_AcctSchema_ID", C_AcctSchema_ID);
				Env.setContext(ctx, "$C_Currency_ID", rs.getInt("C_Currency_ID"));
				Env.setContext(ctx, "$HasAlias", rs.getString("HasAlias"));
			}
			rs.close();
			pstmt.close();

			//	Accounting Elements
			sql = "SELECT ElementType "
				+ "FROM C_AcctSchema_Element "
				+ "WHERE C_AcctSchema_ID=?"
				+ " AND IsActive='Y'";
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, C_AcctSchema_ID);
			rs = pstmt.executeQuery();
			while (rs.next())
				Env.setContext(ctx, "$Element_" + rs.getString("ElementType"), "Y");
			rs.close();
			pstmt.close();

			//	This reads all relevant window neutral defaults
			//	overwriting superseeded ones.  Window specific is read in Mainain
			sql = "SELECT Attribute, Value, AD_Window_ID "
				+ "FROM AD_Preference "
				+ "WHERE AD_Client_ID IN (0, @#AD_Client_ID@)"
				+ " AND AD_Org_ID IN (0, @#AD_Org_ID@)"
				+ " AND (AD_User_ID IS NULL OR AD_User_ID=0 OR AD_User_ID=@#AD_User_ID@)"
				+ " AND IsActive='Y' "
				+ "ORDER BY Attribute, AD_Client_ID, AD_User_ID DESC, AD_Org_ID";
				//	the last one overwrites - System - Client - User - Org - Window
			sql = Env.parseContext(ctx, 0, sql, false);
			if (sql.length() == 0)
				s_log.error("loadPreferences - Missing Environment");
			else
			{
				pstmt = prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					int AD_Window_ID = rs.getInt(3);
					String at = "";
					if (rs.wasNull())
						at = "P|" + rs.getString(1);
					else
						at = "P" + AD_Window_ID + "|" + rs.getString(1);
					String va = rs.getString(2);
					Env.setContext(ctx, at, va);
				}
				rs.close();
				pstmt.close();
			}
		}
		catch (SQLException ex)
		{
			s_log.error("loadPreferences (" + sql + ")", ex);
		}

		//	Default Values
		s_log.info("Default Values ...");
		sql = "SELECT t.TableName, c.ColumnName "
			+ "FROM AD_Column c "
			+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID) "
			+ "WHERE c.IsKey='Y' AND t.IsActive='Y'"
			+ " AND EXISTS (SELECT * FROM AD_Column cc "
			+ " WHERE ColumnName = 'IsDefault' AND t.AD_Table_ID=cc.AD_Table_ID AND cc.IsActive='Y')";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				loadDefault (ctx, rs.getString(1), rs.getString(2));
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("loadPreferences", e);
		}
		//
		Ini.saveProperties(Ini.isClient());
		//
		return retValue;
	}	//	loadPreferences

	/**
	 *	Load Default Value for Table into Context.
	 *
	 *  @param ctx context
	 *  @param TableName table name
	 *  @param ColumnName column name
	 */
	private static void loadDefault (Properties ctx, String TableName, String ColumnName)
	{
		String value = null;
		//
		String SQL = "SELECT " + ColumnName + " FROM " + TableName
			+ " WHERE IsDefault='Y' AND IsActive='Y' ORDER BY AD_Client_ID";
		SQL = Access.addROAccessSQL(ctx, SQL, TableName, false);
		try
		{
			PreparedStatement pstmt = prepareStatement(SQL);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())		//	overwrites system defaults
				value = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("loadDefault - " + TableName + " (" + SQL + ")", e);
			return;
		}
		//	Set Context Value
		if (value != null && value.length() != 0)
		{
			if (TableName.equals("C_DocType"))
				Env.setContext(ctx, "#C_DocTypeTarget_ID", value);
			else
				Env.setContext(ctx, "#" + ColumnName, value);
		}
	}	//	loadDefault

	/**
	 *  Load Warehouses
	 *
	 * @param ctx context
	 * @param client client
	 * @return Array of Warehouse Info
	 */
	public static KeyNamePair[] loadWarehouses(Properties ctx, KeyNamePair client)
	{
		if (ctx == null || client == null)
			throw new IllegalArgumentException("DB.loadWarehouses - required parameter missing");

		s_log.info("loadWarehouses - Client=" + client.toString());

		ArrayList list = new ArrayList();
		try
		{
			String sql = "SELECT M_Warehouse_ID, Name FROM M_Warehouse "
				+ "WHERE AD_Client_ID=? AND IsActive='Y'";  //  #1

			PreparedStatement pstmt = prepareStatement(sql);
			pstmt.setInt(1, client.getKey());
			ResultSet rs = pstmt.executeQuery();

			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				s_log.warn("loadWarehouses - No Warehouses for Client=" + client.getKey());
				return null;
			}

			//  load Warehousess
			do
			{
				int AD_Warehouse_ID = rs.getInt(1);
				String Name = rs.getString(2);
				KeyNamePair p = new KeyNamePair(AD_Warehouse_ID, Name);
				list.add(p);
			}
			while (rs.next());

			rs.close();
			pstmt.close();
		}
		catch (SQLException ex)
		{
			s_log.error("loadWarehouses", ex);
			return null;
		}
		//
		KeyNamePair[] retValue = new KeyNamePair[list.size()];
		list.toArray(retValue);
		s_log.info("# warehouses = " + retValue.length);
		return retValue;
	}   //  loadWarehouses

	/*************************************************************************/

	/**
	 *  Set connection
	 *  @param cc connection
	 */
	public static void setDBTarget (CConnection cc)
	{
		if (cc == null)
			throw new IllegalArgumentException("DB.setDBTarget connection is NULL");

		if (s_cc == null)
			s_cc = cc;
		synchronized (s_cc)    //  use as mutex
		{
			s_cc = cc;
			//  Closing existing
			if (s_connections != null)
			{
				for (int i = 0; i < s_connections.length; i++)
				{
					try {
						s_connections[i].close();
					} catch (Exception e) {}
				}
			}
			s_connections = null;
		}
	}   //  setDBTarget

	/**
	 *  Is there a connection to the database ?
	 *  @return true, if connected to database
	 */
	public static boolean isConnected()
	{
		try
		{
			getConnectionRW();	//	try to get a connection
			return true;
		}
		catch (Exception e)
		{
		}
		return false;
	}   //  isConnected

	/**
	 *	Return (pooled) r/w AutoCommit, read committed connection
	 *
	 *  @return Connection (r/w)
	 */
	public static Connection getConnectionRW ()
	{
		//	check health of connection
		try
		{
			if (s_connectionRW == null)
				;
			else if (s_connectionRW.isClosed())
				s_connectionRW = null;
			else if (s_connectionRW instanceof OracleConnection && ((OracleConnection)s_connectionRW).pingDatabase(1) < 0)
				s_connectionRW = null;
			else
				 s_connectionRW.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		catch (Exception e)
		{
			s_connectionRW = null;
		}
		if (s_connectionRW == null)
			s_connectionRW = s_cc.getConnection (true, Connection.TRANSACTION_READ_COMMITTED);
		if (s_connectionRW == null)
			throw new UnsupportedOperationException("DB.getConnectionRW - @NoDBConnection@");
		return s_connectionRW;
	}   //  getConnectionRW

	/**
	 *	Return read committed, read/only connection with AutoCommit from pool
	 *  @return Connection (r/o)
	 */
	static Connection getConnectionRO ()
	{
		try
		{
			synchronized (s_cc)    //  use as mutex as s_connection is null the first time
			{
				if (s_connections == null)
					s_connections = createConnections (Connection.TRANSACTION_READ_COMMITTED);     //  see below
			}
		}
		catch (Exception e)
		{
			s_log.error("getConnectionRO", e);
		}

		//  check health of connection
		int pos = s_conCount++;
		Connection connection = s_connections[pos % s_conCacheSize];
		try
		{
			if (connection == null)
				;
			else if (connection.isClosed())
				connection = null;
			else if (connection instanceof OracleConnection && ((OracleConnection)connection).pingDatabase(1) < 0)
				connection = null;
			else
				 connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		catch (Exception e)
		{
			connection = null;
		}
		//	Get new
		if (connection == null)
		{
			System.out.println("DB.getConnectionRO - replacing connection #" + pos % s_conCacheSize);
		//	s_log.error("getConnectionRO - replacing connection #" + pos % s_conCacheSize);
			connection = s_cc.getConnection (true, Connection.TRANSACTION_READ_COMMITTED);            //  see above
		/*	try
			{
				retValue.setReadOnly(true);     //  not supported by Oracle
			}
			catch (Exception e)
			{
				System.err.println("DB.getConnectionRO - Cannot set to R/O - " + e);
			}   */
			s_connections[pos % s_conCacheSize] = connection;
		}
		if (connection == null)
			throw new UnsupportedOperationException("DB.getConnectionRO - @NoDBConnection@");
		return connection;
	}	//	getConnectionRO

	/**
	 *	Create new Connection.
	 *  The connection must be closed explicitly by the application
	 *
	 *  @param autoCommit auto commit
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Connection connection
	 */
	public static Connection createConnection (boolean autoCommit, int trxLevel)
	{
		s_log.debug("createConnection " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", AutoCommit=" + autoCommit + ", TrxLevel=" + trxLevel);
		return s_cc.getConnection (autoCommit, trxLevel);
	}	//	createConnection

	/**
	 *	Create new set of r/o Connections.
	 *  R/O connection might not be supported by DB
	 *
	 *  @param trxLevel - Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE.
	 *  @return Array of Connections (size based on s_conCacheSize)
	 */
	private static Connection[] createConnections (int trxLevel)
	{
		s_log.debug("createConnections (" + s_conCacheSize + ") " + s_cc.getConnectionURL()
			+ ", UserID=" + s_cc.getDbUid() + ", TrxLevel=" + trxLevel);
		Connection cons[] = new Connection[s_conCacheSize];
		try
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				cons[i] = s_cc.getConnection (true, trxLevel);  //  auto commit
				if (cons[i] == null)
					System.err.println("createConnections - connection is NULL");	//	don't use log
			}
		}
		catch (Exception e)
		{
			//  Don't use Log
			System.err.println("DB.createConnections - " + e.getMessage());
		}
		return cons;
	}	//	createConnections

	/**
	 *  Get Database Driver.
	 *  Access to database specific functionality.
	 *  @return Compiere Database Driver
	 */
	public static CompiereDatabase getDatabase()
	{
		if (s_cc != null)
			return s_cc.getDatabase();
		return null;
	}   //  getDatabase

	/*************************************************************************/

	/**
	 *  Check database Version with Code version
	 *  @param ctx context
	 *  @return true if Database version (date) is the same
	 */
	public static boolean isDatabaseOK (Properties ctx)
	{
		//	Force loading Messages of current language
		Msg.getMsg(ctx, "0");

		//  Check Version
		String version = "?";
		String sql = "SELECT Version FROM AD_System";
		try
		{
			PreparedStatement pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				version = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("Problem with AD_System Table - Run system.sql script - " + e.toString());
		}
		s_log.info("isDatabaseOK - DB_Version=" + version);
		//  identical version
		if (Compiere.DB_VERSION.equals(version))
			return true;

		String AD_Message = "DatabaseVersionError";
		String title = org.compiere.Compiere.getName() + " " +  Msg.getMsg(ctx, AD_Message, true);
		//	Code assumes Database version {0}, but Database has Version {1}.
		String msg = Msg.getMsg(ctx, AD_Message);	//	complete message
		msg = MessageFormat.format(msg, new Object[] {Compiere.DB_VERSION, version});
		Object[] options = { UIManager.get("OptionPane.noButtonText"), "Migrate" };
		int no = JOptionPane.showOptionDialog (null, msg,
			title, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
			UIManager.getIcon("OptionPane.errorIcon"), options, options[0]);
		if (no == 1)
		{
			try
			{
				Class.forName("com.compiere.client.StartMaintain").newInstance();
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog (null,
					ex.getMessage() + "\nSee: http://www.compiere.com/maintain",
					title, JOptionPane.ERROR_MESSAGE);
				s_log.error("isDatabaseOK - " + ex.getMessage());
			}
		}
		return false;
	}   //  isDatabaseOK

	/*************************************************************************/

	/**
	 *	Security Context Login.
	 *  @param uid uid
	 *  @param pwd pwd
	 *  @param role role
	 *  @return true if connected
	 */
	public static boolean login_context (String uid, String pwd, String role)
	{
		if (uid == null || pwd == null || role == null || uid.length() == 0 || pwd.length() == 0 || role.length() == 0)
			throw new IllegalArgumentException("DB.login_context - required parameter missing");
		s_log.info("login_context uid=" + uid);
		if (uid == null || pwd == null || role == null)
			return false;
		//
		String SQL = "{CALL Compiere_Context.Login(?,?,?,?)}";
		boolean result = true;
		try
		{
			CallableStatement cstmt = getConnectionRO().prepareCall(SQL);
			cstmt.setString(1, uid);
			cstmt.setString(2, pwd);
			cstmt.setString(3, role);
			cstmt.setString(4, Language.getBaseAD_Language());
			result = cstmt.execute();
			cstmt.close();
		}
		catch (SQLException e)
		{
			s_log.error("login_context", e);
			result = false;
		}
		return result;
	}	//	login_context

	/*************************************************************************/

	/**
	 *	Close Target
	 */
	public static void closeTarget()
	{
		if (s_connections != null)
		{
			for (int i = 0; i < s_conCacheSize; i++)
			{
				try
				{
					if (s_connections[i] != null)
						s_connections[i].close();
				}
				catch (SQLException e)
				{
					s_log.error("close connection #" + i, e);
				}
				s_connections[i] = null;
			}
			try
			{
				if (s_connectionRW != null)
					s_connectionRW.close();
			}
			catch (SQLException e)
			{
				s_log.error("close R/W connection", e);
			}
			s_connectionRW = null;
		}
		s_connections = null;
//		EJB.close();
	}	//	closeTarget

	/*************************************************************************/

	/**
	 *	Prepare Forward Read Only Call
	 *  @param RO_SQL sql (RO)
	 *  @return Callable Statement
	 */
	public static CallableStatement prepareCall(String RO_SQL)
	{
		if (RO_SQL == null || RO_SQL.length() == 0)
			throw new IllegalArgumentException("DB.prepareCall - required parameter missing - " + RO_SQL);
		//
		String sql = getDatabase().convertStatement(RO_SQL);
		try
		{
			return getConnectionRO().prepareCall
				(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		}
		catch (SQLException e)
		{
			s_log.error("prepareCall (" + sql + ")", e);
		}
		return null;
	}	//	prepareCall

	/*************************************************************************/

	/**
	 *	Prepare Read Only Statement
	 *  @param RO_SQL sql (RO)
	 *  @return Prepared Statement
	 */
	public static PreparedStatement prepareStatement(String RO_SQL)
	{
		return prepareStatement(RO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}	//	prepareStatement

	/**
	 *	Prepare Statement
	 *
	 *  @param SQL sql statement
	 *  @param resultSetType - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param resultSetConcurrency - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Prepared Statement r/o or r/w depending on concur
	 */
	public static PreparedStatement prepareStatement(String SQL, int resultSetType, int resultSetConcurrency)
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.prepareStatement - required parameter missing - " + SQL);
		//
		return new CompiereStatement(SQL, resultSetType, resultSetConcurrency);
		/**
		try
		{
			Connection conn = null;
			if (resultSetConcurrency == ResultSet.CONCUR_UPDATABLE)
				conn = getConnectionRW();
			else
				conn = getConnectionRO();
			return conn.prepareStatement (sql, resultSetType, resultSetConcurrency);
		}
		catch (SQLException e)
		{
			s_log.error("prepareStatement (" + sql + ")", e);
		}
		return null;
		**/
	}	//	prepareStatement

	/**
	 *	Create Read Only Statement
	 *  @return Statement
	 */
	public static Statement createStatement()
	{
		return createStatement (ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}	//	createStatement

	/**
	 *	Create Statement
	 *
	 *  @param type - ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_SCROLL_SENSITIVE
	 *  @param concur - ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
	 *  @return Statement - either r/w ir r/o depending on concur
	 */
	public static Statement createStatement(int type, int concur)
	{
		try
		{
			Connection conn = null;
			if (concur == ResultSet.CONCUR_UPDATABLE)
				conn = getConnectionRW();
			else
				conn = getConnectionRO();
			return conn.createStatement(type, concur);
		}
		catch (SQLException e)
		{
			s_log.error("createStatement", e);
		}
		return null;
	}	//	createStatement

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param SQL sql
	 *  @return number of rows updated
	 */
	public static int executeUpdate (String SQL)
	{
		return executeUpdate(SQL, false);
	}	//	executeUpdate

	/**
	 *	Execute Update.
	 *  saves "DBExecuteError" in Log
	 *  @param SQL sql
	 * 	@param ignoreError if true, no execution error is reported
	 *  @return number of rows updated
	 */
	public static int executeUpdate (String SQL, boolean ignoreError)
	{
		if (SQL == null || SQL.length() == 0)
			throw new IllegalArgumentException("DB.executeUpdate - required parameter missing - " + SQL);
		//
		String sql = getDatabase().convertStatement(SQL);
		int no = 0;
		Statement stmt = null;
		try
		{
			Connection conn = getConnectionRW();
			stmt = conn.createStatement();
			no = stmt.executeUpdate(sql);
		}
		catch (SQLException e)
		{
			if (!ignoreError)
			{
				s_log.error ("executeUpdate - " + sql, e);
				Log.saveError ("DBExecuteError", e.getLocalizedMessage ());
			}
		}
		finally
		{
			//  Always close cursor
			try
			{
				stmt.close();
			}
			catch (SQLException e2)
			{
				s_log.error("executeUpdate - cannot close statement");
			}
		}
		return no;
	}	//	execute Update


	/**
	 *	Commit - commit on RW connection.
	 *  Is not required as RW connection is AutoCommit
	 *  @param throwException if true, re-throws exception
	 *  @return true if not needed or success
	 *  @throws SQLException
	 */
	public static boolean commit (boolean throwException) throws SQLException
	{
		try
		{
			Connection conn = getConnectionRW();
		//	if (!conn.getAutoCommit())
				conn.commit();
		}
		catch (SQLException e)
		{
			s_log.error("commit", e);
			if (throwException)
				throw e;
			return false;
		}
		return true;
	}	//	commit

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @return next no
	 */
	public static int getKeyNextNo (Properties ctx, int WindowNo, String TableName)
	{
		if (ctx == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getKeyNextNo - required parameter missing");
		String CompiereSys = Env.getContext(ctx, "#CompiereSys");
		int AD_Client_ID = Env.getContextAsInt(ctx, WindowNo, "AD_Client_ID");
		//
		return getKeyNextNo (AD_Client_ID, CompiereSys, TableName);
	}   //  getKeyNextNo

	/**
	 *	Get next number for Key column = 0 is Error.
	 *  @param AD_Client_ID client
	 *  @param CompiereSys compiere sys
	 *  @param TableName table name
	 *  @return next no
	 */
	public static int getKeyNextNo (int AD_Client_ID, String CompiereSys, String TableName)
	{
		if (TableName == null || CompiereSys == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getKeyNextNo - required parameter missing");
		int retValue = 0;

		//	Check CompiereSys
	//	if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
	//		throw new UnsupportedOperationException("DB.getKeyNextNo - Cannot add System records");
		//
		try
		{
			String SQL = "{CALL AD_Sequence_Next(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setString(1, TableName);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.NUMERIC);
			cstmt.executeUpdate();
			retValue = cstmt.getInt(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getKeyNextNo - Table=" + TableName + ")", e);
		}
		return retValue;
	}	//	getKeyNextNo

	/**
	 *	Get Document Number for current document.
	 *  <br>
	 *  - first search for DocType based Document No
	 *  - then Search for DocumentNo based on TableName
	 *  @param ctx context
	 *  @param WindowNo window
	 *  @param TableName table
	 *  @param onlyDocType Do not search for document no based on TableName
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (Properties ctx, int WindowNo, String TableName, boolean onlyDocType)
	{
		if (ctx == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getDocumentNo - required parameter missing");
		String CompiereSys = Env.getContext(ctx, "#CompiereSys");
		int AD_Client_ID = Env.getContextAsInt(ctx, WindowNo, "AD_Client_ID");

		//	Get C_DocType_ID from context - NO Defaults -
		int C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID");
		if (C_DocType_ID == 0)
			C_DocType_ID = Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID");
		if (C_DocType_ID == 0)
		{
			s_log.debug("getDocumentNo - for Window=" + WindowNo
				+ " - Target=" + Env.getContextAsInt(ctx, WindowNo + "|C_DocTypeTarget_ID") + "/" + Env.getContextAsInt(ctx, WindowNo, "C_DocTypeTarget_ID")
				+ " - Actual=" + Env.getContextAsInt(ctx, WindowNo + "|C_DocType_ID") + "/" + Env.getContextAsInt(ctx, WindowNo, "C_DocType_ID"));
			return getDocumentNo (AD_Client_ID, CompiereSys, TableName);
		}

		//	Check CompiereSys
		if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
			throw new UnsupportedOperationException("DB.getDocumentNo - Cannot add System records");
		//
		String retValue = getDocumentNo(AD_Client_ID, C_DocType_ID);
		if (!onlyDocType && retValue == null)
			return getDocumentNo (AD_Client_ID, CompiereSys, TableName);
		return retValue;
	}	//	getDocumentNo

	/**
	 *	Get Document Number for current document.
	 *  @param AD_Client_ID client
	 *  @param C_DocType_ID (target) document type
	 *	@return DocumentNo or null, if no doc number defined
	 */
	public static String getDocumentNo (int AD_Client_ID, int C_DocType_ID)
	{
		String retValue = null;
		try
		{
			String SQL = "{CALL AD_Sequence_DocType(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setInt(1, C_DocType_ID);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.VARCHAR);
			cstmt.executeUpdate();
			retValue = cstmt.getString(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getDocumentNo - DocType=" + C_DocType_ID, e);
		}
		s_log.info("getDocumentNo - DocType=" + C_DocType_ID + " -> " + retValue);
		return retValue;
	}	//	getDocumentNo


	/**
	 *  Get Next Document No
	 *  @param AD_Client_ID client
	 *  @param CompiereSys system
	 *  @param TableName table name
	 *  @return DocumentNo
	 */
	public static String getDocumentNo (int AD_Client_ID, String CompiereSys, String TableName)
	{
		if (CompiereSys == null || TableName == null || TableName.length() == 0)
			throw new IllegalArgumentException("DB.getDocumentNo - required parameter missing");
		//	Check CompiereSys
	//	if (AD_Client_ID == 0 && !CompiereSys.equals("Y"))
	//		throw new UnsupportedOperationException("DB.getDocumentNo - Cannot add System records");
		//
		String retValue = null;
		try
		{
			String SQL = "{CALL AD_Sequence_Doc(?,?,?)}";
			CallableStatement cstmt = prepareCall(SQL);
			cstmt.setString(1, "DocumentNo_" + TableName);
			cstmt.setInt(2, AD_Client_ID);
			cstmt.registerOutParameter(3, Types.VARCHAR);
			cstmt.executeUpdate();
			retValue = cstmt.getString(3);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getDocumentNo  TableName=" + TableName, e);
		}
		s_log.info("getDocumentNo - TableName=" + TableName + " -> " + retValue);
		return retValue;
	}   //  getDocumentNo


	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int int_param1)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value " + sql + " - Param1=" + int_param1);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue

	/**
	 * 	Get Value from sql
	 * 	@param sql sql
	 * 	@param int_param1 parameter 1
	 * 	@param s_param2 parameter 2
	 * 	@return first value or -1
	 */
	public static int getSQLValue (String sql, int int_param1, String s_param2)
	{
		int retValue = -1;
		PreparedStatement pstmt = null;
		try
		{
			pstmt = prepareStatement(sql);
			pstmt.setInt(1, int_param1);
			pstmt.setString(2, s_param2);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			else
				s_log.warn("getSQLValue - No Value: " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2);
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error("getSQLValue - " + sql + " - Param1=" + int_param1 + ",Param2=" + s_param2, e);
		}
		finally
		{
			try
			{
				if (pstmt != null)
					pstmt.close ();
			}
			catch (Exception e)
			{}
			pstmt = null;
		}
		return retValue;
	}	//	getSQLValue


	/**
	 *  Convert an amount with today's spot rate
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  Amt         The amount to be converted
	 * 	@param	AD_Client_ID client
	 * 	@param	AD_Org_ID	organization
	 *  @return converted amount
	 */
	public static BigDecimal getConvertedAmt (BigDecimal Amt, int CurFrom_ID, int CurTo_ID, int AD_Client_ID, int AD_Org_ID)
	{
		return getConvertedAmt (Amt, CurFrom_ID, CurTo_ID, null, null, AD_Client_ID, AD_Org_ID);
	}   //  getConvertedAmt

	/**
	 *	Convert an amount
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  ConvDate    The Conversion date - if null - use current date
	 *  @param  RateType    The Conversion rate type - if null/empty - use Spot
	 *  @param  Amt         The amount to be converted
	 * 	@param	AD_Client_ID client
	 * 	@param	AD_Org_ID	organization
	 *  @return converted amount
	 */
	public static BigDecimal getConvertedAmt (BigDecimal Amt, int CurFrom_ID, int CurTo_ID,
		Timestamp ConvDate, String RateType, int AD_Client_ID, int AD_Org_ID)
	{
		if (Amt == null)
			throw new IllegalArgumentException("DB.getConvertedAmt - required parameter missing - Amt");
		BigDecimal retValue = null;
		if (CurFrom_ID == CurTo_ID || Amt.equals(Env.ZERO))
			return Amt;
		//
		try
		{
			String sql = "{? = call C_Currency_Convert(?,?,?,?,?, ?,?) }";
			CallableStatement cstmt = prepareCall(sql);
			//
			cstmt.registerOutParameter(1, Types.NUMERIC);
			//
			cstmt.setBigDecimal(2, Amt);					//	Amount		IN  	NUMBER
			cstmt.setInt(3, CurFrom_ID);             		//	CurFrom		IN 		NUMBER
			cstmt.setInt(4, CurTo_ID);                  	//	CurTo		IN 		NUMBER
			if (ConvDate == null)
				ConvDate = new Timestamp (System.currentTimeMillis());
			cstmt.setTimestamp(5, ConvDate);				//	ConvDate	IN 		DATE
			if (RateType == null || RateType.equals(""))
				RateType = "S";
			cstmt.setString(6, RateType);					//	RateType	IN 		CHAR
			cstmt.setInt(7, AD_Client_ID);
			cstmt.setInt(8, AD_Org_ID);
			//
			cstmt.executeUpdate();
			retValue = cstmt.getBigDecimal(1);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getConvertedAmt", e);
		}
		if (retValue == null)
			s_log.info("getConvertedAmt - not found - CurFrom=" + CurFrom_ID + ", CurTo=" + CurTo_ID
				+ ", " + ConvDate + ", " + RateType + ", Client=" + AD_Client_ID + ", Org=" + AD_Org_ID);
		return retValue;
	}	//	getConvertedAmt

	/**
	 *	Get Currency Rate
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  ConvDate    The Conversion date - if null - use current date
	 *  @param  RateType    The Conversion rate type - if null/empty - use Spot
	 * 	@param	AD_Client_ID client
	 * 	@param	AD_Org_ID	organization
	 *  @return currency Rate or null
	 */
	public static BigDecimal getConvesionRate (int CurFrom_ID, int CurTo_ID,
		Timestamp ConvDate, String RateType, int AD_Client_ID, int AD_Org_ID)
	{
		BigDecimal retValue = null;
		if (CurFrom_ID == CurTo_ID)
			return new BigDecimal(1);
		//
		try
		{
			String sql = "{? = call C_Currency_Rate(?,?,?,?,?,?)}";
			CallableStatement cstmt = prepareCall(sql);
			//
			cstmt.registerOutParameter(1, Types.NUMERIC);
			//
			cstmt.setInt(2, CurFrom_ID);             		//	CurFrom		IN 		NUMBER
			cstmt.setInt(3, CurTo_ID);                  	//	CurTo		IN 		NUMBER
			if (ConvDate == null)
				ConvDate = new Timestamp (System.currentTimeMillis());
			cstmt.setTimestamp(4, ConvDate);				//	ConvDate	IN 		DATE
			if (RateType == null || RateType.equals(""))
				RateType = "S";
			cstmt.setString(5, RateType);					//	RateType	IN 		CHAR
			cstmt.setInt(6, AD_Client_ID);
			cstmt.setInt(7, AD_Org_ID);
			//
			cstmt.executeUpdate();
			retValue = cstmt.getBigDecimal(1);
			cstmt.close();
		}
		catch(SQLException e)
		{
			s_log.error("getConvesionRate", e);
		}
		if (retValue == null)
			s_log.info ("getConversionRate - not found - CurFrom=" + CurFrom_ID + ", CurTo=" + CurTo_ID
			  + ", " + ConvDate + ", " + RateType + ", Client=" + AD_Client_ID + ", Org=" + AD_Org_ID);
		return retValue;
	}	//	getConvesionRate


	/**
	 *  Get fully qualified Account Combination
	 *
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param C_AcctSchema_ID acct schema
	 *  @param Account_ID natural account
	 *  @param  base_ValidCombination_ID optional base combination to be specified
	 *  @param Alias aloas
	 *  @param AD_User_ID user
	 *  @param M_Product_ID product
	 *  @param C_BPartner_ID partner
	 *  @param AD_OrgTrx_ID trx org
	 *  @param C_LocFrom_ID loc from
	 *  @param C_LocTo_ID loc to
	 *  @param C_SRegion_ID sales region
	 *  @param C_Project_ID project
	 *  @param C_Campaign_ID campaign
	 *  @param C_Activity_ID activity
	 *  @param User1_ID user1
	 *  @param User2_ID user2
	 *  @return C_ValidCombination_ID of existing or new Combination
	 */
	public static int getValidCombination (int AD_Client_ID, int AD_Org_ID,
		int C_AcctSchema_ID, int Account_ID, int base_ValidCombination_ID, String Alias, int AD_User_ID,
		int M_Product_ID, int C_BPartner_ID, int AD_OrgTrx_ID,
		int C_LocFrom_ID, int C_LocTo_ID, int C_SRegion_ID, int C_Project_ID,
		int C_Campaign_ID, int C_Activity_ID, int User1_ID, int User2_ID)
	{
		int retValue = 0;
		StringBuffer sb = new StringBuffer ("C_ValidCombination_Get[");
		try
		{
			String sql = "{CALL C_ValidCombination_Get(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)}";
			CallableStatement cstmt = prepareCall(sql);
			//  1 - ID
			cstmt.registerOutParameter(1, Types.NUMERIC);
			sb.append("v,");
			//  --  Mandatory Accounting fields
			//	2 - AD_Client_ID
			cstmt.setInt(2, AD_Client_ID);
			sb.append("AD_Client_ID=").append(AD_Client_ID).append(",");
			//	3 - AD_Org_ID
			cstmt.setInt(3, AD_Org_ID);
			sb.append("AD_Org_ID=").append(AD_Org_ID).append(",");
			//  4- C_AcctSchema_ID
			cstmt.setInt(4, C_AcctSchema_ID);
			sb.append("C_AcctSchema_ID=").append(C_AcctSchema_ID).append(",");
			//  5 - Account_ID
			cstmt.setInt(5, Account_ID);
			sb.append("Account_ID=").append(Account_ID).append(", ");

			//  --  Optional
			//  6 - Base C_ValidCombination_ID
			if (base_ValidCombination_ID != 0)
			{
				cstmt.setInt (6, base_ValidCombination_ID);
				sb.append("BaseValidCombination_ID=").append(base_ValidCombination_ID).append(",");
			}
			else
			{
				cstmt.setNull(6, Types.NUMERIC);
				sb.append("BaseValidCombination_ID=").append("NULL,");
			}
			//  7 - MustBeFullyQualified
			cstmt.setString(7, "Y");
			sb.append("MustBeFullyQualified='Y',");
			//  8 - Alias
			if (Alias != null && Alias.length() > 0)
			{
				cstmt.setString(8, Alias);
				sb.append("Alias='").append(Alias).append("',");
			}
			else
			{
				cstmt.setNull(8, Types.VARCHAR);
				sb.append("Alias=NULL,");
			}
			//  9 - CreatedBy
			cstmt.setInt(9, AD_User_ID);
			sb.append("AD_User_ID=").append(AD_User_ID).append(", ");

			//	--  Optional Accounting fields
			//  10 - M_Product_ID
			if (M_Product_ID != 0)
			{
				cstmt.setInt(10, M_Product_ID);
				sb.append("M_Product_ID=").append(M_Product_ID).append(",");
			}
			else
			{
				cstmt.setNull(10, Types.NUMERIC);
				sb.append("M_Product_ID=NULL,");
			}
			//  11 - C_BPartner_ID
			if (C_BPartner_ID != 0)
			{
				cstmt.setInt(11, C_BPartner_ID);
				sb.append("C_BPartner_ID=").append(C_BPartner_ID).append(",");
			}
			else
			{
				cstmt.setNull(11, Types.NUMERIC);
				sb.append("C_BPartner_ID=NULL,");
			}
			//  12 - AD_OrgTrx_ID
			if (AD_OrgTrx_ID != 0)
			{
				cstmt.setInt(12, AD_OrgTrx_ID);
				sb.append("AD_OrgTrx_ID=").append(AD_OrgTrx_ID).append(",");
			}
			else
			{
				cstmt.setNull(12, Types.NUMERIC);
				sb.append("AD_OrgTrx_ID=NULL,");
			}
			//  13 - C_LocFrom_ID
			if (C_LocFrom_ID != 0)
			{
				cstmt.setInt(13, C_LocFrom_ID);
				sb.append("C_LocFrom_ID=").append(C_LocFrom_ID).append(",");
			}
			else
			{
				cstmt.setNull(13, Types.NUMERIC);
				sb.append("C_LocFrom=NULL,");
			}
			//  14 - C_LocTo_ID
			if (C_LocTo_ID != 0)
			{
				cstmt.setInt(14, (C_LocTo_ID));
				sb.append("C_LocTo_ID=").append(C_LocTo_ID).append(", ");
			}
			else
			{
				cstmt.setNull(14, Types.NUMERIC);
				sb.append("C_LocTo_ID=NULL, ");
			}
			//  15 - C_SalesRegion_ID
			if (C_SRegion_ID != 0)
			{
				cstmt.setInt(15, (C_SRegion_ID));
				sb.append("C_SalesRegion_ID=").append(C_SRegion_ID).append(",");
			}
			else
			{
				cstmt.setNull(15, Types.NUMERIC);
				sb.append("C_SalesRegion_ID=NULL,");
			}
			//  16 - C_Project_ID
			if (C_Project_ID != 0)
			{
				cstmt.setInt(16, (C_Project_ID));
				sb.append("C_Project_ID=").append(C_Project_ID).append(",");
			}
			else
			{
				cstmt.setNull(16, Types.NUMERIC);
				sb.append("C_Project_ID=NULL,");
			}
			//  17 - C_Campaign_ID
			if (C_Campaign_ID != 0)
			{
				cstmt.setInt(17, (C_Campaign_ID));
				sb.append("C_Campaign_ID=").append(C_Campaign_ID).append(",");
			}
			else
			{
				cstmt.setNull(17, Types.NUMERIC);
				sb.append("C_Campaign_ID=NULL,");
			}
			//  18 - C_Activity_ID
			if (C_Activity_ID != 0)
			{
				cstmt.setInt(18, (C_Activity_ID));
				sb.append("C_Activity_ID=").append(C_Activity_ID).append(",");
			}
			else
			{
				cstmt.setNull(18, Types.NUMERIC);
				sb.append("C_Activity_ID=NULL,");
			}
			//  19 - User1_ID
			if (User1_ID != 0)
			{
				cstmt.setInt(19, (User1_ID));
				sb.append("User1_ID=").append(User1_ID).append(",");
			}
			else
			{
				cstmt.setNull(19, Types.NUMERIC);
				sb.append("User1_ID=NULL,");
			}
			//  20 - User2_ID
			if (User2_ID != 0)
			{
				cstmt.setInt(20, (User2_ID));
				sb.append("User2_ID=").append(User2_ID).append(")");
			}
			else
			{
				cstmt.setNull(20, Types.NUMERIC);
				sb.append("User2_ID=NULL)");
			}

			//
			cstmt.executeUpdate();
			retValue = cstmt.getInt(1);     //  1 - ID
			cstmt.close();
			s_log.debug("getValidCombination " + sb.toString());
		}
		catch(SQLException e)
		{
			s_log.error("getValidCombination " + sb.toString(), e);
		}
		return retValue;
	}   //  getValidCombination

	/**
	 *  Insert Note
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param AD_User_ID user
	 *  @param AD_Table_ID table
	 *  @param Record_ID record
	 *  @param AD_MessageValue message
	 *  @param Text text
	 *  @param Reference subject
	 *  @return true if note was inserted
	 */
	public static boolean insertNote (int AD_Client_ID, int AD_Org_ID, int AD_User_ID,
		int AD_Table_ID, int Record_ID,
		String AD_MessageValue, String Text, String Reference)
	{
		if (AD_MessageValue == null || AD_MessageValue.length() == 0)
			throw new IllegalArgumentException("DB.insertNote - required parameter missing - AD_Message");

		//  Database limits
		if (Text == null)
			Text = "";
		if (Reference == null)
			Reference = "";
		//
		s_log.info("insertNote - " + AD_MessageValue + " - " + Reference);
		//
		StringBuffer sql = new StringBuffer ("INSERT INTO AD_Note (AD_Note_ID,");
		sql.append("AD_Client_ID,AD_Org_ID,IsActive, Created,CreatedBy,Updated,UpdatedBy, ")
			.append("AD_Message_ID,Text,Reference, ")
			.append("AD_User_ID,AD_Table_ID,Record_ID,Processed) ")
			.append("SELECT ");
		//
		String CompiereSys = "N";
		int AD_Note_ID = getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Note");
		sql.append(AD_Note_ID).append(",");
		//
		sql.append(AD_Client_ID).append(",")
			.append(AD_Org_ID).append(", 'Y',SysDate,")
			.append(AD_User_ID).append(",SysDate,0,");
		//	AD_Message_ID,Text,Reference,
		sql.append(" AD_Message_ID,").append(DB.TO_STRING(Text, 2000)).append(",")
			.append(DB.TO_STRING(Reference, 60)).append(", ");
		//	AD_User_ID,AD_Table_ID,Record_ID,Processed
		sql.append(AD_User_ID).append(",").append(AD_Table_ID).append(",")
			.append(Record_ID).append(",'N' ");
		//
		sql.append("FROM AD_Message WHERE Value='").append(AD_MessageValue).append("'");
		//  Create Entry
		int no = executeUpdate(sql.toString());

		//  AD_Message must exist, so if not created, it is probably
		//  due to non-existing AD_Message
		if (no == 0)
		{
			sql = new StringBuffer ("INSERT INTO AD_Note (AD_Note_ID,");
			sql.append("AD_Client_ID,AD_Org_ID,IsActive, Created,CreatedBy,Updated,UpdatedBy, ")
				.append("AD_Message_ID,Text,Reference, ")
				.append("AD_User_ID,AD_Table_ID,Record_ID,Processed) ")
				.append("SELECT ");
			//  - use AD_Note_ID from above
			sql.append(AD_Note_ID).append(",");
			//
			sql.append(AD_Client_ID).append(",")
				.append(AD_Org_ID).append(", 'Y',SysDate,")
				.append(AD_User_ID).append(",SysDate,0, ");
			//	AD_Message_ID,Text,Reference,
			sql.append("AD_Message_ID,").append(TO_STRING (AD_MessageValue + ": " + Text, 2000)).append(",")
				.append(TO_STRING(Reference,60)).append(", ");
			//	AD_User_ID,AD_Table_ID,Record_ID,Processed
			sql.append(AD_User_ID).append(",").append(AD_Table_ID).append(",")
				.append(Record_ID).append(",'N' ");
			//  Hardcoded AD_Message - AD_Message is in Text
			sql.append("FROM AD_Message WHERE Value='NoMessageFound'");
			//  Try again
			no = executeUpdate(sql.toString());
		}

		return no == 1;
	}   //  insertNote

	/*************************************************************************/

	/**
	 *	Print SQL Warnings.
	 *  <br>
	 *		Usage: DB.printWarning(rs.getWarnings(), "xx");
	 *  @param comment comment
	 *  @param warning warning
	 */
	public static void printWarning(String comment, SQLWarning warning)
	{
		if (comment == null || warning == null || comment.length() == 0)
			throw new IllegalArgumentException("DB.printException - required parameter missing");
		s_log.warn("SQL Warning: " + comment);
		if (warning == null)
			return;
		//
		SQLWarning warn = warning;
		while (warn != null)
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append(warn.getMessage());
			buffer.append("; State=").append(warn.getSQLState()).append("; ErrorCode=").append(warn.getErrorCode());
			s_log.warn(buffer.toString());
			warn = warn.getNextWarning();
		}
	}	//	printWarning

	/**
	 *  Create SQL TO Date String from Timestamp
	 *
	 *  @param  time Date to be converted
	 *  @param  dayOnly true if time set to 00:00:00
	 *
	 *  @return TO_DATE('2001-01-30 18:10:20',''YYYY-MM-DD HH24:MI:SS')
	 *      or  TO_DATE('2001-01-30',''YYYY-MM-DD')
	 */
	public static String TO_DATE (Timestamp time, boolean dayOnly)
	{
		if (time == null)
		{
			if (dayOnly)
				return "TRUNC(SysDate)";
			return "SysDate";
		}

		StringBuffer dateString = new StringBuffer("TO_DATE('");
		//  YYYY-MM-DD HH24:MI:SS.mmmm  JDBC Timestamp format
		String myDate = time.toString();
		if (dayOnly)
		{
			dateString.append(myDate.substring(0,10));
			dateString.append("','YYYY-MM-DD')");
		}
		else
		{
			dateString.append(myDate.substring(0, myDate.indexOf(".")));	//	cut off miliseconds
			dateString.append("','YYYY-MM-DD HH24:MI:SS')");
		}
		return dateString.toString();
	}   //  TO_DATE

	/**
	 *  Create SQL TO Date String from Timestamp
	 *  @param time time
	 *  @return TO_DATE String
	 */
	public static String TO_DATE (Timestamp time)
	{
		return TO_DATE(time, true);
	}   //  TO_DATE

	/**
	 *  Create SQL for formatted Date, Number
	 *
	 *  @see org.compiere.util.DisplayType
	 *  @see org.compiere.util.Env
	 *
	 *  @param  columnName  the column name in the SQL
	 *  @param  displayType Display Type
	 *  @param  AD_Language 6 character language setting (from Env.LANG_*)
	 *
	 *  @return TRIM(TO_CHAR(columnName,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.'''))
	 *      or TRIM(TO_CHAR(columnName,'TM9')) depending on DisplayType and Language
	 */
	public static String TO_CHAR (String columnName, int displayType, String AD_Language)
	{
		if (columnName == null || AD_Language == null || columnName.length() == 0)
			throw new IllegalArgumentException("DB.TO_CHAR - required parameter missing");

		StringBuffer retValue = new StringBuffer("TRIM(TO_CHAR(");
		retValue.append(columnName);

		//  Numbers
		if (DisplayType.isNumeric(displayType))
		{
			if (displayType == DisplayType.Amount)
				retValue.append(",'9G999G990D00'");
			else
				retValue.append(",'TM9'");
			//  TO_CHAR(GrandTotal,'9G999G990D00','NLS_NUMERIC_CHARACTERS='',.''')
			if (!Language.isDecimalPoint(AD_Language))      //  reversed
				retValue.append(",'NLS_NUMERIC_CHARACTERS='',.'''");
		}
		else if (DisplayType.isDate(displayType))
		{
			retValue.append(",'")
				.append(Language.getLanguage(AD_Language).getDBdatePattern())
				.append("'");
		}

		retValue.append("))");
		//
		return retValue.toString();
	}   //  TO_CHAR_Number


	/**
	 *  Package Strings for SQL command
	 *  @param txt  String with text
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt)
	{
		return TO_STRING (txt, 0);
	}   //  TO_STRING

	/**
	 *	Package Strings for SQL command.
	 *  <pre>
	 *		-	include in ' (single quotes)
	 *		-	replace ' with ''
	 *  </pre>
	 *  @param txt  String with text
	 *  @param maxLength    Maximum Length of content or 0 to ignore
	 *  @return escaped string for insert statement (NULL if null)
	 */
	public static String TO_STRING (String txt, int maxLength)
	{
		if (txt == null)
			return "NULL";

		//  Length
		String text = txt;
		if (maxLength != 0 && text.length() > maxLength)
			text = txt.substring(0, maxLength);

		char quote = '\'';
		//  copy characters		(wee need to look through anyway)
		StringBuffer out = new StringBuffer();
		out.append(quote);		//	'
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);
			if (c == quote)
				out.append("''");
			else
				out.append(c);
		}
		out.append(quote);		//	'
		//
		return out.toString();
	}	//	TO_STRING

}	//	DB

