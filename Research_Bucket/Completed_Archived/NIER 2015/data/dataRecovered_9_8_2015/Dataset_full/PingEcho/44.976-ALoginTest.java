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
import java.io.*;
import java.net.*;
import java.sql.*;

import org.compiere.plaf.*;

/**
 *  Connection Test
 *  ** Not Translated **
 *
 *  @author Jorg Janke
 *  @version  $Id: ALoginTest.java,v 1.4 2001/11/12 01:19:07 jjanke Exp $
 */
public class ALoginTest extends JDialog implements ActionListener, Runnable
{
	/**
	 *  Consatructor
	 */
	public ALoginTest (Dialog frame, String host, String sid, String port, String uid, String pwd)
	{
		super (frame, "Connect Test: " + host, true);
		m_host = host;
		m_sid = sid;
		m_port = port;
		m_uid = uid;
		m_pwd = pwd;
		try
		{
			jbInit();
			pack();
		}
		catch(Exception ex)
		{
			inform ("Internal Error = " + ex.getMessage());
		}
		//  Start Tests
		try
		{
			m_worker = new Thread(this);
			m_worker.start();
		}
		catch (Exception e1)
		{
			inform ("Internal Error = " + e1);
		}
		AEnv.showCenterScreen(this);
	}   //  ALoginTest

	private String      m_host;
	private String      m_port;
	private String      m_sid;
	private String      m_uid;
	private String      m_pwd;
	private Thread      m_worker;

	private JPanel mainPanel = new JPanel();
	private BorderLayout mainLayout = new BorderLayout();
	private JPanel southPanel = new JPanel();
	private JButton bOK = new JButton();
	private JScrollPane infoPane = new JScrollPane();
	private JTextArea info = new JTextArea();
	private FlowLayout southLayout = new FlowLayout();

	/**
	 *  Static Layout
	 */
	void jbInit() throws Exception
	{
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		mainPanel.setLayout(mainLayout);
		bOK.setText("Exit");
		bOK.addActionListener(this);
		info.setBackground(CompierePLAF.getFieldBackground_Inactive());
		southPanel.setLayout(southLayout);
		southLayout.setAlignment(FlowLayout.RIGHT);
		infoPane.setPreferredSize(new Dimension(400, 400));
		getContentPane().add(mainPanel);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		southPanel.add(bOK, null);
		mainPanel.add(infoPane, BorderLayout.CENTER);
		infoPane.getViewport().add(info, null);
	}   //  jbInit

	/**
	 *  Inform
	 */
	private void inform (String text)
	{
		System.out.println(text);
		info.append(text);
		info.append("\n");
		info.setCaretPosition(info.getText().length());
	}   //  inform

	/**
	 *  Action Listener
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (e.getSource() == bOK)
		{
			while (m_worker != null && m_worker.isAlive())
				m_worker.interrupt();
			dispose();
		}
	}   //  actionEvent

	/**
	 *  Run individual tests
	 */
	public void run ()
	{
		String vmName = System.getProperty("java.vm.name");
		String vmVersion = System.getProperty("java.vm.version");
		inform ("Using Java=" + vmName + " " + vmVersion);
		inform ("");
		//
		boolean found = false;
		boolean foundJDBC = false;
		inform("*** Testing connection to Server: " + m_host + " ***");
		if (m_host == null || m_host.length() == 0)
		{
			inform ("ERROR: invalid host name");
			return;
		}
		String host = m_host;
		inform("Trying Echo - Port 7");
		found = testHostPort(host, 7);

		inform("Trying FTP - Port 21");
		if (testHostPort (host, 21) && !found)
			found = true;

		inform("Trying HTTP - Port 80");
		if (testHostPort (host, 80) && !found)
			found = true;

		inform("Trying Kerberos - Port 88");
		if (testHostPort (host, 88) && !found)
			found = true;

		inform("Trying NetBios Session - Port 139");
		if (testHostPort (host, 139) && !found)
			found = true;

		inform("Trying RMI - Port 1099");
		if (testHostPort (host, 1099) && !found)
			found = true;

		inform("Trying Oracle Connection Manager - Port 1630");
		if (testHostPort (host, 1630) && !found)
			found = true;

		inform("Trying Oracle JDBC - TCP Port 1521");
		foundJDBC = testHostPort (host, 1521);

		int jdbcPort = 0;
		try
		{
			jdbcPort = Integer.parseInt(m_port);
		}
		catch (Exception e)
		{
			inform ("ERROR: Cannot parse port=" + m_port);
			inform (e.getMessage());
			return;
		}
		if (jdbcPort != 1521)
		{
			inform("Trying Oracle JDBC - TCP Port " + jdbcPort);
			if (testHostPort (host, jdbcPort) && !foundJDBC)
				foundJDBC = true;
		}

		//  Test Interrupt
		if (m_worker != null && m_worker.interrupted())
			return;

		/** Info        */

		if (found && foundJDBC)
		{
			inform ("*** Server found: " + host + " ***");
			inform ("");
		}
		else if (!found && foundJDBC)
		{
			inform ("*** Server found: " + host + " (JDBC only) ***");
			inform ("");
		}
		else if (found && !foundJDBC)
		{
			inform ("ERROR: Server found: " + host + " - but no JDBC ***");
			inform ("Make sure that the Oracle Listener process is active");
			return;
		}
		else
		{
			inform ("ERROR: Server NOT found: " + host + "***");
			inform ("End Test: Make sure that you can ping the Server");
			return;
		}

		/*********************************************************************/

		inform ("Connect to SID: " + m_sid);

		inform ("Connect with entered parameters");
		if (!testJDBC(host, jdbcPort, m_sid, m_uid, m_pwd))
		{
			if (m_worker != null && m_worker.interrupted())
				return;

			if (jdbcPort != 1521)
			{
				inform ("Connect with standard JDBC port 1521");
				if (testJDBC(host, 1521, m_sid, m_uid, m_pwd))
				{
					inform ("Please set port to 1521");
					return;
				}
				if (m_worker != null && m_worker.interrupted())
					return;
			}

			inform ("Connect with user system/manager");
			if (testJDBC(host, 1521, m_sid, "system", "manager"))
			{
				inform ("Please check COMPIERE user id and password");
				inform (".... and please change SYSTEM password");
				return;
			}
		}

		inform ("*** Compiere database found: " + host + ":" + jdbcPort + ":" +  m_sid + " ***");
		if (m_worker != null && m_worker.interrupted())
			return;

		/*********************************************************************/

		inform ("");
		inform ("Testing available application users:");
		testCompiereUsers(host, jdbcPort);

		inform ("");
		inform ("*** Test complete **");
	}   //  run

	/**
	 *  Test Host Port
	 */
	private boolean testHostPort (String host, int port)
	{
		Socket pingSocket = null;
		try
		{
			/* Resolve address.     */
			InetAddress server = InetAddress.getByName(host);
			/* Establish socket.    */
			pingSocket = new Socket(server, port);
		}
		catch (UnknownHostException e)
		{
			inform ("  Unknown Host: " + e );
		}
		catch (IOException io )
		{
			inform ("  IO Exception: " + io );
		}

		if (pingSocket != null)
		{
			try
			{
				pingSocket.close();
			}
			catch (IOException e)
			{
				inform ("  IO close exception: " + e );
			}
			inform ("  *** success ***");
			return true;
		}
		else
		{
			return false;
		}
	}   //  testHostPort

	/**
	 *  Test JDBC
	 */
	private boolean testJDBC (String host, int port, String sid, String uid, String pwd)
	{
		try
		{
			inform ("  Registering Driver: oracle.jdbc.driver.OracleDriver");
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			inform ("  - driver registered");
			DriverManager.setLoginTimeout(5);
			DriverManager.setLogWriter(new PrintWriter(System.out));
			inform ("  - driver initialized");
		}
		catch (SQLException e)
		{
			inform ("ERROR: " + e.getMessage());
			return false;
		}

		boolean ok = false;

		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
		try
		{
			inform ("  Trying Client connection URL=" + urlC + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlC, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
			ok = true;
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		String urlS = "jdbc:oracle:oci8:@";
		try
		{
			inform ("  Trying Server connection URL=" + urlS + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlS, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		return ok;
	}   //  testJDBC

	/**
	 *  Test Compiere Users
	 */
	private void testCompiereUsers(String host, int port)
	{
		String sql = "SELECT Name, Password FROM AD_User WHERE IsActive='Y'";
		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + m_sid;
		try
		{
			inform ("  - Client connection URL=" + urlC + ", User=" + m_uid);
			Connection con = DriverManager.getConnection(urlC, m_uid, m_pwd);
			inform ("  - connected");
			Statement stmt = con.createStatement();
			inform ("  - statement created");
			ResultSet rs = stmt.executeQuery(sql);
			inform ("  - query executed listing active application users:");
			while (rs.next())
			{
				String user = rs.getString(1);
				String password = rs.getString(2);
				String answer = ">>  User = " + user;
				if ((user.equals("System") || user.equals("SuperUser")) && password.equals("System"))
					answer += "  with standard password (should be changed)";
				inform (answer);
			}
			rs.close();
			inform ("  - query closed");
			stmt.close();
			inform ("  - statement closed");
			con.close();
			inform ("  - connection closed");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}
	}   //  testCompiereUsers
}   //  ALoginTest
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
import java.io.*;
import java.net.*;
import java.sql.*;

import org.compiere.plaf.*;

/**
 *  Connection Test
 *  ** Not Translated **
 *
 *  @author Jorg Janke
 *  @version  $Id: ALoginTest.java,v 1.5 2003/09/29 01:04:42 jjanke Exp $
 */
public class ALoginTest extends JDialog implements ActionListener, Runnable
{
	/**
	 *  Consatructor
	 */
	public ALoginTest (Dialog frame, String host, String sid, String port, String uid, String pwd)
	{
		super (frame, "Connect Test: " + host, true);
		m_host = host;
		m_sid = sid;
		m_port = port;
		m_uid = uid;
		m_pwd = pwd;
		try
		{
			jbInit();
			pack();
		}
		catch(Exception ex)
		{
			inform ("Internal Error = " + ex.getMessage());
		}
		//  Start Tests
		try
		{
			m_worker = new Thread(this);
			m_worker.start();
		}
		catch (Exception e1)
		{
			inform ("Internal Error = " + e1);
		}
		AEnv.showCenterScreen(this);
	}   //  ALoginTest

	private String      m_host;
	private String      m_port;
	private String      m_sid;
	private String      m_uid;
	private String      m_pwd;
	private Thread      m_worker;

	private JPanel mainPanel = new JPanel();
	private BorderLayout mainLayout = new BorderLayout();
	private JPanel southPanel = new JPanel();
	private JButton bOK = new JButton();
	private JScrollPane infoPane = new JScrollPane();
	private JTextArea info = new JTextArea();
	private FlowLayout southLayout = new FlowLayout();

	/**
	 *  Static Layout
	 */
	void jbInit() throws Exception
	{
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		mainPanel.setLayout(mainLayout);
		bOK.setText("Exit");
		bOK.addActionListener(this);
		info.setBackground(CompierePLAF.getFieldBackground_Inactive());
		southPanel.setLayout(southLayout);
		southLayout.setAlignment(FlowLayout.RIGHT);
		infoPane.setPreferredSize(new Dimension(400, 400));
		getContentPane().add(mainPanel);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		southPanel.add(bOK, null);
		mainPanel.add(infoPane, BorderLayout.CENTER);
		infoPane.getViewport().add(info, null);
	}   //  jbInit

	/**
	 *  Inform
	 */
	private void inform (String text)
	{
		System.out.println(text);
		info.append(text);
		info.append("\n");
		info.setCaretPosition(info.getText().length());
	}   //  inform

	/**
	 *  Action Listener
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (e.getSource() == bOK)
		{
			while (m_worker != null && m_worker.isAlive())
				m_worker.interrupt();
			dispose();
		}
	}   //  actionEvent

	/**
	 *  Run individual tests
	 */
	public void run ()
	{
		String vmName = System.getProperty("java.vm.name");
		String vmVersion = System.getProperty("java.vm.version");
		inform ("Using Java=" + vmName + " " + vmVersion);
		inform ("");
		//
		boolean found = false;
		boolean foundJDBC = false;
		inform("*** Testing connection to Server: " + m_host + " ***");
		if (m_host == null || m_host.length() == 0)
		{
			inform ("ERROR: invalid host name");
			return;
		}
		String host = m_host;
		inform("Trying Echo - Port 7");
		found = testHostPort(host, 7);

		inform("Trying FTP - Port 21");
		if (testHostPort (host, 21) && !found)
			found = true;

		inform("Trying HTTP - Port 80");
		if (testHostPort (host, 80) && !found)
			found = true;

		inform("Trying Kerberos - Port 88");
		if (testHostPort (host, 88) && !found)
			found = true;

		inform("Trying NetBios Session - Port 139");
		if (testHostPort (host, 139) && !found)
			found = true;

		inform("Trying RMI - Port 1099");
		if (testHostPort (host, 1099) && !found)
			found = true;

		inform("Trying Oracle Connection Manager - Port 1630");
		if (testHostPort (host, 1630) && !found)
			found = true;

		inform("Trying Oracle JDBC - TCP Port 1521");
		foundJDBC = testHostPort (host, 1521);

		int jdbcPort = 0;
		try
		{
			jdbcPort = Integer.parseInt(m_port);
		}
		catch (Exception e)
		{
			inform ("ERROR: Cannot parse port=" + m_port);
			inform (e.getMessage());
			return;
		}
		if (jdbcPort != 1521)
		{
			inform("Trying Oracle JDBC - TCP Port " + jdbcPort);
			if (testHostPort (host, jdbcPort) && !foundJDBC)
				foundJDBC = true;
		}

		//  Test Interrupt
		if (m_worker != null && m_worker.isInterrupted())
			return;

		/** Info        */

		if (found && foundJDBC)
		{
			inform ("*** Server found: " + host + " ***");
			inform ("");
		}
		else if (!found && foundJDBC)
		{
			inform ("*** Server found: " + host + " (JDBC only) ***");
			inform ("");
		}
		else if (found && !foundJDBC)
		{
			inform ("ERROR: Server found: " + host + " - but no JDBC ***");
			inform ("Make sure that the Oracle Listener process is active");
			return;
		}
		else
		{
			inform ("ERROR: Server NOT found: " + host + "***");
			inform ("End Test: Make sure that you can ping the Server");
			return;
		}

		/*********************************************************************/

		inform ("Connect to SID: " + m_sid);

		inform ("Connect with entered parameters");
		if (!testJDBC(host, jdbcPort, m_sid, m_uid, m_pwd))
		{
			if (m_worker != null && m_worker.isInterrupted())
				return;

			if (jdbcPort != 1521)
			{
				inform ("Connect with standard JDBC port 1521");
				if (testJDBC(host, 1521, m_sid, m_uid, m_pwd))
				{
					inform ("Please set port to 1521");
					return;
				}
				if (m_worker != null && m_worker.isInterrupted())
					return;
			}

			inform ("Connect with user system/manager");
			if (testJDBC(host, 1521, m_sid, "system", "manager"))
			{
				inform ("Please check COMPIERE user id and password");
				inform (".... and please change SYSTEM password");
				return;
			}
		}

		inform ("*** Compiere database found: " + host + ":" + jdbcPort + ":" +  m_sid + " ***");
		if (m_worker != null && m_worker.isInterrupted())
			return;

		/*********************************************************************/

		inform ("");
		inform ("Testing available application users:");
		testCompiereUsers(host, jdbcPort);

		inform ("");
		inform ("*** Test complete **");
	}   //  run

	/**
	 *  Test Host Port
	 */
	private boolean testHostPort (String host, int port)
	{
		Socket pingSocket = null;
		try
		{
			/* Resolve address.     */
			InetAddress server = InetAddress.getByName(host);
			/* Establish socket.    */
			pingSocket = new Socket(server, port);
		}
		catch (UnknownHostException e)
		{
			inform ("  Unknown Host: " + e );
		}
		catch (IOException io )
		{
			inform ("  IO Exception: " + io );
		}

		if (pingSocket != null)
		{
			try
			{
				pingSocket.close();
			}
			catch (IOException e)
			{
				inform ("  IO close exception: " + e );
			}
			inform ("  *** success ***");
			return true;
		}
		else
		{
			return false;
		}
	}   //  testHostPort

	/**
	 *  Test JDBC
	 */
	private boolean testJDBC (String host, int port, String sid, String uid, String pwd)
	{
		try
		{
			inform ("  Registering Driver: oracle.jdbc.driver.OracleDriver");
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			inform ("  - driver registered");
			DriverManager.setLoginTimeout(5);
			DriverManager.setLogWriter(new PrintWriter(System.out));
			inform ("  - driver initialized");
		}
		catch (SQLException e)
		{
			inform ("ERROR: " + e.getMessage());
			return false;
		}

		boolean ok = false;

		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
		try
		{
			inform ("  Trying Client connection URL=" + urlC + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlC, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
			ok = true;
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		String urlS = "jdbc:oracle:oci8:@";
		try
		{
			inform ("  Trying Server connection URL=" + urlS + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlS, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		return ok;
	}   //  testJDBC

	/**
	 *  Test Compiere Users
	 */
	private void testCompiereUsers(String host, int port)
	{
		String sql = "SELECT Name, Password FROM AD_User WHERE IsActive='Y'";
		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + m_sid;
		try
		{
			inform ("  - Client connection URL=" + urlC + ", User=" + m_uid);
			Connection con = DriverManager.getConnection(urlC, m_uid, m_pwd);
			inform ("  - connected");
			Statement stmt = con.createStatement();
			inform ("  - statement created");
			ResultSet rs = stmt.executeQuery(sql);
			inform ("  - query executed listing active application users:");
			while (rs.next())
			{
				String user = rs.getString(1);
				String password = rs.getString(2);
				String answer = ">>  User = " + user;
				if ((user.equals("System") || user.equals("SuperUser")) && password.equals("System"))
					answer += "  with standard password (should be changed)";
				inform (answer);
			}
			rs.close();
			inform ("  - query closed");
			stmt.close();
			inform ("  - statement closed");
			con.close();
			inform ("  - connection closed");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}
	}   //  testCompiereUsers
}   //  ALoginTest
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
import java.io.*;
import java.net.*;
import java.sql.*;

import org.compiere.plaf.*;

/**
 *  Connection Test
 *  ** Not Translated **
 *
 *  @author Jorg Janke
 *  @version  $Id: ALoginTest.java,v 1.5 2003/09/29 01:04:42 jjanke Exp $
 */
public class ALoginTest extends JDialog implements ActionListener, Runnable
{
	/**
	 *  Consatructor
	 */
	public ALoginTest (Dialog frame, String host, String sid, String port, String uid, String pwd)
	{
		super (frame, "Connect Test: " + host, true);
		m_host = host;
		m_sid = sid;
		m_port = port;
		m_uid = uid;
		m_pwd = pwd;
		try
		{
			jbInit();
			pack();
		}
		catch(Exception ex)
		{
			inform ("Internal Error = " + ex.getMessage());
		}
		//  Start Tests
		try
		{
			m_worker = new Thread(this);
			m_worker.start();
		}
		catch (Exception e1)
		{
			inform ("Internal Error = " + e1);
		}
		AEnv.showCenterScreen(this);
	}   //  ALoginTest

	private String      m_host;
	private String      m_port;
	private String      m_sid;
	private String      m_uid;
	private String      m_pwd;
	private Thread      m_worker;

	private JPanel mainPanel = new JPanel();
	private BorderLayout mainLayout = new BorderLayout();
	private JPanel southPanel = new JPanel();
	private JButton bOK = new JButton();
	private JScrollPane infoPane = new JScrollPane();
	private JTextArea info = new JTextArea();
	private FlowLayout southLayout = new FlowLayout();

	/**
	 *  Static Layout
	 */
	void jbInit() throws Exception
	{
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		mainPanel.setLayout(mainLayout);
		bOK.setText("Exit");
		bOK.addActionListener(this);
		info.setBackground(CompierePLAF.getFieldBackground_Inactive());
		southPanel.setLayout(southLayout);
		southLayout.setAlignment(FlowLayout.RIGHT);
		infoPane.setPreferredSize(new Dimension(400, 400));
		getContentPane().add(mainPanel);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		southPanel.add(bOK, null);
		mainPanel.add(infoPane, BorderLayout.CENTER);
		infoPane.getViewport().add(info, null);
	}   //  jbInit

	/**
	 *  Inform
	 */
	private void inform (String text)
	{
		System.out.println(text);
		info.append(text);
		info.append("\n");
		info.setCaretPosition(info.getText().length());
	}   //  inform

	/**
	 *  Action Listener
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (e.getSource() == bOK)
		{
			while (m_worker != null && m_worker.isAlive())
				m_worker.interrupt();
			dispose();
		}
	}   //  actionEvent

	/**
	 *  Run individual tests
	 */
	public void run ()
	{
		String vmName = System.getProperty("java.vm.name");
		String vmVersion = System.getProperty("java.vm.version");
		inform ("Using Java=" + vmName + " " + vmVersion);
		inform ("");
		//
		boolean found = false;
		boolean foundJDBC = false;
		inform("*** Testing connection to Server: " + m_host + " ***");
		if (m_host == null || m_host.length() == 0)
		{
			inform ("ERROR: invalid host name");
			return;
		}
		String host = m_host;
		inform("Trying Echo - Port 7");
		found = testHostPort(host, 7);

		inform("Trying FTP - Port 21");
		if (testHostPort (host, 21) && !found)
			found = true;

		inform("Trying HTTP - Port 80");
		if (testHostPort (host, 80) && !found)
			found = true;

		inform("Trying Kerberos - Port 88");
		if (testHostPort (host, 88) && !found)
			found = true;

		inform("Trying NetBios Session - Port 139");
		if (testHostPort (host, 139) && !found)
			found = true;

		inform("Trying RMI - Port 1099");
		if (testHostPort (host, 1099) && !found)
			found = true;

		inform("Trying Oracle Connection Manager - Port 1630");
		if (testHostPort (host, 1630) && !found)
			found = true;

		inform("Trying Oracle JDBC - TCP Port 1521");
		foundJDBC = testHostPort (host, 1521);

		int jdbcPort = 0;
		try
		{
			jdbcPort = Integer.parseInt(m_port);
		}
		catch (Exception e)
		{
			inform ("ERROR: Cannot parse port=" + m_port);
			inform (e.getMessage());
			return;
		}
		if (jdbcPort != 1521)
		{
			inform("Trying Oracle JDBC - TCP Port " + jdbcPort);
			if (testHostPort (host, jdbcPort) && !foundJDBC)
				foundJDBC = true;
		}

		//  Test Interrupt
		if (m_worker != null && m_worker.isInterrupted())
			return;

		/** Info        */

		if (found && foundJDBC)
		{
			inform ("*** Server found: " + host + " ***");
			inform ("");
		}
		else if (!found && foundJDBC)
		{
			inform ("*** Server found: " + host + " (JDBC only) ***");
			inform ("");
		}
		else if (found && !foundJDBC)
		{
			inform ("ERROR: Server found: " + host + " - but no JDBC ***");
			inform ("Make sure that the Oracle Listener process is active");
			return;
		}
		else
		{
			inform ("ERROR: Server NOT found: " + host + "***");
			inform ("End Test: Make sure that you can ping the Server");
			return;
		}

		/*********************************************************************/

		inform ("Connect to SID: " + m_sid);

		inform ("Connect with entered parameters");
		if (!testJDBC(host, jdbcPort, m_sid, m_uid, m_pwd))
		{
			if (m_worker != null && m_worker.isInterrupted())
				return;

			if (jdbcPort != 1521)
			{
				inform ("Connect with standard JDBC port 1521");
				if (testJDBC(host, 1521, m_sid, m_uid, m_pwd))
				{
					inform ("Please set port to 1521");
					return;
				}
				if (m_worker != null && m_worker.isInterrupted())
					return;
			}

			inform ("Connect with user system/manager");
			if (testJDBC(host, 1521, m_sid, "system", "manager"))
			{
				inform ("Please check COMPIERE user id and password");
				inform (".... and please change SYSTEM password");
				return;
			}
		}

		inform ("*** Compiere database found: " + host + ":" + jdbcPort + ":" +  m_sid + " ***");
		if (m_worker != null && m_worker.isInterrupted())
			return;

		/*********************************************************************/

		inform ("");
		inform ("Testing available application users:");
		testCompiereUsers(host, jdbcPort);

		inform ("");
		inform ("*** Test complete **");
	}   //  run

	/**
	 *  Test Host Port
	 */
	private boolean testHostPort (String host, int port)
	{
		Socket pingSocket = null;
		try
		{
			/* Resolve address.     */
			InetAddress server = InetAddress.getByName(host);
			/* Establish socket.    */
			pingSocket = new Socket(server, port);
		}
		catch (UnknownHostException e)
		{
			inform ("  Unknown Host: " + e );
		}
		catch (IOException io )
		{
			inform ("  IO Exception: " + io );
		}

		if (pingSocket != null)
		{
			try
			{
				pingSocket.close();
			}
			catch (IOException e)
			{
				inform ("  IO close exception: " + e );
			}
			inform ("  *** success ***");
			return true;
		}
		else
		{
			return false;
		}
	}   //  testHostPort

	/**
	 *  Test JDBC
	 */
	private boolean testJDBC (String host, int port, String sid, String uid, String pwd)
	{
		try
		{
			inform ("  Registering Driver: oracle.jdbc.driver.OracleDriver");
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			inform ("  - driver registered");
			DriverManager.setLoginTimeout(5);
			DriverManager.setLogWriter(new PrintWriter(System.out));
			inform ("  - driver initialized");
		}
		catch (SQLException e)
		{
			inform ("ERROR: " + e.getMessage());
			return false;
		}

		boolean ok = false;

		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
		try
		{
			inform ("  Trying Client connection URL=" + urlC + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlC, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
			ok = true;
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		String urlS = "jdbc:oracle:oci8:@";
		try
		{
			inform ("  Trying Server connection URL=" + urlS + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlS, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		return ok;
	}   //  testJDBC

	/**
	 *  Test Compiere Users
	 */
	private void testCompiereUsers(String host, int port)
	{
		String sql = "SELECT Name, Password FROM AD_User WHERE IsActive='Y'";
		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + m_sid;
		try
		{
			inform ("  - Client connection URL=" + urlC + ", User=" + m_uid);
			Connection con = DriverManager.getConnection(urlC, m_uid, m_pwd);
			inform ("  - connected");
			Statement stmt = con.createStatement();
			inform ("  - statement created");
			ResultSet rs = stmt.executeQuery(sql);
			inform ("  - query executed listing active application users:");
			while (rs.next())
			{
				String user = rs.getString(1);
				String password = rs.getString(2);
				String answer = ">>  User = " + user;
				if ((user.equals("System") || user.equals("SuperUser")) && password.equals("System"))
					answer += "  with standard password (should be changed)";
				inform (answer);
			}
			rs.close();
			inform ("  - query closed");
			stmt.close();
			inform ("  - statement closed");
			con.close();
			inform ("  - connection closed");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}
	}   //  testCompiereUsers
}   //  ALoginTest
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
import java.io.*;
import java.net.*;
import java.sql.*;

import org.compiere.plaf.*;

/**
 *  Connection Test
 *  ** Not Translated **
 *
 *  @author Jorg Janke
 *  @version  $Id: ALoginTest.java,v 1.4 2001/11/12 01:19:07 jjanke Exp $
 */
public class ALoginTest extends JDialog implements ActionListener, Runnable
{
	/**
	 *  Consatructor
	 */
	public ALoginTest (Dialog frame, String host, String sid, String port, String uid, String pwd)
	{
		super (frame, "Connect Test: " + host, true);
		m_host = host;
		m_sid = sid;
		m_port = port;
		m_uid = uid;
		m_pwd = pwd;
		try
		{
			jbInit();
			pack();
		}
		catch(Exception ex)
		{
			inform ("Internal Error = " + ex.getMessage());
		}
		//  Start Tests
		try
		{
			m_worker = new Thread(this);
			m_worker.start();
		}
		catch (Exception e1)
		{
			inform ("Internal Error = " + e1);
		}
		AEnv.showCenterScreen(this);
	}   //  ALoginTest

	private String      m_host;
	private String      m_port;
	private String      m_sid;
	private String      m_uid;
	private String      m_pwd;
	private Thread      m_worker;

	private JPanel mainPanel = new JPanel();
	private BorderLayout mainLayout = new BorderLayout();
	private JPanel southPanel = new JPanel();
	private JButton bOK = new JButton();
	private JScrollPane infoPane = new JScrollPane();
	private JTextArea info = new JTextArea();
	private FlowLayout southLayout = new FlowLayout();

	/**
	 *  Static Layout
	 */
	void jbInit() throws Exception
	{
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		mainPanel.setLayout(mainLayout);
		bOK.setText("Exit");
		bOK.addActionListener(this);
		info.setBackground(CompierePLAF.getFieldBackground_Inactive());
		southPanel.setLayout(southLayout);
		southLayout.setAlignment(FlowLayout.RIGHT);
		infoPane.setPreferredSize(new Dimension(400, 400));
		getContentPane().add(mainPanel);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		southPanel.add(bOK, null);
		mainPanel.add(infoPane, BorderLayout.CENTER);
		infoPane.getViewport().add(info, null);
	}   //  jbInit

	/**
	 *  Inform
	 */
	private void inform (String text)
	{
		System.out.println(text);
		info.append(text);
		info.append("\n");
		info.setCaretPosition(info.getText().length());
	}   //  inform

	/**
	 *  Action Listener
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (e.getSource() == bOK)
		{
			while (m_worker != null && m_worker.isAlive())
				m_worker.interrupt();
			dispose();
		}
	}   //  actionEvent

	/**
	 *  Run individual tests
	 */
	public void run ()
	{
		String vmName = System.getProperty("java.vm.name");
		String vmVersion = System.getProperty("java.vm.version");
		inform ("Using Java=" + vmName + " " + vmVersion);
		inform ("");
		//
		boolean found = false;
		boolean foundJDBC = false;
		inform("*** Testing connection to Server: " + m_host + " ***");
		if (m_host == null || m_host.length() == 0)
		{
			inform ("ERROR: invalid host name");
			return;
		}
		String host = m_host;
		inform("Trying Echo - Port 7");
		found = testHostPort(host, 7);

		inform("Trying FTP - Port 21");
		if (testHostPort (host, 21) && !found)
			found = true;

		inform("Trying HTTP - Port 80");
		if (testHostPort (host, 80) && !found)
			found = true;

		inform("Trying Kerberos - Port 88");
		if (testHostPort (host, 88) && !found)
			found = true;

		inform("Trying NetBios Session - Port 139");
		if (testHostPort (host, 139) && !found)
			found = true;

		inform("Trying RMI - Port 1099");
		if (testHostPort (host, 1099) && !found)
			found = true;

		inform("Trying Oracle Connection Manager - Port 1630");
		if (testHostPort (host, 1630) && !found)
			found = true;

		inform("Trying Oracle JDBC - TCP Port 1521");
		foundJDBC = testHostPort (host, 1521);

		int jdbcPort = 0;
		try
		{
			jdbcPort = Integer.parseInt(m_port);
		}
		catch (Exception e)
		{
			inform ("ERROR: Cannot parse port=" + m_port);
			inform (e.getMessage());
			return;
		}
		if (jdbcPort != 1521)
		{
			inform("Trying Oracle JDBC - TCP Port " + jdbcPort);
			if (testHostPort (host, jdbcPort) && !foundJDBC)
				foundJDBC = true;
		}

		//  Test Interrupt
		if (m_worker != null && m_worker.interrupted())
			return;

		/** Info        */

		if (found && foundJDBC)
		{
			inform ("*** Server found: " + host + " ***");
			inform ("");
		}
		else if (!found && foundJDBC)
		{
			inform ("*** Server found: " + host + " (JDBC only) ***");
			inform ("");
		}
		else if (found && !foundJDBC)
		{
			inform ("ERROR: Server found: " + host + " - but no JDBC ***");
			inform ("Make sure that the Oracle Listener process is active");
			return;
		}
		else
		{
			inform ("ERROR: Server NOT found: " + host + "***");
			inform ("End Test: Make sure that you can ping the Server");
			return;
		}

		/*********************************************************************/

		inform ("Connect to SID: " + m_sid);

		inform ("Connect with entered parameters");
		if (!testJDBC(host, jdbcPort, m_sid, m_uid, m_pwd))
		{
			if (m_worker != null && m_worker.interrupted())
				return;

			if (jdbcPort != 1521)
			{
				inform ("Connect with standard JDBC port 1521");
				if (testJDBC(host, 1521, m_sid, m_uid, m_pwd))
				{
					inform ("Please set port to 1521");
					return;
				}
				if (m_worker != null && m_worker.interrupted())
					return;
			}

			inform ("Connect with user system/manager");
			if (testJDBC(host, 1521, m_sid, "system", "manager"))
			{
				inform ("Please check COMPIERE user id and password");
				inform (".... and please change SYSTEM password");
				return;
			}
		}

		inform ("*** Compiere database found: " + host + ":" + jdbcPort + ":" +  m_sid + " ***");
		if (m_worker != null && m_worker.interrupted())
			return;

		/*********************************************************************/

		inform ("");
		inform ("Testing available application users:");
		testCompiereUsers(host, jdbcPort);

		inform ("");
		inform ("*** Test complete **");
	}   //  run

	/**
	 *  Test Host Port
	 */
	private boolean testHostPort (String host, int port)
	{
		Socket pingSocket = null;
		try
		{
			/* Resolve address.     */
			InetAddress server = InetAddress.getByName(host);
			/* Establish socket.    */
			pingSocket = new Socket(server, port);
		}
		catch (UnknownHostException e)
		{
			inform ("  Unknown Host: " + e );
		}
		catch (IOException io )
		{
			inform ("  IO Exception: " + io );
		}

		if (pingSocket != null)
		{
			try
			{
				pingSocket.close();
			}
			catch (IOException e)
			{
				inform ("  IO close exception: " + e );
			}
			inform ("  *** success ***");
			return true;
		}
		else
		{
			return false;
		}
	}   //  testHostPort

	/**
	 *  Test JDBC
	 */
	private boolean testJDBC (String host, int port, String sid, String uid, String pwd)
	{
		try
		{
			inform ("  Registering Driver: oracle.jdbc.driver.OracleDriver");
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			inform ("  - driver registered");
			DriverManager.setLoginTimeout(5);
			DriverManager.setLogWriter(new PrintWriter(System.out));
			inform ("  - driver initialized");
		}
		catch (SQLException e)
		{
			inform ("ERROR: " + e.getMessage());
			return false;
		}

		boolean ok = false;

		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
		try
		{
			inform ("  Trying Client connection URL=" + urlC + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlC, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
			ok = true;
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		String urlS = "jdbc:oracle:oci8:@";
		try
		{
			inform ("  Trying Server connection URL=" + urlS + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlS, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		return ok;
	}   //  testJDBC

	/**
	 *  Test Compiere Users
	 */
	private void testCompiereUsers(String host, int port)
	{
		String sql = "SELECT Name, Password FROM AD_User WHERE IsActive='Y'";
		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + m_sid;
		try
		{
			inform ("  - Client connection URL=" + urlC + ", User=" + m_uid);
			Connection con = DriverManager.getConnection(urlC, m_uid, m_pwd);
			inform ("  - connected");
			Statement stmt = con.createStatement();
			inform ("  - statement created");
			ResultSet rs = stmt.executeQuery(sql);
			inform ("  - query executed listing active application users:");
			while (rs.next())
			{
				String user = rs.getString(1);
				String password = rs.getString(2);
				String answer = ">>  User = " + user;
				if ((user.equals("System") || user.equals("SuperUser")) && password.equals("System"))
					answer += "  with standard password (should be changed)";
				inform (answer);
			}
			rs.close();
			inform ("  - query closed");
			stmt.close();
			inform ("  - statement closed");
			con.close();
			inform ("  - connection closed");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}
	}   //  testCompiereUsers
}   //  ALoginTest
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
import java.io.*;
import java.net.*;
import java.sql.*;

import org.compiere.plaf.*;

/**
 *  Connection Test
 *  ** Not Translated **
 *
 *  @author Jorg Janke
 *  @version  $Id: ALoginTest.java,v 1.5 2003/09/29 01:04:42 jjanke Exp $
 */
public class ALoginTest extends JDialog implements ActionListener, Runnable
{
	/**
	 *  Consatructor
	 */
	public ALoginTest (Dialog frame, String host, String sid, String port, String uid, String pwd)
	{
		super (frame, "Connect Test: " + host, true);
		m_host = host;
		m_sid = sid;
		m_port = port;
		m_uid = uid;
		m_pwd = pwd;
		try
		{
			jbInit();
			pack();
		}
		catch(Exception ex)
		{
			inform ("Internal Error = " + ex.getMessage());
		}
		//  Start Tests
		try
		{
			m_worker = new Thread(this);
			m_worker.start();
		}
		catch (Exception e1)
		{
			inform ("Internal Error = " + e1);
		}
		AEnv.showCenterScreen(this);
	}   //  ALoginTest

	private String      m_host;
	private String      m_port;
	private String      m_sid;
	private String      m_uid;
	private String      m_pwd;
	private Thread      m_worker;

	private JPanel mainPanel = new JPanel();
	private BorderLayout mainLayout = new BorderLayout();
	private JPanel southPanel = new JPanel();
	private JButton bOK = new JButton();
	private JScrollPane infoPane = new JScrollPane();
	private JTextArea info = new JTextArea();
	private FlowLayout southLayout = new FlowLayout();

	/**
	 *  Static Layout
	 */
	void jbInit() throws Exception
	{
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		mainPanel.setLayout(mainLayout);
		bOK.setText("Exit");
		bOK.addActionListener(this);
		info.setBackground(CompierePLAF.getFieldBackground_Inactive());
		southPanel.setLayout(southLayout);
		southLayout.setAlignment(FlowLayout.RIGHT);
		infoPane.setPreferredSize(new Dimension(400, 400));
		getContentPane().add(mainPanel);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		southPanel.add(bOK, null);
		mainPanel.add(infoPane, BorderLayout.CENTER);
		infoPane.getViewport().add(info, null);
	}   //  jbInit

	/**
	 *  Inform
	 */
	private void inform (String text)
	{
		System.out.println(text);
		info.append(text);
		info.append("\n");
		info.setCaretPosition(info.getText().length());
	}   //  inform

	/**
	 *  Action Listener
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (e.getSource() == bOK)
		{
			while (m_worker != null && m_worker.isAlive())
				m_worker.interrupt();
			dispose();
		}
	}   //  actionEvent

	/**
	 *  Run individual tests
	 */
	public void run ()
	{
		String vmName = System.getProperty("java.vm.name");
		String vmVersion = System.getProperty("java.vm.version");
		inform ("Using Java=" + vmName + " " + vmVersion);
		inform ("");
		//
		boolean found = false;
		boolean foundJDBC = false;
		inform("*** Testing connection to Server: " + m_host + " ***");
		if (m_host == null || m_host.length() == 0)
		{
			inform ("ERROR: invalid host name");
			return;
		}
		String host = m_host;
		inform("Trying Echo - Port 7");
		found = testHostPort(host, 7);

		inform("Trying FTP - Port 21");
		if (testHostPort (host, 21) && !found)
			found = true;

		inform("Trying HTTP - Port 80");
		if (testHostPort (host, 80) && !found)
			found = true;

		inform("Trying Kerberos - Port 88");
		if (testHostPort (host, 88) && !found)
			found = true;

		inform("Trying NetBios Session - Port 139");
		if (testHostPort (host, 139) && !found)
			found = true;

		inform("Trying RMI - Port 1099");
		if (testHostPort (host, 1099) && !found)
			found = true;

		inform("Trying Oracle Connection Manager - Port 1630");
		if (testHostPort (host, 1630) && !found)
			found = true;

		inform("Trying Oracle JDBC - TCP Port 1521");
		foundJDBC = testHostPort (host, 1521);

		int jdbcPort = 0;
		try
		{
			jdbcPort = Integer.parseInt(m_port);
		}
		catch (Exception e)
		{
			inform ("ERROR: Cannot parse port=" + m_port);
			inform (e.getMessage());
			return;
		}
		if (jdbcPort != 1521)
		{
			inform("Trying Oracle JDBC - TCP Port " + jdbcPort);
			if (testHostPort (host, jdbcPort) && !foundJDBC)
				foundJDBC = true;
		}

		//  Test Interrupt
		if (m_worker != null && m_worker.isInterrupted())
			return;

		/** Info        */

		if (found && foundJDBC)
		{
			inform ("*** Server found: " + host + " ***");
			inform ("");
		}
		else if (!found && foundJDBC)
		{
			inform ("*** Server found: " + host + " (JDBC only) ***");
			inform ("");
		}
		else if (found && !foundJDBC)
		{
			inform ("ERROR: Server found: " + host + " - but no JDBC ***");
			inform ("Make sure that the Oracle Listener process is active");
			return;
		}
		else
		{
			inform ("ERROR: Server NOT found: " + host + "***");
			inform ("End Test: Make sure that you can ping the Server");
			return;
		}

		/*********************************************************************/

		inform ("Connect to SID: " + m_sid);

		inform ("Connect with entered parameters");
		if (!testJDBC(host, jdbcPort, m_sid, m_uid, m_pwd))
		{
			if (m_worker != null && m_worker.isInterrupted())
				return;

			if (jdbcPort != 1521)
			{
				inform ("Connect with standard JDBC port 1521");
				if (testJDBC(host, 1521, m_sid, m_uid, m_pwd))
				{
					inform ("Please set port to 1521");
					return;
				}
				if (m_worker != null && m_worker.isInterrupted())
					return;
			}

			inform ("Connect with user system/manager");
			if (testJDBC(host, 1521, m_sid, "system", "manager"))
			{
				inform ("Please check COMPIERE user id and password");
				inform (".... and please change SYSTEM password");
				return;
			}
		}

		inform ("*** Compiere database found: " + host + ":" + jdbcPort + ":" +  m_sid + " ***");
		if (m_worker != null && m_worker.isInterrupted())
			return;

		/*********************************************************************/

		inform ("");
		inform ("Testing available application users:");
		testCompiereUsers(host, jdbcPort);

		inform ("");
		inform ("*** Test complete **");
	}   //  run

	/**
	 *  Test Host Port
	 */
	private boolean testHostPort (String host, int port)
	{
		Socket pingSocket = null;
		try
		{
			/* Resolve address.     */
			InetAddress server = InetAddress.getByName(host);
			/* Establish socket.    */
			pingSocket = new Socket(server, port);
		}
		catch (UnknownHostException e)
		{
			inform ("  Unknown Host: " + e );
		}
		catch (IOException io )
		{
			inform ("  IO Exception: " + io );
		}

		if (pingSocket != null)
		{
			try
			{
				pingSocket.close();
			}
			catch (IOException e)
			{
				inform ("  IO close exception: " + e );
			}
			inform ("  *** success ***");
			return true;
		}
		else
		{
			return false;
		}
	}   //  testHostPort

	/**
	 *  Test JDBC
	 */
	private boolean testJDBC (String host, int port, String sid, String uid, String pwd)
	{
		try
		{
			inform ("  Registering Driver: oracle.jdbc.driver.OracleDriver");
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			inform ("  - driver registered");
			DriverManager.setLoginTimeout(5);
			DriverManager.setLogWriter(new PrintWriter(System.out));
			inform ("  - driver initialized");
		}
		catch (SQLException e)
		{
			inform ("ERROR: " + e.getMessage());
			return false;
		}

		boolean ok = false;

		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
		try
		{
			inform ("  Trying Client connection URL=" + urlC + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlC, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
			ok = true;
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		String urlS = "jdbc:oracle:oci8:@";
		try
		{
			inform ("  Trying Server connection URL=" + urlS + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlS, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		return ok;
	}   //  testJDBC

	/**
	 *  Test Compiere Users
	 */
	private void testCompiereUsers(String host, int port)
	{
		String sql = "SELECT Name, Password FROM AD_User WHERE IsActive='Y'";
		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + m_sid;
		try
		{
			inform ("  - Client connection URL=" + urlC + ", User=" + m_uid);
			Connection con = DriverManager.getConnection(urlC, m_uid, m_pwd);
			inform ("  - connected");
			Statement stmt = con.createStatement();
			inform ("  - statement created");
			ResultSet rs = stmt.executeQuery(sql);
			inform ("  - query executed listing active application users:");
			while (rs.next())
			{
				String user = rs.getString(1);
				String password = rs.getString(2);
				String answer = ">>  User = " + user;
				if ((user.equals("System") || user.equals("SuperUser")) && password.equals("System"))
					answer += "  with standard password (should be changed)";
				inform (answer);
			}
			rs.close();
			inform ("  - query closed");
			stmt.close();
			inform ("  - statement closed");
			con.close();
			inform ("  - connection closed");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}
	}   //  testCompiereUsers
}   //  ALoginTest
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
import java.io.*;
import java.net.*;
import java.sql.*;

import org.compiere.plaf.*;

/**
 *  Connection Test
 *  ** Not Translated **
 *
 *  @author Jorg Janke
 *  @version  $Id: ALoginTest.java,v 1.4 2001/11/12 01:19:07 jjanke Exp $
 */
public class ALoginTest extends JDialog implements ActionListener, Runnable
{
	/**
	 *  Consatructor
	 */
	public ALoginTest (Dialog frame, String host, String sid, String port, String uid, String pwd)
	{
		super (frame, "Connect Test: " + host, true);
		m_host = host;
		m_sid = sid;
		m_port = port;
		m_uid = uid;
		m_pwd = pwd;
		try
		{
			jbInit();
			pack();
		}
		catch(Exception ex)
		{
			inform ("Internal Error = " + ex.getMessage());
		}
		//  Start Tests
		try
		{
			m_worker = new Thread(this);
			m_worker.start();
		}
		catch (Exception e1)
		{
			inform ("Internal Error = " + e1);
		}
		AEnv.showCenterScreen(this);
	}   //  ALoginTest

	private String      m_host;
	private String      m_port;
	private String      m_sid;
	private String      m_uid;
	private String      m_pwd;
	private Thread      m_worker;

	private JPanel mainPanel = new JPanel();
	private BorderLayout mainLayout = new BorderLayout();
	private JPanel southPanel = new JPanel();
	private JButton bOK = new JButton();
	private JScrollPane infoPane = new JScrollPane();
	private JTextArea info = new JTextArea();
	private FlowLayout southLayout = new FlowLayout();

	/**
	 *  Static Layout
	 */
	void jbInit() throws Exception
	{
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		mainPanel.setLayout(mainLayout);
		bOK.setText("Exit");
		bOK.addActionListener(this);
		info.setBackground(CompierePLAF.getFieldBackground_Inactive());
		southPanel.setLayout(southLayout);
		southLayout.setAlignment(FlowLayout.RIGHT);
		infoPane.setPreferredSize(new Dimension(400, 400));
		getContentPane().add(mainPanel);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		southPanel.add(bOK, null);
		mainPanel.add(infoPane, BorderLayout.CENTER);
		infoPane.getViewport().add(info, null);
	}   //  jbInit

	/**
	 *  Inform
	 */
	private void inform (String text)
	{
		System.out.println(text);
		info.append(text);
		info.append("\n");
		info.setCaretPosition(info.getText().length());
	}   //  inform

	/**
	 *  Action Listener
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (e.getSource() == bOK)
		{
			while (m_worker != null && m_worker.isAlive())
				m_worker.interrupt();
			dispose();
		}
	}   //  actionEvent

	/**
	 *  Run individual tests
	 */
	public void run ()
	{
		String vmName = System.getProperty("java.vm.name");
		String vmVersion = System.getProperty("java.vm.version");
		inform ("Using Java=" + vmName + " " + vmVersion);
		inform ("");
		//
		boolean found = false;
		boolean foundJDBC = false;
		inform("*** Testing connection to Server: " + m_host + " ***");
		if (m_host == null || m_host.length() == 0)
		{
			inform ("ERROR: invalid host name");
			return;
		}
		String host = m_host;
		inform("Trying Echo - Port 7");
		found = testHostPort(host, 7);

		inform("Trying FTP - Port 21");
		if (testHostPort (host, 21) && !found)
			found = true;

		inform("Trying HTTP - Port 80");
		if (testHostPort (host, 80) && !found)
			found = true;

		inform("Trying Kerberos - Port 88");
		if (testHostPort (host, 88) && !found)
			found = true;

		inform("Trying NetBios Session - Port 139");
		if (testHostPort (host, 139) && !found)
			found = true;

		inform("Trying RMI - Port 1099");
		if (testHostPort (host, 1099) && !found)
			found = true;

		inform("Trying Oracle Connection Manager - Port 1630");
		if (testHostPort (host, 1630) && !found)
			found = true;

		inform("Trying Oracle JDBC - TCP Port 1521");
		foundJDBC = testHostPort (host, 1521);

		int jdbcPort = 0;
		try
		{
			jdbcPort = Integer.parseInt(m_port);
		}
		catch (Exception e)
		{
			inform ("ERROR: Cannot parse port=" + m_port);
			inform (e.getMessage());
			return;
		}
		if (jdbcPort != 1521)
		{
			inform("Trying Oracle JDBC - TCP Port " + jdbcPort);
			if (testHostPort (host, jdbcPort) && !foundJDBC)
				foundJDBC = true;
		}

		//  Test Interrupt
		if (m_worker != null && m_worker.interrupted())
			return;

		/** Info        */

		if (found && foundJDBC)
		{
			inform ("*** Server found: " + host + " ***");
			inform ("");
		}
		else if (!found && foundJDBC)
		{
			inform ("*** Server found: " + host + " (JDBC only) ***");
			inform ("");
		}
		else if (found && !foundJDBC)
		{
			inform ("ERROR: Server found: " + host + " - but no JDBC ***");
			inform ("Make sure that the Oracle Listener process is active");
			return;
		}
		else
		{
			inform ("ERROR: Server NOT found: " + host + "***");
			inform ("End Test: Make sure that you can ping the Server");
			return;
		}

		/*********************************************************************/

		inform ("Connect to SID: " + m_sid);

		inform ("Connect with entered parameters");
		if (!testJDBC(host, jdbcPort, m_sid, m_uid, m_pwd))
		{
			if (m_worker != null && m_worker.interrupted())
				return;

			if (jdbcPort != 1521)
			{
				inform ("Connect with standard JDBC port 1521");
				if (testJDBC(host, 1521, m_sid, m_uid, m_pwd))
				{
					inform ("Please set port to 1521");
					return;
				}
				if (m_worker != null && m_worker.interrupted())
					return;
			}

			inform ("Connect with user system/manager");
			if (testJDBC(host, 1521, m_sid, "system", "manager"))
			{
				inform ("Please check COMPIERE user id and password");
				inform (".... and please change SYSTEM password");
				return;
			}
		}

		inform ("*** Compiere database found: " + host + ":" + jdbcPort + ":" +  m_sid + " ***");
		if (m_worker != null && m_worker.interrupted())
			return;

		/*********************************************************************/

		inform ("");
		inform ("Testing available application users:");
		testCompiereUsers(host, jdbcPort);

		inform ("");
		inform ("*** Test complete **");
	}   //  run

	/**
	 *  Test Host Port
	 */
	private boolean testHostPort (String host, int port)
	{
		Socket pingSocket = null;
		try
		{
			/* Resolve address.     */
			InetAddress server = InetAddress.getByName(host);
			/* Establish socket.    */
			pingSocket = new Socket(server, port);
		}
		catch (UnknownHostException e)
		{
			inform ("  Unknown Host: " + e );
		}
		catch (IOException io )
		{
			inform ("  IO Exception: " + io );
		}

		if (pingSocket != null)
		{
			try
			{
				pingSocket.close();
			}
			catch (IOException e)
			{
				inform ("  IO close exception: " + e );
			}
			inform ("  *** success ***");
			return true;
		}
		else
		{
			return false;
		}
	}   //  testHostPort

	/**
	 *  Test JDBC
	 */
	private boolean testJDBC (String host, int port, String sid, String uid, String pwd)
	{
		try
		{
			inform ("  Registering Driver: oracle.jdbc.driver.OracleDriver");
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			inform ("  - driver registered");
			DriverManager.setLoginTimeout(5);
			DriverManager.setLogWriter(new PrintWriter(System.out));
			inform ("  - driver initialized");
		}
		catch (SQLException e)
		{
			inform ("ERROR: " + e.getMessage());
			return false;
		}

		boolean ok = false;

		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
		try
		{
			inform ("  Trying Client connection URL=" + urlC + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlC, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
			ok = true;
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		String urlS = "jdbc:oracle:oci8:@";
		try
		{
			inform ("  Trying Server connection URL=" + urlS + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlS, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		return ok;
	}   //  testJDBC

	/**
	 *  Test Compiere Users
	 */
	private void testCompiereUsers(String host, int port)
	{
		String sql = "SELECT Name, Password FROM AD_User WHERE IsActive='Y'";
		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + m_sid;
		try
		{
			inform ("  - Client connection URL=" + urlC + ", User=" + m_uid);
			Connection con = DriverManager.getConnection(urlC, m_uid, m_pwd);
			inform ("  - connected");
			Statement stmt = con.createStatement();
			inform ("  - statement created");
			ResultSet rs = stmt.executeQuery(sql);
			inform ("  - query executed listing active application users:");
			while (rs.next())
			{
				String user = rs.getString(1);
				String password = rs.getString(2);
				String answer = ">>  User = " + user;
				if ((user.equals("System") || user.equals("SuperUser")) && password.equals("System"))
					answer += "  with standard password (should be changed)";
				inform (answer);
			}
			rs.close();
			inform ("  - query closed");
			stmt.close();
			inform ("  - statement closed");
			con.close();
			inform ("  - connection closed");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}
	}   //  testCompiereUsers
}   //  ALoginTest
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
import java.io.*;
import java.net.*;
import java.sql.*;

import org.compiere.plaf.*;

/**
 *  Connection Test
 *  ** Not Translated **
 *
 *  @author Jorg Janke
 *  @version  $Id: ALoginTest.java,v 1.4 2001/11/12 01:19:07 jjanke Exp $
 */
public class ALoginTest extends JDialog implements ActionListener, Runnable
{
	/**
	 *  Consatructor
	 */
	public ALoginTest (Dialog frame, String host, String sid, String port, String uid, String pwd)
	{
		super (frame, "Connect Test: " + host, true);
		m_host = host;
		m_sid = sid;
		m_port = port;
		m_uid = uid;
		m_pwd = pwd;
		try
		{
			jbInit();
			pack();
		}
		catch(Exception ex)
		{
			inform ("Internal Error = " + ex.getMessage());
		}
		//  Start Tests
		try
		{
			m_worker = new Thread(this);
			m_worker.start();
		}
		catch (Exception e1)
		{
			inform ("Internal Error = " + e1);
		}
		AEnv.showCenterScreen(this);
	}   //  ALoginTest

	private String      m_host;
	private String      m_port;
	private String      m_sid;
	private String      m_uid;
	private String      m_pwd;
	private Thread      m_worker;

	private JPanel mainPanel = new JPanel();
	private BorderLayout mainLayout = new BorderLayout();
	private JPanel southPanel = new JPanel();
	private JButton bOK = new JButton();
	private JScrollPane infoPane = new JScrollPane();
	private JTextArea info = new JTextArea();
	private FlowLayout southLayout = new FlowLayout();

	/**
	 *  Static Layout
	 */
	void jbInit() throws Exception
	{
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		mainPanel.setLayout(mainLayout);
		bOK.setText("Exit");
		bOK.addActionListener(this);
		info.setBackground(CompierePLAF.getFieldBackground_Inactive());
		southPanel.setLayout(southLayout);
		southLayout.setAlignment(FlowLayout.RIGHT);
		infoPane.setPreferredSize(new Dimension(400, 400));
		getContentPane().add(mainPanel);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		southPanel.add(bOK, null);
		mainPanel.add(infoPane, BorderLayout.CENTER);
		infoPane.getViewport().add(info, null);
	}   //  jbInit

	/**
	 *  Inform
	 */
	private void inform (String text)
	{
		System.out.println(text);
		info.append(text);
		info.append("\n");
		info.setCaretPosition(info.getText().length());
	}   //  inform

	/**
	 *  Action Listener
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (e.getSource() == bOK)
		{
			while (m_worker != null && m_worker.isAlive())
				m_worker.interrupt();
			dispose();
		}
	}   //  actionEvent

	/**
	 *  Run individual tests
	 */
	public void run ()
	{
		String vmName = System.getProperty("java.vm.name");
		String vmVersion = System.getProperty("java.vm.version");
		inform ("Using Java=" + vmName + " " + vmVersion);
		inform ("");
		//
		boolean found = false;
		boolean foundJDBC = false;
		inform("*** Testing connection to Server: " + m_host + " ***");
		if (m_host == null || m_host.length() == 0)
		{
			inform ("ERROR: invalid host name");
			return;
		}
		String host = m_host;
		inform("Trying Echo - Port 7");
		found = testHostPort(host, 7);

		inform("Trying FTP - Port 21");
		if (testHostPort (host, 21) && !found)
			found = true;

		inform("Trying HTTP - Port 80");
		if (testHostPort (host, 80) && !found)
			found = true;

		inform("Trying Kerberos - Port 88");
		if (testHostPort (host, 88) && !found)
			found = true;

		inform("Trying NetBios Session - Port 139");
		if (testHostPort (host, 139) && !found)
			found = true;

		inform("Trying RMI - Port 1099");
		if (testHostPort (host, 1099) && !found)
			found = true;

		inform("Trying Oracle Connection Manager - Port 1630");
		if (testHostPort (host, 1630) && !found)
			found = true;

		inform("Trying Oracle JDBC - TCP Port 1521");
		foundJDBC = testHostPort (host, 1521);

		int jdbcPort = 0;
		try
		{
			jdbcPort = Integer.parseInt(m_port);
		}
		catch (Exception e)
		{
			inform ("ERROR: Cannot parse port=" + m_port);
			inform (e.getMessage());
			return;
		}
		if (jdbcPort != 1521)
		{
			inform("Trying Oracle JDBC - TCP Port " + jdbcPort);
			if (testHostPort (host, jdbcPort) && !foundJDBC)
				foundJDBC = true;
		}

		//  Test Interrupt
		if (m_worker != null && m_worker.interrupted())
			return;

		/** Info        */

		if (found && foundJDBC)
		{
			inform ("*** Server found: " + host + " ***");
			inform ("");
		}
		else if (!found && foundJDBC)
		{
			inform ("*** Server found: " + host + " (JDBC only) ***");
			inform ("");
		}
		else if (found && !foundJDBC)
		{
			inform ("ERROR: Server found: " + host + " - but no JDBC ***");
			inform ("Make sure that the Oracle Listener process is active");
			return;
		}
		else
		{
			inform ("ERROR: Server NOT found: " + host + "***");
			inform ("End Test: Make sure that you can ping the Server");
			return;
		}

		/*********************************************************************/

		inform ("Connect to SID: " + m_sid);

		inform ("Connect with entered parameters");
		if (!testJDBC(host, jdbcPort, m_sid, m_uid, m_pwd))
		{
			if (m_worker != null && m_worker.interrupted())
				return;

			if (jdbcPort != 1521)
			{
				inform ("Connect with standard JDBC port 1521");
				if (testJDBC(host, 1521, m_sid, m_uid, m_pwd))
				{
					inform ("Please set port to 1521");
					return;
				}
				if (m_worker != null && m_worker.interrupted())
					return;
			}

			inform ("Connect with user system/manager");
			if (testJDBC(host, 1521, m_sid, "system", "manager"))
			{
				inform ("Please check COMPIERE user id and password");
				inform (".... and please change SYSTEM password");
				return;
			}
		}

		inform ("*** Compiere database found: " + host + ":" + jdbcPort + ":" +  m_sid + " ***");
		if (m_worker != null && m_worker.interrupted())
			return;

		/*********************************************************************/

		inform ("");
		inform ("Testing available application users:");
		testCompiereUsers(host, jdbcPort);

		inform ("");
		inform ("*** Test complete **");
	}   //  run

	/**
	 *  Test Host Port
	 */
	private boolean testHostPort (String host, int port)
	{
		Socket pingSocket = null;
		try
		{
			/* Resolve address.     */
			InetAddress server = InetAddress.getByName(host);
			/* Establish socket.    */
			pingSocket = new Socket(server, port);
		}
		catch (UnknownHostException e)
		{
			inform ("  Unknown Host: " + e );
		}
		catch (IOException io )
		{
			inform ("  IO Exception: " + io );
		}

		if (pingSocket != null)
		{
			try
			{
				pingSocket.close();
			}
			catch (IOException e)
			{
				inform ("  IO close exception: " + e );
			}
			inform ("  *** success ***");
			return true;
		}
		else
		{
			return false;
		}
	}   //  testHostPort

	/**
	 *  Test JDBC
	 */
	private boolean testJDBC (String host, int port, String sid, String uid, String pwd)
	{
		try
		{
			inform ("  Registering Driver: oracle.jdbc.driver.OracleDriver");
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			inform ("  - driver registered");
			DriverManager.setLoginTimeout(5);
			DriverManager.setLogWriter(new PrintWriter(System.out));
			inform ("  - driver initialized");
		}
		catch (SQLException e)
		{
			inform ("ERROR: " + e.getMessage());
			return false;
		}

		boolean ok = false;

		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
		try
		{
			inform ("  Trying Client connection URL=" + urlC + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlC, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
			ok = true;
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		String urlS = "jdbc:oracle:oci8:@";
		try
		{
			inform ("  Trying Server connection URL=" + urlS + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlS, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		return ok;
	}   //  testJDBC

	/**
	 *  Test Compiere Users
	 */
	private void testCompiereUsers(String host, int port)
	{
		String sql = "SELECT Name, Password FROM AD_User WHERE IsActive='Y'";
		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + m_sid;
		try
		{
			inform ("  - Client connection URL=" + urlC + ", User=" + m_uid);
			Connection con = DriverManager.getConnection(urlC, m_uid, m_pwd);
			inform ("  - connected");
			Statement stmt = con.createStatement();
			inform ("  - statement created");
			ResultSet rs = stmt.executeQuery(sql);
			inform ("  - query executed listing active application users:");
			while (rs.next())
			{
				String user = rs.getString(1);
				String password = rs.getString(2);
				String answer = ">>  User = " + user;
				if ((user.equals("System") || user.equals("SuperUser")) && password.equals("System"))
					answer += "  with standard password (should be changed)";
				inform (answer);
			}
			rs.close();
			inform ("  - query closed");
			stmt.close();
			inform ("  - statement closed");
			con.close();
			inform ("  - connection closed");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}
	}   //  testCompiereUsers
}   //  ALoginTest
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
import java.io.*;
import java.net.*;
import java.sql.*;

import org.compiere.plaf.*;

/**
 *  Connection Test
 *  ** Not Translated **
 *
 *  @author Jorg Janke
 *  @version  $Id: ALoginTest.java,v 1.5 2003/09/29 01:04:42 jjanke Exp $
 */
public class ALoginTest extends JDialog implements ActionListener, Runnable
{
	/**
	 *  Consatructor
	 */
	public ALoginTest (Dialog frame, String host, String sid, String port, String uid, String pwd)
	{
		super (frame, "Connect Test: " + host, true);
		m_host = host;
		m_sid = sid;
		m_port = port;
		m_uid = uid;
		m_pwd = pwd;
		try
		{
			jbInit();
			pack();
		}
		catch(Exception ex)
		{
			inform ("Internal Error = " + ex.getMessage());
		}
		//  Start Tests
		try
		{
			m_worker = new Thread(this);
			m_worker.start();
		}
		catch (Exception e1)
		{
			inform ("Internal Error = " + e1);
		}
		AEnv.showCenterScreen(this);
	}   //  ALoginTest

	private String      m_host;
	private String      m_port;
	private String      m_sid;
	private String      m_uid;
	private String      m_pwd;
	private Thread      m_worker;

	private JPanel mainPanel = new JPanel();
	private BorderLayout mainLayout = new BorderLayout();
	private JPanel southPanel = new JPanel();
	private JButton bOK = new JButton();
	private JScrollPane infoPane = new JScrollPane();
	private JTextArea info = new JTextArea();
	private FlowLayout southLayout = new FlowLayout();

	/**
	 *  Static Layout
	 */
	void jbInit() throws Exception
	{
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		mainPanel.setLayout(mainLayout);
		bOK.setText("Exit");
		bOK.addActionListener(this);
		info.setBackground(CompierePLAF.getFieldBackground_Inactive());
		southPanel.setLayout(southLayout);
		southLayout.setAlignment(FlowLayout.RIGHT);
		infoPane.setPreferredSize(new Dimension(400, 400));
		getContentPane().add(mainPanel);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		southPanel.add(bOK, null);
		mainPanel.add(infoPane, BorderLayout.CENTER);
		infoPane.getViewport().add(info, null);
	}   //  jbInit

	/**
	 *  Inform
	 */
	private void inform (String text)
	{
		System.out.println(text);
		info.append(text);
		info.append("\n");
		info.setCaretPosition(info.getText().length());
	}   //  inform

	/**
	 *  Action Listener
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (e.getSource() == bOK)
		{
			while (m_worker != null && m_worker.isAlive())
				m_worker.interrupt();
			dispose();
		}
	}   //  actionEvent

	/**
	 *  Run individual tests
	 */
	public void run ()
	{
		String vmName = System.getProperty("java.vm.name");
		String vmVersion = System.getProperty("java.vm.version");
		inform ("Using Java=" + vmName + " " + vmVersion);
		inform ("");
		//
		boolean found = false;
		boolean foundJDBC = false;
		inform("*** Testing connection to Server: " + m_host + " ***");
		if (m_host == null || m_host.length() == 0)
		{
			inform ("ERROR: invalid host name");
			return;
		}
		String host = m_host;
		inform("Trying Echo - Port 7");
		found = testHostPort(host, 7);

		inform("Trying FTP - Port 21");
		if (testHostPort (host, 21) && !found)
			found = true;

		inform("Trying HTTP - Port 80");
		if (testHostPort (host, 80) && !found)
			found = true;

		inform("Trying Kerberos - Port 88");
		if (testHostPort (host, 88) && !found)
			found = true;

		inform("Trying NetBios Session - Port 139");
		if (testHostPort (host, 139) && !found)
			found = true;

		inform("Trying RMI - Port 1099");
		if (testHostPort (host, 1099) && !found)
			found = true;

		inform("Trying Oracle Connection Manager - Port 1630");
		if (testHostPort (host, 1630) && !found)
			found = true;

		inform("Trying Oracle JDBC - TCP Port 1521");
		foundJDBC = testHostPort (host, 1521);

		int jdbcPort = 0;
		try
		{
			jdbcPort = Integer.parseInt(m_port);
		}
		catch (Exception e)
		{
			inform ("ERROR: Cannot parse port=" + m_port);
			inform (e.getMessage());
			return;
		}
		if (jdbcPort != 1521)
		{
			inform("Trying Oracle JDBC - TCP Port " + jdbcPort);
			if (testHostPort (host, jdbcPort) && !foundJDBC)
				foundJDBC = true;
		}

		//  Test Interrupt
		if (m_worker != null && m_worker.isInterrupted())
			return;

		/** Info        */

		if (found && foundJDBC)
		{
			inform ("*** Server found: " + host + " ***");
			inform ("");
		}
		else if (!found && foundJDBC)
		{
			inform ("*** Server found: " + host + " (JDBC only) ***");
			inform ("");
		}
		else if (found && !foundJDBC)
		{
			inform ("ERROR: Server found: " + host + " - but no JDBC ***");
			inform ("Make sure that the Oracle Listener process is active");
			return;
		}
		else
		{
			inform ("ERROR: Server NOT found: " + host + "***");
			inform ("End Test: Make sure that you can ping the Server");
			return;
		}

		/*********************************************************************/

		inform ("Connect to SID: " + m_sid);

		inform ("Connect with entered parameters");
		if (!testJDBC(host, jdbcPort, m_sid, m_uid, m_pwd))
		{
			if (m_worker != null && m_worker.isInterrupted())
				return;

			if (jdbcPort != 1521)
			{
				inform ("Connect with standard JDBC port 1521");
				if (testJDBC(host, 1521, m_sid, m_uid, m_pwd))
				{
					inform ("Please set port to 1521");
					return;
				}
				if (m_worker != null && m_worker.isInterrupted())
					return;
			}

			inform ("Connect with user system/manager");
			if (testJDBC(host, 1521, m_sid, "system", "manager"))
			{
				inform ("Please check COMPIERE user id and password");
				inform (".... and please change SYSTEM password");
				return;
			}
		}

		inform ("*** Compiere database found: " + host + ":" + jdbcPort + ":" +  m_sid + " ***");
		if (m_worker != null && m_worker.isInterrupted())
			return;

		/*********************************************************************/

		inform ("");
		inform ("Testing available application users:");
		testCompiereUsers(host, jdbcPort);

		inform ("");
		inform ("*** Test complete **");
	}   //  run

	/**
	 *  Test Host Port
	 */
	private boolean testHostPort (String host, int port)
	{
		Socket pingSocket = null;
		try
		{
			/* Resolve address.     */
			InetAddress server = InetAddress.getByName(host);
			/* Establish socket.    */
			pingSocket = new Socket(server, port);
		}
		catch (UnknownHostException e)
		{
			inform ("  Unknown Host: " + e );
		}
		catch (IOException io )
		{
			inform ("  IO Exception: " + io );
		}

		if (pingSocket != null)
		{
			try
			{
				pingSocket.close();
			}
			catch (IOException e)
			{
				inform ("  IO close exception: " + e );
			}
			inform ("  *** success ***");
			return true;
		}
		else
		{
			return false;
		}
	}   //  testHostPort

	/**
	 *  Test JDBC
	 */
	private boolean testJDBC (String host, int port, String sid, String uid, String pwd)
	{
		try
		{
			inform ("  Registering Driver: oracle.jdbc.driver.OracleDriver");
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			inform ("  - driver registered");
			DriverManager.setLoginTimeout(5);
			DriverManager.setLogWriter(new PrintWriter(System.out));
			inform ("  - driver initialized");
		}
		catch (SQLException e)
		{
			inform ("ERROR: " + e.getMessage());
			return false;
		}

		boolean ok = false;

		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
		try
		{
			inform ("  Trying Client connection URL=" + urlC + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlC, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
			ok = true;
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		String urlS = "jdbc:oracle:oci8:@";
		try
		{
			inform ("  Trying Server connection URL=" + urlS + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlS, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		return ok;
	}   //  testJDBC

	/**
	 *  Test Compiere Users
	 */
	private void testCompiereUsers(String host, int port)
	{
		String sql = "SELECT Name, Password FROM AD_User WHERE IsActive='Y'";
		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + m_sid;
		try
		{
			inform ("  - Client connection URL=" + urlC + ", User=" + m_uid);
			Connection con = DriverManager.getConnection(urlC, m_uid, m_pwd);
			inform ("  - connected");
			Statement stmt = con.createStatement();
			inform ("  - statement created");
			ResultSet rs = stmt.executeQuery(sql);
			inform ("  - query executed listing active application users:");
			while (rs.next())
			{
				String user = rs.getString(1);
				String password = rs.getString(2);
				String answer = ">>  User = " + user;
				if ((user.equals("System") || user.equals("SuperUser")) && password.equals("System"))
					answer += "  with standard password (should be changed)";
				inform (answer);
			}
			rs.close();
			inform ("  - query closed");
			stmt.close();
			inform ("  - statement closed");
			con.close();
			inform ("  - connection closed");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}
	}   //  testCompiereUsers
}   //  ALoginTest
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
import java.io.*;
import java.net.*;
import java.sql.*;

import org.compiere.plaf.*;

/**
 *  Connection Test
 *  ** Not Translated **
 *
 *  @author Jorg Janke
 *  @version  $Id: ALoginTest.java,v 1.4 2001/11/12 01:19:07 jjanke Exp $
 */
public class ALoginTest extends JDialog implements ActionListener, Runnable
{
	/**
	 *  Consatructor
	 */
	public ALoginTest (Dialog frame, String host, String sid, String port, String uid, String pwd)
	{
		super (frame, "Connect Test: " + host, true);
		m_host = host;
		m_sid = sid;
		m_port = port;
		m_uid = uid;
		m_pwd = pwd;
		try
		{
			jbInit();
			pack();
		}
		catch(Exception ex)
		{
			inform ("Internal Error = " + ex.getMessage());
		}
		//  Start Tests
		try
		{
			m_worker = new Thread(this);
			m_worker.start();
		}
		catch (Exception e1)
		{
			inform ("Internal Error = " + e1);
		}
		AEnv.showCenterScreen(this);
	}   //  ALoginTest

	private String      m_host;
	private String      m_port;
	private String      m_sid;
	private String      m_uid;
	private String      m_pwd;
	private Thread      m_worker;

	private JPanel mainPanel = new JPanel();
	private BorderLayout mainLayout = new BorderLayout();
	private JPanel southPanel = new JPanel();
	private JButton bOK = new JButton();
	private JScrollPane infoPane = new JScrollPane();
	private JTextArea info = new JTextArea();
	private FlowLayout southLayout = new FlowLayout();

	/**
	 *  Static Layout
	 */
	void jbInit() throws Exception
	{
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		mainPanel.setLayout(mainLayout);
		bOK.setText("Exit");
		bOK.addActionListener(this);
		info.setBackground(CompierePLAF.getFieldBackground_Inactive());
		southPanel.setLayout(southLayout);
		southLayout.setAlignment(FlowLayout.RIGHT);
		infoPane.setPreferredSize(new Dimension(400, 400));
		getContentPane().add(mainPanel);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		southPanel.add(bOK, null);
		mainPanel.add(infoPane, BorderLayout.CENTER);
		infoPane.getViewport().add(info, null);
	}   //  jbInit

	/**
	 *  Inform
	 */
	private void inform (String text)
	{
		System.out.println(text);
		info.append(text);
		info.append("\n");
		info.setCaretPosition(info.getText().length());
	}   //  inform

	/**
	 *  Action Listener
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (e.getSource() == bOK)
		{
			while (m_worker != null && m_worker.isAlive())
				m_worker.interrupt();
			dispose();
		}
	}   //  actionEvent

	/**
	 *  Run individual tests
	 */
	public void run ()
	{
		String vmName = System.getProperty("java.vm.name");
		String vmVersion = System.getProperty("java.vm.version");
		inform ("Using Java=" + vmName + " " + vmVersion);
		inform ("");
		//
		boolean found = false;
		boolean foundJDBC = false;
		inform("*** Testing connection to Server: " + m_host + " ***");
		if (m_host == null || m_host.length() == 0)
		{
			inform ("ERROR: invalid host name");
			return;
		}
		String host = m_host;
		inform("Trying Echo - Port 7");
		found = testHostPort(host, 7);

		inform("Trying FTP - Port 21");
		if (testHostPort (host, 21) && !found)
			found = true;

		inform("Trying HTTP - Port 80");
		if (testHostPort (host, 80) && !found)
			found = true;

		inform("Trying Kerberos - Port 88");
		if (testHostPort (host, 88) && !found)
			found = true;

		inform("Trying NetBios Session - Port 139");
		if (testHostPort (host, 139) && !found)
			found = true;

		inform("Trying RMI - Port 1099");
		if (testHostPort (host, 1099) && !found)
			found = true;

		inform("Trying Oracle Connection Manager - Port 1630");
		if (testHostPort (host, 1630) && !found)
			found = true;

		inform("Trying Oracle JDBC - TCP Port 1521");
		foundJDBC = testHostPort (host, 1521);

		int jdbcPort = 0;
		try
		{
			jdbcPort = Integer.parseInt(m_port);
		}
		catch (Exception e)
		{
			inform ("ERROR: Cannot parse port=" + m_port);
			inform (e.getMessage());
			return;
		}
		if (jdbcPort != 1521)
		{
			inform("Trying Oracle JDBC - TCP Port " + jdbcPort);
			if (testHostPort (host, jdbcPort) && !foundJDBC)
				foundJDBC = true;
		}

		//  Test Interrupt
		if (m_worker != null && m_worker.interrupted())
			return;

		/** Info        */

		if (found && foundJDBC)
		{
			inform ("*** Server found: " + host + " ***");
			inform ("");
		}
		else if (!found && foundJDBC)
		{
			inform ("*** Server found: " + host + " (JDBC only) ***");
			inform ("");
		}
		else if (found && !foundJDBC)
		{
			inform ("ERROR: Server found: " + host + " - but no JDBC ***");
			inform ("Make sure that the Oracle Listener process is active");
			return;
		}
		else
		{
			inform ("ERROR: Server NOT found: " + host + "***");
			inform ("End Test: Make sure that you can ping the Server");
			return;
		}

		/*********************************************************************/

		inform ("Connect to SID: " + m_sid);

		inform ("Connect with entered parameters");
		if (!testJDBC(host, jdbcPort, m_sid, m_uid, m_pwd))
		{
			if (m_worker != null && m_worker.interrupted())
				return;

			if (jdbcPort != 1521)
			{
				inform ("Connect with standard JDBC port 1521");
				if (testJDBC(host, 1521, m_sid, m_uid, m_pwd))
				{
					inform ("Please set port to 1521");
					return;
				}
				if (m_worker != null && m_worker.interrupted())
					return;
			}

			inform ("Connect with user system/manager");
			if (testJDBC(host, 1521, m_sid, "system", "manager"))
			{
				inform ("Please check COMPIERE user id and password");
				inform (".... and please change SYSTEM password");
				return;
			}
		}

		inform ("*** Compiere database found: " + host + ":" + jdbcPort + ":" +  m_sid + " ***");
		if (m_worker != null && m_worker.interrupted())
			return;

		/*********************************************************************/

		inform ("");
		inform ("Testing available application users:");
		testCompiereUsers(host, jdbcPort);

		inform ("");
		inform ("*** Test complete **");
	}   //  run

	/**
	 *  Test Host Port
	 */
	private boolean testHostPort (String host, int port)
	{
		Socket pingSocket = null;
		try
		{
			/* Resolve address.     */
			InetAddress server = InetAddress.getByName(host);
			/* Establish socket.    */
			pingSocket = new Socket(server, port);
		}
		catch (UnknownHostException e)
		{
			inform ("  Unknown Host: " + e );
		}
		catch (IOException io )
		{
			inform ("  IO Exception: " + io );
		}

		if (pingSocket != null)
		{
			try
			{
				pingSocket.close();
			}
			catch (IOException e)
			{
				inform ("  IO close exception: " + e );
			}
			inform ("  *** success ***");
			return true;
		}
		else
		{
			return false;
		}
	}   //  testHostPort

	/**
	 *  Test JDBC
	 */
	private boolean testJDBC (String host, int port, String sid, String uid, String pwd)
	{
		try
		{
			inform ("  Registering Driver: oracle.jdbc.driver.OracleDriver");
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			inform ("  - driver registered");
			DriverManager.setLoginTimeout(5);
			DriverManager.setLogWriter(new PrintWriter(System.out));
			inform ("  - driver initialized");
		}
		catch (SQLException e)
		{
			inform ("ERROR: " + e.getMessage());
			return false;
		}

		boolean ok = false;

		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
		try
		{
			inform ("  Trying Client connection URL=" + urlC + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlC, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
			ok = true;
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		String urlS = "jdbc:oracle:oci8:@";
		try
		{
			inform ("  Trying Server connection URL=" + urlS + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlS, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		return ok;
	}   //  testJDBC

	/**
	 *  Test Compiere Users
	 */
	private void testCompiereUsers(String host, int port)
	{
		String sql = "SELECT Name, Password FROM AD_User WHERE IsActive='Y'";
		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + m_sid;
		try
		{
			inform ("  - Client connection URL=" + urlC + ", User=" + m_uid);
			Connection con = DriverManager.getConnection(urlC, m_uid, m_pwd);
			inform ("  - connected");
			Statement stmt = con.createStatement();
			inform ("  - statement created");
			ResultSet rs = stmt.executeQuery(sql);
			inform ("  - query executed listing active application users:");
			while (rs.next())
			{
				String user = rs.getString(1);
				String password = rs.getString(2);
				String answer = ">>  User = " + user;
				if ((user.equals("System") || user.equals("SuperUser")) && password.equals("System"))
					answer += "  with standard password (should be changed)";
				inform (answer);
			}
			rs.close();
			inform ("  - query closed");
			stmt.close();
			inform ("  - statement closed");
			con.close();
			inform ("  - connection closed");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}
	}   //  testCompiereUsers
}   //  ALoginTest
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
import java.io.*;
import java.net.*;
import java.sql.*;

import org.compiere.plaf.*;

/**
 *  Connection Test
 *  ** Not Translated **
 *
 *  @author Jorg Janke
 *  @version  $Id: ALoginTest.java,v 1.5 2003/09/29 01:04:42 jjanke Exp $
 */
public class ALoginTest extends JDialog implements ActionListener, Runnable
{
	/**
	 *  Consatructor
	 */
	public ALoginTest (Dialog frame, String host, String sid, String port, String uid, String pwd)
	{
		super (frame, "Connect Test: " + host, true);
		m_host = host;
		m_sid = sid;
		m_port = port;
		m_uid = uid;
		m_pwd = pwd;
		try
		{
			jbInit();
			pack();
		}
		catch(Exception ex)
		{
			inform ("Internal Error = " + ex.getMessage());
		}
		//  Start Tests
		try
		{
			m_worker = new Thread(this);
			m_worker.start();
		}
		catch (Exception e1)
		{
			inform ("Internal Error = " + e1);
		}
		AEnv.showCenterScreen(this);
	}   //  ALoginTest

	private String      m_host;
	private String      m_port;
	private String      m_sid;
	private String      m_uid;
	private String      m_pwd;
	private Thread      m_worker;

	private JPanel mainPanel = new JPanel();
	private BorderLayout mainLayout = new BorderLayout();
	private JPanel southPanel = new JPanel();
	private JButton bOK = new JButton();
	private JScrollPane infoPane = new JScrollPane();
	private JTextArea info = new JTextArea();
	private FlowLayout southLayout = new FlowLayout();

	/**
	 *  Static Layout
	 */
	void jbInit() throws Exception
	{
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		mainPanel.setLayout(mainLayout);
		bOK.setText("Exit");
		bOK.addActionListener(this);
		info.setBackground(CompierePLAF.getFieldBackground_Inactive());
		southPanel.setLayout(southLayout);
		southLayout.setAlignment(FlowLayout.RIGHT);
		infoPane.setPreferredSize(new Dimension(400, 400));
		getContentPane().add(mainPanel);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		southPanel.add(bOK, null);
		mainPanel.add(infoPane, BorderLayout.CENTER);
		infoPane.getViewport().add(info, null);
	}   //  jbInit

	/**
	 *  Inform
	 */
	private void inform (String text)
	{
		System.out.println(text);
		info.append(text);
		info.append("\n");
		info.setCaretPosition(info.getText().length());
	}   //  inform

	/**
	 *  Action Listener
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (e.getSource() == bOK)
		{
			while (m_worker != null && m_worker.isAlive())
				m_worker.interrupt();
			dispose();
		}
	}   //  actionEvent

	/**
	 *  Run individual tests
	 */
	public void run ()
	{
		String vmName = System.getProperty("java.vm.name");
		String vmVersion = System.getProperty("java.vm.version");
		inform ("Using Java=" + vmName + " " + vmVersion);
		inform ("");
		//
		boolean found = false;
		boolean foundJDBC = false;
		inform("*** Testing connection to Server: " + m_host + " ***");
		if (m_host == null || m_host.length() == 0)
		{
			inform ("ERROR: invalid host name");
			return;
		}
		String host = m_host;
		inform("Trying Echo - Port 7");
		found = testHostPort(host, 7);

		inform("Trying FTP - Port 21");
		if (testHostPort (host, 21) && !found)
			found = true;

		inform("Trying HTTP - Port 80");
		if (testHostPort (host, 80) && !found)
			found = true;

		inform("Trying Kerberos - Port 88");
		if (testHostPort (host, 88) && !found)
			found = true;

		inform("Trying NetBios Session - Port 139");
		if (testHostPort (host, 139) && !found)
			found = true;

		inform("Trying RMI - Port 1099");
		if (testHostPort (host, 1099) && !found)
			found = true;

		inform("Trying Oracle Connection Manager - Port 1630");
		if (testHostPort (host, 1630) && !found)
			found = true;

		inform("Trying Oracle JDBC - TCP Port 1521");
		foundJDBC = testHostPort (host, 1521);

		int jdbcPort = 0;
		try
		{
			jdbcPort = Integer.parseInt(m_port);
		}
		catch (Exception e)
		{
			inform ("ERROR: Cannot parse port=" + m_port);
			inform (e.getMessage());
			return;
		}
		if (jdbcPort != 1521)
		{
			inform("Trying Oracle JDBC - TCP Port " + jdbcPort);
			if (testHostPort (host, jdbcPort) && !foundJDBC)
				foundJDBC = true;
		}

		//  Test Interrupt
		if (m_worker != null && m_worker.isInterrupted())
			return;

		/** Info        */

		if (found && foundJDBC)
		{
			inform ("*** Server found: " + host + " ***");
			inform ("");
		}
		else if (!found && foundJDBC)
		{
			inform ("*** Server found: " + host + " (JDBC only) ***");
			inform ("");
		}
		else if (found && !foundJDBC)
		{
			inform ("ERROR: Server found: " + host + " - but no JDBC ***");
			inform ("Make sure that the Oracle Listener process is active");
			return;
		}
		else
		{
			inform ("ERROR: Server NOT found: " + host + "***");
			inform ("End Test: Make sure that you can ping the Server");
			return;
		}

		/*********************************************************************/

		inform ("Connect to SID: " + m_sid);

		inform ("Connect with entered parameters");
		if (!testJDBC(host, jdbcPort, m_sid, m_uid, m_pwd))
		{
			if (m_worker != null && m_worker.isInterrupted())
				return;

			if (jdbcPort != 1521)
			{
				inform ("Connect with standard JDBC port 1521");
				if (testJDBC(host, 1521, m_sid, m_uid, m_pwd))
				{
					inform ("Please set port to 1521");
					return;
				}
				if (m_worker != null && m_worker.isInterrupted())
					return;
			}

			inform ("Connect with user system/manager");
			if (testJDBC(host, 1521, m_sid, "system", "manager"))
			{
				inform ("Please check COMPIERE user id and password");
				inform (".... and please change SYSTEM password");
				return;
			}
		}

		inform ("*** Compiere database found: " + host + ":" + jdbcPort + ":" +  m_sid + " ***");
		if (m_worker != null && m_worker.isInterrupted())
			return;

		/*********************************************************************/

		inform ("");
		inform ("Testing available application users:");
		testCompiereUsers(host, jdbcPort);

		inform ("");
		inform ("*** Test complete **");
	}   //  run

	/**
	 *  Test Host Port
	 */
	private boolean testHostPort (String host, int port)
	{
		Socket pingSocket = null;
		try
		{
			/* Resolve address.     */
			InetAddress server = InetAddress.getByName(host);
			/* Establish socket.    */
			pingSocket = new Socket(server, port);
		}
		catch (UnknownHostException e)
		{
			inform ("  Unknown Host: " + e );
		}
		catch (IOException io )
		{
			inform ("  IO Exception: " + io );
		}

		if (pingSocket != null)
		{
			try
			{
				pingSocket.close();
			}
			catch (IOException e)
			{
				inform ("  IO close exception: " + e );
			}
			inform ("  *** success ***");
			return true;
		}
		else
		{
			return false;
		}
	}   //  testHostPort

	/**
	 *  Test JDBC
	 */
	private boolean testJDBC (String host, int port, String sid, String uid, String pwd)
	{
		try
		{
			inform ("  Registering Driver: oracle.jdbc.driver.OracleDriver");
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			inform ("  - driver registered");
			DriverManager.setLoginTimeout(5);
			DriverManager.setLogWriter(new PrintWriter(System.out));
			inform ("  - driver initialized");
		}
		catch (SQLException e)
		{
			inform ("ERROR: " + e.getMessage());
			return false;
		}

		boolean ok = false;

		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
		try
		{
			inform ("  Trying Client connection URL=" + urlC + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlC, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
			ok = true;
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		String urlS = "jdbc:oracle:oci8:@";
		try
		{
			inform ("  Trying Server connection URL=" + urlS + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlS, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		return ok;
	}   //  testJDBC

	/**
	 *  Test Compiere Users
	 */
	private void testCompiereUsers(String host, int port)
	{
		String sql = "SELECT Name, Password FROM AD_User WHERE IsActive='Y'";
		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + m_sid;
		try
		{
			inform ("  - Client connection URL=" + urlC + ", User=" + m_uid);
			Connection con = DriverManager.getConnection(urlC, m_uid, m_pwd);
			inform ("  - connected");
			Statement stmt = con.createStatement();
			inform ("  - statement created");
			ResultSet rs = stmt.executeQuery(sql);
			inform ("  - query executed listing active application users:");
			while (rs.next())
			{
				String user = rs.getString(1);
				String password = rs.getString(2);
				String answer = ">>  User = " + user;
				if ((user.equals("System") || user.equals("SuperUser")) && password.equals("System"))
					answer += "  with standard password (should be changed)";
				inform (answer);
			}
			rs.close();
			inform ("  - query closed");
			stmt.close();
			inform ("  - statement closed");
			con.close();
			inform ("  - connection closed");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}
	}   //  testCompiereUsers
}   //  ALoginTest
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
import java.io.*;
import java.net.*;
import java.sql.*;

import org.compiere.plaf.*;

/**
 *  Connection Test
 *  ** Not Translated **
 *
 *  @author Jorg Janke
 *  @version  $Id: ALoginTest.java,v 1.4 2001/11/12 01:19:07 jjanke Exp $
 */
public class ALoginTest extends JDialog implements ActionListener, Runnable
{
	/**
	 *  Consatructor
	 */
	public ALoginTest (Dialog frame, String host, String sid, String port, String uid, String pwd)
	{
		super (frame, "Connect Test: " + host, true);
		m_host = host;
		m_sid = sid;
		m_port = port;
		m_uid = uid;
		m_pwd = pwd;
		try
		{
			jbInit();
			pack();
		}
		catch(Exception ex)
		{
			inform ("Internal Error = " + ex.getMessage());
		}
		//  Start Tests
		try
		{
			m_worker = new Thread(this);
			m_worker.start();
		}
		catch (Exception e1)
		{
			inform ("Internal Error = " + e1);
		}
		AEnv.showCenterScreen(this);
	}   //  ALoginTest

	private String      m_host;
	private String      m_port;
	private String      m_sid;
	private String      m_uid;
	private String      m_pwd;
	private Thread      m_worker;

	private JPanel mainPanel = new JPanel();
	private BorderLayout mainLayout = new BorderLayout();
	private JPanel southPanel = new JPanel();
	private JButton bOK = new JButton();
	private JScrollPane infoPane = new JScrollPane();
	private JTextArea info = new JTextArea();
	private FlowLayout southLayout = new FlowLayout();

	/**
	 *  Static Layout
	 */
	void jbInit() throws Exception
	{
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		mainPanel.setLayout(mainLayout);
		bOK.setText("Exit");
		bOK.addActionListener(this);
		info.setBackground(CompierePLAF.getFieldBackground_Inactive());
		southPanel.setLayout(southLayout);
		southLayout.setAlignment(FlowLayout.RIGHT);
		infoPane.setPreferredSize(new Dimension(400, 400));
		getContentPane().add(mainPanel);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		southPanel.add(bOK, null);
		mainPanel.add(infoPane, BorderLayout.CENTER);
		infoPane.getViewport().add(info, null);
	}   //  jbInit

	/**
	 *  Inform
	 */
	private void inform (String text)
	{
		System.out.println(text);
		info.append(text);
		info.append("\n");
		info.setCaretPosition(info.getText().length());
	}   //  inform

	/**
	 *  Action Listener
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (e.getSource() == bOK)
		{
			while (m_worker != null && m_worker.isAlive())
				m_worker.interrupt();
			dispose();
		}
	}   //  actionEvent

	/**
	 *  Run individual tests
	 */
	public void run ()
	{
		String vmName = System.getProperty("java.vm.name");
		String vmVersion = System.getProperty("java.vm.version");
		inform ("Using Java=" + vmName + " " + vmVersion);
		inform ("");
		//
		boolean found = false;
		boolean foundJDBC = false;
		inform("*** Testing connection to Server: " + m_host + " ***");
		if (m_host == null || m_host.length() == 0)
		{
			inform ("ERROR: invalid host name");
			return;
		}
		String host = m_host;
		inform("Trying Echo - Port 7");
		found = testHostPort(host, 7);

		inform("Trying FTP - Port 21");
		if (testHostPort (host, 21) && !found)
			found = true;

		inform("Trying HTTP - Port 80");
		if (testHostPort (host, 80) && !found)
			found = true;

		inform("Trying Kerberos - Port 88");
		if (testHostPort (host, 88) && !found)
			found = true;

		inform("Trying NetBios Session - Port 139");
		if (testHostPort (host, 139) && !found)
			found = true;

		inform("Trying RMI - Port 1099");
		if (testHostPort (host, 1099) && !found)
			found = true;

		inform("Trying Oracle Connection Manager - Port 1630");
		if (testHostPort (host, 1630) && !found)
			found = true;

		inform("Trying Oracle JDBC - TCP Port 1521");
		foundJDBC = testHostPort (host, 1521);

		int jdbcPort = 0;
		try
		{
			jdbcPort = Integer.parseInt(m_port);
		}
		catch (Exception e)
		{
			inform ("ERROR: Cannot parse port=" + m_port);
			inform (e.getMessage());
			return;
		}
		if (jdbcPort != 1521)
		{
			inform("Trying Oracle JDBC - TCP Port " + jdbcPort);
			if (testHostPort (host, jdbcPort) && !foundJDBC)
				foundJDBC = true;
		}

		//  Test Interrupt
		if (m_worker != null && m_worker.interrupted())
			return;

		/** Info        */

		if (found && foundJDBC)
		{
			inform ("*** Server found: " + host + " ***");
			inform ("");
		}
		else if (!found && foundJDBC)
		{
			inform ("*** Server found: " + host + " (JDBC only) ***");
			inform ("");
		}
		else if (found && !foundJDBC)
		{
			inform ("ERROR: Server found: " + host + " - but no JDBC ***");
			inform ("Make sure that the Oracle Listener process is active");
			return;
		}
		else
		{
			inform ("ERROR: Server NOT found: " + host + "***");
			inform ("End Test: Make sure that you can ping the Server");
			return;
		}

		/*********************************************************************/

		inform ("Connect to SID: " + m_sid);

		inform ("Connect with entered parameters");
		if (!testJDBC(host, jdbcPort, m_sid, m_uid, m_pwd))
		{
			if (m_worker != null && m_worker.interrupted())
				return;

			if (jdbcPort != 1521)
			{
				inform ("Connect with standard JDBC port 1521");
				if (testJDBC(host, 1521, m_sid, m_uid, m_pwd))
				{
					inform ("Please set port to 1521");
					return;
				}
				if (m_worker != null && m_worker.interrupted())
					return;
			}

			inform ("Connect with user system/manager");
			if (testJDBC(host, 1521, m_sid, "system", "manager"))
			{
				inform ("Please check COMPIERE user id and password");
				inform (".... and please change SYSTEM password");
				return;
			}
		}

		inform ("*** Compiere database found: " + host + ":" + jdbcPort + ":" +  m_sid + " ***");
		if (m_worker != null && m_worker.interrupted())
			return;

		/*********************************************************************/

		inform ("");
		inform ("Testing available application users:");
		testCompiereUsers(host, jdbcPort);

		inform ("");
		inform ("*** Test complete **");
	}   //  run

	/**
	 *  Test Host Port
	 */
	private boolean testHostPort (String host, int port)
	{
		Socket pingSocket = null;
		try
		{
			/* Resolve address.     */
			InetAddress server = InetAddress.getByName(host);
			/* Establish socket.    */
			pingSocket = new Socket(server, port);
		}
		catch (UnknownHostException e)
		{
			inform ("  Unknown Host: " + e );
		}
		catch (IOException io )
		{
			inform ("  IO Exception: " + io );
		}

		if (pingSocket != null)
		{
			try
			{
				pingSocket.close();
			}
			catch (IOException e)
			{
				inform ("  IO close exception: " + e );
			}
			inform ("  *** success ***");
			return true;
		}
		else
		{
			return false;
		}
	}   //  testHostPort

	/**
	 *  Test JDBC
	 */
	private boolean testJDBC (String host, int port, String sid, String uid, String pwd)
	{
		try
		{
			inform ("  Registering Driver: oracle.jdbc.driver.OracleDriver");
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			inform ("  - driver registered");
			DriverManager.setLoginTimeout(5);
			DriverManager.setLogWriter(new PrintWriter(System.out));
			inform ("  - driver initialized");
		}
		catch (SQLException e)
		{
			inform ("ERROR: " + e.getMessage());
			return false;
		}

		boolean ok = false;

		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
		try
		{
			inform ("  Trying Client connection URL=" + urlC + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlC, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
			ok = true;
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		String urlS = "jdbc:oracle:oci8:@";
		try
		{
			inform ("  Trying Server connection URL=" + urlS + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlS, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		return ok;
	}   //  testJDBC

	/**
	 *  Test Compiere Users
	 */
	private void testCompiereUsers(String host, int port)
	{
		String sql = "SELECT Name, Password FROM AD_User WHERE IsActive='Y'";
		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + m_sid;
		try
		{
			inform ("  - Client connection URL=" + urlC + ", User=" + m_uid);
			Connection con = DriverManager.getConnection(urlC, m_uid, m_pwd);
			inform ("  - connected");
			Statement stmt = con.createStatement();
			inform ("  - statement created");
			ResultSet rs = stmt.executeQuery(sql);
			inform ("  - query executed listing active application users:");
			while (rs.next())
			{
				String user = rs.getString(1);
				String password = rs.getString(2);
				String answer = ">>  User = " + user;
				if ((user.equals("System") || user.equals("SuperUser")) && password.equals("System"))
					answer += "  with standard password (should be changed)";
				inform (answer);
			}
			rs.close();
			inform ("  - query closed");
			stmt.close();
			inform ("  - statement closed");
			con.close();
			inform ("  - connection closed");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}
	}   //  testCompiereUsers
}   //  ALoginTest
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
import java.io.*;
import java.net.*;
import java.sql.*;

import org.compiere.plaf.*;

/**
 *  Connection Test
 *  ** Not Translated **
 *
 *  @author Jorg Janke
 *  @version  $Id: ALoginTest.java,v 1.5 2003/09/29 01:04:42 jjanke Exp $
 */
public class ALoginTest extends JDialog implements ActionListener, Runnable
{
	/**
	 *  Consatructor
	 */
	public ALoginTest (Dialog frame, String host, String sid, String port, String uid, String pwd)
	{
		super (frame, "Connect Test: " + host, true);
		m_host = host;
		m_sid = sid;
		m_port = port;
		m_uid = uid;
		m_pwd = pwd;
		try
		{
			jbInit();
			pack();
		}
		catch(Exception ex)
		{
			inform ("Internal Error = " + ex.getMessage());
		}
		//  Start Tests
		try
		{
			m_worker = new Thread(this);
			m_worker.start();
		}
		catch (Exception e1)
		{
			inform ("Internal Error = " + e1);
		}
		AEnv.showCenterScreen(this);
	}   //  ALoginTest

	private String      m_host;
	private String      m_port;
	private String      m_sid;
	private String      m_uid;
	private String      m_pwd;
	private Thread      m_worker;

	private JPanel mainPanel = new JPanel();
	private BorderLayout mainLayout = new BorderLayout();
	private JPanel southPanel = new JPanel();
	private JButton bOK = new JButton();
	private JScrollPane infoPane = new JScrollPane();
	private JTextArea info = new JTextArea();
	private FlowLayout southLayout = new FlowLayout();

	/**
	 *  Static Layout
	 */
	void jbInit() throws Exception
	{
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		mainPanel.setLayout(mainLayout);
		bOK.setText("Exit");
		bOK.addActionListener(this);
		info.setBackground(CompierePLAF.getFieldBackground_Inactive());
		southPanel.setLayout(southLayout);
		southLayout.setAlignment(FlowLayout.RIGHT);
		infoPane.setPreferredSize(new Dimension(400, 400));
		getContentPane().add(mainPanel);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		southPanel.add(bOK, null);
		mainPanel.add(infoPane, BorderLayout.CENTER);
		infoPane.getViewport().add(info, null);
	}   //  jbInit

	/**
	 *  Inform
	 */
	private void inform (String text)
	{
		System.out.println(text);
		info.append(text);
		info.append("\n");
		info.setCaretPosition(info.getText().length());
	}   //  inform

	/**
	 *  Action Listener
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (e.getSource() == bOK)
		{
			while (m_worker != null && m_worker.isAlive())
				m_worker.interrupt();
			dispose();
		}
	}   //  actionEvent

	/**
	 *  Run individual tests
	 */
	public void run ()
	{
		String vmName = System.getProperty("java.vm.name");
		String vmVersion = System.getProperty("java.vm.version");
		inform ("Using Java=" + vmName + " " + vmVersion);
		inform ("");
		//
		boolean found = false;
		boolean foundJDBC = false;
		inform("*** Testing connection to Server: " + m_host + " ***");
		if (m_host == null || m_host.length() == 0)
		{
			inform ("ERROR: invalid host name");
			return;
		}
		String host = m_host;
		inform("Trying Echo - Port 7");
		found = testHostPort(host, 7);

		inform("Trying FTP - Port 21");
		if (testHostPort (host, 21) && !found)
			found = true;

		inform("Trying HTTP - Port 80");
		if (testHostPort (host, 80) && !found)
			found = true;

		inform("Trying Kerberos - Port 88");
		if (testHostPort (host, 88) && !found)
			found = true;

		inform("Trying NetBios Session - Port 139");
		if (testHostPort (host, 139) && !found)
			found = true;

		inform("Trying RMI - Port 1099");
		if (testHostPort (host, 1099) && !found)
			found = true;

		inform("Trying Oracle Connection Manager - Port 1630");
		if (testHostPort (host, 1630) && !found)
			found = true;

		inform("Trying Oracle JDBC - TCP Port 1521");
		foundJDBC = testHostPort (host, 1521);

		int jdbcPort = 0;
		try
		{
			jdbcPort = Integer.parseInt(m_port);
		}
		catch (Exception e)
		{
			inform ("ERROR: Cannot parse port=" + m_port);
			inform (e.getMessage());
			return;
		}
		if (jdbcPort != 1521)
		{
			inform("Trying Oracle JDBC - TCP Port " + jdbcPort);
			if (testHostPort (host, jdbcPort) && !foundJDBC)
				foundJDBC = true;
		}

		//  Test Interrupt
		if (m_worker != null && m_worker.isInterrupted())
			return;

		/** Info        */

		if (found && foundJDBC)
		{
			inform ("*** Server found: " + host + " ***");
			inform ("");
		}
		else if (!found && foundJDBC)
		{
			inform ("*** Server found: " + host + " (JDBC only) ***");
			inform ("");
		}
		else if (found && !foundJDBC)
		{
			inform ("ERROR: Server found: " + host + " - but no JDBC ***");
			inform ("Make sure that the Oracle Listener process is active");
			return;
		}
		else
		{
			inform ("ERROR: Server NOT found: " + host + "***");
			inform ("End Test: Make sure that you can ping the Server");
			return;
		}

		/*********************************************************************/

		inform ("Connect to SID: " + m_sid);

		inform ("Connect with entered parameters");
		if (!testJDBC(host, jdbcPort, m_sid, m_uid, m_pwd))
		{
			if (m_worker != null && m_worker.isInterrupted())
				return;

			if (jdbcPort != 1521)
			{
				inform ("Connect with standard JDBC port 1521");
				if (testJDBC(host, 1521, m_sid, m_uid, m_pwd))
				{
					inform ("Please set port to 1521");
					return;
				}
				if (m_worker != null && m_worker.isInterrupted())
					return;
			}

			inform ("Connect with user system/manager");
			if (testJDBC(host, 1521, m_sid, "system", "manager"))
			{
				inform ("Please check COMPIERE user id and password");
				inform (".... and please change SYSTEM password");
				return;
			}
		}

		inform ("*** Compiere database found: " + host + ":" + jdbcPort + ":" +  m_sid + " ***");
		if (m_worker != null && m_worker.isInterrupted())
			return;

		/*********************************************************************/

		inform ("");
		inform ("Testing available application users:");
		testCompiereUsers(host, jdbcPort);

		inform ("");
		inform ("*** Test complete **");
	}   //  run

	/**
	 *  Test Host Port
	 */
	private boolean testHostPort (String host, int port)
	{
		Socket pingSocket = null;
		try
		{
			/* Resolve address.     */
			InetAddress server = InetAddress.getByName(host);
			/* Establish socket.    */
			pingSocket = new Socket(server, port);
		}
		catch (UnknownHostException e)
		{
			inform ("  Unknown Host: " + e );
		}
		catch (IOException io )
		{
			inform ("  IO Exception: " + io );
		}

		if (pingSocket != null)
		{
			try
			{
				pingSocket.close();
			}
			catch (IOException e)
			{
				inform ("  IO close exception: " + e );
			}
			inform ("  *** success ***");
			return true;
		}
		else
		{
			return false;
		}
	}   //  testHostPort

	/**
	 *  Test JDBC
	 */
	private boolean testJDBC (String host, int port, String sid, String uid, String pwd)
	{
		try
		{
			inform ("  Registering Driver: oracle.jdbc.driver.OracleDriver");
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			inform ("  - driver registered");
			DriverManager.setLoginTimeout(5);
			DriverManager.setLogWriter(new PrintWriter(System.out));
			inform ("  - driver initialized");
		}
		catch (SQLException e)
		{
			inform ("ERROR: " + e.getMessage());
			return false;
		}

		boolean ok = false;

		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
		try
		{
			inform ("  Trying Client connection URL=" + urlC + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlC, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
			ok = true;
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		String urlS = "jdbc:oracle:oci8:@";
		try
		{
			inform ("  Trying Server connection URL=" + urlS + ", User=" + uid);
			Connection con = DriverManager.getConnection(urlS, uid, pwd);
			inform ("  - connected");
			//
			DatabaseMetaData conMD = con.getMetaData();
			inform("  - Driver Name:\t"    + conMD.getDriverName());
			inform("  - Driver Version:\t" + conMD.getDriverVersion());
			inform("  - DB Name:\t" + conMD.getDatabaseProductName());
			inform("  - DB Version:\t" + conMD.getDatabaseProductVersion());
			//
			con.close();
			inform ("  *** success ***");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}

		return ok;
	}   //  testJDBC

	/**
	 *  Test Compiere Users
	 */
	private void testCompiereUsers(String host, int port)
	{
		String sql = "SELECT Name, Password FROM AD_User WHERE IsActive='Y'";
		String urlC = "jdbc:oracle:thin:@" + host + ":" + port + ":" + m_sid;
		try
		{
			inform ("  - Client connection URL=" + urlC + ", User=" + m_uid);
			Connection con = DriverManager.getConnection(urlC, m_uid, m_pwd);
			inform ("  - connected");
			Statement stmt = con.createStatement();
			inform ("  - statement created");
			ResultSet rs = stmt.executeQuery(sql);
			inform ("  - query executed listing active application users:");
			while (rs.next())
			{
				String user = rs.getString(1);
				String password = rs.getString(2);
				String answer = ">>  User = " + user;
				if ((user.equals("System") || user.equals("SuperUser")) && password.equals("System"))
					answer += "  with standard password (should be changed)";
				inform (answer);
			}
			rs.close();
			inform ("  - query closed");
			stmt.close();
			inform ("  - statement closed");
			con.close();
			inform ("  - connection closed");
		}
		catch (SQLException e)
		{
			inform ("  ERROR: " + e.getMessage());
		}
	}   //  testCompiereUsers
}   //  ALoginTest
