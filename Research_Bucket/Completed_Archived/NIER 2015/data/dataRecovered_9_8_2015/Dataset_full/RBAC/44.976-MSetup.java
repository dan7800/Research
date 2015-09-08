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
package org.compiere.model;

import java.util.*;
import java.sql.*;
import java.io.*;

import org.compiere.util.*;

/**
 * Initial Setup Model
 *
 * @author Jorg Janke
 * @version $Id: MSetup.java,v 1.25 2003/07/22 18:49:02 jjanke Exp $
 */
public final class MSetup
{
	/**
	 *  Constructor
	 *  @param ctx context
	 *  @param WindowNo window
	 */
	public MSetup(Properties ctx, int WindowNo)
	{
		m_ctx = ctx;
		m_lang = Env.getAD_Language(m_ctx);
		m_WindowNo = WindowNo;
	}   //  MSetup

	private Properties      m_ctx;
	private String          m_lang;
	private int             m_WindowNo;
	private StringBuffer    m_info;
	//
	private String          m_clientName;
	private String          m_orgName;
	//
	private String          m_stdColumns = "AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy";
	private String          m_stdValues;
	private String          m_stdValuesOrg;
	//
	private NaturalAccountMap m_nap = null;
	//
	private final String    CompiereSys = "N";           //  Should NOT be changed
	private int             AD_Client_ID = 1000000;     //  initial Client_ID
	//
	private int     AD_Org_ID;
	private int     AD_User_ID;
	private String  AD_User_Name;
	private int     AD_User_U_ID;
	private String  AD_User_U_Name;
	private int     AD_Role_ID;
	private int     C_Calendar_ID;
	private int     C_AcctSchema_ID;
	private int     C_Currency_ID;
	private int     AD_Tree_Account_ID;
	private int     C_Cycle_ID;
	//
	private boolean         m_hasProject = false;
	private boolean         m_hasMCampaign = false;
	private boolean         m_hasSRegion = false;
	private AcctSchema      m_AcctSchema = null;


	/**
	 *  Create Client Info.
	 *  - Client, Trees, Org, Role, User, User_Role
	 *  @param clientName client name
	 *  @param orgName org name
	 *  @param userClient user id client
	 *  @param userOrg user id org
	 *  @return true if created
	 */
	public boolean createClient (String clientName, String orgName,
		String userClient, String userOrg)
	{
		Log.trace(Log.l3_Util, "MSetup.createClient");
		//  info header
		m_info = new StringBuffer();
		//  Standarc columns
		String name = null;
		String sql = null;
		int no = 0;

		/**
		 *  Create Client
		 */
		Env.setContext(m_ctx, "#CompiereSys", CompiereSys);
		AD_Client_ID = DB.getKeyNextNo (AD_Client_ID, CompiereSys, "AD_Client");
		Env.setContext(m_ctx, m_WindowNo, "AD_Client_ID", AD_Client_ID);

		//	Standard Values
		m_stdValues = String.valueOf(AD_Client_ID) + ",0,'Y',SysDate,0,SysDate,0";

		//	Create Client
		name = clientName;
		if (name == null || name.length() == 0)
			name = "newClient";
		name = name.trim();
		m_clientName = name;
		sql = "INSERT INTO AD_Client(" + m_stdColumns + ",Value,Name,Description)"
			+ " VALUES (" + m_stdValues + ",'" + name + "','" + name + "','" + name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Client NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info - Client
		m_info.append(Msg.translate(m_lang, "AD_Client_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Trees
		 */
		String sql2 = null;
		if (Env.isBaseLanguage(m_lang, "AD_Ref_List"))	//	Get TreeTypes & Name
			sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=120";
		else
			sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Reference_ID=120 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID";

		//  Tree IDs
		int AD_Tree_Org_ID=0, AD_Tree_BPartner_ID=0, AD_Tree_Project_ID=0,
			AD_Tree_SalesRegion_ID=0, AD_Tree_Product_ID=0;

		try
		{
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next())
			{
				String value = rs.getString(1);
				int AD_Tree_ID = 0;
				if (value.equals("OO"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Org_ID = AD_Tree_ID;
				}
				else if (value.equals("BP"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_BPartner_ID = AD_Tree_ID;
				}
				else if (value.equals("PJ"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Project_ID = AD_Tree_ID;
				}
				else if (value.equals("SR"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_SalesRegion_ID = AD_Tree_ID;
				}
				else if (value.equals("PR"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Product_ID = AD_Tree_ID;
				}
				else if (value.endsWith("EV"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Account_ID = AD_Tree_ID;
				}
				else if (value.equals("MM"))	//	No Menu
					;
				else
					//	PC (Product Category), BB (BOM), MC (Marketing Campaign), AY (Activity)
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");

				//
				if (AD_Tree_ID != 0)
				{
					name = m_clientName + " " + rs.getString(2);
					sql = "INSERT INTO AD_Tree(" + m_stdColumns + ",AD_Tree_ID,Name,Description,TreeType)"
						+ " VALUES (" + m_stdValues + "," + AD_Tree_ID + ",'" + name + "','" + name + "','" + value + "')";
					no = DB.executeUpdate(sql);
					if (no == 1)
						m_info.append(Msg.translate(m_lang, "AD_Tree_ID")).append("=").append(name).append("\n");
					else
						Log.error("MSetup.createClient - Tree NOT created: " + name);
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e1)
		{
			Log.error ("MSetup.createClient - Trees", e1);
		}

		//	Get Primary Tree
		int AD_Tree_Menu_ID = 10;	//	hardcoded

		//	Create ClientInfo
		sql = "INSERT INTO AD_ClientInfo(" + m_stdColumns + ",Acct2_Active,Acct3_Active,"
			+ "AD_Tree_Menu_ID,AD_Tree_Org_ID,AD_Tree_BPartner_ID,AD_Tree_Project_ID,AD_Tree_SalesRegion_ID,AD_Tree_Product_ID)"
			+ " VALUES (" + m_stdValues + ",'N','N',"
			+ AD_Tree_Menu_ID + "," + AD_Tree_Org_ID + "," + AD_Tree_BPartner_ID + "," + AD_Tree_Project_ID + "," + AD_Tree_SalesRegion_ID + "," + AD_Tree_Product_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - ClientInfo NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
	//	m_info.append(Msg.translate(m_lang, "AD_ClientInfo")).append("\n");

		/**
		 *  Create Org
		 */
		AD_Org_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Org");
		m_stdValuesOrg = AD_Client_ID + "," + AD_Org_ID + ",'Y',SysDate,0,SysDate,0";

		name = orgName;
		if (name == null || name.length() == 0)
			name = "newOrg";
		m_orgName = name;
		sql = "INSERT INTO AD_Org (" + m_stdColumns + ",Value,Name,IsSummary)"
			+ " VALUES (" +	m_stdValuesOrg + ",'" + name + "','" + name + "','N')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Org NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_Org_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Roles
		 *  - Admin
		 *  - User
		 */
		name = m_clientName + " Admin";
		AD_Role_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Role");
		sql = "INSERT INTO AD_Role(" + m_stdColumns + ",AD_Role_ID,"
			+ "Name,Description,UserLevel,ClientList,OrgList)"
			+ " VALUES (" + m_stdValues + "," + AD_Role_ID + ","
			+ "'" + name + "','" + name + "',' CO','" + AD_Client_ID + "','0," + AD_Org_ID + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin Role A NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  OrgAccess x,0
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValues + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_OrgAccess 0 NOT created");
		//  OrgAccess x,y
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValuesOrg + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_OrgAccess NOT created");
		//  Info - Admin Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		//
		name = m_clientName + " User";
		int AD_Role_ID_U = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Role");
		sql = "INSERT INTO AD_Role(" + m_stdColumns + ",AD_Role_ID,"
			+ "Name,Description,UserLevel,ClientList,OrgList)"
			+ " VALUES (" + m_stdValues + "," + AD_Role_ID_U + ","
			+ "'" + name + "','" + name + "','  O','" + AD_Client_ID + "','" + AD_Org_ID + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - User Role A NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  OrgAccess x,y
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValuesOrg + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - User Role_OrgAccess NOT created");
		//  Info - Client Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Users
		 *  - Client
		 *  - Org
		 */
		name = userClient;
		if (name == null || name.length() == 0)
			name = m_clientName + "Client";
		AD_User_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_User");
		AD_User_Name = name;
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + ","
			+ "'" + AD_User_Name + "','" + AD_User_Name + "','" + AD_User_Name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin User NOT inserted - " + AD_User_Name;
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_Name).append("/").append(AD_User_Name).append("\n");

		name = userOrg;
		if (name == null || name.length() == 0)
			name = m_clientName + "Org";
		AD_User_U_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_User");
		AD_User_U_Name = name;
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + ","
			+ "'" + AD_User_U_Name + "','" + AD_User_U_Name + "','" + AD_User_U_Name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Org User NOT inserted - " + AD_User_U_Name;
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_U_Name).append("/").append(AD_User_U_Name).append("\n");

		/**
		 *  Create User-Role
		 */
		//  ClientUser          - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+Admin NOT inserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+User NOT inserted");
		//  OrgUser             - User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole OrgUser+Org NOT inserted");
		//  SuperUser(100)      - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + ",100," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole SuperUser+Admin NOT inserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + ",100," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole SuperUser+User NOT inserted");

		return true;
	}   //  createClient



	/*************************************************************************/

	/**
	 *  Create Accounting elements.
	 *  - Calendar
	 *  - Account Trees
	 *  - Account Values
	 *  - Accounting Schema
	 *  - Default Accounts
	 *
	 *  @param newC_Currency_ID currency
	 *  @param curName currency name
	 *  @param hasProduct has product segment
	 *  @param hasBPartner has bp segment
	 *  @param hasProject has project segment
	 *  @param hasMCampaign has campaign segment
	 *  @param hasSRegion has sales region segment
	 *  @param AccountingFile file name of accounting file
	 *  @return true if created
	 */
	public boolean createAccounting(int newC_Currency_ID, String curName,
		boolean hasProduct, boolean hasBPartner, boolean hasProject,
		boolean hasMCampaign, boolean hasSRegion,
		File AccountingFile)
	{
		Log.trace(Log.l3_Util, "MSetup.createAccount");
		//
		C_Currency_ID = newC_Currency_ID;
		m_hasProject = hasProject;
		m_hasMCampaign = hasMCampaign;
		m_hasSRegion = hasSRegion;

		//  Standard variables
		m_info = new StringBuffer();
		String name = null;
		StringBuffer sqlCmd = null;
		int no = 0;

		/**
		 *  Create Calendar
		 */
		C_Calendar_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Calendar");
		name = m_clientName + " " + Msg.translate(m_lang, "C_Calendar_ID");
		sqlCmd = new StringBuffer("INSERT INTO C_Calendar(");
		sqlCmd.append(m_stdColumns).append(",C_Calendar_ID,Name)").append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_Calendar_ID).append(",'").append(name).append("')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Calendar NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_Calendar_ID")).append("=").append(name).append("\n");

		//  Year
		int C_Year_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Year");
		sqlCmd = new StringBuffer ("INSERT INTO C_Year ");
		sqlCmd.append("(C_Year_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Year,C_Calendar_ID) VALUES (");
		sqlCmd.append(C_Year_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY'),").append(C_Calendar_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createAccounting - Year NOT inserted");
		/** @todo Create Periods */


		//	Create Account Elements
		int C_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Element");
		name = m_clientName + " " + Msg.translate(m_lang, "Account_ID");
		sqlCmd = new StringBuffer ("INSERT INTO C_ELEMENT(");
		sqlCmd.append(m_stdColumns).append(",C_Element_ID,Name,Description,")
			.append("VFormat,ElementType,IsBalancing,IsNaturalAccount,AD_Tree_ID)").append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_Element_ID).append(",'").append(name).append("','").append(name).append("',")
			.append("NULL,'A','N','Y',").append(AD_Tree_Account_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Acct Element NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		m_info.append(Msg.translate(m_lang, "C_Element_ID")).append("=").append(name).append("\n");

		//	Create Account Values
		m_nap = new NaturalAccountMap(m_ctx);
		String errMsg = m_nap.parseFile(AccountingFile);
		if (errMsg.length() != 0)
		{
			Log.error(errMsg);
			m_info.append(errMsg);
			return false;
		}
		if (m_nap.createAccounts(AD_Client_ID, AD_Org_ID, C_Element_ID))
			m_info.append(Msg.translate(m_lang, "C_ElementValue_ID")).append(" # ").append(m_nap.size()).append("\n");
		else
		{
			String err = "MSetup.createAccounting - Acct Element Values NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		int C_ElementValue_ID = m_nap.getC_ElementValue_ID("DEFAULT_ACCT");
		Log.trace(Log.l4_Data, "C_ElementValue_ID=" + C_ElementValue_ID);

		/**
		 *  Create AccountingSchema
		 */
		C_AcctSchema_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema");
		//
		String GAAP = "US";				//	AD_Reference_ID=123
		String CostingMethod = "A";		//	AD_Reference_ID=122
		name = m_clientName + " " + GAAP + "/" + CostingMethod + "/" + curName;
		//
		sqlCmd = new StringBuffer ("INSERT INTO C_AcctSchema(");
		sqlCmd.append(m_stdColumns).append(",C_AcctSchema_ID,Name,")
			.append("GAAP,IsAccrual,CostingMethod,C_Currency_ID,")
			.append("AutoPeriodControl,Separator,HasAlias,HasCombination)")
			.append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",'").append(name).append("',")
			.append("'").append(GAAP).append("','Y','").append(CostingMethod).append("',").append(C_Currency_ID).append(",")
			.append("'N','-','Y','N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - AcctSchema NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_AcctSchema_ID")).append("=").append(name).append("\n");

		/**
		 *  Create AccountingSchema Elements (Structure)
		 */
		String sql2 = null;
		if (Env.isBaseLanguage(m_lang, "AD_Reference"))	//	Get TreeTypes & Name
			sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=181";
		else
			sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Reference_ID=181 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID";
		//
		int Element_OO=0, Element_AC=0, Element_PR=0, Element_BP=0, Element_PJ=0,
			Element_MC=0, Element_SR=0;
		try
		{
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next())
			{
				String ElementType = rs.getString(1);
				name = rs.getString(2);
				//
				String IsMandatory = null;
				String IsBalanced = "N";
				int SeqNo = 0;
				int C_AcctSchema_Element_ID = 0;

				if (ElementType.equals("OO"))
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_OO = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					IsBalanced = "Y";
					SeqNo = 10;
				}
				else if (ElementType.equals("AC"))
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_AC = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					SeqNo = 20;
				}
				else if (ElementType.equals("PR") && hasProduct)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_PR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 30;
				}
				else if (ElementType.equals("BP") && hasBPartner)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_BP = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 40;
				}
				else if (ElementType.equals("PJ") && hasProject)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_PJ = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 50;
				}
				else if (ElementType.equals("MC") && hasMCampaign)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_MC = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 60;
				}
				else if (ElementType.equals("SR") && hasSRegion)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_SR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 70;
				}
				//	Not OT, LF, LT, U1, U2, AY

				if (IsMandatory != null)
				{
					sqlCmd = new StringBuffer ("INSERT INTO C_AcctSchema_Element(");
					sqlCmd.append(m_stdColumns).append(",C_AcctSchema_Element_ID,C_AcctSchema_ID,")
						.append("ElementType,Name,SeqNo,IsMandatory,IsBalanced) VALUES (");
					sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_Element_ID).append(",").append(C_AcctSchema_ID).append(",")
						.append("'").append(ElementType).append("','").append(name).append("',").append(SeqNo).append(",'")
						.append(IsMandatory).append("','").append(IsBalanced).append("')");
					no = DB.executeUpdate(sqlCmd.toString());
					if (no == 1)
						m_info.append(Msg.translate(m_lang, "C_AcctSchema_Element_ID")).append("=").append(name).append("\n");

					/** Default value for mandatory elements: OO and AC */
					if (ElementType.equals("OO"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET Org_ID=");
						sqlCmd.append(AD_Org_ID).append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Org in AcctSchamaElement NOT updated");
					}
					if (ElementType.equals("AC"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET C_ElementValue_ID=");
						sqlCmd.append(C_ElementValue_ID).append(", C_Element_ID=").append(C_Element_ID);
						sqlCmd.append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Account in AcctSchamaElement NOT updated");
					}
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e1)
		{
			Log.error ("MSetup.createAccounting - Elements", e1);
		}
		//  Create AcctSchema
		m_AcctSchema = new AcctSchema (C_AcctSchema_ID);


		//  Create GL Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_GL (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "USESUSPENSEBALANCING,SUSPENSEBALANCING_ACCT,"
			+ "USESUSPENSEERROR,SUSPENSEERROR_ACCT,"
			+ "USECURRENCYBALANCING,CURRENCYBALANCING_ACCT,"
			+ "RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,"
			+ "INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT,"
			+ "PPVOFFSET_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEBALANCING_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEERROR_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("CURRENCYBALANCING_ACCT")).append(",");
		//  RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,
		sqlCmd.append(getAcct("RETAINEDEARNING_ACCT")).append(",");
		sqlCmd.append(getAcct("INCOMESUMMARY_ACCT")).append(",");
		//  INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT)
		sqlCmd.append(getAcct("INTERCOMPANYDUETO_ACCT")).append(",");
		sqlCmd.append(getAcct("INTERCOMPANYDUEFROM_ACCT")).append(",");
		sqlCmd.append(getAcct("PPVOFFSET_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - GL Accounts NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//	Create Std Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_DEFAULT (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT, "
			+ "P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT, "
			+ "P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT, "
			+ "C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT, "
			+ "V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT, "
			+ "PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT, "
			+ "UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT, "
			+ "WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT, "
			+ "PJ_ASSET_ACCT,PJ_WIP_ACCT,"
			+ "T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT, "
			+ "B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,"
			+ "B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,"
			+ "B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT, "
			+ "CH_EXPENSE_ACCT,CH_REVENUE_ACCT, "
			+ "UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT, "
			+ "CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",");
		//  W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT
		sqlCmd.append(getAcct("W_INVENTORY_ACCT")).append(",");
		sqlCmd.append(getAcct("W_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("W_REVALUATION_ACCT")).append(",");
		sqlCmd.append(getAcct("W_INVACTUALADJUST_ACCT")).append(", ");
		//  P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT,
		sqlCmd.append(getAcct("P_REVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("P_COGS_ACCT")).append(", ");
		//  P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT,
		sqlCmd.append(getAcct("P_PURCHASEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_INVOICEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTREC_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTGRANT_ACCT")).append(", ");
		//  C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("C_RECEIVABLE_ACCT")).append(",");
		sqlCmd.append(getAcct("C_PREPAYMENT_ACCT")).append(", ");
		//  V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("V_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("V_LIABILITY_SERVICES_ACCT")).append(",");
		sqlCmd.append(getAcct("V_PREPAYMENT_ACCT")).append(", ");
		//  PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT,
		sqlCmd.append(getAcct("PAYDISCOUNT_EXP_ACCT")).append(",");
		sqlCmd.append(getAcct("PAYDISCOUNT_REV_ACCT")).append(",");
		sqlCmd.append(getAcct("WRITEOFF_ACCT")).append(", ");
		//  UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT,
		sqlCmd.append(getAcct("UNREALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("UNREALIZEDLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDLOSS_ACCT")).append(", ");
		//  WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT,
		sqlCmd.append(getAcct("WITHHOLDING_ACCT")).append(",");
		sqlCmd.append(getAcct("E_PREPAYMENT_ACCT")).append(",");
		sqlCmd.append(getAcct("E_EXPENSE_ACCT")).append(", ");
		//  PJ_ASSET_ACCT,PJ_WIP_ACCT,
		sqlCmd.append(getAcct("PJ_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("PJ_WIP_ACCT")).append(",");
		//  T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT,
		sqlCmd.append(getAcct("T_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("T_RECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("T_DUE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_CREDIT_ACCT")).append(", ");
		//  B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,
		sqlCmd.append(getAcct("B_INTRANSIT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("B_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTREV_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTEXP_ACCT")).append(",");
		//  B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,
		sqlCmd.append(getAcct("B_UNIDENTIFIED_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTLOSS_ACCT")).append(",");
		//  B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT,
		sqlCmd.append(getAcct("B_REVALUATIONGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_REVALUATIONLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("B_PAYMENTSELECT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_UNALLOCATEDCASH_ACCT")).append(", ");
		//  CH_EXPENSE_ACCT,CH_REVENUE_ACCT,
		sqlCmd.append(getAcct("CH_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CH_REVENUE_ACCT")).append(", ");
		//  UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT,
		sqlCmd.append(getAcct("UNEARNEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIPTS_ACCT")).append(", ");
		//  CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT)
		sqlCmd.append(getAcct("CB_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_CASHTRANSFER_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_RECEIPT_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Default Accounts NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//  GL Categories
		createGLCategory("Standard", "M", true);
		int GL_None = createGLCategory("None", "D", false);
		int GL_GL = createGLCategory("Manual", "M", false);
		int GL_ARI = createGLCategory("AR Invoice", "D", false);
		int GL_ARR = createGLCategory("AR Receipt", "D", false);
		int GL_MM = createGLCategory("Material Management", "D", false);
		int GL_API = createGLCategory("AP Invoice", "D", false);
		int GL_APP = createGLCategory("AP Payment", "D", false);

		//	Base DocumentTypes
		createDocType("GL Journal", "Journal", "GLJ", null, 0, 0, 1000, GL_GL);
		int DT_I = createDocType("AR Invoice", "Invoice", "ARI", null, 0, 0, 100000, GL_ARI);
		int DT_II = createDocType("AR Invoice Indirect", "Invoice", "ARI", null, 0, 0, 200000, GL_ARI);
		createDocType("AR Credit Memo", "Credit Memo", "ARC", null, 0, 0, 300000, GL_ARI);
		createDocType("AR Receipt", "Receipt", "ARR", null, 0, 0, 400000, GL_ARR);
		int DT_S  = createDocType("MM Shipment", "Delivery Note", "MMS", null, 0, 0, 500000, GL_MM);
		int DT_SI = createDocType("MM Shipment Indirect", "Delivery Note", "MMS", null, 0, 0, 600000, GL_MM);
		createDocType("MM Receipt", "Vendor Delivery", "MMR", null, 0, 0, 0, GL_MM);
		createDocType("AP Invoice", "Vendor Invoice", "API", null, 0, 0, 0, GL_API);
		createDocType("AP CreditMemo", "Vendor Credit Memo", "APC", null, 0, 0, 0, GL_API);
		createDocType("AP Payment", "Vendor Payment", "APP", null, 0, 0, 700000, GL_APP);
		createDocType("Purchase Order", "Purchase Order", "POO", null, 0, 0, 800000, GL_None);
		createDocType("Purchase Requisition", "Purchase Requisition", "POR", null, 0, 0, 900000, GL_None);

		//  Order Entry
		createDocType("Quotation", "Binding offer", "SOO", "OB", 0, 0, 10000, GL_None);
		createDocType("Proposal", "Non binding offer", "SOO", "ON", 0, 0, 20000, GL_None);
		createDocType("Prepay Order", "Prepay Order", "SOO", "PR", DT_S, DT_I, 30000, GL_None);
		createDocType("Return Material", "Return Material Authorization", "SOO", "RM", DT_S, DT_I, 40000, GL_None);
		createDocType("Standard Order", "Order Confirmation", "SOO", "SO", DT_S, DT_I, 50000, GL_None);
		createDocType("Credit Order", "Order Confirmation", "SOO", "WI", DT_SI, DT_I, 60000, GL_None);   //  RE
		createDocType("Warehouse Order", "Order Confirmation", "SOO", "WP", DT_S, DT_I, 70000, GL_None);    //  LS
		int DT = createDocType("POS Order", "Order Confirmation", "SOO", "WR", DT_SI, DT_II, 80000, GL_None);    // Bar
		createPreference("C_DocTypeTarget_ID", String.valueOf(DT), 143);

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_AcctSchema1_ID=").append(C_AcctSchema_ID)
			.append(", C_Calendar_ID=").append(C_Calendar_ID)
			.append(" WHERE AD_Client_ID=").append(AD_Client_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//
		return true;
	}   //  createAccounting

	/**
	 *  Get Account ID for key
	 *  @param key key
	 *  @return C_ValidCombination_ID
	 */
	private int getAcct (String key)
	{
		Log.trace(Log.l4_Data, "MSetup.getAcct - " + key);
		//  Element
		int C_ElementValue_ID = m_nap.getC_ElementValue_ID(key);
		Account vc = Account.getDefault(m_AcctSchema, true);	//	optional null
		vc.setAccount_ID(C_ElementValue_ID);
		vc.save(AD_Client_ID, 0);
		int C_ValidCombination_ID = vc.getC_ValidCombination_ID();

		//  Error
		if (C_ValidCombination_ID == 0)
			Log.error("MSetup.getAcct - no account for " + key);
		return C_ValidCombination_ID;
	}   //  getAcct

	/**
	 *  Create GL Category
	 *  @param Name name
	 *  @param CategoryType category type
	 *  @param isDefault is default value
	 *  @return GL_Category_ID
	 */
	private int createGLCategory (String Name, String CategoryType, boolean isDefault)
	{
		int GL_Category_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "GL_Category");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO GL_Category ");
		sqlCmd.append("(GL_Category_ID,").append(m_stdColumns).append(",")
			.append("Name,CategoryType,IsDefault) VALUES (")
			.append(GL_Category_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(Name).append("','").append(CategoryType).append("','")
			.append(isDefault ? "Y" : "N").append("')");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createGLCategory - GL Category NOT created - " + Name);
		//
		return GL_Category_ID;
	}   //  createGLCategory

	/**
	 *  Create Document Types with Sequence
	 *  @param Name name
	 *  @param PrintName print name
	 *  @param DocBaseType document base type
	 *  @param DocSubTypeSO sales order sub type
	 *  @param C_DocTypeShipment_ID shipment doc
	 *  @param C_DocTypeInvoice_ID invoice doc
	 *  @param StartNo start doc no
	 *  @param GL_Category_ID gl category
	 *  @return C_DocType_ID doc type
	 */
	private int createDocType (String Name, String PrintName,
		String DocBaseType, String DocSubTypeSO,
		int C_DocTypeShipment_ID, int C_DocTypeInvoice_ID,
		int StartNo, int GL_Category_ID)
	{
		StringBuffer sqlCmd = null;
		//  Get Sequence
		int AD_Sequence_ID = 0;
		if (StartNo != 0)   //  manual sequenec, if startNo == 0
		{
			AD_Sequence_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Sequence");
			sqlCmd = new StringBuffer ("INSERT INTO AD_Sequence ");
			sqlCmd.append("(AD_Sequence_ID,").append(m_stdColumns).append(",");
			sqlCmd.append("Name,IsAutoSequence,IncrementNo,StartNo,CurrentNext,");
			sqlCmd.append("CurrentNextSys,IsTableID) VALUES (");
			sqlCmd.append(AD_Sequence_ID).append(",").append(m_stdValues).append(",");
			sqlCmd.append("'").append(Name).append("','Y',1,").append(StartNo).append(",").append(StartNo).append(",");
			sqlCmd.append(StartNo/10).append(",'N')");
			int no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createDocType - Sequence NOT created - " + Name);
		}

		//  Get Document Type
		int C_DocType_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_DocType");
		sqlCmd = new StringBuffer ("INSERT INTO	C_DocType ");
		sqlCmd.append("(C_DocType_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,PrintName,DocBaseType,DocSubTypeSO,");
		sqlCmd.append("C_DocTypeShipment_ID,C_DocTypeInvoice_ID,");
		sqlCmd.append("IsDocNoControlled,DocNoSequence_ID,GL_Category_ID,");
		sqlCmd.append("IsPrinted,IsTransferred,DocumentCopies,IsSOTrx)");
		//
		sqlCmd.append(" VALUES (").append(C_DocType_ID).append(",").append(m_stdValues).append(",");
		//  Name,PrintName,DocBaseType,DocSubTypeSO,
		sqlCmd.append("'").append(Name).append("','").append(PrintName).append("','").append(DocBaseType).append("',");
		if (DocSubTypeSO == null || DocSubTypeSO.length() == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append("'").append(DocSubTypeSO).append("',");
		//  C_DocTypeShipment_ID,C_DocTypeInvoice_ID,
		if (C_DocTypeShipment_ID == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append(C_DocTypeShipment_ID).append(",");
		if (C_DocTypeInvoice_ID == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append(C_DocTypeInvoice_ID).append(",");
		//  IsDocNoControlled,DocNoSequence_ID,GL_Category_ID,
		if (AD_Sequence_ID == 0)
			sqlCmd.append("'N',NULL,");
		else
			sqlCmd.append("'Y',").append(AD_Sequence_ID).append(",");
		sqlCmd.append(GL_Category_ID).append(",");
		//  IsPrinted,IsTransferred,DocumentCopies,IsSOTrx)
		sqlCmd.append("'Y',");
		if (DocBaseType.equals("SOO"))
			sqlCmd.append("'N',");
		else
			sqlCmd.append("'Y',");
		sqlCmd.append("0,");
		if (DocBaseType.startsWith("AR") || DocBaseType.equals("MMS") || DocBaseType.equals("SOO"))
			sqlCmd.append("'Y'");
		else
			sqlCmd.append("'N'");
		sqlCmd.append(")");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createDocType - DocType NOT created - " + Name);
		//
		return C_DocType_ID;
	}   //  createDocType

	/*************************************************************************/

	/**
	 *  Create Default main entities.
	 *  - Dimensions & BPGroup, Prod Category)
	 *  - Location, Locator, Warehouse
	 *  - PriceList
	 *  - Cashbook, PaymentTerm
	 *  @param C_Country_ID country
	 *  @param City city
	 *  @param C_Region_ID region
	 *  @return true if created
	 */
	public boolean createEntities (int C_Country_ID, String City, int C_Region_ID)
	{
		Log.trace(Log.l3_Util, "MSetup.createEntries", "C_Country_ID=" + C_Country_ID + ", City=" + City + ", C_Region_ID=" + C_Region_ID);
		m_info.append("\n----\n");
		//
		String defaultName = Msg.translate(m_lang, "Standard");
		String defaultEntry = "'" + defaultName + "',";
		StringBuffer sqlCmd = null;
		int no = 0;

		//	Create Marketing Channel/Campaign
		int C_Channel_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Channel");
		sqlCmd = new StringBuffer("INSERT INTO C_Channel ");
		sqlCmd.append("(C_Channel_ID,Name,");
		sqlCmd.append(m_stdColumns).append(") VALUES (");
		sqlCmd.append(C_Channel_ID).append(",").append(defaultEntry);
		sqlCmd.append(m_stdValues).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Channel NOT inserted");
		int C_Campaign_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Campaign");
		sqlCmd = new StringBuffer("INSERT INTO C_Campaign ");
		sqlCmd.append("(C_Campaign_ID,C_Channel_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,Costs) VALUES (");
		sqlCmd.append(C_Campaign_ID).append(",").append(C_Channel_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Campaign_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Campaign NOT inserted");
		if (m_hasMCampaign)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Campaign_ID=").append(C_Campaign_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='MC'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Campaign NOT updated");
		}

		//	Create Sales Region
		int C_SalesRegion_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_SalesRegion");
		sqlCmd = new StringBuffer ("INSERT INTO C_SalesRegion ");
		sqlCmd.append("(C_SalesRegion_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsSummary) VALUES (");
		sqlCmd.append(C_SalesRegion_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_SalesRegion_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRegion NOT inserted");
		if (m_hasSRegion)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_SalesRegion_ID=").append(C_SalesRegion_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='SR'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement SalesRegion NOT updated");
		}

		/**
		 *  Business Partner
		 */
		//  Create BP Group
		int C_BP_Group_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BP_Group");
		sqlCmd = new StringBuffer ("INSERT INTO C_BP_Group ");
		sqlCmd.append("(C_BP_Group_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault) VALUES ( ");
		sqlCmd.append(C_BP_Group_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BP_Group_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BP Group NOT inserted");

		//	Create BPartner
		int C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_BP_Group_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BPartner_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BPartner NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("C_BPartner_ID=").append(C_BPartner_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
		sqlCmd.append(" AND ElementType='BP'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element BPartner NOT updated");
		createPreference("C_BPartner_ID", String.valueOf(C_BPartner_ID), 143);

		/**
		 *  Product
		 */
		//  Create Product Category
		int M_Product_Category_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Product_Category");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product_Category ");
		sqlCmd.append("(M_Product_Category_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault,PlannedMargin) VALUES (");
		sqlCmd.append(M_Product_Category_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y',0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_Category_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product Category NOT inserted");

		//  UOM (EA)
		int C_UOM_ID = 100;

		//  TaxCategory
		int C_TaxCategory_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_TaxCategory");
		sqlCmd = new StringBuffer ("INSERT INTO C_TaxCategory ");
		sqlCmd.append("(C_TaxCategory_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,IsDefault) VALUES (");
		sqlCmd.append(C_TaxCategory_ID).append(",").append(m_stdValues).append(", ");
		if (C_Country_ID == 100)    // US
			sqlCmd.append("'Sales Tax','Y')");
		else
			sqlCmd.append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - TaxCategory NOT inserted");

		//  Tax - Zero Rate
		int C_Tax_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Tax");
		sqlCmd = new StringBuffer ("INSERT INTO C_Tax ");
		sqlCmd.append("(C_Tax_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,Rate,C_TaxCategory_ID,C_Country_ID,TO_Country_ID,ValidFrom,IsDefault) VALUES (");
		sqlCmd.append(C_Tax_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append("0,").append(C_TaxCategory_ID).append(",")
			.append(C_Country_ID).append(",").append(C_Country_ID)
			.append(",TO_DATE('1990-01-01','YYYY-MM-DD'),'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Tax_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Tax NOT inserted");

		//	Create Product
		int M_Product_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Product");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product ");
		sqlCmd.append("(M_Product_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_UOM_ID,M_Product_Category_ID,C_TaxCategory_ID) VALUES (");
		sqlCmd.append(M_Product_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry);
		sqlCmd.append(C_UOM_ID).append(",").append(M_Product_Category_ID).append(",");
		sqlCmd.append(C_TaxCategory_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("M_Product_ID=").append(M_Product_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
		sqlCmd.append(" AND ElementType='PR'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element Product NOT updated");

		/**
		 *  Warehouse
		 */
		//  Location (Company)
		int C_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		sqlCmd = new StringBuffer ("UPDATE AD_OrgInfo SET C_Location_ID=");
		sqlCmd.append(C_Location_ID).append(" WHERE AD_Org_ID=").append(AD_Org_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Location NOT inserted");
		createPreference("C_Country_ID", String.valueOf(C_Country_ID), 0);

		//  Default Warehouse
		int M_Warehouse_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Warehouse");
		sqlCmd = new StringBuffer ("INSERT INTO M_Warehouse ");
		sqlCmd.append("(M_Warehouse_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Location_ID,Separator) VALUES (");
		sqlCmd.append(M_Warehouse_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Location_ID).append(",'-')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Warehouse NOT inserted");

		//   Locator
		int M_Locator_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Locator");
		sqlCmd = new StringBuffer ("INSERT INTO M_Locator ");
		sqlCmd.append("(M_Locator_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,M_Warehouse_ID,X,Y,Z,PriorityNo,IsDefault) VALUES (");
		sqlCmd.append(M_Locator_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(M_Warehouse_ID).append(",0,0,0,50,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Locator NOT inserted");

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_BPartnerCashTrx_ID=").append(C_BPartner_ID);
		sqlCmd.append(",M_ProductFreight_ID=").append(M_Product_ID);
//		sqlCmd.append("C_UOM_Volume_ID=");
//		sqlCmd.append(",C_UOM_Weight_ID=");
//		sqlCmd.append(",C_UOM_Length_ID=");
//		sqlCmd.append(",C_UOM_Time_ID=");
		sqlCmd.append(" WHERE AD_Client_ID=").append(AD_Client_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createEntities - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		/**
		 *  Other
		 */
		//  PriceList
		int M_PriceList_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_PriceList");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList ");
		sqlCmd.append("(M_PriceList_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID,IsDefault) VALUES (");
		sqlCmd.append(M_PriceList_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(",'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList NOT inserted");
		//  DiscountSchema
		int M_DiscountSchema_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_DiscountSchema");
		sqlCmd = new StringBuffer ("INSERT INTO M_DiscountSchema ");
		sqlCmd.append("(M_DiscountSchema_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,DiscountType, FlatDiscount) VALUES (");
		sqlCmd.append(M_DiscountSchema_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append("SysDate,'P',0)");	//	PriceList
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - DiscountSchema NOT inserted");
		//  PriceList Version
		int M_PriceList_Version_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_PriceList_Version");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList_Version ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,M_PriceList_ID,M_DiscountSchema_ID) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY-MM-DD'),SysDate,").append(M_PriceList_ID)
			.append(",").append(M_DiscountSchema_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList_Version NOT inserted");
		//  ProductPrice
		sqlCmd = new StringBuffer ("INSERT INTO M_ProductPrice ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("M_Product_ID,PriceList,PriceStd,PriceLimit) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(M_Product_ID).append(",1,1,1)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - ProductPrice NOT inserted");

		//  Location for Standard BP
		C_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		int C_BPartner_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(City).append("',").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (Standard) NOT inserted");

		//	Create Sales Rep for Client-User
		C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_U_Name).append("','").append(AD_User_U_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_U_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (User) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_U_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (User) NOT updated");

		//  Location for Client-User
		C_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		C_BPartner_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(City).append("',").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (User) NOT inserted");


		//	Create Sales Rep for Client-Admin
		C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_Name).append("','").append(AD_User_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (Admin) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (Admin) NOT updated");

		//  Location for Client-Admin
		C_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		C_BPartner_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(City).append("',").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (Admin) NOT inserted");


		//  Payment Term
		int C_PaymentTerm_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_PaymentTerm");
		sqlCmd = new StringBuffer ("INSERT INTO C_PaymentTerm ");
		sqlCmd.append("(C_PaymentTerm_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,NetDays,GraceDays,DiscountDays,Discount,DiscountDays2,Discount2,IsDefault) VALUES (");
		sqlCmd.append(C_PaymentTerm_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'Immediate','Immediate',0,0,0,0,0,0,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PaymentTerm NOT inserted");

		//  Project Cycle
		C_Cycle_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Cycle");
		sqlCmd = new StringBuffer ("INSERT INTO C_Cycle ");
		sqlCmd.append("(C_Cycle_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_Cycle_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Cycle NOT inserted");

		/**
		 *  Organization level data	===========================================
		 */

		//	Create Default Project
		int C_Project_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Project");
		sqlCmd = new StringBuffer ("INSERT INTO C_Project ");
		sqlCmd.append("(C_Project_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Currency_ID,IsSummary) VALUES (");
		sqlCmd.append(C_Project_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Currency_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Project_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Project NOT inserted");
		//  Default Project
		if (m_hasProject)
		{
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Project_ID=").append(C_Project_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='PJ'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Project NOT updated");
		}

		//  CashBook
		int C_CashBook_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_CashBook");
		sqlCmd = new StringBuffer ("INSERT INTO C_CashBook ");
		sqlCmd.append("(C_CashBook_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_CashBook_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_CashBook_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - CashBook NOT inserted");

		return true;
	}   //  createEntities

	/**
	 *  Create Preference
	 *  @param Attribute attribute
	 *  @param Value value
	 *  @param AD_Window_ID window
	 */
	private void createPreference (String Attribute, String Value, int AD_Window_ID)
	{
		int AD_Preference_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Preference");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO AD_Preference ");
		sqlCmd.append("(AD_Preference_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Attribute,Value,AD_Window_ID) VALUES (");
		sqlCmd.append(AD_Preference_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(Attribute).append("','").append(Value).append("',");
		if (AD_Window_ID == 0)
			sqlCmd.append("NULL)");
		else
			sqlCmd.append(AD_Window_ID).append(")");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createPreference - Preference NOT inserted - " + Attribute);
	}   //  createPreference

	/*************************************************************************/

	/**
	 *  Get Client
	 *  @return AD_Client_ID
	 */
	public int getAD_Client_ID()
	{
		return AD_Client_ID;
	}
	public int getAD_Org_ID()
	{
		return AD_Org_ID;
	}
	public int getAD_User_ID()
	{
		return AD_User_ID;
	}
	public int getAD_Role_ID()
	{
		return AD_Role_ID;
	}
	public int getC_AcctSchema_ID()
	{
		return C_AcctSchema_ID;
	}
	public String getInfo()
	{
		return m_info.toString();
	}
}   //  MSetup
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
package org.compiere.model;

import java.util.*;
import java.sql.*;
import java.io.*;

import org.compiere.util.*;

/**
 * Initial Setup Model
 *
 * @author Jorg Janke
 * @version $Id: MSetup.java,v 1.18 2003/01/20 05:39:20 jjanke Exp $
 */
public final class MSetup
{
	/**
	 *  Constructor
	 *  @param ctx context
	 *  @param WindowNo window
	 */
	public MSetup(Properties ctx, int WindowNo)
	{
		m_ctx = ctx;
		m_lang = Env.getAD_Language(m_ctx);
		m_WindowNo = WindowNo;
	}   //  MSetup

	private Properties      m_ctx;
	private String          m_lang;
	private int             m_WindowNo;
	private StringBuffer    m_info;
	//
	private String          m_clientName;
	private String          m_orgName;
	//
	private String          m_stdColumns = "AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy";
	private String          m_stdValues;
	private String          m_stdValuesOrg;
	//
	private NaturalAccountMap m_nap = null;
	//
	private final String    CompiereSys = "N";           //  Should NOT be changed
	private int             AD_Client_ID = 1000000;     //  initial Client_ID
	//
	private int     AD_Org_ID;
	private int     AD_User_ID;
	private String  AD_User_Name;
	private int     AD_User_U_ID;
	private String  AD_User_U_Name;
	private int     AD_Role_ID;
	private int     C_Calendar_ID;
	private int     C_AcctSchema_ID;
	private int     C_Currency_ID;
	private int     AD_Tree_Account_ID;
	private int     C_Cycle_ID;
	//
	private boolean         m_hasProject = false;
	private boolean         m_hasMCampaign = false;
	private boolean         m_hasSRegion = false;
	private AcctSchema      m_AcctSchema = null;


	/**
	 *  Create Client Info.
	 *  - Client, Trees, Org, Role, User, User_Role
	 *  @param clientName client name
	 *  @param orgName org name
	 *  @param userClient user id client
	 *  @param userOrg user id org
	 *  @return true if created
	 */
	public boolean createClient (String clientName, String orgName,
		String userClient, String userOrg)
	{
		Log.trace(Log.l3_Util, "MSetup.createClient");
		//  info header
		m_info = new StringBuffer();
		//  Standarc columns
		String name = null;
		String sql = null;
		int no = 0;

		/**
		 *  Create Client
		 */
		Env.setContext(m_ctx, "#CompiereSys", CompiereSys);
		AD_Client_ID = DB.getKeyNextNo (AD_Client_ID, CompiereSys, "AD_Client");
		Env.setContext(m_ctx, m_WindowNo, "AD_Client_ID", AD_Client_ID);

		//	Standard Values
		m_stdValues = String.valueOf(AD_Client_ID) + ",0,'Y',SysDate,0,SysDate,0";

		//	Create Client
		name = clientName;
		if (name == null || name.length() == 0)
			name = "newClient";
		name = name.trim();
		m_clientName = name;
		sql = "INSERT INTO AD_Client(" + m_stdColumns + ",Value,Name,Description)"
			+ " VALUES (" + m_stdValues + ",'" + name + "','" + name + "','" + name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Client NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info - Client
		m_info.append(Msg.translate(m_lang, "AD_Client_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Trees
		 */
		String sql2 = null;
		if (Env.isBaseLanguage(m_lang, "AD_Ref_List"))	//	Get TreeTypes & Name
			sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=120";
		else
			sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Reference_ID=120 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID";

		//  Tree IDs
		int AD_Tree_Org_ID=0, AD_Tree_BPartner_ID=0, AD_Tree_Project_ID=0,
			AD_Tree_SalesRegion_ID=0, AD_Tree_Product_ID=0;

		try
		{
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next())
			{
				String value = rs.getString(1);
				int AD_Tree_ID = 0;
				if (value.equals("OO"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Org_ID = AD_Tree_ID;
				}
				else if (value.equals("BP"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_BPartner_ID = AD_Tree_ID;
				}
				else if (value.equals("PJ"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Project_ID = AD_Tree_ID;
				}
				else if (value.equals("SR"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_SalesRegion_ID = AD_Tree_ID;
				}
				else if (value.equals("PR"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Product_ID = AD_Tree_ID;
				}
				else if (value.endsWith("EV"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Account_ID = AD_Tree_ID;
				}
				else if (value.equals("MM"))	//	No Menu
					;
				else
					//	PC (Product Category), BB (BOM), MC (Marketing Campaign), AY (Activity)
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");

				//
				if (AD_Tree_ID != 0)
				{
					name = m_clientName + " " + rs.getString(2);
					sql = "INSERT INTO AD_Tree(" + m_stdColumns + ",AD_Tree_ID,Name,Description,TreeType)"
						+ " VALUES (" + m_stdValues + "," + AD_Tree_ID + ",'" + name + "','" + name + "','" + value + "')";
					no = DB.executeUpdate(sql);
					if (no == 1)
						m_info.append(Msg.translate(m_lang, "AD_Tree_ID")).append("=").append(name).append("\n");
					else
						Log.error("MSetup.createClient - Tree NOT created: " + name);
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e1)
		{
			Log.error ("MSetup.createClient - Trees", e1);
		}

		//	Get Primary Tree
		int AD_Tree_Menu_ID = 10;	//	hardcoded

		//	Create ClientInfo
		sql = "INSERT INTO AD_ClientInfo(" + m_stdColumns + ",Acct2_Active,Acct3_Active,"
			+ "AD_Tree_Menu_ID,AD_Tree_Org_ID,AD_Tree_BPartner_ID,AD_Tree_Project_ID,AD_Tree_SalesRegion_ID,AD_Tree_Product_ID)"
			+ " VALUES (" + m_stdValues + ",'N','N',"
			+ AD_Tree_Menu_ID + "," + AD_Tree_Org_ID + "," + AD_Tree_BPartner_ID + "," + AD_Tree_Project_ID + "," + AD_Tree_SalesRegion_ID + "," + AD_Tree_Product_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - ClientInfo NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
	//	m_info.append(Msg.translate(m_lang, "AD_ClientInfo")).append("\n");

		/**
		 *  Create Org
		 */
		AD_Org_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Org");
		m_stdValuesOrg = AD_Client_ID + "," + AD_Org_ID + ",'Y',SysDate,0,SysDate,0";

		name = orgName;
		if (name == null || name.length() == 0)
			name = "newOrg";
		m_orgName = name;
		sql = "INSERT INTO AD_Org (" + m_stdColumns + ",Value,Name,IsSummary)"
			+ " VALUES (" +	m_stdValuesOrg + ",'" + name + "','" + name + "','N')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Org NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_Org_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Roles
		 *  - Admin
		 *  - User
		 */
		name = m_clientName + " Admin";
		AD_Role_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Role");
		sql = "INSERT INTO AD_Role(" + m_stdColumns + ",AD_Role_ID,"
			+ "Name,Description,UserLevel,ClientList,OrgList)"
			+ " VALUES (" + m_stdValues + "," + AD_Role_ID + ","
			+ "'" + name + "','" + name + "',' CO','" + AD_Client_ID + "','0," + AD_Org_ID + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin Role A NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  ClientAccess x,0
		sql = "INSERT INTO AD_Role_ClientAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValues + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_ClientAccess NOT created");
		//  OrgAccess x,0
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValues + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_OrgAccess 0 NOT created");
		//  OrgAccess x,y
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValuesOrg + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_OrgAccess NOT created");
		//  Info - Admin Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		//
		name = m_clientName + " User";
		int AD_Role_ID_U = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Role");
		sql = "INSERT INTO AD_Role(" + m_stdColumns + ",AD_Role_ID,"
			+ "Name,Description,UserLevel,ClientList,OrgList)"
			+ " VALUES (" + m_stdValues + "," + AD_Role_ID_U + ","
			+ "'" + name + "','" + name + "','  O','" + AD_Client_ID + "','" + AD_Org_ID + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - User Role A NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  ClientAccess x,0
		sql = "INSERT INTO AD_Role_ClientAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValues + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - User Role_ClientAccess NOT created");
		//  OrgAccess x,y
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValuesOrg + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - User Role_OrgAccess NOT created");
		//  Info - Client Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Users
		 *  - Client
		 *  - Org
		 */
		name = userClient;
		if (name == null || name.length() == 0)
			name = m_clientName + "Client";
		AD_User_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_User");
		AD_User_Name = name;
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + ","
			+ "'" + name + "','" + name + "','" + name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin User A NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(name).append("/").append(name).append("\n");

		name = userOrg;
		if (name == null || name.length() == 0)
			name = m_clientName + "Org";
		AD_User_U_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_User");
		AD_User_U_Name = name;
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + ","
			+ "'" + name + "','" + name + "','" + name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Org User A NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(name).append("/").append(name).append("\n");

		/**
		 *  Create User-Role
		 */
		//  ClientUser          - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+Admin NOT iserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+User NOT iserted");
		//  OrgUser             - User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole OrgUser+Org NOT iserted");
		//  SuperUser(100)      - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + ",100," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole SuperUser+Admin NOT iserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + ",100," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole SuperUser+User NOT iserted");

		return true;
	}   //  createClient



	/*************************************************************************/

	/**
	 *  Create Accounting elements.
	 *  - Calendar
	 *  - Account Trees
	 *  - Account Values
	 *  - Accounting Schema
	 *  - Default Accounts
	 *
	 *  @param newC_Currency_ID currency
	 *  @param curName currency name
	 *  @param hasProduct has product segment
	 *  @param hasBPartner has bp segment
	 *  @param hasProject has project segment
	 *  @param hasMCampaign has campaign segment
	 *  @param hasSRegion has sales region segment
	 *  @param AccountingFile file name of accounting file
	 *  @return true if created
	 */
	public boolean createAccounting(int newC_Currency_ID, String curName,
		boolean hasProduct, boolean hasBPartner, boolean hasProject,
		boolean hasMCampaign, boolean hasSRegion,
		File AccountingFile)
	{
		Log.trace(Log.l3_Util, "MSetup.createAccount");
		//
		C_Currency_ID = newC_Currency_ID;
		m_hasProject = hasProject;
		m_hasMCampaign = hasMCampaign;
		m_hasSRegion = hasSRegion;

		//  Standard variables
		m_info = new StringBuffer();
		String name = null;
		StringBuffer sqlCmd = null;
		int no = 0;

		/**
		 *  Create Calendar
		 */
		C_Calendar_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Calendar");
		name = m_clientName + " " + Msg.translate(m_lang, "C_Calendar_ID");
		sqlCmd = new StringBuffer("INSERT INTO C_Calendar(");
		sqlCmd.append(m_stdColumns).append(",C_Calendar_ID,Name)").append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_Calendar_ID).append(",'").append(name).append("')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Calendar NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_Calendar_ID")).append("=").append(name).append("\n");

		//  Year
		int C_Year_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Year");
		sqlCmd = new StringBuffer ("INSERT INTO C_Year ");
		sqlCmd.append("(C_Year_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Year,C_Calendar_ID) VALUES (");
		sqlCmd.append(C_Year_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY'),").append(C_Calendar_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createAccounting - Year NOT inserted");
		/** @todo Create Periods */


		//	Create Account Elements
		int C_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Element");
		name = m_clientName + " " + Msg.translate(m_lang, "Account_ID");
		sqlCmd = new StringBuffer ("INSERT INTO C_ELEMENT(");
		sqlCmd.append(m_stdColumns).append(",C_Element_ID,Name,Description,")
			.append("VFormat,ElementType,IsBalancing,IsNaturalAccount,AD_Tree_ID)").append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_Element_ID).append(",'").append(name).append("','").append(name).append("',")
			.append("NULL,'A','N','Y',").append(AD_Tree_Account_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Acct Element NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		m_info.append(Msg.translate(m_lang, "C_Element_ID")).append("=").append(name).append("\n");

		//	Create Account Values
		m_nap = new NaturalAccountMap();
		String errMsg = m_nap.parseFile(AccountingFile);
		if (errMsg.length() != 0)
		{
			Log.error(errMsg);
			m_info.append(errMsg);
			return false;
		}
		if (m_nap.createAccounts(AD_Client_ID, AD_Org_ID, C_Element_ID))
			m_info.append(Msg.translate(m_lang, "C_ElementValue_ID")).append(" # ").append(m_nap.size()).append("\n");
		else
		{
			String err = "MSetup.createAccounting - Acct Element Values NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		int C_ElementValue_ID = m_nap.getC_ElementValue_ID("DEFAULT_ACCT");
		Log.trace(Log.l4_Data, "C_ElementValue_ID=" + C_ElementValue_ID);

		/**
		 *  Create AccountingSchema
		 */
		C_AcctSchema_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema");
		//
		String GAAP = "US";				//	AD_Reference_ID=123
		String CostingMethod = "A";		//	AD_Reference_ID=122
		name = m_clientName + " " + GAAP + "/" + CostingMethod + "/" + curName;
		//
		sqlCmd = new StringBuffer ("INSERT INTO C_AcctSchema(");
		sqlCmd.append(m_stdColumns).append(",C_AcctSchema_ID,Name,")
			.append("GAAP,IsAccrual,CostingMethod,C_Currency_ID,")
			.append("AutoPeriodControl,Separator,HasAlias,HasCombination)")
			.append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",'").append(name).append("',")
			.append("'").append(GAAP).append("','Y','").append(CostingMethod).append("',").append(C_Currency_ID).append(",")
			.append("'N','-','Y','N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - AcctSchema NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_AcctSchema_ID")).append("=").append(name).append("\n");

		/**
		 *  Create AccountingSchema Elements (Structure)
		 */
		String sql2 = null;
		if (Env.isBaseLanguage(m_lang, "AD_Reference"))	//	Get TreeTypes & Name
			sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=181";
		else
			sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Reference_ID=181 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID";
		//
		int Element_OO=0, Element_AC=0, Element_PR=0, Element_BP=0, Element_PJ=0,
			Element_MC=0, Element_SR=0;
		try
		{
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next())
			{
				String ElementType = rs.getString(1);
				name = rs.getString(2);
				//
				String IsMandatory = null;
				String IsBalanced = "N";
				int SeqNo = 0;
				int C_AcctSchema_Element_ID = 0;

				if (ElementType.equals("OO"))
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_OO = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					IsBalanced = "Y";
					SeqNo = 10;
				}
				else if (ElementType.equals("AC"))
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_AC = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					SeqNo = 20;
				}
				else if (ElementType.equals("PR") && hasProduct)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_PR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 30;
				}
				else if (ElementType.equals("BP") && hasBPartner)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_BP = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 40;
				}
				else if (ElementType.equals("PJ") && hasProject)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_PJ = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 50;
				}
				else if (ElementType.equals("MC") && hasMCampaign)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_MC = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 60;
				}
				else if (ElementType.equals("SR") && hasSRegion)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_SR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 70;
				}
				//	Not OT, LF, LT, U1, U2, AY

				if (IsMandatory != null)
				{
					sqlCmd = new StringBuffer ("INSERT INTO C_AcctSchema_Element(");
					sqlCmd.append(m_stdColumns).append(",C_AcctSchema_Element_ID,C_AcctSchema_ID,")
						.append("ElementType,Name,SeqNo,IsMandatory,IsBalanced) VALUES (");
					sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_Element_ID).append(",").append(C_AcctSchema_ID).append(",")
						.append("'").append(ElementType).append("','").append(name).append("',").append(SeqNo).append(",'")
						.append(IsMandatory).append("','").append(IsBalanced).append("')");
					no = DB.executeUpdate(sqlCmd.toString());
					if (no == 1)
						m_info.append(Msg.translate(m_lang, "C_AcctSchema_Element_ID")).append("=").append(name).append("\n");

					/** Default value for mandatory elements: OO and AC */
					if (ElementType.equals("OO"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET Org_ID=");
						sqlCmd.append(AD_Org_ID).append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Org in AcctSchamaElement NOT updated");
					}
					if (ElementType.equals("AC"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET C_ElementValue_ID=");
						sqlCmd.append(C_ElementValue_ID).append(", C_Element_ID=").append(C_Element_ID);
						sqlCmd.append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Account in AcctSchamaElement NOT updated");
					}
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e1)
		{
			Log.error ("MSetup.createAccounting - Elements", e1);
		}
		//  Create AcctSchema
		m_AcctSchema = new AcctSchema (C_AcctSchema_ID);


		//  Create GL Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_GL (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "USESUSPENSEBALANCING,SUSPENSEBALANCING_ACCT,"
			+ "USESUSPENSEERROR,SUSPENSEERROR_ACCT,"
			+ "USECURRENCYBALANCING,CURRENCYBALANCING_ACCT,"
			+ "RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,"
			+ "INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT,"
			+ "PPVOFFSET_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEBALANCING_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEERROR_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("CURRENCYBALANCING_ACCT")).append(",");
		//  RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,
		sqlCmd.append(getAcct("RETAINEDEARNING_ACCT")).append(",");
		sqlCmd.append(getAcct("INCOMESUMMARY_ACCT")).append(",");
		//  INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT)
		sqlCmd.append(getAcct("INTERCOMPANYDUETO_ACCT")).append(",");
		sqlCmd.append(getAcct("INTERCOMPANYDUEFROM_ACCT")).append(",");
		sqlCmd.append(getAcct("PPVOFFSET_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - GL Accounts NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//	Create Std Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_DEFAULT (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT, "
			+ "P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT, "
			+ "P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT, "
			+ "C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT, "
			+ "V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT, "
			+ "PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT, "
			+ "UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT, "
			+ "WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT, "
			+ "PJ_ASSET_ACCT,PJ_WIP_ACCT,"
			+ "T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT, "
			+ "B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,"
			+ "B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,"
			+ "B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT, "
			+ "CH_EXPENSE_ACCT,CH_REVENUE_ACCT, "
			+ "UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT, "
			+ "CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",");
		//  W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT
		sqlCmd.append(getAcct("W_INVENTORY_ACCT")).append(",");
		sqlCmd.append(getAcct("W_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("W_REVALUATION_ACCT")).append(",");
		sqlCmd.append(getAcct("W_INVACTUALADJUST_ACCT")).append(", ");
		//  P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT,
		sqlCmd.append(getAcct("P_REVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("P_COGS_ACCT")).append(", ");
		//  P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT,
		sqlCmd.append(getAcct("P_PURCHASEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_INVOICEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTREC_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTGRANT_ACCT")).append(", ");
		//  C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("C_RECEIVABLE_ACCT")).append(",");
		sqlCmd.append(getAcct("C_PREPAYMENT_ACCT")).append(", ");
		//  V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("V_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("V_LIABILITY_SERVICES_ACCT")).append(",");
		sqlCmd.append(getAcct("V_PREPAYMENT_ACCT")).append(", ");
		//  PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT,
		sqlCmd.append(getAcct("PAYDISCOUNT_EXP_ACCT")).append(",");
		sqlCmd.append(getAcct("PAYDISCOUNT_REV_ACCT")).append(",");
		sqlCmd.append(getAcct("WRITEOFF_ACCT")).append(", ");
		//  UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT,
		sqlCmd.append(getAcct("UNREALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("UNREALIZEDLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDLOSS_ACCT")).append(", ");
		//  WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT,
		sqlCmd.append(getAcct("WITHHOLDING_ACCT")).append(",");
		sqlCmd.append(getAcct("E_PREPAYMENT_ACCT")).append(",");
		sqlCmd.append(getAcct("E_EXPENSE_ACCT")).append(", ");
		//  PJ_ASSET_ACCT,PJ_WIP_ACCT,
		sqlCmd.append(getAcct("PJ_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("PJ_WIP_ACCT")).append(",");
		//  T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT,
		sqlCmd.append(getAcct("T_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("T_RECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("T_DUE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_CREDIT_ACCT")).append(", ");
		//  B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,
		sqlCmd.append(getAcct("B_INTRANSIT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("B_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTREV_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTEXP_ACCT")).append(",");
		//  B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,
		sqlCmd.append(getAcct("B_UNIDENTIFIED_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTLOSS_ACCT")).append(",");
		//  B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT,
		sqlCmd.append(getAcct("B_REVALUATIONGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_REVALUATIONLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("B_PAYMENTSELECT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_UNALLOCATEDCASH_ACCT")).append(", ");
		//  CH_EXPENSE_ACCT,CH_REVENUE_ACCT,
		sqlCmd.append(getAcct("CH_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CH_REVENUE_ACCT")).append(", ");
		//  UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT,
		sqlCmd.append(getAcct("UNEARNEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIPTS_ACCT")).append(", ");
		//  CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT)
		sqlCmd.append(getAcct("CB_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_CASHTRANSFER_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_RECEIPT_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Default Accounts NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//  GL Categories
		createGLCategory("Standard", "M", true);
		int GL_None = createGLCategory("None", "D", false);
		int GL_GL = createGLCategory("Manual", "M", false);
		int GL_ARI = createGLCategory("AR Invoice", "D", false);
		int GL_ARR = createGLCategory("AR Receipt", "D", false);
		int GL_MM = createGLCategory("Material Management", "D", false);
		int GL_API = createGLCategory("AP Invoice", "D", false);
		int GL_APP = createGLCategory("AP Payment", "D", false);

		//	Base DocumentTypes
		createDocType("GL Journal", "Journal", "GLJ", null, 0, 0, 1000, GL_GL);
		int DT_I = createDocType("AR Invoice", "Invoice", "ARI", null, 0, 0, 100000, GL_ARI);
		int DT_II = createDocType("AR Invoice Indirect", "Invoice", "ARI", null, 0, 0, 200000, GL_ARI);
		createDocType("AR Credit Memo", "Credit Memo", "ARC", null, 0, 0, 300000, GL_ARI);
		createDocType("AR Receipt", "Receipt", "ARR", null, 0, 0, 400000, GL_ARR);
		int DT_S  = createDocType("MM Shipment", "Delivery Note", "MMS", null, 0, 0, 500000, GL_MM);
		int DT_SI = createDocType("MM Shipment Indirect", "Delivery Note", "MMS", null, 0, 0, 600000, GL_MM);
		createDocType("MM Receipt", "Vendor Delivery", "MMR", null, 0, 0, 0, GL_MM);
		createDocType("AP Invoice", "Vendor Invoice", "API", null, 0, 0, 0, GL_API);
		createDocType("AP CreditMemo", "Vendor Credit Memo", "APC", null, 0, 0, 0, GL_API);
		createDocType("AP Payment", "Vendor Payment", "APP", null, 0, 0, 700000, GL_APP);
		createDocType("Purchase Order", "Purchase Order", "POO", null, 0, 0, 800000, GL_None);
		createDocType("Purchase Requisition", "Purchase Requisition", "POR", null, 0, 0, 900000, GL_None);

		//  Order Entry
		createDocType("Quotation", "Binding offer", "SOO", "OB", 0, 0, 10000, GL_None);
		createDocType("Proposal", "Non binding offer", "SOO", "ON", 0, 0, 20000, GL_None);
		createDocType("Prepay Order", "Prepay Order", "SOO", "PR", DT_S, DT_I, 30000, GL_None);
		createDocType("Return Material", "Return Material Authorization", "SOO", "RM", DT_S, DT_I, 40000, GL_None);
		createDocType("Standard Order", "Order Confirmation", "SOO", "SO", DT_S, DT_I, 50000, GL_None);
		createDocType("Credit Order", "Order Confirmation", "SOO", "WI", DT_SI, DT_I, 60000, GL_None);   //  RE
		createDocType("Warehouse Order", "Order Confirmation", "SOO", "WP", DT_S, DT_I, 70000, GL_None);    //  LS
		int DT = createDocType("POS Order", "Order Confirmation", "SOO", "WR", DT_SI, DT_II, 80000, GL_None);    // Bar
		createPreference("C_DocTypeTarget_ID", String.valueOf(DT), 143);

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_AcctSchema1_ID=").append(C_AcctSchema_ID)
			.append(", C_Calendar_ID=").append(C_Calendar_ID)
			.append(" WHERE AD_Client_ID=").append(AD_Client_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//
		return true;
	}   //  createAccounting

	/**
	 *  Get Account ID for key
	 *  @param key key
	 *  @return C_ValidCombination_ID
	 */
	private int getAcct (String key)
	{
		Log.trace(Log.l4_Data, "MSetup.getAcct - " + key);
		//  Element
		int C_ElementValue_ID = m_nap.getC_ElementValue_ID(key);
		Account vc = Account.getDefault(m_AcctSchema);
		vc.setAccount_ID(C_ElementValue_ID);
		vc.save(AD_Client_ID, 0);
		int C_ValidCombination_ID = vc.getC_ValidCombination_ID();

		//  Error
		if (C_ValidCombination_ID == 0)
			Log.error("MSetup.getAcct - no account for " + key);
		return C_ValidCombination_ID;
	}   //  getAcct

	/**
	 *  Create GL Category
	 *  @param Name name
	 *  @param CategoryType category type
	 *  @param isDefault is default value
	 *  @return GL_Category_ID
	 */
	private int createGLCategory (String Name, String CategoryType, boolean isDefault)
	{
		int GL_Category_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "GL_Category");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO GL_Category ");
		sqlCmd.append("(GL_Category_ID,").append(m_stdColumns).append(",")
			.append("Name,CategoryType,IsDefault) VALUES (")
			.append(GL_Category_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(Name).append("','").append(CategoryType).append("','")
			.append(isDefault ? "Y" : "N").append("')");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createGLCategory - GL Category NOT created - " + Name);
		//
		return GL_Category_ID;
	}   //  createGLCategory

	/**
	 *  Create Document Types with Sequence
	 *  @param Name name
	 *  @param PrintName print name
	 *  @param DocBaseType document base type
	 *  @param DocSubTypeSO sales order sub type
	 *  @param C_DocTypeShipment_ID shipment doc
	 *  @param C_DocTypeInvoice_ID invoice doc
	 *  @param StartNo start doc no
	 *  @param GL_Category_ID gl category
	 *  @return C_DocType_ID doc type
	 */
	private int createDocType (String Name, String PrintName,
		String DocBaseType, String DocSubTypeSO,
		int C_DocTypeShipment_ID, int C_DocTypeInvoice_ID,
		int StartNo, int GL_Category_ID)
	{
		StringBuffer sqlCmd = null;
		//  Get Sequence
		int AD_Sequence_ID = 0;
		if (StartNo != 0)   //  manual sequenec, if startNo == 0
		{
			AD_Sequence_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Sequence");
			sqlCmd = new StringBuffer ("INSERT INTO AD_Sequence ");
			sqlCmd.append("(AD_Sequence_ID,").append(m_stdColumns).append(",");
			sqlCmd.append("Name,IsAutoSequence,IncrementNo,StartNo,CurrentNext,");
			sqlCmd.append("CurrentNextSys,IsTableID) VALUES (");
			sqlCmd.append(AD_Sequence_ID).append(",").append(m_stdValues).append(",");
			sqlCmd.append("'").append(Name).append("','Y',1,").append(StartNo).append(",").append(StartNo).append(",");
			sqlCmd.append(StartNo/10).append(",'N')");
			int no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createDocType - Sequence NOT created - " + Name);
		}

		//  Get Document Type
		int C_DocType_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_DocType");
		sqlCmd = new StringBuffer ("INSERT INTO	C_DocType ");
		sqlCmd.append("(C_DocType_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,PrintName,DocBaseType,DocSubTypeSO,");
		sqlCmd.append("C_DocTypeShipment_ID,C_DocTypeInvoice_ID,");
		sqlCmd.append("IsDocNoControlled,DocNoSequence_ID,GL_Category_ID,");
		sqlCmd.append("IsPrinted,IsTransferred,DocumentCopies,IsSOTrx)");
		//
		sqlCmd.append(" VALUES (").append(C_DocType_ID).append(",").append(m_stdValues).append(",");
		//  Name,PrintName,DocBaseType,DocSubTypeSO,
		sqlCmd.append("'").append(Name).append("','").append(PrintName).append("','").append(DocBaseType).append("',");
		if (DocSubTypeSO == null || DocSubTypeSO.length() == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append("'").append(DocSubTypeSO).append("',");
		//  C_DocTypeShipment_ID,C_DocTypeInvoice_ID,
		if (C_DocTypeShipment_ID == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append(C_DocTypeShipment_ID).append(",");
		if (C_DocTypeInvoice_ID == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append(C_DocTypeInvoice_ID).append(",");
		//  IsDocNoControlled,DocNoSequence_ID,GL_Category_ID,
		if (AD_Sequence_ID == 0)
			sqlCmd.append("'N',NULL,");
		else
			sqlCmd.append("'Y',").append(AD_Sequence_ID).append(",");
		sqlCmd.append(GL_Category_ID).append(",");
		//  IsPrinted,IsTransferred,DocumentCopies,IsSOTrx)
		sqlCmd.append("'Y',");
		if (DocBaseType.equals("SOO"))
			sqlCmd.append("'N',");
		else
			sqlCmd.append("'Y',");
		sqlCmd.append("0,");
		if (DocBaseType.startsWith("AR") || DocBaseType.equals("MMS") || DocBaseType.equals("SOO"))
			sqlCmd.append("'Y'");
		else
			sqlCmd.append("'N'");
		sqlCmd.append(")");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createDocType - DocType NOT created - " + Name);
		//
		return C_DocType_ID;
	}   //  createDocType

	/*************************************************************************/

	/**
	 *  Create Default main entities.
	 *  - Dimensions & BPGroup, Prod Category)
	 *  - Location, Locator, Warehouse
	 *  - PriceList
	 *  - Cashbook, PaymentTerm
	 *  @param C_Country_ID country
	 *  @param City city
	 *  @param C_Region_ID region
	 *  @return true if created
	 */
	public boolean createEntities (int C_Country_ID, String City, int C_Region_ID)
	{
		Log.trace(Log.l3_Util, "MSetup.createEntries", "C_Country_ID=" + C_Country_ID + ", City=" + City + ", C_Region_ID=" + C_Region_ID);
		m_info.append("\n----\n");
		//
		String defaultName = Msg.translate(m_lang, "Standard");
		String defaultEntry = "'" + defaultName + "',";
		StringBuffer sqlCmd = null;
		int no = 0;

		//	Create Marketing Channel/Campaign
		int C_Channel_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Channel");
		sqlCmd = new StringBuffer("INSERT INTO C_Channel ");
		sqlCmd.append("(C_Channel_ID,Name,");
		sqlCmd.append(m_stdColumns).append(") VALUES (");
		sqlCmd.append(C_Channel_ID).append(",").append(defaultEntry);
		sqlCmd.append(m_stdValues).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Channel NOT inserted");
		int C_Campaign_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Campaign");
		sqlCmd = new StringBuffer("INSERT INTO C_Campaign ");
		sqlCmd.append("(C_Campaign_ID,C_Channel_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,Costs) VALUES (");
		sqlCmd.append(C_Campaign_ID).append(",").append(C_Channel_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Campaign_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Campaign NOT inserted");
		if (m_hasMCampaign)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Campaign_ID=").append(C_Campaign_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='MC'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Campaign NOT updated");
		}

		//	Create Sales Region
		int C_SalesRegion_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_SalesRegion");
		sqlCmd = new StringBuffer ("INSERT INTO C_SalesRegion ");
		sqlCmd.append("(C_SalesRegion_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsSummary) VALUES (");
		sqlCmd.append(C_SalesRegion_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_SalesRegion_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRegion NOT inserted");
		if (m_hasSRegion)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_SalesRegion_ID=").append(C_SalesRegion_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='SR'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement SalesRegion NOT updated");
		}

		/**
		 *  Business Partner
		 */
		//  Create BP Group
		int C_BP_Group_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BP_Group");
		sqlCmd = new StringBuffer ("INSERT INTO C_BP_Group ");
		sqlCmd.append("(C_BP_Group_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault) VALUES ( ");
		sqlCmd.append(C_BP_Group_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BP_Group_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BP Group NOT inserted");

		//	Create BPartner
		int C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_BP_Group_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BPartner_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BPartner NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("C_BPartner_ID=").append(C_BPartner_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
		sqlCmd.append(" AND ElementType='BP'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element BPartner NOT updated");
		createPreference("C_BPartner_ID", String.valueOf(C_BPartner_ID), 143);

		/**
		 *  Product
		 */
		//  Create Product Category
		int M_Product_Category_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Product_Category");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product_Category ");
		sqlCmd.append("(M_Product_Category_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault,PlannedMargin) VALUES (");
		sqlCmd.append(M_Product_Category_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y',0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_Category_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product Category NOT inserted");

		//  UOM (EA)
		int C_UOM_ID = 100;

		//  TaxCategory
		int C_TaxCategory_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_TaxCategory");
		sqlCmd = new StringBuffer ("INSERT INTO C_TaxCategory ");
		sqlCmd.append("(C_TaxCategory_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,IsDefault) VALUES (");
		sqlCmd.append(C_TaxCategory_ID).append(",").append(m_stdValues).append(", ");
		if (C_Country_ID == 100)    // US
			sqlCmd.append("'Sales Tax','Y')");
		else
			sqlCmd.append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - TaxCategory NOT inserted");

		//  Tax - Zero Rate
		int C_Tax_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Tax");
		sqlCmd = new StringBuffer ("INSERT INTO C_Tax ");
		sqlCmd.append("(C_Tax_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,Rate,C_TaxCategory_ID,C_Country_ID,TO_Country_ID,ValidFrom,IsDefault) VALUES (");
		sqlCmd.append(C_Tax_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append("0,").append(C_TaxCategory_ID).append(",")
			.append(C_Country_ID).append(",").append(C_Country_ID)
			.append(",TO_DATE('01-JAN-1990'),'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Tax NOT inserted");

		//	Create Product
		int M_Product_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Product");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product ");
		sqlCmd.append("(M_Product_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_UOM_ID,M_Product_Category_ID,C_TaxCategory_ID) VALUES (");
		sqlCmd.append(M_Product_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry);
		sqlCmd.append(C_UOM_ID).append(",").append(M_Product_Category_ID).append(",");
		sqlCmd.append(C_TaxCategory_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("M_Product_ID=").append(M_Product_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
		sqlCmd.append(" AND ElementType='PR'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element Product NOT updated");

		/**
		 *  Warehouse
		 */
		//  Location (Company)
		int C_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		sqlCmd = new StringBuffer ("UPDATE AD_OrgInfo SET C_Location_ID=");
		sqlCmd.append(C_Location_ID).append(" WHERE AD_Org_ID=").append(AD_Org_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Location NOT inserted");
		createPreference("C_Country_ID", String.valueOf(C_Country_ID), 0);

		//  Default Warehouse
		int M_Warehouse_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Warehouse");
		sqlCmd = new StringBuffer ("INSERT INTO M_Warehouse ");
		sqlCmd.append("(M_Warehouse_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Location_ID,Separator) VALUES (");
		sqlCmd.append(M_Warehouse_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Location_ID).append(",'-')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Warehouse NOT inserted");

		//   Locator
		int M_Locator_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Locator");
		sqlCmd = new StringBuffer ("INSERT INTO M_Locator ");
		sqlCmd.append("(M_Locator_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,M_Warehouse_ID,X,Y,Z,PriorityNo,IsDefault) VALUES (");
		sqlCmd.append(M_Locator_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(M_Warehouse_ID).append(",0,0,0,50,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Locator NOT inserted");

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_BPartnerCashTrx_ID=").append(C_BPartner_ID);
		sqlCmd.append(",M_ProductFreight_ID=").append(M_Product_ID);
//		sqlCmd.append("C_UOM_Volume_ID=");
//		sqlCmd.append(",C_UOM_Weight_ID=");
//		sqlCmd.append(",C_UOM_Length_ID=");
//		sqlCmd.append(",C_UOM_Time_ID=");
		sqlCmd.append(" WHERE AD_Client_ID=").append(AD_Client_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createEntities - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		/**
		 *  Other
		 */
		//  PriceList
		int M_PriceList_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_PriceList");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList ");
		sqlCmd.append("(M_PriceList_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID,IsDefault) VALUES (");
		sqlCmd.append(M_PriceList_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(",'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList NOT inserted");
		//  DiscountSchema
		int M_DiscountSchema_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_DiscountSchema");
		sqlCmd = new StringBuffer ("INSERT INTO M_DiscountSchema ");
		sqlCmd.append("(M_DiscountSchema_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,DiscountType, FlatDiscount) VALUES (");
		sqlCmd.append(M_DiscountSchema_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append("SysDate,'F',0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - DiscountSchema NOT inserted");
		//  PriceList Version
		int M_PriceList_Version_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_PriceList_Version");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList_Version ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,M_PriceList_ID,M_DiscountSchema_ID) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY-MM-DD'),SysDate,").append(M_PriceList_ID)
			.append(",").append(M_DiscountSchema_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList_Version NOT inserted");
		//  ProductPrice
		sqlCmd = new StringBuffer ("INSERT INTO M_ProductPrice ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("M_Product_ID,PriceList,PriceStd,PriceLimit) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(M_Product_ID).append(",1,1,1)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - ProductPrice NOT inserted");

		//  Location for Standard BP
		C_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");

		DB.executeUpdate(sqlCmd.toString());
		int C_BPartner_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(City).append("',").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location NOT inserted");

		//	Create Sales Rep for User
		C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_U_Name).append("','").append(AD_User_U_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "IsSalesRep")).append("=").append(AD_User_U_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (User) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_U_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (User) NOT updated");

		//	Create Sales Rep for Admin
		C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_Name).append("','").append(AD_User_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "IsSalesRep")).append("=").append(AD_User_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (Admin) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (Admin) NOT updated");

		//  Payment Term
		int C_PaymentTerm_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_PaymentTerm");
		sqlCmd = new StringBuffer ("INSERT INTO C_PaymentTerm ");
		sqlCmd.append("(C_PaymentTerm_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,NetDays,GraceDays,DiscountDays,Discount,DiscountDays2,Discount2,IsDefault) VALUES (");
		sqlCmd.append(C_PaymentTerm_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'Immediate','Immediate',0,0,0,0,0,0,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PaymentTerm NOT inserted");

		//  Project Cycle
		C_Cycle_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Cycle");
		sqlCmd = new StringBuffer ("INSERT INTO C_Cycle ");
		sqlCmd.append("(C_Cycle_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_Cycle_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Cycle NOT inserted");

		//  ProjectStatus
		int PS = createProjectStatus(110, "Prospect", "");
		createProjectStatus(120, "Qualified", "");
		createProjectStatus(130, "Opportunity", "Has Budget and Timeframe");
		createProjectStatus(140, "Solution", "Need Analysis");
		createProjectStatus(150, "Quote", "");
		createProjectStatus(160, "Close", "Contract Negotiations");
		createProjectStatus(200, "Delivery", "Delivery of Goods and Service");
		createPreference("C_ProjectStatus_ID", String.valueOf(PS), 0);

		/**
		 *  Organization level data	===========================================
		 */

		//	Create Default Project
		int C_Project_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Project");
		sqlCmd = new StringBuffer ("INSERT INTO C_Project ");
		sqlCmd.append("(C_Project_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Currency_ID,IsSummary) VALUES (");
		sqlCmd.append(C_Project_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Currency_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Project_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Project NOT inserted");
		//  Default Project
		if (m_hasProject)
		{
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Project_ID=").append(C_Project_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='PJ'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Project NOT updated");
		}

		//  CashBook
		int C_CashBook_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_CashBook");
		sqlCmd = new StringBuffer ("INSERT INTO C_CashBook ");
		sqlCmd.append("(C_CashBook_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_CashBook_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_CashBook_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - CashBook NOT inserted");


		//  Create Other Defaults
		try
		{
			CallableStatement cstmt = DB.prepareCall("CALL AD_Setup(?,?)");
			cstmt.setInt(1, AD_Client_ID);
			cstmt.setInt(2, AD_Org_ID);
			cstmt.execute();
			cstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("MSetup.CreateEntities - Call AD_Setup", e);
		}

		return true;
	}   //  createEntities

	/**
	 *  Create Project Status and CycleStep
	 *  @param SeqNo seq
	 *  @param Name name
	 *  @param Description description
	 *  @return C_ProjectStatus_ID
	 */
	private int createProjectStatus (int SeqNo, String Name, String Description)
	{
		int C_ProjectStatus_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_ProjectStatus");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO C_ProjectStatus ");
		sqlCmd.append("(C_ProjectStatus_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("SeqNo,Name,Description) VALUES (");
		sqlCmd.append(C_ProjectStatus_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(SeqNo).append(",'").append(Name).append("','").append(Description).append("')");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createProjectStatus - ProjectStatus NOT inserted - " + Name);
		//
		int C_CycleStep_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_CycleStep");
		sqlCmd = new StringBuffer ("INSERT INTO C_CycleStep ");
		sqlCmd.append("(C_CycleStep_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("C_Cycle_ID,SeqNo,C_ProjectStatus_ID,RelativeWeight) VALUES (");
		sqlCmd.append(C_CycleStep_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(C_Cycle_ID).append(",").append(SeqNo).append(",").append(C_ProjectStatus_ID).append(",1)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createProjectStatus - CycleStep NOT inserted - " + Name);
		//
		return C_ProjectStatus_ID;
	}   //  createProjectStatus

	/**
	 *  Create Preference
	 *  @param Attribute attribute
	 *  @param Value value
	 *  @param AD_Window_ID window
	 */
	private void createPreference (String Attribute, String Value, int AD_Window_ID)
	{
		int AD_Preference_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Preference");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO AD_Preference ");
		sqlCmd.append("(AD_Preference_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Attribute,Value,AD_Window_ID) VALUES (");
		sqlCmd.append(AD_Preference_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(Attribute).append("','").append(Value).append("',");
		if (AD_Window_ID == 0)
			sqlCmd.append("NULL)");
		else
			sqlCmd.append(AD_Window_ID).append(")");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createPreference - Preference NOT inserted - " + Attribute);
	}   //  createPreference

	/*************************************************************************/

	/**
	 *  Get Client
	 *  @return AD_Client_ID
	 */
	public int getAD_Client_ID()
	{
		return AD_Client_ID;
	}
	public int getAD_Org_ID()
	{
		return AD_Org_ID;
	}
	public int getAD_User_ID()
	{
		return AD_User_ID;
	}
	public int getAD_Role_ID()
	{
		return AD_Role_ID;
	}
	public int getC_AcctSchema_ID()
	{
		return C_AcctSchema_ID;
	}
	public String getInfo()
	{
		return m_info.toString();
	}
}   //  MSetup
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
package org.compiere.model;

import java.util.*;
import java.sql.*;
import java.io.*;

import org.compiere.util.*;

/**
 * Initial Setup Model
 *
 * @author Jorg Janke
 * @version $Id: MSetup.java,v 1.36 2004/05/20 05:57:47 jjanke Exp $
 */
public final class MSetup
{
	/**
	 *  Constructor
	 *  @param ctx context
	 *  @param WindowNo window
	 */
	public MSetup(Properties ctx, int WindowNo)
	{
		m_ctx = new Properties(ctx);	//	copy
		m_lang = Env.getAD_Language(m_ctx);
		m_WindowNo = WindowNo;
	}   //  MSetup

	private Properties      m_ctx;
	private String          m_lang;
	private int             m_WindowNo;
	private StringBuffer    m_info;
	//
	private String          m_clientName;
//	private String          m_orgName;
	//
	private String          m_stdColumns = "AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy";
	private String          m_stdValues;
	private String          m_stdValuesOrg;
	//
	private NaturalAccountMap m_nap = null;
	//
	private MClient			m_client;
	private MOrg			m_org;
	private MAcctSchema		m_as;
	//
	private int     		AD_User_ID;
	private String  		AD_User_Name;
	private int     		AD_User_U_ID;
	private String  		AD_User_U_Name;
	private int     		AD_Role_ID;
	private int     		C_Calendar_ID;
	private int     		m_AD_Tree_Account_ID;
	private int     		C_Cycle_ID;
	//
	private boolean         m_hasProject = false;
	private boolean         m_hasMCampaign = false;
	private boolean         m_hasSRegion = false;


	/**
	 *  Create Client Info.
	 *  - Client, Trees, Org, Role, User, User_Role
	 *  @param clientName client name
	 *  @param orgName org name
	 *  @param userClient user id client
	 *  @param userOrg user id org
	 *  @return true if created
	 */
	public boolean createClient (String clientName, String orgName,
		String userClient, String userOrg)
	{
		Log.trace(Log.l3_Util, "MSetup.createClient - " + clientName);
		//  info header
		m_info = new StringBuffer();
		//  Standarc columns
		String name = null;
		String sql = null;
		int no = 0;

		/**
		 *  Create Client
		 */
		name = clientName;
		if (name == null || name.length() == 0)
			name = "newClient";
		m_clientName = name;
		m_client = new MClient(m_ctx);
		m_client.setValue(m_clientName);
		m_client.setName(m_clientName);
		if (!m_client.save())
		{
			String err = "MSetup.createClient - Client NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		int AD_Client_ID = m_client.getAD_Client_ID();
		Env.setContext(m_ctx, m_WindowNo, "AD_Client_ID", AD_Client_ID);
		Env.setContext(m_ctx, "#AD_Client_ID", AD_Client_ID);

		//	Standard Values
		m_stdValues = String.valueOf(AD_Client_ID) + ",0,'Y',SysDate,0,SysDate,0";
		//  Info - Client
		m_info.append(Msg.translate(m_lang, "AD_Client_ID")).append("=").append(name).append("\n");

		//	Setup Sequences
		if (!MSequence.checkClientSequences (m_ctx, AD_Client_ID))
		{
			String err = "MSetup.createClient - Sequences NOT created";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		
		//  Trees and Client Info
		if (!m_client.setupClientInfo(m_lang))
		{
			String err = "MSetup.createClient - ClientInfo NOT created";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		m_AD_Tree_Account_ID = m_client.getSetup_AD_Tree_Account_ID();

		/**
		 *  Create Org
		 */
		name = orgName;
		if (name == null || name.length() == 0)
			name = "newOrg";
		m_org = new MOrg (m_client, name);
		if (!m_org.save())
		{
			String err = "MSetup.createClient - Org NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		Env.setContext(m_ctx, m_WindowNo, "AD_Org_ID", getAD_Org_ID());
		Env.setContext(m_ctx, "#AD_Org_ID", getAD_Org_ID());
		m_stdValuesOrg = AD_Client_ID + "," + getAD_Org_ID() + ",'Y',SysDate,0,SysDate,0";
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_Org_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Roles
		 *  - Admin
		 *  - User
		 */
		name = m_clientName + " Admin";
		name = DB.TO_STRING(name);
		AD_Role_ID = getNextID(AD_Client_ID, "AD_Role");
		sql = "INSERT INTO AD_Role(" + m_stdColumns + ",AD_Role_ID,"
			+ "Name,Description,UserLevel,ClientList,OrgList)"
			+ " VALUES (" + m_stdValues + "," + AD_Role_ID + ","
			+ name + "," + name + ",' CO','" + AD_Client_ID + "','0," + getAD_Org_ID() + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin Role A NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  OrgAccess x,0
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValues + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_OrgAccess 0 NOT created");
		//  OrgAccess x,y
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValuesOrg + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_OrgAccess NOT created");
		//  Info - Admin Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		//
		name = m_clientName + " User";
		name = DB.TO_STRING(name);
		int AD_Role_ID_U = getNextID(AD_Client_ID, "AD_Role");
		sql = "INSERT INTO AD_Role(" + m_stdColumns + ",AD_Role_ID,"
			+ "Name,Description,UserLevel,ClientList,OrgList)"
			+ " VALUES (" + m_stdValues + "," + AD_Role_ID_U + ","
			+ name + "," + name + ",'  O','" + AD_Client_ID + "','" + getAD_Org_ID() + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - User Role A NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  OrgAccess x,y
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValuesOrg + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - User Role_OrgAccess NOT created");
		//  Info - Client Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Users
		 *  - Client
		 *  - Org
		 */
		name = userClient;
		if (name == null || name.length() == 0)
			name = m_clientName + "Client";
		AD_User_ID = getNextID(AD_Client_ID, "AD_User");
		AD_User_Name = name;
		name = DB.TO_STRING(name);
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + ","
			+ name + "," + name + "," + name + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin User NOT inserted - " + AD_User_Name;
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_Name).append("/").append(AD_User_Name).append("\n");

		name = userOrg;
		if (name == null || name.length() == 0)
			name = m_clientName + "Org";
		AD_User_U_ID = getNextID(AD_Client_ID, "AD_User");
		AD_User_U_Name = name;
		name = DB.TO_STRING(name);
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + ","
			+ name + "," + name + "," + name + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Org User NOT inserted - " + AD_User_U_Name;
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_U_Name).append("/").append(AD_User_U_Name).append("\n");

		/**
		 *  Create User-Role
		 */
		//  ClientUser          - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+Admin NOT inserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+User NOT inserted");
		//  OrgUser             - User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole OrgUser+Org NOT inserted");
		//  SuperUser(100)      - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + ",100," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole SuperUser+Admin NOT inserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + ",100," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole SuperUser+User NOT inserted");

		return true;
	}   //  createClient



	/**************************************************************************
	 *  Create Accounting elements.
	 *  - Calendar
	 *  - Account Trees
	 *  - Account Values
	 *  - Accounting Schema
	 *  - Default Accounts
	 *
	 *  @param currency currency
	 *  @param hasProduct has product segment
	 *  @param hasBPartner has bp segment
	 *  @param hasProject has project segment
	 *  @param hasMCampaign has campaign segment
	 *  @param hasSRegion has sales region segment
	 *  @param AccountingFile file name of accounting file
	 *  @return true if created
	 */
	public boolean createAccounting(KeyNamePair currency,
		boolean hasProduct, boolean hasBPartner, boolean hasProject,
		boolean hasMCampaign, boolean hasSRegion,
		File AccountingFile)
	{
		Log.trace(Log.l3_Util, "MSetup.createAccounting - " + m_client);
		//
		m_hasProject = hasProject;
		m_hasMCampaign = hasMCampaign;
		m_hasSRegion = hasSRegion;

		//  Standard variables
		m_info = new StringBuffer();
		String name = null;
		StringBuffer sqlCmd = null;
		int no = 0;

		/**
		 *  Create Calendar
		 */
		C_Calendar_ID = getNextID(m_client.getAD_Client_ID(), "C_Calendar");
		name = m_clientName + " " + Msg.translate(m_lang, "C_Calendar_ID");
		name = DB.TO_STRING(name);
		sqlCmd = new StringBuffer("INSERT INTO C_Calendar(");
		sqlCmd.append(m_stdColumns).append(",C_Calendar_ID,Name)").append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_Calendar_ID).append(",").append(name).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Calendar NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_Calendar_ID")).append("=").append(name).append("\n");

		//  Year
		int C_Year_ID = getNextID(m_client.getAD_Client_ID(), "C_Year");
		sqlCmd = new StringBuffer ("INSERT INTO C_Year ");
		sqlCmd.append("(C_Year_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Year,C_Calendar_ID) VALUES (");
		sqlCmd.append(C_Year_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY'),").append(C_Calendar_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createAccounting - Year NOT inserted");
		//	TODO Create Periods


		//	Create Account Elements
		name = m_clientName + " " + Msg.translate(m_lang, "Account_ID");
		MElement element = new MElement (m_client, name, 
			MElement.ELEMENTTYPE_Account, m_AD_Tree_Account_ID);
		if (!element.save())
		{
			String err = "MSetup.createAccounting - Acct Element NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		int C_Element_ID = element.getC_Element_ID();
		m_info.append(Msg.translate(m_lang, "C_Element_ID")).append("=").append(name).append("\n");

		//	Create Account Values
		m_nap = new NaturalAccountMap(m_ctx);
		String errMsg = m_nap.parseFile(AccountingFile);
		if (errMsg.length() != 0)
		{
			Log.error(errMsg);
			m_info.append(errMsg);
			return false;
		}
		if (m_nap.saveAccounts(getAD_Client_ID(), getAD_Org_ID(), C_Element_ID))
			m_info.append(Msg.translate(m_lang, "C_ElementValue_ID")).append(" # ").append(m_nap.size()).append("\n");
		else
		{
			String err = "MSetup.createAccounting - Acct Element Values NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		int C_ElementValue_ID = m_nap.getC_ElementValue_ID("DEFAULT_ACCT");
		Log.trace(Log.l4_Data, "C_ElementValue_ID=" + C_ElementValue_ID);

		/**
		 *  Create AccountingSchema
		 */
		m_as = new MAcctSchema (m_client, currency);
		if (!m_as.save())
		{
			String err = "MSetup.createAccounting - AcctSchema NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_AcctSchema_ID")).append("=").append(m_as.getName()).append("\n");

		/**
		 *  Create AccountingSchema Elements (Structure)
		 */
		String sql2 = null;
		if (Env.isBaseLanguage(m_lang, "AD_Reference"))	//	Get ElementTypes & Name
			sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=181";
		else
			sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Reference_ID=181 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID";
		//
		int Element_OO=0, Element_AC=0, Element_PR=0, Element_BP=0, Element_PJ=0,
			Element_MC=0, Element_SR=0;
		try
		{
			int AD_Client_ID = m_client.getAD_Client_ID();
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next())
			{
				String ElementType = rs.getString(1);
				name = rs.getString(2);
				//
				String IsMandatory = null;
				String IsBalanced = "N";
				int SeqNo = 0;
				int C_AcctSchema_Element_ID = 0;

				if (ElementType.equals("OO"))
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_OO = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					IsBalanced = "Y";
					SeqNo = 10;
				}
				else if (ElementType.equals("AC"))
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_AC = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					SeqNo = 20;
				}
				else if (ElementType.equals("PR") && hasProduct)
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_PR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 30;
				}
				else if (ElementType.equals("BP") && hasBPartner)
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_BP = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 40;
				}
				else if (ElementType.equals("PJ") && hasProject)
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_PJ = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 50;
				}
				else if (ElementType.equals("MC") && hasMCampaign)
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_MC = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 60;
				}
				else if (ElementType.equals("SR") && hasSRegion)
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_SR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 70;
				}
				//	Not OT, LF, LT, U1, U2, AY

				if (IsMandatory != null)
				{
					sqlCmd = new StringBuffer ("INSERT INTO C_AcctSchema_Element(");
					sqlCmd.append(m_stdColumns).append(",C_AcctSchema_Element_ID,C_AcctSchema_ID,")
						.append("ElementType,Name,SeqNo,IsMandatory,IsBalanced) VALUES (");
					sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_Element_ID).append(",").append(m_as.getC_AcctSchema_ID()).append(",")
						.append("'").append(ElementType).append("','").append(name).append("',").append(SeqNo).append(",'")
						.append(IsMandatory).append("','").append(IsBalanced).append("')");
					no = DB.executeUpdate(sqlCmd.toString());
					if (no == 1)
						m_info.append(Msg.translate(m_lang, "C_AcctSchema_Element_ID")).append("=").append(name).append("\n");

					/** Default value for mandatory elements: OO and AC */
					if (ElementType.equals("OO"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET Org_ID=");
						sqlCmd.append(getAD_Org_ID()).append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Org in AcctSchamaElement NOT updated");
					}
					if (ElementType.equals("AC"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET C_ElementValue_ID=");
						sqlCmd.append(C_ElementValue_ID).append(", C_Element_ID=").append(C_Element_ID);
						sqlCmd.append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Account in AcctSchamaElement NOT updated");
					}
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e1)
		{
			Log.error ("MSetup.createAccounting - Elements", e1);
		}
		//  Create AcctSchema


		//  Create GL Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_GL (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "USESUSPENSEBALANCING,SUSPENSEBALANCING_ACCT,"
			+ "USESUSPENSEERROR,SUSPENSEERROR_ACCT,"
			+ "USECURRENCYBALANCING,CURRENCYBALANCING_ACCT,"
			+ "RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,"
			+ "INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT,"
			+ "PPVOFFSET_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(m_as.getC_AcctSchema_ID()).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEBALANCING_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEERROR_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("CURRENCYBALANCING_ACCT")).append(",");
		//  RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,
		sqlCmd.append(getAcct("RETAINEDEARNING_ACCT")).append(",");
		sqlCmd.append(getAcct("INCOMESUMMARY_ACCT")).append(",");
		//  INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT)
		sqlCmd.append(getAcct("INTERCOMPANYDUETO_ACCT")).append(",");
		sqlCmd.append(getAcct("INTERCOMPANYDUEFROM_ACCT")).append(",");
		sqlCmd.append(getAcct("PPVOFFSET_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - GL Accounts NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//	Create Std Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_DEFAULT (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT, "
			+ "P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT, "
			+ "P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT, "
			+ "C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT, "
			+ "V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT, "
			+ "PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT, "
			+ "UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT, "
			+ "WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT, "
			+ "PJ_ASSET_ACCT,PJ_WIP_ACCT,"
			+ "T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT, "
			+ "B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,"
			+ "B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,"
			+ "B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT, "
			+ "CH_EXPENSE_ACCT,CH_REVENUE_ACCT, "
			+ "UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT, "
			+ "CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(m_as.getC_AcctSchema_ID()).append(",");
		//  W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT
		sqlCmd.append(getAcct("W_INVENTORY_ACCT")).append(",");
		sqlCmd.append(getAcct("W_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("W_REVALUATION_ACCT")).append(",");
		sqlCmd.append(getAcct("W_INVACTUALADJUST_ACCT")).append(", ");
		//  P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT,
		sqlCmd.append(getAcct("P_REVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("P_COGS_ACCT")).append(", ");
		//  P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT,
		sqlCmd.append(getAcct("P_PURCHASEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_INVOICEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTREC_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTGRANT_ACCT")).append(", ");
		//  C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("C_RECEIVABLE_ACCT")).append(",");
		sqlCmd.append(getAcct("C_PREPAYMENT_ACCT")).append(", ");
		//  V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("V_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("V_LIABILITY_SERVICES_ACCT")).append(",");
		sqlCmd.append(getAcct("V_PREPAYMENT_ACCT")).append(", ");
		//  PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT,
		sqlCmd.append(getAcct("PAYDISCOUNT_EXP_ACCT")).append(",");
		sqlCmd.append(getAcct("PAYDISCOUNT_REV_ACCT")).append(",");
		sqlCmd.append(getAcct("WRITEOFF_ACCT")).append(", ");
		//  UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT,
		sqlCmd.append(getAcct("UNREALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("UNREALIZEDLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDLOSS_ACCT")).append(", ");
		//  WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT,
		sqlCmd.append(getAcct("WITHHOLDING_ACCT")).append(",");
		sqlCmd.append(getAcct("E_PREPAYMENT_ACCT")).append(",");
		sqlCmd.append(getAcct("E_EXPENSE_ACCT")).append(", ");
		//  PJ_ASSET_ACCT,PJ_WIP_ACCT,
		sqlCmd.append(getAcct("PJ_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("PJ_WIP_ACCT")).append(",");
		//  T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT,
		sqlCmd.append(getAcct("T_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("T_RECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("T_DUE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_CREDIT_ACCT")).append(", ");
		//  B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,
		sqlCmd.append(getAcct("B_INTRANSIT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("B_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTREV_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTEXP_ACCT")).append(",");
		//  B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,
		sqlCmd.append(getAcct("B_UNIDENTIFIED_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTLOSS_ACCT")).append(",");
		//  B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT,
		sqlCmd.append(getAcct("B_REVALUATIONGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_REVALUATIONLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("B_PAYMENTSELECT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_UNALLOCATEDCASH_ACCT")).append(", ");
		//  CH_EXPENSE_ACCT,CH_REVENUE_ACCT,
		sqlCmd.append(getAcct("CH_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CH_REVENUE_ACCT")).append(", ");
		//  UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT,
		sqlCmd.append(getAcct("UNEARNEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIPTS_ACCT")).append(", ");
		//  CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT)
		sqlCmd.append(getAcct("CB_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_CASHTRANSFER_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_RECEIPT_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Default Accounts NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//  GL Categories
		createGLCategory("Standard", "M", true);
		int GL_None = createGLCategory("None", "D", false);
		int GL_GL = createGLCategory("Manual", "M", false);
		int GL_ARI = createGLCategory("AR Invoice", "D", false);
		int GL_ARR = createGLCategory("AR Receipt", "D", false);
		int GL_MM = createGLCategory("Material Management", "D", false);
		int GL_API = createGLCategory("AP Invoice", "D", false);
		int GL_APP = createGLCategory("AP Payment", "D", false);
		int GL_CASH = createGLCategory("Cash/Payments", "D", false);

		//	Base DocumentTypes
		createDocType("GL Journal", "Journal", 
			MDocType.DOCBASETYPE_GLJournal, null, 0, 0, 1000, GL_GL);
		createDocType("GL Journal Batch", "Journal Batch", 
			MDocType.DOCBASETYPE_GLJournal, null, 0, 0, 100, GL_GL);
		//	MDocType.DOCBASETYPE_GLDocument
		//
		int DT_I = createDocType("AR Invoice", "Invoice", 
			MDocType.DOCBASETYPE_ARInvoice, null, 0, 0, 100000, GL_ARI);
		int DT_II = createDocType("AR Invoice Indirect", "Invoice", 
			MDocType.DOCBASETYPE_ARInvoice, null, 0, 0, 150000, GL_ARI);
		int DT_IC = createDocType("AR Credit Memo", "Credit Memo", 
			MDocType.DOCBASETYPE_ARCreditMemo, null, 0, 0, 170000, GL_ARI);
		//	MDocType.DOCBASETYPE_ARProFormaInvoice
		
		createDocType("AP Invoice", "Vendor Invoice", 
			MDocType.DOCBASETYPE_APInvoice, null, 0, 0, 0, GL_API);
		createDocType("AP CreditMemo", "Vendor Credit Memo", 
			MDocType.DOCBASETYPE_APCreditMemo, null, 0, 0, 0, GL_API);
		createDocType("Match Invoice", "Match Invoice", 
			MDocType.DOCBASETYPE_MatchInvoice, null, 0, 0, 390000, GL_API);
		
		createDocType("AR Receipt", "Customer Payment", 
			MDocType.DOCBASETYPE_ARReceipt, null, 0, 0, 0, GL_ARR);
		createDocType("AP Payment", "Vendor Payment", 
			MDocType.DOCBASETYPE_APPayment, null, 0, 0, 0, GL_APP);
		createDocType("Allocation", "Allocation", 
			MDocType.DOCBASETYPE_PaymentAllocation, null, 0, 0, 490000, GL_CASH);

		int DT_S  = createDocType("MM Shipment", "Delivery Note", 
			MDocType.DOCBASETYPE_MaterialDelivery, null, 0, 0, 500000, GL_MM);
		int DT_SI = createDocType("MM Shipment Indirect", "Delivery Note", 
			MDocType.DOCBASETYPE_MaterialDelivery, null, 0, 0, 550000, GL_MM);
		
		createDocType("MM Receipt", "Vendor Delivery", 
			MDocType.DOCBASETYPE_MaterialReceipt, null, 0, 0, 0, GL_MM);
		int DT_RM = createDocType("MM Returns", "Customer Returns", 
			MDocType.DOCBASETYPE_MaterialReceipt, null, 0, 0, 570000, GL_MM);
		
		createDocType("Purchase Order", "Purchase Order", 
			MDocType.DOCBASETYPE_PurchaseOrder, null, 0, 0, 800000, GL_None);
		createDocType("Match PO", "Patch PO", 
			MDocType.DOCBASETYPE_MatchPO, null, 0, 0, 890000, GL_None);
		createDocType("Purchase Requisition", "Purchase Requisition", 
			MDocType.DOCBASETYPE_PurchaseRequisition, null, 0, 0, 900000, GL_None);

		createDocType("Bank Statement", "Bank Statement", 
			MDocType.DOCBASETYPE_BankStatement, null, 0, 0, 700000, GL_CASH);
		createDocType("Cash Journal", "Cash Journal",
			MDocType.DOCBASETYPE_CashJournal, null, 0, 0, 750000, GL_CASH);
		
		createDocType("Material Movement", "Material Movement",
			MDocType.DOCBASETYPE_MaterialMovement, null, 0, 0, 610000, GL_MM);
		createDocType("Physical Inventory", "Physical Inventory", 
			MDocType.DOCBASETYPE_MaterialPhysicalInventory, null, 0, 0, 620000, GL_MM);
		createDocType("Material Production", "Material Production", 
			MDocType.DOCBASETYPE_MaterialProduction, null, 0, 0, 630000, GL_MM);
		createDocType("Project Issue", "Project Issue", 
			MDocType.DOCBASETYPE_ProjectIssue, null, 0, 0, 640000, GL_MM);

		//  Order Entry
		createDocType("Binding offer", "Quotation", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_Quotation, 
			0, 0, 10000, GL_None);
		createDocType("Non binding offer", "Proposal", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_Proposal, 
			0, 0, 20000, GL_None);
		createDocType("Prepay Order", "Prepay Order", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_PrepayOrder, 
			DT_S, DT_I, 30000, GL_None);
		createDocType("Return Material", "Return Material Authorization", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_ReturnMaterial, 
			DT_RM, DT_IC, 30000, GL_None);
		createDocType("Standard Order", "Order Confirmation", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_StandardOrder, 
			DT_S, DT_I, 50000, GL_None);
		createDocType("Credit Order", "Order Confirmation", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_OnCreditOrder, 
			DT_SI, DT_I, 60000, GL_None);   //  RE
		createDocType("Warehouse Order", "Order Confirmation", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_WarehouseOrder, 
			DT_S, DT_I,	70000, GL_None);    //  LS
		int DT = createDocType("POS Order", "Order Confirmation", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_POSOrder, 
			DT_SI, DT_II, 80000, GL_None);    // Bar
		//	POS As Default for window SO
		createPreference("C_DocTypeTarget_ID", String.valueOf(DT), 143);

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_AcctSchema1_ID=").append(m_as.getC_AcctSchema_ID())
			.append(", C_Calendar_ID=").append(C_Calendar_ID)
			.append(" WHERE AD_Client_ID=").append(m_client.getAD_Client_ID());
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//
		return true;
	}   //  createAccounting

	/**
	 *  Get Account ID for key
	 *  @param key key
	 *  @return C_ValidCombination_ID
	 */
	private int getAcct (String key)
	{
		Log.trace(Log.l4_Data, "MSetup.getAcct - " + key);
		//  Element
		int C_ElementValue_ID = m_nap.getC_ElementValue_ID(key);
		MAccount vc = MAccount.getDefault(m_as, true);	//	optional null
		vc.setAccount_ID(C_ElementValue_ID);
		vc.save();
		int C_ValidCombination_ID = vc.getC_ValidCombination_ID();

		//  Error
		if (C_ValidCombination_ID == 0)
			Log.error("MSetup.getAcct - no account for " + key);
		return C_ValidCombination_ID;
	}   //  getAcct

	/**
	 *  Create GL Category
	 *  @param Name name
	 *  @param CategoryType category type
	 *  @param isDefault is default value
	 *  @return GL_Category_ID
	 */
	private int createGLCategory (String Name, String CategoryType, boolean isDefault)
	{
		int GL_Category_ID = getNextID(m_client.getAD_Client_ID(), "GL_Category");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO GL_Category ");
		sqlCmd.append("(GL_Category_ID,").append(m_stdColumns).append(",")
			.append("Name,CategoryType,IsDefault) VALUES (")
			.append(GL_Category_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(Name).append("','").append(CategoryType).append("','")
			.append(isDefault ? "Y" : "N").append("')");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createGLCategory - GL Category NOT created - " + Name);
		//
		return GL_Category_ID;
	}   //  createGLCategory

	/**
	 *  Create Document Types with Sequence
	 *  @param Name name
	 *  @param PrintName print name
	 *  @param DocBaseType document base type
	 *  @param DocSubTypeSO sales order sub type
	 *  @param C_DocTypeShipment_ID shipment doc
	 *  @param C_DocTypeInvoice_ID invoice doc
	 *  @param StartNo start doc no
	 *  @param GL_Category_ID gl category
	 *  @return C_DocType_ID doc type
	 */
	private int createDocType (String Name, String PrintName,
		String DocBaseType, String DocSubTypeSO,
		int C_DocTypeShipment_ID, int C_DocTypeInvoice_ID,
		int StartNo, int GL_Category_ID)
	{
		MSequence sequence = null;
		if (StartNo != 0)
		{
			sequence = new MSequence(m_ctx, getAD_Client_ID(), Name, StartNo);
			if (!sequence.save())
				Log.error("MSetup.createDocType - Sequence NOT created - " + Name);
		}
		
		MDocType dt = new MDocType (m_ctx, DocBaseType, Name);
		dt.setPrintName(PrintName);
		if (DocSubTypeSO != null)
			dt.setDocSubTypeSO(DocSubTypeSO);
		if (C_DocTypeShipment_ID != 0)
			dt.setC_DocTypeShipment_ID(C_DocTypeShipment_ID);
		if (C_DocTypeInvoice_ID != 0)
			dt.setC_DocTypeInvoice_ID(C_DocTypeInvoice_ID);
		if (GL_Category_ID != 0)
			dt.setGL_Category_ID(GL_Category_ID);
		if (sequence == null)
			dt.setIsDocNoControlled(false);
		else
			dt.setDocNoSequence_ID(sequence.getAD_Sequence_ID());
		dt.setIsSOTrx();
		if (!dt.save())
			Log.error("MSetup.createDocType - DocType NOT created - " + Name);
		//
		return dt.getC_DocType_ID();
	}   //  createDocType

	
	/**************************************************************************
	 *  Create Default main entities.
	 *  - Dimensions & BPGroup, Prod Category)
	 *  - Location, Locator, Warehouse
	 *  - PriceList
	 *  - Cashbook, PaymentTerm
	 *  @param C_Country_ID country
	 *  @param City city
	 *  @param C_Region_ID region
	 *  @return true if created
	 */
	public boolean createEntities (int C_Country_ID, String City, int C_Region_ID, int C_Currency_ID)
	{
		Log.trace(Log.l3_Util, "MSetup.createEntries", "C_Country_ID=" + C_Country_ID + ", City=" + City + ", C_Region_ID=" + C_Region_ID);
		m_info.append("\n----\n");
		//
		String defaultName = Msg.translate(m_lang, "Standard");
		String defaultEntry = "'" + defaultName + "',";
		StringBuffer sqlCmd = null;
		int no = 0;

		//	Create Marketing Channel/Campaign
		int C_Channel_ID = getNextID(getAD_Client_ID(), "C_Channel");
		sqlCmd = new StringBuffer("INSERT INTO C_Channel ");
		sqlCmd.append("(C_Channel_ID,Name,");
		sqlCmd.append(m_stdColumns).append(") VALUES (");
		sqlCmd.append(C_Channel_ID).append(",").append(defaultEntry);
		sqlCmd.append(m_stdValues).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Channel NOT inserted");
		int C_Campaign_ID = getNextID(getAD_Client_ID(), "C_Campaign");
		sqlCmd = new StringBuffer("INSERT INTO C_Campaign ");
		sqlCmd.append("(C_Campaign_ID,C_Channel_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,Costs) VALUES (");
		sqlCmd.append(C_Campaign_ID).append(",").append(C_Channel_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Campaign_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Campaign NOT inserted");
		if (m_hasMCampaign)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Campaign_ID=").append(C_Campaign_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
			sqlCmd.append(" AND ElementType='MC'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Campaign NOT updated");
		}

		//	Create Sales Region
		int C_SalesRegion_ID = getNextID(getAD_Client_ID(), "C_SalesRegion");
		sqlCmd = new StringBuffer ("INSERT INTO C_SalesRegion ");
		sqlCmd.append("(C_SalesRegion_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsSummary) VALUES (");
		sqlCmd.append(C_SalesRegion_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_SalesRegion_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRegion NOT inserted");
		if (m_hasSRegion)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_SalesRegion_ID=").append(C_SalesRegion_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
			sqlCmd.append(" AND ElementType='SR'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement SalesRegion NOT updated");
		}

		/**
		 *  Business Partner
		 */
		//  Create BP Group
		int C_BP_Group_ID = getNextID(getAD_Client_ID(), "C_BP_Group");
		sqlCmd = new StringBuffer ("INSERT INTO C_BP_Group ");
		sqlCmd.append("(C_BP_Group_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault) VALUES ( ");
		sqlCmd.append(C_BP_Group_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BP_Group_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BP Group NOT inserted");

		//	Create BPartner
		int C_BPartner_ID = getNextID(getAD_Client_ID(), "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_BP_Group_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BPartner_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BPartner NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("C_BPartner_ID=").append(C_BPartner_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
		sqlCmd.append(" AND ElementType='BP'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element BPartner NOT updated");
		createPreference("C_BPartner_ID", String.valueOf(C_BPartner_ID), 143);

		/**
		 *  Product
		 */
		//  Create Product Category
		int M_Product_Category_ID = getNextID(getAD_Client_ID(), "M_Product_Category");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product_Category ");
		sqlCmd.append("(M_Product_Category_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault,PlannedMargin) VALUES (");
		sqlCmd.append(M_Product_Category_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y',0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_Category_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product Category NOT inserted");

		//  UOM (EA)
		int C_UOM_ID = 100;

		//  TaxCategory
		int C_TaxCategory_ID = getNextID(getAD_Client_ID(), "C_TaxCategory");
		sqlCmd = new StringBuffer ("INSERT INTO C_TaxCategory ");
		sqlCmd.append("(C_TaxCategory_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,IsDefault) VALUES (");
		sqlCmd.append(C_TaxCategory_ID).append(",").append(m_stdValues).append(", ");
		if (C_Country_ID == 100)    // US
			sqlCmd.append("'Sales Tax','Y')");
		else
			sqlCmd.append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - TaxCategory NOT inserted");

		//  Tax - Zero Rate
		MTax tax = new MTax (m_ctx, "Standard", Env.ZERO, C_TaxCategory_ID);
		tax.setIsDefault(true);
		if (tax.save())
			m_info.append(Msg.translate(m_lang, "C_Tax_ID")).append("=").append(tax).append("\n");
		else
			Log.error("MSetup.createEntities - Tax NOT inserted");

		//	Create Product
		int M_Product_ID = getNextID(getAD_Client_ID(), "M_Product");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product ");
		sqlCmd.append("(M_Product_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_UOM_ID,M_Product_Category_ID,C_TaxCategory_ID) VALUES (");
		sqlCmd.append(M_Product_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry);
		sqlCmd.append(C_UOM_ID).append(",").append(M_Product_Category_ID).append(",");
		sqlCmd.append(C_TaxCategory_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("M_Product_ID=").append(M_Product_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
		sqlCmd.append(" AND ElementType='PR'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element Product NOT updated");

		/**
		 *  Warehouse
		 */
		//  Location (Company)
		int C_Location_ID = getNextID(getAD_Client_ID(), "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append(DB.TO_STRING(City)).append(",").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		sqlCmd = new StringBuffer ("UPDATE AD_OrgInfo SET C_Location_ID=");
		sqlCmd.append(C_Location_ID).append(" WHERE AD_Org_ID=").append(getAD_Org_ID());
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Location NOT inserted");
		createPreference("C_Country_ID", String.valueOf(C_Country_ID), 0);

		//  Default Warehouse
		int M_Warehouse_ID = getNextID(getAD_Client_ID(), "M_Warehouse");
		sqlCmd = new StringBuffer ("INSERT INTO M_Warehouse ");
		sqlCmd.append("(M_Warehouse_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Location_ID,Separator) VALUES (");
		sqlCmd.append(M_Warehouse_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Location_ID).append(",'-')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Warehouse NOT inserted");

		//   Locator
		int M_Locator_ID = getNextID(getAD_Client_ID(), "M_Locator");
		sqlCmd = new StringBuffer ("INSERT INTO M_Locator ");
		sqlCmd.append("(M_Locator_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,M_Warehouse_ID,X,Y,Z,PriorityNo,IsDefault) VALUES (");
		sqlCmd.append(M_Locator_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(M_Warehouse_ID).append(",0,0,0,50,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Locator NOT inserted");

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_BPartnerCashTrx_ID=").append(C_BPartner_ID);
		sqlCmd.append(",M_ProductFreight_ID=").append(M_Product_ID);
//		sqlCmd.append("C_UOM_Volume_ID=");
//		sqlCmd.append(",C_UOM_Weight_ID=");
//		sqlCmd.append(",C_UOM_Length_ID=");
//		sqlCmd.append(",C_UOM_Time_ID=");
		sqlCmd.append(" WHERE AD_Client_ID=").append(getAD_Client_ID());
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createEntities - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		/**
		 *  Other
		 */
		//  PriceList
		int M_PriceList_ID = getNextID(getAD_Client_ID(), "M_PriceList");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList ");
		sqlCmd.append("(M_PriceList_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID,IsDefault) VALUES (");
		sqlCmd.append(M_PriceList_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(",'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList NOT inserted");
		//  DiscountSchema
		int M_DiscountSchema_ID = getNextID(getAD_Client_ID(), "M_DiscountSchema");
		sqlCmd = new StringBuffer ("INSERT INTO M_DiscountSchema ");
		sqlCmd.append("(M_DiscountSchema_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,DiscountType, FlatDiscount) VALUES (");
		sqlCmd.append(M_DiscountSchema_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append("SysDate,'P',0)");	//	PriceList
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - DiscountSchema NOT inserted");
		//  PriceList Version
		int M_PriceList_Version_ID = getNextID(getAD_Client_ID(), "M_PriceList_Version");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList_Version ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,M_PriceList_ID,M_DiscountSchema_ID) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY-MM-DD'),SysDate,").append(M_PriceList_ID)
			.append(",").append(M_DiscountSchema_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList_Version NOT inserted");
		//  ProductPrice
		sqlCmd = new StringBuffer ("INSERT INTO M_ProductPrice ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("M_Product_ID,PriceList,PriceStd,PriceLimit) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(M_Product_ID).append(",1,1,1)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - ProductPrice NOT inserted");

		//  Location for Standard BP
		C_Location_ID = getNextID(getAD_Client_ID(), "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append(DB.TO_STRING(City)).append(",").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		int C_BPartner_Location_ID = getNextID(getAD_Client_ID(), "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(DB.TO_STRING(City)).append(",").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (Standard) NOT inserted");

		//	Create Sales Rep for Client-User
		C_BPartner_ID = getNextID(getAD_Client_ID(), "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_U_Name).append("','").append(AD_User_U_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_U_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (User) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_U_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (User) NOT updated");

		//  Location for Client-User
		C_Location_ID = getNextID(getAD_Client_ID(), "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append(DB.TO_STRING(City)).append(",").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		C_BPartner_Location_ID = getNextID(getAD_Client_ID(), "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(DB.TO_STRING(City)).append(",").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (User) NOT inserted");


		//	Create Sales Rep for Client-Admin
		C_BPartner_ID = getNextID(getAD_Client_ID(), "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_Name).append("','").append(AD_User_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (Admin) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (Admin) NOT updated");

		//  Location for Client-Admin
		C_Location_ID = getNextID(getAD_Client_ID(), "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append(DB.TO_STRING(City)).append(",").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		C_BPartner_Location_ID = getNextID(getAD_Client_ID(), "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(DB.TO_STRING(City)).append(",").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (Admin) NOT inserted");


		//  Payment Term
		int C_PaymentTerm_ID = getNextID(getAD_Client_ID(), "C_PaymentTerm");
		sqlCmd = new StringBuffer ("INSERT INTO C_PaymentTerm ");
		sqlCmd.append("(C_PaymentTerm_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,NetDays,GraceDays,DiscountDays,Discount,DiscountDays2,Discount2,IsDefault) VALUES (");
		sqlCmd.append(C_PaymentTerm_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'Immediate','Immediate',0,0,0,0,0,0,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PaymentTerm NOT inserted");

		//  Project Cycle
		C_Cycle_ID = getNextID(getAD_Client_ID(), "C_Cycle");
		sqlCmd = new StringBuffer ("INSERT INTO C_Cycle ");
		sqlCmd.append("(C_Cycle_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_Cycle_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Cycle NOT inserted");

		/**
		 *  Organization level data	===========================================
		 */

		//	Create Default Project
		int C_Project_ID = getNextID(getAD_Client_ID(), "C_Project");
		sqlCmd = new StringBuffer ("INSERT INTO C_Project ");
		sqlCmd.append("(C_Project_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Currency_ID,IsSummary) VALUES (");
		sqlCmd.append(C_Project_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Currency_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Project_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Project NOT inserted");
		//  Default Project
		if (m_hasProject)
		{
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Project_ID=").append(C_Project_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
			sqlCmd.append(" AND ElementType='PJ'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Project NOT updated");
		}

		//  CashBook
		int C_CashBook_ID = getNextID(getAD_Client_ID(), "C_CashBook");
		sqlCmd = new StringBuffer ("INSERT INTO C_CashBook ");
		sqlCmd.append("(C_CashBook_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_CashBook_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_CashBook_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - CashBook NOT inserted");

		return true;
	}   //  createEntities

	/**
	 *  Create Preference
	 *  @param Attribute attribute
	 *  @param Value value
	 *  @param AD_Window_ID window
	 */
	private void createPreference (String Attribute, String Value, int AD_Window_ID)
	{
		int AD_Preference_ID = getNextID(getAD_Client_ID(), "AD_Preference");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO AD_Preference ");
		sqlCmd.append("(AD_Preference_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Attribute,Value,AD_Window_ID) VALUES (");
		sqlCmd.append(AD_Preference_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(Attribute).append("','").append(Value).append("',");
		if (AD_Window_ID == 0)
			sqlCmd.append("NULL)");
		else
			sqlCmd.append(AD_Window_ID).append(")");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createPreference - Preference NOT inserted - " + Attribute);
	}   //  createPreference

	/**************************************************************************

	/**
	 * 	Get Next ID
	 * 	@param AD_Client_ID client
	 * 	@param TableName table name
	 */
	private int getNextID (int AD_Client_ID, String TableName)
	{
		return DB.getNextID(AD_Client_ID, TableName, null);
	}	//	getNextID

	/**
	 *  Get Client
	 *  @return AD_Client_ID
	 */
	public int getAD_Client_ID()
	{
		return m_client.getAD_Client_ID();
	}
	public int getAD_Org_ID()
	{
		return m_org.getAD_Org_ID();
	}
	public int getAD_User_ID()
	{
		return AD_User_ID;
	}
	public int getAD_Role_ID()
	{
		return AD_Role_ID;
	}
	public String getInfo()
	{
		return m_info.toString();
	}
}   //  MSetup
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
package org.compiere.model;

import java.util.*;
import java.sql.*;
import java.io.*;

import org.compiere.util.*;

/**
 * Initial Setup Model
 *
 * @author Jorg Janke
 * @version $Id: MSetup.java,v 1.27 2003/11/06 07:08:06 jjanke Exp $
 */
public final class MSetup
{
	/**
	 *  Constructor
	 *  @param ctx context
	 *  @param WindowNo window
	 */
	public MSetup(Properties ctx, int WindowNo)
	{
		m_ctx = ctx;
		m_lang = Env.getAD_Language(m_ctx);
		m_WindowNo = WindowNo;
	}   //  MSetup

	private Properties      m_ctx;
	private String          m_lang;
	private int             m_WindowNo;
	private StringBuffer    m_info;
	//
	private String          m_clientName;
	private String          m_orgName;
	//
	private String          m_stdColumns = "AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy";
	private String          m_stdValues;
	private String          m_stdValuesOrg;
	//
	private NaturalAccountMap m_nap = null;
	//
	private int             AD_Client_ID = 1000001;     //  initial Client_ID
	//
	private int     AD_Org_ID;
	private int     AD_User_ID;
	private String  AD_User_Name;
	private int     AD_User_U_ID;
	private String  AD_User_U_Name;
	private int     AD_Role_ID;
	private int     C_Calendar_ID;
	private int     C_AcctSchema_ID;
	private int     C_Currency_ID;
	private int     AD_Tree_Account_ID;
	private int     C_Cycle_ID;
	//
	private boolean         m_hasProject = false;
	private boolean         m_hasMCampaign = false;
	private boolean         m_hasSRegion = false;
	private AcctSchema      m_AcctSchema = null;


	/**
	 *  Create Client Info.
	 *  - Client, Trees, Org, Role, User, User_Role
	 *  @param clientName client name
	 *  @param orgName org name
	 *  @param userClient user id client
	 *  @param userOrg user id org
	 *  @return true if created
	 */
	public boolean createClient (String clientName, String orgName,
		String userClient, String userOrg)
	{
		Log.trace(Log.l3_Util, "MSetup.createClient");
		//  info header
		m_info = new StringBuffer();
		//  Standarc columns
		String name = null;
		String sql = null;
		int no = 0;

		/**
		 *  Create Client
		 */
		AD_Client_ID = DB.getKeyNextNo (AD_Client_ID, "AD_Client");
		Env.setContext(m_ctx, m_WindowNo, "AD_Client_ID", AD_Client_ID);

		//	Standard Values
		m_stdValues = String.valueOf(AD_Client_ID) + ",0,'Y',SysDate,0,SysDate,0";

		//	Create Client
		name = clientName;
		if (name == null || name.length() == 0)
			name = "newClient";
		name = name.trim();
		m_clientName = name;
		sql = "INSERT INTO AD_Client(" + m_stdColumns + ",Value,Name,Description)"
			+ " VALUES (" + m_stdValues + ",'" + name + "','" + name + "','" + name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Client NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info - Client
		m_info.append(Msg.translate(m_lang, "AD_Client_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Trees
		 */
		String sql2 = null;
		if (Env.isBaseLanguage(m_lang, "AD_Ref_List"))	//	Get TreeTypes & Name
			sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=120";
		else
			sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Reference_ID=120 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID";

		//  Tree IDs
		int AD_Tree_Org_ID=0, AD_Tree_BPartner_ID=0, AD_Tree_Project_ID=0,
			AD_Tree_SalesRegion_ID=0, AD_Tree_Product_ID=0;

		try
		{
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next())
			{
				String value = rs.getString(1);
				int AD_Tree_ID = 0;
				if (value.equals("OO"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Tree");
					AD_Tree_Org_ID = AD_Tree_ID;
				}
				else if (value.equals("BP"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Tree");
					AD_Tree_BPartner_ID = AD_Tree_ID;
				}
				else if (value.equals("PJ"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Tree");
					AD_Tree_Project_ID = AD_Tree_ID;
				}
				else if (value.equals("SR"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Tree");
					AD_Tree_SalesRegion_ID = AD_Tree_ID;
				}
				else if (value.equals("PR"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Tree");
					AD_Tree_Product_ID = AD_Tree_ID;
				}
				else if (value.endsWith("EV"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Tree");
					AD_Tree_Account_ID = AD_Tree_ID;
				}
				else if (value.equals("MM"))	//	No Menu
					;
				else
					//	PC (Product Category), BB (BOM), MC (Marketing Campaign), AY (Activity)
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Tree");

				//
				if (AD_Tree_ID != 0)
				{
					name = m_clientName + " " + rs.getString(2);
					sql = "INSERT INTO AD_Tree(" + m_stdColumns + ",AD_Tree_ID,Name,Description,TreeType)"
						+ " VALUES (" + m_stdValues + "," + AD_Tree_ID + ",'" + name + "','" + name + "','" + value + "')";
					no = DB.executeUpdate(sql);
					if (no == 1)
						m_info.append(Msg.translate(m_lang, "AD_Tree_ID")).append("=").append(name).append("\n");
					else
						Log.error("MSetup.createClient - Tree NOT created: " + name);
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e1)
		{
			Log.error ("MSetup.createClient - Trees", e1);
		}

		//	Get Primary Tree
		int AD_Tree_Menu_ID = 10;	//	hardcoded

		//	Create ClientInfo
		sql = "INSERT INTO AD_ClientInfo(" + m_stdColumns + ",Acct2_Active,Acct3_Active,"
			+ "AD_Tree_Menu_ID,AD_Tree_Org_ID,AD_Tree_BPartner_ID,AD_Tree_Project_ID,AD_Tree_SalesRegion_ID,AD_Tree_Product_ID)"
			+ " VALUES (" + m_stdValues + ",'N','N',"
			+ AD_Tree_Menu_ID + "," + AD_Tree_Org_ID + "," + AD_Tree_BPartner_ID + "," + AD_Tree_Project_ID + "," + AD_Tree_SalesRegion_ID + "," + AD_Tree_Product_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - ClientInfo NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
	//	m_info.append(Msg.translate(m_lang, "AD_ClientInfo")).append("\n");

		/**
		 *  Create Org
		 */
		AD_Org_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Org");
		m_stdValuesOrg = AD_Client_ID + "," + AD_Org_ID + ",'Y',SysDate,0,SysDate,0";

		name = orgName;
		if (name == null || name.length() == 0)
			name = "newOrg";
		m_orgName = name;
		sql = "INSERT INTO AD_Org (" + m_stdColumns + ",Value,Name,IsSummary)"
			+ " VALUES (" +	m_stdValuesOrg + ",'" + name + "','" + name + "','N')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Org NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_Org_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Roles
		 *  - Admin
		 *  - User
		 */
		name = m_clientName + " Admin";
		AD_Role_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Role");
		sql = "INSERT INTO AD_Role(" + m_stdColumns + ",AD_Role_ID,"
			+ "Name,Description,UserLevel,ClientList,OrgList)"
			+ " VALUES (" + m_stdValues + "," + AD_Role_ID + ","
			+ "'" + name + "','" + name + "',' CO','" + AD_Client_ID + "','0," + AD_Org_ID + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin Role A NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  OrgAccess x,0
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValues + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_OrgAccess 0 NOT created");
		//  OrgAccess x,y
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValuesOrg + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_OrgAccess NOT created");
		//  Info - Admin Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		//
		name = m_clientName + " User";
		int AD_Role_ID_U = DB.getKeyNextNo(AD_Client_ID, "AD_Role");
		sql = "INSERT INTO AD_Role(" + m_stdColumns + ",AD_Role_ID,"
			+ "Name,Description,UserLevel,ClientList,OrgList)"
			+ " VALUES (" + m_stdValues + "," + AD_Role_ID_U + ","
			+ "'" + name + "','" + name + "','  O','" + AD_Client_ID + "','" + AD_Org_ID + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - User Role A NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  OrgAccess x,y
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValuesOrg + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - User Role_OrgAccess NOT created");
		//  Info - Client Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Users
		 *  - Client
		 *  - Org
		 */
		name = userClient;
		if (name == null || name.length() == 0)
			name = m_clientName + "Client";
		AD_User_ID = DB.getKeyNextNo(AD_Client_ID, "AD_User");
		AD_User_Name = name;
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + ","
			+ "'" + AD_User_Name + "','" + AD_User_Name + "','" + AD_User_Name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin User NOT inserted - " + AD_User_Name;
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_Name).append("/").append(AD_User_Name).append("\n");

		name = userOrg;
		if (name == null || name.length() == 0)
			name = m_clientName + "Org";
		AD_User_U_ID = DB.getKeyNextNo(AD_Client_ID, "AD_User");
		AD_User_U_Name = name;
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + ","
			+ "'" + AD_User_U_Name + "','" + AD_User_U_Name + "','" + AD_User_U_Name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Org User NOT inserted - " + AD_User_U_Name;
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_U_Name).append("/").append(AD_User_U_Name).append("\n");

		/**
		 *  Create User-Role
		 */
		//  ClientUser          - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+Admin NOT inserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+User NOT inserted");
		//  OrgUser             - User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole OrgUser+Org NOT inserted");
		//  SuperUser(100)      - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + ",100," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole SuperUser+Admin NOT inserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + ",100," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole SuperUser+User NOT inserted");

		return true;
	}   //  createClient



	/*************************************************************************/

	/**
	 *  Create Accounting elements.
	 *  - Calendar
	 *  - Account Trees
	 *  - Account Values
	 *  - Accounting Schema
	 *  - Default Accounts
	 *
	 *  @param newC_Currency_ID currency
	 *  @param curName currency name
	 *  @param hasProduct has product segment
	 *  @param hasBPartner has bp segment
	 *  @param hasProject has project segment
	 *  @param hasMCampaign has campaign segment
	 *  @param hasSRegion has sales region segment
	 *  @param AccountingFile file name of accounting file
	 *  @return true if created
	 */
	public boolean createAccounting(int newC_Currency_ID, String curName,
		boolean hasProduct, boolean hasBPartner, boolean hasProject,
		boolean hasMCampaign, boolean hasSRegion,
		File AccountingFile)
	{
		Log.trace(Log.l3_Util, "MSetup.createAccount");
		//
		C_Currency_ID = newC_Currency_ID;
		m_hasProject = hasProject;
		m_hasMCampaign = hasMCampaign;
		m_hasSRegion = hasSRegion;

		//  Standard variables
		m_info = new StringBuffer();
		String name = null;
		StringBuffer sqlCmd = null;
		int no = 0;

		/**
		 *  Create Calendar
		 */
		C_Calendar_ID = DB.getKeyNextNo(AD_Client_ID, "C_Calendar");
		name = m_clientName + " " + Msg.translate(m_lang, "C_Calendar_ID");
		sqlCmd = new StringBuffer("INSERT INTO C_Calendar(");
		sqlCmd.append(m_stdColumns).append(",C_Calendar_ID,Name)").append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_Calendar_ID).append(",'").append(name).append("')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Calendar NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_Calendar_ID")).append("=").append(name).append("\n");

		//  Year
		int C_Year_ID = DB.getKeyNextNo(AD_Client_ID, "C_Year");
		sqlCmd = new StringBuffer ("INSERT INTO C_Year ");
		sqlCmd.append("(C_Year_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Year,C_Calendar_ID) VALUES (");
		sqlCmd.append(C_Year_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY'),").append(C_Calendar_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createAccounting - Year NOT inserted");
		/** @todo Create Periods */


		//	Create Account Elements
		int C_Element_ID = DB.getKeyNextNo(AD_Client_ID, "C_Element");
		name = m_clientName + " " + Msg.translate(m_lang, "Account_ID");
		sqlCmd = new StringBuffer ("INSERT INTO C_ELEMENT(");
		sqlCmd.append(m_stdColumns).append(",C_Element_ID,Name,Description,")
			.append("VFormat,ElementType,IsBalancing,IsNaturalAccount,AD_Tree_ID)").append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_Element_ID).append(",'").append(name).append("','").append(name).append("',")
			.append("NULL,'A','N','Y',").append(AD_Tree_Account_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Acct Element NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		m_info.append(Msg.translate(m_lang, "C_Element_ID")).append("=").append(name).append("\n");

		//	Create Account Values
		m_nap = new NaturalAccountMap(m_ctx);
		String errMsg = m_nap.parseFile(AccountingFile);
		if (errMsg.length() != 0)
		{
			Log.error(errMsg);
			m_info.append(errMsg);
			return false;
		}
		if (m_nap.createAccounts(AD_Client_ID, AD_Org_ID, C_Element_ID))
			m_info.append(Msg.translate(m_lang, "C_ElementValue_ID")).append(" # ").append(m_nap.size()).append("\n");
		else
		{
			String err = "MSetup.createAccounting - Acct Element Values NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		int C_ElementValue_ID = m_nap.getC_ElementValue_ID("DEFAULT_ACCT");
		Log.trace(Log.l4_Data, "C_ElementValue_ID=" + C_ElementValue_ID);

		/**
		 *  Create AccountingSchema
		 */
		C_AcctSchema_ID = DB.getKeyNextNo(AD_Client_ID, "C_AcctSchema");
		//
		String GAAP = "US";				//	AD_Reference_ID=123
		String CostingMethod = "A";		//	AD_Reference_ID=122
		name = m_clientName + " " + GAAP + "/" + CostingMethod + "/" + curName;
		//
		sqlCmd = new StringBuffer ("INSERT INTO C_AcctSchema(");
		sqlCmd.append(m_stdColumns).append(",C_AcctSchema_ID,Name,")
			.append("GAAP,IsAccrual,CostingMethod,C_Currency_ID,")
			.append("AutoPeriodControl,Separator,HasAlias,HasCombination)")
			.append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",'").append(name).append("',")
			.append("'").append(GAAP).append("','Y','").append(CostingMethod).append("',").append(C_Currency_ID).append(",")
			.append("'N','-','Y','N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - AcctSchema NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_AcctSchema_ID")).append("=").append(name).append("\n");

		/**
		 *  Create AccountingSchema Elements (Structure)
		 */
		String sql2 = null;
		if (Env.isBaseLanguage(m_lang, "AD_Reference"))	//	Get TreeTypes & Name
			sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=181";
		else
			sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Reference_ID=181 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID";
		//
		int Element_OO=0, Element_AC=0, Element_PR=0, Element_BP=0, Element_PJ=0,
			Element_MC=0, Element_SR=0;
		try
		{
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next())
			{
				String ElementType = rs.getString(1);
				name = rs.getString(2);
				//
				String IsMandatory = null;
				String IsBalanced = "N";
				int SeqNo = 0;
				int C_AcctSchema_Element_ID = 0;

				if (ElementType.equals("OO"))
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, "C_AcctSchema_Element");
					Element_OO = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					IsBalanced = "Y";
					SeqNo = 10;
				}
				else if (ElementType.equals("AC"))
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, "C_AcctSchema_Element");
					Element_AC = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					SeqNo = 20;
				}
				else if (ElementType.equals("PR") && hasProduct)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, "C_AcctSchema_Element");
					Element_PR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 30;
				}
				else if (ElementType.equals("BP") && hasBPartner)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, "C_AcctSchema_Element");
					Element_BP = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 40;
				}
				else if (ElementType.equals("PJ") && hasProject)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, "C_AcctSchema_Element");
					Element_PJ = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 50;
				}
				else if (ElementType.equals("MC") && hasMCampaign)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, "C_AcctSchema_Element");
					Element_MC = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 60;
				}
				else if (ElementType.equals("SR") && hasSRegion)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, "C_AcctSchema_Element");
					Element_SR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 70;
				}
				//	Not OT, LF, LT, U1, U2, AY

				if (IsMandatory != null)
				{
					sqlCmd = new StringBuffer ("INSERT INTO C_AcctSchema_Element(");
					sqlCmd.append(m_stdColumns).append(",C_AcctSchema_Element_ID,C_AcctSchema_ID,")
						.append("ElementType,Name,SeqNo,IsMandatory,IsBalanced) VALUES (");
					sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_Element_ID).append(",").append(C_AcctSchema_ID).append(",")
						.append("'").append(ElementType).append("','").append(name).append("',").append(SeqNo).append(",'")
						.append(IsMandatory).append("','").append(IsBalanced).append("')");
					no = DB.executeUpdate(sqlCmd.toString());
					if (no == 1)
						m_info.append(Msg.translate(m_lang, "C_AcctSchema_Element_ID")).append("=").append(name).append("\n");

					/** Default value for mandatory elements: OO and AC */
					if (ElementType.equals("OO"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET Org_ID=");
						sqlCmd.append(AD_Org_ID).append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Org in AcctSchamaElement NOT updated");
					}
					if (ElementType.equals("AC"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET C_ElementValue_ID=");
						sqlCmd.append(C_ElementValue_ID).append(", C_Element_ID=").append(C_Element_ID);
						sqlCmd.append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Account in AcctSchamaElement NOT updated");
					}
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e1)
		{
			Log.error ("MSetup.createAccounting - Elements", e1);
		}
		//  Create AcctSchema
		m_AcctSchema = new AcctSchema (C_AcctSchema_ID);


		//  Create GL Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_GL (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "USESUSPENSEBALANCING,SUSPENSEBALANCING_ACCT,"
			+ "USESUSPENSEERROR,SUSPENSEERROR_ACCT,"
			+ "USECURRENCYBALANCING,CURRENCYBALANCING_ACCT,"
			+ "RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,"
			+ "INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT,"
			+ "PPVOFFSET_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEBALANCING_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEERROR_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("CURRENCYBALANCING_ACCT")).append(",");
		//  RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,
		sqlCmd.append(getAcct("RETAINEDEARNING_ACCT")).append(",");
		sqlCmd.append(getAcct("INCOMESUMMARY_ACCT")).append(",");
		//  INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT)
		sqlCmd.append(getAcct("INTERCOMPANYDUETO_ACCT")).append(",");
		sqlCmd.append(getAcct("INTERCOMPANYDUEFROM_ACCT")).append(",");
		sqlCmd.append(getAcct("PPVOFFSET_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - GL Accounts NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//	Create Std Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_DEFAULT (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT, "
			+ "P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT, "
			+ "P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT, "
			+ "C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT, "
			+ "V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT, "
			+ "PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT, "
			+ "UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT, "
			+ "WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT, "
			+ "PJ_ASSET_ACCT,PJ_WIP_ACCT,"
			+ "T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT, "
			+ "B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,"
			+ "B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,"
			+ "B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT, "
			+ "CH_EXPENSE_ACCT,CH_REVENUE_ACCT, "
			+ "UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT, "
			+ "CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",");
		//  W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT
		sqlCmd.append(getAcct("W_INVENTORY_ACCT")).append(",");
		sqlCmd.append(getAcct("W_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("W_REVALUATION_ACCT")).append(",");
		sqlCmd.append(getAcct("W_INVACTUALADJUST_ACCT")).append(", ");
		//  P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT,
		sqlCmd.append(getAcct("P_REVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("P_COGS_ACCT")).append(", ");
		//  P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT,
		sqlCmd.append(getAcct("P_PURCHASEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_INVOICEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTREC_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTGRANT_ACCT")).append(", ");
		//  C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("C_RECEIVABLE_ACCT")).append(",");
		sqlCmd.append(getAcct("C_PREPAYMENT_ACCT")).append(", ");
		//  V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("V_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("V_LIABILITY_SERVICES_ACCT")).append(",");
		sqlCmd.append(getAcct("V_PREPAYMENT_ACCT")).append(", ");
		//  PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT,
		sqlCmd.append(getAcct("PAYDISCOUNT_EXP_ACCT")).append(",");
		sqlCmd.append(getAcct("PAYDISCOUNT_REV_ACCT")).append(",");
		sqlCmd.append(getAcct("WRITEOFF_ACCT")).append(", ");
		//  UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT,
		sqlCmd.append(getAcct("UNREALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("UNREALIZEDLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDLOSS_ACCT")).append(", ");
		//  WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT,
		sqlCmd.append(getAcct("WITHHOLDING_ACCT")).append(",");
		sqlCmd.append(getAcct("E_PREPAYMENT_ACCT")).append(",");
		sqlCmd.append(getAcct("E_EXPENSE_ACCT")).append(", ");
		//  PJ_ASSET_ACCT,PJ_WIP_ACCT,
		sqlCmd.append(getAcct("PJ_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("PJ_WIP_ACCT")).append(",");
		//  T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT,
		sqlCmd.append(getAcct("T_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("T_RECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("T_DUE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_CREDIT_ACCT")).append(", ");
		//  B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,
		sqlCmd.append(getAcct("B_INTRANSIT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("B_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTREV_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTEXP_ACCT")).append(",");
		//  B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,
		sqlCmd.append(getAcct("B_UNIDENTIFIED_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTLOSS_ACCT")).append(",");
		//  B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT,
		sqlCmd.append(getAcct("B_REVALUATIONGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_REVALUATIONLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("B_PAYMENTSELECT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_UNALLOCATEDCASH_ACCT")).append(", ");
		//  CH_EXPENSE_ACCT,CH_REVENUE_ACCT,
		sqlCmd.append(getAcct("CH_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CH_REVENUE_ACCT")).append(", ");
		//  UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT,
		sqlCmd.append(getAcct("UNEARNEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIPTS_ACCT")).append(", ");
		//  CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT)
		sqlCmd.append(getAcct("CB_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_CASHTRANSFER_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_RECEIPT_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Default Accounts NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//  GL Categories
		createGLCategory("Standard", "M", true);
		int GL_None = createGLCategory("None", "D", false);
		int GL_GL = createGLCategory("Manual", "M", false);
		int GL_ARI = createGLCategory("AR Invoice", "D", false);
		int GL_ARR = createGLCategory("AR Receipt", "D", false);
		int GL_MM = createGLCategory("Material Management", "D", false);
		int GL_API = createGLCategory("AP Invoice", "D", false);
		int GL_APP = createGLCategory("AP Payment", "D", false);

		//	Base DocumentTypes
		createDocType("GL Journal", "Journal", "GLJ", null, 0, 0, 1000, GL_GL);
		int DT_I = createDocType("AR Invoice", "Invoice", "ARI", null, 0, 0, 100000, GL_ARI);
		int DT_II = createDocType("AR Invoice Indirect", "Invoice", "ARI", null, 0, 0, 200000, GL_ARI);
		createDocType("AR Credit Memo", "Credit Memo", "ARC", null, 0, 0, 300000, GL_ARI);
		createDocType("AR Receipt", "Receipt", "ARR", null, 0, 0, 400000, GL_ARR);
		int DT_S  = createDocType("MM Shipment", "Delivery Note", "MMS", null, 0, 0, 500000, GL_MM);
		int DT_SI = createDocType("MM Shipment Indirect", "Delivery Note", "MMS", null, 0, 0, 600000, GL_MM);
		createDocType("MM Receipt", "Vendor Delivery", "MMR", null, 0, 0, 0, GL_MM);
		createDocType("AP Invoice", "Vendor Invoice", "API", null, 0, 0, 0, GL_API);
		createDocType("AP CreditMemo", "Vendor Credit Memo", "APC", null, 0, 0, 0, GL_API);
		createDocType("AP Payment", "Vendor Payment", "APP", null, 0, 0, 700000, GL_APP);
		createDocType("Purchase Order", "Purchase Order", "POO", null, 0, 0, 800000, GL_None);
		createDocType("Purchase Requisition", "Purchase Requisition", "POR", null, 0, 0, 900000, GL_None);

		//  Order Entry
		createDocType("Quotation", "Binding offer", "SOO", "OB", 0, 0, 10000, GL_None);
		createDocType("Proposal", "Non binding offer", "SOO", "ON", 0, 0, 20000, GL_None);
		createDocType("Prepay Order", "Prepay Order", "SOO", "PR", DT_S, DT_I, 30000, GL_None);
		createDocType("Return Material", "Return Material Authorization", "SOO", "RM", DT_S, DT_I, 40000, GL_None);
		createDocType("Standard Order", "Order Confirmation", "SOO", "SO", DT_S, DT_I, 50000, GL_None);
		createDocType("Credit Order", "Order Confirmation", "SOO", "WI", DT_SI, DT_I, 60000, GL_None);   //  RE
		createDocType("Warehouse Order", "Order Confirmation", "SOO", "WP", DT_S, DT_I, 70000, GL_None);    //  LS
		int DT = createDocType("POS Order", "Order Confirmation", "SOO", "WR", DT_SI, DT_II, 80000, GL_None);    // Bar
		createPreference("C_DocTypeTarget_ID", String.valueOf(DT), 143);

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_AcctSchema1_ID=").append(C_AcctSchema_ID)
			.append(", C_Calendar_ID=").append(C_Calendar_ID)
			.append(" WHERE AD_Client_ID=").append(AD_Client_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//
		return true;
	}   //  createAccounting

	/**
	 *  Get Account ID for key
	 *  @param key key
	 *  @return C_ValidCombination_ID
	 */
	private int getAcct (String key)
	{
		Log.trace(Log.l4_Data, "MSetup.getAcct - " + key);
		//  Element
		int C_ElementValue_ID = m_nap.getC_ElementValue_ID(key);
		Account vc = Account.getDefault(m_AcctSchema, true);	//	optional null
		vc.setAccount_ID(C_ElementValue_ID);
		vc.save(AD_Client_ID, 0);
		int C_ValidCombination_ID = vc.getC_ValidCombination_ID();

		//  Error
		if (C_ValidCombination_ID == 0)
			Log.error("MSetup.getAcct - no account for " + key);
		return C_ValidCombination_ID;
	}   //  getAcct

	/**
	 *  Create GL Category
	 *  @param Name name
	 *  @param CategoryType category type
	 *  @param isDefault is default value
	 *  @return GL_Category_ID
	 */
	private int createGLCategory (String Name, String CategoryType, boolean isDefault)
	{
		int GL_Category_ID = DB.getKeyNextNo(AD_Client_ID, "GL_Category");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO GL_Category ");
		sqlCmd.append("(GL_Category_ID,").append(m_stdColumns).append(",")
			.append("Name,CategoryType,IsDefault) VALUES (")
			.append(GL_Category_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(Name).append("','").append(CategoryType).append("','")
			.append(isDefault ? "Y" : "N").append("')");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createGLCategory - GL Category NOT created - " + Name);
		//
		return GL_Category_ID;
	}   //  createGLCategory

	/**
	 *  Create Document Types with Sequence
	 *  @param Name name
	 *  @param PrintName print name
	 *  @param DocBaseType document base type
	 *  @param DocSubTypeSO sales order sub type
	 *  @param C_DocTypeShipment_ID shipment doc
	 *  @param C_DocTypeInvoice_ID invoice doc
	 *  @param StartNo start doc no
	 *  @param GL_Category_ID gl category
	 *  @return C_DocType_ID doc type
	 */
	private int createDocType (String Name, String PrintName,
		String DocBaseType, String DocSubTypeSO,
		int C_DocTypeShipment_ID, int C_DocTypeInvoice_ID,
		int StartNo, int GL_Category_ID)
	{
		StringBuffer sqlCmd = null;
		//  Get Sequence
		int AD_Sequence_ID = 0;
		if (StartNo != 0)   //  manual sequenec, if startNo == 0
		{
			AD_Sequence_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Sequence");
			sqlCmd = new StringBuffer ("INSERT INTO AD_Sequence ");
			sqlCmd.append("(AD_Sequence_ID,").append(m_stdColumns).append(",");
			sqlCmd.append("Name,IsAutoSequence,IncrementNo,StartNo,CurrentNext,");
			sqlCmd.append("CurrentNextSys,IsTableID) VALUES (");
			sqlCmd.append(AD_Sequence_ID).append(",").append(m_stdValues).append(",");
			sqlCmd.append("'").append(Name).append("','Y',1,").append(StartNo).append(",").append(StartNo).append(",");
			sqlCmd.append(StartNo/10).append(",'N')");
			int no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createDocType - Sequence NOT created - " + Name);
		}

		//  Get Document Type
		int C_DocType_ID = DB.getKeyNextNo(AD_Client_ID, "C_DocType");
		sqlCmd = new StringBuffer ("INSERT INTO	C_DocType ");
		sqlCmd.append("(C_DocType_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,PrintName,DocBaseType,DocSubTypeSO,");
		sqlCmd.append("C_DocTypeShipment_ID,C_DocTypeInvoice_ID,");
		sqlCmd.append("IsDocNoControlled,DocNoSequence_ID,GL_Category_ID,");
		sqlCmd.append("IsPrinted,IsTransferred,DocumentCopies,IsSOTrx)");
		//
		sqlCmd.append(" VALUES (").append(C_DocType_ID).append(",").append(m_stdValues).append(",");
		//  Name,PrintName,DocBaseType,DocSubTypeSO,
		sqlCmd.append("'").append(Name).append("','").append(PrintName).append("','").append(DocBaseType).append("',");
		if (DocSubTypeSO == null || DocSubTypeSO.length() == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append("'").append(DocSubTypeSO).append("',");
		//  C_DocTypeShipment_ID,C_DocTypeInvoice_ID,
		if (C_DocTypeShipment_ID == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append(C_DocTypeShipment_ID).append(",");
		if (C_DocTypeInvoice_ID == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append(C_DocTypeInvoice_ID).append(",");
		//  IsDocNoControlled,DocNoSequence_ID,GL_Category_ID,
		if (AD_Sequence_ID == 0)
			sqlCmd.append("'N',NULL,");
		else
			sqlCmd.append("'Y',").append(AD_Sequence_ID).append(",");
		sqlCmd.append(GL_Category_ID).append(",");
		//  IsPrinted,IsTransferred,DocumentCopies,IsSOTrx)
		sqlCmd.append("'Y',");
		if (DocBaseType.equals("SOO"))
			sqlCmd.append("'N',");
		else
			sqlCmd.append("'Y',");
		sqlCmd.append("0,");
		if (DocBaseType.startsWith("AR") || DocBaseType.equals("MMS") || DocBaseType.equals("SOO"))
			sqlCmd.append("'Y'");
		else
			sqlCmd.append("'N'");
		sqlCmd.append(")");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createDocType - DocType NOT created - " + Name);
		//
		return C_DocType_ID;
	}   //  createDocType

	/*************************************************************************/

	/**
	 *  Create Default main entities.
	 *  - Dimensions & BPGroup, Prod Category)
	 *  - Location, Locator, Warehouse
	 *  - PriceList
	 *  - Cashbook, PaymentTerm
	 *  @param C_Country_ID country
	 *  @param City city
	 *  @param C_Region_ID region
	 *  @return true if created
	 */
	public boolean createEntities (int C_Country_ID, String City, int C_Region_ID)
	{
		Log.trace(Log.l3_Util, "MSetup.createEntries", "C_Country_ID=" + C_Country_ID + ", City=" + City + ", C_Region_ID=" + C_Region_ID);
		m_info.append("\n----\n");
		//
		String defaultName = Msg.translate(m_lang, "Standard");
		String defaultEntry = "'" + defaultName + "',";
		StringBuffer sqlCmd = null;
		int no = 0;

		//	Create Marketing Channel/Campaign
		int C_Channel_ID = DB.getKeyNextNo(AD_Client_ID, "C_Channel");
		sqlCmd = new StringBuffer("INSERT INTO C_Channel ");
		sqlCmd.append("(C_Channel_ID,Name,");
		sqlCmd.append(m_stdColumns).append(") VALUES (");
		sqlCmd.append(C_Channel_ID).append(",").append(defaultEntry);
		sqlCmd.append(m_stdValues).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Channel NOT inserted");
		int C_Campaign_ID = DB.getKeyNextNo(AD_Client_ID, "C_Campaign");
		sqlCmd = new StringBuffer("INSERT INTO C_Campaign ");
		sqlCmd.append("(C_Campaign_ID,C_Channel_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,Costs) VALUES (");
		sqlCmd.append(C_Campaign_ID).append(",").append(C_Channel_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Campaign_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Campaign NOT inserted");
		if (m_hasMCampaign)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Campaign_ID=").append(C_Campaign_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='MC'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Campaign NOT updated");
		}

		//	Create Sales Region
		int C_SalesRegion_ID = DB.getKeyNextNo(AD_Client_ID, "C_SalesRegion");
		sqlCmd = new StringBuffer ("INSERT INTO C_SalesRegion ");
		sqlCmd.append("(C_SalesRegion_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsSummary) VALUES (");
		sqlCmd.append(C_SalesRegion_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_SalesRegion_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRegion NOT inserted");
		if (m_hasSRegion)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_SalesRegion_ID=").append(C_SalesRegion_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='SR'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement SalesRegion NOT updated");
		}

		/**
		 *  Business Partner
		 */
		//  Create BP Group
		int C_BP_Group_ID = DB.getKeyNextNo(AD_Client_ID, "C_BP_Group");
		sqlCmd = new StringBuffer ("INSERT INTO C_BP_Group ");
		sqlCmd.append("(C_BP_Group_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault) VALUES ( ");
		sqlCmd.append(C_BP_Group_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BP_Group_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BP Group NOT inserted");

		//	Create BPartner
		int C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_BP_Group_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BPartner_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BPartner NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("C_BPartner_ID=").append(C_BPartner_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
		sqlCmd.append(" AND ElementType='BP'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element BPartner NOT updated");
		createPreference("C_BPartner_ID", String.valueOf(C_BPartner_ID), 143);

		/**
		 *  Product
		 */
		//  Create Product Category
		int M_Product_Category_ID = DB.getKeyNextNo(AD_Client_ID, "M_Product_Category");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product_Category ");
		sqlCmd.append("(M_Product_Category_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault,PlannedMargin) VALUES (");
		sqlCmd.append(M_Product_Category_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y',0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_Category_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product Category NOT inserted");

		//  UOM (EA)
		int C_UOM_ID = 100;

		//  TaxCategory
		int C_TaxCategory_ID = DB.getKeyNextNo(AD_Client_ID, "C_TaxCategory");
		sqlCmd = new StringBuffer ("INSERT INTO C_TaxCategory ");
		sqlCmd.append("(C_TaxCategory_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,IsDefault) VALUES (");
		sqlCmd.append(C_TaxCategory_ID).append(",").append(m_stdValues).append(", ");
		if (C_Country_ID == 100)    // US
			sqlCmd.append("'Sales Tax','Y')");
		else
			sqlCmd.append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - TaxCategory NOT inserted");

		//  Tax - Zero Rate
	//	MTax tax = new MTax (m_ctx, "Standard", Env.ZERO, C_TaxCategory_ID);
		int C_Tax_ID = DB.getKeyNextNo(AD_Client_ID, "C_Tax");
		sqlCmd = new StringBuffer ("INSERT INTO C_Tax ");
		sqlCmd.append("(C_Tax_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,Rate,C_TaxCategory_ID,C_Country_ID,TO_Country_ID,ValidFrom,SoPoType,IsDefault) VALUES (");
		sqlCmd.append(C_Tax_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append("0,").append(C_TaxCategory_ID).append(",")
			.append(C_Country_ID).append(",").append(C_Country_ID)
			.append(",TO_DATE('1990-01-01','YYYY-MM-DD'),'B','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Tax_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Tax NOT inserted");

		//	Create Product
		int M_Product_ID = DB.getKeyNextNo(AD_Client_ID, "M_Product");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product ");
		sqlCmd.append("(M_Product_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_UOM_ID,M_Product_Category_ID,C_TaxCategory_ID) VALUES (");
		sqlCmd.append(M_Product_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry);
		sqlCmd.append(C_UOM_ID).append(",").append(M_Product_Category_ID).append(",");
		sqlCmd.append(C_TaxCategory_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("M_Product_ID=").append(M_Product_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
		sqlCmd.append(" AND ElementType='PR'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element Product NOT updated");

		/**
		 *  Warehouse
		 */
		//  Location (Company)
		int C_Location_ID = DB.getKeyNextNo(AD_Client_ID, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		sqlCmd = new StringBuffer ("UPDATE AD_OrgInfo SET C_Location_ID=");
		sqlCmd.append(C_Location_ID).append(" WHERE AD_Org_ID=").append(AD_Org_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Location NOT inserted");
		createPreference("C_Country_ID", String.valueOf(C_Country_ID), 0);

		//  Default Warehouse
		int M_Warehouse_ID = DB.getKeyNextNo(AD_Client_ID, "M_Warehouse");
		sqlCmd = new StringBuffer ("INSERT INTO M_Warehouse ");
		sqlCmd.append("(M_Warehouse_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Location_ID,Separator) VALUES (");
		sqlCmd.append(M_Warehouse_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Location_ID).append(",'-')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Warehouse NOT inserted");

		//   Locator
		int M_Locator_ID = DB.getKeyNextNo(AD_Client_ID, "M_Locator");
		sqlCmd = new StringBuffer ("INSERT INTO M_Locator ");
		sqlCmd.append("(M_Locator_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,M_Warehouse_ID,X,Y,Z,PriorityNo,IsDefault) VALUES (");
		sqlCmd.append(M_Locator_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(M_Warehouse_ID).append(",0,0,0,50,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Locator NOT inserted");

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_BPartnerCashTrx_ID=").append(C_BPartner_ID);
		sqlCmd.append(",M_ProductFreight_ID=").append(M_Product_ID);
//		sqlCmd.append("C_UOM_Volume_ID=");
//		sqlCmd.append(",C_UOM_Weight_ID=");
//		sqlCmd.append(",C_UOM_Length_ID=");
//		sqlCmd.append(",C_UOM_Time_ID=");
		sqlCmd.append(" WHERE AD_Client_ID=").append(AD_Client_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createEntities - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		/**
		 *  Other
		 */
		//  PriceList
		int M_PriceList_ID = DB.getKeyNextNo(AD_Client_ID, "M_PriceList");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList ");
		sqlCmd.append("(M_PriceList_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID,IsDefault) VALUES (");
		sqlCmd.append(M_PriceList_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(",'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList NOT inserted");
		//  DiscountSchema
		int M_DiscountSchema_ID = DB.getKeyNextNo(AD_Client_ID, "M_DiscountSchema");
		sqlCmd = new StringBuffer ("INSERT INTO M_DiscountSchema ");
		sqlCmd.append("(M_DiscountSchema_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,DiscountType, FlatDiscount) VALUES (");
		sqlCmd.append(M_DiscountSchema_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append("SysDate,'P',0)");	//	PriceList
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - DiscountSchema NOT inserted");
		//  PriceList Version
		int M_PriceList_Version_ID = DB.getKeyNextNo(AD_Client_ID, "M_PriceList_Version");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList_Version ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,M_PriceList_ID,M_DiscountSchema_ID) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY-MM-DD'),SysDate,").append(M_PriceList_ID)
			.append(",").append(M_DiscountSchema_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList_Version NOT inserted");
		//  ProductPrice
		sqlCmd = new StringBuffer ("INSERT INTO M_ProductPrice ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("M_Product_ID,PriceList,PriceStd,PriceLimit) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(M_Product_ID).append(",1,1,1)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - ProductPrice NOT inserted");

		//  Location for Standard BP
		C_Location_ID = DB.getKeyNextNo(AD_Client_ID, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		int C_BPartner_Location_ID = DB.getKeyNextNo(AD_Client_ID, "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(City).append("',").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (Standard) NOT inserted");

		//	Create Sales Rep for Client-User
		C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_U_Name).append("','").append(AD_User_U_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_U_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (User) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_U_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (User) NOT updated");

		//  Location for Client-User
		C_Location_ID = DB.getKeyNextNo(AD_Client_ID, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		C_BPartner_Location_ID = DB.getKeyNextNo(AD_Client_ID, "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(City).append("',").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (User) NOT inserted");


		//	Create Sales Rep for Client-Admin
		C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_Name).append("','").append(AD_User_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (Admin) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (Admin) NOT updated");

		//  Location for Client-Admin
		C_Location_ID = DB.getKeyNextNo(AD_Client_ID, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		C_BPartner_Location_ID = DB.getKeyNextNo(AD_Client_ID, "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(City).append("',").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (Admin) NOT inserted");


		//  Payment Term
		int C_PaymentTerm_ID = DB.getKeyNextNo(AD_Client_ID, "C_PaymentTerm");
		sqlCmd = new StringBuffer ("INSERT INTO C_PaymentTerm ");
		sqlCmd.append("(C_PaymentTerm_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,NetDays,GraceDays,DiscountDays,Discount,DiscountDays2,Discount2,IsDefault) VALUES (");
		sqlCmd.append(C_PaymentTerm_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'Immediate','Immediate',0,0,0,0,0,0,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PaymentTerm NOT inserted");

		//  Project Cycle
		C_Cycle_ID = DB.getKeyNextNo(AD_Client_ID, "C_Cycle");
		sqlCmd = new StringBuffer ("INSERT INTO C_Cycle ");
		sqlCmd.append("(C_Cycle_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_Cycle_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Cycle NOT inserted");

		/**
		 *  Organization level data	===========================================
		 */

		//	Create Default Project
		int C_Project_ID = DB.getKeyNextNo(AD_Client_ID, "C_Project");
		sqlCmd = new StringBuffer ("INSERT INTO C_Project ");
		sqlCmd.append("(C_Project_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Currency_ID,IsSummary) VALUES (");
		sqlCmd.append(C_Project_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Currency_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Project_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Project NOT inserted");
		//  Default Project
		if (m_hasProject)
		{
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Project_ID=").append(C_Project_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='PJ'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Project NOT updated");
		}

		//  CashBook
		int C_CashBook_ID = DB.getKeyNextNo(AD_Client_ID, "C_CashBook");
		sqlCmd = new StringBuffer ("INSERT INTO C_CashBook ");
		sqlCmd.append("(C_CashBook_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_CashBook_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_CashBook_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - CashBook NOT inserted");

		return true;
	}   //  createEntities

	/**
	 *  Create Preference
	 *  @param Attribute attribute
	 *  @param Value value
	 *  @param AD_Window_ID window
	 */
	private void createPreference (String Attribute, String Value, int AD_Window_ID)
	{
		int AD_Preference_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Preference");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO AD_Preference ");
		sqlCmd.append("(AD_Preference_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Attribute,Value,AD_Window_ID) VALUES (");
		sqlCmd.append(AD_Preference_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(Attribute).append("','").append(Value).append("',");
		if (AD_Window_ID == 0)
			sqlCmd.append("NULL)");
		else
			sqlCmd.append(AD_Window_ID).append(")");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createPreference - Preference NOT inserted - " + Attribute);
	}   //  createPreference

	/*************************************************************************/

	/**
	 *  Get Client
	 *  @return AD_Client_ID
	 */
	public int getAD_Client_ID()
	{
		return AD_Client_ID;
	}
	public int getAD_Org_ID()
	{
		return AD_Org_ID;
	}
	public int getAD_User_ID()
	{
		return AD_User_ID;
	}
	public int getAD_Role_ID()
	{
		return AD_Role_ID;
	}
	public int getC_AcctSchema_ID()
	{
		return C_AcctSchema_ID;
	}
	public String getInfo()
	{
		return m_info.toString();
	}
}   //  MSetup
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
package org.compiere.model;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.compiere.util.*;

/**
 * Initial Setup Model
 *
 * @author Jorg Janke
 * @version $Id: MSetup.java,v 1.41 2004/09/09 14:14:32 jjanke Exp $
 */
public final class MSetup
{
	/**
	 *  Constructor
	 *  @param ctx context
	 *  @param WindowNo window
	 */
	public MSetup(Properties ctx, int WindowNo)
	{
		m_ctx = new Properties(ctx);	//	copy
		m_lang = Env.getAD_Language(m_ctx);
		m_WindowNo = WindowNo;
	}   //  MSetup

	private Properties      m_ctx;
	private String          m_lang;
	private int             m_WindowNo;
	private StringBuffer    m_info;
	//
	private String          m_clientName;
//	private String          m_orgName;
	//
	private String          m_stdColumns = "AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy";
	private String          m_stdValues;
	private String          m_stdValuesOrg;
	//
	private NaturalAccountMap m_nap = null;
	//
	private MClient			m_client;
	private MOrg			m_org;
	private MAcctSchema		m_as;
	//
	private int     		AD_User_ID;
	private String  		AD_User_Name;
	private int     		AD_User_U_ID;
	private String  		AD_User_U_Name;
	private int     		C_Calendar_ID;
	private int     		m_AD_Tree_Account_ID;
	private int     		C_Cycle_ID;
	//
	private boolean         m_hasProject = false;
	private boolean         m_hasMCampaign = false;
	private boolean         m_hasSRegion = false;


	/**
	 *  Create Client Info.
	 *  - Client, Trees, Org, Role, User, User_Role
	 *  @param clientName client name
	 *  @param orgName org name
	 *  @param userClient user id client
	 *  @param userOrg user id org
	 *  @return true if created
	 */
	public boolean createClient (String clientName, String orgName,
		String userClient, String userOrg)
	{
		Log.trace(Log.l3_Util, "MSetup.createClient - " + clientName);
		//  info header
		m_info = new StringBuffer();
		//  Standarc columns
		String name = null;
		String sql = null;
		int no = 0;

		/**
		 *  Create Client
		 */
		name = clientName;
		if (name == null || name.length() == 0)
			name = "newClient";
		m_clientName = name;
		m_client = new MClient(m_ctx, 0, true);
		m_client.setValue(m_clientName);
		m_client.setName(m_clientName);
		if (!m_client.save())
		{
			String err = "MSetup.createClient - Client NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		int AD_Client_ID = m_client.getAD_Client_ID();
		Env.setContext(m_ctx, m_WindowNo, "AD_Client_ID", AD_Client_ID);
		Env.setContext(m_ctx, "#AD_Client_ID", AD_Client_ID);

		//	Standard Values
		m_stdValues = String.valueOf(AD_Client_ID) + ",0,'Y',SysDate,0,SysDate,0";
		//  Info - Client
		m_info.append(Msg.translate(m_lang, "AD_Client_ID")).append("=").append(name).append("\n");

		//	Setup Sequences
		if (!MSequence.checkClientSequences (m_ctx, AD_Client_ID))
		{
			String err = "MSetup.createClient - Sequences NOT created";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		
		//  Trees and Client Info
		if (!m_client.setupClientInfo(m_lang))
		{
			String err = "MSetup.createClient - ClientInfo NOT created";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		m_AD_Tree_Account_ID = m_client.getSetup_AD_Tree_Account_ID();

		/**
		 *  Create Org
		 */
		name = orgName;
		if (name == null || name.length() == 0)
			name = "newOrg";
		m_org = new MOrg (m_client, name);
		if (!m_org.save())
		{
			String err = "MSetup.createClient - Org NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		Env.setContext(m_ctx, m_WindowNo, "AD_Org_ID", getAD_Org_ID());
		Env.setContext(m_ctx, "#AD_Org_ID", getAD_Org_ID());
		m_stdValuesOrg = AD_Client_ID + "," + getAD_Org_ID() + ",'Y',SysDate,0,SysDate,0";
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_Org_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Roles
		 *  - Admin
		 *  - User
		 */
		name = m_clientName + " Admin";
		MRole admin = new MRole(m_ctx, 0);
		admin.setClientOrg(m_client);
		admin.setName(name);
		admin.setUserLevel(MRole.USERLEVEL_ClientPlusOrganization);
		admin.setIsShowAcct(true);
		if (!admin.save())
		{
			String err = "MSetup.createClient - Admin Role A NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//	OrgAccess x, 0
		MRoleOrgAccess adminClientAccess = new MRoleOrgAccess (admin, 0);
		if (!adminClientAccess.save())
			Log.error("MSetup.createClient - Admin Role_OrgAccess 0 NOT created");
		//  OrgAccess x,y
		MRoleOrgAccess adminOrgAccess = new MRoleOrgAccess (admin, m_org.getAD_Org_ID());
		if (!adminOrgAccess.save())
			Log.error("MSetup.createClient - Admin Role_OrgAccess NOT created");
		
		//  Info - Admin Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		//
		name = m_clientName + " User";
		MRole user = new MRole (m_ctx, 0);
		user.setClientOrg(m_client);
		user.setName(name);
		if (!user.save())
		{
			String err = "MSetup.createClient - User Role A NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  OrgAccess x,y
		MRoleOrgAccess userOrgAccess = new MRoleOrgAccess (user, m_org.getAD_Org_ID());
		if (!userOrgAccess.save())
			Log.error("MSetup.createClient - User Role_OrgAccess NOT created");
		
		//  Info - Client Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Users
		 *  - Client
		 *  - Org
		 */
		name = userClient;
		if (name == null || name.length() == 0)
			name = m_clientName + "Client";
		AD_User_ID = getNextID(AD_Client_ID, "AD_User");
		AD_User_Name = name;
		name = DB.TO_STRING(name);
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + ","
			+ name + "," + name + "," + name + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin User NOT inserted - " + AD_User_Name;
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_Name).append("/").append(AD_User_Name).append("\n");

		name = userOrg;
		if (name == null || name.length() == 0)
			name = m_clientName + "Org";
		AD_User_U_ID = getNextID(AD_Client_ID, "AD_User");
		AD_User_U_Name = name;
		name = DB.TO_STRING(name);
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + ","
			+ name + "," + name + "," + name + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Org User NOT inserted - " + AD_User_U_Name;
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_U_Name).append("/").append(AD_User_U_Name).append("\n");

		/**
		 *  Create User-Role
		 */
		//  ClientUser          - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + admin.getAD_Role_ID() + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+Admin NOT inserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + user.getAD_Role_ID() + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+User NOT inserted");
		//  OrgUser             - User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + "," + user.getAD_Role_ID() + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole OrgUser+Org NOT inserted");

		//	Processors
		MAcctProcessor ap = new MAcctProcessor(m_client, AD_User_ID);
		ap.save();
		
		MRequestProcessor rp = new MRequestProcessor (m_client, AD_User_ID);
		rp.save();
		
		return true;
	}   //  createClient



	/**************************************************************************
	 *  Create Accounting elements.
	 *  - Calendar
	 *  - Account Trees
	 *  - Account Values
	 *  - Accounting Schema
	 *  - Default Accounts
	 *
	 *  @param currency currency
	 *  @param hasProduct has product segment
	 *  @param hasBPartner has bp segment
	 *  @param hasProject has project segment
	 *  @param hasMCampaign has campaign segment
	 *  @param hasSRegion has sales region segment
	 *  @param AccountingFile file name of accounting file
	 *  @return true if created
	 */
	public boolean createAccounting(KeyNamePair currency,
		boolean hasProduct, boolean hasBPartner, boolean hasProject,
		boolean hasMCampaign, boolean hasSRegion,
		File AccountingFile)
	{
		Log.trace(Log.l3_Util, "MSetup.createAccounting - " + m_client);
		//
		m_hasProject = hasProject;
		m_hasMCampaign = hasMCampaign;
		m_hasSRegion = hasSRegion;

		//  Standard variables
		m_info = new StringBuffer();
		String name = null;
		StringBuffer sqlCmd = null;
		int no = 0;

		/**
		 *  Create Calendar
		 */
		C_Calendar_ID = getNextID(m_client.getAD_Client_ID(), "C_Calendar");
		name = m_clientName + " " + Msg.translate(m_lang, "C_Calendar_ID");
		name = DB.TO_STRING(name);
		sqlCmd = new StringBuffer("INSERT INTO C_Calendar(");
		sqlCmd.append(m_stdColumns).append(",C_Calendar_ID,Name)").append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_Calendar_ID).append(",").append(name).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Calendar NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_Calendar_ID")).append("=").append(name).append("\n");

		//  Year
		int C_Year_ID = getNextID(m_client.getAD_Client_ID(), "C_Year");
		sqlCmd = new StringBuffer ("INSERT INTO C_Year ");
		sqlCmd.append("(C_Year_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Year,C_Calendar_ID) VALUES (");
		sqlCmd.append(C_Year_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY'),").append(C_Calendar_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createAccounting - Year NOT inserted");
		//	TODO Create Periods


		//	Create Account Elements
		name = m_clientName + " " + Msg.translate(m_lang, "Account_ID");
		MElement element = new MElement (m_client, name, 
			MElement.ELEMENTTYPE_Account, m_AD_Tree_Account_ID);
		if (!element.save())
		{
			String err = "MSetup.createAccounting - Acct Element NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		int C_Element_ID = element.getC_Element_ID();
		m_info.append(Msg.translate(m_lang, "C_Element_ID")).append("=").append(name).append("\n");

		//	Create Account Values
		m_nap = new NaturalAccountMap(m_ctx);
		String errMsg = m_nap.parseFile(AccountingFile);
		if (errMsg.length() != 0)
		{
			Log.error(errMsg);
			m_info.append(errMsg);
			return false;
		}
		if (m_nap.saveAccounts(getAD_Client_ID(), getAD_Org_ID(), C_Element_ID))
			m_info.append(Msg.translate(m_lang, "C_ElementValue_ID")).append(" # ").append(m_nap.size()).append("\n");
		else
		{
			String err = "MSetup.createAccounting - Acct Element Values NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		int C_ElementValue_ID = m_nap.getC_ElementValue_ID("DEFAULT_ACCT");
		Log.trace(Log.l4_Data, "C_ElementValue_ID=" + C_ElementValue_ID);

		/**
		 *  Create AccountingSchema
		 */
		m_as = new MAcctSchema (m_client, currency);
		if (!m_as.save())
		{
			String err = "MSetup.createAccounting - AcctSchema NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_AcctSchema_ID")).append("=").append(m_as.getName()).append("\n");

		/**
		 *  Create AccountingSchema Elements (Structure)
		 */
		String sql2 = null;
		if (Env.isBaseLanguage(m_lang, "AD_Reference"))	//	Get ElementTypes & Name
			sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=181";
		else
			sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Reference_ID=181 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID";
		//
		int Element_OO=0, Element_AC=0, Element_PR=0, Element_BP=0, Element_PJ=0,
			Element_MC=0, Element_SR=0;
		try
		{
			int AD_Client_ID = m_client.getAD_Client_ID();
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next())
			{
				String ElementType = rs.getString(1);
				name = rs.getString(2);
				//
				String IsMandatory = null;
				String IsBalanced = "N";
				int SeqNo = 0;
				int C_AcctSchema_Element_ID = 0;

				if (ElementType.equals("OO"))
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_OO = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					IsBalanced = "Y";
					SeqNo = 10;
				}
				else if (ElementType.equals("AC"))
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_AC = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					SeqNo = 20;
				}
				else if (ElementType.equals("PR") && hasProduct)
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_PR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 30;
				}
				else if (ElementType.equals("BP") && hasBPartner)
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_BP = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 40;
				}
				else if (ElementType.equals("PJ") && hasProject)
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_PJ = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 50;
				}
				else if (ElementType.equals("MC") && hasMCampaign)
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_MC = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 60;
				}
				else if (ElementType.equals("SR") && hasSRegion)
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_SR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 70;
				}
				//	Not OT, LF, LT, U1, U2, AY

				if (IsMandatory != null)
				{
					sqlCmd = new StringBuffer ("INSERT INTO C_AcctSchema_Element(");
					sqlCmd.append(m_stdColumns).append(",C_AcctSchema_Element_ID,C_AcctSchema_ID,")
						.append("ElementType,Name,SeqNo,IsMandatory,IsBalanced) VALUES (");
					sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_Element_ID).append(",").append(m_as.getC_AcctSchema_ID()).append(",")
						.append("'").append(ElementType).append("','").append(name).append("',").append(SeqNo).append(",'")
						.append(IsMandatory).append("','").append(IsBalanced).append("')");
					no = DB.executeUpdate(sqlCmd.toString());
					if (no == 1)
						m_info.append(Msg.translate(m_lang, "C_AcctSchema_Element_ID")).append("=").append(name).append("\n");

					/** Default value for mandatory elements: OO and AC */
					if (ElementType.equals("OO"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET Org_ID=");
						sqlCmd.append(getAD_Org_ID()).append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Org in AcctSchamaElement NOT updated");
					}
					if (ElementType.equals("AC"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET C_ElementValue_ID=");
						sqlCmd.append(C_ElementValue_ID).append(", C_Element_ID=").append(C_Element_ID);
						sqlCmd.append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Account in AcctSchamaElement NOT updated");
					}
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e1)
		{
			Log.error ("MSetup.createAccounting - Elements", e1);
		}
		//  Create AcctSchema


		//  Create GL Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_GL (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "USESUSPENSEBALANCING,SUSPENSEBALANCING_ACCT,"
			+ "USESUSPENSEERROR,SUSPENSEERROR_ACCT,"
			+ "USECURRENCYBALANCING,CURRENCYBALANCING_ACCT,"
			+ "RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,"
			+ "INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT,"
			+ "PPVOFFSET_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(m_as.getC_AcctSchema_ID()).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEBALANCING_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEERROR_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("CURRENCYBALANCING_ACCT")).append(",");
		//  RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,
		sqlCmd.append(getAcct("RETAINEDEARNING_ACCT")).append(",");
		sqlCmd.append(getAcct("INCOMESUMMARY_ACCT")).append(",");
		//  INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT)
		sqlCmd.append(getAcct("INTERCOMPANYDUETO_ACCT")).append(",");
		sqlCmd.append(getAcct("INTERCOMPANYDUEFROM_ACCT")).append(",");
		sqlCmd.append(getAcct("PPVOFFSET_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - GL Accounts NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//	Create Std Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_DEFAULT (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT, "
			+ "P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT, "
			+ "P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT, "
			+ "C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT, "
			+ "V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT, "
			+ "PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT, "
			+ "UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT, "
			+ "WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT, "
			+ "PJ_ASSET_ACCT,PJ_WIP_ACCT,"
			+ "T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT, "
			+ "B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,"
			+ "B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,"
			+ "B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT, "
			+ "CH_EXPENSE_ACCT,CH_REVENUE_ACCT, "
			+ "UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT, "
			+ "CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(m_as.getC_AcctSchema_ID()).append(",");
		//  W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT
		sqlCmd.append(getAcct("W_INVENTORY_ACCT")).append(",");
		sqlCmd.append(getAcct("W_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("W_REVALUATION_ACCT")).append(",");
		sqlCmd.append(getAcct("W_INVACTUALADJUST_ACCT")).append(", ");
		//  P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT,
		sqlCmd.append(getAcct("P_REVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("P_COGS_ACCT")).append(", ");
		//  P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT,
		sqlCmd.append(getAcct("P_PURCHASEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_INVOICEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTREC_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTGRANT_ACCT")).append(", ");
		//  C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("C_RECEIVABLE_ACCT")).append(",");
		sqlCmd.append(getAcct("C_PREPAYMENT_ACCT")).append(", ");
		//  V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("V_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("V_LIABILITY_SERVICES_ACCT")).append(",");
		sqlCmd.append(getAcct("V_PREPAYMENT_ACCT")).append(", ");
		//  PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT,
		sqlCmd.append(getAcct("PAYDISCOUNT_EXP_ACCT")).append(",");
		sqlCmd.append(getAcct("PAYDISCOUNT_REV_ACCT")).append(",");
		sqlCmd.append(getAcct("WRITEOFF_ACCT")).append(", ");
		//  UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT,
		sqlCmd.append(getAcct("UNREALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("UNREALIZEDLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDLOSS_ACCT")).append(", ");
		//  WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT,
		sqlCmd.append(getAcct("WITHHOLDING_ACCT")).append(",");
		sqlCmd.append(getAcct("E_PREPAYMENT_ACCT")).append(",");
		sqlCmd.append(getAcct("E_EXPENSE_ACCT")).append(", ");
		//  PJ_ASSET_ACCT,PJ_WIP_ACCT,
		sqlCmd.append(getAcct("PJ_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("PJ_WIP_ACCT")).append(",");
		//  T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT,
		sqlCmd.append(getAcct("T_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("T_RECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("T_DUE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_CREDIT_ACCT")).append(", ");
		//  B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,
		sqlCmd.append(getAcct("B_INTRANSIT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("B_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTREV_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTEXP_ACCT")).append(",");
		//  B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,
		sqlCmd.append(getAcct("B_UNIDENTIFIED_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTLOSS_ACCT")).append(",");
		//  B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT,
		sqlCmd.append(getAcct("B_REVALUATIONGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_REVALUATIONLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("B_PAYMENTSELECT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_UNALLOCATEDCASH_ACCT")).append(", ");
		//  CH_EXPENSE_ACCT,CH_REVENUE_ACCT,
		sqlCmd.append(getAcct("CH_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CH_REVENUE_ACCT")).append(", ");
		//  UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT,
		sqlCmd.append(getAcct("UNEARNEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIPTS_ACCT")).append(", ");
		//  CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT)
		sqlCmd.append(getAcct("CB_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_CASHTRANSFER_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_RECEIPT_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Default Accounts NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//  GL Categories
		createGLCategory("Standard", "M", true);
		int GL_None = createGLCategory("None", "D", false);
		int GL_GL = createGLCategory("Manual", "M", false);
		int GL_ARI = createGLCategory("AR Invoice", "D", false);
		int GL_ARR = createGLCategory("AR Receipt", "D", false);
		int GL_MM = createGLCategory("Material Management", "D", false);
		int GL_API = createGLCategory("AP Invoice", "D", false);
		int GL_APP = createGLCategory("AP Payment", "D", false);
		int GL_CASH = createGLCategory("Cash/Payments", "D", false);

		//	Base DocumentTypes
		createDocType("GL Journal", "Journal", 
			MDocType.DOCBASETYPE_GLJournal, null, 0, 0, 1000, GL_GL);
		createDocType("GL Journal Batch", "Journal Batch", 
			MDocType.DOCBASETYPE_GLJournal, null, 0, 0, 100, GL_GL);
		//	MDocType.DOCBASETYPE_GLDocument
		//
		int DT_I = createDocType("AR Invoice", "Invoice", 
			MDocType.DOCBASETYPE_ARInvoice, null, 0, 0, 100000, GL_ARI);
		int DT_II = createDocType("AR Invoice Indirect", "Invoice", 
			MDocType.DOCBASETYPE_ARInvoice, null, 0, 0, 150000, GL_ARI);
		int DT_IC = createDocType("AR Credit Memo", "Credit Memo", 
			MDocType.DOCBASETYPE_ARCreditMemo, null, 0, 0, 170000, GL_ARI);
		//	MDocType.DOCBASETYPE_ARProFormaInvoice
		
		createDocType("AP Invoice", "Vendor Invoice", 
			MDocType.DOCBASETYPE_APInvoice, null, 0, 0, 0, GL_API);
		createDocType("AP CreditMemo", "Vendor Credit Memo", 
			MDocType.DOCBASETYPE_APCreditMemo, null, 0, 0, 0, GL_API);
		createDocType("Match Invoice", "Match Invoice", 
			MDocType.DOCBASETYPE_MatchInvoice, null, 0, 0, 390000, GL_API);
		
		createDocType("AR Receipt", "Customer Payment", 
			MDocType.DOCBASETYPE_ARReceipt, null, 0, 0, 0, GL_ARR);
		createDocType("AP Payment", "Vendor Payment", 
			MDocType.DOCBASETYPE_APPayment, null, 0, 0, 0, GL_APP);
		createDocType("Allocation", "Allocation", 
			MDocType.DOCBASETYPE_PaymentAllocation, null, 0, 0, 490000, GL_CASH);

		int DT_S  = createDocType("MM Shipment", "Delivery Note", 
			MDocType.DOCBASETYPE_MaterialDelivery, null, 0, 0, 500000, GL_MM);
		int DT_SI = createDocType("MM Shipment Indirect", "Delivery Note", 
			MDocType.DOCBASETYPE_MaterialDelivery, null, 0, 0, 550000, GL_MM);
		
		createDocType("MM Receipt", "Vendor Delivery", 
			MDocType.DOCBASETYPE_MaterialReceipt, null, 0, 0, 0, GL_MM);
		int DT_RM = createDocType("MM Returns", "Customer Returns", 
			MDocType.DOCBASETYPE_MaterialReceipt, null, 0, 0, 570000, GL_MM);
		
		createDocType("Purchase Order", "Purchase Order", 
			MDocType.DOCBASETYPE_PurchaseOrder, null, 0, 0, 800000, GL_None);
		createDocType("Match PO", "Patch PO", 
			MDocType.DOCBASETYPE_MatchPO, null, 0, 0, 890000, GL_None);
		createDocType("Purchase Requisition", "Purchase Requisition", 
			MDocType.DOCBASETYPE_PurchaseRequisition, null, 0, 0, 900000, GL_None);

		createDocType("Bank Statement", "Bank Statement", 
			MDocType.DOCBASETYPE_BankStatement, null, 0, 0, 700000, GL_CASH);
		createDocType("Cash Journal", "Cash Journal",
			MDocType.DOCBASETYPE_CashJournal, null, 0, 0, 750000, GL_CASH);
		
		createDocType("Material Movement", "Material Movement",
			MDocType.DOCBASETYPE_MaterialMovement, null, 0, 0, 610000, GL_MM);
		createDocType("Physical Inventory", "Physical Inventory", 
			MDocType.DOCBASETYPE_MaterialPhysicalInventory, null, 0, 0, 620000, GL_MM);
		createDocType("Material Production", "Material Production", 
			MDocType.DOCBASETYPE_MaterialProduction, null, 0, 0, 630000, GL_MM);
		createDocType("Project Issue", "Project Issue", 
			MDocType.DOCBASETYPE_ProjectIssue, null, 0, 0, 640000, GL_MM);

		//  Order Entry
		createDocType("Binding offer", "Quotation", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_Quotation, 
			0, 0, 10000, GL_None);
		createDocType("Non binding offer", "Proposal", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_Proposal, 
			0, 0, 20000, GL_None);
		createDocType("Prepay Order", "Prepay Order", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_PrepayOrder, 
			DT_S, DT_I, 30000, GL_None);
		createDocType("Return Material", "Return Material Authorization", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_ReturnMaterial, 
			DT_RM, DT_IC, 30000, GL_None);
		createDocType("Standard Order", "Order Confirmation", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_StandardOrder, 
			DT_S, DT_I, 50000, GL_None);
		createDocType("Credit Order", "Order Confirmation", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_OnCreditOrder, 
			DT_SI, DT_I, 60000, GL_None);   //  RE
		createDocType("Warehouse Order", "Order Confirmation", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_WarehouseOrder, 
			DT_S, DT_I,	70000, GL_None);    //  LS
		int DT = createDocType("POS Order", "Order Confirmation", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_POSOrder, 
			DT_SI, DT_II, 80000, GL_None);    // Bar
		//	POS As Default for window SO
		createPreference("C_DocTypeTarget_ID", String.valueOf(DT), 143);

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_AcctSchema1_ID=").append(m_as.getC_AcctSchema_ID())
			.append(", C_Calendar_ID=").append(C_Calendar_ID)
			.append(" WHERE AD_Client_ID=").append(m_client.getAD_Client_ID());
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//
		return true;
	}   //  createAccounting

	/**
	 *  Get Account ID for key
	 *  @param key key
	 *  @return C_ValidCombination_ID
	 */
	private int getAcct (String key)
	{
		Log.trace(Log.l4_Data, "MSetup.getAcct - " + key);
		//  Element
		int C_ElementValue_ID = m_nap.getC_ElementValue_ID(key);
		MAccount vc = MAccount.getDefault(m_as, true);	//	optional null
		vc.setAccount_ID(C_ElementValue_ID);
		vc.save();
		int C_ValidCombination_ID = vc.getC_ValidCombination_ID();

		//  Error
		if (C_ValidCombination_ID == 0)
			Log.error("MSetup.getAcct - no account for " + key);
		return C_ValidCombination_ID;
	}   //  getAcct

	/**
	 *  Create GL Category
	 *  @param Name name
	 *  @param CategoryType category type
	 *  @param isDefault is default value
	 *  @return GL_Category_ID
	 */
	private int createGLCategory (String Name, String CategoryType, boolean isDefault)
	{
		int GL_Category_ID = getNextID(m_client.getAD_Client_ID(), "GL_Category");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO GL_Category ");
		sqlCmd.append("(GL_Category_ID,").append(m_stdColumns).append(",")
			.append("Name,CategoryType,IsDefault) VALUES (")
			.append(GL_Category_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(Name).append("','").append(CategoryType).append("','")
			.append(isDefault ? "Y" : "N").append("')");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createGLCategory - GL Category NOT created - " + Name);
		//
		return GL_Category_ID;
	}   //  createGLCategory

	/**
	 *  Create Document Types with Sequence
	 *  @param Name name
	 *  @param PrintName print name
	 *  @param DocBaseType document base type
	 *  @param DocSubTypeSO sales order sub type
	 *  @param C_DocTypeShipment_ID shipment doc
	 *  @param C_DocTypeInvoice_ID invoice doc
	 *  @param StartNo start doc no
	 *  @param GL_Category_ID gl category
	 *  @return C_DocType_ID doc type
	 */
	private int createDocType (String Name, String PrintName,
		String DocBaseType, String DocSubTypeSO,
		int C_DocTypeShipment_ID, int C_DocTypeInvoice_ID,
		int StartNo, int GL_Category_ID)
	{
		MSequence sequence = null;
		if (StartNo != 0)
		{
			sequence = new MSequence(m_ctx, getAD_Client_ID(), Name, StartNo);
			if (!sequence.save())
				Log.error("MSetup.createDocType - Sequence NOT created - " + Name);
		}
		
		MDocType dt = new MDocType (m_ctx, DocBaseType, Name);
		dt.setPrintName(PrintName);
		if (DocSubTypeSO != null)
			dt.setDocSubTypeSO(DocSubTypeSO);
		if (C_DocTypeShipment_ID != 0)
			dt.setC_DocTypeShipment_ID(C_DocTypeShipment_ID);
		if (C_DocTypeInvoice_ID != 0)
			dt.setC_DocTypeInvoice_ID(C_DocTypeInvoice_ID);
		if (GL_Category_ID != 0)
			dt.setGL_Category_ID(GL_Category_ID);
		if (sequence == null)
			dt.setIsDocNoControlled(false);
		else
			dt.setDocNoSequence_ID(sequence.getAD_Sequence_ID());
		dt.setIsSOTrx();
		if (!dt.save())
			Log.error("MSetup.createDocType - DocType NOT created - " + Name);
		//
		return dt.getC_DocType_ID();
	}   //  createDocType

	
	/**************************************************************************
	 *  Create Default main entities.
	 *  - Dimensions & BPGroup, Prod Category)
	 *  - Location, Locator, Warehouse
	 *  - PriceList
	 *  - Cashbook, PaymentTerm
	 *  @param C_Country_ID country
	 *  @param City city
	 *  @param C_Region_ID region
	 *  @return true if created
	 */
	public boolean createEntities (int C_Country_ID, String City, int C_Region_ID, int C_Currency_ID)
	{
		Log.trace(Log.l3_Util, "MSetup.createEntries", "C_Country_ID=" + C_Country_ID + ", City=" + City + ", C_Region_ID=" + C_Region_ID);
		m_info.append("\n----\n");
		//
		String defaultName = Msg.translate(m_lang, "Standard");
		String defaultEntry = "'" + defaultName + "',";
		StringBuffer sqlCmd = null;
		int no = 0;

		//	Create Marketing Channel/Campaign
		int C_Channel_ID = getNextID(getAD_Client_ID(), "C_Channel");
		sqlCmd = new StringBuffer("INSERT INTO C_Channel ");
		sqlCmd.append("(C_Channel_ID,Name,");
		sqlCmd.append(m_stdColumns).append(") VALUES (");
		sqlCmd.append(C_Channel_ID).append(",").append(defaultEntry);
		sqlCmd.append(m_stdValues).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Channel NOT inserted");
		int C_Campaign_ID = getNextID(getAD_Client_ID(), "C_Campaign");
		sqlCmd = new StringBuffer("INSERT INTO C_Campaign ");
		sqlCmd.append("(C_Campaign_ID,C_Channel_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,Costs) VALUES (");
		sqlCmd.append(C_Campaign_ID).append(",").append(C_Channel_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Campaign_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Campaign NOT inserted");
		if (m_hasMCampaign)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Campaign_ID=").append(C_Campaign_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
			sqlCmd.append(" AND ElementType='MC'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Campaign NOT updated");
		}

		//	Create Sales Region
		int C_SalesRegion_ID = getNextID(getAD_Client_ID(), "C_SalesRegion");
		sqlCmd = new StringBuffer ("INSERT INTO C_SalesRegion ");
		sqlCmd.append("(C_SalesRegion_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsSummary) VALUES (");
		sqlCmd.append(C_SalesRegion_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_SalesRegion_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRegion NOT inserted");
		if (m_hasSRegion)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_SalesRegion_ID=").append(C_SalesRegion_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
			sqlCmd.append(" AND ElementType='SR'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement SalesRegion NOT updated");
		}

		/**
		 *  Business Partner
		 */
		//  Create BP Group
		int C_BP_Group_ID = getNextID(getAD_Client_ID(), "C_BP_Group");
		sqlCmd = new StringBuffer ("INSERT INTO C_BP_Group ");
		sqlCmd.append("(C_BP_Group_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault) VALUES ( ");
		sqlCmd.append(C_BP_Group_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BP_Group_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BP Group NOT inserted");

		//	Create BPartner
		int C_BPartner_ID = getNextID(getAD_Client_ID(), "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_BP_Group_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BPartner_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BPartner NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("C_BPartner_ID=").append(C_BPartner_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
		sqlCmd.append(" AND ElementType='BP'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element BPartner NOT updated");
		createPreference("C_BPartner_ID", String.valueOf(C_BPartner_ID), 143);

		/**
		 *  Product
		 */
		//  Create Product Category
		int M_Product_Category_ID = getNextID(getAD_Client_ID(), "M_Product_Category");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product_Category ");
		sqlCmd.append("(M_Product_Category_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault,PlannedMargin) VALUES (");
		sqlCmd.append(M_Product_Category_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y',0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_Category_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product Category NOT inserted");

		//  UOM (EA)
		int C_UOM_ID = 100;

		//  TaxCategory
		int C_TaxCategory_ID = getNextID(getAD_Client_ID(), "C_TaxCategory");
		sqlCmd = new StringBuffer ("INSERT INTO C_TaxCategory ");
		sqlCmd.append("(C_TaxCategory_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,IsDefault) VALUES (");
		sqlCmd.append(C_TaxCategory_ID).append(",").append(m_stdValues).append(", ");
		if (C_Country_ID == 100)    // US
			sqlCmd.append("'Sales Tax','Y')");
		else
			sqlCmd.append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - TaxCategory NOT inserted");

		//  Tax - Zero Rate
		MTax tax = new MTax (m_ctx, "Standard", Env.ZERO, C_TaxCategory_ID);
		tax.setIsDefault(true);
		if (tax.save())
			m_info.append(Msg.translate(m_lang, "C_Tax_ID"))
				.append("=").append(tax.getName()).append("\n");
		else
			Log.error("MSetup.createEntities - Tax NOT inserted");

		//	Create Product
		int M_Product_ID = getNextID(getAD_Client_ID(), "M_Product");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product ");
		sqlCmd.append("(M_Product_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_UOM_ID,M_Product_Category_ID,C_TaxCategory_ID) VALUES (");
		sqlCmd.append(M_Product_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry);
		sqlCmd.append(C_UOM_ID).append(",").append(M_Product_Category_ID).append(",");
		sqlCmd.append(C_TaxCategory_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("M_Product_ID=").append(M_Product_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
		sqlCmd.append(" AND ElementType='PR'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element Product NOT updated");

		/**
		 *  Warehouse
		 */
		//  Location (Company)
		int C_Location_ID = getNextID(getAD_Client_ID(), "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append(DB.TO_STRING(City)).append(",").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		sqlCmd = new StringBuffer ("UPDATE AD_OrgInfo SET C_Location_ID=");
		sqlCmd.append(C_Location_ID).append(" WHERE AD_Org_ID=").append(getAD_Org_ID());
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Location NOT inserted");
		createPreference("C_Country_ID", String.valueOf(C_Country_ID), 0);

		//  Default Warehouse
		int M_Warehouse_ID = getNextID(getAD_Client_ID(), "M_Warehouse");
		sqlCmd = new StringBuffer ("INSERT INTO M_Warehouse ");
		sqlCmd.append("(M_Warehouse_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Location_ID,Separator) VALUES (");
		sqlCmd.append(M_Warehouse_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Location_ID).append(",'-')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Warehouse NOT inserted");

		//   Locator
		int M_Locator_ID = getNextID(getAD_Client_ID(), "M_Locator");
		sqlCmd = new StringBuffer ("INSERT INTO M_Locator ");
		sqlCmd.append("(M_Locator_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,M_Warehouse_ID,X,Y,Z,PriorityNo,IsDefault) VALUES (");
		sqlCmd.append(M_Locator_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(M_Warehouse_ID).append(",0,0,0,50,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Locator NOT inserted");

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_BPartnerCashTrx_ID=").append(C_BPartner_ID);
		sqlCmd.append(",M_ProductFreight_ID=").append(M_Product_ID);
//		sqlCmd.append("C_UOM_Volume_ID=");
//		sqlCmd.append(",C_UOM_Weight_ID=");
//		sqlCmd.append(",C_UOM_Length_ID=");
//		sqlCmd.append(",C_UOM_Time_ID=");
		sqlCmd.append(" WHERE AD_Client_ID=").append(getAD_Client_ID());
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createEntities - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		/**
		 *  Other
		 */
		//  PriceList
		int M_PriceList_ID = getNextID(getAD_Client_ID(), "M_PriceList");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList ");
		sqlCmd.append("(M_PriceList_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID,IsDefault) VALUES (");
		sqlCmd.append(M_PriceList_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(",'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList NOT inserted");
		//  DiscountSchema
		int M_DiscountSchema_ID = getNextID(getAD_Client_ID(), "M_DiscountSchema");
		sqlCmd = new StringBuffer ("INSERT INTO M_DiscountSchema ");
		sqlCmd.append("(M_DiscountSchema_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,DiscountType, FlatDiscount) VALUES (");
		sqlCmd.append(M_DiscountSchema_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append("SysDate,'P',0)");	//	PriceList
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - DiscountSchema NOT inserted");
		//  PriceList Version
		int M_PriceList_Version_ID = getNextID(getAD_Client_ID(), "M_PriceList_Version");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList_Version ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,M_PriceList_ID,M_DiscountSchema_ID) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY-MM-DD'),SysDate,").append(M_PriceList_ID)
			.append(",").append(M_DiscountSchema_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList_Version NOT inserted");
		//  ProductPrice
		sqlCmd = new StringBuffer ("INSERT INTO M_ProductPrice ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("M_Product_ID,PriceList,PriceStd,PriceLimit) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(M_Product_ID).append(",1,1,1)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - ProductPrice NOT inserted");

		//  Location for Standard BP
		C_Location_ID = getNextID(getAD_Client_ID(), "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append(DB.TO_STRING(City)).append(",").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		int C_BPartner_Location_ID = getNextID(getAD_Client_ID(), "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(DB.TO_STRING(City)).append(",").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (Standard) NOT inserted");

		//	Create Sales Rep for Client-User
		C_BPartner_ID = getNextID(getAD_Client_ID(), "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_U_Name).append("','").append(AD_User_U_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_U_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (User) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_U_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (User) NOT updated");

		//  Location for Client-User
		C_Location_ID = getNextID(getAD_Client_ID(), "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append(DB.TO_STRING(City)).append(",").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		C_BPartner_Location_ID = getNextID(getAD_Client_ID(), "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(DB.TO_STRING(City)).append(",").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (User) NOT inserted");


		//	Create Sales Rep for Client-Admin
		C_BPartner_ID = getNextID(getAD_Client_ID(), "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_Name).append("','").append(AD_User_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (Admin) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (Admin) NOT updated");

		//  Location for Client-Admin
		C_Location_ID = getNextID(getAD_Client_ID(), "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append(DB.TO_STRING(City)).append(",").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		C_BPartner_Location_ID = getNextID(getAD_Client_ID(), "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(DB.TO_STRING(City)).append(",").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (Admin) NOT inserted");


		//  Payment Term
		int C_PaymentTerm_ID = getNextID(getAD_Client_ID(), "C_PaymentTerm");
		sqlCmd = new StringBuffer ("INSERT INTO C_PaymentTerm ");
		sqlCmd.append("(C_PaymentTerm_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,NetDays,GraceDays,DiscountDays,Discount,DiscountDays2,Discount2,IsDefault) VALUES (");
		sqlCmd.append(C_PaymentTerm_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'Immediate','Immediate',0,0,0,0,0,0,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PaymentTerm NOT inserted");

		//  Project Cycle
		C_Cycle_ID = getNextID(getAD_Client_ID(), "C_Cycle");
		sqlCmd = new StringBuffer ("INSERT INTO C_Cycle ");
		sqlCmd.append("(C_Cycle_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_Cycle_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Cycle NOT inserted");

		/**
		 *  Organization level data	===========================================
		 */

		//	Create Default Project
		int C_Project_ID = getNextID(getAD_Client_ID(), "C_Project");
		sqlCmd = new StringBuffer ("INSERT INTO C_Project ");
		sqlCmd.append("(C_Project_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Currency_ID,IsSummary) VALUES (");
		sqlCmd.append(C_Project_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Currency_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Project_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Project NOT inserted");
		//  Default Project
		if (m_hasProject)
		{
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Project_ID=").append(C_Project_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
			sqlCmd.append(" AND ElementType='PJ'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Project NOT updated");
		}

		//  CashBook
		int C_CashBook_ID = getNextID(getAD_Client_ID(), "C_CashBook");
		sqlCmd = new StringBuffer ("INSERT INTO C_CashBook ");
		sqlCmd.append("(C_CashBook_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_CashBook_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_CashBook_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - CashBook NOT inserted");

		return true;
	}   //  createEntities

	/**
	 *  Create Preference
	 *  @param Attribute attribute
	 *  @param Value value
	 *  @param AD_Window_ID window
	 */
	private void createPreference (String Attribute, String Value, int AD_Window_ID)
	{
		int AD_Preference_ID = getNextID(getAD_Client_ID(), "AD_Preference");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO AD_Preference ");
		sqlCmd.append("(AD_Preference_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Attribute,Value,AD_Window_ID) VALUES (");
		sqlCmd.append(AD_Preference_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(Attribute).append("','").append(Value).append("',");
		if (AD_Window_ID == 0)
			sqlCmd.append("NULL)");
		else
			sqlCmd.append(AD_Window_ID).append(")");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createPreference - Preference NOT inserted - " + Attribute);
	}   //  createPreference

	/**************************************************************************

	/**
	 * 	Get Next ID
	 * 	@param AD_Client_ID client
	 * 	@param TableName table name
	 */
	private int getNextID (int AD_Client_ID, String TableName)
	{
		return DB.getNextID(AD_Client_ID, TableName, null);
	}	//	getNextID

	/**
	 *  Get Client
	 *  @return AD_Client_ID
	 */
	public int getAD_Client_ID()
	{
		return m_client.getAD_Client_ID();
	}
	public int getAD_Org_ID()
	{
		return m_org.getAD_Org_ID();
	}
	public int getAD_User_ID()
	{
		return AD_User_ID;
	}
	public String getInfo()
	{
		return m_info.toString();
	}
}   //  MSetup
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
package org.compiere.model;

import java.util.*;
import java.sql.*;
import java.io.*;

import org.compiere.util.*;

/**
 * Initial Setup Model
 *
 * @author Jorg Janke
 * @version $Id: MSetup.java,v 1.25 2003/07/22 18:49:02 jjanke Exp $
 */
public final class MSetup
{
	/**
	 *  Constructor
	 *  @param ctx context
	 *  @param WindowNo window
	 */
	public MSetup(Properties ctx, int WindowNo)
	{
		m_ctx = ctx;
		m_lang = Env.getAD_Language(m_ctx);
		m_WindowNo = WindowNo;
	}   //  MSetup

	private Properties      m_ctx;
	private String          m_lang;
	private int             m_WindowNo;
	private StringBuffer    m_info;
	//
	private String          m_clientName;
	private String          m_orgName;
	//
	private String          m_stdColumns = "AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy";
	private String          m_stdValues;
	private String          m_stdValuesOrg;
	//
	private NaturalAccountMap m_nap = null;
	//
	private final String    CompiereSys = "N";           //  Should NOT be changed
	private int             AD_Client_ID = 1000000;     //  initial Client_ID
	//
	private int     AD_Org_ID;
	private int     AD_User_ID;
	private String  AD_User_Name;
	private int     AD_User_U_ID;
	private String  AD_User_U_Name;
	private int     AD_Role_ID;
	private int     C_Calendar_ID;
	private int     C_AcctSchema_ID;
	private int     C_Currency_ID;
	private int     AD_Tree_Account_ID;
	private int     C_Cycle_ID;
	//
	private boolean         m_hasProject = false;
	private boolean         m_hasMCampaign = false;
	private boolean         m_hasSRegion = false;
	private AcctSchema      m_AcctSchema = null;


	/**
	 *  Create Client Info.
	 *  - Client, Trees, Org, Role, User, User_Role
	 *  @param clientName client name
	 *  @param orgName org name
	 *  @param userClient user id client
	 *  @param userOrg user id org
	 *  @return true if created
	 */
	public boolean createClient (String clientName, String orgName,
		String userClient, String userOrg)
	{
		Log.trace(Log.l3_Util, "MSetup.createClient");
		//  info header
		m_info = new StringBuffer();
		//  Standarc columns
		String name = null;
		String sql = null;
		int no = 0;

		/**
		 *  Create Client
		 */
		Env.setContext(m_ctx, "#CompiereSys", CompiereSys);
		AD_Client_ID = DB.getKeyNextNo (AD_Client_ID, CompiereSys, "AD_Client");
		Env.setContext(m_ctx, m_WindowNo, "AD_Client_ID", AD_Client_ID);

		//	Standard Values
		m_stdValues = String.valueOf(AD_Client_ID) + ",0,'Y',SysDate,0,SysDate,0";

		//	Create Client
		name = clientName;
		if (name == null || name.length() == 0)
			name = "newClient";
		name = name.trim();
		m_clientName = name;
		sql = "INSERT INTO AD_Client(" + m_stdColumns + ",Value,Name,Description)"
			+ " VALUES (" + m_stdValues + ",'" + name + "','" + name + "','" + name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Client NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info - Client
		m_info.append(Msg.translate(m_lang, "AD_Client_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Trees
		 */
		String sql2 = null;
		if (Env.isBaseLanguage(m_lang, "AD_Ref_List"))	//	Get TreeTypes & Name
			sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=120";
		else
			sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Reference_ID=120 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID";

		//  Tree IDs
		int AD_Tree_Org_ID=0, AD_Tree_BPartner_ID=0, AD_Tree_Project_ID=0,
			AD_Tree_SalesRegion_ID=0, AD_Tree_Product_ID=0;

		try
		{
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next())
			{
				String value = rs.getString(1);
				int AD_Tree_ID = 0;
				if (value.equals("OO"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Org_ID = AD_Tree_ID;
				}
				else if (value.equals("BP"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_BPartner_ID = AD_Tree_ID;
				}
				else if (value.equals("PJ"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Project_ID = AD_Tree_ID;
				}
				else if (value.equals("SR"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_SalesRegion_ID = AD_Tree_ID;
				}
				else if (value.equals("PR"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Product_ID = AD_Tree_ID;
				}
				else if (value.endsWith("EV"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Account_ID = AD_Tree_ID;
				}
				else if (value.equals("MM"))	//	No Menu
					;
				else
					//	PC (Product Category), BB (BOM), MC (Marketing Campaign), AY (Activity)
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");

				//
				if (AD_Tree_ID != 0)
				{
					name = m_clientName + " " + rs.getString(2);
					sql = "INSERT INTO AD_Tree(" + m_stdColumns + ",AD_Tree_ID,Name,Description,TreeType)"
						+ " VALUES (" + m_stdValues + "," + AD_Tree_ID + ",'" + name + "','" + name + "','" + value + "')";
					no = DB.executeUpdate(sql);
					if (no == 1)
						m_info.append(Msg.translate(m_lang, "AD_Tree_ID")).append("=").append(name).append("\n");
					else
						Log.error("MSetup.createClient - Tree NOT created: " + name);
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e1)
		{
			Log.error ("MSetup.createClient - Trees", e1);
		}

		//	Get Primary Tree
		int AD_Tree_Menu_ID = 10;	//	hardcoded

		//	Create ClientInfo
		sql = "INSERT INTO AD_ClientInfo(" + m_stdColumns + ",Acct2_Active,Acct3_Active,"
			+ "AD_Tree_Menu_ID,AD_Tree_Org_ID,AD_Tree_BPartner_ID,AD_Tree_Project_ID,AD_Tree_SalesRegion_ID,AD_Tree_Product_ID)"
			+ " VALUES (" + m_stdValues + ",'N','N',"
			+ AD_Tree_Menu_ID + "," + AD_Tree_Org_ID + "," + AD_Tree_BPartner_ID + "," + AD_Tree_Project_ID + "," + AD_Tree_SalesRegion_ID + "," + AD_Tree_Product_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - ClientInfo NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
	//	m_info.append(Msg.translate(m_lang, "AD_ClientInfo")).append("\n");

		/**
		 *  Create Org
		 */
		AD_Org_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Org");
		m_stdValuesOrg = AD_Client_ID + "," + AD_Org_ID + ",'Y',SysDate,0,SysDate,0";

		name = orgName;
		if (name == null || name.length() == 0)
			name = "newOrg";
		m_orgName = name;
		sql = "INSERT INTO AD_Org (" + m_stdColumns + ",Value,Name,IsSummary)"
			+ " VALUES (" +	m_stdValuesOrg + ",'" + name + "','" + name + "','N')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Org NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_Org_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Roles
		 *  - Admin
		 *  - User
		 */
		name = m_clientName + " Admin";
		AD_Role_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Role");
		sql = "INSERT INTO AD_Role(" + m_stdColumns + ",AD_Role_ID,"
			+ "Name,Description,UserLevel,ClientList,OrgList)"
			+ " VALUES (" + m_stdValues + "," + AD_Role_ID + ","
			+ "'" + name + "','" + name + "',' CO','" + AD_Client_ID + "','0," + AD_Org_ID + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin Role A NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  OrgAccess x,0
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValues + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_OrgAccess 0 NOT created");
		//  OrgAccess x,y
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValuesOrg + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_OrgAccess NOT created");
		//  Info - Admin Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		//
		name = m_clientName + " User";
		int AD_Role_ID_U = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Role");
		sql = "INSERT INTO AD_Role(" + m_stdColumns + ",AD_Role_ID,"
			+ "Name,Description,UserLevel,ClientList,OrgList)"
			+ " VALUES (" + m_stdValues + "," + AD_Role_ID_U + ","
			+ "'" + name + "','" + name + "','  O','" + AD_Client_ID + "','" + AD_Org_ID + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - User Role A NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  OrgAccess x,y
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValuesOrg + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - User Role_OrgAccess NOT created");
		//  Info - Client Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Users
		 *  - Client
		 *  - Org
		 */
		name = userClient;
		if (name == null || name.length() == 0)
			name = m_clientName + "Client";
		AD_User_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_User");
		AD_User_Name = name;
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + ","
			+ "'" + AD_User_Name + "','" + AD_User_Name + "','" + AD_User_Name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin User NOT inserted - " + AD_User_Name;
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_Name).append("/").append(AD_User_Name).append("\n");

		name = userOrg;
		if (name == null || name.length() == 0)
			name = m_clientName + "Org";
		AD_User_U_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_User");
		AD_User_U_Name = name;
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + ","
			+ "'" + AD_User_U_Name + "','" + AD_User_U_Name + "','" + AD_User_U_Name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Org User NOT inserted - " + AD_User_U_Name;
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_U_Name).append("/").append(AD_User_U_Name).append("\n");

		/**
		 *  Create User-Role
		 */
		//  ClientUser          - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+Admin NOT inserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+User NOT inserted");
		//  OrgUser             - User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole OrgUser+Org NOT inserted");
		//  SuperUser(100)      - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + ",100," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole SuperUser+Admin NOT inserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + ",100," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole SuperUser+User NOT inserted");

		return true;
	}   //  createClient



	/*************************************************************************/

	/**
	 *  Create Accounting elements.
	 *  - Calendar
	 *  - Account Trees
	 *  - Account Values
	 *  - Accounting Schema
	 *  - Default Accounts
	 *
	 *  @param newC_Currency_ID currency
	 *  @param curName currency name
	 *  @param hasProduct has product segment
	 *  @param hasBPartner has bp segment
	 *  @param hasProject has project segment
	 *  @param hasMCampaign has campaign segment
	 *  @param hasSRegion has sales region segment
	 *  @param AccountingFile file name of accounting file
	 *  @return true if created
	 */
	public boolean createAccounting(int newC_Currency_ID, String curName,
		boolean hasProduct, boolean hasBPartner, boolean hasProject,
		boolean hasMCampaign, boolean hasSRegion,
		File AccountingFile)
	{
		Log.trace(Log.l3_Util, "MSetup.createAccount");
		//
		C_Currency_ID = newC_Currency_ID;
		m_hasProject = hasProject;
		m_hasMCampaign = hasMCampaign;
		m_hasSRegion = hasSRegion;

		//  Standard variables
		m_info = new StringBuffer();
		String name = null;
		StringBuffer sqlCmd = null;
		int no = 0;

		/**
		 *  Create Calendar
		 */
		C_Calendar_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Calendar");
		name = m_clientName + " " + Msg.translate(m_lang, "C_Calendar_ID");
		sqlCmd = new StringBuffer("INSERT INTO C_Calendar(");
		sqlCmd.append(m_stdColumns).append(",C_Calendar_ID,Name)").append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_Calendar_ID).append(",'").append(name).append("')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Calendar NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_Calendar_ID")).append("=").append(name).append("\n");

		//  Year
		int C_Year_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Year");
		sqlCmd = new StringBuffer ("INSERT INTO C_Year ");
		sqlCmd.append("(C_Year_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Year,C_Calendar_ID) VALUES (");
		sqlCmd.append(C_Year_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY'),").append(C_Calendar_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createAccounting - Year NOT inserted");
		/** @todo Create Periods */


		//	Create Account Elements
		int C_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Element");
		name = m_clientName + " " + Msg.translate(m_lang, "Account_ID");
		sqlCmd = new StringBuffer ("INSERT INTO C_ELEMENT(");
		sqlCmd.append(m_stdColumns).append(",C_Element_ID,Name,Description,")
			.append("VFormat,ElementType,IsBalancing,IsNaturalAccount,AD_Tree_ID)").append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_Element_ID).append(",'").append(name).append("','").append(name).append("',")
			.append("NULL,'A','N','Y',").append(AD_Tree_Account_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Acct Element NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		m_info.append(Msg.translate(m_lang, "C_Element_ID")).append("=").append(name).append("\n");

		//	Create Account Values
		m_nap = new NaturalAccountMap(m_ctx);
		String errMsg = m_nap.parseFile(AccountingFile);
		if (errMsg.length() != 0)
		{
			Log.error(errMsg);
			m_info.append(errMsg);
			return false;
		}
		if (m_nap.createAccounts(AD_Client_ID, AD_Org_ID, C_Element_ID))
			m_info.append(Msg.translate(m_lang, "C_ElementValue_ID")).append(" # ").append(m_nap.size()).append("\n");
		else
		{
			String err = "MSetup.createAccounting - Acct Element Values NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		int C_ElementValue_ID = m_nap.getC_ElementValue_ID("DEFAULT_ACCT");
		Log.trace(Log.l4_Data, "C_ElementValue_ID=" + C_ElementValue_ID);

		/**
		 *  Create AccountingSchema
		 */
		C_AcctSchema_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema");
		//
		String GAAP = "US";				//	AD_Reference_ID=123
		String CostingMethod = "A";		//	AD_Reference_ID=122
		name = m_clientName + " " + GAAP + "/" + CostingMethod + "/" + curName;
		//
		sqlCmd = new StringBuffer ("INSERT INTO C_AcctSchema(");
		sqlCmd.append(m_stdColumns).append(",C_AcctSchema_ID,Name,")
			.append("GAAP,IsAccrual,CostingMethod,C_Currency_ID,")
			.append("AutoPeriodControl,Separator,HasAlias,HasCombination)")
			.append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",'").append(name).append("',")
			.append("'").append(GAAP).append("','Y','").append(CostingMethod).append("',").append(C_Currency_ID).append(",")
			.append("'N','-','Y','N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - AcctSchema NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_AcctSchema_ID")).append("=").append(name).append("\n");

		/**
		 *  Create AccountingSchema Elements (Structure)
		 */
		String sql2 = null;
		if (Env.isBaseLanguage(m_lang, "AD_Reference"))	//	Get TreeTypes & Name
			sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=181";
		else
			sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Reference_ID=181 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID";
		//
		int Element_OO=0, Element_AC=0, Element_PR=0, Element_BP=0, Element_PJ=0,
			Element_MC=0, Element_SR=0;
		try
		{
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next())
			{
				String ElementType = rs.getString(1);
				name = rs.getString(2);
				//
				String IsMandatory = null;
				String IsBalanced = "N";
				int SeqNo = 0;
				int C_AcctSchema_Element_ID = 0;

				if (ElementType.equals("OO"))
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_OO = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					IsBalanced = "Y";
					SeqNo = 10;
				}
				else if (ElementType.equals("AC"))
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_AC = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					SeqNo = 20;
				}
				else if (ElementType.equals("PR") && hasProduct)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_PR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 30;
				}
				else if (ElementType.equals("BP") && hasBPartner)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_BP = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 40;
				}
				else if (ElementType.equals("PJ") && hasProject)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_PJ = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 50;
				}
				else if (ElementType.equals("MC") && hasMCampaign)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_MC = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 60;
				}
				else if (ElementType.equals("SR") && hasSRegion)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_SR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 70;
				}
				//	Not OT, LF, LT, U1, U2, AY

				if (IsMandatory != null)
				{
					sqlCmd = new StringBuffer ("INSERT INTO C_AcctSchema_Element(");
					sqlCmd.append(m_stdColumns).append(",C_AcctSchema_Element_ID,C_AcctSchema_ID,")
						.append("ElementType,Name,SeqNo,IsMandatory,IsBalanced) VALUES (");
					sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_Element_ID).append(",").append(C_AcctSchema_ID).append(",")
						.append("'").append(ElementType).append("','").append(name).append("',").append(SeqNo).append(",'")
						.append(IsMandatory).append("','").append(IsBalanced).append("')");
					no = DB.executeUpdate(sqlCmd.toString());
					if (no == 1)
						m_info.append(Msg.translate(m_lang, "C_AcctSchema_Element_ID")).append("=").append(name).append("\n");

					/** Default value for mandatory elements: OO and AC */
					if (ElementType.equals("OO"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET Org_ID=");
						sqlCmd.append(AD_Org_ID).append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Org in AcctSchamaElement NOT updated");
					}
					if (ElementType.equals("AC"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET C_ElementValue_ID=");
						sqlCmd.append(C_ElementValue_ID).append(", C_Element_ID=").append(C_Element_ID);
						sqlCmd.append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Account in AcctSchamaElement NOT updated");
					}
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e1)
		{
			Log.error ("MSetup.createAccounting - Elements", e1);
		}
		//  Create AcctSchema
		m_AcctSchema = new AcctSchema (C_AcctSchema_ID);


		//  Create GL Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_GL (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "USESUSPENSEBALANCING,SUSPENSEBALANCING_ACCT,"
			+ "USESUSPENSEERROR,SUSPENSEERROR_ACCT,"
			+ "USECURRENCYBALANCING,CURRENCYBALANCING_ACCT,"
			+ "RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,"
			+ "INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT,"
			+ "PPVOFFSET_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEBALANCING_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEERROR_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("CURRENCYBALANCING_ACCT")).append(",");
		//  RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,
		sqlCmd.append(getAcct("RETAINEDEARNING_ACCT")).append(",");
		sqlCmd.append(getAcct("INCOMESUMMARY_ACCT")).append(",");
		//  INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT)
		sqlCmd.append(getAcct("INTERCOMPANYDUETO_ACCT")).append(",");
		sqlCmd.append(getAcct("INTERCOMPANYDUEFROM_ACCT")).append(",");
		sqlCmd.append(getAcct("PPVOFFSET_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - GL Accounts NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//	Create Std Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_DEFAULT (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT, "
			+ "P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT, "
			+ "P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT, "
			+ "C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT, "
			+ "V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT, "
			+ "PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT, "
			+ "UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT, "
			+ "WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT, "
			+ "PJ_ASSET_ACCT,PJ_WIP_ACCT,"
			+ "T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT, "
			+ "B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,"
			+ "B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,"
			+ "B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT, "
			+ "CH_EXPENSE_ACCT,CH_REVENUE_ACCT, "
			+ "UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT, "
			+ "CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",");
		//  W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT
		sqlCmd.append(getAcct("W_INVENTORY_ACCT")).append(",");
		sqlCmd.append(getAcct("W_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("W_REVALUATION_ACCT")).append(",");
		sqlCmd.append(getAcct("W_INVACTUALADJUST_ACCT")).append(", ");
		//  P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT,
		sqlCmd.append(getAcct("P_REVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("P_COGS_ACCT")).append(", ");
		//  P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT,
		sqlCmd.append(getAcct("P_PURCHASEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_INVOICEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTREC_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTGRANT_ACCT")).append(", ");
		//  C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("C_RECEIVABLE_ACCT")).append(",");
		sqlCmd.append(getAcct("C_PREPAYMENT_ACCT")).append(", ");
		//  V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("V_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("V_LIABILITY_SERVICES_ACCT")).append(",");
		sqlCmd.append(getAcct("V_PREPAYMENT_ACCT")).append(", ");
		//  PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT,
		sqlCmd.append(getAcct("PAYDISCOUNT_EXP_ACCT")).append(",");
		sqlCmd.append(getAcct("PAYDISCOUNT_REV_ACCT")).append(",");
		sqlCmd.append(getAcct("WRITEOFF_ACCT")).append(", ");
		//  UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT,
		sqlCmd.append(getAcct("UNREALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("UNREALIZEDLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDLOSS_ACCT")).append(", ");
		//  WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT,
		sqlCmd.append(getAcct("WITHHOLDING_ACCT")).append(",");
		sqlCmd.append(getAcct("E_PREPAYMENT_ACCT")).append(",");
		sqlCmd.append(getAcct("E_EXPENSE_ACCT")).append(", ");
		//  PJ_ASSET_ACCT,PJ_WIP_ACCT,
		sqlCmd.append(getAcct("PJ_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("PJ_WIP_ACCT")).append(",");
		//  T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT,
		sqlCmd.append(getAcct("T_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("T_RECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("T_DUE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_CREDIT_ACCT")).append(", ");
		//  B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,
		sqlCmd.append(getAcct("B_INTRANSIT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("B_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTREV_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTEXP_ACCT")).append(",");
		//  B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,
		sqlCmd.append(getAcct("B_UNIDENTIFIED_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTLOSS_ACCT")).append(",");
		//  B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT,
		sqlCmd.append(getAcct("B_REVALUATIONGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_REVALUATIONLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("B_PAYMENTSELECT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_UNALLOCATEDCASH_ACCT")).append(", ");
		//  CH_EXPENSE_ACCT,CH_REVENUE_ACCT,
		sqlCmd.append(getAcct("CH_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CH_REVENUE_ACCT")).append(", ");
		//  UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT,
		sqlCmd.append(getAcct("UNEARNEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIPTS_ACCT")).append(", ");
		//  CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT)
		sqlCmd.append(getAcct("CB_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_CASHTRANSFER_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_RECEIPT_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Default Accounts NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//  GL Categories
		createGLCategory("Standard", "M", true);
		int GL_None = createGLCategory("None", "D", false);
		int GL_GL = createGLCategory("Manual", "M", false);
		int GL_ARI = createGLCategory("AR Invoice", "D", false);
		int GL_ARR = createGLCategory("AR Receipt", "D", false);
		int GL_MM = createGLCategory("Material Management", "D", false);
		int GL_API = createGLCategory("AP Invoice", "D", false);
		int GL_APP = createGLCategory("AP Payment", "D", false);

		//	Base DocumentTypes
		createDocType("GL Journal", "Journal", "GLJ", null, 0, 0, 1000, GL_GL);
		int DT_I = createDocType("AR Invoice", "Invoice", "ARI", null, 0, 0, 100000, GL_ARI);
		int DT_II = createDocType("AR Invoice Indirect", "Invoice", "ARI", null, 0, 0, 200000, GL_ARI);
		createDocType("AR Credit Memo", "Credit Memo", "ARC", null, 0, 0, 300000, GL_ARI);
		createDocType("AR Receipt", "Receipt", "ARR", null, 0, 0, 400000, GL_ARR);
		int DT_S  = createDocType("MM Shipment", "Delivery Note", "MMS", null, 0, 0, 500000, GL_MM);
		int DT_SI = createDocType("MM Shipment Indirect", "Delivery Note", "MMS", null, 0, 0, 600000, GL_MM);
		createDocType("MM Receipt", "Vendor Delivery", "MMR", null, 0, 0, 0, GL_MM);
		createDocType("AP Invoice", "Vendor Invoice", "API", null, 0, 0, 0, GL_API);
		createDocType("AP CreditMemo", "Vendor Credit Memo", "APC", null, 0, 0, 0, GL_API);
		createDocType("AP Payment", "Vendor Payment", "APP", null, 0, 0, 700000, GL_APP);
		createDocType("Purchase Order", "Purchase Order", "POO", null, 0, 0, 800000, GL_None);
		createDocType("Purchase Requisition", "Purchase Requisition", "POR", null, 0, 0, 900000, GL_None);

		//  Order Entry
		createDocType("Quotation", "Binding offer", "SOO", "OB", 0, 0, 10000, GL_None);
		createDocType("Proposal", "Non binding offer", "SOO", "ON", 0, 0, 20000, GL_None);
		createDocType("Prepay Order", "Prepay Order", "SOO", "PR", DT_S, DT_I, 30000, GL_None);
		createDocType("Return Material", "Return Material Authorization", "SOO", "RM", DT_S, DT_I, 40000, GL_None);
		createDocType("Standard Order", "Order Confirmation", "SOO", "SO", DT_S, DT_I, 50000, GL_None);
		createDocType("Credit Order", "Order Confirmation", "SOO", "WI", DT_SI, DT_I, 60000, GL_None);   //  RE
		createDocType("Warehouse Order", "Order Confirmation", "SOO", "WP", DT_S, DT_I, 70000, GL_None);    //  LS
		int DT = createDocType("POS Order", "Order Confirmation", "SOO", "WR", DT_SI, DT_II, 80000, GL_None);    // Bar
		createPreference("C_DocTypeTarget_ID", String.valueOf(DT), 143);

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_AcctSchema1_ID=").append(C_AcctSchema_ID)
			.append(", C_Calendar_ID=").append(C_Calendar_ID)
			.append(" WHERE AD_Client_ID=").append(AD_Client_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//
		return true;
	}   //  createAccounting

	/**
	 *  Get Account ID for key
	 *  @param key key
	 *  @return C_ValidCombination_ID
	 */
	private int getAcct (String key)
	{
		Log.trace(Log.l4_Data, "MSetup.getAcct - " + key);
		//  Element
		int C_ElementValue_ID = m_nap.getC_ElementValue_ID(key);
		Account vc = Account.getDefault(m_AcctSchema, true);	//	optional null
		vc.setAccount_ID(C_ElementValue_ID);
		vc.save(AD_Client_ID, 0);
		int C_ValidCombination_ID = vc.getC_ValidCombination_ID();

		//  Error
		if (C_ValidCombination_ID == 0)
			Log.error("MSetup.getAcct - no account for " + key);
		return C_ValidCombination_ID;
	}   //  getAcct

	/**
	 *  Create GL Category
	 *  @param Name name
	 *  @param CategoryType category type
	 *  @param isDefault is default value
	 *  @return GL_Category_ID
	 */
	private int createGLCategory (String Name, String CategoryType, boolean isDefault)
	{
		int GL_Category_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "GL_Category");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO GL_Category ");
		sqlCmd.append("(GL_Category_ID,").append(m_stdColumns).append(",")
			.append("Name,CategoryType,IsDefault) VALUES (")
			.append(GL_Category_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(Name).append("','").append(CategoryType).append("','")
			.append(isDefault ? "Y" : "N").append("')");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createGLCategory - GL Category NOT created - " + Name);
		//
		return GL_Category_ID;
	}   //  createGLCategory

	/**
	 *  Create Document Types with Sequence
	 *  @param Name name
	 *  @param PrintName print name
	 *  @param DocBaseType document base type
	 *  @param DocSubTypeSO sales order sub type
	 *  @param C_DocTypeShipment_ID shipment doc
	 *  @param C_DocTypeInvoice_ID invoice doc
	 *  @param StartNo start doc no
	 *  @param GL_Category_ID gl category
	 *  @return C_DocType_ID doc type
	 */
	private int createDocType (String Name, String PrintName,
		String DocBaseType, String DocSubTypeSO,
		int C_DocTypeShipment_ID, int C_DocTypeInvoice_ID,
		int StartNo, int GL_Category_ID)
	{
		StringBuffer sqlCmd = null;
		//  Get Sequence
		int AD_Sequence_ID = 0;
		if (StartNo != 0)   //  manual sequenec, if startNo == 0
		{
			AD_Sequence_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Sequence");
			sqlCmd = new StringBuffer ("INSERT INTO AD_Sequence ");
			sqlCmd.append("(AD_Sequence_ID,").append(m_stdColumns).append(",");
			sqlCmd.append("Name,IsAutoSequence,IncrementNo,StartNo,CurrentNext,");
			sqlCmd.append("CurrentNextSys,IsTableID) VALUES (");
			sqlCmd.append(AD_Sequence_ID).append(",").append(m_stdValues).append(",");
			sqlCmd.append("'").append(Name).append("','Y',1,").append(StartNo).append(",").append(StartNo).append(",");
			sqlCmd.append(StartNo/10).append(",'N')");
			int no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createDocType - Sequence NOT created - " + Name);
		}

		//  Get Document Type
		int C_DocType_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_DocType");
		sqlCmd = new StringBuffer ("INSERT INTO	C_DocType ");
		sqlCmd.append("(C_DocType_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,PrintName,DocBaseType,DocSubTypeSO,");
		sqlCmd.append("C_DocTypeShipment_ID,C_DocTypeInvoice_ID,");
		sqlCmd.append("IsDocNoControlled,DocNoSequence_ID,GL_Category_ID,");
		sqlCmd.append("IsPrinted,IsTransferred,DocumentCopies,IsSOTrx)");
		//
		sqlCmd.append(" VALUES (").append(C_DocType_ID).append(",").append(m_stdValues).append(",");
		//  Name,PrintName,DocBaseType,DocSubTypeSO,
		sqlCmd.append("'").append(Name).append("','").append(PrintName).append("','").append(DocBaseType).append("',");
		if (DocSubTypeSO == null || DocSubTypeSO.length() == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append("'").append(DocSubTypeSO).append("',");
		//  C_DocTypeShipment_ID,C_DocTypeInvoice_ID,
		if (C_DocTypeShipment_ID == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append(C_DocTypeShipment_ID).append(",");
		if (C_DocTypeInvoice_ID == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append(C_DocTypeInvoice_ID).append(",");
		//  IsDocNoControlled,DocNoSequence_ID,GL_Category_ID,
		if (AD_Sequence_ID == 0)
			sqlCmd.append("'N',NULL,");
		else
			sqlCmd.append("'Y',").append(AD_Sequence_ID).append(",");
		sqlCmd.append(GL_Category_ID).append(",");
		//  IsPrinted,IsTransferred,DocumentCopies,IsSOTrx)
		sqlCmd.append("'Y',");
		if (DocBaseType.equals("SOO"))
			sqlCmd.append("'N',");
		else
			sqlCmd.append("'Y',");
		sqlCmd.append("0,");
		if (DocBaseType.startsWith("AR") || DocBaseType.equals("MMS") || DocBaseType.equals("SOO"))
			sqlCmd.append("'Y'");
		else
			sqlCmd.append("'N'");
		sqlCmd.append(")");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createDocType - DocType NOT created - " + Name);
		//
		return C_DocType_ID;
	}   //  createDocType

	/*************************************************************************/

	/**
	 *  Create Default main entities.
	 *  - Dimensions & BPGroup, Prod Category)
	 *  - Location, Locator, Warehouse
	 *  - PriceList
	 *  - Cashbook, PaymentTerm
	 *  @param C_Country_ID country
	 *  @param City city
	 *  @param C_Region_ID region
	 *  @return true if created
	 */
	public boolean createEntities (int C_Country_ID, String City, int C_Region_ID)
	{
		Log.trace(Log.l3_Util, "MSetup.createEntries", "C_Country_ID=" + C_Country_ID + ", City=" + City + ", C_Region_ID=" + C_Region_ID);
		m_info.append("\n----\n");
		//
		String defaultName = Msg.translate(m_lang, "Standard");
		String defaultEntry = "'" + defaultName + "',";
		StringBuffer sqlCmd = null;
		int no = 0;

		//	Create Marketing Channel/Campaign
		int C_Channel_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Channel");
		sqlCmd = new StringBuffer("INSERT INTO C_Channel ");
		sqlCmd.append("(C_Channel_ID,Name,");
		sqlCmd.append(m_stdColumns).append(") VALUES (");
		sqlCmd.append(C_Channel_ID).append(",").append(defaultEntry);
		sqlCmd.append(m_stdValues).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Channel NOT inserted");
		int C_Campaign_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Campaign");
		sqlCmd = new StringBuffer("INSERT INTO C_Campaign ");
		sqlCmd.append("(C_Campaign_ID,C_Channel_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,Costs) VALUES (");
		sqlCmd.append(C_Campaign_ID).append(",").append(C_Channel_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Campaign_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Campaign NOT inserted");
		if (m_hasMCampaign)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Campaign_ID=").append(C_Campaign_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='MC'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Campaign NOT updated");
		}

		//	Create Sales Region
		int C_SalesRegion_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_SalesRegion");
		sqlCmd = new StringBuffer ("INSERT INTO C_SalesRegion ");
		sqlCmd.append("(C_SalesRegion_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsSummary) VALUES (");
		sqlCmd.append(C_SalesRegion_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_SalesRegion_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRegion NOT inserted");
		if (m_hasSRegion)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_SalesRegion_ID=").append(C_SalesRegion_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='SR'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement SalesRegion NOT updated");
		}

		/**
		 *  Business Partner
		 */
		//  Create BP Group
		int C_BP_Group_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BP_Group");
		sqlCmd = new StringBuffer ("INSERT INTO C_BP_Group ");
		sqlCmd.append("(C_BP_Group_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault) VALUES ( ");
		sqlCmd.append(C_BP_Group_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BP_Group_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BP Group NOT inserted");

		//	Create BPartner
		int C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_BP_Group_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BPartner_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BPartner NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("C_BPartner_ID=").append(C_BPartner_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
		sqlCmd.append(" AND ElementType='BP'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element BPartner NOT updated");
		createPreference("C_BPartner_ID", String.valueOf(C_BPartner_ID), 143);

		/**
		 *  Product
		 */
		//  Create Product Category
		int M_Product_Category_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Product_Category");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product_Category ");
		sqlCmd.append("(M_Product_Category_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault,PlannedMargin) VALUES (");
		sqlCmd.append(M_Product_Category_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y',0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_Category_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product Category NOT inserted");

		//  UOM (EA)
		int C_UOM_ID = 100;

		//  TaxCategory
		int C_TaxCategory_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_TaxCategory");
		sqlCmd = new StringBuffer ("INSERT INTO C_TaxCategory ");
		sqlCmd.append("(C_TaxCategory_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,IsDefault) VALUES (");
		sqlCmd.append(C_TaxCategory_ID).append(",").append(m_stdValues).append(", ");
		if (C_Country_ID == 100)    // US
			sqlCmd.append("'Sales Tax','Y')");
		else
			sqlCmd.append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - TaxCategory NOT inserted");

		//  Tax - Zero Rate
		int C_Tax_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Tax");
		sqlCmd = new StringBuffer ("INSERT INTO C_Tax ");
		sqlCmd.append("(C_Tax_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,Rate,C_TaxCategory_ID,C_Country_ID,TO_Country_ID,ValidFrom,IsDefault) VALUES (");
		sqlCmd.append(C_Tax_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append("0,").append(C_TaxCategory_ID).append(",")
			.append(C_Country_ID).append(",").append(C_Country_ID)
			.append(",TO_DATE('1990-01-01','YYYY-MM-DD'),'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Tax_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Tax NOT inserted");

		//	Create Product
		int M_Product_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Product");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product ");
		sqlCmd.append("(M_Product_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_UOM_ID,M_Product_Category_ID,C_TaxCategory_ID) VALUES (");
		sqlCmd.append(M_Product_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry);
		sqlCmd.append(C_UOM_ID).append(",").append(M_Product_Category_ID).append(",");
		sqlCmd.append(C_TaxCategory_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("M_Product_ID=").append(M_Product_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
		sqlCmd.append(" AND ElementType='PR'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element Product NOT updated");

		/**
		 *  Warehouse
		 */
		//  Location (Company)
		int C_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		sqlCmd = new StringBuffer ("UPDATE AD_OrgInfo SET C_Location_ID=");
		sqlCmd.append(C_Location_ID).append(" WHERE AD_Org_ID=").append(AD_Org_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Location NOT inserted");
		createPreference("C_Country_ID", String.valueOf(C_Country_ID), 0);

		//  Default Warehouse
		int M_Warehouse_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Warehouse");
		sqlCmd = new StringBuffer ("INSERT INTO M_Warehouse ");
		sqlCmd.append("(M_Warehouse_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Location_ID,Separator) VALUES (");
		sqlCmd.append(M_Warehouse_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Location_ID).append(",'-')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Warehouse NOT inserted");

		//   Locator
		int M_Locator_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Locator");
		sqlCmd = new StringBuffer ("INSERT INTO M_Locator ");
		sqlCmd.append("(M_Locator_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,M_Warehouse_ID,X,Y,Z,PriorityNo,IsDefault) VALUES (");
		sqlCmd.append(M_Locator_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(M_Warehouse_ID).append(",0,0,0,50,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Locator NOT inserted");

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_BPartnerCashTrx_ID=").append(C_BPartner_ID);
		sqlCmd.append(",M_ProductFreight_ID=").append(M_Product_ID);
//		sqlCmd.append("C_UOM_Volume_ID=");
//		sqlCmd.append(",C_UOM_Weight_ID=");
//		sqlCmd.append(",C_UOM_Length_ID=");
//		sqlCmd.append(",C_UOM_Time_ID=");
		sqlCmd.append(" WHERE AD_Client_ID=").append(AD_Client_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createEntities - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		/**
		 *  Other
		 */
		//  PriceList
		int M_PriceList_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_PriceList");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList ");
		sqlCmd.append("(M_PriceList_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID,IsDefault) VALUES (");
		sqlCmd.append(M_PriceList_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(",'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList NOT inserted");
		//  DiscountSchema
		int M_DiscountSchema_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_DiscountSchema");
		sqlCmd = new StringBuffer ("INSERT INTO M_DiscountSchema ");
		sqlCmd.append("(M_DiscountSchema_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,DiscountType, FlatDiscount) VALUES (");
		sqlCmd.append(M_DiscountSchema_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append("SysDate,'P',0)");	//	PriceList
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - DiscountSchema NOT inserted");
		//  PriceList Version
		int M_PriceList_Version_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_PriceList_Version");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList_Version ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,M_PriceList_ID,M_DiscountSchema_ID) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY-MM-DD'),SysDate,").append(M_PriceList_ID)
			.append(",").append(M_DiscountSchema_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList_Version NOT inserted");
		//  ProductPrice
		sqlCmd = new StringBuffer ("INSERT INTO M_ProductPrice ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("M_Product_ID,PriceList,PriceStd,PriceLimit) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(M_Product_ID).append(",1,1,1)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - ProductPrice NOT inserted");

		//  Location for Standard BP
		C_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		int C_BPartner_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(City).append("',").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (Standard) NOT inserted");

		//	Create Sales Rep for Client-User
		C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_U_Name).append("','").append(AD_User_U_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_U_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (User) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_U_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (User) NOT updated");

		//  Location for Client-User
		C_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		C_BPartner_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(City).append("',").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (User) NOT inserted");


		//	Create Sales Rep for Client-Admin
		C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_Name).append("','").append(AD_User_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (Admin) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (Admin) NOT updated");

		//  Location for Client-Admin
		C_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		C_BPartner_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(City).append("',").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (Admin) NOT inserted");


		//  Payment Term
		int C_PaymentTerm_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_PaymentTerm");
		sqlCmd = new StringBuffer ("INSERT INTO C_PaymentTerm ");
		sqlCmd.append("(C_PaymentTerm_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,NetDays,GraceDays,DiscountDays,Discount,DiscountDays2,Discount2,IsDefault) VALUES (");
		sqlCmd.append(C_PaymentTerm_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'Immediate','Immediate',0,0,0,0,0,0,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PaymentTerm NOT inserted");

		//  Project Cycle
		C_Cycle_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Cycle");
		sqlCmd = new StringBuffer ("INSERT INTO C_Cycle ");
		sqlCmd.append("(C_Cycle_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_Cycle_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Cycle NOT inserted");

		/**
		 *  Organization level data	===========================================
		 */

		//	Create Default Project
		int C_Project_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Project");
		sqlCmd = new StringBuffer ("INSERT INTO C_Project ");
		sqlCmd.append("(C_Project_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Currency_ID,IsSummary) VALUES (");
		sqlCmd.append(C_Project_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Currency_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Project_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Project NOT inserted");
		//  Default Project
		if (m_hasProject)
		{
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Project_ID=").append(C_Project_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='PJ'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Project NOT updated");
		}

		//  CashBook
		int C_CashBook_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_CashBook");
		sqlCmd = new StringBuffer ("INSERT INTO C_CashBook ");
		sqlCmd.append("(C_CashBook_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_CashBook_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_CashBook_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - CashBook NOT inserted");

		return true;
	}   //  createEntities

	/**
	 *  Create Preference
	 *  @param Attribute attribute
	 *  @param Value value
	 *  @param AD_Window_ID window
	 */
	private void createPreference (String Attribute, String Value, int AD_Window_ID)
	{
		int AD_Preference_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Preference");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO AD_Preference ");
		sqlCmd.append("(AD_Preference_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Attribute,Value,AD_Window_ID) VALUES (");
		sqlCmd.append(AD_Preference_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(Attribute).append("','").append(Value).append("',");
		if (AD_Window_ID == 0)
			sqlCmd.append("NULL)");
		else
			sqlCmd.append(AD_Window_ID).append(")");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createPreference - Preference NOT inserted - " + Attribute);
	}   //  createPreference

	/*************************************************************************/

	/**
	 *  Get Client
	 *  @return AD_Client_ID
	 */
	public int getAD_Client_ID()
	{
		return AD_Client_ID;
	}
	public int getAD_Org_ID()
	{
		return AD_Org_ID;
	}
	public int getAD_User_ID()
	{
		return AD_User_ID;
	}
	public int getAD_Role_ID()
	{
		return AD_Role_ID;
	}
	public int getC_AcctSchema_ID()
	{
		return C_AcctSchema_ID;
	}
	public String getInfo()
	{
		return m_info.toString();
	}
}   //  MSetup
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
package org.compiere.model;

import java.util.*;
import java.sql.*;
import java.io.*;

import org.compiere.util.*;

/**
 * Initial Setup Model
 *
 * @author Jorg Janke
 * @version $Id: MSetup.java,v 1.25 2003/07/22 18:49:02 jjanke Exp $
 */
public final class MSetup
{
	/**
	 *  Constructor
	 *  @param ctx context
	 *  @param WindowNo window
	 */
	public MSetup(Properties ctx, int WindowNo)
	{
		m_ctx = ctx;
		m_lang = Env.getAD_Language(m_ctx);
		m_WindowNo = WindowNo;
	}   //  MSetup

	private Properties      m_ctx;
	private String          m_lang;
	private int             m_WindowNo;
	private StringBuffer    m_info;
	//
	private String          m_clientName;
	private String          m_orgName;
	//
	private String          m_stdColumns = "AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy";
	private String          m_stdValues;
	private String          m_stdValuesOrg;
	//
	private NaturalAccountMap m_nap = null;
	//
	private final String    CompiereSys = "N";           //  Should NOT be changed
	private int             AD_Client_ID = 1000000;     //  initial Client_ID
	//
	private int     AD_Org_ID;
	private int     AD_User_ID;
	private String  AD_User_Name;
	private int     AD_User_U_ID;
	private String  AD_User_U_Name;
	private int     AD_Role_ID;
	private int     C_Calendar_ID;
	private int     C_AcctSchema_ID;
	private int     C_Currency_ID;
	private int     AD_Tree_Account_ID;
	private int     C_Cycle_ID;
	//
	private boolean         m_hasProject = false;
	private boolean         m_hasMCampaign = false;
	private boolean         m_hasSRegion = false;
	private AcctSchema      m_AcctSchema = null;


	/**
	 *  Create Client Info.
	 *  - Client, Trees, Org, Role, User, User_Role
	 *  @param clientName client name
	 *  @param orgName org name
	 *  @param userClient user id client
	 *  @param userOrg user id org
	 *  @return true if created
	 */
	public boolean createClient (String clientName, String orgName,
		String userClient, String userOrg)
	{
		Log.trace(Log.l3_Util, "MSetup.createClient");
		//  info header
		m_info = new StringBuffer();
		//  Standarc columns
		String name = null;
		String sql = null;
		int no = 0;

		/**
		 *  Create Client
		 */
		Env.setContext(m_ctx, "#CompiereSys", CompiereSys);
		AD_Client_ID = DB.getKeyNextNo (AD_Client_ID, CompiereSys, "AD_Client");
		Env.setContext(m_ctx, m_WindowNo, "AD_Client_ID", AD_Client_ID);

		//	Standard Values
		m_stdValues = String.valueOf(AD_Client_ID) + ",0,'Y',SysDate,0,SysDate,0";

		//	Create Client
		name = clientName;
		if (name == null || name.length() == 0)
			name = "newClient";
		name = name.trim();
		m_clientName = name;
		sql = "INSERT INTO AD_Client(" + m_stdColumns + ",Value,Name,Description)"
			+ " VALUES (" + m_stdValues + ",'" + name + "','" + name + "','" + name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Client NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info - Client
		m_info.append(Msg.translate(m_lang, "AD_Client_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Trees
		 */
		String sql2 = null;
		if (Env.isBaseLanguage(m_lang, "AD_Ref_List"))	//	Get TreeTypes & Name
			sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=120";
		else
			sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Reference_ID=120 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID";

		//  Tree IDs
		int AD_Tree_Org_ID=0, AD_Tree_BPartner_ID=0, AD_Tree_Project_ID=0,
			AD_Tree_SalesRegion_ID=0, AD_Tree_Product_ID=0;

		try
		{
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next())
			{
				String value = rs.getString(1);
				int AD_Tree_ID = 0;
				if (value.equals("OO"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Org_ID = AD_Tree_ID;
				}
				else if (value.equals("BP"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_BPartner_ID = AD_Tree_ID;
				}
				else if (value.equals("PJ"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Project_ID = AD_Tree_ID;
				}
				else if (value.equals("SR"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_SalesRegion_ID = AD_Tree_ID;
				}
				else if (value.equals("PR"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Product_ID = AD_Tree_ID;
				}
				else if (value.endsWith("EV"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Account_ID = AD_Tree_ID;
				}
				else if (value.equals("MM"))	//	No Menu
					;
				else
					//	PC (Product Category), BB (BOM), MC (Marketing Campaign), AY (Activity)
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");

				//
				if (AD_Tree_ID != 0)
				{
					name = m_clientName + " " + rs.getString(2);
					sql = "INSERT INTO AD_Tree(" + m_stdColumns + ",AD_Tree_ID,Name,Description,TreeType)"
						+ " VALUES (" + m_stdValues + "," + AD_Tree_ID + ",'" + name + "','" + name + "','" + value + "')";
					no = DB.executeUpdate(sql);
					if (no == 1)
						m_info.append(Msg.translate(m_lang, "AD_Tree_ID")).append("=").append(name).append("\n");
					else
						Log.error("MSetup.createClient - Tree NOT created: " + name);
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e1)
		{
			Log.error ("MSetup.createClient - Trees", e1);
		}

		//	Get Primary Tree
		int AD_Tree_Menu_ID = 10;	//	hardcoded

		//	Create ClientInfo
		sql = "INSERT INTO AD_ClientInfo(" + m_stdColumns + ",Acct2_Active,Acct3_Active,"
			+ "AD_Tree_Menu_ID,AD_Tree_Org_ID,AD_Tree_BPartner_ID,AD_Tree_Project_ID,AD_Tree_SalesRegion_ID,AD_Tree_Product_ID)"
			+ " VALUES (" + m_stdValues + ",'N','N',"
			+ AD_Tree_Menu_ID + "," + AD_Tree_Org_ID + "," + AD_Tree_BPartner_ID + "," + AD_Tree_Project_ID + "," + AD_Tree_SalesRegion_ID + "," + AD_Tree_Product_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - ClientInfo NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
	//	m_info.append(Msg.translate(m_lang, "AD_ClientInfo")).append("\n");

		/**
		 *  Create Org
		 */
		AD_Org_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Org");
		m_stdValuesOrg = AD_Client_ID + "," + AD_Org_ID + ",'Y',SysDate,0,SysDate,0";

		name = orgName;
		if (name == null || name.length() == 0)
			name = "newOrg";
		m_orgName = name;
		sql = "INSERT INTO AD_Org (" + m_stdColumns + ",Value,Name,IsSummary)"
			+ " VALUES (" +	m_stdValuesOrg + ",'" + name + "','" + name + "','N')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Org NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_Org_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Roles
		 *  - Admin
		 *  - User
		 */
		name = m_clientName + " Admin";
		AD_Role_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Role");
		sql = "INSERT INTO AD_Role(" + m_stdColumns + ",AD_Role_ID,"
			+ "Name,Description,UserLevel,ClientList,OrgList)"
			+ " VALUES (" + m_stdValues + "," + AD_Role_ID + ","
			+ "'" + name + "','" + name + "',' CO','" + AD_Client_ID + "','0," + AD_Org_ID + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin Role A NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  OrgAccess x,0
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValues + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_OrgAccess 0 NOT created");
		//  OrgAccess x,y
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValuesOrg + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_OrgAccess NOT created");
		//  Info - Admin Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		//
		name = m_clientName + " User";
		int AD_Role_ID_U = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Role");
		sql = "INSERT INTO AD_Role(" + m_stdColumns + ",AD_Role_ID,"
			+ "Name,Description,UserLevel,ClientList,OrgList)"
			+ " VALUES (" + m_stdValues + "," + AD_Role_ID_U + ","
			+ "'" + name + "','" + name + "','  O','" + AD_Client_ID + "','" + AD_Org_ID + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - User Role A NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  OrgAccess x,y
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValuesOrg + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - User Role_OrgAccess NOT created");
		//  Info - Client Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Users
		 *  - Client
		 *  - Org
		 */
		name = userClient;
		if (name == null || name.length() == 0)
			name = m_clientName + "Client";
		AD_User_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_User");
		AD_User_Name = name;
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + ","
			+ "'" + AD_User_Name + "','" + AD_User_Name + "','" + AD_User_Name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin User NOT inserted - " + AD_User_Name;
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_Name).append("/").append(AD_User_Name).append("\n");

		name = userOrg;
		if (name == null || name.length() == 0)
			name = m_clientName + "Org";
		AD_User_U_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_User");
		AD_User_U_Name = name;
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + ","
			+ "'" + AD_User_U_Name + "','" + AD_User_U_Name + "','" + AD_User_U_Name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Org User NOT inserted - " + AD_User_U_Name;
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_U_Name).append("/").append(AD_User_U_Name).append("\n");

		/**
		 *  Create User-Role
		 */
		//  ClientUser          - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+Admin NOT inserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+User NOT inserted");
		//  OrgUser             - User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole OrgUser+Org NOT inserted");
		//  SuperUser(100)      - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + ",100," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole SuperUser+Admin NOT inserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + ",100," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole SuperUser+User NOT inserted");

		return true;
	}   //  createClient



	/*************************************************************************/

	/**
	 *  Create Accounting elements.
	 *  - Calendar
	 *  - Account Trees
	 *  - Account Values
	 *  - Accounting Schema
	 *  - Default Accounts
	 *
	 *  @param newC_Currency_ID currency
	 *  @param curName currency name
	 *  @param hasProduct has product segment
	 *  @param hasBPartner has bp segment
	 *  @param hasProject has project segment
	 *  @param hasMCampaign has campaign segment
	 *  @param hasSRegion has sales region segment
	 *  @param AccountingFile file name of accounting file
	 *  @return true if created
	 */
	public boolean createAccounting(int newC_Currency_ID, String curName,
		boolean hasProduct, boolean hasBPartner, boolean hasProject,
		boolean hasMCampaign, boolean hasSRegion,
		File AccountingFile)
	{
		Log.trace(Log.l3_Util, "MSetup.createAccount");
		//
		C_Currency_ID = newC_Currency_ID;
		m_hasProject = hasProject;
		m_hasMCampaign = hasMCampaign;
		m_hasSRegion = hasSRegion;

		//  Standard variables
		m_info = new StringBuffer();
		String name = null;
		StringBuffer sqlCmd = null;
		int no = 0;

		/**
		 *  Create Calendar
		 */
		C_Calendar_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Calendar");
		name = m_clientName + " " + Msg.translate(m_lang, "C_Calendar_ID");
		sqlCmd = new StringBuffer("INSERT INTO C_Calendar(");
		sqlCmd.append(m_stdColumns).append(",C_Calendar_ID,Name)").append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_Calendar_ID).append(",'").append(name).append("')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Calendar NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_Calendar_ID")).append("=").append(name).append("\n");

		//  Year
		int C_Year_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Year");
		sqlCmd = new StringBuffer ("INSERT INTO C_Year ");
		sqlCmd.append("(C_Year_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Year,C_Calendar_ID) VALUES (");
		sqlCmd.append(C_Year_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY'),").append(C_Calendar_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createAccounting - Year NOT inserted");
		/** @todo Create Periods */


		//	Create Account Elements
		int C_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Element");
		name = m_clientName + " " + Msg.translate(m_lang, "Account_ID");
		sqlCmd = new StringBuffer ("INSERT INTO C_ELEMENT(");
		sqlCmd.append(m_stdColumns).append(",C_Element_ID,Name,Description,")
			.append("VFormat,ElementType,IsBalancing,IsNaturalAccount,AD_Tree_ID)").append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_Element_ID).append(",'").append(name).append("','").append(name).append("',")
			.append("NULL,'A','N','Y',").append(AD_Tree_Account_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Acct Element NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		m_info.append(Msg.translate(m_lang, "C_Element_ID")).append("=").append(name).append("\n");

		//	Create Account Values
		m_nap = new NaturalAccountMap(m_ctx);
		String errMsg = m_nap.parseFile(AccountingFile);
		if (errMsg.length() != 0)
		{
			Log.error(errMsg);
			m_info.append(errMsg);
			return false;
		}
		if (m_nap.createAccounts(AD_Client_ID, AD_Org_ID, C_Element_ID))
			m_info.append(Msg.translate(m_lang, "C_ElementValue_ID")).append(" # ").append(m_nap.size()).append("\n");
		else
		{
			String err = "MSetup.createAccounting - Acct Element Values NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		int C_ElementValue_ID = m_nap.getC_ElementValue_ID("DEFAULT_ACCT");
		Log.trace(Log.l4_Data, "C_ElementValue_ID=" + C_ElementValue_ID);

		/**
		 *  Create AccountingSchema
		 */
		C_AcctSchema_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema");
		//
		String GAAP = "US";				//	AD_Reference_ID=123
		String CostingMethod = "A";		//	AD_Reference_ID=122
		name = m_clientName + " " + GAAP + "/" + CostingMethod + "/" + curName;
		//
		sqlCmd = new StringBuffer ("INSERT INTO C_AcctSchema(");
		sqlCmd.append(m_stdColumns).append(",C_AcctSchema_ID,Name,")
			.append("GAAP,IsAccrual,CostingMethod,C_Currency_ID,")
			.append("AutoPeriodControl,Separator,HasAlias,HasCombination)")
			.append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",'").append(name).append("',")
			.append("'").append(GAAP).append("','Y','").append(CostingMethod).append("',").append(C_Currency_ID).append(",")
			.append("'N','-','Y','N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - AcctSchema NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_AcctSchema_ID")).append("=").append(name).append("\n");

		/**
		 *  Create AccountingSchema Elements (Structure)
		 */
		String sql2 = null;
		if (Env.isBaseLanguage(m_lang, "AD_Reference"))	//	Get TreeTypes & Name
			sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=181";
		else
			sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Reference_ID=181 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID";
		//
		int Element_OO=0, Element_AC=0, Element_PR=0, Element_BP=0, Element_PJ=0,
			Element_MC=0, Element_SR=0;
		try
		{
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next())
			{
				String ElementType = rs.getString(1);
				name = rs.getString(2);
				//
				String IsMandatory = null;
				String IsBalanced = "N";
				int SeqNo = 0;
				int C_AcctSchema_Element_ID = 0;

				if (ElementType.equals("OO"))
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_OO = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					IsBalanced = "Y";
					SeqNo = 10;
				}
				else if (ElementType.equals("AC"))
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_AC = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					SeqNo = 20;
				}
				else if (ElementType.equals("PR") && hasProduct)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_PR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 30;
				}
				else if (ElementType.equals("BP") && hasBPartner)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_BP = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 40;
				}
				else if (ElementType.equals("PJ") && hasProject)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_PJ = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 50;
				}
				else if (ElementType.equals("MC") && hasMCampaign)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_MC = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 60;
				}
				else if (ElementType.equals("SR") && hasSRegion)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_SR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 70;
				}
				//	Not OT, LF, LT, U1, U2, AY

				if (IsMandatory != null)
				{
					sqlCmd = new StringBuffer ("INSERT INTO C_AcctSchema_Element(");
					sqlCmd.append(m_stdColumns).append(",C_AcctSchema_Element_ID,C_AcctSchema_ID,")
						.append("ElementType,Name,SeqNo,IsMandatory,IsBalanced) VALUES (");
					sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_Element_ID).append(",").append(C_AcctSchema_ID).append(",")
						.append("'").append(ElementType).append("','").append(name).append("',").append(SeqNo).append(",'")
						.append(IsMandatory).append("','").append(IsBalanced).append("')");
					no = DB.executeUpdate(sqlCmd.toString());
					if (no == 1)
						m_info.append(Msg.translate(m_lang, "C_AcctSchema_Element_ID")).append("=").append(name).append("\n");

					/** Default value for mandatory elements: OO and AC */
					if (ElementType.equals("OO"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET Org_ID=");
						sqlCmd.append(AD_Org_ID).append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Org in AcctSchamaElement NOT updated");
					}
					if (ElementType.equals("AC"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET C_ElementValue_ID=");
						sqlCmd.append(C_ElementValue_ID).append(", C_Element_ID=").append(C_Element_ID);
						sqlCmd.append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Account in AcctSchamaElement NOT updated");
					}
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e1)
		{
			Log.error ("MSetup.createAccounting - Elements", e1);
		}
		//  Create AcctSchema
		m_AcctSchema = new AcctSchema (C_AcctSchema_ID);


		//  Create GL Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_GL (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "USESUSPENSEBALANCING,SUSPENSEBALANCING_ACCT,"
			+ "USESUSPENSEERROR,SUSPENSEERROR_ACCT,"
			+ "USECURRENCYBALANCING,CURRENCYBALANCING_ACCT,"
			+ "RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,"
			+ "INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT,"
			+ "PPVOFFSET_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEBALANCING_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEERROR_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("CURRENCYBALANCING_ACCT")).append(",");
		//  RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,
		sqlCmd.append(getAcct("RETAINEDEARNING_ACCT")).append(",");
		sqlCmd.append(getAcct("INCOMESUMMARY_ACCT")).append(",");
		//  INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT)
		sqlCmd.append(getAcct("INTERCOMPANYDUETO_ACCT")).append(",");
		sqlCmd.append(getAcct("INTERCOMPANYDUEFROM_ACCT")).append(",");
		sqlCmd.append(getAcct("PPVOFFSET_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - GL Accounts NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//	Create Std Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_DEFAULT (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT, "
			+ "P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT, "
			+ "P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT, "
			+ "C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT, "
			+ "V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT, "
			+ "PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT, "
			+ "UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT, "
			+ "WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT, "
			+ "PJ_ASSET_ACCT,PJ_WIP_ACCT,"
			+ "T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT, "
			+ "B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,"
			+ "B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,"
			+ "B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT, "
			+ "CH_EXPENSE_ACCT,CH_REVENUE_ACCT, "
			+ "UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT, "
			+ "CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",");
		//  W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT
		sqlCmd.append(getAcct("W_INVENTORY_ACCT")).append(",");
		sqlCmd.append(getAcct("W_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("W_REVALUATION_ACCT")).append(",");
		sqlCmd.append(getAcct("W_INVACTUALADJUST_ACCT")).append(", ");
		//  P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT,
		sqlCmd.append(getAcct("P_REVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("P_COGS_ACCT")).append(", ");
		//  P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT,
		sqlCmd.append(getAcct("P_PURCHASEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_INVOICEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTREC_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTGRANT_ACCT")).append(", ");
		//  C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("C_RECEIVABLE_ACCT")).append(",");
		sqlCmd.append(getAcct("C_PREPAYMENT_ACCT")).append(", ");
		//  V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("V_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("V_LIABILITY_SERVICES_ACCT")).append(",");
		sqlCmd.append(getAcct("V_PREPAYMENT_ACCT")).append(", ");
		//  PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT,
		sqlCmd.append(getAcct("PAYDISCOUNT_EXP_ACCT")).append(",");
		sqlCmd.append(getAcct("PAYDISCOUNT_REV_ACCT")).append(",");
		sqlCmd.append(getAcct("WRITEOFF_ACCT")).append(", ");
		//  UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT,
		sqlCmd.append(getAcct("UNREALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("UNREALIZEDLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDLOSS_ACCT")).append(", ");
		//  WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT,
		sqlCmd.append(getAcct("WITHHOLDING_ACCT")).append(",");
		sqlCmd.append(getAcct("E_PREPAYMENT_ACCT")).append(",");
		sqlCmd.append(getAcct("E_EXPENSE_ACCT")).append(", ");
		//  PJ_ASSET_ACCT,PJ_WIP_ACCT,
		sqlCmd.append(getAcct("PJ_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("PJ_WIP_ACCT")).append(",");
		//  T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT,
		sqlCmd.append(getAcct("T_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("T_RECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("T_DUE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_CREDIT_ACCT")).append(", ");
		//  B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,
		sqlCmd.append(getAcct("B_INTRANSIT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("B_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTREV_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTEXP_ACCT")).append(",");
		//  B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,
		sqlCmd.append(getAcct("B_UNIDENTIFIED_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTLOSS_ACCT")).append(",");
		//  B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT,
		sqlCmd.append(getAcct("B_REVALUATIONGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_REVALUATIONLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("B_PAYMENTSELECT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_UNALLOCATEDCASH_ACCT")).append(", ");
		//  CH_EXPENSE_ACCT,CH_REVENUE_ACCT,
		sqlCmd.append(getAcct("CH_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CH_REVENUE_ACCT")).append(", ");
		//  UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT,
		sqlCmd.append(getAcct("UNEARNEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIPTS_ACCT")).append(", ");
		//  CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT)
		sqlCmd.append(getAcct("CB_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_CASHTRANSFER_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_RECEIPT_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Default Accounts NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//  GL Categories
		createGLCategory("Standard", "M", true);
		int GL_None = createGLCategory("None", "D", false);
		int GL_GL = createGLCategory("Manual", "M", false);
		int GL_ARI = createGLCategory("AR Invoice", "D", false);
		int GL_ARR = createGLCategory("AR Receipt", "D", false);
		int GL_MM = createGLCategory("Material Management", "D", false);
		int GL_API = createGLCategory("AP Invoice", "D", false);
		int GL_APP = createGLCategory("AP Payment", "D", false);

		//	Base DocumentTypes
		createDocType("GL Journal", "Journal", "GLJ", null, 0, 0, 1000, GL_GL);
		int DT_I = createDocType("AR Invoice", "Invoice", "ARI", null, 0, 0, 100000, GL_ARI);
		int DT_II = createDocType("AR Invoice Indirect", "Invoice", "ARI", null, 0, 0, 200000, GL_ARI);
		createDocType("AR Credit Memo", "Credit Memo", "ARC", null, 0, 0, 300000, GL_ARI);
		createDocType("AR Receipt", "Receipt", "ARR", null, 0, 0, 400000, GL_ARR);
		int DT_S  = createDocType("MM Shipment", "Delivery Note", "MMS", null, 0, 0, 500000, GL_MM);
		int DT_SI = createDocType("MM Shipment Indirect", "Delivery Note", "MMS", null, 0, 0, 600000, GL_MM);
		createDocType("MM Receipt", "Vendor Delivery", "MMR", null, 0, 0, 0, GL_MM);
		createDocType("AP Invoice", "Vendor Invoice", "API", null, 0, 0, 0, GL_API);
		createDocType("AP CreditMemo", "Vendor Credit Memo", "APC", null, 0, 0, 0, GL_API);
		createDocType("AP Payment", "Vendor Payment", "APP", null, 0, 0, 700000, GL_APP);
		createDocType("Purchase Order", "Purchase Order", "POO", null, 0, 0, 800000, GL_None);
		createDocType("Purchase Requisition", "Purchase Requisition", "POR", null, 0, 0, 900000, GL_None);

		//  Order Entry
		createDocType("Quotation", "Binding offer", "SOO", "OB", 0, 0, 10000, GL_None);
		createDocType("Proposal", "Non binding offer", "SOO", "ON", 0, 0, 20000, GL_None);
		createDocType("Prepay Order", "Prepay Order", "SOO", "PR", DT_S, DT_I, 30000, GL_None);
		createDocType("Return Material", "Return Material Authorization", "SOO", "RM", DT_S, DT_I, 40000, GL_None);
		createDocType("Standard Order", "Order Confirmation", "SOO", "SO", DT_S, DT_I, 50000, GL_None);
		createDocType("Credit Order", "Order Confirmation", "SOO", "WI", DT_SI, DT_I, 60000, GL_None);   //  RE
		createDocType("Warehouse Order", "Order Confirmation", "SOO", "WP", DT_S, DT_I, 70000, GL_None);    //  LS
		int DT = createDocType("POS Order", "Order Confirmation", "SOO", "WR", DT_SI, DT_II, 80000, GL_None);    // Bar
		createPreference("C_DocTypeTarget_ID", String.valueOf(DT), 143);

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_AcctSchema1_ID=").append(C_AcctSchema_ID)
			.append(", C_Calendar_ID=").append(C_Calendar_ID)
			.append(" WHERE AD_Client_ID=").append(AD_Client_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//
		return true;
	}   //  createAccounting

	/**
	 *  Get Account ID for key
	 *  @param key key
	 *  @return C_ValidCombination_ID
	 */
	private int getAcct (String key)
	{
		Log.trace(Log.l4_Data, "MSetup.getAcct - " + key);
		//  Element
		int C_ElementValue_ID = m_nap.getC_ElementValue_ID(key);
		Account vc = Account.getDefault(m_AcctSchema, true);	//	optional null
		vc.setAccount_ID(C_ElementValue_ID);
		vc.save(AD_Client_ID, 0);
		int C_ValidCombination_ID = vc.getC_ValidCombination_ID();

		//  Error
		if (C_ValidCombination_ID == 0)
			Log.error("MSetup.getAcct - no account for " + key);
		return C_ValidCombination_ID;
	}   //  getAcct

	/**
	 *  Create GL Category
	 *  @param Name name
	 *  @param CategoryType category type
	 *  @param isDefault is default value
	 *  @return GL_Category_ID
	 */
	private int createGLCategory (String Name, String CategoryType, boolean isDefault)
	{
		int GL_Category_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "GL_Category");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO GL_Category ");
		sqlCmd.append("(GL_Category_ID,").append(m_stdColumns).append(",")
			.append("Name,CategoryType,IsDefault) VALUES (")
			.append(GL_Category_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(Name).append("','").append(CategoryType).append("','")
			.append(isDefault ? "Y" : "N").append("')");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createGLCategory - GL Category NOT created - " + Name);
		//
		return GL_Category_ID;
	}   //  createGLCategory

	/**
	 *  Create Document Types with Sequence
	 *  @param Name name
	 *  @param PrintName print name
	 *  @param DocBaseType document base type
	 *  @param DocSubTypeSO sales order sub type
	 *  @param C_DocTypeShipment_ID shipment doc
	 *  @param C_DocTypeInvoice_ID invoice doc
	 *  @param StartNo start doc no
	 *  @param GL_Category_ID gl category
	 *  @return C_DocType_ID doc type
	 */
	private int createDocType (String Name, String PrintName,
		String DocBaseType, String DocSubTypeSO,
		int C_DocTypeShipment_ID, int C_DocTypeInvoice_ID,
		int StartNo, int GL_Category_ID)
	{
		StringBuffer sqlCmd = null;
		//  Get Sequence
		int AD_Sequence_ID = 0;
		if (StartNo != 0)   //  manual sequenec, if startNo == 0
		{
			AD_Sequence_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Sequence");
			sqlCmd = new StringBuffer ("INSERT INTO AD_Sequence ");
			sqlCmd.append("(AD_Sequence_ID,").append(m_stdColumns).append(",");
			sqlCmd.append("Name,IsAutoSequence,IncrementNo,StartNo,CurrentNext,");
			sqlCmd.append("CurrentNextSys,IsTableID) VALUES (");
			sqlCmd.append(AD_Sequence_ID).append(",").append(m_stdValues).append(",");
			sqlCmd.append("'").append(Name).append("','Y',1,").append(StartNo).append(",").append(StartNo).append(",");
			sqlCmd.append(StartNo/10).append(",'N')");
			int no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createDocType - Sequence NOT created - " + Name);
		}

		//  Get Document Type
		int C_DocType_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_DocType");
		sqlCmd = new StringBuffer ("INSERT INTO	C_DocType ");
		sqlCmd.append("(C_DocType_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,PrintName,DocBaseType,DocSubTypeSO,");
		sqlCmd.append("C_DocTypeShipment_ID,C_DocTypeInvoice_ID,");
		sqlCmd.append("IsDocNoControlled,DocNoSequence_ID,GL_Category_ID,");
		sqlCmd.append("IsPrinted,IsTransferred,DocumentCopies,IsSOTrx)");
		//
		sqlCmd.append(" VALUES (").append(C_DocType_ID).append(",").append(m_stdValues).append(",");
		//  Name,PrintName,DocBaseType,DocSubTypeSO,
		sqlCmd.append("'").append(Name).append("','").append(PrintName).append("','").append(DocBaseType).append("',");
		if (DocSubTypeSO == null || DocSubTypeSO.length() == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append("'").append(DocSubTypeSO).append("',");
		//  C_DocTypeShipment_ID,C_DocTypeInvoice_ID,
		if (C_DocTypeShipment_ID == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append(C_DocTypeShipment_ID).append(",");
		if (C_DocTypeInvoice_ID == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append(C_DocTypeInvoice_ID).append(",");
		//  IsDocNoControlled,DocNoSequence_ID,GL_Category_ID,
		if (AD_Sequence_ID == 0)
			sqlCmd.append("'N',NULL,");
		else
			sqlCmd.append("'Y',").append(AD_Sequence_ID).append(",");
		sqlCmd.append(GL_Category_ID).append(",");
		//  IsPrinted,IsTransferred,DocumentCopies,IsSOTrx)
		sqlCmd.append("'Y',");
		if (DocBaseType.equals("SOO"))
			sqlCmd.append("'N',");
		else
			sqlCmd.append("'Y',");
		sqlCmd.append("0,");
		if (DocBaseType.startsWith("AR") || DocBaseType.equals("MMS") || DocBaseType.equals("SOO"))
			sqlCmd.append("'Y'");
		else
			sqlCmd.append("'N'");
		sqlCmd.append(")");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createDocType - DocType NOT created - " + Name);
		//
		return C_DocType_ID;
	}   //  createDocType

	/*************************************************************************/

	/**
	 *  Create Default main entities.
	 *  - Dimensions & BPGroup, Prod Category)
	 *  - Location, Locator, Warehouse
	 *  - PriceList
	 *  - Cashbook, PaymentTerm
	 *  @param C_Country_ID country
	 *  @param City city
	 *  @param C_Region_ID region
	 *  @return true if created
	 */
	public boolean createEntities (int C_Country_ID, String City, int C_Region_ID)
	{
		Log.trace(Log.l3_Util, "MSetup.createEntries", "C_Country_ID=" + C_Country_ID + ", City=" + City + ", C_Region_ID=" + C_Region_ID);
		m_info.append("\n----\n");
		//
		String defaultName = Msg.translate(m_lang, "Standard");
		String defaultEntry = "'" + defaultName + "',";
		StringBuffer sqlCmd = null;
		int no = 0;

		//	Create Marketing Channel/Campaign
		int C_Channel_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Channel");
		sqlCmd = new StringBuffer("INSERT INTO C_Channel ");
		sqlCmd.append("(C_Channel_ID,Name,");
		sqlCmd.append(m_stdColumns).append(") VALUES (");
		sqlCmd.append(C_Channel_ID).append(",").append(defaultEntry);
		sqlCmd.append(m_stdValues).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Channel NOT inserted");
		int C_Campaign_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Campaign");
		sqlCmd = new StringBuffer("INSERT INTO C_Campaign ");
		sqlCmd.append("(C_Campaign_ID,C_Channel_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,Costs) VALUES (");
		sqlCmd.append(C_Campaign_ID).append(",").append(C_Channel_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Campaign_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Campaign NOT inserted");
		if (m_hasMCampaign)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Campaign_ID=").append(C_Campaign_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='MC'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Campaign NOT updated");
		}

		//	Create Sales Region
		int C_SalesRegion_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_SalesRegion");
		sqlCmd = new StringBuffer ("INSERT INTO C_SalesRegion ");
		sqlCmd.append("(C_SalesRegion_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsSummary) VALUES (");
		sqlCmd.append(C_SalesRegion_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_SalesRegion_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRegion NOT inserted");
		if (m_hasSRegion)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_SalesRegion_ID=").append(C_SalesRegion_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='SR'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement SalesRegion NOT updated");
		}

		/**
		 *  Business Partner
		 */
		//  Create BP Group
		int C_BP_Group_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BP_Group");
		sqlCmd = new StringBuffer ("INSERT INTO C_BP_Group ");
		sqlCmd.append("(C_BP_Group_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault) VALUES ( ");
		sqlCmd.append(C_BP_Group_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BP_Group_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BP Group NOT inserted");

		//	Create BPartner
		int C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_BP_Group_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BPartner_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BPartner NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("C_BPartner_ID=").append(C_BPartner_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
		sqlCmd.append(" AND ElementType='BP'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element BPartner NOT updated");
		createPreference("C_BPartner_ID", String.valueOf(C_BPartner_ID), 143);

		/**
		 *  Product
		 */
		//  Create Product Category
		int M_Product_Category_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Product_Category");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product_Category ");
		sqlCmd.append("(M_Product_Category_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault,PlannedMargin) VALUES (");
		sqlCmd.append(M_Product_Category_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y',0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_Category_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product Category NOT inserted");

		//  UOM (EA)
		int C_UOM_ID = 100;

		//  TaxCategory
		int C_TaxCategory_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_TaxCategory");
		sqlCmd = new StringBuffer ("INSERT INTO C_TaxCategory ");
		sqlCmd.append("(C_TaxCategory_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,IsDefault) VALUES (");
		sqlCmd.append(C_TaxCategory_ID).append(",").append(m_stdValues).append(", ");
		if (C_Country_ID == 100)    // US
			sqlCmd.append("'Sales Tax','Y')");
		else
			sqlCmd.append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - TaxCategory NOT inserted");

		//  Tax - Zero Rate
		int C_Tax_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Tax");
		sqlCmd = new StringBuffer ("INSERT INTO C_Tax ");
		sqlCmd.append("(C_Tax_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,Rate,C_TaxCategory_ID,C_Country_ID,TO_Country_ID,ValidFrom,IsDefault) VALUES (");
		sqlCmd.append(C_Tax_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append("0,").append(C_TaxCategory_ID).append(",")
			.append(C_Country_ID).append(",").append(C_Country_ID)
			.append(",TO_DATE('1990-01-01','YYYY-MM-DD'),'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Tax_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Tax NOT inserted");

		//	Create Product
		int M_Product_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Product");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product ");
		sqlCmd.append("(M_Product_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_UOM_ID,M_Product_Category_ID,C_TaxCategory_ID) VALUES (");
		sqlCmd.append(M_Product_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry);
		sqlCmd.append(C_UOM_ID).append(",").append(M_Product_Category_ID).append(",");
		sqlCmd.append(C_TaxCategory_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("M_Product_ID=").append(M_Product_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
		sqlCmd.append(" AND ElementType='PR'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element Product NOT updated");

		/**
		 *  Warehouse
		 */
		//  Location (Company)
		int C_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		sqlCmd = new StringBuffer ("UPDATE AD_OrgInfo SET C_Location_ID=");
		sqlCmd.append(C_Location_ID).append(" WHERE AD_Org_ID=").append(AD_Org_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Location NOT inserted");
		createPreference("C_Country_ID", String.valueOf(C_Country_ID), 0);

		//  Default Warehouse
		int M_Warehouse_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Warehouse");
		sqlCmd = new StringBuffer ("INSERT INTO M_Warehouse ");
		sqlCmd.append("(M_Warehouse_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Location_ID,Separator) VALUES (");
		sqlCmd.append(M_Warehouse_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Location_ID).append(",'-')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Warehouse NOT inserted");

		//   Locator
		int M_Locator_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Locator");
		sqlCmd = new StringBuffer ("INSERT INTO M_Locator ");
		sqlCmd.append("(M_Locator_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,M_Warehouse_ID,X,Y,Z,PriorityNo,IsDefault) VALUES (");
		sqlCmd.append(M_Locator_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(M_Warehouse_ID).append(",0,0,0,50,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Locator NOT inserted");

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_BPartnerCashTrx_ID=").append(C_BPartner_ID);
		sqlCmd.append(",M_ProductFreight_ID=").append(M_Product_ID);
//		sqlCmd.append("C_UOM_Volume_ID=");
//		sqlCmd.append(",C_UOM_Weight_ID=");
//		sqlCmd.append(",C_UOM_Length_ID=");
//		sqlCmd.append(",C_UOM_Time_ID=");
		sqlCmd.append(" WHERE AD_Client_ID=").append(AD_Client_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createEntities - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		/**
		 *  Other
		 */
		//  PriceList
		int M_PriceList_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_PriceList");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList ");
		sqlCmd.append("(M_PriceList_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID,IsDefault) VALUES (");
		sqlCmd.append(M_PriceList_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(",'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList NOT inserted");
		//  DiscountSchema
		int M_DiscountSchema_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_DiscountSchema");
		sqlCmd = new StringBuffer ("INSERT INTO M_DiscountSchema ");
		sqlCmd.append("(M_DiscountSchema_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,DiscountType, FlatDiscount) VALUES (");
		sqlCmd.append(M_DiscountSchema_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append("SysDate,'P',0)");	//	PriceList
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - DiscountSchema NOT inserted");
		//  PriceList Version
		int M_PriceList_Version_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_PriceList_Version");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList_Version ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,M_PriceList_ID,M_DiscountSchema_ID) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY-MM-DD'),SysDate,").append(M_PriceList_ID)
			.append(",").append(M_DiscountSchema_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList_Version NOT inserted");
		//  ProductPrice
		sqlCmd = new StringBuffer ("INSERT INTO M_ProductPrice ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("M_Product_ID,PriceList,PriceStd,PriceLimit) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(M_Product_ID).append(",1,1,1)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - ProductPrice NOT inserted");

		//  Location for Standard BP
		C_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		int C_BPartner_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(City).append("',").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (Standard) NOT inserted");

		//	Create Sales Rep for Client-User
		C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_U_Name).append("','").append(AD_User_U_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_U_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (User) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_U_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (User) NOT updated");

		//  Location for Client-User
		C_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		C_BPartner_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(City).append("',").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (User) NOT inserted");


		//	Create Sales Rep for Client-Admin
		C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_Name).append("','").append(AD_User_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (Admin) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (Admin) NOT updated");

		//  Location for Client-Admin
		C_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		C_BPartner_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(City).append("',").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (Admin) NOT inserted");


		//  Payment Term
		int C_PaymentTerm_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_PaymentTerm");
		sqlCmd = new StringBuffer ("INSERT INTO C_PaymentTerm ");
		sqlCmd.append("(C_PaymentTerm_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,NetDays,GraceDays,DiscountDays,Discount,DiscountDays2,Discount2,IsDefault) VALUES (");
		sqlCmd.append(C_PaymentTerm_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'Immediate','Immediate',0,0,0,0,0,0,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PaymentTerm NOT inserted");

		//  Project Cycle
		C_Cycle_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Cycle");
		sqlCmd = new StringBuffer ("INSERT INTO C_Cycle ");
		sqlCmd.append("(C_Cycle_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_Cycle_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Cycle NOT inserted");

		/**
		 *  Organization level data	===========================================
		 */

		//	Create Default Project
		int C_Project_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Project");
		sqlCmd = new StringBuffer ("INSERT INTO C_Project ");
		sqlCmd.append("(C_Project_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Currency_ID,IsSummary) VALUES (");
		sqlCmd.append(C_Project_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Currency_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Project_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Project NOT inserted");
		//  Default Project
		if (m_hasProject)
		{
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Project_ID=").append(C_Project_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='PJ'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Project NOT updated");
		}

		//  CashBook
		int C_CashBook_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_CashBook");
		sqlCmd = new StringBuffer ("INSERT INTO C_CashBook ");
		sqlCmd.append("(C_CashBook_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_CashBook_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_CashBook_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - CashBook NOT inserted");

		return true;
	}   //  createEntities

	/**
	 *  Create Preference
	 *  @param Attribute attribute
	 *  @param Value value
	 *  @param AD_Window_ID window
	 */
	private void createPreference (String Attribute, String Value, int AD_Window_ID)
	{
		int AD_Preference_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Preference");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO AD_Preference ");
		sqlCmd.append("(AD_Preference_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Attribute,Value,AD_Window_ID) VALUES (");
		sqlCmd.append(AD_Preference_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(Attribute).append("','").append(Value).append("',");
		if (AD_Window_ID == 0)
			sqlCmd.append("NULL)");
		else
			sqlCmd.append(AD_Window_ID).append(")");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createPreference - Preference NOT inserted - " + Attribute);
	}   //  createPreference

	/*************************************************************************/

	/**
	 *  Get Client
	 *  @return AD_Client_ID
	 */
	public int getAD_Client_ID()
	{
		return AD_Client_ID;
	}
	public int getAD_Org_ID()
	{
		return AD_Org_ID;
	}
	public int getAD_User_ID()
	{
		return AD_User_ID;
	}
	public int getAD_Role_ID()
	{
		return AD_Role_ID;
	}
	public int getC_AcctSchema_ID()
	{
		return C_AcctSchema_ID;
	}
	public String getInfo()
	{
		return m_info.toString();
	}
}   //  MSetup
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
package org.compiere.model;

import java.util.*;
import java.sql.*;
import java.io.*;

import org.compiere.util.*;

/**
 * Initial Setup Model
 *
 * @author Jorg Janke
 * @version $Id: MSetup.java,v 1.36 2004/05/20 05:57:47 jjanke Exp $
 */
public final class MSetup
{
	/**
	 *  Constructor
	 *  @param ctx context
	 *  @param WindowNo window
	 */
	public MSetup(Properties ctx, int WindowNo)
	{
		m_ctx = new Properties(ctx);	//	copy
		m_lang = Env.getAD_Language(m_ctx);
		m_WindowNo = WindowNo;
	}   //  MSetup

	private Properties      m_ctx;
	private String          m_lang;
	private int             m_WindowNo;
	private StringBuffer    m_info;
	//
	private String          m_clientName;
//	private String          m_orgName;
	//
	private String          m_stdColumns = "AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy";
	private String          m_stdValues;
	private String          m_stdValuesOrg;
	//
	private NaturalAccountMap m_nap = null;
	//
	private MClient			m_client;
	private MOrg			m_org;
	private MAcctSchema		m_as;
	//
	private int     		AD_User_ID;
	private String  		AD_User_Name;
	private int     		AD_User_U_ID;
	private String  		AD_User_U_Name;
	private int     		AD_Role_ID;
	private int     		C_Calendar_ID;
	private int     		m_AD_Tree_Account_ID;
	private int     		C_Cycle_ID;
	//
	private boolean         m_hasProject = false;
	private boolean         m_hasMCampaign = false;
	private boolean         m_hasSRegion = false;


	/**
	 *  Create Client Info.
	 *  - Client, Trees, Org, Role, User, User_Role
	 *  @param clientName client name
	 *  @param orgName org name
	 *  @param userClient user id client
	 *  @param userOrg user id org
	 *  @return true if created
	 */
	public boolean createClient (String clientName, String orgName,
		String userClient, String userOrg)
	{
		Log.trace(Log.l3_Util, "MSetup.createClient - " + clientName);
		//  info header
		m_info = new StringBuffer();
		//  Standarc columns
		String name = null;
		String sql = null;
		int no = 0;

		/**
		 *  Create Client
		 */
		name = clientName;
		if (name == null || name.length() == 0)
			name = "newClient";
		m_clientName = name;
		m_client = new MClient(m_ctx);
		m_client.setValue(m_clientName);
		m_client.setName(m_clientName);
		if (!m_client.save())
		{
			String err = "MSetup.createClient - Client NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		int AD_Client_ID = m_client.getAD_Client_ID();
		Env.setContext(m_ctx, m_WindowNo, "AD_Client_ID", AD_Client_ID);
		Env.setContext(m_ctx, "#AD_Client_ID", AD_Client_ID);

		//	Standard Values
		m_stdValues = String.valueOf(AD_Client_ID) + ",0,'Y',SysDate,0,SysDate,0";
		//  Info - Client
		m_info.append(Msg.translate(m_lang, "AD_Client_ID")).append("=").append(name).append("\n");

		//	Setup Sequences
		if (!MSequence.checkClientSequences (m_ctx, AD_Client_ID))
		{
			String err = "MSetup.createClient - Sequences NOT created";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		
		//  Trees and Client Info
		if (!m_client.setupClientInfo(m_lang))
		{
			String err = "MSetup.createClient - ClientInfo NOT created";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		m_AD_Tree_Account_ID = m_client.getSetup_AD_Tree_Account_ID();

		/**
		 *  Create Org
		 */
		name = orgName;
		if (name == null || name.length() == 0)
			name = "newOrg";
		m_org = new MOrg (m_client, name);
		if (!m_org.save())
		{
			String err = "MSetup.createClient - Org NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		Env.setContext(m_ctx, m_WindowNo, "AD_Org_ID", getAD_Org_ID());
		Env.setContext(m_ctx, "#AD_Org_ID", getAD_Org_ID());
		m_stdValuesOrg = AD_Client_ID + "," + getAD_Org_ID() + ",'Y',SysDate,0,SysDate,0";
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_Org_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Roles
		 *  - Admin
		 *  - User
		 */
		name = m_clientName + " Admin";
		name = DB.TO_STRING(name);
		AD_Role_ID = getNextID(AD_Client_ID, "AD_Role");
		sql = "INSERT INTO AD_Role(" + m_stdColumns + ",AD_Role_ID,"
			+ "Name,Description,UserLevel,ClientList,OrgList)"
			+ " VALUES (" + m_stdValues + "," + AD_Role_ID + ","
			+ name + "," + name + ",' CO','" + AD_Client_ID + "','0," + getAD_Org_ID() + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin Role A NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  OrgAccess x,0
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValues + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_OrgAccess 0 NOT created");
		//  OrgAccess x,y
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValuesOrg + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_OrgAccess NOT created");
		//  Info - Admin Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		//
		name = m_clientName + " User";
		name = DB.TO_STRING(name);
		int AD_Role_ID_U = getNextID(AD_Client_ID, "AD_Role");
		sql = "INSERT INTO AD_Role(" + m_stdColumns + ",AD_Role_ID,"
			+ "Name,Description,UserLevel,ClientList,OrgList)"
			+ " VALUES (" + m_stdValues + "," + AD_Role_ID_U + ","
			+ name + "," + name + ",'  O','" + AD_Client_ID + "','" + getAD_Org_ID() + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - User Role A NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  OrgAccess x,y
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValuesOrg + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - User Role_OrgAccess NOT created");
		//  Info - Client Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Users
		 *  - Client
		 *  - Org
		 */
		name = userClient;
		if (name == null || name.length() == 0)
			name = m_clientName + "Client";
		AD_User_ID = getNextID(AD_Client_ID, "AD_User");
		AD_User_Name = name;
		name = DB.TO_STRING(name);
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + ","
			+ name + "," + name + "," + name + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin User NOT inserted - " + AD_User_Name;
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_Name).append("/").append(AD_User_Name).append("\n");

		name = userOrg;
		if (name == null || name.length() == 0)
			name = m_clientName + "Org";
		AD_User_U_ID = getNextID(AD_Client_ID, "AD_User");
		AD_User_U_Name = name;
		name = DB.TO_STRING(name);
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + ","
			+ name + "," + name + "," + name + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Org User NOT inserted - " + AD_User_U_Name;
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_U_Name).append("/").append(AD_User_U_Name).append("\n");

		/**
		 *  Create User-Role
		 */
		//  ClientUser          - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+Admin NOT inserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+User NOT inserted");
		//  OrgUser             - User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole OrgUser+Org NOT inserted");
		//  SuperUser(100)      - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + ",100," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole SuperUser+Admin NOT inserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + ",100," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole SuperUser+User NOT inserted");

		return true;
	}   //  createClient



	/**************************************************************************
	 *  Create Accounting elements.
	 *  - Calendar
	 *  - Account Trees
	 *  - Account Values
	 *  - Accounting Schema
	 *  - Default Accounts
	 *
	 *  @param currency currency
	 *  @param hasProduct has product segment
	 *  @param hasBPartner has bp segment
	 *  @param hasProject has project segment
	 *  @param hasMCampaign has campaign segment
	 *  @param hasSRegion has sales region segment
	 *  @param AccountingFile file name of accounting file
	 *  @return true if created
	 */
	public boolean createAccounting(KeyNamePair currency,
		boolean hasProduct, boolean hasBPartner, boolean hasProject,
		boolean hasMCampaign, boolean hasSRegion,
		File AccountingFile)
	{
		Log.trace(Log.l3_Util, "MSetup.createAccounting - " + m_client);
		//
		m_hasProject = hasProject;
		m_hasMCampaign = hasMCampaign;
		m_hasSRegion = hasSRegion;

		//  Standard variables
		m_info = new StringBuffer();
		String name = null;
		StringBuffer sqlCmd = null;
		int no = 0;

		/**
		 *  Create Calendar
		 */
		C_Calendar_ID = getNextID(m_client.getAD_Client_ID(), "C_Calendar");
		name = m_clientName + " " + Msg.translate(m_lang, "C_Calendar_ID");
		name = DB.TO_STRING(name);
		sqlCmd = new StringBuffer("INSERT INTO C_Calendar(");
		sqlCmd.append(m_stdColumns).append(",C_Calendar_ID,Name)").append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_Calendar_ID).append(",").append(name).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Calendar NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_Calendar_ID")).append("=").append(name).append("\n");

		//  Year
		int C_Year_ID = getNextID(m_client.getAD_Client_ID(), "C_Year");
		sqlCmd = new StringBuffer ("INSERT INTO C_Year ");
		sqlCmd.append("(C_Year_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Year,C_Calendar_ID) VALUES (");
		sqlCmd.append(C_Year_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY'),").append(C_Calendar_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createAccounting - Year NOT inserted");
		//	TODO Create Periods


		//	Create Account Elements
		name = m_clientName + " " + Msg.translate(m_lang, "Account_ID");
		MElement element = new MElement (m_client, name, 
			MElement.ELEMENTTYPE_Account, m_AD_Tree_Account_ID);
		if (!element.save())
		{
			String err = "MSetup.createAccounting - Acct Element NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		int C_Element_ID = element.getC_Element_ID();
		m_info.append(Msg.translate(m_lang, "C_Element_ID")).append("=").append(name).append("\n");

		//	Create Account Values
		m_nap = new NaturalAccountMap(m_ctx);
		String errMsg = m_nap.parseFile(AccountingFile);
		if (errMsg.length() != 0)
		{
			Log.error(errMsg);
			m_info.append(errMsg);
			return false;
		}
		if (m_nap.saveAccounts(getAD_Client_ID(), getAD_Org_ID(), C_Element_ID))
			m_info.append(Msg.translate(m_lang, "C_ElementValue_ID")).append(" # ").append(m_nap.size()).append("\n");
		else
		{
			String err = "MSetup.createAccounting - Acct Element Values NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		int C_ElementValue_ID = m_nap.getC_ElementValue_ID("DEFAULT_ACCT");
		Log.trace(Log.l4_Data, "C_ElementValue_ID=" + C_ElementValue_ID);

		/**
		 *  Create AccountingSchema
		 */
		m_as = new MAcctSchema (m_client, currency);
		if (!m_as.save())
		{
			String err = "MSetup.createAccounting - AcctSchema NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_AcctSchema_ID")).append("=").append(m_as.getName()).append("\n");

		/**
		 *  Create AccountingSchema Elements (Structure)
		 */
		String sql2 = null;
		if (Env.isBaseLanguage(m_lang, "AD_Reference"))	//	Get ElementTypes & Name
			sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=181";
		else
			sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Reference_ID=181 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID";
		//
		int Element_OO=0, Element_AC=0, Element_PR=0, Element_BP=0, Element_PJ=0,
			Element_MC=0, Element_SR=0;
		try
		{
			int AD_Client_ID = m_client.getAD_Client_ID();
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next())
			{
				String ElementType = rs.getString(1);
				name = rs.getString(2);
				//
				String IsMandatory = null;
				String IsBalanced = "N";
				int SeqNo = 0;
				int C_AcctSchema_Element_ID = 0;

				if (ElementType.equals("OO"))
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_OO = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					IsBalanced = "Y";
					SeqNo = 10;
				}
				else if (ElementType.equals("AC"))
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_AC = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					SeqNo = 20;
				}
				else if (ElementType.equals("PR") && hasProduct)
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_PR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 30;
				}
				else if (ElementType.equals("BP") && hasBPartner)
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_BP = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 40;
				}
				else if (ElementType.equals("PJ") && hasProject)
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_PJ = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 50;
				}
				else if (ElementType.equals("MC") && hasMCampaign)
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_MC = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 60;
				}
				else if (ElementType.equals("SR") && hasSRegion)
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_SR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 70;
				}
				//	Not OT, LF, LT, U1, U2, AY

				if (IsMandatory != null)
				{
					sqlCmd = new StringBuffer ("INSERT INTO C_AcctSchema_Element(");
					sqlCmd.append(m_stdColumns).append(",C_AcctSchema_Element_ID,C_AcctSchema_ID,")
						.append("ElementType,Name,SeqNo,IsMandatory,IsBalanced) VALUES (");
					sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_Element_ID).append(",").append(m_as.getC_AcctSchema_ID()).append(",")
						.append("'").append(ElementType).append("','").append(name).append("',").append(SeqNo).append(",'")
						.append(IsMandatory).append("','").append(IsBalanced).append("')");
					no = DB.executeUpdate(sqlCmd.toString());
					if (no == 1)
						m_info.append(Msg.translate(m_lang, "C_AcctSchema_Element_ID")).append("=").append(name).append("\n");

					/** Default value for mandatory elements: OO and AC */
					if (ElementType.equals("OO"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET Org_ID=");
						sqlCmd.append(getAD_Org_ID()).append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Org in AcctSchamaElement NOT updated");
					}
					if (ElementType.equals("AC"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET C_ElementValue_ID=");
						sqlCmd.append(C_ElementValue_ID).append(", C_Element_ID=").append(C_Element_ID);
						sqlCmd.append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Account in AcctSchamaElement NOT updated");
					}
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e1)
		{
			Log.error ("MSetup.createAccounting - Elements", e1);
		}
		//  Create AcctSchema


		//  Create GL Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_GL (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "USESUSPENSEBALANCING,SUSPENSEBALANCING_ACCT,"
			+ "USESUSPENSEERROR,SUSPENSEERROR_ACCT,"
			+ "USECURRENCYBALANCING,CURRENCYBALANCING_ACCT,"
			+ "RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,"
			+ "INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT,"
			+ "PPVOFFSET_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(m_as.getC_AcctSchema_ID()).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEBALANCING_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEERROR_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("CURRENCYBALANCING_ACCT")).append(",");
		//  RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,
		sqlCmd.append(getAcct("RETAINEDEARNING_ACCT")).append(",");
		sqlCmd.append(getAcct("INCOMESUMMARY_ACCT")).append(",");
		//  INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT)
		sqlCmd.append(getAcct("INTERCOMPANYDUETO_ACCT")).append(",");
		sqlCmd.append(getAcct("INTERCOMPANYDUEFROM_ACCT")).append(",");
		sqlCmd.append(getAcct("PPVOFFSET_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - GL Accounts NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//	Create Std Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_DEFAULT (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT, "
			+ "P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT, "
			+ "P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT, "
			+ "C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT, "
			+ "V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT, "
			+ "PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT, "
			+ "UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT, "
			+ "WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT, "
			+ "PJ_ASSET_ACCT,PJ_WIP_ACCT,"
			+ "T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT, "
			+ "B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,"
			+ "B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,"
			+ "B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT, "
			+ "CH_EXPENSE_ACCT,CH_REVENUE_ACCT, "
			+ "UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT, "
			+ "CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(m_as.getC_AcctSchema_ID()).append(",");
		//  W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT
		sqlCmd.append(getAcct("W_INVENTORY_ACCT")).append(",");
		sqlCmd.append(getAcct("W_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("W_REVALUATION_ACCT")).append(",");
		sqlCmd.append(getAcct("W_INVACTUALADJUST_ACCT")).append(", ");
		//  P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT,
		sqlCmd.append(getAcct("P_REVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("P_COGS_ACCT")).append(", ");
		//  P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT,
		sqlCmd.append(getAcct("P_PURCHASEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_INVOICEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTREC_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTGRANT_ACCT")).append(", ");
		//  C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("C_RECEIVABLE_ACCT")).append(",");
		sqlCmd.append(getAcct("C_PREPAYMENT_ACCT")).append(", ");
		//  V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("V_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("V_LIABILITY_SERVICES_ACCT")).append(",");
		sqlCmd.append(getAcct("V_PREPAYMENT_ACCT")).append(", ");
		//  PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT,
		sqlCmd.append(getAcct("PAYDISCOUNT_EXP_ACCT")).append(",");
		sqlCmd.append(getAcct("PAYDISCOUNT_REV_ACCT")).append(",");
		sqlCmd.append(getAcct("WRITEOFF_ACCT")).append(", ");
		//  UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT,
		sqlCmd.append(getAcct("UNREALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("UNREALIZEDLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDLOSS_ACCT")).append(", ");
		//  WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT,
		sqlCmd.append(getAcct("WITHHOLDING_ACCT")).append(",");
		sqlCmd.append(getAcct("E_PREPAYMENT_ACCT")).append(",");
		sqlCmd.append(getAcct("E_EXPENSE_ACCT")).append(", ");
		//  PJ_ASSET_ACCT,PJ_WIP_ACCT,
		sqlCmd.append(getAcct("PJ_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("PJ_WIP_ACCT")).append(",");
		//  T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT,
		sqlCmd.append(getAcct("T_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("T_RECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("T_DUE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_CREDIT_ACCT")).append(", ");
		//  B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,
		sqlCmd.append(getAcct("B_INTRANSIT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("B_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTREV_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTEXP_ACCT")).append(",");
		//  B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,
		sqlCmd.append(getAcct("B_UNIDENTIFIED_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTLOSS_ACCT")).append(",");
		//  B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT,
		sqlCmd.append(getAcct("B_REVALUATIONGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_REVALUATIONLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("B_PAYMENTSELECT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_UNALLOCATEDCASH_ACCT")).append(", ");
		//  CH_EXPENSE_ACCT,CH_REVENUE_ACCT,
		sqlCmd.append(getAcct("CH_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CH_REVENUE_ACCT")).append(", ");
		//  UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT,
		sqlCmd.append(getAcct("UNEARNEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIPTS_ACCT")).append(", ");
		//  CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT)
		sqlCmd.append(getAcct("CB_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_CASHTRANSFER_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_RECEIPT_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Default Accounts NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//  GL Categories
		createGLCategory("Standard", "M", true);
		int GL_None = createGLCategory("None", "D", false);
		int GL_GL = createGLCategory("Manual", "M", false);
		int GL_ARI = createGLCategory("AR Invoice", "D", false);
		int GL_ARR = createGLCategory("AR Receipt", "D", false);
		int GL_MM = createGLCategory("Material Management", "D", false);
		int GL_API = createGLCategory("AP Invoice", "D", false);
		int GL_APP = createGLCategory("AP Payment", "D", false);
		int GL_CASH = createGLCategory("Cash/Payments", "D", false);

		//	Base DocumentTypes
		createDocType("GL Journal", "Journal", 
			MDocType.DOCBASETYPE_GLJournal, null, 0, 0, 1000, GL_GL);
		createDocType("GL Journal Batch", "Journal Batch", 
			MDocType.DOCBASETYPE_GLJournal, null, 0, 0, 100, GL_GL);
		//	MDocType.DOCBASETYPE_GLDocument
		//
		int DT_I = createDocType("AR Invoice", "Invoice", 
			MDocType.DOCBASETYPE_ARInvoice, null, 0, 0, 100000, GL_ARI);
		int DT_II = createDocType("AR Invoice Indirect", "Invoice", 
			MDocType.DOCBASETYPE_ARInvoice, null, 0, 0, 150000, GL_ARI);
		int DT_IC = createDocType("AR Credit Memo", "Credit Memo", 
			MDocType.DOCBASETYPE_ARCreditMemo, null, 0, 0, 170000, GL_ARI);
		//	MDocType.DOCBASETYPE_ARProFormaInvoice
		
		createDocType("AP Invoice", "Vendor Invoice", 
			MDocType.DOCBASETYPE_APInvoice, null, 0, 0, 0, GL_API);
		createDocType("AP CreditMemo", "Vendor Credit Memo", 
			MDocType.DOCBASETYPE_APCreditMemo, null, 0, 0, 0, GL_API);
		createDocType("Match Invoice", "Match Invoice", 
			MDocType.DOCBASETYPE_MatchInvoice, null, 0, 0, 390000, GL_API);
		
		createDocType("AR Receipt", "Customer Payment", 
			MDocType.DOCBASETYPE_ARReceipt, null, 0, 0, 0, GL_ARR);
		createDocType("AP Payment", "Vendor Payment", 
			MDocType.DOCBASETYPE_APPayment, null, 0, 0, 0, GL_APP);
		createDocType("Allocation", "Allocation", 
			MDocType.DOCBASETYPE_PaymentAllocation, null, 0, 0, 490000, GL_CASH);

		int DT_S  = createDocType("MM Shipment", "Delivery Note", 
			MDocType.DOCBASETYPE_MaterialDelivery, null, 0, 0, 500000, GL_MM);
		int DT_SI = createDocType("MM Shipment Indirect", "Delivery Note", 
			MDocType.DOCBASETYPE_MaterialDelivery, null, 0, 0, 550000, GL_MM);
		
		createDocType("MM Receipt", "Vendor Delivery", 
			MDocType.DOCBASETYPE_MaterialReceipt, null, 0, 0, 0, GL_MM);
		int DT_RM = createDocType("MM Returns", "Customer Returns", 
			MDocType.DOCBASETYPE_MaterialReceipt, null, 0, 0, 570000, GL_MM);
		
		createDocType("Purchase Order", "Purchase Order", 
			MDocType.DOCBASETYPE_PurchaseOrder, null, 0, 0, 800000, GL_None);
		createDocType("Match PO", "Patch PO", 
			MDocType.DOCBASETYPE_MatchPO, null, 0, 0, 890000, GL_None);
		createDocType("Purchase Requisition", "Purchase Requisition", 
			MDocType.DOCBASETYPE_PurchaseRequisition, null, 0, 0, 900000, GL_None);

		createDocType("Bank Statement", "Bank Statement", 
			MDocType.DOCBASETYPE_BankStatement, null, 0, 0, 700000, GL_CASH);
		createDocType("Cash Journal", "Cash Journal",
			MDocType.DOCBASETYPE_CashJournal, null, 0, 0, 750000, GL_CASH);
		
		createDocType("Material Movement", "Material Movement",
			MDocType.DOCBASETYPE_MaterialMovement, null, 0, 0, 610000, GL_MM);
		createDocType("Physical Inventory", "Physical Inventory", 
			MDocType.DOCBASETYPE_MaterialPhysicalInventory, null, 0, 0, 620000, GL_MM);
		createDocType("Material Production", "Material Production", 
			MDocType.DOCBASETYPE_MaterialProduction, null, 0, 0, 630000, GL_MM);
		createDocType("Project Issue", "Project Issue", 
			MDocType.DOCBASETYPE_ProjectIssue, null, 0, 0, 640000, GL_MM);

		//  Order Entry
		createDocType("Binding offer", "Quotation", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_Quotation, 
			0, 0, 10000, GL_None);
		createDocType("Non binding offer", "Proposal", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_Proposal, 
			0, 0, 20000, GL_None);
		createDocType("Prepay Order", "Prepay Order", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_PrepayOrder, 
			DT_S, DT_I, 30000, GL_None);
		createDocType("Return Material", "Return Material Authorization", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_ReturnMaterial, 
			DT_RM, DT_IC, 30000, GL_None);
		createDocType("Standard Order", "Order Confirmation", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_StandardOrder, 
			DT_S, DT_I, 50000, GL_None);
		createDocType("Credit Order", "Order Confirmation", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_OnCreditOrder, 
			DT_SI, DT_I, 60000, GL_None);   //  RE
		createDocType("Warehouse Order", "Order Confirmation", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_WarehouseOrder, 
			DT_S, DT_I,	70000, GL_None);    //  LS
		int DT = createDocType("POS Order", "Order Confirmation", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_POSOrder, 
			DT_SI, DT_II, 80000, GL_None);    // Bar
		//	POS As Default for window SO
		createPreference("C_DocTypeTarget_ID", String.valueOf(DT), 143);

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_AcctSchema1_ID=").append(m_as.getC_AcctSchema_ID())
			.append(", C_Calendar_ID=").append(C_Calendar_ID)
			.append(" WHERE AD_Client_ID=").append(m_client.getAD_Client_ID());
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//
		return true;
	}   //  createAccounting

	/**
	 *  Get Account ID for key
	 *  @param key key
	 *  @return C_ValidCombination_ID
	 */
	private int getAcct (String key)
	{
		Log.trace(Log.l4_Data, "MSetup.getAcct - " + key);
		//  Element
		int C_ElementValue_ID = m_nap.getC_ElementValue_ID(key);
		MAccount vc = MAccount.getDefault(m_as, true);	//	optional null
		vc.setAccount_ID(C_ElementValue_ID);
		vc.save();
		int C_ValidCombination_ID = vc.getC_ValidCombination_ID();

		//  Error
		if (C_ValidCombination_ID == 0)
			Log.error("MSetup.getAcct - no account for " + key);
		return C_ValidCombination_ID;
	}   //  getAcct

	/**
	 *  Create GL Category
	 *  @param Name name
	 *  @param CategoryType category type
	 *  @param isDefault is default value
	 *  @return GL_Category_ID
	 */
	private int createGLCategory (String Name, String CategoryType, boolean isDefault)
	{
		int GL_Category_ID = getNextID(m_client.getAD_Client_ID(), "GL_Category");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO GL_Category ");
		sqlCmd.append("(GL_Category_ID,").append(m_stdColumns).append(",")
			.append("Name,CategoryType,IsDefault) VALUES (")
			.append(GL_Category_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(Name).append("','").append(CategoryType).append("','")
			.append(isDefault ? "Y" : "N").append("')");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createGLCategory - GL Category NOT created - " + Name);
		//
		return GL_Category_ID;
	}   //  createGLCategory

	/**
	 *  Create Document Types with Sequence
	 *  @param Name name
	 *  @param PrintName print name
	 *  @param DocBaseType document base type
	 *  @param DocSubTypeSO sales order sub type
	 *  @param C_DocTypeShipment_ID shipment doc
	 *  @param C_DocTypeInvoice_ID invoice doc
	 *  @param StartNo start doc no
	 *  @param GL_Category_ID gl category
	 *  @return C_DocType_ID doc type
	 */
	private int createDocType (String Name, String PrintName,
		String DocBaseType, String DocSubTypeSO,
		int C_DocTypeShipment_ID, int C_DocTypeInvoice_ID,
		int StartNo, int GL_Category_ID)
	{
		MSequence sequence = null;
		if (StartNo != 0)
		{
			sequence = new MSequence(m_ctx, getAD_Client_ID(), Name, StartNo);
			if (!sequence.save())
				Log.error("MSetup.createDocType - Sequence NOT created - " + Name);
		}
		
		MDocType dt = new MDocType (m_ctx, DocBaseType, Name);
		dt.setPrintName(PrintName);
		if (DocSubTypeSO != null)
			dt.setDocSubTypeSO(DocSubTypeSO);
		if (C_DocTypeShipment_ID != 0)
			dt.setC_DocTypeShipment_ID(C_DocTypeShipment_ID);
		if (C_DocTypeInvoice_ID != 0)
			dt.setC_DocTypeInvoice_ID(C_DocTypeInvoice_ID);
		if (GL_Category_ID != 0)
			dt.setGL_Category_ID(GL_Category_ID);
		if (sequence == null)
			dt.setIsDocNoControlled(false);
		else
			dt.setDocNoSequence_ID(sequence.getAD_Sequence_ID());
		dt.setIsSOTrx();
		if (!dt.save())
			Log.error("MSetup.createDocType - DocType NOT created - " + Name);
		//
		return dt.getC_DocType_ID();
	}   //  createDocType

	
	/**************************************************************************
	 *  Create Default main entities.
	 *  - Dimensions & BPGroup, Prod Category)
	 *  - Location, Locator, Warehouse
	 *  - PriceList
	 *  - Cashbook, PaymentTerm
	 *  @param C_Country_ID country
	 *  @param City city
	 *  @param C_Region_ID region
	 *  @return true if created
	 */
	public boolean createEntities (int C_Country_ID, String City, int C_Region_ID, int C_Currency_ID)
	{
		Log.trace(Log.l3_Util, "MSetup.createEntries", "C_Country_ID=" + C_Country_ID + ", City=" + City + ", C_Region_ID=" + C_Region_ID);
		m_info.append("\n----\n");
		//
		String defaultName = Msg.translate(m_lang, "Standard");
		String defaultEntry = "'" + defaultName + "',";
		StringBuffer sqlCmd = null;
		int no = 0;

		//	Create Marketing Channel/Campaign
		int C_Channel_ID = getNextID(getAD_Client_ID(), "C_Channel");
		sqlCmd = new StringBuffer("INSERT INTO C_Channel ");
		sqlCmd.append("(C_Channel_ID,Name,");
		sqlCmd.append(m_stdColumns).append(") VALUES (");
		sqlCmd.append(C_Channel_ID).append(",").append(defaultEntry);
		sqlCmd.append(m_stdValues).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Channel NOT inserted");
		int C_Campaign_ID = getNextID(getAD_Client_ID(), "C_Campaign");
		sqlCmd = new StringBuffer("INSERT INTO C_Campaign ");
		sqlCmd.append("(C_Campaign_ID,C_Channel_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,Costs) VALUES (");
		sqlCmd.append(C_Campaign_ID).append(",").append(C_Channel_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Campaign_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Campaign NOT inserted");
		if (m_hasMCampaign)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Campaign_ID=").append(C_Campaign_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
			sqlCmd.append(" AND ElementType='MC'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Campaign NOT updated");
		}

		//	Create Sales Region
		int C_SalesRegion_ID = getNextID(getAD_Client_ID(), "C_SalesRegion");
		sqlCmd = new StringBuffer ("INSERT INTO C_SalesRegion ");
		sqlCmd.append("(C_SalesRegion_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsSummary) VALUES (");
		sqlCmd.append(C_SalesRegion_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_SalesRegion_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRegion NOT inserted");
		if (m_hasSRegion)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_SalesRegion_ID=").append(C_SalesRegion_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
			sqlCmd.append(" AND ElementType='SR'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement SalesRegion NOT updated");
		}

		/**
		 *  Business Partner
		 */
		//  Create BP Group
		int C_BP_Group_ID = getNextID(getAD_Client_ID(), "C_BP_Group");
		sqlCmd = new StringBuffer ("INSERT INTO C_BP_Group ");
		sqlCmd.append("(C_BP_Group_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault) VALUES ( ");
		sqlCmd.append(C_BP_Group_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BP_Group_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BP Group NOT inserted");

		//	Create BPartner
		int C_BPartner_ID = getNextID(getAD_Client_ID(), "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_BP_Group_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BPartner_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BPartner NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("C_BPartner_ID=").append(C_BPartner_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
		sqlCmd.append(" AND ElementType='BP'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element BPartner NOT updated");
		createPreference("C_BPartner_ID", String.valueOf(C_BPartner_ID), 143);

		/**
		 *  Product
		 */
		//  Create Product Category
		int M_Product_Category_ID = getNextID(getAD_Client_ID(), "M_Product_Category");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product_Category ");
		sqlCmd.append("(M_Product_Category_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault,PlannedMargin) VALUES (");
		sqlCmd.append(M_Product_Category_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y',0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_Category_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product Category NOT inserted");

		//  UOM (EA)
		int C_UOM_ID = 100;

		//  TaxCategory
		int C_TaxCategory_ID = getNextID(getAD_Client_ID(), "C_TaxCategory");
		sqlCmd = new StringBuffer ("INSERT INTO C_TaxCategory ");
		sqlCmd.append("(C_TaxCategory_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,IsDefault) VALUES (");
		sqlCmd.append(C_TaxCategory_ID).append(",").append(m_stdValues).append(", ");
		if (C_Country_ID == 100)    // US
			sqlCmd.append("'Sales Tax','Y')");
		else
			sqlCmd.append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - TaxCategory NOT inserted");

		//  Tax - Zero Rate
		MTax tax = new MTax (m_ctx, "Standard", Env.ZERO, C_TaxCategory_ID);
		tax.setIsDefault(true);
		if (tax.save())
			m_info.append(Msg.translate(m_lang, "C_Tax_ID")).append("=").append(tax).append("\n");
		else
			Log.error("MSetup.createEntities - Tax NOT inserted");

		//	Create Product
		int M_Product_ID = getNextID(getAD_Client_ID(), "M_Product");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product ");
		sqlCmd.append("(M_Product_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_UOM_ID,M_Product_Category_ID,C_TaxCategory_ID) VALUES (");
		sqlCmd.append(M_Product_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry);
		sqlCmd.append(C_UOM_ID).append(",").append(M_Product_Category_ID).append(",");
		sqlCmd.append(C_TaxCategory_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("M_Product_ID=").append(M_Product_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
		sqlCmd.append(" AND ElementType='PR'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element Product NOT updated");

		/**
		 *  Warehouse
		 */
		//  Location (Company)
		int C_Location_ID = getNextID(getAD_Client_ID(), "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append(DB.TO_STRING(City)).append(",").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		sqlCmd = new StringBuffer ("UPDATE AD_OrgInfo SET C_Location_ID=");
		sqlCmd.append(C_Location_ID).append(" WHERE AD_Org_ID=").append(getAD_Org_ID());
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Location NOT inserted");
		createPreference("C_Country_ID", String.valueOf(C_Country_ID), 0);

		//  Default Warehouse
		int M_Warehouse_ID = getNextID(getAD_Client_ID(), "M_Warehouse");
		sqlCmd = new StringBuffer ("INSERT INTO M_Warehouse ");
		sqlCmd.append("(M_Warehouse_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Location_ID,Separator) VALUES (");
		sqlCmd.append(M_Warehouse_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Location_ID).append(",'-')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Warehouse NOT inserted");

		//   Locator
		int M_Locator_ID = getNextID(getAD_Client_ID(), "M_Locator");
		sqlCmd = new StringBuffer ("INSERT INTO M_Locator ");
		sqlCmd.append("(M_Locator_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,M_Warehouse_ID,X,Y,Z,PriorityNo,IsDefault) VALUES (");
		sqlCmd.append(M_Locator_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(M_Warehouse_ID).append(",0,0,0,50,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Locator NOT inserted");

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_BPartnerCashTrx_ID=").append(C_BPartner_ID);
		sqlCmd.append(",M_ProductFreight_ID=").append(M_Product_ID);
//		sqlCmd.append("C_UOM_Volume_ID=");
//		sqlCmd.append(",C_UOM_Weight_ID=");
//		sqlCmd.append(",C_UOM_Length_ID=");
//		sqlCmd.append(",C_UOM_Time_ID=");
		sqlCmd.append(" WHERE AD_Client_ID=").append(getAD_Client_ID());
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createEntities - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		/**
		 *  Other
		 */
		//  PriceList
		int M_PriceList_ID = getNextID(getAD_Client_ID(), "M_PriceList");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList ");
		sqlCmd.append("(M_PriceList_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID,IsDefault) VALUES (");
		sqlCmd.append(M_PriceList_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(",'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList NOT inserted");
		//  DiscountSchema
		int M_DiscountSchema_ID = getNextID(getAD_Client_ID(), "M_DiscountSchema");
		sqlCmd = new StringBuffer ("INSERT INTO M_DiscountSchema ");
		sqlCmd.append("(M_DiscountSchema_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,DiscountType, FlatDiscount) VALUES (");
		sqlCmd.append(M_DiscountSchema_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append("SysDate,'P',0)");	//	PriceList
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - DiscountSchema NOT inserted");
		//  PriceList Version
		int M_PriceList_Version_ID = getNextID(getAD_Client_ID(), "M_PriceList_Version");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList_Version ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,M_PriceList_ID,M_DiscountSchema_ID) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY-MM-DD'),SysDate,").append(M_PriceList_ID)
			.append(",").append(M_DiscountSchema_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList_Version NOT inserted");
		//  ProductPrice
		sqlCmd = new StringBuffer ("INSERT INTO M_ProductPrice ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("M_Product_ID,PriceList,PriceStd,PriceLimit) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(M_Product_ID).append(",1,1,1)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - ProductPrice NOT inserted");

		//  Location for Standard BP
		C_Location_ID = getNextID(getAD_Client_ID(), "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append(DB.TO_STRING(City)).append(",").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		int C_BPartner_Location_ID = getNextID(getAD_Client_ID(), "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(DB.TO_STRING(City)).append(",").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (Standard) NOT inserted");

		//	Create Sales Rep for Client-User
		C_BPartner_ID = getNextID(getAD_Client_ID(), "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_U_Name).append("','").append(AD_User_U_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_U_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (User) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_U_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (User) NOT updated");

		//  Location for Client-User
		C_Location_ID = getNextID(getAD_Client_ID(), "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append(DB.TO_STRING(City)).append(",").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		C_BPartner_Location_ID = getNextID(getAD_Client_ID(), "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(DB.TO_STRING(City)).append(",").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (User) NOT inserted");


		//	Create Sales Rep for Client-Admin
		C_BPartner_ID = getNextID(getAD_Client_ID(), "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_Name).append("','").append(AD_User_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (Admin) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (Admin) NOT updated");

		//  Location for Client-Admin
		C_Location_ID = getNextID(getAD_Client_ID(), "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append(DB.TO_STRING(City)).append(",").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		C_BPartner_Location_ID = getNextID(getAD_Client_ID(), "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(DB.TO_STRING(City)).append(",").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (Admin) NOT inserted");


		//  Payment Term
		int C_PaymentTerm_ID = getNextID(getAD_Client_ID(), "C_PaymentTerm");
		sqlCmd = new StringBuffer ("INSERT INTO C_PaymentTerm ");
		sqlCmd.append("(C_PaymentTerm_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,NetDays,GraceDays,DiscountDays,Discount,DiscountDays2,Discount2,IsDefault) VALUES (");
		sqlCmd.append(C_PaymentTerm_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'Immediate','Immediate',0,0,0,0,0,0,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PaymentTerm NOT inserted");

		//  Project Cycle
		C_Cycle_ID = getNextID(getAD_Client_ID(), "C_Cycle");
		sqlCmd = new StringBuffer ("INSERT INTO C_Cycle ");
		sqlCmd.append("(C_Cycle_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_Cycle_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Cycle NOT inserted");

		/**
		 *  Organization level data	===========================================
		 */

		//	Create Default Project
		int C_Project_ID = getNextID(getAD_Client_ID(), "C_Project");
		sqlCmd = new StringBuffer ("INSERT INTO C_Project ");
		sqlCmd.append("(C_Project_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Currency_ID,IsSummary) VALUES (");
		sqlCmd.append(C_Project_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Currency_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Project_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Project NOT inserted");
		//  Default Project
		if (m_hasProject)
		{
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Project_ID=").append(C_Project_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
			sqlCmd.append(" AND ElementType='PJ'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Project NOT updated");
		}

		//  CashBook
		int C_CashBook_ID = getNextID(getAD_Client_ID(), "C_CashBook");
		sqlCmd = new StringBuffer ("INSERT INTO C_CashBook ");
		sqlCmd.append("(C_CashBook_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_CashBook_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_CashBook_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - CashBook NOT inserted");

		return true;
	}   //  createEntities

	/**
	 *  Create Preference
	 *  @param Attribute attribute
	 *  @param Value value
	 *  @param AD_Window_ID window
	 */
	private void createPreference (String Attribute, String Value, int AD_Window_ID)
	{
		int AD_Preference_ID = getNextID(getAD_Client_ID(), "AD_Preference");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO AD_Preference ");
		sqlCmd.append("(AD_Preference_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Attribute,Value,AD_Window_ID) VALUES (");
		sqlCmd.append(AD_Preference_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(Attribute).append("','").append(Value).append("',");
		if (AD_Window_ID == 0)
			sqlCmd.append("NULL)");
		else
			sqlCmd.append(AD_Window_ID).append(")");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createPreference - Preference NOT inserted - " + Attribute);
	}   //  createPreference

	/**************************************************************************

	/**
	 * 	Get Next ID
	 * 	@param AD_Client_ID client
	 * 	@param TableName table name
	 */
	private int getNextID (int AD_Client_ID, String TableName)
	{
		return DB.getNextID(AD_Client_ID, TableName, null);
	}	//	getNextID

	/**
	 *  Get Client
	 *  @return AD_Client_ID
	 */
	public int getAD_Client_ID()
	{
		return m_client.getAD_Client_ID();
	}
	public int getAD_Org_ID()
	{
		return m_org.getAD_Org_ID();
	}
	public int getAD_User_ID()
	{
		return AD_User_ID;
	}
	public int getAD_Role_ID()
	{
		return AD_Role_ID;
	}
	public String getInfo()
	{
		return m_info.toString();
	}
}   //  MSetup
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
package org.compiere.model;

import java.util.*;
import java.sql.*;
import java.io.*;

import org.compiere.util.*;

/**
 * Initial Setup Model
 *
 * @author Jorg Janke
 * @version $Id: MSetup.java,v 1.18 2003/01/20 05:39:20 jjanke Exp $
 */
public final class MSetup
{
	/**
	 *  Constructor
	 *  @param ctx context
	 *  @param WindowNo window
	 */
	public MSetup(Properties ctx, int WindowNo)
	{
		m_ctx = ctx;
		m_lang = Env.getAD_Language(m_ctx);
		m_WindowNo = WindowNo;
	}   //  MSetup

	private Properties      m_ctx;
	private String          m_lang;
	private int             m_WindowNo;
	private StringBuffer    m_info;
	//
	private String          m_clientName;
	private String          m_orgName;
	//
	private String          m_stdColumns = "AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy";
	private String          m_stdValues;
	private String          m_stdValuesOrg;
	//
	private NaturalAccountMap m_nap = null;
	//
	private final String    CompiereSys = "N";           //  Should NOT be changed
	private int             AD_Client_ID = 1000000;     //  initial Client_ID
	//
	private int     AD_Org_ID;
	private int     AD_User_ID;
	private String  AD_User_Name;
	private int     AD_User_U_ID;
	private String  AD_User_U_Name;
	private int     AD_Role_ID;
	private int     C_Calendar_ID;
	private int     C_AcctSchema_ID;
	private int     C_Currency_ID;
	private int     AD_Tree_Account_ID;
	private int     C_Cycle_ID;
	//
	private boolean         m_hasProject = false;
	private boolean         m_hasMCampaign = false;
	private boolean         m_hasSRegion = false;
	private AcctSchema      m_AcctSchema = null;


	/**
	 *  Create Client Info.
	 *  - Client, Trees, Org, Role, User, User_Role
	 *  @param clientName client name
	 *  @param orgName org name
	 *  @param userClient user id client
	 *  @param userOrg user id org
	 *  @return true if created
	 */
	public boolean createClient (String clientName, String orgName,
		String userClient, String userOrg)
	{
		Log.trace(Log.l3_Util, "MSetup.createClient");
		//  info header
		m_info = new StringBuffer();
		//  Standarc columns
		String name = null;
		String sql = null;
		int no = 0;

		/**
		 *  Create Client
		 */
		Env.setContext(m_ctx, "#CompiereSys", CompiereSys);
		AD_Client_ID = DB.getKeyNextNo (AD_Client_ID, CompiereSys, "AD_Client");
		Env.setContext(m_ctx, m_WindowNo, "AD_Client_ID", AD_Client_ID);

		//	Standard Values
		m_stdValues = String.valueOf(AD_Client_ID) + ",0,'Y',SysDate,0,SysDate,0";

		//	Create Client
		name = clientName;
		if (name == null || name.length() == 0)
			name = "newClient";
		name = name.trim();
		m_clientName = name;
		sql = "INSERT INTO AD_Client(" + m_stdColumns + ",Value,Name,Description)"
			+ " VALUES (" + m_stdValues + ",'" + name + "','" + name + "','" + name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Client NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info - Client
		m_info.append(Msg.translate(m_lang, "AD_Client_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Trees
		 */
		String sql2 = null;
		if (Env.isBaseLanguage(m_lang, "AD_Ref_List"))	//	Get TreeTypes & Name
			sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=120";
		else
			sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Reference_ID=120 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID";

		//  Tree IDs
		int AD_Tree_Org_ID=0, AD_Tree_BPartner_ID=0, AD_Tree_Project_ID=0,
			AD_Tree_SalesRegion_ID=0, AD_Tree_Product_ID=0;

		try
		{
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next())
			{
				String value = rs.getString(1);
				int AD_Tree_ID = 0;
				if (value.equals("OO"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Org_ID = AD_Tree_ID;
				}
				else if (value.equals("BP"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_BPartner_ID = AD_Tree_ID;
				}
				else if (value.equals("PJ"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Project_ID = AD_Tree_ID;
				}
				else if (value.equals("SR"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_SalesRegion_ID = AD_Tree_ID;
				}
				else if (value.equals("PR"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Product_ID = AD_Tree_ID;
				}
				else if (value.endsWith("EV"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Account_ID = AD_Tree_ID;
				}
				else if (value.equals("MM"))	//	No Menu
					;
				else
					//	PC (Product Category), BB (BOM), MC (Marketing Campaign), AY (Activity)
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");

				//
				if (AD_Tree_ID != 0)
				{
					name = m_clientName + " " + rs.getString(2);
					sql = "INSERT INTO AD_Tree(" + m_stdColumns + ",AD_Tree_ID,Name,Description,TreeType)"
						+ " VALUES (" + m_stdValues + "," + AD_Tree_ID + ",'" + name + "','" + name + "','" + value + "')";
					no = DB.executeUpdate(sql);
					if (no == 1)
						m_info.append(Msg.translate(m_lang, "AD_Tree_ID")).append("=").append(name).append("\n");
					else
						Log.error("MSetup.createClient - Tree NOT created: " + name);
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e1)
		{
			Log.error ("MSetup.createClient - Trees", e1);
		}

		//	Get Primary Tree
		int AD_Tree_Menu_ID = 10;	//	hardcoded

		//	Create ClientInfo
		sql = "INSERT INTO AD_ClientInfo(" + m_stdColumns + ",Acct2_Active,Acct3_Active,"
			+ "AD_Tree_Menu_ID,AD_Tree_Org_ID,AD_Tree_BPartner_ID,AD_Tree_Project_ID,AD_Tree_SalesRegion_ID,AD_Tree_Product_ID)"
			+ " VALUES (" + m_stdValues + ",'N','N',"
			+ AD_Tree_Menu_ID + "," + AD_Tree_Org_ID + "," + AD_Tree_BPartner_ID + "," + AD_Tree_Project_ID + "," + AD_Tree_SalesRegion_ID + "," + AD_Tree_Product_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - ClientInfo NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
	//	m_info.append(Msg.translate(m_lang, "AD_ClientInfo")).append("\n");

		/**
		 *  Create Org
		 */
		AD_Org_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Org");
		m_stdValuesOrg = AD_Client_ID + "," + AD_Org_ID + ",'Y',SysDate,0,SysDate,0";

		name = orgName;
		if (name == null || name.length() == 0)
			name = "newOrg";
		m_orgName = name;
		sql = "INSERT INTO AD_Org (" + m_stdColumns + ",Value,Name,IsSummary)"
			+ " VALUES (" +	m_stdValuesOrg + ",'" + name + "','" + name + "','N')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Org NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_Org_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Roles
		 *  - Admin
		 *  - User
		 */
		name = m_clientName + " Admin";
		AD_Role_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Role");
		sql = "INSERT INTO AD_Role(" + m_stdColumns + ",AD_Role_ID,"
			+ "Name,Description,UserLevel,ClientList,OrgList)"
			+ " VALUES (" + m_stdValues + "," + AD_Role_ID + ","
			+ "'" + name + "','" + name + "',' CO','" + AD_Client_ID + "','0," + AD_Org_ID + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin Role A NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  ClientAccess x,0
		sql = "INSERT INTO AD_Role_ClientAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValues + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_ClientAccess NOT created");
		//  OrgAccess x,0
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValues + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_OrgAccess 0 NOT created");
		//  OrgAccess x,y
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValuesOrg + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_OrgAccess NOT created");
		//  Info - Admin Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		//
		name = m_clientName + " User";
		int AD_Role_ID_U = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Role");
		sql = "INSERT INTO AD_Role(" + m_stdColumns + ",AD_Role_ID,"
			+ "Name,Description,UserLevel,ClientList,OrgList)"
			+ " VALUES (" + m_stdValues + "," + AD_Role_ID_U + ","
			+ "'" + name + "','" + name + "','  O','" + AD_Client_ID + "','" + AD_Org_ID + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - User Role A NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  ClientAccess x,0
		sql = "INSERT INTO AD_Role_ClientAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValues + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - User Role_ClientAccess NOT created");
		//  OrgAccess x,y
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValuesOrg + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - User Role_OrgAccess NOT created");
		//  Info - Client Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Users
		 *  - Client
		 *  - Org
		 */
		name = userClient;
		if (name == null || name.length() == 0)
			name = m_clientName + "Client";
		AD_User_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_User");
		AD_User_Name = name;
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + ","
			+ "'" + name + "','" + name + "','" + name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin User A NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(name).append("/").append(name).append("\n");

		name = userOrg;
		if (name == null || name.length() == 0)
			name = m_clientName + "Org";
		AD_User_U_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_User");
		AD_User_U_Name = name;
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + ","
			+ "'" + name + "','" + name + "','" + name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Org User A NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(name).append("/").append(name).append("\n");

		/**
		 *  Create User-Role
		 */
		//  ClientUser          - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+Admin NOT iserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+User NOT iserted");
		//  OrgUser             - User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole OrgUser+Org NOT iserted");
		//  SuperUser(100)      - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + ",100," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole SuperUser+Admin NOT iserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + ",100," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole SuperUser+User NOT iserted");

		return true;
	}   //  createClient



	/*************************************************************************/

	/**
	 *  Create Accounting elements.
	 *  - Calendar
	 *  - Account Trees
	 *  - Account Values
	 *  - Accounting Schema
	 *  - Default Accounts
	 *
	 *  @param newC_Currency_ID currency
	 *  @param curName currency name
	 *  @param hasProduct has product segment
	 *  @param hasBPartner has bp segment
	 *  @param hasProject has project segment
	 *  @param hasMCampaign has campaign segment
	 *  @param hasSRegion has sales region segment
	 *  @param AccountingFile file name of accounting file
	 *  @return true if created
	 */
	public boolean createAccounting(int newC_Currency_ID, String curName,
		boolean hasProduct, boolean hasBPartner, boolean hasProject,
		boolean hasMCampaign, boolean hasSRegion,
		File AccountingFile)
	{
		Log.trace(Log.l3_Util, "MSetup.createAccount");
		//
		C_Currency_ID = newC_Currency_ID;
		m_hasProject = hasProject;
		m_hasMCampaign = hasMCampaign;
		m_hasSRegion = hasSRegion;

		//  Standard variables
		m_info = new StringBuffer();
		String name = null;
		StringBuffer sqlCmd = null;
		int no = 0;

		/**
		 *  Create Calendar
		 */
		C_Calendar_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Calendar");
		name = m_clientName + " " + Msg.translate(m_lang, "C_Calendar_ID");
		sqlCmd = new StringBuffer("INSERT INTO C_Calendar(");
		sqlCmd.append(m_stdColumns).append(",C_Calendar_ID,Name)").append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_Calendar_ID).append(",'").append(name).append("')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Calendar NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_Calendar_ID")).append("=").append(name).append("\n");

		//  Year
		int C_Year_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Year");
		sqlCmd = new StringBuffer ("INSERT INTO C_Year ");
		sqlCmd.append("(C_Year_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Year,C_Calendar_ID) VALUES (");
		sqlCmd.append(C_Year_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY'),").append(C_Calendar_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createAccounting - Year NOT inserted");
		/** @todo Create Periods */


		//	Create Account Elements
		int C_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Element");
		name = m_clientName + " " + Msg.translate(m_lang, "Account_ID");
		sqlCmd = new StringBuffer ("INSERT INTO C_ELEMENT(");
		sqlCmd.append(m_stdColumns).append(",C_Element_ID,Name,Description,")
			.append("VFormat,ElementType,IsBalancing,IsNaturalAccount,AD_Tree_ID)").append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_Element_ID).append(",'").append(name).append("','").append(name).append("',")
			.append("NULL,'A','N','Y',").append(AD_Tree_Account_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Acct Element NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		m_info.append(Msg.translate(m_lang, "C_Element_ID")).append("=").append(name).append("\n");

		//	Create Account Values
		m_nap = new NaturalAccountMap();
		String errMsg = m_nap.parseFile(AccountingFile);
		if (errMsg.length() != 0)
		{
			Log.error(errMsg);
			m_info.append(errMsg);
			return false;
		}
		if (m_nap.createAccounts(AD_Client_ID, AD_Org_ID, C_Element_ID))
			m_info.append(Msg.translate(m_lang, "C_ElementValue_ID")).append(" # ").append(m_nap.size()).append("\n");
		else
		{
			String err = "MSetup.createAccounting - Acct Element Values NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		int C_ElementValue_ID = m_nap.getC_ElementValue_ID("DEFAULT_ACCT");
		Log.trace(Log.l4_Data, "C_ElementValue_ID=" + C_ElementValue_ID);

		/**
		 *  Create AccountingSchema
		 */
		C_AcctSchema_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema");
		//
		String GAAP = "US";				//	AD_Reference_ID=123
		String CostingMethod = "A";		//	AD_Reference_ID=122
		name = m_clientName + " " + GAAP + "/" + CostingMethod + "/" + curName;
		//
		sqlCmd = new StringBuffer ("INSERT INTO C_AcctSchema(");
		sqlCmd.append(m_stdColumns).append(",C_AcctSchema_ID,Name,")
			.append("GAAP,IsAccrual,CostingMethod,C_Currency_ID,")
			.append("AutoPeriodControl,Separator,HasAlias,HasCombination)")
			.append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",'").append(name).append("',")
			.append("'").append(GAAP).append("','Y','").append(CostingMethod).append("',").append(C_Currency_ID).append(",")
			.append("'N','-','Y','N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - AcctSchema NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_AcctSchema_ID")).append("=").append(name).append("\n");

		/**
		 *  Create AccountingSchema Elements (Structure)
		 */
		String sql2 = null;
		if (Env.isBaseLanguage(m_lang, "AD_Reference"))	//	Get TreeTypes & Name
			sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=181";
		else
			sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Reference_ID=181 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID";
		//
		int Element_OO=0, Element_AC=0, Element_PR=0, Element_BP=0, Element_PJ=0,
			Element_MC=0, Element_SR=0;
		try
		{
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next())
			{
				String ElementType = rs.getString(1);
				name = rs.getString(2);
				//
				String IsMandatory = null;
				String IsBalanced = "N";
				int SeqNo = 0;
				int C_AcctSchema_Element_ID = 0;

				if (ElementType.equals("OO"))
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_OO = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					IsBalanced = "Y";
					SeqNo = 10;
				}
				else if (ElementType.equals("AC"))
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_AC = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					SeqNo = 20;
				}
				else if (ElementType.equals("PR") && hasProduct)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_PR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 30;
				}
				else if (ElementType.equals("BP") && hasBPartner)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_BP = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 40;
				}
				else if (ElementType.equals("PJ") && hasProject)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_PJ = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 50;
				}
				else if (ElementType.equals("MC") && hasMCampaign)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_MC = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 60;
				}
				else if (ElementType.equals("SR") && hasSRegion)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_SR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 70;
				}
				//	Not OT, LF, LT, U1, U2, AY

				if (IsMandatory != null)
				{
					sqlCmd = new StringBuffer ("INSERT INTO C_AcctSchema_Element(");
					sqlCmd.append(m_stdColumns).append(",C_AcctSchema_Element_ID,C_AcctSchema_ID,")
						.append("ElementType,Name,SeqNo,IsMandatory,IsBalanced) VALUES (");
					sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_Element_ID).append(",").append(C_AcctSchema_ID).append(",")
						.append("'").append(ElementType).append("','").append(name).append("',").append(SeqNo).append(",'")
						.append(IsMandatory).append("','").append(IsBalanced).append("')");
					no = DB.executeUpdate(sqlCmd.toString());
					if (no == 1)
						m_info.append(Msg.translate(m_lang, "C_AcctSchema_Element_ID")).append("=").append(name).append("\n");

					/** Default value for mandatory elements: OO and AC */
					if (ElementType.equals("OO"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET Org_ID=");
						sqlCmd.append(AD_Org_ID).append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Org in AcctSchamaElement NOT updated");
					}
					if (ElementType.equals("AC"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET C_ElementValue_ID=");
						sqlCmd.append(C_ElementValue_ID).append(", C_Element_ID=").append(C_Element_ID);
						sqlCmd.append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Account in AcctSchamaElement NOT updated");
					}
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e1)
		{
			Log.error ("MSetup.createAccounting - Elements", e1);
		}
		//  Create AcctSchema
		m_AcctSchema = new AcctSchema (C_AcctSchema_ID);


		//  Create GL Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_GL (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "USESUSPENSEBALANCING,SUSPENSEBALANCING_ACCT,"
			+ "USESUSPENSEERROR,SUSPENSEERROR_ACCT,"
			+ "USECURRENCYBALANCING,CURRENCYBALANCING_ACCT,"
			+ "RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,"
			+ "INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT,"
			+ "PPVOFFSET_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEBALANCING_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEERROR_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("CURRENCYBALANCING_ACCT")).append(",");
		//  RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,
		sqlCmd.append(getAcct("RETAINEDEARNING_ACCT")).append(",");
		sqlCmd.append(getAcct("INCOMESUMMARY_ACCT")).append(",");
		//  INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT)
		sqlCmd.append(getAcct("INTERCOMPANYDUETO_ACCT")).append(",");
		sqlCmd.append(getAcct("INTERCOMPANYDUEFROM_ACCT")).append(",");
		sqlCmd.append(getAcct("PPVOFFSET_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - GL Accounts NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//	Create Std Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_DEFAULT (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT, "
			+ "P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT, "
			+ "P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT, "
			+ "C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT, "
			+ "V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT, "
			+ "PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT, "
			+ "UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT, "
			+ "WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT, "
			+ "PJ_ASSET_ACCT,PJ_WIP_ACCT,"
			+ "T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT, "
			+ "B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,"
			+ "B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,"
			+ "B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT, "
			+ "CH_EXPENSE_ACCT,CH_REVENUE_ACCT, "
			+ "UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT, "
			+ "CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",");
		//  W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT
		sqlCmd.append(getAcct("W_INVENTORY_ACCT")).append(",");
		sqlCmd.append(getAcct("W_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("W_REVALUATION_ACCT")).append(",");
		sqlCmd.append(getAcct("W_INVACTUALADJUST_ACCT")).append(", ");
		//  P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT,
		sqlCmd.append(getAcct("P_REVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("P_COGS_ACCT")).append(", ");
		//  P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT,
		sqlCmd.append(getAcct("P_PURCHASEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_INVOICEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTREC_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTGRANT_ACCT")).append(", ");
		//  C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("C_RECEIVABLE_ACCT")).append(",");
		sqlCmd.append(getAcct("C_PREPAYMENT_ACCT")).append(", ");
		//  V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("V_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("V_LIABILITY_SERVICES_ACCT")).append(",");
		sqlCmd.append(getAcct("V_PREPAYMENT_ACCT")).append(", ");
		//  PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT,
		sqlCmd.append(getAcct("PAYDISCOUNT_EXP_ACCT")).append(",");
		sqlCmd.append(getAcct("PAYDISCOUNT_REV_ACCT")).append(",");
		sqlCmd.append(getAcct("WRITEOFF_ACCT")).append(", ");
		//  UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT,
		sqlCmd.append(getAcct("UNREALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("UNREALIZEDLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDLOSS_ACCT")).append(", ");
		//  WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT,
		sqlCmd.append(getAcct("WITHHOLDING_ACCT")).append(",");
		sqlCmd.append(getAcct("E_PREPAYMENT_ACCT")).append(",");
		sqlCmd.append(getAcct("E_EXPENSE_ACCT")).append(", ");
		//  PJ_ASSET_ACCT,PJ_WIP_ACCT,
		sqlCmd.append(getAcct("PJ_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("PJ_WIP_ACCT")).append(",");
		//  T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT,
		sqlCmd.append(getAcct("T_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("T_RECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("T_DUE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_CREDIT_ACCT")).append(", ");
		//  B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,
		sqlCmd.append(getAcct("B_INTRANSIT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("B_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTREV_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTEXP_ACCT")).append(",");
		//  B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,
		sqlCmd.append(getAcct("B_UNIDENTIFIED_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTLOSS_ACCT")).append(",");
		//  B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT,
		sqlCmd.append(getAcct("B_REVALUATIONGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_REVALUATIONLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("B_PAYMENTSELECT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_UNALLOCATEDCASH_ACCT")).append(", ");
		//  CH_EXPENSE_ACCT,CH_REVENUE_ACCT,
		sqlCmd.append(getAcct("CH_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CH_REVENUE_ACCT")).append(", ");
		//  UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT,
		sqlCmd.append(getAcct("UNEARNEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIPTS_ACCT")).append(", ");
		//  CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT)
		sqlCmd.append(getAcct("CB_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_CASHTRANSFER_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_RECEIPT_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Default Accounts NOT iserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//  GL Categories
		createGLCategory("Standard", "M", true);
		int GL_None = createGLCategory("None", "D", false);
		int GL_GL = createGLCategory("Manual", "M", false);
		int GL_ARI = createGLCategory("AR Invoice", "D", false);
		int GL_ARR = createGLCategory("AR Receipt", "D", false);
		int GL_MM = createGLCategory("Material Management", "D", false);
		int GL_API = createGLCategory("AP Invoice", "D", false);
		int GL_APP = createGLCategory("AP Payment", "D", false);

		//	Base DocumentTypes
		createDocType("GL Journal", "Journal", "GLJ", null, 0, 0, 1000, GL_GL);
		int DT_I = createDocType("AR Invoice", "Invoice", "ARI", null, 0, 0, 100000, GL_ARI);
		int DT_II = createDocType("AR Invoice Indirect", "Invoice", "ARI", null, 0, 0, 200000, GL_ARI);
		createDocType("AR Credit Memo", "Credit Memo", "ARC", null, 0, 0, 300000, GL_ARI);
		createDocType("AR Receipt", "Receipt", "ARR", null, 0, 0, 400000, GL_ARR);
		int DT_S  = createDocType("MM Shipment", "Delivery Note", "MMS", null, 0, 0, 500000, GL_MM);
		int DT_SI = createDocType("MM Shipment Indirect", "Delivery Note", "MMS", null, 0, 0, 600000, GL_MM);
		createDocType("MM Receipt", "Vendor Delivery", "MMR", null, 0, 0, 0, GL_MM);
		createDocType("AP Invoice", "Vendor Invoice", "API", null, 0, 0, 0, GL_API);
		createDocType("AP CreditMemo", "Vendor Credit Memo", "APC", null, 0, 0, 0, GL_API);
		createDocType("AP Payment", "Vendor Payment", "APP", null, 0, 0, 700000, GL_APP);
		createDocType("Purchase Order", "Purchase Order", "POO", null, 0, 0, 800000, GL_None);
		createDocType("Purchase Requisition", "Purchase Requisition", "POR", null, 0, 0, 900000, GL_None);

		//  Order Entry
		createDocType("Quotation", "Binding offer", "SOO", "OB", 0, 0, 10000, GL_None);
		createDocType("Proposal", "Non binding offer", "SOO", "ON", 0, 0, 20000, GL_None);
		createDocType("Prepay Order", "Prepay Order", "SOO", "PR", DT_S, DT_I, 30000, GL_None);
		createDocType("Return Material", "Return Material Authorization", "SOO", "RM", DT_S, DT_I, 40000, GL_None);
		createDocType("Standard Order", "Order Confirmation", "SOO", "SO", DT_S, DT_I, 50000, GL_None);
		createDocType("Credit Order", "Order Confirmation", "SOO", "WI", DT_SI, DT_I, 60000, GL_None);   //  RE
		createDocType("Warehouse Order", "Order Confirmation", "SOO", "WP", DT_S, DT_I, 70000, GL_None);    //  LS
		int DT = createDocType("POS Order", "Order Confirmation", "SOO", "WR", DT_SI, DT_II, 80000, GL_None);    // Bar
		createPreference("C_DocTypeTarget_ID", String.valueOf(DT), 143);

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_AcctSchema1_ID=").append(C_AcctSchema_ID)
			.append(", C_Calendar_ID=").append(C_Calendar_ID)
			.append(" WHERE AD_Client_ID=").append(AD_Client_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//
		return true;
	}   //  createAccounting

	/**
	 *  Get Account ID for key
	 *  @param key key
	 *  @return C_ValidCombination_ID
	 */
	private int getAcct (String key)
	{
		Log.trace(Log.l4_Data, "MSetup.getAcct - " + key);
		//  Element
		int C_ElementValue_ID = m_nap.getC_ElementValue_ID(key);
		Account vc = Account.getDefault(m_AcctSchema);
		vc.setAccount_ID(C_ElementValue_ID);
		vc.save(AD_Client_ID, 0);
		int C_ValidCombination_ID = vc.getC_ValidCombination_ID();

		//  Error
		if (C_ValidCombination_ID == 0)
			Log.error("MSetup.getAcct - no account for " + key);
		return C_ValidCombination_ID;
	}   //  getAcct

	/**
	 *  Create GL Category
	 *  @param Name name
	 *  @param CategoryType category type
	 *  @param isDefault is default value
	 *  @return GL_Category_ID
	 */
	private int createGLCategory (String Name, String CategoryType, boolean isDefault)
	{
		int GL_Category_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "GL_Category");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO GL_Category ");
		sqlCmd.append("(GL_Category_ID,").append(m_stdColumns).append(",")
			.append("Name,CategoryType,IsDefault) VALUES (")
			.append(GL_Category_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(Name).append("','").append(CategoryType).append("','")
			.append(isDefault ? "Y" : "N").append("')");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createGLCategory - GL Category NOT created - " + Name);
		//
		return GL_Category_ID;
	}   //  createGLCategory

	/**
	 *  Create Document Types with Sequence
	 *  @param Name name
	 *  @param PrintName print name
	 *  @param DocBaseType document base type
	 *  @param DocSubTypeSO sales order sub type
	 *  @param C_DocTypeShipment_ID shipment doc
	 *  @param C_DocTypeInvoice_ID invoice doc
	 *  @param StartNo start doc no
	 *  @param GL_Category_ID gl category
	 *  @return C_DocType_ID doc type
	 */
	private int createDocType (String Name, String PrintName,
		String DocBaseType, String DocSubTypeSO,
		int C_DocTypeShipment_ID, int C_DocTypeInvoice_ID,
		int StartNo, int GL_Category_ID)
	{
		StringBuffer sqlCmd = null;
		//  Get Sequence
		int AD_Sequence_ID = 0;
		if (StartNo != 0)   //  manual sequenec, if startNo == 0
		{
			AD_Sequence_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Sequence");
			sqlCmd = new StringBuffer ("INSERT INTO AD_Sequence ");
			sqlCmd.append("(AD_Sequence_ID,").append(m_stdColumns).append(",");
			sqlCmd.append("Name,IsAutoSequence,IncrementNo,StartNo,CurrentNext,");
			sqlCmd.append("CurrentNextSys,IsTableID) VALUES (");
			sqlCmd.append(AD_Sequence_ID).append(",").append(m_stdValues).append(",");
			sqlCmd.append("'").append(Name).append("','Y',1,").append(StartNo).append(",").append(StartNo).append(",");
			sqlCmd.append(StartNo/10).append(",'N')");
			int no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createDocType - Sequence NOT created - " + Name);
		}

		//  Get Document Type
		int C_DocType_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_DocType");
		sqlCmd = new StringBuffer ("INSERT INTO	C_DocType ");
		sqlCmd.append("(C_DocType_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,PrintName,DocBaseType,DocSubTypeSO,");
		sqlCmd.append("C_DocTypeShipment_ID,C_DocTypeInvoice_ID,");
		sqlCmd.append("IsDocNoControlled,DocNoSequence_ID,GL_Category_ID,");
		sqlCmd.append("IsPrinted,IsTransferred,DocumentCopies,IsSOTrx)");
		//
		sqlCmd.append(" VALUES (").append(C_DocType_ID).append(",").append(m_stdValues).append(",");
		//  Name,PrintName,DocBaseType,DocSubTypeSO,
		sqlCmd.append("'").append(Name).append("','").append(PrintName).append("','").append(DocBaseType).append("',");
		if (DocSubTypeSO == null || DocSubTypeSO.length() == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append("'").append(DocSubTypeSO).append("',");
		//  C_DocTypeShipment_ID,C_DocTypeInvoice_ID,
		if (C_DocTypeShipment_ID == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append(C_DocTypeShipment_ID).append(",");
		if (C_DocTypeInvoice_ID == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append(C_DocTypeInvoice_ID).append(",");
		//  IsDocNoControlled,DocNoSequence_ID,GL_Category_ID,
		if (AD_Sequence_ID == 0)
			sqlCmd.append("'N',NULL,");
		else
			sqlCmd.append("'Y',").append(AD_Sequence_ID).append(",");
		sqlCmd.append(GL_Category_ID).append(",");
		//  IsPrinted,IsTransferred,DocumentCopies,IsSOTrx)
		sqlCmd.append("'Y',");
		if (DocBaseType.equals("SOO"))
			sqlCmd.append("'N',");
		else
			sqlCmd.append("'Y',");
		sqlCmd.append("0,");
		if (DocBaseType.startsWith("AR") || DocBaseType.equals("MMS") || DocBaseType.equals("SOO"))
			sqlCmd.append("'Y'");
		else
			sqlCmd.append("'N'");
		sqlCmd.append(")");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createDocType - DocType NOT created - " + Name);
		//
		return C_DocType_ID;
	}   //  createDocType

	/*************************************************************************/

	/**
	 *  Create Default main entities.
	 *  - Dimensions & BPGroup, Prod Category)
	 *  - Location, Locator, Warehouse
	 *  - PriceList
	 *  - Cashbook, PaymentTerm
	 *  @param C_Country_ID country
	 *  @param City city
	 *  @param C_Region_ID region
	 *  @return true if created
	 */
	public boolean createEntities (int C_Country_ID, String City, int C_Region_ID)
	{
		Log.trace(Log.l3_Util, "MSetup.createEntries", "C_Country_ID=" + C_Country_ID + ", City=" + City + ", C_Region_ID=" + C_Region_ID);
		m_info.append("\n----\n");
		//
		String defaultName = Msg.translate(m_lang, "Standard");
		String defaultEntry = "'" + defaultName + "',";
		StringBuffer sqlCmd = null;
		int no = 0;

		//	Create Marketing Channel/Campaign
		int C_Channel_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Channel");
		sqlCmd = new StringBuffer("INSERT INTO C_Channel ");
		sqlCmd.append("(C_Channel_ID,Name,");
		sqlCmd.append(m_stdColumns).append(") VALUES (");
		sqlCmd.append(C_Channel_ID).append(",").append(defaultEntry);
		sqlCmd.append(m_stdValues).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Channel NOT inserted");
		int C_Campaign_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Campaign");
		sqlCmd = new StringBuffer("INSERT INTO C_Campaign ");
		sqlCmd.append("(C_Campaign_ID,C_Channel_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,Costs) VALUES (");
		sqlCmd.append(C_Campaign_ID).append(",").append(C_Channel_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Campaign_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Campaign NOT inserted");
		if (m_hasMCampaign)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Campaign_ID=").append(C_Campaign_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='MC'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Campaign NOT updated");
		}

		//	Create Sales Region
		int C_SalesRegion_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_SalesRegion");
		sqlCmd = new StringBuffer ("INSERT INTO C_SalesRegion ");
		sqlCmd.append("(C_SalesRegion_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsSummary) VALUES (");
		sqlCmd.append(C_SalesRegion_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_SalesRegion_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRegion NOT inserted");
		if (m_hasSRegion)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_SalesRegion_ID=").append(C_SalesRegion_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='SR'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement SalesRegion NOT updated");
		}

		/**
		 *  Business Partner
		 */
		//  Create BP Group
		int C_BP_Group_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BP_Group");
		sqlCmd = new StringBuffer ("INSERT INTO C_BP_Group ");
		sqlCmd.append("(C_BP_Group_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault) VALUES ( ");
		sqlCmd.append(C_BP_Group_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BP_Group_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BP Group NOT inserted");

		//	Create BPartner
		int C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_BP_Group_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BPartner_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BPartner NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("C_BPartner_ID=").append(C_BPartner_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
		sqlCmd.append(" AND ElementType='BP'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element BPartner NOT updated");
		createPreference("C_BPartner_ID", String.valueOf(C_BPartner_ID), 143);

		/**
		 *  Product
		 */
		//  Create Product Category
		int M_Product_Category_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Product_Category");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product_Category ");
		sqlCmd.append("(M_Product_Category_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault,PlannedMargin) VALUES (");
		sqlCmd.append(M_Product_Category_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y',0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_Category_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product Category NOT inserted");

		//  UOM (EA)
		int C_UOM_ID = 100;

		//  TaxCategory
		int C_TaxCategory_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_TaxCategory");
		sqlCmd = new StringBuffer ("INSERT INTO C_TaxCategory ");
		sqlCmd.append("(C_TaxCategory_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,IsDefault) VALUES (");
		sqlCmd.append(C_TaxCategory_ID).append(",").append(m_stdValues).append(", ");
		if (C_Country_ID == 100)    // US
			sqlCmd.append("'Sales Tax','Y')");
		else
			sqlCmd.append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - TaxCategory NOT inserted");

		//  Tax - Zero Rate
		int C_Tax_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Tax");
		sqlCmd = new StringBuffer ("INSERT INTO C_Tax ");
		sqlCmd.append("(C_Tax_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,Rate,C_TaxCategory_ID,C_Country_ID,TO_Country_ID,ValidFrom,IsDefault) VALUES (");
		sqlCmd.append(C_Tax_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append("0,").append(C_TaxCategory_ID).append(",")
			.append(C_Country_ID).append(",").append(C_Country_ID)
			.append(",TO_DATE('01-JAN-1990'),'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Tax NOT inserted");

		//	Create Product
		int M_Product_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Product");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product ");
		sqlCmd.append("(M_Product_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_UOM_ID,M_Product_Category_ID,C_TaxCategory_ID) VALUES (");
		sqlCmd.append(M_Product_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry);
		sqlCmd.append(C_UOM_ID).append(",").append(M_Product_Category_ID).append(",");
		sqlCmd.append(C_TaxCategory_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("M_Product_ID=").append(M_Product_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
		sqlCmd.append(" AND ElementType='PR'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element Product NOT updated");

		/**
		 *  Warehouse
		 */
		//  Location (Company)
		int C_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		sqlCmd = new StringBuffer ("UPDATE AD_OrgInfo SET C_Location_ID=");
		sqlCmd.append(C_Location_ID).append(" WHERE AD_Org_ID=").append(AD_Org_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Location NOT inserted");
		createPreference("C_Country_ID", String.valueOf(C_Country_ID), 0);

		//  Default Warehouse
		int M_Warehouse_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Warehouse");
		sqlCmd = new StringBuffer ("INSERT INTO M_Warehouse ");
		sqlCmd.append("(M_Warehouse_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Location_ID,Separator) VALUES (");
		sqlCmd.append(M_Warehouse_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Location_ID).append(",'-')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Warehouse NOT inserted");

		//   Locator
		int M_Locator_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Locator");
		sqlCmd = new StringBuffer ("INSERT INTO M_Locator ");
		sqlCmd.append("(M_Locator_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,M_Warehouse_ID,X,Y,Z,PriorityNo,IsDefault) VALUES (");
		sqlCmd.append(M_Locator_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(M_Warehouse_ID).append(",0,0,0,50,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Locator NOT inserted");

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_BPartnerCashTrx_ID=").append(C_BPartner_ID);
		sqlCmd.append(",M_ProductFreight_ID=").append(M_Product_ID);
//		sqlCmd.append("C_UOM_Volume_ID=");
//		sqlCmd.append(",C_UOM_Weight_ID=");
//		sqlCmd.append(",C_UOM_Length_ID=");
//		sqlCmd.append(",C_UOM_Time_ID=");
		sqlCmd.append(" WHERE AD_Client_ID=").append(AD_Client_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createEntities - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		/**
		 *  Other
		 */
		//  PriceList
		int M_PriceList_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_PriceList");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList ");
		sqlCmd.append("(M_PriceList_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID,IsDefault) VALUES (");
		sqlCmd.append(M_PriceList_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(",'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList NOT inserted");
		//  DiscountSchema
		int M_DiscountSchema_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_DiscountSchema");
		sqlCmd = new StringBuffer ("INSERT INTO M_DiscountSchema ");
		sqlCmd.append("(M_DiscountSchema_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,DiscountType, FlatDiscount) VALUES (");
		sqlCmd.append(M_DiscountSchema_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append("SysDate,'F',0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - DiscountSchema NOT inserted");
		//  PriceList Version
		int M_PriceList_Version_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_PriceList_Version");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList_Version ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,M_PriceList_ID,M_DiscountSchema_ID) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY-MM-DD'),SysDate,").append(M_PriceList_ID)
			.append(",").append(M_DiscountSchema_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList_Version NOT inserted");
		//  ProductPrice
		sqlCmd = new StringBuffer ("INSERT INTO M_ProductPrice ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("M_Product_ID,PriceList,PriceStd,PriceLimit) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(M_Product_ID).append(",1,1,1)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - ProductPrice NOT inserted");

		//  Location for Standard BP
		C_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");

		DB.executeUpdate(sqlCmd.toString());
		int C_BPartner_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(City).append("',").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location NOT inserted");

		//	Create Sales Rep for User
		C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_U_Name).append("','").append(AD_User_U_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "IsSalesRep")).append("=").append(AD_User_U_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (User) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_U_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (User) NOT updated");

		//	Create Sales Rep for Admin
		C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_Name).append("','").append(AD_User_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "IsSalesRep")).append("=").append(AD_User_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (Admin) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (Admin) NOT updated");

		//  Payment Term
		int C_PaymentTerm_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_PaymentTerm");
		sqlCmd = new StringBuffer ("INSERT INTO C_PaymentTerm ");
		sqlCmd.append("(C_PaymentTerm_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,NetDays,GraceDays,DiscountDays,Discount,DiscountDays2,Discount2,IsDefault) VALUES (");
		sqlCmd.append(C_PaymentTerm_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'Immediate','Immediate',0,0,0,0,0,0,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PaymentTerm NOT inserted");

		//  Project Cycle
		C_Cycle_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Cycle");
		sqlCmd = new StringBuffer ("INSERT INTO C_Cycle ");
		sqlCmd.append("(C_Cycle_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_Cycle_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Cycle NOT inserted");

		//  ProjectStatus
		int PS = createProjectStatus(110, "Prospect", "");
		createProjectStatus(120, "Qualified", "");
		createProjectStatus(130, "Opportunity", "Has Budget and Timeframe");
		createProjectStatus(140, "Solution", "Need Analysis");
		createProjectStatus(150, "Quote", "");
		createProjectStatus(160, "Close", "Contract Negotiations");
		createProjectStatus(200, "Delivery", "Delivery of Goods and Service");
		createPreference("C_ProjectStatus_ID", String.valueOf(PS), 0);

		/**
		 *  Organization level data	===========================================
		 */

		//	Create Default Project
		int C_Project_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Project");
		sqlCmd = new StringBuffer ("INSERT INTO C_Project ");
		sqlCmd.append("(C_Project_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Currency_ID,IsSummary) VALUES (");
		sqlCmd.append(C_Project_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Currency_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Project_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Project NOT inserted");
		//  Default Project
		if (m_hasProject)
		{
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Project_ID=").append(C_Project_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='PJ'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Project NOT updated");
		}

		//  CashBook
		int C_CashBook_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_CashBook");
		sqlCmd = new StringBuffer ("INSERT INTO C_CashBook ");
		sqlCmd.append("(C_CashBook_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_CashBook_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_CashBook_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - CashBook NOT inserted");


		//  Create Other Defaults
		try
		{
			CallableStatement cstmt = DB.prepareCall("CALL AD_Setup(?,?)");
			cstmt.setInt(1, AD_Client_ID);
			cstmt.setInt(2, AD_Org_ID);
			cstmt.execute();
			cstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("MSetup.CreateEntities - Call AD_Setup", e);
		}

		return true;
	}   //  createEntities

	/**
	 *  Create Project Status and CycleStep
	 *  @param SeqNo seq
	 *  @param Name name
	 *  @param Description description
	 *  @return C_ProjectStatus_ID
	 */
	private int createProjectStatus (int SeqNo, String Name, String Description)
	{
		int C_ProjectStatus_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_ProjectStatus");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO C_ProjectStatus ");
		sqlCmd.append("(C_ProjectStatus_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("SeqNo,Name,Description) VALUES (");
		sqlCmd.append(C_ProjectStatus_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(SeqNo).append(",'").append(Name).append("','").append(Description).append("')");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createProjectStatus - ProjectStatus NOT inserted - " + Name);
		//
		int C_CycleStep_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_CycleStep");
		sqlCmd = new StringBuffer ("INSERT INTO C_CycleStep ");
		sqlCmd.append("(C_CycleStep_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("C_Cycle_ID,SeqNo,C_ProjectStatus_ID,RelativeWeight) VALUES (");
		sqlCmd.append(C_CycleStep_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(C_Cycle_ID).append(",").append(SeqNo).append(",").append(C_ProjectStatus_ID).append(",1)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createProjectStatus - CycleStep NOT inserted - " + Name);
		//
		return C_ProjectStatus_ID;
	}   //  createProjectStatus

	/**
	 *  Create Preference
	 *  @param Attribute attribute
	 *  @param Value value
	 *  @param AD_Window_ID window
	 */
	private void createPreference (String Attribute, String Value, int AD_Window_ID)
	{
		int AD_Preference_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Preference");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO AD_Preference ");
		sqlCmd.append("(AD_Preference_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Attribute,Value,AD_Window_ID) VALUES (");
		sqlCmd.append(AD_Preference_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(Attribute).append("','").append(Value).append("',");
		if (AD_Window_ID == 0)
			sqlCmd.append("NULL)");
		else
			sqlCmd.append(AD_Window_ID).append(")");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createPreference - Preference NOT inserted - " + Attribute);
	}   //  createPreference

	/*************************************************************************/

	/**
	 *  Get Client
	 *  @return AD_Client_ID
	 */
	public int getAD_Client_ID()
	{
		return AD_Client_ID;
	}
	public int getAD_Org_ID()
	{
		return AD_Org_ID;
	}
	public int getAD_User_ID()
	{
		return AD_User_ID;
	}
	public int getAD_Role_ID()
	{
		return AD_Role_ID;
	}
	public int getC_AcctSchema_ID()
	{
		return C_AcctSchema_ID;
	}
	public String getInfo()
	{
		return m_info.toString();
	}
}   //  MSetup
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
package org.compiere.model;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.compiere.util.*;

/**
 * Initial Setup Model
 *
 * @author Jorg Janke
 * @version $Id: MSetup.java,v 1.41 2004/09/09 14:14:32 jjanke Exp $
 */
public final class MSetup
{
	/**
	 *  Constructor
	 *  @param ctx context
	 *  @param WindowNo window
	 */
	public MSetup(Properties ctx, int WindowNo)
	{
		m_ctx = new Properties(ctx);	//	copy
		m_lang = Env.getAD_Language(m_ctx);
		m_WindowNo = WindowNo;
	}   //  MSetup

	private Properties      m_ctx;
	private String          m_lang;
	private int             m_WindowNo;
	private StringBuffer    m_info;
	//
	private String          m_clientName;
//	private String          m_orgName;
	//
	private String          m_stdColumns = "AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy";
	private String          m_stdValues;
	private String          m_stdValuesOrg;
	//
	private NaturalAccountMap m_nap = null;
	//
	private MClient			m_client;
	private MOrg			m_org;
	private MAcctSchema		m_as;
	//
	private int     		AD_User_ID;
	private String  		AD_User_Name;
	private int     		AD_User_U_ID;
	private String  		AD_User_U_Name;
	private int     		C_Calendar_ID;
	private int     		m_AD_Tree_Account_ID;
	private int     		C_Cycle_ID;
	//
	private boolean         m_hasProject = false;
	private boolean         m_hasMCampaign = false;
	private boolean         m_hasSRegion = false;


	/**
	 *  Create Client Info.
	 *  - Client, Trees, Org, Role, User, User_Role
	 *  @param clientName client name
	 *  @param orgName org name
	 *  @param userClient user id client
	 *  @param userOrg user id org
	 *  @return true if created
	 */
	public boolean createClient (String clientName, String orgName,
		String userClient, String userOrg)
	{
		Log.trace(Log.l3_Util, "MSetup.createClient - " + clientName);
		//  info header
		m_info = new StringBuffer();
		//  Standarc columns
		String name = null;
		String sql = null;
		int no = 0;

		/**
		 *  Create Client
		 */
		name = clientName;
		if (name == null || name.length() == 0)
			name = "newClient";
		m_clientName = name;
		m_client = new MClient(m_ctx, 0, true);
		m_client.setValue(m_clientName);
		m_client.setName(m_clientName);
		if (!m_client.save())
		{
			String err = "MSetup.createClient - Client NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		int AD_Client_ID = m_client.getAD_Client_ID();
		Env.setContext(m_ctx, m_WindowNo, "AD_Client_ID", AD_Client_ID);
		Env.setContext(m_ctx, "#AD_Client_ID", AD_Client_ID);

		//	Standard Values
		m_stdValues = String.valueOf(AD_Client_ID) + ",0,'Y',SysDate,0,SysDate,0";
		//  Info - Client
		m_info.append(Msg.translate(m_lang, "AD_Client_ID")).append("=").append(name).append("\n");

		//	Setup Sequences
		if (!MSequence.checkClientSequences (m_ctx, AD_Client_ID))
		{
			String err = "MSetup.createClient - Sequences NOT created";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		
		//  Trees and Client Info
		if (!m_client.setupClientInfo(m_lang))
		{
			String err = "MSetup.createClient - ClientInfo NOT created";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		m_AD_Tree_Account_ID = m_client.getSetup_AD_Tree_Account_ID();

		/**
		 *  Create Org
		 */
		name = orgName;
		if (name == null || name.length() == 0)
			name = "newOrg";
		m_org = new MOrg (m_client, name);
		if (!m_org.save())
		{
			String err = "MSetup.createClient - Org NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		Env.setContext(m_ctx, m_WindowNo, "AD_Org_ID", getAD_Org_ID());
		Env.setContext(m_ctx, "#AD_Org_ID", getAD_Org_ID());
		m_stdValuesOrg = AD_Client_ID + "," + getAD_Org_ID() + ",'Y',SysDate,0,SysDate,0";
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_Org_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Roles
		 *  - Admin
		 *  - User
		 */
		name = m_clientName + " Admin";
		MRole admin = new MRole(m_ctx, 0);
		admin.setClientOrg(m_client);
		admin.setName(name);
		admin.setUserLevel(MRole.USERLEVEL_ClientPlusOrganization);
		admin.setIsShowAcct(true);
		if (!admin.save())
		{
			String err = "MSetup.createClient - Admin Role A NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//	OrgAccess x, 0
		MRoleOrgAccess adminClientAccess = new MRoleOrgAccess (admin, 0);
		if (!adminClientAccess.save())
			Log.error("MSetup.createClient - Admin Role_OrgAccess 0 NOT created");
		//  OrgAccess x,y
		MRoleOrgAccess adminOrgAccess = new MRoleOrgAccess (admin, m_org.getAD_Org_ID());
		if (!adminOrgAccess.save())
			Log.error("MSetup.createClient - Admin Role_OrgAccess NOT created");
		
		//  Info - Admin Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		//
		name = m_clientName + " User";
		MRole user = new MRole (m_ctx, 0);
		user.setClientOrg(m_client);
		user.setName(name);
		if (!user.save())
		{
			String err = "MSetup.createClient - User Role A NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  OrgAccess x,y
		MRoleOrgAccess userOrgAccess = new MRoleOrgAccess (user, m_org.getAD_Org_ID());
		if (!userOrgAccess.save())
			Log.error("MSetup.createClient - User Role_OrgAccess NOT created");
		
		//  Info - Client Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Users
		 *  - Client
		 *  - Org
		 */
		name = userClient;
		if (name == null || name.length() == 0)
			name = m_clientName + "Client";
		AD_User_ID = getNextID(AD_Client_ID, "AD_User");
		AD_User_Name = name;
		name = DB.TO_STRING(name);
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + ","
			+ name + "," + name + "," + name + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin User NOT inserted - " + AD_User_Name;
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_Name).append("/").append(AD_User_Name).append("\n");

		name = userOrg;
		if (name == null || name.length() == 0)
			name = m_clientName + "Org";
		AD_User_U_ID = getNextID(AD_Client_ID, "AD_User");
		AD_User_U_Name = name;
		name = DB.TO_STRING(name);
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + ","
			+ name + "," + name + "," + name + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Org User NOT inserted - " + AD_User_U_Name;
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_U_Name).append("/").append(AD_User_U_Name).append("\n");

		/**
		 *  Create User-Role
		 */
		//  ClientUser          - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + admin.getAD_Role_ID() + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+Admin NOT inserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + user.getAD_Role_ID() + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+User NOT inserted");
		//  OrgUser             - User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + "," + user.getAD_Role_ID() + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole OrgUser+Org NOT inserted");

		//	Processors
		MAcctProcessor ap = new MAcctProcessor(m_client, AD_User_ID);
		ap.save();
		
		MRequestProcessor rp = new MRequestProcessor (m_client, AD_User_ID);
		rp.save();
		
		return true;
	}   //  createClient



	/**************************************************************************
	 *  Create Accounting elements.
	 *  - Calendar
	 *  - Account Trees
	 *  - Account Values
	 *  - Accounting Schema
	 *  - Default Accounts
	 *
	 *  @param currency currency
	 *  @param hasProduct has product segment
	 *  @param hasBPartner has bp segment
	 *  @param hasProject has project segment
	 *  @param hasMCampaign has campaign segment
	 *  @param hasSRegion has sales region segment
	 *  @param AccountingFile file name of accounting file
	 *  @return true if created
	 */
	public boolean createAccounting(KeyNamePair currency,
		boolean hasProduct, boolean hasBPartner, boolean hasProject,
		boolean hasMCampaign, boolean hasSRegion,
		File AccountingFile)
	{
		Log.trace(Log.l3_Util, "MSetup.createAccounting - " + m_client);
		//
		m_hasProject = hasProject;
		m_hasMCampaign = hasMCampaign;
		m_hasSRegion = hasSRegion;

		//  Standard variables
		m_info = new StringBuffer();
		String name = null;
		StringBuffer sqlCmd = null;
		int no = 0;

		/**
		 *  Create Calendar
		 */
		C_Calendar_ID = getNextID(m_client.getAD_Client_ID(), "C_Calendar");
		name = m_clientName + " " + Msg.translate(m_lang, "C_Calendar_ID");
		name = DB.TO_STRING(name);
		sqlCmd = new StringBuffer("INSERT INTO C_Calendar(");
		sqlCmd.append(m_stdColumns).append(",C_Calendar_ID,Name)").append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_Calendar_ID).append(",").append(name).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Calendar NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_Calendar_ID")).append("=").append(name).append("\n");

		//  Year
		int C_Year_ID = getNextID(m_client.getAD_Client_ID(), "C_Year");
		sqlCmd = new StringBuffer ("INSERT INTO C_Year ");
		sqlCmd.append("(C_Year_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Year,C_Calendar_ID) VALUES (");
		sqlCmd.append(C_Year_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY'),").append(C_Calendar_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createAccounting - Year NOT inserted");
		//	TODO Create Periods


		//	Create Account Elements
		name = m_clientName + " " + Msg.translate(m_lang, "Account_ID");
		MElement element = new MElement (m_client, name, 
			MElement.ELEMENTTYPE_Account, m_AD_Tree_Account_ID);
		if (!element.save())
		{
			String err = "MSetup.createAccounting - Acct Element NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		int C_Element_ID = element.getC_Element_ID();
		m_info.append(Msg.translate(m_lang, "C_Element_ID")).append("=").append(name).append("\n");

		//	Create Account Values
		m_nap = new NaturalAccountMap(m_ctx);
		String errMsg = m_nap.parseFile(AccountingFile);
		if (errMsg.length() != 0)
		{
			Log.error(errMsg);
			m_info.append(errMsg);
			return false;
		}
		if (m_nap.saveAccounts(getAD_Client_ID(), getAD_Org_ID(), C_Element_ID))
			m_info.append(Msg.translate(m_lang, "C_ElementValue_ID")).append(" # ").append(m_nap.size()).append("\n");
		else
		{
			String err = "MSetup.createAccounting - Acct Element Values NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		int C_ElementValue_ID = m_nap.getC_ElementValue_ID("DEFAULT_ACCT");
		Log.trace(Log.l4_Data, "C_ElementValue_ID=" + C_ElementValue_ID);

		/**
		 *  Create AccountingSchema
		 */
		m_as = new MAcctSchema (m_client, currency);
		if (!m_as.save())
		{
			String err = "MSetup.createAccounting - AcctSchema NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_AcctSchema_ID")).append("=").append(m_as.getName()).append("\n");

		/**
		 *  Create AccountingSchema Elements (Structure)
		 */
		String sql2 = null;
		if (Env.isBaseLanguage(m_lang, "AD_Reference"))	//	Get ElementTypes & Name
			sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=181";
		else
			sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Reference_ID=181 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID";
		//
		int Element_OO=0, Element_AC=0, Element_PR=0, Element_BP=0, Element_PJ=0,
			Element_MC=0, Element_SR=0;
		try
		{
			int AD_Client_ID = m_client.getAD_Client_ID();
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next())
			{
				String ElementType = rs.getString(1);
				name = rs.getString(2);
				//
				String IsMandatory = null;
				String IsBalanced = "N";
				int SeqNo = 0;
				int C_AcctSchema_Element_ID = 0;

				if (ElementType.equals("OO"))
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_OO = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					IsBalanced = "Y";
					SeqNo = 10;
				}
				else if (ElementType.equals("AC"))
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_AC = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					SeqNo = 20;
				}
				else if (ElementType.equals("PR") && hasProduct)
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_PR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 30;
				}
				else if (ElementType.equals("BP") && hasBPartner)
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_BP = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 40;
				}
				else if (ElementType.equals("PJ") && hasProject)
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_PJ = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 50;
				}
				else if (ElementType.equals("MC") && hasMCampaign)
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_MC = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 60;
				}
				else if (ElementType.equals("SR") && hasSRegion)
				{
					C_AcctSchema_Element_ID = getNextID(AD_Client_ID, "C_AcctSchema_Element");
					Element_SR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 70;
				}
				//	Not OT, LF, LT, U1, U2, AY

				if (IsMandatory != null)
				{
					sqlCmd = new StringBuffer ("INSERT INTO C_AcctSchema_Element(");
					sqlCmd.append(m_stdColumns).append(",C_AcctSchema_Element_ID,C_AcctSchema_ID,")
						.append("ElementType,Name,SeqNo,IsMandatory,IsBalanced) VALUES (");
					sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_Element_ID).append(",").append(m_as.getC_AcctSchema_ID()).append(",")
						.append("'").append(ElementType).append("','").append(name).append("',").append(SeqNo).append(",'")
						.append(IsMandatory).append("','").append(IsBalanced).append("')");
					no = DB.executeUpdate(sqlCmd.toString());
					if (no == 1)
						m_info.append(Msg.translate(m_lang, "C_AcctSchema_Element_ID")).append("=").append(name).append("\n");

					/** Default value for mandatory elements: OO and AC */
					if (ElementType.equals("OO"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET Org_ID=");
						sqlCmd.append(getAD_Org_ID()).append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Org in AcctSchamaElement NOT updated");
					}
					if (ElementType.equals("AC"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET C_ElementValue_ID=");
						sqlCmd.append(C_ElementValue_ID).append(", C_Element_ID=").append(C_Element_ID);
						sqlCmd.append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Account in AcctSchamaElement NOT updated");
					}
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e1)
		{
			Log.error ("MSetup.createAccounting - Elements", e1);
		}
		//  Create AcctSchema


		//  Create GL Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_GL (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "USESUSPENSEBALANCING,SUSPENSEBALANCING_ACCT,"
			+ "USESUSPENSEERROR,SUSPENSEERROR_ACCT,"
			+ "USECURRENCYBALANCING,CURRENCYBALANCING_ACCT,"
			+ "RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,"
			+ "INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT,"
			+ "PPVOFFSET_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(m_as.getC_AcctSchema_ID()).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEBALANCING_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEERROR_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("CURRENCYBALANCING_ACCT")).append(",");
		//  RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,
		sqlCmd.append(getAcct("RETAINEDEARNING_ACCT")).append(",");
		sqlCmd.append(getAcct("INCOMESUMMARY_ACCT")).append(",");
		//  INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT)
		sqlCmd.append(getAcct("INTERCOMPANYDUETO_ACCT")).append(",");
		sqlCmd.append(getAcct("INTERCOMPANYDUEFROM_ACCT")).append(",");
		sqlCmd.append(getAcct("PPVOFFSET_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - GL Accounts NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//	Create Std Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_DEFAULT (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT, "
			+ "P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT, "
			+ "P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT, "
			+ "C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT, "
			+ "V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT, "
			+ "PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT, "
			+ "UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT, "
			+ "WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT, "
			+ "PJ_ASSET_ACCT,PJ_WIP_ACCT,"
			+ "T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT, "
			+ "B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,"
			+ "B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,"
			+ "B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT, "
			+ "CH_EXPENSE_ACCT,CH_REVENUE_ACCT, "
			+ "UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT, "
			+ "CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(m_as.getC_AcctSchema_ID()).append(",");
		//  W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT
		sqlCmd.append(getAcct("W_INVENTORY_ACCT")).append(",");
		sqlCmd.append(getAcct("W_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("W_REVALUATION_ACCT")).append(",");
		sqlCmd.append(getAcct("W_INVACTUALADJUST_ACCT")).append(", ");
		//  P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT,
		sqlCmd.append(getAcct("P_REVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("P_COGS_ACCT")).append(", ");
		//  P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT,
		sqlCmd.append(getAcct("P_PURCHASEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_INVOICEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTREC_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTGRANT_ACCT")).append(", ");
		//  C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("C_RECEIVABLE_ACCT")).append(",");
		sqlCmd.append(getAcct("C_PREPAYMENT_ACCT")).append(", ");
		//  V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("V_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("V_LIABILITY_SERVICES_ACCT")).append(",");
		sqlCmd.append(getAcct("V_PREPAYMENT_ACCT")).append(", ");
		//  PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT,
		sqlCmd.append(getAcct("PAYDISCOUNT_EXP_ACCT")).append(",");
		sqlCmd.append(getAcct("PAYDISCOUNT_REV_ACCT")).append(",");
		sqlCmd.append(getAcct("WRITEOFF_ACCT")).append(", ");
		//  UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT,
		sqlCmd.append(getAcct("UNREALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("UNREALIZEDLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDLOSS_ACCT")).append(", ");
		//  WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT,
		sqlCmd.append(getAcct("WITHHOLDING_ACCT")).append(",");
		sqlCmd.append(getAcct("E_PREPAYMENT_ACCT")).append(",");
		sqlCmd.append(getAcct("E_EXPENSE_ACCT")).append(", ");
		//  PJ_ASSET_ACCT,PJ_WIP_ACCT,
		sqlCmd.append(getAcct("PJ_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("PJ_WIP_ACCT")).append(",");
		//  T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT,
		sqlCmd.append(getAcct("T_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("T_RECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("T_DUE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_CREDIT_ACCT")).append(", ");
		//  B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,
		sqlCmd.append(getAcct("B_INTRANSIT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("B_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTREV_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTEXP_ACCT")).append(",");
		//  B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,
		sqlCmd.append(getAcct("B_UNIDENTIFIED_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTLOSS_ACCT")).append(",");
		//  B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT,
		sqlCmd.append(getAcct("B_REVALUATIONGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_REVALUATIONLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("B_PAYMENTSELECT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_UNALLOCATEDCASH_ACCT")).append(", ");
		//  CH_EXPENSE_ACCT,CH_REVENUE_ACCT,
		sqlCmd.append(getAcct("CH_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CH_REVENUE_ACCT")).append(", ");
		//  UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT,
		sqlCmd.append(getAcct("UNEARNEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIPTS_ACCT")).append(", ");
		//  CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT)
		sqlCmd.append(getAcct("CB_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_CASHTRANSFER_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_RECEIPT_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Default Accounts NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//  GL Categories
		createGLCategory("Standard", "M", true);
		int GL_None = createGLCategory("None", "D", false);
		int GL_GL = createGLCategory("Manual", "M", false);
		int GL_ARI = createGLCategory("AR Invoice", "D", false);
		int GL_ARR = createGLCategory("AR Receipt", "D", false);
		int GL_MM = createGLCategory("Material Management", "D", false);
		int GL_API = createGLCategory("AP Invoice", "D", false);
		int GL_APP = createGLCategory("AP Payment", "D", false);
		int GL_CASH = createGLCategory("Cash/Payments", "D", false);

		//	Base DocumentTypes
		createDocType("GL Journal", "Journal", 
			MDocType.DOCBASETYPE_GLJournal, null, 0, 0, 1000, GL_GL);
		createDocType("GL Journal Batch", "Journal Batch", 
			MDocType.DOCBASETYPE_GLJournal, null, 0, 0, 100, GL_GL);
		//	MDocType.DOCBASETYPE_GLDocument
		//
		int DT_I = createDocType("AR Invoice", "Invoice", 
			MDocType.DOCBASETYPE_ARInvoice, null, 0, 0, 100000, GL_ARI);
		int DT_II = createDocType("AR Invoice Indirect", "Invoice", 
			MDocType.DOCBASETYPE_ARInvoice, null, 0, 0, 150000, GL_ARI);
		int DT_IC = createDocType("AR Credit Memo", "Credit Memo", 
			MDocType.DOCBASETYPE_ARCreditMemo, null, 0, 0, 170000, GL_ARI);
		//	MDocType.DOCBASETYPE_ARProFormaInvoice
		
		createDocType("AP Invoice", "Vendor Invoice", 
			MDocType.DOCBASETYPE_APInvoice, null, 0, 0, 0, GL_API);
		createDocType("AP CreditMemo", "Vendor Credit Memo", 
			MDocType.DOCBASETYPE_APCreditMemo, null, 0, 0, 0, GL_API);
		createDocType("Match Invoice", "Match Invoice", 
			MDocType.DOCBASETYPE_MatchInvoice, null, 0, 0, 390000, GL_API);
		
		createDocType("AR Receipt", "Customer Payment", 
			MDocType.DOCBASETYPE_ARReceipt, null, 0, 0, 0, GL_ARR);
		createDocType("AP Payment", "Vendor Payment", 
			MDocType.DOCBASETYPE_APPayment, null, 0, 0, 0, GL_APP);
		createDocType("Allocation", "Allocation", 
			MDocType.DOCBASETYPE_PaymentAllocation, null, 0, 0, 490000, GL_CASH);

		int DT_S  = createDocType("MM Shipment", "Delivery Note", 
			MDocType.DOCBASETYPE_MaterialDelivery, null, 0, 0, 500000, GL_MM);
		int DT_SI = createDocType("MM Shipment Indirect", "Delivery Note", 
			MDocType.DOCBASETYPE_MaterialDelivery, null, 0, 0, 550000, GL_MM);
		
		createDocType("MM Receipt", "Vendor Delivery", 
			MDocType.DOCBASETYPE_MaterialReceipt, null, 0, 0, 0, GL_MM);
		int DT_RM = createDocType("MM Returns", "Customer Returns", 
			MDocType.DOCBASETYPE_MaterialReceipt, null, 0, 0, 570000, GL_MM);
		
		createDocType("Purchase Order", "Purchase Order", 
			MDocType.DOCBASETYPE_PurchaseOrder, null, 0, 0, 800000, GL_None);
		createDocType("Match PO", "Patch PO", 
			MDocType.DOCBASETYPE_MatchPO, null, 0, 0, 890000, GL_None);
		createDocType("Purchase Requisition", "Purchase Requisition", 
			MDocType.DOCBASETYPE_PurchaseRequisition, null, 0, 0, 900000, GL_None);

		createDocType("Bank Statement", "Bank Statement", 
			MDocType.DOCBASETYPE_BankStatement, null, 0, 0, 700000, GL_CASH);
		createDocType("Cash Journal", "Cash Journal",
			MDocType.DOCBASETYPE_CashJournal, null, 0, 0, 750000, GL_CASH);
		
		createDocType("Material Movement", "Material Movement",
			MDocType.DOCBASETYPE_MaterialMovement, null, 0, 0, 610000, GL_MM);
		createDocType("Physical Inventory", "Physical Inventory", 
			MDocType.DOCBASETYPE_MaterialPhysicalInventory, null, 0, 0, 620000, GL_MM);
		createDocType("Material Production", "Material Production", 
			MDocType.DOCBASETYPE_MaterialProduction, null, 0, 0, 630000, GL_MM);
		createDocType("Project Issue", "Project Issue", 
			MDocType.DOCBASETYPE_ProjectIssue, null, 0, 0, 640000, GL_MM);

		//  Order Entry
		createDocType("Binding offer", "Quotation", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_Quotation, 
			0, 0, 10000, GL_None);
		createDocType("Non binding offer", "Proposal", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_Proposal, 
			0, 0, 20000, GL_None);
		createDocType("Prepay Order", "Prepay Order", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_PrepayOrder, 
			DT_S, DT_I, 30000, GL_None);
		createDocType("Return Material", "Return Material Authorization", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_ReturnMaterial, 
			DT_RM, DT_IC, 30000, GL_None);
		createDocType("Standard Order", "Order Confirmation", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_StandardOrder, 
			DT_S, DT_I, 50000, GL_None);
		createDocType("Credit Order", "Order Confirmation", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_OnCreditOrder, 
			DT_SI, DT_I, 60000, GL_None);   //  RE
		createDocType("Warehouse Order", "Order Confirmation", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_WarehouseOrder, 
			DT_S, DT_I,	70000, GL_None);    //  LS
		int DT = createDocType("POS Order", "Order Confirmation", 
			MDocType.DOCBASETYPE_SalesOrder, MDocType.DOCSUBTYPESO_POSOrder, 
			DT_SI, DT_II, 80000, GL_None);    // Bar
		//	POS As Default for window SO
		createPreference("C_DocTypeTarget_ID", String.valueOf(DT), 143);

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_AcctSchema1_ID=").append(m_as.getC_AcctSchema_ID())
			.append(", C_Calendar_ID=").append(C_Calendar_ID)
			.append(" WHERE AD_Client_ID=").append(m_client.getAD_Client_ID());
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//
		return true;
	}   //  createAccounting

	/**
	 *  Get Account ID for key
	 *  @param key key
	 *  @return C_ValidCombination_ID
	 */
	private int getAcct (String key)
	{
		Log.trace(Log.l4_Data, "MSetup.getAcct - " + key);
		//  Element
		int C_ElementValue_ID = m_nap.getC_ElementValue_ID(key);
		MAccount vc = MAccount.getDefault(m_as, true);	//	optional null
		vc.setAccount_ID(C_ElementValue_ID);
		vc.save();
		int C_ValidCombination_ID = vc.getC_ValidCombination_ID();

		//  Error
		if (C_ValidCombination_ID == 0)
			Log.error("MSetup.getAcct - no account for " + key);
		return C_ValidCombination_ID;
	}   //  getAcct

	/**
	 *  Create GL Category
	 *  @param Name name
	 *  @param CategoryType category type
	 *  @param isDefault is default value
	 *  @return GL_Category_ID
	 */
	private int createGLCategory (String Name, String CategoryType, boolean isDefault)
	{
		int GL_Category_ID = getNextID(m_client.getAD_Client_ID(), "GL_Category");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO GL_Category ");
		sqlCmd.append("(GL_Category_ID,").append(m_stdColumns).append(",")
			.append("Name,CategoryType,IsDefault) VALUES (")
			.append(GL_Category_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(Name).append("','").append(CategoryType).append("','")
			.append(isDefault ? "Y" : "N").append("')");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createGLCategory - GL Category NOT created - " + Name);
		//
		return GL_Category_ID;
	}   //  createGLCategory

	/**
	 *  Create Document Types with Sequence
	 *  @param Name name
	 *  @param PrintName print name
	 *  @param DocBaseType document base type
	 *  @param DocSubTypeSO sales order sub type
	 *  @param C_DocTypeShipment_ID shipment doc
	 *  @param C_DocTypeInvoice_ID invoice doc
	 *  @param StartNo start doc no
	 *  @param GL_Category_ID gl category
	 *  @return C_DocType_ID doc type
	 */
	private int createDocType (String Name, String PrintName,
		String DocBaseType, String DocSubTypeSO,
		int C_DocTypeShipment_ID, int C_DocTypeInvoice_ID,
		int StartNo, int GL_Category_ID)
	{
		MSequence sequence = null;
		if (StartNo != 0)
		{
			sequence = new MSequence(m_ctx, getAD_Client_ID(), Name, StartNo);
			if (!sequence.save())
				Log.error("MSetup.createDocType - Sequence NOT created - " + Name);
		}
		
		MDocType dt = new MDocType (m_ctx, DocBaseType, Name);
		dt.setPrintName(PrintName);
		if (DocSubTypeSO != null)
			dt.setDocSubTypeSO(DocSubTypeSO);
		if (C_DocTypeShipment_ID != 0)
			dt.setC_DocTypeShipment_ID(C_DocTypeShipment_ID);
		if (C_DocTypeInvoice_ID != 0)
			dt.setC_DocTypeInvoice_ID(C_DocTypeInvoice_ID);
		if (GL_Category_ID != 0)
			dt.setGL_Category_ID(GL_Category_ID);
		if (sequence == null)
			dt.setIsDocNoControlled(false);
		else
			dt.setDocNoSequence_ID(sequence.getAD_Sequence_ID());
		dt.setIsSOTrx();
		if (!dt.save())
			Log.error("MSetup.createDocType - DocType NOT created - " + Name);
		//
		return dt.getC_DocType_ID();
	}   //  createDocType

	
	/**************************************************************************
	 *  Create Default main entities.
	 *  - Dimensions & BPGroup, Prod Category)
	 *  - Location, Locator, Warehouse
	 *  - PriceList
	 *  - Cashbook, PaymentTerm
	 *  @param C_Country_ID country
	 *  @param City city
	 *  @param C_Region_ID region
	 *  @return true if created
	 */
	public boolean createEntities (int C_Country_ID, String City, int C_Region_ID, int C_Currency_ID)
	{
		Log.trace(Log.l3_Util, "MSetup.createEntries", "C_Country_ID=" + C_Country_ID + ", City=" + City + ", C_Region_ID=" + C_Region_ID);
		m_info.append("\n----\n");
		//
		String defaultName = Msg.translate(m_lang, "Standard");
		String defaultEntry = "'" + defaultName + "',";
		StringBuffer sqlCmd = null;
		int no = 0;

		//	Create Marketing Channel/Campaign
		int C_Channel_ID = getNextID(getAD_Client_ID(), "C_Channel");
		sqlCmd = new StringBuffer("INSERT INTO C_Channel ");
		sqlCmd.append("(C_Channel_ID,Name,");
		sqlCmd.append(m_stdColumns).append(") VALUES (");
		sqlCmd.append(C_Channel_ID).append(",").append(defaultEntry);
		sqlCmd.append(m_stdValues).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Channel NOT inserted");
		int C_Campaign_ID = getNextID(getAD_Client_ID(), "C_Campaign");
		sqlCmd = new StringBuffer("INSERT INTO C_Campaign ");
		sqlCmd.append("(C_Campaign_ID,C_Channel_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,Costs) VALUES (");
		sqlCmd.append(C_Campaign_ID).append(",").append(C_Channel_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Campaign_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Campaign NOT inserted");
		if (m_hasMCampaign)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Campaign_ID=").append(C_Campaign_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
			sqlCmd.append(" AND ElementType='MC'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Campaign NOT updated");
		}

		//	Create Sales Region
		int C_SalesRegion_ID = getNextID(getAD_Client_ID(), "C_SalesRegion");
		sqlCmd = new StringBuffer ("INSERT INTO C_SalesRegion ");
		sqlCmd.append("(C_SalesRegion_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsSummary) VALUES (");
		sqlCmd.append(C_SalesRegion_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_SalesRegion_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRegion NOT inserted");
		if (m_hasSRegion)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_SalesRegion_ID=").append(C_SalesRegion_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
			sqlCmd.append(" AND ElementType='SR'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement SalesRegion NOT updated");
		}

		/**
		 *  Business Partner
		 */
		//  Create BP Group
		int C_BP_Group_ID = getNextID(getAD_Client_ID(), "C_BP_Group");
		sqlCmd = new StringBuffer ("INSERT INTO C_BP_Group ");
		sqlCmd.append("(C_BP_Group_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault) VALUES ( ");
		sqlCmd.append(C_BP_Group_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BP_Group_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BP Group NOT inserted");

		//	Create BPartner
		int C_BPartner_ID = getNextID(getAD_Client_ID(), "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_BP_Group_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BPartner_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BPartner NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("C_BPartner_ID=").append(C_BPartner_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
		sqlCmd.append(" AND ElementType='BP'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element BPartner NOT updated");
		createPreference("C_BPartner_ID", String.valueOf(C_BPartner_ID), 143);

		/**
		 *  Product
		 */
		//  Create Product Category
		int M_Product_Category_ID = getNextID(getAD_Client_ID(), "M_Product_Category");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product_Category ");
		sqlCmd.append("(M_Product_Category_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault,PlannedMargin) VALUES (");
		sqlCmd.append(M_Product_Category_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y',0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_Category_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product Category NOT inserted");

		//  UOM (EA)
		int C_UOM_ID = 100;

		//  TaxCategory
		int C_TaxCategory_ID = getNextID(getAD_Client_ID(), "C_TaxCategory");
		sqlCmd = new StringBuffer ("INSERT INTO C_TaxCategory ");
		sqlCmd.append("(C_TaxCategory_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,IsDefault) VALUES (");
		sqlCmd.append(C_TaxCategory_ID).append(",").append(m_stdValues).append(", ");
		if (C_Country_ID == 100)    // US
			sqlCmd.append("'Sales Tax','Y')");
		else
			sqlCmd.append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - TaxCategory NOT inserted");

		//  Tax - Zero Rate
		MTax tax = new MTax (m_ctx, "Standard", Env.ZERO, C_TaxCategory_ID);
		tax.setIsDefault(true);
		if (tax.save())
			m_info.append(Msg.translate(m_lang, "C_Tax_ID"))
				.append("=").append(tax.getName()).append("\n");
		else
			Log.error("MSetup.createEntities - Tax NOT inserted");

		//	Create Product
		int M_Product_ID = getNextID(getAD_Client_ID(), "M_Product");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product ");
		sqlCmd.append("(M_Product_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_UOM_ID,M_Product_Category_ID,C_TaxCategory_ID) VALUES (");
		sqlCmd.append(M_Product_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry);
		sqlCmd.append(C_UOM_ID).append(",").append(M_Product_Category_ID).append(",");
		sqlCmd.append(C_TaxCategory_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("M_Product_ID=").append(M_Product_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
		sqlCmd.append(" AND ElementType='PR'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element Product NOT updated");

		/**
		 *  Warehouse
		 */
		//  Location (Company)
		int C_Location_ID = getNextID(getAD_Client_ID(), "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append(DB.TO_STRING(City)).append(",").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		sqlCmd = new StringBuffer ("UPDATE AD_OrgInfo SET C_Location_ID=");
		sqlCmd.append(C_Location_ID).append(" WHERE AD_Org_ID=").append(getAD_Org_ID());
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Location NOT inserted");
		createPreference("C_Country_ID", String.valueOf(C_Country_ID), 0);

		//  Default Warehouse
		int M_Warehouse_ID = getNextID(getAD_Client_ID(), "M_Warehouse");
		sqlCmd = new StringBuffer ("INSERT INTO M_Warehouse ");
		sqlCmd.append("(M_Warehouse_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Location_ID,Separator) VALUES (");
		sqlCmd.append(M_Warehouse_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Location_ID).append(",'-')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Warehouse NOT inserted");

		//   Locator
		int M_Locator_ID = getNextID(getAD_Client_ID(), "M_Locator");
		sqlCmd = new StringBuffer ("INSERT INTO M_Locator ");
		sqlCmd.append("(M_Locator_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,M_Warehouse_ID,X,Y,Z,PriorityNo,IsDefault) VALUES (");
		sqlCmd.append(M_Locator_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(M_Warehouse_ID).append(",0,0,0,50,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Locator NOT inserted");

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_BPartnerCashTrx_ID=").append(C_BPartner_ID);
		sqlCmd.append(",M_ProductFreight_ID=").append(M_Product_ID);
//		sqlCmd.append("C_UOM_Volume_ID=");
//		sqlCmd.append(",C_UOM_Weight_ID=");
//		sqlCmd.append(",C_UOM_Length_ID=");
//		sqlCmd.append(",C_UOM_Time_ID=");
		sqlCmd.append(" WHERE AD_Client_ID=").append(getAD_Client_ID());
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createEntities - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		/**
		 *  Other
		 */
		//  PriceList
		int M_PriceList_ID = getNextID(getAD_Client_ID(), "M_PriceList");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList ");
		sqlCmd.append("(M_PriceList_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID,IsDefault) VALUES (");
		sqlCmd.append(M_PriceList_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(",'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList NOT inserted");
		//  DiscountSchema
		int M_DiscountSchema_ID = getNextID(getAD_Client_ID(), "M_DiscountSchema");
		sqlCmd = new StringBuffer ("INSERT INTO M_DiscountSchema ");
		sqlCmd.append("(M_DiscountSchema_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,DiscountType, FlatDiscount) VALUES (");
		sqlCmd.append(M_DiscountSchema_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append("SysDate,'P',0)");	//	PriceList
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - DiscountSchema NOT inserted");
		//  PriceList Version
		int M_PriceList_Version_ID = getNextID(getAD_Client_ID(), "M_PriceList_Version");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList_Version ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,M_PriceList_ID,M_DiscountSchema_ID) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY-MM-DD'),SysDate,").append(M_PriceList_ID)
			.append(",").append(M_DiscountSchema_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList_Version NOT inserted");
		//  ProductPrice
		sqlCmd = new StringBuffer ("INSERT INTO M_ProductPrice ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("M_Product_ID,PriceList,PriceStd,PriceLimit) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(M_Product_ID).append(",1,1,1)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - ProductPrice NOT inserted");

		//  Location for Standard BP
		C_Location_ID = getNextID(getAD_Client_ID(), "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append(DB.TO_STRING(City)).append(",").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		int C_BPartner_Location_ID = getNextID(getAD_Client_ID(), "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(DB.TO_STRING(City)).append(",").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (Standard) NOT inserted");

		//	Create Sales Rep for Client-User
		C_BPartner_ID = getNextID(getAD_Client_ID(), "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_U_Name).append("','").append(AD_User_U_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_U_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (User) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_U_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (User) NOT updated");

		//  Location for Client-User
		C_Location_ID = getNextID(getAD_Client_ID(), "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append(DB.TO_STRING(City)).append(",").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		C_BPartner_Location_ID = getNextID(getAD_Client_ID(), "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(DB.TO_STRING(City)).append(",").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (User) NOT inserted");


		//	Create Sales Rep for Client-Admin
		C_BPartner_ID = getNextID(getAD_Client_ID(), "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_Name).append("','").append(AD_User_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (Admin) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (Admin) NOT updated");

		//  Location for Client-Admin
		C_Location_ID = getNextID(getAD_Client_ID(), "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append(DB.TO_STRING(City)).append(",").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		C_BPartner_Location_ID = getNextID(getAD_Client_ID(), "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(DB.TO_STRING(City)).append(",").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (Admin) NOT inserted");


		//  Payment Term
		int C_PaymentTerm_ID = getNextID(getAD_Client_ID(), "C_PaymentTerm");
		sqlCmd = new StringBuffer ("INSERT INTO C_PaymentTerm ");
		sqlCmd.append("(C_PaymentTerm_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,NetDays,GraceDays,DiscountDays,Discount,DiscountDays2,Discount2,IsDefault) VALUES (");
		sqlCmd.append(C_PaymentTerm_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'Immediate','Immediate',0,0,0,0,0,0,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PaymentTerm NOT inserted");

		//  Project Cycle
		C_Cycle_ID = getNextID(getAD_Client_ID(), "C_Cycle");
		sqlCmd = new StringBuffer ("INSERT INTO C_Cycle ");
		sqlCmd.append("(C_Cycle_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_Cycle_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Cycle NOT inserted");

		/**
		 *  Organization level data	===========================================
		 */

		//	Create Default Project
		int C_Project_ID = getNextID(getAD_Client_ID(), "C_Project");
		sqlCmd = new StringBuffer ("INSERT INTO C_Project ");
		sqlCmd.append("(C_Project_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Currency_ID,IsSummary) VALUES (");
		sqlCmd.append(C_Project_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Currency_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Project_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Project NOT inserted");
		//  Default Project
		if (m_hasProject)
		{
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Project_ID=").append(C_Project_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(m_as.getC_AcctSchema_ID());
			sqlCmd.append(" AND ElementType='PJ'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Project NOT updated");
		}

		//  CashBook
		int C_CashBook_ID = getNextID(getAD_Client_ID(), "C_CashBook");
		sqlCmd = new StringBuffer ("INSERT INTO C_CashBook ");
		sqlCmd.append("(C_CashBook_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_CashBook_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_CashBook_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - CashBook NOT inserted");

		return true;
	}   //  createEntities

	/**
	 *  Create Preference
	 *  @param Attribute attribute
	 *  @param Value value
	 *  @param AD_Window_ID window
	 */
	private void createPreference (String Attribute, String Value, int AD_Window_ID)
	{
		int AD_Preference_ID = getNextID(getAD_Client_ID(), "AD_Preference");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO AD_Preference ");
		sqlCmd.append("(AD_Preference_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Attribute,Value,AD_Window_ID) VALUES (");
		sqlCmd.append(AD_Preference_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(Attribute).append("','").append(Value).append("',");
		if (AD_Window_ID == 0)
			sqlCmd.append("NULL)");
		else
			sqlCmd.append(AD_Window_ID).append(")");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createPreference - Preference NOT inserted - " + Attribute);
	}   //  createPreference

	/**************************************************************************

	/**
	 * 	Get Next ID
	 * 	@param AD_Client_ID client
	 * 	@param TableName table name
	 */
	private int getNextID (int AD_Client_ID, String TableName)
	{
		return DB.getNextID(AD_Client_ID, TableName, null);
	}	//	getNextID

	/**
	 *  Get Client
	 *  @return AD_Client_ID
	 */
	public int getAD_Client_ID()
	{
		return m_client.getAD_Client_ID();
	}
	public int getAD_Org_ID()
	{
		return m_org.getAD_Org_ID();
	}
	public int getAD_User_ID()
	{
		return AD_User_ID;
	}
	public String getInfo()
	{
		return m_info.toString();
	}
}   //  MSetup
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
package org.compiere.model;

import java.util.*;
import java.sql.*;
import java.io.*;

import org.compiere.util.*;

/**
 * Initial Setup Model
 *
 * @author Jorg Janke
 * @version $Id: MSetup.java,v 1.27 2003/11/06 07:08:06 jjanke Exp $
 */
public final class MSetup
{
	/**
	 *  Constructor
	 *  @param ctx context
	 *  @param WindowNo window
	 */
	public MSetup(Properties ctx, int WindowNo)
	{
		m_ctx = ctx;
		m_lang = Env.getAD_Language(m_ctx);
		m_WindowNo = WindowNo;
	}   //  MSetup

	private Properties      m_ctx;
	private String          m_lang;
	private int             m_WindowNo;
	private StringBuffer    m_info;
	//
	private String          m_clientName;
	private String          m_orgName;
	//
	private String          m_stdColumns = "AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy";
	private String          m_stdValues;
	private String          m_stdValuesOrg;
	//
	private NaturalAccountMap m_nap = null;
	//
	private int             AD_Client_ID = 1000001;     //  initial Client_ID
	//
	private int     AD_Org_ID;
	private int     AD_User_ID;
	private String  AD_User_Name;
	private int     AD_User_U_ID;
	private String  AD_User_U_Name;
	private int     AD_Role_ID;
	private int     C_Calendar_ID;
	private int     C_AcctSchema_ID;
	private int     C_Currency_ID;
	private int     AD_Tree_Account_ID;
	private int     C_Cycle_ID;
	//
	private boolean         m_hasProject = false;
	private boolean         m_hasMCampaign = false;
	private boolean         m_hasSRegion = false;
	private AcctSchema      m_AcctSchema = null;


	/**
	 *  Create Client Info.
	 *  - Client, Trees, Org, Role, User, User_Role
	 *  @param clientName client name
	 *  @param orgName org name
	 *  @param userClient user id client
	 *  @param userOrg user id org
	 *  @return true if created
	 */
	public boolean createClient (String clientName, String orgName,
		String userClient, String userOrg)
	{
		Log.trace(Log.l3_Util, "MSetup.createClient");
		//  info header
		m_info = new StringBuffer();
		//  Standarc columns
		String name = null;
		String sql = null;
		int no = 0;

		/**
		 *  Create Client
		 */
		AD_Client_ID = DB.getKeyNextNo (AD_Client_ID, "AD_Client");
		Env.setContext(m_ctx, m_WindowNo, "AD_Client_ID", AD_Client_ID);

		//	Standard Values
		m_stdValues = String.valueOf(AD_Client_ID) + ",0,'Y',SysDate,0,SysDate,0";

		//	Create Client
		name = clientName;
		if (name == null || name.length() == 0)
			name = "newClient";
		name = name.trim();
		m_clientName = name;
		sql = "INSERT INTO AD_Client(" + m_stdColumns + ",Value,Name,Description)"
			+ " VALUES (" + m_stdValues + ",'" + name + "','" + name + "','" + name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Client NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info - Client
		m_info.append(Msg.translate(m_lang, "AD_Client_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Trees
		 */
		String sql2 = null;
		if (Env.isBaseLanguage(m_lang, "AD_Ref_List"))	//	Get TreeTypes & Name
			sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=120";
		else
			sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Reference_ID=120 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID";

		//  Tree IDs
		int AD_Tree_Org_ID=0, AD_Tree_BPartner_ID=0, AD_Tree_Project_ID=0,
			AD_Tree_SalesRegion_ID=0, AD_Tree_Product_ID=0;

		try
		{
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next())
			{
				String value = rs.getString(1);
				int AD_Tree_ID = 0;
				if (value.equals("OO"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Tree");
					AD_Tree_Org_ID = AD_Tree_ID;
				}
				else if (value.equals("BP"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Tree");
					AD_Tree_BPartner_ID = AD_Tree_ID;
				}
				else if (value.equals("PJ"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Tree");
					AD_Tree_Project_ID = AD_Tree_ID;
				}
				else if (value.equals("SR"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Tree");
					AD_Tree_SalesRegion_ID = AD_Tree_ID;
				}
				else if (value.equals("PR"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Tree");
					AD_Tree_Product_ID = AD_Tree_ID;
				}
				else if (value.endsWith("EV"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Tree");
					AD_Tree_Account_ID = AD_Tree_ID;
				}
				else if (value.equals("MM"))	//	No Menu
					;
				else
					//	PC (Product Category), BB (BOM), MC (Marketing Campaign), AY (Activity)
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Tree");

				//
				if (AD_Tree_ID != 0)
				{
					name = m_clientName + " " + rs.getString(2);
					sql = "INSERT INTO AD_Tree(" + m_stdColumns + ",AD_Tree_ID,Name,Description,TreeType)"
						+ " VALUES (" + m_stdValues + "," + AD_Tree_ID + ",'" + name + "','" + name + "','" + value + "')";
					no = DB.executeUpdate(sql);
					if (no == 1)
						m_info.append(Msg.translate(m_lang, "AD_Tree_ID")).append("=").append(name).append("\n");
					else
						Log.error("MSetup.createClient - Tree NOT created: " + name);
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e1)
		{
			Log.error ("MSetup.createClient - Trees", e1);
		}

		//	Get Primary Tree
		int AD_Tree_Menu_ID = 10;	//	hardcoded

		//	Create ClientInfo
		sql = "INSERT INTO AD_ClientInfo(" + m_stdColumns + ",Acct2_Active,Acct3_Active,"
			+ "AD_Tree_Menu_ID,AD_Tree_Org_ID,AD_Tree_BPartner_ID,AD_Tree_Project_ID,AD_Tree_SalesRegion_ID,AD_Tree_Product_ID)"
			+ " VALUES (" + m_stdValues + ",'N','N',"
			+ AD_Tree_Menu_ID + "," + AD_Tree_Org_ID + "," + AD_Tree_BPartner_ID + "," + AD_Tree_Project_ID + "," + AD_Tree_SalesRegion_ID + "," + AD_Tree_Product_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - ClientInfo NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
	//	m_info.append(Msg.translate(m_lang, "AD_ClientInfo")).append("\n");

		/**
		 *  Create Org
		 */
		AD_Org_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Org");
		m_stdValuesOrg = AD_Client_ID + "," + AD_Org_ID + ",'Y',SysDate,0,SysDate,0";

		name = orgName;
		if (name == null || name.length() == 0)
			name = "newOrg";
		m_orgName = name;
		sql = "INSERT INTO AD_Org (" + m_stdColumns + ",Value,Name,IsSummary)"
			+ " VALUES (" +	m_stdValuesOrg + ",'" + name + "','" + name + "','N')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Org NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_Org_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Roles
		 *  - Admin
		 *  - User
		 */
		name = m_clientName + " Admin";
		AD_Role_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Role");
		sql = "INSERT INTO AD_Role(" + m_stdColumns + ",AD_Role_ID,"
			+ "Name,Description,UserLevel,ClientList,OrgList)"
			+ " VALUES (" + m_stdValues + "," + AD_Role_ID + ","
			+ "'" + name + "','" + name + "',' CO','" + AD_Client_ID + "','0," + AD_Org_ID + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin Role A NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  OrgAccess x,0
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValues + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_OrgAccess 0 NOT created");
		//  OrgAccess x,y
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValuesOrg + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_OrgAccess NOT created");
		//  Info - Admin Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		//
		name = m_clientName + " User";
		int AD_Role_ID_U = DB.getKeyNextNo(AD_Client_ID, "AD_Role");
		sql = "INSERT INTO AD_Role(" + m_stdColumns + ",AD_Role_ID,"
			+ "Name,Description,UserLevel,ClientList,OrgList)"
			+ " VALUES (" + m_stdValues + "," + AD_Role_ID_U + ","
			+ "'" + name + "','" + name + "','  O','" + AD_Client_ID + "','" + AD_Org_ID + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - User Role A NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  OrgAccess x,y
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValuesOrg + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - User Role_OrgAccess NOT created");
		//  Info - Client Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Users
		 *  - Client
		 *  - Org
		 */
		name = userClient;
		if (name == null || name.length() == 0)
			name = m_clientName + "Client";
		AD_User_ID = DB.getKeyNextNo(AD_Client_ID, "AD_User");
		AD_User_Name = name;
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + ","
			+ "'" + AD_User_Name + "','" + AD_User_Name + "','" + AD_User_Name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin User NOT inserted - " + AD_User_Name;
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_Name).append("/").append(AD_User_Name).append("\n");

		name = userOrg;
		if (name == null || name.length() == 0)
			name = m_clientName + "Org";
		AD_User_U_ID = DB.getKeyNextNo(AD_Client_ID, "AD_User");
		AD_User_U_Name = name;
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + ","
			+ "'" + AD_User_U_Name + "','" + AD_User_U_Name + "','" + AD_User_U_Name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Org User NOT inserted - " + AD_User_U_Name;
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_U_Name).append("/").append(AD_User_U_Name).append("\n");

		/**
		 *  Create User-Role
		 */
		//  ClientUser          - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+Admin NOT inserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+User NOT inserted");
		//  OrgUser             - User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole OrgUser+Org NOT inserted");
		//  SuperUser(100)      - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + ",100," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole SuperUser+Admin NOT inserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + ",100," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole SuperUser+User NOT inserted");

		return true;
	}   //  createClient



	/*************************************************************************/

	/**
	 *  Create Accounting elements.
	 *  - Calendar
	 *  - Account Trees
	 *  - Account Values
	 *  - Accounting Schema
	 *  - Default Accounts
	 *
	 *  @param newC_Currency_ID currency
	 *  @param curName currency name
	 *  @param hasProduct has product segment
	 *  @param hasBPartner has bp segment
	 *  @param hasProject has project segment
	 *  @param hasMCampaign has campaign segment
	 *  @param hasSRegion has sales region segment
	 *  @param AccountingFile file name of accounting file
	 *  @return true if created
	 */
	public boolean createAccounting(int newC_Currency_ID, String curName,
		boolean hasProduct, boolean hasBPartner, boolean hasProject,
		boolean hasMCampaign, boolean hasSRegion,
		File AccountingFile)
	{
		Log.trace(Log.l3_Util, "MSetup.createAccount");
		//
		C_Currency_ID = newC_Currency_ID;
		m_hasProject = hasProject;
		m_hasMCampaign = hasMCampaign;
		m_hasSRegion = hasSRegion;

		//  Standard variables
		m_info = new StringBuffer();
		String name = null;
		StringBuffer sqlCmd = null;
		int no = 0;

		/**
		 *  Create Calendar
		 */
		C_Calendar_ID = DB.getKeyNextNo(AD_Client_ID, "C_Calendar");
		name = m_clientName + " " + Msg.translate(m_lang, "C_Calendar_ID");
		sqlCmd = new StringBuffer("INSERT INTO C_Calendar(");
		sqlCmd.append(m_stdColumns).append(",C_Calendar_ID,Name)").append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_Calendar_ID).append(",'").append(name).append("')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Calendar NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_Calendar_ID")).append("=").append(name).append("\n");

		//  Year
		int C_Year_ID = DB.getKeyNextNo(AD_Client_ID, "C_Year");
		sqlCmd = new StringBuffer ("INSERT INTO C_Year ");
		sqlCmd.append("(C_Year_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Year,C_Calendar_ID) VALUES (");
		sqlCmd.append(C_Year_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY'),").append(C_Calendar_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createAccounting - Year NOT inserted");
		/** @todo Create Periods */


		//	Create Account Elements
		int C_Element_ID = DB.getKeyNextNo(AD_Client_ID, "C_Element");
		name = m_clientName + " " + Msg.translate(m_lang, "Account_ID");
		sqlCmd = new StringBuffer ("INSERT INTO C_ELEMENT(");
		sqlCmd.append(m_stdColumns).append(",C_Element_ID,Name,Description,")
			.append("VFormat,ElementType,IsBalancing,IsNaturalAccount,AD_Tree_ID)").append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_Element_ID).append(",'").append(name).append("','").append(name).append("',")
			.append("NULL,'A','N','Y',").append(AD_Tree_Account_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Acct Element NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		m_info.append(Msg.translate(m_lang, "C_Element_ID")).append("=").append(name).append("\n");

		//	Create Account Values
		m_nap = new NaturalAccountMap(m_ctx);
		String errMsg = m_nap.parseFile(AccountingFile);
		if (errMsg.length() != 0)
		{
			Log.error(errMsg);
			m_info.append(errMsg);
			return false;
		}
		if (m_nap.createAccounts(AD_Client_ID, AD_Org_ID, C_Element_ID))
			m_info.append(Msg.translate(m_lang, "C_ElementValue_ID")).append(" # ").append(m_nap.size()).append("\n");
		else
		{
			String err = "MSetup.createAccounting - Acct Element Values NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		int C_ElementValue_ID = m_nap.getC_ElementValue_ID("DEFAULT_ACCT");
		Log.trace(Log.l4_Data, "C_ElementValue_ID=" + C_ElementValue_ID);

		/**
		 *  Create AccountingSchema
		 */
		C_AcctSchema_ID = DB.getKeyNextNo(AD_Client_ID, "C_AcctSchema");
		//
		String GAAP = "US";				//	AD_Reference_ID=123
		String CostingMethod = "A";		//	AD_Reference_ID=122
		name = m_clientName + " " + GAAP + "/" + CostingMethod + "/" + curName;
		//
		sqlCmd = new StringBuffer ("INSERT INTO C_AcctSchema(");
		sqlCmd.append(m_stdColumns).append(",C_AcctSchema_ID,Name,")
			.append("GAAP,IsAccrual,CostingMethod,C_Currency_ID,")
			.append("AutoPeriodControl,Separator,HasAlias,HasCombination)")
			.append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",'").append(name).append("',")
			.append("'").append(GAAP).append("','Y','").append(CostingMethod).append("',").append(C_Currency_ID).append(",")
			.append("'N','-','Y','N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - AcctSchema NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_AcctSchema_ID")).append("=").append(name).append("\n");

		/**
		 *  Create AccountingSchema Elements (Structure)
		 */
		String sql2 = null;
		if (Env.isBaseLanguage(m_lang, "AD_Reference"))	//	Get TreeTypes & Name
			sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=181";
		else
			sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Reference_ID=181 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID";
		//
		int Element_OO=0, Element_AC=0, Element_PR=0, Element_BP=0, Element_PJ=0,
			Element_MC=0, Element_SR=0;
		try
		{
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next())
			{
				String ElementType = rs.getString(1);
				name = rs.getString(2);
				//
				String IsMandatory = null;
				String IsBalanced = "N";
				int SeqNo = 0;
				int C_AcctSchema_Element_ID = 0;

				if (ElementType.equals("OO"))
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, "C_AcctSchema_Element");
					Element_OO = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					IsBalanced = "Y";
					SeqNo = 10;
				}
				else if (ElementType.equals("AC"))
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, "C_AcctSchema_Element");
					Element_AC = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					SeqNo = 20;
				}
				else if (ElementType.equals("PR") && hasProduct)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, "C_AcctSchema_Element");
					Element_PR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 30;
				}
				else if (ElementType.equals("BP") && hasBPartner)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, "C_AcctSchema_Element");
					Element_BP = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 40;
				}
				else if (ElementType.equals("PJ") && hasProject)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, "C_AcctSchema_Element");
					Element_PJ = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 50;
				}
				else if (ElementType.equals("MC") && hasMCampaign)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, "C_AcctSchema_Element");
					Element_MC = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 60;
				}
				else if (ElementType.equals("SR") && hasSRegion)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, "C_AcctSchema_Element");
					Element_SR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 70;
				}
				//	Not OT, LF, LT, U1, U2, AY

				if (IsMandatory != null)
				{
					sqlCmd = new StringBuffer ("INSERT INTO C_AcctSchema_Element(");
					sqlCmd.append(m_stdColumns).append(",C_AcctSchema_Element_ID,C_AcctSchema_ID,")
						.append("ElementType,Name,SeqNo,IsMandatory,IsBalanced) VALUES (");
					sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_Element_ID).append(",").append(C_AcctSchema_ID).append(",")
						.append("'").append(ElementType).append("','").append(name).append("',").append(SeqNo).append(",'")
						.append(IsMandatory).append("','").append(IsBalanced).append("')");
					no = DB.executeUpdate(sqlCmd.toString());
					if (no == 1)
						m_info.append(Msg.translate(m_lang, "C_AcctSchema_Element_ID")).append("=").append(name).append("\n");

					/** Default value for mandatory elements: OO and AC */
					if (ElementType.equals("OO"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET Org_ID=");
						sqlCmd.append(AD_Org_ID).append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Org in AcctSchamaElement NOT updated");
					}
					if (ElementType.equals("AC"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET C_ElementValue_ID=");
						sqlCmd.append(C_ElementValue_ID).append(", C_Element_ID=").append(C_Element_ID);
						sqlCmd.append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Account in AcctSchamaElement NOT updated");
					}
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e1)
		{
			Log.error ("MSetup.createAccounting - Elements", e1);
		}
		//  Create AcctSchema
		m_AcctSchema = new AcctSchema (C_AcctSchema_ID);


		//  Create GL Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_GL (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "USESUSPENSEBALANCING,SUSPENSEBALANCING_ACCT,"
			+ "USESUSPENSEERROR,SUSPENSEERROR_ACCT,"
			+ "USECURRENCYBALANCING,CURRENCYBALANCING_ACCT,"
			+ "RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,"
			+ "INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT,"
			+ "PPVOFFSET_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEBALANCING_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEERROR_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("CURRENCYBALANCING_ACCT")).append(",");
		//  RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,
		sqlCmd.append(getAcct("RETAINEDEARNING_ACCT")).append(",");
		sqlCmd.append(getAcct("INCOMESUMMARY_ACCT")).append(",");
		//  INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT)
		sqlCmd.append(getAcct("INTERCOMPANYDUETO_ACCT")).append(",");
		sqlCmd.append(getAcct("INTERCOMPANYDUEFROM_ACCT")).append(",");
		sqlCmd.append(getAcct("PPVOFFSET_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - GL Accounts NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//	Create Std Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_DEFAULT (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT, "
			+ "P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT, "
			+ "P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT, "
			+ "C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT, "
			+ "V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT, "
			+ "PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT, "
			+ "UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT, "
			+ "WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT, "
			+ "PJ_ASSET_ACCT,PJ_WIP_ACCT,"
			+ "T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT, "
			+ "B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,"
			+ "B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,"
			+ "B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT, "
			+ "CH_EXPENSE_ACCT,CH_REVENUE_ACCT, "
			+ "UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT, "
			+ "CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",");
		//  W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT
		sqlCmd.append(getAcct("W_INVENTORY_ACCT")).append(",");
		sqlCmd.append(getAcct("W_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("W_REVALUATION_ACCT")).append(",");
		sqlCmd.append(getAcct("W_INVACTUALADJUST_ACCT")).append(", ");
		//  P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT,
		sqlCmd.append(getAcct("P_REVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("P_COGS_ACCT")).append(", ");
		//  P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT,
		sqlCmd.append(getAcct("P_PURCHASEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_INVOICEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTREC_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTGRANT_ACCT")).append(", ");
		//  C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("C_RECEIVABLE_ACCT")).append(",");
		sqlCmd.append(getAcct("C_PREPAYMENT_ACCT")).append(", ");
		//  V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("V_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("V_LIABILITY_SERVICES_ACCT")).append(",");
		sqlCmd.append(getAcct("V_PREPAYMENT_ACCT")).append(", ");
		//  PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT,
		sqlCmd.append(getAcct("PAYDISCOUNT_EXP_ACCT")).append(",");
		sqlCmd.append(getAcct("PAYDISCOUNT_REV_ACCT")).append(",");
		sqlCmd.append(getAcct("WRITEOFF_ACCT")).append(", ");
		//  UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT,
		sqlCmd.append(getAcct("UNREALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("UNREALIZEDLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDLOSS_ACCT")).append(", ");
		//  WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT,
		sqlCmd.append(getAcct("WITHHOLDING_ACCT")).append(",");
		sqlCmd.append(getAcct("E_PREPAYMENT_ACCT")).append(",");
		sqlCmd.append(getAcct("E_EXPENSE_ACCT")).append(", ");
		//  PJ_ASSET_ACCT,PJ_WIP_ACCT,
		sqlCmd.append(getAcct("PJ_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("PJ_WIP_ACCT")).append(",");
		//  T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT,
		sqlCmd.append(getAcct("T_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("T_RECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("T_DUE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_CREDIT_ACCT")).append(", ");
		//  B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,
		sqlCmd.append(getAcct("B_INTRANSIT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("B_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTREV_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTEXP_ACCT")).append(",");
		//  B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,
		sqlCmd.append(getAcct("B_UNIDENTIFIED_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTLOSS_ACCT")).append(",");
		//  B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT,
		sqlCmd.append(getAcct("B_REVALUATIONGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_REVALUATIONLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("B_PAYMENTSELECT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_UNALLOCATEDCASH_ACCT")).append(", ");
		//  CH_EXPENSE_ACCT,CH_REVENUE_ACCT,
		sqlCmd.append(getAcct("CH_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CH_REVENUE_ACCT")).append(", ");
		//  UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT,
		sqlCmd.append(getAcct("UNEARNEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIPTS_ACCT")).append(", ");
		//  CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT)
		sqlCmd.append(getAcct("CB_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_CASHTRANSFER_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_RECEIPT_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Default Accounts NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//  GL Categories
		createGLCategory("Standard", "M", true);
		int GL_None = createGLCategory("None", "D", false);
		int GL_GL = createGLCategory("Manual", "M", false);
		int GL_ARI = createGLCategory("AR Invoice", "D", false);
		int GL_ARR = createGLCategory("AR Receipt", "D", false);
		int GL_MM = createGLCategory("Material Management", "D", false);
		int GL_API = createGLCategory("AP Invoice", "D", false);
		int GL_APP = createGLCategory("AP Payment", "D", false);

		//	Base DocumentTypes
		createDocType("GL Journal", "Journal", "GLJ", null, 0, 0, 1000, GL_GL);
		int DT_I = createDocType("AR Invoice", "Invoice", "ARI", null, 0, 0, 100000, GL_ARI);
		int DT_II = createDocType("AR Invoice Indirect", "Invoice", "ARI", null, 0, 0, 200000, GL_ARI);
		createDocType("AR Credit Memo", "Credit Memo", "ARC", null, 0, 0, 300000, GL_ARI);
		createDocType("AR Receipt", "Receipt", "ARR", null, 0, 0, 400000, GL_ARR);
		int DT_S  = createDocType("MM Shipment", "Delivery Note", "MMS", null, 0, 0, 500000, GL_MM);
		int DT_SI = createDocType("MM Shipment Indirect", "Delivery Note", "MMS", null, 0, 0, 600000, GL_MM);
		createDocType("MM Receipt", "Vendor Delivery", "MMR", null, 0, 0, 0, GL_MM);
		createDocType("AP Invoice", "Vendor Invoice", "API", null, 0, 0, 0, GL_API);
		createDocType("AP CreditMemo", "Vendor Credit Memo", "APC", null, 0, 0, 0, GL_API);
		createDocType("AP Payment", "Vendor Payment", "APP", null, 0, 0, 700000, GL_APP);
		createDocType("Purchase Order", "Purchase Order", "POO", null, 0, 0, 800000, GL_None);
		createDocType("Purchase Requisition", "Purchase Requisition", "POR", null, 0, 0, 900000, GL_None);

		//  Order Entry
		createDocType("Quotation", "Binding offer", "SOO", "OB", 0, 0, 10000, GL_None);
		createDocType("Proposal", "Non binding offer", "SOO", "ON", 0, 0, 20000, GL_None);
		createDocType("Prepay Order", "Prepay Order", "SOO", "PR", DT_S, DT_I, 30000, GL_None);
		createDocType("Return Material", "Return Material Authorization", "SOO", "RM", DT_S, DT_I, 40000, GL_None);
		createDocType("Standard Order", "Order Confirmation", "SOO", "SO", DT_S, DT_I, 50000, GL_None);
		createDocType("Credit Order", "Order Confirmation", "SOO", "WI", DT_SI, DT_I, 60000, GL_None);   //  RE
		createDocType("Warehouse Order", "Order Confirmation", "SOO", "WP", DT_S, DT_I, 70000, GL_None);    //  LS
		int DT = createDocType("POS Order", "Order Confirmation", "SOO", "WR", DT_SI, DT_II, 80000, GL_None);    // Bar
		createPreference("C_DocTypeTarget_ID", String.valueOf(DT), 143);

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_AcctSchema1_ID=").append(C_AcctSchema_ID)
			.append(", C_Calendar_ID=").append(C_Calendar_ID)
			.append(" WHERE AD_Client_ID=").append(AD_Client_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//
		return true;
	}   //  createAccounting

	/**
	 *  Get Account ID for key
	 *  @param key key
	 *  @return C_ValidCombination_ID
	 */
	private int getAcct (String key)
	{
		Log.trace(Log.l4_Data, "MSetup.getAcct - " + key);
		//  Element
		int C_ElementValue_ID = m_nap.getC_ElementValue_ID(key);
		Account vc = Account.getDefault(m_AcctSchema, true);	//	optional null
		vc.setAccount_ID(C_ElementValue_ID);
		vc.save(AD_Client_ID, 0);
		int C_ValidCombination_ID = vc.getC_ValidCombination_ID();

		//  Error
		if (C_ValidCombination_ID == 0)
			Log.error("MSetup.getAcct - no account for " + key);
		return C_ValidCombination_ID;
	}   //  getAcct

	/**
	 *  Create GL Category
	 *  @param Name name
	 *  @param CategoryType category type
	 *  @param isDefault is default value
	 *  @return GL_Category_ID
	 */
	private int createGLCategory (String Name, String CategoryType, boolean isDefault)
	{
		int GL_Category_ID = DB.getKeyNextNo(AD_Client_ID, "GL_Category");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO GL_Category ");
		sqlCmd.append("(GL_Category_ID,").append(m_stdColumns).append(",")
			.append("Name,CategoryType,IsDefault) VALUES (")
			.append(GL_Category_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(Name).append("','").append(CategoryType).append("','")
			.append(isDefault ? "Y" : "N").append("')");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createGLCategory - GL Category NOT created - " + Name);
		//
		return GL_Category_ID;
	}   //  createGLCategory

	/**
	 *  Create Document Types with Sequence
	 *  @param Name name
	 *  @param PrintName print name
	 *  @param DocBaseType document base type
	 *  @param DocSubTypeSO sales order sub type
	 *  @param C_DocTypeShipment_ID shipment doc
	 *  @param C_DocTypeInvoice_ID invoice doc
	 *  @param StartNo start doc no
	 *  @param GL_Category_ID gl category
	 *  @return C_DocType_ID doc type
	 */
	private int createDocType (String Name, String PrintName,
		String DocBaseType, String DocSubTypeSO,
		int C_DocTypeShipment_ID, int C_DocTypeInvoice_ID,
		int StartNo, int GL_Category_ID)
	{
		StringBuffer sqlCmd = null;
		//  Get Sequence
		int AD_Sequence_ID = 0;
		if (StartNo != 0)   //  manual sequenec, if startNo == 0
		{
			AD_Sequence_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Sequence");
			sqlCmd = new StringBuffer ("INSERT INTO AD_Sequence ");
			sqlCmd.append("(AD_Sequence_ID,").append(m_stdColumns).append(",");
			sqlCmd.append("Name,IsAutoSequence,IncrementNo,StartNo,CurrentNext,");
			sqlCmd.append("CurrentNextSys,IsTableID) VALUES (");
			sqlCmd.append(AD_Sequence_ID).append(",").append(m_stdValues).append(",");
			sqlCmd.append("'").append(Name).append("','Y',1,").append(StartNo).append(",").append(StartNo).append(",");
			sqlCmd.append(StartNo/10).append(",'N')");
			int no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createDocType - Sequence NOT created - " + Name);
		}

		//  Get Document Type
		int C_DocType_ID = DB.getKeyNextNo(AD_Client_ID, "C_DocType");
		sqlCmd = new StringBuffer ("INSERT INTO	C_DocType ");
		sqlCmd.append("(C_DocType_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,PrintName,DocBaseType,DocSubTypeSO,");
		sqlCmd.append("C_DocTypeShipment_ID,C_DocTypeInvoice_ID,");
		sqlCmd.append("IsDocNoControlled,DocNoSequence_ID,GL_Category_ID,");
		sqlCmd.append("IsPrinted,IsTransferred,DocumentCopies,IsSOTrx)");
		//
		sqlCmd.append(" VALUES (").append(C_DocType_ID).append(",").append(m_stdValues).append(",");
		//  Name,PrintName,DocBaseType,DocSubTypeSO,
		sqlCmd.append("'").append(Name).append("','").append(PrintName).append("','").append(DocBaseType).append("',");
		if (DocSubTypeSO == null || DocSubTypeSO.length() == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append("'").append(DocSubTypeSO).append("',");
		//  C_DocTypeShipment_ID,C_DocTypeInvoice_ID,
		if (C_DocTypeShipment_ID == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append(C_DocTypeShipment_ID).append(",");
		if (C_DocTypeInvoice_ID == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append(C_DocTypeInvoice_ID).append(",");
		//  IsDocNoControlled,DocNoSequence_ID,GL_Category_ID,
		if (AD_Sequence_ID == 0)
			sqlCmd.append("'N',NULL,");
		else
			sqlCmd.append("'Y',").append(AD_Sequence_ID).append(",");
		sqlCmd.append(GL_Category_ID).append(",");
		//  IsPrinted,IsTransferred,DocumentCopies,IsSOTrx)
		sqlCmd.append("'Y',");
		if (DocBaseType.equals("SOO"))
			sqlCmd.append("'N',");
		else
			sqlCmd.append("'Y',");
		sqlCmd.append("0,");
		if (DocBaseType.startsWith("AR") || DocBaseType.equals("MMS") || DocBaseType.equals("SOO"))
			sqlCmd.append("'Y'");
		else
			sqlCmd.append("'N'");
		sqlCmd.append(")");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createDocType - DocType NOT created - " + Name);
		//
		return C_DocType_ID;
	}   //  createDocType

	/*************************************************************************/

	/**
	 *  Create Default main entities.
	 *  - Dimensions & BPGroup, Prod Category)
	 *  - Location, Locator, Warehouse
	 *  - PriceList
	 *  - Cashbook, PaymentTerm
	 *  @param C_Country_ID country
	 *  @param City city
	 *  @param C_Region_ID region
	 *  @return true if created
	 */
	public boolean createEntities (int C_Country_ID, String City, int C_Region_ID)
	{
		Log.trace(Log.l3_Util, "MSetup.createEntries", "C_Country_ID=" + C_Country_ID + ", City=" + City + ", C_Region_ID=" + C_Region_ID);
		m_info.append("\n----\n");
		//
		String defaultName = Msg.translate(m_lang, "Standard");
		String defaultEntry = "'" + defaultName + "',";
		StringBuffer sqlCmd = null;
		int no = 0;

		//	Create Marketing Channel/Campaign
		int C_Channel_ID = DB.getKeyNextNo(AD_Client_ID, "C_Channel");
		sqlCmd = new StringBuffer("INSERT INTO C_Channel ");
		sqlCmd.append("(C_Channel_ID,Name,");
		sqlCmd.append(m_stdColumns).append(") VALUES (");
		sqlCmd.append(C_Channel_ID).append(",").append(defaultEntry);
		sqlCmd.append(m_stdValues).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Channel NOT inserted");
		int C_Campaign_ID = DB.getKeyNextNo(AD_Client_ID, "C_Campaign");
		sqlCmd = new StringBuffer("INSERT INTO C_Campaign ");
		sqlCmd.append("(C_Campaign_ID,C_Channel_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,Costs) VALUES (");
		sqlCmd.append(C_Campaign_ID).append(",").append(C_Channel_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Campaign_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Campaign NOT inserted");
		if (m_hasMCampaign)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Campaign_ID=").append(C_Campaign_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='MC'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Campaign NOT updated");
		}

		//	Create Sales Region
		int C_SalesRegion_ID = DB.getKeyNextNo(AD_Client_ID, "C_SalesRegion");
		sqlCmd = new StringBuffer ("INSERT INTO C_SalesRegion ");
		sqlCmd.append("(C_SalesRegion_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsSummary) VALUES (");
		sqlCmd.append(C_SalesRegion_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_SalesRegion_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRegion NOT inserted");
		if (m_hasSRegion)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_SalesRegion_ID=").append(C_SalesRegion_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='SR'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement SalesRegion NOT updated");
		}

		/**
		 *  Business Partner
		 */
		//  Create BP Group
		int C_BP_Group_ID = DB.getKeyNextNo(AD_Client_ID, "C_BP_Group");
		sqlCmd = new StringBuffer ("INSERT INTO C_BP_Group ");
		sqlCmd.append("(C_BP_Group_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault) VALUES ( ");
		sqlCmd.append(C_BP_Group_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BP_Group_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BP Group NOT inserted");

		//	Create BPartner
		int C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_BP_Group_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BPartner_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BPartner NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("C_BPartner_ID=").append(C_BPartner_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
		sqlCmd.append(" AND ElementType='BP'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element BPartner NOT updated");
		createPreference("C_BPartner_ID", String.valueOf(C_BPartner_ID), 143);

		/**
		 *  Product
		 */
		//  Create Product Category
		int M_Product_Category_ID = DB.getKeyNextNo(AD_Client_ID, "M_Product_Category");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product_Category ");
		sqlCmd.append("(M_Product_Category_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault,PlannedMargin) VALUES (");
		sqlCmd.append(M_Product_Category_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y',0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_Category_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product Category NOT inserted");

		//  UOM (EA)
		int C_UOM_ID = 100;

		//  TaxCategory
		int C_TaxCategory_ID = DB.getKeyNextNo(AD_Client_ID, "C_TaxCategory");
		sqlCmd = new StringBuffer ("INSERT INTO C_TaxCategory ");
		sqlCmd.append("(C_TaxCategory_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,IsDefault) VALUES (");
		sqlCmd.append(C_TaxCategory_ID).append(",").append(m_stdValues).append(", ");
		if (C_Country_ID == 100)    // US
			sqlCmd.append("'Sales Tax','Y')");
		else
			sqlCmd.append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - TaxCategory NOT inserted");

		//  Tax - Zero Rate
	//	MTax tax = new MTax (m_ctx, "Standard", Env.ZERO, C_TaxCategory_ID);
		int C_Tax_ID = DB.getKeyNextNo(AD_Client_ID, "C_Tax");
		sqlCmd = new StringBuffer ("INSERT INTO C_Tax ");
		sqlCmd.append("(C_Tax_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,Rate,C_TaxCategory_ID,C_Country_ID,TO_Country_ID,ValidFrom,SoPoType,IsDefault) VALUES (");
		sqlCmd.append(C_Tax_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append("0,").append(C_TaxCategory_ID).append(",")
			.append(C_Country_ID).append(",").append(C_Country_ID)
			.append(",TO_DATE('1990-01-01','YYYY-MM-DD'),'B','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Tax_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Tax NOT inserted");

		//	Create Product
		int M_Product_ID = DB.getKeyNextNo(AD_Client_ID, "M_Product");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product ");
		sqlCmd.append("(M_Product_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_UOM_ID,M_Product_Category_ID,C_TaxCategory_ID) VALUES (");
		sqlCmd.append(M_Product_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry);
		sqlCmd.append(C_UOM_ID).append(",").append(M_Product_Category_ID).append(",");
		sqlCmd.append(C_TaxCategory_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("M_Product_ID=").append(M_Product_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
		sqlCmd.append(" AND ElementType='PR'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element Product NOT updated");

		/**
		 *  Warehouse
		 */
		//  Location (Company)
		int C_Location_ID = DB.getKeyNextNo(AD_Client_ID, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		sqlCmd = new StringBuffer ("UPDATE AD_OrgInfo SET C_Location_ID=");
		sqlCmd.append(C_Location_ID).append(" WHERE AD_Org_ID=").append(AD_Org_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Location NOT inserted");
		createPreference("C_Country_ID", String.valueOf(C_Country_ID), 0);

		//  Default Warehouse
		int M_Warehouse_ID = DB.getKeyNextNo(AD_Client_ID, "M_Warehouse");
		sqlCmd = new StringBuffer ("INSERT INTO M_Warehouse ");
		sqlCmd.append("(M_Warehouse_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Location_ID,Separator) VALUES (");
		sqlCmd.append(M_Warehouse_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Location_ID).append(",'-')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Warehouse NOT inserted");

		//   Locator
		int M_Locator_ID = DB.getKeyNextNo(AD_Client_ID, "M_Locator");
		sqlCmd = new StringBuffer ("INSERT INTO M_Locator ");
		sqlCmd.append("(M_Locator_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,M_Warehouse_ID,X,Y,Z,PriorityNo,IsDefault) VALUES (");
		sqlCmd.append(M_Locator_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(M_Warehouse_ID).append(",0,0,0,50,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Locator NOT inserted");

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_BPartnerCashTrx_ID=").append(C_BPartner_ID);
		sqlCmd.append(",M_ProductFreight_ID=").append(M_Product_ID);
//		sqlCmd.append("C_UOM_Volume_ID=");
//		sqlCmd.append(",C_UOM_Weight_ID=");
//		sqlCmd.append(",C_UOM_Length_ID=");
//		sqlCmd.append(",C_UOM_Time_ID=");
		sqlCmd.append(" WHERE AD_Client_ID=").append(AD_Client_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createEntities - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		/**
		 *  Other
		 */
		//  PriceList
		int M_PriceList_ID = DB.getKeyNextNo(AD_Client_ID, "M_PriceList");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList ");
		sqlCmd.append("(M_PriceList_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID,IsDefault) VALUES (");
		sqlCmd.append(M_PriceList_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(",'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList NOT inserted");
		//  DiscountSchema
		int M_DiscountSchema_ID = DB.getKeyNextNo(AD_Client_ID, "M_DiscountSchema");
		sqlCmd = new StringBuffer ("INSERT INTO M_DiscountSchema ");
		sqlCmd.append("(M_DiscountSchema_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,DiscountType, FlatDiscount) VALUES (");
		sqlCmd.append(M_DiscountSchema_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append("SysDate,'P',0)");	//	PriceList
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - DiscountSchema NOT inserted");
		//  PriceList Version
		int M_PriceList_Version_ID = DB.getKeyNextNo(AD_Client_ID, "M_PriceList_Version");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList_Version ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,M_PriceList_ID,M_DiscountSchema_ID) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY-MM-DD'),SysDate,").append(M_PriceList_ID)
			.append(",").append(M_DiscountSchema_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList_Version NOT inserted");
		//  ProductPrice
		sqlCmd = new StringBuffer ("INSERT INTO M_ProductPrice ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("M_Product_ID,PriceList,PriceStd,PriceLimit) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(M_Product_ID).append(",1,1,1)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - ProductPrice NOT inserted");

		//  Location for Standard BP
		C_Location_ID = DB.getKeyNextNo(AD_Client_ID, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		int C_BPartner_Location_ID = DB.getKeyNextNo(AD_Client_ID, "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(City).append("',").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (Standard) NOT inserted");

		//	Create Sales Rep for Client-User
		C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_U_Name).append("','").append(AD_User_U_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_U_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (User) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_U_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (User) NOT updated");

		//  Location for Client-User
		C_Location_ID = DB.getKeyNextNo(AD_Client_ID, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		C_BPartner_Location_ID = DB.getKeyNextNo(AD_Client_ID, "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(City).append("',").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (User) NOT inserted");


		//	Create Sales Rep for Client-Admin
		C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_Name).append("','").append(AD_User_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (Admin) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (Admin) NOT updated");

		//  Location for Client-Admin
		C_Location_ID = DB.getKeyNextNo(AD_Client_ID, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		C_BPartner_Location_ID = DB.getKeyNextNo(AD_Client_ID, "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(City).append("',").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (Admin) NOT inserted");


		//  Payment Term
		int C_PaymentTerm_ID = DB.getKeyNextNo(AD_Client_ID, "C_PaymentTerm");
		sqlCmd = new StringBuffer ("INSERT INTO C_PaymentTerm ");
		sqlCmd.append("(C_PaymentTerm_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,NetDays,GraceDays,DiscountDays,Discount,DiscountDays2,Discount2,IsDefault) VALUES (");
		sqlCmd.append(C_PaymentTerm_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'Immediate','Immediate',0,0,0,0,0,0,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PaymentTerm NOT inserted");

		//  Project Cycle
		C_Cycle_ID = DB.getKeyNextNo(AD_Client_ID, "C_Cycle");
		sqlCmd = new StringBuffer ("INSERT INTO C_Cycle ");
		sqlCmd.append("(C_Cycle_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_Cycle_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Cycle NOT inserted");

		/**
		 *  Organization level data	===========================================
		 */

		//	Create Default Project
		int C_Project_ID = DB.getKeyNextNo(AD_Client_ID, "C_Project");
		sqlCmd = new StringBuffer ("INSERT INTO C_Project ");
		sqlCmd.append("(C_Project_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Currency_ID,IsSummary) VALUES (");
		sqlCmd.append(C_Project_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Currency_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Project_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Project NOT inserted");
		//  Default Project
		if (m_hasProject)
		{
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Project_ID=").append(C_Project_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='PJ'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Project NOT updated");
		}

		//  CashBook
		int C_CashBook_ID = DB.getKeyNextNo(AD_Client_ID, "C_CashBook");
		sqlCmd = new StringBuffer ("INSERT INTO C_CashBook ");
		sqlCmd.append("(C_CashBook_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_CashBook_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_CashBook_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - CashBook NOT inserted");

		return true;
	}   //  createEntities

	/**
	 *  Create Preference
	 *  @param Attribute attribute
	 *  @param Value value
	 *  @param AD_Window_ID window
	 */
	private void createPreference (String Attribute, String Value, int AD_Window_ID)
	{
		int AD_Preference_ID = DB.getKeyNextNo(AD_Client_ID, "AD_Preference");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO AD_Preference ");
		sqlCmd.append("(AD_Preference_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Attribute,Value,AD_Window_ID) VALUES (");
		sqlCmd.append(AD_Preference_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(Attribute).append("','").append(Value).append("',");
		if (AD_Window_ID == 0)
			sqlCmd.append("NULL)");
		else
			sqlCmd.append(AD_Window_ID).append(")");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createPreference - Preference NOT inserted - " + Attribute);
	}   //  createPreference

	/*************************************************************************/

	/**
	 *  Get Client
	 *  @return AD_Client_ID
	 */
	public int getAD_Client_ID()
	{
		return AD_Client_ID;
	}
	public int getAD_Org_ID()
	{
		return AD_Org_ID;
	}
	public int getAD_User_ID()
	{
		return AD_User_ID;
	}
	public int getAD_Role_ID()
	{
		return AD_Role_ID;
	}
	public int getC_AcctSchema_ID()
	{
		return C_AcctSchema_ID;
	}
	public String getInfo()
	{
		return m_info.toString();
	}
}   //  MSetup
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
package org.compiere.model;

import java.util.*;
import java.sql.*;
import java.io.*;

import org.compiere.util.*;

/**
 * Initial Setup Model
 *
 * @author Jorg Janke
 * @version $Id: MSetup.java,v 1.25 2003/07/22 18:49:02 jjanke Exp $
 */
public final class MSetup
{
	/**
	 *  Constructor
	 *  @param ctx context
	 *  @param WindowNo window
	 */
	public MSetup(Properties ctx, int WindowNo)
	{
		m_ctx = ctx;
		m_lang = Env.getAD_Language(m_ctx);
		m_WindowNo = WindowNo;
	}   //  MSetup

	private Properties      m_ctx;
	private String          m_lang;
	private int             m_WindowNo;
	private StringBuffer    m_info;
	//
	private String          m_clientName;
	private String          m_orgName;
	//
	private String          m_stdColumns = "AD_Client_ID,AD_Org_ID,IsActive,Created,CreatedBy,Updated,UpdatedBy";
	private String          m_stdValues;
	private String          m_stdValuesOrg;
	//
	private NaturalAccountMap m_nap = null;
	//
	private final String    CompiereSys = "N";           //  Should NOT be changed
	private int             AD_Client_ID = 1000000;     //  initial Client_ID
	//
	private int     AD_Org_ID;
	private int     AD_User_ID;
	private String  AD_User_Name;
	private int     AD_User_U_ID;
	private String  AD_User_U_Name;
	private int     AD_Role_ID;
	private int     C_Calendar_ID;
	private int     C_AcctSchema_ID;
	private int     C_Currency_ID;
	private int     AD_Tree_Account_ID;
	private int     C_Cycle_ID;
	//
	private boolean         m_hasProject = false;
	private boolean         m_hasMCampaign = false;
	private boolean         m_hasSRegion = false;
	private AcctSchema      m_AcctSchema = null;


	/**
	 *  Create Client Info.
	 *  - Client, Trees, Org, Role, User, User_Role
	 *  @param clientName client name
	 *  @param orgName org name
	 *  @param userClient user id client
	 *  @param userOrg user id org
	 *  @return true if created
	 */
	public boolean createClient (String clientName, String orgName,
		String userClient, String userOrg)
	{
		Log.trace(Log.l3_Util, "MSetup.createClient");
		//  info header
		m_info = new StringBuffer();
		//  Standarc columns
		String name = null;
		String sql = null;
		int no = 0;

		/**
		 *  Create Client
		 */
		Env.setContext(m_ctx, "#CompiereSys", CompiereSys);
		AD_Client_ID = DB.getKeyNextNo (AD_Client_ID, CompiereSys, "AD_Client");
		Env.setContext(m_ctx, m_WindowNo, "AD_Client_ID", AD_Client_ID);

		//	Standard Values
		m_stdValues = String.valueOf(AD_Client_ID) + ",0,'Y',SysDate,0,SysDate,0";

		//	Create Client
		name = clientName;
		if (name == null || name.length() == 0)
			name = "newClient";
		name = name.trim();
		m_clientName = name;
		sql = "INSERT INTO AD_Client(" + m_stdColumns + ",Value,Name,Description)"
			+ " VALUES (" + m_stdValues + ",'" + name + "','" + name + "','" + name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Client NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info - Client
		m_info.append(Msg.translate(m_lang, "AD_Client_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Trees
		 */
		String sql2 = null;
		if (Env.isBaseLanguage(m_lang, "AD_Ref_List"))	//	Get TreeTypes & Name
			sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=120";
		else
			sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Reference_ID=120 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID";

		//  Tree IDs
		int AD_Tree_Org_ID=0, AD_Tree_BPartner_ID=0, AD_Tree_Project_ID=0,
			AD_Tree_SalesRegion_ID=0, AD_Tree_Product_ID=0;

		try
		{
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next())
			{
				String value = rs.getString(1);
				int AD_Tree_ID = 0;
				if (value.equals("OO"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Org_ID = AD_Tree_ID;
				}
				else if (value.equals("BP"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_BPartner_ID = AD_Tree_ID;
				}
				else if (value.equals("PJ"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Project_ID = AD_Tree_ID;
				}
				else if (value.equals("SR"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_SalesRegion_ID = AD_Tree_ID;
				}
				else if (value.equals("PR"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Product_ID = AD_Tree_ID;
				}
				else if (value.endsWith("EV"))
				{
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");
					AD_Tree_Account_ID = AD_Tree_ID;
				}
				else if (value.equals("MM"))	//	No Menu
					;
				else
					//	PC (Product Category), BB (BOM), MC (Marketing Campaign), AY (Activity)
					AD_Tree_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Tree");

				//
				if (AD_Tree_ID != 0)
				{
					name = m_clientName + " " + rs.getString(2);
					sql = "INSERT INTO AD_Tree(" + m_stdColumns + ",AD_Tree_ID,Name,Description,TreeType)"
						+ " VALUES (" + m_stdValues + "," + AD_Tree_ID + ",'" + name + "','" + name + "','" + value + "')";
					no = DB.executeUpdate(sql);
					if (no == 1)
						m_info.append(Msg.translate(m_lang, "AD_Tree_ID")).append("=").append(name).append("\n");
					else
						Log.error("MSetup.createClient - Tree NOT created: " + name);
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e1)
		{
			Log.error ("MSetup.createClient - Trees", e1);
		}

		//	Get Primary Tree
		int AD_Tree_Menu_ID = 10;	//	hardcoded

		//	Create ClientInfo
		sql = "INSERT INTO AD_ClientInfo(" + m_stdColumns + ",Acct2_Active,Acct3_Active,"
			+ "AD_Tree_Menu_ID,AD_Tree_Org_ID,AD_Tree_BPartner_ID,AD_Tree_Project_ID,AD_Tree_SalesRegion_ID,AD_Tree_Product_ID)"
			+ " VALUES (" + m_stdValues + ",'N','N',"
			+ AD_Tree_Menu_ID + "," + AD_Tree_Org_ID + "," + AD_Tree_BPartner_ID + "," + AD_Tree_Project_ID + "," + AD_Tree_SalesRegion_ID + "," + AD_Tree_Product_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - ClientInfo NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
	//	m_info.append(Msg.translate(m_lang, "AD_ClientInfo")).append("\n");

		/**
		 *  Create Org
		 */
		AD_Org_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Org");
		m_stdValuesOrg = AD_Client_ID + "," + AD_Org_ID + ",'Y',SysDate,0,SysDate,0";

		name = orgName;
		if (name == null || name.length() == 0)
			name = "newOrg";
		m_orgName = name;
		sql = "INSERT INTO AD_Org (" + m_stdColumns + ",Value,Name,IsSummary)"
			+ " VALUES (" +	m_stdValuesOrg + ",'" + name + "','" + name + "','N')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Org NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_Org_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Roles
		 *  - Admin
		 *  - User
		 */
		name = m_clientName + " Admin";
		AD_Role_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Role");
		sql = "INSERT INTO AD_Role(" + m_stdColumns + ",AD_Role_ID,"
			+ "Name,Description,UserLevel,ClientList,OrgList)"
			+ " VALUES (" + m_stdValues + "," + AD_Role_ID + ","
			+ "'" + name + "','" + name + "',' CO','" + AD_Client_ID + "','0," + AD_Org_ID + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin Role A NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  OrgAccess x,0
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValues + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_OrgAccess 0 NOT created");
		//  OrgAccess x,y
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValuesOrg + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - Admin Role_OrgAccess NOT created");
		//  Info - Admin Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		//
		name = m_clientName + " User";
		int AD_Role_ID_U = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Role");
		sql = "INSERT INTO AD_Role(" + m_stdColumns + ",AD_Role_ID,"
			+ "Name,Description,UserLevel,ClientList,OrgList)"
			+ " VALUES (" + m_stdValues + "," + AD_Role_ID_U + ","
			+ "'" + name + "','" + name + "','  O','" + AD_Client_ID + "','" + AD_Org_ID + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - User Role A NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  OrgAccess x,y
		sql = "INSERT INTO AD_Role_OrgAccess(" + m_stdColumns + ",AD_Role_ID) VALUES ("
			+ m_stdValuesOrg + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - User Role_OrgAccess NOT created");
		//  Info - Client Role
		m_info.append(Msg.translate(m_lang, "AD_Role_ID")).append("=").append(name).append("\n");

		/**
		 *  Create Users
		 *  - Client
		 *  - Org
		 */
		name = userClient;
		if (name == null || name.length() == 0)
			name = m_clientName + "Client";
		AD_User_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_User");
		AD_User_Name = name;
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + ","
			+ "'" + AD_User_Name + "','" + AD_User_Name + "','" + AD_User_Name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Admin User NOT inserted - " + AD_User_Name;
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_Name).append("/").append(AD_User_Name).append("\n");

		name = userOrg;
		if (name == null || name.length() == 0)
			name = m_clientName + "Org";
		AD_User_U_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_User");
		AD_User_U_Name = name;
		sql = "INSERT INTO AD_User(" + m_stdColumns + ",AD_User_ID,"
			+ "Name,Description,Password)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + ","
			+ "'" + AD_User_U_Name + "','" + AD_User_U_Name + "','" + AD_User_U_Name + "')";
		no = DB.executeUpdate(sql);
		if (no != 1)
		{
			String err = "MSetup.createClient - Org User NOT inserted - " + AD_User_U_Name;
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "AD_User_ID")).append("=").append(AD_User_U_Name).append("/").append(AD_User_U_Name).append("\n");

		/**
		 *  Create User-Role
		 */
		//  ClientUser          - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+Admin NOT inserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_ID + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole ClientUser+User NOT inserted");
		//  OrgUser             - User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + "," + AD_User_U_ID + "," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole OrgUser+Org NOT inserted");
		//  SuperUser(100)      - Admin & User
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + ",100," + AD_Role_ID + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole SuperUser+Admin NOT inserted");
		sql = "INSERT INTO AD_User_Roles(" + m_stdColumns + ",AD_User_ID,AD_Role_ID)"
			+ " VALUES (" + m_stdValues + ",100," + AD_Role_ID_U + ")";
		no = DB.executeUpdate(sql);
		if (no != 1)
			Log.error("MSetup.createClient - UserRole SuperUser+User NOT inserted");

		return true;
	}   //  createClient



	/*************************************************************************/

	/**
	 *  Create Accounting elements.
	 *  - Calendar
	 *  - Account Trees
	 *  - Account Values
	 *  - Accounting Schema
	 *  - Default Accounts
	 *
	 *  @param newC_Currency_ID currency
	 *  @param curName currency name
	 *  @param hasProduct has product segment
	 *  @param hasBPartner has bp segment
	 *  @param hasProject has project segment
	 *  @param hasMCampaign has campaign segment
	 *  @param hasSRegion has sales region segment
	 *  @param AccountingFile file name of accounting file
	 *  @return true if created
	 */
	public boolean createAccounting(int newC_Currency_ID, String curName,
		boolean hasProduct, boolean hasBPartner, boolean hasProject,
		boolean hasMCampaign, boolean hasSRegion,
		File AccountingFile)
	{
		Log.trace(Log.l3_Util, "MSetup.createAccount");
		//
		C_Currency_ID = newC_Currency_ID;
		m_hasProject = hasProject;
		m_hasMCampaign = hasMCampaign;
		m_hasSRegion = hasSRegion;

		//  Standard variables
		m_info = new StringBuffer();
		String name = null;
		StringBuffer sqlCmd = null;
		int no = 0;

		/**
		 *  Create Calendar
		 */
		C_Calendar_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Calendar");
		name = m_clientName + " " + Msg.translate(m_lang, "C_Calendar_ID");
		sqlCmd = new StringBuffer("INSERT INTO C_Calendar(");
		sqlCmd.append(m_stdColumns).append(",C_Calendar_ID,Name)").append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_Calendar_ID).append(",'").append(name).append("')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Calendar NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_Calendar_ID")).append("=").append(name).append("\n");

		//  Year
		int C_Year_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Year");
		sqlCmd = new StringBuffer ("INSERT INTO C_Year ");
		sqlCmd.append("(C_Year_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Year,C_Calendar_ID) VALUES (");
		sqlCmd.append(C_Year_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY'),").append(C_Calendar_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createAccounting - Year NOT inserted");
		/** @todo Create Periods */


		//	Create Account Elements
		int C_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Element");
		name = m_clientName + " " + Msg.translate(m_lang, "Account_ID");
		sqlCmd = new StringBuffer ("INSERT INTO C_ELEMENT(");
		sqlCmd.append(m_stdColumns).append(",C_Element_ID,Name,Description,")
			.append("VFormat,ElementType,IsBalancing,IsNaturalAccount,AD_Tree_ID)").append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_Element_ID).append(",'").append(name).append("','").append(name).append("',")
			.append("NULL,'A','N','Y',").append(AD_Tree_Account_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Acct Element NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		m_info.append(Msg.translate(m_lang, "C_Element_ID")).append("=").append(name).append("\n");

		//	Create Account Values
		m_nap = new NaturalAccountMap(m_ctx);
		String errMsg = m_nap.parseFile(AccountingFile);
		if (errMsg.length() != 0)
		{
			Log.error(errMsg);
			m_info.append(errMsg);
			return false;
		}
		if (m_nap.createAccounts(AD_Client_ID, AD_Org_ID, C_Element_ID))
			m_info.append(Msg.translate(m_lang, "C_ElementValue_ID")).append(" # ").append(m_nap.size()).append("\n");
		else
		{
			String err = "MSetup.createAccounting - Acct Element Values NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		int C_ElementValue_ID = m_nap.getC_ElementValue_ID("DEFAULT_ACCT");
		Log.trace(Log.l4_Data, "C_ElementValue_ID=" + C_ElementValue_ID);

		/**
		 *  Create AccountingSchema
		 */
		C_AcctSchema_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema");
		//
		String GAAP = "US";				//	AD_Reference_ID=123
		String CostingMethod = "A";		//	AD_Reference_ID=122
		name = m_clientName + " " + GAAP + "/" + CostingMethod + "/" + curName;
		//
		sqlCmd = new StringBuffer ("INSERT INTO C_AcctSchema(");
		sqlCmd.append(m_stdColumns).append(",C_AcctSchema_ID,Name,")
			.append("GAAP,IsAccrual,CostingMethod,C_Currency_ID,")
			.append("AutoPeriodControl,Separator,HasAlias,HasCombination)")
			.append(" VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",'").append(name).append("',")
			.append("'").append(GAAP).append("','Y','").append(CostingMethod).append("',").append(C_Currency_ID).append(",")
			.append("'N','-','Y','N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - AcctSchema NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}
		//  Info
		m_info.append(Msg.translate(m_lang, "C_AcctSchema_ID")).append("=").append(name).append("\n");

		/**
		 *  Create AccountingSchema Elements (Structure)
		 */
		String sql2 = null;
		if (Env.isBaseLanguage(m_lang, "AD_Reference"))	//	Get TreeTypes & Name
			sql2 = "SELECT Value, Name FROM AD_Ref_List WHERE AD_Reference_ID=181";
		else
			sql2 = "SELECT l.Value, t.Name FROM AD_Ref_List l, AD_Ref_List_Trl t "
				+ "WHERE l.AD_Reference_ID=181 AND l.AD_Ref_List_ID=t.AD_Ref_List_ID";
		//
		int Element_OO=0, Element_AC=0, Element_PR=0, Element_BP=0, Element_PJ=0,
			Element_MC=0, Element_SR=0;
		try
		{
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql2);
			while (rs.next())
			{
				String ElementType = rs.getString(1);
				name = rs.getString(2);
				//
				String IsMandatory = null;
				String IsBalanced = "N";
				int SeqNo = 0;
				int C_AcctSchema_Element_ID = 0;

				if (ElementType.equals("OO"))
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_OO = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					IsBalanced = "Y";
					SeqNo = 10;
				}
				else if (ElementType.equals("AC"))
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_AC = C_AcctSchema_Element_ID;
					IsMandatory = "Y";
					SeqNo = 20;
				}
				else if (ElementType.equals("PR") && hasProduct)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_PR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 30;
				}
				else if (ElementType.equals("BP") && hasBPartner)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_BP = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 40;
				}
				else if (ElementType.equals("PJ") && hasProject)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_PJ = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 50;
				}
				else if (ElementType.equals("MC") && hasMCampaign)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_MC = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 60;
				}
				else if (ElementType.equals("SR") && hasSRegion)
				{
					C_AcctSchema_Element_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_AcctSchema_Element");
					Element_SR = C_AcctSchema_Element_ID;
					IsMandatory = "N";
					SeqNo = 70;
				}
				//	Not OT, LF, LT, U1, U2, AY

				if (IsMandatory != null)
				{
					sqlCmd = new StringBuffer ("INSERT INTO C_AcctSchema_Element(");
					sqlCmd.append(m_stdColumns).append(",C_AcctSchema_Element_ID,C_AcctSchema_ID,")
						.append("ElementType,Name,SeqNo,IsMandatory,IsBalanced) VALUES (");
					sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_Element_ID).append(",").append(C_AcctSchema_ID).append(",")
						.append("'").append(ElementType).append("','").append(name).append("',").append(SeqNo).append(",'")
						.append(IsMandatory).append("','").append(IsBalanced).append("')");
					no = DB.executeUpdate(sqlCmd.toString());
					if (no == 1)
						m_info.append(Msg.translate(m_lang, "C_AcctSchema_Element_ID")).append("=").append(name).append("\n");

					/** Default value for mandatory elements: OO and AC */
					if (ElementType.equals("OO"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET Org_ID=");
						sqlCmd.append(AD_Org_ID).append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Org in AcctSchamaElement NOT updated");
					}
					if (ElementType.equals("AC"))
					{
						sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET C_ElementValue_ID=");
						sqlCmd.append(C_ElementValue_ID).append(", C_Element_ID=").append(C_Element_ID);
						sqlCmd.append(" WHERE C_AcctSchema_Element_ID=").append(C_AcctSchema_Element_ID);
						no = DB.executeUpdate(sqlCmd.toString());
						if (no != 1)
							Log.error("MSetup.createAccounting - Default Account in AcctSchamaElement NOT updated");
					}
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e1)
		{
			Log.error ("MSetup.createAccounting - Elements", e1);
		}
		//  Create AcctSchema
		m_AcctSchema = new AcctSchema (C_AcctSchema_ID);


		//  Create GL Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_GL (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "USESUSPENSEBALANCING,SUSPENSEBALANCING_ACCT,"
			+ "USESUSPENSEERROR,SUSPENSEERROR_ACCT,"
			+ "USECURRENCYBALANCING,CURRENCYBALANCING_ACCT,"
			+ "RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,"
			+ "INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT,"
			+ "PPVOFFSET_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEBALANCING_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("SUSPENSEERROR_ACCT")).append(",");
		sqlCmd.append("'Y',").append(getAcct("CURRENCYBALANCING_ACCT")).append(",");
		//  RETAINEDEARNING_ACCT,INCOMESUMMARY_ACCT,
		sqlCmd.append(getAcct("RETAINEDEARNING_ACCT")).append(",");
		sqlCmd.append(getAcct("INCOMESUMMARY_ACCT")).append(",");
		//  INTERCOMPANYDUETO_ACCT,INTERCOMPANYDUEFROM_ACCT)
		sqlCmd.append(getAcct("INTERCOMPANYDUETO_ACCT")).append(",");
		sqlCmd.append(getAcct("INTERCOMPANYDUEFROM_ACCT")).append(",");
		sqlCmd.append(getAcct("PPVOFFSET_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - GL Accounts NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//	Create Std Accounts
		sqlCmd = new StringBuffer ("INSERT INTO C_ACCTSCHEMA_DEFAULT (");
		sqlCmd.append(m_stdColumns).append(",C_ACCTSCHEMA_ID,"
			+ "W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT, "
			+ "P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT, "
			+ "P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT, "
			+ "C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT, "
			+ "V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT, "
			+ "PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT, "
			+ "UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT, "
			+ "WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT, "
			+ "PJ_ASSET_ACCT,PJ_WIP_ACCT,"
			+ "T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT, "
			+ "B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,"
			+ "B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,"
			+ "B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT, "
			+ "CH_EXPENSE_ACCT,CH_REVENUE_ACCT, "
			+ "UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT, "
			+ "CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT) VALUES (");
		sqlCmd.append(m_stdValues).append(",").append(C_AcctSchema_ID).append(",");
		//  W_INVENTORY_ACCT,W_DIFFERENCES_ACCT,W_REVALUATION_ACCT,W_INVACTUALADJUST_ACCT
		sqlCmd.append(getAcct("W_INVENTORY_ACCT")).append(",");
		sqlCmd.append(getAcct("W_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("W_REVALUATION_ACCT")).append(",");
		sqlCmd.append(getAcct("W_INVACTUALADJUST_ACCT")).append(", ");
		//  P_REVENUE_ACCT,P_EXPENSE_ACCT,P_ASSET_ACCT,P_COGS_ACCT,
		sqlCmd.append(getAcct("P_REVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("P_COGS_ACCT")).append(", ");
		//  P_PURCHASEPRICEVARIANCE_ACCT,P_INVOICEPRICEVARIANCE_ACCT,P_TRADEDISCOUNTREC_ACCT,P_TRADEDISCOUNTGRANT_ACCT,
		sqlCmd.append(getAcct("P_PURCHASEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_INVOICEPRICEVARIANCE_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTREC_ACCT")).append(",");
		sqlCmd.append(getAcct("P_TRADEDISCOUNTGRANT_ACCT")).append(", ");
		//  C_RECEIVABLE_ACCT,C_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("C_RECEIVABLE_ACCT")).append(",");
		sqlCmd.append(getAcct("C_PREPAYMENT_ACCT")).append(", ");
		//  V_LIABILITY_ACCT,V_LIABILITY_SERVICES_ACCT,V_PREPAYMENT_ACCT,
		sqlCmd.append(getAcct("V_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("V_LIABILITY_SERVICES_ACCT")).append(",");
		sqlCmd.append(getAcct("V_PREPAYMENT_ACCT")).append(", ");
		//  PAYDISCOUNT_EXP_ACCT,PAYDISCOUNT_REV_ACCT,WRITEOFF_ACCT,
		sqlCmd.append(getAcct("PAYDISCOUNT_EXP_ACCT")).append(",");
		sqlCmd.append(getAcct("PAYDISCOUNT_REV_ACCT")).append(",");
		sqlCmd.append(getAcct("WRITEOFF_ACCT")).append(", ");
		//  UNREALIZEDGAIN_ACCT,UNREALIZEDLOSS_ACCT,REALIZEDGAIN_ACCT,REALIZEDLOSS_ACCT,
		sqlCmd.append(getAcct("UNREALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("UNREALIZEDLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("REALIZEDLOSS_ACCT")).append(", ");
		//  WITHHOLDING_ACCT,E_PREPAYMENT_ACCT,E_EXPENSE_ACCT,
		sqlCmd.append(getAcct("WITHHOLDING_ACCT")).append(",");
		sqlCmd.append(getAcct("E_PREPAYMENT_ACCT")).append(",");
		sqlCmd.append(getAcct("E_EXPENSE_ACCT")).append(", ");
		//  PJ_ASSET_ACCT,PJ_WIP_ACCT,
		sqlCmd.append(getAcct("PJ_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("PJ_WIP_ACCT")).append(",");
		//  T_EXPENSE_ACCT,T_LIABILITY_ACCT,T_RECEIVABLES_ACCT,T_DUE_ACCT,T_CREDIT_ACCT,
		sqlCmd.append(getAcct("T_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_LIABILITY_ACCT")).append(",");
		sqlCmd.append(getAcct("T_RECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("T_DUE_ACCT")).append(",");
		sqlCmd.append(getAcct("T_CREDIT_ACCT")).append(", ");
		//  B_INTRANSIT_ACCT,B_ASSET_ACCT,B_EXPENSE_ACCT,B_INTERESTREV_ACCT,B_INTERESTEXP_ACCT,
		sqlCmd.append(getAcct("B_INTRANSIT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("B_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTREV_ACCT")).append(",");
		sqlCmd.append(getAcct("B_INTERESTEXP_ACCT")).append(",");
		//  B_UNIDENTIFIED_ACCT,B_SETTLEMENTGAIN_ACCT,B_SETTLEMENTLOSS_ACCT,
		sqlCmd.append(getAcct("B_UNIDENTIFIED_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_SETTLEMENTLOSS_ACCT")).append(",");
		//  B_REVALUATIONGAIN_ACCT,B_REVALUATIONLOSS_ACCT,B_PAYMENTSELECT_ACCT,B_UNALLOCATEDCASH_ACCT,
		sqlCmd.append(getAcct("B_REVALUATIONGAIN_ACCT")).append(",");
		sqlCmd.append(getAcct("B_REVALUATIONLOSS_ACCT")).append(",");
		sqlCmd.append(getAcct("B_PAYMENTSELECT_ACCT")).append(",");
		sqlCmd.append(getAcct("B_UNALLOCATEDCASH_ACCT")).append(", ");
		//  CH_EXPENSE_ACCT,CH_REVENUE_ACCT,
		sqlCmd.append(getAcct("CH_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CH_REVENUE_ACCT")).append(", ");
		//  UNEARNEDREVENUE_ACCT,NOTINVOICEDRECEIVABLES_ACCT,NOTINVOICEDREVENUE_ACCT,NOTINVOICEDRECEIPTS_ACCT,
		sqlCmd.append(getAcct("UNEARNEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIVABLES_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDREVENUE_ACCT")).append(",");
		sqlCmd.append(getAcct("NOTINVOICEDRECEIPTS_ACCT")).append(", ");
		//  CB_ASSET_ACCT,CB_CASHTRANSFER_ACCT,CB_DIFFERENCES_ACCT,CB_EXPENSE_ACCT,CB_RECEIPT_ACCT)
		sqlCmd.append(getAcct("CB_ASSET_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_CASHTRANSFER_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_DIFFERENCES_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_EXPENSE_ACCT")).append(",");
		sqlCmd.append(getAcct("CB_RECEIPT_ACCT")).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - Default Accounts NOT inserted";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//  GL Categories
		createGLCategory("Standard", "M", true);
		int GL_None = createGLCategory("None", "D", false);
		int GL_GL = createGLCategory("Manual", "M", false);
		int GL_ARI = createGLCategory("AR Invoice", "D", false);
		int GL_ARR = createGLCategory("AR Receipt", "D", false);
		int GL_MM = createGLCategory("Material Management", "D", false);
		int GL_API = createGLCategory("AP Invoice", "D", false);
		int GL_APP = createGLCategory("AP Payment", "D", false);

		//	Base DocumentTypes
		createDocType("GL Journal", "Journal", "GLJ", null, 0, 0, 1000, GL_GL);
		int DT_I = createDocType("AR Invoice", "Invoice", "ARI", null, 0, 0, 100000, GL_ARI);
		int DT_II = createDocType("AR Invoice Indirect", "Invoice", "ARI", null, 0, 0, 200000, GL_ARI);
		createDocType("AR Credit Memo", "Credit Memo", "ARC", null, 0, 0, 300000, GL_ARI);
		createDocType("AR Receipt", "Receipt", "ARR", null, 0, 0, 400000, GL_ARR);
		int DT_S  = createDocType("MM Shipment", "Delivery Note", "MMS", null, 0, 0, 500000, GL_MM);
		int DT_SI = createDocType("MM Shipment Indirect", "Delivery Note", "MMS", null, 0, 0, 600000, GL_MM);
		createDocType("MM Receipt", "Vendor Delivery", "MMR", null, 0, 0, 0, GL_MM);
		createDocType("AP Invoice", "Vendor Invoice", "API", null, 0, 0, 0, GL_API);
		createDocType("AP CreditMemo", "Vendor Credit Memo", "APC", null, 0, 0, 0, GL_API);
		createDocType("AP Payment", "Vendor Payment", "APP", null, 0, 0, 700000, GL_APP);
		createDocType("Purchase Order", "Purchase Order", "POO", null, 0, 0, 800000, GL_None);
		createDocType("Purchase Requisition", "Purchase Requisition", "POR", null, 0, 0, 900000, GL_None);

		//  Order Entry
		createDocType("Quotation", "Binding offer", "SOO", "OB", 0, 0, 10000, GL_None);
		createDocType("Proposal", "Non binding offer", "SOO", "ON", 0, 0, 20000, GL_None);
		createDocType("Prepay Order", "Prepay Order", "SOO", "PR", DT_S, DT_I, 30000, GL_None);
		createDocType("Return Material", "Return Material Authorization", "SOO", "RM", DT_S, DT_I, 40000, GL_None);
		createDocType("Standard Order", "Order Confirmation", "SOO", "SO", DT_S, DT_I, 50000, GL_None);
		createDocType("Credit Order", "Order Confirmation", "SOO", "WI", DT_SI, DT_I, 60000, GL_None);   //  RE
		createDocType("Warehouse Order", "Order Confirmation", "SOO", "WP", DT_S, DT_I, 70000, GL_None);    //  LS
		int DT = createDocType("POS Order", "Order Confirmation", "SOO", "WR", DT_SI, DT_II, 80000, GL_None);    // Bar
		createPreference("C_DocTypeTarget_ID", String.valueOf(DT), 143);

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_AcctSchema1_ID=").append(C_AcctSchema_ID)
			.append(", C_Calendar_ID=").append(C_Calendar_ID)
			.append(" WHERE AD_Client_ID=").append(AD_Client_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createAccounting - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		//
		return true;
	}   //  createAccounting

	/**
	 *  Get Account ID for key
	 *  @param key key
	 *  @return C_ValidCombination_ID
	 */
	private int getAcct (String key)
	{
		Log.trace(Log.l4_Data, "MSetup.getAcct - " + key);
		//  Element
		int C_ElementValue_ID = m_nap.getC_ElementValue_ID(key);
		Account vc = Account.getDefault(m_AcctSchema, true);	//	optional null
		vc.setAccount_ID(C_ElementValue_ID);
		vc.save(AD_Client_ID, 0);
		int C_ValidCombination_ID = vc.getC_ValidCombination_ID();

		//  Error
		if (C_ValidCombination_ID == 0)
			Log.error("MSetup.getAcct - no account for " + key);
		return C_ValidCombination_ID;
	}   //  getAcct

	/**
	 *  Create GL Category
	 *  @param Name name
	 *  @param CategoryType category type
	 *  @param isDefault is default value
	 *  @return GL_Category_ID
	 */
	private int createGLCategory (String Name, String CategoryType, boolean isDefault)
	{
		int GL_Category_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "GL_Category");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO GL_Category ");
		sqlCmd.append("(GL_Category_ID,").append(m_stdColumns).append(",")
			.append("Name,CategoryType,IsDefault) VALUES (")
			.append(GL_Category_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(Name).append("','").append(CategoryType).append("','")
			.append(isDefault ? "Y" : "N").append("')");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createGLCategory - GL Category NOT created - " + Name);
		//
		return GL_Category_ID;
	}   //  createGLCategory

	/**
	 *  Create Document Types with Sequence
	 *  @param Name name
	 *  @param PrintName print name
	 *  @param DocBaseType document base type
	 *  @param DocSubTypeSO sales order sub type
	 *  @param C_DocTypeShipment_ID shipment doc
	 *  @param C_DocTypeInvoice_ID invoice doc
	 *  @param StartNo start doc no
	 *  @param GL_Category_ID gl category
	 *  @return C_DocType_ID doc type
	 */
	private int createDocType (String Name, String PrintName,
		String DocBaseType, String DocSubTypeSO,
		int C_DocTypeShipment_ID, int C_DocTypeInvoice_ID,
		int StartNo, int GL_Category_ID)
	{
		StringBuffer sqlCmd = null;
		//  Get Sequence
		int AD_Sequence_ID = 0;
		if (StartNo != 0)   //  manual sequenec, if startNo == 0
		{
			AD_Sequence_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Sequence");
			sqlCmd = new StringBuffer ("INSERT INTO AD_Sequence ");
			sqlCmd.append("(AD_Sequence_ID,").append(m_stdColumns).append(",");
			sqlCmd.append("Name,IsAutoSequence,IncrementNo,StartNo,CurrentNext,");
			sqlCmd.append("CurrentNextSys,IsTableID) VALUES (");
			sqlCmd.append(AD_Sequence_ID).append(",").append(m_stdValues).append(",");
			sqlCmd.append("'").append(Name).append("','Y',1,").append(StartNo).append(",").append(StartNo).append(",");
			sqlCmd.append(StartNo/10).append(",'N')");
			int no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createDocType - Sequence NOT created - " + Name);
		}

		//  Get Document Type
		int C_DocType_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_DocType");
		sqlCmd = new StringBuffer ("INSERT INTO	C_DocType ");
		sqlCmd.append("(C_DocType_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,PrintName,DocBaseType,DocSubTypeSO,");
		sqlCmd.append("C_DocTypeShipment_ID,C_DocTypeInvoice_ID,");
		sqlCmd.append("IsDocNoControlled,DocNoSequence_ID,GL_Category_ID,");
		sqlCmd.append("IsPrinted,IsTransferred,DocumentCopies,IsSOTrx)");
		//
		sqlCmd.append(" VALUES (").append(C_DocType_ID).append(",").append(m_stdValues).append(",");
		//  Name,PrintName,DocBaseType,DocSubTypeSO,
		sqlCmd.append("'").append(Name).append("','").append(PrintName).append("','").append(DocBaseType).append("',");
		if (DocSubTypeSO == null || DocSubTypeSO.length() == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append("'").append(DocSubTypeSO).append("',");
		//  C_DocTypeShipment_ID,C_DocTypeInvoice_ID,
		if (C_DocTypeShipment_ID == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append(C_DocTypeShipment_ID).append(",");
		if (C_DocTypeInvoice_ID == 0)
			sqlCmd.append("NULL,");
		else
			sqlCmd.append(C_DocTypeInvoice_ID).append(",");
		//  IsDocNoControlled,DocNoSequence_ID,GL_Category_ID,
		if (AD_Sequence_ID == 0)
			sqlCmd.append("'N',NULL,");
		else
			sqlCmd.append("'Y',").append(AD_Sequence_ID).append(",");
		sqlCmd.append(GL_Category_ID).append(",");
		//  IsPrinted,IsTransferred,DocumentCopies,IsSOTrx)
		sqlCmd.append("'Y',");
		if (DocBaseType.equals("SOO"))
			sqlCmd.append("'N',");
		else
			sqlCmd.append("'Y',");
		sqlCmd.append("0,");
		if (DocBaseType.startsWith("AR") || DocBaseType.equals("MMS") || DocBaseType.equals("SOO"))
			sqlCmd.append("'Y'");
		else
			sqlCmd.append("'N'");
		sqlCmd.append(")");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createDocType - DocType NOT created - " + Name);
		//
		return C_DocType_ID;
	}   //  createDocType

	/*************************************************************************/

	/**
	 *  Create Default main entities.
	 *  - Dimensions & BPGroup, Prod Category)
	 *  - Location, Locator, Warehouse
	 *  - PriceList
	 *  - Cashbook, PaymentTerm
	 *  @param C_Country_ID country
	 *  @param City city
	 *  @param C_Region_ID region
	 *  @return true if created
	 */
	public boolean createEntities (int C_Country_ID, String City, int C_Region_ID)
	{
		Log.trace(Log.l3_Util, "MSetup.createEntries", "C_Country_ID=" + C_Country_ID + ", City=" + City + ", C_Region_ID=" + C_Region_ID);
		m_info.append("\n----\n");
		//
		String defaultName = Msg.translate(m_lang, "Standard");
		String defaultEntry = "'" + defaultName + "',";
		StringBuffer sqlCmd = null;
		int no = 0;

		//	Create Marketing Channel/Campaign
		int C_Channel_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Channel");
		sqlCmd = new StringBuffer("INSERT INTO C_Channel ");
		sqlCmd.append("(C_Channel_ID,Name,");
		sqlCmd.append(m_stdColumns).append(") VALUES (");
		sqlCmd.append(C_Channel_ID).append(",").append(defaultEntry);
		sqlCmd.append(m_stdValues).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Channel NOT inserted");
		int C_Campaign_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Campaign");
		sqlCmd = new StringBuffer("INSERT INTO C_Campaign ");
		sqlCmd.append("(C_Campaign_ID,C_Channel_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,Costs) VALUES (");
		sqlCmd.append(C_Campaign_ID).append(",").append(C_Channel_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Campaign_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Campaign NOT inserted");
		if (m_hasMCampaign)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Campaign_ID=").append(C_Campaign_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='MC'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Campaign NOT updated");
		}

		//	Create Sales Region
		int C_SalesRegion_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_SalesRegion");
		sqlCmd = new StringBuffer ("INSERT INTO C_SalesRegion ");
		sqlCmd.append("(C_SalesRegion_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsSummary) VALUES (");
		sqlCmd.append(C_SalesRegion_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_SalesRegion_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRegion NOT inserted");
		if (m_hasSRegion)
		{
			//  Default
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_SalesRegion_ID=").append(C_SalesRegion_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='SR'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement SalesRegion NOT updated");
		}

		/**
		 *  Business Partner
		 */
		//  Create BP Group
		int C_BP_Group_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BP_Group");
		sqlCmd = new StringBuffer ("INSERT INTO C_BP_Group ");
		sqlCmd.append("(C_BP_Group_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault) VALUES ( ");
		sqlCmd.append(C_BP_Group_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BP_Group_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BP Group NOT inserted");

		//	Create BPartner
		int C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_BP_Group_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_BPartner_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - BPartner NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("C_BPartner_ID=").append(C_BPartner_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
		sqlCmd.append(" AND ElementType='BP'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element BPartner NOT updated");
		createPreference("C_BPartner_ID", String.valueOf(C_BPartner_ID), 143);

		/**
		 *  Product
		 */
		//  Create Product Category
		int M_Product_Category_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Product_Category");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product_Category ");
		sqlCmd.append("(M_Product_Category_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,IsDefault,PlannedMargin) VALUES (");
		sqlCmd.append(M_Product_Category_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append("'Y',0)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_Category_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product Category NOT inserted");

		//  UOM (EA)
		int C_UOM_ID = 100;

		//  TaxCategory
		int C_TaxCategory_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_TaxCategory");
		sqlCmd = new StringBuffer ("INSERT INTO C_TaxCategory ");
		sqlCmd.append("(C_TaxCategory_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,IsDefault) VALUES (");
		sqlCmd.append(C_TaxCategory_ID).append(",").append(m_stdValues).append(", ");
		if (C_Country_ID == 100)    // US
			sqlCmd.append("'Sales Tax','Y')");
		else
			sqlCmd.append(defaultEntry).append("'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - TaxCategory NOT inserted");

		//  Tax - Zero Rate
		int C_Tax_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Tax");
		sqlCmd = new StringBuffer ("INSERT INTO C_Tax ");
		sqlCmd.append("(C_Tax_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,Rate,C_TaxCategory_ID,C_Country_ID,TO_Country_ID,ValidFrom,IsDefault) VALUES (");
		sqlCmd.append(C_Tax_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append("0,").append(C_TaxCategory_ID).append(",")
			.append(C_Country_ID).append(",").append(C_Country_ID)
			.append(",TO_DATE('1990-01-01','YYYY-MM-DD'),'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Tax_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Tax NOT inserted");

		//	Create Product
		int M_Product_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Product");
		sqlCmd = new StringBuffer ("INSERT INTO M_Product ");
		sqlCmd.append("(M_Product_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_UOM_ID,M_Product_Category_ID,C_TaxCategory_ID) VALUES (");
		sqlCmd.append(M_Product_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry);
		sqlCmd.append(C_UOM_ID).append(",").append(M_Product_Category_ID).append(",");
		sqlCmd.append(C_TaxCategory_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "M_Product_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Product NOT inserted");
		//  Default
		sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
		sqlCmd.append("M_Product_ID=").append(M_Product_ID);
		sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
		sqlCmd.append(" AND ElementType='PR'");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - AcctSchema Element Product NOT updated");

		/**
		 *  Warehouse
		 */
		//  Location (Company)
		int C_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		sqlCmd = new StringBuffer ("UPDATE AD_OrgInfo SET C_Location_ID=");
		sqlCmd.append(C_Location_ID).append(" WHERE AD_Org_ID=").append(AD_Org_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Location NOT inserted");
		createPreference("C_Country_ID", String.valueOf(C_Country_ID), 0);

		//  Default Warehouse
		int M_Warehouse_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Warehouse");
		sqlCmd = new StringBuffer ("INSERT INTO M_Warehouse ");
		sqlCmd.append("(M_Warehouse_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Location_ID,Separator) VALUES (");
		sqlCmd.append(M_Warehouse_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Location_ID).append(",'-')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Warehouse NOT inserted");

		//   Locator
		int M_Locator_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_Locator");
		sqlCmd = new StringBuffer ("INSERT INTO M_Locator ");
		sqlCmd.append("(M_Locator_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,M_Warehouse_ID,X,Y,Z,PriorityNo,IsDefault) VALUES (");
		sqlCmd.append(M_Locator_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(M_Warehouse_ID).append(",0,0,0,50,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Locator NOT inserted");

		//  Update ClientInfo
		sqlCmd = new StringBuffer ("UPDATE AD_ClientInfo SET ");
		sqlCmd.append("C_BPartnerCashTrx_ID=").append(C_BPartner_ID);
		sqlCmd.append(",M_ProductFreight_ID=").append(M_Product_ID);
//		sqlCmd.append("C_UOM_Volume_ID=");
//		sqlCmd.append(",C_UOM_Weight_ID=");
//		sqlCmd.append(",C_UOM_Length_ID=");
//		sqlCmd.append(",C_UOM_Time_ID=");
		sqlCmd.append(" WHERE AD_Client_ID=").append(AD_Client_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
		{
			String err = "MSetup.createEntities - ClientInfo not updated";
			Log.error(err);
			m_info.append(err);
			return false;
		}

		/**
		 *  Other
		 */
		//  PriceList
		int M_PriceList_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_PriceList");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList ");
		sqlCmd.append("(M_PriceList_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID,IsDefault) VALUES (");
		sqlCmd.append(M_PriceList_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(",'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList NOT inserted");
		//  DiscountSchema
		int M_DiscountSchema_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_DiscountSchema");
		sqlCmd = new StringBuffer ("INSERT INTO M_DiscountSchema ");
		sqlCmd.append("(M_DiscountSchema_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,DiscountType, FlatDiscount) VALUES (");
		sqlCmd.append(M_DiscountSchema_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append("SysDate,'P',0)");	//	PriceList
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - DiscountSchema NOT inserted");
		//  PriceList Version
		int M_PriceList_Version_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "M_PriceList_Version");
		sqlCmd = new StringBuffer ("INSERT INTO M_PriceList_Version ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,ValidFrom,M_PriceList_ID,M_DiscountSchema_ID) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("TO_CHAR(SysDate,'YYYY-MM-DD'),SysDate,").append(M_PriceList_ID)
			.append(",").append(M_DiscountSchema_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PriceList_Version NOT inserted");
		//  ProductPrice
		sqlCmd = new StringBuffer ("INSERT INTO M_ProductPrice ");
		sqlCmd.append("(M_PriceList_Version_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("M_Product_ID,PriceList,PriceStd,PriceLimit) VALUES (");
		sqlCmd.append(M_PriceList_Version_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append(M_Product_ID).append(",1,1,1)");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - ProductPrice NOT inserted");

		//  Location for Standard BP
		C_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		int C_BPartner_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(City).append("',").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (Standard) NOT inserted");

		//	Create Sales Rep for Client-User
		C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_U_Name).append("','").append(AD_User_U_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_U_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (User) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_U_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (User) NOT updated");

		//  Location for Client-User
		C_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		C_BPartner_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(City).append("',").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (User) NOT inserted");


		//	Create Sales Rep for Client-Admin
		C_BPartner_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner ");
		sqlCmd.append("(C_BPartner_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,C_BP_Group_ID,IsSummary,IsEmployee,IsSalesRep,IsVendor) VALUES (");
		sqlCmd.append(C_BPartner_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(AD_User_Name).append("','").append(AD_User_Name).append("',");
		sqlCmd.append(C_BP_Group_ID).append(",'N','Y','Y','Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "SalesRep_ID")).append("=").append(AD_User_Name).append("\n");
		else
			Log.error("MSetup.createEntities - SalesRep (Admin) NOT inserted");
		//  Update User
		sqlCmd = new StringBuffer ("UPDATE AD_User SET C_BPartner_ID=");
		sqlCmd.append(C_BPartner_ID).append(" WHERE AD_User_ID=").append(AD_User_ID);
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - User of SalesRep (Admin) NOT updated");

		//  Location for Client-Admin
		C_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_Location ")
			.append("(C_Location_ID,").append(m_stdColumns).append(",")
			.append("City,C_Country_ID,C_Region_ID) VALUES (");
		sqlCmd.append(C_Location_ID).append(",").append(m_stdValues).append(",")
			.append("'").append(City).append("',").append(C_Country_ID).append(",");
		if (C_Region_ID != 0)
			sqlCmd.append(C_Region_ID).append(")");
		else
			sqlCmd.append("null)");
		DB.executeUpdate(sqlCmd.toString());
		C_BPartner_Location_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_BPartner_Location");
		sqlCmd = new StringBuffer ("INSERT INTO C_BPartner_Location ");
		sqlCmd.append("(C_BPartner_Location_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Name,C_BPartner_ID,C_Location_ID) VALUES (");
		sqlCmd.append(C_BPartner_Location_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(City).append("',").append(C_BPartner_ID).append(",").append(C_Location_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - BP_Location (Admin) NOT inserted");


		//  Payment Term
		int C_PaymentTerm_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_PaymentTerm");
		sqlCmd = new StringBuffer ("INSERT INTO C_PaymentTerm ");
		sqlCmd.append("(C_PaymentTerm_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Value,Name,NetDays,GraceDays,DiscountDays,Discount,DiscountDays2,Discount2,IsDefault) VALUES (");
		sqlCmd.append(C_PaymentTerm_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'Immediate','Immediate',0,0,0,0,0,0,'Y')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - PaymentTerm NOT inserted");

		//  Project Cycle
		C_Cycle_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Cycle");
		sqlCmd = new StringBuffer ("INSERT INTO C_Cycle ");
		sqlCmd.append("(C_Cycle_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_Cycle_ID).append(",").append(m_stdValues).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createEntities - Cycle NOT inserted");

		/**
		 *  Organization level data	===========================================
		 */

		//	Create Default Project
		int C_Project_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_Project");
		sqlCmd = new StringBuffer ("INSERT INTO C_Project ");
		sqlCmd.append("(C_Project_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Value,Name,C_Currency_ID,IsSummary) VALUES (");
		sqlCmd.append(C_Project_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(defaultEntry).append(C_Currency_ID).append(",'N')");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_Project_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - Project NOT inserted");
		//  Default Project
		if (m_hasProject)
		{
			sqlCmd = new StringBuffer ("UPDATE C_AcctSchema_Element SET ");
			sqlCmd.append("C_Project_ID=").append(C_Project_ID);
			sqlCmd.append(" WHERE C_AcctSchema_ID=").append(C_AcctSchema_ID);
			sqlCmd.append(" AND ElementType='PJ'");
			no = DB.executeUpdate(sqlCmd.toString());
			if (no != 1)
				Log.error("MSetup.createEntities - AcctSchema ELement Project NOT updated");
		}

		//  CashBook
		int C_CashBook_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "C_CashBook");
		sqlCmd = new StringBuffer ("INSERT INTO C_CashBook ");
		sqlCmd.append("(C_CashBook_ID,").append(m_stdColumns).append(",");
		sqlCmd.append(" Name,C_Currency_ID) VALUES (");
		sqlCmd.append(C_CashBook_ID).append(",").append(m_stdValuesOrg).append(", ");
		sqlCmd.append(defaultEntry).append(C_Currency_ID).append(")");
		no = DB.executeUpdate(sqlCmd.toString());
		if (no == 1)
			m_info.append(Msg.translate(m_lang, "C_CashBook_ID")).append("=").append(defaultName).append("\n");
		else
			Log.error("MSetup.createEntities - CashBook NOT inserted");

		return true;
	}   //  createEntities

	/**
	 *  Create Preference
	 *  @param Attribute attribute
	 *  @param Value value
	 *  @param AD_Window_ID window
	 */
	private void createPreference (String Attribute, String Value, int AD_Window_ID)
	{
		int AD_Preference_ID = DB.getKeyNextNo(AD_Client_ID, CompiereSys, "AD_Preference");
		StringBuffer sqlCmd = new StringBuffer ("INSERT INTO AD_Preference ");
		sqlCmd.append("(AD_Preference_ID,").append(m_stdColumns).append(",");
		sqlCmd.append("Attribute,Value,AD_Window_ID) VALUES (");
		sqlCmd.append(AD_Preference_ID).append(",").append(m_stdValues).append(",");
		sqlCmd.append("'").append(Attribute).append("','").append(Value).append("',");
		if (AD_Window_ID == 0)
			sqlCmd.append("NULL)");
		else
			sqlCmd.append(AD_Window_ID).append(")");
		int no = DB.executeUpdate(sqlCmd.toString());
		if (no != 1)
			Log.error("MSetup.createPreference - Preference NOT inserted - " + Attribute);
	}   //  createPreference

	/*************************************************************************/

	/**
	 *  Get Client
	 *  @return AD_Client_ID
	 */
	public int getAD_Client_ID()
	{
		return AD_Client_ID;
	}
	public int getAD_Org_ID()
	{
		return AD_Org_ID;
	}
	public int getAD_User_ID()
	{
		return AD_User_ID;
	}
	public int getAD_Role_ID()
	{
		return AD_Role_ID;
	}
	public int getC_AcctSchema_ID()
	{
		return C_AcctSchema_ID;
	}
	public String getInfo()
	{
		return m_info.toString();
	}
}   //  MSetup
