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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;

import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Web Menu
 *
 *  @author Jorg Janke
 *  @version  $Id: WMenu.java,v 1.2 2003/03/31 05:27:26 jjanke Exp $
 */
public class WMenu extends HttpServlet
{
	/**
	 *  Initialize global variables
	 *  @param config config
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("WMenu.init");
	}   //  init

	/**
	 * 	Get Servlet information
	 *  @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Menu";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		Log.trace(Log.l1_User, "WMenu.destroy");
		super.destroy();
	}   //  destroy

	/** */
	private PreparedStatement	m_pstmt;


	/*************************************************************************/

	/**
	 *  Process the HTTP Get request.
	 *  - Exit (Logout)
	 *  - AD_Window_ID Forward to Window
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WMenu.doGet - Process Menu Request");
		//  Get Parameter: Exit
		if (request.getParameter("Exit") != null)
		{
			WUtil.createLoginPage (request, response, this, null, "Exit");
			return;
		}

		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		if (ctx == null)
		{
			WUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Window
		int AD_Window_ID = WUtil.getParameterAsInt(request, "AD_Window_ID");

		//  Forward to WWindow
		if (AD_Window_ID != 0)
		{
			Log.trace(Log.l4_Data, "AD_Window_ID=" + AD_Window_ID);
			//
			String url = WEnv.getBaseDirectory("WWindow?AD_Window_ID=" + AD_Window_ID);
			Log.trace(Log.l4_Data, "Forward to - " + url);
			//
			RequestDispatcher rd = getServletContext().getRequestDispatcher(url);
			rd.forward(request, response);
			return;
		}

		//  Request not serviceable
		WUtil.createErrorPage(request, response, this, null, "NotImplemented");
	}   //  doGet


	/*************************************************************************/

	/**
	 *  Process the HTTP Post request - Verify Input & Create Menu
	 *
	 * @param request request
	 * @param response response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WMenu.doPost - Create Menu");
		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		Properties cProp = WUtil.getCookieProprties(request);
		if (ctx == null)
		{
			WUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Get Parameters: Role, Client, Org, Warehouse, Date
		String role = request.getParameter(WLogin.P_ROLE);
		String client = request.getParameter(WLogin.P_CLIENT);
		String org = request.getParameter(WLogin.P_ORG);
		String wh = request.getParameter(WLogin.P_WAREHOUSE);
		if (wh == null)
			wh = "";

		//  Get context
		if (role == null || client == null || org == null)
		{
			WUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Get Info from Context - User, Role, Client
		int AD_User_ID = Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID = Env.getContextAsInt(ctx, "#AD_Role_ID");
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		//  Not available in context yet - Org, Warehouse
		int AD_Org_ID = -1;
		int M_Warehouse_ID = -1;

		//  Get latest info from context
		try
		{
			int req_role = Integer.parseInt(role);
			if (req_role != AD_Role_ID)
			{
				Log.trace(Log.l5_DData, "AD_Role_ID - changed from " + AD_Role_ID);
				AD_Role_ID = req_role;
				Env.setContext(ctx, "#AD_Role_ID", AD_Role_ID);
			}
			Log.trace(Log.l5_DData, "AD_Role_ID = " + AD_Role_ID);
			//
			int req_client = Integer.parseInt(client);
			if (req_client != AD_Client_ID)
			{
				Log.trace(Log.l5_DData, "AD_Client_ID - changed from " + AD_Client_ID);
				AD_Client_ID = req_client;
				Env.setContext(ctx, "#AD_Client_ID", AD_Client_ID);
			}
			Log.trace(Log.l5_DData, "AD_Client_ID = " + AD_Client_ID);
			//
			AD_Org_ID = Integer.parseInt(org);
			Log.trace(Log.l5_DData, "AD_Org_ID = " + AD_Org_ID);
			//
			if (wh.length() > 0)
			{
				M_Warehouse_ID = Integer.parseInt(wh);
				Log.trace(Log.l5_DData, "M_Warehouse_ID = " + M_Warehouse_ID);
			}
		}
		catch (Exception e)
		{
			Log.error("WMenu.doPost - Parameter", e);
			WUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Check Login info and set environment
		String loginInfo = checkLogin (ctx, AD_User_ID, AD_Role_ID, AD_Client_ID, AD_Org_ID, M_Warehouse_ID);
		if (loginInfo == null)
		{
			WUtil.createErrorPage(request, response, this, ctx, Msg.getMsg(ctx, "RoleInconsistent"));
			return;
		}
		sess.setAttribute(WEnv.SA_LOGININFO, loginInfo);

		//  Set cookie for future defaults
		cProp.setProperty(WLogin.P_ROLE, String.valueOf(AD_Role_ID));
		cProp.setProperty(WLogin.P_CLIENT, String.valueOf(AD_Client_ID));
		cProp.setProperty(WLogin.P_ORG, String.valueOf(AD_Org_ID));
		if (M_Warehouse_ID == -1)
			cProp.setProperty(WLogin.P_WAREHOUSE, "");
		else
			cProp.setProperty(WLogin.P_WAREHOUSE, String.valueOf(M_Warehouse_ID));

		//  Set Date
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		//  Try to parse Date
		String dateString = request.getParameter(WLogin.P_DATE);
		try
		{
			if (dateString != null && dateString.length() > 0)
			{
				Language language = (Language)sess.getAttribute(WEnv.SA_LANGUAGE);
				DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
				java.util.Date date = df.parse(dateString);
				ts = new Timestamp(date.getTime());
			}
		}
		catch (Exception e)
		{
			Log.trace(Log.l1_User, "WMenu.doPost - Cannot parse date: " + dateString + " - " + e.getMessage());
		}
		Env.setContext(ctx, "#Date", ts.toString());    //  JDBC format


	//	Log.printProperties(System.getProperties(), "System");
	//	Log.printProperties(cProp, "Cookie");
	//	Log.printProperties(ctx, "Servlet Context");
	//	Log.printProperties(Env.getCtx(), "Apps Env Context");


		/**********************************************************************
		 *  Create Menu output
		 */
		String windowTitle = Msg.getMsg(ctx, "Menu");
		String statusMessage = Msg.getMsg(ctx, "SelectMenuItem");

		//	Document
		WDoc doc = WDoc.create (windowTitle);
		head header = doc.getHead();
		//  Target
		header.addElement(new base().setTarget(WEnv.TARGET_WINDOW));
		//  Specific Menu Script/Stylesheet
		header.addElement(new script("", WEnv.getBaseDirectory("menu.js"), "text/javascript", "JavaScript1.2"));
		header.addElement(new link().setRel("stylesheet").setHref(WEnv.getBaseDirectory("menu.css")));

		//	Body
		body body = doc.getBody();
		//  Header
		body.addElement(new cite(loginInfo));
		String title = windowTitle + " - " + loginInfo;
		body.addElement(new script("top.document.title='" + title + "';"));
		body.addElement(new script("defaultStatus = '" + statusMessage + "';"));

		//  Clear Window Frame
		body.addElement(WUtil.getClearFrame(WEnv.TARGET_WINDOW));

		//  Load Menu Structure     ----------------------
		MNode root = loadTree(AD_Client_ID, AD_Role_ID, AD_User_ID, Env.getAD_Language(ctx));
		//
		StringBuffer buf = new StringBuffer();
		buf.append("<ul>");
		for (int i = 0; i < root.children.size(); i++)
		{
			MNode node = (MNode)root.children.get(i);
			node.print(node, buf);
		}
		buf.append("</ul>");
		body.addElement(buf.toString());

		//  Exit Info
		body.addElement(new hr());
		String url = request.getRequestURI() + "?Exit=true";
		body.addElement(new a(url, Msg.getMsg(ctx, "Exit")));

		//  Can we store Cookie ?
		if (!cProp.getProperty(WLogin.P_STORE, "N").equals("Y"))
			cProp.clear();

		WUtil.createResponse (request, response, this, cProp, doc, false);
	}   //  doPost

	/**
	 *  Check Login information and set context.
	 *  @returns    true if login info are OK
	 *  @param ctx context
	 *  @param AD_User_ID user
	 *  @param AD_Role_ID role
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param M_Warehouse_ID warehouse
	 */
	private String checkLogin (Properties ctx, int AD_User_ID, int AD_Role_ID, int AD_Client_ID, int AD_Org_ID, int M_Warehouse_ID)
	{
		//  Get Login Info
		String loginInfo = null;
		//  Verify existance of User/Client/Org/Role and User's acces to Client & Org
		String sql = "SELECT u.Name || '@' || c.Name || '.' || o.Name || ' [' || INITCAP(USER) || ']' AS Text "
			+ "FROM AD_User u, AD_Client c, AD_Org o, AD_User_Roles ur "
			+ "WHERE u.AD_User_ID=?"    //  #1
			+ " AND c.AD_Client_ID=?"   //  #2
			+ " AND o.AD_Org_ID=?"      //  #3
			+ " AND ur.AD_Role_ID=?"    //  #4
			+ " AND ur.AD_User_ID=u.AD_User_ID"
			+ " AND (o.AD_Client_ID = 0 OR o.AD_Client_ID=c.AD_Client_ID)"
			+ " AND c.AD_Client_ID IN (SELECT AD_Client_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)"
			+ " AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			pstmt.setInt(2, AD_Client_ID);
			pstmt.setInt(3, AD_Org_ID);
			pstmt.setInt(4, AD_Role_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				loginInfo = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("WMenu.checkLogin", e);
		}

		//  not verified
		if (loginInfo == null)
			return null;

		//  Set Preferences
		KeyNamePair org = new KeyNamePair(AD_Org_ID, String.valueOf(AD_Org_ID));
		KeyNamePair wh = null;
		if (M_Warehouse_ID > 0)
			wh = new KeyNamePair(M_Warehouse_ID, String.valueOf(M_Warehouse_ID));
		//
		Timestamp date = null;
		String printer = null;
		DB.loadPreferences(ctx, org, wh, date, printer);
		//
		return loginInfo;
	}   //  checkLogin


	/*************************************************************************/

	/**
	 *  Load Tree and return root node
	 *  @param AD_Client_ID client
	 *  @param AD_Role_ID role
	 *  @param AD_User_ID user
	 *  @param AD_Language language
	 *  @return node
	 */
	private MNode loadTree (int AD_Client_ID, int AD_Role_ID, int AD_User_ID, String AD_Language)
	{
		//	Get Tree info with start node
		int     AD_Tree_ID;
		String	Name;
		String	TreeType;
		String	Description;
		int 	startID;
		//
		MNode   root = null;

		//  Get Root Node
		String SQL = "SELECT t.AD_Tree_ID, t.Name, t.Description, t.TreeType, tn.Node_ID "
			+ "FROM AD_Tree t, AD_ClientInfo c, AD_TreeNode tn "
			+ "WHERE t.AD_Tree_ID=tn.AD_Tree_ID"
			+ " AND tn.Parent_ID IS NULL"
			+ " AND t.AD_Tree_ID=c.AD_Tree_Menu_ID"
			+ " AND c.AD_Client_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(SQL);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				return null;
			}
			AD_Tree_ID = rs.getInt(1);
			Name = rs.getString(2);
			Description = rs.getString(3);
			TreeType = rs.getString(4);
			startID = rs.getInt(5);

			//	create root node
			root = new MNode (startID, Name, Description, true, "");
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadTree -1", e);
			return null;
		}

		//  SQL for TreeNodes
		StringBuffer cmd = new StringBuffer("SELECT tn.Node_ID,tn.Parent_ID,tn.SeqNo, "
			+ "(SELECT 'Y' FROM AD_TreeBar tb WHERE tb.AD_Tree_ID=tn.AD_Tree_ID AND tb.AD_User_ID=")
			.append(AD_User_ID).append(" AND tb.Node_ID=tn.Node_ID) "
			+ "FROM AD_TreeNode tn ")
			.append("WHERE tn.IsActive='Y' "
			+ "START WITH Parent_ID IS NULL AND AD_Tree_ID=? "
			+ "CONNECT BY Parent_ID=PRIOR Node_ID AND AD_Tree_ID=? "
			+ "ORDER BY LEVEL, SeqNo");

		//  SQL for Node Info
		StringBuffer cmdNode = new StringBuffer();
		boolean base = Env.isBaseLanguage(Env.getCtx(), "AD_Menu");
		if (base)
			cmdNode.append("SELECT m.Name,m.Description,m.IsSummary,m.Action "
				+ "FROM AD_Menu m");
		else
			cmdNode.append("SELECT t.Name,t.Description,m.IsSummary,m.Action "
				+ "FROM AD_Menu m, AD_Menu_Trl t");
			cmdNode.append(", (SELECT ").append(AD_Role_ID).append(" AS XRole FROM DUAL)");
		cmdNode.append(" WHERE m.AD_Menu_ID=?");
		if (!base)
			cmdNode.append(" AND m.AD_Menu_ID=t.AD_Menu_ID AND t.AD_Language='")
				.append(Env.getAD_Language(Env.getCtx())).append("'");

		cmdNode.append(" AND m.IsActive='Y' "
			+ "AND (m.IsSummary='Y' OR m.Action='B'"
			+ " OR EXISTS (SELECT * FROM AD_Window_Access wa WHERE wa.AD_Window_ID=m.AD_Window_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Process_Access wa WHERE wa.AD_Process_ID=m.AD_Process_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Form_Access wa WHERE wa.AD_Form_ID=m.AD_Form_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Task_Access wa WHERE wa.AD_Task_ID=m.AD_Task_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Workflow_Access wa WHERE wa.AD_Workflow_ID=m.AD_Workflow_ID AND wa.AD_Role_ID=XRole)"
			+ ")");
	//	Log.trace(Log.l6_Database, "SQL Tree", cmd.toString());
	//	Log.trace(Log.l6_Database, "SQL Node", cmdNode.toString());

		//  The Node Loop
		try
		{
			PreparedStatement pstmtNode = DB.prepareStatement(cmdNode.toString());
			//
			PreparedStatement pstmt = DB.prepareStatement(cmd.toString());
			pstmt.setInt(1, AD_Tree_ID);
			pstmt.setInt(2, AD_Tree_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int Node_ID = rs.getInt(1);
				int Parent_ID = rs.getInt(2);
				int SeqNo = rs.getInt(3);
				boolean onBar = (rs.getString(4) != null);
				loadNode (pstmtNode, root, Node_ID, Parent_ID, SeqNo, onBar);
			}
			rs.close();
			pstmt.close();
			pstmtNode.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadTree -2", e);
		}

		//  Clean Menu tree
		root.clean(root);

		return root;
	}   //  loadTree

	/**
	 *  Load Node using prepared statement
	 *  @param  pstmt       Prepared Statement requiring to set Node_ID and returning
	 *      Name,Description,IsSummary,ImageIndiactor
	 *  @param  root		root node
	 *  @param  Node_ID     Key of the record
	 *  @param  Parent_ID   Parent ID of the record
	 *  @param  SeqNo       Sort index
	 *  @param  onBar       Node also on Shortcut bar
	 */
	private void loadNode (PreparedStatement pstmt, MNode root,
		int Node_ID, int Parent_ID, int SeqNo, boolean onBar)
	{
		try
		{
			pstmt.setInt(1, Node_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				String Name = rs.getString(1);
				String Description = rs.getString(2);
				boolean IsSummary = rs.getString(3).equals("Y");
				String ImageIndicator = rs.getString(4);

				if (Name != null)
				{
					MNode child = new MNode (Node_ID, Name, Description, IsSummary, ImageIndicator);
					child.add(root, Parent_ID, child);
				}
			}
		//	else
		//		Log.trace(Log.l6_Database,"Not added", "Node_ID=" + Node_ID);
			rs.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadNode", e);
		}
	}   //  loadNode


	/**************************************************************************
	 *  Web Menu Tree Node
	 */
	class MNode
	{
		/**
		 *  Constructor
		 *  @param ID id
		 *  @param Name name
		 *  @param Description description
		 *  @param isSummary summary
		 *  @param Type type
		 */
		public MNode (int ID, String Name, String Description, boolean isSummary, String Type)
		{
			this.ID = ID;
			this.Name = Name;
			this.Description = Description;
			if (this.Description == null)
				this.Description = "";
			this.isSummary = isSummary;
			this.Type = Type;
			if (this.Type == null)
				this.Type = "";
		}

		public int          ID;
		public String       Name;
		public String       Description;
		public boolean      isSummary;
		public String       Type;
		public ArrayList    children = new ArrayList();

		/**
		 *  Add to list of children
		 *  @param child child
		 *  @return true
		 */
		public boolean add (MNode child)
		{
			children.add(child);
			return true;
		}   //  add

		/*********************************************************************/

		/**
		 *  Traverse Tree starting at root to add child to parent with parentID
		 *  @param root root
		 *  @param parentID parent
		 *  @param child child
		 *  @returns    false if not added
		 */
		public boolean add (MNode root, int parentID, MNode child)
		{
			//  is this root the parent?
			if (root.ID == parentID)
				return root.add(child);

			//  do we have children to check?
			else if (root.children.size() == 0)
				return false;

			//  check children
			for (int i = 0; i < root.children.size(); i++)
			{
				MNode cc = (MNode)root.children.get(i);
				if (root.add(cc, parentID, child))
					return true;
			}
			//  nothing found
			return false;
		}   //  add

		/**
		 *  Traverse Tree and print it
		 *  @param root root
		 *  @param buf buffer
		 */
		public void print (MNode root, StringBuffer buf)
		{
			//  Leaf
			if (root.children.size() == 0)
			{
				/**
				 *  <li id="menuXXXXX"><a href="...." onMouseOver="status='Menu Description';return true;">Menu Entry</a></li>
				 */
				String item = "";
				String servletName = "";
				if (root.Type.equals("W"))
				{
					item = "menuWindow";
					servletName = "WWindow";
				}
				else if (root.Type.equals("X"))
				{
					item = "menuWindow";
					servletName = "WForm";
				}
				else if (root.Type.equals("R"))
				{
					item = "menuReport";
					servletName = "WReport";
				}
				else if (root.Type.equals("P"))
				{
					item = "menuProcess";
					servletName = "WProcess";
				}
				else if (root.Type.equals("F"))
				{
					item = "menuWorkflow";
					servletName = "WWorkflow";
				}
				else if (root.Type.equals("T"))
				{
					item = "menuProcess";
					servletName = "WTask";
				}
				else
					servletName = "WError";

				String description = root.Description.replace('\'',' ').replace('"',' ');
				buf.append("<li id=\"" + item + "\"><a href=\"");
				//  url -   /appl/servletName?AD_Menu_ID=x
				buf.append(WEnv.getBaseDirectory(servletName))
					.append("?AD_Menu_ID=")
					.append(root.ID);
				//
				buf.append("\" onMouseOver=\"status='" + description + "';return true;\" onclick=showLoadingWindow(\"" + WEnv.getBaseDirectory("") + "\")>");
				buf.append(root.Name);
				buf.append("</a></li>\n");
			}
			else
			{
				/**
				 *  <li id="foldHeader">MenuEntry</li>
				 *  <ul style="display:none">
				 *  ....
				 *  </ul>
				 */
				buf.append("\n<li id=\"menuHeader\">");         //  summary node
				buf.append(root.Name);
				buf.append("</li>\n");
				//  Next Level
				buf.append("<ul style=\"display:none\">\n");    //  start next level
				for (int i = 0; i < root.children.size(); i++)
				{
					MNode cc = (MNode)root.children.get(i);
					root.print(cc, buf);
				}
				buf.append("</ul>");                                            //  finish next level
			}
		}	//  print

		/**
		 *  Clean tree of parents without children
		 *  @param root root node
		 */
		public void clean (MNode root)
		{
			int size = root.children.size();
			if (size == 0)
				return;
			//
			ArrayList temp = new ArrayList(size);
			boolean changed = false;
			for (int i = 0; i < size; i++)
			{
				MNode cc = (MNode)root.children.get(i);
				int ccSize = cc.children.size();
				if (cc.isSummary && ccSize == 0)
					changed = true;
				else
					temp.add(cc);
				if (ccSize != 0)
					cc.clean(cc);
			}
			if (changed)
				root.children = temp;
		}   //  clean

	}   //  MNode

}   //  WMenu
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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;

import org.apache.ecs.xhtml.*;

import org.compiere.model.*;
import org.compiere.util.*;

/**
 *  Web Menu
 *
 *  @author Jorg Janke
 *  @version  $Id: WMenu.java,v 1.3 2003/12/04 03:25:52 jjanke Exp $
 */
public class WMenu extends HttpServlet
{
	/**
	 *  Initialize global variables
	 *  @param config config
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("WMenu.init");
	}   //  init

	/**
	 * 	Get Servlet information
	 *  @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Menu";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		Log.trace(Log.l1_User, "WMenu.destroy");
		super.destroy();
	}   //  destroy

	/** */
	private PreparedStatement	m_pstmt;


	/*************************************************************************/

	/**
	 *  Process the HTTP Get request.
	 *  - Exit (Logout)
	 *  - AD_Window_ID Forward to Window
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WMenu.doGet - Process Menu Request");
		//  Get Parameter: Exit
		if (request.getParameter("Exit") != null)
		{
			WUtil.createLoginPage (request, response, this, null, "Exit");
			return;
		}

		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		if (ctx == null)
		{
			WUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Window
		int AD_Window_ID = WUtil.getParameterAsInt(request, "AD_Window_ID");

		//  Forward to WWindow
		if (AD_Window_ID != 0)
		{
			Log.trace(Log.l4_Data, "AD_Window_ID=" + AD_Window_ID);
			//
			String url = WEnv.getBaseDirectory("WWindow?AD_Window_ID=" + AD_Window_ID);
			Log.trace(Log.l4_Data, "Forward to - " + url);
			//
			RequestDispatcher rd = getServletContext().getRequestDispatcher(url);
			rd.forward(request, response);
			return;
		}

		//  Request not serviceable
		WUtil.createErrorPage(request, response, this, null, "NotImplemented");
	}   //  doGet


	/*************************************************************************/

	/**
	 *  Process the HTTP Post request - Verify Input & Create Menu
	 *
	 * @param request request
	 * @param response response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WMenu.doPost - Create Menu");
		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		Properties cProp = WUtil.getCookieProprties(request);
		if (ctx == null)
		{
			WUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Get Parameters: Role, Client, Org, Warehouse, Date
		String role = request.getParameter(WLogin.P_ROLE);
		String client = request.getParameter(WLogin.P_CLIENT);
		String org = request.getParameter(WLogin.P_ORG);
		String wh = request.getParameter(WLogin.P_WAREHOUSE);
		if (wh == null)
			wh = "";

		//  Get context
		if (role == null || client == null || org == null)
		{
			WUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Get Info from Context - User, Role, Client
		int AD_User_ID = Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID = Env.getContextAsInt(ctx, "#AD_Role_ID");
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		//  Not available in context yet - Org, Warehouse
		int AD_Org_ID = -1;
		int M_Warehouse_ID = -1;

		//  Get latest info from context
		try
		{
			int req_role = Integer.parseInt(role);
			if (req_role != AD_Role_ID)
			{
				Log.trace(Log.l5_DData, "AD_Role_ID - changed from " + AD_Role_ID);
				AD_Role_ID = req_role;
				Env.setContext(ctx, "#AD_Role_ID", AD_Role_ID);
			}
			Log.trace(Log.l5_DData, "AD_Role_ID = " + AD_Role_ID);
			//
			int req_client = Integer.parseInt(client);
			if (req_client != AD_Client_ID)
			{
				Log.trace(Log.l5_DData, "AD_Client_ID - changed from " + AD_Client_ID);
				AD_Client_ID = req_client;
				Env.setContext(ctx, "#AD_Client_ID", AD_Client_ID);
			}
			Log.trace(Log.l5_DData, "AD_Client_ID = " + AD_Client_ID);
			//
			AD_Org_ID = Integer.parseInt(org);
			Log.trace(Log.l5_DData, "AD_Org_ID = " + AD_Org_ID);
			//
			if (wh.length() > 0)
			{
				M_Warehouse_ID = Integer.parseInt(wh);
				Log.trace(Log.l5_DData, "M_Warehouse_ID = " + M_Warehouse_ID);
			}
		}
		catch (Exception e)
		{
			Log.error("WMenu.doPost - Parameter", e);
			WUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Check Login info and set environment
		String loginInfo = checkLogin (ctx, AD_User_ID, AD_Role_ID, AD_Client_ID, AD_Org_ID, M_Warehouse_ID);
		if (loginInfo == null)
		{
			WUtil.createErrorPage(request, response, this, ctx, Msg.getMsg(ctx, "RoleInconsistent"));
			return;
		}
		sess.setAttribute(WEnv.SA_LOGININFO, loginInfo);

		//  Set cookie for future defaults
		cProp.setProperty(WLogin.P_ROLE, String.valueOf(AD_Role_ID));
		cProp.setProperty(WLogin.P_CLIENT, String.valueOf(AD_Client_ID));
		cProp.setProperty(WLogin.P_ORG, String.valueOf(AD_Org_ID));
		if (M_Warehouse_ID == -1)
			cProp.setProperty(WLogin.P_WAREHOUSE, "");
		else
			cProp.setProperty(WLogin.P_WAREHOUSE, String.valueOf(M_Warehouse_ID));

		//  Set Date
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		//  Try to parse Date
		String dateString = request.getParameter(WLogin.P_DATE);
		try
		{
			if (dateString != null && dateString.length() > 0)
			{
				Language language = (Language)sess.getAttribute(WEnv.SA_LANGUAGE);
				DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
				java.util.Date date = df.parse(dateString);
				ts = new Timestamp(date.getTime());
			}
		}
		catch (Exception e)
		{
			Log.trace(Log.l1_User, "WMenu.doPost - Cannot parse date: " + dateString + " - " + e.getMessage());
		}
		Env.setContext(ctx, "#Date", ts.toString());    //  JDBC format


	//	Log.printProperties(System.getProperties(), "System");
	//	Log.printProperties(cProp, "Cookie");
	//	Log.printProperties(ctx, "Servlet Context");
	//	Log.printProperties(Env.getCtx(), "Apps Env Context");


		/**********************************************************************
		 *  Create Menu output
		 */
		String windowTitle = Msg.getMsg(ctx, "Menu");
		String statusMessage = Msg.getMsg(ctx, "SelectMenuItem");

		//	Document
		WDoc doc = WDoc.create (windowTitle);
		head header = doc.getHead();
		//  Target
		header.addElement(new base().setTarget(WEnv.TARGET_WINDOW));
		//  Specific Menu Script/Stylesheet
		header.addElement(new script("", WEnv.getBaseDirectory("menu.js"), "text/javascript", "JavaScript1.2"));
		header.addElement(new link().setRel("stylesheet").setHref(WEnv.getBaseDirectory("menu.css")));

		//	Body
		body body = doc.getBody();
		//  Header
		body.addElement(new cite(loginInfo));
		String title = windowTitle + " - " + loginInfo;
		body.addElement(new script("top.document.title='" + title + "';"));
		body.addElement(new script("defaultStatus = '" + statusMessage + "';"));

		//  Clear Window Frame
		body.addElement(WUtil.getClearFrame(WEnv.TARGET_WINDOW));

		//  Load Menu Structure     ----------------------
		int AD_Tree_ID = 10;	//	Menu
		MTree tree = new MTree (ctx, AD_Tree_ID, false);
		StringBuffer buf = new StringBuffer();
		Enumeration en = tree.getRoot().preorderEnumeration();
		//
		int oldLevel = 0;
		while (en.hasMoreElements())
		{
			MTreeNode nd = (MTreeNode)en.nextElement();

			//  Level
			int level = nd.getLevel();	//	0 == root
			if (level == 0)
				continue;
			while (oldLevel < level)
			{
				if (level > 1)
					buf.append("<ul style=\"display:none\">\n");//  start next level
				else
					buf.append("<ul>\n");						//  start next level
				oldLevel++;
			}
			while (oldLevel > level)
			{
				buf.append("</ul>");                            //  finish next level
				oldLevel--;
			}
				
			//	Print Node
			buf.append(printNode(nd));
		}
		while (oldLevel >  0)
		{
			buf.append("</ul>");	                            //  finish next level
			oldLevel--;
		}
		body.addElement(buf.toString());

		//  Exit Info
		body.addElement(new hr());
		String url = request.getRequestURI() + "?Exit=true";
		body.addElement(new a(url, Msg.getMsg(ctx, "Exit")));
System.out.println(doc);

		//  Can we store Cookie ?
		if (!cProp.getProperty(WLogin.P_STORE, "N").equals("Y"))
			cProp.clear();

		WUtil.createResponse (request, response, this, cProp, doc, false);
	}   //  doPost

	/**
	 *  Check Login information and set context.
	 *  @returns    true if login info are OK
	 *  @param ctx context
	 *  @param AD_User_ID user
	 *  @param AD_Role_ID role
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param M_Warehouse_ID warehouse
	 */
	private String checkLogin (Properties ctx, int AD_User_ID, int AD_Role_ID, int AD_Client_ID, int AD_Org_ID, int M_Warehouse_ID)
	{
		//  Get Login Info
		String loginInfo = null;
		//  Verify existance of User/Client/Org/Role and User's acces to Client & Org
		String sql = "SELECT u.Name || '@' || c.Name || '.' || o.Name || ' [' || INITCAP(USER) || ']' AS Text "
			+ "FROM AD_User u, AD_Client c, AD_Org o, AD_User_Roles ur "
			+ "WHERE u.AD_User_ID=?"    //  #1
			+ " AND c.AD_Client_ID=?"   //  #2
			+ " AND o.AD_Org_ID=?"      //  #3
			+ " AND ur.AD_Role_ID=?"    //  #4
			+ " AND ur.AD_User_ID=u.AD_User_ID"
			+ " AND (o.AD_Client_ID = 0 OR o.AD_Client_ID=c.AD_Client_ID)"
			+ " AND c.AD_Client_ID IN (SELECT AD_Client_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)"
			+ " AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			pstmt.setInt(2, AD_Client_ID);
			pstmt.setInt(3, AD_Org_ID);
			pstmt.setInt(4, AD_Role_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				loginInfo = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("WMenu.checkLogin", e);
		}

		//  not verified
		if (loginInfo == null)
			return null;

		//  Set Preferences
		KeyNamePair org = new KeyNamePair(AD_Org_ID, String.valueOf(AD_Org_ID));
		KeyNamePair wh = null;
		if (M_Warehouse_ID > 0)
			wh = new KeyNamePair(M_Warehouse_ID, String.valueOf(M_Warehouse_ID));
		//
		Timestamp date = null;
		String printer = null;
		DB.loadPreferences(ctx, org, wh, date, printer);
		//
		return loginInfo;
	}   //  checkLogin


	/**
	 *  Print Menu Item
	 *  @param node node
	 */
	public StringBuffer printNode (MTreeNode node)
	{
		StringBuffer sb = new StringBuffer();
		
		//  Leaf
		if (!node.isSummary())
		{
			/**
			 *  <li id="menuXXXXX"><a href="...." onMouseOver="status='Menu Description';return true;">Menu Entry</a></li>
			 */
			String item = "";
			String servletName = "";
			if (node.isWindow())
			{
				item = "menuWindow";
				servletName = "WWindow";
			}
			else if (node.isForm())
			{
				item = "menuWindow";
				servletName = "WForm";
			}
			else if (node.isReport())
			{
				item = "menuReport";
				servletName = "WReport";
			}
			else if (node.isProcess())
			{
				item = "menuProcess";
				servletName = "WProcess";
			}
			else if (node.isWorkFlow())
			{
				item = "menuWorkflow";
				servletName = "WWorkflow";
			}
			else if (node.isTask())
			{
				item = "menuProcess";
				servletName = "WTask";
			}
			else
				servletName = "WError";

			String description = node.getDescription().replace('\'',' ').replace('"',' ');
			sb.append("<li id=\"" + item + "\"><a href=\"")
				//  url -   /appl/servletName?AD_Menu_ID=x
				.append(WEnv.getBaseDirectory(servletName))
				.append("?AD_Menu_ID=")
				.append(node.getNode_ID())
				//
				.append("\" onMouseOver=\"status='" + description + "';return true;\" onclick=showLoadingWindow(\"" + WEnv.getBaseDirectory("") + "\")>")
				.append(node.getName())
				.append("</a></li>\n");
		}
		else
		{
			/**
			 *  <li id="foldHeader">MenuEntry</li>
			 *  <ul style="display:none">
			 *  ....
			 *  </ul>
			 */
			sb.append("\n<li id=\"menuHeader\">")		//  summary node
				.append(node.getName())
				.append("</li>\n");
		}
		return sb;
	}	//  printNode


}   //  WMenu
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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;

import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Web Menu
 *
 *  @author Jorg Janke
 *  @version  $Id: WMenu.java,v 1.1.1.1 2002/10/12 01:06:55 jjanke Exp $
 */
public class WMenu extends HttpServlet
{
	/**
	 *  Initialize global variables
	 *  @param config config
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("WMenu.init");
	}   //  init

	/**
	 * 	Get Servlet information
	 *  @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Menu";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		Log.trace(Log.l1_User, "WMenu.destroy");
		super.destroy();
	}   //  destroy

	/** */
	private PreparedStatement	m_pstmt;


	/*************************************************************************/

	/**
	 *  Process the HTTP Get request.
	 *  - Exit (Logout)
	 *  - AD_Window_ID Forward to Window
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WMenu.doGet - Process Menu Request");
		//  Get Parameter: Exit
		if (request.getParameter("Exit") != null)
		{
			WUtil.createLoginPage (request, response, this, null, "Exit");
			return;
		}

		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		if (ctx == null)
		{
			WUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Window
		int AD_Window_ID = WUtil.getParameterAsInt(request, "AD_Window_ID");

		//  Forward to WWindow
		if (AD_Window_ID != 0)
		{
			Log.trace(Log.l4_Data, "AD_Window_ID=" + AD_Window_ID);
			//
			String url = WEnv.getBaseDirectory("WWindow?AD_Window_ID=" + AD_Window_ID);
			Log.trace(Log.l4_Data, "Forward to - " + url);
			//
			RequestDispatcher rd = getServletContext().getRequestDispatcher(url);
			rd.forward(request, response);
			return;
		}

		//  Request not serviceable
		WUtil.createErrorPage(request, response, this, null, "NotImplemented");
	}   //  doGet


	/*************************************************************************/

	/**
	 *  Process the HTTP Post request - Verify Input & Create Menu
	 *
	 * @param request request
	 * @param response response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WMenu.doPost - Create Menu");
		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		Properties cProp = WUtil.getCookieProprties(request);
		if (ctx == null)
		{
			WUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Get Parameters: Role, Client, Org, Warehouse, Date
		String role = request.getParameter(WLogin.P_ROLE);
		String client = request.getParameter(WLogin.P_CLIENT);
		String org = request.getParameter(WLogin.P_ORG);
		String wh = request.getParameter(WLogin.P_WAREHOUSE);
		if (wh == null)
			wh = "";

		//  Get context
		if (role == null || client == null || org == null)
		{
			WUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Get Info from Context - User, Role, Client
		int AD_User_ID = Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID = Env.getContextAsInt(ctx, "#AD_Role_ID");
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		//  Not available in context yet - Org, Warehouse
		int AD_Org_ID = -1;
		int M_Warehouse_ID = -1;

		//  Get latest info from context
		try
		{
			int req_role = Integer.parseInt(role);
			if (req_role != AD_Role_ID)
			{
				Log.trace(Log.l5_DData, "AD_Role_ID - changed from " + AD_Role_ID);
				AD_Role_ID = req_role;
				Env.setContext(ctx, "#AD_Role_ID", AD_Role_ID);
			}
			Log.trace(Log.l5_DData, "AD_Role_ID = " + AD_Role_ID);
			//
			int req_client = Integer.parseInt(client);
			if (req_client != AD_Client_ID)
			{
				Log.trace(Log.l5_DData, "AD_Client_ID - changed from " + AD_Client_ID);
				AD_Client_ID = req_client;
				Env.setContext(ctx, "#AD_Client_ID", AD_Client_ID);
			}
			Log.trace(Log.l5_DData, "AD_Client_ID = " + AD_Client_ID);
			//
			AD_Org_ID = Integer.parseInt(org);
			Log.trace(Log.l5_DData, "AD_Org_ID = " + AD_Org_ID);
			//
			if (wh.length() > 0)
			{
				M_Warehouse_ID = Integer.parseInt(wh);
				Log.trace(Log.l5_DData, "M_Warehouse_ID = " + M_Warehouse_ID);
			}
		}
		catch (Exception e)
		{
			Log.error("WMenu.doPost - Parameter", e);
			WUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Check Login info and set environment
		String loginInfo = checkLogin (ctx, AD_User_ID, AD_Role_ID, AD_Client_ID, AD_Org_ID, M_Warehouse_ID);
		if (loginInfo == null)
		{
			WUtil.createErrorPage(request, response, this, ctx, Msg.getMsg(ctx, "RoleInconsistent"));
			return;
		}
		sess.setAttribute(WEnv.SA_LOGININFO, loginInfo);

		//  Set cookie for future defaults
		cProp.setProperty(WLogin.P_ROLE, String.valueOf(AD_Role_ID));
		cProp.setProperty(WLogin.P_CLIENT, String.valueOf(AD_Client_ID));
		cProp.setProperty(WLogin.P_ORG, String.valueOf(AD_Org_ID));
		if (M_Warehouse_ID == -1)
			cProp.setProperty(WLogin.P_WAREHOUSE, "");
		else
			cProp.setProperty(WLogin.P_WAREHOUSE, String.valueOf(M_Warehouse_ID));

		//  Set Date
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		//  Try to parse Date
		String dateString = request.getParameter(WLogin.P_DATE);
		try
		{
			if (dateString != null && dateString.length() > 0)
			{
				Language language = (Language)sess.getAttribute(WEnv.SA_LANGUAGE);
				DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
				java.util.Date date = df.parse(dateString);
				ts = new Timestamp(date.getTime());
			}
		}
		catch (Exception e)
		{
			Log.trace(Log.l1_User, "WMenu.doPost - Cannot parse date: " + dateString + " - " + e.getMessage());
		}
		Env.setContext(ctx, "#Date", ts.toString());    //  JDBC format


	//	Log.printProperties(System.getProperties(), "System");
	//	Log.printProperties(cProp, "Cookie");
	//	Log.printProperties(ctx, "Servlet Context");
	//	Log.printProperties(Env.getCtx(), "Apps Env Context");


		/**********************************************************************
		 *  Create Menu output
		 */
		String windowTitle = Msg.getMsg(ctx, "Menu");
		String statusMessage = Msg.getMsg(ctx, "SelectMenuItem");

		//	Document
		WDoc doc = WDoc.create (windowTitle);
		head header = doc.getHead();
		//  Target
		header.addElement(new base().setTarget(WEnv.TARGET_WINDOW));
		//  Specific Menu Script/Stylesheet
		header.addElement(new script("", WEnv.getBaseDirectory("menu.js"), "text/javascript", "JavaScript1.2"));
		header.addElement(new link().setRel("stylesheet").setHref(WEnv.getBaseDirectory("menu.css")));

		//	Body
		body body = doc.getBody();
		//  Header
		body.addElement(new cite(loginInfo));
		String title = windowTitle + " - " + loginInfo;
		body.addElement(new script("top.document.title='" + title + "';"));
		body.addElement(new script("defaultStatus = '" + statusMessage + "';"));

		//  Clear Window Frame
		body.addElement(WUtil.getClearFrame(WEnv.TARGET_WINDOW));

		//  Load Menu Structure     ----------------------
		MNode root = loadTree(AD_Client_ID, AD_Role_ID, AD_User_ID, Env.getAD_Language(ctx));
		//
		StringBuffer buf = new StringBuffer();
		buf.append("<ul>");
		for (int i = 0; i < root.children.size(); i++)
		{
			MNode node = (MNode)root.children.get(i);
			node.print(node, buf);
		}
		buf.append("</ul>");
		body.addElement(buf.toString());

		//  Exit Info
		body.addElement(new hr());
		String url = request.getRequestURI() + "?Exit=true";
		body.addElement(new a(url, Msg.getMsg(ctx, "Exit")));

		//  Can we store Cookie ?
		if (!cProp.getProperty(WLogin.P_STORE, "N").equals("Y"))
			cProp.clear();

		WUtil.createResponse (request, response, this, cProp, doc, false);
	}   //  doPost

	/**
	 *  Check Login information and set context.
	 *  @returns    true if login info are OK
	 *  @param ctx context
	 *  @param AD_User_ID user
	 *  @param AD_Role_ID role
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param M_Warehouse_ID warehouse
	 */
	private String checkLogin (Properties ctx, int AD_User_ID, int AD_Role_ID, int AD_Client_ID, int AD_Org_ID, int M_Warehouse_ID)
	{
		//  Get Login Info
		String loginInfo = null;
		//  Verify existance of User/Client/Org/Role and User's acces to Client & Org
		String sql = "SELECT u.Name || '@' || c.Name || '.' || o.Name || ' [' || INITCAP(USER) || ']' AS Text "
			+ "FROM AD_User u, AD_Client c, AD_Org o, AD_User_Roles ur "
			+ "WHERE u.AD_User_ID=?"    //  #1
			+ " AND c.AD_Client_ID=?"   //  #2
			+ " AND o.AD_Org_ID=?"      //  #3
			+ " AND ur.AD_Role_ID=?"    //  #4
			+ " AND ur.AD_User_ID=u.AD_User_ID"
			+ " AND (o.AD_Client_ID = 0 OR o.AD_Client_ID=c.AD_Client_ID)"
			+ " AND c.AD_Client_ID IN (SELECT AD_Client_ID FROM AD_Role_ClientAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)"
			+ " AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			pstmt.setInt(2, AD_Client_ID);
			pstmt.setInt(3, AD_Org_ID);
			pstmt.setInt(4, AD_Role_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				loginInfo = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("WMenu.checkLogin", e);
		}

		//  not verified
		if (loginInfo == null)
			return null;

		//  Set Preferences
		KeyNamePair org = new KeyNamePair(AD_Org_ID, String.valueOf(AD_Org_ID));
		KeyNamePair wh = null;
		if (M_Warehouse_ID > 0)
			wh = new KeyNamePair(M_Warehouse_ID, String.valueOf(M_Warehouse_ID));
		//
		Timestamp date = null;
		String printer = null;
		DB.loadPreferences(ctx, org, wh, date, printer);
		//
		return loginInfo;
	}   //  checkLogin


	/*************************************************************************/

	/**
	 *  Load Tree and return root node
	 *  @param AD_Client_ID client
	 *  @param AD_Role_ID role
	 *  @param AD_User_ID user
	 *  @param AD_Language language
	 *  @return node
	 */
	private MNode loadTree (int AD_Client_ID, int AD_Role_ID, int AD_User_ID, String AD_Language)
	{
		//	Get Tree info with start node
		int     AD_Tree_ID;
		String	Name;
		String	TreeType;
		String	Description;
		int 	startID;
		//
		MNode   root = null;

		//  Get Root Node
		String SQL = "SELECT t.AD_Tree_ID, t.Name, t.Description, t.TreeType, tn.Node_ID "
			+ "FROM AD_Tree t, AD_ClientInfo c, AD_TreeNode tn "
			+ "WHERE t.AD_Tree_ID=tn.AD_Tree_ID"
			+ " AND tn.Parent_ID IS NULL"
			+ " AND t.AD_Tree_ID=c.AD_Tree_Menu_ID"
			+ " AND c.AD_Client_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(SQL);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				return null;
			}
			AD_Tree_ID = rs.getInt(1);
			Name = rs.getString(2);
			Description = rs.getString(3);
			TreeType = rs.getString(4);
			startID = rs.getInt(5);

			//	create root node
			root = new MNode (startID, Name, Description, true, "");
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadTree -1", e);
			return null;
		}

		//  SQL for TreeNodes
		StringBuffer cmd = new StringBuffer("SELECT tn.Node_ID,tn.Parent_ID,tn.SeqNo, "
			+ "(SELECT 'Y' FROM AD_TreeBar tb WHERE tb.AD_Tree_ID=tn.AD_Tree_ID AND tb.AD_User_ID=")
			.append(AD_User_ID).append(" AND tb.Node_ID=tn.Node_ID) "
			+ "FROM AD_TreeNode tn ")
			.append("WHERE tn.IsActive='Y' "
			+ "START WITH Parent_ID IS NULL AND AD_Tree_ID=? "
			+ "CONNECT BY Parent_ID=PRIOR Node_ID AND AD_Tree_ID=? "
			+ "ORDER BY LEVEL, SeqNo");

		//  SQL for Node Info
		StringBuffer cmdNode = new StringBuffer();
		boolean base = Env.isBaseLanguage(Env.getCtx(), "AD_Menu");
		if (base)
			cmdNode.append("SELECT m.Name,m.Description,m.IsSummary,m.Action "
				+ "FROM AD_Menu m");
		else
			cmdNode.append("SELECT t.Name,t.Description,m.IsSummary,m.Action "
				+ "FROM AD_Menu m, AD_Menu_Trl t");
			cmdNode.append(", (SELECT ").append(AD_Role_ID).append(" AS XRole FROM DUAL)");
		cmdNode.append(" WHERE m.AD_Menu_ID=?");
		if (!base)
			cmdNode.append(" AND m.AD_Menu_ID=t.AD_Menu_ID AND t.AD_Language='")
				.append(Env.getAD_Language(Env.getCtx())).append("'");

		cmdNode.append(" AND m.IsActive='Y' "
			+ "AND (m.IsSummary='Y' OR m.Action='B'"
			+ " OR EXISTS (SELECT * FROM AD_Window_Access wa WHERE wa.AD_Window_ID=m.AD_Window_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Process_Access wa WHERE wa.AD_Process_ID=m.AD_Process_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Form_Access wa WHERE wa.AD_Form_ID=m.AD_Form_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Task_Access wa WHERE wa.AD_Task_ID=m.AD_Task_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Workflow_Access wa WHERE wa.AD_Workflow_ID=m.AD_Workflow_ID AND wa.AD_Role_ID=XRole)"
			+ ")");
	//	Log.trace(Log.l6_Database, "SQL Tree", cmd.toString());
	//	Log.trace(Log.l6_Database, "SQL Node", cmdNode.toString());

		//  The Node Loop
		try
		{
			PreparedStatement pstmtNode = DB.prepareStatement(cmdNode.toString());
			//
			PreparedStatement pstmt = DB.prepareStatement(cmd.toString());
			pstmt.setInt(1, AD_Tree_ID);
			pstmt.setInt(2, AD_Tree_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int Node_ID = rs.getInt(1);
				int Parent_ID = rs.getInt(2);
				int SeqNo = rs.getInt(3);
				boolean onBar = (rs.getString(4) != null);
				loadNode (pstmtNode, root, Node_ID, Parent_ID, SeqNo, onBar);
			}
			rs.close();
			pstmt.close();
			pstmtNode.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadTree -2", e);
		}

		//  Clean Menu tree
		root.clean(root);

		return root;
	}   //  loadTree

	/**
	 *  Load Node using prepared statement
	 *  @param  pstmt       Prepared Statement requiring to set Node_ID and returning
	 *      Name,Description,IsSummary,ImageIndiactor
	 *  @param  root		root node
	 *  @param  Node_ID     Key of the record
	 *  @param  Parent_ID   Parent ID of the record
	 *  @param  SeqNo       Sort index
	 *  @param  onBar       Node also on Shortcut bar
	 */
	private void loadNode (PreparedStatement pstmt, MNode root,
		int Node_ID, int Parent_ID, int SeqNo, boolean onBar)
	{
		try
		{
			pstmt.setInt(1, Node_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				String Name = rs.getString(1);
				String Description = rs.getString(2);
				boolean IsSummary = rs.getString(3).equals("Y");
				String ImageIndicator = rs.getString(4);

				if (Name != null)
				{
					MNode child = new MNode (Node_ID, Name, Description, IsSummary, ImageIndicator);
					child.add(root, Parent_ID, child);
				}
			}
		//	else
		//		Log.trace(Log.l6_Database,"Not added", "Node_ID=" + Node_ID);
			rs.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadNode", e);
		}
	}   //  loadNode


	/**************************************************************************
	 *  Web Menu Tree Node
	 */
	class MNode
	{
		/**
		 *  Constructor
		 *  @param ID id
		 *  @param Name name
		 *  @param Description description
		 *  @param isSummary summary
		 *  @param Type type
		 */
		public MNode (int ID, String Name, String Description, boolean isSummary, String Type)
		{
			this.ID = ID;
			this.Name = Name;
			this.Description = Description;
			if (this.Description == null)
				this.Description = "";
			this.isSummary = isSummary;
			this.Type = Type;
			if (this.Type == null)
				this.Type = "";
		}

		public int          ID;
		public String       Name;
		public String       Description;
		public boolean      isSummary;
		public String       Type;
		public ArrayList    children = new ArrayList();

		/**
		 *  Add to list of children
		 *  @param child child
		 *  @return true
		 */
		public boolean add (MNode child)
		{
			children.add(child);
			return true;
		}   //  add

		/*********************************************************************/

		/**
		 *  Traverse Tree starting at root to add child to parent with parentID
		 *  @param root root
		 *  @param parentID parent
		 *  @param child child
		 *  @returns    false if not added
		 */
		public boolean add (MNode root, int parentID, MNode child)
		{
			//  is this root the parent?
			if (root.ID == parentID)
				return root.add(child);

			//  do we have children to check?
			else if (root.children.size() == 0)
				return false;

			//  check children
			for (int i = 0; i < root.children.size(); i++)
			{
				MNode cc = (MNode)root.children.get(i);
				if (root.add(cc, parentID, child))
					return true;
			}
			//  nothing found
			return false;
		}   //  add

		/**
		 *  Traverse Tree and print it
		 *  @param root root
		 *  @param buf buffer
		 */
		public void print (MNode root, StringBuffer buf)
		{
			//  Leaf
			if (root.children.size() == 0)
			{
				/**
				 *  <li id="menuXXXXX"><a href="...." onMouseOver="status='Menu Description';return true;">Menu Entry</a></li>
				 */
				String item = "";
				String servletName = "";
				if (root.Type.equals("W"))
				{
					item = "menuWindow";
					servletName = "WWindow";
				}
				else if (root.Type.equals("X"))
				{
					item = "menuWindow";
					servletName = "WForm";
				}
				else if (root.Type.equals("R"))
				{
					item = "menuReport";
					servletName = "WReport";
				}
				else if (root.Type.equals("P"))
				{
					item = "menuProcess";
					servletName = "WProcess";
				}
				else if (root.Type.equals("F"))
				{
					item = "menuWorkflow";
					servletName = "WWorkflow";
				}
				else if (root.Type.equals("T"))
				{
					item = "menuProcess";
					servletName = "WTask";
				}
				else
					servletName = "WError";

				String description = root.Description.replace('\'',' ').replace('"',' ');
				buf.append("<li id=\"" + item + "\"><a href=\"");
				//  url -   /appl/servletName?AD_Menu_ID=x
				buf.append(WEnv.getBaseDirectory(servletName))
					.append("?AD_Menu_ID=")
					.append(root.ID);
				//
				buf.append("\" onMouseOver=\"status='" + description + "';return true;\" onclick=showLoadingWindow(\"" + WEnv.getBaseDirectory("") + "\")>");
				buf.append(root.Name);
				buf.append("</a></li>\n");
			}
			else
			{
				/**
				 *  <li id="foldHeader">MenuEntry</li>
				 *  <ul style="display:none">
				 *  ....
				 *  </ul>
				 */
				buf.append("\n<li id=\"menuHeader\">");         //  summary node
				buf.append(root.Name);
				buf.append("</li>\n");
				//  Next Level
				buf.append("<ul style=\"display:none\">\n");    //  start next level
				for (int i = 0; i < root.children.size(); i++)
				{
					MNode cc = (MNode)root.children.get(i);
					root.print(cc, buf);
				}
				buf.append("</ul>");                                            //  finish next level
			}
		}	//  print

		/**
		 *  Clean tree of parents without children
		 *  @param root root node
		 */
		public void clean (MNode root)
		{
			int size = root.children.size();
			if (size == 0)
				return;
			//
			ArrayList temp = new ArrayList(size);
			boolean changed = false;
			for (int i = 0; i < size; i++)
			{
				MNode cc = (MNode)root.children.get(i);
				int ccSize = cc.children.size();
				if (cc.isSummary && ccSize == 0)
					changed = true;
				else
					temp.add(cc);
				if (ccSize != 0)
					cc.clean(cc);
			}
			if (changed)
				root.children = temp;
		}   //  clean

	}   //  MNode

}   //  WMenu
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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;

import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Web Menu
 *
 *  @author Jorg Janke
 *  @version  $Id: WMenu.java,v 1.2 2003/03/31 05:27:26 jjanke Exp $
 */
public class WMenu extends HttpServlet
{
	/**
	 *  Initialize global variables
	 *  @param config config
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("WMenu.init");
	}   //  init

	/**
	 * 	Get Servlet information
	 *  @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Menu";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		Log.trace(Log.l1_User, "WMenu.destroy");
		super.destroy();
	}   //  destroy

	/** */
	private PreparedStatement	m_pstmt;


	/*************************************************************************/

	/**
	 *  Process the HTTP Get request.
	 *  - Exit (Logout)
	 *  - AD_Window_ID Forward to Window
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WMenu.doGet - Process Menu Request");
		//  Get Parameter: Exit
		if (request.getParameter("Exit") != null)
		{
			WUtil.createLoginPage (request, response, this, null, "Exit");
			return;
		}

		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		if (ctx == null)
		{
			WUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Window
		int AD_Window_ID = WUtil.getParameterAsInt(request, "AD_Window_ID");

		//  Forward to WWindow
		if (AD_Window_ID != 0)
		{
			Log.trace(Log.l4_Data, "AD_Window_ID=" + AD_Window_ID);
			//
			String url = WEnv.getBaseDirectory("WWindow?AD_Window_ID=" + AD_Window_ID);
			Log.trace(Log.l4_Data, "Forward to - " + url);
			//
			RequestDispatcher rd = getServletContext().getRequestDispatcher(url);
			rd.forward(request, response);
			return;
		}

		//  Request not serviceable
		WUtil.createErrorPage(request, response, this, null, "NotImplemented");
	}   //  doGet


	/*************************************************************************/

	/**
	 *  Process the HTTP Post request - Verify Input & Create Menu
	 *
	 * @param request request
	 * @param response response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WMenu.doPost - Create Menu");
		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		Properties cProp = WUtil.getCookieProprties(request);
		if (ctx == null)
		{
			WUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Get Parameters: Role, Client, Org, Warehouse, Date
		String role = request.getParameter(WLogin.P_ROLE);
		String client = request.getParameter(WLogin.P_CLIENT);
		String org = request.getParameter(WLogin.P_ORG);
		String wh = request.getParameter(WLogin.P_WAREHOUSE);
		if (wh == null)
			wh = "";

		//  Get context
		if (role == null || client == null || org == null)
		{
			WUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Get Info from Context - User, Role, Client
		int AD_User_ID = Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID = Env.getContextAsInt(ctx, "#AD_Role_ID");
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		//  Not available in context yet - Org, Warehouse
		int AD_Org_ID = -1;
		int M_Warehouse_ID = -1;

		//  Get latest info from context
		try
		{
			int req_role = Integer.parseInt(role);
			if (req_role != AD_Role_ID)
			{
				Log.trace(Log.l5_DData, "AD_Role_ID - changed from " + AD_Role_ID);
				AD_Role_ID = req_role;
				Env.setContext(ctx, "#AD_Role_ID", AD_Role_ID);
			}
			Log.trace(Log.l5_DData, "AD_Role_ID = " + AD_Role_ID);
			//
			int req_client = Integer.parseInt(client);
			if (req_client != AD_Client_ID)
			{
				Log.trace(Log.l5_DData, "AD_Client_ID - changed from " + AD_Client_ID);
				AD_Client_ID = req_client;
				Env.setContext(ctx, "#AD_Client_ID", AD_Client_ID);
			}
			Log.trace(Log.l5_DData, "AD_Client_ID = " + AD_Client_ID);
			//
			AD_Org_ID = Integer.parseInt(org);
			Log.trace(Log.l5_DData, "AD_Org_ID = " + AD_Org_ID);
			//
			if (wh.length() > 0)
			{
				M_Warehouse_ID = Integer.parseInt(wh);
				Log.trace(Log.l5_DData, "M_Warehouse_ID = " + M_Warehouse_ID);
			}
		}
		catch (Exception e)
		{
			Log.error("WMenu.doPost - Parameter", e);
			WUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Check Login info and set environment
		String loginInfo = checkLogin (ctx, AD_User_ID, AD_Role_ID, AD_Client_ID, AD_Org_ID, M_Warehouse_ID);
		if (loginInfo == null)
		{
			WUtil.createErrorPage(request, response, this, ctx, Msg.getMsg(ctx, "RoleInconsistent"));
			return;
		}
		sess.setAttribute(WEnv.SA_LOGININFO, loginInfo);

		//  Set cookie for future defaults
		cProp.setProperty(WLogin.P_ROLE, String.valueOf(AD_Role_ID));
		cProp.setProperty(WLogin.P_CLIENT, String.valueOf(AD_Client_ID));
		cProp.setProperty(WLogin.P_ORG, String.valueOf(AD_Org_ID));
		if (M_Warehouse_ID == -1)
			cProp.setProperty(WLogin.P_WAREHOUSE, "");
		else
			cProp.setProperty(WLogin.P_WAREHOUSE, String.valueOf(M_Warehouse_ID));

		//  Set Date
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		//  Try to parse Date
		String dateString = request.getParameter(WLogin.P_DATE);
		try
		{
			if (dateString != null && dateString.length() > 0)
			{
				Language language = (Language)sess.getAttribute(WEnv.SA_LANGUAGE);
				DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
				java.util.Date date = df.parse(dateString);
				ts = new Timestamp(date.getTime());
			}
		}
		catch (Exception e)
		{
			Log.trace(Log.l1_User, "WMenu.doPost - Cannot parse date: " + dateString + " - " + e.getMessage());
		}
		Env.setContext(ctx, "#Date", ts.toString());    //  JDBC format


	//	Log.printProperties(System.getProperties(), "System");
	//	Log.printProperties(cProp, "Cookie");
	//	Log.printProperties(ctx, "Servlet Context");
	//	Log.printProperties(Env.getCtx(), "Apps Env Context");


		/**********************************************************************
		 *  Create Menu output
		 */
		String windowTitle = Msg.getMsg(ctx, "Menu");
		String statusMessage = Msg.getMsg(ctx, "SelectMenuItem");

		//	Document
		WDoc doc = WDoc.create (windowTitle);
		head header = doc.getHead();
		//  Target
		header.addElement(new base().setTarget(WEnv.TARGET_WINDOW));
		//  Specific Menu Script/Stylesheet
		header.addElement(new script("", WEnv.getBaseDirectory("menu.js"), "text/javascript", "JavaScript1.2"));
		header.addElement(new link().setRel("stylesheet").setHref(WEnv.getBaseDirectory("menu.css")));

		//	Body
		body body = doc.getBody();
		//  Header
		body.addElement(new cite(loginInfo));
		String title = windowTitle + " - " + loginInfo;
		body.addElement(new script("top.document.title='" + title + "';"));
		body.addElement(new script("defaultStatus = '" + statusMessage + "';"));

		//  Clear Window Frame
		body.addElement(WUtil.getClearFrame(WEnv.TARGET_WINDOW));

		//  Load Menu Structure     ----------------------
		MNode root = loadTree(AD_Client_ID, AD_Role_ID, AD_User_ID, Env.getAD_Language(ctx));
		//
		StringBuffer buf = new StringBuffer();
		buf.append("<ul>");
		for (int i = 0; i < root.children.size(); i++)
		{
			MNode node = (MNode)root.children.get(i);
			node.print(node, buf);
		}
		buf.append("</ul>");
		body.addElement(buf.toString());

		//  Exit Info
		body.addElement(new hr());
		String url = request.getRequestURI() + "?Exit=true";
		body.addElement(new a(url, Msg.getMsg(ctx, "Exit")));

		//  Can we store Cookie ?
		if (!cProp.getProperty(WLogin.P_STORE, "N").equals("Y"))
			cProp.clear();

		WUtil.createResponse (request, response, this, cProp, doc, false);
	}   //  doPost

	/**
	 *  Check Login information and set context.
	 *  @returns    true if login info are OK
	 *  @param ctx context
	 *  @param AD_User_ID user
	 *  @param AD_Role_ID role
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param M_Warehouse_ID warehouse
	 */
	private String checkLogin (Properties ctx, int AD_User_ID, int AD_Role_ID, int AD_Client_ID, int AD_Org_ID, int M_Warehouse_ID)
	{
		//  Get Login Info
		String loginInfo = null;
		//  Verify existance of User/Client/Org/Role and User's acces to Client & Org
		String sql = "SELECT u.Name || '@' || c.Name || '.' || o.Name || ' [' || INITCAP(USER) || ']' AS Text "
			+ "FROM AD_User u, AD_Client c, AD_Org o, AD_User_Roles ur "
			+ "WHERE u.AD_User_ID=?"    //  #1
			+ " AND c.AD_Client_ID=?"   //  #2
			+ " AND o.AD_Org_ID=?"      //  #3
			+ " AND ur.AD_Role_ID=?"    //  #4
			+ " AND ur.AD_User_ID=u.AD_User_ID"
			+ " AND (o.AD_Client_ID = 0 OR o.AD_Client_ID=c.AD_Client_ID)"
			+ " AND c.AD_Client_ID IN (SELECT AD_Client_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)"
			+ " AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			pstmt.setInt(2, AD_Client_ID);
			pstmt.setInt(3, AD_Org_ID);
			pstmt.setInt(4, AD_Role_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				loginInfo = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("WMenu.checkLogin", e);
		}

		//  not verified
		if (loginInfo == null)
			return null;

		//  Set Preferences
		KeyNamePair org = new KeyNamePair(AD_Org_ID, String.valueOf(AD_Org_ID));
		KeyNamePair wh = null;
		if (M_Warehouse_ID > 0)
			wh = new KeyNamePair(M_Warehouse_ID, String.valueOf(M_Warehouse_ID));
		//
		Timestamp date = null;
		String printer = null;
		DB.loadPreferences(ctx, org, wh, date, printer);
		//
		return loginInfo;
	}   //  checkLogin


	/*************************************************************************/

	/**
	 *  Load Tree and return root node
	 *  @param AD_Client_ID client
	 *  @param AD_Role_ID role
	 *  @param AD_User_ID user
	 *  @param AD_Language language
	 *  @return node
	 */
	private MNode loadTree (int AD_Client_ID, int AD_Role_ID, int AD_User_ID, String AD_Language)
	{
		//	Get Tree info with start node
		int     AD_Tree_ID;
		String	Name;
		String	TreeType;
		String	Description;
		int 	startID;
		//
		MNode   root = null;

		//  Get Root Node
		String SQL = "SELECT t.AD_Tree_ID, t.Name, t.Description, t.TreeType, tn.Node_ID "
			+ "FROM AD_Tree t, AD_ClientInfo c, AD_TreeNode tn "
			+ "WHERE t.AD_Tree_ID=tn.AD_Tree_ID"
			+ " AND tn.Parent_ID IS NULL"
			+ " AND t.AD_Tree_ID=c.AD_Tree_Menu_ID"
			+ " AND c.AD_Client_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(SQL);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				return null;
			}
			AD_Tree_ID = rs.getInt(1);
			Name = rs.getString(2);
			Description = rs.getString(3);
			TreeType = rs.getString(4);
			startID = rs.getInt(5);

			//	create root node
			root = new MNode (startID, Name, Description, true, "");
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadTree -1", e);
			return null;
		}

		//  SQL for TreeNodes
		StringBuffer cmd = new StringBuffer("SELECT tn.Node_ID,tn.Parent_ID,tn.SeqNo, "
			+ "(SELECT 'Y' FROM AD_TreeBar tb WHERE tb.AD_Tree_ID=tn.AD_Tree_ID AND tb.AD_User_ID=")
			.append(AD_User_ID).append(" AND tb.Node_ID=tn.Node_ID) "
			+ "FROM AD_TreeNode tn ")
			.append("WHERE tn.IsActive='Y' "
			+ "START WITH Parent_ID IS NULL AND AD_Tree_ID=? "
			+ "CONNECT BY Parent_ID=PRIOR Node_ID AND AD_Tree_ID=? "
			+ "ORDER BY LEVEL, SeqNo");

		//  SQL for Node Info
		StringBuffer cmdNode = new StringBuffer();
		boolean base = Env.isBaseLanguage(Env.getCtx(), "AD_Menu");
		if (base)
			cmdNode.append("SELECT m.Name,m.Description,m.IsSummary,m.Action "
				+ "FROM AD_Menu m");
		else
			cmdNode.append("SELECT t.Name,t.Description,m.IsSummary,m.Action "
				+ "FROM AD_Menu m, AD_Menu_Trl t");
			cmdNode.append(", (SELECT ").append(AD_Role_ID).append(" AS XRole FROM DUAL)");
		cmdNode.append(" WHERE m.AD_Menu_ID=?");
		if (!base)
			cmdNode.append(" AND m.AD_Menu_ID=t.AD_Menu_ID AND t.AD_Language='")
				.append(Env.getAD_Language(Env.getCtx())).append("'");

		cmdNode.append(" AND m.IsActive='Y' "
			+ "AND (m.IsSummary='Y' OR m.Action='B'"
			+ " OR EXISTS (SELECT * FROM AD_Window_Access wa WHERE wa.AD_Window_ID=m.AD_Window_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Process_Access wa WHERE wa.AD_Process_ID=m.AD_Process_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Form_Access wa WHERE wa.AD_Form_ID=m.AD_Form_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Task_Access wa WHERE wa.AD_Task_ID=m.AD_Task_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Workflow_Access wa WHERE wa.AD_Workflow_ID=m.AD_Workflow_ID AND wa.AD_Role_ID=XRole)"
			+ ")");
	//	Log.trace(Log.l6_Database, "SQL Tree", cmd.toString());
	//	Log.trace(Log.l6_Database, "SQL Node", cmdNode.toString());

		//  The Node Loop
		try
		{
			PreparedStatement pstmtNode = DB.prepareStatement(cmdNode.toString());
			//
			PreparedStatement pstmt = DB.prepareStatement(cmd.toString());
			pstmt.setInt(1, AD_Tree_ID);
			pstmt.setInt(2, AD_Tree_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int Node_ID = rs.getInt(1);
				int Parent_ID = rs.getInt(2);
				int SeqNo = rs.getInt(3);
				boolean onBar = (rs.getString(4) != null);
				loadNode (pstmtNode, root, Node_ID, Parent_ID, SeqNo, onBar);
			}
			rs.close();
			pstmt.close();
			pstmtNode.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadTree -2", e);
		}

		//  Clean Menu tree
		root.clean(root);

		return root;
	}   //  loadTree

	/**
	 *  Load Node using prepared statement
	 *  @param  pstmt       Prepared Statement requiring to set Node_ID and returning
	 *      Name,Description,IsSummary,ImageIndiactor
	 *  @param  root		root node
	 *  @param  Node_ID     Key of the record
	 *  @param  Parent_ID   Parent ID of the record
	 *  @param  SeqNo       Sort index
	 *  @param  onBar       Node also on Shortcut bar
	 */
	private void loadNode (PreparedStatement pstmt, MNode root,
		int Node_ID, int Parent_ID, int SeqNo, boolean onBar)
	{
		try
		{
			pstmt.setInt(1, Node_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				String Name = rs.getString(1);
				String Description = rs.getString(2);
				boolean IsSummary = rs.getString(3).equals("Y");
				String ImageIndicator = rs.getString(4);

				if (Name != null)
				{
					MNode child = new MNode (Node_ID, Name, Description, IsSummary, ImageIndicator);
					child.add(root, Parent_ID, child);
				}
			}
		//	else
		//		Log.trace(Log.l6_Database,"Not added", "Node_ID=" + Node_ID);
			rs.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadNode", e);
		}
	}   //  loadNode


	/**************************************************************************
	 *  Web Menu Tree Node
	 */
	class MNode
	{
		/**
		 *  Constructor
		 *  @param ID id
		 *  @param Name name
		 *  @param Description description
		 *  @param isSummary summary
		 *  @param Type type
		 */
		public MNode (int ID, String Name, String Description, boolean isSummary, String Type)
		{
			this.ID = ID;
			this.Name = Name;
			this.Description = Description;
			if (this.Description == null)
				this.Description = "";
			this.isSummary = isSummary;
			this.Type = Type;
			if (this.Type == null)
				this.Type = "";
		}

		public int          ID;
		public String       Name;
		public String       Description;
		public boolean      isSummary;
		public String       Type;
		public ArrayList    children = new ArrayList();

		/**
		 *  Add to list of children
		 *  @param child child
		 *  @return true
		 */
		public boolean add (MNode child)
		{
			children.add(child);
			return true;
		}   //  add

		/*********************************************************************/

		/**
		 *  Traverse Tree starting at root to add child to parent with parentID
		 *  @param root root
		 *  @param parentID parent
		 *  @param child child
		 *  @returns    false if not added
		 */
		public boolean add (MNode root, int parentID, MNode child)
		{
			//  is this root the parent?
			if (root.ID == parentID)
				return root.add(child);

			//  do we have children to check?
			else if (root.children.size() == 0)
				return false;

			//  check children
			for (int i = 0; i < root.children.size(); i++)
			{
				MNode cc = (MNode)root.children.get(i);
				if (root.add(cc, parentID, child))
					return true;
			}
			//  nothing found
			return false;
		}   //  add

		/**
		 *  Traverse Tree and print it
		 *  @param root root
		 *  @param buf buffer
		 */
		public void print (MNode root, StringBuffer buf)
		{
			//  Leaf
			if (root.children.size() == 0)
			{
				/**
				 *  <li id="menuXXXXX"><a href="...." onMouseOver="status='Menu Description';return true;">Menu Entry</a></li>
				 */
				String item = "";
				String servletName = "";
				if (root.Type.equals("W"))
				{
					item = "menuWindow";
					servletName = "WWindow";
				}
				else if (root.Type.equals("X"))
				{
					item = "menuWindow";
					servletName = "WForm";
				}
				else if (root.Type.equals("R"))
				{
					item = "menuReport";
					servletName = "WReport";
				}
				else if (root.Type.equals("P"))
				{
					item = "menuProcess";
					servletName = "WProcess";
				}
				else if (root.Type.equals("F"))
				{
					item = "menuWorkflow";
					servletName = "WWorkflow";
				}
				else if (root.Type.equals("T"))
				{
					item = "menuProcess";
					servletName = "WTask";
				}
				else
					servletName = "WError";

				String description = root.Description.replace('\'',' ').replace('"',' ');
				buf.append("<li id=\"" + item + "\"><a href=\"");
				//  url -   /appl/servletName?AD_Menu_ID=x
				buf.append(WEnv.getBaseDirectory(servletName))
					.append("?AD_Menu_ID=")
					.append(root.ID);
				//
				buf.append("\" onMouseOver=\"status='" + description + "';return true;\" onclick=showLoadingWindow(\"" + WEnv.getBaseDirectory("") + "\")>");
				buf.append(root.Name);
				buf.append("</a></li>\n");
			}
			else
			{
				/**
				 *  <li id="foldHeader">MenuEntry</li>
				 *  <ul style="display:none">
				 *  ....
				 *  </ul>
				 */
				buf.append("\n<li id=\"menuHeader\">");         //  summary node
				buf.append(root.Name);
				buf.append("</li>\n");
				//  Next Level
				buf.append("<ul style=\"display:none\">\n");    //  start next level
				for (int i = 0; i < root.children.size(); i++)
				{
					MNode cc = (MNode)root.children.get(i);
					root.print(cc, buf);
				}
				buf.append("</ul>");                                            //  finish next level
			}
		}	//  print

		/**
		 *  Clean tree of parents without children
		 *  @param root root node
		 */
		public void clean (MNode root)
		{
			int size = root.children.size();
			if (size == 0)
				return;
			//
			ArrayList temp = new ArrayList(size);
			boolean changed = false;
			for (int i = 0; i < size; i++)
			{
				MNode cc = (MNode)root.children.get(i);
				int ccSize = cc.children.size();
				if (cc.isSummary && ccSize == 0)
					changed = true;
				else
					temp.add(cc);
				if (ccSize != 0)
					cc.clean(cc);
			}
			if (changed)
				root.children = temp;
		}   //  clean

	}   //  MNode

}   //  WMenu
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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;

import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Web Menu
 *
 *  @author Jorg Janke
 *  @version  $Id: WMenu.java,v 1.2 2003/03/31 05:27:26 jjanke Exp $
 */
public class WMenu extends HttpServlet
{
	/**
	 *  Initialize global variables
	 *  @param config config
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("WMenu.init");
	}   //  init

	/**
	 * 	Get Servlet information
	 *  @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Menu";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		Log.trace(Log.l1_User, "WMenu.destroy");
		super.destroy();
	}   //  destroy

	/** */
	private PreparedStatement	m_pstmt;


	/*************************************************************************/

	/**
	 *  Process the HTTP Get request.
	 *  - Exit (Logout)
	 *  - AD_Window_ID Forward to Window
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WMenu.doGet - Process Menu Request");
		//  Get Parameter: Exit
		if (request.getParameter("Exit") != null)
		{
			WUtil.createLoginPage (request, response, this, null, "Exit");
			return;
		}

		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		if (ctx == null)
		{
			WUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Window
		int AD_Window_ID = WUtil.getParameterAsInt(request, "AD_Window_ID");

		//  Forward to WWindow
		if (AD_Window_ID != 0)
		{
			Log.trace(Log.l4_Data, "AD_Window_ID=" + AD_Window_ID);
			//
			String url = WEnv.getBaseDirectory("WWindow?AD_Window_ID=" + AD_Window_ID);
			Log.trace(Log.l4_Data, "Forward to - " + url);
			//
			RequestDispatcher rd = getServletContext().getRequestDispatcher(url);
			rd.forward(request, response);
			return;
		}

		//  Request not serviceable
		WUtil.createErrorPage(request, response, this, null, "NotImplemented");
	}   //  doGet


	/*************************************************************************/

	/**
	 *  Process the HTTP Post request - Verify Input & Create Menu
	 *
	 * @param request request
	 * @param response response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WMenu.doPost - Create Menu");
		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		Properties cProp = WUtil.getCookieProprties(request);
		if (ctx == null)
		{
			WUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Get Parameters: Role, Client, Org, Warehouse, Date
		String role = request.getParameter(WLogin.P_ROLE);
		String client = request.getParameter(WLogin.P_CLIENT);
		String org = request.getParameter(WLogin.P_ORG);
		String wh = request.getParameter(WLogin.P_WAREHOUSE);
		if (wh == null)
			wh = "";

		//  Get context
		if (role == null || client == null || org == null)
		{
			WUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Get Info from Context - User, Role, Client
		int AD_User_ID = Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID = Env.getContextAsInt(ctx, "#AD_Role_ID");
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		//  Not available in context yet - Org, Warehouse
		int AD_Org_ID = -1;
		int M_Warehouse_ID = -1;

		//  Get latest info from context
		try
		{
			int req_role = Integer.parseInt(role);
			if (req_role != AD_Role_ID)
			{
				Log.trace(Log.l5_DData, "AD_Role_ID - changed from " + AD_Role_ID);
				AD_Role_ID = req_role;
				Env.setContext(ctx, "#AD_Role_ID", AD_Role_ID);
			}
			Log.trace(Log.l5_DData, "AD_Role_ID = " + AD_Role_ID);
			//
			int req_client = Integer.parseInt(client);
			if (req_client != AD_Client_ID)
			{
				Log.trace(Log.l5_DData, "AD_Client_ID - changed from " + AD_Client_ID);
				AD_Client_ID = req_client;
				Env.setContext(ctx, "#AD_Client_ID", AD_Client_ID);
			}
			Log.trace(Log.l5_DData, "AD_Client_ID = " + AD_Client_ID);
			//
			AD_Org_ID = Integer.parseInt(org);
			Log.trace(Log.l5_DData, "AD_Org_ID = " + AD_Org_ID);
			//
			if (wh.length() > 0)
			{
				M_Warehouse_ID = Integer.parseInt(wh);
				Log.trace(Log.l5_DData, "M_Warehouse_ID = " + M_Warehouse_ID);
			}
		}
		catch (Exception e)
		{
			Log.error("WMenu.doPost - Parameter", e);
			WUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Check Login info and set environment
		String loginInfo = checkLogin (ctx, AD_User_ID, AD_Role_ID, AD_Client_ID, AD_Org_ID, M_Warehouse_ID);
		if (loginInfo == null)
		{
			WUtil.createErrorPage(request, response, this, ctx, Msg.getMsg(ctx, "RoleInconsistent"));
			return;
		}
		sess.setAttribute(WEnv.SA_LOGININFO, loginInfo);

		//  Set cookie for future defaults
		cProp.setProperty(WLogin.P_ROLE, String.valueOf(AD_Role_ID));
		cProp.setProperty(WLogin.P_CLIENT, String.valueOf(AD_Client_ID));
		cProp.setProperty(WLogin.P_ORG, String.valueOf(AD_Org_ID));
		if (M_Warehouse_ID == -1)
			cProp.setProperty(WLogin.P_WAREHOUSE, "");
		else
			cProp.setProperty(WLogin.P_WAREHOUSE, String.valueOf(M_Warehouse_ID));

		//  Set Date
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		//  Try to parse Date
		String dateString = request.getParameter(WLogin.P_DATE);
		try
		{
			if (dateString != null && dateString.length() > 0)
			{
				Language language = (Language)sess.getAttribute(WEnv.SA_LANGUAGE);
				DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
				java.util.Date date = df.parse(dateString);
				ts = new Timestamp(date.getTime());
			}
		}
		catch (Exception e)
		{
			Log.trace(Log.l1_User, "WMenu.doPost - Cannot parse date: " + dateString + " - " + e.getMessage());
		}
		Env.setContext(ctx, "#Date", ts.toString());    //  JDBC format


	//	Log.printProperties(System.getProperties(), "System");
	//	Log.printProperties(cProp, "Cookie");
	//	Log.printProperties(ctx, "Servlet Context");
	//	Log.printProperties(Env.getCtx(), "Apps Env Context");


		/**********************************************************************
		 *  Create Menu output
		 */
		String windowTitle = Msg.getMsg(ctx, "Menu");
		String statusMessage = Msg.getMsg(ctx, "SelectMenuItem");

		//	Document
		WDoc doc = WDoc.create (windowTitle);
		head header = doc.getHead();
		//  Target
		header.addElement(new base().setTarget(WEnv.TARGET_WINDOW));
		//  Specific Menu Script/Stylesheet
		header.addElement(new script("", WEnv.getBaseDirectory("menu.js"), "text/javascript", "JavaScript1.2"));
		header.addElement(new link().setRel("stylesheet").setHref(WEnv.getBaseDirectory("menu.css")));

		//	Body
		body body = doc.getBody();
		//  Header
		body.addElement(new cite(loginInfo));
		String title = windowTitle + " - " + loginInfo;
		body.addElement(new script("top.document.title='" + title + "';"));
		body.addElement(new script("defaultStatus = '" + statusMessage + "';"));

		//  Clear Window Frame
		body.addElement(WUtil.getClearFrame(WEnv.TARGET_WINDOW));

		//  Load Menu Structure     ----------------------
		MNode root = loadTree(AD_Client_ID, AD_Role_ID, AD_User_ID, Env.getAD_Language(ctx));
		//
		StringBuffer buf = new StringBuffer();
		buf.append("<ul>");
		for (int i = 0; i < root.children.size(); i++)
		{
			MNode node = (MNode)root.children.get(i);
			node.print(node, buf);
		}
		buf.append("</ul>");
		body.addElement(buf.toString());

		//  Exit Info
		body.addElement(new hr());
		String url = request.getRequestURI() + "?Exit=true";
		body.addElement(new a(url, Msg.getMsg(ctx, "Exit")));

		//  Can we store Cookie ?
		if (!cProp.getProperty(WLogin.P_STORE, "N").equals("Y"))
			cProp.clear();

		WUtil.createResponse (request, response, this, cProp, doc, false);
	}   //  doPost

	/**
	 *  Check Login information and set context.
	 *  @returns    true if login info are OK
	 *  @param ctx context
	 *  @param AD_User_ID user
	 *  @param AD_Role_ID role
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param M_Warehouse_ID warehouse
	 */
	private String checkLogin (Properties ctx, int AD_User_ID, int AD_Role_ID, int AD_Client_ID, int AD_Org_ID, int M_Warehouse_ID)
	{
		//  Get Login Info
		String loginInfo = null;
		//  Verify existance of User/Client/Org/Role and User's acces to Client & Org
		String sql = "SELECT u.Name || '@' || c.Name || '.' || o.Name || ' [' || INITCAP(USER) || ']' AS Text "
			+ "FROM AD_User u, AD_Client c, AD_Org o, AD_User_Roles ur "
			+ "WHERE u.AD_User_ID=?"    //  #1
			+ " AND c.AD_Client_ID=?"   //  #2
			+ " AND o.AD_Org_ID=?"      //  #3
			+ " AND ur.AD_Role_ID=?"    //  #4
			+ " AND ur.AD_User_ID=u.AD_User_ID"
			+ " AND (o.AD_Client_ID = 0 OR o.AD_Client_ID=c.AD_Client_ID)"
			+ " AND c.AD_Client_ID IN (SELECT AD_Client_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)"
			+ " AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			pstmt.setInt(2, AD_Client_ID);
			pstmt.setInt(3, AD_Org_ID);
			pstmt.setInt(4, AD_Role_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				loginInfo = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("WMenu.checkLogin", e);
		}

		//  not verified
		if (loginInfo == null)
			return null;

		//  Set Preferences
		KeyNamePair org = new KeyNamePair(AD_Org_ID, String.valueOf(AD_Org_ID));
		KeyNamePair wh = null;
		if (M_Warehouse_ID > 0)
			wh = new KeyNamePair(M_Warehouse_ID, String.valueOf(M_Warehouse_ID));
		//
		Timestamp date = null;
		String printer = null;
		DB.loadPreferences(ctx, org, wh, date, printer);
		//
		return loginInfo;
	}   //  checkLogin


	/*************************************************************************/

	/**
	 *  Load Tree and return root node
	 *  @param AD_Client_ID client
	 *  @param AD_Role_ID role
	 *  @param AD_User_ID user
	 *  @param AD_Language language
	 *  @return node
	 */
	private MNode loadTree (int AD_Client_ID, int AD_Role_ID, int AD_User_ID, String AD_Language)
	{
		//	Get Tree info with start node
		int     AD_Tree_ID;
		String	Name;
		String	TreeType;
		String	Description;
		int 	startID;
		//
		MNode   root = null;

		//  Get Root Node
		String SQL = "SELECT t.AD_Tree_ID, t.Name, t.Description, t.TreeType, tn.Node_ID "
			+ "FROM AD_Tree t, AD_ClientInfo c, AD_TreeNode tn "
			+ "WHERE t.AD_Tree_ID=tn.AD_Tree_ID"
			+ " AND tn.Parent_ID IS NULL"
			+ " AND t.AD_Tree_ID=c.AD_Tree_Menu_ID"
			+ " AND c.AD_Client_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(SQL);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				return null;
			}
			AD_Tree_ID = rs.getInt(1);
			Name = rs.getString(2);
			Description = rs.getString(3);
			TreeType = rs.getString(4);
			startID = rs.getInt(5);

			//	create root node
			root = new MNode (startID, Name, Description, true, "");
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadTree -1", e);
			return null;
		}

		//  SQL for TreeNodes
		StringBuffer cmd = new StringBuffer("SELECT tn.Node_ID,tn.Parent_ID,tn.SeqNo, "
			+ "(SELECT 'Y' FROM AD_TreeBar tb WHERE tb.AD_Tree_ID=tn.AD_Tree_ID AND tb.AD_User_ID=")
			.append(AD_User_ID).append(" AND tb.Node_ID=tn.Node_ID) "
			+ "FROM AD_TreeNode tn ")
			.append("WHERE tn.IsActive='Y' "
			+ "START WITH Parent_ID IS NULL AND AD_Tree_ID=? "
			+ "CONNECT BY Parent_ID=PRIOR Node_ID AND AD_Tree_ID=? "
			+ "ORDER BY LEVEL, SeqNo");

		//  SQL for Node Info
		StringBuffer cmdNode = new StringBuffer();
		boolean base = Env.isBaseLanguage(Env.getCtx(), "AD_Menu");
		if (base)
			cmdNode.append("SELECT m.Name,m.Description,m.IsSummary,m.Action "
				+ "FROM AD_Menu m");
		else
			cmdNode.append("SELECT t.Name,t.Description,m.IsSummary,m.Action "
				+ "FROM AD_Menu m, AD_Menu_Trl t");
			cmdNode.append(", (SELECT ").append(AD_Role_ID).append(" AS XRole FROM DUAL)");
		cmdNode.append(" WHERE m.AD_Menu_ID=?");
		if (!base)
			cmdNode.append(" AND m.AD_Menu_ID=t.AD_Menu_ID AND t.AD_Language='")
				.append(Env.getAD_Language(Env.getCtx())).append("'");

		cmdNode.append(" AND m.IsActive='Y' "
			+ "AND (m.IsSummary='Y' OR m.Action='B'"
			+ " OR EXISTS (SELECT * FROM AD_Window_Access wa WHERE wa.AD_Window_ID=m.AD_Window_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Process_Access wa WHERE wa.AD_Process_ID=m.AD_Process_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Form_Access wa WHERE wa.AD_Form_ID=m.AD_Form_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Task_Access wa WHERE wa.AD_Task_ID=m.AD_Task_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Workflow_Access wa WHERE wa.AD_Workflow_ID=m.AD_Workflow_ID AND wa.AD_Role_ID=XRole)"
			+ ")");
	//	Log.trace(Log.l6_Database, "SQL Tree", cmd.toString());
	//	Log.trace(Log.l6_Database, "SQL Node", cmdNode.toString());

		//  The Node Loop
		try
		{
			PreparedStatement pstmtNode = DB.prepareStatement(cmdNode.toString());
			//
			PreparedStatement pstmt = DB.prepareStatement(cmd.toString());
			pstmt.setInt(1, AD_Tree_ID);
			pstmt.setInt(2, AD_Tree_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int Node_ID = rs.getInt(1);
				int Parent_ID = rs.getInt(2);
				int SeqNo = rs.getInt(3);
				boolean onBar = (rs.getString(4) != null);
				loadNode (pstmtNode, root, Node_ID, Parent_ID, SeqNo, onBar);
			}
			rs.close();
			pstmt.close();
			pstmtNode.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadTree -2", e);
		}

		//  Clean Menu tree
		root.clean(root);

		return root;
	}   //  loadTree

	/**
	 *  Load Node using prepared statement
	 *  @param  pstmt       Prepared Statement requiring to set Node_ID and returning
	 *      Name,Description,IsSummary,ImageIndiactor
	 *  @param  root		root node
	 *  @param  Node_ID     Key of the record
	 *  @param  Parent_ID   Parent ID of the record
	 *  @param  SeqNo       Sort index
	 *  @param  onBar       Node also on Shortcut bar
	 */
	private void loadNode (PreparedStatement pstmt, MNode root,
		int Node_ID, int Parent_ID, int SeqNo, boolean onBar)
	{
		try
		{
			pstmt.setInt(1, Node_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				String Name = rs.getString(1);
				String Description = rs.getString(2);
				boolean IsSummary = rs.getString(3).equals("Y");
				String ImageIndicator = rs.getString(4);

				if (Name != null)
				{
					MNode child = new MNode (Node_ID, Name, Description, IsSummary, ImageIndicator);
					child.add(root, Parent_ID, child);
				}
			}
		//	else
		//		Log.trace(Log.l6_Database,"Not added", "Node_ID=" + Node_ID);
			rs.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadNode", e);
		}
	}   //  loadNode


	/**************************************************************************
	 *  Web Menu Tree Node
	 */
	class MNode
	{
		/**
		 *  Constructor
		 *  @param ID id
		 *  @param Name name
		 *  @param Description description
		 *  @param isSummary summary
		 *  @param Type type
		 */
		public MNode (int ID, String Name, String Description, boolean isSummary, String Type)
		{
			this.ID = ID;
			this.Name = Name;
			this.Description = Description;
			if (this.Description == null)
				this.Description = "";
			this.isSummary = isSummary;
			this.Type = Type;
			if (this.Type == null)
				this.Type = "";
		}

		public int          ID;
		public String       Name;
		public String       Description;
		public boolean      isSummary;
		public String       Type;
		public ArrayList    children = new ArrayList();

		/**
		 *  Add to list of children
		 *  @param child child
		 *  @return true
		 */
		public boolean add (MNode child)
		{
			children.add(child);
			return true;
		}   //  add

		/*********************************************************************/

		/**
		 *  Traverse Tree starting at root to add child to parent with parentID
		 *  @param root root
		 *  @param parentID parent
		 *  @param child child
		 *  @returns    false if not added
		 */
		public boolean add (MNode root, int parentID, MNode child)
		{
			//  is this root the parent?
			if (root.ID == parentID)
				return root.add(child);

			//  do we have children to check?
			else if (root.children.size() == 0)
				return false;

			//  check children
			for (int i = 0; i < root.children.size(); i++)
			{
				MNode cc = (MNode)root.children.get(i);
				if (root.add(cc, parentID, child))
					return true;
			}
			//  nothing found
			return false;
		}   //  add

		/**
		 *  Traverse Tree and print it
		 *  @param root root
		 *  @param buf buffer
		 */
		public void print (MNode root, StringBuffer buf)
		{
			//  Leaf
			if (root.children.size() == 0)
			{
				/**
				 *  <li id="menuXXXXX"><a href="...." onMouseOver="status='Menu Description';return true;">Menu Entry</a></li>
				 */
				String item = "";
				String servletName = "";
				if (root.Type.equals("W"))
				{
					item = "menuWindow";
					servletName = "WWindow";
				}
				else if (root.Type.equals("X"))
				{
					item = "menuWindow";
					servletName = "WForm";
				}
				else if (root.Type.equals("R"))
				{
					item = "menuReport";
					servletName = "WReport";
				}
				else if (root.Type.equals("P"))
				{
					item = "menuProcess";
					servletName = "WProcess";
				}
				else if (root.Type.equals("F"))
				{
					item = "menuWorkflow";
					servletName = "WWorkflow";
				}
				else if (root.Type.equals("T"))
				{
					item = "menuProcess";
					servletName = "WTask";
				}
				else
					servletName = "WError";

				String description = root.Description.replace('\'',' ').replace('"',' ');
				buf.append("<li id=\"" + item + "\"><a href=\"");
				//  url -   /appl/servletName?AD_Menu_ID=x
				buf.append(WEnv.getBaseDirectory(servletName))
					.append("?AD_Menu_ID=")
					.append(root.ID);
				//
				buf.append("\" onMouseOver=\"status='" + description + "';return true;\" onclick=showLoadingWindow(\"" + WEnv.getBaseDirectory("") + "\")>");
				buf.append(root.Name);
				buf.append("</a></li>\n");
			}
			else
			{
				/**
				 *  <li id="foldHeader">MenuEntry</li>
				 *  <ul style="display:none">
				 *  ....
				 *  </ul>
				 */
				buf.append("\n<li id=\"menuHeader\">");         //  summary node
				buf.append(root.Name);
				buf.append("</li>\n");
				//  Next Level
				buf.append("<ul style=\"display:none\">\n");    //  start next level
				for (int i = 0; i < root.children.size(); i++)
				{
					MNode cc = (MNode)root.children.get(i);
					root.print(cc, buf);
				}
				buf.append("</ul>");                                            //  finish next level
			}
		}	//  print

		/**
		 *  Clean tree of parents without children
		 *  @param root root node
		 */
		public void clean (MNode root)
		{
			int size = root.children.size();
			if (size == 0)
				return;
			//
			ArrayList temp = new ArrayList(size);
			boolean changed = false;
			for (int i = 0; i < size; i++)
			{
				MNode cc = (MNode)root.children.get(i);
				int ccSize = cc.children.size();
				if (cc.isSummary && ccSize == 0)
					changed = true;
				else
					temp.add(cc);
				if (ccSize != 0)
					cc.clean(cc);
			}
			if (changed)
				root.children = temp;
		}   //  clean

	}   //  MNode

}   //  WMenu
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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;

import org.apache.ecs.xhtml.*;

import org.compiere.model.*;
import org.compiere.util.*;

/**
 *  Web Menu
 *
 *  @author Jorg Janke
 *  @version  $Id: WMenu.java,v 1.6 2004/09/10 02:54:23 jjanke Exp $
 */
public class WMenu extends HttpServlet
{
	/**	Logger			*/
	protected Logger	log = Logger.getCLogger(getClass());
	
	/**
	 *  Initialize global variables
	 *  @param config config
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WebEnv.initWeb(config))
			throw new ServletException("WMenu.init");
	}   //  init

	/**
	 * 	Get Servlet information
	 *  @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Menu";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
		super.destroy();
	}   //  destroy

	/** */
	private PreparedStatement	m_pstmt;


	
	/**************************************************************************
	 *  Process the HTTP Get request.
	 *  - Exit (Logout)
	 *  - AD_Window_ID Forward to Window
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.debug("doGet - Process Menu Request");
		//  Get Parameter: Exit
		if (request.getParameter("Exit") != null)
		{
			WebUtil.createLoginPage (request, response, this, null, "Exit");
			return;
		}

		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WebEnv.SA_CONTEXT);
		if (ctx == null)
		{
			WebUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Window
		int AD_Window_ID = WebUtil.getParameterAsInt(request, "AD_Window_ID");

		//  Forward to WWindow
		if (AD_Window_ID != 0)
		{
			log.debug("doGet - AD_Window_ID=" + AD_Window_ID);
			//
			String url = WebEnv.getBaseDirectory("WWindow?AD_Window_ID=" + AD_Window_ID);
			log.debug("doGet - Forward to=" + url);
			//
			RequestDispatcher rd = getServletContext().getRequestDispatcher(url);
			rd.forward(request, response);
			return;
		}

		//  Request not serviceable
		WebUtil.createErrorPage(request, response, this, null, "NotImplemented");
	}   //  doGet


	/**************************************************************************
	 *  Process the HTTP Post request - Verify Input & Create Menu
	 *
	 * @param request request
	 * @param response response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.debug("doPost - Create Menu");
		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WebEnv.SA_CONTEXT);
		Properties cProp = WebUtil.getCookieProprties(request);
		if (ctx == null)
		{
			WebUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Get Parameters: Role, Client, Org, Warehouse, Date
		String role = request.getParameter(WLogin.P_ROLE);
		String client = request.getParameter(WLogin.P_CLIENT);
		String org = request.getParameter(WLogin.P_ORG);
		String wh = request.getParameter(WLogin.P_WAREHOUSE);
		if (wh == null)
			wh = "";

		//  Get context
		if (role == null || client == null || org == null)
		{
			WebUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Get Info from Context - User, Role, Client
		int AD_User_ID = Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID = Env.getContextAsInt(ctx, "#AD_Role_ID");
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		//  Not available in context yet - Org, Warehouse
		int AD_Org_ID = -1;
		int M_Warehouse_ID = -1;

		//  Get latest info from context
		try
		{
			int req_role = Integer.parseInt(role);
			if (req_role != AD_Role_ID)
			{
				log.debug("doPost - AD_Role_ID - changed from " + AD_Role_ID);
				AD_Role_ID = req_role;
				Env.setContext(ctx, "#AD_Role_ID", AD_Role_ID);
			}
			log.debug("doPost - AD_Role_ID = " + AD_Role_ID);
			//
			int req_client = Integer.parseInt(client);
			if (req_client != AD_Client_ID)
			{
				log.debug("doPost - AD_Client_ID - changed from " + AD_Client_ID);
				AD_Client_ID = req_client;
				Env.setContext(ctx, "#AD_Client_ID", AD_Client_ID);
			}
			log.debug("doPost - AD_Client_ID = " + AD_Client_ID);
			//
			AD_Org_ID = Integer.parseInt(org);
			log.debug("doPost - AD_Org_ID = " + AD_Org_ID);
			//
			if (wh.length() > 0)
			{
				M_Warehouse_ID = Integer.parseInt(wh);
				log.debug("doPost - M_Warehouse_ID = " + M_Warehouse_ID);
			}
		}
		catch (Exception e)
		{
			log.error("doPost - Parameter", e);
			WebUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Check Login info and set environment
		String loginInfo = checkLogin (ctx, AD_User_ID, AD_Role_ID, AD_Client_ID, AD_Org_ID, M_Warehouse_ID);
		if (loginInfo == null)
		{
			WebUtil.createErrorPage(request, response, this, ctx, Msg.getMsg(ctx, "RoleInconsistent"));
			return;
		}
		sess.setAttribute(WebEnv.SA_LOGININFO, loginInfo);

		//  Set cookie for future defaults
		cProp.setProperty(WLogin.P_ROLE, String.valueOf(AD_Role_ID));
		cProp.setProperty(WLogin.P_CLIENT, String.valueOf(AD_Client_ID));
		cProp.setProperty(WLogin.P_ORG, String.valueOf(AD_Org_ID));
		if (M_Warehouse_ID == -1)
			cProp.setProperty(WLogin.P_WAREHOUSE, "");
		else
			cProp.setProperty(WLogin.P_WAREHOUSE, String.valueOf(M_Warehouse_ID));

		//  Set Date
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		//  Try to parse Date
		String dateString = request.getParameter(WLogin.P_DATE);
		try
		{
			if (dateString != null && dateString.length() > 0)
			{
				Language language = (Language)sess.getAttribute(WebEnv.SA_LANGUAGE);
				DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
				java.util.Date date = df.parse(dateString);
				ts = new Timestamp(date.getTime());
			}
		}
		catch (Exception e)
		{
			log.warn("doPost - Cannot parse date: " + dateString + " - " + e.getMessage());
		}
		Env.setContext(ctx, "#Date", ts.toString());    //  JDBC format


	//	log.printProperties(System.getProperties(), "System");
	//	log.printProperties(cProp, "Cookie");
	//	log.printProperties(ctx, "Servlet Context");
	//	log.printProperties(Env.getCtx(), "Apps Env Context");


		/**********************************************************************
		 *  Create Menu output
		 */
		String windowTitle = Msg.getMsg(ctx, "Menu");
		String statusMessage = Msg.getMsg(ctx, "SelectMenuItem");

		//	Document
		WebDoc doc = WebDoc.create (windowTitle);
		head header = doc.getHead();
		//  Target
		header.addElement(new base().setTarget(WebEnv.TARGET_WINDOW));
		//  Specific Menu Script/Stylesheet
		header.addElement(new script("", WebEnv.getBaseDirectory("menu.js"), "text/javascript", "JavaScript1.2"));
		header.addElement(new link().setRel("stylesheet").setHref(WebEnv.getBaseDirectory("menu.css")));

		//	Body
		body body = doc.getBody();
		//  Header
		body.addElement(new cite(loginInfo));
		String title = windowTitle + " - " + loginInfo;
		body.addElement(new script("top.document.title='" + title + "';"));
		body.addElement(new script("defaultStatus = '" + statusMessage + "';"));

		//  Clear Window Frame
		body.addElement(WebUtil.getClearFrame(WebEnv.TARGET_WINDOW));

		//  Load Menu Structure     ----------------------
		int AD_Tree_ID = 10;	//	Menu
		MTree tree = new MTree (ctx, AD_Tree_ID, false);	// Language set in WLogin
		StringBuffer buf = new StringBuffer();
		Enumeration en = tree.getRoot().preorderEnumeration();
		//
		int oldLevel = 0;
		while (en.hasMoreElements())
		{
			MTreeNode nd = (MTreeNode)en.nextElement();
			if (nd.isTask() || nd.isForm() || nd.isWorkbench() || nd.isWorkFlow()
				|| nd.isProcess() || nd.isReport())
				continue;

			//  Level
			int level = nd.getLevel();	//	0 == root
			if (level == 0)
				continue;
			//
			while (oldLevel < level)
			{
				if (level > 1)
					buf.append("<ul style=\"display:none\">\n");//  start next level
				else
					buf.append("<ul>\n");						//  start next level
				oldLevel++;
			}
			while (oldLevel > level)
			{
				buf.append("</ul>");                            //  finish next level
				oldLevel--;
			}
				
			//	Print Node
			buf.append(printNode(nd));
		}
		while (oldLevel >  0)
		{
			buf.append("</ul>");	                            //  finish next level
			oldLevel--;
		}
		body.addElement(buf.toString());

		//  Exit Info
		body.addElement(new hr());
		String url = request.getRequestURI() + "?Exit=true";
		body.addElement(new a(url, Msg.getMsg(ctx, "Exit")));
//System.out.println(doc);

		//  Can we store Cookie ?
		if (!cProp.getProperty(WLogin.P_STORE, "N").equals("Y"))
			cProp.clear();

		WebUtil.createResponse (request, response, this, cProp, doc, false);
	}   //  doPost

	/**
	 *  Check Login information and set context.
	 *  @returns    true if login info are OK
	 *  @param ctx context
	 *  @param AD_User_ID user
	 *  @param AD_Role_ID role
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param M_Warehouse_ID warehouse
	 */
	private String checkLogin (Properties ctx, int AD_User_ID, int AD_Role_ID, int AD_Client_ID, int AD_Org_ID, int M_Warehouse_ID)
	{
		//  Get Login Info
		String loginInfo = null;
		//  Verify existance of User/Client/Org/Role and User's acces to Client & Org
		String sql = "SELECT u.Name || '@' || c.Name || '.' || o.Name || ' [' || INITCAP(USER) || ']' AS Text "
			+ "FROM AD_User u, AD_Client c, AD_Org o, AD_User_Roles ur "
			+ "WHERE u.AD_User_ID=?"    //  #1
			+ " AND c.AD_Client_ID=?"   //  #2
			+ " AND o.AD_Org_ID=?"      //  #3
			+ " AND ur.AD_Role_ID=?"    //  #4
			+ " AND ur.AD_User_ID=u.AD_User_ID"
			+ " AND (o.AD_Client_ID = 0 OR o.AD_Client_ID=c.AD_Client_ID)"
			+ " AND c.AD_Client_ID IN (SELECT AD_Client_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)"
			+ " AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			pstmt.setInt(2, AD_Client_ID);
			pstmt.setInt(3, AD_Org_ID);
			pstmt.setInt(4, AD_Role_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				loginInfo = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.error ("checkLogin", e);
		}

		//  not verified
		if (loginInfo == null)
			return null;

		//  Set Preferences
		KeyNamePair org = new KeyNamePair(AD_Org_ID, String.valueOf(AD_Org_ID));
		KeyNamePair wh = null;
		if (M_Warehouse_ID > 0)
			wh = new KeyNamePair(M_Warehouse_ID, String.valueOf(M_Warehouse_ID));
		//
		Timestamp date = null;
		String printer = null;
		DB.loadPreferences(ctx, org, wh, date, printer);
		//
		return loginInfo;
	}   //  checkLogin


	/**
	 *  Print Menu Item
	 *  @param node node
	 */
	public StringBuffer printNode (MTreeNode node)
	{
		StringBuffer sb = new StringBuffer();
		
		//  Leaf
		if (!node.isSummary())
		{
			/**
			 *  <li id="menuXXXXX"><a href="...." onMouseOver="status='Menu Description';return true;">Menu Entry</a></li>
			 */
			String item = "";
			String servletName = "";
			if (node.isWindow())
			{
				item = "menuWindow";
				servletName = "WWindow";
			}
			else if (node.isForm())
			{
				item = "menuWindow";
				servletName = "WForm";
			}
			else if (node.isReport())
			{
				item = "menuReport";
				servletName = "WReport";
			}
			else if (node.isProcess())
			{
				item = "menuProcess";
				servletName = "WProcess";
			}
			else if (node.isWorkFlow())
			{
				item = "menuWorkflow";
				servletName = "WWorkflow";
			}
			else if (node.isTask())
			{
				item = "menuProcess";
				servletName = "WTask";
			}
			else
				servletName = "WError";

			String description = node.getDescription().replace('\'',' ').replace('"',' ');
			sb.append("<li id=\"" + item + "\"><a href=\"")
				//  url -   /appl/servletName?AD_Menu_ID=x
				.append(WebEnv.getBaseDirectory(servletName))
				.append("?AD_Menu_ID=")
				.append(node.getNode_ID())
				//
				.append("\" onMouseOver=\"status='" + description + "';return true;\" onclick=showLoadingWindow(\"" + WebEnv.getBaseDirectory("") + "\")>")
				.append(node.getName())		//	language set in MTree.getNodeDetails based on ctx
				.append("</a></li>\n");
		}
		else
		{
			/**
			 *  <li id="foldHeader">MenuEntry</li>
			 *  <ul style="display:none">
			 *  ....
			 *  </ul>
			 */
			sb.append("\n<li id=\"menuHeader\">")		//  summary node
				.append(node.getName())
				.append("</li>\n");
		}
		return sb;
	}	//  printNode


}   //  WMenu
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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;

import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Web Menu
 *
 *  @author Jorg Janke
 *  @version  $Id: WMenu.java,v 1.2 2003/03/31 05:27:26 jjanke Exp $
 */
public class WMenu extends HttpServlet
{
	/**
	 *  Initialize global variables
	 *  @param config config
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("WMenu.init");
	}   //  init

	/**
	 * 	Get Servlet information
	 *  @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Menu";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		Log.trace(Log.l1_User, "WMenu.destroy");
		super.destroy();
	}   //  destroy

	/** */
	private PreparedStatement	m_pstmt;


	/*************************************************************************/

	/**
	 *  Process the HTTP Get request.
	 *  - Exit (Logout)
	 *  - AD_Window_ID Forward to Window
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WMenu.doGet - Process Menu Request");
		//  Get Parameter: Exit
		if (request.getParameter("Exit") != null)
		{
			WUtil.createLoginPage (request, response, this, null, "Exit");
			return;
		}

		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		if (ctx == null)
		{
			WUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Window
		int AD_Window_ID = WUtil.getParameterAsInt(request, "AD_Window_ID");

		//  Forward to WWindow
		if (AD_Window_ID != 0)
		{
			Log.trace(Log.l4_Data, "AD_Window_ID=" + AD_Window_ID);
			//
			String url = WEnv.getBaseDirectory("WWindow?AD_Window_ID=" + AD_Window_ID);
			Log.trace(Log.l4_Data, "Forward to - " + url);
			//
			RequestDispatcher rd = getServletContext().getRequestDispatcher(url);
			rd.forward(request, response);
			return;
		}

		//  Request not serviceable
		WUtil.createErrorPage(request, response, this, null, "NotImplemented");
	}   //  doGet


	/*************************************************************************/

	/**
	 *  Process the HTTP Post request - Verify Input & Create Menu
	 *
	 * @param request request
	 * @param response response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WMenu.doPost - Create Menu");
		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		Properties cProp = WUtil.getCookieProprties(request);
		if (ctx == null)
		{
			WUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Get Parameters: Role, Client, Org, Warehouse, Date
		String role = request.getParameter(WLogin.P_ROLE);
		String client = request.getParameter(WLogin.P_CLIENT);
		String org = request.getParameter(WLogin.P_ORG);
		String wh = request.getParameter(WLogin.P_WAREHOUSE);
		if (wh == null)
			wh = "";

		//  Get context
		if (role == null || client == null || org == null)
		{
			WUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Get Info from Context - User, Role, Client
		int AD_User_ID = Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID = Env.getContextAsInt(ctx, "#AD_Role_ID");
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		//  Not available in context yet - Org, Warehouse
		int AD_Org_ID = -1;
		int M_Warehouse_ID = -1;

		//  Get latest info from context
		try
		{
			int req_role = Integer.parseInt(role);
			if (req_role != AD_Role_ID)
			{
				Log.trace(Log.l5_DData, "AD_Role_ID - changed from " + AD_Role_ID);
				AD_Role_ID = req_role;
				Env.setContext(ctx, "#AD_Role_ID", AD_Role_ID);
			}
			Log.trace(Log.l5_DData, "AD_Role_ID = " + AD_Role_ID);
			//
			int req_client = Integer.parseInt(client);
			if (req_client != AD_Client_ID)
			{
				Log.trace(Log.l5_DData, "AD_Client_ID - changed from " + AD_Client_ID);
				AD_Client_ID = req_client;
				Env.setContext(ctx, "#AD_Client_ID", AD_Client_ID);
			}
			Log.trace(Log.l5_DData, "AD_Client_ID = " + AD_Client_ID);
			//
			AD_Org_ID = Integer.parseInt(org);
			Log.trace(Log.l5_DData, "AD_Org_ID = " + AD_Org_ID);
			//
			if (wh.length() > 0)
			{
				M_Warehouse_ID = Integer.parseInt(wh);
				Log.trace(Log.l5_DData, "M_Warehouse_ID = " + M_Warehouse_ID);
			}
		}
		catch (Exception e)
		{
			Log.error("WMenu.doPost - Parameter", e);
			WUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Check Login info and set environment
		String loginInfo = checkLogin (ctx, AD_User_ID, AD_Role_ID, AD_Client_ID, AD_Org_ID, M_Warehouse_ID);
		if (loginInfo == null)
		{
			WUtil.createErrorPage(request, response, this, ctx, Msg.getMsg(ctx, "RoleInconsistent"));
			return;
		}
		sess.setAttribute(WEnv.SA_LOGININFO, loginInfo);

		//  Set cookie for future defaults
		cProp.setProperty(WLogin.P_ROLE, String.valueOf(AD_Role_ID));
		cProp.setProperty(WLogin.P_CLIENT, String.valueOf(AD_Client_ID));
		cProp.setProperty(WLogin.P_ORG, String.valueOf(AD_Org_ID));
		if (M_Warehouse_ID == -1)
			cProp.setProperty(WLogin.P_WAREHOUSE, "");
		else
			cProp.setProperty(WLogin.P_WAREHOUSE, String.valueOf(M_Warehouse_ID));

		//  Set Date
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		//  Try to parse Date
		String dateString = request.getParameter(WLogin.P_DATE);
		try
		{
			if (dateString != null && dateString.length() > 0)
			{
				Language language = (Language)sess.getAttribute(WEnv.SA_LANGUAGE);
				DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
				java.util.Date date = df.parse(dateString);
				ts = new Timestamp(date.getTime());
			}
		}
		catch (Exception e)
		{
			Log.trace(Log.l1_User, "WMenu.doPost - Cannot parse date: " + dateString + " - " + e.getMessage());
		}
		Env.setContext(ctx, "#Date", ts.toString());    //  JDBC format


	//	Log.printProperties(System.getProperties(), "System");
	//	Log.printProperties(cProp, "Cookie");
	//	Log.printProperties(ctx, "Servlet Context");
	//	Log.printProperties(Env.getCtx(), "Apps Env Context");


		/**********************************************************************
		 *  Create Menu output
		 */
		String windowTitle = Msg.getMsg(ctx, "Menu");
		String statusMessage = Msg.getMsg(ctx, "SelectMenuItem");

		//	Document
		WDoc doc = WDoc.create (windowTitle);
		head header = doc.getHead();
		//  Target
		header.addElement(new base().setTarget(WEnv.TARGET_WINDOW));
		//  Specific Menu Script/Stylesheet
		header.addElement(new script("", WEnv.getBaseDirectory("menu.js"), "text/javascript", "JavaScript1.2"));
		header.addElement(new link().setRel("stylesheet").setHref(WEnv.getBaseDirectory("menu.css")));

		//	Body
		body body = doc.getBody();
		//  Header
		body.addElement(new cite(loginInfo));
		String title = windowTitle + " - " + loginInfo;
		body.addElement(new script("top.document.title='" + title + "';"));
		body.addElement(new script("defaultStatus = '" + statusMessage + "';"));

		//  Clear Window Frame
		body.addElement(WUtil.getClearFrame(WEnv.TARGET_WINDOW));

		//  Load Menu Structure     ----------------------
		MNode root = loadTree(AD_Client_ID, AD_Role_ID, AD_User_ID, Env.getAD_Language(ctx));
		//
		StringBuffer buf = new StringBuffer();
		buf.append("<ul>");
		for (int i = 0; i < root.children.size(); i++)
		{
			MNode node = (MNode)root.children.get(i);
			node.print(node, buf);
		}
		buf.append("</ul>");
		body.addElement(buf.toString());

		//  Exit Info
		body.addElement(new hr());
		String url = request.getRequestURI() + "?Exit=true";
		body.addElement(new a(url, Msg.getMsg(ctx, "Exit")));

		//  Can we store Cookie ?
		if (!cProp.getProperty(WLogin.P_STORE, "N").equals("Y"))
			cProp.clear();

		WUtil.createResponse (request, response, this, cProp, doc, false);
	}   //  doPost

	/**
	 *  Check Login information and set context.
	 *  @returns    true if login info are OK
	 *  @param ctx context
	 *  @param AD_User_ID user
	 *  @param AD_Role_ID role
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param M_Warehouse_ID warehouse
	 */
	private String checkLogin (Properties ctx, int AD_User_ID, int AD_Role_ID, int AD_Client_ID, int AD_Org_ID, int M_Warehouse_ID)
	{
		//  Get Login Info
		String loginInfo = null;
		//  Verify existance of User/Client/Org/Role and User's acces to Client & Org
		String sql = "SELECT u.Name || '@' || c.Name || '.' || o.Name || ' [' || INITCAP(USER) || ']' AS Text "
			+ "FROM AD_User u, AD_Client c, AD_Org o, AD_User_Roles ur "
			+ "WHERE u.AD_User_ID=?"    //  #1
			+ " AND c.AD_Client_ID=?"   //  #2
			+ " AND o.AD_Org_ID=?"      //  #3
			+ " AND ur.AD_Role_ID=?"    //  #4
			+ " AND ur.AD_User_ID=u.AD_User_ID"
			+ " AND (o.AD_Client_ID = 0 OR o.AD_Client_ID=c.AD_Client_ID)"
			+ " AND c.AD_Client_ID IN (SELECT AD_Client_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)"
			+ " AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			pstmt.setInt(2, AD_Client_ID);
			pstmt.setInt(3, AD_Org_ID);
			pstmt.setInt(4, AD_Role_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				loginInfo = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("WMenu.checkLogin", e);
		}

		//  not verified
		if (loginInfo == null)
			return null;

		//  Set Preferences
		KeyNamePair org = new KeyNamePair(AD_Org_ID, String.valueOf(AD_Org_ID));
		KeyNamePair wh = null;
		if (M_Warehouse_ID > 0)
			wh = new KeyNamePair(M_Warehouse_ID, String.valueOf(M_Warehouse_ID));
		//
		Timestamp date = null;
		String printer = null;
		DB.loadPreferences(ctx, org, wh, date, printer);
		//
		return loginInfo;
	}   //  checkLogin


	/*************************************************************************/

	/**
	 *  Load Tree and return root node
	 *  @param AD_Client_ID client
	 *  @param AD_Role_ID role
	 *  @param AD_User_ID user
	 *  @param AD_Language language
	 *  @return node
	 */
	private MNode loadTree (int AD_Client_ID, int AD_Role_ID, int AD_User_ID, String AD_Language)
	{
		//	Get Tree info with start node
		int     AD_Tree_ID;
		String	Name;
		String	TreeType;
		String	Description;
		int 	startID;
		//
		MNode   root = null;

		//  Get Root Node
		String SQL = "SELECT t.AD_Tree_ID, t.Name, t.Description, t.TreeType, tn.Node_ID "
			+ "FROM AD_Tree t, AD_ClientInfo c, AD_TreeNode tn "
			+ "WHERE t.AD_Tree_ID=tn.AD_Tree_ID"
			+ " AND tn.Parent_ID IS NULL"
			+ " AND t.AD_Tree_ID=c.AD_Tree_Menu_ID"
			+ " AND c.AD_Client_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(SQL);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				return null;
			}
			AD_Tree_ID = rs.getInt(1);
			Name = rs.getString(2);
			Description = rs.getString(3);
			TreeType = rs.getString(4);
			startID = rs.getInt(5);

			//	create root node
			root = new MNode (startID, Name, Description, true, "");
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadTree -1", e);
			return null;
		}

		//  SQL for TreeNodes
		StringBuffer cmd = new StringBuffer("SELECT tn.Node_ID,tn.Parent_ID,tn.SeqNo, "
			+ "(SELECT 'Y' FROM AD_TreeBar tb WHERE tb.AD_Tree_ID=tn.AD_Tree_ID AND tb.AD_User_ID=")
			.append(AD_User_ID).append(" AND tb.Node_ID=tn.Node_ID) "
			+ "FROM AD_TreeNode tn ")
			.append("WHERE tn.IsActive='Y' "
			+ "START WITH Parent_ID IS NULL AND AD_Tree_ID=? "
			+ "CONNECT BY Parent_ID=PRIOR Node_ID AND AD_Tree_ID=? "
			+ "ORDER BY LEVEL, SeqNo");

		//  SQL for Node Info
		StringBuffer cmdNode = new StringBuffer();
		boolean base = Env.isBaseLanguage(Env.getCtx(), "AD_Menu");
		if (base)
			cmdNode.append("SELECT m.Name,m.Description,m.IsSummary,m.Action "
				+ "FROM AD_Menu m");
		else
			cmdNode.append("SELECT t.Name,t.Description,m.IsSummary,m.Action "
				+ "FROM AD_Menu m, AD_Menu_Trl t");
			cmdNode.append(", (SELECT ").append(AD_Role_ID).append(" AS XRole FROM DUAL)");
		cmdNode.append(" WHERE m.AD_Menu_ID=?");
		if (!base)
			cmdNode.append(" AND m.AD_Menu_ID=t.AD_Menu_ID AND t.AD_Language='")
				.append(Env.getAD_Language(Env.getCtx())).append("'");

		cmdNode.append(" AND m.IsActive='Y' "
			+ "AND (m.IsSummary='Y' OR m.Action='B'"
			+ " OR EXISTS (SELECT * FROM AD_Window_Access wa WHERE wa.AD_Window_ID=m.AD_Window_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Process_Access wa WHERE wa.AD_Process_ID=m.AD_Process_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Form_Access wa WHERE wa.AD_Form_ID=m.AD_Form_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Task_Access wa WHERE wa.AD_Task_ID=m.AD_Task_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Workflow_Access wa WHERE wa.AD_Workflow_ID=m.AD_Workflow_ID AND wa.AD_Role_ID=XRole)"
			+ ")");
	//	Log.trace(Log.l6_Database, "SQL Tree", cmd.toString());
	//	Log.trace(Log.l6_Database, "SQL Node", cmdNode.toString());

		//  The Node Loop
		try
		{
			PreparedStatement pstmtNode = DB.prepareStatement(cmdNode.toString());
			//
			PreparedStatement pstmt = DB.prepareStatement(cmd.toString());
			pstmt.setInt(1, AD_Tree_ID);
			pstmt.setInt(2, AD_Tree_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int Node_ID = rs.getInt(1);
				int Parent_ID = rs.getInt(2);
				int SeqNo = rs.getInt(3);
				boolean onBar = (rs.getString(4) != null);
				loadNode (pstmtNode, root, Node_ID, Parent_ID, SeqNo, onBar);
			}
			rs.close();
			pstmt.close();
			pstmtNode.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadTree -2", e);
		}

		//  Clean Menu tree
		root.clean(root);

		return root;
	}   //  loadTree

	/**
	 *  Load Node using prepared statement
	 *  @param  pstmt       Prepared Statement requiring to set Node_ID and returning
	 *      Name,Description,IsSummary,ImageIndiactor
	 *  @param  root		root node
	 *  @param  Node_ID     Key of the record
	 *  @param  Parent_ID   Parent ID of the record
	 *  @param  SeqNo       Sort index
	 *  @param  onBar       Node also on Shortcut bar
	 */
	private void loadNode (PreparedStatement pstmt, MNode root,
		int Node_ID, int Parent_ID, int SeqNo, boolean onBar)
	{
		try
		{
			pstmt.setInt(1, Node_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				String Name = rs.getString(1);
				String Description = rs.getString(2);
				boolean IsSummary = rs.getString(3).equals("Y");
				String ImageIndicator = rs.getString(4);

				if (Name != null)
				{
					MNode child = new MNode (Node_ID, Name, Description, IsSummary, ImageIndicator);
					child.add(root, Parent_ID, child);
				}
			}
		//	else
		//		Log.trace(Log.l6_Database,"Not added", "Node_ID=" + Node_ID);
			rs.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadNode", e);
		}
	}   //  loadNode


	/**************************************************************************
	 *  Web Menu Tree Node
	 */
	class MNode
	{
		/**
		 *  Constructor
		 *  @param ID id
		 *  @param Name name
		 *  @param Description description
		 *  @param isSummary summary
		 *  @param Type type
		 */
		public MNode (int ID, String Name, String Description, boolean isSummary, String Type)
		{
			this.ID = ID;
			this.Name = Name;
			this.Description = Description;
			if (this.Description == null)
				this.Description = "";
			this.isSummary = isSummary;
			this.Type = Type;
			if (this.Type == null)
				this.Type = "";
		}

		public int          ID;
		public String       Name;
		public String       Description;
		public boolean      isSummary;
		public String       Type;
		public ArrayList    children = new ArrayList();

		/**
		 *  Add to list of children
		 *  @param child child
		 *  @return true
		 */
		public boolean add (MNode child)
		{
			children.add(child);
			return true;
		}   //  add

		/*********************************************************************/

		/**
		 *  Traverse Tree starting at root to add child to parent with parentID
		 *  @param root root
		 *  @param parentID parent
		 *  @param child child
		 *  @returns    false if not added
		 */
		public boolean add (MNode root, int parentID, MNode child)
		{
			//  is this root the parent?
			if (root.ID == parentID)
				return root.add(child);

			//  do we have children to check?
			else if (root.children.size() == 0)
				return false;

			//  check children
			for (int i = 0; i < root.children.size(); i++)
			{
				MNode cc = (MNode)root.children.get(i);
				if (root.add(cc, parentID, child))
					return true;
			}
			//  nothing found
			return false;
		}   //  add

		/**
		 *  Traverse Tree and print it
		 *  @param root root
		 *  @param buf buffer
		 */
		public void print (MNode root, StringBuffer buf)
		{
			//  Leaf
			if (root.children.size() == 0)
			{
				/**
				 *  <li id="menuXXXXX"><a href="...." onMouseOver="status='Menu Description';return true;">Menu Entry</a></li>
				 */
				String item = "";
				String servletName = "";
				if (root.Type.equals("W"))
				{
					item = "menuWindow";
					servletName = "WWindow";
				}
				else if (root.Type.equals("X"))
				{
					item = "menuWindow";
					servletName = "WForm";
				}
				else if (root.Type.equals("R"))
				{
					item = "menuReport";
					servletName = "WReport";
				}
				else if (root.Type.equals("P"))
				{
					item = "menuProcess";
					servletName = "WProcess";
				}
				else if (root.Type.equals("F"))
				{
					item = "menuWorkflow";
					servletName = "WWorkflow";
				}
				else if (root.Type.equals("T"))
				{
					item = "menuProcess";
					servletName = "WTask";
				}
				else
					servletName = "WError";

				String description = root.Description.replace('\'',' ').replace('"',' ');
				buf.append("<li id=\"" + item + "\"><a href=\"");
				//  url -   /appl/servletName?AD_Menu_ID=x
				buf.append(WEnv.getBaseDirectory(servletName))
					.append("?AD_Menu_ID=")
					.append(root.ID);
				//
				buf.append("\" onMouseOver=\"status='" + description + "';return true;\" onclick=showLoadingWindow(\"" + WEnv.getBaseDirectory("") + "\")>");
				buf.append(root.Name);
				buf.append("</a></li>\n");
			}
			else
			{
				/**
				 *  <li id="foldHeader">MenuEntry</li>
				 *  <ul style="display:none">
				 *  ....
				 *  </ul>
				 */
				buf.append("\n<li id=\"menuHeader\">");         //  summary node
				buf.append(root.Name);
				buf.append("</li>\n");
				//  Next Level
				buf.append("<ul style=\"display:none\">\n");    //  start next level
				for (int i = 0; i < root.children.size(); i++)
				{
					MNode cc = (MNode)root.children.get(i);
					root.print(cc, buf);
				}
				buf.append("</ul>");                                            //  finish next level
			}
		}	//  print

		/**
		 *  Clean tree of parents without children
		 *  @param root root node
		 */
		public void clean (MNode root)
		{
			int size = root.children.size();
			if (size == 0)
				return;
			//
			ArrayList temp = new ArrayList(size);
			boolean changed = false;
			for (int i = 0; i < size; i++)
			{
				MNode cc = (MNode)root.children.get(i);
				int ccSize = cc.children.size();
				if (cc.isSummary && ccSize == 0)
					changed = true;
				else
					temp.add(cc);
				if (ccSize != 0)
					cc.clean(cc);
			}
			if (changed)
				root.children = temp;
		}   //  clean

	}   //  MNode

}   //  WMenu
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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;

import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Web Menu
 *
 *  @author Jorg Janke
 *  @version  $Id: WMenu.java,v 1.1.1.1 2002/10/12 01:06:55 jjanke Exp $
 */
public class WMenu extends HttpServlet
{
	/**
	 *  Initialize global variables
	 *  @param config config
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("WMenu.init");
	}   //  init

	/**
	 * 	Get Servlet information
	 *  @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Menu";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		Log.trace(Log.l1_User, "WMenu.destroy");
		super.destroy();
	}   //  destroy

	/** */
	private PreparedStatement	m_pstmt;


	/*************************************************************************/

	/**
	 *  Process the HTTP Get request.
	 *  - Exit (Logout)
	 *  - AD_Window_ID Forward to Window
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WMenu.doGet - Process Menu Request");
		//  Get Parameter: Exit
		if (request.getParameter("Exit") != null)
		{
			WUtil.createLoginPage (request, response, this, null, "Exit");
			return;
		}

		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		if (ctx == null)
		{
			WUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Window
		int AD_Window_ID = WUtil.getParameterAsInt(request, "AD_Window_ID");

		//  Forward to WWindow
		if (AD_Window_ID != 0)
		{
			Log.trace(Log.l4_Data, "AD_Window_ID=" + AD_Window_ID);
			//
			String url = WEnv.getBaseDirectory("WWindow?AD_Window_ID=" + AD_Window_ID);
			Log.trace(Log.l4_Data, "Forward to - " + url);
			//
			RequestDispatcher rd = getServletContext().getRequestDispatcher(url);
			rd.forward(request, response);
			return;
		}

		//  Request not serviceable
		WUtil.createErrorPage(request, response, this, null, "NotImplemented");
	}   //  doGet


	/*************************************************************************/

	/**
	 *  Process the HTTP Post request - Verify Input & Create Menu
	 *
	 * @param request request
	 * @param response response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WMenu.doPost - Create Menu");
		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		Properties cProp = WUtil.getCookieProprties(request);
		if (ctx == null)
		{
			WUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Get Parameters: Role, Client, Org, Warehouse, Date
		String role = request.getParameter(WLogin.P_ROLE);
		String client = request.getParameter(WLogin.P_CLIENT);
		String org = request.getParameter(WLogin.P_ORG);
		String wh = request.getParameter(WLogin.P_WAREHOUSE);
		if (wh == null)
			wh = "";

		//  Get context
		if (role == null || client == null || org == null)
		{
			WUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Get Info from Context - User, Role, Client
		int AD_User_ID = Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID = Env.getContextAsInt(ctx, "#AD_Role_ID");
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		//  Not available in context yet - Org, Warehouse
		int AD_Org_ID = -1;
		int M_Warehouse_ID = -1;

		//  Get latest info from context
		try
		{
			int req_role = Integer.parseInt(role);
			if (req_role != AD_Role_ID)
			{
				Log.trace(Log.l5_DData, "AD_Role_ID - changed from " + AD_Role_ID);
				AD_Role_ID = req_role;
				Env.setContext(ctx, "#AD_Role_ID", AD_Role_ID);
			}
			Log.trace(Log.l5_DData, "AD_Role_ID = " + AD_Role_ID);
			//
			int req_client = Integer.parseInt(client);
			if (req_client != AD_Client_ID)
			{
				Log.trace(Log.l5_DData, "AD_Client_ID - changed from " + AD_Client_ID);
				AD_Client_ID = req_client;
				Env.setContext(ctx, "#AD_Client_ID", AD_Client_ID);
			}
			Log.trace(Log.l5_DData, "AD_Client_ID = " + AD_Client_ID);
			//
			AD_Org_ID = Integer.parseInt(org);
			Log.trace(Log.l5_DData, "AD_Org_ID = " + AD_Org_ID);
			//
			if (wh.length() > 0)
			{
				M_Warehouse_ID = Integer.parseInt(wh);
				Log.trace(Log.l5_DData, "M_Warehouse_ID = " + M_Warehouse_ID);
			}
		}
		catch (Exception e)
		{
			Log.error("WMenu.doPost - Parameter", e);
			WUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Check Login info and set environment
		String loginInfo = checkLogin (ctx, AD_User_ID, AD_Role_ID, AD_Client_ID, AD_Org_ID, M_Warehouse_ID);
		if (loginInfo == null)
		{
			WUtil.createErrorPage(request, response, this, ctx, Msg.getMsg(ctx, "RoleInconsistent"));
			return;
		}
		sess.setAttribute(WEnv.SA_LOGININFO, loginInfo);

		//  Set cookie for future defaults
		cProp.setProperty(WLogin.P_ROLE, String.valueOf(AD_Role_ID));
		cProp.setProperty(WLogin.P_CLIENT, String.valueOf(AD_Client_ID));
		cProp.setProperty(WLogin.P_ORG, String.valueOf(AD_Org_ID));
		if (M_Warehouse_ID == -1)
			cProp.setProperty(WLogin.P_WAREHOUSE, "");
		else
			cProp.setProperty(WLogin.P_WAREHOUSE, String.valueOf(M_Warehouse_ID));

		//  Set Date
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		//  Try to parse Date
		String dateString = request.getParameter(WLogin.P_DATE);
		try
		{
			if (dateString != null && dateString.length() > 0)
			{
				Language language = (Language)sess.getAttribute(WEnv.SA_LANGUAGE);
				DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
				java.util.Date date = df.parse(dateString);
				ts = new Timestamp(date.getTime());
			}
		}
		catch (Exception e)
		{
			Log.trace(Log.l1_User, "WMenu.doPost - Cannot parse date: " + dateString + " - " + e.getMessage());
		}
		Env.setContext(ctx, "#Date", ts.toString());    //  JDBC format


	//	Log.printProperties(System.getProperties(), "System");
	//	Log.printProperties(cProp, "Cookie");
	//	Log.printProperties(ctx, "Servlet Context");
	//	Log.printProperties(Env.getCtx(), "Apps Env Context");


		/**********************************************************************
		 *  Create Menu output
		 */
		String windowTitle = Msg.getMsg(ctx, "Menu");
		String statusMessage = Msg.getMsg(ctx, "SelectMenuItem");

		//	Document
		WDoc doc = WDoc.create (windowTitle);
		head header = doc.getHead();
		//  Target
		header.addElement(new base().setTarget(WEnv.TARGET_WINDOW));
		//  Specific Menu Script/Stylesheet
		header.addElement(new script("", WEnv.getBaseDirectory("menu.js"), "text/javascript", "JavaScript1.2"));
		header.addElement(new link().setRel("stylesheet").setHref(WEnv.getBaseDirectory("menu.css")));

		//	Body
		body body = doc.getBody();
		//  Header
		body.addElement(new cite(loginInfo));
		String title = windowTitle + " - " + loginInfo;
		body.addElement(new script("top.document.title='" + title + "';"));
		body.addElement(new script("defaultStatus = '" + statusMessage + "';"));

		//  Clear Window Frame
		body.addElement(WUtil.getClearFrame(WEnv.TARGET_WINDOW));

		//  Load Menu Structure     ----------------------
		MNode root = loadTree(AD_Client_ID, AD_Role_ID, AD_User_ID, Env.getAD_Language(ctx));
		//
		StringBuffer buf = new StringBuffer();
		buf.append("<ul>");
		for (int i = 0; i < root.children.size(); i++)
		{
			MNode node = (MNode)root.children.get(i);
			node.print(node, buf);
		}
		buf.append("</ul>");
		body.addElement(buf.toString());

		//  Exit Info
		body.addElement(new hr());
		String url = request.getRequestURI() + "?Exit=true";
		body.addElement(new a(url, Msg.getMsg(ctx, "Exit")));

		//  Can we store Cookie ?
		if (!cProp.getProperty(WLogin.P_STORE, "N").equals("Y"))
			cProp.clear();

		WUtil.createResponse (request, response, this, cProp, doc, false);
	}   //  doPost

	/**
	 *  Check Login information and set context.
	 *  @returns    true if login info are OK
	 *  @param ctx context
	 *  @param AD_User_ID user
	 *  @param AD_Role_ID role
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param M_Warehouse_ID warehouse
	 */
	private String checkLogin (Properties ctx, int AD_User_ID, int AD_Role_ID, int AD_Client_ID, int AD_Org_ID, int M_Warehouse_ID)
	{
		//  Get Login Info
		String loginInfo = null;
		//  Verify existance of User/Client/Org/Role and User's acces to Client & Org
		String sql = "SELECT u.Name || '@' || c.Name || '.' || o.Name || ' [' || INITCAP(USER) || ']' AS Text "
			+ "FROM AD_User u, AD_Client c, AD_Org o, AD_User_Roles ur "
			+ "WHERE u.AD_User_ID=?"    //  #1
			+ " AND c.AD_Client_ID=?"   //  #2
			+ " AND o.AD_Org_ID=?"      //  #3
			+ " AND ur.AD_Role_ID=?"    //  #4
			+ " AND ur.AD_User_ID=u.AD_User_ID"
			+ " AND (o.AD_Client_ID = 0 OR o.AD_Client_ID=c.AD_Client_ID)"
			+ " AND c.AD_Client_ID IN (SELECT AD_Client_ID FROM AD_Role_ClientAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)"
			+ " AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			pstmt.setInt(2, AD_Client_ID);
			pstmt.setInt(3, AD_Org_ID);
			pstmt.setInt(4, AD_Role_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				loginInfo = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("WMenu.checkLogin", e);
		}

		//  not verified
		if (loginInfo == null)
			return null;

		//  Set Preferences
		KeyNamePair org = new KeyNamePair(AD_Org_ID, String.valueOf(AD_Org_ID));
		KeyNamePair wh = null;
		if (M_Warehouse_ID > 0)
			wh = new KeyNamePair(M_Warehouse_ID, String.valueOf(M_Warehouse_ID));
		//
		Timestamp date = null;
		String printer = null;
		DB.loadPreferences(ctx, org, wh, date, printer);
		//
		return loginInfo;
	}   //  checkLogin


	/*************************************************************************/

	/**
	 *  Load Tree and return root node
	 *  @param AD_Client_ID client
	 *  @param AD_Role_ID role
	 *  @param AD_User_ID user
	 *  @param AD_Language language
	 *  @return node
	 */
	private MNode loadTree (int AD_Client_ID, int AD_Role_ID, int AD_User_ID, String AD_Language)
	{
		//	Get Tree info with start node
		int     AD_Tree_ID;
		String	Name;
		String	TreeType;
		String	Description;
		int 	startID;
		//
		MNode   root = null;

		//  Get Root Node
		String SQL = "SELECT t.AD_Tree_ID, t.Name, t.Description, t.TreeType, tn.Node_ID "
			+ "FROM AD_Tree t, AD_ClientInfo c, AD_TreeNode tn "
			+ "WHERE t.AD_Tree_ID=tn.AD_Tree_ID"
			+ " AND tn.Parent_ID IS NULL"
			+ " AND t.AD_Tree_ID=c.AD_Tree_Menu_ID"
			+ " AND c.AD_Client_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(SQL);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				return null;
			}
			AD_Tree_ID = rs.getInt(1);
			Name = rs.getString(2);
			Description = rs.getString(3);
			TreeType = rs.getString(4);
			startID = rs.getInt(5);

			//	create root node
			root = new MNode (startID, Name, Description, true, "");
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadTree -1", e);
			return null;
		}

		//  SQL for TreeNodes
		StringBuffer cmd = new StringBuffer("SELECT tn.Node_ID,tn.Parent_ID,tn.SeqNo, "
			+ "(SELECT 'Y' FROM AD_TreeBar tb WHERE tb.AD_Tree_ID=tn.AD_Tree_ID AND tb.AD_User_ID=")
			.append(AD_User_ID).append(" AND tb.Node_ID=tn.Node_ID) "
			+ "FROM AD_TreeNode tn ")
			.append("WHERE tn.IsActive='Y' "
			+ "START WITH Parent_ID IS NULL AND AD_Tree_ID=? "
			+ "CONNECT BY Parent_ID=PRIOR Node_ID AND AD_Tree_ID=? "
			+ "ORDER BY LEVEL, SeqNo");

		//  SQL for Node Info
		StringBuffer cmdNode = new StringBuffer();
		boolean base = Env.isBaseLanguage(Env.getCtx(), "AD_Menu");
		if (base)
			cmdNode.append("SELECT m.Name,m.Description,m.IsSummary,m.Action "
				+ "FROM AD_Menu m");
		else
			cmdNode.append("SELECT t.Name,t.Description,m.IsSummary,m.Action "
				+ "FROM AD_Menu m, AD_Menu_Trl t");
			cmdNode.append(", (SELECT ").append(AD_Role_ID).append(" AS XRole FROM DUAL)");
		cmdNode.append(" WHERE m.AD_Menu_ID=?");
		if (!base)
			cmdNode.append(" AND m.AD_Menu_ID=t.AD_Menu_ID AND t.AD_Language='")
				.append(Env.getAD_Language(Env.getCtx())).append("'");

		cmdNode.append(" AND m.IsActive='Y' "
			+ "AND (m.IsSummary='Y' OR m.Action='B'"
			+ " OR EXISTS (SELECT * FROM AD_Window_Access wa WHERE wa.AD_Window_ID=m.AD_Window_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Process_Access wa WHERE wa.AD_Process_ID=m.AD_Process_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Form_Access wa WHERE wa.AD_Form_ID=m.AD_Form_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Task_Access wa WHERE wa.AD_Task_ID=m.AD_Task_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Workflow_Access wa WHERE wa.AD_Workflow_ID=m.AD_Workflow_ID AND wa.AD_Role_ID=XRole)"
			+ ")");
	//	Log.trace(Log.l6_Database, "SQL Tree", cmd.toString());
	//	Log.trace(Log.l6_Database, "SQL Node", cmdNode.toString());

		//  The Node Loop
		try
		{
			PreparedStatement pstmtNode = DB.prepareStatement(cmdNode.toString());
			//
			PreparedStatement pstmt = DB.prepareStatement(cmd.toString());
			pstmt.setInt(1, AD_Tree_ID);
			pstmt.setInt(2, AD_Tree_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int Node_ID = rs.getInt(1);
				int Parent_ID = rs.getInt(2);
				int SeqNo = rs.getInt(3);
				boolean onBar = (rs.getString(4) != null);
				loadNode (pstmtNode, root, Node_ID, Parent_ID, SeqNo, onBar);
			}
			rs.close();
			pstmt.close();
			pstmtNode.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadTree -2", e);
		}

		//  Clean Menu tree
		root.clean(root);

		return root;
	}   //  loadTree

	/**
	 *  Load Node using prepared statement
	 *  @param  pstmt       Prepared Statement requiring to set Node_ID and returning
	 *      Name,Description,IsSummary,ImageIndiactor
	 *  @param  root		root node
	 *  @param  Node_ID     Key of the record
	 *  @param  Parent_ID   Parent ID of the record
	 *  @param  SeqNo       Sort index
	 *  @param  onBar       Node also on Shortcut bar
	 */
	private void loadNode (PreparedStatement pstmt, MNode root,
		int Node_ID, int Parent_ID, int SeqNo, boolean onBar)
	{
		try
		{
			pstmt.setInt(1, Node_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				String Name = rs.getString(1);
				String Description = rs.getString(2);
				boolean IsSummary = rs.getString(3).equals("Y");
				String ImageIndicator = rs.getString(4);

				if (Name != null)
				{
					MNode child = new MNode (Node_ID, Name, Description, IsSummary, ImageIndicator);
					child.add(root, Parent_ID, child);
				}
			}
		//	else
		//		Log.trace(Log.l6_Database,"Not added", "Node_ID=" + Node_ID);
			rs.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadNode", e);
		}
	}   //  loadNode


	/**************************************************************************
	 *  Web Menu Tree Node
	 */
	class MNode
	{
		/**
		 *  Constructor
		 *  @param ID id
		 *  @param Name name
		 *  @param Description description
		 *  @param isSummary summary
		 *  @param Type type
		 */
		public MNode (int ID, String Name, String Description, boolean isSummary, String Type)
		{
			this.ID = ID;
			this.Name = Name;
			this.Description = Description;
			if (this.Description == null)
				this.Description = "";
			this.isSummary = isSummary;
			this.Type = Type;
			if (this.Type == null)
				this.Type = "";
		}

		public int          ID;
		public String       Name;
		public String       Description;
		public boolean      isSummary;
		public String       Type;
		public ArrayList    children = new ArrayList();

		/**
		 *  Add to list of children
		 *  @param child child
		 *  @return true
		 */
		public boolean add (MNode child)
		{
			children.add(child);
			return true;
		}   //  add

		/*********************************************************************/

		/**
		 *  Traverse Tree starting at root to add child to parent with parentID
		 *  @param root root
		 *  @param parentID parent
		 *  @param child child
		 *  @returns    false if not added
		 */
		public boolean add (MNode root, int parentID, MNode child)
		{
			//  is this root the parent?
			if (root.ID == parentID)
				return root.add(child);

			//  do we have children to check?
			else if (root.children.size() == 0)
				return false;

			//  check children
			for (int i = 0; i < root.children.size(); i++)
			{
				MNode cc = (MNode)root.children.get(i);
				if (root.add(cc, parentID, child))
					return true;
			}
			//  nothing found
			return false;
		}   //  add

		/**
		 *  Traverse Tree and print it
		 *  @param root root
		 *  @param buf buffer
		 */
		public void print (MNode root, StringBuffer buf)
		{
			//  Leaf
			if (root.children.size() == 0)
			{
				/**
				 *  <li id="menuXXXXX"><a href="...." onMouseOver="status='Menu Description';return true;">Menu Entry</a></li>
				 */
				String item = "";
				String servletName = "";
				if (root.Type.equals("W"))
				{
					item = "menuWindow";
					servletName = "WWindow";
				}
				else if (root.Type.equals("X"))
				{
					item = "menuWindow";
					servletName = "WForm";
				}
				else if (root.Type.equals("R"))
				{
					item = "menuReport";
					servletName = "WReport";
				}
				else if (root.Type.equals("P"))
				{
					item = "menuProcess";
					servletName = "WProcess";
				}
				else if (root.Type.equals("F"))
				{
					item = "menuWorkflow";
					servletName = "WWorkflow";
				}
				else if (root.Type.equals("T"))
				{
					item = "menuProcess";
					servletName = "WTask";
				}
				else
					servletName = "WError";

				String description = root.Description.replace('\'',' ').replace('"',' ');
				buf.append("<li id=\"" + item + "\"><a href=\"");
				//  url -   /appl/servletName?AD_Menu_ID=x
				buf.append(WEnv.getBaseDirectory(servletName))
					.append("?AD_Menu_ID=")
					.append(root.ID);
				//
				buf.append("\" onMouseOver=\"status='" + description + "';return true;\" onclick=showLoadingWindow(\"" + WEnv.getBaseDirectory("") + "\")>");
				buf.append(root.Name);
				buf.append("</a></li>\n");
			}
			else
			{
				/**
				 *  <li id="foldHeader">MenuEntry</li>
				 *  <ul style="display:none">
				 *  ....
				 *  </ul>
				 */
				buf.append("\n<li id=\"menuHeader\">");         //  summary node
				buf.append(root.Name);
				buf.append("</li>\n");
				//  Next Level
				buf.append("<ul style=\"display:none\">\n");    //  start next level
				for (int i = 0; i < root.children.size(); i++)
				{
					MNode cc = (MNode)root.children.get(i);
					root.print(cc, buf);
				}
				buf.append("</ul>");                                            //  finish next level
			}
		}	//  print

		/**
		 *  Clean tree of parents without children
		 *  @param root root node
		 */
		public void clean (MNode root)
		{
			int size = root.children.size();
			if (size == 0)
				return;
			//
			ArrayList temp = new ArrayList(size);
			boolean changed = false;
			for (int i = 0; i < size; i++)
			{
				MNode cc = (MNode)root.children.get(i);
				int ccSize = cc.children.size();
				if (cc.isSummary && ccSize == 0)
					changed = true;
				else
					temp.add(cc);
				if (ccSize != 0)
					cc.clean(cc);
			}
			if (changed)
				root.children = temp;
		}   //  clean

	}   //  MNode

}   //  WMenu
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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;

import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Web Menu
 *
 *  @author Jorg Janke
 *  @version  $Id: WMenu.java,v 1.2 2003/03/31 05:27:26 jjanke Exp $
 */
public class WMenu extends HttpServlet
{
	/**
	 *  Initialize global variables
	 *  @param config config
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("WMenu.init");
	}   //  init

	/**
	 * 	Get Servlet information
	 *  @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Menu";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		Log.trace(Log.l1_User, "WMenu.destroy");
		super.destroy();
	}   //  destroy

	/** */
	private PreparedStatement	m_pstmt;


	/*************************************************************************/

	/**
	 *  Process the HTTP Get request.
	 *  - Exit (Logout)
	 *  - AD_Window_ID Forward to Window
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WMenu.doGet - Process Menu Request");
		//  Get Parameter: Exit
		if (request.getParameter("Exit") != null)
		{
			WUtil.createLoginPage (request, response, this, null, "Exit");
			return;
		}

		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		if (ctx == null)
		{
			WUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Window
		int AD_Window_ID = WUtil.getParameterAsInt(request, "AD_Window_ID");

		//  Forward to WWindow
		if (AD_Window_ID != 0)
		{
			Log.trace(Log.l4_Data, "AD_Window_ID=" + AD_Window_ID);
			//
			String url = WEnv.getBaseDirectory("WWindow?AD_Window_ID=" + AD_Window_ID);
			Log.trace(Log.l4_Data, "Forward to - " + url);
			//
			RequestDispatcher rd = getServletContext().getRequestDispatcher(url);
			rd.forward(request, response);
			return;
		}

		//  Request not serviceable
		WUtil.createErrorPage(request, response, this, null, "NotImplemented");
	}   //  doGet


	/*************************************************************************/

	/**
	 *  Process the HTTP Post request - Verify Input & Create Menu
	 *
	 * @param request request
	 * @param response response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WMenu.doPost - Create Menu");
		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		Properties cProp = WUtil.getCookieProprties(request);
		if (ctx == null)
		{
			WUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Get Parameters: Role, Client, Org, Warehouse, Date
		String role = request.getParameter(WLogin.P_ROLE);
		String client = request.getParameter(WLogin.P_CLIENT);
		String org = request.getParameter(WLogin.P_ORG);
		String wh = request.getParameter(WLogin.P_WAREHOUSE);
		if (wh == null)
			wh = "";

		//  Get context
		if (role == null || client == null || org == null)
		{
			WUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Get Info from Context - User, Role, Client
		int AD_User_ID = Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID = Env.getContextAsInt(ctx, "#AD_Role_ID");
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		//  Not available in context yet - Org, Warehouse
		int AD_Org_ID = -1;
		int M_Warehouse_ID = -1;

		//  Get latest info from context
		try
		{
			int req_role = Integer.parseInt(role);
			if (req_role != AD_Role_ID)
			{
				Log.trace(Log.l5_DData, "AD_Role_ID - changed from " + AD_Role_ID);
				AD_Role_ID = req_role;
				Env.setContext(ctx, "#AD_Role_ID", AD_Role_ID);
			}
			Log.trace(Log.l5_DData, "AD_Role_ID = " + AD_Role_ID);
			//
			int req_client = Integer.parseInt(client);
			if (req_client != AD_Client_ID)
			{
				Log.trace(Log.l5_DData, "AD_Client_ID - changed from " + AD_Client_ID);
				AD_Client_ID = req_client;
				Env.setContext(ctx, "#AD_Client_ID", AD_Client_ID);
			}
			Log.trace(Log.l5_DData, "AD_Client_ID = " + AD_Client_ID);
			//
			AD_Org_ID = Integer.parseInt(org);
			Log.trace(Log.l5_DData, "AD_Org_ID = " + AD_Org_ID);
			//
			if (wh.length() > 0)
			{
				M_Warehouse_ID = Integer.parseInt(wh);
				Log.trace(Log.l5_DData, "M_Warehouse_ID = " + M_Warehouse_ID);
			}
		}
		catch (Exception e)
		{
			Log.error("WMenu.doPost - Parameter", e);
			WUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Check Login info and set environment
		String loginInfo = checkLogin (ctx, AD_User_ID, AD_Role_ID, AD_Client_ID, AD_Org_ID, M_Warehouse_ID);
		if (loginInfo == null)
		{
			WUtil.createErrorPage(request, response, this, ctx, Msg.getMsg(ctx, "RoleInconsistent"));
			return;
		}
		sess.setAttribute(WEnv.SA_LOGININFO, loginInfo);

		//  Set cookie for future defaults
		cProp.setProperty(WLogin.P_ROLE, String.valueOf(AD_Role_ID));
		cProp.setProperty(WLogin.P_CLIENT, String.valueOf(AD_Client_ID));
		cProp.setProperty(WLogin.P_ORG, String.valueOf(AD_Org_ID));
		if (M_Warehouse_ID == -1)
			cProp.setProperty(WLogin.P_WAREHOUSE, "");
		else
			cProp.setProperty(WLogin.P_WAREHOUSE, String.valueOf(M_Warehouse_ID));

		//  Set Date
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		//  Try to parse Date
		String dateString = request.getParameter(WLogin.P_DATE);
		try
		{
			if (dateString != null && dateString.length() > 0)
			{
				Language language = (Language)sess.getAttribute(WEnv.SA_LANGUAGE);
				DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
				java.util.Date date = df.parse(dateString);
				ts = new Timestamp(date.getTime());
			}
		}
		catch (Exception e)
		{
			Log.trace(Log.l1_User, "WMenu.doPost - Cannot parse date: " + dateString + " - " + e.getMessage());
		}
		Env.setContext(ctx, "#Date", ts.toString());    //  JDBC format


	//	Log.printProperties(System.getProperties(), "System");
	//	Log.printProperties(cProp, "Cookie");
	//	Log.printProperties(ctx, "Servlet Context");
	//	Log.printProperties(Env.getCtx(), "Apps Env Context");


		/**********************************************************************
		 *  Create Menu output
		 */
		String windowTitle = Msg.getMsg(ctx, "Menu");
		String statusMessage = Msg.getMsg(ctx, "SelectMenuItem");

		//	Document
		WDoc doc = WDoc.create (windowTitle);
		head header = doc.getHead();
		//  Target
		header.addElement(new base().setTarget(WEnv.TARGET_WINDOW));
		//  Specific Menu Script/Stylesheet
		header.addElement(new script("", WEnv.getBaseDirectory("menu.js"), "text/javascript", "JavaScript1.2"));
		header.addElement(new link().setRel("stylesheet").setHref(WEnv.getBaseDirectory("menu.css")));

		//	Body
		body body = doc.getBody();
		//  Header
		body.addElement(new cite(loginInfo));
		String title = windowTitle + " - " + loginInfo;
		body.addElement(new script("top.document.title='" + title + "';"));
		body.addElement(new script("defaultStatus = '" + statusMessage + "';"));

		//  Clear Window Frame
		body.addElement(WUtil.getClearFrame(WEnv.TARGET_WINDOW));

		//  Load Menu Structure     ----------------------
		MNode root = loadTree(AD_Client_ID, AD_Role_ID, AD_User_ID, Env.getAD_Language(ctx));
		//
		StringBuffer buf = new StringBuffer();
		buf.append("<ul>");
		for (int i = 0; i < root.children.size(); i++)
		{
			MNode node = (MNode)root.children.get(i);
			node.print(node, buf);
		}
		buf.append("</ul>");
		body.addElement(buf.toString());

		//  Exit Info
		body.addElement(new hr());
		String url = request.getRequestURI() + "?Exit=true";
		body.addElement(new a(url, Msg.getMsg(ctx, "Exit")));

		//  Can we store Cookie ?
		if (!cProp.getProperty(WLogin.P_STORE, "N").equals("Y"))
			cProp.clear();

		WUtil.createResponse (request, response, this, cProp, doc, false);
	}   //  doPost

	/**
	 *  Check Login information and set context.
	 *  @returns    true if login info are OK
	 *  @param ctx context
	 *  @param AD_User_ID user
	 *  @param AD_Role_ID role
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param M_Warehouse_ID warehouse
	 */
	private String checkLogin (Properties ctx, int AD_User_ID, int AD_Role_ID, int AD_Client_ID, int AD_Org_ID, int M_Warehouse_ID)
	{
		//  Get Login Info
		String loginInfo = null;
		//  Verify existance of User/Client/Org/Role and User's acces to Client & Org
		String sql = "SELECT u.Name || '@' || c.Name || '.' || o.Name || ' [' || INITCAP(USER) || ']' AS Text "
			+ "FROM AD_User u, AD_Client c, AD_Org o, AD_User_Roles ur "
			+ "WHERE u.AD_User_ID=?"    //  #1
			+ " AND c.AD_Client_ID=?"   //  #2
			+ " AND o.AD_Org_ID=?"      //  #3
			+ " AND ur.AD_Role_ID=?"    //  #4
			+ " AND ur.AD_User_ID=u.AD_User_ID"
			+ " AND (o.AD_Client_ID = 0 OR o.AD_Client_ID=c.AD_Client_ID)"
			+ " AND c.AD_Client_ID IN (SELECT AD_Client_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)"
			+ " AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			pstmt.setInt(2, AD_Client_ID);
			pstmt.setInt(3, AD_Org_ID);
			pstmt.setInt(4, AD_Role_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				loginInfo = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("WMenu.checkLogin", e);
		}

		//  not verified
		if (loginInfo == null)
			return null;

		//  Set Preferences
		KeyNamePair org = new KeyNamePair(AD_Org_ID, String.valueOf(AD_Org_ID));
		KeyNamePair wh = null;
		if (M_Warehouse_ID > 0)
			wh = new KeyNamePair(M_Warehouse_ID, String.valueOf(M_Warehouse_ID));
		//
		Timestamp date = null;
		String printer = null;
		DB.loadPreferences(ctx, org, wh, date, printer);
		//
		return loginInfo;
	}   //  checkLogin


	/*************************************************************************/

	/**
	 *  Load Tree and return root node
	 *  @param AD_Client_ID client
	 *  @param AD_Role_ID role
	 *  @param AD_User_ID user
	 *  @param AD_Language language
	 *  @return node
	 */
	private MNode loadTree (int AD_Client_ID, int AD_Role_ID, int AD_User_ID, String AD_Language)
	{
		//	Get Tree info with start node
		int     AD_Tree_ID;
		String	Name;
		String	TreeType;
		String	Description;
		int 	startID;
		//
		MNode   root = null;

		//  Get Root Node
		String SQL = "SELECT t.AD_Tree_ID, t.Name, t.Description, t.TreeType, tn.Node_ID "
			+ "FROM AD_Tree t, AD_ClientInfo c, AD_TreeNode tn "
			+ "WHERE t.AD_Tree_ID=tn.AD_Tree_ID"
			+ " AND tn.Parent_ID IS NULL"
			+ " AND t.AD_Tree_ID=c.AD_Tree_Menu_ID"
			+ " AND c.AD_Client_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(SQL);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				return null;
			}
			AD_Tree_ID = rs.getInt(1);
			Name = rs.getString(2);
			Description = rs.getString(3);
			TreeType = rs.getString(4);
			startID = rs.getInt(5);

			//	create root node
			root = new MNode (startID, Name, Description, true, "");
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadTree -1", e);
			return null;
		}

		//  SQL for TreeNodes
		StringBuffer cmd = new StringBuffer("SELECT tn.Node_ID,tn.Parent_ID,tn.SeqNo, "
			+ "(SELECT 'Y' FROM AD_TreeBar tb WHERE tb.AD_Tree_ID=tn.AD_Tree_ID AND tb.AD_User_ID=")
			.append(AD_User_ID).append(" AND tb.Node_ID=tn.Node_ID) "
			+ "FROM AD_TreeNode tn ")
			.append("WHERE tn.IsActive='Y' "
			+ "START WITH Parent_ID IS NULL AND AD_Tree_ID=? "
			+ "CONNECT BY Parent_ID=PRIOR Node_ID AND AD_Tree_ID=? "
			+ "ORDER BY LEVEL, SeqNo");

		//  SQL for Node Info
		StringBuffer cmdNode = new StringBuffer();
		boolean base = Env.isBaseLanguage(Env.getCtx(), "AD_Menu");
		if (base)
			cmdNode.append("SELECT m.Name,m.Description,m.IsSummary,m.Action "
				+ "FROM AD_Menu m");
		else
			cmdNode.append("SELECT t.Name,t.Description,m.IsSummary,m.Action "
				+ "FROM AD_Menu m, AD_Menu_Trl t");
			cmdNode.append(", (SELECT ").append(AD_Role_ID).append(" AS XRole FROM DUAL)");
		cmdNode.append(" WHERE m.AD_Menu_ID=?");
		if (!base)
			cmdNode.append(" AND m.AD_Menu_ID=t.AD_Menu_ID AND t.AD_Language='")
				.append(Env.getAD_Language(Env.getCtx())).append("'");

		cmdNode.append(" AND m.IsActive='Y' "
			+ "AND (m.IsSummary='Y' OR m.Action='B'"
			+ " OR EXISTS (SELECT * FROM AD_Window_Access wa WHERE wa.AD_Window_ID=m.AD_Window_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Process_Access wa WHERE wa.AD_Process_ID=m.AD_Process_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Form_Access wa WHERE wa.AD_Form_ID=m.AD_Form_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Task_Access wa WHERE wa.AD_Task_ID=m.AD_Task_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Workflow_Access wa WHERE wa.AD_Workflow_ID=m.AD_Workflow_ID AND wa.AD_Role_ID=XRole)"
			+ ")");
	//	Log.trace(Log.l6_Database, "SQL Tree", cmd.toString());
	//	Log.trace(Log.l6_Database, "SQL Node", cmdNode.toString());

		//  The Node Loop
		try
		{
			PreparedStatement pstmtNode = DB.prepareStatement(cmdNode.toString());
			//
			PreparedStatement pstmt = DB.prepareStatement(cmd.toString());
			pstmt.setInt(1, AD_Tree_ID);
			pstmt.setInt(2, AD_Tree_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int Node_ID = rs.getInt(1);
				int Parent_ID = rs.getInt(2);
				int SeqNo = rs.getInt(3);
				boolean onBar = (rs.getString(4) != null);
				loadNode (pstmtNode, root, Node_ID, Parent_ID, SeqNo, onBar);
			}
			rs.close();
			pstmt.close();
			pstmtNode.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadTree -2", e);
		}

		//  Clean Menu tree
		root.clean(root);

		return root;
	}   //  loadTree

	/**
	 *  Load Node using prepared statement
	 *  @param  pstmt       Prepared Statement requiring to set Node_ID and returning
	 *      Name,Description,IsSummary,ImageIndiactor
	 *  @param  root		root node
	 *  @param  Node_ID     Key of the record
	 *  @param  Parent_ID   Parent ID of the record
	 *  @param  SeqNo       Sort index
	 *  @param  onBar       Node also on Shortcut bar
	 */
	private void loadNode (PreparedStatement pstmt, MNode root,
		int Node_ID, int Parent_ID, int SeqNo, boolean onBar)
	{
		try
		{
			pstmt.setInt(1, Node_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				String Name = rs.getString(1);
				String Description = rs.getString(2);
				boolean IsSummary = rs.getString(3).equals("Y");
				String ImageIndicator = rs.getString(4);

				if (Name != null)
				{
					MNode child = new MNode (Node_ID, Name, Description, IsSummary, ImageIndicator);
					child.add(root, Parent_ID, child);
				}
			}
		//	else
		//		Log.trace(Log.l6_Database,"Not added", "Node_ID=" + Node_ID);
			rs.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadNode", e);
		}
	}   //  loadNode


	/**************************************************************************
	 *  Web Menu Tree Node
	 */
	class MNode
	{
		/**
		 *  Constructor
		 *  @param ID id
		 *  @param Name name
		 *  @param Description description
		 *  @param isSummary summary
		 *  @param Type type
		 */
		public MNode (int ID, String Name, String Description, boolean isSummary, String Type)
		{
			this.ID = ID;
			this.Name = Name;
			this.Description = Description;
			if (this.Description == null)
				this.Description = "";
			this.isSummary = isSummary;
			this.Type = Type;
			if (this.Type == null)
				this.Type = "";
		}

		public int          ID;
		public String       Name;
		public String       Description;
		public boolean      isSummary;
		public String       Type;
		public ArrayList    children = new ArrayList();

		/**
		 *  Add to list of children
		 *  @param child child
		 *  @return true
		 */
		public boolean add (MNode child)
		{
			children.add(child);
			return true;
		}   //  add

		/*********************************************************************/

		/**
		 *  Traverse Tree starting at root to add child to parent with parentID
		 *  @param root root
		 *  @param parentID parent
		 *  @param child child
		 *  @returns    false if not added
		 */
		public boolean add (MNode root, int parentID, MNode child)
		{
			//  is this root the parent?
			if (root.ID == parentID)
				return root.add(child);

			//  do we have children to check?
			else if (root.children.size() == 0)
				return false;

			//  check children
			for (int i = 0; i < root.children.size(); i++)
			{
				MNode cc = (MNode)root.children.get(i);
				if (root.add(cc, parentID, child))
					return true;
			}
			//  nothing found
			return false;
		}   //  add

		/**
		 *  Traverse Tree and print it
		 *  @param root root
		 *  @param buf buffer
		 */
		public void print (MNode root, StringBuffer buf)
		{
			//  Leaf
			if (root.children.size() == 0)
			{
				/**
				 *  <li id="menuXXXXX"><a href="...." onMouseOver="status='Menu Description';return true;">Menu Entry</a></li>
				 */
				String item = "";
				String servletName = "";
				if (root.Type.equals("W"))
				{
					item = "menuWindow";
					servletName = "WWindow";
				}
				else if (root.Type.equals("X"))
				{
					item = "menuWindow";
					servletName = "WForm";
				}
				else if (root.Type.equals("R"))
				{
					item = "menuReport";
					servletName = "WReport";
				}
				else if (root.Type.equals("P"))
				{
					item = "menuProcess";
					servletName = "WProcess";
				}
				else if (root.Type.equals("F"))
				{
					item = "menuWorkflow";
					servletName = "WWorkflow";
				}
				else if (root.Type.equals("T"))
				{
					item = "menuProcess";
					servletName = "WTask";
				}
				else
					servletName = "WError";

				String description = root.Description.replace('\'',' ').replace('"',' ');
				buf.append("<li id=\"" + item + "\"><a href=\"");
				//  url -   /appl/servletName?AD_Menu_ID=x
				buf.append(WEnv.getBaseDirectory(servletName))
					.append("?AD_Menu_ID=")
					.append(root.ID);
				//
				buf.append("\" onMouseOver=\"status='" + description + "';return true;\" onclick=showLoadingWindow(\"" + WEnv.getBaseDirectory("") + "\")>");
				buf.append(root.Name);
				buf.append("</a></li>\n");
			}
			else
			{
				/**
				 *  <li id="foldHeader">MenuEntry</li>
				 *  <ul style="display:none">
				 *  ....
				 *  </ul>
				 */
				buf.append("\n<li id=\"menuHeader\">");         //  summary node
				buf.append(root.Name);
				buf.append("</li>\n");
				//  Next Level
				buf.append("<ul style=\"display:none\">\n");    //  start next level
				for (int i = 0; i < root.children.size(); i++)
				{
					MNode cc = (MNode)root.children.get(i);
					root.print(cc, buf);
				}
				buf.append("</ul>");                                            //  finish next level
			}
		}	//  print

		/**
		 *  Clean tree of parents without children
		 *  @param root root node
		 */
		public void clean (MNode root)
		{
			int size = root.children.size();
			if (size == 0)
				return;
			//
			ArrayList temp = new ArrayList(size);
			boolean changed = false;
			for (int i = 0; i < size; i++)
			{
				MNode cc = (MNode)root.children.get(i);
				int ccSize = cc.children.size();
				if (cc.isSummary && ccSize == 0)
					changed = true;
				else
					temp.add(cc);
				if (ccSize != 0)
					cc.clean(cc);
			}
			if (changed)
				root.children = temp;
		}   //  clean

	}   //  MNode

}   //  WMenu
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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;

import org.apache.ecs.xhtml.*;

import org.compiere.model.*;
import org.compiere.util.*;

/**
 *  Web Menu
 *
 *  @author Jorg Janke
 *  @version  $Id: WMenu.java,v 1.3 2003/12/04 03:25:52 jjanke Exp $
 */
public class WMenu extends HttpServlet
{
	/**
	 *  Initialize global variables
	 *  @param config config
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("WMenu.init");
	}   //  init

	/**
	 * 	Get Servlet information
	 *  @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Menu";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		Log.trace(Log.l1_User, "WMenu.destroy");
		super.destroy();
	}   //  destroy

	/** */
	private PreparedStatement	m_pstmt;


	/*************************************************************************/

	/**
	 *  Process the HTTP Get request.
	 *  - Exit (Logout)
	 *  - AD_Window_ID Forward to Window
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WMenu.doGet - Process Menu Request");
		//  Get Parameter: Exit
		if (request.getParameter("Exit") != null)
		{
			WUtil.createLoginPage (request, response, this, null, "Exit");
			return;
		}

		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		if (ctx == null)
		{
			WUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Window
		int AD_Window_ID = WUtil.getParameterAsInt(request, "AD_Window_ID");

		//  Forward to WWindow
		if (AD_Window_ID != 0)
		{
			Log.trace(Log.l4_Data, "AD_Window_ID=" + AD_Window_ID);
			//
			String url = WEnv.getBaseDirectory("WWindow?AD_Window_ID=" + AD_Window_ID);
			Log.trace(Log.l4_Data, "Forward to - " + url);
			//
			RequestDispatcher rd = getServletContext().getRequestDispatcher(url);
			rd.forward(request, response);
			return;
		}

		//  Request not serviceable
		WUtil.createErrorPage(request, response, this, null, "NotImplemented");
	}   //  doGet


	/*************************************************************************/

	/**
	 *  Process the HTTP Post request - Verify Input & Create Menu
	 *
	 * @param request request
	 * @param response response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WMenu.doPost - Create Menu");
		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		Properties cProp = WUtil.getCookieProprties(request);
		if (ctx == null)
		{
			WUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Get Parameters: Role, Client, Org, Warehouse, Date
		String role = request.getParameter(WLogin.P_ROLE);
		String client = request.getParameter(WLogin.P_CLIENT);
		String org = request.getParameter(WLogin.P_ORG);
		String wh = request.getParameter(WLogin.P_WAREHOUSE);
		if (wh == null)
			wh = "";

		//  Get context
		if (role == null || client == null || org == null)
		{
			WUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Get Info from Context - User, Role, Client
		int AD_User_ID = Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID = Env.getContextAsInt(ctx, "#AD_Role_ID");
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		//  Not available in context yet - Org, Warehouse
		int AD_Org_ID = -1;
		int M_Warehouse_ID = -1;

		//  Get latest info from context
		try
		{
			int req_role = Integer.parseInt(role);
			if (req_role != AD_Role_ID)
			{
				Log.trace(Log.l5_DData, "AD_Role_ID - changed from " + AD_Role_ID);
				AD_Role_ID = req_role;
				Env.setContext(ctx, "#AD_Role_ID", AD_Role_ID);
			}
			Log.trace(Log.l5_DData, "AD_Role_ID = " + AD_Role_ID);
			//
			int req_client = Integer.parseInt(client);
			if (req_client != AD_Client_ID)
			{
				Log.trace(Log.l5_DData, "AD_Client_ID - changed from " + AD_Client_ID);
				AD_Client_ID = req_client;
				Env.setContext(ctx, "#AD_Client_ID", AD_Client_ID);
			}
			Log.trace(Log.l5_DData, "AD_Client_ID = " + AD_Client_ID);
			//
			AD_Org_ID = Integer.parseInt(org);
			Log.trace(Log.l5_DData, "AD_Org_ID = " + AD_Org_ID);
			//
			if (wh.length() > 0)
			{
				M_Warehouse_ID = Integer.parseInt(wh);
				Log.trace(Log.l5_DData, "M_Warehouse_ID = " + M_Warehouse_ID);
			}
		}
		catch (Exception e)
		{
			Log.error("WMenu.doPost - Parameter", e);
			WUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Check Login info and set environment
		String loginInfo = checkLogin (ctx, AD_User_ID, AD_Role_ID, AD_Client_ID, AD_Org_ID, M_Warehouse_ID);
		if (loginInfo == null)
		{
			WUtil.createErrorPage(request, response, this, ctx, Msg.getMsg(ctx, "RoleInconsistent"));
			return;
		}
		sess.setAttribute(WEnv.SA_LOGININFO, loginInfo);

		//  Set cookie for future defaults
		cProp.setProperty(WLogin.P_ROLE, String.valueOf(AD_Role_ID));
		cProp.setProperty(WLogin.P_CLIENT, String.valueOf(AD_Client_ID));
		cProp.setProperty(WLogin.P_ORG, String.valueOf(AD_Org_ID));
		if (M_Warehouse_ID == -1)
			cProp.setProperty(WLogin.P_WAREHOUSE, "");
		else
			cProp.setProperty(WLogin.P_WAREHOUSE, String.valueOf(M_Warehouse_ID));

		//  Set Date
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		//  Try to parse Date
		String dateString = request.getParameter(WLogin.P_DATE);
		try
		{
			if (dateString != null && dateString.length() > 0)
			{
				Language language = (Language)sess.getAttribute(WEnv.SA_LANGUAGE);
				DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
				java.util.Date date = df.parse(dateString);
				ts = new Timestamp(date.getTime());
			}
		}
		catch (Exception e)
		{
			Log.trace(Log.l1_User, "WMenu.doPost - Cannot parse date: " + dateString + " - " + e.getMessage());
		}
		Env.setContext(ctx, "#Date", ts.toString());    //  JDBC format


	//	Log.printProperties(System.getProperties(), "System");
	//	Log.printProperties(cProp, "Cookie");
	//	Log.printProperties(ctx, "Servlet Context");
	//	Log.printProperties(Env.getCtx(), "Apps Env Context");


		/**********************************************************************
		 *  Create Menu output
		 */
		String windowTitle = Msg.getMsg(ctx, "Menu");
		String statusMessage = Msg.getMsg(ctx, "SelectMenuItem");

		//	Document
		WDoc doc = WDoc.create (windowTitle);
		head header = doc.getHead();
		//  Target
		header.addElement(new base().setTarget(WEnv.TARGET_WINDOW));
		//  Specific Menu Script/Stylesheet
		header.addElement(new script("", WEnv.getBaseDirectory("menu.js"), "text/javascript", "JavaScript1.2"));
		header.addElement(new link().setRel("stylesheet").setHref(WEnv.getBaseDirectory("menu.css")));

		//	Body
		body body = doc.getBody();
		//  Header
		body.addElement(new cite(loginInfo));
		String title = windowTitle + " - " + loginInfo;
		body.addElement(new script("top.document.title='" + title + "';"));
		body.addElement(new script("defaultStatus = '" + statusMessage + "';"));

		//  Clear Window Frame
		body.addElement(WUtil.getClearFrame(WEnv.TARGET_WINDOW));

		//  Load Menu Structure     ----------------------
		int AD_Tree_ID = 10;	//	Menu
		MTree tree = new MTree (ctx, AD_Tree_ID, false);
		StringBuffer buf = new StringBuffer();
		Enumeration en = tree.getRoot().preorderEnumeration();
		//
		int oldLevel = 0;
		while (en.hasMoreElements())
		{
			MTreeNode nd = (MTreeNode)en.nextElement();

			//  Level
			int level = nd.getLevel();	//	0 == root
			if (level == 0)
				continue;
			while (oldLevel < level)
			{
				if (level > 1)
					buf.append("<ul style=\"display:none\">\n");//  start next level
				else
					buf.append("<ul>\n");						//  start next level
				oldLevel++;
			}
			while (oldLevel > level)
			{
				buf.append("</ul>");                            //  finish next level
				oldLevel--;
			}
				
			//	Print Node
			buf.append(printNode(nd));
		}
		while (oldLevel >  0)
		{
			buf.append("</ul>");	                            //  finish next level
			oldLevel--;
		}
		body.addElement(buf.toString());

		//  Exit Info
		body.addElement(new hr());
		String url = request.getRequestURI() + "?Exit=true";
		body.addElement(new a(url, Msg.getMsg(ctx, "Exit")));
System.out.println(doc);

		//  Can we store Cookie ?
		if (!cProp.getProperty(WLogin.P_STORE, "N").equals("Y"))
			cProp.clear();

		WUtil.createResponse (request, response, this, cProp, doc, false);
	}   //  doPost

	/**
	 *  Check Login information and set context.
	 *  @returns    true if login info are OK
	 *  @param ctx context
	 *  @param AD_User_ID user
	 *  @param AD_Role_ID role
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param M_Warehouse_ID warehouse
	 */
	private String checkLogin (Properties ctx, int AD_User_ID, int AD_Role_ID, int AD_Client_ID, int AD_Org_ID, int M_Warehouse_ID)
	{
		//  Get Login Info
		String loginInfo = null;
		//  Verify existance of User/Client/Org/Role and User's acces to Client & Org
		String sql = "SELECT u.Name || '@' || c.Name || '.' || o.Name || ' [' || INITCAP(USER) || ']' AS Text "
			+ "FROM AD_User u, AD_Client c, AD_Org o, AD_User_Roles ur "
			+ "WHERE u.AD_User_ID=?"    //  #1
			+ " AND c.AD_Client_ID=?"   //  #2
			+ " AND o.AD_Org_ID=?"      //  #3
			+ " AND ur.AD_Role_ID=?"    //  #4
			+ " AND ur.AD_User_ID=u.AD_User_ID"
			+ " AND (o.AD_Client_ID = 0 OR o.AD_Client_ID=c.AD_Client_ID)"
			+ " AND c.AD_Client_ID IN (SELECT AD_Client_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)"
			+ " AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			pstmt.setInt(2, AD_Client_ID);
			pstmt.setInt(3, AD_Org_ID);
			pstmt.setInt(4, AD_Role_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				loginInfo = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("WMenu.checkLogin", e);
		}

		//  not verified
		if (loginInfo == null)
			return null;

		//  Set Preferences
		KeyNamePair org = new KeyNamePair(AD_Org_ID, String.valueOf(AD_Org_ID));
		KeyNamePair wh = null;
		if (M_Warehouse_ID > 0)
			wh = new KeyNamePair(M_Warehouse_ID, String.valueOf(M_Warehouse_ID));
		//
		Timestamp date = null;
		String printer = null;
		DB.loadPreferences(ctx, org, wh, date, printer);
		//
		return loginInfo;
	}   //  checkLogin


	/**
	 *  Print Menu Item
	 *  @param node node
	 */
	public StringBuffer printNode (MTreeNode node)
	{
		StringBuffer sb = new StringBuffer();
		
		//  Leaf
		if (!node.isSummary())
		{
			/**
			 *  <li id="menuXXXXX"><a href="...." onMouseOver="status='Menu Description';return true;">Menu Entry</a></li>
			 */
			String item = "";
			String servletName = "";
			if (node.isWindow())
			{
				item = "menuWindow";
				servletName = "WWindow";
			}
			else if (node.isForm())
			{
				item = "menuWindow";
				servletName = "WForm";
			}
			else if (node.isReport())
			{
				item = "menuReport";
				servletName = "WReport";
			}
			else if (node.isProcess())
			{
				item = "menuProcess";
				servletName = "WProcess";
			}
			else if (node.isWorkFlow())
			{
				item = "menuWorkflow";
				servletName = "WWorkflow";
			}
			else if (node.isTask())
			{
				item = "menuProcess";
				servletName = "WTask";
			}
			else
				servletName = "WError";

			String description = node.getDescription().replace('\'',' ').replace('"',' ');
			sb.append("<li id=\"" + item + "\"><a href=\"")
				//  url -   /appl/servletName?AD_Menu_ID=x
				.append(WEnv.getBaseDirectory(servletName))
				.append("?AD_Menu_ID=")
				.append(node.getNode_ID())
				//
				.append("\" onMouseOver=\"status='" + description + "';return true;\" onclick=showLoadingWindow(\"" + WEnv.getBaseDirectory("") + "\")>")
				.append(node.getName())
				.append("</a></li>\n");
		}
		else
		{
			/**
			 *  <li id="foldHeader">MenuEntry</li>
			 *  <ul style="display:none">
			 *  ....
			 *  </ul>
			 */
			sb.append("\n<li id=\"menuHeader\">")		//  summary node
				.append(node.getName())
				.append("</li>\n");
		}
		return sb;
	}	//  printNode


}   //  WMenu
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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;

import org.apache.ecs.xhtml.*;

import org.compiere.util.*;

/**
 *  Web Menu
 *
 *  @author Jorg Janke
 *  @version  $Id: WMenu.java,v 1.2 2003/03/31 05:27:26 jjanke Exp $
 */
public class WMenu extends HttpServlet
{
	/**
	 *  Initialize global variables
	 *  @param config config
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WEnv.initWeb(config))
			throw new ServletException("WMenu.init");
	}   //  init

	/**
	 * 	Get Servlet information
	 *  @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Menu";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		Log.trace(Log.l1_User, "WMenu.destroy");
		super.destroy();
	}   //  destroy

	/** */
	private PreparedStatement	m_pstmt;


	/*************************************************************************/

	/**
	 *  Process the HTTP Get request.
	 *  - Exit (Logout)
	 *  - AD_Window_ID Forward to Window
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WMenu.doGet - Process Menu Request");
		//  Get Parameter: Exit
		if (request.getParameter("Exit") != null)
		{
			WUtil.createLoginPage (request, response, this, null, "Exit");
			return;
		}

		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		if (ctx == null)
		{
			WUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Window
		int AD_Window_ID = WUtil.getParameterAsInt(request, "AD_Window_ID");

		//  Forward to WWindow
		if (AD_Window_ID != 0)
		{
			Log.trace(Log.l4_Data, "AD_Window_ID=" + AD_Window_ID);
			//
			String url = WEnv.getBaseDirectory("WWindow?AD_Window_ID=" + AD_Window_ID);
			Log.trace(Log.l4_Data, "Forward to - " + url);
			//
			RequestDispatcher rd = getServletContext().getRequestDispatcher(url);
			rd.forward(request, response);
			return;
		}

		//  Request not serviceable
		WUtil.createErrorPage(request, response, this, null, "NotImplemented");
	}   //  doGet


	/*************************************************************************/

	/**
	 *  Process the HTTP Post request - Verify Input & Create Menu
	 *
	 * @param request request
	 * @param response response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		Log.trace(Log.l1_User, "WMenu.doPost - Create Menu");
		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WEnv.SA_CONTEXT);
		Properties cProp = WUtil.getCookieProprties(request);
		if (ctx == null)
		{
			WUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Get Parameters: Role, Client, Org, Warehouse, Date
		String role = request.getParameter(WLogin.P_ROLE);
		String client = request.getParameter(WLogin.P_CLIENT);
		String org = request.getParameter(WLogin.P_ORG);
		String wh = request.getParameter(WLogin.P_WAREHOUSE);
		if (wh == null)
			wh = "";

		//  Get context
		if (role == null || client == null || org == null)
		{
			WUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Get Info from Context - User, Role, Client
		int AD_User_ID = Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID = Env.getContextAsInt(ctx, "#AD_Role_ID");
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		//  Not available in context yet - Org, Warehouse
		int AD_Org_ID = -1;
		int M_Warehouse_ID = -1;

		//  Get latest info from context
		try
		{
			int req_role = Integer.parseInt(role);
			if (req_role != AD_Role_ID)
			{
				Log.trace(Log.l5_DData, "AD_Role_ID - changed from " + AD_Role_ID);
				AD_Role_ID = req_role;
				Env.setContext(ctx, "#AD_Role_ID", AD_Role_ID);
			}
			Log.trace(Log.l5_DData, "AD_Role_ID = " + AD_Role_ID);
			//
			int req_client = Integer.parseInt(client);
			if (req_client != AD_Client_ID)
			{
				Log.trace(Log.l5_DData, "AD_Client_ID - changed from " + AD_Client_ID);
				AD_Client_ID = req_client;
				Env.setContext(ctx, "#AD_Client_ID", AD_Client_ID);
			}
			Log.trace(Log.l5_DData, "AD_Client_ID = " + AD_Client_ID);
			//
			AD_Org_ID = Integer.parseInt(org);
			Log.trace(Log.l5_DData, "AD_Org_ID = " + AD_Org_ID);
			//
			if (wh.length() > 0)
			{
				M_Warehouse_ID = Integer.parseInt(wh);
				Log.trace(Log.l5_DData, "M_Warehouse_ID = " + M_Warehouse_ID);
			}
		}
		catch (Exception e)
		{
			Log.error("WMenu.doPost - Parameter", e);
			WUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Check Login info and set environment
		String loginInfo = checkLogin (ctx, AD_User_ID, AD_Role_ID, AD_Client_ID, AD_Org_ID, M_Warehouse_ID);
		if (loginInfo == null)
		{
			WUtil.createErrorPage(request, response, this, ctx, Msg.getMsg(ctx, "RoleInconsistent"));
			return;
		}
		sess.setAttribute(WEnv.SA_LOGININFO, loginInfo);

		//  Set cookie for future defaults
		cProp.setProperty(WLogin.P_ROLE, String.valueOf(AD_Role_ID));
		cProp.setProperty(WLogin.P_CLIENT, String.valueOf(AD_Client_ID));
		cProp.setProperty(WLogin.P_ORG, String.valueOf(AD_Org_ID));
		if (M_Warehouse_ID == -1)
			cProp.setProperty(WLogin.P_WAREHOUSE, "");
		else
			cProp.setProperty(WLogin.P_WAREHOUSE, String.valueOf(M_Warehouse_ID));

		//  Set Date
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		//  Try to parse Date
		String dateString = request.getParameter(WLogin.P_DATE);
		try
		{
			if (dateString != null && dateString.length() > 0)
			{
				Language language = (Language)sess.getAttribute(WEnv.SA_LANGUAGE);
				DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
				java.util.Date date = df.parse(dateString);
				ts = new Timestamp(date.getTime());
			}
		}
		catch (Exception e)
		{
			Log.trace(Log.l1_User, "WMenu.doPost - Cannot parse date: " + dateString + " - " + e.getMessage());
		}
		Env.setContext(ctx, "#Date", ts.toString());    //  JDBC format


	//	Log.printProperties(System.getProperties(), "System");
	//	Log.printProperties(cProp, "Cookie");
	//	Log.printProperties(ctx, "Servlet Context");
	//	Log.printProperties(Env.getCtx(), "Apps Env Context");


		/**********************************************************************
		 *  Create Menu output
		 */
		String windowTitle = Msg.getMsg(ctx, "Menu");
		String statusMessage = Msg.getMsg(ctx, "SelectMenuItem");

		//	Document
		WDoc doc = WDoc.create (windowTitle);
		head header = doc.getHead();
		//  Target
		header.addElement(new base().setTarget(WEnv.TARGET_WINDOW));
		//  Specific Menu Script/Stylesheet
		header.addElement(new script("", WEnv.getBaseDirectory("menu.js"), "text/javascript", "JavaScript1.2"));
		header.addElement(new link().setRel("stylesheet").setHref(WEnv.getBaseDirectory("menu.css")));

		//	Body
		body body = doc.getBody();
		//  Header
		body.addElement(new cite(loginInfo));
		String title = windowTitle + " - " + loginInfo;
		body.addElement(new script("top.document.title='" + title + "';"));
		body.addElement(new script("defaultStatus = '" + statusMessage + "';"));

		//  Clear Window Frame
		body.addElement(WUtil.getClearFrame(WEnv.TARGET_WINDOW));

		//  Load Menu Structure     ----------------------
		MNode root = loadTree(AD_Client_ID, AD_Role_ID, AD_User_ID, Env.getAD_Language(ctx));
		//
		StringBuffer buf = new StringBuffer();
		buf.append("<ul>");
		for (int i = 0; i < root.children.size(); i++)
		{
			MNode node = (MNode)root.children.get(i);
			node.print(node, buf);
		}
		buf.append("</ul>");
		body.addElement(buf.toString());

		//  Exit Info
		body.addElement(new hr());
		String url = request.getRequestURI() + "?Exit=true";
		body.addElement(new a(url, Msg.getMsg(ctx, "Exit")));

		//  Can we store Cookie ?
		if (!cProp.getProperty(WLogin.P_STORE, "N").equals("Y"))
			cProp.clear();

		WUtil.createResponse (request, response, this, cProp, doc, false);
	}   //  doPost

	/**
	 *  Check Login information and set context.
	 *  @returns    true if login info are OK
	 *  @param ctx context
	 *  @param AD_User_ID user
	 *  @param AD_Role_ID role
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param M_Warehouse_ID warehouse
	 */
	private String checkLogin (Properties ctx, int AD_User_ID, int AD_Role_ID, int AD_Client_ID, int AD_Org_ID, int M_Warehouse_ID)
	{
		//  Get Login Info
		String loginInfo = null;
		//  Verify existance of User/Client/Org/Role and User's acces to Client & Org
		String sql = "SELECT u.Name || '@' || c.Name || '.' || o.Name || ' [' || INITCAP(USER) || ']' AS Text "
			+ "FROM AD_User u, AD_Client c, AD_Org o, AD_User_Roles ur "
			+ "WHERE u.AD_User_ID=?"    //  #1
			+ " AND c.AD_Client_ID=?"   //  #2
			+ " AND o.AD_Org_ID=?"      //  #3
			+ " AND ur.AD_Role_ID=?"    //  #4
			+ " AND ur.AD_User_ID=u.AD_User_ID"
			+ " AND (o.AD_Client_ID = 0 OR o.AD_Client_ID=c.AD_Client_ID)"
			+ " AND c.AD_Client_ID IN (SELECT AD_Client_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)"
			+ " AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			pstmt.setInt(2, AD_Client_ID);
			pstmt.setInt(3, AD_Org_ID);
			pstmt.setInt(4, AD_Role_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				loginInfo = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("WMenu.checkLogin", e);
		}

		//  not verified
		if (loginInfo == null)
			return null;

		//  Set Preferences
		KeyNamePair org = new KeyNamePair(AD_Org_ID, String.valueOf(AD_Org_ID));
		KeyNamePair wh = null;
		if (M_Warehouse_ID > 0)
			wh = new KeyNamePair(M_Warehouse_ID, String.valueOf(M_Warehouse_ID));
		//
		Timestamp date = null;
		String printer = null;
		DB.loadPreferences(ctx, org, wh, date, printer);
		//
		return loginInfo;
	}   //  checkLogin


	/*************************************************************************/

	/**
	 *  Load Tree and return root node
	 *  @param AD_Client_ID client
	 *  @param AD_Role_ID role
	 *  @param AD_User_ID user
	 *  @param AD_Language language
	 *  @return node
	 */
	private MNode loadTree (int AD_Client_ID, int AD_Role_ID, int AD_User_ID, String AD_Language)
	{
		//	Get Tree info with start node
		int     AD_Tree_ID;
		String	Name;
		String	TreeType;
		String	Description;
		int 	startID;
		//
		MNode   root = null;

		//  Get Root Node
		String SQL = "SELECT t.AD_Tree_ID, t.Name, t.Description, t.TreeType, tn.Node_ID "
			+ "FROM AD_Tree t, AD_ClientInfo c, AD_TreeNode tn "
			+ "WHERE t.AD_Tree_ID=tn.AD_Tree_ID"
			+ " AND tn.Parent_ID IS NULL"
			+ " AND t.AD_Tree_ID=c.AD_Tree_Menu_ID"
			+ " AND c.AD_Client_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(SQL);
			pstmt.setInt(1, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next())
			{
				rs.close();
				pstmt.close();
				return null;
			}
			AD_Tree_ID = rs.getInt(1);
			Name = rs.getString(2);
			Description = rs.getString(3);
			TreeType = rs.getString(4);
			startID = rs.getInt(5);

			//	create root node
			root = new MNode (startID, Name, Description, true, "");
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadTree -1", e);
			return null;
		}

		//  SQL for TreeNodes
		StringBuffer cmd = new StringBuffer("SELECT tn.Node_ID,tn.Parent_ID,tn.SeqNo, "
			+ "(SELECT 'Y' FROM AD_TreeBar tb WHERE tb.AD_Tree_ID=tn.AD_Tree_ID AND tb.AD_User_ID=")
			.append(AD_User_ID).append(" AND tb.Node_ID=tn.Node_ID) "
			+ "FROM AD_TreeNode tn ")
			.append("WHERE tn.IsActive='Y' "
			+ "START WITH Parent_ID IS NULL AND AD_Tree_ID=? "
			+ "CONNECT BY Parent_ID=PRIOR Node_ID AND AD_Tree_ID=? "
			+ "ORDER BY LEVEL, SeqNo");

		//  SQL for Node Info
		StringBuffer cmdNode = new StringBuffer();
		boolean base = Env.isBaseLanguage(Env.getCtx(), "AD_Menu");
		if (base)
			cmdNode.append("SELECT m.Name,m.Description,m.IsSummary,m.Action "
				+ "FROM AD_Menu m");
		else
			cmdNode.append("SELECT t.Name,t.Description,m.IsSummary,m.Action "
				+ "FROM AD_Menu m, AD_Menu_Trl t");
			cmdNode.append(", (SELECT ").append(AD_Role_ID).append(" AS XRole FROM DUAL)");
		cmdNode.append(" WHERE m.AD_Menu_ID=?");
		if (!base)
			cmdNode.append(" AND m.AD_Menu_ID=t.AD_Menu_ID AND t.AD_Language='")
				.append(Env.getAD_Language(Env.getCtx())).append("'");

		cmdNode.append(" AND m.IsActive='Y' "
			+ "AND (m.IsSummary='Y' OR m.Action='B'"
			+ " OR EXISTS (SELECT * FROM AD_Window_Access wa WHERE wa.AD_Window_ID=m.AD_Window_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Process_Access wa WHERE wa.AD_Process_ID=m.AD_Process_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Form_Access wa WHERE wa.AD_Form_ID=m.AD_Form_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Task_Access wa WHERE wa.AD_Task_ID=m.AD_Task_ID AND wa.AD_Role_ID=XRole)"
			+ " OR EXISTS (SELECT * FROM AD_Workflow_Access wa WHERE wa.AD_Workflow_ID=m.AD_Workflow_ID AND wa.AD_Role_ID=XRole)"
			+ ")");
	//	Log.trace(Log.l6_Database, "SQL Tree", cmd.toString());
	//	Log.trace(Log.l6_Database, "SQL Node", cmdNode.toString());

		//  The Node Loop
		try
		{
			PreparedStatement pstmtNode = DB.prepareStatement(cmdNode.toString());
			//
			PreparedStatement pstmt = DB.prepareStatement(cmd.toString());
			pstmt.setInt(1, AD_Tree_ID);
			pstmt.setInt(2, AD_Tree_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int Node_ID = rs.getInt(1);
				int Parent_ID = rs.getInt(2);
				int SeqNo = rs.getInt(3);
				boolean onBar = (rs.getString(4) != null);
				loadNode (pstmtNode, root, Node_ID, Parent_ID, SeqNo, onBar);
			}
			rs.close();
			pstmt.close();
			pstmtNode.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadTree -2", e);
		}

		//  Clean Menu tree
		root.clean(root);

		return root;
	}   //  loadTree

	/**
	 *  Load Node using prepared statement
	 *  @param  pstmt       Prepared Statement requiring to set Node_ID and returning
	 *      Name,Description,IsSummary,ImageIndiactor
	 *  @param  root		root node
	 *  @param  Node_ID     Key of the record
	 *  @param  Parent_ID   Parent ID of the record
	 *  @param  SeqNo       Sort index
	 *  @param  onBar       Node also on Shortcut bar
	 */
	private void loadNode (PreparedStatement pstmt, MNode root,
		int Node_ID, int Parent_ID, int SeqNo, boolean onBar)
	{
		try
		{
			pstmt.setInt(1, Node_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				String Name = rs.getString(1);
				String Description = rs.getString(2);
				boolean IsSummary = rs.getString(3).equals("Y");
				String ImageIndicator = rs.getString(4);

				if (Name != null)
				{
					MNode child = new MNode (Node_ID, Name, Description, IsSummary, ImageIndicator);
					child.add(root, Parent_ID, child);
				}
			}
		//	else
		//		Log.trace(Log.l6_Database,"Not added", "Node_ID=" + Node_ID);
			rs.close();
		}
		catch (SQLException e)
		{
			Log.error("WMenu.loadNode", e);
		}
	}   //  loadNode


	/**************************************************************************
	 *  Web Menu Tree Node
	 */
	class MNode
	{
		/**
		 *  Constructor
		 *  @param ID id
		 *  @param Name name
		 *  @param Description description
		 *  @param isSummary summary
		 *  @param Type type
		 */
		public MNode (int ID, String Name, String Description, boolean isSummary, String Type)
		{
			this.ID = ID;
			this.Name = Name;
			this.Description = Description;
			if (this.Description == null)
				this.Description = "";
			this.isSummary = isSummary;
			this.Type = Type;
			if (this.Type == null)
				this.Type = "";
		}

		public int          ID;
		public String       Name;
		public String       Description;
		public boolean      isSummary;
		public String       Type;
		public ArrayList    children = new ArrayList();

		/**
		 *  Add to list of children
		 *  @param child child
		 *  @return true
		 */
		public boolean add (MNode child)
		{
			children.add(child);
			return true;
		}   //  add

		/*********************************************************************/

		/**
		 *  Traverse Tree starting at root to add child to parent with parentID
		 *  @param root root
		 *  @param parentID parent
		 *  @param child child
		 *  @returns    false if not added
		 */
		public boolean add (MNode root, int parentID, MNode child)
		{
			//  is this root the parent?
			if (root.ID == parentID)
				return root.add(child);

			//  do we have children to check?
			else if (root.children.size() == 0)
				return false;

			//  check children
			for (int i = 0; i < root.children.size(); i++)
			{
				MNode cc = (MNode)root.children.get(i);
				if (root.add(cc, parentID, child))
					return true;
			}
			//  nothing found
			return false;
		}   //  add

		/**
		 *  Traverse Tree and print it
		 *  @param root root
		 *  @param buf buffer
		 */
		public void print (MNode root, StringBuffer buf)
		{
			//  Leaf
			if (root.children.size() == 0)
			{
				/**
				 *  <li id="menuXXXXX"><a href="...." onMouseOver="status='Menu Description';return true;">Menu Entry</a></li>
				 */
				String item = "";
				String servletName = "";
				if (root.Type.equals("W"))
				{
					item = "menuWindow";
					servletName = "WWindow";
				}
				else if (root.Type.equals("X"))
				{
					item = "menuWindow";
					servletName = "WForm";
				}
				else if (root.Type.equals("R"))
				{
					item = "menuReport";
					servletName = "WReport";
				}
				else if (root.Type.equals("P"))
				{
					item = "menuProcess";
					servletName = "WProcess";
				}
				else if (root.Type.equals("F"))
				{
					item = "menuWorkflow";
					servletName = "WWorkflow";
				}
				else if (root.Type.equals("T"))
				{
					item = "menuProcess";
					servletName = "WTask";
				}
				else
					servletName = "WError";

				String description = root.Description.replace('\'',' ').replace('"',' ');
				buf.append("<li id=\"" + item + "\"><a href=\"");
				//  url -   /appl/servletName?AD_Menu_ID=x
				buf.append(WEnv.getBaseDirectory(servletName))
					.append("?AD_Menu_ID=")
					.append(root.ID);
				//
				buf.append("\" onMouseOver=\"status='" + description + "';return true;\" onclick=showLoadingWindow(\"" + WEnv.getBaseDirectory("") + "\")>");
				buf.append(root.Name);
				buf.append("</a></li>\n");
			}
			else
			{
				/**
				 *  <li id="foldHeader">MenuEntry</li>
				 *  <ul style="display:none">
				 *  ....
				 *  </ul>
				 */
				buf.append("\n<li id=\"menuHeader\">");         //  summary node
				buf.append(root.Name);
				buf.append("</li>\n");
				//  Next Level
				buf.append("<ul style=\"display:none\">\n");    //  start next level
				for (int i = 0; i < root.children.size(); i++)
				{
					MNode cc = (MNode)root.children.get(i);
					root.print(cc, buf);
				}
				buf.append("</ul>");                                            //  finish next level
			}
		}	//  print

		/**
		 *  Clean tree of parents without children
		 *  @param root root node
		 */
		public void clean (MNode root)
		{
			int size = root.children.size();
			if (size == 0)
				return;
			//
			ArrayList temp = new ArrayList(size);
			boolean changed = false;
			for (int i = 0; i < size; i++)
			{
				MNode cc = (MNode)root.children.get(i);
				int ccSize = cc.children.size();
				if (cc.isSummary && ccSize == 0)
					changed = true;
				else
					temp.add(cc);
				if (ccSize != 0)
					cc.clean(cc);
			}
			if (changed)
				root.children = temp;
		}   //  clean

	}   //  MNode

}   //  WMenu
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
package org.compiere.www;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;

import org.apache.ecs.xhtml.*;

import org.compiere.model.*;
import org.compiere.util.*;

/**
 *  Web Menu
 *
 *  @author Jorg Janke
 *  @version  $Id: WMenu.java,v 1.6 2004/09/10 02:54:23 jjanke Exp $
 */
public class WMenu extends HttpServlet
{
	/**	Logger			*/
	protected Logger	log = Logger.getCLogger(getClass());
	
	/**
	 *  Initialize global variables
	 *  @param config config
	 *  @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		if (!WebEnv.initWeb(config))
			throw new ServletException("WMenu.init");
	}   //  init

	/**
	 * 	Get Servlet information
	 *  @return servlet info
	 */
	public String getServletInfo()
	{
		return "Compiere Web Menu";
	}	//	getServletInfo

	/**
	 * Clean up resources
	 */
	public void destroy()
	{
		log.debug("destroy");
		super.destroy();
	}   //  destroy

	/** */
	private PreparedStatement	m_pstmt;


	
	/**************************************************************************
	 *  Process the HTTP Get request.
	 *  - Exit (Logout)
	 *  - AD_Window_ID Forward to Window
	 *
	 *  @param request request
	 *  @param response response
	 *  @throws ServletException
	 *  @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.debug("doGet - Process Menu Request");
		//  Get Parameter: Exit
		if (request.getParameter("Exit") != null)
		{
			WebUtil.createLoginPage (request, response, this, null, "Exit");
			return;
		}

		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WebEnv.SA_CONTEXT);
		if (ctx == null)
		{
			WebUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Window
		int AD_Window_ID = WebUtil.getParameterAsInt(request, "AD_Window_ID");

		//  Forward to WWindow
		if (AD_Window_ID != 0)
		{
			log.debug("doGet - AD_Window_ID=" + AD_Window_ID);
			//
			String url = WebEnv.getBaseDirectory("WWindow?AD_Window_ID=" + AD_Window_ID);
			log.debug("doGet - Forward to=" + url);
			//
			RequestDispatcher rd = getServletContext().getRequestDispatcher(url);
			rd.forward(request, response);
			return;
		}

		//  Request not serviceable
		WebUtil.createErrorPage(request, response, this, null, "NotImplemented");
	}   //  doGet


	/**************************************************************************
	 *  Process the HTTP Post request - Verify Input & Create Menu
	 *
	 * @param request request
	 * @param response response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		log.debug("doPost - Create Menu");
		//  Get Session attributes
		HttpSession sess = request.getSession();
		Properties ctx = (Properties)sess.getAttribute(WebEnv.SA_CONTEXT);
		Properties cProp = WebUtil.getCookieProprties(request);
		if (ctx == null)
		{
			WebUtil.createTimeoutPage(request, response, this, null, null);
			return;
		}

		//  Get Parameters: Role, Client, Org, Warehouse, Date
		String role = request.getParameter(WLogin.P_ROLE);
		String client = request.getParameter(WLogin.P_CLIENT);
		String org = request.getParameter(WLogin.P_ORG);
		String wh = request.getParameter(WLogin.P_WAREHOUSE);
		if (wh == null)
			wh = "";

		//  Get context
		if (role == null || client == null || org == null)
		{
			WebUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Get Info from Context - User, Role, Client
		int AD_User_ID = Env.getContextAsInt(ctx, "#AD_User_ID");
		int AD_Role_ID = Env.getContextAsInt(ctx, "#AD_Role_ID");
		int AD_Client_ID = Env.getContextAsInt(ctx, "#AD_Client_ID");
		//  Not available in context yet - Org, Warehouse
		int AD_Org_ID = -1;
		int M_Warehouse_ID = -1;

		//  Get latest info from context
		try
		{
			int req_role = Integer.parseInt(role);
			if (req_role != AD_Role_ID)
			{
				log.debug("doPost - AD_Role_ID - changed from " + AD_Role_ID);
				AD_Role_ID = req_role;
				Env.setContext(ctx, "#AD_Role_ID", AD_Role_ID);
			}
			log.debug("doPost - AD_Role_ID = " + AD_Role_ID);
			//
			int req_client = Integer.parseInt(client);
			if (req_client != AD_Client_ID)
			{
				log.debug("doPost - AD_Client_ID - changed from " + AD_Client_ID);
				AD_Client_ID = req_client;
				Env.setContext(ctx, "#AD_Client_ID", AD_Client_ID);
			}
			log.debug("doPost - AD_Client_ID = " + AD_Client_ID);
			//
			AD_Org_ID = Integer.parseInt(org);
			log.debug("doPost - AD_Org_ID = " + AD_Org_ID);
			//
			if (wh.length() > 0)
			{
				M_Warehouse_ID = Integer.parseInt(wh);
				log.debug("doPost - M_Warehouse_ID = " + M_Warehouse_ID);
			}
		}
		catch (Exception e)
		{
			log.error("doPost - Parameter", e);
			WebUtil.createTimeoutPage(request, response, this, ctx, Msg.getMsg(ctx, "ParameterMissing"));
			return;
		}

		//  Check Login info and set environment
		String loginInfo = checkLogin (ctx, AD_User_ID, AD_Role_ID, AD_Client_ID, AD_Org_ID, M_Warehouse_ID);
		if (loginInfo == null)
		{
			WebUtil.createErrorPage(request, response, this, ctx, Msg.getMsg(ctx, "RoleInconsistent"));
			return;
		}
		sess.setAttribute(WebEnv.SA_LOGININFO, loginInfo);

		//  Set cookie for future defaults
		cProp.setProperty(WLogin.P_ROLE, String.valueOf(AD_Role_ID));
		cProp.setProperty(WLogin.P_CLIENT, String.valueOf(AD_Client_ID));
		cProp.setProperty(WLogin.P_ORG, String.valueOf(AD_Org_ID));
		if (M_Warehouse_ID == -1)
			cProp.setProperty(WLogin.P_WAREHOUSE, "");
		else
			cProp.setProperty(WLogin.P_WAREHOUSE, String.valueOf(M_Warehouse_ID));

		//  Set Date
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		//  Try to parse Date
		String dateString = request.getParameter(WLogin.P_DATE);
		try
		{
			if (dateString != null && dateString.length() > 0)
			{
				Language language = (Language)sess.getAttribute(WebEnv.SA_LANGUAGE);
				DateFormat df = DisplayType.getDateFormat(DisplayType.Date, language);
				java.util.Date date = df.parse(dateString);
				ts = new Timestamp(date.getTime());
			}
		}
		catch (Exception e)
		{
			log.warn("doPost - Cannot parse date: " + dateString + " - " + e.getMessage());
		}
		Env.setContext(ctx, "#Date", ts.toString());    //  JDBC format


	//	log.printProperties(System.getProperties(), "System");
	//	log.printProperties(cProp, "Cookie");
	//	log.printProperties(ctx, "Servlet Context");
	//	log.printProperties(Env.getCtx(), "Apps Env Context");


		/**********************************************************************
		 *  Create Menu output
		 */
		String windowTitle = Msg.getMsg(ctx, "Menu");
		String statusMessage = Msg.getMsg(ctx, "SelectMenuItem");

		//	Document
		WebDoc doc = WebDoc.create (windowTitle);
		head header = doc.getHead();
		//  Target
		header.addElement(new base().setTarget(WebEnv.TARGET_WINDOW));
		//  Specific Menu Script/Stylesheet
		header.addElement(new script("", WebEnv.getBaseDirectory("menu.js"), "text/javascript", "JavaScript1.2"));
		header.addElement(new link().setRel("stylesheet").setHref(WebEnv.getBaseDirectory("menu.css")));

		//	Body
		body body = doc.getBody();
		//  Header
		body.addElement(new cite(loginInfo));
		String title = windowTitle + " - " + loginInfo;
		body.addElement(new script("top.document.title='" + title + "';"));
		body.addElement(new script("defaultStatus = '" + statusMessage + "';"));

		//  Clear Window Frame
		body.addElement(WebUtil.getClearFrame(WebEnv.TARGET_WINDOW));

		//  Load Menu Structure     ----------------------
		int AD_Tree_ID = 10;	//	Menu
		MTree tree = new MTree (ctx, AD_Tree_ID, false);	// Language set in WLogin
		StringBuffer buf = new StringBuffer();
		Enumeration en = tree.getRoot().preorderEnumeration();
		//
		int oldLevel = 0;
		while (en.hasMoreElements())
		{
			MTreeNode nd = (MTreeNode)en.nextElement();
			if (nd.isTask() || nd.isForm() || nd.isWorkbench() || nd.isWorkFlow()
				|| nd.isProcess() || nd.isReport())
				continue;

			//  Level
			int level = nd.getLevel();	//	0 == root
			if (level == 0)
				continue;
			//
			while (oldLevel < level)
			{
				if (level > 1)
					buf.append("<ul style=\"display:none\">\n");//  start next level
				else
					buf.append("<ul>\n");						//  start next level
				oldLevel++;
			}
			while (oldLevel > level)
			{
				buf.append("</ul>");                            //  finish next level
				oldLevel--;
			}
				
			//	Print Node
			buf.append(printNode(nd));
		}
		while (oldLevel >  0)
		{
			buf.append("</ul>");	                            //  finish next level
			oldLevel--;
		}
		body.addElement(buf.toString());

		//  Exit Info
		body.addElement(new hr());
		String url = request.getRequestURI() + "?Exit=true";
		body.addElement(new a(url, Msg.getMsg(ctx, "Exit")));
//System.out.println(doc);

		//  Can we store Cookie ?
		if (!cProp.getProperty(WLogin.P_STORE, "N").equals("Y"))
			cProp.clear();

		WebUtil.createResponse (request, response, this, cProp, doc, false);
	}   //  doPost

	/**
	 *  Check Login information and set context.
	 *  @returns    true if login info are OK
	 *  @param ctx context
	 *  @param AD_User_ID user
	 *  @param AD_Role_ID role
	 *  @param AD_Client_ID client
	 *  @param AD_Org_ID org
	 *  @param M_Warehouse_ID warehouse
	 */
	private String checkLogin (Properties ctx, int AD_User_ID, int AD_Role_ID, int AD_Client_ID, int AD_Org_ID, int M_Warehouse_ID)
	{
		//  Get Login Info
		String loginInfo = null;
		//  Verify existance of User/Client/Org/Role and User's acces to Client & Org
		String sql = "SELECT u.Name || '@' || c.Name || '.' || o.Name || ' [' || INITCAP(USER) || ']' AS Text "
			+ "FROM AD_User u, AD_Client c, AD_Org o, AD_User_Roles ur "
			+ "WHERE u.AD_User_ID=?"    //  #1
			+ " AND c.AD_Client_ID=?"   //  #2
			+ " AND o.AD_Org_ID=?"      //  #3
			+ " AND ur.AD_Role_ID=?"    //  #4
			+ " AND ur.AD_User_ID=u.AD_User_ID"
			+ " AND (o.AD_Client_ID = 0 OR o.AD_Client_ID=c.AD_Client_ID)"
			+ " AND c.AD_Client_ID IN (SELECT AD_Client_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)"
			+ " AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ca WHERE ca.AD_Role_ID=ur.AD_Role_ID)";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_User_ID);
			pstmt.setInt(2, AD_Client_ID);
			pstmt.setInt(3, AD_Org_ID);
			pstmt.setInt(4, AD_Role_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				loginInfo = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.error ("checkLogin", e);
		}

		//  not verified
		if (loginInfo == null)
			return null;

		//  Set Preferences
		KeyNamePair org = new KeyNamePair(AD_Org_ID, String.valueOf(AD_Org_ID));
		KeyNamePair wh = null;
		if (M_Warehouse_ID > 0)
			wh = new KeyNamePair(M_Warehouse_ID, String.valueOf(M_Warehouse_ID));
		//
		Timestamp date = null;
		String printer = null;
		DB.loadPreferences(ctx, org, wh, date, printer);
		//
		return loginInfo;
	}   //  checkLogin


	/**
	 *  Print Menu Item
	 *  @param node node
	 */
	public StringBuffer printNode (MTreeNode node)
	{
		StringBuffer sb = new StringBuffer();
		
		//  Leaf
		if (!node.isSummary())
		{
			/**
			 *  <li id="menuXXXXX"><a href="...." onMouseOver="status='Menu Description';return true;">Menu Entry</a></li>
			 */
			String item = "";
			String servletName = "";
			if (node.isWindow())
			{
				item = "menuWindow";
				servletName = "WWindow";
			}
			else if (node.isForm())
			{
				item = "menuWindow";
				servletName = "WForm";
			}
			else if (node.isReport())
			{
				item = "menuReport";
				servletName = "WReport";
			}
			else if (node.isProcess())
			{
				item = "menuProcess";
				servletName = "WProcess";
			}
			else if (node.isWorkFlow())
			{
				item = "menuWorkflow";
				servletName = "WWorkflow";
			}
			else if (node.isTask())
			{
				item = "menuProcess";
				servletName = "WTask";
			}
			else
				servletName = "WError";

			String description = node.getDescription().replace('\'',' ').replace('"',' ');
			sb.append("<li id=\"" + item + "\"><a href=\"")
				//  url -   /appl/servletName?AD_Menu_ID=x
				.append(WebEnv.getBaseDirectory(servletName))
				.append("?AD_Menu_ID=")
				.append(node.getNode_ID())
				//
				.append("\" onMouseOver=\"status='" + description + "';return true;\" onclick=showLoadingWindow(\"" + WebEnv.getBaseDirectory("") + "\")>")
				.append(node.getName())		//	language set in MTree.getNodeDetails based on ctx
				.append("</a></li>\n");
		}
		else
		{
			/**
			 *  <li id="foldHeader">MenuEntry</li>
			 *  <ul style="display:none">
			 *  ....
			 *  </ul>
			 */
			sb.append("\n<li id=\"menuHeader\">")		//  summary node
				.append(node.getName())
				.append("</li>\n");
		}
		return sb;
	}	//  printNode


}   //  WMenu
