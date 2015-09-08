/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// AuthDBImpl.java

package vu.globe.svcs.gls.node.contact.proto.auth;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.net.ProtocolException;

import vu.globe.svcs.gls.node.contact.*;
import vu.globe.svcs.gls.types.ObjHandleRep;
import vu.globe.svcs.gls.types.ObjectID;
import vu.globe.svcs.gls.config.NodeProperties;
import vu.globe.svcs.gls.config.ObjEnv;
import vu.globe.svcs.gls.stats.NodeStatistics;
import vu.globe.svcs.gls.debug.Debug;
import vu.globe.util.debug.DebugOutput;
import vu.globe.util.time.Stopwatch;
import vu.globe.util.exc.NotImplementedException;
import vu.globe.util.exc.AssertionFailedException;

import vu.globe.svcs.gls.node.contact.proto.base.DBBase;
import vu.globe.util.db.dbif.*;
import vu.globe.util.db.berkeley.BerkeleyDBFact;
import vu.globe.util.db.jgdbm.*;
import vu.globe.util.db.inmem.*;


import vu.globe.util.comm.idl.rawData.rawDef;
import vu.globe.util.comm.RawOps;



/**
   A contact record database with authoritative contact records. Persistency
   is implemented as follows. A contact record element (e.g., contact
   field, address set) notifies the contact record to which it belongs
   whenever the element has been updated. Next, the contact record requests
   the authoritative database to write its contents (i.e., the contact
   record data) to the legacy database. The legacy database (e.g. Berkeley DB)
   is responsible for writing the data to disk.
 
   @author Patrick Verkaik
   @author Egon Amade (added persistency)
*/


public class AuthDBImpl extends DBBase implements ContactsDB
{
   private static final boolean DEBUG = false;                   // debug flag
   private static final boolean STRICT_ASSERT = true;            // debug flag

   // If set, use synchronous-writes when writing to the legacy database.
   private boolean syncWrites;

   private DbIf database;                                      // the database
   private String dbType;                                     // database type
   private boolean disableWrites;   // debug: if true, do no write to database
   private boolean chkWrites;    // debug: if true, check each write operation
   private boolean diskWrites;  // if true, use disk for DB, else use inmemory

   // use of statistics
   private NodeStatistics stats;

   // use of Stopwatch to time disk i/o
   private Stopwatch stopwatch;

   /**
      Instance creation. Use the default database file.
    */
   public AuthDBImpl (ObjEnv env)
   {
      this (env, NodeProperties.authoritativeDBFilename ());
   }


   /**
      Instance creation.

      @param  dbname  the name of the database file
    */
   public AuthDBImpl (ObjEnv env, String dbname)
   {
      stats = (NodeStatistics) env.getStatistics();
      if(Debug.statsTime())
         stopwatch = new Stopwatch();

      try {

         // read the properties for the authoritative db.
         dbType = NodeProperties.authoritativeDatabaseType ();
         diskWrites = NodeProperties.authoritativeDiskWrites ();
         syncWrites = NodeProperties.authoritativeSyncWrites ();
         disableWrites = NodeProperties.authoritativeDisableWrites ();
         chkWrites = NodeProperties.authoritativeWritesCheck ();

         // Open the Berkeley or GDBM database.
         if(diskWrites) {
            if (dbType.equals("berkeleydb")) {
              database = BerkeleyDBFact.createBerkeleyDB (dbname,
	      	BerkeleyDBFact.DBTYPE_HASH);
            }
            else if (dbType.equals("gdbm")) {
              database = new JGDBM (dbname, JGDBM.JGDBM_CREATE);
            }
            else {
              fatal ("unknown database type");
            }
         }
         else
            database = new MemDB(dbname);

         if (DEBUG) {
            debugPrintLn ("created " + dbType + " database object, file "
                          + dbname);
         }

         if (diskWrites) {
           debugPrintLn ("db type = " + dbType);
         }
         debugPrintLn ("disk writes = " + diskWrites
            + ", sync writes = " + syncWrites
            + ", disable writes = " + disableWrites
            + ", check writes = " + chkWrites);
      }
      catch (Exception e) {
         fatal ("cannot initialize database " + dbname + ": " + e.toString ());
      }
   }


   // ContactsDB interface


   /**
      Closes the database.
    */
   public void close ()
   {
      if (database != null) {
         try {
            System.out.println ("closing database ...");
            database.close ();
         }
         catch (DBException e) {
            System.err.println ("cannot close database: " + e.toString ());
         }
      }
   }


   /**
      Looks up an object's contact record. A new contact record is
      created if necessary.
    
      @param  ohandle  the object's handle
      @return the object's contact record
   */
   public ContactRecord mapContactRecord (ObjHandleRep ohandle)
   {
      ContactRecord cr = getContactRecord (ohandle);

      if (cr != null) {
         throw new AssertionFailedException ("contact record is already mapped");
      }

      /*
         Desired contact record object is not present. Read the contact
         record from the database.
      */
      if ( (cr = readContactRecord (ohandle)) != null) {
         putContactRecord (ohandle, cr);
         return cr;
      }

      /*
         The contact record is not in the database, a new one is created.
       */ 
      cr = new AuthCRImpl (this, ohandle);

      putContactRecord (ohandle, cr);

      /*
         No need to call writeContactRecord() because empty contact
         records are not saved on disk.
       */

      return cr;
   }


   /**
      Releases a contact record.

      @param  ohandle  the object's handle
   */
   public void releaseContactRecord (ObjHandleRep ohandle)
   {
      // Remove contact record from contact record pool.
      AuthCRImpl cr = (AuthCRImpl) removeContactRecord (ohandle);

      if (cr == null) {
         throw new AssertionFailedException ("no such contact record");
      }

      if (STRICT_ASSERT) {
         /*
          * If the contact record is empty, it should not be present
          * in the legacy database.
          */
 
         if (cr.isEmpty ()) {
            ObjectID oid = ohandle.getOid ();
            byte key[] = oid.getBytes ();
 
            try {
               if (database.getData (key) == null) {     // not found
                  // OK
               }
               else {
                 throw new AssertionFailedException ("empty contact "
                                                     + "record found");
               }
            }
            catch(DBException e) {
               fatal ("readContactRecord", "cannot get contact record: "
                      + e.toString ());
            }
      	}
		}
   }


   public Enumeration getObjecthandles ()
   {
      return database.keys ();
   }


   /**
      NOT IMPLEMENTED
    */
   public Object clone()
   {
      throw new NotImplementedException ();
   }


   /**
      Writes a contact record to the legacy database. If, however, the
		contact record is empty, it is deleted from the legacy database
		instead.
    
      @param  ohandle  the object's handle
      @param  cr       the object's contact record
   */
   void syncContactRecord (ObjHandleRep ohandle, ContactRecord cr)
   {
      if (cr.isEmpty ()) {
         deleteContactRecord (ohandle);
      }
      else {
      	writeContactRecord (ohandle, cr);
      }
   }


   /**
      Deletes a contact record from the legacy database.

      @param  ohandle  the object's handle
    */
   private void deleteContactRecord (ObjHandleRep ohandle)
   {
      byte key[];
      ObjectID oid;
      ContactRecord cr;

      if (DEBUG) {
         debugPrintLn ("deleteContactRecord", "removing cr from database ...");
      }

      oid = ohandle.getOid ();
      key = oid.getBytes ();

      if(Debug.statsTime())
         stopwatch.start();

      try {
         database.delete (key);

         if (syncWrites) {
            database.sync ();
         }
      }
      catch(DBException e) {
         fatal ("deleteContactRecord", "cannot remove contact record: "
                + e.toString ());
      }

      if(Debug.statsTime())
      {
         stopwatch.stop();
         stats.addCRDBRemoveTime(stopwatch);
      }
      stats.addCRDBRemove(1);

   }


   /**
      Fetches a contact record from the legacy database.

      @param  ohandle  the object's handle
      @return the object's contact record;
              <code>null</code> if the contact record is not found
   */
   private ContactRecord readContactRecord (ObjHandleRep ohandle)
   {
      byte key[], data[];
      rawDef rawData;
      ObjectID oid;
      ContactRecord cr;

      if (DEBUG) {
         debugPrintLn ("readContactRecord", "looking for cr in database ...");
      }

      oid = ohandle.getOid ();
      key = oid.getBytes ();
      data = null;

      if(Debug.statsTime())
         stopwatch.start();

      try {
         if ( (data = database.getData (key)) == null) {     // not found
            return null;
         }
      }
      catch(DBException e) {
         fatal ("readContactRecord", "cannot get contact record: "
                + e.toString ());
      }

      if(Debug.statsTime())
      {
         stopwatch.stop(); 
         stats.addCRDBReadTime(stopwatch);
      }

      if (data.length == 0) {                         // should not occur
         return null;
      }

      if (DEBUG) {
         debugPrintLn ("readContactRecord", "found cr: " + data.length
                       + " bytes");
      }

      stats.addCRDBReadSize(data.length);

      rawData = RawOps.createRaw ();
      RawOps.setRaw (rawData, data, 0, data.length);

      try {
         
         if(Debug.statsTime())
            stopwatch.start();

         cr = new AuthCRImpl (this, ohandle, rawData);
         
         if(Debug.statsTime())
         {
            stopwatch.stop();
            stats.addCRDBMarshallTime(stopwatch);
         }
         
         return cr;
      }
      catch (ProtocolException e) {
         fatal ("readContactRecord", "cannot unmarshall contact record: "
                + e.toString ());

         // NOTREACHED
         return null;
      }
   }


   /**
      Inserts a new contact record in the legacy database or update an
      existing one.

      @param  ohandle  the object's handle
      @param  cr       the object's contact record
    */
   private void writeContactRecord (ObjHandleRep ohandle, ContactRecord cr)
   {
      byte key[], data[];
      rawDef rawData;
      ObjectID oid;

      if (disableWrites) {                // do not write to database
         return;
      }

      try {
         oid = ohandle.getOid ();
         key = oid.getBytes ();

         if(Debug.statsTime())
            stopwatch.start();

         // Marshall the contact record.
         rawData = ( (AuthCRImpl) cr).marshall ().getPacket ();
         
         if(Debug.statsTime())
         {
            stopwatch.stop();
            stats.addCRDBMarshallTime(stopwatch);
         }

         data = RawOps.getRaw (rawData);

         if(Debug.statsTime())
            stopwatch.start();

         try {
            if (DEBUG) {
               debugPrintLn ("writeContactRecord", "saving cr: "
                             + data.length + " bytes");
            }

            database.replace (key, data);

            //System.out.println("writing CR for " + ohandle + " " + cr);
            stats.addCRDBWriteSize(data.length);

            if (syncWrites) {
               database.sync ();
            }
         }
         catch (DBException e) {
            fatal ("writeContactRecord", "cannot store contact record: "
                   + e.toString ());
         }

         if(Debug.statsTime())
         {
            stopwatch.stop();   
            stats.addCRDBWriteTime(stopwatch);
         }

         /*
            If the 'check writes' option is enabled, read the contact
            record that has just been saved, and verify its contents.
         */
         if (chkWrites) {
            if (DEBUG) {
               debugPrintLn ("writeContactRecord", "comparing cr ...");
            }

            AuthCRImpl src = (AuthCRImpl) cr;
            AuthCRImpl tgt = (AuthCRImpl) readContactRecord (ohandle);

            if (src.equals (tgt) == false) {
               fatal ("writeContactRecord",
                      "saved cr differs from original cr");
            }
         }
      }
      catch (ProtocolException e) {
         fatal ("writeContactRecord", "cannot marshall contact record: "
                + e.toString ());
      }
   }


   /**
      Prints an error message, then exit.
    */
   private void fatal (String msg)
   {
      fatal (null, msg);
   }


   /**
      Prints an error message, then exit.
    */
   private void fatal (String method, String msg)
   {
      if (method == null) {
         System.err.println ("Fatal error: AuthDBImpl: " + msg);
      }
      else {
         System.err.println ("Fatal error: AuthDBImpl." + method + ": " + msg);
      }

      close ();

      System.exit (-1);
   }


   /**
      Prints a debug message.
    */
   private void debugPrintLn (String msg)
   {
      debugPrintLn (null, msg);
   }


   /**
      Prints a debug message.
    */
   private void debugPrintLn (String method, String msg)
   {
      if (method == null) {
         DebugOutput.println ("AuthDBImpl: " + msg);
      }
      else {
         DebugOutput.println ("AuthDBImpl." + method + ": " + msg);
      }
   }
}
