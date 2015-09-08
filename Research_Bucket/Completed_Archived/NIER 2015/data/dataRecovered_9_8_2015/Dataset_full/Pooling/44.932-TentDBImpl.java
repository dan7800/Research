/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.node.contact.proto.tent;

import vu.globe.util.exc.NotImplementedException;
import vu.globe.util.exc.AssertionFailedException;
import vu.globe.svcs.gls.types.ObjHandleRep;
import vu.globe.svcs.gls.node.contact.*;
import vu.globe.svcs.gls.config.NodeProperties;
import vu.globe.svcs.gls.node.contact.proto.base.DBBase;
import vu.globe.svcs.gls.config.ObjEnv;
import vu.globe.svcs.gls.stats.NodeStatistics;
import java.util.Enumeration;

/**
   A contact record database with tentative contact records. The database
   maintains two important datastructures: a hashtable and a recycle-list.
   The hashtable contains contact records that should not be thrown away.
   This set of contact records is also called the contact record set. The
   recycle-list contains records that can be thrown away or can be
   re-used when necessary.

   A contact record can be thrown away if it has been released AND if
   its view series are empty. Conversely, a contact record cannot be thrown
   away if it is not released (i.e., if it is mapped), or if it has been
   released but its view series aren't empty.

   Instead of throwing contact records away, they are put on a recycle-list.
   If a contact record is needed again, a check is made to see if it is in
   the recycle-list. If it is in the recycle-list, it will be moved to the
   hashtable. Otherwise, a new contact record object is created (which
   is less efficient). 
 
   @author Patrick Verkaik
   @author Egon Amade (recycle-list, releasing contact records)
*/

public class TentDBImpl extends DBBase implements TentDB
{
   private static final boolean DEBUG = false;          // debug flag

   // the corresponding authoritative database
   private ContactsDB auth_db;

   /*
      The recycle-list holds the contact records that are allowed to be
      thrown away.
    */
   private LruHashtable recycleList;

   /*
      Maximum number of contact records that may be kept in memory (i.e.,
      hashtable + recycle-list). Note that this threshold can be exceeded
      if the hashtable is large enough.
    */
   private int crPoolSize;

   /*
      Number of contact records to remove from the recycle-list whenever
      the total number of contact records exceeds <code>crPoolSize</code>.
   */
   private static final int R_NUM_DRAIN = 100;

   private ObjEnv env;

   /**
      Constructor. The authoritative database should be passed.
   */
   public TentDBImpl (ObjEnv env, ContactsDB auth_db)
   {
      this.env = env;
      this.auth_db = auth_db;
      recycleList = new LruHashtable ();
      crPoolSize = NodeProperties.tentativeContactRecordPoolSize ();

      if(NodeProperties.tentativeCaching())
         System.out.println ("tent cr pool size: " + crPoolSize);
      else
         System.out.println ("tent cr disabled");
   }

   // ContactsDB methods

   public void close ()
   {
      Enumeration ohandles = getObjecthandles ();
      int n = 0;

      /*
         Display an error message if there are any contact records that
         should not be thrown away at the moment.
       */

      for ( ; ohandles.hasMoreElements (); ) {
         ObjHandleRep ohandle = (ObjHandleRep) ohandles.nextElement ();
         TentCRImpl cr = (TentCRImpl) getContactRecord (ohandle);

         if (cr.hasViewSeries ()) {
            n++;
         }
      }

      if (n > 0) {
         System.err.println ("Warning: losing tentative state of " + n
                             + " contact records");
      }
   }
 
   public ContactRecord mapContactRecord (ObjHandleRep ohandle)
   {
      ContactRecord cr = getContactRecord (ohandle);

      /*
         Found the CR in the contact record set. Return the CR if it was
         virtually mapped, or throw an exception if it was mapped.
       */
      if (cr != null) {
         if (( (TentCRImpl) cr).getReleaseFlag ()) {
            ( (TentCRImpl) cr).setReleaseFlag (false);
            return cr;
         }
         throw new AssertionFailedException ("CR already mapped");
      }

      /*
         The CR is not in the contact record set. If it is in the
         recycle-list, it can be used again.
      */
      if ( (cr = (ContactRecord) recycleList.remove (ohandle)) != null) {

         if (DEBUG) {
            System.out.println("[TentDBImpl:mapCR] found entry in "
                               + "recycle-list");
         }

         ( (TentCRImpl) cr).setReleaseFlag (false);

         /*
            Add CR (already removed from recycle-list) to contact record set.
            The authoritative contact record is already mapped.
          */
         putContactRecord (ohandle, cr);

         return cr;
      }

      // Create a new contact record.

      /*
         If the maximum number of contact records that may be kept in
         memory is reached, try to remove some contact records that are
         on the recycle-list.
      */
      int n = recycleList.size ();
      if (size () + n >= crPoolSize) {

         int m = (n < R_NUM_DRAIN) ? n : R_NUM_DRAIN;
         TentCRImpl tcr;

           if (DEBUG) {
            System.out.println("[TentDBImpl:mapCR] draining "
                               + m + " entries, recycle-list size " + n
                               + ", cr set size " + size ());
           }

         for ( ; m > 0; m--) {
            if ( (tcr = (TentCRImpl) recycleList.removeLruEntry()) == null) {
               System.err.println("cannot remove LRU entry");
               break;
            }

            // Release the corresponding authoritative contact record.
            auth_db.releaseContactRecord (tcr.getObjHandle ());
         }
      }

      ContactRecord auth_cr = auth_db.mapContactRecord (ohandle);
      cr = new TentCRImpl (env, auth_cr, ohandle);

      // Add CR to contact record set.
      putContactRecord (ohandle, cr);
      return cr;
   }

   public void releaseContactRecord (ObjHandleRep ohandle)
   {
      ContactRecord cr = getContactRecord (ohandle);

      if (cr == null) { 
         if (recycleList.get (ohandle) != null) {
            throw new AssertionFailedException ("CR already released");
         }
         throw new AssertionFailedException ("no such CR");
      }

      // Set the release-flag of the CR.
      ( (TentCRImpl) cr).setReleaseFlag (true);

      // Return if the CR cannot be destroyed.
      if (( (TentCRImpl) cr).hasViewSeries ()) {
         if (DEBUG) {
            System.out.println ("TentDBImpl::releaseCR: keeping CR");
         }
         return;
      }

      /*
         Remove CR from contact record set and put it in the recycle-list.
         The corresponding authoritative contact record is released when
         the CR is removed from this list for 'destruction'.
      */
      removeContactRecord (ohandle);
      recycleList.put (ohandle, cr);
   }

   public Object clone ()
   {
      throw new NotImplementedException (); // XXX
   }
}
