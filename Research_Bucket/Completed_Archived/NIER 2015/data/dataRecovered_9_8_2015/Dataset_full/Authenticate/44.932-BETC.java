/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

/**
 *
 * GDN Back-End Traceability Checker (BETC)
 *
 * A BETC server verifies the traceability of the content of GDN objects
 * in a particular object server.
 *
 * Author: Arno Bakker
 *
 * Usage:
 * - when object server is cleared (rm -r gos) BETC should be restarted
 *   without persistent state as well.
 */

package vu.globe.tools.betc;

import java.io.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.naming.NamingException; // used by X509Entity
import java.net.UnknownHostException;

import vu.globe.idlsys.g;              // g.idl
import vu.globe.rts.std.idl.stdInf.SOInf;
import vu.globe.rts.std.idl.stdInf.SCInf;
import vu.globe.rts.std.idl.stdInf.SOIerrors;
import vu.globe.rts.std.StdUtil;
import vu.globe.rts.java.GInterface;
import vu.globe.rts.runtime.ns.idl.nsTypes.*; // nsTypes.idl
import vu.globe.rts.runtime.ns.nsConst;
import vu.globe.rts.runtime.ns.idl.context;
import vu.globe.rts.runtime.ns.idl.ns.*;      // ns.idl
import vu.globe.rts.runtime.ns.nameSpaceImp.nameSpaceImp;

import vu.globe.rts.runtime.cfg.idl.rtconfig.*;  // rtconfig.idl

import vu.globe.util.comm.idl.rawData.rawDef;
import vu.globe.util.comm.RawOps;

import vu.globe.svcs.gls.types.PropertyMap;
import vu.globe.svcs.gls.types.PropertySelector;
import vu.globe.svcs.gls.idl.lsClient.idManager;
import vu.globe.svcs.gls.idl.lsClient.propertySelector;
import vu.globe.svcs.gls.idl.lsClient.contactAddressInfo;
import vu.globe.util.idltypes.ObjectHandleOps;
import vu.globe.util.idltypes.PropertySelectorOps;

import vu.globe.svcs.objsvr.types.ObjSvrAddr;
import vu.globe.svcs.objsvr.idl.management.objectServerOps;
import vu.globe.svcs.objsvr.idl.management.localReprIDList;
import vu.globe.svcs.objsvr.idl.management.localReprStat;
import vu.globe.svcs.objsvr.idl.management.osvrMgrError;
import vu.globe.svcs.objsvr.idl.gosClient.gosClientContact;

import vu.globe.tools.tika.util.IdlTypesTools;
import vu.globe.tools.tika.util.Binder;
import vu.globe.tools.tika.util.ObjSvrClients;
import vu.globe.tools.tika.util.AGORecord;

import vu.globe.apps.gdn.util.GdnCryptoUtil;
import vu.globe.apps.gdn.util.GdnACSClientSet;
import vu.globe.apps.gdn.util.GdnACSClientException;
import vu.globe.apps.gdn.util.GdnCannotDetermineTraceabilityException;
import vu.globe.apps.gdn.util.X509Entity;
import vu.globe.tools.betc.GdnContentRemovalPolicy;

import vu.globe.svcs.gids.client.*;
import vu.globe.rts.runtime.cfg.*;
import vu.globe.util.parse.SettingFileException;
import vu.globe.util.parse.GetOpt;
import netscape.ldap.LDAPException;

import vu.globe.rts.security.idl.sectypes.*;
import vu.globe.rts.security.BaseKeyMaterial;
import vu.globe.rts.security.certs.IdentCertUtil;
import vu.globe.rts.security.certs.RightsCertUtil;
import vu.gaia.apps.gdn.Package;
import vu.gaia.apps.gdn.PackageException;
import vu.gaia.apps.gdn.getFileTraceInfoResults;


// TODO:
// V do chain checking on cert via trustedAGO keystore ["and AGOCA" REMOVED]
// * apply object-server owner's content-removal policy when producer accused or blocked
// * unbind when sig broken



public class BETC
{
    
    static final String PROGNAME = "BETC";
    public static final int DEFAULT_BLOCKSIZE_READ = GdnCryptoUtil.DEFAULT_BLOCKSIZE_READ;
    public static final int BETC_CHECK_TRACE_MODE = 1;
    public static final int BETC_CHECK_JUSTPRODUCERSTATUS_MODE = 2;
    public static int   BETCBindCount=0;

    static IDLKeyIdentChainPair	myKP=null;
    static IDLIdentCerts		caCerts=null;
    
    public static void main( String[] argv )
    {
        boolean     securityEnabled=true;
        String 		securityFile=null;
        String		objectservertocheck=null;
        PrintWriter	shout=null;
        PrintWriter	sherr=null;
        
        Binder   	binder=null;
        idManager	glsIDManager=null;
        int 		main_ctx=0;
        nameSpace 	localNameSpace=null;
        objectServerOps goscli=null;
        
        String 		downloadedfile=null;
        GdnBETCConfig 	config = null;
        com.sun.net.ssl.TrustManager[] tmlist;
        GdnACSClientSet acsclientset=null;
        SimpleDateFormat 	df=null;
        
        GIDSSite gids = null;
        
        
        // enable logging to stdout
        shout = new PrintWriter( System.out, true );
        sherr = shout;
        
        df = new SimpleDateFormat( "EEE, dd MMM yyyyy hh:mm:ss z" );
        
        /*
         * Parse command line options.
         */
        GetOpt getOpt = new GetOpt(argv, 0, "N");
        try
        {
            char c=0;
            while ( ( c = getOpt.getNextOpt()) != GetOpt.EOF)
            {
                switch(c)
                {
                    case 'N':
                        if (getOpt.getOptArg() != null)
                        {
                            usage( sherr );
                        }
                        securityEnabled = false;
                        break;
                        
                    default:
                        usage( sherr );
                }
            }
        }
        catch(IOException e)
        {
            fatal("cannot process command line option: " + e.toString());
        }
        
        if (securityEnabled)
        {
            // Load in the providers BEWARE THAT THE ORDER IS IMPORTANT !!
            // SSL NEEDS the Cryptix provider does not work with BouncyCastle!
            Security.addProvider(new cryptix.provider.Cryptix());
            Security.addProvider(
                    new COM.claymoresystems.provider.ClaymoreProvider());
            Security.addProvider(
                    new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
        
        int i = getOpt.getIndex(); // i indexes the remaining args within argv
        
        // site-dir: required argument
        if (i >= argv.length)
        {
            usage( sherr );
        }
        String site_dir = argv[i++];
        
        /*
         * Initialise access to the GIDS
         */
        try
        {
            gids = new GIDSSite(site_dir);
            // BETC only needs to authenticate if it writes to GIDS
            // (i.e., if it registers itself with GIDS).  this is
            // not the case)
            // gids.authenticate();
        }
        catch (SettingFileException e)
        {
            fatal("error reading file: " + e.toString());
        }
        catch (FileNotFoundException e)
        {
            fatal("unable to find file: " + e.toString());
        }
        catch (IOException e)
        {
            fatal("unable to read file: " + e.toString());
        }
        catch (LDAPException e)
        {
            fatal("unable to contact and/or authenticate to GIDS: " + e);
        }
        
        // Initialise the Settings object.
        Settings.initGIDSSettings(gids, new String[]
        { "baseRegionConfig",
          "BETCConfig"});
          Settings settings = Settings.getInstance();
          
          // check whether clientca setting is set
          String clientca = settings.getValue("gids.schema.clientca");
          if (clientca == null)
          {
              fatal("baseRegionConfig.clientca must be defined in GIDS or"+
              " site configuration file");
          }
          
          // override globeca so that clientca will be used instead.
          settings.addSetting("gids.schema.globeca", clientca);
          
          // get objectserver info
          String gosHost = settings.getValue("gids.schema.gosHost");
          String gosPort = settings.getValue("gids.schema.gosPort");
          if (gosHost == null)
          {
              fatal("BETCConfig.gosHost (or GOSConfig.host) must be " +
              " defined in GIDS or site configuration file");
          }
          if (gosPort == null)
          {
              fatal("BETCConfig.gosPort (or GOSConfig.tlsClientAuthCpPort)"+
              " must be defined in GIDS or site configuration file");
          }
          
          objectservertocheck = gosHost + ":" + gosPort;
          
          if (securityEnabled)
          {
              securityFile = settings.getValue("gids.schema.securityFile");
              if (securityFile == null)
              {
                  fatal("BETCConfig.securityFile must be defined in GIDS or"+
                  " site configuration file");
              }
          }

          String configFile =
          settings.getValue("gids.schema.configFile");
          if (configFile == null)
          {
              configFile = site_dir + File.separator + "betc.cfg";
          }
          
          try
          {
              config = GdnBETCConfig.read( configFile );
          }
          catch( Exception e )
          {
              sherr.println( "Error: " + e.toString() );
              e.printStackTrace();
              System.exit(-1);
          }
          
        /*
          Arno: I need to set the Java -Djavax.net.ssl.trustStore=
          to the object-server owner's trusted AGO keystore. Reason
          is that JNDI (LDAP interface) that is used to access the
          GDN ACS servers is done over TLS/SSL and I only have control
          over the keystore it retrieves its Certification Authority
          certificates from via this Java option.
         */
          String fname =config.getObjectServerOwnerTrustedAGOsFileName();
          if (fname != null)
          {
              System.setProperty("javax.net.ssl.trustStore", fname);
          }
          
          localNameSpace = nameSpaceImp.getLNS();
          
          try
          {
            /*
             * Create a local name space context for this process: ":/GDN-BETC".
             */
              main_ctx = localNameSpace.create(context.NsRootCtx, "GDN-BETC");
          }
          catch(Exception e)
          {
              sherr.println("Cannot create local name space");
              sherr.println( "Error: " + e.toString() );
              e.printStackTrace();
              System.exit(-1);
          }
          
          
          if (securityEnabled)
          {
              /*
               * Check if the security file is a normal file.
               */
              switch(checkFile(securityFile, true))
              {
                  case 0:                                                 // OK
                      break;
                  case -1:
                      fatal( sherr, "no such security file: " + securityFile + "\n"
                      + "Please fix (or add) the security file setting in BETC's "
                      + "configuration file.");
                  case -2:
                      fatal( sherr, "security file refers to a directory: " + securityFile + "\n"
                      + "Please fix (or add) the security file setting in BETC's "
                      + "configuration file.");
                  case -3:
                      fatal( sherr, "security file is not a regular file: " + securityFile + "\n"
                      + "Please fix (or add) the security file setting in BETC's "
                      + "configuration file.");
              }

              /*
               * Add the settings defined in the security file to the Globe runtime.
               * These settings are needed by Globe's communication objects.
                       */
              try
              {
                  setSecurityFileSetting(localNameSpace, securityFile);
              }
              catch(Exception e)
              {
                  fatal( sherr, "Cannot add security settings from " + securityFile
                  + ": " + e.toString());
              }
          }

          // Configure Globe binding stuff
          binder = new Binder();
          
          try
          {
              glsIDManager = binder.getLocationService();
          }
          catch(Exception e)
          {
              sherr.println( "Error: " + e.toString() );
              e.printStackTrace();
              System.exit(-1);
          }
          
          
          
          //
          // Get trust managers for trace check
          //
          tmlist = config.getObjectServerOwnerTrustedAGOTrustManagerList();
          
          acsclientset = config.getACSClientSet();
          
          downloadedfile = config.getTempDir()+"/downloadedfile";

       
          // MOET
          try
          {
              BaseKeyMaterial bkm = BaseKeyMaterial.loadBaseKeyMaterial();
              myKP = IdentCertUtil.keyStoreToIDLKeyIdentChainPair( bkm.baseKeyStore, bkm.basePassphrase );
              caCerts = IdentCertUtil.keyStoreToIDLIdentCerts( bkm.caKeyStore );
              
              IDLSecurityContext secctx = RightsCertUtil.baseKeyMaterialToIDLSecurityContext( bkm );
              binder.setSecurityContext( secctx );
          }
          catch( Exception e )
          {
              sherr.println( "Error: " + e.toString() );
              e.printStackTrace();
              System.exit(-1);
          }
       
          //
          // Create client for querying object server
          //

          try
          {
              goscli = connectToObjectServer( objectservertocheck, main_ctx, localNameSpace, sherr );
          }
          catch(Exception e)
          {
              sherr.println( "Error: " + e.toString() );
              e.printStackTrace();
              System.exit(-1);
          }
          
          
          // LOOP
          shout.println( "BETC: Entering mainloop." );
          ArrayList	newstatlist=null;
          ArrayList	oldstatlist=null;
          ArrayList	tocheckstatlist=null;
          int		traceCheckCount=0;
          ArrayList rechecklaterstatlist=null;
          try{
              
          while( true )
          {
              shout.println( "\n\n\n\n============================================================================" );
              Date currentTime = new Date();
              shout.println( df.format( currentTime ) );
              
              try
              {
                  oldstatlist = newstatlist;
                  newstatlist = composeLRStatusList( goscli, config, glsIDManager, shout, sherr );
                  tocheckstatlist = diffLRStatusLists( oldstatlist, newstatlist );
              }
              catch( Exception e )
              {
                  sherr.println( "BETC: Sleeping 1 minute before reattempting." );
                  try
                  {
                      // Sleep a while before trying again
                      Thread.currentThread().sleep( 60*1000 );
                      
                      //
                      // Handle objsvr reboots
                      //
                      if (goscli != null)
                          goscli.relInf();
                      goscli = null;
                      goscli = connectToObjectServer( objectservertocheck, main_ctx, localNameSpace, sherr );
                  }
                  catch( InterruptedException ie )
                  {
                      sherr.println( "Error: " + ie.toString() );
                  }
                  catch( UnknownHostException x)
                  {
                      // If this happens, DNS is being actively broken as we speak
                      sherr.println( "Error: " + x.toString() );
                      x.printStackTrace();
                      System.exit(-1);
                  }
                  
                  continue;
              }
              
              
              // caching done only per check of list, not for as long as BETC lives
              Hashtable acslookupcache = new Hashtable();
              
              if (traceCheckCount == config.getProducerStatusRecheckAfter())
              {
                  shout.println( "BETC: time to recheck producers' status, "  + newstatlist.size() + " LRs to check." );
                  
                  checkListOfLRs( BETC_CHECK_JUSTPRODUCERSTATUS_MODE, newstatlist, config, goscli, glsIDManager, binder, tmlist, acsclientset, acslookupcache, downloadedfile, df, shout, sherr );
                  traceCheckCount = 0;
              }
              else
              {
                  shout.println( "BETC: #LRs to check is " + tocheckstatlist.size() + "/" + newstatlist.size() );
                  
                  
                  rechecklaterstatlist = checkListOfLRs( BETC_CHECK_TRACE_MODE, tocheckstatlist, config, goscli, glsIDManager, binder, tmlist, acsclientset, acslookupcache, downloadedfile, df, shout, sherr );
                  traceCheckCount++;
              }
              
              // If the content-removal policy is not to unbind
              // when an error occurs during a trace check,
              // rechecklaterstatlist contains the stat record of
              // LRs on which a traceability check failed.
              //
              // These LRs should be rechecked at the next occasion
              // as the errors may have been transient (e.g. ACS server unreachable)
              //
              
              removeUncheckedLRsFromNewStatList( newstatlist, rechecklaterstatlist, shout, sherr );
              
              // Do real check directly after producer status check
              if (traceCheckCount == 0)
                  continue;
              
              // Sleep until next check
              // TODO: measure elapsed time of checking and do something when it
              // longer than the interval. I.e. BETC should check ever X seconds,
              // and not just have X seconds between checks.
              
              shout.println( "BETC: Sleeps until time for next check..." + config.getTraceCheckInternval() + " seconds." );
              try
              {
                  Thread.currentThread().sleep( 1000*config.getTraceCheckInternval() );
              }
              catch( InterruptedException e )
              {
                  sherr.println( "Error: " + e.toString() );
              }
          }
          }
          catch( Exception e )
          {
              sherr.println( "BETC: Exception in mainloop:\n" );
              e.printStackTrace();
          }
    }
    
    
    
    private static objectServerOps connectToObjectServer( String ObjectServer, int	main_ctx, nameSpace lns, PrintWriter sherr )
    throws UnknownHostException
    {
        ObjSvrAddr 	osaddr= new ObjSvrAddr( ObjectServer, ObjSvrAddr.SECURE_IDENT_CP );
        objectServerOps goscli=null;
        
        //
        // Create client for querying object server
        //
        try
        {
            SOInf cosoi = lns.bind( context.NsRootCtx,
            ":/repository/JAVA;vu.globe.svcs.objsvr.client.GOSClientCO");
            SCInf sci = (SCInf) cosoi.swapInf( SCInf.infid );
            SOInf soi = StdUtil.createGlobeObject( sci, context.NsRootCtx,
                                                   "GOSClient"); 
            sci.relInf();
            gosClientContact cli_contact = (gosClientContact)soi.getInf( gosClientContact.infid );
            goscli = (objectServerOps) soi.swapInf( objectServerOps.infid );
            cli_contact.connectWithIdent( osaddr.getAddrTuple(), myKP, caCerts );

            return goscli;
        }
        catch( Exception e )
        {
            sherr.println( "Error: " + e.toString() );
            e.printStackTrace();
            sherr.println( "BETC: continues..." );
            return null;
        }
    }
    
    
    
    private static ArrayList composeLRStatusList( objectServerOps GOSCLI, GdnBETCConfig Config, idManager GLSIDManager, PrintWriter shout, PrintWriter sherr )
    throws
    Exception
    {
        //
        // Obtain current list of LRs in object server
        //
        localReprIDList newlist =null;
        try
        {
            newlist = GOSCLI.listAll();
        }
        catch( Exception e )
        {
            sherr.println( "Error: cannot get LR list from object server: " + e.toString() );
            throw new Exception();
        }
        
        //
        // Compose list of status records.
        // Due to concurrency with the object server, LRs may have been deleted
        // before we try to stat them.
        //
        ArrayList newstatlist = new ArrayList();
        int i=0;
        for (i=0; i<newlist.v.length; i++)
        {
            long lrid = newlist.v[i];
            
            //
            // Get LR's status
            //
            localReprStat stat=null;
            try
            {
                stat = GOSCLI.statLR( lrid );
                if (stat.lrid != lrid)
                {
                    sherr.println( "Error: returned status info contains different lrid." );
                    makeServerUnbindWhenRequiredByPolicy( null, lrid, stat.ohandle, Config, GOSCLI, GLSIDManager, shout, sherr );
                    continue;
                }
                else
                    newstatlist.add( stat );
            }
            catch( Exception e )
            {
                sherr.println( "Error: cannot stat LR: " + e.toString() );
                makeServerUnbindWhenRequiredByPolicy( null, lrid, null, Config, GOSCLI, GLSIDManager, shout, sherr );
                continue;
            }
        }
        
        return newstatlist;
    }
    
    
    private static ArrayList diffLRStatusLists( ArrayList OldStatList, ArrayList NewStatList )
    {
        // To reduce the amount of work the previous list is
        // compared to the current list and only new or updated
        // LRs are checked.
        //
        // LRIDs are never reused. A new LRID is allocated for
        // each LR and the object server keeps track of which
        // LRIDs it issued. This information persists over reboots
        // so as long as the object server state is not all
        // thrown away (rm -r gos), LRIDs are unique.
        //
        // Implication is that when an object server is cleared
        // BETC should be rebooted and any persistent storage
        // of LR info should be deleted as well.
        //
        // So if LRIDs match in the lists, and the state has not
        // been updated since the last check, the LR need not be
        // checked. What may happen, however, is that LRs are restored to
        // a previous state as part of fault-tolerance. In that
        // case we can encounter an LR whose update time is less
        // than at a previous occasion.
        //
        // We must NOT assume that the content is such a reincarnated
        // LR is traceable. The BETC's content-removal policy may
        // have changed, the producer may have been blocked since
        // then, etc. So if the time of last state update differ
        // from what BETC saw before, we check.
        //
        
        ArrayList tocheckstatlist = new ArrayList();
        
        int i=0;
        for (i=0; i<NewStatList.size(); i++)
        {
            localReprStat newstat = (localReprStat)NewStatList.get( i );
            boolean found=false;
            int j=0;
            
            // just copy newstatlist to tocheckstatlist if OldStatList not set,
            // which happens when BETC is started clean.
            if (OldStatList != null)
            {
                for (j=0; j<OldStatList.size(); j++)
                {
                    localReprStat oldstat = (localReprStat)OldStatList.get( j );
                    if (newstat.lrid == oldstat.lrid)
                    {
                        // Old LR found, check update times
                        // Do simple equality check, such that
                        // time warps due to fault tolerance
                        // are also handled correctly.
                        //
                        if (newstat.lsmtime != oldstat.lsmtime)
                        {
                            tocheckstatlist.add( newstat );
                        }
                        found=true;
                        break;
                    }
                }
            }
            
            if (found == false)
            {
                // LR is a new LR
                tocheckstatlist.add( newstat );
            }
        }
        
        return tocheckstatlist;
    }
    
    
    
    private static void removeUncheckedLRsFromNewStatList( ArrayList NewStatList, ArrayList ReCheckLaterStatList, PrintWriter shout, PrintWriter sherr )
    {
        int i=0, j=0;
        for (j=0; j<ReCheckLaterStatList.size(); j++)
        {
            localReprStat re = (localReprStat) ReCheckLaterStatList.get( j );
            
            for (i=0; i<NewStatList.size(); i++)
            {
                localReprStat news = (localReprStat)NewStatList.get( i );
                if (re.lrid == news.lrid)
                {
                    // should be rechecked, remove
                    NewStatList.remove( i );
                    
                    shout.println( "BETC: will recheck LR " + re.lrid );
                    break;
                }
            }
        }
    }
    
    
    
    
    private static ArrayList checkListOfLRs( int BETCMode,
    ArrayList StatList,
    GdnBETCConfig Config,
    objectServerOps GOSCLI,
    idManager GLSIDManager,
    Binder BinderObj,
    com.sun.net.ssl.TrustManager[] TMList,
    GdnACSClientSet ACSClientSet,
    Hashtable ACSLookupCache,
    String DownloadedFile,
    SimpleDateFormat DateFormatter,
    PrintWriter shout,
    PrintWriter sherr )
    {
        ArrayList toRecheckStatList = new ArrayList();
        
        int i=0;
        for (i=0; i<StatList.size(); i++)
        {
            localReprStat stat = (localReprStat) StatList.get( i );
            
            shout.println( "----------------------------------------------------------------------------" );
            Date currentTime = new Date();
            shout.println( DateFormatter.format( currentTime ) );
            String objecthandlestr = ObjectHandleOps.objectHandleToString( stat.ohandle, GLSIDManager );
            
            shout.println( "BETC: Checking LRID " + stat.lrid + " OH " + objecthandlestr );
            shout.println( "BETC: LR saw last state update on " +  DateFormatter.format( new java.util.Date( stat.lsmtime )) );
            
            //
            // Select the address to which clients should bind
            //
            g.opaque clientcaddr = selectClientContactAddress( stat.caddrs.v, GLSIDManager, sherr );
            if (clientcaddr == null)
            {
                sherr.println( "Error: could not find client address in LR's contact addresses." );
                if (!makeServerUnbindWhenRequiredByPolicy( null, stat.lrid, stat.ohandle, Config, GOSCLI, GLSIDManager, shout, sherr ))
                {
                    // no unbind was issued, make sure object is rechecked later
                    toRecheckStatList.add( stat );
                }
                continue;
            }
            
            
            //
            // Bind and get package interface
            //
            SOInf lrInf=null;
            try
            {
                lrInf = BinderObj.bindCAddr( stat.ohandle, clientcaddr );
                BETCBindCount++;
                //shout.println( "BETC: bound to " + BETCBindCount + "++ objects" );
            }
            catch( Exception e )
            {
                sherr.println( "Error: could not bind to " + ObjectHandleOps.objectHandleToString( stat.ohandle, GLSIDManager ) + " LRID " + stat.lrid + "." + e.toString() );
                e.printStackTrace();
                if (!makeServerUnbindWhenRequiredByPolicy( null, stat.lrid, stat.ohandle, Config, GOSCLI, GLSIDManager, shout, sherr ))
                {
                    // no unbind was issued, make sure object is rechecked later
                    toRecheckStatList.add( stat );
                } 
                
                continue;
            }

            // As we're not using deputies here, we need to hack our way
            // to the control subobject we want.
            vu.globe.rts.lr.mgr.LRMgr realLR = (vu.globe.rts.lr.mgr.LRMgr)lrInf.soi.init_s;
            Package pkgInf = (Package)realLR.getControlObject();

            
            //
            // Read list of files from object
            //
            shout.println( "BETC: Asking object for list of files...." );
            String[] names = null;
            try
            {
                names = pkgInf.allFiles();
            }
            catch( Exception e )
            {
                sherr.println( "Error: could not get list of object contents " + ObjectHandleOps.objectHandleToString( stat.ohandle, GLSIDManager ) + " LRID " + stat.lrid + "." + e.toString() );
                if (!makeServerUnbindWhenRequiredByPolicy( lrInf, stat.lrid, stat.ohandle, Config, GOSCLI, GLSIDManager, shout, sherr ))
                {
                    // no unbind was issued, make sure object is rechecked later
                    toRecheckStatList.add( stat );
                    unbindOurselves( lrInf, shout );
                }
                pkgInf = null;
                continue;
            }
            
            
            //
            // Check files
            //
            int j=0,temp=0;
            boolean addedToRecheckList=false;
            boolean betcAlreadyUnboundItself=false;
            for( j=0; j<names.length; j++)
            {
                temp=j+1;
                shout.println( "\nBETC: Checks " + names[j] + " (" + temp + "/" + names.length + ")" );
                
                int traceresult = GdnCryptoUtil.CONTENT_UNTRACEABLE;
                try
                {
                    if (BETCMode == BETC_CHECK_TRACE_MODE)
                        traceresult = testFileTraceab( names[j], pkgInf, TMList, ACSClientSet, ACSLookupCache, DownloadedFile, objecthandlestr, shout, sherr );
                    else
                        traceresult = testProducerStatus( names[j], pkgInf, ACSClientSet, ACSLookupCache, DownloadedFile, objecthandlestr, shout, sherr );
                }
                catch( GdnCannotDetermineTraceabilityException e )
                {
                    // Error occurred. If the policy requires BETC to
                    // let the object server unbind we should not
                    // go on checking files, so we break if an unbind
                    // has been issued.
                    //
                    if (makeServerUnbindWhenRequiredByPolicy( lrInf, stat.lrid, stat.ohandle, Config, GOSCLI, GLSIDManager, shout, sherr ))
                    {
                        betcAlreadyUnboundItself=true;
                        break;
                    }
                    else
                    {
                        // no unbind was issued, make sure object is rechecked later
                        if (!addedToRecheckList)
                        {
                            toRecheckStatList.add( stat );
                            addedToRecheckList=true;
                        }
                        continue;
                    }
                }
                catch( GdnACSClientException ae )
                {
                    // Transient error occurred. If the policy requires BETC to
                    // let the object server unbind we should not
                    // go on checking files, so we break if an unbind
                    // has been issued.
                    //
                    // TODO: at present this is the only transient errors,
                    // there may be others that qualify (e.g. unable to stat
                    // an LR)
                    
                    if (transientErrorHaveServerUnbindWhenRequiredByPolicy( lrInf, stat.lrid, stat.ohandle, Config, GOSCLI, GLSIDManager, shout, sherr ))
                    {
                        betcAlreadyUnboundItself=true;
                        break;
                    }
                    else
                    {
                        // no unbind was issued, make sure object is rechecked later
                        if (!addedToRecheckList)
                        {
                            toRecheckStatList.add( stat );
                            addedToRecheckList=true;
                        }
                        continue;
                    }
                }
                catch( PackageException iue ) // gdnPackage.GDNErrors_inUse
                {
                    if (iue.toString().equals( PackageException.E_INUSE ))
                    {
                        // the file we attempted to check was being uploaded,
                        // we will continue to check the other files and
                        // recheck this object next time round:
                        if (!addedToRecheckList)
                        {
                            toRecheckStatList.add( stat );
                            addedToRecheckList=true;
                        }
                    }
                    else
                    {
                        sherr.println( "Error: got PackageException which was not INUSE: " + iue.toString() );
                    }
                    continue;
                }
                
                
                if (traceresult != GdnCryptoUtil.CONTENT_TRACEABLE)
                {
                    // This means that either
                    // * the file is positively traceable but the producer not active
                    // * the file is positively *NOT* traceable
                    // *
                    // Whether or not BETC makeServers an unbind in the first case
                    // is subject to policy. Latter case always leads to an unbind
                    //
                    GdnContentRemovalPolicy pol = Config.getContentRemovalPolicy();
                    if (!pol.refuseContentWhenProducerAccused && traceresult == GdnCryptoUtil.CONTENT_TRACEABLE_BUT_PRODUCER_ACCUSED)
                    {
                        // policy is to remove when blocked, and the producer
                        // is just accused, so no unbind
                        
                        // no need to have LR rechecked later
                    }
                    else
                    {
                        // Instruct object server to unbind from object, as it
                        // POSITVELY has untraceable content
                        shout.println( "BETC: Found UNTRACEABLE CONTENT, instructing object server to unbind..." );
                        
                        // unbind from obj ourselves before instructing object server
                        unbindOurselves( lrInf, shout );
                        pkgInf = null;
                        betcAlreadyUnboundItself=true;
                        
                        makeServerUnbind( stat.lrid, stat.ohandle, GOSCLI, GLSIDManager, shout, sherr );
                        break; // from file loop
                    }
                }
            }
            
            if (!betcAlreadyUnboundItself)
            {
                // unbind from obj
                unbindOurselves( lrInf, shout );
                pkgInf = null;
            }
        }
        
        shout.println( "----------------------------------------------------------------------------" );
        return toRecheckStatList;
    }
    
    
    
    private static boolean makeServerUnbindWhenRequiredByPolicy( GInterface Inf,
    long LRID,
    g.opaque ObjectHandle,
    GdnBETCConfig Config,
    objectServerOps GOSCLI,
    idManager GLSIDManager, // for print OHs
    PrintWriter shout,
    PrintWriter sherr )
    {
        // POLICY DECISION:
        // If the refuseContentWhenTraceabilityUnknown
        // part of the content-removal policy
        // is false, BETC will not makeServer an unbind if there is no
        // positive evidence that a file in the object
        // is not traceable. This means that if errors
        // occurred while determining traceability
        // this will not lead to an unbind.
        
        GdnContentRemovalPolicy pol = Config.getContentRemovalPolicy();
        if (pol.refuseContentWhenTraceabilityUnknown)
        {
            shout.println( "BETC: Unbinding from object after error determining traceability, as per the content-removal policy" );
            
            // Let BETC unbind first, to prevent a communication's error
            // from appearing.
            unbindOurselves( Inf, shout );
            Inf = null;
            makeServerUnbind( LRID, ObjectHandle, GOSCLI, GLSIDManager, shout, sherr );
            return true;
        }
        else
            shout.println( "BETC: *NOT* unbinding from object after error determining traceability, as per the content-removal policy" );
        
        return false;
    }
    
    
    private static boolean transientErrorHaveServerUnbindWhenRequiredByPolicy( GInterface Inf,
    long LRID,
    g.opaque ObjectHandle,
    GdnBETCConfig Config,
    objectServerOps GOSCLI,
    idManager GLSIDManager, // for print OHs
    PrintWriter shout,
    PrintWriter sherr )
    {
        // POLICY DECISION:
        // If the refuseContentWhenACSDown part of the content-removal policy
        // is false, BETC will not makeServer an unbind if the ACS happened to be down.
        
        GdnContentRemovalPolicy pol = Config.getContentRemovalPolicy();
        if (pol.refuseContentWhenACSDown)
        {
            shout.println( "BETC: Unbinding from object after GDN ACS unavailability, as per the content-removal policy" );
            
            // Let BETC unbind first, to prevent a communication's error
            // from appearing.
            unbindOurselves( Inf, shout );
            Inf = null;
            makeServerUnbind( LRID, ObjectHandle, GOSCLI, GLSIDManager, shout, sherr );
            return true;
        }
        else
            shout.println( "BETC: *NOT* unbinding from object after GDN ACS unavailability, as per the content-removal policy" );
        
        return false;
    }
    
    
    //
    // LRID is used to unbind, unless ObjectHandle is non-null
    //
    private static void makeServerUnbind( long LRID,
    g.opaque ObjectHandle,
    objectServerOps GOSCLI,
    idManager GLSIDManager,
    PrintWriter shout,
    PrintWriter sherr )
    
    {
        // unbindDSO not yet implemented (2001-09-07)
        //if (ObjectHandle != null)
        //{
        //	try
        //	{
        //		GOSCLI.unbindDSO( ObjectHandle );
        //	}
        //	catch( Exception e )
        //	{
        //		sherr.println( "Error: " + e.toString() );
        //	}
        //}
        //else
        //{
        // I need this in case the GOSCLI.statLR() failed and
        // we consequently do not know the object handle of
        // the local representative under investigation.
        
        shout.println( "BETC: Requesting object server to remove LR " + LRID + ", OH=" + ObjectHandleOps.objectHandleToString( ObjectHandle, GLSIDManager ) );
        try
        {
            GOSCLI.removeLR( LRID );
        }
        catch( Exception e )
        {
            sherr.println( "Error: " + e.toString() );
        }
        //}
    }
    
    
    private static void unbindOurselves( GInterface Inf, PrintWriter shout )
    {
        if (Inf != null)
        {
            shout.println( "BETC: Unbinding itself." );
            Inf.relInf();
            Inf = null;
            
            BETCBindCount--;
            //shout.println( "BETC: bound to " + BETCBindCount + "-- objects" );
        }
    }
    
    
    
    private static int testFileTraceab( String Filename,
    Package pkgInf,
    com.sun.net.ssl.TrustManager[] TMList,
    GdnACSClientSet ACSClientSet,
    Hashtable ACSLookupCache,
    String DownloadedFile,
    String ObjectHandleStr,
    PrintWriter shout,
    PrintWriter sherr )
    throws
    GdnCannotDetermineTraceabilityException,
    GdnACSClientException,
    PackageException // = gdnPackage.GDNErrors_inUse
    {
        shout.print( "BETC: Downloading file... " );
        shout.flush();
        
        FileOutputStream fos=null;
        long incarnationID = 0L;
        try
        {
            fos = new FileOutputStream( DownloadedFile );
            
            byte[] data=null;
            
            // TRACEAB:
            // *********TODO: CHECK THAT HACKER CANNOT CREATE MALICIOUS STATE
            // THAT LETS getIncarnationID RETURN AN INCARNATION ID
            // THAT getFileContent PUKES ON!!!!!
            //
            incarnationID = pkgInf.getIncarnationID( Filename );
            long offset = 0;
            long blockSize = DEFAULT_BLOCKSIZE_READ;
            long bytesRead = 0;
            do
            {
                data = pkgInf.getFileContent( Filename, incarnationID, offset, blockSize );
                bytesRead = data.length;
                offset += bytesRead;
                
                fos.write( data );
                
            } while( bytesRead != 0);
            
            fos.close();
            shout.println( "[ok]" );
        }
        catch( PackageException iue )
        {
            if (iue.toString().equals( PackageException.E_INUSE ))
            {
                sherr.println( "[POSTPONED]");
                sherr.println( "Note: check postponed on " + Filename + " because it is being uploaded at the moment. " );
                throw iue;
            }
            else
            {
                sherr.println( "[INTERNAL ERROR]");
                sherr.println( "Note: check postponed on " + Filename + " because we got an unknown (non inuse) package exception. " + iue.toString() );
                throw iue;
            }
        }
        catch( Exception e )
        {
            sherr.println( "[FAILED]");
            sherr.println( "Error: could not download " + Filename + " from object: " + e.toString() );
            throw new GdnCannotDetermineTraceabilityException();
        }
        
        
        getFileTraceInfoResults tio=null;
        int traceresult=GdnCryptoUtil.CONTENT_UNTRACEABLE;
        try
        {
            tio = pkgInf.getFileTraceInfo( Filename, incarnationID );
            
            String msg = GdnCryptoUtil.createLogMessage( ObjectHandleStr, "name unknown", DownloadedFile, tio.traceCert, tio.traceSig );
            shout.println( msg );
            
            traceresult = GdnCryptoUtil.determineTraceability( tio.traceCert, tio.traceSig,
            TMList,
            true, // always check for producer's status in ACS
            ACSClientSet,
            ACSLookupCache,
            DownloadedFile,
            "BETC: ", shout, sherr );
            
        }
        catch( PackageException iue )
        {
            if (iue.toString().equals( PackageException.E_INUSE ))
            {
                sherr.println( "Error: getFileTraceInfo() throws inUse exception, which should alreay have been reported by getFileContent()" );
            }
            else
                sherr.println( "Error: getFileTraceInfo() throws " + iue.toString() + " exception, which is not expected here!" );
            throw new GdnCannotDetermineTraceabilityException();
        }
        catch( GdnCannotDetermineTraceabilityException exc )
        {
            // message already written by GdnCryptoUtil.determineTraceability()
            throw new GdnCannotDetermineTraceabilityException();
        }
        catch( GdnACSClientException ae )
        {
            // message already written by GdnCryptoUtil.determineTraceability()
            throw new GdnACSClientException();
        }
        catch(Exception e)
        {
            sherr.println( "Error: " + e.toString() );
            throw new GdnCannotDetermineTraceabilityException();
        }
        
        return traceresult;
    }
    
    
    
    private static int testProducerStatus( String Filename,
    Package pkgInf,
    GdnACSClientSet ACSClientSet,
    Hashtable ACSLookupCache,
    String DownloadedFile,
    String ObjectHandleStr,
    PrintWriter shout,
    PrintWriter sherr )
    throws
    GdnCannotDetermineTraceabilityException,
    GdnACSClientException,
    PackageException // = gdnPackage.GDNErrors_inUse
    {
        shout.println( "BETC: Checking producer status...." );
        shout.flush();
        
        long incarnationID = 0L;
        getFileTraceInfoResults tio=null;
        int traceresult=GdnCryptoUtil.CONTENT_UNTRACEABLE;
        try
        {
            // TRACEAB:
            // *********TODO: CHECK THAT HACKER CANNOT CREATE MALICIOUS STATE
            // THAT LETS getIncarnationID RETURN AN INCARNATION ID
            // THAT getFileContent PUKES ON!!!!!
            //
            incarnationID = pkgInf.getIncarnationID( Filename );
            
            tio = pkgInf.getFileTraceInfo( Filename, incarnationID );
            
            //String msg = GdnCryptoUtil.createLogMessage( ObjectHandleStr, "name unknown", DownloadedFile, tio.traceCert.v, tio.traceSig.v );
            //shout.println( msg );
            
            traceresult = GdnCryptoUtil.getProducerStatus( tio.traceCert, tio.traceSig,
            ACSClientSet, ACSLookupCache,
            "BETC: ", shout, sherr );
            
        }
        catch( PackageException iue )
        {
            if (iue.toString().equals( PackageException.E_INUSE ))
            {
                sherr.println( "[POSTPONED]");
                sherr.println( "Note: check postponed on " + Filename + " because it is being uploaded at the moment. " );
                throw iue;
            }
            else
            {
                sherr.println( "[INTERNAL ERROR]");
                sherr.println( "Note: check postponed on " + Filename + " because we got an unknown (non inuse) package exception. " + iue.toString() );
                throw iue;
            }
        }
        catch( GdnCannotDetermineTraceabilityException exc )
        {
            // message already written by GdnCryptoUtil.getProducerStatus()
            throw new GdnCannotDetermineTraceabilityException();
        }
        catch( GdnACSClientException ae )
        {
            // message already written by GdnCryptoUtil.getProducerStatus()
            throw new GdnACSClientException();
        }
        catch(Exception e)
        {
            sherr.println( "Error: " + e.toString() );
            throw new GdnCannotDetermineTraceabilityException();
        }
        
        return traceresult;
    }
    
    
    
    
    // based on code from .../tika/sh/BaseHandler.java
    
    public static g.opaque selectClientContactAddress( g.opaque caddrs[], vu.globe.svcs.gls.idl.lsClient.idManager GLSIDManager, PrintWriter sherr )
    {
        g.opaque caddr = null;
        
        propertySelector idlPropSel = PropertySelectorOps.
                                        createGetBlueprintsPropertySelector();
        PropertySelector jPropSel = IdlTypesTools.convertPropertySelector( 
                                                                 idlPropSel );
        for (int i = 0; i < caddrs.length; i++)
        {
            caddr = caddrs[i];
            try
            {
                contactAddressInfo cinfo = GLSIDManager.getContactAddressInfo( caddr );
                PropertyMap pmap = IdlTypesTools.convertPropertyMap( cinfo.props );
                
                // BUG: won't work if we use all 3 contact points on a 
                // security subobject, i.e. we have a CP on not just INSECURE
                // and SECURE_RIGHTS
                //
                if (! jPropSel.test( pmap ))
                {
                    return caddr;
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                sherr.println("Error: cannot get contact address info");
                continue;
            }
        }
        return null;
    }
    
    // AUX METHODS copied from Tika.java, should move these to some shared dir
    
    /**
     * Set the security file setting. That is, read the named security file
     * and add the settings defined in it to the runtime. These settings are
     * needed by Globe runtime's communication objects. The setting names
     * are prefixed with the string "security.ssl".
     *
     * @param  ns     local name space to bind to the settings object
     * @param  fname  setting file
     */
    private static void setSecurityFileSetting(nameSpace ns, String fname)
    throws Exception
    {
        setRuntimeFileSetting(ns, fname, "security.ssl");
    }
    
    
    
    /**
     * Set a runtime file setting. The named setting file is parsed by
     * the settings object. All settings found in this file are prefixed
     * with the given prefix and added as a runtime setting.
     *
     * @param  ns      local name space to bind to the settings object
     * @param  fname   file name
     * @param  prefix  setting prefix
     */
    private static void setRuntimeFileSetting(nameSpace ns,
    String fname, String prefix)
    throws Exception
    {
        rtSettings settings = null;
        
        try
        {
            // bind to the settings object
            SOInf sett_soi = ns.bind(context.NsRootCtx, nsConst.SETTINGS_NAME);
            settings = (rtSettings) sett_soi.swapInf(rtSettings.infid);
        }
        catch(Exception e)
        {
            throw new RuntimeException("cannot bind to settings object");
        }
        
        try
        {
            settings.addSettingsFromFile(fname, prefix);
        }
        catch(configError_notFound e)
        {
            throw new RuntimeException("no such file: " + fname);
        }
        catch(configError_badFormat e)
        {
            throw new RuntimeException("corrupted setting file: " + fname);
        }
        catch(configError_io e)
        {
            throw new RuntimeException("cannot read setting file: I/O error");
        }
        finally
        {
            if (settings != null)
            {
                settings.relInf();
                settings = null;
            }
        }
    }
    
    
    /**
     * Check if a file is normal, if it exists.
     *
     * @param  fname   name of the file to be checked
     * @param  exists  if set, the file should exist
     *
     * @return <code>0</code>;
     *         <code>-1</code> if the file does not exist (and
     *                         <code>exists</code> is set;
     *         <code>-2</code> if the file is a directory;
     *         <code>-3</code> if it is not a regular file
     */
    private static int checkFile(String fname, boolean exists)
    {
        File f = new File(fname);
        
        if (f.exists() == false)
        {
            if (exists)
            {
                return -1;
            }
        }
        else if (f.isFile() == false)
        {
            if (f.isDirectory())
            {
                return -2;
            }
            else
            {
                return -3;
            }
        }
        return 0;
    }
    
    
    /**
     * Print an error message and exit.
     */
    public static void fatal( PrintWriter sherr, String s)
    {
        sherr.println(PROGNAME + ": " + s);
        System.exit(-1);
    }
    
    /**
     * Print an error message and exit.
     */
    private static void fatal(String s)
    {
        System.err.println(s);
        System.exit(-1);
    }

    private static void usage( PrintWriter SHErr )
    {
        SHErr.println("Usage:");
        SHErr.println("  " + PROGNAME + " [options] site-dir");
        SHErr.println();
        SHErr.println("Parameters:");
        SHErr.println("  site-dir    : directory of Globe site to use");
        SHErr.println();
        SHErr.println("Options:");
        SHErr.println("  -N          : disable security");
        System.exit( -1 );
    }
}


