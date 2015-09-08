/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.sql.*;
import java.util.*;

import org.compiere.util.*;

/**
 *	Role Model.
 *	Includes AD_User runtime info for Personal Access
 *	The class is final, so that you cannot overwrite the security rules.
 *	
 *  @author Jorg Janke
 *  @version $Id: MRole.java,v 1.21 2004/05/13 06:08:07 jjanke Exp $
 */
public final class MRole extends X_AD_Role
{
	/**
	 * 	Get Default (Client) Role
	 *	@return role
	 */
	public static MRole getDefault ()
	{
		if (s_defaultRole == null && Ini.isClient())
			return getDefault (Env.getCtx(), false);
		return s_defaultRole;
	}	//	getDefault

	/**
	 * 	Get/Set Default Role.
	 * 	@param ctx context
	 * 	@param reload if true forces load
	 *	@return role
	 *	@see org.compiere.util.DB#loadPreferences
	 */
	public static MRole getDefault (Properties ctx, boolean reload)
	{
		int AD_Role_ID = Env.getContextAsInt(ctx, "#AD_Role_ID");
		int AD_User_ID = Env.getContextAsInt(ctx, "#AD_User_ID");
		if (!Ini.isClient())	//	none for Server
			AD_User_ID = 0;
		if (reload || s_defaultRole == null)
		{
			s_defaultRole = get (ctx, AD_Role_ID, AD_User_ID, reload);
		}
		else if (s_defaultRole.getAD_Role_ID() != AD_Role_ID
			|| s_defaultRole.getAD_User_ID() != AD_User_ID)
		{
			s_defaultRole = get (ctx, AD_Role_ID, AD_User_ID, reload);
		}
		return s_defaultRole;
	}	//	getDefault
	
	/**
	 * 	Get Role for User
	 * 	@param ctx context
	 * 	@param AD_Role_ID role
	 * 	@param AD_User_ID user
	 * 	@param reload if true forces load
	 *	@return role
	 */
	public static MRole get (Properties ctx, int AD_Role_ID, int AD_User_ID, boolean reload)
	{
		s_log.info("get - AD_Role_ID=" + AD_Role_ID + ", AD_User_ID=" + AD_User_ID + ", reload=" + reload);
		String key = AD_Role_ID + "_" + AD_User_ID;
		MRole role = (MRole)s_roles.get (key);
		if (role == null || reload)
		{
			role = new MRole (ctx, AD_Role_ID);
			s_roles.put (key, role);
			if (AD_Role_ID == 0)
				role.load();			//	special Handling
			role.setAD_User_ID(AD_User_ID);
			role.loadAccess(reload);
			s_log.info("get - " + role);
		}
		return role;
	}	//	get

	/**
	 * 	Get Role (cached).
	 * 	Did not set user - so no access loaded
	 * 	@param ctx context
	 * 	@param AD_Role_ID role
	 *	@return role
	 */
	public static MRole get (Properties ctx, int AD_Role_ID)
	{
		String key = String.valueOf(AD_Role_ID);
		MRole role = (MRole)s_roles.get (key);
		if (role == null)
		{
			role = new MRole (ctx, AD_Role_ID);
			s_roles.put (key, role);
			if (AD_Role_ID == 0)
				role.load();			//	special Handling
		}
		return role;
	}	//	get
	
	/**
	 * 	Get Roles Of Client
	 *	@param ctx context
	 *	@return roles of client
	 */
	public static MRole[] getOfClient (Properties ctx)
	{
		String sql = "SELECT * FROM AD_Role WHERE AD_Client_ID=?";
		ArrayList list = new ArrayList ();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql);
			pstmt.setInt (1, Env.getAD_Client_ID(ctx));
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add (new MRole(ctx, rs));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error ("getOfClient", e);
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
		MRole[] retValue = new MRole[list.size ()];
		list.toArray (retValue);
		return retValue;
	}	//	getOfClient
	
	/**	Default Role			*/
	private static MRole			s_defaultRole = null;
	/** Role/User Cache			*/
	private static CCache			s_roles = new CCache("AD_Role", 5);
	/** Log						*/ 
	private static Logger			s_log = Logger.getCLogger(MRole.class);
	
	/**	Access SQL Read Write		*/
	public static final boolean		SQL_RW = true;
	/**	Access SQL Read Only		*/
	public static final boolean		SQL_RO = false;
	/**	Access SQL Fully Qualified	*/
	public static final boolean		SQL_FULLYQUALIFIED = true;
	/**	Access SQL Not Fully Qualified	*/
	public static final boolean		SQL_NOTQUALIFIED = false;

	
	
	/**************************************************************************
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param AD_Role_ID id
	 */
	public MRole (Properties ctx, int AD_Role_ID)
	{
		super (ctx, AD_Role_ID);
		/**	ID=0 == System Administrator
		if (AD_Role_ID == 0)
		{
			setIsCanExport (false);
			setIsCanReport (false);
			setIsManual (false);
			setIsPersonalAccess (false);
			setIsPersonalLock (false);
			setIsShowAcct (false);
			setUserLevel (USERLEVEL_Organization);
		//	setName (null);
		//	setClientList (null);
		//	setOrgList (null);
		}
		/**	System Administrator	**/		
	}	//	MRole

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MRole(Properties ctx, ResultSet rs)
	{
		super(ctx, rs);
	}	//	MRole

	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MRole[");
		sb.append(getAD_Role_ID()).append(",").append(getName())
			.append(",UserLevel=").append(getUserLevel())
			.append(",").append(getClientWhere(false))
			.append(",").append(getOrgWhere(false))
			.append("]");
		return sb.toString();
	}	//	toString

	/**
	 * 	Extended String Representation
	 *	@param ctx Properties
	 *	@return extended info
	 */
	public String toStringX (Properties ctx)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(Msg.translate(ctx, "AD_Role_ID")).append("=").append(getName())
			.append(" - ").append(Msg.translate(ctx, "IsCanExport")).append("=").append(isCanExport())
			.append(" - ").append(Msg.translate(ctx, "IsCanReport")).append("=").append(isCanReport())
			.append(Env.NL).append(Env.NL);
		//
		for (int i = 0; i < m_orgAccess.length; i++)
			sb.append(m_orgAccess[i].toStringX(ctx)).append(Env.NL);
		sb.append(Env.NL);
		//
		loadTableAccess(false);
		for (int i = 0; i < m_tableAccess.length; i++)
			sb.append(m_tableAccess[i].toStringX(ctx)).append(Env.NL);
		if (m_tableAccess.length > 0)
			sb.append(Env.NL);
		//
		loadColumnAccess(false);
		for (int i = 0; i < m_columnAccess.length; i++)
			sb.append(m_columnAccess[i].toStringX(ctx)).append(Env.NL);
		if (m_columnAccess.length > 0)
			sb.append(Env.NL);
		//
		loadRecordAccess(false);
		for (int i = 0; i < m_recordAccess.length; i++)
			sb.append(m_recordAccess[i].toStringX(ctx)).append(Env.NL);
		return sb.toString();
	}	//	toStringX


	/**
	 * 	Load Access Info
	 * 	@param reload re-load from disk
	 */
	public void loadAccess (boolean reload)
	{
		loadOrgAccess(reload);
		loadTableAccess(reload);
		loadTableInfo(reload);
		loadColumnAccess(reload);
		loadRecordAccess(reload);
		if (reload)
		{
			m_windowAccess = null;
			m_processAccess = null;
			m_taskAccess = null;
			m_workflowAccess = null;
			m_formAccess = null;
		}
	}	//	loadAccess


	/*************************************************************************/

	/** User 								*/
	private int						m_AD_User_ID = -1;	

	/**	Positive List of Organizational Access		*/	
	private MRoleOrgAccess[]		m_orgAccess = null;
	/** List of Table Access						*/
	private MTableAccess[]			m_tableAccess = null;
	/** List of Column Access				*/
	private MColumnAccess[]			m_columnAccess = null;
	/** List of Record Access				*/
	private MRecordAccess[]			m_recordAccess = null;
	/** List of Dependent Record Access		*/
	private MRecordAccess[]			m_recordDependentAccess = null;
	
	/**	Table Data Access Level	*/
	private HashMap					m_tableAccessLevel = null;
	/**	Table Name				*/
	private HashMap					m_tableName = null;
	
	/**	Window Access			*/
	private HashMap		m_windowAccess = null;
	/**	Process Access			*/
	private HashMap		m_processAccess = null;
	/**	Task Access				*/
	private HashMap		m_taskAccess = null;
	/**	Workflow Access			*/
	private HashMap		m_workflowAccess = null;
	/**	Form Access				*/
	private HashMap		m_formAccess = null;

	/**
	 * 	Set Logged in user
	 *	@param AD_User_ID user requesting info
	 */
	public void setAD_User_ID(int AD_User_ID)
	{
		m_AD_User_ID = AD_User_ID;
	}	//	setAD_User_ID

	/**
	 * 	Get Logged in user
	 *	@return AD_User_ID user requesting info
	 */
	public int getAD_User_ID()
	{
		return m_AD_User_ID;
	}	//	getAD_User_ID
	
	/**
	 * 	Load Org Access
	 *	@param reload reload
	 */
	private void loadOrgAccess (boolean reload)
	{
		if (!(reload || m_orgAccess == null))
			return;
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Role_OrgAccess "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MRoleOrgAccess(getCtx(), rs)); 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadOrgAccess", e);
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
		m_orgAccess = new MRoleOrgAccess[list.size()];
		list.toArray(m_orgAccess); 
		log.debug("loadOrgAccess - #" + m_orgAccess.length + (reload ? " - reload" : "")); 
		if (Ini.isClient())
		{
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < m_orgAccess.length; i++)
			{
				if (i > 0)
					sb.append(",");
				sb.append(m_orgAccess[i].getAD_Org_ID());
			}
			Env.setContext(Env.getCtx(), "#User_Org", sb.toString());
		}
	}	//	loadOrgAccess
	
	/**
	 * 	Load Table Access
	 *	@param reload reload
	 */
	private void loadTableAccess(boolean reload)
	{
		if (m_tableAccess != null && !reload)
			return;
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Table_Access "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MTableAccess(getCtx(), rs)); 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadTableAccess", e);
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
		m_tableAccess = new MTableAccess[list.size()];
		list.toArray(m_tableAccess); 
		log.debug("loadTableAccess - #" + m_tableAccess.length); 
	}	//	loadTableAccess

	/**
	 * 	Load Table Access and Name
	 *	@param reload reload
	 */
	private void loadTableInfo (boolean reload)
	{
		if (m_tableAccessLevel != null && m_tableName != null && !reload)
			return;
		m_tableAccessLevel = new HashMap(300);
		m_tableName = new HashMap(300);
		PreparedStatement pstmt = null;
		String sql = "SELECT AD_Table_ID, AccessLevel, TableName "
			+ "FROM AD_Table WHERE IsActive='Y'";
		try
		{
			pstmt = DB.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				Integer ii = new Integer(rs.getInt(1));
				m_tableAccessLevel.put(ii, rs.getString(2));
				m_tableName.put(rs.getString(3), ii);
			} 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadTableAccessLevel", e);
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
		log.debug("loadTableAccessLevel - #" + m_tableAccessLevel.size()); 
	}	//	loadTableAccessLevel

	
	/**
	 * 	Load Column Access
	 *	@param reload reload
	 */
	private void loadColumnAccess(boolean reload)
	{
		if (m_columnAccess != null && !reload)
			return;
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Column_Access "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MColumnAccess(getCtx(), rs)); 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadColumnAccess", e);
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
		m_columnAccess = new MColumnAccess[list.size()];
		list.toArray(m_columnAccess); 
		log.debug("loadColumnAccess - #" + m_columnAccess.length); 
	}	//	loadColumnAccess
	
	/**
	 * 	Load Record Access
	 *	@param reload reload
	 */
	private void loadRecordAccess(boolean reload)
	{
		if (!(reload || m_recordAccess == null || m_recordDependentAccess == null))
			return;
		ArrayList list = new ArrayList();
		ArrayList dependent = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Record_Access "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				MRecordAccess ra = new MRecordAccess(getCtx(), rs);
				list.add(ra);
				if (ra.isDependentEntities())
					dependent.add(ra);
			} 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadRecordAccess", e);
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
		m_recordAccess = new MRecordAccess[list.size()];
		list.toArray(m_recordAccess);
		m_recordDependentAccess = new MRecordAccess[dependent.size()];
		dependent.toArray(m_recordDependentAccess);
		log.debug("loadRecordAccess - #" + m_recordAccess.length + " - Dependent #" + m_recordDependentAccess.length); 
	}	//	loadRecordAccess


	/**************************************************************************
	 * 	Get Client Where Clause Value 
	 * 	@param rw read write
	 * 	@return "AD_Client_ID=0" or "AD_Client_ID IN(0,1)"
	 */
	public String getClientWhere (boolean rw)
	{
		loadOrgAccess (false);
		//	Unique Strings
		HashSet set = new HashSet();
		if (!rw)
			set.add("0");
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
			set.add(String.valueOf(m_orgAccess[i].getAD_Client_ID()));
		//
		StringBuffer sb = new StringBuffer();
		Iterator it = set.iterator();
		boolean oneOnly = true;
		while (it.hasNext())
		{
			if (sb.length() > 0)
			{
				sb.append(",");
				oneOnly = false;
			}
			sb.append(it.next());
		}
		if (oneOnly)
		{
			if (sb.length() > 0)
				return "AD_Client_ID=" + sb.toString();
			else
			{
				log.error("getClientWhere - No Access Org records");
				return "AD_Client_ID=-1";	//	No Access Record
			}
		}
		return "AD_Client_ID IN(" + sb.toString() + ")";
	}	//	getClientWhereValue
	
	/**
	 * 	Access to Client
	 *	@param AD_Client_ID client
	 *	@param rw read write access
	 *	@return true if access
	 */
	public boolean isClientAccess(int AD_Client_ID, boolean rw)
	{
		if (AD_Client_ID == 0 && !rw)	//	can always read System
			return true;
		loadOrgAccess(false);
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
		{
			if (m_orgAccess[i].getAD_Client_ID() == AD_Client_ID)
				return true;
		}
		return false;
	}	//	isClientAccess
	
	/**
	 * 	Get Org Where Clause Value 
	 * 	@param rw read write
	 * 	@return "AD_Org_ID=0" or "AD_Org_ID IN(0,1)"
	 */
	public String getOrgWhere (boolean rw)
	{
		loadOrgAccess(false);
		//	Unique Strings
		HashSet set = new HashSet();
		if (!rw)
			set.add("0");
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
			set.add(String.valueOf(m_orgAccess[i].getAD_Org_ID()));
		//
		StringBuffer sb = new StringBuffer();
		Iterator it = set.iterator();
		boolean oneOnly = true;
		while (it.hasNext())
		{
			if (sb.length() > 0)
			{
				sb.append(",");
				oneOnly = false;
			}
			sb.append(it.next());
		}
		if (oneOnly)
		{
			if (sb.length() > 0)
				return "AD_Org_ID=" + sb.toString();
			else
			{
				log.error("getOrgWhere - No Access Org records");
				return "AD_Org_ID=-1";	//	No Access Record
			}
		}		
		return "AD_Org_ID IN(" + sb.toString() + ")";
	}	//	getOrgWhereValue
	
	/**
	 * 	Access to Org
	 *	@param AD_Org_ID org
	 *	@param rw read write access
	 *	@return true if access
	 */
	public boolean isOrgAccess(int AD_Org_ID, boolean rw)
	{
		if (AD_Org_ID == 0 && !rw)		//	can always read common org
			return true;
		loadOrgAccess(false);
		
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
		{
			if (m_orgAccess[i].getAD_Org_ID() == AD_Org_ID)
				return true;
		}
		return false;
	}	//	isOrgAccess


	/**
	 * 	Can Report on table
	 *	@param AD_Table_ID table
	 *	@return true if access
	 */
	public boolean isCanReport (int AD_Table_ID)
	{
		if (!isCanReport())						//	Role Level block
		{
			log.warn ("isCanReport - Role denied");
			return false;
		}
		if (!isTableAccess(AD_Table_ID, true))	//	No R/O Access to Table
			return false;
		//
		boolean canReport = true;
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (!MTableAccess.ACCESSTYPERULE_Reporting.equals(m_tableAccess[i].getAccessTypeRule()))
				continue;
			if (m_tableAccess[i].isExclude())		//	Exclude
			{
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					canReport = m_tableAccess[i].isCanReport();
					log.debug ("isCanReport - Exclude " + AD_Table_ID + " - " + canReport);
					return canReport;
				}
			}
			else									//	Include
			{
				canReport = false;
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					canReport = m_tableAccess[i].isCanReport();
					log.debug ("isCanReport - Include " + AD_Table_ID + " - " + canReport);
					return canReport;
				}
			}
		}	//	for all Table Access
		log.debug ("isCanReport - " + AD_Table_ID + " - " + canReport);
		return canReport;
	}	//	isCanReport
	
	/**
	 * 	Can Export Table
	 *	@param AD_Table_ID
	 *	@return true if access
	 */
	public boolean isCanExport (int AD_Table_ID)
	{
		if (!isCanExport())						//	Role Level block
		{
			log.warn ("isCanExport - Role denied");
			return false;
		}
		if (!isTableAccess(AD_Table_ID, true))	//	No R/O Access to Table
			return false;
		if (!isCanReport (AD_Table_ID))			//	We cannot Export if we cannot report
			return false;
		//
		boolean canExport = true;
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (!MTableAccess.ACCESSTYPERULE_Exporting.equals(m_tableAccess[i].getAccessTypeRule()))
				continue;
			if (m_tableAccess[i].isExclude())		//	Exclude
			{
				canExport = m_tableAccess[i].isCanExport();
				log.debug ("isCanExport - Exclude " + AD_Table_ID + " - " + canExport);
				return canExport;
			}
			else									//	Include
			{
				canExport = false;
				canExport = m_tableAccess[i].isCanExport();
				log.debug ("isCanExport - Include " + AD_Table_ID + " - " + canExport);
				return canExport;
			}
		}	//	for all Table Access
		log.debug ("isCanExport - " + AD_Table_ID + " - " + canExport);
		return canExport;
	}	//	isCanExport

	/**
	 * 	Access to Table
	 *	@param AD_Table_ID table
	 *	@param ro check read only access otherwise read write access level
	 *	@return has RO/RW access to table
	 */
	public boolean isTableAccess (int AD_Table_ID, boolean ro)
	{
		if (!isTableAccessLevel (AD_Table_ID, ro))	//	Role Based Access
			return false;
		loadTableAccess(false);
		//
		boolean hasAccess = true;	//	assuming exclusive rule
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (!MTableAccess.ACCESSTYPERULE_Accessing.equals(m_tableAccess[i].getAccessTypeRule()))
				continue;
			if (m_tableAccess[i].isExclude())		//	Exclude
			//	If you Exclude Access to a table and select Read Only, 
			//	you can only read data (otherwise no access).
			{
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (ro)
						hasAccess = m_tableAccess[i].isReadOnly();
					else
						hasAccess = false;
					log.debug("isTableAccess - Exclude AD_Table_ID=" + AD_Table_ID 
						+ " (ro="  + ro + ",TableAccessRO=" + m_tableAccess[i].isReadOnly() + ") = " + hasAccess);
					return hasAccess;
				}
			}
			else								//	Include
			//	If you Include Access to a table and select Read Only, 
			//	you can only read data (otherwise full access).
			{
				hasAccess = false;
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (!ro)	//	rw only if not r/o
						hasAccess = !m_tableAccess[i].isReadOnly();
					else
						hasAccess = true;
					log.debug("isTableAccess - Include AD_Table_ID=" + AD_Table_ID 
						+ " (ro="  + ro + ",TableAccessRO=" + m_tableAccess[i].isReadOnly() + ") = " + hasAccess);
					return hasAccess;
				}
			}
		}	//	for all Table Access
		if (!hasAccess)
			log.debug("isTableAccess - AD_Table_ID=" + AD_Table_ID 
				+ "(ro="  + ro + ") = " + hasAccess);
		return hasAccess;
	}	//	isTableAccess

	/**
	 * 	Access to Table based on Role User Level Table Access Level
	 *	@param AD_Table_ID table
	 *	@param ro check read only access otherwise read write access level
	 *	@return has RO/RW access to table
	 */
	public boolean isTableAccessLevel (int AD_Table_ID, boolean ro)
	{
		if (ro)				//	role can always read
			return true;
		//
		loadTableInfo(false);
		//	AccessLevel
		//		1 = Org - 2 = Client - 4 = System
		//		3 = Org+Client - 6 = Client+System - 7 = All
		String accessLevel = (String)m_tableAccessLevel.get(new Integer(AD_Table_ID));
		if (accessLevel == null)
		{
			log.debug("isTableAccessLevel - NO - No AccessLevel - AD_Table_ID=" + AD_Table_ID);
			return false;
		}
		//	Access to all User Levels
		if (accessLevel.equals(X_AD_Table.ACCESSLEVEL_All))
			return true;
		//	User Level = SCO
		String userLevel = getUserLevel();
		//	
		if (userLevel.charAt(0) == 'S'
			&& (accessLevel.equals(X_AD_Table.ACCESSLEVEL_SystemOnly) 
				|| accessLevel.equals(X_AD_Table.ACCESSLEVEL_SystemPlusClient)))
			return true;
		if (userLevel.charAt(1) == 'C'
			&& (accessLevel.equals(X_AD_Table.ACCESSLEVEL_ClientOnly) 
				|| accessLevel.equals(X_AD_Table.ACCESSLEVEL_SystemPlusClient)))
			return true;
		if (userLevel.charAt(2) == 'O'
			&& (accessLevel.equals(X_AD_Table.ACCESSLEVEL_Organization) 
				|| accessLevel.equals(X_AD_Table.ACCESSLEVEL_ClientPlusOrganization)))
			return true;
		log.debug("isTableAccessLevel - NO - AD_Table_ID=" + AD_Table_ID 
			+ ", UserLebel=" + userLevel + ", AccessLevel=" + accessLevel);
		return false;
	}	//	isTableAccessLevel


	/**
	 * 	Access to Column
	 *	@param AD_Table_ID table
	 *	@param AD_Column_ID column
	 *	@param ro read only
	 *	@return true if access
	 */
	public boolean isColumnAccess (int AD_Table_ID, int AD_Column_ID, boolean ro)
	{
		if (!isTableAccess(AD_Table_ID, ro))		//	No Access to Table		
			return false;
		loadColumnAccess(false);
		
		boolean retValue = true;		//	assuming exclusive
		for (int i = 0; i < m_columnAccess.length; i++)
		{
			if (m_columnAccess[i].isExclude())		//	Exclude
			//	If you Exclude Access to a column and select Read Only, 
			//	you can only read data (otherwise no access).
			{
				if (m_columnAccess[i].getAD_Table_ID() == AD_Table_ID 
					&& m_columnAccess[i].getAD_Column_ID() == AD_Column_ID)
				{
					if (ro)		//	just R/O Access requested
						retValue = m_columnAccess[i].isReadOnly();
					else
						retValue = false;
					if (!retValue)
						log.debug("isColumnAccess - Exclude AD_Table_ID=" + AD_Table_ID + ", AD_Column_ID=" + AD_Column_ID 
							+ " (ro="  + ro + ",ColumnAccessRO=" + m_columnAccess[i].isReadOnly() + ") = " + retValue);
					return retValue;
				}
			}
			else								//	Include
			//	If you Include Access to a column and select Read Only, 
			//	you can only read data (otherwise full access).
			{
				if (m_columnAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					retValue = false;
					if (m_columnAccess[i].getAD_Column_ID() == AD_Column_ID)
					{
						if (!ro)	//	rw only if not r/o
							retValue = !m_columnAccess[i].isReadOnly();
						else
							retValue = true;
						if (!retValue)
							log.debug("isColumnAccess - Include AD_Table_ID=" + AD_Table_ID + ", AD_Column_ID=" + AD_Column_ID 
								+ " (ro="  + ro + ",ColumnAccessRO=" + m_columnAccess[i].isReadOnly() + ") = " + retValue);
						return retValue;
					}
				}	//	same table
			}	//	include
		}	//	for all Table Access
		if (!retValue)
			log.debug("isColumnAccess - AD_Table_ID=" + AD_Table_ID + ", AD_Column_ID=" + AD_Column_ID 
				+ " (ro="  + ro + ") = " + retValue);
		return retValue;
	}	//	isColumnAccess

	/**
	 *	Access to Record
	 *	@param AD_Table_ID table
	 *	@param Record_ID
	 *	@param ro
	 *	 * @return boolean
	 */
	public boolean isRecordAccess (int AD_Table_ID, int Record_ID, boolean ro)
	{
		if (!isTableAccess(AD_Table_ID, ro))		//	No Access to Table
			return false;
		loadRecordAccess(false);
		
		boolean negativeList = true;
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (m_recordAccess[i].isExclude())		//	Exclude
			//	If you Exclude Access to a column and select Read Only, 
			//	you can only read data (otherwise no access).
			{
				if (m_recordAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (ro)
						return m_tableAccess[i].isReadOnly();
					else
						return false;
				}
			}
			else								//	Include
			//	If you Include Access to a column and select Read Only, 
			//	you can only read data (otherwise full access).
			{
				negativeList = false;
				if (m_recordAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (!ro)	//	rw only if not r/o
						return !m_tableAccess[i].isReadOnly();
					else
						return true;
				}
			}
		}	//	for all Table Access
		return negativeList;
	}	//	isRecordAccess

	/**
	 * 	Get Window Access
	 *	@param AD_Window_ID window
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getWindowAccess (int AD_Window_ID)
	{
		if (m_windowAccess == null)
		{
			m_windowAccess = new HashMap(100);
			String sql = "SELECT AD_Window_ID, IsReadWrite FROM AD_Window_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_windowAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getWindowAccess", e);
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
			log.debug("getWindowAccess #" + m_windowAccess.size());
		}	//	reload
		Boolean retValue = (Boolean)m_windowAccess.get(new Integer(AD_Window_ID));
	//	log.debug("getWindowAccess - AD_Window_ID=" + AD_Window_ID + " - " + retValue);
		return retValue;
	}	//	getWindowAccess

	/**
	 * 	Get Process Access
	 *	@param AD_Process_ID process
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getProcessAccess (int AD_Process_ID)
	{
		if (m_processAccess == null)
		{
			m_processAccess = new HashMap(50);
			String sql = "SELECT AD_Process_ID, IsReadWrite FROM AD_Process_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_processAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getProcessAccess", e);
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
		}	//	reload
		return (Boolean)m_processAccess.get(new Integer(AD_Process_ID));
	}	//	getProcessAccess

	/**
	 * 	Get Task Access
	 *	@param AD_Task_ID task
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getTaskAccess (int AD_Task_ID)
	{
		if (m_taskAccess == null)
		{
			m_taskAccess = new HashMap(10);
			String sql = "SELECT AD_Task_ID, IsReadWrite FROM AD_Task_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_taskAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getTaskAccess", e);
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
		}	//	reload
		return (Boolean)m_taskAccess.get(new Integer(AD_Task_ID));
	}	//	getTaskAccess

	/**
	 * 	Get Form Access
	 *	@param AD_Form_ID form
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getFormAccess (int AD_Form_ID)
	{
		if (m_formAccess == null)
		{
			m_formAccess = new HashMap(20);
			String sql = "SELECT AD_Form_ID, IsReadWrite FROM AD_Form_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_formAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getFormAccess", e);
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
		}	//	reload
		return (Boolean)m_formAccess.get(new Integer(AD_Form_ID));
	}	//	getTaskAccess

	/**
	 * 	Get Workflow Access
	 *	@param AD_Workflow_ID workflow
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getWorkflowAccess (int AD_Workflow_ID)
	{
		if (m_workflowAccess == null)
		{
			m_workflowAccess = new HashMap(20);
			String sql = "SELECT AD_Workflow_ID, IsReadWrite FROM AD_Workflow_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_workflowAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getWorkflowAccess", e);
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
		}	//	reload
		return (Boolean)m_workflowAccess.get(new Integer(AD_Workflow_ID));
	}	//	getTaskAccess

	/*************************************************************************

	/**
	 *	Appends where clause to SQL statement for Table
	 *
	 *	@param SQL			existing SQL statement
	 *	@param TableNameIn	Table Name or list of table names AAA, BBB or AAA a, BBB b
	 *	@param fullyQualified	fullyQualified names
	 *	@param rw			if false, includes System Data
	 *	@return				updated SQL statement
	 */
	public String addAccessSQL (String SQL, String TableNameIn, 
		boolean fullyQualified, boolean rw)
	{
		StringBuffer retSQL = new StringBuffer();

		//	Cut off last ORDER BY clause
		String orderBy = "";
		int posOrder = SQL.lastIndexOf(" ORDER BY ");
		if (posOrder != -1)
		{
			orderBy = SQL.substring(posOrder);
			retSQL.append(SQL.substring(0, posOrder));
		}
		else
			retSQL.append(SQL);

		//	Parse SQL
		AccessSqlParser asp = new AccessSqlParser(retSQL.toString());
		AccessSqlParser.TableInfo[] ti = asp.getTableInfo(asp.getMainSqlIndex()); 

		//  Do we have to add WHERE or AND
		if (asp.getMainSql().indexOf(" WHERE ") == -1)
			retSQL.append(" WHERE ");
		else
			retSQL.append(" AND ");

		//	Use First Table
		String tableName = "";
		if (ti.length > 0)
		{
			tableName = ti[0].getSynonym();
			if (tableName.length() == 0)
				tableName = ti[0].getTableName();
		}
		if (!tableName.equals(TableNameIn) && TableNameIn != null)
		{
			String msg = "addAccessSQL - TableName not correctly parsed - TableNameIn=" 
				+ TableNameIn + " - " + asp;
			if (ti.length > 0)
				msg += " - #1 " + ti[0]; 
			msg += "\n = " + SQL;
			log.error(msg);
			Trace.printStack();
			tableName = TableNameIn;
		}

		//	Client Access
		if (fullyQualified)
			retSQL.append(tableName).append(".");
		retSQL.append(getClientWhere(rw));
		
		//	Org Access
		retSQL.append(" AND ");
		if (fullyQualified)
			retSQL.append(tableName).append(".");
		retSQL.append(getOrgWhere(rw));

			
		//	** Data Access	**
		for (int i = 0; i < ti.length; i++)
		{
			String TableName = ti[i].getTableName();
			int AD_Table_ID = getAD_Table_ID (TableName);
			//	Data Table Access
			if (AD_Table_ID != 0 && !isTableAccess(AD_Table_ID, !rw))
			{
				retSQL.append(" AND 1=3");	//	prevent access at all
				log.debug("addAccessSQL - no access to AD_Table_ID=" + AD_Table_ID 
					+ " - " + TableName + " - " + retSQL);
				break;	//	no need to check further 
			}
			
			//	Data Column Access
	
	
		
			//	Data Record Access
			String keyColumnName = "";
			if (fullyQualified)
			{
				keyColumnName = ti[i].getSynonym();	//	table synonym
				if (keyColumnName.length() == 0)
					keyColumnName = TableName;
				keyColumnName += ".";
			}
			keyColumnName += TableName + "_ID";	//	derived from table
	
		//	log.debug("addAccessSQL - " + TableName + "(" + AD_Table_ID + ") " + keyColumnName);
			String recordWhere = getRecordWhere (AD_Table_ID, keyColumnName);
			if (recordWhere.length() > 0)
			{
				retSQL.append(" AND ").append(recordWhere);
				if (Log.isTraceLevel(9))
					log.debug("addAccessSQL - adding record access - " + recordWhere);
			}
		}	//	for all table info
		
		//	Dependent Records (only for main SQL)
		String mainSql = asp.getMainSql();
		loadRecordAccess(false);
		for (int i = 0; i < m_recordDependentAccess.length; i++)
		{
			String columnName = m_recordDependentAccess[i].getKeyColumnName(asp.getTableInfo(asp.getMainSqlIndex()) );
			if (columnName == null)
				continue;	//	no key column
			int posColumn = mainSql.indexOf(columnName);
			if (posColumn == -1)
				continue;
			//	we found the column name - make sure it's a clumn name
			char charCheck = mainSql.charAt(posColumn-1);	//	before
			if (!(charCheck == ',' || charCheck == '.' || charCheck == ' ' || charCheck == '('))
				continue;
			charCheck = mainSql.charAt(posColumn+columnName.length());	//	after
			if (!(charCheck == ',' || charCheck == ' ' || charCheck == ')'))
				continue;
				
			//	*** we found the column in the main query
			log.debug("DEPENDENT  " + columnName + " - " + m_recordDependentAccess[i]);
			StringBuffer where = new StringBuffer();
			where.append(getDependentRecordWhereColumn (mainSql, columnName))
				.append((m_recordDependentAccess[i].isExclude() ? "<>" : "="))
				.append(m_recordDependentAccess[i].getRecord_ID());
			if (Log.isTraceLevel(9))
				log.debug("addAccessSQL - adding dependent access - " + where);
			retSQL.append(" AND ").append(where);			
		}	//	for all dependent records
		
		//
		retSQL.append(orderBy);
		if (Log.isTraceLevel(10))
			log.debug("addAccessSQL - " + retSQL);
		return retSQL.toString();
	}	//	addAccessSQL

	/**
	 * 	Get Dependent Record Where clause
	 *	@param mainSql sql to examine
	 *	@param columnName columnName
	 *	@return where clause column "x.columnName"
	 */
	private String getDependentRecordWhereColumn (String mainSql, String columnName)
	{
		String retValue = columnName;	//	if nothing else found
		int index = mainSql.indexOf(columnName);
		//	see if there are table synonym
		int offset = index - 1;
		char c = mainSql.charAt(offset);
		if (c == '.')
		{
			StringBuffer sb = new StringBuffer();
			while (c != ' ' && c != ',' && c != '(')	//	delimeter
			{
				sb.insert(0, c);
				c = mainSql.charAt(--offset);
			}
			sb.append(columnName);
			return sb.toString();
		}
		return retValue;
	}	//	getDependentRecordWhereColumn



	/**
	 *	UPADATE - Can I Update the record
	 *  Access error info (AccessTableNoUpdate) is saved in the log
	 * 
	 *   * @param AD_Client_ID comntext to derive client/org/user level
	 * @param AD_Org_ID number of the current window to retrieve context
	 * @param AD_Table_ID int
	 * @param createError boolean
	@return	true if you can update
	@see org.compiere.model.MTable#save(boolean)
	 *
	 *   */
	public boolean canUpdate (int AD_Client_ID, int AD_Org_ID, 
		int AD_Table_ID, boolean createError)
	{
		String userLevel = getUserLevel();	//	Format 'SCO'

		if (userLevel.indexOf("S") != -1)		//	System can change anything
			return true;

		boolean	retValue = true;
		String whatMissing = "";

		//	System == Client=0 & Org=0
		if (AD_Client_ID == 0 && AD_Org_ID == 0
			&& userLevel.charAt(0) != 'S')
		{
			retValue = false;
			whatMissing += "S";
		}

		//	Client == Client!=0 & Org=0
		else if (AD_Client_ID != 0 && AD_Org_ID == 0
			&& userLevel.charAt(1) != 'C')
		{
			retValue = false;
			whatMissing += "C";
		}

		//	Organization == Client!=0 & Org!=0
		else if (AD_Client_ID != 0 && AD_Org_ID != 0
			&& userLevel.charAt(2) != 'O')
		{
			retValue = false;
			whatMissing += "O";
		}

		//	Data Access
		//	get Table.IsSecurityEnabled
		//	if yes: get UserAcess info => Where Table.TableKey in (List)
		//									and Access=r/w

		if (!retValue && createError)
		{
			Log.saveError("AccessTableNoUpdate",
				"AD_Client_ID=" + AD_Client_ID + ", AD_Org_ID=" + AD_Org_ID + ", UserLevel=" + userLevel
				+ " => missing=" + whatMissing);
			log.error ("canUpdate - " + toString());
		}
		return retValue;
	}	//	canUpdate

	/**
	 *	VIEW - Can I view record in Table with given TableLevel
	 *  <code>
	 *	TableLevel			S__ 100		4	System info
	 *						SCO	111		7	System shared info
	 *						SC_ 110		6	System/Client info
	 *						_CO	011		3	Client shared info
	 *						_C_	011		2	Client shared info
	 *						__O	001		1	Organization info
	 *  </code>
	 * 
	 * 	@param ctx	context
	 *	@param TableLevel	AccessLevel
	 *	@return	true/false
	 *  Access error info (AccessTableNoUpdate, AccessTableNoView) is saved in the log
	@see org.compiere.model.MTabVO#loadTabDetails (MTabVO, ResultSet)
	 *   */
	public boolean canView(Properties ctx, String TableLevel)
	{
		String userLevel = getUserLevel();	//	Format 'SCO'

		boolean retValue = true;

		//	7 - All
		if (X_AD_Table.ACCESSLEVEL_All.equals(TableLevel))
			retValue = true;
			 
		//	4 - System data requires S
		else if (X_AD_Table.ACCESSLEVEL_SystemOnly.equals(TableLevel) 
			&& userLevel.charAt(0) != 'S')
			retValue = false;

		//	2 - Client data requires C
		else if (X_AD_Table.ACCESSLEVEL_ClientOnly.equals(TableLevel) 
			&& userLevel.charAt(1) != 'C')
			retValue = false;

		//	1 - Organization data requires O
		else if (X_AD_Table.ACCESSLEVEL_Organization.equals(TableLevel) 
			&& userLevel.charAt(2) != 'O')
			retValue = false;

		//	3 - Client Shared requires C or O
		else if (X_AD_Table.ACCESSLEVEL_ClientPlusOrganization.equals(TableLevel)
			&& (!(userLevel.charAt(1) == 'C' || userLevel.charAt(2) == 'O')) )
				retValue = false;

		//	6 - System/Client requires S or C
		else if (X_AD_Table.ACCESSLEVEL_SystemPlusClient.equals(TableLevel)
			&& (!(userLevel.charAt(0) == 'S' || userLevel.charAt(1) == 'C')) )
			retValue = false;

		if (retValue)
			return retValue;

		//  Notification
		/**
		if (forInsert)
			Log.saveError("AccessTableNoUpdate",
				"(Required=" + TableLevel + "("
				+ getTableLevelString(Env.getAD_Language(ctx), TableLevel)
				+ ") != UserLevel=" + userLevel);
		else
		**/
			Log.saveError("AccessTableNoView",
				"Required=" + TableLevel + "("
				+ getTableLevelString(Env.getAD_Language(ctx), TableLevel)
				+ ") != UserLevel=" + userLevel);
		log.error ("canView - " + toString());
		return retValue;
	}	//	canView


	/**
	 *	Returns clear text String of TableLevel
	 *  @param AD_Language language
	 *  @param TableLevel level
	 *  @return info
	 */
	private String getTableLevelString (String AD_Language, String TableLevel)
	{
		String level = TableLevel + "??";
		if (TableLevel.equals("1"))
			level = "AccessOrg";
		else if (TableLevel.equals("2"))
			level = "AccessClient";
		else if (TableLevel.equals("3"))
			level = "AccessClientOrg";
		else if (TableLevel.equals("4"))
			level = "AccessSystem";
		else if (TableLevel.equals("6"))
			level = "AccessSystemClient";
		else if (TableLevel.equals("7"))
			level = "AccessShared";

		return Msg.getMsg(AD_Language, level);
	}	//	getTableLevelString

	/**
	 * 	Get Table ID from name
	 *	@param tableName table name
	 *	@return AD_Table_ID or 0
	 */
	private int getAD_Table_ID (String tableName)
	{
		loadTableInfo(false);
		Integer ii = (Integer)m_tableName.get(tableName);
		if (ii != null)
			return ii.intValue();
	//	log.warn("getAD_Table_ID - not found (" + tableName + ")");
		return 0;
	}	//	getAD_Table_ID

	/**
	 * 	Return Where clause for Record Access
	 *	@param AD_Table_ID table
	 *	@param keyColumnName (fully qualified) key column name
	 *	@return where clause or ""
	 */
	private String getRecordWhere (int AD_Table_ID, String keyColumnName)
	{
		loadRecordAccess(false);
		//
		StringBuffer sb = new StringBuffer();
		//	Role Access
		for (int i = 0; i < m_recordAccess.length; i++)
		{
			if (m_recordAccess[i].getAD_Table_ID() == AD_Table_ID)
			{
				if (sb.length() > 0)
					sb.append(" AND ");
				sb.append(keyColumnName)
					.append((m_recordAccess[i].isExclude() ? "<>" : "="))
					.append(m_recordAccess[i].getRecord_ID());
			}
		}	//	for all Table Access
		
		//	Don't ignore Privacy Access
		if (!isPersonalAccess())
		{
			String lockedIDs = MPrivateAccess.getLockedRecordWhere(AD_Table_ID, m_AD_User_ID);
			if (lockedIDs != null)
			{
				if (sb.length() > 0)
					sb.append(" AND ");
				sb.append(keyColumnName).append(lockedIDs);
			}
		}
		//
		return sb.toString();
	}	//	getRecordWhere

}	//	MRole
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.sql.*;
import java.util.*;

import org.compiere.util.*;

/**
 *	Role Model.
 *	Includes AD_User runtime info for Personal Access
 *	The class is final, so that you cannot overwrite the security rules.
 *	
 *  @author Jorg Janke
 *  @version $Id: MRole.java,v 1.27 2004/09/10 02:53:28 jjanke Exp $
 */
public final class MRole extends X_AD_Role
{
	/**
	 * 	Get Default (Client) Role
	 *	@return role
	 */
	public static MRole getDefault ()
	{
		if (s_defaultRole == null && Ini.isClient())
			return getDefault (Env.getCtx(), false);
		return s_defaultRole;
	}	//	getDefault

	/**
	 * 	Get/Set Default Role.
	 * 	@param ctx context
	 * 	@param reload if true forces load
	 *	@return role
	 *	@see org.compiere.util.DB#loadPreferences
	 */
	public static MRole getDefault (Properties ctx, boolean reload)
	{
		int AD_Role_ID = Env.getContextAsInt(ctx, "#AD_Role_ID");
		int AD_User_ID = Env.getContextAsInt(ctx, "#AD_User_ID");
		if (!Ini.isClient())	//	none for Server
			AD_User_ID = 0;
		if (reload || s_defaultRole == null)
		{
			s_defaultRole = get (ctx, AD_Role_ID, AD_User_ID, reload);
		}
		else if (s_defaultRole.getAD_Role_ID() != AD_Role_ID
			|| s_defaultRole.getAD_User_ID() != AD_User_ID)
		{
			s_defaultRole = get (ctx, AD_Role_ID, AD_User_ID, reload);
		}
		return s_defaultRole;
	}	//	getDefault
	
	/**
	 * 	Get Role for User
	 * 	@param ctx context
	 * 	@param AD_Role_ID role
	 * 	@param AD_User_ID user
	 * 	@param reload if true forces load
	 *	@return role
	 */
	public static MRole get (Properties ctx, int AD_Role_ID, int AD_User_ID, boolean reload)
	{
		s_log.info("get - AD_Role_ID=" + AD_Role_ID + ", AD_User_ID=" + AD_User_ID + ", reload=" + reload);
		String key = AD_Role_ID + "_" + AD_User_ID;
		MRole role = (MRole)s_roles.get (key);
		if (role == null || reload)
		{
			role = new MRole (ctx, AD_Role_ID);
			s_roles.put (key, role);
			if (AD_Role_ID == 0)
				role.load();			//	special Handling
			role.setAD_User_ID(AD_User_ID);
			role.loadAccess(reload);
			s_log.info("get - " + role);
		}
		return role;
	}	//	get

	/**
	 * 	Get Role (cached).
	 * 	Did not set user - so no access loaded
	 * 	@param ctx context
	 * 	@param AD_Role_ID role
	 *	@return role
	 */
	public static MRole get (Properties ctx, int AD_Role_ID)
	{
		String key = String.valueOf(AD_Role_ID);
		MRole role = (MRole)s_roles.get (key);
		if (role == null)
		{
			role = new MRole (ctx, AD_Role_ID);
			s_roles.put (key, role);
			if (AD_Role_ID == 0)	//	System Role
				role.load();		//	special Handling
		}
		return role;
	}	//	get
	
	/**
	 * 	Get Roles Of Client
	 *	@param ctx context
	 *	@return roles of client
	 */
	public static MRole[] getOfClient (Properties ctx)
	{
		String sql = "SELECT * FROM AD_Role WHERE AD_Client_ID=?";
		ArrayList list = new ArrayList ();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql);
			pstmt.setInt (1, Env.getAD_Client_ID(ctx));
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add (new MRole(ctx, rs));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error ("getOfClient", e);
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
		MRole[] retValue = new MRole[list.size ()];
		list.toArray (retValue);
		return retValue;
	}	//	getOfClient
	
	/**	Default Role			*/
	private static MRole			s_defaultRole = null;
	/** Role/User Cache			*/
	private static CCache			s_roles = new CCache("AD_Role", 5);
	/** Log						*/ 
	private static Logger			s_log = Logger.getCLogger(MRole.class);
	
	/**	Access SQL Read Write		*/
	public static final boolean		SQL_RW = true;
	/**	Access SQL Read Only		*/
	public static final boolean		SQL_RO = false;
	/**	Access SQL Fully Qualified	*/
	public static final boolean		SQL_FULLYQUALIFIED = true;
	/**	Access SQL Not Fully Qualified	*/
	public static final boolean		SQL_NOTQUALIFIED = false;

	/**	The AD_User_ID of the SuperUser				*/
	public static final int			SUPERUSER_USER_ID = 100;
	/**	The AD_User_ID of the System Administrator	*/
	public static final int			SYSTEM_USER_ID = 0;
	
	
	/**************************************************************************
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param AD_Role_ID id
	 */
	public MRole (Properties ctx, int AD_Role_ID)
	{
		super (ctx, AD_Role_ID);
		//	ID=0 == System Administrator
		if (AD_Role_ID == 0)
		{
		//	setName (null);
			setIsCanExport (true);
			setIsCanReport (true);
			setIsManual (false);
			setIsPersonalAccess (false);
			setIsPersonalLock (false);
			setIsShowAcct (false);
			setIsAccessAllOrgs(false);
			setUserLevel (USERLEVEL_Organization);
		}
	}	//	MRole

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MRole(Properties ctx, ResultSet rs)
	{
		super(ctx, rs);
	}	//	MRole

	
	/**
	 * 	After Save
	 *	@param newRecord new
	 *	@param success success
	 *	@return success
	 */
	protected boolean afterSave (boolean newRecord, boolean success)
	{
		if (newRecord)
		{
			//	Add Role to SuperUser
			MUserRoles su = new MUserRoles(getCtx(), SUPERUSER_USER_ID, getAD_Role_ID());
			su.save();
			//	Add Role to User
			if (getCreatedBy() != SUPERUSER_USER_ID)
			{
				MUserRoles ur = new MUserRoles(getCtx(), getCreatedBy(), getAD_Role_ID());
				ur.save();
			}
			updateAccessRecords();
		}
		//
		else if (is_ValueChanged("UserLevel"))
			updateAccessRecords();
		return success;
	}	//	afterSave

	/**
	 * 	Create Access Records
	 *	@param newRecord new record
	 *	@return info
	 */
	public String updateAccessRecords ()
	{
		if (isManual())
			return "-";
		
		String roleClientOrgUser = getAD_Role_ID() + ","
			+ getAD_Client_ID() + "," + getAD_Org_ID() + ",'Y', SysDate," 
			+ getUpdatedBy() + ", SysDate," + getUpdatedBy() 
			+ ",'Y' ";	//	IsReadWrite
		
		String sqlWindow = "INSERT INTO AD_Window_Access "
			+ "(AD_Window_ID, AD_Role_ID,"
			+ " AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy,IsReadWrite) "
			+ "SELECT DISTINCT w.AD_Window_ID, " + roleClientOrgUser
			+ "FROM AD_Window w"
			+ " INNER JOIN AD_Tab t ON (w.AD_Window_ID=t.AD_Window_ID)"
			+ " INNER JOIN AD_Table tt ON (t.AD_Table_ID=tt.AD_Table_ID) "
			+ "WHERE t.SeqNo=(SELECT MIN(SeqNo) FROM AD_Tab xt "	// only check first tab
				+ "WHERE xt.AD_Window_ID=w.AD_Window_ID)"
			+ "AND tt.AccessLevel IN ";
		
		String sqlProcess = "INSERT INTO AD_Process_Access "
			+ "(AD_Process_ID, AD_Role_ID,"
			+ " AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy,IsReadWrite) "
			+ "SELECT DISTINCT p.AD_Process_ID, " + roleClientOrgUser
			+ "FROM AD_Process p "
			+ "WHERE AccessLevel IN ";

		String sqlForm = "INSERT INTO AD_Form_Access "
			+ "(AD_Form_ID, AD_Role_ID," 
			+ " AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy,IsReadWrite) "
			+ "SELECT f.AD_Form_ID, " + roleClientOrgUser
			+ "FROM AD_Form f "
			+ "WHERE AccessLevel IN ";

		String sqlWorkflow = "INSERT INTO AD_WorkFlow_Access "
			+ "(AD_WorkFlow_ID, AD_Role_ID,"
			+ " AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy,IsReadWrite) "
			+ "SELECT w.AD_WorkFlow_ID, " + roleClientOrgUser
			+ "FROM AD_WorkFlow w "
			+ "WHERE AccessLevel IN ";

		/**
		 *	Fill AD_xx_Access
		 *	---------------------------------------------------------------------------
		 *	SCO# Levels			S__ 100		4	System info
		 *						SCO	111		7	System shared info
		 *						SC_ 110		6	System/Client info
		 *						_CO	011		3	Client shared info
		 *						_C_	011		2	Client
		 *						__O	001		1	Organization info
		 *	Roles:
		 *		S		4,7,6
		 *		_CO		7,6,3,2,1
		 *		__O		3,1,7
		 */
		String accessLevel = null;
		if (USERLEVEL_System.equals(getUserLevel()))
			accessLevel = "('4','7','6')";
		else if (USERLEVEL_Client.equals(getUserLevel()))
			accessLevel = "('7','6','3','2')";
		else if (USERLEVEL_ClientPlusOrganization.equals(getUserLevel()))
			accessLevel = "('7','6','3','2','1')";
		else //	if (USERLEVEL_Organization.equals(getUserLevel()))
			accessLevel = "('3','1','7')";
		//
		String whereDel = " WHERE AD_Role_ID=" + getAD_Role_ID();
		//
		int winDel = DB.executeUpdate("DELETE AD_Window_Access" + whereDel);
		int win = DB.executeUpdate(sqlWindow + accessLevel);
		int procDel = DB.executeUpdate("DELETE AD_Process_Access" + whereDel);
		int proc = DB.executeUpdate(sqlProcess + accessLevel);
		int formDel = DB.executeUpdate("DELETE AD_Form_Access" + whereDel);
		int form = DB.executeUpdate(sqlForm + accessLevel);
		int wfDel = DB.executeUpdate("DELETE AD_WorkFlow_Access" + whereDel);
		int wf = DB.executeUpdate(sqlWorkflow + accessLevel);

		log.debug("updateAccessRecords - AD_Window_ID=" + winDel + "+" + win 
			+ ", AD_Process_ID=" + procDel + "+" + proc
			+ ", AD_Form_ID=" + formDel + "+" + form
			+ ", AD_Workflow_ID=" + wfDel + "+" + wf);
		
		return "@AD_Window_ID@ #" + win 
			+ " -  @AD_Process_ID@ #" + proc
			+ " -  @AD_Form_ID@ #" + form
			+ " -  @AD_Workflow_ID@ #" + wf;
	}	//	createAccessRecords

	
	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MRole[");
		sb.append(getAD_Role_ID()).append(",").append(getName())
			.append(",UserLevel=").append(getUserLevel())
			.append(",").append(getClientWhere(false))
			.append(",").append(getOrgWhere(false))
			.append("]");
		return sb.toString();
	}	//	toString

	/**
	 * 	Extended String Representation
	 *	@param ctx Properties
	 *	@return extended info
	 */
	public String toStringX (Properties ctx)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(Msg.translate(ctx, "AD_Role_ID")).append("=").append(getName())
			.append(" - ").append(Msg.translate(ctx, "IsCanExport")).append("=").append(isCanExport())
			.append(" - ").append(Msg.translate(ctx, "IsCanReport")).append("=").append(isCanReport())
			.append(Env.NL).append(Env.NL);
		//
		for (int i = 0; i < m_orgAccess.length; i++)
			sb.append(m_orgAccess[i].toStringX(ctx)).append(Env.NL);
		sb.append(Env.NL);
		//
		loadTableAccess(false);
		for (int i = 0; i < m_tableAccess.length; i++)
			sb.append(m_tableAccess[i].toStringX(ctx)).append(Env.NL);
		if (m_tableAccess.length > 0)
			sb.append(Env.NL);
		//
		loadColumnAccess(false);
		for (int i = 0; i < m_columnAccess.length; i++)
			sb.append(m_columnAccess[i].toStringX(ctx)).append(Env.NL);
		if (m_columnAccess.length > 0)
			sb.append(Env.NL);
		//
		loadRecordAccess(false);
		for (int i = 0; i < m_recordAccess.length; i++)
			sb.append(m_recordAccess[i].toStringX(ctx)).append(Env.NL);
		return sb.toString();
	}	//	toStringX



	/*************************************************************************
	 * 	Access Management
	 ************************************************************************/

	/** User 								*/
	private int						m_AD_User_ID = -1;	

	/**	Positive List of Organizational Access		*/	
	private MRoleOrgAccess[]		m_orgAccess = null;
	/** List of Table Access						*/
	private MTableAccess[]			m_tableAccess = null;
	/** List of Column Access				*/
	private MColumnAccess[]			m_columnAccess = null;
	/** List of Record Access				*/
	private MRecordAccess[]			m_recordAccess = null;
	/** List of Dependent Record Access		*/
	private MRecordAccess[]			m_recordDependentAccess = null;
	
	/**	Table Data Access Level	*/
	private HashMap					m_tableAccessLevel = null;
	/**	Table Name				*/
	private HashMap					m_tableName = null;
	
	/**	Window Access			*/
	private HashMap		m_windowAccess = null;
	/**	Process Access			*/
	private HashMap		m_processAccess = null;
	/**	Task Access				*/
	private HashMap		m_taskAccess = null;
	/**	Workflow Access			*/
	private HashMap		m_workflowAccess = null;
	/**	Form Access				*/
	private HashMap		m_formAccess = null;

	/**
	 * 	Set Logged in user
	 *	@param AD_User_ID user requesting info
	 */
	public void setAD_User_ID(int AD_User_ID)
	{
		m_AD_User_ID = AD_User_ID;
	}	//	setAD_User_ID

	/**
	 * 	Get Logged in user
	 *	@return AD_User_ID user requesting info
	 */
	public int getAD_User_ID()
	{
		return m_AD_User_ID;
	}	//	getAD_User_ID

	
	/**************************************************************************
	 * 	Load Access Info
	 * 	@param reload re-load from disk
	 */
	public void loadAccess (boolean reload)
	{
		loadOrgAccess(reload);
		loadTableAccess(reload);
		loadTableInfo(reload);
		loadColumnAccess(reload);
		loadRecordAccess(reload);
		if (reload)
		{
			m_windowAccess = null;
			m_processAccess = null;
			m_taskAccess = null;
			m_workflowAccess = null;
			m_formAccess = null;
		}
	}	//	loadAccess

	/**
	 * 	Load Org Access
	 *	@param reload reload
	 */
	private void loadOrgAccess (boolean reload)
	{
		if (!(reload || m_orgAccess == null))
			return;
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Role_OrgAccess "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MRoleOrgAccess(getCtx(), rs)); 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadOrgAccess", e);
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
		m_orgAccess = new MRoleOrgAccess[list.size()];
		list.toArray(m_orgAccess); 
		log.debug("loadOrgAccess - #" + m_orgAccess.length + (reload ? " - reload" : "")); 
		if (Ini.isClient())
		{
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < m_orgAccess.length; i++)
			{
				if (i > 0)
					sb.append(",");
				sb.append(m_orgAccess[i].getAD_Org_ID());
			}
			Env.setContext(Env.getCtx(), "#User_Org", sb.toString());
		}
	}	//	loadOrgAccess
	
	/**
	 * 	Load Table Access
	 *	@param reload reload
	 */
	private void loadTableAccess(boolean reload)
	{
		if (m_tableAccess != null && !reload)
			return;
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Table_Access "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MTableAccess(getCtx(), rs)); 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadTableAccess", e);
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
		m_tableAccess = new MTableAccess[list.size()];
		list.toArray(m_tableAccess); 
		log.debug("loadTableAccess - #" + m_tableAccess.length); 
	}	//	loadTableAccess

	/**
	 * 	Load Table Access and Name
	 *	@param reload reload
	 */
	private void loadTableInfo (boolean reload)
	{
		if (m_tableAccessLevel != null && m_tableName != null && !reload)
			return;
		m_tableAccessLevel = new HashMap(300);
		m_tableName = new HashMap(300);
		PreparedStatement pstmt = null;
		String sql = "SELECT AD_Table_ID, AccessLevel, TableName "
			+ "FROM AD_Table WHERE IsActive='Y'";
		try
		{
			pstmt = DB.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				Integer ii = new Integer(rs.getInt(1));
				m_tableAccessLevel.put(ii, rs.getString(2));
				m_tableName.put(rs.getString(3), ii);
			} 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadTableAccessLevel", e);
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
		log.debug("loadTableAccessLevel - #" + m_tableAccessLevel.size()); 
	}	//	loadTableAccessLevel

	
	/**
	 * 	Load Column Access
	 *	@param reload reload
	 */
	private void loadColumnAccess(boolean reload)
	{
		if (m_columnAccess != null && !reload)
			return;
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Column_Access "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MColumnAccess(getCtx(), rs)); 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadColumnAccess", e);
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
		m_columnAccess = new MColumnAccess[list.size()];
		list.toArray(m_columnAccess); 
		log.debug("loadColumnAccess - #" + m_columnAccess.length); 
	}	//	loadColumnAccess
	
	/**
	 * 	Load Record Access
	 *	@param reload reload
	 */
	private void loadRecordAccess(boolean reload)
	{
		if (!(reload || m_recordAccess == null || m_recordDependentAccess == null))
			return;
		ArrayList list = new ArrayList();
		ArrayList dependent = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Record_Access "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				MRecordAccess ra = new MRecordAccess(getCtx(), rs);
				list.add(ra);
				if (ra.isDependentEntities())
					dependent.add(ra);
			} 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadRecordAccess", e);
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
		m_recordAccess = new MRecordAccess[list.size()];
		list.toArray(m_recordAccess);
		m_recordDependentAccess = new MRecordAccess[dependent.size()];
		dependent.toArray(m_recordDependentAccess);
		log.debug("loadRecordAccess - #" + m_recordAccess.length + " - Dependent #" + m_recordDependentAccess.length); 
	}	//	loadRecordAccess


	/**************************************************************************
	 * 	Get Client Where Clause Value 
	 * 	@param rw read write
	 * 	@return "AD_Client_ID=0" or "AD_Client_ID IN(0,1)"
	 */
	public String getClientWhere (boolean rw)
	{
		//	All Orgs - use Client of Role
		if (isAccessAllOrgs())
		{
			if (rw || getAD_Client_ID() == 0)
				return "AD_Client_ID=" + getAD_Client_ID();
			return "AD_Client_ID IN (0," + getAD_Client_ID() + ")";
		}

		//	Get Client from Org List
		loadOrgAccess (false);
		//	Unique Strings
		HashSet set = new HashSet();
		if (!rw)
			set.add("0");
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
			set.add(String.valueOf(m_orgAccess[i].getAD_Client_ID()));
		//
		StringBuffer sb = new StringBuffer();
		Iterator it = set.iterator();
		boolean oneOnly = true;
		while (it.hasNext())
		{
			if (sb.length() > 0)
			{
				sb.append(",");
				oneOnly = false;
			}
			sb.append(it.next());
		}
		if (oneOnly)
		{
			if (sb.length() > 0)
				return "AD_Client_ID=" + sb.toString();
			else
			{
				log.error("getClientWhere - No Access Org records");
				return "AD_Client_ID=-1";	//	No Access Record
			}
		}
		return "AD_Client_ID IN(" + sb.toString() + ")";
	}	//	getClientWhereValue
	
	/**
	 * 	Access to Client
	 *	@param AD_Client_ID client
	 *	@param rw read write access
	 *	@return true if access
	 */
	public boolean isClientAccess(int AD_Client_ID, boolean rw)
	{
		if (AD_Client_ID == 0 && !rw)	//	can always read System
			return true;
		loadOrgAccess(false);
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
		{
			if (m_orgAccess[i].getAD_Client_ID() == AD_Client_ID)
				return true;
		}
		return false;
	}	//	isClientAccess
	
	/**
	 * 	Get Org Where Clause Value 
	 * 	@param rw read write
	 * 	@return "AD_Org_ID=0" or "AD_Org_ID IN(0,1)" or null (if access all org)
	 */
	public String getOrgWhere (boolean rw)
	{
		if (isAccessAllOrgs())
			return null;
		loadOrgAccess(false);
		//	Unique Strings
		HashSet set = new HashSet();
		if (!rw)
			set.add("0");
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
			set.add(String.valueOf(m_orgAccess[i].getAD_Org_ID()));
		//
		StringBuffer sb = new StringBuffer();
		Iterator it = set.iterator();
		boolean oneOnly = true;
		while (it.hasNext())
		{
			if (sb.length() > 0)
			{
				sb.append(",");
				oneOnly = false;
			}
			sb.append(it.next());
		}
		if (oneOnly)
		{
			if (sb.length() > 0)
				return "AD_Org_ID=" + sb.toString();
			else
			{
				log.error("getOrgWhere - No Access Org records");
				return "AD_Org_ID=-1";	//	No Access Record
			}
		}		
		return "AD_Org_ID IN(" + sb.toString() + ")";
	}	//	getOrgWhereValue
	
	/**
	 * 	Access to Org
	 *	@param AD_Org_ID org
	 *	@param rw read write access
	 *	@return true if access
	 */
	public boolean isOrgAccess(int AD_Org_ID, boolean rw)
	{
		if (isAccessAllOrgs())
			return true;
		if (AD_Org_ID == 0 && !rw)		//	can always read common org
			return true;
		loadOrgAccess(false);
		
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
		{
			if (m_orgAccess[i].getAD_Org_ID() == AD_Org_ID)
				return true;
		}
		return false;
	}	//	isOrgAccess


	/**
	 * 	Can Report on table
	 *	@param AD_Table_ID table
	 *	@return true if access
	 */
	public boolean isCanReport (int AD_Table_ID)
	{
		if (!isCanReport())						//	Role Level block
		{
			log.warn ("isCanReport - Role denied");
			return false;
		}
		if (!isTableAccess(AD_Table_ID, true))	//	No R/O Access to Table
			return false;
		//
		boolean canReport = true;
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (!MTableAccess.ACCESSTYPERULE_Reporting.equals(m_tableAccess[i].getAccessTypeRule()))
				continue;
			if (m_tableAccess[i].isExclude())		//	Exclude
			{
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					canReport = m_tableAccess[i].isCanReport();
					log.debug ("isCanReport - Exclude " + AD_Table_ID + " - " + canReport);
					return canReport;
				}
			}
			else									//	Include
			{
				canReport = false;
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					canReport = m_tableAccess[i].isCanReport();
					log.debug ("isCanReport - Include " + AD_Table_ID + " - " + canReport);
					return canReport;
				}
			}
		}	//	for all Table Access
		log.debug ("isCanReport - " + AD_Table_ID + " - " + canReport);
		return canReport;
	}	//	isCanReport
	
	/**
	 * 	Can Export Table
	 *	@param AD_Table_ID
	 *	@return true if access
	 */
	public boolean isCanExport (int AD_Table_ID)
	{
		if (!isCanExport())						//	Role Level block
		{
			log.warn ("isCanExport - Role denied");
			return false;
		}
		if (!isTableAccess(AD_Table_ID, true))	//	No R/O Access to Table
			return false;
		if (!isCanReport (AD_Table_ID))			//	We cannot Export if we cannot report
			return false;
		//
		boolean canExport = true;
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (!MTableAccess.ACCESSTYPERULE_Exporting.equals(m_tableAccess[i].getAccessTypeRule()))
				continue;
			if (m_tableAccess[i].isExclude())		//	Exclude
			{
				canExport = m_tableAccess[i].isCanExport();
				log.debug ("isCanExport - Exclude " + AD_Table_ID + " - " + canExport);
				return canExport;
			}
			else									//	Include
			{
				canExport = false;
				canExport = m_tableAccess[i].isCanExport();
				log.debug ("isCanExport - Include " + AD_Table_ID + " - " + canExport);
				return canExport;
			}
		}	//	for all Table Access
		log.debug ("isCanExport - " + AD_Table_ID + " - " + canExport);
		return canExport;
	}	//	isCanExport

	/**
	 * 	Access to Table
	 *	@param AD_Table_ID table
	 *	@param ro check read only access otherwise read write access level
	 *	@return has RO/RW access to table
	 */
	public boolean isTableAccess (int AD_Table_ID, boolean ro)
	{
		if (!isTableAccessLevel (AD_Table_ID, ro))	//	Role Based Access
			return false;
		loadTableAccess(false);
		//
		boolean hasAccess = true;	//	assuming exclusive rule
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (!MTableAccess.ACCESSTYPERULE_Accessing.equals(m_tableAccess[i].getAccessTypeRule()))
				continue;
			if (m_tableAccess[i].isExclude())		//	Exclude
			//	If you Exclude Access to a table and select Read Only, 
			//	you can only read data (otherwise no access).
			{
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (ro)
						hasAccess = m_tableAccess[i].isReadOnly();
					else
						hasAccess = false;
					log.debug("isTableAccess - Exclude AD_Table_ID=" + AD_Table_ID 
						+ " (ro="  + ro + ",TableAccessRO=" + m_tableAccess[i].isReadOnly() + ") = " + hasAccess);
					return hasAccess;
				}
			}
			else								//	Include
			//	If you Include Access to a table and select Read Only, 
			//	you can only read data (otherwise full access).
			{
				hasAccess = false;
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (!ro)	//	rw only if not r/o
						hasAccess = !m_tableAccess[i].isReadOnly();
					else
						hasAccess = true;
					log.debug("isTableAccess - Include AD_Table_ID=" + AD_Table_ID 
						+ " (ro="  + ro + ",TableAccessRO=" + m_tableAccess[i].isReadOnly() + ") = " + hasAccess);
					return hasAccess;
				}
			}
		}	//	for all Table Access
		if (!hasAccess)
			log.debug("isTableAccess - AD_Table_ID=" + AD_Table_ID 
				+ "(ro="  + ro + ") = " + hasAccess);
		return hasAccess;
	}	//	isTableAccess

	/**
	 * 	Access to Table based on Role User Level Table Access Level
	 *	@param AD_Table_ID table
	 *	@param ro check read only access otherwise read write access level
	 *	@return has RO/RW access to table
	 */
	public boolean isTableAccessLevel (int AD_Table_ID, boolean ro)
	{
		if (ro)				//	role can always read
			return true;
		//
		loadTableInfo(false);
		//	AccessLevel
		//		1 = Org - 2 = Client - 4 = System
		//		3 = Org+Client - 6 = Client+System - 7 = All
		String accessLevel = (String)m_tableAccessLevel.get(new Integer(AD_Table_ID));
		if (accessLevel == null)
		{
			log.debug("isTableAccessLevel - NO - No AccessLevel - AD_Table_ID=" + AD_Table_ID);
			return false;
		}
		//	Access to all User Levels
		if (accessLevel.equals(X_AD_Table.ACCESSLEVEL_All))
			return true;
		//	User Level = SCO
		String userLevel = getUserLevel();
		//	
		if (userLevel.charAt(0) == 'S'
			&& (accessLevel.equals(X_AD_Table.ACCESSLEVEL_SystemOnly) 
				|| accessLevel.equals(X_AD_Table.ACCESSLEVEL_SystemPlusClient)))
			return true;
		if (userLevel.charAt(1) == 'C'
			&& (accessLevel.equals(X_AD_Table.ACCESSLEVEL_ClientOnly) 
				|| accessLevel.equals(X_AD_Table.ACCESSLEVEL_SystemPlusClient)))
			return true;
		if (userLevel.charAt(2) == 'O'
			&& (accessLevel.equals(X_AD_Table.ACCESSLEVEL_Organization) 
				|| accessLevel.equals(X_AD_Table.ACCESSLEVEL_ClientPlusOrganization)))
			return true;
		log.debug("isTableAccessLevel - NO - AD_Table_ID=" + AD_Table_ID 
			+ ", UserLebel=" + userLevel + ", AccessLevel=" + accessLevel);
		return false;
	}	//	isTableAccessLevel


	/**
	 * 	Access to Column
	 *	@param AD_Table_ID table
	 *	@param AD_Column_ID column
	 *	@param ro read only
	 *	@return true if access
	 */
	public boolean isColumnAccess (int AD_Table_ID, int AD_Column_ID, boolean ro)
	{
		if (!isTableAccess(AD_Table_ID, ro))		//	No Access to Table		
			return false;
		loadColumnAccess(false);
		
		boolean retValue = true;		//	assuming exclusive
		for (int i = 0; i < m_columnAccess.length; i++)
		{
			if (m_columnAccess[i].isExclude())		//	Exclude
			//	If you Exclude Access to a column and select Read Only, 
			//	you can only read data (otherwise no access).
			{
				if (m_columnAccess[i].getAD_Table_ID() == AD_Table_ID 
					&& m_columnAccess[i].getAD_Column_ID() == AD_Column_ID)
				{
					if (ro)		//	just R/O Access requested
						retValue = m_columnAccess[i].isReadOnly();
					else
						retValue = false;
					if (!retValue)
						log.debug("isColumnAccess - Exclude AD_Table_ID=" + AD_Table_ID + ", AD_Column_ID=" + AD_Column_ID 
							+ " (ro="  + ro + ",ColumnAccessRO=" + m_columnAccess[i].isReadOnly() + ") = " + retValue);
					return retValue;
				}
			}
			else								//	Include
			//	If you Include Access to a column and select Read Only, 
			//	you can only read data (otherwise full access).
			{
				if (m_columnAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					retValue = false;
					if (m_columnAccess[i].getAD_Column_ID() == AD_Column_ID)
					{
						if (!ro)	//	rw only if not r/o
							retValue = !m_columnAccess[i].isReadOnly();
						else
							retValue = true;
						if (!retValue)
							log.debug("isColumnAccess - Include AD_Table_ID=" + AD_Table_ID + ", AD_Column_ID=" + AD_Column_ID 
								+ " (ro="  + ro + ",ColumnAccessRO=" + m_columnAccess[i].isReadOnly() + ") = " + retValue);
						return retValue;
					}
				}	//	same table
			}	//	include
		}	//	for all Table Access
		if (!retValue)
			log.debug("isColumnAccess - AD_Table_ID=" + AD_Table_ID + ", AD_Column_ID=" + AD_Column_ID 
				+ " (ro="  + ro + ") = " + retValue);
		return retValue;
	}	//	isColumnAccess

	/**
	 *	Access to Record
	 *	@param AD_Table_ID table
	 *	@param Record_ID
	 *	@param ro
	 *	 * @return boolean
	 */
	public boolean isRecordAccess (int AD_Table_ID, int Record_ID, boolean ro)
	{
		if (!isTableAccess(AD_Table_ID, ro))		//	No Access to Table
			return false;
		loadRecordAccess(false);
		
		boolean negativeList = true;
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (m_recordAccess[i].isExclude())		//	Exclude
			//	If you Exclude Access to a column and select Read Only, 
			//	you can only read data (otherwise no access).
			{
				if (m_recordAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (ro)
						return m_tableAccess[i].isReadOnly();
					else
						return false;
				}
			}
			else								//	Include
			//	If you Include Access to a column and select Read Only, 
			//	you can only read data (otherwise full access).
			{
				negativeList = false;
				if (m_recordAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (!ro)	//	rw only if not r/o
						return !m_tableAccess[i].isReadOnly();
					else
						return true;
				}
			}
		}	//	for all Table Access
		return negativeList;
	}	//	isRecordAccess

	/**
	 * 	Get Window Access
	 *	@param AD_Window_ID window
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getWindowAccess (int AD_Window_ID)
	{
		if (m_windowAccess == null)
		{
			m_windowAccess = new HashMap(100);
			String sql = "SELECT AD_Window_ID, IsReadWrite FROM AD_Window_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_windowAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getWindowAccess", e);
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
			log.debug("getWindowAccess #" + m_windowAccess.size());
		}	//	reload
		Boolean retValue = (Boolean)m_windowAccess.get(new Integer(AD_Window_ID));
	//	log.debug("getWindowAccess - AD_Window_ID=" + AD_Window_ID + " - " + retValue);
		return retValue;
	}	//	getWindowAccess

	/**
	 * 	Get Process Access
	 *	@param AD_Process_ID process
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getProcessAccess (int AD_Process_ID)
	{
		if (m_processAccess == null)
		{
			m_processAccess = new HashMap(50);
			String sql = "SELECT AD_Process_ID, IsReadWrite FROM AD_Process_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_processAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getProcessAccess", e);
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
		}	//	reload
		return (Boolean)m_processAccess.get(new Integer(AD_Process_ID));
	}	//	getProcessAccess

	/**
	 * 	Get Task Access
	 *	@param AD_Task_ID task
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getTaskAccess (int AD_Task_ID)
	{
		if (m_taskAccess == null)
		{
			m_taskAccess = new HashMap(10);
			String sql = "SELECT AD_Task_ID, IsReadWrite FROM AD_Task_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_taskAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getTaskAccess", e);
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
		}	//	reload
		return (Boolean)m_taskAccess.get(new Integer(AD_Task_ID));
	}	//	getTaskAccess

	/**
	 * 	Get Form Access
	 *	@param AD_Form_ID form
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getFormAccess (int AD_Form_ID)
	{
		if (m_formAccess == null)
		{
			m_formAccess = new HashMap(20);
			String sql = "SELECT AD_Form_ID, IsReadWrite FROM AD_Form_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_formAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getFormAccess", e);
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
		}	//	reload
		return (Boolean)m_formAccess.get(new Integer(AD_Form_ID));
	}	//	getTaskAccess

	/**
	 * 	Get Workflow Access
	 *	@param AD_Workflow_ID workflow
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getWorkflowAccess (int AD_Workflow_ID)
	{
		if (m_workflowAccess == null)
		{
			m_workflowAccess = new HashMap(20);
			String sql = "SELECT AD_Workflow_ID, IsReadWrite FROM AD_Workflow_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_workflowAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getWorkflowAccess", e);
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
		}	//	reload
		return (Boolean)m_workflowAccess.get(new Integer(AD_Workflow_ID));
	}	//	getTaskAccess

	
	/*************************************************************************
	 *	Appends where clause to SQL statement for Table
	 *
	 *	@param SQL			existing SQL statement
	 *	@param TableNameIn	Table Name or list of table names AAA, BBB or AAA a, BBB b
	 *	@param fullyQualified	fullyQualified names
	 *	@param rw			if false, includes System Data
	 *	@return				updated SQL statement
	 */
	public String addAccessSQL (String SQL, String TableNameIn, 
		boolean fullyQualified, boolean rw)
	{
		StringBuffer retSQL = new StringBuffer();

		//	Cut off last ORDER BY clause
		String orderBy = "";
		int posOrder = SQL.lastIndexOf(" ORDER BY ");
		if (posOrder != -1)
		{
			orderBy = SQL.substring(posOrder);
			retSQL.append(SQL.substring(0, posOrder));
		}
		else
			retSQL.append(SQL);

		//	Parse SQL
		AccessSqlParser asp = new AccessSqlParser(retSQL.toString());
		AccessSqlParser.TableInfo[] ti = asp.getTableInfo(asp.getMainSqlIndex()); 

		//  Do we have to add WHERE or AND
		if (asp.getMainSql().indexOf(" WHERE ") == -1)
			retSQL.append(" WHERE ");
		else
			retSQL.append(" AND ");

		//	Use First Table
		String tableName = "";
		if (ti.length > 0)
		{
			tableName = ti[0].getSynonym();
			if (tableName.length() == 0)
				tableName = ti[0].getTableName();
		}
		if (TableNameIn != null && !tableName.equals(TableNameIn))
		{
			String msg = "addAccessSQL - TableName not correctly parsed - TableNameIn=" 
				+ TableNameIn + " - " + asp;
			if (ti.length > 0)
				msg += " - #1 " + ti[0]; 
			msg += "\n = " + SQL;
			log.error(msg);
			Trace.printStack();
			tableName = TableNameIn;
		}

		//	Client Access
		if (fullyQualified)
			retSQL.append(tableName).append(".");
		retSQL.append(getClientWhere(rw));
		
		//	Org Access
		if (!isAccessAllOrgs())
		{
			retSQL.append(" AND ");
			if (fullyQualified)
				retSQL.append(tableName).append(".");
			retSQL.append(getOrgWhere(rw));
		}
			
		//	** Data Access	**
		for (int i = 0; i < ti.length; i++)
		{
			String TableName = ti[i].getTableName();
			int AD_Table_ID = getAD_Table_ID (TableName);
			//	Data Table Access
			if (AD_Table_ID != 0 && !isTableAccess(AD_Table_ID, !rw))
			{
				retSQL.append(" AND 1=3");	//	prevent access at all
				log.debug("addAccessSQL - no access to AD_Table_ID=" + AD_Table_ID 
					+ " - " + TableName + " - " + retSQL);
				break;	//	no need to check further 
			}
			
			//	Data Column Access
	
	
		
			//	Data Record Access
			String keyColumnName = "";
			if (fullyQualified)
			{
				keyColumnName = ti[i].getSynonym();	//	table synonym
				if (keyColumnName.length() == 0)
					keyColumnName = TableName;
				keyColumnName += ".";
			}
			keyColumnName += TableName + "_ID";	//	derived from table
	
		//	log.debug("addAccessSQL - " + TableName + "(" + AD_Table_ID + ") " + keyColumnName);
			String recordWhere = getRecordWhere (AD_Table_ID, keyColumnName);
			if (recordWhere.length() > 0)
			{
				retSQL.append(" AND ").append(recordWhere);
				if (Log.isTraceLevel(9))
					log.debug("addAccessSQL - adding record access - " + recordWhere);
			}
		}	//	for all table info
		
		//	Dependent Records (only for main SQL)
		String mainSql = asp.getMainSql();
		loadRecordAccess(false);
		for (int i = 0; i < m_recordDependentAccess.length; i++)
		{
			String columnName = m_recordDependentAccess[i].getKeyColumnName(asp.getTableInfo(asp.getMainSqlIndex()) );
			if (columnName == null)
				continue;	//	no key column
			int posColumn = mainSql.indexOf(columnName);
			if (posColumn == -1)
				continue;
			//	we found the column name - make sure it's a clumn name
			char charCheck = mainSql.charAt(posColumn-1);	//	before
			if (!(charCheck == ',' || charCheck == '.' || charCheck == ' ' || charCheck == '('))
				continue;
			charCheck = mainSql.charAt(posColumn+columnName.length());	//	after
			if (!(charCheck == ',' || charCheck == ' ' || charCheck == ')'))
				continue;
				
			//	*** we found the column in the main query
			log.debug("DEPENDENT  " + columnName + " - " + m_recordDependentAccess[i]);
			StringBuffer where = new StringBuffer();
			where.append(getDependentRecordWhereColumn (mainSql, columnName))
				.append((m_recordDependentAccess[i].isExclude() ? "<>" : "="))
				.append(m_recordDependentAccess[i].getRecord_ID());
			if (Log.isTraceLevel(9))
				log.debug("addAccessSQL - adding dependent access - " + where);
			retSQL.append(" AND ").append(where);			
		}	//	for all dependent records
		
		//
		retSQL.append(orderBy);
		if (Log.isTraceLevel(10))
			log.debug("addAccessSQL - " + retSQL);
		return retSQL.toString();
	}	//	addAccessSQL

	/**
	 * 	Get Dependent Record Where clause
	 *	@param mainSql sql to examine
	 *	@param columnName columnName
	 *	@return where clause column "x.columnName"
	 */
	private String getDependentRecordWhereColumn (String mainSql, String columnName)
	{
		String retValue = columnName;	//	if nothing else found
		int index = mainSql.indexOf(columnName);
		//	see if there are table synonym
		int offset = index - 1;
		char c = mainSql.charAt(offset);
		if (c == '.')
		{
			StringBuffer sb = new StringBuffer();
			while (c != ' ' && c != ',' && c != '(')	//	delimeter
			{
				sb.insert(0, c);
				c = mainSql.charAt(--offset);
			}
			sb.append(columnName);
			return sb.toString();
		}
		return retValue;
	}	//	getDependentRecordWhereColumn



	/**
	 *	UPADATE - Can I Update the record
	 *  Access error info (AccessTableNoUpdate) is saved in the log
	 * 
	 *   * @param AD_Client_ID comntext to derive client/org/user level
	 * @param AD_Org_ID number of the current window to retrieve context
	 * @param AD_Table_ID int
	 * @param createError boolean
	@return	true if you can update
	@see org.compiere.model.MTable#save(boolean)
	 *
	 *   */
	public boolean canUpdate (int AD_Client_ID, int AD_Org_ID, 
		int AD_Table_ID, boolean createError)
	{
		String userLevel = getUserLevel();	//	Format 'SCO'

		if (userLevel.indexOf("S") != -1)		//	System can change anything
			return true;

		boolean	retValue = true;
		String whatMissing = "";

		//	System == Client=0 & Org=0
		if (AD_Client_ID == 0 && AD_Org_ID == 0
			&& userLevel.charAt(0) != 'S')
		{
			retValue = false;
			whatMissing += "S";
		}

		//	Client == Client!=0 & Org=0
		else if (AD_Client_ID != 0 && AD_Org_ID == 0
			&& userLevel.charAt(1) != 'C')
		{
			retValue = false;
			whatMissing += "C";
		}

		//	Organization == Client!=0 & Org!=0
		else if (AD_Client_ID != 0 && AD_Org_ID != 0
			&& userLevel.charAt(2) != 'O')
		{
			retValue = false;
			whatMissing += "O";
		}

		//	Data Access
		//	get Table.IsSecurityEnabled
		//	if yes: get UserAcess info => Where Table.TableKey in (List)
		//									and Access=r/w

		if (!retValue && createError)
		{
			Log.saveError("AccessTableNoUpdate",
				"AD_Client_ID=" + AD_Client_ID + ", AD_Org_ID=" + AD_Org_ID + ", UserLevel=" + userLevel
				+ " => missing=" + whatMissing);
			log.error ("canUpdate - " + toString());
		}
		return retValue;
	}	//	canUpdate

	/**
	 *	VIEW - Can I view record in Table with given TableLevel
	 *  <code>
	 *	TableLevel			S__ 100		4	System info
	 *						SCO	111		7	System shared info
	 *						SC_ 110		6	System/Client info
	 *						_CO	011		3	Client shared info
	 *						_C_	011		2	Client shared info
	 *						__O	001		1	Organization info
	 *  </code>
	 * 
	 * 	@param ctx	context
	 *	@param TableLevel	AccessLevel
	 *	@return	true/false
	 *  Access error info (AccessTableNoUpdate, AccessTableNoView) is saved in the log
	@see org.compiere.model.MTabVO#loadTabDetails (MTabVO, ResultSet)
	 *   */
	public boolean canView(Properties ctx, String TableLevel)
	{
		String userLevel = getUserLevel();	//	Format 'SCO'

		boolean retValue = true;

		//	7 - All
		if (X_AD_Table.ACCESSLEVEL_All.equals(TableLevel))
			retValue = true;
			 
		//	4 - System data requires S
		else if (X_AD_Table.ACCESSLEVEL_SystemOnly.equals(TableLevel) 
			&& userLevel.charAt(0) != 'S')
			retValue = false;

		//	2 - Client data requires C
		else if (X_AD_Table.ACCESSLEVEL_ClientOnly.equals(TableLevel) 
			&& userLevel.charAt(1) != 'C')
			retValue = false;

		//	1 - Organization data requires O
		else if (X_AD_Table.ACCESSLEVEL_Organization.equals(TableLevel) 
			&& userLevel.charAt(2) != 'O')
			retValue = false;

		//	3 - Client Shared requires C or O
		else if (X_AD_Table.ACCESSLEVEL_ClientPlusOrganization.equals(TableLevel)
			&& (!(userLevel.charAt(1) == 'C' || userLevel.charAt(2) == 'O')) )
				retValue = false;

		//	6 - System/Client requires S or C
		else if (X_AD_Table.ACCESSLEVEL_SystemPlusClient.equals(TableLevel)
			&& (!(userLevel.charAt(0) == 'S' || userLevel.charAt(1) == 'C')) )
			retValue = false;

		if (retValue)
			return retValue;

		//  Notification
		/**
		if (forInsert)
			Log.saveError("AccessTableNoUpdate",
				"(Required=" + TableLevel + "("
				+ getTableLevelString(Env.getAD_Language(ctx), TableLevel)
				+ ") != UserLevel=" + userLevel);
		else
		**/
			Log.saveError("AccessTableNoView",
				"Required=" + TableLevel + "("
				+ getTableLevelString(Env.getAD_Language(ctx), TableLevel)
				+ ") != UserLevel=" + userLevel);
		log.info ("canView - " + toString());
		return retValue;
	}	//	canView


	/**
	 *	Returns clear text String of TableLevel
	 *  @param AD_Language language
	 *  @param TableLevel level
	 *  @return info
	 */
	private String getTableLevelString (String AD_Language, String TableLevel)
	{
		String level = TableLevel + "??";
		if (TableLevel.equals("1"))
			level = "AccessOrg";
		else if (TableLevel.equals("2"))
			level = "AccessClient";
		else if (TableLevel.equals("3"))
			level = "AccessClientOrg";
		else if (TableLevel.equals("4"))
			level = "AccessSystem";
		else if (TableLevel.equals("6"))
			level = "AccessSystemClient";
		else if (TableLevel.equals("7"))
			level = "AccessShared";

		return Msg.getMsg(AD_Language, level);
	}	//	getTableLevelString

	/**
	 * 	Get Table ID from name
	 *	@param tableName table name
	 *	@return AD_Table_ID or 0
	 */
	private int getAD_Table_ID (String tableName)
	{
		loadTableInfo(false);
		Integer ii = (Integer)m_tableName.get(tableName);
		if (ii != null)
			return ii.intValue();
	//	log.warn("getAD_Table_ID - not found (" + tableName + ")");
		return 0;
	}	//	getAD_Table_ID

	/**
	 * 	Return Where clause for Record Access
	 *	@param AD_Table_ID table
	 *	@param keyColumnName (fully qualified) key column name
	 *	@return where clause or ""
	 */
	private String getRecordWhere (int AD_Table_ID, String keyColumnName)
	{
		loadRecordAccess(false);
		//
		StringBuffer sb = new StringBuffer();
		//	Role Access
		for (int i = 0; i < m_recordAccess.length; i++)
		{
			if (m_recordAccess[i].getAD_Table_ID() == AD_Table_ID)
			{
				if (sb.length() > 0)
					sb.append(" AND ");
				sb.append(keyColumnName)
					.append((m_recordAccess[i].isExclude() ? "<>" : "="))
					.append(m_recordAccess[i].getRecord_ID());
			}
		}	//	for all Table Access
		
		//	Don't ignore Privacy Access
		if (!isPersonalAccess())
		{
			String lockedIDs = MPrivateAccess.getLockedRecordWhere(AD_Table_ID, m_AD_User_ID);
			if (lockedIDs != null)
			{
				if (sb.length() > 0)
					sb.append(" AND ");
				sb.append(keyColumnName).append(lockedIDs);
			}
		}
		//
		return sb.toString();
	}	//	getRecordWhere

}	//	MRole
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.sql.*;
import java.util.*;

import org.compiere.util.*;

/**
 *	Role Model.
 *	Includes AD_User runtime info for Personal Access
 *	The class is final, so that you cannot overwrite the security rules.
 *	
 *  @author Jorg Janke
 *  @version $Id: MRole.java,v 1.21 2004/05/13 06:08:07 jjanke Exp $
 */
public final class MRole extends X_AD_Role
{
	/**
	 * 	Get Default (Client) Role
	 *	@return role
	 */
	public static MRole getDefault ()
	{
		if (s_defaultRole == null && Ini.isClient())
			return getDefault (Env.getCtx(), false);
		return s_defaultRole;
	}	//	getDefault

	/**
	 * 	Get/Set Default Role.
	 * 	@param ctx context
	 * 	@param reload if true forces load
	 *	@return role
	 *	@see org.compiere.util.DB#loadPreferences
	 */
	public static MRole getDefault (Properties ctx, boolean reload)
	{
		int AD_Role_ID = Env.getContextAsInt(ctx, "#AD_Role_ID");
		int AD_User_ID = Env.getContextAsInt(ctx, "#AD_User_ID");
		if (!Ini.isClient())	//	none for Server
			AD_User_ID = 0;
		if (reload || s_defaultRole == null)
		{
			s_defaultRole = get (ctx, AD_Role_ID, AD_User_ID, reload);
		}
		else if (s_defaultRole.getAD_Role_ID() != AD_Role_ID
			|| s_defaultRole.getAD_User_ID() != AD_User_ID)
		{
			s_defaultRole = get (ctx, AD_Role_ID, AD_User_ID, reload);
		}
		return s_defaultRole;
	}	//	getDefault
	
	/**
	 * 	Get Role for User
	 * 	@param ctx context
	 * 	@param AD_Role_ID role
	 * 	@param AD_User_ID user
	 * 	@param reload if true forces load
	 *	@return role
	 */
	public static MRole get (Properties ctx, int AD_Role_ID, int AD_User_ID, boolean reload)
	{
		s_log.info("get - AD_Role_ID=" + AD_Role_ID + ", AD_User_ID=" + AD_User_ID + ", reload=" + reload);
		String key = AD_Role_ID + "_" + AD_User_ID;
		MRole role = (MRole)s_roles.get (key);
		if (role == null || reload)
		{
			role = new MRole (ctx, AD_Role_ID);
			s_roles.put (key, role);
			if (AD_Role_ID == 0)
				role.load();			//	special Handling
			role.setAD_User_ID(AD_User_ID);
			role.loadAccess(reload);
			s_log.info("get - " + role);
		}
		return role;
	}	//	get

	/**
	 * 	Get Role (cached).
	 * 	Did not set user - so no access loaded
	 * 	@param ctx context
	 * 	@param AD_Role_ID role
	 *	@return role
	 */
	public static MRole get (Properties ctx, int AD_Role_ID)
	{
		String key = String.valueOf(AD_Role_ID);
		MRole role = (MRole)s_roles.get (key);
		if (role == null)
		{
			role = new MRole (ctx, AD_Role_ID);
			s_roles.put (key, role);
			if (AD_Role_ID == 0)
				role.load();			//	special Handling
		}
		return role;
	}	//	get
	
	/**
	 * 	Get Roles Of Client
	 *	@param ctx context
	 *	@return roles of client
	 */
	public static MRole[] getOfClient (Properties ctx)
	{
		String sql = "SELECT * FROM AD_Role WHERE AD_Client_ID=?";
		ArrayList list = new ArrayList ();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql);
			pstmt.setInt (1, Env.getAD_Client_ID(ctx));
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add (new MRole(ctx, rs));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error ("getOfClient", e);
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
		MRole[] retValue = new MRole[list.size ()];
		list.toArray (retValue);
		return retValue;
	}	//	getOfClient
	
	/**	Default Role			*/
	private static MRole			s_defaultRole = null;
	/** Role/User Cache			*/
	private static CCache			s_roles = new CCache("AD_Role", 5);
	/** Log						*/ 
	private static Logger			s_log = Logger.getCLogger(MRole.class);
	
	/**	Access SQL Read Write		*/
	public static final boolean		SQL_RW = true;
	/**	Access SQL Read Only		*/
	public static final boolean		SQL_RO = false;
	/**	Access SQL Fully Qualified	*/
	public static final boolean		SQL_FULLYQUALIFIED = true;
	/**	Access SQL Not Fully Qualified	*/
	public static final boolean		SQL_NOTQUALIFIED = false;

	
	
	/**************************************************************************
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param AD_Role_ID id
	 */
	public MRole (Properties ctx, int AD_Role_ID)
	{
		super (ctx, AD_Role_ID);
		/**	ID=0 == System Administrator
		if (AD_Role_ID == 0)
		{
			setIsCanExport (false);
			setIsCanReport (false);
			setIsManual (false);
			setIsPersonalAccess (false);
			setIsPersonalLock (false);
			setIsShowAcct (false);
			setUserLevel (USERLEVEL_Organization);
		//	setName (null);
		//	setClientList (null);
		//	setOrgList (null);
		}
		/**	System Administrator	**/		
	}	//	MRole

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MRole(Properties ctx, ResultSet rs)
	{
		super(ctx, rs);
	}	//	MRole

	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MRole[");
		sb.append(getAD_Role_ID()).append(",").append(getName())
			.append(",UserLevel=").append(getUserLevel())
			.append(",").append(getClientWhere(false))
			.append(",").append(getOrgWhere(false))
			.append("]");
		return sb.toString();
	}	//	toString

	/**
	 * 	Extended String Representation
	 *	@param ctx Properties
	 *	@return extended info
	 */
	public String toStringX (Properties ctx)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(Msg.translate(ctx, "AD_Role_ID")).append("=").append(getName())
			.append(" - ").append(Msg.translate(ctx, "IsCanExport")).append("=").append(isCanExport())
			.append(" - ").append(Msg.translate(ctx, "IsCanReport")).append("=").append(isCanReport())
			.append(Env.NL).append(Env.NL);
		//
		for (int i = 0; i < m_orgAccess.length; i++)
			sb.append(m_orgAccess[i].toStringX(ctx)).append(Env.NL);
		sb.append(Env.NL);
		//
		loadTableAccess(false);
		for (int i = 0; i < m_tableAccess.length; i++)
			sb.append(m_tableAccess[i].toStringX(ctx)).append(Env.NL);
		if (m_tableAccess.length > 0)
			sb.append(Env.NL);
		//
		loadColumnAccess(false);
		for (int i = 0; i < m_columnAccess.length; i++)
			sb.append(m_columnAccess[i].toStringX(ctx)).append(Env.NL);
		if (m_columnAccess.length > 0)
			sb.append(Env.NL);
		//
		loadRecordAccess(false);
		for (int i = 0; i < m_recordAccess.length; i++)
			sb.append(m_recordAccess[i].toStringX(ctx)).append(Env.NL);
		return sb.toString();
	}	//	toStringX


	/**
	 * 	Load Access Info
	 * 	@param reload re-load from disk
	 */
	public void loadAccess (boolean reload)
	{
		loadOrgAccess(reload);
		loadTableAccess(reload);
		loadTableInfo(reload);
		loadColumnAccess(reload);
		loadRecordAccess(reload);
		if (reload)
		{
			m_windowAccess = null;
			m_processAccess = null;
			m_taskAccess = null;
			m_workflowAccess = null;
			m_formAccess = null;
		}
	}	//	loadAccess


	/*************************************************************************/

	/** User 								*/
	private int						m_AD_User_ID = -1;	

	/**	Positive List of Organizational Access		*/	
	private MRoleOrgAccess[]		m_orgAccess = null;
	/** List of Table Access						*/
	private MTableAccess[]			m_tableAccess = null;
	/** List of Column Access				*/
	private MColumnAccess[]			m_columnAccess = null;
	/** List of Record Access				*/
	private MRecordAccess[]			m_recordAccess = null;
	/** List of Dependent Record Access		*/
	private MRecordAccess[]			m_recordDependentAccess = null;
	
	/**	Table Data Access Level	*/
	private HashMap					m_tableAccessLevel = null;
	/**	Table Name				*/
	private HashMap					m_tableName = null;
	
	/**	Window Access			*/
	private HashMap		m_windowAccess = null;
	/**	Process Access			*/
	private HashMap		m_processAccess = null;
	/**	Task Access				*/
	private HashMap		m_taskAccess = null;
	/**	Workflow Access			*/
	private HashMap		m_workflowAccess = null;
	/**	Form Access				*/
	private HashMap		m_formAccess = null;

	/**
	 * 	Set Logged in user
	 *	@param AD_User_ID user requesting info
	 */
	public void setAD_User_ID(int AD_User_ID)
	{
		m_AD_User_ID = AD_User_ID;
	}	//	setAD_User_ID

	/**
	 * 	Get Logged in user
	 *	@return AD_User_ID user requesting info
	 */
	public int getAD_User_ID()
	{
		return m_AD_User_ID;
	}	//	getAD_User_ID
	
	/**
	 * 	Load Org Access
	 *	@param reload reload
	 */
	private void loadOrgAccess (boolean reload)
	{
		if (!(reload || m_orgAccess == null))
			return;
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Role_OrgAccess "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MRoleOrgAccess(getCtx(), rs)); 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadOrgAccess", e);
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
		m_orgAccess = new MRoleOrgAccess[list.size()];
		list.toArray(m_orgAccess); 
		log.debug("loadOrgAccess - #" + m_orgAccess.length + (reload ? " - reload" : "")); 
		if (Ini.isClient())
		{
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < m_orgAccess.length; i++)
			{
				if (i > 0)
					sb.append(",");
				sb.append(m_orgAccess[i].getAD_Org_ID());
			}
			Env.setContext(Env.getCtx(), "#User_Org", sb.toString());
		}
	}	//	loadOrgAccess
	
	/**
	 * 	Load Table Access
	 *	@param reload reload
	 */
	private void loadTableAccess(boolean reload)
	{
		if (m_tableAccess != null && !reload)
			return;
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Table_Access "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MTableAccess(getCtx(), rs)); 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadTableAccess", e);
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
		m_tableAccess = new MTableAccess[list.size()];
		list.toArray(m_tableAccess); 
		log.debug("loadTableAccess - #" + m_tableAccess.length); 
	}	//	loadTableAccess

	/**
	 * 	Load Table Access and Name
	 *	@param reload reload
	 */
	private void loadTableInfo (boolean reload)
	{
		if (m_tableAccessLevel != null && m_tableName != null && !reload)
			return;
		m_tableAccessLevel = new HashMap(300);
		m_tableName = new HashMap(300);
		PreparedStatement pstmt = null;
		String sql = "SELECT AD_Table_ID, AccessLevel, TableName "
			+ "FROM AD_Table WHERE IsActive='Y'";
		try
		{
			pstmt = DB.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				Integer ii = new Integer(rs.getInt(1));
				m_tableAccessLevel.put(ii, rs.getString(2));
				m_tableName.put(rs.getString(3), ii);
			} 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadTableAccessLevel", e);
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
		log.debug("loadTableAccessLevel - #" + m_tableAccessLevel.size()); 
	}	//	loadTableAccessLevel

	
	/**
	 * 	Load Column Access
	 *	@param reload reload
	 */
	private void loadColumnAccess(boolean reload)
	{
		if (m_columnAccess != null && !reload)
			return;
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Column_Access "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MColumnAccess(getCtx(), rs)); 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadColumnAccess", e);
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
		m_columnAccess = new MColumnAccess[list.size()];
		list.toArray(m_columnAccess); 
		log.debug("loadColumnAccess - #" + m_columnAccess.length); 
	}	//	loadColumnAccess
	
	/**
	 * 	Load Record Access
	 *	@param reload reload
	 */
	private void loadRecordAccess(boolean reload)
	{
		if (!(reload || m_recordAccess == null || m_recordDependentAccess == null))
			return;
		ArrayList list = new ArrayList();
		ArrayList dependent = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Record_Access "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				MRecordAccess ra = new MRecordAccess(getCtx(), rs);
				list.add(ra);
				if (ra.isDependentEntities())
					dependent.add(ra);
			} 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadRecordAccess", e);
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
		m_recordAccess = new MRecordAccess[list.size()];
		list.toArray(m_recordAccess);
		m_recordDependentAccess = new MRecordAccess[dependent.size()];
		dependent.toArray(m_recordDependentAccess);
		log.debug("loadRecordAccess - #" + m_recordAccess.length + " - Dependent #" + m_recordDependentAccess.length); 
	}	//	loadRecordAccess


	/**************************************************************************
	 * 	Get Client Where Clause Value 
	 * 	@param rw read write
	 * 	@return "AD_Client_ID=0" or "AD_Client_ID IN(0,1)"
	 */
	public String getClientWhere (boolean rw)
	{
		loadOrgAccess (false);
		//	Unique Strings
		HashSet set = new HashSet();
		if (!rw)
			set.add("0");
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
			set.add(String.valueOf(m_orgAccess[i].getAD_Client_ID()));
		//
		StringBuffer sb = new StringBuffer();
		Iterator it = set.iterator();
		boolean oneOnly = true;
		while (it.hasNext())
		{
			if (sb.length() > 0)
			{
				sb.append(",");
				oneOnly = false;
			}
			sb.append(it.next());
		}
		if (oneOnly)
		{
			if (sb.length() > 0)
				return "AD_Client_ID=" + sb.toString();
			else
			{
				log.error("getClientWhere - No Access Org records");
				return "AD_Client_ID=-1";	//	No Access Record
			}
		}
		return "AD_Client_ID IN(" + sb.toString() + ")";
	}	//	getClientWhereValue
	
	/**
	 * 	Access to Client
	 *	@param AD_Client_ID client
	 *	@param rw read write access
	 *	@return true if access
	 */
	public boolean isClientAccess(int AD_Client_ID, boolean rw)
	{
		if (AD_Client_ID == 0 && !rw)	//	can always read System
			return true;
		loadOrgAccess(false);
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
		{
			if (m_orgAccess[i].getAD_Client_ID() == AD_Client_ID)
				return true;
		}
		return false;
	}	//	isClientAccess
	
	/**
	 * 	Get Org Where Clause Value 
	 * 	@param rw read write
	 * 	@return "AD_Org_ID=0" or "AD_Org_ID IN(0,1)"
	 */
	public String getOrgWhere (boolean rw)
	{
		loadOrgAccess(false);
		//	Unique Strings
		HashSet set = new HashSet();
		if (!rw)
			set.add("0");
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
			set.add(String.valueOf(m_orgAccess[i].getAD_Org_ID()));
		//
		StringBuffer sb = new StringBuffer();
		Iterator it = set.iterator();
		boolean oneOnly = true;
		while (it.hasNext())
		{
			if (sb.length() > 0)
			{
				sb.append(",");
				oneOnly = false;
			}
			sb.append(it.next());
		}
		if (oneOnly)
		{
			if (sb.length() > 0)
				return "AD_Org_ID=" + sb.toString();
			else
			{
				log.error("getOrgWhere - No Access Org records");
				return "AD_Org_ID=-1";	//	No Access Record
			}
		}		
		return "AD_Org_ID IN(" + sb.toString() + ")";
	}	//	getOrgWhereValue
	
	/**
	 * 	Access to Org
	 *	@param AD_Org_ID org
	 *	@param rw read write access
	 *	@return true if access
	 */
	public boolean isOrgAccess(int AD_Org_ID, boolean rw)
	{
		if (AD_Org_ID == 0 && !rw)		//	can always read common org
			return true;
		loadOrgAccess(false);
		
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
		{
			if (m_orgAccess[i].getAD_Org_ID() == AD_Org_ID)
				return true;
		}
		return false;
	}	//	isOrgAccess


	/**
	 * 	Can Report on table
	 *	@param AD_Table_ID table
	 *	@return true if access
	 */
	public boolean isCanReport (int AD_Table_ID)
	{
		if (!isCanReport())						//	Role Level block
		{
			log.warn ("isCanReport - Role denied");
			return false;
		}
		if (!isTableAccess(AD_Table_ID, true))	//	No R/O Access to Table
			return false;
		//
		boolean canReport = true;
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (!MTableAccess.ACCESSTYPERULE_Reporting.equals(m_tableAccess[i].getAccessTypeRule()))
				continue;
			if (m_tableAccess[i].isExclude())		//	Exclude
			{
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					canReport = m_tableAccess[i].isCanReport();
					log.debug ("isCanReport - Exclude " + AD_Table_ID + " - " + canReport);
					return canReport;
				}
			}
			else									//	Include
			{
				canReport = false;
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					canReport = m_tableAccess[i].isCanReport();
					log.debug ("isCanReport - Include " + AD_Table_ID + " - " + canReport);
					return canReport;
				}
			}
		}	//	for all Table Access
		log.debug ("isCanReport - " + AD_Table_ID + " - " + canReport);
		return canReport;
	}	//	isCanReport
	
	/**
	 * 	Can Export Table
	 *	@param AD_Table_ID
	 *	@return true if access
	 */
	public boolean isCanExport (int AD_Table_ID)
	{
		if (!isCanExport())						//	Role Level block
		{
			log.warn ("isCanExport - Role denied");
			return false;
		}
		if (!isTableAccess(AD_Table_ID, true))	//	No R/O Access to Table
			return false;
		if (!isCanReport (AD_Table_ID))			//	We cannot Export if we cannot report
			return false;
		//
		boolean canExport = true;
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (!MTableAccess.ACCESSTYPERULE_Exporting.equals(m_tableAccess[i].getAccessTypeRule()))
				continue;
			if (m_tableAccess[i].isExclude())		//	Exclude
			{
				canExport = m_tableAccess[i].isCanExport();
				log.debug ("isCanExport - Exclude " + AD_Table_ID + " - " + canExport);
				return canExport;
			}
			else									//	Include
			{
				canExport = false;
				canExport = m_tableAccess[i].isCanExport();
				log.debug ("isCanExport - Include " + AD_Table_ID + " - " + canExport);
				return canExport;
			}
		}	//	for all Table Access
		log.debug ("isCanExport - " + AD_Table_ID + " - " + canExport);
		return canExport;
	}	//	isCanExport

	/**
	 * 	Access to Table
	 *	@param AD_Table_ID table
	 *	@param ro check read only access otherwise read write access level
	 *	@return has RO/RW access to table
	 */
	public boolean isTableAccess (int AD_Table_ID, boolean ro)
	{
		if (!isTableAccessLevel (AD_Table_ID, ro))	//	Role Based Access
			return false;
		loadTableAccess(false);
		//
		boolean hasAccess = true;	//	assuming exclusive rule
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (!MTableAccess.ACCESSTYPERULE_Accessing.equals(m_tableAccess[i].getAccessTypeRule()))
				continue;
			if (m_tableAccess[i].isExclude())		//	Exclude
			//	If you Exclude Access to a table and select Read Only, 
			//	you can only read data (otherwise no access).
			{
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (ro)
						hasAccess = m_tableAccess[i].isReadOnly();
					else
						hasAccess = false;
					log.debug("isTableAccess - Exclude AD_Table_ID=" + AD_Table_ID 
						+ " (ro="  + ro + ",TableAccessRO=" + m_tableAccess[i].isReadOnly() + ") = " + hasAccess);
					return hasAccess;
				}
			}
			else								//	Include
			//	If you Include Access to a table and select Read Only, 
			//	you can only read data (otherwise full access).
			{
				hasAccess = false;
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (!ro)	//	rw only if not r/o
						hasAccess = !m_tableAccess[i].isReadOnly();
					else
						hasAccess = true;
					log.debug("isTableAccess - Include AD_Table_ID=" + AD_Table_ID 
						+ " (ro="  + ro + ",TableAccessRO=" + m_tableAccess[i].isReadOnly() + ") = " + hasAccess);
					return hasAccess;
				}
			}
		}	//	for all Table Access
		if (!hasAccess)
			log.debug("isTableAccess - AD_Table_ID=" + AD_Table_ID 
				+ "(ro="  + ro + ") = " + hasAccess);
		return hasAccess;
	}	//	isTableAccess

	/**
	 * 	Access to Table based on Role User Level Table Access Level
	 *	@param AD_Table_ID table
	 *	@param ro check read only access otherwise read write access level
	 *	@return has RO/RW access to table
	 */
	public boolean isTableAccessLevel (int AD_Table_ID, boolean ro)
	{
		if (ro)				//	role can always read
			return true;
		//
		loadTableInfo(false);
		//	AccessLevel
		//		1 = Org - 2 = Client - 4 = System
		//		3 = Org+Client - 6 = Client+System - 7 = All
		String accessLevel = (String)m_tableAccessLevel.get(new Integer(AD_Table_ID));
		if (accessLevel == null)
		{
			log.debug("isTableAccessLevel - NO - No AccessLevel - AD_Table_ID=" + AD_Table_ID);
			return false;
		}
		//	Access to all User Levels
		if (accessLevel.equals(X_AD_Table.ACCESSLEVEL_All))
			return true;
		//	User Level = SCO
		String userLevel = getUserLevel();
		//	
		if (userLevel.charAt(0) == 'S'
			&& (accessLevel.equals(X_AD_Table.ACCESSLEVEL_SystemOnly) 
				|| accessLevel.equals(X_AD_Table.ACCESSLEVEL_SystemPlusClient)))
			return true;
		if (userLevel.charAt(1) == 'C'
			&& (accessLevel.equals(X_AD_Table.ACCESSLEVEL_ClientOnly) 
				|| accessLevel.equals(X_AD_Table.ACCESSLEVEL_SystemPlusClient)))
			return true;
		if (userLevel.charAt(2) == 'O'
			&& (accessLevel.equals(X_AD_Table.ACCESSLEVEL_Organization) 
				|| accessLevel.equals(X_AD_Table.ACCESSLEVEL_ClientPlusOrganization)))
			return true;
		log.debug("isTableAccessLevel - NO - AD_Table_ID=" + AD_Table_ID 
			+ ", UserLebel=" + userLevel + ", AccessLevel=" + accessLevel);
		return false;
	}	//	isTableAccessLevel


	/**
	 * 	Access to Column
	 *	@param AD_Table_ID table
	 *	@param AD_Column_ID column
	 *	@param ro read only
	 *	@return true if access
	 */
	public boolean isColumnAccess (int AD_Table_ID, int AD_Column_ID, boolean ro)
	{
		if (!isTableAccess(AD_Table_ID, ro))		//	No Access to Table		
			return false;
		loadColumnAccess(false);
		
		boolean retValue = true;		//	assuming exclusive
		for (int i = 0; i < m_columnAccess.length; i++)
		{
			if (m_columnAccess[i].isExclude())		//	Exclude
			//	If you Exclude Access to a column and select Read Only, 
			//	you can only read data (otherwise no access).
			{
				if (m_columnAccess[i].getAD_Table_ID() == AD_Table_ID 
					&& m_columnAccess[i].getAD_Column_ID() == AD_Column_ID)
				{
					if (ro)		//	just R/O Access requested
						retValue = m_columnAccess[i].isReadOnly();
					else
						retValue = false;
					if (!retValue)
						log.debug("isColumnAccess - Exclude AD_Table_ID=" + AD_Table_ID + ", AD_Column_ID=" + AD_Column_ID 
							+ " (ro="  + ro + ",ColumnAccessRO=" + m_columnAccess[i].isReadOnly() + ") = " + retValue);
					return retValue;
				}
			}
			else								//	Include
			//	If you Include Access to a column and select Read Only, 
			//	you can only read data (otherwise full access).
			{
				if (m_columnAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					retValue = false;
					if (m_columnAccess[i].getAD_Column_ID() == AD_Column_ID)
					{
						if (!ro)	//	rw only if not r/o
							retValue = !m_columnAccess[i].isReadOnly();
						else
							retValue = true;
						if (!retValue)
							log.debug("isColumnAccess - Include AD_Table_ID=" + AD_Table_ID + ", AD_Column_ID=" + AD_Column_ID 
								+ " (ro="  + ro + ",ColumnAccessRO=" + m_columnAccess[i].isReadOnly() + ") = " + retValue);
						return retValue;
					}
				}	//	same table
			}	//	include
		}	//	for all Table Access
		if (!retValue)
			log.debug("isColumnAccess - AD_Table_ID=" + AD_Table_ID + ", AD_Column_ID=" + AD_Column_ID 
				+ " (ro="  + ro + ") = " + retValue);
		return retValue;
	}	//	isColumnAccess

	/**
	 *	Access to Record
	 *	@param AD_Table_ID table
	 *	@param Record_ID
	 *	@param ro
	 *	 * @return boolean
	 */
	public boolean isRecordAccess (int AD_Table_ID, int Record_ID, boolean ro)
	{
		if (!isTableAccess(AD_Table_ID, ro))		//	No Access to Table
			return false;
		loadRecordAccess(false);
		
		boolean negativeList = true;
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (m_recordAccess[i].isExclude())		//	Exclude
			//	If you Exclude Access to a column and select Read Only, 
			//	you can only read data (otherwise no access).
			{
				if (m_recordAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (ro)
						return m_tableAccess[i].isReadOnly();
					else
						return false;
				}
			}
			else								//	Include
			//	If you Include Access to a column and select Read Only, 
			//	you can only read data (otherwise full access).
			{
				negativeList = false;
				if (m_recordAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (!ro)	//	rw only if not r/o
						return !m_tableAccess[i].isReadOnly();
					else
						return true;
				}
			}
		}	//	for all Table Access
		return negativeList;
	}	//	isRecordAccess

	/**
	 * 	Get Window Access
	 *	@param AD_Window_ID window
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getWindowAccess (int AD_Window_ID)
	{
		if (m_windowAccess == null)
		{
			m_windowAccess = new HashMap(100);
			String sql = "SELECT AD_Window_ID, IsReadWrite FROM AD_Window_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_windowAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getWindowAccess", e);
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
			log.debug("getWindowAccess #" + m_windowAccess.size());
		}	//	reload
		Boolean retValue = (Boolean)m_windowAccess.get(new Integer(AD_Window_ID));
	//	log.debug("getWindowAccess - AD_Window_ID=" + AD_Window_ID + " - " + retValue);
		return retValue;
	}	//	getWindowAccess

	/**
	 * 	Get Process Access
	 *	@param AD_Process_ID process
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getProcessAccess (int AD_Process_ID)
	{
		if (m_processAccess == null)
		{
			m_processAccess = new HashMap(50);
			String sql = "SELECT AD_Process_ID, IsReadWrite FROM AD_Process_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_processAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getProcessAccess", e);
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
		}	//	reload
		return (Boolean)m_processAccess.get(new Integer(AD_Process_ID));
	}	//	getProcessAccess

	/**
	 * 	Get Task Access
	 *	@param AD_Task_ID task
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getTaskAccess (int AD_Task_ID)
	{
		if (m_taskAccess == null)
		{
			m_taskAccess = new HashMap(10);
			String sql = "SELECT AD_Task_ID, IsReadWrite FROM AD_Task_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_taskAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getTaskAccess", e);
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
		}	//	reload
		return (Boolean)m_taskAccess.get(new Integer(AD_Task_ID));
	}	//	getTaskAccess

	/**
	 * 	Get Form Access
	 *	@param AD_Form_ID form
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getFormAccess (int AD_Form_ID)
	{
		if (m_formAccess == null)
		{
			m_formAccess = new HashMap(20);
			String sql = "SELECT AD_Form_ID, IsReadWrite FROM AD_Form_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_formAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getFormAccess", e);
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
		}	//	reload
		return (Boolean)m_formAccess.get(new Integer(AD_Form_ID));
	}	//	getTaskAccess

	/**
	 * 	Get Workflow Access
	 *	@param AD_Workflow_ID workflow
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getWorkflowAccess (int AD_Workflow_ID)
	{
		if (m_workflowAccess == null)
		{
			m_workflowAccess = new HashMap(20);
			String sql = "SELECT AD_Workflow_ID, IsReadWrite FROM AD_Workflow_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_workflowAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getWorkflowAccess", e);
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
		}	//	reload
		return (Boolean)m_workflowAccess.get(new Integer(AD_Workflow_ID));
	}	//	getTaskAccess

	/*************************************************************************

	/**
	 *	Appends where clause to SQL statement for Table
	 *
	 *	@param SQL			existing SQL statement
	 *	@param TableNameIn	Table Name or list of table names AAA, BBB or AAA a, BBB b
	 *	@param fullyQualified	fullyQualified names
	 *	@param rw			if false, includes System Data
	 *	@return				updated SQL statement
	 */
	public String addAccessSQL (String SQL, String TableNameIn, 
		boolean fullyQualified, boolean rw)
	{
		StringBuffer retSQL = new StringBuffer();

		//	Cut off last ORDER BY clause
		String orderBy = "";
		int posOrder = SQL.lastIndexOf(" ORDER BY ");
		if (posOrder != -1)
		{
			orderBy = SQL.substring(posOrder);
			retSQL.append(SQL.substring(0, posOrder));
		}
		else
			retSQL.append(SQL);

		//	Parse SQL
		AccessSqlParser asp = new AccessSqlParser(retSQL.toString());
		AccessSqlParser.TableInfo[] ti = asp.getTableInfo(asp.getMainSqlIndex()); 

		//  Do we have to add WHERE or AND
		if (asp.getMainSql().indexOf(" WHERE ") == -1)
			retSQL.append(" WHERE ");
		else
			retSQL.append(" AND ");

		//	Use First Table
		String tableName = "";
		if (ti.length > 0)
		{
			tableName = ti[0].getSynonym();
			if (tableName.length() == 0)
				tableName = ti[0].getTableName();
		}
		if (!tableName.equals(TableNameIn) && TableNameIn != null)
		{
			String msg = "addAccessSQL - TableName not correctly parsed - TableNameIn=" 
				+ TableNameIn + " - " + asp;
			if (ti.length > 0)
				msg += " - #1 " + ti[0]; 
			msg += "\n = " + SQL;
			log.error(msg);
			Trace.printStack();
			tableName = TableNameIn;
		}

		//	Client Access
		if (fullyQualified)
			retSQL.append(tableName).append(".");
		retSQL.append(getClientWhere(rw));
		
		//	Org Access
		retSQL.append(" AND ");
		if (fullyQualified)
			retSQL.append(tableName).append(".");
		retSQL.append(getOrgWhere(rw));

			
		//	** Data Access	**
		for (int i = 0; i < ti.length; i++)
		{
			String TableName = ti[i].getTableName();
			int AD_Table_ID = getAD_Table_ID (TableName);
			//	Data Table Access
			if (AD_Table_ID != 0 && !isTableAccess(AD_Table_ID, !rw))
			{
				retSQL.append(" AND 1=3");	//	prevent access at all
				log.debug("addAccessSQL - no access to AD_Table_ID=" + AD_Table_ID 
					+ " - " + TableName + " - " + retSQL);
				break;	//	no need to check further 
			}
			
			//	Data Column Access
	
	
		
			//	Data Record Access
			String keyColumnName = "";
			if (fullyQualified)
			{
				keyColumnName = ti[i].getSynonym();	//	table synonym
				if (keyColumnName.length() == 0)
					keyColumnName = TableName;
				keyColumnName += ".";
			}
			keyColumnName += TableName + "_ID";	//	derived from table
	
		//	log.debug("addAccessSQL - " + TableName + "(" + AD_Table_ID + ") " + keyColumnName);
			String recordWhere = getRecordWhere (AD_Table_ID, keyColumnName);
			if (recordWhere.length() > 0)
			{
				retSQL.append(" AND ").append(recordWhere);
				if (Log.isTraceLevel(9))
					log.debug("addAccessSQL - adding record access - " + recordWhere);
			}
		}	//	for all table info
		
		//	Dependent Records (only for main SQL)
		String mainSql = asp.getMainSql();
		loadRecordAccess(false);
		for (int i = 0; i < m_recordDependentAccess.length; i++)
		{
			String columnName = m_recordDependentAccess[i].getKeyColumnName(asp.getTableInfo(asp.getMainSqlIndex()) );
			if (columnName == null)
				continue;	//	no key column
			int posColumn = mainSql.indexOf(columnName);
			if (posColumn == -1)
				continue;
			//	we found the column name - make sure it's a clumn name
			char charCheck = mainSql.charAt(posColumn-1);	//	before
			if (!(charCheck == ',' || charCheck == '.' || charCheck == ' ' || charCheck == '('))
				continue;
			charCheck = mainSql.charAt(posColumn+columnName.length());	//	after
			if (!(charCheck == ',' || charCheck == ' ' || charCheck == ')'))
				continue;
				
			//	*** we found the column in the main query
			log.debug("DEPENDENT  " + columnName + " - " + m_recordDependentAccess[i]);
			StringBuffer where = new StringBuffer();
			where.append(getDependentRecordWhereColumn (mainSql, columnName))
				.append((m_recordDependentAccess[i].isExclude() ? "<>" : "="))
				.append(m_recordDependentAccess[i].getRecord_ID());
			if (Log.isTraceLevel(9))
				log.debug("addAccessSQL - adding dependent access - " + where);
			retSQL.append(" AND ").append(where);			
		}	//	for all dependent records
		
		//
		retSQL.append(orderBy);
		if (Log.isTraceLevel(10))
			log.debug("addAccessSQL - " + retSQL);
		return retSQL.toString();
	}	//	addAccessSQL

	/**
	 * 	Get Dependent Record Where clause
	 *	@param mainSql sql to examine
	 *	@param columnName columnName
	 *	@return where clause column "x.columnName"
	 */
	private String getDependentRecordWhereColumn (String mainSql, String columnName)
	{
		String retValue = columnName;	//	if nothing else found
		int index = mainSql.indexOf(columnName);
		//	see if there are table synonym
		int offset = index - 1;
		char c = mainSql.charAt(offset);
		if (c == '.')
		{
			StringBuffer sb = new StringBuffer();
			while (c != ' ' && c != ',' && c != '(')	//	delimeter
			{
				sb.insert(0, c);
				c = mainSql.charAt(--offset);
			}
			sb.append(columnName);
			return sb.toString();
		}
		return retValue;
	}	//	getDependentRecordWhereColumn



	/**
	 *	UPADATE - Can I Update the record
	 *  Access error info (AccessTableNoUpdate) is saved in the log
	 * 
	 *   * @param AD_Client_ID comntext to derive client/org/user level
	 * @param AD_Org_ID number of the current window to retrieve context
	 * @param AD_Table_ID int
	 * @param createError boolean
	@return	true if you can update
	@see org.compiere.model.MTable#save(boolean)
	 *
	 *   */
	public boolean canUpdate (int AD_Client_ID, int AD_Org_ID, 
		int AD_Table_ID, boolean createError)
	{
		String userLevel = getUserLevel();	//	Format 'SCO'

		if (userLevel.indexOf("S") != -1)		//	System can change anything
			return true;

		boolean	retValue = true;
		String whatMissing = "";

		//	System == Client=0 & Org=0
		if (AD_Client_ID == 0 && AD_Org_ID == 0
			&& userLevel.charAt(0) != 'S')
		{
			retValue = false;
			whatMissing += "S";
		}

		//	Client == Client!=0 & Org=0
		else if (AD_Client_ID != 0 && AD_Org_ID == 0
			&& userLevel.charAt(1) != 'C')
		{
			retValue = false;
			whatMissing += "C";
		}

		//	Organization == Client!=0 & Org!=0
		else if (AD_Client_ID != 0 && AD_Org_ID != 0
			&& userLevel.charAt(2) != 'O')
		{
			retValue = false;
			whatMissing += "O";
		}

		//	Data Access
		//	get Table.IsSecurityEnabled
		//	if yes: get UserAcess info => Where Table.TableKey in (List)
		//									and Access=r/w

		if (!retValue && createError)
		{
			Log.saveError("AccessTableNoUpdate",
				"AD_Client_ID=" + AD_Client_ID + ", AD_Org_ID=" + AD_Org_ID + ", UserLevel=" + userLevel
				+ " => missing=" + whatMissing);
			log.error ("canUpdate - " + toString());
		}
		return retValue;
	}	//	canUpdate

	/**
	 *	VIEW - Can I view record in Table with given TableLevel
	 *  <code>
	 *	TableLevel			S__ 100		4	System info
	 *						SCO	111		7	System shared info
	 *						SC_ 110		6	System/Client info
	 *						_CO	011		3	Client shared info
	 *						_C_	011		2	Client shared info
	 *						__O	001		1	Organization info
	 *  </code>
	 * 
	 * 	@param ctx	context
	 *	@param TableLevel	AccessLevel
	 *	@return	true/false
	 *  Access error info (AccessTableNoUpdate, AccessTableNoView) is saved in the log
	@see org.compiere.model.MTabVO#loadTabDetails (MTabVO, ResultSet)
	 *   */
	public boolean canView(Properties ctx, String TableLevel)
	{
		String userLevel = getUserLevel();	//	Format 'SCO'

		boolean retValue = true;

		//	7 - All
		if (X_AD_Table.ACCESSLEVEL_All.equals(TableLevel))
			retValue = true;
			 
		//	4 - System data requires S
		else if (X_AD_Table.ACCESSLEVEL_SystemOnly.equals(TableLevel) 
			&& userLevel.charAt(0) != 'S')
			retValue = false;

		//	2 - Client data requires C
		else if (X_AD_Table.ACCESSLEVEL_ClientOnly.equals(TableLevel) 
			&& userLevel.charAt(1) != 'C')
			retValue = false;

		//	1 - Organization data requires O
		else if (X_AD_Table.ACCESSLEVEL_Organization.equals(TableLevel) 
			&& userLevel.charAt(2) != 'O')
			retValue = false;

		//	3 - Client Shared requires C or O
		else if (X_AD_Table.ACCESSLEVEL_ClientPlusOrganization.equals(TableLevel)
			&& (!(userLevel.charAt(1) == 'C' || userLevel.charAt(2) == 'O')) )
				retValue = false;

		//	6 - System/Client requires S or C
		else if (X_AD_Table.ACCESSLEVEL_SystemPlusClient.equals(TableLevel)
			&& (!(userLevel.charAt(0) == 'S' || userLevel.charAt(1) == 'C')) )
			retValue = false;

		if (retValue)
			return retValue;

		//  Notification
		/**
		if (forInsert)
			Log.saveError("AccessTableNoUpdate",
				"(Required=" + TableLevel + "("
				+ getTableLevelString(Env.getAD_Language(ctx), TableLevel)
				+ ") != UserLevel=" + userLevel);
		else
		**/
			Log.saveError("AccessTableNoView",
				"Required=" + TableLevel + "("
				+ getTableLevelString(Env.getAD_Language(ctx), TableLevel)
				+ ") != UserLevel=" + userLevel);
		log.error ("canView - " + toString());
		return retValue;
	}	//	canView


	/**
	 *	Returns clear text String of TableLevel
	 *  @param AD_Language language
	 *  @param TableLevel level
	 *  @return info
	 */
	private String getTableLevelString (String AD_Language, String TableLevel)
	{
		String level = TableLevel + "??";
		if (TableLevel.equals("1"))
			level = "AccessOrg";
		else if (TableLevel.equals("2"))
			level = "AccessClient";
		else if (TableLevel.equals("3"))
			level = "AccessClientOrg";
		else if (TableLevel.equals("4"))
			level = "AccessSystem";
		else if (TableLevel.equals("6"))
			level = "AccessSystemClient";
		else if (TableLevel.equals("7"))
			level = "AccessShared";

		return Msg.getMsg(AD_Language, level);
	}	//	getTableLevelString

	/**
	 * 	Get Table ID from name
	 *	@param tableName table name
	 *	@return AD_Table_ID or 0
	 */
	private int getAD_Table_ID (String tableName)
	{
		loadTableInfo(false);
		Integer ii = (Integer)m_tableName.get(tableName);
		if (ii != null)
			return ii.intValue();
	//	log.warn("getAD_Table_ID - not found (" + tableName + ")");
		return 0;
	}	//	getAD_Table_ID

	/**
	 * 	Return Where clause for Record Access
	 *	@param AD_Table_ID table
	 *	@param keyColumnName (fully qualified) key column name
	 *	@return where clause or ""
	 */
	private String getRecordWhere (int AD_Table_ID, String keyColumnName)
	{
		loadRecordAccess(false);
		//
		StringBuffer sb = new StringBuffer();
		//	Role Access
		for (int i = 0; i < m_recordAccess.length; i++)
		{
			if (m_recordAccess[i].getAD_Table_ID() == AD_Table_ID)
			{
				if (sb.length() > 0)
					sb.append(" AND ");
				sb.append(keyColumnName)
					.append((m_recordAccess[i].isExclude() ? "<>" : "="))
					.append(m_recordAccess[i].getRecord_ID());
			}
		}	//	for all Table Access
		
		//	Don't ignore Privacy Access
		if (!isPersonalAccess())
		{
			String lockedIDs = MPrivateAccess.getLockedRecordWhere(AD_Table_ID, m_AD_User_ID);
			if (lockedIDs != null)
			{
				if (sb.length() > 0)
					sb.append(" AND ");
				sb.append(keyColumnName).append(lockedIDs);
			}
		}
		//
		return sb.toString();
	}	//	getRecordWhere

}	//	MRole
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.net.*;
import java.sql.*;
import java.util.*;

import org.compiere.util.*;

/**
 *	Role Model.
 *	The class is final, so that you cannot overwrite the security rules.
 *	
 *  @author Jorg Janke
 *  @version $Id: MRole.java,v 1.10 2003/11/07 06:37:56 jjanke Exp $
 */
public final class MRole extends X_AD_Role
{
	/**
	 * 	Get Default Role
	 *	@return role
	 */
	public static MRole getDefault ()
	{
		if (s_defaultRole == null && Ini.isClient())
			return getDefault (Env.getCtx(), false);
		return s_defaultRole;
	}	//	getDefault

	/**
	 * 	Get/Set Default Role.
	 * 	@see org.compiere.util.DB#loadPreferences
	 * 	@param ctx context
	 * 	@param reload if true forces load
	 *	@return role
	 */
	public static MRole getDefault (Properties ctx, boolean reload)
	{
		int AD_Role_ID = Env.getContextAsInt(ctx, "#AD_Role_ID");
		int AD_User_ID = Env.getContextAsInt(ctx, "#AD_User_ID");
		if (reload || s_defaultRole == null)
		{
			s_defaultRole = get (ctx, AD_Role_ID, AD_User_ID, reload);
		}
		else if (s_defaultRole.getAD_Role_ID() != AD_Role_ID
			|| s_defaultRole.getAD_User_ID() != AD_User_ID)
		{
			s_defaultRole = get (ctx, AD_Role_ID, AD_User_ID, reload);
		}
		return s_defaultRole;
	}	//	getDefault
	
	/**
	 * 	Get Role for User
	 * 	@param ctx, context
	 * 	@param AD_Role_ID role
	 * 	@param AD_User_ID user
	 * 	@param reload if true forces load
	 *	@return role
	 */
	public static MRole get (Properties ctx, int AD_Role_ID, int AD_User_ID, boolean reload)
	{
		s_log.info("get - AD_Role_ID=" + AD_Role_ID + ", AD_User_ID=" + AD_User_ID + ", reload=" + reload);
		String key = AD_Role_ID + "_" + AD_User_ID;
		MRole role = (MRole)s_roles.get (key);
		if (role == null || reload)
		{
			role = new MRole (ctx, AD_Role_ID);
			s_roles.put (key, role);
			if (AD_Role_ID == 0)
				role.load();			//	special Handling
			role.setAD_User_ID(AD_User_ID);
			s_log.info("get - " + role);
		}
		return role;
	}	//	get
	
	
	/**	Default Role			*/
	private static MRole			s_defaultRole = null;
	/** Role/User Cache			*/
	private static CCache			s_roles = new CCache("MRole", 5);
	/** Log						*/ 
	private static Logger			s_log = Logger.getCLogger(MRole.class);
	
	/**	Access SQL Read Write		*/
	public static final boolean		SQL_RW = true;
	/**	Access SQL Read Only		*/
	public static final boolean		SQL_RO = false;
	/**	Access SQL Fully Qualified	*/
	public static final boolean		SQL_FULLYQUALIFIED = true;
	/**	Access SQL Not Fully Qualified	*/
	public static final boolean		SQL_NOTQUALIFIED = false;

	
	
	/*************************************************************************/
	
	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param AD_Role_ID id
	 */
	public MRole (Properties ctx, int AD_Role_ID)
	{
		super(ctx, AD_Role_ID);
		if (AD_Role_ID == 0)
		{
		/**
		//	setAD_Role_ID (0);
			setIsCanExport (false);
			setIsCanReport (false);
			setIsManual (false);
			setIsPersonalAccess (false);
			setIsPersonalLock (false);
			setIsShowAcct (false);
		//	setName (null);
			setUserLevel (USERLEVEL_Client);
		//	setClientList (null);
		//	setOrgList (null);
		**/
		}		
	}	//	MRole

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MRole(Properties ctx, ResultSet rs)
	{
		super(ctx, rs);
	}	//	MRole

	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MRole[");
		sb.append(getAD_Role_ID()).append(",").append(getName())
			.append(",UserLevel=").append(getUserLevel())
			.append(",").append(getClientWhere(false))
			.append(",").append(getOrgWhere(false))
			.append("]");
		return sb.toString();
	}	//	toString

	/**
	 * 	Extended String Representation
	 *	@return extended info
	 */
	public String toStringX (Properties ctx)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(Msg.translate(ctx, "AD_Role_ID")).append("=").append(getName())
			.append(" - ").append(Msg.translate(ctx, "IsCanExport")).append("=").append(isCanExport())
			.append(" - ").append(Msg.translate(ctx, "IsCanReport")).append("=").append(isCanReport())
			.append(Env.NL).append(Env.NL);
		//
		for (int i = 0; i < m_orgAccess.length; i++)
			sb.append(m_orgAccess[i].toStringX(ctx)).append(Env.NL);
		sb.append(Env.NL);
		//
		loadTableAccess(false);
		for (int i = 0; i < m_tableAccess.length; i++)
			sb.append(m_tableAccess[i].toStringX(ctx)).append(Env.NL);
		if (m_tableAccess.length > 0)
			sb.append(Env.NL);
		//
		loadColumnAccess(false);
		for (int i = 0; i < m_columnAccess.length; i++)
			sb.append(m_columnAccess[i].toStringX(ctx)).append(Env.NL);
		if (m_columnAccess.length > 0)
			sb.append(Env.NL);
		//
		loadRecordAccess(false);
		for (int i = 0; i < m_recordAccess.length; i++)
			sb.append(m_recordAccess[i].toStringX(ctx)).append(Env.NL);
		return sb.toString();
	}	//	toStringX


	/**
	 * 	Load Access Info
	 * 	@param reload re-load from disk
	 */
	public void loadAccess (boolean reload)
	{
		loadOrgAccess(reload);
		loadTableAccess(reload);
		loadTableInfo(reload);
		loadColumnAccess(reload);
		loadRecordAccess(reload);
		if (reload)
		{
			m_windowAccess = null;
			m_processAccess = null;
			m_taskAccess = null;
			m_workflowAccess = null;
			m_formAccess = null;
		}
	}	//	loadAccess


	/*************************************************************************/

	/** User 								*/
	private int						m_AD_User_ID = -1;	

	/**	Positive List of Organizational Access		*/	
	private MRoleOrgAccess[]		m_orgAccess = null;
	/** List of Table Access						*/
	private MTableAccess[]			m_tableAccess = null;
	/** List of Column Access				*/
	private MColumnAccess[]	m_columnAccess = null;
	/** List of Record Access				*/
	private MRecordAccess[]			m_recordAccess = null;
	/** List of Dependent Record Access		*/
	private MRecordAccess[]			m_recordDependentAccess = null;
	
	/**	Table Data Access Level	*/
	private HashMap					m_tableAccessLevel = null;
	/**	Table Name				*/
	private HashMap					m_tableName = null;
	
	/**	Window Access			*/
	private HashMap		m_windowAccess = null;
	/**	Process Access			*/
	private HashMap		m_processAccess = null;
	/**	Task Access				*/
	private HashMap		m_taskAccess = null;
	/**	Workflow Access			*/
	private HashMap		m_workflowAccess = null;
	/**	Form Access				*/
	private HashMap		m_formAccess = null;

	/**
	 * 	Set Logged in user
	 *	@param AD_User_ID user requesting info
	 */
	public void setAD_User_ID(int AD_User_ID)
	{
		m_AD_User_ID = AD_User_ID;
	}	//	setAD_User_ID

	/**
	 * 	Get Logged in user
	 *	@return AD_User_ID user requesting info
	 */
	public int getAD_User_ID()
	{
		return m_AD_User_ID;
	}	//	getAD_User_ID
	
	/**
	 * 	Load Org Access
	 *	@param reload reload
	 */
	private void loadOrgAccess (boolean reload)
	{
		if (!(reload || m_orgAccess == null))
			return;
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Role_OrgAccess "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareCall(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MRoleOrgAccess(getCtx(), rs)); 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadOrgAccess", e);
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
		m_orgAccess = new MRoleOrgAccess[list.size()];
		list.toArray(m_orgAccess); 
		log.debug("loadOrgAccess - #" + m_orgAccess.length + (reload ? " - reload" : "")); 
	}	//	loadOrgAccess
	
	/**
	 * 	Load Table Access
	 *	@param reload reload
	 */
	private void loadTableAccess(boolean reload)
	{
		if (m_tableAccess != null && !reload)
			return;
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Table_Access "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareCall(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MTableAccess(getCtx(), rs)); 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadTableAccess", e);
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
		m_tableAccess = new MTableAccess[list.size()];
		list.toArray(m_tableAccess); 
		log.debug("loadTableAccess - #" + m_tableAccess.length); 
	}	//	loadTableAccess

	/**
	 * 	Load Table Access and Name
	 *	@param reload reload
	 */
	private void loadTableInfo (boolean reload)
	{
		if (m_tableAccessLevel != null && m_tableName != null && !reload)
			return;
		m_tableAccessLevel = new HashMap(300);
		m_tableName = new HashMap(300);
		PreparedStatement pstmt = null;
		String sql = "SELECT AD_Table_ID, AccessLevel, TableName "
			+ "FROM AD_Table WHERE IsActive='Y'";
		try
		{
			pstmt = DB.prepareCall(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				Integer ii = new Integer(rs.getInt(1));
				m_tableAccessLevel.put(ii, rs.getString(2));
				m_tableName.put(rs.getString(3), ii);
			} 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadTableAccessLevel", e);
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
		log.debug("loadTableAccessLevel - #" + m_tableAccessLevel.size()); 
	}	//	loadTableAccessLevel

	
	/**
	 * 	Load Column Access
	 *	@param reload reload
	 */
	private void loadColumnAccess(boolean reload)
	{
		if (m_columnAccess != null && !reload)
			return;
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Column_Access "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareCall(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MColumnAccess(getCtx(), rs)); 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadColumnAccess", e);
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
		m_columnAccess = new MColumnAccess[list.size()];
		list.toArray(m_columnAccess); 
		log.debug("loadColumnAccess - #" + m_columnAccess.length); 
	}	//	loadColumnAccess
	
	/**
	 * 	Load Record Access
	 *	@param reload reload
	 */
	private void loadRecordAccess(boolean reload)
	{
		if (!(reload || m_recordAccess == null || m_recordDependentAccess == null))
			return;
		ArrayList list = new ArrayList();
		ArrayList dependent = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Record_Access "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareCall(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				MRecordAccess ra = new MRecordAccess(getCtx(), rs);
				list.add(ra);
				if (ra.isDependentEntities())
					dependent.add(ra);
			} 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadRecordAccess", e);
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
		m_recordAccess = new MRecordAccess[list.size()];
		list.toArray(m_recordAccess);
		m_recordDependentAccess = new MRecordAccess[dependent.size()];
		dependent.toArray(m_recordDependentAccess);
		log.debug("loadRecordAccess - #" + m_recordAccess.length + " - Dependent #" + m_recordDependentAccess.length); 
	}	//	loadRecordAccess

	/*************************************************************************/
	
	/**
	 * 	Get Client Where Clause Value 
	 * 	@param rw read write
	 * 	@return "AD_Client_ID=0" or "AD_Client_ID IN(0,1)"
	 */
	public String getClientWhere (boolean rw)
	{
		loadOrgAccess(false);
		//	Unique Strings
		HashSet set = new HashSet();
		if (!rw)
			set.add("0");
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
			set.add(String.valueOf(m_orgAccess[i].getAD_Client_ID()));
		//
		StringBuffer sb = new StringBuffer();
		Iterator it = set.iterator();
		boolean oneOnly = true;
		while (it.hasNext())
		{
			if (sb.length() > 0)
			{
				sb.append(",");
				oneOnly = false;
			}
			sb.append(it.next());
		}
		if (oneOnly)
		{
			if (sb.length() > 0)
				return "AD_Client_ID=" + sb.toString();
			else
			{
				log.error("getClientWhere - No Access Org records");
				return "AD_Client_ID=-1";	//	No Access Record
			}
		}
		return "AD_Client_ID IN(" + sb.toString() + ")";
	}	//	getClientWhereValue
	
	/**
	 * 	Access to Client
	 *	@param AD_Client_ID client
	 *	@param rw read write access
	 *	@return true if access
	 */
	public boolean isClientAccess(int AD_Client_ID, boolean rw)
	{
		if (AD_Client_ID == 0 && !rw)	//	can always read System
			return true;
		loadOrgAccess(false);
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
		{
			if (m_orgAccess[i].getAD_Client_ID() == AD_Client_ID)
				return true;
		}
		return false;
	}	//	isClientAccess
	
	/**
	 * 	Get Org Where Clause Value 
	 * 	@param rw read write
	 * 	@return "AD_Org_ID=0" or "AD_Org_ID IN(0,1)"
	 */
	public String getOrgWhere (boolean rw)
	{
		loadOrgAccess(false);
		//	Unique Strings
		HashSet set = new HashSet();
		if (!rw)
			set.add("0");
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
			set.add(String.valueOf(m_orgAccess[i].getAD_Org_ID()));
		//
		StringBuffer sb = new StringBuffer();
		Iterator it = set.iterator();
		boolean oneOnly = true;
		while (it.hasNext())
		{
			if (sb.length() > 0)
			{
				sb.append(",");
				oneOnly = false;
			}
			sb.append(it.next());
		}
		if (oneOnly)
		{
			if (sb.length() > 0)
				return "AD_Org_ID=" + sb.toString();
			else
			{
				log.error("getOrgWhere - No Access Org records");
				return "AD_Org_ID=-1";	//	No Access Record
			}
		}		
		return "AD_Org_ID IN(" + sb.toString() + ")";
	}	//	getOrgWhereValue
	
	/**
	 * 	Access to Org
	 *	@param AD_Org_ID org
	 *	@param rw read write access
	 *	@return true if access
	 */
	public boolean isOrgAccess(int AD_Org_ID, boolean rw)
	{
		if (AD_Org_ID == 0 && !rw)		//	can always read common org
			return true;
		loadOrgAccess(false);
		
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
		{
			if (m_orgAccess[i].getAD_Org_ID() == AD_Org_ID)
				return true;
		}
		return false;
	}	//	isOrgAccess


	/**
	 * 	Can Report on table
	 *	@param AD_Table_ID table
	 *	@return true if access
	 */
	public boolean isCanReport (int AD_Table_ID)
	{
		if (!isCanReport())						//	Role Level block
		{
			log.warn ("isCanReport - Role denied");
			return false;
		}
		if (!isTableAccess(AD_Table_ID, true))	//	No R/O Access to Table
			return false;
		//
		boolean canReport = true;
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (!MTableAccess.ACCESSTYPERULE_Reporting.equals(m_tableAccess[i].getAccessTypeRule()))
				continue;
			if (m_tableAccess[i].isExclude())		//	Exclude
			{
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					canReport = m_tableAccess[i].isCanReport();
					log.debug ("isCanReport - Exclude " + AD_Table_ID + " - " + canReport);
					return canReport;
				}
			}
			else									//	Include
			{
				canReport = false;
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					canReport = m_tableAccess[i].isCanReport();
					log.debug ("isCanReport - Include " + AD_Table_ID + " - " + canReport);
					return canReport;
				}
			}
		}	//	for all Table Access
		log.debug ("isCanReport - " + AD_Table_ID + " - " + canReport);
		return canReport;
	}	//	isCanReport
	
	/**
	 * 	Can Export Table
	 *	@param AD_Table_ID
	 *	@return true if access
	 */
	public boolean isCanExport (int AD_Table_ID)
	{
		if (!isCanExport())						//	Role Level block
		{
			log.warn ("isCanExport - Role denied");
			return false;
		}
		if (!isTableAccess(AD_Table_ID, true))	//	No R/O Access to Table
			return false;
		if (!isCanReport (AD_Table_ID))
			return false;
		//
		boolean canExport = true;
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (!MTableAccess.ACCESSTYPERULE_Exporting.equals(m_tableAccess[i].getAccessTypeRule()))
				continue;
			if (m_tableAccess[i].isExclude())		//	Exclude
			{
				canExport = m_tableAccess[i].isCanExport();
				log.debug ("isCanExport - Exclude " + AD_Table_ID + " - " + canExport);
				return canExport;
			}
			else									//	Include
			{
				canExport = false;
				canExport = m_tableAccess[i].isCanExport();
				log.debug ("isCanExport - Include " + AD_Table_ID + " - " + canExport);
				return canExport;
			}
		}	//	for all Table Access
		log.debug ("isCanExport - " + AD_Table_ID + " - " + canExport);
		return canExport;
	}	//	isCanExport

	/**
	 * 	Access to Table
	 *	@param AD_Table_ID table
	 *	@param ro check read only access otherwise read write access level
	 *	@return has RO/RW access to table
	 */
	public boolean isTableAccess (int AD_Table_ID, boolean ro)
	{
		if (!isTableAccessLevel (AD_Table_ID, ro))	//	Role Based Access
			return false;
		loadTableAccess(false);
		//
		boolean hasAccess = true;	//	assuming exclusive rule
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (!MTableAccess.ACCESSTYPERULE_Accessing.equals(m_tableAccess[i].getAccessTypeRule()))
				continue;
			if (m_tableAccess[i].isExclude())		//	Exclude
			//	If you Exclude Access to a table and select Read Only, 
			//	you can only read data (otherwise no access).
			{
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (ro)
						hasAccess = m_tableAccess[i].isReadOnly();
					else
						hasAccess = false;
					log.debug("isTableAccess - Exclude AD_Table_ID=" + AD_Table_ID 
						+ " (ro="  + ro + ",TableAccessRO=" + m_tableAccess[i].isReadOnly() + ") = " + hasAccess);
					return hasAccess;
				}
			}
			else								//	Include
			//	If you Include Access to a table and select Read Only, 
			//	you can only read data (otherwise full access).
			{
				hasAccess = false;
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (!ro)	//	rw only if not r/o
						hasAccess = !m_tableAccess[i].isReadOnly();
					else
						hasAccess = true;
					log.debug("isTableAccess - Include AD_Table_ID=" + AD_Table_ID 
						+ " (ro="  + ro + ",TableAccessRO=" + m_tableAccess[i].isReadOnly() + ") = " + hasAccess);
					return hasAccess;
				}
			}
		}	//	for all Table Access
		if (!hasAccess)
			log.debug("isTableAccess - AD_Table_ID=" + AD_Table_ID 
				+ "(ro="  + ro + ") = " + hasAccess);
		return hasAccess;
	}	//	isTableAccess

	/**
	 * 	Access to Table based on Role User Level Table Access Level
	 *	@param AD_Table_ID table
	 *	@param ro check read only access otherwise read write access level
	 *	@return has RO/RW access to table
	 */
	public boolean isTableAccessLevel (int AD_Table_ID, boolean ro)
	{
		if (ro)				//	role can always read
			return true;
		//
		loadTableInfo(false);
		//	AccessLevel
		//		1 = Org - 2 = Client - 4 = System
		//		3 = Org+Client - 6 = Client+System - 7 = All
		String accessLevel = (String)m_tableAccessLevel.get(new Integer(AD_Table_ID));
		if (accessLevel == null)
		{
			log.debug("isTableAccessLevel - NO - No AccessLevel - AD_Table_ID=" + AD_Table_ID);
			return false;
		}
		//	Access to all User Levels
		if (accessLevel.equals(X_AD_Table.ACCESSLEVEL_All))
			return true;
		//	User Level = SCO
		String userLevel = getUserLevel();
		//	
		if (userLevel.charAt(0) == 'S'
			&& (accessLevel.equals(X_AD_Table.ACCESSLEVEL_SystemOnly) 
				|| accessLevel.equals(X_AD_Table.ACCESSLEVEL_SystemPlusClient)))
			return true;
		if (userLevel.charAt(1) == 'C'
			&& (accessLevel.equals(X_AD_Table.ACCESSLEVEL_ClientOnly) 
				|| accessLevel.equals(X_AD_Table.ACCESSLEVEL_SystemPlusClient)))
			return true;
		if (userLevel.charAt(2) == 'O'
			&& (accessLevel.equals(X_AD_Table.ACCESSLEVEL_Organization) 
				|| accessLevel.equals(X_AD_Table.ACCESSLEVEL_ClientPlusOrganization)))
			return true;
		log.debug("isTableAccessLevel - NO - AD_Table_ID=" + AD_Table_ID 
			+ ", UserLebel=" + userLevel + ", AccessLevel=" + accessLevel);
		return false;
	}	//	isTableAccessLevel


	/**
	 * 	Access to Column
	 *	@param AD_Table_ID table
	 *	@param AD_Column_ID column
	 *	@param ro read only
	 *	@return true if access
	 */
	public boolean isColumnAccess (int AD_Table_ID, int AD_Column_ID, boolean ro)
	{
		if (!isTableAccess(AD_Table_ID, ro))		//	No Access to Table		
			return false;
		loadColumnAccess(false);
		
		boolean retValue = true;		//	assuming exclusive
		for (int i = 0; i < m_columnAccess.length; i++)
		{
			if (m_columnAccess[i].isExclude())		//	Exclude
			//	If you Exclude Access to a column and select Read Only, 
			//	you can only read data (otherwise no access).
			{
				if (m_columnAccess[i].getAD_Table_ID() == AD_Table_ID 
					&& m_columnAccess[i].getAD_Column_ID() == AD_Column_ID)
				{
					if (ro)		//	just R/O Access requested
						retValue = m_columnAccess[i].isReadOnly();
					else
						retValue = false;
					if (!retValue)
						log.debug("isColumnAccess - Exclude AD_Table_ID=" + AD_Table_ID + ", AD_Column_ID=" + AD_Column_ID 
							+ " (ro="  + ro + ",ColumnAccessRO=" + m_columnAccess[i].isReadOnly() + ") = " + retValue);
					return retValue;
				}
			}
			else								//	Include
			//	If you Include Access to a column and select Read Only, 
			//	you can only read data (otherwise full access).
			{
				if (m_columnAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					retValue = false;
					if (m_columnAccess[i].getAD_Column_ID() == AD_Column_ID)
					{
						if (!ro)	//	rw only if not r/o
							retValue = !m_columnAccess[i].isReadOnly();
						else
							retValue = true;
						if (!retValue)
							log.debug("isColumnAccess - Include AD_Table_ID=" + AD_Table_ID + ", AD_Column_ID=" + AD_Column_ID 
								+ " (ro="  + ro + ",ColumnAccessRO=" + m_columnAccess[i].isReadOnly() + ") = " + retValue);
						return retValue;
					}
				}	//	same table
			}	//	include
		}	//	for all Table Access
		if (!retValue)
			log.debug("isColumnAccess - AD_Table_ID=" + AD_Table_ID + ", AD_Column_ID=" + AD_Column_ID 
				+ " (ro="  + ro + ") = " + retValue);
		return retValue;
	}	//	isColumnAccess

	/**
	 *	Access to Record
	 *	@param AD_Table_ID table
	 *	@param Record_ID
	 *	@param ro
	 *	@return
	 */
	public boolean isRecordAccess (int AD_Table_ID, int Record_ID, boolean ro)
	{
		if (!isTableAccess(AD_Table_ID, ro))		//	No Access to Table
			return false;
		loadRecordAccess(false);
		
		boolean negativeList = true;
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (m_recordAccess[i].isExclude())		//	Exclude
			//	If you Exclude Access to a column and select Read Only, 
			//	you can only read data (otherwise no access).
			{
				if (m_recordAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (ro)
						return m_tableAccess[i].isReadOnly();
					else
						return false;
				}
			}
			else								//	Include
			//	If you Include Access to a column and select Read Only, 
			//	you can only read data (otherwise full access).
			{
				negativeList = false;
				if (m_recordAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (!ro)	//	rw only if not r/o
						return !m_tableAccess[i].isReadOnly();
					else
						return true;
				}
			}
		}	//	for all Table Access
		return negativeList;
	}	//	isRecordAccess

	/**
	 * 	Get Window Access
	 *	@param AD_Window_ID window
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getWindowAccess (int AD_Window_ID)
	{
		if (m_windowAccess == null)
		{
			m_windowAccess = new HashMap(100);
			String sql = "SELECT AD_Window_ID, IsReadWrite FROM AD_Window_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareCall(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_windowAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getWindowAccess", e);
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
			log.debug("getWindowAccess #" + m_windowAccess.size());
		}	//	reload
		Boolean retValue = (Boolean)m_windowAccess.get(new Integer(AD_Window_ID));
	//	log.debug("getWindowAccess - AD_Window_ID=" + AD_Window_ID + " - " + retValue);
		return retValue;
	}	//	getWindowAccess

	/**
	 * 	Get Process Access
	 *	@param AD_Process_ID process
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getProcessAccess (int AD_Process_ID)
	{
		if (m_processAccess == null)
		{
			m_processAccess = new HashMap(50);
			String sql = "SELECT AD_Process_ID, IsReadWrite FROM AD_Process_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareCall(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_processAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getProcessAccess", e);
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
		}	//	reload
		return (Boolean)m_processAccess.get(new Integer(AD_Process_ID));
	}	//	getProcessAccess

	/**
	 * 	Get Task Access
	 *	@param AD_Task_ID task
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getTaskAccess (int AD_Task_ID)
	{
		if (m_taskAccess == null)
		{
			m_taskAccess = new HashMap(10);
			String sql = "SELECT AD_Task_ID, IsReadWrite FROM AD_Task_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareCall(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_taskAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getTaskAccess", e);
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
		}	//	reload
		return (Boolean)m_taskAccess.get(new Integer(AD_Task_ID));
	}	//	getTaskAccess

	/**
	 * 	Get Form Access
	 *	@param AD_Form_ID form
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getFormAccess (int AD_Form_ID)
	{
		if (m_formAccess == null)
		{
			m_formAccess = new HashMap(20);
			String sql = "SELECT AD_Form_ID, IsReadWrite FROM AD_Form_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareCall(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_formAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getFormAccess", e);
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
		}	//	reload
		return (Boolean)m_formAccess.get(new Integer(AD_Form_ID));
	}	//	getTaskAccess

	/**
	 * 	Get Workflow Access
	 *	@param AD_Workflow_ID workflow
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getWorkflowAccess (int AD_Workflow_ID)
	{
		if (m_workflowAccess == null)
		{
			m_workflowAccess = new HashMap(20);
			String sql = "SELECT AD_Workflow_ID, IsReadWrite FROM AD_Workflow_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareCall(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_workflowAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getWorkflowAccess", e);
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
		}	//	reload
		return (Boolean)m_workflowAccess.get(new Integer(AD_Workflow_ID));
	}	//	getTaskAccess

	/*************************************************************************

	/**
	 *	Appends where clause to SQL statement for Table
	 *
	 *	@param SQL			existing SQL statement
	 *	@param TableNameIn	Table Name or list of table names AAA, BBB or AAA a, BBB b
	 *	@param fullyQualified	fullyQualified names
	 *	@param rw			if false, includes System Data
	 *	@return				updated SQL statement
	 */
	public String addAccessSQL (String SQL, String TableNameIn, 
		boolean fullyQualified, boolean rw)
	{
		StringBuffer retSQL = new StringBuffer();

		//	Cut off last ORDER BY clause
		String orderBy = "";
		int posOrder = SQL.lastIndexOf(" ORDER BY ");
		if (posOrder != -1)
		{
			orderBy = SQL.substring(posOrder);
			retSQL.append(SQL.substring(0, posOrder));
		}
		else
			retSQL.append(SQL);

		//	Parse SQL
		AccessSqlParser asp = new AccessSqlParser(retSQL.toString());
		AccessSqlParser.TableInfo[] ti = asp.getTableInfo(asp.getMainSqlIndex()); 

		//  Do we have to add WHERE or AND
		if (asp.getMainSql().indexOf(" WHERE ") == -1)
			retSQL.append(" WHERE ");
		else
			retSQL.append(" AND ");

		//	Use First Table
		String tableName = "";
		if (ti.length > 0)
		{
			tableName = ti[0].getSynonym();
			if (tableName.length() == 0)
				tableName = ti[0].getTableName();
		}
		if (!tableName.equals(TableNameIn) && TableNameIn != null)
		{
			String msg = "addAccessSQL - TableName not correctly parsed - TableNameIn=" 
				+ TableNameIn + " - " + asp;
			if (ti.length > 0)
				msg += " - #1 " + ti[0]; 
			msg += "\n = " + SQL;
			log.error(msg);
			Trace.printStack();
			tableName = TableNameIn;
		}

		//	Client Access
		if (fullyQualified)
			retSQL.append(tableName).append(".");
		retSQL.append(getClientWhere(rw));
		
		//	Org Access
		retSQL.append(" AND ");
		if (fullyQualified)
			retSQL.append(tableName).append(".");
		retSQL.append(getOrgWhere(rw));

			
		//	** Data Access	**
		for (int i = 0; i < ti.length; i++)
		{
			String TableName = ti[i].getTableName();
			int AD_Table_ID = getAD_Table_ID (TableName);
			//	Data Table Access
			if (AD_Table_ID != 0 && !isTableAccess(AD_Table_ID, !rw))
			{
				retSQL.append(" AND 1=3");	//	prevent access at all
				log.debug("addAccessSQL - no access to AD_Table_ID=" + AD_Table_ID 
					+ " - " + TableName + " - " + retSQL);
				break;	//	no need to check further 
			}
			
			//	Data Column Access
	
	
		
			//	Data Record Access
			String keyColumnName = "";
			if (fullyQualified)
			{
				keyColumnName = ti[i].getSynonym();	//	table synonym
				if (keyColumnName.length() == 0)
					keyColumnName = TableName;
				keyColumnName += ".";
			}
			keyColumnName += TableName + "_ID";	//	derived from table
	
		//	log.debug("addAccessSQL - " + TableName + "(" + AD_Table_ID + ") " + keyColumnName);
			String recordWhere = getRecordWhere (AD_Table_ID, keyColumnName);
			if (recordWhere.length() > 0)
			{
				retSQL.append(" AND ").append(recordWhere);
				if (Log.isTraceLevel(9))
					log.debug("addAccessSQL - adding record access - " + recordWhere);
			}
		}	//	for all table info
		
		//	Dependent Records (only for main SQL)
		String mainSql = asp.getMainSql();
		loadRecordAccess(false);
		for (int i = 0; i < m_recordDependentAccess.length; i++)
		{
			String columnName = m_recordDependentAccess[i].getKeyColumnName(asp.getTableInfo(asp.getMainSqlIndex()) );
			if (columnName == null)
				continue;	//	no key column
			int posColumn = mainSql.indexOf(columnName);
			if (posColumn == -1)
				continue;
			//	we found the column name - make sure it's a clumn name
			char charCheck = mainSql.charAt(posColumn-1);	//	before
			if (!(charCheck == ',' || charCheck == '.' || charCheck == ' ' || charCheck == '('))
				continue;
			charCheck = mainSql.charAt(posColumn+columnName.length());	//	after
			if (!(charCheck == ',' || charCheck == ' ' || charCheck == ')'))
				continue;
				
			//	*** we found the column in the main query
			log.debug("DEPENDENT  " + columnName + " - " + m_recordDependentAccess[i]);
			StringBuffer where = new StringBuffer();
			where.append(getDependentRecordWhereColumn (mainSql, columnName))
				.append((m_recordDependentAccess[i].isExclude() ? "<>" : "="))
				.append(m_recordDependentAccess[i].getRecord_ID());
			if (Log.isTraceLevel(9))
				log.debug("addAccessSQL - adding dependent access - " + where);
			retSQL.append(" AND ").append(where);			
		}	//	for all dependent records
		
		//
		retSQL.append(orderBy);
		if (Log.isTraceLevel(10))
			log.debug("addAccessSQL - " + retSQL);
		return retSQL.toString();
	}	//	addAccessSQL

	/**
	 * 	Get Dependent Record Where clause
	 *	@param mainSql sql to examine
	 *	@param columnName columnName
	 *	@return where clause column "x.columnName"
	 */
	private String getDependentRecordWhereColumn (String mainSql, String columnName)
	{
		String retValue = columnName;	//	if nothing else found
		int index = mainSql.indexOf(columnName);
		//	see if there are table synonym
		int offset = index - 1;
		char c = mainSql.charAt(offset);
		if (c == '.')
		{
			StringBuffer sb = new StringBuffer();
			while (c != ' ' && c != ',' && c != '(')	//	delimeter
			{
				sb.insert(0, c);
				c = mainSql.charAt(--offset);
			}
			sb.append(columnName);
			return sb.toString();
		}
		return retValue;
	}	//	getDependentRecordWhereColumn



	/**
	 *	UPADATE - Can I Update the record
	 *  Access error info (AccessTableNoUpdate) is saved in the log
	 * 
	 *  @see org.compiere.model.MTable#save(boolean)
	 *
	 *  @param ctx comntext to derive client/org/user level
	 *	@param	WindowNo	number of the current window to retrieve context
	 *	@return	true if you can update
	 */
	public boolean canUpdate (int AD_Client_ID, int AD_Org_ID, 
		int AD_Table_ID, boolean createError)
	{
		String userLevel = getUserLevel();	//	Format 'SCO'

		if (userLevel.indexOf("S") != -1)		//	System can change anything
			return true;

		boolean	retValue = true;
		String whatMissing = "";

		//	System == Client=0 & Org=0
		if (AD_Client_ID == 0 && AD_Org_ID == 0
			&& userLevel.charAt(0) != 'S')
		{
			retValue = false;
			whatMissing += "S";
		}

		//	Client == Client!=0 & Org=0
		else if (AD_Client_ID != 0 && AD_Org_ID == 0
			&& userLevel.charAt(1) != 'C')
		{
			retValue = false;
			whatMissing += "C";
		}

		//	Organization == Client!=0 & Org!=0
		else if (AD_Client_ID != 0 && AD_Org_ID != 0
			&& userLevel.charAt(2) != 'O')
		{
			retValue = false;
			whatMissing += "O";
		}

		//	Data Access
		//	get Table.IsSecurityEnabled
		//	if yes: get UserAcess info => Where Table.TableKey in (List)
		//									and Access=r/w

		if (!retValue && createError)
		{
			Log.saveError("AccessTableNoUpdate",
				"AD_Client_ID=" + AD_Client_ID + ", AD_Org_ID=" + AD_Org_ID + ", UserLevel=" + userLevel
				+ " => missing=" + whatMissing);
			log.error ("canUpdate - " + toString());
		}
		return retValue;
	}	//	canUpdate

	/**
	 *	VIEW - Can I view record in Table with given TableLevel
	 *  <code>
	 *	TableLevel			S__ 100		4	System info
	 *						SCO	111		7	System shared info
	 *						SC_ 110		6	System/Client info
	 *						_CO	011		3	Client shared info
	 *						_C_	011		2	Client shared info
	 *						__O	001		1	Organization info
	 *  </code>
	 * 
	 * 	@see org.compiere.model.MTabVO#loadTabDetails (MTabVO, ResultSet)
	 *  @param ctx	context
	 *	@param TableLevel	AccessLevel
	 *	@param forInsert	true for Insert, false for View
	 *	@return	true/false
	 *  Access error info (AccessTableNoUpdate, AccessTableNoView) is saved in the log
	 */
	public boolean canView(Properties ctx, String TableLevel)
	{
		String userLevel = getUserLevel();	//	Format 'SCO'

		boolean retValue = true;

		//	7 - All
		if (X_AD_Table.ACCESSLEVEL_All.equals(TableLevel))
			retValue = true;
			 
		//	4 - System data requires S
		else if (X_AD_Table.ACCESSLEVEL_SystemOnly.equals(TableLevel) 
			&& userLevel.charAt(0) != 'S')
			retValue = false;

		//	2 - Client data requires C
		else if (X_AD_Table.ACCESSLEVEL_ClientOnly.equals(TableLevel) 
			&& userLevel.charAt(1) != 'C')
			retValue = false;

		//	1 - Organization data requires O
		else if (X_AD_Table.ACCESSLEVEL_Organization.equals(TableLevel) 
			&& userLevel.charAt(2) != 'O')
			retValue = false;

		//	3 - Client Shared requires C or O
		else if (X_AD_Table.ACCESSLEVEL_ClientPlusOrganization.equals(TableLevel)
			&& (!(userLevel.charAt(1) == 'C' || userLevel.charAt(2) == 'O')) )
				retValue = false;

		//	6 - System/Client requires S or C
		else if (X_AD_Table.ACCESSLEVEL_SystemPlusClient.equals(TableLevel)
			&& (!(userLevel.charAt(0) == 'S' || userLevel.charAt(1) == 'C')) )
			retValue = false;

		if (retValue)
			return retValue;

		//  Notification
		/**
		if (forInsert)
			Log.saveError("AccessTableNoUpdate",
				"(Required=" + TableLevel + "("
				+ getTableLevelString(Env.getAD_Language(ctx), TableLevel)
				+ ") != UserLevel=" + userLevel);
		else
		**/
			Log.saveError("AccessTableNoView",
				"Required=" + TableLevel + "("
				+ getTableLevelString(Env.getAD_Language(ctx), TableLevel)
				+ ") != UserLevel=" + userLevel);
		log.error ("canView - " + toString());
		return retValue;
	}	//	canView


	/**
	 *	Returns clear text String of TableLevel
	 *  @param AD_Language language
	 *  @param TableLevel level
	 *  @return info
	 */
	private String getTableLevelString (String AD_Language, String TableLevel)
	{
		String level = TableLevel + "??";
		if (TableLevel.equals("1"))
			level = "AccessOrg";
		else if (TableLevel.equals("2"))
			level = "AccessClient";
		else if (TableLevel.equals("3"))
			level = "AccessClientOrg";
		else if (TableLevel.equals("4"))
			level = "AccessSystem";
		else if (TableLevel.equals("6"))
			level = "AccessSystemClient";
		else if (TableLevel.equals("7"))
			level = "AccessShared";

		return Msg.getMsg(AD_Language, level);
	}	//	getTableLevelString

	/**
	 * 	Get Table ID from name
	 *	@param tableName table name
	 *	@return AD_Table_ID or 0
	 */
	private int getAD_Table_ID (String tableName)
	{
		loadTableInfo(false);
		Integer ii = (Integer)m_tableName.get(tableName);
		if (ii != null)
			return ii.intValue();
	//	log.warn("getAD_Table_ID - not found (" + tableName + ")");
		return 0;
	}	//	getAD_Table_ID

	/**
	 * 	Return Where clause for Record Access
	 *	@param AD_Table_ID table
	 *	@param keyColumnName (fully qualified) key column name
	 *	@return where clause or ""
	 */
	private String getRecordWhere (int AD_Table_ID, String keyColumnName)
	{
		loadRecordAccess(false);
		//
		StringBuffer sb = new StringBuffer();
		//	Role Access
		for (int i = 0; i < m_recordAccess.length; i++)
		{
			if (m_recordAccess[i].getAD_Table_ID() == AD_Table_ID)
			{
				if (sb.length() > 0)
					sb.append(" AND ");
				sb.append(keyColumnName)
					.append((m_recordAccess[i].isExclude() ? "<>" : "="))
					.append(m_recordAccess[i].getRecord_ID());
			}
		}	//	for all Table Access
		
		//	Don't ignore Privacy Access
		if (!isPersonalAccess())
		{
			String lockedIDs = MPrivateAccess.getLockedRecordWhere(AD_Table_ID, m_AD_User_ID);
			if (lockedIDs != null)
			{
				if (sb.length() > 0)
					sb.append(" AND ");
				sb.append(keyColumnName).append(lockedIDs);
			}
		}
		//
		return sb.toString();
	}	//	getRecordWhere

}	//	MRole
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.sql.*;
import java.util.*;

import org.compiere.util.*;

/**
 *	Role Model.
 *	Includes AD_User runtime info for Personal Access
 *	The class is final, so that you cannot overwrite the security rules.
 *	
 *  @author Jorg Janke
 *  @version $Id: MRole.java,v 1.27 2004/09/10 02:53:28 jjanke Exp $
 */
public final class MRole extends X_AD_Role
{
	/**
	 * 	Get Default (Client) Role
	 *	@return role
	 */
	public static MRole getDefault ()
	{
		if (s_defaultRole == null && Ini.isClient())
			return getDefault (Env.getCtx(), false);
		return s_defaultRole;
	}	//	getDefault

	/**
	 * 	Get/Set Default Role.
	 * 	@param ctx context
	 * 	@param reload if true forces load
	 *	@return role
	 *	@see org.compiere.util.DB#loadPreferences
	 */
	public static MRole getDefault (Properties ctx, boolean reload)
	{
		int AD_Role_ID = Env.getContextAsInt(ctx, "#AD_Role_ID");
		int AD_User_ID = Env.getContextAsInt(ctx, "#AD_User_ID");
		if (!Ini.isClient())	//	none for Server
			AD_User_ID = 0;
		if (reload || s_defaultRole == null)
		{
			s_defaultRole = get (ctx, AD_Role_ID, AD_User_ID, reload);
		}
		else if (s_defaultRole.getAD_Role_ID() != AD_Role_ID
			|| s_defaultRole.getAD_User_ID() != AD_User_ID)
		{
			s_defaultRole = get (ctx, AD_Role_ID, AD_User_ID, reload);
		}
		return s_defaultRole;
	}	//	getDefault
	
	/**
	 * 	Get Role for User
	 * 	@param ctx context
	 * 	@param AD_Role_ID role
	 * 	@param AD_User_ID user
	 * 	@param reload if true forces load
	 *	@return role
	 */
	public static MRole get (Properties ctx, int AD_Role_ID, int AD_User_ID, boolean reload)
	{
		s_log.info("get - AD_Role_ID=" + AD_Role_ID + ", AD_User_ID=" + AD_User_ID + ", reload=" + reload);
		String key = AD_Role_ID + "_" + AD_User_ID;
		MRole role = (MRole)s_roles.get (key);
		if (role == null || reload)
		{
			role = new MRole (ctx, AD_Role_ID);
			s_roles.put (key, role);
			if (AD_Role_ID == 0)
				role.load();			//	special Handling
			role.setAD_User_ID(AD_User_ID);
			role.loadAccess(reload);
			s_log.info("get - " + role);
		}
		return role;
	}	//	get

	/**
	 * 	Get Role (cached).
	 * 	Did not set user - so no access loaded
	 * 	@param ctx context
	 * 	@param AD_Role_ID role
	 *	@return role
	 */
	public static MRole get (Properties ctx, int AD_Role_ID)
	{
		String key = String.valueOf(AD_Role_ID);
		MRole role = (MRole)s_roles.get (key);
		if (role == null)
		{
			role = new MRole (ctx, AD_Role_ID);
			s_roles.put (key, role);
			if (AD_Role_ID == 0)	//	System Role
				role.load();		//	special Handling
		}
		return role;
	}	//	get
	
	/**
	 * 	Get Roles Of Client
	 *	@param ctx context
	 *	@return roles of client
	 */
	public static MRole[] getOfClient (Properties ctx)
	{
		String sql = "SELECT * FROM AD_Role WHERE AD_Client_ID=?";
		ArrayList list = new ArrayList ();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql);
			pstmt.setInt (1, Env.getAD_Client_ID(ctx));
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add (new MRole(ctx, rs));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.error ("getOfClient", e);
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
		MRole[] retValue = new MRole[list.size ()];
		list.toArray (retValue);
		return retValue;
	}	//	getOfClient
	
	/**	Default Role			*/
	private static MRole			s_defaultRole = null;
	/** Role/User Cache			*/
	private static CCache			s_roles = new CCache("AD_Role", 5);
	/** Log						*/ 
	private static Logger			s_log = Logger.getCLogger(MRole.class);
	
	/**	Access SQL Read Write		*/
	public static final boolean		SQL_RW = true;
	/**	Access SQL Read Only		*/
	public static final boolean		SQL_RO = false;
	/**	Access SQL Fully Qualified	*/
	public static final boolean		SQL_FULLYQUALIFIED = true;
	/**	Access SQL Not Fully Qualified	*/
	public static final boolean		SQL_NOTQUALIFIED = false;

	/**	The AD_User_ID of the SuperUser				*/
	public static final int			SUPERUSER_USER_ID = 100;
	/**	The AD_User_ID of the System Administrator	*/
	public static final int			SYSTEM_USER_ID = 0;
	
	
	/**************************************************************************
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param AD_Role_ID id
	 */
	public MRole (Properties ctx, int AD_Role_ID)
	{
		super (ctx, AD_Role_ID);
		//	ID=0 == System Administrator
		if (AD_Role_ID == 0)
		{
		//	setName (null);
			setIsCanExport (true);
			setIsCanReport (true);
			setIsManual (false);
			setIsPersonalAccess (false);
			setIsPersonalLock (false);
			setIsShowAcct (false);
			setIsAccessAllOrgs(false);
			setUserLevel (USERLEVEL_Organization);
		}
	}	//	MRole

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MRole(Properties ctx, ResultSet rs)
	{
		super(ctx, rs);
	}	//	MRole

	
	/**
	 * 	After Save
	 *	@param newRecord new
	 *	@param success success
	 *	@return success
	 */
	protected boolean afterSave (boolean newRecord, boolean success)
	{
		if (newRecord)
		{
			//	Add Role to SuperUser
			MUserRoles su = new MUserRoles(getCtx(), SUPERUSER_USER_ID, getAD_Role_ID());
			su.save();
			//	Add Role to User
			if (getCreatedBy() != SUPERUSER_USER_ID)
			{
				MUserRoles ur = new MUserRoles(getCtx(), getCreatedBy(), getAD_Role_ID());
				ur.save();
			}
			updateAccessRecords();
		}
		//
		else if (is_ValueChanged("UserLevel"))
			updateAccessRecords();
		return success;
	}	//	afterSave

	/**
	 * 	Create Access Records
	 *	@param newRecord new record
	 *	@return info
	 */
	public String updateAccessRecords ()
	{
		if (isManual())
			return "-";
		
		String roleClientOrgUser = getAD_Role_ID() + ","
			+ getAD_Client_ID() + "," + getAD_Org_ID() + ",'Y', SysDate," 
			+ getUpdatedBy() + ", SysDate," + getUpdatedBy() 
			+ ",'Y' ";	//	IsReadWrite
		
		String sqlWindow = "INSERT INTO AD_Window_Access "
			+ "(AD_Window_ID, AD_Role_ID,"
			+ " AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy,IsReadWrite) "
			+ "SELECT DISTINCT w.AD_Window_ID, " + roleClientOrgUser
			+ "FROM AD_Window w"
			+ " INNER JOIN AD_Tab t ON (w.AD_Window_ID=t.AD_Window_ID)"
			+ " INNER JOIN AD_Table tt ON (t.AD_Table_ID=tt.AD_Table_ID) "
			+ "WHERE t.SeqNo=(SELECT MIN(SeqNo) FROM AD_Tab xt "	// only check first tab
				+ "WHERE xt.AD_Window_ID=w.AD_Window_ID)"
			+ "AND tt.AccessLevel IN ";
		
		String sqlProcess = "INSERT INTO AD_Process_Access "
			+ "(AD_Process_ID, AD_Role_ID,"
			+ " AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy,IsReadWrite) "
			+ "SELECT DISTINCT p.AD_Process_ID, " + roleClientOrgUser
			+ "FROM AD_Process p "
			+ "WHERE AccessLevel IN ";

		String sqlForm = "INSERT INTO AD_Form_Access "
			+ "(AD_Form_ID, AD_Role_ID," 
			+ " AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy,IsReadWrite) "
			+ "SELECT f.AD_Form_ID, " + roleClientOrgUser
			+ "FROM AD_Form f "
			+ "WHERE AccessLevel IN ";

		String sqlWorkflow = "INSERT INTO AD_WorkFlow_Access "
			+ "(AD_WorkFlow_ID, AD_Role_ID,"
			+ " AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy,IsReadWrite) "
			+ "SELECT w.AD_WorkFlow_ID, " + roleClientOrgUser
			+ "FROM AD_WorkFlow w "
			+ "WHERE AccessLevel IN ";

		/**
		 *	Fill AD_xx_Access
		 *	---------------------------------------------------------------------------
		 *	SCO# Levels			S__ 100		4	System info
		 *						SCO	111		7	System shared info
		 *						SC_ 110		6	System/Client info
		 *						_CO	011		3	Client shared info
		 *						_C_	011		2	Client
		 *						__O	001		1	Organization info
		 *	Roles:
		 *		S		4,7,6
		 *		_CO		7,6,3,2,1
		 *		__O		3,1,7
		 */
		String accessLevel = null;
		if (USERLEVEL_System.equals(getUserLevel()))
			accessLevel = "('4','7','6')";
		else if (USERLEVEL_Client.equals(getUserLevel()))
			accessLevel = "('7','6','3','2')";
		else if (USERLEVEL_ClientPlusOrganization.equals(getUserLevel()))
			accessLevel = "('7','6','3','2','1')";
		else //	if (USERLEVEL_Organization.equals(getUserLevel()))
			accessLevel = "('3','1','7')";
		//
		String whereDel = " WHERE AD_Role_ID=" + getAD_Role_ID();
		//
		int winDel = DB.executeUpdate("DELETE AD_Window_Access" + whereDel);
		int win = DB.executeUpdate(sqlWindow + accessLevel);
		int procDel = DB.executeUpdate("DELETE AD_Process_Access" + whereDel);
		int proc = DB.executeUpdate(sqlProcess + accessLevel);
		int formDel = DB.executeUpdate("DELETE AD_Form_Access" + whereDel);
		int form = DB.executeUpdate(sqlForm + accessLevel);
		int wfDel = DB.executeUpdate("DELETE AD_WorkFlow_Access" + whereDel);
		int wf = DB.executeUpdate(sqlWorkflow + accessLevel);

		log.debug("updateAccessRecords - AD_Window_ID=" + winDel + "+" + win 
			+ ", AD_Process_ID=" + procDel + "+" + proc
			+ ", AD_Form_ID=" + formDel + "+" + form
			+ ", AD_Workflow_ID=" + wfDel + "+" + wf);
		
		return "@AD_Window_ID@ #" + win 
			+ " -  @AD_Process_ID@ #" + proc
			+ " -  @AD_Form_ID@ #" + form
			+ " -  @AD_Workflow_ID@ #" + wf;
	}	//	createAccessRecords

	
	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MRole[");
		sb.append(getAD_Role_ID()).append(",").append(getName())
			.append(",UserLevel=").append(getUserLevel())
			.append(",").append(getClientWhere(false))
			.append(",").append(getOrgWhere(false))
			.append("]");
		return sb.toString();
	}	//	toString

	/**
	 * 	Extended String Representation
	 *	@param ctx Properties
	 *	@return extended info
	 */
	public String toStringX (Properties ctx)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(Msg.translate(ctx, "AD_Role_ID")).append("=").append(getName())
			.append(" - ").append(Msg.translate(ctx, "IsCanExport")).append("=").append(isCanExport())
			.append(" - ").append(Msg.translate(ctx, "IsCanReport")).append("=").append(isCanReport())
			.append(Env.NL).append(Env.NL);
		//
		for (int i = 0; i < m_orgAccess.length; i++)
			sb.append(m_orgAccess[i].toStringX(ctx)).append(Env.NL);
		sb.append(Env.NL);
		//
		loadTableAccess(false);
		for (int i = 0; i < m_tableAccess.length; i++)
			sb.append(m_tableAccess[i].toStringX(ctx)).append(Env.NL);
		if (m_tableAccess.length > 0)
			sb.append(Env.NL);
		//
		loadColumnAccess(false);
		for (int i = 0; i < m_columnAccess.length; i++)
			sb.append(m_columnAccess[i].toStringX(ctx)).append(Env.NL);
		if (m_columnAccess.length > 0)
			sb.append(Env.NL);
		//
		loadRecordAccess(false);
		for (int i = 0; i < m_recordAccess.length; i++)
			sb.append(m_recordAccess[i].toStringX(ctx)).append(Env.NL);
		return sb.toString();
	}	//	toStringX



	/*************************************************************************
	 * 	Access Management
	 ************************************************************************/

	/** User 								*/
	private int						m_AD_User_ID = -1;	

	/**	Positive List of Organizational Access		*/	
	private MRoleOrgAccess[]		m_orgAccess = null;
	/** List of Table Access						*/
	private MTableAccess[]			m_tableAccess = null;
	/** List of Column Access				*/
	private MColumnAccess[]			m_columnAccess = null;
	/** List of Record Access				*/
	private MRecordAccess[]			m_recordAccess = null;
	/** List of Dependent Record Access		*/
	private MRecordAccess[]			m_recordDependentAccess = null;
	
	/**	Table Data Access Level	*/
	private HashMap					m_tableAccessLevel = null;
	/**	Table Name				*/
	private HashMap					m_tableName = null;
	
	/**	Window Access			*/
	private HashMap		m_windowAccess = null;
	/**	Process Access			*/
	private HashMap		m_processAccess = null;
	/**	Task Access				*/
	private HashMap		m_taskAccess = null;
	/**	Workflow Access			*/
	private HashMap		m_workflowAccess = null;
	/**	Form Access				*/
	private HashMap		m_formAccess = null;

	/**
	 * 	Set Logged in user
	 *	@param AD_User_ID user requesting info
	 */
	public void setAD_User_ID(int AD_User_ID)
	{
		m_AD_User_ID = AD_User_ID;
	}	//	setAD_User_ID

	/**
	 * 	Get Logged in user
	 *	@return AD_User_ID user requesting info
	 */
	public int getAD_User_ID()
	{
		return m_AD_User_ID;
	}	//	getAD_User_ID

	
	/**************************************************************************
	 * 	Load Access Info
	 * 	@param reload re-load from disk
	 */
	public void loadAccess (boolean reload)
	{
		loadOrgAccess(reload);
		loadTableAccess(reload);
		loadTableInfo(reload);
		loadColumnAccess(reload);
		loadRecordAccess(reload);
		if (reload)
		{
			m_windowAccess = null;
			m_processAccess = null;
			m_taskAccess = null;
			m_workflowAccess = null;
			m_formAccess = null;
		}
	}	//	loadAccess

	/**
	 * 	Load Org Access
	 *	@param reload reload
	 */
	private void loadOrgAccess (boolean reload)
	{
		if (!(reload || m_orgAccess == null))
			return;
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Role_OrgAccess "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MRoleOrgAccess(getCtx(), rs)); 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadOrgAccess", e);
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
		m_orgAccess = new MRoleOrgAccess[list.size()];
		list.toArray(m_orgAccess); 
		log.debug("loadOrgAccess - #" + m_orgAccess.length + (reload ? " - reload" : "")); 
		if (Ini.isClient())
		{
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < m_orgAccess.length; i++)
			{
				if (i > 0)
					sb.append(",");
				sb.append(m_orgAccess[i].getAD_Org_ID());
			}
			Env.setContext(Env.getCtx(), "#User_Org", sb.toString());
		}
	}	//	loadOrgAccess
	
	/**
	 * 	Load Table Access
	 *	@param reload reload
	 */
	private void loadTableAccess(boolean reload)
	{
		if (m_tableAccess != null && !reload)
			return;
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Table_Access "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MTableAccess(getCtx(), rs)); 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadTableAccess", e);
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
		m_tableAccess = new MTableAccess[list.size()];
		list.toArray(m_tableAccess); 
		log.debug("loadTableAccess - #" + m_tableAccess.length); 
	}	//	loadTableAccess

	/**
	 * 	Load Table Access and Name
	 *	@param reload reload
	 */
	private void loadTableInfo (boolean reload)
	{
		if (m_tableAccessLevel != null && m_tableName != null && !reload)
			return;
		m_tableAccessLevel = new HashMap(300);
		m_tableName = new HashMap(300);
		PreparedStatement pstmt = null;
		String sql = "SELECT AD_Table_ID, AccessLevel, TableName "
			+ "FROM AD_Table WHERE IsActive='Y'";
		try
		{
			pstmt = DB.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				Integer ii = new Integer(rs.getInt(1));
				m_tableAccessLevel.put(ii, rs.getString(2));
				m_tableName.put(rs.getString(3), ii);
			} 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadTableAccessLevel", e);
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
		log.debug("loadTableAccessLevel - #" + m_tableAccessLevel.size()); 
	}	//	loadTableAccessLevel

	
	/**
	 * 	Load Column Access
	 *	@param reload reload
	 */
	private void loadColumnAccess(boolean reload)
	{
		if (m_columnAccess != null && !reload)
			return;
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Column_Access "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MColumnAccess(getCtx(), rs)); 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadColumnAccess", e);
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
		m_columnAccess = new MColumnAccess[list.size()];
		list.toArray(m_columnAccess); 
		log.debug("loadColumnAccess - #" + m_columnAccess.length); 
	}	//	loadColumnAccess
	
	/**
	 * 	Load Record Access
	 *	@param reload reload
	 */
	private void loadRecordAccess(boolean reload)
	{
		if (!(reload || m_recordAccess == null || m_recordDependentAccess == null))
			return;
		ArrayList list = new ArrayList();
		ArrayList dependent = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Record_Access "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				MRecordAccess ra = new MRecordAccess(getCtx(), rs);
				list.add(ra);
				if (ra.isDependentEntities())
					dependent.add(ra);
			} 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadRecordAccess", e);
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
		m_recordAccess = new MRecordAccess[list.size()];
		list.toArray(m_recordAccess);
		m_recordDependentAccess = new MRecordAccess[dependent.size()];
		dependent.toArray(m_recordDependentAccess);
		log.debug("loadRecordAccess - #" + m_recordAccess.length + " - Dependent #" + m_recordDependentAccess.length); 
	}	//	loadRecordAccess


	/**************************************************************************
	 * 	Get Client Where Clause Value 
	 * 	@param rw read write
	 * 	@return "AD_Client_ID=0" or "AD_Client_ID IN(0,1)"
	 */
	public String getClientWhere (boolean rw)
	{
		//	All Orgs - use Client of Role
		if (isAccessAllOrgs())
		{
			if (rw || getAD_Client_ID() == 0)
				return "AD_Client_ID=" + getAD_Client_ID();
			return "AD_Client_ID IN (0," + getAD_Client_ID() + ")";
		}

		//	Get Client from Org List
		loadOrgAccess (false);
		//	Unique Strings
		HashSet set = new HashSet();
		if (!rw)
			set.add("0");
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
			set.add(String.valueOf(m_orgAccess[i].getAD_Client_ID()));
		//
		StringBuffer sb = new StringBuffer();
		Iterator it = set.iterator();
		boolean oneOnly = true;
		while (it.hasNext())
		{
			if (sb.length() > 0)
			{
				sb.append(",");
				oneOnly = false;
			}
			sb.append(it.next());
		}
		if (oneOnly)
		{
			if (sb.length() > 0)
				return "AD_Client_ID=" + sb.toString();
			else
			{
				log.error("getClientWhere - No Access Org records");
				return "AD_Client_ID=-1";	//	No Access Record
			}
		}
		return "AD_Client_ID IN(" + sb.toString() + ")";
	}	//	getClientWhereValue
	
	/**
	 * 	Access to Client
	 *	@param AD_Client_ID client
	 *	@param rw read write access
	 *	@return true if access
	 */
	public boolean isClientAccess(int AD_Client_ID, boolean rw)
	{
		if (AD_Client_ID == 0 && !rw)	//	can always read System
			return true;
		loadOrgAccess(false);
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
		{
			if (m_orgAccess[i].getAD_Client_ID() == AD_Client_ID)
				return true;
		}
		return false;
	}	//	isClientAccess
	
	/**
	 * 	Get Org Where Clause Value 
	 * 	@param rw read write
	 * 	@return "AD_Org_ID=0" or "AD_Org_ID IN(0,1)" or null (if access all org)
	 */
	public String getOrgWhere (boolean rw)
	{
		if (isAccessAllOrgs())
			return null;
		loadOrgAccess(false);
		//	Unique Strings
		HashSet set = new HashSet();
		if (!rw)
			set.add("0");
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
			set.add(String.valueOf(m_orgAccess[i].getAD_Org_ID()));
		//
		StringBuffer sb = new StringBuffer();
		Iterator it = set.iterator();
		boolean oneOnly = true;
		while (it.hasNext())
		{
			if (sb.length() > 0)
			{
				sb.append(",");
				oneOnly = false;
			}
			sb.append(it.next());
		}
		if (oneOnly)
		{
			if (sb.length() > 0)
				return "AD_Org_ID=" + sb.toString();
			else
			{
				log.error("getOrgWhere - No Access Org records");
				return "AD_Org_ID=-1";	//	No Access Record
			}
		}		
		return "AD_Org_ID IN(" + sb.toString() + ")";
	}	//	getOrgWhereValue
	
	/**
	 * 	Access to Org
	 *	@param AD_Org_ID org
	 *	@param rw read write access
	 *	@return true if access
	 */
	public boolean isOrgAccess(int AD_Org_ID, boolean rw)
	{
		if (isAccessAllOrgs())
			return true;
		if (AD_Org_ID == 0 && !rw)		//	can always read common org
			return true;
		loadOrgAccess(false);
		
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
		{
			if (m_orgAccess[i].getAD_Org_ID() == AD_Org_ID)
				return true;
		}
		return false;
	}	//	isOrgAccess


	/**
	 * 	Can Report on table
	 *	@param AD_Table_ID table
	 *	@return true if access
	 */
	public boolean isCanReport (int AD_Table_ID)
	{
		if (!isCanReport())						//	Role Level block
		{
			log.warn ("isCanReport - Role denied");
			return false;
		}
		if (!isTableAccess(AD_Table_ID, true))	//	No R/O Access to Table
			return false;
		//
		boolean canReport = true;
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (!MTableAccess.ACCESSTYPERULE_Reporting.equals(m_tableAccess[i].getAccessTypeRule()))
				continue;
			if (m_tableAccess[i].isExclude())		//	Exclude
			{
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					canReport = m_tableAccess[i].isCanReport();
					log.debug ("isCanReport - Exclude " + AD_Table_ID + " - " + canReport);
					return canReport;
				}
			}
			else									//	Include
			{
				canReport = false;
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					canReport = m_tableAccess[i].isCanReport();
					log.debug ("isCanReport - Include " + AD_Table_ID + " - " + canReport);
					return canReport;
				}
			}
		}	//	for all Table Access
		log.debug ("isCanReport - " + AD_Table_ID + " - " + canReport);
		return canReport;
	}	//	isCanReport
	
	/**
	 * 	Can Export Table
	 *	@param AD_Table_ID
	 *	@return true if access
	 */
	public boolean isCanExport (int AD_Table_ID)
	{
		if (!isCanExport())						//	Role Level block
		{
			log.warn ("isCanExport - Role denied");
			return false;
		}
		if (!isTableAccess(AD_Table_ID, true))	//	No R/O Access to Table
			return false;
		if (!isCanReport (AD_Table_ID))			//	We cannot Export if we cannot report
			return false;
		//
		boolean canExport = true;
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (!MTableAccess.ACCESSTYPERULE_Exporting.equals(m_tableAccess[i].getAccessTypeRule()))
				continue;
			if (m_tableAccess[i].isExclude())		//	Exclude
			{
				canExport = m_tableAccess[i].isCanExport();
				log.debug ("isCanExport - Exclude " + AD_Table_ID + " - " + canExport);
				return canExport;
			}
			else									//	Include
			{
				canExport = false;
				canExport = m_tableAccess[i].isCanExport();
				log.debug ("isCanExport - Include " + AD_Table_ID + " - " + canExport);
				return canExport;
			}
		}	//	for all Table Access
		log.debug ("isCanExport - " + AD_Table_ID + " - " + canExport);
		return canExport;
	}	//	isCanExport

	/**
	 * 	Access to Table
	 *	@param AD_Table_ID table
	 *	@param ro check read only access otherwise read write access level
	 *	@return has RO/RW access to table
	 */
	public boolean isTableAccess (int AD_Table_ID, boolean ro)
	{
		if (!isTableAccessLevel (AD_Table_ID, ro))	//	Role Based Access
			return false;
		loadTableAccess(false);
		//
		boolean hasAccess = true;	//	assuming exclusive rule
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (!MTableAccess.ACCESSTYPERULE_Accessing.equals(m_tableAccess[i].getAccessTypeRule()))
				continue;
			if (m_tableAccess[i].isExclude())		//	Exclude
			//	If you Exclude Access to a table and select Read Only, 
			//	you can only read data (otherwise no access).
			{
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (ro)
						hasAccess = m_tableAccess[i].isReadOnly();
					else
						hasAccess = false;
					log.debug("isTableAccess - Exclude AD_Table_ID=" + AD_Table_ID 
						+ " (ro="  + ro + ",TableAccessRO=" + m_tableAccess[i].isReadOnly() + ") = " + hasAccess);
					return hasAccess;
				}
			}
			else								//	Include
			//	If you Include Access to a table and select Read Only, 
			//	you can only read data (otherwise full access).
			{
				hasAccess = false;
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (!ro)	//	rw only if not r/o
						hasAccess = !m_tableAccess[i].isReadOnly();
					else
						hasAccess = true;
					log.debug("isTableAccess - Include AD_Table_ID=" + AD_Table_ID 
						+ " (ro="  + ro + ",TableAccessRO=" + m_tableAccess[i].isReadOnly() + ") = " + hasAccess);
					return hasAccess;
				}
			}
		}	//	for all Table Access
		if (!hasAccess)
			log.debug("isTableAccess - AD_Table_ID=" + AD_Table_ID 
				+ "(ro="  + ro + ") = " + hasAccess);
		return hasAccess;
	}	//	isTableAccess

	/**
	 * 	Access to Table based on Role User Level Table Access Level
	 *	@param AD_Table_ID table
	 *	@param ro check read only access otherwise read write access level
	 *	@return has RO/RW access to table
	 */
	public boolean isTableAccessLevel (int AD_Table_ID, boolean ro)
	{
		if (ro)				//	role can always read
			return true;
		//
		loadTableInfo(false);
		//	AccessLevel
		//		1 = Org - 2 = Client - 4 = System
		//		3 = Org+Client - 6 = Client+System - 7 = All
		String accessLevel = (String)m_tableAccessLevel.get(new Integer(AD_Table_ID));
		if (accessLevel == null)
		{
			log.debug("isTableAccessLevel - NO - No AccessLevel - AD_Table_ID=" + AD_Table_ID);
			return false;
		}
		//	Access to all User Levels
		if (accessLevel.equals(X_AD_Table.ACCESSLEVEL_All))
			return true;
		//	User Level = SCO
		String userLevel = getUserLevel();
		//	
		if (userLevel.charAt(0) == 'S'
			&& (accessLevel.equals(X_AD_Table.ACCESSLEVEL_SystemOnly) 
				|| accessLevel.equals(X_AD_Table.ACCESSLEVEL_SystemPlusClient)))
			return true;
		if (userLevel.charAt(1) == 'C'
			&& (accessLevel.equals(X_AD_Table.ACCESSLEVEL_ClientOnly) 
				|| accessLevel.equals(X_AD_Table.ACCESSLEVEL_SystemPlusClient)))
			return true;
		if (userLevel.charAt(2) == 'O'
			&& (accessLevel.equals(X_AD_Table.ACCESSLEVEL_Organization) 
				|| accessLevel.equals(X_AD_Table.ACCESSLEVEL_ClientPlusOrganization)))
			return true;
		log.debug("isTableAccessLevel - NO - AD_Table_ID=" + AD_Table_ID 
			+ ", UserLebel=" + userLevel + ", AccessLevel=" + accessLevel);
		return false;
	}	//	isTableAccessLevel


	/**
	 * 	Access to Column
	 *	@param AD_Table_ID table
	 *	@param AD_Column_ID column
	 *	@param ro read only
	 *	@return true if access
	 */
	public boolean isColumnAccess (int AD_Table_ID, int AD_Column_ID, boolean ro)
	{
		if (!isTableAccess(AD_Table_ID, ro))		//	No Access to Table		
			return false;
		loadColumnAccess(false);
		
		boolean retValue = true;		//	assuming exclusive
		for (int i = 0; i < m_columnAccess.length; i++)
		{
			if (m_columnAccess[i].isExclude())		//	Exclude
			//	If you Exclude Access to a column and select Read Only, 
			//	you can only read data (otherwise no access).
			{
				if (m_columnAccess[i].getAD_Table_ID() == AD_Table_ID 
					&& m_columnAccess[i].getAD_Column_ID() == AD_Column_ID)
				{
					if (ro)		//	just R/O Access requested
						retValue = m_columnAccess[i].isReadOnly();
					else
						retValue = false;
					if (!retValue)
						log.debug("isColumnAccess - Exclude AD_Table_ID=" + AD_Table_ID + ", AD_Column_ID=" + AD_Column_ID 
							+ " (ro="  + ro + ",ColumnAccessRO=" + m_columnAccess[i].isReadOnly() + ") = " + retValue);
					return retValue;
				}
			}
			else								//	Include
			//	If you Include Access to a column and select Read Only, 
			//	you can only read data (otherwise full access).
			{
				if (m_columnAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					retValue = false;
					if (m_columnAccess[i].getAD_Column_ID() == AD_Column_ID)
					{
						if (!ro)	//	rw only if not r/o
							retValue = !m_columnAccess[i].isReadOnly();
						else
							retValue = true;
						if (!retValue)
							log.debug("isColumnAccess - Include AD_Table_ID=" + AD_Table_ID + ", AD_Column_ID=" + AD_Column_ID 
								+ " (ro="  + ro + ",ColumnAccessRO=" + m_columnAccess[i].isReadOnly() + ") = " + retValue);
						return retValue;
					}
				}	//	same table
			}	//	include
		}	//	for all Table Access
		if (!retValue)
			log.debug("isColumnAccess - AD_Table_ID=" + AD_Table_ID + ", AD_Column_ID=" + AD_Column_ID 
				+ " (ro="  + ro + ") = " + retValue);
		return retValue;
	}	//	isColumnAccess

	/**
	 *	Access to Record
	 *	@param AD_Table_ID table
	 *	@param Record_ID
	 *	@param ro
	 *	 * @return boolean
	 */
	public boolean isRecordAccess (int AD_Table_ID, int Record_ID, boolean ro)
	{
		if (!isTableAccess(AD_Table_ID, ro))		//	No Access to Table
			return false;
		loadRecordAccess(false);
		
		boolean negativeList = true;
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (m_recordAccess[i].isExclude())		//	Exclude
			//	If you Exclude Access to a column and select Read Only, 
			//	you can only read data (otherwise no access).
			{
				if (m_recordAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (ro)
						return m_tableAccess[i].isReadOnly();
					else
						return false;
				}
			}
			else								//	Include
			//	If you Include Access to a column and select Read Only, 
			//	you can only read data (otherwise full access).
			{
				negativeList = false;
				if (m_recordAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (!ro)	//	rw only if not r/o
						return !m_tableAccess[i].isReadOnly();
					else
						return true;
				}
			}
		}	//	for all Table Access
		return negativeList;
	}	//	isRecordAccess

	/**
	 * 	Get Window Access
	 *	@param AD_Window_ID window
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getWindowAccess (int AD_Window_ID)
	{
		if (m_windowAccess == null)
		{
			m_windowAccess = new HashMap(100);
			String sql = "SELECT AD_Window_ID, IsReadWrite FROM AD_Window_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_windowAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getWindowAccess", e);
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
			log.debug("getWindowAccess #" + m_windowAccess.size());
		}	//	reload
		Boolean retValue = (Boolean)m_windowAccess.get(new Integer(AD_Window_ID));
	//	log.debug("getWindowAccess - AD_Window_ID=" + AD_Window_ID + " - " + retValue);
		return retValue;
	}	//	getWindowAccess

	/**
	 * 	Get Process Access
	 *	@param AD_Process_ID process
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getProcessAccess (int AD_Process_ID)
	{
		if (m_processAccess == null)
		{
			m_processAccess = new HashMap(50);
			String sql = "SELECT AD_Process_ID, IsReadWrite FROM AD_Process_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_processAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getProcessAccess", e);
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
		}	//	reload
		return (Boolean)m_processAccess.get(new Integer(AD_Process_ID));
	}	//	getProcessAccess

	/**
	 * 	Get Task Access
	 *	@param AD_Task_ID task
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getTaskAccess (int AD_Task_ID)
	{
		if (m_taskAccess == null)
		{
			m_taskAccess = new HashMap(10);
			String sql = "SELECT AD_Task_ID, IsReadWrite FROM AD_Task_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_taskAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getTaskAccess", e);
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
		}	//	reload
		return (Boolean)m_taskAccess.get(new Integer(AD_Task_ID));
	}	//	getTaskAccess

	/**
	 * 	Get Form Access
	 *	@param AD_Form_ID form
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getFormAccess (int AD_Form_ID)
	{
		if (m_formAccess == null)
		{
			m_formAccess = new HashMap(20);
			String sql = "SELECT AD_Form_ID, IsReadWrite FROM AD_Form_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_formAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getFormAccess", e);
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
		}	//	reload
		return (Boolean)m_formAccess.get(new Integer(AD_Form_ID));
	}	//	getTaskAccess

	/**
	 * 	Get Workflow Access
	 *	@param AD_Workflow_ID workflow
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getWorkflowAccess (int AD_Workflow_ID)
	{
		if (m_workflowAccess == null)
		{
			m_workflowAccess = new HashMap(20);
			String sql = "SELECT AD_Workflow_ID, IsReadWrite FROM AD_Workflow_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareStatement(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_workflowAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getWorkflowAccess", e);
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
		}	//	reload
		return (Boolean)m_workflowAccess.get(new Integer(AD_Workflow_ID));
	}	//	getTaskAccess

	
	/*************************************************************************
	 *	Appends where clause to SQL statement for Table
	 *
	 *	@param SQL			existing SQL statement
	 *	@param TableNameIn	Table Name or list of table names AAA, BBB or AAA a, BBB b
	 *	@param fullyQualified	fullyQualified names
	 *	@param rw			if false, includes System Data
	 *	@return				updated SQL statement
	 */
	public String addAccessSQL (String SQL, String TableNameIn, 
		boolean fullyQualified, boolean rw)
	{
		StringBuffer retSQL = new StringBuffer();

		//	Cut off last ORDER BY clause
		String orderBy = "";
		int posOrder = SQL.lastIndexOf(" ORDER BY ");
		if (posOrder != -1)
		{
			orderBy = SQL.substring(posOrder);
			retSQL.append(SQL.substring(0, posOrder));
		}
		else
			retSQL.append(SQL);

		//	Parse SQL
		AccessSqlParser asp = new AccessSqlParser(retSQL.toString());
		AccessSqlParser.TableInfo[] ti = asp.getTableInfo(asp.getMainSqlIndex()); 

		//  Do we have to add WHERE or AND
		if (asp.getMainSql().indexOf(" WHERE ") == -1)
			retSQL.append(" WHERE ");
		else
			retSQL.append(" AND ");

		//	Use First Table
		String tableName = "";
		if (ti.length > 0)
		{
			tableName = ti[0].getSynonym();
			if (tableName.length() == 0)
				tableName = ti[0].getTableName();
		}
		if (TableNameIn != null && !tableName.equals(TableNameIn))
		{
			String msg = "addAccessSQL - TableName not correctly parsed - TableNameIn=" 
				+ TableNameIn + " - " + asp;
			if (ti.length > 0)
				msg += " - #1 " + ti[0]; 
			msg += "\n = " + SQL;
			log.error(msg);
			Trace.printStack();
			tableName = TableNameIn;
		}

		//	Client Access
		if (fullyQualified)
			retSQL.append(tableName).append(".");
		retSQL.append(getClientWhere(rw));
		
		//	Org Access
		if (!isAccessAllOrgs())
		{
			retSQL.append(" AND ");
			if (fullyQualified)
				retSQL.append(tableName).append(".");
			retSQL.append(getOrgWhere(rw));
		}
			
		//	** Data Access	**
		for (int i = 0; i < ti.length; i++)
		{
			String TableName = ti[i].getTableName();
			int AD_Table_ID = getAD_Table_ID (TableName);
			//	Data Table Access
			if (AD_Table_ID != 0 && !isTableAccess(AD_Table_ID, !rw))
			{
				retSQL.append(" AND 1=3");	//	prevent access at all
				log.debug("addAccessSQL - no access to AD_Table_ID=" + AD_Table_ID 
					+ " - " + TableName + " - " + retSQL);
				break;	//	no need to check further 
			}
			
			//	Data Column Access
	
	
		
			//	Data Record Access
			String keyColumnName = "";
			if (fullyQualified)
			{
				keyColumnName = ti[i].getSynonym();	//	table synonym
				if (keyColumnName.length() == 0)
					keyColumnName = TableName;
				keyColumnName += ".";
			}
			keyColumnName += TableName + "_ID";	//	derived from table
	
		//	log.debug("addAccessSQL - " + TableName + "(" + AD_Table_ID + ") " + keyColumnName);
			String recordWhere = getRecordWhere (AD_Table_ID, keyColumnName);
			if (recordWhere.length() > 0)
			{
				retSQL.append(" AND ").append(recordWhere);
				if (Log.isTraceLevel(9))
					log.debug("addAccessSQL - adding record access - " + recordWhere);
			}
		}	//	for all table info
		
		//	Dependent Records (only for main SQL)
		String mainSql = asp.getMainSql();
		loadRecordAccess(false);
		for (int i = 0; i < m_recordDependentAccess.length; i++)
		{
			String columnName = m_recordDependentAccess[i].getKeyColumnName(asp.getTableInfo(asp.getMainSqlIndex()) );
			if (columnName == null)
				continue;	//	no key column
			int posColumn = mainSql.indexOf(columnName);
			if (posColumn == -1)
				continue;
			//	we found the column name - make sure it's a clumn name
			char charCheck = mainSql.charAt(posColumn-1);	//	before
			if (!(charCheck == ',' || charCheck == '.' || charCheck == ' ' || charCheck == '('))
				continue;
			charCheck = mainSql.charAt(posColumn+columnName.length());	//	after
			if (!(charCheck == ',' || charCheck == ' ' || charCheck == ')'))
				continue;
				
			//	*** we found the column in the main query
			log.debug("DEPENDENT  " + columnName + " - " + m_recordDependentAccess[i]);
			StringBuffer where = new StringBuffer();
			where.append(getDependentRecordWhereColumn (mainSql, columnName))
				.append((m_recordDependentAccess[i].isExclude() ? "<>" : "="))
				.append(m_recordDependentAccess[i].getRecord_ID());
			if (Log.isTraceLevel(9))
				log.debug("addAccessSQL - adding dependent access - " + where);
			retSQL.append(" AND ").append(where);			
		}	//	for all dependent records
		
		//
		retSQL.append(orderBy);
		if (Log.isTraceLevel(10))
			log.debug("addAccessSQL - " + retSQL);
		return retSQL.toString();
	}	//	addAccessSQL

	/**
	 * 	Get Dependent Record Where clause
	 *	@param mainSql sql to examine
	 *	@param columnName columnName
	 *	@return where clause column "x.columnName"
	 */
	private String getDependentRecordWhereColumn (String mainSql, String columnName)
	{
		String retValue = columnName;	//	if nothing else found
		int index = mainSql.indexOf(columnName);
		//	see if there are table synonym
		int offset = index - 1;
		char c = mainSql.charAt(offset);
		if (c == '.')
		{
			StringBuffer sb = new StringBuffer();
			while (c != ' ' && c != ',' && c != '(')	//	delimeter
			{
				sb.insert(0, c);
				c = mainSql.charAt(--offset);
			}
			sb.append(columnName);
			return sb.toString();
		}
		return retValue;
	}	//	getDependentRecordWhereColumn



	/**
	 *	UPADATE - Can I Update the record
	 *  Access error info (AccessTableNoUpdate) is saved in the log
	 * 
	 *   * @param AD_Client_ID comntext to derive client/org/user level
	 * @param AD_Org_ID number of the current window to retrieve context
	 * @param AD_Table_ID int
	 * @param createError boolean
	@return	true if you can update
	@see org.compiere.model.MTable#save(boolean)
	 *
	 *   */
	public boolean canUpdate (int AD_Client_ID, int AD_Org_ID, 
		int AD_Table_ID, boolean createError)
	{
		String userLevel = getUserLevel();	//	Format 'SCO'

		if (userLevel.indexOf("S") != -1)		//	System can change anything
			return true;

		boolean	retValue = true;
		String whatMissing = "";

		//	System == Client=0 & Org=0
		if (AD_Client_ID == 0 && AD_Org_ID == 0
			&& userLevel.charAt(0) != 'S')
		{
			retValue = false;
			whatMissing += "S";
		}

		//	Client == Client!=0 & Org=0
		else if (AD_Client_ID != 0 && AD_Org_ID == 0
			&& userLevel.charAt(1) != 'C')
		{
			retValue = false;
			whatMissing += "C";
		}

		//	Organization == Client!=0 & Org!=0
		else if (AD_Client_ID != 0 && AD_Org_ID != 0
			&& userLevel.charAt(2) != 'O')
		{
			retValue = false;
			whatMissing += "O";
		}

		//	Data Access
		//	get Table.IsSecurityEnabled
		//	if yes: get UserAcess info => Where Table.TableKey in (List)
		//									and Access=r/w

		if (!retValue && createError)
		{
			Log.saveError("AccessTableNoUpdate",
				"AD_Client_ID=" + AD_Client_ID + ", AD_Org_ID=" + AD_Org_ID + ", UserLevel=" + userLevel
				+ " => missing=" + whatMissing);
			log.error ("canUpdate - " + toString());
		}
		return retValue;
	}	//	canUpdate

	/**
	 *	VIEW - Can I view record in Table with given TableLevel
	 *  <code>
	 *	TableLevel			S__ 100		4	System info
	 *						SCO	111		7	System shared info
	 *						SC_ 110		6	System/Client info
	 *						_CO	011		3	Client shared info
	 *						_C_	011		2	Client shared info
	 *						__O	001		1	Organization info
	 *  </code>
	 * 
	 * 	@param ctx	context
	 *	@param TableLevel	AccessLevel
	 *	@return	true/false
	 *  Access error info (AccessTableNoUpdate, AccessTableNoView) is saved in the log
	@see org.compiere.model.MTabVO#loadTabDetails (MTabVO, ResultSet)
	 *   */
	public boolean canView(Properties ctx, String TableLevel)
	{
		String userLevel = getUserLevel();	//	Format 'SCO'

		boolean retValue = true;

		//	7 - All
		if (X_AD_Table.ACCESSLEVEL_All.equals(TableLevel))
			retValue = true;
			 
		//	4 - System data requires S
		else if (X_AD_Table.ACCESSLEVEL_SystemOnly.equals(TableLevel) 
			&& userLevel.charAt(0) != 'S')
			retValue = false;

		//	2 - Client data requires C
		else if (X_AD_Table.ACCESSLEVEL_ClientOnly.equals(TableLevel) 
			&& userLevel.charAt(1) != 'C')
			retValue = false;

		//	1 - Organization data requires O
		else if (X_AD_Table.ACCESSLEVEL_Organization.equals(TableLevel) 
			&& userLevel.charAt(2) != 'O')
			retValue = false;

		//	3 - Client Shared requires C or O
		else if (X_AD_Table.ACCESSLEVEL_ClientPlusOrganization.equals(TableLevel)
			&& (!(userLevel.charAt(1) == 'C' || userLevel.charAt(2) == 'O')) )
				retValue = false;

		//	6 - System/Client requires S or C
		else if (X_AD_Table.ACCESSLEVEL_SystemPlusClient.equals(TableLevel)
			&& (!(userLevel.charAt(0) == 'S' || userLevel.charAt(1) == 'C')) )
			retValue = false;

		if (retValue)
			return retValue;

		//  Notification
		/**
		if (forInsert)
			Log.saveError("AccessTableNoUpdate",
				"(Required=" + TableLevel + "("
				+ getTableLevelString(Env.getAD_Language(ctx), TableLevel)
				+ ") != UserLevel=" + userLevel);
		else
		**/
			Log.saveError("AccessTableNoView",
				"Required=" + TableLevel + "("
				+ getTableLevelString(Env.getAD_Language(ctx), TableLevel)
				+ ") != UserLevel=" + userLevel);
		log.info ("canView - " + toString());
		return retValue;
	}	//	canView


	/**
	 *	Returns clear text String of TableLevel
	 *  @param AD_Language language
	 *  @param TableLevel level
	 *  @return info
	 */
	private String getTableLevelString (String AD_Language, String TableLevel)
	{
		String level = TableLevel + "??";
		if (TableLevel.equals("1"))
			level = "AccessOrg";
		else if (TableLevel.equals("2"))
			level = "AccessClient";
		else if (TableLevel.equals("3"))
			level = "AccessClientOrg";
		else if (TableLevel.equals("4"))
			level = "AccessSystem";
		else if (TableLevel.equals("6"))
			level = "AccessSystemClient";
		else if (TableLevel.equals("7"))
			level = "AccessShared";

		return Msg.getMsg(AD_Language, level);
	}	//	getTableLevelString

	/**
	 * 	Get Table ID from name
	 *	@param tableName table name
	 *	@return AD_Table_ID or 0
	 */
	private int getAD_Table_ID (String tableName)
	{
		loadTableInfo(false);
		Integer ii = (Integer)m_tableName.get(tableName);
		if (ii != null)
			return ii.intValue();
	//	log.warn("getAD_Table_ID - not found (" + tableName + ")");
		return 0;
	}	//	getAD_Table_ID

	/**
	 * 	Return Where clause for Record Access
	 *	@param AD_Table_ID table
	 *	@param keyColumnName (fully qualified) key column name
	 *	@return where clause or ""
	 */
	private String getRecordWhere (int AD_Table_ID, String keyColumnName)
	{
		loadRecordAccess(false);
		//
		StringBuffer sb = new StringBuffer();
		//	Role Access
		for (int i = 0; i < m_recordAccess.length; i++)
		{
			if (m_recordAccess[i].getAD_Table_ID() == AD_Table_ID)
			{
				if (sb.length() > 0)
					sb.append(" AND ");
				sb.append(keyColumnName)
					.append((m_recordAccess[i].isExclude() ? "<>" : "="))
					.append(m_recordAccess[i].getRecord_ID());
			}
		}	//	for all Table Access
		
		//	Don't ignore Privacy Access
		if (!isPersonalAccess())
		{
			String lockedIDs = MPrivateAccess.getLockedRecordWhere(AD_Table_ID, m_AD_User_ID);
			if (lockedIDs != null)
			{
				if (sb.length() > 0)
					sb.append(" AND ");
				sb.append(keyColumnName).append(lockedIDs);
			}
		}
		//
		return sb.toString();
	}	//	getRecordWhere

}	//	MRole
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2003 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.model;

import java.net.*;
import java.sql.*;
import java.util.*;

import org.compiere.util.*;

/**
 *	Role Model.
 *	The class is final, so that you cannot overwrite the security rules.
 *	
 *  @author Jorg Janke
 *  @version $Id: MRole.java,v 1.10 2003/11/07 06:37:56 jjanke Exp $
 */
public final class MRole extends X_AD_Role
{
	/**
	 * 	Get Default Role
	 *	@return role
	 */
	public static MRole getDefault ()
	{
		if (s_defaultRole == null && Ini.isClient())
			return getDefault (Env.getCtx(), false);
		return s_defaultRole;
	}	//	getDefault

	/**
	 * 	Get/Set Default Role.
	 * 	@see org.compiere.util.DB#loadPreferences
	 * 	@param ctx context
	 * 	@param reload if true forces load
	 *	@return role
	 */
	public static MRole getDefault (Properties ctx, boolean reload)
	{
		int AD_Role_ID = Env.getContextAsInt(ctx, "#AD_Role_ID");
		int AD_User_ID = Env.getContextAsInt(ctx, "#AD_User_ID");
		if (reload || s_defaultRole == null)
		{
			s_defaultRole = get (ctx, AD_Role_ID, AD_User_ID, reload);
		}
		else if (s_defaultRole.getAD_Role_ID() != AD_Role_ID
			|| s_defaultRole.getAD_User_ID() != AD_User_ID)
		{
			s_defaultRole = get (ctx, AD_Role_ID, AD_User_ID, reload);
		}
		return s_defaultRole;
	}	//	getDefault
	
	/**
	 * 	Get Role for User
	 * 	@param ctx, context
	 * 	@param AD_Role_ID role
	 * 	@param AD_User_ID user
	 * 	@param reload if true forces load
	 *	@return role
	 */
	public static MRole get (Properties ctx, int AD_Role_ID, int AD_User_ID, boolean reload)
	{
		s_log.info("get - AD_Role_ID=" + AD_Role_ID + ", AD_User_ID=" + AD_User_ID + ", reload=" + reload);
		String key = AD_Role_ID + "_" + AD_User_ID;
		MRole role = (MRole)s_roles.get (key);
		if (role == null || reload)
		{
			role = new MRole (ctx, AD_Role_ID);
			s_roles.put (key, role);
			if (AD_Role_ID == 0)
				role.load();			//	special Handling
			role.setAD_User_ID(AD_User_ID);
			s_log.info("get - " + role);
		}
		return role;
	}	//	get
	
	
	/**	Default Role			*/
	private static MRole			s_defaultRole = null;
	/** Role/User Cache			*/
	private static CCache			s_roles = new CCache("MRole", 5);
	/** Log						*/ 
	private static Logger			s_log = Logger.getCLogger(MRole.class);
	
	/**	Access SQL Read Write		*/
	public static final boolean		SQL_RW = true;
	/**	Access SQL Read Only		*/
	public static final boolean		SQL_RO = false;
	/**	Access SQL Fully Qualified	*/
	public static final boolean		SQL_FULLYQUALIFIED = true;
	/**	Access SQL Not Fully Qualified	*/
	public static final boolean		SQL_NOTQUALIFIED = false;

	
	
	/*************************************************************************/
	
	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param AD_Role_ID id
	 */
	public MRole (Properties ctx, int AD_Role_ID)
	{
		super(ctx, AD_Role_ID);
		if (AD_Role_ID == 0)
		{
		/**
		//	setAD_Role_ID (0);
			setIsCanExport (false);
			setIsCanReport (false);
			setIsManual (false);
			setIsPersonalAccess (false);
			setIsPersonalLock (false);
			setIsShowAcct (false);
		//	setName (null);
			setUserLevel (USERLEVEL_Client);
		//	setClientList (null);
		//	setOrgList (null);
		**/
		}		
	}	//	MRole

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 */
	public MRole(Properties ctx, ResultSet rs)
	{
		super(ctx, rs);
	}	//	MRole

	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MRole[");
		sb.append(getAD_Role_ID()).append(",").append(getName())
			.append(",UserLevel=").append(getUserLevel())
			.append(",").append(getClientWhere(false))
			.append(",").append(getOrgWhere(false))
			.append("]");
		return sb.toString();
	}	//	toString

	/**
	 * 	Extended String Representation
	 *	@return extended info
	 */
	public String toStringX (Properties ctx)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(Msg.translate(ctx, "AD_Role_ID")).append("=").append(getName())
			.append(" - ").append(Msg.translate(ctx, "IsCanExport")).append("=").append(isCanExport())
			.append(" - ").append(Msg.translate(ctx, "IsCanReport")).append("=").append(isCanReport())
			.append(Env.NL).append(Env.NL);
		//
		for (int i = 0; i < m_orgAccess.length; i++)
			sb.append(m_orgAccess[i].toStringX(ctx)).append(Env.NL);
		sb.append(Env.NL);
		//
		loadTableAccess(false);
		for (int i = 0; i < m_tableAccess.length; i++)
			sb.append(m_tableAccess[i].toStringX(ctx)).append(Env.NL);
		if (m_tableAccess.length > 0)
			sb.append(Env.NL);
		//
		loadColumnAccess(false);
		for (int i = 0; i < m_columnAccess.length; i++)
			sb.append(m_columnAccess[i].toStringX(ctx)).append(Env.NL);
		if (m_columnAccess.length > 0)
			sb.append(Env.NL);
		//
		loadRecordAccess(false);
		for (int i = 0; i < m_recordAccess.length; i++)
			sb.append(m_recordAccess[i].toStringX(ctx)).append(Env.NL);
		return sb.toString();
	}	//	toStringX


	/**
	 * 	Load Access Info
	 * 	@param reload re-load from disk
	 */
	public void loadAccess (boolean reload)
	{
		loadOrgAccess(reload);
		loadTableAccess(reload);
		loadTableInfo(reload);
		loadColumnAccess(reload);
		loadRecordAccess(reload);
		if (reload)
		{
			m_windowAccess = null;
			m_processAccess = null;
			m_taskAccess = null;
			m_workflowAccess = null;
			m_formAccess = null;
		}
	}	//	loadAccess


	/*************************************************************************/

	/** User 								*/
	private int						m_AD_User_ID = -1;	

	/**	Positive List of Organizational Access		*/	
	private MRoleOrgAccess[]		m_orgAccess = null;
	/** List of Table Access						*/
	private MTableAccess[]			m_tableAccess = null;
	/** List of Column Access				*/
	private MColumnAccess[]	m_columnAccess = null;
	/** List of Record Access				*/
	private MRecordAccess[]			m_recordAccess = null;
	/** List of Dependent Record Access		*/
	private MRecordAccess[]			m_recordDependentAccess = null;
	
	/**	Table Data Access Level	*/
	private HashMap					m_tableAccessLevel = null;
	/**	Table Name				*/
	private HashMap					m_tableName = null;
	
	/**	Window Access			*/
	private HashMap		m_windowAccess = null;
	/**	Process Access			*/
	private HashMap		m_processAccess = null;
	/**	Task Access				*/
	private HashMap		m_taskAccess = null;
	/**	Workflow Access			*/
	private HashMap		m_workflowAccess = null;
	/**	Form Access				*/
	private HashMap		m_formAccess = null;

	/**
	 * 	Set Logged in user
	 *	@param AD_User_ID user requesting info
	 */
	public void setAD_User_ID(int AD_User_ID)
	{
		m_AD_User_ID = AD_User_ID;
	}	//	setAD_User_ID

	/**
	 * 	Get Logged in user
	 *	@return AD_User_ID user requesting info
	 */
	public int getAD_User_ID()
	{
		return m_AD_User_ID;
	}	//	getAD_User_ID
	
	/**
	 * 	Load Org Access
	 *	@param reload reload
	 */
	private void loadOrgAccess (boolean reload)
	{
		if (!(reload || m_orgAccess == null))
			return;
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Role_OrgAccess "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareCall(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MRoleOrgAccess(getCtx(), rs)); 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadOrgAccess", e);
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
		m_orgAccess = new MRoleOrgAccess[list.size()];
		list.toArray(m_orgAccess); 
		log.debug("loadOrgAccess - #" + m_orgAccess.length + (reload ? " - reload" : "")); 
	}	//	loadOrgAccess
	
	/**
	 * 	Load Table Access
	 *	@param reload reload
	 */
	private void loadTableAccess(boolean reload)
	{
		if (m_tableAccess != null && !reload)
			return;
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Table_Access "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareCall(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MTableAccess(getCtx(), rs)); 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadTableAccess", e);
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
		m_tableAccess = new MTableAccess[list.size()];
		list.toArray(m_tableAccess); 
		log.debug("loadTableAccess - #" + m_tableAccess.length); 
	}	//	loadTableAccess

	/**
	 * 	Load Table Access and Name
	 *	@param reload reload
	 */
	private void loadTableInfo (boolean reload)
	{
		if (m_tableAccessLevel != null && m_tableName != null && !reload)
			return;
		m_tableAccessLevel = new HashMap(300);
		m_tableName = new HashMap(300);
		PreparedStatement pstmt = null;
		String sql = "SELECT AD_Table_ID, AccessLevel, TableName "
			+ "FROM AD_Table WHERE IsActive='Y'";
		try
		{
			pstmt = DB.prepareCall(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				Integer ii = new Integer(rs.getInt(1));
				m_tableAccessLevel.put(ii, rs.getString(2));
				m_tableName.put(rs.getString(3), ii);
			} 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadTableAccessLevel", e);
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
		log.debug("loadTableAccessLevel - #" + m_tableAccessLevel.size()); 
	}	//	loadTableAccessLevel

	
	/**
	 * 	Load Column Access
	 *	@param reload reload
	 */
	private void loadColumnAccess(boolean reload)
	{
		if (m_columnAccess != null && !reload)
			return;
		ArrayList list = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Column_Access "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareCall(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MColumnAccess(getCtx(), rs)); 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadColumnAccess", e);
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
		m_columnAccess = new MColumnAccess[list.size()];
		list.toArray(m_columnAccess); 
		log.debug("loadColumnAccess - #" + m_columnAccess.length); 
	}	//	loadColumnAccess
	
	/**
	 * 	Load Record Access
	 *	@param reload reload
	 */
	private void loadRecordAccess(boolean reload)
	{
		if (!(reload || m_recordAccess == null || m_recordDependentAccess == null))
			return;
		ArrayList list = new ArrayList();
		ArrayList dependent = new ArrayList();
		PreparedStatement pstmt = null;
		String sql = "SELECT * FROM AD_Record_Access "
			+ "WHERE AD_Role_ID=? AND IsActive='Y'";
		try
		{
			pstmt = DB.prepareCall(sql);
			pstmt.setInt(1, getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				MRecordAccess ra = new MRecordAccess(getCtx(), rs);
				list.add(ra);
				if (ra.isDependentEntities())
					dependent.add(ra);
			} 
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.error("loadRecordAccess", e);
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
		m_recordAccess = new MRecordAccess[list.size()];
		list.toArray(m_recordAccess);
		m_recordDependentAccess = new MRecordAccess[dependent.size()];
		dependent.toArray(m_recordDependentAccess);
		log.debug("loadRecordAccess - #" + m_recordAccess.length + " - Dependent #" + m_recordDependentAccess.length); 
	}	//	loadRecordAccess

	/*************************************************************************/
	
	/**
	 * 	Get Client Where Clause Value 
	 * 	@param rw read write
	 * 	@return "AD_Client_ID=0" or "AD_Client_ID IN(0,1)"
	 */
	public String getClientWhere (boolean rw)
	{
		loadOrgAccess(false);
		//	Unique Strings
		HashSet set = new HashSet();
		if (!rw)
			set.add("0");
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
			set.add(String.valueOf(m_orgAccess[i].getAD_Client_ID()));
		//
		StringBuffer sb = new StringBuffer();
		Iterator it = set.iterator();
		boolean oneOnly = true;
		while (it.hasNext())
		{
			if (sb.length() > 0)
			{
				sb.append(",");
				oneOnly = false;
			}
			sb.append(it.next());
		}
		if (oneOnly)
		{
			if (sb.length() > 0)
				return "AD_Client_ID=" + sb.toString();
			else
			{
				log.error("getClientWhere - No Access Org records");
				return "AD_Client_ID=-1";	//	No Access Record
			}
		}
		return "AD_Client_ID IN(" + sb.toString() + ")";
	}	//	getClientWhereValue
	
	/**
	 * 	Access to Client
	 *	@param AD_Client_ID client
	 *	@param rw read write access
	 *	@return true if access
	 */
	public boolean isClientAccess(int AD_Client_ID, boolean rw)
	{
		if (AD_Client_ID == 0 && !rw)	//	can always read System
			return true;
		loadOrgAccess(false);
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
		{
			if (m_orgAccess[i].getAD_Client_ID() == AD_Client_ID)
				return true;
		}
		return false;
	}	//	isClientAccess
	
	/**
	 * 	Get Org Where Clause Value 
	 * 	@param rw read write
	 * 	@return "AD_Org_ID=0" or "AD_Org_ID IN(0,1)"
	 */
	public String getOrgWhere (boolean rw)
	{
		loadOrgAccess(false);
		//	Unique Strings
		HashSet set = new HashSet();
		if (!rw)
			set.add("0");
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
			set.add(String.valueOf(m_orgAccess[i].getAD_Org_ID()));
		//
		StringBuffer sb = new StringBuffer();
		Iterator it = set.iterator();
		boolean oneOnly = true;
		while (it.hasNext())
		{
			if (sb.length() > 0)
			{
				sb.append(",");
				oneOnly = false;
			}
			sb.append(it.next());
		}
		if (oneOnly)
		{
			if (sb.length() > 0)
				return "AD_Org_ID=" + sb.toString();
			else
			{
				log.error("getOrgWhere - No Access Org records");
				return "AD_Org_ID=-1";	//	No Access Record
			}
		}		
		return "AD_Org_ID IN(" + sb.toString() + ")";
	}	//	getOrgWhereValue
	
	/**
	 * 	Access to Org
	 *	@param AD_Org_ID org
	 *	@param rw read write access
	 *	@return true if access
	 */
	public boolean isOrgAccess(int AD_Org_ID, boolean rw)
	{
		if (AD_Org_ID == 0 && !rw)		//	can always read common org
			return true;
		loadOrgAccess(false);
		
		//	Positive List
		for (int i = 0; i < m_orgAccess.length; i++)
		{
			if (m_orgAccess[i].getAD_Org_ID() == AD_Org_ID)
				return true;
		}
		return false;
	}	//	isOrgAccess


	/**
	 * 	Can Report on table
	 *	@param AD_Table_ID table
	 *	@return true if access
	 */
	public boolean isCanReport (int AD_Table_ID)
	{
		if (!isCanReport())						//	Role Level block
		{
			log.warn ("isCanReport - Role denied");
			return false;
		}
		if (!isTableAccess(AD_Table_ID, true))	//	No R/O Access to Table
			return false;
		//
		boolean canReport = true;
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (!MTableAccess.ACCESSTYPERULE_Reporting.equals(m_tableAccess[i].getAccessTypeRule()))
				continue;
			if (m_tableAccess[i].isExclude())		//	Exclude
			{
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					canReport = m_tableAccess[i].isCanReport();
					log.debug ("isCanReport - Exclude " + AD_Table_ID + " - " + canReport);
					return canReport;
				}
			}
			else									//	Include
			{
				canReport = false;
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					canReport = m_tableAccess[i].isCanReport();
					log.debug ("isCanReport - Include " + AD_Table_ID + " - " + canReport);
					return canReport;
				}
			}
		}	//	for all Table Access
		log.debug ("isCanReport - " + AD_Table_ID + " - " + canReport);
		return canReport;
	}	//	isCanReport
	
	/**
	 * 	Can Export Table
	 *	@param AD_Table_ID
	 *	@return true if access
	 */
	public boolean isCanExport (int AD_Table_ID)
	{
		if (!isCanExport())						//	Role Level block
		{
			log.warn ("isCanExport - Role denied");
			return false;
		}
		if (!isTableAccess(AD_Table_ID, true))	//	No R/O Access to Table
			return false;
		if (!isCanReport (AD_Table_ID))
			return false;
		//
		boolean canExport = true;
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (!MTableAccess.ACCESSTYPERULE_Exporting.equals(m_tableAccess[i].getAccessTypeRule()))
				continue;
			if (m_tableAccess[i].isExclude())		//	Exclude
			{
				canExport = m_tableAccess[i].isCanExport();
				log.debug ("isCanExport - Exclude " + AD_Table_ID + " - " + canExport);
				return canExport;
			}
			else									//	Include
			{
				canExport = false;
				canExport = m_tableAccess[i].isCanExport();
				log.debug ("isCanExport - Include " + AD_Table_ID + " - " + canExport);
				return canExport;
			}
		}	//	for all Table Access
		log.debug ("isCanExport - " + AD_Table_ID + " - " + canExport);
		return canExport;
	}	//	isCanExport

	/**
	 * 	Access to Table
	 *	@param AD_Table_ID table
	 *	@param ro check read only access otherwise read write access level
	 *	@return has RO/RW access to table
	 */
	public boolean isTableAccess (int AD_Table_ID, boolean ro)
	{
		if (!isTableAccessLevel (AD_Table_ID, ro))	//	Role Based Access
			return false;
		loadTableAccess(false);
		//
		boolean hasAccess = true;	//	assuming exclusive rule
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (!MTableAccess.ACCESSTYPERULE_Accessing.equals(m_tableAccess[i].getAccessTypeRule()))
				continue;
			if (m_tableAccess[i].isExclude())		//	Exclude
			//	If you Exclude Access to a table and select Read Only, 
			//	you can only read data (otherwise no access).
			{
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (ro)
						hasAccess = m_tableAccess[i].isReadOnly();
					else
						hasAccess = false;
					log.debug("isTableAccess - Exclude AD_Table_ID=" + AD_Table_ID 
						+ " (ro="  + ro + ",TableAccessRO=" + m_tableAccess[i].isReadOnly() + ") = " + hasAccess);
					return hasAccess;
				}
			}
			else								//	Include
			//	If you Include Access to a table and select Read Only, 
			//	you can only read data (otherwise full access).
			{
				hasAccess = false;
				if (m_tableAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (!ro)	//	rw only if not r/o
						hasAccess = !m_tableAccess[i].isReadOnly();
					else
						hasAccess = true;
					log.debug("isTableAccess - Include AD_Table_ID=" + AD_Table_ID 
						+ " (ro="  + ro + ",TableAccessRO=" + m_tableAccess[i].isReadOnly() + ") = " + hasAccess);
					return hasAccess;
				}
			}
		}	//	for all Table Access
		if (!hasAccess)
			log.debug("isTableAccess - AD_Table_ID=" + AD_Table_ID 
				+ "(ro="  + ro + ") = " + hasAccess);
		return hasAccess;
	}	//	isTableAccess

	/**
	 * 	Access to Table based on Role User Level Table Access Level
	 *	@param AD_Table_ID table
	 *	@param ro check read only access otherwise read write access level
	 *	@return has RO/RW access to table
	 */
	public boolean isTableAccessLevel (int AD_Table_ID, boolean ro)
	{
		if (ro)				//	role can always read
			return true;
		//
		loadTableInfo(false);
		//	AccessLevel
		//		1 = Org - 2 = Client - 4 = System
		//		3 = Org+Client - 6 = Client+System - 7 = All
		String accessLevel = (String)m_tableAccessLevel.get(new Integer(AD_Table_ID));
		if (accessLevel == null)
		{
			log.debug("isTableAccessLevel - NO - No AccessLevel - AD_Table_ID=" + AD_Table_ID);
			return false;
		}
		//	Access to all User Levels
		if (accessLevel.equals(X_AD_Table.ACCESSLEVEL_All))
			return true;
		//	User Level = SCO
		String userLevel = getUserLevel();
		//	
		if (userLevel.charAt(0) == 'S'
			&& (accessLevel.equals(X_AD_Table.ACCESSLEVEL_SystemOnly) 
				|| accessLevel.equals(X_AD_Table.ACCESSLEVEL_SystemPlusClient)))
			return true;
		if (userLevel.charAt(1) == 'C'
			&& (accessLevel.equals(X_AD_Table.ACCESSLEVEL_ClientOnly) 
				|| accessLevel.equals(X_AD_Table.ACCESSLEVEL_SystemPlusClient)))
			return true;
		if (userLevel.charAt(2) == 'O'
			&& (accessLevel.equals(X_AD_Table.ACCESSLEVEL_Organization) 
				|| accessLevel.equals(X_AD_Table.ACCESSLEVEL_ClientPlusOrganization)))
			return true;
		log.debug("isTableAccessLevel - NO - AD_Table_ID=" + AD_Table_ID 
			+ ", UserLebel=" + userLevel + ", AccessLevel=" + accessLevel);
		return false;
	}	//	isTableAccessLevel


	/**
	 * 	Access to Column
	 *	@param AD_Table_ID table
	 *	@param AD_Column_ID column
	 *	@param ro read only
	 *	@return true if access
	 */
	public boolean isColumnAccess (int AD_Table_ID, int AD_Column_ID, boolean ro)
	{
		if (!isTableAccess(AD_Table_ID, ro))		//	No Access to Table		
			return false;
		loadColumnAccess(false);
		
		boolean retValue = true;		//	assuming exclusive
		for (int i = 0; i < m_columnAccess.length; i++)
		{
			if (m_columnAccess[i].isExclude())		//	Exclude
			//	If you Exclude Access to a column and select Read Only, 
			//	you can only read data (otherwise no access).
			{
				if (m_columnAccess[i].getAD_Table_ID() == AD_Table_ID 
					&& m_columnAccess[i].getAD_Column_ID() == AD_Column_ID)
				{
					if (ro)		//	just R/O Access requested
						retValue = m_columnAccess[i].isReadOnly();
					else
						retValue = false;
					if (!retValue)
						log.debug("isColumnAccess - Exclude AD_Table_ID=" + AD_Table_ID + ", AD_Column_ID=" + AD_Column_ID 
							+ " (ro="  + ro + ",ColumnAccessRO=" + m_columnAccess[i].isReadOnly() + ") = " + retValue);
					return retValue;
				}
			}
			else								//	Include
			//	If you Include Access to a column and select Read Only, 
			//	you can only read data (otherwise full access).
			{
				if (m_columnAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					retValue = false;
					if (m_columnAccess[i].getAD_Column_ID() == AD_Column_ID)
					{
						if (!ro)	//	rw only if not r/o
							retValue = !m_columnAccess[i].isReadOnly();
						else
							retValue = true;
						if (!retValue)
							log.debug("isColumnAccess - Include AD_Table_ID=" + AD_Table_ID + ", AD_Column_ID=" + AD_Column_ID 
								+ " (ro="  + ro + ",ColumnAccessRO=" + m_columnAccess[i].isReadOnly() + ") = " + retValue);
						return retValue;
					}
				}	//	same table
			}	//	include
		}	//	for all Table Access
		if (!retValue)
			log.debug("isColumnAccess - AD_Table_ID=" + AD_Table_ID + ", AD_Column_ID=" + AD_Column_ID 
				+ " (ro="  + ro + ") = " + retValue);
		return retValue;
	}	//	isColumnAccess

	/**
	 *	Access to Record
	 *	@param AD_Table_ID table
	 *	@param Record_ID
	 *	@param ro
	 *	@return
	 */
	public boolean isRecordAccess (int AD_Table_ID, int Record_ID, boolean ro)
	{
		if (!isTableAccess(AD_Table_ID, ro))		//	No Access to Table
			return false;
		loadRecordAccess(false);
		
		boolean negativeList = true;
		for (int i = 0; i < m_tableAccess.length; i++)
		{
			if (m_recordAccess[i].isExclude())		//	Exclude
			//	If you Exclude Access to a column and select Read Only, 
			//	you can only read data (otherwise no access).
			{
				if (m_recordAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (ro)
						return m_tableAccess[i].isReadOnly();
					else
						return false;
				}
			}
			else								//	Include
			//	If you Include Access to a column and select Read Only, 
			//	you can only read data (otherwise full access).
			{
				negativeList = false;
				if (m_recordAccess[i].getAD_Table_ID() == AD_Table_ID)
				{
					if (!ro)	//	rw only if not r/o
						return !m_tableAccess[i].isReadOnly();
					else
						return true;
				}
			}
		}	//	for all Table Access
		return negativeList;
	}	//	isRecordAccess

	/**
	 * 	Get Window Access
	 *	@param AD_Window_ID window
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getWindowAccess (int AD_Window_ID)
	{
		if (m_windowAccess == null)
		{
			m_windowAccess = new HashMap(100);
			String sql = "SELECT AD_Window_ID, IsReadWrite FROM AD_Window_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareCall(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_windowAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getWindowAccess", e);
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
			log.debug("getWindowAccess #" + m_windowAccess.size());
		}	//	reload
		Boolean retValue = (Boolean)m_windowAccess.get(new Integer(AD_Window_ID));
	//	log.debug("getWindowAccess - AD_Window_ID=" + AD_Window_ID + " - " + retValue);
		return retValue;
	}	//	getWindowAccess

	/**
	 * 	Get Process Access
	 *	@param AD_Process_ID process
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getProcessAccess (int AD_Process_ID)
	{
		if (m_processAccess == null)
		{
			m_processAccess = new HashMap(50);
			String sql = "SELECT AD_Process_ID, IsReadWrite FROM AD_Process_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareCall(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_processAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getProcessAccess", e);
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
		}	//	reload
		return (Boolean)m_processAccess.get(new Integer(AD_Process_ID));
	}	//	getProcessAccess

	/**
	 * 	Get Task Access
	 *	@param AD_Task_ID task
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getTaskAccess (int AD_Task_ID)
	{
		if (m_taskAccess == null)
		{
			m_taskAccess = new HashMap(10);
			String sql = "SELECT AD_Task_ID, IsReadWrite FROM AD_Task_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareCall(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_taskAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getTaskAccess", e);
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
		}	//	reload
		return (Boolean)m_taskAccess.get(new Integer(AD_Task_ID));
	}	//	getTaskAccess

	/**
	 * 	Get Form Access
	 *	@param AD_Form_ID form
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getFormAccess (int AD_Form_ID)
	{
		if (m_formAccess == null)
		{
			m_formAccess = new HashMap(20);
			String sql = "SELECT AD_Form_ID, IsReadWrite FROM AD_Form_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareCall(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_formAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getFormAccess", e);
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
		}	//	reload
		return (Boolean)m_formAccess.get(new Integer(AD_Form_ID));
	}	//	getTaskAccess

	/**
	 * 	Get Workflow Access
	 *	@param AD_Workflow_ID workflow
	 *	@return null in no access, TRUE if r/w and FALSE if r/o
	 */
	public Boolean getWorkflowAccess (int AD_Workflow_ID)
	{
		if (m_workflowAccess == null)
		{
			m_workflowAccess = new HashMap(20);
			String sql = "SELECT AD_Workflow_ID, IsReadWrite FROM AD_Workflow_Access WHERE AD_Role_ID=? AND IsActive='Y'";
			PreparedStatement pstmt = null;
			try
			{
				pstmt = DB.prepareCall(sql);
				pstmt.setInt(1, getAD_Role_ID());
				ResultSet rs = pstmt.executeQuery();
				while (rs.next())
					m_workflowAccess.put(new Integer(rs.getInt(1)), new Boolean("Y".equals(rs.getString(2))));
				rs.close();
				pstmt.close();
				pstmt = null;
			}
			catch (Exception e)
			{
				log.error("getWorkflowAccess", e);
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
		}	//	reload
		return (Boolean)m_workflowAccess.get(new Integer(AD_Workflow_ID));
	}	//	getTaskAccess

	/*************************************************************************

	/**
	 *	Appends where clause to SQL statement for Table
	 *
	 *	@param SQL			existing SQL statement
	 *	@param TableNameIn	Table Name or list of table names AAA, BBB or AAA a, BBB b
	 *	@param fullyQualified	fullyQualified names
	 *	@param rw			if false, includes System Data
	 *	@return				updated SQL statement
	 */
	public String addAccessSQL (String SQL, String TableNameIn, 
		boolean fullyQualified, boolean rw)
	{
		StringBuffer retSQL = new StringBuffer();

		//	Cut off last ORDER BY clause
		String orderBy = "";
		int posOrder = SQL.lastIndexOf(" ORDER BY ");
		if (posOrder != -1)
		{
			orderBy = SQL.substring(posOrder);
			retSQL.append(SQL.substring(0, posOrder));
		}
		else
			retSQL.append(SQL);

		//	Parse SQL
		AccessSqlParser asp = new AccessSqlParser(retSQL.toString());
		AccessSqlParser.TableInfo[] ti = asp.getTableInfo(asp.getMainSqlIndex()); 

		//  Do we have to add WHERE or AND
		if (asp.getMainSql().indexOf(" WHERE ") == -1)
			retSQL.append(" WHERE ");
		else
			retSQL.append(" AND ");

		//	Use First Table
		String tableName = "";
		if (ti.length > 0)
		{
			tableName = ti[0].getSynonym();
			if (tableName.length() == 0)
				tableName = ti[0].getTableName();
		}
		if (!tableName.equals(TableNameIn) && TableNameIn != null)
		{
			String msg = "addAccessSQL - TableName not correctly parsed - TableNameIn=" 
				+ TableNameIn + " - " + asp;
			if (ti.length > 0)
				msg += " - #1 " + ti[0]; 
			msg += "\n = " + SQL;
			log.error(msg);
			Trace.printStack();
			tableName = TableNameIn;
		}

		//	Client Access
		if (fullyQualified)
			retSQL.append(tableName).append(".");
		retSQL.append(getClientWhere(rw));
		
		//	Org Access
		retSQL.append(" AND ");
		if (fullyQualified)
			retSQL.append(tableName).append(".");
		retSQL.append(getOrgWhere(rw));

			
		//	** Data Access	**
		for (int i = 0; i < ti.length; i++)
		{
			String TableName = ti[i].getTableName();
			int AD_Table_ID = getAD_Table_ID (TableName);
			//	Data Table Access
			if (AD_Table_ID != 0 && !isTableAccess(AD_Table_ID, !rw))
			{
				retSQL.append(" AND 1=3");	//	prevent access at all
				log.debug("addAccessSQL - no access to AD_Table_ID=" + AD_Table_ID 
					+ " - " + TableName + " - " + retSQL);
				break;	//	no need to check further 
			}
			
			//	Data Column Access
	
	
		
			//	Data Record Access
			String keyColumnName = "";
			if (fullyQualified)
			{
				keyColumnName = ti[i].getSynonym();	//	table synonym
				if (keyColumnName.length() == 0)
					keyColumnName = TableName;
				keyColumnName += ".";
			}
			keyColumnName += TableName + "_ID";	//	derived from table
	
		//	log.debug("addAccessSQL - " + TableName + "(" + AD_Table_ID + ") " + keyColumnName);
			String recordWhere = getRecordWhere (AD_Table_ID, keyColumnName);
			if (recordWhere.length() > 0)
			{
				retSQL.append(" AND ").append(recordWhere);
				if (Log.isTraceLevel(9))
					log.debug("addAccessSQL - adding record access - " + recordWhere);
			}
		}	//	for all table info
		
		//	Dependent Records (only for main SQL)
		String mainSql = asp.getMainSql();
		loadRecordAccess(false);
		for (int i = 0; i < m_recordDependentAccess.length; i++)
		{
			String columnName = m_recordDependentAccess[i].getKeyColumnName(asp.getTableInfo(asp.getMainSqlIndex()) );
			if (columnName == null)
				continue;	//	no key column
			int posColumn = mainSql.indexOf(columnName);
			if (posColumn == -1)
				continue;
			//	we found the column name - make sure it's a clumn name
			char charCheck = mainSql.charAt(posColumn-1);	//	before
			if (!(charCheck == ',' || charCheck == '.' || charCheck == ' ' || charCheck == '('))
				continue;
			charCheck = mainSql.charAt(posColumn+columnName.length());	//	after
			if (!(charCheck == ',' || charCheck == ' ' || charCheck == ')'))
				continue;
				
			//	*** we found the column in the main query
			log.debug("DEPENDENT  " + columnName + " - " + m_recordDependentAccess[i]);
			StringBuffer where = new StringBuffer();
			where.append(getDependentRecordWhereColumn (mainSql, columnName))
				.append((m_recordDependentAccess[i].isExclude() ? "<>" : "="))
				.append(m_recordDependentAccess[i].getRecord_ID());
			if (Log.isTraceLevel(9))
				log.debug("addAccessSQL - adding dependent access - " + where);
			retSQL.append(" AND ").append(where);			
		}	//	for all dependent records
		
		//
		retSQL.append(orderBy);
		if (Log.isTraceLevel(10))
			log.debug("addAccessSQL - " + retSQL);
		return retSQL.toString();
	}	//	addAccessSQL

	/**
	 * 	Get Dependent Record Where clause
	 *	@param mainSql sql to examine
	 *	@param columnName columnName
	 *	@return where clause column "x.columnName"
	 */
	private String getDependentRecordWhereColumn (String mainSql, String columnName)
	{
		String retValue = columnName;	//	if nothing else found
		int index = mainSql.indexOf(columnName);
		//	see if there are table synonym
		int offset = index - 1;
		char c = mainSql.charAt(offset);
		if (c == '.')
		{
			StringBuffer sb = new StringBuffer();
			while (c != ' ' && c != ',' && c != '(')	//	delimeter
			{
				sb.insert(0, c);
				c = mainSql.charAt(--offset);
			}
			sb.append(columnName);
			return sb.toString();
		}
		return retValue;
	}	//	getDependentRecordWhereColumn



	/**
	 *	UPADATE - Can I Update the record
	 *  Access error info (AccessTableNoUpdate) is saved in the log
	 * 
	 *  @see org.compiere.model.MTable#save(boolean)
	 *
	 *  @param ctx comntext to derive client/org/user level
	 *	@param	WindowNo	number of the current window to retrieve context
	 *	@return	true if you can update
	 */
	public boolean canUpdate (int AD_Client_ID, int AD_Org_ID, 
		int AD_Table_ID, boolean createError)
	{
		String userLevel = getUserLevel();	//	Format 'SCO'

		if (userLevel.indexOf("S") != -1)		//	System can change anything
			return true;

		boolean	retValue = true;
		String whatMissing = "";

		//	System == Client=0 & Org=0
		if (AD_Client_ID == 0 && AD_Org_ID == 0
			&& userLevel.charAt(0) != 'S')
		{
			retValue = false;
			whatMissing += "S";
		}

		//	Client == Client!=0 & Org=0
		else if (AD_Client_ID != 0 && AD_Org_ID == 0
			&& userLevel.charAt(1) != 'C')
		{
			retValue = false;
			whatMissing += "C";
		}

		//	Organization == Client!=0 & Org!=0
		else if (AD_Client_ID != 0 && AD_Org_ID != 0
			&& userLevel.charAt(2) != 'O')
		{
			retValue = false;
			whatMissing += "O";
		}

		//	Data Access
		//	get Table.IsSecurityEnabled
		//	if yes: get UserAcess info => Where Table.TableKey in (List)
		//									and Access=r/w

		if (!retValue && createError)
		{
			Log.saveError("AccessTableNoUpdate",
				"AD_Client_ID=" + AD_Client_ID + ", AD_Org_ID=" + AD_Org_ID + ", UserLevel=" + userLevel
				+ " => missing=" + whatMissing);
			log.error ("canUpdate - " + toString());
		}
		return retValue;
	}	//	canUpdate

	/**
	 *	VIEW - Can I view record in Table with given TableLevel
	 *  <code>
	 *	TableLevel			S__ 100		4	System info
	 *						SCO	111		7	System shared info
	 *						SC_ 110		6	System/Client info
	 *						_CO	011		3	Client shared info
	 *						_C_	011		2	Client shared info
	 *						__O	001		1	Organization info
	 *  </code>
	 * 
	 * 	@see org.compiere.model.MTabVO#loadTabDetails (MTabVO, ResultSet)
	 *  @param ctx	context
	 *	@param TableLevel	AccessLevel
	 *	@param forInsert	true for Insert, false for View
	 *	@return	true/false
	 *  Access error info (AccessTableNoUpdate, AccessTableNoView) is saved in the log
	 */
	public boolean canView(Properties ctx, String TableLevel)
	{
		String userLevel = getUserLevel();	//	Format 'SCO'

		boolean retValue = true;

		//	7 - All
		if (X_AD_Table.ACCESSLEVEL_All.equals(TableLevel))
			retValue = true;
			 
		//	4 - System data requires S
		else if (X_AD_Table.ACCESSLEVEL_SystemOnly.equals(TableLevel) 
			&& userLevel.charAt(0) != 'S')
			retValue = false;

		//	2 - Client data requires C
		else if (X_AD_Table.ACCESSLEVEL_ClientOnly.equals(TableLevel) 
			&& userLevel.charAt(1) != 'C')
			retValue = false;

		//	1 - Organization data requires O
		else if (X_AD_Table.ACCESSLEVEL_Organization.equals(TableLevel) 
			&& userLevel.charAt(2) != 'O')
			retValue = false;

		//	3 - Client Shared requires C or O
		else if (X_AD_Table.ACCESSLEVEL_ClientPlusOrganization.equals(TableLevel)
			&& (!(userLevel.charAt(1) == 'C' || userLevel.charAt(2) == 'O')) )
				retValue = false;

		//	6 - System/Client requires S or C
		else if (X_AD_Table.ACCESSLEVEL_SystemPlusClient.equals(TableLevel)
			&& (!(userLevel.charAt(0) == 'S' || userLevel.charAt(1) == 'C')) )
			retValue = false;

		if (retValue)
			return retValue;

		//  Notification
		/**
		if (forInsert)
			Log.saveError("AccessTableNoUpdate",
				"(Required=" + TableLevel + "("
				+ getTableLevelString(Env.getAD_Language(ctx), TableLevel)
				+ ") != UserLevel=" + userLevel);
		else
		**/
			Log.saveError("AccessTableNoView",
				"Required=" + TableLevel + "("
				+ getTableLevelString(Env.getAD_Language(ctx), TableLevel)
				+ ") != UserLevel=" + userLevel);
		log.error ("canView - " + toString());
		return retValue;
	}	//	canView


	/**
	 *	Returns clear text String of TableLevel
	 *  @param AD_Language language
	 *  @param TableLevel level
	 *  @return info
	 */
	private String getTableLevelString (String AD_Language, String TableLevel)
	{
		String level = TableLevel + "??";
		if (TableLevel.equals("1"))
			level = "AccessOrg";
		else if (TableLevel.equals("2"))
			level = "AccessClient";
		else if (TableLevel.equals("3"))
			level = "AccessClientOrg";
		else if (TableLevel.equals("4"))
			level = "AccessSystem";
		else if (TableLevel.equals("6"))
			level = "AccessSystemClient";
		else if (TableLevel.equals("7"))
			level = "AccessShared";

		return Msg.getMsg(AD_Language, level);
	}	//	getTableLevelString

	/**
	 * 	Get Table ID from name
	 *	@param tableName table name
	 *	@return AD_Table_ID or 0
	 */
	private int getAD_Table_ID (String tableName)
	{
		loadTableInfo(false);
		Integer ii = (Integer)m_tableName.get(tableName);
		if (ii != null)
			return ii.intValue();
	//	log.warn("getAD_Table_ID - not found (" + tableName + ")");
		return 0;
	}	//	getAD_Table_ID

	/**
	 * 	Return Where clause for Record Access
	 *	@param AD_Table_ID table
	 *	@param keyColumnName (fully qualified) key column name
	 *	@return where clause or ""
	 */
	private String getRecordWhere (int AD_Table_ID, String keyColumnName)
	{
		loadRecordAccess(false);
		//
		StringBuffer sb = new StringBuffer();
		//	Role Access
		for (int i = 0; i < m_recordAccess.length; i++)
		{
			if (m_recordAccess[i].getAD_Table_ID() == AD_Table_ID)
			{
				if (sb.length() > 0)
					sb.append(" AND ");
				sb.append(keyColumnName)
					.append((m_recordAccess[i].isExclude() ? "<>" : "="))
					.append(m_recordAccess[i].getRecord_ID());
			}
		}	//	for all Table Access
		
		//	Don't ignore Privacy Access
		if (!isPersonalAccess())
		{
			String lockedIDs = MPrivateAccess.getLockedRecordWhere(AD_Table_ID, m_AD_User_ID);
			if (lockedIDs != null)
			{
				if (sb.length() > 0)
					sb.append(" AND ");
				sb.append(keyColumnName).append(lockedIDs);
			}
		}
		//
		return sb.toString();
	}	//	getRecordWhere

}	//	MRole
