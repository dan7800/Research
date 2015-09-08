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

package net.tinyos.viz.sensor;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import java.sql.*;

import edu.umd.cs.jazz.*;
import edu.umd.cs.jazz.component.*;
import edu.umd.cs.jazz.event.*;
import edu.umd.cs.jazz.util.*;

import gwe.sql.gweMysqlDriver;

import net.tinyos.viz.*;

/**
 * This class is a visualization of environmental data: heat and light, along with network route information. 
 * The visualization reads the data from the database at a regular interval (every 60 seconds) and updates 
 * the display. Temperature and light can be visualized as transparent circles: temperature varying between
 * blue and red, light with shades of gray; or they can be visualized as a gradient map. Routing information
 * between sensor nodes are shown with a red line between a node and its parent and a green line between a node
 * and any other node it can communicate with.
 */
public class Environment {

  /**
   * Light sensor
   */
  public static final String LIGHT = "Light";

  /**
   * Temperature sensor
   */
  public static final String TEMPERATURE = "Temperature";

  /**
   * Simple visualization
   */
  public static final String SIMPLE = "Simple";

  /**
   * Gradient visualization
   */
  public static final String GRADIENT = "Gradient";

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
   * Interval to re-read data from the database = 60 seconds
   */
  public static final long SLEEP = 1000 * 60; // 60 seconds

  /**
   * Window of time to read from database = 60 seconds
   */
  public static final long WINDOW = 1000 * 60; // 60 seconds

  /**
   * Pixel step amount for doing gradient calculations
   */
  public static final int STEP = 10; // 10 pixels

  public String sensorType, viewType;

  private GregorianCalendar cal;

  private ZEventHandler currentEventHandler = null;
  private ZPanEventHandler panEventHandler = null;
  private ZoomEventHandler zoomEventHandler = null;
  ZImageCanvas canvas = null;
  JScrollPane scrollPane;
  JMenu viz, sensor;
  ZLayerGroup layer;
  ZGroup lightSimpleGroup, tempSimpleGroup, lightGradientGroup, tempGradientGroup, routeGroup;
  SensorMotes motes = new SensorMotes();
  private String dbConnection, dbUser, dbPass;
  private JFrame frame;
  private Configuration config;
  private long starttime = -1;
  int imageWidth, imageHeight;


  /**
   * Constructor for the sensor network visualization using the default database connection information
   */
  public Environment() {
    dbConnection = "jdbc:mysql://"+setup.DEFAULT_DB_HOST+":"+setup.DEFAULT_DB_PORT+"/"+setup.DEFAULT_DB_PATH;
    dbUser = setup.DEFAULT_DB_USER;
    dbPass = setup.DEFAULT_DB_PASSWORD;
    cal = new GregorianCalendar();
    beginSetup();
  }

  /**
   * Constructor for the sensor network visualization using the given database connection information
   *
   * @param dbHost Name of the machine hosting the jdbc compatible database
   * @param dbPort Port number the database is running on
   * @param dbPath Name of the database being used
   * @param dbUser User name to access the database
   * @param dbPass Password to use to access the database
   */
  public Environment(String dbHost, int dbPort, String dbPath, String dbUser, String dbPass) {
    dbConnection = "jdbc:mysql://"+dbHost+":"+dbPort+"/"+dbPath;
    this.dbUser = dbUser;
    this.dbPass = dbPass;
    cal = new GregorianCalendar();
    beginSetup();
  }

  /**
   * Constructor for the sensor network visualization using the default database connection information.
   * This constructor is used to visualize old data, and starts from the given time using the default
   * database information
   *
   * @param time Time in YYMMDDHHMMSS format to start reading person tracker data from
   */
  public Environment(String time) {
    cal = new GregorianCalendar();
System.out.println("time: "+time);
    starttime = fromSqlTime(time);
System.out.println("starttime: "+starttime);
    dbConnection = "jdbc:mysql://"+setup.DEFAULT_DB_HOST+":"+setup.DEFAULT_DB_PORT+"/"+"openhouse";
    dbUser = setup.DEFAULT_DB_USER;
    dbPass = setup.DEFAULT_DB_PASSWORD;
    beginSetup();
  }

  /**
   * Sets up the visualization by accessing the configuration and mote tables.
   */
  private void beginSetup() {
    try {
      Class.forName("gwe.sql.gweMysqlDriver");
    } catch (ClassNotFoundException cnfe) {
        System.out.println("setup constructor ClassNotFound: "+cnfe);
        System.out.println("Could not load the mysql driver: please check your classpath");
        System.exit(-1);
    }

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

    // creates a new frame
    frame = new JFrame("Environment and Routing Visualization");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    // creates a menubar
    JMenuBar menubar = new JMenuBar();
    frame.setJMenuBar(menubar);
    
    JMenu configuration = new JMenu("Configuration");
    menubar.add(configuration);
    configuration.setMnemonic(KeyEvent.VK_C);

    JMenuItem configLoad = new JMenuItem("Load", KeyEvent.VK_L);
    configLoad.setActionCommand("Load Configuration");
    configLoad.setToolTipText("Load Configuration");
    configLoad.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        loadConfiguration(setup.DEFAULT_DB_CONFIG_TABLE);
      }
    });
    configuration.add(configLoad);

    JMenuItem configExit = new JMenuItem("Exit", KeyEvent.VK_X);
    configExit.setActionCommand("Exit");
    configExit.setToolTipText("Exit Application");
    configExit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        exitConfiguration();
      }
    });
    configuration.add(configExit);

    sensor = new JMenu("Sensor");
    menubar.add(sensor);
    sensor.setMnemonic(KeyEvent.VK_S);

    ButtonGroup group = new ButtonGroup();
    JRadioButtonMenuItem light = new JRadioButtonMenuItem(LIGHT, true);
    light.setActionCommand(LIGHT);
    light.setToolTipText(LIGHT);
    light.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        for (Enumeration e=motes.elements(); e.hasMoreElements(); ) {
          SensorMote sm = (SensorMote)e.nextElement();
          sm.setSensorToVisualize(1);
          sm.setColorScheme(SensorMote.GRAY);
        }
        sensorType = LIGHT;
        if (viewType.equals(GRADIENT)) {
          layer.removeChild(tempGradientGroup);
          layer.addChild(lightGradientGroup);
        }
      }
    });
    group.add(light);
    sensor.add(light);

    JRadioButtonMenuItem temp = new JRadioButtonMenuItem(TEMPERATURE, false);
    temp.setActionCommand(TEMPERATURE);
    temp.setToolTipText(TEMPERATURE);
    temp.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        for (Enumeration e=motes.elements(); e.hasMoreElements(); ) {
          SensorMote sm = (SensorMote)e.nextElement();
          sm.setSensorToVisualize(2);
          sm.setColorScheme(SensorMote.COLOR);
        }
        sensorType = TEMPERATURE;
        if (viewType.equals(GRADIENT)) {
          layer.removeChild(lightGradientGroup);
          layer.addChild(tempGradientGroup);
        }
      }
    });
    group.add(temp);
    sensor.add(temp);

    sensor.disable();
 
    viz = new JMenu("Visualization");
    menubar.add(viz);
    viz.setMnemonic(KeyEvent.VK_V);

    ButtonGroup group2 = new ButtonGroup();
    JRadioButtonMenuItem simple = new JRadioButtonMenuItem(SIMPLE, true);
    simple.setActionCommand(SIMPLE);
    simple.setToolTipText(SIMPLE);
    simple.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        for (Enumeration e=motes.elements(); e.hasMoreElements(); ) {
          SensorMote sm = (SensorMote)e.nextElement();
          sm.setVisualizationType(SensorMote.SIMPLE);
        }
        viewType = SIMPLE;
        layer.removeChild(lightGradientGroup);
        layer.removeChild(tempGradientGroup);
      }
    });
    group2.add(simple);
    viz.add(simple);

    JRadioButtonMenuItem gradient = new JRadioButtonMenuItem(GRADIENT, false);
    gradient.setActionCommand(SIMPLE);
    gradient.setToolTipText(SIMPLE);
    gradient.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        for (Enumeration e=motes.elements(); e.hasMoreElements(); ) {
          SensorMote sm = (SensorMote)e.nextElement();
          sm.setVisualizationType(SensorMote.GRADIENT);
        }
        viewType = GRADIENT;

        layer.removeChild(tempGradientGroup);
        layer.removeChild(lightGradientGroup);
        if (sensorType.equals(LIGHT)) {
          layer.addChild(lightGradientGroup);
        }
        else {
          layer.addChild(tempGradientGroup);
        }
      }
    });
    group2.add(gradient);
    viz.add(gradient);

    viz.disable();

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

    ToolBarButton pan = new ToolBarButton("../images/P.gif");
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
      ConfigurationSelectDialog csdialog = new ConfigurationSelectDialog(frame, "load", configNames);
      csdialog.pack();
      csdialog.setLocationRelativeTo(frame);
      csdialog.setVisible(true);
      if (csdialog.isDataValid()) {
        System.out.println(csdialog.getSelectedConfiguration()+" chosen");
      }

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

    sensorType = LIGHT;
    viewType = SIMPLE;
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

    viz.enable();
    sensor.enable();

    if (scrollPane != null) {
      frame.getContentPane().remove(scrollPane);
    }

    // render background image
    if (config.useBlankImage()) {
      imageWidth = config.getImageWidth();
      imageHeight = config.getImageHeight();
      canvas = new ZImageCanvas(imageWidth, imageHeight);
    }
    else {
      ImageIcon icon = new ImageIcon(config.getImageName(), config.getImageName());
      Image base = icon.getImage();
System.out.println("have image");
System.out.println("height: "+base.getHeight(null));
      imageHeight = base.getHeight(null);
      imageWidth = base.getWidth(null);
      canvas = new ZImageCanvas(base);
    }

    // render surrounding rectangle
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

    // render motes
System.out.println(motes.size());
    for (Enumeration e=motes.elements(); e.hasMoreElements(); ) {
      leaf = new ZVisualLeaf((SensorMote)e.nextElement());
      layer.addChild(leaf);
    }

    lightSimpleGroup = new ZGroup();
    tempSimpleGroup = new ZGroup();
    lightGradientGroup = new ZGroup();
    tempGradientGroup = new ZGroup();
    routeGroup = new ZGroup();
    layer.addChild(routeGroup);

    JPanel main = new JPanel(new BorderLayout());

    // add scroll pane
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

    // create event handlers
    panEventHandler = new ZPanEventHandler(canvas.getCameraNode());
    zoomEventHandler = new ZoomEventHandler(canvas.getCameraNode());
    zoomEventHandler.setActive(true);

    // set a timer thread to deal with repetitive tasks - putting the thread to sleep delays
    // drawing in Jazz, so a timer has to be used
    java.util.Timer timer = new java.util.Timer();
    timer.schedule(new Task(), 0, 10);
  }

  /**
   * If the user chooses exit, the visualization should exit
   */
  private void exitConfiguration() {
    System.exit(0);
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
   * Retrieves the list of sensor motes from the given table
   *
   * @param table Table containing the sensor motes information
   * @return List of sensor motes 
   */
  public SensorMotes getMotes(String table) {
    if (HAVE_DB) {
      SensorMotes m = new SensorMotes();
      try {
        Connection con = DriverManager.getConnection(dbConnection, dbUser, dbPass);
        Statement stmt = con.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT * FROM "+table);
        while(rs.next()) {
          SensorMote sm = new SensorMote(rs.getDouble(1), rs.getDouble(2), rs.getInt(3));
          m.addMote(sm);
          if (sensorType.equals(LIGHT)) {
            sm.setSensorToVisualize(1);
            sm.setColorScheme(SensorMote.GRAY);
          }
          else {
            sm.setSensorToVisualize(1);
            sm.setColorScheme(SensorMote.GRAY);
          }
          if (viewType.equals(SIMPLE)) {
            sm.setVisualizationType(SensorMote.SIMPLE);
          }
          else {
            sm.setVisualizationType(SensorMote.GRADIENT);
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
      return m;
    }
    else {
      SensorMotes m = new SensorMotes();
      FileRead file = new FileRead("moteLocations.txt");
      StringTokenizer st = new StringTokenizer(file.read());
      while (st.hasMoreTokens()) {
        SensorMote sm = new SensorMote(Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()), Integer.parseInt(st.nextToken()));
        m.addMote(sm);
        if (sensorType.equals(LIGHT)) {
          sm.setSensorToVisualize(2);
          sm.setColorScheme(SensorMote.GRAY);
        }
        else {
          sm.setSensorToVisualize(1);
          sm.setColorScheme(SensorMote.GRAY);
        }
        if (viewType.equals(SIMPLE)) {
          sm.setVisualizationType(SensorMote.SIMPLE);
        }
        else {
          sm.setVisualizationType(SensorMote.GRADIENT);
        }
      }
      return m;
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
     * This method calculates the distance between 2 points
     *
     * @param x X coordinate of first point
     * @param y Y coordinate of first point
     * @param x X coordinate of second point
     * @param y Y coordinate of second point
     * 
     * @returns distance between 2 points
     */
    public double distance(int x, int y, int x1, int y1){
      return Math.sqrt( (x-x1)*(x-x1)+(y-y1)*(y-y1));
    }

    /**
     * The repeatable method for doing the visualization
     */
    public void run() {
      // temperature and light data
      if (HAVE_DB) {
        /*** using real data here ***/
        try {
          String s;
          if (starttime == -1) {
            s = "SELECT moteid, light, temp, MAX(time) FROM sensor GROUP BY moteid";
          }
          else {
            s = "SELECT moteid, light, temp, MAX(time) FROM sensor WHERE TIME < "+sqlTime(starttime)+" GROUP BY moteid";
          } 
          Connection con = DriverManager.getConnection(dbConnection, dbUser, dbPass);
          Statement stmt = con.createStatement();
          ResultSet rs = stmt.executeQuery(s);
          while (rs.next()) {
            SensorMote mote = motes.getMote(rs.getInt(1));
            if (mote != null) {
              mote.setSensorValue(1, rs.getInt(2));  // light in slot 1
              mote.setSensorValue(2, rs.getInt(3));  // temperature in slot 2
            }
          }
          stmt.close();
          con.close();

          // do gradient work here

          lightGradientGroup.removeAllChildren();
          tempGradientGroup.removeAllChildren();
          
          int xp = 0;
          int yp = 0;
          int tempmin = 1000000000;
          int tempmax = 0;

          for (xp=0; xp<imageWidth; xp+=STEP) {
            for (yp=0; yp<imageHeight; yp+=STEP) {
              double lightval = 0;
              double lightsum = 0; 
              double lighttotal = 0;
              double tempval = 0;
              double tempsum = 0; 
              double temptotal = 0;
              for (Enumeration e = motes.elements(); e.hasMoreElements(); ) {
                SensorMote sm = (SensorMote)e.nextElement();
                double dist = distance(xp, yp, (int)(sm.getX()), (int)(sm.getY()));
                if (sm.getSensorValue(1) > 0) {
                  lightval += ((double)sm.getSensorValue(1)) / dist / dist;
                  lightsum += (1/dist/dist);
                }
                if (sm.getSensorValue(2) > 0) {
                  tempval += ((double)sm.getSensorValue(2)) / dist / dist;
                  tempsum += (1/dist/dist);
                }
              }

              int lightreading = (int)(lightval/lightsum)/4;
              if (lightreading > 0xff) {
                lightreading = 0xff;
              }
              // AKD - create light rectangle here
              // need to get graphics object to set transparency
              TransparencyRectangle rect = new TransparencyRectangle(xp, yp, STEP, STEP);
              rect.setFillPaint(new Color(lightreading, lightreading, lightreading));
              ZVisualLeaf leaf = new ZVisualLeaf(rect);
              lightGradientGroup.addChild(leaf);
    
              int tempreading = (int)(tempval/tempsum)/4;
              if (tempreading < 0) {
                tempreading = 0;
              }
              if (tempreading > 255) {
                tempreading = 255;
              }
              if (tempreading > tempmax) {
                tempmax = tempreading;
              }
              if (tempreading < tempmin) {
                tempmin = tempreading;
              }

              // ADK - create temp rectangle here
              rect = new TransparencyRectangle(xp, yp, STEP, STEP);
              rect.setFillPaint(new Color(tempreading, tempreading, tempreading));
              leaf = new ZVisualLeaf(rect);
              tempGradientGroup.addChild(leaf);
            }
          }
        } catch(SQLException sqle) {
            System.out.println("VectorStorage flushStorage() SQL: "+sqle);
        }
      }
      else {
        /*** using fake data here ***/
        int i=0;
        for (Enumeration e=motes.elements(); e.hasMoreElements(); ) {
          SensorMote mote = (SensorMote)e.nextElement();
          mote.setSensorValue(1, i*23); // light in slot 1
          mote.setSensorValue(2, i*23);  // light in slot 2
          i++;
        }
      }

      // route data
      routeGroup.removeAllChildren();

      if (HAVE_DB) {
        try {
          java.util.Date date = new java.util.Date();
          long ms = date.getTime();

          String s;
          if (starttime == -1) {
            s = "SELECT moteid, parentid, nbrid1, nbrid2, nbrid3, nbrid4, nbrid5, MAX(time) from route where time > "+sqlTime(ms-WINDOW)+" group by moteid";
          }
          else {
            s = "SELECT moteid, parentid, nbrid1, nbrid2, nbrid3, nbrid4, nbrid5, MAX(time) from route WHERE TIME < "+sqlTime(starttime)+" AND time > "+sqlTime(starttime-WINDOW)+" group by moteid";
          }
          Connection con = DriverManager.getConnection(dbConnection, dbUser, dbPass);
          Statement stmt = con.createStatement();
          ResultSet rs = stmt.executeQuery(s);
          while (rs.next()) {
            SensorMote mote = (SensorMote)motes.getMote(rs.getInt(1));
            if (mote != null) {
              double x = mote.getX();
              double y = mote.getY();
    
              for (int i=2; i<7; i++) {
                SensorMote nbr = (SensorMote)motes.getMote(rs.getInt(i));
                if (nbr != null) {
                  double xn = nbr.getX();
                  double yn = nbr.getY();
                  ZLine line = new ZLine(x,y,xn,yn);
                  routeGroup.addChild(new ZVisualLeaf(line));
                  if (i == 2) {
                    // draw line for parent
                    line.setPenPaint(Color.red);
                    line.setPenWidth(4);
                  }
                  else {
                    // draw line for parent
                    line.setPenPaint(Color.green);
                    line.setPenWidth(1);
                  }
                }
              }
            }
          }
          stmt.close();
          con.close();
        } catch(SQLException sqle) {
            System.out.println("VectorStorage flushStorage() SQL: "+sqle);
        }
      }
      else {
         /* USING FAKE DATA HERE */
        FileRead file = new FileRead("route.txt");
        StringTokenizer st = new StringTokenizer(file.read());
        while (st.hasMoreTokens()) {
          SensorMote mote = (SensorMote)motes.getMote(Integer.parseInt(st.nextToken()));
          if (mote != null) {
            double x = mote.getX();
            double y = mote.getY();

            for (int i=2; i<7; i++) {
              SensorMote nbr = (SensorMote)motes.getMote(Integer.parseInt(st.nextToken()));
              if (nbr != null) {
                double xn = nbr.getX();
                double yn = nbr.getY();
                ZLine line = new ZLine(x,y,xn,yn);
                routeGroup.addChild(new ZVisualLeaf(line));
                if (i == 2) {
                  // draw line for parent
                  line.setPenPaint(Color.red);
                  line.setPenWidth(4);
                } // close if (i == 2)
                else {
                  // draw line for parent
                  line.setPenPaint(Color.green);
                  line.setPenWidth(1);
                } // close else
              } // close (if nbr != null) 
            } // close for
          } // close if mote != null
        }
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

  public static void main(String argv[]) {
    if (argv.length == 5) {
      Environment env = new Environment(argv[0], Integer.parseInt(argv[1]), argv[2], argv[3], argv[4]);
    }
    else if (argv.length == 1) {
      Environment env = new Environment(argv[0]);
    }
    else if (argv.length == 0) {
      Environment env = new Environment();
    }
    else {
      System.out.println("USAGE: viz.sensor.Environment [database ip/hostname] [database port] [database path] [database user] [database password]");
      System.out.println("OR");
      System.out.println("USAGE: viz.sensor.Environment <starttime>, to use historical information, where starttime refers to the database time to start with");
    }
  }
}
