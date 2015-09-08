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
package org.compiere.grid.ed;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.text.*;
import java.util.*;
import java.math.*;
import java.beans.*;
import java.sql.*;

import org.compiere.apps.*;
import org.compiere.util.*;
import org.compiere.model.*;
import org.compiere.apps.search.*;
import org.compiere.plaf.*;
import org.compiere.swing.*;

/**
 *  Lookup Field.
 *  <p>
 *	    When r/o - display a Label
 *		When STABLE - display a ComboBox
 *		Otherwise show Selection Dialog
 *  <p>
 *  Sepecial handling of BPartner and Product
 *
 *  @author 	Jorg Janke
 *  @version 	$Id: VLookup.java,v 1.44 2003/08/12 17:58:30 jjanke Exp $
 */
public class VLookup extends JComponent
	implements VEditor, ActionListener, FocusListener
{
	/**
	 *  Create BPartner Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createBPartner (int WindowNo)
	{
		int AD_Column_ID = 3499;    //  C_Invoice.C_BPartner_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, AD_Column_ID, DisplayType.Search);
			return new VLookup ("C_BPartner_ID", false, false, true, lookup, DisplayType.Search, WindowNo);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createBPartner", e);
		}
		return null;
	}   //  createBPartner

	/**
	 *  Create Product Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createProduct (int WindowNo)
	{
		int AD_Column_ID = 3840;    //  C_InvoiceLine.M_Product_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, AD_Column_ID, DisplayType.Search);
			return new VLookup ("M_Product_ID", false, false, true, lookup, DisplayType.Search, WindowNo);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createProduct", e);
		}
		return null;
	}   //  createProduct

	/*************************************************************************/

	/**
	 *	IDE Default Constructor
	 */
	public VLookup()
	{
		this("Lookup", false, false, true, null, 0, 0);
	}	//	VLookup


	/**
	 *	Detail Constructor
	 *
	 *  @param columnName column
	 *  @param mandatory mandatory
	 *  @param isReadOnly read only
	 *  @param isUpdateable updateable
	 *  @param lookup lookup
	 *  @param displayType display type
	 *  @param WindowNo window no
	 */
	public VLookup (String columnName, boolean mandatory, boolean isReadOnly, boolean isUpdateable,
		Lookup lookup, int displayType, int WindowNo)
	{
		super();
		m_columnName = columnName;
		m_lookup = lookup;
		if (lookup == null)
			Log.trace(Log.l3_Util, "VLookup", "Lookup is NULL = " + columnName);
		setMandatory(mandatory);
		m_displayType = displayType;
		m_WindowNo = WindowNo;		//	for Info
		//
		setLayout(new BorderLayout());
		VLookup_mouseAdapter mouse = new VLookup_mouseAdapter(this);    //  popup

		//	***	Text & Button	***
		m_text.addActionListener(this);
		m_text.addMouseListener(mouse);
		//  Button
		m_button.addActionListener(this);
		m_button.addMouseListener(mouse);
		m_button.setFocusable(false);   //  don't focus when tabbing
		if (columnName.equals("C_BPartner_ID"))
			m_button.setIcon(Env.getImageIcon("BPartner10.gif"));
		else if (columnName.equals("M_Product_ID"))
			m_button.setIcon(Env.getImageIcon("Product10.gif"));
		else
			m_button.setIcon(Env.getImageIcon("PickOpen10.gif"));

		//	*** VComboBox	***
		if (m_lookup != null && displayType != DisplayType.Search)	//	No Search
		{
			//  Memory Leak after executing the next two lines ??
			m_lookup.fillComboBox (isMandatory(), false, false, false);
			m_combo.setModel(m_lookup);
			//
			m_combo.addActionListener(this);							//	Selection
			m_combo.addMouseListener(mouse);	                        //	popup
			//	FocusListener to refresh selection before opening
			if (!m_lookup.isValidated() || m_lookup.hasInactive())
				m_combo.addFocusListener(this);
		}

		setUI (true);
		//	ReadWrite	-	decides what components to show
		if (isReadOnly || !isUpdateable || m_lookup == null)
			setReadWrite(false);
		else
			setReadWrite(true);

		//	Popup
		if (m_lookup != null)
		{
			if ((displayType == DisplayType.List && Env.getContextAsInt(Env.getCtx(), "#AD_Role_ID") == 0)
				|| displayType != DisplayType.List)     //  only system admins can change lists, so no need to zoom for others
			{
				mZoom = new JMenuItem(Msg.getMsg(Env.getCtx(), "Zoom"), Env.getImageIcon("Zoom16.gif"));
				mZoom.addActionListener(this);
				popupMenu.add(mZoom);
			}
			mRefresh = new JMenuItem(Msg.getMsg(Env.getCtx(), "Refresh"), Env.getImageIcon("Refresh16.gif"));
			mRefresh.addActionListener(this);
			popupMenu.add(mRefresh);
		}
		//	VBPartner quick entry link
		if (columnName.equals("C_BPartner_ID"))
		{
			mBPartnerNew = new JMenuItem (Msg.getMsg(Env.getCtx(), "New"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerNew.addActionListener(this);
			popupMenu.add(mBPartnerNew);
			mBPartnerUpd = new JMenuItem (Msg.getMsg(Env.getCtx(), "Update"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerUpd.addActionListener(this);
			popupMenu.add(mBPartnerUpd);
		}
		//
		if (m_lookup != null && m_lookup.getZoom() == 0)
			mZoom.setEnabled(false);
	}	//	VLookup

	/**
	 *  Dispose
	 */
	public void dispose()
	{
		m_text = null;
		m_button = null;
		m_lookup = null;
		m_mField = null;
		m_combo.removeActionListener(this);
		m_combo.setModel(new DefaultComboBoxModel());    //  remove reference
	//	m_combo.removeAllItems();
		m_combo = null;
	}   //  dispose

	/** Display Length for Lookups (18)         */
	public final static int     DISPLAY_LENGTH = 18;

	/** Search: The Editable Text Field         */
	private CTextField 			m_text = new CTextField (DISPLAY_LENGTH);
	/** Search: The Button to open Editor   */
	private CButton				m_button = new CButton();
	/** The Combo Box if not a Search Lookup    */
	private VComboBox			m_combo = new VComboBox();
	/** Indicator that value is being set       */
	private volatile boolean 	m_settingValue = false;
	private volatile boolean 	m_settingFocus = false;
	/** Indicator that Lookup has focus         */
	private volatile boolean	m_haveFocus = false;
	/** Indicator - inserting new value			*/
	private volatile boolean	m_inserting = false;

	//
	private String				m_columnName;
	private Lookup				m_lookup;
	private int					m_displayType;
	private int					m_WindowNo;

	private boolean				m_comboActive = true;
	private Object				m_value;

	//	Popup
	JPopupMenu 					popupMenu = new JPopupMenu();
	private JMenuItem 			mZoom;
	private JMenuItem 			mRefresh;
	private JMenuItem			mBPartnerNew;
	private JMenuItem			mBPartnerUpd;

	private MField              m_mField = null;

	/**
	 *  Set Content and Size of Compoments
	 *  @param initial if true, size and margins will be set
	 */
	private void setUI (boolean initial)
	{
		if (initial)
		{
			Dimension size = m_text.getPreferredSize();
			setPreferredSize(new Dimension(size));  //	causes r/o to be the same length
			m_combo.setPreferredSize(new Dimension(size));
			//
			m_text.setBorder(null);
			Dimension bSize = new Dimension(size.height, size.height);
			m_button.setPreferredSize (bSize);
			m_button.setMargin(new Insets(0, 0, 0, 0));
		}

		//	What to show
		this.remove(m_combo);
		this.remove(m_button);
		this.remove(m_text);
		//
		if (!isReadWrite())									//	r/o - show text only
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			m_text.setReadWrite(false);
			m_combo.setReadWrite(false);
			m_comboActive = false;
		}
		else if (m_displayType != DisplayType.Search)	    //	show combo if not Search
		{
			this.setBorder(null);
			this.add(m_combo, BorderLayout.CENTER);
			m_comboActive = true;
		}
		else 												//	Search or unstable - show text & button
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			this.add(m_button, BorderLayout.EAST);
			m_text.setReadWrite (true);
			m_comboActive = false;
		}
	}   //  setUI

	/**
	 *	Set ReadWrite
	 *  @param value ReadWrite
	 */
	public void setReadWrite (boolean value)
	{
		boolean rw = value;
		if (m_lookup == null)
			rw = false;
		if (m_combo.isReadWrite() != value)
		{
			m_combo.setReadWrite(rw);
			setUI (false);
			if (m_comboActive)
				setValue (m_value);
		}
	}	//	setReadWrite

	/**
	 *	IsEditable
	 *  @return is lookup ReadWrite
	 */
	public boolean isReadWrite()
	{
		return m_combo.isReadWrite();
	}	//	isReadWrite

	/**
	 *	Set Mandatory (and back bolor)
	 *  @param mandatory mandatory
	 */
	public void setMandatory (boolean mandatory)
	{
		m_combo.setMandatory(mandatory);
		m_text.setMandatory(mandatory);
	}	//	setMandatory

	/**
	 *	Is it mandatory
	 *  @return true if mandatory
	 */
	public boolean isMandatory()
	{
		return m_combo.isMandatory();
	}	//	isMandatory

	/**
	 *	Set Background
	 *  @param color color
	 */
	public void setBackground(Color color)
	{
		m_text.setBackground(color);
		m_combo.setBackground(color);
	}	//	setBackground

	/**
	 *	Set Background
	 *  @param error error
	 */
	public void setBackground (boolean error)
	{
		m_text.setBackground(error);
		m_combo.setBackground(error);
	}	//	setBackground

	/**
	 *  Set Foreground
	 *  @param fg Foreground color
	 */
	public void setForeground(Color fg)
	{
		m_text.setForeground(fg);
		m_combo.setForeground(fg);
	}   //  setForeground

	/**
	 *  Set Editor to value
	 *  @param value new Value
	 */
	public void setValue (Object value)
	{
		Log.trace(Log.l6_Database, "VLookup.setValue", m_columnName + "=" + value);
		m_settingValue = true;		//	disable actions
		m_value = value;

		//	Set both for switching
		m_combo.setValue (value);
		if (value == null)
		{
			m_text.setText (null);
			m_settingValue = false;
			return;
		}
		if (m_lookup == null)
		{
			m_text.setText (value.toString());
			m_settingValue = false;
			return;
		}

		//	Set Display
		String display = m_lookup.getDisplay(value);
		boolean notFound = display.startsWith("<") && display.startsWith(">");
		m_text.setText (display);
		m_text.setCaretPosition (0); //	show beginning

		//	Nothing showing in Combo and should be showing
		if (m_combo.getSelectedItem() == null
			&& (m_comboActive || (m_inserting && m_displayType != DisplayType.Search)))
		{
			//  lookup found nothing too
			if (notFound)
			{
				Log.trace(8, "VLookup.setValue - Not found (1)", display);
				//  we may have a new value
				m_lookup.refresh();
				m_combo.setValue (value);
				display = m_lookup.getDisplay(value);
				m_text.setText (display);
				m_text.setCaretPosition (0);	//	show beginning
				notFound = display.startsWith("<") && display.endsWith(">");
			}
			if (notFound)	//	<key>
			{
				m_value = null;
				actionCombo (null);             //  data binding
				Log.trace(Log.l6_Database, "VLookup.setValue - not found - " + value);
			}
			//  we have lookup
			else if (m_combo.getSelectedItem() == null)
			{
				NamePair pp = m_lookup.get(value);
				if (pp != null)
				{
					Log.trace (Log.l6_Database, "VLookup.setValue - added to combo - " + pp);
					//  Add to Combo
					m_combo.addItem (pp);
					m_combo.setValue (value);
				}
			}
			//  Not in Lookup - set to Null
			if (m_combo.getSelectedItem() == null)
			{
				Log.trace(Log.l1_User, "VLookup.setValue - not in Lookup - set to NULL");
				actionCombo (null);             //  data binding (calls setValue again)
				m_value = null;
			}
		}
		m_settingValue = false;
	}	//	setValue

	/**
	 *  Property Change Listener
	 *  @param evt PropertyChangeEvent
	 */
	public void propertyChange (PropertyChangeEvent evt)
	{
	//	Log.trace(Log.l5_DData, "VLookup.propertyChange", evt);
		if (evt.getPropertyName().equals(MField.PROPERTY))
		{
			m_inserting = MField.INSERTING.equals(evt.getOldValue());
			setValue(evt.getNewValue());
			m_inserting = false;
		}
	}   //  propertyChange

	/**
	 *	Return Editor value (Integer)
	 *  @return value
	 */
	public Object getValue()
	{
		if (m_comboActive)
			return m_combo.getValue ();
		return m_value;
	}	//	getValue

	/**
	 *  Return editor display
	 *  @return display value
	 */
	public String getDisplay()
	{
		String retValue = null;
		if (m_comboActive)
			retValue = m_combo.getDisplay();
		//  check lookup
		else if (m_lookup == null)
			retValue = m_value.toString();
		else
			retValue = m_lookup.getDisplay(m_value);
	//	Log.trace(Log.l6_Database, "VLookup.getDisplay - " + retValue, "ComboActive=" + m_comboActive);
		return retValue;
	}   //  getDisplay

	/**
	 *  Set Field/WindowNo for ValuePreference
	 *  @param mField Model Field for Lookup
	 */
	public void setField (MField mField)
	{
		m_mField = mField;
		if (m_mField != null)
			ValuePreference.addMenu (this, popupMenu);
	}   //  setField

	/*************************************************************************/

	/**
	 *	Action Listener	- data binding
	 *  @param e ActionEvent
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (m_settingValue || m_settingFocus)
			return;
		Log.trace(Log.l4_Data, "VLookup.actionPerformed", e.getActionCommand() + ", ComboValue=" + m_combo.getSelectedItem());
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());

		//  Preference
		if (e.getActionCommand().equals(ValuePreference.NAME))
		{
			ValuePreference.start (m_mField, getValue(), getDisplay());
			return;
		}

		//  Combo Selection
		else if (e.getSource() == m_combo)
		{
			Object value = getValue();
			Object o = m_combo.getSelectedItem();
			if (o != null)
			{
				String s = o.toString();
				//  don't allow selection of inactive
				if (s.startsWith(MLookup.INACTIVE_S) && s.endsWith(MLookup.INACTIVE_E))
				{
					Log.trace(Log.l1_User, "VLookup.actionPerformed - Selection inactive set to NULL");
					value = null;
				}
			}
			actionCombo (value);                //  data binding
		}
		//  Button pressed
		else if (e.getSource() == m_button)
			actionButton ("");
		//  Text entered
		else if (e.getSource() == m_text)
			actionText();

		//  Popup Menu
		else if (e.getSource() == mZoom)
			actionZoom();
		else if (e.getSource() == mRefresh)
			actionRefresh();
		else if (e.getSource() == mBPartnerNew)
			actionBPartner(true);
		else if (e.getSource() == mBPartnerUpd)
			actionBPartner(false);
	}	//	actionPerformed

	/**
	 *  Action Listener Interface
	 *  @param listener listener
	 */
	public void addActionListener(ActionListener listener)
	{
		m_combo.addActionListener(listener);
		m_text.addActionListener(listener);
	}   //  addActionListener

	/**
	 *	Action - Combo.
	 *  <br>
	 *	== dataBinding == inform of new value
	 *  <pre>
	 *  VLookup.actionCombo
	 *      GridController.vetoableChange
	 *          MTable.setValueAt
	 *              MField.setValue
	 *                  VLookup.setValue
	 *          MTab.dataStatusChanged
	 *  </pre>
	 *  @param value new value
	 */
	private void actionCombo (Object value)
	{
	//	Log.trace(Log.l6_Database, "VLookup.actionCombo", value==null ? "null" : value.toString());
		try
		{
			fireVetoableChange (m_columnName, null, value);
		}
		catch (PropertyVetoException pve)
		{
			Log.error("VLookup.actionCombo", pve);
		}
		//  is the value updated ?
		boolean updated = false;
		if (value == null && m_value == null)
			updated = true;
		else if (value != null && value.equals(m_value))
			updated = true;
		if (!updated)
		{
			//  happens if VLookup is used outside of APanel/GridController (no property listener)
			Log.trace(Log.l6_Database, "VLookup.actionCombo - Value explicitly set - new=" + value + ", old=" + m_value);
			setValue(value);
		}
	}	//	actionCombo


	/**
	 *	Action - Button.
	 *	- Call Info
	 *	@param queryValue initial query value
	 */
	private void actionButton (String queryValue)
	{
		m_button.setEnabled(false);                 //  disable double click
		requestFocus();                             //  closes other editors
		Frame frame = Env.getFrame(this);

		/**
		 *  Three return options:
		 *  - Value Selected & OK pressed   => store result => result has value
		 *  - Cancel pressed                => store null   => result == null && cancelled
		 *  - Window closed                 -> ignore       => result == null && !cancalled
		 */
		Object result = null;
		boolean cancelled = false;
		//
		String col = m_lookup.getColumnName();		//	fully qualified name
		if (col.indexOf(".") != -1)
			col = col.substring(col.indexOf(".")+1);
		//  Zoom / Validation
		String whereClause = "";
		if (m_lookup.getZoomQuery() != null)
			whereClause = m_lookup.getZoomQuery().getWhereClause();
		if (whereClause.length() == 0)
			whereClause = m_lookup.getValidation();
		//
		Log.trace(Log.l5_DData, "VLookup.actionButton - " + col + ", Zoom=" + m_lookup.getZoom()
			+ " (" + whereClause + ")");
		//
		boolean resetValue = false;             //  reset value so that is always treated as new entry
		if (col.equals("M_Product_ID"))
		{
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = "@" + m_text.getText() + "@";   //  Name indicator - otherwise Value
			int M_Warehouse_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNo, "M_Warehouse_ID");
			int M_PriceList_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNo, "M_PriceList_ID");
			InfoProduct ip = new InfoProduct (frame, true, m_WindowNo,
				M_Warehouse_ID, M_PriceList_ID, queryValue, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
			resetValue = true;
		}
		else if (col.equals("C_BPartner_ID"))
		{
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = m_text.getText();
			boolean isSOTrx = true;     //  default
			if (Env.getContext(Env.getCtx(), m_WindowNo, "IsSOTrx").equals("N"))
				isSOTrx = false;
			InfoBPartner ip = new InfoBPartner (frame, true, m_WindowNo,
				queryValue, isSOTrx, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
		}
		else
		{
			String tableName = col;
			int index = m_columnName.indexOf("_ID");
			if (index != -1)
				tableName = m_columnName.substring(0, index);
			Info ig = Info.create (frame, true, m_WindowNo, tableName, m_columnName, queryValue, false, whereClause);
			ig.show();
			cancelled = ig.isCancelled();
			result = ig.getSelectedKey();
		}
		//  Result
		if (result != null)
		{
			Log.trace(Log.l4_Data, "VLookupactionButton", "Result = " + result.toString() + " " + result.getClass().getName());
			//  make sure that value is in cache
			m_lookup.getDirect(result, true);
			if (resetValue)
				actionCombo (null);
			actionCombo (result);
		}
		else if (cancelled)
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (cancelled)");
			actionCombo (null);
		}
		else
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (not cancelled)");
			setValue(m_value);      //  to re-display value
		}
		//
		m_button.setEnabled(true);
	}	//	actionButton

	/**
	 *	Check, if data returns unique entry, otherwise involve Info via Button
	 */
	private void actionText()
	{
		String text = m_text.getText();
		//	Nothing entered
		if (text == null || text.length() == 0 || text.equals("%"))
		{
			actionButton(text);
			return;
		}
		//	Always like
		if (!text.endsWith("%"))
			text += "%";
		text = text.toUpperCase();

		StringBuffer SQL = new StringBuffer();
		if (m_columnName.equals("M_Product_ID"))
		{
			SQL.append("SELECT M_Product_ID FROM M_Product WHERE UPPER(Value) LIKE ")
				.append(DB.TO_STRING(text))
				.append(" OR UPC LIKE ").append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_BPartner_ID"))
		{
			SQL.append("SELECT C_BPartner_ID FROM C_BPartner WHERE UPPER(Value) LIKE ")
				.append(DB.TO_STRING(text))
				.append(" OR UPPER(Name) LIKE ").append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_Order_ID"))
		{
			SQL.append("SELECT C_Order_ID FROM C_Order WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_Invoice_ID"))
		{
			SQL.append("SELECT C_Invoice_ID FROM C_Invoice WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("M_InOut_ID"))
		{
			SQL.append("SELECT M_InOut_ID FROM M_InOut WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_Payment_ID"))
		{
			SQL.append("SELECT C_Payment_ID FROM C_Payment WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("GL_JournalBatch_ID"))
		{
			SQL.append("SELECT GL_JournalBatch_ID FROM GL_JournalBatch WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else
		{
			SQL = getDirectAccessSQL (m_columnName, text);
			if (SQL.length() == 0)
			{
				actionButton (text);
				return;
			}
		}
		//	Finish SQL
		SQL.append(" AND IsActive='Y'");
		//	AddSecurity
		String tableName = m_columnName.substring(0, m_columnName.length()-3);
		String finalSQL = Access.addROAccessSQL(Env.getCtx(), SQL.toString(), tableName, false);
		int id = 0;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(finalSQL);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				id = rs.getInt(1);		//	first
				if (rs.next())
					id = -1;			//	only if unique
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("VLookup.actionText\nSQL=" + finalSQL, e);
			id = -2;
		}
		Log.trace(Log.l5_DData, "VLookup.actionText ID => " + id);

		//	No (unique) result
		if (id <= 0)
		{
			Log.trace(Log.l6_Database, finalSQL);
			actionButton(text);
			return;
		}
		m_value = null;     //  forces re-display if value is unchanged but text updated and still unique
		actionCombo (new Integer(id));          //  data binding
	}	//	actionText


	/**
	 * 	Generate Access SQL
	 *	@param columnName column
	 *	@param text upper like text
	 *	@return sql or ""
	 */
	private StringBuffer getDirectAccessSQL (String columnName, String text)
	{
		StringBuffer sb = new StringBuffer();
		String TableName = null;
		String sql = "SELECT t.TableName, c.ColumnName "
			+ "FROM AD_Column c "
			+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID AND t.IsView='N') "
			+ "WHERE c.ColumnName IN ('DocumentNo', 'Value')"
			+ " AND EXISTS (SELECT * FROM AD_Column cc WHERE cc.AD_Table_ID=t.AD_Table_ID"
				+ " AND cc.IsKey='Y' AND cc.ColumnName=?)";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setString(1, columnName);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				if (sb.length() != 0)
					sb.append(" OR ");
				TableName = rs.getString(1);
				sb.append("UPPER(").append(rs.getString(2)).append(") LIKE ").append(DB.TO_STRING(text));
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException ex)
		{
			Log.error("", ex);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
		}
		catch (SQLException ex1)
		{
		}
		pstmt = null;
		if (TableName == null || sb.length() == 0)
			return sb;
		StringBuffer retValue = new StringBuffer ("SELECT ")
			.append(TableName).append(" FROM ").append(TableName)
			.append(" WHERE ").append(sb);
		return retValue;
	}	//	getDirectAccessSQL


	/**
	 *	Action - Special BPartner Screen
	 *  @param newRecord true if new record should be created
	 */
	private void actionBPartner (boolean newRecord)
	{
		VBPartner vbp = new VBPartner (Env.getFrame(this), m_WindowNo);
		int BPartner_ID = 0;
		//  if update, get current value
		if (!newRecord)
		{
			if (m_value instanceof Integer)
				BPartner_ID = ((Integer)m_value).intValue();
			else if (m_value != null)
				BPartner_ID = Integer.parseInt(m_value.toString());
		}

		vbp.loadBPartner (BPartner_ID);
		vbp.show();
		//  get result
		int result = vbp.getBPartner_ID();
		if (result == 0					//	0 = not saved
			&& result == BPartner_ID)	//	the same
			return;
		//  Maybe new BPartner - put in cache
		m_lookup.getDirect(new Integer(result), true);

		actionCombo (new Integer(result));      //  data binding
	}	//	actionBPartner

	/**
	 *	Action - Zoom
	 */
	private void actionZoom()
	{
		if (m_lookup == null)
			return;
		//
		String IsSOTrx = Env.getContext(Env.getCtx(), m_WindowNo, "IsSOTrx");
		int AD_Window_ID = m_lookup.getZoom(IsSOTrx);
		MQuery zoomQuery = m_lookup.getZoomQuery();
		Log.trace(Log.l1_User, "VLookup.actionZoom - " + AD_Window_ID + " - IsSOTrx=" + IsSOTrx + " - Query=" + zoomQuery + " - Value=" + getValue());
		//	If not already exist
		if (zoomQuery == null || (!zoomQuery.isActive() && getValue() != null))
		{
			zoomQuery = new MQuery();
			zoomQuery.addRestriction(m_columnName, MQuery.EQUAL, getValue());
		}
		//
		setCursor(Cursor.getDefaultCursor());
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		AWindow frame = new AWindow();
		if (!frame.initWindow(AD_Window_ID, zoomQuery, IsSOTrx.equals("Y")))
		{
			setCursor(Cursor.getDefaultCursor());
			ValueNamePair pp = Log.retrieveError();
			String msg = pp==null ? "AccessTableNoView" : pp.getValue();
			ADialog.error(m_WindowNo, this, msg, pp==null ? "" : pp.getName());
		}
		else
			AEnv.showCenterScreen(frame);
			//  async window - not able to get feedback
		frame = null;
		//
		setCursor(Cursor.getDefaultCursor());
	}	//	actionZoom

	/**
	 *	Action - Refresh
	 */
	private void actionRefresh()
	{
		if (m_lookup == null)
			return;
		//
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		Object obj = m_combo.getSelectedItem();
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + obj);
		m_lookup.refresh();
		if (m_lookup.isValidated())
			m_lookup.fillComboBox(isMandatory(), false, false, false);
		else
			m_lookup.fillComboBox(isMandatory(), true, false, false);
		m_combo.setSelectedItem(obj);
	//	m_combo.revalidate();
		//
		setCursor(Cursor.getDefaultCursor());
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + m_combo.getSelectedItem());
	}	//	actionRefresh

	/*************************************************************************/

	/**
	 *	Focus Listener for ComboBoxes with missing Validation or invalid entries
	 *	- Requery listener for updated list
	 *  @param e FocusEvent
	 */
	public void focusGained (FocusEvent e)
	{
		if (e.getSource() != m_combo || e.isTemporary() || m_haveFocus)
			return;
		//
		m_haveFocus = true;     //  prevents calling focus gained twice
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Object obj = m_lookup.getSelectedItem();
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Start    Count=" + m_combo.getItemCount() + ", Selected=" + obj);
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());
		m_lookup.fillComboBox(isMandatory(), true, true, true);     //  only validated & active & temporary
	//	Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Update   Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		m_lookup.setSelectedItem(obj);
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Selected Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		//
		m_settingFocus = false;
	}	//	focusGained

	/**
	 *	Reset Selection List
	 *  @param e FocusEvent
	 */
	public void focusLost(FocusEvent e)
	{
		if (e.getSource() != m_combo || e.isTemporary())
			return;
		//
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Log.trace(Log.l4_Data, "VLookup.focusLost", m_columnName + " = " + m_combo.getSelectedItem());
		Object obj = m_combo.getSelectedItem();
		//	set original model
		if (!m_lookup.isValidated())
			m_lookup.fillComboBox(true);    //  previous selection
		//	Set value
		if (obj != null)
		{
			m_combo.setSelectedItem(obj);
			//	original model may not have item
			if (!m_combo.getSelectedItem().equals(obj))
			{
				Log.trace(Log.l6_Database, "VLookup.focusLost " + m_columnName, "added to combo - " + obj);
				m_combo.addItem(obj);
				m_combo.setSelectedItem(obj);
			}
		}
	//	actionCombo(getValue());
		m_settingFocus = false;
		m_haveFocus = false;    //  can gain focus again
	}	//	focusLost

	/**
	 *  Set ToolTip
	 *  @param text tool tip text
	 */
	public void setToolTipText(String text)
	{
		super.setToolTipText(text);
		m_button.setToolTipText(text);
		m_text.setToolTipText(text);
		m_combo.setToolTipText(text);
	}   //  setToolTipText

}	//	VLookup

/*****************************************************************************/

/**
 *	Mouse Listener for Popup Menu
 */
final class VLookup_mouseAdapter extends java.awt.event.MouseAdapter
{
	/**
	 *	Constructor
	 *  @param adaptee adaptee
	 */
	VLookup_mouseAdapter(VLookup adaptee)
	{
		this.adaptee = adaptee;
	}	//	VLookup_mouseAdapter

	private VLookup adaptee;

	/**
	 *	Mouse Listener
	 *  @param e MouseEvent
	 */
	public void mouseClicked(MouseEvent e)
	{
	//	System.out.println("mouseClicked " + e.getID() + " " + e.getSource().getClass().toString());
		//	popup menu
		if (SwingUtilities.isRightMouseButton(e))
			adaptee.popupMenu.show((Component)e.getSource(), e.getX(), e.getY());
	}	//	mouse Clicked

}	//	VLookup_mouseAdapter
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
package org.compiere.grid.ed;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;
import java.sql.*;

import org.compiere.apps.*;
import org.compiere.util.*;
import org.compiere.model.*;
import org.compiere.apps.search.*;
import org.compiere.swing.*;

/**
 *  Lookup Field.
 *  <p>
 *	    When r/o - display a Label
 *		When STABLE - display a ComboBox
 *		Otherwise show Selection Dialog
 *  <p>
 *  Sepecial handling of BPartner and Product
 *
 *  @author 	Jorg Janke
 *  @version 	$Id: VLookup.java,v 1.65 2004/05/14 05:35:01 jjanke Exp $
 */
public class VLookup extends JComponent
	implements VEditor, ActionListener, FocusListener
{
	/**
	 *  Create Optional BPartner Search Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createBPartner (int WindowNo)
	{
		int AD_Column_ID = 3499;    //  C_Invoice.C_BPartner_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, 0, AD_Column_ID, DisplayType.Search);
			return new VLookup ("C_BPartner_ID", false, false, true, lookup);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createBPartner", e);
		}
		return null;
	}   //  createBPartner

	/**
	 *  Create Optional Product Search Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createProduct (int WindowNo)
	{
		int AD_Column_ID = 3840;    //  C_InvoiceLine.M_Product_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, 0, AD_Column_ID, DisplayType.Search);
			return new VLookup ("M_Product_ID", false, false, true, lookup);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createProduct", e);
		}
		return null;
	}   //  createProduct

	/**
	 *  Create Optional User Search Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createUser (int WindowNo)
	{
		int AD_Column_ID = 10443;    //  AD_WF_Activity.AD_User_UD
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, 0, AD_Column_ID, DisplayType.Search);
			return new VLookup ("AD_User_ID", false, false, true, lookup);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createUser", e);
		}
		return null;
	}   //  createProduct

	
	/*************************************************************************
	 *	Detail Constructor
	 *
	 *  @param columnName column
	 *  @param mandatory mandatory
	 *  @param isReadOnly read only
	 *  @param isUpdateable updateable
	 *  @param lookup lookup
	 */
	public VLookup (String columnName, boolean mandatory, boolean isReadOnly, boolean isUpdateable,
		Lookup lookup)
	{
		super();
		super.setName(columnName);
		m_combo.setName(columnName);
		m_columnName = columnName;
		setMandatory(mandatory);
		m_lookup = lookup;
		//
		setLayout(new BorderLayout());
		VLookup_mouseAdapter mouse = new VLookup_mouseAdapter(this);    //  popup

		//	***	Text & Button	***
		m_text.addActionListener(this);
		m_text.addFocusListener(this);
		m_text.addMouseListener(mouse);
		//  Button
		m_button.addActionListener(this);
		m_button.addMouseListener(mouse);
		m_button.setFocusable(false);   //  don't focus when tabbing
		if (columnName.equals("C_BPartner_ID"))
			m_button.setIcon(Env.getImageIcon("BPartner10.gif"));
		else if (columnName.equals("M_Product_ID"))
			m_button.setIcon(Env.getImageIcon("Product10.gif"));
		else
			m_button.setIcon(Env.getImageIcon("PickOpen10.gif"));

		//	*** VComboBox	***
		if (m_lookup != null && m_lookup.getDisplayType() != DisplayType.Search)	//	No Search
		{
			//  Memory Leak after executing the next two lines ??
			m_lookup.fillComboBox (isMandatory(), false, false, false);
			m_combo.setModel(m_lookup);
			//
			m_combo.addActionListener(this);							//	Selection
			m_combo.addMouseListener(mouse);	                        //	popup
			//	FocusListener to refresh selection before opening
			m_combo.addFocusListener(this);
		}

		setUI (true);
		//	ReadWrite	-	decides what components to show
		if (isReadOnly || !isUpdateable || m_lookup == null)
			setReadWrite(false);
		else
			setReadWrite(true);

		//	Popup
		if (m_lookup != null)
		{
			if ((m_lookup.getDisplayType() == DisplayType.List && Env.getContextAsInt(Env.getCtx(), "#AD_Role_ID") == 0)
				|| m_lookup.getDisplayType() != DisplayType.List)     //  only system admins can change lists, so no need to zoom for others
			{
				mZoom = new JMenuItem(Msg.getMsg(Env.getCtx(), "Zoom"), Env.getImageIcon("Zoom16.gif"));
				mZoom.addActionListener(this);
				popupMenu.add(mZoom);
			}
			mRefresh = new JMenuItem(Msg.getMsg(Env.getCtx(), "Refresh"), Env.getImageIcon("Refresh16.gif"));
			mRefresh.addActionListener(this);
			popupMenu.add(mRefresh);
		}
		//	VBPartner quick entry link
		if (columnName.equals("C_BPartner_ID"))
		{
			mBPartnerNew = new JMenuItem (Msg.getMsg(Env.getCtx(), "New"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerNew.addActionListener(this);
			popupMenu.add(mBPartnerNew);
			mBPartnerUpd = new JMenuItem (Msg.getMsg(Env.getCtx(), "Update"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerUpd.addActionListener(this);
			popupMenu.add(mBPartnerUpd);
		}
		//
		if (m_lookup != null && m_lookup.getZoom() == 0)
			mZoom.setEnabled(false);
	}	//	VLookup

	/**
	 *  Dispose
	 */
	public void dispose()
	{
		m_text = null;
		m_button = null;
		m_lookup = null;
		m_mField = null;
		//
		m_combo.removeFocusListener(this);
		m_combo.removeActionListener(this);
		m_combo.setModel(new DefaultComboBoxModel());    //  remove reference
	//	m_combo.removeAllItems();
		m_combo = null;
	}   //  dispose

	/** Display Length for Lookups (15)         */
	public final static int     DISPLAY_LENGTH = 15;

	/** Search: The Editable Text Field         */
	private CTextField 			m_text = new CTextField (DISPLAY_LENGTH);
	/** Search: The Button to open Editor   */
	private CButton				m_button = new CButton();
	/** The Combo Box if not a Search Lookup    */
	private VComboBox			m_combo = new VComboBox();
	/** Indicator that value is being set       */
	private volatile boolean 	m_settingValue = false;
	private volatile boolean 	m_settingFocus = false;
	/** Indicator that Lookup has focus         */
	private volatile boolean	m_haveFocus = false;
	/** Indicator - inserting new value			*/
	private volatile boolean	m_inserting = false;
	/** Last Display							*/
	private String				m_lastDisplay = "";

	//
	private String				m_columnName;
	private Lookup				m_lookup;

	private boolean				m_comboActive = true;
	private Object				m_value;

	//	Popup
	JPopupMenu 					popupMenu = new JPopupMenu();
	private JMenuItem 			mZoom;
	private JMenuItem 			mRefresh;
	private JMenuItem			mBPartnerNew;
	private JMenuItem			mBPartnerUpd;

	private MField              m_mField = null;

	/**
	 *  Set Content and Size of Compoments
	 *  @param initial if true, size and margins will be set
	 */
	private void setUI (boolean initial)
	{
		if (initial)
		{
			Dimension size = m_text.getPreferredSize();
			setPreferredSize(new Dimension(size));  //	causes r/o to be the same length
			m_combo.setPreferredSize(new Dimension(size));
			setMinimumSize(new Dimension (30,size.height));
			//
			m_text.setBorder(null);
			Dimension bSize = new Dimension(size.height, size.height);
			m_button.setPreferredSize (bSize);
			m_button.setMargin(new Insets(0, 0, 0, 0));
		}

		//	What to show
		this.remove(m_combo);
		this.remove(m_button);
		this.remove(m_text);
		//
		if (!isReadWrite())									//	r/o - show text only
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			m_text.setReadWrite(false);
			m_combo.setReadWrite(false);
			m_comboActive = false;
		}
		else if (m_lookup != null && m_lookup.getDisplayType() != DisplayType.Search)	    //	show combo if not Search
		{
			this.setBorder(null);
			this.add(m_combo, BorderLayout.CENTER);
			m_comboActive = true;
		}
		else 												//	Search or unstable - show text & button
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			this.add(m_button, BorderLayout.EAST);
			m_text.setReadWrite (true);
			m_comboActive = false;
		}
	}   //  setUI

	/**
	 *	Set ReadWrite
	 *  @param value ReadWrite
	 */
	public void setReadWrite (boolean value)
	{
		boolean rw = value;
		if (m_lookup == null)
			rw = false;
		if (m_combo.isReadWrite() != value)
		{
			m_combo.setReadWrite(rw);
			setUI (false);
			if (m_comboActive)
				setValue (m_value);
		}
	}	//	setReadWrite

	/**
	 *	IsEditable
	 *  @return is lookup ReadWrite
	 */
	public boolean isReadWrite()
	{
		return m_combo.isReadWrite();
	}	//	isReadWrite

	/**
	 *	Set Mandatory (and back bolor)
	 *  @param mandatory mandatory
	 */
	public void setMandatory (boolean mandatory)
	{
		m_combo.setMandatory(mandatory);
		m_text.setMandatory(mandatory);
	}	//	setMandatory

	/**
	 *	Is it mandatory
	 *  @return true if mandatory
	 */
	public boolean isMandatory()
	{
		return m_combo.isMandatory();
	}	//	isMandatory

	/**
	 *	Set Background
	 *  @param color color
	 */
	public void setBackground(Color color)
	{
		m_text.setBackground(color);
		m_combo.setBackground(color);
	}	//	setBackground

	/**
	 *	Set Background
	 *  @param error error
	 */
	public void setBackground (boolean error)
	{
		m_text.setBackground(error);
		m_combo.setBackground(error);
	}	//	setBackground

	/**
	 *  Set Foreground
	 *  @param fg Foreground color
	 */
	public void setForeground(Color fg)
	{
		m_text.setForeground(fg);
		m_combo.setForeground(fg);
	}   //  setForeground

	/**
	 *  Set Editor to value
	 *  @param value new Value
	 */
	public void setValue (Object value)
	{
		Log.trace(Log.l6_Database, "VLookup.setValue", m_columnName + "=" + value);
		m_settingValue = true;		//	disable actions
		m_value = value;

		//	Set both for switching
		m_combo.setValue (value);
		if (value == null)
		{
			m_text.setText (null);
			m_lastDisplay = "";
			m_settingValue = false;
			return;
		}
		if (m_lookup == null)
		{
			m_text.setText (value.toString());
			m_lastDisplay = value.toString();
			m_settingValue = false;
			return;
		}

		//	Set Display
		m_lastDisplay = m_lookup.getDisplay(value);
		if (m_lastDisplay.equals("<-1>"))
		{
			m_lastDisplay = "";
			m_value = null;
		}
		boolean notFound = m_lastDisplay.startsWith("<") && m_lastDisplay.startsWith(">");
		m_text.setText (m_lastDisplay);
		m_text.setCaretPosition (0); //	show beginning

		//	Nothing showing in Combo and should be showing
		if (m_combo.getSelectedItem() == null
			&& (m_comboActive || (m_inserting && m_lookup.getDisplayType() != DisplayType.Search)))
		{
			//  lookup found nothing too
			if (notFound)
			{
				Log.trace(8, "VLookup.setValue - Not found (1)", m_lastDisplay);
				//  we may have a new value
				m_lookup.refresh();
				m_combo.setValue (value);
				m_lastDisplay = m_lookup.getDisplay(value);
				m_text.setText (m_lastDisplay);
				m_text.setCaretPosition (0);	//	show beginning
				notFound = m_lastDisplay.startsWith("<") && m_lastDisplay.endsWith(">");
			}
			if (notFound)	//	<key>
			{
				m_value = null;
				actionCombo (null);             //  data binding
				Log.trace(Log.l6_Database, "VLookup.setValue - not found - " + value);
			}
			//  we have lookup
			else if (m_combo.getSelectedItem() == null)
			{
				NamePair pp = m_lookup.get(value);
				if (pp != null)
				{
					Log.trace (Log.l6_Database, "VLookup.setValue - added to combo - " + pp);
					//  Add to Combo
					m_combo.addItem (pp);
					m_combo.setValue (value);
				}
			}
			//  Not in Lookup - set to Null
			if (m_combo.getSelectedItem() == null)
			{
				Log.trace(Log.l1_User, "VLookup.setValue - not in Lookup - set to NULL");
				actionCombo (null);             //  data binding (calls setValue again)
				m_value = null;
			}
		}
		m_settingValue = false;
	}	//	setValue

	/**
	 *  Property Change Listener
	 *  @param evt PropertyChangeEvent
	 */
	public void propertyChange (PropertyChangeEvent evt)
	{
	//	Log.trace(Log.l5_DData, "VLookup.propertyChange", evt);
		if (evt.getPropertyName().equals(MField.PROPERTY))
		{
			m_inserting = MField.INSERTING.equals(evt.getOldValue());	//	MField.setValue
			setValue(evt.getNewValue());
			m_inserting = false;
		}
	}   //  propertyChange

	/**
	 *	Return Editor value (Integer)
	 *  @return value
	 */
	public Object getValue()
	{
		if (m_comboActive)
			return m_combo.getValue ();
		return m_value;
	}	//	getValue

	/**
	 *  Return editor display
	 *  @return display value
	 */
	public String getDisplay()
	{
		String retValue = null;
		if (m_comboActive)
			retValue = m_combo.getDisplay();
		//  check lookup
		else if (m_lookup == null)
			retValue = m_value.toString();
		else
			retValue = m_lookup.getDisplay(m_value);
	//	Log.trace(Log.l6_Database, "VLookup.getDisplay - " + retValue, "ComboActive=" + m_comboActive);
		return retValue;
	}   //  getDisplay

	/**
	 *  Set Field/WindowNo for ValuePreference
	 *  @param mField Model Field for Lookup
	 */
	public void setField (MField mField)
	{
		m_mField = mField;
		if (m_mField != null)
			ValuePreference.addMenu (this, popupMenu);
	}   //  setField

	/*************************************************************************/

	/**
	 *	Action Listener	- data binding
	 *  @param e ActionEvent
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (m_settingValue || m_settingFocus)
			return;
		Log.trace(Log.l4_Data, "VLookup.actionPerformed", e.getActionCommand() + ", ComboValue=" + m_combo.getSelectedItem());
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());

		//  Preference
		if (e.getActionCommand().equals(ValuePreference.NAME))
		{
			ValuePreference.start (m_mField, getValue(), getDisplay());
			return;
		}

		//  Combo Selection
		else if (e.getSource() == m_combo)
		{
			Object value = getValue();
			Object o = m_combo.getSelectedItem();
			if (o != null)
			{
				String s = o.toString();
				//  don't allow selection of inactive
				if (s.startsWith(MLookup.INACTIVE_S) && s.endsWith(MLookup.INACTIVE_E))
				{
					Log.trace(Log.l1_User, "VLookup.actionPerformed - Selection inactive set to NULL");
					value = null;
				}
			}
			actionCombo (value);                //  data binding
		}
		//  Button pressed
		else if (e.getSource() == m_button)
			actionButton ("");
		//  Text entered
		else if (e.getSource() == m_text)
			actionText();

		//  Popup Menu
		else if (e.getSource() == mZoom)
			actionZoom();
		else if (e.getSource() == mRefresh)
			actionRefresh();
		else if (e.getSource() == mBPartnerNew)
			actionBPartner(true);
		else if (e.getSource() == mBPartnerUpd)
			actionBPartner(false);
	}	//	actionPerformed

	/**
	 *  Action Listener Interface
	 *  @param listener listener
	 */
	public void addActionListener(ActionListener listener)
	{
		m_combo.addActionListener(listener);
		m_text.addActionListener(listener);
	}   //  addActionListener

	/**
	 *	Action - Combo.
	 *  <br>
	 *	== dataBinding == inform of new value
	 *  <pre>
	 *  VLookup.actionCombo
	 *      GridController.vetoableChange
	 *          MTable.setValueAt
	 *              MField.setValue
	 *                  VLookup.setValue
	 *          MTab.dataStatusChanged
	 *  </pre>
	 *  @param value new value
	 */
	private void actionCombo (Object value)
	{
		Log.trace(Log.l6_Database, "VLookup.actionCombo", value==null ? "null" : value.toString());
		try
		{
			fireVetoableChange (m_columnName, null, value);
		}
		catch (PropertyVetoException pve)
		{
			Log.error("VLookup.actionCombo", pve);
		}
		//  is the value updated ?
		boolean updated = false;
		if (value == null && m_value == null)
			updated = true;
		else if (value != null && value.equals(m_value))
			updated = true;
		if (!updated)
		{
			//  happens if VLookup is used outside of APanel/GridController (no property listener)
			Log.trace(Log.l6_Database, "VLookup.actionCombo",
				"Value explicitly set - new=" + value + ", old=" + m_value);
			setValue(value);
		}
	}	//	actionCombo


	/**
	 *	Action - Button.
	 *	- Call Info
	 *	@param queryValue initial query value
	 */
	private void actionButton (String queryValue)
	{
		m_button.setEnabled(false);                 //  disable double click
		if (m_lookup == null)
			return;		//	leave button disabled
		this.requestFocus();						//  closes other editors
		Frame frame = Env.getFrame(this);

		/**
		 *  Three return options:
		 *  - Value Selected & OK pressed   => store result => result has value
		 *  - Cancel pressed                => store null   => result == null && cancelled
		 *  - Window closed                 -> ignore       => result == null && !cancalled
		 */
		Object result = null;
		boolean cancelled = false;
		//
		String col = m_lookup.getColumnName();		//	fully qualified name
		if (col.indexOf(".") != -1)
			col = col.substring(col.indexOf(".")+1);
		//  Zoom / Validation
		String whereClause = getWhereClause();
		//
		Log.trace(Log.l5_DData, "VLookup.actionButton - " + col 
			+ ", Zoom=" + m_lookup.getZoom()
			+ " (" + whereClause + ")");
		//
		boolean resetValue = false;	//	reset value so that is always treated as new entry    
		if (col.equals("M_Product_ID"))
		{
			//	Reset
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_Product_ID", "0");
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_AttributeSetInstance_ID", "0");
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = "@" + m_text.getText() + "@";   //  Name indicator - otherwise Value
			int M_Warehouse_ID = Env.getContextAsInt(Env.getCtx(), m_lookup.getWindowNo(), "M_Warehouse_ID");
			int M_PriceList_ID = Env.getContextAsInt(Env.getCtx(), m_lookup.getWindowNo(), "M_PriceList_ID");
			//	Show Info
			InfoProduct ip = new InfoProduct (frame, true, m_lookup.getWindowNo(),
				M_Warehouse_ID, M_PriceList_ID, queryValue, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
			resetValue = true;
		}
		else if (col.equals("C_BPartner_ID"))
		{
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = m_text.getText();
			boolean isSOTrx = true;     //  default
			if (Env.getContext(Env.getCtx(), m_lookup.getWindowNo(), "IsSOTrx").equals("N"))
				isSOTrx = false;
			InfoBPartner ip = new InfoBPartner (frame, true, m_lookup.getWindowNo(),
				queryValue, isSOTrx, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
		}
		else	//	General Info
		{
			if (m_tableName == null)	//	sets table name & key column
				getDirectAccessSQL("*");
			Info ig = Info.create (frame, true, m_lookup.getWindowNo(), 
				m_tableName, m_keyColumnName, queryValue, false, whereClause);
			ig.show();
			cancelled = ig.isCancelled();
			result = ig.getSelectedKey();
		}
		
		//  Result
		if (result != null)
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = " + result.toString() + " (" + result.getClass().getName() + ")");
			//  make sure that value is in cache
			m_lookup.getDirect(result, false, true);
			if (resetValue)
				actionCombo (null);
			actionCombo (result);
		}
		else if (cancelled)
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (cancelled)");
			actionCombo (null);
		}
		else
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (not cancelled)");
			setValue(m_value);      //  to re-display value
		}
		//
		m_button.setEnabled(true);
		m_text.requestFocus();
	}	//	actionButton

	/**
	 * 	Get Where Clause
	 *	@return where clause or ""
	 */
	private String getWhereClause()
	{
		String whereClause = "";
		if (m_lookup == null)
			return "";
		if (m_lookup.getZoomQuery() != null)
			whereClause = m_lookup.getZoomQuery().getWhereClause();
		if (whereClause.length() == 0)
			whereClause = m_lookup.getValidation();
	//	Log.trace(9, "VLookup.getWhereClause - ZoomQuery=" 
	//		+ (m_lookup.getZoomQuery()==null ? "" : m_lookup.getZoomQuery().getWhereClause())
	//		+ ", Validation=" + m_lookup.getValidation());
		return whereClause;
	}	//	getWhereClause

	/**
	 *	Check, if data returns unique entry, otherwise involve Info via Button
	 */
	private void actionText()
	{
		String text = m_text.getText();
		//	Nothing entered
		if (text == null || text.length() == 0 || text.equals("%"))
		{
			actionButton(text);
			return;
		}
		//	Always like
		if (!text.endsWith("%"))
			text += "%";
		text = text.toUpperCase();
		Log.trace(Log.l4_Data, "VLookup.actionText", m_columnName + " - " + text);

		String finalSQL = Msg.parseTranslation(Env.getCtx(), getDirectAccessSQL(text));
		int id = 0;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(finalSQL);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				id = rs.getInt(1);		//	first
				if (rs.next())
					id = -1;			//	only if unique
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e)
		{
			Log.error("VLookup.actionText\nSQL=" + finalSQL, e);
			id = -2;
		}

		//	No (unique) result
		if (id <= 0)
		{
			if (id == 0)
				Log.trace(Log.l6_Database, "VLookup.actionText - Not Found - " + finalSQL);
			else
				Log.trace(Log.l6_Database, "VLookup.actionText - Not Unique - " + finalSQL);
			m_value = null;	// force re-display
			actionButton(m_text.getText());
			return;
		}
		Log.trace(Log.l5_DData, "VLookup.actionText - Unique ID => " + id);
		m_value = null;     //  forces re-display if value is unchanged but text updated and still unique
		actionCombo (new Integer(id));          //  data binding
	}	//	actionText


	private String		m_tableName = null;
	private String		m_keyColumnName = null;

	/**
	 * 	Generate Access SQL for Search.
	 * 	The SQL returns the ID of the value entered
	 * 	Also sets m_tableName and m_keyColumnName
	 *	@param text uppercase text for LIKE comparison
	 *	@return sql or ""
	 *  Example
	 *	SELECT C_Payment_ID FROM C_Payment WHERE UPPER(DocumentNo) LIKE x OR ...
	 */
	private String getDirectAccessSQL (String text)
	{
		StringBuffer sql = new StringBuffer();
		m_tableName = m_columnName.substring(0, m_columnName.length()-3);
		m_keyColumnName = m_columnName;
		//
		if (m_columnName.equals("M_Product_ID"))
		{
			//	Reset
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_Product_ID", "0");
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_AttributeSetInstance_ID", "0");
			//
			sql.append("SELECT M_Product_ID FROM M_Product WHERE (UPPER(Value) LIKE ")
				.append(DB.TO_STRING(text))
				.append(" OR UPC LIKE ").append(DB.TO_STRING(text)).append(")");
		}
		else if (m_columnName.equals("C_BPartner_ID"))
		{
			sql.append("SELECT C_BPartner_ID FROM C_BPartner WHERE (UPPER(Value) LIKE ")
				.append(DB.TO_STRING(text))
				.append(" OR UPPER(Name) LIKE ").append(DB.TO_STRING(text)).append(")");
		}
		else if (m_columnName.equals("C_Order_ID"))
		{
			sql.append("SELECT C_Order_ID FROM C_Order WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_Invoice_ID"))
		{
			sql.append("SELECT C_Invoice_ID FROM C_Invoice WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("M_InOut_ID"))
		{
			sql.append("SELECT M_InOut_ID FROM M_InOut WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_Payment_ID"))
		{
			sql.append("SELECT C_Payment_ID FROM C_Payment WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("GL_JournalBatch_ID"))
		{
			sql.append("SELECT GL_JournalBatch_ID FROM GL_JournalBatch WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("SalesRep_ID"))
		{
			sql.append("SELECT AD_User_ID FROM AD_User WHERE UPPER(Name) LIKE ")
				.append(DB.TO_STRING(text));
			m_tableName = "AD_User";
			m_keyColumnName = "AD_User_ID";
		}
		//	Predefined
		if (sql.length() > 0)
		{
			String wc = getWhereClause();
			if (wc != null && wc.length() > 0)
				sql.append(" AND ").append(wc);
			sql.append(" AND IsActive='Y'");
			//	***
			Log.trace(9, "VLookup.getDirectAccessSQL (predefined) " + sql.toString());
			return MRole.getDefault().addAccessSQL(sql.toString(),
				m_tableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
		}
		
		//	Check if it is a Table Reference
		if (m_lookup != null && m_lookup instanceof MLookup)
		{
			int AD_Reference_ID = ((MLookup)m_lookup).getAD_Reference_Value_ID();
			if (AD_Reference_ID != 0)
			{
				String query = "SELECT kc.ColumnName, dc.ColumnName, t.TableName "
					+ "FROM AD_Ref_Table rt"
					+ " INNER JOIN AD_Column kc ON (rt.AD_Key=kc.AD_Column_ID)"
					+ " INNER JOIN AD_Column dc ON (rt.AD_Display=dc.AD_Column_ID)"
					+ " INNER JOIN AD_Table t ON (rt.AD_Table_ID=t.AD_Table_ID) "
					+ "WHERE rt.AD_Reference_ID=?";
				String displayColumnName = null;
				PreparedStatement pstmt = null;
				try
				{
					pstmt = DB.prepareStatement(query);
					pstmt.setInt(1, AD_Reference_ID);
					ResultSet rs = pstmt.executeQuery();
					if (rs.next())
					{
						m_keyColumnName = rs.getString(1);
						displayColumnName = rs.getString(2);
						m_tableName = rs.getString(3);
					}
					rs.close();
					pstmt.close();
					pstmt = null;
				}
				catch (Exception e)
				{
					Log.error("VLookup.getDirectAccessSQL", e);
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
				if (displayColumnName != null)
				{
					sql = new StringBuffer();
					sql.append("SELECT ").append(m_keyColumnName)
						.append(" FROM ").append(m_tableName)
						.append(" WHERE UPPER(").append(displayColumnName)
						.append(") LIKE ").append(DB.TO_STRING(text))
						.append(" AND IsActive='Y'");
					String wc = getWhereClause();
					if (wc != null && wc.length() > 0)
						sql.append(" AND ").append(wc);
					//	***
					Log.trace(9, "VLookup.getDirectAccessSQL (Table) " + sql.toString());
					return MRole.getDefault().addAccessSQL(sql.toString(),
								m_tableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
				}
			}	//	Table Reference
		}	//	MLookup
		
		/** Check Well Known Columns of Table - assumes TableDir	**/
		String query = "SELECT t.TableName, c.ColumnName "
			+ "FROM AD_Column c "
			+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID AND t.IsView='N') "
			+ "WHERE (c.ColumnName IN ('DocumentNo', 'Value', 'Name') OR c.IsIdentifier='Y')"
			+ " AND c.AD_Reference_ID IN (10,14)"
			+ " AND EXISTS (SELECT * FROM AD_Column cc WHERE cc.AD_Table_ID=t.AD_Table_ID"
				+ " AND cc.IsKey='Y' AND cc.ColumnName=?)";
		m_keyColumnName = m_columnName;
		sql = new StringBuffer();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(query);
			pstmt.setString(1, m_keyColumnName);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				if (sql.length() != 0)
					sql.append(" OR ");
				m_tableName = rs.getString(1);
				sql.append("UPPER(").append(rs.getString(2)).append(") LIKE ").append(DB.TO_STRING(text));
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException ex)
		{
			Log.error("VLookup.getDirectAccessSQL", ex);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
		}
		catch (SQLException ex1)
		{
		}
		pstmt = null;
		//
		if (sql.length() == 0)
		{
			Log.error("VLookup.getDirectAccessSQL (TableDir) - no standard/identifier columns");
			return "";
		}
		//
		StringBuffer retValue = new StringBuffer ("SELECT ")
			.append(m_columnName).append(" FROM ").append(m_tableName)
			.append(" WHERE ").append(sql)
			.append(" AND IsActive='Y'");
		String wc = getWhereClause();
		if (wc != null && wc.length() > 0)
			retValue.append(" AND ").append(wc);
		//	***
		Log.trace(9, "VLookup.getDirectAccessSQL (TableDir) " + sql.toString());
		return MRole.getDefault().addAccessSQL(retValue.toString(),
					m_tableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
	}	//	getDirectAccessSQL


	/**
	 *	Action - Special BPartner Screen
	 *  @param newRecord true if new record should be created
	 */
	private void actionBPartner (boolean newRecord)
	{
		VBPartner vbp = new VBPartner (Env.getFrame(this), m_lookup.getWindowNo());
		int BPartner_ID = 0;
		//  if update, get current value
		if (!newRecord)
		{
			if (m_value instanceof Integer)
				BPartner_ID = ((Integer)m_value).intValue();
			else if (m_value != null)
				BPartner_ID = Integer.parseInt(m_value.toString());
		}

		vbp.loadBPartner (BPartner_ID);
		vbp.show();
		//  get result
		int result = vbp.getBPartner_ID();
		if (result == 0					//	0 = not saved
			&& result == BPartner_ID)	//	the same
			return;
		//  Maybe new BPartner - put in cache
		m_lookup.getDirect(new Integer(result), false, true);

		actionCombo (new Integer(result));      //  data binding
	}	//	actionBPartner

	/**
	 *	Action - Zoom
	 */
	private void actionZoom()
	{
		if (m_lookup == null)
			return;
		//
		String IsSOTrx = Env.getContext(Env.getCtx(), m_lookup.getWindowNo(), "IsSOTrx");
		int AD_Window_ID = m_lookup.getZoom(IsSOTrx);
		MQuery zoomQuery = m_lookup.getZoomQuery();
		Log.trace(Log.l1_User, "VLookup.actionZoom - " + AD_Window_ID 
			+ " - IsSOTrx=" + IsSOTrx 
			+ " - Query=" + zoomQuery + " - Value=" + getValue());
		//	If not already exist or exact value
		if (zoomQuery == null || getValue() != null)
		{
			zoomQuery = new MQuery();	//	ColumnName might be changed in MTab.validateQuery
			zoomQuery.addRestriction(m_columnName, MQuery.EQUAL, getValue());
			Log.trace(8, "VLookup.actionZoom - Query = " + zoomQuery); 
		}
		//
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		AWindow frame = new AWindow();
		if (!frame.initWindow(AD_Window_ID, zoomQuery, IsSOTrx.equals("Y")))
		{
			setCursor(Cursor.getDefaultCursor());
			ValueNamePair pp = Log.retrieveError();
			String msg = pp==null ? "AccessTableNoView" : pp.getValue();
			ADialog.error(m_lookup.getWindowNo(), this, msg, pp==null ? "" : pp.getName());
		}
		else
			AEnv.showCenterScreen(frame);
			//  async window - not able to get feedback
		frame = null;
		//
		setCursor(Cursor.getDefaultCursor());
	}	//	actionZoom

	/**
	 *	Action - Refresh
	 */
	private void actionRefresh()
	{
		if (m_lookup == null)
			return;
		//
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		Object obj = m_combo.getSelectedItem();
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + obj);
		m_lookup.refresh();
		if (m_lookup.isValidated())
			m_lookup.fillComboBox(isMandatory(), false, false, false);
		else
			m_lookup.fillComboBox(isMandatory(), true, false, false);
		m_combo.setSelectedItem(obj);
	//	m_combo.revalidate();
		//
		setCursor(Cursor.getDefaultCursor());
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + m_combo.getSelectedItem());
	}	//	actionRefresh

	/**************************************************************************

	/**
	 *	Focus Listener for ComboBoxes with missing Validation or invalid entries
	 *	- Requery listener for updated list
	 *  @param e FocusEvent
	 */
	public void focusGained (FocusEvent e)
	{
		if (e.getSource() != m_combo || e.isTemporary() || m_haveFocus || m_lookup == null)
			return;
		if (m_lookup.isValidated() && !m_lookup.hasInactive())
			return;
		//
		m_haveFocus = true;     //  prevents calling focus gained twice
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Object obj = m_lookup.getSelectedItem();
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName 
			+ " - Start    Count=" + m_combo.getItemCount() + ", Selected=" + obj);
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());
		m_lookup.fillComboBox(isMandatory(), true, true, true);     //  only validated & active & temporary
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName 
			+ " - Update   Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		m_lookup.setSelectedItem(obj);
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName 
			+ " - Selected Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		//
		m_settingFocus = false;
	}	//	focusGained

	/**
	 *	Reset Selection List
	 *  @param e FocusEvent
	 */
	public void focusLost(FocusEvent e)
	{
		if (e.isTemporary() || m_lookup == null 
			|| !m_button.isEnabled() )
			return;
		if (e.getSource() == m_text)
		{
			String text = m_text.getText();
			Log.trace(Log.l4_Data, "VLookup.focusLost Text", 
				m_columnName + " = " + m_value + " - " + text);
			//	Skip if empty
			if ((m_value == null && m_text.getText().length() == 0))
				return;
			if (m_lastDisplay.equals(text))
				return;
			//
			actionText();
			m_haveFocus = false;
			return;
		}
		//	Combo lost focus
		if (e.getSource() != m_combo)
			return;
		if (m_lookup.isValidated() && !m_lookup.hasInactive())
			return;
		//
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Log.trace(Log.l4_Data, "VLookup.focusLost", m_columnName + " = " + m_combo.getSelectedItem());
		Object obj = m_combo.getSelectedItem();
		//	set original model
		if (!m_lookup.isValidated())
			m_lookup.fillComboBox(true);    //  previous selection
		//	Set value
		if (obj != null)
		{
			m_combo.setSelectedItem(obj);
			//	original model may not have item
			if (!m_combo.getSelectedItem().equals(obj))
			{
				Log.trace(Log.l6_Database, "VLookup.focusLost " + m_columnName, "added to combo - " + obj);
				m_combo.addItem(obj);
				m_combo.setSelectedItem(obj);
			}
		}
	//	actionCombo(getValue());
		m_settingFocus = false;
		m_haveFocus = false;    //  can gain focus again
	}	//	focusLost

	/**
	 *  Set ToolTip
	 *  @param text tool tip text
	 */
	public void setToolTipText(String text)
	{
		super.setToolTipText(text);
		m_button.setToolTipText(text);
		m_text.setToolTipText(text);
		m_combo.setToolTipText(text);
	}   //  setToolTipText

	/**
	 * 	Refresh Query
	 *	@return count
	 */
	public int refresh()
	{
		if (m_lookup == null)
			return -1;
		return m_lookup.refresh();
	}	//	refresh


}	//	VLookup

/*****************************************************************************
 *	Mouse Listener for Popup Menu
 */
final class VLookup_mouseAdapter extends java.awt.event.MouseAdapter
{
	/**
	 *	Constructor
	 *  @param adaptee adaptee
	 */
	VLookup_mouseAdapter(VLookup adaptee)
	{
		this.adaptee = adaptee;
	}	//	VLookup_mouseAdapter

	private VLookup adaptee;

	/**
	 *	Mouse Listener
	 *  @param e MouseEvent
	 */
	public void mouseClicked(MouseEvent e)
	{
	//	System.out.println("mouseClicked " + e.getID() + " " + e.getSource().getClass().toString());
		//	popup menu
		if (SwingUtilities.isRightMouseButton(e))
			adaptee.popupMenu.show((Component)e.getSource(), e.getX(), e.getY());
	}	//	mouse Clicked

}	//	VLookup_mouseAdapter
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
package org.compiere.grid.ed;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.text.*;
import java.util.*;
import java.math.*;
import java.beans.*;
import java.sql.*;

import org.compiere.apps.*;
import org.compiere.util.*;
import org.compiere.model.*;
import org.compiere.apps.search.*;
import org.compiere.plaf.*;
import org.compiere.swing.*;

/**
 *  Lookup Field.
 *  <p>
 *	    When r/o - display a Label
 *		When STABLE - display a ComboBox
 *		Otherwise show Selection Dialog
 *  <p>
 *  Sepecial handling of BPartner and Product
 *
 *  @author 	Jorg Janke
 *  @version 	$Id: VLookup.java,v 1.40 2003/02/23 19:42:47 jjanke Exp $
 */
public class VLookup extends JComponent
	implements VEditor, ActionListener, FocusListener
{
	/**
	 *  Create BPartner Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createBPartner (int WindowNo)
	{
		int AD_Column_ID = 3499;    //  C_Invoice.C_BPartner_ID
		try
		{
			Lookup lookup = MLookupFactory.create(Env.getCtx(), AD_Column_ID, WindowNo, DisplayType.Search, false);
			return new VLookup ("C_BPartner_ID", false, false, true, lookup, DisplayType.Search, WindowNo);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createBPartner", e);
		}
		return null;
	}   //  createBPartner

	/**
	 *  Create Product Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createProduct (int WindowNo)
	{
		int AD_Column_ID = 3840;    //  C_InvoiceLine.M_Product_ID
		try
		{
			Lookup lookup = MLookupFactory.create(Env.getCtx(), AD_Column_ID, WindowNo, DisplayType.Search, false);
			return new VLookup ("M_Product_ID", false, false, true, lookup, DisplayType.Search, WindowNo);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createProduct", e);
		}
		return null;
	}   //  createProduct

	/*************************************************************************/

	/**
	 *	IDE Default Constructor
	 */
	public VLookup()
	{
		this("Lookup", false, false, true, null, 0, 0);
	}	//	VLookup


	/**
	 *	Detail Constructor
	 *
	 *  @param columnName column
	 *  @param mandatory mandatory
	 *  @param isReadOnly read only
	 *  @param isUpdateable updateable
	 *  @param lookup lookup
	 *  @param displayType display type
	 *  @param WindowNo window no
	 */
	public VLookup (String columnName, boolean mandatory, boolean isReadOnly, boolean isUpdateable,
		Lookup lookup, int displayType, int WindowNo)
	{
		super();
		m_columnName = columnName;
		m_lookup = lookup;
		if (lookup == null)
			Log.trace(Log.l3_Util, "VLookup", "Lookup is NULL = " + columnName);
		setMandatory(mandatory);
		m_displayType = displayType;
		m_WindowNo = WindowNo;		//	for Info
		//
		setLayout(new BorderLayout());
		VLookup_mouseAdapter mouse = new VLookup_mouseAdapter(this);    //  popup

		//	***	Text & Button	***
		m_text.addActionListener(this);
		m_text.addMouseListener(mouse);
		//  Button
		m_button.addActionListener(this);
		m_button.addMouseListener(mouse);
		m_button.setFocusable(false);   //  don't focus when tabbing
		if (columnName.equals("C_BPartner_ID"))
			m_button.setIcon(Env.getImageIcon("BPartner10.gif"));
		else if (columnName.equals("M_Product_ID"))
			m_button.setIcon(Env.getImageIcon("Product10.gif"));
		else
			m_button.setIcon(Env.getImageIcon("PickOpen10.gif"));

		//	*** VComboBox	***
		if (m_lookup != null && displayType != DisplayType.Search)	//	No Search
		{
			//  Memory Leak after executing the next two lines ??
			m_lookup.fillComboBox (isMandatory(), false, false, false);
			m_combo.setModel(m_lookup);
			//
			m_combo.addActionListener(this);							//	Selection
			m_combo.addMouseListener(mouse);	                        //	popup
			//	FocusListener to refresh selection before opening
			if (!m_lookup.isValidated() || m_lookup.hasInactive())
				m_combo.addFocusListener(this);
		}

		setUI (true);
		//	ReadWrite	-	decides what components to show
		if (isReadOnly || !isUpdateable || m_lookup == null)
			setReadWrite(false);
		else
			setReadWrite(true);

		//	Popup
		if (m_lookup != null)
		{
			if ((displayType == DisplayType.List && Env.getContextAsInt(Env.getCtx(), "#AD_Role_ID") == 0)
				|| displayType != DisplayType.List)     //  only system admins can change lists, so no need to zoom for others
			{
				mZoom = new JMenuItem(Msg.getMsg(Env.getCtx(), "Zoom"), Env.getImageIcon("Zoom16.gif"));
				mZoom.addActionListener(this);
				popupMenu.add(mZoom);
			}
			mRefresh = new JMenuItem(Msg.getMsg(Env.getCtx(), "Refresh"), Env.getImageIcon("Refresh16.gif"));
			mRefresh.addActionListener(this);
			popupMenu.add(mRefresh);
		}
		//	VBPartner quick entry link
		if (columnName.equals("C_BPartner_ID"))
		{
			mBPartnerNew = new JMenuItem (Msg.getMsg(Env.getCtx(), "New"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerNew.addActionListener(this);
			popupMenu.add(mBPartnerNew);
			mBPartnerUpd = new JMenuItem (Msg.getMsg(Env.getCtx(), "Update"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerUpd.addActionListener(this);
			popupMenu.add(mBPartnerUpd);
		}
		//
		if (m_lookup != null && m_lookup.getZoom() == 0)
			mZoom.setEnabled(false);
	}	//	VLookup

	/**
	 *  Dispose
	 */
	public void dispose()
	{
		m_text = null;
		m_button = null;
		m_lookup = null;
		m_mField = null;
		m_combo.removeActionListener(this);
		m_combo.setModel(new DefaultComboBoxModel());    //  remove reference
	//	m_combo.removeAllItems();
		m_combo = null;
	}   //  dispose

	/** Display Length for Lookups (18)         */
	public final static int     DISPLAY_LENGTH = 18;

	/** Search: The Editable Text Field         */
	private CTextField 			m_text = new CTextField (DISPLAY_LENGTH);
	/** Search: The Button to open Editor   */
	private CButton				m_button = new CButton();
	/** The Combo Box if not a Search Lookup    */
	private VComboBox			m_combo = new VComboBox();
	/** Indicator that value is being set       */
	private volatile boolean 	m_settingValue = false;
	private volatile boolean 	m_settingFocus = false;
	/** Indicator that Lookup has focus         */
	private volatile boolean	m_haveFocus = false;
	/** Indicator - inserting new value			*/
	private volatile boolean	m_inserting = false;

	//
	private String				m_columnName;
	private Lookup				m_lookup;
	private int					m_displayType;
	private int					m_WindowNo;

	private boolean				m_comboActive = true;
	private Object				m_value;

	//	Popup
	JPopupMenu 					popupMenu = new JPopupMenu();
	private JMenuItem 			mZoom;
	private JMenuItem 			mRefresh;
	private JMenuItem			mBPartnerNew;
	private JMenuItem			mBPartnerUpd;

	private MField              m_mField = null;

	/**
	 *  Set Content and Size of Compoments
	 *  @param initial if true, size and margins will be set
	 */
	private void setUI (boolean initial)
	{
		if (initial)
		{
			Dimension size = m_text.getPreferredSize();
			setPreferredSize(new Dimension(size));  //	causes r/o to be the same length
			m_combo.setPreferredSize(new Dimension(size));
			//
			m_text.setBorder(null);
			Dimension bSize = new Dimension(size.height, size.height);
			m_button.setPreferredSize (bSize);
			m_button.setMargin(new Insets(0, 0, 0, 0));
		}

		//	What to show
		this.remove(m_combo);
		this.remove(m_button);
		this.remove(m_text);
		//
		if (!isReadWrite())									//	r/o - show text only
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			m_text.setReadWrite(false);
			m_combo.setReadWrite(false);
			m_comboActive = false;
		}
		else if (m_displayType != DisplayType.Search)	    //	show combo if not Search
		{
			this.setBorder(null);
			this.add(m_combo, BorderLayout.CENTER);
			m_comboActive = true;
		}
		else 												//	Search or unstable - show text & button
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			this.add(m_button, BorderLayout.EAST);
			m_text.setReadWrite (true);
			m_comboActive = false;
		}
	}   //  setUI

	/**
	 *	Set ReadWrite
	 *  @param value ReadWrite
	 */
	public void setReadWrite (boolean value)
	{
		boolean rw = value;
		if (m_lookup == null)
			rw = false;
		if (m_combo.isReadWrite() != value)
		{
			m_combo.setReadWrite(rw);
			setUI (false);
			if (m_comboActive)
				setValue (m_value);
		}
	}	//	setReadWrite

	/**
	 *	IsEditable
	 *  @return is lookup ReadWrite
	 */
	public boolean isReadWrite()
	{
		return m_combo.isReadWrite();
	}	//	isReadWrite

	/**
	 *	Set Mandatory (and back bolor)
	 *  @param mandatory mandatory
	 */
	public void setMandatory (boolean mandatory)
	{
		m_combo.setMandatory(mandatory);
		m_text.setMandatory(mandatory);
	}	//	setMandatory

	/**
	 *	Is it mandatory
	 *  @return true if mandatory
	 */
	public boolean isMandatory()
	{
		return m_combo.isMandatory();
	}	//	isMandatory

	/**
	 *	Set Background
	 *  @param color color
	 */
	public void setBackground(Color color)
	{
		m_text.setBackground(color);
		m_combo.setBackground(color);
	}	//	setBackground

	/**
	 *	Set Background
	 *  @param error error
	 */
	public void setBackground (boolean error)
	{
		m_text.setBackground(error);
		m_combo.setBackground(error);
	}	//	setBackground

	/**
	 *  Set Foreground
	 *  @param fg Foreground color
	 */
	public void setForeground(Color fg)
	{
		m_text.setForeground(fg);
		m_combo.setForeground(fg);
	}   //  setForeground

	/**
	 *  Set Editor to value
	 *  @param value new Value
	 */
	public void setValue (Object value)
	{
		Log.trace(Log.l6_Database, "VLookup.setValue", m_columnName + "=" + value);
		m_settingValue = true;		//	disable actions
		m_value = value;

		//	Set both for switching
		m_combo.setValue (value);
		if (value == null)
		{
			m_text.setText (null);
			m_settingValue = false;
			return;
		}
		if (m_lookup == null)
		{
			m_text.setText (value.toString());
			m_settingValue = false;
			return;
		}

		//	Set Display
		String display = m_lookup.getDisplay(value);
		boolean notFound = display.startsWith("<") && display.startsWith(">");
		m_text.setText (display);
		m_text.setCaretPosition (0); //	show beginning

		//	Nothing showing in Combo and should be showing
		if (m_combo.getSelectedItem() == null
			&& (m_comboActive || (m_inserting && m_displayType != DisplayType.Search)))
		{
			//  lookup found nothing too
			if (notFound)
			{
				Log.trace(8, "VLookup.setValue - Not found (1)", display);
				//  we may have a new value
				m_lookup.refresh();
				m_combo.setValue (value);
				display = m_lookup.getDisplay(value);
				m_text.setText (display);
				m_text.setCaretPosition (0);	//	show beginning
				notFound = display.startsWith("<") && display.endsWith(">");
			}
			if (notFound)	//	<key>
			{
				m_value = null;
				actionCombo (null);             //  data binding
				Log.trace(Log.l6_Database, "VLookup.setValue - not found - " + value);
			}
			//  we have lookup
			else if (m_combo.getSelectedItem() == null)
			{
				NamePair pp = m_lookup.get(value);
				if (pp != null)
				{
					Log.trace (Log.l6_Database, "VLookup.setValue - added to combo - " + pp);
					//  Add to Combo
					m_combo.addItem (pp);
					m_combo.setValue (value);
				}
			}
			//  Not in Lookup - set to Null
			if (m_combo.getSelectedItem() == null)
			{
				Log.trace(Log.l1_User, "VLookup.setValue - not in Lookup - set to NULL");
				actionCombo (null);             //  data binding (calls setValue again)
				m_value = null;
			}
		}
		m_settingValue = false;
	}	//	setValue

	/**
	 *  Property Change Listener
	 *  @param evt PropertyChangeEvent
	 */
	public void propertyChange (PropertyChangeEvent evt)
	{
	//	Log.trace(Log.l5_DData, "VLookup.propertyChange", evt);
		if (evt.getPropertyName().equals(MField.PROPERTY))
		{
			m_inserting = MField.INSERTING.equals(evt.getOldValue());
			setValue(evt.getNewValue());
			m_inserting = false;
		}
	}   //  propertyChange

	/**
	 *	Return Editor value (Integer)
	 *  @return value
	 */
	public Object getValue()
	{
		if (m_comboActive)
			return m_combo.getValue ();
		return m_value;
	}	//	getValue

	/**
	 *  Return editor display
	 *  @return display value
	 */
	public String getDisplay()
	{
		String retValue = null;
		if (m_comboActive)
			retValue = m_combo.getDisplay();
		//  check lookup
		else if (m_lookup == null)
			retValue = m_value.toString();
		else
			retValue = m_lookup.getDisplay(m_value);
	//	Log.trace(Log.l6_Database, "VLookup.getDisplay - " + retValue, "ComboActive=" + m_comboActive);
		return retValue;
	}   //  getDisplay

	/**
	 *  Set Field/WindowNo for ValuePreference
	 *  @param mField Model Field for Lookup
	 */
	public void setField (MField mField)
	{
		m_mField = mField;
		if (m_mField != null)
			ValuePreference.addMenu (this, popupMenu);
	}   //  setField

	/*************************************************************************/

	/**
	 *	Action Listener	- data binding
	 *  @param e ActionEvent
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (m_settingValue || m_settingFocus)
			return;
		Log.trace(Log.l4_Data, "VLookup.actionPerformed", e.getActionCommand() + ", ComboValue=" + m_combo.getSelectedItem());
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());

		//  Preference
		if (e.getActionCommand().equals(ValuePreference.NAME))
		{
			ValuePreference.start (m_mField, getValue(), getDisplay());
			return;
		}

		//  Combo Selection
		else if (e.getSource() == m_combo)
		{
			Object value = getValue();
			Object o = m_combo.getSelectedItem();
			if (o != null)
			{
				String s = o.toString();
				//  don't allow selection of inactive
				if (s.startsWith(MLookup.INACTIVE_S) && s.endsWith(MLookup.INACTIVE_E))
				{
					Log.trace(Log.l1_User, "VLookup.actionPerformed - Selection inactive set to NULL");
					value = null;
				}
			}
			actionCombo (value);                //  data binding
		}
		//  Button pressed
		else if (e.getSource() == m_button)
			actionButton ("");
		//  Text entered
		else if (e.getSource() == m_text)
			actionText();

		//  Popup Menu
		else if (e.getSource() == mZoom)
			actionZoom();
		else if (e.getSource() == mRefresh)
			actionRefresh();
		else if (e.getSource() == mBPartnerNew)
			actionBPartner(true);
		else if (e.getSource() == mBPartnerUpd)
			actionBPartner(false);
	}	//	actionPerformed

	/**
	 *  Action Listener Interface
	 *  @param listener listener
	 */
	public void addActionListener(ActionListener listener)
	{
		m_combo.addActionListener(listener);
		m_text.addActionListener(listener);
	}   //  addActionListener

	/**
	 *	Action - Combo.
	 *  <br>
	 *	== dataBinding == inform of new value
	 *  <pre>
	 *  VLookup.actionCombo
	 *      GridController.vetoableChange
	 *          MTable.setValueAt
	 *              MField.setValue
	 *                  VLookup.setValue
	 *          MTab.dataStatusChanged
	 *  </pre>
	 *  @param value new value
	 */
	private void actionCombo (Object value)
	{
	//	Log.trace(Log.l6_Database, "VLookup.actionCombo", value==null ? "null" : value.toString());
		try
		{
			fireVetoableChange (m_columnName, null, value);
		}
		catch (PropertyVetoException pve)
		{
			Log.error("VLookup.actionCombo", pve);
		}
		//  is the value updated ?
		boolean updated = false;
		if (value == null && m_value == null)
			updated = true;
		else if (value != null && value.equals(m_value))
			updated = true;
		if (!updated)
		{
			//  happens if VLookup is used outside of APanel/GridController (no property listener)
			Log.trace(Log.l6_Database, "VLookup.actionCombo - Value explicitly set - new=" + value + ", old=" + m_value);
			setValue(value);
		}
	}	//	actionCombo


	/**
	 *	Action - Button.
	 *	- Call Info
	 *	@param queryValue initial query value
	 */
	private void actionButton (String queryValue)
	{
		m_button.setEnabled(false);                 //  disable double click
		requestFocus();                             //  closes other editors
		Frame frame = Env.getFrame(this);

		/**
		 *  Three return options:
		 *  - Value Selected & OK pressed   => store result => result has value
		 *  - Cancel pressed                => store null   => result == null && cancelled
		 *  - Window closed                 -> ignore       => result == null && !cancalled
		 */
		Object result = null;
		boolean cancelled = false;
		//
		String col = m_lookup.getColumnName();		//	fully qualified name
		if (col.indexOf(".") != -1)
			col = col.substring(col.indexOf(".")+1);
		//  Zoom / Validation
		String whereClause = "";
		if (m_lookup.getZoomQuery() != null)
			whereClause = m_lookup.getZoomQuery().getWhereClause();
		if (whereClause.length() == 0)
			whereClause = m_lookup.getValidation();
		//
		Log.trace(Log.l5_DData, "VLookup.actionButton - " + col + ", Zoom=" + m_lookup.getZoom()
			+ " (" + whereClause + ")");
		//
		boolean resetValue = false;             //  reset value so that is always treated as new entry
		if (col.equals("M_Product_ID"))
		{
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = "@" + m_text.getText() + "@";   //  Name indicator - otherwise Value
			int M_Warehouse_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNo, "M_Warehouse_ID");
			int M_PriceList_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNo, "M_PriceList_ID");
			InfoProduct ip = new InfoProduct (frame, true, m_WindowNo,
				M_Warehouse_ID, M_PriceList_ID, queryValue, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
			resetValue = true;
		}
		else if (col.equals("C_BPartner_ID"))
		{
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = m_text.getText();
			boolean isSOTrx = true;     //  default
			if (Env.getContext(Env.getCtx(), m_WindowNo, "IsSOTrx").equals("N"))
				isSOTrx = false;
			InfoBPartner ip = new InfoBPartner (frame, true, m_WindowNo,
				queryValue, isSOTrx, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
		}
		else
		{
			String tableName = col;
			int index = m_columnName.indexOf("_ID");
			if (index != -1)
				tableName = m_columnName.substring(0, index);
			Info ig = Info.create (frame, true, m_WindowNo, tableName, m_columnName, queryValue, false, whereClause);
			ig.show();
			cancelled = ig.isCancelled();
			result = ig.getSelectedKey();
		}
		//  Result
		if (result != null)
		{
			Log.trace(Log.l4_Data, "VLookupactionButton", "Result = " + result.toString() + " " + result.getClass().getName());
			//  make sure that value is in cache
			m_lookup.getDirect(result, true);
			if (resetValue)
				actionCombo (null);
			actionCombo (result);
		}
		else if (cancelled)
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (cancelled)");
			actionCombo (null);
		}
		else
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (not cancelled)");
			setValue(m_value);      //  to re-display value
		}
		//
		m_button.setEnabled(true);
	}	//	actionButton

	/**
	 *	Check, if data returns unique entry, otherwise involve Info via Button
	 */
	private void actionText()
	{
		String text = m_text.getText();
		//	Nothing entered
		if (m_text.getText().length() == 0)
		{
			actionButton(text);
			return;
		}
		//	Always like
		if (!text.endsWith("%"))
			text += "%";

		StringBuffer SQL = new StringBuffer();
		if (m_columnName.equals("M_Product_ID"))
		{
			SQL.append("SELECT M_Product_ID FROM M_Product WHERE UPPER(Value) LIKE '")
				.append(text.toUpperCase()).append("'");
		}
		else if (m_columnName.equals("C_BPartner_ID"))
		{
			SQL.append("SELECT C_BPartner_ID FROM C_BPartner WHERE (UPPER(Value) LIKE '")
				.append(text.toUpperCase())
				.append("' OR UPPER(Name) LIKE '").append(text.toUpperCase()).append("')");
		}
		else if (m_columnName.equals("C_Order_ID"))
		{
			SQL.append("SELECT C_Order_ID FROM C_Order WHERE UPPER(DocumentNo) LIKE '")
				.append(text.toUpperCase()).append("'");
		}
		else if (m_columnName.equals("C_Invoice_ID"))
		{
			SQL.append("SELECT C_Invoice_ID FROM C_Invoice WHERE UPPER(DocumentNo) LIKE '")
				.append(text.toUpperCase()).append("'");
		}
		else if (m_columnName.equals("M_InOut_ID"))
		{
			SQL.append("SELECT M_InOut_ID FROM M_InOut WHERE UPPER(DocumentNo) LIKE '")
				.append(text.toUpperCase()).append("'");
		}
		else if (m_columnName.equals("C_Payment_ID"))
		{
			SQL.append("SELECT C_Payment_ID FROM C_Payment WHERE UPPER(DocumentNo) LIKE '")
				.append(text.toUpperCase()).append("'");
		}
		else
		{
			actionButton(text);
			return;
		}
		//	Finish SQL
		SQL.append(" AND IsActive='Y'");
		//	AddSecurity
		String tableName = m_columnName.substring(0, m_columnName.length()-3);
		String finalSQL = Access.addROAccessSQL(Env.getCtx(), SQL.toString(), tableName, false);
		int id = 0;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(finalSQL);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				id = rs.getInt(1);		//	first
				if (rs.next())
					id = -1;			//	only if unique
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("VLookup.actionText\nSQL=" + finalSQL, e);
			id = -2;
		}
		Log.trace(Log.l5_DData, "VLookup.actionText ID => " + id);

		//	No (unique) result
		if (id <= 0)
		{
			Log.trace(Log.l6_Database, finalSQL);
			actionButton(text);
			return;
		}
		m_value = null;     //  forces re-display if value is unchanged but text updated and still unique
		actionCombo (new Integer(id));          //  data binding
	}	//	actionText

	/**
	 *	Action - Special BPartner Screen
	 *  @param newRecord true if new record should be created
	 */
	private void actionBPartner (boolean newRecord)
	{
		VBPartner vbp = new VBPartner (Env.getFrame(this), m_WindowNo);
		int BPartner_ID = 0;
		//  if update, get current value
		if (!newRecord)
		{
			if (m_value instanceof Integer)
				BPartner_ID = ((Integer)m_value).intValue();
			else if (m_value != null)
				BPartner_ID = Integer.parseInt(m_value.toString());
		}

		vbp.loadBPartner (BPartner_ID);
		vbp.show();
		//  get result
		int result = vbp.getBPartner_ID();
		if (result == 0					//	0 = not saved
			&& result == BPartner_ID)	//	the same
			return;
		//  Maybe new BPartner - put in cache
		m_lookup.getDirect(new Integer(result), true);

		actionCombo (new Integer(result));      //  data binding
	}	//	actionBPartner

	/**
	 *	Action - Zoom
	 */
	private void actionZoom()
	{
		if (m_lookup == null)
			return;
		//
		int AD_Window_ID = m_lookup.getZoom();
		MQuery zoomQuery = m_lookup.getZoomQuery();
		String IsSOTrx = Env.getContext(Env.getCtx(), m_WindowNo, "IsSOTrx");
		Log.trace(Log.l1_User, "VLookup.actionZoom - " + AD_Window_ID + " - Query=" + zoomQuery + " - Value=" + getValue());
		//	If not already exist
		if (zoomQuery == null || (!zoomQuery.isActive() && getValue() != null))
		{
			zoomQuery = new MQuery();
			zoomQuery.addRestriction(m_columnName, MQuery.EQUAL, getValue());
		}
		//
		setCursor(Cursor.getDefaultCursor());
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		AWindow frame = new AWindow();
		if (!frame.initWindow(AD_Window_ID, zoomQuery, IsSOTrx.equals("Y")))
		{
			setCursor(Cursor.getDefaultCursor());
			ValueNamePair pp = Log.retrieveError();
			String msg = pp==null ? "AccessTableNoView" : pp.getValue();
			ADialog.error(m_WindowNo, this, msg, pp==null ? "" : pp.getName());
		}
		else
			AEnv.showCenterScreen(frame);
			//  async window - not able to get feedback
		frame = null;
		//
		setCursor(Cursor.getDefaultCursor());
	}	//	actionZoom

	/**
	 *	Action - Refresh
	 */
	private void actionRefresh()
	{
		if (m_lookup == null)
			return;
		//
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		Object obj = m_combo.getSelectedItem();
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + obj);
		m_lookup.refresh();
		if (m_lookup.isValidated())
			m_lookup.fillComboBox(isMandatory(), false, false, false);
		else
			m_lookup.fillComboBox(isMandatory(), true, false, false);
		m_combo.setSelectedItem(obj);
	//	m_combo.revalidate();
		//
		setCursor(Cursor.getDefaultCursor());
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + m_combo.getSelectedItem());
	}	//	actionRefresh

	/*************************************************************************/

	/**
	 *	Focus Listener for ComboBoxes with missing Validation or invalid entries
	 *	- Requery listener for updated list
	 *  @param e FocusEvent
	 */
	public void focusGained (FocusEvent e)
	{
		if (e.getSource() != m_combo || e.isTemporary() || m_haveFocus)
			return;
		//
		m_haveFocus = true;     //  prevents calling focus gained twice
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Object obj = m_lookup.getSelectedItem();
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Start    Count=" + m_combo.getItemCount() + ", Selected=" + obj);
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());
		m_lookup.fillComboBox(isMandatory(), true, true, true);     //  only validated & active & temporary
	//	Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Update   Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		m_lookup.setSelectedItem(obj);
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Selected Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		//
		m_settingFocus = false;
	}	//	focusGained

	/**
	 *	Reset Selection List
	 *  @param e FocusEvent
	 */
	public void focusLost(FocusEvent e)
	{
		if (e.getSource() != m_combo || e.isTemporary())
			return;
		//
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Log.trace(Log.l4_Data, "VLookup.focusLost", m_columnName + " = " + m_combo.getSelectedItem());
		Object obj = m_combo.getSelectedItem();
		//	set original model
		if (!m_lookup.isValidated())
			m_lookup.fillComboBox(true);    //  previous selection
		//	Set value
		if (obj != null)
		{
			m_combo.setSelectedItem(obj);
			//	original model may not have item
			if (!m_combo.getSelectedItem().equals(obj))
			{
				Log.trace(Log.l6_Database, "VLookup.focusLost " + m_columnName, "added to combo - " + obj);
				m_combo.addItem(obj);
				m_combo.setSelectedItem(obj);
			}
		}
	//	actionCombo(getValue());
		m_settingFocus = false;
		m_haveFocus = false;    //  can gain focus again
	}	//	focusLost

	/**
	 *  Set ToolTip
	 *  @param text tool tip text
	 */
	public void setToolTipText(String text)
	{
		super.setToolTipText(text);
		m_button.setToolTipText(text);
		m_text.setToolTipText(text);
		m_combo.setToolTipText(text);
	}   //  setToolTipText

}	//	VLookup

/*****************************************************************************/

/**
 *	Mouse Listener for Popup Menu
 */
final class VLookup_mouseAdapter extends java.awt.event.MouseAdapter
{
	/**
	 *	Constructor
	 *  @param adaptee adaptee
	 */
	VLookup_mouseAdapter(VLookup adaptee)
	{
		this.adaptee = adaptee;
	}	//	VLookup_mouseAdapter

	private VLookup adaptee;

	/**
	 *	Mouse Listener
	 *  @param e MouseEvent
	 */
	public void mouseClicked(MouseEvent e)
	{
	//	System.out.println("mouseClicked " + e.getID() + " " + e.getSource().getClass().toString());
		//	popup menu
		if (SwingUtilities.isRightMouseButton(e))
			adaptee.popupMenu.show((Component)e.getSource(), e.getX(), e.getY());
	}	//	mouse Clicked

}	//	VLookup_mouseAdapter
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
package org.compiere.grid.ed;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.sql.*;

import javax.swing.*;

import org.compiere.apps.*;
import org.compiere.apps.search.*;
import org.compiere.model.*;
import org.compiere.swing.*;
import org.compiere.util.*;

/**
 *  Lookup Field.
 *  <p>
 *	    When r/o - display a Label
 *		When STABLE - display a ComboBox
 *		Otherwise show Selection Dialog
 *  <p>
 *  Sepecial handling of BPartner and Product
 *
 *  @author 	Jorg Janke
 *  @version 	$Id: VLookup.java,v 1.68 2004/08/27 21:24:59 jjanke Exp $
 */
public class VLookup extends JComponent
	implements VEditor, ActionListener, FocusListener
{
	/**
	 *  Create Optional BPartner Search Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createBPartner (int WindowNo)
	{
		int AD_Column_ID = 3499;    //  C_Invoice.C_BPartner_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, 0, AD_Column_ID, DisplayType.Search);
			return new VLookup ("C_BPartner_ID", false, false, true, lookup);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createBPartner", e);
		}
		return null;
	}   //  createBPartner

	/**
	 *  Create Optional Product Search Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createProduct (int WindowNo)
	{
		int AD_Column_ID = 3840;    //  C_InvoiceLine.M_Product_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, 0, AD_Column_ID, DisplayType.Search);
			return new VLookup ("M_Product_ID", false, false, true, lookup);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createProduct", e);
		}
		return null;
	}   //  createProduct

	/**
	 *  Create Optional User Search Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createUser (int WindowNo)
	{
		int AD_Column_ID = 10443;    //  AD_WF_Activity.AD_User_UD
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, 0, AD_Column_ID, DisplayType.Search);
			return new VLookup ("AD_User_ID", false, false, true, lookup);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createUser", e);
		}
		return null;
	}   //  createProduct

	
	/*************************************************************************
	 *	Detail Constructor
	 *
	 *  @param columnName column
	 *  @param mandatory mandatory
	 *  @param isReadOnly read only
	 *  @param isUpdateable updateable
	 *  @param lookup lookup
	 */
	public VLookup (String columnName, boolean mandatory, boolean isReadOnly, boolean isUpdateable,
		Lookup lookup)
	{
		super();
		super.setName(columnName);
		m_combo.setName(columnName);
		m_columnName = columnName;
		setMandatory(mandatory);
		m_lookup = lookup;
		//
		setLayout(new BorderLayout());
		VLookup_mouseAdapter mouse = new VLookup_mouseAdapter(this);    //  popup

		//	***	Text & Button	***
		m_text.addActionListener(this);
		m_text.addFocusListener(this);
		m_text.addMouseListener(mouse);
		//  Button
		m_button.addActionListener(this);
		m_button.addMouseListener(mouse);
		m_button.setFocusable(false);   //  don't focus when tabbing
		m_button.setMargin(new Insets(0, 0, 0, 0));
		if (columnName.equals("C_BPartner_ID"))
			m_button.setIcon(Env.getImageIcon("BPartner10.gif"));
		else if (columnName.equals("M_Product_ID"))
			m_button.setIcon(Env.getImageIcon("Product10.gif"));
		else
			m_button.setIcon(Env.getImageIcon("PickOpen10.gif"));

		//	*** VComboBox	***
		if (m_lookup != null && m_lookup.getDisplayType() != DisplayType.Search)	//	No Search
		{
			//  Memory Leak after executing the next two lines ??
			m_lookup.fillComboBox (isMandatory(), false, false, false);
			m_combo.setModel(m_lookup);
			//
			m_combo.addActionListener(this);							//	Selection
			m_combo.addMouseListener(mouse);	                        //	popup
			//	FocusListener to refresh selection before opening
			m_combo.addFocusListener(this);
		}

		setUI (true);
		//	ReadWrite	-	decides what components to show
		if (isReadOnly || !isUpdateable || m_lookup == null)
			setReadWrite(false);
		else
			setReadWrite(true);

		//	Popup
		if (m_lookup != null)
		{
			if ((m_lookup.getDisplayType() == DisplayType.List && Env.getContextAsInt(Env.getCtx(), "#AD_Role_ID") == 0)
				|| m_lookup.getDisplayType() != DisplayType.List)     //  only system admins can change lists, so no need to zoom for others
			{
				mZoom = new JMenuItem(Msg.getMsg(Env.getCtx(), "Zoom"), Env.getImageIcon("Zoom16.gif"));
				mZoom.addActionListener(this);
				popupMenu.add(mZoom);
			}
			mRefresh = new JMenuItem(Msg.getMsg(Env.getCtx(), "Refresh"), Env.getImageIcon("Refresh16.gif"));
			mRefresh.addActionListener(this);
			popupMenu.add(mRefresh);
		}
		//	VBPartner quick entry link
		if (columnName.equals("C_BPartner_ID"))
		{
			mBPartnerNew = new JMenuItem (Msg.getMsg(Env.getCtx(), "New"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerNew.addActionListener(this);
			popupMenu.add(mBPartnerNew);
			mBPartnerUpd = new JMenuItem (Msg.getMsg(Env.getCtx(), "Update"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerUpd.addActionListener(this);
			popupMenu.add(mBPartnerUpd);
		}
		//
		if (m_lookup != null && m_lookup.getZoom() == 0)
			mZoom.setEnabled(false);
	}	//	VLookup

	/**
	 *  Dispose
	 */
	public void dispose()
	{
		m_text = null;
		m_button = null;
		m_lookup = null;
		m_mField = null;
		//
		m_combo.removeFocusListener(this);
		m_combo.removeActionListener(this);
		m_combo.setModel(new DefaultComboBoxModel());    //  remove reference
	//	m_combo.removeAllItems();
		m_combo = null;
	}   //  dispose

	/** Display Length for Lookups (15)         */
	public final static int     DISPLAY_LENGTH = 15;
	/** Field Height 				 */
	public static int     		FIELD_HIGHT = 0;

	/** Search: The Editable Text Field         */
	private CTextField 			m_text = new CTextField (DISPLAY_LENGTH);
	/** Search: The Button to open Editor   */
	private CButton				m_button = new CButton();
	/** The Combo Box if not a Search Lookup    */
	private VComboBox			m_combo = new VComboBox();
	/** Indicator that value is being set       */
	private volatile boolean 	m_settingValue = false;
	private volatile boolean 	m_settingFocus = false;
	/** Indicator that Lookup has focus         */
	private volatile boolean	m_haveFocus = false;
	/** Indicator - inserting new value			*/
	private volatile boolean	m_inserting = false;
	/** Last Display							*/
	private String				m_lastDisplay = "";

	//
	private String				m_columnName;
	private Lookup				m_lookup;

	private boolean				m_comboActive = true;
	private Object				m_value;

	//	Popup
	JPopupMenu 					popupMenu = new JPopupMenu();
	private JMenuItem 			mZoom;
	private JMenuItem 			mRefresh;
	private JMenuItem			mBPartnerNew;
	private JMenuItem			mBPartnerUpd;

	private MField              m_mField = null;

	/**
	 *  Set Content and Size of Compoments
	 *  @param initial if true, size and margins will be set
	 */
	private void setUI (boolean initial)
	{
		if (initial)
		{
			Dimension size = m_text.getPreferredSize();
			setPreferredSize(new Dimension(size));  //	causes r/o to be the same length
			m_combo.setPreferredSize(new Dimension(size));
			setMinimumSize(new Dimension (30, size.height));
			FIELD_HIGHT = size.height;
			//
			m_text.setBorder(null);
			Dimension bSize = new Dimension(size.height, size.height);
			m_button.setPreferredSize (bSize);
		}

		//	What to show
		this.remove(m_combo);
		this.remove(m_button);
		this.remove(m_text);
		//
		if (!isReadWrite())									//	r/o - show text only
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			m_text.setReadWrite(false);
			m_combo.setReadWrite(false);
			m_comboActive = false;
		}
		else if (m_lookup != null && m_lookup.getDisplayType() != DisplayType.Search)	    //	show combo if not Search
		{
			this.setBorder(null);
			this.add(m_combo, BorderLayout.CENTER);
			m_comboActive = true;
		}
		else 												//	Search or unstable - show text & button
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			this.add(m_button, BorderLayout.EAST);
			m_text.setReadWrite (true);
			m_comboActive = false;
		}
	}   //  setUI

	/**
	 *	Set ReadWrite
	 *  @param value ReadWrite
	 */
	public void setReadWrite (boolean value)
	{
		boolean rw = value;
		if (m_lookup == null)
			rw = false;
		if (m_combo.isReadWrite() != value)
		{
			m_combo.setReadWrite(rw);
			setUI (false);
			if (m_comboActive)
				setValue (m_value);
		}
	}	//	setReadWrite

	/**
	 *	IsEditable
	 *  @return is lookup ReadWrite
	 */
	public boolean isReadWrite()
	{
		return m_combo.isReadWrite();
	}	//	isReadWrite

	/**
	 *	Set Mandatory (and back bolor)
	 *  @param mandatory mandatory
	 */
	public void setMandatory (boolean mandatory)
	{
		m_combo.setMandatory(mandatory);
		m_text.setMandatory(mandatory);
	}	//	setMandatory

	/**
	 *	Is it mandatory
	 *  @return true if mandatory
	 */
	public boolean isMandatory()
	{
		return m_combo.isMandatory();
	}	//	isMandatory

	/**
	 *	Set Background
	 *  @param color color
	 */
	public void setBackground(Color color)
	{
		m_text.setBackground(color);
		m_combo.setBackground(color);
	}	//	setBackground

	/**
	 *	Set Background
	 *  @param error error
	 */
	public void setBackground (boolean error)
	{
		m_text.setBackground(error);
		m_combo.setBackground(error);
	}	//	setBackground

	/**
	 *  Set Foreground
	 *  @param fg Foreground color
	 */
	public void setForeground(Color fg)
	{
		m_text.setForeground(fg);
		m_combo.setForeground(fg);
	}   //  setForeground

	/**
	 *  Set Editor to value
	 *  @param value new Value
	 */
	public void setValue (Object value)
	{
		Log.trace(Log.l6_Database, "VLookup.setValue", m_columnName + "=" + value);
		m_settingValue = true;		//	disable actions
		m_value = value;

		//	Set both for switching
		m_combo.setValue (value);
		if (value == null)
		{
			m_text.setText (null);
			m_lastDisplay = "";
			m_settingValue = false;
			return;
		}
		if (m_lookup == null)
		{
			m_text.setText (value.toString());
			m_lastDisplay = value.toString();
			m_settingValue = false;
			return;
		}

		//	Set Display
		m_lastDisplay = m_lookup.getDisplay(value);
		if (m_lastDisplay.equals("<-1>"))
		{
			m_lastDisplay = "";
			m_value = null;
		}
		boolean notFound = m_lastDisplay.startsWith("<") && m_lastDisplay.startsWith(">");
		m_text.setText (m_lastDisplay);
		m_text.setCaretPosition (0); //	show beginning

		//	Nothing showing in Combo and should be showing
		if (m_combo.getSelectedItem() == null
			&& (m_comboActive || (m_inserting && m_lookup.getDisplayType() != DisplayType.Search)))
		{
			//  lookup found nothing too
			if (notFound)
			{
				Log.trace(8, "VLookup.setValue - Not found (1)", m_lastDisplay);
				//  we may have a new value
				m_lookup.refresh();
				m_combo.setValue (value);
				m_lastDisplay = m_lookup.getDisplay(value);
				m_text.setText (m_lastDisplay);
				m_text.setCaretPosition (0);	//	show beginning
				notFound = m_lastDisplay.startsWith("<") && m_lastDisplay.endsWith(">");
			}
			if (notFound)	//	<key>
			{
				m_value = null;
				actionCombo (null);             //  data binding
				Log.trace(Log.l6_Database, "VLookup.setValue - not found - " + value);
			}
			//  we have lookup
			else if (m_combo.getSelectedItem() == null)
			{
				NamePair pp = m_lookup.get(value);
				if (pp != null)
				{
					Log.trace (Log.l6_Database, "VLookup.setValue - added to combo - " + pp);
					//  Add to Combo
					m_combo.addItem (pp);
					m_combo.setValue (value);
				}
			}
			//  Not in Lookup - set to Null
			if (m_combo.getSelectedItem() == null)
			{
				Log.trace(Log.l1_User, "VLookup.setValue - not in Lookup - set to NULL");
				actionCombo (null);             //  data binding (calls setValue again)
				m_value = null;
			}
		}
		m_settingValue = false;
	}	//	setValue

	/**
	 *  Property Change Listener
	 *  @param evt PropertyChangeEvent
	 */
	public void propertyChange (PropertyChangeEvent evt)
	{
	//	Log.trace(Log.l5_DData, "VLookup.propertyChange", evt);
		if (evt.getPropertyName().equals(MField.PROPERTY))
		{
			m_inserting = MField.INSERTING.equals(evt.getOldValue());	//	MField.setValue
			setValue(evt.getNewValue());
			m_inserting = false;
		}
	}   //  propertyChange

	/**
	 *	Return Editor value (Integer)
	 *  @return value
	 */
	public Object getValue()
	{
		if (m_comboActive)
			return m_combo.getValue ();
		return m_value;
	}	//	getValue

	/**
	 *  Return editor display
	 *  @return display value
	 */
	public String getDisplay()
	{
		String retValue = null;
		if (m_comboActive)
			retValue = m_combo.getDisplay();
		//  check lookup
		else if (m_lookup == null)
			retValue = m_value.toString();
		else
			retValue = m_lookup.getDisplay(m_value);
	//	Log.trace(Log.l6_Database, "VLookup.getDisplay - " + retValue, "ComboActive=" + m_comboActive);
		return retValue;
	}   //  getDisplay

	/**
	 *  Set Field/WindowNo for ValuePreference
	 *  @param mField Model Field for Lookup
	 */
	public void setField (MField mField)
	{
		m_mField = mField;
		if (m_mField != null)
			ValuePreference.addMenu (this, popupMenu);
	}   //  setField

	
	/**************************************************************************
	 *	Action Listener	- data binding
	 *  @param e ActionEvent
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (m_settingValue || m_settingFocus)
			return;
		Log.trace(Log.l4_Data, "VLookup.actionPerformed", e.getActionCommand() + ", ComboValue=" + m_combo.getSelectedItem());
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());

		//  Preference
		if (e.getActionCommand().equals(ValuePreference.NAME))
		{
			ValuePreference.start (m_mField, getValue(), getDisplay());
			return;
		}

		//  Combo Selection
		else if (e.getSource() == m_combo)
		{
			Object value = getValue();
			Object o = m_combo.getSelectedItem();
			if (o != null)
			{
				String s = o.toString();
				//  don't allow selection of inactive
				if (s.startsWith(MLookup.INACTIVE_S) && s.endsWith(MLookup.INACTIVE_E))
				{
					Log.trace(Log.l1_User, "VLookup.actionPerformed - Selection inactive set to NULL");
					value = null;
				}
			}
			actionCombo (value);                //  data binding
		}
		//  Button pressed
		else if (e.getSource() == m_button)
			actionButton ("");
		//  Text entered
		else if (e.getSource() == m_text)
			actionText();

		//  Popup Menu
		else if (e.getSource() == mZoom)
			actionZoom(m_combo.getSelectedItem());
		else if (e.getSource() == mRefresh)
			actionRefresh();
		else if (e.getSource() == mBPartnerNew)
			actionBPartner(true);
		else if (e.getSource() == mBPartnerUpd)
			actionBPartner(false);
	}	//	actionPerformed

	/**
	 *  Action Listener Interface
	 *  @param listener listener
	 */
	public void addActionListener(ActionListener listener)
	{
		m_combo.addActionListener(listener);
		m_text.addActionListener(listener);
	}   //  addActionListener

	/**
	 *	Action - Combo.
	 *  <br>
	 *	== dataBinding == inform of new value
	 *  <pre>
	 *  VLookup.actionCombo
	 *      GridController.vetoableChange
	 *          MTable.setValueAt
	 *              MField.setValue
	 *                  VLookup.setValue
	 *          MTab.dataStatusChanged
	 *  </pre>
	 *  @param value new value
	 */
	private void actionCombo (Object value)
	{
		Log.trace(Log.l6_Database, "VLookup.actionCombo", value==null ? "null" : value.toString());
		try
		{
			fireVetoableChange (m_columnName, null, value);
		}
		catch (PropertyVetoException pve)
		{
			Log.error("VLookup.actionCombo", pve);
		}
		//  is the value updated ?
		boolean updated = false;
		if (value == null && m_value == null)
			updated = true;
		else if (value != null && value.equals(m_value))
			updated = true;
		if (!updated)
		{
			//  happens if VLookup is used outside of APanel/GridController (no property listener)
			Log.trace(Log.l6_Database, "VLookup.actionCombo",
				"Value explicitly set - new=" + value + ", old=" + m_value);
			setValue(value);
		}
	}	//	actionCombo


	/**
	 *	Action - Button.
	 *	- Call Info
	 *	@param queryValue initial query value
	 */
	private void actionButton (String queryValue)
	{
		m_button.setEnabled(false);                 //  disable double click
		if (m_lookup == null)
			return;		//	leave button disabled
		this.requestFocus();						//  closes other editors
		Frame frame = Env.getFrame(this);

		/**
		 *  Three return options:
		 *  - Value Selected & OK pressed   => store result => result has value
		 *  - Cancel pressed                => store null   => result == null && cancelled
		 *  - Window closed                 -> ignore       => result == null && !cancalled
		 */
		Object result = null;
		boolean cancelled = false;
		//
		String col = m_lookup.getColumnName();		//	fully qualified name
		if (col.indexOf(".") != -1)
			col = col.substring(col.indexOf(".")+1);
		//  Zoom / Validation
		String whereClause = getWhereClause();
		//
		Log.trace(Log.l5_DData, "VLookup.actionButton - " + col 
			+ ", Zoom=" + m_lookup.getZoom()
			+ " (" + whereClause + ")");
		//
		boolean resetValue = false;	//	reset value so that is always treated as new entry    
		if (col.equals("M_Product_ID"))
		{
			//	Reset
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_Product_ID", "0");
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_AttributeSetInstance_ID", "0");
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = "@" + m_text.getText() + "@";   //  Name indicator - otherwise Value
			int M_Warehouse_ID = Env.getContextAsInt(Env.getCtx(), m_lookup.getWindowNo(), "M_Warehouse_ID");
			int M_PriceList_ID = Env.getContextAsInt(Env.getCtx(), m_lookup.getWindowNo(), "M_PriceList_ID");
			//	Show Info
			InfoProduct ip = new InfoProduct (frame, true, m_lookup.getWindowNo(),
				M_Warehouse_ID, M_PriceList_ID, queryValue, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
			resetValue = true;
		}
		else if (col.equals("C_BPartner_ID"))
		{
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = m_text.getText();
			boolean isSOTrx = true;     //  default
			if (Env.getContext(Env.getCtx(), m_lookup.getWindowNo(), "IsSOTrx").equals("N"))
				isSOTrx = false;
			InfoBPartner ip = new InfoBPartner (frame, true, m_lookup.getWindowNo(),
				queryValue, isSOTrx, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
		}
		else	//	General Info
		{
			if (m_tableName == null)	//	sets table name & key column
				getDirectAccessSQL("*");
			Info ig = Info.create (frame, true, m_lookup.getWindowNo(), 
				m_tableName, m_keyColumnName, queryValue, false, whereClause);
			ig.show();
			cancelled = ig.isCancelled();
			result = ig.getSelectedKey();
		}
		
		//  Result
		if (result != null)
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = " + result.toString() + " (" + result.getClass().getName() + ")");
			//  make sure that value is in cache
			m_lookup.getDirect(result, false, true);
			if (resetValue)
				actionCombo (null);
			actionCombo (result);
		}
		else if (cancelled)
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (cancelled)");
			actionCombo (null);
		}
		else
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (not cancelled)");
			setValue(m_value);      //  to re-display value
		}
		//
		m_button.setEnabled(true);
		m_text.requestFocus();
	}	//	actionButton

	/**
	 * 	Get Where Clause
	 *	@return where clause or ""
	 */
	private String getWhereClause()
	{
		String whereClause = "";
		if (m_lookup == null)
			return "";
		if (m_lookup.getZoomQuery() != null)
			whereClause = m_lookup.getZoomQuery().getWhereClause();
		if (whereClause.length() == 0)
			whereClause = m_lookup.getValidation();
	//	Log.trace(9, "VLookup.getWhereClause - ZoomQuery=" 
	//		+ (m_lookup.getZoomQuery()==null ? "" : m_lookup.getZoomQuery().getWhereClause())
	//		+ ", Validation=" + m_lookup.getValidation());
		return whereClause;
	}	//	getWhereClause

	/**
	 *	Check, if data returns unique entry, otherwise involve Info via Button
	 */
	private void actionText()
	{
		String text = m_text.getText();
		//	Nothing entered
		if (text == null || text.length() == 0 || text.equals("%"))
		{
			actionButton(text);
			return;
		}
		//	Always like
		if (!text.endsWith("%"))
			text += "%";
		text = text.toUpperCase();
		Log.trace(Log.l4_Data, "VLookup.actionText", m_columnName + " - " + text);

		String finalSQL = Msg.parseTranslation(Env.getCtx(), getDirectAccessSQL(text));
		int id = 0;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(finalSQL);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				id = rs.getInt(1);		//	first
				if (rs.next())
					id = -1;			//	only if unique
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e)
		{
			Log.error("VLookup.actionText\nSQL=" + finalSQL, e);
			id = -2;
		}

		//	No (unique) result
		if (id <= 0)
		{
			if (id == 0)
				Log.trace(Log.l6_Database, "VLookup.actionText - Not Found - " + finalSQL);
			else
				Log.trace(Log.l6_Database, "VLookup.actionText - Not Unique - " + finalSQL);
			m_value = null;	// force re-display
			actionButton(m_text.getText());
			return;
		}
		Log.trace(Log.l5_DData, "VLookup.actionText - Unique ID => " + id);
		m_value = null;     //  forces re-display if value is unchanged but text updated and still unique
		actionCombo (new Integer(id));          //  data binding
	}	//	actionText


	private String		m_tableName = null;
	private String		m_keyColumnName = null;

	/**
	 * 	Generate Access SQL for Search.
	 * 	The SQL returns the ID of the value entered
	 * 	Also sets m_tableName and m_keyColumnName
	 *	@param text uppercase text for LIKE comparison
	 *	@return sql or ""
	 *  Example
	 *	SELECT C_Payment_ID FROM C_Payment WHERE UPPER(DocumentNo) LIKE x OR ...
	 */
	private String getDirectAccessSQL (String text)
	{
		StringBuffer sql = new StringBuffer();
		m_tableName = m_columnName.substring(0, m_columnName.length()-3);
		m_keyColumnName = m_columnName;
		//
		if (m_columnName.equals("M_Product_ID"))
		{
			//	Reset
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_Product_ID", "0");
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_AttributeSetInstance_ID", "0");
			//
			sql.append("SELECT M_Product_ID FROM M_Product WHERE (UPPER(Value) LIKE ")
				.append(DB.TO_STRING(text))
				.append(" OR UPC LIKE ").append(DB.TO_STRING(text)).append(")");
		}
		else if (m_columnName.equals("C_BPartner_ID"))
		{
			sql.append("SELECT C_BPartner_ID FROM C_BPartner WHERE (UPPER(Value) LIKE ")
				.append(DB.TO_STRING(text))
				.append(" OR UPPER(Name) LIKE ").append(DB.TO_STRING(text)).append(")");
		}
		else if (m_columnName.equals("C_Order_ID"))
		{
			sql.append("SELECT C_Order_ID FROM C_Order WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_Invoice_ID"))
		{
			sql.append("SELECT C_Invoice_ID FROM C_Invoice WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("M_InOut_ID"))
		{
			sql.append("SELECT M_InOut_ID FROM M_InOut WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_Payment_ID"))
		{
			sql.append("SELECT C_Payment_ID FROM C_Payment WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("GL_JournalBatch_ID"))
		{
			sql.append("SELECT GL_JournalBatch_ID FROM GL_JournalBatch WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("SalesRep_ID"))
		{
			sql.append("SELECT AD_User_ID FROM AD_User WHERE UPPER(Name) LIKE ")
				.append(DB.TO_STRING(text));
			m_tableName = "AD_User";
			m_keyColumnName = "AD_User_ID";
		}
		//	Predefined
		if (sql.length() > 0)
		{
			String wc = getWhereClause();
			if (wc != null && wc.length() > 0)
				sql.append(" AND ").append(wc);
			sql.append(" AND IsActive='Y'");
			//	***
			Log.trace(9, "VLookup.getDirectAccessSQL (predefined) " + sql.toString());
			return MRole.getDefault().addAccessSQL(sql.toString(),
				m_tableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
		}
		
		//	Check if it is a Table Reference
		if (m_lookup != null && m_lookup instanceof MLookup)
		{
			int AD_Reference_ID = ((MLookup)m_lookup).getAD_Reference_Value_ID();
			if (AD_Reference_ID != 0)
			{
				String query = "SELECT kc.ColumnName, dc.ColumnName, t.TableName "
					+ "FROM AD_Ref_Table rt"
					+ " INNER JOIN AD_Column kc ON (rt.AD_Key=kc.AD_Column_ID)"
					+ " INNER JOIN AD_Column dc ON (rt.AD_Display=dc.AD_Column_ID)"
					+ " INNER JOIN AD_Table t ON (rt.AD_Table_ID=t.AD_Table_ID) "
					+ "WHERE rt.AD_Reference_ID=?";
				String displayColumnName = null;
				PreparedStatement pstmt = null;
				try
				{
					pstmt = DB.prepareStatement(query);
					pstmt.setInt(1, AD_Reference_ID);
					ResultSet rs = pstmt.executeQuery();
					if (rs.next())
					{
						m_keyColumnName = rs.getString(1);
						displayColumnName = rs.getString(2);
						m_tableName = rs.getString(3);
					}
					rs.close();
					pstmt.close();
					pstmt = null;
				}
				catch (Exception e)
				{
					Log.error("VLookup.getDirectAccessSQL", e);
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
				if (displayColumnName != null)
				{
					sql = new StringBuffer();
					sql.append("SELECT ").append(m_keyColumnName)
						.append(" FROM ").append(m_tableName)
						.append(" WHERE UPPER(").append(displayColumnName)
						.append(") LIKE ").append(DB.TO_STRING(text))
						.append(" AND IsActive='Y'");
					String wc = getWhereClause();
					if (wc != null && wc.length() > 0)
						sql.append(" AND ").append(wc);
					//	***
					Log.trace(9, "VLookup.getDirectAccessSQL (Table) " + sql.toString());
					return MRole.getDefault().addAccessSQL(sql.toString(),
								m_tableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
				}
			}	//	Table Reference
		}	//	MLookup
		
		/** Check Well Known Columns of Table - assumes TableDir	**/
		String query = "SELECT t.TableName, c.ColumnName "
			+ "FROM AD_Column c "
			+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID AND t.IsView='N') "
			+ "WHERE (c.ColumnName IN ('DocumentNo', 'Value', 'Name') OR c.IsIdentifier='Y')"
			+ " AND c.AD_Reference_ID IN (10,14)"
			+ " AND EXISTS (SELECT * FROM AD_Column cc WHERE cc.AD_Table_ID=t.AD_Table_ID"
				+ " AND cc.IsKey='Y' AND cc.ColumnName=?)";
		m_keyColumnName = m_columnName;
		sql = new StringBuffer();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(query);
			pstmt.setString(1, m_keyColumnName);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				if (sql.length() != 0)
					sql.append(" OR ");
				m_tableName = rs.getString(1);
				sql.append("UPPER(").append(rs.getString(2)).append(") LIKE ").append(DB.TO_STRING(text));
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException ex)
		{
			Log.error("VLookup.getDirectAccessSQL", ex);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
		}
		catch (SQLException ex1)
		{
		}
		pstmt = null;
		//
		if (sql.length() == 0)
		{
			Log.error("VLookup.getDirectAccessSQL (TableDir) - no standard/identifier columns");
			return "";
		}
		//
		StringBuffer retValue = new StringBuffer ("SELECT ")
			.append(m_columnName).append(" FROM ").append(m_tableName)
			.append(" WHERE ").append(sql)
			.append(" AND IsActive='Y'");
		String wc = getWhereClause();
		if (wc != null && wc.length() > 0)
			retValue.append(" AND ").append(wc);
		//	***
		Log.trace(9, "VLookup.getDirectAccessSQL (TableDir) " + sql.toString());
		return MRole.getDefault().addAccessSQL(retValue.toString(),
					m_tableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
	}	//	getDirectAccessSQL


	/**
	 *	Action - Special BPartner Screen
	 *  @param newRecord true if new record should be created
	 */
	private void actionBPartner (boolean newRecord)
	{
		VBPartner vbp = new VBPartner (Env.getFrame(this), m_lookup.getWindowNo());
		int BPartner_ID = 0;
		//  if update, get current value
		if (!newRecord)
		{
			if (m_value instanceof Integer)
				BPartner_ID = ((Integer)m_value).intValue();
			else if (m_value != null)
				BPartner_ID = Integer.parseInt(m_value.toString());
		}

		vbp.loadBPartner (BPartner_ID);
		vbp.show();
		//  get result
		int result = vbp.getC_BPartner_ID();
		if (result == 0					//	0 = not saved
			&& result == BPartner_ID)	//	the same
			return;
		//  Maybe new BPartner - put in cache
		m_lookup.getDirect(new Integer(result), false, true);

		actionCombo (new Integer(result));      //  data binding
	}	//	actionBPartner

	/**
	 *	Action - Zoom
	 *	@parem selected item
	 */
	private void actionZoom (Object selectedItem)
	{
		if (m_lookup == null)
			return;
		//
		String IsSOTrx = Env.getContext(Env.getCtx(), m_lookup.getWindowNo(), "IsSOTrx");
		int AD_Window_ID = m_lookup.getZoom(IsSOTrx);
		MQuery zoomQuery = m_lookup.getZoomQuery();
		Object value = getValue();
		if (value == null)
			value = selectedItem;
		Log.trace(Log.l1_User, "VLookup.actionZoom - " + AD_Window_ID 
			+ " - IsSOTrx=" + IsSOTrx 
			+ " - Query=" + zoomQuery + " - Value=" + value);
		//	If not already exist or exact value
		if (zoomQuery == null || value != null)
		{
			zoomQuery = new MQuery();	//	ColumnName might be changed in MTab.validateQuery
			zoomQuery.addRestriction(m_columnName, MQuery.EQUAL, value);
			Log.trace(8, "VLookup.actionZoom - Query = " + zoomQuery); 
		}
		//
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		AWindow frame = new AWindow();
		if (!frame.initWindow(AD_Window_ID, zoomQuery, IsSOTrx.equals("Y")))
		{
			setCursor(Cursor.getDefaultCursor());
			ValueNamePair pp = Log.retrieveError();
			String msg = pp==null ? "AccessTableNoView" : pp.getValue();
			ADialog.error(m_lookup.getWindowNo(), this, msg, pp==null ? "" : pp.getName());
		}
		else
			AEnv.showCenterScreen(frame);
			//  async window - not able to get feedback
		frame = null;
		//
		setCursor(Cursor.getDefaultCursor());
	}	//	actionZoom

	/**
	 *	Action - Refresh
	 */
	private void actionRefresh()
	{
		if (m_lookup == null)
			return;
		//
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		Object obj = m_combo.getSelectedItem();
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + obj);
		m_lookup.refresh();
		if (m_lookup.isValidated())
			m_lookup.fillComboBox(isMandatory(), false, false, false);
		else
			m_lookup.fillComboBox(isMandatory(), true, false, false);
		m_combo.setSelectedItem(obj);
	//	m_combo.revalidate();
		//
		setCursor(Cursor.getDefaultCursor());
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + m_combo.getSelectedItem());
	}	//	actionRefresh

	
	/**************************************************************************
	 *	Focus Listener for ComboBoxes with missing Validation or invalid entries
	 *	- Requery listener for updated list
	 *  @param e FocusEvent
	 */
	public void focusGained (FocusEvent e)
	{
		if (e.getSource() != m_combo || e.isTemporary() || m_haveFocus || m_lookup == null)
			return;
		if (m_lookup.isValidated() && !m_lookup.hasInactive())
			return;
		//
		m_haveFocus = true;     //  prevents calling focus gained twice
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Object obj = m_lookup.getSelectedItem();
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName 
			+ " - Start    Count=" + m_combo.getItemCount() + ", Selected=" + obj);
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());
		m_lookup.fillComboBox(isMandatory(), true, true, true);     //  only validated & active & temporary
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName 
			+ " - Update   Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		m_lookup.setSelectedItem(obj);
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName 
			+ " - Selected Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		//
		m_settingFocus = false;
	}	//	focusGained

	/**
	 *	Reset Selection List
	 *  @param e FocusEvent
	 */
	public void focusLost(FocusEvent e)
	{
		if (e.isTemporary() || m_lookup == null 
			|| !m_button.isEnabled() )
			return;
		if (e.getSource() == m_text)
		{
			String text = m_text.getText();
			Log.trace(Log.l4_Data, "VLookup.focusLost Text", 
				m_columnName + " = " + m_value + " - " + text);
			//	Skip if empty
			if ((m_value == null && m_text.getText().length() == 0))
				return;
			if (m_lastDisplay.equals(text))
				return;
			//
			actionText();
			m_haveFocus = false;
			return;
		}
		//	Combo lost focus
		if (e.getSource() != m_combo)
			return;
		if (m_lookup.isValidated() && !m_lookup.hasInactive())
			return;
		//
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Log.trace(Log.l4_Data, "VLookup.focusLost", m_columnName + " = " + m_combo.getSelectedItem());
		Object obj = m_combo.getSelectedItem();
		//	set original model
		if (!m_lookup.isValidated())
			m_lookup.fillComboBox(true);    //  previous selection
		//	Set value
		if (obj != null)
		{
			m_combo.setSelectedItem(obj);
			//	original model may not have item
			if (!m_combo.getSelectedItem().equals(obj))
			{
				Log.trace(Log.l6_Database, "VLookup.focusLost " + m_columnName, "added to combo - " + obj);
				m_combo.addItem(obj);
				m_combo.setSelectedItem(obj);
			}
		}
	//	actionCombo(getValue());
		m_settingFocus = false;
		m_haveFocus = false;    //  can gain focus again
	}	//	focusLost

	/**
	 *  Set ToolTip
	 *  @param text tool tip text
	 */
	public void setToolTipText(String text)
	{
		super.setToolTipText(text);
		m_button.setToolTipText(text);
		m_text.setToolTipText(text);
		m_combo.setToolTipText(text);
	}   //  setToolTipText

	/**
	 * 	Refresh Query
	 *	@return count
	 */
	public int refresh()
	{
		if (m_lookup == null)
			return -1;
		return m_lookup.refresh();
	}	//	refresh


}	//	VLookup

/*****************************************************************************
 *	Mouse Listener for Popup Menu
 */
final class VLookup_mouseAdapter extends java.awt.event.MouseAdapter
{
	/**
	 *	Constructor
	 *  @param adaptee adaptee
	 */
	VLookup_mouseAdapter(VLookup adaptee)
	{
		this.adaptee = adaptee;
	}	//	VLookup_mouseAdapter

	private VLookup adaptee;

	/**
	 *	Mouse Listener
	 *  @param e MouseEvent
	 */
	public void mouseClicked(MouseEvent e)
	{
	//	System.out.println("mouseClicked " + e.getID() + " " + e.getSource().getClass().toString());
		//	popup menu
		if (SwingUtilities.isRightMouseButton(e))
			adaptee.popupMenu.show((Component)e.getSource(), e.getX(), e.getY());
	}	//	mouse Clicked

}	//	VLookup_mouseAdapter
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
package org.compiere.grid.ed;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.text.*;
import java.util.*;
import java.math.*;
import java.beans.*;
import java.sql.*;

import org.compiere.apps.*;
import org.compiere.util.*;
import org.compiere.model.*;
import org.compiere.apps.search.*;
import org.compiere.plaf.*;
import org.compiere.swing.*;

/**
 *  Lookup Field.
 *  <p>
 *	    When r/o - display a Label
 *		When STABLE - display a ComboBox
 *		Otherwise show Selection Dialog
 *  <p>
 *  Sepecial handling of BPartner and Product
 *
 *  @author 	Jorg Janke
 *  @version 	$Id: VLookup.java,v 1.49 2003/11/06 07:08:28 jjanke Exp $
 */
public class VLookup extends JComponent
	implements VEditor, ActionListener, FocusListener
{
	/**
	 *  Create BPartner Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createBPartner (int WindowNo)
	{
		int AD_Column_ID = 3499;    //  C_Invoice.C_BPartner_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, 0, AD_Column_ID, DisplayType.Search);
			return new VLookup ("C_BPartner_ID", false, false, true, lookup, DisplayType.Search, WindowNo);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createBPartner", e);
		}
		return null;
	}   //  createBPartner

	/**
	 *  Create Product Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createProduct (int WindowNo)
	{
		int AD_Column_ID = 3840;    //  C_InvoiceLine.M_Product_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, 0, AD_Column_ID, DisplayType.Search);
			return new VLookup ("M_Product_ID", false, false, true, lookup, DisplayType.Search, WindowNo);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createProduct", e);
		}
		return null;
	}   //  createProduct

	/*************************************************************************/

	/**
	 *	IDE Default Constructor
	 */
	public VLookup()
	{
		this("Lookup", false, false, true, null, 0, 0);
	}	//	VLookup


	/**
	 *	Detail Constructor
	 *
	 *  @param columnName column
	 *  @param mandatory mandatory
	 *  @param isReadOnly read only
	 *  @param isUpdateable updateable
	 *  @param lookup lookup
	 *  @param displayType display type
	 *  @param WindowNo window no
	 */
	public VLookup (String columnName, boolean mandatory, boolean isReadOnly, boolean isUpdateable,
		Lookup lookup, int displayType, int WindowNo)
	{
		super();
		super.setName(columnName);
		m_combo.setName(columnName);
		m_columnName = columnName;
		m_lookup = lookup;
		if (lookup == null)
			Log.trace(Log.l3_Util, "VLookup", "Lookup is NULL = " + columnName);
		setMandatory(mandatory);
		m_displayType = displayType;
		m_WindowNo = WindowNo;		//	for Info
		//
		setLayout(new BorderLayout());
		VLookup_mouseAdapter mouse = new VLookup_mouseAdapter(this);    //  popup

		//	***	Text & Button	***
		m_text.addActionListener(this);
		m_text.addMouseListener(mouse);
		//  Button
		m_button.addActionListener(this);
		m_button.addMouseListener(mouse);
		m_button.setFocusable(false);   //  don't focus when tabbing
		if (columnName.equals("C_BPartner_ID"))
			m_button.setIcon(Env.getImageIcon("BPartner10.gif"));
		else if (columnName.equals("M_Product_ID"))
			m_button.setIcon(Env.getImageIcon("Product10.gif"));
		else
			m_button.setIcon(Env.getImageIcon("PickOpen10.gif"));

		//	*** VComboBox	***
		if (m_lookup != null && displayType != DisplayType.Search)	//	No Search
		{
			//  Memory Leak after executing the next two lines ??
			m_lookup.fillComboBox (isMandatory(), false, false, false);
			m_combo.setModel(m_lookup);
			//
			m_combo.addActionListener(this);							//	Selection
			m_combo.addMouseListener(mouse);	                        //	popup
			//	FocusListener to refresh selection before opening
			m_combo.addFocusListener(this);
		}

		setUI (true);
		//	ReadWrite	-	decides what components to show
		if (isReadOnly || !isUpdateable || m_lookup == null)
			setReadWrite(false);
		else
			setReadWrite(true);

		//	Popup
		if (m_lookup != null)
		{
			if ((displayType == DisplayType.List && Env.getContextAsInt(Env.getCtx(), "#AD_Role_ID") == 0)
				|| displayType != DisplayType.List)     //  only system admins can change lists, so no need to zoom for others
			{
				mZoom = new JMenuItem(Msg.getMsg(Env.getCtx(), "Zoom"), Env.getImageIcon("Zoom16.gif"));
				mZoom.addActionListener(this);
				popupMenu.add(mZoom);
			}
			mRefresh = new JMenuItem(Msg.getMsg(Env.getCtx(), "Refresh"), Env.getImageIcon("Refresh16.gif"));
			mRefresh.addActionListener(this);
			popupMenu.add(mRefresh);
		}
		//	VBPartner quick entry link
		if (columnName.equals("C_BPartner_ID"))
		{
			mBPartnerNew = new JMenuItem (Msg.getMsg(Env.getCtx(), "New"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerNew.addActionListener(this);
			popupMenu.add(mBPartnerNew);
			mBPartnerUpd = new JMenuItem (Msg.getMsg(Env.getCtx(), "Update"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerUpd.addActionListener(this);
			popupMenu.add(mBPartnerUpd);
		}
		//
		if (m_lookup != null && m_lookup.getZoom() == 0)
			mZoom.setEnabled(false);
	}	//	VLookup

	/**
	 *  Dispose
	 */
	public void dispose()
	{
		m_text = null;
		m_button = null;
		m_lookup = null;
		m_mField = null;
		m_combo.removeActionListener(this);
		m_combo.setModel(new DefaultComboBoxModel());    //  remove reference
	//	m_combo.removeAllItems();
		m_combo = null;
	}   //  dispose

	/** Display Length for Lookups (16)         */
	public final static int     DISPLAY_LENGTH = 16;

	/** Search: The Editable Text Field         */
	private CTextField 			m_text = new CTextField (DISPLAY_LENGTH);
	/** Search: The Button to open Editor   */
	private CButton				m_button = new CButton();
	/** The Combo Box if not a Search Lookup    */
	private VComboBox			m_combo = new VComboBox();
	/** Indicator that value is being set       */
	private volatile boolean 	m_settingValue = false;
	private volatile boolean 	m_settingFocus = false;
	/** Indicator that Lookup has focus         */
	private volatile boolean	m_haveFocus = false;
	/** Indicator - inserting new value			*/
	private volatile boolean	m_inserting = false;

	//
	private String				m_columnName;
	private Lookup				m_lookup;
	private int					m_displayType;
	private int					m_WindowNo;

	private boolean				m_comboActive = true;
	private Object				m_value;

	//	Popup
	JPopupMenu 					popupMenu = new JPopupMenu();
	private JMenuItem 			mZoom;
	private JMenuItem 			mRefresh;
	private JMenuItem			mBPartnerNew;
	private JMenuItem			mBPartnerUpd;

	private MField              m_mField = null;

	/**
	 *  Set Content and Size of Compoments
	 *  @param initial if true, size and margins will be set
	 */
	private void setUI (boolean initial)
	{
		if (initial)
		{
			Dimension size = m_text.getPreferredSize();
			setPreferredSize(new Dimension(size));  //	causes r/o to be the same length
			m_combo.setPreferredSize(new Dimension(size));
			//
			m_text.setBorder(null);
			Dimension bSize = new Dimension(size.height, size.height);
			m_button.setPreferredSize (bSize);
			m_button.setMargin(new Insets(0, 0, 0, 0));
		}

		//	What to show
		this.remove(m_combo);
		this.remove(m_button);
		this.remove(m_text);
		//
		if (!isReadWrite())									//	r/o - show text only
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			m_text.setReadWrite(false);
			m_combo.setReadWrite(false);
			m_comboActive = false;
		}
		else if (m_displayType != DisplayType.Search)	    //	show combo if not Search
		{
			this.setBorder(null);
			this.add(m_combo, BorderLayout.CENTER);
			m_comboActive = true;
		}
		else 												//	Search or unstable - show text & button
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			this.add(m_button, BorderLayout.EAST);
			m_text.setReadWrite (true);
			m_comboActive = false;
		}
	}   //  setUI

	/**
	 *	Set ReadWrite
	 *  @param value ReadWrite
	 */
	public void setReadWrite (boolean value)
	{
		boolean rw = value;
		if (m_lookup == null)
			rw = false;
		if (m_combo.isReadWrite() != value)
		{
			m_combo.setReadWrite(rw);
			setUI (false);
			if (m_comboActive)
				setValue (m_value);
		}
	}	//	setReadWrite

	/**
	 *	IsEditable
	 *  @return is lookup ReadWrite
	 */
	public boolean isReadWrite()
	{
		return m_combo.isReadWrite();
	}	//	isReadWrite

	/**
	 *	Set Mandatory (and back bolor)
	 *  @param mandatory mandatory
	 */
	public void setMandatory (boolean mandatory)
	{
		m_combo.setMandatory(mandatory);
		m_text.setMandatory(mandatory);
	}	//	setMandatory

	/**
	 *	Is it mandatory
	 *  @return true if mandatory
	 */
	public boolean isMandatory()
	{
		return m_combo.isMandatory();
	}	//	isMandatory

	/**
	 *	Set Background
	 *  @param color color
	 */
	public void setBackground(Color color)
	{
		m_text.setBackground(color);
		m_combo.setBackground(color);
	}	//	setBackground

	/**
	 *	Set Background
	 *  @param error error
	 */
	public void setBackground (boolean error)
	{
		m_text.setBackground(error);
		m_combo.setBackground(error);
	}	//	setBackground

	/**
	 *  Set Foreground
	 *  @param fg Foreground color
	 */
	public void setForeground(Color fg)
	{
		m_text.setForeground(fg);
		m_combo.setForeground(fg);
	}   //  setForeground

	/**
	 *  Set Editor to value
	 *  @param value new Value
	 */
	public void setValue (Object value)
	{
		Log.trace(Log.l6_Database, "VLookup.setValue", m_columnName + "=" + value);
		m_settingValue = true;		//	disable actions
		m_value = value;

		//	Set both for switching
		m_combo.setValue (value);
		if (value == null)
		{
			m_text.setText (null);
			m_settingValue = false;
			return;
		}
		if (m_lookup == null)
		{
			m_text.setText (value.toString());
			m_settingValue = false;
			return;
		}

		//	Set Display
		String display = m_lookup.getDisplay(value);
		boolean notFound = display.startsWith("<") && display.startsWith(">");
		m_text.setText (display);
		m_text.setCaretPosition (0); //	show beginning

		//	Nothing showing in Combo and should be showing
		if (m_combo.getSelectedItem() == null
			&& (m_comboActive || (m_inserting && m_displayType != DisplayType.Search)))
		{
			//  lookup found nothing too
			if (notFound)
			{
				Log.trace(8, "VLookup.setValue - Not found (1)", display);
				//  we may have a new value
				m_lookup.refresh();
				m_combo.setValue (value);
				display = m_lookup.getDisplay(value);
				m_text.setText (display);
				m_text.setCaretPosition (0);	//	show beginning
				notFound = display.startsWith("<") && display.endsWith(">");
			}
			if (notFound)	//	<key>
			{
				m_value = null;
				actionCombo (null);             //  data binding
				Log.trace(Log.l6_Database, "VLookup.setValue - not found - " + value);
			}
			//  we have lookup
			else if (m_combo.getSelectedItem() == null)
			{
				NamePair pp = m_lookup.get(value);
				if (pp != null)
				{
					Log.trace (Log.l6_Database, "VLookup.setValue - added to combo - " + pp);
					//  Add to Combo
					m_combo.addItem (pp);
					m_combo.setValue (value);
				}
			}
			//  Not in Lookup - set to Null
			if (m_combo.getSelectedItem() == null)
			{
				Log.trace(Log.l1_User, "VLookup.setValue - not in Lookup - set to NULL");
				actionCombo (null);             //  data binding (calls setValue again)
				m_value = null;
			}
		}
		m_settingValue = false;
	}	//	setValue

	/**
	 *  Property Change Listener
	 *  @param evt PropertyChangeEvent
	 */
	public void propertyChange (PropertyChangeEvent evt)
	{
	//	Log.trace(Log.l5_DData, "VLookup.propertyChange", evt);
		if (evt.getPropertyName().equals(MField.PROPERTY))
		{
			m_inserting = MField.INSERTING.equals(evt.getOldValue());
			setValue(evt.getNewValue());
			m_inserting = false;
		}
	}   //  propertyChange

	/**
	 *	Return Editor value (Integer)
	 *  @return value
	 */
	public Object getValue()
	{
		if (m_comboActive)
			return m_combo.getValue ();
		return m_value;
	}	//	getValue

	/**
	 *  Return editor display
	 *  @return display value
	 */
	public String getDisplay()
	{
		String retValue = null;
		if (m_comboActive)
			retValue = m_combo.getDisplay();
		//  check lookup
		else if (m_lookup == null)
			retValue = m_value.toString();
		else
			retValue = m_lookup.getDisplay(m_value);
	//	Log.trace(Log.l6_Database, "VLookup.getDisplay - " + retValue, "ComboActive=" + m_comboActive);
		return retValue;
	}   //  getDisplay

	/**
	 *  Set Field/WindowNo for ValuePreference
	 *  @param mField Model Field for Lookup
	 */
	public void setField (MField mField)
	{
		m_mField = mField;
		if (m_mField != null)
			ValuePreference.addMenu (this, popupMenu);
	}   //  setField

	/*************************************************************************/

	/**
	 *	Action Listener	- data binding
	 *  @param e ActionEvent
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (m_settingValue || m_settingFocus)
			return;
		Log.trace(Log.l4_Data, "VLookup.actionPerformed", e.getActionCommand() + ", ComboValue=" + m_combo.getSelectedItem());
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());

		//  Preference
		if (e.getActionCommand().equals(ValuePreference.NAME))
		{
			ValuePreference.start (m_mField, getValue(), getDisplay());
			return;
		}

		//  Combo Selection
		else if (e.getSource() == m_combo)
		{
			Object value = getValue();
			Object o = m_combo.getSelectedItem();
			if (o != null)
			{
				String s = o.toString();
				//  don't allow selection of inactive
				if (s.startsWith(MLookup.INACTIVE_S) && s.endsWith(MLookup.INACTIVE_E))
				{
					Log.trace(Log.l1_User, "VLookup.actionPerformed - Selection inactive set to NULL");
					value = null;
				}
			}
			actionCombo (value);                //  data binding
		}
		//  Button pressed
		else if (e.getSource() == m_button)
			actionButton ("");
		//  Text entered
		else if (e.getSource() == m_text)
			actionText();

		//  Popup Menu
		else if (e.getSource() == mZoom)
			actionZoom();
		else if (e.getSource() == mRefresh)
			actionRefresh();
		else if (e.getSource() == mBPartnerNew)
			actionBPartner(true);
		else if (e.getSource() == mBPartnerUpd)
			actionBPartner(false);
	}	//	actionPerformed

	/**
	 *  Action Listener Interface
	 *  @param listener listener
	 */
	public void addActionListener(ActionListener listener)
	{
		m_combo.addActionListener(listener);
		m_text.addActionListener(listener);
	}   //  addActionListener

	/**
	 *	Action - Combo.
	 *  <br>
	 *	== dataBinding == inform of new value
	 *  <pre>
	 *  VLookup.actionCombo
	 *      GridController.vetoableChange
	 *          MTable.setValueAt
	 *              MField.setValue
	 *                  VLookup.setValue
	 *          MTab.dataStatusChanged
	 *  </pre>
	 *  @param value new value
	 */
	private void actionCombo (Object value)
	{
	//	Log.trace(Log.l6_Database, "VLookup.actionCombo", value==null ? "null" : value.toString());
		try
		{
			fireVetoableChange (m_columnName, null, value);
		}
		catch (PropertyVetoException pve)
		{
			Log.error("VLookup.actionCombo", pve);
		}
		//  is the value updated ?
		boolean updated = false;
		if (value == null && m_value == null)
			updated = true;
		else if (value != null && value.equals(m_value))
			updated = true;
		if (!updated)
		{
			//  happens if VLookup is used outside of APanel/GridController (no property listener)
			Log.trace(Log.l6_Database, "VLookup.actionCombo - Value explicitly set - new=" + value + ", old=" + m_value);
			setValue(value);
		}
	}	//	actionCombo


	/**
	 *	Action - Button.
	 *	- Call Info
	 *	@param queryValue initial query value
	 */
	private void actionButton (String queryValue)
	{
		m_button.setEnabled(false);                 //  disable double click
		requestFocus();                             //  closes other editors
		Frame frame = Env.getFrame(this);

		/**
		 *  Three return options:
		 *  - Value Selected & OK pressed   => store result => result has value
		 *  - Cancel pressed                => store null   => result == null && cancelled
		 *  - Window closed                 -> ignore       => result == null && !cancalled
		 */
		Object result = null;
		boolean cancelled = false;
		//
		String col = m_lookup.getColumnName();		//	fully qualified name
		if (col.indexOf(".") != -1)
			col = col.substring(col.indexOf(".")+1);
		//  Zoom / Validation
		String whereClause = whereClause = getWhereClause();
		//
		Log.trace(Log.l5_DData, "VLookup.actionButton - " + col 
			+ ", Zoom=" + m_lookup.getZoom()
			+ " (" + whereClause + ")");
		//
		boolean resetValue = false;	//	reset value so that is always treated as new entry    
		if (col.equals("M_Product_ID"))
		{
			//	Reset
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_Product_ID", "0");
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_AttributeSetInstance_ID", "0");
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = "@" + m_text.getText() + "@";   //  Name indicator - otherwise Value
			int M_Warehouse_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNo, "M_Warehouse_ID");
			int M_PriceList_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNo, "M_PriceList_ID");
			//	Show Info
			InfoProduct ip = new InfoProduct (frame, true, m_WindowNo,
				M_Warehouse_ID, M_PriceList_ID, queryValue, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
			resetValue = true;
		}
		else if (col.equals("C_BPartner_ID"))
		{
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = m_text.getText();
			boolean isSOTrx = true;     //  default
			if (Env.getContext(Env.getCtx(), m_WindowNo, "IsSOTrx").equals("N"))
				isSOTrx = false;
			InfoBPartner ip = new InfoBPartner (frame, true, m_WindowNo,
				queryValue, isSOTrx, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
		}
		else
		{
			if (m_tableName == null)	//	sets table anem & key column
				getDirectAccessSQL("*");
			Info ig = Info.create (frame, true, m_WindowNo, m_tableName, m_keyColumnName, queryValue, false, whereClause);
			ig.show();
			cancelled = ig.isCancelled();
			result = ig.getSelectedKey();
		}
		//  Result
		if (result != null)
		{
			Log.trace(Log.l4_Data, "VLookupactionButton", "Result = " + result.toString() + " " + result.getClass().getName());
			//  make sure that value is in cache
			m_lookup.getDirect(result, false, true);
			if (resetValue)
				actionCombo (null);
			actionCombo (result);
		}
		else if (cancelled)
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (cancelled)");
			actionCombo (null);
		}
		else
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (not cancelled)");
			setValue(m_value);      //  to re-display value
		}
		//
		m_button.setEnabled(true);
	}	//	actionButton

	/**
	 * 	Get Where Clause
	 *	@return where clause or ""
	 */
	private String getWhereClause()
	{
		String whereClause = "";
		if (m_lookup.getZoomQuery() != null)
			whereClause = m_lookup.getZoomQuery().getWhereClause();
		if (whereClause.length() == 0)
			whereClause = m_lookup.getValidation();
		return whereClause;
	}	//	getWhereClause

	/**
	 *	Check, if data returns unique entry, otherwise involve Info via Button
	 */
	private void actionText()
	{
		String text = m_text.getText();
		//	Nothing entered
		if (text == null || text.length() == 0 || text.equals("%"))
		{
			actionButton(text);
			return;
		}
		//	Always like
		if (!text.endsWith("%"))
			text += "%";
		text = text.toUpperCase();

		String finalSQL = getDirectAccessSQL(text);
		int id = 0;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(finalSQL);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				id = rs.getInt(1);		//	first
				if (rs.next())
					id = -1;			//	only if unique
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("VLookup.actionText\nSQL=" + finalSQL, e);
			id = -2;
		}
		Log.trace(Log.l5_DData, "VLookup.actionText ID => " + id);

		//	No (unique) result
		if (id <= 0)
		{
			Log.trace(Log.l6_Database, finalSQL);
			actionButton(m_text.getText());
			return;
		}
		m_value = null;     //  forces re-display if value is unchanged but text updated and still unique
		actionCombo (new Integer(id));          //  data binding
	}	//	actionText


	private String		m_tableName = null;
	private String		m_keyColumnName = null;

	/**
	 * 	Generate Access SQL for Search.
	 * 	The SQL returns the ID of the value entered
	 * 	Also sets m_tableName and m_keyColumnName
	 *	@param text upper like text
	 *	@return sql or ""
	 *  Example
	 *	SELECT C_Payment_ID FROM C_Payment WHERE UPPER(DocumentNo) LIKE x OR ...
	 */
	private String getDirectAccessSQL (String text)
	{
		StringBuffer sql = new StringBuffer();
		m_tableName = m_columnName.substring(0, m_columnName.length()-3);
		m_keyColumnName = m_columnName;
		//
		if (m_columnName.equals("M_Product_ID"))
		{
			//	Reset
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_Product_ID", "0");
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_AttributeSetInstance_ID", "0");
			//
			sql.append("SELECT M_Product_ID FROM M_Product WHERE UPPER(Value) LIKE ")
				.append(DB.TO_STRING(text))
				.append(" OR UPC LIKE ").append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_BPartner_ID"))
		{
			sql.append("SELECT C_BPartner_ID FROM C_BPartner WHERE UPPER(Value) LIKE ")
				.append(DB.TO_STRING(text))
				.append(" OR UPPER(Name) LIKE ").append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_Order_ID"))
		{
			sql.append("SELECT C_Order_ID FROM C_Order WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_Invoice_ID"))
		{
			sql.append("SELECT C_Invoice_ID FROM C_Invoice WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("M_InOut_ID"))
		{
			sql.append("SELECT M_InOut_ID FROM M_InOut WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_Payment_ID"))
		{
			sql.append("SELECT C_Payment_ID FROM C_Payment WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("GL_JournalBatch_ID"))
		{
			sql.append("SELECT GL_JournalBatch_ID FROM GL_JournalBatch WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		/**
		else if (m_columnName.equals("SalesRep_ID"))
		{
			sql.append("SELECT AD_User_ID FROM AD_User WHERE UPPER(Name) LIKE ")
				.append(DB.TO_STRING(text));
			m_tableName = "AD_User";
			m_keyColumnName = "AD_User_ID";
		}
		**/
		//	Predefined
		if (sql.length() > 0)
		{
			String wc = getWhereClause();
			if (wc != null && wc.length() > 0)
				sql.append(" AND ").append(wc);
			sql.append(" AND IsActive='Y'");
			//	***
			return MRole.getDefault().addAccessSQL(sql.toString(),
				m_tableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
		}
		
		//	Check if it is a Table Reference
		if (m_lookup instanceof MLookup)
		{
			int AD_Reference_ID = ((MLookup)m_lookup).getAD_Reference_Value_ID();
			if (AD_Reference_ID != 0)
			{
				String query = "SELECT kc.ColumnName, dc.ColumnName, t.TableName "
					+ "FROM AD_Ref_Table rt"
					+ " INNER JOIN AD_Column kc ON (rt.AD_Key=kc.AD_Column_ID)"
					+ " INNER JOIN AD_Column dc ON (rt.AD_Display=dc.AD_Column_ID)"
					+ " INNER JOIN AD_Table t ON (rt.AD_Table_ID=t.AD_Table_ID) "
					+ "WHERE rt.AD_Reference_ID=?";
				String displayColumnName = null;
				PreparedStatement pstmt = null;
				try
				{
					pstmt = DB.prepareCall(query);
					pstmt.setInt(1, AD_Reference_ID);
					ResultSet rs = pstmt.executeQuery();
					if (rs.next())
					{
						m_keyColumnName = rs.getString(1);
						displayColumnName = rs.getString(2);
						m_tableName = rs.getString(3);
					}
					rs.close();
					pstmt.close();
					pstmt = null;
				}
				catch (Exception e)
				{
					Log.error("VLookup.getDirectAccessSQL", e);
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
				if (displayColumnName != null)
				{
					sql = new StringBuffer();
					sql.append("SELECT ").append(m_keyColumnName)
						.append(" FROM ").append(m_tableName)
						.append(" WHERE UPPER(").append(displayColumnName)
						.append(") LIKE ").append(DB.TO_STRING(text))
						.append(" AND IsActive='Y'");
					String wc = getWhereClause();
					if (wc != null && wc.length() > 0)
						sql.append(" AND ").append(wc);
					//	***
					return MRole.getDefault().addAccessSQL(sql.toString(),
								m_tableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
				}
			}	//	Table Reference
		}	//	MLookup
		
		/** Check Well Known Columns of Table - assumes TableDir	**/
		String query = "SELECT t.TableName, c.ColumnName "
			+ "FROM AD_Column c "
			+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID AND t.IsView='N') "
			+ "WHERE c.ColumnName IN ('DocumentNo', 'Value', 'Name')"
			+ " AND EXISTS (SELECT * FROM AD_Column cc WHERE cc.AD_Table_ID=t.AD_Table_ID"
				+ " AND cc.IsKey='Y' AND cc.ColumnName=?)";
		sql = new StringBuffer();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(query);
			pstmt.setString(1, m_columnName);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				if (sql.length() != 0)
					sql.append(" OR ");
				m_tableName = rs.getString(1);
				sql.append("UPPER(").append(rs.getString(2)).append(") LIKE ").append(DB.TO_STRING(text));
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException ex)
		{
			Log.error("VLookup.getDirectAccessSQL", ex);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
		}
		catch (SQLException ex1)
		{
		}
		pstmt = null;
		if (sql.length() == 0)
			return "";
		//
		StringBuffer retValue = new StringBuffer ("SELECT ")
			.append(m_columnName).append(" FROM ").append(m_tableName)
			.append(" WHERE ").append(sql)
			.append(" AND IsActive='Y'");
		String wc = getWhereClause();
		if (wc != null && wc.length() > 0)
			retValue.append(" AND ").append(wc);
		//	***
		return MRole.getDefault().addAccessSQL(retValue.toString(),
					m_tableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
	}	//	getDirectAccessSQL


	/**
	 *	Action - Special BPartner Screen
	 *  @param newRecord true if new record should be created
	 */
	private void actionBPartner (boolean newRecord)
	{
		VBPartner vbp = new VBPartner (Env.getFrame(this), m_WindowNo);
		int BPartner_ID = 0;
		//  if update, get current value
		if (!newRecord)
		{
			if (m_value instanceof Integer)
				BPartner_ID = ((Integer)m_value).intValue();
			else if (m_value != null)
				BPartner_ID = Integer.parseInt(m_value.toString());
		}

		vbp.loadBPartner (BPartner_ID);
		vbp.show();
		//  get result
		int result = vbp.getBPartner_ID();
		if (result == 0					//	0 = not saved
			&& result == BPartner_ID)	//	the same
			return;
		//  Maybe new BPartner - put in cache
		m_lookup.getDirect(new Integer(result), false, true);

		actionCombo (new Integer(result));      //  data binding
	}	//	actionBPartner

	/**
	 *	Action - Zoom
	 */
	private void actionZoom()
	{
		if (m_lookup == null)
			return;
		//
		String IsSOTrx = Env.getContext(Env.getCtx(), m_WindowNo, "IsSOTrx");
		int AD_Window_ID = m_lookup.getZoom(IsSOTrx);
		MQuery zoomQuery = m_lookup.getZoomQuery();
		Log.trace(Log.l1_User, "VLookup.actionZoom - " + AD_Window_ID + " - IsSOTrx=" + IsSOTrx + " - Query=" + zoomQuery + " - Value=" + getValue());
		//	If not already exist
		if (zoomQuery == null || (!zoomQuery.isActive() && getValue() != null))
		{
			zoomQuery = new MQuery();
			zoomQuery.addRestriction(m_columnName, MQuery.EQUAL, getValue());
		}
		//
		setCursor(Cursor.getDefaultCursor());
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		AWindow frame = new AWindow();
		if (!frame.initWindow(AD_Window_ID, zoomQuery, IsSOTrx.equals("Y")))
		{
			setCursor(Cursor.getDefaultCursor());
			ValueNamePair pp = Log.retrieveError();
			String msg = pp==null ? "AccessTableNoView" : pp.getValue();
			ADialog.error(m_WindowNo, this, msg, pp==null ? "" : pp.getName());
		}
		else
			AEnv.showCenterScreen(frame);
			//  async window - not able to get feedback
		frame = null;
		//
		setCursor(Cursor.getDefaultCursor());
	}	//	actionZoom

	/**
	 *	Action - Refresh
	 */
	private void actionRefresh()
	{
		if (m_lookup == null)
			return;
		//
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		Object obj = m_combo.getSelectedItem();
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + obj);
		m_lookup.refresh();
		if (m_lookup.isValidated())
			m_lookup.fillComboBox(isMandatory(), false, false, false);
		else
			m_lookup.fillComboBox(isMandatory(), true, false, false);
		m_combo.setSelectedItem(obj);
	//	m_combo.revalidate();
		//
		setCursor(Cursor.getDefaultCursor());
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + m_combo.getSelectedItem());
	}	//	actionRefresh

	/*************************************************************************/

	/**
	 *	Focus Listener for ComboBoxes with missing Validation or invalid entries
	 *	- Requery listener for updated list
	 *  @param e FocusEvent
	 */
	public void focusGained (FocusEvent e)
	{
		if (e.getSource() != m_combo || e.isTemporary() || m_haveFocus)
			return;
		if (m_lookup.isValidated() && !m_lookup.hasInactive())
			return;
		//
		m_haveFocus = true;     //  prevents calling focus gained twice
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Object obj = m_lookup.getSelectedItem();
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Start    Count=" + m_combo.getItemCount() + ", Selected=" + obj);
		Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());
		m_lookup.fillComboBox(isMandatory(), true, true, true);     //  only validated & active & temporary
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Update   Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		m_lookup.setSelectedItem(obj);
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Selected Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		//
		m_settingFocus = false;
	}	//	focusGained

	/**
	 *	Reset Selection List
	 *  @param e FocusEvent
	 */
	public void focusLost(FocusEvent e)
	{
		if (e.getSource() != m_combo || e.isTemporary())
			return;
		if (m_lookup.isValidated() && !m_lookup.hasInactive())
			return;
		//
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Log.trace(Log.l4_Data, "VLookup.focusLost", m_columnName + " = " + m_combo.getSelectedItem());
		Object obj = m_combo.getSelectedItem();
		//	set original model
		if (!m_lookup.isValidated())
			m_lookup.fillComboBox(true);    //  previous selection
		//	Set value
		if (obj != null)
		{
			m_combo.setSelectedItem(obj);
			//	original model may not have item
			if (!m_combo.getSelectedItem().equals(obj))
			{
				Log.trace(Log.l6_Database, "VLookup.focusLost " + m_columnName, "added to combo - " + obj);
				m_combo.addItem(obj);
				m_combo.setSelectedItem(obj);
			}
		}
	//	actionCombo(getValue());
		m_settingFocus = false;
		m_haveFocus = false;    //  can gain focus again
	}	//	focusLost

	/**
	 *  Set ToolTip
	 *  @param text tool tip text
	 */
	public void setToolTipText(String text)
	{
		super.setToolTipText(text);
		m_button.setToolTipText(text);
		m_text.setToolTipText(text);
		m_combo.setToolTipText(text);
	}   //  setToolTipText

}	//	VLookup

/*****************************************************************************/

/**
 *	Mouse Listener for Popup Menu
 */
final class VLookup_mouseAdapter extends java.awt.event.MouseAdapter
{
	/**
	 *	Constructor
	 *  @param adaptee adaptee
	 */
	VLookup_mouseAdapter(VLookup adaptee)
	{
		this.adaptee = adaptee;
	}	//	VLookup_mouseAdapter

	private VLookup adaptee;

	/**
	 *	Mouse Listener
	 *  @param e MouseEvent
	 */
	public void mouseClicked(MouseEvent e)
	{
	//	System.out.println("mouseClicked " + e.getID() + " " + e.getSource().getClass().toString());
		//	popup menu
		if (SwingUtilities.isRightMouseButton(e))
			adaptee.popupMenu.show((Component)e.getSource(), e.getX(), e.getY());
	}	//	mouse Clicked

}	//	VLookup_mouseAdapter
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
package org.compiere.grid.ed;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.text.*;
import java.util.*;
import java.math.*;
import java.beans.*;
import java.sql.*;

import org.compiere.apps.*;
import org.compiere.util.*;
import org.compiere.model.*;
import org.compiere.apps.search.*;
import org.compiere.plaf.*;
import org.compiere.swing.*;

/**
 *  Lookup Field.
 *  <p>
 *	    When r/o - display a Label
 *		When STABLE - display a ComboBox
 *		Otherwise show Selection Dialog
 *  <p>
 *  Sepecial handling of BPartner and Product
 *
 *  @author 	Jorg Janke
 *  @version 	$Id: VLookup.java,v 1.43 2003/07/16 19:08:57 jjanke Exp $
 */
public class VLookup extends JComponent
	implements VEditor, ActionListener, FocusListener
{
	/**
	 *  Create BPartner Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createBPartner (int WindowNo)
	{
		int AD_Column_ID = 3499;    //  C_Invoice.C_BPartner_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, AD_Column_ID, DisplayType.Search);
			return new VLookup ("C_BPartner_ID", false, false, true, lookup, DisplayType.Search, WindowNo);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createBPartner", e);
		}
		return null;
	}   //  createBPartner

	/**
	 *  Create Product Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createProduct (int WindowNo)
	{
		int AD_Column_ID = 3840;    //  C_InvoiceLine.M_Product_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, AD_Column_ID, DisplayType.Search);
			return new VLookup ("M_Product_ID", false, false, true, lookup, DisplayType.Search, WindowNo);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createProduct", e);
		}
		return null;
	}   //  createProduct

	/*************************************************************************/

	/**
	 *	IDE Default Constructor
	 */
	public VLookup()
	{
		this("Lookup", false, false, true, null, 0, 0);
	}	//	VLookup


	/**
	 *	Detail Constructor
	 *
	 *  @param columnName column
	 *  @param mandatory mandatory
	 *  @param isReadOnly read only
	 *  @param isUpdateable updateable
	 *  @param lookup lookup
	 *  @param displayType display type
	 *  @param WindowNo window no
	 */
	public VLookup (String columnName, boolean mandatory, boolean isReadOnly, boolean isUpdateable,
		Lookup lookup, int displayType, int WindowNo)
	{
		super();
		m_columnName = columnName;
		m_lookup = lookup;
		if (lookup == null)
			Log.trace(Log.l3_Util, "VLookup", "Lookup is NULL = " + columnName);
		setMandatory(mandatory);
		m_displayType = displayType;
		m_WindowNo = WindowNo;		//	for Info
		//
		setLayout(new BorderLayout());
		VLookup_mouseAdapter mouse = new VLookup_mouseAdapter(this);    //  popup

		//	***	Text & Button	***
		m_text.addActionListener(this);
		m_text.addMouseListener(mouse);
		//  Button
		m_button.addActionListener(this);
		m_button.addMouseListener(mouse);
		m_button.setFocusable(false);   //  don't focus when tabbing
		if (columnName.equals("C_BPartner_ID"))
			m_button.setIcon(Env.getImageIcon("BPartner10.gif"));
		else if (columnName.equals("M_Product_ID"))
			m_button.setIcon(Env.getImageIcon("Product10.gif"));
		else
			m_button.setIcon(Env.getImageIcon("PickOpen10.gif"));

		//	*** VComboBox	***
		if (m_lookup != null && displayType != DisplayType.Search)	//	No Search
		{
			//  Memory Leak after executing the next two lines ??
			m_lookup.fillComboBox (isMandatory(), false, false, false);
			m_combo.setModel(m_lookup);
			//
			m_combo.addActionListener(this);							//	Selection
			m_combo.addMouseListener(mouse);	                        //	popup
			//	FocusListener to refresh selection before opening
			if (!m_lookup.isValidated() || m_lookup.hasInactive())
				m_combo.addFocusListener(this);
		}

		setUI (true);
		//	ReadWrite	-	decides what components to show
		if (isReadOnly || !isUpdateable || m_lookup == null)
			setReadWrite(false);
		else
			setReadWrite(true);

		//	Popup
		if (m_lookup != null)
		{
			if ((displayType == DisplayType.List && Env.getContextAsInt(Env.getCtx(), "#AD_Role_ID") == 0)
				|| displayType != DisplayType.List)     //  only system admins can change lists, so no need to zoom for others
			{
				mZoom = new JMenuItem(Msg.getMsg(Env.getCtx(), "Zoom"), Env.getImageIcon("Zoom16.gif"));
				mZoom.addActionListener(this);
				popupMenu.add(mZoom);
			}
			mRefresh = new JMenuItem(Msg.getMsg(Env.getCtx(), "Refresh"), Env.getImageIcon("Refresh16.gif"));
			mRefresh.addActionListener(this);
			popupMenu.add(mRefresh);
		}
		//	VBPartner quick entry link
		if (columnName.equals("C_BPartner_ID"))
		{
			mBPartnerNew = new JMenuItem (Msg.getMsg(Env.getCtx(), "New"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerNew.addActionListener(this);
			popupMenu.add(mBPartnerNew);
			mBPartnerUpd = new JMenuItem (Msg.getMsg(Env.getCtx(), "Update"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerUpd.addActionListener(this);
			popupMenu.add(mBPartnerUpd);
		}
		//
		if (m_lookup != null && m_lookup.getZoom() == 0)
			mZoom.setEnabled(false);
	}	//	VLookup

	/**
	 *  Dispose
	 */
	public void dispose()
	{
		m_text = null;
		m_button = null;
		m_lookup = null;
		m_mField = null;
		m_combo.removeActionListener(this);
		m_combo.setModel(new DefaultComboBoxModel());    //  remove reference
	//	m_combo.removeAllItems();
		m_combo = null;
	}   //  dispose

	/** Display Length for Lookups (18)         */
	public final static int     DISPLAY_LENGTH = 18;

	/** Search: The Editable Text Field         */
	private CTextField 			m_text = new CTextField (DISPLAY_LENGTH);
	/** Search: The Button to open Editor   */
	private CButton				m_button = new CButton();
	/** The Combo Box if not a Search Lookup    */
	private VComboBox			m_combo = new VComboBox();
	/** Indicator that value is being set       */
	private volatile boolean 	m_settingValue = false;
	private volatile boolean 	m_settingFocus = false;
	/** Indicator that Lookup has focus         */
	private volatile boolean	m_haveFocus = false;
	/** Indicator - inserting new value			*/
	private volatile boolean	m_inserting = false;

	//
	private String				m_columnName;
	private Lookup				m_lookup;
	private int					m_displayType;
	private int					m_WindowNo;

	private boolean				m_comboActive = true;
	private Object				m_value;

	//	Popup
	JPopupMenu 					popupMenu = new JPopupMenu();
	private JMenuItem 			mZoom;
	private JMenuItem 			mRefresh;
	private JMenuItem			mBPartnerNew;
	private JMenuItem			mBPartnerUpd;

	private MField              m_mField = null;

	/**
	 *  Set Content and Size of Compoments
	 *  @param initial if true, size and margins will be set
	 */
	private void setUI (boolean initial)
	{
		if (initial)
		{
			Dimension size = m_text.getPreferredSize();
			setPreferredSize(new Dimension(size));  //	causes r/o to be the same length
			m_combo.setPreferredSize(new Dimension(size));
			//
			m_text.setBorder(null);
			Dimension bSize = new Dimension(size.height, size.height);
			m_button.setPreferredSize (bSize);
			m_button.setMargin(new Insets(0, 0, 0, 0));
		}

		//	What to show
		this.remove(m_combo);
		this.remove(m_button);
		this.remove(m_text);
		//
		if (!isReadWrite())									//	r/o - show text only
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			m_text.setReadWrite(false);
			m_combo.setReadWrite(false);
			m_comboActive = false;
		}
		else if (m_displayType != DisplayType.Search)	    //	show combo if not Search
		{
			this.setBorder(null);
			this.add(m_combo, BorderLayout.CENTER);
			m_comboActive = true;
		}
		else 												//	Search or unstable - show text & button
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			this.add(m_button, BorderLayout.EAST);
			m_text.setReadWrite (true);
			m_comboActive = false;
		}
	}   //  setUI

	/**
	 *	Set ReadWrite
	 *  @param value ReadWrite
	 */
	public void setReadWrite (boolean value)
	{
		boolean rw = value;
		if (m_lookup == null)
			rw = false;
		if (m_combo.isReadWrite() != value)
		{
			m_combo.setReadWrite(rw);
			setUI (false);
			if (m_comboActive)
				setValue (m_value);
		}
	}	//	setReadWrite

	/**
	 *	IsEditable
	 *  @return is lookup ReadWrite
	 */
	public boolean isReadWrite()
	{
		return m_combo.isReadWrite();
	}	//	isReadWrite

	/**
	 *	Set Mandatory (and back bolor)
	 *  @param mandatory mandatory
	 */
	public void setMandatory (boolean mandatory)
	{
		m_combo.setMandatory(mandatory);
		m_text.setMandatory(mandatory);
	}	//	setMandatory

	/**
	 *	Is it mandatory
	 *  @return true if mandatory
	 */
	public boolean isMandatory()
	{
		return m_combo.isMandatory();
	}	//	isMandatory

	/**
	 *	Set Background
	 *  @param color color
	 */
	public void setBackground(Color color)
	{
		m_text.setBackground(color);
		m_combo.setBackground(color);
	}	//	setBackground

	/**
	 *	Set Background
	 *  @param error error
	 */
	public void setBackground (boolean error)
	{
		m_text.setBackground(error);
		m_combo.setBackground(error);
	}	//	setBackground

	/**
	 *  Set Foreground
	 *  @param fg Foreground color
	 */
	public void setForeground(Color fg)
	{
		m_text.setForeground(fg);
		m_combo.setForeground(fg);
	}   //  setForeground

	/**
	 *  Set Editor to value
	 *  @param value new Value
	 */
	public void setValue (Object value)
	{
		Log.trace(Log.l6_Database, "VLookup.setValue", m_columnName + "=" + value);
		m_settingValue = true;		//	disable actions
		m_value = value;

		//	Set both for switching
		m_combo.setValue (value);
		if (value == null)
		{
			m_text.setText (null);
			m_settingValue = false;
			return;
		}
		if (m_lookup == null)
		{
			m_text.setText (value.toString());
			m_settingValue = false;
			return;
		}

		//	Set Display
		String display = m_lookup.getDisplay(value);
		boolean notFound = display.startsWith("<") && display.startsWith(">");
		m_text.setText (display);
		m_text.setCaretPosition (0); //	show beginning

		//	Nothing showing in Combo and should be showing
		if (m_combo.getSelectedItem() == null
			&& (m_comboActive || (m_inserting && m_displayType != DisplayType.Search)))
		{
			//  lookup found nothing too
			if (notFound)
			{
				Log.trace(8, "VLookup.setValue - Not found (1)", display);
				//  we may have a new value
				m_lookup.refresh();
				m_combo.setValue (value);
				display = m_lookup.getDisplay(value);
				m_text.setText (display);
				m_text.setCaretPosition (0);	//	show beginning
				notFound = display.startsWith("<") && display.endsWith(">");
			}
			if (notFound)	//	<key>
			{
				m_value = null;
				actionCombo (null);             //  data binding
				Log.trace(Log.l6_Database, "VLookup.setValue - not found - " + value);
			}
			//  we have lookup
			else if (m_combo.getSelectedItem() == null)
			{
				NamePair pp = m_lookup.get(value);
				if (pp != null)
				{
					Log.trace (Log.l6_Database, "VLookup.setValue - added to combo - " + pp);
					//  Add to Combo
					m_combo.addItem (pp);
					m_combo.setValue (value);
				}
			}
			//  Not in Lookup - set to Null
			if (m_combo.getSelectedItem() == null)
			{
				Log.trace(Log.l1_User, "VLookup.setValue - not in Lookup - set to NULL");
				actionCombo (null);             //  data binding (calls setValue again)
				m_value = null;
			}
		}
		m_settingValue = false;
	}	//	setValue

	/**
	 *  Property Change Listener
	 *  @param evt PropertyChangeEvent
	 */
	public void propertyChange (PropertyChangeEvent evt)
	{
	//	Log.trace(Log.l5_DData, "VLookup.propertyChange", evt);
		if (evt.getPropertyName().equals(MField.PROPERTY))
		{
			m_inserting = MField.INSERTING.equals(evt.getOldValue());
			setValue(evt.getNewValue());
			m_inserting = false;
		}
	}   //  propertyChange

	/**
	 *	Return Editor value (Integer)
	 *  @return value
	 */
	public Object getValue()
	{
		if (m_comboActive)
			return m_combo.getValue ();
		return m_value;
	}	//	getValue

	/**
	 *  Return editor display
	 *  @return display value
	 */
	public String getDisplay()
	{
		String retValue = null;
		if (m_comboActive)
			retValue = m_combo.getDisplay();
		//  check lookup
		else if (m_lookup == null)
			retValue = m_value.toString();
		else
			retValue = m_lookup.getDisplay(m_value);
	//	Log.trace(Log.l6_Database, "VLookup.getDisplay - " + retValue, "ComboActive=" + m_comboActive);
		return retValue;
	}   //  getDisplay

	/**
	 *  Set Field/WindowNo for ValuePreference
	 *  @param mField Model Field for Lookup
	 */
	public void setField (MField mField)
	{
		m_mField = mField;
		if (m_mField != null)
			ValuePreference.addMenu (this, popupMenu);
	}   //  setField

	/*************************************************************************/

	/**
	 *	Action Listener	- data binding
	 *  @param e ActionEvent
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (m_settingValue || m_settingFocus)
			return;
		Log.trace(Log.l4_Data, "VLookup.actionPerformed", e.getActionCommand() + ", ComboValue=" + m_combo.getSelectedItem());
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());

		//  Preference
		if (e.getActionCommand().equals(ValuePreference.NAME))
		{
			ValuePreference.start (m_mField, getValue(), getDisplay());
			return;
		}

		//  Combo Selection
		else if (e.getSource() == m_combo)
		{
			Object value = getValue();
			Object o = m_combo.getSelectedItem();
			if (o != null)
			{
				String s = o.toString();
				//  don't allow selection of inactive
				if (s.startsWith(MLookup.INACTIVE_S) && s.endsWith(MLookup.INACTIVE_E))
				{
					Log.trace(Log.l1_User, "VLookup.actionPerformed - Selection inactive set to NULL");
					value = null;
				}
			}
			actionCombo (value);                //  data binding
		}
		//  Button pressed
		else if (e.getSource() == m_button)
			actionButton ("");
		//  Text entered
		else if (e.getSource() == m_text)
			actionText();

		//  Popup Menu
		else if (e.getSource() == mZoom)
			actionZoom();
		else if (e.getSource() == mRefresh)
			actionRefresh();
		else if (e.getSource() == mBPartnerNew)
			actionBPartner(true);
		else if (e.getSource() == mBPartnerUpd)
			actionBPartner(false);
	}	//	actionPerformed

	/**
	 *  Action Listener Interface
	 *  @param listener listener
	 */
	public void addActionListener(ActionListener listener)
	{
		m_combo.addActionListener(listener);
		m_text.addActionListener(listener);
	}   //  addActionListener

	/**
	 *	Action - Combo.
	 *  <br>
	 *	== dataBinding == inform of new value
	 *  <pre>
	 *  VLookup.actionCombo
	 *      GridController.vetoableChange
	 *          MTable.setValueAt
	 *              MField.setValue
	 *                  VLookup.setValue
	 *          MTab.dataStatusChanged
	 *  </pre>
	 *  @param value new value
	 */
	private void actionCombo (Object value)
	{
	//	Log.trace(Log.l6_Database, "VLookup.actionCombo", value==null ? "null" : value.toString());
		try
		{
			fireVetoableChange (m_columnName, null, value);
		}
		catch (PropertyVetoException pve)
		{
			Log.error("VLookup.actionCombo", pve);
		}
		//  is the value updated ?
		boolean updated = false;
		if (value == null && m_value == null)
			updated = true;
		else if (value != null && value.equals(m_value))
			updated = true;
		if (!updated)
		{
			//  happens if VLookup is used outside of APanel/GridController (no property listener)
			Log.trace(Log.l6_Database, "VLookup.actionCombo - Value explicitly set - new=" + value + ", old=" + m_value);
			setValue(value);
		}
	}	//	actionCombo


	/**
	 *	Action - Button.
	 *	- Call Info
	 *	@param queryValue initial query value
	 */
	private void actionButton (String queryValue)
	{
		m_button.setEnabled(false);                 //  disable double click
		requestFocus();                             //  closes other editors
		Frame frame = Env.getFrame(this);

		/**
		 *  Three return options:
		 *  - Value Selected & OK pressed   => store result => result has value
		 *  - Cancel pressed                => store null   => result == null && cancelled
		 *  - Window closed                 -> ignore       => result == null && !cancalled
		 */
		Object result = null;
		boolean cancelled = false;
		//
		String col = m_lookup.getColumnName();		//	fully qualified name
		if (col.indexOf(".") != -1)
			col = col.substring(col.indexOf(".")+1);
		//  Zoom / Validation
		String whereClause = "";
		if (m_lookup.getZoomQuery() != null)
			whereClause = m_lookup.getZoomQuery().getWhereClause();
		if (whereClause.length() == 0)
			whereClause = m_lookup.getValidation();
		//
		Log.trace(Log.l5_DData, "VLookup.actionButton - " + col + ", Zoom=" + m_lookup.getZoom()
			+ " (" + whereClause + ")");
		//
		boolean resetValue = false;             //  reset value so that is always treated as new entry
		if (col.equals("M_Product_ID"))
		{
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = "@" + m_text.getText() + "@";   //  Name indicator - otherwise Value
			int M_Warehouse_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNo, "M_Warehouse_ID");
			int M_PriceList_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNo, "M_PriceList_ID");
			InfoProduct ip = new InfoProduct (frame, true, m_WindowNo,
				M_Warehouse_ID, M_PriceList_ID, queryValue, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
			resetValue = true;
		}
		else if (col.equals("C_BPartner_ID"))
		{
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = m_text.getText();
			boolean isSOTrx = true;     //  default
			if (Env.getContext(Env.getCtx(), m_WindowNo, "IsSOTrx").equals("N"))
				isSOTrx = false;
			InfoBPartner ip = new InfoBPartner (frame, true, m_WindowNo,
				queryValue, isSOTrx, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
		}
		else
		{
			String tableName = col;
			int index = m_columnName.indexOf("_ID");
			if (index != -1)
				tableName = m_columnName.substring(0, index);
			Info ig = Info.create (frame, true, m_WindowNo, tableName, m_columnName, queryValue, false, whereClause);
			ig.show();
			cancelled = ig.isCancelled();
			result = ig.getSelectedKey();
		}
		//  Result
		if (result != null)
		{
			Log.trace(Log.l4_Data, "VLookupactionButton", "Result = " + result.toString() + " " + result.getClass().getName());
			//  make sure that value is in cache
			m_lookup.getDirect(result, true);
			if (resetValue)
				actionCombo (null);
			actionCombo (result);
		}
		else if (cancelled)
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (cancelled)");
			actionCombo (null);
		}
		else
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (not cancelled)");
			setValue(m_value);      //  to re-display value
		}
		//
		m_button.setEnabled(true);
	}	//	actionButton

	/**
	 *	Check, if data returns unique entry, otherwise involve Info via Button
	 */
	private void actionText()
	{
		String text = m_text.getText();
		//	Nothing entered
		if (m_text.getText().length() == 0)
		{
			actionButton(text);
			return;
		}
		//	Always like
		if (!text.endsWith("%"))
			text += "%";

		StringBuffer SQL = new StringBuffer();
		if (m_columnName.equals("M_Product_ID"))
		{
			SQL.append("SELECT M_Product_ID FROM M_Product WHERE UPPER(Value) LIKE '")
				.append(text.toUpperCase()).append("'");
		}
		else if (m_columnName.equals("C_BPartner_ID"))
		{
			SQL.append("SELECT C_BPartner_ID FROM C_BPartner WHERE (UPPER(Value) LIKE '")
				.append(text.toUpperCase())
				.append("' OR UPPER(Name) LIKE '").append(text.toUpperCase()).append("')");
		}
		else if (m_columnName.equals("C_Order_ID"))
		{
			SQL.append("SELECT C_Order_ID FROM C_Order WHERE UPPER(DocumentNo) LIKE '")
				.append(text.toUpperCase()).append("'");
		}
		else if (m_columnName.equals("C_Invoice_ID"))
		{
			SQL.append("SELECT C_Invoice_ID FROM C_Invoice WHERE UPPER(DocumentNo) LIKE '")
				.append(text.toUpperCase()).append("'");
		}
		else if (m_columnName.equals("M_InOut_ID"))
		{
			SQL.append("SELECT M_InOut_ID FROM M_InOut WHERE UPPER(DocumentNo) LIKE '")
				.append(text.toUpperCase()).append("'");
		}
		else if (m_columnName.equals("C_Payment_ID"))
		{
			SQL.append("SELECT C_Payment_ID FROM C_Payment WHERE UPPER(DocumentNo) LIKE '")
				.append(text.toUpperCase()).append("'");
		}
		else
		{
			actionButton(text);
			return;
		}
		//	Finish SQL
		SQL.append(" AND IsActive='Y'");
		//	AddSecurity
		String tableName = m_columnName.substring(0, m_columnName.length()-3);
		String finalSQL = Access.addROAccessSQL(Env.getCtx(), SQL.toString(), tableName, false);
		int id = 0;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(finalSQL);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				id = rs.getInt(1);		//	first
				if (rs.next())
					id = -1;			//	only if unique
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("VLookup.actionText\nSQL=" + finalSQL, e);
			id = -2;
		}
		Log.trace(Log.l5_DData, "VLookup.actionText ID => " + id);

		//	No (unique) result
		if (id <= 0)
		{
			Log.trace(Log.l6_Database, finalSQL);
			actionButton(text);
			return;
		}
		m_value = null;     //  forces re-display if value is unchanged but text updated and still unique
		actionCombo (new Integer(id));          //  data binding
	}	//	actionText

	/**
	 *	Action - Special BPartner Screen
	 *  @param newRecord true if new record should be created
	 */
	private void actionBPartner (boolean newRecord)
	{
		VBPartner vbp = new VBPartner (Env.getFrame(this), m_WindowNo);
		int BPartner_ID = 0;
		//  if update, get current value
		if (!newRecord)
		{
			if (m_value instanceof Integer)
				BPartner_ID = ((Integer)m_value).intValue();
			else if (m_value != null)
				BPartner_ID = Integer.parseInt(m_value.toString());
		}

		vbp.loadBPartner (BPartner_ID);
		vbp.show();
		//  get result
		int result = vbp.getBPartner_ID();
		if (result == 0					//	0 = not saved
			&& result == BPartner_ID)	//	the same
			return;
		//  Maybe new BPartner - put in cache
		m_lookup.getDirect(new Integer(result), true);

		actionCombo (new Integer(result));      //  data binding
	}	//	actionBPartner

	/**
	 *	Action - Zoom
	 */
	private void actionZoom()
	{
		if (m_lookup == null)
			return;
		//
		String IsSOTrx = Env.getContext(Env.getCtx(), m_WindowNo, "IsSOTrx");
		int AD_Window_ID = m_lookup.getZoom(IsSOTrx);
		MQuery zoomQuery = m_lookup.getZoomQuery();
		Log.trace(Log.l1_User, "VLookup.actionZoom - " + AD_Window_ID + " - IsSOTrx=" + IsSOTrx + " - Query=" + zoomQuery + " - Value=" + getValue());
		//	If not already exist
		if (zoomQuery == null || (!zoomQuery.isActive() && getValue() != null))
		{
			zoomQuery = new MQuery();
			zoomQuery.addRestriction(m_columnName, MQuery.EQUAL, getValue());
		}
		//
		setCursor(Cursor.getDefaultCursor());
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		AWindow frame = new AWindow();
		if (!frame.initWindow(AD_Window_ID, zoomQuery, IsSOTrx.equals("Y")))
		{
			setCursor(Cursor.getDefaultCursor());
			ValueNamePair pp = Log.retrieveError();
			String msg = pp==null ? "AccessTableNoView" : pp.getValue();
			ADialog.error(m_WindowNo, this, msg, pp==null ? "" : pp.getName());
		}
		else
			AEnv.showCenterScreen(frame);
			//  async window - not able to get feedback
		frame = null;
		//
		setCursor(Cursor.getDefaultCursor());
	}	//	actionZoom

	/**
	 *	Action - Refresh
	 */
	private void actionRefresh()
	{
		if (m_lookup == null)
			return;
		//
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		Object obj = m_combo.getSelectedItem();
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + obj);
		m_lookup.refresh();
		if (m_lookup.isValidated())
			m_lookup.fillComboBox(isMandatory(), false, false, false);
		else
			m_lookup.fillComboBox(isMandatory(), true, false, false);
		m_combo.setSelectedItem(obj);
	//	m_combo.revalidate();
		//
		setCursor(Cursor.getDefaultCursor());
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + m_combo.getSelectedItem());
	}	//	actionRefresh

	/*************************************************************************/

	/**
	 *	Focus Listener for ComboBoxes with missing Validation or invalid entries
	 *	- Requery listener for updated list
	 *  @param e FocusEvent
	 */
	public void focusGained (FocusEvent e)
	{
		if (e.getSource() != m_combo || e.isTemporary() || m_haveFocus)
			return;
		//
		m_haveFocus = true;     //  prevents calling focus gained twice
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Object obj = m_lookup.getSelectedItem();
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Start    Count=" + m_combo.getItemCount() + ", Selected=" + obj);
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());
		m_lookup.fillComboBox(isMandatory(), true, true, true);     //  only validated & active & temporary
	//	Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Update   Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		m_lookup.setSelectedItem(obj);
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Selected Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		//
		m_settingFocus = false;
	}	//	focusGained

	/**
	 *	Reset Selection List
	 *  @param e FocusEvent
	 */
	public void focusLost(FocusEvent e)
	{
		if (e.getSource() != m_combo || e.isTemporary())
			return;
		//
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Log.trace(Log.l4_Data, "VLookup.focusLost", m_columnName + " = " + m_combo.getSelectedItem());
		Object obj = m_combo.getSelectedItem();
		//	set original model
		if (!m_lookup.isValidated())
			m_lookup.fillComboBox(true);    //  previous selection
		//	Set value
		if (obj != null)
		{
			m_combo.setSelectedItem(obj);
			//	original model may not have item
			if (!m_combo.getSelectedItem().equals(obj))
			{
				Log.trace(Log.l6_Database, "VLookup.focusLost " + m_columnName, "added to combo - " + obj);
				m_combo.addItem(obj);
				m_combo.setSelectedItem(obj);
			}
		}
	//	actionCombo(getValue());
		m_settingFocus = false;
		m_haveFocus = false;    //  can gain focus again
	}	//	focusLost

	/**
	 *  Set ToolTip
	 *  @param text tool tip text
	 */
	public void setToolTipText(String text)
	{
		super.setToolTipText(text);
		m_button.setToolTipText(text);
		m_text.setToolTipText(text);
		m_combo.setToolTipText(text);
	}   //  setToolTipText

}	//	VLookup

/*****************************************************************************/

/**
 *	Mouse Listener for Popup Menu
 */
final class VLookup_mouseAdapter extends java.awt.event.MouseAdapter
{
	/**
	 *	Constructor
	 *  @param adaptee adaptee
	 */
	VLookup_mouseAdapter(VLookup adaptee)
	{
		this.adaptee = adaptee;
	}	//	VLookup_mouseAdapter

	private VLookup adaptee;

	/**
	 *	Mouse Listener
	 *  @param e MouseEvent
	 */
	public void mouseClicked(MouseEvent e)
	{
	//	System.out.println("mouseClicked " + e.getID() + " " + e.getSource().getClass().toString());
		//	popup menu
		if (SwingUtilities.isRightMouseButton(e))
			adaptee.popupMenu.show((Component)e.getSource(), e.getX(), e.getY());
	}	//	mouse Clicked

}	//	VLookup_mouseAdapter
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
package org.compiere.grid.ed;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.text.*;
import java.util.*;
import java.math.*;
import java.beans.*;
import java.sql.*;

import org.compiere.apps.*;
import org.compiere.util.*;
import org.compiere.model.*;
import org.compiere.apps.search.*;
import org.compiere.plaf.*;
import org.compiere.swing.*;

/**
 *  Lookup Field.
 *  <p>
 *	    When r/o - display a Label
 *		When STABLE - display a ComboBox
 *		Otherwise show Selection Dialog
 *  <p>
 *  Sepecial handling of BPartner and Product
 *
 *  @author 	Jorg Janke
 *  @version 	$Id: VLookup.java,v 1.44 2003/08/12 17:58:30 jjanke Exp $
 */
public class VLookup extends JComponent
	implements VEditor, ActionListener, FocusListener
{
	/**
	 *  Create BPartner Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createBPartner (int WindowNo)
	{
		int AD_Column_ID = 3499;    //  C_Invoice.C_BPartner_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, AD_Column_ID, DisplayType.Search);
			return new VLookup ("C_BPartner_ID", false, false, true, lookup, DisplayType.Search, WindowNo);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createBPartner", e);
		}
		return null;
	}   //  createBPartner

	/**
	 *  Create Product Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createProduct (int WindowNo)
	{
		int AD_Column_ID = 3840;    //  C_InvoiceLine.M_Product_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, AD_Column_ID, DisplayType.Search);
			return new VLookup ("M_Product_ID", false, false, true, lookup, DisplayType.Search, WindowNo);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createProduct", e);
		}
		return null;
	}   //  createProduct

	/*************************************************************************/

	/**
	 *	IDE Default Constructor
	 */
	public VLookup()
	{
		this("Lookup", false, false, true, null, 0, 0);
	}	//	VLookup


	/**
	 *	Detail Constructor
	 *
	 *  @param columnName column
	 *  @param mandatory mandatory
	 *  @param isReadOnly read only
	 *  @param isUpdateable updateable
	 *  @param lookup lookup
	 *  @param displayType display type
	 *  @param WindowNo window no
	 */
	public VLookup (String columnName, boolean mandatory, boolean isReadOnly, boolean isUpdateable,
		Lookup lookup, int displayType, int WindowNo)
	{
		super();
		m_columnName = columnName;
		m_lookup = lookup;
		if (lookup == null)
			Log.trace(Log.l3_Util, "VLookup", "Lookup is NULL = " + columnName);
		setMandatory(mandatory);
		m_displayType = displayType;
		m_WindowNo = WindowNo;		//	for Info
		//
		setLayout(new BorderLayout());
		VLookup_mouseAdapter mouse = new VLookup_mouseAdapter(this);    //  popup

		//	***	Text & Button	***
		m_text.addActionListener(this);
		m_text.addMouseListener(mouse);
		//  Button
		m_button.addActionListener(this);
		m_button.addMouseListener(mouse);
		m_button.setFocusable(false);   //  don't focus when tabbing
		if (columnName.equals("C_BPartner_ID"))
			m_button.setIcon(Env.getImageIcon("BPartner10.gif"));
		else if (columnName.equals("M_Product_ID"))
			m_button.setIcon(Env.getImageIcon("Product10.gif"));
		else
			m_button.setIcon(Env.getImageIcon("PickOpen10.gif"));

		//	*** VComboBox	***
		if (m_lookup != null && displayType != DisplayType.Search)	//	No Search
		{
			//  Memory Leak after executing the next two lines ??
			m_lookup.fillComboBox (isMandatory(), false, false, false);
			m_combo.setModel(m_lookup);
			//
			m_combo.addActionListener(this);							//	Selection
			m_combo.addMouseListener(mouse);	                        //	popup
			//	FocusListener to refresh selection before opening
			if (!m_lookup.isValidated() || m_lookup.hasInactive())
				m_combo.addFocusListener(this);
		}

		setUI (true);
		//	ReadWrite	-	decides what components to show
		if (isReadOnly || !isUpdateable || m_lookup == null)
			setReadWrite(false);
		else
			setReadWrite(true);

		//	Popup
		if (m_lookup != null)
		{
			if ((displayType == DisplayType.List && Env.getContextAsInt(Env.getCtx(), "#AD_Role_ID") == 0)
				|| displayType != DisplayType.List)     //  only system admins can change lists, so no need to zoom for others
			{
				mZoom = new JMenuItem(Msg.getMsg(Env.getCtx(), "Zoom"), Env.getImageIcon("Zoom16.gif"));
				mZoom.addActionListener(this);
				popupMenu.add(mZoom);
			}
			mRefresh = new JMenuItem(Msg.getMsg(Env.getCtx(), "Refresh"), Env.getImageIcon("Refresh16.gif"));
			mRefresh.addActionListener(this);
			popupMenu.add(mRefresh);
		}
		//	VBPartner quick entry link
		if (columnName.equals("C_BPartner_ID"))
		{
			mBPartnerNew = new JMenuItem (Msg.getMsg(Env.getCtx(), "New"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerNew.addActionListener(this);
			popupMenu.add(mBPartnerNew);
			mBPartnerUpd = new JMenuItem (Msg.getMsg(Env.getCtx(), "Update"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerUpd.addActionListener(this);
			popupMenu.add(mBPartnerUpd);
		}
		//
		if (m_lookup != null && m_lookup.getZoom() == 0)
			mZoom.setEnabled(false);
	}	//	VLookup

	/**
	 *  Dispose
	 */
	public void dispose()
	{
		m_text = null;
		m_button = null;
		m_lookup = null;
		m_mField = null;
		m_combo.removeActionListener(this);
		m_combo.setModel(new DefaultComboBoxModel());    //  remove reference
	//	m_combo.removeAllItems();
		m_combo = null;
	}   //  dispose

	/** Display Length for Lookups (18)         */
	public final static int     DISPLAY_LENGTH = 18;

	/** Search: The Editable Text Field         */
	private CTextField 			m_text = new CTextField (DISPLAY_LENGTH);
	/** Search: The Button to open Editor   */
	private CButton				m_button = new CButton();
	/** The Combo Box if not a Search Lookup    */
	private VComboBox			m_combo = new VComboBox();
	/** Indicator that value is being set       */
	private volatile boolean 	m_settingValue = false;
	private volatile boolean 	m_settingFocus = false;
	/** Indicator that Lookup has focus         */
	private volatile boolean	m_haveFocus = false;
	/** Indicator - inserting new value			*/
	private volatile boolean	m_inserting = false;

	//
	private String				m_columnName;
	private Lookup				m_lookup;
	private int					m_displayType;
	private int					m_WindowNo;

	private boolean				m_comboActive = true;
	private Object				m_value;

	//	Popup
	JPopupMenu 					popupMenu = new JPopupMenu();
	private JMenuItem 			mZoom;
	private JMenuItem 			mRefresh;
	private JMenuItem			mBPartnerNew;
	private JMenuItem			mBPartnerUpd;

	private MField              m_mField = null;

	/**
	 *  Set Content and Size of Compoments
	 *  @param initial if true, size and margins will be set
	 */
	private void setUI (boolean initial)
	{
		if (initial)
		{
			Dimension size = m_text.getPreferredSize();
			setPreferredSize(new Dimension(size));  //	causes r/o to be the same length
			m_combo.setPreferredSize(new Dimension(size));
			//
			m_text.setBorder(null);
			Dimension bSize = new Dimension(size.height, size.height);
			m_button.setPreferredSize (bSize);
			m_button.setMargin(new Insets(0, 0, 0, 0));
		}

		//	What to show
		this.remove(m_combo);
		this.remove(m_button);
		this.remove(m_text);
		//
		if (!isReadWrite())									//	r/o - show text only
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			m_text.setReadWrite(false);
			m_combo.setReadWrite(false);
			m_comboActive = false;
		}
		else if (m_displayType != DisplayType.Search)	    //	show combo if not Search
		{
			this.setBorder(null);
			this.add(m_combo, BorderLayout.CENTER);
			m_comboActive = true;
		}
		else 												//	Search or unstable - show text & button
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			this.add(m_button, BorderLayout.EAST);
			m_text.setReadWrite (true);
			m_comboActive = false;
		}
	}   //  setUI

	/**
	 *	Set ReadWrite
	 *  @param value ReadWrite
	 */
	public void setReadWrite (boolean value)
	{
		boolean rw = value;
		if (m_lookup == null)
			rw = false;
		if (m_combo.isReadWrite() != value)
		{
			m_combo.setReadWrite(rw);
			setUI (false);
			if (m_comboActive)
				setValue (m_value);
		}
	}	//	setReadWrite

	/**
	 *	IsEditable
	 *  @return is lookup ReadWrite
	 */
	public boolean isReadWrite()
	{
		return m_combo.isReadWrite();
	}	//	isReadWrite

	/**
	 *	Set Mandatory (and back bolor)
	 *  @param mandatory mandatory
	 */
	public void setMandatory (boolean mandatory)
	{
		m_combo.setMandatory(mandatory);
		m_text.setMandatory(mandatory);
	}	//	setMandatory

	/**
	 *	Is it mandatory
	 *  @return true if mandatory
	 */
	public boolean isMandatory()
	{
		return m_combo.isMandatory();
	}	//	isMandatory

	/**
	 *	Set Background
	 *  @param color color
	 */
	public void setBackground(Color color)
	{
		m_text.setBackground(color);
		m_combo.setBackground(color);
	}	//	setBackground

	/**
	 *	Set Background
	 *  @param error error
	 */
	public void setBackground (boolean error)
	{
		m_text.setBackground(error);
		m_combo.setBackground(error);
	}	//	setBackground

	/**
	 *  Set Foreground
	 *  @param fg Foreground color
	 */
	public void setForeground(Color fg)
	{
		m_text.setForeground(fg);
		m_combo.setForeground(fg);
	}   //  setForeground

	/**
	 *  Set Editor to value
	 *  @param value new Value
	 */
	public void setValue (Object value)
	{
		Log.trace(Log.l6_Database, "VLookup.setValue", m_columnName + "=" + value);
		m_settingValue = true;		//	disable actions
		m_value = value;

		//	Set both for switching
		m_combo.setValue (value);
		if (value == null)
		{
			m_text.setText (null);
			m_settingValue = false;
			return;
		}
		if (m_lookup == null)
		{
			m_text.setText (value.toString());
			m_settingValue = false;
			return;
		}

		//	Set Display
		String display = m_lookup.getDisplay(value);
		boolean notFound = display.startsWith("<") && display.startsWith(">");
		m_text.setText (display);
		m_text.setCaretPosition (0); //	show beginning

		//	Nothing showing in Combo and should be showing
		if (m_combo.getSelectedItem() == null
			&& (m_comboActive || (m_inserting && m_displayType != DisplayType.Search)))
		{
			//  lookup found nothing too
			if (notFound)
			{
				Log.trace(8, "VLookup.setValue - Not found (1)", display);
				//  we may have a new value
				m_lookup.refresh();
				m_combo.setValue (value);
				display = m_lookup.getDisplay(value);
				m_text.setText (display);
				m_text.setCaretPosition (0);	//	show beginning
				notFound = display.startsWith("<") && display.endsWith(">");
			}
			if (notFound)	//	<key>
			{
				m_value = null;
				actionCombo (null);             //  data binding
				Log.trace(Log.l6_Database, "VLookup.setValue - not found - " + value);
			}
			//  we have lookup
			else if (m_combo.getSelectedItem() == null)
			{
				NamePair pp = m_lookup.get(value);
				if (pp != null)
				{
					Log.trace (Log.l6_Database, "VLookup.setValue - added to combo - " + pp);
					//  Add to Combo
					m_combo.addItem (pp);
					m_combo.setValue (value);
				}
			}
			//  Not in Lookup - set to Null
			if (m_combo.getSelectedItem() == null)
			{
				Log.trace(Log.l1_User, "VLookup.setValue - not in Lookup - set to NULL");
				actionCombo (null);             //  data binding (calls setValue again)
				m_value = null;
			}
		}
		m_settingValue = false;
	}	//	setValue

	/**
	 *  Property Change Listener
	 *  @param evt PropertyChangeEvent
	 */
	public void propertyChange (PropertyChangeEvent evt)
	{
	//	Log.trace(Log.l5_DData, "VLookup.propertyChange", evt);
		if (evt.getPropertyName().equals(MField.PROPERTY))
		{
			m_inserting = MField.INSERTING.equals(evt.getOldValue());
			setValue(evt.getNewValue());
			m_inserting = false;
		}
	}   //  propertyChange

	/**
	 *	Return Editor value (Integer)
	 *  @return value
	 */
	public Object getValue()
	{
		if (m_comboActive)
			return m_combo.getValue ();
		return m_value;
	}	//	getValue

	/**
	 *  Return editor display
	 *  @return display value
	 */
	public String getDisplay()
	{
		String retValue = null;
		if (m_comboActive)
			retValue = m_combo.getDisplay();
		//  check lookup
		else if (m_lookup == null)
			retValue = m_value.toString();
		else
			retValue = m_lookup.getDisplay(m_value);
	//	Log.trace(Log.l6_Database, "VLookup.getDisplay - " + retValue, "ComboActive=" + m_comboActive);
		return retValue;
	}   //  getDisplay

	/**
	 *  Set Field/WindowNo for ValuePreference
	 *  @param mField Model Field for Lookup
	 */
	public void setField (MField mField)
	{
		m_mField = mField;
		if (m_mField != null)
			ValuePreference.addMenu (this, popupMenu);
	}   //  setField

	/*************************************************************************/

	/**
	 *	Action Listener	- data binding
	 *  @param e ActionEvent
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (m_settingValue || m_settingFocus)
			return;
		Log.trace(Log.l4_Data, "VLookup.actionPerformed", e.getActionCommand() + ", ComboValue=" + m_combo.getSelectedItem());
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());

		//  Preference
		if (e.getActionCommand().equals(ValuePreference.NAME))
		{
			ValuePreference.start (m_mField, getValue(), getDisplay());
			return;
		}

		//  Combo Selection
		else if (e.getSource() == m_combo)
		{
			Object value = getValue();
			Object o = m_combo.getSelectedItem();
			if (o != null)
			{
				String s = o.toString();
				//  don't allow selection of inactive
				if (s.startsWith(MLookup.INACTIVE_S) && s.endsWith(MLookup.INACTIVE_E))
				{
					Log.trace(Log.l1_User, "VLookup.actionPerformed - Selection inactive set to NULL");
					value = null;
				}
			}
			actionCombo (value);                //  data binding
		}
		//  Button pressed
		else if (e.getSource() == m_button)
			actionButton ("");
		//  Text entered
		else if (e.getSource() == m_text)
			actionText();

		//  Popup Menu
		else if (e.getSource() == mZoom)
			actionZoom();
		else if (e.getSource() == mRefresh)
			actionRefresh();
		else if (e.getSource() == mBPartnerNew)
			actionBPartner(true);
		else if (e.getSource() == mBPartnerUpd)
			actionBPartner(false);
	}	//	actionPerformed

	/**
	 *  Action Listener Interface
	 *  @param listener listener
	 */
	public void addActionListener(ActionListener listener)
	{
		m_combo.addActionListener(listener);
		m_text.addActionListener(listener);
	}   //  addActionListener

	/**
	 *	Action - Combo.
	 *  <br>
	 *	== dataBinding == inform of new value
	 *  <pre>
	 *  VLookup.actionCombo
	 *      GridController.vetoableChange
	 *          MTable.setValueAt
	 *              MField.setValue
	 *                  VLookup.setValue
	 *          MTab.dataStatusChanged
	 *  </pre>
	 *  @param value new value
	 */
	private void actionCombo (Object value)
	{
	//	Log.trace(Log.l6_Database, "VLookup.actionCombo", value==null ? "null" : value.toString());
		try
		{
			fireVetoableChange (m_columnName, null, value);
		}
		catch (PropertyVetoException pve)
		{
			Log.error("VLookup.actionCombo", pve);
		}
		//  is the value updated ?
		boolean updated = false;
		if (value == null && m_value == null)
			updated = true;
		else if (value != null && value.equals(m_value))
			updated = true;
		if (!updated)
		{
			//  happens if VLookup is used outside of APanel/GridController (no property listener)
			Log.trace(Log.l6_Database, "VLookup.actionCombo - Value explicitly set - new=" + value + ", old=" + m_value);
			setValue(value);
		}
	}	//	actionCombo


	/**
	 *	Action - Button.
	 *	- Call Info
	 *	@param queryValue initial query value
	 */
	private void actionButton (String queryValue)
	{
		m_button.setEnabled(false);                 //  disable double click
		requestFocus();                             //  closes other editors
		Frame frame = Env.getFrame(this);

		/**
		 *  Three return options:
		 *  - Value Selected & OK pressed   => store result => result has value
		 *  - Cancel pressed                => store null   => result == null && cancelled
		 *  - Window closed                 -> ignore       => result == null && !cancalled
		 */
		Object result = null;
		boolean cancelled = false;
		//
		String col = m_lookup.getColumnName();		//	fully qualified name
		if (col.indexOf(".") != -1)
			col = col.substring(col.indexOf(".")+1);
		//  Zoom / Validation
		String whereClause = "";
		if (m_lookup.getZoomQuery() != null)
			whereClause = m_lookup.getZoomQuery().getWhereClause();
		if (whereClause.length() == 0)
			whereClause = m_lookup.getValidation();
		//
		Log.trace(Log.l5_DData, "VLookup.actionButton - " + col + ", Zoom=" + m_lookup.getZoom()
			+ " (" + whereClause + ")");
		//
		boolean resetValue = false;             //  reset value so that is always treated as new entry
		if (col.equals("M_Product_ID"))
		{
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = "@" + m_text.getText() + "@";   //  Name indicator - otherwise Value
			int M_Warehouse_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNo, "M_Warehouse_ID");
			int M_PriceList_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNo, "M_PriceList_ID");
			InfoProduct ip = new InfoProduct (frame, true, m_WindowNo,
				M_Warehouse_ID, M_PriceList_ID, queryValue, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
			resetValue = true;
		}
		else if (col.equals("C_BPartner_ID"))
		{
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = m_text.getText();
			boolean isSOTrx = true;     //  default
			if (Env.getContext(Env.getCtx(), m_WindowNo, "IsSOTrx").equals("N"))
				isSOTrx = false;
			InfoBPartner ip = new InfoBPartner (frame, true, m_WindowNo,
				queryValue, isSOTrx, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
		}
		else
		{
			String tableName = col;
			int index = m_columnName.indexOf("_ID");
			if (index != -1)
				tableName = m_columnName.substring(0, index);
			Info ig = Info.create (frame, true, m_WindowNo, tableName, m_columnName, queryValue, false, whereClause);
			ig.show();
			cancelled = ig.isCancelled();
			result = ig.getSelectedKey();
		}
		//  Result
		if (result != null)
		{
			Log.trace(Log.l4_Data, "VLookupactionButton", "Result = " + result.toString() + " " + result.getClass().getName());
			//  make sure that value is in cache
			m_lookup.getDirect(result, true);
			if (resetValue)
				actionCombo (null);
			actionCombo (result);
		}
		else if (cancelled)
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (cancelled)");
			actionCombo (null);
		}
		else
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (not cancelled)");
			setValue(m_value);      //  to re-display value
		}
		//
		m_button.setEnabled(true);
	}	//	actionButton

	/**
	 *	Check, if data returns unique entry, otherwise involve Info via Button
	 */
	private void actionText()
	{
		String text = m_text.getText();
		//	Nothing entered
		if (text == null || text.length() == 0 || text.equals("%"))
		{
			actionButton(text);
			return;
		}
		//	Always like
		if (!text.endsWith("%"))
			text += "%";
		text = text.toUpperCase();

		StringBuffer SQL = new StringBuffer();
		if (m_columnName.equals("M_Product_ID"))
		{
			SQL.append("SELECT M_Product_ID FROM M_Product WHERE UPPER(Value) LIKE ")
				.append(DB.TO_STRING(text))
				.append(" OR UPC LIKE ").append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_BPartner_ID"))
		{
			SQL.append("SELECT C_BPartner_ID FROM C_BPartner WHERE UPPER(Value) LIKE ")
				.append(DB.TO_STRING(text))
				.append(" OR UPPER(Name) LIKE ").append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_Order_ID"))
		{
			SQL.append("SELECT C_Order_ID FROM C_Order WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_Invoice_ID"))
		{
			SQL.append("SELECT C_Invoice_ID FROM C_Invoice WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("M_InOut_ID"))
		{
			SQL.append("SELECT M_InOut_ID FROM M_InOut WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_Payment_ID"))
		{
			SQL.append("SELECT C_Payment_ID FROM C_Payment WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("GL_JournalBatch_ID"))
		{
			SQL.append("SELECT GL_JournalBatch_ID FROM GL_JournalBatch WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else
		{
			SQL = getDirectAccessSQL (m_columnName, text);
			if (SQL.length() == 0)
			{
				actionButton (text);
				return;
			}
		}
		//	Finish SQL
		SQL.append(" AND IsActive='Y'");
		//	AddSecurity
		String tableName = m_columnName.substring(0, m_columnName.length()-3);
		String finalSQL = Access.addROAccessSQL(Env.getCtx(), SQL.toString(), tableName, false);
		int id = 0;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(finalSQL);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				id = rs.getInt(1);		//	first
				if (rs.next())
					id = -1;			//	only if unique
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("VLookup.actionText\nSQL=" + finalSQL, e);
			id = -2;
		}
		Log.trace(Log.l5_DData, "VLookup.actionText ID => " + id);

		//	No (unique) result
		if (id <= 0)
		{
			Log.trace(Log.l6_Database, finalSQL);
			actionButton(text);
			return;
		}
		m_value = null;     //  forces re-display if value is unchanged but text updated and still unique
		actionCombo (new Integer(id));          //  data binding
	}	//	actionText


	/**
	 * 	Generate Access SQL
	 *	@param columnName column
	 *	@param text upper like text
	 *	@return sql or ""
	 */
	private StringBuffer getDirectAccessSQL (String columnName, String text)
	{
		StringBuffer sb = new StringBuffer();
		String TableName = null;
		String sql = "SELECT t.TableName, c.ColumnName "
			+ "FROM AD_Column c "
			+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID AND t.IsView='N') "
			+ "WHERE c.ColumnName IN ('DocumentNo', 'Value')"
			+ " AND EXISTS (SELECT * FROM AD_Column cc WHERE cc.AD_Table_ID=t.AD_Table_ID"
				+ " AND cc.IsKey='Y' AND cc.ColumnName=?)";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql);
			pstmt.setString(1, columnName);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				if (sb.length() != 0)
					sb.append(" OR ");
				TableName = rs.getString(1);
				sb.append("UPPER(").append(rs.getString(2)).append(") LIKE ").append(DB.TO_STRING(text));
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException ex)
		{
			Log.error("", ex);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
		}
		catch (SQLException ex1)
		{
		}
		pstmt = null;
		if (TableName == null || sb.length() == 0)
			return sb;
		StringBuffer retValue = new StringBuffer ("SELECT ")
			.append(TableName).append(" FROM ").append(TableName)
			.append(" WHERE ").append(sb);
		return retValue;
	}	//	getDirectAccessSQL


	/**
	 *	Action - Special BPartner Screen
	 *  @param newRecord true if new record should be created
	 */
	private void actionBPartner (boolean newRecord)
	{
		VBPartner vbp = new VBPartner (Env.getFrame(this), m_WindowNo);
		int BPartner_ID = 0;
		//  if update, get current value
		if (!newRecord)
		{
			if (m_value instanceof Integer)
				BPartner_ID = ((Integer)m_value).intValue();
			else if (m_value != null)
				BPartner_ID = Integer.parseInt(m_value.toString());
		}

		vbp.loadBPartner (BPartner_ID);
		vbp.show();
		//  get result
		int result = vbp.getBPartner_ID();
		if (result == 0					//	0 = not saved
			&& result == BPartner_ID)	//	the same
			return;
		//  Maybe new BPartner - put in cache
		m_lookup.getDirect(new Integer(result), true);

		actionCombo (new Integer(result));      //  data binding
	}	//	actionBPartner

	/**
	 *	Action - Zoom
	 */
	private void actionZoom()
	{
		if (m_lookup == null)
			return;
		//
		String IsSOTrx = Env.getContext(Env.getCtx(), m_WindowNo, "IsSOTrx");
		int AD_Window_ID = m_lookup.getZoom(IsSOTrx);
		MQuery zoomQuery = m_lookup.getZoomQuery();
		Log.trace(Log.l1_User, "VLookup.actionZoom - " + AD_Window_ID + " - IsSOTrx=" + IsSOTrx + " - Query=" + zoomQuery + " - Value=" + getValue());
		//	If not already exist
		if (zoomQuery == null || (!zoomQuery.isActive() && getValue() != null))
		{
			zoomQuery = new MQuery();
			zoomQuery.addRestriction(m_columnName, MQuery.EQUAL, getValue());
		}
		//
		setCursor(Cursor.getDefaultCursor());
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		AWindow frame = new AWindow();
		if (!frame.initWindow(AD_Window_ID, zoomQuery, IsSOTrx.equals("Y")))
		{
			setCursor(Cursor.getDefaultCursor());
			ValueNamePair pp = Log.retrieveError();
			String msg = pp==null ? "AccessTableNoView" : pp.getValue();
			ADialog.error(m_WindowNo, this, msg, pp==null ? "" : pp.getName());
		}
		else
			AEnv.showCenterScreen(frame);
			//  async window - not able to get feedback
		frame = null;
		//
		setCursor(Cursor.getDefaultCursor());
	}	//	actionZoom

	/**
	 *	Action - Refresh
	 */
	private void actionRefresh()
	{
		if (m_lookup == null)
			return;
		//
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		Object obj = m_combo.getSelectedItem();
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + obj);
		m_lookup.refresh();
		if (m_lookup.isValidated())
			m_lookup.fillComboBox(isMandatory(), false, false, false);
		else
			m_lookup.fillComboBox(isMandatory(), true, false, false);
		m_combo.setSelectedItem(obj);
	//	m_combo.revalidate();
		//
		setCursor(Cursor.getDefaultCursor());
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + m_combo.getSelectedItem());
	}	//	actionRefresh

	/*************************************************************************/

	/**
	 *	Focus Listener for ComboBoxes with missing Validation or invalid entries
	 *	- Requery listener for updated list
	 *  @param e FocusEvent
	 */
	public void focusGained (FocusEvent e)
	{
		if (e.getSource() != m_combo || e.isTemporary() || m_haveFocus)
			return;
		//
		m_haveFocus = true;     //  prevents calling focus gained twice
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Object obj = m_lookup.getSelectedItem();
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Start    Count=" + m_combo.getItemCount() + ", Selected=" + obj);
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());
		m_lookup.fillComboBox(isMandatory(), true, true, true);     //  only validated & active & temporary
	//	Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Update   Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		m_lookup.setSelectedItem(obj);
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Selected Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		//
		m_settingFocus = false;
	}	//	focusGained

	/**
	 *	Reset Selection List
	 *  @param e FocusEvent
	 */
	public void focusLost(FocusEvent e)
	{
		if (e.getSource() != m_combo || e.isTemporary())
			return;
		//
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Log.trace(Log.l4_Data, "VLookup.focusLost", m_columnName + " = " + m_combo.getSelectedItem());
		Object obj = m_combo.getSelectedItem();
		//	set original model
		if (!m_lookup.isValidated())
			m_lookup.fillComboBox(true);    //  previous selection
		//	Set value
		if (obj != null)
		{
			m_combo.setSelectedItem(obj);
			//	original model may not have item
			if (!m_combo.getSelectedItem().equals(obj))
			{
				Log.trace(Log.l6_Database, "VLookup.focusLost " + m_columnName, "added to combo - " + obj);
				m_combo.addItem(obj);
				m_combo.setSelectedItem(obj);
			}
		}
	//	actionCombo(getValue());
		m_settingFocus = false;
		m_haveFocus = false;    //  can gain focus again
	}	//	focusLost

	/**
	 *  Set ToolTip
	 *  @param text tool tip text
	 */
	public void setToolTipText(String text)
	{
		super.setToolTipText(text);
		m_button.setToolTipText(text);
		m_text.setToolTipText(text);
		m_combo.setToolTipText(text);
	}   //  setToolTipText

}	//	VLookup

/*****************************************************************************/

/**
 *	Mouse Listener for Popup Menu
 */
final class VLookup_mouseAdapter extends java.awt.event.MouseAdapter
{
	/**
	 *	Constructor
	 *  @param adaptee adaptee
	 */
	VLookup_mouseAdapter(VLookup adaptee)
	{
		this.adaptee = adaptee;
	}	//	VLookup_mouseAdapter

	private VLookup adaptee;

	/**
	 *	Mouse Listener
	 *  @param e MouseEvent
	 */
	public void mouseClicked(MouseEvent e)
	{
	//	System.out.println("mouseClicked " + e.getID() + " " + e.getSource().getClass().toString());
		//	popup menu
		if (SwingUtilities.isRightMouseButton(e))
			adaptee.popupMenu.show((Component)e.getSource(), e.getX(), e.getY());
	}	//	mouse Clicked

}	//	VLookup_mouseAdapter
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
package org.compiere.grid.ed;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;
import java.sql.*;

import org.compiere.apps.*;
import org.compiere.util.*;
import org.compiere.model.*;
import org.compiere.apps.search.*;
import org.compiere.swing.*;

/**
 *  Lookup Field.
 *  <p>
 *	    When r/o - display a Label
 *		When STABLE - display a ComboBox
 *		Otherwise show Selection Dialog
 *  <p>
 *  Sepecial handling of BPartner and Product
 *
 *  @author 	Jorg Janke
 *  @version 	$Id: VLookup.java,v 1.65 2004/05/14 05:35:01 jjanke Exp $
 */
public class VLookup extends JComponent
	implements VEditor, ActionListener, FocusListener
{
	/**
	 *  Create Optional BPartner Search Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createBPartner (int WindowNo)
	{
		int AD_Column_ID = 3499;    //  C_Invoice.C_BPartner_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, 0, AD_Column_ID, DisplayType.Search);
			return new VLookup ("C_BPartner_ID", false, false, true, lookup);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createBPartner", e);
		}
		return null;
	}   //  createBPartner

	/**
	 *  Create Optional Product Search Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createProduct (int WindowNo)
	{
		int AD_Column_ID = 3840;    //  C_InvoiceLine.M_Product_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, 0, AD_Column_ID, DisplayType.Search);
			return new VLookup ("M_Product_ID", false, false, true, lookup);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createProduct", e);
		}
		return null;
	}   //  createProduct

	/**
	 *  Create Optional User Search Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createUser (int WindowNo)
	{
		int AD_Column_ID = 10443;    //  AD_WF_Activity.AD_User_UD
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, 0, AD_Column_ID, DisplayType.Search);
			return new VLookup ("AD_User_ID", false, false, true, lookup);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createUser", e);
		}
		return null;
	}   //  createProduct

	
	/*************************************************************************
	 *	Detail Constructor
	 *
	 *  @param columnName column
	 *  @param mandatory mandatory
	 *  @param isReadOnly read only
	 *  @param isUpdateable updateable
	 *  @param lookup lookup
	 */
	public VLookup (String columnName, boolean mandatory, boolean isReadOnly, boolean isUpdateable,
		Lookup lookup)
	{
		super();
		super.setName(columnName);
		m_combo.setName(columnName);
		m_columnName = columnName;
		setMandatory(mandatory);
		m_lookup = lookup;
		//
		setLayout(new BorderLayout());
		VLookup_mouseAdapter mouse = new VLookup_mouseAdapter(this);    //  popup

		//	***	Text & Button	***
		m_text.addActionListener(this);
		m_text.addFocusListener(this);
		m_text.addMouseListener(mouse);
		//  Button
		m_button.addActionListener(this);
		m_button.addMouseListener(mouse);
		m_button.setFocusable(false);   //  don't focus when tabbing
		if (columnName.equals("C_BPartner_ID"))
			m_button.setIcon(Env.getImageIcon("BPartner10.gif"));
		else if (columnName.equals("M_Product_ID"))
			m_button.setIcon(Env.getImageIcon("Product10.gif"));
		else
			m_button.setIcon(Env.getImageIcon("PickOpen10.gif"));

		//	*** VComboBox	***
		if (m_lookup != null && m_lookup.getDisplayType() != DisplayType.Search)	//	No Search
		{
			//  Memory Leak after executing the next two lines ??
			m_lookup.fillComboBox (isMandatory(), false, false, false);
			m_combo.setModel(m_lookup);
			//
			m_combo.addActionListener(this);							//	Selection
			m_combo.addMouseListener(mouse);	                        //	popup
			//	FocusListener to refresh selection before opening
			m_combo.addFocusListener(this);
		}

		setUI (true);
		//	ReadWrite	-	decides what components to show
		if (isReadOnly || !isUpdateable || m_lookup == null)
			setReadWrite(false);
		else
			setReadWrite(true);

		//	Popup
		if (m_lookup != null)
		{
			if ((m_lookup.getDisplayType() == DisplayType.List && Env.getContextAsInt(Env.getCtx(), "#AD_Role_ID") == 0)
				|| m_lookup.getDisplayType() != DisplayType.List)     //  only system admins can change lists, so no need to zoom for others
			{
				mZoom = new JMenuItem(Msg.getMsg(Env.getCtx(), "Zoom"), Env.getImageIcon("Zoom16.gif"));
				mZoom.addActionListener(this);
				popupMenu.add(mZoom);
			}
			mRefresh = new JMenuItem(Msg.getMsg(Env.getCtx(), "Refresh"), Env.getImageIcon("Refresh16.gif"));
			mRefresh.addActionListener(this);
			popupMenu.add(mRefresh);
		}
		//	VBPartner quick entry link
		if (columnName.equals("C_BPartner_ID"))
		{
			mBPartnerNew = new JMenuItem (Msg.getMsg(Env.getCtx(), "New"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerNew.addActionListener(this);
			popupMenu.add(mBPartnerNew);
			mBPartnerUpd = new JMenuItem (Msg.getMsg(Env.getCtx(), "Update"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerUpd.addActionListener(this);
			popupMenu.add(mBPartnerUpd);
		}
		//
		if (m_lookup != null && m_lookup.getZoom() == 0)
			mZoom.setEnabled(false);
	}	//	VLookup

	/**
	 *  Dispose
	 */
	public void dispose()
	{
		m_text = null;
		m_button = null;
		m_lookup = null;
		m_mField = null;
		//
		m_combo.removeFocusListener(this);
		m_combo.removeActionListener(this);
		m_combo.setModel(new DefaultComboBoxModel());    //  remove reference
	//	m_combo.removeAllItems();
		m_combo = null;
	}   //  dispose

	/** Display Length for Lookups (15)         */
	public final static int     DISPLAY_LENGTH = 15;

	/** Search: The Editable Text Field         */
	private CTextField 			m_text = new CTextField (DISPLAY_LENGTH);
	/** Search: The Button to open Editor   */
	private CButton				m_button = new CButton();
	/** The Combo Box if not a Search Lookup    */
	private VComboBox			m_combo = new VComboBox();
	/** Indicator that value is being set       */
	private volatile boolean 	m_settingValue = false;
	private volatile boolean 	m_settingFocus = false;
	/** Indicator that Lookup has focus         */
	private volatile boolean	m_haveFocus = false;
	/** Indicator - inserting new value			*/
	private volatile boolean	m_inserting = false;
	/** Last Display							*/
	private String				m_lastDisplay = "";

	//
	private String				m_columnName;
	private Lookup				m_lookup;

	private boolean				m_comboActive = true;
	private Object				m_value;

	//	Popup
	JPopupMenu 					popupMenu = new JPopupMenu();
	private JMenuItem 			mZoom;
	private JMenuItem 			mRefresh;
	private JMenuItem			mBPartnerNew;
	private JMenuItem			mBPartnerUpd;

	private MField              m_mField = null;

	/**
	 *  Set Content and Size of Compoments
	 *  @param initial if true, size and margins will be set
	 */
	private void setUI (boolean initial)
	{
		if (initial)
		{
			Dimension size = m_text.getPreferredSize();
			setPreferredSize(new Dimension(size));  //	causes r/o to be the same length
			m_combo.setPreferredSize(new Dimension(size));
			setMinimumSize(new Dimension (30,size.height));
			//
			m_text.setBorder(null);
			Dimension bSize = new Dimension(size.height, size.height);
			m_button.setPreferredSize (bSize);
			m_button.setMargin(new Insets(0, 0, 0, 0));
		}

		//	What to show
		this.remove(m_combo);
		this.remove(m_button);
		this.remove(m_text);
		//
		if (!isReadWrite())									//	r/o - show text only
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			m_text.setReadWrite(false);
			m_combo.setReadWrite(false);
			m_comboActive = false;
		}
		else if (m_lookup != null && m_lookup.getDisplayType() != DisplayType.Search)	    //	show combo if not Search
		{
			this.setBorder(null);
			this.add(m_combo, BorderLayout.CENTER);
			m_comboActive = true;
		}
		else 												//	Search or unstable - show text & button
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			this.add(m_button, BorderLayout.EAST);
			m_text.setReadWrite (true);
			m_comboActive = false;
		}
	}   //  setUI

	/**
	 *	Set ReadWrite
	 *  @param value ReadWrite
	 */
	public void setReadWrite (boolean value)
	{
		boolean rw = value;
		if (m_lookup == null)
			rw = false;
		if (m_combo.isReadWrite() != value)
		{
			m_combo.setReadWrite(rw);
			setUI (false);
			if (m_comboActive)
				setValue (m_value);
		}
	}	//	setReadWrite

	/**
	 *	IsEditable
	 *  @return is lookup ReadWrite
	 */
	public boolean isReadWrite()
	{
		return m_combo.isReadWrite();
	}	//	isReadWrite

	/**
	 *	Set Mandatory (and back bolor)
	 *  @param mandatory mandatory
	 */
	public void setMandatory (boolean mandatory)
	{
		m_combo.setMandatory(mandatory);
		m_text.setMandatory(mandatory);
	}	//	setMandatory

	/**
	 *	Is it mandatory
	 *  @return true if mandatory
	 */
	public boolean isMandatory()
	{
		return m_combo.isMandatory();
	}	//	isMandatory

	/**
	 *	Set Background
	 *  @param color color
	 */
	public void setBackground(Color color)
	{
		m_text.setBackground(color);
		m_combo.setBackground(color);
	}	//	setBackground

	/**
	 *	Set Background
	 *  @param error error
	 */
	public void setBackground (boolean error)
	{
		m_text.setBackground(error);
		m_combo.setBackground(error);
	}	//	setBackground

	/**
	 *  Set Foreground
	 *  @param fg Foreground color
	 */
	public void setForeground(Color fg)
	{
		m_text.setForeground(fg);
		m_combo.setForeground(fg);
	}   //  setForeground

	/**
	 *  Set Editor to value
	 *  @param value new Value
	 */
	public void setValue (Object value)
	{
		Log.trace(Log.l6_Database, "VLookup.setValue", m_columnName + "=" + value);
		m_settingValue = true;		//	disable actions
		m_value = value;

		//	Set both for switching
		m_combo.setValue (value);
		if (value == null)
		{
			m_text.setText (null);
			m_lastDisplay = "";
			m_settingValue = false;
			return;
		}
		if (m_lookup == null)
		{
			m_text.setText (value.toString());
			m_lastDisplay = value.toString();
			m_settingValue = false;
			return;
		}

		//	Set Display
		m_lastDisplay = m_lookup.getDisplay(value);
		if (m_lastDisplay.equals("<-1>"))
		{
			m_lastDisplay = "";
			m_value = null;
		}
		boolean notFound = m_lastDisplay.startsWith("<") && m_lastDisplay.startsWith(">");
		m_text.setText (m_lastDisplay);
		m_text.setCaretPosition (0); //	show beginning

		//	Nothing showing in Combo and should be showing
		if (m_combo.getSelectedItem() == null
			&& (m_comboActive || (m_inserting && m_lookup.getDisplayType() != DisplayType.Search)))
		{
			//  lookup found nothing too
			if (notFound)
			{
				Log.trace(8, "VLookup.setValue - Not found (1)", m_lastDisplay);
				//  we may have a new value
				m_lookup.refresh();
				m_combo.setValue (value);
				m_lastDisplay = m_lookup.getDisplay(value);
				m_text.setText (m_lastDisplay);
				m_text.setCaretPosition (0);	//	show beginning
				notFound = m_lastDisplay.startsWith("<") && m_lastDisplay.endsWith(">");
			}
			if (notFound)	//	<key>
			{
				m_value = null;
				actionCombo (null);             //  data binding
				Log.trace(Log.l6_Database, "VLookup.setValue - not found - " + value);
			}
			//  we have lookup
			else if (m_combo.getSelectedItem() == null)
			{
				NamePair pp = m_lookup.get(value);
				if (pp != null)
				{
					Log.trace (Log.l6_Database, "VLookup.setValue - added to combo - " + pp);
					//  Add to Combo
					m_combo.addItem (pp);
					m_combo.setValue (value);
				}
			}
			//  Not in Lookup - set to Null
			if (m_combo.getSelectedItem() == null)
			{
				Log.trace(Log.l1_User, "VLookup.setValue - not in Lookup - set to NULL");
				actionCombo (null);             //  data binding (calls setValue again)
				m_value = null;
			}
		}
		m_settingValue = false;
	}	//	setValue

	/**
	 *  Property Change Listener
	 *  @param evt PropertyChangeEvent
	 */
	public void propertyChange (PropertyChangeEvent evt)
	{
	//	Log.trace(Log.l5_DData, "VLookup.propertyChange", evt);
		if (evt.getPropertyName().equals(MField.PROPERTY))
		{
			m_inserting = MField.INSERTING.equals(evt.getOldValue());	//	MField.setValue
			setValue(evt.getNewValue());
			m_inserting = false;
		}
	}   //  propertyChange

	/**
	 *	Return Editor value (Integer)
	 *  @return value
	 */
	public Object getValue()
	{
		if (m_comboActive)
			return m_combo.getValue ();
		return m_value;
	}	//	getValue

	/**
	 *  Return editor display
	 *  @return display value
	 */
	public String getDisplay()
	{
		String retValue = null;
		if (m_comboActive)
			retValue = m_combo.getDisplay();
		//  check lookup
		else if (m_lookup == null)
			retValue = m_value.toString();
		else
			retValue = m_lookup.getDisplay(m_value);
	//	Log.trace(Log.l6_Database, "VLookup.getDisplay - " + retValue, "ComboActive=" + m_comboActive);
		return retValue;
	}   //  getDisplay

	/**
	 *  Set Field/WindowNo for ValuePreference
	 *  @param mField Model Field for Lookup
	 */
	public void setField (MField mField)
	{
		m_mField = mField;
		if (m_mField != null)
			ValuePreference.addMenu (this, popupMenu);
	}   //  setField

	/*************************************************************************/

	/**
	 *	Action Listener	- data binding
	 *  @param e ActionEvent
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (m_settingValue || m_settingFocus)
			return;
		Log.trace(Log.l4_Data, "VLookup.actionPerformed", e.getActionCommand() + ", ComboValue=" + m_combo.getSelectedItem());
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());

		//  Preference
		if (e.getActionCommand().equals(ValuePreference.NAME))
		{
			ValuePreference.start (m_mField, getValue(), getDisplay());
			return;
		}

		//  Combo Selection
		else if (e.getSource() == m_combo)
		{
			Object value = getValue();
			Object o = m_combo.getSelectedItem();
			if (o != null)
			{
				String s = o.toString();
				//  don't allow selection of inactive
				if (s.startsWith(MLookup.INACTIVE_S) && s.endsWith(MLookup.INACTIVE_E))
				{
					Log.trace(Log.l1_User, "VLookup.actionPerformed - Selection inactive set to NULL");
					value = null;
				}
			}
			actionCombo (value);                //  data binding
		}
		//  Button pressed
		else if (e.getSource() == m_button)
			actionButton ("");
		//  Text entered
		else if (e.getSource() == m_text)
			actionText();

		//  Popup Menu
		else if (e.getSource() == mZoom)
			actionZoom();
		else if (e.getSource() == mRefresh)
			actionRefresh();
		else if (e.getSource() == mBPartnerNew)
			actionBPartner(true);
		else if (e.getSource() == mBPartnerUpd)
			actionBPartner(false);
	}	//	actionPerformed

	/**
	 *  Action Listener Interface
	 *  @param listener listener
	 */
	public void addActionListener(ActionListener listener)
	{
		m_combo.addActionListener(listener);
		m_text.addActionListener(listener);
	}   //  addActionListener

	/**
	 *	Action - Combo.
	 *  <br>
	 *	== dataBinding == inform of new value
	 *  <pre>
	 *  VLookup.actionCombo
	 *      GridController.vetoableChange
	 *          MTable.setValueAt
	 *              MField.setValue
	 *                  VLookup.setValue
	 *          MTab.dataStatusChanged
	 *  </pre>
	 *  @param value new value
	 */
	private void actionCombo (Object value)
	{
		Log.trace(Log.l6_Database, "VLookup.actionCombo", value==null ? "null" : value.toString());
		try
		{
			fireVetoableChange (m_columnName, null, value);
		}
		catch (PropertyVetoException pve)
		{
			Log.error("VLookup.actionCombo", pve);
		}
		//  is the value updated ?
		boolean updated = false;
		if (value == null && m_value == null)
			updated = true;
		else if (value != null && value.equals(m_value))
			updated = true;
		if (!updated)
		{
			//  happens if VLookup is used outside of APanel/GridController (no property listener)
			Log.trace(Log.l6_Database, "VLookup.actionCombo",
				"Value explicitly set - new=" + value + ", old=" + m_value);
			setValue(value);
		}
	}	//	actionCombo


	/**
	 *	Action - Button.
	 *	- Call Info
	 *	@param queryValue initial query value
	 */
	private void actionButton (String queryValue)
	{
		m_button.setEnabled(false);                 //  disable double click
		if (m_lookup == null)
			return;		//	leave button disabled
		this.requestFocus();						//  closes other editors
		Frame frame = Env.getFrame(this);

		/**
		 *  Three return options:
		 *  - Value Selected & OK pressed   => store result => result has value
		 *  - Cancel pressed                => store null   => result == null && cancelled
		 *  - Window closed                 -> ignore       => result == null && !cancalled
		 */
		Object result = null;
		boolean cancelled = false;
		//
		String col = m_lookup.getColumnName();		//	fully qualified name
		if (col.indexOf(".") != -1)
			col = col.substring(col.indexOf(".")+1);
		//  Zoom / Validation
		String whereClause = getWhereClause();
		//
		Log.trace(Log.l5_DData, "VLookup.actionButton - " + col 
			+ ", Zoom=" + m_lookup.getZoom()
			+ " (" + whereClause + ")");
		//
		boolean resetValue = false;	//	reset value so that is always treated as new entry    
		if (col.equals("M_Product_ID"))
		{
			//	Reset
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_Product_ID", "0");
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_AttributeSetInstance_ID", "0");
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = "@" + m_text.getText() + "@";   //  Name indicator - otherwise Value
			int M_Warehouse_ID = Env.getContextAsInt(Env.getCtx(), m_lookup.getWindowNo(), "M_Warehouse_ID");
			int M_PriceList_ID = Env.getContextAsInt(Env.getCtx(), m_lookup.getWindowNo(), "M_PriceList_ID");
			//	Show Info
			InfoProduct ip = new InfoProduct (frame, true, m_lookup.getWindowNo(),
				M_Warehouse_ID, M_PriceList_ID, queryValue, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
			resetValue = true;
		}
		else if (col.equals("C_BPartner_ID"))
		{
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = m_text.getText();
			boolean isSOTrx = true;     //  default
			if (Env.getContext(Env.getCtx(), m_lookup.getWindowNo(), "IsSOTrx").equals("N"))
				isSOTrx = false;
			InfoBPartner ip = new InfoBPartner (frame, true, m_lookup.getWindowNo(),
				queryValue, isSOTrx, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
		}
		else	//	General Info
		{
			if (m_tableName == null)	//	sets table name & key column
				getDirectAccessSQL("*");
			Info ig = Info.create (frame, true, m_lookup.getWindowNo(), 
				m_tableName, m_keyColumnName, queryValue, false, whereClause);
			ig.show();
			cancelled = ig.isCancelled();
			result = ig.getSelectedKey();
		}
		
		//  Result
		if (result != null)
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = " + result.toString() + " (" + result.getClass().getName() + ")");
			//  make sure that value is in cache
			m_lookup.getDirect(result, false, true);
			if (resetValue)
				actionCombo (null);
			actionCombo (result);
		}
		else if (cancelled)
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (cancelled)");
			actionCombo (null);
		}
		else
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (not cancelled)");
			setValue(m_value);      //  to re-display value
		}
		//
		m_button.setEnabled(true);
		m_text.requestFocus();
	}	//	actionButton

	/**
	 * 	Get Where Clause
	 *	@return where clause or ""
	 */
	private String getWhereClause()
	{
		String whereClause = "";
		if (m_lookup == null)
			return "";
		if (m_lookup.getZoomQuery() != null)
			whereClause = m_lookup.getZoomQuery().getWhereClause();
		if (whereClause.length() == 0)
			whereClause = m_lookup.getValidation();
	//	Log.trace(9, "VLookup.getWhereClause - ZoomQuery=" 
	//		+ (m_lookup.getZoomQuery()==null ? "" : m_lookup.getZoomQuery().getWhereClause())
	//		+ ", Validation=" + m_lookup.getValidation());
		return whereClause;
	}	//	getWhereClause

	/**
	 *	Check, if data returns unique entry, otherwise involve Info via Button
	 */
	private void actionText()
	{
		String text = m_text.getText();
		//	Nothing entered
		if (text == null || text.length() == 0 || text.equals("%"))
		{
			actionButton(text);
			return;
		}
		//	Always like
		if (!text.endsWith("%"))
			text += "%";
		text = text.toUpperCase();
		Log.trace(Log.l4_Data, "VLookup.actionText", m_columnName + " - " + text);

		String finalSQL = Msg.parseTranslation(Env.getCtx(), getDirectAccessSQL(text));
		int id = 0;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(finalSQL);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				id = rs.getInt(1);		//	first
				if (rs.next())
					id = -1;			//	only if unique
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e)
		{
			Log.error("VLookup.actionText\nSQL=" + finalSQL, e);
			id = -2;
		}

		//	No (unique) result
		if (id <= 0)
		{
			if (id == 0)
				Log.trace(Log.l6_Database, "VLookup.actionText - Not Found - " + finalSQL);
			else
				Log.trace(Log.l6_Database, "VLookup.actionText - Not Unique - " + finalSQL);
			m_value = null;	// force re-display
			actionButton(m_text.getText());
			return;
		}
		Log.trace(Log.l5_DData, "VLookup.actionText - Unique ID => " + id);
		m_value = null;     //  forces re-display if value is unchanged but text updated and still unique
		actionCombo (new Integer(id));          //  data binding
	}	//	actionText


	private String		m_tableName = null;
	private String		m_keyColumnName = null;

	/**
	 * 	Generate Access SQL for Search.
	 * 	The SQL returns the ID of the value entered
	 * 	Also sets m_tableName and m_keyColumnName
	 *	@param text uppercase text for LIKE comparison
	 *	@return sql or ""
	 *  Example
	 *	SELECT C_Payment_ID FROM C_Payment WHERE UPPER(DocumentNo) LIKE x OR ...
	 */
	private String getDirectAccessSQL (String text)
	{
		StringBuffer sql = new StringBuffer();
		m_tableName = m_columnName.substring(0, m_columnName.length()-3);
		m_keyColumnName = m_columnName;
		//
		if (m_columnName.equals("M_Product_ID"))
		{
			//	Reset
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_Product_ID", "0");
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_AttributeSetInstance_ID", "0");
			//
			sql.append("SELECT M_Product_ID FROM M_Product WHERE (UPPER(Value) LIKE ")
				.append(DB.TO_STRING(text))
				.append(" OR UPC LIKE ").append(DB.TO_STRING(text)).append(")");
		}
		else if (m_columnName.equals("C_BPartner_ID"))
		{
			sql.append("SELECT C_BPartner_ID FROM C_BPartner WHERE (UPPER(Value) LIKE ")
				.append(DB.TO_STRING(text))
				.append(" OR UPPER(Name) LIKE ").append(DB.TO_STRING(text)).append(")");
		}
		else if (m_columnName.equals("C_Order_ID"))
		{
			sql.append("SELECT C_Order_ID FROM C_Order WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_Invoice_ID"))
		{
			sql.append("SELECT C_Invoice_ID FROM C_Invoice WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("M_InOut_ID"))
		{
			sql.append("SELECT M_InOut_ID FROM M_InOut WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_Payment_ID"))
		{
			sql.append("SELECT C_Payment_ID FROM C_Payment WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("GL_JournalBatch_ID"))
		{
			sql.append("SELECT GL_JournalBatch_ID FROM GL_JournalBatch WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("SalesRep_ID"))
		{
			sql.append("SELECT AD_User_ID FROM AD_User WHERE UPPER(Name) LIKE ")
				.append(DB.TO_STRING(text));
			m_tableName = "AD_User";
			m_keyColumnName = "AD_User_ID";
		}
		//	Predefined
		if (sql.length() > 0)
		{
			String wc = getWhereClause();
			if (wc != null && wc.length() > 0)
				sql.append(" AND ").append(wc);
			sql.append(" AND IsActive='Y'");
			//	***
			Log.trace(9, "VLookup.getDirectAccessSQL (predefined) " + sql.toString());
			return MRole.getDefault().addAccessSQL(sql.toString(),
				m_tableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
		}
		
		//	Check if it is a Table Reference
		if (m_lookup != null && m_lookup instanceof MLookup)
		{
			int AD_Reference_ID = ((MLookup)m_lookup).getAD_Reference_Value_ID();
			if (AD_Reference_ID != 0)
			{
				String query = "SELECT kc.ColumnName, dc.ColumnName, t.TableName "
					+ "FROM AD_Ref_Table rt"
					+ " INNER JOIN AD_Column kc ON (rt.AD_Key=kc.AD_Column_ID)"
					+ " INNER JOIN AD_Column dc ON (rt.AD_Display=dc.AD_Column_ID)"
					+ " INNER JOIN AD_Table t ON (rt.AD_Table_ID=t.AD_Table_ID) "
					+ "WHERE rt.AD_Reference_ID=?";
				String displayColumnName = null;
				PreparedStatement pstmt = null;
				try
				{
					pstmt = DB.prepareStatement(query);
					pstmt.setInt(1, AD_Reference_ID);
					ResultSet rs = pstmt.executeQuery();
					if (rs.next())
					{
						m_keyColumnName = rs.getString(1);
						displayColumnName = rs.getString(2);
						m_tableName = rs.getString(3);
					}
					rs.close();
					pstmt.close();
					pstmt = null;
				}
				catch (Exception e)
				{
					Log.error("VLookup.getDirectAccessSQL", e);
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
				if (displayColumnName != null)
				{
					sql = new StringBuffer();
					sql.append("SELECT ").append(m_keyColumnName)
						.append(" FROM ").append(m_tableName)
						.append(" WHERE UPPER(").append(displayColumnName)
						.append(") LIKE ").append(DB.TO_STRING(text))
						.append(" AND IsActive='Y'");
					String wc = getWhereClause();
					if (wc != null && wc.length() > 0)
						sql.append(" AND ").append(wc);
					//	***
					Log.trace(9, "VLookup.getDirectAccessSQL (Table) " + sql.toString());
					return MRole.getDefault().addAccessSQL(sql.toString(),
								m_tableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
				}
			}	//	Table Reference
		}	//	MLookup
		
		/** Check Well Known Columns of Table - assumes TableDir	**/
		String query = "SELECT t.TableName, c.ColumnName "
			+ "FROM AD_Column c "
			+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID AND t.IsView='N') "
			+ "WHERE (c.ColumnName IN ('DocumentNo', 'Value', 'Name') OR c.IsIdentifier='Y')"
			+ " AND c.AD_Reference_ID IN (10,14)"
			+ " AND EXISTS (SELECT * FROM AD_Column cc WHERE cc.AD_Table_ID=t.AD_Table_ID"
				+ " AND cc.IsKey='Y' AND cc.ColumnName=?)";
		m_keyColumnName = m_columnName;
		sql = new StringBuffer();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(query);
			pstmt.setString(1, m_keyColumnName);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				if (sql.length() != 0)
					sql.append(" OR ");
				m_tableName = rs.getString(1);
				sql.append("UPPER(").append(rs.getString(2)).append(") LIKE ").append(DB.TO_STRING(text));
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException ex)
		{
			Log.error("VLookup.getDirectAccessSQL", ex);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
		}
		catch (SQLException ex1)
		{
		}
		pstmt = null;
		//
		if (sql.length() == 0)
		{
			Log.error("VLookup.getDirectAccessSQL (TableDir) - no standard/identifier columns");
			return "";
		}
		//
		StringBuffer retValue = new StringBuffer ("SELECT ")
			.append(m_columnName).append(" FROM ").append(m_tableName)
			.append(" WHERE ").append(sql)
			.append(" AND IsActive='Y'");
		String wc = getWhereClause();
		if (wc != null && wc.length() > 0)
			retValue.append(" AND ").append(wc);
		//	***
		Log.trace(9, "VLookup.getDirectAccessSQL (TableDir) " + sql.toString());
		return MRole.getDefault().addAccessSQL(retValue.toString(),
					m_tableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
	}	//	getDirectAccessSQL


	/**
	 *	Action - Special BPartner Screen
	 *  @param newRecord true if new record should be created
	 */
	private void actionBPartner (boolean newRecord)
	{
		VBPartner vbp = new VBPartner (Env.getFrame(this), m_lookup.getWindowNo());
		int BPartner_ID = 0;
		//  if update, get current value
		if (!newRecord)
		{
			if (m_value instanceof Integer)
				BPartner_ID = ((Integer)m_value).intValue();
			else if (m_value != null)
				BPartner_ID = Integer.parseInt(m_value.toString());
		}

		vbp.loadBPartner (BPartner_ID);
		vbp.show();
		//  get result
		int result = vbp.getBPartner_ID();
		if (result == 0					//	0 = not saved
			&& result == BPartner_ID)	//	the same
			return;
		//  Maybe new BPartner - put in cache
		m_lookup.getDirect(new Integer(result), false, true);

		actionCombo (new Integer(result));      //  data binding
	}	//	actionBPartner

	/**
	 *	Action - Zoom
	 */
	private void actionZoom()
	{
		if (m_lookup == null)
			return;
		//
		String IsSOTrx = Env.getContext(Env.getCtx(), m_lookup.getWindowNo(), "IsSOTrx");
		int AD_Window_ID = m_lookup.getZoom(IsSOTrx);
		MQuery zoomQuery = m_lookup.getZoomQuery();
		Log.trace(Log.l1_User, "VLookup.actionZoom - " + AD_Window_ID 
			+ " - IsSOTrx=" + IsSOTrx 
			+ " - Query=" + zoomQuery + " - Value=" + getValue());
		//	If not already exist or exact value
		if (zoomQuery == null || getValue() != null)
		{
			zoomQuery = new MQuery();	//	ColumnName might be changed in MTab.validateQuery
			zoomQuery.addRestriction(m_columnName, MQuery.EQUAL, getValue());
			Log.trace(8, "VLookup.actionZoom - Query = " + zoomQuery); 
		}
		//
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		AWindow frame = new AWindow();
		if (!frame.initWindow(AD_Window_ID, zoomQuery, IsSOTrx.equals("Y")))
		{
			setCursor(Cursor.getDefaultCursor());
			ValueNamePair pp = Log.retrieveError();
			String msg = pp==null ? "AccessTableNoView" : pp.getValue();
			ADialog.error(m_lookup.getWindowNo(), this, msg, pp==null ? "" : pp.getName());
		}
		else
			AEnv.showCenterScreen(frame);
			//  async window - not able to get feedback
		frame = null;
		//
		setCursor(Cursor.getDefaultCursor());
	}	//	actionZoom

	/**
	 *	Action - Refresh
	 */
	private void actionRefresh()
	{
		if (m_lookup == null)
			return;
		//
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		Object obj = m_combo.getSelectedItem();
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + obj);
		m_lookup.refresh();
		if (m_lookup.isValidated())
			m_lookup.fillComboBox(isMandatory(), false, false, false);
		else
			m_lookup.fillComboBox(isMandatory(), true, false, false);
		m_combo.setSelectedItem(obj);
	//	m_combo.revalidate();
		//
		setCursor(Cursor.getDefaultCursor());
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + m_combo.getSelectedItem());
	}	//	actionRefresh

	/**************************************************************************

	/**
	 *	Focus Listener for ComboBoxes with missing Validation or invalid entries
	 *	- Requery listener for updated list
	 *  @param e FocusEvent
	 */
	public void focusGained (FocusEvent e)
	{
		if (e.getSource() != m_combo || e.isTemporary() || m_haveFocus || m_lookup == null)
			return;
		if (m_lookup.isValidated() && !m_lookup.hasInactive())
			return;
		//
		m_haveFocus = true;     //  prevents calling focus gained twice
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Object obj = m_lookup.getSelectedItem();
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName 
			+ " - Start    Count=" + m_combo.getItemCount() + ", Selected=" + obj);
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());
		m_lookup.fillComboBox(isMandatory(), true, true, true);     //  only validated & active & temporary
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName 
			+ " - Update   Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		m_lookup.setSelectedItem(obj);
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName 
			+ " - Selected Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		//
		m_settingFocus = false;
	}	//	focusGained

	/**
	 *	Reset Selection List
	 *  @param e FocusEvent
	 */
	public void focusLost(FocusEvent e)
	{
		if (e.isTemporary() || m_lookup == null 
			|| !m_button.isEnabled() )
			return;
		if (e.getSource() == m_text)
		{
			String text = m_text.getText();
			Log.trace(Log.l4_Data, "VLookup.focusLost Text", 
				m_columnName + " = " + m_value + " - " + text);
			//	Skip if empty
			if ((m_value == null && m_text.getText().length() == 0))
				return;
			if (m_lastDisplay.equals(text))
				return;
			//
			actionText();
			m_haveFocus = false;
			return;
		}
		//	Combo lost focus
		if (e.getSource() != m_combo)
			return;
		if (m_lookup.isValidated() && !m_lookup.hasInactive())
			return;
		//
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Log.trace(Log.l4_Data, "VLookup.focusLost", m_columnName + " = " + m_combo.getSelectedItem());
		Object obj = m_combo.getSelectedItem();
		//	set original model
		if (!m_lookup.isValidated())
			m_lookup.fillComboBox(true);    //  previous selection
		//	Set value
		if (obj != null)
		{
			m_combo.setSelectedItem(obj);
			//	original model may not have item
			if (!m_combo.getSelectedItem().equals(obj))
			{
				Log.trace(Log.l6_Database, "VLookup.focusLost " + m_columnName, "added to combo - " + obj);
				m_combo.addItem(obj);
				m_combo.setSelectedItem(obj);
			}
		}
	//	actionCombo(getValue());
		m_settingFocus = false;
		m_haveFocus = false;    //  can gain focus again
	}	//	focusLost

	/**
	 *  Set ToolTip
	 *  @param text tool tip text
	 */
	public void setToolTipText(String text)
	{
		super.setToolTipText(text);
		m_button.setToolTipText(text);
		m_text.setToolTipText(text);
		m_combo.setToolTipText(text);
	}   //  setToolTipText

	/**
	 * 	Refresh Query
	 *	@return count
	 */
	public int refresh()
	{
		if (m_lookup == null)
			return -1;
		return m_lookup.refresh();
	}	//	refresh


}	//	VLookup

/*****************************************************************************
 *	Mouse Listener for Popup Menu
 */
final class VLookup_mouseAdapter extends java.awt.event.MouseAdapter
{
	/**
	 *	Constructor
	 *  @param adaptee adaptee
	 */
	VLookup_mouseAdapter(VLookup adaptee)
	{
		this.adaptee = adaptee;
	}	//	VLookup_mouseAdapter

	private VLookup adaptee;

	/**
	 *	Mouse Listener
	 *  @param e MouseEvent
	 */
	public void mouseClicked(MouseEvent e)
	{
	//	System.out.println("mouseClicked " + e.getID() + " " + e.getSource().getClass().toString());
		//	popup menu
		if (SwingUtilities.isRightMouseButton(e))
			adaptee.popupMenu.show((Component)e.getSource(), e.getX(), e.getY());
	}	//	mouse Clicked

}	//	VLookup_mouseAdapter
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
package org.compiere.grid.ed;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.text.*;
import java.util.*;
import java.math.*;
import java.beans.*;
import java.sql.*;

import org.compiere.apps.*;
import org.compiere.util.*;
import org.compiere.model.*;
import org.compiere.apps.search.*;
import org.compiere.plaf.*;
import org.compiere.swing.*;

/**
 *  Lookup Field.
 *  <p>
 *	    When r/o - display a Label
 *		When STABLE - display a ComboBox
 *		Otherwise show Selection Dialog
 *  <p>
 *  Sepecial handling of BPartner and Product
 *
 *  @author 	Jorg Janke
 *  @version 	$Id: VLookup.java,v 1.40 2003/02/23 19:42:47 jjanke Exp $
 */
public class VLookup extends JComponent
	implements VEditor, ActionListener, FocusListener
{
	/**
	 *  Create BPartner Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createBPartner (int WindowNo)
	{
		int AD_Column_ID = 3499;    //  C_Invoice.C_BPartner_ID
		try
		{
			Lookup lookup = MLookupFactory.create(Env.getCtx(), AD_Column_ID, WindowNo, DisplayType.Search, false);
			return new VLookup ("C_BPartner_ID", false, false, true, lookup, DisplayType.Search, WindowNo);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createBPartner", e);
		}
		return null;
	}   //  createBPartner

	/**
	 *  Create Product Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createProduct (int WindowNo)
	{
		int AD_Column_ID = 3840;    //  C_InvoiceLine.M_Product_ID
		try
		{
			Lookup lookup = MLookupFactory.create(Env.getCtx(), AD_Column_ID, WindowNo, DisplayType.Search, false);
			return new VLookup ("M_Product_ID", false, false, true, lookup, DisplayType.Search, WindowNo);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createProduct", e);
		}
		return null;
	}   //  createProduct

	/*************************************************************************/

	/**
	 *	IDE Default Constructor
	 */
	public VLookup()
	{
		this("Lookup", false, false, true, null, 0, 0);
	}	//	VLookup


	/**
	 *	Detail Constructor
	 *
	 *  @param columnName column
	 *  @param mandatory mandatory
	 *  @param isReadOnly read only
	 *  @param isUpdateable updateable
	 *  @param lookup lookup
	 *  @param displayType display type
	 *  @param WindowNo window no
	 */
	public VLookup (String columnName, boolean mandatory, boolean isReadOnly, boolean isUpdateable,
		Lookup lookup, int displayType, int WindowNo)
	{
		super();
		m_columnName = columnName;
		m_lookup = lookup;
		if (lookup == null)
			Log.trace(Log.l3_Util, "VLookup", "Lookup is NULL = " + columnName);
		setMandatory(mandatory);
		m_displayType = displayType;
		m_WindowNo = WindowNo;		//	for Info
		//
		setLayout(new BorderLayout());
		VLookup_mouseAdapter mouse = new VLookup_mouseAdapter(this);    //  popup

		//	***	Text & Button	***
		m_text.addActionListener(this);
		m_text.addMouseListener(mouse);
		//  Button
		m_button.addActionListener(this);
		m_button.addMouseListener(mouse);
		m_button.setFocusable(false);   //  don't focus when tabbing
		if (columnName.equals("C_BPartner_ID"))
			m_button.setIcon(Env.getImageIcon("BPartner10.gif"));
		else if (columnName.equals("M_Product_ID"))
			m_button.setIcon(Env.getImageIcon("Product10.gif"));
		else
			m_button.setIcon(Env.getImageIcon("PickOpen10.gif"));

		//	*** VComboBox	***
		if (m_lookup != null && displayType != DisplayType.Search)	//	No Search
		{
			//  Memory Leak after executing the next two lines ??
			m_lookup.fillComboBox (isMandatory(), false, false, false);
			m_combo.setModel(m_lookup);
			//
			m_combo.addActionListener(this);							//	Selection
			m_combo.addMouseListener(mouse);	                        //	popup
			//	FocusListener to refresh selection before opening
			if (!m_lookup.isValidated() || m_lookup.hasInactive())
				m_combo.addFocusListener(this);
		}

		setUI (true);
		//	ReadWrite	-	decides what components to show
		if (isReadOnly || !isUpdateable || m_lookup == null)
			setReadWrite(false);
		else
			setReadWrite(true);

		//	Popup
		if (m_lookup != null)
		{
			if ((displayType == DisplayType.List && Env.getContextAsInt(Env.getCtx(), "#AD_Role_ID") == 0)
				|| displayType != DisplayType.List)     //  only system admins can change lists, so no need to zoom for others
			{
				mZoom = new JMenuItem(Msg.getMsg(Env.getCtx(), "Zoom"), Env.getImageIcon("Zoom16.gif"));
				mZoom.addActionListener(this);
				popupMenu.add(mZoom);
			}
			mRefresh = new JMenuItem(Msg.getMsg(Env.getCtx(), "Refresh"), Env.getImageIcon("Refresh16.gif"));
			mRefresh.addActionListener(this);
			popupMenu.add(mRefresh);
		}
		//	VBPartner quick entry link
		if (columnName.equals("C_BPartner_ID"))
		{
			mBPartnerNew = new JMenuItem (Msg.getMsg(Env.getCtx(), "New"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerNew.addActionListener(this);
			popupMenu.add(mBPartnerNew);
			mBPartnerUpd = new JMenuItem (Msg.getMsg(Env.getCtx(), "Update"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerUpd.addActionListener(this);
			popupMenu.add(mBPartnerUpd);
		}
		//
		if (m_lookup != null && m_lookup.getZoom() == 0)
			mZoom.setEnabled(false);
	}	//	VLookup

	/**
	 *  Dispose
	 */
	public void dispose()
	{
		m_text = null;
		m_button = null;
		m_lookup = null;
		m_mField = null;
		m_combo.removeActionListener(this);
		m_combo.setModel(new DefaultComboBoxModel());    //  remove reference
	//	m_combo.removeAllItems();
		m_combo = null;
	}   //  dispose

	/** Display Length for Lookups (18)         */
	public final static int     DISPLAY_LENGTH = 18;

	/** Search: The Editable Text Field         */
	private CTextField 			m_text = new CTextField (DISPLAY_LENGTH);
	/** Search: The Button to open Editor   */
	private CButton				m_button = new CButton();
	/** The Combo Box if not a Search Lookup    */
	private VComboBox			m_combo = new VComboBox();
	/** Indicator that value is being set       */
	private volatile boolean 	m_settingValue = false;
	private volatile boolean 	m_settingFocus = false;
	/** Indicator that Lookup has focus         */
	private volatile boolean	m_haveFocus = false;
	/** Indicator - inserting new value			*/
	private volatile boolean	m_inserting = false;

	//
	private String				m_columnName;
	private Lookup				m_lookup;
	private int					m_displayType;
	private int					m_WindowNo;

	private boolean				m_comboActive = true;
	private Object				m_value;

	//	Popup
	JPopupMenu 					popupMenu = new JPopupMenu();
	private JMenuItem 			mZoom;
	private JMenuItem 			mRefresh;
	private JMenuItem			mBPartnerNew;
	private JMenuItem			mBPartnerUpd;

	private MField              m_mField = null;

	/**
	 *  Set Content and Size of Compoments
	 *  @param initial if true, size and margins will be set
	 */
	private void setUI (boolean initial)
	{
		if (initial)
		{
			Dimension size = m_text.getPreferredSize();
			setPreferredSize(new Dimension(size));  //	causes r/o to be the same length
			m_combo.setPreferredSize(new Dimension(size));
			//
			m_text.setBorder(null);
			Dimension bSize = new Dimension(size.height, size.height);
			m_button.setPreferredSize (bSize);
			m_button.setMargin(new Insets(0, 0, 0, 0));
		}

		//	What to show
		this.remove(m_combo);
		this.remove(m_button);
		this.remove(m_text);
		//
		if (!isReadWrite())									//	r/o - show text only
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			m_text.setReadWrite(false);
			m_combo.setReadWrite(false);
			m_comboActive = false;
		}
		else if (m_displayType != DisplayType.Search)	    //	show combo if not Search
		{
			this.setBorder(null);
			this.add(m_combo, BorderLayout.CENTER);
			m_comboActive = true;
		}
		else 												//	Search or unstable - show text & button
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			this.add(m_button, BorderLayout.EAST);
			m_text.setReadWrite (true);
			m_comboActive = false;
		}
	}   //  setUI

	/**
	 *	Set ReadWrite
	 *  @param value ReadWrite
	 */
	public void setReadWrite (boolean value)
	{
		boolean rw = value;
		if (m_lookup == null)
			rw = false;
		if (m_combo.isReadWrite() != value)
		{
			m_combo.setReadWrite(rw);
			setUI (false);
			if (m_comboActive)
				setValue (m_value);
		}
	}	//	setReadWrite

	/**
	 *	IsEditable
	 *  @return is lookup ReadWrite
	 */
	public boolean isReadWrite()
	{
		return m_combo.isReadWrite();
	}	//	isReadWrite

	/**
	 *	Set Mandatory (and back bolor)
	 *  @param mandatory mandatory
	 */
	public void setMandatory (boolean mandatory)
	{
		m_combo.setMandatory(mandatory);
		m_text.setMandatory(mandatory);
	}	//	setMandatory

	/**
	 *	Is it mandatory
	 *  @return true if mandatory
	 */
	public boolean isMandatory()
	{
		return m_combo.isMandatory();
	}	//	isMandatory

	/**
	 *	Set Background
	 *  @param color color
	 */
	public void setBackground(Color color)
	{
		m_text.setBackground(color);
		m_combo.setBackground(color);
	}	//	setBackground

	/**
	 *	Set Background
	 *  @param error error
	 */
	public void setBackground (boolean error)
	{
		m_text.setBackground(error);
		m_combo.setBackground(error);
	}	//	setBackground

	/**
	 *  Set Foreground
	 *  @param fg Foreground color
	 */
	public void setForeground(Color fg)
	{
		m_text.setForeground(fg);
		m_combo.setForeground(fg);
	}   //  setForeground

	/**
	 *  Set Editor to value
	 *  @param value new Value
	 */
	public void setValue (Object value)
	{
		Log.trace(Log.l6_Database, "VLookup.setValue", m_columnName + "=" + value);
		m_settingValue = true;		//	disable actions
		m_value = value;

		//	Set both for switching
		m_combo.setValue (value);
		if (value == null)
		{
			m_text.setText (null);
			m_settingValue = false;
			return;
		}
		if (m_lookup == null)
		{
			m_text.setText (value.toString());
			m_settingValue = false;
			return;
		}

		//	Set Display
		String display = m_lookup.getDisplay(value);
		boolean notFound = display.startsWith("<") && display.startsWith(">");
		m_text.setText (display);
		m_text.setCaretPosition (0); //	show beginning

		//	Nothing showing in Combo and should be showing
		if (m_combo.getSelectedItem() == null
			&& (m_comboActive || (m_inserting && m_displayType != DisplayType.Search)))
		{
			//  lookup found nothing too
			if (notFound)
			{
				Log.trace(8, "VLookup.setValue - Not found (1)", display);
				//  we may have a new value
				m_lookup.refresh();
				m_combo.setValue (value);
				display = m_lookup.getDisplay(value);
				m_text.setText (display);
				m_text.setCaretPosition (0);	//	show beginning
				notFound = display.startsWith("<") && display.endsWith(">");
			}
			if (notFound)	//	<key>
			{
				m_value = null;
				actionCombo (null);             //  data binding
				Log.trace(Log.l6_Database, "VLookup.setValue - not found - " + value);
			}
			//  we have lookup
			else if (m_combo.getSelectedItem() == null)
			{
				NamePair pp = m_lookup.get(value);
				if (pp != null)
				{
					Log.trace (Log.l6_Database, "VLookup.setValue - added to combo - " + pp);
					//  Add to Combo
					m_combo.addItem (pp);
					m_combo.setValue (value);
				}
			}
			//  Not in Lookup - set to Null
			if (m_combo.getSelectedItem() == null)
			{
				Log.trace(Log.l1_User, "VLookup.setValue - not in Lookup - set to NULL");
				actionCombo (null);             //  data binding (calls setValue again)
				m_value = null;
			}
		}
		m_settingValue = false;
	}	//	setValue

	/**
	 *  Property Change Listener
	 *  @param evt PropertyChangeEvent
	 */
	public void propertyChange (PropertyChangeEvent evt)
	{
	//	Log.trace(Log.l5_DData, "VLookup.propertyChange", evt);
		if (evt.getPropertyName().equals(MField.PROPERTY))
		{
			m_inserting = MField.INSERTING.equals(evt.getOldValue());
			setValue(evt.getNewValue());
			m_inserting = false;
		}
	}   //  propertyChange

	/**
	 *	Return Editor value (Integer)
	 *  @return value
	 */
	public Object getValue()
	{
		if (m_comboActive)
			return m_combo.getValue ();
		return m_value;
	}	//	getValue

	/**
	 *  Return editor display
	 *  @return display value
	 */
	public String getDisplay()
	{
		String retValue = null;
		if (m_comboActive)
			retValue = m_combo.getDisplay();
		//  check lookup
		else if (m_lookup == null)
			retValue = m_value.toString();
		else
			retValue = m_lookup.getDisplay(m_value);
	//	Log.trace(Log.l6_Database, "VLookup.getDisplay - " + retValue, "ComboActive=" + m_comboActive);
		return retValue;
	}   //  getDisplay

	/**
	 *  Set Field/WindowNo for ValuePreference
	 *  @param mField Model Field for Lookup
	 */
	public void setField (MField mField)
	{
		m_mField = mField;
		if (m_mField != null)
			ValuePreference.addMenu (this, popupMenu);
	}   //  setField

	/*************************************************************************/

	/**
	 *	Action Listener	- data binding
	 *  @param e ActionEvent
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (m_settingValue || m_settingFocus)
			return;
		Log.trace(Log.l4_Data, "VLookup.actionPerformed", e.getActionCommand() + ", ComboValue=" + m_combo.getSelectedItem());
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());

		//  Preference
		if (e.getActionCommand().equals(ValuePreference.NAME))
		{
			ValuePreference.start (m_mField, getValue(), getDisplay());
			return;
		}

		//  Combo Selection
		else if (e.getSource() == m_combo)
		{
			Object value = getValue();
			Object o = m_combo.getSelectedItem();
			if (o != null)
			{
				String s = o.toString();
				//  don't allow selection of inactive
				if (s.startsWith(MLookup.INACTIVE_S) && s.endsWith(MLookup.INACTIVE_E))
				{
					Log.trace(Log.l1_User, "VLookup.actionPerformed - Selection inactive set to NULL");
					value = null;
				}
			}
			actionCombo (value);                //  data binding
		}
		//  Button pressed
		else if (e.getSource() == m_button)
			actionButton ("");
		//  Text entered
		else if (e.getSource() == m_text)
			actionText();

		//  Popup Menu
		else if (e.getSource() == mZoom)
			actionZoom();
		else if (e.getSource() == mRefresh)
			actionRefresh();
		else if (e.getSource() == mBPartnerNew)
			actionBPartner(true);
		else if (e.getSource() == mBPartnerUpd)
			actionBPartner(false);
	}	//	actionPerformed

	/**
	 *  Action Listener Interface
	 *  @param listener listener
	 */
	public void addActionListener(ActionListener listener)
	{
		m_combo.addActionListener(listener);
		m_text.addActionListener(listener);
	}   //  addActionListener

	/**
	 *	Action - Combo.
	 *  <br>
	 *	== dataBinding == inform of new value
	 *  <pre>
	 *  VLookup.actionCombo
	 *      GridController.vetoableChange
	 *          MTable.setValueAt
	 *              MField.setValue
	 *                  VLookup.setValue
	 *          MTab.dataStatusChanged
	 *  </pre>
	 *  @param value new value
	 */
	private void actionCombo (Object value)
	{
	//	Log.trace(Log.l6_Database, "VLookup.actionCombo", value==null ? "null" : value.toString());
		try
		{
			fireVetoableChange (m_columnName, null, value);
		}
		catch (PropertyVetoException pve)
		{
			Log.error("VLookup.actionCombo", pve);
		}
		//  is the value updated ?
		boolean updated = false;
		if (value == null && m_value == null)
			updated = true;
		else if (value != null && value.equals(m_value))
			updated = true;
		if (!updated)
		{
			//  happens if VLookup is used outside of APanel/GridController (no property listener)
			Log.trace(Log.l6_Database, "VLookup.actionCombo - Value explicitly set - new=" + value + ", old=" + m_value);
			setValue(value);
		}
	}	//	actionCombo


	/**
	 *	Action - Button.
	 *	- Call Info
	 *	@param queryValue initial query value
	 */
	private void actionButton (String queryValue)
	{
		m_button.setEnabled(false);                 //  disable double click
		requestFocus();                             //  closes other editors
		Frame frame = Env.getFrame(this);

		/**
		 *  Three return options:
		 *  - Value Selected & OK pressed   => store result => result has value
		 *  - Cancel pressed                => store null   => result == null && cancelled
		 *  - Window closed                 -> ignore       => result == null && !cancalled
		 */
		Object result = null;
		boolean cancelled = false;
		//
		String col = m_lookup.getColumnName();		//	fully qualified name
		if (col.indexOf(".") != -1)
			col = col.substring(col.indexOf(".")+1);
		//  Zoom / Validation
		String whereClause = "";
		if (m_lookup.getZoomQuery() != null)
			whereClause = m_lookup.getZoomQuery().getWhereClause();
		if (whereClause.length() == 0)
			whereClause = m_lookup.getValidation();
		//
		Log.trace(Log.l5_DData, "VLookup.actionButton - " + col + ", Zoom=" + m_lookup.getZoom()
			+ " (" + whereClause + ")");
		//
		boolean resetValue = false;             //  reset value so that is always treated as new entry
		if (col.equals("M_Product_ID"))
		{
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = "@" + m_text.getText() + "@";   //  Name indicator - otherwise Value
			int M_Warehouse_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNo, "M_Warehouse_ID");
			int M_PriceList_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNo, "M_PriceList_ID");
			InfoProduct ip = new InfoProduct (frame, true, m_WindowNo,
				M_Warehouse_ID, M_PriceList_ID, queryValue, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
			resetValue = true;
		}
		else if (col.equals("C_BPartner_ID"))
		{
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = m_text.getText();
			boolean isSOTrx = true;     //  default
			if (Env.getContext(Env.getCtx(), m_WindowNo, "IsSOTrx").equals("N"))
				isSOTrx = false;
			InfoBPartner ip = new InfoBPartner (frame, true, m_WindowNo,
				queryValue, isSOTrx, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
		}
		else
		{
			String tableName = col;
			int index = m_columnName.indexOf("_ID");
			if (index != -1)
				tableName = m_columnName.substring(0, index);
			Info ig = Info.create (frame, true, m_WindowNo, tableName, m_columnName, queryValue, false, whereClause);
			ig.show();
			cancelled = ig.isCancelled();
			result = ig.getSelectedKey();
		}
		//  Result
		if (result != null)
		{
			Log.trace(Log.l4_Data, "VLookupactionButton", "Result = " + result.toString() + " " + result.getClass().getName());
			//  make sure that value is in cache
			m_lookup.getDirect(result, true);
			if (resetValue)
				actionCombo (null);
			actionCombo (result);
		}
		else if (cancelled)
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (cancelled)");
			actionCombo (null);
		}
		else
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (not cancelled)");
			setValue(m_value);      //  to re-display value
		}
		//
		m_button.setEnabled(true);
	}	//	actionButton

	/**
	 *	Check, if data returns unique entry, otherwise involve Info via Button
	 */
	private void actionText()
	{
		String text = m_text.getText();
		//	Nothing entered
		if (m_text.getText().length() == 0)
		{
			actionButton(text);
			return;
		}
		//	Always like
		if (!text.endsWith("%"))
			text += "%";

		StringBuffer SQL = new StringBuffer();
		if (m_columnName.equals("M_Product_ID"))
		{
			SQL.append("SELECT M_Product_ID FROM M_Product WHERE UPPER(Value) LIKE '")
				.append(text.toUpperCase()).append("'");
		}
		else if (m_columnName.equals("C_BPartner_ID"))
		{
			SQL.append("SELECT C_BPartner_ID FROM C_BPartner WHERE (UPPER(Value) LIKE '")
				.append(text.toUpperCase())
				.append("' OR UPPER(Name) LIKE '").append(text.toUpperCase()).append("')");
		}
		else if (m_columnName.equals("C_Order_ID"))
		{
			SQL.append("SELECT C_Order_ID FROM C_Order WHERE UPPER(DocumentNo) LIKE '")
				.append(text.toUpperCase()).append("'");
		}
		else if (m_columnName.equals("C_Invoice_ID"))
		{
			SQL.append("SELECT C_Invoice_ID FROM C_Invoice WHERE UPPER(DocumentNo) LIKE '")
				.append(text.toUpperCase()).append("'");
		}
		else if (m_columnName.equals("M_InOut_ID"))
		{
			SQL.append("SELECT M_InOut_ID FROM M_InOut WHERE UPPER(DocumentNo) LIKE '")
				.append(text.toUpperCase()).append("'");
		}
		else if (m_columnName.equals("C_Payment_ID"))
		{
			SQL.append("SELECT C_Payment_ID FROM C_Payment WHERE UPPER(DocumentNo) LIKE '")
				.append(text.toUpperCase()).append("'");
		}
		else
		{
			actionButton(text);
			return;
		}
		//	Finish SQL
		SQL.append(" AND IsActive='Y'");
		//	AddSecurity
		String tableName = m_columnName.substring(0, m_columnName.length()-3);
		String finalSQL = Access.addROAccessSQL(Env.getCtx(), SQL.toString(), tableName, false);
		int id = 0;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(finalSQL);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				id = rs.getInt(1);		//	first
				if (rs.next())
					id = -1;			//	only if unique
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("VLookup.actionText\nSQL=" + finalSQL, e);
			id = -2;
		}
		Log.trace(Log.l5_DData, "VLookup.actionText ID => " + id);

		//	No (unique) result
		if (id <= 0)
		{
			Log.trace(Log.l6_Database, finalSQL);
			actionButton(text);
			return;
		}
		m_value = null;     //  forces re-display if value is unchanged but text updated and still unique
		actionCombo (new Integer(id));          //  data binding
	}	//	actionText

	/**
	 *	Action - Special BPartner Screen
	 *  @param newRecord true if new record should be created
	 */
	private void actionBPartner (boolean newRecord)
	{
		VBPartner vbp = new VBPartner (Env.getFrame(this), m_WindowNo);
		int BPartner_ID = 0;
		//  if update, get current value
		if (!newRecord)
		{
			if (m_value instanceof Integer)
				BPartner_ID = ((Integer)m_value).intValue();
			else if (m_value != null)
				BPartner_ID = Integer.parseInt(m_value.toString());
		}

		vbp.loadBPartner (BPartner_ID);
		vbp.show();
		//  get result
		int result = vbp.getBPartner_ID();
		if (result == 0					//	0 = not saved
			&& result == BPartner_ID)	//	the same
			return;
		//  Maybe new BPartner - put in cache
		m_lookup.getDirect(new Integer(result), true);

		actionCombo (new Integer(result));      //  data binding
	}	//	actionBPartner

	/**
	 *	Action - Zoom
	 */
	private void actionZoom()
	{
		if (m_lookup == null)
			return;
		//
		int AD_Window_ID = m_lookup.getZoom();
		MQuery zoomQuery = m_lookup.getZoomQuery();
		String IsSOTrx = Env.getContext(Env.getCtx(), m_WindowNo, "IsSOTrx");
		Log.trace(Log.l1_User, "VLookup.actionZoom - " + AD_Window_ID + " - Query=" + zoomQuery + " - Value=" + getValue());
		//	If not already exist
		if (zoomQuery == null || (!zoomQuery.isActive() && getValue() != null))
		{
			zoomQuery = new MQuery();
			zoomQuery.addRestriction(m_columnName, MQuery.EQUAL, getValue());
		}
		//
		setCursor(Cursor.getDefaultCursor());
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		AWindow frame = new AWindow();
		if (!frame.initWindow(AD_Window_ID, zoomQuery, IsSOTrx.equals("Y")))
		{
			setCursor(Cursor.getDefaultCursor());
			ValueNamePair pp = Log.retrieveError();
			String msg = pp==null ? "AccessTableNoView" : pp.getValue();
			ADialog.error(m_WindowNo, this, msg, pp==null ? "" : pp.getName());
		}
		else
			AEnv.showCenterScreen(frame);
			//  async window - not able to get feedback
		frame = null;
		//
		setCursor(Cursor.getDefaultCursor());
	}	//	actionZoom

	/**
	 *	Action - Refresh
	 */
	private void actionRefresh()
	{
		if (m_lookup == null)
			return;
		//
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		Object obj = m_combo.getSelectedItem();
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + obj);
		m_lookup.refresh();
		if (m_lookup.isValidated())
			m_lookup.fillComboBox(isMandatory(), false, false, false);
		else
			m_lookup.fillComboBox(isMandatory(), true, false, false);
		m_combo.setSelectedItem(obj);
	//	m_combo.revalidate();
		//
		setCursor(Cursor.getDefaultCursor());
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + m_combo.getSelectedItem());
	}	//	actionRefresh

	/*************************************************************************/

	/**
	 *	Focus Listener for ComboBoxes with missing Validation or invalid entries
	 *	- Requery listener for updated list
	 *  @param e FocusEvent
	 */
	public void focusGained (FocusEvent e)
	{
		if (e.getSource() != m_combo || e.isTemporary() || m_haveFocus)
			return;
		//
		m_haveFocus = true;     //  prevents calling focus gained twice
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Object obj = m_lookup.getSelectedItem();
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Start    Count=" + m_combo.getItemCount() + ", Selected=" + obj);
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());
		m_lookup.fillComboBox(isMandatory(), true, true, true);     //  only validated & active & temporary
	//	Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Update   Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		m_lookup.setSelectedItem(obj);
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Selected Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		//
		m_settingFocus = false;
	}	//	focusGained

	/**
	 *	Reset Selection List
	 *  @param e FocusEvent
	 */
	public void focusLost(FocusEvent e)
	{
		if (e.getSource() != m_combo || e.isTemporary())
			return;
		//
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Log.trace(Log.l4_Data, "VLookup.focusLost", m_columnName + " = " + m_combo.getSelectedItem());
		Object obj = m_combo.getSelectedItem();
		//	set original model
		if (!m_lookup.isValidated())
			m_lookup.fillComboBox(true);    //  previous selection
		//	Set value
		if (obj != null)
		{
			m_combo.setSelectedItem(obj);
			//	original model may not have item
			if (!m_combo.getSelectedItem().equals(obj))
			{
				Log.trace(Log.l6_Database, "VLookup.focusLost " + m_columnName, "added to combo - " + obj);
				m_combo.addItem(obj);
				m_combo.setSelectedItem(obj);
			}
		}
	//	actionCombo(getValue());
		m_settingFocus = false;
		m_haveFocus = false;    //  can gain focus again
	}	//	focusLost

	/**
	 *  Set ToolTip
	 *  @param text tool tip text
	 */
	public void setToolTipText(String text)
	{
		super.setToolTipText(text);
		m_button.setToolTipText(text);
		m_text.setToolTipText(text);
		m_combo.setToolTipText(text);
	}   //  setToolTipText

}	//	VLookup

/*****************************************************************************/

/**
 *	Mouse Listener for Popup Menu
 */
final class VLookup_mouseAdapter extends java.awt.event.MouseAdapter
{
	/**
	 *	Constructor
	 *  @param adaptee adaptee
	 */
	VLookup_mouseAdapter(VLookup adaptee)
	{
		this.adaptee = adaptee;
	}	//	VLookup_mouseAdapter

	private VLookup adaptee;

	/**
	 *	Mouse Listener
	 *  @param e MouseEvent
	 */
	public void mouseClicked(MouseEvent e)
	{
	//	System.out.println("mouseClicked " + e.getID() + " " + e.getSource().getClass().toString());
		//	popup menu
		if (SwingUtilities.isRightMouseButton(e))
			adaptee.popupMenu.show((Component)e.getSource(), e.getX(), e.getY());
	}	//	mouse Clicked

}	//	VLookup_mouseAdapter
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
package org.compiere.grid.ed;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.sql.*;

import javax.swing.*;

import org.compiere.apps.*;
import org.compiere.apps.search.*;
import org.compiere.model.*;
import org.compiere.swing.*;
import org.compiere.util.*;

/**
 *  Lookup Field.
 *  <p>
 *	    When r/o - display a Label
 *		When STABLE - display a ComboBox
 *		Otherwise show Selection Dialog
 *  <p>
 *  Sepecial handling of BPartner and Product
 *
 *  @author 	Jorg Janke
 *  @version 	$Id: VLookup.java,v 1.68 2004/08/27 21:24:59 jjanke Exp $
 */
public class VLookup extends JComponent
	implements VEditor, ActionListener, FocusListener
{
	/**
	 *  Create Optional BPartner Search Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createBPartner (int WindowNo)
	{
		int AD_Column_ID = 3499;    //  C_Invoice.C_BPartner_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, 0, AD_Column_ID, DisplayType.Search);
			return new VLookup ("C_BPartner_ID", false, false, true, lookup);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createBPartner", e);
		}
		return null;
	}   //  createBPartner

	/**
	 *  Create Optional Product Search Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createProduct (int WindowNo)
	{
		int AD_Column_ID = 3840;    //  C_InvoiceLine.M_Product_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, 0, AD_Column_ID, DisplayType.Search);
			return new VLookup ("M_Product_ID", false, false, true, lookup);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createProduct", e);
		}
		return null;
	}   //  createProduct

	/**
	 *  Create Optional User Search Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createUser (int WindowNo)
	{
		int AD_Column_ID = 10443;    //  AD_WF_Activity.AD_User_UD
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, 0, AD_Column_ID, DisplayType.Search);
			return new VLookup ("AD_User_ID", false, false, true, lookup);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createUser", e);
		}
		return null;
	}   //  createProduct

	
	/*************************************************************************
	 *	Detail Constructor
	 *
	 *  @param columnName column
	 *  @param mandatory mandatory
	 *  @param isReadOnly read only
	 *  @param isUpdateable updateable
	 *  @param lookup lookup
	 */
	public VLookup (String columnName, boolean mandatory, boolean isReadOnly, boolean isUpdateable,
		Lookup lookup)
	{
		super();
		super.setName(columnName);
		m_combo.setName(columnName);
		m_columnName = columnName;
		setMandatory(mandatory);
		m_lookup = lookup;
		//
		setLayout(new BorderLayout());
		VLookup_mouseAdapter mouse = new VLookup_mouseAdapter(this);    //  popup

		//	***	Text & Button	***
		m_text.addActionListener(this);
		m_text.addFocusListener(this);
		m_text.addMouseListener(mouse);
		//  Button
		m_button.addActionListener(this);
		m_button.addMouseListener(mouse);
		m_button.setFocusable(false);   //  don't focus when tabbing
		m_button.setMargin(new Insets(0, 0, 0, 0));
		if (columnName.equals("C_BPartner_ID"))
			m_button.setIcon(Env.getImageIcon("BPartner10.gif"));
		else if (columnName.equals("M_Product_ID"))
			m_button.setIcon(Env.getImageIcon("Product10.gif"));
		else
			m_button.setIcon(Env.getImageIcon("PickOpen10.gif"));

		//	*** VComboBox	***
		if (m_lookup != null && m_lookup.getDisplayType() != DisplayType.Search)	//	No Search
		{
			//  Memory Leak after executing the next two lines ??
			m_lookup.fillComboBox (isMandatory(), false, false, false);
			m_combo.setModel(m_lookup);
			//
			m_combo.addActionListener(this);							//	Selection
			m_combo.addMouseListener(mouse);	                        //	popup
			//	FocusListener to refresh selection before opening
			m_combo.addFocusListener(this);
		}

		setUI (true);
		//	ReadWrite	-	decides what components to show
		if (isReadOnly || !isUpdateable || m_lookup == null)
			setReadWrite(false);
		else
			setReadWrite(true);

		//	Popup
		if (m_lookup != null)
		{
			if ((m_lookup.getDisplayType() == DisplayType.List && Env.getContextAsInt(Env.getCtx(), "#AD_Role_ID") == 0)
				|| m_lookup.getDisplayType() != DisplayType.List)     //  only system admins can change lists, so no need to zoom for others
			{
				mZoom = new JMenuItem(Msg.getMsg(Env.getCtx(), "Zoom"), Env.getImageIcon("Zoom16.gif"));
				mZoom.addActionListener(this);
				popupMenu.add(mZoom);
			}
			mRefresh = new JMenuItem(Msg.getMsg(Env.getCtx(), "Refresh"), Env.getImageIcon("Refresh16.gif"));
			mRefresh.addActionListener(this);
			popupMenu.add(mRefresh);
		}
		//	VBPartner quick entry link
		if (columnName.equals("C_BPartner_ID"))
		{
			mBPartnerNew = new JMenuItem (Msg.getMsg(Env.getCtx(), "New"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerNew.addActionListener(this);
			popupMenu.add(mBPartnerNew);
			mBPartnerUpd = new JMenuItem (Msg.getMsg(Env.getCtx(), "Update"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerUpd.addActionListener(this);
			popupMenu.add(mBPartnerUpd);
		}
		//
		if (m_lookup != null && m_lookup.getZoom() == 0)
			mZoom.setEnabled(false);
	}	//	VLookup

	/**
	 *  Dispose
	 */
	public void dispose()
	{
		m_text = null;
		m_button = null;
		m_lookup = null;
		m_mField = null;
		//
		m_combo.removeFocusListener(this);
		m_combo.removeActionListener(this);
		m_combo.setModel(new DefaultComboBoxModel());    //  remove reference
	//	m_combo.removeAllItems();
		m_combo = null;
	}   //  dispose

	/** Display Length for Lookups (15)         */
	public final static int     DISPLAY_LENGTH = 15;
	/** Field Height 				 */
	public static int     		FIELD_HIGHT = 0;

	/** Search: The Editable Text Field         */
	private CTextField 			m_text = new CTextField (DISPLAY_LENGTH);
	/** Search: The Button to open Editor   */
	private CButton				m_button = new CButton();
	/** The Combo Box if not a Search Lookup    */
	private VComboBox			m_combo = new VComboBox();
	/** Indicator that value is being set       */
	private volatile boolean 	m_settingValue = false;
	private volatile boolean 	m_settingFocus = false;
	/** Indicator that Lookup has focus         */
	private volatile boolean	m_haveFocus = false;
	/** Indicator - inserting new value			*/
	private volatile boolean	m_inserting = false;
	/** Last Display							*/
	private String				m_lastDisplay = "";

	//
	private String				m_columnName;
	private Lookup				m_lookup;

	private boolean				m_comboActive = true;
	private Object				m_value;

	//	Popup
	JPopupMenu 					popupMenu = new JPopupMenu();
	private JMenuItem 			mZoom;
	private JMenuItem 			mRefresh;
	private JMenuItem			mBPartnerNew;
	private JMenuItem			mBPartnerUpd;

	private MField              m_mField = null;

	/**
	 *  Set Content and Size of Compoments
	 *  @param initial if true, size and margins will be set
	 */
	private void setUI (boolean initial)
	{
		if (initial)
		{
			Dimension size = m_text.getPreferredSize();
			setPreferredSize(new Dimension(size));  //	causes r/o to be the same length
			m_combo.setPreferredSize(new Dimension(size));
			setMinimumSize(new Dimension (30, size.height));
			FIELD_HIGHT = size.height;
			//
			m_text.setBorder(null);
			Dimension bSize = new Dimension(size.height, size.height);
			m_button.setPreferredSize (bSize);
		}

		//	What to show
		this.remove(m_combo);
		this.remove(m_button);
		this.remove(m_text);
		//
		if (!isReadWrite())									//	r/o - show text only
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			m_text.setReadWrite(false);
			m_combo.setReadWrite(false);
			m_comboActive = false;
		}
		else if (m_lookup != null && m_lookup.getDisplayType() != DisplayType.Search)	    //	show combo if not Search
		{
			this.setBorder(null);
			this.add(m_combo, BorderLayout.CENTER);
			m_comboActive = true;
		}
		else 												//	Search or unstable - show text & button
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			this.add(m_button, BorderLayout.EAST);
			m_text.setReadWrite (true);
			m_comboActive = false;
		}
	}   //  setUI

	/**
	 *	Set ReadWrite
	 *  @param value ReadWrite
	 */
	public void setReadWrite (boolean value)
	{
		boolean rw = value;
		if (m_lookup == null)
			rw = false;
		if (m_combo.isReadWrite() != value)
		{
			m_combo.setReadWrite(rw);
			setUI (false);
			if (m_comboActive)
				setValue (m_value);
		}
	}	//	setReadWrite

	/**
	 *	IsEditable
	 *  @return is lookup ReadWrite
	 */
	public boolean isReadWrite()
	{
		return m_combo.isReadWrite();
	}	//	isReadWrite

	/**
	 *	Set Mandatory (and back bolor)
	 *  @param mandatory mandatory
	 */
	public void setMandatory (boolean mandatory)
	{
		m_combo.setMandatory(mandatory);
		m_text.setMandatory(mandatory);
	}	//	setMandatory

	/**
	 *	Is it mandatory
	 *  @return true if mandatory
	 */
	public boolean isMandatory()
	{
		return m_combo.isMandatory();
	}	//	isMandatory

	/**
	 *	Set Background
	 *  @param color color
	 */
	public void setBackground(Color color)
	{
		m_text.setBackground(color);
		m_combo.setBackground(color);
	}	//	setBackground

	/**
	 *	Set Background
	 *  @param error error
	 */
	public void setBackground (boolean error)
	{
		m_text.setBackground(error);
		m_combo.setBackground(error);
	}	//	setBackground

	/**
	 *  Set Foreground
	 *  @param fg Foreground color
	 */
	public void setForeground(Color fg)
	{
		m_text.setForeground(fg);
		m_combo.setForeground(fg);
	}   //  setForeground

	/**
	 *  Set Editor to value
	 *  @param value new Value
	 */
	public void setValue (Object value)
	{
		Log.trace(Log.l6_Database, "VLookup.setValue", m_columnName + "=" + value);
		m_settingValue = true;		//	disable actions
		m_value = value;

		//	Set both for switching
		m_combo.setValue (value);
		if (value == null)
		{
			m_text.setText (null);
			m_lastDisplay = "";
			m_settingValue = false;
			return;
		}
		if (m_lookup == null)
		{
			m_text.setText (value.toString());
			m_lastDisplay = value.toString();
			m_settingValue = false;
			return;
		}

		//	Set Display
		m_lastDisplay = m_lookup.getDisplay(value);
		if (m_lastDisplay.equals("<-1>"))
		{
			m_lastDisplay = "";
			m_value = null;
		}
		boolean notFound = m_lastDisplay.startsWith("<") && m_lastDisplay.startsWith(">");
		m_text.setText (m_lastDisplay);
		m_text.setCaretPosition (0); //	show beginning

		//	Nothing showing in Combo and should be showing
		if (m_combo.getSelectedItem() == null
			&& (m_comboActive || (m_inserting && m_lookup.getDisplayType() != DisplayType.Search)))
		{
			//  lookup found nothing too
			if (notFound)
			{
				Log.trace(8, "VLookup.setValue - Not found (1)", m_lastDisplay);
				//  we may have a new value
				m_lookup.refresh();
				m_combo.setValue (value);
				m_lastDisplay = m_lookup.getDisplay(value);
				m_text.setText (m_lastDisplay);
				m_text.setCaretPosition (0);	//	show beginning
				notFound = m_lastDisplay.startsWith("<") && m_lastDisplay.endsWith(">");
			}
			if (notFound)	//	<key>
			{
				m_value = null;
				actionCombo (null);             //  data binding
				Log.trace(Log.l6_Database, "VLookup.setValue - not found - " + value);
			}
			//  we have lookup
			else if (m_combo.getSelectedItem() == null)
			{
				NamePair pp = m_lookup.get(value);
				if (pp != null)
				{
					Log.trace (Log.l6_Database, "VLookup.setValue - added to combo - " + pp);
					//  Add to Combo
					m_combo.addItem (pp);
					m_combo.setValue (value);
				}
			}
			//  Not in Lookup - set to Null
			if (m_combo.getSelectedItem() == null)
			{
				Log.trace(Log.l1_User, "VLookup.setValue - not in Lookup - set to NULL");
				actionCombo (null);             //  data binding (calls setValue again)
				m_value = null;
			}
		}
		m_settingValue = false;
	}	//	setValue

	/**
	 *  Property Change Listener
	 *  @param evt PropertyChangeEvent
	 */
	public void propertyChange (PropertyChangeEvent evt)
	{
	//	Log.trace(Log.l5_DData, "VLookup.propertyChange", evt);
		if (evt.getPropertyName().equals(MField.PROPERTY))
		{
			m_inserting = MField.INSERTING.equals(evt.getOldValue());	//	MField.setValue
			setValue(evt.getNewValue());
			m_inserting = false;
		}
	}   //  propertyChange

	/**
	 *	Return Editor value (Integer)
	 *  @return value
	 */
	public Object getValue()
	{
		if (m_comboActive)
			return m_combo.getValue ();
		return m_value;
	}	//	getValue

	/**
	 *  Return editor display
	 *  @return display value
	 */
	public String getDisplay()
	{
		String retValue = null;
		if (m_comboActive)
			retValue = m_combo.getDisplay();
		//  check lookup
		else if (m_lookup == null)
			retValue = m_value.toString();
		else
			retValue = m_lookup.getDisplay(m_value);
	//	Log.trace(Log.l6_Database, "VLookup.getDisplay - " + retValue, "ComboActive=" + m_comboActive);
		return retValue;
	}   //  getDisplay

	/**
	 *  Set Field/WindowNo for ValuePreference
	 *  @param mField Model Field for Lookup
	 */
	public void setField (MField mField)
	{
		m_mField = mField;
		if (m_mField != null)
			ValuePreference.addMenu (this, popupMenu);
	}   //  setField

	
	/**************************************************************************
	 *	Action Listener	- data binding
	 *  @param e ActionEvent
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (m_settingValue || m_settingFocus)
			return;
		Log.trace(Log.l4_Data, "VLookup.actionPerformed", e.getActionCommand() + ", ComboValue=" + m_combo.getSelectedItem());
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());

		//  Preference
		if (e.getActionCommand().equals(ValuePreference.NAME))
		{
			ValuePreference.start (m_mField, getValue(), getDisplay());
			return;
		}

		//  Combo Selection
		else if (e.getSource() == m_combo)
		{
			Object value = getValue();
			Object o = m_combo.getSelectedItem();
			if (o != null)
			{
				String s = o.toString();
				//  don't allow selection of inactive
				if (s.startsWith(MLookup.INACTIVE_S) && s.endsWith(MLookup.INACTIVE_E))
				{
					Log.trace(Log.l1_User, "VLookup.actionPerformed - Selection inactive set to NULL");
					value = null;
				}
			}
			actionCombo (value);                //  data binding
		}
		//  Button pressed
		else if (e.getSource() == m_button)
			actionButton ("");
		//  Text entered
		else if (e.getSource() == m_text)
			actionText();

		//  Popup Menu
		else if (e.getSource() == mZoom)
			actionZoom(m_combo.getSelectedItem());
		else if (e.getSource() == mRefresh)
			actionRefresh();
		else if (e.getSource() == mBPartnerNew)
			actionBPartner(true);
		else if (e.getSource() == mBPartnerUpd)
			actionBPartner(false);
	}	//	actionPerformed

	/**
	 *  Action Listener Interface
	 *  @param listener listener
	 */
	public void addActionListener(ActionListener listener)
	{
		m_combo.addActionListener(listener);
		m_text.addActionListener(listener);
	}   //  addActionListener

	/**
	 *	Action - Combo.
	 *  <br>
	 *	== dataBinding == inform of new value
	 *  <pre>
	 *  VLookup.actionCombo
	 *      GridController.vetoableChange
	 *          MTable.setValueAt
	 *              MField.setValue
	 *                  VLookup.setValue
	 *          MTab.dataStatusChanged
	 *  </pre>
	 *  @param value new value
	 */
	private void actionCombo (Object value)
	{
		Log.trace(Log.l6_Database, "VLookup.actionCombo", value==null ? "null" : value.toString());
		try
		{
			fireVetoableChange (m_columnName, null, value);
		}
		catch (PropertyVetoException pve)
		{
			Log.error("VLookup.actionCombo", pve);
		}
		//  is the value updated ?
		boolean updated = false;
		if (value == null && m_value == null)
			updated = true;
		else if (value != null && value.equals(m_value))
			updated = true;
		if (!updated)
		{
			//  happens if VLookup is used outside of APanel/GridController (no property listener)
			Log.trace(Log.l6_Database, "VLookup.actionCombo",
				"Value explicitly set - new=" + value + ", old=" + m_value);
			setValue(value);
		}
	}	//	actionCombo


	/**
	 *	Action - Button.
	 *	- Call Info
	 *	@param queryValue initial query value
	 */
	private void actionButton (String queryValue)
	{
		m_button.setEnabled(false);                 //  disable double click
		if (m_lookup == null)
			return;		//	leave button disabled
		this.requestFocus();						//  closes other editors
		Frame frame = Env.getFrame(this);

		/**
		 *  Three return options:
		 *  - Value Selected & OK pressed   => store result => result has value
		 *  - Cancel pressed                => store null   => result == null && cancelled
		 *  - Window closed                 -> ignore       => result == null && !cancalled
		 */
		Object result = null;
		boolean cancelled = false;
		//
		String col = m_lookup.getColumnName();		//	fully qualified name
		if (col.indexOf(".") != -1)
			col = col.substring(col.indexOf(".")+1);
		//  Zoom / Validation
		String whereClause = getWhereClause();
		//
		Log.trace(Log.l5_DData, "VLookup.actionButton - " + col 
			+ ", Zoom=" + m_lookup.getZoom()
			+ " (" + whereClause + ")");
		//
		boolean resetValue = false;	//	reset value so that is always treated as new entry    
		if (col.equals("M_Product_ID"))
		{
			//	Reset
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_Product_ID", "0");
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_AttributeSetInstance_ID", "0");
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = "@" + m_text.getText() + "@";   //  Name indicator - otherwise Value
			int M_Warehouse_ID = Env.getContextAsInt(Env.getCtx(), m_lookup.getWindowNo(), "M_Warehouse_ID");
			int M_PriceList_ID = Env.getContextAsInt(Env.getCtx(), m_lookup.getWindowNo(), "M_PriceList_ID");
			//	Show Info
			InfoProduct ip = new InfoProduct (frame, true, m_lookup.getWindowNo(),
				M_Warehouse_ID, M_PriceList_ID, queryValue, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
			resetValue = true;
		}
		else if (col.equals("C_BPartner_ID"))
		{
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = m_text.getText();
			boolean isSOTrx = true;     //  default
			if (Env.getContext(Env.getCtx(), m_lookup.getWindowNo(), "IsSOTrx").equals("N"))
				isSOTrx = false;
			InfoBPartner ip = new InfoBPartner (frame, true, m_lookup.getWindowNo(),
				queryValue, isSOTrx, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
		}
		else	//	General Info
		{
			if (m_tableName == null)	//	sets table name & key column
				getDirectAccessSQL("*");
			Info ig = Info.create (frame, true, m_lookup.getWindowNo(), 
				m_tableName, m_keyColumnName, queryValue, false, whereClause);
			ig.show();
			cancelled = ig.isCancelled();
			result = ig.getSelectedKey();
		}
		
		//  Result
		if (result != null)
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = " + result.toString() + " (" + result.getClass().getName() + ")");
			//  make sure that value is in cache
			m_lookup.getDirect(result, false, true);
			if (resetValue)
				actionCombo (null);
			actionCombo (result);
		}
		else if (cancelled)
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (cancelled)");
			actionCombo (null);
		}
		else
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (not cancelled)");
			setValue(m_value);      //  to re-display value
		}
		//
		m_button.setEnabled(true);
		m_text.requestFocus();
	}	//	actionButton

	/**
	 * 	Get Where Clause
	 *	@return where clause or ""
	 */
	private String getWhereClause()
	{
		String whereClause = "";
		if (m_lookup == null)
			return "";
		if (m_lookup.getZoomQuery() != null)
			whereClause = m_lookup.getZoomQuery().getWhereClause();
		if (whereClause.length() == 0)
			whereClause = m_lookup.getValidation();
	//	Log.trace(9, "VLookup.getWhereClause - ZoomQuery=" 
	//		+ (m_lookup.getZoomQuery()==null ? "" : m_lookup.getZoomQuery().getWhereClause())
	//		+ ", Validation=" + m_lookup.getValidation());
		return whereClause;
	}	//	getWhereClause

	/**
	 *	Check, if data returns unique entry, otherwise involve Info via Button
	 */
	private void actionText()
	{
		String text = m_text.getText();
		//	Nothing entered
		if (text == null || text.length() == 0 || text.equals("%"))
		{
			actionButton(text);
			return;
		}
		//	Always like
		if (!text.endsWith("%"))
			text += "%";
		text = text.toUpperCase();
		Log.trace(Log.l4_Data, "VLookup.actionText", m_columnName + " - " + text);

		String finalSQL = Msg.parseTranslation(Env.getCtx(), getDirectAccessSQL(text));
		int id = 0;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(finalSQL);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				id = rs.getInt(1);		//	first
				if (rs.next())
					id = -1;			//	only if unique
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e)
		{
			Log.error("VLookup.actionText\nSQL=" + finalSQL, e);
			id = -2;
		}

		//	No (unique) result
		if (id <= 0)
		{
			if (id == 0)
				Log.trace(Log.l6_Database, "VLookup.actionText - Not Found - " + finalSQL);
			else
				Log.trace(Log.l6_Database, "VLookup.actionText - Not Unique - " + finalSQL);
			m_value = null;	// force re-display
			actionButton(m_text.getText());
			return;
		}
		Log.trace(Log.l5_DData, "VLookup.actionText - Unique ID => " + id);
		m_value = null;     //  forces re-display if value is unchanged but text updated and still unique
		actionCombo (new Integer(id));          //  data binding
	}	//	actionText


	private String		m_tableName = null;
	private String		m_keyColumnName = null;

	/**
	 * 	Generate Access SQL for Search.
	 * 	The SQL returns the ID of the value entered
	 * 	Also sets m_tableName and m_keyColumnName
	 *	@param text uppercase text for LIKE comparison
	 *	@return sql or ""
	 *  Example
	 *	SELECT C_Payment_ID FROM C_Payment WHERE UPPER(DocumentNo) LIKE x OR ...
	 */
	private String getDirectAccessSQL (String text)
	{
		StringBuffer sql = new StringBuffer();
		m_tableName = m_columnName.substring(0, m_columnName.length()-3);
		m_keyColumnName = m_columnName;
		//
		if (m_columnName.equals("M_Product_ID"))
		{
			//	Reset
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_Product_ID", "0");
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_AttributeSetInstance_ID", "0");
			//
			sql.append("SELECT M_Product_ID FROM M_Product WHERE (UPPER(Value) LIKE ")
				.append(DB.TO_STRING(text))
				.append(" OR UPC LIKE ").append(DB.TO_STRING(text)).append(")");
		}
		else if (m_columnName.equals("C_BPartner_ID"))
		{
			sql.append("SELECT C_BPartner_ID FROM C_BPartner WHERE (UPPER(Value) LIKE ")
				.append(DB.TO_STRING(text))
				.append(" OR UPPER(Name) LIKE ").append(DB.TO_STRING(text)).append(")");
		}
		else if (m_columnName.equals("C_Order_ID"))
		{
			sql.append("SELECT C_Order_ID FROM C_Order WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_Invoice_ID"))
		{
			sql.append("SELECT C_Invoice_ID FROM C_Invoice WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("M_InOut_ID"))
		{
			sql.append("SELECT M_InOut_ID FROM M_InOut WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_Payment_ID"))
		{
			sql.append("SELECT C_Payment_ID FROM C_Payment WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("GL_JournalBatch_ID"))
		{
			sql.append("SELECT GL_JournalBatch_ID FROM GL_JournalBatch WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("SalesRep_ID"))
		{
			sql.append("SELECT AD_User_ID FROM AD_User WHERE UPPER(Name) LIKE ")
				.append(DB.TO_STRING(text));
			m_tableName = "AD_User";
			m_keyColumnName = "AD_User_ID";
		}
		//	Predefined
		if (sql.length() > 0)
		{
			String wc = getWhereClause();
			if (wc != null && wc.length() > 0)
				sql.append(" AND ").append(wc);
			sql.append(" AND IsActive='Y'");
			//	***
			Log.trace(9, "VLookup.getDirectAccessSQL (predefined) " + sql.toString());
			return MRole.getDefault().addAccessSQL(sql.toString(),
				m_tableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
		}
		
		//	Check if it is a Table Reference
		if (m_lookup != null && m_lookup instanceof MLookup)
		{
			int AD_Reference_ID = ((MLookup)m_lookup).getAD_Reference_Value_ID();
			if (AD_Reference_ID != 0)
			{
				String query = "SELECT kc.ColumnName, dc.ColumnName, t.TableName "
					+ "FROM AD_Ref_Table rt"
					+ " INNER JOIN AD_Column kc ON (rt.AD_Key=kc.AD_Column_ID)"
					+ " INNER JOIN AD_Column dc ON (rt.AD_Display=dc.AD_Column_ID)"
					+ " INNER JOIN AD_Table t ON (rt.AD_Table_ID=t.AD_Table_ID) "
					+ "WHERE rt.AD_Reference_ID=?";
				String displayColumnName = null;
				PreparedStatement pstmt = null;
				try
				{
					pstmt = DB.prepareStatement(query);
					pstmt.setInt(1, AD_Reference_ID);
					ResultSet rs = pstmt.executeQuery();
					if (rs.next())
					{
						m_keyColumnName = rs.getString(1);
						displayColumnName = rs.getString(2);
						m_tableName = rs.getString(3);
					}
					rs.close();
					pstmt.close();
					pstmt = null;
				}
				catch (Exception e)
				{
					Log.error("VLookup.getDirectAccessSQL", e);
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
				if (displayColumnName != null)
				{
					sql = new StringBuffer();
					sql.append("SELECT ").append(m_keyColumnName)
						.append(" FROM ").append(m_tableName)
						.append(" WHERE UPPER(").append(displayColumnName)
						.append(") LIKE ").append(DB.TO_STRING(text))
						.append(" AND IsActive='Y'");
					String wc = getWhereClause();
					if (wc != null && wc.length() > 0)
						sql.append(" AND ").append(wc);
					//	***
					Log.trace(9, "VLookup.getDirectAccessSQL (Table) " + sql.toString());
					return MRole.getDefault().addAccessSQL(sql.toString(),
								m_tableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
				}
			}	//	Table Reference
		}	//	MLookup
		
		/** Check Well Known Columns of Table - assumes TableDir	**/
		String query = "SELECT t.TableName, c.ColumnName "
			+ "FROM AD_Column c "
			+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID AND t.IsView='N') "
			+ "WHERE (c.ColumnName IN ('DocumentNo', 'Value', 'Name') OR c.IsIdentifier='Y')"
			+ " AND c.AD_Reference_ID IN (10,14)"
			+ " AND EXISTS (SELECT * FROM AD_Column cc WHERE cc.AD_Table_ID=t.AD_Table_ID"
				+ " AND cc.IsKey='Y' AND cc.ColumnName=?)";
		m_keyColumnName = m_columnName;
		sql = new StringBuffer();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(query);
			pstmt.setString(1, m_keyColumnName);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				if (sql.length() != 0)
					sql.append(" OR ");
				m_tableName = rs.getString(1);
				sql.append("UPPER(").append(rs.getString(2)).append(") LIKE ").append(DB.TO_STRING(text));
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException ex)
		{
			Log.error("VLookup.getDirectAccessSQL", ex);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
		}
		catch (SQLException ex1)
		{
		}
		pstmt = null;
		//
		if (sql.length() == 0)
		{
			Log.error("VLookup.getDirectAccessSQL (TableDir) - no standard/identifier columns");
			return "";
		}
		//
		StringBuffer retValue = new StringBuffer ("SELECT ")
			.append(m_columnName).append(" FROM ").append(m_tableName)
			.append(" WHERE ").append(sql)
			.append(" AND IsActive='Y'");
		String wc = getWhereClause();
		if (wc != null && wc.length() > 0)
			retValue.append(" AND ").append(wc);
		//	***
		Log.trace(9, "VLookup.getDirectAccessSQL (TableDir) " + sql.toString());
		return MRole.getDefault().addAccessSQL(retValue.toString(),
					m_tableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
	}	//	getDirectAccessSQL


	/**
	 *	Action - Special BPartner Screen
	 *  @param newRecord true if new record should be created
	 */
	private void actionBPartner (boolean newRecord)
	{
		VBPartner vbp = new VBPartner (Env.getFrame(this), m_lookup.getWindowNo());
		int BPartner_ID = 0;
		//  if update, get current value
		if (!newRecord)
		{
			if (m_value instanceof Integer)
				BPartner_ID = ((Integer)m_value).intValue();
			else if (m_value != null)
				BPartner_ID = Integer.parseInt(m_value.toString());
		}

		vbp.loadBPartner (BPartner_ID);
		vbp.show();
		//  get result
		int result = vbp.getC_BPartner_ID();
		if (result == 0					//	0 = not saved
			&& result == BPartner_ID)	//	the same
			return;
		//  Maybe new BPartner - put in cache
		m_lookup.getDirect(new Integer(result), false, true);

		actionCombo (new Integer(result));      //  data binding
	}	//	actionBPartner

	/**
	 *	Action - Zoom
	 *	@parem selected item
	 */
	private void actionZoom (Object selectedItem)
	{
		if (m_lookup == null)
			return;
		//
		String IsSOTrx = Env.getContext(Env.getCtx(), m_lookup.getWindowNo(), "IsSOTrx");
		int AD_Window_ID = m_lookup.getZoom(IsSOTrx);
		MQuery zoomQuery = m_lookup.getZoomQuery();
		Object value = getValue();
		if (value == null)
			value = selectedItem;
		Log.trace(Log.l1_User, "VLookup.actionZoom - " + AD_Window_ID 
			+ " - IsSOTrx=" + IsSOTrx 
			+ " - Query=" + zoomQuery + " - Value=" + value);
		//	If not already exist or exact value
		if (zoomQuery == null || value != null)
		{
			zoomQuery = new MQuery();	//	ColumnName might be changed in MTab.validateQuery
			zoomQuery.addRestriction(m_columnName, MQuery.EQUAL, value);
			Log.trace(8, "VLookup.actionZoom - Query = " + zoomQuery); 
		}
		//
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		AWindow frame = new AWindow();
		if (!frame.initWindow(AD_Window_ID, zoomQuery, IsSOTrx.equals("Y")))
		{
			setCursor(Cursor.getDefaultCursor());
			ValueNamePair pp = Log.retrieveError();
			String msg = pp==null ? "AccessTableNoView" : pp.getValue();
			ADialog.error(m_lookup.getWindowNo(), this, msg, pp==null ? "" : pp.getName());
		}
		else
			AEnv.showCenterScreen(frame);
			//  async window - not able to get feedback
		frame = null;
		//
		setCursor(Cursor.getDefaultCursor());
	}	//	actionZoom

	/**
	 *	Action - Refresh
	 */
	private void actionRefresh()
	{
		if (m_lookup == null)
			return;
		//
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		Object obj = m_combo.getSelectedItem();
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + obj);
		m_lookup.refresh();
		if (m_lookup.isValidated())
			m_lookup.fillComboBox(isMandatory(), false, false, false);
		else
			m_lookup.fillComboBox(isMandatory(), true, false, false);
		m_combo.setSelectedItem(obj);
	//	m_combo.revalidate();
		//
		setCursor(Cursor.getDefaultCursor());
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + m_combo.getSelectedItem());
	}	//	actionRefresh

	
	/**************************************************************************
	 *	Focus Listener for ComboBoxes with missing Validation or invalid entries
	 *	- Requery listener for updated list
	 *  @param e FocusEvent
	 */
	public void focusGained (FocusEvent e)
	{
		if (e.getSource() != m_combo || e.isTemporary() || m_haveFocus || m_lookup == null)
			return;
		if (m_lookup.isValidated() && !m_lookup.hasInactive())
			return;
		//
		m_haveFocus = true;     //  prevents calling focus gained twice
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Object obj = m_lookup.getSelectedItem();
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName 
			+ " - Start    Count=" + m_combo.getItemCount() + ", Selected=" + obj);
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());
		m_lookup.fillComboBox(isMandatory(), true, true, true);     //  only validated & active & temporary
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName 
			+ " - Update   Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		m_lookup.setSelectedItem(obj);
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName 
			+ " - Selected Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		//
		m_settingFocus = false;
	}	//	focusGained

	/**
	 *	Reset Selection List
	 *  @param e FocusEvent
	 */
	public void focusLost(FocusEvent e)
	{
		if (e.isTemporary() || m_lookup == null 
			|| !m_button.isEnabled() )
			return;
		if (e.getSource() == m_text)
		{
			String text = m_text.getText();
			Log.trace(Log.l4_Data, "VLookup.focusLost Text", 
				m_columnName + " = " + m_value + " - " + text);
			//	Skip if empty
			if ((m_value == null && m_text.getText().length() == 0))
				return;
			if (m_lastDisplay.equals(text))
				return;
			//
			actionText();
			m_haveFocus = false;
			return;
		}
		//	Combo lost focus
		if (e.getSource() != m_combo)
			return;
		if (m_lookup.isValidated() && !m_lookup.hasInactive())
			return;
		//
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Log.trace(Log.l4_Data, "VLookup.focusLost", m_columnName + " = " + m_combo.getSelectedItem());
		Object obj = m_combo.getSelectedItem();
		//	set original model
		if (!m_lookup.isValidated())
			m_lookup.fillComboBox(true);    //  previous selection
		//	Set value
		if (obj != null)
		{
			m_combo.setSelectedItem(obj);
			//	original model may not have item
			if (!m_combo.getSelectedItem().equals(obj))
			{
				Log.trace(Log.l6_Database, "VLookup.focusLost " + m_columnName, "added to combo - " + obj);
				m_combo.addItem(obj);
				m_combo.setSelectedItem(obj);
			}
		}
	//	actionCombo(getValue());
		m_settingFocus = false;
		m_haveFocus = false;    //  can gain focus again
	}	//	focusLost

	/**
	 *  Set ToolTip
	 *  @param text tool tip text
	 */
	public void setToolTipText(String text)
	{
		super.setToolTipText(text);
		m_button.setToolTipText(text);
		m_text.setToolTipText(text);
		m_combo.setToolTipText(text);
	}   //  setToolTipText

	/**
	 * 	Refresh Query
	 *	@return count
	 */
	public int refresh()
	{
		if (m_lookup == null)
			return -1;
		return m_lookup.refresh();
	}	//	refresh


}	//	VLookup

/*****************************************************************************
 *	Mouse Listener for Popup Menu
 */
final class VLookup_mouseAdapter extends java.awt.event.MouseAdapter
{
	/**
	 *	Constructor
	 *  @param adaptee adaptee
	 */
	VLookup_mouseAdapter(VLookup adaptee)
	{
		this.adaptee = adaptee;
	}	//	VLookup_mouseAdapter

	private VLookup adaptee;

	/**
	 *	Mouse Listener
	 *  @param e MouseEvent
	 */
	public void mouseClicked(MouseEvent e)
	{
	//	System.out.println("mouseClicked " + e.getID() + " " + e.getSource().getClass().toString());
		//	popup menu
		if (SwingUtilities.isRightMouseButton(e))
			adaptee.popupMenu.show((Component)e.getSource(), e.getX(), e.getY());
	}	//	mouse Clicked

}	//	VLookup_mouseAdapter
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
package org.compiere.grid.ed;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.text.*;
import java.util.*;
import java.math.*;
import java.beans.*;
import java.sql.*;

import org.compiere.apps.*;
import org.compiere.util.*;
import org.compiere.model.*;
import org.compiere.apps.search.*;
import org.compiere.plaf.*;
import org.compiere.swing.*;

/**
 *  Lookup Field.
 *  <p>
 *	    When r/o - display a Label
 *		When STABLE - display a ComboBox
 *		Otherwise show Selection Dialog
 *  <p>
 *  Sepecial handling of BPartner and Product
 *
 *  @author 	Jorg Janke
 *  @version 	$Id: VLookup.java,v 1.49 2003/11/06 07:08:28 jjanke Exp $
 */
public class VLookup extends JComponent
	implements VEditor, ActionListener, FocusListener
{
	/**
	 *  Create BPartner Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createBPartner (int WindowNo)
	{
		int AD_Column_ID = 3499;    //  C_Invoice.C_BPartner_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, 0, AD_Column_ID, DisplayType.Search);
			return new VLookup ("C_BPartner_ID", false, false, true, lookup, DisplayType.Search, WindowNo);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createBPartner", e);
		}
		return null;
	}   //  createBPartner

	/**
	 *  Create Product Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createProduct (int WindowNo)
	{
		int AD_Column_ID = 3840;    //  C_InvoiceLine.M_Product_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, 0, AD_Column_ID, DisplayType.Search);
			return new VLookup ("M_Product_ID", false, false, true, lookup, DisplayType.Search, WindowNo);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createProduct", e);
		}
		return null;
	}   //  createProduct

	/*************************************************************************/

	/**
	 *	IDE Default Constructor
	 */
	public VLookup()
	{
		this("Lookup", false, false, true, null, 0, 0);
	}	//	VLookup


	/**
	 *	Detail Constructor
	 *
	 *  @param columnName column
	 *  @param mandatory mandatory
	 *  @param isReadOnly read only
	 *  @param isUpdateable updateable
	 *  @param lookup lookup
	 *  @param displayType display type
	 *  @param WindowNo window no
	 */
	public VLookup (String columnName, boolean mandatory, boolean isReadOnly, boolean isUpdateable,
		Lookup lookup, int displayType, int WindowNo)
	{
		super();
		super.setName(columnName);
		m_combo.setName(columnName);
		m_columnName = columnName;
		m_lookup = lookup;
		if (lookup == null)
			Log.trace(Log.l3_Util, "VLookup", "Lookup is NULL = " + columnName);
		setMandatory(mandatory);
		m_displayType = displayType;
		m_WindowNo = WindowNo;		//	for Info
		//
		setLayout(new BorderLayout());
		VLookup_mouseAdapter mouse = new VLookup_mouseAdapter(this);    //  popup

		//	***	Text & Button	***
		m_text.addActionListener(this);
		m_text.addMouseListener(mouse);
		//  Button
		m_button.addActionListener(this);
		m_button.addMouseListener(mouse);
		m_button.setFocusable(false);   //  don't focus when tabbing
		if (columnName.equals("C_BPartner_ID"))
			m_button.setIcon(Env.getImageIcon("BPartner10.gif"));
		else if (columnName.equals("M_Product_ID"))
			m_button.setIcon(Env.getImageIcon("Product10.gif"));
		else
			m_button.setIcon(Env.getImageIcon("PickOpen10.gif"));

		//	*** VComboBox	***
		if (m_lookup != null && displayType != DisplayType.Search)	//	No Search
		{
			//  Memory Leak after executing the next two lines ??
			m_lookup.fillComboBox (isMandatory(), false, false, false);
			m_combo.setModel(m_lookup);
			//
			m_combo.addActionListener(this);							//	Selection
			m_combo.addMouseListener(mouse);	                        //	popup
			//	FocusListener to refresh selection before opening
			m_combo.addFocusListener(this);
		}

		setUI (true);
		//	ReadWrite	-	decides what components to show
		if (isReadOnly || !isUpdateable || m_lookup == null)
			setReadWrite(false);
		else
			setReadWrite(true);

		//	Popup
		if (m_lookup != null)
		{
			if ((displayType == DisplayType.List && Env.getContextAsInt(Env.getCtx(), "#AD_Role_ID") == 0)
				|| displayType != DisplayType.List)     //  only system admins can change lists, so no need to zoom for others
			{
				mZoom = new JMenuItem(Msg.getMsg(Env.getCtx(), "Zoom"), Env.getImageIcon("Zoom16.gif"));
				mZoom.addActionListener(this);
				popupMenu.add(mZoom);
			}
			mRefresh = new JMenuItem(Msg.getMsg(Env.getCtx(), "Refresh"), Env.getImageIcon("Refresh16.gif"));
			mRefresh.addActionListener(this);
			popupMenu.add(mRefresh);
		}
		//	VBPartner quick entry link
		if (columnName.equals("C_BPartner_ID"))
		{
			mBPartnerNew = new JMenuItem (Msg.getMsg(Env.getCtx(), "New"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerNew.addActionListener(this);
			popupMenu.add(mBPartnerNew);
			mBPartnerUpd = new JMenuItem (Msg.getMsg(Env.getCtx(), "Update"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerUpd.addActionListener(this);
			popupMenu.add(mBPartnerUpd);
		}
		//
		if (m_lookup != null && m_lookup.getZoom() == 0)
			mZoom.setEnabled(false);
	}	//	VLookup

	/**
	 *  Dispose
	 */
	public void dispose()
	{
		m_text = null;
		m_button = null;
		m_lookup = null;
		m_mField = null;
		m_combo.removeActionListener(this);
		m_combo.setModel(new DefaultComboBoxModel());    //  remove reference
	//	m_combo.removeAllItems();
		m_combo = null;
	}   //  dispose

	/** Display Length for Lookups (16)         */
	public final static int     DISPLAY_LENGTH = 16;

	/** Search: The Editable Text Field         */
	private CTextField 			m_text = new CTextField (DISPLAY_LENGTH);
	/** Search: The Button to open Editor   */
	private CButton				m_button = new CButton();
	/** The Combo Box if not a Search Lookup    */
	private VComboBox			m_combo = new VComboBox();
	/** Indicator that value is being set       */
	private volatile boolean 	m_settingValue = false;
	private volatile boolean 	m_settingFocus = false;
	/** Indicator that Lookup has focus         */
	private volatile boolean	m_haveFocus = false;
	/** Indicator - inserting new value			*/
	private volatile boolean	m_inserting = false;

	//
	private String				m_columnName;
	private Lookup				m_lookup;
	private int					m_displayType;
	private int					m_WindowNo;

	private boolean				m_comboActive = true;
	private Object				m_value;

	//	Popup
	JPopupMenu 					popupMenu = new JPopupMenu();
	private JMenuItem 			mZoom;
	private JMenuItem 			mRefresh;
	private JMenuItem			mBPartnerNew;
	private JMenuItem			mBPartnerUpd;

	private MField              m_mField = null;

	/**
	 *  Set Content and Size of Compoments
	 *  @param initial if true, size and margins will be set
	 */
	private void setUI (boolean initial)
	{
		if (initial)
		{
			Dimension size = m_text.getPreferredSize();
			setPreferredSize(new Dimension(size));  //	causes r/o to be the same length
			m_combo.setPreferredSize(new Dimension(size));
			//
			m_text.setBorder(null);
			Dimension bSize = new Dimension(size.height, size.height);
			m_button.setPreferredSize (bSize);
			m_button.setMargin(new Insets(0, 0, 0, 0));
		}

		//	What to show
		this.remove(m_combo);
		this.remove(m_button);
		this.remove(m_text);
		//
		if (!isReadWrite())									//	r/o - show text only
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			m_text.setReadWrite(false);
			m_combo.setReadWrite(false);
			m_comboActive = false;
		}
		else if (m_displayType != DisplayType.Search)	    //	show combo if not Search
		{
			this.setBorder(null);
			this.add(m_combo, BorderLayout.CENTER);
			m_comboActive = true;
		}
		else 												//	Search or unstable - show text & button
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			this.add(m_button, BorderLayout.EAST);
			m_text.setReadWrite (true);
			m_comboActive = false;
		}
	}   //  setUI

	/**
	 *	Set ReadWrite
	 *  @param value ReadWrite
	 */
	public void setReadWrite (boolean value)
	{
		boolean rw = value;
		if (m_lookup == null)
			rw = false;
		if (m_combo.isReadWrite() != value)
		{
			m_combo.setReadWrite(rw);
			setUI (false);
			if (m_comboActive)
				setValue (m_value);
		}
	}	//	setReadWrite

	/**
	 *	IsEditable
	 *  @return is lookup ReadWrite
	 */
	public boolean isReadWrite()
	{
		return m_combo.isReadWrite();
	}	//	isReadWrite

	/**
	 *	Set Mandatory (and back bolor)
	 *  @param mandatory mandatory
	 */
	public void setMandatory (boolean mandatory)
	{
		m_combo.setMandatory(mandatory);
		m_text.setMandatory(mandatory);
	}	//	setMandatory

	/**
	 *	Is it mandatory
	 *  @return true if mandatory
	 */
	public boolean isMandatory()
	{
		return m_combo.isMandatory();
	}	//	isMandatory

	/**
	 *	Set Background
	 *  @param color color
	 */
	public void setBackground(Color color)
	{
		m_text.setBackground(color);
		m_combo.setBackground(color);
	}	//	setBackground

	/**
	 *	Set Background
	 *  @param error error
	 */
	public void setBackground (boolean error)
	{
		m_text.setBackground(error);
		m_combo.setBackground(error);
	}	//	setBackground

	/**
	 *  Set Foreground
	 *  @param fg Foreground color
	 */
	public void setForeground(Color fg)
	{
		m_text.setForeground(fg);
		m_combo.setForeground(fg);
	}   //  setForeground

	/**
	 *  Set Editor to value
	 *  @param value new Value
	 */
	public void setValue (Object value)
	{
		Log.trace(Log.l6_Database, "VLookup.setValue", m_columnName + "=" + value);
		m_settingValue = true;		//	disable actions
		m_value = value;

		//	Set both for switching
		m_combo.setValue (value);
		if (value == null)
		{
			m_text.setText (null);
			m_settingValue = false;
			return;
		}
		if (m_lookup == null)
		{
			m_text.setText (value.toString());
			m_settingValue = false;
			return;
		}

		//	Set Display
		String display = m_lookup.getDisplay(value);
		boolean notFound = display.startsWith("<") && display.startsWith(">");
		m_text.setText (display);
		m_text.setCaretPosition (0); //	show beginning

		//	Nothing showing in Combo and should be showing
		if (m_combo.getSelectedItem() == null
			&& (m_comboActive || (m_inserting && m_displayType != DisplayType.Search)))
		{
			//  lookup found nothing too
			if (notFound)
			{
				Log.trace(8, "VLookup.setValue - Not found (1)", display);
				//  we may have a new value
				m_lookup.refresh();
				m_combo.setValue (value);
				display = m_lookup.getDisplay(value);
				m_text.setText (display);
				m_text.setCaretPosition (0);	//	show beginning
				notFound = display.startsWith("<") && display.endsWith(">");
			}
			if (notFound)	//	<key>
			{
				m_value = null;
				actionCombo (null);             //  data binding
				Log.trace(Log.l6_Database, "VLookup.setValue - not found - " + value);
			}
			//  we have lookup
			else if (m_combo.getSelectedItem() == null)
			{
				NamePair pp = m_lookup.get(value);
				if (pp != null)
				{
					Log.trace (Log.l6_Database, "VLookup.setValue - added to combo - " + pp);
					//  Add to Combo
					m_combo.addItem (pp);
					m_combo.setValue (value);
				}
			}
			//  Not in Lookup - set to Null
			if (m_combo.getSelectedItem() == null)
			{
				Log.trace(Log.l1_User, "VLookup.setValue - not in Lookup - set to NULL");
				actionCombo (null);             //  data binding (calls setValue again)
				m_value = null;
			}
		}
		m_settingValue = false;
	}	//	setValue

	/**
	 *  Property Change Listener
	 *  @param evt PropertyChangeEvent
	 */
	public void propertyChange (PropertyChangeEvent evt)
	{
	//	Log.trace(Log.l5_DData, "VLookup.propertyChange", evt);
		if (evt.getPropertyName().equals(MField.PROPERTY))
		{
			m_inserting = MField.INSERTING.equals(evt.getOldValue());
			setValue(evt.getNewValue());
			m_inserting = false;
		}
	}   //  propertyChange

	/**
	 *	Return Editor value (Integer)
	 *  @return value
	 */
	public Object getValue()
	{
		if (m_comboActive)
			return m_combo.getValue ();
		return m_value;
	}	//	getValue

	/**
	 *  Return editor display
	 *  @return display value
	 */
	public String getDisplay()
	{
		String retValue = null;
		if (m_comboActive)
			retValue = m_combo.getDisplay();
		//  check lookup
		else if (m_lookup == null)
			retValue = m_value.toString();
		else
			retValue = m_lookup.getDisplay(m_value);
	//	Log.trace(Log.l6_Database, "VLookup.getDisplay - " + retValue, "ComboActive=" + m_comboActive);
		return retValue;
	}   //  getDisplay

	/**
	 *  Set Field/WindowNo for ValuePreference
	 *  @param mField Model Field for Lookup
	 */
	public void setField (MField mField)
	{
		m_mField = mField;
		if (m_mField != null)
			ValuePreference.addMenu (this, popupMenu);
	}   //  setField

	/*************************************************************************/

	/**
	 *	Action Listener	- data binding
	 *  @param e ActionEvent
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (m_settingValue || m_settingFocus)
			return;
		Log.trace(Log.l4_Data, "VLookup.actionPerformed", e.getActionCommand() + ", ComboValue=" + m_combo.getSelectedItem());
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());

		//  Preference
		if (e.getActionCommand().equals(ValuePreference.NAME))
		{
			ValuePreference.start (m_mField, getValue(), getDisplay());
			return;
		}

		//  Combo Selection
		else if (e.getSource() == m_combo)
		{
			Object value = getValue();
			Object o = m_combo.getSelectedItem();
			if (o != null)
			{
				String s = o.toString();
				//  don't allow selection of inactive
				if (s.startsWith(MLookup.INACTIVE_S) && s.endsWith(MLookup.INACTIVE_E))
				{
					Log.trace(Log.l1_User, "VLookup.actionPerformed - Selection inactive set to NULL");
					value = null;
				}
			}
			actionCombo (value);                //  data binding
		}
		//  Button pressed
		else if (e.getSource() == m_button)
			actionButton ("");
		//  Text entered
		else if (e.getSource() == m_text)
			actionText();

		//  Popup Menu
		else if (e.getSource() == mZoom)
			actionZoom();
		else if (e.getSource() == mRefresh)
			actionRefresh();
		else if (e.getSource() == mBPartnerNew)
			actionBPartner(true);
		else if (e.getSource() == mBPartnerUpd)
			actionBPartner(false);
	}	//	actionPerformed

	/**
	 *  Action Listener Interface
	 *  @param listener listener
	 */
	public void addActionListener(ActionListener listener)
	{
		m_combo.addActionListener(listener);
		m_text.addActionListener(listener);
	}   //  addActionListener

	/**
	 *	Action - Combo.
	 *  <br>
	 *	== dataBinding == inform of new value
	 *  <pre>
	 *  VLookup.actionCombo
	 *      GridController.vetoableChange
	 *          MTable.setValueAt
	 *              MField.setValue
	 *                  VLookup.setValue
	 *          MTab.dataStatusChanged
	 *  </pre>
	 *  @param value new value
	 */
	private void actionCombo (Object value)
	{
	//	Log.trace(Log.l6_Database, "VLookup.actionCombo", value==null ? "null" : value.toString());
		try
		{
			fireVetoableChange (m_columnName, null, value);
		}
		catch (PropertyVetoException pve)
		{
			Log.error("VLookup.actionCombo", pve);
		}
		//  is the value updated ?
		boolean updated = false;
		if (value == null && m_value == null)
			updated = true;
		else if (value != null && value.equals(m_value))
			updated = true;
		if (!updated)
		{
			//  happens if VLookup is used outside of APanel/GridController (no property listener)
			Log.trace(Log.l6_Database, "VLookup.actionCombo - Value explicitly set - new=" + value + ", old=" + m_value);
			setValue(value);
		}
	}	//	actionCombo


	/**
	 *	Action - Button.
	 *	- Call Info
	 *	@param queryValue initial query value
	 */
	private void actionButton (String queryValue)
	{
		m_button.setEnabled(false);                 //  disable double click
		requestFocus();                             //  closes other editors
		Frame frame = Env.getFrame(this);

		/**
		 *  Three return options:
		 *  - Value Selected & OK pressed   => store result => result has value
		 *  - Cancel pressed                => store null   => result == null && cancelled
		 *  - Window closed                 -> ignore       => result == null && !cancalled
		 */
		Object result = null;
		boolean cancelled = false;
		//
		String col = m_lookup.getColumnName();		//	fully qualified name
		if (col.indexOf(".") != -1)
			col = col.substring(col.indexOf(".")+1);
		//  Zoom / Validation
		String whereClause = whereClause = getWhereClause();
		//
		Log.trace(Log.l5_DData, "VLookup.actionButton - " + col 
			+ ", Zoom=" + m_lookup.getZoom()
			+ " (" + whereClause + ")");
		//
		boolean resetValue = false;	//	reset value so that is always treated as new entry    
		if (col.equals("M_Product_ID"))
		{
			//	Reset
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_Product_ID", "0");
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_AttributeSetInstance_ID", "0");
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = "@" + m_text.getText() + "@";   //  Name indicator - otherwise Value
			int M_Warehouse_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNo, "M_Warehouse_ID");
			int M_PriceList_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNo, "M_PriceList_ID");
			//	Show Info
			InfoProduct ip = new InfoProduct (frame, true, m_WindowNo,
				M_Warehouse_ID, M_PriceList_ID, queryValue, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
			resetValue = true;
		}
		else if (col.equals("C_BPartner_ID"))
		{
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = m_text.getText();
			boolean isSOTrx = true;     //  default
			if (Env.getContext(Env.getCtx(), m_WindowNo, "IsSOTrx").equals("N"))
				isSOTrx = false;
			InfoBPartner ip = new InfoBPartner (frame, true, m_WindowNo,
				queryValue, isSOTrx, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
		}
		else
		{
			if (m_tableName == null)	//	sets table anem & key column
				getDirectAccessSQL("*");
			Info ig = Info.create (frame, true, m_WindowNo, m_tableName, m_keyColumnName, queryValue, false, whereClause);
			ig.show();
			cancelled = ig.isCancelled();
			result = ig.getSelectedKey();
		}
		//  Result
		if (result != null)
		{
			Log.trace(Log.l4_Data, "VLookupactionButton", "Result = " + result.toString() + " " + result.getClass().getName());
			//  make sure that value is in cache
			m_lookup.getDirect(result, false, true);
			if (resetValue)
				actionCombo (null);
			actionCombo (result);
		}
		else if (cancelled)
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (cancelled)");
			actionCombo (null);
		}
		else
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (not cancelled)");
			setValue(m_value);      //  to re-display value
		}
		//
		m_button.setEnabled(true);
	}	//	actionButton

	/**
	 * 	Get Where Clause
	 *	@return where clause or ""
	 */
	private String getWhereClause()
	{
		String whereClause = "";
		if (m_lookup.getZoomQuery() != null)
			whereClause = m_lookup.getZoomQuery().getWhereClause();
		if (whereClause.length() == 0)
			whereClause = m_lookup.getValidation();
		return whereClause;
	}	//	getWhereClause

	/**
	 *	Check, if data returns unique entry, otherwise involve Info via Button
	 */
	private void actionText()
	{
		String text = m_text.getText();
		//	Nothing entered
		if (text == null || text.length() == 0 || text.equals("%"))
		{
			actionButton(text);
			return;
		}
		//	Always like
		if (!text.endsWith("%"))
			text += "%";
		text = text.toUpperCase();

		String finalSQL = getDirectAccessSQL(text);
		int id = 0;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(finalSQL);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				id = rs.getInt(1);		//	first
				if (rs.next())
					id = -1;			//	only if unique
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("VLookup.actionText\nSQL=" + finalSQL, e);
			id = -2;
		}
		Log.trace(Log.l5_DData, "VLookup.actionText ID => " + id);

		//	No (unique) result
		if (id <= 0)
		{
			Log.trace(Log.l6_Database, finalSQL);
			actionButton(m_text.getText());
			return;
		}
		m_value = null;     //  forces re-display if value is unchanged but text updated and still unique
		actionCombo (new Integer(id));          //  data binding
	}	//	actionText


	private String		m_tableName = null;
	private String		m_keyColumnName = null;

	/**
	 * 	Generate Access SQL for Search.
	 * 	The SQL returns the ID of the value entered
	 * 	Also sets m_tableName and m_keyColumnName
	 *	@param text upper like text
	 *	@return sql or ""
	 *  Example
	 *	SELECT C_Payment_ID FROM C_Payment WHERE UPPER(DocumentNo) LIKE x OR ...
	 */
	private String getDirectAccessSQL (String text)
	{
		StringBuffer sql = new StringBuffer();
		m_tableName = m_columnName.substring(0, m_columnName.length()-3);
		m_keyColumnName = m_columnName;
		//
		if (m_columnName.equals("M_Product_ID"))
		{
			//	Reset
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_Product_ID", "0");
			Env.setContext(Env.getCtx(), Env.WINDOW_INFO, Env.TAB_INFO, "M_AttributeSetInstance_ID", "0");
			//
			sql.append("SELECT M_Product_ID FROM M_Product WHERE UPPER(Value) LIKE ")
				.append(DB.TO_STRING(text))
				.append(" OR UPC LIKE ").append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_BPartner_ID"))
		{
			sql.append("SELECT C_BPartner_ID FROM C_BPartner WHERE UPPER(Value) LIKE ")
				.append(DB.TO_STRING(text))
				.append(" OR UPPER(Name) LIKE ").append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_Order_ID"))
		{
			sql.append("SELECT C_Order_ID FROM C_Order WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_Invoice_ID"))
		{
			sql.append("SELECT C_Invoice_ID FROM C_Invoice WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("M_InOut_ID"))
		{
			sql.append("SELECT M_InOut_ID FROM M_InOut WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("C_Payment_ID"))
		{
			sql.append("SELECT C_Payment_ID FROM C_Payment WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		else if (m_columnName.equals("GL_JournalBatch_ID"))
		{
			sql.append("SELECT GL_JournalBatch_ID FROM GL_JournalBatch WHERE UPPER(DocumentNo) LIKE ")
				.append(DB.TO_STRING(text));
		}
		/**
		else if (m_columnName.equals("SalesRep_ID"))
		{
			sql.append("SELECT AD_User_ID FROM AD_User WHERE UPPER(Name) LIKE ")
				.append(DB.TO_STRING(text));
			m_tableName = "AD_User";
			m_keyColumnName = "AD_User_ID";
		}
		**/
		//	Predefined
		if (sql.length() > 0)
		{
			String wc = getWhereClause();
			if (wc != null && wc.length() > 0)
				sql.append(" AND ").append(wc);
			sql.append(" AND IsActive='Y'");
			//	***
			return MRole.getDefault().addAccessSQL(sql.toString(),
				m_tableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
		}
		
		//	Check if it is a Table Reference
		if (m_lookup instanceof MLookup)
		{
			int AD_Reference_ID = ((MLookup)m_lookup).getAD_Reference_Value_ID();
			if (AD_Reference_ID != 0)
			{
				String query = "SELECT kc.ColumnName, dc.ColumnName, t.TableName "
					+ "FROM AD_Ref_Table rt"
					+ " INNER JOIN AD_Column kc ON (rt.AD_Key=kc.AD_Column_ID)"
					+ " INNER JOIN AD_Column dc ON (rt.AD_Display=dc.AD_Column_ID)"
					+ " INNER JOIN AD_Table t ON (rt.AD_Table_ID=t.AD_Table_ID) "
					+ "WHERE rt.AD_Reference_ID=?";
				String displayColumnName = null;
				PreparedStatement pstmt = null;
				try
				{
					pstmt = DB.prepareCall(query);
					pstmt.setInt(1, AD_Reference_ID);
					ResultSet rs = pstmt.executeQuery();
					if (rs.next())
					{
						m_keyColumnName = rs.getString(1);
						displayColumnName = rs.getString(2);
						m_tableName = rs.getString(3);
					}
					rs.close();
					pstmt.close();
					pstmt = null;
				}
				catch (Exception e)
				{
					Log.error("VLookup.getDirectAccessSQL", e);
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
				if (displayColumnName != null)
				{
					sql = new StringBuffer();
					sql.append("SELECT ").append(m_keyColumnName)
						.append(" FROM ").append(m_tableName)
						.append(" WHERE UPPER(").append(displayColumnName)
						.append(") LIKE ").append(DB.TO_STRING(text))
						.append(" AND IsActive='Y'");
					String wc = getWhereClause();
					if (wc != null && wc.length() > 0)
						sql.append(" AND ").append(wc);
					//	***
					return MRole.getDefault().addAccessSQL(sql.toString(),
								m_tableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
				}
			}	//	Table Reference
		}	//	MLookup
		
		/** Check Well Known Columns of Table - assumes TableDir	**/
		String query = "SELECT t.TableName, c.ColumnName "
			+ "FROM AD_Column c "
			+ " INNER JOIN AD_Table t ON (c.AD_Table_ID=t.AD_Table_ID AND t.IsView='N') "
			+ "WHERE c.ColumnName IN ('DocumentNo', 'Value', 'Name')"
			+ " AND EXISTS (SELECT * FROM AD_Column cc WHERE cc.AD_Table_ID=t.AD_Table_ID"
				+ " AND cc.IsKey='Y' AND cc.ColumnName=?)";
		sql = new StringBuffer();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(query);
			pstmt.setString(1, m_columnName);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				if (sql.length() != 0)
					sql.append(" OR ");
				m_tableName = rs.getString(1);
				sql.append("UPPER(").append(rs.getString(2)).append(") LIKE ").append(DB.TO_STRING(text));
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (SQLException ex)
		{
			Log.error("VLookup.getDirectAccessSQL", ex);
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
		}
		catch (SQLException ex1)
		{
		}
		pstmt = null;
		if (sql.length() == 0)
			return "";
		//
		StringBuffer retValue = new StringBuffer ("SELECT ")
			.append(m_columnName).append(" FROM ").append(m_tableName)
			.append(" WHERE ").append(sql)
			.append(" AND IsActive='Y'");
		String wc = getWhereClause();
		if (wc != null && wc.length() > 0)
			retValue.append(" AND ").append(wc);
		//	***
		return MRole.getDefault().addAccessSQL(retValue.toString(),
					m_tableName, MRole.SQL_NOTQUALIFIED, MRole.SQL_RO);
	}	//	getDirectAccessSQL


	/**
	 *	Action - Special BPartner Screen
	 *  @param newRecord true if new record should be created
	 */
	private void actionBPartner (boolean newRecord)
	{
		VBPartner vbp = new VBPartner (Env.getFrame(this), m_WindowNo);
		int BPartner_ID = 0;
		//  if update, get current value
		if (!newRecord)
		{
			if (m_value instanceof Integer)
				BPartner_ID = ((Integer)m_value).intValue();
			else if (m_value != null)
				BPartner_ID = Integer.parseInt(m_value.toString());
		}

		vbp.loadBPartner (BPartner_ID);
		vbp.show();
		//  get result
		int result = vbp.getBPartner_ID();
		if (result == 0					//	0 = not saved
			&& result == BPartner_ID)	//	the same
			return;
		//  Maybe new BPartner - put in cache
		m_lookup.getDirect(new Integer(result), false, true);

		actionCombo (new Integer(result));      //  data binding
	}	//	actionBPartner

	/**
	 *	Action - Zoom
	 */
	private void actionZoom()
	{
		if (m_lookup == null)
			return;
		//
		String IsSOTrx = Env.getContext(Env.getCtx(), m_WindowNo, "IsSOTrx");
		int AD_Window_ID = m_lookup.getZoom(IsSOTrx);
		MQuery zoomQuery = m_lookup.getZoomQuery();
		Log.trace(Log.l1_User, "VLookup.actionZoom - " + AD_Window_ID + " - IsSOTrx=" + IsSOTrx + " - Query=" + zoomQuery + " - Value=" + getValue());
		//	If not already exist
		if (zoomQuery == null || (!zoomQuery.isActive() && getValue() != null))
		{
			zoomQuery = new MQuery();
			zoomQuery.addRestriction(m_columnName, MQuery.EQUAL, getValue());
		}
		//
		setCursor(Cursor.getDefaultCursor());
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		AWindow frame = new AWindow();
		if (!frame.initWindow(AD_Window_ID, zoomQuery, IsSOTrx.equals("Y")))
		{
			setCursor(Cursor.getDefaultCursor());
			ValueNamePair pp = Log.retrieveError();
			String msg = pp==null ? "AccessTableNoView" : pp.getValue();
			ADialog.error(m_WindowNo, this, msg, pp==null ? "" : pp.getName());
		}
		else
			AEnv.showCenterScreen(frame);
			//  async window - not able to get feedback
		frame = null;
		//
		setCursor(Cursor.getDefaultCursor());
	}	//	actionZoom

	/**
	 *	Action - Refresh
	 */
	private void actionRefresh()
	{
		if (m_lookup == null)
			return;
		//
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		Object obj = m_combo.getSelectedItem();
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + obj);
		m_lookup.refresh();
		if (m_lookup.isValidated())
			m_lookup.fillComboBox(isMandatory(), false, false, false);
		else
			m_lookup.fillComboBox(isMandatory(), true, false, false);
		m_combo.setSelectedItem(obj);
	//	m_combo.revalidate();
		//
		setCursor(Cursor.getDefaultCursor());
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + m_combo.getSelectedItem());
	}	//	actionRefresh

	/*************************************************************************/

	/**
	 *	Focus Listener for ComboBoxes with missing Validation or invalid entries
	 *	- Requery listener for updated list
	 *  @param e FocusEvent
	 */
	public void focusGained (FocusEvent e)
	{
		if (e.getSource() != m_combo || e.isTemporary() || m_haveFocus)
			return;
		if (m_lookup.isValidated() && !m_lookup.hasInactive())
			return;
		//
		m_haveFocus = true;     //  prevents calling focus gained twice
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Object obj = m_lookup.getSelectedItem();
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Start    Count=" + m_combo.getItemCount() + ", Selected=" + obj);
		Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());
		m_lookup.fillComboBox(isMandatory(), true, true, true);     //  only validated & active & temporary
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Update   Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		m_lookup.setSelectedItem(obj);
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Selected Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		//
		m_settingFocus = false;
	}	//	focusGained

	/**
	 *	Reset Selection List
	 *  @param e FocusEvent
	 */
	public void focusLost(FocusEvent e)
	{
		if (e.getSource() != m_combo || e.isTemporary())
			return;
		if (m_lookup.isValidated() && !m_lookup.hasInactive())
			return;
		//
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Log.trace(Log.l4_Data, "VLookup.focusLost", m_columnName + " = " + m_combo.getSelectedItem());
		Object obj = m_combo.getSelectedItem();
		//	set original model
		if (!m_lookup.isValidated())
			m_lookup.fillComboBox(true);    //  previous selection
		//	Set value
		if (obj != null)
		{
			m_combo.setSelectedItem(obj);
			//	original model may not have item
			if (!m_combo.getSelectedItem().equals(obj))
			{
				Log.trace(Log.l6_Database, "VLookup.focusLost " + m_columnName, "added to combo - " + obj);
				m_combo.addItem(obj);
				m_combo.setSelectedItem(obj);
			}
		}
	//	actionCombo(getValue());
		m_settingFocus = false;
		m_haveFocus = false;    //  can gain focus again
	}	//	focusLost

	/**
	 *  Set ToolTip
	 *  @param text tool tip text
	 */
	public void setToolTipText(String text)
	{
		super.setToolTipText(text);
		m_button.setToolTipText(text);
		m_text.setToolTipText(text);
		m_combo.setToolTipText(text);
	}   //  setToolTipText

}	//	VLookup

/*****************************************************************************/

/**
 *	Mouse Listener for Popup Menu
 */
final class VLookup_mouseAdapter extends java.awt.event.MouseAdapter
{
	/**
	 *	Constructor
	 *  @param adaptee adaptee
	 */
	VLookup_mouseAdapter(VLookup adaptee)
	{
		this.adaptee = adaptee;
	}	//	VLookup_mouseAdapter

	private VLookup adaptee;

	/**
	 *	Mouse Listener
	 *  @param e MouseEvent
	 */
	public void mouseClicked(MouseEvent e)
	{
	//	System.out.println("mouseClicked " + e.getID() + " " + e.getSource().getClass().toString());
		//	popup menu
		if (SwingUtilities.isRightMouseButton(e))
			adaptee.popupMenu.show((Component)e.getSource(), e.getX(), e.getY());
	}	//	mouse Clicked

}	//	VLookup_mouseAdapter
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
package org.compiere.grid.ed;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.text.*;
import java.util.*;
import java.math.*;
import java.beans.*;
import java.sql.*;

import org.compiere.apps.*;
import org.compiere.util.*;
import org.compiere.model.*;
import org.compiere.apps.search.*;
import org.compiere.plaf.*;
import org.compiere.swing.*;

/**
 *  Lookup Field.
 *  <p>
 *	    When r/o - display a Label
 *		When STABLE - display a ComboBox
 *		Otherwise show Selection Dialog
 *  <p>
 *  Sepecial handling of BPartner and Product
 *
 *  @author 	Jorg Janke
 *  @version 	$Id: VLookup.java,v 1.43 2003/07/16 19:08:57 jjanke Exp $
 */
public class VLookup extends JComponent
	implements VEditor, ActionListener, FocusListener
{
	/**
	 *  Create BPartner Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createBPartner (int WindowNo)
	{
		int AD_Column_ID = 3499;    //  C_Invoice.C_BPartner_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, AD_Column_ID, DisplayType.Search);
			return new VLookup ("C_BPartner_ID", false, false, true, lookup, DisplayType.Search, WindowNo);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createBPartner", e);
		}
		return null;
	}   //  createBPartner

	/**
	 *  Create Product Lookup
	 *  @param WindowNo window
	 *  @return VLookup
	 */
	public static VLookup createProduct (int WindowNo)
	{
		int AD_Column_ID = 3840;    //  C_InvoiceLine.M_Product_ID
		try
		{
			Lookup lookup = MLookupFactory.get (Env.getCtx(), WindowNo, AD_Column_ID, DisplayType.Search);
			return new VLookup ("M_Product_ID", false, false, true, lookup, DisplayType.Search, WindowNo);
		}
		catch (Exception e)
		{
			Log.error("VLookup.createProduct", e);
		}
		return null;
	}   //  createProduct

	/*************************************************************************/

	/**
	 *	IDE Default Constructor
	 */
	public VLookup()
	{
		this("Lookup", false, false, true, null, 0, 0);
	}	//	VLookup


	/**
	 *	Detail Constructor
	 *
	 *  @param columnName column
	 *  @param mandatory mandatory
	 *  @param isReadOnly read only
	 *  @param isUpdateable updateable
	 *  @param lookup lookup
	 *  @param displayType display type
	 *  @param WindowNo window no
	 */
	public VLookup (String columnName, boolean mandatory, boolean isReadOnly, boolean isUpdateable,
		Lookup lookup, int displayType, int WindowNo)
	{
		super();
		m_columnName = columnName;
		m_lookup = lookup;
		if (lookup == null)
			Log.trace(Log.l3_Util, "VLookup", "Lookup is NULL = " + columnName);
		setMandatory(mandatory);
		m_displayType = displayType;
		m_WindowNo = WindowNo;		//	for Info
		//
		setLayout(new BorderLayout());
		VLookup_mouseAdapter mouse = new VLookup_mouseAdapter(this);    //  popup

		//	***	Text & Button	***
		m_text.addActionListener(this);
		m_text.addMouseListener(mouse);
		//  Button
		m_button.addActionListener(this);
		m_button.addMouseListener(mouse);
		m_button.setFocusable(false);   //  don't focus when tabbing
		if (columnName.equals("C_BPartner_ID"))
			m_button.setIcon(Env.getImageIcon("BPartner10.gif"));
		else if (columnName.equals("M_Product_ID"))
			m_button.setIcon(Env.getImageIcon("Product10.gif"));
		else
			m_button.setIcon(Env.getImageIcon("PickOpen10.gif"));

		//	*** VComboBox	***
		if (m_lookup != null && displayType != DisplayType.Search)	//	No Search
		{
			//  Memory Leak after executing the next two lines ??
			m_lookup.fillComboBox (isMandatory(), false, false, false);
			m_combo.setModel(m_lookup);
			//
			m_combo.addActionListener(this);							//	Selection
			m_combo.addMouseListener(mouse);	                        //	popup
			//	FocusListener to refresh selection before opening
			if (!m_lookup.isValidated() || m_lookup.hasInactive())
				m_combo.addFocusListener(this);
		}

		setUI (true);
		//	ReadWrite	-	decides what components to show
		if (isReadOnly || !isUpdateable || m_lookup == null)
			setReadWrite(false);
		else
			setReadWrite(true);

		//	Popup
		if (m_lookup != null)
		{
			if ((displayType == DisplayType.List && Env.getContextAsInt(Env.getCtx(), "#AD_Role_ID") == 0)
				|| displayType != DisplayType.List)     //  only system admins can change lists, so no need to zoom for others
			{
				mZoom = new JMenuItem(Msg.getMsg(Env.getCtx(), "Zoom"), Env.getImageIcon("Zoom16.gif"));
				mZoom.addActionListener(this);
				popupMenu.add(mZoom);
			}
			mRefresh = new JMenuItem(Msg.getMsg(Env.getCtx(), "Refresh"), Env.getImageIcon("Refresh16.gif"));
			mRefresh.addActionListener(this);
			popupMenu.add(mRefresh);
		}
		//	VBPartner quick entry link
		if (columnName.equals("C_BPartner_ID"))
		{
			mBPartnerNew = new JMenuItem (Msg.getMsg(Env.getCtx(), "New"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerNew.addActionListener(this);
			popupMenu.add(mBPartnerNew);
			mBPartnerUpd = new JMenuItem (Msg.getMsg(Env.getCtx(), "Update"), Env.getImageIcon("InfoBPartner16.gif"));
			mBPartnerUpd.addActionListener(this);
			popupMenu.add(mBPartnerUpd);
		}
		//
		if (m_lookup != null && m_lookup.getZoom() == 0)
			mZoom.setEnabled(false);
	}	//	VLookup

	/**
	 *  Dispose
	 */
	public void dispose()
	{
		m_text = null;
		m_button = null;
		m_lookup = null;
		m_mField = null;
		m_combo.removeActionListener(this);
		m_combo.setModel(new DefaultComboBoxModel());    //  remove reference
	//	m_combo.removeAllItems();
		m_combo = null;
	}   //  dispose

	/** Display Length for Lookups (18)         */
	public final static int     DISPLAY_LENGTH = 18;

	/** Search: The Editable Text Field         */
	private CTextField 			m_text = new CTextField (DISPLAY_LENGTH);
	/** Search: The Button to open Editor   */
	private CButton				m_button = new CButton();
	/** The Combo Box if not a Search Lookup    */
	private VComboBox			m_combo = new VComboBox();
	/** Indicator that value is being set       */
	private volatile boolean 	m_settingValue = false;
	private volatile boolean 	m_settingFocus = false;
	/** Indicator that Lookup has focus         */
	private volatile boolean	m_haveFocus = false;
	/** Indicator - inserting new value			*/
	private volatile boolean	m_inserting = false;

	//
	private String				m_columnName;
	private Lookup				m_lookup;
	private int					m_displayType;
	private int					m_WindowNo;

	private boolean				m_comboActive = true;
	private Object				m_value;

	//	Popup
	JPopupMenu 					popupMenu = new JPopupMenu();
	private JMenuItem 			mZoom;
	private JMenuItem 			mRefresh;
	private JMenuItem			mBPartnerNew;
	private JMenuItem			mBPartnerUpd;

	private MField              m_mField = null;

	/**
	 *  Set Content and Size of Compoments
	 *  @param initial if true, size and margins will be set
	 */
	private void setUI (boolean initial)
	{
		if (initial)
		{
			Dimension size = m_text.getPreferredSize();
			setPreferredSize(new Dimension(size));  //	causes r/o to be the same length
			m_combo.setPreferredSize(new Dimension(size));
			//
			m_text.setBorder(null);
			Dimension bSize = new Dimension(size.height, size.height);
			m_button.setPreferredSize (bSize);
			m_button.setMargin(new Insets(0, 0, 0, 0));
		}

		//	What to show
		this.remove(m_combo);
		this.remove(m_button);
		this.remove(m_text);
		//
		if (!isReadWrite())									//	r/o - show text only
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			m_text.setReadWrite(false);
			m_combo.setReadWrite(false);
			m_comboActive = false;
		}
		else if (m_displayType != DisplayType.Search)	    //	show combo if not Search
		{
			this.setBorder(null);
			this.add(m_combo, BorderLayout.CENTER);
			m_comboActive = true;
		}
		else 												//	Search or unstable - show text & button
		{
			LookAndFeel.installBorder(this, "TextField.border");
			this.add(m_text, BorderLayout.CENTER);
			this.add(m_button, BorderLayout.EAST);
			m_text.setReadWrite (true);
			m_comboActive = false;
		}
	}   //  setUI

	/**
	 *	Set ReadWrite
	 *  @param value ReadWrite
	 */
	public void setReadWrite (boolean value)
	{
		boolean rw = value;
		if (m_lookup == null)
			rw = false;
		if (m_combo.isReadWrite() != value)
		{
			m_combo.setReadWrite(rw);
			setUI (false);
			if (m_comboActive)
				setValue (m_value);
		}
	}	//	setReadWrite

	/**
	 *	IsEditable
	 *  @return is lookup ReadWrite
	 */
	public boolean isReadWrite()
	{
		return m_combo.isReadWrite();
	}	//	isReadWrite

	/**
	 *	Set Mandatory (and back bolor)
	 *  @param mandatory mandatory
	 */
	public void setMandatory (boolean mandatory)
	{
		m_combo.setMandatory(mandatory);
		m_text.setMandatory(mandatory);
	}	//	setMandatory

	/**
	 *	Is it mandatory
	 *  @return true if mandatory
	 */
	public boolean isMandatory()
	{
		return m_combo.isMandatory();
	}	//	isMandatory

	/**
	 *	Set Background
	 *  @param color color
	 */
	public void setBackground(Color color)
	{
		m_text.setBackground(color);
		m_combo.setBackground(color);
	}	//	setBackground

	/**
	 *	Set Background
	 *  @param error error
	 */
	public void setBackground (boolean error)
	{
		m_text.setBackground(error);
		m_combo.setBackground(error);
	}	//	setBackground

	/**
	 *  Set Foreground
	 *  @param fg Foreground color
	 */
	public void setForeground(Color fg)
	{
		m_text.setForeground(fg);
		m_combo.setForeground(fg);
	}   //  setForeground

	/**
	 *  Set Editor to value
	 *  @param value new Value
	 */
	public void setValue (Object value)
	{
		Log.trace(Log.l6_Database, "VLookup.setValue", m_columnName + "=" + value);
		m_settingValue = true;		//	disable actions
		m_value = value;

		//	Set both for switching
		m_combo.setValue (value);
		if (value == null)
		{
			m_text.setText (null);
			m_settingValue = false;
			return;
		}
		if (m_lookup == null)
		{
			m_text.setText (value.toString());
			m_settingValue = false;
			return;
		}

		//	Set Display
		String display = m_lookup.getDisplay(value);
		boolean notFound = display.startsWith("<") && display.startsWith(">");
		m_text.setText (display);
		m_text.setCaretPosition (0); //	show beginning

		//	Nothing showing in Combo and should be showing
		if (m_combo.getSelectedItem() == null
			&& (m_comboActive || (m_inserting && m_displayType != DisplayType.Search)))
		{
			//  lookup found nothing too
			if (notFound)
			{
				Log.trace(8, "VLookup.setValue - Not found (1)", display);
				//  we may have a new value
				m_lookup.refresh();
				m_combo.setValue (value);
				display = m_lookup.getDisplay(value);
				m_text.setText (display);
				m_text.setCaretPosition (0);	//	show beginning
				notFound = display.startsWith("<") && display.endsWith(">");
			}
			if (notFound)	//	<key>
			{
				m_value = null;
				actionCombo (null);             //  data binding
				Log.trace(Log.l6_Database, "VLookup.setValue - not found - " + value);
			}
			//  we have lookup
			else if (m_combo.getSelectedItem() == null)
			{
				NamePair pp = m_lookup.get(value);
				if (pp != null)
				{
					Log.trace (Log.l6_Database, "VLookup.setValue - added to combo - " + pp);
					//  Add to Combo
					m_combo.addItem (pp);
					m_combo.setValue (value);
				}
			}
			//  Not in Lookup - set to Null
			if (m_combo.getSelectedItem() == null)
			{
				Log.trace(Log.l1_User, "VLookup.setValue - not in Lookup - set to NULL");
				actionCombo (null);             //  data binding (calls setValue again)
				m_value = null;
			}
		}
		m_settingValue = false;
	}	//	setValue

	/**
	 *  Property Change Listener
	 *  @param evt PropertyChangeEvent
	 */
	public void propertyChange (PropertyChangeEvent evt)
	{
	//	Log.trace(Log.l5_DData, "VLookup.propertyChange", evt);
		if (evt.getPropertyName().equals(MField.PROPERTY))
		{
			m_inserting = MField.INSERTING.equals(evt.getOldValue());
			setValue(evt.getNewValue());
			m_inserting = false;
		}
	}   //  propertyChange

	/**
	 *	Return Editor value (Integer)
	 *  @return value
	 */
	public Object getValue()
	{
		if (m_comboActive)
			return m_combo.getValue ();
		return m_value;
	}	//	getValue

	/**
	 *  Return editor display
	 *  @return display value
	 */
	public String getDisplay()
	{
		String retValue = null;
		if (m_comboActive)
			retValue = m_combo.getDisplay();
		//  check lookup
		else if (m_lookup == null)
			retValue = m_value.toString();
		else
			retValue = m_lookup.getDisplay(m_value);
	//	Log.trace(Log.l6_Database, "VLookup.getDisplay - " + retValue, "ComboActive=" + m_comboActive);
		return retValue;
	}   //  getDisplay

	/**
	 *  Set Field/WindowNo for ValuePreference
	 *  @param mField Model Field for Lookup
	 */
	public void setField (MField mField)
	{
		m_mField = mField;
		if (m_mField != null)
			ValuePreference.addMenu (this, popupMenu);
	}   //  setField

	/*************************************************************************/

	/**
	 *	Action Listener	- data binding
	 *  @param e ActionEvent
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (m_settingValue || m_settingFocus)
			return;
		Log.trace(Log.l4_Data, "VLookup.actionPerformed", e.getActionCommand() + ", ComboValue=" + m_combo.getSelectedItem());
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());

		//  Preference
		if (e.getActionCommand().equals(ValuePreference.NAME))
		{
			ValuePreference.start (m_mField, getValue(), getDisplay());
			return;
		}

		//  Combo Selection
		else if (e.getSource() == m_combo)
		{
			Object value = getValue();
			Object o = m_combo.getSelectedItem();
			if (o != null)
			{
				String s = o.toString();
				//  don't allow selection of inactive
				if (s.startsWith(MLookup.INACTIVE_S) && s.endsWith(MLookup.INACTIVE_E))
				{
					Log.trace(Log.l1_User, "VLookup.actionPerformed - Selection inactive set to NULL");
					value = null;
				}
			}
			actionCombo (value);                //  data binding
		}
		//  Button pressed
		else if (e.getSource() == m_button)
			actionButton ("");
		//  Text entered
		else if (e.getSource() == m_text)
			actionText();

		//  Popup Menu
		else if (e.getSource() == mZoom)
			actionZoom();
		else if (e.getSource() == mRefresh)
			actionRefresh();
		else if (e.getSource() == mBPartnerNew)
			actionBPartner(true);
		else if (e.getSource() == mBPartnerUpd)
			actionBPartner(false);
	}	//	actionPerformed

	/**
	 *  Action Listener Interface
	 *  @param listener listener
	 */
	public void addActionListener(ActionListener listener)
	{
		m_combo.addActionListener(listener);
		m_text.addActionListener(listener);
	}   //  addActionListener

	/**
	 *	Action - Combo.
	 *  <br>
	 *	== dataBinding == inform of new value
	 *  <pre>
	 *  VLookup.actionCombo
	 *      GridController.vetoableChange
	 *          MTable.setValueAt
	 *              MField.setValue
	 *                  VLookup.setValue
	 *          MTab.dataStatusChanged
	 *  </pre>
	 *  @param value new value
	 */
	private void actionCombo (Object value)
	{
	//	Log.trace(Log.l6_Database, "VLookup.actionCombo", value==null ? "null" : value.toString());
		try
		{
			fireVetoableChange (m_columnName, null, value);
		}
		catch (PropertyVetoException pve)
		{
			Log.error("VLookup.actionCombo", pve);
		}
		//  is the value updated ?
		boolean updated = false;
		if (value == null && m_value == null)
			updated = true;
		else if (value != null && value.equals(m_value))
			updated = true;
		if (!updated)
		{
			//  happens if VLookup is used outside of APanel/GridController (no property listener)
			Log.trace(Log.l6_Database, "VLookup.actionCombo - Value explicitly set - new=" + value + ", old=" + m_value);
			setValue(value);
		}
	}	//	actionCombo


	/**
	 *	Action - Button.
	 *	- Call Info
	 *	@param queryValue initial query value
	 */
	private void actionButton (String queryValue)
	{
		m_button.setEnabled(false);                 //  disable double click
		requestFocus();                             //  closes other editors
		Frame frame = Env.getFrame(this);

		/**
		 *  Three return options:
		 *  - Value Selected & OK pressed   => store result => result has value
		 *  - Cancel pressed                => store null   => result == null && cancelled
		 *  - Window closed                 -> ignore       => result == null && !cancalled
		 */
		Object result = null;
		boolean cancelled = false;
		//
		String col = m_lookup.getColumnName();		//	fully qualified name
		if (col.indexOf(".") != -1)
			col = col.substring(col.indexOf(".")+1);
		//  Zoom / Validation
		String whereClause = "";
		if (m_lookup.getZoomQuery() != null)
			whereClause = m_lookup.getZoomQuery().getWhereClause();
		if (whereClause.length() == 0)
			whereClause = m_lookup.getValidation();
		//
		Log.trace(Log.l5_DData, "VLookup.actionButton - " + col + ", Zoom=" + m_lookup.getZoom()
			+ " (" + whereClause + ")");
		//
		boolean resetValue = false;             //  reset value so that is always treated as new entry
		if (col.equals("M_Product_ID"))
		{
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = "@" + m_text.getText() + "@";   //  Name indicator - otherwise Value
			int M_Warehouse_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNo, "M_Warehouse_ID");
			int M_PriceList_ID = Env.getContextAsInt(Env.getCtx(), m_WindowNo, "M_PriceList_ID");
			InfoProduct ip = new InfoProduct (frame, true, m_WindowNo,
				M_Warehouse_ID, M_PriceList_ID, queryValue, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
			resetValue = true;
		}
		else if (col.equals("C_BPartner_ID"))
		{
			//  Replace Value with name if no value exists
			if (queryValue.length() == 0 && m_text.getText().length() > 0)
				queryValue = m_text.getText();
			boolean isSOTrx = true;     //  default
			if (Env.getContext(Env.getCtx(), m_WindowNo, "IsSOTrx").equals("N"))
				isSOTrx = false;
			InfoBPartner ip = new InfoBPartner (frame, true, m_WindowNo,
				queryValue, isSOTrx, false, whereClause);
			ip.show();
			cancelled = ip.isCancelled();
			result = ip.getSelectedKey();
		}
		else
		{
			String tableName = col;
			int index = m_columnName.indexOf("_ID");
			if (index != -1)
				tableName = m_columnName.substring(0, index);
			Info ig = Info.create (frame, true, m_WindowNo, tableName, m_columnName, queryValue, false, whereClause);
			ig.show();
			cancelled = ig.isCancelled();
			result = ig.getSelectedKey();
		}
		//  Result
		if (result != null)
		{
			Log.trace(Log.l4_Data, "VLookupactionButton", "Result = " + result.toString() + " " + result.getClass().getName());
			//  make sure that value is in cache
			m_lookup.getDirect(result, true);
			if (resetValue)
				actionCombo (null);
			actionCombo (result);
		}
		else if (cancelled)
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (cancelled)");
			actionCombo (null);
		}
		else
		{
			Log.trace(Log.l4_Data, "VLookup.actionButton", "Result = null (not cancelled)");
			setValue(m_value);      //  to re-display value
		}
		//
		m_button.setEnabled(true);
	}	//	actionButton

	/**
	 *	Check, if data returns unique entry, otherwise involve Info via Button
	 */
	private void actionText()
	{
		String text = m_text.getText();
		//	Nothing entered
		if (m_text.getText().length() == 0)
		{
			actionButton(text);
			return;
		}
		//	Always like
		if (!text.endsWith("%"))
			text += "%";

		StringBuffer SQL = new StringBuffer();
		if (m_columnName.equals("M_Product_ID"))
		{
			SQL.append("SELECT M_Product_ID FROM M_Product WHERE UPPER(Value) LIKE '")
				.append(text.toUpperCase()).append("'");
		}
		else if (m_columnName.equals("C_BPartner_ID"))
		{
			SQL.append("SELECT C_BPartner_ID FROM C_BPartner WHERE (UPPER(Value) LIKE '")
				.append(text.toUpperCase())
				.append("' OR UPPER(Name) LIKE '").append(text.toUpperCase()).append("')");
		}
		else if (m_columnName.equals("C_Order_ID"))
		{
			SQL.append("SELECT C_Order_ID FROM C_Order WHERE UPPER(DocumentNo) LIKE '")
				.append(text.toUpperCase()).append("'");
		}
		else if (m_columnName.equals("C_Invoice_ID"))
		{
			SQL.append("SELECT C_Invoice_ID FROM C_Invoice WHERE UPPER(DocumentNo) LIKE '")
				.append(text.toUpperCase()).append("'");
		}
		else if (m_columnName.equals("M_InOut_ID"))
		{
			SQL.append("SELECT M_InOut_ID FROM M_InOut WHERE UPPER(DocumentNo) LIKE '")
				.append(text.toUpperCase()).append("'");
		}
		else if (m_columnName.equals("C_Payment_ID"))
		{
			SQL.append("SELECT C_Payment_ID FROM C_Payment WHERE UPPER(DocumentNo) LIKE '")
				.append(text.toUpperCase()).append("'");
		}
		else
		{
			actionButton(text);
			return;
		}
		//	Finish SQL
		SQL.append(" AND IsActive='Y'");
		//	AddSecurity
		String tableName = m_columnName.substring(0, m_columnName.length()-3);
		String finalSQL = Access.addROAccessSQL(Env.getCtx(), SQL.toString(), tableName, false);
		int id = 0;
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(finalSQL);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
			{
				id = rs.getInt(1);		//	first
				if (rs.next())
					id = -1;			//	only if unique
			}
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error("VLookup.actionText\nSQL=" + finalSQL, e);
			id = -2;
		}
		Log.trace(Log.l5_DData, "VLookup.actionText ID => " + id);

		//	No (unique) result
		if (id <= 0)
		{
			Log.trace(Log.l6_Database, finalSQL);
			actionButton(text);
			return;
		}
		m_value = null;     //  forces re-display if value is unchanged but text updated and still unique
		actionCombo (new Integer(id));          //  data binding
	}	//	actionText

	/**
	 *	Action - Special BPartner Screen
	 *  @param newRecord true if new record should be created
	 */
	private void actionBPartner (boolean newRecord)
	{
		VBPartner vbp = new VBPartner (Env.getFrame(this), m_WindowNo);
		int BPartner_ID = 0;
		//  if update, get current value
		if (!newRecord)
		{
			if (m_value instanceof Integer)
				BPartner_ID = ((Integer)m_value).intValue();
			else if (m_value != null)
				BPartner_ID = Integer.parseInt(m_value.toString());
		}

		vbp.loadBPartner (BPartner_ID);
		vbp.show();
		//  get result
		int result = vbp.getBPartner_ID();
		if (result == 0					//	0 = not saved
			&& result == BPartner_ID)	//	the same
			return;
		//  Maybe new BPartner - put in cache
		m_lookup.getDirect(new Integer(result), true);

		actionCombo (new Integer(result));      //  data binding
	}	//	actionBPartner

	/**
	 *	Action - Zoom
	 */
	private void actionZoom()
	{
		if (m_lookup == null)
			return;
		//
		String IsSOTrx = Env.getContext(Env.getCtx(), m_WindowNo, "IsSOTrx");
		int AD_Window_ID = m_lookup.getZoom(IsSOTrx);
		MQuery zoomQuery = m_lookup.getZoomQuery();
		Log.trace(Log.l1_User, "VLookup.actionZoom - " + AD_Window_ID + " - IsSOTrx=" + IsSOTrx + " - Query=" + zoomQuery + " - Value=" + getValue());
		//	If not already exist
		if (zoomQuery == null || (!zoomQuery.isActive() && getValue() != null))
		{
			zoomQuery = new MQuery();
			zoomQuery.addRestriction(m_columnName, MQuery.EQUAL, getValue());
		}
		//
		setCursor(Cursor.getDefaultCursor());
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		AWindow frame = new AWindow();
		if (!frame.initWindow(AD_Window_ID, zoomQuery, IsSOTrx.equals("Y")))
		{
			setCursor(Cursor.getDefaultCursor());
			ValueNamePair pp = Log.retrieveError();
			String msg = pp==null ? "AccessTableNoView" : pp.getValue();
			ADialog.error(m_WindowNo, this, msg, pp==null ? "" : pp.getName());
		}
		else
			AEnv.showCenterScreen(frame);
			//  async window - not able to get feedback
		frame = null;
		//
		setCursor(Cursor.getDefaultCursor());
	}	//	actionZoom

	/**
	 *	Action - Refresh
	 */
	private void actionRefresh()
	{
		if (m_lookup == null)
			return;
		//
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//
		Object obj = m_combo.getSelectedItem();
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + obj);
		m_lookup.refresh();
		if (m_lookup.isValidated())
			m_lookup.fillComboBox(isMandatory(), false, false, false);
		else
			m_lookup.fillComboBox(isMandatory(), true, false, false);
		m_combo.setSelectedItem(obj);
	//	m_combo.revalidate();
		//
		setCursor(Cursor.getDefaultCursor());
		Log.trace(Log.l1_User, "VLookup.actionRefresh - #" + m_lookup.getSize(), "Selected=" + m_combo.getSelectedItem());
	}	//	actionRefresh

	/*************************************************************************/

	/**
	 *	Focus Listener for ComboBoxes with missing Validation or invalid entries
	 *	- Requery listener for updated list
	 *  @param e FocusEvent
	 */
	public void focusGained (FocusEvent e)
	{
		if (e.getSource() != m_combo || e.isTemporary() || m_haveFocus)
			return;
		//
		m_haveFocus = true;     //  prevents calling focus gained twice
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Object obj = m_lookup.getSelectedItem();
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Start    Count=" + m_combo.getItemCount() + ", Selected=" + obj);
	//	Log.trace(Log.l5_DData, "VLookupHash=" + this.hashCode());
		m_lookup.fillComboBox(isMandatory(), true, true, true);     //  only validated & active & temporary
	//	Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Update   Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		m_lookup.setSelectedItem(obj);
		Log.trace(Log.l4_Data, "VLookup.focusGained", m_columnName + " Selected Count=" + m_combo.getItemCount() + ", Selected=" + m_lookup.getSelectedItem());
		//
		m_settingFocus = false;
	}	//	focusGained

	/**
	 *	Reset Selection List
	 *  @param e FocusEvent
	 */
	public void focusLost(FocusEvent e)
	{
		if (e.getSource() != m_combo || e.isTemporary())
			return;
		//
		m_settingFocus = true;  //  prevents actionPerformed
		//
		Log.trace(Log.l4_Data, "VLookup.focusLost", m_columnName + " = " + m_combo.getSelectedItem());
		Object obj = m_combo.getSelectedItem();
		//	set original model
		if (!m_lookup.isValidated())
			m_lookup.fillComboBox(true);    //  previous selection
		//	Set value
		if (obj != null)
		{
			m_combo.setSelectedItem(obj);
			//	original model may not have item
			if (!m_combo.getSelectedItem().equals(obj))
			{
				Log.trace(Log.l6_Database, "VLookup.focusLost " + m_columnName, "added to combo - " + obj);
				m_combo.addItem(obj);
				m_combo.setSelectedItem(obj);
			}
		}
	//	actionCombo(getValue());
		m_settingFocus = false;
		m_haveFocus = false;    //  can gain focus again
	}	//	focusLost

	/**
	 *  Set ToolTip
	 *  @param text tool tip text
	 */
	public void setToolTipText(String text)
	{
		super.setToolTipText(text);
		m_button.setToolTipText(text);
		m_text.setToolTipText(text);
		m_combo.setToolTipText(text);
	}   //  setToolTipText

}	//	VLookup

/*****************************************************************************/

/**
 *	Mouse Listener for Popup Menu
 */
final class VLookup_mouseAdapter extends java.awt.event.MouseAdapter
{
	/**
	 *	Constructor
	 *  @param adaptee adaptee
	 */
	VLookup_mouseAdapter(VLookup adaptee)
	{
		this.adaptee = adaptee;
	}	//	VLookup_mouseAdapter

	private VLookup adaptee;

	/**
	 *	Mouse Listener
	 *  @param e MouseEvent
	 */
	public void mouseClicked(MouseEvent e)
	{
	//	System.out.println("mouseClicked " + e.getID() + " " + e.getSource().getClass().toString());
		//	popup menu
		if (SwingUtilities.isRightMouseButton(e))
			adaptee.popupMenu.show((Component)e.getSource(), e.getX(), e.getY());
	}	//	mouse Clicked

}	//	VLookup_mouseAdapter
