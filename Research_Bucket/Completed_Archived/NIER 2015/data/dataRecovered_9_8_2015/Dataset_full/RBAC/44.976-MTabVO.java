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
 *  Model Tab Value Object
 *
 *  @author Jorg Janke
 *  @version  $Id: MTabVO.java,v 1.13 2004/05/18 05:07:19 jjanke Exp $
 */
public class MTabVO implements Serializable
{
	/**
	 *  Private constructor - must use Factory
	 */
	private MTabVO()
	{
	}   //  MTabVO

	/** Context - replicated    */
	public  Properties      ctx;
	/** Window No - replicated  */
	public  int				WindowNo;
	/** AD Window - replicated  */
	public  int             AD_Window_ID;

	/** Tab No (not AD_Tab_ID)  */
	public  int				TabNo;

	//  Database Fields

	public	int		    AD_Tab_ID;
	public	String	    Name = "";
	public	String	    Description = "";
	public	String	    Help = "";
	public	boolean	    IsSingleRow = false;
	public  boolean     IsReadOnly = false;
	public  boolean	    HasTree = false;
	public  int		    AD_Table_ID;
	/** Primary Parent Column   */
	public  int		    AD_Column_ID = 0;
	public  String	    TableName;
	public  boolean     IsView = false;
	public  String	    AccessLevel;
	public  boolean	    IsSecurityEnabled = false;
	public  boolean	    IsDeleteable = false;
	public  boolean     IsHighVolume = false;
	public	int		    AD_Process_ID = 0;
	public  String	    CommitWarning;
	public  String	    WhereClause;
	public  String      OrderByClause;
	public  int         TabLevel = 0;
	public int          AD_Image_ID = 0;
	public int          Included_Tab_ID = 0;
	public String		ReplicationType = "L";

	//
	public boolean		IsSortTab = false;
	public int			AD_ColumnSortOrder_ID = 0;
	public int			AD_ColumnSortYesNo_ID = 0;

	//  Derived
	public  boolean     onlyCurrentRows = true;
	public int			onlyCurrentDays = 1;

	/** Fields contain MFieldVO entities    */
	public ArrayList    Fields = null;

	/**
	 *  Set Context including contained elements
	 *  @param newCtx new context
	 */
	public void setCtx (Properties newCtx)
	{
		ctx = newCtx;
		for (int i = 0; i < Fields.size() ; i++)
		{
			MFieldVO field = (MFieldVO)Fields.get(i);
			field.setCtx(newCtx);
		}
	}   //  setCtx

	/**
	 *  Return the SQL statement used for the MTabVO.create
	 *  @param ctx context
	 *  @return SQL SELECT String
	 */
	protected static String getSQL (Properties ctx)
	{
		//  View only returns IsActive='Y'
		String sql = "SELECT * FROM AD_Tab_v WHERE AD_Window_ID=?"
			+ " ORDER BY SeqNo";
		if (!Env.isBaseLanguage(ctx, "AD_Window"))
			sql = "SELECT * FROM AD_Tab_vt WHERE AD_Window_ID=?"
				+ " AND AD_Language='" + Env.getAD_Language(ctx) + "'"
				+ " ORDER BY SeqNo";
		return sql;
	}   //  getSQL

	
	/**************************************************************************
	 *	Create MTabVO
	 *
	 *  @param wVO value object
	 *  @param TabNo tab no
	 *	@param rs ResultSet from AD_Tab_v
	 *	@param isRO true if window is r/o
	 *  @param onlyCurrentRows if true query is limited to not processed records
	 *  @return TabVO
	 */
	public static MTabVO create (MWindowVO wVO, int TabNo, ResultSet rs, boolean isRO, boolean onlyCurrentRows)
	{
		Log.trace(Log.l3_Util, "MTabVO.create #" + TabNo);

		MTabVO vo = new MTabVO ();
		vo.ctx = wVO.ctx;
		vo.WindowNo = wVO.WindowNo;
		vo.AD_Window_ID = wVO.AD_Window_ID;
		vo.TabNo = TabNo;
		//
		if (!loadTabDetails(vo, rs))
			return null;

		if (isRO)
		{
			Log.trace(Log.l6_Database, "MTabVO.create", "Tab is ReadOnly");
			vo.IsReadOnly = true;
		}
		vo.onlyCurrentRows = onlyCurrentRows;

		//  Create Fields
		if (vo.IsSortTab)
		{
			vo.Fields = new ArrayList();	//	dummy
		}
		else
		{
			createFields (vo);
			if (vo.Fields == null || vo.Fields.size() == 0)
			{
				Log.error("MTabVO.create - No Fields");
				return null;
			}
		}
		return vo;
	}	//	create

	/**
	 * 	Load Tab Details from rs into vo
	 * 	@param vo Tab value object
	 *	@param rs ResultSet from AD_Tab_v/t
	 * 	@return true if read ok
	 */
	private static boolean loadTabDetails (MTabVO vo, ResultSet rs)
	{
		MRole role = MRole.getDefault(vo.ctx, false);
		try
		{
			vo.AD_Tab_ID = rs.getInt("AD_Tab_ID");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "AD_Tab_ID", String.valueOf(vo.AD_Tab_ID));
			vo.Name = rs.getString("Name");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "Name", vo.Name);

			//	Translation Tab
			boolean showTrl = Env.getContext(vo.ctx, "#ShowTrl").equals("Y") && Env.isMultiLingualDocument(vo.ctx);
			if (!showTrl && rs.getString("IsTranslationTab").equals("Y"))
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails", "NoTranslationTab - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo.Name);
				return false;
			}
			//	Accounting Info Tab
			boolean showAcct = Env.getContext(vo.ctx, "#ShowAcct").equals("Y");
			if (!showAcct && rs.getString("IsInfoTab").equals("Y"))
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails", "NoAcctTab - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo.Name);
				return false;
			}
			//	Access Level
			vo.AccessLevel = rs.getString("AccessLevel");
			if (!role.canView (vo.ctx, vo.AccessLevel))	//	No Access
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails", "No Role Access - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo. Name);
				return false;
			}	//	Used by MField.getDefault
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "AccessLevel", vo.AccessLevel);

			//	Table Access
			vo.AD_Table_ID = rs.getInt("AD_Table_ID");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "AD_Table_ID", String.valueOf(vo.AD_Table_ID));
			if (!role.isTableAccess(vo.AD_Table_ID, true))
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails", "No Table Access - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo. Name);
				return false;
			}
			if (rs.getString("IsReadOnly").equals("Y"))
				vo.IsReadOnly = true;

			//
			vo.Description = rs.getString("Description");
			if (vo.Description == null)
				vo.Description = "";
			vo.Help = rs.getString("Help");
			if (vo.Help == null)
				vo.Help = "";

			if (rs.getString("IsSingleRow").equals("Y"))
				vo.IsSingleRow = true;
			if (rs.getString("HasTree").equals("Y"))
				vo.HasTree = true;

			vo.AD_Table_ID = rs.getInt("AD_Table_ID");
			vo.TableName = rs.getString("TableName");
			if (rs.getString("IsView").equals("Y"))
				vo.IsView = true;
			vo.AD_Column_ID = rs.getInt("AD_Column_ID");   //  Primary Parent Column

			if (rs.getString("IsSecurityEnabled").equals("Y"))
				vo.IsSecurityEnabled = true;
			if (rs.getString("IsDeleteable").equals("Y"))
				vo.IsDeleteable = true;
			if (rs.getString("IsHighVolume").equals("Y"))
				vo.IsHighVolume = true;

			vo.CommitWarning = rs.getString("CommitWarning");
			if (vo.CommitWarning == null)
				vo.CommitWarning = "";
			vo.WhereClause = rs.getString("WhereClause");
			if (vo.WhereClause == null)
				vo.WhereClause = "";
			vo.OrderByClause = rs.getString("OrderByClause");
			if (vo.OrderByClause == null)
				vo.OrderByClause = "";

			vo.AD_Process_ID = rs.getInt("AD_Process_ID");
			if (rs.wasNull())
				vo.AD_Process_ID = 0;
			vo.AD_Image_ID = rs.getInt("AD_Image_ID");
			if (rs.wasNull())
				vo.AD_Image_ID = 0;
			vo.Included_Tab_ID = rs.getInt("Included_Tab_ID");
			if (rs.wasNull())
				vo.Included_Tab_ID = 0;
			//
			vo.TabLevel = rs.getInt("TabLevel");
			if (rs.wasNull())
				vo.TabLevel = 0;
			//
			vo.IsSortTab = rs.getString("IsSortTab").equals("Y");
			if (vo.IsSortTab)
			{
				vo.AD_ColumnSortOrder_ID = rs.getInt("AD_ColumnSortOrder_ID");
				vo.AD_ColumnSortYesNo_ID = rs.getInt("AD_ColumnSortYesNo_ID");
			}
			//	Replication Type - set R/O if Reference
			try
			{
				int index = rs.findColumn ("ReplicationType");
				vo.ReplicationType = rs.getString (index);
				if ("R".equals(vo.ReplicationType))
					vo.IsReadOnly = true;
			}
			catch (Exception e)
			{
			}
		}
		catch (SQLException ex)
		{
			Log.error("MTabVO.loadTabDetails", ex);
			return false;
		}
		return true;
	}	//	loadTabDetails


	/**************************************************************************
	 *  Create Tab Fields
	 *  @param mTabVO tab value object
	 *  @return true if fields were created
	 */
	private static boolean createFields (MTabVO mTabVO)
	{
		mTabVO.Fields = new ArrayList();

		String sql = MFieldVO.getSQL(mTabVO.ctx);
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, mTabVO.AD_Tab_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				MFieldVO voF = MFieldVO.create (mTabVO.ctx, mTabVO.WindowNo, mTabVO.TabNo, mTabVO.AD_Window_ID, mTabVO.IsReadOnly, rs);
				if (voF != null)
					mTabVO.Fields.add(voF);
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e)
		{
			Log.error("MTabVO.createFields", e);
			return false;
		}
		return mTabVO.Fields.size() != 0;
	}   //  createFields

}   //  MTabVO

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
 *  Model Tab Value Object
 *
 *  @author Jorg Janke
 *  @version  $Id: MTabVO.java,v 1.17 2004/08/17 05:53:07 jjanke Exp $
 */
public class MTabVO implements Serializable
{
	/**
	 *  Private constructor - must use Factory
	 */
	private MTabVO()
	{
	}   //  MTabVO

	/** Context - replicated    */
	public  Properties      ctx;
	/** Window No - replicated  */
	public  int				WindowNo;
	/** AD Window - replicated  */
	public  int             AD_Window_ID;

	/** Tab No (not AD_Tab_ID)  */
	public  int				TabNo;

	//  Database Fields

	public	int		    AD_Tab_ID;
	public	String	    Name = "";
	public	String	    Description = "";
	public	String	    Help = "";
	public	boolean	    IsSingleRow = false;
	public  boolean     IsReadOnly = false;
	public  boolean	    HasTree = false;
	public  int		    AD_Table_ID;
	/** Primary Parent Column   */
	public  int		    AD_Column_ID = 0;
	public  String	    TableName;
	public  boolean     IsView = false;
	public  String	    AccessLevel;
	public  boolean	    IsSecurityEnabled = false;
	public  boolean	    IsDeleteable = false;
	public  boolean     IsHighVolume = false;
	public	int		    AD_Process_ID = 0;
	public  String	    CommitWarning;
	public  String	    WhereClause;
	public  String      OrderByClause;
	public  int         TabLevel = 0;
	public int          AD_Image_ID = 0;
	public int          Included_Tab_ID = 0;
	public String		ReplicationType = "L";

	//
	public boolean		IsSortTab = false;
	public int			AD_ColumnSortOrder_ID = 0;
	public int			AD_ColumnSortYesNo_ID = 0;

	//  Derived
	public  boolean     onlyCurrentRows = true;
	public int			onlyCurrentDays = 1;

	/** Fields contain MFieldVO entities    */
	public ArrayList    Fields = null;

	/**
	 *  Set Context including contained elements
	 *  @param newCtx new context
	 */
	public void setCtx (Properties newCtx)
	{
		ctx = newCtx;
		for (int i = 0; i < Fields.size() ; i++)
		{
			MFieldVO field = (MFieldVO)Fields.get(i);
			field.setCtx(newCtx);
		}
	}   //  setCtx

	/**
	 *  Return the SQL statement used for the MTabVO.create
	 *  @param ctx context
	 *  @return SQL SELECT String
	 */
	protected static String getSQL (Properties ctx)
	{
		//  View only returns IsActive='Y'
		String sql = "SELECT * FROM AD_Tab_v WHERE AD_Window_ID=?"
			+ " ORDER BY SeqNo";
		if (!Env.isBaseLanguage(ctx, "AD_Window"))
			sql = "SELECT * FROM AD_Tab_vt WHERE AD_Window_ID=?"
				+ " AND AD_Language='" + Env.getAD_Language(ctx) + "'"
				+ " ORDER BY SeqNo";
		return sql;
	}   //  getSQL

	
	/**************************************************************************
	 *	Create MTab VO
	 *
	 *  @param wVO value object
	 *  @param TabNo tab no
	 *	@param rs ResultSet from AD_Tab_v
	 *	@param isRO true if window is r/o
	 *  @param onlyCurrentRows if true query is limited to not processed records
	 *  @return TabVO
	 */
	public static MTabVO create (MWindowVO wVO, int TabNo, ResultSet rs, 
		boolean isRO, boolean onlyCurrentRows)
	{
		Log.trace(Log.l3_Util, "MTabVO.create #" + TabNo);

		MTabVO vo = new MTabVO ();
		vo.ctx = wVO.ctx;
		vo.WindowNo = wVO.WindowNo;
		vo.AD_Window_ID = wVO.AD_Window_ID;
		vo.TabNo = TabNo;
		//
		if (!loadTabDetails(vo, rs))
			return null;

		if (isRO)
		{
			Log.trace(Log.l6_Database, "MTabVO.create", "Tab is ReadOnly");
			vo.IsReadOnly = true;
		}
		vo.onlyCurrentRows = onlyCurrentRows;

		//  Create Fields
		if (vo.IsSortTab)
		{
			vo.Fields = new ArrayList();	//	dummy
		}
		else
		{
			createFields (vo);
			if (vo.Fields == null || vo.Fields.size() == 0)
			{
				Log.error("MTabVO.create - No Fields");
				return null;
			}
		}
		return vo;
	}	//	create

	/**
	 * 	Load Tab Details from rs into vo
	 * 	@param vo Tab value object
	 *	@param rs ResultSet from AD_Tab_v/t
	 * 	@return true if read ok
	 */
	private static boolean loadTabDetails (MTabVO vo, ResultSet rs)
	{
		MRole role = MRole.getDefault(vo.ctx, false);
		boolean showTrl = Env.getContext(vo.ctx, "#ShowTrl").equals("Y");
		try
		{
			vo.AD_Tab_ID = rs.getInt("AD_Tab_ID");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "AD_Tab_ID", String.valueOf(vo.AD_Tab_ID));
			vo.Name = rs.getString("Name");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "Name", vo.Name);

			//	Translation Tab	**
			if (rs.getString("IsTranslationTab").equals("Y"))
			{
				//	Document Translation
				vo.TableName = rs.getString("TableName");
				if (!Env.isBaseTranslation(vo.TableName)	//	C_UOM, ...
					&& !Env.isMultiLingualDocument(vo.ctx))
					showTrl = false;
				if (!showTrl)
				{
					Log.trace(Log.l5_DData, "MTabVO.loadTabDetails - TranslationTab Not displayed - ShowTrl="
						+ Env.getContext(vo.ctx, "#ShowTrl") + ", AD_Tab_ID=" 
						+ vo.AD_Tab_ID + "=" + vo.Name + ", Table=" + vo.TableName
						+ ", BaseTrl=" + Env.isBaseTranslation(vo.TableName)
						+ ", MultiLingual=" + Env.isMultiLingualDocument(vo.ctx));
					return false;
				}
			}
			
			//	Accounting Info Tab	**
			boolean showAcct = Env.getContext(vo.ctx, "#ShowAcct").equals("Y");
			if (!showAcct && rs.getString("IsInfoTab").equals("Y"))
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails - NoAcctTab - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo.Name);
				return false;
			}
			//	Access Level
			vo.AccessLevel = rs.getString("AccessLevel");
			if (!role.canView (vo.ctx, vo.AccessLevel))	//	No Access
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails", "No Role Access - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo. Name);
				return false;
			}	//	Used by MField.getDefault
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "AccessLevel", vo.AccessLevel);

			//	Table Access
			vo.AD_Table_ID = rs.getInt("AD_Table_ID");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "AD_Table_ID", String.valueOf(vo.AD_Table_ID));
			if (!role.isTableAccess(vo.AD_Table_ID, true))
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails", "No Table Access - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo. Name);
				return false;
			}
			if (rs.getString("IsReadOnly").equals("Y"))
				vo.IsReadOnly = true;

			//
			vo.Description = rs.getString("Description");
			if (vo.Description == null)
				vo.Description = "";
			vo.Help = rs.getString("Help");
			if (vo.Help == null)
				vo.Help = "";

			if (rs.getString("IsSingleRow").equals("Y"))
				vo.IsSingleRow = true;
			if (rs.getString("HasTree").equals("Y"))
				vo.HasTree = true;

			vo.AD_Table_ID = rs.getInt("AD_Table_ID");
			vo.TableName = rs.getString("TableName");
			if (rs.getString("IsView").equals("Y"))
				vo.IsView = true;
			vo.AD_Column_ID = rs.getInt("AD_Column_ID");   //  Primary Parent Column

			if (rs.getString("IsSecurityEnabled").equals("Y"))
				vo.IsSecurityEnabled = true;
			if (rs.getString("IsDeleteable").equals("Y"))
				vo.IsDeleteable = true;
			if (rs.getString("IsHighVolume").equals("Y"))
				vo.IsHighVolume = true;

			vo.CommitWarning = rs.getString("CommitWarning");
			if (vo.CommitWarning == null)
				vo.CommitWarning = "";
			vo.WhereClause = rs.getString("WhereClause");
			if (vo.WhereClause == null)
				vo.WhereClause = "";
			vo.OrderByClause = rs.getString("OrderByClause");
			if (vo.OrderByClause == null)
				vo.OrderByClause = "";

			vo.AD_Process_ID = rs.getInt("AD_Process_ID");
			if (rs.wasNull())
				vo.AD_Process_ID = 0;
			vo.AD_Image_ID = rs.getInt("AD_Image_ID");
			if (rs.wasNull())
				vo.AD_Image_ID = 0;
			vo.Included_Tab_ID = rs.getInt("Included_Tab_ID");
			if (rs.wasNull())
				vo.Included_Tab_ID = 0;
			//
			vo.TabLevel = rs.getInt("TabLevel");
			if (rs.wasNull())
				vo.TabLevel = 0;
			//
			vo.IsSortTab = rs.getString("IsSortTab").equals("Y");
			if (vo.IsSortTab)
			{
				vo.AD_ColumnSortOrder_ID = rs.getInt("AD_ColumnSortOrder_ID");
				vo.AD_ColumnSortYesNo_ID = rs.getInt("AD_ColumnSortYesNo_ID");
			}
			//	Replication Type - set R/O if Reference
			try
			{
				int index = rs.findColumn ("ReplicationType");
				vo.ReplicationType = rs.getString (index);
				if ("R".equals(vo.ReplicationType))
					vo.IsReadOnly = true;
			}
			catch (Exception e)
			{
			}
		}
		catch (SQLException ex)
		{
			Log.error("MTabVO.loadTabDetails", ex);
			return false;
		}
		
		return true;
	}	//	loadTabDetails


	/**************************************************************************
	 *  Create Tab Fields
	 *  @param mTabVO tab value object
	 *  @return true if fields were created
	 */
	private static boolean createFields (MTabVO mTabVO)
	{
		mTabVO.Fields = new ArrayList();

		String sql = MFieldVO.getSQL(mTabVO.ctx);
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, mTabVO.AD_Tab_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				MFieldVO voF = MFieldVO.create (mTabVO.ctx, mTabVO.WindowNo, mTabVO.TabNo, mTabVO.AD_Window_ID, mTabVO.IsReadOnly, rs);
				if (voF != null)
					mTabVO.Fields.add(voF);
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e)
		{
			Log.error("MTabVO.createFields", e);
			return false;
		}
		return mTabVO.Fields.size() != 0;
	}   //  createFields

}   //  MTabVO

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
 *  Model Tab Value Object
 *
 *  @author Jorg Janke
 *  @version  $Id: MTabVO.java,v 1.13 2004/05/18 05:07:19 jjanke Exp $
 */
public class MTabVO implements Serializable
{
	/**
	 *  Private constructor - must use Factory
	 */
	private MTabVO()
	{
	}   //  MTabVO

	/** Context - replicated    */
	public  Properties      ctx;
	/** Window No - replicated  */
	public  int				WindowNo;
	/** AD Window - replicated  */
	public  int             AD_Window_ID;

	/** Tab No (not AD_Tab_ID)  */
	public  int				TabNo;

	//  Database Fields

	public	int		    AD_Tab_ID;
	public	String	    Name = "";
	public	String	    Description = "";
	public	String	    Help = "";
	public	boolean	    IsSingleRow = false;
	public  boolean     IsReadOnly = false;
	public  boolean	    HasTree = false;
	public  int		    AD_Table_ID;
	/** Primary Parent Column   */
	public  int		    AD_Column_ID = 0;
	public  String	    TableName;
	public  boolean     IsView = false;
	public  String	    AccessLevel;
	public  boolean	    IsSecurityEnabled = false;
	public  boolean	    IsDeleteable = false;
	public  boolean     IsHighVolume = false;
	public	int		    AD_Process_ID = 0;
	public  String	    CommitWarning;
	public  String	    WhereClause;
	public  String      OrderByClause;
	public  int         TabLevel = 0;
	public int          AD_Image_ID = 0;
	public int          Included_Tab_ID = 0;
	public String		ReplicationType = "L";

	//
	public boolean		IsSortTab = false;
	public int			AD_ColumnSortOrder_ID = 0;
	public int			AD_ColumnSortYesNo_ID = 0;

	//  Derived
	public  boolean     onlyCurrentRows = true;
	public int			onlyCurrentDays = 1;

	/** Fields contain MFieldVO entities    */
	public ArrayList    Fields = null;

	/**
	 *  Set Context including contained elements
	 *  @param newCtx new context
	 */
	public void setCtx (Properties newCtx)
	{
		ctx = newCtx;
		for (int i = 0; i < Fields.size() ; i++)
		{
			MFieldVO field = (MFieldVO)Fields.get(i);
			field.setCtx(newCtx);
		}
	}   //  setCtx

	/**
	 *  Return the SQL statement used for the MTabVO.create
	 *  @param ctx context
	 *  @return SQL SELECT String
	 */
	protected static String getSQL (Properties ctx)
	{
		//  View only returns IsActive='Y'
		String sql = "SELECT * FROM AD_Tab_v WHERE AD_Window_ID=?"
			+ " ORDER BY SeqNo";
		if (!Env.isBaseLanguage(ctx, "AD_Window"))
			sql = "SELECT * FROM AD_Tab_vt WHERE AD_Window_ID=?"
				+ " AND AD_Language='" + Env.getAD_Language(ctx) + "'"
				+ " ORDER BY SeqNo";
		return sql;
	}   //  getSQL

	
	/**************************************************************************
	 *	Create MTabVO
	 *
	 *  @param wVO value object
	 *  @param TabNo tab no
	 *	@param rs ResultSet from AD_Tab_v
	 *	@param isRO true if window is r/o
	 *  @param onlyCurrentRows if true query is limited to not processed records
	 *  @return TabVO
	 */
	public static MTabVO create (MWindowVO wVO, int TabNo, ResultSet rs, boolean isRO, boolean onlyCurrentRows)
	{
		Log.trace(Log.l3_Util, "MTabVO.create #" + TabNo);

		MTabVO vo = new MTabVO ();
		vo.ctx = wVO.ctx;
		vo.WindowNo = wVO.WindowNo;
		vo.AD_Window_ID = wVO.AD_Window_ID;
		vo.TabNo = TabNo;
		//
		if (!loadTabDetails(vo, rs))
			return null;

		if (isRO)
		{
			Log.trace(Log.l6_Database, "MTabVO.create", "Tab is ReadOnly");
			vo.IsReadOnly = true;
		}
		vo.onlyCurrentRows = onlyCurrentRows;

		//  Create Fields
		if (vo.IsSortTab)
		{
			vo.Fields = new ArrayList();	//	dummy
		}
		else
		{
			createFields (vo);
			if (vo.Fields == null || vo.Fields.size() == 0)
			{
				Log.error("MTabVO.create - No Fields");
				return null;
			}
		}
		return vo;
	}	//	create

	/**
	 * 	Load Tab Details from rs into vo
	 * 	@param vo Tab value object
	 *	@param rs ResultSet from AD_Tab_v/t
	 * 	@return true if read ok
	 */
	private static boolean loadTabDetails (MTabVO vo, ResultSet rs)
	{
		MRole role = MRole.getDefault(vo.ctx, false);
		try
		{
			vo.AD_Tab_ID = rs.getInt("AD_Tab_ID");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "AD_Tab_ID", String.valueOf(vo.AD_Tab_ID));
			vo.Name = rs.getString("Name");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "Name", vo.Name);

			//	Translation Tab
			boolean showTrl = Env.getContext(vo.ctx, "#ShowTrl").equals("Y") && Env.isMultiLingualDocument(vo.ctx);
			if (!showTrl && rs.getString("IsTranslationTab").equals("Y"))
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails", "NoTranslationTab - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo.Name);
				return false;
			}
			//	Accounting Info Tab
			boolean showAcct = Env.getContext(vo.ctx, "#ShowAcct").equals("Y");
			if (!showAcct && rs.getString("IsInfoTab").equals("Y"))
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails", "NoAcctTab - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo.Name);
				return false;
			}
			//	Access Level
			vo.AccessLevel = rs.getString("AccessLevel");
			if (!role.canView (vo.ctx, vo.AccessLevel))	//	No Access
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails", "No Role Access - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo. Name);
				return false;
			}	//	Used by MField.getDefault
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "AccessLevel", vo.AccessLevel);

			//	Table Access
			vo.AD_Table_ID = rs.getInt("AD_Table_ID");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "AD_Table_ID", String.valueOf(vo.AD_Table_ID));
			if (!role.isTableAccess(vo.AD_Table_ID, true))
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails", "No Table Access - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo. Name);
				return false;
			}
			if (rs.getString("IsReadOnly").equals("Y"))
				vo.IsReadOnly = true;

			//
			vo.Description = rs.getString("Description");
			if (vo.Description == null)
				vo.Description = "";
			vo.Help = rs.getString("Help");
			if (vo.Help == null)
				vo.Help = "";

			if (rs.getString("IsSingleRow").equals("Y"))
				vo.IsSingleRow = true;
			if (rs.getString("HasTree").equals("Y"))
				vo.HasTree = true;

			vo.AD_Table_ID = rs.getInt("AD_Table_ID");
			vo.TableName = rs.getString("TableName");
			if (rs.getString("IsView").equals("Y"))
				vo.IsView = true;
			vo.AD_Column_ID = rs.getInt("AD_Column_ID");   //  Primary Parent Column

			if (rs.getString("IsSecurityEnabled").equals("Y"))
				vo.IsSecurityEnabled = true;
			if (rs.getString("IsDeleteable").equals("Y"))
				vo.IsDeleteable = true;
			if (rs.getString("IsHighVolume").equals("Y"))
				vo.IsHighVolume = true;

			vo.CommitWarning = rs.getString("CommitWarning");
			if (vo.CommitWarning == null)
				vo.CommitWarning = "";
			vo.WhereClause = rs.getString("WhereClause");
			if (vo.WhereClause == null)
				vo.WhereClause = "";
			vo.OrderByClause = rs.getString("OrderByClause");
			if (vo.OrderByClause == null)
				vo.OrderByClause = "";

			vo.AD_Process_ID = rs.getInt("AD_Process_ID");
			if (rs.wasNull())
				vo.AD_Process_ID = 0;
			vo.AD_Image_ID = rs.getInt("AD_Image_ID");
			if (rs.wasNull())
				vo.AD_Image_ID = 0;
			vo.Included_Tab_ID = rs.getInt("Included_Tab_ID");
			if (rs.wasNull())
				vo.Included_Tab_ID = 0;
			//
			vo.TabLevel = rs.getInt("TabLevel");
			if (rs.wasNull())
				vo.TabLevel = 0;
			//
			vo.IsSortTab = rs.getString("IsSortTab").equals("Y");
			if (vo.IsSortTab)
			{
				vo.AD_ColumnSortOrder_ID = rs.getInt("AD_ColumnSortOrder_ID");
				vo.AD_ColumnSortYesNo_ID = rs.getInt("AD_ColumnSortYesNo_ID");
			}
			//	Replication Type - set R/O if Reference
			try
			{
				int index = rs.findColumn ("ReplicationType");
				vo.ReplicationType = rs.getString (index);
				if ("R".equals(vo.ReplicationType))
					vo.IsReadOnly = true;
			}
			catch (Exception e)
			{
			}
		}
		catch (SQLException ex)
		{
			Log.error("MTabVO.loadTabDetails", ex);
			return false;
		}
		return true;
	}	//	loadTabDetails


	/**************************************************************************
	 *  Create Tab Fields
	 *  @param mTabVO tab value object
	 *  @return true if fields were created
	 */
	private static boolean createFields (MTabVO mTabVO)
	{
		mTabVO.Fields = new ArrayList();

		String sql = MFieldVO.getSQL(mTabVO.ctx);
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, mTabVO.AD_Tab_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				MFieldVO voF = MFieldVO.create (mTabVO.ctx, mTabVO.WindowNo, mTabVO.TabNo, mTabVO.AD_Window_ID, mTabVO.IsReadOnly, rs);
				if (voF != null)
					mTabVO.Fields.add(voF);
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e)
		{
			Log.error("MTabVO.createFields", e);
			return false;
		}
		return mTabVO.Fields.size() != 0;
	}   //  createFields

}   //  MTabVO

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

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import org.compiere.util.Access;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Log;

/**
 *  Model Tab Value Object
 *
 *  @author Jorg Janke
 *  @version  $Id: MTabVO.java,v 1.9 2003/11/02 07:49:56 jjanke Exp $
 */
public class MTabVO implements Serializable
{
	/**
	 *  Private constructor - must use Factory
	 */
	private MTabVO()
	{
	}   //  MTabVO

	/** Context - replicated    */
	public  Properties      ctx;
	/** Window No - replicated  */
	public  int				WindowNo;
	/** AD Window - replicated  */
	public  int             AD_Window_ID;

	/** Tab No (not AD_Tab_ID)  */
	public  int				TabNo;

	//  Database Fields

	public	int		    AD_Tab_ID;
	public	String	    Name = "";
	public	String	    Description = "";
	public	String	    Help = "";
	public	boolean	    IsSingleRow = false;
	public  boolean     IsReadOnly = false;
	public  boolean	    HasTree = false;
	public  int		    AD_Table_ID;
	/** Primary Parent Column   */
	public  int		    AD_Column_ID = 0;
	public  String	    TableName;
	public  boolean     IsView = false;
	public  String	    AccessLevel;
	public  boolean	    IsSecurityEnabled = false;
	public  boolean	    IsDeleteable = false;
	public  boolean     IsHighVolume = false;
	public	int		    AD_Process_ID = 0;
	public  String	    CommitWarning;
	public  String	    WhereClause;
	public  String      OrderByClause;
	public  int         TabLevel = 0;
	public int          AD_Image_ID = 0;
	public int          Included_Tab_ID = 0;
	public String		ReplicationType = "L";

	//
	public boolean		IsSortTab = false;
	public int			AD_ColumnSortOrder_ID = 0;
	public int			AD_ColumnSortYesNo_ID = 0;

	//  Derived
	public  boolean     onlyCurrentRows = true;
	public int			onlyCurrentDays = 1;

	/** Fields contain MFieldVO entities    */
	public ArrayList    Fields = null;

	/**
	 *  Set Context including contained elements
	 *  @param newCtx new context
	 */
	public void setCtx (Properties newCtx)
	{
		ctx = newCtx;
		for (int i = 0; i < Fields.size() ; i++)
		{
			MFieldVO field = (MFieldVO)Fields.get(i);
			field.setCtx(newCtx);
		}
	}   //  setCtx

	/**
	 *  Return the SQL statement used for the MTabVO.create
	 *  @param mWindowVO WindowVO
	 *  @return SQL SELECT String
	 */
	protected static String getSQL (MWindowVO mWindowVO)
	{
		//  View only returns IsActive='Y'
		String sql = "SELECT * FROM AD_Tab_v WHERE AD_Window_ID=?"
			+ " ORDER BY SeqNo";
		if (!Env.isBaseLanguage(mWindowVO.ctx, "AD_Window"))
			sql = "SELECT * FROM AD_Tab_vt WHERE AD_Window_ID=?"
				+ " AND AD_Language='" + Env.getAD_Language(mWindowVO.ctx) + "'"
				+ " ORDER BY SeqNo";
		return sql;
	}   //  getSQL

	/*************************************************************************/

	/**
	 *	Create MTabVO
	 *
	 *  @param wVO value object
	 *  @param TabNo tab no
	 *	@param rs ResultSet from AD_Tab_v
	 *	@param isRO true if window is r/o
	 *  @param onlyCurrentRows if true query is limited to not processed records
	 *  @return TabVO
	 */
	public static MTabVO create (MWindowVO wVO, int TabNo, ResultSet rs, boolean isRO, boolean onlyCurrentRows)
	{
		Log.trace(Log.l3_Util, "MTabVO.create #" + TabNo);

		MTabVO vo = new MTabVO ();
		vo.ctx = wVO.ctx;
		vo.WindowNo = wVO.WindowNo;
		vo.AD_Window_ID = wVO.AD_Window_ID;
		vo.TabNo = TabNo;
		//
		if (!loadTabDetails(vo, rs))
			return null;

		if (isRO)
		{
			Log.trace(Log.l6_Database, "MTabVO.create", "Tab is ReadOnly");
			vo.IsReadOnly = true;
		}
		vo.onlyCurrentRows = onlyCurrentRows;

		//  Create Fields
		if (vo.IsSortTab)
		{
			vo.Fields = new ArrayList();	//	dummy
		}
		else
		{
			createFields (vo);
			if (vo.Fields == null || vo.Fields.size() == 0)
			{
				Log.error("MTabVO.create - No Fields");
				return null;
			}
		}
		return vo;
	}	//	create

	/**
	 *	Create MTabVO
	 *
	 * 	@param ctx context
	 * 	@param WindowNo window
	 * 	@param AD_Table_ID table
	 *  @return TabVO
	  */
	public static MTabVO create(Properties ctx, int WindowNo, int AD_Table_ID)
	{
		Log.trace(Log.l3_Util, "MTabVO.create");

		MTabVO vo = new MTabVO ();
		vo.ctx = ctx;
		vo.WindowNo = WindowNo;
		vo.AD_Window_ID = 0;
		vo.TabNo = 0;
		//
		boolean resultOK = false;
		String sql = "SELECT * FROM AD_Tab_v WHERE AD_Table_ID=?";
		if (!Env.isBaseLanguage(ctx, "AD_Tab"))
			sql = "SELECT * FROM AD_Tab_vt WHERE AD_Table_ID=?"
				+ " AND AD_Language='" + Env.getAD_Language(ctx) + "'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Table_ID);
			ResultSet rs = pstmt.executeQuery();
			//
			if (rs.next())
				resultOK = loadTabDetails(vo, rs);
			else
				Log.trace(Log.l6_Database, "MTabVO.create", "No Tab for AD_Table_ID=" + AD_Table_ID);
			//
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTabVO.create(1)", e);
		}

		if (!resultOK)
			return null;

		//  Create Fields
		if (vo.IsSortTab)
		{
			vo.Fields = new ArrayList();	//	dummy
		}
		else
		{
			createFields (vo);
			if (vo.Fields == null || vo.Fields.size() == 0)
			{
				Log.error("MTabVO.create - No Fields");
				return null;
			}
		}
		return vo;
	}	//	create

	/**
	 * 	Load Tab Details from rs into vo
	 * 	@param vo Tab value object
	 *	@param rs ResultSet from AD_Tab_v/t
	 * 	@return true if read ok
	 */
	private static boolean loadTabDetails (MTabVO vo, ResultSet rs)
	{
		MRole role = MRole.getDefault(vo.ctx, false);
		try
		{
			vo.AD_Tab_ID = rs.getInt("AD_Tab_ID");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "AD_Tab_ID", String.valueOf(vo.AD_Tab_ID));
			vo.Name = rs.getString("Name");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "Name", vo.Name);

			//	Translation Tab
			boolean showTrl = Env.getContext(vo.ctx, "#ShowTrl").equals("Y") && Env.isMultiLingualDocument(vo.ctx);
			if (!showTrl && rs.getString("IsTranslationTab").equals("Y"))
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails", "NoTranslationTab - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo.Name);
				return false;
			}
			//	Accounting Info Tab
			boolean showAcct = Env.getContext(vo.ctx, "#ShowAcct").equals("Y");
			if (!showAcct && rs.getString("IsInfoTab").equals("Y"))
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails", "NoAcctTab - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo.Name);
				return false;
			}
			//	Access Level
			vo.AccessLevel = rs.getString("AccessLevel");
			if (!role.canView (vo.ctx, vo.AccessLevel))	//	No Access
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails", "No Role Access - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo. Name);
				return false;
			}	//	Used by MField.getDefault
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "AccessLevel", vo.AccessLevel);

			//	Table Access
			vo.AD_Table_ID = rs.getInt("AD_Table_ID");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "AD_Table_ID", String.valueOf(vo.AD_Table_ID));
			if (!role.isTableAccess(vo.AD_Table_ID, true))
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails", "No Table Access - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo. Name);
				return false;
			}
			if (rs.getString("IsReadOnly").equals("Y"))
				vo.IsReadOnly = true;

			//
			vo.Description = rs.getString("Description");
			if (vo.Description == null)
				vo.Description = "";
			vo.Help = rs.getString("Help");
			if (vo.Help == null)
				vo.Help = "";

			if (rs.getString("IsSingleRow").equals("Y"))
				vo.IsSingleRow = true;
			if (rs.getString("HasTree").equals("Y"))
				vo.HasTree = true;

			vo.AD_Table_ID = rs.getInt("AD_Table_ID");
			vo.TableName = rs.getString("TableName");
			if (rs.getString("IsView").equals("Y"))
				vo.IsView = true;
			vo.AD_Column_ID = rs.getInt("AD_Column_ID");   //  Primary Parent Column

			if (rs.getString("IsSecurityEnabled").equals("Y"))
				vo.IsSecurityEnabled = true;
			if (rs.getString("IsDeleteable").equals("Y"))
				vo.IsDeleteable = true;
			if (rs.getString("IsHighVolume").equals("Y"))
				vo.IsHighVolume = true;

			vo.CommitWarning = rs.getString("CommitWarning");
			if (vo.CommitWarning == null)
				vo.CommitWarning = "";
			vo.WhereClause = rs.getString("WhereClause");
			if (vo.WhereClause == null)
				vo.WhereClause = "";
			vo.OrderByClause = rs.getString("OrderByClause");
			if (vo.OrderByClause == null)
				vo.OrderByClause = "";

			vo.AD_Process_ID = rs.getInt("AD_Process_ID");
			if (rs.wasNull())
				vo.AD_Process_ID = 0;
			vo.AD_Image_ID = rs.getInt("AD_Image_ID");
			if (rs.wasNull())
				vo.AD_Image_ID = 0;
			vo.Included_Tab_ID = rs.getInt("Included_Tab_ID");
			if (rs.wasNull())
				vo.Included_Tab_ID = 0;
			//
			vo.TabLevel = rs.getInt("TabLevel");
			if (rs.wasNull())
				vo.TabLevel = 0;
			//
			vo.IsSortTab = rs.getString("IsSortTab").equals("Y");
			if (vo.IsSortTab)
			{
				vo.AD_ColumnSortOrder_ID = rs.getInt("AD_ColumnSortOrder_ID");
				vo.AD_ColumnSortYesNo_ID = rs.getInt("AD_ColumnSortYesNo_ID");
			}
			//	Replication Type - set R/O if Reference
			try
			{
				int index = rs.findColumn ("ReplicationType");
				vo.ReplicationType = rs.getString (index);
				if ("R".equals(vo.ReplicationType))
					vo.IsReadOnly = true;
			}
			catch (Exception e)
			{
			}
		}
		catch (SQLException ex)
		{
			Log.error("MTabVO.loadTabDetails", ex);
			return false;
		}
		return true;
	}	//	loadTabDetails


	/*************************************************************************/

	/**
	 *  Create Tab Fields
	 *  @param mTabVO tab value object
	 *  @return true if fields were created
	 */
	private static boolean createFields (MTabVO mTabVO)
	{
		mTabVO.Fields = new ArrayList();

		String sql = MFieldVO.getSQL(mTabVO);
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, mTabVO.AD_Tab_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				MFieldVO voF = MFieldVO.create (mTabVO.ctx, mTabVO.WindowNo, mTabVO.TabNo, mTabVO.AD_Window_ID, mTabVO.IsReadOnly, rs);
				if (voF != null)
					mTabVO.Fields.add(voF);
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e)
		{
			Log.error("MTabVO.createFields", e);
			return false;
		}
		return mTabVO.Fields.size() != 0;
	}   //  createFields

}   //  MTabVO

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
 *  Model Tab Value Object
 *
 *  @author Jorg Janke
 *  @version  $Id: MTabVO.java,v 1.17 2004/08/17 05:53:07 jjanke Exp $
 */
public class MTabVO implements Serializable
{
	/**
	 *  Private constructor - must use Factory
	 */
	private MTabVO()
	{
	}   //  MTabVO

	/** Context - replicated    */
	public  Properties      ctx;
	/** Window No - replicated  */
	public  int				WindowNo;
	/** AD Window - replicated  */
	public  int             AD_Window_ID;

	/** Tab No (not AD_Tab_ID)  */
	public  int				TabNo;

	//  Database Fields

	public	int		    AD_Tab_ID;
	public	String	    Name = "";
	public	String	    Description = "";
	public	String	    Help = "";
	public	boolean	    IsSingleRow = false;
	public  boolean     IsReadOnly = false;
	public  boolean	    HasTree = false;
	public  int		    AD_Table_ID;
	/** Primary Parent Column   */
	public  int		    AD_Column_ID = 0;
	public  String	    TableName;
	public  boolean     IsView = false;
	public  String	    AccessLevel;
	public  boolean	    IsSecurityEnabled = false;
	public  boolean	    IsDeleteable = false;
	public  boolean     IsHighVolume = false;
	public	int		    AD_Process_ID = 0;
	public  String	    CommitWarning;
	public  String	    WhereClause;
	public  String      OrderByClause;
	public  int         TabLevel = 0;
	public int          AD_Image_ID = 0;
	public int          Included_Tab_ID = 0;
	public String		ReplicationType = "L";

	//
	public boolean		IsSortTab = false;
	public int			AD_ColumnSortOrder_ID = 0;
	public int			AD_ColumnSortYesNo_ID = 0;

	//  Derived
	public  boolean     onlyCurrentRows = true;
	public int			onlyCurrentDays = 1;

	/** Fields contain MFieldVO entities    */
	public ArrayList    Fields = null;

	/**
	 *  Set Context including contained elements
	 *  @param newCtx new context
	 */
	public void setCtx (Properties newCtx)
	{
		ctx = newCtx;
		for (int i = 0; i < Fields.size() ; i++)
		{
			MFieldVO field = (MFieldVO)Fields.get(i);
			field.setCtx(newCtx);
		}
	}   //  setCtx

	/**
	 *  Return the SQL statement used for the MTabVO.create
	 *  @param ctx context
	 *  @return SQL SELECT String
	 */
	protected static String getSQL (Properties ctx)
	{
		//  View only returns IsActive='Y'
		String sql = "SELECT * FROM AD_Tab_v WHERE AD_Window_ID=?"
			+ " ORDER BY SeqNo";
		if (!Env.isBaseLanguage(ctx, "AD_Window"))
			sql = "SELECT * FROM AD_Tab_vt WHERE AD_Window_ID=?"
				+ " AND AD_Language='" + Env.getAD_Language(ctx) + "'"
				+ " ORDER BY SeqNo";
		return sql;
	}   //  getSQL

	
	/**************************************************************************
	 *	Create MTab VO
	 *
	 *  @param wVO value object
	 *  @param TabNo tab no
	 *	@param rs ResultSet from AD_Tab_v
	 *	@param isRO true if window is r/o
	 *  @param onlyCurrentRows if true query is limited to not processed records
	 *  @return TabVO
	 */
	public static MTabVO create (MWindowVO wVO, int TabNo, ResultSet rs, 
		boolean isRO, boolean onlyCurrentRows)
	{
		Log.trace(Log.l3_Util, "MTabVO.create #" + TabNo);

		MTabVO vo = new MTabVO ();
		vo.ctx = wVO.ctx;
		vo.WindowNo = wVO.WindowNo;
		vo.AD_Window_ID = wVO.AD_Window_ID;
		vo.TabNo = TabNo;
		//
		if (!loadTabDetails(vo, rs))
			return null;

		if (isRO)
		{
			Log.trace(Log.l6_Database, "MTabVO.create", "Tab is ReadOnly");
			vo.IsReadOnly = true;
		}
		vo.onlyCurrentRows = onlyCurrentRows;

		//  Create Fields
		if (vo.IsSortTab)
		{
			vo.Fields = new ArrayList();	//	dummy
		}
		else
		{
			createFields (vo);
			if (vo.Fields == null || vo.Fields.size() == 0)
			{
				Log.error("MTabVO.create - No Fields");
				return null;
			}
		}
		return vo;
	}	//	create

	/**
	 * 	Load Tab Details from rs into vo
	 * 	@param vo Tab value object
	 *	@param rs ResultSet from AD_Tab_v/t
	 * 	@return true if read ok
	 */
	private static boolean loadTabDetails (MTabVO vo, ResultSet rs)
	{
		MRole role = MRole.getDefault(vo.ctx, false);
		boolean showTrl = Env.getContext(vo.ctx, "#ShowTrl").equals("Y");
		try
		{
			vo.AD_Tab_ID = rs.getInt("AD_Tab_ID");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "AD_Tab_ID", String.valueOf(vo.AD_Tab_ID));
			vo.Name = rs.getString("Name");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "Name", vo.Name);

			//	Translation Tab	**
			if (rs.getString("IsTranslationTab").equals("Y"))
			{
				//	Document Translation
				vo.TableName = rs.getString("TableName");
				if (!Env.isBaseTranslation(vo.TableName)	//	C_UOM, ...
					&& !Env.isMultiLingualDocument(vo.ctx))
					showTrl = false;
				if (!showTrl)
				{
					Log.trace(Log.l5_DData, "MTabVO.loadTabDetails - TranslationTab Not displayed - ShowTrl="
						+ Env.getContext(vo.ctx, "#ShowTrl") + ", AD_Tab_ID=" 
						+ vo.AD_Tab_ID + "=" + vo.Name + ", Table=" + vo.TableName
						+ ", BaseTrl=" + Env.isBaseTranslation(vo.TableName)
						+ ", MultiLingual=" + Env.isMultiLingualDocument(vo.ctx));
					return false;
				}
			}
			
			//	Accounting Info Tab	**
			boolean showAcct = Env.getContext(vo.ctx, "#ShowAcct").equals("Y");
			if (!showAcct && rs.getString("IsInfoTab").equals("Y"))
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails - NoAcctTab - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo.Name);
				return false;
			}
			//	Access Level
			vo.AccessLevel = rs.getString("AccessLevel");
			if (!role.canView (vo.ctx, vo.AccessLevel))	//	No Access
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails", "No Role Access - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo. Name);
				return false;
			}	//	Used by MField.getDefault
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "AccessLevel", vo.AccessLevel);

			//	Table Access
			vo.AD_Table_ID = rs.getInt("AD_Table_ID");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "AD_Table_ID", String.valueOf(vo.AD_Table_ID));
			if (!role.isTableAccess(vo.AD_Table_ID, true))
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails", "No Table Access - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo. Name);
				return false;
			}
			if (rs.getString("IsReadOnly").equals("Y"))
				vo.IsReadOnly = true;

			//
			vo.Description = rs.getString("Description");
			if (vo.Description == null)
				vo.Description = "";
			vo.Help = rs.getString("Help");
			if (vo.Help == null)
				vo.Help = "";

			if (rs.getString("IsSingleRow").equals("Y"))
				vo.IsSingleRow = true;
			if (rs.getString("HasTree").equals("Y"))
				vo.HasTree = true;

			vo.AD_Table_ID = rs.getInt("AD_Table_ID");
			vo.TableName = rs.getString("TableName");
			if (rs.getString("IsView").equals("Y"))
				vo.IsView = true;
			vo.AD_Column_ID = rs.getInt("AD_Column_ID");   //  Primary Parent Column

			if (rs.getString("IsSecurityEnabled").equals("Y"))
				vo.IsSecurityEnabled = true;
			if (rs.getString("IsDeleteable").equals("Y"))
				vo.IsDeleteable = true;
			if (rs.getString("IsHighVolume").equals("Y"))
				vo.IsHighVolume = true;

			vo.CommitWarning = rs.getString("CommitWarning");
			if (vo.CommitWarning == null)
				vo.CommitWarning = "";
			vo.WhereClause = rs.getString("WhereClause");
			if (vo.WhereClause == null)
				vo.WhereClause = "";
			vo.OrderByClause = rs.getString("OrderByClause");
			if (vo.OrderByClause == null)
				vo.OrderByClause = "";

			vo.AD_Process_ID = rs.getInt("AD_Process_ID");
			if (rs.wasNull())
				vo.AD_Process_ID = 0;
			vo.AD_Image_ID = rs.getInt("AD_Image_ID");
			if (rs.wasNull())
				vo.AD_Image_ID = 0;
			vo.Included_Tab_ID = rs.getInt("Included_Tab_ID");
			if (rs.wasNull())
				vo.Included_Tab_ID = 0;
			//
			vo.TabLevel = rs.getInt("TabLevel");
			if (rs.wasNull())
				vo.TabLevel = 0;
			//
			vo.IsSortTab = rs.getString("IsSortTab").equals("Y");
			if (vo.IsSortTab)
			{
				vo.AD_ColumnSortOrder_ID = rs.getInt("AD_ColumnSortOrder_ID");
				vo.AD_ColumnSortYesNo_ID = rs.getInt("AD_ColumnSortYesNo_ID");
			}
			//	Replication Type - set R/O if Reference
			try
			{
				int index = rs.findColumn ("ReplicationType");
				vo.ReplicationType = rs.getString (index);
				if ("R".equals(vo.ReplicationType))
					vo.IsReadOnly = true;
			}
			catch (Exception e)
			{
			}
		}
		catch (SQLException ex)
		{
			Log.error("MTabVO.loadTabDetails", ex);
			return false;
		}
		
		return true;
	}	//	loadTabDetails


	/**************************************************************************
	 *  Create Tab Fields
	 *  @param mTabVO tab value object
	 *  @return true if fields were created
	 */
	private static boolean createFields (MTabVO mTabVO)
	{
		mTabVO.Fields = new ArrayList();

		String sql = MFieldVO.getSQL(mTabVO.ctx);
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, mTabVO.AD_Tab_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				MFieldVO voF = MFieldVO.create (mTabVO.ctx, mTabVO.WindowNo, mTabVO.TabNo, mTabVO.AD_Window_ID, mTabVO.IsReadOnly, rs);
				if (voF != null)
					mTabVO.Fields.add(voF);
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e)
		{
			Log.error("MTabVO.createFields", e);
			return false;
		}
		return mTabVO.Fields.size() != 0;
	}   //  createFields

}   //  MTabVO

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

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import org.compiere.util.Access;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Log;

/**
 *  Model Tab Value Object
 *
 *  @author Jorg Janke
 *  @version  $Id: MTabVO.java,v 1.9 2003/11/02 07:49:56 jjanke Exp $
 */
public class MTabVO implements Serializable
{
	/**
	 *  Private constructor - must use Factory
	 */
	private MTabVO()
	{
	}   //  MTabVO

	/** Context - replicated    */
	public  Properties      ctx;
	/** Window No - replicated  */
	public  int				WindowNo;
	/** AD Window - replicated  */
	public  int             AD_Window_ID;

	/** Tab No (not AD_Tab_ID)  */
	public  int				TabNo;

	//  Database Fields

	public	int		    AD_Tab_ID;
	public	String	    Name = "";
	public	String	    Description = "";
	public	String	    Help = "";
	public	boolean	    IsSingleRow = false;
	public  boolean     IsReadOnly = false;
	public  boolean	    HasTree = false;
	public  int		    AD_Table_ID;
	/** Primary Parent Column   */
	public  int		    AD_Column_ID = 0;
	public  String	    TableName;
	public  boolean     IsView = false;
	public  String	    AccessLevel;
	public  boolean	    IsSecurityEnabled = false;
	public  boolean	    IsDeleteable = false;
	public  boolean     IsHighVolume = false;
	public	int		    AD_Process_ID = 0;
	public  String	    CommitWarning;
	public  String	    WhereClause;
	public  String      OrderByClause;
	public  int         TabLevel = 0;
	public int          AD_Image_ID = 0;
	public int          Included_Tab_ID = 0;
	public String		ReplicationType = "L";

	//
	public boolean		IsSortTab = false;
	public int			AD_ColumnSortOrder_ID = 0;
	public int			AD_ColumnSortYesNo_ID = 0;

	//  Derived
	public  boolean     onlyCurrentRows = true;
	public int			onlyCurrentDays = 1;

	/** Fields contain MFieldVO entities    */
	public ArrayList    Fields = null;

	/**
	 *  Set Context including contained elements
	 *  @param newCtx new context
	 */
	public void setCtx (Properties newCtx)
	{
		ctx = newCtx;
		for (int i = 0; i < Fields.size() ; i++)
		{
			MFieldVO field = (MFieldVO)Fields.get(i);
			field.setCtx(newCtx);
		}
	}   //  setCtx

	/**
	 *  Return the SQL statement used for the MTabVO.create
	 *  @param mWindowVO WindowVO
	 *  @return SQL SELECT String
	 */
	protected static String getSQL (MWindowVO mWindowVO)
	{
		//  View only returns IsActive='Y'
		String sql = "SELECT * FROM AD_Tab_v WHERE AD_Window_ID=?"
			+ " ORDER BY SeqNo";
		if (!Env.isBaseLanguage(mWindowVO.ctx, "AD_Window"))
			sql = "SELECT * FROM AD_Tab_vt WHERE AD_Window_ID=?"
				+ " AND AD_Language='" + Env.getAD_Language(mWindowVO.ctx) + "'"
				+ " ORDER BY SeqNo";
		return sql;
	}   //  getSQL

	/*************************************************************************/

	/**
	 *	Create MTabVO
	 *
	 *  @param wVO value object
	 *  @param TabNo tab no
	 *	@param rs ResultSet from AD_Tab_v
	 *	@param isRO true if window is r/o
	 *  @param onlyCurrentRows if true query is limited to not processed records
	 *  @return TabVO
	 */
	public static MTabVO create (MWindowVO wVO, int TabNo, ResultSet rs, boolean isRO, boolean onlyCurrentRows)
	{
		Log.trace(Log.l3_Util, "MTabVO.create #" + TabNo);

		MTabVO vo = new MTabVO ();
		vo.ctx = wVO.ctx;
		vo.WindowNo = wVO.WindowNo;
		vo.AD_Window_ID = wVO.AD_Window_ID;
		vo.TabNo = TabNo;
		//
		if (!loadTabDetails(vo, rs))
			return null;

		if (isRO)
		{
			Log.trace(Log.l6_Database, "MTabVO.create", "Tab is ReadOnly");
			vo.IsReadOnly = true;
		}
		vo.onlyCurrentRows = onlyCurrentRows;

		//  Create Fields
		if (vo.IsSortTab)
		{
			vo.Fields = new ArrayList();	//	dummy
		}
		else
		{
			createFields (vo);
			if (vo.Fields == null || vo.Fields.size() == 0)
			{
				Log.error("MTabVO.create - No Fields");
				return null;
			}
		}
		return vo;
	}	//	create

	/**
	 *	Create MTabVO
	 *
	 * 	@param ctx context
	 * 	@param WindowNo window
	 * 	@param AD_Table_ID table
	 *  @return TabVO
	  */
	public static MTabVO create(Properties ctx, int WindowNo, int AD_Table_ID)
	{
		Log.trace(Log.l3_Util, "MTabVO.create");

		MTabVO vo = new MTabVO ();
		vo.ctx = ctx;
		vo.WindowNo = WindowNo;
		vo.AD_Window_ID = 0;
		vo.TabNo = 0;
		//
		boolean resultOK = false;
		String sql = "SELECT * FROM AD_Tab_v WHERE AD_Table_ID=?";
		if (!Env.isBaseLanguage(ctx, "AD_Tab"))
			sql = "SELECT * FROM AD_Tab_vt WHERE AD_Table_ID=?"
				+ " AND AD_Language='" + Env.getAD_Language(ctx) + "'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, AD_Table_ID);
			ResultSet rs = pstmt.executeQuery();
			//
			if (rs.next())
				resultOK = loadTabDetails(vo, rs);
			else
				Log.trace(Log.l6_Database, "MTabVO.create", "No Tab for AD_Table_ID=" + AD_Table_ID);
			//
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("MTabVO.create(1)", e);
		}

		if (!resultOK)
			return null;

		//  Create Fields
		if (vo.IsSortTab)
		{
			vo.Fields = new ArrayList();	//	dummy
		}
		else
		{
			createFields (vo);
			if (vo.Fields == null || vo.Fields.size() == 0)
			{
				Log.error("MTabVO.create - No Fields");
				return null;
			}
		}
		return vo;
	}	//	create

	/**
	 * 	Load Tab Details from rs into vo
	 * 	@param vo Tab value object
	 *	@param rs ResultSet from AD_Tab_v/t
	 * 	@return true if read ok
	 */
	private static boolean loadTabDetails (MTabVO vo, ResultSet rs)
	{
		MRole role = MRole.getDefault(vo.ctx, false);
		try
		{
			vo.AD_Tab_ID = rs.getInt("AD_Tab_ID");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "AD_Tab_ID", String.valueOf(vo.AD_Tab_ID));
			vo.Name = rs.getString("Name");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "Name", vo.Name);

			//	Translation Tab
			boolean showTrl = Env.getContext(vo.ctx, "#ShowTrl").equals("Y") && Env.isMultiLingualDocument(vo.ctx);
			if (!showTrl && rs.getString("IsTranslationTab").equals("Y"))
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails", "NoTranslationTab - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo.Name);
				return false;
			}
			//	Accounting Info Tab
			boolean showAcct = Env.getContext(vo.ctx, "#ShowAcct").equals("Y");
			if (!showAcct && rs.getString("IsInfoTab").equals("Y"))
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails", "NoAcctTab - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo.Name);
				return false;
			}
			//	Access Level
			vo.AccessLevel = rs.getString("AccessLevel");
			if (!role.canView (vo.ctx, vo.AccessLevel))	//	No Access
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails", "No Role Access - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo. Name);
				return false;
			}	//	Used by MField.getDefault
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "AccessLevel", vo.AccessLevel);

			//	Table Access
			vo.AD_Table_ID = rs.getInt("AD_Table_ID");
			Env.setContext(vo.ctx, vo.WindowNo, vo.TabNo, "AD_Table_ID", String.valueOf(vo.AD_Table_ID));
			if (!role.isTableAccess(vo.AD_Table_ID, true))
			{
				Log.trace(Log.l5_DData, "MTabVO.loadTabDetails", "No Table Access - AD_Tab_ID=" + vo.AD_Tab_ID + " " + vo. Name);
				return false;
			}
			if (rs.getString("IsReadOnly").equals("Y"))
				vo.IsReadOnly = true;

			//
			vo.Description = rs.getString("Description");
			if (vo.Description == null)
				vo.Description = "";
			vo.Help = rs.getString("Help");
			if (vo.Help == null)
				vo.Help = "";

			if (rs.getString("IsSingleRow").equals("Y"))
				vo.IsSingleRow = true;
			if (rs.getString("HasTree").equals("Y"))
				vo.HasTree = true;

			vo.AD_Table_ID = rs.getInt("AD_Table_ID");
			vo.TableName = rs.getString("TableName");
			if (rs.getString("IsView").equals("Y"))
				vo.IsView = true;
			vo.AD_Column_ID = rs.getInt("AD_Column_ID");   //  Primary Parent Column

			if (rs.getString("IsSecurityEnabled").equals("Y"))
				vo.IsSecurityEnabled = true;
			if (rs.getString("IsDeleteable").equals("Y"))
				vo.IsDeleteable = true;
			if (rs.getString("IsHighVolume").equals("Y"))
				vo.IsHighVolume = true;

			vo.CommitWarning = rs.getString("CommitWarning");
			if (vo.CommitWarning == null)
				vo.CommitWarning = "";
			vo.WhereClause = rs.getString("WhereClause");
			if (vo.WhereClause == null)
				vo.WhereClause = "";
			vo.OrderByClause = rs.getString("OrderByClause");
			if (vo.OrderByClause == null)
				vo.OrderByClause = "";

			vo.AD_Process_ID = rs.getInt("AD_Process_ID");
			if (rs.wasNull())
				vo.AD_Process_ID = 0;
			vo.AD_Image_ID = rs.getInt("AD_Image_ID");
			if (rs.wasNull())
				vo.AD_Image_ID = 0;
			vo.Included_Tab_ID = rs.getInt("Included_Tab_ID");
			if (rs.wasNull())
				vo.Included_Tab_ID = 0;
			//
			vo.TabLevel = rs.getInt("TabLevel");
			if (rs.wasNull())
				vo.TabLevel = 0;
			//
			vo.IsSortTab = rs.getString("IsSortTab").equals("Y");
			if (vo.IsSortTab)
			{
				vo.AD_ColumnSortOrder_ID = rs.getInt("AD_ColumnSortOrder_ID");
				vo.AD_ColumnSortYesNo_ID = rs.getInt("AD_ColumnSortYesNo_ID");
			}
			//	Replication Type - set R/O if Reference
			try
			{
				int index = rs.findColumn ("ReplicationType");
				vo.ReplicationType = rs.getString (index);
				if ("R".equals(vo.ReplicationType))
					vo.IsReadOnly = true;
			}
			catch (Exception e)
			{
			}
		}
		catch (SQLException ex)
		{
			Log.error("MTabVO.loadTabDetails", ex);
			return false;
		}
		return true;
	}	//	loadTabDetails


	/*************************************************************************/

	/**
	 *  Create Tab Fields
	 *  @param mTabVO tab value object
	 *  @return true if fields were created
	 */
	private static boolean createFields (MTabVO mTabVO)
	{
		mTabVO.Fields = new ArrayList();

		String sql = MFieldVO.getSQL(mTabVO);
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, mTabVO.AD_Tab_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				MFieldVO voF = MFieldVO.create (mTabVO.ctx, mTabVO.WindowNo, mTabVO.TabNo, mTabVO.AD_Window_ID, mTabVO.IsReadOnly, rs);
				if (voF != null)
					mTabVO.Fields.add(voF);
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e)
		{
			Log.error("MTabVO.createFields", e);
			return false;
		}
		return mTabVO.Fields.size() != 0;
	}   //  createFields

}   //  MTabVO

