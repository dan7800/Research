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

import java.beans.*;
import java.io.*;
import java.math.*;
import java.sql.*;
import java.util.*;

import javax.swing.event.*;
import javax.swing.table.*;

import org.compiere.util.*;

/**
 *	Grid Table Model for JDBC access including buffering.
 *  <pre>
 *		The following data types are handeled
 *			Integer		for all IDs
 *			BigDecimal	for all Numbers
 *			Timestamp	for all Dates
 *			String		for all others
 *  The data is read via r/o resultset and cached in m_buffer. Writes/updates
 *  are via dynamically constructed SQL INSERT/UPDATE statements. The record
 *  is re-read via the resultset to get results of triggers.
 *
 *  </pre>
 *  The model maintains and fires the requires TableModelEvent changes,
 *  the DataChanged events (loading, changed, etc.)
 *  as well as Vetoable Change event "RowChange"
 *  (for row changes initiated by moving the row in the table grid).
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: MTable.java,v 1.68 2004/08/26 02:04:12 jjanke Exp $
 */
public final class MTable extends AbstractTableModel
	implements Serializable
{
	/**
	 *	JDBC Based Buffered Table
	 *
	 *  @param ctx Properties
	 *  @param TableName table name
	 *  @param WindowNo window no
	 *  @param TabNo tab no
	 *  @param withAccessControl    if true adds AD_Client/Org restrictuins
	 */
	public MTable(Properties ctx, int AD_Table_ID, String TableName, int WindowNo, int TabNo,
		boolean withAccessControl)
	{
		super();
		log.info(TableName);
		m_ctx = ctx;
		m_AD_Table_ID = AD_Table_ID;
		setTableName(TableName);
		m_WindowNo = WindowNo;
		m_TabNo = TabNo;
		m_withAccessControl = withAccessControl;
	}	//	MTable

	private Logger				log = Logger.getCLogger(getClass());
	private Properties          m_ctx;
	private int					m_AD_Table_ID;
	private String 		        m_tableName = "";
	private int				    m_WindowNo;
	private int				    m_TabNo;
	private boolean			    m_withAccessControl;
	private boolean			    m_readOnly = true;
	private boolean			    m_deleteable = true;
	//

	/**	Rowcount                    */
	private int				    m_rowCount = 0;
	/**	Has Data changed?           */
	private boolean			    m_changed = false;
	/** Index of changed row via SetValueAt */
	private int				    m_rowChanged = -1;
	/** Insert mode active          */
	private boolean			    m_inserting = false;
	/** Inserted Row number         */
	private int                 m_newRow = -1;
	/**	Is the Resultset open?      */
	private boolean			    m_open = false;
	/**	Compare to DB before save	*/
	private boolean				m_compareDB = true;		//	set to true after every save

	//	The buffer for all data
	private volatile ArrayList	m_buffer = new ArrayList(100);
	private volatile ArrayList	m_sort = new ArrayList(100);
	/** Original row data               */
	private Object[]			m_rowData = null;
	/** Original data [row,col,data]    */
	private Object[]            m_oldValue = null;
	//
	private Loader		        m_loader = null;

	/**	Columns                 		*/
	private ArrayList	        m_fields = new ArrayList(30);
	private ArrayList 	        m_parameterSELECT = new ArrayList(5);
	private ArrayList 	        m_parameterWHERE = new ArrayList(5);

	/** Complete SQL statement          */
	private String 		        m_SQL;
	/** SQL Statement for Row Count     */
	private String 		        m_SQL_Count;
	/** The SELECT clause with FROM     */
	private String 		        m_SQL_Select;
	/** The static where clause         */
	private String 		        m_whereClause = "";
	/** Show only Processed='N' and last 24h records    */
	private boolean		        m_onlyCurrentRows = false;
	/** Show only Not processed and x days				*/
	private int					m_onlyCurrentDays = 1;
	/** Static ORDER BY clause          */
	private String		        m_orderClause = "";

	/** Index of Key Column                 */
	private int			        m_indexKeyColumn = -1;
	/** Index of RowID column               */
	private int                 m_indexRowIDColumn = -1;
	/** Index of Color Column               */
	private int			        m_indexColorColumn = -1;
	/** Index of Processed Column           */
	private int                 m_indexProcessedColumn = -1;
	/** Index of IsActive Column            */
	private int                 m_indexActiveColumn = -1;
	/** Index of AD_Client_ID Column        */
	private int					m_indexClientColumn = -1;
	/** Index of AD_Org_ID Column           */
	private int					m_indexOrgColumn = -1;

	/** List of DataStatus Listeners    */
	private Vector 		        m_dataStatusListeners;
	/** Vetoable Change Bean support    */
	private VetoableChangeSupport   m_vetoableChangeSupport = new VetoableChangeSupport(this);
	/** Property of Vetoable Bean support "RowChange" */
	public static final String  PROPERTY = "MTable-RowSave";

	/**
	 *	Set Table Name
	 *  @param newTableName table name
	 */
	public void setTableName(String newTableName)
	{
		if (m_open)
		{
			log.error("setTableName - Table already open - ignored");
			return;
		}
		if (newTableName == null || newTableName.length() == 0)
			return;
		m_tableName = newTableName;
	}	//	setTableName

	/**
	 *	Get Table Name
	 *  @return table name
	 */
	public String getTableName()
	{
		return m_tableName;
	}	//	getTableName

	/**
	 *	Set Where Clause (w/o the WHERE and w/o History).
	 *  @param newWhereClause sql where clause
	 *  @param onlyCurrentRows only current rows
	 *  @param onlyCurrentDays how many days back for current
	 *	@return true if where clase set
	 */
	public boolean setWhereClause(String newWhereClause, boolean onlyCurrentRows, int onlyCurrentDays)
	{
		if (m_open)
		{
			log.error("setWhereClause - Table already open - ignored");
			return false;
		}
		//
		m_whereClause = newWhereClause;
		m_onlyCurrentRows = onlyCurrentRows;
		m_onlyCurrentDays = onlyCurrentDays;
		if (m_whereClause == null)
			m_whereClause = "";
		return true;
	}	//	setWhereClause

	/**
	 *	Get Where Clause (w/o the WHERE and w/o History)
	 *  @return where clause
	 */
	public String getWhereClause()
	{
		return m_whereClause;
	}	//	getWhereClause

	/**
	 *	Is History displayed
	 *  @return true if history displayed
	 */
	public boolean isOnlyCurrentRowsDisplayed()
	{
		return !m_onlyCurrentRows;
	}	//	isHistoryDisplayed

	/**
	 *	Set Order Clause (w/o the ORDER BY)
	 *  @param newOrderClause sql order by clause
	 */
	public void setOrderClause(String newOrderClause)
	{
		m_orderClause = newOrderClause;
		if (m_orderClause == null)
			m_orderClause = "";
	}	//	setOrderClause

	/**
	 *	Get Order Clause (w/o the ORDER BY)
	 *  @return order by clause
	 */
	public String getOrderClause()
	{
		return m_orderClause;
	}	//	getOrderClause

	/**
	 *	Assemble & store
	 *	m_SQL and m_countSQL
	 *  @return m_SQL
	 */
	private String createSelectSql()
	{
		if (m_fields.size() == 0 || m_tableName == null || m_tableName.equals(""))
			return "";

		//	Create SELECT Part
		StringBuffer select = new StringBuffer("SELECT ");
		for (int i = 0; i < m_fields.size(); i++)
		{
			if (i > 0)
				select.append(",");
			MField field = (MField)m_fields.get(i);
			select.append(field.getColumnName());
		}
		//
		select.append(" FROM ").append(m_tableName);
		m_SQL_Select = select.toString();
		m_SQL_Count = "SELECT COUNT(*) FROM " + m_tableName;
		//

		StringBuffer where = new StringBuffer("");
		//	WHERE
		if (m_whereClause.length() > 0)
		{
			where.append(" WHERE ");
			if (m_whereClause.indexOf("@") == -1)
				where.append(m_whereClause);
			else    //  replace variables
				where.append(Env.parseContext(m_ctx, m_WindowNo, m_whereClause, false));
		}
		if (m_onlyCurrentRows)
		{
			if (where.toString().indexOf(" WHERE ") == -1)
				where.append(" WHERE ");
			else
				where.append(" AND ");
			//	Show only unprocessed or the one updated within x days
			where.append("(Processed='N' OR Updated>SysDate-").append(m_onlyCurrentDays).append(")");
		}

		//	RO/RW Access
		m_SQL = m_SQL_Select + where.toString();
		m_SQL_Count += where.toString();
		if (m_withAccessControl)
		{
			boolean ro = MRole.SQL_RO;
		//	if (!m_readOnly)
		//		ro = MRole.SQL_RW;
			m_SQL = MRole.getDefault(m_ctx, false).addAccessSQL(m_SQL, 
				m_tableName, MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO);
			m_SQL_Count = MRole.getDefault(m_ctx, false).addAccessSQL(m_SQL_Count, 
				m_tableName, MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO);
		}

		//	ORDER BY
		if (!m_orderClause.equals(""))
			m_SQL += " ORDER BY " + m_orderClause;
		//
		log.debug("createSelectSql - " + m_SQL_Count);
		Env.setContext(m_ctx, m_WindowNo, m_TabNo, "SQL", m_SQL);
		return m_SQL;
	}	//	createSelectSql

	/**
	 *	Add Field to Table
	 *  @param field field
	 */
	public void addField (MField field)
	{
		log.debug ("addField (" + m_tableName + ") - " + field.getColumnName());
		if (m_open)
		{
			log.error("addField - Table already open - ignored: " + field.getColumnName());
			return;
		}
		if (!MRole.getDefault(m_ctx, false).isColumnAccess (m_AD_Table_ID, field.getAD_Column_ID(), true))
		{
			log.debug ("addField - No Column Access " + field.getColumnName());
			return;			
		}
		//  Set Index for RowID column
		if (field.getDisplayType() == DisplayType.RowID)
			m_indexRowIDColumn = m_fields.size();
		//  Set Index for Key column
		if (field.isKey())
			m_indexKeyColumn = m_fields.size();
		else if (field.getColumnName().equals("IsActive"))
			m_indexActiveColumn = m_fields.size();
		else if (field.getColumnName().equals("Processed"))
			m_indexProcessedColumn = m_fields.size();
		else if (field.getColumnName().equals("AD_Client_ID"))
			m_indexClientColumn = m_fields.size();
		else if (field.getColumnName().equals("AD_Org_ID"))
			m_indexOrgColumn = m_fields.size();
		//
		m_fields.add(field);
	}	//	addColumn

	/**
	 *  Returns database column name
	 *
	 *  @param index  the column being queried
	 *  @return column name
	 */
	public String getColumnName (int index)
	{
		if (index < 0 || index > m_fields.size())
		{
			log.error("getColumnName - invalid index=" + index);
			return "";
		}
		//
		MField field = (MField)m_fields.get(index);
		return field.getColumnName();
	}   //  getColumnName

	/**
	 * Returns a column given its name.
	 *
	 * @param columnName string containing name of column to be located
	 * @return the column index with <code>columnName</code>, or -1 if not found
	 */
	public int findColumn (String columnName)
	{
		for (int i = 0; i < m_fields.size(); i++)
		{
			MField field = (MField)m_fields.get(i);
			if (columnName.equals(field.getColumnName()))
				return i;
		}
		return -1;
	}   //  findColumn

	/**
	 *  Returns Class of database column/field
	 *
	 *  @param index  the column being queried
	 *  @return the class
	 */
	public Class getColumnClass (int index)
	{
		if (index < 0 || index >= m_fields.size())
		{
			log.error("getColumnClass - invalid index=" + index);
			return null;
		}
		MField field = (MField)m_fields.get(index);
		return DisplayType.getClass(field.getDisplayType(), false);
	}   //  getColumnClass

	/**
	 *	Set Select Clause Parameter.
	 *	Assumes that you set parameters starting from index zero
	 *  @param index index
	 *  @param parameter parameter
	 */
	public void setParameterSELECT (int index, Object parameter)
	{
		if (index >= m_parameterSELECT.size())
			m_parameterSELECT.add(parameter);
		else
			m_parameterSELECT.set(index, parameter);
	}	//	setParameterSELECT

	/**
	 *	Set Where Clause Parameter.
	 *	Assumes that you set parameters starting from index zero
	 *  @param index index
	 *  @param parameter parameter
	 */
	public void setParameterWHERE (int index, Object parameter)
	{
		if (index >= m_parameterWHERE.size())
			m_parameterWHERE.add(parameter);
		else
			m_parameterWHERE.set(index, parameter);
	}	//	setParameterWHERE


	/**
	 *	Get Column at index
	 *  @param index index
	 *  @return MField
	 */
	protected MField getField (int index)
	{
		if (index < 0 || index >= m_fields.size())
			return null;
		return (MField)m_fields.get(index);
	}	//	getColumn

	/**
	 *	Return Columns with Indentifier (ColumnName)
	 *  @param identifier column name
	 *  @return MField
	 */
	protected MField getField (String identifier)
	{
		if (identifier == null || identifier.length() == 0)
			return null;
		int cols = m_fields.size();
		for (int i = 0; i < cols; i++)
		{
			MField field = (MField)m_fields.get(i);
			if (identifier.equalsIgnoreCase(field.getColumnName()))
				return field;
		}
	//	log.error ("getField - not found: '" + identifier + "'");
		return null;
	}	//	getField

	/**
	 *  Get all Fields
	 *  @return MFields
	 */
	public MField[] getFields ()
	{
		MField[] retValue = new MField[m_fields.size()];
		m_fields.toArray(retValue);
		return retValue;
	}   //  getField

	
	/**************************************************************************
	 *	Open Database.
	 *  if already opened, data is refreshed
	 *
	 *	@return true if success
	 */
	public boolean open ()
	{
		log.info("open");
		if (m_open)
		{
			log.debug("open - already open");
			dataRefreshAll();
			return true;
		}

		//	create m_SQL and m_countSQL
		createSelectSql();
		if (m_SQL == null || m_SQL.equals(""))
		{
			log.error("open - No SQL");
			return false;
		}

		//	Start Loading
		m_loader = new Loader();
		m_rowCount = m_loader.open();
		m_buffer = new ArrayList(m_rowCount+10);
		m_sort = new ArrayList(m_rowCount+10);
		if (m_rowCount > 0)
			m_loader.start();
		else
			m_loader.close();
		m_open = true;
		//
		m_changed = false;
		m_rowChanged = -1;
		return true;
	}	//	open

	/**
	 *  Wait until async loader of Table and Lookup Fields is complete
	 *  Used for performance tests
	 */
	public void loadComplete()
	{
		//  Wait for loader
		if (m_loader != null)
		{
			if (m_loader.isAlive())
			{
				try
				{
					m_loader.join();
				}
				catch (InterruptedException ie)
				{
					log.error("loadComplete - join interrupted", ie);
				}
			}
		}
		//  wait for field lookup loaders
		for (int i = 0; i < m_fields.size(); i++)
		{
			MField field = (MField)m_fields.get(i);
			field.lookupLoadComplete();
		}
	}   //  loadComplete

	/**
	 *  Is Loading
	 *  @return true if loading
	 */
	public boolean isLoading()
	{
		if (m_loader != null && m_loader.isAlive())
			return true;
		return false;
	}   //  isLoading

	/**
	 *	Is it open?
	 *  @return true if opened
	 */
	public boolean isOpen()
	{
		return m_open;
	}	//	isOpen

	/**
	 *	Close Resultset
	 *  @param finalCall final call
	 */
	public void close (boolean finalCall)
	{
		if (!m_open)
			return;
		log.debug("close - final=" + finalCall);

		//  remove listeners
		if (finalCall)
		{
			m_dataStatusListeners.clear();
			EventListener evl[] = listenerList.getListeners(TableModelListener.class);
			for (int i = 0; i < evl.length; i++)
				listenerList.remove(TableModelListener.class, evl[i]);
			VetoableChangeListener vcl[] = m_vetoableChangeSupport.getVetoableChangeListeners();
			for (int i = 0; i < vcl.length; i++)
				m_vetoableChangeSupport.removeVetoableChangeListener(vcl[i]);
		}

		//	Stop loader
		while (m_loader != null && m_loader.isAlive())
		{
			log.debug("close - interrupting Loader");
			m_loader.interrupt();
			try
			{
				Thread.sleep(200);		//	.2 second
			}
			catch (InterruptedException ie)
			{}
		}

		if (!m_inserting)
			dataSave(true);

		if (m_buffer != null)
			m_buffer.clear();
		m_buffer = null;
		if (m_sort != null)
			m_sort.clear();
		m_sort = null;

		if (finalCall)
			dispose();

		//  Fields are disposed from MTab
		log.debug("close - complete");
		m_open = false;
	}	//	close

	/**
	 *  Dispose MTable.
	 *  Called by close-final
	 */
	private void dispose()
	{
		//  MFields
		for (int i = 0; i < m_fields.size(); i++)
			((MField)m_fields.get(i)).dispose();
		m_fields.clear();
		m_fields = null;
		//
		m_dataStatusListeners = null;
		m_vetoableChangeSupport = null;
		//
		m_parameterSELECT.clear();
		m_parameterSELECT = null;
		m_parameterWHERE.clear();
		m_parameterWHERE = null;
		//  clear data arrays
		m_buffer = null;
		m_sort = null;
		m_rowData = null;
		m_oldValue = null;
		m_loader = null;
	}   //  dispose

	/**
	 *	Get total database column count (displayed and not displayed)
	 *  @return column count
	 */
	public int getColumnCount()
	{
		return m_fields.size();
	}	//	getColumnCount

	/**
	 *	Get (displayed) field count
	 *  @return field count
	 */
	public int getFieldCount()
	{
		return m_fields.size();
	}	//	getFieldCount

	/**
	 *  Return number of rows
	 *  @return Number of rows or 0 if not opened
	 */
	public int getRowCount()
	{
		return m_rowCount;
	}	//	getRowCount

	/**
	 *	Set the Column to determine the color of the row
	 *  @param columnName column name
	 */
	public void setColorColumn (String columnName)
	{
		m_indexColorColumn = findColumn(columnName);
	}	//  setColorColumn

	/**
	 *	Get ColorCode for Row.
	 *  <pre>
	 *	If numerical value in compare column is
	 *		negative = -1,
	 *      positive = 1,
	 *      otherwise = 0
	 *  </pre>
	 *  @see #setColorColumn
	 *  @param row row
	 *  @return color code
	 */
	public int getColorCode (int row)
	{
		if (m_indexColorColumn  == -1)
			return 0;
		Object data = getValueAt(row, m_indexColorColumn);
		//	We need to have a Number
		if (data == null || !(data instanceof BigDecimal))
			return 0;
		int cmp = Env.ZERO.compareTo(data);
		if (cmp > 0)
			return -1;
		if (cmp < 0)
			return 1;
		return 0;
	}	//	getColorCode


	/**
	 *	Sort Entries by Column.
	 *  actually the rows are not sorted, just the access pointer ArrayList
	 *  with the same size as m_buffer with MSort entities
	 *  @param col col
	 *  @param ascending ascending
	 */
	public void sort (int col, boolean ascending)
	{
		log.info("sort #" + col + " " + ascending);
		if (getRowCount() == 0)
			return;
		MField field = getField (col);
		//	RowIDs are not sorted
		if (field.getDisplayType() == DisplayType.RowID)
			return;
		boolean isLookup = DisplayType.isLookup(field.getDisplayType());

		//	fill MSort entities with data entity
		for (int i = 0; i < m_sort.size(); i++)
		{
			MSort sort = (MSort)m_sort.get(i);
			Object[] rowData = (Object[])m_buffer.get(sort.index);
			if (isLookup)
				sort.data = field.getLookup().getDisplay(rowData[col]);	//	lookup
			else
				sort.data = rowData[col];								//	data
		}

		//	sort it
		MSort sort = new MSort(0, null);
		sort.setSortAsc(ascending);
		Collections.sort(m_sort, sort);
		//	update UI
		fireTableDataChanged();
		//  Info detected by MTab.dataStatusChanged and current row set to 0
		fireDataStatusIEvent("Sorted");
	}	//	sort

	/**
	 *	Get Key ID or -1 of none
	 *  @param row row
	 *  @return ID or -1
	 */
	public int getKeyID (int row)
	{
	//	Log.info("MTable.getKeyID - row=" + row + ", keyColIdx=" + m_indexKeyColumn);
		if (m_indexKeyColumn != -1)
		{
			try
			{
				Integer ii = (Integer)getValueAt(row, m_indexKeyColumn);
				if (ii == null)
					return -1;
				return ii.intValue();
			}
			catch (Exception e)     //  Alpha Key
			{
				return -1;
			}
		}
		return -1;
	}	//	getKeyID

	/**
	 *	Get Key ColumnName
	 *  @return key column name
	 */
	public String getKeyColumnName()
	{
		if (m_indexKeyColumn != -1)
			return getColumnName(m_indexKeyColumn);
		return "";
	}	//	getKeyColumnName

	/**
	 *	Get Selected ROWID or null, if no RowID exists
	 *  @param row row
	 *  @return ROWID
	 */
	public Object getRowID (int row)
	{
		Object[] rid = getRID(row);
		if (rid == null)
			return null;
		return rid[0];
	}	//	getSelectedRowID

	/**
	 *	Get RowID Structure [0]=RowID, [1]=Selected, [2]=ID.
	 *  <p>
	 *  Either RowID or ID is populated (views don't have RowID)
	 *  @param row row
	 *  @return RowID
	 */
	public Object[] getRID (int row)
	{
		if (m_indexRowIDColumn == -1 || row < 0 || row >= getRowCount())
			return null;
		return (Object[])getValueAt(row, m_indexRowIDColumn);
	}	//	getRID

	/**
	 *	Find Row with RowID
	 *  @param RowID row id or oid
	 *	@return number of row or 0 if not found
	 */
	public int getRow (Object RowID)
	{
		if (RowID == null)
			return 0;

		//	the value to find
		String find = RowID.toString();

		//	Wait a bit to load rows
		if (m_loader != null && m_loader.isAlive())
		{
			try
			{
				Thread.sleep(250);		//	1/4 second
			}
			catch (InterruptedException ie)
			{}
		}

		//	Build search vector
		int size = m_sort.size();		//	may still change
		ArrayList search = new ArrayList(size);
		for (int i = 0; i < size; i++)
		{
			Object[] r = (Object[])getValueAt(i, 0);
			String s = r[0].toString();
			MSort so = new MSort(i, s);
			search.add(so);
		}

		//	Sort it
		MSort sort = new MSort(0, null);
		Collections.sort(search, sort);

		//	Find it
		int index = Collections.binarySearch(search, find, sort);
		if (index < 0)	//	not found
		{
			search.clear();
			return 0;
		}
		//	Get Info
		MSort result = (MSort)search.get(index);
		//	Clean up
		search.clear();
		return result.index;
	}	//	getRow


	/**************************************************************************
	 * 	Get Value in Resultset
	 *  @param row row
	 *  @param col col
	 *  @return Object of that row/column
	 */
	public Object getValueAt (int row, int col)
	{
	//	Log.trace(Log.l4_Data, "MTable.getValueAt r=" + row + " c=" + col);
		if (!m_open || row < 0 || col < 0 || row >= m_rowCount)
		{
		//	Log.trace(Log.l5_DData, "Out of bounds - Open=" + m_open + ", RowCount=" + m_rowCount);
			return null;
		}

		//	need to wait for data read into buffer
		int loops = 0;
		while (row >= m_buffer.size() && m_loader.isAlive() && loops < 15)
		{
			log.debug("getValueAt - waiting for loader row=" + row + ", size=" + m_buffer.size());
			try
			{
				Thread.sleep(500);		//	1/2 second
			}
			catch (InterruptedException ie)
			{}
			loops++;
		}

		//	empty buffer
		if (row >= m_buffer.size())
		{
		//	Log.trace(Log.l5_DData, "Empty buffer");
			return null;
		}

		//	return Data item
		MSort sort = (MSort)m_sort.get(row);
		Object[] rowData = (Object[])m_buffer.get(sort.index);
		//	out of bounds
		if (rowData == null || col > rowData.length)
		{
		//	Log.trace(Log.l5_DData, "No data or Column out of bounds");
			return null;
		}
		return rowData[col];
	}	//	getValueAt

	/**
	 *	Indicate that there will be a change
	 *  @param changed changed
	 */
	public void setChanged (boolean changed)
	{
		//	Can we edit?
		if (!m_open || m_readOnly)
			return;

		//	Indicate Change
		m_changed = changed;
		if (!changed)
			m_rowChanged = -1;
		fireDataStatusIEvent("");
	}	//	setChanged

	/**
	 * 	Set Value in data and update MField.
	 *  (called directly or from JTable.editingStopped())
	 *
	 *  @param  value value to assign to cell
	 *  @param  row row index of cell
	 *  @param  col column index of cell
	 */
	public final void setValueAt (Object value, int row, int col)
	{
		setValueAt (value, row, col, false);
	}	//	setValueAt

	/**
	 * 	Set Value in data and update MField.
	 *  (called directly or from JTable.editingStopped())
	 *
	 *  @param  value value to assign to cell
	 *  @param  row row index of cell
	 *  @param  col column index of cell
	 * 	@param	force force setting new value
	 */
	public final void setValueAt (Object value, int row, int col, boolean force)
	{
		//	Can we edit?
		if (!m_open || m_readOnly       //  not accessible
				|| row < 0 || col < 0   //  invalid index
				|| col == 0             //  cannot change ID
				|| m_rowCount == 0)     //  no rows
			return;

		dataSave(row, false);

		//	Has anything changed?
		Object oldValue = getValueAt(row, col);
		if (!force && (
			(oldValue == null && value == null)
			||	(oldValue != null && oldValue.equals(value))
			||	(oldValue != null && value != null && oldValue.toString().equals(value.toString()))
			))
			return;

		log.debug("setValueAt r=" + row + " c=" + col + " = " + value + " (" + oldValue + ")");

		//  Save old value
		m_oldValue = new Object[3];
		m_oldValue[0] = new Integer(row);
		m_oldValue[1] = new Integer(col);
		m_oldValue[2] = oldValue;

		//	Set Data item
		MSort sort = (MSort)m_sort.get(row);
		Object[] rowData = (Object[])m_buffer.get(sort.index);
		m_rowChanged = row;

		//	Selection
		if (col == 0)
		{
			rowData[col] = value;
			m_buffer.set(sort.index, rowData);
			return;
		}

		//	save original value - shallow copy
		if (m_rowData == null)
		{
			int size = m_fields.size();
			m_rowData = new Object[size];
			for (int i = 0; i < size; i++)
				m_rowData[i] = rowData[i];
		}

		//	save & update
		rowData[col] = value;
		m_buffer.set(sort.index, rowData);
		//  update Table
		fireTableCellUpdated(row, col);
		//  update MField
		MField field = getField(col);
		field.setValue(value, m_inserting);
		//  inform
		DataStatusEvent evt = createDSE();
		evt.setChangedColumn(col);
		fireDataStatusChanged(evt);
	}	//	setValueAt

	/**
	 *  Get Old Value
	 *  @param row row
	 *  @param col col
	 *  @return old value
	 */
	public Object getOldValue (int row, int col)
	{
		if (m_oldValue == null)
			return null;
		if (((Integer)m_oldValue[0]).intValue() == row
				&& ((Integer)m_oldValue[1]).intValue() == col)
			return m_oldValue[2];
		return null;
	}   // getOldValue

	/**
	 *	Check if the current row needs to be saved.
	 *  @param  onlyRealChange if true the value of a field was actually changed
	 *  (e.g. for new records, which have not been changed) - default false
	 *	@return true it needs to be saved
	 */
	public boolean needSave(boolean onlyRealChange)
	{
		return needSave(m_rowChanged, onlyRealChange);
	}   //  needSave

	/**
	 *	Check if the row needs to be saved.
	 *  - only if nothing was changed
	 *	@return true it needs to be saved
	 */
	public boolean needSave()
	{
		return needSave(m_rowChanged, false);
	}   //  needSave

	/**
	 *	Check if the row needs to be saved.
	 *  - only when row changed
	 *  - only if nothing was changed
	 *	@param	newRow to check
	 *	@return true it needs to be saved
	 */
	public boolean needSave(int newRow)
	{
		return needSave(newRow, false);
	}   //  needSave

	/**
	 *	Check if the row needs to be saved.
	 *  - only when row changed
	 *  - only if nothing was changed
	 *	@param	newRow to check
	 *  @param  onlyRealChange if true the value of a field was actually changed
	 *  (e.g. for new records, which have not been changed) - default false
	 *	@return true it needs to be saved
	 */
	public boolean needSave(int newRow, boolean onlyRealChange)
	{
		log.debug("needSave - Row=" + newRow +
			", Changed=" + m_rowChanged + "/" + m_changed);  //  m_rowChanged set in setValueAt
		//  nothing done
		if (!m_changed && m_rowChanged == -1)
			return false;
		//  E.g. New unchanged records
		if (m_changed && m_rowChanged == -1 && onlyRealChange)
			return false;
		//  same row
		if (newRow == m_rowChanged)
			return false;

		return true;
	}	//	needSave

	/*************************************************************************/

	public static final char	SAVE_OK = 'O';			//	the only OK condition
	public static final char	SAVE_ERROR = 'E';
	public static final char	SAVE_ACCESS = 'A';
	public static final char	SAVE_MANDATORY = 'M';
	public static final char	SAVE_ABORT = 'U';

	/**
	 *	Check if it needs to be saved and save it.
	 *  @param newRow row
	 *  @param manualCmd manual command to save
	 *	@return true if not needed to be saved or successful saved
	 */
	public boolean dataSave (int newRow, boolean manualCmd)
	{
		log.debug("dataSave - Row=" + newRow +
			", Changed=" + m_rowChanged + "/" + m_changed);  //  m_rowChanged set in setValueAt
		//  nothing done
		if (!m_changed && m_rowChanged == -1)
			return true;
		//  same row, don't save yet
		if (newRow == m_rowChanged)
			return true;

		return (dataSave(manualCmd) == SAVE_OK);
	}   //  dataSave

	/**
	 *	Save unconditional.
	 *  @param manualCmd if true, no vetoable PropertyChange will be fired for save confirmation
	 *	@return OK Or Error condition
	 *  Error info (Access*, FillMandatory, SaveErrorNotUnique,
	 *  SaveErrorRowNotFound, SaveErrorDataChanged) is saved in the log
	 */
	public char dataSave (boolean manualCmd)
	{
		//	cannot save
		if (!m_open)
		{
			log.warn ("dataSave - Error - Open=" + m_open);
			return SAVE_ERROR;
		}
		//	no need - not changed - row not positioned - no Value changed
		if (m_rowChanged == -1)
		{
			log.info("dataSave - NoNeed - Changed=" + m_changed + ", Row=" + m_rowChanged);
		//	return SAVE_ERROR;
			if (!manualCmd)
				return SAVE_OK;
		}
		//  Value not changed
		if (m_rowData == null)
		{
			log.warn ("dataSave - Error - DataNull=" + (m_rowData == null));
			return SAVE_ERROR;
		}

		if (m_readOnly)
		//	If Processed - not editable (Find always editable)  -> ok for changing payment terms, etc.
		{
			log.warn("dataSave - IsReadOnly - ignored");
			dataIgnore();
			return SAVE_ACCESS;
		}

		//	row not positioned - no Value changed
		if (m_rowChanged == -1)
		{
			if (m_newRow != -1)     //  new row and nothing changed - might be OK
				m_rowChanged = m_newRow;
			else
			{
				fireDataStatusEEvent("SaveErrorNoChange", "");
				return SAVE_ERROR;
			}
		}

		//	Can we change?
		int[] co = getClientOrg(m_rowChanged);
		int AD_Client_ID = co[0]; 
		int AD_Org_ID = co[1];
		if (!MRole.getDefault(m_ctx, false).canUpdate(AD_Client_ID, AD_Org_ID, m_AD_Table_ID, true))
		{
			fireDataStatusEEvent(Log.retrieveError());
			dataIgnore();
			return SAVE_ACCESS;
		}

		log.info("dataSave - Saving row " + m_rowChanged);

		//  inform about data save action, if not manually initiated
		try
		{
			if (!manualCmd)
				m_vetoableChangeSupport.fireVetoableChange(PROPERTY, 0, m_rowChanged);
		}
		catch (PropertyVetoException pve)
		{
			log.warn("dataSave - " + pve.getMessage());
			dataIgnore();
			return SAVE_ABORT;
		}

		//	get updated row data
		MSort sort = (MSort)m_sort.get(m_rowChanged);
		Object[] rowData = (Object[])m_buffer.get(sort.index);

		//	Check Mandatory
		String missingColumns = getMandatory(rowData);
		if (missingColumns.length() != 0)
		{
			fireDataStatusEEvent("FillMandatory", missingColumns + "\n");
			return SAVE_MANDATORY;
		}

		/**
		 *	Update row *****
		 */
		int Record_ID = 0;
		if (!m_inserting)
			Record_ID = getKeyID(m_rowChanged);
		try
		{
			if (!m_tableName.endsWith("_Trl"))	//	translation tables have no model
				return dataSavePO (Record_ID);
		}
		catch (Exception e)
		{
			if (e instanceof IllegalStateException)
				log.error("MTable.dataSave - " + m_tableName + " - " + e.getLocalizedMessage());
			else
			{
				log.error("MTable.dataSave - Persistency Issue - " + m_tableName, e);
				return SAVE_ERROR;
			}
		}
		
		
		boolean error = false;
		lobReset();
		//
		String is = null;
		final String ERROR = "ERROR: ";
		final String INFO  = "Info: ";

		//	SQL with specific where clause
		String SQL = m_SQL_Select;
		StringBuffer refreshSQL = new StringBuffer(SQL).append(" WHERE ");	//	to be completed when key known
		StringBuffer singleRowWHERE = new StringBuffer();
		StringBuffer multiRowWHERE = new StringBuffer();
		//	Create SQL	& RowID
		Object rowID = null;
		if (m_inserting)
		{
			SQL += " WHERE 1=2";
		}
		else
		{
			//  FOR UPDATE causes  -  ORA-01002 fetch out of sequence
			SQL += " WHERE ROWID=?";
			rowID = getRowID (m_rowChanged);
		}
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			if (!m_inserting)
				DB.getDatabase().setRowID(pstmt, 1, rowID);
			ResultSet rs = pstmt.executeQuery();
			//	only one row
			if (!(m_inserting || rs.next()))
			{
				rs.close();
				pstmt.close();
				fireDataStatusEEvent("SaveErrorRowNotFound", "");
				dataRefresh(m_rowChanged);
				return SAVE_ERROR;
			}

			Object[] rowDataDB = null;
			//	Prepare
			boolean manualUpdate = ResultSet.CONCUR_READ_ONLY == rs.getConcurrency();
			if (manualUpdate)
				createUpdateSqlReset();
			if (m_inserting)
			{
				if (manualUpdate)
					log.debug("dataSave - prepare inserting ... manual");
				else
				{
					log.debug ("dataSave - prepare inserting ... RowSet");
					rs.moveToInsertRow ();
				}
			}
			else
			{
				log.debug("dataSave - prepare updating ... manual=" + manualUpdate);
				//	get current Data in DB
				rowDataDB = readData (rs);
			}

			/**	Data:
			 *		m_rowData	= original Data
			 *		rowData 	= updated Data
			 *		rowDataDB	= current Data in DB
			 *	1) Difference between original & updated Data?	N:next
			 *	2) Difference between original & current Data?	Y:don't update
			 *	3) Update current Data
			 *	4) Refresh to get last Data (changed by trigger, ...)
			 */

			//	Constants for Created/Updated(By)
			Timestamp now = new Timestamp(System.currentTimeMillis());
			int user = Env.getContextAsInt(m_ctx, "#AD_User_ID");

			/**
			 *	for every column
			 */
			int size = m_fields.size();
			for (int col = 0; col < size; col++)
			{
				MField field = (MField)m_fields.get (col);
				String columnName = field.getColumnName ();
			//	log.debug ("dataSave - " + columnName + "= " + m_rowData[col] + " <> DB: " + rowDataDB[col] + " -> " + rowData[col]);

				//	RowID
				if (field.getDisplayType () == DisplayType.RowID)
					; //	ignore

				//	New Key
				else if (field.isKey () && m_inserting)
				{
					if (columnName.endsWith ("_ID") || columnName.toUpperCase().endsWith ("_ID"))
					{
						int insertID = DB.getNextID (m_ctx, m_tableName, null);	//	no trx
						if (manualUpdate)
							createUpdateSql (columnName, String.valueOf (insertID));
						else
							rs.updateInt (col + 1, insertID); 						// ***
						singleRowWHERE.append (columnName).append ("=").append (insertID);
						//
						is = INFO + columnName + " -> " + insertID + " (Key)";
					}
					else //	Key with String value
					{
						String str = rowData[col].toString ();
						if (manualUpdate)
							createUpdateSql (columnName, DB.TO_STRING (str));
						else
							rs.updateString (col + 1, str); 						// ***
						singleRowWHERE = new StringBuffer();	//	overwrite
						singleRowWHERE.append (columnName).append ("=").append (DB.TO_STRING(str));
						//
						is = INFO + columnName + " -> " + str + " (StringKey)";
					}
					log.debug ("dataSave - " + is);
				} //	New Key

				//	New DocumentNo
				else if (columnName.equals ("DocumentNo"))
				{
					boolean newDocNo = false;
					String docNo = (String)rowData[col];
					//  we need to have a doc number
					if (docNo == null || docNo.length () == 0)
						newDocNo = true;
						//  Preliminary ID from CalloutSystem
					else if (docNo.startsWith ("<") && docNo.endsWith (">"))
						newDocNo = true;

					if (newDocNo || m_inserting)
					{
						String insertDoc = null;
						//  always overwrite if insering with mandatory DocType DocNo
						if (m_inserting)
							insertDoc = DB.getDocumentNo (m_ctx, m_WindowNo, 
								m_tableName, true, null);	//	only doc type - no trx
						log.debug ("dataSave - DocumentNo entered=" + docNo + ", DocTypeInsert=" + insertDoc + ", newDocNo=" + newDocNo);
						// can we use entered DocNo?
						if (insertDoc == null || insertDoc.length () == 0)
						{
							if (!newDocNo && docNo != null && docNo.length () > 0)
								insertDoc = docNo;
							else //  get a number from DocType or Table
								insertDoc = DB.getDocumentNo (m_ctx, m_WindowNo, 
									m_tableName, false, null);	//	no trx
						}
						//	There might not be an automatic document no for this document
						if (insertDoc == null || insertDoc.length () == 0)
						{
							//  in case DB function did not return a value
							if (docNo != null && docNo.length () != 0)
								insertDoc = (String)rowData[col];
							else
							{
								error = true;
								is = ERROR + field.getColumnName () + "= " + rowData[col] + " NO DocumentNo";
								log.debug ("dataSave - " + is);
								break;
							}
						}
						//
						if (manualUpdate)
							createUpdateSql (columnName, DB.TO_STRING (insertDoc));
						else
							rs.updateString (col + 1, insertDoc);					//	***
							//
						is = INFO + columnName + " -> " + insertDoc + " (DocNo)";
						log.debug ("dataSave - " + is);
					}
				}	//	New DocumentNo

				//  New Value(key)
				else if (columnName.equals ("Value") && m_inserting)
				{
					String value = (String)rowData[col];
					//  Get from Sequence, if not entered
					if (value == null || value.length () == 0)
					{
						value = DB.getDocumentNo (m_ctx, m_WindowNo, m_tableName, false, null);
						//  No Value
						if (value == null || value.length () == 0)
						{
							error = true;
							is = ERROR + field.getColumnName () + "= " + rowData[col]
								 + " No Value";
							log.debug ("dataSave - " + is);
							break;
						}
					}
					if (manualUpdate)
						createUpdateSql (columnName, DB.TO_STRING (value));
					else
						rs.updateString (col + 1, value); 							//	***
						//
					is = INFO + columnName + " -> " + value + " (Value)";
					log.debug ("dataSave - " + is);
				}	//	New Value(key)

				//	Updated		- check database
				else if (columnName.equals ("Updated"))
				{
					if (m_compareDB && !m_inserting && !m_rowData[col].equals (rowDataDB[col]))	//	changed
					{
						error = true;
						is = ERROR + field.getColumnName () + "= " + m_rowData[col]
							 + " != DB: " + rowDataDB[col];
						log.debug ("dataSave - " + is);
						break;
					}
					if (manualUpdate)
						createUpdateSql (columnName, DB.TO_DATE (now, false));
					else
						rs.updateTimestamp (col + 1, now); 							//	***
						//
					is = INFO + "Updated/By -> " + now + " - " + user;
					log.debug ("dataSave - " + is);
				} //	Updated

				//	UpdatedBy	- update
				else if (columnName.equals ("UpdatedBy"))
				{
					if (manualUpdate)
						createUpdateSql (columnName, String.valueOf (user));
					else
						rs.updateInt (col + 1, user); 								//	***
				} //	UpdatedBy

				//	Created
				else if (m_inserting && columnName.equals ("Created"))
				{
					if (manualUpdate)
						createUpdateSql (columnName, DB.TO_DATE (now, false));
					else
						rs.updateTimestamp (col + 1, now); 							//	***
				} //	Created

				//	CreatedBy
				else if (m_inserting && columnName.equals ("CreatedBy"))
				{
					if (manualUpdate)
						createUpdateSql (columnName, String.valueOf (user));
					else
						rs.updateInt (col + 1, user); 								//	***
				} //	CreatedBy

				//	Nothing changed & null
				else if (m_rowData[col] == null && rowData[col] == null)
				{
					if (m_inserting)
					{
						if (manualUpdate)
							createUpdateSql (columnName, "NULL");
						else
							rs.updateNull (col + 1); 								//	***
						is = INFO + columnName + "= NULL";
						log.debug ("dataSave - " + is);
					}
				}

				//	***	Data changed ***
				else if (m_inserting
				  || (m_rowData[col] == null && rowData[col] != null)
				  || (m_rowData[col] != null && rowData[col] == null)
				  || !m_rowData[col].equals (rowData[col])) 			//	changed
				{
					//	Original == DB
					if (m_inserting || !m_compareDB
					  || (m_rowData[col] == null && rowDataDB[col] == null)
					  || (m_rowData[col] != null && m_rowData[col].equals (rowDataDB[col])))
					{
						if (Log.isTraceLevel(10))
							log.debug("dataSave: " + columnName + "=" + rowData[col]
								+ " " + (rowData[col]==null ? "" : rowData[col].getClass().getName()));
						//
						String type = "String";
						if (rowData[col] == null)
						{
							if (manualUpdate)
								createUpdateSql (columnName, "NULL");
							else
								rs.updateNull (col + 1); 							//	***
						}
						
						//	ID - int
						else if (DisplayType.isID (field.getDisplayType()) 
							|| field.getDisplayType() == DisplayType.Integer)
						{
							int number = 0;
							try
							{
								number = Integer.parseInt (rowData[col].toString ());
								if (manualUpdate)
									createUpdateSql (columnName, String.valueOf (number));
								else
									rs.updateInt (col + 1, number); 			// 	***
							}
							catch (Exception e) //  could also be a String (AD_Language, AD_Message)
							{
								if (manualUpdate)
									createUpdateSql (columnName, DB.TO_STRING (rowData[col].toString ()));
								else
									rs.updateString (col + 1, rowData[col].toString ()); //	***
							}
							type = "Int";
						}
						//	Numeric - BigDecimal
						else if (DisplayType.isNumeric (field.getDisplayType ()))
						{
							if (manualUpdate)
								createUpdateSql (columnName, rowData[col].toString ());
							else
								rs.updateBigDecimal (col + 1, (BigDecimal)rowData[col]); //	***
							type = "Number";
						}
						//	Date - Timestamp
						else if (DisplayType.isDate (field.getDisplayType ()))
						{
							if (manualUpdate)
								createUpdateSql (columnName, DB.TO_DATE ((Timestamp)rowData[col], false));
							else
								rs.updateTimestamp (col + 1, (Timestamp)rowData[col]); //	***
							type = "Date";
						}
						//	LOB
						else if (field.getDisplayType() == DisplayType.TextLong)
						{
							PO_LOB lob = new PO_LOB (getTableName(), columnName, 
								null, null, field.getDisplayType(), rowData[col]);
							lobAdd(lob);
							type = "CLOB";
						}
						//	Boolean
						else if (field.getDisplayType() == DisplayType.YesNo)
						{
							String yn = null;
							if (rowData[col] instanceof Boolean)
							{
								Boolean bb = (Boolean)rowData[col];
								yn = bb.booleanValue() ? "Y" : "N";
							}
							else
								yn = "Y".equals(rowData[col]) ? "Y" : "N"; 
							if (manualUpdate)
								createUpdateSql (columnName, DB.TO_STRING (yn));
							else
								rs.updateString (col + 1, yn); //	***
						}
						//	String and others
						else	
						{
							if (manualUpdate)
								createUpdateSql (columnName, DB.TO_STRING (rowData[col].toString ()));
							else
								rs.updateString (col + 1, rowData[col].toString ()); //	***
						}
						//
						is = INFO + columnName + "= " + m_rowData[col]
							 + " -> " + rowData[col] + " (" + type + ")";
						log.debug ("dataSave - " + is);
					}
					//	Original != DB
					else
					{
						error = true;
						is = ERROR + field.getColumnName () + "= " + m_rowData[col]
							 + " != DB: " + rowDataDB[col] + " -> " + rowData[col];
						log.debug ("dataSave - " + is);
					}
				}	//	Data changed

				//	Single Key - retrieval sql
				if (field.isKey() && !m_inserting)
				{
					if (rowData[col] == null)
						throw new RuntimeException("dataSave - Key " + columnName + " is NULL");
					if (columnName.endsWith ("_ID"))
						singleRowWHERE.append (columnName).append ("=").append (rowData[col]);
					else
					{
						singleRowWHERE = new StringBuffer();	//	overwrite
						singleRowWHERE.append (columnName).append ("=").append (DB.TO_STRING(rowData[col].toString()));
					}
				}
				//	MultiKey Inserting - retrieval sql
				if (field.isParent())
				{
					if (rowData[col] == null)
						throw new RuntimeException("dataSave - MultiKey Parent " + columnName + " is NULL");
					if (multiRowWHERE.length() != 0)
						multiRowWHERE.append(" AND ");
					if (columnName.endsWith ("_ID"))
						multiRowWHERE.append (columnName).append ("=").append (rowData[col]);
					else
						multiRowWHERE.append (columnName).append ("=").append (DB.TO_STRING(rowData[col].toString()));
				}
			}	//	for every column

			if (error)
			{
				if (manualUpdate)
					createUpdateSqlReset();
				else
					rs.cancelRowUpdates();
				rs.close();
				pstmt.close();
				fireDataStatusEEvent("SaveErrorDataChanged", "");
				dataRefresh(m_rowChanged);
				return SAVE_ERROR;
			}

			/**
			 *	Save to Database
			 */
			//
			String whereClause = singleRowWHERE.toString();
			if (whereClause.length() == 0)
				whereClause = multiRowWHERE.toString();
			if (m_inserting)
			{
				log.debug("dataSave - inserting ...");
				if (manualUpdate)
				{
					String sql = createUpdateSql(true, null);
					int no = DB.executeUpdateEx (sql, null);	//	no Trx
					if (no != 1)
						log.error("dataSave - insert #=" + no + " - " + sql);
				}
				else
					rs.insertRow();
			}
			else
			{
				log.debug("dataSave - updating ... " + whereClause);
				if (manualUpdate)
				{
					String sql = createUpdateSql(false, whereClause);
					int no = DB.executeUpdateEx (sql, null);	//	no Trx
					if (no != 1)
						log.error("dataSave - update #=" + no + " - " + sql);
				}
				else
					rs.updateRow();
			}

			log.debug("dataSave - committing ...");
			DB.commit(true, null);	//	no Trx
			//
			lobSave(whereClause);
			//	data may be updated by trigger after update
			if (m_inserting || manualUpdate)
			{
				rs.close();
				pstmt.close();
				//	need to re-read row to get ROWID, Key, DocumentNo
				log.debug("dataSave - reading ... " + whereClause);
				refreshSQL.append(whereClause);
				pstmt = DB.prepareStatement(refreshSQL.toString());
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					rowDataDB = readData(rs);
					//	update buffer
					m_buffer.set(sort.index, rowDataDB);
					fireTableRowsUpdated(m_rowChanged, m_rowChanged);
				}
				else
					log.error("dataSave - inserted row not found");
			}
			else
			{
				log.debug("dataSave - refreshing ...");
				rs.refreshRow();	//	only use
				rowDataDB = readData(rs);
				//	update buffer
				m_buffer.set(sort.index, rowDataDB);
				fireTableRowsUpdated(m_rowChanged, m_rowChanged);
			}
			//
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException e)
		{
			try
			{
				if (pstmt != null)
				  pstmt.close ();
				pstmt = null;
			}
			catch (Exception ex)
			{
			}

			String msg = "SaveError";
			if (e.getErrorCode() == 1)		//	Unique Constraint
			{
				log.error ("dataSave - Key Not Unique", e);
				msg = "SaveErrorNotUnique";
			}
			else
				log.error ("dataSave\nSQL= " + SQL, e);
			fireDataStatusEEvent(msg, e.getLocalizedMessage());
			return SAVE_ERROR;
		}

		//	everything ok
		m_rowData = null;
		m_changed = false;
		m_compareDB = true;
		m_rowChanged = -1;
		m_newRow = -1;
		m_inserting = false;
		fireDataStatusIEvent("Saved");
		//
		log.info("dataSave - fini");
		return SAVE_OK;
	}	//	dataSave

	/**
	 * 	Save via PO
	 *	@param Record_ID
	 *	@return
	 */
	private char dataSavePO (int Record_ID)
	{
		log.debug("dataSavePO - " + Record_ID);
		//
		MSort sort = (MSort)m_sort.get(m_rowChanged);
		Object[] rowData = (Object[])m_buffer.get(sort.index);
		//
		M_Table table = M_Table.get (m_ctx, m_AD_Table_ID);
		PO po = null;
		if (Record_ID != -1)
			po = table.getPO(Record_ID);
		else	//	Multi - Key
			po = table.getPO(getWhereClause(rowData));
		//	No Persistent Object
		if (po == null)
			throw new IllegalStateException("No Persistent Object");
		if (po == null)
		{
			ValueNamePair pp = Log.retrieveError();
			if (pp != null)
				fireDataStatusEEvent(pp);
			else
			{
				String msg = "SaveError";
				fireDataStatusEEvent(msg, "No Persistent Object");
			}
			return SAVE_ERROR;
		}
		
		int size = m_fields.size();
		for (int col = 0; col < size; col++)
		{
			MField field = (MField)m_fields.get (col);
			String columnName = field.getColumnName ();
			Object value = rowData[col];
			Object oldValue = m_rowData[col];
			//	RowID
			if (field.getDisplayType () == DisplayType.RowID)
				; 	//	ignore

			//	Nothing changed & null
			else if (oldValue == null && value == null)
				;	//	ignore
			
			//	***	Data changed ***
			else if (m_inserting
			  || (oldValue == null && value != null)
			  || (oldValue != null && value == null)
			  || !oldValue.equals (value)) 			//	changed
			{
				//	Check existence
				int poIndex = po.get_ColumnIndex(columnName);
				if (poIndex < 0)
				{
					//	Custom Fields not in PO
					po.set_CustomColumn(columnName, value);
				//	log.error("dataSavePO - Column not found: " + columnName);
					continue;
				}
				
				Object dbValue = po.get_Value(poIndex);
				if (m_inserting 
					|| !m_compareDB
					//	Original == DB
					|| (oldValue == null && dbValue == null)
					|| (oldValue != null && oldValue.equals (dbValue))
					//	Target == DB (changed by trigger to new value already)
					|| (value == null && dbValue == null)
					|| (value != null && value.equals (dbValue)) )
				{
					po.set_ValueNoCheck (columnName, value);
				}
				//	Original != DB
				else
				{
					fireDataStatusEEvent("SaveErrorDataChanged", 
						columnName 
						+ "= " + oldValue 
							+ (oldValue==null ? "" : "(" + oldValue.getClass().getName() + ")")
						+ " != DB: " + dbValue 
							+ (dbValue==null ? "" : "(" + dbValue.getClass().getName() + ")")
						+ " -> New: " + value 
							+ (value==null ? "" : "(" + value.getClass().getName() + ")"));
					dataRefresh(m_rowChanged);
					return SAVE_ERROR;
				}
			}	//	Data changed

		}	//	for every column

		if (!po.save())
		{
			String msg = "SaveError";
			String info = "";
			ValueNamePair pp = Log.retrieveError();
			if (pp != null)
			{
				msg = pp.getValue();
				info = pp.getName();
			}
			Exception ex = Log.retrieveException();
			if (ex != null 
				&& ex instanceof SQLException
				&& ((SQLException)ex).getErrorCode() == 1)
				msg = "SaveErrorNotUnique";
			fireDataStatusEEvent(msg, info);
			return SAVE_ERROR;
		}
		
		//	Refresh - update buffer
		String whereClause = po.get_WhereClause(true);
		log.debug("dataSavePO - reading ... " + whereClause);
		StringBuffer refreshSQL = new StringBuffer(m_SQL_Select)
			.append(" WHERE ").append(whereClause);
		PreparedStatement pstmt = DB.prepareStatement(refreshSQL.toString());
		try
		{
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				Object[] rowDataDB = readData(rs);
				//	update buffer
				m_buffer.set(sort.index, rowDataDB);
				fireTableRowsUpdated(m_rowChanged, m_rowChanged);
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException e)
		{
			try
			{
				if (pstmt != null)
				  pstmt.close ();
				pstmt = null;
			}
			catch (Exception ex)
			{
			}

			String msg = "SaveError";
			log.error ("dataSavePO - " + refreshSQL.toString(), e);
			fireDataStatusEEvent(msg, e.getLocalizedMessage());
			return SAVE_ERROR;
		}

		//	everything ok
		m_rowData = null;
		m_changed = false;
		m_compareDB = true;
		m_rowChanged = -1;
		m_newRow = -1;
		m_inserting = false;
		fireDataStatusIEvent("Saved");
		//
		log.info("dataSave - fini");
		return SAVE_OK;
	}	//	dataSavePO
	
	/**
	 * 	Get Where Clause
	 *	@param rowData data
	 *	@return where clause or null
	 */
	private String getWhereClause (Object[] rowData)
	{
		int size = m_fields.size();
		StringBuffer singleRowWHERE = null;
		StringBuffer multiRowWHERE = null;
		for (int col = 0; col < size; col++)
		{
			MField field = (MField)m_fields.get (col);
			if (field.isKey())
			{
				String columnName = field.getColumnName();
				Object value = rowData[col]; 
				if (value == null)
				{
					log.error("getWhereClause - PK data is null");
					return null;
				}
				if (columnName.endsWith ("_ID"))
					singleRowWHERE = new StringBuffer(columnName)
						.append ("=").append (value);
				else
					singleRowWHERE = new StringBuffer(columnName)
						.append ("=").append (DB.TO_STRING(value.toString()));
			}
			else if (field.isParent())
			{
				String columnName = field.getColumnName();
				Object value = rowData[col]; 
				if (value == null)
				{
					log.error("getWhereClause - FK data is null");
					return null;
				}
				if (multiRowWHERE == null)
					multiRowWHERE = new StringBuffer();
				else
					multiRowWHERE.append(" AND ");
				if (columnName.endsWith ("_ID"))
					multiRowWHERE.append (columnName)
						.append ("=").append (value);
				else
					multiRowWHERE.append (columnName)
						.append ("=").append (DB.TO_STRING(value.toString()));
			}
		}	//	for all columns
		if (singleRowWHERE != null)
			return singleRowWHERE.toString();
		if (multiRowWHERE != null)
			return multiRowWHERE.toString();
		log.error("getWhereClause - No key Found");
		return null;
	}	//	getWhereClause
	
	/*************************************************************************/

	private ArrayList	m_createSqlColumn = new ArrayList();
	private ArrayList	m_createSqlValue = new ArrayList();

	/**
	 * 	Prepare SQL creation
	 * 	@param columnName column name
	 * 	@param value value
	 */
	private void createUpdateSql (String columnName, String value)
	{
		m_createSqlColumn.add(columnName);
		m_createSqlValue.add(value);
		if (Log.isTraceLevel(10))
			log.debug("createUpdateSql #" + m_createSqlColumn.size()
				+ " - " + columnName + "=" + value);
	}	//	createUpdateSQL

	/**
	 * 	Create update/insert SQL
	 * 	@param insert true if insert - update otherwise
	 * 	@param whereClause where clause for update
	 * 	@return sql statement
	 */
	private String createUpdateSql (boolean insert, String whereClause)
	{
		StringBuffer sb = new StringBuffer();
		if (insert)
		{
			sb.append("INSERT INTO ").append(m_tableName).append(" (");
			for (int i = 0; i < m_createSqlColumn.size(); i++)
			{
				if (i != 0)
					sb.append(",");
				sb.append(m_createSqlColumn.get(i));
			}
			sb.append(") VALUES ( ");
			for (int i = 0; i < m_createSqlValue.size(); i++)
			{
				if (i != 0)
					sb.append(",");
				sb.append(m_createSqlValue.get(i));
			}
			sb.append(")");
		}
		else
		{
			sb.append("UPDATE ").append(m_tableName).append(" SET ");
			for (int i = 0; i < m_createSqlColumn.size(); i++)
			{
				if (i != 0)
					sb.append(",");
				sb.append(m_createSqlColumn.get(i)).append("=").append(m_createSqlValue.get(i));
			}
			sb.append(" WHERE ").append(whereClause);
		}
		log.debug("createUpdateSql=" + sb.toString());
		//	reset
		createUpdateSqlReset();
		return sb.toString();
	}	//	createUpdateSql

	/**
	 * 	Reset Update Data
	 */
	private void createUpdateSqlReset()
	{
		m_createSqlColumn = new ArrayList();
		m_createSqlValue = new ArrayList();
	}	//	createUpdateSqlReset

	/**
	 *	Get Mandatory empty columns
	 *  @param rowData row data
	 *  @return String with missing column headers/labels
	 */
	private String getMandatory(Object[] rowData)
	{
		//  see also => ProcessParameter.saveParameter
		StringBuffer sb = new StringBuffer();

		//	Check all columns
		int size = m_fields.size();
		for (int i = 0; i < size; i++)
		{
			MField field = (MField)m_fields.get(i);
			if (field.isMandatory(true))        //  check context
			{
				if (rowData[i] == null || rowData[i].toString().length() == 0)
				{
					field.setInserting (true);  //  set editable otherwise deadlock
					field.setError(true);
					if (sb.length() > 0)
						sb.append(", ");
					sb.append(field.getHeader());
				}
				else
					field.setError(false);
			}
		}

		if (sb.length() == 0)
			return "";
		return sb.toString();
	}	//	getMandatory

	/*************************************************************************/

	/**	LOB Info				*/
	private ArrayList		m_lobInfo = null;

	/**
	 * 	Reset LOB info
	 */
	private void lobReset()
	{
		m_lobInfo = null;
	}	//	resetLOB
	
	/**
	 * 	Prepare LOB save
	 *	@param value value 
	 *	@param index index
	 *	@param displayType display type
	 */	
	private void lobAdd (PO_LOB lob)
	{
		log.debug("lobAdd - " + lob);
		if (m_lobInfo == null)
			m_lobInfo = new ArrayList();
		m_lobInfo.add(lob);
	}	//	lobAdd
	
	/**
	 * 	Save LOB
	 */
	private void lobSave (String whereClause)
	{
		if (m_lobInfo == null)
			return;
		for (int i = 0; i < m_lobInfo.size(); i++)
		{
			PO_LOB lob = (PO_LOB)m_lobInfo.get(i);
			lob.save(whereClause);
		}	//	for all LOBs
		lobReset();
	}	//	lobSave

	
	/**************************************************************************
	 *	New Record after current Row
	 *  @param currentRow row
	 *  @param copyCurrent copy
	 *  @return true if success -
	 *  Error info (Access*, AccessCannotInsert) is saved in the log
	 */
	public boolean dataNew (int currentRow, boolean copyCurrent)
	{
		log.info("dataNew - Current=" + currentRow + ", Copy=" + copyCurrent);
		//  Read only
		if (m_readOnly)
		{
			fireDataStatusEEvent("AccessCannotInsert", "");
			return false;
		}

		/** @todo No TableLevel */
		//  || !Access.canViewInsert(m_ctx, m_WindowNo, tableLevel, true, true))
		//  fireDataStatusEvent(Log.retrieveError());

		//  see if we need to save
		dataSave(-2, false);


		m_inserting = true;
		//	Create default data
		int size = m_fields.size();
		m_rowData = new Object[size];	//	"original" data
		Object[] rowData = new Object[size];
		//	fill data
		if (copyCurrent)
		{
			MSort sort = (MSort) m_sort.get(currentRow);
			Object[] origData = (Object[])m_buffer.get(sort.index);
			for (int i = 0; i < size; i++)
			{
				MField field = (MField)m_fields.get(i);
				String columnName = field.getColumnName();
				if (columnName.startsWith("Created") || columnName.startsWith("Updated")
					|| columnName.equals("EntityType") || columnName.startsWith("DocumentNo"))
				{
					rowData[i] = field.getDefault();
					field.setValue(rowData[i], m_inserting);
				}
				else
					rowData[i] = origData[i];
			}
		}
		else	//	new
		{
			for (int i = 0; i < size; i++)
			{
				MField field = (MField)m_fields.get(i);
				rowData[i] = field.getDefault();
				field.setValue(rowData[i], m_inserting);
			}
		}
		m_changed = true;
		m_compareDB = true;
		m_rowChanged = -1;  //  only changed in setValueAt
		m_newRow = currentRow + 1;
		//  if there is no record, the current row could be 0 (and not -1)
		if (m_buffer.size() < m_newRow)
			m_newRow = m_buffer.size();

		//	add Data at end of buffer
		MSort sort = new MSort(m_buffer.size(), null);	//	index
		m_buffer.add(rowData);
		//	add Sort pointer
		m_sort.add(m_newRow, sort);
		m_rowCount++;

		//	inform
		log.debug("dataNew - Current=" + currentRow + ", New=" + m_newRow);
		fireTableRowsInserted(m_newRow, m_newRow);
		fireDataStatusIEvent(copyCurrent ? "UpdateCopied" : "Inserted");
		log.debug("dataNew - Current=" + currentRow + ", New=" + m_newRow + " - complete");
		return true;
	}	//	dataNew


	/**************************************************************************
	 *	Delete Data
	 *  @param row row
	 *  @return true if success -
	 *  Error info (Access*, AccessNotDeleteable, DeleteErrorDependent,
	 *  DeleteError) is saved in the log
	 */
	public boolean dataDelete (int row)
	{
		log.info("dataDelete - " + row);
		if (row < 0)
			return false;
		Object rowID = getRowID(row);
		if (rowID == null)
			return false;

		//	Tab R/O
		if (m_readOnly)
		{
			fireDataStatusEEvent("AccessCannotDelete", "");		//	previleges
			return false;
		}

		//	Is this record deletable?
		if (!m_deleteable)
		{
			fireDataStatusEEvent("AccessNotDeleteable", "");	//	audit
			return false;
		}

		//	Processed Column and not an Import Table
		if (m_indexProcessedColumn > 0 && !m_tableName.startsWith("I_"))
		{
			Boolean processed = (Boolean)getValueAt(row, m_indexProcessedColumn);
			if (processed != null && processed.booleanValue())
			{
				fireDataStatusEEvent("CannotDeleteTrx", "");
				return false;
			}
		}
		

		/** @todo check Access */
		//  fireDataStatusEvent(Log.retrieveError());

		MSort sort = (MSort)m_sort.get(row);
		Object[] rowData = (Object[])m_buffer.get(sort.index);
		//
		M_Table table = M_Table.get (m_ctx, m_AD_Table_ID);
		PO po = null;
		int Record_ID = getKeyID(m_rowChanged);
		if (Record_ID != -1)
			po = table.getPO(Record_ID);
		else	//	Multi - Key
			po = table.getPO(getWhereClause(rowData));
		
		//	Delete via PO 
		if (po != null)
		{
			if (!po.delete(false))
			{
				ValueNamePair vp = Log.retrieveError();
				if (vp != null)
					fireDataStatusEEvent(vp);
				else
					fireDataStatusEEvent("DeleteError", "");
				return false;
			}
		}
		else	//	Delete via SQL
		{
			StringBuffer SQL = new StringBuffer("DELETE ");
			SQL.append(m_tableName).append(" WHERE ROWID=?");
			int no = 0;
			try
			{
				PreparedStatement pstmt = DB.prepareStatement(SQL.toString());
				DB.getDatabase().setRowID(pstmt, 1, rowID);
				no = pstmt.executeUpdate();
				pstmt.close();
			}
			catch (SQLException e)
			{
				log.error ("dataDelete", e);
				String msg = "DeleteError";
				if (e.getErrorCode() == 2292)	//	Child Record Found
					msg = "DeleteErrorDependent";
				fireDataStatusEEvent(msg, e.getLocalizedMessage());
				return false;
			}
			//	Check Result
			if (no != 1)
			{
				log.error("dataDelete - Number of deleted rows = " + no);
				return false;
			}
		}

		//	Get Sort
		int bufferRow = sort.index;
		//	Delete row in Buffer and shifts all below up
		m_buffer.remove(bufferRow);
		m_rowCount--;

		//	Delete row in Sort
		m_sort.remove(row);
		//	Correct pointer in Sort
		for (int i = 0; i < m_sort.size(); i++)
		{
			MSort ptr = (MSort)m_sort.get(i);
			if (ptr.index > bufferRow)
				ptr.index--;	//	move up
		}

		//	inform
		m_changed = false;
		m_rowChanged = -1;
		fireTableRowsDeleted(row, row);
		fireDataStatusIEvent("Deleted");
		log.debug("dataDelete - " + row + " complete");
		return true;
	}	//	dataDelete

	
	/**************************************************************************
	 *	Ignore changes
	 */
	public void dataIgnore()
	{
		log.info("dataIgnore - Inserting=" + m_inserting);
		if (!m_inserting && !m_changed && m_rowChanged < 0)
		{
			log.debug("dataIgnore - Nothing to ignore");
			return;
		}

		//	Inserting - delete new row
		if (m_inserting)
		{
			//	Get Sort
			MSort sort = (MSort)m_sort.get(m_newRow);
			int bufferRow = sort.index;
			//	Delete row in Buffer and shifts all below up
			m_buffer.remove(bufferRow);
			m_rowCount--;
			//	Delete row in Sort
			m_sort.remove(m_newRow);	//	pintint to the last column, so no adjustment
			//
			m_changed = false;
			m_rowData = null;
			m_rowChanged = -1;
			m_inserting = false;
			//	inform
			fireTableRowsDeleted(m_newRow, m_newRow);
		}
		else
		{
			//	update buffer
			if (m_rowData != null)
			{
				MSort sort = (MSort)m_sort.get(m_rowChanged);
				m_buffer.set(sort.index, m_rowData);
			}
			m_changed = false;
			m_rowData = null;
			m_rowChanged = -1;
			m_inserting = false;
			//	inform
		//	fireTableRowsUpdated(m_rowChanged, m_rowChanged); >> messes up display?? (clearSelection)
		}
		m_newRow = -1;
		fireDataStatusIEvent("Ignored");
	}	//	dataIgnore


	/**
	 *	Refresh Row - ignore changes
	 *  @param row row
	 */
	public void dataRefresh (int row)
	{
		log.info("dataRefresh " + row);

		if (row < 0)
			return;
		Object rowID = getRowID(row);
		if (rowID == null)
			return;

		//  ignore
		dataIgnore();

		//	Create SQL
		String SQL = m_SQL_Select + " WHERE ROWID=?";
		MSort sort = (MSort)m_sort.get(row);
		Object[] rowDataDB = null;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(SQL);
			DB.getDatabase().setRowID(pstmt, 1, rowID);
			ResultSet rs = pstmt.executeQuery();
			//	only one row
			if (rs.next())
				rowDataDB = readData(rs);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.error ("dataRefresh\nSQL=" + SQL, e);
			fireTableRowsUpdated(row, row);
			fireDataStatusEEvent("RefreshError", "");
			return;
		}

		//	update buffer
		m_buffer.set(sort.index, rowDataDB);
		//	info
		m_rowData = null;
		m_changed = false;
		m_rowChanged = -1;
		m_inserting = false;
		fireTableRowsUpdated(row, row);
		fireDataStatusIEvent("Refreshed");
	}	//	dataRefresh


	/**
	 *	Refresh all Rows - ignore changes
	 */
	public void dataRefreshAll()
	{
		log.info("dataRefreshAll");
		dataIgnore();
		close(false);
		open();
		//	Info
		m_rowData = null;
		m_changed = false;
		m_rowChanged = -1;
		m_inserting = false;
		fireTableDataChanged();
		fireDataStatusIEvent("Refreshed");
	}	//	dataRefreshAll


	/**
	 *	Requery with new whereClause
	 *  @param whereClause sql where clause
	 *  @param onlyCurrentRows only current rows
	 *  @param onlyCurrentDays how many days back
	 *  @return true if success
	 */
	public boolean dataRequery (String whereClause, boolean onlyCurrentRows, int onlyCurrentDays)
	{
		log.info("dataRequery - " + whereClause + "; OnlyCurrent=" + onlyCurrentRows);
		close(false);
		m_onlyCurrentDays = onlyCurrentDays;
		setWhereClause(whereClause, onlyCurrentRows, m_onlyCurrentDays);
		open();
		//  Info
		m_rowData = null;
		m_changed = false;
		m_rowChanged = -1;
		m_inserting = false;
		fireTableDataChanged();
		fireDataStatusIEvent("Refreshed");
		return true;
	}	//	dataRequery


	/**************************************************************************
	 *	Is Cell Editable.
	 *	Is queried from JTable before checking VCellEditor.isCellEditable
	 *  @param  row the row index being queried
	 *  @param  col the column index being queried
	 *  @return true, if editable
	 */
	public boolean isCellEditable (int row, int col)
	{
	//	Log.trace(Log.l6_Database, "MTable.isCellEditable - Row=" + row + ", Col=" + col);
		//	Make Rows selectable
		if (col == 0)
			return true;

		//	Entire Table not editable
		if (m_readOnly)
			return false;
		//	Key & ID not editable
		if (col == m_indexRowIDColumn || col == m_indexKeyColumn)
			return false;
		/** @todo check link columns */

		//	Check column range
		if (col < 0 && col >= m_fields.size())
			return false;
		//  IsActive Column always editable if no processed exists
		if (col == m_indexActiveColumn && m_indexProcessedColumn == -1)
			return true;
		//	Row
		if (!isRowEditable(row))
			return false;

		//	Column
		return ((MField)m_fields.get(col)).isEditable(false);
	}	//	IsCellEditable


	/**
	 *	Is Current Row Editable
	 *  @param row row
	 *  @return true if editable
	 */
	public boolean isRowEditable (int row)
	{
	//	Log.trace(Log.l6_Database, "MTable.isRowEditable - Row=" + row);
		//	Entire Table not editable or no row
		if (m_readOnly || row < 0)
			return false;
		//	If not Active - not editable
		if (m_indexActiveColumn > 0)		//	&& m_TabNo != Find.s_TabNo)
		{
			Object value = getValueAt(row, m_indexActiveColumn);
			if (value instanceof Boolean)
			{
				if (!((Boolean)value).booleanValue())
					return false;
			}
			else if ("N".equals(value)) 
				return false;
		}
		//	If Processed - not editable (Find always editable)
		if (m_indexProcessedColumn > 0)		//	&& m_TabNo != Find.s_TabNo)
		{
			Object processed = getValueAt(row, m_indexProcessedColumn);
			if (processed instanceof Boolean)
			{
				if (((Boolean)processed).booleanValue())
					return false;
			}
			else if ("Y".equals(processed)) 
				return false;
		}
		//
		int[] co = getClientOrg(row);
		int AD_Client_ID = co[0]; 
		int AD_Org_ID = co[1];
		return MRole.getDefault(m_ctx, false).canUpdate(AD_Client_ID, AD_Org_ID, m_AD_Table_ID, false);
	}	//	isRowEditable

	/**
	 * 	Get Client Org for row
	 *	@param row row
	 *	@return array [0] = Client [1] = Org - a value of -1 is not defined/found
	 */
	private int[] getClientOrg (int row)
	{
		int AD_Client_ID = -1;
		if (m_indexClientColumn != -1)
		{
			Integer ii = (Integer)getValueAt(row, m_indexClientColumn);
			if (ii != null)
				AD_Client_ID = ii.intValue();
		}
		int AD_Org_ID = 0;
		if (m_indexOrgColumn != -1)
		{
			Integer ii = (Integer)getValueAt(row, m_indexOrgColumn);
			if (ii != null)
				AD_Org_ID = ii.intValue();
		}
		return new int[] {AD_Client_ID, AD_Org_ID};
	}	//	getClientOrg

	/**
	 *	Set entire table as read only
	 *  @param value new read only value
	 */
	public void setReadOnly (boolean value)
	{
		log.debug("setReadOnly " + value);
		m_readOnly = value;
	}	//	setReadOnly

	/**
	 *  Is entire Table Read/Only
	 *  @return true if read only
	 */
	public boolean isReadOnly()
	{
		return m_readOnly;
	}   //  isReadOnly

	/**
	 *  Is inserting
	 *  @return true if inserting
	 */
	public boolean isInserting()
	{
		return m_inserting;
	}   //  isInserting

	/**
	 *	Set Compare DB.
	 * 	If Set to false, save overwrites the record, regardless of DB changes.
	 *  (When a payment is changed in Sales Order, the payment reversal clears the payment id)
	 * 	@param compareDB compare DB - false forces overwrite
	 */
	public void setCompareDB (boolean compareDB)
	{
		m_compareDB = compareDB;
	}  	//	setCompareDB

	/**
	 *	Get Compare DB.
	 * 	@return false if save overwrites the record, regardless of DB changes
	 * 	(false forces overwrite).
	 */
	public boolean getCompareDB ()
	{
		return m_compareDB;
	}  	//	getCompareDB


	/**
	 *	Can Table rows be deleted
	 *  @param value new deleteable value
	 */
	public void setDeleteable (boolean value)
	{
		log.debug("setDeleteable " + value);
		m_deleteable = value;
	}	//	setDeleteable

	
	/**************************************************************************
	 *	Read Data from Recordset
	 *  @param rs result set
	 *  @return Data Array
	 */
	private Object[] readData (ResultSet rs)
	{
		int size = m_fields.size();
		Object[] rowData = new Object[size];
		String columnName = null;
		int displayType = 0;

		//	Types see also MField.createDefault
		try
		{
			//	get row data
			for (int j = 0; j < size; j++)
			{
				//	Column Info
				MField field = (MField)m_fields.get(j);
				columnName = field.getColumnName();
				displayType = field.getDisplayType();
				//	Integer, ID, Lookup (UpdatedBy is a numeric column)
				if (displayType == DisplayType.Integer
					|| (DisplayType.isID(displayType) && (columnName.endsWith("_ID") || columnName.endsWith("_Acct"))) 
					|| columnName.endsWith("atedBy"))
				{
					rowData[j] = new Integer(rs.getInt(j+1));	//	Integer
					if (rs.wasNull())
						rowData[j] = null;
				}
				//	Number
				else if (DisplayType.isNumeric(displayType))
					rowData[j] = rs.getBigDecimal(j+1);			//	BigDecimal
				//	Date
				else if (DisplayType.isDate(displayType))
					rowData[j] = rs.getTimestamp(j+1);			//	Timestamp
				//	RowID or Key (and Selection)
				else if (displayType == DisplayType.RowID)
				{
					Object[] rid = new Object[3];
					if (columnName.equals("ROWID"))
						rid[0] = DB.getDatabase().getRowID(rs, j+1);
					else
						rid[2] = new Integer (rs.getInt(j+1));
					rid[1] = new Boolean(false);
					rowData[j] = rid;
				}
				//	YesNo
				else if (displayType == DisplayType.YesNo)
					rowData[j] = new Boolean ("Y".equals(rs.getString(j+1)));	//	Boolean			
				//	LOB
				else if (displayType == DisplayType.TextLong)
				{
					Object value = rs.getObject(j+1);
					if (rs.wasNull())
						rowData[j] = null;
					else if (value instanceof Clob) 
					{
						Clob lob = (Clob)value;
						long length = lob.length();
						rowData[j] = lob.getSubString(1, (int)length);
					}
				}
				//	String
				else
					rowData[j] = rs.getString(j+1);				//	String
			}
		}
		catch (SQLException e)
		{
			log.error("readData - " + columnName + ", DT=" + displayType, e);
		}
		return rowData;
	}	//	readData

	
	/**************************************************************************
	 *	Remove Data Status Listener
	 *  @param l listener
	 */
	public synchronized void removeDataStatusListener(DataStatusListener l)
	{
		if (m_dataStatusListeners != null && m_dataStatusListeners.contains(l))
		{
			Vector v = (Vector) m_dataStatusListeners.clone();
			v.removeElement(l);
			m_dataStatusListeners = v;
		}
	}	//	removeDataStatusListener

	/**
	 *	Add Data Status Listener
	 *  @param l listener
	 */
	public synchronized void addDataStatusListener(DataStatusListener l)
	{
		Vector v = m_dataStatusListeners == null ? new Vector(2) : (Vector) m_dataStatusListeners.clone();
		if (!v.contains(l))
		{
			v.addElement(l);
			m_dataStatusListeners = v;
		}
	}	//	addDataStatusListener

	/**
	 *	Inform Listeners
	 *  @param e event
	 */
	private void fireDataStatusChanged (DataStatusEvent e)
	{
		if (m_dataStatusListeners != null)
		{
			Vector listeners = m_dataStatusListeners;
			int count = listeners.size();
			for (int i = 0; i < count; i++)
				((DataStatusListener) listeners.elementAt(i)).dataStatusChanged(e);
		}
	}	//	fireDataStatusChanged

	/**
	 *  Create Data Status Event
	 *  @return data status event
	 */
	private DataStatusEvent createDSE()
	{
		boolean changed = m_changed;
		if (m_rowChanged != -1)
			changed = true;
		DataStatusEvent dse = new DataStatusEvent(this, m_rowCount, changed,
			Env.isAutoCommit(m_ctx, m_WindowNo), m_inserting);
		return dse;
	}   //  createDSE

	/**
	 *  Create and fire Data Status Info Event
	 *  @param AD_Message message
	 */
	protected void fireDataStatusIEvent (String AD_Message)
	{
		DataStatusEvent e = createDSE();
		e.setInfo(AD_Message, "", false);
		fireDataStatusChanged (e);
	}   //  fireDataStatusEvent

	/**
	 *  Create and fire Data Status Error Event
	 *  @param AD_Message message
	 *  @param info info
	 */
	protected void fireDataStatusEEvent (String AD_Message, String info)
	{
	//	org.compiere.util.Trace.printStack();
		//
		DataStatusEvent e = createDSE();
		e.setInfo(AD_Message, info, true);
		Log.saveError(AD_Message, info);
		fireDataStatusChanged (e);
	}   //  fireDataStatusEvent

	/**
	 *  Create and fire Data Status Event (from Error Log)
	 *  @param errorLog error log info
	 */
	protected void fireDataStatusEEvent (ValueNamePair errorLog)
	{
		if (errorLog != null)
			fireDataStatusEEvent (errorLog.getValue(), errorLog.getName());
	}   //  fireDataStatusEvent

	
	/**************************************************************************
	 *  Remove Vetoable change listener for row changes
	 *  @param l listener
	 */
	public synchronized void removeVetoableChangeListener(VetoableChangeListener l)
	{
		m_vetoableChangeSupport.removeVetoableChangeListener(l);
	}   //  removeVetoableChangeListener

	/**
	 *  Add Vetoable change listener for row changes
	 *  @param l listener
	 */
	public synchronized void addVetoableChangeListener(VetoableChangeListener l)
	{
		m_vetoableChangeSupport.addVetoableChangeListener(l);
	}   //  addVetoableChangeListener

	/**
	 *  Fire Vetoable change listener for row changes
	 *  @param e event
	 *  @throws PropertyVetoException
	 */
	protected void fireVetoableChange(PropertyChangeEvent e) throws java.beans.PropertyVetoException
	{
		m_vetoableChangeSupport.fireVetoableChange(e);
	}   //  fireVetoableChange

	/**
	 *  toString
	 *  @return String representation
	 */
	public String toString()
	{
		return new StringBuffer("MTable[").append(m_tableName)
			.append(",WindowNo=").append(m_WindowNo)
			.append(",Tab=").append(m_TabNo).append("]").toString();
	}   //  toString


	
	/**************************************************************************
	 *	ASync Loader
	 */
	class Loader extends Thread implements Serializable
	{
		/**
		 *  Construct Loader
		 */
		public Loader()
		{
			super("TLoader");
		}	//	Loader

		private PreparedStatement   m_pstmt = null;
		private ResultSet 		    m_rs = null;

		/**
		 *	Open ResultSet
		 *	@return number of records
		 */
		protected int open()
		{
		//	Log.trace(Log.l4_Data, "MTable Loader.open");
			//	Get Number of Rows
			int rows = 0;
			try
			{
				PreparedStatement pstmt = DB.prepareStatement(m_SQL_Count);
				setParameter (pstmt, true);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next())
					rows = rs.getInt(1);
				rs.close();
				pstmt.close();
			}
			catch (SQLException e0)
			{
				//	Zoom Query may have invalid where clause
				if (e0.getErrorCode() == 904) 	//	ORA-00904: "C_x_ID": invalid identifier
					log.warn("Loader.open Count - " + e0.getLocalizedMessage() + "\nSQL=" + m_SQL_Count);
				else
					log.error ("Loader.open Count SQL=" + m_SQL_Count, e0);
				return 0;
			}

			//	open Statement (closed by Loader.close)
			try
			{
				m_pstmt = DB.prepareStatement(m_SQL);
			//	m_pstmt.setFetchSize(20);
				setParameter (m_pstmt, false);
				m_rs = m_pstmt.executeQuery();
			}
			catch (SQLException e)
			{
				log.error ("Loader.open\nFull SQL=" + m_SQL, e);
				return 0;
			}
			StringBuffer info = new StringBuffer("Rows=");
			info.append(rows);
			if (rows == 0)
				info.append(" - ").append(m_SQL_Count);
			log.debug("Loader.open - " + info.toString());
			return rows;
		}	//	open

		/**
		 *	Close RS and Statement
		 */
		private void close()
		{
		//	Log.trace(Log.l4_Data, "MTable Loader.close");
			try
			{
				if (m_rs != null)
					m_rs.close();
				if (m_pstmt != null)
					m_pstmt.close();
			}
			catch (SQLException e)
			{
				log.error ("Loader.closeRS", e);
			}
			m_rs = null;
			m_pstmt = null;
		}	//	close

		/**
		 *	Fill Buffer to include Row
		 */
		public void run()
		{
			log.info("Loader.run");
			if (m_rs == null)
				return;

			try
			{
				while(m_rs.next())
				{
					if (this.isInterrupted())
					{
						log.debug("Loader interrupted");
						close();
						return;
					}
					//  Get Data
					Object[] rowData = readData(m_rs);
					//	add Data
					MSort sort = new MSort(m_buffer.size(), null);	//	index
					m_buffer.add(rowData);
					m_sort.add(sort);

					//	Statement all 250 rows & sleep
					if (m_buffer.size() % 250 == 0)
					{
						//	give the other processes a chance
						try
						{
							yield();
							sleep(10);		//	.01 second
						}
						catch (InterruptedException ie)
						{
							log.debug("Loader interrupted while sleeping");
							close();
							return;
						}
						DataStatusEvent evt = createDSE();
						evt.setLoading(m_buffer.size());
						fireDataStatusChanged(evt);
					}
				}	//	while(rs.next())
			}
			catch (SQLException e)
			{
				log.error ("Loader.run", e);
			}
			close();
			fireDataStatusIEvent("");
		}	//	run

		/**
		 *	Set Parameter for Query.
		 *		elements must be Integer, BigDecimal, String (default)
		 *  @param pstmt prepared statement
		 *  @param countSQL count
		 */
		private void setParameter (PreparedStatement pstmt, boolean countSQL)
		{
			if (m_parameterSELECT.size() == 0 && m_parameterWHERE.size() == 0)
				return;
			try
			{
				int pos = 1;	//	position in Statement
				//	Select Clause Parameters
				for (int i = 0; !countSQL && i < m_parameterSELECT.size(); i++)
				{
					Object para = m_parameterSELECT.get(i);
					if (para != null)
						log.debug("setParameter Select " + i + "=" + para);
					//
					if (para == null)
						;
					else if (para instanceof Integer)
					{
						Integer ii = (Integer)para;
						pstmt.setInt (pos++, ii.intValue());
					}
					else if (para instanceof BigDecimal)
						pstmt.setBigDecimal (pos++, (BigDecimal)para);
					else
						pstmt.setString(pos++, para.toString());
				}
				//	Where Clause Parameters
				for (int i = 0; i < m_parameterWHERE.size(); i++)
				{
					Object para = m_parameterWHERE.get(i);
					if (para != null)
						log.debug("setParameter Where " + i + "=" + para);
					//
					if (para == null)
						;
					else if (para instanceof Integer)
					{
						Integer ii = (Integer)para;
						pstmt.setInt (pos++, ii.intValue());
					}
					else if (para instanceof BigDecimal)
						pstmt.setBigDecimal (pos++, (BigDecimal)para);
					else
						pstmt.setString(pos++, para.toString());
				}
			}
			catch (SQLException e)
			{
				log.error("Loader.setParameter", e);
			}
		}	//	setParameter

	}	//	Loader

}	//	MTable
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

import javax.swing.table.*;
import javax.swing.event.*;

import java.sql.*;
import java.util.*;
import java.math.*;
import java.beans.*;
import java.io.Serializable;

import org.compiere.util.*;

/**
 *	Grid Table Model for JDBC access including buffering.
 *  <pre>
 *		The following data types are handeled
 *			Integer		for all IDs
 *			BigDecimal	for all Numbers
 *			Timestamp	for all Dates
 *			String		for all others
 *  The data is read via r/o resultset and cached in m_buffer. Writes/updates
 *  are via dynamically constructed SQL INSERT/UPDATE statements. The record
 *  is re-read via the resultset to get results of triggers.
 *
 *  </pre>
 *  The model maintains and fires the requires TableModelEvent changes,
 *  the DataChanged events (loading, changed, etc.)
 *  as well as Vetoable Change event "RowChange"
 *  (for row changes initiated by moving the row in the table grid).
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: MTable.java,v 1.62 2004/05/13 06:05:21 jjanke Exp $
 */
public final class MTable extends AbstractTableModel
	implements Serializable
{
	/**
	 *	JDBC Based Buffered Table
	 *
	 *  @param ctx Properties
	 *  @param TableName table name
	 *  @param WindowNo window no
	 *  @param TabNo tab no
	 *  @param withAccessControl    if true adds AD_Client/Org restrictuins
	 */
	public MTable(Properties ctx, int AD_Table_ID, String TableName, int WindowNo, int TabNo,
		boolean withAccessControl)
	{
		super();
		log.info(TableName);
		m_ctx = ctx;
		m_AD_Table_ID = AD_Table_ID;
		setTableName(TableName);
		m_WindowNo = WindowNo;
		m_TabNo = TabNo;
		m_withAccessControl = withAccessControl;
	}	//	MTable

	private Logger				log = Logger.getCLogger(getClass());
	private Properties          m_ctx;
	private int					m_AD_Table_ID;
	private String 		        m_tableName = "";
	private int				    m_WindowNo;
	private int				    m_TabNo;
	private boolean			    m_withAccessControl;
	private boolean			    m_readOnly = true;
	private boolean			    m_deleteable = true;
	//

	/**	Rowcount                    */
	private int				    m_rowCount = 0;
	/**	Has Data changed?           */
	private boolean			    m_changed = false;
	/** Index of changed row via SetValueAt */
	private int				    m_rowChanged = -1;
	/** Insert mode active          */
	private boolean			    m_inserting = false;
	/** Inserted Row number         */
	private int                 m_newRow = -1;
	/**	Is the Resultset open?      */
	private boolean			    m_open = false;
	/**	Compare to DB before save	*/
	private boolean				m_compareDB = true;		//	set to true after every save

	//	The buffer for all data
	private volatile ArrayList	m_buffer = new ArrayList(100);
	private volatile ArrayList	m_sort = new ArrayList(100);
	/** Original row data               */
	private Object[]			m_rowData = null;
	/** Original data [row,col,data]    */
	private Object[]            m_oldValue = null;
	//
	private Loader		        m_loader = null;

	/**	Columns                 		*/
	private ArrayList	        m_fields = new ArrayList(30);
	private ArrayList 	        m_parameterSELECT = new ArrayList(5);
	private ArrayList 	        m_parameterWHERE = new ArrayList(5);

	/** Complete SQL statement          */
	private String 		        m_SQL;
	/** SQL Statement for Row Count     */
	private String 		        m_SQL_Count;
	/** The SELECT clause with FROM     */
	private String 		        m_SQL_Select;
	/** The static where clause         */
	private String 		        m_whereClause = "";
	/** Show only Processed='N' and last 24h records    */
	private boolean		        m_onlyCurrentRows = false;
	/** Show only Not processed and x days				*/
	private int					m_onlyCurrentDays = 1;
	/** Static ORDER BY clause          */
	private String		        m_orderClause = "";

	/** Index of Key Column                 */
	private int			        m_indexKeyColumn = -1;
	/** Index of RowID column               */
	private int                 m_indexRowIDColumn = -1;
	/** Index of Color Column               */
	private int			        m_indexColorColumn = -1;
	/** Index of Processed Column           */
	private int                 m_indexProcessedColumn = -1;
	/** Index of IsActive Column            */
	private int                 m_indexActiveColumn = -1;
	/** Index of AD_Client_ID Column        */
	private int					m_indexClientColumn = -1;
	/** Index of AD_Org_ID Column           */
	private int					m_indexOrgColumn = -1;

	/** List of DataStatus Listeners    */
	private Vector 		        m_dataStatusListeners;
	/** Vetoable Change Bean support    */
	private VetoableChangeSupport   m_vetoableChangeSupport = new VetoableChangeSupport(this);
	/** Property of Vetoable Bean support "RowChange" */
	public static final String  PROPERTY = "MTable-RowSave";

	/**
	 *	Set Table Name
	 *  @param newTableName table name
	 */
	public void setTableName(String newTableName)
	{
		if (m_open)
		{
			log.error("setTableName - Table already open - ignored");
			return;
		}
		if (newTableName == null || newTableName.length() == 0)
			return;
		m_tableName = newTableName;
	}	//	setTableName

	/**
	 *	Get Table Name
	 *  @return table name
	 */
	public String getTableName()
	{
		return m_tableName;
	}	//	getTableName

	/**
	 *	Set Where Clause (w/o the WHERE and w/o History).
	 *  @param newWhereClause sql where clause
	 *  @param onlyCurrentRows only current rows
	 *  @param onlyCurrentDays how many days back for current
	 *	@return true if where clase set
	 */
	public boolean setWhereClause(String newWhereClause, boolean onlyCurrentRows, int onlyCurrentDays)
	{
		if (m_open)
		{
			log.error("setWhereClause - Table already open - ignored");
			return false;
		}
		//
		m_whereClause = newWhereClause;
		m_onlyCurrentRows = onlyCurrentRows;
		m_onlyCurrentDays = onlyCurrentDays;
		if (m_whereClause == null)
			m_whereClause = "";
		return true;
	}	//	setWhereClause

	/**
	 *	Get Where Clause (w/o the WHERE and w/o History)
	 *  @return where clause
	 */
	public String getWhereClause()
	{
		return m_whereClause;
	}	//	getWhereClause

	/**
	 *	Is History displayed
	 *  @return true if history displayed
	 */
	public boolean isOnlyCurrentRowsDisplayed()
	{
		return !m_onlyCurrentRows;
	}	//	isHistoryDisplayed

	/**
	 *	Set Order Clause (w/o the ORDER BY)
	 *  @param newOrderClause sql order by clause
	 */
	public void setOrderClause(String newOrderClause)
	{
		m_orderClause = newOrderClause;
		if (m_orderClause == null)
			m_orderClause = "";
	}	//	setOrderClause

	/**
	 *	Get Order Clause (w/o the ORDER BY)
	 *  @return order by clause
	 */
	public String getOrderClause()
	{
		return m_orderClause;
	}	//	getOrderClause

	/**
	 *	Assemble & store
	 *	m_SQL and m_countSQL
	 *  @return m_SQL
	 */
	private String createSelectSql()
	{
		if (m_fields.size() == 0 || m_tableName == null || m_tableName.equals(""))
			return "";

		//	Create SELECT Part
		StringBuffer select = new StringBuffer("SELECT ");
		for (int i = 0; i < m_fields.size(); i++)
		{
			if (i > 0)
				select.append(",");
			MField field = (MField)m_fields.get(i);
			select.append(field.getColumnName());
		}
		//
		select.append(" FROM ").append(m_tableName);
		m_SQL_Select = select.toString();
		m_SQL_Count = "SELECT COUNT(*) FROM " + m_tableName;
		//

		StringBuffer where = new StringBuffer("");
		//	WHERE
		if (m_whereClause.length() > 0)
		{
			where.append(" WHERE ");
			if (m_whereClause.indexOf("@") == -1)
				where.append(m_whereClause);
			else    //  replace variables
				where.append(Env.parseContext(m_ctx, m_WindowNo, m_whereClause, false));
		}
		if (m_onlyCurrentRows)
		{
			if (where.toString().indexOf(" WHERE ") == -1)
				where.append(" WHERE ");
			else
				where.append(" AND ");
			//	Show only unprocessed or the one updated within x days
			where.append("(Processed='N' OR Updated>SysDate-").append(m_onlyCurrentDays).append(")");
		}

		//	RO/RW Access
		m_SQL = m_SQL_Select + where.toString();
		m_SQL_Count += where.toString();
		if (m_withAccessControl)
		{
			boolean ro = MRole.SQL_RO;
		//	if (!m_readOnly)
		//		ro = MRole.SQL_RW;
			m_SQL = MRole.getDefault(m_ctx, false).addAccessSQL(m_SQL, 
				m_tableName, MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO);
			m_SQL_Count = MRole.getDefault(m_ctx, false).addAccessSQL(m_SQL_Count, 
				m_tableName, MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO);
		}

		//	ORDER BY
		if (!m_orderClause.equals(""))
			m_SQL += " ORDER BY " + m_orderClause;
		//
		log.debug("createSelectSql - " + m_SQL_Count);
		Env.setContext(m_ctx, m_WindowNo, m_TabNo, "SQL", m_SQL);
		return m_SQL;
	}	//	createSelectSql

	/**
	 *	Add Field to Table
	 *  @param field field
	 */
	public void addField (MField field)
	{
		log.debug ("addField (" + m_tableName + ") - " + field.getColumnName());
		if (m_open)
		{
			log.error("addField - Table already open - ignored: " + field.getColumnName());
			return;
		}
		if (!MRole.getDefault(m_ctx, false).isColumnAccess (m_AD_Table_ID, field.getAD_Column_ID(), true))
		{
			log.debug ("addField - No Column Access " + field.getColumnName());
			return;			
		}
		//  Set Index for RowID column
		if (field.getDisplayType() == DisplayType.RowID)
			m_indexRowIDColumn = m_fields.size();
		//  Set Index for Key column
		if (field.isKey())
			m_indexKeyColumn = m_fields.size();
		else if (field.getColumnName().equals("IsActive"))
			m_indexActiveColumn = m_fields.size();
		else if (field.getColumnName().equals("Processed"))
			m_indexProcessedColumn = m_fields.size();
		else if (field.getColumnName().equals("AD_Client_ID"))
			m_indexClientColumn = m_fields.size();
		else if (field.getColumnName().equals("AD_Org_ID"))
			m_indexOrgColumn = m_fields.size();
		//
		m_fields.add(field);
	}	//	addColumn

	/**
	 *  Returns database column name
	 *
	 *  @param index  the column being queried
	 *  @return column name
	 */
	public String getColumnName (int index)
	{
		if (index < 0 || index > m_fields.size())
		{
			log.error("getColumnName - invalid index=" + index);
			return "";
		}
		//
		MField field = (MField)m_fields.get(index);
		return field.getColumnName();
	}   //  getColumnName

	/**
	 * Returns a column given its name.
	 *
	 * @param columnName string containing name of column to be located
	 * @return the column index with <code>columnName</code>, or -1 if not found
	 */
	public int findColumn (String columnName)
	{
		for (int i = 0; i < m_fields.size(); i++)
		{
			MField field = (MField)m_fields.get(i);
			if (columnName.equals(field.getColumnName()))
				return i;
		}
		return -1;
	}   //  findColumn

	/**
	 *  Returns Class of database column/field
	 *
	 *  @param index  the column being queried
	 *  @return the class
	 */
	public Class getColumnClass (int index)
	{
		if (index < 0 || index >= m_fields.size())
		{
			log.error("getColumnClass - invalid index=" + index);
			return null;
		}
		MField field = (MField)m_fields.get(index);
		return DisplayType.getClass(field.getDisplayType(), false);
	}   //  getColumnClass

	/**
	 *	Set Select Clause Parameter.
	 *	Assumes that you set parameters starting from index zero
	 *  @param index index
	 *  @param parameter parameter
	 */
	public void setParameterSELECT (int index, Object parameter)
	{
		if (index >= m_parameterSELECT.size())
			m_parameterSELECT.add(parameter);
		else
			m_parameterSELECT.set(index, parameter);
	}	//	setParameterSELECT

	/**
	 *	Set Where Clause Parameter.
	 *	Assumes that you set parameters starting from index zero
	 *  @param index index
	 *  @param parameter parameter
	 */
	public void setParameterWHERE (int index, Object parameter)
	{
		if (index >= m_parameterWHERE.size())
			m_parameterWHERE.add(parameter);
		else
			m_parameterWHERE.set(index, parameter);
	}	//	setParameterWHERE


	/**
	 *	Get Column at index
	 *  @param index index
	 *  @return MField
	 */
	protected MField getField (int index)
	{
		if (index < 0 || index >= m_fields.size())
			return null;
		return (MField)m_fields.get(index);
	}	//	getColumn

	/**
	 *	Return Columns with Indentifier (ColumnName)
	 *  @param identifier column name
	 *  @return MField
	 */
	protected MField getField (String identifier)
	{
		if (identifier == null || identifier.length() == 0)
			return null;
		int cols = m_fields.size();
		for (int i = 0; i < cols; i++)
		{
			MField field = (MField)m_fields.get(i);
			if (identifier.equalsIgnoreCase(field.getColumnName()))
				return field;
		}
	//	log.error ("getField - not found: '" + identifier + "'");
		return null;
	}	//	getField

	/**
	 *  Get all Fields
	 *  @return MFields
	 */
	public MField[] getFields ()
	{
		MField[] retValue = new MField[m_fields.size()];
		m_fields.toArray(retValue);
		return retValue;
	}   //  getField

	
	/**************************************************************************
	 *	Open Database.
	 *  if already opened, data is refreshed
	 *
	 *	@return true if success
	 */
	public boolean open ()
	{
		log.info("open");
		if (m_open)
		{
			log.debug("open - already open");
			dataRefreshAll();
			return true;
		}

		//	create m_SQL and m_countSQL
		createSelectSql();
		if (m_SQL == null || m_SQL.equals(""))
		{
			log.error("open - No SQL");
			return false;
		}

		//	Start Loading
		m_loader = new Loader();
		m_rowCount = m_loader.open();
		m_buffer = new ArrayList(m_rowCount+10);
		m_sort = new ArrayList(m_rowCount+10);
		if (m_rowCount > 0)
			m_loader.start();
		else
			m_loader.close();
		m_open = true;
		//
		m_changed = false;
		m_rowChanged = -1;
		return true;
	}	//	open

	/**
	 *  Wait until async loader of Table and Lookup Fields is complete
	 *  Used for performance tests
	 */
	public void loadComplete()
	{
		//  Wait for loader
		if (m_loader != null)
		{
			if (m_loader.isAlive())
			{
				try
				{
					m_loader.join();
				}
				catch (InterruptedException ie)
				{
					log.error("loadComplete - join interrupted", ie);
				}
			}
		}
		//  wait for field lookup loaders
		for (int i = 0; i < m_fields.size(); i++)
		{
			MField field = (MField)m_fields.get(i);
			field.lookupLoadComplete();
		}
	}   //  loadComplete

	/**
	 *  Is Loading
	 *  @return true if loading
	 */
	public boolean isLoading()
	{
		if (m_loader != null && m_loader.isAlive())
			return true;
		return false;
	}   //  isLoading

	/**
	 *	Is it open?
	 *  @return true if opened
	 */
	public boolean isOpen()
	{
		return m_open;
	}	//	isOpen

	/**
	 *	Close Resultset
	 *  @param finalCall final call
	 */
	public void close (boolean finalCall)
	{
		if (!m_open)
			return;
		log.debug("close - final=" + finalCall);

		//  remove listeners
		if (finalCall)
		{
			m_dataStatusListeners.clear();
			EventListener evl[] = listenerList.getListeners(TableModelListener.class);
			for (int i = 0; i < evl.length; i++)
				listenerList.remove(TableModelListener.class, evl[i]);
			VetoableChangeListener vcl[] = m_vetoableChangeSupport.getVetoableChangeListeners();
			for (int i = 0; i < vcl.length; i++)
				m_vetoableChangeSupport.removeVetoableChangeListener(vcl[i]);
		}

		//	Stop loader
		while (m_loader != null && m_loader.isAlive())
		{
			log.debug("close - interrupting Loader");
			m_loader.interrupt();
			try
			{
				Thread.sleep(200);		//	.2 second
			}
			catch (InterruptedException ie)
			{}
		}

		if (!m_inserting)
			dataSave(true);

		if (m_buffer != null)
			m_buffer.clear();
		m_buffer = null;
		if (m_sort != null)
			m_sort.clear();
		m_sort = null;

		if (finalCall)
			dispose();

		//  Fields are disposed from MTab
		log.debug("close - complete");
		m_open = false;
	}	//	close

	/**
	 *  Dispose MTable.
	 *  Called by close-final
	 */
	private void dispose()
	{
		//  MFields
		for (int i = 0; i < m_fields.size(); i++)
			((MField)m_fields.get(i)).dispose();
		m_fields.clear();
		m_fields = null;
		//
		m_dataStatusListeners = null;
		m_vetoableChangeSupport = null;
		//
		m_parameterSELECT.clear();
		m_parameterSELECT = null;
		m_parameterWHERE.clear();
		m_parameterWHERE = null;
		//  clear data arrays
		m_buffer = null;
		m_sort = null;
		m_rowData = null;
		m_oldValue = null;
		m_loader = null;
	}   //  dispose

	/**
	 *	Get total database column count (displayed and not displayed)
	 *  @return column count
	 */
	public int getColumnCount()
	{
		return m_fields.size();
	}	//	getColumnCount

	/**
	 *	Get (displayed) field count
	 *  @return field count
	 */
	public int getFieldCount()
	{
		return m_fields.size();
	}	//	getFieldCount

	/**
	 *  Return number of rows
	 *  @return Number of rows or 0 if not opened
	 */
	public int getRowCount()
	{
		return m_rowCount;
	}	//	getRowCount

	/**
	 *	Set the Column to determine the color of the row
	 *  @param columnName column name
	 */
	public void setColorColumn (String columnName)
	{
		m_indexColorColumn = findColumn(columnName);
	}	//  setColorColumn

	/**
	 *	Get ColorCode for Row.
	 *  <pre>
	 *	If numerical value in compare column is
	 *		negative = -1,
	 *      positive = 1,
	 *      otherwise = 0
	 *  </pre>
	 *  @see #setColorColumn
	 *  @param row row
	 *  @return color code
	 */
	public int getColorCode (int row)
	{
		if (m_indexColorColumn  == -1)
			return 0;
		Object data = getValueAt(row, m_indexColorColumn);
		//	We need to have a Number
		if (data == null || !(data instanceof BigDecimal))
			return 0;
		int cmp = Env.ZERO.compareTo(data);
		if (cmp > 0)
			return -1;
		if (cmp < 0)
			return 1;
		return 0;
	}	//	getColorCode


	/**
	 *	Sort Entries by Column.
	 *  actually the rows are not sorted, just the access pointer ArrayList
	 *  with the same size as m_buffer with MSort entities
	 *  @param col col
	 *  @param ascending ascending
	 */
	public void sort (int col, boolean ascending)
	{
		log.info("sort #" + col + " " + ascending);
		if (getRowCount() == 0)
			return;
		MField field = getField (col);
		//	RowIDs are not sorted
		if (field.getDisplayType() == DisplayType.RowID)
			return;
		boolean isLookup = DisplayType.isLookup(field.getDisplayType());

		//	fill MSort entities with data entity
		for (int i = 0; i < m_sort.size(); i++)
		{
			MSort sort = (MSort)m_sort.get(i);
			Object[] rowData = (Object[])m_buffer.get(sort.index);
			if (isLookup)
				sort.data = field.getLookup().getDisplay(rowData[col]);	//	lookup
			else
				sort.data = rowData[col];								//	data
		}

		//	sort it
		MSort sort = new MSort(0, null);
		sort.setSortAsc(ascending);
		Collections.sort(m_sort, sort);
		//	update UI
		fireTableDataChanged();
		//  Info detected by MTab.dataStatusChanged and current row set to 0
		fireDataStatusIEvent("Sorted");
	}	//	sort

	/**
	 *	Get Key ID or -1 of none
	 *  @param row row
	 *  @return ID or -1
	 */
	public int getKeyID (int row)
	{
	//	Log.info("MTable.getKeyID - row=" + row + ", keyColIdx=" + m_indexKeyColumn);
		if (m_indexKeyColumn != -1)
		{
			try
			{
				Integer ii = (Integer)getValueAt(row, m_indexKeyColumn);
				if (ii == null)
					return -1;
				return ii.intValue();
			}
			catch (Exception e)     //  Alpha Key
			{
				return -1;
			}
		}
		return -1;
	}	//	getKeyID

	/**
	 *	Get Key ColumnName
	 *  @return key column name
	 */
	public String getKeyColumnName()
	{
		if (m_indexKeyColumn != -1)
			return getColumnName(m_indexKeyColumn);
		return "";
	}	//	getKeyColumnName

	/**
	 *	Get Selected ROWID or null, if no RowID exists
	 *  @param row row
	 *  @return ROWID
	 */
	public Object getRowID (int row)
	{
		Object[] rid = getRID(row);
		if (rid == null)
			return null;
		return rid[0];
	}	//	getSelectedRowID

	/**
	 *	Get RowID Structure [0]=RowID, [1]=Selected, [2]=ID.
	 *  <p>
	 *  Either RowID or ID is populated (views don't have RowID)
	 *  @param row row
	 *  @return RowID
	 */
	public Object[] getRID (int row)
	{
		if (m_indexRowIDColumn == -1 || row < 0 || row >= getRowCount())
			return null;
		return (Object[])getValueAt(row, m_indexRowIDColumn);
	}	//	getRID

	/**
	 *	Find Row with RowID
	 *  @param RowID row id or oid
	 *	@return number of row or 0 if not found
	 */
	public int getRow (Object RowID)
	{
		if (RowID == null)
			return 0;

		//	the value to find
		String find = RowID.toString();

		//	Wait a bit to load rows
		if (m_loader != null && m_loader.isAlive())
		{
			try
			{
				Thread.sleep(250);		//	1/4 second
			}
			catch (InterruptedException ie)
			{}
		}

		//	Build search vector
		int size = m_sort.size();		//	may still change
		ArrayList search = new ArrayList(size);
		for (int i = 0; i < size; i++)
		{
			Object[] r = (Object[])getValueAt(i, 0);
			String s = r[0].toString();
			MSort so = new MSort(i, s);
			search.add(so);
		}

		//	Sort it
		MSort sort = new MSort(0, null);
		Collections.sort(search, sort);

		//	Find it
		int index = Collections.binarySearch(search, find, sort);
		if (index < 0)	//	not found
		{
			search.clear();
			return 0;
		}
		//	Get Info
		MSort result = (MSort)search.get(index);
		//	Clean up
		search.clear();
		return result.index;
	}	//	getRow


	/**************************************************************************
	 * 	Get Value in Resultset
	 *  @param row row
	 *  @param col col
	 *  @return Object of that row/column
	 */
	public Object getValueAt (int row, int col)
	{
	//	Log.trace(Log.l4_Data, "MTable.getValueAt r=" + row + " c=" + col);
		if (!m_open || row < 0 || col < 0 || row >= m_rowCount)
		{
		//	Log.trace(Log.l5_DData, "Out of bounds - Open=" + m_open + ", RowCount=" + m_rowCount);
			return null;
		}

		//	need to wait for data read into buffer
		int loops = 0;
		while (row >= m_buffer.size() && m_loader.isAlive() && loops < 15)
		{
			log.debug("getValueAt - waiting for loader row=" + row + ", size=" + m_buffer.size());
			try
			{
				Thread.sleep(500);		//	1/2 second
			}
			catch (InterruptedException ie)
			{}
			loops++;
		}

		//	empty buffer
		if (row >= m_buffer.size())
		{
		//	Log.trace(Log.l5_DData, "Empty buffer");
			return null;
		}

		//	return Data item
		MSort sort = (MSort)m_sort.get(row);
		Object[] rowData = (Object[])m_buffer.get(sort.index);
		//	out of bounds
		if (rowData == null || col > rowData.length)
		{
		//	Log.trace(Log.l5_DData, "No data or Column out of bounds");
			return null;
		}
		return rowData[col];
	}	//	getValueAt

	/**
	 *	Indicate that there will be a change
	 *  @param changed changed
	 */
	public void setChanged (boolean changed)
	{
		//	Can we edit?
		if (!m_open || m_readOnly)
			return;

		//	Indicate Change
		m_changed = changed;
		if (!changed)
			m_rowChanged = -1;
		fireDataStatusIEvent("");
	}	//	setChanged

	/**
	 * 	Set Value in data and update MField.
	 *  (called directly or from JTable.editingStopped())
	 *
	 *  @param  value value to assign to cell
	 *  @param  row row index of cell
	 *  @param  col column index of cell
	 */
	public final void setValueAt (Object value, int row, int col)
	{
		setValueAt (value, row, col, false);
	}	//	setValueAt

	/**
	 * 	Set Value in data and update MField.
	 *  (called directly or from JTable.editingStopped())
	 *
	 *  @param  value value to assign to cell
	 *  @param  row row index of cell
	 *  @param  col column index of cell
	 * 	@param	force force setting new value
	 */
	public final void setValueAt (Object value, int row, int col, boolean force)
	{
		//	Can we edit?
		if (!m_open || m_readOnly       //  not accessible
				|| row < 0 || col < 0   //  invalid index
				|| col == 0             //  cannot change ID
				|| m_rowCount == 0)     //  no rows
			return;

		dataSave(row, false);

		//	Has anything changed?
		Object oldValue = getValueAt(row, col);
		if (!force && (
			(oldValue == null && value == null)
			||	(oldValue != null && oldValue.equals(value))
			||	(oldValue != null && value != null && oldValue.toString().equals(value.toString()))
			))
			return;

		log.debug("setValueAt r=" + row + " c=" + col + " = " + value + " (" + oldValue + ")");

		//  Save old value
		m_oldValue = new Object[3];
		m_oldValue[0] = new Integer(row);
		m_oldValue[1] = new Integer(col);
		m_oldValue[2] = oldValue;

		//	Set Data item
		MSort sort = (MSort)m_sort.get(row);
		Object[] rowData = (Object[])m_buffer.get(sort.index);
		m_rowChanged = row;

		//	Selection
		if (col == 0)
		{
			rowData[col] = value;
			m_buffer.set(sort.index, rowData);
			return;
		}

		//	save original value - shallow copy
		if (m_rowData == null)
		{
			int size = m_fields.size();
			m_rowData = new Object[size];
			for (int i = 0; i < size; i++)
				m_rowData[i] = rowData[i];
		}

		//	save & update
		rowData[col] = value;
		m_buffer.set(sort.index, rowData);
		//  update Table
		fireTableCellUpdated(row, col);
		//  update MField
		MField field = getField(col);
		field.setValue(value, m_inserting);
		//  inform
		DataStatusEvent evt = createDSE();
		evt.setChangedColumn(col);
		fireDataStatusChanged(evt);
	}	//	setValueAt

	/**
	 *  Get Old Value
	 *  @param row row
	 *  @param col col
	 *  @return old value
	 */
	public Object getOldValue (int row, int col)
	{
		if (m_oldValue == null)
			return null;
		if (((Integer)m_oldValue[0]).intValue() == row
				&& ((Integer)m_oldValue[1]).intValue() == col)
			return m_oldValue[2];
		return null;
	}   // getOldValue

	/**
	 *	Check if the current row needs to be saved.
	 *  @param  onlyRealChange if true the value of a field was actually changed
	 *  (e.g. for new records, which have not been changed) - default false
	 *	@return true it needs to be saved
	 */
	public boolean needSave(boolean onlyRealChange)
	{
		return needSave(m_rowChanged, onlyRealChange);
	}   //  needSave

	/**
	 *	Check if the row needs to be saved.
	 *  - only if nothing was changed
	 *	@return true it needs to be saved
	 */
	public boolean needSave()
	{
		return needSave(m_rowChanged, false);
	}   //  needSave

	/**
	 *	Check if the row needs to be saved.
	 *  - only when row changed
	 *  - only if nothing was changed
	 *	@param	newRow to check
	 *	@return true it needs to be saved
	 */
	public boolean needSave(int newRow)
	{
		return needSave(newRow, false);
	}   //  needSave

	/**
	 *	Check if the row needs to be saved.
	 *  - only when row changed
	 *  - only if nothing was changed
	 *	@param	newRow to check
	 *  @param  onlyRealChange if true the value of a field was actually changed
	 *  (e.g. for new records, which have not been changed) - default false
	 *	@return true it needs to be saved
	 */
	public boolean needSave(int newRow, boolean onlyRealChange)
	{
		log.debug("needSave - Row=" + newRow +
			", Changed=" + m_rowChanged + "/" + m_changed);  //  m_rowChanged set in setValueAt
		//  nothing done
		if (!m_changed && m_rowChanged == -1)
			return false;
		//  E.g. New unchanged records
		if (m_changed && m_rowChanged == -1 && onlyRealChange)
			return false;
		//  same row
		if (newRow == m_rowChanged)
			return false;

		return true;
	}	//	needSave

	/*************************************************************************/

	public static final char	SAVE_OK = 'O';			//	the only OK condition
	public static final char	SAVE_ERROR = 'E';
	public static final char	SAVE_ACCESS = 'A';
	public static final char	SAVE_MANDATORY = 'M';
	public static final char	SAVE_ABORT = 'U';

	/**
	 *	Check if it needs to be saved and save it.
	 *  @param newRow row
	 *  @param manualCmd manual command to save
	 *	@return true if not needed to be saved or successful saved
	 */
	public boolean dataSave (int newRow, boolean manualCmd)
	{
		log.debug("dataSave - Row=" + newRow +
			", Changed=" + m_rowChanged + "/" + m_changed);  //  m_rowChanged set in setValueAt
		//  nothing done
		if (!m_changed && m_rowChanged == -1)
			return true;
		//  same row, don't save yet
		if (newRow == m_rowChanged)
			return true;

		return (dataSave(manualCmd) == SAVE_OK);
	}   //  dataSave

	/**
	 *	Save unconditional.
	 *  @param manualCmd if true, no vetoable PropertyChange will be fired for save confirmation
	 *	@return OK Or Error condition
	 *  Error info (Access*, FillMandatory, SaveErrorNotUnique,
	 *  SaveErrorRowNotFound, SaveErrorDataChanged) is saved in the log
	 */
	public char dataSave (boolean manualCmd)
	{
		//	cannot save
		if (!m_open)
		{
			log.warn ("dataSave - Error - Open=" + m_open);
			return SAVE_ERROR;
		}
		//	no need - not changed - row not positioned - no Value changed
		if (m_rowChanged == -1)
		{
			log.info("dataSave - NoNeed - Changed=" + m_changed + ", Row=" + m_rowChanged);
		//	return SAVE_ERROR;
			if (!manualCmd)
				return SAVE_OK;
		}
		//  Value not changed
		if (m_rowData == null)
		{
			log.warn ("dataSave - Error - DataNull=" + (m_rowData == null));
			return SAVE_ERROR;
		}

		if (m_readOnly)
		//	If Processed - not editable (Find always editable)  -> ok for changing payment terms, etc.
		{
			log.warn("dataSave - IsReadOnly - ignored");
			dataIgnore();
			return SAVE_ACCESS;
		}

		//	row not positioned - no Value changed
		if (m_rowChanged == -1)
		{
			if (m_newRow != -1)     //  new row and nothing changed - might be OK
				m_rowChanged = m_newRow;
			else
			{
				fireDataStatusEEvent("SaveErrorNoChange", "");
				return SAVE_ERROR;
			}
		}

		//	Can we change?
		int[] co = getClientOrg(m_rowChanged);
		int AD_Client_ID = co[0]; 
		int AD_Org_ID = co[1];
		if (!MRole.getDefault(m_ctx, false).canUpdate(AD_Client_ID, AD_Org_ID, m_AD_Table_ID, true))
		{
			fireDataStatusEEvent(Log.retrieveError());
			dataIgnore();
			return SAVE_ACCESS;
		}

		log.info("dataSave - Saving row " + m_rowChanged);

		//  inform about data save action, if not manually initiated
		try
		{
			if (!manualCmd)
				m_vetoableChangeSupport.fireVetoableChange(PROPERTY, 0, m_rowChanged);
		}
		catch (PropertyVetoException pve)
		{
			log.warn("dataSave - " + pve.getMessage());
			dataIgnore();
			return SAVE_ABORT;
		}

		//	get updated row data
		MSort sort = (MSort)m_sort.get(m_rowChanged);
		Object[] rowData = (Object[])m_buffer.get(sort.index);

		//	Check Mandatory
		String missingColumns = getMandatory(rowData);
		if (missingColumns.length() != 0)
		{
			fireDataStatusEEvent("FillMandatory", missingColumns);
			return SAVE_MANDATORY;
		}

		/**
		 *	Update row *****
		 */
		int Record_ID = 0;
		if (!m_inserting)
			Record_ID = getKeyID(m_rowChanged);
		try
		{
			if (!m_tableName.endsWith("_Trl"))	//	translation tables have no model
				return dataSavePO (Record_ID);
		}
		catch (Exception e)
		{
			if (e instanceof IllegalStateException)
				log.error("MTable.dataSave - " + m_tableName + " - " + e.getLocalizedMessage());
			else
			{
				log.error("MTable.dataSave - Persistency Issue - " + m_tableName, e);
				return SAVE_ERROR;
			}
		}
		
		
		boolean error = false;
		lobReset();
		//
		String is = null;
		final String ERROR = "ERROR: ";
		final String INFO  = "Info: ";

		//	SQL with specific where clause
		String SQL = m_SQL_Select;
		StringBuffer refreshSQL = new StringBuffer(SQL).append(" WHERE ");	//	to be completed when key known
		StringBuffer singleRowWHERE = new StringBuffer();
		StringBuffer multiRowWHERE = new StringBuffer();
		//	Create SQL	& RowID
		Object rowID = null;
		if (m_inserting)
		{
			SQL += " WHERE 1=2";
		}
		else
		{
			//  FOR UPDATE causes  -  ORA-01002 fetch out of sequence
			SQL += " WHERE ROWID=?";
			rowID = getRowID (m_rowChanged);
		}
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			if (!m_inserting)
				DB.getDatabase().setRowID(pstmt, 1, rowID);
			ResultSet rs = pstmt.executeQuery();
			//	only one row
			if (!(m_inserting || rs.next()))
			{
				rs.close();
				pstmt.close();
				fireDataStatusEEvent("SaveErrorRowNotFound", "");
				dataRefresh(m_rowChanged);
				return SAVE_ERROR;
			}

			Object[] rowDataDB = null;
			//	Prepare
			boolean manualUpdate = ResultSet.CONCUR_READ_ONLY == rs.getConcurrency();
			if (manualUpdate)
				createUpdateSqlReset();
			if (m_inserting)
			{
				if (manualUpdate)
					log.debug("dataSave - prepare inserting ... manual");
				else
				{
					log.debug ("dataSave - prepare inserting ... RowSet");
					rs.moveToInsertRow ();
				}
			}
			else
			{
				log.debug("dataSave - prepare updating ... manual=" + manualUpdate);
				//	get current Data in DB
				rowDataDB = readData (rs);
			}

			/**	Data:
			 *		m_rowData	= original Data
			 *		rowData 	= updated Data
			 *		rowDataDB	= current Data in DB
			 *	1) Difference between original & updated Data?	N:next
			 *	2) Difference between original & current Data?	Y:don't update
			 *	3) Update current Data
			 *	4) Refresh to get last Data (changed by trigger, ...)
			 */

			//	Constants for Created/Updated(By)
			Timestamp now = new Timestamp(System.currentTimeMillis());
			int user = Env.getContextAsInt(m_ctx, "#AD_User_ID");

			/**
			 *	for every column
			 */
			int size = m_fields.size();
			for (int col = 0; col < size; col++)
			{
				MField field = (MField)m_fields.get (col);
				String columnName = field.getColumnName ();
			//	log.debug ("dataSave - " + columnName + "= " + m_rowData[col] + " <> DB: " + rowDataDB[col] + " -> " + rowData[col]);

				//	RowID
				if (field.getDisplayType () == DisplayType.RowID)
					; //	ignore

				//	New Key
				else if (field.isKey () && m_inserting)
				{
					if (columnName.endsWith ("_ID") || columnName.toUpperCase().endsWith ("_ID"))
					{
						int insertID = DB.getNextID (m_ctx, m_tableName, null);	//	no trx
						if (manualUpdate)
							createUpdateSql (columnName, String.valueOf (insertID));
						else
							rs.updateInt (col + 1, insertID); 						// ***
						singleRowWHERE.append (columnName).append ("=").append (insertID);
						//
						is = INFO + columnName + " -> " + insertID + " (Key)";
					}
					else //	Key with String value
					{
						String str = rowData[col].toString ();
						if (manualUpdate)
							createUpdateSql (columnName, DB.TO_STRING (str));
						else
							rs.updateString (col + 1, str); 						// ***
						singleRowWHERE = new StringBuffer();	//	overwrite
						singleRowWHERE.append (columnName).append ("=").append (DB.TO_STRING(str));
						//
						is = INFO + columnName + " -> " + str + " (StringKey)";
					}
					log.debug ("dataSave - " + is);
				} //	New Key

				//	New DocumentNo
				else if (columnName.equals ("DocumentNo"))
				{
					boolean newDocNo = false;
					String docNo = (String)rowData[col];
					//  we need to have a doc number
					if (docNo == null || docNo.length () == 0)
						newDocNo = true;
						//  Preliminary ID from CalloutSystem
					else if (docNo.startsWith ("<") && docNo.endsWith (">"))
						newDocNo = true;

					if (newDocNo || m_inserting)
					{
						String insertDoc = null;
						//  always overwrite if insering with mandatory DocType DocNo
						if (m_inserting)
							insertDoc = DB.getDocumentNo (m_ctx, m_WindowNo, 
								m_tableName, true, null);	//	only doc type - no trx
						log.debug ("dataSave - DocumentNo entered=" + docNo + ", DocTypeInsert=" + insertDoc + ", newDocNo=" + newDocNo);
						// can we use entered DocNo?
						if (insertDoc == null || insertDoc.length () == 0)
						{
							if (!newDocNo && docNo != null && docNo.length () > 0)
								insertDoc = docNo;
							else //  get a number from DocType or Table
								insertDoc = DB.getDocumentNo (m_ctx, m_WindowNo, 
									m_tableName, false, null);	//	no trx
						}
						//	There might not be an automatic document no for this document
						if (insertDoc == null || insertDoc.length () == 0)
						{
							//  in case DB function did not return a value
							if (docNo != null && docNo.length () != 0)
								insertDoc = (String)rowData[col];
							else
							{
								error = true;
								is = ERROR + field.getColumnName () + "= " + rowData[col] + " NO DocumentNo";
								log.debug ("dataSave - " + is);
								break;
							}
						}
						//
						if (manualUpdate)
							createUpdateSql (columnName, DB.TO_STRING (insertDoc));
						else
							rs.updateString (col + 1, insertDoc);					//	***
							//
						is = INFO + columnName + " -> " + insertDoc + " (DocNo)";
						log.debug ("dataSave - " + is);
					}
				}	//	New DocumentNo

				//  New Value(key)
				else if (columnName.equals ("Value") && m_inserting)
				{
					String value = (String)rowData[col];
					//  Get from Sequence, if not entered
					if (value == null || value.length () == 0)
					{
						value = DB.getDocumentNo (m_ctx, m_WindowNo, m_tableName, false, null);
						//  No Value
						if (value == null || value.length () == 0)
						{
							error = true;
							is = ERROR + field.getColumnName () + "= " + rowData[col]
								 + " No Value";
							log.debug ("dataSave - " + is);
							break;
						}
					}
					if (manualUpdate)
						createUpdateSql (columnName, DB.TO_STRING (value));
					else
						rs.updateString (col + 1, value); 							//	***
						//
					is = INFO + columnName + " -> " + value + " (Value)";
					log.debug ("dataSave - " + is);
				}	//	New Value(key)

				//	Updated		- check database
				else if (columnName.equals ("Updated"))
				{
					if (m_compareDB && !m_inserting && !m_rowData[col].equals (rowDataDB[col]))	//	changed
					{
						error = true;
						is = ERROR + field.getColumnName () + "= " + m_rowData[col]
							 + " != DB: " + rowDataDB[col];
						log.debug ("dataSave - " + is);
						break;
					}
					if (manualUpdate)
						createUpdateSql (columnName, DB.TO_DATE (now, false));
					else
						rs.updateTimestamp (col + 1, now); 							//	***
						//
					is = INFO + "Updated/By -> " + now + " - " + user;
					log.debug ("dataSave - " + is);
				} //	Updated

				//	UpdatedBy	- update
				else if (columnName.equals ("UpdatedBy"))
				{
					if (manualUpdate)
						createUpdateSql (columnName, String.valueOf (user));
					else
						rs.updateInt (col + 1, user); 								//	***
				} //	UpdatedBy

				//	Created
				else if (m_inserting && columnName.equals ("Created"))
				{
					if (manualUpdate)
						createUpdateSql (columnName, DB.TO_DATE (now, false));
					else
						rs.updateTimestamp (col + 1, now); 							//	***
				} //	Created

				//	CreatedBy
				else if (m_inserting && columnName.equals ("CreatedBy"))
				{
					if (manualUpdate)
						createUpdateSql (columnName, String.valueOf (user));
					else
						rs.updateInt (col + 1, user); 								//	***
				} //	CreatedBy

				//	Nothing changed & null
				else if (m_rowData[col] == null && rowData[col] == null)
				{
					if (m_inserting)
					{
						if (manualUpdate)
							createUpdateSql (columnName, "NULL");
						else
							rs.updateNull (col + 1); 								//	***
						is = INFO + columnName + "= NULL";
						log.debug ("dataSave - " + is);
					}
				}

				//	***	Data changed ***
				else if (m_inserting
				  || (m_rowData[col] == null && rowData[col] != null)
				  || (m_rowData[col] != null && rowData[col] == null)
				  || !m_rowData[col].equals (rowData[col])) 			//	changed
				{
					//	Original == DB
					if (m_inserting || !m_compareDB
					  || (m_rowData[col] == null && rowDataDB[col] == null)
					  || (m_rowData[col] != null && m_rowData[col].equals (rowDataDB[col])))
					{
						if (Log.isTraceLevel(10))
							log.debug("dataSave: " + columnName + "=" + rowData[col]
								+ " " + (rowData[col]==null ? "" : rowData[col].getClass().getName()));
						//
						String type = "String";
						if (rowData[col] == null)
						{
							if (manualUpdate)
								createUpdateSql (columnName, "NULL");
							else
								rs.updateNull (col + 1); 							//	***
						}
						
						//	ID - int
						else if (DisplayType.isID (field.getDisplayType()) 
							|| field.getDisplayType() == DisplayType.Integer)
						{
							int number = 0;
							try
							{
								number = Integer.parseInt (rowData[col].toString ());
								if (manualUpdate)
									createUpdateSql (columnName, String.valueOf (number));
								else
									rs.updateInt (col + 1, number); 			// 	***
							}
							catch (Exception e) //  could also be a String (AD_Language, AD_Message)
							{
								if (manualUpdate)
									createUpdateSql (columnName, DB.TO_STRING (rowData[col].toString ()));
								else
									rs.updateString (col + 1, rowData[col].toString ()); //	***
							}
							type = "Int";
						}
						//	Numeric - BigDecimal
						else if (DisplayType.isNumeric (field.getDisplayType ()))
						{
							if (manualUpdate)
								createUpdateSql (columnName, rowData[col].toString ());
							else
								rs.updateBigDecimal (col + 1, (BigDecimal)rowData[col]); //	***
							type = "Number";
						}
						//	Date - Timestamp
						else if (DisplayType.isDate (field.getDisplayType ()))
						{
							if (manualUpdate)
								createUpdateSql (columnName, DB.TO_DATE ((Timestamp)rowData[col], false));
							else
								rs.updateTimestamp (col + 1, (Timestamp)rowData[col]); //	***
							type = "Date";
						}
						//	LOB
						else if (field.getDisplayType() == DisplayType.TextLong)
						{
							PO_LOB lob = new PO_LOB (getTableName(), columnName, 
								null, field.getDisplayType(), rowData[col]);
							lobAdd(lob);
							type = "CLOB";
						}
						//	Boolean
						else if (field.getDisplayType() == DisplayType.YesNo)
						{
							String yn = null;
							if (rowData[col] instanceof Boolean)
							{
								Boolean bb = (Boolean)rowData[col];
								yn = bb.booleanValue() ? "Y" : "N";
							}
							else
								yn = "Y".equals(rowData[col]) ? "Y" : "N"; 
							if (manualUpdate)
								createUpdateSql (columnName, DB.TO_STRING (yn));
							else
								rs.updateString (col + 1, yn); //	***
						}
						//	String and others
						else	
						{
							if (manualUpdate)
								createUpdateSql (columnName, DB.TO_STRING (rowData[col].toString ()));
							else
								rs.updateString (col + 1, rowData[col].toString ()); //	***
						}
						//
						is = INFO + columnName + "= " + m_rowData[col]
							 + " -> " + rowData[col] + " (" + type + ")";
						log.debug ("dataSave - " + is);
					}
					//	Original != DB
					else
					{
						error = true;
						is = ERROR + field.getColumnName () + "= " + m_rowData[col]
							 + " != DB: " + rowDataDB[col] + " -> " + rowData[col];
						log.debug ("dataSave - " + is);
					}
				}	//	Data changed

				//	Single Key - retrieval sql
				if (field.isKey() && !m_inserting)
				{
					if (rowData[col] == null)
						throw new RuntimeException("dataSave - Key " + columnName + " is NULL");
					if (columnName.endsWith ("_ID"))
						singleRowWHERE.append (columnName).append ("=").append (rowData[col]);
					else
					{
						singleRowWHERE = new StringBuffer();	//	overwrite
						singleRowWHERE.append (columnName).append ("=").append (DB.TO_STRING(rowData[col].toString()));
					}
				}
				//	MultiKey Inserting - retrieval sql
				if (field.isParent())
				{
					if (rowData[col] == null)
						throw new RuntimeException("dataSave - MultiKey Parent " + columnName + " is NULL");
					if (multiRowWHERE.length() != 0)
						multiRowWHERE.append(" AND ");
					if (columnName.endsWith ("_ID"))
						multiRowWHERE.append (columnName).append ("=").append (rowData[col]);
					else
						multiRowWHERE.append (columnName).append ("=").append (DB.TO_STRING(rowData[col].toString()));
				}
			}	//	for every column

			if (error)
			{
				if (manualUpdate)
					createUpdateSqlReset();
				else
					rs.cancelRowUpdates();
				rs.close();
				pstmt.close();
				fireDataStatusEEvent("SaveErrorDataChanged", "");
				dataRefresh(m_rowChanged);
				return SAVE_ERROR;
			}

			/**
			 *	Save to Database
			 */
			//
			String whereClause = singleRowWHERE.toString();
			if (whereClause.length() == 0)
				whereClause = multiRowWHERE.toString();
			if (m_inserting)
			{
				log.debug("dataSave - inserting ...");
				if (manualUpdate)
				{
					String sql = createUpdateSql(true, null);
					int no = DB.executeUpdateEx(sql);
					if (no != 1)
						log.error("dataSave - insert #=" + no + " - " + sql);
				}
				else
					rs.insertRow();
			}
			else
			{
				log.debug("dataSave - updating ... " + whereClause);
				if (manualUpdate)
				{
					String sql = createUpdateSql(false, whereClause);
					int no = DB.executeUpdateEx(sql);
					if (no != 1)
						log.error("dataSave - update #=" + no + " - " + sql);
				}
				else
					rs.updateRow();
			}

			log.debug("dataSave - committing ...");
			DB.commit(true);
			//
			lobSave(whereClause);
			//	data may be updated by trigger after update
			if (m_inserting || manualUpdate)
			{
				rs.close();
				pstmt.close();
				//	need to re-read row to get ROWID, Key, DocumentNo
				log.debug("dataSave - reading ... " + whereClause);
				refreshSQL.append(whereClause);
				pstmt = DB.prepareStatement(refreshSQL.toString());
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					rowDataDB = readData(rs);
					//	update buffer
					m_buffer.set(sort.index, rowDataDB);
					fireTableRowsUpdated(m_rowChanged, m_rowChanged);
				}
				else
					log.error("dataSave - inserted row not found");
			}
			else
			{
				log.debug("dataSave - refreshing ...");
				rs.refreshRow();	//	only use
				rowDataDB = readData(rs);
				//	update buffer
				m_buffer.set(sort.index, rowDataDB);
				fireTableRowsUpdated(m_rowChanged, m_rowChanged);
			}
			//
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException e)
		{
			try
			{
				if (pstmt != null)
				  pstmt.close ();
				pstmt = null;
			}
			catch (Exception ex)
			{
			}

			String msg = "SaveError";
			if (e.getErrorCode() == 1)		//	Unique Constraint
			{
				log.error ("dataSave - Key Not Unique", e);
				msg = "SaveErrorNotUnique";
			}
			else
				log.error ("dataSave\nSQL= " + SQL, e);
			fireDataStatusEEvent(msg, e.getLocalizedMessage());
			return SAVE_ERROR;
		}

		//	everything ok
		m_rowData = null;
		m_changed = false;
		m_compareDB = true;
		m_rowChanged = -1;
		m_newRow = -1;
		m_inserting = false;
		fireDataStatusIEvent("Saved");
		//
		log.info("dataSave - fini");
		return SAVE_OK;
	}	//	dataSave

	/**
	 * 	Save via PO
	 *	@param Record_ID
	 *	@return
	 */
	private char dataSavePO (int Record_ID)
	{
		log.debug("dataSavePO - " + Record_ID);
		//
		MSort sort = (MSort)m_sort.get(m_rowChanged);
		Object[] rowData = (Object[])m_buffer.get(sort.index);
		//
		M_Table table = M_Table.get (m_ctx, m_AD_Table_ID);
		PO po = null;
		if (Record_ID != -1)
			po = table.getPO(Record_ID);
		else	//	Multi - Key
			po = table.getPO(getWhereClause(rowData));
		//	No Persistent Object
		if (po == null)
			throw new IllegalStateException("No Persistent Object");
		if (po == null)
		{
			ValueNamePair pp = Log.retrieveError();
			if (pp != null)
				fireDataStatusEEvent(pp);
			else
			{
				String msg = "SaveError";
				fireDataStatusEEvent(msg, "No Persistent Object");
			}
			return SAVE_ERROR;
		}
		
		int size = m_fields.size();
		for (int col = 0; col < size; col++)
		{
			MField field = (MField)m_fields.get (col);
			String columnName = field.getColumnName ();
			Object value = rowData[col];
			Object oldValue = m_rowData[col];
			//	RowID
			if (field.getDisplayType () == DisplayType.RowID)
				; 	//	ignore

			//	Nothing changed & null
			else if (oldValue == null && value == null)
				;	//	ignore
			
			//	***	Data changed ***
			else if (m_inserting
			  || (oldValue == null && value != null)
			  || (oldValue != null && value == null)
			  || !oldValue.equals (value)) 			//	changed
			{
				//	Check existence
				int poIndex = po.get_ColumnIndex(columnName);
				if (poIndex < 0)
				{
					log.error("dataSavePO - Column not found: " + columnName);
					continue;
				}
				//	Original == DB
				Object originalDB = po.get_Value(poIndex);
				if (m_inserting || !m_compareDB
				  || (oldValue == null && originalDB == null)
				  || (oldValue != null && oldValue.equals (originalDB)))
				{
					po.set_ValueNoCheck (columnName, value);
				}
				//	Original != DB
				else
				{
					fireDataStatusEEvent("SaveErrorDataChanged", 
						columnName 
						+ "= " + oldValue + "(" + (oldValue==null ? "-" : oldValue.getClass().getName())
						+ ") != DB: " + originalDB + "(" + (originalDB==null ? "-" : originalDB.getClass().getName())
						+ ") -> New: " + value + "(" + (value==null ? "-" : value.getClass().getName()) 
						+ ")");
					dataRefresh(m_rowChanged);
					return SAVE_ERROR;
				}
			}	//	Data changed

		}	//	for every column

		if (!po.save())
		{
			String msg = "SaveError";
			String info = "";
			ValueNamePair pp = Log.retrieveError();
			if (pp != null)
			{
				msg = pp.getValue();
				info = pp.getName();
			}
			Exception ex = Log.retrieveException();
			if (ex != null 
				&& ex instanceof SQLException
				&& ((SQLException)ex).getErrorCode() == 1)
				msg = "SaveErrorNotUnique";
			fireDataStatusEEvent(msg, info);
			return SAVE_ERROR;
		}
		
		//	Refresh - update buffer
		String whereClause = po.get_WhereClause(true);
		log.debug("dataSavePO - reading ... " + whereClause);
		StringBuffer refreshSQL = new StringBuffer(m_SQL_Select)
			.append(" WHERE ").append(whereClause);
		PreparedStatement pstmt = DB.prepareStatement(refreshSQL.toString());
		try
		{
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				Object[] rowDataDB = readData(rs);
				//	update buffer
				m_buffer.set(sort.index, rowDataDB);
				fireTableRowsUpdated(m_rowChanged, m_rowChanged);
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException e)
		{
			try
			{
				if (pstmt != null)
				  pstmt.close ();
				pstmt = null;
			}
			catch (Exception ex)
			{
			}

			String msg = "SaveError";
			log.error ("dataSavePO", e);
			fireDataStatusEEvent(msg, e.getLocalizedMessage());
			return SAVE_ERROR;
		}

		//	everything ok
		m_rowData = null;
		m_changed = false;
		m_compareDB = true;
		m_rowChanged = -1;
		m_newRow = -1;
		m_inserting = false;
		fireDataStatusIEvent("Saved");
		//
		log.info("dataSave - fini");
		return SAVE_OK;
	}	//	dataSavePO
	
	/**
	 * 	Get Where Clause
	 *	@param rowData data
	 *	@return where clause or null
	 */
	private String getWhereClause (Object[] rowData)
	{
		int size = m_fields.size();
		StringBuffer singleRowWHERE = null;
		StringBuffer multiRowWHERE = null;
		for (int col = 0; col < size; col++)
		{
			MField field = (MField)m_fields.get (col);
			if (field.isKey())
			{
				String columnName = field.getColumnName();
				Object value = rowData[col]; 
				if (value == null)
				{
					log.error("getWhereClause - PK data is null");
					return null;
				}
				if (columnName.endsWith ("_ID"))
					singleRowWHERE = new StringBuffer(columnName)
						.append ("=").append (value);
				else
					singleRowWHERE = new StringBuffer(columnName)
						.append ("=").append (DB.TO_STRING(value.toString()));
			}
			else if (field.isParent())
			{
				String columnName = field.getColumnName();
				Object value = rowData[col]; 
				if (value == null)
				{
					log.error("getWhereClause - FK data is null");
					return null;
				}
				if (multiRowWHERE == null)
					multiRowWHERE = new StringBuffer();
				else
					multiRowWHERE.append(" AND ");
				if (columnName.endsWith ("_ID"))
					multiRowWHERE.append (columnName)
						.append ("=").append (value);
				else
					multiRowWHERE.append (columnName)
						.append ("=").append (DB.TO_STRING(value.toString()));
			}
		}	//	for all columns
		if (singleRowWHERE != null)
			return singleRowWHERE.toString();
		if (multiRowWHERE != null)
			return multiRowWHERE.toString();
		log.error("getWhereClause - No key Found");
		return null;
	}	//	getWhereClause
	
	/*************************************************************************/

	private ArrayList	m_createSqlColumn = new ArrayList();
	private ArrayList	m_createSqlValue = new ArrayList();

	/**
	 * 	Prepare SQL creation
	 * 	@param columnName column name
	 * 	@param value value
	 */
	private void createUpdateSql (String columnName, String value)
	{
		m_createSqlColumn.add(columnName);
		m_createSqlValue.add(value);
		if (Log.isTraceLevel(10))
			log.debug("createUpdateSql #" + m_createSqlColumn.size()
				+ " - " + columnName + "=" + value);
	}	//	createUpdateSQL

	/**
	 * 	Create update/insert SQL
	 * 	@param insert true if insert - update otherwise
	 * 	@param whereClause where clause for update
	 * 	@return sql statement
	 */
	private String createUpdateSql (boolean insert, String whereClause)
	{
		StringBuffer sb = new StringBuffer();
		if (insert)
		{
			sb.append("INSERT INTO ").append(m_tableName).append(" (");
			for (int i = 0; i < m_createSqlColumn.size(); i++)
			{
				if (i != 0)
					sb.append(",");
				sb.append(m_createSqlColumn.get(i));
			}
			sb.append(") VALUES ( ");
			for (int i = 0; i < m_createSqlValue.size(); i++)
			{
				if (i != 0)
					sb.append(",");
				sb.append(m_createSqlValue.get(i));
			}
			sb.append(")");
		}
		else
		{
			sb.append("UPDATE ").append(m_tableName).append(" SET ");
			for (int i = 0; i < m_createSqlColumn.size(); i++)
			{
				if (i != 0)
					sb.append(",");
				sb.append(m_createSqlColumn.get(i)).append("=").append(m_createSqlValue.get(i));
			}
			sb.append(" WHERE ").append(whereClause);
		}
		log.debug("createUpdateSql=" + sb.toString());
		//	reset
		createUpdateSqlReset();
		return sb.toString();
	}	//	createUpdateSql

	/**
	 * 	Reset Update Data
	 */
	private void createUpdateSqlReset()
	{
		m_createSqlColumn = new ArrayList();
		m_createSqlValue = new ArrayList();
	}	//	createUpdateSqlReset

	/**
	 *	Get Mandatory empty columns
	 *  @param rowData row data
	 *  @return String with missing column headers/labels
	 */
	private String getMandatory(Object[] rowData)
	{
		//  see also => ProcessParameter.saveParameter
		StringBuffer sb = new StringBuffer();

		//	Check all columns
		int size = m_fields.size();
		for (int i = 0; i < size; i++)
		{
			MField field = (MField)m_fields.get(i);
			if (field.isMandatory(true))        //  check context
			{
				if (rowData[i] == null || rowData[i].toString().length() == 0)
				{
					field.setInserting (true);  //  set editable otherwise deadlock
					field.setError(true);
					if (sb.length() > 0)
						sb.append(", ");
					sb.append(field.getHeader());
				}
				else
					field.setError(false);
			}
		}

		if (sb.length() == 0)
			return "";
		return sb.toString();
	}	//	getMandatory

	/*************************************************************************/

	/**	LOB Info				*/
	private ArrayList		m_lobInfo = null;

	/**
	 * 	Reset LOB info
	 */
	private void lobReset()
	{
		m_lobInfo = null;
	}	//	resetLOB
	
	/**
	 * 	Prepare LOB save
	 *	@param value value 
	 *	@param index index
	 *	@param displayType display type
	 */	
	private void lobAdd (PO_LOB lob)
	{
		log.debug("lobAdd - " + lob);
		if (m_lobInfo == null)
			m_lobInfo = new ArrayList();
		m_lobInfo.add(lob);
	}	//	lobAdd
	
	/**
	 * 	Save LOB
	 */
	private void lobSave (String whereClause)
	{
		if (m_lobInfo == null)
			return;
		for (int i = 0; i < m_lobInfo.size(); i++)
		{
			PO_LOB lob = (PO_LOB)m_lobInfo.get(i);
			lob.save(whereClause);
		}	//	for all LOBs
		lobReset();
	}	//	lobSave

	
	/**************************************************************************
	 *	New Record after current Row
	 *  @param currentRow row
	 *  @param copyCurrent copy
	 *  @return true if success -
	 *  Error info (Access*, AccessCannotInsert) is saved in the log
	 */
	public boolean dataNew (int currentRow, boolean copyCurrent)
	{
		log.info("dataNew - Current=" + currentRow + ", Copy=" + copyCurrent);
		//  Read only
		if (m_readOnly)
		{
			fireDataStatusEEvent("AccessCannotInsert", "");
			return false;
		}

		/** @todo No TableLevel */
		//  || !Access.canViewInsert(m_ctx, m_WindowNo, tableLevel, true, true))
		//  fireDataStatusEvent(Log.retrieveError());

		//  see if we need to save
		dataSave(-2, false);


		m_inserting = true;
		//	Create default data
		int size = m_fields.size();
		m_rowData = new Object[size];	//	"original" data
		Object[] rowData = new Object[size];
		//	fill data
		if (copyCurrent)
		{
			MSort sort = (MSort) m_sort.get(currentRow);
			Object[] origData = (Object[])m_buffer.get(sort.index);
			for (int i = 0; i < size; i++)
				rowData[i] = origData[i];
		}
		else	//	new
		{
			for (int i = 0; i < size; i++)
			{
				MField field = (MField)m_fields.get(i);
				rowData[i] = field.getDefault();
				field.setValue(rowData[i], m_inserting);
			}
		}
		m_changed = true;
		m_compareDB = true;
		m_rowChanged = -1;  //  only changed in setValueAt
		m_newRow = currentRow + 1;
		//  if there is no record, the current row could be 0 (and not -1)
		if (m_buffer.size() < m_newRow)
			m_newRow = m_buffer.size();

		//	add Data at end of buffer
		MSort sort = new MSort(m_buffer.size(), null);	//	index
		m_buffer.add(rowData);
		//	add Sort pointer
		m_sort.add(m_newRow, sort);
		m_rowCount++;

		//	inform
		log.debug("dataNew - Current=" + currentRow + ", New=" + m_newRow);
		fireTableRowsInserted(m_newRow, m_newRow);
		fireDataStatusIEvent(copyCurrent ? "UpdateCopied" : "Inserted");
		log.debug("dataNew - Current=" + currentRow + ", New=" + m_newRow + " - complete");
		return true;
	}	//	dataNew


	/**************************************************************************
	 *	Delete Data
	 *  @param row row
	 *  @return true if success -
	 *  Error info (Access*, AccessNotDeleteable, DeleteErrorDependent,
	 *  DeleteError) is saved in the log
	 */
	public boolean dataDelete (int row)
	{
		log.info("dataDelete - " + row);
		if (row < 0)
			return false;
		Object rowID = getRowID(row);
		if (rowID == null)
			return false;

		//	Is this record deletable?
		if (!m_deleteable)
		{
			fireDataStatusEEvent("AccessNotDeleteable", "");	//	audit
			return false;
		}

		//	Tab R/O
		if (m_readOnly)
		{
			fireDataStatusEEvent("AccessCannotDelete", "");		//	previleges
			return false;
		}

		//	Processed Column and not an Import Table
		if (m_indexProcessedColumn > 0 && !m_tableName.startsWith("I_"))
		{
			Boolean processed = (Boolean)getValueAt(row, m_indexProcessedColumn);
			if (processed != null && processed.booleanValue())
			{
				fireDataStatusEEvent("CannotDeleteTrx", "");
				return false;
			}
		}
		

		/** @todo check Access */
		//  fireDataStatusEvent(Log.retrieveError());

		MSort sort = (MSort)m_sort.get(row);
		Object[] rowData = (Object[])m_buffer.get(sort.index);
		//
		M_Table table = M_Table.get (m_ctx, m_AD_Table_ID);
		PO po = null;
		int Record_ID = getKeyID(m_rowChanged);
		if (Record_ID != -1)
			po = table.getPO(Record_ID);
		else	//	Multi - Key
			po = table.getPO(getWhereClause(rowData));
		
		//	Delete via PO 
		if (po != null)
		{
			if (!po.delete(false))
			{
				ValueNamePair vp = Log.retrieveError();
				if (vp != null)
					fireDataStatusEEvent(vp);
				else
					fireDataStatusEEvent("DeleteError", "");
				return false;
			}
		}
		else	//	Delete via SQL
		{
			StringBuffer SQL = new StringBuffer("DELETE ");
			SQL.append(m_tableName).append(" WHERE ROWID=?");
			int no = 0;
			try
			{
				PreparedStatement pstmt = DB.prepareStatement(SQL.toString());
				DB.getDatabase().setRowID(pstmt, 1, rowID);
				no = pstmt.executeUpdate();
				pstmt.close();
			}
			catch (SQLException e)
			{
				log.error ("dataDelete", e);
				String msg = "DeleteError";
				if (e.getErrorCode() == 2292)	//	Child Record Found
					msg = "DeleteErrorDependent";
				fireDataStatusEEvent(msg, e.getLocalizedMessage());
				return false;
			}
			//	Check Result
			if (no != 1)
			{
				log.error("dataDelete - Number of deleted rows = " + no);
				return false;
			}
		}

		//	Get Sort
		int bufferRow = sort.index;
		//	Delete row in Buffer and shifts all below up
		m_buffer.remove(bufferRow);
		m_rowCount--;

		//	Delete row in Sort
		m_sort.remove(row);
		//	Correct pointer in Sort
		for (int i = 0; i < m_sort.size(); i++)
		{
			MSort ptr = (MSort)m_sort.get(i);
			if (ptr.index > bufferRow)
				ptr.index--;	//	move up
		}

		//	inform
		m_changed = false;
		m_rowChanged = -1;
		fireTableRowsDeleted(row, row);
		fireDataStatusIEvent("Deleted");
		log.debug("dataDelete - " + row + " complete");
		return true;
	}	//	dataDelete

	
	/**************************************************************************
	 *	Ignore changes
	 */
	public void dataIgnore()
	{
		log.info("dataIgnore - Inserting=" + m_inserting);
		if (!m_inserting && !m_changed && m_rowChanged < 0)
		{
			log.debug("dataIgnore - Nothing to ignore");
			return;
		}

		//	Inserting - delete new row
		if (m_inserting)
		{
			//	Get Sort
			MSort sort = (MSort)m_sort.get(m_newRow);
			int bufferRow = sort.index;
			//	Delete row in Buffer and shifts all below up
			m_buffer.remove(bufferRow);
			m_rowCount--;
			//	Delete row in Sort
			m_sort.remove(m_newRow);	//	pintint to the last column, so no adjustment
			//
			m_changed = false;
			m_rowData = null;
			m_rowChanged = -1;
			m_inserting = false;
			//	inform
			fireTableRowsDeleted(m_newRow, m_newRow);
		}
		else
		{
			//	update buffer
			if (m_rowData != null)
			{
				MSort sort = (MSort)m_sort.get(m_rowChanged);
				m_buffer.set(sort.index, m_rowData);
			}
			m_changed = false;
			m_rowData = null;
			m_rowChanged = -1;
			m_inserting = false;
			//	inform
		//	fireTableRowsUpdated(m_rowChanged, m_rowChanged); >> messes up display?? (clearSelection)
		}
		m_newRow = -1;
		fireDataStatusIEvent("Ignored");
	}	//	dataIgnore


	/**
	 *	Refresh Row - ignore changes
	 *  @param row row
	 */
	public void dataRefresh (int row)
	{
		log.info("dataRefresh " + row);

		if (row < 0)
			return;
		Object rowID = getRowID(row);
		if (rowID == null)
			return;

		//  ignore
		dataIgnore();

		//	Create SQL
		String SQL = m_SQL_Select + " WHERE ROWID=?";
		MSort sort = (MSort)m_sort.get(row);
		Object[] rowDataDB = null;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(SQL);
			DB.getDatabase().setRowID(pstmt, 1, rowID);
			ResultSet rs = pstmt.executeQuery();
			//	only one row
			if (rs.next())
				rowDataDB = readData(rs);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.error ("dataRefresh\nSQL=" + SQL, e);
			fireTableRowsUpdated(row, row);
			fireDataStatusEEvent("RefreshError", "");
			return;
		}

		//	update buffer
		m_buffer.set(sort.index, rowDataDB);
		//	info
		m_rowData = null;
		m_changed = false;
		m_rowChanged = -1;
		m_inserting = false;
		fireTableRowsUpdated(row, row);
		fireDataStatusIEvent("Refreshed");
	}	//	dataRefresh


	/**
	 *	Refresh all Rows - ignore changes
	 */
	public void dataRefreshAll()
	{
		log.info("dataRefreshAll");
		dataIgnore();
		close(false);
		open();
		//	Info
		m_rowData = null;
		m_changed = false;
		m_rowChanged = -1;
		m_inserting = false;
		fireTableDataChanged();
		fireDataStatusIEvent("Refreshed");
	}	//	dataRefreshAll


	/**
	 *	Requery with new whereClause
	 *  @param whereClause sql where clause
	 *  @param onlyCurrentRows only current rows
	 *  @param onlyCurrentDays how many days back
	 *  @return true if success
	 */
	public boolean dataRequery (String whereClause, boolean onlyCurrentRows, int onlyCurrentDays)
	{
		log.info("dataRequery - " + whereClause + "; OnlyCurrent=" + onlyCurrentRows);
		close(false);
		m_onlyCurrentDays = onlyCurrentDays;
		setWhereClause(whereClause, onlyCurrentRows, m_onlyCurrentDays);
		open();
		//  Info
		m_rowData = null;
		m_changed = false;
		m_rowChanged = -1;
		m_inserting = false;
		fireTableDataChanged();
		fireDataStatusIEvent("Refreshed");
		return true;
	}	//	dataRequery


	/**************************************************************************
	 *	Is Cell Editable.
	 *	Is queried from JTable before checking VCellEditor.isCellEditable
	 *  @param  row the row index being queried
	 *  @param  col the column index being queried
	 *  @return true, if editable
	 */
	public boolean isCellEditable (int row, int col)
	{
	//	Log.trace(Log.l6_Database, "MTable.isCellEditable - Row=" + row + ", Col=" + col);
		//	Make Rows selectable
		if (col == 0)
			return true;

		//	Entire Table not editable
		if (m_readOnly)
			return false;
		//	Key & ID not editable
		if (col == m_indexRowIDColumn || col == m_indexKeyColumn)
			return false;
		/** @todo check link columns */

		//	Check column range
		if (col < 0 && col >= m_fields.size())
			return false;
		//  IsActive Column always editable if no processed exists
		if (col == m_indexActiveColumn && m_indexProcessedColumn == -1)
			return true;
		//	Row
		if (!isRowEditable(row))
			return false;

		//	Column
		return ((MField)m_fields.get(col)).isEditable(false);
	}	//	IsCellEditable


	/**
	 *	Is Current Row Editable
	 *  @param row row
	 *  @return true if editable
	 */
	public boolean isRowEditable (int row)
	{
	//	Log.trace(Log.l6_Database, "MTable.isRowEditable - Row=" + row);
		//	Entire Table not editable or no row
		if (m_readOnly || row < 0)
			return false;
		//	If not Active - not editable
		if (m_indexActiveColumn > 0)		//	&& m_TabNo != Find.s_TabNo)
		{
			Object value = getValueAt(row, m_indexActiveColumn);
			if (value instanceof Boolean)
			{
				if (!((Boolean)value).booleanValue())
					return false;
			}
			else if ("N".equals(value)) 
				return false;
		}
		//	If Processed - not editable (Find always editable)
		if (m_indexProcessedColumn > 0)		//	&& m_TabNo != Find.s_TabNo)
		{
			Object value = getValueAt(row, m_indexProcessedColumn); 
			if (value instanceof Boolean)
			{
				if (!((Boolean)value).booleanValue())
					return false;
			}
			else if ("N".equals(value)) 
				return false;
		}
		//
		int[] co = getClientOrg(row);
		int AD_Client_ID = co[0]; 
		int AD_Org_ID = co[1];
		return MRole.getDefault(m_ctx, false).canUpdate(AD_Client_ID, AD_Org_ID, m_AD_Table_ID, false);
	}	//	isRowEditable

	/**
	 * 	Get Client Org for row
	 *	@param row row
	 *	@return array [0] = Client [1] = Org - a value of -1 is not defined/found
	 */
	private int[] getClientOrg (int row)
	{
		int AD_Client_ID = -1;
		if (m_indexClientColumn != -1)
		{
			Integer ii = (Integer)getValueAt(row, m_indexClientColumn);
			if (ii != null)
				AD_Client_ID = ii.intValue();
		}
		int AD_Org_ID = 0;
		if (m_indexOrgColumn != -1)
		{
			Integer ii = (Integer)getValueAt(row, m_indexOrgColumn);
			if (ii != null)
				AD_Org_ID = ii.intValue();
		}
		return new int[] {AD_Client_ID, AD_Org_ID};
	}	//	getClientOrg

	/**
	 *	Set entire table as read only
	 *  @param value new read only value
	 */
	public void setReadOnly (boolean value)
	{
		log.debug("setReadOnly " + value);
		m_readOnly = value;
	}	//	setReadOnly

	/**
	 *  Is entire Table Read/Only
	 *  @return true if read only
	 */
	public boolean isReadOnly()
	{
		return m_readOnly;
	}   //  isReadOnly

	/**
	 *  Is inserting
	 *  @return true if inserting
	 */
	public boolean isInserting()
	{
		return m_inserting;
	}   //  isInserting

	/**
	 *	Set Compare DB.
	 * 	If Set to false, save overwrites the record, regardless of DB changes.
	 *  (When a payment is changed in Sales Order, the payment reversal clears the payment id)
	 * 	@param compareDB compare DB - false forces overwrite
	 */
	public void setCompareDB (boolean compareDB)
	{
		m_compareDB = compareDB;
	}  	//	setCompareDB

	/**
	 *	Get Compare DB.
	 * 	@return false if save overwrites the record, regardless of DB changes
	 * 	(false forces overwrite).
	 */
	public boolean getCompareDB ()
	{
		return m_compareDB;
	}  	//	getCompareDB


	/**
	 *	Can Table rows be deleted
	 *  @param value new deleteable value
	 */
	public void setDeleteable (boolean value)
	{
		log.debug("setDeleteable " + value);
		m_deleteable = value;
	}	//	setDeleteable

	
	/**************************************************************************
	 *	Read Data from Recordset
	 *  @param rs result set
	 *  @return Data Array
	 */
	private Object[] readData (ResultSet rs)
	{
		int size = m_fields.size();
		Object[] rowData = new Object[size];
		String columnName = null;
		int displayType = 0;

		//	Types see also MField.createDefault
		try
		{
			//	get row data
			for (int j = 0; j < size; j++)
			{
				//	Column Info
				MField field = (MField)m_fields.get(j);
				columnName = field.getColumnName();
				displayType = field.getDisplayType();
				//	Integer, ID, Lookup (UpdatedBy is a numeric column)
				if (displayType == DisplayType.Integer
					|| (DisplayType.isID(displayType) && (columnName.endsWith("_ID") || columnName.endsWith("_Acct"))) 
					|| columnName.endsWith("atedBy"))
				{
					rowData[j] = new Integer(rs.getInt(j+1));	//	Integer
					if (rs.wasNull())
						rowData[j] = null;
				}
				//	Number
				else if (DisplayType.isNumeric(displayType))
					rowData[j] = rs.getBigDecimal(j+1);			//	BigDecimal
				//	Date
				else if (DisplayType.isDate(displayType))
					rowData[j] = rs.getTimestamp(j+1);			//	Timestamp
				//	RowID or Key (and Selection)
				else if (displayType == DisplayType.RowID)
				{
					Object[] rid = new Object[3];
					if (columnName.equals("ROWID"))
						rid[0] = DB.getDatabase().getRowID(rs, j+1);
					else
						rid[2] = new Integer (rs.getInt(j+1));
					rid[1] = new Boolean(false);
					rowData[j] = rid;
				}
				//	YesNo
				else if (displayType == DisplayType.YesNo)
					rowData[j] = new Boolean ("Y".equals(rs.getString(j+1)));	//	Boolean			
				//	LOB
				else if (displayType == DisplayType.TextLong)
				{
					Object value = rs.getObject(j+1);
					if (rs.wasNull())
						rowData[j] = null;
					else if (value instanceof Clob) 
					{
						Clob lob = (Clob)value;
						long length = lob.length();
						rowData[j] = lob.getSubString(1, (int)length);
					}
				}
				//	String
				else
					rowData[j] = rs.getString(j+1);				//	String
			}
		}
		catch (SQLException e)
		{
			log.error("readData - " + columnName + ", DT=" + displayType, e);
		}
		return rowData;
	}	//	readData

	
	/**************************************************************************
	 *	Remove Data Status Listener
	 *  @param l listener
	 */
	public synchronized void removeDataStatusListener(DataStatusListener l)
	{
		if (m_dataStatusListeners != null && m_dataStatusListeners.contains(l))
		{
			Vector v = (Vector) m_dataStatusListeners.clone();
			v.removeElement(l);
			m_dataStatusListeners = v;
		}
	}	//	removeDataStatusListener

	/**
	 *	Add Data Status Listener
	 *  @param l listener
	 */
	public synchronized void addDataStatusListener(DataStatusListener l)
	{
		Vector v = m_dataStatusListeners == null ? new Vector(2) : (Vector) m_dataStatusListeners.clone();
		if (!v.contains(l))
		{
			v.addElement(l);
			m_dataStatusListeners = v;
		}
	}	//	addDataStatusListener

	/**
	 *	Inform Listeners
	 *  @param e event
	 */
	private void fireDataStatusChanged (DataStatusEvent e)
	{
		if (m_dataStatusListeners != null)
		{
			Vector listeners = m_dataStatusListeners;
			int count = listeners.size();
			for (int i = 0; i < count; i++)
				((DataStatusListener) listeners.elementAt(i)).dataStatusChanged(e);
		}
	}	//	fireDataStatusChanged

	/**
	 *  Create Data Status Event
	 *  @return data status event
	 */
	private DataStatusEvent createDSE()
	{
		boolean changed = m_changed;
		if (m_rowChanged != -1)
			changed = true;
		DataStatusEvent dse = new DataStatusEvent(this, m_rowCount, changed,
			Env.isAutoCommit(m_ctx, m_WindowNo), m_inserting);
		return dse;
	}   //  createDSE

	/**
	 *  Create and fire Data Status Info Event
	 *  @param AD_Message message
	 */
	protected void fireDataStatusIEvent (String AD_Message)
	{
		DataStatusEvent e = createDSE();
		e.setInfo(AD_Message, "", false);
		fireDataStatusChanged (e);
	}   //  fireDataStatusEvent

	/**
	 *  Create and fire Data Status Error Event
	 *  @param AD_Message message
	 *  @param info info
	 */
	protected void fireDataStatusEEvent (String AD_Message, String info)
	{
	//	org.compiere.util.Trace.printStack();
		//
		DataStatusEvent e = createDSE();
		e.setInfo(AD_Message, info, true);
		Log.saveError(AD_Message, info);
		fireDataStatusChanged (e);
	}   //  fireDataStatusEvent

	/**
	 *  Create and fire Data Status Event (from Error Log)
	 *  @param errorLog error log info
	 */
	protected void fireDataStatusEEvent (ValueNamePair errorLog)
	{
		if (errorLog != null)
			fireDataStatusEEvent (errorLog.getValue(), errorLog.getName());
	}   //  fireDataStatusEvent

	
	/**************************************************************************
	 *  Remove Vetoable change listener for row changes
	 *  @param l listener
	 */
	public synchronized void removeVetoableChangeListener(VetoableChangeListener l)
	{
		m_vetoableChangeSupport.removeVetoableChangeListener(l);
	}   //  removeVetoableChangeListener

	/**
	 *  Add Vetoable change listener for row changes
	 *  @param l listener
	 */
	public synchronized void addVetoableChangeListener(VetoableChangeListener l)
	{
		m_vetoableChangeSupport.addVetoableChangeListener(l);
	}   //  addVetoableChangeListener

	/**
	 *  Fire Vetoable change listener for row changes
	 *  @param e event
	 *  @throws PropertyVetoException
	 */
	protected void fireVetoableChange(PropertyChangeEvent e) throws java.beans.PropertyVetoException
	{
		m_vetoableChangeSupport.fireVetoableChange(e);
	}   //  fireVetoableChange

	/**
	 *  toString
	 *  @return String representation
	 */
	public String toString()
	{
		return new StringBuffer("MTable[").append(m_tableName)
			.append(",WindowNo=").append(m_WindowNo)
			.append(",Tab=").append(m_TabNo).append("]").toString();
	}   //  toString


	
	/**************************************************************************
	 *	ASync Loader
	 */
	class Loader extends Thread implements Serializable
	{
		/**
		 *  Construct Loader
		 */
		public Loader()
		{
			super("TLoader");
		}	//	Loader

		private PreparedStatement   m_pstmt = null;
		private ResultSet 		    m_rs = null;

		/**
		 *	Open ResultSet
		 *	@return number of records
		 */
		protected int open()
		{
		//	Log.trace(Log.l4_Data, "MTable Loader.open");
			//	Get Number of Rows
			int rows = 0;
			try
			{
				PreparedStatement pstmt = DB.prepareStatement(m_SQL_Count);
				setParameter (pstmt, true);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next())
					rows = rs.getInt(1);
				rs.close();
				pstmt.close();
			}
			catch (SQLException e0)
			{
				//	Zoom Query may have invalid where clause
				if (e0.getErrorCode() == 904) 	//	ORA-00904: "C_x_ID": invalid identifier
					log.warn("Loader.open Count - " + e0.getLocalizedMessage() + "\nSQL=" + m_SQL_Count);
				else
					log.error ("Loader.open Count SQL=" + m_SQL_Count, e0);
				return 0;
			}

			//	open Statement (closed by Loader.close)
			try
			{
				m_pstmt = DB.prepareStatement(m_SQL);
			//	m_pstmt.setFetchSize(20);
				setParameter (m_pstmt, false);
				m_rs = m_pstmt.executeQuery();
			}
			catch (SQLException e)
			{
				log.error ("Loader.open\nFull SQL=" + m_SQL, e);
				return 0;
			}
			StringBuffer info = new StringBuffer("Rows=");
			info.append(rows);
			if (rows == 0)
				info.append(" - ").append(m_SQL_Count);
			log.debug("Loader.open - " + info.toString());
			return rows;
		}	//	open

		/**
		 *	Close RS and Statement
		 */
		private void close()
		{
		//	Log.trace(Log.l4_Data, "MTable Loader.close");
			try
			{
				if (m_rs != null)
					m_rs.close();
				if (m_pstmt != null)
					m_pstmt.close();
			}
			catch (SQLException e)
			{
				log.error ("Loader.closeRS", e);
			}
			m_rs = null;
			m_pstmt = null;
		}	//	close

		/**
		 *	Fill Buffer to include Row
		 */
		public void run()
		{
			log.info("Loader.run");
			if (m_rs == null)
				return;

			try
			{
				while(m_rs.next())
				{
					if (this.isInterrupted())
					{
						log.debug("Loader interrupted");
						close();
						return;
					}
					//  Get Data
					Object[] rowData = readData(m_rs);
					//	add Data
					MSort sort = new MSort(m_buffer.size(), null);	//	index
					m_buffer.add(rowData);
					m_sort.add(sort);

					//	Statement all 250 rows & sleep
					if (m_buffer.size() % 250 == 0)
					{
						//	give the other processes a chance
						try
						{
							yield();
							sleep(10);		//	.01 second
						}
						catch (InterruptedException ie)
						{
							log.debug("Loader interrupted while sleeping");
							close();
							return;
						}
						DataStatusEvent evt = createDSE();
						evt.setLoading(m_buffer.size());
						fireDataStatusChanged(evt);
					}
				}	//	while(rs.next())
			}
			catch (SQLException e)
			{
				log.error ("Loader.run", e);
			}
			close();
			fireDataStatusIEvent("");
		}	//	run

		/**
		 *	Set Parameter for Query.
		 *		elements must be Integer, BigDecimal, String (default)
		 *  @param pstmt prepared statement
		 *  @param countSQL count
		 */
		private void setParameter (PreparedStatement pstmt, boolean countSQL)
		{
			if (m_parameterSELECT.size() == 0 && m_parameterWHERE.size() == 0)
				return;
			try
			{
				int pos = 1;	//	position in Statement
				//	Select Clause Parameters
				for (int i = 0; !countSQL && i < m_parameterSELECT.size(); i++)
				{
					Object para = m_parameterSELECT.get(i);
					if (para != null)
						log.debug("setParameter Select " + i + "=" + para);
					//
					if (para == null)
						;
					else if (para instanceof Integer)
					{
						Integer ii = (Integer)para;
						pstmt.setInt (pos++, ii.intValue());
					}
					else if (para instanceof BigDecimal)
						pstmt.setBigDecimal (pos++, (BigDecimal)para);
					else
						pstmt.setString(pos++, para.toString());
				}
				//	Where Clause Parameters
				for (int i = 0; i < m_parameterWHERE.size(); i++)
				{
					Object para = m_parameterWHERE.get(i);
					if (para != null)
						log.debug("setParameter Where " + i + "=" + para);
					//
					if (para == null)
						;
					else if (para instanceof Integer)
					{
						Integer ii = (Integer)para;
						pstmt.setInt (pos++, ii.intValue());
					}
					else if (para instanceof BigDecimal)
						pstmt.setBigDecimal (pos++, (BigDecimal)para);
					else
						pstmt.setString(pos++, para.toString());
				}
			}
			catch (SQLException e)
			{
				log.error("Loader.setParameter", e);
			}
		}	//	setParameter

	}	//	Loader

}	//	MTable
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

import javax.swing.table.*;
import javax.swing.event.*;

import java.sql.*;
import java.util.*;
import java.math.*;
import java.beans.*;
import java.io.Serializable;

import org.compiere.util.*;

/**
 *	Grid Table Model for JDBC access including buffering.
 *  <pre>
 *		The following data types are handeled
 *			Integer		for all IDs
 *			BigDecimal	for all Numbers
 *			Timestamp	for all Dates
 *			String		for all others
 *  The data is read via r/o resultset and cached in m_buffer. Writes/updates
 *  are via dynamically constructed SQL INSERT/UPDATE statements. The record
 *  is re-read via the resultset to get results of triggers.
 *
 *  </pre>
 *  The model maintains and fires the requires TableModelEvent changes,
 *  the DataChanged events (loading, changed, etc.)
 *  as well as Vetoable Change event "RowChange"
 *  (for row changes initiated by moving the row in the table grid).
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: MTable.java,v 1.62 2004/05/13 06:05:21 jjanke Exp $
 */
public final class MTable extends AbstractTableModel
	implements Serializable
{
	/**
	 *	JDBC Based Buffered Table
	 *
	 *  @param ctx Properties
	 *  @param TableName table name
	 *  @param WindowNo window no
	 *  @param TabNo tab no
	 *  @param withAccessControl    if true adds AD_Client/Org restrictuins
	 */
	public MTable(Properties ctx, int AD_Table_ID, String TableName, int WindowNo, int TabNo,
		boolean withAccessControl)
	{
		super();
		log.info(TableName);
		m_ctx = ctx;
		m_AD_Table_ID = AD_Table_ID;
		setTableName(TableName);
		m_WindowNo = WindowNo;
		m_TabNo = TabNo;
		m_withAccessControl = withAccessControl;
	}	//	MTable

	private Logger				log = Logger.getCLogger(getClass());
	private Properties          m_ctx;
	private int					m_AD_Table_ID;
	private String 		        m_tableName = "";
	private int				    m_WindowNo;
	private int				    m_TabNo;
	private boolean			    m_withAccessControl;
	private boolean			    m_readOnly = true;
	private boolean			    m_deleteable = true;
	//

	/**	Rowcount                    */
	private int				    m_rowCount = 0;
	/**	Has Data changed?           */
	private boolean			    m_changed = false;
	/** Index of changed row via SetValueAt */
	private int				    m_rowChanged = -1;
	/** Insert mode active          */
	private boolean			    m_inserting = false;
	/** Inserted Row number         */
	private int                 m_newRow = -1;
	/**	Is the Resultset open?      */
	private boolean			    m_open = false;
	/**	Compare to DB before save	*/
	private boolean				m_compareDB = true;		//	set to true after every save

	//	The buffer for all data
	private volatile ArrayList	m_buffer = new ArrayList(100);
	private volatile ArrayList	m_sort = new ArrayList(100);
	/** Original row data               */
	private Object[]			m_rowData = null;
	/** Original data [row,col,data]    */
	private Object[]            m_oldValue = null;
	//
	private Loader		        m_loader = null;

	/**	Columns                 		*/
	private ArrayList	        m_fields = new ArrayList(30);
	private ArrayList 	        m_parameterSELECT = new ArrayList(5);
	private ArrayList 	        m_parameterWHERE = new ArrayList(5);

	/** Complete SQL statement          */
	private String 		        m_SQL;
	/** SQL Statement for Row Count     */
	private String 		        m_SQL_Count;
	/** The SELECT clause with FROM     */
	private String 		        m_SQL_Select;
	/** The static where clause         */
	private String 		        m_whereClause = "";
	/** Show only Processed='N' and last 24h records    */
	private boolean		        m_onlyCurrentRows = false;
	/** Show only Not processed and x days				*/
	private int					m_onlyCurrentDays = 1;
	/** Static ORDER BY clause          */
	private String		        m_orderClause = "";

	/** Index of Key Column                 */
	private int			        m_indexKeyColumn = -1;
	/** Index of RowID column               */
	private int                 m_indexRowIDColumn = -1;
	/** Index of Color Column               */
	private int			        m_indexColorColumn = -1;
	/** Index of Processed Column           */
	private int                 m_indexProcessedColumn = -1;
	/** Index of IsActive Column            */
	private int                 m_indexActiveColumn = -1;
	/** Index of AD_Client_ID Column        */
	private int					m_indexClientColumn = -1;
	/** Index of AD_Org_ID Column           */
	private int					m_indexOrgColumn = -1;

	/** List of DataStatus Listeners    */
	private Vector 		        m_dataStatusListeners;
	/** Vetoable Change Bean support    */
	private VetoableChangeSupport   m_vetoableChangeSupport = new VetoableChangeSupport(this);
	/** Property of Vetoable Bean support "RowChange" */
	public static final String  PROPERTY = "MTable-RowSave";

	/**
	 *	Set Table Name
	 *  @param newTableName table name
	 */
	public void setTableName(String newTableName)
	{
		if (m_open)
		{
			log.error("setTableName - Table already open - ignored");
			return;
		}
		if (newTableName == null || newTableName.length() == 0)
			return;
		m_tableName = newTableName;
	}	//	setTableName

	/**
	 *	Get Table Name
	 *  @return table name
	 */
	public String getTableName()
	{
		return m_tableName;
	}	//	getTableName

	/**
	 *	Set Where Clause (w/o the WHERE and w/o History).
	 *  @param newWhereClause sql where clause
	 *  @param onlyCurrentRows only current rows
	 *  @param onlyCurrentDays how many days back for current
	 *	@return true if where clase set
	 */
	public boolean setWhereClause(String newWhereClause, boolean onlyCurrentRows, int onlyCurrentDays)
	{
		if (m_open)
		{
			log.error("setWhereClause - Table already open - ignored");
			return false;
		}
		//
		m_whereClause = newWhereClause;
		m_onlyCurrentRows = onlyCurrentRows;
		m_onlyCurrentDays = onlyCurrentDays;
		if (m_whereClause == null)
			m_whereClause = "";
		return true;
	}	//	setWhereClause

	/**
	 *	Get Where Clause (w/o the WHERE and w/o History)
	 *  @return where clause
	 */
	public String getWhereClause()
	{
		return m_whereClause;
	}	//	getWhereClause

	/**
	 *	Is History displayed
	 *  @return true if history displayed
	 */
	public boolean isOnlyCurrentRowsDisplayed()
	{
		return !m_onlyCurrentRows;
	}	//	isHistoryDisplayed

	/**
	 *	Set Order Clause (w/o the ORDER BY)
	 *  @param newOrderClause sql order by clause
	 */
	public void setOrderClause(String newOrderClause)
	{
		m_orderClause = newOrderClause;
		if (m_orderClause == null)
			m_orderClause = "";
	}	//	setOrderClause

	/**
	 *	Get Order Clause (w/o the ORDER BY)
	 *  @return order by clause
	 */
	public String getOrderClause()
	{
		return m_orderClause;
	}	//	getOrderClause

	/**
	 *	Assemble & store
	 *	m_SQL and m_countSQL
	 *  @return m_SQL
	 */
	private String createSelectSql()
	{
		if (m_fields.size() == 0 || m_tableName == null || m_tableName.equals(""))
			return "";

		//	Create SELECT Part
		StringBuffer select = new StringBuffer("SELECT ");
		for (int i = 0; i < m_fields.size(); i++)
		{
			if (i > 0)
				select.append(",");
			MField field = (MField)m_fields.get(i);
			select.append(field.getColumnName());
		}
		//
		select.append(" FROM ").append(m_tableName);
		m_SQL_Select = select.toString();
		m_SQL_Count = "SELECT COUNT(*) FROM " + m_tableName;
		//

		StringBuffer where = new StringBuffer("");
		//	WHERE
		if (m_whereClause.length() > 0)
		{
			where.append(" WHERE ");
			if (m_whereClause.indexOf("@") == -1)
				where.append(m_whereClause);
			else    //  replace variables
				where.append(Env.parseContext(m_ctx, m_WindowNo, m_whereClause, false));
		}
		if (m_onlyCurrentRows)
		{
			if (where.toString().indexOf(" WHERE ") == -1)
				where.append(" WHERE ");
			else
				where.append(" AND ");
			//	Show only unprocessed or the one updated within x days
			where.append("(Processed='N' OR Updated>SysDate-").append(m_onlyCurrentDays).append(")");
		}

		//	RO/RW Access
		m_SQL = m_SQL_Select + where.toString();
		m_SQL_Count += where.toString();
		if (m_withAccessControl)
		{
			boolean ro = MRole.SQL_RO;
		//	if (!m_readOnly)
		//		ro = MRole.SQL_RW;
			m_SQL = MRole.getDefault(m_ctx, false).addAccessSQL(m_SQL, 
				m_tableName, MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO);
			m_SQL_Count = MRole.getDefault(m_ctx, false).addAccessSQL(m_SQL_Count, 
				m_tableName, MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO);
		}

		//	ORDER BY
		if (!m_orderClause.equals(""))
			m_SQL += " ORDER BY " + m_orderClause;
		//
		log.debug("createSelectSql - " + m_SQL_Count);
		Env.setContext(m_ctx, m_WindowNo, m_TabNo, "SQL", m_SQL);
		return m_SQL;
	}	//	createSelectSql

	/**
	 *	Add Field to Table
	 *  @param field field
	 */
	public void addField (MField field)
	{
		log.debug ("addField (" + m_tableName + ") - " + field.getColumnName());
		if (m_open)
		{
			log.error("addField - Table already open - ignored: " + field.getColumnName());
			return;
		}
		if (!MRole.getDefault(m_ctx, false).isColumnAccess (m_AD_Table_ID, field.getAD_Column_ID(), true))
		{
			log.debug ("addField - No Column Access " + field.getColumnName());
			return;			
		}
		//  Set Index for RowID column
		if (field.getDisplayType() == DisplayType.RowID)
			m_indexRowIDColumn = m_fields.size();
		//  Set Index for Key column
		if (field.isKey())
			m_indexKeyColumn = m_fields.size();
		else if (field.getColumnName().equals("IsActive"))
			m_indexActiveColumn = m_fields.size();
		else if (field.getColumnName().equals("Processed"))
			m_indexProcessedColumn = m_fields.size();
		else if (field.getColumnName().equals("AD_Client_ID"))
			m_indexClientColumn = m_fields.size();
		else if (field.getColumnName().equals("AD_Org_ID"))
			m_indexOrgColumn = m_fields.size();
		//
		m_fields.add(field);
	}	//	addColumn

	/**
	 *  Returns database column name
	 *
	 *  @param index  the column being queried
	 *  @return column name
	 */
	public String getColumnName (int index)
	{
		if (index < 0 || index > m_fields.size())
		{
			log.error("getColumnName - invalid index=" + index);
			return "";
		}
		//
		MField field = (MField)m_fields.get(index);
		return field.getColumnName();
	}   //  getColumnName

	/**
	 * Returns a column given its name.
	 *
	 * @param columnName string containing name of column to be located
	 * @return the column index with <code>columnName</code>, or -1 if not found
	 */
	public int findColumn (String columnName)
	{
		for (int i = 0; i < m_fields.size(); i++)
		{
			MField field = (MField)m_fields.get(i);
			if (columnName.equals(field.getColumnName()))
				return i;
		}
		return -1;
	}   //  findColumn

	/**
	 *  Returns Class of database column/field
	 *
	 *  @param index  the column being queried
	 *  @return the class
	 */
	public Class getColumnClass (int index)
	{
		if (index < 0 || index >= m_fields.size())
		{
			log.error("getColumnClass - invalid index=" + index);
			return null;
		}
		MField field = (MField)m_fields.get(index);
		return DisplayType.getClass(field.getDisplayType(), false);
	}   //  getColumnClass

	/**
	 *	Set Select Clause Parameter.
	 *	Assumes that you set parameters starting from index zero
	 *  @param index index
	 *  @param parameter parameter
	 */
	public void setParameterSELECT (int index, Object parameter)
	{
		if (index >= m_parameterSELECT.size())
			m_parameterSELECT.add(parameter);
		else
			m_parameterSELECT.set(index, parameter);
	}	//	setParameterSELECT

	/**
	 *	Set Where Clause Parameter.
	 *	Assumes that you set parameters starting from index zero
	 *  @param index index
	 *  @param parameter parameter
	 */
	public void setParameterWHERE (int index, Object parameter)
	{
		if (index >= m_parameterWHERE.size())
			m_parameterWHERE.add(parameter);
		else
			m_parameterWHERE.set(index, parameter);
	}	//	setParameterWHERE


	/**
	 *	Get Column at index
	 *  @param index index
	 *  @return MField
	 */
	protected MField getField (int index)
	{
		if (index < 0 || index >= m_fields.size())
			return null;
		return (MField)m_fields.get(index);
	}	//	getColumn

	/**
	 *	Return Columns with Indentifier (ColumnName)
	 *  @param identifier column name
	 *  @return MField
	 */
	protected MField getField (String identifier)
	{
		if (identifier == null || identifier.length() == 0)
			return null;
		int cols = m_fields.size();
		for (int i = 0; i < cols; i++)
		{
			MField field = (MField)m_fields.get(i);
			if (identifier.equalsIgnoreCase(field.getColumnName()))
				return field;
		}
	//	log.error ("getField - not found: '" + identifier + "'");
		return null;
	}	//	getField

	/**
	 *  Get all Fields
	 *  @return MFields
	 */
	public MField[] getFields ()
	{
		MField[] retValue = new MField[m_fields.size()];
		m_fields.toArray(retValue);
		return retValue;
	}   //  getField

	
	/**************************************************************************
	 *	Open Database.
	 *  if already opened, data is refreshed
	 *
	 *	@return true if success
	 */
	public boolean open ()
	{
		log.info("open");
		if (m_open)
		{
			log.debug("open - already open");
			dataRefreshAll();
			return true;
		}

		//	create m_SQL and m_countSQL
		createSelectSql();
		if (m_SQL == null || m_SQL.equals(""))
		{
			log.error("open - No SQL");
			return false;
		}

		//	Start Loading
		m_loader = new Loader();
		m_rowCount = m_loader.open();
		m_buffer = new ArrayList(m_rowCount+10);
		m_sort = new ArrayList(m_rowCount+10);
		if (m_rowCount > 0)
			m_loader.start();
		else
			m_loader.close();
		m_open = true;
		//
		m_changed = false;
		m_rowChanged = -1;
		return true;
	}	//	open

	/**
	 *  Wait until async loader of Table and Lookup Fields is complete
	 *  Used for performance tests
	 */
	public void loadComplete()
	{
		//  Wait for loader
		if (m_loader != null)
		{
			if (m_loader.isAlive())
			{
				try
				{
					m_loader.join();
				}
				catch (InterruptedException ie)
				{
					log.error("loadComplete - join interrupted", ie);
				}
			}
		}
		//  wait for field lookup loaders
		for (int i = 0; i < m_fields.size(); i++)
		{
			MField field = (MField)m_fields.get(i);
			field.lookupLoadComplete();
		}
	}   //  loadComplete

	/**
	 *  Is Loading
	 *  @return true if loading
	 */
	public boolean isLoading()
	{
		if (m_loader != null && m_loader.isAlive())
			return true;
		return false;
	}   //  isLoading

	/**
	 *	Is it open?
	 *  @return true if opened
	 */
	public boolean isOpen()
	{
		return m_open;
	}	//	isOpen

	/**
	 *	Close Resultset
	 *  @param finalCall final call
	 */
	public void close (boolean finalCall)
	{
		if (!m_open)
			return;
		log.debug("close - final=" + finalCall);

		//  remove listeners
		if (finalCall)
		{
			m_dataStatusListeners.clear();
			EventListener evl[] = listenerList.getListeners(TableModelListener.class);
			for (int i = 0; i < evl.length; i++)
				listenerList.remove(TableModelListener.class, evl[i]);
			VetoableChangeListener vcl[] = m_vetoableChangeSupport.getVetoableChangeListeners();
			for (int i = 0; i < vcl.length; i++)
				m_vetoableChangeSupport.removeVetoableChangeListener(vcl[i]);
		}

		//	Stop loader
		while (m_loader != null && m_loader.isAlive())
		{
			log.debug("close - interrupting Loader");
			m_loader.interrupt();
			try
			{
				Thread.sleep(200);		//	.2 second
			}
			catch (InterruptedException ie)
			{}
		}

		if (!m_inserting)
			dataSave(true);

		if (m_buffer != null)
			m_buffer.clear();
		m_buffer = null;
		if (m_sort != null)
			m_sort.clear();
		m_sort = null;

		if (finalCall)
			dispose();

		//  Fields are disposed from MTab
		log.debug("close - complete");
		m_open = false;
	}	//	close

	/**
	 *  Dispose MTable.
	 *  Called by close-final
	 */
	private void dispose()
	{
		//  MFields
		for (int i = 0; i < m_fields.size(); i++)
			((MField)m_fields.get(i)).dispose();
		m_fields.clear();
		m_fields = null;
		//
		m_dataStatusListeners = null;
		m_vetoableChangeSupport = null;
		//
		m_parameterSELECT.clear();
		m_parameterSELECT = null;
		m_parameterWHERE.clear();
		m_parameterWHERE = null;
		//  clear data arrays
		m_buffer = null;
		m_sort = null;
		m_rowData = null;
		m_oldValue = null;
		m_loader = null;
	}   //  dispose

	/**
	 *	Get total database column count (displayed and not displayed)
	 *  @return column count
	 */
	public int getColumnCount()
	{
		return m_fields.size();
	}	//	getColumnCount

	/**
	 *	Get (displayed) field count
	 *  @return field count
	 */
	public int getFieldCount()
	{
		return m_fields.size();
	}	//	getFieldCount

	/**
	 *  Return number of rows
	 *  @return Number of rows or 0 if not opened
	 */
	public int getRowCount()
	{
		return m_rowCount;
	}	//	getRowCount

	/**
	 *	Set the Column to determine the color of the row
	 *  @param columnName column name
	 */
	public void setColorColumn (String columnName)
	{
		m_indexColorColumn = findColumn(columnName);
	}	//  setColorColumn

	/**
	 *	Get ColorCode for Row.
	 *  <pre>
	 *	If numerical value in compare column is
	 *		negative = -1,
	 *      positive = 1,
	 *      otherwise = 0
	 *  </pre>
	 *  @see #setColorColumn
	 *  @param row row
	 *  @return color code
	 */
	public int getColorCode (int row)
	{
		if (m_indexColorColumn  == -1)
			return 0;
		Object data = getValueAt(row, m_indexColorColumn);
		//	We need to have a Number
		if (data == null || !(data instanceof BigDecimal))
			return 0;
		int cmp = Env.ZERO.compareTo(data);
		if (cmp > 0)
			return -1;
		if (cmp < 0)
			return 1;
		return 0;
	}	//	getColorCode


	/**
	 *	Sort Entries by Column.
	 *  actually the rows are not sorted, just the access pointer ArrayList
	 *  with the same size as m_buffer with MSort entities
	 *  @param col col
	 *  @param ascending ascending
	 */
	public void sort (int col, boolean ascending)
	{
		log.info("sort #" + col + " " + ascending);
		if (getRowCount() == 0)
			return;
		MField field = getField (col);
		//	RowIDs are not sorted
		if (field.getDisplayType() == DisplayType.RowID)
			return;
		boolean isLookup = DisplayType.isLookup(field.getDisplayType());

		//	fill MSort entities with data entity
		for (int i = 0; i < m_sort.size(); i++)
		{
			MSort sort = (MSort)m_sort.get(i);
			Object[] rowData = (Object[])m_buffer.get(sort.index);
			if (isLookup)
				sort.data = field.getLookup().getDisplay(rowData[col]);	//	lookup
			else
				sort.data = rowData[col];								//	data
		}

		//	sort it
		MSort sort = new MSort(0, null);
		sort.setSortAsc(ascending);
		Collections.sort(m_sort, sort);
		//	update UI
		fireTableDataChanged();
		//  Info detected by MTab.dataStatusChanged and current row set to 0
		fireDataStatusIEvent("Sorted");
	}	//	sort

	/**
	 *	Get Key ID or -1 of none
	 *  @param row row
	 *  @return ID or -1
	 */
	public int getKeyID (int row)
	{
	//	Log.info("MTable.getKeyID - row=" + row + ", keyColIdx=" + m_indexKeyColumn);
		if (m_indexKeyColumn != -1)
		{
			try
			{
				Integer ii = (Integer)getValueAt(row, m_indexKeyColumn);
				if (ii == null)
					return -1;
				return ii.intValue();
			}
			catch (Exception e)     //  Alpha Key
			{
				return -1;
			}
		}
		return -1;
	}	//	getKeyID

	/**
	 *	Get Key ColumnName
	 *  @return key column name
	 */
	public String getKeyColumnName()
	{
		if (m_indexKeyColumn != -1)
			return getColumnName(m_indexKeyColumn);
		return "";
	}	//	getKeyColumnName

	/**
	 *	Get Selected ROWID or null, if no RowID exists
	 *  @param row row
	 *  @return ROWID
	 */
	public Object getRowID (int row)
	{
		Object[] rid = getRID(row);
		if (rid == null)
			return null;
		return rid[0];
	}	//	getSelectedRowID

	/**
	 *	Get RowID Structure [0]=RowID, [1]=Selected, [2]=ID.
	 *  <p>
	 *  Either RowID or ID is populated (views don't have RowID)
	 *  @param row row
	 *  @return RowID
	 */
	public Object[] getRID (int row)
	{
		if (m_indexRowIDColumn == -1 || row < 0 || row >= getRowCount())
			return null;
		return (Object[])getValueAt(row, m_indexRowIDColumn);
	}	//	getRID

	/**
	 *	Find Row with RowID
	 *  @param RowID row id or oid
	 *	@return number of row or 0 if not found
	 */
	public int getRow (Object RowID)
	{
		if (RowID == null)
			return 0;

		//	the value to find
		String find = RowID.toString();

		//	Wait a bit to load rows
		if (m_loader != null && m_loader.isAlive())
		{
			try
			{
				Thread.sleep(250);		//	1/4 second
			}
			catch (InterruptedException ie)
			{}
		}

		//	Build search vector
		int size = m_sort.size();		//	may still change
		ArrayList search = new ArrayList(size);
		for (int i = 0; i < size; i++)
		{
			Object[] r = (Object[])getValueAt(i, 0);
			String s = r[0].toString();
			MSort so = new MSort(i, s);
			search.add(so);
		}

		//	Sort it
		MSort sort = new MSort(0, null);
		Collections.sort(search, sort);

		//	Find it
		int index = Collections.binarySearch(search, find, sort);
		if (index < 0)	//	not found
		{
			search.clear();
			return 0;
		}
		//	Get Info
		MSort result = (MSort)search.get(index);
		//	Clean up
		search.clear();
		return result.index;
	}	//	getRow


	/**************************************************************************
	 * 	Get Value in Resultset
	 *  @param row row
	 *  @param col col
	 *  @return Object of that row/column
	 */
	public Object getValueAt (int row, int col)
	{
	//	Log.trace(Log.l4_Data, "MTable.getValueAt r=" + row + " c=" + col);
		if (!m_open || row < 0 || col < 0 || row >= m_rowCount)
		{
		//	Log.trace(Log.l5_DData, "Out of bounds - Open=" + m_open + ", RowCount=" + m_rowCount);
			return null;
		}

		//	need to wait for data read into buffer
		int loops = 0;
		while (row >= m_buffer.size() && m_loader.isAlive() && loops < 15)
		{
			log.debug("getValueAt - waiting for loader row=" + row + ", size=" + m_buffer.size());
			try
			{
				Thread.sleep(500);		//	1/2 second
			}
			catch (InterruptedException ie)
			{}
			loops++;
		}

		//	empty buffer
		if (row >= m_buffer.size())
		{
		//	Log.trace(Log.l5_DData, "Empty buffer");
			return null;
		}

		//	return Data item
		MSort sort = (MSort)m_sort.get(row);
		Object[] rowData = (Object[])m_buffer.get(sort.index);
		//	out of bounds
		if (rowData == null || col > rowData.length)
		{
		//	Log.trace(Log.l5_DData, "No data or Column out of bounds");
			return null;
		}
		return rowData[col];
	}	//	getValueAt

	/**
	 *	Indicate that there will be a change
	 *  @param changed changed
	 */
	public void setChanged (boolean changed)
	{
		//	Can we edit?
		if (!m_open || m_readOnly)
			return;

		//	Indicate Change
		m_changed = changed;
		if (!changed)
			m_rowChanged = -1;
		fireDataStatusIEvent("");
	}	//	setChanged

	/**
	 * 	Set Value in data and update MField.
	 *  (called directly or from JTable.editingStopped())
	 *
	 *  @param  value value to assign to cell
	 *  @param  row row index of cell
	 *  @param  col column index of cell
	 */
	public final void setValueAt (Object value, int row, int col)
	{
		setValueAt (value, row, col, false);
	}	//	setValueAt

	/**
	 * 	Set Value in data and update MField.
	 *  (called directly or from JTable.editingStopped())
	 *
	 *  @param  value value to assign to cell
	 *  @param  row row index of cell
	 *  @param  col column index of cell
	 * 	@param	force force setting new value
	 */
	public final void setValueAt (Object value, int row, int col, boolean force)
	{
		//	Can we edit?
		if (!m_open || m_readOnly       //  not accessible
				|| row < 0 || col < 0   //  invalid index
				|| col == 0             //  cannot change ID
				|| m_rowCount == 0)     //  no rows
			return;

		dataSave(row, false);

		//	Has anything changed?
		Object oldValue = getValueAt(row, col);
		if (!force && (
			(oldValue == null && value == null)
			||	(oldValue != null && oldValue.equals(value))
			||	(oldValue != null && value != null && oldValue.toString().equals(value.toString()))
			))
			return;

		log.debug("setValueAt r=" + row + " c=" + col + " = " + value + " (" + oldValue + ")");

		//  Save old value
		m_oldValue = new Object[3];
		m_oldValue[0] = new Integer(row);
		m_oldValue[1] = new Integer(col);
		m_oldValue[2] = oldValue;

		//	Set Data item
		MSort sort = (MSort)m_sort.get(row);
		Object[] rowData = (Object[])m_buffer.get(sort.index);
		m_rowChanged = row;

		//	Selection
		if (col == 0)
		{
			rowData[col] = value;
			m_buffer.set(sort.index, rowData);
			return;
		}

		//	save original value - shallow copy
		if (m_rowData == null)
		{
			int size = m_fields.size();
			m_rowData = new Object[size];
			for (int i = 0; i < size; i++)
				m_rowData[i] = rowData[i];
		}

		//	save & update
		rowData[col] = value;
		m_buffer.set(sort.index, rowData);
		//  update Table
		fireTableCellUpdated(row, col);
		//  update MField
		MField field = getField(col);
		field.setValue(value, m_inserting);
		//  inform
		DataStatusEvent evt = createDSE();
		evt.setChangedColumn(col);
		fireDataStatusChanged(evt);
	}	//	setValueAt

	/**
	 *  Get Old Value
	 *  @param row row
	 *  @param col col
	 *  @return old value
	 */
	public Object getOldValue (int row, int col)
	{
		if (m_oldValue == null)
			return null;
		if (((Integer)m_oldValue[0]).intValue() == row
				&& ((Integer)m_oldValue[1]).intValue() == col)
			return m_oldValue[2];
		return null;
	}   // getOldValue

	/**
	 *	Check if the current row needs to be saved.
	 *  @param  onlyRealChange if true the value of a field was actually changed
	 *  (e.g. for new records, which have not been changed) - default false
	 *	@return true it needs to be saved
	 */
	public boolean needSave(boolean onlyRealChange)
	{
		return needSave(m_rowChanged, onlyRealChange);
	}   //  needSave

	/**
	 *	Check if the row needs to be saved.
	 *  - only if nothing was changed
	 *	@return true it needs to be saved
	 */
	public boolean needSave()
	{
		return needSave(m_rowChanged, false);
	}   //  needSave

	/**
	 *	Check if the row needs to be saved.
	 *  - only when row changed
	 *  - only if nothing was changed
	 *	@param	newRow to check
	 *	@return true it needs to be saved
	 */
	public boolean needSave(int newRow)
	{
		return needSave(newRow, false);
	}   //  needSave

	/**
	 *	Check if the row needs to be saved.
	 *  - only when row changed
	 *  - only if nothing was changed
	 *	@param	newRow to check
	 *  @param  onlyRealChange if true the value of a field was actually changed
	 *  (e.g. for new records, which have not been changed) - default false
	 *	@return true it needs to be saved
	 */
	public boolean needSave(int newRow, boolean onlyRealChange)
	{
		log.debug("needSave - Row=" + newRow +
			", Changed=" + m_rowChanged + "/" + m_changed);  //  m_rowChanged set in setValueAt
		//  nothing done
		if (!m_changed && m_rowChanged == -1)
			return false;
		//  E.g. New unchanged records
		if (m_changed && m_rowChanged == -1 && onlyRealChange)
			return false;
		//  same row
		if (newRow == m_rowChanged)
			return false;

		return true;
	}	//	needSave

	/*************************************************************************/

	public static final char	SAVE_OK = 'O';			//	the only OK condition
	public static final char	SAVE_ERROR = 'E';
	public static final char	SAVE_ACCESS = 'A';
	public static final char	SAVE_MANDATORY = 'M';
	public static final char	SAVE_ABORT = 'U';

	/**
	 *	Check if it needs to be saved and save it.
	 *  @param newRow row
	 *  @param manualCmd manual command to save
	 *	@return true if not needed to be saved or successful saved
	 */
	public boolean dataSave (int newRow, boolean manualCmd)
	{
		log.debug("dataSave - Row=" + newRow +
			", Changed=" + m_rowChanged + "/" + m_changed);  //  m_rowChanged set in setValueAt
		//  nothing done
		if (!m_changed && m_rowChanged == -1)
			return true;
		//  same row, don't save yet
		if (newRow == m_rowChanged)
			return true;

		return (dataSave(manualCmd) == SAVE_OK);
	}   //  dataSave

	/**
	 *	Save unconditional.
	 *  @param manualCmd if true, no vetoable PropertyChange will be fired for save confirmation
	 *	@return OK Or Error condition
	 *  Error info (Access*, FillMandatory, SaveErrorNotUnique,
	 *  SaveErrorRowNotFound, SaveErrorDataChanged) is saved in the log
	 */
	public char dataSave (boolean manualCmd)
	{
		//	cannot save
		if (!m_open)
		{
			log.warn ("dataSave - Error - Open=" + m_open);
			return SAVE_ERROR;
		}
		//	no need - not changed - row not positioned - no Value changed
		if (m_rowChanged == -1)
		{
			log.info("dataSave - NoNeed - Changed=" + m_changed + ", Row=" + m_rowChanged);
		//	return SAVE_ERROR;
			if (!manualCmd)
				return SAVE_OK;
		}
		//  Value not changed
		if (m_rowData == null)
		{
			log.warn ("dataSave - Error - DataNull=" + (m_rowData == null));
			return SAVE_ERROR;
		}

		if (m_readOnly)
		//	If Processed - not editable (Find always editable)  -> ok for changing payment terms, etc.
		{
			log.warn("dataSave - IsReadOnly - ignored");
			dataIgnore();
			return SAVE_ACCESS;
		}

		//	row not positioned - no Value changed
		if (m_rowChanged == -1)
		{
			if (m_newRow != -1)     //  new row and nothing changed - might be OK
				m_rowChanged = m_newRow;
			else
			{
				fireDataStatusEEvent("SaveErrorNoChange", "");
				return SAVE_ERROR;
			}
		}

		//	Can we change?
		int[] co = getClientOrg(m_rowChanged);
		int AD_Client_ID = co[0]; 
		int AD_Org_ID = co[1];
		if (!MRole.getDefault(m_ctx, false).canUpdate(AD_Client_ID, AD_Org_ID, m_AD_Table_ID, true))
		{
			fireDataStatusEEvent(Log.retrieveError());
			dataIgnore();
			return SAVE_ACCESS;
		}

		log.info("dataSave - Saving row " + m_rowChanged);

		//  inform about data save action, if not manually initiated
		try
		{
			if (!manualCmd)
				m_vetoableChangeSupport.fireVetoableChange(PROPERTY, 0, m_rowChanged);
		}
		catch (PropertyVetoException pve)
		{
			log.warn("dataSave - " + pve.getMessage());
			dataIgnore();
			return SAVE_ABORT;
		}

		//	get updated row data
		MSort sort = (MSort)m_sort.get(m_rowChanged);
		Object[] rowData = (Object[])m_buffer.get(sort.index);

		//	Check Mandatory
		String missingColumns = getMandatory(rowData);
		if (missingColumns.length() != 0)
		{
			fireDataStatusEEvent("FillMandatory", missingColumns);
			return SAVE_MANDATORY;
		}

		/**
		 *	Update row *****
		 */
		int Record_ID = 0;
		if (!m_inserting)
			Record_ID = getKeyID(m_rowChanged);
		try
		{
			if (!m_tableName.endsWith("_Trl"))	//	translation tables have no model
				return dataSavePO (Record_ID);
		}
		catch (Exception e)
		{
			if (e instanceof IllegalStateException)
				log.error("MTable.dataSave - " + m_tableName + " - " + e.getLocalizedMessage());
			else
			{
				log.error("MTable.dataSave - Persistency Issue - " + m_tableName, e);
				return SAVE_ERROR;
			}
		}
		
		
		boolean error = false;
		lobReset();
		//
		String is = null;
		final String ERROR = "ERROR: ";
		final String INFO  = "Info: ";

		//	SQL with specific where clause
		String SQL = m_SQL_Select;
		StringBuffer refreshSQL = new StringBuffer(SQL).append(" WHERE ");	//	to be completed when key known
		StringBuffer singleRowWHERE = new StringBuffer();
		StringBuffer multiRowWHERE = new StringBuffer();
		//	Create SQL	& RowID
		Object rowID = null;
		if (m_inserting)
		{
			SQL += " WHERE 1=2";
		}
		else
		{
			//  FOR UPDATE causes  -  ORA-01002 fetch out of sequence
			SQL += " WHERE ROWID=?";
			rowID = getRowID (m_rowChanged);
		}
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			if (!m_inserting)
				DB.getDatabase().setRowID(pstmt, 1, rowID);
			ResultSet rs = pstmt.executeQuery();
			//	only one row
			if (!(m_inserting || rs.next()))
			{
				rs.close();
				pstmt.close();
				fireDataStatusEEvent("SaveErrorRowNotFound", "");
				dataRefresh(m_rowChanged);
				return SAVE_ERROR;
			}

			Object[] rowDataDB = null;
			//	Prepare
			boolean manualUpdate = ResultSet.CONCUR_READ_ONLY == rs.getConcurrency();
			if (manualUpdate)
				createUpdateSqlReset();
			if (m_inserting)
			{
				if (manualUpdate)
					log.debug("dataSave - prepare inserting ... manual");
				else
				{
					log.debug ("dataSave - prepare inserting ... RowSet");
					rs.moveToInsertRow ();
				}
			}
			else
			{
				log.debug("dataSave - prepare updating ... manual=" + manualUpdate);
				//	get current Data in DB
				rowDataDB = readData (rs);
			}

			/**	Data:
			 *		m_rowData	= original Data
			 *		rowData 	= updated Data
			 *		rowDataDB	= current Data in DB
			 *	1) Difference between original & updated Data?	N:next
			 *	2) Difference between original & current Data?	Y:don't update
			 *	3) Update current Data
			 *	4) Refresh to get last Data (changed by trigger, ...)
			 */

			//	Constants for Created/Updated(By)
			Timestamp now = new Timestamp(System.currentTimeMillis());
			int user = Env.getContextAsInt(m_ctx, "#AD_User_ID");

			/**
			 *	for every column
			 */
			int size = m_fields.size();
			for (int col = 0; col < size; col++)
			{
				MField field = (MField)m_fields.get (col);
				String columnName = field.getColumnName ();
			//	log.debug ("dataSave - " + columnName + "= " + m_rowData[col] + " <> DB: " + rowDataDB[col] + " -> " + rowData[col]);

				//	RowID
				if (field.getDisplayType () == DisplayType.RowID)
					; //	ignore

				//	New Key
				else if (field.isKey () && m_inserting)
				{
					if (columnName.endsWith ("_ID") || columnName.toUpperCase().endsWith ("_ID"))
					{
						int insertID = DB.getNextID (m_ctx, m_tableName, null);	//	no trx
						if (manualUpdate)
							createUpdateSql (columnName, String.valueOf (insertID));
						else
							rs.updateInt (col + 1, insertID); 						// ***
						singleRowWHERE.append (columnName).append ("=").append (insertID);
						//
						is = INFO + columnName + " -> " + insertID + " (Key)";
					}
					else //	Key with String value
					{
						String str = rowData[col].toString ();
						if (manualUpdate)
							createUpdateSql (columnName, DB.TO_STRING (str));
						else
							rs.updateString (col + 1, str); 						// ***
						singleRowWHERE = new StringBuffer();	//	overwrite
						singleRowWHERE.append (columnName).append ("=").append (DB.TO_STRING(str));
						//
						is = INFO + columnName + " -> " + str + " (StringKey)";
					}
					log.debug ("dataSave - " + is);
				} //	New Key

				//	New DocumentNo
				else if (columnName.equals ("DocumentNo"))
				{
					boolean newDocNo = false;
					String docNo = (String)rowData[col];
					//  we need to have a doc number
					if (docNo == null || docNo.length () == 0)
						newDocNo = true;
						//  Preliminary ID from CalloutSystem
					else if (docNo.startsWith ("<") && docNo.endsWith (">"))
						newDocNo = true;

					if (newDocNo || m_inserting)
					{
						String insertDoc = null;
						//  always overwrite if insering with mandatory DocType DocNo
						if (m_inserting)
							insertDoc = DB.getDocumentNo (m_ctx, m_WindowNo, 
								m_tableName, true, null);	//	only doc type - no trx
						log.debug ("dataSave - DocumentNo entered=" + docNo + ", DocTypeInsert=" + insertDoc + ", newDocNo=" + newDocNo);
						// can we use entered DocNo?
						if (insertDoc == null || insertDoc.length () == 0)
						{
							if (!newDocNo && docNo != null && docNo.length () > 0)
								insertDoc = docNo;
							else //  get a number from DocType or Table
								insertDoc = DB.getDocumentNo (m_ctx, m_WindowNo, 
									m_tableName, false, null);	//	no trx
						}
						//	There might not be an automatic document no for this document
						if (insertDoc == null || insertDoc.length () == 0)
						{
							//  in case DB function did not return a value
							if (docNo != null && docNo.length () != 0)
								insertDoc = (String)rowData[col];
							else
							{
								error = true;
								is = ERROR + field.getColumnName () + "= " + rowData[col] + " NO DocumentNo";
								log.debug ("dataSave - " + is);
								break;
							}
						}
						//
						if (manualUpdate)
							createUpdateSql (columnName, DB.TO_STRING (insertDoc));
						else
							rs.updateString (col + 1, insertDoc);					//	***
							//
						is = INFO + columnName + " -> " + insertDoc + " (DocNo)";
						log.debug ("dataSave - " + is);
					}
				}	//	New DocumentNo

				//  New Value(key)
				else if (columnName.equals ("Value") && m_inserting)
				{
					String value = (String)rowData[col];
					//  Get from Sequence, if not entered
					if (value == null || value.length () == 0)
					{
						value = DB.getDocumentNo (m_ctx, m_WindowNo, m_tableName, false, null);
						//  No Value
						if (value == null || value.length () == 0)
						{
							error = true;
							is = ERROR + field.getColumnName () + "= " + rowData[col]
								 + " No Value";
							log.debug ("dataSave - " + is);
							break;
						}
					}
					if (manualUpdate)
						createUpdateSql (columnName, DB.TO_STRING (value));
					else
						rs.updateString (col + 1, value); 							//	***
						//
					is = INFO + columnName + " -> " + value + " (Value)";
					log.debug ("dataSave - " + is);
				}	//	New Value(key)

				//	Updated		- check database
				else if (columnName.equals ("Updated"))
				{
					if (m_compareDB && !m_inserting && !m_rowData[col].equals (rowDataDB[col]))	//	changed
					{
						error = true;
						is = ERROR + field.getColumnName () + "= " + m_rowData[col]
							 + " != DB: " + rowDataDB[col];
						log.debug ("dataSave - " + is);
						break;
					}
					if (manualUpdate)
						createUpdateSql (columnName, DB.TO_DATE (now, false));
					else
						rs.updateTimestamp (col + 1, now); 							//	***
						//
					is = INFO + "Updated/By -> " + now + " - " + user;
					log.debug ("dataSave - " + is);
				} //	Updated

				//	UpdatedBy	- update
				else if (columnName.equals ("UpdatedBy"))
				{
					if (manualUpdate)
						createUpdateSql (columnName, String.valueOf (user));
					else
						rs.updateInt (col + 1, user); 								//	***
				} //	UpdatedBy

				//	Created
				else if (m_inserting && columnName.equals ("Created"))
				{
					if (manualUpdate)
						createUpdateSql (columnName, DB.TO_DATE (now, false));
					else
						rs.updateTimestamp (col + 1, now); 							//	***
				} //	Created

				//	CreatedBy
				else if (m_inserting && columnName.equals ("CreatedBy"))
				{
					if (manualUpdate)
						createUpdateSql (columnName, String.valueOf (user));
					else
						rs.updateInt (col + 1, user); 								//	***
				} //	CreatedBy

				//	Nothing changed & null
				else if (m_rowData[col] == null && rowData[col] == null)
				{
					if (m_inserting)
					{
						if (manualUpdate)
							createUpdateSql (columnName, "NULL");
						else
							rs.updateNull (col + 1); 								//	***
						is = INFO + columnName + "= NULL";
						log.debug ("dataSave - " + is);
					}
				}

				//	***	Data changed ***
				else if (m_inserting
				  || (m_rowData[col] == null && rowData[col] != null)
				  || (m_rowData[col] != null && rowData[col] == null)
				  || !m_rowData[col].equals (rowData[col])) 			//	changed
				{
					//	Original == DB
					if (m_inserting || !m_compareDB
					  || (m_rowData[col] == null && rowDataDB[col] == null)
					  || (m_rowData[col] != null && m_rowData[col].equals (rowDataDB[col])))
					{
						if (Log.isTraceLevel(10))
							log.debug("dataSave: " + columnName + "=" + rowData[col]
								+ " " + (rowData[col]==null ? "" : rowData[col].getClass().getName()));
						//
						String type = "String";
						if (rowData[col] == null)
						{
							if (manualUpdate)
								createUpdateSql (columnName, "NULL");
							else
								rs.updateNull (col + 1); 							//	***
						}
						
						//	ID - int
						else if (DisplayType.isID (field.getDisplayType()) 
							|| field.getDisplayType() == DisplayType.Integer)
						{
							int number = 0;
							try
							{
								number = Integer.parseInt (rowData[col].toString ());
								if (manualUpdate)
									createUpdateSql (columnName, String.valueOf (number));
								else
									rs.updateInt (col + 1, number); 			// 	***
							}
							catch (Exception e) //  could also be a String (AD_Language, AD_Message)
							{
								if (manualUpdate)
									createUpdateSql (columnName, DB.TO_STRING (rowData[col].toString ()));
								else
									rs.updateString (col + 1, rowData[col].toString ()); //	***
							}
							type = "Int";
						}
						//	Numeric - BigDecimal
						else if (DisplayType.isNumeric (field.getDisplayType ()))
						{
							if (manualUpdate)
								createUpdateSql (columnName, rowData[col].toString ());
							else
								rs.updateBigDecimal (col + 1, (BigDecimal)rowData[col]); //	***
							type = "Number";
						}
						//	Date - Timestamp
						else if (DisplayType.isDate (field.getDisplayType ()))
						{
							if (manualUpdate)
								createUpdateSql (columnName, DB.TO_DATE ((Timestamp)rowData[col], false));
							else
								rs.updateTimestamp (col + 1, (Timestamp)rowData[col]); //	***
							type = "Date";
						}
						//	LOB
						else if (field.getDisplayType() == DisplayType.TextLong)
						{
							PO_LOB lob = new PO_LOB (getTableName(), columnName, 
								null, field.getDisplayType(), rowData[col]);
							lobAdd(lob);
							type = "CLOB";
						}
						//	Boolean
						else if (field.getDisplayType() == DisplayType.YesNo)
						{
							String yn = null;
							if (rowData[col] instanceof Boolean)
							{
								Boolean bb = (Boolean)rowData[col];
								yn = bb.booleanValue() ? "Y" : "N";
							}
							else
								yn = "Y".equals(rowData[col]) ? "Y" : "N"; 
							if (manualUpdate)
								createUpdateSql (columnName, DB.TO_STRING (yn));
							else
								rs.updateString (col + 1, yn); //	***
						}
						//	String and others
						else	
						{
							if (manualUpdate)
								createUpdateSql (columnName, DB.TO_STRING (rowData[col].toString ()));
							else
								rs.updateString (col + 1, rowData[col].toString ()); //	***
						}
						//
						is = INFO + columnName + "= " + m_rowData[col]
							 + " -> " + rowData[col] + " (" + type + ")";
						log.debug ("dataSave - " + is);
					}
					//	Original != DB
					else
					{
						error = true;
						is = ERROR + field.getColumnName () + "= " + m_rowData[col]
							 + " != DB: " + rowDataDB[col] + " -> " + rowData[col];
						log.debug ("dataSave - " + is);
					}
				}	//	Data changed

				//	Single Key - retrieval sql
				if (field.isKey() && !m_inserting)
				{
					if (rowData[col] == null)
						throw new RuntimeException("dataSave - Key " + columnName + " is NULL");
					if (columnName.endsWith ("_ID"))
						singleRowWHERE.append (columnName).append ("=").append (rowData[col]);
					else
					{
						singleRowWHERE = new StringBuffer();	//	overwrite
						singleRowWHERE.append (columnName).append ("=").append (DB.TO_STRING(rowData[col].toString()));
					}
				}
				//	MultiKey Inserting - retrieval sql
				if (field.isParent())
				{
					if (rowData[col] == null)
						throw new RuntimeException("dataSave - MultiKey Parent " + columnName + " is NULL");
					if (multiRowWHERE.length() != 0)
						multiRowWHERE.append(" AND ");
					if (columnName.endsWith ("_ID"))
						multiRowWHERE.append (columnName).append ("=").append (rowData[col]);
					else
						multiRowWHERE.append (columnName).append ("=").append (DB.TO_STRING(rowData[col].toString()));
				}
			}	//	for every column

			if (error)
			{
				if (manualUpdate)
					createUpdateSqlReset();
				else
					rs.cancelRowUpdates();
				rs.close();
				pstmt.close();
				fireDataStatusEEvent("SaveErrorDataChanged", "");
				dataRefresh(m_rowChanged);
				return SAVE_ERROR;
			}

			/**
			 *	Save to Database
			 */
			//
			String whereClause = singleRowWHERE.toString();
			if (whereClause.length() == 0)
				whereClause = multiRowWHERE.toString();
			if (m_inserting)
			{
				log.debug("dataSave - inserting ...");
				if (manualUpdate)
				{
					String sql = createUpdateSql(true, null);
					int no = DB.executeUpdateEx(sql);
					if (no != 1)
						log.error("dataSave - insert #=" + no + " - " + sql);
				}
				else
					rs.insertRow();
			}
			else
			{
				log.debug("dataSave - updating ... " + whereClause);
				if (manualUpdate)
				{
					String sql = createUpdateSql(false, whereClause);
					int no = DB.executeUpdateEx(sql);
					if (no != 1)
						log.error("dataSave - update #=" + no + " - " + sql);
				}
				else
					rs.updateRow();
			}

			log.debug("dataSave - committing ...");
			DB.commit(true);
			//
			lobSave(whereClause);
			//	data may be updated by trigger after update
			if (m_inserting || manualUpdate)
			{
				rs.close();
				pstmt.close();
				//	need to re-read row to get ROWID, Key, DocumentNo
				log.debug("dataSave - reading ... " + whereClause);
				refreshSQL.append(whereClause);
				pstmt = DB.prepareStatement(refreshSQL.toString());
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					rowDataDB = readData(rs);
					//	update buffer
					m_buffer.set(sort.index, rowDataDB);
					fireTableRowsUpdated(m_rowChanged, m_rowChanged);
				}
				else
					log.error("dataSave - inserted row not found");
			}
			else
			{
				log.debug("dataSave - refreshing ...");
				rs.refreshRow();	//	only use
				rowDataDB = readData(rs);
				//	update buffer
				m_buffer.set(sort.index, rowDataDB);
				fireTableRowsUpdated(m_rowChanged, m_rowChanged);
			}
			//
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException e)
		{
			try
			{
				if (pstmt != null)
				  pstmt.close ();
				pstmt = null;
			}
			catch (Exception ex)
			{
			}

			String msg = "SaveError";
			if (e.getErrorCode() == 1)		//	Unique Constraint
			{
				log.error ("dataSave - Key Not Unique", e);
				msg = "SaveErrorNotUnique";
			}
			else
				log.error ("dataSave\nSQL= " + SQL, e);
			fireDataStatusEEvent(msg, e.getLocalizedMessage());
			return SAVE_ERROR;
		}

		//	everything ok
		m_rowData = null;
		m_changed = false;
		m_compareDB = true;
		m_rowChanged = -1;
		m_newRow = -1;
		m_inserting = false;
		fireDataStatusIEvent("Saved");
		//
		log.info("dataSave - fini");
		return SAVE_OK;
	}	//	dataSave

	/**
	 * 	Save via PO
	 *	@param Record_ID
	 *	@return
	 */
	private char dataSavePO (int Record_ID)
	{
		log.debug("dataSavePO - " + Record_ID);
		//
		MSort sort = (MSort)m_sort.get(m_rowChanged);
		Object[] rowData = (Object[])m_buffer.get(sort.index);
		//
		M_Table table = M_Table.get (m_ctx, m_AD_Table_ID);
		PO po = null;
		if (Record_ID != -1)
			po = table.getPO(Record_ID);
		else	//	Multi - Key
			po = table.getPO(getWhereClause(rowData));
		//	No Persistent Object
		if (po == null)
			throw new IllegalStateException("No Persistent Object");
		if (po == null)
		{
			ValueNamePair pp = Log.retrieveError();
			if (pp != null)
				fireDataStatusEEvent(pp);
			else
			{
				String msg = "SaveError";
				fireDataStatusEEvent(msg, "No Persistent Object");
			}
			return SAVE_ERROR;
		}
		
		int size = m_fields.size();
		for (int col = 0; col < size; col++)
		{
			MField field = (MField)m_fields.get (col);
			String columnName = field.getColumnName ();
			Object value = rowData[col];
			Object oldValue = m_rowData[col];
			//	RowID
			if (field.getDisplayType () == DisplayType.RowID)
				; 	//	ignore

			//	Nothing changed & null
			else if (oldValue == null && value == null)
				;	//	ignore
			
			//	***	Data changed ***
			else if (m_inserting
			  || (oldValue == null && value != null)
			  || (oldValue != null && value == null)
			  || !oldValue.equals (value)) 			//	changed
			{
				//	Check existence
				int poIndex = po.get_ColumnIndex(columnName);
				if (poIndex < 0)
				{
					log.error("dataSavePO - Column not found: " + columnName);
					continue;
				}
				//	Original == DB
				Object originalDB = po.get_Value(poIndex);
				if (m_inserting || !m_compareDB
				  || (oldValue == null && originalDB == null)
				  || (oldValue != null && oldValue.equals (originalDB)))
				{
					po.set_ValueNoCheck (columnName, value);
				}
				//	Original != DB
				else
				{
					fireDataStatusEEvent("SaveErrorDataChanged", 
						columnName 
						+ "= " + oldValue + "(" + (oldValue==null ? "-" : oldValue.getClass().getName())
						+ ") != DB: " + originalDB + "(" + (originalDB==null ? "-" : originalDB.getClass().getName())
						+ ") -> New: " + value + "(" + (value==null ? "-" : value.getClass().getName()) 
						+ ")");
					dataRefresh(m_rowChanged);
					return SAVE_ERROR;
				}
			}	//	Data changed

		}	//	for every column

		if (!po.save())
		{
			String msg = "SaveError";
			String info = "";
			ValueNamePair pp = Log.retrieveError();
			if (pp != null)
			{
				msg = pp.getValue();
				info = pp.getName();
			}
			Exception ex = Log.retrieveException();
			if (ex != null 
				&& ex instanceof SQLException
				&& ((SQLException)ex).getErrorCode() == 1)
				msg = "SaveErrorNotUnique";
			fireDataStatusEEvent(msg, info);
			return SAVE_ERROR;
		}
		
		//	Refresh - update buffer
		String whereClause = po.get_WhereClause(true);
		log.debug("dataSavePO - reading ... " + whereClause);
		StringBuffer refreshSQL = new StringBuffer(m_SQL_Select)
			.append(" WHERE ").append(whereClause);
		PreparedStatement pstmt = DB.prepareStatement(refreshSQL.toString());
		try
		{
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				Object[] rowDataDB = readData(rs);
				//	update buffer
				m_buffer.set(sort.index, rowDataDB);
				fireTableRowsUpdated(m_rowChanged, m_rowChanged);
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException e)
		{
			try
			{
				if (pstmt != null)
				  pstmt.close ();
				pstmt = null;
			}
			catch (Exception ex)
			{
			}

			String msg = "SaveError";
			log.error ("dataSavePO", e);
			fireDataStatusEEvent(msg, e.getLocalizedMessage());
			return SAVE_ERROR;
		}

		//	everything ok
		m_rowData = null;
		m_changed = false;
		m_compareDB = true;
		m_rowChanged = -1;
		m_newRow = -1;
		m_inserting = false;
		fireDataStatusIEvent("Saved");
		//
		log.info("dataSave - fini");
		return SAVE_OK;
	}	//	dataSavePO
	
	/**
	 * 	Get Where Clause
	 *	@param rowData data
	 *	@return where clause or null
	 */
	private String getWhereClause (Object[] rowData)
	{
		int size = m_fields.size();
		StringBuffer singleRowWHERE = null;
		StringBuffer multiRowWHERE = null;
		for (int col = 0; col < size; col++)
		{
			MField field = (MField)m_fields.get (col);
			if (field.isKey())
			{
				String columnName = field.getColumnName();
				Object value = rowData[col]; 
				if (value == null)
				{
					log.error("getWhereClause - PK data is null");
					return null;
				}
				if (columnName.endsWith ("_ID"))
					singleRowWHERE = new StringBuffer(columnName)
						.append ("=").append (value);
				else
					singleRowWHERE = new StringBuffer(columnName)
						.append ("=").append (DB.TO_STRING(value.toString()));
			}
			else if (field.isParent())
			{
				String columnName = field.getColumnName();
				Object value = rowData[col]; 
				if (value == null)
				{
					log.error("getWhereClause - FK data is null");
					return null;
				}
				if (multiRowWHERE == null)
					multiRowWHERE = new StringBuffer();
				else
					multiRowWHERE.append(" AND ");
				if (columnName.endsWith ("_ID"))
					multiRowWHERE.append (columnName)
						.append ("=").append (value);
				else
					multiRowWHERE.append (columnName)
						.append ("=").append (DB.TO_STRING(value.toString()));
			}
		}	//	for all columns
		if (singleRowWHERE != null)
			return singleRowWHERE.toString();
		if (multiRowWHERE != null)
			return multiRowWHERE.toString();
		log.error("getWhereClause - No key Found");
		return null;
	}	//	getWhereClause
	
	/*************************************************************************/

	private ArrayList	m_createSqlColumn = new ArrayList();
	private ArrayList	m_createSqlValue = new ArrayList();

	/**
	 * 	Prepare SQL creation
	 * 	@param columnName column name
	 * 	@param value value
	 */
	private void createUpdateSql (String columnName, String value)
	{
		m_createSqlColumn.add(columnName);
		m_createSqlValue.add(value);
		if (Log.isTraceLevel(10))
			log.debug("createUpdateSql #" + m_createSqlColumn.size()
				+ " - " + columnName + "=" + value);
	}	//	createUpdateSQL

	/**
	 * 	Create update/insert SQL
	 * 	@param insert true if insert - update otherwise
	 * 	@param whereClause where clause for update
	 * 	@return sql statement
	 */
	private String createUpdateSql (boolean insert, String whereClause)
	{
		StringBuffer sb = new StringBuffer();
		if (insert)
		{
			sb.append("INSERT INTO ").append(m_tableName).append(" (");
			for (int i = 0; i < m_createSqlColumn.size(); i++)
			{
				if (i != 0)
					sb.append(",");
				sb.append(m_createSqlColumn.get(i));
			}
			sb.append(") VALUES ( ");
			for (int i = 0; i < m_createSqlValue.size(); i++)
			{
				if (i != 0)
					sb.append(",");
				sb.append(m_createSqlValue.get(i));
			}
			sb.append(")");
		}
		else
		{
			sb.append("UPDATE ").append(m_tableName).append(" SET ");
			for (int i = 0; i < m_createSqlColumn.size(); i++)
			{
				if (i != 0)
					sb.append(",");
				sb.append(m_createSqlColumn.get(i)).append("=").append(m_createSqlValue.get(i));
			}
			sb.append(" WHERE ").append(whereClause);
		}
		log.debug("createUpdateSql=" + sb.toString());
		//	reset
		createUpdateSqlReset();
		return sb.toString();
	}	//	createUpdateSql

	/**
	 * 	Reset Update Data
	 */
	private void createUpdateSqlReset()
	{
		m_createSqlColumn = new ArrayList();
		m_createSqlValue = new ArrayList();
	}	//	createUpdateSqlReset

	/**
	 *	Get Mandatory empty columns
	 *  @param rowData row data
	 *  @return String with missing column headers/labels
	 */
	private String getMandatory(Object[] rowData)
	{
		//  see also => ProcessParameter.saveParameter
		StringBuffer sb = new StringBuffer();

		//	Check all columns
		int size = m_fields.size();
		for (int i = 0; i < size; i++)
		{
			MField field = (MField)m_fields.get(i);
			if (field.isMandatory(true))        //  check context
			{
				if (rowData[i] == null || rowData[i].toString().length() == 0)
				{
					field.setInserting (true);  //  set editable otherwise deadlock
					field.setError(true);
					if (sb.length() > 0)
						sb.append(", ");
					sb.append(field.getHeader());
				}
				else
					field.setError(false);
			}
		}

		if (sb.length() == 0)
			return "";
		return sb.toString();
	}	//	getMandatory

	/*************************************************************************/

	/**	LOB Info				*/
	private ArrayList		m_lobInfo = null;

	/**
	 * 	Reset LOB info
	 */
	private void lobReset()
	{
		m_lobInfo = null;
	}	//	resetLOB
	
	/**
	 * 	Prepare LOB save
	 *	@param value value 
	 *	@param index index
	 *	@param displayType display type
	 */	
	private void lobAdd (PO_LOB lob)
	{
		log.debug("lobAdd - " + lob);
		if (m_lobInfo == null)
			m_lobInfo = new ArrayList();
		m_lobInfo.add(lob);
	}	//	lobAdd
	
	/**
	 * 	Save LOB
	 */
	private void lobSave (String whereClause)
	{
		if (m_lobInfo == null)
			return;
		for (int i = 0; i < m_lobInfo.size(); i++)
		{
			PO_LOB lob = (PO_LOB)m_lobInfo.get(i);
			lob.save(whereClause);
		}	//	for all LOBs
		lobReset();
	}	//	lobSave

	
	/**************************************************************************
	 *	New Record after current Row
	 *  @param currentRow row
	 *  @param copyCurrent copy
	 *  @return true if success -
	 *  Error info (Access*, AccessCannotInsert) is saved in the log
	 */
	public boolean dataNew (int currentRow, boolean copyCurrent)
	{
		log.info("dataNew - Current=" + currentRow + ", Copy=" + copyCurrent);
		//  Read only
		if (m_readOnly)
		{
			fireDataStatusEEvent("AccessCannotInsert", "");
			return false;
		}

		/** @todo No TableLevel */
		//  || !Access.canViewInsert(m_ctx, m_WindowNo, tableLevel, true, true))
		//  fireDataStatusEvent(Log.retrieveError());

		//  see if we need to save
		dataSave(-2, false);


		m_inserting = true;
		//	Create default data
		int size = m_fields.size();
		m_rowData = new Object[size];	//	"original" data
		Object[] rowData = new Object[size];
		//	fill data
		if (copyCurrent)
		{
			MSort sort = (MSort) m_sort.get(currentRow);
			Object[] origData = (Object[])m_buffer.get(sort.index);
			for (int i = 0; i < size; i++)
				rowData[i] = origData[i];
		}
		else	//	new
		{
			for (int i = 0; i < size; i++)
			{
				MField field = (MField)m_fields.get(i);
				rowData[i] = field.getDefault();
				field.setValue(rowData[i], m_inserting);
			}
		}
		m_changed = true;
		m_compareDB = true;
		m_rowChanged = -1;  //  only changed in setValueAt
		m_newRow = currentRow + 1;
		//  if there is no record, the current row could be 0 (and not -1)
		if (m_buffer.size() < m_newRow)
			m_newRow = m_buffer.size();

		//	add Data at end of buffer
		MSort sort = new MSort(m_buffer.size(), null);	//	index
		m_buffer.add(rowData);
		//	add Sort pointer
		m_sort.add(m_newRow, sort);
		m_rowCount++;

		//	inform
		log.debug("dataNew - Current=" + currentRow + ", New=" + m_newRow);
		fireTableRowsInserted(m_newRow, m_newRow);
		fireDataStatusIEvent(copyCurrent ? "UpdateCopied" : "Inserted");
		log.debug("dataNew - Current=" + currentRow + ", New=" + m_newRow + " - complete");
		return true;
	}	//	dataNew


	/**************************************************************************
	 *	Delete Data
	 *  @param row row
	 *  @return true if success -
	 *  Error info (Access*, AccessNotDeleteable, DeleteErrorDependent,
	 *  DeleteError) is saved in the log
	 */
	public boolean dataDelete (int row)
	{
		log.info("dataDelete - " + row);
		if (row < 0)
			return false;
		Object rowID = getRowID(row);
		if (rowID == null)
			return false;

		//	Is this record deletable?
		if (!m_deleteable)
		{
			fireDataStatusEEvent("AccessNotDeleteable", "");	//	audit
			return false;
		}

		//	Tab R/O
		if (m_readOnly)
		{
			fireDataStatusEEvent("AccessCannotDelete", "");		//	previleges
			return false;
		}

		//	Processed Column and not an Import Table
		if (m_indexProcessedColumn > 0 && !m_tableName.startsWith("I_"))
		{
			Boolean processed = (Boolean)getValueAt(row, m_indexProcessedColumn);
			if (processed != null && processed.booleanValue())
			{
				fireDataStatusEEvent("CannotDeleteTrx", "");
				return false;
			}
		}
		

		/** @todo check Access */
		//  fireDataStatusEvent(Log.retrieveError());

		MSort sort = (MSort)m_sort.get(row);
		Object[] rowData = (Object[])m_buffer.get(sort.index);
		//
		M_Table table = M_Table.get (m_ctx, m_AD_Table_ID);
		PO po = null;
		int Record_ID = getKeyID(m_rowChanged);
		if (Record_ID != -1)
			po = table.getPO(Record_ID);
		else	//	Multi - Key
			po = table.getPO(getWhereClause(rowData));
		
		//	Delete via PO 
		if (po != null)
		{
			if (!po.delete(false))
			{
				ValueNamePair vp = Log.retrieveError();
				if (vp != null)
					fireDataStatusEEvent(vp);
				else
					fireDataStatusEEvent("DeleteError", "");
				return false;
			}
		}
		else	//	Delete via SQL
		{
			StringBuffer SQL = new StringBuffer("DELETE ");
			SQL.append(m_tableName).append(" WHERE ROWID=?");
			int no = 0;
			try
			{
				PreparedStatement pstmt = DB.prepareStatement(SQL.toString());
				DB.getDatabase().setRowID(pstmt, 1, rowID);
				no = pstmt.executeUpdate();
				pstmt.close();
			}
			catch (SQLException e)
			{
				log.error ("dataDelete", e);
				String msg = "DeleteError";
				if (e.getErrorCode() == 2292)	//	Child Record Found
					msg = "DeleteErrorDependent";
				fireDataStatusEEvent(msg, e.getLocalizedMessage());
				return false;
			}
			//	Check Result
			if (no != 1)
			{
				log.error("dataDelete - Number of deleted rows = " + no);
				return false;
			}
		}

		//	Get Sort
		int bufferRow = sort.index;
		//	Delete row in Buffer and shifts all below up
		m_buffer.remove(bufferRow);
		m_rowCount--;

		//	Delete row in Sort
		m_sort.remove(row);
		//	Correct pointer in Sort
		for (int i = 0; i < m_sort.size(); i++)
		{
			MSort ptr = (MSort)m_sort.get(i);
			if (ptr.index > bufferRow)
				ptr.index--;	//	move up
		}

		//	inform
		m_changed = false;
		m_rowChanged = -1;
		fireTableRowsDeleted(row, row);
		fireDataStatusIEvent("Deleted");
		log.debug("dataDelete - " + row + " complete");
		return true;
	}	//	dataDelete

	
	/**************************************************************************
	 *	Ignore changes
	 */
	public void dataIgnore()
	{
		log.info("dataIgnore - Inserting=" + m_inserting);
		if (!m_inserting && !m_changed && m_rowChanged < 0)
		{
			log.debug("dataIgnore - Nothing to ignore");
			return;
		}

		//	Inserting - delete new row
		if (m_inserting)
		{
			//	Get Sort
			MSort sort = (MSort)m_sort.get(m_newRow);
			int bufferRow = sort.index;
			//	Delete row in Buffer and shifts all below up
			m_buffer.remove(bufferRow);
			m_rowCount--;
			//	Delete row in Sort
			m_sort.remove(m_newRow);	//	pintint to the last column, so no adjustment
			//
			m_changed = false;
			m_rowData = null;
			m_rowChanged = -1;
			m_inserting = false;
			//	inform
			fireTableRowsDeleted(m_newRow, m_newRow);
		}
		else
		{
			//	update buffer
			if (m_rowData != null)
			{
				MSort sort = (MSort)m_sort.get(m_rowChanged);
				m_buffer.set(sort.index, m_rowData);
			}
			m_changed = false;
			m_rowData = null;
			m_rowChanged = -1;
			m_inserting = false;
			//	inform
		//	fireTableRowsUpdated(m_rowChanged, m_rowChanged); >> messes up display?? (clearSelection)
		}
		m_newRow = -1;
		fireDataStatusIEvent("Ignored");
	}	//	dataIgnore


	/**
	 *	Refresh Row - ignore changes
	 *  @param row row
	 */
	public void dataRefresh (int row)
	{
		log.info("dataRefresh " + row);

		if (row < 0)
			return;
		Object rowID = getRowID(row);
		if (rowID == null)
			return;

		//  ignore
		dataIgnore();

		//	Create SQL
		String SQL = m_SQL_Select + " WHERE ROWID=?";
		MSort sort = (MSort)m_sort.get(row);
		Object[] rowDataDB = null;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(SQL);
			DB.getDatabase().setRowID(pstmt, 1, rowID);
			ResultSet rs = pstmt.executeQuery();
			//	only one row
			if (rs.next())
				rowDataDB = readData(rs);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.error ("dataRefresh\nSQL=" + SQL, e);
			fireTableRowsUpdated(row, row);
			fireDataStatusEEvent("RefreshError", "");
			return;
		}

		//	update buffer
		m_buffer.set(sort.index, rowDataDB);
		//	info
		m_rowData = null;
		m_changed = false;
		m_rowChanged = -1;
		m_inserting = false;
		fireTableRowsUpdated(row, row);
		fireDataStatusIEvent("Refreshed");
	}	//	dataRefresh


	/**
	 *	Refresh all Rows - ignore changes
	 */
	public void dataRefreshAll()
	{
		log.info("dataRefreshAll");
		dataIgnore();
		close(false);
		open();
		//	Info
		m_rowData = null;
		m_changed = false;
		m_rowChanged = -1;
		m_inserting = false;
		fireTableDataChanged();
		fireDataStatusIEvent("Refreshed");
	}	//	dataRefreshAll


	/**
	 *	Requery with new whereClause
	 *  @param whereClause sql where clause
	 *  @param onlyCurrentRows only current rows
	 *  @param onlyCurrentDays how many days back
	 *  @return true if success
	 */
	public boolean dataRequery (String whereClause, boolean onlyCurrentRows, int onlyCurrentDays)
	{
		log.info("dataRequery - " + whereClause + "; OnlyCurrent=" + onlyCurrentRows);
		close(false);
		m_onlyCurrentDays = onlyCurrentDays;
		setWhereClause(whereClause, onlyCurrentRows, m_onlyCurrentDays);
		open();
		//  Info
		m_rowData = null;
		m_changed = false;
		m_rowChanged = -1;
		m_inserting = false;
		fireTableDataChanged();
		fireDataStatusIEvent("Refreshed");
		return true;
	}	//	dataRequery


	/**************************************************************************
	 *	Is Cell Editable.
	 *	Is queried from JTable before checking VCellEditor.isCellEditable
	 *  @param  row the row index being queried
	 *  @param  col the column index being queried
	 *  @return true, if editable
	 */
	public boolean isCellEditable (int row, int col)
	{
	//	Log.trace(Log.l6_Database, "MTable.isCellEditable - Row=" + row + ", Col=" + col);
		//	Make Rows selectable
		if (col == 0)
			return true;

		//	Entire Table not editable
		if (m_readOnly)
			return false;
		//	Key & ID not editable
		if (col == m_indexRowIDColumn || col == m_indexKeyColumn)
			return false;
		/** @todo check link columns */

		//	Check column range
		if (col < 0 && col >= m_fields.size())
			return false;
		//  IsActive Column always editable if no processed exists
		if (col == m_indexActiveColumn && m_indexProcessedColumn == -1)
			return true;
		//	Row
		if (!isRowEditable(row))
			return false;

		//	Column
		return ((MField)m_fields.get(col)).isEditable(false);
	}	//	IsCellEditable


	/**
	 *	Is Current Row Editable
	 *  @param row row
	 *  @return true if editable
	 */
	public boolean isRowEditable (int row)
	{
	//	Log.trace(Log.l6_Database, "MTable.isRowEditable - Row=" + row);
		//	Entire Table not editable or no row
		if (m_readOnly || row < 0)
			return false;
		//	If not Active - not editable
		if (m_indexActiveColumn > 0)		//	&& m_TabNo != Find.s_TabNo)
		{
			Object value = getValueAt(row, m_indexActiveColumn);
			if (value instanceof Boolean)
			{
				if (!((Boolean)value).booleanValue())
					return false;
			}
			else if ("N".equals(value)) 
				return false;
		}
		//	If Processed - not editable (Find always editable)
		if (m_indexProcessedColumn > 0)		//	&& m_TabNo != Find.s_TabNo)
		{
			Object value = getValueAt(row, m_indexProcessedColumn); 
			if (value instanceof Boolean)
			{
				if (!((Boolean)value).booleanValue())
					return false;
			}
			else if ("N".equals(value)) 
				return false;
		}
		//
		int[] co = getClientOrg(row);
		int AD_Client_ID = co[0]; 
		int AD_Org_ID = co[1];
		return MRole.getDefault(m_ctx, false).canUpdate(AD_Client_ID, AD_Org_ID, m_AD_Table_ID, false);
	}	//	isRowEditable

	/**
	 * 	Get Client Org for row
	 *	@param row row
	 *	@return array [0] = Client [1] = Org - a value of -1 is not defined/found
	 */
	private int[] getClientOrg (int row)
	{
		int AD_Client_ID = -1;
		if (m_indexClientColumn != -1)
		{
			Integer ii = (Integer)getValueAt(row, m_indexClientColumn);
			if (ii != null)
				AD_Client_ID = ii.intValue();
		}
		int AD_Org_ID = 0;
		if (m_indexOrgColumn != -1)
		{
			Integer ii = (Integer)getValueAt(row, m_indexOrgColumn);
			if (ii != null)
				AD_Org_ID = ii.intValue();
		}
		return new int[] {AD_Client_ID, AD_Org_ID};
	}	//	getClientOrg

	/**
	 *	Set entire table as read only
	 *  @param value new read only value
	 */
	public void setReadOnly (boolean value)
	{
		log.debug("setReadOnly " + value);
		m_readOnly = value;
	}	//	setReadOnly

	/**
	 *  Is entire Table Read/Only
	 *  @return true if read only
	 */
	public boolean isReadOnly()
	{
		return m_readOnly;
	}   //  isReadOnly

	/**
	 *  Is inserting
	 *  @return true if inserting
	 */
	public boolean isInserting()
	{
		return m_inserting;
	}   //  isInserting

	/**
	 *	Set Compare DB.
	 * 	If Set to false, save overwrites the record, regardless of DB changes.
	 *  (When a payment is changed in Sales Order, the payment reversal clears the payment id)
	 * 	@param compareDB compare DB - false forces overwrite
	 */
	public void setCompareDB (boolean compareDB)
	{
		m_compareDB = compareDB;
	}  	//	setCompareDB

	/**
	 *	Get Compare DB.
	 * 	@return false if save overwrites the record, regardless of DB changes
	 * 	(false forces overwrite).
	 */
	public boolean getCompareDB ()
	{
		return m_compareDB;
	}  	//	getCompareDB


	/**
	 *	Can Table rows be deleted
	 *  @param value new deleteable value
	 */
	public void setDeleteable (boolean value)
	{
		log.debug("setDeleteable " + value);
		m_deleteable = value;
	}	//	setDeleteable

	
	/**************************************************************************
	 *	Read Data from Recordset
	 *  @param rs result set
	 *  @return Data Array
	 */
	private Object[] readData (ResultSet rs)
	{
		int size = m_fields.size();
		Object[] rowData = new Object[size];
		String columnName = null;
		int displayType = 0;

		//	Types see also MField.createDefault
		try
		{
			//	get row data
			for (int j = 0; j < size; j++)
			{
				//	Column Info
				MField field = (MField)m_fields.get(j);
				columnName = field.getColumnName();
				displayType = field.getDisplayType();
				//	Integer, ID, Lookup (UpdatedBy is a numeric column)
				if (displayType == DisplayType.Integer
					|| (DisplayType.isID(displayType) && (columnName.endsWith("_ID") || columnName.endsWith("_Acct"))) 
					|| columnName.endsWith("atedBy"))
				{
					rowData[j] = new Integer(rs.getInt(j+1));	//	Integer
					if (rs.wasNull())
						rowData[j] = null;
				}
				//	Number
				else if (DisplayType.isNumeric(displayType))
					rowData[j] = rs.getBigDecimal(j+1);			//	BigDecimal
				//	Date
				else if (DisplayType.isDate(displayType))
					rowData[j] = rs.getTimestamp(j+1);			//	Timestamp
				//	RowID or Key (and Selection)
				else if (displayType == DisplayType.RowID)
				{
					Object[] rid = new Object[3];
					if (columnName.equals("ROWID"))
						rid[0] = DB.getDatabase().getRowID(rs, j+1);
					else
						rid[2] = new Integer (rs.getInt(j+1));
					rid[1] = new Boolean(false);
					rowData[j] = rid;
				}
				//	YesNo
				else if (displayType == DisplayType.YesNo)
					rowData[j] = new Boolean ("Y".equals(rs.getString(j+1)));	//	Boolean			
				//	LOB
				else if (displayType == DisplayType.TextLong)
				{
					Object value = rs.getObject(j+1);
					if (rs.wasNull())
						rowData[j] = null;
					else if (value instanceof Clob) 
					{
						Clob lob = (Clob)value;
						long length = lob.length();
						rowData[j] = lob.getSubString(1, (int)length);
					}
				}
				//	String
				else
					rowData[j] = rs.getString(j+1);				//	String
			}
		}
		catch (SQLException e)
		{
			log.error("readData - " + columnName + ", DT=" + displayType, e);
		}
		return rowData;
	}	//	readData

	
	/**************************************************************************
	 *	Remove Data Status Listener
	 *  @param l listener
	 */
	public synchronized void removeDataStatusListener(DataStatusListener l)
	{
		if (m_dataStatusListeners != null && m_dataStatusListeners.contains(l))
		{
			Vector v = (Vector) m_dataStatusListeners.clone();
			v.removeElement(l);
			m_dataStatusListeners = v;
		}
	}	//	removeDataStatusListener

	/**
	 *	Add Data Status Listener
	 *  @param l listener
	 */
	public synchronized void addDataStatusListener(DataStatusListener l)
	{
		Vector v = m_dataStatusListeners == null ? new Vector(2) : (Vector) m_dataStatusListeners.clone();
		if (!v.contains(l))
		{
			v.addElement(l);
			m_dataStatusListeners = v;
		}
	}	//	addDataStatusListener

	/**
	 *	Inform Listeners
	 *  @param e event
	 */
	private void fireDataStatusChanged (DataStatusEvent e)
	{
		if (m_dataStatusListeners != null)
		{
			Vector listeners = m_dataStatusListeners;
			int count = listeners.size();
			for (int i = 0; i < count; i++)
				((DataStatusListener) listeners.elementAt(i)).dataStatusChanged(e);
		}
	}	//	fireDataStatusChanged

	/**
	 *  Create Data Status Event
	 *  @return data status event
	 */
	private DataStatusEvent createDSE()
	{
		boolean changed = m_changed;
		if (m_rowChanged != -1)
			changed = true;
		DataStatusEvent dse = new DataStatusEvent(this, m_rowCount, changed,
			Env.isAutoCommit(m_ctx, m_WindowNo), m_inserting);
		return dse;
	}   //  createDSE

	/**
	 *  Create and fire Data Status Info Event
	 *  @param AD_Message message
	 */
	protected void fireDataStatusIEvent (String AD_Message)
	{
		DataStatusEvent e = createDSE();
		e.setInfo(AD_Message, "", false);
		fireDataStatusChanged (e);
	}   //  fireDataStatusEvent

	/**
	 *  Create and fire Data Status Error Event
	 *  @param AD_Message message
	 *  @param info info
	 */
	protected void fireDataStatusEEvent (String AD_Message, String info)
	{
	//	org.compiere.util.Trace.printStack();
		//
		DataStatusEvent e = createDSE();
		e.setInfo(AD_Message, info, true);
		Log.saveError(AD_Message, info);
		fireDataStatusChanged (e);
	}   //  fireDataStatusEvent

	/**
	 *  Create and fire Data Status Event (from Error Log)
	 *  @param errorLog error log info
	 */
	protected void fireDataStatusEEvent (ValueNamePair errorLog)
	{
		if (errorLog != null)
			fireDataStatusEEvent (errorLog.getValue(), errorLog.getName());
	}   //  fireDataStatusEvent

	
	/**************************************************************************
	 *  Remove Vetoable change listener for row changes
	 *  @param l listener
	 */
	public synchronized void removeVetoableChangeListener(VetoableChangeListener l)
	{
		m_vetoableChangeSupport.removeVetoableChangeListener(l);
	}   //  removeVetoableChangeListener

	/**
	 *  Add Vetoable change listener for row changes
	 *  @param l listener
	 */
	public synchronized void addVetoableChangeListener(VetoableChangeListener l)
	{
		m_vetoableChangeSupport.addVetoableChangeListener(l);
	}   //  addVetoableChangeListener

	/**
	 *  Fire Vetoable change listener for row changes
	 *  @param e event
	 *  @throws PropertyVetoException
	 */
	protected void fireVetoableChange(PropertyChangeEvent e) throws java.beans.PropertyVetoException
	{
		m_vetoableChangeSupport.fireVetoableChange(e);
	}   //  fireVetoableChange

	/**
	 *  toString
	 *  @return String representation
	 */
	public String toString()
	{
		return new StringBuffer("MTable[").append(m_tableName)
			.append(",WindowNo=").append(m_WindowNo)
			.append(",Tab=").append(m_TabNo).append("]").toString();
	}   //  toString


	
	/**************************************************************************
	 *	ASync Loader
	 */
	class Loader extends Thread implements Serializable
	{
		/**
		 *  Construct Loader
		 */
		public Loader()
		{
			super("TLoader");
		}	//	Loader

		private PreparedStatement   m_pstmt = null;
		private ResultSet 		    m_rs = null;

		/**
		 *	Open ResultSet
		 *	@return number of records
		 */
		protected int open()
		{
		//	Log.trace(Log.l4_Data, "MTable Loader.open");
			//	Get Number of Rows
			int rows = 0;
			try
			{
				PreparedStatement pstmt = DB.prepareStatement(m_SQL_Count);
				setParameter (pstmt, true);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next())
					rows = rs.getInt(1);
				rs.close();
				pstmt.close();
			}
			catch (SQLException e0)
			{
				//	Zoom Query may have invalid where clause
				if (e0.getErrorCode() == 904) 	//	ORA-00904: "C_x_ID": invalid identifier
					log.warn("Loader.open Count - " + e0.getLocalizedMessage() + "\nSQL=" + m_SQL_Count);
				else
					log.error ("Loader.open Count SQL=" + m_SQL_Count, e0);
				return 0;
			}

			//	open Statement (closed by Loader.close)
			try
			{
				m_pstmt = DB.prepareStatement(m_SQL);
			//	m_pstmt.setFetchSize(20);
				setParameter (m_pstmt, false);
				m_rs = m_pstmt.executeQuery();
			}
			catch (SQLException e)
			{
				log.error ("Loader.open\nFull SQL=" + m_SQL, e);
				return 0;
			}
			StringBuffer info = new StringBuffer("Rows=");
			info.append(rows);
			if (rows == 0)
				info.append(" - ").append(m_SQL_Count);
			log.debug("Loader.open - " + info.toString());
			return rows;
		}	//	open

		/**
		 *	Close RS and Statement
		 */
		private void close()
		{
		//	Log.trace(Log.l4_Data, "MTable Loader.close");
			try
			{
				if (m_rs != null)
					m_rs.close();
				if (m_pstmt != null)
					m_pstmt.close();
			}
			catch (SQLException e)
			{
				log.error ("Loader.closeRS", e);
			}
			m_rs = null;
			m_pstmt = null;
		}	//	close

		/**
		 *	Fill Buffer to include Row
		 */
		public void run()
		{
			log.info("Loader.run");
			if (m_rs == null)
				return;

			try
			{
				while(m_rs.next())
				{
					if (this.isInterrupted())
					{
						log.debug("Loader interrupted");
						close();
						return;
					}
					//  Get Data
					Object[] rowData = readData(m_rs);
					//	add Data
					MSort sort = new MSort(m_buffer.size(), null);	//	index
					m_buffer.add(rowData);
					m_sort.add(sort);

					//	Statement all 250 rows & sleep
					if (m_buffer.size() % 250 == 0)
					{
						//	give the other processes a chance
						try
						{
							yield();
							sleep(10);		//	.01 second
						}
						catch (InterruptedException ie)
						{
							log.debug("Loader interrupted while sleeping");
							close();
							return;
						}
						DataStatusEvent evt = createDSE();
						evt.setLoading(m_buffer.size());
						fireDataStatusChanged(evt);
					}
				}	//	while(rs.next())
			}
			catch (SQLException e)
			{
				log.error ("Loader.run", e);
			}
			close();
			fireDataStatusIEvent("");
		}	//	run

		/**
		 *	Set Parameter for Query.
		 *		elements must be Integer, BigDecimal, String (default)
		 *  @param pstmt prepared statement
		 *  @param countSQL count
		 */
		private void setParameter (PreparedStatement pstmt, boolean countSQL)
		{
			if (m_parameterSELECT.size() == 0 && m_parameterWHERE.size() == 0)
				return;
			try
			{
				int pos = 1;	//	position in Statement
				//	Select Clause Parameters
				for (int i = 0; !countSQL && i < m_parameterSELECT.size(); i++)
				{
					Object para = m_parameterSELECT.get(i);
					if (para != null)
						log.debug("setParameter Select " + i + "=" + para);
					//
					if (para == null)
						;
					else if (para instanceof Integer)
					{
						Integer ii = (Integer)para;
						pstmt.setInt (pos++, ii.intValue());
					}
					else if (para instanceof BigDecimal)
						pstmt.setBigDecimal (pos++, (BigDecimal)para);
					else
						pstmt.setString(pos++, para.toString());
				}
				//	Where Clause Parameters
				for (int i = 0; i < m_parameterWHERE.size(); i++)
				{
					Object para = m_parameterWHERE.get(i);
					if (para != null)
						log.debug("setParameter Where " + i + "=" + para);
					//
					if (para == null)
						;
					else if (para instanceof Integer)
					{
						Integer ii = (Integer)para;
						pstmt.setInt (pos++, ii.intValue());
					}
					else if (para instanceof BigDecimal)
						pstmt.setBigDecimal (pos++, (BigDecimal)para);
					else
						pstmt.setString(pos++, para.toString());
				}
			}
			catch (SQLException e)
			{
				log.error("Loader.setParameter", e);
			}
		}	//	setParameter

	}	//	Loader

}	//	MTable
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

import java.beans.*;
import java.io.*;
import java.math.*;
import java.sql.*;
import java.util.*;

import javax.swing.event.*;
import javax.swing.table.*;

import org.compiere.util.*;

/**
 *	Grid Table Model for JDBC access including buffering.
 *  <pre>
 *		The following data types are handeled
 *			Integer		for all IDs
 *			BigDecimal	for all Numbers
 *			Timestamp	for all Dates
 *			String		for all others
 *  The data is read via r/o resultset and cached in m_buffer. Writes/updates
 *  are via dynamically constructed SQL INSERT/UPDATE statements. The record
 *  is re-read via the resultset to get results of triggers.
 *
 *  </pre>
 *  The model maintains and fires the requires TableModelEvent changes,
 *  the DataChanged events (loading, changed, etc.)
 *  as well as Vetoable Change event "RowChange"
 *  (for row changes initiated by moving the row in the table grid).
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: MTable.java,v 1.68 2004/08/26 02:04:12 jjanke Exp $
 */
public final class MTable extends AbstractTableModel
	implements Serializable
{
	/**
	 *	JDBC Based Buffered Table
	 *
	 *  @param ctx Properties
	 *  @param TableName table name
	 *  @param WindowNo window no
	 *  @param TabNo tab no
	 *  @param withAccessControl    if true adds AD_Client/Org restrictuins
	 */
	public MTable(Properties ctx, int AD_Table_ID, String TableName, int WindowNo, int TabNo,
		boolean withAccessControl)
	{
		super();
		log.info(TableName);
		m_ctx = ctx;
		m_AD_Table_ID = AD_Table_ID;
		setTableName(TableName);
		m_WindowNo = WindowNo;
		m_TabNo = TabNo;
		m_withAccessControl = withAccessControl;
	}	//	MTable

	private Logger				log = Logger.getCLogger(getClass());
	private Properties          m_ctx;
	private int					m_AD_Table_ID;
	private String 		        m_tableName = "";
	private int				    m_WindowNo;
	private int				    m_TabNo;
	private boolean			    m_withAccessControl;
	private boolean			    m_readOnly = true;
	private boolean			    m_deleteable = true;
	//

	/**	Rowcount                    */
	private int				    m_rowCount = 0;
	/**	Has Data changed?           */
	private boolean			    m_changed = false;
	/** Index of changed row via SetValueAt */
	private int				    m_rowChanged = -1;
	/** Insert mode active          */
	private boolean			    m_inserting = false;
	/** Inserted Row number         */
	private int                 m_newRow = -1;
	/**	Is the Resultset open?      */
	private boolean			    m_open = false;
	/**	Compare to DB before save	*/
	private boolean				m_compareDB = true;		//	set to true after every save

	//	The buffer for all data
	private volatile ArrayList	m_buffer = new ArrayList(100);
	private volatile ArrayList	m_sort = new ArrayList(100);
	/** Original row data               */
	private Object[]			m_rowData = null;
	/** Original data [row,col,data]    */
	private Object[]            m_oldValue = null;
	//
	private Loader		        m_loader = null;

	/**	Columns                 		*/
	private ArrayList	        m_fields = new ArrayList(30);
	private ArrayList 	        m_parameterSELECT = new ArrayList(5);
	private ArrayList 	        m_parameterWHERE = new ArrayList(5);

	/** Complete SQL statement          */
	private String 		        m_SQL;
	/** SQL Statement for Row Count     */
	private String 		        m_SQL_Count;
	/** The SELECT clause with FROM     */
	private String 		        m_SQL_Select;
	/** The static where clause         */
	private String 		        m_whereClause = "";
	/** Show only Processed='N' and last 24h records    */
	private boolean		        m_onlyCurrentRows = false;
	/** Show only Not processed and x days				*/
	private int					m_onlyCurrentDays = 1;
	/** Static ORDER BY clause          */
	private String		        m_orderClause = "";

	/** Index of Key Column                 */
	private int			        m_indexKeyColumn = -1;
	/** Index of RowID column               */
	private int                 m_indexRowIDColumn = -1;
	/** Index of Color Column               */
	private int			        m_indexColorColumn = -1;
	/** Index of Processed Column           */
	private int                 m_indexProcessedColumn = -1;
	/** Index of IsActive Column            */
	private int                 m_indexActiveColumn = -1;
	/** Index of AD_Client_ID Column        */
	private int					m_indexClientColumn = -1;
	/** Index of AD_Org_ID Column           */
	private int					m_indexOrgColumn = -1;

	/** List of DataStatus Listeners    */
	private Vector 		        m_dataStatusListeners;
	/** Vetoable Change Bean support    */
	private VetoableChangeSupport   m_vetoableChangeSupport = new VetoableChangeSupport(this);
	/** Property of Vetoable Bean support "RowChange" */
	public static final String  PROPERTY = "MTable-RowSave";

	/**
	 *	Set Table Name
	 *  @param newTableName table name
	 */
	public void setTableName(String newTableName)
	{
		if (m_open)
		{
			log.error("setTableName - Table already open - ignored");
			return;
		}
		if (newTableName == null || newTableName.length() == 0)
			return;
		m_tableName = newTableName;
	}	//	setTableName

	/**
	 *	Get Table Name
	 *  @return table name
	 */
	public String getTableName()
	{
		return m_tableName;
	}	//	getTableName

	/**
	 *	Set Where Clause (w/o the WHERE and w/o History).
	 *  @param newWhereClause sql where clause
	 *  @param onlyCurrentRows only current rows
	 *  @param onlyCurrentDays how many days back for current
	 *	@return true if where clase set
	 */
	public boolean setWhereClause(String newWhereClause, boolean onlyCurrentRows, int onlyCurrentDays)
	{
		if (m_open)
		{
			log.error("setWhereClause - Table already open - ignored");
			return false;
		}
		//
		m_whereClause = newWhereClause;
		m_onlyCurrentRows = onlyCurrentRows;
		m_onlyCurrentDays = onlyCurrentDays;
		if (m_whereClause == null)
			m_whereClause = "";
		return true;
	}	//	setWhereClause

	/**
	 *	Get Where Clause (w/o the WHERE and w/o History)
	 *  @return where clause
	 */
	public String getWhereClause()
	{
		return m_whereClause;
	}	//	getWhereClause

	/**
	 *	Is History displayed
	 *  @return true if history displayed
	 */
	public boolean isOnlyCurrentRowsDisplayed()
	{
		return !m_onlyCurrentRows;
	}	//	isHistoryDisplayed

	/**
	 *	Set Order Clause (w/o the ORDER BY)
	 *  @param newOrderClause sql order by clause
	 */
	public void setOrderClause(String newOrderClause)
	{
		m_orderClause = newOrderClause;
		if (m_orderClause == null)
			m_orderClause = "";
	}	//	setOrderClause

	/**
	 *	Get Order Clause (w/o the ORDER BY)
	 *  @return order by clause
	 */
	public String getOrderClause()
	{
		return m_orderClause;
	}	//	getOrderClause

	/**
	 *	Assemble & store
	 *	m_SQL and m_countSQL
	 *  @return m_SQL
	 */
	private String createSelectSql()
	{
		if (m_fields.size() == 0 || m_tableName == null || m_tableName.equals(""))
			return "";

		//	Create SELECT Part
		StringBuffer select = new StringBuffer("SELECT ");
		for (int i = 0; i < m_fields.size(); i++)
		{
			if (i > 0)
				select.append(",");
			MField field = (MField)m_fields.get(i);
			select.append(field.getColumnName());
		}
		//
		select.append(" FROM ").append(m_tableName);
		m_SQL_Select = select.toString();
		m_SQL_Count = "SELECT COUNT(*) FROM " + m_tableName;
		//

		StringBuffer where = new StringBuffer("");
		//	WHERE
		if (m_whereClause.length() > 0)
		{
			where.append(" WHERE ");
			if (m_whereClause.indexOf("@") == -1)
				where.append(m_whereClause);
			else    //  replace variables
				where.append(Env.parseContext(m_ctx, m_WindowNo, m_whereClause, false));
		}
		if (m_onlyCurrentRows)
		{
			if (where.toString().indexOf(" WHERE ") == -1)
				where.append(" WHERE ");
			else
				where.append(" AND ");
			//	Show only unprocessed or the one updated within x days
			where.append("(Processed='N' OR Updated>SysDate-").append(m_onlyCurrentDays).append(")");
		}

		//	RO/RW Access
		m_SQL = m_SQL_Select + where.toString();
		m_SQL_Count += where.toString();
		if (m_withAccessControl)
		{
			boolean ro = MRole.SQL_RO;
		//	if (!m_readOnly)
		//		ro = MRole.SQL_RW;
			m_SQL = MRole.getDefault(m_ctx, false).addAccessSQL(m_SQL, 
				m_tableName, MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO);
			m_SQL_Count = MRole.getDefault(m_ctx, false).addAccessSQL(m_SQL_Count, 
				m_tableName, MRole.SQL_FULLYQUALIFIED, MRole.SQL_RO);
		}

		//	ORDER BY
		if (!m_orderClause.equals(""))
			m_SQL += " ORDER BY " + m_orderClause;
		//
		log.debug("createSelectSql - " + m_SQL_Count);
		Env.setContext(m_ctx, m_WindowNo, m_TabNo, "SQL", m_SQL);
		return m_SQL;
	}	//	createSelectSql

	/**
	 *	Add Field to Table
	 *  @param field field
	 */
	public void addField (MField field)
	{
		log.debug ("addField (" + m_tableName + ") - " + field.getColumnName());
		if (m_open)
		{
			log.error("addField - Table already open - ignored: " + field.getColumnName());
			return;
		}
		if (!MRole.getDefault(m_ctx, false).isColumnAccess (m_AD_Table_ID, field.getAD_Column_ID(), true))
		{
			log.debug ("addField - No Column Access " + field.getColumnName());
			return;			
		}
		//  Set Index for RowID column
		if (field.getDisplayType() == DisplayType.RowID)
			m_indexRowIDColumn = m_fields.size();
		//  Set Index for Key column
		if (field.isKey())
			m_indexKeyColumn = m_fields.size();
		else if (field.getColumnName().equals("IsActive"))
			m_indexActiveColumn = m_fields.size();
		else if (field.getColumnName().equals("Processed"))
			m_indexProcessedColumn = m_fields.size();
		else if (field.getColumnName().equals("AD_Client_ID"))
			m_indexClientColumn = m_fields.size();
		else if (field.getColumnName().equals("AD_Org_ID"))
			m_indexOrgColumn = m_fields.size();
		//
		m_fields.add(field);
	}	//	addColumn

	/**
	 *  Returns database column name
	 *
	 *  @param index  the column being queried
	 *  @return column name
	 */
	public String getColumnName (int index)
	{
		if (index < 0 || index > m_fields.size())
		{
			log.error("getColumnName - invalid index=" + index);
			return "";
		}
		//
		MField field = (MField)m_fields.get(index);
		return field.getColumnName();
	}   //  getColumnName

	/**
	 * Returns a column given its name.
	 *
	 * @param columnName string containing name of column to be located
	 * @return the column index with <code>columnName</code>, or -1 if not found
	 */
	public int findColumn (String columnName)
	{
		for (int i = 0; i < m_fields.size(); i++)
		{
			MField field = (MField)m_fields.get(i);
			if (columnName.equals(field.getColumnName()))
				return i;
		}
		return -1;
	}   //  findColumn

	/**
	 *  Returns Class of database column/field
	 *
	 *  @param index  the column being queried
	 *  @return the class
	 */
	public Class getColumnClass (int index)
	{
		if (index < 0 || index >= m_fields.size())
		{
			log.error("getColumnClass - invalid index=" + index);
			return null;
		}
		MField field = (MField)m_fields.get(index);
		return DisplayType.getClass(field.getDisplayType(), false);
	}   //  getColumnClass

	/**
	 *	Set Select Clause Parameter.
	 *	Assumes that you set parameters starting from index zero
	 *  @param index index
	 *  @param parameter parameter
	 */
	public void setParameterSELECT (int index, Object parameter)
	{
		if (index >= m_parameterSELECT.size())
			m_parameterSELECT.add(parameter);
		else
			m_parameterSELECT.set(index, parameter);
	}	//	setParameterSELECT

	/**
	 *	Set Where Clause Parameter.
	 *	Assumes that you set parameters starting from index zero
	 *  @param index index
	 *  @param parameter parameter
	 */
	public void setParameterWHERE (int index, Object parameter)
	{
		if (index >= m_parameterWHERE.size())
			m_parameterWHERE.add(parameter);
		else
			m_parameterWHERE.set(index, parameter);
	}	//	setParameterWHERE


	/**
	 *	Get Column at index
	 *  @param index index
	 *  @return MField
	 */
	protected MField getField (int index)
	{
		if (index < 0 || index >= m_fields.size())
			return null;
		return (MField)m_fields.get(index);
	}	//	getColumn

	/**
	 *	Return Columns with Indentifier (ColumnName)
	 *  @param identifier column name
	 *  @return MField
	 */
	protected MField getField (String identifier)
	{
		if (identifier == null || identifier.length() == 0)
			return null;
		int cols = m_fields.size();
		for (int i = 0; i < cols; i++)
		{
			MField field = (MField)m_fields.get(i);
			if (identifier.equalsIgnoreCase(field.getColumnName()))
				return field;
		}
	//	log.error ("getField - not found: '" + identifier + "'");
		return null;
	}	//	getField

	/**
	 *  Get all Fields
	 *  @return MFields
	 */
	public MField[] getFields ()
	{
		MField[] retValue = new MField[m_fields.size()];
		m_fields.toArray(retValue);
		return retValue;
	}   //  getField

	
	/**************************************************************************
	 *	Open Database.
	 *  if already opened, data is refreshed
	 *
	 *	@return true if success
	 */
	public boolean open ()
	{
		log.info("open");
		if (m_open)
		{
			log.debug("open - already open");
			dataRefreshAll();
			return true;
		}

		//	create m_SQL and m_countSQL
		createSelectSql();
		if (m_SQL == null || m_SQL.equals(""))
		{
			log.error("open - No SQL");
			return false;
		}

		//	Start Loading
		m_loader = new Loader();
		m_rowCount = m_loader.open();
		m_buffer = new ArrayList(m_rowCount+10);
		m_sort = new ArrayList(m_rowCount+10);
		if (m_rowCount > 0)
			m_loader.start();
		else
			m_loader.close();
		m_open = true;
		//
		m_changed = false;
		m_rowChanged = -1;
		return true;
	}	//	open

	/**
	 *  Wait until async loader of Table and Lookup Fields is complete
	 *  Used for performance tests
	 */
	public void loadComplete()
	{
		//  Wait for loader
		if (m_loader != null)
		{
			if (m_loader.isAlive())
			{
				try
				{
					m_loader.join();
				}
				catch (InterruptedException ie)
				{
					log.error("loadComplete - join interrupted", ie);
				}
			}
		}
		//  wait for field lookup loaders
		for (int i = 0; i < m_fields.size(); i++)
		{
			MField field = (MField)m_fields.get(i);
			field.lookupLoadComplete();
		}
	}   //  loadComplete

	/**
	 *  Is Loading
	 *  @return true if loading
	 */
	public boolean isLoading()
	{
		if (m_loader != null && m_loader.isAlive())
			return true;
		return false;
	}   //  isLoading

	/**
	 *	Is it open?
	 *  @return true if opened
	 */
	public boolean isOpen()
	{
		return m_open;
	}	//	isOpen

	/**
	 *	Close Resultset
	 *  @param finalCall final call
	 */
	public void close (boolean finalCall)
	{
		if (!m_open)
			return;
		log.debug("close - final=" + finalCall);

		//  remove listeners
		if (finalCall)
		{
			m_dataStatusListeners.clear();
			EventListener evl[] = listenerList.getListeners(TableModelListener.class);
			for (int i = 0; i < evl.length; i++)
				listenerList.remove(TableModelListener.class, evl[i]);
			VetoableChangeListener vcl[] = m_vetoableChangeSupport.getVetoableChangeListeners();
			for (int i = 0; i < vcl.length; i++)
				m_vetoableChangeSupport.removeVetoableChangeListener(vcl[i]);
		}

		//	Stop loader
		while (m_loader != null && m_loader.isAlive())
		{
			log.debug("close - interrupting Loader");
			m_loader.interrupt();
			try
			{
				Thread.sleep(200);		//	.2 second
			}
			catch (InterruptedException ie)
			{}
		}

		if (!m_inserting)
			dataSave(true);

		if (m_buffer != null)
			m_buffer.clear();
		m_buffer = null;
		if (m_sort != null)
			m_sort.clear();
		m_sort = null;

		if (finalCall)
			dispose();

		//  Fields are disposed from MTab
		log.debug("close - complete");
		m_open = false;
	}	//	close

	/**
	 *  Dispose MTable.
	 *  Called by close-final
	 */
	private void dispose()
	{
		//  MFields
		for (int i = 0; i < m_fields.size(); i++)
			((MField)m_fields.get(i)).dispose();
		m_fields.clear();
		m_fields = null;
		//
		m_dataStatusListeners = null;
		m_vetoableChangeSupport = null;
		//
		m_parameterSELECT.clear();
		m_parameterSELECT = null;
		m_parameterWHERE.clear();
		m_parameterWHERE = null;
		//  clear data arrays
		m_buffer = null;
		m_sort = null;
		m_rowData = null;
		m_oldValue = null;
		m_loader = null;
	}   //  dispose

	/**
	 *	Get total database column count (displayed and not displayed)
	 *  @return column count
	 */
	public int getColumnCount()
	{
		return m_fields.size();
	}	//	getColumnCount

	/**
	 *	Get (displayed) field count
	 *  @return field count
	 */
	public int getFieldCount()
	{
		return m_fields.size();
	}	//	getFieldCount

	/**
	 *  Return number of rows
	 *  @return Number of rows or 0 if not opened
	 */
	public int getRowCount()
	{
		return m_rowCount;
	}	//	getRowCount

	/**
	 *	Set the Column to determine the color of the row
	 *  @param columnName column name
	 */
	public void setColorColumn (String columnName)
	{
		m_indexColorColumn = findColumn(columnName);
	}	//  setColorColumn

	/**
	 *	Get ColorCode for Row.
	 *  <pre>
	 *	If numerical value in compare column is
	 *		negative = -1,
	 *      positive = 1,
	 *      otherwise = 0
	 *  </pre>
	 *  @see #setColorColumn
	 *  @param row row
	 *  @return color code
	 */
	public int getColorCode (int row)
	{
		if (m_indexColorColumn  == -1)
			return 0;
		Object data = getValueAt(row, m_indexColorColumn);
		//	We need to have a Number
		if (data == null || !(data instanceof BigDecimal))
			return 0;
		int cmp = Env.ZERO.compareTo(data);
		if (cmp > 0)
			return -1;
		if (cmp < 0)
			return 1;
		return 0;
	}	//	getColorCode


	/**
	 *	Sort Entries by Column.
	 *  actually the rows are not sorted, just the access pointer ArrayList
	 *  with the same size as m_buffer with MSort entities
	 *  @param col col
	 *  @param ascending ascending
	 */
	public void sort (int col, boolean ascending)
	{
		log.info("sort #" + col + " " + ascending);
		if (getRowCount() == 0)
			return;
		MField field = getField (col);
		//	RowIDs are not sorted
		if (field.getDisplayType() == DisplayType.RowID)
			return;
		boolean isLookup = DisplayType.isLookup(field.getDisplayType());

		//	fill MSort entities with data entity
		for (int i = 0; i < m_sort.size(); i++)
		{
			MSort sort = (MSort)m_sort.get(i);
			Object[] rowData = (Object[])m_buffer.get(sort.index);
			if (isLookup)
				sort.data = field.getLookup().getDisplay(rowData[col]);	//	lookup
			else
				sort.data = rowData[col];								//	data
		}

		//	sort it
		MSort sort = new MSort(0, null);
		sort.setSortAsc(ascending);
		Collections.sort(m_sort, sort);
		//	update UI
		fireTableDataChanged();
		//  Info detected by MTab.dataStatusChanged and current row set to 0
		fireDataStatusIEvent("Sorted");
	}	//	sort

	/**
	 *	Get Key ID or -1 of none
	 *  @param row row
	 *  @return ID or -1
	 */
	public int getKeyID (int row)
	{
	//	Log.info("MTable.getKeyID - row=" + row + ", keyColIdx=" + m_indexKeyColumn);
		if (m_indexKeyColumn != -1)
		{
			try
			{
				Integer ii = (Integer)getValueAt(row, m_indexKeyColumn);
				if (ii == null)
					return -1;
				return ii.intValue();
			}
			catch (Exception e)     //  Alpha Key
			{
				return -1;
			}
		}
		return -1;
	}	//	getKeyID

	/**
	 *	Get Key ColumnName
	 *  @return key column name
	 */
	public String getKeyColumnName()
	{
		if (m_indexKeyColumn != -1)
			return getColumnName(m_indexKeyColumn);
		return "";
	}	//	getKeyColumnName

	/**
	 *	Get Selected ROWID or null, if no RowID exists
	 *  @param row row
	 *  @return ROWID
	 */
	public Object getRowID (int row)
	{
		Object[] rid = getRID(row);
		if (rid == null)
			return null;
		return rid[0];
	}	//	getSelectedRowID

	/**
	 *	Get RowID Structure [0]=RowID, [1]=Selected, [2]=ID.
	 *  <p>
	 *  Either RowID or ID is populated (views don't have RowID)
	 *  @param row row
	 *  @return RowID
	 */
	public Object[] getRID (int row)
	{
		if (m_indexRowIDColumn == -1 || row < 0 || row >= getRowCount())
			return null;
		return (Object[])getValueAt(row, m_indexRowIDColumn);
	}	//	getRID

	/**
	 *	Find Row with RowID
	 *  @param RowID row id or oid
	 *	@return number of row or 0 if not found
	 */
	public int getRow (Object RowID)
	{
		if (RowID == null)
			return 0;

		//	the value to find
		String find = RowID.toString();

		//	Wait a bit to load rows
		if (m_loader != null && m_loader.isAlive())
		{
			try
			{
				Thread.sleep(250);		//	1/4 second
			}
			catch (InterruptedException ie)
			{}
		}

		//	Build search vector
		int size = m_sort.size();		//	may still change
		ArrayList search = new ArrayList(size);
		for (int i = 0; i < size; i++)
		{
			Object[] r = (Object[])getValueAt(i, 0);
			String s = r[0].toString();
			MSort so = new MSort(i, s);
			search.add(so);
		}

		//	Sort it
		MSort sort = new MSort(0, null);
		Collections.sort(search, sort);

		//	Find it
		int index = Collections.binarySearch(search, find, sort);
		if (index < 0)	//	not found
		{
			search.clear();
			return 0;
		}
		//	Get Info
		MSort result = (MSort)search.get(index);
		//	Clean up
		search.clear();
		return result.index;
	}	//	getRow


	/**************************************************************************
	 * 	Get Value in Resultset
	 *  @param row row
	 *  @param col col
	 *  @return Object of that row/column
	 */
	public Object getValueAt (int row, int col)
	{
	//	Log.trace(Log.l4_Data, "MTable.getValueAt r=" + row + " c=" + col);
		if (!m_open || row < 0 || col < 0 || row >= m_rowCount)
		{
		//	Log.trace(Log.l5_DData, "Out of bounds - Open=" + m_open + ", RowCount=" + m_rowCount);
			return null;
		}

		//	need to wait for data read into buffer
		int loops = 0;
		while (row >= m_buffer.size() && m_loader.isAlive() && loops < 15)
		{
			log.debug("getValueAt - waiting for loader row=" + row + ", size=" + m_buffer.size());
			try
			{
				Thread.sleep(500);		//	1/2 second
			}
			catch (InterruptedException ie)
			{}
			loops++;
		}

		//	empty buffer
		if (row >= m_buffer.size())
		{
		//	Log.trace(Log.l5_DData, "Empty buffer");
			return null;
		}

		//	return Data item
		MSort sort = (MSort)m_sort.get(row);
		Object[] rowData = (Object[])m_buffer.get(sort.index);
		//	out of bounds
		if (rowData == null || col > rowData.length)
		{
		//	Log.trace(Log.l5_DData, "No data or Column out of bounds");
			return null;
		}
		return rowData[col];
	}	//	getValueAt

	/**
	 *	Indicate that there will be a change
	 *  @param changed changed
	 */
	public void setChanged (boolean changed)
	{
		//	Can we edit?
		if (!m_open || m_readOnly)
			return;

		//	Indicate Change
		m_changed = changed;
		if (!changed)
			m_rowChanged = -1;
		fireDataStatusIEvent("");
	}	//	setChanged

	/**
	 * 	Set Value in data and update MField.
	 *  (called directly or from JTable.editingStopped())
	 *
	 *  @param  value value to assign to cell
	 *  @param  row row index of cell
	 *  @param  col column index of cell
	 */
	public final void setValueAt (Object value, int row, int col)
	{
		setValueAt (value, row, col, false);
	}	//	setValueAt

	/**
	 * 	Set Value in data and update MField.
	 *  (called directly or from JTable.editingStopped())
	 *
	 *  @param  value value to assign to cell
	 *  @param  row row index of cell
	 *  @param  col column index of cell
	 * 	@param	force force setting new value
	 */
	public final void setValueAt (Object value, int row, int col, boolean force)
	{
		//	Can we edit?
		if (!m_open || m_readOnly       //  not accessible
				|| row < 0 || col < 0   //  invalid index
				|| col == 0             //  cannot change ID
				|| m_rowCount == 0)     //  no rows
			return;

		dataSave(row, false);

		//	Has anything changed?
		Object oldValue = getValueAt(row, col);
		if (!force && (
			(oldValue == null && value == null)
			||	(oldValue != null && oldValue.equals(value))
			||	(oldValue != null && value != null && oldValue.toString().equals(value.toString()))
			))
			return;

		log.debug("setValueAt r=" + row + " c=" + col + " = " + value + " (" + oldValue + ")");

		//  Save old value
		m_oldValue = new Object[3];
		m_oldValue[0] = new Integer(row);
		m_oldValue[1] = new Integer(col);
		m_oldValue[2] = oldValue;

		//	Set Data item
		MSort sort = (MSort)m_sort.get(row);
		Object[] rowData = (Object[])m_buffer.get(sort.index);
		m_rowChanged = row;

		//	Selection
		if (col == 0)
		{
			rowData[col] = value;
			m_buffer.set(sort.index, rowData);
			return;
		}

		//	save original value - shallow copy
		if (m_rowData == null)
		{
			int size = m_fields.size();
			m_rowData = new Object[size];
			for (int i = 0; i < size; i++)
				m_rowData[i] = rowData[i];
		}

		//	save & update
		rowData[col] = value;
		m_buffer.set(sort.index, rowData);
		//  update Table
		fireTableCellUpdated(row, col);
		//  update MField
		MField field = getField(col);
		field.setValue(value, m_inserting);
		//  inform
		DataStatusEvent evt = createDSE();
		evt.setChangedColumn(col);
		fireDataStatusChanged(evt);
	}	//	setValueAt

	/**
	 *  Get Old Value
	 *  @param row row
	 *  @param col col
	 *  @return old value
	 */
	public Object getOldValue (int row, int col)
	{
		if (m_oldValue == null)
			return null;
		if (((Integer)m_oldValue[0]).intValue() == row
				&& ((Integer)m_oldValue[1]).intValue() == col)
			return m_oldValue[2];
		return null;
	}   // getOldValue

	/**
	 *	Check if the current row needs to be saved.
	 *  @param  onlyRealChange if true the value of a field was actually changed
	 *  (e.g. for new records, which have not been changed) - default false
	 *	@return true it needs to be saved
	 */
	public boolean needSave(boolean onlyRealChange)
	{
		return needSave(m_rowChanged, onlyRealChange);
	}   //  needSave

	/**
	 *	Check if the row needs to be saved.
	 *  - only if nothing was changed
	 *	@return true it needs to be saved
	 */
	public boolean needSave()
	{
		return needSave(m_rowChanged, false);
	}   //  needSave

	/**
	 *	Check if the row needs to be saved.
	 *  - only when row changed
	 *  - only if nothing was changed
	 *	@param	newRow to check
	 *	@return true it needs to be saved
	 */
	public boolean needSave(int newRow)
	{
		return needSave(newRow, false);
	}   //  needSave

	/**
	 *	Check if the row needs to be saved.
	 *  - only when row changed
	 *  - only if nothing was changed
	 *	@param	newRow to check
	 *  @param  onlyRealChange if true the value of a field was actually changed
	 *  (e.g. for new records, which have not been changed) - default false
	 *	@return true it needs to be saved
	 */
	public boolean needSave(int newRow, boolean onlyRealChange)
	{
		log.debug("needSave - Row=" + newRow +
			", Changed=" + m_rowChanged + "/" + m_changed);  //  m_rowChanged set in setValueAt
		//  nothing done
		if (!m_changed && m_rowChanged == -1)
			return false;
		//  E.g. New unchanged records
		if (m_changed && m_rowChanged == -1 && onlyRealChange)
			return false;
		//  same row
		if (newRow == m_rowChanged)
			return false;

		return true;
	}	//	needSave

	/*************************************************************************/

	public static final char	SAVE_OK = 'O';			//	the only OK condition
	public static final char	SAVE_ERROR = 'E';
	public static final char	SAVE_ACCESS = 'A';
	public static final char	SAVE_MANDATORY = 'M';
	public static final char	SAVE_ABORT = 'U';

	/**
	 *	Check if it needs to be saved and save it.
	 *  @param newRow row
	 *  @param manualCmd manual command to save
	 *	@return true if not needed to be saved or successful saved
	 */
	public boolean dataSave (int newRow, boolean manualCmd)
	{
		log.debug("dataSave - Row=" + newRow +
			", Changed=" + m_rowChanged + "/" + m_changed);  //  m_rowChanged set in setValueAt
		//  nothing done
		if (!m_changed && m_rowChanged == -1)
			return true;
		//  same row, don't save yet
		if (newRow == m_rowChanged)
			return true;

		return (dataSave(manualCmd) == SAVE_OK);
	}   //  dataSave

	/**
	 *	Save unconditional.
	 *  @param manualCmd if true, no vetoable PropertyChange will be fired for save confirmation
	 *	@return OK Or Error condition
	 *  Error info (Access*, FillMandatory, SaveErrorNotUnique,
	 *  SaveErrorRowNotFound, SaveErrorDataChanged) is saved in the log
	 */
	public char dataSave (boolean manualCmd)
	{
		//	cannot save
		if (!m_open)
		{
			log.warn ("dataSave - Error - Open=" + m_open);
			return SAVE_ERROR;
		}
		//	no need - not changed - row not positioned - no Value changed
		if (m_rowChanged == -1)
		{
			log.info("dataSave - NoNeed - Changed=" + m_changed + ", Row=" + m_rowChanged);
		//	return SAVE_ERROR;
			if (!manualCmd)
				return SAVE_OK;
		}
		//  Value not changed
		if (m_rowData == null)
		{
			log.warn ("dataSave - Error - DataNull=" + (m_rowData == null));
			return SAVE_ERROR;
		}

		if (m_readOnly)
		//	If Processed - not editable (Find always editable)  -> ok for changing payment terms, etc.
		{
			log.warn("dataSave - IsReadOnly - ignored");
			dataIgnore();
			return SAVE_ACCESS;
		}

		//	row not positioned - no Value changed
		if (m_rowChanged == -1)
		{
			if (m_newRow != -1)     //  new row and nothing changed - might be OK
				m_rowChanged = m_newRow;
			else
			{
				fireDataStatusEEvent("SaveErrorNoChange", "");
				return SAVE_ERROR;
			}
		}

		//	Can we change?
		int[] co = getClientOrg(m_rowChanged);
		int AD_Client_ID = co[0]; 
		int AD_Org_ID = co[1];
		if (!MRole.getDefault(m_ctx, false).canUpdate(AD_Client_ID, AD_Org_ID, m_AD_Table_ID, true))
		{
			fireDataStatusEEvent(Log.retrieveError());
			dataIgnore();
			return SAVE_ACCESS;
		}

		log.info("dataSave - Saving row " + m_rowChanged);

		//  inform about data save action, if not manually initiated
		try
		{
			if (!manualCmd)
				m_vetoableChangeSupport.fireVetoableChange(PROPERTY, 0, m_rowChanged);
		}
		catch (PropertyVetoException pve)
		{
			log.warn("dataSave - " + pve.getMessage());
			dataIgnore();
			return SAVE_ABORT;
		}

		//	get updated row data
		MSort sort = (MSort)m_sort.get(m_rowChanged);
		Object[] rowData = (Object[])m_buffer.get(sort.index);

		//	Check Mandatory
		String missingColumns = getMandatory(rowData);
		if (missingColumns.length() != 0)
		{
			fireDataStatusEEvent("FillMandatory", missingColumns + "\n");
			return SAVE_MANDATORY;
		}

		/**
		 *	Update row *****
		 */
		int Record_ID = 0;
		if (!m_inserting)
			Record_ID = getKeyID(m_rowChanged);
		try
		{
			if (!m_tableName.endsWith("_Trl"))	//	translation tables have no model
				return dataSavePO (Record_ID);
		}
		catch (Exception e)
		{
			if (e instanceof IllegalStateException)
				log.error("MTable.dataSave - " + m_tableName + " - " + e.getLocalizedMessage());
			else
			{
				log.error("MTable.dataSave - Persistency Issue - " + m_tableName, e);
				return SAVE_ERROR;
			}
		}
		
		
		boolean error = false;
		lobReset();
		//
		String is = null;
		final String ERROR = "ERROR: ";
		final String INFO  = "Info: ";

		//	SQL with specific where clause
		String SQL = m_SQL_Select;
		StringBuffer refreshSQL = new StringBuffer(SQL).append(" WHERE ");	//	to be completed when key known
		StringBuffer singleRowWHERE = new StringBuffer();
		StringBuffer multiRowWHERE = new StringBuffer();
		//	Create SQL	& RowID
		Object rowID = null;
		if (m_inserting)
		{
			SQL += " WHERE 1=2";
		}
		else
		{
			//  FOR UPDATE causes  -  ORA-01002 fetch out of sequence
			SQL += " WHERE ROWID=?";
			rowID = getRowID (m_rowChanged);
		}
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			if (!m_inserting)
				DB.getDatabase().setRowID(pstmt, 1, rowID);
			ResultSet rs = pstmt.executeQuery();
			//	only one row
			if (!(m_inserting || rs.next()))
			{
				rs.close();
				pstmt.close();
				fireDataStatusEEvent("SaveErrorRowNotFound", "");
				dataRefresh(m_rowChanged);
				return SAVE_ERROR;
			}

			Object[] rowDataDB = null;
			//	Prepare
			boolean manualUpdate = ResultSet.CONCUR_READ_ONLY == rs.getConcurrency();
			if (manualUpdate)
				createUpdateSqlReset();
			if (m_inserting)
			{
				if (manualUpdate)
					log.debug("dataSave - prepare inserting ... manual");
				else
				{
					log.debug ("dataSave - prepare inserting ... RowSet");
					rs.moveToInsertRow ();
				}
			}
			else
			{
				log.debug("dataSave - prepare updating ... manual=" + manualUpdate);
				//	get current Data in DB
				rowDataDB = readData (rs);
			}

			/**	Data:
			 *		m_rowData	= original Data
			 *		rowData 	= updated Data
			 *		rowDataDB	= current Data in DB
			 *	1) Difference between original & updated Data?	N:next
			 *	2) Difference between original & current Data?	Y:don't update
			 *	3) Update current Data
			 *	4) Refresh to get last Data (changed by trigger, ...)
			 */

			//	Constants for Created/Updated(By)
			Timestamp now = new Timestamp(System.currentTimeMillis());
			int user = Env.getContextAsInt(m_ctx, "#AD_User_ID");

			/**
			 *	for every column
			 */
			int size = m_fields.size();
			for (int col = 0; col < size; col++)
			{
				MField field = (MField)m_fields.get (col);
				String columnName = field.getColumnName ();
			//	log.debug ("dataSave - " + columnName + "= " + m_rowData[col] + " <> DB: " + rowDataDB[col] + " -> " + rowData[col]);

				//	RowID
				if (field.getDisplayType () == DisplayType.RowID)
					; //	ignore

				//	New Key
				else if (field.isKey () && m_inserting)
				{
					if (columnName.endsWith ("_ID") || columnName.toUpperCase().endsWith ("_ID"))
					{
						int insertID = DB.getNextID (m_ctx, m_tableName, null);	//	no trx
						if (manualUpdate)
							createUpdateSql (columnName, String.valueOf (insertID));
						else
							rs.updateInt (col + 1, insertID); 						// ***
						singleRowWHERE.append (columnName).append ("=").append (insertID);
						//
						is = INFO + columnName + " -> " + insertID + " (Key)";
					}
					else //	Key with String value
					{
						String str = rowData[col].toString ();
						if (manualUpdate)
							createUpdateSql (columnName, DB.TO_STRING (str));
						else
							rs.updateString (col + 1, str); 						// ***
						singleRowWHERE = new StringBuffer();	//	overwrite
						singleRowWHERE.append (columnName).append ("=").append (DB.TO_STRING(str));
						//
						is = INFO + columnName + " -> " + str + " (StringKey)";
					}
					log.debug ("dataSave - " + is);
				} //	New Key

				//	New DocumentNo
				else if (columnName.equals ("DocumentNo"))
				{
					boolean newDocNo = false;
					String docNo = (String)rowData[col];
					//  we need to have a doc number
					if (docNo == null || docNo.length () == 0)
						newDocNo = true;
						//  Preliminary ID from CalloutSystem
					else if (docNo.startsWith ("<") && docNo.endsWith (">"))
						newDocNo = true;

					if (newDocNo || m_inserting)
					{
						String insertDoc = null;
						//  always overwrite if insering with mandatory DocType DocNo
						if (m_inserting)
							insertDoc = DB.getDocumentNo (m_ctx, m_WindowNo, 
								m_tableName, true, null);	//	only doc type - no trx
						log.debug ("dataSave - DocumentNo entered=" + docNo + ", DocTypeInsert=" + insertDoc + ", newDocNo=" + newDocNo);
						// can we use entered DocNo?
						if (insertDoc == null || insertDoc.length () == 0)
						{
							if (!newDocNo && docNo != null && docNo.length () > 0)
								insertDoc = docNo;
							else //  get a number from DocType or Table
								insertDoc = DB.getDocumentNo (m_ctx, m_WindowNo, 
									m_tableName, false, null);	//	no trx
						}
						//	There might not be an automatic document no for this document
						if (insertDoc == null || insertDoc.length () == 0)
						{
							//  in case DB function did not return a value
							if (docNo != null && docNo.length () != 0)
								insertDoc = (String)rowData[col];
							else
							{
								error = true;
								is = ERROR + field.getColumnName () + "= " + rowData[col] + " NO DocumentNo";
								log.debug ("dataSave - " + is);
								break;
							}
						}
						//
						if (manualUpdate)
							createUpdateSql (columnName, DB.TO_STRING (insertDoc));
						else
							rs.updateString (col + 1, insertDoc);					//	***
							//
						is = INFO + columnName + " -> " + insertDoc + " (DocNo)";
						log.debug ("dataSave - " + is);
					}
				}	//	New DocumentNo

				//  New Value(key)
				else if (columnName.equals ("Value") && m_inserting)
				{
					String value = (String)rowData[col];
					//  Get from Sequence, if not entered
					if (value == null || value.length () == 0)
					{
						value = DB.getDocumentNo (m_ctx, m_WindowNo, m_tableName, false, null);
						//  No Value
						if (value == null || value.length () == 0)
						{
							error = true;
							is = ERROR + field.getColumnName () + "= " + rowData[col]
								 + " No Value";
							log.debug ("dataSave - " + is);
							break;
						}
					}
					if (manualUpdate)
						createUpdateSql (columnName, DB.TO_STRING (value));
					else
						rs.updateString (col + 1, value); 							//	***
						//
					is = INFO + columnName + " -> " + value + " (Value)";
					log.debug ("dataSave - " + is);
				}	//	New Value(key)

				//	Updated		- check database
				else if (columnName.equals ("Updated"))
				{
					if (m_compareDB && !m_inserting && !m_rowData[col].equals (rowDataDB[col]))	//	changed
					{
						error = true;
						is = ERROR + field.getColumnName () + "= " + m_rowData[col]
							 + " != DB: " + rowDataDB[col];
						log.debug ("dataSave - " + is);
						break;
					}
					if (manualUpdate)
						createUpdateSql (columnName, DB.TO_DATE (now, false));
					else
						rs.updateTimestamp (col + 1, now); 							//	***
						//
					is = INFO + "Updated/By -> " + now + " - " + user;
					log.debug ("dataSave - " + is);
				} //	Updated

				//	UpdatedBy	- update
				else if (columnName.equals ("UpdatedBy"))
				{
					if (manualUpdate)
						createUpdateSql (columnName, String.valueOf (user));
					else
						rs.updateInt (col + 1, user); 								//	***
				} //	UpdatedBy

				//	Created
				else if (m_inserting && columnName.equals ("Created"))
				{
					if (manualUpdate)
						createUpdateSql (columnName, DB.TO_DATE (now, false));
					else
						rs.updateTimestamp (col + 1, now); 							//	***
				} //	Created

				//	CreatedBy
				else if (m_inserting && columnName.equals ("CreatedBy"))
				{
					if (manualUpdate)
						createUpdateSql (columnName, String.valueOf (user));
					else
						rs.updateInt (col + 1, user); 								//	***
				} //	CreatedBy

				//	Nothing changed & null
				else if (m_rowData[col] == null && rowData[col] == null)
				{
					if (m_inserting)
					{
						if (manualUpdate)
							createUpdateSql (columnName, "NULL");
						else
							rs.updateNull (col + 1); 								//	***
						is = INFO + columnName + "= NULL";
						log.debug ("dataSave - " + is);
					}
				}

				//	***	Data changed ***
				else if (m_inserting
				  || (m_rowData[col] == null && rowData[col] != null)
				  || (m_rowData[col] != null && rowData[col] == null)
				  || !m_rowData[col].equals (rowData[col])) 			//	changed
				{
					//	Original == DB
					if (m_inserting || !m_compareDB
					  || (m_rowData[col] == null && rowDataDB[col] == null)
					  || (m_rowData[col] != null && m_rowData[col].equals (rowDataDB[col])))
					{
						if (Log.isTraceLevel(10))
							log.debug("dataSave: " + columnName + "=" + rowData[col]
								+ " " + (rowData[col]==null ? "" : rowData[col].getClass().getName()));
						//
						String type = "String";
						if (rowData[col] == null)
						{
							if (manualUpdate)
								createUpdateSql (columnName, "NULL");
							else
								rs.updateNull (col + 1); 							//	***
						}
						
						//	ID - int
						else if (DisplayType.isID (field.getDisplayType()) 
							|| field.getDisplayType() == DisplayType.Integer)
						{
							int number = 0;
							try
							{
								number = Integer.parseInt (rowData[col].toString ());
								if (manualUpdate)
									createUpdateSql (columnName, String.valueOf (number));
								else
									rs.updateInt (col + 1, number); 			// 	***
							}
							catch (Exception e) //  could also be a String (AD_Language, AD_Message)
							{
								if (manualUpdate)
									createUpdateSql (columnName, DB.TO_STRING (rowData[col].toString ()));
								else
									rs.updateString (col + 1, rowData[col].toString ()); //	***
							}
							type = "Int";
						}
						//	Numeric - BigDecimal
						else if (DisplayType.isNumeric (field.getDisplayType ()))
						{
							if (manualUpdate)
								createUpdateSql (columnName, rowData[col].toString ());
							else
								rs.updateBigDecimal (col + 1, (BigDecimal)rowData[col]); //	***
							type = "Number";
						}
						//	Date - Timestamp
						else if (DisplayType.isDate (field.getDisplayType ()))
						{
							if (manualUpdate)
								createUpdateSql (columnName, DB.TO_DATE ((Timestamp)rowData[col], false));
							else
								rs.updateTimestamp (col + 1, (Timestamp)rowData[col]); //	***
							type = "Date";
						}
						//	LOB
						else if (field.getDisplayType() == DisplayType.TextLong)
						{
							PO_LOB lob = new PO_LOB (getTableName(), columnName, 
								null, null, field.getDisplayType(), rowData[col]);
							lobAdd(lob);
							type = "CLOB";
						}
						//	Boolean
						else if (field.getDisplayType() == DisplayType.YesNo)
						{
							String yn = null;
							if (rowData[col] instanceof Boolean)
							{
								Boolean bb = (Boolean)rowData[col];
								yn = bb.booleanValue() ? "Y" : "N";
							}
							else
								yn = "Y".equals(rowData[col]) ? "Y" : "N"; 
							if (manualUpdate)
								createUpdateSql (columnName, DB.TO_STRING (yn));
							else
								rs.updateString (col + 1, yn); //	***
						}
						//	String and others
						else	
						{
							if (manualUpdate)
								createUpdateSql (columnName, DB.TO_STRING (rowData[col].toString ()));
							else
								rs.updateString (col + 1, rowData[col].toString ()); //	***
						}
						//
						is = INFO + columnName + "= " + m_rowData[col]
							 + " -> " + rowData[col] + " (" + type + ")";
						log.debug ("dataSave - " + is);
					}
					//	Original != DB
					else
					{
						error = true;
						is = ERROR + field.getColumnName () + "= " + m_rowData[col]
							 + " != DB: " + rowDataDB[col] + " -> " + rowData[col];
						log.debug ("dataSave - " + is);
					}
				}	//	Data changed

				//	Single Key - retrieval sql
				if (field.isKey() && !m_inserting)
				{
					if (rowData[col] == null)
						throw new RuntimeException("dataSave - Key " + columnName + " is NULL");
					if (columnName.endsWith ("_ID"))
						singleRowWHERE.append (columnName).append ("=").append (rowData[col]);
					else
					{
						singleRowWHERE = new StringBuffer();	//	overwrite
						singleRowWHERE.append (columnName).append ("=").append (DB.TO_STRING(rowData[col].toString()));
					}
				}
				//	MultiKey Inserting - retrieval sql
				if (field.isParent())
				{
					if (rowData[col] == null)
						throw new RuntimeException("dataSave - MultiKey Parent " + columnName + " is NULL");
					if (multiRowWHERE.length() != 0)
						multiRowWHERE.append(" AND ");
					if (columnName.endsWith ("_ID"))
						multiRowWHERE.append (columnName).append ("=").append (rowData[col]);
					else
						multiRowWHERE.append (columnName).append ("=").append (DB.TO_STRING(rowData[col].toString()));
				}
			}	//	for every column

			if (error)
			{
				if (manualUpdate)
					createUpdateSqlReset();
				else
					rs.cancelRowUpdates();
				rs.close();
				pstmt.close();
				fireDataStatusEEvent("SaveErrorDataChanged", "");
				dataRefresh(m_rowChanged);
				return SAVE_ERROR;
			}

			/**
			 *	Save to Database
			 */
			//
			String whereClause = singleRowWHERE.toString();
			if (whereClause.length() == 0)
				whereClause = multiRowWHERE.toString();
			if (m_inserting)
			{
				log.debug("dataSave - inserting ...");
				if (manualUpdate)
				{
					String sql = createUpdateSql(true, null);
					int no = DB.executeUpdateEx (sql, null);	//	no Trx
					if (no != 1)
						log.error("dataSave - insert #=" + no + " - " + sql);
				}
				else
					rs.insertRow();
			}
			else
			{
				log.debug("dataSave - updating ... " + whereClause);
				if (manualUpdate)
				{
					String sql = createUpdateSql(false, whereClause);
					int no = DB.executeUpdateEx (sql, null);	//	no Trx
					if (no != 1)
						log.error("dataSave - update #=" + no + " - " + sql);
				}
				else
					rs.updateRow();
			}

			log.debug("dataSave - committing ...");
			DB.commit(true, null);	//	no Trx
			//
			lobSave(whereClause);
			//	data may be updated by trigger after update
			if (m_inserting || manualUpdate)
			{
				rs.close();
				pstmt.close();
				//	need to re-read row to get ROWID, Key, DocumentNo
				log.debug("dataSave - reading ... " + whereClause);
				refreshSQL.append(whereClause);
				pstmt = DB.prepareStatement(refreshSQL.toString());
				rs = pstmt.executeQuery();
				if (rs.next())
				{
					rowDataDB = readData(rs);
					//	update buffer
					m_buffer.set(sort.index, rowDataDB);
					fireTableRowsUpdated(m_rowChanged, m_rowChanged);
				}
				else
					log.error("dataSave - inserted row not found");
			}
			else
			{
				log.debug("dataSave - refreshing ...");
				rs.refreshRow();	//	only use
				rowDataDB = readData(rs);
				//	update buffer
				m_buffer.set(sort.index, rowDataDB);
				fireTableRowsUpdated(m_rowChanged, m_rowChanged);
			}
			//
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException e)
		{
			try
			{
				if (pstmt != null)
				  pstmt.close ();
				pstmt = null;
			}
			catch (Exception ex)
			{
			}

			String msg = "SaveError";
			if (e.getErrorCode() == 1)		//	Unique Constraint
			{
				log.error ("dataSave - Key Not Unique", e);
				msg = "SaveErrorNotUnique";
			}
			else
				log.error ("dataSave\nSQL= " + SQL, e);
			fireDataStatusEEvent(msg, e.getLocalizedMessage());
			return SAVE_ERROR;
		}

		//	everything ok
		m_rowData = null;
		m_changed = false;
		m_compareDB = true;
		m_rowChanged = -1;
		m_newRow = -1;
		m_inserting = false;
		fireDataStatusIEvent("Saved");
		//
		log.info("dataSave - fini");
		return SAVE_OK;
	}	//	dataSave

	/**
	 * 	Save via PO
	 *	@param Record_ID
	 *	@return
	 */
	private char dataSavePO (int Record_ID)
	{
		log.debug("dataSavePO - " + Record_ID);
		//
		MSort sort = (MSort)m_sort.get(m_rowChanged);
		Object[] rowData = (Object[])m_buffer.get(sort.index);
		//
		M_Table table = M_Table.get (m_ctx, m_AD_Table_ID);
		PO po = null;
		if (Record_ID != -1)
			po = table.getPO(Record_ID);
		else	//	Multi - Key
			po = table.getPO(getWhereClause(rowData));
		//	No Persistent Object
		if (po == null)
			throw new IllegalStateException("No Persistent Object");
		if (po == null)
		{
			ValueNamePair pp = Log.retrieveError();
			if (pp != null)
				fireDataStatusEEvent(pp);
			else
			{
				String msg = "SaveError";
				fireDataStatusEEvent(msg, "No Persistent Object");
			}
			return SAVE_ERROR;
		}
		
		int size = m_fields.size();
		for (int col = 0; col < size; col++)
		{
			MField field = (MField)m_fields.get (col);
			String columnName = field.getColumnName ();
			Object value = rowData[col];
			Object oldValue = m_rowData[col];
			//	RowID
			if (field.getDisplayType () == DisplayType.RowID)
				; 	//	ignore

			//	Nothing changed & null
			else if (oldValue == null && value == null)
				;	//	ignore
			
			//	***	Data changed ***
			else if (m_inserting
			  || (oldValue == null && value != null)
			  || (oldValue != null && value == null)
			  || !oldValue.equals (value)) 			//	changed
			{
				//	Check existence
				int poIndex = po.get_ColumnIndex(columnName);
				if (poIndex < 0)
				{
					//	Custom Fields not in PO
					po.set_CustomColumn(columnName, value);
				//	log.error("dataSavePO - Column not found: " + columnName);
					continue;
				}
				
				Object dbValue = po.get_Value(poIndex);
				if (m_inserting 
					|| !m_compareDB
					//	Original == DB
					|| (oldValue == null && dbValue == null)
					|| (oldValue != null && oldValue.equals (dbValue))
					//	Target == DB (changed by trigger to new value already)
					|| (value == null && dbValue == null)
					|| (value != null && value.equals (dbValue)) )
				{
					po.set_ValueNoCheck (columnName, value);
				}
				//	Original != DB
				else
				{
					fireDataStatusEEvent("SaveErrorDataChanged", 
						columnName 
						+ "= " + oldValue 
							+ (oldValue==null ? "" : "(" + oldValue.getClass().getName() + ")")
						+ " != DB: " + dbValue 
							+ (dbValue==null ? "" : "(" + dbValue.getClass().getName() + ")")
						+ " -> New: " + value 
							+ (value==null ? "" : "(" + value.getClass().getName() + ")"));
					dataRefresh(m_rowChanged);
					return SAVE_ERROR;
				}
			}	//	Data changed

		}	//	for every column

		if (!po.save())
		{
			String msg = "SaveError";
			String info = "";
			ValueNamePair pp = Log.retrieveError();
			if (pp != null)
			{
				msg = pp.getValue();
				info = pp.getName();
			}
			Exception ex = Log.retrieveException();
			if (ex != null 
				&& ex instanceof SQLException
				&& ((SQLException)ex).getErrorCode() == 1)
				msg = "SaveErrorNotUnique";
			fireDataStatusEEvent(msg, info);
			return SAVE_ERROR;
		}
		
		//	Refresh - update buffer
		String whereClause = po.get_WhereClause(true);
		log.debug("dataSavePO - reading ... " + whereClause);
		StringBuffer refreshSQL = new StringBuffer(m_SQL_Select)
			.append(" WHERE ").append(whereClause);
		PreparedStatement pstmt = DB.prepareStatement(refreshSQL.toString());
		try
		{
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				Object[] rowDataDB = readData(rs);
				//	update buffer
				m_buffer.set(sort.index, rowDataDB);
				fireTableRowsUpdated(m_rowChanged, m_rowChanged);
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException e)
		{
			try
			{
				if (pstmt != null)
				  pstmt.close ();
				pstmt = null;
			}
			catch (Exception ex)
			{
			}

			String msg = "SaveError";
			log.error ("dataSavePO - " + refreshSQL.toString(), e);
			fireDataStatusEEvent(msg, e.getLocalizedMessage());
			return SAVE_ERROR;
		}

		//	everything ok
		m_rowData = null;
		m_changed = false;
		m_compareDB = true;
		m_rowChanged = -1;
		m_newRow = -1;
		m_inserting = false;
		fireDataStatusIEvent("Saved");
		//
		log.info("dataSave - fini");
		return SAVE_OK;
	}	//	dataSavePO
	
	/**
	 * 	Get Where Clause
	 *	@param rowData data
	 *	@return where clause or null
	 */
	private String getWhereClause (Object[] rowData)
	{
		int size = m_fields.size();
		StringBuffer singleRowWHERE = null;
		StringBuffer multiRowWHERE = null;
		for (int col = 0; col < size; col++)
		{
			MField field = (MField)m_fields.get (col);
			if (field.isKey())
			{
				String columnName = field.getColumnName();
				Object value = rowData[col]; 
				if (value == null)
				{
					log.error("getWhereClause - PK data is null");
					return null;
				}
				if (columnName.endsWith ("_ID"))
					singleRowWHERE = new StringBuffer(columnName)
						.append ("=").append (value);
				else
					singleRowWHERE = new StringBuffer(columnName)
						.append ("=").append (DB.TO_STRING(value.toString()));
			}
			else if (field.isParent())
			{
				String columnName = field.getColumnName();
				Object value = rowData[col]; 
				if (value == null)
				{
					log.error("getWhereClause - FK data is null");
					return null;
				}
				if (multiRowWHERE == null)
					multiRowWHERE = new StringBuffer();
				else
					multiRowWHERE.append(" AND ");
				if (columnName.endsWith ("_ID"))
					multiRowWHERE.append (columnName)
						.append ("=").append (value);
				else
					multiRowWHERE.append (columnName)
						.append ("=").append (DB.TO_STRING(value.toString()));
			}
		}	//	for all columns
		if (singleRowWHERE != null)
			return singleRowWHERE.toString();
		if (multiRowWHERE != null)
			return multiRowWHERE.toString();
		log.error("getWhereClause - No key Found");
		return null;
	}	//	getWhereClause
	
	/*************************************************************************/

	private ArrayList	m_createSqlColumn = new ArrayList();
	private ArrayList	m_createSqlValue = new ArrayList();

	/**
	 * 	Prepare SQL creation
	 * 	@param columnName column name
	 * 	@param value value
	 */
	private void createUpdateSql (String columnName, String value)
	{
		m_createSqlColumn.add(columnName);
		m_createSqlValue.add(value);
		if (Log.isTraceLevel(10))
			log.debug("createUpdateSql #" + m_createSqlColumn.size()
				+ " - " + columnName + "=" + value);
	}	//	createUpdateSQL

	/**
	 * 	Create update/insert SQL
	 * 	@param insert true if insert - update otherwise
	 * 	@param whereClause where clause for update
	 * 	@return sql statement
	 */
	private String createUpdateSql (boolean insert, String whereClause)
	{
		StringBuffer sb = new StringBuffer();
		if (insert)
		{
			sb.append("INSERT INTO ").append(m_tableName).append(" (");
			for (int i = 0; i < m_createSqlColumn.size(); i++)
			{
				if (i != 0)
					sb.append(",");
				sb.append(m_createSqlColumn.get(i));
			}
			sb.append(") VALUES ( ");
			for (int i = 0; i < m_createSqlValue.size(); i++)
			{
				if (i != 0)
					sb.append(",");
				sb.append(m_createSqlValue.get(i));
			}
			sb.append(")");
		}
		else
		{
			sb.append("UPDATE ").append(m_tableName).append(" SET ");
			for (int i = 0; i < m_createSqlColumn.size(); i++)
			{
				if (i != 0)
					sb.append(",");
				sb.append(m_createSqlColumn.get(i)).append("=").append(m_createSqlValue.get(i));
			}
			sb.append(" WHERE ").append(whereClause);
		}
		log.debug("createUpdateSql=" + sb.toString());
		//	reset
		createUpdateSqlReset();
		return sb.toString();
	}	//	createUpdateSql

	/**
	 * 	Reset Update Data
	 */
	private void createUpdateSqlReset()
	{
		m_createSqlColumn = new ArrayList();
		m_createSqlValue = new ArrayList();
	}	//	createUpdateSqlReset

	/**
	 *	Get Mandatory empty columns
	 *  @param rowData row data
	 *  @return String with missing column headers/labels
	 */
	private String getMandatory(Object[] rowData)
	{
		//  see also => ProcessParameter.saveParameter
		StringBuffer sb = new StringBuffer();

		//	Check all columns
		int size = m_fields.size();
		for (int i = 0; i < size; i++)
		{
			MField field = (MField)m_fields.get(i);
			if (field.isMandatory(true))        //  check context
			{
				if (rowData[i] == null || rowData[i].toString().length() == 0)
				{
					field.setInserting (true);  //  set editable otherwise deadlock
					field.setError(true);
					if (sb.length() > 0)
						sb.append(", ");
					sb.append(field.getHeader());
				}
				else
					field.setError(false);
			}
		}

		if (sb.length() == 0)
			return "";
		return sb.toString();
	}	//	getMandatory

	/*************************************************************************/

	/**	LOB Info				*/
	private ArrayList		m_lobInfo = null;

	/**
	 * 	Reset LOB info
	 */
	private void lobReset()
	{
		m_lobInfo = null;
	}	//	resetLOB
	
	/**
	 * 	Prepare LOB save
	 *	@param value value 
	 *	@param index index
	 *	@param displayType display type
	 */	
	private void lobAdd (PO_LOB lob)
	{
		log.debug("lobAdd - " + lob);
		if (m_lobInfo == null)
			m_lobInfo = new ArrayList();
		m_lobInfo.add(lob);
	}	//	lobAdd
	
	/**
	 * 	Save LOB
	 */
	private void lobSave (String whereClause)
	{
		if (m_lobInfo == null)
			return;
		for (int i = 0; i < m_lobInfo.size(); i++)
		{
			PO_LOB lob = (PO_LOB)m_lobInfo.get(i);
			lob.save(whereClause);
		}	//	for all LOBs
		lobReset();
	}	//	lobSave

	
	/**************************************************************************
	 *	New Record after current Row
	 *  @param currentRow row
	 *  @param copyCurrent copy
	 *  @return true if success -
	 *  Error info (Access*, AccessCannotInsert) is saved in the log
	 */
	public boolean dataNew (int currentRow, boolean copyCurrent)
	{
		log.info("dataNew - Current=" + currentRow + ", Copy=" + copyCurrent);
		//  Read only
		if (m_readOnly)
		{
			fireDataStatusEEvent("AccessCannotInsert", "");
			return false;
		}

		/** @todo No TableLevel */
		//  || !Access.canViewInsert(m_ctx, m_WindowNo, tableLevel, true, true))
		//  fireDataStatusEvent(Log.retrieveError());

		//  see if we need to save
		dataSave(-2, false);


		m_inserting = true;
		//	Create default data
		int size = m_fields.size();
		m_rowData = new Object[size];	//	"original" data
		Object[] rowData = new Object[size];
		//	fill data
		if (copyCurrent)
		{
			MSort sort = (MSort) m_sort.get(currentRow);
			Object[] origData = (Object[])m_buffer.get(sort.index);
			for (int i = 0; i < size; i++)
			{
				MField field = (MField)m_fields.get(i);
				String columnName = field.getColumnName();
				if (columnName.startsWith("Created") || columnName.startsWith("Updated")
					|| columnName.equals("EntityType") || columnName.startsWith("DocumentNo"))
				{
					rowData[i] = field.getDefault();
					field.setValue(rowData[i], m_inserting);
				}
				else
					rowData[i] = origData[i];
			}
		}
		else	//	new
		{
			for (int i = 0; i < size; i++)
			{
				MField field = (MField)m_fields.get(i);
				rowData[i] = field.getDefault();
				field.setValue(rowData[i], m_inserting);
			}
		}
		m_changed = true;
		m_compareDB = true;
		m_rowChanged = -1;  //  only changed in setValueAt
		m_newRow = currentRow + 1;
		//  if there is no record, the current row could be 0 (and not -1)
		if (m_buffer.size() < m_newRow)
			m_newRow = m_buffer.size();

		//	add Data at end of buffer
		MSort sort = new MSort(m_buffer.size(), null);	//	index
		m_buffer.add(rowData);
		//	add Sort pointer
		m_sort.add(m_newRow, sort);
		m_rowCount++;

		//	inform
		log.debug("dataNew - Current=" + currentRow + ", New=" + m_newRow);
		fireTableRowsInserted(m_newRow, m_newRow);
		fireDataStatusIEvent(copyCurrent ? "UpdateCopied" : "Inserted");
		log.debug("dataNew - Current=" + currentRow + ", New=" + m_newRow + " - complete");
		return true;
	}	//	dataNew


	/**************************************************************************
	 *	Delete Data
	 *  @param row row
	 *  @return true if success -
	 *  Error info (Access*, AccessNotDeleteable, DeleteErrorDependent,
	 *  DeleteError) is saved in the log
	 */
	public boolean dataDelete (int row)
	{
		log.info("dataDelete - " + row);
		if (row < 0)
			return false;
		Object rowID = getRowID(row);
		if (rowID == null)
			return false;

		//	Tab R/O
		if (m_readOnly)
		{
			fireDataStatusEEvent("AccessCannotDelete", "");		//	previleges
			return false;
		}

		//	Is this record deletable?
		if (!m_deleteable)
		{
			fireDataStatusEEvent("AccessNotDeleteable", "");	//	audit
			return false;
		}

		//	Processed Column and not an Import Table
		if (m_indexProcessedColumn > 0 && !m_tableName.startsWith("I_"))
		{
			Boolean processed = (Boolean)getValueAt(row, m_indexProcessedColumn);
			if (processed != null && processed.booleanValue())
			{
				fireDataStatusEEvent("CannotDeleteTrx", "");
				return false;
			}
		}
		

		/** @todo check Access */
		//  fireDataStatusEvent(Log.retrieveError());

		MSort sort = (MSort)m_sort.get(row);
		Object[] rowData = (Object[])m_buffer.get(sort.index);
		//
		M_Table table = M_Table.get (m_ctx, m_AD_Table_ID);
		PO po = null;
		int Record_ID = getKeyID(m_rowChanged);
		if (Record_ID != -1)
			po = table.getPO(Record_ID);
		else	//	Multi - Key
			po = table.getPO(getWhereClause(rowData));
		
		//	Delete via PO 
		if (po != null)
		{
			if (!po.delete(false))
			{
				ValueNamePair vp = Log.retrieveError();
				if (vp != null)
					fireDataStatusEEvent(vp);
				else
					fireDataStatusEEvent("DeleteError", "");
				return false;
			}
		}
		else	//	Delete via SQL
		{
			StringBuffer SQL = new StringBuffer("DELETE ");
			SQL.append(m_tableName).append(" WHERE ROWID=?");
			int no = 0;
			try
			{
				PreparedStatement pstmt = DB.prepareStatement(SQL.toString());
				DB.getDatabase().setRowID(pstmt, 1, rowID);
				no = pstmt.executeUpdate();
				pstmt.close();
			}
			catch (SQLException e)
			{
				log.error ("dataDelete", e);
				String msg = "DeleteError";
				if (e.getErrorCode() == 2292)	//	Child Record Found
					msg = "DeleteErrorDependent";
				fireDataStatusEEvent(msg, e.getLocalizedMessage());
				return false;
			}
			//	Check Result
			if (no != 1)
			{
				log.error("dataDelete - Number of deleted rows = " + no);
				return false;
			}
		}

		//	Get Sort
		int bufferRow = sort.index;
		//	Delete row in Buffer and shifts all below up
		m_buffer.remove(bufferRow);
		m_rowCount--;

		//	Delete row in Sort
		m_sort.remove(row);
		//	Correct pointer in Sort
		for (int i = 0; i < m_sort.size(); i++)
		{
			MSort ptr = (MSort)m_sort.get(i);
			if (ptr.index > bufferRow)
				ptr.index--;	//	move up
		}

		//	inform
		m_changed = false;
		m_rowChanged = -1;
		fireTableRowsDeleted(row, row);
		fireDataStatusIEvent("Deleted");
		log.debug("dataDelete - " + row + " complete");
		return true;
	}	//	dataDelete

	
	/**************************************************************************
	 *	Ignore changes
	 */
	public void dataIgnore()
	{
		log.info("dataIgnore - Inserting=" + m_inserting);
		if (!m_inserting && !m_changed && m_rowChanged < 0)
		{
			log.debug("dataIgnore - Nothing to ignore");
			return;
		}

		//	Inserting - delete new row
		if (m_inserting)
		{
			//	Get Sort
			MSort sort = (MSort)m_sort.get(m_newRow);
			int bufferRow = sort.index;
			//	Delete row in Buffer and shifts all below up
			m_buffer.remove(bufferRow);
			m_rowCount--;
			//	Delete row in Sort
			m_sort.remove(m_newRow);	//	pintint to the last column, so no adjustment
			//
			m_changed = false;
			m_rowData = null;
			m_rowChanged = -1;
			m_inserting = false;
			//	inform
			fireTableRowsDeleted(m_newRow, m_newRow);
		}
		else
		{
			//	update buffer
			if (m_rowData != null)
			{
				MSort sort = (MSort)m_sort.get(m_rowChanged);
				m_buffer.set(sort.index, m_rowData);
			}
			m_changed = false;
			m_rowData = null;
			m_rowChanged = -1;
			m_inserting = false;
			//	inform
		//	fireTableRowsUpdated(m_rowChanged, m_rowChanged); >> messes up display?? (clearSelection)
		}
		m_newRow = -1;
		fireDataStatusIEvent("Ignored");
	}	//	dataIgnore


	/**
	 *	Refresh Row - ignore changes
	 *  @param row row
	 */
	public void dataRefresh (int row)
	{
		log.info("dataRefresh " + row);

		if (row < 0)
			return;
		Object rowID = getRowID(row);
		if (rowID == null)
			return;

		//  ignore
		dataIgnore();

		//	Create SQL
		String SQL = m_SQL_Select + " WHERE ROWID=?";
		MSort sort = (MSort)m_sort.get(row);
		Object[] rowDataDB = null;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(SQL);
			DB.getDatabase().setRowID(pstmt, 1, rowID);
			ResultSet rs = pstmt.executeQuery();
			//	only one row
			if (rs.next())
				rowDataDB = readData(rs);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			log.error ("dataRefresh\nSQL=" + SQL, e);
			fireTableRowsUpdated(row, row);
			fireDataStatusEEvent("RefreshError", "");
			return;
		}

		//	update buffer
		m_buffer.set(sort.index, rowDataDB);
		//	info
		m_rowData = null;
		m_changed = false;
		m_rowChanged = -1;
		m_inserting = false;
		fireTableRowsUpdated(row, row);
		fireDataStatusIEvent("Refreshed");
	}	//	dataRefresh


	/**
	 *	Refresh all Rows - ignore changes
	 */
	public void dataRefreshAll()
	{
		log.info("dataRefreshAll");
		dataIgnore();
		close(false);
		open();
		//	Info
		m_rowData = null;
		m_changed = false;
		m_rowChanged = -1;
		m_inserting = false;
		fireTableDataChanged();
		fireDataStatusIEvent("Refreshed");
	}	//	dataRefreshAll


	/**
	 *	Requery with new whereClause
	 *  @param whereClause sql where clause
	 *  @param onlyCurrentRows only current rows
	 *  @param onlyCurrentDays how many days back
	 *  @return true if success
	 */
	public boolean dataRequery (String whereClause, boolean onlyCurrentRows, int onlyCurrentDays)
	{
		log.info("dataRequery - " + whereClause + "; OnlyCurrent=" + onlyCurrentRows);
		close(false);
		m_onlyCurrentDays = onlyCurrentDays;
		setWhereClause(whereClause, onlyCurrentRows, m_onlyCurrentDays);
		open();
		//  Info
		m_rowData = null;
		m_changed = false;
		m_rowChanged = -1;
		m_inserting = false;
		fireTableDataChanged();
		fireDataStatusIEvent("Refreshed");
		return true;
	}	//	dataRequery


	/**************************************************************************
	 *	Is Cell Editable.
	 *	Is queried from JTable before checking VCellEditor.isCellEditable
	 *  @param  row the row index being queried
	 *  @param  col the column index being queried
	 *  @return true, if editable
	 */
	public boolean isCellEditable (int row, int col)
	{
	//	Log.trace(Log.l6_Database, "MTable.isCellEditable - Row=" + row + ", Col=" + col);
		//	Make Rows selectable
		if (col == 0)
			return true;

		//	Entire Table not editable
		if (m_readOnly)
			return false;
		//	Key & ID not editable
		if (col == m_indexRowIDColumn || col == m_indexKeyColumn)
			return false;
		/** @todo check link columns */

		//	Check column range
		if (col < 0 && col >= m_fields.size())
			return false;
		//  IsActive Column always editable if no processed exists
		if (col == m_indexActiveColumn && m_indexProcessedColumn == -1)
			return true;
		//	Row
		if (!isRowEditable(row))
			return false;

		//	Column
		return ((MField)m_fields.get(col)).isEditable(false);
	}	//	IsCellEditable


	/**
	 *	Is Current Row Editable
	 *  @param row row
	 *  @return true if editable
	 */
	public boolean isRowEditable (int row)
	{
	//	Log.trace(Log.l6_Database, "MTable.isRowEditable - Row=" + row);
		//	Entire Table not editable or no row
		if (m_readOnly || row < 0)
			return false;
		//	If not Active - not editable
		if (m_indexActiveColumn > 0)		//	&& m_TabNo != Find.s_TabNo)
		{
			Object value = getValueAt(row, m_indexActiveColumn);
			if (value instanceof Boolean)
			{
				if (!((Boolean)value).booleanValue())
					return false;
			}
			else if ("N".equals(value)) 
				return false;
		}
		//	If Processed - not editable (Find always editable)
		if (m_indexProcessedColumn > 0)		//	&& m_TabNo != Find.s_TabNo)
		{
			Object processed = getValueAt(row, m_indexProcessedColumn);
			if (processed instanceof Boolean)
			{
				if (((Boolean)processed).booleanValue())
					return false;
			}
			else if ("Y".equals(processed)) 
				return false;
		}
		//
		int[] co = getClientOrg(row);
		int AD_Client_ID = co[0]; 
		int AD_Org_ID = co[1];
		return MRole.getDefault(m_ctx, false).canUpdate(AD_Client_ID, AD_Org_ID, m_AD_Table_ID, false);
	}	//	isRowEditable

	/**
	 * 	Get Client Org for row
	 *	@param row row
	 *	@return array [0] = Client [1] = Org - a value of -1 is not defined/found
	 */
	private int[] getClientOrg (int row)
	{
		int AD_Client_ID = -1;
		if (m_indexClientColumn != -1)
		{
			Integer ii = (Integer)getValueAt(row, m_indexClientColumn);
			if (ii != null)
				AD_Client_ID = ii.intValue();
		}
		int AD_Org_ID = 0;
		if (m_indexOrgColumn != -1)
		{
			Integer ii = (Integer)getValueAt(row, m_indexOrgColumn);
			if (ii != null)
				AD_Org_ID = ii.intValue();
		}
		return new int[] {AD_Client_ID, AD_Org_ID};
	}	//	getClientOrg

	/**
	 *	Set entire table as read only
	 *  @param value new read only value
	 */
	public void setReadOnly (boolean value)
	{
		log.debug("setReadOnly " + value);
		m_readOnly = value;
	}	//	setReadOnly

	/**
	 *  Is entire Table Read/Only
	 *  @return true if read only
	 */
	public boolean isReadOnly()
	{
		return m_readOnly;
	}   //  isReadOnly

	/**
	 *  Is inserting
	 *  @return true if inserting
	 */
	public boolean isInserting()
	{
		return m_inserting;
	}   //  isInserting

	/**
	 *	Set Compare DB.
	 * 	If Set to false, save overwrites the record, regardless of DB changes.
	 *  (When a payment is changed in Sales Order, the payment reversal clears the payment id)
	 * 	@param compareDB compare DB - false forces overwrite
	 */
	public void setCompareDB (boolean compareDB)
	{
		m_compareDB = compareDB;
	}  	//	setCompareDB

	/**
	 *	Get Compare DB.
	 * 	@return false if save overwrites the record, regardless of DB changes
	 * 	(false forces overwrite).
	 */
	public boolean getCompareDB ()
	{
		return m_compareDB;
	}  	//	getCompareDB


	/**
	 *	Can Table rows be deleted
	 *  @param value new deleteable value
	 */
	public void setDeleteable (boolean value)
	{
		log.debug("setDeleteable " + value);
		m_deleteable = value;
	}	//	setDeleteable

	
	/**************************************************************************
	 *	Read Data from Recordset
	 *  @param rs result set
	 *  @return Data Array
	 */
	private Object[] readData (ResultSet rs)
	{
		int size = m_fields.size();
		Object[] rowData = new Object[size];
		String columnName = null;
		int displayType = 0;

		//	Types see also MField.createDefault
		try
		{
			//	get row data
			for (int j = 0; j < size; j++)
			{
				//	Column Info
				MField field = (MField)m_fields.get(j);
				columnName = field.getColumnName();
				displayType = field.getDisplayType();
				//	Integer, ID, Lookup (UpdatedBy is a numeric column)
				if (displayType == DisplayType.Integer
					|| (DisplayType.isID(displayType) && (columnName.endsWith("_ID") || columnName.endsWith("_Acct"))) 
					|| columnName.endsWith("atedBy"))
				{
					rowData[j] = new Integer(rs.getInt(j+1));	//	Integer
					if (rs.wasNull())
						rowData[j] = null;
				}
				//	Number
				else if (DisplayType.isNumeric(displayType))
					rowData[j] = rs.getBigDecimal(j+1);			//	BigDecimal
				//	Date
				else if (DisplayType.isDate(displayType))
					rowData[j] = rs.getTimestamp(j+1);			//	Timestamp
				//	RowID or Key (and Selection)
				else if (displayType == DisplayType.RowID)
				{
					Object[] rid = new Object[3];
					if (columnName.equals("ROWID"))
						rid[0] = DB.getDatabase().getRowID(rs, j+1);
					else
						rid[2] = new Integer (rs.getInt(j+1));
					rid[1] = new Boolean(false);
					rowData[j] = rid;
				}
				//	YesNo
				else if (displayType == DisplayType.YesNo)
					rowData[j] = new Boolean ("Y".equals(rs.getString(j+1)));	//	Boolean			
				//	LOB
				else if (displayType == DisplayType.TextLong)
				{
					Object value = rs.getObject(j+1);
					if (rs.wasNull())
						rowData[j] = null;
					else if (value instanceof Clob) 
					{
						Clob lob = (Clob)value;
						long length = lob.length();
						rowData[j] = lob.getSubString(1, (int)length);
					}
				}
				//	String
				else
					rowData[j] = rs.getString(j+1);				//	String
			}
		}
		catch (SQLException e)
		{
			log.error("readData - " + columnName + ", DT=" + displayType, e);
		}
		return rowData;
	}	//	readData

	
	/**************************************************************************
	 *	Remove Data Status Listener
	 *  @param l listener
	 */
	public synchronized void removeDataStatusListener(DataStatusListener l)
	{
		if (m_dataStatusListeners != null && m_dataStatusListeners.contains(l))
		{
			Vector v = (Vector) m_dataStatusListeners.clone();
			v.removeElement(l);
			m_dataStatusListeners = v;
		}
	}	//	removeDataStatusListener

	/**
	 *	Add Data Status Listener
	 *  @param l listener
	 */
	public synchronized void addDataStatusListener(DataStatusListener l)
	{
		Vector v = m_dataStatusListeners == null ? new Vector(2) : (Vector) m_dataStatusListeners.clone();
		if (!v.contains(l))
		{
			v.addElement(l);
			m_dataStatusListeners = v;
		}
	}	//	addDataStatusListener

	/**
	 *	Inform Listeners
	 *  @param e event
	 */
	private void fireDataStatusChanged (DataStatusEvent e)
	{
		if (m_dataStatusListeners != null)
		{
			Vector listeners = m_dataStatusListeners;
			int count = listeners.size();
			for (int i = 0; i < count; i++)
				((DataStatusListener) listeners.elementAt(i)).dataStatusChanged(e);
		}
	}	//	fireDataStatusChanged

	/**
	 *  Create Data Status Event
	 *  @return data status event
	 */
	private DataStatusEvent createDSE()
	{
		boolean changed = m_changed;
		if (m_rowChanged != -1)
			changed = true;
		DataStatusEvent dse = new DataStatusEvent(this, m_rowCount, changed,
			Env.isAutoCommit(m_ctx, m_WindowNo), m_inserting);
		return dse;
	}   //  createDSE

	/**
	 *  Create and fire Data Status Info Event
	 *  @param AD_Message message
	 */
	protected void fireDataStatusIEvent (String AD_Message)
	{
		DataStatusEvent e = createDSE();
		e.setInfo(AD_Message, "", false);
		fireDataStatusChanged (e);
	}   //  fireDataStatusEvent

	/**
	 *  Create and fire Data Status Error Event
	 *  @param AD_Message message
	 *  @param info info
	 */
	protected void fireDataStatusEEvent (String AD_Message, String info)
	{
	//	org.compiere.util.Trace.printStack();
		//
		DataStatusEvent e = createDSE();
		e.setInfo(AD_Message, info, true);
		Log.saveError(AD_Message, info);
		fireDataStatusChanged (e);
	}   //  fireDataStatusEvent

	/**
	 *  Create and fire Data Status Event (from Error Log)
	 *  @param errorLog error log info
	 */
	protected void fireDataStatusEEvent (ValueNamePair errorLog)
	{
		if (errorLog != null)
			fireDataStatusEEvent (errorLog.getValue(), errorLog.getName());
	}   //  fireDataStatusEvent

	
	/**************************************************************************
	 *  Remove Vetoable change listener for row changes
	 *  @param l listener
	 */
	public synchronized void removeVetoableChangeListener(VetoableChangeListener l)
	{
		m_vetoableChangeSupport.removeVetoableChangeListener(l);
	}   //  removeVetoableChangeListener

	/**
	 *  Add Vetoable change listener for row changes
	 *  @param l listener
	 */
	public synchronized void addVetoableChangeListener(VetoableChangeListener l)
	{
		m_vetoableChangeSupport.addVetoableChangeListener(l);
	}   //  addVetoableChangeListener

	/**
	 *  Fire Vetoable change listener for row changes
	 *  @param e event
	 *  @throws PropertyVetoException
	 */
	protected void fireVetoableChange(PropertyChangeEvent e) throws java.beans.PropertyVetoException
	{
		m_vetoableChangeSupport.fireVetoableChange(e);
	}   //  fireVetoableChange

	/**
	 *  toString
	 *  @return String representation
	 */
	public String toString()
	{
		return new StringBuffer("MTable[").append(m_tableName)
			.append(",WindowNo=").append(m_WindowNo)
			.append(",Tab=").append(m_TabNo).append("]").toString();
	}   //  toString


	
	/**************************************************************************
	 *	ASync Loader
	 */
	class Loader extends Thread implements Serializable
	{
		/**
		 *  Construct Loader
		 */
		public Loader()
		{
			super("TLoader");
		}	//	Loader

		private PreparedStatement   m_pstmt = null;
		private ResultSet 		    m_rs = null;

		/**
		 *	Open ResultSet
		 *	@return number of records
		 */
		protected int open()
		{
		//	Log.trace(Log.l4_Data, "MTable Loader.open");
			//	Get Number of Rows
			int rows = 0;
			try
			{
				PreparedStatement pstmt = DB.prepareStatement(m_SQL_Count);
				setParameter (pstmt, true);
				ResultSet rs = pstmt.executeQuery();
				if (rs.next())
					rows = rs.getInt(1);
				rs.close();
				pstmt.close();
			}
			catch (SQLException e0)
			{
				//	Zoom Query may have invalid where clause
				if (e0.getErrorCode() == 904) 	//	ORA-00904: "C_x_ID": invalid identifier
					log.warn("Loader.open Count - " + e0.getLocalizedMessage() + "\nSQL=" + m_SQL_Count);
				else
					log.error ("Loader.open Count SQL=" + m_SQL_Count, e0);
				return 0;
			}

			//	open Statement (closed by Loader.close)
			try
			{
				m_pstmt = DB.prepareStatement(m_SQL);
			//	m_pstmt.setFetchSize(20);
				setParameter (m_pstmt, false);
				m_rs = m_pstmt.executeQuery();
			}
			catch (SQLException e)
			{
				log.error ("Loader.open\nFull SQL=" + m_SQL, e);
				return 0;
			}
			StringBuffer info = new StringBuffer("Rows=");
			info.append(rows);
			if (rows == 0)
				info.append(" - ").append(m_SQL_Count);
			log.debug("Loader.open - " + info.toString());
			return rows;
		}	//	open

		/**
		 *	Close RS and Statement
		 */
		private void close()
		{
		//	Log.trace(Log.l4_Data, "MTable Loader.close");
			try
			{
				if (m_rs != null)
					m_rs.close();
				if (m_pstmt != null)
					m_pstmt.close();
			}
			catch (SQLException e)
			{
				log.error ("Loader.closeRS", e);
			}
			m_rs = null;
			m_pstmt = null;
		}	//	close

		/**
		 *	Fill Buffer to include Row
		 */
		public void run()
		{
			log.info("Loader.run");
			if (m_rs == null)
				return;

			try
			{
				while(m_rs.next())
				{
					if (this.isInterrupted())
					{
						log.debug("Loader interrupted");
						close();
						return;
					}
					//  Get Data
					Object[] rowData = readData(m_rs);
					//	add Data
					MSort sort = new MSort(m_buffer.size(), null);	//	index
					m_buffer.add(rowData);
					m_sort.add(sort);

					//	Statement all 250 rows & sleep
					if (m_buffer.size() % 250 == 0)
					{
						//	give the other processes a chance
						try
						{
							yield();
							sleep(10);		//	.01 second
						}
						catch (InterruptedException ie)
						{
							log.debug("Loader interrupted while sleeping");
							close();
							return;
						}
						DataStatusEvent evt = createDSE();
						evt.setLoading(m_buffer.size());
						fireDataStatusChanged(evt);
					}
				}	//	while(rs.next())
			}
			catch (SQLException e)
			{
				log.error ("Loader.run", e);
			}
			close();
			fireDataStatusIEvent("");
		}	//	run

		/**
		 *	Set Parameter for Query.
		 *		elements must be Integer, BigDecimal, String (default)
		 *  @param pstmt prepared statement
		 *  @param countSQL count
		 */
		private void setParameter (PreparedStatement pstmt, boolean countSQL)
		{
			if (m_parameterSELECT.size() == 0 && m_parameterWHERE.size() == 0)
				return;
			try
			{
				int pos = 1;	//	position in Statement
				//	Select Clause Parameters
				for (int i = 0; !countSQL && i < m_parameterSELECT.size(); i++)
				{
					Object para = m_parameterSELECT.get(i);
					if (para != null)
						log.debug("setParameter Select " + i + "=" + para);
					//
					if (para == null)
						;
					else if (para instanceof Integer)
					{
						Integer ii = (Integer)para;
						pstmt.setInt (pos++, ii.intValue());
					}
					else if (para instanceof BigDecimal)
						pstmt.setBigDecimal (pos++, (BigDecimal)para);
					else
						pstmt.setString(pos++, para.toString());
				}
				//	Where Clause Parameters
				for (int i = 0; i < m_parameterWHERE.size(); i++)
				{
					Object para = m_parameterWHERE.get(i);
					if (para != null)
						log.debug("setParameter Where " + i + "=" + para);
					//
					if (para == null)
						;
					else if (para instanceof Integer)
					{
						Integer ii = (Integer)para;
						pstmt.setInt (pos++, ii.intValue());
					}
					else if (para instanceof BigDecimal)
						pstmt.setBigDecimal (pos++, (BigDecimal)para);
					else
						pstmt.setString(pos++, para.toString());
				}
			}
			catch (SQLException e)
			{
				log.error("Loader.setParameter", e);
			}
		}	//	setParameter

	}	//	Loader

}	//	MTable
