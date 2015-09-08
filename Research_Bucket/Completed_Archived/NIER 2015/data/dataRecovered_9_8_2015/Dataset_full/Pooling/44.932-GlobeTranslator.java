/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// GlobeTranslator.java

package vu.globe.svcs.gtrans;


import java.util.HashMap;
import java.net.*;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import vu.globe.util.log.Log;

import vu.globe.util.log.*;
import vu.globe.util.parse.*;
import vu.globe.util.security.UID;

import vu.globe.svcs.gids.client.*;

import vu.globe.svcs.gtrans.cfg.*;

import vu.globe.rts.runtime.cfg.Settings;

import netscape.ldap.LDAPException;


/**
 * This is the startup interface of the Globe translator. The Globe translator
 * is GIDS-enabled. It will contact GIDS to obtain its configuration
 * information and register itself with GIDS as a service.
 */

/*
 * GlobeTranslator first parses command line options, contacts GIDS, and then
 * creates a TCP listening socket. Next, it
 * creates a GlobeTranslator object that goes into an infinite loop waiting
 * for clients (i.e., Web browsers) to connect to the socket. For each
 * client a separate thread is created to handle the HTTP request.
 */
public class GlobeTranslator
{
  static final String PROGNAME = "GlobeTranslator";
  static final int BACK_LOG = 50;   // back-log for TCP listening

  /** Translator configuration settings. */
  public static TranslatorConfiguration config;

  /** Error log. */
  public static Log errorLog;

  /** Access log. */
  public static LogFileWriter accessLog;


  // The thread pool.
  private static TranslatorThreadPool _threadPool;


  public static void main(String[] argv)
  {
    int i, debugLevel;
    boolean logToConsole;
    String s, optArg, cfgFile;
    CfgFileReader cfgFileReader;
    GetOpt getOpt;
    char c;
    String user, site_dir;

    // GIDS
    GIDSSite gids = null;
    String gtrans_tag, portStr, admin, preferredIPStr, hostPort, embedTag;

    String host = null;
    int port = 0;
    InetAddress preferredIP = null;

    String ip_result;
    ServerSocket sock = null;

    if (argv.length < 1) {
      usage();
    }

    // Create translator configuration with its defaults.
    config = new TranslatorConfiguration();

    debugLevel = -1;
    logToConsole = false;
    user = null;

    getOpt = new GetOpt(argv, 0, "cd:u:");

    /*
     * Parse command line options.
     */
    try {
      while ( ( c = getOpt.getNextOpt()) != GetOpt.EOF) {
        switch(c) {
          case 'c':
            if (getOpt.getOptArg() != null) {
              usage();
            }
            logToConsole = true;
            break;

	    /*
          case 'd':
            if ( (optArg = getOpt.getOptArg()) == null) {
              usage();
            }

            try {
              debugLevel = Integer.parseInt(optArg);
            }
            catch(NumberFormatException e) {
              fatal("invalid debug level: " + e.getMessage());
            }
            break;
	    */

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

    config.setErrorLogFileName(site_dir + "/gtrans.error_log");
    config.setAccessLogFileName(site_dir + "/gtrans.access_log");

    /*
     * Initialise access to the GIDS
     */
    try {
      gids = new GIDSSite (site_dir);
      gids.authenticate();
      config.setGIDS (gids);
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
						   "gtransConfig"});
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
    
    required = new String[] {  };
    msg = Settings.getInstance().checkSettings("gtransConfig", required);  
    if (msg != null) {
      fatal("Error: "+msg);
    }
    */



    /*
     * Read the configuration file.
     */
    cfgFile = settings.getValue("gids.schema.configfile");
    if (cfgFile == null || cfgFile.equals("")) {
      cfgFile = "file://" + site_dir + "/gtrans.conf";
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
     * Override debug level in the configuration file with the one specified
     * in the Settings object, if it is set.
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
     * get translator configuration from Settings object
     */

    portStr = settings.getValue("gids.schema.port");
    try {
      port = Integer.parseInt(portStr);
    }
    catch(NumberFormatException e) {
      System.err.println("invalid port: " + e.getMessage());
      System.exit (-1);
    }

    gtrans_tag = settings.getValue("gids.schema.tag");

    preferredIPStr = settings.getValue("gids.schema.preferredIP");
    if (preferredIPStr != null) { // optional attribute
      try {
        preferredIP = InetAddress.getByName (preferredIPStr);
      }
      catch (UnknownHostException exc) {
        System.err.println (
	  "gtransConfig.preferredIP refers to unknown IP address " +
	    preferredIPStr);
	System.exit (-1);
      }
    }

    // optional => may be null
    admin = settings.getValue ("gids.schema.admin");

    // create the embedTag 

    host = settings.getValue("gids.schema.host");
    if (host == null) {
      fatal("gtransConfig.host must be defined in GIDS or " +
            "site configuration file");
    }
    if (port != 80) {
      hostPort = host + ":" + port;
    }
    else {
      hostPort = host;
    }
    config.setHostPort(hostPort);
    embedTag = config.getEmbeddedURNPrefix();



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
      fatal("cannot listen on" +
        (preferredIP == null ? "" : " address " + preferredIP) +
	" port " + port);
    }

    /*
     * Change the user ID of this process if necessary.
     */
    changeUserID(user);
    System.out.println("Translator user id: " + UID.geteuid());

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
     * Open access log.
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

    ip_result = (preferredIP == null ? "'any'" :
              sock.getInetAddress().toString());

    System.out.println("listening on ip " + ip_result);
    config.print();
  
    // Start the thread pool.
    _threadPool = new TranslatorThreadPool(config.getThreadPoolSize(), sock);

    /*
     * register with GIDS
     */
    HashMap reg = GIDSUtil.constructRegEntry ("gtrans", gtrans_tag, host,
      portStr, admin, null);
    reg.put ("embedTag", embedTag);
    String dn = GIDSUtil.constructDN (reg);
    settings.addSetting("myDN", dn);
    try { gids.register (reg); }
    catch (LDAPException e) {
      fatal("Unable to register with GIDS: " + e);
    }

    System.out.println("Globe translator ready.");

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
    System.out.println("Globe Translator is closed for business... goodbye!");

    // end.
    System.exit(1);
  }


  /**
   * Change the user ID of this process to the ID of the named user.
   * If the user is not specified, and the real user ID of this process
   * is 0 (superuser), the user ID is set to the process' effective
   * user ID. If, however, the effective user ID is also 0, the translator
   * exits with an error message.
   *
   * @param  user  user's login name or user ID
   */
  private static void changeUserID(String user)
  {
    try {
      if (user != null) {
        System.out.println("Setting user ID to user " + user);

//        try {
//          int uid = Integer.parseInt(user);

// <<<<<<< GlobeTranslator.java
//           setuid(uid);
//         }
//         catch(NumberFormatException e) {
//           setuid(user);
//         }
// =======
        UID.dropUserPriveleges(user);
// >>>>>>> 1.16
      }
      else {
        int uid = UID.getuid();
        int euid = UID.geteuid();

        /*
         * If this process is running in superuser mode, set the ID to
         * the real user ID. If the real user ID is 0 (superuser),
         * the translator exits, telling the user to specify the user
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
  public static void usage()
  {
    System.out.println("Usage:");
    System.out.println("  " + PROGNAME + " [options] site-dir");
    System.out.println();
    System.out.println("Parameters:");
    System.out.println("  site-dir : the directory with site data");
    System.out.println();
    System.out.println("Options:");
    System.out.println("  -c                 : mirror error and access log "
                       + "messages to console");
    //    System.out.println("  -d level           : set debug level (level 0 "
    //                   + "suppresses debugging output)");
    System.out.println("  -u user   : specify the user name to which the user "
                       + "ID of this process");
    System.out.println("              should be set. (default: do not "
                       + "change user ID)");
    System.exit(-1);
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
