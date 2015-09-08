/* Copyright (c) 1995-2000, The Hypersonic SQL Group.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the Hypersonic SQL Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HYPERSONIC SQL GROUP,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many individuals 
 * on behalf of the Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2005, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import org.hsqldb.lib.java.JavaSystem;

//dmarshall@users - 20020101 - original swing port of DatabaseManager
//sqlbob@users 20020401 - patch 537501 by ulrivo - commandline arguments
//sqlbob@users 20020407 - patch 1.7.0 - reengineering and enhancements
//nickferguson@users 20021005 - patch 1.7.1 - enhancements
//deccles@users 20040412 - patch 933671 - various bug fixes
//deccles@users 2004xxxx - enhancements
//weconsultants@users 20041109 - version 1.8.0 - reengineering and enhancements:
//              Added: Goodies 'Look and Feel'.
//              Added: a Font Changer(Font Type\Style).
//              Added: a Color Changer (foreground\bckground).
//              Added: RowCounts for each JTree table nodes.
//              Added: OneTouchExpandable attribute to JSplitPanes.
//              Moved: setFramePositon code to a CommonSwing.setFramePositon() Method.
//              Added: call to new method to handle exception processing (CommonSwing.errorMessage());
//              Added: Added a new pane added at the bottom of the Frame. (Status Icon and StatusLine).
//              Added: 2 Methods (setStatusMessage()), one overrides the other. One to change the ruung status
//                              another to allow a message to be posted without changing the Status Icon if needed.
//              Added: Added a customCursor for the current wait cursor
//      Added: Ability to switch the current LAF while runing (Native,Java or Motif)
//unsaved@users 2005xxxx - improvements and bug fixes

/**
 * Swing Tool for managing a JDBC database.<p>
 * <pre>
 *             Usage: java DatabaseManagerSwing [--options]
 *             where options include:
 *              --driver <classname>  jdbc driver class
 *              --url <name>          jdbc url
 *              --user <name>         username used for connection
 *              --password <password> password for this user
 *              --dir <path>          default directory
 *              --script <file>       reads from script file
 *              --urlid <urlid>       get connection info from RC file
 *              --rcfile <file>       use instead of default (with urlid)
 *              --noexit              Don't exit JVM
 * </pre>
 *
 * Note that the sys-table switch will not work for Oracle, because Oracle
 * does not categorize their system tables correctly in the JDBC Metadata.
 *
 * New class based on Hypersonic SQL original
 *
 * @author dmarshall@users
 * @version 1.8.0
 * @since 1.7.0
 */
public class DatabaseManagerSwing extends JApplet
implements ActionListener, WindowListener, KeyListener {

    /*
     * This is down here because it is an  implementation note, not a
     * Javadoc comment!
     * Tue Apr 26 16:38:54 EDT 2005
     * Switched default switch method from "-switch" to "--switch" because
     * "-switch" usage is ambiguous as used here.  Single switches should
     * be reserved for single-letter switches which can be mixed like
     * "-u -r -l" = "-url".  -blaine
     */
    private static final String DEFAULT_RCFILE =
        System.getProperty("user.home") + "/dbmanager.rc";
    private static final String HELP_TEXT =
        "See the forums, mailing lists, and HSQLDB User Guide\n"
        + "at http://hsqldb.org.\n\n"
        + "Please paste the following version identifier with any\n"
        + "problem reports or help requests:  $Revision: 1.57 $";
    private static final String ABOUT_TEXT =
        "$Revision: 1.57 $ of DatabaseManagerSwing\n\n"
        + "Copyright (c) 1995-2000, The Hypersonic SQL Group.\n"
        + "Copyright (c) 2001-2005, The HSQL Development Group.\n"
        + "http://hsqldb.org\n\n\n"
        + "You may use and redistribute according to the HSQLDB\n"
        + "license documented in the source code and at the web\n"
        + "site above.";
    static final String    NL         = System.getProperty("line.separator");
    static final String    NULL_STR   = "[null]";
    static int             iMaxRecent = 24;
    Connection             cConn;
    Connection             rowConn;        // holds the connetion for getting table row counts
    DatabaseMetaData       dMeta;
    Statement              sStatement;
    JMenu                  mRecent;
    String[]               sRecent;
    int                    iRecent;
    JTextArea              txtCommand;
    JScrollPane            txtCommandScroll;
    JButton                butExecute;
    JTree                  tTree;
    JScrollPane            tScrollPane;
    DefaultTreeModel       treeModel;
    TableModel             tableModel;
    DefaultMutableTreeNode rootNode;
    JPanel                 pResult;
    long                   lTime;
    GridSwing              gResult;
    JTable                 gResultTable;
    JScrollPane            gScrollPane;
    JTextArea              txtResult;
    JScrollPane            txtResultScroll;
    JSplitPane             nsSplitPane;    // Contains query over results
    JSplitPane             ewSplitPane;    // Contains tree beside nsSplitPane
    boolean                bHelp;
    JFrame                 fMain;
    static boolean         bMustExit;
    String                 ifHuge = "";
    JToolBar               jtoolbar;
    private boolean        showSchemas  = true;
    private boolean        showTooltips = true;
    private boolean        autoRefresh  = true;
    private boolean        gridFormat   = true;

    // Added: (weconsultants@users)
    static DatabaseManagerSwing refForFontDialogSwing;
    boolean                     displayRowCounts = false;
    boolean                     showSys          = false;
    boolean                     showIndexDetails = true;
    String                      currentLAF       = null;
    JPanel                      pStatus;
    static JButton              iReadyStatus;
    JRadioButtonMenuItem        rbAllSchemas = new JRadioButtonMenuItem("*");
    JMenuItem                   mitemAbout   = new JMenuItem("About", 'A');
    JMenuItem                   mitemHelp    = new JMenuItem("Help", 'H');
    JMenuItem mitemUpdateSchemas = new JMenuItem("Update Schemas");
    JCheckBoxMenuItem boxAutoCommit =
        new JCheckBoxMenuItem(AUTOCOMMIT_BOX_TEXT);
    JCheckBoxMenuItem boxLogging = new JCheckBoxMenuItem(LOGGING_BOX_TEXT);
    JCheckBoxMenuItem boxShowSchemas =
        new JCheckBoxMenuItem(SHOWSCHEMAS_BOX_TEXT);
    JCheckBoxMenuItem boxAutoRefresh =
        new JCheckBoxMenuItem(AUTOREFRESH_BOX_TEXT);
    JCheckBoxMenuItem boxTooltips = new JCheckBoxMenuItem(SHOWTIPS_BOX_TEXT);
    JCheckBoxMenuItem boxRowCounts =
        new JCheckBoxMenuItem(ROWCOUNTS_BOX_TEXT);
    JCheckBoxMenuItem boxShowGrid = new JCheckBoxMenuItem(GRID_BOX_TEXT);
    JCheckBoxMenuItem boxShowSys  = new JCheckBoxMenuItem(SHOWSYS_BOX_TEXT);

    // Consider adding GTK and Plaf L&Fs.
    JRadioButtonMenuItem rbNativeLF =
        new JRadioButtonMenuItem("Native Look & Feel");
    JRadioButtonMenuItem rbJavaLF =
        new JRadioButtonMenuItem("Java Look & Feel");
    JRadioButtonMenuItem rbMotifLF =
        new JRadioButtonMenuItem("Motif Look & Feel");
    JLabel                      jStatusLine;
    static String               READY_STATUS         = "Ready";
    private static final String AUTOCOMMIT_BOX_TEXT  = "Autocommit mode";
    private static final String LOGGING_BOX_TEXT     = "Logging mode";
    private static final String SHOWSCHEMAS_BOX_TEXT = "Show schemas";
    private static final String AUTOREFRESH_BOX_TEXT = "Auto-refresh tree";
    private static final String SHOWTIPS_BOX_TEXT    = "Show Tooltips";
    private static final String ROWCOUNTS_BOX_TEXT   = "Show row counts";
    private static final String SHOWSYS_BOX_TEXT     = "Show system tables";
    private static final String GRID_BOX_TEXT =
        "Show results in Grid (a.o.t. Text)";

    // variables to hold the default cursors for these top level swing objects
    // so we can restore them when we exit our thread
    Cursor        fMainCursor;
    Cursor        txtCommandCursor;
    Cursor        txtResultCursor;
    HashMap       tipMap     = new HashMap();
    private JMenu mnuSchemas = new JMenu("Schemas");

    /**
     * Wait Cursor
     */

    // Changed: (weconsultants@users): commonted out the, out of the box, cursor to use a custom cursor
    private final Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);

    //getToolkit().createCustomCursor(CommonSwing.getIcon("SystemCursor"),
    //                                new Point(4, 4), "HourGlass cursor");
    // (ulrivo): variables set by arguments from the commandline
    static String  defDriver   = "org.hsqldb.jdbcDriver";
    static String  defURL      = "jdbc:hsqldb:.";
    static String  defUser     = "sa";
    static String  defPassword = "";
    static String  defScript;
    static String  defDirectory;
    private String schemaFilter = null;

    public void init() {

        DatabaseManagerSwing m = new DatabaseManagerSwing();

        m.main();

        try {

            // The connection dialog will be used before the
            // DatabaseManager window is drawn.
            connect(ConnectionDialogSwing.createConnection(defDriver, defURL,
                    defUser, defPassword));
            m.setWaiting("Initializing");
            m.insertTestData();
            m.updateAutoCommitBox();
        } catch (Exception e) {

            //  Added: (weconsultants@users)
            CommonSwing.errorMessage(e);
        } finally {
            setWaiting(null);
        }
    }

    public static void main(String[] arg) {

        System.getProperties().put("sun.java2d.noddraw", "true");

        // (ulrivo): read all arguments from the command line
        String  lowerArg;
        String  urlid        = null;
        String  rcFile       = null;
        boolean autoConnect  = false;
        boolean urlidConnect = false;

        bMustExit = true;

        for (int i = 0; i < arg.length; i++) {
            lowerArg = arg[i].toLowerCase();

            if (lowerArg.length() > 1 && lowerArg.charAt(1) == '-') {
                lowerArg = lowerArg.substring(1);
            }

            i++;

            if (lowerArg.equals("-driver")) {
                defDriver   = arg[i];
                autoConnect = true;
            } else if (lowerArg.equals("-url")) {
                defURL      = arg[i];
                autoConnect = true;
            } else if (lowerArg.equals("-user")) {
                defUser     = arg[i];
                autoConnect = true;
            } else if (lowerArg.equals("-password")) {
                defPassword = arg[i];
                autoConnect = true;
            } else if (lowerArg.equals("-urlid")) {
                urlid        = arg[i];
                urlidConnect = true;
            } else if (lowerArg.equals("-rcfile")) {
                rcFile       = arg[i];
                urlidConnect = true;
            } else if (lowerArg.equals("-dir")) {
                defDirectory = arg[i];
            } else if (lowerArg.equals("-script")) {
                defScript = arg[i];
            } else if (lowerArg.equals("-noexit")) {
                bMustExit = false;

                i--;
            } else {
                showUsage();

                return;
            }
        }

        DatabaseManagerSwing m = new DatabaseManagerSwing();

        // Added: (weconsultants@users): Need databaseManagerSwing for later Reference
        refForFontDialogSwing = m;

        m.main();

        Connection c = null;

        m.setWaiting("Initializing");

        try {
            if (autoConnect && urlidConnect) {
                throw new IllegalArgumentException(
                    "You may not specify both (urlid) AND (url/user/password).");
            }

            if (autoConnect) {
                c = ConnectionDialogSwing.createConnection(defDriver, defURL,
                        defUser, defPassword);
            } else if (urlidConnect) {
                if (urlid == null) {
                    throw new IllegalArgumentException(
                        "You must specify an 'urlid' to use an RC file");
                }

                autoConnect = true;

                String rcfilepath = (rcFile == null) ? DEFAULT_RCFILE
                                                     : rcFile;
                RCData rcdata     = new RCData(new File(rcfilepath), urlid);

                c = rcdata.getConnection(
                    null, System.getProperty("sqlfile.charset"),
                    System.getProperty("javax.net.ssl.trustStore"));
            } else {
                c = ConnectionDialogSwing.createConnection(m.fMain,
                        "Connect");
            }
        } catch (Exception e) {

            //  Added: (weconsultants@users)
            CommonSwing.errorMessage(e);
        } finally {
            m.setWaiting(null);
        }

        if (c == null) {
            return;
        }

        // Added: (weconsultants@users): For preloadng FontDialogSwing
        FontDialogSwing.CreatFontDialog(refForFontDialogSwing);
        m.connect(c);
    }

    /**
     * This stuff is all quick, except for the refreshTree().
     * This unit can be kicked off in main Gui thread.  The refreshTree
     * will be backgrounded and this method will return.
     */
    public void connect(Connection c) {

        schemaFilter = null;

        if (c == null) {
            return;
        }

        if (cConn != null) {
            try {
                cConn.close();
            } catch (SQLException e) {

                //  Added: (weconsultants@users)
                CommonSwing.errorMessage(e);
            }
        }

        cConn = c;

        // Added: (weconsultants@users) Need to barrow to get the table rowcounts
        rowConn = c;

        try {
            dMeta      = cConn.getMetaData();
            sStatement = cConn.createStatement();

            updateAutoCommitBox();

            // Workaround for EXTREME SLOWNESS getting this info from O.
            showIndexDetails =
                (dMeta.getDatabaseProductName().indexOf("Oracle") < 0);

            refreshTree();
        } catch (SQLException e) {

            //  Added: (weconsultants@users)
            CommonSwing.errorMessage(e);
        }
    }

    private static void showUsage() {

        System.out.println(
            "Usage: java DatabaseManagerSwing [--options]\n"
            + "where options include:\n"
            + "    --driver <classname>  jdbc driver class\n"
            + "    --url <name>          jdbc url\n"
            + "    --user <name>         username used for connection\n"
            + "    --password <password> password for this user\n"
            + "    --urlid <urlid>       use url/user/password/driver in rc file\n"
            + "    --rcfile <file>       (defaults to 'dbmanager.rc' in home dir)\n"
            + "    --dir <path>          default directory\n"
            + "    --script <file>       reads from script file\n"
            + "    --noexit              do not call system.exit()\n"
            + "(Single-hypen switches like '-driver' are also supported)");
    }

    private void insertTestData() {

        try {
            DatabaseManagerCommon.createTestTables(sStatement);
            txtCommand.setText(
                DatabaseManagerCommon.createTestData(sStatement));

            for (int i = 0; i < DatabaseManagerCommon.testDataSql.length;
                    i++) {
                addToRecent(DatabaseManagerCommon.testDataSql[i]);
            }

            executeCurrentSQL();
        } catch (SQLException e) {

            //  Added: (weconsultants@users)
            CommonSwing.errorMessage(e);
        }
    }

    // Comment: (weconsultants@users) this boolean does not get referenced..?
    public void setMustExit(boolean b) {
        this.bMustExit = b;
    }

    private DBMPrefs prefs = null;

    public void main() {

        fMain = new JFrame("HSQL Database Manager");

        try {
            prefs = new DBMPrefs();
        } catch (Exception e) {

            // Just don't user persisted preferences.
            prefs = null;
        }

        if (prefs == null) {
            setLF(CommonSwing.Native);
        } else {
            autoRefresh      = prefs.autoRefresh;
            displayRowCounts = prefs.showRowCounts;
            showSys          = prefs.showSysTables;
            showSchemas      = prefs.showSchemas;
            gridFormat       = prefs.resultGrid;
            showTooltips     = prefs.showTooltips;

            setLF(prefs.laf);
        }

        // (ulrivo): An actual icon.  N.b., this adds some tips to the tip map
        fMain.getContentPane().add(createToolBar(), "North");
        fMain.setIconImage(CommonSwing.getIcon("Frame"));
        fMain.addWindowListener(this);

        JMenuBar bar = new JMenuBar();

        // used shortcuts: CERGTSIUDOLM
        String[] fitems = {
            "-Connect...", "--", "OOpen Script...", "-Save Script...",
            "-Save Result...", "--", "-Exit"
        };

        addMenu(bar, "File", fitems);

        Object[] vitems = {
            "RRefresh Tree", boxAutoRefresh, "--", boxRowCounts, boxShowSys,
            boxShowSchemas, boxShowGrid
        };

        addMenu(bar, "View", vitems);

        String[] sitems = {
            "SSELECT", "IINSERT", "UUPDATE", "DDELETE", "EEXECUTE", "---",
            "-CREATE TABLE", "-DROP TABLE", "-CREATE INDEX", "-DROP INDEX",
            "--", "-CHECKPOINT", "-SCRIPT", "-SET", "-SHUTDOWN", "--",
            "-Test Script"
        };

        addMenu(bar, "Command", sitems);

        mRecent = new JMenu("Recent");

        mRecent.setMnemonic(KeyEvent.VK_R);
        bar.add(mRecent);

        ButtonGroup lfGroup = new ButtonGroup();

        lfGroup.add(rbNativeLF);
        lfGroup.add(rbJavaLF);
        lfGroup.add(rbMotifLF);
        boxShowSchemas.setSelected(showSchemas);
        boxShowGrid.setSelected(gridFormat);
        boxTooltips.setSelected(showTooltips);
        boxShowGrid.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
                Event.CTRL_MASK));
        boxAutoRefresh.setSelected(autoRefresh);
        boxRowCounts.setSelected(displayRowCounts);
        boxShowSys.setSelected(showSys);
        rbNativeLF.setActionCommand("LFMODE:" + CommonSwing.Native);
        rbJavaLF.setActionCommand("LFMODE:" + CommonSwing.Java);
        rbMotifLF.setActionCommand("LFMODE:" + CommonSwing.Motif);
        tipMap.put(mitemUpdateSchemas,
                   "Refresh the schema list in this menu");
        tipMap.put(rbAllSchemas, "Display items in all schemas");
        tipMap.put(mitemAbout, "Display product information");
        tipMap.put(mitemHelp, "Display advice for obtaining help");
        tipMap.put(boxAutoRefresh,
                   "Refresh tree (and schema list) automatically"
                   + "when YOU modify database objects");
        tipMap.put(boxShowSchemas,
                   "Display object names in tree like schemaname.basename");
        tipMap.put(rbNativeLF,
                   "Set Look and Feel to Native for your platform");
        tipMap.put(rbJavaLF, "Set Look and Feel to Java");
        tipMap.put(rbMotifLF, "Set Look and Feel to Motif");
        boxTooltips.setToolTipText(
            "Display tooltips (hover text), like this");
        tipMap.put(boxAutoCommit,
                   "Shows current Auto-commit mode.  Click to change");
        tipMap.put(
            boxLogging,
            "Shows current JDBC DriverManager logging mode.  Click to change");
        tipMap.put(boxShowSys,
                   "Show system tables in table tree to the left");
        tipMap.put(boxShowGrid,
                   "Show query results in grid (in text if off)");
        tipMap.put(boxRowCounts, "Show row counts with table names in tree");
        boxAutoRefresh.setMnemonic(KeyEvent.VK_C);
        boxShowSchemas.setMnemonic(KeyEvent.VK_Y);
        boxAutoCommit.setMnemonic(KeyEvent.VK_A);
        boxShowSys.setMnemonic(KeyEvent.VK_Y);
        boxShowGrid.setMnemonic(KeyEvent.VK_G);
        boxRowCounts.setMnemonic(KeyEvent.VK_C);
        boxLogging.setMnemonic(KeyEvent.VK_L);
        rbAllSchemas.setMnemonic(KeyEvent.VK_ASTERISK);
        rbNativeLF.setMnemonic(KeyEvent.VK_N);
        rbJavaLF.setMnemonic(KeyEvent.VK_J);
        rbMotifLF.setMnemonic(KeyEvent.VK_M);
        mitemUpdateSchemas.setMnemonic(KeyEvent.VK_U);

        Object[] soptions = {

            // Added: (weconsultants@users) New menu options
            rbNativeLF, rbJavaLF, rbMotifLF, "--", "-Set Fonts", "--",
            boxAutoCommit, "CCommit", "LRollback", "--", "-Disable MaxRows",
            "-Set MaxRows to 100", "--", boxLogging, "--", "-Insert test data"
        };

        addMenu(bar, "Options", soptions);

        String[] stools = {
            "-Dump", "-Restore", "-Transfer"
        };

        addMenu(bar, "Tools", stools);
        mnuSchemas.setMnemonic(KeyEvent.VK_S);
        bar.add(mnuSchemas);

        JMenu mnuHelp = new JMenu("Help");

        mnuHelp.setMnemonic(KeyEvent.VK_H);
        mnuHelp.add(mitemAbout);
        mnuHelp.add(mitemHelp);
        mnuHelp.add(boxTooltips);
        rbAllSchemas.addActionListener(schemaListListener);

        // May be illegal:
        mitemUpdateSchemas.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent) {
                updateSchemaList();
            }
        });
        mitemHelp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent) {

                JOptionPane.showMessageDialog(
                    fMain.getContentPane(), HELP_TEXT, "HELP",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        mitemAbout.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent) {

                JOptionPane.showMessageDialog(
                    fMain.getContentPane(), ABOUT_TEXT, "About",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        boxTooltips.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent) {

                showTooltips = boxTooltips.isSelected();

                resetTooltips();
            }
        });
        bar.add(mnuHelp);
        fMain.setJMenuBar(bar);
        initGUI();

        sRecent = new String[iMaxRecent];

        // Modified: (weconsultants@users)Mode code to CommonSwing for general use
        CommonSwing.setFramePositon(fMain);

        // Modified: (weconsultants@users) Changed from deprecated show()
        fMain.setVisible(true);

        // (ulrivo): load query from command line
        if (defScript != null) {
            if (defDirectory != null) {
                defScript = defDirectory + File.separator + defScript;
            }

            // if insert stmet is thousands of records...skip showing it
            // as text.  Too huge.
            StringBuffer buf = new StringBuffer();

            ifHuge = DatabaseManagerCommon.readFile(defScript);

            if (4096 <= ifHuge.length()) {
                buf.append(
                    "This huge file cannot be edited. Please execute\n");
                txtCommand.setText(buf.toString());
            } else {
                txtCommand.setText(ifHuge);
            }
        }

        // This must be done AFTER all tip texts are put into the map
        resetTooltips();
        txtCommand.requestFocus();
    }

    private void addMenu(JMenuBar b, String name, Object[] items) {

        JMenu menu = new JMenu(name);

        menu.setMnemonic(name.charAt(0));
        addMenuItems(menu, items);
        b.add(menu);
    }

    private void addMenuItems(JMenu f, Object[] m) {

        /*
         * This method needs to be completely written or just
         * obliterated and we'll use the Menu objects directly.
         * Problem is, passing in Strings for menu elements makes it
         * extremely difficult to use non-text menu items (an important
         * part of a good Gui), hot-keys, mnemonic keys, tooltips.
         * Note the "trick" required here to set hot-keys.
         */
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        for (int i = 0; i < m.length; i++) {
            if (m[i].equals("--")) {
                f.addSeparator();
            } else if (m[i].equals("---")) {

                // (ulrivo): full size on screen with less than 640 width
                if (d.width >= 640) {
                    f.addSeparator();
                } else {
                    return;
                }
            } else {
                JMenuItem item;

                if (m[i] instanceof JMenuItem) {
                    item = (JMenuItem) m[i];
                } else if (m[i] instanceof String) {
                    item = new JMenuItem(((String) m[i]).substring(1));

                    char c = ((String) m[i]).charAt(0);

                    if (c != '-') {
                        KeyStroke key =
                            KeyStroke.getKeyStroke(c, Event.CTRL_MASK);

                        item.setAccelerator(key);
                    }
                } else {
                    throw new RuntimeException(
                        "Unexpected element for menu item creation: "
                        + m[i].getClass().getName());
                }

                item.addActionListener(this);
                f.add(item);
            }
        }
    }

    public void keyPressed(KeyEvent k) {}

    public void keyReleased(KeyEvent k) {}

    public void keyTyped(KeyEvent k) {

        if (k.getKeyChar() == '\n' && k.isControlDown()) {
            k.consume();
            executeCurrentSQL();
        }
    }

    Thread dummyThread = new Thread("dummy");

    public void actionPerformed(ActionEvent ev) {

        String s = ev.getActionCommand();

        if (s == null) {
            if (ev.getSource() instanceof JMenuItem) {
                JMenuItem i;

                s = ((JMenuItem) ev.getSource()).getText();
            }
        }

        if (s == null) {}
        else if (s.equals("Exit")) {
            windowClosing(null);
        } else if (s.equals("Transfer")) {
            Transfer.work(null);
        } else if (s.equals("Dump")) {
            Transfer.work(new String[]{ "-d" });
        } else if (s.equals("Restore")) {
            Transfer.work(new String[]{ "-r" });
        } else if (s.equals(LOGGING_BOX_TEXT)) {
            JavaSystem.setLogToSystem(boxLogging.isSelected());
        } else if (s.equals(AUTOREFRESH_BOX_TEXT)) {
            autoRefresh = boxAutoRefresh.isSelected();

            refreshTree();
        } else if (s.equals("Refresh Tree")) {
            refreshTree();
        } else if (s.startsWith("#")) {
            int i = Integer.parseInt(s.substring(1));

            txtCommand.setText(sRecent[i]);
        } else if (s.equals("Connect...")) {
            Connection newCon = null;

            try {
                setWaiting("Connecting");

                newCon = ConnectionDialogSwing.createConnection(fMain,
                        "Connect");
            } finally {
                setWaiting(null);
            }

            connect(newCon);
        } else if (s.equals(GRID_BOX_TEXT)) {
            gridFormat = boxShowGrid.isSelected();

            displayResults();
        } else if (s.equals("Open Script...")) {
            JFileChooser f = new JFileChooser(".");

            f.setDialogTitle("Open Script...");

            // (ulrivo): set default directory if set from command line
            if (defDirectory != null) {
                f.setCurrentDirectory(new File(defDirectory));
            }

            int option = f.showOpenDialog(fMain);

            if (option == JFileChooser.APPROVE_OPTION) {
                File file = f.getSelectedFile();

                if (file != null) {
                    StringBuffer buf = new StringBuffer();

                    ifHuge = DatabaseManagerCommon.readFile(
                        file.getAbsolutePath());

                    if (4096 <= ifHuge.length()) {
                        buf.append(
                            "This huge file cannot be edited. Please execute\n");
                        txtCommand.setText(buf.toString());
                    } else {
                        txtCommand.setText(ifHuge);
                    }
                }
            }
        } else if (s.equals("Save Script...")) {
            JFileChooser f = new JFileChooser(".");

            f.setDialogTitle("Save Script");

            // (ulrivo): set default directory if set from command line
            if (defDirectory != null) {
                f.setCurrentDirectory(new File(defDirectory));
            }

            int option = f.showSaveDialog(fMain);

            if (option == JFileChooser.APPROVE_OPTION) {
                File file = f.getSelectedFile();

                if (file != null) {
                    DatabaseManagerCommon.writeFile(file.getAbsolutePath(),
                                                    txtCommand.getText());
                }
            }
        } else if (s.equals("Save Result...")) {
            JFileChooser f = new JFileChooser(".");

            f.setDialogTitle("Save Result...");

            // (ulrivo): set default directory if set from command line
            if (defDirectory != null) {
                f.setCurrentDirectory(new File(defDirectory));
            }

            int option = f.showSaveDialog(fMain);

            if (option == JFileChooser.APPROVE_OPTION) {
                File file = f.getSelectedFile();

                if (file != null) {
                    showResultInText();
                    DatabaseManagerCommon.writeFile(file.getAbsolutePath(),
                                                    txtResult.getText());
                }
            }
        } else if (s.equals(SHOWSYS_BOX_TEXT)) {
            showSys = boxShowSys.isSelected();

            try {
                refreshTree();
            } catch (Exception e) {

                //  Added: (weconsultants@users)
                CommonSwing.errorMessage(e);
            }
        } else if (s.equals(ROWCOUNTS_BOX_TEXT)) {
            displayRowCounts = boxRowCounts.isSelected();

            try {
                refreshTree();
            } catch (Exception e) {

                //  Added: (weconsultants@users)
                CommonSwing.errorMessage(e);
            }
        } else if (s.startsWith("LFMODE:")) {
            setLF(s.substring("LFMODE:".length()));
        } else if (s.equals("Set Fonts")) {

            // Added: (weconsultants@users)
            FontDialogSwing.CreatFontDialog(refForFontDialogSwing);
        } else if (s.equals(AUTOCOMMIT_BOX_TEXT)) {
            try {
                cConn.setAutoCommit(boxAutoCommit.isSelected());
            } catch (SQLException e) {
                boxAutoCommit.setSelected(!boxAutoCommit.isSelected());

                //  Added: (weconsultants@users)
                CommonSwing.errorMessage(e);
            }
        } else if (s.equals("Commit")) {
            try {
                cConn.commit();
            } catch (SQLException e) {

                //  Added: (weconsultants@users)
                CommonSwing.errorMessage(e);
            }
        } else if (s.equals("Insert test data")) {
            insertTestData();
            refreshTree();
        } else if (s.equals("Rollback")) {
            try {
                cConn.rollback();
            } catch (SQLException e) {

                //  Added: (weconsultants@users)
                CommonSwing.errorMessage(e);
            }
        } else if (s.equals("Disable MaxRows")) {
            try {
                sStatement.setMaxRows(0);
            } catch (SQLException e) {

                //  Added: (weconsultants@users)
                CommonSwing.errorMessage(e);
            }
        } else if (s.equals("Set MaxRows to 100")) {
            try {
                sStatement.setMaxRows(100);
            } catch (SQLException e) {
                CommonSwing.errorMessage(e);
            }
        } else if (s.equals("SELECT")) {
            showHelp(DatabaseManagerCommon.selectHelp);
        } else if (s.equals("INSERT")) {
            showHelp(DatabaseManagerCommon.insertHelp);
        } else if (s.equals("UPDATE")) {
            showHelp(DatabaseManagerCommon.updateHelp);
        } else if (s.equals("DELETE")) {
            showHelp(DatabaseManagerCommon.deleteHelp);
        } else if (s.equals("EXECUTE")) {
            executeCurrentSQL();
        } else if (s.equals("CREATE TABLE")) {
            showHelp(DatabaseManagerCommon.createTableHelp);
        } else if (s.equals("DROP TABLE")) {
            showHelp(DatabaseManagerCommon.dropTableHelp);
        } else if (s.equals("CREATE INDEX")) {
            showHelp(DatabaseManagerCommon.createIndexHelp);
        } else if (s.equals("DROP INDEX")) {
            showHelp(DatabaseManagerCommon.dropIndexHelp);
        } else if (s.equals("CHECKPOINT")) {
            showHelp(DatabaseManagerCommon.checkpointHelp);
        } else if (s.equals("SCRIPT")) {
            showHelp(DatabaseManagerCommon.scriptHelp);
        } else if (s.equals("SHUTDOWN")) {
            showHelp(DatabaseManagerCommon.shutdownHelp);
        } else if (s.equals("SET")) {
            showHelp(DatabaseManagerCommon.setHelp);
        } else if (s.equals("Test Script")) {
            showHelp(DatabaseManagerCommon.testHelp);
        } else if (s.equals(SHOWSCHEMAS_BOX_TEXT)) {
            showSchemas = boxShowSchemas.isSelected();

            refreshTree();
        } else {
            throw new RuntimeException("Unexpected action triggered: " + s);
        }
    }

    private void displayResults() {

        if (gridFormat) {
            setResultsInGrid();
        } else {
            setResultsInText();
        }
    }

    private void setResultsInGrid() {

        pResult.removeAll();
        pResult.add(gScrollPane, BorderLayout.CENTER);
        pResult.doLayout();
        gResult.fireTableChanged(null);
        pResult.repaint();
    }

    private void setResultsInText() {

        pResult.removeAll();
        pResult.add(txtResultScroll, BorderLayout.CENTER);
        pResult.doLayout();
        showResultInText();
        pResult.repaint();
    }

    private void showHelp(String[] help) {

        txtCommand.setText(help[0]);

        bHelp = true;

        pResult.removeAll();
        pResult.add(txtResultScroll, BorderLayout.CENTER);
        pResult.doLayout();
        txtResult.setText(help[1]);
        pResult.repaint();
        txtCommand.requestFocus();
        txtCommand.setCaretPosition(help[0].length());
    }

    public void windowActivated(WindowEvent e) {}

    public void windowDeactivated(WindowEvent e) {}

    public void windowClosed(WindowEvent e) {}

    public void windowDeiconified(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowOpened(WindowEvent e) {}

    public void windowClosing(WindowEvent ev) {

        try {
            if (cConn != null) {
                cConn.close();
            }

            if (prefs != null) {
                prefs.autoRefresh   = autoRefresh;
                prefs.showRowCounts = displayRowCounts;
                prefs.showSysTables = showSys;
                prefs.showSchemas   = showSchemas;
                prefs.resultGrid    = gridFormat;
                prefs.showTooltips  = showTooltips;
                prefs.laf           = currentLAF;

                prefs.store();
            }
        } catch (Exception e) {

            //  Added: (weconsultants@users)
            CommonSwing.errorMessage(e);
        }

        fMain.dispose();

        if (bMustExit) {
            System.exit(0);
        }
    }

    private void clear() {

        ifHuge = "";

        txtCommand.setText(ifHuge);
    }

    private String busyText = null;

    private void backgroundIt(Runnable r, String description) {

        if (busyText != null) {
            Toolkit.getDefaultToolkit().beep();

            return;
        }

        // set Waiting mode here.  Inverse op must be called by final()
        // in the Thread.run() of every background thread.
        setWaiting(description);
        SwingUtilities.invokeLater(r);
    }

    public void setWaiting(String description) {

        busyText = description;

        if (busyText == null) {

            // restore the cursors we saved
            fMain.setCursor(fMainCursor);
            txtCommand.setCursor(txtCommandCursor);
            txtResult.setCursor(txtResultCursor);

            //TODO:  Enable actionButtons
        } else {

            // save the old cursors
            if (fMainCursor == null) {
                fMainCursor      = fMain.getCursor();
                txtCommandCursor = txtCommand.getCursor();
                txtResultCursor  = txtResult.getCursor();
            }

            // set the cursors to the wait cursor
            fMain.setCursor(waitCursor);
            txtCommand.setCursor(waitCursor);
            txtResult.setCursor(waitCursor);

            //TODO:  Disable actionButtons
        }

        setStatusLine(busyText);
    }

    private Runnable treeRefreshRunnable = new Runnable() {

        public void run() {

            try {
                directRefreshTree();
            } catch (RuntimeException re) {
                CommonSwing.errorMessage(re);

                throw re;
            } finally {
                setWaiting(null);
            }
        }
    };

    /**
     * Schedules to run in a Gui-safe thread
     */
    protected void executeCurrentSQL() {
        backgroundIt(new StatementExecRunnable(), "Executing SQL");
    }

    protected class StatementExecRunnable implements Runnable {

        private String sCmd;

        protected StatementExecRunnable() {

            if (4096 <= ifHuge.length()) {
                sCmd = ifHuge;
            } else {
                sCmd = txtCommand.getText();
            }
        }

        public void run() {

            gResult.clear();

            try {
                if (sCmd.startsWith("-->>>TEST<<<--")) {
                    testPerformance();
                } else {
                    executeSQL();
                }

                updateResult();

                if (gridFormat) {
                    gResult.fireTableChanged(null);
                }

                updateAutoCommitBox();
                System.gc();
            } catch (RuntimeException re) {
                CommonSwing.errorMessage(re);

                throw re;
            } finally {
                setWaiting(null);
            }
        }
    }
    ;

    private void executeSQL() {

        String[] g   = new String[1];
        String   sql = txtCommand.getText();

        try {
            lTime = System.currentTimeMillis();

            sStatement.execute(sql);

            int r = sStatement.getUpdateCount();

            if (r == -1) {
                formatResultSet(sStatement.getResultSet());
            } else {
                g[0] = "update count";

                gResult.setHead(g);

                g[0] = "" + r;

                gResult.addRow(g);
            }

            lTime = System.currentTimeMillis() - lTime;

            addToRecent(sql);
        } catch (SQLException e) {
            lTime = System.currentTimeMillis() - lTime;
            g[0]  = "SQL Error";

            gResult.setHead(g);

            String s = e.getMessage();

            s    += " / Error Code: " + e.getErrorCode();
            s    += " / State: " + e.getSQLState();
            g[0] = s;

            gResult.addRow(g);

            //  Added: (weconsultants@users)
            CommonSwing.errorMessage(e);

            return;
        }

        if (autoRefresh) {

            // We're already running in a "busy" thread.  Just update the
            // status text.
            setStatusLine("Refreshing object tree");

            String upper = sql.toUpperCase(Locale.ENGLISH);

            // This test can be very liberal.  Too liberal will just do
            // some extra refreshes.  Too conservative will display
            // obsolete info.
            if (upper.indexOf("ALTER") > -1 || upper.indexOf("DROP") > -1
                    || upper.indexOf("CREATE") > -1) {
                directRefreshTree();
            }
        }
    }

    private void updateResult() {

        if (gridFormat) {

            // in case 'help' has removed the grid
            if (bHelp) {
                pResult.removeAll();
                pResult.add(gScrollPane, BorderLayout.CENTER);
                pResult.doLayout();
                gResult.fireTableChanged(null);
                pResult.repaint();

                bHelp = false;
            }
        } else {
            showResultInText();
        }

        txtCommand.selectAll();
        txtCommand.requestFocus();
    }

    /**
     * We let Swing handle displaying nulls (which it generally does by
     * printing nothing for them), except for the case of database
     * VARCHARs, because this is the only class where there is any
     * ambiguity about where there is a null stored or not.
     */
    private void formatResultSet(ResultSet r) {

        if (r == null) {
            String[] g = new String[1];

            g[0] = "Result";

            gResult.setHead(g);

            g[0] = "(empty)";

            gResult.addRow(g);

            return;
        }

        try {
            ResultSetMetaData m         = r.getMetaData();
            int               col       = m.getColumnCount();
            Object[]          h         = new Object[col];
            boolean[]         isVarChar = new boolean[col];

            for (int i = 1; i <= col; i++) {
                h[i - 1] = m.getColumnLabel(i);
                isVarChar[i - 1] = (m.getColumnType(i)
                                    == java.sql.Types.VARCHAR);
            }

            gResult.setHead(h);

            while (r.next()) {
                for (int i = 1; i <= col; i++) {
                    try {
                        h[i - 1] = r.getObject(i);

                        if (r.wasNull()) {
                            h[i - 1] = (isVarChar[i - 1] ? NULL_STR
                                                         : null);
                        }
                    } catch (SQLException e) {}
                }

                gResult.addRow(h);
            }

            r.close();
        } catch (SQLException e) {

            //  Added: (weconsultants@users)
            CommonSwing.errorMessage(e);
        }
    }

    private void testPerformance() {

        String       all   = txtCommand.getText();
        StringBuffer b     = new StringBuffer();
        long         total = 0;

        for (int i = 0; i < all.length(); i++) {
            char c = all.charAt(i);

            if (c != '\n') {
                b.append(c);
            }
        }

        all = b.toString();

        String[] g = new String[4];

        g[0] = "ms";
        g[1] = "count";
        g[2] = "sql";
        g[3] = "error";

        gResult.setHead(g);

        int max = 1;

        lTime = System.currentTimeMillis() - lTime;

        while (!all.equals("")) {
            int    i = all.indexOf(';');
            String sql;

            if (i != -1) {
                sql = all.substring(0, i);
                all = all.substring(i + 1);
            } else {
                sql = all;
                all = "";
            }

            if (sql.startsWith("--#")) {
                max = Integer.parseInt(sql.substring(3));

                continue;
            } else if (sql.startsWith("--")) {
                continue;
            }

            g[2] = sql;

            long l = 0;

            try {
                l = DatabaseManagerCommon.testStatement(sStatement, sql, max);
                total += l;
                g[0]  = "" + l;
                g[1]  = "" + max;
                g[3]  = "";
            } catch (SQLException e) {
                g[0] = g[1] = "n/a";
                g[3] = e.toString();

                //  Added: (weconsultants@users)
                CommonSwing.errorMessage(e);
            }

            gResult.addRow(g);
            System.out.println(l + " ms : " + sql);
        }

        g[0] = "" + total;
        g[1] = "total";
        g[2] = "";

        gResult.addRow(g);

        lTime = System.currentTimeMillis() - lTime;
    }

    /**
     * Method declaration
     *
     */
    private void showResultInText() {

        Object[] col   = gResult.getHead();
        int      width = col.length;
        int[]    size  = new int[width];
        Vector   data  = gResult.getData();
        Object[] row;
        int      height = data.size();

        for (int i = 0; i < width; i++) {
            size[i] = col[i].toString().length();
        }

        for (int i = 0; i < height; i++) {
            row = (Object[]) data.elementAt(i);

            for (int j = 0; j < width; j++) {
                String item = ((row[j] == null) ? ""
                                                : row[j].toString());
                int    l    = item.length();

                if (l > size[j]) {
                    size[j] = l;
                }
            }
        }

        StringBuffer b = new StringBuffer();

        for (int i = 0; i < width; i++) {
            b.append(col[i]);

            for (int l = col[i].toString().length(); l <= size[i]; l++) {
                b.append(' ');
            }
        }

        b.append(NL);

        for (int i = 0; i < width; i++) {
            for (int l = 0; l < size[i]; l++) {
                b.append('-');
            }

            b.append(' ');
        }

        b.append(NL);

        for (int i = 0; i < height; i++) {
            row = (Object[]) data.elementAt(i);

            for (int j = 0; j < width; j++) {
                String item = ((row[j] == null) ? ""
                                                : row[j].toString());

                b.append(item);

                for (int l = item.length(); l <= size[j]; l++) {
                    b.append(' ');
                }
            }

            b.append(NL);
        }

        b.append(NL + height + " row(s) in " + lTime + " ms");
        txtResult.setText(b.toString());
    }

    private void addToRecent(String s) {

        for (int i = 0; i < iMaxRecent; i++) {
            if (s.equals(sRecent[i])) {
                return;
            }
        }

        if (sRecent[iRecent] != null) {
            mRecent.remove(iRecent);
        }

        sRecent[iRecent] = s;

        if (s.length() > 43) {
            s = s.substring(0, 40) + "...";
        }

        JMenuItem item = new JMenuItem(s);

        item.setActionCommand("#" + iRecent);
        item.addActionListener(this);
        mRecent.insert(item, iRecent);

        iRecent = (iRecent + 1) % iMaxRecent;
    }

    private void initGUI() {

        JPanel pCommand = new JPanel();

        pResult = new JPanel();
        nsSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pCommand,
                                     pResult);

        // Added: (weconsultants@users)
        nsSplitPane.setOneTouchExpandable(true);
        pCommand.setLayout(new BorderLayout());
        pResult.setLayout(new BorderLayout());

        Font fFont = new Font("Dialog", Font.PLAIN, 12);

        txtCommand = new JTextArea(5, 40);

        txtCommand.setMargin(new Insets(5, 5, 5, 5));
        txtCommand.addKeyListener(this);

        txtCommandScroll = new JScrollPane(txtCommand);
        txtResult        = new JTextArea(20, 40);

        txtResult.setMargin(new Insets(5, 5, 5, 5));

        txtResultScroll = new JScrollPane(txtResult);

        txtCommand.setFont(fFont);
        txtResult.setFont(new Font("Courier", Font.PLAIN, 12));
/*
// button replaced by toolbar
        butExecute = new JButton("Execute");

        butExecute.addActionListener(this);
        pCommand.add(butExecute, BorderLayout.EAST);
*/
        pCommand.add(txtCommandScroll, BorderLayout.CENTER);

        gResult = new GridSwing();

        TableSorter sorter = new TableSorter(gResult);

        tableModel   = sorter;
        gResultTable = new JTable(sorter);

        sorter.setTableHeader(gResultTable.getTableHeader());

        gScrollPane = new JScrollPane(gResultTable);

        gResultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        gResult.setJTable(gResultTable);

        //getContentPane().setLayout(new BorderLayout());
        pResult.add(gScrollPane, BorderLayout.CENTER);

        // Set up the tree
        rootNode    = new DefaultMutableTreeNode("Connection");
        treeModel   = new DefaultTreeModel(rootNode);
        tTree       = new JTree(treeModel);
        tScrollPane = new JScrollPane(tTree);

        tScrollPane.setPreferredSize(new Dimension(120, 400));
        tScrollPane.setMinimumSize(new Dimension(70, 100));
        txtCommandScroll.setPreferredSize(new Dimension(360, 100));
        txtCommandScroll.setMinimumSize(new Dimension(180, 100));
        gScrollPane.setPreferredSize(new Dimension(460, 300));

        ewSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                     tScrollPane, nsSplitPane);

        // Added: (weconsultants@users)
        ewSplitPane.setOneTouchExpandable(true);
        fMain.getContentPane().add(ewSplitPane, BorderLayout.CENTER);

        // Added: (weconsultants@users)
        jStatusLine = new JLabel();
        iReadyStatus =
            new JButton(new ImageIcon(CommonSwing.getIcon("StatusReady")));

        iReadyStatus.setSelectedIcon(
            new ImageIcon(CommonSwing.getIcon("StatusRunning")));

        pStatus = new JPanel();

        pStatus.setLayout(new BorderLayout());
        pStatus.add(iReadyStatus, BorderLayout.WEST);
        pStatus.add(jStatusLine, BorderLayout.CENTER);
        fMain.getContentPane().add(pStatus, "South");
        doLayout();
        fMain.pack();
    }

    /* Simple tree node factory method - set's parent and user object.
     */
    private DefaultMutableTreeNode makeNode(Object userObject,
            MutableTreeNode parent) {

        DefaultMutableTreeNode node = new DefaultMutableTreeNode(userObject);

        if (parent != null) {
            treeModel.insertNodeInto(node, parent, parent.getChildCount());
        }

        return node;
    }

    private static final String[] usertables       = {
        "TABLE", "GLOBAL TEMPORARY", "VIEW", "SYSTEM TABLE"
    };
    private static final String[] nonSystables     = {
        "TABLE", "GLOBAL TEMPORARY", "VIEW"
    };
    private static final HashSet  oracleSysUsers   = new HashSet();
    private static final String[] oracleSysSchemas = {
        "SYS", "SYSTEM", "OUTLN", "DBSNMP", "OUTLN", "MDSYS", "ORDSYS",
        "ORDPLUGINS", "CTXSYS", "DSSYS", "PERFSTAT", "WKPROXY", "WKSYS",
        "WMSYS", "XDB", "ANONYMOUS", "ODM", "ODM_MTR", "OLAPSYS", "TRACESVR",
        "REPADMIN"
    };

    static {
        for (int i = 0; i < oracleSysSchemas.length; i++) {
            oracleSysUsers.add(oracleSysSchemas[i]);
        }
    }

    /**
     * Schedules to run in a Gui-safe thread
     */
    protected void refreshTree() {
        backgroundIt(treeRefreshRunnable, "Refreshing object tree");
    }

    /**
     * Clear all existing nodes from the tree model and rebuild from scratch.
     *
     * This method executes in current thread
     */
    protected void directRefreshTree() {

        int[]                  rowCounts;
        DefaultMutableTreeNode propertiesNode;

        // Added: (weconsultants@users) Moved tableNode here for visibiity nd new DECFM
        DefaultMutableTreeNode tableNode;
        DecimalFormat DECFMT = new DecimalFormat(" ( ####,###,####,##0 )");

        // First clear the existing tree by simply enumerating
        // over the root node's children and removing them one by one.
        while (treeModel.getChildCount(rootNode) > 0) {
            DefaultMutableTreeNode child =
                (DefaultMutableTreeNode) treeModel.getChild(rootNode, 0);

            treeModel.removeNodeFromParent(child);
            child.removeAllChildren();
            child.removeFromParent();
        }

        treeModel.nodeStructureChanged(rootNode);
        treeModel.reload();
        tScrollPane.repaint();

        // Now rebuild the tree below its root
        try {

            // Start by naming the root node from its URL:
            rootNode.setUserObject(dMeta.getURL());

            // get metadata about user tables by building a vector of table names
            ResultSet result = dMeta.getTables(null, null, null,
                                               (showSys ? usertables
                                                        : nonSystables));
            Vector tables  = new Vector();
            Vector schemas = new Vector();

            // sqlbob@users Added remarks.
            Vector remarks = new Vector();
            String schema;

            while (result.next()) {
                schema = result.getString(2);

                if ((!showSys)
                        && dMeta.getDatabaseProductName().indexOf("Oracle")
                           > -1 && oracleSysUsers.contains(schema)) {
                    continue;
                }

                if (schemaFilter == null || schema.equals(schemaFilter)) {
                    schemas.addElement(schema);
                    tables.addElement(result.getString(3));
                    remarks.addElement(result.getString(5));

                    continue;
                }
            }

            result.close();

            // Added: (weconsultants@users)
            // Sort not to go into production. Have to sync with 'remarks Vector' for DBMS that has it
            //   Collections.sort(tables);
            // Added: (weconsultants@users) - Add rowCounts if needed.
            rowCounts = new int[tables.size()];

            try {
                rowCounts = getRowCounts(tables, schemas);
            } catch (Exception e) {

                //  Added: (weconsultants@users)
                CommonSwing.errorMessage(e);
            }

            // For each table, build a tree node with interesting info
            for (int i = 0; i < tables.size(); i++) {
                String name = (String) tables.elementAt(i);

                schema = (String) schemas.elementAt(i);

                String schemaname = "";

                if (schema != null && showSchemas) {
                    schemaname = schema + '.';
                }

                String rowcount = displayRowCounts
                                  ? (", " + DECFMT.format(rowCounts[i]))
                                  : "";
                String displayedName = schemaname + name + rowcount;

                // weconsul@ptd.net Add rowCounts if needed.
                tableNode = makeNode(displayedName, rootNode);

                ResultSet col = dMeta.getColumns(null, schema, name, null);

                if ((schema != null) &&!schema.trim().equals("")) {
                    makeNode(schema, tableNode);
                }

                // sqlbob@users Added remarks.
                String remark = (String) remarks.elementAt(i);

                if ((remark != null) &&!remark.trim().equals("")) {
                    makeNode(remark, tableNode);
                }

                // This block is very slow for some Oracle tables.
                // With a child for each column containing pertinent attributes
                while (col.next()) {
                    String c = col.getString(4);
                    DefaultMutableTreeNode columnNode = makeNode(c,
                        tableNode);
                    String type = col.getString(6);

                    makeNode("Type: " + type, columnNode);

                    boolean nullable = col.getInt(11)
                                       != DatabaseMetaData.columnNoNulls;

                    makeNode("Nullable: " + nullable, columnNode);
                }

                col.close();

                DefaultMutableTreeNode indexesNode = makeNode("Indices",
                    tableNode);
                ResultSet ind = null;

                if (showIndexDetails) {
                    try {
                        ind = dMeta.getIndexInfo(null, schema, name, false,
                                                 false);

                        String                 oldiname  = null;
                        DefaultMutableTreeNode indexNode = null;

                        // A child node to contain each index - and its attributes
                        while (ind.next()) {
                            boolean nonunique = ind.getBoolean(4);
                            String  iname     = ind.getString(6);

                            if ((oldiname == null
                                    ||!oldiname.equals(iname))) {
                                indexNode = makeNode(iname, indexesNode);

                                makeNode("Unique: " + !nonunique, indexNode);

                                oldiname = iname;
                            }

                            // And the ordered column list for index components
                            makeNode(ind.getString(9), indexNode);
                        }
                    } catch (SQLException se) {

                        // Workaround for Oracle
                        if (se.getMessage() == null || ((!se.getMessage()
                                .startsWith("ORA-25191:")) && (!se
                                .getMessage().startsWith("ORA-01702:")) &&!se
                                    .getMessage().startsWith("ORA-01031:"))) {
                            throw se;
                        }
                    } finally {
                        if (ind != null) {
                            ind.close();

                            ind = null;
                        }
                    }
                }
            }

            // Finally - a little additional metadata on this connection
            propertiesNode = makeNode("Properties", rootNode);

            makeNode("User: " + dMeta.getUserName(), propertiesNode);
            makeNode("ReadOnly: " + cConn.isReadOnly(), propertiesNode);
            makeNode("AutoCommit: " + cConn.getAutoCommit(), propertiesNode);
            makeNode("Driver: " + dMeta.getDriverName(), propertiesNode);
            makeNode("Product: " + dMeta.getDatabaseProductName(),
                     propertiesNode);
            makeNode("Version: " + dMeta.getDatabaseProductVersion(),
                     propertiesNode);
        } catch (SQLException se) {
            propertiesNode = makeNode("Error getting metadata:", rootNode);

            makeNode(se.getMessage(), propertiesNode);
            makeNode(se.getSQLState(), propertiesNode);
            CommonSwing.errorMessage(se);
        }

        treeModel.nodeStructureChanged(rootNode);
        treeModel.reload();
        tScrollPane.repaint();

        // We want the Schema List to always be in sync with the displayed tree
        updateSchemaList();
    }

    // Added: (weconsultants@users) Sets up\changes the running status icon
    void setStatusLine(String busyBaseString) {

        iReadyStatus.setSelected(busyBaseString != null);

        if (busyBaseString == null) {
            String additionalMsg = "";

            if (schemaFilter != null) {
                additionalMsg = " /  Tree showing objects in schema '"
                                + schemaFilter + "'";
            }

            jStatusLine.setText("  " + READY_STATUS + additionalMsg);
        } else {
            jStatusLine.setText("  " + busyBaseString + "...");
        }
    }

    // Added: (weconsultants@users) Needed to aggragate counts per table in jTree
    protected int[] getRowCounts(Vector inTable,
                                 Vector inSchema) throws Exception {

        if (!displayRowCounts) {
            return (null);
        }

        String rowCountSelect = "SELECT COUNT(*) FROM ";
        int[]  counts;
        String name;

        counts = new int[inTable.size()];

        try {
            Statement select = rowConn.createStatement();

            for (int i = 0; i < inTable.size(); i++) {
                try {
                    String schemaPart = (String) inSchema.elementAt(i);

                    schemaPart = schemaPart == null ? ""
                                                    : (schemaPart + '.');
                    name       = schemaPart + (String) inTable.elementAt(i);

                    String displayRowCounts = rowCountSelect + name;
                    ResultSet resultSet =
                        select.executeQuery(displayRowCounts);

                    while (resultSet.next()) {
                        counts[i] = resultSet.getInt(1);
                    }
                } catch (Exception e) {
                    System.err.println("Unable to get row count for table "
                                       + inSchema.elementAt(i) + '.'
                                       + inTable.elementAt(i)
                                       + ".  Using value '0': " + e);
                }
            }
        } catch (Exception e) {
            CommonSwing.errorMessage(e);
        }

        return (counts);
    }

    protected JToolBar createToolBar() {

        // Build jtoolbar and jtoolbar Buttons
        JToolBar jtoolbar = new JToolBar();

        jtoolbar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);

        // I'm dropping "Statement" from  "Execute SQL Statement", etc.,
        // because it may or may not be "one statement", but it is SQL.
        // Build jbuttonClear Buttons - blaine
        JButton jbuttonClear =
            new JButton("Clear SQL",
                        new ImageIcon(CommonSwing.getIcon("Clear")));

        jbuttonClear.putClientProperty("is3DEnabled", Boolean.TRUE);
        tipMap.put(jbuttonClear, "Clear SQL");
        jbuttonClear.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent) {
                clear();
            }
        });

        JButton jbuttonExecute =
            new JButton("Execute SQL",
                        new ImageIcon(CommonSwing.getIcon("Execute")));

        tipMap.put(jbuttonExecute, "Execute SQL");
        jbuttonExecute.putClientProperty("is3DEnabled", Boolean.TRUE);
        jbuttonExecute.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent) {
                executeCurrentSQL();
            }
        });
        jtoolbar.addSeparator();
        jtoolbar.add(jbuttonClear);
        jtoolbar.addSeparator();
        jtoolbar.add(jbuttonExecute);
        jtoolbar.addSeparator();
        jbuttonClear.setAlignmentY(0.5F);
        jbuttonClear.setAlignmentX(0.5F);
        jbuttonExecute.setAlignmentY(0.5F);
        jbuttonExecute.setAlignmentX(0.5F);

        return jtoolbar;
    }

    void updateAutoCommitBox() {

        try {
            boxAutoCommit.setSelected(cConn.getAutoCommit());
        } catch (SQLException se) {
            CommonSwing.errorMessage(se);
        }
    }

    private void setLF(String newLAF) {

        if (currentLAF != null && currentLAF == newLAF) {    // No change
            return;
        }

        if (pResult != null && gridFormat) {
            pResult.removeAll();
        }

        CommonSwing.setSwingLAF(fMain, newLAF);

        if (pResult != null && gridFormat) {
            setResultsInGrid();
        }

        currentLAF = newLAF;

        if (currentLAF.equals(CommonSwing.Native)) {
            rbNativeLF.setSelected(true);
        } else if (currentLAF.equals(CommonSwing.Java)) {
            rbJavaLF.setSelected(true);
        } else if (currentLAF.equals(CommonSwing.Motif)) {
            rbMotifLF.setSelected(true);
        }
    }

    void resetTooltips() {

        Iterator   it = tipMap.keySet().iterator();
        JComponent component;

        while (it.hasNext()) {
            component = (JComponent) it.next();

            component.setToolTipText(showTooltips
                                     ? ((String) tipMap.get(component))
                                     : (String) null);
        }
    }

    private void updateSchemaList() {

        ButtonGroup group = new ButtonGroup();
        ArrayList   list  = new ArrayList();

        try {
            ResultSet result = dMeta.getSchemas();

            if (result == null) {
                throw new SQLException(
                    "Failed to get metadata from database");
            }

            while (result.next()) {
                list.add(result.getString(1));
            }

            result = null;
        } catch (SQLException se) {
            CommonSwing.errorMessage(se);
        }

        mnuSchemas.removeAll();
        rbAllSchemas.setSelected(schemaFilter == null);
        group.add(rbAllSchemas);
        mnuSchemas.add(rbAllSchemas);

        String               s;
        JRadioButtonMenuItem radioButton;

        for (int i = 0; i < list.size(); i++) {
            s           = (String) list.get(i);
            radioButton = new JRadioButtonMenuItem(s);

            group.add(radioButton);
            mnuSchemas.add(radioButton);
            radioButton.setSelected(schemaFilter != null
                                    && schemaFilter.equals(s));
            radioButton.addActionListener(schemaListListener);
            radioButton.setEnabled(list.size() > 1);
        }

        mnuSchemas.addSeparator();
        mnuSchemas.add(mitemUpdateSchemas);
    }

    ActionListener schemaListListener = (new ActionListener() {

        public void actionPerformed(ActionEvent actionevent) {

            schemaFilter = actionevent.getActionCommand();

            if (schemaFilter.equals("*")) {
                schemaFilter = null;
            }

            refreshTree();
        }
    });

    /**
     * Persisted User Preferences for DatabaseManagerSwing.
     *
     * These are settings for items in the View and Options pulldown menus,
     * plus Help/Show Tooltips.
     */
    public static class DBMPrefs {

        private File prefsFile = null;

        // Set defaults from Data
        boolean autoRefresh   = true;
        boolean showRowCounts = false;
        boolean showSysTables = false;
        boolean showSchemas   = true;
        boolean resultGrid    = true;
        String  laf           = CommonSwing.Native;

        // Somebody with more time can store the font settings.  IMO, that
        // menu item shouldn'tString even be there if the settings aren't persisted.
        boolean showTooltips = true;

        public DBMPrefs()
        throws NoSuchMethodException, ClassNotFoundException,
               InstantiationException, IllegalAccessException,
               InvocationTargetException {

            String homedir = null;

            // May be running under a security manager
            try {
                Class c =
                    Class.forName("sun.security.action.GetPropertyAction");
                Constructor constructor = c.getConstructor(new Class[]{
                    String.class });
                java.security.PrivilegedAction a =
                    (java.security.PrivilegedAction) constructor.newInstance(
                        new Object[]{ "user.home" });

                homedir =
                    (String) java.security.AccessController.doPrivileged(a);
                prefsFile = new File(homedir, "dbmprefs.properties");
            } catch (NoSuchMethodException e) {
                System.err.println(
                    "Failed to get home directory.\n"
                    + "Therefore not retrieving/storing user preferences.\n("
                    + e.getMessage() + ')');

                throw e;
            } catch (ClassNotFoundException e) {
                System.err.println(
                    "Failed to get home directory.\n"
                    + "Therefore not retrieving/storing user preferences.\n("
                    + e.getMessage() + ')');

                throw e;
            } catch (InstantiationException e) {
                System.err.println(
                    "Failed to get home directory.\n"
                    + "Therefore not retrieving/storing user preferences.\n("
                    + e.getMessage() + ')');

                throw e;
            } catch (IllegalAccessException e) {
                System.err.println(
                    "Failed to get home directory.\n"
                    + "Therefore not retrieving/storing user preferences.\n("
                    + e.getMessage() + ')');

                throw e;
            } catch (InvocationTargetException e) {
                System.err.println(
                    "Failed to get home directory.\n"
                    + "Therefore not retrieving/storing user preferences.\n("
                    + e.getMessage() + ')');

                throw e;
            }

            load();
        }

        private static final String tString = Boolean.TRUE.toString();
        private static final String fString = Boolean.FALSE.toString();

        public void load() {

            Properties props = new Properties();

            if (!prefsFile.exists()) {
                return;
            }

            try {
                FileInputStream fis = new FileInputStream(prefsFile);

                props.load(fis);
                fis.close();
            } catch (IOException ioe) {
                throw new RuntimeException("Failed to read preferences file '"
                                           + prefsFile + "':  "
                                           + ioe.getMessage());
            }

            String tmpString;

            tmpString = props.getProperty("autoRefresh");

            if (tmpString != null) {
                autoRefresh = Boolean.valueOf(tmpString).booleanValue();
            }

            tmpString = props.getProperty("showRowCounts");

            if (tmpString != null) {
                showRowCounts = Boolean.valueOf(tmpString).booleanValue();
            }

            tmpString = props.getProperty("showSysTables");

            if (tmpString != null) {
                showSysTables = Boolean.valueOf(tmpString).booleanValue();
            }

            tmpString = props.getProperty("showSchemas");

            if (tmpString != null) {
                showSchemas = Boolean.valueOf(tmpString).booleanValue();
            }

            tmpString = props.getProperty("resultGrid");

            if (tmpString != null) {
                resultGrid = Boolean.valueOf(tmpString).booleanValue();
            }

            tmpString = props.getProperty("laf");
            laf       = ((tmpString == null) ? CommonSwing.Native
                                             : tmpString);
            tmpString = props.getProperty("showTooltips");

            if (tmpString != null) {
                showTooltips = Boolean.valueOf(tmpString).booleanValue();
            }
        }

        public void store() {

            Properties props = new Properties();

            // Boolean.toString(boolean) was new with Java 1.4, so don't use that.
            props.setProperty("autoRefresh", (autoRefresh ? tString
                                                          : fString));
            props.setProperty("showRowCounts", (showRowCounts ? tString
                                                              : fString));
            props.setProperty("showSysTables", (showSysTables ? tString
                                                              : fString));
            props.setProperty("showSchemas", (showSchemas ? tString
                                                          : fString));
            props.setProperty("resultGrid", (resultGrid ? tString
                                                        : fString));
            props.setProperty("laf", laf);
            props.setProperty("showTooltips", (showTooltips ? tString
                                                            : fString));

            try {
                FileOutputStream fos = new FileOutputStream(prefsFile);

                props.store(fos, "DatabaseManagerSwing user preferences");
                fos.flush();
                fos.close();
            } catch (IOException ioe) {
                throw new RuntimeException(
                    "Failed to prepare preferences file '" + prefsFile
                    + "':  " + ioe.getMessage());
            }
        }
    }
}
