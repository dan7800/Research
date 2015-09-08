/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2002 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.install;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;

import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;

import oracle.jdbc.*;
import org.compiere.*;
import org.compiere.apps.*;
import org.compiere.db.*;
import org.compiere.util.*;


/**
 *	Configuration Panel
 *
 * 	@author 	Jorg Janke
 * 	@author 	Marek Mosiewicz<marek.mosiewicz@jotel.com.pl> support for RMI over HTTP - now reimplemented/removed jj
 * 	@version 	$Id: ConfigurationPanel.java,v 1.6 2003/07/21 04:44:17 jjanke Exp $
 */
public class ConfigurationPanel extends JPanel implements ActionListener
{
	/**
	 * 	Constructor
	 *  @param statusBar for info
	 * 	@throws Exception
	 */
	public ConfigurationPanel (JLabel statusBar) throws Exception
	{
		LogManager.resetConfiguration();
		Logger root = Logger.getRootLogger();
	//	root.addAppender(new ConsoleAppender(new PatternLayout("%d{ABSOLUTE} %-5p [%c{1}] %m%n")));
		root.addAppender(new ConsoleAppender(new LogLayout()));
		root.setLevel(Level.ALL);
		Log.setTraceLevel(10);

		m_statusBar = statusBar;
		jbInit();
		dynInit();
	}	//	ConfigurationPanel

	public static final String	COMPIERE_ENV_FILE		= "CompiereEnv.properties";

	public static final String	COMPIERE_HOME 			= "COMPIERE_HOME";
	public static final String	JAVA_HOME 				= "JAVA_HOME";
	public static final String	COMPIERE_JAVA_OPTIONS 	= "COMPIERE_JAVA_OPTIONS";

	public static final String	COMPIERE_APPS_SERVER 	= "COMPIERE_APPS_SERVER";
	public static final String	COMPIERE_JNP_PORT 		= "COMPIERE_JNP_PORT";
	public static final String	COMPIERE_WEB_PORT 		= "COMPIERE_WEB_PORT";
	public static final String	COMPIERE_SSL_PORT 		= "COMPIERE_SSL_PORT";

	public static final String	COMPIERE_DB_SERVER 		= "COMPIERE_DB_SERVER";
	public static final String	COMPIERE_DB_PORT 		= "COMPIERE_DB_PORT";
	public static final String	COMPIERE_DB_NAME 		= "COMPIERE_DB_NAME";

	public static final String	COMPIERE_DB_USER 		= "COMPIERE_DB_USER";
	public static final String	COMPIERE_DB_PASSWORD 	= "COMPIERE_DB_PASSWORD";
	public static final String	COMPIERE_DB_SYSTEM 		= "COMPIERE_DB_SYSTEM";

	public static final String	COMPIERE_DB_URL 		= "COMPIERE_DB_URL";
	public static final String	COMPIERE_DB_TNS 		= "COMPIERE_DB_TNS";

	public static final String	COMPIERE_MAIL_SERVER 	= "COMPIERE_MAIL_SERVER";
	public static final String	COMPIERE_MAIL_USER 		= "COMPIERE_MAIL_USER";
	public static final String	COMPIERE_MAIL_PASSWORD 	= "COMPIERE_MAIL_PASSWORD";
	public static final String	COMPIERE_ADMIN_EMAIL	= "COMPIERE_ADMIN_EMAIL";

	public static final String	COMPIERE_FTP_SERVER		= "COMPIERE_FTP_SERVER";
	public static final String	COMPIERE_FTP_USER		= "COMPIERE_FTP_USER";
	public static final String	COMPIERE_FTP_PASSWORD	= "COMPIERE_FTP_PASSWORD";
	public static final String	COMPIERE_FTP_PREFIX		= "COMPIERE_FTP_PREFIX";

	private String			m_errorString;
	private File			m_javaHome;
	private File			m_compiereHome;
	private InetAddress		m_appsServer;
	private int				m_WebPort;
	private int				m_JNPPort;
	private InetAddress		m_databaseServer;
	private int				m_databasePort;
	private String			m_databaseName;
	private String			m_connectionString;
	private String			m_systemPassword;
	private String			m_databaseUser;
	private String			m_databasePassword;
	private String			m_TNSName;

	private InetAddress		m_mailServer;
	private InternetAddress	m_adminEMail;
	private String			m_mailUser;
	private String			m_mailPassword;

	private volatile boolean		m_success = false;
	private volatile boolean	 	m_testing = false;

	/** Translation				*/
	static ResourceBundle 		res = ResourceBundle.getBundle("org.compiere.install.SetupRes");

	/**	Driver					*/
	private static Driver		s_driver = null;
	/** Environment Properties	*/
	private Properties			m_properties = new Properties();
	/** Status Bar				*/
	private JLabel 				m_statusBar;

	private static ImageIcon iOpen = new ImageIcon(ConfigurationPanel.class.getResource("openFile.gif"));
	private static ImageIcon iSave = new ImageIcon(Compiere.class.getResource("images/Save16.gif"));
	private static ImageIcon iHelp = new ImageIcon(Compiere.class.getResource("images/Help16.gif"));


	//	-------------	Static UI
	private GridBagLayout gridBagLayout = new GridBagLayout();
	private JLabel lCompiereHome = new JLabel();
	private JTextField fCompiereHome = new JTextField();
	private JLabel lWebPort = new JLabel();
	private JTextField fWebPort = new JTextField();
	private JLabel lAppsServer = new JLabel();
	private JTextField fAppsServer = new JTextField();
	private JLabel lDatabaseType = new JLabel();
	private JComboBox fDatabaseType = new JComboBox();
	private JLabel lDatabaseName = new JLabel();
	private JLabel lDatabasePort = new JLabel();
	private JLabel lDatabaseUser = new JLabel();
	private JLabel lDatabasePassword = new JLabel();
	private JTextField fDatabaseName = new JTextField();
	private JTextField fDatabasePort = new JTextField();
	private JTextField fDatabaseUser = new JTextField();
	private JPasswordField fDatabasePassword = new JPasswordField();
	private JLabel lTNSName = new JLabel();
	private JComboBox fTNSName = new JComboBox();
	private JLabel lSystemPassword = new JLabel();
	private JPasswordField fSystemPassword = new JPasswordField();
	private JLabel lMailServer = new JLabel();
	private JTextField fMailServer = new JTextField();
	private JLabel lAdminEMail = new JLabel();
	private JTextField fAdminEMail = new JTextField();
	private JLabel lDatabaseServer = new JLabel();
	private JTextField fDatabaseServer = new JTextField();
	private JLabel lJavaHome = new JLabel();
	private JTextField fJavaHome = new JTextField();
	private JButton bCompiereHome = new JButton(iOpen);
	private JButton bJavaHome = new JButton(iOpen);
	private JButton bHelp = new JButton(iHelp);
	private JButton bTest = new JButton();
	private JButton bSave = new JButton(iSave);
	private JLabel lJNPPort = new JLabel();
	private JTextField fJNPPort = new JTextField();
	private JLabel lMailUser = new JLabel();
	private JLabel lMailPassword = new JLabel();
	private JTextField fMailUser = new JTextField();
	private JPasswordField fMailPassword = new JPasswordField();


	/**
	 * 	Static Layout Init
	 *  @throws Exception
	 */
	private void jbInit() throws Exception
	{
		lCompiereHome.setToolTipText(res.getString("CompiereHomeInfo"));
		lCompiereHome.setText(res.getString("CompiereHome"));
		this.setLayout(gridBagLayout);
		fCompiereHome.setText("D:\\Compiere2");
		lWebPort.setToolTipText(res.getString("WebPortInfo"));
		lWebPort.setText(res.getString("WebPort"));
		fWebPort.setText("80");
		lAppsServer.setToolTipText(res.getString("AppsServerInfo"));
		lAppsServer.setText(res.getString("AppsServer"));
		fAppsServer.setText("server.company.com");
		lDatabaseType.setToolTipText(res.getString("DatabaseTypeInfo"));
		lDatabaseType.setText(res.getString("DatabaseType"));
		lDatabaseName.setToolTipText(res.getString("DatabaseNameInfo"));
		lDatabaseName.setText(res.getString("DatabaseName"));
		lDatabasePort.setToolTipText(res.getString("DatabasePortInfo"));
		lDatabasePort.setText(res.getString("DatabasePort"));
		lDatabaseUser.setToolTipText(res.getString("DatabaseUserInfo"));
		lDatabaseUser.setText(res.getString("DatabaseUser"));
		lDatabasePassword.setToolTipText(res.getString("DatabasePasswordInfo"));
		lDatabasePassword.setText(res.getString("DatabasePassword"));
		fDatabaseName.setText("compiere");
		fDatabasePort.setText("1521");
		fDatabaseUser.setText("compiere");
		fDatabasePassword.setText("compiere");
		lTNSName.setToolTipText(res.getString("TNSNameInfo"));
		lTNSName.setText(res.getString("TNSName"));
		fTNSName.setEditable(true);
		lSystemPassword.setToolTipText(res.getString("SystemPasswordInfo"));
		lSystemPassword.setText(res.getString("SystemPassword"));
		fSystemPassword.setText("manager");
		lMailServer.setToolTipText(res.getString("MailServerInfo"));
		lMailServer.setText(res.getString("MailServer"));
		fMailServer.setText("mail.company.com");
		lAdminEMail.setToolTipText(res.getString("AdminEMailInfo"));
		lAdminEMail.setText(res.getString("AdminEMail"));
		fAdminEMail.setText("admin@company.com");
		bTest.setToolTipText(res.getString("TestInfo"));
		bTest.setText(res.getString("Test"));
		bSave.setToolTipText(res.getString("SaveInfo"));
		bSave.setText(res.getString("Save"));
		lDatabaseServer.setToolTipText(res.getString("DatabaseServerInfo"));
		lDatabaseServer.setText(res.getString("DatabaseServer"));
		lJavaHome.setToolTipText(res.getString("JavaHomeInfo"));
		lJavaHome.setText(res.getString("JavaHome"));
		fJavaHome.setText("Java141");
		lJNPPort.setToolTipText(res.getString("JNPPortInfo"));
		lJNPPort.setText(res.getString("JNPPort"));
		fJNPPort.setText("1099");
		bHelp.setToolTipText(res.getString("HelpInfo"));
		lMailUser.setToolTipText(res.getString("MailUserInfo"));
		lMailUser.setText(res.getString("MailUser"));
		lMailPassword.setToolTipText(res.getString("MailPasswordInfo"));
		lMailPassword.setText(res.getString("MailPassword"));
		fMailUser.setText("compiere");
		fMailPassword.setText("compiere");

		//
		this.add(lCompiereHome,           new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fCompiereHome,             new GridBagConstraints(1, 1, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lWebPort,           new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fWebPort,            new GridBagConstraints(1, 3, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lAppsServer,          new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fAppsServer,           new GridBagConstraints(1, 2, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseType,           new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseType,          new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabasePort,          new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseUser,           new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseName,             new GridBagConstraints(1, 6, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabasePort,            new GridBagConstraints(1, 7, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseUser,             new GridBagConstraints(1, 8, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lTNSName,           new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fTNSName,              new GridBagConstraints(3, 5, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lMailServer,          new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lSystemPassword,           new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fSystemPassword,          new GridBagConstraints(3, 7, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabasePassword,           new GridBagConstraints(3, 8, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabasePassword,          new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailServer,          new GridBagConstraints(1, 9, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(bTest,         new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(bSave,          new GridBagConstraints(3, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabaseServer,          new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseServer,         new GridBagConstraints(1, 5, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseName,       new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lJavaHome,      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJavaHome,            new GridBagConstraints(1, 0, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bCompiereHome,     new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bHelp,        new GridBagConstraints(2, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bJavaHome,     new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lJNPPort,     new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJNPPort,      new GridBagConstraints(3, 3, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lMailPassword,      new GridBagConstraints(2, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailPassword,       new GridBagConstraints(3, 10, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lAdminEMail,   new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailUser,   new GridBagConstraints(1, 10, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fAdminEMail,    new GridBagConstraints(3, 9, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lMailUser,  new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 0), 0, 0));
		//
		bCompiereHome.addActionListener(this);
		bJavaHome.addActionListener(this);
		bHelp.addActionListener(this);
		bTest.addActionListener(this);
		bSave.addActionListener(this);
	}	//	jbInit


	/**
	 * 	Dynamic Init
	 *  @throws Exception
	 */
	private void dynInit() throws Exception
	{
		//	Layout - Column Length
		fWebPort.setColumns(30);
		fJNPPort.setColumns(30);

		//	DatabaseType
		fDatabaseType.addItem("Oracle 9i2");

		//	Compiere Home
		String ch = System.getProperty(COMPIERE_HOME);
		if (ch == null || ch.length() == 0)
			ch = System.getProperty("user.dir");
		fCompiereHome.setText(ch);
		boolean envLoaded = false;
		String fileName = ch + File.separator + COMPIERE_ENV_FILE;
		File env = new File (fileName);
		System.out.println(env + " - exists=" + env.exists());
		if (env.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(env);
				m_properties.load(fis);
				fis.close();
			}
			catch (Exception e)
			{
				System.err.println(e.toString());
			}
			System.out.println("Loading ...");
			if (m_properties.size() > 5)
				envLoaded = true;
			//
			fCompiereHome.setText((String)m_properties.get(COMPIERE_HOME));
			fJavaHome.setText((String)m_properties.get(JAVA_HOME));
			fAppsServer.setText((String)m_properties.get(COMPIERE_APPS_SERVER));
			fWebPort.setText((String)m_properties.get(COMPIERE_WEB_PORT));
			fJNPPort.setText((String)m_properties.get(COMPIERE_JNP_PORT));

			fDatabaseServer.setText((String)m_properties.get(COMPIERE_DB_SERVER));
			fDatabasePort.setText((String)m_properties.get(COMPIERE_DB_PORT));
			fDatabaseName.setText((String)m_properties.get(COMPIERE_DB_NAME));

			fDatabaseUser.setText((String)m_properties.get(COMPIERE_DB_USER));
			fDatabasePassword.setText((String)m_properties.get(COMPIERE_DB_PASSWORD));
			fSystemPassword.setText((String)m_properties.get(COMPIERE_DB_SYSTEM));
			fillTNSName((String)m_properties.get(COMPIERE_DB_TNS));

			fMailServer.setText((String)m_properties.get(COMPIERE_MAIL_SERVER));
			fMailUser.setText((String)m_properties.get(COMPIERE_MAIL_USER));
			fMailPassword.setText((String)m_properties.get(COMPIERE_MAIL_PASSWORD));
			fAdminEMail.setText((String)m_properties.get(COMPIERE_ADMIN_EMAIL));
		}

		//	No environment file found - defaults
		if (!envLoaded)
		{
			System.out.println("Setting Defaults");
			//	Java Home, e.g. D:\j2sdk1.4.1\jre
			String javaHome = System.getProperty("java.home");
			if (javaHome.endsWith("jre"))
				javaHome = javaHome.substring(0, javaHome.length()-4);
			fJavaHome.setText(javaHome);
			//	AppsServer
			fAppsServer.setText(InetAddress.getLocalHost().getHostName());
			//	Database Server
			fDatabaseServer.setText(InetAddress.getLocalHost().getHostName());
			//	Mail Server
			fMailServer.setText(InetAddress.getLocalHost().getHostName());
			//  Get TNS Names
			fillTNSName("");
		}	//	!envLoaded

		//	Default FTP stuff
		if (!m_properties.containsKey(COMPIERE_FTP_SERVER))
		{
			m_properties.setProperty(COMPIERE_FTP_SERVER, "localhost");
			m_properties.setProperty(COMPIERE_FTP_USER, "anonymous");
			m_properties.setProperty(COMPIERE_FTP_PASSWORD, "user@host.com");
			m_properties.setProperty(COMPIERE_FTP_PREFIX, "my");
		}
		//	Default Java Options
		if (!m_properties.containsKey(COMPIERE_JAVA_OPTIONS))
			m_properties.setProperty(COMPIERE_JAVA_OPTIONS, "-Xms32m -Xmx128m");

		//	(String)m_properties.get(COMPIERE_DB_URL)	//	derived
	}	//	dynInit

	/**
	 * 	Fill TNS Name
	 * 	@param defaultValue defaultValue
	 */
	private void fillTNSName (String defaultValue)
	{
		//	default value to lowercase or null
		String def = defaultValue;
		if (def != null && def.trim().length() == 0)
			def = null;
		if (def != null)
		{
			def = def.toLowerCase();
			fTNSName.addItem(def);
		}

		//	Search for Oracle Info
		String path = System.getProperty("java.library.path");
		String[] entries = path.split(File.pathSeparator);
		for (int e = 0; e < entries.length; e++)
		{
			String entry = entries[e].toLowerCase();
			if (entry.indexOf("ora") != -1 && entry.endsWith("bin"))
			{
				StringBuffer sb = getTNS_File (entries[e].substring(0, entries[e].length()-4));
				String[] tnsnames = getTNS_Names (sb);
				if (tnsnames != null)
				{
					for (int i = 0; i < tnsnames.length; i++)
					{
						String tns = tnsnames[i];
						if (!tns.equals(def))
							fTNSName.addItem(tns);
					}
					break;
				}
			}
		}	//	for all path entries

		//	Set first entry
		fTNSName.addActionListener(this);
		if (fTNSName.getItemCount() > 0)
			fTNSName.setSelectedIndex(0);
	}	//	fillTNSName

	/**
	 * 	Get File tnmsnames.ora in StringBuffer
	 * 	@param oraHome ORACLE_HOME
	 * 	@return tnsnames.ora or null
	 */
	private StringBuffer getTNS_File (String oraHome)
	{
		String tnsnames = oraHome + File.separator
			+ "network" + File.separator
			+ "admin" + File.separator
			+ "tnsnames.ora";
		File tnsfile = new File (tnsnames);
		if (!tnsfile.exists())
			return null;

		System.out.println("Searching " + tnsnames);
		StringBuffer sb = new StringBuffer();
		try
		{
			FileReader fr = new FileReader (tnsfile);
			int c;
			while ((c = fr.read()) != -1)
				sb.append((char)c);
		}
		catch (IOException ex)
		{
			System.err.println("Error Reading " + tnsnames);
			ex.printStackTrace();
			return null;
		}
		if (sb.length() == 0)
			return null;
		return sb;
	}	//	getTNS_File

	/**
	 * 	Get TNS Names entries.
	 * 	Assumes standard tnsmanes.ora formatting of NetMgr
	 * 	@param tnsnames content of tnsnames.ora
	 * 	@return tns names or null
	 */
	private String[] getTNS_Names (StringBuffer tnsnames)
	{
		if (tnsnames == null)
			return null;

		ArrayList list = new ArrayList();
		Pattern pattern = Pattern.compile("$", Pattern.MULTILINE);
		String[] lines = pattern.split(tnsnames);
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i].trim();
			if (line.length() > 0
				&& Character.isLetter(line.charAt(0))	//	no # (
				&& line.indexOf("=") != -1
				&& line.indexOf("EXTPROC_") == -1
				&& line.indexOf("_HTTP") == -1)
			{
				String entry = line.substring(0, line.indexOf('=')).trim().toLowerCase();
				System.out.println("- " + entry);
				list.add(entry);
			}
		}
		//	Convert to Array
		if (list.size() == 0)
			return null;
		String[] retValue = new String[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getTNS_Names

	/*************************************************************************/

	/**
	 * 	ActionListener
	 *  @param e event
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (m_testing)
			return;
		//	TNS Name Changed
		if (e.getSource() == fTNSName)
		{
			String tns = (String)fTNSName.getSelectedItem();
			if (tns != null)
			{
				int pos = tns.indexOf('.');
				if (pos != -1)
					tns = tns.substring(0, pos);
				fDatabaseName.setText(tns);
			}
		}

		else if (e.getSource() == bJavaHome)
			setPath (fJavaHome);
		else if (e.getSource() == bCompiereHome)
			setPath (fCompiereHome);
		else if (e.getSource() == bHelp)
			new Setup_Help((Frame)SwingUtilities.getWindowAncestor(this));
		else if (e.getSource() == bTest)
			startTest();
		else if (e.getSource() == bSave)
			save();
	}	//	actionPerformed

	/**
	 * 	Set Path in Field
	 * 	@param field field to set Path
	 */
	private void setPath (JTextField field)
	{
		JFileChooser fc = new JFileChooser(field.getText());
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			field.setText(fc.getSelectedFile().getAbsolutePath());
	}	//	setPath

	/*************************************************************************/

	/**
	 * 	Start Test Async.
	 *  @return SwingWorker
	 */
	private SwingWorker startTest()
	{
		SwingWorker worker = new SwingWorker()
		{
			//	Start it
			public Object construct()
			{
				m_testing = true;
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				bTest.setEnabled(false);
				m_success = false;
				m_errorString = null;
				try
				{
					test();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					m_errorString = ex.getMessage();
				}
				//
				setCursor(Cursor.getDefaultCursor());
				if (m_errorString == null)
					m_success = true;
				bTest.setEnabled(true);
				m_testing = false;
				return new Boolean(m_success);
			}
			//	Finish it
			public void finished()
			{
				if (m_errorString != null)
				{
					System.err.println(m_errorString);
					JOptionPane.showConfirmDialog (null, m_errorString, res.getString("ServerError"),
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		worker.start();
		return worker;
	}	//	startIt

	/**
	 *	Test it
	 * 	@throws Exception
	 */
	private void test() throws Exception
	{
		System.out.println();
		System.out.println("** Test **");
		bSave.setEnabled(false);


		//	Java Home
		m_errorString = res.getString("ErrorJavaHome");
		m_javaHome = new File (fJavaHome.getText());
		if (!m_javaHome.exists())
			return;
		File tools = new File (fJavaHome.getText() + File.separator + "lib" + File.separator + "tools.jar");
		if (!tools.exists())
		{
			System.err.println("Not Found Java SDK = " + tools);
			return;
		}
		System.out.println("OK: JavaHome=" + m_javaHome.getAbsolutePath());
		m_properties.setProperty(JAVA_HOME, m_javaHome.getAbsolutePath());

		//	Java Version
		final String VERSION = "1.4.1";
		boolean versionOK = false;
		String jh = m_javaHome.getAbsolutePath();
		if (jh.indexOf(VERSION) != -1)	//	file name has version = assuming OK
			versionOK = true;
		String thisJH = System.getProperty("java.home");
		if (thisJH.indexOf(jh) != -1)	//	wre are running the version currently
		{
			String thisJV = System.getProperty("java.version");
			versionOK = thisJV.indexOf(VERSION) != -1;
			if (versionOK)
			  System.out.println("  Java Version OK = " + thisJV);
		}
		if (!versionOK)
		  System.out.println("** Please check Java Version - should be " + VERSION + "*");

		//	Compiere Home
		m_errorString = res.getString("ErrorCompiereHome");
		m_compiereHome = new File (fCompiereHome.getText());
		if (!m_compiereHome.exists())
		{
			System.err.println("Not found CompiereHome = " + m_compiereHome);
			return;
		}
		System.out.println("OK: CompiereHome = " + m_compiereHome);
		m_properties.setProperty(COMPIERE_HOME, m_compiereHome.getAbsolutePath());


		//	AppsServer
		m_errorString = res.getString("ErrorAppsServer");
		String server = fAppsServer.getText();
		if (server == null || server.length() == 0 || server.toLowerCase().indexOf("localhost") != -1)
		{
			System.err.println("Not correct AppsServer = " + m_appsServer);
			return;
		}
		m_appsServer = InetAddress.getByName(server);
		System.out.println("OK: AppsServer = " + m_appsServer);
		m_properties.setProperty(COMPIERE_APPS_SERVER, m_appsServer.getHostName());

		//	Web Port
		m_errorString = res.getString("ErrorWebPort");
		m_statusBar.setText(lWebPort.getText());
		m_WebPort = Integer.parseInt(fWebPort.getText());
		//	HTML Port Use
		if (testPort ("http", m_appsServer.getHostName(), m_WebPort, "/") || !testServerPort(m_WebPort))
		{
			System.err.println("Not correct WebPort = " + m_WebPort);
			return;
		}
		System.out.println("OK: WebPort = " + m_WebPort);
		m_properties.setProperty(COMPIERE_WEB_PORT, String.valueOf(m_WebPort));
		//	Derive SSL Port
		int sslPort = m_WebPort == 80 ? 443 : 8443;
		System.out.println("SSL WebPort = " + sslPort);
		m_properties.setProperty(COMPIERE_SSL_PORT, String.valueOf(sslPort));


		//	JNP Port
		m_errorString = res.getString("ErrorJNPPort");
		m_statusBar.setText(lJNPPort.getText());
		m_JNPPort = Integer.parseInt(fJNPPort.getText());
		//	HTML Port Use
		if (testPort (m_appsServer, m_JNPPort, false) || !testServerPort(m_JNPPort))
		{
			System.err.println("Not correct JNPPort = " + m_JNPPort);
			return;
		}
		System.out.println("OK: JNPPort = " + m_JNPPort);
		m_properties.setProperty(COMPIERE_JNP_PORT, String.valueOf(m_JNPPort));


		//	Database Server
		m_errorString = res.getString("ErrorDatabaseServer");
		m_statusBar.setText(lDatabaseServer.getText());
		server = fDatabaseServer.getText();
		if (server == null || server.length() == 0 || server.toLowerCase().indexOf("localhost") != -1)
		{
			System.err.println("Not correct Database Server = " + server);
			return;
		}
		m_databaseServer = InetAddress.getByName(server);
		System.out.println("OK: Database Server = " + m_databaseServer);
		m_properties.setProperty(COMPIERE_DB_SERVER, m_databaseServer.getHostName());


		//	Database Port
		m_errorString = res.getString("ErrorDatabasePort");
		m_databasePort = Integer.parseInt(fDatabasePort.getText());
		//	Database Port Use
		if (!testPort (m_databaseServer, m_databasePort, true))
		{
			System.err.println("Error Database Port = " + m_databasePort);
			return;
		}
		System.out.println("OK: Database Port = " + m_databasePort);
		m_properties.setProperty(COMPIERE_DB_PORT, String.valueOf(m_databasePort));


		//	JDBC Database Info
		m_errorString = res.getString("ErrorJDBC");
		m_statusBar.setText(lDatabaseName.getText());
		m_databaseName = fDatabaseName.getText();	//	SID
		m_systemPassword = new String(fSystemPassword.getPassword());
		if (!testJDBC("system", m_systemPassword))
		{
			System.err.println("Error Database Name = " + m_databaseName);
			System.err.println("Error Database SystemID = system/" + m_systemPassword);
			return;
		}
		System.out.println("OK: Database Name = " + m_databaseName);
		m_properties.setProperty(COMPIERE_DB_NAME, m_databaseName);
		System.out.println("OK: Database SystemID = system/" + m_systemPassword);
		m_properties.setProperty(COMPIERE_DB_SYSTEM, m_systemPassword);
		//	URL (derived)	jdbc:oracle:thin:@prod1:1521:prod1
		StringBuffer url = new StringBuffer("jdbc:oracle:thin:@")
			.append(m_databaseServer).append(":")
			.append(m_databasePort).append(":").append(m_databaseName);
		System.out.println("OK: Database URL = " + url.toString());
		m_properties.setProperty(COMPIERE_DB_URL, url.toString());


		//	Database User Info
		m_databaseUser = fDatabaseUser.getText();	//	UID
		m_databasePassword = new String(fDatabasePassword.getPassword());	//	PWD
		//	Ignore result as it might not be imported
		if (testJDBC(m_databaseUser, m_databasePassword))
			System.out.println("OK: Database UserID = " + m_databaseUser + "/" + m_databasePassword);
		else
			System.out.println("Not created yet: Database UserID = " + m_databaseUser + "/" + m_databasePassword);
		m_properties.setProperty(COMPIERE_DB_USER, m_databaseUser);
		m_properties.setProperty(COMPIERE_DB_PASSWORD, m_databasePassword);


		//	TNS Name Info
		m_errorString = res.getString("ErrorTNS");
		m_statusBar.setText(lTNSName.getText());
		m_TNSName = (String)fTNSName.getSelectedItem();
		if (!testTNS("system", m_systemPassword))
		{
			System.err.println("Error Database TNS Name = " + m_TNSName);
			return;
		}
		System.out.println("OK: Database TNS Name = " + m_TNSName);
		m_properties.setProperty(COMPIERE_DB_TNS, m_TNSName);


		//	Mail Server
		m_errorString = res.getString("ErrorMailServer");
		m_statusBar.setText(lMailServer.getText());
		server = fMailServer.getText();
		if (server == null || server.length() == 0)
		{
			System.err.println("Error Mail Server = " + server);
			return;
		}
		m_mailServer = InetAddress.getByName(server);
		System.out.println("OK: Mail Server = " + m_mailServer);
		m_properties.setProperty(COMPIERE_MAIL_SERVER, m_mailServer.getHostName());


		//	Mail User
		m_errorString = "ErrorMailUser";
		m_statusBar.setText(lMailUser.getText());
		m_mailUser = fMailUser.getText();
		m_mailPassword = new String(fMailPassword.getPassword());
		m_properties.setProperty(COMPIERE_MAIL_USER, m_mailUser);
		m_properties.setProperty(COMPIERE_MAIL_PASSWORD, m_mailPassword);
		System.out.println("  Mail User = " + m_mailUser + "/" + m_mailPassword);

		//	Mail Address
		m_errorString = res.getString("ErrorMail");
		m_adminEMail = new InternetAddress (fAdminEMail.getText());
		//
		if (testMail())
			System.out.println("OK: Admin EMail = " + m_adminEMail);
		else
			System.out.println("Not verified Admin EMail = " + m_adminEMail);
		m_properties.setProperty(COMPIERE_ADMIN_EMAIL, m_adminEMail.toString());

		//
		m_statusBar.setText(res.getString("Ok"));
		System.out.println("** Test OK **");
		bSave.setEnabled(true);
		m_errorString = null;
	}	//	test


	/**
	 * 	Test Apps Server Port (client perspective)
	 *  @param protocol protocol (http, ..)
	 *  @param server server name
	 *  @param port port
	 *  @param file file name
	 *  @return true if able to connect
	 */
	private boolean testPort (String protocol, String server, int port, String file)
	{
		URL url = null;
		try
		{
			url = new URL (protocol, server, port, file);
		}
		catch (MalformedURLException ex)
		{
			ex.printStackTrace();
			return false;
		}
		System.out.println("  URL=" + url);
		try
		{
			URLConnection c = url.openConnection();
			Object o = c.getContent();
			System.err.println("  URL Connection in use=" + url);	//	error
		}
		catch (Exception ex)
		{
			return false;
		}
		return true;
	}	//	testPort

	/**
	 * 	Test Server Port
	 *  @param port port
	 *  @return true if able to create
	 */
	private boolean testServerPort (int port)
	{
		try
		{
			ServerSocket ss = new ServerSocket (port);
			System.out.println("  ServerPort " + ss.getInetAddress() + ":" + ss.getLocalPort());
			ss.close();
		}
		catch (Exception ex)
		{
			System.err.println(ex);
			return false;
		}
		return true;
	}	//	testPort


	/**
	 * 	Test Port
	 *  @param host host
	 *  @param port port
	 *  @param shouldBenUsed true if it should be used
	 *  @return true if some server answered on port
	 */
	public boolean testPort (InetAddress host, int port, boolean shouldBenUsed)
	{
		Socket pingSocket = null;
		try
		{
			pingSocket = new Socket(host, port);
		}
		catch (Exception e)
		{
			if (shouldBenUsed)
				System.err.println("  Open Socket " + host + " on " + port + ": " + e.getMessage());
			return false;
		}
		if (!shouldBenUsed)
			System.err.println("  Open Socket " + host + " on " + port);
		if (pingSocket == null)
			return false;
		//	success
		try
		{
			pingSocket.close();
		}
		catch (IOException e)
		{
			System.out.println("  CloseSocket=" + e.toString());
		}
		return true;
	}	//	testPort

	/**
	 * 	Test JDBC Connection to Server
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testJDBC (String uid, String pwd)
	{
		//	jdbc:oracle:thin:@dev:1521:dev1
		m_connectionString = "jdbc:oracle:thin:@"
			+ m_databaseServer.getHostName() + ":" + m_databasePort + ":" + m_databaseName;
		System.out.println("  JDBC = " + m_connectionString);

		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(m_connectionString,
				uid, pwd);
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testJDBC

	/**
	 * 	Test TNS Connection
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testTNS (String uid, String pwd)
	{
		String connectionString = "jdbc:oracle:oci8:@" + m_TNSName;
		System.out.println("  TNS = " + connectionString);
		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(connectionString,
				uid, pwd);
		}
		catch (UnsatisfiedLinkError ule)
		{
			System.err.println("Check setup of Oracle Server / Oracle Client / LD_LIBRARY_PATH");
			System.err.println(ule.toString());
			return false;
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testTNS

	/**
	 * 	Test Mail
	 *  @return true of OK
	 */
	private boolean testMail()
	{
		boolean smtpOK = false;
		boolean imapOK = false;
		if (testPort (m_mailServer, 25, true))
		{
			System.out.println("OK: SMTP Server contacted");
			smtpOK = true;
		}
		else
			System.err.println("Error: SMTP Server NOT available");
		//
		if (testPort (m_mailServer, 110, true))
			System.out.println("OK: POP3 Server contacted");
		else
			System.err.println("Error: POP3 Server NOT available");
		if (testPort (m_mailServer, 143, true))
		{
			System.out.println("OK: IMAP4 Server contacted");
			imapOK = true;
		}
		else
			System.err.println("Error: IMAP4 Server NOT available");
		//
		if (!smtpOK)
			return false;
		//
		try
		{
			EMail em = new EMail (m_mailServer.getHostName (),
					   m_adminEMail.toString (), m_adminEMail.toString (),
					   "Compiere Server Setup Test", "Test: " + m_properties);
			em.setEMailUser (m_mailUser, m_mailPassword);
			if (EMail.SENT_OK.equals (em.send ()))
			{
				System.out.println ("OK: Send Test Email to " + m_adminEMail);
			}
			else
			{
				System.err.println ("Error: Could NOT send Email to "
					+ m_adminEMail);
			}
		}
		catch (Exception ex)
		{
			System.err.println("testMail - " + ex.getLocalizedMessage());
			return false;
		}

		//
		if (!imapOK)
			return false;

		//	Test Read Mail Access
		Properties props = new Properties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_mailServer.getHostName());
		props.put("mail.user", m_mailUser);
		props.put("mail.smtp.auth", "true");
		System.out.println("  Connecting to " + m_mailServer.getHostName());
		//
		Session session = null;
		Store store = null;
		try
		{
			EMailAuthenticator auth = new EMailAuthenticator (m_mailUser, m_mailPassword);
			session = Session.getDefaultInstance(props, auth);
			session.setDebug (Log.isTraceLevel (10));
			System.out.println("  Session=" + session);
			//	Connect to Store
			store = session.getStore("imap");
			System.out.println("  Store=" + store);
		}
		catch (NoSuchProviderException nsp)
		{
			System.err.println("Error Mail IMAP Provider - " + nsp.getMessage());
			return false;
		}
		catch (Exception e)
		{
			System.err.println("Error Mail IMAP - " + e.getMessage());
			return false;
		}
		try
		{
			store.connect(m_mailServer.getHostName(), m_mailUser, m_mailPassword);
			System.out.println("  Store - connected");
			Folder folder = store.getDefaultFolder();
			Folder inbox = folder.getFolder("INBOX");
			System.out.println("OK: Mail Connect to " + inbox.getFullName() + " #Msg=" + inbox.getMessageCount());
			//
			store.close();
		}
		catch (MessagingException mex)
		{
			System.err.println("Error Mail Connect " + mex.getMessage());
			return false;
		}
		return true;
	}	//	testMail

	/*************************************************************************/

	/**
	 * 	Save Settings
	 */
	private void save()
	{
		SwingWorker sw = startTest();
		while (sw.isAlive())
		{
			try
			{
				Thread.currentThread().sleep(2000);
			}
			catch (InterruptedException ex)
			{
				System.err.println("save-waiting: " + ex);
			}
		}
		sw.get();	//	block
		if (!m_success)
			return;

		//	Add
		m_properties.setProperty("COMPIERE_MAIN_VERSION", Compiere.MAIN_VERSION);
		m_properties.setProperty("COMPIERE_DATE_VERSION", Compiere.DATE_VERSION);
		m_properties.setProperty("COMPIERE_DB_VERSION", Compiere.DB_VERSION);



		//	Before we save, load Ini
		Ini.setClient(false);
		String fileName = m_compiereHome.getAbsolutePath() + File.separator + Ini.COMPIERE_PROPERTY_FILE;
		Ini.loadProperties(fileName);

		//	Save Environment
		fileName = m_compiereHome.getAbsolutePath() + File.separator + COMPIERE_ENV_FILE;
		try
		{
			FileOutputStream fos = new FileOutputStream(new File(fileName));
			m_properties.store(fos, COMPIERE_ENV_FILE);
			fos.flush();
			fos.close();
		}
		catch (Exception e)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + e.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (Throwable t)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + t.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		System.out.println("Properties saved to " + fileName);

		//	Sync Properties
		Ini.setCompiereHome(m_compiereHome.getAbsolutePath());
		CConnection cc = CConnection.get (Database.DB_ORACLE,
			m_databaseServer.getHostName(), m_databasePort, m_databaseName,
			m_databaseUser, m_databasePassword);
		cc.setAppsHost(m_appsServer.getHostName());
		cc.setRMIoverHTTP(false);
		Ini.setProperty(Ini.P_CONNECTION, cc.toStringLong());
		Ini.saveProperties(false);


		JOptionPane.showConfirmDialog(this, res.getString("EnvironmentSaved"),
			res.getString("CompiereServerSetup"),
			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

		((Frame)SwingUtilities.getWindowAncestor(this)).dispose();
		System.exit(0);		//	remains active when License Dialog called
	}	//	save

}	//	ConfigurationPanel
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2002 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.install;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.tools.ant.launch.Launcher;

import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;

import oracle.jdbc.*;
import org.compiere.*;
import org.compiere.apps.*;
import org.compiere.db.*;
import org.compiere.util.*;
import org.compiere.swing.*;


/**
 *	Configuration Panel
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: ConfigurationPanel.java,v 1.12 2004/03/05 05:59:55 jjanke Exp $
 */
public class ConfigurationPanel extends CPanel implements ActionListener
{
	/**
	 * 	Constructor
	 *  @param statusBar for info
	 * 	@throws Exception
	 */
	public ConfigurationPanel (JLabel statusBar) throws Exception
	{
		LogManager.resetConfiguration();
		Logger root = Logger.getRootLogger();
	//	root.addAppender(new ConsoleAppender(new PatternLayout("%d{ABSOLUTE} %-5p [%c{1}] %m%n")));
		root.addAppender(new ConsoleAppender(new LogLayout()));
		root.setLevel(Level.ALL);
		Log.setTraceLevel(10);
		//
		m_statusBar = statusBar;
		jbInit();
		dynInit();
	}	//	ConfigurationPanel

	public static final String	KEYSTORE_PASSWORD		= "myPassword";
	
	public static final String	COMPIERE_ENV_FILE		= "CompiereEnv.properties";

	public static final String	COMPIERE_HOME 			= "COMPIERE_HOME";
	public static final String	JAVA_HOME 				= "JAVA_HOME";
	public static final String	COMPIERE_JAVA_OPTIONS 	= "COMPIERE_JAVA_OPTIONS";

	public static final String	COMPIERE_APPS_SERVER 	= "COMPIERE_APPS_SERVER";
	public static final String	COMPIERE_JNP_PORT 		= "COMPIERE_JNP_PORT";
	public static final String	COMPIERE_WEB_PORT 		= "COMPIERE_WEB_PORT";
	public static final String	COMPIERE_SSL_PORT 		= "COMPIERE_SSL_PORT";
	public static final String	COMPIERE_WEB_ALIAS 		= "COMPIERE_WEB_ALIAS";
	
	public static final String	COMPIERE_KEYSTORE 		= "COMPIERE_KEYSTORE";
	public static final String	COMPIERE_KEYSTOREPASS	= "COMPIERE_KEYSTOREPASS";
	
	public static final String	COMPIERE_DB_SERVER 		= "COMPIERE_DB_SERVER";
	public static final String	COMPIERE_DB_PORT 		= "COMPIERE_DB_PORT";
	public static final String	COMPIERE_DB_NAME 		= "COMPIERE_DB_NAME";

	public static final String	COMPIERE_DB_USER 		= "COMPIERE_DB_USER";
	public static final String	COMPIERE_DB_PASSWORD 	= "COMPIERE_DB_PASSWORD";
	public static final String	COMPIERE_DB_SYSTEM 		= "COMPIERE_DB_SYSTEM";

	public static final String	COMPIERE_DB_URL 		= "COMPIERE_DB_URL";
	public static final String	COMPIERE_DB_TNS 		= "COMPIERE_DB_TNS";

	public static final String	COMPIERE_MAIL_SERVER 	= "COMPIERE_MAIL_SERVER";
	public static final String	COMPIERE_MAIL_USER 		= "COMPIERE_MAIL_USER";
	public static final String	COMPIERE_MAIL_PASSWORD 	= "COMPIERE_MAIL_PASSWORD";
	public static final String	COMPIERE_ADMIN_EMAIL	= "COMPIERE_ADMIN_EMAIL";

	public static final String	COMPIERE_FTP_SERVER		= "COMPIERE_FTP_SERVER";
	public static final String	COMPIERE_FTP_USER		= "COMPIERE_FTP_USER";
	public static final String	COMPIERE_FTP_PASSWORD	= "COMPIERE_FTP_PASSWORD";
	public static final String	COMPIERE_FTP_PREFIX		= "COMPIERE_FTP_PREFIX";

	private String			m_errorString;
	private File			m_javaHome;
	private File			m_compiereHome;
	private InetAddress		m_appsServer;
	private int				m_WebPort;
	private int				m_JNPPort;
	private InetAddress		m_databaseServer;
	private int				m_databasePort;
	private String			m_databaseName;
	private String			m_connectionString;
	private String			m_systemPassword;
	private String			m_databaseUser;
	private String			m_databasePassword;
	private String			m_TNSName;

	private InetAddress		m_mailServer;
	private InternetAddress	m_adminEMail;
	private String			m_mailUser;
	private String			m_mailPassword;

	private volatile boolean		m_success = false;
	private volatile boolean	 	m_testing = false;

	/** Translation				*/
	static ResourceBundle 		res = ResourceBundle.getBundle("org.compiere.install.SetupRes");

	/**	Driver					*/
	private static Driver		s_driver = null;
	/** Environment Properties	*/
	private Properties			m_properties = new Properties();
	/**	Setup Frame				*/
	private Setup				m_setup = null;
	/** Status Bar				*/
	private JLabel 				m_statusBar;

	private static ImageIcon iOpen = new ImageIcon(ConfigurationPanel.class.getResource("openFile.gif"));
	private static ImageIcon iSave = new ImageIcon(Compiere.class.getResource("images/Save16.gif"));
	private static ImageIcon iHelp = new ImageIcon(Compiere.class.getResource("images/Help16.gif"));


	//	-------------	Static UI
	private GridBagLayout gridBagLayout = new GridBagLayout();
	private CLabel lCompiereHome = new CLabel();
	private CTextField fCompiereHome = new CTextField();
	private CLabel lWebPort = new CLabel();
	private CTextField fWebPort = new CTextField();
	private CLabel lAppsServer = new CLabel();
	private CTextField fAppsServer = new CTextField();
	private CLabel lDatabaseType = new CLabel();
	private CComboBox fDatabaseType = new CComboBox();
	private CLabel lDatabaseName = new CLabel();
	private CLabel lDatabasePort = new CLabel();
	private CLabel lDatabaseUser = new CLabel();
	private CLabel lDatabasePassword = new CLabel();
	private CTextField fDatabaseName = new CTextField();
	private CTextField fDatabasePort = new CTextField();
	private CTextField fDatabaseUser = new CTextField();
	private CPassword fDatabasePassword = new CPassword();
	private CLabel lTNSName = new CLabel();
	private CComboBox fTNSName = new CComboBox();
	private CLabel lSystemPassword = new CLabel();
	private CPassword fSystemPassword = new CPassword();
	private CLabel lMailServer = new CLabel();
	private CTextField fMailServer = new CTextField();
	private CLabel lAdminEMail = new CLabel();
	private CTextField fAdminEMail = new CTextField();
	private CLabel lDatabaseServer = new CLabel();
	private CTextField fDatabaseServer = new CTextField();
	private CLabel lJavaHome = new CLabel();
	private CTextField fJavaHome = new CTextField();
	private CButton bCompiereHome = new CButton(iOpen);
	private CButton bJavaHome = new CButton(iOpen);
	private CButton bHelp = new CButton(iHelp);
	private CButton bTest = new CButton();
	private CButton bSave = new CButton(iSave);
	private CLabel lJNPPort = new CLabel();
	private CTextField fJNPPort = new CTextField();
	private CLabel lMailUser = new CLabel();
	private CLabel lMailPassword = new CLabel();
	private CTextField fMailUser = new CTextField();
	private CPassword fMailPassword = new CPassword();
	private CCheckBox okJavaHome = new CCheckBox();
	private CCheckBox okCompiereHome = new CCheckBox();
	private CCheckBox okAppsServer = new CCheckBox();
	private CCheckBox okWebPort = new CCheckBox();
	private CCheckBox okJNPPort = new CCheckBox();
	private CLabel lSSLPort = new CLabel();
	private CTextField fSSLPort = new CTextField();
	private CCheckBox okSSLPort = new CCheckBox();
	private CCheckBox okDatabaseServer = new CCheckBox();
	private CCheckBox okMailServer = new CCheckBox();
	private CCheckBox okMailUser = new CCheckBox();
	private CCheckBox okDatabaseUser = new CCheckBox();
	private CCheckBox okDatabaseName = new CCheckBox();
	private CLabel lKeyStore = new CLabel();
	private CPassword fKeyStore = new CPassword();
	private CCheckBox okKeyStore = new CCheckBox();
	private CCheckBox okTNSName = new CCheckBox();

	/**
	 * 	Static Layout Init
	 *  @throws Exception
	 */
	private void jbInit() throws Exception
	{
		lCompiereHome.setToolTipText(res.getString("CompiereHomeInfo"));
		lCompiereHome.setText(res.getString("CompiereHome"));
		this.setLayout(gridBagLayout);
		fCompiereHome.setText("C:\\Compiere2");
		lWebPort.setToolTipText(res.getString("WebPortInfo"));
		lWebPort.setText(res.getString("WebPort"));
		fWebPort.setText("80");
		lAppsServer.setToolTipText(res.getString("AppsServerInfo"));
		lAppsServer.setText(res.getString("AppsServer"));
		fAppsServer.setText("server.company.com");
		lDatabaseType.setToolTipText(res.getString("DatabaseTypeInfo"));
		lDatabaseType.setText(res.getString("DatabaseType"));
		lDatabaseName.setToolTipText(res.getString("DatabaseNameInfo"));
		lDatabaseName.setText(res.getString("DatabaseName"));
		lDatabasePort.setToolTipText(res.getString("DatabasePortInfo"));
		lDatabasePort.setText(res.getString("DatabasePort"));
		lDatabaseUser.setToolTipText(res.getString("DatabaseUserInfo"));
		lDatabaseUser.setText(res.getString("DatabaseUser"));
		lDatabasePassword.setToolTipText(res.getString("DatabasePasswordInfo"));
		lDatabasePassword.setText(res.getString("DatabasePassword"));
		fDatabaseName.setText("compiere");
		fDatabasePort.setText("1521");
		fDatabaseUser.setText("compiere");
		fDatabasePassword.setText("compiere");
		lTNSName.setToolTipText(res.getString("TNSNameInfo"));
		lTNSName.setText(res.getString("TNSName"));
		fTNSName.setEditable(true);
		lSystemPassword.setToolTipText(res.getString("SystemPasswordInfo"));
		lSystemPassword.setText(res.getString("SystemPassword"));
		fSystemPassword.setText("manager");
		lMailServer.setToolTipText(res.getString("MailServerInfo"));
		lMailServer.setText(res.getString("MailServer"));
		fMailServer.setText("mail.company.com");
		lAdminEMail.setToolTipText(res.getString("AdminEMailInfo"));
		lAdminEMail.setText(res.getString("AdminEMail"));
		fAdminEMail.setText("admin@company.com");
		bTest.setToolTipText(res.getString("TestInfo"));
		bTest.setText(res.getString("Test"));
		bSave.setToolTipText(res.getString("SaveInfo"));
		bSave.setText(res.getString("Save"));
		lDatabaseServer.setToolTipText(res.getString("DatabaseServerInfo"));
		lDatabaseServer.setText(res.getString("DatabaseServer"));
		lJavaHome.setToolTipText(res.getString("JavaHomeInfo"));
		lJavaHome.setText(res.getString("JavaHome"));
		fJavaHome.setText("Java142");
		lJNPPort.setToolTipText(res.getString("JNPPortInfo"));
		lJNPPort.setText(res.getString("JNPPort"));
		fJNPPort.setText("1099");
		bHelp.setToolTipText(res.getString("HelpInfo"));
		lMailUser.setToolTipText(res.getString("MailUserInfo"));
		lMailUser.setText(res.getString("MailUser"));
		lMailPassword.setToolTipText(res.getString("MailPasswordInfo"));
		lMailPassword.setText(res.getString("MailPassword"));
		fMailUser.setText("compiere");
		fMailPassword.setText("compiere");

		//
		lSSLPort.setText("SSL");
		fSSLPort.setText("443");
		okMailUser.setEnabled(false);
		lKeyStore.setText(res.getString("KeyStorePassword"));
		lKeyStore.setToolTipText(res.getString("KeyStorePasswordInfo"));
		okJavaHome.setEnabled(false);
		okCompiereHome.setEnabled(false);
		okCompiereHome.setDoubleBuffered(false);
		okAppsServer.setEnabled(false);
		okWebPort.setEnabled(false);
		okWebPort.setDoubleBuffered(false);
		okSSLPort.setEnabled(false);
		okJNPPort.setEnabled(false);
		fKeyStore.setText(KEYSTORE_PASSWORD);
		okDatabaseServer.setEnabled(false);
		okTNSName.setEnabled(false);
		okMailServer.setEnabled(false);
		okDatabaseName.setEnabled(false);
		okDatabaseUser.setEnabled(false);
		okKeyStore.setEnabled(false);
		bJavaHome.setMargin(new Insets(2, 10, 2, 10));
		bCompiereHome.setMaximumSize(new Dimension(43, 27));
		bCompiereHome.setMargin(new Insets(2, 10, 2, 10));
		this.add(lCompiereHome,                       new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fCompiereHome,                         new GridBagConstraints(1, 1, 2, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(lWebPort,                       new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fWebPort,                        new GridBagConstraints(1, 3, 4, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(lAppsServer,                      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fAppsServer,                       new GridBagConstraints(1, 2, 3, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(lDatabaseType,                       new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseType,                      new GridBagConstraints(1, 4, 5, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabasePort,                      new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseUser,                       new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseName,                         new GridBagConstraints(1, 6, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabasePort,                        new GridBagConstraints(1, 7, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseUser,                         new GridBagConstraints(1, 8, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lTNSName,                       new GridBagConstraints(7, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(fTNSName,                            new GridBagConstraints(8, 5, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(lMailServer,                      new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lSystemPassword,                       new GridBagConstraints(7, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fSystemPassword,                       new GridBagConstraints(8, 7, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(fDatabasePassword,                        new GridBagConstraints(8, 8, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 0), 0, 0));
		this.add(lDatabasePassword,                      new GridBagConstraints(7, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailServer,                      new GridBagConstraints(1, 9, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(bTest,                      new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 10, 5), 0, 0));
		this.add(bSave,                       new GridBagConstraints(8, 11, 2, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 10, 5), 0, 0));
		this.add(lDatabaseServer,                      new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseServer,                     new GridBagConstraints(1, 5, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseName,                   new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lJavaHome,                  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJavaHome,                        new GridBagConstraints(1, 0, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(bCompiereHome,                 new GridBagConstraints(7, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bHelp,                     new GridBagConstraints(7, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 10, 5), 0, 0));
		this.add(bJavaHome,                 new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lMailPassword,                  new GridBagConstraints(7, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailPassword,                    new GridBagConstraints(8, 10, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 0), 0, 0));
		this.add(lAdminEMail,               new GridBagConstraints(7, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailUser,               new GridBagConstraints(1, 10, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fAdminEMail,                 new GridBagConstraints(8, 9, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 0), 0, 0));
		this.add(lMailUser,              new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 0), 0, 0));
		this.add(lJNPPort,         new GridBagConstraints(7, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJNPPort,         new GridBagConstraints(8, 2, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(okJNPPort,          new GridBagConstraints(9, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(lSSLPort,      new GridBagConstraints(7, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fSSLPort,      new GridBagConstraints(8, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(okSSLPort,     new GridBagConstraints(9, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okDatabaseServer,         new GridBagConstraints(6, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okMailServer,      new GridBagConstraints(6, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 5, 5), 0, 0));
		this.add(okJavaHome,   new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okCompiereHome,  new GridBagConstraints(6, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okAppsServer,  new GridBagConstraints(6, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okWebPort,  new GridBagConstraints(6, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(lKeyStore,  new GridBagConstraints(7, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(fKeyStore,  new GridBagConstraints(8, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(okKeyStore,  new GridBagConstraints(9, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okTNSName, new GridBagConstraints(9, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		//
		bCompiereHome.addActionListener(this);
		bJavaHome.addActionListener(this);
		bHelp.addActionListener(this);
		bTest.addActionListener(this);
		bSave.addActionListener(this);
		this.add(okDatabaseName,  new GridBagConstraints(9, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okDatabaseUser,   new GridBagConstraints(9, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 5, 5), 0, 0));
		this.add(okMailUser,  new GridBagConstraints(9, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
	}	//	jbInit


	/**
	 * 	Dynamic Init
	 *  @throws Exception
	 */
	private void dynInit() throws Exception
	{
		//	DatabaseType
		fDatabaseType.addItem("Oracle 9i2");

		//	Compiere Home
		String ch = System.getProperty(COMPIERE_HOME);
		if (ch == null || ch.length() == 0)
			ch = System.getProperty("user.dir");
		fCompiereHome.setText(ch);
		boolean envLoaded = false;
		String fileName = ch + File.separator + COMPIERE_ENV_FILE;
		File env = new File (fileName);
		System.out.println(env + " - exists=" + env.exists());
		if (env.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(env);
				m_properties.load(fis);
				fis.close();
			}
			catch (Exception e)
			{
				System.err.println(e.toString());
			}
			System.out.println("Loading ...");
			if (m_properties.size() > 5)
				envLoaded = true;
			//
			fCompiereHome.setText((String)m_properties.get(COMPIERE_HOME));
			fJavaHome.setText((String)m_properties.get(JAVA_HOME));
			fAppsServer.setText((String)m_properties.get(COMPIERE_APPS_SERVER));
			fJNPPort.setText((String)m_properties.get(COMPIERE_JNP_PORT));
			fWebPort.setText((String)m_properties.get(COMPIERE_WEB_PORT));
			fSSLPort.setText((String)m_properties.get(COMPIERE_SSL_PORT));
			String s = (String)m_properties.get(COMPIERE_KEYSTOREPASS);
			if (s == null || s.length() == 0)
				s = KEYSTORE_PASSWORD;
			fKeyStore.setText(s);

			fDatabaseServer.setText((String)m_properties.get(COMPIERE_DB_SERVER));
			fDatabasePort.setText((String)m_properties.get(COMPIERE_DB_PORT));
			fDatabaseName.setText((String)m_properties.get(COMPIERE_DB_NAME));

			fDatabaseUser.setText((String)m_properties.get(COMPIERE_DB_USER));
			fDatabasePassword.setText((String)m_properties.get(COMPIERE_DB_PASSWORD));
			fSystemPassword.setText((String)m_properties.get(COMPIERE_DB_SYSTEM));
			fillTNSName((String)m_properties.get(COMPIERE_DB_TNS));

			fMailServer.setText((String)m_properties.get(COMPIERE_MAIL_SERVER));
			fMailUser.setText((String)m_properties.get(COMPIERE_MAIL_USER));
			fMailPassword.setText((String)m_properties.get(COMPIERE_MAIL_PASSWORD));
			fAdminEMail.setText((String)m_properties.get(COMPIERE_ADMIN_EMAIL));
		}

		//	No environment file found - defaults
		if (!envLoaded)
		{
			System.out.println("Setting Defaults");
			//	Java Home, e.g. D:\j2sdk1.4.1\jre
			String javaHome = System.getProperty("java.home");
			if (javaHome.endsWith("jre"))
				javaHome = javaHome.substring(0, javaHome.length()-4);
			fJavaHome.setText(javaHome);
			//	AppsServer
			fAppsServer.setText(InetAddress.getLocalHost().getHostName());
			//	Database Server
			fDatabaseServer.setText(InetAddress.getLocalHost().getHostName());
			//	Mail Server
			fMailServer.setText(InetAddress.getLocalHost().getHostName());
			//  Get TNS Names
			fillTNSName("");
		}	//	!envLoaded

		//	Default FTP stuff
		if (!m_properties.containsKey(COMPIERE_FTP_SERVER))
		{
			m_properties.setProperty(COMPIERE_FTP_SERVER, "localhost");
			m_properties.setProperty(COMPIERE_FTP_USER, "anonymous");
			m_properties.setProperty(COMPIERE_FTP_PASSWORD, "user@host.com");
			m_properties.setProperty(COMPIERE_FTP_PREFIX, "my");
		}
		//	Default Java Options
		if (!m_properties.containsKey(COMPIERE_JAVA_OPTIONS))
			m_properties.setProperty(COMPIERE_JAVA_OPTIONS, "-Xms64M -Xmx512M");
		//	Web Alias
		if (!m_properties.containsKey(COMPIERE_WEB_ALIAS))
			m_properties.setProperty(COMPIERE_WEB_ALIAS, InetAddress.getLocalHost().getCanonicalHostName());
		
		//	(String)m_properties.get(COMPIERE_DB_URL)	//	derived
	}	//	dynInit

	/**
	 * 	Fill TNS Name
	 * 	@param defaultValue defaultValue
	 */
	private void fillTNSName (String defaultValue)
	{
		//	default value to lowercase or null
		String def = defaultValue;
		if (def != null && def.trim().length() == 0)
			def = null;
		if (def != null)
		{
			def = def.toLowerCase();
			fTNSName.addItem(def);
		}

		//	Search for Oracle Info
		String path = System.getProperty("java.library.path");
		String[] entries = path.split(File.pathSeparator);
		for (int e = 0; e < entries.length; e++)
		{
			String entry = entries[e].toLowerCase();
			if (entry.indexOf("ora") != -1 && entry.endsWith("bin"))
			{
				StringBuffer sb = getTNS_File (entries[e].substring(0, entries[e].length()-4));
				String[] tnsnames = getTNS_Names (sb);
				if (tnsnames != null)
				{
					for (int i = 0; i < tnsnames.length; i++)
					{
						String tns = tnsnames[i];
						if (!tns.equals(def))
							fTNSName.addItem(tns);
					}
					break;
				}
			}
		}	//	for all path entries

		//	Set first entry
		fTNSName.addActionListener(this);
		if (fTNSName.getItemCount() > 0)
			fTNSName.setSelectedIndex(0);
	}	//	fillTNSName

	/**
	 * 	Get File tnmsnames.ora in StringBuffer
	 * 	@param oraHome ORACLE_HOME
	 * 	@return tnsnames.ora or null
	 */
	private StringBuffer getTNS_File (String oraHome)
	{
		String tnsnames = oraHome + File.separator
			+ "network" + File.separator
			+ "admin" + File.separator
			+ "tnsnames.ora";
		File tnsfile = new File (tnsnames);
		if (!tnsfile.exists())
			return null;

		System.out.println("Searching " + tnsnames);
		StringBuffer sb = new StringBuffer();
		try
		{
			FileReader fr = new FileReader (tnsfile);
			int c;
			while ((c = fr.read()) != -1)
				sb.append((char)c);
		}
		catch (IOException ex)
		{
			System.err.println("Error Reading " + tnsnames);
			ex.printStackTrace();
			return null;
		}
		if (sb.length() == 0)
			return null;
		return sb;
	}	//	getTNS_File

	/**
	 * 	Get TNS Names entries.
	 * 	Assumes standard tnsmanes.ora formatting of NetMgr
	 * 	@param tnsnames content of tnsnames.ora
	 * 	@return tns names or null
	 */
	private String[] getTNS_Names (StringBuffer tnsnames)
	{
		if (tnsnames == null)
			return null;

		ArrayList list = new ArrayList();
		Pattern pattern = Pattern.compile("$", Pattern.MULTILINE);
		String[] lines = pattern.split(tnsnames);
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i].trim();
			if (line.length() > 0
				&& Character.isLetter(line.charAt(0))	//	no # (
				&& line.indexOf("=") != -1
				&& line.indexOf("EXTPROC_") == -1
				&& line.indexOf("_HTTP") == -1)
			{
				String entry = line.substring(0, line.indexOf('=')).trim().toLowerCase();
				System.out.println("- " + entry);
				list.add(entry);
			}
		}
		//	Convert to Array
		if (list.size() == 0)
			return null;
		String[] retValue = new String[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getTNS_Names

	/**************************************************************************

	/**
	 * 	ActionListener
	 *  @param e event
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (m_testing)
			return;
		//	TNS Name Changed
		if (e.getSource() == fTNSName)
		{
			String tns = (String)fTNSName.getSelectedItem();
			if (tns != null)
			{
				int pos = tns.indexOf('.');
				if (pos != -1)
					tns = tns.substring(0, pos);
				fDatabaseName.setText(tns);
			}
		}

		else if (e.getSource() == bJavaHome)
			setPath (fJavaHome);
		else if (e.getSource() == bCompiereHome)
			setPath (fCompiereHome);
		else if (e.getSource() == bHelp)
			new Setup_Help((Frame)SwingUtilities.getWindowAncestor(this));
		else if (e.getSource() == bTest)
			startTest(false);
		else if (e.getSource() == bSave)
			startTest(true);
	}	//	actionPerformed

	/**
	 * 	Set Path in Field
	 * 	@param field field to set Path
	 */
	private void setPath (CTextField field)
	{
		JFileChooser fc = new JFileChooser(field.getText());
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			field.setText(fc.getSelectedFile().getAbsolutePath());
	}	//	setPath

	/**************************************************************************

	/**
	 * 	Start Test Async.
	 *  @return SwingWorker
	 */
	private SwingWorker startTest(final boolean saveIt)
	{
		SwingWorker worker = new SwingWorker()
		{
			//	Start it
			public Object construct()
			{
				m_testing = true;
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				bTest.setEnabled(false);
				m_success = false;
				m_errorString = null;
				try
				{
					test();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					m_errorString += "\n" + ex.toString();
				}
				//
				setCursor(Cursor.getDefaultCursor());
				if (m_errorString == null)
					m_success = true;
				bTest.setEnabled(true);
				m_testing = false;
				return new Boolean(m_success);
			}
			//	Finish it
			public void finished()
			{
				if (m_errorString != null)
				{
					System.err.println(m_errorString);
					JOptionPane.showConfirmDialog (null, m_errorString, res.getString("ServerError"),
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
				}
				else if (saveIt)
					save();
			}
		};
		worker.start();
		return worker;
	}	//	startIt

	/**
	 *	Test it
	 * 	@throws Exception
	 */
	private void test() throws Exception
	{
		System.out.println();
		System.out.println("** Test **");
		bSave.setEnabled(false);


		//	Java Home
		m_errorString = res.getString("ErrorJavaHome");
		m_javaHome = new File (fJavaHome.getText());
		if (!signalOK(okJavaHome, m_javaHome.exists(), true, "Not found: Java Home"))
			return;
		File tools = new File (fJavaHome.getText() + File.separator + "lib" + File.separator + "tools.jar");
		if (!signalOK(okJavaHome, tools.exists(), true, 
				"Not found: Java SDK = " + tools))
			return;
		System.out.println("OK: JavaHome=" + m_javaHome.getAbsolutePath());
		m_properties.setProperty(JAVA_HOME, m_javaHome.getAbsolutePath());
		System.setProperty(JAVA_HOME, m_javaHome.getAbsolutePath());
		
		
		//	Java Version
		final String VERSION = "1.4.1";
		final String VERSION2 = "1.4.2";
		boolean versionOK = false;
		String jh = m_javaHome.getAbsolutePath();
		if (jh.indexOf(VERSION) != -1)	//	file name has version = assuming OK
			versionOK = true;
		if (!versionOK && jh.indexOf(VERSION2) != -1)	//
			versionOK = true;
		String thisJH = System.getProperty("java.home");
		if (thisJH.indexOf(jh) != -1)	//	we are running the version currently
		{
			String thisJV = System.getProperty("java.version");
			versionOK = thisJV.indexOf(VERSION) != -1;
			if (!versionOK && thisJV.indexOf(VERSION2) != -1)
				versionOK = true;
			if (versionOK)
			  System.out.println("  Java Version OK = " + thisJV);
		}
		if (!signalOK(okJavaHome, versionOK, true, 
				"Wrong Java Version: Should be " + VERSION2))
			return;

		
		//	Compiere Home
		m_errorString = res.getString("ErrorCompiereHome");
		m_compiereHome = new File (fCompiereHome.getText());
		if (!signalOK(okCompiereHome, m_compiereHome.exists(), true, "Not found: CompiereHome = " + m_compiereHome))
			return;
		System.out.println("OK: CompiereHome = " + m_compiereHome);
		m_properties.setProperty(COMPIERE_HOME, m_compiereHome.getAbsolutePath());
		System.setProperty(COMPIERE_HOME, m_compiereHome.getAbsolutePath());
		

		//	AppsServer
		m_errorString = res.getString("ErrorAppsServer");
		String server = fAppsServer.getText();
		boolean error = (server == null || server.length() == 0 
			|| server.toLowerCase().indexOf("localhost") != -1 || server.equals("127.0.0.1"));
		if (!error)
			m_appsServer = InetAddress.getByName(server);
		if (!signalOK(okAppsServer, !error, true, 
				"Not correct: AppsServer = " + server))
			return;
		System.out.println("OK: AppsServer = " + m_appsServer.getHostName());
		m_properties.setProperty(COMPIERE_APPS_SERVER, m_appsServer.getHostName());

		
		//	JNP Port
		m_errorString = res.getString("ErrorJNPPort");
		m_statusBar.setText(lJNPPort.getText());
		m_JNPPort = Integer.parseInt(fJNPPort.getText());
		error = (testPort (m_appsServer, m_JNPPort, false) || !testServerPort(m_JNPPort));
		if (!signalOK(okJNPPort, !error, true, "Not correct: JNP Port = " + m_JNPPort))
			return;
		System.out.println("OK: JNPPort = " + m_JNPPort);
		m_properties.setProperty(COMPIERE_JNP_PORT, String.valueOf(m_JNPPort));


		//	Web Port
		m_errorString = res.getString("ErrorWebPort");
		m_statusBar.setText(lWebPort.getText());
		m_WebPort = Integer.parseInt(fWebPort.getText());
		error = (testPort ("http", m_appsServer.getHostName(), m_WebPort, "/") 
			|| !testServerPort(m_WebPort));
		if (!signalOK(okWebPort, !error, true, 
				"Not correct: Web Port = " + m_WebPort))
			return;
		System.out.println("OK: Web Port = " + m_WebPort);
		m_properties.setProperty(COMPIERE_WEB_PORT, String.valueOf(m_WebPort));
		
		//	SSL Port
		int sslPort = Integer.parseInt(fSSLPort.getText());
		error = (testPort ("https", m_appsServer.getHostName(), sslPort, "/") 
			|| !testServerPort(sslPort));
		if (!signalOK(okSSLPort, !error, true, 
				"Not correct: SSL Port = " + sslPort))
			return;
		System.out.println("OK: SSL Port = " + sslPort);
		m_properties.setProperty(COMPIERE_SSL_PORT, String.valueOf(sslPort));

		
		//	KeyStore
		m_errorString = res.getString("KeyStorePassword");
		String fileName = MyKeyStore.getKeystoreFileName(m_compiereHome.getAbsolutePath());
		m_properties.setProperty(COMPIERE_KEYSTORE, fileName);
		
		//	KeyStore Password
		String pw = new String(fKeyStore.getPassword());
		if (!signalOK(okKeyStore, pw != null && pw.length() > 0, true, 
				"Invalid Key Store Password = " + pw))
			return;
		m_properties.setProperty(COMPIERE_KEYSTOREPASS, pw);
		MyKeyStore ks = new MyKeyStore (fileName, fKeyStore.getPassword());
		String errorString = ks.verify();
		if (!signalOK(okKeyStore, errorString == null, true, errorString))
			return;
		System.out.println("OK: KeyStore = " + fileName);
		
		
		//	Database Server
		m_errorString = res.getString("ErrorDatabaseServer");
		m_statusBar.setText(lDatabaseServer.getText());
		server = fDatabaseServer.getText();
		error = (server == null || server.length() == 0 
			|| server.toLowerCase().indexOf("localhost") != -1 || server.equals("127.0.0.1"));
		if (!error)
			m_databaseServer = InetAddress.getByName(server);
		if (!signalOK(okDatabaseServer, !error, true, 
				"Not correct: DB Server = " + server))
			return;
		System.out.println("OK: Database Server = " + m_databaseServer.getHostName());
		m_properties.setProperty(COMPIERE_DB_SERVER, m_databaseServer.getHostName());


		//	Database Port
		m_errorString = res.getString("ErrorDatabasePort");
		m_databasePort = Integer.parseInt(fDatabasePort.getText());
		if (!signalOK(okDatabaseServer, testPort (m_databaseServer, m_databasePort, true), true, 
				"Not correct: DB Server Port = " + m_databasePort))
			return;
		System.out.println("OK: Database Port = " + m_databasePort);
		m_properties.setProperty(COMPIERE_DB_PORT, String.valueOf(m_databasePort));


		//	JDBC Database Info
		m_errorString = res.getString("ErrorJDBC");
		m_statusBar.setText(lDatabaseName.getText());
		m_databaseName = fDatabaseName.getText();	//	SID
		m_systemPassword = new String(fSystemPassword.getPassword());
		if (!signalOK(okDatabaseName, m_systemPassword != null && m_systemPassword.length() > 0, true, 
				"Invalid Password"))
			return;
		if (!signalOK(okDatabaseName, testJDBC("system", m_systemPassword), true,
				"Error connecting to Database: " + m_databaseName 
				+ " as system/" + m_systemPassword))
			return;
		System.out.println("OK: Database Name = " + m_databaseName);
		m_properties.setProperty(COMPIERE_DB_NAME, m_databaseName);
		System.out.println("OK: Database SystemID = system/" + m_systemPassword);
		m_properties.setProperty(COMPIERE_DB_SYSTEM, m_systemPassword);

		//	URL (derived)	jdbc:oracle:thin:@prod1:1521:prod1
		StringBuffer url = new StringBuffer("jdbc:oracle:thin:@")
			.append(m_databaseServer.getHostName()).append(":")
			.append(m_databasePort).append(":").append(m_databaseName);
		System.out.println("OK: Database URL = " + url.toString());
		m_properties.setProperty(COMPIERE_DB_URL, url.toString());


		//	Database User Info
		m_databaseUser = fDatabaseUser.getText();	//	UID
		m_databasePassword = new String(fDatabasePassword.getPassword());	//	PWD
		if (!signalOK(okDatabaseUser, m_databasePassword != null && m_databasePassword.length() > 0, true, 
				"Invalid Password"))
			return;
		//	Ignore result as it might not be imported
		if (signalOK(okDatabaseUser, testJDBC(m_databaseUser, m_databasePassword), false,
				"Not created yet for Database: " + m_databaseName 
				+ " User: " + m_databaseUser + "/" + m_databasePassword))
			System.out.println("OK: Database UserID = " + m_databaseUser + "/" + m_databasePassword);
		m_properties.setProperty(COMPIERE_DB_USER, m_databaseUser);
		m_properties.setProperty(COMPIERE_DB_PASSWORD, m_databasePassword);


		//	TNS Name Info
		m_errorString = res.getString("ErrorTNS");
		m_statusBar.setText(lTNSName.getText());
		m_TNSName = (String)fTNSName.getSelectedItem();
		if (!signalOK(okTNSName, testTNS("system", m_systemPassword), true,
				"Error connecting to Database: " + m_databaseName 
				+ " via TNS Name: " + m_TNSName))
			return;
		System.out.println("OK: Database TNS Name = " + m_TNSName);
		m_properties.setProperty(COMPIERE_DB_TNS, m_TNSName);


		//	Mail Server
		m_errorString = res.getString("ErrorMailServer");
		m_statusBar.setText(lMailServer.getText());
		server = fMailServer.getText();
		error = (server == null || server.length() == 0 
				|| server.toLowerCase().indexOf("localhost") != -1 || server.equals("127.0.0.1"));
		if (!error)
			m_mailServer = InetAddress.getByName(server);
		if (!signalOK(okMailServer, !error, true,
				"Error Mail Server = " + server))
			return;
		System.out.println("OK: Mail Server = " + m_mailServer);
		m_properties.setProperty(COMPIERE_MAIL_SERVER, m_mailServer.getHostName());


		//	Mail User
		m_errorString = "ErrorMailUser";
		m_statusBar.setText(lMailUser.getText());
		m_mailUser = fMailUser.getText();
		m_mailPassword = new String(fMailPassword.getPassword());
		m_properties.setProperty(COMPIERE_MAIL_USER, m_mailUser);
		m_properties.setProperty(COMPIERE_MAIL_PASSWORD, m_mailPassword);
		System.out.println("  Mail User = " + m_mailUser + "/" + m_mailPassword);

		//	Mail Address
		m_errorString = res.getString("ErrorMail");
		m_adminEMail = new InternetAddress (fAdminEMail.getText());
		//
		if (signalOK(okMailUser, testMail(), false, 
				"Not verified Admin EMail = " + m_adminEMail))
			System.out.println("OK: Admin EMail = " + m_adminEMail);
		m_properties.setProperty(COMPIERE_ADMIN_EMAIL, m_adminEMail.toString());

		//
		m_statusBar.setText(res.getString("Ok"));
		System.out.println("** Test OK **");
		bSave.setEnabled(true);
		m_errorString = null;
	}	//	test

	/**
	 * 	Signal OK
	 *	@param cb ckeck box
	 *	@param pass trus if test passed
	 *	@param critical true if critial
	 *	@param srrorMsg error Message
	 *	@return pass
	 */
	private boolean signalOK (CCheckBox cb, boolean pass, boolean critical, String errorMsg)
	{
		cb.setSelected(pass);
		if (pass)
			cb.setToolTipText(null);
		else
		{
			cb.setToolTipText(errorMsg);
			if (critical)
				System.err.println(errorMsg);
			else
				System.out.println(errorMsg);
			m_errorString += " \n(" + errorMsg + ")";
		}
		if (!pass && critical)
			cb.setBackground(Color.RED);
		else
			cb.setBackground(Color.GREEN);
		return pass;
	}	//	setOK

	/**
	 * 	Test Apps Server Port (client perspective)
	 *  @param protocol protocol (http, ..)
	 *  @param server server name
	 *  @param port port
	 *  @param file file name
	 *  @return true if able to connect
	 */
	private boolean testPort (String protocol, String server, int port, String file)
	{
		URL url = null;
		try
		{
			url = new URL (protocol, server, port, file);
		}
		catch (MalformedURLException ex)
		{
			ex.printStackTrace();
			return false;
		}
		System.out.println("  URL=" + url);
		try
		{
			URLConnection c = url.openConnection();
			Object o = c.getContent();
			System.err.println("  URL Connection in use=" + url);	//	error
		}
		catch (Exception ex)
		{
			return false;
		}
		return true;
	}	//	testPort

	/**
	 * 	Test Server Port
	 *  @param port port
	 *  @return true if able to create
	 */
	private boolean testServerPort (int port)
	{
		try
		{
			ServerSocket ss = new ServerSocket (port);
			System.out.println("  ServerPort " + ss.getInetAddress() + ":" + ss.getLocalPort());
			ss.close();
		}
		catch (Exception ex)
		{
			System.err.println(ex);
			return false;
		}
		return true;
	}	//	testPort


	/**
	 * 	Test Port
	 *  @param host host
	 *  @param port port
	 *  @param shouldBeUsed true if it should be used
	 *  @return true if some server answered on port
	 */
	public boolean testPort (InetAddress host, int port, boolean shouldBeUsed)
	{
		Socket pingSocket = null;
		try
		{
			pingSocket = new Socket(host, port);
		}
		catch (Exception e)
		{
			if (shouldBeUsed)
				System.err.println("  Open Socket " + host + " on " + port + ": " + e.getMessage());
			return false;
		}
		if (!shouldBeUsed)
			System.err.println("  Open Socket " + host + " on " + port);
		if (pingSocket == null)
			return false;
		//	success
		try
		{
			pingSocket.close();
		}
		catch (IOException e)
		{
			System.out.println("  CloseSocket=" + e.toString());
		}
		return true;
	}	//	testPort

	/**
	 * 	Test JDBC Connection to Server
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testJDBC (String uid, String pwd)
	{
		//	jdbc:oracle:thin:@dev:1521:dev1
		m_connectionString = "jdbc:oracle:thin:@"
			+ m_databaseServer.getHostName() + ":" + m_databasePort + ":" + m_databaseName;
		System.out.println("  JDBC = " + m_connectionString);

		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(m_connectionString,
				uid, pwd);
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testJDBC

	/**
	 * 	Test TNS Connection
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testTNS (String uid, String pwd)
	{
		String connectionString = "jdbc:oracle:oci8:@" + m_TNSName;
		System.out.println("  TNS = " + connectionString);
		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(connectionString,
				uid, pwd);
		}
		catch (UnsatisfiedLinkError ule)
		{
			System.err.println("Check setup of Oracle Server / Oracle Client / LD_LIBRARY_PATH");
			System.err.println(ule.toString());
			return false;
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testTNS

	/**
	 * 	Test Mail
	 *  @return true of OK
	 */
	private boolean testMail()
	{
		boolean smtpOK = false;
		boolean imapOK = false;
		if (testPort (m_mailServer, 25, true))
		{
			System.out.println("OK: SMTP Server contacted");
			smtpOK = true;
		}
		else
			System.err.println("Error: SMTP Server NOT available");
		//
		if (testPort (m_mailServer, 110, true))
			System.out.println("OK: POP3 Server contacted");
		else
			System.err.println("Error: POP3 Server NOT available");
		if (testPort (m_mailServer, 143, true))
		{
			System.out.println("OK: IMAP4 Server contacted");
			imapOK = true;
		}
		else
			System.err.println("Error: IMAP4 Server NOT available");
		//
		if (!smtpOK)
		{
			signalOK (okMailServer, false, false, "No active Mail Server");
			return false;
		}
		//
		try
		{
			EMail em = new EMail (m_mailServer.getHostName (),
					   m_adminEMail.toString (), m_adminEMail.toString (),
					   "Compiere Server Setup Test", "Test: " + m_properties);
			em.setEMailUser (m_mailUser, m_mailPassword);
			if (EMail.SENT_OK.equals (em.send ()))
			{
				System.out.println ("OK: Send Test Email to " + m_adminEMail);
			}
			else
			{
				System.err.println ("Error: Could NOT send Email to "
					+ m_adminEMail);
			}
		}
		catch (Exception ex)
		{
			System.err.println("testMail - " + ex.getLocalizedMessage());
			return false;
		}

		//
		if (!imapOK)
			return false;

		//	Test Read Mail Access
		Properties props = new Properties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_mailServer.getHostName());
		props.put("mail.user", m_mailUser);
		props.put("mail.smtp.auth", "true");
		System.out.println("  Connecting to " + m_mailServer.getHostName());
		//
		Session session = null;
		Store store = null;
		try
		{
			EMailAuthenticator auth = new EMailAuthenticator (m_mailUser, m_mailPassword);
			session = Session.getDefaultInstance(props, auth);
			session.setDebug (Log.isTraceLevel (10));
			System.out.println("  Session=" + session);
			//	Connect to Store
			store = session.getStore("imap");
			System.out.println("  Store=" + store);
		}
		catch (NoSuchProviderException nsp)
		{
			System.err.println("Error Mail IMAP Provider - " + nsp.getMessage());
			return false;
		}
		catch (Exception e)
		{
			System.err.println("Error Mail IMAP - " + e.getMessage());
			return false;
		}
		try
		{
			store.connect(m_mailServer.getHostName(), m_mailUser, m_mailPassword);
			System.out.println("  Store - connected");
			Folder folder = store.getDefaultFolder();
			Folder inbox = folder.getFolder("INBOX");
			System.out.println("OK: Mail Connect to " + inbox.getFullName() + " #Msg=" + inbox.getMessageCount());
			//
			store.close();
		}
		catch (MessagingException mex)
		{
			System.err.println("Error Mail Connect " + mex.getMessage());
			return false;
		}
		return true;
	}	//	testMail

	/**************************************************************************
	 * 	Save Settings.
	 * 	Called from startTest.finished()
	 */
	private void save()
	{
		if (!m_success)
			return;

		bSave.setEnabled(false);
		bTest.setEnabled(false);
		
		//	Add
		m_properties.setProperty("COMPIERE_MAIN_VERSION", Compiere.MAIN_VERSION);
		m_properties.setProperty("COMPIERE_DATE_VERSION", Compiere.DATE_VERSION);
		m_properties.setProperty("COMPIERE_DB_VERSION", Compiere.DB_VERSION);



		//	Before we save, load Ini
		Ini.setClient(false);
		String fileName = m_compiereHome.getAbsolutePath() + File.separator + Ini.COMPIERE_PROPERTY_FILE;
		Ini.loadProperties(fileName);

		//	Save Environment
		fileName = m_compiereHome.getAbsolutePath() + File.separator + COMPIERE_ENV_FILE;
		try
		{
			FileOutputStream fos = new FileOutputStream(new File(fileName));
			m_properties.store(fos, COMPIERE_ENV_FILE);
			fos.flush();
			fos.close();
		}
		catch (Exception e)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + e.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (Throwable t)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + t.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		System.out.println("Properties saved to " + fileName);

		//	Sync Properties
		Ini.setCompiereHome(m_compiereHome.getAbsolutePath());
		CConnection cc = CConnection.get (Database.DB_ORACLE,
			m_databaseServer.getHostName(), m_databasePort, m_databaseName,
			m_databaseUser, m_databasePassword);
		cc.setAppsHost(m_appsServer.getHostName());
		cc.setRMIoverHTTP(false);
		Ini.setProperty(Ini.P_CONNECTION, cc.toStringLong());
		Ini.saveProperties(false);
		
		//	Final Info
		JOptionPane.showConfirmDialog(this, res.getString("EnvironmentSaved"),
			res.getString("CompiereServerSetup"),
			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

		//	Run Ant
		try
		{
			System.out.println("Starting Ant ... ");
			System.setProperty("ant.home", ".");
			String[] 	args = new String[] {"setup"};
			Launcher.main (args);	//	calls System.exit
		}
		catch (Exception e)
		{
		}
			
		//	To be sure
		((Frame)SwingUtilities.getWindowAncestor(this)).dispose();
		System.exit(0);		//	remains active when License Dialog called
	}	//	save

}	//	ConfigurationPanel
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2002 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.install;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.sql.*;
import oracle.jdbc.*;

import javax.mail.*;
import javax.mail.internet.*;

import org.compiere.Compiere;
import org.compiere.apps.SwingWorker;
import org.compiere.db.*;
import org.compiere.util.*;


/**
 *	Configuration Panel
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: ConfigurationPanel.java,v 1.14 2003/02/21 06:37:54 jjanke Exp $
 */
public class ConfigurationPanel extends JPanel
	implements ActionListener
{
	/**
	 * 	Constructor
	 *  @param statusBar for info
	 * 	@throws Exception
	 */
	public ConfigurationPanel(JLabel statusBar) throws Exception
	{
		m_statusBar = statusBar;
		jbInit();
		dynInit();
	}	//	ConfigurationPanel

	public static final String	COMPIERE_ENV_FILE	= "CompiereEnv.properties";

	public static final String	COMPIERE_HOME 		= "COMPIERE_HOME";
	public static final String	JAVA_HOME 			= "JAVA_HOME";
	public static final String	COMPIERE_JAVA_OPTIONS = "COMPIERE_JAVA_OPTIONS";

	public static final String	COMPIERE_APPS_SERVER 	= "COMPIERE_APPS_SERVER";
	public static final String	COMPIERE_JNP_PORT 	= "COMPIERE_JNP_PORT";
	public static final String	COMPIERE_WEB_PORT 	= "COMPIERE_WEB_PORT";
	public static final String	COMPIERE_SSL_PORT 	= "COMPIERE_SSL_PORT";

	public static final String	COMPIERE_DB_SERVER 	= "COMPIERE_DB_SERVER";
	public static final String	COMPIERE_DB_PORT 	= "COMPIERE_DB_PORT";
	public static final String	COMPIERE_DB_NAME 	= "COMPIERE_DB_NAME";

	public static final String	COMPIERE_DB_USER 	= "COMPIERE_DB_USER";
	public static final String	COMPIERE_DB_PASSWORD = "COMPIERE_DB_PASSWORD";
	public static final String	COMPIERE_DB_SYSTEM 	= "COMPIERE_DB_SYSTEM";

	public static final String	COMPIERE_DB_URL 	= "COMPIERE_DB_URL";
	public static final String	COMPIERE_DB_TNS 	= "COMPIERE_DB_TNS";

	public static final String	COMPIERE_MAIL_SERVER 	= "COMPIERE_MAIL_SERVER";
	public static final String	COMPIERE_MAIL_USER 		= "COMPIERE_MAIL_USER";
	public static final String	COMPIERE_MAIL_PASSWORD 	= "COMPIERE_MAIL_PASSWORD";
	public static final String	COMPIERE_ADMIN_EMAIL	= "COMPIERE_ADMIN_EMAIL";

	public static final String	COMPIERE_FTP_SERVER		= "COMPIERE_FTP_SERVER";
	public static final String	COMPIERE_FTP_USER		= "COMPIERE_FTP_USER";
	public static final String	COMPIERE_FTP_PASSWORD	= "COMPIERE_FTP_PASSWORD";
	public static final String	COMPIERE_FTP_PREFIX		= "COMPIERE_FTP_PREFIX";

	private String			m_errorString;
	private File			m_javaHome;
	private File			m_compiereHome;
	private InetAddress		m_appsServer;
	private int				m_WebPort;
	private int				m_JNPPort;
	private InetAddress		m_databaseServer;
	private int				m_databasePort;
	private String			m_databaseName;
	private String			m_connectionString;
	private String			m_systemPassword;
	private String			m_databaseUser;
	private String			m_databasePassword;
	private String			m_TNSName;

	private InetAddress		m_mailServer;
	private InternetAddress	m_adminEMail;
	private String			m_mailUser;
	private String			m_mailPassword;

	private volatile boolean		m_success = false;
	private volatile boolean	 	m_testing = false;

	/** Translation				*/
	static ResourceBundle 		res = ResourceBundle.getBundle("org.compiere.install.SetupRes");

	/**	Driver					*/
	private static Driver		s_driver = null;
	/** Environment Properties	*/
	private Properties			m_properties = new Properties();
	/** Status Bar				*/
	private JLabel 				m_statusBar;

	private static ImageIcon iOpen = new ImageIcon(ConfigurationPanel.class.getResource("openFile.gif"));
	private static ImageIcon iSave = new ImageIcon(Compiere.class.getResource("images/Save16.gif"));
	private static ImageIcon iHelp = new ImageIcon(Compiere.class.getResource("images/Help16.gif"));


	//	-------------	Static UI
	private GridBagLayout gridBagLayout = new GridBagLayout();
	private JLabel lCompiereHome = new JLabel();
	private JTextField fCompiereHome = new JTextField();
	private JLabel lWebPort = new JLabel();
	private JTextField fWebPort = new JTextField();
	private JLabel lAppsServer = new JLabel();
	private JTextField fAppsServer = new JTextField();
	private JLabel lDatabaseType = new JLabel();
	private JComboBox fDatabaseType = new JComboBox();
	private JLabel lDatabaseName = new JLabel();
	private JLabel lDatabasePort = new JLabel();
	private JLabel lDatabaseUser = new JLabel();
	private JLabel lDatabasePassword = new JLabel();
	private JTextField fDatabaseName = new JTextField();
	private JTextField fDatabasePort = new JTextField();
	private JTextField fDatabaseUser = new JTextField();
	private JPasswordField fDatabasePassword = new JPasswordField();
	private JLabel lTNSName = new JLabel();
	private JComboBox fTNSName = new JComboBox();
	private JLabel lSystemPassword = new JLabel();
	private JPasswordField fSystemPassword = new JPasswordField();
	private JLabel lMailServer = new JLabel();
	private JTextField fMailServer = new JTextField();
	private JLabel lAdminEMail = new JLabel();
	private JTextField fAdminEMail = new JTextField();
	private JLabel lDatabaseServer = new JLabel();
	private JTextField fDatabaseServer = new JTextField();
	private JLabel lJavaHome = new JLabel();
	private JTextField fJavaHome = new JTextField();
	private JButton bCompiereHome = new JButton(iOpen);
	private JButton bJavaHome = new JButton(iOpen);
	private JButton bHelp = new JButton(iHelp);
	private JButton bTest = new JButton();
	private JButton bSave = new JButton(iSave);
	private JLabel lJNPPort = new JLabel();
	private JTextField fJNPPort = new JTextField();
	private JLabel lMailUser = new JLabel();
	private JLabel lMailPassword = new JLabel();
	private JTextField fMailUser = new JTextField();
	private JPasswordField fMailPassword = new JPasswordField();


	/**
	 * 	Static Layout Init
	 *  @throws Exception
	 */
	private void jbInit() throws Exception
	{
		lCompiereHome.setToolTipText(res.getString("CompiereHomeInfo"));
		lCompiereHome.setText(res.getString("CompiereHome"));
		this.setLayout(gridBagLayout);
		fCompiereHome.setText("D:\\Compiere2");
		lWebPort.setToolTipText(res.getString("WebPortInfo"));
		lWebPort.setText(res.getString("WebPort"));
		fWebPort.setText("80");
		lAppsServer.setToolTipText(res.getString("AppsServerInfo"));
		lAppsServer.setText(res.getString("AppsServer"));
		fAppsServer.setText("server.company.com");
		lDatabaseType.setToolTipText(res.getString("DatabaseTypeInfo"));
		lDatabaseType.setText(res.getString("DatabaseType"));
		lDatabaseName.setToolTipText(res.getString("DatabaseNameInfo"));
		lDatabaseName.setText(res.getString("DatabaseName"));
		lDatabasePort.setToolTipText(res.getString("DatabasePortInfo"));
		lDatabasePort.setText(res.getString("DatabasePort"));
		lDatabaseUser.setToolTipText(res.getString("DatabaseUserInfo"));
		lDatabaseUser.setText(res.getString("DatabaseUser"));
		lDatabasePassword.setToolTipText(res.getString("DatabasePasswordInfo"));
		lDatabasePassword.setText(res.getString("DatabasePassword"));
		fDatabaseName.setText("compiere");
		fDatabasePort.setText("1521");
		fDatabaseUser.setText("compiere");
		fDatabasePassword.setText("compiere");
		lTNSName.setToolTipText(res.getString("TNSNameInfo"));
		lTNSName.setText(res.getString("TNSName"));
		fTNSName.setEditable(true);
		lSystemPassword.setToolTipText(res.getString("SystemPasswordInfo"));
		lSystemPassword.setText(res.getString("SystemPassword"));
		fSystemPassword.setText("manager");
		lMailServer.setToolTipText(res.getString("MailServerInfo"));
		lMailServer.setText(res.getString("MailServer"));
		fMailServer.setText("mail.company.com");
		lAdminEMail.setToolTipText(res.getString("AdminEMailInfo"));
		lAdminEMail.setText(res.getString("AdminEMail"));
		fAdminEMail.setText("admin@company.com");
		bTest.setToolTipText(res.getString("TestInfo"));
		bTest.setText(res.getString("Test"));
		bSave.setToolTipText(res.getString("SaveInfo"));
		bSave.setText(res.getString("Save"));
		lDatabaseServer.setToolTipText(res.getString("DatabaseServerInfo"));
		lDatabaseServer.setText(res.getString("DatabaseServer"));
		lJavaHome.setToolTipText(res.getString("JavaHomeInfo"));
		lJavaHome.setText(res.getString("JavaHome"));
		fJavaHome.setText("Java141");
		lJNPPort.setToolTipText(res.getString("JNPPortInfo"));
		lJNPPort.setText(res.getString("JNPPort"));
		fJNPPort.setText("1099");
		bHelp.setToolTipText(res.getString("HelpInfo"));
		lMailUser.setToolTipText(res.getString("MailUserInfo"));
		lMailUser.setText(res.getString("MailUser"));
		lMailPassword.setToolTipText(res.getString("MailPasswordInfo"));
		lMailPassword.setText(res.getString("MailPassword"));
		fMailUser.setText("compiere");
		fMailPassword.setText("compiere");
		this.add(lCompiereHome,           new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fCompiereHome,             new GridBagConstraints(1, 1, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lWebPort,           new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fWebPort,            new GridBagConstraints(1, 3, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lAppsServer,          new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fAppsServer,           new GridBagConstraints(1, 2, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseType,           new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseType,          new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabasePort,          new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseUser,           new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseName,             new GridBagConstraints(1, 6, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabasePort,            new GridBagConstraints(1, 7, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseUser,             new GridBagConstraints(1, 8, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lTNSName,           new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fTNSName,              new GridBagConstraints(3, 5, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lMailServer,          new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lSystemPassword,           new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fSystemPassword,          new GridBagConstraints(3, 7, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabasePassword,           new GridBagConstraints(3, 8, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabasePassword,          new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailServer,          new GridBagConstraints(1, 9, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(bTest,         new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(bSave,          new GridBagConstraints(3, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabaseServer,          new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseServer,         new GridBagConstraints(1, 5, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseName,       new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lJavaHome,      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJavaHome,            new GridBagConstraints(1, 0, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bCompiereHome,     new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bHelp,        new GridBagConstraints(2, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bJavaHome,     new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lJNPPort,     new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJNPPort,      new GridBagConstraints(3, 3, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lMailPassword,      new GridBagConstraints(2, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailPassword,       new GridBagConstraints(3, 10, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lAdminEMail,   new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailUser,   new GridBagConstraints(1, 10, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fAdminEMail,    new GridBagConstraints(3, 9, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lMailUser,  new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 0), 0, 0));

		//
		bCompiereHome.addActionListener(this);
		bJavaHome.addActionListener(this);
		bHelp.addActionListener(this);
		bTest.addActionListener(this);
		bSave.addActionListener(this);
	}	//	jbInit


	/**
	 * 	Dynamic Init
	 *  @throws Exception
	 */
	private void dynInit() throws Exception
	{
		//	Layout - Column Length
		fWebPort.setColumns(30);
		fJNPPort.setColumns(30);

		//	DatabaseType
		fDatabaseType.addItem("Oracle 9i2");

		//	Compiere Home
		String ch = System.getProperty(COMPIERE_HOME);
		if (ch == null || ch.length() == 0)
			ch = System.getProperty("user.dir");
		fCompiereHome.setText(ch);
		boolean envLoaded = false;
		String fileName = ch + File.separator + COMPIERE_ENV_FILE;
		File env = new File (fileName);
		System.out.println(env + " - exists=" + env.exists());
		if (env.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(env);
				m_properties.load(fis);
				fis.close();
			}
			catch (Exception e)
			{
				System.err.println(e.toString());
			}
			System.out.println("Loading ...");
			if (m_properties.size() > 5)
				envLoaded = true;
			//
			fCompiereHome.setText((String)m_properties.get(COMPIERE_HOME));
			fJavaHome.setText((String)m_properties.get(JAVA_HOME));
			fAppsServer.setText((String)m_properties.get(COMPIERE_APPS_SERVER));
			fWebPort.setText((String)m_properties.get(COMPIERE_WEB_PORT));
			fJNPPort.setText((String)m_properties.get(COMPIERE_JNP_PORT));

			fDatabaseServer.setText((String)m_properties.get(COMPIERE_DB_SERVER));
			fDatabasePort.setText((String)m_properties.get(COMPIERE_DB_PORT));
			fDatabaseName.setText((String)m_properties.get(COMPIERE_DB_NAME));

			fDatabaseUser.setText((String)m_properties.get(COMPIERE_DB_USER));
			fDatabasePassword.setText((String)m_properties.get(COMPIERE_DB_PASSWORD));
			fSystemPassword.setText((String)m_properties.get(COMPIERE_DB_SYSTEM));
			fillTNSName((String)m_properties.get(COMPIERE_DB_TNS));

			fMailServer.setText((String)m_properties.get(COMPIERE_MAIL_SERVER));
			fMailUser.setText((String)m_properties.get(COMPIERE_MAIL_USER));
			fMailPassword.setText((String)m_properties.get(COMPIERE_MAIL_PASSWORD));
			fAdminEMail.setText((String)m_properties.get(COMPIERE_ADMIN_EMAIL));
		}

		//	No environment file found - defaults
		if (!envLoaded)
		{
			System.out.println("Setting Defaults");
			//	Java Home, e.g. D:\j2sdk1.4.1\jre
			String javaHome = System.getProperty("java.home");
			if (javaHome.endsWith("jre"))
				javaHome = javaHome.substring(0, javaHome.length()-4);
			fJavaHome.setText(javaHome);
			//	AppsServer
			fAppsServer.setText(InetAddress.getLocalHost().getHostName());
			//	Database Server
			fDatabaseServer.setText(InetAddress.getLocalHost().getHostName());
			//	Mail Server
			fMailServer.setText(InetAddress.getLocalHost().getHostName());
			//  Get TNS Names
			fillTNSName("");
		}	//	!envLoaded

		//	Default FTP stuff
		if (!m_properties.containsKey(COMPIERE_FTP_SERVER))
		{
			m_properties.setProperty(COMPIERE_FTP_SERVER, "localhost");
			m_properties.setProperty(COMPIERE_FTP_USER, "anonymous");
			m_properties.setProperty(COMPIERE_FTP_PASSWORD, "user@host.com");
			m_properties.setProperty(COMPIERE_FTP_PREFIX, "my");
		}
		//	Default Java Options
		if (!m_properties.containsKey(COMPIERE_JAVA_OPTIONS))
			m_properties.setProperty(COMPIERE_JAVA_OPTIONS, "-Xms32m -Xmx128m");

		//	(String)m_properties.get(COMPIERE_DB_URL)	//	derived
	}	//	dynInit

	/**
	 * 	Fill TNS Name
	 * 	@param defaultValue defaultValue
	 */
	private void fillTNSName (String defaultValue)
	{
		//	default value to lowercase or null
		String def = defaultValue;
		if (def != null && def.trim().length() == 0)
			def = null;
		if (def != null)
		{
			def = def.toLowerCase();
			fTNSName.addItem(def);
		}

		//	Search for Oracle Info
		String path = System.getProperty("java.library.path");
		String[] entries = path.split(File.pathSeparator);
		for (int e = 0; e < entries.length; e++)
		{
			String entry = entries[e].toLowerCase();
			if (entry.indexOf("ora") != -1 && entry.endsWith("bin"))
			{
				StringBuffer sb = getTNS_File (entries[e].substring(0, entries[e].length()-4));
				String[] tnsnames = getTNS_Names (sb);
				if (tnsnames != null)
				{
					for (int i = 0; i < tnsnames.length; i++)
					{
						String tns = tnsnames[i];
						if (!tns.equals(def))
							fTNSName.addItem(tns);
					}
					break;
				}
			}
		}	//	for all path entries

		//	Set first entry
		fTNSName.addActionListener(this);
		if (fTNSName.getItemCount() > 0)
			fTNSName.setSelectedIndex(0);
	}	//	fillTNSName

	/**
	 * 	Get File tnmsnames.ora in StringBuffer
	 * 	@param oraHome ORACLE_HOME
	 * 	@return tnsnames.ora or null
	 */
	private StringBuffer getTNS_File (String oraHome)
	{
		String tnsnames = oraHome + File.separator
			+ "network" + File.separator
			+ "admin" + File.separator
			+ "tnsnames.ora";
		File tnsfile = new File (tnsnames);
		if (!tnsfile.exists())
			return null;

		System.out.println("Searching " + tnsnames);
		StringBuffer sb = new StringBuffer();
		try
		{
			FileReader fr = new FileReader (tnsfile);
			int c;
			while ((c = fr.read()) != -1)
				sb.append((char)c);
		}
		catch (IOException ex)
		{
			System.err.println("Error Reading " + tnsnames);
			ex.printStackTrace();
			return null;
		}
		if (sb.length() == 0)
			return null;
		return sb;
	}	//	getTNS_File

	/**
	 * 	Get TNS Names entries.
	 * 	Assumes standard tnsmanes.ora formatting of NetMgr
	 * 	@param tnsnames content of tnsnames.ora
	 * 	@return tns names or null
	 */
	private String[] getTNS_Names (StringBuffer tnsnames)
	{
		if (tnsnames == null)
			return null;

		ArrayList list = new ArrayList();
		Pattern pattern = Pattern.compile("$", Pattern.MULTILINE);
		String[] lines = pattern.split(tnsnames);
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i].trim();
			if (line.length() > 0
				&& Character.isLetter(line.charAt(0))	//	no # (
				&& line.indexOf("=") != -1
				&& line.indexOf("EXTPROC_") == -1
				&& line.indexOf("_HTTP") == -1)
			{
				String entry = line.substring(0, line.indexOf('=')).trim().toLowerCase();
				System.out.println("- " + entry);
				list.add(entry);
			}
		}
		//	Convert to Array
		if (list.size() == 0)
			return null;
		String[] retValue = new String[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getTNS_Names

	/*************************************************************************/

	/**
	 * 	ActionListener
	 *  @param e event
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (m_testing)
			return;
		//	TNS Name Changed
		if (e.getSource() == fTNSName)
		{
			String tns = (String)fTNSName.getSelectedItem();
			if (tns != null)
			{
				int pos = tns.indexOf('.');
				if (pos != -1)
					tns = tns.substring(0, pos);
				fDatabaseName.setText(tns);
			}
		}

		else if (e.getSource() == bJavaHome)
			setPath (fJavaHome);
		else if (e.getSource() == bCompiereHome)
			setPath (fCompiereHome);
		else if (e.getSource() == bHelp)
			new Setup_Help((Frame)SwingUtilities.getWindowAncestor(this));
		else if (e.getSource() == bTest)
			startTest();
		else if (e.getSource() == bSave)
			save();
	}	//	actionPerformed

	/**
	 * 	Set Path in Field
	 * 	@param field field to set Path
	 */
	private void setPath (JTextField field)
	{
		JFileChooser fc = new JFileChooser(field.getText());
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			field.setText(fc.getSelectedFile().getAbsolutePath());
	}	//	setPath

	/*************************************************************************/

	/**
	 * 	Start Test Async.
	 *  @return SwingWorker
	 */
	private SwingWorker startTest()
	{
		SwingWorker worker = new SwingWorker()
		{
			//	Start it
			public Object construct()
			{
				m_testing = true;
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				bTest.setEnabled(false);
				m_success = false;
				m_errorString = null;
				try
				{
					test();
				}
				catch (Exception ex)
				{
					m_errorString = ex.getMessage();
				}
				//
				setCursor(Cursor.getDefaultCursor());
				if (m_errorString == null)
					m_success = true;
				bTest.setEnabled(true);
				m_testing = false;
				return new Boolean(m_success);
			}
			//	Finish it
			public void finished()
			{
				if (m_errorString != null)
				{
					System.err.println(m_errorString);
					JOptionPane.showConfirmDialog (null, m_errorString, res.getString("ServerError"),
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		worker.start();
		return worker;
	}	//	startIt

	/**
	 *	Test it
	 * 	@throws Exception
	 */
	private void test() throws Exception
	{
		System.out.println();
		System.out.println("** Test **");
		bSave.setEnabled(false);


		//	Java Home
		m_errorString = res.getString("ErrorJavaHome");
		m_javaHome = new File (fJavaHome.getText());
		if (!m_javaHome.exists())
			return;
		File tools = new File (fJavaHome.getText() + File.separator + "lib" + File.separator + "tools.jar");
		if (!tools.exists())
		{
			System.err.println("Not Found Java SDK = " + tools);
			return;
		}
		System.out.println("OK: JavaHome=" + m_javaHome.getAbsolutePath());
		m_properties.setProperty(JAVA_HOME, m_javaHome.getAbsolutePath());

		//	Java Version
		final String VERSION = "1.4.1";
		boolean versionOK = false;
		String jh = m_javaHome.getAbsolutePath();
		if (jh.indexOf(VERSION) != -1)	//	file name has version = assuming OK
			versionOK = true;
		String thisJH = System.getProperty("java.home");
		if (thisJH.indexOf(jh) != -1)	//	wre are running the version currently
		{
			String thisJV = System.getProperty("java.version");
			versionOK = thisJV.indexOf(VERSION) != -1;
			if (versionOK)
			  System.out.println("  Java Version OK = " + thisJV);
		}
		if (!versionOK)
		  System.out.println("** Please check Java Version - should be " + VERSION + "*");

		//	Compiere Home
		m_errorString = res.getString("ErrorCompiereHome");
		m_compiereHome = new File (fCompiereHome.getText());
		if (!m_compiereHome.exists())
		{
			System.err.println("Not found CompiereHome = " + m_compiereHome);
			return;
		}
		System.out.println("OK: CompiereHome = " + m_compiereHome);
		m_properties.setProperty(COMPIERE_HOME, m_compiereHome.getAbsolutePath());


		//	AppsServer
		m_errorString = res.getString("ErrorAppsServer");
		String server = fAppsServer.getText();
		if (server == null || server.length() == 0 || server.toLowerCase().indexOf("localhost") != -1)
		{
			System.err.println("Not correct AppsServer = " + m_appsServer);
			return;
		}
		m_appsServer = InetAddress.getByName(server);
		System.out.println("OK: AppsServer = " + m_appsServer);
		m_properties.setProperty(COMPIERE_APPS_SERVER, m_appsServer.getHostName());


		//	Web Port
		m_errorString = res.getString("ErrorWebPort");
		m_statusBar.setText(lWebPort.getText());
		m_WebPort = Integer.parseInt(fWebPort.getText());
		//	HTML Port Use
		if (testPort ("http", m_appsServer.getHostName(), m_WebPort, "/") || !testServerPort(m_WebPort))
		{
			System.err.println("Not correct WebPort = " + m_WebPort);
			return;
		}
		System.out.println("OK: WebPort = " + m_WebPort);
		m_properties.setProperty(COMPIERE_WEB_PORT, String.valueOf(m_WebPort));
		//	Derive SSL Port
		int sslPort = m_WebPort == 80 ? 443 : 8443;
		System.out.println("SSL WebPort = " + sslPort);
		m_properties.setProperty(COMPIERE_SSL_PORT, String.valueOf(sslPort));


		//	JNP Port
		m_errorString = res.getString("ErrorJNPPort");
		m_statusBar.setText(lJNPPort.getText());
		m_JNPPort = Integer.parseInt(fJNPPort.getText());
		//	HTML Port Use
		if (testPort (m_appsServer, m_JNPPort, false) || !testServerPort(m_JNPPort))
		{
			System.err.println("Not correct JNPPort = " + m_JNPPort);
			return;
		}
		System.out.println("OK: JNPPort = " + m_JNPPort);
		m_properties.setProperty(COMPIERE_JNP_PORT, String.valueOf(m_JNPPort));


		//	Database Server
		m_errorString = res.getString("ErrorDatabaseServer");
		m_statusBar.setText(lDatabaseServer.getText());
		server = fDatabaseServer.getText();
		if (server == null || server.length() == 0 || server.toLowerCase().indexOf("localhost") != -1)
		{
			System.err.println("Not correct Database Server = " + server);
			return;
		}
		m_databaseServer = InetAddress.getByName(server);
		System.out.println("OK: Database Server = " + m_databaseServer);
		m_properties.setProperty(COMPIERE_DB_SERVER, m_databaseServer.getHostName());


		//	Database Port
		m_errorString = res.getString("ErrorDatabasePort");
		m_databasePort = Integer.parseInt(fDatabasePort.getText());
		//	Database Port Use
		if (!testPort (m_databaseServer, m_databasePort, true))
		{
			System.err.println("Error Database Port = " + m_databasePort);
			return;
		}
		System.out.println("OK: Database Port = " + m_databasePort);
		m_properties.setProperty(COMPIERE_DB_PORT, String.valueOf(m_databasePort));


		//	JDBC Database Info
		m_errorString = res.getString("ErrorJDBC");
		m_statusBar.setText(lDatabaseName.getText());
		m_databaseName = fDatabaseName.getText();	//	SID
		m_systemPassword = new String(fSystemPassword.getPassword());
		if (!testJDBC("system", m_systemPassword))
		{
			System.err.println("Error Database Name = " + m_databaseName);
			System.err.println("Error Database SystemID = system/" + m_systemPassword);
			return;
		}
		System.out.println("OK: Database Name = " + m_databaseName);
		m_properties.setProperty(COMPIERE_DB_NAME, m_databaseName);
		System.out.println("OK: Database SystemID = system/" + m_systemPassword);
		m_properties.setProperty(COMPIERE_DB_SYSTEM, m_systemPassword);
		//	URL (derived)	jdbc:oracle:thin:@prod1:1521:prod1
		StringBuffer url = new StringBuffer("jdbc:oracle:thin:@")
			.append(m_databaseServer).append(":")
			.append(m_databasePort).append(":").append(m_databaseName);
		System.out.println("OK: Database URL = " + url.toString());
		m_properties.setProperty(COMPIERE_DB_URL, url.toString());


		//	Database User Info
		m_databaseUser = fDatabaseUser.getText();	//	UID
		m_databasePassword = new String(fDatabasePassword.getPassword());	//	PWD
		//	Ignore result as it might not be imported
		if (testJDBC(m_databaseUser, m_databasePassword))
			System.out.println("OK: Database UserID = " + m_databaseUser + "/" + m_databasePassword);
		else
			System.out.println("Not created yet: Database UserID = " + m_databaseUser + "/" + m_databasePassword);
		m_properties.setProperty(COMPIERE_DB_USER, m_databaseUser);
		m_properties.setProperty(COMPIERE_DB_PASSWORD, m_databasePassword);


		//	TNS Name Info
		m_errorString = res.getString("ErrorTNS");
		m_statusBar.setText(lTNSName.getText());
		m_TNSName = (String)fTNSName.getSelectedItem();
		if (!testTNS("system", m_systemPassword))
		{
			System.err.println("Error Database TNS Name = " + m_TNSName);
			return;
		}
		System.out.println("OK: Database TNS Name = " + m_TNSName);
		m_properties.setProperty(COMPIERE_DB_TNS, m_TNSName);


		//	Mail Server
		m_errorString = res.getString("ErrorMailServer");
		m_statusBar.setText(lMailServer.getText());
		server = fMailServer.getText();
		if (server == null || server.length() == 0)
		{
			System.err.println("Error Mail Server = " + server);
			return;
		}
		m_mailServer = InetAddress.getByName(server);
		System.out.println("OK: Mail Server = " + m_mailServer);
		m_properties.setProperty(COMPIERE_MAIL_SERVER, m_mailServer.getHostName());


		//	Mail User
		m_errorString = "ErrorMailUser";
		m_statusBar.setText(lMailUser.getText());
		m_mailUser = fMailUser.getText();
		m_mailPassword = new String(fMailPassword.getPassword());
		m_properties.setProperty(COMPIERE_MAIL_USER, m_mailUser);
		m_properties.setProperty(COMPIERE_MAIL_PASSWORD, m_mailPassword);
		System.out.println("  Mail User = " + m_mailUser + "/" + m_mailPassword);

		//	Mail Address
		m_errorString = res.getString("ErrorMail");
		m_adminEMail = new InternetAddress (fAdminEMail.getText());
		//
		if (testMail())
			System.out.println("OK: Admin EMail = " + m_adminEMail);
		else
			System.out.println("Not verified Admin EMail = " + m_adminEMail);
		m_properties.setProperty(COMPIERE_ADMIN_EMAIL, m_adminEMail.toString());

		//
		m_statusBar.setText(res.getString("Ok"));
		System.out.println("** Test OK **");
		bSave.setEnabled(true);
		m_errorString = null;
	}	//	test


	/**
	 * 	Test Apps Server Port (client perspective)
	 *  @param protocol protocol (http, ..)
	 *  @param server server name
	 *  @param port port
	 *  @param file file name
	 *  @return true if able to connect
	 */
	private boolean testPort (String protocol, String server, int port, String file)
	{
		URL url = null;
		try
		{
			url = new URL (protocol, server, port, file);
		}
		catch (MalformedURLException ex)
		{
			ex.printStackTrace();
			return false;
		}
		System.out.println("  URL=" + url);
		try
		{
			URLConnection c = url.openConnection();
			Object o = c.getContent();
			System.err.println("  URL Connection in use=" + url);	//	error
		}
		catch (Exception ex)
		{
			return false;
		}
		return true;
	}	//	testPort

	/**
	 * 	Test Server Port
	 *  @param port port
	 *  @return true if able to create
	 */
	private boolean testServerPort (int port)
	{
		try
		{
			ServerSocket ss = new ServerSocket (port);
			System.out.println("  ServerPort " + ss.getInetAddress() + ":" + ss.getLocalPort());
			ss.close();
		}
		catch (Exception ex)
		{
			System.err.println(ex);
			return false;
		}
		return true;
	}	//	testPort


	/**
	 * 	Test Port
	 *  @param host host
	 *  @param port port
	 *  @param shouldBenUsed true if it should be used
	 *  @return true if some server answered on port
	 */
	public boolean testPort (InetAddress host, int port, boolean shouldBenUsed)
	{
		Socket pingSocket = null;
		try
		{
			pingSocket = new Socket(host, port);
		}
		catch (Exception e)
		{
			if (shouldBenUsed)
				System.err.println("  Open Socket " + host + " on " + port + ": " + e.getMessage());
			return false;
		}
		if (!shouldBenUsed)
			System.err.println("  Open Socket " + host + " on " + port);
		if (pingSocket == null)
			return false;
		//	success
		try
		{
			pingSocket.close();
		}
		catch (IOException e)
		{
			System.out.println("  CloseSocket=" + e.toString());
		}
		return true;
	}	//	testPort

	/**
	 * 	Test JDBC Connection to Server
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testJDBC (String uid, String pwd)
	{
		//	jdbc:oracle:thin:@dev:1521:dev1
		m_connectionString = "jdbc:oracle:thin:@"
			+ m_databaseServer.getHostName() + ":" + m_databasePort + ":" + m_databaseName;
		System.out.println("  JDBC = " + m_connectionString);

		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(m_connectionString,
				uid, pwd);
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testJDBC

	/**
	 * 	Test TNS Connection
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testTNS (String uid, String pwd)
	{
		String connectionString = "jdbc:oracle:oci8:@" + m_TNSName;
		System.out.println("  TNS = " + connectionString);
		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(connectionString,
				uid, pwd);
		}
		catch (UnsatisfiedLinkError ule)
		{
			System.err.println("Check setup of Oracle Server / Oracle Client / LD_LIBRARY_PATH");
			System.err.println(ule.toString());
			return false;
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testTNS

	/**
	 * 	Test Mail
	 *  @return true of OK
	 */
	private boolean testMail()
	{
		boolean smtpOK = false;
		boolean imapOK = false;
		if (testPort (m_mailServer, 25, true))
		{
			System.out.println("OK: SMTP Server contacted");
			smtpOK = true;
		}
		else
			System.err.println("Error: SMTP Server NOT available");
		//
		if (testPort (m_mailServer, 110, true))
			System.out.println("OK: POP3 Server contacted");
		else
			System.err.println("Error: POP3 Server NOT available");
		if (testPort (m_mailServer, 143, true))
		{
			System.out.println("OK: IMAP4 Server contacted");
			imapOK = true;
		}
		else
			System.err.println("Error: IMAP4 Server NOT available");
		//
		if (!smtpOK)
			return false;
		//
		EMail em = new EMail(m_mailServer.getHostName(),
			m_adminEMail.toString(), m_adminEMail.toString(),
			"Compiere Server Setup Test", "Test: " + m_properties);
		if (EMail.SENT_OK.equals(em.send()))
			System.out.println("OK: Send Test Email to " + m_adminEMail);
		else
			System.err.println("Error: Could NOT send Email to " + m_adminEMail);

		//
		if (!imapOK)
			return false;
		//	Test Mail Access
		Properties props = new Properties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_mailServer.getHostName());
		props.put("mail.user", m_mailUser);
		Session session = Session.getDefaultInstance(props, null);
		//	Connect to Store
		Store store;
		try
		{
			store = session.getStore("imap");
		}
		catch (NoSuchProviderException nsp)
		{
			System.err.println("Error Mail IMAP " + nsp.getMessage());
			return false;
		}
		try
		{
			store.connect(m_mailServer.getHostName(), m_mailUser, m_mailPassword);
			Folder folder = store.getDefaultFolder();
			Folder inbox = folder.getFolder("INBOX");
			System.out.println("OK: Mail Connect to " + inbox.getFullName() + " #Msg=" + inbox.getMessageCount());
			//
			store.close();
		}
		catch (MessagingException mex)
		{
			System.err.println("Error Mail Connect " + mex.getMessage());
			return false;
		}
		return true;
	}	//	testMail

	/*************************************************************************/

	/**
	 * 	Save Settings
	 */
	private void save()
	{
		SwingWorker sw = startTest();
		while (sw.isAlive())
		{
			try
			{
				Thread.currentThread().sleep(2000);
			}
			catch (InterruptedException ex)
			{
				System.err.println("save-waiting: " + ex);
			}
		}
		sw.get();	//	block
		if (!m_success)
			return;

		//	Add
		m_properties.setProperty("COMPIERE_MAIN_VERSION", Compiere.MAIN_VERSION);
		m_properties.setProperty("COMPIERE_DATE_VERSION", Compiere.DATE_VERSION);
		m_properties.setProperty("COMPIERE_DB_VERSION", Compiere.DB_VERSION);

		//	Before we save, load Ini
		Ini.setClient(false);
		String fileName = m_compiereHome.getAbsolutePath() + File.separator + Ini.COMPIERE_PROPERTY_FILE;
		Ini.loadProperties(fileName);

		//	Save Environment
		fileName = m_compiereHome.getAbsolutePath() + File.separator + COMPIERE_ENV_FILE;
		try
		{
			FileOutputStream fos = new FileOutputStream(new File(fileName));
			m_properties.store(fos, COMPIERE_ENV_FILE);
			fos.flush();
			fos.close();
		}
		catch (Exception e)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + e.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (Throwable t)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + t.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		System.out.println("Properties saved to " + fileName);

		//	Sync Properties
		Ini.setCompiereHome(m_compiereHome.getAbsolutePath());
		CConnection cc = CConnection.get (Database.DB_ORACLE,
			m_databaseServer.getHostName(), m_databasePort, m_databaseName,
			m_databaseUser, m_databasePassword);
		cc.setAppsHost(m_appsServer.getHostName());
		Ini.setProperty(Ini.P_CONNECTION, cc.toStringLong());
		Ini.saveProperties(false);


		JOptionPane.showConfirmDialog(this, res.getString("EnvironmentSaved"),
			res.getString("CompiereServerSetup"),
			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

		((Frame)SwingUtilities.getWindowAncestor(this)).dispose();
		System.exit(0);		//	remains active when License Dialog called
	}	//	save

}	//	ConfigurationPanel

/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2002 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.install;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;

import oracle.jdbc.*;

import org.apache.log4j.*;
import org.apache.log4j.Logger;
import org.apache.tools.ant.launch.*;
import org.compiere.*;
import org.compiere.apps.*;
import org.compiere.db.*;
import org.compiere.swing.*;
import org.compiere.util.*;


/**
 *	Configuration Panel
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: ConfigurationPanel.java,v 1.14 2004/09/09 14:19:17 jjanke Exp $
 */
public class ConfigurationPanel extends CPanel implements ActionListener
{
	/**
	 * 	Constructor
	 *  @param statusBar for info
	 * 	@throws Exception
	 */
	public ConfigurationPanel (JLabel statusBar) throws Exception
	{
		LogManager.resetConfiguration();
		Logger root = Logger.getRootLogger();
	//	root.addAppender(new ConsoleAppender(new PatternLayout("%d{ABSOLUTE} %-5p [%c{1}] %m%n")));
		root.addAppender(new ConsoleAppender(new LogLayout()));
		root.setLevel(Level.ALL);
		Log.setTraceLevel(10);
		//
		m_statusBar = statusBar;
		jbInit();
		dynInit();
	}	//	ConfigurationPanel

	public static final String	KEYSTORE_PASSWORD		= "myPassword";
	
	public static final String	COMPIERE_ENV_FILE		= "CompiereEnv.properties";

	public static final String	COMPIERE_HOME 			= "COMPIERE_HOME";
	public static final String	JAVA_HOME 				= "JAVA_HOME";
	public static final String	COMPIERE_JAVA_OPTIONS 	= "COMPIERE_JAVA_OPTIONS";

	public static final String	COMPIERE_APPS_SERVER 	= "COMPIERE_APPS_SERVER";
	public static final String	COMPIERE_JNP_PORT 		= "COMPIERE_JNP_PORT";
	public static final String	COMPIERE_WEB_PORT 		= "COMPIERE_WEB_PORT";
	public static final String	COMPIERE_SSL_PORT 		= "COMPIERE_SSL_PORT";
	public static final String	COMPIERE_WEB_ALIAS 		= "COMPIERE_WEB_ALIAS";
	
	public static final String	COMPIERE_KEYSTORE 		= "COMPIERE_KEYSTORE";
	public static final String	COMPIERE_KEYSTOREPASS	= "COMPIERE_KEYSTOREPASS";
	
	public static final String	COMPIERE_DB_SERVER 		= "COMPIERE_DB_SERVER";
	public static final String	COMPIERE_DB_PORT 		= "COMPIERE_DB_PORT";
	public static final String	COMPIERE_DB_NAME 		= "COMPIERE_DB_NAME";

	public static final String	COMPIERE_DB_USER 		= "COMPIERE_DB_USER";
	public static final String	COMPIERE_DB_PASSWORD 	= "COMPIERE_DB_PASSWORD";
	public static final String	COMPIERE_DB_SYSTEM 		= "COMPIERE_DB_SYSTEM";

	public static final String	COMPIERE_DB_URL 		= "COMPIERE_DB_URL";
	public static final String	COMPIERE_DB_TNS 		= "COMPIERE_DB_TNS";

	public static final String	COMPIERE_MAIL_SERVER 	= "COMPIERE_MAIL_SERVER";
	public static final String	COMPIERE_MAIL_USER 		= "COMPIERE_MAIL_USER";
	public static final String	COMPIERE_MAIL_PASSWORD 	= "COMPIERE_MAIL_PASSWORD";
	public static final String	COMPIERE_ADMIN_EMAIL	= "COMPIERE_ADMIN_EMAIL";

	public static final String	COMPIERE_FTP_SERVER		= "COMPIERE_FTP_SERVER";
	public static final String	COMPIERE_FTP_USER		= "COMPIERE_FTP_USER";
	public static final String	COMPIERE_FTP_PASSWORD	= "COMPIERE_FTP_PASSWORD";
	public static final String	COMPIERE_FTP_PREFIX		= "COMPIERE_FTP_PREFIX";

	private String			m_errorString;
	private File			m_javaHome;
	private File			m_compiereHome;
	private InetAddress		m_appsServer;
	private int				m_WebPort;
	private int				m_JNPPort;
	private InetAddress		m_databaseServer;
	private int				m_databasePort;
	private String			m_databaseName;
	private String			m_connectionString;
	private String			m_systemPassword;
	private String			m_databaseUser;
	private String			m_databasePassword;
	private String			m_TNSName;

	private InetAddress		m_mailServer;
	private InternetAddress	m_adminEMail;
	private String			m_mailUser;
	private String			m_mailPassword;

	private volatile boolean		m_success = false;
	private volatile boolean	 	m_testing = false;

	/** Translation				*/
	static ResourceBundle 		res = ResourceBundle.getBundle("org.compiere.install.SetupRes");

	/**	Driver					*/
	private static Driver		s_driver = null;
	/** Environment Properties	*/
	private Properties			m_properties = new Properties();
	/**	Setup Frame				*/
	private Setup				m_setup = null;
	/** Status Bar				*/
	private JLabel 				m_statusBar;

	private static ImageIcon iOpen = new ImageIcon(ConfigurationPanel.class.getResource("openFile.gif"));
	private static ImageIcon iSave = new ImageIcon(Compiere.class.getResource("images/Save16.gif"));
	private static ImageIcon iHelp = new ImageIcon(Compiere.class.getResource("images/Help16.gif"));


	//	-------------	Static UI
	private GridBagLayout gridBagLayout = new GridBagLayout();
	private CLabel lCompiereHome = new CLabel();
	private CTextField fCompiereHome = new CTextField();
	private CLabel lWebPort = new CLabel();
	private CTextField fWebPort = new CTextField();
	private CLabel lAppsServer = new CLabel();
	private CTextField fAppsServer = new CTextField();
	private CLabel lDatabaseType = new CLabel();
	private CComboBox fDatabaseType = new CComboBox();
	private CLabel lDatabaseName = new CLabel();
	private CLabel lDatabasePort = new CLabel();
	private CLabel lDatabaseUser = new CLabel();
	private CLabel lDatabasePassword = new CLabel();
	private CTextField fDatabaseName = new CTextField();
	private CTextField fDatabasePort = new CTextField();
	private CTextField fDatabaseUser = new CTextField();
	private CPassword fDatabasePassword = new CPassword();
	private CLabel lTNSName = new CLabel();
	private CComboBox fTNSName = new CComboBox();
	private CLabel lSystemPassword = new CLabel();
	private CPassword fSystemPassword = new CPassword();
	private CLabel lMailServer = new CLabel();
	private CTextField fMailServer = new CTextField();
	private CLabel lAdminEMail = new CLabel();
	private CTextField fAdminEMail = new CTextField();
	private CLabel lDatabaseServer = new CLabel();
	private CTextField fDatabaseServer = new CTextField();
	private CLabel lJavaHome = new CLabel();
	private CTextField fJavaHome = new CTextField();
	private CButton bCompiereHome = new CButton(iOpen);
	private CButton bJavaHome = new CButton(iOpen);
	private CButton bHelp = new CButton(iHelp);
	private CButton bTest = new CButton();
	private CButton bSave = new CButton(iSave);
	private CLabel lJNPPort = new CLabel();
	private CTextField fJNPPort = new CTextField();
	private CLabel lMailUser = new CLabel();
	private CLabel lMailPassword = new CLabel();
	private CTextField fMailUser = new CTextField();
	private CPassword fMailPassword = new CPassword();
	private CCheckBox okJavaHome = new CCheckBox();
	private CCheckBox okCompiereHome = new CCheckBox();
	private CCheckBox okAppsServer = new CCheckBox();
	private CCheckBox okWebPort = new CCheckBox();
	private CCheckBox okJNPPort = new CCheckBox();
	private CLabel lSSLPort = new CLabel();
	private CTextField fSSLPort = new CTextField();
	private CCheckBox okSSLPort = new CCheckBox();
	private CCheckBox okDatabaseServer = new CCheckBox();
	private CCheckBox okMailServer = new CCheckBox();
	private CCheckBox okMailUser = new CCheckBox();
	private CCheckBox okDatabaseUser = new CCheckBox();
	private CCheckBox okDatabaseName = new CCheckBox();
	private CLabel lKeyStore = new CLabel();
	private CPassword fKeyStore = new CPassword();
	private CCheckBox okKeyStore = new CCheckBox();
	private CCheckBox okTNSName = new CCheckBox();

	/**
	 * 	Static Layout Init
	 *  @throws Exception
	 */
	private void jbInit() throws Exception
	{
		lCompiereHome.setToolTipText(res.getString("CompiereHomeInfo"));
		lCompiereHome.setText(res.getString("CompiereHome"));
		this.setLayout(gridBagLayout);
		fCompiereHome.setText("C:\\Compiere2");
		lWebPort.setToolTipText(res.getString("WebPortInfo"));
		lWebPort.setText(res.getString("WebPort"));
		fWebPort.setText("80");
		lAppsServer.setToolTipText(res.getString("AppsServerInfo"));
		lAppsServer.setText(res.getString("AppsServer"));
		fAppsServer.setText("server.company.com");
		lDatabaseType.setToolTipText(res.getString("DatabaseTypeInfo"));
		lDatabaseType.setText(res.getString("DatabaseType"));
		lDatabaseName.setToolTipText(res.getString("DatabaseNameInfo"));
		lDatabaseName.setText(res.getString("DatabaseName"));
		lDatabasePort.setToolTipText(res.getString("DatabasePortInfo"));
		lDatabasePort.setText(res.getString("DatabasePort"));
		lDatabaseUser.setToolTipText(res.getString("DatabaseUserInfo"));
		lDatabaseUser.setText(res.getString("DatabaseUser"));
		lDatabasePassword.setToolTipText(res.getString("DatabasePasswordInfo"));
		lDatabasePassword.setText(res.getString("DatabasePassword"));
		fDatabaseName.setText("compiere");
		fDatabasePort.setText("1521");
		fDatabaseUser.setText("compiere");
		fDatabasePassword.setText("compiere");
		lTNSName.setToolTipText(res.getString("TNSNameInfo"));
		lTNSName.setText(res.getString("TNSName"));
		fTNSName.setEditable(true);
		lSystemPassword.setToolTipText(res.getString("SystemPasswordInfo"));
		lSystemPassword.setText(res.getString("SystemPassword"));
		fSystemPassword.setText("manager");
		lMailServer.setToolTipText(res.getString("MailServerInfo"));
		lMailServer.setText(res.getString("MailServer"));
		fMailServer.setText("mail.company.com");
		lAdminEMail.setToolTipText(res.getString("AdminEMailInfo"));
		lAdminEMail.setText(res.getString("AdminEMail"));
		fAdminEMail.setText("admin@company.com");
		bTest.setToolTipText(res.getString("TestInfo"));
		bTest.setText(res.getString("Test"));
		bSave.setToolTipText(res.getString("SaveInfo"));
		bSave.setText(res.getString("Save"));
		lDatabaseServer.setToolTipText(res.getString("DatabaseServerInfo"));
		lDatabaseServer.setText(res.getString("DatabaseServer"));
		lJavaHome.setToolTipText(res.getString("JavaHomeInfo"));
		lJavaHome.setText(res.getString("JavaHome"));
		fJavaHome.setText("Java142");
		lJNPPort.setToolTipText(res.getString("JNPPortInfo"));
		lJNPPort.setText(res.getString("JNPPort"));
		fJNPPort.setText("1099");
		bHelp.setToolTipText(res.getString("HelpInfo"));
		lMailUser.setToolTipText(res.getString("MailUserInfo"));
		lMailUser.setText(res.getString("MailUser"));
		lMailPassword.setToolTipText(res.getString("MailPasswordInfo"));
		lMailPassword.setText(res.getString("MailPassword"));
		fMailUser.setText("compiere");
		fMailPassword.setText("compiere");

		//
		lSSLPort.setText("SSL");
		fSSLPort.setText("443");
		okMailUser.setEnabled(false);
		lKeyStore.setText(res.getString("KeyStorePassword"));
		lKeyStore.setToolTipText(res.getString("KeyStorePasswordInfo"));
		okJavaHome.setEnabled(false);
		okCompiereHome.setEnabled(false);
		okCompiereHome.setDoubleBuffered(false);
		okAppsServer.setEnabled(false);
		okWebPort.setEnabled(false);
		okWebPort.setDoubleBuffered(false);
		okSSLPort.setEnabled(false);
		okJNPPort.setEnabled(false);
		fKeyStore.setText(KEYSTORE_PASSWORD);
		okDatabaseServer.setEnabled(false);
		okTNSName.setEnabled(false);
		okMailServer.setEnabled(false);
		okDatabaseName.setEnabled(false);
		okDatabaseUser.setEnabled(false);
		okKeyStore.setEnabled(false);
		bJavaHome.setMargin(new Insets(2, 10, 2, 10));
		bCompiereHome.setMaximumSize(new Dimension(43, 27));
		bCompiereHome.setMargin(new Insets(2, 10, 2, 10));
		this.add(lCompiereHome,                       new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fCompiereHome,                         new GridBagConstraints(1, 1, 2, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(lWebPort,                       new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fWebPort,                        new GridBagConstraints(1, 3, 4, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(lAppsServer,                      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fAppsServer,                       new GridBagConstraints(1, 2, 3, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(lDatabaseType,                       new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseType,                      new GridBagConstraints(1, 4, 5, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabasePort,                      new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseUser,                       new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseName,                         new GridBagConstraints(1, 6, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabasePort,                        new GridBagConstraints(1, 7, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseUser,                         new GridBagConstraints(1, 8, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lTNSName,                       new GridBagConstraints(7, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(fTNSName,                            new GridBagConstraints(8, 5, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(lMailServer,                      new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lSystemPassword,                       new GridBagConstraints(7, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fSystemPassword,                       new GridBagConstraints(8, 7, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(fDatabasePassword,                        new GridBagConstraints(8, 8, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 0), 0, 0));
		this.add(lDatabasePassword,                      new GridBagConstraints(7, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailServer,                      new GridBagConstraints(1, 9, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(bTest,                      new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 10, 5), 0, 0));
		this.add(bSave,                       new GridBagConstraints(8, 11, 2, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 10, 5), 0, 0));
		this.add(lDatabaseServer,                      new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseServer,                     new GridBagConstraints(1, 5, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseName,                   new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lJavaHome,                  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJavaHome,                        new GridBagConstraints(1, 0, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(bCompiereHome,                 new GridBagConstraints(7, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bHelp,                     new GridBagConstraints(7, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 10, 5), 0, 0));
		this.add(bJavaHome,                 new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lMailPassword,                  new GridBagConstraints(7, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailPassword,                    new GridBagConstraints(8, 10, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 0), 0, 0));
		this.add(lAdminEMail,               new GridBagConstraints(7, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailUser,               new GridBagConstraints(1, 10, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fAdminEMail,                 new GridBagConstraints(8, 9, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 0), 0, 0));
		this.add(lMailUser,              new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 0), 0, 0));
		this.add(lJNPPort,         new GridBagConstraints(7, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJNPPort,         new GridBagConstraints(8, 2, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(okJNPPort,          new GridBagConstraints(9, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(lSSLPort,      new GridBagConstraints(7, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fSSLPort,      new GridBagConstraints(8, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(okSSLPort,     new GridBagConstraints(9, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okDatabaseServer,         new GridBagConstraints(6, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okMailServer,      new GridBagConstraints(6, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 5, 5), 0, 0));
		this.add(okJavaHome,   new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okCompiereHome,  new GridBagConstraints(6, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okAppsServer,  new GridBagConstraints(6, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okWebPort,  new GridBagConstraints(6, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(lKeyStore,  new GridBagConstraints(7, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(fKeyStore,  new GridBagConstraints(8, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(okKeyStore,  new GridBagConstraints(9, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okTNSName, new GridBagConstraints(9, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		//
		bCompiereHome.addActionListener(this);
		bJavaHome.addActionListener(this);
		bHelp.addActionListener(this);
		bTest.addActionListener(this);
		bSave.addActionListener(this);
		this.add(okDatabaseName,  new GridBagConstraints(9, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okDatabaseUser,   new GridBagConstraints(9, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 5, 5), 0, 0));
		this.add(okMailUser,  new GridBagConstraints(9, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
	}	//	jbInit


	/**
	 * 	Dynamic Init
	 *  @throws Exception
	 */
	private void dynInit() throws Exception
	{
		//	DatabaseType
		fDatabaseType.addItem("Oracle 10g (9i2)");

		//	Compiere Home
		String ch = System.getProperty(COMPIERE_HOME);
		if (ch == null || ch.length() == 0)
			ch = System.getProperty("user.dir");
		fCompiereHome.setText(ch);
		boolean envLoaded = false;
		String fileName = ch + File.separator + COMPIERE_ENV_FILE;
		File env = new File (fileName);
		System.out.println(env + " - exists=" + env.exists());
		if (env.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(env);
				m_properties.load(fis);
				fis.close();
			}
			catch (Exception e)
			{
				System.err.println(e.toString());
			}
			System.out.println("Loading ...");
			if (m_properties.size() > 5)
				envLoaded = true;
			//
			fCompiereHome.setText((String)m_properties.get(COMPIERE_HOME));
			fJavaHome.setText((String)m_properties.get(JAVA_HOME));
			fAppsServer.setText((String)m_properties.get(COMPIERE_APPS_SERVER));
			fJNPPort.setText((String)m_properties.get(COMPIERE_JNP_PORT));
			fWebPort.setText((String)m_properties.get(COMPIERE_WEB_PORT));
			fSSLPort.setText((String)m_properties.get(COMPIERE_SSL_PORT));
			String s = (String)m_properties.get(COMPIERE_KEYSTOREPASS);
			if (s == null || s.length() == 0)
				s = KEYSTORE_PASSWORD;
			fKeyStore.setText(s);

			fDatabaseServer.setText((String)m_properties.get(COMPIERE_DB_SERVER));
			fDatabasePort.setText((String)m_properties.get(COMPIERE_DB_PORT));
			fDatabaseName.setText((String)m_properties.get(COMPIERE_DB_NAME));

			fDatabaseUser.setText((String)m_properties.get(COMPIERE_DB_USER));
			fDatabasePassword.setText((String)m_properties.get(COMPIERE_DB_PASSWORD));
			fSystemPassword.setText((String)m_properties.get(COMPIERE_DB_SYSTEM));
			fillTNSName((String)m_properties.get(COMPIERE_DB_TNS));

			fMailServer.setText((String)m_properties.get(COMPIERE_MAIL_SERVER));
			fMailUser.setText((String)m_properties.get(COMPIERE_MAIL_USER));
			fMailPassword.setText((String)m_properties.get(COMPIERE_MAIL_PASSWORD));
			fAdminEMail.setText((String)m_properties.get(COMPIERE_ADMIN_EMAIL));
		}

		//	No environment file found - defaults
		if (!envLoaded)
		{
			System.out.println("Setting Defaults");
			//	Java Home, e.g. D:\j2sdk1.4.1\jre
			String javaHome = System.getProperty("java.home");
			if (javaHome.endsWith("jre"))
				javaHome = javaHome.substring(0, javaHome.length()-4);
			fJavaHome.setText(javaHome);
			//	AppsServer
			fAppsServer.setText(InetAddress.getLocalHost().getHostName());
			//	Database Server
			fDatabaseServer.setText(InetAddress.getLocalHost().getHostName());
			//	Mail Server
			fMailServer.setText(InetAddress.getLocalHost().getHostName());
			//  Get TNS Names
			fillTNSName("");
		}	//	!envLoaded

		//	Default FTP stuff
		if (!m_properties.containsKey(COMPIERE_FTP_SERVER))
		{
			m_properties.setProperty(COMPIERE_FTP_SERVER, "localhost");
			m_properties.setProperty(COMPIERE_FTP_USER, "anonymous");
			m_properties.setProperty(COMPIERE_FTP_PASSWORD, "user@host.com");
			m_properties.setProperty(COMPIERE_FTP_PREFIX, "my");
		}
		//	Default Java Options
		if (!m_properties.containsKey(COMPIERE_JAVA_OPTIONS))
			m_properties.setProperty(COMPIERE_JAVA_OPTIONS, "-Xms64M -Xmx512M");
		//	Web Alias
		if (!m_properties.containsKey(COMPIERE_WEB_ALIAS))
			m_properties.setProperty(COMPIERE_WEB_ALIAS, InetAddress.getLocalHost().getCanonicalHostName());
		
		//	(String)m_properties.get(COMPIERE_DB_URL)	//	derived
	}	//	dynInit

	/**
	 * 	Fill TNS Name
	 * 	@param defaultValue defaultValue
	 */
	private void fillTNSName (String defaultValue)
	{
		//	default value to lowercase or null
		String def = defaultValue;
		if (def != null && def.trim().length() == 0)
			def = null;
		if (def != null)
		{
			def = def.toLowerCase();
			fTNSName.addItem(def);
		}

		//	Search for Oracle Info
		String path = System.getProperty("java.library.path");
		String[] entries = path.split(File.pathSeparator);
		for (int e = 0; e < entries.length; e++)
		{
			String entry = entries[e].toLowerCase();
			if (entry.indexOf("ora") != -1 && entry.endsWith("bin"))
			{
				StringBuffer sb = getTNS_File (entries[e].substring(0, entries[e].length()-4));
				String[] tnsnames = getTNS_Names (sb);
				if (tnsnames != null)
				{
					for (int i = 0; i < tnsnames.length; i++)
					{
						String tns = tnsnames[i];
						if (!tns.equals(def))
							fTNSName.addItem(tns);
					}
					break;
				}
			}
		}	//	for all path entries

		//	Set first entry
		fTNSName.addActionListener(this);
		if (fTNSName.getItemCount() > 0)
			fTNSName.setSelectedIndex(0);
	}	//	fillTNSName

	/**
	 * 	Get File tnmsnames.ora in StringBuffer
	 * 	@param oraHome ORACLE_HOME
	 * 	@return tnsnames.ora or null
	 */
	private StringBuffer getTNS_File (String oraHome)
	{
		String tnsnames = oraHome + File.separator
			+ "network" + File.separator
			+ "admin" + File.separator
			+ "tnsnames.ora";
		File tnsfile = new File (tnsnames);
		if (!tnsfile.exists())
			return null;

		System.out.println("Searching " + tnsnames);
		StringBuffer sb = new StringBuffer();
		try
		{
			FileReader fr = new FileReader (tnsfile);
			int c;
			while ((c = fr.read()) != -1)
				sb.append((char)c);
		}
		catch (IOException ex)
		{
			System.err.println("Error Reading " + tnsnames);
			ex.printStackTrace();
			return null;
		}
		if (sb.length() == 0)
			return null;
		return sb;
	}	//	getTNS_File

	/**
	 * 	Get TNS Names entries.
	 * 	Assumes standard tnsmanes.ora formatting of NetMgr
	 * 	@param tnsnames content of tnsnames.ora
	 * 	@return tns names or null
	 */
	private String[] getTNS_Names (StringBuffer tnsnames)
	{
		if (tnsnames == null)
			return null;

		ArrayList list = new ArrayList();
		Pattern pattern = Pattern.compile("$", Pattern.MULTILINE);
		String[] lines = pattern.split(tnsnames);
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i].trim();
			if (line.length() > 0
				&& Character.isLetter(line.charAt(0))	//	no # (
				&& line.indexOf("=") != -1
				&& line.indexOf("EXTPROC_") == -1
				&& line.indexOf("_HTTP") == -1)
			{
				String entry = line.substring(0, line.indexOf('=')).trim().toLowerCase();
				System.out.println("- " + entry);
				list.add(entry);
			}
		}
		//	Convert to Array
		if (list.size() == 0)
			return null;
		String[] retValue = new String[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getTNS_Names

	
	/**************************************************************************
	 * 	ActionListener
	 *  @param e event
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (m_testing)
			return;
		//	TNS Name Changed
		if (e.getSource() == fTNSName)
		{
			String tns = (String)fTNSName.getSelectedItem();
			if (tns != null)
			{
				int pos = tns.indexOf('.');
				if (pos != -1)
					tns = tns.substring(0, pos);
				fDatabaseName.setText(tns);
			}
		}

		else if (e.getSource() == bJavaHome)
			setPath (fJavaHome);
		else if (e.getSource() == bCompiereHome)
			setPath (fCompiereHome);
		else if (e.getSource() == bHelp)
			new Setup_Help((Frame)SwingUtilities.getWindowAncestor(this));
		else if (e.getSource() == bTest)
			startTest(false);
		else if (e.getSource() == bSave)
			startTest(true);
	}	//	actionPerformed

	/**
	 * 	Set Path in Field
	 * 	@param field field to set Path
	 */
	private void setPath (CTextField field)
	{
		JFileChooser fc = new JFileChooser(field.getText());
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			field.setText(fc.getSelectedFile().getAbsolutePath());
	}	//	setPath

	
	/**************************************************************************
	 * 	Start Test Async.
	 *  @return SwingWorker
	 */
	private SwingWorker startTest(final boolean saveIt)
	{
		SwingWorker worker = new SwingWorker()
		{
			//	Start it
			public Object construct()
			{
				m_testing = true;
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				bTest.setEnabled(false);
				m_success = false;
				m_errorString = null;
				try
				{
					test();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					m_errorString += "\n" + ex.toString();
				}
				//
				setCursor(Cursor.getDefaultCursor());
				if (m_errorString == null)
					m_success = true;
				bTest.setEnabled(true);
				m_testing = false;
				return new Boolean(m_success);
			}
			//	Finish it
			public void finished()
			{
				if (m_errorString != null)
				{
					System.err.println(m_errorString);
					JOptionPane.showConfirmDialog (null, m_errorString, res.getString("ServerError"),
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
				}
				else if (saveIt)
					save();
			}
		};
		worker.start();
		return worker;
	}	//	startIt

	/**
	 *	Test it
	 * 	@throws Exception
	 */
	private void test() throws Exception
	{
		System.out.println();
		System.out.println("** Test **");
		bSave.setEnabled(false);


		//	Java Home
		m_errorString = res.getString("ErrorJavaHome");
		m_javaHome = new File (fJavaHome.getText());
		if (!signalOK(okJavaHome, m_javaHome.exists(), true, "Not found: Java Home"))
			return;
		File tools = new File (fJavaHome.getText() + File.separator + "lib" + File.separator + "tools.jar");
		if (!signalOK(okJavaHome, tools.exists(), true, 
				"Not found: Java SDK = " + tools))
			return;
		System.out.println("OK: JavaHome=" + m_javaHome.getAbsolutePath());
		m_properties.setProperty(JAVA_HOME, m_javaHome.getAbsolutePath());
		System.setProperty(JAVA_HOME, m_javaHome.getAbsolutePath());
		
		
		//	Java Version
		final String VERSION = "1.4.1";
		final String VERSION2 = "1.4.2";
		boolean versionOK = false;
		String jh = m_javaHome.getAbsolutePath();
		if (jh.indexOf(VERSION) != -1)	//	file name has version = assuming OK
			versionOK = true;
		if (!versionOK && jh.indexOf(VERSION2) != -1)	//
			versionOK = true;
		String thisJH = System.getProperty("java.home");
		if (thisJH.indexOf(jh) != -1)	//	we are running the version currently
		{
			String thisJV = System.getProperty("java.version");
			versionOK = thisJV.indexOf(VERSION) != -1;
			if (!versionOK && thisJV.indexOf(VERSION2) != -1)
				versionOK = true;
			if (versionOK)
			  System.out.println("  Java Version OK = " + thisJV);
		}
		if (!signalOK(okJavaHome, versionOK, true, 
				"Wrong Java Version: Should be " + VERSION2))
			return;

		
		//	Compiere Home
		m_errorString = res.getString("ErrorCompiereHome");
		m_compiereHome = new File (fCompiereHome.getText());
		if (!signalOK(okCompiereHome, m_compiereHome.exists(), true, "Not found: CompiereHome = " + m_compiereHome))
			return;
		System.out.println("OK: CompiereHome = " + m_compiereHome);
		m_properties.setProperty(COMPIERE_HOME, m_compiereHome.getAbsolutePath());
		System.setProperty(COMPIERE_HOME, m_compiereHome.getAbsolutePath());
		

		//	AppsServer
		m_errorString = res.getString("ErrorAppsServer");
		String server = fAppsServer.getText();
		boolean error = (server == null || server.length() == 0 
			|| server.toLowerCase().indexOf("localhost") != -1 || server.equals("127.0.0.1"));
		if (!error)
			m_appsServer = InetAddress.getByName(server);
		if (!signalOK(okAppsServer, !error, true, 
				"Not correct: AppsServer = " + server))
			return;
		System.out.println("OK: AppsServer = " + m_appsServer.getHostName());
		m_properties.setProperty(COMPIERE_APPS_SERVER, m_appsServer.getHostName());

		
		//	JNP Port
		m_errorString = res.getString("ErrorJNPPort");
		m_statusBar.setText(lJNPPort.getText());
		m_JNPPort = Integer.parseInt(fJNPPort.getText());
		error = (testPort (m_appsServer, m_JNPPort, false) || !testServerPort(m_JNPPort));
		if (!signalOK(okJNPPort, !error, true, "Not correct: JNP Port = " + m_JNPPort))
			return;
		System.out.println("OK: JNPPort = " + m_JNPPort);
		m_properties.setProperty(COMPIERE_JNP_PORT, String.valueOf(m_JNPPort));


		//	Web Port
		m_errorString = res.getString("ErrorWebPort");
		m_statusBar.setText(lWebPort.getText());
		m_WebPort = Integer.parseInt(fWebPort.getText());
		error = (testPort ("http", m_appsServer.getHostName(), m_WebPort, "/") 
			|| !testServerPort(m_WebPort));
		if (!signalOK(okWebPort, !error, true, 
				"Not correct: Web Port = " + m_WebPort))
			return;
		System.out.println("OK: Web Port = " + m_WebPort);
		m_properties.setProperty(COMPIERE_WEB_PORT, String.valueOf(m_WebPort));
		
		//	SSL Port
		int sslPort = Integer.parseInt(fSSLPort.getText());
		error = (testPort ("https", m_appsServer.getHostName(), sslPort, "/") 
			|| !testServerPort(sslPort));
		if (!signalOK(okSSLPort, !error, true, 
				"Not correct: SSL Port = " + sslPort))
			return;
		System.out.println("OK: SSL Port = " + sslPort);
		m_properties.setProperty(COMPIERE_SSL_PORT, String.valueOf(sslPort));

		
		//	KeyStore
		m_errorString = res.getString("KeyStorePassword");
		String fileName = MyKeyStore.getKeystoreFileName(m_compiereHome.getAbsolutePath());
		m_properties.setProperty(COMPIERE_KEYSTORE, fileName);
		
		//	KeyStore Password
		String pw = new String(fKeyStore.getPassword());
		if (!signalOK(okKeyStore, pw != null && pw.length() > 0, true, 
				"Invalid Key Store Password = " + pw))
			return;
		m_properties.setProperty(COMPIERE_KEYSTOREPASS, pw);
		MyKeyStore ks = new MyKeyStore (fileName, fKeyStore.getPassword());
		String errorString = ks.verify();
		if (!signalOK(okKeyStore, errorString == null, true, errorString))
			return;
		System.out.println("OK: KeyStore = " + fileName);
		
		
		//	Database Server
		m_errorString = res.getString("ErrorDatabaseServer");
		m_statusBar.setText(lDatabaseServer.getText());
		server = fDatabaseServer.getText();
		error = (server == null || server.length() == 0 
			|| server.toLowerCase().indexOf("localhost") != -1 || server.equals("127.0.0.1"));
		if (!error)
			m_databaseServer = InetAddress.getByName(server);
		if (!signalOK(okDatabaseServer, !error, true, 
				"Not correct: DB Server = " + server))
			return;
		System.out.println("OK: Database Server = " + m_databaseServer.getHostName());
		m_properties.setProperty(COMPIERE_DB_SERVER, m_databaseServer.getHostName());


		//	Database Port
		m_errorString = res.getString("ErrorDatabasePort");
		m_databasePort = Integer.parseInt(fDatabasePort.getText());
		if (!signalOK(okDatabaseServer, testPort (m_databaseServer, m_databasePort, true), true, 
				"Not correct: DB Server Port = " + m_databasePort))
			return;
		System.out.println("OK: Database Port = " + m_databasePort);
		m_properties.setProperty(COMPIERE_DB_PORT, String.valueOf(m_databasePort));


		//	JDBC Database Info
		m_errorString = res.getString("ErrorJDBC");
		m_statusBar.setText(lDatabaseName.getText());
		m_databaseName = fDatabaseName.getText();	//	SID
		m_systemPassword = new String(fSystemPassword.getPassword());
		if (!signalOK(okDatabaseName, m_systemPassword != null && m_systemPassword.length() > 0, true, 
				"Invalid Password"))
			return;
		if (!signalOK(okDatabaseName, testJDBC("system", m_systemPassword), true,
				"Error connecting to Database: " + m_databaseName 
				+ " as system/" + m_systemPassword))
			return;
		System.out.println("OK: Database Name = " + m_databaseName);
		m_properties.setProperty(COMPIERE_DB_NAME, m_databaseName);
		System.out.println("OK: Database SystemID = system/" + m_systemPassword);
		m_properties.setProperty(COMPIERE_DB_SYSTEM, m_systemPassword);

		//	URL (derived)	jdbc:oracle:thin:@prod1:1521:prod1
		StringBuffer url = new StringBuffer("jdbc:oracle:thin:@")
			.append(m_databaseServer.getHostName()).append(":")
			.append(m_databasePort).append(":").append(m_databaseName);
		System.out.println("OK: Database URL = " + url.toString());
		m_properties.setProperty(COMPIERE_DB_URL, url.toString());


		//	Database User Info
		m_databaseUser = fDatabaseUser.getText();	//	UID
		m_databasePassword = new String(fDatabasePassword.getPassword());	//	PWD
		if (!signalOK(okDatabaseUser, m_databasePassword != null && m_databasePassword.length() > 0, true, 
				"Invalid Password"))
			return;
		//	Ignore result as it might not be imported
		if (signalOK(okDatabaseUser, testJDBC(m_databaseUser, m_databasePassword), false,
				"Not created yet for Database: " + m_databaseName 
				+ " User: " + m_databaseUser + "/" + m_databasePassword))
			System.out.println("OK: Database UserID = " + m_databaseUser + "/" + m_databasePassword);
		m_properties.setProperty(COMPIERE_DB_USER, m_databaseUser);
		m_properties.setProperty(COMPIERE_DB_PASSWORD, m_databasePassword);


		//	TNS Name Info
		m_errorString = res.getString("ErrorTNS");
		m_statusBar.setText(lTNSName.getText());
		m_TNSName = (String)fTNSName.getSelectedItem();
		signalOK(okTNSName, testTNS("system", m_systemPassword), true,
			"Error connecting to Database: " + m_databaseName 
			+ " via TNS Name: " + m_TNSName);
		System.out.println("OK: Database TNS Name = " + m_TNSName);
		m_properties.setProperty(COMPIERE_DB_TNS, m_TNSName);


		//	Mail Server
		m_errorString = res.getString("ErrorMailServer");
		m_statusBar.setText(lMailServer.getText());
		server = fMailServer.getText();
		error = (server == null || server.length() == 0 
				|| server.toLowerCase().indexOf("localhost") != -1 || server.equals("127.0.0.1"));
		if (!error)
			m_mailServer = InetAddress.getByName(server);
		if (!signalOK(okMailServer, !error, true,
				"Error Mail Server = " + server))
			return;
		System.out.println("OK: Mail Server = " + m_mailServer);
		m_properties.setProperty(COMPIERE_MAIL_SERVER, m_mailServer.getHostName());


		//	Mail User
		m_errorString = "ErrorMailUser";
		m_statusBar.setText(lMailUser.getText());
		m_mailUser = fMailUser.getText();
		m_mailPassword = new String(fMailPassword.getPassword());
		m_properties.setProperty(COMPIERE_MAIL_USER, m_mailUser);
		m_properties.setProperty(COMPIERE_MAIL_PASSWORD, m_mailPassword);
		System.out.println("  Mail User = " + m_mailUser + "/" + m_mailPassword);

		//	Mail Address
		m_errorString = res.getString("ErrorMail");
		m_adminEMail = new InternetAddress (fAdminEMail.getText());
		//
		if (signalOK(okMailUser, testMail(), false, 
				"Not verified Admin EMail = " + m_adminEMail))
			System.out.println("OK: Admin EMail = " + m_adminEMail);
		m_properties.setProperty(COMPIERE_ADMIN_EMAIL, m_adminEMail.toString());

		//
		m_statusBar.setText(res.getString("Ok"));
		System.out.println("** Test OK **");
		bSave.setEnabled(true);
		m_errorString = null;
	}	//	test

	/**
	 * 	Signal OK
	 *	@param cb ckeck box
	 *	@param pass trus if test passed
	 *	@param critical true if critial
	 *	@param srrorMsg error Message
	 *	@return pass
	 */
	private boolean signalOK (CCheckBox cb, boolean pass, boolean critical, String errorMsg)
	{
		cb.setSelected(pass);
		if (pass)
			cb.setToolTipText(null);
		else
		{
			cb.setToolTipText(errorMsg);
			if (critical)
				System.err.println(errorMsg);
			else
				System.out.println(errorMsg);
			m_errorString += " \n(" + errorMsg + ")";
		}
		if (!pass && critical)
			cb.setBackground(Color.RED);
		else
			cb.setBackground(Color.GREEN);
		return pass;
	}	//	setOK

	/**
	 * 	Test Apps Server Port (client perspective)
	 *  @param protocol protocol (http, ..)
	 *  @param server server name
	 *  @param port port
	 *  @param file file name
	 *  @return true if able to connect
	 */
	private boolean testPort (String protocol, String server, int port, String file)
	{
		URL url = null;
		try
		{
			url = new URL (protocol, server, port, file);
		}
		catch (MalformedURLException ex)
		{
			ex.printStackTrace();
			return false;
		}
		System.out.println("  URL=" + url);
		try
		{
			URLConnection c = url.openConnection();
			Object o = c.getContent();
			System.err.println("  URL Connection in use=" + url);	//	error
		}
		catch (Exception ex)
		{
			return false;
		}
		return true;
	}	//	testPort

	/**
	 * 	Test Server Port
	 *  @param port port
	 *  @return true if able to create
	 */
	private boolean testServerPort (int port)
	{
		try
		{
			ServerSocket ss = new ServerSocket (port);
			System.out.println("  ServerPort " + ss.getInetAddress() + ":" + ss.getLocalPort());
			ss.close();
		}
		catch (Exception ex)
		{
			System.err.println(ex);
			return false;
		}
		return true;
	}	//	testPort


	/**
	 * 	Test Port
	 *  @param host host
	 *  @param port port
	 *  @param shouldBeUsed true if it should be used
	 *  @return true if some server answered on port
	 */
	public boolean testPort (InetAddress host, int port, boolean shouldBeUsed)
	{
		Socket pingSocket = null;
		try
		{
			pingSocket = new Socket(host, port);
		}
		catch (Exception e)
		{
			if (shouldBeUsed)
				System.err.println("  Open Socket " + host + " on " + port + ": " + e.getMessage());
			return false;
		}
		if (!shouldBeUsed)
			System.err.println("  Open Socket " + host + " on " + port);
		if (pingSocket == null)
			return false;
		//	success
		try
		{
			pingSocket.close();
		}
		catch (IOException e)
		{
			System.out.println("  CloseSocket=" + e.toString());
		}
		return true;
	}	//	testPort

	/**
	 * 	Test JDBC Connection to Server
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testJDBC (String uid, String pwd)
	{
		//	jdbc:oracle:thin:@dev:1521:dev1
		m_connectionString = "jdbc:oracle:thin:@"
			+ m_databaseServer.getHostName() + ":" + m_databasePort + ":" + m_databaseName;
		System.out.println("  JDBC = " + m_connectionString);

		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(m_connectionString,
				uid, pwd);
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testJDBC

	/**
	 * 	Test TNS Connection
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testTNS (String uid, String pwd)
	{
		String sqlplus = "sqlplus " + uid + "/" + pwd + "@" + m_TNSName
			+ " @utils/oracle/Test.sql";
		System.out.println("  SQL = " + sqlplus);
		int result = -1;
		try
		{
			Process p = Runtime.getRuntime().exec (sqlplus);
			InputStream in = p.getInputStream();
			int c;
			while ((c = in.read()) != -1)
				System.out.print((char)c);
			in.close();
			in = p.getErrorStream();
			while ((c = in.read()) != -1)
				System.err.print((char)c);
			in.close();
			//	Get result
			try
			{
				Thread.yield();
				result = p.exitValue();
			}
			catch (Exception e)		//	Timing issue on Solaris.
			{
				Thread.sleep(200);	//	.2 sec
				result = p.exitValue();
			}
		}
		catch (Exception ex)
		{
			System.err.println(ex.toString());
		}
		if (result != 0)
			return false;

		
		//	Test OCI Driver
		String connectionString = "jdbc:oracle:oci8:@" + m_TNSName;
		System.out.println("  TNS = " + connectionString);
		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(connectionString,
				uid, pwd);
		}
		catch (UnsatisfiedLinkError ule)
		{
			System.err.println("Check [ORACLE_HOME]/jdbc/Readme.txt for OCI driver setup");
			System.err.println(ule.toString());
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
		}
		return true;
	}	//	testTNS

	/**
	 * 	Test Mail
	 *  @return true of OK
	 */
	private boolean testMail()
	{
		boolean smtpOK = false;
		boolean imapOK = false;
		if (testPort (m_mailServer, 25, true))
		{
			System.out.println("OK: SMTP Server contacted");
			smtpOK = true;
		}
		else
			System.err.println("Error: SMTP Server NOT available");
		//
		if (testPort (m_mailServer, 110, true))
			System.out.println("OK: POP3 Server contacted");
		else
			System.err.println("Error: POP3 Server NOT available");
		if (testPort (m_mailServer, 143, true))
		{
			System.out.println("OK: IMAP4 Server contacted");
			imapOK = true;
		}
		else
			System.err.println("Error: IMAP4 Server NOT available");
		//
		if (!smtpOK)
		{
			signalOK (okMailServer, false, false, "No active Mail Server");
			return false;
		}
		//
		try
		{
			EMail em = new EMail (m_mailServer.getHostName (),
					   m_adminEMail.toString (), m_adminEMail.toString (),
					   "Compiere Server Setup Test", "Test: " + m_properties);
			em.setEMailUser (m_mailUser, m_mailPassword);
			if (EMail.SENT_OK.equals (em.send ()))
			{
				System.out.println ("OK: Send Test Email to " + m_adminEMail);
			}
			else
			{
				System.err.println ("Error: Could NOT send Email to "
					+ m_adminEMail);
			}
		}
		catch (Exception ex)
		{
			System.err.println("testMail - " + ex.getLocalizedMessage());
			return false;
		}

		//
		if (!imapOK)
			return false;

		//	Test Read Mail Access
		Properties props = new Properties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_mailServer.getHostName());
		props.put("mail.user", m_mailUser);
		props.put("mail.smtp.auth", "true");
		System.out.println("  Connecting to " + m_mailServer.getHostName());
		//
		Session session = null;
		Store store = null;
		try
		{
			EMailAuthenticator auth = new EMailAuthenticator (m_mailUser, m_mailPassword);
			session = Session.getDefaultInstance(props, auth);
			session.setDebug (Log.isTraceLevel (10));
			System.out.println("  Session=" + session);
			//	Connect to Store
			store = session.getStore("imap");
			System.out.println("  Store=" + store);
		}
		catch (NoSuchProviderException nsp)
		{
			System.err.println("Error Mail IMAP Provider - " + nsp.getMessage());
			return false;
		}
		catch (Exception e)
		{
			System.err.println("Error Mail IMAP - " + e.getMessage());
			return false;
		}
		try
		{
			store.connect(m_mailServer.getHostName(), m_mailUser, m_mailPassword);
			System.out.println("  Store - connected");
			Folder folder = store.getDefaultFolder();
			Folder inbox = folder.getFolder("INBOX");
			System.out.println("OK: Mail Connect to " + inbox.getFullName() + " #Msg=" + inbox.getMessageCount());
			//
			store.close();
		}
		catch (MessagingException mex)
		{
			System.err.println("Error Mail Connect " + mex.getMessage());
			return false;
		}
		return true;
	}	//	testMail

	/**************************************************************************
	 * 	Save Settings.
	 * 	Called from startTest.finished()
	 */
	private void save()
	{
		if (!m_success)
			return;

		bSave.setEnabled(false);
		bTest.setEnabled(false);
		
		//	Add
		m_properties.setProperty("COMPIERE_MAIN_VERSION", Compiere.MAIN_VERSION);
		m_properties.setProperty("COMPIERE_DATE_VERSION", Compiere.DATE_VERSION);
		m_properties.setProperty("COMPIERE_DB_VERSION", Compiere.DB_VERSION);



		//	Before we save, load Ini
		Ini.setClient(false);
		String fileName = m_compiereHome.getAbsolutePath() + File.separator + Ini.COMPIERE_PROPERTY_FILE;
		Ini.loadProperties(fileName);

		//	Save Environment
		fileName = m_compiereHome.getAbsolutePath() + File.separator + COMPIERE_ENV_FILE;
		try
		{
			FileOutputStream fos = new FileOutputStream(new File(fileName));
			m_properties.store(fos, COMPIERE_ENV_FILE);
			fos.flush();
			fos.close();
		}
		catch (Exception e)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + e.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (Throwable t)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + t.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		System.out.println("Properties saved to " + fileName);

		//	Sync Properties
		Ini.setCompiereHome(m_compiereHome.getAbsolutePath());
		CConnection cc = CConnection.get (Database.DB_ORACLE,
			m_databaseServer.getHostName(), m_databasePort, m_databaseName,
			m_databaseUser, m_databasePassword);
		cc.setAppsHost(m_appsServer.getHostName());
		cc.setRMIoverHTTP(false);
		Ini.setProperty(Ini.P_CONNECTION, cc.toStringLong());
		Ini.saveProperties(false);
		
		//	Final Info
		JOptionPane.showConfirmDialog(this, res.getString("EnvironmentSaved"),
			res.getString("CompiereServerSetup"),
			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

		//	Run Ant
		try
		{
			System.out.println("Starting Ant ... ");
			System.setProperty("ant.home", ".");
			String[] 	args = new String[] {"setup"};
			Launcher.main (args);	//	calls System.exit
		}
		catch (Exception e)
		{
		}
			
		//	To be sure
		((Frame)SwingUtilities.getWindowAncestor(this)).dispose();
		System.exit(0);		//	remains active when License Dialog called
	}	//	save

}	//	ConfigurationPanel
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2002 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.install;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;

import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;

import oracle.jdbc.*;
import org.compiere.*;
import org.compiere.apps.*;
import org.compiere.db.*;
import org.compiere.util.*;


/**
 *	Configuration Panel
 *
 * 	@author 	Jorg Janke
 * 	@author 	Marek Mosiewicz<marek.mosiewicz@jotel.com.pl> support for RMI over HTTP - now reimplemented/removed jj
 * 	@version 	$Id: ConfigurationPanel.java,v 1.8 2003/09/30 14:32:04 jjanke Exp $
 */
public class ConfigurationPanel extends JPanel implements ActionListener
{
	/**
	 * 	Constructor
	 *  @param statusBar for info
	 * 	@throws Exception
	 */
	public ConfigurationPanel (JLabel statusBar) throws Exception
	{
		LogManager.resetConfiguration();
		Logger root = Logger.getRootLogger();
	//	root.addAppender(new ConsoleAppender(new PatternLayout("%d{ABSOLUTE} %-5p [%c{1}] %m%n")));
		root.addAppender(new ConsoleAppender(new LogLayout()));
		root.setLevel(Level.ALL);
		Log.setTraceLevel(10);

		m_statusBar = statusBar;
		jbInit();
		dynInit();
	}	//	ConfigurationPanel

	public static final String	COMPIERE_ENV_FILE		= "CompiereEnv.properties";

	public static final String	COMPIERE_HOME 			= "COMPIERE_HOME";
	public static final String	JAVA_HOME 				= "JAVA_HOME";
	public static final String	COMPIERE_JAVA_OPTIONS 	= "COMPIERE_JAVA_OPTIONS";

	public static final String	COMPIERE_APPS_SERVER 	= "COMPIERE_APPS_SERVER";
	public static final String	COMPIERE_JNP_PORT 		= "COMPIERE_JNP_PORT";
	public static final String	COMPIERE_WEB_PORT 		= "COMPIERE_WEB_PORT";
	public static final String	COMPIERE_SSL_PORT 		= "COMPIERE_SSL_PORT";

	public static final String	COMPIERE_DB_SERVER 		= "COMPIERE_DB_SERVER";
	public static final String	COMPIERE_DB_PORT 		= "COMPIERE_DB_PORT";
	public static final String	COMPIERE_DB_NAME 		= "COMPIERE_DB_NAME";

	public static final String	COMPIERE_DB_USER 		= "COMPIERE_DB_USER";
	public static final String	COMPIERE_DB_PASSWORD 	= "COMPIERE_DB_PASSWORD";
	public static final String	COMPIERE_DB_SYSTEM 		= "COMPIERE_DB_SYSTEM";

	public static final String	COMPIERE_DB_URL 		= "COMPIERE_DB_URL";
	public static final String	COMPIERE_DB_TNS 		= "COMPIERE_DB_TNS";

	public static final String	COMPIERE_MAIL_SERVER 	= "COMPIERE_MAIL_SERVER";
	public static final String	COMPIERE_MAIL_USER 		= "COMPIERE_MAIL_USER";
	public static final String	COMPIERE_MAIL_PASSWORD 	= "COMPIERE_MAIL_PASSWORD";
	public static final String	COMPIERE_ADMIN_EMAIL	= "COMPIERE_ADMIN_EMAIL";

	public static final String	COMPIERE_FTP_SERVER		= "COMPIERE_FTP_SERVER";
	public static final String	COMPIERE_FTP_USER		= "COMPIERE_FTP_USER";
	public static final String	COMPIERE_FTP_PASSWORD	= "COMPIERE_FTP_PASSWORD";
	public static final String	COMPIERE_FTP_PREFIX		= "COMPIERE_FTP_PREFIX";

	private String			m_errorString;
	private File			m_javaHome;
	private File			m_compiereHome;
	private InetAddress		m_appsServer;
	private int				m_WebPort;
	private int				m_JNPPort;
	private InetAddress		m_databaseServer;
	private int				m_databasePort;
	private String			m_databaseName;
	private String			m_connectionString;
	private String			m_systemPassword;
	private String			m_databaseUser;
	private String			m_databasePassword;
	private String			m_TNSName;

	private InetAddress		m_mailServer;
	private InternetAddress	m_adminEMail;
	private String			m_mailUser;
	private String			m_mailPassword;

	private volatile boolean		m_success = false;
	private volatile boolean	 	m_testing = false;

	/** Translation				*/
	static ResourceBundle 		res = ResourceBundle.getBundle("org.compiere.install.SetupRes");

	/**	Driver					*/
	private static Driver		s_driver = null;
	/** Environment Properties	*/
	private Properties			m_properties = new Properties();
	/** Status Bar				*/
	private JLabel 				m_statusBar;

	private static ImageIcon iOpen = new ImageIcon(ConfigurationPanel.class.getResource("openFile.gif"));
	private static ImageIcon iSave = new ImageIcon(Compiere.class.getResource("images/Save16.gif"));
	private static ImageIcon iHelp = new ImageIcon(Compiere.class.getResource("images/Help16.gif"));


	//	-------------	Static UI
	private GridBagLayout gridBagLayout = new GridBagLayout();
	private JLabel lCompiereHome = new JLabel();
	private JTextField fCompiereHome = new JTextField();
	private JLabel lWebPort = new JLabel();
	private JTextField fWebPort = new JTextField();
	private JLabel lAppsServer = new JLabel();
	private JTextField fAppsServer = new JTextField();
	private JLabel lDatabaseType = new JLabel();
	private JComboBox fDatabaseType = new JComboBox();
	private JLabel lDatabaseName = new JLabel();
	private JLabel lDatabasePort = new JLabel();
	private JLabel lDatabaseUser = new JLabel();
	private JLabel lDatabasePassword = new JLabel();
	private JTextField fDatabaseName = new JTextField();
	private JTextField fDatabasePort = new JTextField();
	private JTextField fDatabaseUser = new JTextField();
	private JPasswordField fDatabasePassword = new JPasswordField();
	private JLabel lTNSName = new JLabel();
	private JComboBox fTNSName = new JComboBox();
	private JLabel lSystemPassword = new JLabel();
	private JPasswordField fSystemPassword = new JPasswordField();
	private JLabel lMailServer = new JLabel();
	private JTextField fMailServer = new JTextField();
	private JLabel lAdminEMail = new JLabel();
	private JTextField fAdminEMail = new JTextField();
	private JLabel lDatabaseServer = new JLabel();
	private JTextField fDatabaseServer = new JTextField();
	private JLabel lJavaHome = new JLabel();
	private JTextField fJavaHome = new JTextField();
	private JButton bCompiereHome = new JButton(iOpen);
	private JButton bJavaHome = new JButton(iOpen);
	private JButton bHelp = new JButton(iHelp);
	private JButton bTest = new JButton();
	private JButton bSave = new JButton(iSave);
	private JLabel lJNPPort = new JLabel();
	private JTextField fJNPPort = new JTextField();
	private JLabel lMailUser = new JLabel();
	private JLabel lMailPassword = new JLabel();
	private JTextField fMailUser = new JTextField();
	private JPasswordField fMailPassword = new JPasswordField();


	/**
	 * 	Static Layout Init
	 *  @throws Exception
	 */
	private void jbInit() throws Exception
	{
		lCompiereHome.setToolTipText(res.getString("CompiereHomeInfo"));
		lCompiereHome.setText(res.getString("CompiereHome"));
		this.setLayout(gridBagLayout);
		fCompiereHome.setText("D:\\Compiere2");
		lWebPort.setToolTipText(res.getString("WebPortInfo"));
		lWebPort.setText(res.getString("WebPort"));
		fWebPort.setText("80");
		lAppsServer.setToolTipText(res.getString("AppsServerInfo"));
		lAppsServer.setText(res.getString("AppsServer"));
		fAppsServer.setText("server.company.com");
		lDatabaseType.setToolTipText(res.getString("DatabaseTypeInfo"));
		lDatabaseType.setText(res.getString("DatabaseType"));
		lDatabaseName.setToolTipText(res.getString("DatabaseNameInfo"));
		lDatabaseName.setText(res.getString("DatabaseName"));
		lDatabasePort.setToolTipText(res.getString("DatabasePortInfo"));
		lDatabasePort.setText(res.getString("DatabasePort"));
		lDatabaseUser.setToolTipText(res.getString("DatabaseUserInfo"));
		lDatabaseUser.setText(res.getString("DatabaseUser"));
		lDatabasePassword.setToolTipText(res.getString("DatabasePasswordInfo"));
		lDatabasePassword.setText(res.getString("DatabasePassword"));
		fDatabaseName.setText("compiere");
		fDatabasePort.setText("1521");
		fDatabaseUser.setText("compiere");
		fDatabasePassword.setText("compiere");
		lTNSName.setToolTipText(res.getString("TNSNameInfo"));
		lTNSName.setText(res.getString("TNSName"));
		fTNSName.setEditable(true);
		lSystemPassword.setToolTipText(res.getString("SystemPasswordInfo"));
		lSystemPassword.setText(res.getString("SystemPassword"));
		fSystemPassword.setText("manager");
		lMailServer.setToolTipText(res.getString("MailServerInfo"));
		lMailServer.setText(res.getString("MailServer"));
		fMailServer.setText("mail.company.com");
		lAdminEMail.setToolTipText(res.getString("AdminEMailInfo"));
		lAdminEMail.setText(res.getString("AdminEMail"));
		fAdminEMail.setText("admin@company.com");
		bTest.setToolTipText(res.getString("TestInfo"));
		bTest.setText(res.getString("Test"));
		bSave.setToolTipText(res.getString("SaveInfo"));
		bSave.setText(res.getString("Save"));
		lDatabaseServer.setToolTipText(res.getString("DatabaseServerInfo"));
		lDatabaseServer.setText(res.getString("DatabaseServer"));
		lJavaHome.setToolTipText(res.getString("JavaHomeInfo"));
		lJavaHome.setText(res.getString("JavaHome"));
		fJavaHome.setText("Java141");
		lJNPPort.setToolTipText(res.getString("JNPPortInfo"));
		lJNPPort.setText(res.getString("JNPPort"));
		fJNPPort.setText("1099");
		bHelp.setToolTipText(res.getString("HelpInfo"));
		lMailUser.setToolTipText(res.getString("MailUserInfo"));
		lMailUser.setText(res.getString("MailUser"));
		lMailPassword.setToolTipText(res.getString("MailPasswordInfo"));
		lMailPassword.setText(res.getString("MailPassword"));
		fMailUser.setText("compiere");
		fMailPassword.setText("compiere");

		//
		this.add(lCompiereHome,           new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fCompiereHome,             new GridBagConstraints(1, 1, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lWebPort,           new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fWebPort,            new GridBagConstraints(1, 3, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lAppsServer,          new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fAppsServer,           new GridBagConstraints(1, 2, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseType,           new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseType,          new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabasePort,          new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseUser,           new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseName,             new GridBagConstraints(1, 6, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabasePort,            new GridBagConstraints(1, 7, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseUser,             new GridBagConstraints(1, 8, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lTNSName,           new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fTNSName,              new GridBagConstraints(3, 5, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lMailServer,          new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lSystemPassword,           new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fSystemPassword,          new GridBagConstraints(3, 7, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabasePassword,           new GridBagConstraints(3, 8, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabasePassword,          new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailServer,          new GridBagConstraints(1, 9, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(bTest,         new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(bSave,          new GridBagConstraints(3, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabaseServer,          new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseServer,         new GridBagConstraints(1, 5, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseName,       new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lJavaHome,      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJavaHome,            new GridBagConstraints(1, 0, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bCompiereHome,     new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bHelp,        new GridBagConstraints(2, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bJavaHome,     new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lJNPPort,     new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJNPPort,      new GridBagConstraints(3, 3, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lMailPassword,      new GridBagConstraints(2, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailPassword,       new GridBagConstraints(3, 10, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lAdminEMail,   new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailUser,   new GridBagConstraints(1, 10, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fAdminEMail,    new GridBagConstraints(3, 9, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lMailUser,  new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 0), 0, 0));
		//
		bCompiereHome.addActionListener(this);
		bJavaHome.addActionListener(this);
		bHelp.addActionListener(this);
		bTest.addActionListener(this);
		bSave.addActionListener(this);
	}	//	jbInit


	/**
	 * 	Dynamic Init
	 *  @throws Exception
	 */
	private void dynInit() throws Exception
	{
		//	Layout - Column Length
		fWebPort.setColumns(30);
		fJNPPort.setColumns(30);

		//	DatabaseType
		fDatabaseType.addItem("Oracle 9i2");

		//	Compiere Home
		String ch = System.getProperty(COMPIERE_HOME);
		if (ch == null || ch.length() == 0)
			ch = System.getProperty("user.dir");
		fCompiereHome.setText(ch);
		boolean envLoaded = false;
		String fileName = ch + File.separator + COMPIERE_ENV_FILE;
		File env = new File (fileName);
		System.out.println(env + " - exists=" + env.exists());
		if (env.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(env);
				m_properties.load(fis);
				fis.close();
			}
			catch (Exception e)
			{
				System.err.println(e.toString());
			}
			System.out.println("Loading ...");
			if (m_properties.size() > 5)
				envLoaded = true;
			//
			fCompiereHome.setText((String)m_properties.get(COMPIERE_HOME));
			fJavaHome.setText((String)m_properties.get(JAVA_HOME));
			fAppsServer.setText((String)m_properties.get(COMPIERE_APPS_SERVER));
			fWebPort.setText((String)m_properties.get(COMPIERE_WEB_PORT));
			fJNPPort.setText((String)m_properties.get(COMPIERE_JNP_PORT));

			fDatabaseServer.setText((String)m_properties.get(COMPIERE_DB_SERVER));
			fDatabasePort.setText((String)m_properties.get(COMPIERE_DB_PORT));
			fDatabaseName.setText((String)m_properties.get(COMPIERE_DB_NAME));

			fDatabaseUser.setText((String)m_properties.get(COMPIERE_DB_USER));
			fDatabasePassword.setText((String)m_properties.get(COMPIERE_DB_PASSWORD));
			fSystemPassword.setText((String)m_properties.get(COMPIERE_DB_SYSTEM));
			fillTNSName((String)m_properties.get(COMPIERE_DB_TNS));

			fMailServer.setText((String)m_properties.get(COMPIERE_MAIL_SERVER));
			fMailUser.setText((String)m_properties.get(COMPIERE_MAIL_USER));
			fMailPassword.setText((String)m_properties.get(COMPIERE_MAIL_PASSWORD));
			fAdminEMail.setText((String)m_properties.get(COMPIERE_ADMIN_EMAIL));
		}

		//	No environment file found - defaults
		if (!envLoaded)
		{
			System.out.println("Setting Defaults");
			//	Java Home, e.g. D:\j2sdk1.4.1\jre
			String javaHome = System.getProperty("java.home");
			if (javaHome.endsWith("jre"))
				javaHome = javaHome.substring(0, javaHome.length()-4);
			fJavaHome.setText(javaHome);
			//	AppsServer
			fAppsServer.setText(InetAddress.getLocalHost().getHostName());
			//	Database Server
			fDatabaseServer.setText(InetAddress.getLocalHost().getHostName());
			//	Mail Server
			fMailServer.setText(InetAddress.getLocalHost().getHostName());
			//  Get TNS Names
			fillTNSName("");
		}	//	!envLoaded

		//	Default FTP stuff
		if (!m_properties.containsKey(COMPIERE_FTP_SERVER))
		{
			m_properties.setProperty(COMPIERE_FTP_SERVER, "localhost");
			m_properties.setProperty(COMPIERE_FTP_USER, "anonymous");
			m_properties.setProperty(COMPIERE_FTP_PASSWORD, "user@host.com");
			m_properties.setProperty(COMPIERE_FTP_PREFIX, "my");
		}
		//	Default Java Options
		if (!m_properties.containsKey(COMPIERE_JAVA_OPTIONS))
			m_properties.setProperty(COMPIERE_JAVA_OPTIONS, "-Xms32m -Xmx128m");

		//	(String)m_properties.get(COMPIERE_DB_URL)	//	derived
	}	//	dynInit

	/**
	 * 	Fill TNS Name
	 * 	@param defaultValue defaultValue
	 */
	private void fillTNSName (String defaultValue)
	{
		//	default value to lowercase or null
		String def = defaultValue;
		if (def != null && def.trim().length() == 0)
			def = null;
		if (def != null)
		{
			def = def.toLowerCase();
			fTNSName.addItem(def);
		}

		//	Search for Oracle Info
		String path = System.getProperty("java.library.path");
		String[] entries = path.split(File.pathSeparator);
		for (int e = 0; e < entries.length; e++)
		{
			String entry = entries[e].toLowerCase();
			if (entry.indexOf("ora") != -1 && entry.endsWith("bin"))
			{
				StringBuffer sb = getTNS_File (entries[e].substring(0, entries[e].length()-4));
				String[] tnsnames = getTNS_Names (sb);
				if (tnsnames != null)
				{
					for (int i = 0; i < tnsnames.length; i++)
					{
						String tns = tnsnames[i];
						if (!tns.equals(def))
							fTNSName.addItem(tns);
					}
					break;
				}
			}
		}	//	for all path entries

		//	Set first entry
		fTNSName.addActionListener(this);
		if (fTNSName.getItemCount() > 0)
			fTNSName.setSelectedIndex(0);
	}	//	fillTNSName

	/**
	 * 	Get File tnmsnames.ora in StringBuffer
	 * 	@param oraHome ORACLE_HOME
	 * 	@return tnsnames.ora or null
	 */
	private StringBuffer getTNS_File (String oraHome)
	{
		String tnsnames = oraHome + File.separator
			+ "network" + File.separator
			+ "admin" + File.separator
			+ "tnsnames.ora";
		File tnsfile = new File (tnsnames);
		if (!tnsfile.exists())
			return null;

		System.out.println("Searching " + tnsnames);
		StringBuffer sb = new StringBuffer();
		try
		{
			FileReader fr = new FileReader (tnsfile);
			int c;
			while ((c = fr.read()) != -1)
				sb.append((char)c);
		}
		catch (IOException ex)
		{
			System.err.println("Error Reading " + tnsnames);
			ex.printStackTrace();
			return null;
		}
		if (sb.length() == 0)
			return null;
		return sb;
	}	//	getTNS_File

	/**
	 * 	Get TNS Names entries.
	 * 	Assumes standard tnsmanes.ora formatting of NetMgr
	 * 	@param tnsnames content of tnsnames.ora
	 * 	@return tns names or null
	 */
	private String[] getTNS_Names (StringBuffer tnsnames)
	{
		if (tnsnames == null)
			return null;

		ArrayList list = new ArrayList();
		Pattern pattern = Pattern.compile("$", Pattern.MULTILINE);
		String[] lines = pattern.split(tnsnames);
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i].trim();
			if (line.length() > 0
				&& Character.isLetter(line.charAt(0))	//	no # (
				&& line.indexOf("=") != -1
				&& line.indexOf("EXTPROC_") == -1
				&& line.indexOf("_HTTP") == -1)
			{
				String entry = line.substring(0, line.indexOf('=')).trim().toLowerCase();
				System.out.println("- " + entry);
				list.add(entry);
			}
		}
		//	Convert to Array
		if (list.size() == 0)
			return null;
		String[] retValue = new String[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getTNS_Names

	/*************************************************************************/

	/**
	 * 	ActionListener
	 *  @param e event
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (m_testing)
			return;
		//	TNS Name Changed
		if (e.getSource() == fTNSName)
		{
			String tns = (String)fTNSName.getSelectedItem();
			if (tns != null)
			{
				int pos = tns.indexOf('.');
				if (pos != -1)
					tns = tns.substring(0, pos);
				fDatabaseName.setText(tns);
			}
		}

		else if (e.getSource() == bJavaHome)
			setPath (fJavaHome);
		else if (e.getSource() == bCompiereHome)
			setPath (fCompiereHome);
		else if (e.getSource() == bHelp)
			new Setup_Help((Frame)SwingUtilities.getWindowAncestor(this));
		else if (e.getSource() == bTest)
			startTest();
		else if (e.getSource() == bSave)
			save();
	}	//	actionPerformed

	/**
	 * 	Set Path in Field
	 * 	@param field field to set Path
	 */
	private void setPath (JTextField field)
	{
		JFileChooser fc = new JFileChooser(field.getText());
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			field.setText(fc.getSelectedFile().getAbsolutePath());
	}	//	setPath

	/*************************************************************************/

	/**
	 * 	Start Test Async.
	 *  @return SwingWorker
	 */
	private SwingWorker startTest()
	{
		SwingWorker worker = new SwingWorker()
		{
			//	Start it
			public Object construct()
			{
				m_testing = true;
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				bTest.setEnabled(false);
				m_success = false;
				m_errorString = null;
				try
				{
					test();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					m_errorString = ex.getMessage();
				}
				//
				setCursor(Cursor.getDefaultCursor());
				if (m_errorString == null)
					m_success = true;
				bTest.setEnabled(true);
				m_testing = false;
				return new Boolean(m_success);
			}
			//	Finish it
			public void finished()
			{
				if (m_errorString != null)
				{
					System.err.println(m_errorString);
					JOptionPane.showConfirmDialog (null, m_errorString, res.getString("ServerError"),
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		worker.start();
		return worker;
	}	//	startIt

	/**
	 *	Test it
	 * 	@throws Exception
	 */
	private void test() throws Exception
	{
		System.out.println();
		System.out.println("** Test **");
		bSave.setEnabled(false);


		//	Java Home
		m_errorString = res.getString("ErrorJavaHome");
		m_javaHome = new File (fJavaHome.getText());
		if (!m_javaHome.exists())
			return;
		File tools = new File (fJavaHome.getText() + File.separator + "lib" + File.separator + "tools.jar");
		if (!tools.exists())
		{
			System.err.println("Not Found Java SDK = " + tools);
			return;
		}
		System.out.println("OK: JavaHome=" + m_javaHome.getAbsolutePath());
		m_properties.setProperty(JAVA_HOME, m_javaHome.getAbsolutePath());

		//	Java Version
		final String VERSION = "1.4.1";
		final String VERSION2 = "1.4.2";
		boolean versionOK = false;
		String jh = m_javaHome.getAbsolutePath();
		if (jh.indexOf(VERSION) != -1)	//	file name has version = assuming OK
			versionOK = true;
		if (!versionOK && jh.indexOf(VERSION2) != -1)	//	
			versionOK = true;
		String thisJH = System.getProperty("java.home");
		if (thisJH.indexOf(jh) != -1)	//	wre are running the version currently
		{
			String thisJV = System.getProperty("java.version");
			versionOK = thisJV.indexOf(VERSION) != -1;
			if (versionOK)
			  System.out.println("  Java Version OK = " + thisJV);
		}
		if (!versionOK)
		  System.out.println("** Please check Java Version - should be " + VERSION2 + "*");

		//	Compiere Home
		m_errorString = res.getString("ErrorCompiereHome");
		m_compiereHome = new File (fCompiereHome.getText());
		if (!m_compiereHome.exists())
		{
			System.err.println("Not found CompiereHome = " + m_compiereHome);
			return;
		}
		System.out.println("OK: CompiereHome = " + m_compiereHome);
		m_properties.setProperty(COMPIERE_HOME, m_compiereHome.getAbsolutePath());


		//	AppsServer
		m_errorString = res.getString("ErrorAppsServer");
		String server = fAppsServer.getText();
		if (server == null || server.length() == 0 || server.toLowerCase().indexOf("localhost") != -1)
		{
			System.err.println("Not correct AppsServer = " + m_appsServer);
			return;
		}
		m_appsServer = InetAddress.getByName(server);
		System.out.println("OK: AppsServer = " + m_appsServer);
		m_properties.setProperty(COMPIERE_APPS_SERVER, m_appsServer.getHostName());

		//	Web Port
		m_errorString = res.getString("ErrorWebPort");
		m_statusBar.setText(lWebPort.getText());
		m_WebPort = Integer.parseInt(fWebPort.getText());
		//	HTML Port Use
		if (testPort ("http", m_appsServer.getHostName(), m_WebPort, "/") || !testServerPort(m_WebPort))
		{
			System.err.println("Not correct WebPort = " + m_WebPort);
			return;
		}
		System.out.println("OK: WebPort = " + m_WebPort);
		m_properties.setProperty(COMPIERE_WEB_PORT, String.valueOf(m_WebPort));
		//	Derive SSL Port
		int sslPort = m_WebPort == 80 ? 443 : 8443;
		System.out.println("SSL WebPort = " + sslPort);
		m_properties.setProperty(COMPIERE_SSL_PORT, String.valueOf(sslPort));


		//	JNP Port
		m_errorString = res.getString("ErrorJNPPort");
		m_statusBar.setText(lJNPPort.getText());
		m_JNPPort = Integer.parseInt(fJNPPort.getText());
		//	HTML Port Use
		if (testPort (m_appsServer, m_JNPPort, false) || !testServerPort(m_JNPPort))
		{
			System.err.println("Not correct JNPPort = " + m_JNPPort);
			return;
		}
		System.out.println("OK: JNPPort = " + m_JNPPort);
		m_properties.setProperty(COMPIERE_JNP_PORT, String.valueOf(m_JNPPort));


		//	Database Server
		m_errorString = res.getString("ErrorDatabaseServer");
		m_statusBar.setText(lDatabaseServer.getText());
		server = fDatabaseServer.getText();
		if (server == null || server.length() == 0 || server.toLowerCase().indexOf("localhost") != -1)
		{
			System.err.println("Not correct Database Server = " + server);
			return;
		}
		m_databaseServer = InetAddress.getByName(server);
		System.out.println("OK: Database Server = " + m_databaseServer);
		m_properties.setProperty(COMPIERE_DB_SERVER, m_databaseServer.getHostName());


		//	Database Port
		m_errorString = res.getString("ErrorDatabasePort");
		m_databasePort = Integer.parseInt(fDatabasePort.getText());
		//	Database Port Use
		if (!testPort (m_databaseServer, m_databasePort, true))
		{
			System.err.println("Error Database Port = " + m_databasePort);
			return;
		}
		System.out.println("OK: Database Port = " + m_databasePort);
		m_properties.setProperty(COMPIERE_DB_PORT, String.valueOf(m_databasePort));


		//	JDBC Database Info
		m_errorString = res.getString("ErrorJDBC");
		m_statusBar.setText(lDatabaseName.getText());
		m_databaseName = fDatabaseName.getText();	//	SID
		m_systemPassword = new String(fSystemPassword.getPassword());
		if (!testJDBC("system", m_systemPassword))
		{
			System.err.println("Error Database Name = " + m_databaseName);
			System.err.println("Error Database SystemID = system/" + m_systemPassword);
			return;
		}
		System.out.println("OK: Database Name = " + m_databaseName);
		m_properties.setProperty(COMPIERE_DB_NAME, m_databaseName);
		System.out.println("OK: Database SystemID = system/" + m_systemPassword);
		m_properties.setProperty(COMPIERE_DB_SYSTEM, m_systemPassword);
		//	URL (derived)	jdbc:oracle:thin:@prod1:1521:prod1
		StringBuffer url = new StringBuffer("jdbc:oracle:thin:@")
			.append(m_databaseServer).append(":")
			.append(m_databasePort).append(":").append(m_databaseName);
		System.out.println("OK: Database URL = " + url.toString());
		m_properties.setProperty(COMPIERE_DB_URL, url.toString());


		//	Database User Info
		m_databaseUser = fDatabaseUser.getText();	//	UID
		m_databasePassword = new String(fDatabasePassword.getPassword());	//	PWD
		//	Ignore result as it might not be imported
		if (testJDBC(m_databaseUser, m_databasePassword))
			System.out.println("OK: Database UserID = " + m_databaseUser + "/" + m_databasePassword);
		else
			System.out.println("Not created yet: Database UserID = " + m_databaseUser + "/" + m_databasePassword);
		m_properties.setProperty(COMPIERE_DB_USER, m_databaseUser);
		m_properties.setProperty(COMPIERE_DB_PASSWORD, m_databasePassword);


		//	TNS Name Info
		m_errorString = res.getString("ErrorTNS");
		m_statusBar.setText(lTNSName.getText());
		m_TNSName = (String)fTNSName.getSelectedItem();
		if (!testTNS("system", m_systemPassword))
		{
			System.err.println("Error Database TNS Name = " + m_TNSName);
			return;
		}
		System.out.println("OK: Database TNS Name = " + m_TNSName);
		m_properties.setProperty(COMPIERE_DB_TNS, m_TNSName);


		//	Mail Server
		m_errorString = res.getString("ErrorMailServer");
		m_statusBar.setText(lMailServer.getText());
		server = fMailServer.getText();
		if (server == null || server.length() == 0)
		{
			System.err.println("Error Mail Server = " + server);
			return;
		}
		m_mailServer = InetAddress.getByName(server);
		System.out.println("OK: Mail Server = " + m_mailServer);
		m_properties.setProperty(COMPIERE_MAIL_SERVER, m_mailServer.getHostName());


		//	Mail User
		m_errorString = "ErrorMailUser";
		m_statusBar.setText(lMailUser.getText());
		m_mailUser = fMailUser.getText();
		m_mailPassword = new String(fMailPassword.getPassword());
		m_properties.setProperty(COMPIERE_MAIL_USER, m_mailUser);
		m_properties.setProperty(COMPIERE_MAIL_PASSWORD, m_mailPassword);
		System.out.println("  Mail User = " + m_mailUser + "/" + m_mailPassword);

		//	Mail Address
		m_errorString = res.getString("ErrorMail");
		m_adminEMail = new InternetAddress (fAdminEMail.getText());
		//
		if (testMail())
			System.out.println("OK: Admin EMail = " + m_adminEMail);
		else
			System.out.println("Not verified Admin EMail = " + m_adminEMail);
		m_properties.setProperty(COMPIERE_ADMIN_EMAIL, m_adminEMail.toString());

		//
		m_statusBar.setText(res.getString("Ok"));
		System.out.println("** Test OK **");
		bSave.setEnabled(true);
		m_errorString = null;
	}	//	test


	/**
	 * 	Test Apps Server Port (client perspective)
	 *  @param protocol protocol (http, ..)
	 *  @param server server name
	 *  @param port port
	 *  @param file file name
	 *  @return true if able to connect
	 */
	private boolean testPort (String protocol, String server, int port, String file)
	{
		URL url = null;
		try
		{
			url = new URL (protocol, server, port, file);
		}
		catch (MalformedURLException ex)
		{
			ex.printStackTrace();
			return false;
		}
		System.out.println("  URL=" + url);
		try
		{
			URLConnection c = url.openConnection();
			Object o = c.getContent();
			System.err.println("  URL Connection in use=" + url);	//	error
		}
		catch (Exception ex)
		{
			return false;
		}
		return true;
	}	//	testPort

	/**
	 * 	Test Server Port
	 *  @param port port
	 *  @return true if able to create
	 */
	private boolean testServerPort (int port)
	{
		try
		{
			ServerSocket ss = new ServerSocket (port);
			System.out.println("  ServerPort " + ss.getInetAddress() + ":" + ss.getLocalPort());
			ss.close();
		}
		catch (Exception ex)
		{
			System.err.println(ex);
			return false;
		}
		return true;
	}	//	testPort


	/**
	 * 	Test Port
	 *  @param host host
	 *  @param port port
	 *  @param shouldBenUsed true if it should be used
	 *  @return true if some server answered on port
	 */
	public boolean testPort (InetAddress host, int port, boolean shouldBenUsed)
	{
		Socket pingSocket = null;
		try
		{
			pingSocket = new Socket(host, port);
		}
		catch (Exception e)
		{
			if (shouldBenUsed)
				System.err.println("  Open Socket " + host + " on " + port + ": " + e.getMessage());
			return false;
		}
		if (!shouldBenUsed)
			System.err.println("  Open Socket " + host + " on " + port);
		if (pingSocket == null)
			return false;
		//	success
		try
		{
			pingSocket.close();
		}
		catch (IOException e)
		{
			System.out.println("  CloseSocket=" + e.toString());
		}
		return true;
	}	//	testPort

	/**
	 * 	Test JDBC Connection to Server
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testJDBC (String uid, String pwd)
	{
		//	jdbc:oracle:thin:@dev:1521:dev1
		m_connectionString = "jdbc:oracle:thin:@"
			+ m_databaseServer.getHostName() + ":" + m_databasePort + ":" + m_databaseName;
		System.out.println("  JDBC = " + m_connectionString);

		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(m_connectionString,
				uid, pwd);
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testJDBC

	/**
	 * 	Test TNS Connection
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testTNS (String uid, String pwd)
	{
		String connectionString = "jdbc:oracle:oci8:@" + m_TNSName;
		System.out.println("  TNS = " + connectionString);
		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(connectionString,
				uid, pwd);
		}
		catch (UnsatisfiedLinkError ule)
		{
			System.err.println("Check setup of Oracle Server / Oracle Client / LD_LIBRARY_PATH");
			System.err.println(ule.toString());
			return false;
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testTNS

	/**
	 * 	Test Mail
	 *  @return true of OK
	 */
	private boolean testMail()
	{
		boolean smtpOK = false;
		boolean imapOK = false;
		if (testPort (m_mailServer, 25, true))
		{
			System.out.println("OK: SMTP Server contacted");
			smtpOK = true;
		}
		else
			System.err.println("Error: SMTP Server NOT available");
		//
		if (testPort (m_mailServer, 110, true))
			System.out.println("OK: POP3 Server contacted");
		else
			System.err.println("Error: POP3 Server NOT available");
		if (testPort (m_mailServer, 143, true))
		{
			System.out.println("OK: IMAP4 Server contacted");
			imapOK = true;
		}
		else
			System.err.println("Error: IMAP4 Server NOT available");
		//
		if (!smtpOK)
			return false;
		//
		try
		{
			EMail em = new EMail (m_mailServer.getHostName (),
					   m_adminEMail.toString (), m_adminEMail.toString (),
					   "Compiere Server Setup Test", "Test: " + m_properties);
			em.setEMailUser (m_mailUser, m_mailPassword);
			if (EMail.SENT_OK.equals (em.send ()))
			{
				System.out.println ("OK: Send Test Email to " + m_adminEMail);
			}
			else
			{
				System.err.println ("Error: Could NOT send Email to "
					+ m_adminEMail);
			}
		}
		catch (Exception ex)
		{
			System.err.println("testMail - " + ex.getLocalizedMessage());
			return false;
		}

		//
		if (!imapOK)
			return false;

		//	Test Read Mail Access
		Properties props = new Properties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_mailServer.getHostName());
		props.put("mail.user", m_mailUser);
		props.put("mail.smtp.auth", "true");
		System.out.println("  Connecting to " + m_mailServer.getHostName());
		//
		Session session = null;
		Store store = null;
		try
		{
			EMailAuthenticator auth = new EMailAuthenticator (m_mailUser, m_mailPassword);
			session = Session.getDefaultInstance(props, auth);
			session.setDebug (Log.isTraceLevel (10));
			System.out.println("  Session=" + session);
			//	Connect to Store
			store = session.getStore("imap");
			System.out.println("  Store=" + store);
		}
		catch (NoSuchProviderException nsp)
		{
			System.err.println("Error Mail IMAP Provider - " + nsp.getMessage());
			return false;
		}
		catch (Exception e)
		{
			System.err.println("Error Mail IMAP - " + e.getMessage());
			return false;
		}
		try
		{
			store.connect(m_mailServer.getHostName(), m_mailUser, m_mailPassword);
			System.out.println("  Store - connected");
			Folder folder = store.getDefaultFolder();
			Folder inbox = folder.getFolder("INBOX");
			System.out.println("OK: Mail Connect to " + inbox.getFullName() + " #Msg=" + inbox.getMessageCount());
			//
			store.close();
		}
		catch (MessagingException mex)
		{
			System.err.println("Error Mail Connect " + mex.getMessage());
			return false;
		}
		return true;
	}	//	testMail

	/*************************************************************************/

	/**
	 * 	Save Settings
	 */
	private void save()
	{
		SwingWorker sw = startTest();
		while (sw.isAlive())
		{
			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException ex)
			{
				System.err.println("save-waiting: " + ex);
			}
		}
		sw.get();	//	block
		if (!m_success)
			return;

		//	Add
		m_properties.setProperty("COMPIERE_MAIN_VERSION", Compiere.MAIN_VERSION);
		m_properties.setProperty("COMPIERE_DATE_VERSION", Compiere.DATE_VERSION);
		m_properties.setProperty("COMPIERE_DB_VERSION", Compiere.DB_VERSION);



		//	Before we save, load Ini
		Ini.setClient(false);
		String fileName = m_compiereHome.getAbsolutePath() + File.separator + Ini.COMPIERE_PROPERTY_FILE;
		Ini.loadProperties(fileName);

		//	Save Environment
		fileName = m_compiereHome.getAbsolutePath() + File.separator + COMPIERE_ENV_FILE;
		try
		{
			FileOutputStream fos = new FileOutputStream(new File(fileName));
			m_properties.store(fos, COMPIERE_ENV_FILE);
			fos.flush();
			fos.close();
		}
		catch (Exception e)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + e.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (Throwable t)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + t.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		System.out.println("Properties saved to " + fileName);

		//	Sync Properties
		Ini.setCompiereHome(m_compiereHome.getAbsolutePath());
		CConnection cc = CConnection.get (Database.DB_ORACLE,
			m_databaseServer.getHostName(), m_databasePort, m_databaseName,
			m_databaseUser, m_databasePassword);
		cc.setAppsHost(m_appsServer.getHostName());
		cc.setRMIoverHTTP(false);
		Ini.setProperty(Ini.P_CONNECTION, cc.toStringLong());
		Ini.saveProperties(false);


		JOptionPane.showConfirmDialog(this, res.getString("EnvironmentSaved"),
			res.getString("CompiereServerSetup"),
			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

		((Frame)SwingUtilities.getWindowAncestor(this)).dispose();
		System.exit(0);		//	remains active when License Dialog called
	}	//	save

}	//	ConfigurationPanel
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2002 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.install;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;

import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;

import oracle.jdbc.*;
import org.compiere.*;
import org.compiere.apps.*;
import org.compiere.db.*;
import org.compiere.util.*;


/**
 *	Configuration Panel
 *
 * 	@author 	Jorg Janke
 * 	@author 	Marek Mosiewicz<marek.mosiewicz@jotel.com.pl> support for RMI over HTTP - now reimplemented/removed jj
 * 	@version 	$Id: ConfigurationPanel.java,v 1.6 2003/07/21 04:44:17 jjanke Exp $
 */
public class ConfigurationPanel extends JPanel implements ActionListener
{
	/**
	 * 	Constructor
	 *  @param statusBar for info
	 * 	@throws Exception
	 */
	public ConfigurationPanel (JLabel statusBar) throws Exception
	{
		LogManager.resetConfiguration();
		Logger root = Logger.getRootLogger();
	//	root.addAppender(new ConsoleAppender(new PatternLayout("%d{ABSOLUTE} %-5p [%c{1}] %m%n")));
		root.addAppender(new ConsoleAppender(new LogLayout()));
		root.setLevel(Level.ALL);
		Log.setTraceLevel(10);

		m_statusBar = statusBar;
		jbInit();
		dynInit();
	}	//	ConfigurationPanel

	public static final String	COMPIERE_ENV_FILE		= "CompiereEnv.properties";

	public static final String	COMPIERE_HOME 			= "COMPIERE_HOME";
	public static final String	JAVA_HOME 				= "JAVA_HOME";
	public static final String	COMPIERE_JAVA_OPTIONS 	= "COMPIERE_JAVA_OPTIONS";

	public static final String	COMPIERE_APPS_SERVER 	= "COMPIERE_APPS_SERVER";
	public static final String	COMPIERE_JNP_PORT 		= "COMPIERE_JNP_PORT";
	public static final String	COMPIERE_WEB_PORT 		= "COMPIERE_WEB_PORT";
	public static final String	COMPIERE_SSL_PORT 		= "COMPIERE_SSL_PORT";

	public static final String	COMPIERE_DB_SERVER 		= "COMPIERE_DB_SERVER";
	public static final String	COMPIERE_DB_PORT 		= "COMPIERE_DB_PORT";
	public static final String	COMPIERE_DB_NAME 		= "COMPIERE_DB_NAME";

	public static final String	COMPIERE_DB_USER 		= "COMPIERE_DB_USER";
	public static final String	COMPIERE_DB_PASSWORD 	= "COMPIERE_DB_PASSWORD";
	public static final String	COMPIERE_DB_SYSTEM 		= "COMPIERE_DB_SYSTEM";

	public static final String	COMPIERE_DB_URL 		= "COMPIERE_DB_URL";
	public static final String	COMPIERE_DB_TNS 		= "COMPIERE_DB_TNS";

	public static final String	COMPIERE_MAIL_SERVER 	= "COMPIERE_MAIL_SERVER";
	public static final String	COMPIERE_MAIL_USER 		= "COMPIERE_MAIL_USER";
	public static final String	COMPIERE_MAIL_PASSWORD 	= "COMPIERE_MAIL_PASSWORD";
	public static final String	COMPIERE_ADMIN_EMAIL	= "COMPIERE_ADMIN_EMAIL";

	public static final String	COMPIERE_FTP_SERVER		= "COMPIERE_FTP_SERVER";
	public static final String	COMPIERE_FTP_USER		= "COMPIERE_FTP_USER";
	public static final String	COMPIERE_FTP_PASSWORD	= "COMPIERE_FTP_PASSWORD";
	public static final String	COMPIERE_FTP_PREFIX		= "COMPIERE_FTP_PREFIX";

	private String			m_errorString;
	private File			m_javaHome;
	private File			m_compiereHome;
	private InetAddress		m_appsServer;
	private int				m_WebPort;
	private int				m_JNPPort;
	private InetAddress		m_databaseServer;
	private int				m_databasePort;
	private String			m_databaseName;
	private String			m_connectionString;
	private String			m_systemPassword;
	private String			m_databaseUser;
	private String			m_databasePassword;
	private String			m_TNSName;

	private InetAddress		m_mailServer;
	private InternetAddress	m_adminEMail;
	private String			m_mailUser;
	private String			m_mailPassword;

	private volatile boolean		m_success = false;
	private volatile boolean	 	m_testing = false;

	/** Translation				*/
	static ResourceBundle 		res = ResourceBundle.getBundle("org.compiere.install.SetupRes");

	/**	Driver					*/
	private static Driver		s_driver = null;
	/** Environment Properties	*/
	private Properties			m_properties = new Properties();
	/** Status Bar				*/
	private JLabel 				m_statusBar;

	private static ImageIcon iOpen = new ImageIcon(ConfigurationPanel.class.getResource("openFile.gif"));
	private static ImageIcon iSave = new ImageIcon(Compiere.class.getResource("images/Save16.gif"));
	private static ImageIcon iHelp = new ImageIcon(Compiere.class.getResource("images/Help16.gif"));


	//	-------------	Static UI
	private GridBagLayout gridBagLayout = new GridBagLayout();
	private JLabel lCompiereHome = new JLabel();
	private JTextField fCompiereHome = new JTextField();
	private JLabel lWebPort = new JLabel();
	private JTextField fWebPort = new JTextField();
	private JLabel lAppsServer = new JLabel();
	private JTextField fAppsServer = new JTextField();
	private JLabel lDatabaseType = new JLabel();
	private JComboBox fDatabaseType = new JComboBox();
	private JLabel lDatabaseName = new JLabel();
	private JLabel lDatabasePort = new JLabel();
	private JLabel lDatabaseUser = new JLabel();
	private JLabel lDatabasePassword = new JLabel();
	private JTextField fDatabaseName = new JTextField();
	private JTextField fDatabasePort = new JTextField();
	private JTextField fDatabaseUser = new JTextField();
	private JPasswordField fDatabasePassword = new JPasswordField();
	private JLabel lTNSName = new JLabel();
	private JComboBox fTNSName = new JComboBox();
	private JLabel lSystemPassword = new JLabel();
	private JPasswordField fSystemPassword = new JPasswordField();
	private JLabel lMailServer = new JLabel();
	private JTextField fMailServer = new JTextField();
	private JLabel lAdminEMail = new JLabel();
	private JTextField fAdminEMail = new JTextField();
	private JLabel lDatabaseServer = new JLabel();
	private JTextField fDatabaseServer = new JTextField();
	private JLabel lJavaHome = new JLabel();
	private JTextField fJavaHome = new JTextField();
	private JButton bCompiereHome = new JButton(iOpen);
	private JButton bJavaHome = new JButton(iOpen);
	private JButton bHelp = new JButton(iHelp);
	private JButton bTest = new JButton();
	private JButton bSave = new JButton(iSave);
	private JLabel lJNPPort = new JLabel();
	private JTextField fJNPPort = new JTextField();
	private JLabel lMailUser = new JLabel();
	private JLabel lMailPassword = new JLabel();
	private JTextField fMailUser = new JTextField();
	private JPasswordField fMailPassword = new JPasswordField();


	/**
	 * 	Static Layout Init
	 *  @throws Exception
	 */
	private void jbInit() throws Exception
	{
		lCompiereHome.setToolTipText(res.getString("CompiereHomeInfo"));
		lCompiereHome.setText(res.getString("CompiereHome"));
		this.setLayout(gridBagLayout);
		fCompiereHome.setText("D:\\Compiere2");
		lWebPort.setToolTipText(res.getString("WebPortInfo"));
		lWebPort.setText(res.getString("WebPort"));
		fWebPort.setText("80");
		lAppsServer.setToolTipText(res.getString("AppsServerInfo"));
		lAppsServer.setText(res.getString("AppsServer"));
		fAppsServer.setText("server.company.com");
		lDatabaseType.setToolTipText(res.getString("DatabaseTypeInfo"));
		lDatabaseType.setText(res.getString("DatabaseType"));
		lDatabaseName.setToolTipText(res.getString("DatabaseNameInfo"));
		lDatabaseName.setText(res.getString("DatabaseName"));
		lDatabasePort.setToolTipText(res.getString("DatabasePortInfo"));
		lDatabasePort.setText(res.getString("DatabasePort"));
		lDatabaseUser.setToolTipText(res.getString("DatabaseUserInfo"));
		lDatabaseUser.setText(res.getString("DatabaseUser"));
		lDatabasePassword.setToolTipText(res.getString("DatabasePasswordInfo"));
		lDatabasePassword.setText(res.getString("DatabasePassword"));
		fDatabaseName.setText("compiere");
		fDatabasePort.setText("1521");
		fDatabaseUser.setText("compiere");
		fDatabasePassword.setText("compiere");
		lTNSName.setToolTipText(res.getString("TNSNameInfo"));
		lTNSName.setText(res.getString("TNSName"));
		fTNSName.setEditable(true);
		lSystemPassword.setToolTipText(res.getString("SystemPasswordInfo"));
		lSystemPassword.setText(res.getString("SystemPassword"));
		fSystemPassword.setText("manager");
		lMailServer.setToolTipText(res.getString("MailServerInfo"));
		lMailServer.setText(res.getString("MailServer"));
		fMailServer.setText("mail.company.com");
		lAdminEMail.setToolTipText(res.getString("AdminEMailInfo"));
		lAdminEMail.setText(res.getString("AdminEMail"));
		fAdminEMail.setText("admin@company.com");
		bTest.setToolTipText(res.getString("TestInfo"));
		bTest.setText(res.getString("Test"));
		bSave.setToolTipText(res.getString("SaveInfo"));
		bSave.setText(res.getString("Save"));
		lDatabaseServer.setToolTipText(res.getString("DatabaseServerInfo"));
		lDatabaseServer.setText(res.getString("DatabaseServer"));
		lJavaHome.setToolTipText(res.getString("JavaHomeInfo"));
		lJavaHome.setText(res.getString("JavaHome"));
		fJavaHome.setText("Java141");
		lJNPPort.setToolTipText(res.getString("JNPPortInfo"));
		lJNPPort.setText(res.getString("JNPPort"));
		fJNPPort.setText("1099");
		bHelp.setToolTipText(res.getString("HelpInfo"));
		lMailUser.setToolTipText(res.getString("MailUserInfo"));
		lMailUser.setText(res.getString("MailUser"));
		lMailPassword.setToolTipText(res.getString("MailPasswordInfo"));
		lMailPassword.setText(res.getString("MailPassword"));
		fMailUser.setText("compiere");
		fMailPassword.setText("compiere");

		//
		this.add(lCompiereHome,           new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fCompiereHome,             new GridBagConstraints(1, 1, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lWebPort,           new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fWebPort,            new GridBagConstraints(1, 3, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lAppsServer,          new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fAppsServer,           new GridBagConstraints(1, 2, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseType,           new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseType,          new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabasePort,          new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseUser,           new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseName,             new GridBagConstraints(1, 6, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabasePort,            new GridBagConstraints(1, 7, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseUser,             new GridBagConstraints(1, 8, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lTNSName,           new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fTNSName,              new GridBagConstraints(3, 5, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lMailServer,          new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lSystemPassword,           new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fSystemPassword,          new GridBagConstraints(3, 7, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabasePassword,           new GridBagConstraints(3, 8, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabasePassword,          new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailServer,          new GridBagConstraints(1, 9, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(bTest,         new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(bSave,          new GridBagConstraints(3, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabaseServer,          new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseServer,         new GridBagConstraints(1, 5, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseName,       new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lJavaHome,      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJavaHome,            new GridBagConstraints(1, 0, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bCompiereHome,     new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bHelp,        new GridBagConstraints(2, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bJavaHome,     new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lJNPPort,     new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJNPPort,      new GridBagConstraints(3, 3, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lMailPassword,      new GridBagConstraints(2, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailPassword,       new GridBagConstraints(3, 10, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lAdminEMail,   new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailUser,   new GridBagConstraints(1, 10, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fAdminEMail,    new GridBagConstraints(3, 9, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lMailUser,  new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 0), 0, 0));
		//
		bCompiereHome.addActionListener(this);
		bJavaHome.addActionListener(this);
		bHelp.addActionListener(this);
		bTest.addActionListener(this);
		bSave.addActionListener(this);
	}	//	jbInit


	/**
	 * 	Dynamic Init
	 *  @throws Exception
	 */
	private void dynInit() throws Exception
	{
		//	Layout - Column Length
		fWebPort.setColumns(30);
		fJNPPort.setColumns(30);

		//	DatabaseType
		fDatabaseType.addItem("Oracle 9i2");

		//	Compiere Home
		String ch = System.getProperty(COMPIERE_HOME);
		if (ch == null || ch.length() == 0)
			ch = System.getProperty("user.dir");
		fCompiereHome.setText(ch);
		boolean envLoaded = false;
		String fileName = ch + File.separator + COMPIERE_ENV_FILE;
		File env = new File (fileName);
		System.out.println(env + " - exists=" + env.exists());
		if (env.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(env);
				m_properties.load(fis);
				fis.close();
			}
			catch (Exception e)
			{
				System.err.println(e.toString());
			}
			System.out.println("Loading ...");
			if (m_properties.size() > 5)
				envLoaded = true;
			//
			fCompiereHome.setText((String)m_properties.get(COMPIERE_HOME));
			fJavaHome.setText((String)m_properties.get(JAVA_HOME));
			fAppsServer.setText((String)m_properties.get(COMPIERE_APPS_SERVER));
			fWebPort.setText((String)m_properties.get(COMPIERE_WEB_PORT));
			fJNPPort.setText((String)m_properties.get(COMPIERE_JNP_PORT));

			fDatabaseServer.setText((String)m_properties.get(COMPIERE_DB_SERVER));
			fDatabasePort.setText((String)m_properties.get(COMPIERE_DB_PORT));
			fDatabaseName.setText((String)m_properties.get(COMPIERE_DB_NAME));

			fDatabaseUser.setText((String)m_properties.get(COMPIERE_DB_USER));
			fDatabasePassword.setText((String)m_properties.get(COMPIERE_DB_PASSWORD));
			fSystemPassword.setText((String)m_properties.get(COMPIERE_DB_SYSTEM));
			fillTNSName((String)m_properties.get(COMPIERE_DB_TNS));

			fMailServer.setText((String)m_properties.get(COMPIERE_MAIL_SERVER));
			fMailUser.setText((String)m_properties.get(COMPIERE_MAIL_USER));
			fMailPassword.setText((String)m_properties.get(COMPIERE_MAIL_PASSWORD));
			fAdminEMail.setText((String)m_properties.get(COMPIERE_ADMIN_EMAIL));
		}

		//	No environment file found - defaults
		if (!envLoaded)
		{
			System.out.println("Setting Defaults");
			//	Java Home, e.g. D:\j2sdk1.4.1\jre
			String javaHome = System.getProperty("java.home");
			if (javaHome.endsWith("jre"))
				javaHome = javaHome.substring(0, javaHome.length()-4);
			fJavaHome.setText(javaHome);
			//	AppsServer
			fAppsServer.setText(InetAddress.getLocalHost().getHostName());
			//	Database Server
			fDatabaseServer.setText(InetAddress.getLocalHost().getHostName());
			//	Mail Server
			fMailServer.setText(InetAddress.getLocalHost().getHostName());
			//  Get TNS Names
			fillTNSName("");
		}	//	!envLoaded

		//	Default FTP stuff
		if (!m_properties.containsKey(COMPIERE_FTP_SERVER))
		{
			m_properties.setProperty(COMPIERE_FTP_SERVER, "localhost");
			m_properties.setProperty(COMPIERE_FTP_USER, "anonymous");
			m_properties.setProperty(COMPIERE_FTP_PASSWORD, "user@host.com");
			m_properties.setProperty(COMPIERE_FTP_PREFIX, "my");
		}
		//	Default Java Options
		if (!m_properties.containsKey(COMPIERE_JAVA_OPTIONS))
			m_properties.setProperty(COMPIERE_JAVA_OPTIONS, "-Xms32m -Xmx128m");

		//	(String)m_properties.get(COMPIERE_DB_URL)	//	derived
	}	//	dynInit

	/**
	 * 	Fill TNS Name
	 * 	@param defaultValue defaultValue
	 */
	private void fillTNSName (String defaultValue)
	{
		//	default value to lowercase or null
		String def = defaultValue;
		if (def != null && def.trim().length() == 0)
			def = null;
		if (def != null)
		{
			def = def.toLowerCase();
			fTNSName.addItem(def);
		}

		//	Search for Oracle Info
		String path = System.getProperty("java.library.path");
		String[] entries = path.split(File.pathSeparator);
		for (int e = 0; e < entries.length; e++)
		{
			String entry = entries[e].toLowerCase();
			if (entry.indexOf("ora") != -1 && entry.endsWith("bin"))
			{
				StringBuffer sb = getTNS_File (entries[e].substring(0, entries[e].length()-4));
				String[] tnsnames = getTNS_Names (sb);
				if (tnsnames != null)
				{
					for (int i = 0; i < tnsnames.length; i++)
					{
						String tns = tnsnames[i];
						if (!tns.equals(def))
							fTNSName.addItem(tns);
					}
					break;
				}
			}
		}	//	for all path entries

		//	Set first entry
		fTNSName.addActionListener(this);
		if (fTNSName.getItemCount() > 0)
			fTNSName.setSelectedIndex(0);
	}	//	fillTNSName

	/**
	 * 	Get File tnmsnames.ora in StringBuffer
	 * 	@param oraHome ORACLE_HOME
	 * 	@return tnsnames.ora or null
	 */
	private StringBuffer getTNS_File (String oraHome)
	{
		String tnsnames = oraHome + File.separator
			+ "network" + File.separator
			+ "admin" + File.separator
			+ "tnsnames.ora";
		File tnsfile = new File (tnsnames);
		if (!tnsfile.exists())
			return null;

		System.out.println("Searching " + tnsnames);
		StringBuffer sb = new StringBuffer();
		try
		{
			FileReader fr = new FileReader (tnsfile);
			int c;
			while ((c = fr.read()) != -1)
				sb.append((char)c);
		}
		catch (IOException ex)
		{
			System.err.println("Error Reading " + tnsnames);
			ex.printStackTrace();
			return null;
		}
		if (sb.length() == 0)
			return null;
		return sb;
	}	//	getTNS_File

	/**
	 * 	Get TNS Names entries.
	 * 	Assumes standard tnsmanes.ora formatting of NetMgr
	 * 	@param tnsnames content of tnsnames.ora
	 * 	@return tns names or null
	 */
	private String[] getTNS_Names (StringBuffer tnsnames)
	{
		if (tnsnames == null)
			return null;

		ArrayList list = new ArrayList();
		Pattern pattern = Pattern.compile("$", Pattern.MULTILINE);
		String[] lines = pattern.split(tnsnames);
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i].trim();
			if (line.length() > 0
				&& Character.isLetter(line.charAt(0))	//	no # (
				&& line.indexOf("=") != -1
				&& line.indexOf("EXTPROC_") == -1
				&& line.indexOf("_HTTP") == -1)
			{
				String entry = line.substring(0, line.indexOf('=')).trim().toLowerCase();
				System.out.println("- " + entry);
				list.add(entry);
			}
		}
		//	Convert to Array
		if (list.size() == 0)
			return null;
		String[] retValue = new String[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getTNS_Names

	/*************************************************************************/

	/**
	 * 	ActionListener
	 *  @param e event
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (m_testing)
			return;
		//	TNS Name Changed
		if (e.getSource() == fTNSName)
		{
			String tns = (String)fTNSName.getSelectedItem();
			if (tns != null)
			{
				int pos = tns.indexOf('.');
				if (pos != -1)
					tns = tns.substring(0, pos);
				fDatabaseName.setText(tns);
			}
		}

		else if (e.getSource() == bJavaHome)
			setPath (fJavaHome);
		else if (e.getSource() == bCompiereHome)
			setPath (fCompiereHome);
		else if (e.getSource() == bHelp)
			new Setup_Help((Frame)SwingUtilities.getWindowAncestor(this));
		else if (e.getSource() == bTest)
			startTest();
		else if (e.getSource() == bSave)
			save();
	}	//	actionPerformed

	/**
	 * 	Set Path in Field
	 * 	@param field field to set Path
	 */
	private void setPath (JTextField field)
	{
		JFileChooser fc = new JFileChooser(field.getText());
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			field.setText(fc.getSelectedFile().getAbsolutePath());
	}	//	setPath

	/*************************************************************************/

	/**
	 * 	Start Test Async.
	 *  @return SwingWorker
	 */
	private SwingWorker startTest()
	{
		SwingWorker worker = new SwingWorker()
		{
			//	Start it
			public Object construct()
			{
				m_testing = true;
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				bTest.setEnabled(false);
				m_success = false;
				m_errorString = null;
				try
				{
					test();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					m_errorString = ex.getMessage();
				}
				//
				setCursor(Cursor.getDefaultCursor());
				if (m_errorString == null)
					m_success = true;
				bTest.setEnabled(true);
				m_testing = false;
				return new Boolean(m_success);
			}
			//	Finish it
			public void finished()
			{
				if (m_errorString != null)
				{
					System.err.println(m_errorString);
					JOptionPane.showConfirmDialog (null, m_errorString, res.getString("ServerError"),
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		worker.start();
		return worker;
	}	//	startIt

	/**
	 *	Test it
	 * 	@throws Exception
	 */
	private void test() throws Exception
	{
		System.out.println();
		System.out.println("** Test **");
		bSave.setEnabled(false);


		//	Java Home
		m_errorString = res.getString("ErrorJavaHome");
		m_javaHome = new File (fJavaHome.getText());
		if (!m_javaHome.exists())
			return;
		File tools = new File (fJavaHome.getText() + File.separator + "lib" + File.separator + "tools.jar");
		if (!tools.exists())
		{
			System.err.println("Not Found Java SDK = " + tools);
			return;
		}
		System.out.println("OK: JavaHome=" + m_javaHome.getAbsolutePath());
		m_properties.setProperty(JAVA_HOME, m_javaHome.getAbsolutePath());

		//	Java Version
		final String VERSION = "1.4.1";
		boolean versionOK = false;
		String jh = m_javaHome.getAbsolutePath();
		if (jh.indexOf(VERSION) != -1)	//	file name has version = assuming OK
			versionOK = true;
		String thisJH = System.getProperty("java.home");
		if (thisJH.indexOf(jh) != -1)	//	wre are running the version currently
		{
			String thisJV = System.getProperty("java.version");
			versionOK = thisJV.indexOf(VERSION) != -1;
			if (versionOK)
			  System.out.println("  Java Version OK = " + thisJV);
		}
		if (!versionOK)
		  System.out.println("** Please check Java Version - should be " + VERSION + "*");

		//	Compiere Home
		m_errorString = res.getString("ErrorCompiereHome");
		m_compiereHome = new File (fCompiereHome.getText());
		if (!m_compiereHome.exists())
		{
			System.err.println("Not found CompiereHome = " + m_compiereHome);
			return;
		}
		System.out.println("OK: CompiereHome = " + m_compiereHome);
		m_properties.setProperty(COMPIERE_HOME, m_compiereHome.getAbsolutePath());


		//	AppsServer
		m_errorString = res.getString("ErrorAppsServer");
		String server = fAppsServer.getText();
		if (server == null || server.length() == 0 || server.toLowerCase().indexOf("localhost") != -1)
		{
			System.err.println("Not correct AppsServer = " + m_appsServer);
			return;
		}
		m_appsServer = InetAddress.getByName(server);
		System.out.println("OK: AppsServer = " + m_appsServer);
		m_properties.setProperty(COMPIERE_APPS_SERVER, m_appsServer.getHostName());

		//	Web Port
		m_errorString = res.getString("ErrorWebPort");
		m_statusBar.setText(lWebPort.getText());
		m_WebPort = Integer.parseInt(fWebPort.getText());
		//	HTML Port Use
		if (testPort ("http", m_appsServer.getHostName(), m_WebPort, "/") || !testServerPort(m_WebPort))
		{
			System.err.println("Not correct WebPort = " + m_WebPort);
			return;
		}
		System.out.println("OK: WebPort = " + m_WebPort);
		m_properties.setProperty(COMPIERE_WEB_PORT, String.valueOf(m_WebPort));
		//	Derive SSL Port
		int sslPort = m_WebPort == 80 ? 443 : 8443;
		System.out.println("SSL WebPort = " + sslPort);
		m_properties.setProperty(COMPIERE_SSL_PORT, String.valueOf(sslPort));


		//	JNP Port
		m_errorString = res.getString("ErrorJNPPort");
		m_statusBar.setText(lJNPPort.getText());
		m_JNPPort = Integer.parseInt(fJNPPort.getText());
		//	HTML Port Use
		if (testPort (m_appsServer, m_JNPPort, false) || !testServerPort(m_JNPPort))
		{
			System.err.println("Not correct JNPPort = " + m_JNPPort);
			return;
		}
		System.out.println("OK: JNPPort = " + m_JNPPort);
		m_properties.setProperty(COMPIERE_JNP_PORT, String.valueOf(m_JNPPort));


		//	Database Server
		m_errorString = res.getString("ErrorDatabaseServer");
		m_statusBar.setText(lDatabaseServer.getText());
		server = fDatabaseServer.getText();
		if (server == null || server.length() == 0 || server.toLowerCase().indexOf("localhost") != -1)
		{
			System.err.println("Not correct Database Server = " + server);
			return;
		}
		m_databaseServer = InetAddress.getByName(server);
		System.out.println("OK: Database Server = " + m_databaseServer);
		m_properties.setProperty(COMPIERE_DB_SERVER, m_databaseServer.getHostName());


		//	Database Port
		m_errorString = res.getString("ErrorDatabasePort");
		m_databasePort = Integer.parseInt(fDatabasePort.getText());
		//	Database Port Use
		if (!testPort (m_databaseServer, m_databasePort, true))
		{
			System.err.println("Error Database Port = " + m_databasePort);
			return;
		}
		System.out.println("OK: Database Port = " + m_databasePort);
		m_properties.setProperty(COMPIERE_DB_PORT, String.valueOf(m_databasePort));


		//	JDBC Database Info
		m_errorString = res.getString("ErrorJDBC");
		m_statusBar.setText(lDatabaseName.getText());
		m_databaseName = fDatabaseName.getText();	//	SID
		m_systemPassword = new String(fSystemPassword.getPassword());
		if (!testJDBC("system", m_systemPassword))
		{
			System.err.println("Error Database Name = " + m_databaseName);
			System.err.println("Error Database SystemID = system/" + m_systemPassword);
			return;
		}
		System.out.println("OK: Database Name = " + m_databaseName);
		m_properties.setProperty(COMPIERE_DB_NAME, m_databaseName);
		System.out.println("OK: Database SystemID = system/" + m_systemPassword);
		m_properties.setProperty(COMPIERE_DB_SYSTEM, m_systemPassword);
		//	URL (derived)	jdbc:oracle:thin:@prod1:1521:prod1
		StringBuffer url = new StringBuffer("jdbc:oracle:thin:@")
			.append(m_databaseServer).append(":")
			.append(m_databasePort).append(":").append(m_databaseName);
		System.out.println("OK: Database URL = " + url.toString());
		m_properties.setProperty(COMPIERE_DB_URL, url.toString());


		//	Database User Info
		m_databaseUser = fDatabaseUser.getText();	//	UID
		m_databasePassword = new String(fDatabasePassword.getPassword());	//	PWD
		//	Ignore result as it might not be imported
		if (testJDBC(m_databaseUser, m_databasePassword))
			System.out.println("OK: Database UserID = " + m_databaseUser + "/" + m_databasePassword);
		else
			System.out.println("Not created yet: Database UserID = " + m_databaseUser + "/" + m_databasePassword);
		m_properties.setProperty(COMPIERE_DB_USER, m_databaseUser);
		m_properties.setProperty(COMPIERE_DB_PASSWORD, m_databasePassword);


		//	TNS Name Info
		m_errorString = res.getString("ErrorTNS");
		m_statusBar.setText(lTNSName.getText());
		m_TNSName = (String)fTNSName.getSelectedItem();
		if (!testTNS("system", m_systemPassword))
		{
			System.err.println("Error Database TNS Name = " + m_TNSName);
			return;
		}
		System.out.println("OK: Database TNS Name = " + m_TNSName);
		m_properties.setProperty(COMPIERE_DB_TNS, m_TNSName);


		//	Mail Server
		m_errorString = res.getString("ErrorMailServer");
		m_statusBar.setText(lMailServer.getText());
		server = fMailServer.getText();
		if (server == null || server.length() == 0)
		{
			System.err.println("Error Mail Server = " + server);
			return;
		}
		m_mailServer = InetAddress.getByName(server);
		System.out.println("OK: Mail Server = " + m_mailServer);
		m_properties.setProperty(COMPIERE_MAIL_SERVER, m_mailServer.getHostName());


		//	Mail User
		m_errorString = "ErrorMailUser";
		m_statusBar.setText(lMailUser.getText());
		m_mailUser = fMailUser.getText();
		m_mailPassword = new String(fMailPassword.getPassword());
		m_properties.setProperty(COMPIERE_MAIL_USER, m_mailUser);
		m_properties.setProperty(COMPIERE_MAIL_PASSWORD, m_mailPassword);
		System.out.println("  Mail User = " + m_mailUser + "/" + m_mailPassword);

		//	Mail Address
		m_errorString = res.getString("ErrorMail");
		m_adminEMail = new InternetAddress (fAdminEMail.getText());
		//
		if (testMail())
			System.out.println("OK: Admin EMail = " + m_adminEMail);
		else
			System.out.println("Not verified Admin EMail = " + m_adminEMail);
		m_properties.setProperty(COMPIERE_ADMIN_EMAIL, m_adminEMail.toString());

		//
		m_statusBar.setText(res.getString("Ok"));
		System.out.println("** Test OK **");
		bSave.setEnabled(true);
		m_errorString = null;
	}	//	test


	/**
	 * 	Test Apps Server Port (client perspective)
	 *  @param protocol protocol (http, ..)
	 *  @param server server name
	 *  @param port port
	 *  @param file file name
	 *  @return true if able to connect
	 */
	private boolean testPort (String protocol, String server, int port, String file)
	{
		URL url = null;
		try
		{
			url = new URL (protocol, server, port, file);
		}
		catch (MalformedURLException ex)
		{
			ex.printStackTrace();
			return false;
		}
		System.out.println("  URL=" + url);
		try
		{
			URLConnection c = url.openConnection();
			Object o = c.getContent();
			System.err.println("  URL Connection in use=" + url);	//	error
		}
		catch (Exception ex)
		{
			return false;
		}
		return true;
	}	//	testPort

	/**
	 * 	Test Server Port
	 *  @param port port
	 *  @return true if able to create
	 */
	private boolean testServerPort (int port)
	{
		try
		{
			ServerSocket ss = new ServerSocket (port);
			System.out.println("  ServerPort " + ss.getInetAddress() + ":" + ss.getLocalPort());
			ss.close();
		}
		catch (Exception ex)
		{
			System.err.println(ex);
			return false;
		}
		return true;
	}	//	testPort


	/**
	 * 	Test Port
	 *  @param host host
	 *  @param port port
	 *  @param shouldBenUsed true if it should be used
	 *  @return true if some server answered on port
	 */
	public boolean testPort (InetAddress host, int port, boolean shouldBenUsed)
	{
		Socket pingSocket = null;
		try
		{
			pingSocket = new Socket(host, port);
		}
		catch (Exception e)
		{
			if (shouldBenUsed)
				System.err.println("  Open Socket " + host + " on " + port + ": " + e.getMessage());
			return false;
		}
		if (!shouldBenUsed)
			System.err.println("  Open Socket " + host + " on " + port);
		if (pingSocket == null)
			return false;
		//	success
		try
		{
			pingSocket.close();
		}
		catch (IOException e)
		{
			System.out.println("  CloseSocket=" + e.toString());
		}
		return true;
	}	//	testPort

	/**
	 * 	Test JDBC Connection to Server
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testJDBC (String uid, String pwd)
	{
		//	jdbc:oracle:thin:@dev:1521:dev1
		m_connectionString = "jdbc:oracle:thin:@"
			+ m_databaseServer.getHostName() + ":" + m_databasePort + ":" + m_databaseName;
		System.out.println("  JDBC = " + m_connectionString);

		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(m_connectionString,
				uid, pwd);
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testJDBC

	/**
	 * 	Test TNS Connection
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testTNS (String uid, String pwd)
	{
		String connectionString = "jdbc:oracle:oci8:@" + m_TNSName;
		System.out.println("  TNS = " + connectionString);
		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(connectionString,
				uid, pwd);
		}
		catch (UnsatisfiedLinkError ule)
		{
			System.err.println("Check setup of Oracle Server / Oracle Client / LD_LIBRARY_PATH");
			System.err.println(ule.toString());
			return false;
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testTNS

	/**
	 * 	Test Mail
	 *  @return true of OK
	 */
	private boolean testMail()
	{
		boolean smtpOK = false;
		boolean imapOK = false;
		if (testPort (m_mailServer, 25, true))
		{
			System.out.println("OK: SMTP Server contacted");
			smtpOK = true;
		}
		else
			System.err.println("Error: SMTP Server NOT available");
		//
		if (testPort (m_mailServer, 110, true))
			System.out.println("OK: POP3 Server contacted");
		else
			System.err.println("Error: POP3 Server NOT available");
		if (testPort (m_mailServer, 143, true))
		{
			System.out.println("OK: IMAP4 Server contacted");
			imapOK = true;
		}
		else
			System.err.println("Error: IMAP4 Server NOT available");
		//
		if (!smtpOK)
			return false;
		//
		try
		{
			EMail em = new EMail (m_mailServer.getHostName (),
					   m_adminEMail.toString (), m_adminEMail.toString (),
					   "Compiere Server Setup Test", "Test: " + m_properties);
			em.setEMailUser (m_mailUser, m_mailPassword);
			if (EMail.SENT_OK.equals (em.send ()))
			{
				System.out.println ("OK: Send Test Email to " + m_adminEMail);
			}
			else
			{
				System.err.println ("Error: Could NOT send Email to "
					+ m_adminEMail);
			}
		}
		catch (Exception ex)
		{
			System.err.println("testMail - " + ex.getLocalizedMessage());
			return false;
		}

		//
		if (!imapOK)
			return false;

		//	Test Read Mail Access
		Properties props = new Properties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_mailServer.getHostName());
		props.put("mail.user", m_mailUser);
		props.put("mail.smtp.auth", "true");
		System.out.println("  Connecting to " + m_mailServer.getHostName());
		//
		Session session = null;
		Store store = null;
		try
		{
			EMailAuthenticator auth = new EMailAuthenticator (m_mailUser, m_mailPassword);
			session = Session.getDefaultInstance(props, auth);
			session.setDebug (Log.isTraceLevel (10));
			System.out.println("  Session=" + session);
			//	Connect to Store
			store = session.getStore("imap");
			System.out.println("  Store=" + store);
		}
		catch (NoSuchProviderException nsp)
		{
			System.err.println("Error Mail IMAP Provider - " + nsp.getMessage());
			return false;
		}
		catch (Exception e)
		{
			System.err.println("Error Mail IMAP - " + e.getMessage());
			return false;
		}
		try
		{
			store.connect(m_mailServer.getHostName(), m_mailUser, m_mailPassword);
			System.out.println("  Store - connected");
			Folder folder = store.getDefaultFolder();
			Folder inbox = folder.getFolder("INBOX");
			System.out.println("OK: Mail Connect to " + inbox.getFullName() + " #Msg=" + inbox.getMessageCount());
			//
			store.close();
		}
		catch (MessagingException mex)
		{
			System.err.println("Error Mail Connect " + mex.getMessage());
			return false;
		}
		return true;
	}	//	testMail

	/*************************************************************************/

	/**
	 * 	Save Settings
	 */
	private void save()
	{
		SwingWorker sw = startTest();
		while (sw.isAlive())
		{
			try
			{
				Thread.currentThread().sleep(2000);
			}
			catch (InterruptedException ex)
			{
				System.err.println("save-waiting: " + ex);
			}
		}
		sw.get();	//	block
		if (!m_success)
			return;

		//	Add
		m_properties.setProperty("COMPIERE_MAIN_VERSION", Compiere.MAIN_VERSION);
		m_properties.setProperty("COMPIERE_DATE_VERSION", Compiere.DATE_VERSION);
		m_properties.setProperty("COMPIERE_DB_VERSION", Compiere.DB_VERSION);



		//	Before we save, load Ini
		Ini.setClient(false);
		String fileName = m_compiereHome.getAbsolutePath() + File.separator + Ini.COMPIERE_PROPERTY_FILE;
		Ini.loadProperties(fileName);

		//	Save Environment
		fileName = m_compiereHome.getAbsolutePath() + File.separator + COMPIERE_ENV_FILE;
		try
		{
			FileOutputStream fos = new FileOutputStream(new File(fileName));
			m_properties.store(fos, COMPIERE_ENV_FILE);
			fos.flush();
			fos.close();
		}
		catch (Exception e)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + e.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (Throwable t)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + t.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		System.out.println("Properties saved to " + fileName);

		//	Sync Properties
		Ini.setCompiereHome(m_compiereHome.getAbsolutePath());
		CConnection cc = CConnection.get (Database.DB_ORACLE,
			m_databaseServer.getHostName(), m_databasePort, m_databaseName,
			m_databaseUser, m_databasePassword);
		cc.setAppsHost(m_appsServer.getHostName());
		cc.setRMIoverHTTP(false);
		Ini.setProperty(Ini.P_CONNECTION, cc.toStringLong());
		Ini.saveProperties(false);


		JOptionPane.showConfirmDialog(this, res.getString("EnvironmentSaved"),
			res.getString("CompiereServerSetup"),
			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

		((Frame)SwingUtilities.getWindowAncestor(this)).dispose();
		System.exit(0);		//	remains active when License Dialog called
	}	//	save

}	//	ConfigurationPanel
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2002 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.install;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.tools.ant.launch.Launcher;

import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;

import oracle.jdbc.*;
import org.compiere.*;
import org.compiere.apps.*;
import org.compiere.db.*;
import org.compiere.util.*;
import org.compiere.swing.*;


/**
 *	Configuration Panel
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: ConfigurationPanel.java,v 1.12 2004/03/05 05:59:55 jjanke Exp $
 */
public class ConfigurationPanel extends CPanel implements ActionListener
{
	/**
	 * 	Constructor
	 *  @param statusBar for info
	 * 	@throws Exception
	 */
	public ConfigurationPanel (JLabel statusBar) throws Exception
	{
		LogManager.resetConfiguration();
		Logger root = Logger.getRootLogger();
	//	root.addAppender(new ConsoleAppender(new PatternLayout("%d{ABSOLUTE} %-5p [%c{1}] %m%n")));
		root.addAppender(new ConsoleAppender(new LogLayout()));
		root.setLevel(Level.ALL);
		Log.setTraceLevel(10);
		//
		m_statusBar = statusBar;
		jbInit();
		dynInit();
	}	//	ConfigurationPanel

	public static final String	KEYSTORE_PASSWORD		= "myPassword";
	
	public static final String	COMPIERE_ENV_FILE		= "CompiereEnv.properties";

	public static final String	COMPIERE_HOME 			= "COMPIERE_HOME";
	public static final String	JAVA_HOME 				= "JAVA_HOME";
	public static final String	COMPIERE_JAVA_OPTIONS 	= "COMPIERE_JAVA_OPTIONS";

	public static final String	COMPIERE_APPS_SERVER 	= "COMPIERE_APPS_SERVER";
	public static final String	COMPIERE_JNP_PORT 		= "COMPIERE_JNP_PORT";
	public static final String	COMPIERE_WEB_PORT 		= "COMPIERE_WEB_PORT";
	public static final String	COMPIERE_SSL_PORT 		= "COMPIERE_SSL_PORT";
	public static final String	COMPIERE_WEB_ALIAS 		= "COMPIERE_WEB_ALIAS";
	
	public static final String	COMPIERE_KEYSTORE 		= "COMPIERE_KEYSTORE";
	public static final String	COMPIERE_KEYSTOREPASS	= "COMPIERE_KEYSTOREPASS";
	
	public static final String	COMPIERE_DB_SERVER 		= "COMPIERE_DB_SERVER";
	public static final String	COMPIERE_DB_PORT 		= "COMPIERE_DB_PORT";
	public static final String	COMPIERE_DB_NAME 		= "COMPIERE_DB_NAME";

	public static final String	COMPIERE_DB_USER 		= "COMPIERE_DB_USER";
	public static final String	COMPIERE_DB_PASSWORD 	= "COMPIERE_DB_PASSWORD";
	public static final String	COMPIERE_DB_SYSTEM 		= "COMPIERE_DB_SYSTEM";

	public static final String	COMPIERE_DB_URL 		= "COMPIERE_DB_URL";
	public static final String	COMPIERE_DB_TNS 		= "COMPIERE_DB_TNS";

	public static final String	COMPIERE_MAIL_SERVER 	= "COMPIERE_MAIL_SERVER";
	public static final String	COMPIERE_MAIL_USER 		= "COMPIERE_MAIL_USER";
	public static final String	COMPIERE_MAIL_PASSWORD 	= "COMPIERE_MAIL_PASSWORD";
	public static final String	COMPIERE_ADMIN_EMAIL	= "COMPIERE_ADMIN_EMAIL";

	public static final String	COMPIERE_FTP_SERVER		= "COMPIERE_FTP_SERVER";
	public static final String	COMPIERE_FTP_USER		= "COMPIERE_FTP_USER";
	public static final String	COMPIERE_FTP_PASSWORD	= "COMPIERE_FTP_PASSWORD";
	public static final String	COMPIERE_FTP_PREFIX		= "COMPIERE_FTP_PREFIX";

	private String			m_errorString;
	private File			m_javaHome;
	private File			m_compiereHome;
	private InetAddress		m_appsServer;
	private int				m_WebPort;
	private int				m_JNPPort;
	private InetAddress		m_databaseServer;
	private int				m_databasePort;
	private String			m_databaseName;
	private String			m_connectionString;
	private String			m_systemPassword;
	private String			m_databaseUser;
	private String			m_databasePassword;
	private String			m_TNSName;

	private InetAddress		m_mailServer;
	private InternetAddress	m_adminEMail;
	private String			m_mailUser;
	private String			m_mailPassword;

	private volatile boolean		m_success = false;
	private volatile boolean	 	m_testing = false;

	/** Translation				*/
	static ResourceBundle 		res = ResourceBundle.getBundle("org.compiere.install.SetupRes");

	/**	Driver					*/
	private static Driver		s_driver = null;
	/** Environment Properties	*/
	private Properties			m_properties = new Properties();
	/**	Setup Frame				*/
	private Setup				m_setup = null;
	/** Status Bar				*/
	private JLabel 				m_statusBar;

	private static ImageIcon iOpen = new ImageIcon(ConfigurationPanel.class.getResource("openFile.gif"));
	private static ImageIcon iSave = new ImageIcon(Compiere.class.getResource("images/Save16.gif"));
	private static ImageIcon iHelp = new ImageIcon(Compiere.class.getResource("images/Help16.gif"));


	//	-------------	Static UI
	private GridBagLayout gridBagLayout = new GridBagLayout();
	private CLabel lCompiereHome = new CLabel();
	private CTextField fCompiereHome = new CTextField();
	private CLabel lWebPort = new CLabel();
	private CTextField fWebPort = new CTextField();
	private CLabel lAppsServer = new CLabel();
	private CTextField fAppsServer = new CTextField();
	private CLabel lDatabaseType = new CLabel();
	private CComboBox fDatabaseType = new CComboBox();
	private CLabel lDatabaseName = new CLabel();
	private CLabel lDatabasePort = new CLabel();
	private CLabel lDatabaseUser = new CLabel();
	private CLabel lDatabasePassword = new CLabel();
	private CTextField fDatabaseName = new CTextField();
	private CTextField fDatabasePort = new CTextField();
	private CTextField fDatabaseUser = new CTextField();
	private CPassword fDatabasePassword = new CPassword();
	private CLabel lTNSName = new CLabel();
	private CComboBox fTNSName = new CComboBox();
	private CLabel lSystemPassword = new CLabel();
	private CPassword fSystemPassword = new CPassword();
	private CLabel lMailServer = new CLabel();
	private CTextField fMailServer = new CTextField();
	private CLabel lAdminEMail = new CLabel();
	private CTextField fAdminEMail = new CTextField();
	private CLabel lDatabaseServer = new CLabel();
	private CTextField fDatabaseServer = new CTextField();
	private CLabel lJavaHome = new CLabel();
	private CTextField fJavaHome = new CTextField();
	private CButton bCompiereHome = new CButton(iOpen);
	private CButton bJavaHome = new CButton(iOpen);
	private CButton bHelp = new CButton(iHelp);
	private CButton bTest = new CButton();
	private CButton bSave = new CButton(iSave);
	private CLabel lJNPPort = new CLabel();
	private CTextField fJNPPort = new CTextField();
	private CLabel lMailUser = new CLabel();
	private CLabel lMailPassword = new CLabel();
	private CTextField fMailUser = new CTextField();
	private CPassword fMailPassword = new CPassword();
	private CCheckBox okJavaHome = new CCheckBox();
	private CCheckBox okCompiereHome = new CCheckBox();
	private CCheckBox okAppsServer = new CCheckBox();
	private CCheckBox okWebPort = new CCheckBox();
	private CCheckBox okJNPPort = new CCheckBox();
	private CLabel lSSLPort = new CLabel();
	private CTextField fSSLPort = new CTextField();
	private CCheckBox okSSLPort = new CCheckBox();
	private CCheckBox okDatabaseServer = new CCheckBox();
	private CCheckBox okMailServer = new CCheckBox();
	private CCheckBox okMailUser = new CCheckBox();
	private CCheckBox okDatabaseUser = new CCheckBox();
	private CCheckBox okDatabaseName = new CCheckBox();
	private CLabel lKeyStore = new CLabel();
	private CPassword fKeyStore = new CPassword();
	private CCheckBox okKeyStore = new CCheckBox();
	private CCheckBox okTNSName = new CCheckBox();

	/**
	 * 	Static Layout Init
	 *  @throws Exception
	 */
	private void jbInit() throws Exception
	{
		lCompiereHome.setToolTipText(res.getString("CompiereHomeInfo"));
		lCompiereHome.setText(res.getString("CompiereHome"));
		this.setLayout(gridBagLayout);
		fCompiereHome.setText("C:\\Compiere2");
		lWebPort.setToolTipText(res.getString("WebPortInfo"));
		lWebPort.setText(res.getString("WebPort"));
		fWebPort.setText("80");
		lAppsServer.setToolTipText(res.getString("AppsServerInfo"));
		lAppsServer.setText(res.getString("AppsServer"));
		fAppsServer.setText("server.company.com");
		lDatabaseType.setToolTipText(res.getString("DatabaseTypeInfo"));
		lDatabaseType.setText(res.getString("DatabaseType"));
		lDatabaseName.setToolTipText(res.getString("DatabaseNameInfo"));
		lDatabaseName.setText(res.getString("DatabaseName"));
		lDatabasePort.setToolTipText(res.getString("DatabasePortInfo"));
		lDatabasePort.setText(res.getString("DatabasePort"));
		lDatabaseUser.setToolTipText(res.getString("DatabaseUserInfo"));
		lDatabaseUser.setText(res.getString("DatabaseUser"));
		lDatabasePassword.setToolTipText(res.getString("DatabasePasswordInfo"));
		lDatabasePassword.setText(res.getString("DatabasePassword"));
		fDatabaseName.setText("compiere");
		fDatabasePort.setText("1521");
		fDatabaseUser.setText("compiere");
		fDatabasePassword.setText("compiere");
		lTNSName.setToolTipText(res.getString("TNSNameInfo"));
		lTNSName.setText(res.getString("TNSName"));
		fTNSName.setEditable(true);
		lSystemPassword.setToolTipText(res.getString("SystemPasswordInfo"));
		lSystemPassword.setText(res.getString("SystemPassword"));
		fSystemPassword.setText("manager");
		lMailServer.setToolTipText(res.getString("MailServerInfo"));
		lMailServer.setText(res.getString("MailServer"));
		fMailServer.setText("mail.company.com");
		lAdminEMail.setToolTipText(res.getString("AdminEMailInfo"));
		lAdminEMail.setText(res.getString("AdminEMail"));
		fAdminEMail.setText("admin@company.com");
		bTest.setToolTipText(res.getString("TestInfo"));
		bTest.setText(res.getString("Test"));
		bSave.setToolTipText(res.getString("SaveInfo"));
		bSave.setText(res.getString("Save"));
		lDatabaseServer.setToolTipText(res.getString("DatabaseServerInfo"));
		lDatabaseServer.setText(res.getString("DatabaseServer"));
		lJavaHome.setToolTipText(res.getString("JavaHomeInfo"));
		lJavaHome.setText(res.getString("JavaHome"));
		fJavaHome.setText("Java142");
		lJNPPort.setToolTipText(res.getString("JNPPortInfo"));
		lJNPPort.setText(res.getString("JNPPort"));
		fJNPPort.setText("1099");
		bHelp.setToolTipText(res.getString("HelpInfo"));
		lMailUser.setToolTipText(res.getString("MailUserInfo"));
		lMailUser.setText(res.getString("MailUser"));
		lMailPassword.setToolTipText(res.getString("MailPasswordInfo"));
		lMailPassword.setText(res.getString("MailPassword"));
		fMailUser.setText("compiere");
		fMailPassword.setText("compiere");

		//
		lSSLPort.setText("SSL");
		fSSLPort.setText("443");
		okMailUser.setEnabled(false);
		lKeyStore.setText(res.getString("KeyStorePassword"));
		lKeyStore.setToolTipText(res.getString("KeyStorePasswordInfo"));
		okJavaHome.setEnabled(false);
		okCompiereHome.setEnabled(false);
		okCompiereHome.setDoubleBuffered(false);
		okAppsServer.setEnabled(false);
		okWebPort.setEnabled(false);
		okWebPort.setDoubleBuffered(false);
		okSSLPort.setEnabled(false);
		okJNPPort.setEnabled(false);
		fKeyStore.setText(KEYSTORE_PASSWORD);
		okDatabaseServer.setEnabled(false);
		okTNSName.setEnabled(false);
		okMailServer.setEnabled(false);
		okDatabaseName.setEnabled(false);
		okDatabaseUser.setEnabled(false);
		okKeyStore.setEnabled(false);
		bJavaHome.setMargin(new Insets(2, 10, 2, 10));
		bCompiereHome.setMaximumSize(new Dimension(43, 27));
		bCompiereHome.setMargin(new Insets(2, 10, 2, 10));
		this.add(lCompiereHome,                       new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fCompiereHome,                         new GridBagConstraints(1, 1, 2, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(lWebPort,                       new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fWebPort,                        new GridBagConstraints(1, 3, 4, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(lAppsServer,                      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fAppsServer,                       new GridBagConstraints(1, 2, 3, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(lDatabaseType,                       new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseType,                      new GridBagConstraints(1, 4, 5, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabasePort,                      new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseUser,                       new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseName,                         new GridBagConstraints(1, 6, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabasePort,                        new GridBagConstraints(1, 7, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseUser,                         new GridBagConstraints(1, 8, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lTNSName,                       new GridBagConstraints(7, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(fTNSName,                            new GridBagConstraints(8, 5, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(lMailServer,                      new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lSystemPassword,                       new GridBagConstraints(7, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fSystemPassword,                       new GridBagConstraints(8, 7, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(fDatabasePassword,                        new GridBagConstraints(8, 8, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 0), 0, 0));
		this.add(lDatabasePassword,                      new GridBagConstraints(7, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailServer,                      new GridBagConstraints(1, 9, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(bTest,                      new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 10, 5), 0, 0));
		this.add(bSave,                       new GridBagConstraints(8, 11, 2, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 10, 5), 0, 0));
		this.add(lDatabaseServer,                      new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseServer,                     new GridBagConstraints(1, 5, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseName,                   new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lJavaHome,                  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJavaHome,                        new GridBagConstraints(1, 0, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(bCompiereHome,                 new GridBagConstraints(7, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bHelp,                     new GridBagConstraints(7, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 10, 5), 0, 0));
		this.add(bJavaHome,                 new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lMailPassword,                  new GridBagConstraints(7, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailPassword,                    new GridBagConstraints(8, 10, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 0), 0, 0));
		this.add(lAdminEMail,               new GridBagConstraints(7, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailUser,               new GridBagConstraints(1, 10, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fAdminEMail,                 new GridBagConstraints(8, 9, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 0), 0, 0));
		this.add(lMailUser,              new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 0), 0, 0));
		this.add(lJNPPort,         new GridBagConstraints(7, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJNPPort,         new GridBagConstraints(8, 2, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(okJNPPort,          new GridBagConstraints(9, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(lSSLPort,      new GridBagConstraints(7, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fSSLPort,      new GridBagConstraints(8, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(okSSLPort,     new GridBagConstraints(9, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okDatabaseServer,         new GridBagConstraints(6, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okMailServer,      new GridBagConstraints(6, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 5, 5), 0, 0));
		this.add(okJavaHome,   new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okCompiereHome,  new GridBagConstraints(6, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okAppsServer,  new GridBagConstraints(6, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okWebPort,  new GridBagConstraints(6, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(lKeyStore,  new GridBagConstraints(7, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(fKeyStore,  new GridBagConstraints(8, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(okKeyStore,  new GridBagConstraints(9, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okTNSName, new GridBagConstraints(9, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		//
		bCompiereHome.addActionListener(this);
		bJavaHome.addActionListener(this);
		bHelp.addActionListener(this);
		bTest.addActionListener(this);
		bSave.addActionListener(this);
		this.add(okDatabaseName,  new GridBagConstraints(9, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okDatabaseUser,   new GridBagConstraints(9, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 5, 5), 0, 0));
		this.add(okMailUser,  new GridBagConstraints(9, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
	}	//	jbInit


	/**
	 * 	Dynamic Init
	 *  @throws Exception
	 */
	private void dynInit() throws Exception
	{
		//	DatabaseType
		fDatabaseType.addItem("Oracle 9i2");

		//	Compiere Home
		String ch = System.getProperty(COMPIERE_HOME);
		if (ch == null || ch.length() == 0)
			ch = System.getProperty("user.dir");
		fCompiereHome.setText(ch);
		boolean envLoaded = false;
		String fileName = ch + File.separator + COMPIERE_ENV_FILE;
		File env = new File (fileName);
		System.out.println(env + " - exists=" + env.exists());
		if (env.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(env);
				m_properties.load(fis);
				fis.close();
			}
			catch (Exception e)
			{
				System.err.println(e.toString());
			}
			System.out.println("Loading ...");
			if (m_properties.size() > 5)
				envLoaded = true;
			//
			fCompiereHome.setText((String)m_properties.get(COMPIERE_HOME));
			fJavaHome.setText((String)m_properties.get(JAVA_HOME));
			fAppsServer.setText((String)m_properties.get(COMPIERE_APPS_SERVER));
			fJNPPort.setText((String)m_properties.get(COMPIERE_JNP_PORT));
			fWebPort.setText((String)m_properties.get(COMPIERE_WEB_PORT));
			fSSLPort.setText((String)m_properties.get(COMPIERE_SSL_PORT));
			String s = (String)m_properties.get(COMPIERE_KEYSTOREPASS);
			if (s == null || s.length() == 0)
				s = KEYSTORE_PASSWORD;
			fKeyStore.setText(s);

			fDatabaseServer.setText((String)m_properties.get(COMPIERE_DB_SERVER));
			fDatabasePort.setText((String)m_properties.get(COMPIERE_DB_PORT));
			fDatabaseName.setText((String)m_properties.get(COMPIERE_DB_NAME));

			fDatabaseUser.setText((String)m_properties.get(COMPIERE_DB_USER));
			fDatabasePassword.setText((String)m_properties.get(COMPIERE_DB_PASSWORD));
			fSystemPassword.setText((String)m_properties.get(COMPIERE_DB_SYSTEM));
			fillTNSName((String)m_properties.get(COMPIERE_DB_TNS));

			fMailServer.setText((String)m_properties.get(COMPIERE_MAIL_SERVER));
			fMailUser.setText((String)m_properties.get(COMPIERE_MAIL_USER));
			fMailPassword.setText((String)m_properties.get(COMPIERE_MAIL_PASSWORD));
			fAdminEMail.setText((String)m_properties.get(COMPIERE_ADMIN_EMAIL));
		}

		//	No environment file found - defaults
		if (!envLoaded)
		{
			System.out.println("Setting Defaults");
			//	Java Home, e.g. D:\j2sdk1.4.1\jre
			String javaHome = System.getProperty("java.home");
			if (javaHome.endsWith("jre"))
				javaHome = javaHome.substring(0, javaHome.length()-4);
			fJavaHome.setText(javaHome);
			//	AppsServer
			fAppsServer.setText(InetAddress.getLocalHost().getHostName());
			//	Database Server
			fDatabaseServer.setText(InetAddress.getLocalHost().getHostName());
			//	Mail Server
			fMailServer.setText(InetAddress.getLocalHost().getHostName());
			//  Get TNS Names
			fillTNSName("");
		}	//	!envLoaded

		//	Default FTP stuff
		if (!m_properties.containsKey(COMPIERE_FTP_SERVER))
		{
			m_properties.setProperty(COMPIERE_FTP_SERVER, "localhost");
			m_properties.setProperty(COMPIERE_FTP_USER, "anonymous");
			m_properties.setProperty(COMPIERE_FTP_PASSWORD, "user@host.com");
			m_properties.setProperty(COMPIERE_FTP_PREFIX, "my");
		}
		//	Default Java Options
		if (!m_properties.containsKey(COMPIERE_JAVA_OPTIONS))
			m_properties.setProperty(COMPIERE_JAVA_OPTIONS, "-Xms64M -Xmx512M");
		//	Web Alias
		if (!m_properties.containsKey(COMPIERE_WEB_ALIAS))
			m_properties.setProperty(COMPIERE_WEB_ALIAS, InetAddress.getLocalHost().getCanonicalHostName());
		
		//	(String)m_properties.get(COMPIERE_DB_URL)	//	derived
	}	//	dynInit

	/**
	 * 	Fill TNS Name
	 * 	@param defaultValue defaultValue
	 */
	private void fillTNSName (String defaultValue)
	{
		//	default value to lowercase or null
		String def = defaultValue;
		if (def != null && def.trim().length() == 0)
			def = null;
		if (def != null)
		{
			def = def.toLowerCase();
			fTNSName.addItem(def);
		}

		//	Search for Oracle Info
		String path = System.getProperty("java.library.path");
		String[] entries = path.split(File.pathSeparator);
		for (int e = 0; e < entries.length; e++)
		{
			String entry = entries[e].toLowerCase();
			if (entry.indexOf("ora") != -1 && entry.endsWith("bin"))
			{
				StringBuffer sb = getTNS_File (entries[e].substring(0, entries[e].length()-4));
				String[] tnsnames = getTNS_Names (sb);
				if (tnsnames != null)
				{
					for (int i = 0; i < tnsnames.length; i++)
					{
						String tns = tnsnames[i];
						if (!tns.equals(def))
							fTNSName.addItem(tns);
					}
					break;
				}
			}
		}	//	for all path entries

		//	Set first entry
		fTNSName.addActionListener(this);
		if (fTNSName.getItemCount() > 0)
			fTNSName.setSelectedIndex(0);
	}	//	fillTNSName

	/**
	 * 	Get File tnmsnames.ora in StringBuffer
	 * 	@param oraHome ORACLE_HOME
	 * 	@return tnsnames.ora or null
	 */
	private StringBuffer getTNS_File (String oraHome)
	{
		String tnsnames = oraHome + File.separator
			+ "network" + File.separator
			+ "admin" + File.separator
			+ "tnsnames.ora";
		File tnsfile = new File (tnsnames);
		if (!tnsfile.exists())
			return null;

		System.out.println("Searching " + tnsnames);
		StringBuffer sb = new StringBuffer();
		try
		{
			FileReader fr = new FileReader (tnsfile);
			int c;
			while ((c = fr.read()) != -1)
				sb.append((char)c);
		}
		catch (IOException ex)
		{
			System.err.println("Error Reading " + tnsnames);
			ex.printStackTrace();
			return null;
		}
		if (sb.length() == 0)
			return null;
		return sb;
	}	//	getTNS_File

	/**
	 * 	Get TNS Names entries.
	 * 	Assumes standard tnsmanes.ora formatting of NetMgr
	 * 	@param tnsnames content of tnsnames.ora
	 * 	@return tns names or null
	 */
	private String[] getTNS_Names (StringBuffer tnsnames)
	{
		if (tnsnames == null)
			return null;

		ArrayList list = new ArrayList();
		Pattern pattern = Pattern.compile("$", Pattern.MULTILINE);
		String[] lines = pattern.split(tnsnames);
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i].trim();
			if (line.length() > 0
				&& Character.isLetter(line.charAt(0))	//	no # (
				&& line.indexOf("=") != -1
				&& line.indexOf("EXTPROC_") == -1
				&& line.indexOf("_HTTP") == -1)
			{
				String entry = line.substring(0, line.indexOf('=')).trim().toLowerCase();
				System.out.println("- " + entry);
				list.add(entry);
			}
		}
		//	Convert to Array
		if (list.size() == 0)
			return null;
		String[] retValue = new String[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getTNS_Names

	/**************************************************************************

	/**
	 * 	ActionListener
	 *  @param e event
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (m_testing)
			return;
		//	TNS Name Changed
		if (e.getSource() == fTNSName)
		{
			String tns = (String)fTNSName.getSelectedItem();
			if (tns != null)
			{
				int pos = tns.indexOf('.');
				if (pos != -1)
					tns = tns.substring(0, pos);
				fDatabaseName.setText(tns);
			}
		}

		else if (e.getSource() == bJavaHome)
			setPath (fJavaHome);
		else if (e.getSource() == bCompiereHome)
			setPath (fCompiereHome);
		else if (e.getSource() == bHelp)
			new Setup_Help((Frame)SwingUtilities.getWindowAncestor(this));
		else if (e.getSource() == bTest)
			startTest(false);
		else if (e.getSource() == bSave)
			startTest(true);
	}	//	actionPerformed

	/**
	 * 	Set Path in Field
	 * 	@param field field to set Path
	 */
	private void setPath (CTextField field)
	{
		JFileChooser fc = new JFileChooser(field.getText());
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			field.setText(fc.getSelectedFile().getAbsolutePath());
	}	//	setPath

	/**************************************************************************

	/**
	 * 	Start Test Async.
	 *  @return SwingWorker
	 */
	private SwingWorker startTest(final boolean saveIt)
	{
		SwingWorker worker = new SwingWorker()
		{
			//	Start it
			public Object construct()
			{
				m_testing = true;
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				bTest.setEnabled(false);
				m_success = false;
				m_errorString = null;
				try
				{
					test();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					m_errorString += "\n" + ex.toString();
				}
				//
				setCursor(Cursor.getDefaultCursor());
				if (m_errorString == null)
					m_success = true;
				bTest.setEnabled(true);
				m_testing = false;
				return new Boolean(m_success);
			}
			//	Finish it
			public void finished()
			{
				if (m_errorString != null)
				{
					System.err.println(m_errorString);
					JOptionPane.showConfirmDialog (null, m_errorString, res.getString("ServerError"),
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
				}
				else if (saveIt)
					save();
			}
		};
		worker.start();
		return worker;
	}	//	startIt

	/**
	 *	Test it
	 * 	@throws Exception
	 */
	private void test() throws Exception
	{
		System.out.println();
		System.out.println("** Test **");
		bSave.setEnabled(false);


		//	Java Home
		m_errorString = res.getString("ErrorJavaHome");
		m_javaHome = new File (fJavaHome.getText());
		if (!signalOK(okJavaHome, m_javaHome.exists(), true, "Not found: Java Home"))
			return;
		File tools = new File (fJavaHome.getText() + File.separator + "lib" + File.separator + "tools.jar");
		if (!signalOK(okJavaHome, tools.exists(), true, 
				"Not found: Java SDK = " + tools))
			return;
		System.out.println("OK: JavaHome=" + m_javaHome.getAbsolutePath());
		m_properties.setProperty(JAVA_HOME, m_javaHome.getAbsolutePath());
		System.setProperty(JAVA_HOME, m_javaHome.getAbsolutePath());
		
		
		//	Java Version
		final String VERSION = "1.4.1";
		final String VERSION2 = "1.4.2";
		boolean versionOK = false;
		String jh = m_javaHome.getAbsolutePath();
		if (jh.indexOf(VERSION) != -1)	//	file name has version = assuming OK
			versionOK = true;
		if (!versionOK && jh.indexOf(VERSION2) != -1)	//
			versionOK = true;
		String thisJH = System.getProperty("java.home");
		if (thisJH.indexOf(jh) != -1)	//	we are running the version currently
		{
			String thisJV = System.getProperty("java.version");
			versionOK = thisJV.indexOf(VERSION) != -1;
			if (!versionOK && thisJV.indexOf(VERSION2) != -1)
				versionOK = true;
			if (versionOK)
			  System.out.println("  Java Version OK = " + thisJV);
		}
		if (!signalOK(okJavaHome, versionOK, true, 
				"Wrong Java Version: Should be " + VERSION2))
			return;

		
		//	Compiere Home
		m_errorString = res.getString("ErrorCompiereHome");
		m_compiereHome = new File (fCompiereHome.getText());
		if (!signalOK(okCompiereHome, m_compiereHome.exists(), true, "Not found: CompiereHome = " + m_compiereHome))
			return;
		System.out.println("OK: CompiereHome = " + m_compiereHome);
		m_properties.setProperty(COMPIERE_HOME, m_compiereHome.getAbsolutePath());
		System.setProperty(COMPIERE_HOME, m_compiereHome.getAbsolutePath());
		

		//	AppsServer
		m_errorString = res.getString("ErrorAppsServer");
		String server = fAppsServer.getText();
		boolean error = (server == null || server.length() == 0 
			|| server.toLowerCase().indexOf("localhost") != -1 || server.equals("127.0.0.1"));
		if (!error)
			m_appsServer = InetAddress.getByName(server);
		if (!signalOK(okAppsServer, !error, true, 
				"Not correct: AppsServer = " + server))
			return;
		System.out.println("OK: AppsServer = " + m_appsServer.getHostName());
		m_properties.setProperty(COMPIERE_APPS_SERVER, m_appsServer.getHostName());

		
		//	JNP Port
		m_errorString = res.getString("ErrorJNPPort");
		m_statusBar.setText(lJNPPort.getText());
		m_JNPPort = Integer.parseInt(fJNPPort.getText());
		error = (testPort (m_appsServer, m_JNPPort, false) || !testServerPort(m_JNPPort));
		if (!signalOK(okJNPPort, !error, true, "Not correct: JNP Port = " + m_JNPPort))
			return;
		System.out.println("OK: JNPPort = " + m_JNPPort);
		m_properties.setProperty(COMPIERE_JNP_PORT, String.valueOf(m_JNPPort));


		//	Web Port
		m_errorString = res.getString("ErrorWebPort");
		m_statusBar.setText(lWebPort.getText());
		m_WebPort = Integer.parseInt(fWebPort.getText());
		error = (testPort ("http", m_appsServer.getHostName(), m_WebPort, "/") 
			|| !testServerPort(m_WebPort));
		if (!signalOK(okWebPort, !error, true, 
				"Not correct: Web Port = " + m_WebPort))
			return;
		System.out.println("OK: Web Port = " + m_WebPort);
		m_properties.setProperty(COMPIERE_WEB_PORT, String.valueOf(m_WebPort));
		
		//	SSL Port
		int sslPort = Integer.parseInt(fSSLPort.getText());
		error = (testPort ("https", m_appsServer.getHostName(), sslPort, "/") 
			|| !testServerPort(sslPort));
		if (!signalOK(okSSLPort, !error, true, 
				"Not correct: SSL Port = " + sslPort))
			return;
		System.out.println("OK: SSL Port = " + sslPort);
		m_properties.setProperty(COMPIERE_SSL_PORT, String.valueOf(sslPort));

		
		//	KeyStore
		m_errorString = res.getString("KeyStorePassword");
		String fileName = MyKeyStore.getKeystoreFileName(m_compiereHome.getAbsolutePath());
		m_properties.setProperty(COMPIERE_KEYSTORE, fileName);
		
		//	KeyStore Password
		String pw = new String(fKeyStore.getPassword());
		if (!signalOK(okKeyStore, pw != null && pw.length() > 0, true, 
				"Invalid Key Store Password = " + pw))
			return;
		m_properties.setProperty(COMPIERE_KEYSTOREPASS, pw);
		MyKeyStore ks = new MyKeyStore (fileName, fKeyStore.getPassword());
		String errorString = ks.verify();
		if (!signalOK(okKeyStore, errorString == null, true, errorString))
			return;
		System.out.println("OK: KeyStore = " + fileName);
		
		
		//	Database Server
		m_errorString = res.getString("ErrorDatabaseServer");
		m_statusBar.setText(lDatabaseServer.getText());
		server = fDatabaseServer.getText();
		error = (server == null || server.length() == 0 
			|| server.toLowerCase().indexOf("localhost") != -1 || server.equals("127.0.0.1"));
		if (!error)
			m_databaseServer = InetAddress.getByName(server);
		if (!signalOK(okDatabaseServer, !error, true, 
				"Not correct: DB Server = " + server))
			return;
		System.out.println("OK: Database Server = " + m_databaseServer.getHostName());
		m_properties.setProperty(COMPIERE_DB_SERVER, m_databaseServer.getHostName());


		//	Database Port
		m_errorString = res.getString("ErrorDatabasePort");
		m_databasePort = Integer.parseInt(fDatabasePort.getText());
		if (!signalOK(okDatabaseServer, testPort (m_databaseServer, m_databasePort, true), true, 
				"Not correct: DB Server Port = " + m_databasePort))
			return;
		System.out.println("OK: Database Port = " + m_databasePort);
		m_properties.setProperty(COMPIERE_DB_PORT, String.valueOf(m_databasePort));


		//	JDBC Database Info
		m_errorString = res.getString("ErrorJDBC");
		m_statusBar.setText(lDatabaseName.getText());
		m_databaseName = fDatabaseName.getText();	//	SID
		m_systemPassword = new String(fSystemPassword.getPassword());
		if (!signalOK(okDatabaseName, m_systemPassword != null && m_systemPassword.length() > 0, true, 
				"Invalid Password"))
			return;
		if (!signalOK(okDatabaseName, testJDBC("system", m_systemPassword), true,
				"Error connecting to Database: " + m_databaseName 
				+ " as system/" + m_systemPassword))
			return;
		System.out.println("OK: Database Name = " + m_databaseName);
		m_properties.setProperty(COMPIERE_DB_NAME, m_databaseName);
		System.out.println("OK: Database SystemID = system/" + m_systemPassword);
		m_properties.setProperty(COMPIERE_DB_SYSTEM, m_systemPassword);

		//	URL (derived)	jdbc:oracle:thin:@prod1:1521:prod1
		StringBuffer url = new StringBuffer("jdbc:oracle:thin:@")
			.append(m_databaseServer.getHostName()).append(":")
			.append(m_databasePort).append(":").append(m_databaseName);
		System.out.println("OK: Database URL = " + url.toString());
		m_properties.setProperty(COMPIERE_DB_URL, url.toString());


		//	Database User Info
		m_databaseUser = fDatabaseUser.getText();	//	UID
		m_databasePassword = new String(fDatabasePassword.getPassword());	//	PWD
		if (!signalOK(okDatabaseUser, m_databasePassword != null && m_databasePassword.length() > 0, true, 
				"Invalid Password"))
			return;
		//	Ignore result as it might not be imported
		if (signalOK(okDatabaseUser, testJDBC(m_databaseUser, m_databasePassword), false,
				"Not created yet for Database: " + m_databaseName 
				+ " User: " + m_databaseUser + "/" + m_databasePassword))
			System.out.println("OK: Database UserID = " + m_databaseUser + "/" + m_databasePassword);
		m_properties.setProperty(COMPIERE_DB_USER, m_databaseUser);
		m_properties.setProperty(COMPIERE_DB_PASSWORD, m_databasePassword);


		//	TNS Name Info
		m_errorString = res.getString("ErrorTNS");
		m_statusBar.setText(lTNSName.getText());
		m_TNSName = (String)fTNSName.getSelectedItem();
		if (!signalOK(okTNSName, testTNS("system", m_systemPassword), true,
				"Error connecting to Database: " + m_databaseName 
				+ " via TNS Name: " + m_TNSName))
			return;
		System.out.println("OK: Database TNS Name = " + m_TNSName);
		m_properties.setProperty(COMPIERE_DB_TNS, m_TNSName);


		//	Mail Server
		m_errorString = res.getString("ErrorMailServer");
		m_statusBar.setText(lMailServer.getText());
		server = fMailServer.getText();
		error = (server == null || server.length() == 0 
				|| server.toLowerCase().indexOf("localhost") != -1 || server.equals("127.0.0.1"));
		if (!error)
			m_mailServer = InetAddress.getByName(server);
		if (!signalOK(okMailServer, !error, true,
				"Error Mail Server = " + server))
			return;
		System.out.println("OK: Mail Server = " + m_mailServer);
		m_properties.setProperty(COMPIERE_MAIL_SERVER, m_mailServer.getHostName());


		//	Mail User
		m_errorString = "ErrorMailUser";
		m_statusBar.setText(lMailUser.getText());
		m_mailUser = fMailUser.getText();
		m_mailPassword = new String(fMailPassword.getPassword());
		m_properties.setProperty(COMPIERE_MAIL_USER, m_mailUser);
		m_properties.setProperty(COMPIERE_MAIL_PASSWORD, m_mailPassword);
		System.out.println("  Mail User = " + m_mailUser + "/" + m_mailPassword);

		//	Mail Address
		m_errorString = res.getString("ErrorMail");
		m_adminEMail = new InternetAddress (fAdminEMail.getText());
		//
		if (signalOK(okMailUser, testMail(), false, 
				"Not verified Admin EMail = " + m_adminEMail))
			System.out.println("OK: Admin EMail = " + m_adminEMail);
		m_properties.setProperty(COMPIERE_ADMIN_EMAIL, m_adminEMail.toString());

		//
		m_statusBar.setText(res.getString("Ok"));
		System.out.println("** Test OK **");
		bSave.setEnabled(true);
		m_errorString = null;
	}	//	test

	/**
	 * 	Signal OK
	 *	@param cb ckeck box
	 *	@param pass trus if test passed
	 *	@param critical true if critial
	 *	@param srrorMsg error Message
	 *	@return pass
	 */
	private boolean signalOK (CCheckBox cb, boolean pass, boolean critical, String errorMsg)
	{
		cb.setSelected(pass);
		if (pass)
			cb.setToolTipText(null);
		else
		{
			cb.setToolTipText(errorMsg);
			if (critical)
				System.err.println(errorMsg);
			else
				System.out.println(errorMsg);
			m_errorString += " \n(" + errorMsg + ")";
		}
		if (!pass && critical)
			cb.setBackground(Color.RED);
		else
			cb.setBackground(Color.GREEN);
		return pass;
	}	//	setOK

	/**
	 * 	Test Apps Server Port (client perspective)
	 *  @param protocol protocol (http, ..)
	 *  @param server server name
	 *  @param port port
	 *  @param file file name
	 *  @return true if able to connect
	 */
	private boolean testPort (String protocol, String server, int port, String file)
	{
		URL url = null;
		try
		{
			url = new URL (protocol, server, port, file);
		}
		catch (MalformedURLException ex)
		{
			ex.printStackTrace();
			return false;
		}
		System.out.println("  URL=" + url);
		try
		{
			URLConnection c = url.openConnection();
			Object o = c.getContent();
			System.err.println("  URL Connection in use=" + url);	//	error
		}
		catch (Exception ex)
		{
			return false;
		}
		return true;
	}	//	testPort

	/**
	 * 	Test Server Port
	 *  @param port port
	 *  @return true if able to create
	 */
	private boolean testServerPort (int port)
	{
		try
		{
			ServerSocket ss = new ServerSocket (port);
			System.out.println("  ServerPort " + ss.getInetAddress() + ":" + ss.getLocalPort());
			ss.close();
		}
		catch (Exception ex)
		{
			System.err.println(ex);
			return false;
		}
		return true;
	}	//	testPort


	/**
	 * 	Test Port
	 *  @param host host
	 *  @param port port
	 *  @param shouldBeUsed true if it should be used
	 *  @return true if some server answered on port
	 */
	public boolean testPort (InetAddress host, int port, boolean shouldBeUsed)
	{
		Socket pingSocket = null;
		try
		{
			pingSocket = new Socket(host, port);
		}
		catch (Exception e)
		{
			if (shouldBeUsed)
				System.err.println("  Open Socket " + host + " on " + port + ": " + e.getMessage());
			return false;
		}
		if (!shouldBeUsed)
			System.err.println("  Open Socket " + host + " on " + port);
		if (pingSocket == null)
			return false;
		//	success
		try
		{
			pingSocket.close();
		}
		catch (IOException e)
		{
			System.out.println("  CloseSocket=" + e.toString());
		}
		return true;
	}	//	testPort

	/**
	 * 	Test JDBC Connection to Server
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testJDBC (String uid, String pwd)
	{
		//	jdbc:oracle:thin:@dev:1521:dev1
		m_connectionString = "jdbc:oracle:thin:@"
			+ m_databaseServer.getHostName() + ":" + m_databasePort + ":" + m_databaseName;
		System.out.println("  JDBC = " + m_connectionString);

		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(m_connectionString,
				uid, pwd);
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testJDBC

	/**
	 * 	Test TNS Connection
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testTNS (String uid, String pwd)
	{
		String connectionString = "jdbc:oracle:oci8:@" + m_TNSName;
		System.out.println("  TNS = " + connectionString);
		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(connectionString,
				uid, pwd);
		}
		catch (UnsatisfiedLinkError ule)
		{
			System.err.println("Check setup of Oracle Server / Oracle Client / LD_LIBRARY_PATH");
			System.err.println(ule.toString());
			return false;
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testTNS

	/**
	 * 	Test Mail
	 *  @return true of OK
	 */
	private boolean testMail()
	{
		boolean smtpOK = false;
		boolean imapOK = false;
		if (testPort (m_mailServer, 25, true))
		{
			System.out.println("OK: SMTP Server contacted");
			smtpOK = true;
		}
		else
			System.err.println("Error: SMTP Server NOT available");
		//
		if (testPort (m_mailServer, 110, true))
			System.out.println("OK: POP3 Server contacted");
		else
			System.err.println("Error: POP3 Server NOT available");
		if (testPort (m_mailServer, 143, true))
		{
			System.out.println("OK: IMAP4 Server contacted");
			imapOK = true;
		}
		else
			System.err.println("Error: IMAP4 Server NOT available");
		//
		if (!smtpOK)
		{
			signalOK (okMailServer, false, false, "No active Mail Server");
			return false;
		}
		//
		try
		{
			EMail em = new EMail (m_mailServer.getHostName (),
					   m_adminEMail.toString (), m_adminEMail.toString (),
					   "Compiere Server Setup Test", "Test: " + m_properties);
			em.setEMailUser (m_mailUser, m_mailPassword);
			if (EMail.SENT_OK.equals (em.send ()))
			{
				System.out.println ("OK: Send Test Email to " + m_adminEMail);
			}
			else
			{
				System.err.println ("Error: Could NOT send Email to "
					+ m_adminEMail);
			}
		}
		catch (Exception ex)
		{
			System.err.println("testMail - " + ex.getLocalizedMessage());
			return false;
		}

		//
		if (!imapOK)
			return false;

		//	Test Read Mail Access
		Properties props = new Properties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_mailServer.getHostName());
		props.put("mail.user", m_mailUser);
		props.put("mail.smtp.auth", "true");
		System.out.println("  Connecting to " + m_mailServer.getHostName());
		//
		Session session = null;
		Store store = null;
		try
		{
			EMailAuthenticator auth = new EMailAuthenticator (m_mailUser, m_mailPassword);
			session = Session.getDefaultInstance(props, auth);
			session.setDebug (Log.isTraceLevel (10));
			System.out.println("  Session=" + session);
			//	Connect to Store
			store = session.getStore("imap");
			System.out.println("  Store=" + store);
		}
		catch (NoSuchProviderException nsp)
		{
			System.err.println("Error Mail IMAP Provider - " + nsp.getMessage());
			return false;
		}
		catch (Exception e)
		{
			System.err.println("Error Mail IMAP - " + e.getMessage());
			return false;
		}
		try
		{
			store.connect(m_mailServer.getHostName(), m_mailUser, m_mailPassword);
			System.out.println("  Store - connected");
			Folder folder = store.getDefaultFolder();
			Folder inbox = folder.getFolder("INBOX");
			System.out.println("OK: Mail Connect to " + inbox.getFullName() + " #Msg=" + inbox.getMessageCount());
			//
			store.close();
		}
		catch (MessagingException mex)
		{
			System.err.println("Error Mail Connect " + mex.getMessage());
			return false;
		}
		return true;
	}	//	testMail

	/**************************************************************************
	 * 	Save Settings.
	 * 	Called from startTest.finished()
	 */
	private void save()
	{
		if (!m_success)
			return;

		bSave.setEnabled(false);
		bTest.setEnabled(false);
		
		//	Add
		m_properties.setProperty("COMPIERE_MAIN_VERSION", Compiere.MAIN_VERSION);
		m_properties.setProperty("COMPIERE_DATE_VERSION", Compiere.DATE_VERSION);
		m_properties.setProperty("COMPIERE_DB_VERSION", Compiere.DB_VERSION);



		//	Before we save, load Ini
		Ini.setClient(false);
		String fileName = m_compiereHome.getAbsolutePath() + File.separator + Ini.COMPIERE_PROPERTY_FILE;
		Ini.loadProperties(fileName);

		//	Save Environment
		fileName = m_compiereHome.getAbsolutePath() + File.separator + COMPIERE_ENV_FILE;
		try
		{
			FileOutputStream fos = new FileOutputStream(new File(fileName));
			m_properties.store(fos, COMPIERE_ENV_FILE);
			fos.flush();
			fos.close();
		}
		catch (Exception e)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + e.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (Throwable t)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + t.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		System.out.println("Properties saved to " + fileName);

		//	Sync Properties
		Ini.setCompiereHome(m_compiereHome.getAbsolutePath());
		CConnection cc = CConnection.get (Database.DB_ORACLE,
			m_databaseServer.getHostName(), m_databasePort, m_databaseName,
			m_databaseUser, m_databasePassword);
		cc.setAppsHost(m_appsServer.getHostName());
		cc.setRMIoverHTTP(false);
		Ini.setProperty(Ini.P_CONNECTION, cc.toStringLong());
		Ini.saveProperties(false);
		
		//	Final Info
		JOptionPane.showConfirmDialog(this, res.getString("EnvironmentSaved"),
			res.getString("CompiereServerSetup"),
			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

		//	Run Ant
		try
		{
			System.out.println("Starting Ant ... ");
			System.setProperty("ant.home", ".");
			String[] 	args = new String[] {"setup"};
			Launcher.main (args);	//	calls System.exit
		}
		catch (Exception e)
		{
		}
			
		//	To be sure
		((Frame)SwingUtilities.getWindowAncestor(this)).dispose();
		System.exit(0);		//	remains active when License Dialog called
	}	//	save

}	//	ConfigurationPanel
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2002 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.install;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.sql.*;
import oracle.jdbc.*;

import javax.mail.*;
import javax.mail.internet.*;

import org.compiere.Compiere;
import org.compiere.apps.SwingWorker;
import org.compiere.db.*;
import org.compiere.util.*;


/**
 *	Configuration Panel
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: ConfigurationPanel.java,v 1.14 2003/02/21 06:37:54 jjanke Exp $
 */
public class ConfigurationPanel extends JPanel
	implements ActionListener
{
	/**
	 * 	Constructor
	 *  @param statusBar for info
	 * 	@throws Exception
	 */
	public ConfigurationPanel(JLabel statusBar) throws Exception
	{
		m_statusBar = statusBar;
		jbInit();
		dynInit();
	}	//	ConfigurationPanel

	public static final String	COMPIERE_ENV_FILE	= "CompiereEnv.properties";

	public static final String	COMPIERE_HOME 		= "COMPIERE_HOME";
	public static final String	JAVA_HOME 			= "JAVA_HOME";
	public static final String	COMPIERE_JAVA_OPTIONS = "COMPIERE_JAVA_OPTIONS";

	public static final String	COMPIERE_APPS_SERVER 	= "COMPIERE_APPS_SERVER";
	public static final String	COMPIERE_JNP_PORT 	= "COMPIERE_JNP_PORT";
	public static final String	COMPIERE_WEB_PORT 	= "COMPIERE_WEB_PORT";
	public static final String	COMPIERE_SSL_PORT 	= "COMPIERE_SSL_PORT";

	public static final String	COMPIERE_DB_SERVER 	= "COMPIERE_DB_SERVER";
	public static final String	COMPIERE_DB_PORT 	= "COMPIERE_DB_PORT";
	public static final String	COMPIERE_DB_NAME 	= "COMPIERE_DB_NAME";

	public static final String	COMPIERE_DB_USER 	= "COMPIERE_DB_USER";
	public static final String	COMPIERE_DB_PASSWORD = "COMPIERE_DB_PASSWORD";
	public static final String	COMPIERE_DB_SYSTEM 	= "COMPIERE_DB_SYSTEM";

	public static final String	COMPIERE_DB_URL 	= "COMPIERE_DB_URL";
	public static final String	COMPIERE_DB_TNS 	= "COMPIERE_DB_TNS";

	public static final String	COMPIERE_MAIL_SERVER 	= "COMPIERE_MAIL_SERVER";
	public static final String	COMPIERE_MAIL_USER 		= "COMPIERE_MAIL_USER";
	public static final String	COMPIERE_MAIL_PASSWORD 	= "COMPIERE_MAIL_PASSWORD";
	public static final String	COMPIERE_ADMIN_EMAIL	= "COMPIERE_ADMIN_EMAIL";

	public static final String	COMPIERE_FTP_SERVER		= "COMPIERE_FTP_SERVER";
	public static final String	COMPIERE_FTP_USER		= "COMPIERE_FTP_USER";
	public static final String	COMPIERE_FTP_PASSWORD	= "COMPIERE_FTP_PASSWORD";
	public static final String	COMPIERE_FTP_PREFIX		= "COMPIERE_FTP_PREFIX";

	private String			m_errorString;
	private File			m_javaHome;
	private File			m_compiereHome;
	private InetAddress		m_appsServer;
	private int				m_WebPort;
	private int				m_JNPPort;
	private InetAddress		m_databaseServer;
	private int				m_databasePort;
	private String			m_databaseName;
	private String			m_connectionString;
	private String			m_systemPassword;
	private String			m_databaseUser;
	private String			m_databasePassword;
	private String			m_TNSName;

	private InetAddress		m_mailServer;
	private InternetAddress	m_adminEMail;
	private String			m_mailUser;
	private String			m_mailPassword;

	private volatile boolean		m_success = false;
	private volatile boolean	 	m_testing = false;

	/** Translation				*/
	static ResourceBundle 		res = ResourceBundle.getBundle("org.compiere.install.SetupRes");

	/**	Driver					*/
	private static Driver		s_driver = null;
	/** Environment Properties	*/
	private Properties			m_properties = new Properties();
	/** Status Bar				*/
	private JLabel 				m_statusBar;

	private static ImageIcon iOpen = new ImageIcon(ConfigurationPanel.class.getResource("openFile.gif"));
	private static ImageIcon iSave = new ImageIcon(Compiere.class.getResource("images/Save16.gif"));
	private static ImageIcon iHelp = new ImageIcon(Compiere.class.getResource("images/Help16.gif"));


	//	-------------	Static UI
	private GridBagLayout gridBagLayout = new GridBagLayout();
	private JLabel lCompiereHome = new JLabel();
	private JTextField fCompiereHome = new JTextField();
	private JLabel lWebPort = new JLabel();
	private JTextField fWebPort = new JTextField();
	private JLabel lAppsServer = new JLabel();
	private JTextField fAppsServer = new JTextField();
	private JLabel lDatabaseType = new JLabel();
	private JComboBox fDatabaseType = new JComboBox();
	private JLabel lDatabaseName = new JLabel();
	private JLabel lDatabasePort = new JLabel();
	private JLabel lDatabaseUser = new JLabel();
	private JLabel lDatabasePassword = new JLabel();
	private JTextField fDatabaseName = new JTextField();
	private JTextField fDatabasePort = new JTextField();
	private JTextField fDatabaseUser = new JTextField();
	private JPasswordField fDatabasePassword = new JPasswordField();
	private JLabel lTNSName = new JLabel();
	private JComboBox fTNSName = new JComboBox();
	private JLabel lSystemPassword = new JLabel();
	private JPasswordField fSystemPassword = new JPasswordField();
	private JLabel lMailServer = new JLabel();
	private JTextField fMailServer = new JTextField();
	private JLabel lAdminEMail = new JLabel();
	private JTextField fAdminEMail = new JTextField();
	private JLabel lDatabaseServer = new JLabel();
	private JTextField fDatabaseServer = new JTextField();
	private JLabel lJavaHome = new JLabel();
	private JTextField fJavaHome = new JTextField();
	private JButton bCompiereHome = new JButton(iOpen);
	private JButton bJavaHome = new JButton(iOpen);
	private JButton bHelp = new JButton(iHelp);
	private JButton bTest = new JButton();
	private JButton bSave = new JButton(iSave);
	private JLabel lJNPPort = new JLabel();
	private JTextField fJNPPort = new JTextField();
	private JLabel lMailUser = new JLabel();
	private JLabel lMailPassword = new JLabel();
	private JTextField fMailUser = new JTextField();
	private JPasswordField fMailPassword = new JPasswordField();


	/**
	 * 	Static Layout Init
	 *  @throws Exception
	 */
	private void jbInit() throws Exception
	{
		lCompiereHome.setToolTipText(res.getString("CompiereHomeInfo"));
		lCompiereHome.setText(res.getString("CompiereHome"));
		this.setLayout(gridBagLayout);
		fCompiereHome.setText("D:\\Compiere2");
		lWebPort.setToolTipText(res.getString("WebPortInfo"));
		lWebPort.setText(res.getString("WebPort"));
		fWebPort.setText("80");
		lAppsServer.setToolTipText(res.getString("AppsServerInfo"));
		lAppsServer.setText(res.getString("AppsServer"));
		fAppsServer.setText("server.company.com");
		lDatabaseType.setToolTipText(res.getString("DatabaseTypeInfo"));
		lDatabaseType.setText(res.getString("DatabaseType"));
		lDatabaseName.setToolTipText(res.getString("DatabaseNameInfo"));
		lDatabaseName.setText(res.getString("DatabaseName"));
		lDatabasePort.setToolTipText(res.getString("DatabasePortInfo"));
		lDatabasePort.setText(res.getString("DatabasePort"));
		lDatabaseUser.setToolTipText(res.getString("DatabaseUserInfo"));
		lDatabaseUser.setText(res.getString("DatabaseUser"));
		lDatabasePassword.setToolTipText(res.getString("DatabasePasswordInfo"));
		lDatabasePassword.setText(res.getString("DatabasePassword"));
		fDatabaseName.setText("compiere");
		fDatabasePort.setText("1521");
		fDatabaseUser.setText("compiere");
		fDatabasePassword.setText("compiere");
		lTNSName.setToolTipText(res.getString("TNSNameInfo"));
		lTNSName.setText(res.getString("TNSName"));
		fTNSName.setEditable(true);
		lSystemPassword.setToolTipText(res.getString("SystemPasswordInfo"));
		lSystemPassword.setText(res.getString("SystemPassword"));
		fSystemPassword.setText("manager");
		lMailServer.setToolTipText(res.getString("MailServerInfo"));
		lMailServer.setText(res.getString("MailServer"));
		fMailServer.setText("mail.company.com");
		lAdminEMail.setToolTipText(res.getString("AdminEMailInfo"));
		lAdminEMail.setText(res.getString("AdminEMail"));
		fAdminEMail.setText("admin@company.com");
		bTest.setToolTipText(res.getString("TestInfo"));
		bTest.setText(res.getString("Test"));
		bSave.setToolTipText(res.getString("SaveInfo"));
		bSave.setText(res.getString("Save"));
		lDatabaseServer.setToolTipText(res.getString("DatabaseServerInfo"));
		lDatabaseServer.setText(res.getString("DatabaseServer"));
		lJavaHome.setToolTipText(res.getString("JavaHomeInfo"));
		lJavaHome.setText(res.getString("JavaHome"));
		fJavaHome.setText("Java141");
		lJNPPort.setToolTipText(res.getString("JNPPortInfo"));
		lJNPPort.setText(res.getString("JNPPort"));
		fJNPPort.setText("1099");
		bHelp.setToolTipText(res.getString("HelpInfo"));
		lMailUser.setToolTipText(res.getString("MailUserInfo"));
		lMailUser.setText(res.getString("MailUser"));
		lMailPassword.setToolTipText(res.getString("MailPasswordInfo"));
		lMailPassword.setText(res.getString("MailPassword"));
		fMailUser.setText("compiere");
		fMailPassword.setText("compiere");
		this.add(lCompiereHome,           new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fCompiereHome,             new GridBagConstraints(1, 1, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lWebPort,           new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fWebPort,            new GridBagConstraints(1, 3, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lAppsServer,          new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fAppsServer,           new GridBagConstraints(1, 2, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseType,           new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseType,          new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabasePort,          new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseUser,           new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseName,             new GridBagConstraints(1, 6, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabasePort,            new GridBagConstraints(1, 7, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseUser,             new GridBagConstraints(1, 8, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lTNSName,           new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fTNSName,              new GridBagConstraints(3, 5, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lMailServer,          new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lSystemPassword,           new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fSystemPassword,          new GridBagConstraints(3, 7, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabasePassword,           new GridBagConstraints(3, 8, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabasePassword,          new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailServer,          new GridBagConstraints(1, 9, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(bTest,         new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(bSave,          new GridBagConstraints(3, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabaseServer,          new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseServer,         new GridBagConstraints(1, 5, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseName,       new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lJavaHome,      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJavaHome,            new GridBagConstraints(1, 0, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bCompiereHome,     new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bHelp,        new GridBagConstraints(2, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bJavaHome,     new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lJNPPort,     new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJNPPort,      new GridBagConstraints(3, 3, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lMailPassword,      new GridBagConstraints(2, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailPassword,       new GridBagConstraints(3, 10, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lAdminEMail,   new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailUser,   new GridBagConstraints(1, 10, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fAdminEMail,    new GridBagConstraints(3, 9, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lMailUser,  new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 0), 0, 0));

		//
		bCompiereHome.addActionListener(this);
		bJavaHome.addActionListener(this);
		bHelp.addActionListener(this);
		bTest.addActionListener(this);
		bSave.addActionListener(this);
	}	//	jbInit


	/**
	 * 	Dynamic Init
	 *  @throws Exception
	 */
	private void dynInit() throws Exception
	{
		//	Layout - Column Length
		fWebPort.setColumns(30);
		fJNPPort.setColumns(30);

		//	DatabaseType
		fDatabaseType.addItem("Oracle 9i2");

		//	Compiere Home
		String ch = System.getProperty(COMPIERE_HOME);
		if (ch == null || ch.length() == 0)
			ch = System.getProperty("user.dir");
		fCompiereHome.setText(ch);
		boolean envLoaded = false;
		String fileName = ch + File.separator + COMPIERE_ENV_FILE;
		File env = new File (fileName);
		System.out.println(env + " - exists=" + env.exists());
		if (env.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(env);
				m_properties.load(fis);
				fis.close();
			}
			catch (Exception e)
			{
				System.err.println(e.toString());
			}
			System.out.println("Loading ...");
			if (m_properties.size() > 5)
				envLoaded = true;
			//
			fCompiereHome.setText((String)m_properties.get(COMPIERE_HOME));
			fJavaHome.setText((String)m_properties.get(JAVA_HOME));
			fAppsServer.setText((String)m_properties.get(COMPIERE_APPS_SERVER));
			fWebPort.setText((String)m_properties.get(COMPIERE_WEB_PORT));
			fJNPPort.setText((String)m_properties.get(COMPIERE_JNP_PORT));

			fDatabaseServer.setText((String)m_properties.get(COMPIERE_DB_SERVER));
			fDatabasePort.setText((String)m_properties.get(COMPIERE_DB_PORT));
			fDatabaseName.setText((String)m_properties.get(COMPIERE_DB_NAME));

			fDatabaseUser.setText((String)m_properties.get(COMPIERE_DB_USER));
			fDatabasePassword.setText((String)m_properties.get(COMPIERE_DB_PASSWORD));
			fSystemPassword.setText((String)m_properties.get(COMPIERE_DB_SYSTEM));
			fillTNSName((String)m_properties.get(COMPIERE_DB_TNS));

			fMailServer.setText((String)m_properties.get(COMPIERE_MAIL_SERVER));
			fMailUser.setText((String)m_properties.get(COMPIERE_MAIL_USER));
			fMailPassword.setText((String)m_properties.get(COMPIERE_MAIL_PASSWORD));
			fAdminEMail.setText((String)m_properties.get(COMPIERE_ADMIN_EMAIL));
		}

		//	No environment file found - defaults
		if (!envLoaded)
		{
			System.out.println("Setting Defaults");
			//	Java Home, e.g. D:\j2sdk1.4.1\jre
			String javaHome = System.getProperty("java.home");
			if (javaHome.endsWith("jre"))
				javaHome = javaHome.substring(0, javaHome.length()-4);
			fJavaHome.setText(javaHome);
			//	AppsServer
			fAppsServer.setText(InetAddress.getLocalHost().getHostName());
			//	Database Server
			fDatabaseServer.setText(InetAddress.getLocalHost().getHostName());
			//	Mail Server
			fMailServer.setText(InetAddress.getLocalHost().getHostName());
			//  Get TNS Names
			fillTNSName("");
		}	//	!envLoaded

		//	Default FTP stuff
		if (!m_properties.containsKey(COMPIERE_FTP_SERVER))
		{
			m_properties.setProperty(COMPIERE_FTP_SERVER, "localhost");
			m_properties.setProperty(COMPIERE_FTP_USER, "anonymous");
			m_properties.setProperty(COMPIERE_FTP_PASSWORD, "user@host.com");
			m_properties.setProperty(COMPIERE_FTP_PREFIX, "my");
		}
		//	Default Java Options
		if (!m_properties.containsKey(COMPIERE_JAVA_OPTIONS))
			m_properties.setProperty(COMPIERE_JAVA_OPTIONS, "-Xms32m -Xmx128m");

		//	(String)m_properties.get(COMPIERE_DB_URL)	//	derived
	}	//	dynInit

	/**
	 * 	Fill TNS Name
	 * 	@param defaultValue defaultValue
	 */
	private void fillTNSName (String defaultValue)
	{
		//	default value to lowercase or null
		String def = defaultValue;
		if (def != null && def.trim().length() == 0)
			def = null;
		if (def != null)
		{
			def = def.toLowerCase();
			fTNSName.addItem(def);
		}

		//	Search for Oracle Info
		String path = System.getProperty("java.library.path");
		String[] entries = path.split(File.pathSeparator);
		for (int e = 0; e < entries.length; e++)
		{
			String entry = entries[e].toLowerCase();
			if (entry.indexOf("ora") != -1 && entry.endsWith("bin"))
			{
				StringBuffer sb = getTNS_File (entries[e].substring(0, entries[e].length()-4));
				String[] tnsnames = getTNS_Names (sb);
				if (tnsnames != null)
				{
					for (int i = 0; i < tnsnames.length; i++)
					{
						String tns = tnsnames[i];
						if (!tns.equals(def))
							fTNSName.addItem(tns);
					}
					break;
				}
			}
		}	//	for all path entries

		//	Set first entry
		fTNSName.addActionListener(this);
		if (fTNSName.getItemCount() > 0)
			fTNSName.setSelectedIndex(0);
	}	//	fillTNSName

	/**
	 * 	Get File tnmsnames.ora in StringBuffer
	 * 	@param oraHome ORACLE_HOME
	 * 	@return tnsnames.ora or null
	 */
	private StringBuffer getTNS_File (String oraHome)
	{
		String tnsnames = oraHome + File.separator
			+ "network" + File.separator
			+ "admin" + File.separator
			+ "tnsnames.ora";
		File tnsfile = new File (tnsnames);
		if (!tnsfile.exists())
			return null;

		System.out.println("Searching " + tnsnames);
		StringBuffer sb = new StringBuffer();
		try
		{
			FileReader fr = new FileReader (tnsfile);
			int c;
			while ((c = fr.read()) != -1)
				sb.append((char)c);
		}
		catch (IOException ex)
		{
			System.err.println("Error Reading " + tnsnames);
			ex.printStackTrace();
			return null;
		}
		if (sb.length() == 0)
			return null;
		return sb;
	}	//	getTNS_File

	/**
	 * 	Get TNS Names entries.
	 * 	Assumes standard tnsmanes.ora formatting of NetMgr
	 * 	@param tnsnames content of tnsnames.ora
	 * 	@return tns names or null
	 */
	private String[] getTNS_Names (StringBuffer tnsnames)
	{
		if (tnsnames == null)
			return null;

		ArrayList list = new ArrayList();
		Pattern pattern = Pattern.compile("$", Pattern.MULTILINE);
		String[] lines = pattern.split(tnsnames);
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i].trim();
			if (line.length() > 0
				&& Character.isLetter(line.charAt(0))	//	no # (
				&& line.indexOf("=") != -1
				&& line.indexOf("EXTPROC_") == -1
				&& line.indexOf("_HTTP") == -1)
			{
				String entry = line.substring(0, line.indexOf('=')).trim().toLowerCase();
				System.out.println("- " + entry);
				list.add(entry);
			}
		}
		//	Convert to Array
		if (list.size() == 0)
			return null;
		String[] retValue = new String[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getTNS_Names

	/*************************************************************************/

	/**
	 * 	ActionListener
	 *  @param e event
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (m_testing)
			return;
		//	TNS Name Changed
		if (e.getSource() == fTNSName)
		{
			String tns = (String)fTNSName.getSelectedItem();
			if (tns != null)
			{
				int pos = tns.indexOf('.');
				if (pos != -1)
					tns = tns.substring(0, pos);
				fDatabaseName.setText(tns);
			}
		}

		else if (e.getSource() == bJavaHome)
			setPath (fJavaHome);
		else if (e.getSource() == bCompiereHome)
			setPath (fCompiereHome);
		else if (e.getSource() == bHelp)
			new Setup_Help((Frame)SwingUtilities.getWindowAncestor(this));
		else if (e.getSource() == bTest)
			startTest();
		else if (e.getSource() == bSave)
			save();
	}	//	actionPerformed

	/**
	 * 	Set Path in Field
	 * 	@param field field to set Path
	 */
	private void setPath (JTextField field)
	{
		JFileChooser fc = new JFileChooser(field.getText());
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			field.setText(fc.getSelectedFile().getAbsolutePath());
	}	//	setPath

	/*************************************************************************/

	/**
	 * 	Start Test Async.
	 *  @return SwingWorker
	 */
	private SwingWorker startTest()
	{
		SwingWorker worker = new SwingWorker()
		{
			//	Start it
			public Object construct()
			{
				m_testing = true;
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				bTest.setEnabled(false);
				m_success = false;
				m_errorString = null;
				try
				{
					test();
				}
				catch (Exception ex)
				{
					m_errorString = ex.getMessage();
				}
				//
				setCursor(Cursor.getDefaultCursor());
				if (m_errorString == null)
					m_success = true;
				bTest.setEnabled(true);
				m_testing = false;
				return new Boolean(m_success);
			}
			//	Finish it
			public void finished()
			{
				if (m_errorString != null)
				{
					System.err.println(m_errorString);
					JOptionPane.showConfirmDialog (null, m_errorString, res.getString("ServerError"),
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		worker.start();
		return worker;
	}	//	startIt

	/**
	 *	Test it
	 * 	@throws Exception
	 */
	private void test() throws Exception
	{
		System.out.println();
		System.out.println("** Test **");
		bSave.setEnabled(false);


		//	Java Home
		m_errorString = res.getString("ErrorJavaHome");
		m_javaHome = new File (fJavaHome.getText());
		if (!m_javaHome.exists())
			return;
		File tools = new File (fJavaHome.getText() + File.separator + "lib" + File.separator + "tools.jar");
		if (!tools.exists())
		{
			System.err.println("Not Found Java SDK = " + tools);
			return;
		}
		System.out.println("OK: JavaHome=" + m_javaHome.getAbsolutePath());
		m_properties.setProperty(JAVA_HOME, m_javaHome.getAbsolutePath());

		//	Java Version
		final String VERSION = "1.4.1";
		boolean versionOK = false;
		String jh = m_javaHome.getAbsolutePath();
		if (jh.indexOf(VERSION) != -1)	//	file name has version = assuming OK
			versionOK = true;
		String thisJH = System.getProperty("java.home");
		if (thisJH.indexOf(jh) != -1)	//	wre are running the version currently
		{
			String thisJV = System.getProperty("java.version");
			versionOK = thisJV.indexOf(VERSION) != -1;
			if (versionOK)
			  System.out.println("  Java Version OK = " + thisJV);
		}
		if (!versionOK)
		  System.out.println("** Please check Java Version - should be " + VERSION + "*");

		//	Compiere Home
		m_errorString = res.getString("ErrorCompiereHome");
		m_compiereHome = new File (fCompiereHome.getText());
		if (!m_compiereHome.exists())
		{
			System.err.println("Not found CompiereHome = " + m_compiereHome);
			return;
		}
		System.out.println("OK: CompiereHome = " + m_compiereHome);
		m_properties.setProperty(COMPIERE_HOME, m_compiereHome.getAbsolutePath());


		//	AppsServer
		m_errorString = res.getString("ErrorAppsServer");
		String server = fAppsServer.getText();
		if (server == null || server.length() == 0 || server.toLowerCase().indexOf("localhost") != -1)
		{
			System.err.println("Not correct AppsServer = " + m_appsServer);
			return;
		}
		m_appsServer = InetAddress.getByName(server);
		System.out.println("OK: AppsServer = " + m_appsServer);
		m_properties.setProperty(COMPIERE_APPS_SERVER, m_appsServer.getHostName());


		//	Web Port
		m_errorString = res.getString("ErrorWebPort");
		m_statusBar.setText(lWebPort.getText());
		m_WebPort = Integer.parseInt(fWebPort.getText());
		//	HTML Port Use
		if (testPort ("http", m_appsServer.getHostName(), m_WebPort, "/") || !testServerPort(m_WebPort))
		{
			System.err.println("Not correct WebPort = " + m_WebPort);
			return;
		}
		System.out.println("OK: WebPort = " + m_WebPort);
		m_properties.setProperty(COMPIERE_WEB_PORT, String.valueOf(m_WebPort));
		//	Derive SSL Port
		int sslPort = m_WebPort == 80 ? 443 : 8443;
		System.out.println("SSL WebPort = " + sslPort);
		m_properties.setProperty(COMPIERE_SSL_PORT, String.valueOf(sslPort));


		//	JNP Port
		m_errorString = res.getString("ErrorJNPPort");
		m_statusBar.setText(lJNPPort.getText());
		m_JNPPort = Integer.parseInt(fJNPPort.getText());
		//	HTML Port Use
		if (testPort (m_appsServer, m_JNPPort, false) || !testServerPort(m_JNPPort))
		{
			System.err.println("Not correct JNPPort = " + m_JNPPort);
			return;
		}
		System.out.println("OK: JNPPort = " + m_JNPPort);
		m_properties.setProperty(COMPIERE_JNP_PORT, String.valueOf(m_JNPPort));


		//	Database Server
		m_errorString = res.getString("ErrorDatabaseServer");
		m_statusBar.setText(lDatabaseServer.getText());
		server = fDatabaseServer.getText();
		if (server == null || server.length() == 0 || server.toLowerCase().indexOf("localhost") != -1)
		{
			System.err.println("Not correct Database Server = " + server);
			return;
		}
		m_databaseServer = InetAddress.getByName(server);
		System.out.println("OK: Database Server = " + m_databaseServer);
		m_properties.setProperty(COMPIERE_DB_SERVER, m_databaseServer.getHostName());


		//	Database Port
		m_errorString = res.getString("ErrorDatabasePort");
		m_databasePort = Integer.parseInt(fDatabasePort.getText());
		//	Database Port Use
		if (!testPort (m_databaseServer, m_databasePort, true))
		{
			System.err.println("Error Database Port = " + m_databasePort);
			return;
		}
		System.out.println("OK: Database Port = " + m_databasePort);
		m_properties.setProperty(COMPIERE_DB_PORT, String.valueOf(m_databasePort));


		//	JDBC Database Info
		m_errorString = res.getString("ErrorJDBC");
		m_statusBar.setText(lDatabaseName.getText());
		m_databaseName = fDatabaseName.getText();	//	SID
		m_systemPassword = new String(fSystemPassword.getPassword());
		if (!testJDBC("system", m_systemPassword))
		{
			System.err.println("Error Database Name = " + m_databaseName);
			System.err.println("Error Database SystemID = system/" + m_systemPassword);
			return;
		}
		System.out.println("OK: Database Name = " + m_databaseName);
		m_properties.setProperty(COMPIERE_DB_NAME, m_databaseName);
		System.out.println("OK: Database SystemID = system/" + m_systemPassword);
		m_properties.setProperty(COMPIERE_DB_SYSTEM, m_systemPassword);
		//	URL (derived)	jdbc:oracle:thin:@prod1:1521:prod1
		StringBuffer url = new StringBuffer("jdbc:oracle:thin:@")
			.append(m_databaseServer).append(":")
			.append(m_databasePort).append(":").append(m_databaseName);
		System.out.println("OK: Database URL = " + url.toString());
		m_properties.setProperty(COMPIERE_DB_URL, url.toString());


		//	Database User Info
		m_databaseUser = fDatabaseUser.getText();	//	UID
		m_databasePassword = new String(fDatabasePassword.getPassword());	//	PWD
		//	Ignore result as it might not be imported
		if (testJDBC(m_databaseUser, m_databasePassword))
			System.out.println("OK: Database UserID = " + m_databaseUser + "/" + m_databasePassword);
		else
			System.out.println("Not created yet: Database UserID = " + m_databaseUser + "/" + m_databasePassword);
		m_properties.setProperty(COMPIERE_DB_USER, m_databaseUser);
		m_properties.setProperty(COMPIERE_DB_PASSWORD, m_databasePassword);


		//	TNS Name Info
		m_errorString = res.getString("ErrorTNS");
		m_statusBar.setText(lTNSName.getText());
		m_TNSName = (String)fTNSName.getSelectedItem();
		if (!testTNS("system", m_systemPassword))
		{
			System.err.println("Error Database TNS Name = " + m_TNSName);
			return;
		}
		System.out.println("OK: Database TNS Name = " + m_TNSName);
		m_properties.setProperty(COMPIERE_DB_TNS, m_TNSName);


		//	Mail Server
		m_errorString = res.getString("ErrorMailServer");
		m_statusBar.setText(lMailServer.getText());
		server = fMailServer.getText();
		if (server == null || server.length() == 0)
		{
			System.err.println("Error Mail Server = " + server);
			return;
		}
		m_mailServer = InetAddress.getByName(server);
		System.out.println("OK: Mail Server = " + m_mailServer);
		m_properties.setProperty(COMPIERE_MAIL_SERVER, m_mailServer.getHostName());


		//	Mail User
		m_errorString = "ErrorMailUser";
		m_statusBar.setText(lMailUser.getText());
		m_mailUser = fMailUser.getText();
		m_mailPassword = new String(fMailPassword.getPassword());
		m_properties.setProperty(COMPIERE_MAIL_USER, m_mailUser);
		m_properties.setProperty(COMPIERE_MAIL_PASSWORD, m_mailPassword);
		System.out.println("  Mail User = " + m_mailUser + "/" + m_mailPassword);

		//	Mail Address
		m_errorString = res.getString("ErrorMail");
		m_adminEMail = new InternetAddress (fAdminEMail.getText());
		//
		if (testMail())
			System.out.println("OK: Admin EMail = " + m_adminEMail);
		else
			System.out.println("Not verified Admin EMail = " + m_adminEMail);
		m_properties.setProperty(COMPIERE_ADMIN_EMAIL, m_adminEMail.toString());

		//
		m_statusBar.setText(res.getString("Ok"));
		System.out.println("** Test OK **");
		bSave.setEnabled(true);
		m_errorString = null;
	}	//	test


	/**
	 * 	Test Apps Server Port (client perspective)
	 *  @param protocol protocol (http, ..)
	 *  @param server server name
	 *  @param port port
	 *  @param file file name
	 *  @return true if able to connect
	 */
	private boolean testPort (String protocol, String server, int port, String file)
	{
		URL url = null;
		try
		{
			url = new URL (protocol, server, port, file);
		}
		catch (MalformedURLException ex)
		{
			ex.printStackTrace();
			return false;
		}
		System.out.println("  URL=" + url);
		try
		{
			URLConnection c = url.openConnection();
			Object o = c.getContent();
			System.err.println("  URL Connection in use=" + url);	//	error
		}
		catch (Exception ex)
		{
			return false;
		}
		return true;
	}	//	testPort

	/**
	 * 	Test Server Port
	 *  @param port port
	 *  @return true if able to create
	 */
	private boolean testServerPort (int port)
	{
		try
		{
			ServerSocket ss = new ServerSocket (port);
			System.out.println("  ServerPort " + ss.getInetAddress() + ":" + ss.getLocalPort());
			ss.close();
		}
		catch (Exception ex)
		{
			System.err.println(ex);
			return false;
		}
		return true;
	}	//	testPort


	/**
	 * 	Test Port
	 *  @param host host
	 *  @param port port
	 *  @param shouldBenUsed true if it should be used
	 *  @return true if some server answered on port
	 */
	public boolean testPort (InetAddress host, int port, boolean shouldBenUsed)
	{
		Socket pingSocket = null;
		try
		{
			pingSocket = new Socket(host, port);
		}
		catch (Exception e)
		{
			if (shouldBenUsed)
				System.err.println("  Open Socket " + host + " on " + port + ": " + e.getMessage());
			return false;
		}
		if (!shouldBenUsed)
			System.err.println("  Open Socket " + host + " on " + port);
		if (pingSocket == null)
			return false;
		//	success
		try
		{
			pingSocket.close();
		}
		catch (IOException e)
		{
			System.out.println("  CloseSocket=" + e.toString());
		}
		return true;
	}	//	testPort

	/**
	 * 	Test JDBC Connection to Server
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testJDBC (String uid, String pwd)
	{
		//	jdbc:oracle:thin:@dev:1521:dev1
		m_connectionString = "jdbc:oracle:thin:@"
			+ m_databaseServer.getHostName() + ":" + m_databasePort + ":" + m_databaseName;
		System.out.println("  JDBC = " + m_connectionString);

		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(m_connectionString,
				uid, pwd);
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testJDBC

	/**
	 * 	Test TNS Connection
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testTNS (String uid, String pwd)
	{
		String connectionString = "jdbc:oracle:oci8:@" + m_TNSName;
		System.out.println("  TNS = " + connectionString);
		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(connectionString,
				uid, pwd);
		}
		catch (UnsatisfiedLinkError ule)
		{
			System.err.println("Check setup of Oracle Server / Oracle Client / LD_LIBRARY_PATH");
			System.err.println(ule.toString());
			return false;
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testTNS

	/**
	 * 	Test Mail
	 *  @return true of OK
	 */
	private boolean testMail()
	{
		boolean smtpOK = false;
		boolean imapOK = false;
		if (testPort (m_mailServer, 25, true))
		{
			System.out.println("OK: SMTP Server contacted");
			smtpOK = true;
		}
		else
			System.err.println("Error: SMTP Server NOT available");
		//
		if (testPort (m_mailServer, 110, true))
			System.out.println("OK: POP3 Server contacted");
		else
			System.err.println("Error: POP3 Server NOT available");
		if (testPort (m_mailServer, 143, true))
		{
			System.out.println("OK: IMAP4 Server contacted");
			imapOK = true;
		}
		else
			System.err.println("Error: IMAP4 Server NOT available");
		//
		if (!smtpOK)
			return false;
		//
		EMail em = new EMail(m_mailServer.getHostName(),
			m_adminEMail.toString(), m_adminEMail.toString(),
			"Compiere Server Setup Test", "Test: " + m_properties);
		if (EMail.SENT_OK.equals(em.send()))
			System.out.println("OK: Send Test Email to " + m_adminEMail);
		else
			System.err.println("Error: Could NOT send Email to " + m_adminEMail);

		//
		if (!imapOK)
			return false;
		//	Test Mail Access
		Properties props = new Properties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_mailServer.getHostName());
		props.put("mail.user", m_mailUser);
		Session session = Session.getDefaultInstance(props, null);
		//	Connect to Store
		Store store;
		try
		{
			store = session.getStore("imap");
		}
		catch (NoSuchProviderException nsp)
		{
			System.err.println("Error Mail IMAP " + nsp.getMessage());
			return false;
		}
		try
		{
			store.connect(m_mailServer.getHostName(), m_mailUser, m_mailPassword);
			Folder folder = store.getDefaultFolder();
			Folder inbox = folder.getFolder("INBOX");
			System.out.println("OK: Mail Connect to " + inbox.getFullName() + " #Msg=" + inbox.getMessageCount());
			//
			store.close();
		}
		catch (MessagingException mex)
		{
			System.err.println("Error Mail Connect " + mex.getMessage());
			return false;
		}
		return true;
	}	//	testMail

	/*************************************************************************/

	/**
	 * 	Save Settings
	 */
	private void save()
	{
		SwingWorker sw = startTest();
		while (sw.isAlive())
		{
			try
			{
				Thread.currentThread().sleep(2000);
			}
			catch (InterruptedException ex)
			{
				System.err.println("save-waiting: " + ex);
			}
		}
		sw.get();	//	block
		if (!m_success)
			return;

		//	Add
		m_properties.setProperty("COMPIERE_MAIN_VERSION", Compiere.MAIN_VERSION);
		m_properties.setProperty("COMPIERE_DATE_VERSION", Compiere.DATE_VERSION);
		m_properties.setProperty("COMPIERE_DB_VERSION", Compiere.DB_VERSION);

		//	Before we save, load Ini
		Ini.setClient(false);
		String fileName = m_compiereHome.getAbsolutePath() + File.separator + Ini.COMPIERE_PROPERTY_FILE;
		Ini.loadProperties(fileName);

		//	Save Environment
		fileName = m_compiereHome.getAbsolutePath() + File.separator + COMPIERE_ENV_FILE;
		try
		{
			FileOutputStream fos = new FileOutputStream(new File(fileName));
			m_properties.store(fos, COMPIERE_ENV_FILE);
			fos.flush();
			fos.close();
		}
		catch (Exception e)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + e.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (Throwable t)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + t.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		System.out.println("Properties saved to " + fileName);

		//	Sync Properties
		Ini.setCompiereHome(m_compiereHome.getAbsolutePath());
		CConnection cc = CConnection.get (Database.DB_ORACLE,
			m_databaseServer.getHostName(), m_databasePort, m_databaseName,
			m_databaseUser, m_databasePassword);
		cc.setAppsHost(m_appsServer.getHostName());
		Ini.setProperty(Ini.P_CONNECTION, cc.toStringLong());
		Ini.saveProperties(false);


		JOptionPane.showConfirmDialog(this, res.getString("EnvironmentSaved"),
			res.getString("CompiereServerSetup"),
			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

		((Frame)SwingUtilities.getWindowAncestor(this)).dispose();
		System.exit(0);		//	remains active when License Dialog called
	}	//	save

}	//	ConfigurationPanel

/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2002 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.install;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;

import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;

import oracle.jdbc.*;
import org.compiere.*;
import org.compiere.apps.*;
import org.compiere.db.*;
import org.compiere.util.*;


/**
 *	Configuration Panel
 *
 * 	@author 	Jorg Janke
 * 	@author 	Marek Mosiewicz<marek.mosiewicz@jotel.com.pl> support for RMI over HTTP - now reimplemented/removed jj
 * 	@version 	$Id: ConfigurationPanel.java,v 1.6 2003/07/21 04:44:17 jjanke Exp $
 */
public class ConfigurationPanel extends JPanel implements ActionListener
{
	/**
	 * 	Constructor
	 *  @param statusBar for info
	 * 	@throws Exception
	 */
	public ConfigurationPanel (JLabel statusBar) throws Exception
	{
		LogManager.resetConfiguration();
		Logger root = Logger.getRootLogger();
	//	root.addAppender(new ConsoleAppender(new PatternLayout("%d{ABSOLUTE} %-5p [%c{1}] %m%n")));
		root.addAppender(new ConsoleAppender(new LogLayout()));
		root.setLevel(Level.ALL);
		Log.setTraceLevel(10);

		m_statusBar = statusBar;
		jbInit();
		dynInit();
	}	//	ConfigurationPanel

	public static final String	COMPIERE_ENV_FILE		= "CompiereEnv.properties";

	public static final String	COMPIERE_HOME 			= "COMPIERE_HOME";
	public static final String	JAVA_HOME 				= "JAVA_HOME";
	public static final String	COMPIERE_JAVA_OPTIONS 	= "COMPIERE_JAVA_OPTIONS";

	public static final String	COMPIERE_APPS_SERVER 	= "COMPIERE_APPS_SERVER";
	public static final String	COMPIERE_JNP_PORT 		= "COMPIERE_JNP_PORT";
	public static final String	COMPIERE_WEB_PORT 		= "COMPIERE_WEB_PORT";
	public static final String	COMPIERE_SSL_PORT 		= "COMPIERE_SSL_PORT";

	public static final String	COMPIERE_DB_SERVER 		= "COMPIERE_DB_SERVER";
	public static final String	COMPIERE_DB_PORT 		= "COMPIERE_DB_PORT";
	public static final String	COMPIERE_DB_NAME 		= "COMPIERE_DB_NAME";

	public static final String	COMPIERE_DB_USER 		= "COMPIERE_DB_USER";
	public static final String	COMPIERE_DB_PASSWORD 	= "COMPIERE_DB_PASSWORD";
	public static final String	COMPIERE_DB_SYSTEM 		= "COMPIERE_DB_SYSTEM";

	public static final String	COMPIERE_DB_URL 		= "COMPIERE_DB_URL";
	public static final String	COMPIERE_DB_TNS 		= "COMPIERE_DB_TNS";

	public static final String	COMPIERE_MAIL_SERVER 	= "COMPIERE_MAIL_SERVER";
	public static final String	COMPIERE_MAIL_USER 		= "COMPIERE_MAIL_USER";
	public static final String	COMPIERE_MAIL_PASSWORD 	= "COMPIERE_MAIL_PASSWORD";
	public static final String	COMPIERE_ADMIN_EMAIL	= "COMPIERE_ADMIN_EMAIL";

	public static final String	COMPIERE_FTP_SERVER		= "COMPIERE_FTP_SERVER";
	public static final String	COMPIERE_FTP_USER		= "COMPIERE_FTP_USER";
	public static final String	COMPIERE_FTP_PASSWORD	= "COMPIERE_FTP_PASSWORD";
	public static final String	COMPIERE_FTP_PREFIX		= "COMPIERE_FTP_PREFIX";

	private String			m_errorString;
	private File			m_javaHome;
	private File			m_compiereHome;
	private InetAddress		m_appsServer;
	private int				m_WebPort;
	private int				m_JNPPort;
	private InetAddress		m_databaseServer;
	private int				m_databasePort;
	private String			m_databaseName;
	private String			m_connectionString;
	private String			m_systemPassword;
	private String			m_databaseUser;
	private String			m_databasePassword;
	private String			m_TNSName;

	private InetAddress		m_mailServer;
	private InternetAddress	m_adminEMail;
	private String			m_mailUser;
	private String			m_mailPassword;

	private volatile boolean		m_success = false;
	private volatile boolean	 	m_testing = false;

	/** Translation				*/
	static ResourceBundle 		res = ResourceBundle.getBundle("org.compiere.install.SetupRes");

	/**	Driver					*/
	private static Driver		s_driver = null;
	/** Environment Properties	*/
	private Properties			m_properties = new Properties();
	/** Status Bar				*/
	private JLabel 				m_statusBar;

	private static ImageIcon iOpen = new ImageIcon(ConfigurationPanel.class.getResource("openFile.gif"));
	private static ImageIcon iSave = new ImageIcon(Compiere.class.getResource("images/Save16.gif"));
	private static ImageIcon iHelp = new ImageIcon(Compiere.class.getResource("images/Help16.gif"));


	//	-------------	Static UI
	private GridBagLayout gridBagLayout = new GridBagLayout();
	private JLabel lCompiereHome = new JLabel();
	private JTextField fCompiereHome = new JTextField();
	private JLabel lWebPort = new JLabel();
	private JTextField fWebPort = new JTextField();
	private JLabel lAppsServer = new JLabel();
	private JTextField fAppsServer = new JTextField();
	private JLabel lDatabaseType = new JLabel();
	private JComboBox fDatabaseType = new JComboBox();
	private JLabel lDatabaseName = new JLabel();
	private JLabel lDatabasePort = new JLabel();
	private JLabel lDatabaseUser = new JLabel();
	private JLabel lDatabasePassword = new JLabel();
	private JTextField fDatabaseName = new JTextField();
	private JTextField fDatabasePort = new JTextField();
	private JTextField fDatabaseUser = new JTextField();
	private JPasswordField fDatabasePassword = new JPasswordField();
	private JLabel lTNSName = new JLabel();
	private JComboBox fTNSName = new JComboBox();
	private JLabel lSystemPassword = new JLabel();
	private JPasswordField fSystemPassword = new JPasswordField();
	private JLabel lMailServer = new JLabel();
	private JTextField fMailServer = new JTextField();
	private JLabel lAdminEMail = new JLabel();
	private JTextField fAdminEMail = new JTextField();
	private JLabel lDatabaseServer = new JLabel();
	private JTextField fDatabaseServer = new JTextField();
	private JLabel lJavaHome = new JLabel();
	private JTextField fJavaHome = new JTextField();
	private JButton bCompiereHome = new JButton(iOpen);
	private JButton bJavaHome = new JButton(iOpen);
	private JButton bHelp = new JButton(iHelp);
	private JButton bTest = new JButton();
	private JButton bSave = new JButton(iSave);
	private JLabel lJNPPort = new JLabel();
	private JTextField fJNPPort = new JTextField();
	private JLabel lMailUser = new JLabel();
	private JLabel lMailPassword = new JLabel();
	private JTextField fMailUser = new JTextField();
	private JPasswordField fMailPassword = new JPasswordField();


	/**
	 * 	Static Layout Init
	 *  @throws Exception
	 */
	private void jbInit() throws Exception
	{
		lCompiereHome.setToolTipText(res.getString("CompiereHomeInfo"));
		lCompiereHome.setText(res.getString("CompiereHome"));
		this.setLayout(gridBagLayout);
		fCompiereHome.setText("D:\\Compiere2");
		lWebPort.setToolTipText(res.getString("WebPortInfo"));
		lWebPort.setText(res.getString("WebPort"));
		fWebPort.setText("80");
		lAppsServer.setToolTipText(res.getString("AppsServerInfo"));
		lAppsServer.setText(res.getString("AppsServer"));
		fAppsServer.setText("server.company.com");
		lDatabaseType.setToolTipText(res.getString("DatabaseTypeInfo"));
		lDatabaseType.setText(res.getString("DatabaseType"));
		lDatabaseName.setToolTipText(res.getString("DatabaseNameInfo"));
		lDatabaseName.setText(res.getString("DatabaseName"));
		lDatabasePort.setToolTipText(res.getString("DatabasePortInfo"));
		lDatabasePort.setText(res.getString("DatabasePort"));
		lDatabaseUser.setToolTipText(res.getString("DatabaseUserInfo"));
		lDatabaseUser.setText(res.getString("DatabaseUser"));
		lDatabasePassword.setToolTipText(res.getString("DatabasePasswordInfo"));
		lDatabasePassword.setText(res.getString("DatabasePassword"));
		fDatabaseName.setText("compiere");
		fDatabasePort.setText("1521");
		fDatabaseUser.setText("compiere");
		fDatabasePassword.setText("compiere");
		lTNSName.setToolTipText(res.getString("TNSNameInfo"));
		lTNSName.setText(res.getString("TNSName"));
		fTNSName.setEditable(true);
		lSystemPassword.setToolTipText(res.getString("SystemPasswordInfo"));
		lSystemPassword.setText(res.getString("SystemPassword"));
		fSystemPassword.setText("manager");
		lMailServer.setToolTipText(res.getString("MailServerInfo"));
		lMailServer.setText(res.getString("MailServer"));
		fMailServer.setText("mail.company.com");
		lAdminEMail.setToolTipText(res.getString("AdminEMailInfo"));
		lAdminEMail.setText(res.getString("AdminEMail"));
		fAdminEMail.setText("admin@company.com");
		bTest.setToolTipText(res.getString("TestInfo"));
		bTest.setText(res.getString("Test"));
		bSave.setToolTipText(res.getString("SaveInfo"));
		bSave.setText(res.getString("Save"));
		lDatabaseServer.setToolTipText(res.getString("DatabaseServerInfo"));
		lDatabaseServer.setText(res.getString("DatabaseServer"));
		lJavaHome.setToolTipText(res.getString("JavaHomeInfo"));
		lJavaHome.setText(res.getString("JavaHome"));
		fJavaHome.setText("Java141");
		lJNPPort.setToolTipText(res.getString("JNPPortInfo"));
		lJNPPort.setText(res.getString("JNPPort"));
		fJNPPort.setText("1099");
		bHelp.setToolTipText(res.getString("HelpInfo"));
		lMailUser.setToolTipText(res.getString("MailUserInfo"));
		lMailUser.setText(res.getString("MailUser"));
		lMailPassword.setToolTipText(res.getString("MailPasswordInfo"));
		lMailPassword.setText(res.getString("MailPassword"));
		fMailUser.setText("compiere");
		fMailPassword.setText("compiere");

		//
		this.add(lCompiereHome,           new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fCompiereHome,             new GridBagConstraints(1, 1, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lWebPort,           new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fWebPort,            new GridBagConstraints(1, 3, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lAppsServer,          new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fAppsServer,           new GridBagConstraints(1, 2, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseType,           new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseType,          new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabasePort,          new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseUser,           new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseName,             new GridBagConstraints(1, 6, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabasePort,            new GridBagConstraints(1, 7, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseUser,             new GridBagConstraints(1, 8, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lTNSName,           new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fTNSName,              new GridBagConstraints(3, 5, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lMailServer,          new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lSystemPassword,           new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fSystemPassword,          new GridBagConstraints(3, 7, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabasePassword,           new GridBagConstraints(3, 8, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabasePassword,          new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailServer,          new GridBagConstraints(1, 9, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(bTest,         new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(bSave,          new GridBagConstraints(3, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabaseServer,          new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseServer,         new GridBagConstraints(1, 5, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseName,       new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lJavaHome,      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJavaHome,            new GridBagConstraints(1, 0, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bCompiereHome,     new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bHelp,        new GridBagConstraints(2, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bJavaHome,     new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lJNPPort,     new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJNPPort,      new GridBagConstraints(3, 3, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lMailPassword,      new GridBagConstraints(2, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailPassword,       new GridBagConstraints(3, 10, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lAdminEMail,   new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailUser,   new GridBagConstraints(1, 10, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fAdminEMail,    new GridBagConstraints(3, 9, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lMailUser,  new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 0), 0, 0));
		//
		bCompiereHome.addActionListener(this);
		bJavaHome.addActionListener(this);
		bHelp.addActionListener(this);
		bTest.addActionListener(this);
		bSave.addActionListener(this);
	}	//	jbInit


	/**
	 * 	Dynamic Init
	 *  @throws Exception
	 */
	private void dynInit() throws Exception
	{
		//	Layout - Column Length
		fWebPort.setColumns(30);
		fJNPPort.setColumns(30);

		//	DatabaseType
		fDatabaseType.addItem("Oracle 9i2");

		//	Compiere Home
		String ch = System.getProperty(COMPIERE_HOME);
		if (ch == null || ch.length() == 0)
			ch = System.getProperty("user.dir");
		fCompiereHome.setText(ch);
		boolean envLoaded = false;
		String fileName = ch + File.separator + COMPIERE_ENV_FILE;
		File env = new File (fileName);
		System.out.println(env + " - exists=" + env.exists());
		if (env.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(env);
				m_properties.load(fis);
				fis.close();
			}
			catch (Exception e)
			{
				System.err.println(e.toString());
			}
			System.out.println("Loading ...");
			if (m_properties.size() > 5)
				envLoaded = true;
			//
			fCompiereHome.setText((String)m_properties.get(COMPIERE_HOME));
			fJavaHome.setText((String)m_properties.get(JAVA_HOME));
			fAppsServer.setText((String)m_properties.get(COMPIERE_APPS_SERVER));
			fWebPort.setText((String)m_properties.get(COMPIERE_WEB_PORT));
			fJNPPort.setText((String)m_properties.get(COMPIERE_JNP_PORT));

			fDatabaseServer.setText((String)m_properties.get(COMPIERE_DB_SERVER));
			fDatabasePort.setText((String)m_properties.get(COMPIERE_DB_PORT));
			fDatabaseName.setText((String)m_properties.get(COMPIERE_DB_NAME));

			fDatabaseUser.setText((String)m_properties.get(COMPIERE_DB_USER));
			fDatabasePassword.setText((String)m_properties.get(COMPIERE_DB_PASSWORD));
			fSystemPassword.setText((String)m_properties.get(COMPIERE_DB_SYSTEM));
			fillTNSName((String)m_properties.get(COMPIERE_DB_TNS));

			fMailServer.setText((String)m_properties.get(COMPIERE_MAIL_SERVER));
			fMailUser.setText((String)m_properties.get(COMPIERE_MAIL_USER));
			fMailPassword.setText((String)m_properties.get(COMPIERE_MAIL_PASSWORD));
			fAdminEMail.setText((String)m_properties.get(COMPIERE_ADMIN_EMAIL));
		}

		//	No environment file found - defaults
		if (!envLoaded)
		{
			System.out.println("Setting Defaults");
			//	Java Home, e.g. D:\j2sdk1.4.1\jre
			String javaHome = System.getProperty("java.home");
			if (javaHome.endsWith("jre"))
				javaHome = javaHome.substring(0, javaHome.length()-4);
			fJavaHome.setText(javaHome);
			//	AppsServer
			fAppsServer.setText(InetAddress.getLocalHost().getHostName());
			//	Database Server
			fDatabaseServer.setText(InetAddress.getLocalHost().getHostName());
			//	Mail Server
			fMailServer.setText(InetAddress.getLocalHost().getHostName());
			//  Get TNS Names
			fillTNSName("");
		}	//	!envLoaded

		//	Default FTP stuff
		if (!m_properties.containsKey(COMPIERE_FTP_SERVER))
		{
			m_properties.setProperty(COMPIERE_FTP_SERVER, "localhost");
			m_properties.setProperty(COMPIERE_FTP_USER, "anonymous");
			m_properties.setProperty(COMPIERE_FTP_PASSWORD, "user@host.com");
			m_properties.setProperty(COMPIERE_FTP_PREFIX, "my");
		}
		//	Default Java Options
		if (!m_properties.containsKey(COMPIERE_JAVA_OPTIONS))
			m_properties.setProperty(COMPIERE_JAVA_OPTIONS, "-Xms32m -Xmx128m");

		//	(String)m_properties.get(COMPIERE_DB_URL)	//	derived
	}	//	dynInit

	/**
	 * 	Fill TNS Name
	 * 	@param defaultValue defaultValue
	 */
	private void fillTNSName (String defaultValue)
	{
		//	default value to lowercase or null
		String def = defaultValue;
		if (def != null && def.trim().length() == 0)
			def = null;
		if (def != null)
		{
			def = def.toLowerCase();
			fTNSName.addItem(def);
		}

		//	Search for Oracle Info
		String path = System.getProperty("java.library.path");
		String[] entries = path.split(File.pathSeparator);
		for (int e = 0; e < entries.length; e++)
		{
			String entry = entries[e].toLowerCase();
			if (entry.indexOf("ora") != -1 && entry.endsWith("bin"))
			{
				StringBuffer sb = getTNS_File (entries[e].substring(0, entries[e].length()-4));
				String[] tnsnames = getTNS_Names (sb);
				if (tnsnames != null)
				{
					for (int i = 0; i < tnsnames.length; i++)
					{
						String tns = tnsnames[i];
						if (!tns.equals(def))
							fTNSName.addItem(tns);
					}
					break;
				}
			}
		}	//	for all path entries

		//	Set first entry
		fTNSName.addActionListener(this);
		if (fTNSName.getItemCount() > 0)
			fTNSName.setSelectedIndex(0);
	}	//	fillTNSName

	/**
	 * 	Get File tnmsnames.ora in StringBuffer
	 * 	@param oraHome ORACLE_HOME
	 * 	@return tnsnames.ora or null
	 */
	private StringBuffer getTNS_File (String oraHome)
	{
		String tnsnames = oraHome + File.separator
			+ "network" + File.separator
			+ "admin" + File.separator
			+ "tnsnames.ora";
		File tnsfile = new File (tnsnames);
		if (!tnsfile.exists())
			return null;

		System.out.println("Searching " + tnsnames);
		StringBuffer sb = new StringBuffer();
		try
		{
			FileReader fr = new FileReader (tnsfile);
			int c;
			while ((c = fr.read()) != -1)
				sb.append((char)c);
		}
		catch (IOException ex)
		{
			System.err.println("Error Reading " + tnsnames);
			ex.printStackTrace();
			return null;
		}
		if (sb.length() == 0)
			return null;
		return sb;
	}	//	getTNS_File

	/**
	 * 	Get TNS Names entries.
	 * 	Assumes standard tnsmanes.ora formatting of NetMgr
	 * 	@param tnsnames content of tnsnames.ora
	 * 	@return tns names or null
	 */
	private String[] getTNS_Names (StringBuffer tnsnames)
	{
		if (tnsnames == null)
			return null;

		ArrayList list = new ArrayList();
		Pattern pattern = Pattern.compile("$", Pattern.MULTILINE);
		String[] lines = pattern.split(tnsnames);
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i].trim();
			if (line.length() > 0
				&& Character.isLetter(line.charAt(0))	//	no # (
				&& line.indexOf("=") != -1
				&& line.indexOf("EXTPROC_") == -1
				&& line.indexOf("_HTTP") == -1)
			{
				String entry = line.substring(0, line.indexOf('=')).trim().toLowerCase();
				System.out.println("- " + entry);
				list.add(entry);
			}
		}
		//	Convert to Array
		if (list.size() == 0)
			return null;
		String[] retValue = new String[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getTNS_Names

	/*************************************************************************/

	/**
	 * 	ActionListener
	 *  @param e event
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (m_testing)
			return;
		//	TNS Name Changed
		if (e.getSource() == fTNSName)
		{
			String tns = (String)fTNSName.getSelectedItem();
			if (tns != null)
			{
				int pos = tns.indexOf('.');
				if (pos != -1)
					tns = tns.substring(0, pos);
				fDatabaseName.setText(tns);
			}
		}

		else if (e.getSource() == bJavaHome)
			setPath (fJavaHome);
		else if (e.getSource() == bCompiereHome)
			setPath (fCompiereHome);
		else if (e.getSource() == bHelp)
			new Setup_Help((Frame)SwingUtilities.getWindowAncestor(this));
		else if (e.getSource() == bTest)
			startTest();
		else if (e.getSource() == bSave)
			save();
	}	//	actionPerformed

	/**
	 * 	Set Path in Field
	 * 	@param field field to set Path
	 */
	private void setPath (JTextField field)
	{
		JFileChooser fc = new JFileChooser(field.getText());
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			field.setText(fc.getSelectedFile().getAbsolutePath());
	}	//	setPath

	/*************************************************************************/

	/**
	 * 	Start Test Async.
	 *  @return SwingWorker
	 */
	private SwingWorker startTest()
	{
		SwingWorker worker = new SwingWorker()
		{
			//	Start it
			public Object construct()
			{
				m_testing = true;
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				bTest.setEnabled(false);
				m_success = false;
				m_errorString = null;
				try
				{
					test();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					m_errorString = ex.getMessage();
				}
				//
				setCursor(Cursor.getDefaultCursor());
				if (m_errorString == null)
					m_success = true;
				bTest.setEnabled(true);
				m_testing = false;
				return new Boolean(m_success);
			}
			//	Finish it
			public void finished()
			{
				if (m_errorString != null)
				{
					System.err.println(m_errorString);
					JOptionPane.showConfirmDialog (null, m_errorString, res.getString("ServerError"),
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		worker.start();
		return worker;
	}	//	startIt

	/**
	 *	Test it
	 * 	@throws Exception
	 */
	private void test() throws Exception
	{
		System.out.println();
		System.out.println("** Test **");
		bSave.setEnabled(false);


		//	Java Home
		m_errorString = res.getString("ErrorJavaHome");
		m_javaHome = new File (fJavaHome.getText());
		if (!m_javaHome.exists())
			return;
		File tools = new File (fJavaHome.getText() + File.separator + "lib" + File.separator + "tools.jar");
		if (!tools.exists())
		{
			System.err.println("Not Found Java SDK = " + tools);
			return;
		}
		System.out.println("OK: JavaHome=" + m_javaHome.getAbsolutePath());
		m_properties.setProperty(JAVA_HOME, m_javaHome.getAbsolutePath());

		//	Java Version
		final String VERSION = "1.4.1";
		boolean versionOK = false;
		String jh = m_javaHome.getAbsolutePath();
		if (jh.indexOf(VERSION) != -1)	//	file name has version = assuming OK
			versionOK = true;
		String thisJH = System.getProperty("java.home");
		if (thisJH.indexOf(jh) != -1)	//	wre are running the version currently
		{
			String thisJV = System.getProperty("java.version");
			versionOK = thisJV.indexOf(VERSION) != -1;
			if (versionOK)
			  System.out.println("  Java Version OK = " + thisJV);
		}
		if (!versionOK)
		  System.out.println("** Please check Java Version - should be " + VERSION + "*");

		//	Compiere Home
		m_errorString = res.getString("ErrorCompiereHome");
		m_compiereHome = new File (fCompiereHome.getText());
		if (!m_compiereHome.exists())
		{
			System.err.println("Not found CompiereHome = " + m_compiereHome);
			return;
		}
		System.out.println("OK: CompiereHome = " + m_compiereHome);
		m_properties.setProperty(COMPIERE_HOME, m_compiereHome.getAbsolutePath());


		//	AppsServer
		m_errorString = res.getString("ErrorAppsServer");
		String server = fAppsServer.getText();
		if (server == null || server.length() == 0 || server.toLowerCase().indexOf("localhost") != -1)
		{
			System.err.println("Not correct AppsServer = " + m_appsServer);
			return;
		}
		m_appsServer = InetAddress.getByName(server);
		System.out.println("OK: AppsServer = " + m_appsServer);
		m_properties.setProperty(COMPIERE_APPS_SERVER, m_appsServer.getHostName());

		//	Web Port
		m_errorString = res.getString("ErrorWebPort");
		m_statusBar.setText(lWebPort.getText());
		m_WebPort = Integer.parseInt(fWebPort.getText());
		//	HTML Port Use
		if (testPort ("http", m_appsServer.getHostName(), m_WebPort, "/") || !testServerPort(m_WebPort))
		{
			System.err.println("Not correct WebPort = " + m_WebPort);
			return;
		}
		System.out.println("OK: WebPort = " + m_WebPort);
		m_properties.setProperty(COMPIERE_WEB_PORT, String.valueOf(m_WebPort));
		//	Derive SSL Port
		int sslPort = m_WebPort == 80 ? 443 : 8443;
		System.out.println("SSL WebPort = " + sslPort);
		m_properties.setProperty(COMPIERE_SSL_PORT, String.valueOf(sslPort));


		//	JNP Port
		m_errorString = res.getString("ErrorJNPPort");
		m_statusBar.setText(lJNPPort.getText());
		m_JNPPort = Integer.parseInt(fJNPPort.getText());
		//	HTML Port Use
		if (testPort (m_appsServer, m_JNPPort, false) || !testServerPort(m_JNPPort))
		{
			System.err.println("Not correct JNPPort = " + m_JNPPort);
			return;
		}
		System.out.println("OK: JNPPort = " + m_JNPPort);
		m_properties.setProperty(COMPIERE_JNP_PORT, String.valueOf(m_JNPPort));


		//	Database Server
		m_errorString = res.getString("ErrorDatabaseServer");
		m_statusBar.setText(lDatabaseServer.getText());
		server = fDatabaseServer.getText();
		if (server == null || server.length() == 0 || server.toLowerCase().indexOf("localhost") != -1)
		{
			System.err.println("Not correct Database Server = " + server);
			return;
		}
		m_databaseServer = InetAddress.getByName(server);
		System.out.println("OK: Database Server = " + m_databaseServer);
		m_properties.setProperty(COMPIERE_DB_SERVER, m_databaseServer.getHostName());


		//	Database Port
		m_errorString = res.getString("ErrorDatabasePort");
		m_databasePort = Integer.parseInt(fDatabasePort.getText());
		//	Database Port Use
		if (!testPort (m_databaseServer, m_databasePort, true))
		{
			System.err.println("Error Database Port = " + m_databasePort);
			return;
		}
		System.out.println("OK: Database Port = " + m_databasePort);
		m_properties.setProperty(COMPIERE_DB_PORT, String.valueOf(m_databasePort));


		//	JDBC Database Info
		m_errorString = res.getString("ErrorJDBC");
		m_statusBar.setText(lDatabaseName.getText());
		m_databaseName = fDatabaseName.getText();	//	SID
		m_systemPassword = new String(fSystemPassword.getPassword());
		if (!testJDBC("system", m_systemPassword))
		{
			System.err.println("Error Database Name = " + m_databaseName);
			System.err.println("Error Database SystemID = system/" + m_systemPassword);
			return;
		}
		System.out.println("OK: Database Name = " + m_databaseName);
		m_properties.setProperty(COMPIERE_DB_NAME, m_databaseName);
		System.out.println("OK: Database SystemID = system/" + m_systemPassword);
		m_properties.setProperty(COMPIERE_DB_SYSTEM, m_systemPassword);
		//	URL (derived)	jdbc:oracle:thin:@prod1:1521:prod1
		StringBuffer url = new StringBuffer("jdbc:oracle:thin:@")
			.append(m_databaseServer).append(":")
			.append(m_databasePort).append(":").append(m_databaseName);
		System.out.println("OK: Database URL = " + url.toString());
		m_properties.setProperty(COMPIERE_DB_URL, url.toString());


		//	Database User Info
		m_databaseUser = fDatabaseUser.getText();	//	UID
		m_databasePassword = new String(fDatabasePassword.getPassword());	//	PWD
		//	Ignore result as it might not be imported
		if (testJDBC(m_databaseUser, m_databasePassword))
			System.out.println("OK: Database UserID = " + m_databaseUser + "/" + m_databasePassword);
		else
			System.out.println("Not created yet: Database UserID = " + m_databaseUser + "/" + m_databasePassword);
		m_properties.setProperty(COMPIERE_DB_USER, m_databaseUser);
		m_properties.setProperty(COMPIERE_DB_PASSWORD, m_databasePassword);


		//	TNS Name Info
		m_errorString = res.getString("ErrorTNS");
		m_statusBar.setText(lTNSName.getText());
		m_TNSName = (String)fTNSName.getSelectedItem();
		if (!testTNS("system", m_systemPassword))
		{
			System.err.println("Error Database TNS Name = " + m_TNSName);
			return;
		}
		System.out.println("OK: Database TNS Name = " + m_TNSName);
		m_properties.setProperty(COMPIERE_DB_TNS, m_TNSName);


		//	Mail Server
		m_errorString = res.getString("ErrorMailServer");
		m_statusBar.setText(lMailServer.getText());
		server = fMailServer.getText();
		if (server == null || server.length() == 0)
		{
			System.err.println("Error Mail Server = " + server);
			return;
		}
		m_mailServer = InetAddress.getByName(server);
		System.out.println("OK: Mail Server = " + m_mailServer);
		m_properties.setProperty(COMPIERE_MAIL_SERVER, m_mailServer.getHostName());


		//	Mail User
		m_errorString = "ErrorMailUser";
		m_statusBar.setText(lMailUser.getText());
		m_mailUser = fMailUser.getText();
		m_mailPassword = new String(fMailPassword.getPassword());
		m_properties.setProperty(COMPIERE_MAIL_USER, m_mailUser);
		m_properties.setProperty(COMPIERE_MAIL_PASSWORD, m_mailPassword);
		System.out.println("  Mail User = " + m_mailUser + "/" + m_mailPassword);

		//	Mail Address
		m_errorString = res.getString("ErrorMail");
		m_adminEMail = new InternetAddress (fAdminEMail.getText());
		//
		if (testMail())
			System.out.println("OK: Admin EMail = " + m_adminEMail);
		else
			System.out.println("Not verified Admin EMail = " + m_adminEMail);
		m_properties.setProperty(COMPIERE_ADMIN_EMAIL, m_adminEMail.toString());

		//
		m_statusBar.setText(res.getString("Ok"));
		System.out.println("** Test OK **");
		bSave.setEnabled(true);
		m_errorString = null;
	}	//	test


	/**
	 * 	Test Apps Server Port (client perspective)
	 *  @param protocol protocol (http, ..)
	 *  @param server server name
	 *  @param port port
	 *  @param file file name
	 *  @return true if able to connect
	 */
	private boolean testPort (String protocol, String server, int port, String file)
	{
		URL url = null;
		try
		{
			url = new URL (protocol, server, port, file);
		}
		catch (MalformedURLException ex)
		{
			ex.printStackTrace();
			return false;
		}
		System.out.println("  URL=" + url);
		try
		{
			URLConnection c = url.openConnection();
			Object o = c.getContent();
			System.err.println("  URL Connection in use=" + url);	//	error
		}
		catch (Exception ex)
		{
			return false;
		}
		return true;
	}	//	testPort

	/**
	 * 	Test Server Port
	 *  @param port port
	 *  @return true if able to create
	 */
	private boolean testServerPort (int port)
	{
		try
		{
			ServerSocket ss = new ServerSocket (port);
			System.out.println("  ServerPort " + ss.getInetAddress() + ":" + ss.getLocalPort());
			ss.close();
		}
		catch (Exception ex)
		{
			System.err.println(ex);
			return false;
		}
		return true;
	}	//	testPort


	/**
	 * 	Test Port
	 *  @param host host
	 *  @param port port
	 *  @param shouldBenUsed true if it should be used
	 *  @return true if some server answered on port
	 */
	public boolean testPort (InetAddress host, int port, boolean shouldBenUsed)
	{
		Socket pingSocket = null;
		try
		{
			pingSocket = new Socket(host, port);
		}
		catch (Exception e)
		{
			if (shouldBenUsed)
				System.err.println("  Open Socket " + host + " on " + port + ": " + e.getMessage());
			return false;
		}
		if (!shouldBenUsed)
			System.err.println("  Open Socket " + host + " on " + port);
		if (pingSocket == null)
			return false;
		//	success
		try
		{
			pingSocket.close();
		}
		catch (IOException e)
		{
			System.out.println("  CloseSocket=" + e.toString());
		}
		return true;
	}	//	testPort

	/**
	 * 	Test JDBC Connection to Server
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testJDBC (String uid, String pwd)
	{
		//	jdbc:oracle:thin:@dev:1521:dev1
		m_connectionString = "jdbc:oracle:thin:@"
			+ m_databaseServer.getHostName() + ":" + m_databasePort + ":" + m_databaseName;
		System.out.println("  JDBC = " + m_connectionString);

		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(m_connectionString,
				uid, pwd);
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testJDBC

	/**
	 * 	Test TNS Connection
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testTNS (String uid, String pwd)
	{
		String connectionString = "jdbc:oracle:oci8:@" + m_TNSName;
		System.out.println("  TNS = " + connectionString);
		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(connectionString,
				uid, pwd);
		}
		catch (UnsatisfiedLinkError ule)
		{
			System.err.println("Check setup of Oracle Server / Oracle Client / LD_LIBRARY_PATH");
			System.err.println(ule.toString());
			return false;
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testTNS

	/**
	 * 	Test Mail
	 *  @return true of OK
	 */
	private boolean testMail()
	{
		boolean smtpOK = false;
		boolean imapOK = false;
		if (testPort (m_mailServer, 25, true))
		{
			System.out.println("OK: SMTP Server contacted");
			smtpOK = true;
		}
		else
			System.err.println("Error: SMTP Server NOT available");
		//
		if (testPort (m_mailServer, 110, true))
			System.out.println("OK: POP3 Server contacted");
		else
			System.err.println("Error: POP3 Server NOT available");
		if (testPort (m_mailServer, 143, true))
		{
			System.out.println("OK: IMAP4 Server contacted");
			imapOK = true;
		}
		else
			System.err.println("Error: IMAP4 Server NOT available");
		//
		if (!smtpOK)
			return false;
		//
		try
		{
			EMail em = new EMail (m_mailServer.getHostName (),
					   m_adminEMail.toString (), m_adminEMail.toString (),
					   "Compiere Server Setup Test", "Test: " + m_properties);
			em.setEMailUser (m_mailUser, m_mailPassword);
			if (EMail.SENT_OK.equals (em.send ()))
			{
				System.out.println ("OK: Send Test Email to " + m_adminEMail);
			}
			else
			{
				System.err.println ("Error: Could NOT send Email to "
					+ m_adminEMail);
			}
		}
		catch (Exception ex)
		{
			System.err.println("testMail - " + ex.getLocalizedMessage());
			return false;
		}

		//
		if (!imapOK)
			return false;

		//	Test Read Mail Access
		Properties props = new Properties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_mailServer.getHostName());
		props.put("mail.user", m_mailUser);
		props.put("mail.smtp.auth", "true");
		System.out.println("  Connecting to " + m_mailServer.getHostName());
		//
		Session session = null;
		Store store = null;
		try
		{
			EMailAuthenticator auth = new EMailAuthenticator (m_mailUser, m_mailPassword);
			session = Session.getDefaultInstance(props, auth);
			session.setDebug (Log.isTraceLevel (10));
			System.out.println("  Session=" + session);
			//	Connect to Store
			store = session.getStore("imap");
			System.out.println("  Store=" + store);
		}
		catch (NoSuchProviderException nsp)
		{
			System.err.println("Error Mail IMAP Provider - " + nsp.getMessage());
			return false;
		}
		catch (Exception e)
		{
			System.err.println("Error Mail IMAP - " + e.getMessage());
			return false;
		}
		try
		{
			store.connect(m_mailServer.getHostName(), m_mailUser, m_mailPassword);
			System.out.println("  Store - connected");
			Folder folder = store.getDefaultFolder();
			Folder inbox = folder.getFolder("INBOX");
			System.out.println("OK: Mail Connect to " + inbox.getFullName() + " #Msg=" + inbox.getMessageCount());
			//
			store.close();
		}
		catch (MessagingException mex)
		{
			System.err.println("Error Mail Connect " + mex.getMessage());
			return false;
		}
		return true;
	}	//	testMail

	/*************************************************************************/

	/**
	 * 	Save Settings
	 */
	private void save()
	{
		SwingWorker sw = startTest();
		while (sw.isAlive())
		{
			try
			{
				Thread.currentThread().sleep(2000);
			}
			catch (InterruptedException ex)
			{
				System.err.println("save-waiting: " + ex);
			}
		}
		sw.get();	//	block
		if (!m_success)
			return;

		//	Add
		m_properties.setProperty("COMPIERE_MAIN_VERSION", Compiere.MAIN_VERSION);
		m_properties.setProperty("COMPIERE_DATE_VERSION", Compiere.DATE_VERSION);
		m_properties.setProperty("COMPIERE_DB_VERSION", Compiere.DB_VERSION);



		//	Before we save, load Ini
		Ini.setClient(false);
		String fileName = m_compiereHome.getAbsolutePath() + File.separator + Ini.COMPIERE_PROPERTY_FILE;
		Ini.loadProperties(fileName);

		//	Save Environment
		fileName = m_compiereHome.getAbsolutePath() + File.separator + COMPIERE_ENV_FILE;
		try
		{
			FileOutputStream fos = new FileOutputStream(new File(fileName));
			m_properties.store(fos, COMPIERE_ENV_FILE);
			fos.flush();
			fos.close();
		}
		catch (Exception e)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + e.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (Throwable t)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + t.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		System.out.println("Properties saved to " + fileName);

		//	Sync Properties
		Ini.setCompiereHome(m_compiereHome.getAbsolutePath());
		CConnection cc = CConnection.get (Database.DB_ORACLE,
			m_databaseServer.getHostName(), m_databasePort, m_databaseName,
			m_databaseUser, m_databasePassword);
		cc.setAppsHost(m_appsServer.getHostName());
		cc.setRMIoverHTTP(false);
		Ini.setProperty(Ini.P_CONNECTION, cc.toStringLong());
		Ini.saveProperties(false);


		JOptionPane.showConfirmDialog(this, res.getString("EnvironmentSaved"),
			res.getString("CompiereServerSetup"),
			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

		((Frame)SwingUtilities.getWindowAncestor(this)).dispose();
		System.exit(0);		//	remains active when License Dialog called
	}	//	save

}	//	ConfigurationPanel
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2002 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.install;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;

import oracle.jdbc.*;

import org.apache.log4j.*;
import org.apache.log4j.Logger;
import org.apache.tools.ant.launch.*;
import org.compiere.*;
import org.compiere.apps.*;
import org.compiere.db.*;
import org.compiere.swing.*;
import org.compiere.util.*;


/**
 *	Configuration Panel
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: ConfigurationPanel.java,v 1.14 2004/09/09 14:19:17 jjanke Exp $
 */
public class ConfigurationPanel extends CPanel implements ActionListener
{
	/**
	 * 	Constructor
	 *  @param statusBar for info
	 * 	@throws Exception
	 */
	public ConfigurationPanel (JLabel statusBar) throws Exception
	{
		LogManager.resetConfiguration();
		Logger root = Logger.getRootLogger();
	//	root.addAppender(new ConsoleAppender(new PatternLayout("%d{ABSOLUTE} %-5p [%c{1}] %m%n")));
		root.addAppender(new ConsoleAppender(new LogLayout()));
		root.setLevel(Level.ALL);
		Log.setTraceLevel(10);
		//
		m_statusBar = statusBar;
		jbInit();
		dynInit();
	}	//	ConfigurationPanel

	public static final String	KEYSTORE_PASSWORD		= "myPassword";
	
	public static final String	COMPIERE_ENV_FILE		= "CompiereEnv.properties";

	public static final String	COMPIERE_HOME 			= "COMPIERE_HOME";
	public static final String	JAVA_HOME 				= "JAVA_HOME";
	public static final String	COMPIERE_JAVA_OPTIONS 	= "COMPIERE_JAVA_OPTIONS";

	public static final String	COMPIERE_APPS_SERVER 	= "COMPIERE_APPS_SERVER";
	public static final String	COMPIERE_JNP_PORT 		= "COMPIERE_JNP_PORT";
	public static final String	COMPIERE_WEB_PORT 		= "COMPIERE_WEB_PORT";
	public static final String	COMPIERE_SSL_PORT 		= "COMPIERE_SSL_PORT";
	public static final String	COMPIERE_WEB_ALIAS 		= "COMPIERE_WEB_ALIAS";
	
	public static final String	COMPIERE_KEYSTORE 		= "COMPIERE_KEYSTORE";
	public static final String	COMPIERE_KEYSTOREPASS	= "COMPIERE_KEYSTOREPASS";
	
	public static final String	COMPIERE_DB_SERVER 		= "COMPIERE_DB_SERVER";
	public static final String	COMPIERE_DB_PORT 		= "COMPIERE_DB_PORT";
	public static final String	COMPIERE_DB_NAME 		= "COMPIERE_DB_NAME";

	public static final String	COMPIERE_DB_USER 		= "COMPIERE_DB_USER";
	public static final String	COMPIERE_DB_PASSWORD 	= "COMPIERE_DB_PASSWORD";
	public static final String	COMPIERE_DB_SYSTEM 		= "COMPIERE_DB_SYSTEM";

	public static final String	COMPIERE_DB_URL 		= "COMPIERE_DB_URL";
	public static final String	COMPIERE_DB_TNS 		= "COMPIERE_DB_TNS";

	public static final String	COMPIERE_MAIL_SERVER 	= "COMPIERE_MAIL_SERVER";
	public static final String	COMPIERE_MAIL_USER 		= "COMPIERE_MAIL_USER";
	public static final String	COMPIERE_MAIL_PASSWORD 	= "COMPIERE_MAIL_PASSWORD";
	public static final String	COMPIERE_ADMIN_EMAIL	= "COMPIERE_ADMIN_EMAIL";

	public static final String	COMPIERE_FTP_SERVER		= "COMPIERE_FTP_SERVER";
	public static final String	COMPIERE_FTP_USER		= "COMPIERE_FTP_USER";
	public static final String	COMPIERE_FTP_PASSWORD	= "COMPIERE_FTP_PASSWORD";
	public static final String	COMPIERE_FTP_PREFIX		= "COMPIERE_FTP_PREFIX";

	private String			m_errorString;
	private File			m_javaHome;
	private File			m_compiereHome;
	private InetAddress		m_appsServer;
	private int				m_WebPort;
	private int				m_JNPPort;
	private InetAddress		m_databaseServer;
	private int				m_databasePort;
	private String			m_databaseName;
	private String			m_connectionString;
	private String			m_systemPassword;
	private String			m_databaseUser;
	private String			m_databasePassword;
	private String			m_TNSName;

	private InetAddress		m_mailServer;
	private InternetAddress	m_adminEMail;
	private String			m_mailUser;
	private String			m_mailPassword;

	private volatile boolean		m_success = false;
	private volatile boolean	 	m_testing = false;

	/** Translation				*/
	static ResourceBundle 		res = ResourceBundle.getBundle("org.compiere.install.SetupRes");

	/**	Driver					*/
	private static Driver		s_driver = null;
	/** Environment Properties	*/
	private Properties			m_properties = new Properties();
	/**	Setup Frame				*/
	private Setup				m_setup = null;
	/** Status Bar				*/
	private JLabel 				m_statusBar;

	private static ImageIcon iOpen = new ImageIcon(ConfigurationPanel.class.getResource("openFile.gif"));
	private static ImageIcon iSave = new ImageIcon(Compiere.class.getResource("images/Save16.gif"));
	private static ImageIcon iHelp = new ImageIcon(Compiere.class.getResource("images/Help16.gif"));


	//	-------------	Static UI
	private GridBagLayout gridBagLayout = new GridBagLayout();
	private CLabel lCompiereHome = new CLabel();
	private CTextField fCompiereHome = new CTextField();
	private CLabel lWebPort = new CLabel();
	private CTextField fWebPort = new CTextField();
	private CLabel lAppsServer = new CLabel();
	private CTextField fAppsServer = new CTextField();
	private CLabel lDatabaseType = new CLabel();
	private CComboBox fDatabaseType = new CComboBox();
	private CLabel lDatabaseName = new CLabel();
	private CLabel lDatabasePort = new CLabel();
	private CLabel lDatabaseUser = new CLabel();
	private CLabel lDatabasePassword = new CLabel();
	private CTextField fDatabaseName = new CTextField();
	private CTextField fDatabasePort = new CTextField();
	private CTextField fDatabaseUser = new CTextField();
	private CPassword fDatabasePassword = new CPassword();
	private CLabel lTNSName = new CLabel();
	private CComboBox fTNSName = new CComboBox();
	private CLabel lSystemPassword = new CLabel();
	private CPassword fSystemPassword = new CPassword();
	private CLabel lMailServer = new CLabel();
	private CTextField fMailServer = new CTextField();
	private CLabel lAdminEMail = new CLabel();
	private CTextField fAdminEMail = new CTextField();
	private CLabel lDatabaseServer = new CLabel();
	private CTextField fDatabaseServer = new CTextField();
	private CLabel lJavaHome = new CLabel();
	private CTextField fJavaHome = new CTextField();
	private CButton bCompiereHome = new CButton(iOpen);
	private CButton bJavaHome = new CButton(iOpen);
	private CButton bHelp = new CButton(iHelp);
	private CButton bTest = new CButton();
	private CButton bSave = new CButton(iSave);
	private CLabel lJNPPort = new CLabel();
	private CTextField fJNPPort = new CTextField();
	private CLabel lMailUser = new CLabel();
	private CLabel lMailPassword = new CLabel();
	private CTextField fMailUser = new CTextField();
	private CPassword fMailPassword = new CPassword();
	private CCheckBox okJavaHome = new CCheckBox();
	private CCheckBox okCompiereHome = new CCheckBox();
	private CCheckBox okAppsServer = new CCheckBox();
	private CCheckBox okWebPort = new CCheckBox();
	private CCheckBox okJNPPort = new CCheckBox();
	private CLabel lSSLPort = new CLabel();
	private CTextField fSSLPort = new CTextField();
	private CCheckBox okSSLPort = new CCheckBox();
	private CCheckBox okDatabaseServer = new CCheckBox();
	private CCheckBox okMailServer = new CCheckBox();
	private CCheckBox okMailUser = new CCheckBox();
	private CCheckBox okDatabaseUser = new CCheckBox();
	private CCheckBox okDatabaseName = new CCheckBox();
	private CLabel lKeyStore = new CLabel();
	private CPassword fKeyStore = new CPassword();
	private CCheckBox okKeyStore = new CCheckBox();
	private CCheckBox okTNSName = new CCheckBox();

	/**
	 * 	Static Layout Init
	 *  @throws Exception
	 */
	private void jbInit() throws Exception
	{
		lCompiereHome.setToolTipText(res.getString("CompiereHomeInfo"));
		lCompiereHome.setText(res.getString("CompiereHome"));
		this.setLayout(gridBagLayout);
		fCompiereHome.setText("C:\\Compiere2");
		lWebPort.setToolTipText(res.getString("WebPortInfo"));
		lWebPort.setText(res.getString("WebPort"));
		fWebPort.setText("80");
		lAppsServer.setToolTipText(res.getString("AppsServerInfo"));
		lAppsServer.setText(res.getString("AppsServer"));
		fAppsServer.setText("server.company.com");
		lDatabaseType.setToolTipText(res.getString("DatabaseTypeInfo"));
		lDatabaseType.setText(res.getString("DatabaseType"));
		lDatabaseName.setToolTipText(res.getString("DatabaseNameInfo"));
		lDatabaseName.setText(res.getString("DatabaseName"));
		lDatabasePort.setToolTipText(res.getString("DatabasePortInfo"));
		lDatabasePort.setText(res.getString("DatabasePort"));
		lDatabaseUser.setToolTipText(res.getString("DatabaseUserInfo"));
		lDatabaseUser.setText(res.getString("DatabaseUser"));
		lDatabasePassword.setToolTipText(res.getString("DatabasePasswordInfo"));
		lDatabasePassword.setText(res.getString("DatabasePassword"));
		fDatabaseName.setText("compiere");
		fDatabasePort.setText("1521");
		fDatabaseUser.setText("compiere");
		fDatabasePassword.setText("compiere");
		lTNSName.setToolTipText(res.getString("TNSNameInfo"));
		lTNSName.setText(res.getString("TNSName"));
		fTNSName.setEditable(true);
		lSystemPassword.setToolTipText(res.getString("SystemPasswordInfo"));
		lSystemPassword.setText(res.getString("SystemPassword"));
		fSystemPassword.setText("manager");
		lMailServer.setToolTipText(res.getString("MailServerInfo"));
		lMailServer.setText(res.getString("MailServer"));
		fMailServer.setText("mail.company.com");
		lAdminEMail.setToolTipText(res.getString("AdminEMailInfo"));
		lAdminEMail.setText(res.getString("AdminEMail"));
		fAdminEMail.setText("admin@company.com");
		bTest.setToolTipText(res.getString("TestInfo"));
		bTest.setText(res.getString("Test"));
		bSave.setToolTipText(res.getString("SaveInfo"));
		bSave.setText(res.getString("Save"));
		lDatabaseServer.setToolTipText(res.getString("DatabaseServerInfo"));
		lDatabaseServer.setText(res.getString("DatabaseServer"));
		lJavaHome.setToolTipText(res.getString("JavaHomeInfo"));
		lJavaHome.setText(res.getString("JavaHome"));
		fJavaHome.setText("Java142");
		lJNPPort.setToolTipText(res.getString("JNPPortInfo"));
		lJNPPort.setText(res.getString("JNPPort"));
		fJNPPort.setText("1099");
		bHelp.setToolTipText(res.getString("HelpInfo"));
		lMailUser.setToolTipText(res.getString("MailUserInfo"));
		lMailUser.setText(res.getString("MailUser"));
		lMailPassword.setToolTipText(res.getString("MailPasswordInfo"));
		lMailPassword.setText(res.getString("MailPassword"));
		fMailUser.setText("compiere");
		fMailPassword.setText("compiere");

		//
		lSSLPort.setText("SSL");
		fSSLPort.setText("443");
		okMailUser.setEnabled(false);
		lKeyStore.setText(res.getString("KeyStorePassword"));
		lKeyStore.setToolTipText(res.getString("KeyStorePasswordInfo"));
		okJavaHome.setEnabled(false);
		okCompiereHome.setEnabled(false);
		okCompiereHome.setDoubleBuffered(false);
		okAppsServer.setEnabled(false);
		okWebPort.setEnabled(false);
		okWebPort.setDoubleBuffered(false);
		okSSLPort.setEnabled(false);
		okJNPPort.setEnabled(false);
		fKeyStore.setText(KEYSTORE_PASSWORD);
		okDatabaseServer.setEnabled(false);
		okTNSName.setEnabled(false);
		okMailServer.setEnabled(false);
		okDatabaseName.setEnabled(false);
		okDatabaseUser.setEnabled(false);
		okKeyStore.setEnabled(false);
		bJavaHome.setMargin(new Insets(2, 10, 2, 10));
		bCompiereHome.setMaximumSize(new Dimension(43, 27));
		bCompiereHome.setMargin(new Insets(2, 10, 2, 10));
		this.add(lCompiereHome,                       new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fCompiereHome,                         new GridBagConstraints(1, 1, 2, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(lWebPort,                       new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fWebPort,                        new GridBagConstraints(1, 3, 4, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(lAppsServer,                      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fAppsServer,                       new GridBagConstraints(1, 2, 3, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(lDatabaseType,                       new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseType,                      new GridBagConstraints(1, 4, 5, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabasePort,                      new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseUser,                       new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseName,                         new GridBagConstraints(1, 6, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabasePort,                        new GridBagConstraints(1, 7, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseUser,                         new GridBagConstraints(1, 8, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lTNSName,                       new GridBagConstraints(7, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(fTNSName,                            new GridBagConstraints(8, 5, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(lMailServer,                      new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lSystemPassword,                       new GridBagConstraints(7, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fSystemPassword,                       new GridBagConstraints(8, 7, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(fDatabasePassword,                        new GridBagConstraints(8, 8, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 0), 0, 0));
		this.add(lDatabasePassword,                      new GridBagConstraints(7, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailServer,                      new GridBagConstraints(1, 9, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(bTest,                      new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 10, 5), 0, 0));
		this.add(bSave,                       new GridBagConstraints(8, 11, 2, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 10, 5), 0, 0));
		this.add(lDatabaseServer,                      new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseServer,                     new GridBagConstraints(1, 5, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseName,                   new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lJavaHome,                  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJavaHome,                        new GridBagConstraints(1, 0, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(bCompiereHome,                 new GridBagConstraints(7, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bHelp,                     new GridBagConstraints(7, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 10, 5), 0, 0));
		this.add(bJavaHome,                 new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lMailPassword,                  new GridBagConstraints(7, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailPassword,                    new GridBagConstraints(8, 10, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 0), 0, 0));
		this.add(lAdminEMail,               new GridBagConstraints(7, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailUser,               new GridBagConstraints(1, 10, 5, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fAdminEMail,                 new GridBagConstraints(8, 9, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 0), 0, 0));
		this.add(lMailUser,              new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 0), 0, 0));
		this.add(lJNPPort,         new GridBagConstraints(7, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJNPPort,         new GridBagConstraints(8, 2, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(okJNPPort,          new GridBagConstraints(9, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(lSSLPort,      new GridBagConstraints(7, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fSSLPort,      new GridBagConstraints(8, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(okSSLPort,     new GridBagConstraints(9, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okDatabaseServer,         new GridBagConstraints(6, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okMailServer,      new GridBagConstraints(6, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 5, 5), 0, 0));
		this.add(okJavaHome,   new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okCompiereHome,  new GridBagConstraints(6, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okAppsServer,  new GridBagConstraints(6, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okWebPort,  new GridBagConstraints(6, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(lKeyStore,  new GridBagConstraints(7, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(fKeyStore,  new GridBagConstraints(8, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		this.add(okKeyStore,  new GridBagConstraints(9, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okTNSName, new GridBagConstraints(9, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		//
		bCompiereHome.addActionListener(this);
		bJavaHome.addActionListener(this);
		bHelp.addActionListener(this);
		bTest.addActionListener(this);
		bSave.addActionListener(this);
		this.add(okDatabaseName,  new GridBagConstraints(9, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
		this.add(okDatabaseUser,   new GridBagConstraints(9, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 5, 5), 0, 0));
		this.add(okMailUser,  new GridBagConstraints(9, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
	}	//	jbInit


	/**
	 * 	Dynamic Init
	 *  @throws Exception
	 */
	private void dynInit() throws Exception
	{
		//	DatabaseType
		fDatabaseType.addItem("Oracle 10g (9i2)");

		//	Compiere Home
		String ch = System.getProperty(COMPIERE_HOME);
		if (ch == null || ch.length() == 0)
			ch = System.getProperty("user.dir");
		fCompiereHome.setText(ch);
		boolean envLoaded = false;
		String fileName = ch + File.separator + COMPIERE_ENV_FILE;
		File env = new File (fileName);
		System.out.println(env + " - exists=" + env.exists());
		if (env.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(env);
				m_properties.load(fis);
				fis.close();
			}
			catch (Exception e)
			{
				System.err.println(e.toString());
			}
			System.out.println("Loading ...");
			if (m_properties.size() > 5)
				envLoaded = true;
			//
			fCompiereHome.setText((String)m_properties.get(COMPIERE_HOME));
			fJavaHome.setText((String)m_properties.get(JAVA_HOME));
			fAppsServer.setText((String)m_properties.get(COMPIERE_APPS_SERVER));
			fJNPPort.setText((String)m_properties.get(COMPIERE_JNP_PORT));
			fWebPort.setText((String)m_properties.get(COMPIERE_WEB_PORT));
			fSSLPort.setText((String)m_properties.get(COMPIERE_SSL_PORT));
			String s = (String)m_properties.get(COMPIERE_KEYSTOREPASS);
			if (s == null || s.length() == 0)
				s = KEYSTORE_PASSWORD;
			fKeyStore.setText(s);

			fDatabaseServer.setText((String)m_properties.get(COMPIERE_DB_SERVER));
			fDatabasePort.setText((String)m_properties.get(COMPIERE_DB_PORT));
			fDatabaseName.setText((String)m_properties.get(COMPIERE_DB_NAME));

			fDatabaseUser.setText((String)m_properties.get(COMPIERE_DB_USER));
			fDatabasePassword.setText((String)m_properties.get(COMPIERE_DB_PASSWORD));
			fSystemPassword.setText((String)m_properties.get(COMPIERE_DB_SYSTEM));
			fillTNSName((String)m_properties.get(COMPIERE_DB_TNS));

			fMailServer.setText((String)m_properties.get(COMPIERE_MAIL_SERVER));
			fMailUser.setText((String)m_properties.get(COMPIERE_MAIL_USER));
			fMailPassword.setText((String)m_properties.get(COMPIERE_MAIL_PASSWORD));
			fAdminEMail.setText((String)m_properties.get(COMPIERE_ADMIN_EMAIL));
		}

		//	No environment file found - defaults
		if (!envLoaded)
		{
			System.out.println("Setting Defaults");
			//	Java Home, e.g. D:\j2sdk1.4.1\jre
			String javaHome = System.getProperty("java.home");
			if (javaHome.endsWith("jre"))
				javaHome = javaHome.substring(0, javaHome.length()-4);
			fJavaHome.setText(javaHome);
			//	AppsServer
			fAppsServer.setText(InetAddress.getLocalHost().getHostName());
			//	Database Server
			fDatabaseServer.setText(InetAddress.getLocalHost().getHostName());
			//	Mail Server
			fMailServer.setText(InetAddress.getLocalHost().getHostName());
			//  Get TNS Names
			fillTNSName("");
		}	//	!envLoaded

		//	Default FTP stuff
		if (!m_properties.containsKey(COMPIERE_FTP_SERVER))
		{
			m_properties.setProperty(COMPIERE_FTP_SERVER, "localhost");
			m_properties.setProperty(COMPIERE_FTP_USER, "anonymous");
			m_properties.setProperty(COMPIERE_FTP_PASSWORD, "user@host.com");
			m_properties.setProperty(COMPIERE_FTP_PREFIX, "my");
		}
		//	Default Java Options
		if (!m_properties.containsKey(COMPIERE_JAVA_OPTIONS))
			m_properties.setProperty(COMPIERE_JAVA_OPTIONS, "-Xms64M -Xmx512M");
		//	Web Alias
		if (!m_properties.containsKey(COMPIERE_WEB_ALIAS))
			m_properties.setProperty(COMPIERE_WEB_ALIAS, InetAddress.getLocalHost().getCanonicalHostName());
		
		//	(String)m_properties.get(COMPIERE_DB_URL)	//	derived
	}	//	dynInit

	/**
	 * 	Fill TNS Name
	 * 	@param defaultValue defaultValue
	 */
	private void fillTNSName (String defaultValue)
	{
		//	default value to lowercase or null
		String def = defaultValue;
		if (def != null && def.trim().length() == 0)
			def = null;
		if (def != null)
		{
			def = def.toLowerCase();
			fTNSName.addItem(def);
		}

		//	Search for Oracle Info
		String path = System.getProperty("java.library.path");
		String[] entries = path.split(File.pathSeparator);
		for (int e = 0; e < entries.length; e++)
		{
			String entry = entries[e].toLowerCase();
			if (entry.indexOf("ora") != -1 && entry.endsWith("bin"))
			{
				StringBuffer sb = getTNS_File (entries[e].substring(0, entries[e].length()-4));
				String[] tnsnames = getTNS_Names (sb);
				if (tnsnames != null)
				{
					for (int i = 0; i < tnsnames.length; i++)
					{
						String tns = tnsnames[i];
						if (!tns.equals(def))
							fTNSName.addItem(tns);
					}
					break;
				}
			}
		}	//	for all path entries

		//	Set first entry
		fTNSName.addActionListener(this);
		if (fTNSName.getItemCount() > 0)
			fTNSName.setSelectedIndex(0);
	}	//	fillTNSName

	/**
	 * 	Get File tnmsnames.ora in StringBuffer
	 * 	@param oraHome ORACLE_HOME
	 * 	@return tnsnames.ora or null
	 */
	private StringBuffer getTNS_File (String oraHome)
	{
		String tnsnames = oraHome + File.separator
			+ "network" + File.separator
			+ "admin" + File.separator
			+ "tnsnames.ora";
		File tnsfile = new File (tnsnames);
		if (!tnsfile.exists())
			return null;

		System.out.println("Searching " + tnsnames);
		StringBuffer sb = new StringBuffer();
		try
		{
			FileReader fr = new FileReader (tnsfile);
			int c;
			while ((c = fr.read()) != -1)
				sb.append((char)c);
		}
		catch (IOException ex)
		{
			System.err.println("Error Reading " + tnsnames);
			ex.printStackTrace();
			return null;
		}
		if (sb.length() == 0)
			return null;
		return sb;
	}	//	getTNS_File

	/**
	 * 	Get TNS Names entries.
	 * 	Assumes standard tnsmanes.ora formatting of NetMgr
	 * 	@param tnsnames content of tnsnames.ora
	 * 	@return tns names or null
	 */
	private String[] getTNS_Names (StringBuffer tnsnames)
	{
		if (tnsnames == null)
			return null;

		ArrayList list = new ArrayList();
		Pattern pattern = Pattern.compile("$", Pattern.MULTILINE);
		String[] lines = pattern.split(tnsnames);
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i].trim();
			if (line.length() > 0
				&& Character.isLetter(line.charAt(0))	//	no # (
				&& line.indexOf("=") != -1
				&& line.indexOf("EXTPROC_") == -1
				&& line.indexOf("_HTTP") == -1)
			{
				String entry = line.substring(0, line.indexOf('=')).trim().toLowerCase();
				System.out.println("- " + entry);
				list.add(entry);
			}
		}
		//	Convert to Array
		if (list.size() == 0)
			return null;
		String[] retValue = new String[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getTNS_Names

	
	/**************************************************************************
	 * 	ActionListener
	 *  @param e event
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (m_testing)
			return;
		//	TNS Name Changed
		if (e.getSource() == fTNSName)
		{
			String tns = (String)fTNSName.getSelectedItem();
			if (tns != null)
			{
				int pos = tns.indexOf('.');
				if (pos != -1)
					tns = tns.substring(0, pos);
				fDatabaseName.setText(tns);
			}
		}

		else if (e.getSource() == bJavaHome)
			setPath (fJavaHome);
		else if (e.getSource() == bCompiereHome)
			setPath (fCompiereHome);
		else if (e.getSource() == bHelp)
			new Setup_Help((Frame)SwingUtilities.getWindowAncestor(this));
		else if (e.getSource() == bTest)
			startTest(false);
		else if (e.getSource() == bSave)
			startTest(true);
	}	//	actionPerformed

	/**
	 * 	Set Path in Field
	 * 	@param field field to set Path
	 */
	private void setPath (CTextField field)
	{
		JFileChooser fc = new JFileChooser(field.getText());
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			field.setText(fc.getSelectedFile().getAbsolutePath());
	}	//	setPath

	
	/**************************************************************************
	 * 	Start Test Async.
	 *  @return SwingWorker
	 */
	private SwingWorker startTest(final boolean saveIt)
	{
		SwingWorker worker = new SwingWorker()
		{
			//	Start it
			public Object construct()
			{
				m_testing = true;
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				bTest.setEnabled(false);
				m_success = false;
				m_errorString = null;
				try
				{
					test();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					m_errorString += "\n" + ex.toString();
				}
				//
				setCursor(Cursor.getDefaultCursor());
				if (m_errorString == null)
					m_success = true;
				bTest.setEnabled(true);
				m_testing = false;
				return new Boolean(m_success);
			}
			//	Finish it
			public void finished()
			{
				if (m_errorString != null)
				{
					System.err.println(m_errorString);
					JOptionPane.showConfirmDialog (null, m_errorString, res.getString("ServerError"),
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
				}
				else if (saveIt)
					save();
			}
		};
		worker.start();
		return worker;
	}	//	startIt

	/**
	 *	Test it
	 * 	@throws Exception
	 */
	private void test() throws Exception
	{
		System.out.println();
		System.out.println("** Test **");
		bSave.setEnabled(false);


		//	Java Home
		m_errorString = res.getString("ErrorJavaHome");
		m_javaHome = new File (fJavaHome.getText());
		if (!signalOK(okJavaHome, m_javaHome.exists(), true, "Not found: Java Home"))
			return;
		File tools = new File (fJavaHome.getText() + File.separator + "lib" + File.separator + "tools.jar");
		if (!signalOK(okJavaHome, tools.exists(), true, 
				"Not found: Java SDK = " + tools))
			return;
		System.out.println("OK: JavaHome=" + m_javaHome.getAbsolutePath());
		m_properties.setProperty(JAVA_HOME, m_javaHome.getAbsolutePath());
		System.setProperty(JAVA_HOME, m_javaHome.getAbsolutePath());
		
		
		//	Java Version
		final String VERSION = "1.4.1";
		final String VERSION2 = "1.4.2";
		boolean versionOK = false;
		String jh = m_javaHome.getAbsolutePath();
		if (jh.indexOf(VERSION) != -1)	//	file name has version = assuming OK
			versionOK = true;
		if (!versionOK && jh.indexOf(VERSION2) != -1)	//
			versionOK = true;
		String thisJH = System.getProperty("java.home");
		if (thisJH.indexOf(jh) != -1)	//	we are running the version currently
		{
			String thisJV = System.getProperty("java.version");
			versionOK = thisJV.indexOf(VERSION) != -1;
			if (!versionOK && thisJV.indexOf(VERSION2) != -1)
				versionOK = true;
			if (versionOK)
			  System.out.println("  Java Version OK = " + thisJV);
		}
		if (!signalOK(okJavaHome, versionOK, true, 
				"Wrong Java Version: Should be " + VERSION2))
			return;

		
		//	Compiere Home
		m_errorString = res.getString("ErrorCompiereHome");
		m_compiereHome = new File (fCompiereHome.getText());
		if (!signalOK(okCompiereHome, m_compiereHome.exists(), true, "Not found: CompiereHome = " + m_compiereHome))
			return;
		System.out.println("OK: CompiereHome = " + m_compiereHome);
		m_properties.setProperty(COMPIERE_HOME, m_compiereHome.getAbsolutePath());
		System.setProperty(COMPIERE_HOME, m_compiereHome.getAbsolutePath());
		

		//	AppsServer
		m_errorString = res.getString("ErrorAppsServer");
		String server = fAppsServer.getText();
		boolean error = (server == null || server.length() == 0 
			|| server.toLowerCase().indexOf("localhost") != -1 || server.equals("127.0.0.1"));
		if (!error)
			m_appsServer = InetAddress.getByName(server);
		if (!signalOK(okAppsServer, !error, true, 
				"Not correct: AppsServer = " + server))
			return;
		System.out.println("OK: AppsServer = " + m_appsServer.getHostName());
		m_properties.setProperty(COMPIERE_APPS_SERVER, m_appsServer.getHostName());

		
		//	JNP Port
		m_errorString = res.getString("ErrorJNPPort");
		m_statusBar.setText(lJNPPort.getText());
		m_JNPPort = Integer.parseInt(fJNPPort.getText());
		error = (testPort (m_appsServer, m_JNPPort, false) || !testServerPort(m_JNPPort));
		if (!signalOK(okJNPPort, !error, true, "Not correct: JNP Port = " + m_JNPPort))
			return;
		System.out.println("OK: JNPPort = " + m_JNPPort);
		m_properties.setProperty(COMPIERE_JNP_PORT, String.valueOf(m_JNPPort));


		//	Web Port
		m_errorString = res.getString("ErrorWebPort");
		m_statusBar.setText(lWebPort.getText());
		m_WebPort = Integer.parseInt(fWebPort.getText());
		error = (testPort ("http", m_appsServer.getHostName(), m_WebPort, "/") 
			|| !testServerPort(m_WebPort));
		if (!signalOK(okWebPort, !error, true, 
				"Not correct: Web Port = " + m_WebPort))
			return;
		System.out.println("OK: Web Port = " + m_WebPort);
		m_properties.setProperty(COMPIERE_WEB_PORT, String.valueOf(m_WebPort));
		
		//	SSL Port
		int sslPort = Integer.parseInt(fSSLPort.getText());
		error = (testPort ("https", m_appsServer.getHostName(), sslPort, "/") 
			|| !testServerPort(sslPort));
		if (!signalOK(okSSLPort, !error, true, 
				"Not correct: SSL Port = " + sslPort))
			return;
		System.out.println("OK: SSL Port = " + sslPort);
		m_properties.setProperty(COMPIERE_SSL_PORT, String.valueOf(sslPort));

		
		//	KeyStore
		m_errorString = res.getString("KeyStorePassword");
		String fileName = MyKeyStore.getKeystoreFileName(m_compiereHome.getAbsolutePath());
		m_properties.setProperty(COMPIERE_KEYSTORE, fileName);
		
		//	KeyStore Password
		String pw = new String(fKeyStore.getPassword());
		if (!signalOK(okKeyStore, pw != null && pw.length() > 0, true, 
				"Invalid Key Store Password = " + pw))
			return;
		m_properties.setProperty(COMPIERE_KEYSTOREPASS, pw);
		MyKeyStore ks = new MyKeyStore (fileName, fKeyStore.getPassword());
		String errorString = ks.verify();
		if (!signalOK(okKeyStore, errorString == null, true, errorString))
			return;
		System.out.println("OK: KeyStore = " + fileName);
		
		
		//	Database Server
		m_errorString = res.getString("ErrorDatabaseServer");
		m_statusBar.setText(lDatabaseServer.getText());
		server = fDatabaseServer.getText();
		error = (server == null || server.length() == 0 
			|| server.toLowerCase().indexOf("localhost") != -1 || server.equals("127.0.0.1"));
		if (!error)
			m_databaseServer = InetAddress.getByName(server);
		if (!signalOK(okDatabaseServer, !error, true, 
				"Not correct: DB Server = " + server))
			return;
		System.out.println("OK: Database Server = " + m_databaseServer.getHostName());
		m_properties.setProperty(COMPIERE_DB_SERVER, m_databaseServer.getHostName());


		//	Database Port
		m_errorString = res.getString("ErrorDatabasePort");
		m_databasePort = Integer.parseInt(fDatabasePort.getText());
		if (!signalOK(okDatabaseServer, testPort (m_databaseServer, m_databasePort, true), true, 
				"Not correct: DB Server Port = " + m_databasePort))
			return;
		System.out.println("OK: Database Port = " + m_databasePort);
		m_properties.setProperty(COMPIERE_DB_PORT, String.valueOf(m_databasePort));


		//	JDBC Database Info
		m_errorString = res.getString("ErrorJDBC");
		m_statusBar.setText(lDatabaseName.getText());
		m_databaseName = fDatabaseName.getText();	//	SID
		m_systemPassword = new String(fSystemPassword.getPassword());
		if (!signalOK(okDatabaseName, m_systemPassword != null && m_systemPassword.length() > 0, true, 
				"Invalid Password"))
			return;
		if (!signalOK(okDatabaseName, testJDBC("system", m_systemPassword), true,
				"Error connecting to Database: " + m_databaseName 
				+ " as system/" + m_systemPassword))
			return;
		System.out.println("OK: Database Name = " + m_databaseName);
		m_properties.setProperty(COMPIERE_DB_NAME, m_databaseName);
		System.out.println("OK: Database SystemID = system/" + m_systemPassword);
		m_properties.setProperty(COMPIERE_DB_SYSTEM, m_systemPassword);

		//	URL (derived)	jdbc:oracle:thin:@prod1:1521:prod1
		StringBuffer url = new StringBuffer("jdbc:oracle:thin:@")
			.append(m_databaseServer.getHostName()).append(":")
			.append(m_databasePort).append(":").append(m_databaseName);
		System.out.println("OK: Database URL = " + url.toString());
		m_properties.setProperty(COMPIERE_DB_URL, url.toString());


		//	Database User Info
		m_databaseUser = fDatabaseUser.getText();	//	UID
		m_databasePassword = new String(fDatabasePassword.getPassword());	//	PWD
		if (!signalOK(okDatabaseUser, m_databasePassword != null && m_databasePassword.length() > 0, true, 
				"Invalid Password"))
			return;
		//	Ignore result as it might not be imported
		if (signalOK(okDatabaseUser, testJDBC(m_databaseUser, m_databasePassword), false,
				"Not created yet for Database: " + m_databaseName 
				+ " User: " + m_databaseUser + "/" + m_databasePassword))
			System.out.println("OK: Database UserID = " + m_databaseUser + "/" + m_databasePassword);
		m_properties.setProperty(COMPIERE_DB_USER, m_databaseUser);
		m_properties.setProperty(COMPIERE_DB_PASSWORD, m_databasePassword);


		//	TNS Name Info
		m_errorString = res.getString("ErrorTNS");
		m_statusBar.setText(lTNSName.getText());
		m_TNSName = (String)fTNSName.getSelectedItem();
		signalOK(okTNSName, testTNS("system", m_systemPassword), true,
			"Error connecting to Database: " + m_databaseName 
			+ " via TNS Name: " + m_TNSName);
		System.out.println("OK: Database TNS Name = " + m_TNSName);
		m_properties.setProperty(COMPIERE_DB_TNS, m_TNSName);


		//	Mail Server
		m_errorString = res.getString("ErrorMailServer");
		m_statusBar.setText(lMailServer.getText());
		server = fMailServer.getText();
		error = (server == null || server.length() == 0 
				|| server.toLowerCase().indexOf("localhost") != -1 || server.equals("127.0.0.1"));
		if (!error)
			m_mailServer = InetAddress.getByName(server);
		if (!signalOK(okMailServer, !error, true,
				"Error Mail Server = " + server))
			return;
		System.out.println("OK: Mail Server = " + m_mailServer);
		m_properties.setProperty(COMPIERE_MAIL_SERVER, m_mailServer.getHostName());


		//	Mail User
		m_errorString = "ErrorMailUser";
		m_statusBar.setText(lMailUser.getText());
		m_mailUser = fMailUser.getText();
		m_mailPassword = new String(fMailPassword.getPassword());
		m_properties.setProperty(COMPIERE_MAIL_USER, m_mailUser);
		m_properties.setProperty(COMPIERE_MAIL_PASSWORD, m_mailPassword);
		System.out.println("  Mail User = " + m_mailUser + "/" + m_mailPassword);

		//	Mail Address
		m_errorString = res.getString("ErrorMail");
		m_adminEMail = new InternetAddress (fAdminEMail.getText());
		//
		if (signalOK(okMailUser, testMail(), false, 
				"Not verified Admin EMail = " + m_adminEMail))
			System.out.println("OK: Admin EMail = " + m_adminEMail);
		m_properties.setProperty(COMPIERE_ADMIN_EMAIL, m_adminEMail.toString());

		//
		m_statusBar.setText(res.getString("Ok"));
		System.out.println("** Test OK **");
		bSave.setEnabled(true);
		m_errorString = null;
	}	//	test

	/**
	 * 	Signal OK
	 *	@param cb ckeck box
	 *	@param pass trus if test passed
	 *	@param critical true if critial
	 *	@param srrorMsg error Message
	 *	@return pass
	 */
	private boolean signalOK (CCheckBox cb, boolean pass, boolean critical, String errorMsg)
	{
		cb.setSelected(pass);
		if (pass)
			cb.setToolTipText(null);
		else
		{
			cb.setToolTipText(errorMsg);
			if (critical)
				System.err.println(errorMsg);
			else
				System.out.println(errorMsg);
			m_errorString += " \n(" + errorMsg + ")";
		}
		if (!pass && critical)
			cb.setBackground(Color.RED);
		else
			cb.setBackground(Color.GREEN);
		return pass;
	}	//	setOK

	/**
	 * 	Test Apps Server Port (client perspective)
	 *  @param protocol protocol (http, ..)
	 *  @param server server name
	 *  @param port port
	 *  @param file file name
	 *  @return true if able to connect
	 */
	private boolean testPort (String protocol, String server, int port, String file)
	{
		URL url = null;
		try
		{
			url = new URL (protocol, server, port, file);
		}
		catch (MalformedURLException ex)
		{
			ex.printStackTrace();
			return false;
		}
		System.out.println("  URL=" + url);
		try
		{
			URLConnection c = url.openConnection();
			Object o = c.getContent();
			System.err.println("  URL Connection in use=" + url);	//	error
		}
		catch (Exception ex)
		{
			return false;
		}
		return true;
	}	//	testPort

	/**
	 * 	Test Server Port
	 *  @param port port
	 *  @return true if able to create
	 */
	private boolean testServerPort (int port)
	{
		try
		{
			ServerSocket ss = new ServerSocket (port);
			System.out.println("  ServerPort " + ss.getInetAddress() + ":" + ss.getLocalPort());
			ss.close();
		}
		catch (Exception ex)
		{
			System.err.println(ex);
			return false;
		}
		return true;
	}	//	testPort


	/**
	 * 	Test Port
	 *  @param host host
	 *  @param port port
	 *  @param shouldBeUsed true if it should be used
	 *  @return true if some server answered on port
	 */
	public boolean testPort (InetAddress host, int port, boolean shouldBeUsed)
	{
		Socket pingSocket = null;
		try
		{
			pingSocket = new Socket(host, port);
		}
		catch (Exception e)
		{
			if (shouldBeUsed)
				System.err.println("  Open Socket " + host + " on " + port + ": " + e.getMessage());
			return false;
		}
		if (!shouldBeUsed)
			System.err.println("  Open Socket " + host + " on " + port);
		if (pingSocket == null)
			return false;
		//	success
		try
		{
			pingSocket.close();
		}
		catch (IOException e)
		{
			System.out.println("  CloseSocket=" + e.toString());
		}
		return true;
	}	//	testPort

	/**
	 * 	Test JDBC Connection to Server
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testJDBC (String uid, String pwd)
	{
		//	jdbc:oracle:thin:@dev:1521:dev1
		m_connectionString = "jdbc:oracle:thin:@"
			+ m_databaseServer.getHostName() + ":" + m_databasePort + ":" + m_databaseName;
		System.out.println("  JDBC = " + m_connectionString);

		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(m_connectionString,
				uid, pwd);
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testJDBC

	/**
	 * 	Test TNS Connection
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testTNS (String uid, String pwd)
	{
		String sqlplus = "sqlplus " + uid + "/" + pwd + "@" + m_TNSName
			+ " @utils/oracle/Test.sql";
		System.out.println("  SQL = " + sqlplus);
		int result = -1;
		try
		{
			Process p = Runtime.getRuntime().exec (sqlplus);
			InputStream in = p.getInputStream();
			int c;
			while ((c = in.read()) != -1)
				System.out.print((char)c);
			in.close();
			in = p.getErrorStream();
			while ((c = in.read()) != -1)
				System.err.print((char)c);
			in.close();
			//	Get result
			try
			{
				Thread.yield();
				result = p.exitValue();
			}
			catch (Exception e)		//	Timing issue on Solaris.
			{
				Thread.sleep(200);	//	.2 sec
				result = p.exitValue();
			}
		}
		catch (Exception ex)
		{
			System.err.println(ex.toString());
		}
		if (result != 0)
			return false;

		
		//	Test OCI Driver
		String connectionString = "jdbc:oracle:oci8:@" + m_TNSName;
		System.out.println("  TNS = " + connectionString);
		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(connectionString,
				uid, pwd);
		}
		catch (UnsatisfiedLinkError ule)
		{
			System.err.println("Check [ORACLE_HOME]/jdbc/Readme.txt for OCI driver setup");
			System.err.println(ule.toString());
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
		}
		return true;
	}	//	testTNS

	/**
	 * 	Test Mail
	 *  @return true of OK
	 */
	private boolean testMail()
	{
		boolean smtpOK = false;
		boolean imapOK = false;
		if (testPort (m_mailServer, 25, true))
		{
			System.out.println("OK: SMTP Server contacted");
			smtpOK = true;
		}
		else
			System.err.println("Error: SMTP Server NOT available");
		//
		if (testPort (m_mailServer, 110, true))
			System.out.println("OK: POP3 Server contacted");
		else
			System.err.println("Error: POP3 Server NOT available");
		if (testPort (m_mailServer, 143, true))
		{
			System.out.println("OK: IMAP4 Server contacted");
			imapOK = true;
		}
		else
			System.err.println("Error: IMAP4 Server NOT available");
		//
		if (!smtpOK)
		{
			signalOK (okMailServer, false, false, "No active Mail Server");
			return false;
		}
		//
		try
		{
			EMail em = new EMail (m_mailServer.getHostName (),
					   m_adminEMail.toString (), m_adminEMail.toString (),
					   "Compiere Server Setup Test", "Test: " + m_properties);
			em.setEMailUser (m_mailUser, m_mailPassword);
			if (EMail.SENT_OK.equals (em.send ()))
			{
				System.out.println ("OK: Send Test Email to " + m_adminEMail);
			}
			else
			{
				System.err.println ("Error: Could NOT send Email to "
					+ m_adminEMail);
			}
		}
		catch (Exception ex)
		{
			System.err.println("testMail - " + ex.getLocalizedMessage());
			return false;
		}

		//
		if (!imapOK)
			return false;

		//	Test Read Mail Access
		Properties props = new Properties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_mailServer.getHostName());
		props.put("mail.user", m_mailUser);
		props.put("mail.smtp.auth", "true");
		System.out.println("  Connecting to " + m_mailServer.getHostName());
		//
		Session session = null;
		Store store = null;
		try
		{
			EMailAuthenticator auth = new EMailAuthenticator (m_mailUser, m_mailPassword);
			session = Session.getDefaultInstance(props, auth);
			session.setDebug (Log.isTraceLevel (10));
			System.out.println("  Session=" + session);
			//	Connect to Store
			store = session.getStore("imap");
			System.out.println("  Store=" + store);
		}
		catch (NoSuchProviderException nsp)
		{
			System.err.println("Error Mail IMAP Provider - " + nsp.getMessage());
			return false;
		}
		catch (Exception e)
		{
			System.err.println("Error Mail IMAP - " + e.getMessage());
			return false;
		}
		try
		{
			store.connect(m_mailServer.getHostName(), m_mailUser, m_mailPassword);
			System.out.println("  Store - connected");
			Folder folder = store.getDefaultFolder();
			Folder inbox = folder.getFolder("INBOX");
			System.out.println("OK: Mail Connect to " + inbox.getFullName() + " #Msg=" + inbox.getMessageCount());
			//
			store.close();
		}
		catch (MessagingException mex)
		{
			System.err.println("Error Mail Connect " + mex.getMessage());
			return false;
		}
		return true;
	}	//	testMail

	/**************************************************************************
	 * 	Save Settings.
	 * 	Called from startTest.finished()
	 */
	private void save()
	{
		if (!m_success)
			return;

		bSave.setEnabled(false);
		bTest.setEnabled(false);
		
		//	Add
		m_properties.setProperty("COMPIERE_MAIN_VERSION", Compiere.MAIN_VERSION);
		m_properties.setProperty("COMPIERE_DATE_VERSION", Compiere.DATE_VERSION);
		m_properties.setProperty("COMPIERE_DB_VERSION", Compiere.DB_VERSION);



		//	Before we save, load Ini
		Ini.setClient(false);
		String fileName = m_compiereHome.getAbsolutePath() + File.separator + Ini.COMPIERE_PROPERTY_FILE;
		Ini.loadProperties(fileName);

		//	Save Environment
		fileName = m_compiereHome.getAbsolutePath() + File.separator + COMPIERE_ENV_FILE;
		try
		{
			FileOutputStream fos = new FileOutputStream(new File(fileName));
			m_properties.store(fos, COMPIERE_ENV_FILE);
			fos.flush();
			fos.close();
		}
		catch (Exception e)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + e.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (Throwable t)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + t.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		System.out.println("Properties saved to " + fileName);

		//	Sync Properties
		Ini.setCompiereHome(m_compiereHome.getAbsolutePath());
		CConnection cc = CConnection.get (Database.DB_ORACLE,
			m_databaseServer.getHostName(), m_databasePort, m_databaseName,
			m_databaseUser, m_databasePassword);
		cc.setAppsHost(m_appsServer.getHostName());
		cc.setRMIoverHTTP(false);
		Ini.setProperty(Ini.P_CONNECTION, cc.toStringLong());
		Ini.saveProperties(false);
		
		//	Final Info
		JOptionPane.showConfirmDialog(this, res.getString("EnvironmentSaved"),
			res.getString("CompiereServerSetup"),
			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

		//	Run Ant
		try
		{
			System.out.println("Starting Ant ... ");
			System.setProperty("ant.home", ".");
			String[] 	args = new String[] {"setup"};
			Launcher.main (args);	//	calls System.exit
		}
		catch (Exception e)
		{
		}
			
		//	To be sure
		((Frame)SwingUtilities.getWindowAncestor(this)).dispose();
		System.exit(0);		//	remains active when License Dialog called
	}	//	save

}	//	ConfigurationPanel
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2002 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.install;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;

import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;

import oracle.jdbc.*;
import org.compiere.*;
import org.compiere.apps.*;
import org.compiere.db.*;
import org.compiere.util.*;


/**
 *	Configuration Panel
 *
 * 	@author 	Jorg Janke
 * 	@author 	Marek Mosiewicz<marek.mosiewicz@jotel.com.pl> support for RMI over HTTP - now reimplemented/removed jj
 * 	@version 	$Id: ConfigurationPanel.java,v 1.8 2003/09/30 14:32:04 jjanke Exp $
 */
public class ConfigurationPanel extends JPanel implements ActionListener
{
	/**
	 * 	Constructor
	 *  @param statusBar for info
	 * 	@throws Exception
	 */
	public ConfigurationPanel (JLabel statusBar) throws Exception
	{
		LogManager.resetConfiguration();
		Logger root = Logger.getRootLogger();
	//	root.addAppender(new ConsoleAppender(new PatternLayout("%d{ABSOLUTE} %-5p [%c{1}] %m%n")));
		root.addAppender(new ConsoleAppender(new LogLayout()));
		root.setLevel(Level.ALL);
		Log.setTraceLevel(10);

		m_statusBar = statusBar;
		jbInit();
		dynInit();
	}	//	ConfigurationPanel

	public static final String	COMPIERE_ENV_FILE		= "CompiereEnv.properties";

	public static final String	COMPIERE_HOME 			= "COMPIERE_HOME";
	public static final String	JAVA_HOME 				= "JAVA_HOME";
	public static final String	COMPIERE_JAVA_OPTIONS 	= "COMPIERE_JAVA_OPTIONS";

	public static final String	COMPIERE_APPS_SERVER 	= "COMPIERE_APPS_SERVER";
	public static final String	COMPIERE_JNP_PORT 		= "COMPIERE_JNP_PORT";
	public static final String	COMPIERE_WEB_PORT 		= "COMPIERE_WEB_PORT";
	public static final String	COMPIERE_SSL_PORT 		= "COMPIERE_SSL_PORT";

	public static final String	COMPIERE_DB_SERVER 		= "COMPIERE_DB_SERVER";
	public static final String	COMPIERE_DB_PORT 		= "COMPIERE_DB_PORT";
	public static final String	COMPIERE_DB_NAME 		= "COMPIERE_DB_NAME";

	public static final String	COMPIERE_DB_USER 		= "COMPIERE_DB_USER";
	public static final String	COMPIERE_DB_PASSWORD 	= "COMPIERE_DB_PASSWORD";
	public static final String	COMPIERE_DB_SYSTEM 		= "COMPIERE_DB_SYSTEM";

	public static final String	COMPIERE_DB_URL 		= "COMPIERE_DB_URL";
	public static final String	COMPIERE_DB_TNS 		= "COMPIERE_DB_TNS";

	public static final String	COMPIERE_MAIL_SERVER 	= "COMPIERE_MAIL_SERVER";
	public static final String	COMPIERE_MAIL_USER 		= "COMPIERE_MAIL_USER";
	public static final String	COMPIERE_MAIL_PASSWORD 	= "COMPIERE_MAIL_PASSWORD";
	public static final String	COMPIERE_ADMIN_EMAIL	= "COMPIERE_ADMIN_EMAIL";

	public static final String	COMPIERE_FTP_SERVER		= "COMPIERE_FTP_SERVER";
	public static final String	COMPIERE_FTP_USER		= "COMPIERE_FTP_USER";
	public static final String	COMPIERE_FTP_PASSWORD	= "COMPIERE_FTP_PASSWORD";
	public static final String	COMPIERE_FTP_PREFIX		= "COMPIERE_FTP_PREFIX";

	private String			m_errorString;
	private File			m_javaHome;
	private File			m_compiereHome;
	private InetAddress		m_appsServer;
	private int				m_WebPort;
	private int				m_JNPPort;
	private InetAddress		m_databaseServer;
	private int				m_databasePort;
	private String			m_databaseName;
	private String			m_connectionString;
	private String			m_systemPassword;
	private String			m_databaseUser;
	private String			m_databasePassword;
	private String			m_TNSName;

	private InetAddress		m_mailServer;
	private InternetAddress	m_adminEMail;
	private String			m_mailUser;
	private String			m_mailPassword;

	private volatile boolean		m_success = false;
	private volatile boolean	 	m_testing = false;

	/** Translation				*/
	static ResourceBundle 		res = ResourceBundle.getBundle("org.compiere.install.SetupRes");

	/**	Driver					*/
	private static Driver		s_driver = null;
	/** Environment Properties	*/
	private Properties			m_properties = new Properties();
	/** Status Bar				*/
	private JLabel 				m_statusBar;

	private static ImageIcon iOpen = new ImageIcon(ConfigurationPanel.class.getResource("openFile.gif"));
	private static ImageIcon iSave = new ImageIcon(Compiere.class.getResource("images/Save16.gif"));
	private static ImageIcon iHelp = new ImageIcon(Compiere.class.getResource("images/Help16.gif"));


	//	-------------	Static UI
	private GridBagLayout gridBagLayout = new GridBagLayout();
	private JLabel lCompiereHome = new JLabel();
	private JTextField fCompiereHome = new JTextField();
	private JLabel lWebPort = new JLabel();
	private JTextField fWebPort = new JTextField();
	private JLabel lAppsServer = new JLabel();
	private JTextField fAppsServer = new JTextField();
	private JLabel lDatabaseType = new JLabel();
	private JComboBox fDatabaseType = new JComboBox();
	private JLabel lDatabaseName = new JLabel();
	private JLabel lDatabasePort = new JLabel();
	private JLabel lDatabaseUser = new JLabel();
	private JLabel lDatabasePassword = new JLabel();
	private JTextField fDatabaseName = new JTextField();
	private JTextField fDatabasePort = new JTextField();
	private JTextField fDatabaseUser = new JTextField();
	private JPasswordField fDatabasePassword = new JPasswordField();
	private JLabel lTNSName = new JLabel();
	private JComboBox fTNSName = new JComboBox();
	private JLabel lSystemPassword = new JLabel();
	private JPasswordField fSystemPassword = new JPasswordField();
	private JLabel lMailServer = new JLabel();
	private JTextField fMailServer = new JTextField();
	private JLabel lAdminEMail = new JLabel();
	private JTextField fAdminEMail = new JTextField();
	private JLabel lDatabaseServer = new JLabel();
	private JTextField fDatabaseServer = new JTextField();
	private JLabel lJavaHome = new JLabel();
	private JTextField fJavaHome = new JTextField();
	private JButton bCompiereHome = new JButton(iOpen);
	private JButton bJavaHome = new JButton(iOpen);
	private JButton bHelp = new JButton(iHelp);
	private JButton bTest = new JButton();
	private JButton bSave = new JButton(iSave);
	private JLabel lJNPPort = new JLabel();
	private JTextField fJNPPort = new JTextField();
	private JLabel lMailUser = new JLabel();
	private JLabel lMailPassword = new JLabel();
	private JTextField fMailUser = new JTextField();
	private JPasswordField fMailPassword = new JPasswordField();


	/**
	 * 	Static Layout Init
	 *  @throws Exception
	 */
	private void jbInit() throws Exception
	{
		lCompiereHome.setToolTipText(res.getString("CompiereHomeInfo"));
		lCompiereHome.setText(res.getString("CompiereHome"));
		this.setLayout(gridBagLayout);
		fCompiereHome.setText("D:\\Compiere2");
		lWebPort.setToolTipText(res.getString("WebPortInfo"));
		lWebPort.setText(res.getString("WebPort"));
		fWebPort.setText("80");
		lAppsServer.setToolTipText(res.getString("AppsServerInfo"));
		lAppsServer.setText(res.getString("AppsServer"));
		fAppsServer.setText("server.company.com");
		lDatabaseType.setToolTipText(res.getString("DatabaseTypeInfo"));
		lDatabaseType.setText(res.getString("DatabaseType"));
		lDatabaseName.setToolTipText(res.getString("DatabaseNameInfo"));
		lDatabaseName.setText(res.getString("DatabaseName"));
		lDatabasePort.setToolTipText(res.getString("DatabasePortInfo"));
		lDatabasePort.setText(res.getString("DatabasePort"));
		lDatabaseUser.setToolTipText(res.getString("DatabaseUserInfo"));
		lDatabaseUser.setText(res.getString("DatabaseUser"));
		lDatabasePassword.setToolTipText(res.getString("DatabasePasswordInfo"));
		lDatabasePassword.setText(res.getString("DatabasePassword"));
		fDatabaseName.setText("compiere");
		fDatabasePort.setText("1521");
		fDatabaseUser.setText("compiere");
		fDatabasePassword.setText("compiere");
		lTNSName.setToolTipText(res.getString("TNSNameInfo"));
		lTNSName.setText(res.getString("TNSName"));
		fTNSName.setEditable(true);
		lSystemPassword.setToolTipText(res.getString("SystemPasswordInfo"));
		lSystemPassword.setText(res.getString("SystemPassword"));
		fSystemPassword.setText("manager");
		lMailServer.setToolTipText(res.getString("MailServerInfo"));
		lMailServer.setText(res.getString("MailServer"));
		fMailServer.setText("mail.company.com");
		lAdminEMail.setToolTipText(res.getString("AdminEMailInfo"));
		lAdminEMail.setText(res.getString("AdminEMail"));
		fAdminEMail.setText("admin@company.com");
		bTest.setToolTipText(res.getString("TestInfo"));
		bTest.setText(res.getString("Test"));
		bSave.setToolTipText(res.getString("SaveInfo"));
		bSave.setText(res.getString("Save"));
		lDatabaseServer.setToolTipText(res.getString("DatabaseServerInfo"));
		lDatabaseServer.setText(res.getString("DatabaseServer"));
		lJavaHome.setToolTipText(res.getString("JavaHomeInfo"));
		lJavaHome.setText(res.getString("JavaHome"));
		fJavaHome.setText("Java141");
		lJNPPort.setToolTipText(res.getString("JNPPortInfo"));
		lJNPPort.setText(res.getString("JNPPort"));
		fJNPPort.setText("1099");
		bHelp.setToolTipText(res.getString("HelpInfo"));
		lMailUser.setToolTipText(res.getString("MailUserInfo"));
		lMailUser.setText(res.getString("MailUser"));
		lMailPassword.setToolTipText(res.getString("MailPasswordInfo"));
		lMailPassword.setText(res.getString("MailPassword"));
		fMailUser.setText("compiere");
		fMailPassword.setText("compiere");

		//
		this.add(lCompiereHome,           new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fCompiereHome,             new GridBagConstraints(1, 1, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lWebPort,           new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fWebPort,            new GridBagConstraints(1, 3, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lAppsServer,          new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fAppsServer,           new GridBagConstraints(1, 2, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseType,           new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseType,          new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabasePort,          new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseUser,           new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseName,             new GridBagConstraints(1, 6, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabasePort,            new GridBagConstraints(1, 7, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseUser,             new GridBagConstraints(1, 8, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lTNSName,           new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fTNSName,              new GridBagConstraints(3, 5, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lMailServer,          new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lSystemPassword,           new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fSystemPassword,          new GridBagConstraints(3, 7, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabasePassword,           new GridBagConstraints(3, 8, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabasePassword,          new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailServer,          new GridBagConstraints(1, 9, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(bTest,         new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(bSave,          new GridBagConstraints(3, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabaseServer,          new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseServer,         new GridBagConstraints(1, 5, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseName,       new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lJavaHome,      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJavaHome,            new GridBagConstraints(1, 0, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bCompiereHome,     new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bHelp,        new GridBagConstraints(2, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bJavaHome,     new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lJNPPort,     new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJNPPort,      new GridBagConstraints(3, 3, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lMailPassword,      new GridBagConstraints(2, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailPassword,       new GridBagConstraints(3, 10, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lAdminEMail,   new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailUser,   new GridBagConstraints(1, 10, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fAdminEMail,    new GridBagConstraints(3, 9, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lMailUser,  new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 0), 0, 0));
		//
		bCompiereHome.addActionListener(this);
		bJavaHome.addActionListener(this);
		bHelp.addActionListener(this);
		bTest.addActionListener(this);
		bSave.addActionListener(this);
	}	//	jbInit


	/**
	 * 	Dynamic Init
	 *  @throws Exception
	 */
	private void dynInit() throws Exception
	{
		//	Layout - Column Length
		fWebPort.setColumns(30);
		fJNPPort.setColumns(30);

		//	DatabaseType
		fDatabaseType.addItem("Oracle 9i2");

		//	Compiere Home
		String ch = System.getProperty(COMPIERE_HOME);
		if (ch == null || ch.length() == 0)
			ch = System.getProperty("user.dir");
		fCompiereHome.setText(ch);
		boolean envLoaded = false;
		String fileName = ch + File.separator + COMPIERE_ENV_FILE;
		File env = new File (fileName);
		System.out.println(env + " - exists=" + env.exists());
		if (env.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(env);
				m_properties.load(fis);
				fis.close();
			}
			catch (Exception e)
			{
				System.err.println(e.toString());
			}
			System.out.println("Loading ...");
			if (m_properties.size() > 5)
				envLoaded = true;
			//
			fCompiereHome.setText((String)m_properties.get(COMPIERE_HOME));
			fJavaHome.setText((String)m_properties.get(JAVA_HOME));
			fAppsServer.setText((String)m_properties.get(COMPIERE_APPS_SERVER));
			fWebPort.setText((String)m_properties.get(COMPIERE_WEB_PORT));
			fJNPPort.setText((String)m_properties.get(COMPIERE_JNP_PORT));

			fDatabaseServer.setText((String)m_properties.get(COMPIERE_DB_SERVER));
			fDatabasePort.setText((String)m_properties.get(COMPIERE_DB_PORT));
			fDatabaseName.setText((String)m_properties.get(COMPIERE_DB_NAME));

			fDatabaseUser.setText((String)m_properties.get(COMPIERE_DB_USER));
			fDatabasePassword.setText((String)m_properties.get(COMPIERE_DB_PASSWORD));
			fSystemPassword.setText((String)m_properties.get(COMPIERE_DB_SYSTEM));
			fillTNSName((String)m_properties.get(COMPIERE_DB_TNS));

			fMailServer.setText((String)m_properties.get(COMPIERE_MAIL_SERVER));
			fMailUser.setText((String)m_properties.get(COMPIERE_MAIL_USER));
			fMailPassword.setText((String)m_properties.get(COMPIERE_MAIL_PASSWORD));
			fAdminEMail.setText((String)m_properties.get(COMPIERE_ADMIN_EMAIL));
		}

		//	No environment file found - defaults
		if (!envLoaded)
		{
			System.out.println("Setting Defaults");
			//	Java Home, e.g. D:\j2sdk1.4.1\jre
			String javaHome = System.getProperty("java.home");
			if (javaHome.endsWith("jre"))
				javaHome = javaHome.substring(0, javaHome.length()-4);
			fJavaHome.setText(javaHome);
			//	AppsServer
			fAppsServer.setText(InetAddress.getLocalHost().getHostName());
			//	Database Server
			fDatabaseServer.setText(InetAddress.getLocalHost().getHostName());
			//	Mail Server
			fMailServer.setText(InetAddress.getLocalHost().getHostName());
			//  Get TNS Names
			fillTNSName("");
		}	//	!envLoaded

		//	Default FTP stuff
		if (!m_properties.containsKey(COMPIERE_FTP_SERVER))
		{
			m_properties.setProperty(COMPIERE_FTP_SERVER, "localhost");
			m_properties.setProperty(COMPIERE_FTP_USER, "anonymous");
			m_properties.setProperty(COMPIERE_FTP_PASSWORD, "user@host.com");
			m_properties.setProperty(COMPIERE_FTP_PREFIX, "my");
		}
		//	Default Java Options
		if (!m_properties.containsKey(COMPIERE_JAVA_OPTIONS))
			m_properties.setProperty(COMPIERE_JAVA_OPTIONS, "-Xms32m -Xmx128m");

		//	(String)m_properties.get(COMPIERE_DB_URL)	//	derived
	}	//	dynInit

	/**
	 * 	Fill TNS Name
	 * 	@param defaultValue defaultValue
	 */
	private void fillTNSName (String defaultValue)
	{
		//	default value to lowercase or null
		String def = defaultValue;
		if (def != null && def.trim().length() == 0)
			def = null;
		if (def != null)
		{
			def = def.toLowerCase();
			fTNSName.addItem(def);
		}

		//	Search for Oracle Info
		String path = System.getProperty("java.library.path");
		String[] entries = path.split(File.pathSeparator);
		for (int e = 0; e < entries.length; e++)
		{
			String entry = entries[e].toLowerCase();
			if (entry.indexOf("ora") != -1 && entry.endsWith("bin"))
			{
				StringBuffer sb = getTNS_File (entries[e].substring(0, entries[e].length()-4));
				String[] tnsnames = getTNS_Names (sb);
				if (tnsnames != null)
				{
					for (int i = 0; i < tnsnames.length; i++)
					{
						String tns = tnsnames[i];
						if (!tns.equals(def))
							fTNSName.addItem(tns);
					}
					break;
				}
			}
		}	//	for all path entries

		//	Set first entry
		fTNSName.addActionListener(this);
		if (fTNSName.getItemCount() > 0)
			fTNSName.setSelectedIndex(0);
	}	//	fillTNSName

	/**
	 * 	Get File tnmsnames.ora in StringBuffer
	 * 	@param oraHome ORACLE_HOME
	 * 	@return tnsnames.ora or null
	 */
	private StringBuffer getTNS_File (String oraHome)
	{
		String tnsnames = oraHome + File.separator
			+ "network" + File.separator
			+ "admin" + File.separator
			+ "tnsnames.ora";
		File tnsfile = new File (tnsnames);
		if (!tnsfile.exists())
			return null;

		System.out.println("Searching " + tnsnames);
		StringBuffer sb = new StringBuffer();
		try
		{
			FileReader fr = new FileReader (tnsfile);
			int c;
			while ((c = fr.read()) != -1)
				sb.append((char)c);
		}
		catch (IOException ex)
		{
			System.err.println("Error Reading " + tnsnames);
			ex.printStackTrace();
			return null;
		}
		if (sb.length() == 0)
			return null;
		return sb;
	}	//	getTNS_File

	/**
	 * 	Get TNS Names entries.
	 * 	Assumes standard tnsmanes.ora formatting of NetMgr
	 * 	@param tnsnames content of tnsnames.ora
	 * 	@return tns names or null
	 */
	private String[] getTNS_Names (StringBuffer tnsnames)
	{
		if (tnsnames == null)
			return null;

		ArrayList list = new ArrayList();
		Pattern pattern = Pattern.compile("$", Pattern.MULTILINE);
		String[] lines = pattern.split(tnsnames);
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i].trim();
			if (line.length() > 0
				&& Character.isLetter(line.charAt(0))	//	no # (
				&& line.indexOf("=") != -1
				&& line.indexOf("EXTPROC_") == -1
				&& line.indexOf("_HTTP") == -1)
			{
				String entry = line.substring(0, line.indexOf('=')).trim().toLowerCase();
				System.out.println("- " + entry);
				list.add(entry);
			}
		}
		//	Convert to Array
		if (list.size() == 0)
			return null;
		String[] retValue = new String[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getTNS_Names

	/*************************************************************************/

	/**
	 * 	ActionListener
	 *  @param e event
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (m_testing)
			return;
		//	TNS Name Changed
		if (e.getSource() == fTNSName)
		{
			String tns = (String)fTNSName.getSelectedItem();
			if (tns != null)
			{
				int pos = tns.indexOf('.');
				if (pos != -1)
					tns = tns.substring(0, pos);
				fDatabaseName.setText(tns);
			}
		}

		else if (e.getSource() == bJavaHome)
			setPath (fJavaHome);
		else if (e.getSource() == bCompiereHome)
			setPath (fCompiereHome);
		else if (e.getSource() == bHelp)
			new Setup_Help((Frame)SwingUtilities.getWindowAncestor(this));
		else if (e.getSource() == bTest)
			startTest();
		else if (e.getSource() == bSave)
			save();
	}	//	actionPerformed

	/**
	 * 	Set Path in Field
	 * 	@param field field to set Path
	 */
	private void setPath (JTextField field)
	{
		JFileChooser fc = new JFileChooser(field.getText());
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			field.setText(fc.getSelectedFile().getAbsolutePath());
	}	//	setPath

	/*************************************************************************/

	/**
	 * 	Start Test Async.
	 *  @return SwingWorker
	 */
	private SwingWorker startTest()
	{
		SwingWorker worker = new SwingWorker()
		{
			//	Start it
			public Object construct()
			{
				m_testing = true;
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				bTest.setEnabled(false);
				m_success = false;
				m_errorString = null;
				try
				{
					test();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					m_errorString = ex.getMessage();
				}
				//
				setCursor(Cursor.getDefaultCursor());
				if (m_errorString == null)
					m_success = true;
				bTest.setEnabled(true);
				m_testing = false;
				return new Boolean(m_success);
			}
			//	Finish it
			public void finished()
			{
				if (m_errorString != null)
				{
					System.err.println(m_errorString);
					JOptionPane.showConfirmDialog (null, m_errorString, res.getString("ServerError"),
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		worker.start();
		return worker;
	}	//	startIt

	/**
	 *	Test it
	 * 	@throws Exception
	 */
	private void test() throws Exception
	{
		System.out.println();
		System.out.println("** Test **");
		bSave.setEnabled(false);


		//	Java Home
		m_errorString = res.getString("ErrorJavaHome");
		m_javaHome = new File (fJavaHome.getText());
		if (!m_javaHome.exists())
			return;
		File tools = new File (fJavaHome.getText() + File.separator + "lib" + File.separator + "tools.jar");
		if (!tools.exists())
		{
			System.err.println("Not Found Java SDK = " + tools);
			return;
		}
		System.out.println("OK: JavaHome=" + m_javaHome.getAbsolutePath());
		m_properties.setProperty(JAVA_HOME, m_javaHome.getAbsolutePath());

		//	Java Version
		final String VERSION = "1.4.1";
		final String VERSION2 = "1.4.2";
		boolean versionOK = false;
		String jh = m_javaHome.getAbsolutePath();
		if (jh.indexOf(VERSION) != -1)	//	file name has version = assuming OK
			versionOK = true;
		if (!versionOK && jh.indexOf(VERSION2) != -1)	//	
			versionOK = true;
		String thisJH = System.getProperty("java.home");
		if (thisJH.indexOf(jh) != -1)	//	wre are running the version currently
		{
			String thisJV = System.getProperty("java.version");
			versionOK = thisJV.indexOf(VERSION) != -1;
			if (versionOK)
			  System.out.println("  Java Version OK = " + thisJV);
		}
		if (!versionOK)
		  System.out.println("** Please check Java Version - should be " + VERSION2 + "*");

		//	Compiere Home
		m_errorString = res.getString("ErrorCompiereHome");
		m_compiereHome = new File (fCompiereHome.getText());
		if (!m_compiereHome.exists())
		{
			System.err.println("Not found CompiereHome = " + m_compiereHome);
			return;
		}
		System.out.println("OK: CompiereHome = " + m_compiereHome);
		m_properties.setProperty(COMPIERE_HOME, m_compiereHome.getAbsolutePath());


		//	AppsServer
		m_errorString = res.getString("ErrorAppsServer");
		String server = fAppsServer.getText();
		if (server == null || server.length() == 0 || server.toLowerCase().indexOf("localhost") != -1)
		{
			System.err.println("Not correct AppsServer = " + m_appsServer);
			return;
		}
		m_appsServer = InetAddress.getByName(server);
		System.out.println("OK: AppsServer = " + m_appsServer);
		m_properties.setProperty(COMPIERE_APPS_SERVER, m_appsServer.getHostName());

		//	Web Port
		m_errorString = res.getString("ErrorWebPort");
		m_statusBar.setText(lWebPort.getText());
		m_WebPort = Integer.parseInt(fWebPort.getText());
		//	HTML Port Use
		if (testPort ("http", m_appsServer.getHostName(), m_WebPort, "/") || !testServerPort(m_WebPort))
		{
			System.err.println("Not correct WebPort = " + m_WebPort);
			return;
		}
		System.out.println("OK: WebPort = " + m_WebPort);
		m_properties.setProperty(COMPIERE_WEB_PORT, String.valueOf(m_WebPort));
		//	Derive SSL Port
		int sslPort = m_WebPort == 80 ? 443 : 8443;
		System.out.println("SSL WebPort = " + sslPort);
		m_properties.setProperty(COMPIERE_SSL_PORT, String.valueOf(sslPort));


		//	JNP Port
		m_errorString = res.getString("ErrorJNPPort");
		m_statusBar.setText(lJNPPort.getText());
		m_JNPPort = Integer.parseInt(fJNPPort.getText());
		//	HTML Port Use
		if (testPort (m_appsServer, m_JNPPort, false) || !testServerPort(m_JNPPort))
		{
			System.err.println("Not correct JNPPort = " + m_JNPPort);
			return;
		}
		System.out.println("OK: JNPPort = " + m_JNPPort);
		m_properties.setProperty(COMPIERE_JNP_PORT, String.valueOf(m_JNPPort));


		//	Database Server
		m_errorString = res.getString("ErrorDatabaseServer");
		m_statusBar.setText(lDatabaseServer.getText());
		server = fDatabaseServer.getText();
		if (server == null || server.length() == 0 || server.toLowerCase().indexOf("localhost") != -1)
		{
			System.err.println("Not correct Database Server = " + server);
			return;
		}
		m_databaseServer = InetAddress.getByName(server);
		System.out.println("OK: Database Server = " + m_databaseServer);
		m_properties.setProperty(COMPIERE_DB_SERVER, m_databaseServer.getHostName());


		//	Database Port
		m_errorString = res.getString("ErrorDatabasePort");
		m_databasePort = Integer.parseInt(fDatabasePort.getText());
		//	Database Port Use
		if (!testPort (m_databaseServer, m_databasePort, true))
		{
			System.err.println("Error Database Port = " + m_databasePort);
			return;
		}
		System.out.println("OK: Database Port = " + m_databasePort);
		m_properties.setProperty(COMPIERE_DB_PORT, String.valueOf(m_databasePort));


		//	JDBC Database Info
		m_errorString = res.getString("ErrorJDBC");
		m_statusBar.setText(lDatabaseName.getText());
		m_databaseName = fDatabaseName.getText();	//	SID
		m_systemPassword = new String(fSystemPassword.getPassword());
		if (!testJDBC("system", m_systemPassword))
		{
			System.err.println("Error Database Name = " + m_databaseName);
			System.err.println("Error Database SystemID = system/" + m_systemPassword);
			return;
		}
		System.out.println("OK: Database Name = " + m_databaseName);
		m_properties.setProperty(COMPIERE_DB_NAME, m_databaseName);
		System.out.println("OK: Database SystemID = system/" + m_systemPassword);
		m_properties.setProperty(COMPIERE_DB_SYSTEM, m_systemPassword);
		//	URL (derived)	jdbc:oracle:thin:@prod1:1521:prod1
		StringBuffer url = new StringBuffer("jdbc:oracle:thin:@")
			.append(m_databaseServer).append(":")
			.append(m_databasePort).append(":").append(m_databaseName);
		System.out.println("OK: Database URL = " + url.toString());
		m_properties.setProperty(COMPIERE_DB_URL, url.toString());


		//	Database User Info
		m_databaseUser = fDatabaseUser.getText();	//	UID
		m_databasePassword = new String(fDatabasePassword.getPassword());	//	PWD
		//	Ignore result as it might not be imported
		if (testJDBC(m_databaseUser, m_databasePassword))
			System.out.println("OK: Database UserID = " + m_databaseUser + "/" + m_databasePassword);
		else
			System.out.println("Not created yet: Database UserID = " + m_databaseUser + "/" + m_databasePassword);
		m_properties.setProperty(COMPIERE_DB_USER, m_databaseUser);
		m_properties.setProperty(COMPIERE_DB_PASSWORD, m_databasePassword);


		//	TNS Name Info
		m_errorString = res.getString("ErrorTNS");
		m_statusBar.setText(lTNSName.getText());
		m_TNSName = (String)fTNSName.getSelectedItem();
		if (!testTNS("system", m_systemPassword))
		{
			System.err.println("Error Database TNS Name = " + m_TNSName);
			return;
		}
		System.out.println("OK: Database TNS Name = " + m_TNSName);
		m_properties.setProperty(COMPIERE_DB_TNS, m_TNSName);


		//	Mail Server
		m_errorString = res.getString("ErrorMailServer");
		m_statusBar.setText(lMailServer.getText());
		server = fMailServer.getText();
		if (server == null || server.length() == 0)
		{
			System.err.println("Error Mail Server = " + server);
			return;
		}
		m_mailServer = InetAddress.getByName(server);
		System.out.println("OK: Mail Server = " + m_mailServer);
		m_properties.setProperty(COMPIERE_MAIL_SERVER, m_mailServer.getHostName());


		//	Mail User
		m_errorString = "ErrorMailUser";
		m_statusBar.setText(lMailUser.getText());
		m_mailUser = fMailUser.getText();
		m_mailPassword = new String(fMailPassword.getPassword());
		m_properties.setProperty(COMPIERE_MAIL_USER, m_mailUser);
		m_properties.setProperty(COMPIERE_MAIL_PASSWORD, m_mailPassword);
		System.out.println("  Mail User = " + m_mailUser + "/" + m_mailPassword);

		//	Mail Address
		m_errorString = res.getString("ErrorMail");
		m_adminEMail = new InternetAddress (fAdminEMail.getText());
		//
		if (testMail())
			System.out.println("OK: Admin EMail = " + m_adminEMail);
		else
			System.out.println("Not verified Admin EMail = " + m_adminEMail);
		m_properties.setProperty(COMPIERE_ADMIN_EMAIL, m_adminEMail.toString());

		//
		m_statusBar.setText(res.getString("Ok"));
		System.out.println("** Test OK **");
		bSave.setEnabled(true);
		m_errorString = null;
	}	//	test


	/**
	 * 	Test Apps Server Port (client perspective)
	 *  @param protocol protocol (http, ..)
	 *  @param server server name
	 *  @param port port
	 *  @param file file name
	 *  @return true if able to connect
	 */
	private boolean testPort (String protocol, String server, int port, String file)
	{
		URL url = null;
		try
		{
			url = new URL (protocol, server, port, file);
		}
		catch (MalformedURLException ex)
		{
			ex.printStackTrace();
			return false;
		}
		System.out.println("  URL=" + url);
		try
		{
			URLConnection c = url.openConnection();
			Object o = c.getContent();
			System.err.println("  URL Connection in use=" + url);	//	error
		}
		catch (Exception ex)
		{
			return false;
		}
		return true;
	}	//	testPort

	/**
	 * 	Test Server Port
	 *  @param port port
	 *  @return true if able to create
	 */
	private boolean testServerPort (int port)
	{
		try
		{
			ServerSocket ss = new ServerSocket (port);
			System.out.println("  ServerPort " + ss.getInetAddress() + ":" + ss.getLocalPort());
			ss.close();
		}
		catch (Exception ex)
		{
			System.err.println(ex);
			return false;
		}
		return true;
	}	//	testPort


	/**
	 * 	Test Port
	 *  @param host host
	 *  @param port port
	 *  @param shouldBenUsed true if it should be used
	 *  @return true if some server answered on port
	 */
	public boolean testPort (InetAddress host, int port, boolean shouldBenUsed)
	{
		Socket pingSocket = null;
		try
		{
			pingSocket = new Socket(host, port);
		}
		catch (Exception e)
		{
			if (shouldBenUsed)
				System.err.println("  Open Socket " + host + " on " + port + ": " + e.getMessage());
			return false;
		}
		if (!shouldBenUsed)
			System.err.println("  Open Socket " + host + " on " + port);
		if (pingSocket == null)
			return false;
		//	success
		try
		{
			pingSocket.close();
		}
		catch (IOException e)
		{
			System.out.println("  CloseSocket=" + e.toString());
		}
		return true;
	}	//	testPort

	/**
	 * 	Test JDBC Connection to Server
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testJDBC (String uid, String pwd)
	{
		//	jdbc:oracle:thin:@dev:1521:dev1
		m_connectionString = "jdbc:oracle:thin:@"
			+ m_databaseServer.getHostName() + ":" + m_databasePort + ":" + m_databaseName;
		System.out.println("  JDBC = " + m_connectionString);

		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(m_connectionString,
				uid, pwd);
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testJDBC

	/**
	 * 	Test TNS Connection
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testTNS (String uid, String pwd)
	{
		String connectionString = "jdbc:oracle:oci8:@" + m_TNSName;
		System.out.println("  TNS = " + connectionString);
		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(connectionString,
				uid, pwd);
		}
		catch (UnsatisfiedLinkError ule)
		{
			System.err.println("Check setup of Oracle Server / Oracle Client / LD_LIBRARY_PATH");
			System.err.println(ule.toString());
			return false;
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testTNS

	/**
	 * 	Test Mail
	 *  @return true of OK
	 */
	private boolean testMail()
	{
		boolean smtpOK = false;
		boolean imapOK = false;
		if (testPort (m_mailServer, 25, true))
		{
			System.out.println("OK: SMTP Server contacted");
			smtpOK = true;
		}
		else
			System.err.println("Error: SMTP Server NOT available");
		//
		if (testPort (m_mailServer, 110, true))
			System.out.println("OK: POP3 Server contacted");
		else
			System.err.println("Error: POP3 Server NOT available");
		if (testPort (m_mailServer, 143, true))
		{
			System.out.println("OK: IMAP4 Server contacted");
			imapOK = true;
		}
		else
			System.err.println("Error: IMAP4 Server NOT available");
		//
		if (!smtpOK)
			return false;
		//
		try
		{
			EMail em = new EMail (m_mailServer.getHostName (),
					   m_adminEMail.toString (), m_adminEMail.toString (),
					   "Compiere Server Setup Test", "Test: " + m_properties);
			em.setEMailUser (m_mailUser, m_mailPassword);
			if (EMail.SENT_OK.equals (em.send ()))
			{
				System.out.println ("OK: Send Test Email to " + m_adminEMail);
			}
			else
			{
				System.err.println ("Error: Could NOT send Email to "
					+ m_adminEMail);
			}
		}
		catch (Exception ex)
		{
			System.err.println("testMail - " + ex.getLocalizedMessage());
			return false;
		}

		//
		if (!imapOK)
			return false;

		//	Test Read Mail Access
		Properties props = new Properties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_mailServer.getHostName());
		props.put("mail.user", m_mailUser);
		props.put("mail.smtp.auth", "true");
		System.out.println("  Connecting to " + m_mailServer.getHostName());
		//
		Session session = null;
		Store store = null;
		try
		{
			EMailAuthenticator auth = new EMailAuthenticator (m_mailUser, m_mailPassword);
			session = Session.getDefaultInstance(props, auth);
			session.setDebug (Log.isTraceLevel (10));
			System.out.println("  Session=" + session);
			//	Connect to Store
			store = session.getStore("imap");
			System.out.println("  Store=" + store);
		}
		catch (NoSuchProviderException nsp)
		{
			System.err.println("Error Mail IMAP Provider - " + nsp.getMessage());
			return false;
		}
		catch (Exception e)
		{
			System.err.println("Error Mail IMAP - " + e.getMessage());
			return false;
		}
		try
		{
			store.connect(m_mailServer.getHostName(), m_mailUser, m_mailPassword);
			System.out.println("  Store - connected");
			Folder folder = store.getDefaultFolder();
			Folder inbox = folder.getFolder("INBOX");
			System.out.println("OK: Mail Connect to " + inbox.getFullName() + " #Msg=" + inbox.getMessageCount());
			//
			store.close();
		}
		catch (MessagingException mex)
		{
			System.err.println("Error Mail Connect " + mex.getMessage());
			return false;
		}
		return true;
	}	//	testMail

	/*************************************************************************/

	/**
	 * 	Save Settings
	 */
	private void save()
	{
		SwingWorker sw = startTest();
		while (sw.isAlive())
		{
			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException ex)
			{
				System.err.println("save-waiting: " + ex);
			}
		}
		sw.get();	//	block
		if (!m_success)
			return;

		//	Add
		m_properties.setProperty("COMPIERE_MAIN_VERSION", Compiere.MAIN_VERSION);
		m_properties.setProperty("COMPIERE_DATE_VERSION", Compiere.DATE_VERSION);
		m_properties.setProperty("COMPIERE_DB_VERSION", Compiere.DB_VERSION);



		//	Before we save, load Ini
		Ini.setClient(false);
		String fileName = m_compiereHome.getAbsolutePath() + File.separator + Ini.COMPIERE_PROPERTY_FILE;
		Ini.loadProperties(fileName);

		//	Save Environment
		fileName = m_compiereHome.getAbsolutePath() + File.separator + COMPIERE_ENV_FILE;
		try
		{
			FileOutputStream fos = new FileOutputStream(new File(fileName));
			m_properties.store(fos, COMPIERE_ENV_FILE);
			fos.flush();
			fos.close();
		}
		catch (Exception e)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + e.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (Throwable t)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + t.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		System.out.println("Properties saved to " + fileName);

		//	Sync Properties
		Ini.setCompiereHome(m_compiereHome.getAbsolutePath());
		CConnection cc = CConnection.get (Database.DB_ORACLE,
			m_databaseServer.getHostName(), m_databasePort, m_databaseName,
			m_databaseUser, m_databasePassword);
		cc.setAppsHost(m_appsServer.getHostName());
		cc.setRMIoverHTTP(false);
		Ini.setProperty(Ini.P_CONNECTION, cc.toStringLong());
		Ini.saveProperties(false);


		JOptionPane.showConfirmDialog(this, res.getString("EnvironmentSaved"),
			res.getString("CompiereServerSetup"),
			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

		((Frame)SwingUtilities.getWindowAncestor(this)).dispose();
		System.exit(0);		//	remains active when License Dialog called
	}	//	save

}	//	ConfigurationPanel
/******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2002 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): ______________________________________.
 *****************************************************************************/
package org.compiere.install;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;

import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;

import oracle.jdbc.*;
import org.compiere.*;
import org.compiere.apps.*;
import org.compiere.db.*;
import org.compiere.util.*;


/**
 *	Configuration Panel
 *
 * 	@author 	Jorg Janke
 * 	@author 	Marek Mosiewicz<marek.mosiewicz@jotel.com.pl> support for RMI over HTTP - now reimplemented/removed jj
 * 	@version 	$Id: ConfigurationPanel.java,v 1.6 2003/07/21 04:44:17 jjanke Exp $
 */
public class ConfigurationPanel extends JPanel implements ActionListener
{
	/**
	 * 	Constructor
	 *  @param statusBar for info
	 * 	@throws Exception
	 */
	public ConfigurationPanel (JLabel statusBar) throws Exception
	{
		LogManager.resetConfiguration();
		Logger root = Logger.getRootLogger();
	//	root.addAppender(new ConsoleAppender(new PatternLayout("%d{ABSOLUTE} %-5p [%c{1}] %m%n")));
		root.addAppender(new ConsoleAppender(new LogLayout()));
		root.setLevel(Level.ALL);
		Log.setTraceLevel(10);

		m_statusBar = statusBar;
		jbInit();
		dynInit();
	}	//	ConfigurationPanel

	public static final String	COMPIERE_ENV_FILE		= "CompiereEnv.properties";

	public static final String	COMPIERE_HOME 			= "COMPIERE_HOME";
	public static final String	JAVA_HOME 				= "JAVA_HOME";
	public static final String	COMPIERE_JAVA_OPTIONS 	= "COMPIERE_JAVA_OPTIONS";

	public static final String	COMPIERE_APPS_SERVER 	= "COMPIERE_APPS_SERVER";
	public static final String	COMPIERE_JNP_PORT 		= "COMPIERE_JNP_PORT";
	public static final String	COMPIERE_WEB_PORT 		= "COMPIERE_WEB_PORT";
	public static final String	COMPIERE_SSL_PORT 		= "COMPIERE_SSL_PORT";

	public static final String	COMPIERE_DB_SERVER 		= "COMPIERE_DB_SERVER";
	public static final String	COMPIERE_DB_PORT 		= "COMPIERE_DB_PORT";
	public static final String	COMPIERE_DB_NAME 		= "COMPIERE_DB_NAME";

	public static final String	COMPIERE_DB_USER 		= "COMPIERE_DB_USER";
	public static final String	COMPIERE_DB_PASSWORD 	= "COMPIERE_DB_PASSWORD";
	public static final String	COMPIERE_DB_SYSTEM 		= "COMPIERE_DB_SYSTEM";

	public static final String	COMPIERE_DB_URL 		= "COMPIERE_DB_URL";
	public static final String	COMPIERE_DB_TNS 		= "COMPIERE_DB_TNS";

	public static final String	COMPIERE_MAIL_SERVER 	= "COMPIERE_MAIL_SERVER";
	public static final String	COMPIERE_MAIL_USER 		= "COMPIERE_MAIL_USER";
	public static final String	COMPIERE_MAIL_PASSWORD 	= "COMPIERE_MAIL_PASSWORD";
	public static final String	COMPIERE_ADMIN_EMAIL	= "COMPIERE_ADMIN_EMAIL";

	public static final String	COMPIERE_FTP_SERVER		= "COMPIERE_FTP_SERVER";
	public static final String	COMPIERE_FTP_USER		= "COMPIERE_FTP_USER";
	public static final String	COMPIERE_FTP_PASSWORD	= "COMPIERE_FTP_PASSWORD";
	public static final String	COMPIERE_FTP_PREFIX		= "COMPIERE_FTP_PREFIX";

	private String			m_errorString;
	private File			m_javaHome;
	private File			m_compiereHome;
	private InetAddress		m_appsServer;
	private int				m_WebPort;
	private int				m_JNPPort;
	private InetAddress		m_databaseServer;
	private int				m_databasePort;
	private String			m_databaseName;
	private String			m_connectionString;
	private String			m_systemPassword;
	private String			m_databaseUser;
	private String			m_databasePassword;
	private String			m_TNSName;

	private InetAddress		m_mailServer;
	private InternetAddress	m_adminEMail;
	private String			m_mailUser;
	private String			m_mailPassword;

	private volatile boolean		m_success = false;
	private volatile boolean	 	m_testing = false;

	/** Translation				*/
	static ResourceBundle 		res = ResourceBundle.getBundle("org.compiere.install.SetupRes");

	/**	Driver					*/
	private static Driver		s_driver = null;
	/** Environment Properties	*/
	private Properties			m_properties = new Properties();
	/** Status Bar				*/
	private JLabel 				m_statusBar;

	private static ImageIcon iOpen = new ImageIcon(ConfigurationPanel.class.getResource("openFile.gif"));
	private static ImageIcon iSave = new ImageIcon(Compiere.class.getResource("images/Save16.gif"));
	private static ImageIcon iHelp = new ImageIcon(Compiere.class.getResource("images/Help16.gif"));


	//	-------------	Static UI
	private GridBagLayout gridBagLayout = new GridBagLayout();
	private JLabel lCompiereHome = new JLabel();
	private JTextField fCompiereHome = new JTextField();
	private JLabel lWebPort = new JLabel();
	private JTextField fWebPort = new JTextField();
	private JLabel lAppsServer = new JLabel();
	private JTextField fAppsServer = new JTextField();
	private JLabel lDatabaseType = new JLabel();
	private JComboBox fDatabaseType = new JComboBox();
	private JLabel lDatabaseName = new JLabel();
	private JLabel lDatabasePort = new JLabel();
	private JLabel lDatabaseUser = new JLabel();
	private JLabel lDatabasePassword = new JLabel();
	private JTextField fDatabaseName = new JTextField();
	private JTextField fDatabasePort = new JTextField();
	private JTextField fDatabaseUser = new JTextField();
	private JPasswordField fDatabasePassword = new JPasswordField();
	private JLabel lTNSName = new JLabel();
	private JComboBox fTNSName = new JComboBox();
	private JLabel lSystemPassword = new JLabel();
	private JPasswordField fSystemPassword = new JPasswordField();
	private JLabel lMailServer = new JLabel();
	private JTextField fMailServer = new JTextField();
	private JLabel lAdminEMail = new JLabel();
	private JTextField fAdminEMail = new JTextField();
	private JLabel lDatabaseServer = new JLabel();
	private JTextField fDatabaseServer = new JTextField();
	private JLabel lJavaHome = new JLabel();
	private JTextField fJavaHome = new JTextField();
	private JButton bCompiereHome = new JButton(iOpen);
	private JButton bJavaHome = new JButton(iOpen);
	private JButton bHelp = new JButton(iHelp);
	private JButton bTest = new JButton();
	private JButton bSave = new JButton(iSave);
	private JLabel lJNPPort = new JLabel();
	private JTextField fJNPPort = new JTextField();
	private JLabel lMailUser = new JLabel();
	private JLabel lMailPassword = new JLabel();
	private JTextField fMailUser = new JTextField();
	private JPasswordField fMailPassword = new JPasswordField();


	/**
	 * 	Static Layout Init
	 *  @throws Exception
	 */
	private void jbInit() throws Exception
	{
		lCompiereHome.setToolTipText(res.getString("CompiereHomeInfo"));
		lCompiereHome.setText(res.getString("CompiereHome"));
		this.setLayout(gridBagLayout);
		fCompiereHome.setText("D:\\Compiere2");
		lWebPort.setToolTipText(res.getString("WebPortInfo"));
		lWebPort.setText(res.getString("WebPort"));
		fWebPort.setText("80");
		lAppsServer.setToolTipText(res.getString("AppsServerInfo"));
		lAppsServer.setText(res.getString("AppsServer"));
		fAppsServer.setText("server.company.com");
		lDatabaseType.setToolTipText(res.getString("DatabaseTypeInfo"));
		lDatabaseType.setText(res.getString("DatabaseType"));
		lDatabaseName.setToolTipText(res.getString("DatabaseNameInfo"));
		lDatabaseName.setText(res.getString("DatabaseName"));
		lDatabasePort.setToolTipText(res.getString("DatabasePortInfo"));
		lDatabasePort.setText(res.getString("DatabasePort"));
		lDatabaseUser.setToolTipText(res.getString("DatabaseUserInfo"));
		lDatabaseUser.setText(res.getString("DatabaseUser"));
		lDatabasePassword.setToolTipText(res.getString("DatabasePasswordInfo"));
		lDatabasePassword.setText(res.getString("DatabasePassword"));
		fDatabaseName.setText("compiere");
		fDatabasePort.setText("1521");
		fDatabaseUser.setText("compiere");
		fDatabasePassword.setText("compiere");
		lTNSName.setToolTipText(res.getString("TNSNameInfo"));
		lTNSName.setText(res.getString("TNSName"));
		fTNSName.setEditable(true);
		lSystemPassword.setToolTipText(res.getString("SystemPasswordInfo"));
		lSystemPassword.setText(res.getString("SystemPassword"));
		fSystemPassword.setText("manager");
		lMailServer.setToolTipText(res.getString("MailServerInfo"));
		lMailServer.setText(res.getString("MailServer"));
		fMailServer.setText("mail.company.com");
		lAdminEMail.setToolTipText(res.getString("AdminEMailInfo"));
		lAdminEMail.setText(res.getString("AdminEMail"));
		fAdminEMail.setText("admin@company.com");
		bTest.setToolTipText(res.getString("TestInfo"));
		bTest.setText(res.getString("Test"));
		bSave.setToolTipText(res.getString("SaveInfo"));
		bSave.setText(res.getString("Save"));
		lDatabaseServer.setToolTipText(res.getString("DatabaseServerInfo"));
		lDatabaseServer.setText(res.getString("DatabaseServer"));
		lJavaHome.setToolTipText(res.getString("JavaHomeInfo"));
		lJavaHome.setText(res.getString("JavaHome"));
		fJavaHome.setText("Java141");
		lJNPPort.setToolTipText(res.getString("JNPPortInfo"));
		lJNPPort.setText(res.getString("JNPPort"));
		fJNPPort.setText("1099");
		bHelp.setToolTipText(res.getString("HelpInfo"));
		lMailUser.setToolTipText(res.getString("MailUserInfo"));
		lMailUser.setText(res.getString("MailUser"));
		lMailPassword.setToolTipText(res.getString("MailPasswordInfo"));
		lMailPassword.setText(res.getString("MailPassword"));
		fMailUser.setText("compiere");
		fMailPassword.setText("compiere");

		//
		this.add(lCompiereHome,           new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fCompiereHome,             new GridBagConstraints(1, 1, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lWebPort,           new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fWebPort,            new GridBagConstraints(1, 3, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lAppsServer,          new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fAppsServer,           new GridBagConstraints(1, 2, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseType,           new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseType,          new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabasePort,          new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseUser,           new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fDatabaseName,             new GridBagConstraints(1, 6, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabasePort,            new GridBagConstraints(1, 7, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseUser,             new GridBagConstraints(1, 8, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lTNSName,           new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fTNSName,              new GridBagConstraints(3, 5, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lMailServer,          new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lSystemPassword,           new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fSystemPassword,          new GridBagConstraints(3, 7, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabasePassword,           new GridBagConstraints(3, 8, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabasePassword,          new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailServer,          new GridBagConstraints(1, 9, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(bTest,         new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(bSave,          new GridBagConstraints(3, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lDatabaseServer,          new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fDatabaseServer,         new GridBagConstraints(1, 5, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lDatabaseName,       new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lJavaHome,      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJavaHome,            new GridBagConstraints(1, 0, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bCompiereHome,     new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bHelp,        new GridBagConstraints(2, 11, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(bJavaHome,     new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lJNPPort,     new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(fJNPPort,      new GridBagConstraints(3, 3, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.add(lMailPassword,      new GridBagConstraints(2, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailPassword,       new GridBagConstraints(3, 10, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lAdminEMail,   new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fMailUser,   new GridBagConstraints(1, 10, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(fAdminEMail,    new GridBagConstraints(3, 9, 1, 1, 0.5, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 5), 0, 0));
		this.add(lMailUser,  new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0
			,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 5, 5, 0), 0, 0));
		//
		bCompiereHome.addActionListener(this);
		bJavaHome.addActionListener(this);
		bHelp.addActionListener(this);
		bTest.addActionListener(this);
		bSave.addActionListener(this);
	}	//	jbInit


	/**
	 * 	Dynamic Init
	 *  @throws Exception
	 */
	private void dynInit() throws Exception
	{
		//	Layout - Column Length
		fWebPort.setColumns(30);
		fJNPPort.setColumns(30);

		//	DatabaseType
		fDatabaseType.addItem("Oracle 9i2");

		//	Compiere Home
		String ch = System.getProperty(COMPIERE_HOME);
		if (ch == null || ch.length() == 0)
			ch = System.getProperty("user.dir");
		fCompiereHome.setText(ch);
		boolean envLoaded = false;
		String fileName = ch + File.separator + COMPIERE_ENV_FILE;
		File env = new File (fileName);
		System.out.println(env + " - exists=" + env.exists());
		if (env.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(env);
				m_properties.load(fis);
				fis.close();
			}
			catch (Exception e)
			{
				System.err.println(e.toString());
			}
			System.out.println("Loading ...");
			if (m_properties.size() > 5)
				envLoaded = true;
			//
			fCompiereHome.setText((String)m_properties.get(COMPIERE_HOME));
			fJavaHome.setText((String)m_properties.get(JAVA_HOME));
			fAppsServer.setText((String)m_properties.get(COMPIERE_APPS_SERVER));
			fWebPort.setText((String)m_properties.get(COMPIERE_WEB_PORT));
			fJNPPort.setText((String)m_properties.get(COMPIERE_JNP_PORT));

			fDatabaseServer.setText((String)m_properties.get(COMPIERE_DB_SERVER));
			fDatabasePort.setText((String)m_properties.get(COMPIERE_DB_PORT));
			fDatabaseName.setText((String)m_properties.get(COMPIERE_DB_NAME));

			fDatabaseUser.setText((String)m_properties.get(COMPIERE_DB_USER));
			fDatabasePassword.setText((String)m_properties.get(COMPIERE_DB_PASSWORD));
			fSystemPassword.setText((String)m_properties.get(COMPIERE_DB_SYSTEM));
			fillTNSName((String)m_properties.get(COMPIERE_DB_TNS));

			fMailServer.setText((String)m_properties.get(COMPIERE_MAIL_SERVER));
			fMailUser.setText((String)m_properties.get(COMPIERE_MAIL_USER));
			fMailPassword.setText((String)m_properties.get(COMPIERE_MAIL_PASSWORD));
			fAdminEMail.setText((String)m_properties.get(COMPIERE_ADMIN_EMAIL));
		}

		//	No environment file found - defaults
		if (!envLoaded)
		{
			System.out.println("Setting Defaults");
			//	Java Home, e.g. D:\j2sdk1.4.1\jre
			String javaHome = System.getProperty("java.home");
			if (javaHome.endsWith("jre"))
				javaHome = javaHome.substring(0, javaHome.length()-4);
			fJavaHome.setText(javaHome);
			//	AppsServer
			fAppsServer.setText(InetAddress.getLocalHost().getHostName());
			//	Database Server
			fDatabaseServer.setText(InetAddress.getLocalHost().getHostName());
			//	Mail Server
			fMailServer.setText(InetAddress.getLocalHost().getHostName());
			//  Get TNS Names
			fillTNSName("");
		}	//	!envLoaded

		//	Default FTP stuff
		if (!m_properties.containsKey(COMPIERE_FTP_SERVER))
		{
			m_properties.setProperty(COMPIERE_FTP_SERVER, "localhost");
			m_properties.setProperty(COMPIERE_FTP_USER, "anonymous");
			m_properties.setProperty(COMPIERE_FTP_PASSWORD, "user@host.com");
			m_properties.setProperty(COMPIERE_FTP_PREFIX, "my");
		}
		//	Default Java Options
		if (!m_properties.containsKey(COMPIERE_JAVA_OPTIONS))
			m_properties.setProperty(COMPIERE_JAVA_OPTIONS, "-Xms32m -Xmx128m");

		//	(String)m_properties.get(COMPIERE_DB_URL)	//	derived
	}	//	dynInit

	/**
	 * 	Fill TNS Name
	 * 	@param defaultValue defaultValue
	 */
	private void fillTNSName (String defaultValue)
	{
		//	default value to lowercase or null
		String def = defaultValue;
		if (def != null && def.trim().length() == 0)
			def = null;
		if (def != null)
		{
			def = def.toLowerCase();
			fTNSName.addItem(def);
		}

		//	Search for Oracle Info
		String path = System.getProperty("java.library.path");
		String[] entries = path.split(File.pathSeparator);
		for (int e = 0; e < entries.length; e++)
		{
			String entry = entries[e].toLowerCase();
			if (entry.indexOf("ora") != -1 && entry.endsWith("bin"))
			{
				StringBuffer sb = getTNS_File (entries[e].substring(0, entries[e].length()-4));
				String[] tnsnames = getTNS_Names (sb);
				if (tnsnames != null)
				{
					for (int i = 0; i < tnsnames.length; i++)
					{
						String tns = tnsnames[i];
						if (!tns.equals(def))
							fTNSName.addItem(tns);
					}
					break;
				}
			}
		}	//	for all path entries

		//	Set first entry
		fTNSName.addActionListener(this);
		if (fTNSName.getItemCount() > 0)
			fTNSName.setSelectedIndex(0);
	}	//	fillTNSName

	/**
	 * 	Get File tnmsnames.ora in StringBuffer
	 * 	@param oraHome ORACLE_HOME
	 * 	@return tnsnames.ora or null
	 */
	private StringBuffer getTNS_File (String oraHome)
	{
		String tnsnames = oraHome + File.separator
			+ "network" + File.separator
			+ "admin" + File.separator
			+ "tnsnames.ora";
		File tnsfile = new File (tnsnames);
		if (!tnsfile.exists())
			return null;

		System.out.println("Searching " + tnsnames);
		StringBuffer sb = new StringBuffer();
		try
		{
			FileReader fr = new FileReader (tnsfile);
			int c;
			while ((c = fr.read()) != -1)
				sb.append((char)c);
		}
		catch (IOException ex)
		{
			System.err.println("Error Reading " + tnsnames);
			ex.printStackTrace();
			return null;
		}
		if (sb.length() == 0)
			return null;
		return sb;
	}	//	getTNS_File

	/**
	 * 	Get TNS Names entries.
	 * 	Assumes standard tnsmanes.ora formatting of NetMgr
	 * 	@param tnsnames content of tnsnames.ora
	 * 	@return tns names or null
	 */
	private String[] getTNS_Names (StringBuffer tnsnames)
	{
		if (tnsnames == null)
			return null;

		ArrayList list = new ArrayList();
		Pattern pattern = Pattern.compile("$", Pattern.MULTILINE);
		String[] lines = pattern.split(tnsnames);
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i].trim();
			if (line.length() > 0
				&& Character.isLetter(line.charAt(0))	//	no # (
				&& line.indexOf("=") != -1
				&& line.indexOf("EXTPROC_") == -1
				&& line.indexOf("_HTTP") == -1)
			{
				String entry = line.substring(0, line.indexOf('=')).trim().toLowerCase();
				System.out.println("- " + entry);
				list.add(entry);
			}
		}
		//	Convert to Array
		if (list.size() == 0)
			return null;
		String[] retValue = new String[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getTNS_Names

	/*************************************************************************/

	/**
	 * 	ActionListener
	 *  @param e event
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (m_testing)
			return;
		//	TNS Name Changed
		if (e.getSource() == fTNSName)
		{
			String tns = (String)fTNSName.getSelectedItem();
			if (tns != null)
			{
				int pos = tns.indexOf('.');
				if (pos != -1)
					tns = tns.substring(0, pos);
				fDatabaseName.setText(tns);
			}
		}

		else if (e.getSource() == bJavaHome)
			setPath (fJavaHome);
		else if (e.getSource() == bCompiereHome)
			setPath (fCompiereHome);
		else if (e.getSource() == bHelp)
			new Setup_Help((Frame)SwingUtilities.getWindowAncestor(this));
		else if (e.getSource() == bTest)
			startTest();
		else if (e.getSource() == bSave)
			save();
	}	//	actionPerformed

	/**
	 * 	Set Path in Field
	 * 	@param field field to set Path
	 */
	private void setPath (JTextField field)
	{
		JFileChooser fc = new JFileChooser(field.getText());
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			field.setText(fc.getSelectedFile().getAbsolutePath());
	}	//	setPath

	/*************************************************************************/

	/**
	 * 	Start Test Async.
	 *  @return SwingWorker
	 */
	private SwingWorker startTest()
	{
		SwingWorker worker = new SwingWorker()
		{
			//	Start it
			public Object construct()
			{
				m_testing = true;
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				bTest.setEnabled(false);
				m_success = false;
				m_errorString = null;
				try
				{
					test();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					m_errorString = ex.getMessage();
				}
				//
				setCursor(Cursor.getDefaultCursor());
				if (m_errorString == null)
					m_success = true;
				bTest.setEnabled(true);
				m_testing = false;
				return new Boolean(m_success);
			}
			//	Finish it
			public void finished()
			{
				if (m_errorString != null)
				{
					System.err.println(m_errorString);
					JOptionPane.showConfirmDialog (null, m_errorString, res.getString("ServerError"),
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		worker.start();
		return worker;
	}	//	startIt

	/**
	 *	Test it
	 * 	@throws Exception
	 */
	private void test() throws Exception
	{
		System.out.println();
		System.out.println("** Test **");
		bSave.setEnabled(false);


		//	Java Home
		m_errorString = res.getString("ErrorJavaHome");
		m_javaHome = new File (fJavaHome.getText());
		if (!m_javaHome.exists())
			return;
		File tools = new File (fJavaHome.getText() + File.separator + "lib" + File.separator + "tools.jar");
		if (!tools.exists())
		{
			System.err.println("Not Found Java SDK = " + tools);
			return;
		}
		System.out.println("OK: JavaHome=" + m_javaHome.getAbsolutePath());
		m_properties.setProperty(JAVA_HOME, m_javaHome.getAbsolutePath());

		//	Java Version
		final String VERSION = "1.4.1";
		boolean versionOK = false;
		String jh = m_javaHome.getAbsolutePath();
		if (jh.indexOf(VERSION) != -1)	//	file name has version = assuming OK
			versionOK = true;
		String thisJH = System.getProperty("java.home");
		if (thisJH.indexOf(jh) != -1)	//	wre are running the version currently
		{
			String thisJV = System.getProperty("java.version");
			versionOK = thisJV.indexOf(VERSION) != -1;
			if (versionOK)
			  System.out.println("  Java Version OK = " + thisJV);
		}
		if (!versionOK)
		  System.out.println("** Please check Java Version - should be " + VERSION + "*");

		//	Compiere Home
		m_errorString = res.getString("ErrorCompiereHome");
		m_compiereHome = new File (fCompiereHome.getText());
		if (!m_compiereHome.exists())
		{
			System.err.println("Not found CompiereHome = " + m_compiereHome);
			return;
		}
		System.out.println("OK: CompiereHome = " + m_compiereHome);
		m_properties.setProperty(COMPIERE_HOME, m_compiereHome.getAbsolutePath());


		//	AppsServer
		m_errorString = res.getString("ErrorAppsServer");
		String server = fAppsServer.getText();
		if (server == null || server.length() == 0 || server.toLowerCase().indexOf("localhost") != -1)
		{
			System.err.println("Not correct AppsServer = " + m_appsServer);
			return;
		}
		m_appsServer = InetAddress.getByName(server);
		System.out.println("OK: AppsServer = " + m_appsServer);
		m_properties.setProperty(COMPIERE_APPS_SERVER, m_appsServer.getHostName());

		//	Web Port
		m_errorString = res.getString("ErrorWebPort");
		m_statusBar.setText(lWebPort.getText());
		m_WebPort = Integer.parseInt(fWebPort.getText());
		//	HTML Port Use
		if (testPort ("http", m_appsServer.getHostName(), m_WebPort, "/") || !testServerPort(m_WebPort))
		{
			System.err.println("Not correct WebPort = " + m_WebPort);
			return;
		}
		System.out.println("OK: WebPort = " + m_WebPort);
		m_properties.setProperty(COMPIERE_WEB_PORT, String.valueOf(m_WebPort));
		//	Derive SSL Port
		int sslPort = m_WebPort == 80 ? 443 : 8443;
		System.out.println("SSL WebPort = " + sslPort);
		m_properties.setProperty(COMPIERE_SSL_PORT, String.valueOf(sslPort));


		//	JNP Port
		m_errorString = res.getString("ErrorJNPPort");
		m_statusBar.setText(lJNPPort.getText());
		m_JNPPort = Integer.parseInt(fJNPPort.getText());
		//	HTML Port Use
		if (testPort (m_appsServer, m_JNPPort, false) || !testServerPort(m_JNPPort))
		{
			System.err.println("Not correct JNPPort = " + m_JNPPort);
			return;
		}
		System.out.println("OK: JNPPort = " + m_JNPPort);
		m_properties.setProperty(COMPIERE_JNP_PORT, String.valueOf(m_JNPPort));


		//	Database Server
		m_errorString = res.getString("ErrorDatabaseServer");
		m_statusBar.setText(lDatabaseServer.getText());
		server = fDatabaseServer.getText();
		if (server == null || server.length() == 0 || server.toLowerCase().indexOf("localhost") != -1)
		{
			System.err.println("Not correct Database Server = " + server);
			return;
		}
		m_databaseServer = InetAddress.getByName(server);
		System.out.println("OK: Database Server = " + m_databaseServer);
		m_properties.setProperty(COMPIERE_DB_SERVER, m_databaseServer.getHostName());


		//	Database Port
		m_errorString = res.getString("ErrorDatabasePort");
		m_databasePort = Integer.parseInt(fDatabasePort.getText());
		//	Database Port Use
		if (!testPort (m_databaseServer, m_databasePort, true))
		{
			System.err.println("Error Database Port = " + m_databasePort);
			return;
		}
		System.out.println("OK: Database Port = " + m_databasePort);
		m_properties.setProperty(COMPIERE_DB_PORT, String.valueOf(m_databasePort));


		//	JDBC Database Info
		m_errorString = res.getString("ErrorJDBC");
		m_statusBar.setText(lDatabaseName.getText());
		m_databaseName = fDatabaseName.getText();	//	SID
		m_systemPassword = new String(fSystemPassword.getPassword());
		if (!testJDBC("system", m_systemPassword))
		{
			System.err.println("Error Database Name = " + m_databaseName);
			System.err.println("Error Database SystemID = system/" + m_systemPassword);
			return;
		}
		System.out.println("OK: Database Name = " + m_databaseName);
		m_properties.setProperty(COMPIERE_DB_NAME, m_databaseName);
		System.out.println("OK: Database SystemID = system/" + m_systemPassword);
		m_properties.setProperty(COMPIERE_DB_SYSTEM, m_systemPassword);
		//	URL (derived)	jdbc:oracle:thin:@prod1:1521:prod1
		StringBuffer url = new StringBuffer("jdbc:oracle:thin:@")
			.append(m_databaseServer).append(":")
			.append(m_databasePort).append(":").append(m_databaseName);
		System.out.println("OK: Database URL = " + url.toString());
		m_properties.setProperty(COMPIERE_DB_URL, url.toString());


		//	Database User Info
		m_databaseUser = fDatabaseUser.getText();	//	UID
		m_databasePassword = new String(fDatabasePassword.getPassword());	//	PWD
		//	Ignore result as it might not be imported
		if (testJDBC(m_databaseUser, m_databasePassword))
			System.out.println("OK: Database UserID = " + m_databaseUser + "/" + m_databasePassword);
		else
			System.out.println("Not created yet: Database UserID = " + m_databaseUser + "/" + m_databasePassword);
		m_properties.setProperty(COMPIERE_DB_USER, m_databaseUser);
		m_properties.setProperty(COMPIERE_DB_PASSWORD, m_databasePassword);


		//	TNS Name Info
		m_errorString = res.getString("ErrorTNS");
		m_statusBar.setText(lTNSName.getText());
		m_TNSName = (String)fTNSName.getSelectedItem();
		if (!testTNS("system", m_systemPassword))
		{
			System.err.println("Error Database TNS Name = " + m_TNSName);
			return;
		}
		System.out.println("OK: Database TNS Name = " + m_TNSName);
		m_properties.setProperty(COMPIERE_DB_TNS, m_TNSName);


		//	Mail Server
		m_errorString = res.getString("ErrorMailServer");
		m_statusBar.setText(lMailServer.getText());
		server = fMailServer.getText();
		if (server == null || server.length() == 0)
		{
			System.err.println("Error Mail Server = " + server);
			return;
		}
		m_mailServer = InetAddress.getByName(server);
		System.out.println("OK: Mail Server = " + m_mailServer);
		m_properties.setProperty(COMPIERE_MAIL_SERVER, m_mailServer.getHostName());


		//	Mail User
		m_errorString = "ErrorMailUser";
		m_statusBar.setText(lMailUser.getText());
		m_mailUser = fMailUser.getText();
		m_mailPassword = new String(fMailPassword.getPassword());
		m_properties.setProperty(COMPIERE_MAIL_USER, m_mailUser);
		m_properties.setProperty(COMPIERE_MAIL_PASSWORD, m_mailPassword);
		System.out.println("  Mail User = " + m_mailUser + "/" + m_mailPassword);

		//	Mail Address
		m_errorString = res.getString("ErrorMail");
		m_adminEMail = new InternetAddress (fAdminEMail.getText());
		//
		if (testMail())
			System.out.println("OK: Admin EMail = " + m_adminEMail);
		else
			System.out.println("Not verified Admin EMail = " + m_adminEMail);
		m_properties.setProperty(COMPIERE_ADMIN_EMAIL, m_adminEMail.toString());

		//
		m_statusBar.setText(res.getString("Ok"));
		System.out.println("** Test OK **");
		bSave.setEnabled(true);
		m_errorString = null;
	}	//	test


	/**
	 * 	Test Apps Server Port (client perspective)
	 *  @param protocol protocol (http, ..)
	 *  @param server server name
	 *  @param port port
	 *  @param file file name
	 *  @return true if able to connect
	 */
	private boolean testPort (String protocol, String server, int port, String file)
	{
		URL url = null;
		try
		{
			url = new URL (protocol, server, port, file);
		}
		catch (MalformedURLException ex)
		{
			ex.printStackTrace();
			return false;
		}
		System.out.println("  URL=" + url);
		try
		{
			URLConnection c = url.openConnection();
			Object o = c.getContent();
			System.err.println("  URL Connection in use=" + url);	//	error
		}
		catch (Exception ex)
		{
			return false;
		}
		return true;
	}	//	testPort

	/**
	 * 	Test Server Port
	 *  @param port port
	 *  @return true if able to create
	 */
	private boolean testServerPort (int port)
	{
		try
		{
			ServerSocket ss = new ServerSocket (port);
			System.out.println("  ServerPort " + ss.getInetAddress() + ":" + ss.getLocalPort());
			ss.close();
		}
		catch (Exception ex)
		{
			System.err.println(ex);
			return false;
		}
		return true;
	}	//	testPort


	/**
	 * 	Test Port
	 *  @param host host
	 *  @param port port
	 *  @param shouldBenUsed true if it should be used
	 *  @return true if some server answered on port
	 */
	public boolean testPort (InetAddress host, int port, boolean shouldBenUsed)
	{
		Socket pingSocket = null;
		try
		{
			pingSocket = new Socket(host, port);
		}
		catch (Exception e)
		{
			if (shouldBenUsed)
				System.err.println("  Open Socket " + host + " on " + port + ": " + e.getMessage());
			return false;
		}
		if (!shouldBenUsed)
			System.err.println("  Open Socket " + host + " on " + port);
		if (pingSocket == null)
			return false;
		//	success
		try
		{
			pingSocket.close();
		}
		catch (IOException e)
		{
			System.out.println("  CloseSocket=" + e.toString());
		}
		return true;
	}	//	testPort

	/**
	 * 	Test JDBC Connection to Server
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testJDBC (String uid, String pwd)
	{
		//	jdbc:oracle:thin:@dev:1521:dev1
		m_connectionString = "jdbc:oracle:thin:@"
			+ m_databaseServer.getHostName() + ":" + m_databasePort + ":" + m_databaseName;
		System.out.println("  JDBC = " + m_connectionString);

		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(m_connectionString,
				uid, pwd);
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testJDBC

	/**
	 * 	Test TNS Connection
	 *  @param uid user id
	 *  @param pwd password
	 * 	@return true if OK
	 */
	private boolean testTNS (String uid, String pwd)
	{
		String connectionString = "jdbc:oracle:oci8:@" + m_TNSName;
		System.out.println("  TNS = " + connectionString);
		try
		{
			if (s_driver == null)
			{
				s_driver = new OracleDriver();
				DriverManager.registerDriver(s_driver);
			}
			Connection con = DriverManager.getConnection(connectionString,
				uid, pwd);
		}
		catch (UnsatisfiedLinkError ule)
		{
			System.err.println("Check setup of Oracle Server / Oracle Client / LD_LIBRARY_PATH");
			System.err.println(ule.toString());
			return false;
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
			return false;
		}
		return true;
	}	//	testTNS

	/**
	 * 	Test Mail
	 *  @return true of OK
	 */
	private boolean testMail()
	{
		boolean smtpOK = false;
		boolean imapOK = false;
		if (testPort (m_mailServer, 25, true))
		{
			System.out.println("OK: SMTP Server contacted");
			smtpOK = true;
		}
		else
			System.err.println("Error: SMTP Server NOT available");
		//
		if (testPort (m_mailServer, 110, true))
			System.out.println("OK: POP3 Server contacted");
		else
			System.err.println("Error: POP3 Server NOT available");
		if (testPort (m_mailServer, 143, true))
		{
			System.out.println("OK: IMAP4 Server contacted");
			imapOK = true;
		}
		else
			System.err.println("Error: IMAP4 Server NOT available");
		//
		if (!smtpOK)
			return false;
		//
		try
		{
			EMail em = new EMail (m_mailServer.getHostName (),
					   m_adminEMail.toString (), m_adminEMail.toString (),
					   "Compiere Server Setup Test", "Test: " + m_properties);
			em.setEMailUser (m_mailUser, m_mailPassword);
			if (EMail.SENT_OK.equals (em.send ()))
			{
				System.out.println ("OK: Send Test Email to " + m_adminEMail);
			}
			else
			{
				System.err.println ("Error: Could NOT send Email to "
					+ m_adminEMail);
			}
		}
		catch (Exception ex)
		{
			System.err.println("testMail - " + ex.getLocalizedMessage());
			return false;
		}

		//
		if (!imapOK)
			return false;

		//	Test Read Mail Access
		Properties props = new Properties();
		props.put("mail.store.protocol", "smtp");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.host", m_mailServer.getHostName());
		props.put("mail.user", m_mailUser);
		props.put("mail.smtp.auth", "true");
		System.out.println("  Connecting to " + m_mailServer.getHostName());
		//
		Session session = null;
		Store store = null;
		try
		{
			EMailAuthenticator auth = new EMailAuthenticator (m_mailUser, m_mailPassword);
			session = Session.getDefaultInstance(props, auth);
			session.setDebug (Log.isTraceLevel (10));
			System.out.println("  Session=" + session);
			//	Connect to Store
			store = session.getStore("imap");
			System.out.println("  Store=" + store);
		}
		catch (NoSuchProviderException nsp)
		{
			System.err.println("Error Mail IMAP Provider - " + nsp.getMessage());
			return false;
		}
		catch (Exception e)
		{
			System.err.println("Error Mail IMAP - " + e.getMessage());
			return false;
		}
		try
		{
			store.connect(m_mailServer.getHostName(), m_mailUser, m_mailPassword);
			System.out.println("  Store - connected");
			Folder folder = store.getDefaultFolder();
			Folder inbox = folder.getFolder("INBOX");
			System.out.println("OK: Mail Connect to " + inbox.getFullName() + " #Msg=" + inbox.getMessageCount());
			//
			store.close();
		}
		catch (MessagingException mex)
		{
			System.err.println("Error Mail Connect " + mex.getMessage());
			return false;
		}
		return true;
	}	//	testMail

	/*************************************************************************/

	/**
	 * 	Save Settings
	 */
	private void save()
	{
		SwingWorker sw = startTest();
		while (sw.isAlive())
		{
			try
			{
				Thread.currentThread().sleep(2000);
			}
			catch (InterruptedException ex)
			{
				System.err.println("save-waiting: " + ex);
			}
		}
		sw.get();	//	block
		if (!m_success)
			return;

		//	Add
		m_properties.setProperty("COMPIERE_MAIN_VERSION", Compiere.MAIN_VERSION);
		m_properties.setProperty("COMPIERE_DATE_VERSION", Compiere.DATE_VERSION);
		m_properties.setProperty("COMPIERE_DB_VERSION", Compiere.DB_VERSION);



		//	Before we save, load Ini
		Ini.setClient(false);
		String fileName = m_compiereHome.getAbsolutePath() + File.separator + Ini.COMPIERE_PROPERTY_FILE;
		Ini.loadProperties(fileName);

		//	Save Environment
		fileName = m_compiereHome.getAbsolutePath() + File.separator + COMPIERE_ENV_FILE;
		try
		{
			FileOutputStream fos = new FileOutputStream(new File(fileName));
			m_properties.store(fos, COMPIERE_ENV_FILE);
			fos.flush();
			fos.close();
		}
		catch (Exception e)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + e.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (Throwable t)
		{
			System.err.println ("Cannot save Properties to " + fileName + " - " + t.toString());
			JOptionPane.showConfirmDialog(this, res.getString("ErrorSave"), res.getString("CompiereServerSetup"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return;
		}
		System.out.println("Properties saved to " + fileName);

		//	Sync Properties
		Ini.setCompiereHome(m_compiereHome.getAbsolutePath());
		CConnection cc = CConnection.get (Database.DB_ORACLE,
			m_databaseServer.getHostName(), m_databasePort, m_databaseName,
			m_databaseUser, m_databasePassword);
		cc.setAppsHost(m_appsServer.getHostName());
		cc.setRMIoverHTTP(false);
		Ini.setProperty(Ini.P_CONNECTION, cc.toStringLong());
		Ini.saveProperties(false);


		JOptionPane.showConfirmDialog(this, res.getString("EnvironmentSaved"),
			res.getString("CompiereServerSetup"),
			JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

		((Frame)SwingUtilities.getWindowAncestor(this)).dispose();
		System.exit(0);		//	remains active when License Dialog called
	}	//	save

}	//	ConfigurationPanel
