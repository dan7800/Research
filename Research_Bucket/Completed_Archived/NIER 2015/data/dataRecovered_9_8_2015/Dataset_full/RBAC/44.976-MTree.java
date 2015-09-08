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

import java.sql.*;
import java.util.*;

import org.compiere.util.*;

/**
 *  Builds Tree.
 *  Creates tree structure - maintained in VTreePanel
 *
 *  @author     Jorg Janke
 *  @version    $Id: MTree.java,v 1.2 2003/02/19 06:48:23 jjanke Exp $
 */
public class MTree
{
	/**
	 *  Construct Tree
	 *  @param AD_Tree_ID   The tree to build
	 *  @param editable     True, if tree can be modified
	 *  - includes inactive and empty summary nodes
	 */
	public MTree (int AD_Tree_ID, boolean editable)
	{
		this (AD_Tree_ID, 0, 0, editable);
	}	//	MTree

	/**
	 *  Construct Tree
	 *  @param AD_Tree_ID   The tree to build
	 *  @param AD_User_ID   Tree Bar Access control - optional
	 *  @param AD_Role_ID   Menu Access control
	 *  @param editable     True, if tree can be modified
	 *  - includes inactive and empty summary nodes
	 */
	public MTree (int AD_Tree_ID,
		int AD_User_ID, int AD_Role_ID, boolean editable)
	{
		Log.trace(Log.l4_Data, "MTree - AD_Tree_ID=" + AD_Tree_ID,
			"AD_Role_ID=" + AD_Role_ID + ", AD_User_ID=" + AD_User_ID
			+ ", Editable=" + editable);

		m_AD_Tree_ID = AD_Tree_ID;
		m_AD_User_ID = AD_User_ID;
		m_AD_Role_ID = AD_Role_ID;
		m_editable = editable;
		loadTree();
	}   //  MTree

	/** Tree ID             */
	private int         m_AD_Tree_ID;
	/** Tree for User       */
	private int         m_AD_User_ID;
	/** Role Access Control */
	private int         m_AD_Role_ID;
	/** Is Tree editable    */
	private boolean     m_editable;

	/** Tree Name           */
	private String      m_name;
	/** Tree Description    */
	private String	    m_description;
	/** Tree Type           */
	private String      m_treeType;

	/** Table Name          */
	private String      m_tableName = "AD_TreeNode";

	/** Root Node                   */
	private MTreeNode           m_root = null;
	/** Buffer while loading tree   */
	private ArrayList           m_buffer = new ArrayList();
	/** Prepared Statement for Node Details */
	private PreparedStatement   m_pstmtDetail;

	/*************************************************************************/

	/** Tree Type Menu MM   */
	public static final String	TREETYPE_Menu =         "MM";
	/** Tree Type Account ElementValue EV   */
	public static final String	TREETYPE_ElementValue = "EV";
	/** Tree Type Product PR   */
	public static final String	TREETYPE_Product =      "PR";
	/** Tree Type BusinessPartner BP   */
	public static final String	TREETYPE_BPartner =     "BP";
	/** Tree Type Organization OO   */
	public static final String	TREETYPE_Org =          "OO";
	/** Tree Type Project PJ   */
	public static final String	TREETYPE_Project =      "PJ";
	/** Tree Type ProductCategory PC   */
	public static final String	TREETYPE_ProductCategory = "PC";
	/** Tree Type BOM BB   */
	public static final String	TREETYPE_BOM =          "BB";
	/** Tree Type SalesRegion SR   */
	public static final String	TREETYPE_SalesRegion =  "SR";
	/** Tree Type Camoaign MC   */
	public static final String	TREETYPE_Campaign =     "MC";
	/** Tree Type Activity AY   */
	public static final String	TREETYPE_Activity =     "AY";

	/*************************************************************************/

	/**
	 *  Get primary AD_Tree_ID for KeyColumn.
	 *  Called from GridController
	 *  @param keyColumnName key column name, eg. C_Project_ID
	 *  @return AD_Tree_ID
	 */
	public static int getAD_Tree_ID (String keyColumnName)
	{
		Log.trace(Log.l4_Data, "MTree.getAD_Tree_ID", keyColumnName);
		if (keyColumnName == null || keyColumnName.length() == 0)
			return 0;

		String TreeType = null;
		if (keyColumnName.equals("AD_Menu_ID"))
			TreeType = MTree.TREETYPE_Menu;
		else if (keyColumnName.equals("C_ElementValue_ID"))
			TreeType = MTree.TREETYPE_ElementValue;
		else if (keyColumnName.equals("M_Product_ID"))
			TreeType = MTree.TREETYPE_Product;
		else if (keyColumnName.equals("C_BPartner_ID"))
			TreeType = MTree.TREETYPE_BPartner;
		else if (keyColumnName.equals("AD_Org_ID"))
			TreeType = MTree.TREETYPE_Org;
		else if (keyColumnName.equals("C_Project_ID"))
			TreeType = MTree.TREETYPE_Project;
		else if (keyColumnName.equals("M_ProductCategory_ID"))
			TreeType = MTree.TREETYPE_ProductCategory;
		else if (keyColumnName.equals("M_BOM_ID"))
			TreeType = MTree.TREETYPE_BOM;
		else if (keyColumnName.equals("C_SalesRegion_ID"))
			TreeType = MTree.TREETYPE_SalesRegion;
		else if (keyColumnName.equals("C_Campaign_ID"))
			TreeType = MTree.TREETYPE_Campaign;
		else if (keyColumnName.equals("C_Activity_ID"))
			TreeType = MTree.TREETYPE_Activity;
		else
		{
			Log.error("MTree.getAD_Tree_ID - Could not map " + keyColumnName);
			return 0;
		}

		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getAD_Tree_ID", e);
		}

		return AD_Tree_ID;
	}   //  getAD_Tree_ID


	/**
	 * 	Get Primary Tree of Tree Type
	 * 	@param TreeType see TREETYPE_
	 * 	@return MTree
	 */
	public static MTree getTree (String TreeType)
	{
		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getTree", e);
		}
		//	Not found
		if (AD_Tree_ID == 0)
		{
			Log.trace(Log.l4_Data, "MTree.getTree - No AD_Tree_ID for TreeType=" + TreeType + ", AD_Client_ID=" + AD_Client_ID);
			return null;
		}
		//
		MTree tree = new MTree (AD_Tree_ID, false);
		return tree;
	}	//	getTree


	/*************************************************************************/

	/**
	 *  Load Tree
	 */
	private void loadTree()
	{
		//  Get Tree info
		String sql = "SELECT Name, Description, TreeType "
			+ "FROM AD_Tree "
			+ "WHERE AD_Tree_ID=?"
			+ " AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, m_AD_Tree_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				m_name = rs.getString(1);
				m_description = rs.getString(2);
				if (m_description == null)
					m_description = "";
				m_treeType = rs.getString(3);
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.loadTree", e);
			return;
		}
		loadNodes();
	}   //  loadTree

	/**
	 *  Load Nodes
	 */
	private void loadNodes()
	{
		//  TableName: AD_TreeNode
		if (m_treeType.equals(TREETYPE_Menu))
			;   //  m_tableName += "MM";
		else if  (m_treeType.equals(TREETYPE_BPartner))
			m_tableName += "BP";
		else if  (m_treeType.equals(TREETYPE_Product))
			m_tableName += "PR";

		//  SQL for TreeNodes
		StringBuffer cmd = new StringBuffer("SELECT "
			+ "tn.Node_ID,tn.Parent_ID,tn.SeqNo,tb.IsActive "
			+ "FROM ").append(m_tableName).append(" tn"
			+ " LEFT OUTER JOIN AD_TreeBar tb ON (tn.AD_Tree_ID=tb.AD_Tree_ID"
			+ " AND tn.Node_ID=tb.Node_ID AND tb.AD_User_ID=?) "	//	#1
			+ "WHERE tn.AD_Tree_ID=?");								//	#2
		if (!m_editable)
			cmd.append(" AND tn.IsActive='Y'");
		cmd.append(" ORDER BY COALESCE(tn.Parent_ID, -1), tn.SeqNo");
		Log.trace(Log.l6_Database, "MTree.loadNodes", cmd.toString());

		//  The Node Loop
		try
		{
			m_pstmtDetail = DB.prepareStatement(prepareNodeDetail());
			//
			PreparedStatement pstmt = DB.prepareStatement(cmd.toString());
			pstmt.setInt(1, m_AD_User_ID);
			pstmt.setInt(2, m_AD_Tree_ID);
			//
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int node_ID = rs.getInt(1);
				int parent_ID = rs.getInt(2);
				int seqNo = rs.getInt(3);
				boolean onBar = (rs.getString(4) != null);
				//
				if (node_ID == 0 && parent_ID == 0)
					m_root = new MTreeNode (node_ID, 0, m_name, m_description, 0, true, null, onBar);
				else
					addToTree (node_ID, parent_ID, seqNo, onBar);
			}
			rs.close();
			pstmt.close();
			//
			m_pstmtDetail.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.loadNodes", e);
		}
		//  Done with loading - add remainder from buffer
		if (m_buffer.size() != 0)
		{
			Log.trace(Log.l6_Database, "MTree.loadNodes - clearing buffer");
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				MTreeNode parent = m_root.findNode(node.getParent_ID());
				if (parent != null && parent.getAllowsChildren())
				{
					parent.add(node);
					checkBuffer(node);
					m_buffer.remove(i);
					i = -1;
				}
			}
		}

		//	Nodes w/o parent
		if (m_buffer.size() != 0)
		{
			Log.error ("MTree.loadNodes - Nodes w/o parent - adding to root - " + m_buffer);
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				m_root.add(node);
				checkBuffer(node);
				m_buffer.remove(i);
				i = -1;
			}
			if (m_buffer.size() != 0)
				Log.error ("MTree.loadNodes - still nodes in Buffer - " + m_buffer);
		}	//	nodes w/o parents

		//  clean up
		if (!m_editable && m_root.getChildCount() > 0)
			trimTree();
//		diagPrintTree();
	}   //  loadNodes

	/**
	 *  Add Node to Tree.
	 *  If not found add to buffer
	 *  @param node_ID Node_ID
	 *  @param parent_ID Parent_ID
	 *  @param seqNo SeqNo
	 *  @param onBar on bar
	 */
	private void addToTree (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		//  Create new Node
		MTreeNode child = getNodeDetail (node_ID, parent_ID, seqNo, onBar);
		if (child == null)
			return;

		//  Add to Tree
		MTreeNode parent = null;
		if (m_root != null)
			parent = m_root.findNode (parent_ID);
		//  Parent found
		if (parent != null && parent.getAllowsChildren())
		{
			parent.add(child);
			//  see if we can add nodes from buffer
			if (m_buffer.size() > 0)
				checkBuffer(child);
		}
		else
			m_buffer.add(child);
	}   //  addToTree

	/**
	 *  Check the buffer for nodes which have newNode as Parents
	 *  @param newNode new node
	 */
	private void checkBuffer (MTreeNode newNode)
	{
		for (int i = 0; i < m_buffer.size(); i++)
		{
			MTreeNode node = (MTreeNode)m_buffer.get(i);
			if (node.getParent_ID() == newNode.getID())
			{
				newNode.add(node);
				m_buffer.remove(i);
				i--;
			}
		}
	}   //  checkBuffer

	/*************************************************************************/

	/**
	 *  Prepare Node Detail.
	 *  Columns:
	 *  - Name
	 *  - Description
	 *  - IsSummary
	 *  - ImageIndicator
	 *  Parameter:
	 *  - Node_ID
	 *  The SQL contains security/access control
	 *  @return SQL to get Node Detail Info
	 */
	private String prepareNodeDetail ()
	{
		//  SQL for Node Info
		StringBuffer cmdNode = new StringBuffer();
		if (m_treeType.equals(MTree.TREETYPE_Menu))
		{
			boolean base = Env.isBaseLanguage(Env.getCtx(), "AD_Menu");
			if (base)
				cmdNode.append("SELECT m.Name,m.Description,m.IsSummary,m.Action "
					+ "FROM AD_Menu m");
			else
				cmdNode.append("SELECT t.Name,t.Description,m.IsSummary,m.Action "
					+ "FROM AD_Menu m, AD_Menu_Trl t");
			if (!m_editable)
				cmdNode.append(", (SELECT ").append(m_AD_Role_ID).append(" AS XRole FROM DUAL) x");
			cmdNode.append(" WHERE m.AD_Menu_ID=?");            //  #1
			if (!base)
				cmdNode.append(" AND m.AD_Menu_ID=t.AD_Menu_ID AND t.AD_Language='")
					.append(Env.getAD_Language(Env.getCtx())).append("'");
			if (!m_editable)
				cmdNode.append(" AND m.IsActive='Y' "
					+ "AND (m.IsSummary='Y' OR m.Action='B'"
					+ " OR EXISTS (SELECT * FROM AD_Window_Access wa WHERE wa.AD_Window_ID=m.AD_Window_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Process_Access wa WHERE wa.AD_Process_ID=m.AD_Process_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Form_Access wa WHERE wa.AD_Form_ID=m.AD_Form_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Task_Access wa WHERE wa.AD_Task_ID=m.AD_Task_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Workflow_Access wa WHERE wa.AD_Workflow_ID=m.AD_Workflow_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ ")");
		}
		else
		{
			cmdNode.append("SELECT Name,Description,IsSummary,NULL FROM ");
			//
			String sourceTable = null;
			if (m_treeType.equals(MTree.TREETYPE_Org))
				sourceTable = "AD_Org";
			else if (m_treeType.equals(MTree.TREETYPE_Product))
				sourceTable = "M_Product";
			else if (m_treeType.equals(MTree.TREETYPE_ProductCategory))
				sourceTable = "M_Product_Category";
			else if (m_treeType.equals(MTree.TREETYPE_BOM))
				sourceTable = "M_BOM";
			else if (m_treeType.equals(MTree.TREETYPE_ElementValue))
				sourceTable = "C_ElementValue";
			else if (m_treeType.equals(MTree.TREETYPE_BPartner))
				sourceTable = "C_BPartner";
			else if (m_treeType.equals(MTree.TREETYPE_Campaign))
				sourceTable = "C_Campaign";
			else if (m_treeType.equals(MTree.TREETYPE_Project))
				sourceTable = "C_Project";
			else if (m_treeType.equals(MTree.TREETYPE_Activity))
				sourceTable = "C_Activity";
			else if (m_treeType.equals(MTree.TREETYPE_SalesRegion))
				sourceTable = "C_SalesRegion";
			if (sourceTable == null)
			{
				throw new IllegalArgumentException("MTree.prepareNodeDetail - Unknown TreeType=" + m_treeType);
			}
			cmdNode.append(sourceTable).append(" WHERE ")
				.append(sourceTable).append("_ID=?");               // #1
			if (!m_editable)
				cmdNode.append(" AND IsActive='Y'");
		}
		Log.trace(Log.l6_Database, "MTree.prepareNodeDetail", cmdNode.toString());
		return cmdNode.toString();
	}   //  prepareNodeDetail

	/**
	 *  Get Node Details.
	 *  As SQL contains security access, not all nodes will be found
	 *  @param  node_ID     Key of the record
	 *  @param  parent_ID   Parent ID of the record
	 *  @param  seqNo       Sort index
	 *  @param  onBar       Node also on Shortcut bar
	 *  @return Node
	 */
	private MTreeNode getNodeDetail (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		MTreeNode retValue = null;
		try
		{
			m_pstmtDetail.setInt(1, node_ID);
			ResultSet rs = m_pstmtDetail.executeQuery();
			if (rs.next())
			{
				retValue = new MTreeNode (node_ID, seqNo,
					rs.getString(1),                //  name
					rs.getString(2),                //  description
					parent_ID,
					"Y".equals(rs.getString(3)),    //  IsSummary
					rs.getString(4),                //  ImageIndicator
					onBar);
			}
			rs.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getNodeDetail", e);
		}
		return retValue;
	}   //  getNodeDetails

	/*************************************************************************/

	/**
	 *  Trim tree of empty summary nodes
	 */
	private void trimTree()
	{
		boolean needsTrim = m_root != null;
		while (needsTrim)
		{
			needsTrim = false;
			Enumeration en = m_root.preorderEnumeration();
			while (en.hasMoreElements())
			{
				MTreeNode nd = (MTreeNode)en.nextElement();
				if (nd.isSummary() && nd.getChildCount() == 0)
				{
					nd.removeFromParent();
					needsTrim = true;
				}
			}
		}
	}   //  trimTree

	/**
	 *  Diagnostics: Print tree
	 */
	private void diagPrintTree()
	{
		Enumeration en = m_root.preorderEnumeration();
		int count = 0;
		while (en.hasMoreElements())
		{
			StringBuffer sb = new StringBuffer();
			MTreeNode nd = (MTreeNode)en.nextElement();
			for (int i = 0; i < nd.getLevel(); i++)
				sb.append(" ");
			sb.append("ID=").append(nd.getID())
				.append(", SeqNo=").append(nd.getSeqNo())
				.append(" ").append(nd.getName());
			System.out.println(sb.toString());
			count++;
		}
		System.out.println("Count=" + count);
	}   //  diagPrintTree

	/**
	 *  Get Root node
	 *  @return root
	 */
	public MTreeNode getRoot()
	{
		return m_root;
	}   //  getRoot

	/**
	 *  Get Table Name
	 *  @return Table Name
	 */
	public String getTableName()
	{
		return m_tableName;
	}   //  getTableName

	/**
	 *  String representation
	 *  @return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MTree[");
		sb.append("AD_Tree_ID=").append(m_AD_Tree_ID)
			.append(", Name=").append(m_name);
		sb.append("]");
		return sb.toString();
	}
}   //  MTree
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

import java.awt.*;
import java.sql.*;
import java.util.*;

import javax.sql.*;

import org.compiere.print.*;
import org.compiere.util.*;

/**
 *  Builds Tree.
 *  Creates tree structure - maintained in VTreePanel
 *
 *  @author     Jorg Janke
 *  @version    $Id: MTree.java,v 1.24 2004/08/14 21:48:10 jjanke Exp $
 */
public class MTree extends MTree_Base
{
	/**
	 *  Default Constructor.
	 * 	Need to call loadNodes explicitly
	 * 	@param ctx context for security
	 *  @param AD_Tree_ID   The tree to build
	 */
	public MTree (Properties ctx, int AD_Tree_ID)
	{
		super (ctx, AD_Tree_ID);
	}   //  MTree

	/**
	 *  Construct & Load Tree
	 *  @param AD_Tree_ID   The tree to build
	 *  @param editable     True, if tree can be modified
	 *  - includes inactive and empty summary nodes
	 * 	@param ctx context for security
	 */
	public MTree (Properties ctx, int AD_Tree_ID, boolean editable)
	{
		this (ctx, AD_Tree_ID);
		m_editable = editable;
		int AD_User_ID = Env.getContextAsInt(ctx, "AD_User_ID");
		log.info("AD_Tree_ID=" + AD_Tree_ID
			+ ", AD_User_ID=" + AD_User_ID 
			+ ", Editable=" + editable);
		//
		loadNodes(AD_User_ID);
	}   //  MTree


	/** Is Tree editable    	*/
	private boolean     		m_editable = false;
	/** Root Node                   */
	private MTreeNode           m_root = null;
	/** Buffer while loading tree   */
	private ArrayList           m_buffer = new ArrayList();
	/** Prepared Statement for Node Details */
	private RowSet			   	m_nodeRowSet;

	
	/**************************************************************************
	 *  Get primary AD_Tree_ID for KeyColumn.
	 *  Called from GridController
	 *  @param keyColumnName key column name, eg. C_Project_ID
	 *  @return AD_Tree_ID
	 */
	public static int getAD_Tree_ID (String keyColumnName)
	{
		Log.trace(Log.l4_Data, "MTree.getAD_Tree_ID", keyColumnName);
		if (keyColumnName == null || keyColumnName.length() == 0)
			return 0;

		String TreeType = null;
		if (keyColumnName.equals("AD_Menu_ID"))
			TreeType = TREETYPE_Menu; 
		else if (keyColumnName.equals("C_ElementValue_ID"))
			TreeType = TREETYPE_ElementValue;
		else if (keyColumnName.equals("M_Product_ID"))
			TreeType = TREETYPE_Product;
		else if (keyColumnName.equals("C_BPartner_ID"))
			TreeType = TREETYPE_BPartner;
		else if (keyColumnName.equals("AD_Org_ID"))
			TreeType = TREETYPE_Organization;
		else if (keyColumnName.equals("C_Project_ID"))
			TreeType = TREETYPE_Project;
		else if (keyColumnName.equals("M_ProductCategory_ID"))
			TreeType = TREETYPE_ProductCategory;
		else if (keyColumnName.equals("M_BOM_ID"))
			TreeType = TREETYPE_BoM;
		else if (keyColumnName.equals("C_SalesRegion_ID"))
			TreeType = TREETYPE_SalesRegion;
		else if (keyColumnName.equals("C_Campaign_ID"))
			TreeType = TREETYPE_Campaign;
		else if (keyColumnName.equals("C_Activity_ID"))
			TreeType = TREETYPE_Activity;
		else
		{
			Log.error("MTree.getAD_Tree_ID - Could not map " + keyColumnName);
			return 0;
		}

		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getAD_Tree_ID", e);
		}

		return AD_Tree_ID;
	}   //  getAD_Tree_ID


	/**
	 * 	Get Primary Tree of Tree Type
	 * 	@param TreeType see TREETYPE_
	 * 	@param ctx context for Security
	 * 	@return MTree
	 */
	public static MTree getTree (Properties ctx, String TreeType)
	{
		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getTree", e);
		}
		//	Not found
		if (AD_Tree_ID == 0)
		{
			Log.trace(Log.l4_Data, "MTree.getTree - No AD_Tree_ID for TreeType=" + TreeType + ", AD_Client_ID=" + AD_Client_ID);
			return null;
		}
		//
		MTree tree = new MTree (ctx, AD_Tree_ID, false);
		return tree;
	}	//	getTree


	/*************************************************************************
	 *  Load Nodes and Bar
	 * 	@param AD_User_ID user
	 */
	private void loadNodes (int AD_User_ID)
	{
		//  SQL for TreeNodes
		StringBuffer sql = new StringBuffer("SELECT "
			+ "tn.Node_ID,tn.Parent_ID,tn.SeqNo,tb.IsActive "
			+ "FROM ").append(getNodeTableName()).append(" tn"
			+ " LEFT OUTER JOIN AD_TreeBar tb ON (tn.AD_Tree_ID=tb.AD_Tree_ID"
			+ " AND tn.Node_ID=tb.Node_ID AND tb.AD_User_ID=?) "	//	#1
			+ "WHERE tn.AD_Tree_ID=?");								//	#2
		if (!m_editable)
			sql.append(" AND tn.IsActive='Y'");
		sql.append(" ORDER BY COALESCE(tn.Parent_ID, -1), tn.SeqNo");
		if (Log.isTraceLevel(8))
			log.debug("loadNodes - " + sql.toString());

		//  The Node Loop
		try
		{
			// load Node details - addToTree -> getNodeDetail
			getNodeDetails(); 
			//
			PreparedStatement pstmt = DB.prepareStatement(sql.toString());
			pstmt.setInt(1, AD_User_ID);
			pstmt.setInt(2, getAD_Tree_ID());
			//	Get Tree & Bar
			ResultSet rs = pstmt.executeQuery();
			m_root = new MTreeNode (0, 0, getName(), getDescription(), 0, true, null, false, null);
			while (rs.next())
			{
				int node_ID = rs.getInt(1);
				int parent_ID = rs.getInt(2);
				int seqNo = rs.getInt(3);
				boolean onBar = (rs.getString(4) != null);
				//
				if (node_ID == 0 && parent_ID == 0)
					;
				else
					addToTree (node_ID, parent_ID, seqNo, onBar);	//	calls getNodeDetail
			}
			rs.close();
			pstmt.close();
			//
			m_nodeRowSet.close();
			m_nodeRowSet = null;
		}
		catch (SQLException e)
		{
			log.error("loadNodes", e);
			m_nodeRowSet = null;
		}
			
		//  Done with loading - add remainder from buffer
		if (m_buffer.size() != 0)
		{
			if (Log.isTraceLevel(8))
				log.debug("loadNodes - clearing buffer - Adding to: " + m_root);
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				MTreeNode parent = m_root.findNode(node.getParent_ID());
				if (parent != null && parent.getAllowsChildren())
				{
					parent.add(node);
					checkBuffer(node);
					m_buffer.remove(i);
					i = -1;		//	start again with i=0
				}
			}
		}

		//	Nodes w/o parent
		if (m_buffer.size() != 0)
		{
			log.error ("loadNodes - Nodes w/o parent - adding to root - " + m_buffer);
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				m_root.add(node);
				checkBuffer(node);
				m_buffer.remove(i);
				i = -1;
			}
			if (m_buffer.size() != 0)
				log.error ("loadNodes - still nodes in Buffer - " + m_buffer);
		}	//	nodes w/o parents

		//  clean up
		if (!m_editable && m_root.getChildCount() > 0)
			trimTree();
//		diagPrintTree();
		if (Log.isTraceLevel(8) || m_root.getChildCount() == 0)
			log.debug("loadTree - ChildCount=" + m_root.getChildCount());
	}   //  loadNodes

	/**
	 *  Add Node to Tree.
	 *  If not found add to buffer
	 *  @param node_ID Node_ID
	 *  @param parent_ID Parent_ID
	 *  @param seqNo SeqNo
	 *  @param onBar on bar
	 */
	private void addToTree (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		//  Create new Node
		MTreeNode child = getNodeDetail (node_ID, parent_ID, seqNo, onBar);
		if (child == null)
			return;

		//  Add to Tree
		MTreeNode parent = null;
		if (m_root != null)
			parent = m_root.findNode (parent_ID);
		//  Parent found
		if (parent != null && parent.getAllowsChildren())
		{
			parent.add(child);
			//  see if we can add nodes from buffer
			if (m_buffer.size() > 0)
				checkBuffer(child);
		}
		else
			m_buffer.add(child);
	}   //  addToTree

	/**
	 *  Check the buffer for nodes which have newNode as Parents
	 *  @param newNode new node
	 */
	private void checkBuffer (MTreeNode newNode)
	{
		//	Ability to add nodes
		if (!newNode.isSummary() || !newNode.getAllowsChildren())
			return;
		//
		for (int i = 0; i < m_buffer.size(); i++)
		{
			MTreeNode node = (MTreeNode)m_buffer.get(i);
			if (node.getParent_ID() == newNode.getNode_ID())
			{
				newNode.add(node);
				m_buffer.remove(i);
				i--;
			}
		}
	}   //  checkBuffer

	
	
	/**************************************************************************
	 *  Get Node Detail.
	 * 	Loads data into RowSet m_nodeRowSet
	 *  Columns:
	 * 	- ID
	 *  - Name
	 *  - Description
	 *  - IsSummary
	 *  - ImageIndicator
	 * 	- additional for Menu
	 *  Parameter:
	 *  - Node_ID
	 *  The SQL contains security/access control
	 *  @return SQL to get Node Detail Info
	 */
	private void getNodeDetails ()
	{
		//  SQL for Node Info
		StringBuffer sqlNode = new StringBuffer();
		String sourceTable = "t";
		String fromClause = getSourceTableName(false);	//	fully qualified
		String columnNameX = getSourceTableName(true);
		String color = getActionColorName();
		if (getTreeType().equals(TREETYPE_Menu))
		{
			boolean base = Env.isBaseLanguage(Env.getCtx(), "AD_Menu");
			sourceTable = "m";
			if (base)
				sqlNode.append("SELECT m.AD_Menu_ID, m.Name,m.Description,m.IsSummary,m.Action, "
					+ "m.AD_Window_ID, m.AD_Process_ID, m.AD_Form_ID, m.AD_Workflow_ID, m.AD_Task_ID, m.AD_Workbench_ID "
					+ "FROM AD_Menu m");
			else
				sqlNode.append("SELECT m.AD_Menu_ID,  t.Name,t.Description,m.IsSummary,m.Action, "
					+ "m.AD_Window_ID, m.AD_Process_ID, m.AD_Form_ID, m.AD_Workflow_ID, m.AD_Task_ID, m.AD_Workbench_ID "
					+ "FROM AD_Menu m, AD_Menu_Trl t");
			if (!base)
				sqlNode.append(" WHERE m.AD_Menu_ID=t.AD_Menu_ID AND t.AD_Language='")
					.append(Env.getAD_Language(Env.getCtx())).append("'");
			if (!m_editable)
			{
				boolean hasWhere = sqlNode.indexOf(" WHERE ") != -1;
				sqlNode.append(hasWhere ? " AND " : " WHERE ").append("m.IsActive='Y' ");
			}
			//	Do not show Beta
			if (!MClient.get(getCtx()).isUseBetaFunctions())
			{
				boolean hasWhere = sqlNode.indexOf(" WHERE ") != -1;
				sqlNode.append(hasWhere ? " AND " : " WHERE ");
				sqlNode.append("(m.AD_Window_ID IS NULL OR EXISTS (SELECT * FROM AD_Window w WHERE m.AD_Window_ID=w.AD_Window_ID AND w.IsBetaFunctionality='N'))")
					.append(" AND (m.AD_Process_ID IS NULL OR EXISTS (SELECT * FROM AD_Process p WHERE m.AD_Process_ID=p.AD_Process_ID AND p.IsBetaFunctionality='N'))")
					.append(" AND (m.AD_Form_ID IS NULL OR EXISTS (SELECT * FROM AD_Form f WHERE m.AD_Form_ID=f.AD_Form_ID AND f.IsBetaFunctionality='N'))");
			}
		}
		else
		{
			if (columnNameX == null)
				throw new IllegalArgumentException("MTree.prepareNodeDetail - Unknown TreeType=" + getTreeType());
			sqlNode.append("SELECT t.").append(columnNameX)
				.append("_ID,t.Name,t.Description,t.IsSummary,").append(color)
				.append(" FROM ").append(fromClause);
			if (!m_editable)
				sqlNode.append(" WHERE t.IsActive='Y'");
		}
		String sql = sqlNode.toString();
		if (!m_editable)	//	editable = menu/etc. window
			sql = MRole.getDefault(getCtx(), false).addAccessSQL(sql, 
				sourceTable, MRole.SQL_FULLYQUALIFIED, m_editable);
		log.debug("getNodeDetails - " + sql);
		m_nodeRowSet = DB.getRowSet (sql, true);
	}   //  getNodeDetails

	/**
	 *  Get Node Details.
	 *  As SQL contains security access, not all nodes will be found
	 *  @param  node_ID     Key of the record
	 *  @param  parent_ID   Parent ID of the record
	 *  @param  seqNo       Sort index
	 *  @param  onBar       Node also on Shortcut bar
	 *  @return Node
	 */
	private MTreeNode getNodeDetail (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		MTreeNode retValue = null;
		try
		{
			m_nodeRowSet.beforeFirst();
			while (m_nodeRowSet.next())
			{
				int node = m_nodeRowSet.getInt(1);				
				if (node_ID != node)	//	search for correct one
					continue;
				//	ID,Name,Description,IsSummary,Action/Color
				int index = 2;				
				String name = m_nodeRowSet.getString(index++); 
				String description = m_nodeRowSet.getString(index++);
				boolean isSummary = "Y".equals(m_nodeRowSet.getString(index++));
				String actionColor = m_nodeRowSet.getString(index++);
				//	Menu only
				if (getTreeType().equals(TREETYPE_Menu) && !isSummary)
				{
					int AD_Window_ID = m_nodeRowSet.getInt(index++);
					int AD_Process_ID = m_nodeRowSet.getInt(index++);
					int AD_Form_ID = m_nodeRowSet.getInt(index++);
					int AD_Workflow_ID = m_nodeRowSet.getInt(index++);
					int AD_Task_ID = m_nodeRowSet.getInt(index++);
					int AD_Workbench_ID = m_nodeRowSet.getInt(index++);
					//
					MRole role = MRole.getDefault(getCtx(), false);
					Boolean access = null;
					if (X_AD_Menu.ACTION_Window.equals(actionColor))
						access = role.getWindowAccess(AD_Window_ID);
					else if (X_AD_Menu.ACTION_Process.equals(actionColor) 
						|| X_AD_Menu.ACTION_Report.equals(actionColor))
						access = role.getProcessAccess(AD_Process_ID);
					else if (X_AD_Menu.ACTION_Form.equals(actionColor))
						access = role.getFormAccess(AD_Form_ID);
					else if (X_AD_Menu.ACTION_WorkFlow.equals(actionColor))
						access = role.getWorkflowAccess(AD_Workflow_ID);
					else if (X_AD_Menu.ACTION_Task.equals(actionColor))
						access = role.getTaskAccess(AD_Task_ID);
				//	else if (X_AD_Menu.ACTION_Workbench.equals(action))
				//		access = role.getWorkbenchAccess(AD_Window_ID);
				//	log.debug("getNodeDetail - " + name + " - " + actionColor + " - " + access);
					//
					if (access != null		//	rw or ro for Role 
						|| m_editable)		//	Menu Window can see all
					{
						retValue = new MTreeNode (node_ID, seqNo,
							name, description, parent_ID, isSummary,
							actionColor, onBar, null);	//	menu has no color
					}
				}
				else	//	always add
				{
					Color color = null;	//	action
					MPrintColor printColor = MPrintColor.get(getCtx(), actionColor);
					if (printColor != null)
						color = printColor.getColor(); 
					//
					retValue = new MTreeNode (node_ID, seqNo,
						name, description, parent_ID, isSummary,
						null, onBar, color);
				}
			}
		}
		catch (SQLException e)
		{
			log.error("getNodeDetails", e);
		}
		return retValue;
	}   //  getNodeDetails

	/*************************************************************************/

	/**
	 *  Trim tree of empty summary nodes
	 */
	private void trimTree()
	{
		boolean needsTrim = m_root != null;
		while (needsTrim)
		{
			needsTrim = false;
			Enumeration en = m_root.preorderEnumeration();
			while (m_root.getChildCount() > 0 && en.hasMoreElements())
			{
				MTreeNode nd = (MTreeNode)en.nextElement();
				if (nd.isSummary() && nd.getChildCount() == 0)
				{
					nd.removeFromParent();
					needsTrim = true;
				}
			}
		}
	}   //  trimTree

	/**
	 *  Diagnostics: Print tree
	 */
	private void dumpTree()
	{
		Enumeration en = m_root.preorderEnumeration();
		int count = 0;
		while (en.hasMoreElements())
		{
			StringBuffer sb = new StringBuffer();
			MTreeNode nd = (MTreeNode)en.nextElement();
			for (int i = 0; i < nd.getLevel(); i++)
				sb.append(" ");
			sb.append("ID=").append(nd.getNode_ID())
				.append(", SeqNo=").append(nd.getSeqNo())
				.append(" ").append(nd.getName());
			System.out.println(sb.toString());
			count++;
		}
		System.out.println("Count=" + count);
	}   //  diagPrintTree

	/**
	 *  Get Root node
	 *  @return root
	 */
	public MTreeNode getRoot()
	{
		return m_root;
	}   //  getRoot

	/**
	 * 	Is Menu Tree
	 *	@return true if menu
	 */
	public boolean isMenu()
	{
		return TREETYPE_Menu.equals(getTreeType());
	}	//	isMenu

	/**
	 * 	Is Product Tree
	 *	@return true if product
	 */
	public boolean isProduct()
	{
		return TREETYPE_Product.equals(getTreeType());
	}	//	isProduct
	
	/**
	 * 	Is Business Partner Tree
	 *	@return true if partner
	 */
	public boolean isBPartner()
	{
		return TREETYPE_BPartner.equals(getTreeType());
	}	//	isBPartner
	
	/**
	 *  String representation
	 *  @return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MTree[");
		sb.append("AD_Tree_ID=").append(getAD_Tree_ID())
			.append(", Name=").append(getName());
		sb.append("]");
		return sb.toString();
	}
}   //  MTree
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

import java.sql.*;
import java.util.*;

import org.compiere.util.*;

/**
 *  Builds Tree.
 *  Creates tree structure - maintained in VTreePanel
 *
 *  @author     Jorg Janke
 *  @version    $Id: MTree.java,v 1.2 2003/02/19 06:48:23 jjanke Exp $
 */
public class MTree
{
	/**
	 *  Construct Tree
	 *  @param AD_Tree_ID   The tree to build
	 *  @param editable     True, if tree can be modified
	 *  - includes inactive and empty summary nodes
	 */
	public MTree (int AD_Tree_ID, boolean editable)
	{
		this (AD_Tree_ID, 0, 0, editable);
	}	//	MTree

	/**
	 *  Construct Tree
	 *  @param AD_Tree_ID   The tree to build
	 *  @param AD_User_ID   Tree Bar Access control - optional
	 *  @param AD_Role_ID   Menu Access control
	 *  @param editable     True, if tree can be modified
	 *  - includes inactive and empty summary nodes
	 */
	public MTree (int AD_Tree_ID,
		int AD_User_ID, int AD_Role_ID, boolean editable)
	{
		Log.trace(Log.l4_Data, "MTree - AD_Tree_ID=" + AD_Tree_ID,
			"AD_Role_ID=" + AD_Role_ID + ", AD_User_ID=" + AD_User_ID
			+ ", Editable=" + editable);

		m_AD_Tree_ID = AD_Tree_ID;
		m_AD_User_ID = AD_User_ID;
		m_AD_Role_ID = AD_Role_ID;
		m_editable = editable;
		loadTree();
	}   //  MTree

	/** Tree ID             */
	private int         m_AD_Tree_ID;
	/** Tree for User       */
	private int         m_AD_User_ID;
	/** Role Access Control */
	private int         m_AD_Role_ID;
	/** Is Tree editable    */
	private boolean     m_editable;

	/** Tree Name           */
	private String      m_name;
	/** Tree Description    */
	private String	    m_description;
	/** Tree Type           */
	private String      m_treeType;

	/** Table Name          */
	private String      m_tableName = "AD_TreeNode";

	/** Root Node                   */
	private MTreeNode           m_root = null;
	/** Buffer while loading tree   */
	private ArrayList           m_buffer = new ArrayList();
	/** Prepared Statement for Node Details */
	private PreparedStatement   m_pstmtDetail;

	/*************************************************************************/

	/** Tree Type Menu MM   */
	public static final String	TREETYPE_Menu =         "MM";
	/** Tree Type Account ElementValue EV   */
	public static final String	TREETYPE_ElementValue = "EV";
	/** Tree Type Product PR   */
	public static final String	TREETYPE_Product =      "PR";
	/** Tree Type BusinessPartner BP   */
	public static final String	TREETYPE_BPartner =     "BP";
	/** Tree Type Organization OO   */
	public static final String	TREETYPE_Org =          "OO";
	/** Tree Type Project PJ   */
	public static final String	TREETYPE_Project =      "PJ";
	/** Tree Type ProductCategory PC   */
	public static final String	TREETYPE_ProductCategory = "PC";
	/** Tree Type BOM BB   */
	public static final String	TREETYPE_BOM =          "BB";
	/** Tree Type SalesRegion SR   */
	public static final String	TREETYPE_SalesRegion =  "SR";
	/** Tree Type Camoaign MC   */
	public static final String	TREETYPE_Campaign =     "MC";
	/** Tree Type Activity AY   */
	public static final String	TREETYPE_Activity =     "AY";

	/*************************************************************************/

	/**
	 *  Get primary AD_Tree_ID for KeyColumn.
	 *  Called from GridController
	 *  @param keyColumnName key column name, eg. C_Project_ID
	 *  @return AD_Tree_ID
	 */
	public static int getAD_Tree_ID (String keyColumnName)
	{
		Log.trace(Log.l4_Data, "MTree.getAD_Tree_ID", keyColumnName);
		if (keyColumnName == null || keyColumnName.length() == 0)
			return 0;

		String TreeType = null;
		if (keyColumnName.equals("AD_Menu_ID"))
			TreeType = MTree.TREETYPE_Menu;
		else if (keyColumnName.equals("C_ElementValue_ID"))
			TreeType = MTree.TREETYPE_ElementValue;
		else if (keyColumnName.equals("M_Product_ID"))
			TreeType = MTree.TREETYPE_Product;
		else if (keyColumnName.equals("C_BPartner_ID"))
			TreeType = MTree.TREETYPE_BPartner;
		else if (keyColumnName.equals("AD_Org_ID"))
			TreeType = MTree.TREETYPE_Org;
		else if (keyColumnName.equals("C_Project_ID"))
			TreeType = MTree.TREETYPE_Project;
		else if (keyColumnName.equals("M_ProductCategory_ID"))
			TreeType = MTree.TREETYPE_ProductCategory;
		else if (keyColumnName.equals("M_BOM_ID"))
			TreeType = MTree.TREETYPE_BOM;
		else if (keyColumnName.equals("C_SalesRegion_ID"))
			TreeType = MTree.TREETYPE_SalesRegion;
		else if (keyColumnName.equals("C_Campaign_ID"))
			TreeType = MTree.TREETYPE_Campaign;
		else if (keyColumnName.equals("C_Activity_ID"))
			TreeType = MTree.TREETYPE_Activity;
		else
		{
			Log.error("MTree.getAD_Tree_ID - Could not map " + keyColumnName);
			return 0;
		}

		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getAD_Tree_ID", e);
		}

		return AD_Tree_ID;
	}   //  getAD_Tree_ID


	/**
	 * 	Get Primary Tree of Tree Type
	 * 	@param TreeType see TREETYPE_
	 * 	@return MTree
	 */
	public static MTree getTree (String TreeType)
	{
		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getTree", e);
		}
		//	Not found
		if (AD_Tree_ID == 0)
		{
			Log.trace(Log.l4_Data, "MTree.getTree - No AD_Tree_ID for TreeType=" + TreeType + ", AD_Client_ID=" + AD_Client_ID);
			return null;
		}
		//
		MTree tree = new MTree (AD_Tree_ID, false);
		return tree;
	}	//	getTree


	/*************************************************************************/

	/**
	 *  Load Tree
	 */
	private void loadTree()
	{
		//  Get Tree info
		String sql = "SELECT Name, Description, TreeType "
			+ "FROM AD_Tree "
			+ "WHERE AD_Tree_ID=?"
			+ " AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, m_AD_Tree_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				m_name = rs.getString(1);
				m_description = rs.getString(2);
				if (m_description == null)
					m_description = "";
				m_treeType = rs.getString(3);
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.loadTree", e);
			return;
		}
		loadNodes();
	}   //  loadTree

	/**
	 *  Load Nodes
	 */
	private void loadNodes()
	{
		//  TableName: AD_TreeNode
		if (m_treeType.equals(TREETYPE_Menu))
			;   //  m_tableName += "MM";
		else if  (m_treeType.equals(TREETYPE_BPartner))
			m_tableName += "BP";
		else if  (m_treeType.equals(TREETYPE_Product))
			m_tableName += "PR";

		//  SQL for TreeNodes
		StringBuffer cmd = new StringBuffer("SELECT "
			+ "tn.Node_ID,tn.Parent_ID,tn.SeqNo,tb.IsActive "
			+ "FROM ").append(m_tableName).append(" tn"
			+ " LEFT OUTER JOIN AD_TreeBar tb ON (tn.AD_Tree_ID=tb.AD_Tree_ID"
			+ " AND tn.Node_ID=tb.Node_ID AND tb.AD_User_ID=?) "	//	#1
			+ "WHERE tn.AD_Tree_ID=?");								//	#2
		if (!m_editable)
			cmd.append(" AND tn.IsActive='Y'");
		cmd.append(" ORDER BY COALESCE(tn.Parent_ID, -1), tn.SeqNo");
		Log.trace(Log.l6_Database, "MTree.loadNodes", cmd.toString());

		//  The Node Loop
		try
		{
			m_pstmtDetail = DB.prepareStatement(prepareNodeDetail());
			//
			PreparedStatement pstmt = DB.prepareStatement(cmd.toString());
			pstmt.setInt(1, m_AD_User_ID);
			pstmt.setInt(2, m_AD_Tree_ID);
			//
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int node_ID = rs.getInt(1);
				int parent_ID = rs.getInt(2);
				int seqNo = rs.getInt(3);
				boolean onBar = (rs.getString(4) != null);
				//
				if (node_ID == 0 && parent_ID == 0)
					m_root = new MTreeNode (node_ID, 0, m_name, m_description, 0, true, null, onBar);
				else
					addToTree (node_ID, parent_ID, seqNo, onBar);
			}
			rs.close();
			pstmt.close();
			//
			m_pstmtDetail.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.loadNodes", e);
		}
		//  Done with loading - add remainder from buffer
		if (m_buffer.size() != 0)
		{
			Log.trace(Log.l6_Database, "MTree.loadNodes - clearing buffer");
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				MTreeNode parent = m_root.findNode(node.getParent_ID());
				if (parent != null && parent.getAllowsChildren())
				{
					parent.add(node);
					checkBuffer(node);
					m_buffer.remove(i);
					i = -1;
				}
			}
		}

		//	Nodes w/o parent
		if (m_buffer.size() != 0)
		{
			Log.error ("MTree.loadNodes - Nodes w/o parent - adding to root - " + m_buffer);
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				m_root.add(node);
				checkBuffer(node);
				m_buffer.remove(i);
				i = -1;
			}
			if (m_buffer.size() != 0)
				Log.error ("MTree.loadNodes - still nodes in Buffer - " + m_buffer);
		}	//	nodes w/o parents

		//  clean up
		if (!m_editable && m_root.getChildCount() > 0)
			trimTree();
//		diagPrintTree();
	}   //  loadNodes

	/**
	 *  Add Node to Tree.
	 *  If not found add to buffer
	 *  @param node_ID Node_ID
	 *  @param parent_ID Parent_ID
	 *  @param seqNo SeqNo
	 *  @param onBar on bar
	 */
	private void addToTree (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		//  Create new Node
		MTreeNode child = getNodeDetail (node_ID, parent_ID, seqNo, onBar);
		if (child == null)
			return;

		//  Add to Tree
		MTreeNode parent = null;
		if (m_root != null)
			parent = m_root.findNode (parent_ID);
		//  Parent found
		if (parent != null && parent.getAllowsChildren())
		{
			parent.add(child);
			//  see if we can add nodes from buffer
			if (m_buffer.size() > 0)
				checkBuffer(child);
		}
		else
			m_buffer.add(child);
	}   //  addToTree

	/**
	 *  Check the buffer for nodes which have newNode as Parents
	 *  @param newNode new node
	 */
	private void checkBuffer (MTreeNode newNode)
	{
		for (int i = 0; i < m_buffer.size(); i++)
		{
			MTreeNode node = (MTreeNode)m_buffer.get(i);
			if (node.getParent_ID() == newNode.getID())
			{
				newNode.add(node);
				m_buffer.remove(i);
				i--;
			}
		}
	}   //  checkBuffer

	/*************************************************************************/

	/**
	 *  Prepare Node Detail.
	 *  Columns:
	 *  - Name
	 *  - Description
	 *  - IsSummary
	 *  - ImageIndicator
	 *  Parameter:
	 *  - Node_ID
	 *  The SQL contains security/access control
	 *  @return SQL to get Node Detail Info
	 */
	private String prepareNodeDetail ()
	{
		//  SQL for Node Info
		StringBuffer cmdNode = new StringBuffer();
		if (m_treeType.equals(MTree.TREETYPE_Menu))
		{
			boolean base = Env.isBaseLanguage(Env.getCtx(), "AD_Menu");
			if (base)
				cmdNode.append("SELECT m.Name,m.Description,m.IsSummary,m.Action "
					+ "FROM AD_Menu m");
			else
				cmdNode.append("SELECT t.Name,t.Description,m.IsSummary,m.Action "
					+ "FROM AD_Menu m, AD_Menu_Trl t");
			if (!m_editable)
				cmdNode.append(", (SELECT ").append(m_AD_Role_ID).append(" AS XRole FROM DUAL) x");
			cmdNode.append(" WHERE m.AD_Menu_ID=?");            //  #1
			if (!base)
				cmdNode.append(" AND m.AD_Menu_ID=t.AD_Menu_ID AND t.AD_Language='")
					.append(Env.getAD_Language(Env.getCtx())).append("'");
			if (!m_editable)
				cmdNode.append(" AND m.IsActive='Y' "
					+ "AND (m.IsSummary='Y' OR m.Action='B'"
					+ " OR EXISTS (SELECT * FROM AD_Window_Access wa WHERE wa.AD_Window_ID=m.AD_Window_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Process_Access wa WHERE wa.AD_Process_ID=m.AD_Process_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Form_Access wa WHERE wa.AD_Form_ID=m.AD_Form_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Task_Access wa WHERE wa.AD_Task_ID=m.AD_Task_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Workflow_Access wa WHERE wa.AD_Workflow_ID=m.AD_Workflow_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ ")");
		}
		else
		{
			cmdNode.append("SELECT Name,Description,IsSummary,NULL FROM ");
			//
			String sourceTable = null;
			if (m_treeType.equals(MTree.TREETYPE_Org))
				sourceTable = "AD_Org";
			else if (m_treeType.equals(MTree.TREETYPE_Product))
				sourceTable = "M_Product";
			else if (m_treeType.equals(MTree.TREETYPE_ProductCategory))
				sourceTable = "M_Product_Category";
			else if (m_treeType.equals(MTree.TREETYPE_BOM))
				sourceTable = "M_BOM";
			else if (m_treeType.equals(MTree.TREETYPE_ElementValue))
				sourceTable = "C_ElementValue";
			else if (m_treeType.equals(MTree.TREETYPE_BPartner))
				sourceTable = "C_BPartner";
			else if (m_treeType.equals(MTree.TREETYPE_Campaign))
				sourceTable = "C_Campaign";
			else if (m_treeType.equals(MTree.TREETYPE_Project))
				sourceTable = "C_Project";
			else if (m_treeType.equals(MTree.TREETYPE_Activity))
				sourceTable = "C_Activity";
			else if (m_treeType.equals(MTree.TREETYPE_SalesRegion))
				sourceTable = "C_SalesRegion";
			if (sourceTable == null)
			{
				throw new IllegalArgumentException("MTree.prepareNodeDetail - Unknown TreeType=" + m_treeType);
			}
			cmdNode.append(sourceTable).append(" WHERE ")
				.append(sourceTable).append("_ID=?");               // #1
			if (!m_editable)
				cmdNode.append(" AND IsActive='Y'");
		}
		Log.trace(Log.l6_Database, "MTree.prepareNodeDetail", cmdNode.toString());
		return cmdNode.toString();
	}   //  prepareNodeDetail

	/**
	 *  Get Node Details.
	 *  As SQL contains security access, not all nodes will be found
	 *  @param  node_ID     Key of the record
	 *  @param  parent_ID   Parent ID of the record
	 *  @param  seqNo       Sort index
	 *  @param  onBar       Node also on Shortcut bar
	 *  @return Node
	 */
	private MTreeNode getNodeDetail (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		MTreeNode retValue = null;
		try
		{
			m_pstmtDetail.setInt(1, node_ID);
			ResultSet rs = m_pstmtDetail.executeQuery();
			if (rs.next())
			{
				retValue = new MTreeNode (node_ID, seqNo,
					rs.getString(1),                //  name
					rs.getString(2),                //  description
					parent_ID,
					"Y".equals(rs.getString(3)),    //  IsSummary
					rs.getString(4),                //  ImageIndicator
					onBar);
			}
			rs.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getNodeDetail", e);
		}
		return retValue;
	}   //  getNodeDetails

	/*************************************************************************/

	/**
	 *  Trim tree of empty summary nodes
	 */
	private void trimTree()
	{
		boolean needsTrim = m_root != null;
		while (needsTrim)
		{
			needsTrim = false;
			Enumeration en = m_root.preorderEnumeration();
			while (en.hasMoreElements())
			{
				MTreeNode nd = (MTreeNode)en.nextElement();
				if (nd.isSummary() && nd.getChildCount() == 0)
				{
					nd.removeFromParent();
					needsTrim = true;
				}
			}
		}
	}   //  trimTree

	/**
	 *  Diagnostics: Print tree
	 */
	private void diagPrintTree()
	{
		Enumeration en = m_root.preorderEnumeration();
		int count = 0;
		while (en.hasMoreElements())
		{
			StringBuffer sb = new StringBuffer();
			MTreeNode nd = (MTreeNode)en.nextElement();
			for (int i = 0; i < nd.getLevel(); i++)
				sb.append(" ");
			sb.append("ID=").append(nd.getID())
				.append(", SeqNo=").append(nd.getSeqNo())
				.append(" ").append(nd.getName());
			System.out.println(sb.toString());
			count++;
		}
		System.out.println("Count=" + count);
	}   //  diagPrintTree

	/**
	 *  Get Root node
	 *  @return root
	 */
	public MTreeNode getRoot()
	{
		return m_root;
	}   //  getRoot

	/**
	 *  Get Table Name
	 *  @return Table Name
	 */
	public String getTableName()
	{
		return m_tableName;
	}   //  getTableName

	/**
	 *  String representation
	 *  @return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MTree[");
		sb.append("AD_Tree_ID=").append(m_AD_Tree_ID)
			.append(", Name=").append(m_name);
		sb.append("]");
		return sb.toString();
	}
}   //  MTree
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

import java.awt.*;
import java.sql.*;
import java.util.*;
import javax.sql.*;

import org.compiere.util.*;
import org.compiere.print.*;

/**
 *  Builds Tree.
 *  Creates tree structure - maintained in VTreePanel
 *
 *  @author     Jorg Janke
 *  @version    $Id: MTree.java,v 1.20 2004/05/02 01:40:27 jjanke Exp $
 */
public class MTree extends MTree_Base
{
	/**
	 *  Default Constructor.
	 * 	Need to call loadNodes explicitly
	 * 	@param ctx context for security
	 *  @param AD_Tree_ID   The tree to build
	 */
	public MTree (Properties ctx, int AD_Tree_ID)
	{
		super (ctx, AD_Tree_ID);
	}   //  MTree

	/**
	 *  Construct & Load Tree
	 *  @param AD_Tree_ID   The tree to build
	 *  @param editable     True, if tree can be modified
	 *  - includes inactive and empty summary nodes
	 * 	@param ctx context for security
	 */
	public MTree (Properties ctx, int AD_Tree_ID, boolean editable)
	{
		this (ctx, AD_Tree_ID);
		m_editable = editable;
		int AD_User_ID = Env.getContextAsInt(ctx, "AD_User_ID");
		log.info("AD_Tree_ID=" + AD_Tree_ID
			+ ", AD_User_ID=" + AD_User_ID 
			+ ", Editable=" + editable);
		//
		loadNodes(AD_User_ID);
	}   //  MTree


	/** Is Tree editable    	*/
	private boolean     		m_editable = false;
	/** Root Node                   */
	private MTreeNode           m_root = null;
	/** Buffer while loading tree   */
	private ArrayList           m_buffer = new ArrayList();
	/** Prepared Statement for Node Details */
	private RowSet			   	m_nodeRowSet;

	
	/**************************************************************************
	 *  Get primary AD_Tree_ID for KeyColumn.
	 *  Called from GridController
	 *  @param keyColumnName key column name, eg. C_Project_ID
	 *  @return AD_Tree_ID
	 */
	public static int getAD_Tree_ID (String keyColumnName)
	{
		Log.trace(Log.l4_Data, "MTree.getAD_Tree_ID", keyColumnName);
		if (keyColumnName == null || keyColumnName.length() == 0)
			return 0;

		String TreeType = null;
		if (keyColumnName.equals("AD_Menu_ID"))
			TreeType = TREETYPE_Menu; 
		else if (keyColumnName.equals("C_ElementValue_ID"))
			TreeType = TREETYPE_ElementValue;
		else if (keyColumnName.equals("M_Product_ID"))
			TreeType = TREETYPE_Product;
		else if (keyColumnName.equals("C_BPartner_ID"))
			TreeType = TREETYPE_BPartner;
		else if (keyColumnName.equals("AD_Org_ID"))
			TreeType = TREETYPE_Organization;
		else if (keyColumnName.equals("C_Project_ID"))
			TreeType = TREETYPE_Project;
		else if (keyColumnName.equals("M_ProductCategory_ID"))
			TreeType = TREETYPE_ProductCategory;
		else if (keyColumnName.equals("M_BOM_ID"))
			TreeType = TREETYPE_BoM;
		else if (keyColumnName.equals("C_SalesRegion_ID"))
			TreeType = TREETYPE_SalesRegion;
		else if (keyColumnName.equals("C_Campaign_ID"))
			TreeType = TREETYPE_Campaign;
		else if (keyColumnName.equals("C_Activity_ID"))
			TreeType = TREETYPE_Activity;
		else
		{
			Log.error("MTree.getAD_Tree_ID - Could not map " + keyColumnName);
			return 0;
		}

		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getAD_Tree_ID", e);
		}

		return AD_Tree_ID;
	}   //  getAD_Tree_ID


	/**
	 * 	Get Primary Tree of Tree Type
	 * 	@param TreeType see TREETYPE_
	 * 	@param ctx context for Security
	 * 	@return MTree
	 */
	public static MTree getTree (Properties ctx, String TreeType)
	{
		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getTree", e);
		}
		//	Not found
		if (AD_Tree_ID == 0)
		{
			Log.trace(Log.l4_Data, "MTree.getTree - No AD_Tree_ID for TreeType=" + TreeType + ", AD_Client_ID=" + AD_Client_ID);
			return null;
		}
		//
		MTree tree = new MTree (ctx, AD_Tree_ID, false);
		return tree;
	}	//	getTree


	/*************************************************************************
	 *  Load Nodes and Bar
	 * 	@param AD_User_ID user
	 */
	private void loadNodes (int AD_User_ID)
	{
		//  SQL for TreeNodes
		StringBuffer sql = new StringBuffer("SELECT "
			+ "tn.Node_ID,tn.Parent_ID,tn.SeqNo,tb.IsActive "
			+ "FROM ").append(getNodeTableName()).append(" tn"
			+ " LEFT OUTER JOIN AD_TreeBar tb ON (tn.AD_Tree_ID=tb.AD_Tree_ID"
			+ " AND tn.Node_ID=tb.Node_ID AND tb.AD_User_ID=?) "	//	#1
			+ "WHERE tn.AD_Tree_ID=?");								//	#2
		if (!m_editable)
			sql.append(" AND tn.IsActive='Y'");
		sql.append(" ORDER BY COALESCE(tn.Parent_ID, -1), tn.SeqNo");
		Log.trace(9, "MTree.loadNodes", sql.toString());

		//  The Node Loop
		try
		{
			// load Node details - addToTree -> getNodeDetail
			getNodeDetails(); 
			//
			PreparedStatement pstmt = DB.prepareStatement(sql.toString());
			pstmt.setInt(1, AD_User_ID);
			pstmt.setInt(2, getAD_Tree_ID());
			//	Get Tree & Bar
			ResultSet rs = pstmt.executeQuery();
			m_root = new MTreeNode (0, 0, getName(), getDescription(), 0, true, null, false, null);
			while (rs.next())
			{
				int node_ID = rs.getInt(1);
				int parent_ID = rs.getInt(2);
				int seqNo = rs.getInt(3);
				boolean onBar = (rs.getString(4) != null);
				//
				if (node_ID == 0 && parent_ID == 0)
					;
				else
					addToTree (node_ID, parent_ID, seqNo, onBar);	//	calls getNodeDetail
			}
			rs.close();
			pstmt.close();
			//
			m_nodeRowSet.close();
			m_nodeRowSet = null;
		}
		catch (SQLException e)
		{
			Log.error("MTree.loadNodes", e);
			m_nodeRowSet = null;
		}
			
		//  Done with loading - add remainder from buffer
		if (m_buffer.size() != 0)
		{
			Log.trace(Log.l6_Database, "MTree.loadNodes - clearing buffer - Adding to: " + m_root);
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				MTreeNode parent = m_root.findNode(node.getParent_ID());
				if (parent != null && parent.getAllowsChildren())
				{
					parent.add(node);
					checkBuffer(node);
					m_buffer.remove(i);
					i = -1;		//	start again with i=0
				}
			}
		}

		//	Nodes w/o parent
		if (m_buffer.size() != 0)
		{
			Log.error ("MTree.loadNodes - Nodes w/o parent - adding to root - " + m_buffer);
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				m_root.add(node);
				checkBuffer(node);
				m_buffer.remove(i);
				i = -1;
			}
			if (m_buffer.size() != 0)
				Log.error ("MTree.loadNodes - still nodes in Buffer - " + m_buffer);
		}	//	nodes w/o parents

		//  clean up
		if (!m_editable && m_root.getChildCount() > 0)
			trimTree();
//		diagPrintTree();
	}   //  loadNodes

	/**
	 *  Add Node to Tree.
	 *  If not found add to buffer
	 *  @param node_ID Node_ID
	 *  @param parent_ID Parent_ID
	 *  @param seqNo SeqNo
	 *  @param onBar on bar
	 */
	private void addToTree (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		//  Create new Node
		MTreeNode child = getNodeDetail (node_ID, parent_ID, seqNo, onBar);
		if (child == null)
			return;

		//  Add to Tree
		MTreeNode parent = null;
		if (m_root != null)
			parent = m_root.findNode (parent_ID);
		//  Parent found
		if (parent != null && parent.getAllowsChildren())
		{
			parent.add(child);
			//  see if we can add nodes from buffer
			if (m_buffer.size() > 0)
				checkBuffer(child);
		}
		else
			m_buffer.add(child);
	}   //  addToTree

	/**
	 *  Check the buffer for nodes which have newNode as Parents
	 *  @param newNode new node
	 */
	private void checkBuffer (MTreeNode newNode)
	{
		//	Ability to add nodes
		if (!newNode.isSummary() || !newNode.getAllowsChildren())
			return;
		//
		for (int i = 0; i < m_buffer.size(); i++)
		{
			MTreeNode node = (MTreeNode)m_buffer.get(i);
			if (node.getParent_ID() == newNode.getNode_ID())
			{
				newNode.add(node);
				m_buffer.remove(i);
				i--;
			}
		}
	}   //  checkBuffer

	
	
	/**************************************************************************
	 *  Get Node Detail.
	 * 	Loads data into RowSet m_nodeRowSet
	 *  Columns:
	 * 	- ID
	 *  - Name
	 *  - Description
	 *  - IsSummary
	 *  - ImageIndicator
	 * 	- additional for Menu
	 *  Parameter:
	 *  - Node_ID
	 *  The SQL contains security/access control
	 *  @return SQL to get Node Detail Info
	 */
	private void getNodeDetails ()
	{
		//  SQL for Node Info
		StringBuffer sqlNode = new StringBuffer();
		String sourceTable = "t";
		String fromClause = getSourceTableName(false);	//	fully qualified
		String columnNameX = getSourceTableName(true);
		String color = getActionColorName();
		if (getTreeType().equals(TREETYPE_Menu))
		{
			boolean base = Env.isBaseLanguage(Env.getCtx(), "AD_Menu");
			sourceTable = "m";
			if (base)
				sqlNode.append("SELECT m.AD_Menu_ID, m.Name,m.Description,m.IsSummary,m.Action, "
					+ "m.AD_Window_ID, m.AD_Process_ID, m.AD_Form_ID, m.AD_Workflow_ID, m.AD_Task_ID, m.AD_Workbench_ID "
					+ "FROM AD_Menu m");
			else
				sqlNode.append("SELECT m.AD_Menu_ID,  t.Name,t.Description,m.IsSummary,m.Action, "
					+ "m.AD_Window_ID, m.AD_Process_ID, m.AD_Form_ID, m.AD_Workflow_ID, m.AD_Task_ID, m.AD_Workbench_ID "
					+ "FROM AD_Menu m, AD_Menu_Trl t");
			if (!base)
				sqlNode.append(" WHERE m.AD_Menu_ID=t.AD_Menu_ID AND t.AD_Language='")
					.append(Env.getAD_Language(Env.getCtx())).append("'");
			if (!m_editable)
			{
				boolean hasWhere = sqlNode.indexOf(" WHERE ") != -1;
				sqlNode.append(hasWhere ? " AND " : " WHERE ").append(" m.IsActive='Y' ");
			}
		}
		else
		{
			if (columnNameX == null)
				throw new IllegalArgumentException("MTree.prepareNodeDetail - Unknown TreeType=" + getTreeType());
			sqlNode.append("SELECT t.").append(columnNameX)
				.append("_ID,t.Name,t.Description,t.IsSummary,").append(color)
				.append(" FROM ").append(fromClause);
			if (!m_editable)
				sqlNode.append(" WHERE t.IsActive='Y'");
		}
		String sql = sqlNode.toString();
		if (!m_editable)	//	editable = menu/etc. window
			sql = MRole.getDefault(getCtx(), false).addAccessSQL(sql, 
				sourceTable, MRole.SQL_FULLYQUALIFIED, m_editable);
		log.debug("getNodeDetails - " + sql);
		m_nodeRowSet = DB.getRowSet (sql, true);
	}   //  getNodeDetails

	/**
	 *  Get Node Details.
	 *  As SQL contains security access, not all nodes will be found
	 *  @param  node_ID     Key of the record
	 *  @param  parent_ID   Parent ID of the record
	 *  @param  seqNo       Sort index
	 *  @param  onBar       Node also on Shortcut bar
	 *  @return Node
	 */
	private MTreeNode getNodeDetail (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		MTreeNode retValue = null;
		try
		{
			m_nodeRowSet.beforeFirst();
			while (m_nodeRowSet.next())
			{
				int node = m_nodeRowSet.getInt(1);				
				if (node_ID != node)	//	search for correct one
					continue;
				//	ID,Name,Description,IsSummary,Action/Color
				int index = 2;				
				String name = m_nodeRowSet.getString(index++); 
				String description = m_nodeRowSet.getString(index++);
				boolean isSummary = "Y".equals(m_nodeRowSet.getString(index++));
				String actionColor = m_nodeRowSet.getString(index++);
				//	Menu only
				if (getTreeType().equals(TREETYPE_Menu) && !isSummary)
				{
					int AD_Window_ID = m_nodeRowSet.getInt(index++);
					int AD_Process_ID = m_nodeRowSet.getInt(index++);
					int AD_Form_ID = m_nodeRowSet.getInt(index++);
					int AD_Workflow_ID = m_nodeRowSet.getInt(index++);
					int AD_Task_ID = m_nodeRowSet.getInt(index++);
					int AD_Workbench_ID = m_nodeRowSet.getInt(index++);
					//
					MRole role = MRole.getDefault(getCtx(), false);
					Boolean access = null;
					if (X_AD_Menu.ACTION_Window.equals(actionColor))
						access = role.getWindowAccess(AD_Window_ID);
					else if (X_AD_Menu.ACTION_Process.equals(actionColor) 
						|| X_AD_Menu.ACTION_Report.equals(actionColor))
						access = role.getProcessAccess(AD_Process_ID);
					else if (X_AD_Menu.ACTION_Form.equals(actionColor))
						access = role.getFormAccess(AD_Form_ID);
					else if (X_AD_Menu.ACTION_WorkFlow.equals(actionColor))
						access = role.getWorkflowAccess(AD_Workflow_ID);
					else if (X_AD_Menu.ACTION_Task.equals(actionColor))
						access = role.getTaskAccess(AD_Task_ID);
				//	else if (X_AD_Menu.ACTION_Workbench.equals(action))
				//		access = role.getWorkbenchAccess(AD_Window_ID);
				//	log.debug("getNodeDetail - " + name + " - " + actionColor + " - " + access);
					//
					if (access != null		//	rw or ro for Role 
						|| m_editable)		//	Menu Window can see all
					{
						retValue = new MTreeNode (node_ID, seqNo,
							name, description, parent_ID, isSummary,
							actionColor, onBar, null);	//	menu has no color
					}
				}
				else	//	always add
				{
					Color color = null;	//	action
					MPrintColor printColor = MPrintColor.get(actionColor);
					if (printColor != null)
						color = printColor.getColor(); 
					//
					retValue = new MTreeNode (node_ID, seqNo,
						name, description, parent_ID, isSummary,
						null, onBar, color);
				}
			}
		}
		catch (SQLException e)
		{
			log.error("getNodeDetails", e);
		}
		return retValue;
	}   //  getNodeDetails

	/*************************************************************************/

	/**
	 *  Trim tree of empty summary nodes
	 */
	private void trimTree()
	{
		boolean needsTrim = m_root != null;
		while (needsTrim)
		{
			needsTrim = false;
			Enumeration en = m_root.preorderEnumeration();
			while (en.hasMoreElements())
			{
				MTreeNode nd = (MTreeNode)en.nextElement();
				if (nd.isSummary() && nd.getChildCount() == 0)
				{
					nd.removeFromParent();
					needsTrim = true;
				}
			}
		}
	}   //  trimTree

	/**
	 *  Diagnostics: Print tree
	 */
	private void dumpTree()
	{
		Enumeration en = m_root.preorderEnumeration();
		int count = 0;
		while (en.hasMoreElements())
		{
			StringBuffer sb = new StringBuffer();
			MTreeNode nd = (MTreeNode)en.nextElement();
			for (int i = 0; i < nd.getLevel(); i++)
				sb.append(" ");
			sb.append("ID=").append(nd.getNode_ID())
				.append(", SeqNo=").append(nd.getSeqNo())
				.append(" ").append(nd.getName());
			System.out.println(sb.toString());
			count++;
		}
		System.out.println("Count=" + count);
	}   //  diagPrintTree

	/**
	 *  Get Root node
	 *  @return root
	 */
	public MTreeNode getRoot()
	{
		return m_root;
	}   //  getRoot

	/**
	 * 	Is Menu Tree
	 *	@return true if menu
	 */
	public boolean isMenu()
	{
		return TREETYPE_Menu.equals(getTreeType());
	}	//	isMenu

	/**
	 * 	Is Product Tree
	 *	@return true if product
	 */
	public boolean isProduct()
	{
		return TREETYPE_Product.equals(getTreeType());
	}	//	isProduct
	
	/**
	 * 	Is Business Partner Tree
	 *	@return true if partner
	 */
	public boolean isBPartner()
	{
		return TREETYPE_BPartner.equals(getTreeType());
	}	//	isBPartner
	
	/**
	 *  String representation
	 *  @return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MTree[");
		sb.append("AD_Tree_ID=").append(getAD_Tree_ID())
			.append(", Name=").append(getName());
		sb.append("]");
		return sb.toString();
	}
}   //  MTree
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

import java.sql.*;
import java.util.*;
import javax.sql.*;

import org.compiere.util.*;

/**
 *  Builds Tree.
 *  Creates tree structure - maintained in VTreePanel
 *
 *  @author     Jorg Janke
 *  @version    $Id: MTree.java,v 1.6 2003/11/06 07:08:06 jjanke Exp $
 */
public class MTree extends X_AD_Tree
{
	/**
	 *  Construct Tree
	 *  @param AD_Tree_ID   The tree to build
	 *  @param editable     True, if tree can be modified
	 *  - includes inactive and empty summary nodes
	 * 	@param ctx context for security
	 */
	public MTree (Properties ctx, int AD_Tree_ID, boolean editable)
	{
		super(ctx, AD_Tree_ID);
		log.info("AD_Tree_ID=" + AD_Tree_ID + ", Editable=" + editable);
		m_editable = editable;

		int AD_User_ID = Env.getContextAsInt(ctx, "AD_User_ID");
		loadNodes(AD_User_ID);
	}   //  MTree

	/** Is Tree editable    	*/
	private boolean     		m_editable;
	/** Table Name          	*/
	private String      		m_nodeTableName = "AD_TreeNode";
	/** Root Node                   */
	private MTreeNode           m_root = null;
	/** Buffer while loading tree   */
	private ArrayList           m_buffer = new ArrayList();
	/** Prepared Statement for Node Details */
	private RowSet			   m_nodeRowSet;

	/*************************************************************************/

	/**
	 *  Get primary AD_Tree_ID for KeyColumn.
	 *  Called from GridController
	 *  @param keyColumnName key column name, eg. C_Project_ID
	 *  @return AD_Tree_ID
	 */
	public static int getAD_Tree_ID (String keyColumnName)
	{
		Log.trace(Log.l4_Data, "MTree.getAD_Tree_ID", keyColumnName);
		if (keyColumnName == null || keyColumnName.length() == 0)
			return 0;

		String TreeType = null;
		if (keyColumnName.equals("AD_Menu_ID"))
			TreeType = TREETYPE_Menu; 
		else if (keyColumnName.equals("C_ElementValue_ID"))
			TreeType = TREETYPE_ElementValueAccountEtc;
		else if (keyColumnName.equals("M_Product_ID"))
			TreeType = TREETYPE_Product;
		else if (keyColumnName.equals("C_BPartner_ID"))
			TreeType = TREETYPE_BusPartner;
		else if (keyColumnName.equals("AD_Org_ID"))
			TreeType = TREETYPE_Organization;
		else if (keyColumnName.equals("C_Project_ID"))
			TreeType = TREETYPE_Project;
		else if (keyColumnName.equals("M_ProductCategory_ID"))
			TreeType = TREETYPE_ProductCategory;
		else if (keyColumnName.equals("M_BOM_ID"))
			TreeType = TREETYPE_BoM;
		else if (keyColumnName.equals("C_SalesRegion_ID"))
			TreeType = TREETYPE_SalesRegion;
		else if (keyColumnName.equals("C_Campaign_ID"))
			TreeType = TREETYPE_Campaign;
		else if (keyColumnName.equals("C_Activity_ID"))
			TreeType = TREETYPE_Activity;
		else
		{
			Log.error("MTree.getAD_Tree_ID - Could not map " + keyColumnName);
			return 0;
		}

		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getAD_Tree_ID", e);
		}

		return AD_Tree_ID;
	}   //  getAD_Tree_ID


	/**
	 * 	Get Primary Tree of Tree Type
	 * 	@param TreeType see TREETYPE_
	 * 	@param ctx context for Security
	 * 	@return MTree
	 */
	public static MTree getTree (Properties ctx, String TreeType)
	{
		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getTree", e);
		}
		//	Not found
		if (AD_Tree_ID == 0)
		{
			Log.trace(Log.l4_Data, "MTree.getTree - No AD_Tree_ID for TreeType=" + TreeType + ", AD_Client_ID=" + AD_Client_ID);
			return null;
		}
		//
		MTree tree = new MTree (ctx, AD_Tree_ID, false);
		return tree;
	}	//	getTree


	/*************************************************************************

	/**
	 *  Load Nodes and Bar
	 * 	@param AD_User_ID user
	 */
	private void loadNodes (int AD_User_ID)
	{
		//  TableName: AD_TreeNode
		if (getTreeType().equals(TREETYPE_Menu))
			;   //  m_tableName += "MM";
		else if  (getTreeType().equals(TREETYPE_BusPartner))
			m_nodeTableName += "BP";
		else if  (getTreeType().equals(TREETYPE_Product))
			m_nodeTableName += "PR";

		//  SQL for TreeNodes
		StringBuffer cmd = new StringBuffer("SELECT "
			+ "tn.Node_ID,tn.Parent_ID,tn.SeqNo,tb.IsActive "
			+ "FROM ").append(m_nodeTableName).append(" tn"
			+ " LEFT OUTER JOIN AD_TreeBar tb ON (tn.AD_Tree_ID=tb.AD_Tree_ID"
			+ " AND tn.Node_ID=tb.Node_ID AND tb.AD_User_ID=?) "	//	#1
			+ "WHERE tn.AD_Tree_ID=?");								//	#2
		if (!m_editable)
			cmd.append(" AND tn.IsActive='Y'");
		cmd.append(" ORDER BY COALESCE(tn.Parent_ID, -1), tn.SeqNo");
		Log.trace(Log.l6_Database, "MTree.loadNodes", cmd.toString());

		//  The Node Loop
		try
		{
			// load Node details - addToTree -> getNodeDetail
			getNodeDetails(); 
			//
			PreparedStatement pstmt = DB.prepareStatement(cmd.toString());
			pstmt.setInt(1, AD_User_ID);
			pstmt.setInt(2, getAD_Tree_ID());
			//	Get Tree & Bar
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int node_ID = rs.getInt(1);
				int parent_ID = rs.getInt(2);
				int seqNo = rs.getInt(3);
				boolean onBar = (rs.getString(4) != null);
				//
				if (node_ID == 0 && parent_ID == 0)
					m_root = new MTreeNode (node_ID, 0, getName(), getDescription(), 0, true, null, onBar);
				else
					addToTree (node_ID, parent_ID, seqNo, onBar);	//	calls getNodeDetail
			}
			rs.close();
			pstmt.close();
			//
			m_nodeRowSet.close();
			m_nodeRowSet = null;
		}
		catch (SQLException e)
		{
			Log.error("MTree.loadNodes", e);
			m_nodeRowSet = null;
		}
		
		//  Done with loading - add remainder from buffer
		if (m_buffer.size() != 0)
		{
			Log.trace(Log.l6_Database, "MTree.loadNodes - clearing buffer");
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				MTreeNode parent = m_root.findNode(node.getParent_ID());
				if (parent != null && parent.getAllowsChildren())
				{
					parent.add(node);
					checkBuffer(node);
					m_buffer.remove(i);
					i = -1;
				}
			}
		}

		//	Nodes w/o parent
		if (m_buffer.size() != 0)
		{
			Log.error ("MTree.loadNodes - Nodes w/o parent - adding to root - " + m_buffer);
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				m_root.add(node);
				checkBuffer(node);
				m_buffer.remove(i);
				i = -1;
			}
			if (m_buffer.size() != 0)
				Log.error ("MTree.loadNodes - still nodes in Buffer - " + m_buffer);
		}	//	nodes w/o parents

		//  clean up
		if (!m_editable && m_root.getChildCount() > 0)
			trimTree();
//		diagPrintTree();
	}   //  loadNodes

	/**
	 *  Add Node to Tree.
	 *  If not found add to buffer
	 *  @param node_ID Node_ID
	 *  @param parent_ID Parent_ID
	 *  @param seqNo SeqNo
	 *  @param onBar on bar
	 */
	private void addToTree (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		//  Create new Node
		MTreeNode child = getNodeDetail (node_ID, parent_ID, seqNo, onBar);
		if (child == null)
			return;

		//  Add to Tree
		MTreeNode parent = null;
		if (m_root != null)
			parent = m_root.findNode (parent_ID);
		//  Parent found
		if (parent != null && parent.getAllowsChildren())
		{
			parent.add(child);
			//  see if we can add nodes from buffer
			if (m_buffer.size() > 0)
				checkBuffer(child);
		}
		else
			m_buffer.add(child);
	}   //  addToTree

	/**
	 *  Check the buffer for nodes which have newNode as Parents
	 *  @param newNode new node
	 */
	private void checkBuffer (MTreeNode newNode)
	{
		for (int i = 0; i < m_buffer.size(); i++)
		{
			MTreeNode node = (MTreeNode)m_buffer.get(i);
			if (node.getParent_ID() == newNode.getID())
			{
				newNode.add(node);
				m_buffer.remove(i);
				i--;
			}
		}
	}   //  checkBuffer

	/*************************************************************************/

	/**
	 *  Get Node Detail.
	 * 	Loads data into RowSet m_nodeRowSet
	 *  Columns:
	 * 	- ID
	 *  - Name
	 *  - Description
	 *  - IsSummary
	 *  - ImageIndicator
	 * 	- additional for Menu
	 *  Parameter:
	 *  - Node_ID
	 *  The SQL contains security/access control
	 *  @return SQL to get Node Detail Info
	 */
	private void getNodeDetails ()
	{
		//  SQL for Node Info
		StringBuffer sqlNode = new StringBuffer();
		String sourceTable = null;
		if (getTreeType().equals(TREETYPE_Menu))
		{
			boolean base = Env.isBaseLanguage(Env.getCtx(), "AD_Menu");
			sourceTable = "m";
			if (base)
				sqlNode.append("SELECT m.AD_Menu_ID, m.Name,m.Description,m.IsSummary,m.Action, "
					+ "m.AD_Window_ID, m.AD_Process_ID, m.AD_Form_ID, m.AD_Workflow_ID, m.AD_Task_ID, m.AD_Workbench_ID "
					+ "FROM AD_Menu m");
			else
				sqlNode.append("SELECT m_AD_Menu_ID,  t.Name,t.Description,m.IsSummary,m.Action, "
					+ "m.AD_Window_ID, m.AD_Process_ID, m.AD_Form_ID, m.AD_Workflow_ID, m.AD_Task_ID, m.AD_Workbench_ID "
					+ "FROM AD_Menu m, AD_Menu_Trl t");
			if (!base)
				sqlNode.append(" WHERE m.AD_Menu_ID=t.AD_Menu_ID AND t.AD_Language='")
					.append(Env.getAD_Language(Env.getCtx())).append("'");
			if (!m_editable)
			{
				boolean hasWhere = sqlNode.indexOf(" WHERE ") != -1;
				sqlNode.append(hasWhere ? " AND " : " WHERE ").append(" m.IsActive='Y' ");
			}
		}
		else
		{
			if (getTreeType().equals(TREETYPE_Organization))
				sourceTable = "AD_Org";
			else if (getTreeType().equals(TREETYPE_Product))
				sourceTable = "M_Product";
			else if (getTreeType().equals(TREETYPE_ProductCategory))
				sourceTable = "M_Product_Category";
			else if (getTreeType().equals(TREETYPE_BoM))
				sourceTable = "M_BOM";
			else if (getTreeType().equals(TREETYPE_ElementValueAccountEtc))
				sourceTable = "C_ElementValue";
			else if (getTreeType().equals(TREETYPE_BusPartner))
				sourceTable = "C_BPartner";
			else if (getTreeType().equals(TREETYPE_Campaign))
				sourceTable = "C_Campaign";
			else if (getTreeType().equals(TREETYPE_Project))
				sourceTable = "C_Project";
			else if (getTreeType().equals(TREETYPE_Activity))
				sourceTable = "C_Activity";
			else if (getTreeType().equals(TREETYPE_SalesRegion))
				sourceTable = "C_SalesRegion";
			if (sourceTable == null)
			{
				throw new IllegalArgumentException("MTree.prepareNodeDetail - Unknown TreeType=" + getTreeType());
			}
			sqlNode.append("SELECT ").append(sourceTable).append("_ID,Name,Description,IsSummary,NULL FROM ")
				.append(sourceTable);
			if (!m_editable)
				sqlNode.append(" WHERE IsActive='Y'");
		}
		String sql = MRole.getDefault(getCtx(), false).addAccessSQL(sqlNode.toString(), 
			sourceTable, MRole.SQL_FULLYQUALIFIED, m_editable);
		log.debug("getNodeDetails - " + sql);
		m_nodeRowSet = DB.getRowSet (sql);
	}   //  getNodeDetails

	/**
	 *  Get Node Details.
	 *  As SQL contains security access, not all nodes will be found
	 *  @param  node_ID     Key of the record
	 *  @param  parent_ID   Parent ID of the record
	 *  @param  seqNo       Sort index
	 *  @param  onBar       Node also on Shortcut bar
	 *  @return Node
	 */
	private MTreeNode getNodeDetail (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		MTreeNode retValue = null;
		try
		{
			m_nodeRowSet.beforeFirst();
			while (m_nodeRowSet.next())
			{
				int node = m_nodeRowSet.getInt(1);				
				if (node_ID != node)	//	search for correct one
					continue;
				//
				int index = 2;				
				String name = m_nodeRowSet.getString(index++); 
				String description = m_nodeRowSet.getString(index++);
				boolean isSummary = "Y".equals(m_nodeRowSet.getString(index++));
				String action = m_nodeRowSet.getString(index++);
				//
				if (getTreeType().equals(TREETYPE_Menu) && !isSummary)
				{
					int AD_Window_ID = m_nodeRowSet.getInt(index++);
					int AD_Process_ID = m_nodeRowSet.getInt(index++);
					int AD_Form_ID = m_nodeRowSet.getInt(index++);
					int AD_Workflow_ID = m_nodeRowSet.getInt(index++);
					int AD_Task_ID = m_nodeRowSet.getInt(index++);
					int AD_Workbench_ID = m_nodeRowSet.getInt(index++);
					//
					MRole role = MRole.getDefault(getCtx(), false);
					Boolean access = null;
					if (X_AD_Menu.ACTION_Window.equals(action))
						access = role.getWindowAccess(AD_Window_ID);
					else if (X_AD_Menu.ACTION_Process.equals(action) || X_AD_Menu.ACTION_Report.equals(action))
						access = role.getProcessAccess(AD_Process_ID);
					else if (X_AD_Menu.ACTION_Form.equals(action))
						access = role.getFormAccess(AD_Form_ID);
					else if (X_AD_Menu.ACTION_WorkFlow.equals(action))
						access = role.getWorkflowAccess(AD_Workflow_ID);
					else if (X_AD_Menu.ACTION_Task.equals(action))
						access = role.getTaskAccess(AD_Task_ID);
				//	else if (X_AD_Menu.ACTION_Workbench.equals(action))
				//		access = role.getWorkbenchAccess(AD_Window_ID);
				//	log.debug("getNodeDetail - " + name + " - " + action + " - " + access);
					//
					if (access != null)		//	rw or ro
					{
						retValue = new MTreeNode (node_ID, seqNo,
							name, description, parent_ID, isSummary,
							action, onBar);
					}
				}
				else
				{	//	always add
					retValue = new MTreeNode (node_ID, seqNo,
						name, description, parent_ID, isSummary,
						action, onBar);
				}
			}
		}
		catch (SQLException e)
		{
			log.error("getNodeDetails", e);
		}
		return retValue;
	}   //  getNodeDetails

	/*************************************************************************/

	/**
	 *  Trim tree of empty summary nodes
	 */
	private void trimTree()
	{
		boolean needsTrim = m_root != null;
		while (needsTrim)
		{
			needsTrim = false;
			Enumeration en = m_root.preorderEnumeration();
			while (en.hasMoreElements())
			{
				MTreeNode nd = (MTreeNode)en.nextElement();
				if (nd.isSummary() && nd.getChildCount() == 0)
				{
					nd.removeFromParent();
					needsTrim = true;
				}
			}
		}
	}   //  trimTree

	/**
	 *  Diagnostics: Print tree
	 */
	private void diagPrintTree()
	{
		Enumeration en = m_root.preorderEnumeration();
		int count = 0;
		while (en.hasMoreElements())
		{
			StringBuffer sb = new StringBuffer();
			MTreeNode nd = (MTreeNode)en.nextElement();
			for (int i = 0; i < nd.getLevel(); i++)
				sb.append(" ");
			sb.append("ID=").append(nd.getID())
				.append(", SeqNo=").append(nd.getSeqNo())
				.append(" ").append(nd.getName());
			System.out.println(sb.toString());
			count++;
		}
		System.out.println("Count=" + count);
	}   //  diagPrintTree

	/**
	 *  Get Root node
	 *  @return root
	 */
	public MTreeNode getRoot()
	{
		return m_root;
	}   //  getRoot

	/**
	 *  Get Node Table Name
	 *  @return Table Name
	 */
	public String getNodeTableName()
	{
		return m_nodeTableName;
	}   //  getTableName

	/**
	 *  String representation
	 *  @return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MTree[");
		sb.append("AD_Tree_ID=").append(getAD_Tree_ID())
			.append(", Name=").append(getName());
		sb.append("]");
		return sb.toString();
	}
}   //  MTree
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

import java.sql.*;
import java.util.*;

import org.compiere.util.*;

/**
 *  Builds Tree.
 *  Creates tree structure - maintained in VTreePanel
 *
 *  @author     Jorg Janke
 *  @version    $Id: MTree.java,v 1.2 2003/02/19 06:48:23 jjanke Exp $
 */
public class MTree
{
	/**
	 *  Construct Tree
	 *  @param AD_Tree_ID   The tree to build
	 *  @param editable     True, if tree can be modified
	 *  - includes inactive and empty summary nodes
	 */
	public MTree (int AD_Tree_ID, boolean editable)
	{
		this (AD_Tree_ID, 0, 0, editable);
	}	//	MTree

	/**
	 *  Construct Tree
	 *  @param AD_Tree_ID   The tree to build
	 *  @param AD_User_ID   Tree Bar Access control - optional
	 *  @param AD_Role_ID   Menu Access control
	 *  @param editable     True, if tree can be modified
	 *  - includes inactive and empty summary nodes
	 */
	public MTree (int AD_Tree_ID,
		int AD_User_ID, int AD_Role_ID, boolean editable)
	{
		Log.trace(Log.l4_Data, "MTree - AD_Tree_ID=" + AD_Tree_ID,
			"AD_Role_ID=" + AD_Role_ID + ", AD_User_ID=" + AD_User_ID
			+ ", Editable=" + editable);

		m_AD_Tree_ID = AD_Tree_ID;
		m_AD_User_ID = AD_User_ID;
		m_AD_Role_ID = AD_Role_ID;
		m_editable = editable;
		loadTree();
	}   //  MTree

	/** Tree ID             */
	private int         m_AD_Tree_ID;
	/** Tree for User       */
	private int         m_AD_User_ID;
	/** Role Access Control */
	private int         m_AD_Role_ID;
	/** Is Tree editable    */
	private boolean     m_editable;

	/** Tree Name           */
	private String      m_name;
	/** Tree Description    */
	private String	    m_description;
	/** Tree Type           */
	private String      m_treeType;

	/** Table Name          */
	private String      m_tableName = "AD_TreeNode";

	/** Root Node                   */
	private MTreeNode           m_root = null;
	/** Buffer while loading tree   */
	private ArrayList           m_buffer = new ArrayList();
	/** Prepared Statement for Node Details */
	private PreparedStatement   m_pstmtDetail;

	/*************************************************************************/

	/** Tree Type Menu MM   */
	public static final String	TREETYPE_Menu =         "MM";
	/** Tree Type Account ElementValue EV   */
	public static final String	TREETYPE_ElementValue = "EV";
	/** Tree Type Product PR   */
	public static final String	TREETYPE_Product =      "PR";
	/** Tree Type BusinessPartner BP   */
	public static final String	TREETYPE_BPartner =     "BP";
	/** Tree Type Organization OO   */
	public static final String	TREETYPE_Org =          "OO";
	/** Tree Type Project PJ   */
	public static final String	TREETYPE_Project =      "PJ";
	/** Tree Type ProductCategory PC   */
	public static final String	TREETYPE_ProductCategory = "PC";
	/** Tree Type BOM BB   */
	public static final String	TREETYPE_BOM =          "BB";
	/** Tree Type SalesRegion SR   */
	public static final String	TREETYPE_SalesRegion =  "SR";
	/** Tree Type Camoaign MC   */
	public static final String	TREETYPE_Campaign =     "MC";
	/** Tree Type Activity AY   */
	public static final String	TREETYPE_Activity =     "AY";

	/*************************************************************************/

	/**
	 *  Get primary AD_Tree_ID for KeyColumn.
	 *  Called from GridController
	 *  @param keyColumnName key column name, eg. C_Project_ID
	 *  @return AD_Tree_ID
	 */
	public static int getAD_Tree_ID (String keyColumnName)
	{
		Log.trace(Log.l4_Data, "MTree.getAD_Tree_ID", keyColumnName);
		if (keyColumnName == null || keyColumnName.length() == 0)
			return 0;

		String TreeType = null;
		if (keyColumnName.equals("AD_Menu_ID"))
			TreeType = MTree.TREETYPE_Menu;
		else if (keyColumnName.equals("C_ElementValue_ID"))
			TreeType = MTree.TREETYPE_ElementValue;
		else if (keyColumnName.equals("M_Product_ID"))
			TreeType = MTree.TREETYPE_Product;
		else if (keyColumnName.equals("C_BPartner_ID"))
			TreeType = MTree.TREETYPE_BPartner;
		else if (keyColumnName.equals("AD_Org_ID"))
			TreeType = MTree.TREETYPE_Org;
		else if (keyColumnName.equals("C_Project_ID"))
			TreeType = MTree.TREETYPE_Project;
		else if (keyColumnName.equals("M_ProductCategory_ID"))
			TreeType = MTree.TREETYPE_ProductCategory;
		else if (keyColumnName.equals("M_BOM_ID"))
			TreeType = MTree.TREETYPE_BOM;
		else if (keyColumnName.equals("C_SalesRegion_ID"))
			TreeType = MTree.TREETYPE_SalesRegion;
		else if (keyColumnName.equals("C_Campaign_ID"))
			TreeType = MTree.TREETYPE_Campaign;
		else if (keyColumnName.equals("C_Activity_ID"))
			TreeType = MTree.TREETYPE_Activity;
		else
		{
			Log.error("MTree.getAD_Tree_ID - Could not map " + keyColumnName);
			return 0;
		}

		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getAD_Tree_ID", e);
		}

		return AD_Tree_ID;
	}   //  getAD_Tree_ID


	/**
	 * 	Get Primary Tree of Tree Type
	 * 	@param TreeType see TREETYPE_
	 * 	@return MTree
	 */
	public static MTree getTree (String TreeType)
	{
		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getTree", e);
		}
		//	Not found
		if (AD_Tree_ID == 0)
		{
			Log.trace(Log.l4_Data, "MTree.getTree - No AD_Tree_ID for TreeType=" + TreeType + ", AD_Client_ID=" + AD_Client_ID);
			return null;
		}
		//
		MTree tree = new MTree (AD_Tree_ID, false);
		return tree;
	}	//	getTree


	/*************************************************************************/

	/**
	 *  Load Tree
	 */
	private void loadTree()
	{
		//  Get Tree info
		String sql = "SELECT Name, Description, TreeType "
			+ "FROM AD_Tree "
			+ "WHERE AD_Tree_ID=?"
			+ " AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, m_AD_Tree_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				m_name = rs.getString(1);
				m_description = rs.getString(2);
				if (m_description == null)
					m_description = "";
				m_treeType = rs.getString(3);
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.loadTree", e);
			return;
		}
		loadNodes();
	}   //  loadTree

	/**
	 *  Load Nodes
	 */
	private void loadNodes()
	{
		//  TableName: AD_TreeNode
		if (m_treeType.equals(TREETYPE_Menu))
			;   //  m_tableName += "MM";
		else if  (m_treeType.equals(TREETYPE_BPartner))
			m_tableName += "BP";
		else if  (m_treeType.equals(TREETYPE_Product))
			m_tableName += "PR";

		//  SQL for TreeNodes
		StringBuffer cmd = new StringBuffer("SELECT "
			+ "tn.Node_ID,tn.Parent_ID,tn.SeqNo,tb.IsActive "
			+ "FROM ").append(m_tableName).append(" tn"
			+ " LEFT OUTER JOIN AD_TreeBar tb ON (tn.AD_Tree_ID=tb.AD_Tree_ID"
			+ " AND tn.Node_ID=tb.Node_ID AND tb.AD_User_ID=?) "	//	#1
			+ "WHERE tn.AD_Tree_ID=?");								//	#2
		if (!m_editable)
			cmd.append(" AND tn.IsActive='Y'");
		cmd.append(" ORDER BY COALESCE(tn.Parent_ID, -1), tn.SeqNo");
		Log.trace(Log.l6_Database, "MTree.loadNodes", cmd.toString());

		//  The Node Loop
		try
		{
			m_pstmtDetail = DB.prepareStatement(prepareNodeDetail());
			//
			PreparedStatement pstmt = DB.prepareStatement(cmd.toString());
			pstmt.setInt(1, m_AD_User_ID);
			pstmt.setInt(2, m_AD_Tree_ID);
			//
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int node_ID = rs.getInt(1);
				int parent_ID = rs.getInt(2);
				int seqNo = rs.getInt(3);
				boolean onBar = (rs.getString(4) != null);
				//
				if (node_ID == 0 && parent_ID == 0)
					m_root = new MTreeNode (node_ID, 0, m_name, m_description, 0, true, null, onBar);
				else
					addToTree (node_ID, parent_ID, seqNo, onBar);
			}
			rs.close();
			pstmt.close();
			//
			m_pstmtDetail.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.loadNodes", e);
		}
		//  Done with loading - add remainder from buffer
		if (m_buffer.size() != 0)
		{
			Log.trace(Log.l6_Database, "MTree.loadNodes - clearing buffer");
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				MTreeNode parent = m_root.findNode(node.getParent_ID());
				if (parent != null && parent.getAllowsChildren())
				{
					parent.add(node);
					checkBuffer(node);
					m_buffer.remove(i);
					i = -1;
				}
			}
		}

		//	Nodes w/o parent
		if (m_buffer.size() != 0)
		{
			Log.error ("MTree.loadNodes - Nodes w/o parent - adding to root - " + m_buffer);
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				m_root.add(node);
				checkBuffer(node);
				m_buffer.remove(i);
				i = -1;
			}
			if (m_buffer.size() != 0)
				Log.error ("MTree.loadNodes - still nodes in Buffer - " + m_buffer);
		}	//	nodes w/o parents

		//  clean up
		if (!m_editable && m_root.getChildCount() > 0)
			trimTree();
//		diagPrintTree();
	}   //  loadNodes

	/**
	 *  Add Node to Tree.
	 *  If not found add to buffer
	 *  @param node_ID Node_ID
	 *  @param parent_ID Parent_ID
	 *  @param seqNo SeqNo
	 *  @param onBar on bar
	 */
	private void addToTree (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		//  Create new Node
		MTreeNode child = getNodeDetail (node_ID, parent_ID, seqNo, onBar);
		if (child == null)
			return;

		//  Add to Tree
		MTreeNode parent = null;
		if (m_root != null)
			parent = m_root.findNode (parent_ID);
		//  Parent found
		if (parent != null && parent.getAllowsChildren())
		{
			parent.add(child);
			//  see if we can add nodes from buffer
			if (m_buffer.size() > 0)
				checkBuffer(child);
		}
		else
			m_buffer.add(child);
	}   //  addToTree

	/**
	 *  Check the buffer for nodes which have newNode as Parents
	 *  @param newNode new node
	 */
	private void checkBuffer (MTreeNode newNode)
	{
		for (int i = 0; i < m_buffer.size(); i++)
		{
			MTreeNode node = (MTreeNode)m_buffer.get(i);
			if (node.getParent_ID() == newNode.getID())
			{
				newNode.add(node);
				m_buffer.remove(i);
				i--;
			}
		}
	}   //  checkBuffer

	/*************************************************************************/

	/**
	 *  Prepare Node Detail.
	 *  Columns:
	 *  - Name
	 *  - Description
	 *  - IsSummary
	 *  - ImageIndicator
	 *  Parameter:
	 *  - Node_ID
	 *  The SQL contains security/access control
	 *  @return SQL to get Node Detail Info
	 */
	private String prepareNodeDetail ()
	{
		//  SQL for Node Info
		StringBuffer cmdNode = new StringBuffer();
		if (m_treeType.equals(MTree.TREETYPE_Menu))
		{
			boolean base = Env.isBaseLanguage(Env.getCtx(), "AD_Menu");
			if (base)
				cmdNode.append("SELECT m.Name,m.Description,m.IsSummary,m.Action "
					+ "FROM AD_Menu m");
			else
				cmdNode.append("SELECT t.Name,t.Description,m.IsSummary,m.Action "
					+ "FROM AD_Menu m, AD_Menu_Trl t");
			if (!m_editable)
				cmdNode.append(", (SELECT ").append(m_AD_Role_ID).append(" AS XRole FROM DUAL) x");
			cmdNode.append(" WHERE m.AD_Menu_ID=?");            //  #1
			if (!base)
				cmdNode.append(" AND m.AD_Menu_ID=t.AD_Menu_ID AND t.AD_Language='")
					.append(Env.getAD_Language(Env.getCtx())).append("'");
			if (!m_editable)
				cmdNode.append(" AND m.IsActive='Y' "
					+ "AND (m.IsSummary='Y' OR m.Action='B'"
					+ " OR EXISTS (SELECT * FROM AD_Window_Access wa WHERE wa.AD_Window_ID=m.AD_Window_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Process_Access wa WHERE wa.AD_Process_ID=m.AD_Process_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Form_Access wa WHERE wa.AD_Form_ID=m.AD_Form_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Task_Access wa WHERE wa.AD_Task_ID=m.AD_Task_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Workflow_Access wa WHERE wa.AD_Workflow_ID=m.AD_Workflow_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ ")");
		}
		else
		{
			cmdNode.append("SELECT Name,Description,IsSummary,NULL FROM ");
			//
			String sourceTable = null;
			if (m_treeType.equals(MTree.TREETYPE_Org))
				sourceTable = "AD_Org";
			else if (m_treeType.equals(MTree.TREETYPE_Product))
				sourceTable = "M_Product";
			else if (m_treeType.equals(MTree.TREETYPE_ProductCategory))
				sourceTable = "M_Product_Category";
			else if (m_treeType.equals(MTree.TREETYPE_BOM))
				sourceTable = "M_BOM";
			else if (m_treeType.equals(MTree.TREETYPE_ElementValue))
				sourceTable = "C_ElementValue";
			else if (m_treeType.equals(MTree.TREETYPE_BPartner))
				sourceTable = "C_BPartner";
			else if (m_treeType.equals(MTree.TREETYPE_Campaign))
				sourceTable = "C_Campaign";
			else if (m_treeType.equals(MTree.TREETYPE_Project))
				sourceTable = "C_Project";
			else if (m_treeType.equals(MTree.TREETYPE_Activity))
				sourceTable = "C_Activity";
			else if (m_treeType.equals(MTree.TREETYPE_SalesRegion))
				sourceTable = "C_SalesRegion";
			if (sourceTable == null)
			{
				throw new IllegalArgumentException("MTree.prepareNodeDetail - Unknown TreeType=" + m_treeType);
			}
			cmdNode.append(sourceTable).append(" WHERE ")
				.append(sourceTable).append("_ID=?");               // #1
			if (!m_editable)
				cmdNode.append(" AND IsActive='Y'");
		}
		Log.trace(Log.l6_Database, "MTree.prepareNodeDetail", cmdNode.toString());
		return cmdNode.toString();
	}   //  prepareNodeDetail

	/**
	 *  Get Node Details.
	 *  As SQL contains security access, not all nodes will be found
	 *  @param  node_ID     Key of the record
	 *  @param  parent_ID   Parent ID of the record
	 *  @param  seqNo       Sort index
	 *  @param  onBar       Node also on Shortcut bar
	 *  @return Node
	 */
	private MTreeNode getNodeDetail (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		MTreeNode retValue = null;
		try
		{
			m_pstmtDetail.setInt(1, node_ID);
			ResultSet rs = m_pstmtDetail.executeQuery();
			if (rs.next())
			{
				retValue = new MTreeNode (node_ID, seqNo,
					rs.getString(1),                //  name
					rs.getString(2),                //  description
					parent_ID,
					"Y".equals(rs.getString(3)),    //  IsSummary
					rs.getString(4),                //  ImageIndicator
					onBar);
			}
			rs.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getNodeDetail", e);
		}
		return retValue;
	}   //  getNodeDetails

	/*************************************************************************/

	/**
	 *  Trim tree of empty summary nodes
	 */
	private void trimTree()
	{
		boolean needsTrim = m_root != null;
		while (needsTrim)
		{
			needsTrim = false;
			Enumeration en = m_root.preorderEnumeration();
			while (en.hasMoreElements())
			{
				MTreeNode nd = (MTreeNode)en.nextElement();
				if (nd.isSummary() && nd.getChildCount() == 0)
				{
					nd.removeFromParent();
					needsTrim = true;
				}
			}
		}
	}   //  trimTree

	/**
	 *  Diagnostics: Print tree
	 */
	private void diagPrintTree()
	{
		Enumeration en = m_root.preorderEnumeration();
		int count = 0;
		while (en.hasMoreElements())
		{
			StringBuffer sb = new StringBuffer();
			MTreeNode nd = (MTreeNode)en.nextElement();
			for (int i = 0; i < nd.getLevel(); i++)
				sb.append(" ");
			sb.append("ID=").append(nd.getID())
				.append(", SeqNo=").append(nd.getSeqNo())
				.append(" ").append(nd.getName());
			System.out.println(sb.toString());
			count++;
		}
		System.out.println("Count=" + count);
	}   //  diagPrintTree

	/**
	 *  Get Root node
	 *  @return root
	 */
	public MTreeNode getRoot()
	{
		return m_root;
	}   //  getRoot

	/**
	 *  Get Table Name
	 *  @return Table Name
	 */
	public String getTableName()
	{
		return m_tableName;
	}   //  getTableName

	/**
	 *  String representation
	 *  @return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MTree[");
		sb.append("AD_Tree_ID=").append(m_AD_Tree_ID)
			.append(", Name=").append(m_name);
		sb.append("]");
		return sb.toString();
	}
}   //  MTree
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

import java.sql.*;
import java.util.*;

import org.compiere.util.*;

/**
 *  Builds Tree.
 *  Creates tree structure - maintained in VTreePanel
 *
 *  @author     Jorg Janke
 *  @version    $Id: MTree.java,v 1.2 2003/02/19 06:48:23 jjanke Exp $
 */
public class MTree
{
	/**
	 *  Construct Tree
	 *  @param AD_Tree_ID   The tree to build
	 *  @param editable     True, if tree can be modified
	 *  - includes inactive and empty summary nodes
	 */
	public MTree (int AD_Tree_ID, boolean editable)
	{
		this (AD_Tree_ID, 0, 0, editable);
	}	//	MTree

	/**
	 *  Construct Tree
	 *  @param AD_Tree_ID   The tree to build
	 *  @param AD_User_ID   Tree Bar Access control - optional
	 *  @param AD_Role_ID   Menu Access control
	 *  @param editable     True, if tree can be modified
	 *  - includes inactive and empty summary nodes
	 */
	public MTree (int AD_Tree_ID,
		int AD_User_ID, int AD_Role_ID, boolean editable)
	{
		Log.trace(Log.l4_Data, "MTree - AD_Tree_ID=" + AD_Tree_ID,
			"AD_Role_ID=" + AD_Role_ID + ", AD_User_ID=" + AD_User_ID
			+ ", Editable=" + editable);

		m_AD_Tree_ID = AD_Tree_ID;
		m_AD_User_ID = AD_User_ID;
		m_AD_Role_ID = AD_Role_ID;
		m_editable = editable;
		loadTree();
	}   //  MTree

	/** Tree ID             */
	private int         m_AD_Tree_ID;
	/** Tree for User       */
	private int         m_AD_User_ID;
	/** Role Access Control */
	private int         m_AD_Role_ID;
	/** Is Tree editable    */
	private boolean     m_editable;

	/** Tree Name           */
	private String      m_name;
	/** Tree Description    */
	private String	    m_description;
	/** Tree Type           */
	private String      m_treeType;

	/** Table Name          */
	private String      m_tableName = "AD_TreeNode";

	/** Root Node                   */
	private MTreeNode           m_root = null;
	/** Buffer while loading tree   */
	private ArrayList           m_buffer = new ArrayList();
	/** Prepared Statement for Node Details */
	private PreparedStatement   m_pstmtDetail;

	/*************************************************************************/

	/** Tree Type Menu MM   */
	public static final String	TREETYPE_Menu =         "MM";
	/** Tree Type Account ElementValue EV   */
	public static final String	TREETYPE_ElementValue = "EV";
	/** Tree Type Product PR   */
	public static final String	TREETYPE_Product =      "PR";
	/** Tree Type BusinessPartner BP   */
	public static final String	TREETYPE_BPartner =     "BP";
	/** Tree Type Organization OO   */
	public static final String	TREETYPE_Org =          "OO";
	/** Tree Type Project PJ   */
	public static final String	TREETYPE_Project =      "PJ";
	/** Tree Type ProductCategory PC   */
	public static final String	TREETYPE_ProductCategory = "PC";
	/** Tree Type BOM BB   */
	public static final String	TREETYPE_BOM =          "BB";
	/** Tree Type SalesRegion SR   */
	public static final String	TREETYPE_SalesRegion =  "SR";
	/** Tree Type Camoaign MC   */
	public static final String	TREETYPE_Campaign =     "MC";
	/** Tree Type Activity AY   */
	public static final String	TREETYPE_Activity =     "AY";

	/*************************************************************************/

	/**
	 *  Get primary AD_Tree_ID for KeyColumn.
	 *  Called from GridController
	 *  @param keyColumnName key column name, eg. C_Project_ID
	 *  @return AD_Tree_ID
	 */
	public static int getAD_Tree_ID (String keyColumnName)
	{
		Log.trace(Log.l4_Data, "MTree.getAD_Tree_ID", keyColumnName);
		if (keyColumnName == null || keyColumnName.length() == 0)
			return 0;

		String TreeType = null;
		if (keyColumnName.equals("AD_Menu_ID"))
			TreeType = MTree.TREETYPE_Menu;
		else if (keyColumnName.equals("C_ElementValue_ID"))
			TreeType = MTree.TREETYPE_ElementValue;
		else if (keyColumnName.equals("M_Product_ID"))
			TreeType = MTree.TREETYPE_Product;
		else if (keyColumnName.equals("C_BPartner_ID"))
			TreeType = MTree.TREETYPE_BPartner;
		else if (keyColumnName.equals("AD_Org_ID"))
			TreeType = MTree.TREETYPE_Org;
		else if (keyColumnName.equals("C_Project_ID"))
			TreeType = MTree.TREETYPE_Project;
		else if (keyColumnName.equals("M_ProductCategory_ID"))
			TreeType = MTree.TREETYPE_ProductCategory;
		else if (keyColumnName.equals("M_BOM_ID"))
			TreeType = MTree.TREETYPE_BOM;
		else if (keyColumnName.equals("C_SalesRegion_ID"))
			TreeType = MTree.TREETYPE_SalesRegion;
		else if (keyColumnName.equals("C_Campaign_ID"))
			TreeType = MTree.TREETYPE_Campaign;
		else if (keyColumnName.equals("C_Activity_ID"))
			TreeType = MTree.TREETYPE_Activity;
		else
		{
			Log.error("MTree.getAD_Tree_ID - Could not map " + keyColumnName);
			return 0;
		}

		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getAD_Tree_ID", e);
		}

		return AD_Tree_ID;
	}   //  getAD_Tree_ID


	/**
	 * 	Get Primary Tree of Tree Type
	 * 	@param TreeType see TREETYPE_
	 * 	@return MTree
	 */
	public static MTree getTree (String TreeType)
	{
		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getTree", e);
		}
		//	Not found
		if (AD_Tree_ID == 0)
		{
			Log.trace(Log.l4_Data, "MTree.getTree - No AD_Tree_ID for TreeType=" + TreeType + ", AD_Client_ID=" + AD_Client_ID);
			return null;
		}
		//
		MTree tree = new MTree (AD_Tree_ID, false);
		return tree;
	}	//	getTree


	/*************************************************************************/

	/**
	 *  Load Tree
	 */
	private void loadTree()
	{
		//  Get Tree info
		String sql = "SELECT Name, Description, TreeType "
			+ "FROM AD_Tree "
			+ "WHERE AD_Tree_ID=?"
			+ " AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, m_AD_Tree_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				m_name = rs.getString(1);
				m_description = rs.getString(2);
				if (m_description == null)
					m_description = "";
				m_treeType = rs.getString(3);
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.loadTree", e);
			return;
		}
		loadNodes();
	}   //  loadTree

	/**
	 *  Load Nodes
	 */
	private void loadNodes()
	{
		//  TableName: AD_TreeNode
		if (m_treeType.equals(TREETYPE_Menu))
			;   //  m_tableName += "MM";
		else if  (m_treeType.equals(TREETYPE_BPartner))
			m_tableName += "BP";
		else if  (m_treeType.equals(TREETYPE_Product))
			m_tableName += "PR";

		//  SQL for TreeNodes
		StringBuffer cmd = new StringBuffer("SELECT "
			+ "tn.Node_ID,tn.Parent_ID,tn.SeqNo,tb.IsActive "
			+ "FROM ").append(m_tableName).append(" tn"
			+ " LEFT OUTER JOIN AD_TreeBar tb ON (tn.AD_Tree_ID=tb.AD_Tree_ID"
			+ " AND tn.Node_ID=tb.Node_ID AND tb.AD_User_ID=?) "	//	#1
			+ "WHERE tn.AD_Tree_ID=?");								//	#2
		if (!m_editable)
			cmd.append(" AND tn.IsActive='Y'");
		cmd.append(" ORDER BY COALESCE(tn.Parent_ID, -1), tn.SeqNo");
		Log.trace(Log.l6_Database, "MTree.loadNodes", cmd.toString());

		//  The Node Loop
		try
		{
			m_pstmtDetail = DB.prepareStatement(prepareNodeDetail());
			//
			PreparedStatement pstmt = DB.prepareStatement(cmd.toString());
			pstmt.setInt(1, m_AD_User_ID);
			pstmt.setInt(2, m_AD_Tree_ID);
			//
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int node_ID = rs.getInt(1);
				int parent_ID = rs.getInt(2);
				int seqNo = rs.getInt(3);
				boolean onBar = (rs.getString(4) != null);
				//
				if (node_ID == 0 && parent_ID == 0)
					m_root = new MTreeNode (node_ID, 0, m_name, m_description, 0, true, null, onBar);
				else
					addToTree (node_ID, parent_ID, seqNo, onBar);
			}
			rs.close();
			pstmt.close();
			//
			m_pstmtDetail.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.loadNodes", e);
		}
		//  Done with loading - add remainder from buffer
		if (m_buffer.size() != 0)
		{
			Log.trace(Log.l6_Database, "MTree.loadNodes - clearing buffer");
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				MTreeNode parent = m_root.findNode(node.getParent_ID());
				if (parent != null && parent.getAllowsChildren())
				{
					parent.add(node);
					checkBuffer(node);
					m_buffer.remove(i);
					i = -1;
				}
			}
		}

		//	Nodes w/o parent
		if (m_buffer.size() != 0)
		{
			Log.error ("MTree.loadNodes - Nodes w/o parent - adding to root - " + m_buffer);
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				m_root.add(node);
				checkBuffer(node);
				m_buffer.remove(i);
				i = -1;
			}
			if (m_buffer.size() != 0)
				Log.error ("MTree.loadNodes - still nodes in Buffer - " + m_buffer);
		}	//	nodes w/o parents

		//  clean up
		if (!m_editable && m_root.getChildCount() > 0)
			trimTree();
//		diagPrintTree();
	}   //  loadNodes

	/**
	 *  Add Node to Tree.
	 *  If not found add to buffer
	 *  @param node_ID Node_ID
	 *  @param parent_ID Parent_ID
	 *  @param seqNo SeqNo
	 *  @param onBar on bar
	 */
	private void addToTree (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		//  Create new Node
		MTreeNode child = getNodeDetail (node_ID, parent_ID, seqNo, onBar);
		if (child == null)
			return;

		//  Add to Tree
		MTreeNode parent = null;
		if (m_root != null)
			parent = m_root.findNode (parent_ID);
		//  Parent found
		if (parent != null && parent.getAllowsChildren())
		{
			parent.add(child);
			//  see if we can add nodes from buffer
			if (m_buffer.size() > 0)
				checkBuffer(child);
		}
		else
			m_buffer.add(child);
	}   //  addToTree

	/**
	 *  Check the buffer for nodes which have newNode as Parents
	 *  @param newNode new node
	 */
	private void checkBuffer (MTreeNode newNode)
	{
		for (int i = 0; i < m_buffer.size(); i++)
		{
			MTreeNode node = (MTreeNode)m_buffer.get(i);
			if (node.getParent_ID() == newNode.getID())
			{
				newNode.add(node);
				m_buffer.remove(i);
				i--;
			}
		}
	}   //  checkBuffer

	/*************************************************************************/

	/**
	 *  Prepare Node Detail.
	 *  Columns:
	 *  - Name
	 *  - Description
	 *  - IsSummary
	 *  - ImageIndicator
	 *  Parameter:
	 *  - Node_ID
	 *  The SQL contains security/access control
	 *  @return SQL to get Node Detail Info
	 */
	private String prepareNodeDetail ()
	{
		//  SQL for Node Info
		StringBuffer cmdNode = new StringBuffer();
		if (m_treeType.equals(MTree.TREETYPE_Menu))
		{
			boolean base = Env.isBaseLanguage(Env.getCtx(), "AD_Menu");
			if (base)
				cmdNode.append("SELECT m.Name,m.Description,m.IsSummary,m.Action "
					+ "FROM AD_Menu m");
			else
				cmdNode.append("SELECT t.Name,t.Description,m.IsSummary,m.Action "
					+ "FROM AD_Menu m, AD_Menu_Trl t");
			if (!m_editable)
				cmdNode.append(", (SELECT ").append(m_AD_Role_ID).append(" AS XRole FROM DUAL) x");
			cmdNode.append(" WHERE m.AD_Menu_ID=?");            //  #1
			if (!base)
				cmdNode.append(" AND m.AD_Menu_ID=t.AD_Menu_ID AND t.AD_Language='")
					.append(Env.getAD_Language(Env.getCtx())).append("'");
			if (!m_editable)
				cmdNode.append(" AND m.IsActive='Y' "
					+ "AND (m.IsSummary='Y' OR m.Action='B'"
					+ " OR EXISTS (SELECT * FROM AD_Window_Access wa WHERE wa.AD_Window_ID=m.AD_Window_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Process_Access wa WHERE wa.AD_Process_ID=m.AD_Process_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Form_Access wa WHERE wa.AD_Form_ID=m.AD_Form_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Task_Access wa WHERE wa.AD_Task_ID=m.AD_Task_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Workflow_Access wa WHERE wa.AD_Workflow_ID=m.AD_Workflow_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ ")");
		}
		else
		{
			cmdNode.append("SELECT Name,Description,IsSummary,NULL FROM ");
			//
			String sourceTable = null;
			if (m_treeType.equals(MTree.TREETYPE_Org))
				sourceTable = "AD_Org";
			else if (m_treeType.equals(MTree.TREETYPE_Product))
				sourceTable = "M_Product";
			else if (m_treeType.equals(MTree.TREETYPE_ProductCategory))
				sourceTable = "M_Product_Category";
			else if (m_treeType.equals(MTree.TREETYPE_BOM))
				sourceTable = "M_BOM";
			else if (m_treeType.equals(MTree.TREETYPE_ElementValue))
				sourceTable = "C_ElementValue";
			else if (m_treeType.equals(MTree.TREETYPE_BPartner))
				sourceTable = "C_BPartner";
			else if (m_treeType.equals(MTree.TREETYPE_Campaign))
				sourceTable = "C_Campaign";
			else if (m_treeType.equals(MTree.TREETYPE_Project))
				sourceTable = "C_Project";
			else if (m_treeType.equals(MTree.TREETYPE_Activity))
				sourceTable = "C_Activity";
			else if (m_treeType.equals(MTree.TREETYPE_SalesRegion))
				sourceTable = "C_SalesRegion";
			if (sourceTable == null)
			{
				throw new IllegalArgumentException("MTree.prepareNodeDetail - Unknown TreeType=" + m_treeType);
			}
			cmdNode.append(sourceTable).append(" WHERE ")
				.append(sourceTable).append("_ID=?");               // #1
			if (!m_editable)
				cmdNode.append(" AND IsActive='Y'");
		}
		Log.trace(Log.l6_Database, "MTree.prepareNodeDetail", cmdNode.toString());
		return cmdNode.toString();
	}   //  prepareNodeDetail

	/**
	 *  Get Node Details.
	 *  As SQL contains security access, not all nodes will be found
	 *  @param  node_ID     Key of the record
	 *  @param  parent_ID   Parent ID of the record
	 *  @param  seqNo       Sort index
	 *  @param  onBar       Node also on Shortcut bar
	 *  @return Node
	 */
	private MTreeNode getNodeDetail (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		MTreeNode retValue = null;
		try
		{
			m_pstmtDetail.setInt(1, node_ID);
			ResultSet rs = m_pstmtDetail.executeQuery();
			if (rs.next())
			{
				retValue = new MTreeNode (node_ID, seqNo,
					rs.getString(1),                //  name
					rs.getString(2),                //  description
					parent_ID,
					"Y".equals(rs.getString(3)),    //  IsSummary
					rs.getString(4),                //  ImageIndicator
					onBar);
			}
			rs.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getNodeDetail", e);
		}
		return retValue;
	}   //  getNodeDetails

	/*************************************************************************/

	/**
	 *  Trim tree of empty summary nodes
	 */
	private void trimTree()
	{
		boolean needsTrim = m_root != null;
		while (needsTrim)
		{
			needsTrim = false;
			Enumeration en = m_root.preorderEnumeration();
			while (en.hasMoreElements())
			{
				MTreeNode nd = (MTreeNode)en.nextElement();
				if (nd.isSummary() && nd.getChildCount() == 0)
				{
					nd.removeFromParent();
					needsTrim = true;
				}
			}
		}
	}   //  trimTree

	/**
	 *  Diagnostics: Print tree
	 */
	private void diagPrintTree()
	{
		Enumeration en = m_root.preorderEnumeration();
		int count = 0;
		while (en.hasMoreElements())
		{
			StringBuffer sb = new StringBuffer();
			MTreeNode nd = (MTreeNode)en.nextElement();
			for (int i = 0; i < nd.getLevel(); i++)
				sb.append(" ");
			sb.append("ID=").append(nd.getID())
				.append(", SeqNo=").append(nd.getSeqNo())
				.append(" ").append(nd.getName());
			System.out.println(sb.toString());
			count++;
		}
		System.out.println("Count=" + count);
	}   //  diagPrintTree

	/**
	 *  Get Root node
	 *  @return root
	 */
	public MTreeNode getRoot()
	{
		return m_root;
	}   //  getRoot

	/**
	 *  Get Table Name
	 *  @return Table Name
	 */
	public String getTableName()
	{
		return m_tableName;
	}   //  getTableName

	/**
	 *  String representation
	 *  @return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MTree[");
		sb.append("AD_Tree_ID=").append(m_AD_Tree_ID)
			.append(", Name=").append(m_name);
		sb.append("]");
		return sb.toString();
	}
}   //  MTree
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

import java.awt.*;
import java.sql.*;
import java.util.*;
import javax.sql.*;

import org.compiere.util.*;
import org.compiere.print.*;

/**
 *  Builds Tree.
 *  Creates tree structure - maintained in VTreePanel
 *
 *  @author     Jorg Janke
 *  @version    $Id: MTree.java,v 1.20 2004/05/02 01:40:27 jjanke Exp $
 */
public class MTree extends MTree_Base
{
	/**
	 *  Default Constructor.
	 * 	Need to call loadNodes explicitly
	 * 	@param ctx context for security
	 *  @param AD_Tree_ID   The tree to build
	 */
	public MTree (Properties ctx, int AD_Tree_ID)
	{
		super (ctx, AD_Tree_ID);
	}   //  MTree

	/**
	 *  Construct & Load Tree
	 *  @param AD_Tree_ID   The tree to build
	 *  @param editable     True, if tree can be modified
	 *  - includes inactive and empty summary nodes
	 * 	@param ctx context for security
	 */
	public MTree (Properties ctx, int AD_Tree_ID, boolean editable)
	{
		this (ctx, AD_Tree_ID);
		m_editable = editable;
		int AD_User_ID = Env.getContextAsInt(ctx, "AD_User_ID");
		log.info("AD_Tree_ID=" + AD_Tree_ID
			+ ", AD_User_ID=" + AD_User_ID 
			+ ", Editable=" + editable);
		//
		loadNodes(AD_User_ID);
	}   //  MTree


	/** Is Tree editable    	*/
	private boolean     		m_editable = false;
	/** Root Node                   */
	private MTreeNode           m_root = null;
	/** Buffer while loading tree   */
	private ArrayList           m_buffer = new ArrayList();
	/** Prepared Statement for Node Details */
	private RowSet			   	m_nodeRowSet;

	
	/**************************************************************************
	 *  Get primary AD_Tree_ID for KeyColumn.
	 *  Called from GridController
	 *  @param keyColumnName key column name, eg. C_Project_ID
	 *  @return AD_Tree_ID
	 */
	public static int getAD_Tree_ID (String keyColumnName)
	{
		Log.trace(Log.l4_Data, "MTree.getAD_Tree_ID", keyColumnName);
		if (keyColumnName == null || keyColumnName.length() == 0)
			return 0;

		String TreeType = null;
		if (keyColumnName.equals("AD_Menu_ID"))
			TreeType = TREETYPE_Menu; 
		else if (keyColumnName.equals("C_ElementValue_ID"))
			TreeType = TREETYPE_ElementValue;
		else if (keyColumnName.equals("M_Product_ID"))
			TreeType = TREETYPE_Product;
		else if (keyColumnName.equals("C_BPartner_ID"))
			TreeType = TREETYPE_BPartner;
		else if (keyColumnName.equals("AD_Org_ID"))
			TreeType = TREETYPE_Organization;
		else if (keyColumnName.equals("C_Project_ID"))
			TreeType = TREETYPE_Project;
		else if (keyColumnName.equals("M_ProductCategory_ID"))
			TreeType = TREETYPE_ProductCategory;
		else if (keyColumnName.equals("M_BOM_ID"))
			TreeType = TREETYPE_BoM;
		else if (keyColumnName.equals("C_SalesRegion_ID"))
			TreeType = TREETYPE_SalesRegion;
		else if (keyColumnName.equals("C_Campaign_ID"))
			TreeType = TREETYPE_Campaign;
		else if (keyColumnName.equals("C_Activity_ID"))
			TreeType = TREETYPE_Activity;
		else
		{
			Log.error("MTree.getAD_Tree_ID - Could not map " + keyColumnName);
			return 0;
		}

		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getAD_Tree_ID", e);
		}

		return AD_Tree_ID;
	}   //  getAD_Tree_ID


	/**
	 * 	Get Primary Tree of Tree Type
	 * 	@param TreeType see TREETYPE_
	 * 	@param ctx context for Security
	 * 	@return MTree
	 */
	public static MTree getTree (Properties ctx, String TreeType)
	{
		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getTree", e);
		}
		//	Not found
		if (AD_Tree_ID == 0)
		{
			Log.trace(Log.l4_Data, "MTree.getTree - No AD_Tree_ID for TreeType=" + TreeType + ", AD_Client_ID=" + AD_Client_ID);
			return null;
		}
		//
		MTree tree = new MTree (ctx, AD_Tree_ID, false);
		return tree;
	}	//	getTree


	/*************************************************************************
	 *  Load Nodes and Bar
	 * 	@param AD_User_ID user
	 */
	private void loadNodes (int AD_User_ID)
	{
		//  SQL for TreeNodes
		StringBuffer sql = new StringBuffer("SELECT "
			+ "tn.Node_ID,tn.Parent_ID,tn.SeqNo,tb.IsActive "
			+ "FROM ").append(getNodeTableName()).append(" tn"
			+ " LEFT OUTER JOIN AD_TreeBar tb ON (tn.AD_Tree_ID=tb.AD_Tree_ID"
			+ " AND tn.Node_ID=tb.Node_ID AND tb.AD_User_ID=?) "	//	#1
			+ "WHERE tn.AD_Tree_ID=?");								//	#2
		if (!m_editable)
			sql.append(" AND tn.IsActive='Y'");
		sql.append(" ORDER BY COALESCE(tn.Parent_ID, -1), tn.SeqNo");
		Log.trace(9, "MTree.loadNodes", sql.toString());

		//  The Node Loop
		try
		{
			// load Node details - addToTree -> getNodeDetail
			getNodeDetails(); 
			//
			PreparedStatement pstmt = DB.prepareStatement(sql.toString());
			pstmt.setInt(1, AD_User_ID);
			pstmt.setInt(2, getAD_Tree_ID());
			//	Get Tree & Bar
			ResultSet rs = pstmt.executeQuery();
			m_root = new MTreeNode (0, 0, getName(), getDescription(), 0, true, null, false, null);
			while (rs.next())
			{
				int node_ID = rs.getInt(1);
				int parent_ID = rs.getInt(2);
				int seqNo = rs.getInt(3);
				boolean onBar = (rs.getString(4) != null);
				//
				if (node_ID == 0 && parent_ID == 0)
					;
				else
					addToTree (node_ID, parent_ID, seqNo, onBar);	//	calls getNodeDetail
			}
			rs.close();
			pstmt.close();
			//
			m_nodeRowSet.close();
			m_nodeRowSet = null;
		}
		catch (SQLException e)
		{
			Log.error("MTree.loadNodes", e);
			m_nodeRowSet = null;
		}
			
		//  Done with loading - add remainder from buffer
		if (m_buffer.size() != 0)
		{
			Log.trace(Log.l6_Database, "MTree.loadNodes - clearing buffer - Adding to: " + m_root);
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				MTreeNode parent = m_root.findNode(node.getParent_ID());
				if (parent != null && parent.getAllowsChildren())
				{
					parent.add(node);
					checkBuffer(node);
					m_buffer.remove(i);
					i = -1;		//	start again with i=0
				}
			}
		}

		//	Nodes w/o parent
		if (m_buffer.size() != 0)
		{
			Log.error ("MTree.loadNodes - Nodes w/o parent - adding to root - " + m_buffer);
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				m_root.add(node);
				checkBuffer(node);
				m_buffer.remove(i);
				i = -1;
			}
			if (m_buffer.size() != 0)
				Log.error ("MTree.loadNodes - still nodes in Buffer - " + m_buffer);
		}	//	nodes w/o parents

		//  clean up
		if (!m_editable && m_root.getChildCount() > 0)
			trimTree();
//		diagPrintTree();
	}   //  loadNodes

	/**
	 *  Add Node to Tree.
	 *  If not found add to buffer
	 *  @param node_ID Node_ID
	 *  @param parent_ID Parent_ID
	 *  @param seqNo SeqNo
	 *  @param onBar on bar
	 */
	private void addToTree (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		//  Create new Node
		MTreeNode child = getNodeDetail (node_ID, parent_ID, seqNo, onBar);
		if (child == null)
			return;

		//  Add to Tree
		MTreeNode parent = null;
		if (m_root != null)
			parent = m_root.findNode (parent_ID);
		//  Parent found
		if (parent != null && parent.getAllowsChildren())
		{
			parent.add(child);
			//  see if we can add nodes from buffer
			if (m_buffer.size() > 0)
				checkBuffer(child);
		}
		else
			m_buffer.add(child);
	}   //  addToTree

	/**
	 *  Check the buffer for nodes which have newNode as Parents
	 *  @param newNode new node
	 */
	private void checkBuffer (MTreeNode newNode)
	{
		//	Ability to add nodes
		if (!newNode.isSummary() || !newNode.getAllowsChildren())
			return;
		//
		for (int i = 0; i < m_buffer.size(); i++)
		{
			MTreeNode node = (MTreeNode)m_buffer.get(i);
			if (node.getParent_ID() == newNode.getNode_ID())
			{
				newNode.add(node);
				m_buffer.remove(i);
				i--;
			}
		}
	}   //  checkBuffer

	
	
	/**************************************************************************
	 *  Get Node Detail.
	 * 	Loads data into RowSet m_nodeRowSet
	 *  Columns:
	 * 	- ID
	 *  - Name
	 *  - Description
	 *  - IsSummary
	 *  - ImageIndicator
	 * 	- additional for Menu
	 *  Parameter:
	 *  - Node_ID
	 *  The SQL contains security/access control
	 *  @return SQL to get Node Detail Info
	 */
	private void getNodeDetails ()
	{
		//  SQL for Node Info
		StringBuffer sqlNode = new StringBuffer();
		String sourceTable = "t";
		String fromClause = getSourceTableName(false);	//	fully qualified
		String columnNameX = getSourceTableName(true);
		String color = getActionColorName();
		if (getTreeType().equals(TREETYPE_Menu))
		{
			boolean base = Env.isBaseLanguage(Env.getCtx(), "AD_Menu");
			sourceTable = "m";
			if (base)
				sqlNode.append("SELECT m.AD_Menu_ID, m.Name,m.Description,m.IsSummary,m.Action, "
					+ "m.AD_Window_ID, m.AD_Process_ID, m.AD_Form_ID, m.AD_Workflow_ID, m.AD_Task_ID, m.AD_Workbench_ID "
					+ "FROM AD_Menu m");
			else
				sqlNode.append("SELECT m.AD_Menu_ID,  t.Name,t.Description,m.IsSummary,m.Action, "
					+ "m.AD_Window_ID, m.AD_Process_ID, m.AD_Form_ID, m.AD_Workflow_ID, m.AD_Task_ID, m.AD_Workbench_ID "
					+ "FROM AD_Menu m, AD_Menu_Trl t");
			if (!base)
				sqlNode.append(" WHERE m.AD_Menu_ID=t.AD_Menu_ID AND t.AD_Language='")
					.append(Env.getAD_Language(Env.getCtx())).append("'");
			if (!m_editable)
			{
				boolean hasWhere = sqlNode.indexOf(" WHERE ") != -1;
				sqlNode.append(hasWhere ? " AND " : " WHERE ").append(" m.IsActive='Y' ");
			}
		}
		else
		{
			if (columnNameX == null)
				throw new IllegalArgumentException("MTree.prepareNodeDetail - Unknown TreeType=" + getTreeType());
			sqlNode.append("SELECT t.").append(columnNameX)
				.append("_ID,t.Name,t.Description,t.IsSummary,").append(color)
				.append(" FROM ").append(fromClause);
			if (!m_editable)
				sqlNode.append(" WHERE t.IsActive='Y'");
		}
		String sql = sqlNode.toString();
		if (!m_editable)	//	editable = menu/etc. window
			sql = MRole.getDefault(getCtx(), false).addAccessSQL(sql, 
				sourceTable, MRole.SQL_FULLYQUALIFIED, m_editable);
		log.debug("getNodeDetails - " + sql);
		m_nodeRowSet = DB.getRowSet (sql, true);
	}   //  getNodeDetails

	/**
	 *  Get Node Details.
	 *  As SQL contains security access, not all nodes will be found
	 *  @param  node_ID     Key of the record
	 *  @param  parent_ID   Parent ID of the record
	 *  @param  seqNo       Sort index
	 *  @param  onBar       Node also on Shortcut bar
	 *  @return Node
	 */
	private MTreeNode getNodeDetail (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		MTreeNode retValue = null;
		try
		{
			m_nodeRowSet.beforeFirst();
			while (m_nodeRowSet.next())
			{
				int node = m_nodeRowSet.getInt(1);				
				if (node_ID != node)	//	search for correct one
					continue;
				//	ID,Name,Description,IsSummary,Action/Color
				int index = 2;				
				String name = m_nodeRowSet.getString(index++); 
				String description = m_nodeRowSet.getString(index++);
				boolean isSummary = "Y".equals(m_nodeRowSet.getString(index++));
				String actionColor = m_nodeRowSet.getString(index++);
				//	Menu only
				if (getTreeType().equals(TREETYPE_Menu) && !isSummary)
				{
					int AD_Window_ID = m_nodeRowSet.getInt(index++);
					int AD_Process_ID = m_nodeRowSet.getInt(index++);
					int AD_Form_ID = m_nodeRowSet.getInt(index++);
					int AD_Workflow_ID = m_nodeRowSet.getInt(index++);
					int AD_Task_ID = m_nodeRowSet.getInt(index++);
					int AD_Workbench_ID = m_nodeRowSet.getInt(index++);
					//
					MRole role = MRole.getDefault(getCtx(), false);
					Boolean access = null;
					if (X_AD_Menu.ACTION_Window.equals(actionColor))
						access = role.getWindowAccess(AD_Window_ID);
					else if (X_AD_Menu.ACTION_Process.equals(actionColor) 
						|| X_AD_Menu.ACTION_Report.equals(actionColor))
						access = role.getProcessAccess(AD_Process_ID);
					else if (X_AD_Menu.ACTION_Form.equals(actionColor))
						access = role.getFormAccess(AD_Form_ID);
					else if (X_AD_Menu.ACTION_WorkFlow.equals(actionColor))
						access = role.getWorkflowAccess(AD_Workflow_ID);
					else if (X_AD_Menu.ACTION_Task.equals(actionColor))
						access = role.getTaskAccess(AD_Task_ID);
				//	else if (X_AD_Menu.ACTION_Workbench.equals(action))
				//		access = role.getWorkbenchAccess(AD_Window_ID);
				//	log.debug("getNodeDetail - " + name + " - " + actionColor + " - " + access);
					//
					if (access != null		//	rw or ro for Role 
						|| m_editable)		//	Menu Window can see all
					{
						retValue = new MTreeNode (node_ID, seqNo,
							name, description, parent_ID, isSummary,
							actionColor, onBar, null);	//	menu has no color
					}
				}
				else	//	always add
				{
					Color color = null;	//	action
					MPrintColor printColor = MPrintColor.get(actionColor);
					if (printColor != null)
						color = printColor.getColor(); 
					//
					retValue = new MTreeNode (node_ID, seqNo,
						name, description, parent_ID, isSummary,
						null, onBar, color);
				}
			}
		}
		catch (SQLException e)
		{
			log.error("getNodeDetails", e);
		}
		return retValue;
	}   //  getNodeDetails

	/*************************************************************************/

	/**
	 *  Trim tree of empty summary nodes
	 */
	private void trimTree()
	{
		boolean needsTrim = m_root != null;
		while (needsTrim)
		{
			needsTrim = false;
			Enumeration en = m_root.preorderEnumeration();
			while (en.hasMoreElements())
			{
				MTreeNode nd = (MTreeNode)en.nextElement();
				if (nd.isSummary() && nd.getChildCount() == 0)
				{
					nd.removeFromParent();
					needsTrim = true;
				}
			}
		}
	}   //  trimTree

	/**
	 *  Diagnostics: Print tree
	 */
	private void dumpTree()
	{
		Enumeration en = m_root.preorderEnumeration();
		int count = 0;
		while (en.hasMoreElements())
		{
			StringBuffer sb = new StringBuffer();
			MTreeNode nd = (MTreeNode)en.nextElement();
			for (int i = 0; i < nd.getLevel(); i++)
				sb.append(" ");
			sb.append("ID=").append(nd.getNode_ID())
				.append(", SeqNo=").append(nd.getSeqNo())
				.append(" ").append(nd.getName());
			System.out.println(sb.toString());
			count++;
		}
		System.out.println("Count=" + count);
	}   //  diagPrintTree

	/**
	 *  Get Root node
	 *  @return root
	 */
	public MTreeNode getRoot()
	{
		return m_root;
	}   //  getRoot

	/**
	 * 	Is Menu Tree
	 *	@return true if menu
	 */
	public boolean isMenu()
	{
		return TREETYPE_Menu.equals(getTreeType());
	}	//	isMenu

	/**
	 * 	Is Product Tree
	 *	@return true if product
	 */
	public boolean isProduct()
	{
		return TREETYPE_Product.equals(getTreeType());
	}	//	isProduct
	
	/**
	 * 	Is Business Partner Tree
	 *	@return true if partner
	 */
	public boolean isBPartner()
	{
		return TREETYPE_BPartner.equals(getTreeType());
	}	//	isBPartner
	
	/**
	 *  String representation
	 *  @return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MTree[");
		sb.append("AD_Tree_ID=").append(getAD_Tree_ID())
			.append(", Name=").append(getName());
		sb.append("]");
		return sb.toString();
	}
}   //  MTree
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

import java.sql.*;
import java.util.*;

import org.compiere.util.*;

/**
 *  Builds Tree.
 *  Creates tree structure - maintained in VTreePanel
 *
 *  @author     Jorg Janke
 *  @version    $Id: MTree.java,v 1.2 2003/02/19 06:48:23 jjanke Exp $
 */
public class MTree
{
	/**
	 *  Construct Tree
	 *  @param AD_Tree_ID   The tree to build
	 *  @param editable     True, if tree can be modified
	 *  - includes inactive and empty summary nodes
	 */
	public MTree (int AD_Tree_ID, boolean editable)
	{
		this (AD_Tree_ID, 0, 0, editable);
	}	//	MTree

	/**
	 *  Construct Tree
	 *  @param AD_Tree_ID   The tree to build
	 *  @param AD_User_ID   Tree Bar Access control - optional
	 *  @param AD_Role_ID   Menu Access control
	 *  @param editable     True, if tree can be modified
	 *  - includes inactive and empty summary nodes
	 */
	public MTree (int AD_Tree_ID,
		int AD_User_ID, int AD_Role_ID, boolean editable)
	{
		Log.trace(Log.l4_Data, "MTree - AD_Tree_ID=" + AD_Tree_ID,
			"AD_Role_ID=" + AD_Role_ID + ", AD_User_ID=" + AD_User_ID
			+ ", Editable=" + editable);

		m_AD_Tree_ID = AD_Tree_ID;
		m_AD_User_ID = AD_User_ID;
		m_AD_Role_ID = AD_Role_ID;
		m_editable = editable;
		loadTree();
	}   //  MTree

	/** Tree ID             */
	private int         m_AD_Tree_ID;
	/** Tree for User       */
	private int         m_AD_User_ID;
	/** Role Access Control */
	private int         m_AD_Role_ID;
	/** Is Tree editable    */
	private boolean     m_editable;

	/** Tree Name           */
	private String      m_name;
	/** Tree Description    */
	private String	    m_description;
	/** Tree Type           */
	private String      m_treeType;

	/** Table Name          */
	private String      m_tableName = "AD_TreeNode";

	/** Root Node                   */
	private MTreeNode           m_root = null;
	/** Buffer while loading tree   */
	private ArrayList           m_buffer = new ArrayList();
	/** Prepared Statement for Node Details */
	private PreparedStatement   m_pstmtDetail;

	/*************************************************************************/

	/** Tree Type Menu MM   */
	public static final String	TREETYPE_Menu =         "MM";
	/** Tree Type Account ElementValue EV   */
	public static final String	TREETYPE_ElementValue = "EV";
	/** Tree Type Product PR   */
	public static final String	TREETYPE_Product =      "PR";
	/** Tree Type BusinessPartner BP   */
	public static final String	TREETYPE_BPartner =     "BP";
	/** Tree Type Organization OO   */
	public static final String	TREETYPE_Org =          "OO";
	/** Tree Type Project PJ   */
	public static final String	TREETYPE_Project =      "PJ";
	/** Tree Type ProductCategory PC   */
	public static final String	TREETYPE_ProductCategory = "PC";
	/** Tree Type BOM BB   */
	public static final String	TREETYPE_BOM =          "BB";
	/** Tree Type SalesRegion SR   */
	public static final String	TREETYPE_SalesRegion =  "SR";
	/** Tree Type Camoaign MC   */
	public static final String	TREETYPE_Campaign =     "MC";
	/** Tree Type Activity AY   */
	public static final String	TREETYPE_Activity =     "AY";

	/*************************************************************************/

	/**
	 *  Get primary AD_Tree_ID for KeyColumn.
	 *  Called from GridController
	 *  @param keyColumnName key column name, eg. C_Project_ID
	 *  @return AD_Tree_ID
	 */
	public static int getAD_Tree_ID (String keyColumnName)
	{
		Log.trace(Log.l4_Data, "MTree.getAD_Tree_ID", keyColumnName);
		if (keyColumnName == null || keyColumnName.length() == 0)
			return 0;

		String TreeType = null;
		if (keyColumnName.equals("AD_Menu_ID"))
			TreeType = MTree.TREETYPE_Menu;
		else if (keyColumnName.equals("C_ElementValue_ID"))
			TreeType = MTree.TREETYPE_ElementValue;
		else if (keyColumnName.equals("M_Product_ID"))
			TreeType = MTree.TREETYPE_Product;
		else if (keyColumnName.equals("C_BPartner_ID"))
			TreeType = MTree.TREETYPE_BPartner;
		else if (keyColumnName.equals("AD_Org_ID"))
			TreeType = MTree.TREETYPE_Org;
		else if (keyColumnName.equals("C_Project_ID"))
			TreeType = MTree.TREETYPE_Project;
		else if (keyColumnName.equals("M_ProductCategory_ID"))
			TreeType = MTree.TREETYPE_ProductCategory;
		else if (keyColumnName.equals("M_BOM_ID"))
			TreeType = MTree.TREETYPE_BOM;
		else if (keyColumnName.equals("C_SalesRegion_ID"))
			TreeType = MTree.TREETYPE_SalesRegion;
		else if (keyColumnName.equals("C_Campaign_ID"))
			TreeType = MTree.TREETYPE_Campaign;
		else if (keyColumnName.equals("C_Activity_ID"))
			TreeType = MTree.TREETYPE_Activity;
		else
		{
			Log.error("MTree.getAD_Tree_ID - Could not map " + keyColumnName);
			return 0;
		}

		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getAD_Tree_ID", e);
		}

		return AD_Tree_ID;
	}   //  getAD_Tree_ID


	/**
	 * 	Get Primary Tree of Tree Type
	 * 	@param TreeType see TREETYPE_
	 * 	@return MTree
	 */
	public static MTree getTree (String TreeType)
	{
		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getTree", e);
		}
		//	Not found
		if (AD_Tree_ID == 0)
		{
			Log.trace(Log.l4_Data, "MTree.getTree - No AD_Tree_ID for TreeType=" + TreeType + ", AD_Client_ID=" + AD_Client_ID);
			return null;
		}
		//
		MTree tree = new MTree (AD_Tree_ID, false);
		return tree;
	}	//	getTree


	/*************************************************************************/

	/**
	 *  Load Tree
	 */
	private void loadTree()
	{
		//  Get Tree info
		String sql = "SELECT Name, Description, TreeType "
			+ "FROM AD_Tree "
			+ "WHERE AD_Tree_ID=?"
			+ " AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, m_AD_Tree_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				m_name = rs.getString(1);
				m_description = rs.getString(2);
				if (m_description == null)
					m_description = "";
				m_treeType = rs.getString(3);
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.loadTree", e);
			return;
		}
		loadNodes();
	}   //  loadTree

	/**
	 *  Load Nodes
	 */
	private void loadNodes()
	{
		//  TableName: AD_TreeNode
		if (m_treeType.equals(TREETYPE_Menu))
			;   //  m_tableName += "MM";
		else if  (m_treeType.equals(TREETYPE_BPartner))
			m_tableName += "BP";
		else if  (m_treeType.equals(TREETYPE_Product))
			m_tableName += "PR";

		//  SQL for TreeNodes
		StringBuffer cmd = new StringBuffer("SELECT "
			+ "tn.Node_ID,tn.Parent_ID,tn.SeqNo,tb.IsActive "
			+ "FROM ").append(m_tableName).append(" tn"
			+ " LEFT OUTER JOIN AD_TreeBar tb ON (tn.AD_Tree_ID=tb.AD_Tree_ID"
			+ " AND tn.Node_ID=tb.Node_ID AND tb.AD_User_ID=?) "	//	#1
			+ "WHERE tn.AD_Tree_ID=?");								//	#2
		if (!m_editable)
			cmd.append(" AND tn.IsActive='Y'");
		cmd.append(" ORDER BY COALESCE(tn.Parent_ID, -1), tn.SeqNo");
		Log.trace(Log.l6_Database, "MTree.loadNodes", cmd.toString());

		//  The Node Loop
		try
		{
			m_pstmtDetail = DB.prepareStatement(prepareNodeDetail());
			//
			PreparedStatement pstmt = DB.prepareStatement(cmd.toString());
			pstmt.setInt(1, m_AD_User_ID);
			pstmt.setInt(2, m_AD_Tree_ID);
			//
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int node_ID = rs.getInt(1);
				int parent_ID = rs.getInt(2);
				int seqNo = rs.getInt(3);
				boolean onBar = (rs.getString(4) != null);
				//
				if (node_ID == 0 && parent_ID == 0)
					m_root = new MTreeNode (node_ID, 0, m_name, m_description, 0, true, null, onBar);
				else
					addToTree (node_ID, parent_ID, seqNo, onBar);
			}
			rs.close();
			pstmt.close();
			//
			m_pstmtDetail.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.loadNodes", e);
		}
		//  Done with loading - add remainder from buffer
		if (m_buffer.size() != 0)
		{
			Log.trace(Log.l6_Database, "MTree.loadNodes - clearing buffer");
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				MTreeNode parent = m_root.findNode(node.getParent_ID());
				if (parent != null && parent.getAllowsChildren())
				{
					parent.add(node);
					checkBuffer(node);
					m_buffer.remove(i);
					i = -1;
				}
			}
		}

		//	Nodes w/o parent
		if (m_buffer.size() != 0)
		{
			Log.error ("MTree.loadNodes - Nodes w/o parent - adding to root - " + m_buffer);
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				m_root.add(node);
				checkBuffer(node);
				m_buffer.remove(i);
				i = -1;
			}
			if (m_buffer.size() != 0)
				Log.error ("MTree.loadNodes - still nodes in Buffer - " + m_buffer);
		}	//	nodes w/o parents

		//  clean up
		if (!m_editable && m_root.getChildCount() > 0)
			trimTree();
//		diagPrintTree();
	}   //  loadNodes

	/**
	 *  Add Node to Tree.
	 *  If not found add to buffer
	 *  @param node_ID Node_ID
	 *  @param parent_ID Parent_ID
	 *  @param seqNo SeqNo
	 *  @param onBar on bar
	 */
	private void addToTree (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		//  Create new Node
		MTreeNode child = getNodeDetail (node_ID, parent_ID, seqNo, onBar);
		if (child == null)
			return;

		//  Add to Tree
		MTreeNode parent = null;
		if (m_root != null)
			parent = m_root.findNode (parent_ID);
		//  Parent found
		if (parent != null && parent.getAllowsChildren())
		{
			parent.add(child);
			//  see if we can add nodes from buffer
			if (m_buffer.size() > 0)
				checkBuffer(child);
		}
		else
			m_buffer.add(child);
	}   //  addToTree

	/**
	 *  Check the buffer for nodes which have newNode as Parents
	 *  @param newNode new node
	 */
	private void checkBuffer (MTreeNode newNode)
	{
		for (int i = 0; i < m_buffer.size(); i++)
		{
			MTreeNode node = (MTreeNode)m_buffer.get(i);
			if (node.getParent_ID() == newNode.getID())
			{
				newNode.add(node);
				m_buffer.remove(i);
				i--;
			}
		}
	}   //  checkBuffer

	/*************************************************************************/

	/**
	 *  Prepare Node Detail.
	 *  Columns:
	 *  - Name
	 *  - Description
	 *  - IsSummary
	 *  - ImageIndicator
	 *  Parameter:
	 *  - Node_ID
	 *  The SQL contains security/access control
	 *  @return SQL to get Node Detail Info
	 */
	private String prepareNodeDetail ()
	{
		//  SQL for Node Info
		StringBuffer cmdNode = new StringBuffer();
		if (m_treeType.equals(MTree.TREETYPE_Menu))
		{
			boolean base = Env.isBaseLanguage(Env.getCtx(), "AD_Menu");
			if (base)
				cmdNode.append("SELECT m.Name,m.Description,m.IsSummary,m.Action "
					+ "FROM AD_Menu m");
			else
				cmdNode.append("SELECT t.Name,t.Description,m.IsSummary,m.Action "
					+ "FROM AD_Menu m, AD_Menu_Trl t");
			if (!m_editable)
				cmdNode.append(", (SELECT ").append(m_AD_Role_ID).append(" AS XRole FROM DUAL) x");
			cmdNode.append(" WHERE m.AD_Menu_ID=?");            //  #1
			if (!base)
				cmdNode.append(" AND m.AD_Menu_ID=t.AD_Menu_ID AND t.AD_Language='")
					.append(Env.getAD_Language(Env.getCtx())).append("'");
			if (!m_editable)
				cmdNode.append(" AND m.IsActive='Y' "
					+ "AND (m.IsSummary='Y' OR m.Action='B'"
					+ " OR EXISTS (SELECT * FROM AD_Window_Access wa WHERE wa.AD_Window_ID=m.AD_Window_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Process_Access wa WHERE wa.AD_Process_ID=m.AD_Process_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Form_Access wa WHERE wa.AD_Form_ID=m.AD_Form_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Task_Access wa WHERE wa.AD_Task_ID=m.AD_Task_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Workflow_Access wa WHERE wa.AD_Workflow_ID=m.AD_Workflow_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ ")");
		}
		else
		{
			cmdNode.append("SELECT Name,Description,IsSummary,NULL FROM ");
			//
			String sourceTable = null;
			if (m_treeType.equals(MTree.TREETYPE_Org))
				sourceTable = "AD_Org";
			else if (m_treeType.equals(MTree.TREETYPE_Product))
				sourceTable = "M_Product";
			else if (m_treeType.equals(MTree.TREETYPE_ProductCategory))
				sourceTable = "M_Product_Category";
			else if (m_treeType.equals(MTree.TREETYPE_BOM))
				sourceTable = "M_BOM";
			else if (m_treeType.equals(MTree.TREETYPE_ElementValue))
				sourceTable = "C_ElementValue";
			else if (m_treeType.equals(MTree.TREETYPE_BPartner))
				sourceTable = "C_BPartner";
			else if (m_treeType.equals(MTree.TREETYPE_Campaign))
				sourceTable = "C_Campaign";
			else if (m_treeType.equals(MTree.TREETYPE_Project))
				sourceTable = "C_Project";
			else if (m_treeType.equals(MTree.TREETYPE_Activity))
				sourceTable = "C_Activity";
			else if (m_treeType.equals(MTree.TREETYPE_SalesRegion))
				sourceTable = "C_SalesRegion";
			if (sourceTable == null)
			{
				throw new IllegalArgumentException("MTree.prepareNodeDetail - Unknown TreeType=" + m_treeType);
			}
			cmdNode.append(sourceTable).append(" WHERE ")
				.append(sourceTable).append("_ID=?");               // #1
			if (!m_editable)
				cmdNode.append(" AND IsActive='Y'");
		}
		Log.trace(Log.l6_Database, "MTree.prepareNodeDetail", cmdNode.toString());
		return cmdNode.toString();
	}   //  prepareNodeDetail

	/**
	 *  Get Node Details.
	 *  As SQL contains security access, not all nodes will be found
	 *  @param  node_ID     Key of the record
	 *  @param  parent_ID   Parent ID of the record
	 *  @param  seqNo       Sort index
	 *  @param  onBar       Node also on Shortcut bar
	 *  @return Node
	 */
	private MTreeNode getNodeDetail (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		MTreeNode retValue = null;
		try
		{
			m_pstmtDetail.setInt(1, node_ID);
			ResultSet rs = m_pstmtDetail.executeQuery();
			if (rs.next())
			{
				retValue = new MTreeNode (node_ID, seqNo,
					rs.getString(1),                //  name
					rs.getString(2),                //  description
					parent_ID,
					"Y".equals(rs.getString(3)),    //  IsSummary
					rs.getString(4),                //  ImageIndicator
					onBar);
			}
			rs.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getNodeDetail", e);
		}
		return retValue;
	}   //  getNodeDetails

	/*************************************************************************/

	/**
	 *  Trim tree of empty summary nodes
	 */
	private void trimTree()
	{
		boolean needsTrim = m_root != null;
		while (needsTrim)
		{
			needsTrim = false;
			Enumeration en = m_root.preorderEnumeration();
			while (en.hasMoreElements())
			{
				MTreeNode nd = (MTreeNode)en.nextElement();
				if (nd.isSummary() && nd.getChildCount() == 0)
				{
					nd.removeFromParent();
					needsTrim = true;
				}
			}
		}
	}   //  trimTree

	/**
	 *  Diagnostics: Print tree
	 */
	private void diagPrintTree()
	{
		Enumeration en = m_root.preorderEnumeration();
		int count = 0;
		while (en.hasMoreElements())
		{
			StringBuffer sb = new StringBuffer();
			MTreeNode nd = (MTreeNode)en.nextElement();
			for (int i = 0; i < nd.getLevel(); i++)
				sb.append(" ");
			sb.append("ID=").append(nd.getID())
				.append(", SeqNo=").append(nd.getSeqNo())
				.append(" ").append(nd.getName());
			System.out.println(sb.toString());
			count++;
		}
		System.out.println("Count=" + count);
	}   //  diagPrintTree

	/**
	 *  Get Root node
	 *  @return root
	 */
	public MTreeNode getRoot()
	{
		return m_root;
	}   //  getRoot

	/**
	 *  Get Table Name
	 *  @return Table Name
	 */
	public String getTableName()
	{
		return m_tableName;
	}   //  getTableName

	/**
	 *  String representation
	 *  @return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MTree[");
		sb.append("AD_Tree_ID=").append(m_AD_Tree_ID)
			.append(", Name=").append(m_name);
		sb.append("]");
		return sb.toString();
	}
}   //  MTree
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

import java.awt.*;
import java.sql.*;
import java.util.*;

import javax.sql.*;

import org.compiere.print.*;
import org.compiere.util.*;

/**
 *  Builds Tree.
 *  Creates tree structure - maintained in VTreePanel
 *
 *  @author     Jorg Janke
 *  @version    $Id: MTree.java,v 1.24 2004/08/14 21:48:10 jjanke Exp $
 */
public class MTree extends MTree_Base
{
	/**
	 *  Default Constructor.
	 * 	Need to call loadNodes explicitly
	 * 	@param ctx context for security
	 *  @param AD_Tree_ID   The tree to build
	 */
	public MTree (Properties ctx, int AD_Tree_ID)
	{
		super (ctx, AD_Tree_ID);
	}   //  MTree

	/**
	 *  Construct & Load Tree
	 *  @param AD_Tree_ID   The tree to build
	 *  @param editable     True, if tree can be modified
	 *  - includes inactive and empty summary nodes
	 * 	@param ctx context for security
	 */
	public MTree (Properties ctx, int AD_Tree_ID, boolean editable)
	{
		this (ctx, AD_Tree_ID);
		m_editable = editable;
		int AD_User_ID = Env.getContextAsInt(ctx, "AD_User_ID");
		log.info("AD_Tree_ID=" + AD_Tree_ID
			+ ", AD_User_ID=" + AD_User_ID 
			+ ", Editable=" + editable);
		//
		loadNodes(AD_User_ID);
	}   //  MTree


	/** Is Tree editable    	*/
	private boolean     		m_editable = false;
	/** Root Node                   */
	private MTreeNode           m_root = null;
	/** Buffer while loading tree   */
	private ArrayList           m_buffer = new ArrayList();
	/** Prepared Statement for Node Details */
	private RowSet			   	m_nodeRowSet;

	
	/**************************************************************************
	 *  Get primary AD_Tree_ID for KeyColumn.
	 *  Called from GridController
	 *  @param keyColumnName key column name, eg. C_Project_ID
	 *  @return AD_Tree_ID
	 */
	public static int getAD_Tree_ID (String keyColumnName)
	{
		Log.trace(Log.l4_Data, "MTree.getAD_Tree_ID", keyColumnName);
		if (keyColumnName == null || keyColumnName.length() == 0)
			return 0;

		String TreeType = null;
		if (keyColumnName.equals("AD_Menu_ID"))
			TreeType = TREETYPE_Menu; 
		else if (keyColumnName.equals("C_ElementValue_ID"))
			TreeType = TREETYPE_ElementValue;
		else if (keyColumnName.equals("M_Product_ID"))
			TreeType = TREETYPE_Product;
		else if (keyColumnName.equals("C_BPartner_ID"))
			TreeType = TREETYPE_BPartner;
		else if (keyColumnName.equals("AD_Org_ID"))
			TreeType = TREETYPE_Organization;
		else if (keyColumnName.equals("C_Project_ID"))
			TreeType = TREETYPE_Project;
		else if (keyColumnName.equals("M_ProductCategory_ID"))
			TreeType = TREETYPE_ProductCategory;
		else if (keyColumnName.equals("M_BOM_ID"))
			TreeType = TREETYPE_BoM;
		else if (keyColumnName.equals("C_SalesRegion_ID"))
			TreeType = TREETYPE_SalesRegion;
		else if (keyColumnName.equals("C_Campaign_ID"))
			TreeType = TREETYPE_Campaign;
		else if (keyColumnName.equals("C_Activity_ID"))
			TreeType = TREETYPE_Activity;
		else
		{
			Log.error("MTree.getAD_Tree_ID - Could not map " + keyColumnName);
			return 0;
		}

		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getAD_Tree_ID", e);
		}

		return AD_Tree_ID;
	}   //  getAD_Tree_ID


	/**
	 * 	Get Primary Tree of Tree Type
	 * 	@param TreeType see TREETYPE_
	 * 	@param ctx context for Security
	 * 	@return MTree
	 */
	public static MTree getTree (Properties ctx, String TreeType)
	{
		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getTree", e);
		}
		//	Not found
		if (AD_Tree_ID == 0)
		{
			Log.trace(Log.l4_Data, "MTree.getTree - No AD_Tree_ID for TreeType=" + TreeType + ", AD_Client_ID=" + AD_Client_ID);
			return null;
		}
		//
		MTree tree = new MTree (ctx, AD_Tree_ID, false);
		return tree;
	}	//	getTree


	/*************************************************************************
	 *  Load Nodes and Bar
	 * 	@param AD_User_ID user
	 */
	private void loadNodes (int AD_User_ID)
	{
		//  SQL for TreeNodes
		StringBuffer sql = new StringBuffer("SELECT "
			+ "tn.Node_ID,tn.Parent_ID,tn.SeqNo,tb.IsActive "
			+ "FROM ").append(getNodeTableName()).append(" tn"
			+ " LEFT OUTER JOIN AD_TreeBar tb ON (tn.AD_Tree_ID=tb.AD_Tree_ID"
			+ " AND tn.Node_ID=tb.Node_ID AND tb.AD_User_ID=?) "	//	#1
			+ "WHERE tn.AD_Tree_ID=?");								//	#2
		if (!m_editable)
			sql.append(" AND tn.IsActive='Y'");
		sql.append(" ORDER BY COALESCE(tn.Parent_ID, -1), tn.SeqNo");
		if (Log.isTraceLevel(8))
			log.debug("loadNodes - " + sql.toString());

		//  The Node Loop
		try
		{
			// load Node details - addToTree -> getNodeDetail
			getNodeDetails(); 
			//
			PreparedStatement pstmt = DB.prepareStatement(sql.toString());
			pstmt.setInt(1, AD_User_ID);
			pstmt.setInt(2, getAD_Tree_ID());
			//	Get Tree & Bar
			ResultSet rs = pstmt.executeQuery();
			m_root = new MTreeNode (0, 0, getName(), getDescription(), 0, true, null, false, null);
			while (rs.next())
			{
				int node_ID = rs.getInt(1);
				int parent_ID = rs.getInt(2);
				int seqNo = rs.getInt(3);
				boolean onBar = (rs.getString(4) != null);
				//
				if (node_ID == 0 && parent_ID == 0)
					;
				else
					addToTree (node_ID, parent_ID, seqNo, onBar);	//	calls getNodeDetail
			}
			rs.close();
			pstmt.close();
			//
			m_nodeRowSet.close();
			m_nodeRowSet = null;
		}
		catch (SQLException e)
		{
			log.error("loadNodes", e);
			m_nodeRowSet = null;
		}
			
		//  Done with loading - add remainder from buffer
		if (m_buffer.size() != 0)
		{
			if (Log.isTraceLevel(8))
				log.debug("loadNodes - clearing buffer - Adding to: " + m_root);
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				MTreeNode parent = m_root.findNode(node.getParent_ID());
				if (parent != null && parent.getAllowsChildren())
				{
					parent.add(node);
					checkBuffer(node);
					m_buffer.remove(i);
					i = -1;		//	start again with i=0
				}
			}
		}

		//	Nodes w/o parent
		if (m_buffer.size() != 0)
		{
			log.error ("loadNodes - Nodes w/o parent - adding to root - " + m_buffer);
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				m_root.add(node);
				checkBuffer(node);
				m_buffer.remove(i);
				i = -1;
			}
			if (m_buffer.size() != 0)
				log.error ("loadNodes - still nodes in Buffer - " + m_buffer);
		}	//	nodes w/o parents

		//  clean up
		if (!m_editable && m_root.getChildCount() > 0)
			trimTree();
//		diagPrintTree();
		if (Log.isTraceLevel(8) || m_root.getChildCount() == 0)
			log.debug("loadTree - ChildCount=" + m_root.getChildCount());
	}   //  loadNodes

	/**
	 *  Add Node to Tree.
	 *  If not found add to buffer
	 *  @param node_ID Node_ID
	 *  @param parent_ID Parent_ID
	 *  @param seqNo SeqNo
	 *  @param onBar on bar
	 */
	private void addToTree (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		//  Create new Node
		MTreeNode child = getNodeDetail (node_ID, parent_ID, seqNo, onBar);
		if (child == null)
			return;

		//  Add to Tree
		MTreeNode parent = null;
		if (m_root != null)
			parent = m_root.findNode (parent_ID);
		//  Parent found
		if (parent != null && parent.getAllowsChildren())
		{
			parent.add(child);
			//  see if we can add nodes from buffer
			if (m_buffer.size() > 0)
				checkBuffer(child);
		}
		else
			m_buffer.add(child);
	}   //  addToTree

	/**
	 *  Check the buffer for nodes which have newNode as Parents
	 *  @param newNode new node
	 */
	private void checkBuffer (MTreeNode newNode)
	{
		//	Ability to add nodes
		if (!newNode.isSummary() || !newNode.getAllowsChildren())
			return;
		//
		for (int i = 0; i < m_buffer.size(); i++)
		{
			MTreeNode node = (MTreeNode)m_buffer.get(i);
			if (node.getParent_ID() == newNode.getNode_ID())
			{
				newNode.add(node);
				m_buffer.remove(i);
				i--;
			}
		}
	}   //  checkBuffer

	
	
	/**************************************************************************
	 *  Get Node Detail.
	 * 	Loads data into RowSet m_nodeRowSet
	 *  Columns:
	 * 	- ID
	 *  - Name
	 *  - Description
	 *  - IsSummary
	 *  - ImageIndicator
	 * 	- additional for Menu
	 *  Parameter:
	 *  - Node_ID
	 *  The SQL contains security/access control
	 *  @return SQL to get Node Detail Info
	 */
	private void getNodeDetails ()
	{
		//  SQL for Node Info
		StringBuffer sqlNode = new StringBuffer();
		String sourceTable = "t";
		String fromClause = getSourceTableName(false);	//	fully qualified
		String columnNameX = getSourceTableName(true);
		String color = getActionColorName();
		if (getTreeType().equals(TREETYPE_Menu))
		{
			boolean base = Env.isBaseLanguage(Env.getCtx(), "AD_Menu");
			sourceTable = "m";
			if (base)
				sqlNode.append("SELECT m.AD_Menu_ID, m.Name,m.Description,m.IsSummary,m.Action, "
					+ "m.AD_Window_ID, m.AD_Process_ID, m.AD_Form_ID, m.AD_Workflow_ID, m.AD_Task_ID, m.AD_Workbench_ID "
					+ "FROM AD_Menu m");
			else
				sqlNode.append("SELECT m.AD_Menu_ID,  t.Name,t.Description,m.IsSummary,m.Action, "
					+ "m.AD_Window_ID, m.AD_Process_ID, m.AD_Form_ID, m.AD_Workflow_ID, m.AD_Task_ID, m.AD_Workbench_ID "
					+ "FROM AD_Menu m, AD_Menu_Trl t");
			if (!base)
				sqlNode.append(" WHERE m.AD_Menu_ID=t.AD_Menu_ID AND t.AD_Language='")
					.append(Env.getAD_Language(Env.getCtx())).append("'");
			if (!m_editable)
			{
				boolean hasWhere = sqlNode.indexOf(" WHERE ") != -1;
				sqlNode.append(hasWhere ? " AND " : " WHERE ").append("m.IsActive='Y' ");
			}
			//	Do not show Beta
			if (!MClient.get(getCtx()).isUseBetaFunctions())
			{
				boolean hasWhere = sqlNode.indexOf(" WHERE ") != -1;
				sqlNode.append(hasWhere ? " AND " : " WHERE ");
				sqlNode.append("(m.AD_Window_ID IS NULL OR EXISTS (SELECT * FROM AD_Window w WHERE m.AD_Window_ID=w.AD_Window_ID AND w.IsBetaFunctionality='N'))")
					.append(" AND (m.AD_Process_ID IS NULL OR EXISTS (SELECT * FROM AD_Process p WHERE m.AD_Process_ID=p.AD_Process_ID AND p.IsBetaFunctionality='N'))")
					.append(" AND (m.AD_Form_ID IS NULL OR EXISTS (SELECT * FROM AD_Form f WHERE m.AD_Form_ID=f.AD_Form_ID AND f.IsBetaFunctionality='N'))");
			}
		}
		else
		{
			if (columnNameX == null)
				throw new IllegalArgumentException("MTree.prepareNodeDetail - Unknown TreeType=" + getTreeType());
			sqlNode.append("SELECT t.").append(columnNameX)
				.append("_ID,t.Name,t.Description,t.IsSummary,").append(color)
				.append(" FROM ").append(fromClause);
			if (!m_editable)
				sqlNode.append(" WHERE t.IsActive='Y'");
		}
		String sql = sqlNode.toString();
		if (!m_editable)	//	editable = menu/etc. window
			sql = MRole.getDefault(getCtx(), false).addAccessSQL(sql, 
				sourceTable, MRole.SQL_FULLYQUALIFIED, m_editable);
		log.debug("getNodeDetails - " + sql);
		m_nodeRowSet = DB.getRowSet (sql, true);
	}   //  getNodeDetails

	/**
	 *  Get Node Details.
	 *  As SQL contains security access, not all nodes will be found
	 *  @param  node_ID     Key of the record
	 *  @param  parent_ID   Parent ID of the record
	 *  @param  seqNo       Sort index
	 *  @param  onBar       Node also on Shortcut bar
	 *  @return Node
	 */
	private MTreeNode getNodeDetail (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		MTreeNode retValue = null;
		try
		{
			m_nodeRowSet.beforeFirst();
			while (m_nodeRowSet.next())
			{
				int node = m_nodeRowSet.getInt(1);				
				if (node_ID != node)	//	search for correct one
					continue;
				//	ID,Name,Description,IsSummary,Action/Color
				int index = 2;				
				String name = m_nodeRowSet.getString(index++); 
				String description = m_nodeRowSet.getString(index++);
				boolean isSummary = "Y".equals(m_nodeRowSet.getString(index++));
				String actionColor = m_nodeRowSet.getString(index++);
				//	Menu only
				if (getTreeType().equals(TREETYPE_Menu) && !isSummary)
				{
					int AD_Window_ID = m_nodeRowSet.getInt(index++);
					int AD_Process_ID = m_nodeRowSet.getInt(index++);
					int AD_Form_ID = m_nodeRowSet.getInt(index++);
					int AD_Workflow_ID = m_nodeRowSet.getInt(index++);
					int AD_Task_ID = m_nodeRowSet.getInt(index++);
					int AD_Workbench_ID = m_nodeRowSet.getInt(index++);
					//
					MRole role = MRole.getDefault(getCtx(), false);
					Boolean access = null;
					if (X_AD_Menu.ACTION_Window.equals(actionColor))
						access = role.getWindowAccess(AD_Window_ID);
					else if (X_AD_Menu.ACTION_Process.equals(actionColor) 
						|| X_AD_Menu.ACTION_Report.equals(actionColor))
						access = role.getProcessAccess(AD_Process_ID);
					else if (X_AD_Menu.ACTION_Form.equals(actionColor))
						access = role.getFormAccess(AD_Form_ID);
					else if (X_AD_Menu.ACTION_WorkFlow.equals(actionColor))
						access = role.getWorkflowAccess(AD_Workflow_ID);
					else if (X_AD_Menu.ACTION_Task.equals(actionColor))
						access = role.getTaskAccess(AD_Task_ID);
				//	else if (X_AD_Menu.ACTION_Workbench.equals(action))
				//		access = role.getWorkbenchAccess(AD_Window_ID);
				//	log.debug("getNodeDetail - " + name + " - " + actionColor + " - " + access);
					//
					if (access != null		//	rw or ro for Role 
						|| m_editable)		//	Menu Window can see all
					{
						retValue = new MTreeNode (node_ID, seqNo,
							name, description, parent_ID, isSummary,
							actionColor, onBar, null);	//	menu has no color
					}
				}
				else	//	always add
				{
					Color color = null;	//	action
					MPrintColor printColor = MPrintColor.get(getCtx(), actionColor);
					if (printColor != null)
						color = printColor.getColor(); 
					//
					retValue = new MTreeNode (node_ID, seqNo,
						name, description, parent_ID, isSummary,
						null, onBar, color);
				}
			}
		}
		catch (SQLException e)
		{
			log.error("getNodeDetails", e);
		}
		return retValue;
	}   //  getNodeDetails

	/*************************************************************************/

	/**
	 *  Trim tree of empty summary nodes
	 */
	private void trimTree()
	{
		boolean needsTrim = m_root != null;
		while (needsTrim)
		{
			needsTrim = false;
			Enumeration en = m_root.preorderEnumeration();
			while (m_root.getChildCount() > 0 && en.hasMoreElements())
			{
				MTreeNode nd = (MTreeNode)en.nextElement();
				if (nd.isSummary() && nd.getChildCount() == 0)
				{
					nd.removeFromParent();
					needsTrim = true;
				}
			}
		}
	}   //  trimTree

	/**
	 *  Diagnostics: Print tree
	 */
	private void dumpTree()
	{
		Enumeration en = m_root.preorderEnumeration();
		int count = 0;
		while (en.hasMoreElements())
		{
			StringBuffer sb = new StringBuffer();
			MTreeNode nd = (MTreeNode)en.nextElement();
			for (int i = 0; i < nd.getLevel(); i++)
				sb.append(" ");
			sb.append("ID=").append(nd.getNode_ID())
				.append(", SeqNo=").append(nd.getSeqNo())
				.append(" ").append(nd.getName());
			System.out.println(sb.toString());
			count++;
		}
		System.out.println("Count=" + count);
	}   //  diagPrintTree

	/**
	 *  Get Root node
	 *  @return root
	 */
	public MTreeNode getRoot()
	{
		return m_root;
	}   //  getRoot

	/**
	 * 	Is Menu Tree
	 *	@return true if menu
	 */
	public boolean isMenu()
	{
		return TREETYPE_Menu.equals(getTreeType());
	}	//	isMenu

	/**
	 * 	Is Product Tree
	 *	@return true if product
	 */
	public boolean isProduct()
	{
		return TREETYPE_Product.equals(getTreeType());
	}	//	isProduct
	
	/**
	 * 	Is Business Partner Tree
	 *	@return true if partner
	 */
	public boolean isBPartner()
	{
		return TREETYPE_BPartner.equals(getTreeType());
	}	//	isBPartner
	
	/**
	 *  String representation
	 *  @return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MTree[");
		sb.append("AD_Tree_ID=").append(getAD_Tree_ID())
			.append(", Name=").append(getName());
		sb.append("]");
		return sb.toString();
	}
}   //  MTree
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

import java.sql.*;
import java.util.*;
import javax.sql.*;

import org.compiere.util.*;

/**
 *  Builds Tree.
 *  Creates tree structure - maintained in VTreePanel
 *
 *  @author     Jorg Janke
 *  @version    $Id: MTree.java,v 1.6 2003/11/06 07:08:06 jjanke Exp $
 */
public class MTree extends X_AD_Tree
{
	/**
	 *  Construct Tree
	 *  @param AD_Tree_ID   The tree to build
	 *  @param editable     True, if tree can be modified
	 *  - includes inactive and empty summary nodes
	 * 	@param ctx context for security
	 */
	public MTree (Properties ctx, int AD_Tree_ID, boolean editable)
	{
		super(ctx, AD_Tree_ID);
		log.info("AD_Tree_ID=" + AD_Tree_ID + ", Editable=" + editable);
		m_editable = editable;

		int AD_User_ID = Env.getContextAsInt(ctx, "AD_User_ID");
		loadNodes(AD_User_ID);
	}   //  MTree

	/** Is Tree editable    	*/
	private boolean     		m_editable;
	/** Table Name          	*/
	private String      		m_nodeTableName = "AD_TreeNode";
	/** Root Node                   */
	private MTreeNode           m_root = null;
	/** Buffer while loading tree   */
	private ArrayList           m_buffer = new ArrayList();
	/** Prepared Statement for Node Details */
	private RowSet			   m_nodeRowSet;

	/*************************************************************************/

	/**
	 *  Get primary AD_Tree_ID for KeyColumn.
	 *  Called from GridController
	 *  @param keyColumnName key column name, eg. C_Project_ID
	 *  @return AD_Tree_ID
	 */
	public static int getAD_Tree_ID (String keyColumnName)
	{
		Log.trace(Log.l4_Data, "MTree.getAD_Tree_ID", keyColumnName);
		if (keyColumnName == null || keyColumnName.length() == 0)
			return 0;

		String TreeType = null;
		if (keyColumnName.equals("AD_Menu_ID"))
			TreeType = TREETYPE_Menu; 
		else if (keyColumnName.equals("C_ElementValue_ID"))
			TreeType = TREETYPE_ElementValueAccountEtc;
		else if (keyColumnName.equals("M_Product_ID"))
			TreeType = TREETYPE_Product;
		else if (keyColumnName.equals("C_BPartner_ID"))
			TreeType = TREETYPE_BusPartner;
		else if (keyColumnName.equals("AD_Org_ID"))
			TreeType = TREETYPE_Organization;
		else if (keyColumnName.equals("C_Project_ID"))
			TreeType = TREETYPE_Project;
		else if (keyColumnName.equals("M_ProductCategory_ID"))
			TreeType = TREETYPE_ProductCategory;
		else if (keyColumnName.equals("M_BOM_ID"))
			TreeType = TREETYPE_BoM;
		else if (keyColumnName.equals("C_SalesRegion_ID"))
			TreeType = TREETYPE_SalesRegion;
		else if (keyColumnName.equals("C_Campaign_ID"))
			TreeType = TREETYPE_Campaign;
		else if (keyColumnName.equals("C_Activity_ID"))
			TreeType = TREETYPE_Activity;
		else
		{
			Log.error("MTree.getAD_Tree_ID - Could not map " + keyColumnName);
			return 0;
		}

		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getAD_Tree_ID", e);
		}

		return AD_Tree_ID;
	}   //  getAD_Tree_ID


	/**
	 * 	Get Primary Tree of Tree Type
	 * 	@param TreeType see TREETYPE_
	 * 	@param ctx context for Security
	 * 	@return MTree
	 */
	public static MTree getTree (Properties ctx, String TreeType)
	{
		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getTree", e);
		}
		//	Not found
		if (AD_Tree_ID == 0)
		{
			Log.trace(Log.l4_Data, "MTree.getTree - No AD_Tree_ID for TreeType=" + TreeType + ", AD_Client_ID=" + AD_Client_ID);
			return null;
		}
		//
		MTree tree = new MTree (ctx, AD_Tree_ID, false);
		return tree;
	}	//	getTree


	/*************************************************************************

	/**
	 *  Load Nodes and Bar
	 * 	@param AD_User_ID user
	 */
	private void loadNodes (int AD_User_ID)
	{
		//  TableName: AD_TreeNode
		if (getTreeType().equals(TREETYPE_Menu))
			;   //  m_tableName += "MM";
		else if  (getTreeType().equals(TREETYPE_BusPartner))
			m_nodeTableName += "BP";
		else if  (getTreeType().equals(TREETYPE_Product))
			m_nodeTableName += "PR";

		//  SQL for TreeNodes
		StringBuffer cmd = new StringBuffer("SELECT "
			+ "tn.Node_ID,tn.Parent_ID,tn.SeqNo,tb.IsActive "
			+ "FROM ").append(m_nodeTableName).append(" tn"
			+ " LEFT OUTER JOIN AD_TreeBar tb ON (tn.AD_Tree_ID=tb.AD_Tree_ID"
			+ " AND tn.Node_ID=tb.Node_ID AND tb.AD_User_ID=?) "	//	#1
			+ "WHERE tn.AD_Tree_ID=?");								//	#2
		if (!m_editable)
			cmd.append(" AND tn.IsActive='Y'");
		cmd.append(" ORDER BY COALESCE(tn.Parent_ID, -1), tn.SeqNo");
		Log.trace(Log.l6_Database, "MTree.loadNodes", cmd.toString());

		//  The Node Loop
		try
		{
			// load Node details - addToTree -> getNodeDetail
			getNodeDetails(); 
			//
			PreparedStatement pstmt = DB.prepareStatement(cmd.toString());
			pstmt.setInt(1, AD_User_ID);
			pstmt.setInt(2, getAD_Tree_ID());
			//	Get Tree & Bar
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int node_ID = rs.getInt(1);
				int parent_ID = rs.getInt(2);
				int seqNo = rs.getInt(3);
				boolean onBar = (rs.getString(4) != null);
				//
				if (node_ID == 0 && parent_ID == 0)
					m_root = new MTreeNode (node_ID, 0, getName(), getDescription(), 0, true, null, onBar);
				else
					addToTree (node_ID, parent_ID, seqNo, onBar);	//	calls getNodeDetail
			}
			rs.close();
			pstmt.close();
			//
			m_nodeRowSet.close();
			m_nodeRowSet = null;
		}
		catch (SQLException e)
		{
			Log.error("MTree.loadNodes", e);
			m_nodeRowSet = null;
		}
		
		//  Done with loading - add remainder from buffer
		if (m_buffer.size() != 0)
		{
			Log.trace(Log.l6_Database, "MTree.loadNodes - clearing buffer");
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				MTreeNode parent = m_root.findNode(node.getParent_ID());
				if (parent != null && parent.getAllowsChildren())
				{
					parent.add(node);
					checkBuffer(node);
					m_buffer.remove(i);
					i = -1;
				}
			}
		}

		//	Nodes w/o parent
		if (m_buffer.size() != 0)
		{
			Log.error ("MTree.loadNodes - Nodes w/o parent - adding to root - " + m_buffer);
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				m_root.add(node);
				checkBuffer(node);
				m_buffer.remove(i);
				i = -1;
			}
			if (m_buffer.size() != 0)
				Log.error ("MTree.loadNodes - still nodes in Buffer - " + m_buffer);
		}	//	nodes w/o parents

		//  clean up
		if (!m_editable && m_root.getChildCount() > 0)
			trimTree();
//		diagPrintTree();
	}   //  loadNodes

	/**
	 *  Add Node to Tree.
	 *  If not found add to buffer
	 *  @param node_ID Node_ID
	 *  @param parent_ID Parent_ID
	 *  @param seqNo SeqNo
	 *  @param onBar on bar
	 */
	private void addToTree (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		//  Create new Node
		MTreeNode child = getNodeDetail (node_ID, parent_ID, seqNo, onBar);
		if (child == null)
			return;

		//  Add to Tree
		MTreeNode parent = null;
		if (m_root != null)
			parent = m_root.findNode (parent_ID);
		//  Parent found
		if (parent != null && parent.getAllowsChildren())
		{
			parent.add(child);
			//  see if we can add nodes from buffer
			if (m_buffer.size() > 0)
				checkBuffer(child);
		}
		else
			m_buffer.add(child);
	}   //  addToTree

	/**
	 *  Check the buffer for nodes which have newNode as Parents
	 *  @param newNode new node
	 */
	private void checkBuffer (MTreeNode newNode)
	{
		for (int i = 0; i < m_buffer.size(); i++)
		{
			MTreeNode node = (MTreeNode)m_buffer.get(i);
			if (node.getParent_ID() == newNode.getID())
			{
				newNode.add(node);
				m_buffer.remove(i);
				i--;
			}
		}
	}   //  checkBuffer

	/*************************************************************************/

	/**
	 *  Get Node Detail.
	 * 	Loads data into RowSet m_nodeRowSet
	 *  Columns:
	 * 	- ID
	 *  - Name
	 *  - Description
	 *  - IsSummary
	 *  - ImageIndicator
	 * 	- additional for Menu
	 *  Parameter:
	 *  - Node_ID
	 *  The SQL contains security/access control
	 *  @return SQL to get Node Detail Info
	 */
	private void getNodeDetails ()
	{
		//  SQL for Node Info
		StringBuffer sqlNode = new StringBuffer();
		String sourceTable = null;
		if (getTreeType().equals(TREETYPE_Menu))
		{
			boolean base = Env.isBaseLanguage(Env.getCtx(), "AD_Menu");
			sourceTable = "m";
			if (base)
				sqlNode.append("SELECT m.AD_Menu_ID, m.Name,m.Description,m.IsSummary,m.Action, "
					+ "m.AD_Window_ID, m.AD_Process_ID, m.AD_Form_ID, m.AD_Workflow_ID, m.AD_Task_ID, m.AD_Workbench_ID "
					+ "FROM AD_Menu m");
			else
				sqlNode.append("SELECT m_AD_Menu_ID,  t.Name,t.Description,m.IsSummary,m.Action, "
					+ "m.AD_Window_ID, m.AD_Process_ID, m.AD_Form_ID, m.AD_Workflow_ID, m.AD_Task_ID, m.AD_Workbench_ID "
					+ "FROM AD_Menu m, AD_Menu_Trl t");
			if (!base)
				sqlNode.append(" WHERE m.AD_Menu_ID=t.AD_Menu_ID AND t.AD_Language='")
					.append(Env.getAD_Language(Env.getCtx())).append("'");
			if (!m_editable)
			{
				boolean hasWhere = sqlNode.indexOf(" WHERE ") != -1;
				sqlNode.append(hasWhere ? " AND " : " WHERE ").append(" m.IsActive='Y' ");
			}
		}
		else
		{
			if (getTreeType().equals(TREETYPE_Organization))
				sourceTable = "AD_Org";
			else if (getTreeType().equals(TREETYPE_Product))
				sourceTable = "M_Product";
			else if (getTreeType().equals(TREETYPE_ProductCategory))
				sourceTable = "M_Product_Category";
			else if (getTreeType().equals(TREETYPE_BoM))
				sourceTable = "M_BOM";
			else if (getTreeType().equals(TREETYPE_ElementValueAccountEtc))
				sourceTable = "C_ElementValue";
			else if (getTreeType().equals(TREETYPE_BusPartner))
				sourceTable = "C_BPartner";
			else if (getTreeType().equals(TREETYPE_Campaign))
				sourceTable = "C_Campaign";
			else if (getTreeType().equals(TREETYPE_Project))
				sourceTable = "C_Project";
			else if (getTreeType().equals(TREETYPE_Activity))
				sourceTable = "C_Activity";
			else if (getTreeType().equals(TREETYPE_SalesRegion))
				sourceTable = "C_SalesRegion";
			if (sourceTable == null)
			{
				throw new IllegalArgumentException("MTree.prepareNodeDetail - Unknown TreeType=" + getTreeType());
			}
			sqlNode.append("SELECT ").append(sourceTable).append("_ID,Name,Description,IsSummary,NULL FROM ")
				.append(sourceTable);
			if (!m_editable)
				sqlNode.append(" WHERE IsActive='Y'");
		}
		String sql = MRole.getDefault(getCtx(), false).addAccessSQL(sqlNode.toString(), 
			sourceTable, MRole.SQL_FULLYQUALIFIED, m_editable);
		log.debug("getNodeDetails - " + sql);
		m_nodeRowSet = DB.getRowSet (sql);
	}   //  getNodeDetails

	/**
	 *  Get Node Details.
	 *  As SQL contains security access, not all nodes will be found
	 *  @param  node_ID     Key of the record
	 *  @param  parent_ID   Parent ID of the record
	 *  @param  seqNo       Sort index
	 *  @param  onBar       Node also on Shortcut bar
	 *  @return Node
	 */
	private MTreeNode getNodeDetail (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		MTreeNode retValue = null;
		try
		{
			m_nodeRowSet.beforeFirst();
			while (m_nodeRowSet.next())
			{
				int node = m_nodeRowSet.getInt(1);				
				if (node_ID != node)	//	search for correct one
					continue;
				//
				int index = 2;				
				String name = m_nodeRowSet.getString(index++); 
				String description = m_nodeRowSet.getString(index++);
				boolean isSummary = "Y".equals(m_nodeRowSet.getString(index++));
				String action = m_nodeRowSet.getString(index++);
				//
				if (getTreeType().equals(TREETYPE_Menu) && !isSummary)
				{
					int AD_Window_ID = m_nodeRowSet.getInt(index++);
					int AD_Process_ID = m_nodeRowSet.getInt(index++);
					int AD_Form_ID = m_nodeRowSet.getInt(index++);
					int AD_Workflow_ID = m_nodeRowSet.getInt(index++);
					int AD_Task_ID = m_nodeRowSet.getInt(index++);
					int AD_Workbench_ID = m_nodeRowSet.getInt(index++);
					//
					MRole role = MRole.getDefault(getCtx(), false);
					Boolean access = null;
					if (X_AD_Menu.ACTION_Window.equals(action))
						access = role.getWindowAccess(AD_Window_ID);
					else if (X_AD_Menu.ACTION_Process.equals(action) || X_AD_Menu.ACTION_Report.equals(action))
						access = role.getProcessAccess(AD_Process_ID);
					else if (X_AD_Menu.ACTION_Form.equals(action))
						access = role.getFormAccess(AD_Form_ID);
					else if (X_AD_Menu.ACTION_WorkFlow.equals(action))
						access = role.getWorkflowAccess(AD_Workflow_ID);
					else if (X_AD_Menu.ACTION_Task.equals(action))
						access = role.getTaskAccess(AD_Task_ID);
				//	else if (X_AD_Menu.ACTION_Workbench.equals(action))
				//		access = role.getWorkbenchAccess(AD_Window_ID);
				//	log.debug("getNodeDetail - " + name + " - " + action + " - " + access);
					//
					if (access != null)		//	rw or ro
					{
						retValue = new MTreeNode (node_ID, seqNo,
							name, description, parent_ID, isSummary,
							action, onBar);
					}
				}
				else
				{	//	always add
					retValue = new MTreeNode (node_ID, seqNo,
						name, description, parent_ID, isSummary,
						action, onBar);
				}
			}
		}
		catch (SQLException e)
		{
			log.error("getNodeDetails", e);
		}
		return retValue;
	}   //  getNodeDetails

	/*************************************************************************/

	/**
	 *  Trim tree of empty summary nodes
	 */
	private void trimTree()
	{
		boolean needsTrim = m_root != null;
		while (needsTrim)
		{
			needsTrim = false;
			Enumeration en = m_root.preorderEnumeration();
			while (en.hasMoreElements())
			{
				MTreeNode nd = (MTreeNode)en.nextElement();
				if (nd.isSummary() && nd.getChildCount() == 0)
				{
					nd.removeFromParent();
					needsTrim = true;
				}
			}
		}
	}   //  trimTree

	/**
	 *  Diagnostics: Print tree
	 */
	private void diagPrintTree()
	{
		Enumeration en = m_root.preorderEnumeration();
		int count = 0;
		while (en.hasMoreElements())
		{
			StringBuffer sb = new StringBuffer();
			MTreeNode nd = (MTreeNode)en.nextElement();
			for (int i = 0; i < nd.getLevel(); i++)
				sb.append(" ");
			sb.append("ID=").append(nd.getID())
				.append(", SeqNo=").append(nd.getSeqNo())
				.append(" ").append(nd.getName());
			System.out.println(sb.toString());
			count++;
		}
		System.out.println("Count=" + count);
	}   //  diagPrintTree

	/**
	 *  Get Root node
	 *  @return root
	 */
	public MTreeNode getRoot()
	{
		return m_root;
	}   //  getRoot

	/**
	 *  Get Node Table Name
	 *  @return Table Name
	 */
	public String getNodeTableName()
	{
		return m_nodeTableName;
	}   //  getTableName

	/**
	 *  String representation
	 *  @return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MTree[");
		sb.append("AD_Tree_ID=").append(getAD_Tree_ID())
			.append(", Name=").append(getName());
		sb.append("]");
		return sb.toString();
	}
}   //  MTree
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

import java.sql.*;
import java.util.*;

import org.compiere.util.*;

/**
 *  Builds Tree.
 *  Creates tree structure - maintained in VTreePanel
 *
 *  @author     Jorg Janke
 *  @version    $Id: MTree.java,v 1.2 2003/02/19 06:48:23 jjanke Exp $
 */
public class MTree
{
	/**
	 *  Construct Tree
	 *  @param AD_Tree_ID   The tree to build
	 *  @param editable     True, if tree can be modified
	 *  - includes inactive and empty summary nodes
	 */
	public MTree (int AD_Tree_ID, boolean editable)
	{
		this (AD_Tree_ID, 0, 0, editable);
	}	//	MTree

	/**
	 *  Construct Tree
	 *  @param AD_Tree_ID   The tree to build
	 *  @param AD_User_ID   Tree Bar Access control - optional
	 *  @param AD_Role_ID   Menu Access control
	 *  @param editable     True, if tree can be modified
	 *  - includes inactive and empty summary nodes
	 */
	public MTree (int AD_Tree_ID,
		int AD_User_ID, int AD_Role_ID, boolean editable)
	{
		Log.trace(Log.l4_Data, "MTree - AD_Tree_ID=" + AD_Tree_ID,
			"AD_Role_ID=" + AD_Role_ID + ", AD_User_ID=" + AD_User_ID
			+ ", Editable=" + editable);

		m_AD_Tree_ID = AD_Tree_ID;
		m_AD_User_ID = AD_User_ID;
		m_AD_Role_ID = AD_Role_ID;
		m_editable = editable;
		loadTree();
	}   //  MTree

	/** Tree ID             */
	private int         m_AD_Tree_ID;
	/** Tree for User       */
	private int         m_AD_User_ID;
	/** Role Access Control */
	private int         m_AD_Role_ID;
	/** Is Tree editable    */
	private boolean     m_editable;

	/** Tree Name           */
	private String      m_name;
	/** Tree Description    */
	private String	    m_description;
	/** Tree Type           */
	private String      m_treeType;

	/** Table Name          */
	private String      m_tableName = "AD_TreeNode";

	/** Root Node                   */
	private MTreeNode           m_root = null;
	/** Buffer while loading tree   */
	private ArrayList           m_buffer = new ArrayList();
	/** Prepared Statement for Node Details */
	private PreparedStatement   m_pstmtDetail;

	/*************************************************************************/

	/** Tree Type Menu MM   */
	public static final String	TREETYPE_Menu =         "MM";
	/** Tree Type Account ElementValue EV   */
	public static final String	TREETYPE_ElementValue = "EV";
	/** Tree Type Product PR   */
	public static final String	TREETYPE_Product =      "PR";
	/** Tree Type BusinessPartner BP   */
	public static final String	TREETYPE_BPartner =     "BP";
	/** Tree Type Organization OO   */
	public static final String	TREETYPE_Org =          "OO";
	/** Tree Type Project PJ   */
	public static final String	TREETYPE_Project =      "PJ";
	/** Tree Type ProductCategory PC   */
	public static final String	TREETYPE_ProductCategory = "PC";
	/** Tree Type BOM BB   */
	public static final String	TREETYPE_BOM =          "BB";
	/** Tree Type SalesRegion SR   */
	public static final String	TREETYPE_SalesRegion =  "SR";
	/** Tree Type Camoaign MC   */
	public static final String	TREETYPE_Campaign =     "MC";
	/** Tree Type Activity AY   */
	public static final String	TREETYPE_Activity =     "AY";

	/*************************************************************************/

	/**
	 *  Get primary AD_Tree_ID for KeyColumn.
	 *  Called from GridController
	 *  @param keyColumnName key column name, eg. C_Project_ID
	 *  @return AD_Tree_ID
	 */
	public static int getAD_Tree_ID (String keyColumnName)
	{
		Log.trace(Log.l4_Data, "MTree.getAD_Tree_ID", keyColumnName);
		if (keyColumnName == null || keyColumnName.length() == 0)
			return 0;

		String TreeType = null;
		if (keyColumnName.equals("AD_Menu_ID"))
			TreeType = MTree.TREETYPE_Menu;
		else if (keyColumnName.equals("C_ElementValue_ID"))
			TreeType = MTree.TREETYPE_ElementValue;
		else if (keyColumnName.equals("M_Product_ID"))
			TreeType = MTree.TREETYPE_Product;
		else if (keyColumnName.equals("C_BPartner_ID"))
			TreeType = MTree.TREETYPE_BPartner;
		else if (keyColumnName.equals("AD_Org_ID"))
			TreeType = MTree.TREETYPE_Org;
		else if (keyColumnName.equals("C_Project_ID"))
			TreeType = MTree.TREETYPE_Project;
		else if (keyColumnName.equals("M_ProductCategory_ID"))
			TreeType = MTree.TREETYPE_ProductCategory;
		else if (keyColumnName.equals("M_BOM_ID"))
			TreeType = MTree.TREETYPE_BOM;
		else if (keyColumnName.equals("C_SalesRegion_ID"))
			TreeType = MTree.TREETYPE_SalesRegion;
		else if (keyColumnName.equals("C_Campaign_ID"))
			TreeType = MTree.TREETYPE_Campaign;
		else if (keyColumnName.equals("C_Activity_ID"))
			TreeType = MTree.TREETYPE_Activity;
		else
		{
			Log.error("MTree.getAD_Tree_ID - Could not map " + keyColumnName);
			return 0;
		}

		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getAD_Tree_ID", e);
		}

		return AD_Tree_ID;
	}   //  getAD_Tree_ID


	/**
	 * 	Get Primary Tree of Tree Type
	 * 	@param TreeType see TREETYPE_
	 * 	@return MTree
	 */
	public static MTree getTree (String TreeType)
	{
		int AD_Tree_ID = 0;
		int AD_Client_ID = Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID");
		String sql = "SELECT AD_Tree_ID,Name FROM AD_Tree "
			+ "WHERE AD_Client_ID=? AND TreeType=? AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Client_ID);
			pstmt.setString(2, TreeType);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				AD_Tree_ID = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getTree", e);
		}
		//	Not found
		if (AD_Tree_ID == 0)
		{
			Log.trace(Log.l4_Data, "MTree.getTree - No AD_Tree_ID for TreeType=" + TreeType + ", AD_Client_ID=" + AD_Client_ID);
			return null;
		}
		//
		MTree tree = new MTree (AD_Tree_ID, false);
		return tree;
	}	//	getTree


	/*************************************************************************/

	/**
	 *  Load Tree
	 */
	private void loadTree()
	{
		//  Get Tree info
		String sql = "SELECT Name, Description, TreeType "
			+ "FROM AD_Tree "
			+ "WHERE AD_Tree_ID=?"
			+ " AND IsActive='Y'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, m_AD_Tree_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				m_name = rs.getString(1);
				m_description = rs.getString(2);
				if (m_description == null)
					m_description = "";
				m_treeType = rs.getString(3);
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.loadTree", e);
			return;
		}
		loadNodes();
	}   //  loadTree

	/**
	 *  Load Nodes
	 */
	private void loadNodes()
	{
		//  TableName: AD_TreeNode
		if (m_treeType.equals(TREETYPE_Menu))
			;   //  m_tableName += "MM";
		else if  (m_treeType.equals(TREETYPE_BPartner))
			m_tableName += "BP";
		else if  (m_treeType.equals(TREETYPE_Product))
			m_tableName += "PR";

		//  SQL for TreeNodes
		StringBuffer cmd = new StringBuffer("SELECT "
			+ "tn.Node_ID,tn.Parent_ID,tn.SeqNo,tb.IsActive "
			+ "FROM ").append(m_tableName).append(" tn"
			+ " LEFT OUTER JOIN AD_TreeBar tb ON (tn.AD_Tree_ID=tb.AD_Tree_ID"
			+ " AND tn.Node_ID=tb.Node_ID AND tb.AD_User_ID=?) "	//	#1
			+ "WHERE tn.AD_Tree_ID=?");								//	#2
		if (!m_editable)
			cmd.append(" AND tn.IsActive='Y'");
		cmd.append(" ORDER BY COALESCE(tn.Parent_ID, -1), tn.SeqNo");
		Log.trace(Log.l6_Database, "MTree.loadNodes", cmd.toString());

		//  The Node Loop
		try
		{
			m_pstmtDetail = DB.prepareStatement(prepareNodeDetail());
			//
			PreparedStatement pstmt = DB.prepareStatement(cmd.toString());
			pstmt.setInt(1, m_AD_User_ID);
			pstmt.setInt(2, m_AD_Tree_ID);
			//
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				int node_ID = rs.getInt(1);
				int parent_ID = rs.getInt(2);
				int seqNo = rs.getInt(3);
				boolean onBar = (rs.getString(4) != null);
				//
				if (node_ID == 0 && parent_ID == 0)
					m_root = new MTreeNode (node_ID, 0, m_name, m_description, 0, true, null, onBar);
				else
					addToTree (node_ID, parent_ID, seqNo, onBar);
			}
			rs.close();
			pstmt.close();
			//
			m_pstmtDetail.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.loadNodes", e);
		}
		//  Done with loading - add remainder from buffer
		if (m_buffer.size() != 0)
		{
			Log.trace(Log.l6_Database, "MTree.loadNodes - clearing buffer");
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				MTreeNode parent = m_root.findNode(node.getParent_ID());
				if (parent != null && parent.getAllowsChildren())
				{
					parent.add(node);
					checkBuffer(node);
					m_buffer.remove(i);
					i = -1;
				}
			}
		}

		//	Nodes w/o parent
		if (m_buffer.size() != 0)
		{
			Log.error ("MTree.loadNodes - Nodes w/o parent - adding to root - " + m_buffer);
			for (int i = 0; i < m_buffer.size(); i++)
			{
				MTreeNode node = (MTreeNode)m_buffer.get(i);
				m_root.add(node);
				checkBuffer(node);
				m_buffer.remove(i);
				i = -1;
			}
			if (m_buffer.size() != 0)
				Log.error ("MTree.loadNodes - still nodes in Buffer - " + m_buffer);
		}	//	nodes w/o parents

		//  clean up
		if (!m_editable && m_root.getChildCount() > 0)
			trimTree();
//		diagPrintTree();
	}   //  loadNodes

	/**
	 *  Add Node to Tree.
	 *  If not found add to buffer
	 *  @param node_ID Node_ID
	 *  @param parent_ID Parent_ID
	 *  @param seqNo SeqNo
	 *  @param onBar on bar
	 */
	private void addToTree (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		//  Create new Node
		MTreeNode child = getNodeDetail (node_ID, parent_ID, seqNo, onBar);
		if (child == null)
			return;

		//  Add to Tree
		MTreeNode parent = null;
		if (m_root != null)
			parent = m_root.findNode (parent_ID);
		//  Parent found
		if (parent != null && parent.getAllowsChildren())
		{
			parent.add(child);
			//  see if we can add nodes from buffer
			if (m_buffer.size() > 0)
				checkBuffer(child);
		}
		else
			m_buffer.add(child);
	}   //  addToTree

	/**
	 *  Check the buffer for nodes which have newNode as Parents
	 *  @param newNode new node
	 */
	private void checkBuffer (MTreeNode newNode)
	{
		for (int i = 0; i < m_buffer.size(); i++)
		{
			MTreeNode node = (MTreeNode)m_buffer.get(i);
			if (node.getParent_ID() == newNode.getID())
			{
				newNode.add(node);
				m_buffer.remove(i);
				i--;
			}
		}
	}   //  checkBuffer

	/*************************************************************************/

	/**
	 *  Prepare Node Detail.
	 *  Columns:
	 *  - Name
	 *  - Description
	 *  - IsSummary
	 *  - ImageIndicator
	 *  Parameter:
	 *  - Node_ID
	 *  The SQL contains security/access control
	 *  @return SQL to get Node Detail Info
	 */
	private String prepareNodeDetail ()
	{
		//  SQL for Node Info
		StringBuffer cmdNode = new StringBuffer();
		if (m_treeType.equals(MTree.TREETYPE_Menu))
		{
			boolean base = Env.isBaseLanguage(Env.getCtx(), "AD_Menu");
			if (base)
				cmdNode.append("SELECT m.Name,m.Description,m.IsSummary,m.Action "
					+ "FROM AD_Menu m");
			else
				cmdNode.append("SELECT t.Name,t.Description,m.IsSummary,m.Action "
					+ "FROM AD_Menu m, AD_Menu_Trl t");
			if (!m_editable)
				cmdNode.append(", (SELECT ").append(m_AD_Role_ID).append(" AS XRole FROM DUAL) x");
			cmdNode.append(" WHERE m.AD_Menu_ID=?");            //  #1
			if (!base)
				cmdNode.append(" AND m.AD_Menu_ID=t.AD_Menu_ID AND t.AD_Language='")
					.append(Env.getAD_Language(Env.getCtx())).append("'");
			if (!m_editable)
				cmdNode.append(" AND m.IsActive='Y' "
					+ "AND (m.IsSummary='Y' OR m.Action='B'"
					+ " OR EXISTS (SELECT * FROM AD_Window_Access wa WHERE wa.AD_Window_ID=m.AD_Window_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Process_Access wa WHERE wa.AD_Process_ID=m.AD_Process_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Form_Access wa WHERE wa.AD_Form_ID=m.AD_Form_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Task_Access wa WHERE wa.AD_Task_ID=m.AD_Task_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ " OR EXISTS (SELECT * FROM AD_Workflow_Access wa WHERE wa.AD_Workflow_ID=m.AD_Workflow_ID AND wa.AD_Role_ID=XRole AND wa.IsActive='Y')"
					+ ")");
		}
		else
		{
			cmdNode.append("SELECT Name,Description,IsSummary,NULL FROM ");
			//
			String sourceTable = null;
			if (m_treeType.equals(MTree.TREETYPE_Org))
				sourceTable = "AD_Org";
			else if (m_treeType.equals(MTree.TREETYPE_Product))
				sourceTable = "M_Product";
			else if (m_treeType.equals(MTree.TREETYPE_ProductCategory))
				sourceTable = "M_Product_Category";
			else if (m_treeType.equals(MTree.TREETYPE_BOM))
				sourceTable = "M_BOM";
			else if (m_treeType.equals(MTree.TREETYPE_ElementValue))
				sourceTable = "C_ElementValue";
			else if (m_treeType.equals(MTree.TREETYPE_BPartner))
				sourceTable = "C_BPartner";
			else if (m_treeType.equals(MTree.TREETYPE_Campaign))
				sourceTable = "C_Campaign";
			else if (m_treeType.equals(MTree.TREETYPE_Project))
				sourceTable = "C_Project";
			else if (m_treeType.equals(MTree.TREETYPE_Activity))
				sourceTable = "C_Activity";
			else if (m_treeType.equals(MTree.TREETYPE_SalesRegion))
				sourceTable = "C_SalesRegion";
			if (sourceTable == null)
			{
				throw new IllegalArgumentException("MTree.prepareNodeDetail - Unknown TreeType=" + m_treeType);
			}
			cmdNode.append(sourceTable).append(" WHERE ")
				.append(sourceTable).append("_ID=?");               // #1
			if (!m_editable)
				cmdNode.append(" AND IsActive='Y'");
		}
		Log.trace(Log.l6_Database, "MTree.prepareNodeDetail", cmdNode.toString());
		return cmdNode.toString();
	}   //  prepareNodeDetail

	/**
	 *  Get Node Details.
	 *  As SQL contains security access, not all nodes will be found
	 *  @param  node_ID     Key of the record
	 *  @param  parent_ID   Parent ID of the record
	 *  @param  seqNo       Sort index
	 *  @param  onBar       Node also on Shortcut bar
	 *  @return Node
	 */
	private MTreeNode getNodeDetail (int node_ID, int parent_ID, int seqNo, boolean onBar)
	{
		MTreeNode retValue = null;
		try
		{
			m_pstmtDetail.setInt(1, node_ID);
			ResultSet rs = m_pstmtDetail.executeQuery();
			if (rs.next())
			{
				retValue = new MTreeNode (node_ID, seqNo,
					rs.getString(1),                //  name
					rs.getString(2),                //  description
					parent_ID,
					"Y".equals(rs.getString(3)),    //  IsSummary
					rs.getString(4),                //  ImageIndicator
					onBar);
			}
			rs.close();
		}
		catch (SQLException e)
		{
			Log.error("MTree.getNodeDetail", e);
		}
		return retValue;
	}   //  getNodeDetails

	/*************************************************************************/

	/**
	 *  Trim tree of empty summary nodes
	 */
	private void trimTree()
	{
		boolean needsTrim = m_root != null;
		while (needsTrim)
		{
			needsTrim = false;
			Enumeration en = m_root.preorderEnumeration();
			while (en.hasMoreElements())
			{
				MTreeNode nd = (MTreeNode)en.nextElement();
				if (nd.isSummary() && nd.getChildCount() == 0)
				{
					nd.removeFromParent();
					needsTrim = true;
				}
			}
		}
	}   //  trimTree

	/**
	 *  Diagnostics: Print tree
	 */
	private void diagPrintTree()
	{
		Enumeration en = m_root.preorderEnumeration();
		int count = 0;
		while (en.hasMoreElements())
		{
			StringBuffer sb = new StringBuffer();
			MTreeNode nd = (MTreeNode)en.nextElement();
			for (int i = 0; i < nd.getLevel(); i++)
				sb.append(" ");
			sb.append("ID=").append(nd.getID())
				.append(", SeqNo=").append(nd.getSeqNo())
				.append(" ").append(nd.getName());
			System.out.println(sb.toString());
			count++;
		}
		System.out.println("Count=" + count);
	}   //  diagPrintTree

	/**
	 *  Get Root node
	 *  @return root
	 */
	public MTreeNode getRoot()
	{
		return m_root;
	}   //  getRoot

	/**
	 *  Get Table Name
	 *  @return Table Name
	 */
	public String getTableName()
	{
		return m_tableName;
	}   //  getTableName

	/**
	 *  String representation
	 *  @return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MTree[");
		sb.append("AD_Tree_ID=").append(m_AD_Tree_ID)
			.append(", Name=").append(m_name);
		sb.append("]");
		return sb.toString();
	}
}   //  MTree
