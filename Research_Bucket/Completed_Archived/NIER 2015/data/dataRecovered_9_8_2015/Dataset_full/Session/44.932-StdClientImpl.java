/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.resolver.stdclient.proto;

import vu.globe.util.exc.*;
import vu.globe.svcs.gls.config.*;
import vu.globe.svcs.gls.types.*;
import vu.globe.svcs.gls.typedaddress.*;
import vu.globe.svcs.gls.types.*;
import vu.globe.svcs.gls.resolver.ActiveResolverOps;
import vu.globe.svcs.gls.resolver.stdclient.*;
import vu.globe.svcs.gls.resolver.server.ResolverServer;
import vu.globe.svcs.gls.protocol.ProtOperation;
import vu.globe.svcs.gls.alert.*;
import vu.globe.svcs.gls.active.*;
import vu.globe.util.comm.idl.rawData.rawDef;
import vu.globe.svcs.gls.protocol.ProtLocator;
import vu.globe.svcs.gls.protocol.ProtLocatorImpl;

import java.io.IOException;
import java.net.ProtocolException;
import java.util.Enumeration;

/**
   A <code>StdClient</code> implementation.
 
   @author Patrick Verkaik
*/

/*
   Continuations
   -------------

   The resolver server is based on calls with callbacks. A
   client operation is therefore split between the initial call and the
   continuation in the callback. Some state must be passed from the initial call
   to the continuation. (This is done in the form of a user cookie via the
   resolver server.)

   To the user, each resolver client call is a single blocking call. The client
   must therefore hide its continuations from the user. It does so by blocking
   the user thread which makes the initial call, and waking it up in the
   continuation. The blocked thread is made an implicit part of the state:
   it is the state object that blocks the thread and wakes it up when asked
   to.

   Consistency
   -----------

   A standard resolver client must provide consistency of look-ups wrt
   insertions in the local domain. The easiest way to do this is to block a
   local insert operation until the domain has been updated. Any look-up can
   then proceed immediately. This is the approach implemented here. In fact
   *all* update operations block.

   [
   A more elaborate, but perhaps more efficient alternative
   is not to block insert operations, but to block the look-up operation
   instead. A look-up would wait until preceding local insertions had updated
   the domain, before sending its request.
   ]

   Updates
   -------

   These block until a leaf node has been updated. Blocking is really only
   necessary for insertions sent to the local leaf node, since the aim is for
   insertions to be consistent with local look-up operations. However, in this
   implementation we do this for all updates.

   If blockUpdatesUntilCompletion() is invoked by the user, updates block until
   they complete have been process throughout the location service (rather than
   just the local domain).

   As with other operations, I/O exceptions are passed to the user through the
   insert()/delete()/... call. There is no way to notify the user of an
   exception after the call has completed but before the optional notification
   takes place. Such exceptions are printed but otherwise ignored.

   Look-Ups
   --------
   
   This implementation supports queries of at most one object handle. (A
   restriction imposed by the resolver server.)

   Mutual Exclusion
   ----------------

   Operations and their callbacks synchronize on their OperationState for the
   following reasons:
   
   1. A registration id must be passed to the callback handler through the
   state. The registration id is returned by the server operation. The callback
   also results from invoking the operation in the server. Therefore without
   synchronization there is a (small) risk of the callback coming in before the
   registration id has been saved, and an attempt to access it.

   2. The call to the state's awaitCompletion() method and the server
   invocation must be atomic with respect to the callback handler.

   Care must be taken not to notify the user while holding the object lock.
   As explained in the LS documentation on thread usage, this avoids a deadlock.

   Data Management
   ---------------

   The StdClient interface extends the ID manager interface, and a StdClientImpl
   must therefore create object handles and contact addresses for the user.

   StdClientImpl protects the resolver against careless users by providing
   the user with separate, restricted classes for object handles and contact
   addresses. The user cannot convert these classes to the unrestricted classes
   used internally by the resolver. This implies some conversion overhead in
   the StdResolverOps operations. Also, the remaining data types in the look-up
   operation are protected by data copying.
*/

public class StdClientImpl implements StdClient
{
   // Environment
   protected ResolverServer resolver_server;

   // The typed address factory. Always set.
   protected TypedAddressFact ta_fact;
   
   // Various notifiables to use
   protected Notifiable update_notifiable;
   protected Notifiable lookup_notifiable;

   protected Notifiable user_notifiable;  // set after being activated
   protected LocationID location_id;      // always set
   protected DomainID domain_id;          // always set

   // Whether to block an update invocation until the update has been processed
   // throughout the location service.
   protected boolean block_update_completion = false;

   public StdClientImpl ()
   {
      // create the typed address factory
      // assume the default until overridden by the user
      ta_fact = new StdTypedAddressFactImpl ();

      // create notifiables

      update_notifiable = new Notifiable () {
         public void notifyEvent (ActiveUID uid) {
            notifyUpdateEvents (uid);
         }
      };

      lookup_notifiable = new Notifiable () {
         public void notifyEvent (ActiveUID uid) {
            notifyLookupEvents (uid);
         }
      };
   }

   public void setServer (ResolverServer server)
   {
      resolver_server = server;
      domain_id = server.getDomain ();
      location_id = server.getLocation ();
   }

   public void blockUpdatesUntilCompletion()
   {
      block_update_completion = true;
      System.out.println ("StdClientImpl: updates to location service block " +
        "until LS is fully updated");
   }

   // Active interface
   public ActiveRID activate (Notifiable note, ActiveUID user_info)
   {
      checkArg (note);
      user_notifiable = note;
      return null;
   }
 
   public ActiveRID activate (int service, Notifiable note, ActiveUID userInfo)
   {
      throw new NotSupportedException ("activate with service id");
   }

   public void deactivate (ActiveRID regID)
   {
      // the most efficient deactivate method so far!
   }


   // StdResolverOps interface

   public void insert (ObjectHandle ohandle, ContactAddress caddr,
                     ActiveUID user_info, AlertID id) throws IOException
   {
      checkArg (ohandle);
      checkArg (caddr, false);

      update (ProtOperation.INSERT, (CltObjHandleImpl) ohandle,
                                 (CltCAddrImpl) caddr, user_info, id);
   }

   public void delete (ObjectHandle ohandle, ContactAddress caddr,
                      ActiveUID user_info, AlertID id) throws IOException
   {
      checkArg (ohandle);
      checkArg (caddr, true);

      update (ProtOperation.DELETE, (CltObjHandleImpl) ohandle,
                                 (CltCAddrImpl) caddr, user_info, id);
   }

   public void testDelete (ObjectHandle ohandle, ContactAddress caddr,
                      ActiveUID user_info, AlertID id) throws IOException
   {
      checkArg (ohandle);
      checkArg (caddr, true);

      update (ProtOperation.TEST_DELETE, (CltObjHandleImpl) ohandle,
                                 (CltCAddrImpl) caddr, user_info, id);
   }

   public void testInsert (ObjectHandle ohandle, ContactAddress caddr,
                      ActiveUID user_info, AlertID id) throws IOException
   {
      checkArg (ohandle);
      checkArg (caddr, true);

      update (ProtOperation.TEST_INSERT, (CltObjHandleImpl) ohandle,
                                 (CltCAddrImpl) caddr, user_info, id);
   }

   /**
      Performs an update such as <code>insert</code> or <code>delete</code>.
      The update to perform is specified by the <code>opcode</code> parameter.
 
      @param opcode  one of <code>ProtOperation</code>'s resolver update opcodes
   */
   protected void update (int opcode, CltObjHandleImpl ohandle,
      CltCAddrImpl caddr, ActiveUID user_info, AlertID id) throws IOException
   {
      /*
	 In the default StdClient behaviour (i.e. blockUpdatesUntilCompletion()
	 has not been invoked), the server is invoked with 'callback
	 notification for local updates' enabled. This is the callback which
	 will wake up the calling thread. Callback notification for _global_
         updates is optional, and is enabled only if the user desires such
         notification (i.e. a user_info is passed).

	 If the default StdClient behaviour has been overridden (by
	 blockUpdatesUntilCompletion()), the server is invoked with 'callback
	 notification for local updates' disabled, and callback notification
	 for _global_ updates enabled.
      */

      if (user_info != null) {
         if (user_notifiable == null) {
            throw new IllegalStateException ("not activated");
         }
         if (block_update_completion) {
            throw new IllegalStateException ("updates block until completion");
         }
      }
      
      // Java: initially false
      boolean [] services = new boolean [ActiveResolverOps.NSERVICES];

      services [ActiveResolverOps.LOCALLY_UPDATED] = ! block_update_completion;
      services [ActiveResolverOps.FULLY_UPDATED] = block_update_completion ||
                                                   (user_info != null);

      UpdateState state = new UpdateState (opcode);
      state.setUID (user_info);     // even if it is null

      // See doc above
      synchronized (state) {

         ActiveRID rid = null;
         switch (opcode) {
         case ProtOperation.INSERT:
            rid = resolver_server.insert (ohandle.getFullHandle(),
                     caddr.toCAddrRep (), update_notifiable, state,
                     services);
            break;
         case ProtOperation.DELETE:
            rid = resolver_server.delete (ohandle.getFullHandle(),
                     caddr.toCAddrRep (), update_notifiable, state,
                     services);
            break;
         case ProtOperation.TEST_DELETE:
            rid = resolver_server.testDelete (ohandle.getFullHandle(),
                     caddr.toCAddrRep (), update_notifiable, state,
                     services);
            break;
         case ProtOperation.TEST_INSERT:
            rid = resolver_server.testInsert (ohandle.getFullHandle(),
                     caddr.toCAddrRep (), update_notifiable, state,
                     services);
            break;
         default:
            throw new AssertionFailedException ();
         }

         state.setRegID (rid);
         state.awaitCompletion ();
      }
      state.testException ();
   }

   public ObjectCAddr [] lookup (Query query, int min, int max, AlertID id)
                           throws IOException
   {
      checkArg (query);

      // empty queries are not allowed by the server
      Enumeration query_enum = query.getObjAddrSelectors ();
      if (! query_enum.hasMoreElements ())
         return new ObjectCAddr [0];

      // this call leaves 'query' intact, as expected by the user
      Query svr_query = client2server (query);

      LookupState state = new LookupState ();

      // See doc above
      synchronized (state) {
         ActiveRID rid = resolver_server.lookup (svr_query, min, max,
                                 lookup_notifiable, state);
         state.setRegID (rid);
         state.awaitCompletion ();
      }
      state.testException ();
      ObjectCAddr [] results = state.getResults ();

      // nobody is using 'results', so allow server2client() to modify them
      server2client (results, ta_fact);

      return results;
   }

   // IDManager interface

   public CltObjHandle createObjectHandle (ObjectID oid, LocationID location_id)
   {
      checkArg (oid);

      return new CltObjHandleImpl (oid, location_id);
   }

   public CltObjHandle createObjectHandle (ObjectID oid)
   {
      checkArg (oid);

      return new CltObjHandleImpl (oid, location_id);
   }

   public CltObjHandle unmarshallObjectHandle (rawDef pkt)
                                    throws ProtocolException
   {
      CltObjHandleImpl ohandle = new CltObjHandleImpl ();
      ohandle.fromPacket (pkt);
      return ohandle;
   }

   public CltCAddr createContactAddress (AddressID aid)
   {
      checkArg (aid);
      return new CltCAddrImpl (domain_id, aid);
   }

  // Added -- Aline
   public CltCAddr createContactAddress (DomainID domain_id, AddressID aid)
   {
      checkArg (aid);
      //checkArg (domain_id); // todo
      return new CltCAddrImpl (domain_id, aid);
   }

   public CltCAddr unmarshallContactAddress (rawDef pkt)
                                    throws ProtocolException
   {
      CltCAddrImpl caddr = new CltCAddrImpl ();
      caddr.fromPacket (pkt, ta_fact);
      return caddr;
   }

   public void setTypedAddressFact (TypedAddressFact user_factory)
   {
      checkArg (user_factory);
      ta_fact = user_factory;
   }

   /**
      A Notifiable for update operations. It runs synchronized on the operation
      state.
   */
   protected void notifyUpdateEvents (ActiveUID uid)
   {
      UpdateState state = (UpdateState) uid;

      ActiveUID user_cookie;
      synchronized (state) {
         user_cookie = processUpdateEvent (state);
      }

      // notify the user outside the monitor
      if (user_cookie != null)
         user_notifiable.notifyEvent (user_cookie);
   }

   /**
      Processes a completed server update operation, awakening the user
      thread if necessary. Also determines whether the user should be notified.
      If so, returns a user cookie.

      @return
         a user cookie if the user should be notified, otherwise null
   */
   protected ActiveUID processUpdateEvent (UpdateState state)
   {
      ActiveUID user_cookie = null;    // return value

      try {
         // apart from assess...(), updates are treated the same

         // first, determine the progress

         boolean [] progress;
         switch (state.getOpCode ()) {
         case ProtOperation.INSERT :
            progress = resolver_server.assessInsert (state.getRegID ());
            break;
         case ProtOperation.DELETE :
            progress = resolver_server.assessDelete (state.getRegID ());
            break;
         case ProtOperation.TEST_DELETE :
            progress = resolver_server.assessTestDelete (state.getRegID ());
            break;
         case ProtOperation.TEST_INSERT :
            progress = resolver_server.assessTestInsert (state.getRegID ());
            break;
         default:
            throw new AssertionFailedException ();
         }

         // now, analyse the progress
   
         if (progress [ActiveResolverOps.LOCALLY_UPDATED]) {
            // ignore whether the thread has already been awoken before
	    if (! block_update_completion) {
               state.signalCompletion ();
	    }
	 }
   
         if (progress [ActiveResolverOps.FULLY_UPDATED]) {

	    if (block_update_completion) {
               state.signalCompletion ();
	    }
	    else if (! block_update_completion) {

               // only notify once: the user cookie is cleared
               user_cookie = state.getUid ();
               if (user_cookie != null)
                  state.setUID (null);
            }
         }
      }
      catch (IOException exc) {
         state.setException (exc);

         if (state.completed ())
            // too late
            System.out.println (exc);
         else
            // ignore whether the thread has been awoken before
            state.signalCompletion ();
      }
      return user_cookie;
   }

   /**
      A Notifiable for lookup operations. It runs synchronized on the operation
      state.
   */
   protected void notifyLookupEvents (ActiveUID uid)
   {
      LookupState state = (LookupState) uid;

      synchronized (state) {
         processLookupEvent (state);
      }
   }

   /**
      Processes a completed server look-up operation and awakens the user
      thread.
   */
   protected void processLookupEvent (LookupState state)
   {
      try {
         ObjectCAddr [] results =
                           resolver_server.lookupResults (state.getRegID ());
         if (results == null)
            throw new AssertionFailedException ();

         state.setResults (results);
         state.signalCompletion ();
      }
      catch (IOException exc) {
         state.setException (exc);
         state.signalCompletion ();
      }
   }

   /*
      Argument checking
      
      These methods perform a number of checks on arguments supplied to
      public methods.
   */

   /**
      Checks a <code>NodeID</code> argument.
      
      @exception IllegalArgumentException
         if the argument check failed
   */
   protected void checkArg (NodeID node)
   {
      if (node == null)
         throw new IllegalArgumentException ("null node id");
   }

   /**
      Checks an <code>ObjectHandle</code> argument.
      
      @exception IllegalArgumentException
         if the argument check failed
   */
   protected void checkArg (ObjectHandle ohandle)
   {
      if (! (ohandle instanceof CltObjHandleImpl))
         throw new IllegalArgumentException
                                       ("null or unrecognised object handle");
      if (! ohandle.complete ())
         throw new IllegalArgumentException
                                 ("incomplete object handle: " + ohandle);
   }

   /**
      Checks a <code>ContactAddress</code> argument.
      
      @param caddr   the contact address to check
      @param as_id   true if only the identifying parts of the contact address
                     need to be present
      @exception IllegalArgumentException
         if the argument check failed
   */
   protected void checkArg (ContactAddress caddr, boolean as_id)
   {
      if (! (caddr instanceof CltCAddrImpl))
         throw new IllegalArgumentException
                              ("null or unrecognised contact address");
      if (! caddr.complete (as_id))
         throw new IllegalArgumentException
                        ("incomplete contact address: " + caddr);
   }

   /**
      Checks a <code>Query</code> argument.
      
      @exception IllegalArgumentException
         if the argument check failed
   */
   protected void checkArg (Query query)
   {
      if (query == null)
         throw new IllegalArgumentException ("null query");
      if (! query.complete ())
         throw new IllegalArgumentException ("incomplete query: " + query);
   }

   /**
      Checks an <code>ObjectID</code> argument.
      
      @exception IllegalArgumentException
         if the argument check failed
   */
   protected void checkArg (ObjectID oid)
   {
      if (oid == null)
         throw new IllegalArgumentException ("null object id");
      if (! oid.complete ())
         throw new IllegalArgumentException ("incomplete object id: " + oid);
   }

   /**
      Checks an <code>AddressID</code> argument.
      
      @exception IllegalArgumentException
         if the argument check failed
   */
   protected void checkArg (AddressID aid)
   {
      if (aid == null)
         throw new IllegalArgumentException ("null address id");
      if (! aid.complete ())
         throw new IllegalArgumentException ("incomplete address id: " + aid);
   }

   /**
      Checks a <code>TypedAddressFact</code> argument.
      
      @exception IllegalArgumentException
         if the argument check failed
   */
   protected void checkArg (TypedAddressFact fact)
   {
      if (fact == null)
         throw new IllegalArgumentException ("null protocol address factory");
   }

   /**
      Checks a <code>Notifiable</code> argument.
      
      @exception IllegalArgumentException
         if the argument check failed
   */
   protected void checkArg (Notifiable note)
   {
      if (note == null)
         throw new IllegalArgumentException ("null notifiable");
   }

   /**
      Transforms a query containing client data types to a query containing
      corresponding server data types. In order to shield the resolver from
      client modifications, shared references to mutable data are avoided by
      copying all of the client's data.

      @return     a transformed copy of the query
      @exception  ClassCastException if the transformation could not be made.
   */
   static protected Query client2server (Query client_query)
   {
      Enumeration query_enum = client_query.getObjAddrSelectors ();
      Query server_query = new QueryImpl ();
      while (query_enum.hasMoreElements ()) {

         ObjAddrSelector oa_sel = (ObjAddrSelector) query_enum.nextElement ();

         // this is where the transformation happens
         ObjAddrSelector server_oa_sel = new ObjAddrSelectorImpl ();

         CltObjHandleImpl ohandle = (CltObjHandleImpl)oa_sel.getObjectHandle ();
         // CltObjHandle protects any mutable data
         server_oa_sel.setObjectHandle (ohandle.getFullHandle ());
         // mutable => a copy must be made
         server_oa_sel.setPropertySelector (
            (PropertySelector) oa_sel.getPropertySelector ().clone ());

         server_query.addObjAddrSelector (server_oa_sel);
      }
      return server_query;
   }

   /**
      Transforms look-up results containing server data types to look-up
      results containing corresponding client data types. The transformation
      is made by modification, rather than copying.

      @exception  ClassCastException if the transformation could not be made.
   */
   static protected void server2client (ObjectCAddr [] results,
                        TypedAddressFact ta_fact) throws ProtocolException
   {
      for (int i = 0; i < results.length; i++) {

         ObjectCAddr oa = results [i];

         ObjHandleRep server_ohandle = (ObjHandleRep) oa.getObject ();
         CAddrRep server_address = (CAddrRep) oa.getAddress ();

         CltObjHandle client_ohandle = new CltObjHandleImpl (server_ohandle);
         CltCAddrImpl client_address = new CltCAddrImpl ();
         client_address.fromCAddrRep (server_address, ta_fact);

         oa.setObject (client_ohandle);
         oa.setAddress (client_address);
      }
   }
}
