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
package org.compiere.apps;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.sql.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import java.math.*;

import org.compiere.Compiere;
import org.compiere.grid.tree.*;
import org.compiere.model.*;
import org.compiere.util.*;
import org.compiere.apps.wf.*;
import org.compiere.plaf.*;
import org.compiere.swing.*;

/**
 *	Application Menu Controller
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: AMenu.java,v 1.76 2004/05/09 18:54:17 jjanke Exp $
 */
public final class AMenu extends JFrame
	implements ActionListener, PropertyChangeListener, ChangeListener
{
	/**
	 *	Application Menu
	 */
	public AMenu ()
	{
		super();
		Splash splash = Splash.getSplash();
		m_WindowNo = Env.createWindowNo(this);

		//	Login
		initSystem (splash);        //	login
		splash.setText(Msg.getMsg(m_ctx, "Loading"));
		MSession.getDefault(Env.getCtx());				//	Start Session

		//	Preparation
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		wfActivity = new WFActivity(this); 
		wfPanel = new WFPanel(this);
		treePanel = new VTreePanel (m_WindowNo, true, false);	//	!editable & hasBar

		try
		{
			jbInit();
			createMenu();
		}
		catch(Exception ex)
		{
			Log.error("AMenu", ex);
		}

		//	initialize & load tree
		int AD_Role_ID = Env.getAD_Role_ID(Env.getCtx());
		int AD_Tree_ID = DB.getSQLValue(
			"SELECT COALESCE(r.AD_Tree_Menu_ID, ci.AD_Tree_Menu_ID)" 
			+ "FROM AD_ClientInfo ci" 
			+ " INNER JOIN AD_Role r ON (ci.AD_Client_ID=r.AD_Client_ID) "
			+ "WHERE AD_Role_ID=?", AD_Role_ID);
		if (AD_Tree_ID <= 0)
			AD_Tree_ID = 10;	//	Menu
		treePanel.initTree(AD_Tree_ID);

		//	Translate
		Env.setContext(m_ctx, m_WindowNo, "WindowName", Msg.getMsg(m_ctx, "Menu"));
		setTitle(Env.getHeader(m_ctx, m_WindowNo));

		progressBar.setString(Msg.getMsg(m_ctx, "SelectProgram"));

		//  Finish UI
		this.setLocation(0, 0);
		this.pack();
		this.setVisible(true);
		this.setState(Frame.NORMAL);
		m_AD_User_ID = Env.getContextAsInt(m_ctx, "#AD_User_ID");
		updateInfo();
		//
		splash.dispose();
		splash = null;
	}	//	AMenu

	private int 		m_WindowNo;
	private Properties  m_ctx = Env.getCtx();
	private boolean		m_startingItem = false;
	private int 		m_AD_User_ID;
	//	Links
	private int			m_request_Menu_ID = 0;
	private int			m_note_Menu_ID = 0;
	private String		m_requestSQL = null;
	private DecimalFormat	m_memoryFormat = DisplayType.getNumberFormat(DisplayType.Integer);

	/**************************************************************************
	 *	Init System.
	 *  -- do not get Msg as environment not initialized yet --
	 *  <pre>
	 *	- Login - in not successful, exit
	 *  </pre>
	 *  @param splash splash window
	 */
	private void initSystem (Splash splash)
	{
		//  Image
		this.setIconImage(Compiere.getImage16());

		//  Focus Traversal
		FocusManager.getCurrentManager().setDefaultFocusTraversalPolicy(AFocusTraversalPolicy.get());
		this.setFocusTraversalPolicy(AFocusTraversalPolicy.get());

		/**
		 *	Show Login Screen - if not successful - exit
		 */
		ALogin login = new ALogin(splash);
		if (!login.initLogin())		//	no automatic login
		{
			//	Center the window
			try
			{
				AEnv.showCenterScreen(login);	//	HTML load errors
			}
			catch (Exception ex)
			{
			}
			if (!login.isConnected())
				AEnv.exit(1);
		}

		//  Check DB	(AppsServer Version checked in Login)
		boolean dbOK = DB.isDatabaseOK(m_ctx);
	//	if (!dbOK)
	//		AEnv.exit(1);
	}	//	initSystem

	//	UI
	private CPanel mainPanel = new CPanel();
	private BorderLayout mainLayout = new BorderLayout();
	private CTabbedPane centerPane = new CTabbedPane();
	private CPanel southPanel = new CPanel();
	private BorderLayout southLayout = new BorderLayout();
	private JMenuBar menuBar = new JMenuBar();
	protected JProgressBar progressBar = new JProgressBar(0,100);
	private CPanel infoPanel = new CPanel();
	private CButton bNotes = new CButton();
	private CButton bTasks = new CButton();
	private GridLayout infoLayout = new GridLayout();
	private JProgressBar memoryBar = new JProgressBar();
	//
	private VTreePanel treePanel = null;
	private WFActivity wfActivity = null;
	private WFPanel wfPanel = null;

	/**
	 *	Static Init.
	 *  <pre>
	 *  - mainPanel
	 * 		- centerPane
	 *      	- treePanel
	 * 			- wfActivity
	 * 			- wfPanel
	 *      - southPanel
	 *          - infoPanel
	 *              - bNotes
	 *              - bTask
	 *              - memoryBar
	 *          - wfPanel
	 *          - progressBar
	 *  </pre>
	 *  @throws Exception
	 */
	void jbInit() throws Exception
	{
		this.setName("Menu");
		this.setLocale(Language.getLanguage().getLocale());
		this.setJMenuBar(menuBar);
		CompiereColor.setBackground(this);
		//
		mainPanel.setLayout(mainLayout);
		mainLayout.setHgap(0);
		mainLayout.setVgap(2);
		//
		treePanel.addPropertyChangeListener(VTreePanel.NODE_SELECTION, this);
		//
		infoPanel.setLayout(infoLayout);
		infoLayout.setColumns(2);
		infoLayout.setHgap(4);
		infoLayout.setVgap(0);
		bNotes.setRequestFocusEnabled(false);
		bNotes.setToolTipText("");
		bNotes.setActionCommand("Notes");
		bNotes.addActionListener(this);
		bNotes.setIcon(Env.getImageIcon("GetMail24.gif"));
		bNotes.setMargin(new Insets(0, 0, 0, 0));
		bTasks.setRequestFocusEnabled(false);
		bTasks.setActionCommand("Tasks");
		bTasks.addActionListener(this);
		bTasks.setIcon(Env.getImageIcon("Import24.gif"));
		bTasks.setMargin(new Insets(0, 0, 0, 0));
		//
		southLayout.setHgap(0);
		southLayout.setVgap(1);
		//
		memoryBar.setStringPainted(true);
		memoryBar.setOpaque(false);
		memoryBar.setBorderPainted(false);
		memoryBar.addMouseListener(new AMenu_MouseAdapter());
		//
		progressBar.setStringPainted(true);
		progressBar.setOpaque(false);
		//
		getContentPane().add(mainPanel);
		mainPanel.add(centerPane, BorderLayout.CENTER);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		mainPanel.add(Box.createHorizontalStrut(3), BorderLayout.EAST);
		mainPanel.add(Box.createHorizontalStrut(3), BorderLayout.WEST);
		//
		centerPane.add(treePanel, Msg.getMsg(m_ctx, "Menu"));
		centerPane.add(new CScollPane(wfActivity), Msg.translate (m_ctx, "AD_WF_Activity_ID") + ": 0");
		centerPane.add(new CScollPane(wfPanel), Msg.translate (m_ctx, "AD_Workflow_ID"));
		centerPane.addChangeListener (this);
		//
		southPanel.setLayout(southLayout);
		southPanel.add(infoPanel, BorderLayout.NORTH);
		southPanel.add(progressBar, BorderLayout.SOUTH);
		//
		infoPanel.add(bNotes, null);
		infoPanel.add(bTasks, null);
		infoPanel.add(memoryBar, null);
		//
	}	//	jbInit

	/**
	 * 	Get Preferred Size
	 * 	@return preferred Size
	 */
	public Dimension getPreferredSize()
	{
		return new Dimension (350, 500);
	}	//	getPreferredSize


	/**
	 *  Create Menu
	 */
	private void createMenu()
	{
		//      File
		JMenu mFile = AEnv.getMenu("File");
		menuBar.add(mFile);
		AEnv.addMenuItem("PrintScreen", null, KeyStroke.getKeyStroke(KeyEvent.VK_PRINTSCREEN, 0), mFile, this);
		AEnv.addMenuItem("ScreenShot", null, KeyStroke.getKeyStroke(KeyEvent.VK_PRINTSCREEN, KeyEvent.SHIFT_MASK), mFile, this);
	//	AEnv.addMenuItem("Report", null, KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.ALT_MASK), mFile, this);
		mFile.addSeparator();
		AEnv.addMenuItem("Exit", null, KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.SHIFT_MASK+Event.ALT_MASK), mFile, this);

		//      View
		JMenu mView = AEnv.getMenu("View");
		menuBar.add(mView);
		AEnv.addMenuItem("InfoProduct", null, KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK), mView, this);
		AEnv.addMenuItem("InfoBPartner", null, KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.SHIFT_MASK+Event.CTRL_MASK), mView, this);
		if (MRole.getDefault().isShowAcct())
			AEnv.addMenuItem("InfoAccount", null, KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.ALT_MASK+Event.CTRL_MASK), mView, this);
		AEnv.addMenuItem("InfoSchedule", null, null, mView, this);
		mView.addSeparator();
		AEnv.addMenuItem("InfoOrder", "Info", null, mView, this);
		AEnv.addMenuItem("InfoInvoice", "Info", null, mView, this);
		AEnv.addMenuItem("InfoInOut", "Info", null, mView, this);
		AEnv.addMenuItem("InfoPayment", "Info", null, mView, this);
		AEnv.addMenuItem("InfoCashLine", "Info", null, mView, this);
		AEnv.addMenuItem("InfoAssignment", "Info", null, mView, this);
		AEnv.addMenuItem("InfoAsset", "Info", null, mView, this);

		//      Tools
		JMenu mTools = AEnv.getMenu("Tools");
		menuBar.add(mTools);
		AEnv.addMenuItem("Calculator", null, null, mTools, this);
		AEnv.addMenuItem("Calendar", null, null, mTools, this);
		AEnv.addMenuItem("Editor", null, null, mTools, this);
		AEnv.addMenuItem("Script", null, null, mTools, this);
		if (AEnv.isWorkflowProcess())
			AEnv.addMenuItem("WorkFlow", null, null, mTools, this);
		mTools.addSeparator();
		AEnv.addMenuItem("Preference", null, null, mTools, this);

		//      Help
		JMenu mHelp = AEnv.getMenu("Help");
		menuBar.add(mHelp);
		AEnv.addMenuItem("Online", null, null, mHelp, this);
		AEnv.addMenuItem("EMailSupport", null, null, mHelp, this);
		AEnv.addMenuItem("About", null, null, mHelp, this);
	}   //  createMenu

	/**
	 *	Dispose - end system
	 */
	public void dispose()
	{
		//	End Session
		MSession session = MSession.getDefault();
		session.logout();
		
		//	clean up - close windows
		super.dispose();
		AEnv.exit(0);
	}	//	dispose

	/**
	 *  Window Events - requestFocus
	 *  @param e event
	 */
	protected void processWindowEvent(WindowEvent e)
	{
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_OPENED)
		{
			treePanel.getSearchField().requestFocusInWindow();
		//	this.toFront();
		}
	}   //  processWindowEvent

	/**
	 *	Set Busy
	 *  @param value true if buzy
	 */
	protected void setBusy (boolean value)
	{
		m_startingItem = value;
		if (value)
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		else
			setCursor(Cursor.getDefaultCursor());
	//	setEnabled (!value);        //  causes flicker
	}	//	setBusy

	/**
	 * 	Selection in tree - launch Application
	 *  @param e PropertyChangeEvent
	 */
	public void propertyChange(PropertyChangeEvent e)
	{
		MTreeNode nd = (MTreeNode)e.getNewValue();
		Log.trace(Log.l1_User, "AMenu => " + nd.getNode_ID() + " " + nd.toString());

		//	ignore summary items & when loading
		if (m_startingItem || nd.isSummary())
			return;

		String sta = nd.toString();
		progressBar.setString(sta);
		int cmd = nd.getNode_ID();

		(new AMenuStartItem(cmd, true, sta, this)).start();		//	async load
		updateInfo();
	}	//	propertyChange


	/**************************************************************************
	 *	ActionListener
	 *  @param e ActionEvent
	 */
	public void actionPerformed(ActionEvent e)
	{
		//	Buttons
		if (e.getSource() == bNotes)
			gotoNotes();
		else if (e.getSource() == bTasks)
			gotoTasks();
		else if (!AEnv.actionPerformed(e.getActionCommand(), m_WindowNo, this))
			Log.error("AMenu.actionPerformed - unknown action=" + e.getActionCommand());
		updateInfo();
	}	//	actionPerformed

	/**
	 *  Get number of open Notes
	 *  @return bumber of notes
	 */
	private int getNotes()
	{
		int retValue = 0;
		String sql = "SELECT COUNT(*) FROM AD_Note "
			+ "WHERE AD_Client_ID=? AND AD_User_ID IN (0,?)"
			+ " AND Processed='N'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID"));
			pstmt.setInt(2, m_AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("AMenu.getNotes", e);
		}
		return retValue;
	}	//	getNotes

	/**
	 *  Open Note Window
	 */
	private void gotoNotes()
	{
		//	AD_Table_ID for AD_Note = 389		HARDCODED
		if (m_note_Menu_ID == 0)
			m_note_Menu_ID = DB.getSQLValue("SELECT AD_Menu_ID "
				+ "FROM AD_Menu m"
				+ " INNER JOIN AD_TABLE t ON (t.AD_Window_ID=m.AD_Window_ID) "
				+ "WHERE t.AD_Table_ID=?", 389);
		if (m_note_Menu_ID == 0)
			m_note_Menu_ID = 233;	//	fallback HARDCODED
		(new AMenuStartItem (m_note_Menu_ID, true, Msg.translate(m_ctx, "AD_Note_ID"), this)).start();		//	async load
	}   //  gotoMessage

	/**
	 *  Ger Number of open Requests
	 *  @return number of requests
	 */
	private int getRequests()
	{
		int retValue = 0;
		if (m_requestSQL == null)
			m_requestSQL = MRole.getDefault().addAccessSQL ("SELECT COUNT(*) FROM R_Request "
				+ "WHERE SalesRep_ID=? AND Processed='N'"
				+ " AND (DateNextAction IS NULL OR TRUNC(DateNextAction) <= TRUNC(SysDate))",
					"R_Request", false, true);	//	not qualified - RW
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(m_requestSQL);
			pstmt.setInt(1, m_AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("AMenu.getRequests", e);
		}
		return retValue;
	}	//	getRequests

	/**
	 *  Open Request Window
	 */
	private void gotoTasks()
	{
		//	AD_Table_ID for R_Request = 417		HARDCODED
		if (m_request_Menu_ID == 0)
			m_request_Menu_ID = DB.getSQLValue ("SELECT AD_Menu_ID "
				+ "FROM AD_Menu m"
				+ " INNER JOIN AD_TABLE t ON (t.AD_Window_ID=m.AD_Window_ID) "
				+ "WHERE t.AD_Table_ID=?", 417);
		if (m_request_Menu_ID == 0)
			m_request_Menu_ID = 237;	//	fallback HARDCODED
		(new AMenuStartItem (m_request_Menu_ID, true, Msg.translate(m_ctx, "R_Request_ID"), this)).start();		//	async load
	}   //  gotoTasks

	/**
	 *	Show Memory Info - run GC if required - Update Requests/Memos/Activities
	 */
	public void updateInfo()
	{
		double total = Runtime.getRuntime().totalMemory() / 1024;
		double free = Runtime.getRuntime().freeMemory() / 1024;
		double used = total - free;
		double percent = used * 100 / total;
		//
		memoryBar.setMaximum((int)total);
		memoryBar.setValue((int)used);
		String msg = MessageFormat.format("{0,number,integer} MB - {1,number,integer}%", 
			new Object[] {new BigDecimal(total / 1024), new BigDecimal(percent)});
		memoryBar.setString(msg);
		//
	//	msg = MessageFormat.format("Total Memory {0,number,integer} kB - Free {1,number,integer} kB", 
		msg = Msg.getMsg(m_ctx, "MemoryInfo",
			new Object[] {new BigDecimal(total), new BigDecimal(free)});
		memoryBar.setToolTipText(msg);
	//	progressBar.repaint();
		
		//
		if (percent > 50)
			System.gc();

		//	Requests
		int requests = getRequests();
		bTasks.setText(Msg.translate(m_ctx, "R_Request_ID") + ": " + requests);
		//	Memo
		int notes = getNotes();
		bNotes.setText(Msg.translate(m_ctx, "AD_Note_ID") + ": " + notes);
		//	Activities
		int activities = wfActivity.loadActivities();
		centerPane.setTitleAt(1, Msg.translate (m_ctx, "AD_WF_Activity_ID") + ": " + activities);
		//
		if (Log.isTraceLevel(6))
			Log.trace(1, "AMenu.updateInfo - " + msg
				+ ", Processors=" + Runtime.getRuntime().availableProcessors()
				+ ", Requests=" + requests + ", Notes=" + notes + ", Activities=" + activities 
			);
	}	//	updateInfo

	
	/*************************************************************************
	 * 	Start Workflow Activity
	 * 	@param AD_Workflow_ID id
	 */
	protected void startWorkFlow (int AD_Workflow_ID)
	{
		centerPane.setSelectedIndex(2);		//	switch
		wfPanel.load(AD_Workflow_ID, false);
	}	//	startWorkFlow

	
	/**
	 * 	Change Listener (tab)
	 *	@see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 *	@param e event
	 */
	public void stateChanged (ChangeEvent e)
	{
		//	show activities
		if (centerPane.getSelectedIndex() == 1)
			wfActivity.display();
	}	//	stateChanged

	
	/**************************************************************************
	 * 	Mouse Listener
	 */
	class AMenu_MouseAdapter extends MouseAdapter
	{
		/**
		 * 	Invoked when the mouse has been clicked on a component.
		 * 	@param e evant
		 */
		public void mouseClicked(MouseEvent e) 
		{
			if (e.getClickCount() > 1)
			{
				System.gc();
				updateInfo();
			}
		}
	}	//	AMenu_MouseAdapter


	/**************************************************************************
	 *	OS Start
	 *  @param args Array of String arguments (ignored)
	 */
	public static void main(String[] args)
	{
		Splash.getSplash();
		org.compiere.Compiere.startupClient ();
		AMenu menu = new AMenu();
	}	//	main

}	//	AMenu
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
package org.compiere.apps;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.math.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.FocusManager;
import javax.swing.event.*;

import org.compiere.*;
import org.compiere.apps.wf.*;
import org.compiere.db.*;
import org.compiere.grid.tree.*;
import org.compiere.model.*;
import org.compiere.plaf.*;
import org.compiere.swing.*;
import org.compiere.util.*;
import org.compiere.wf.*;

/**
 *	Application Menu Controller
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: AMenu.java,v 1.82 2004/09/09 14:15:11 jjanke Exp $
 */
public final class AMenu extends JFrame
	implements ActionListener, PropertyChangeListener, ChangeListener
{
	/**
	 *	Application Start and Menu
	 */
	public AMenu ()
	{
		super();
		Splash splash = Splash.getSplash();
		PO.setDocWorkflowMgr (DocWorkflowManager.get());
		//
		m_WindowNo = Env.createWindowNo(this);
		//	Login
		initSystem (splash);        //	login
		splash.setText(Msg.getMsg(m_ctx, "Loading"));
		MSession.getDefault(Env.getCtx());				//	Start Session

		//	Preparation
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		wfActivity = new WFActivity(this); 
		wfPanel = new WFPanel(this);
		treePanel = new VTreePanel (m_WindowNo, true, false);	//	!editable & hasBar

		try
		{
			jbInit();
			createMenu();
		}
		catch(Exception ex)
		{
			Log.error("AMenu", ex);
		}

		//	initialize & load tree
		int AD_Role_ID = Env.getAD_Role_ID(Env.getCtx());
		int AD_Tree_ID = DB.getSQLValue(
			"SELECT COALESCE(r.AD_Tree_Menu_ID, ci.AD_Tree_Menu_ID)" 
			+ "FROM AD_ClientInfo ci" 
			+ " INNER JOIN AD_Role r ON (ci.AD_Client_ID=r.AD_Client_ID) "
			+ "WHERE AD_Role_ID=?", AD_Role_ID);
		if (AD_Tree_ID <= 0)
			AD_Tree_ID = 10;	//	Menu
		treePanel.initTree(AD_Tree_ID);

		//	Translate
		Env.setContext(m_ctx, m_WindowNo, "WindowName", Msg.getMsg(m_ctx, "Menu"));
		setTitle(Env.getHeader(m_ctx, m_WindowNo));

		progressBar.setString(Msg.getMsg(m_ctx, "SelectProgram"));

		//  Finish UI
		this.setLocation(0, 0);
		this.pack();
		this.setVisible(true);
		this.setState(Frame.NORMAL);
		m_AD_User_ID = Env.getContextAsInt(m_ctx, "#AD_User_ID");
		updateInfo();
		//
		splash.dispose();
		splash = null;
	}	//	AMenu

	private int 		m_WindowNo;
	private Properties  m_ctx = Env.getCtx();
	private boolean		m_startingItem = false;
	private int 		m_AD_User_ID;
	//	Links
	private int			m_request_Menu_ID = 0;
	private int			m_note_Menu_ID = 0;
	private String		m_requestSQL = null;
	private DecimalFormat	m_memoryFormat = DisplayType.getNumberFormat(DisplayType.Integer);

	/**************************************************************************
	 *	Init System.
	 *  -- do not get Msg as environment not initialized yet --
	 *  <pre>
	 *	- Login - in not successful, exit
	 *  </pre>
	 *  @param splash splash window
	 */
	private void initSystem (Splash splash)
	{
		//  Image
		this.setIconImage(Compiere.getImage16());

		//  Focus Traversal
		FocusManager.getCurrentManager().setDefaultFocusTraversalPolicy(AFocusTraversalPolicy.get());
		this.setFocusTraversalPolicy(AFocusTraversalPolicy.get());

		/**
		 *	Show Login Screen - if not successful - exit
		 */
		ALogin login = new ALogin(splash);
		if (!login.initLogin())		//	no automatic login
		{
			//	Center the window
			try
			{
				AEnv.showCenterScreen(login);	//	HTML load errors
			}
			catch (Exception ex)
			{
			}
			if (!login.isConnected() || !login.isOKpressed())
				AEnv.exit(1);
		}

		//  Check DB	(AppsServer Version checked in Login)
		boolean dbOK = DB.isDatabaseOK(m_ctx);
	//	if (!dbOK)
	//		AEnv.exit(1);
	}	//	initSystem

	//	UI
	private CPanel mainPanel = new CPanel();
	private BorderLayout mainLayout = new BorderLayout();
	private CTabbedPane centerPane = new CTabbedPane();
	private CPanel southPanel = new CPanel();
	private BorderLayout southLayout = new BorderLayout();
	private JMenuBar menuBar = new JMenuBar();
	protected JProgressBar progressBar = new JProgressBar(0,100);
	private CPanel infoPanel = new CPanel();
	private CButton bNotes = new CButton();
	private CButton bTasks = new CButton();
	private GridLayout infoLayout = new GridLayout();
	private JProgressBar memoryBar = new JProgressBar();
	//
	private VTreePanel treePanel = null;
	private WFActivity wfActivity = null;
	private WFPanel wfPanel = null;

	/**
	 *	Static Init.
	 *  <pre>
	 *  - mainPanel
	 * 		- centerPane
	 *      	- treePanel
	 * 			- wfActivity
	 * 			- wfPanel
	 *      - southPanel
	 *          - infoPanel
	 *              - bNotes
	 *              - bTask
	 *              - memoryBar
	 *          - wfPanel
	 *          - progressBar
	 *  </pre>
	 *  @throws Exception
	 */
	void jbInit() throws Exception
	{
		this.setName("Menu");
		this.setLocale(Language.getLoginLanguage().getLocale());
		this.setJMenuBar(menuBar);
		CompiereColor.setBackground(this);
		//
		mainPanel.setLayout(mainLayout);
		mainLayout.setHgap(0);
		mainLayout.setVgap(2);
		//
		treePanel.addPropertyChangeListener(VTreePanel.NODE_SELECTION, this);
		//
		infoPanel.setLayout(infoLayout);
		infoLayout.setColumns(2);
		infoLayout.setHgap(4);
		infoLayout.setVgap(0);
		bNotes.setRequestFocusEnabled(false);
		bNotes.setToolTipText("");
		bNotes.setActionCommand("Notes");
		bNotes.addActionListener(this);
		bNotes.setIcon(Env.getImageIcon("GetMail24.gif"));
		bNotes.setMargin(new Insets(0, 0, 0, 0));
		bTasks.setRequestFocusEnabled(false);
		bTasks.setActionCommand("Tasks");
		bTasks.addActionListener(this);
		bTasks.setIcon(Env.getImageIcon("Import24.gif"));
		bTasks.setMargin(new Insets(0, 0, 0, 0));
		//
		southLayout.setHgap(0);
		southLayout.setVgap(1);
		//
		memoryBar.setStringPainted(true);
		memoryBar.setOpaque(false);
		memoryBar.setBorderPainted(false);
		memoryBar.addMouseListener(new AMenu_MouseAdapter());
		//
		progressBar.setStringPainted(true);
		progressBar.setOpaque(false);
		//
		getContentPane().add(mainPanel);
		mainPanel.add(centerPane, BorderLayout.CENTER);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		mainPanel.add(Box.createHorizontalStrut(3), BorderLayout.EAST);
		mainPanel.add(Box.createHorizontalStrut(3), BorderLayout.WEST);
		//
		centerPane.add(treePanel, Msg.getMsg(m_ctx, "Menu"));
		centerPane.add(new CScrollPane(wfActivity), Msg.translate (m_ctx, "AD_WF_Activity_ID") + ": 0");
		centerPane.add(new CScrollPane(wfPanel), Msg.translate (m_ctx, "AD_Workflow_ID"));
		centerPane.addChangeListener (this);
		//
		southPanel.setLayout(southLayout);
		southPanel.add(infoPanel, BorderLayout.NORTH);
		southPanel.add(progressBar, BorderLayout.SOUTH);
		//
		infoPanel.add(bNotes, null);
		infoPanel.add(bTasks, null);
		infoPanel.add(memoryBar, null);
		//
	}	//	jbInit

	/**
	 * 	Get Preferred Size
	 * 	@return preferred Size
	 */
	public Dimension getPreferredSize()
	{
		return new Dimension (350, 500);
	}	//	getPreferredSize


	/**
	 *  Create Menu
	 */
	private void createMenu()
	{
		//      File
		JMenu mFile = AEnv.getMenu("File");
		menuBar.add(mFile);
		AEnv.addMenuItem("PrintScreen", null, KeyStroke.getKeyStroke(KeyEvent.VK_PRINTSCREEN, 0), mFile, this);
		AEnv.addMenuItem("ScreenShot", null, KeyStroke.getKeyStroke(KeyEvent.VK_PRINTSCREEN, KeyEvent.SHIFT_MASK), mFile, this);
	//	AEnv.addMenuItem("Report", null, KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.ALT_MASK), mFile, this);
		mFile.addSeparator();
		AEnv.addMenuItem("Exit", null, KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.SHIFT_MASK+Event.ALT_MASK), mFile, this);

		//      View
		JMenu mView = AEnv.getMenu("View");
		menuBar.add(mView);
		AEnv.addMenuItem("InfoProduct", null, KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK), mView, this);
		AEnv.addMenuItem("InfoBPartner", null, KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.SHIFT_MASK+Event.CTRL_MASK), mView, this);
		if (MRole.getDefault().isShowAcct())
			AEnv.addMenuItem("InfoAccount", null, KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.ALT_MASK+Event.CTRL_MASK), mView, this);
		AEnv.addMenuItem("InfoSchedule", null, null, mView, this);
		mView.addSeparator();
		AEnv.addMenuItem("InfoOrder", "Info", null, mView, this);
		AEnv.addMenuItem("InfoInvoice", "Info", null, mView, this);
		AEnv.addMenuItem("InfoInOut", "Info", null, mView, this);
		AEnv.addMenuItem("InfoPayment", "Info", null, mView, this);
		AEnv.addMenuItem("InfoCashLine", "Info", null, mView, this);
		AEnv.addMenuItem("InfoAssignment", "Info", null, mView, this);
		AEnv.addMenuItem("InfoAsset", "Info", null, mView, this);

		//      Tools
		JMenu mTools = AEnv.getMenu("Tools");
		menuBar.add(mTools);
		AEnv.addMenuItem("Calculator", null, null, mTools, this);
		AEnv.addMenuItem("Calendar", null, null, mTools, this);
		AEnv.addMenuItem("Editor", null, null, mTools, this);
		AEnv.addMenuItem("Script", null, null, mTools, this);
		if (AEnv.isWorkflowProcess())
			AEnv.addMenuItem("WorkFlow", null, null, mTools, this);
		mTools.addSeparator();
		AEnv.addMenuItem("Preference", null, null, mTools, this);

		//      Help
		JMenu mHelp = AEnv.getMenu("Help");
		menuBar.add(mHelp);
		AEnv.addMenuItem("Online", null, null, mHelp, this);
		AEnv.addMenuItem("EMailSupport", null, null, mHelp, this);
		AEnv.addMenuItem("About", null, null, mHelp, this);
	}   //  createMenu

	/**
	 *	Dispose - end system
	 */
	public void dispose()
	{
		//	End Session
		MSession session = MSession.getDefault();
		session.logout();
		
		//	clean up - close windows
		super.dispose();
		AEnv.exit(0);
	}	//	dispose

	/**
	 *  Window Events - requestFocus
	 *  @param e event
	 */
	protected void processWindowEvent(WindowEvent e)
	{
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_OPENED)
		{
			treePanel.getSearchField().requestFocusInWindow();
		//	this.toFront();
		}
	}   //  processWindowEvent

	/**
	 *	Set Busy
	 *  @param value true if buzy
	 */
	protected void setBusy (boolean value)
	{
		m_startingItem = value;
		if (value)
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		else
			setCursor(Cursor.getDefaultCursor());
	//	setEnabled (!value);        //  causes flicker
	}	//	setBusy

	/**
	 * 	Selection in tree - launch Application
	 *  @param e PropertyChangeEvent
	 */
	public void propertyChange(PropertyChangeEvent e)
	{
		MTreeNode nd = (MTreeNode)e.getNewValue();
		Log.trace(Log.l1_User, "AMenu => " + nd.getNode_ID() + " " + nd.toString());

		//	ignore summary items & when loading
		if (m_startingItem || nd.isSummary())
			return;

		String sta = nd.toString();
		progressBar.setString(sta);
		int cmd = nd.getNode_ID();

		(new AMenuStartItem(cmd, true, sta, this)).start();		//	async load
		updateInfo();
	}	//	propertyChange


	/**************************************************************************
	 *	ActionListener
	 *  @param e ActionEvent
	 */
	public void actionPerformed(ActionEvent e)
	{
		//	Buttons
		if (e.getSource() == bNotes)
			gotoNotes();
		else if (e.getSource() == bTasks)
			gotoTasks();
		else if (!AEnv.actionPerformed(e.getActionCommand(), m_WindowNo, this))
			Log.error("AMenu.actionPerformed - unknown action=" + e.getActionCommand());
		updateInfo();
	}	//	actionPerformed

	/**
	 *  Get number of open Notes
	 *  @return bumber of notes
	 */
	private int getNotes()
	{
		int retValue = 0;
		String sql = "SELECT COUNT(*) FROM AD_Note "
			+ "WHERE AD_Client_ID=? AND AD_User_ID IN (0,?)"
			+ " AND Processed='N'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID"));
			pstmt.setInt(2, m_AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("AMenu.getNotes", e);
		}
		return retValue;
	}	//	getNotes

	/**
	 *  Open Note Window
	 */
	private void gotoNotes()
	{
		//	AD_Table_ID for AD_Note = 389		HARDCODED
		if (m_note_Menu_ID == 0)
			m_note_Menu_ID = DB.getSQLValue("SELECT AD_Menu_ID "
				+ "FROM AD_Menu m"
				+ " INNER JOIN AD_TABLE t ON (t.AD_Window_ID=m.AD_Window_ID) "
				+ "WHERE t.AD_Table_ID=?", 389);
		if (m_note_Menu_ID == 0)
			m_note_Menu_ID = 233;	//	fallback HARDCODED
		(new AMenuStartItem (m_note_Menu_ID, true, Msg.translate(m_ctx, "AD_Note_ID"), this)).start();		//	async load
	}   //  gotoMessage

	/**
	 *  Ger Number of open Requests
	 *  @return number of requests
	 */
	private int getRequests()
	{
		int retValue = 0;
		if (m_requestSQL == null)
			m_requestSQL = MRole.getDefault().addAccessSQL ("SELECT COUNT(*) FROM R_Request "
				+ "WHERE SalesRep_ID=? AND Processed='N'"
				+ " AND (DateNextAction IS NULL OR TRUNC(DateNextAction) <= TRUNC(SysDate))",
					"R_Request", false, true);	//	not qualified - RW
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(m_requestSQL);
			pstmt.setInt(1, m_AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("AMenu.getRequests", e);
		}
		return retValue;
	}	//	getRequests

	/**
	 *  Open Request Window
	 */
	private void gotoTasks()
	{
		//	AD_Table_ID for R_Request = 417		HARDCODED
		if (m_request_Menu_ID == 0)
			m_request_Menu_ID = DB.getSQLValue ("SELECT AD_Menu_ID "
				+ "FROM AD_Menu m"
				+ " INNER JOIN AD_TABLE t ON (t.AD_Window_ID=m.AD_Window_ID) "
				+ "WHERE t.AD_Table_ID=?", 417);
		if (m_request_Menu_ID == 0)
			m_request_Menu_ID = 237;	//	fallback HARDCODED
		(new AMenuStartItem (m_request_Menu_ID, true, Msg.translate(m_ctx, "R_Request_ID"), this)).start();		//	async load
	}   //  gotoTasks

	/**
	 *	Show Memory Info - run GC if required - Update Requests/Memos/Activities
	 */
	public void updateInfo()
	{
		double total = Runtime.getRuntime().totalMemory() / 1024;
		double free = Runtime.getRuntime().freeMemory() / 1024;
		double used = total - free;
		double percent = used * 100 / total;
		//
		memoryBar.setMaximum((int)total);
		memoryBar.setValue((int)used);
		String msg = MessageFormat.format("{0,number,integer} MB - {1,number,integer}%", 
			new Object[] {new BigDecimal(total / 1024), new BigDecimal(percent)});
		memoryBar.setString(msg);
		//
	//	msg = MessageFormat.format("Total Memory {0,number,integer} kB - Free {1,number,integer} kB", 
		msg = Msg.getMsg(m_ctx, "MemoryInfo",
			new Object[] {new BigDecimal(total), new BigDecimal(free)});
		memoryBar.setToolTipText(msg);
	//	progressBar.repaint();
		
		//
		if (percent > 50)
			System.gc();

		//	Requests
		int requests = getRequests();
		bTasks.setText(Msg.translate(m_ctx, "R_Request_ID") + ": " + requests);
		//	Memo
		int notes = getNotes();
		bNotes.setText(Msg.translate(m_ctx, "AD_Note_ID") + ": " + notes);
		//	Activities
		int activities = wfActivity.loadActivities();
		centerPane.setTitleAt(1, Msg.translate (m_ctx, "AD_WF_Activity_ID") + ": " + activities);
		//
		if (Log.isTraceLevel(Log.l2_Sub))
			Log.trace(1, "AMenu.updateInfo - " + msg
				+ ", Processors=" + Runtime.getRuntime().availableProcessors()
				+ ", Requests=" + requests + ", Notes=" + notes + ", Activities=" + activities 
				+ "," + CConnection.get().getStatus()
			);
	}	//	updateInfo

	
	/*************************************************************************
	 * 	Start Workflow Activity
	 * 	@param AD_Workflow_ID id
	 */
	protected void startWorkFlow (int AD_Workflow_ID)
	{
		centerPane.setSelectedIndex(2);		//	switch
		wfPanel.load(AD_Workflow_ID, false);
	}	//	startWorkFlow

	
	/**
	 * 	Change Listener (tab)
	 *	@see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 *	@param e event
	 */
	public void stateChanged (ChangeEvent e)
	{
		//	show activities
		if (centerPane.getSelectedIndex() == 1)
			wfActivity.display();
	}	//	stateChanged

	
	/**************************************************************************
	 * 	Mouse Listener
	 */
	class AMenu_MouseAdapter extends MouseAdapter
	{
		/**
		 * 	Invoked when the mouse has been clicked on a component.
		 * 	@param e evant
		 */
		public void mouseClicked(MouseEvent e) 
		{
			if (e.getClickCount() > 1)
			{
				System.gc();
				updateInfo();
			}
		}
	}	//	AMenu_MouseAdapter


	/**************************************************************************
	 *	OS Start
	 *  @param args Array of String arguments (ignored)
	 */
	public static void main(String[] args)
	{
		Splash splash = Splash.getSplash();
		Compiere.startupClient();	//	needs to be here for UI
		AMenu menu = new AMenu();
	}	//	main

}	//	AMenu
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
package org.compiere.apps;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.sql.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import java.math.*;

import org.compiere.Compiere;
import org.compiere.grid.tree.*;
import org.compiere.model.*;
import org.compiere.util.*;
import org.compiere.apps.wf.*;
import org.compiere.plaf.*;
import org.compiere.swing.*;

/**
 *	Application Menu Controller
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: AMenu.java,v 1.76 2004/05/09 18:54:17 jjanke Exp $
 */
public final class AMenu extends JFrame
	implements ActionListener, PropertyChangeListener, ChangeListener
{
	/**
	 *	Application Menu
	 */
	public AMenu ()
	{
		super();
		Splash splash = Splash.getSplash();
		m_WindowNo = Env.createWindowNo(this);

		//	Login
		initSystem (splash);        //	login
		splash.setText(Msg.getMsg(m_ctx, "Loading"));
		MSession.getDefault(Env.getCtx());				//	Start Session

		//	Preparation
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		wfActivity = new WFActivity(this); 
		wfPanel = new WFPanel(this);
		treePanel = new VTreePanel (m_WindowNo, true, false);	//	!editable & hasBar

		try
		{
			jbInit();
			createMenu();
		}
		catch(Exception ex)
		{
			Log.error("AMenu", ex);
		}

		//	initialize & load tree
		int AD_Role_ID = Env.getAD_Role_ID(Env.getCtx());
		int AD_Tree_ID = DB.getSQLValue(
			"SELECT COALESCE(r.AD_Tree_Menu_ID, ci.AD_Tree_Menu_ID)" 
			+ "FROM AD_ClientInfo ci" 
			+ " INNER JOIN AD_Role r ON (ci.AD_Client_ID=r.AD_Client_ID) "
			+ "WHERE AD_Role_ID=?", AD_Role_ID);
		if (AD_Tree_ID <= 0)
			AD_Tree_ID = 10;	//	Menu
		treePanel.initTree(AD_Tree_ID);

		//	Translate
		Env.setContext(m_ctx, m_WindowNo, "WindowName", Msg.getMsg(m_ctx, "Menu"));
		setTitle(Env.getHeader(m_ctx, m_WindowNo));

		progressBar.setString(Msg.getMsg(m_ctx, "SelectProgram"));

		//  Finish UI
		this.setLocation(0, 0);
		this.pack();
		this.setVisible(true);
		this.setState(Frame.NORMAL);
		m_AD_User_ID = Env.getContextAsInt(m_ctx, "#AD_User_ID");
		updateInfo();
		//
		splash.dispose();
		splash = null;
	}	//	AMenu

	private int 		m_WindowNo;
	private Properties  m_ctx = Env.getCtx();
	private boolean		m_startingItem = false;
	private int 		m_AD_User_ID;
	//	Links
	private int			m_request_Menu_ID = 0;
	private int			m_note_Menu_ID = 0;
	private String		m_requestSQL = null;
	private DecimalFormat	m_memoryFormat = DisplayType.getNumberFormat(DisplayType.Integer);

	/**************************************************************************
	 *	Init System.
	 *  -- do not get Msg as environment not initialized yet --
	 *  <pre>
	 *	- Login - in not successful, exit
	 *  </pre>
	 *  @param splash splash window
	 */
	private void initSystem (Splash splash)
	{
		//  Image
		this.setIconImage(Compiere.getImage16());

		//  Focus Traversal
		FocusManager.getCurrentManager().setDefaultFocusTraversalPolicy(AFocusTraversalPolicy.get());
		this.setFocusTraversalPolicy(AFocusTraversalPolicy.get());

		/**
		 *	Show Login Screen - if not successful - exit
		 */
		ALogin login = new ALogin(splash);
		if (!login.initLogin())		//	no automatic login
		{
			//	Center the window
			try
			{
				AEnv.showCenterScreen(login);	//	HTML load errors
			}
			catch (Exception ex)
			{
			}
			if (!login.isConnected())
				AEnv.exit(1);
		}

		//  Check DB	(AppsServer Version checked in Login)
		boolean dbOK = DB.isDatabaseOK(m_ctx);
	//	if (!dbOK)
	//		AEnv.exit(1);
	}	//	initSystem

	//	UI
	private CPanel mainPanel = new CPanel();
	private BorderLayout mainLayout = new BorderLayout();
	private CTabbedPane centerPane = new CTabbedPane();
	private CPanel southPanel = new CPanel();
	private BorderLayout southLayout = new BorderLayout();
	private JMenuBar menuBar = new JMenuBar();
	protected JProgressBar progressBar = new JProgressBar(0,100);
	private CPanel infoPanel = new CPanel();
	private CButton bNotes = new CButton();
	private CButton bTasks = new CButton();
	private GridLayout infoLayout = new GridLayout();
	private JProgressBar memoryBar = new JProgressBar();
	//
	private VTreePanel treePanel = null;
	private WFActivity wfActivity = null;
	private WFPanel wfPanel = null;

	/**
	 *	Static Init.
	 *  <pre>
	 *  - mainPanel
	 * 		- centerPane
	 *      	- treePanel
	 * 			- wfActivity
	 * 			- wfPanel
	 *      - southPanel
	 *          - infoPanel
	 *              - bNotes
	 *              - bTask
	 *              - memoryBar
	 *          - wfPanel
	 *          - progressBar
	 *  </pre>
	 *  @throws Exception
	 */
	void jbInit() throws Exception
	{
		this.setName("Menu");
		this.setLocale(Language.getLanguage().getLocale());
		this.setJMenuBar(menuBar);
		CompiereColor.setBackground(this);
		//
		mainPanel.setLayout(mainLayout);
		mainLayout.setHgap(0);
		mainLayout.setVgap(2);
		//
		treePanel.addPropertyChangeListener(VTreePanel.NODE_SELECTION, this);
		//
		infoPanel.setLayout(infoLayout);
		infoLayout.setColumns(2);
		infoLayout.setHgap(4);
		infoLayout.setVgap(0);
		bNotes.setRequestFocusEnabled(false);
		bNotes.setToolTipText("");
		bNotes.setActionCommand("Notes");
		bNotes.addActionListener(this);
		bNotes.setIcon(Env.getImageIcon("GetMail24.gif"));
		bNotes.setMargin(new Insets(0, 0, 0, 0));
		bTasks.setRequestFocusEnabled(false);
		bTasks.setActionCommand("Tasks");
		bTasks.addActionListener(this);
		bTasks.setIcon(Env.getImageIcon("Import24.gif"));
		bTasks.setMargin(new Insets(0, 0, 0, 0));
		//
		southLayout.setHgap(0);
		southLayout.setVgap(1);
		//
		memoryBar.setStringPainted(true);
		memoryBar.setOpaque(false);
		memoryBar.setBorderPainted(false);
		memoryBar.addMouseListener(new AMenu_MouseAdapter());
		//
		progressBar.setStringPainted(true);
		progressBar.setOpaque(false);
		//
		getContentPane().add(mainPanel);
		mainPanel.add(centerPane, BorderLayout.CENTER);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		mainPanel.add(Box.createHorizontalStrut(3), BorderLayout.EAST);
		mainPanel.add(Box.createHorizontalStrut(3), BorderLayout.WEST);
		//
		centerPane.add(treePanel, Msg.getMsg(m_ctx, "Menu"));
		centerPane.add(new CScollPane(wfActivity), Msg.translate (m_ctx, "AD_WF_Activity_ID") + ": 0");
		centerPane.add(new CScollPane(wfPanel), Msg.translate (m_ctx, "AD_Workflow_ID"));
		centerPane.addChangeListener (this);
		//
		southPanel.setLayout(southLayout);
		southPanel.add(infoPanel, BorderLayout.NORTH);
		southPanel.add(progressBar, BorderLayout.SOUTH);
		//
		infoPanel.add(bNotes, null);
		infoPanel.add(bTasks, null);
		infoPanel.add(memoryBar, null);
		//
	}	//	jbInit

	/**
	 * 	Get Preferred Size
	 * 	@return preferred Size
	 */
	public Dimension getPreferredSize()
	{
		return new Dimension (350, 500);
	}	//	getPreferredSize


	/**
	 *  Create Menu
	 */
	private void createMenu()
	{
		//      File
		JMenu mFile = AEnv.getMenu("File");
		menuBar.add(mFile);
		AEnv.addMenuItem("PrintScreen", null, KeyStroke.getKeyStroke(KeyEvent.VK_PRINTSCREEN, 0), mFile, this);
		AEnv.addMenuItem("ScreenShot", null, KeyStroke.getKeyStroke(KeyEvent.VK_PRINTSCREEN, KeyEvent.SHIFT_MASK), mFile, this);
	//	AEnv.addMenuItem("Report", null, KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.ALT_MASK), mFile, this);
		mFile.addSeparator();
		AEnv.addMenuItem("Exit", null, KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.SHIFT_MASK+Event.ALT_MASK), mFile, this);

		//      View
		JMenu mView = AEnv.getMenu("View");
		menuBar.add(mView);
		AEnv.addMenuItem("InfoProduct", null, KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK), mView, this);
		AEnv.addMenuItem("InfoBPartner", null, KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.SHIFT_MASK+Event.CTRL_MASK), mView, this);
		if (MRole.getDefault().isShowAcct())
			AEnv.addMenuItem("InfoAccount", null, KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.ALT_MASK+Event.CTRL_MASK), mView, this);
		AEnv.addMenuItem("InfoSchedule", null, null, mView, this);
		mView.addSeparator();
		AEnv.addMenuItem("InfoOrder", "Info", null, mView, this);
		AEnv.addMenuItem("InfoInvoice", "Info", null, mView, this);
		AEnv.addMenuItem("InfoInOut", "Info", null, mView, this);
		AEnv.addMenuItem("InfoPayment", "Info", null, mView, this);
		AEnv.addMenuItem("InfoCashLine", "Info", null, mView, this);
		AEnv.addMenuItem("InfoAssignment", "Info", null, mView, this);
		AEnv.addMenuItem("InfoAsset", "Info", null, mView, this);

		//      Tools
		JMenu mTools = AEnv.getMenu("Tools");
		menuBar.add(mTools);
		AEnv.addMenuItem("Calculator", null, null, mTools, this);
		AEnv.addMenuItem("Calendar", null, null, mTools, this);
		AEnv.addMenuItem("Editor", null, null, mTools, this);
		AEnv.addMenuItem("Script", null, null, mTools, this);
		if (AEnv.isWorkflowProcess())
			AEnv.addMenuItem("WorkFlow", null, null, mTools, this);
		mTools.addSeparator();
		AEnv.addMenuItem("Preference", null, null, mTools, this);

		//      Help
		JMenu mHelp = AEnv.getMenu("Help");
		menuBar.add(mHelp);
		AEnv.addMenuItem("Online", null, null, mHelp, this);
		AEnv.addMenuItem("EMailSupport", null, null, mHelp, this);
		AEnv.addMenuItem("About", null, null, mHelp, this);
	}   //  createMenu

	/**
	 *	Dispose - end system
	 */
	public void dispose()
	{
		//	End Session
		MSession session = MSession.getDefault();
		session.logout();
		
		//	clean up - close windows
		super.dispose();
		AEnv.exit(0);
	}	//	dispose

	/**
	 *  Window Events - requestFocus
	 *  @param e event
	 */
	protected void processWindowEvent(WindowEvent e)
	{
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_OPENED)
		{
			treePanel.getSearchField().requestFocusInWindow();
		//	this.toFront();
		}
	}   //  processWindowEvent

	/**
	 *	Set Busy
	 *  @param value true if buzy
	 */
	protected void setBusy (boolean value)
	{
		m_startingItem = value;
		if (value)
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		else
			setCursor(Cursor.getDefaultCursor());
	//	setEnabled (!value);        //  causes flicker
	}	//	setBusy

	/**
	 * 	Selection in tree - launch Application
	 *  @param e PropertyChangeEvent
	 */
	public void propertyChange(PropertyChangeEvent e)
	{
		MTreeNode nd = (MTreeNode)e.getNewValue();
		Log.trace(Log.l1_User, "AMenu => " + nd.getNode_ID() + " " + nd.toString());

		//	ignore summary items & when loading
		if (m_startingItem || nd.isSummary())
			return;

		String sta = nd.toString();
		progressBar.setString(sta);
		int cmd = nd.getNode_ID();

		(new AMenuStartItem(cmd, true, sta, this)).start();		//	async load
		updateInfo();
	}	//	propertyChange


	/**************************************************************************
	 *	ActionListener
	 *  @param e ActionEvent
	 */
	public void actionPerformed(ActionEvent e)
	{
		//	Buttons
		if (e.getSource() == bNotes)
			gotoNotes();
		else if (e.getSource() == bTasks)
			gotoTasks();
		else if (!AEnv.actionPerformed(e.getActionCommand(), m_WindowNo, this))
			Log.error("AMenu.actionPerformed - unknown action=" + e.getActionCommand());
		updateInfo();
	}	//	actionPerformed

	/**
	 *  Get number of open Notes
	 *  @return bumber of notes
	 */
	private int getNotes()
	{
		int retValue = 0;
		String sql = "SELECT COUNT(*) FROM AD_Note "
			+ "WHERE AD_Client_ID=? AND AD_User_ID IN (0,?)"
			+ " AND Processed='N'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID"));
			pstmt.setInt(2, m_AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("AMenu.getNotes", e);
		}
		return retValue;
	}	//	getNotes

	/**
	 *  Open Note Window
	 */
	private void gotoNotes()
	{
		//	AD_Table_ID for AD_Note = 389		HARDCODED
		if (m_note_Menu_ID == 0)
			m_note_Menu_ID = DB.getSQLValue("SELECT AD_Menu_ID "
				+ "FROM AD_Menu m"
				+ " INNER JOIN AD_TABLE t ON (t.AD_Window_ID=m.AD_Window_ID) "
				+ "WHERE t.AD_Table_ID=?", 389);
		if (m_note_Menu_ID == 0)
			m_note_Menu_ID = 233;	//	fallback HARDCODED
		(new AMenuStartItem (m_note_Menu_ID, true, Msg.translate(m_ctx, "AD_Note_ID"), this)).start();		//	async load
	}   //  gotoMessage

	/**
	 *  Ger Number of open Requests
	 *  @return number of requests
	 */
	private int getRequests()
	{
		int retValue = 0;
		if (m_requestSQL == null)
			m_requestSQL = MRole.getDefault().addAccessSQL ("SELECT COUNT(*) FROM R_Request "
				+ "WHERE SalesRep_ID=? AND Processed='N'"
				+ " AND (DateNextAction IS NULL OR TRUNC(DateNextAction) <= TRUNC(SysDate))",
					"R_Request", false, true);	//	not qualified - RW
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(m_requestSQL);
			pstmt.setInt(1, m_AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("AMenu.getRequests", e);
		}
		return retValue;
	}	//	getRequests

	/**
	 *  Open Request Window
	 */
	private void gotoTasks()
	{
		//	AD_Table_ID for R_Request = 417		HARDCODED
		if (m_request_Menu_ID == 0)
			m_request_Menu_ID = DB.getSQLValue ("SELECT AD_Menu_ID "
				+ "FROM AD_Menu m"
				+ " INNER JOIN AD_TABLE t ON (t.AD_Window_ID=m.AD_Window_ID) "
				+ "WHERE t.AD_Table_ID=?", 417);
		if (m_request_Menu_ID == 0)
			m_request_Menu_ID = 237;	//	fallback HARDCODED
		(new AMenuStartItem (m_request_Menu_ID, true, Msg.translate(m_ctx, "R_Request_ID"), this)).start();		//	async load
	}   //  gotoTasks

	/**
	 *	Show Memory Info - run GC if required - Update Requests/Memos/Activities
	 */
	public void updateInfo()
	{
		double total = Runtime.getRuntime().totalMemory() / 1024;
		double free = Runtime.getRuntime().freeMemory() / 1024;
		double used = total - free;
		double percent = used * 100 / total;
		//
		memoryBar.setMaximum((int)total);
		memoryBar.setValue((int)used);
		String msg = MessageFormat.format("{0,number,integer} MB - {1,number,integer}%", 
			new Object[] {new BigDecimal(total / 1024), new BigDecimal(percent)});
		memoryBar.setString(msg);
		//
	//	msg = MessageFormat.format("Total Memory {0,number,integer} kB - Free {1,number,integer} kB", 
		msg = Msg.getMsg(m_ctx, "MemoryInfo",
			new Object[] {new BigDecimal(total), new BigDecimal(free)});
		memoryBar.setToolTipText(msg);
	//	progressBar.repaint();
		
		//
		if (percent > 50)
			System.gc();

		//	Requests
		int requests = getRequests();
		bTasks.setText(Msg.translate(m_ctx, "R_Request_ID") + ": " + requests);
		//	Memo
		int notes = getNotes();
		bNotes.setText(Msg.translate(m_ctx, "AD_Note_ID") + ": " + notes);
		//	Activities
		int activities = wfActivity.loadActivities();
		centerPane.setTitleAt(1, Msg.translate (m_ctx, "AD_WF_Activity_ID") + ": " + activities);
		//
		if (Log.isTraceLevel(6))
			Log.trace(1, "AMenu.updateInfo - " + msg
				+ ", Processors=" + Runtime.getRuntime().availableProcessors()
				+ ", Requests=" + requests + ", Notes=" + notes + ", Activities=" + activities 
			);
	}	//	updateInfo

	
	/*************************************************************************
	 * 	Start Workflow Activity
	 * 	@param AD_Workflow_ID id
	 */
	protected void startWorkFlow (int AD_Workflow_ID)
	{
		centerPane.setSelectedIndex(2);		//	switch
		wfPanel.load(AD_Workflow_ID, false);
	}	//	startWorkFlow

	
	/**
	 * 	Change Listener (tab)
	 *	@see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 *	@param e event
	 */
	public void stateChanged (ChangeEvent e)
	{
		//	show activities
		if (centerPane.getSelectedIndex() == 1)
			wfActivity.display();
	}	//	stateChanged

	
	/**************************************************************************
	 * 	Mouse Listener
	 */
	class AMenu_MouseAdapter extends MouseAdapter
	{
		/**
		 * 	Invoked when the mouse has been clicked on a component.
		 * 	@param e evant
		 */
		public void mouseClicked(MouseEvent e) 
		{
			if (e.getClickCount() > 1)
			{
				System.gc();
				updateInfo();
			}
		}
	}	//	AMenu_MouseAdapter


	/**************************************************************************
	 *	OS Start
	 *  @param args Array of String arguments (ignored)
	 */
	public static void main(String[] args)
	{
		Splash.getSplash();
		org.compiere.Compiere.startupClient ();
		AMenu menu = new AMenu();
	}	//	main

}	//	AMenu
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
package org.compiere.apps;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.math.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.FocusManager;
import javax.swing.event.*;

import org.compiere.*;
import org.compiere.apps.wf.*;
import org.compiere.db.*;
import org.compiere.grid.tree.*;
import org.compiere.model.*;
import org.compiere.plaf.*;
import org.compiere.swing.*;
import org.compiere.util.*;
import org.compiere.wf.*;

/**
 *	Application Menu Controller
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: AMenu.java,v 1.82 2004/09/09 14:15:11 jjanke Exp $
 */
public final class AMenu extends JFrame
	implements ActionListener, PropertyChangeListener, ChangeListener
{
	/**
	 *	Application Start and Menu
	 */
	public AMenu ()
	{
		super();
		Splash splash = Splash.getSplash();
		PO.setDocWorkflowMgr (DocWorkflowManager.get());
		//
		m_WindowNo = Env.createWindowNo(this);
		//	Login
		initSystem (splash);        //	login
		splash.setText(Msg.getMsg(m_ctx, "Loading"));
		MSession.getDefault(Env.getCtx());				//	Start Session

		//	Preparation
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		wfActivity = new WFActivity(this); 
		wfPanel = new WFPanel(this);
		treePanel = new VTreePanel (m_WindowNo, true, false);	//	!editable & hasBar

		try
		{
			jbInit();
			createMenu();
		}
		catch(Exception ex)
		{
			Log.error("AMenu", ex);
		}

		//	initialize & load tree
		int AD_Role_ID = Env.getAD_Role_ID(Env.getCtx());
		int AD_Tree_ID = DB.getSQLValue(
			"SELECT COALESCE(r.AD_Tree_Menu_ID, ci.AD_Tree_Menu_ID)" 
			+ "FROM AD_ClientInfo ci" 
			+ " INNER JOIN AD_Role r ON (ci.AD_Client_ID=r.AD_Client_ID) "
			+ "WHERE AD_Role_ID=?", AD_Role_ID);
		if (AD_Tree_ID <= 0)
			AD_Tree_ID = 10;	//	Menu
		treePanel.initTree(AD_Tree_ID);

		//	Translate
		Env.setContext(m_ctx, m_WindowNo, "WindowName", Msg.getMsg(m_ctx, "Menu"));
		setTitle(Env.getHeader(m_ctx, m_WindowNo));

		progressBar.setString(Msg.getMsg(m_ctx, "SelectProgram"));

		//  Finish UI
		this.setLocation(0, 0);
		this.pack();
		this.setVisible(true);
		this.setState(Frame.NORMAL);
		m_AD_User_ID = Env.getContextAsInt(m_ctx, "#AD_User_ID");
		updateInfo();
		//
		splash.dispose();
		splash = null;
	}	//	AMenu

	private int 		m_WindowNo;
	private Properties  m_ctx = Env.getCtx();
	private boolean		m_startingItem = false;
	private int 		m_AD_User_ID;
	//	Links
	private int			m_request_Menu_ID = 0;
	private int			m_note_Menu_ID = 0;
	private String		m_requestSQL = null;
	private DecimalFormat	m_memoryFormat = DisplayType.getNumberFormat(DisplayType.Integer);

	/**************************************************************************
	 *	Init System.
	 *  -- do not get Msg as environment not initialized yet --
	 *  <pre>
	 *	- Login - in not successful, exit
	 *  </pre>
	 *  @param splash splash window
	 */
	private void initSystem (Splash splash)
	{
		//  Image
		this.setIconImage(Compiere.getImage16());

		//  Focus Traversal
		FocusManager.getCurrentManager().setDefaultFocusTraversalPolicy(AFocusTraversalPolicy.get());
		this.setFocusTraversalPolicy(AFocusTraversalPolicy.get());

		/**
		 *	Show Login Screen - if not successful - exit
		 */
		ALogin login = new ALogin(splash);
		if (!login.initLogin())		//	no automatic login
		{
			//	Center the window
			try
			{
				AEnv.showCenterScreen(login);	//	HTML load errors
			}
			catch (Exception ex)
			{
			}
			if (!login.isConnected() || !login.isOKpressed())
				AEnv.exit(1);
		}

		//  Check DB	(AppsServer Version checked in Login)
		boolean dbOK = DB.isDatabaseOK(m_ctx);
	//	if (!dbOK)
	//		AEnv.exit(1);
	}	//	initSystem

	//	UI
	private CPanel mainPanel = new CPanel();
	private BorderLayout mainLayout = new BorderLayout();
	private CTabbedPane centerPane = new CTabbedPane();
	private CPanel southPanel = new CPanel();
	private BorderLayout southLayout = new BorderLayout();
	private JMenuBar menuBar = new JMenuBar();
	protected JProgressBar progressBar = new JProgressBar(0,100);
	private CPanel infoPanel = new CPanel();
	private CButton bNotes = new CButton();
	private CButton bTasks = new CButton();
	private GridLayout infoLayout = new GridLayout();
	private JProgressBar memoryBar = new JProgressBar();
	//
	private VTreePanel treePanel = null;
	private WFActivity wfActivity = null;
	private WFPanel wfPanel = null;

	/**
	 *	Static Init.
	 *  <pre>
	 *  - mainPanel
	 * 		- centerPane
	 *      	- treePanel
	 * 			- wfActivity
	 * 			- wfPanel
	 *      - southPanel
	 *          - infoPanel
	 *              - bNotes
	 *              - bTask
	 *              - memoryBar
	 *          - wfPanel
	 *          - progressBar
	 *  </pre>
	 *  @throws Exception
	 */
	void jbInit() throws Exception
	{
		this.setName("Menu");
		this.setLocale(Language.getLoginLanguage().getLocale());
		this.setJMenuBar(menuBar);
		CompiereColor.setBackground(this);
		//
		mainPanel.setLayout(mainLayout);
		mainLayout.setHgap(0);
		mainLayout.setVgap(2);
		//
		treePanel.addPropertyChangeListener(VTreePanel.NODE_SELECTION, this);
		//
		infoPanel.setLayout(infoLayout);
		infoLayout.setColumns(2);
		infoLayout.setHgap(4);
		infoLayout.setVgap(0);
		bNotes.setRequestFocusEnabled(false);
		bNotes.setToolTipText("");
		bNotes.setActionCommand("Notes");
		bNotes.addActionListener(this);
		bNotes.setIcon(Env.getImageIcon("GetMail24.gif"));
		bNotes.setMargin(new Insets(0, 0, 0, 0));
		bTasks.setRequestFocusEnabled(false);
		bTasks.setActionCommand("Tasks");
		bTasks.addActionListener(this);
		bTasks.setIcon(Env.getImageIcon("Import24.gif"));
		bTasks.setMargin(new Insets(0, 0, 0, 0));
		//
		southLayout.setHgap(0);
		southLayout.setVgap(1);
		//
		memoryBar.setStringPainted(true);
		memoryBar.setOpaque(false);
		memoryBar.setBorderPainted(false);
		memoryBar.addMouseListener(new AMenu_MouseAdapter());
		//
		progressBar.setStringPainted(true);
		progressBar.setOpaque(false);
		//
		getContentPane().add(mainPanel);
		mainPanel.add(centerPane, BorderLayout.CENTER);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		mainPanel.add(Box.createHorizontalStrut(3), BorderLayout.EAST);
		mainPanel.add(Box.createHorizontalStrut(3), BorderLayout.WEST);
		//
		centerPane.add(treePanel, Msg.getMsg(m_ctx, "Menu"));
		centerPane.add(new CScrollPane(wfActivity), Msg.translate (m_ctx, "AD_WF_Activity_ID") + ": 0");
		centerPane.add(new CScrollPane(wfPanel), Msg.translate (m_ctx, "AD_Workflow_ID"));
		centerPane.addChangeListener (this);
		//
		southPanel.setLayout(southLayout);
		southPanel.add(infoPanel, BorderLayout.NORTH);
		southPanel.add(progressBar, BorderLayout.SOUTH);
		//
		infoPanel.add(bNotes, null);
		infoPanel.add(bTasks, null);
		infoPanel.add(memoryBar, null);
		//
	}	//	jbInit

	/**
	 * 	Get Preferred Size
	 * 	@return preferred Size
	 */
	public Dimension getPreferredSize()
	{
		return new Dimension (350, 500);
	}	//	getPreferredSize


	/**
	 *  Create Menu
	 */
	private void createMenu()
	{
		//      File
		JMenu mFile = AEnv.getMenu("File");
		menuBar.add(mFile);
		AEnv.addMenuItem("PrintScreen", null, KeyStroke.getKeyStroke(KeyEvent.VK_PRINTSCREEN, 0), mFile, this);
		AEnv.addMenuItem("ScreenShot", null, KeyStroke.getKeyStroke(KeyEvent.VK_PRINTSCREEN, KeyEvent.SHIFT_MASK), mFile, this);
	//	AEnv.addMenuItem("Report", null, KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.ALT_MASK), mFile, this);
		mFile.addSeparator();
		AEnv.addMenuItem("Exit", null, KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.SHIFT_MASK+Event.ALT_MASK), mFile, this);

		//      View
		JMenu mView = AEnv.getMenu("View");
		menuBar.add(mView);
		AEnv.addMenuItem("InfoProduct", null, KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK), mView, this);
		AEnv.addMenuItem("InfoBPartner", null, KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.SHIFT_MASK+Event.CTRL_MASK), mView, this);
		if (MRole.getDefault().isShowAcct())
			AEnv.addMenuItem("InfoAccount", null, KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.ALT_MASK+Event.CTRL_MASK), mView, this);
		AEnv.addMenuItem("InfoSchedule", null, null, mView, this);
		mView.addSeparator();
		AEnv.addMenuItem("InfoOrder", "Info", null, mView, this);
		AEnv.addMenuItem("InfoInvoice", "Info", null, mView, this);
		AEnv.addMenuItem("InfoInOut", "Info", null, mView, this);
		AEnv.addMenuItem("InfoPayment", "Info", null, mView, this);
		AEnv.addMenuItem("InfoCashLine", "Info", null, mView, this);
		AEnv.addMenuItem("InfoAssignment", "Info", null, mView, this);
		AEnv.addMenuItem("InfoAsset", "Info", null, mView, this);

		//      Tools
		JMenu mTools = AEnv.getMenu("Tools");
		menuBar.add(mTools);
		AEnv.addMenuItem("Calculator", null, null, mTools, this);
		AEnv.addMenuItem("Calendar", null, null, mTools, this);
		AEnv.addMenuItem("Editor", null, null, mTools, this);
		AEnv.addMenuItem("Script", null, null, mTools, this);
		if (AEnv.isWorkflowProcess())
			AEnv.addMenuItem("WorkFlow", null, null, mTools, this);
		mTools.addSeparator();
		AEnv.addMenuItem("Preference", null, null, mTools, this);

		//      Help
		JMenu mHelp = AEnv.getMenu("Help");
		menuBar.add(mHelp);
		AEnv.addMenuItem("Online", null, null, mHelp, this);
		AEnv.addMenuItem("EMailSupport", null, null, mHelp, this);
		AEnv.addMenuItem("About", null, null, mHelp, this);
	}   //  createMenu

	/**
	 *	Dispose - end system
	 */
	public void dispose()
	{
		//	End Session
		MSession session = MSession.getDefault();
		session.logout();
		
		//	clean up - close windows
		super.dispose();
		AEnv.exit(0);
	}	//	dispose

	/**
	 *  Window Events - requestFocus
	 *  @param e event
	 */
	protected void processWindowEvent(WindowEvent e)
	{
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_OPENED)
		{
			treePanel.getSearchField().requestFocusInWindow();
		//	this.toFront();
		}
	}   //  processWindowEvent

	/**
	 *	Set Busy
	 *  @param value true if buzy
	 */
	protected void setBusy (boolean value)
	{
		m_startingItem = value;
		if (value)
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		else
			setCursor(Cursor.getDefaultCursor());
	//	setEnabled (!value);        //  causes flicker
	}	//	setBusy

	/**
	 * 	Selection in tree - launch Application
	 *  @param e PropertyChangeEvent
	 */
	public void propertyChange(PropertyChangeEvent e)
	{
		MTreeNode nd = (MTreeNode)e.getNewValue();
		Log.trace(Log.l1_User, "AMenu => " + nd.getNode_ID() + " " + nd.toString());

		//	ignore summary items & when loading
		if (m_startingItem || nd.isSummary())
			return;

		String sta = nd.toString();
		progressBar.setString(sta);
		int cmd = nd.getNode_ID();

		(new AMenuStartItem(cmd, true, sta, this)).start();		//	async load
		updateInfo();
	}	//	propertyChange


	/**************************************************************************
	 *	ActionListener
	 *  @param e ActionEvent
	 */
	public void actionPerformed(ActionEvent e)
	{
		//	Buttons
		if (e.getSource() == bNotes)
			gotoNotes();
		else if (e.getSource() == bTasks)
			gotoTasks();
		else if (!AEnv.actionPerformed(e.getActionCommand(), m_WindowNo, this))
			Log.error("AMenu.actionPerformed - unknown action=" + e.getActionCommand());
		updateInfo();
	}	//	actionPerformed

	/**
	 *  Get number of open Notes
	 *  @return bumber of notes
	 */
	private int getNotes()
	{
		int retValue = 0;
		String sql = "SELECT COUNT(*) FROM AD_Note "
			+ "WHERE AD_Client_ID=? AND AD_User_ID IN (0,?)"
			+ " AND Processed='N'";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql);
			pstmt.setInt(1, Env.getContextAsInt(Env.getCtx(), "#AD_Client_ID"));
			pstmt.setInt(2, m_AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("AMenu.getNotes", e);
		}
		return retValue;
	}	//	getNotes

	/**
	 *  Open Note Window
	 */
	private void gotoNotes()
	{
		//	AD_Table_ID for AD_Note = 389		HARDCODED
		if (m_note_Menu_ID == 0)
			m_note_Menu_ID = DB.getSQLValue("SELECT AD_Menu_ID "
				+ "FROM AD_Menu m"
				+ " INNER JOIN AD_TABLE t ON (t.AD_Window_ID=m.AD_Window_ID) "
				+ "WHERE t.AD_Table_ID=?", 389);
		if (m_note_Menu_ID == 0)
			m_note_Menu_ID = 233;	//	fallback HARDCODED
		(new AMenuStartItem (m_note_Menu_ID, true, Msg.translate(m_ctx, "AD_Note_ID"), this)).start();		//	async load
	}   //  gotoMessage

	/**
	 *  Ger Number of open Requests
	 *  @return number of requests
	 */
	private int getRequests()
	{
		int retValue = 0;
		if (m_requestSQL == null)
			m_requestSQL = MRole.getDefault().addAccessSQL ("SELECT COUNT(*) FROM R_Request "
				+ "WHERE SalesRep_ID=? AND Processed='N'"
				+ " AND (DateNextAction IS NULL OR TRUNC(DateNextAction) <= TRUNC(SysDate))",
					"R_Request", false, true);	//	not qualified - RW
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(m_requestSQL);
			pstmt.setInt(1, m_AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				retValue = rs.getInt(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			Log.error ("AMenu.getRequests", e);
		}
		return retValue;
	}	//	getRequests

	/**
	 *  Open Request Window
	 */
	private void gotoTasks()
	{
		//	AD_Table_ID for R_Request = 417		HARDCODED
		if (m_request_Menu_ID == 0)
			m_request_Menu_ID = DB.getSQLValue ("SELECT AD_Menu_ID "
				+ "FROM AD_Menu m"
				+ " INNER JOIN AD_TABLE t ON (t.AD_Window_ID=m.AD_Window_ID) "
				+ "WHERE t.AD_Table_ID=?", 417);
		if (m_request_Menu_ID == 0)
			m_request_Menu_ID = 237;	//	fallback HARDCODED
		(new AMenuStartItem (m_request_Menu_ID, true, Msg.translate(m_ctx, "R_Request_ID"), this)).start();		//	async load
	}   //  gotoTasks

	/**
	 *	Show Memory Info - run GC if required - Update Requests/Memos/Activities
	 */
	public void updateInfo()
	{
		double total = Runtime.getRuntime().totalMemory() / 1024;
		double free = Runtime.getRuntime().freeMemory() / 1024;
		double used = total - free;
		double percent = used * 100 / total;
		//
		memoryBar.setMaximum((int)total);
		memoryBar.setValue((int)used);
		String msg = MessageFormat.format("{0,number,integer} MB - {1,number,integer}%", 
			new Object[] {new BigDecimal(total / 1024), new BigDecimal(percent)});
		memoryBar.setString(msg);
		//
	//	msg = MessageFormat.format("Total Memory {0,number,integer} kB - Free {1,number,integer} kB", 
		msg = Msg.getMsg(m_ctx, "MemoryInfo",
			new Object[] {new BigDecimal(total), new BigDecimal(free)});
		memoryBar.setToolTipText(msg);
	//	progressBar.repaint();
		
		//
		if (percent > 50)
			System.gc();

		//	Requests
		int requests = getRequests();
		bTasks.setText(Msg.translate(m_ctx, "R_Request_ID") + ": " + requests);
		//	Memo
		int notes = getNotes();
		bNotes.setText(Msg.translate(m_ctx, "AD_Note_ID") + ": " + notes);
		//	Activities
		int activities = wfActivity.loadActivities();
		centerPane.setTitleAt(1, Msg.translate (m_ctx, "AD_WF_Activity_ID") + ": " + activities);
		//
		if (Log.isTraceLevel(Log.l2_Sub))
			Log.trace(1, "AMenu.updateInfo - " + msg
				+ ", Processors=" + Runtime.getRuntime().availableProcessors()
				+ ", Requests=" + requests + ", Notes=" + notes + ", Activities=" + activities 
				+ "," + CConnection.get().getStatus()
			);
	}	//	updateInfo

	
	/*************************************************************************
	 * 	Start Workflow Activity
	 * 	@param AD_Workflow_ID id
	 */
	protected void startWorkFlow (int AD_Workflow_ID)
	{
		centerPane.setSelectedIndex(2);		//	switch
		wfPanel.load(AD_Workflow_ID, false);
	}	//	startWorkFlow

	
	/**
	 * 	Change Listener (tab)
	 *	@see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 *	@param e event
	 */
	public void stateChanged (ChangeEvent e)
	{
		//	show activities
		if (centerPane.getSelectedIndex() == 1)
			wfActivity.display();
	}	//	stateChanged

	
	/**************************************************************************
	 * 	Mouse Listener
	 */
	class AMenu_MouseAdapter extends MouseAdapter
	{
		/**
		 * 	Invoked when the mouse has been clicked on a component.
		 * 	@param e evant
		 */
		public void mouseClicked(MouseEvent e) 
		{
			if (e.getClickCount() > 1)
			{
				System.gc();
				updateInfo();
			}
		}
	}	//	AMenu_MouseAdapter


	/**************************************************************************
	 *	OS Start
	 *  @param args Array of String arguments (ignored)
	 */
	public static void main(String[] args)
	{
		Splash splash = Splash.getSplash();
		Compiere.startupClient();	//	needs to be here for UI
		AMenu menu = new AMenu();
	}	//	main

}	//	AMenu
