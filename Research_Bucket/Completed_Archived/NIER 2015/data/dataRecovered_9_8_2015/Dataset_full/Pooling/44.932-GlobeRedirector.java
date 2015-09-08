/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// GlobeRedirector.java

package vu.globe.svcs.gred;


import java.net.*;
import java.io.*;
import java.util.*;

import vu.globe.rts.runtime.cfg.Settings;
import vu.globe.util.log.Log;
import vu.globe.util.db.dbif.*;
import vu.globe.util.http.*;
import vu.globe.util.log.*;
import vu.globe.util.parse.GetOpt;
import vu.globe.util.security.UID;

import vu.globe.svcs.gids.client.*;
//import vu.globe.rts.runtime.cfg.*;
import vu.globe.util.parse.SettingFileException;
import netscape.ldap.LDAPException;

import vu.globe.svcs.gred.cfg.*;
import vu.globe.svcs.gred.blocklist.*;
import vu.globe.svcs.gred.util.*;


/**
 * This is the startup interface of the Globe redirector. It first parses
 * command line options and then creates a TCP listening socket. Next, it
 * creates a GlobeRedirector object that goes into an infinite loop waiting
 * for clients (i.e., Web browsers) to connect to the socket. For each
 * client, a separate job is created that is inserted in a thread pool.
 * <p>
 * On a multi-homed host the IP address to listen to may be specified with the
 * gredConfig.preferredIP setting
 *
 * @see vu.globe.rts.runtime.cfg.Settings
 */
public class GlobeRedirector
{
  /** The following name is used in the "Server:" field of an HTTP reply. */
  public static final String SERVER_NAME = "GlobeRedirector 0.1";

  /** Redirector configuration settings. */
  public static RedirectorConfiguration config;

  /** Error log. */
  public static Log errorLog;

  /** Access log. If set to <code>null</code>, access logging is disabled. */
  public static LogFileWriter accessLog;

  /** A cache to map IP addresses to their nearest GAP records. */
  public static Cache ip2GAPCache;

  /** A database to map IP addresses to their geographical coordinates. */
  public static GeoDB geoDB;

  /** A NetGeo client. */
  public static NetGeoClient netgeo;

  /**
   * A list with blocked site names, file names, and URLs. Requests for
   * these items are blocked by the redirector.
   */
  public static BlockList blockList;


  /**
   * A list with available GAPs. This list is peridiocally replaced (by
   * another GAPList) by the GAP list refresh thread.
   */
  private static GAPList _gapList;

  /** The thread pool. */
  private static RedirectorThreadPool _threadPool;


  static final String PROGNAME = "GlobeRedirector";

  // back-log for TCP listening
  static final int BACK_LOG = 50;



  public static void main(String[] argv)
  {
    ServerSocket sock = null;
    int port = 0;
    String ip_result;
    int i, debugLevel;
    InetAddress preferredIP = null;
    boolean logToConsole;
    String s, optArg, cfgFile, site_dir;
    CfgFileReader cfgFileReader;
    GetOpt getOpt;
    char c;
    String user;

    // GIDS
    GIDSSite gids = null;
    String gred_tag, portStr, admin, preferredIPStr;
    String host = "localhost";

    if (argv.length < 4) {
      usage();
    }

    // Create redirector configuration with its defaults.
    config = new RedirectorConfiguration();

    debugLevel = -1;
    logToConsole = false;
    user = null;

    /*
     * Read the command line options.
     */
    getOpt = new GetOpt(argv, 0, "cd:A:L:G:u:");
    try {
       while ( (c = getOpt.getNextOpt()) != GetOpt.EOF) {
         switch(c) {
          case 'c':
            if (getOpt.getOptArg() != null) {
              usage();
            }
            logToConsole = true;
            break;

          case 'L':
            if ( (optArg = getOpt.getOptArg()) == null) {
              usage();
            }
            config.setErrorLogFileName(optArg);
            break;

          case 'A':
            if ( (optArg = getOpt.getOptArg()) == null) {
              usage();
            }
            config.setAccessLogFileName(optArg);
            break;

          case 'G':
            if ( (optArg = getOpt.getOptArg()) == null) {
              usage();
            }
            config.setGeoDBFileName(optArg);
            break;

          case 'u':
            if ( (optArg = getOpt.getOptArg()) == null) {
              usage();
            }
            user = optArg;
            break;

          default:
            usage();
        }
      }
    }
    catch(IOException e) {
      fatal("cannot process command line option: " + e.getMessage());
    }

    i = getOpt.getIndex();

    if (i != argv.length - 1) {
      usage();
    }

    site_dir = argv[i];
    File f = new File(site_dir);
    site_dir = f.getAbsolutePath();

    /*
     * Override debug level in the configuration file with the one specified
     * on the command line, if it is set.
     */
    try {
      debugLevel = Integer.parseInt(Settings.getDebugLevelSetting());
    }
    catch(NumberFormatException e) {
      // safely ignore the error.  debugLevel does not change.
    }
    if (debugLevel >= 0) {
      config.setDebugLevel(debugLevel);
    }


    /*
     * Initialise access to the GIDS
     */
    try {
      gids = new GIDSSite (site_dir);
      gids.authenticate();
    }
    catch (SettingFileException e) {
      fatal("error reading file: " + e.getMessage());
    }
    catch (FileNotFoundException e) {
      fatal("unable to find file: " + e.getMessage());
    }
    catch (IOException e) {
      fatal("unable to read file: " + e.getMessage());
    }
    catch (LDAPException e) {
      fatal("unable to contact and/or authenticate to GIDS: " + e);
    }

    // Initialise the Settings object.
    Settings.initGIDSSettings(gids, new String[] { "baseRegionConfig",
						   "gredConfig"});
    Settings settings = Settings.getInstance();


    // check the required settings
    /*
    String[] required;
    String msg;
    
    required = new String[] {	"latitude", "longitude", "treeHost", 
				"treePort", "leafNodeID", "dns", "gnsRoot", 
				"globeca" };
    msg = Settings.getInstance().checkSettings("baseRegionConfig", required);
    if (msg != null) {
      fatal("Error: "+msg);
    }
    
    required = new String[] { };
    msg = Settings.getInstance().checkSettings("gredConfig", required);  
    if (msg != null) {
      fatal("Error: "+msg);
    }
    */


    /*
     * Read the configuration file.
     */
    cfgFile = settings.getValue("gids.schema.configfile"); //argv[i++];
    if (cfgFile == null || cfgFile.equals("")) {
      cfgFile = "file:///" + site_dir + "/gred.conf";
    }
    try {
      cfgFileReader = new CfgFileReader();
      cfgFileReader.read(cfgFile, config);
      cfgFileReader = null;
    }
    catch(FileNotFoundException e) {
      fatal("No such configuration file: " + cfgFile);
    }
    catch(CfgFileException e) {
      fatal("Configuration file error: " + e.getMessage());
    }
    catch(IOException e) {
      fatal("Configuration file read error: I/O error: " + e.getMessage());
    }

    /*
     * get gred configuration from Settings object
     */

    // port is required
    portStr = settings.getValue("gids.schema.port");
    try {
      port = Integer.parseInt(portStr);
    }
    catch(NumberFormatException e) {
      fatal("invalid port: " + e.getMessage());
    }

    // tag is required
    gred_tag = settings.getValue("gids.schema.tag");

    // preferredIP is optional => may be null
    preferredIPStr = settings.getValue("gids.schema.preferredIP");
    if (preferredIPStr != null) { 
      try {
        preferredIP = InetAddress.getByName (preferredIPStr);
      }
      catch (UnknownHostException exc) {
        fatal("preferredIP attribute refers to unknown IP address " +
	    preferredIPStr);
      }
    }

    // admin is optional => may be null
    admin = settings.getValue ("gids.schema.admin");
    

    /*
     * Open the listening socket.
     */
    try { 
      if (preferredIP == null) {
        sock = new ServerSocket(port, BACK_LOG);
      }
      else {
        sock = new ServerSocket(port, BACK_LOG, preferredIP);
      }
    }
    catch(IOException e) {
      fatal("cannot listen on" + (preferredIP == null ? "" : " address " +
				  preferredIP) + " port " + port);
    }

    ip_result = (preferredIP == null?"'any'":sock.getInetAddress().toString());

    System.out.println("listening on ip " + ip_result + ", port " + port);

    /*
     * Add listening port and host name and address to configuration.
     */
    config.setListeningPort(port);
    if (preferredIP == null) {
      try {
	// XXX should use gids.schema.host instead, see GlobeTranslator.java
        InetAddress myAddr = InetAddress.getLocalHost();
        config.setHostAddress(myAddr);
        config.setHostName(myAddr.getHostName());
      }
      catch(UnknownHostException e) {
        fatal("cannot determine my host address");
      }
    }
    else {
      config.setHostAddress(preferredIP);
      config.setHostName(preferredIP.getHostName());
    }

    /*
     * Set the redirector signature. This signature is added to a HTML
     * reload page that is sent to a client.
     */
    config.setSignature("Globe Redirector at " + config.getHostName()
                        + " Port " + config.getListeningPort());

    /*
     * Change the user ID of this process if necessary.
     */
    changeUserID(user);
    System.out.println("Redirector user id: " + UID.geteuid());

    /*
     * Open error log.
     */
    errorLog = null;
    try {
      if ( (s = config.getErrorLogFileName()) == null) {
        fatal("No error log specified.");
      }
      errorLog = new Log(PROGNAME, logToConsole, s);
    }
    catch(IOException e) {
      fatal("Cannot open error log: " + e.getMessage());
    }

    /*
     * Open access log if necessary.
     */
    accessLog = null;
    if (config.getAccessLogEnabledFlag()) {
      try {
        if ( (s = config.getAccessLogFileName()) == null) {
          fatal("No access log file defined.");
        }
        accessLog = new LogFileWriter(s, logToConsole);
      }
      catch(IOException e) {
        fatal("Cannot open access log: " + e.getMessage());
      }
    } 

    config.print();

    _gapList = new GAPList();

    /*
     * Read the GAP list.
     */
    if (config.getGAPListURL() != null) {
      try {
        GAPListReader reader = new GAPListReader();

        reader.read(config.getGAPListURL(), _gapList);
      }
      catch(IOException e) {
        fatal("cannot read GAP list: " + e.getMessage());
      }

      System.out.println("Available Globe Access Points (GAPs):");
      _gapList.print();
    }
    else {
      System.out.println("\nWarning: no GAP list specified.\n");
    }

    /*
     * Read the block-list file (if specified).
     */
    blockList = new BlockList();
    if ( (s = config.getBlockListFileName()) != null) {
      BlockListReader reader = new BlockListReader();

      try {
        reader.read(s, blockList);

        System.out.println("Blocked files/sites/URLs:");
        blockList.print();
      }
      catch(BlockListFileException e) {
        fatal("corrupted block list file: " + e.getMessage());
      }
      catch(IOException e) {
        fatal("cannot read block list file: I/O error: " + e.getMessage());
      }
    }

    /*
     * Set the default GAP.
     */
    if (config.getDefaultGAPHost() == null) {
      fatal("No default GAP specified in the configuration file");
    }
    GlobeAccessPointRecord gap = new GlobeAccessPointRecord();
    gap.hostport = config.getDefaultGAPHost() + ":"
                   + config.getDefaultGAPPort();
    gap.coords = config.getDefaultGAPCoordinates();
    config.setDefaultGAP(gap);

    // Add the default to the GAP list.
    if (_gapList.contains(gap) == false) {
      _gapList.add(gap.hostport, gap);
    }

    _gapList.createHTMLList();

    /*
     * Start the GAP list refresh thread. This thread periodically retrieves
     * a new copy of the GAP list and assigns it to the variable _gapList.
     */
    if (config.getGAPListURL() != null) {
      GAPListRefreshThread th = new GAPListRefreshThread(
                                      config.getGAPListURL(),
                                      config.getGAPListRefreshInterval(),
                                      gap);
      th.start();
    }

    /*
     * Open the Geo database to read and store the IP-address-to-coordinates
     * mappings.
     */
    try {
      geoDB = new GeoDB(config.getGeoDBFileName(), config.getGeoDBType());
    }
    catch(DBException e) {
      fatal("cannot open Geo database: " + e.getMessage());
    }

    geoDB.setSyncWritesFlag(config.getGeoDBSyncWritesFlag());

    // Create the IP-address-to-GAP-record cache.
    ip2GAPCache = new Cache(config.getIP2GAPCacheSize(),
                            config.getIP2GAPCacheTTL());

    // Create NetGeo client.
    // See http://www.caida.org/tools/utilities/netgeo/NGServer/index.xml.
    netgeo = new NetGeoClient();



    
    //register with GIDS
    HashMap reg = GIDSUtil.constructRegEntry ("gred", gred_tag, 
					      config.getHostName(),
					      config.getListeningPort()+"", 
					      admin, null);
    String dn = GIDSUtil.constructDN (reg);
    settings.addSetting("myDN", dn);
    try { gids.register (reg); }
    catch (LDAPException e) {
      fatal("Unable to register with GIDS: " + e);
    }


    // Start the thread pool.
    _threadPool = new RedirectorThreadPool(config.getThreadPoolSize(), sock);

    System.out.println("Globe redirector ready."); 

    // Now block reading from standard input and wait for it to close.  
    // When it closes clean up and end the program.
    try {
      while (true) {
	if (System.in.read() == -1) {
	  break;
	}
      }
    } catch (IOException e) {
      // nothing
    }

    // deregister from GIDS
    try {
      gids.remove (settings.getValue("myDN"));
    } 
    catch (LDAPException e) {
      System.err.println("could not deregister from GIDS: "+ e);
    }

    // Finished program
    System.out.println("Globe Redirector is closed for business... goodbye!");

    // end.
    System.exit(1);
  }


  /**
   * Return the GAP list. Note that this list reflects the current
   * GAP list.
   */
  public static GAPList getGAPList()
  {
    return _gapList;
  }


  /**
   * Set the GAP list.
   *
   * @param  gapList  GAP list to replace the current GAP list with
   */
  public static void setGAPList(GAPList gapList)
  {
    _gapList = gapList;
  }


  /**
   * Change the user ID of this process to the ID of the named user.
   * If the user is not specified, and the real user ID of this process
   * is 0 (superuser), the user ID is set to the process' effective
   * user ID. If, however, the effective user ID is also 0, the redirector
   * exits with an error message.
   *
   * @param  user  users's login name or user ID
   */
  private static void changeUserID(String user)
  {
    try {
      if (user != null) {
        System.out.println("Setting user ID to user " + user);
        
        UID.dropUserPriveleges(user);
      }
      else {
        int uid = UID.getuid();
        int euid = UID.geteuid();

        /*
         * If this process is running in superuser mode, set the ID to
         * the real user ID. If the real user ID is 0 (superuser),
         * the redirector exits, telling the user to specify the user
         * ID.
         */
        if (uid == 0) {
          if (euid == 0) {
            fatal("You should not run this program with superuser privileges."
                  + "\nUse the -u option to specify the user ID "
                  + "of this process");
          }
          else {
            System.out.println("Setting user ID to " + euid);

            UID.dropUserPriveleges(euid);
          }
        }
      }
    }
    catch(RuntimeException e) {
      fatal("Cannot set user ID: " + e.getMessage());
    }
  }


  /**
   * Print a usage message and exit.
   */
  private static void usage()
  {
    System.out.println("Usage:");
    System.out.println("  "+PROGNAME+" [options] site-dir");
    //configuration-file site-dir");
    System.out.println();
    System.out.println("Parameters:");
    //    System.out.println("  configuration-file: redirector configuration file");
    System.out.println("  site-dir  : the directory with site data");
    System.out.println();
    System.out.println("Options:");
    System.out.println("  -c        : mirror error and access log "
                       + "messages to console");
    //    System.out.println("  -d level  : set debug level (level 0 "
    //                       + "suppresses debugging output");
    System.out.println("  -L file   : specify the error log file");
    System.out.println("  -A file   : specify the access log file");
    System.out.println("  -G file   : specify the Geo database file");
    System.out.println("  -u user   : specify the user name to which the user "
                       + "ID of this process");
    System.out.println("              should be set. (default: do not "
                       + "change user ID)");
    System.exit(1);
  }


  /**
   * Print an error message and exit.
   */
  private static void fatal(String s)
  {
    System.err.println(PROGNAME + ": " + s);
    System.exit(-1);
  }
}
