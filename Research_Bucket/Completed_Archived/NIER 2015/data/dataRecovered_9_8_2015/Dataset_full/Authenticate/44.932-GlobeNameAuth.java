/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// GlobeNameAuth.java - The Globe Naming Authority

package vu.globe.svcs.gns.nameauth;


import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.HashMap;

import vu.globe.util.net.*;
import vu.globe.rts.runtime.cfg.Settings;

import vu.globe.svcs.gns.lib.DNS.*;
import vu.globe.svcs.gns.lib.namesrvce.*;

import vu.globe.util.log.Log;
import vu.globe.util.parse.GetOpt;

import vu.globe.svcs.gids.client.*;
import vu.globe.rts.runtime.cfg.*;
import vu.globe.util.parse.SettingFileException;
import netscape.ldap.LDAPException;

/**
 * This is the startup interface of the Globe Naming Authority. It first
 * parses command line options and then creates a TCP listening socket.
 * Next, it goes into an infinite loop waiting for a client to connect to
 * the socket. When a client connects, it processes the request, and then
 * waits for another client. As a result, at most one client is serviced
 * at a time.
 * <p>
 * On a multi-homed host the IP address to listen to may be specified with the
 * -Dglobe.rt.ip runtime setting.
 *
 * @see vu.globe.rts.runtime.cfg.Settings
 */
public class GlobeNameAuth
{
  static final String progName = "GlobeNameAuth";
  static final int BACK_LOG = 50;   // back-log for TCP listening


  public static void main(String argv[])
  {
    int i, j, recvPort, srvPort, debugLevel, numZnames;
    InetAddress srvAddr;
    Dname znames[];
    IPSelector trusted[];
    ServerSocket sock;
    Socket cliSock;
    boolean clog;
    String flogname, srv, zonename;
    Log log;
    String opt, optArg, znameStr;
    ClientHandler cliHandler;
    String trusted_spec;         // the -T option argument
    char c;
    GetOpt getOpt;

    if (argv.length < 1) {
      usage();
    }

    clog = false;
    flogname = null;
    recvPort = NameSrvceConfig.NAMEAUTHPORT;
    srvPort = NameSrvceConfig.NAMESRVPORT;
    debugLevel = 0;
    trusted_spec = null;

    getOpt = new GetOpt(argv, 0, "L:s:i");

    /*
     * Parse command line options.
     */
    try {
      while ( ( c = getOpt.getNextOpt()) != GetOpt.EOF) {
        switch(c) {
          case 'L':
            if ( (optArg = getOpt.getOptArg()) == null) {
              clog = true; 
            } else {
	      flogname = optArg;
	    }
            break;

          case 's':
            if ( (optArg = getOpt.getOptArg()) == null) {
              usage();
            }

            try {
              srvPort = Integer.parseInt(optArg);
            }
            catch(NumberFormatException e) {
              fatal("invalid port: " + e.getMessage());
            }
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
    if (i > argv.length - 1) {
      usage();
    }

    String site_dir = argv[i];


    /*
     * Initialise access to the GIDS
     */
    GIDSSite gids = null;
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
						   "nameAuthConfig"});
    Settings settings = Settings.getInstance();


    // set debuglevel
    try {
      debugLevel = Integer.parseInt(Settings.getDebugLevelSetting());
    }
    catch(NumberFormatException e) {
      // safely ignore the error.  debugLevel does not change.
    }

    srvAddr = null;
    sock = null;
    znames = null;
    srv = null;

    // preferredIP is optional
    String srvAddrStr = settings.getValue("gids.schema.preferredIP");
    if (srvAddrStr != null) { 
      try {
        srvAddr = InetAddress.getByName (srvAddrStr);
      }
      catch (UnknownHostException exc) {
        System.err.println (
	  "preferredIP attribute refers to unknown IP address " +
	    srvAddrStr);
	System.exit (-1);
      }
    } else {
      try {
        srvAddr = InetAddress.getLocalHost();
      }
      catch(UnknownHostException e) {
        fatal("cannot get IP address of this host");
      }
    }
    srv = srvAddr.getHostAddress();


    /*
     * Get authoritive zones and check if they are valid.
     */

    String znamesStr = settings.getValue("gids.schema.authZones");
    // authZones is required
    if (znamesStr == null) {
      fatal("no authorized zone names (authZones) specified in GIDS "+
	    "or site configuration file");
    }
    Vector znamesVec = new Vector();
    numZnames = 0;

    // split at spaces
    int lastSpace = 0;
    int nextSpace = 0;
    do {

      nextSpace = znamesStr.indexOf(" ", lastSpace);
      if (nextSpace < 0) {
	nextSpace = znamesStr.length();
      }
      znameStr = znamesStr.substring(lastSpace, nextSpace);

      if (znameStr.length() > 0) {
	Dname zname = null;
	try {
	  zname = new Dname(znameStr);
	  znamesVec.add(zname);
	  if (zname.isAbsolute() == false) {
	    fatal(zname.toString() + ": zone name is not fully-qualified");
	  }
	} 
	catch(MalformedDnameException e) {
	  fatal(znameStr + ": zone name is not a valid domain name: "
		+ e.getMessage());
	}
	numZnames++;
      }

      lastSpace = nextSpace+1;
    } while (lastSpace < znamesStr.length());

    znames = (Dname[])znamesVec.toArray(new Dname[0]);


    /*
     * Process the trusted IP ranges
     */

    trusted_spec = settings.getValue("gids.schema.trustedIPRanges");
    // trustedIPRanges is required but it may be empty
    trusted = processTrustedSpec (trusted_spec, znames);

    // summarise trusted spec for user
    for (int z = 0; z < znames.length; z++) {
      System.out.println ("trusted IP addresses of " + znames[z] + ": "
      			+ trusted[z]);
    }

    // tag is required
    String nsauth_tag = settings.getValue("gids.schema.tag");

    // port is optional (used to be -p option)
    String portStr = settings.getValue("gids.schema.port");
    if (portStr != null) {
      try {
	recvPort = Integer.parseInt(portStr);
      }
      catch(NumberFormatException e) {
	fatal("invalid port: " +portStr+" : "+ e.getMessage());
      }
    }

    // admin is optional
    String admin = settings.getValue("gids.schema.admin");

    /*
     * Create listening socket.
     */
    try {
      sock = new ServerSocket(recvPort, BACK_LOG, srvAddr);
    }
    catch(UnknownHostException e) {
      fatal("unknown name server host: " + e.getMessage());
    }
    catch(IOException e) {
      fatal("cannot listen on port " + recvPort + ": " + e.getMessage());
    }

    /*
     * Check if the name server is setup correctly.
     */
    checkNameSrv(srvAddr, srvPort, znames, debugLevel, true);

    /*
     * Create logger.
     */
    log = null;
    try {
      log = new Log(progName, clog, flogname);
    }
    catch(IOException e) {
      fatal("cannot create log: " + e.getMessage());
    }

    cliHandler = new ClientHandler(srvAddr, srvPort, znames, trusted, log,
                                   debugLevel);

    System.out.println("listening on port " + recvPort
                       + ", name server [" + srv + "]." + srvPort);

    System.out.print("zones:");
    for (i = 0; i < znames.length; i++) {
      System.out.print(" " + znames[i].toString());
    }
    System.out.println();


    /*
     * register with GIDS
     */
    // XXX should perhaps define nameAuthConfig.host, and use gids.schema.host
    // instead, see GlobeTranslator.java
    String host = srvAddr.getHostName();
    HashMap reg = GIDSUtil.constructRegEntry ("nameauth", nsauth_tag, host,
      recvPort+"", admin, null);
    String dn = GIDSUtil.constructDN (reg);
    settings.addSetting("myDN", dn);
    try { gids.register (reg); }
    catch (LDAPException e) {
      fatal("Unable to register with GIDS: " + e);
    }

    // run the Name Auth in a seaparate thread
    NsAuthRun r = new NsAuthRun(sock, cliHandler, log);
    Thread t = new Thread(r);
    t.start();

    System.out.println("NS Auth ready.");

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

    // Finish program
    System.out.println("NS Auth is closed for business... goodbye!");

    // end.
    System.exit(1);
  }


  public static class NsAuthRun implements Runnable 
  {
    ServerSocket sock;
    ClientHandler cliHandler;
    Log log;

    public NsAuthRun(ServerSocket sock, ClientHandler cliHandler, Log log) {
      this.sock = sock;
      this.cliHandler = cliHandler;
      this.log = log;
    }

    public void run() {
      /*
       * Wait for a client to connect, then process its request, and wait
       * for the next client.
       */
      Socket cliSock;
      for ( ; ; ) {
	try {
	  cliSock = sock.accept();

	  cliHandler.process(cliSock);
	  
	  cliSock.close();
	}
	catch(IOException e) {
	  log.write("socket accept() or close() failure: " + e.getMessage());
	}
	finally {
	  cliSock = null;
	}
      }
      
    }
    
  }




  /**
   * Display a usage message, then exit.
   */
  private static void usage()
  {
    System.out.println("Usage:");
    System.out.println("  " + progName + " [options] site-dir");
    // zone-names");
    System.out.println();
    System.out.println("Parameters:");
    System.out.println("  site-dir  : the directory with site data");
    //System.out.println("  zone-names  : fully-qualified DNS domain names of "
    //		       + "the zones for ");
    //System.out.println("                which name server is authoritative");
    System.out.println();
    System.out.println("Options:");

    //    System.out.println("  -d<level>    : set debug level (level 0 "
    //                   + "suppresses debugging output)");
    System.out.println("  -L log file  : log errors to a file");
    System.out.println("  -L           : log errors to screen");
    //    System.out.println("  -p<port>     : TCP listening port");
    System.out.println("  -s port      : name server listening port");
    //    System.out.println(
    //"  -T<trusted>  : trusted IP addresses. A list of IP ranges in order of");
    //    System.out.println(
    //"                 increasing priority, each preceded by a '+' (trusted) or");
    //    System.out.println(
    //"                 '-' (untrusted).");
    //    System.out.println(
    //"                 Example: -T' +0.1.2.0/24 +1.2.0.4&ffff00ff -1.2.3.4'");
    //    System.out.println(
    //"                 This example trusts 0.1.2.* and 1.2.*.4, except 1.2.3.4,");
    //    System.out.println(
    //"                 and distrusts any other address.");
    System.out.println("  -i           : initialize a zone (if necessary) to "
                       + "make it suitable for Globe");
    System.exit(1);
  }


  /**
   * Print an error message, then exit.
   */
  private static void fatal(String s)
  {
    System.err.println("cannot start " + progName + ": " + s);
    System.exit(-1);
  }


  /**
   * Contact the name server to get info from the root directory (i.e.,
   * the zone), and compare this info against what we expect.
   *
   * @param  srvAddr     name server address
   * @param  srvPort     name server listening port
   * @param  znames      zones for which name server should be authoritative
   * @param  debugLevel  resolver's debug level
   * @param  initZone    if set, initialize a zone if necessary to make it
   *                     suitable for Globe
   */
  private static void checkNameSrv(InetAddress srvAddr, int srvPort,
                                   Dname znames[], int debugLevel,
                                   boolean initZone)
  {
    Resolver resv;
    GlobeDirCookie gdc;
    Dname zname, zname2;
    int i;

    resv = new Resolver(srvAddr, srvPort);
    resv.setDebugLevel(debugLevel);
    gdc = null;
    zname2 = null;

    for (i = 0; i < znames.length; i++) {
      zname = znames[i];

      System.out.println ("checking zone " + zname + " through DNS server " +
          srvAddr.getHostAddress() + ":" + srvPort);

      try { 
        /*
         * Check if the globe domain refers to a directory, i.e. if it has
         * has a GLOBEDIR resource record.
         */
        gdc = GlobeDirCookie.getCookie(resv, zname);
      }
      catch(CookieNotFoundException e) {
        if (!initZone) {
          fatal(zname.toString() + " does not have a GLOBEDIR RR");
        }
        else {
          System.err.println();
          System.err.println("**********************************************");
          System.err.println("* " + zname.toString()
                             + " does not have a GLOBEDIR RR");
          System.err.println("* Making zone " + zname.toString()
                             + " suitable for Globe...");
          System.err.println("**********************************************");

          try {
            gdc = GlobeDirCookie.setCookie(resv, zname, zname, true);
          }
          catch(Exception e2) {
            fatal("cannot make zone " + zname.toString()
                  + " suitable for Globe: " + e2.getMessage());
          }

          /*
           * Note: instead of immediately continuing with the next zone,
           * we check below if the cookie is correct.
           */
        }
      }
      catch(CookieException e) {
        fatal(zname.toString() + " does not have a valid GLOBEDIR RR");
      }
      catch(Exception e) {
        fatal("cannot stat " + zname.toString() + ": " + e.getMessage());
      }

      zname2 = gdc.getZone();

      /*
       * Check if the zone name in the GLOBEDIR RR is fully qualified.
       */
      if (zname2.isAbsolute() == false) {
        fatal("GLOBEDIR RR of " + zname.toString() + " is corrupt: "
              + "zone field is not fully qualified");
      }

      /*
       * Check if the zone name in the GLOBEDIR RR is similar to the
       * zone for which the name server is authoritative.
       */
      if (zname.equals(zname2) == false) {
        fatal("zone mismatch: zone field in GLOBEDIR RR of "
              + zname.toString() + " is " + zname2.toString()
              + " (expected " + zname.toString() + ")");
      }

      /*
       * Check if I am the naming authority for the zone. Note that srvAddr
       * is not only the name server address but also my own.
       */
      if (srvAddr.equals(gdc.getNameAuthAddr()) == false) {
        fatal("the naming authority for zone " + zname
              + " must be run at address "
              + gdc.getNameAuthAddr().getHostAddress()
              + " rather than " + srvAddr.getHostAddress());
      }

      // MIRA check if we are authorized to send update messages?
    }
  }


  /**
   * Processes the specification of trusted IP addresses 
   *
   * @param spec	the specification, or null if none was given
   * @param znames	the zones for which this NA is authoritative
   * @return	for each zone znames[i], the IP selector at index i has the
   *		set of addresses from which the zone may be updated
   */
  private static IPSelector[] processTrustedSpec (String spec, Dname[] znames)
  {
    // the return value
    IPSelector[] trusted = new IPSelector [znames.length];

    if (spec == null) {     // No -T option passed.
      // In this special case all IP addresses are trusted.

      System.out.println ("Warning: trusting all IP addresses!");

      // assign the same trusting IPSelector to all zones
      IPSelector ip_sel = new MaskIPSelector ("0.0.0.0&0");
      for (int i = 0; i < znames.length; i++)
        trusted [i] = ip_sel;
      
      return trusted;
    }

    // Trusted spec must have one of two formats:
    // Format A (one ip selector for all zones):
    //   OrderedIPSelector
    // Format B (ip selector for each zone):
    //   zone1 : OrderedIPSelector1 ,  zone2 : OrderedIPSelector2, ...

    // Format A

    if (spec.indexOf (':') == -1) {

      // one OrderedIPSelector for all zones
      try {
        OrderedIPSelector ip_sel = new OrderedIPSelector (spec);
        for (int i = 0; i < znames.length; i++)
          trusted [i] = ip_sel;
      }
      catch (IllegalArgumentException e) {
        fatal (spec + ": " + e.getMessage());
      }
      return trusted;
    }

    // Format B

    StringTokenizer toks = new StringTokenizer (spec.trim(), ":,", true);

    while (toks.hasMoreTokens()) {
	      
      // Step 1: get a zone string and an ip selector string from toks
      // Step 2: map a zone to an IPSelector

      // Step 1

      // read zone
      String zone = toks.nextToken().trim();

      // read ':'
      if (! toks.hasMoreTokens())
        fatal ("'" + spec + "': incomplete (expected ':')");
      String sep = toks.nextToken();
      if (! sep.equals (":"))
        fatal ("'" + spec + "': expected ':' instead of '" + sep + "'");

      // read ip selector string: ip_sel_str
      if (! toks.hasMoreTokens())
        fatal ("'" + spec + "': incomplete (expected IP address set)");
      String ip_sel_str = toks.nextToken();

      // finish parsing this round by reading ',' or end of spec
      if (toks.hasMoreTokens()) {
        sep = toks.nextToken();
        if (! sep.equals (","))
          fatal ("'" + spec + "': expected ',' instead of '" + sep + "'");
        if (! toks.hasMoreTokens())
          fatal ("'" + spec + "': incomplete (expected another zone)");
      }

      // Step 2

      // search for the index of zone in znames
      int zindex = -1;
      for (int i = 0; i < znames.length; i++) {
        if (zone.equals (znames[i].toString())) {
          zindex = i;
	  break;
        }
      }
      if (zindex == -1) {
        fatal ("'" + spec + "': not authoritative for zone " + zone);
      }
      if (trusted [zindex] != null) {
        fatal ("'" + spec + "': zone " + zone + " appears several times");
      }

      try {
        trusted [zindex] = new OrderedIPSelector (ip_sel_str);
      }
      catch (IllegalArgumentException e) {
        fatal ("'" + spec + "': " + e.getMessage());
      }
    } // end of while

    // check that all zones have been assigned
    for (int i = 0; i < znames.length; i++) {
      if (trusted [i] == null) {
        fatal ("'" + spec + "': zone " + znames[i] + " has not been specified");
      }
    }
    return trusted;
  }
}
