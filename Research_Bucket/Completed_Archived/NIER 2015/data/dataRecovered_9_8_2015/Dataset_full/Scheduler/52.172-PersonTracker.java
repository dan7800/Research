/*
 * IMPORTANT:  READ BEFORE DOWNLOADING, COPYING, INSTALLING OR USING.
 * By downloading, copying, installing or using the software you agree to this
 * license.  If you do not agree to this license, do not download, install,
 * copy or use the software.
 * 
 * Intel Open Source License 
 * 
 * Copyright (c) 1996-2002 Intel Corporation. All rights reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 
 * 
 * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer. 
 * 
 * 	Redistributions in binary form must reproduce the above copyright
 * 	notice, this list of conditions and the following disclaimer in the
 * 	documentation and/or other materials provided with the distribution. 
 * 
 * 	Neither the name of the Intel Corporation nor the names of its
 * 	contributors may be used to endorse or promote products derived from
 * 	this software without specific prior written permission.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE INTEL OR ITS  CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.tinyos.viz;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import java.sql.*;
import java.text.DecimalFormat;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;

import gwe.sql.gweMysqlDriver;

/**
 * This class is a visualization of people moving throughout an environment. The people are tagged
 * with mobile motes and are moving through an environment instrumented with static motes. The
 * static motes record and send back their interactions with mobile motes to a base station, which
 * logs the interaction data in a database. The visualization reads the data from the database at
 * a regular interval (every 30 seconds) and updates the display, only taking into account readings that
 * occurred in the last 60 seconds.
 */
public class PersonTracker {

  /**
   * Flag to indicate whether a database is available
   */
  public static final boolean HAVE_DB = true;  // debugging flag for testing at home

  /**
   * Width of the scroll window
   */
  public static final int SCROLL_WIDTH = 600;

  /**
   * Height of the scroll window
   */
  public static final int SCROLL_HEIGHT = 600;

  /**
   * Pan mode for interaction
   */
  public static final int PAN_MODE = 1;

  /**
   * Put the visualization in no interaction mode
   */
  public static final int NO_MODE = 2;

  /**
   * Constant not being used currently
   */
  public static final int LIMIT = 5;

  /**
   * Interval to re-read data from the database = 30 seconds
   */
  public static final long SLEEP = 1000 * 30; // 30 seconds

  /**
   * Window of time to read from database = 60 seconds
   */
  public static final long WINDOW = 1000 * 60; // 60 seconds

  private JMenuItem configLoad, configExit;
  private JFrame frame;
  private Configuration config;
  private String dbConnection, dbUser, dbPass;
  private JMenu configuration;

  private ZEventHandler currentEventHandler = null;
  private ZPanEventHandler panEventHandler = null;
  private ZoomEventHandler zoomEventHandler = null;
  ZImageCanvas canvas = null;
  JScrollPane scrollPane;
  ZLayerGroup layer;

  Motes motes = new Motes();
  MobileMotes mobileMotes = new MobileMotes();

  int imageWidth, imageHeight;

  GregorianCalendar cal;
  Random rand = new Random();
  private long starttime = -1;

  double xd1 = 50.0;
  double yd1 = 50.0;

  /**
   * Constructor for the person tracker visualization using the default database connection information
   */
  public PersonTracker() {
    dbConnection = "jdbc:mysql://"+setup.DEFAULT_DB_HOST+":"+setup.DEFAULT_DB_PORT+"/"+setup.DEFAULT_DB_PATH;
    dbUser = setup.DEFAULT_DB_USER;
    dbPass = setup.DEFAULT_DB_PASSWORD;
    cal = new GregorianCalendar();
    beginSetup();
  }

  /**
   * Constructor for the person tracker visualization using the given database connection information
   *
   * @param dbHost Name of the machine hosting the jdbc compatible database
   * @param dbPort Port number the database is running on
   * @param dbPath Name of the database being used
   * @param dbUser User name to access the database
   * @param dbPass Password to use to access the database
   */
  public PersonTracker(String dbHost, int dbPort, String dbPath, String dbUser, String dbPass) {
    dbConnection = "jdbc:mysql://"+dbHost+":"+dbPort+"/"+dbPath;
    this.dbUser = dbUser;
    this.dbPass = dbPass;
    cal = new GregorianCalendar();
    beginSetup();
  }

  /**
   * Constructor for the person tracker visualization using the default database connection information.
   * This constructor is used to visualize old data, and starts from the given time using the default
   * database information
   *
   * @param time Time in YYMMDDHHMMSS format to start reading person tracker data from
   */
  public PersonTracker(String time) {
    cal = new GregorianCalendar();
    starttime = fromSqlTime(time);
    dbConnection = "jdbc:mysql://"+setup.DEFAULT_DB_HOST+":"+setup.DEFAULT_DB_PORT+"/"+"retreatbackup";
    dbUser = setup.DEFAULT_DB_USER;
    dbPass = setup.DEFAULT_DB_PASSWORD;
    beginSetup();
  }

  /**
   * Sets up the visualization by accessing the configuration and mote tables.
   */
  private void beginSetup() {
    // make sure the database is reachable
    try {
      Class.forName("gwe.sql.gweMysqlDriver");
    } catch (ClassNotFoundException cnfe) {
        System.out.println("setup constructor ClassNotFound: "+cnfe);
        System.out.println("Could not load the mysql driver: please check your classpath");
        System.exit(-1);
    }

    // try to reach the default table containing the configurations
    try {
      if (!tableExists(setup.DEFAULT_DB_CONFIG_TABLE)) {
        System.out.println("Could not access table: "+setup.DEFAULT_DB_CONFIG_TABLE);
        System.exit(-2);
      }
    } catch (SQLException sqle) {
        System.out.println("setup beginSetup SQLException: "+sqle);
        System.out.println("Trouble interacting with vizConfig database table");
        System.exit(-3);
    }
    prepareFrame();
  }

  /**
   * Prepares the frame for the visualization. Displays the menus for interaction.
   */
  public void prepareFrame() {
    config = new Configuration();

    // create a new frame
    frame = new JFrame("Person Tracking Visualization");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    // add a menubar to deal with configurations
    JMenuBar menubar = new JMenuBar();
    frame.setJMenuBar(menubar);
    
    configuration = new JMenu("Configuration");
    menubar.add(configuration);
    configuration.setMnemonic(KeyEvent.VK_C);

    configLoad = new JMenuItem("Load", KeyEvent.VK_L);
    configLoad.setActionCommand("Load Configuration");
    configLoad.setToolTipText("Load Configuration");
    configLoad.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        loadConfiguration(setup.DEFAULT_DB_CONFIG_TABLE);
      }
    });
    configuration.add(configLoad);

    configExit = new JMenuItem("Exit", KeyEvent.VK_X);
    configExit.setActionCommand("Exit");
    configExit.setToolTipText("Exit Application");
    configExit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        exitConfiguration();
      }
    });
    configuration.add(configExit);

    frame.getContentPane().add(createToolBar(), BorderLayout.NORTH);

    frame.setSize(400,200);
    frame.setVisible(true);
  }

  /**
   * Creates a toolbar to allow panning of the frame
   *
   * @return the created toolbar
   */ 
  private JToolBar createToolBar() {
    JToolBar toolbar = new JToolBar();
    Insets margins = new Insets(0, 0, 0, 0);

    ToolBarButton pan = new ToolBarButton("images/P.gif");
    pan.setToolTipText("Pan view");
    pan.setMargin(margins);
    pan.setActionCommand("pan");
    pan.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        if (((ToolBarButton)ae.getSource()).isSelected()) {
          setMode(PAN_MODE);
        }
        else {
          setMode(NO_MODE);
        }
      }
    });
    toolbar.add(pan);
    toolbar.setFloatable(true);
    return toolbar;
  }

  /**
   * Loads the table containing the list of available configurations and displays them for the user to 
   * select one
   *
   * @param table Table containing the configurations
   */
  private void loadConfiguration(String table) {
    if (HAVE_DB) {
      Vector configNames = getConfigurations(table);
      if (configNames.size() == 0) {
        JOptionPane.showMessageDialog(frame, "There are no configurations to load", "No Configurations Available", JOptionPane.ERROR_MESSAGE);
        return;
      }
  
      // show dialog to allow user to select a configuration
      ConfigurationSelectDialog csdialog = new ConfigurationSelectDialog(frame, "load", configNames);
      csdialog.pack();
      csdialog.setLocationRelativeTo(frame);
      csdialog.setVisible(true);
      if (csdialog.isDataValid()) {
        System.out.println(csdialog.getSelectedConfiguration()+" chosen");
      }

      // if one has been selected, collect the configuration data from the database
      try {
        String s = "SELECT * FROM "+table+" WHERE configName='"+csdialog.getSelectedConfiguration()+"'";
        Connection con = DriverManager.getConnection(dbConnection, dbUser, dbPass);
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(s);
        rs.next();
        config = new Configuration(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4),
                 rs.getInt(5), rs.getInt(6), rs.getInt(7), rs.getInt(8), rs.getInt(9), rs.getInt(10), rs.getInt(11), rs.getInt(12));
        rs.close();
        stmt.close();
        con.close();
      } catch (SQLException sqle) {
          System.out.println("SQLException: "+sqle+": error reading configuration");
          return;
      }
    }
    else {
      // USE FAKE DATA
      config = new Configuration("test", "null", 400, 400, 0, 0, 400, 400, 0, 0, 400, 400);
    }

    // AKD load up motes from d/b table
    // motes = getMotes(config.getName()+"Motes");
    // mobileMotes = getMobileMotes(config.getName()+"MobileMotes");

    // load up the motes information from the "motelocation" table
    // This should be the code up above, but the data collection code written by others
   //  puts the data into this one table rather than a table prefixed with the configuration name.
    motes = getMotes("motelocation");
    viewConfiguration(table);
  }

  /**
   * This method renders the configuration to the screen
   *
   * @param table Table containing the configuration information
   */  
  private void viewConfiguration(String table) {
    // do view stuff here
    if (scrollPane != null) {
      frame.getContentPane().remove(scrollPane);
    }

    // render the background image
    if (config.useBlankImage()) {
      imageWidth = config.getImageWidth();
      imageHeight = config.getImageHeight();
      canvas = new ZImageCanvas(imageWidth, imageHeight);
    }
    else {
      ImageIcon icon = new ImageIcon(config.getImageName(), config.getImageName());
      Image base = icon.getImage();
      imageHeight = base.getHeight(null);
      imageWidth = base.getWidth(null);
      canvas = new ZImageCanvas(base);
    }

    // render a rectangle around the active area
    layer = canvas.getLayer();
    ZLine line = new ZLine(config.getMinimumPixelX(), config.getMinimumPixelY(), 
                           config.getMaximumPixelX(), config.getMinimumPixelY());
    ZVisualLeaf leaf = new ZVisualLeaf(line);
    layer.addChild(leaf);
    line = new ZLine(config.getMinimumPixelX(), config.getMinimumPixelY(), 
                     config.getMinimumPixelX(), config.getMaximumPixelY());
    leaf = new ZVisualLeaf(line);
    layer.addChild(leaf);
    line = new ZLine(config.getMaximumPixelX(), config.getMinimumPixelY(), 
                     config.getMaximumPixelX(), config.getMaximumPixelY());
    leaf = new ZVisualLeaf(line);
    layer.addChild(leaf);
    line = new ZLine(config.getMinimumPixelX(), config.getMaximumPixelY(), 
                     config.getMaximumPixelX(), config.getMaximumPixelY());
    leaf = new ZVisualLeaf(line);
    layer.addChild(leaf);

    // render the motes
    for (Enumeration e=motes.elements(); e.hasMoreElements(); ) {
      leaf = new ZVisualLeaf((Mote)e.nextElement());
      layer.addChild(leaf);
    }

    // get the mobile motes from the "mobilemote" table
    // This should use other code as before, but the data collection code written by others
   //  puts the data into this one table rather than a table prefixed with the configuration name.
    getMobileMotes("mobilemote");

    JPanel main = new JPanel(new BorderLayout());

    // add a scroll pane around the image
    int x=0, y = 0;
    if (imageWidth > SCROLL_WIDTH) {
      x = SCROLL_WIDTH;
    }
    else {
      x = imageWidth;
    }

    if (imageHeight > SCROLL_HEIGHT) {
      y = SCROLL_HEIGHT;
    }
    else {
      y = imageHeight;
    }

    scrollPane = new ZScrollPane(canvas);

    scrollPane.setPreferredSize(new Dimension(x+20, y+20));
    frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

    frame.pack();

    // create the handlers and set the zoom handler to true
    panEventHandler = new ZPanEventHandler(canvas.getCameraNode());
    zoomEventHandler = new ZoomEventHandler(canvas.getCameraNode());
    zoomEventHandler.setActive(true);

    // set a timer thread to deal with repetitive tasks - putting the thread to sleep delays
    // drawing in Jazz, so a timer has to be used
    java.util.Timer timer = new java.util.Timer();
    timer.schedule(new Task(), 0, SLEEP);
  }

/*
   get list of mobile motes from d/b
     then populate image with static motes
    mobile motes show text label with id/name
     check d/b every x seconds for each mobile mote
       get data from last y seconds
        get x,y for each static mote in data
        x = (x1*ss1 + x2*ss2 + ... + xn*ssn)/(ss1 + ss2 + ... + ssn)
        y = (y1*ss1 + y2*ss2 + ... + yn*ssn)/(ss1 + ss2 + ... + ssn)
       if (all xn are same) and (all yn are same) 
         give random offset from x,y
       move mobile mote to new x,y
*/
  
  /**
   * If the user chooses exit, the visualization should exit
   */
  private void exitConfiguration() {
    System.exit(0);
  }

  /**
   * This method determines whether the points given are the all the same
   *
   * @param x Array of x coordinates
   * @param y Array of y coordinates
   * @return whether the points given are all the same or not
   */
  private boolean same(double[] x, double[] y) {
    if ((x[0] == -1) || (x[1] == -1)) {
      return true;
    }

    int i=1;
    while ((x[i] != -1) && (i<10)) {
      if ((x[i] != x[i-1]) || (y[i] != y[i-1])) {
        return false;
      }
      i++;
    }
    return true;
  }

  /**
   * Helper method for converting an integer to a 2 character string
   *
   * @param n Integer to convert
   * @return Converted integer
   */
  private String d2(int n) {
    return "" + n / 10 + n % 10;
  }

  /**
   * This method converts the given time to a string in the form YYMMDDHHMMSS
   *
   * @param millis Time in milliseconds to convert to
   * @return The resulting string
   */
  private String sqlTime(long millis) {
    cal.setTime(new java.util.Date(millis));
    return d2(cal.get(cal.YEAR) % 100) + d2(cal.get(cal.MONTH) + 1) + d2(cal.get(cal.DAY_OF_MONTH)) + d2(cal.get(cal.HOUR_OF_DAY)) + d2(cal.get(cal.MINUTE)) + d2(cal.get(cal.SECOND));
  }

  /**
   * This method converts from the time in format YYMMDDHHMMSS to a number of milliseconds
   *
   * @param time Time to convert to
   * @return number of milliseconds
   */
  private long fromSqlTime(String time) {
    cal.set(2000 + Integer.valueOf(time.substring(0,2)).intValue(), -1 + Integer.valueOf(time.substring(2,4)).intValue(), Integer.valueOf(time.substring(4,6)).intValue(),
            Integer.valueOf(time.substring(6,8)).intValue(), Integer.valueOf(time.substring(8,10)).intValue(), Integer.valueOf(time.substring(10)).intValue());
    return cal.getTime().getTime();
  }

  /**
   * Returns a random number between +/- 1.5*Mote.NODE_SIZE
   *
   * @return Random number between +/- 1.5*Mote.NODE_SIZE
   */
  private double getRandom() {
    int random = (rand.nextInt() & Integer.MAX_VALUE) % (new Double(Mote.NODE_SIZE/2.0).intValue() + 1);
    boolean sign = rand.nextBoolean();

    double offset = Mote.NODE_SIZE + random;
    if (!sign) {
      offset= -1 * offset;
    }
    return offset;
  }

  /**
   * Checks to see if the given table exists
   *
   * @param tablename Name of the table to check on
   * @return whether the table exists or not
   * @throws SQLException when problems with check occur
   */
  private boolean tableExists(String table) throws SQLException {
    if (HAVE_DB) {
      Connection con = DriverManager.getConnection(dbConnection, dbUser, dbPass);
      Statement stmt = con.createStatement();

      ResultSet rs = stmt.executeQuery("SHOW TABLES");
      while(rs.next()) {
        String result = rs.getString(1);
        if (result.equals(table)) {
          return true;
        }
      }
      rs.close();
      stmt.close();
      con.close();
      return false;
    }
    else {
      return true;
    }
  }

  /**
   * Retrieves the list of static motes from the given table
   *
   * @param table Table containing the static motes information
   * @return List of static motes 
   */
  public Motes getMotes(String table) {
    if (HAVE_DB) {
      Motes m = new Motes();
      try {
        Connection con = DriverManager.getConnection(dbConnection, dbUser, dbPass);
        Statement stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT * FROM "+table);
        while(rs.next()) {
          m.addMote(new Mote(rs.getDouble(1), rs.getDouble(2), rs.getInt(3)));
        }
        rs.close();
        stmt.close();
        con.close();
      } catch (SQLException sqle) {
          System.out.println("setup getConfigurations SQLException: "+sqle);
          System.out.println("Trouble interacting with vizConfig database table");
          System.exit(-2);
      }
      return m;
    }
    else {
      Motes m = new Motes();
      m.addMote(new Mote(40.0, 40.0, 1));
      m.addMote(new Mote(80.0, 80.0, 2));
      m.addMote(new Mote(40.0, 80.0, 3));
      return m;
    }
  }

  /**
   * Retrieves the list of mobile motes from the given table
   *
   * @param table Table containing the mobile motes information
   * @return List of mobile motes 
   */
  public void getMobileMotes(String table) {
    if (HAVE_DB) {
      try {
        Connection con = DriverManager.getConnection(dbConnection, dbUser, dbPass);
        Statement stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT * FROM "+table);
        while(rs.next()) {
          if (!mobileMotes.contains(rs.getInt(1))) {
            MobileMote mm = new MobileMote(rs.getInt(1), rs.getString(2));
            mobileMotes.addMobileMote(mm);
            layer.addChild(new ZVisualLeaf(mm));
          }
        }
        rs.close();
        stmt.close();
        con.close();
      } catch (SQLException sqle) {
          System.out.println("setup getConfigurations SQLException: "+sqle);
          System.out.println("Trouble interacting with vizConfig database table");
          System.exit(-2);
      }
   }
   else {
      if (!mobileMotes.contains(10)) {
        MobileMote mm = new MobileMote(10, "10");
        mobileMotes.addMobileMote(mm);
        layer.addChild(new ZVisualLeaf(mm));
      }
      if (!mobileMotes.contains(11)) {
        MobileMote mm = new MobileMote(11, "11");
        mobileMotes.addMobileMote(mm);
        layer.addChild(new ZVisualLeaf(mm));
      }

      if (!mobileMotes.contains(12)) {
        MobileMote mm = new MobileMote(12, "12");
        mobileMotes.addMobileMote(mm);
        layer.addChild(new ZVisualLeaf(mm));
      }
    }
  }

  /**
   * This method retrieves the list of configurations from the default table
   *
   * @return list of existing configurations
   */
  public Vector getConfigurations() {
    return getConfigurations(setup.DEFAULT_DB_CONFIG_TABLE);
  }

  /**
   * This method retrieves the list of configurations from the given table
   *
   * @return list of existing configurations
   */
  public Vector getConfigurations(String table) {
    if (HAVE_DB) {
      Vector configs = new Vector();
      try {
        Connection con = DriverManager.getConnection(dbConnection, dbUser, dbPass);
        Statement stmt = con.createStatement();

        int i=0;
        ResultSet rs = stmt.executeQuery("SELECT configName FROM "+table);
        while(rs.next()) {
          i++;
          configs.addElement(rs.getString(1));
        }
        rs.close();
        stmt.close();
        con.close();
      } catch (SQLException sqle) {
          System.out.println("setup getConfigurations SQLException: "+sqle);
          System.out.println("Trouble interacting with vizConfig database table");
          System.exit(-2);
      }
      return configs;
   }
   else {
     return new Vector();
   }
  }

  /**
   * This method sets the mode of the interface
   * 
   * @param mode Mode to set the interface to
   */
  public void setMode(int mode) {
    if (currentEventHandler != null) {
      currentEventHandler.setActive(false);
    }

    switch (mode) {
      case PAN_MODE: currentEventHandler = panEventHandler;
                     canvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                     break;
      case NO_MODE:  currentEventHandler = null;
                     canvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                     break;
    }

    if (currentEventHandler != null) {
      currentEventHandler.setActive(true);
    }
  }

  /**
   * Inner class containing the repeated task to run. This really handles accessing the dynamic information
   * from the database and renders it on the static information collected and displayed earlier.
   */
  class Task extends TimerTask {

    /**
     * The repeatable method for doing the visualization
     */
    public void run() {
      double signalSum = 0;
      double xc, yc;
      double[] xs = new double[100];
      double[] ys = new double[100];
      double[] signals = new double[100];
      double XmultiplierSum = 0;
      double YmultiplierSum = 0;

      for (int i=0; i<100; i++) {
        xs[i] = -1;
        ys[i] = -1;
      }

      // re-load the set of mobile motes in case one has been added or removed
      getMobileMotes("mobilemote");

      if (HAVE_DB) {
        try {
          Connection con = DriverManager.getConnection(dbConnection, dbUser, dbPass);
          Statement stmt = con.createStatement();

          java.util.Date date = new java.util.Date();
          long ms = date.getTime();

          // for each mobile mote
          for (Enumeration e=mobileMotes.elements(); e.hasMoreElements(); ) {
            MobileMote mm = (MobileMote)e.nextElement();
            String s;
            if (starttime == -1) {
              s = new String("SELECT nbrid, signalstrength, time FROM tracking WHERE moteid = '"+mm.getId()+"' AND time > "+sqlTime(ms-WINDOW));
            }
            else {
              s = new String("SELECT nbrid, signalstrength, time FROM tracking WHERE moteid = '"+mm.getId()+"' AND time < "+sqlTime(starttime)+" AND time > "+sqlTime(starttime-WINDOW));
            }
            ResultSet rs = stmt.executeQuery(s);

            int i=0;
            boolean found = false;
            // for each of the table entries, average the position using signal strengths 
            while (rs.next()) {
              int id = rs.getInt(1);
              Mote m = motes.getMote(id);
              if (m != null) {
                found = true;
System.out.println(i);
                xs[i] = m.getX();
                ys[i] = m.getY();
                int signal = rs.getInt(2);
                if (signal == -1) {
                  signals[i] = 512.0; // some typical value
                }
                else {
                  signals[i] = new Integer(signal).doubleValue();
                }
                signalSum += signals[i];
                XmultiplierSum += xs[i] * signals[i];
                YmultiplierSum += ys[i] * signals[i];
                i++;
              }
            }
            rs.close();

            // if the signal strength is not -1, a static mote other than a base station saw the mobile mote 
            if (found) {
              // if all the points are the same, add a random offset to distribute mobile motes around
              // a static mote
              // AKD - for some reason, randomizer seems to be clustering multiple motes
              if (same(xs,ys)) { 
                xc = xs[0] + getRandom(); // + random offset
                yc = ys[0] + getRandom(); // + random offset
              }
              else {
                xc = XmultiplierSum/signalSum;
                yc = YmultiplierSum/signalSum;
              }

              // move xc and yc to within image
              if (xc < Mote.NODE_SIZE/2) {
                xc = Mote.NODE_SIZE/2;
              }
              else if (xc > (imageWidth - Mote.NODE_SIZE/2)) {
                xc = imageWidth - Mote.NODE_SIZE/2;
              }
              if (yc < Mote.NODE_SIZE/2) {
                yc = Mote.NODE_SIZE/2;
              }
              else if (yc > (imageHeight - Mote.NODE_SIZE/2)) {
                yc = imageHeight - Mote.NODE_SIZE/2;
              }
              mm.move(xc,yc);
            }
          }
          stmt.close();
          con.close();
        } catch (SQLException sqle) {
          System.out.println("PersonTracker viewConfiguration SQLException: "+sqle);
        }
      }
      else {
        // USE FAKE DATA
        for (Enumeration e=mobileMotes.elements(); e.hasMoreElements(); ) {
          MobileMote mm = (MobileMote)e.nextElement();
          xs[0] = 80.0;
          ys[0] = 80.0;
          if (xs[0] != -1) {
            if (same(xs,ys)) {
              xc = xs[0] + getRandom();
              yc = ys[0] + getRandom();
              mm.move(xc,yc);
            }
          }
        }
       
//        for (Enumeration e=mobileMotes.elements(); e.hasMoreElements(); ) {
//          MobileMote m = (MobileMote)e.nextElement();
//          m.move(xd1,yd1);
//          xd1+= 40.0;
//          yd1+= 40.0;
//        }     
      }
      try {
        Thread.sleep(SLEEP);
      } catch (InterruptedException ie) {
          System.out.println("PersonTracker: couldn't sleep: "+ie);
      }

      if (starttime != -1) {
        starttime += SLEEP;
      }
    }
  }
    
  /**
   * Main method for the person tracker visualization
   */
  public static void main(String argv[]) {
    if (argv.length == 5) {
      PersonTracker pt = new PersonTracker(argv[0], Integer.parseInt(argv[1]), argv[2], argv[3], argv[4]);
    }
    else if (argv.length == 1) {
      PersonTracker pt = new PersonTracker(argv[0]);
    }
    else if (argv.length == 0) {
      PersonTracker pt = new PersonTracker();
    }
    else {
      System.out.println("USAGE: viz.PersonTracker [database ip/hostname] [database port] [database path] [database user] [database password]");
      System.out.println("OR");
      System.out.println("USAGE: viz.PersonTracker <starttime>, to use historical information, where starttime refers to the database time to start with");
    }
  }
}
