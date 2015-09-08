/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.resolver.server;

import vu.globe.svcs.gls.active.*;
import vu.globe.svcs.gls.typedaddress.TypedAddress;
import vu.globe.svcs.gls.types.*;
import vu.globe.svcs.gls.resolver.ActiveResolverOps;
import java.io.IOException;

/**
   <code>ResolverServer</code> represents the resolver server as a Java
   interface. All contact addresses and object handles passed to
   <code>ActiveResolverOps</code> operations must be of type
   <code>CAddrRep</code> and <code>ObjHandleRep</code>. Similarly, all object
   handles and contact addresses returned by these operations are of these
   types.
 
   @author Patrick Verkaik
*/

public interface ResolverServer extends ActiveResolverOps
{
   /**
      Hooks up the the resolver to the location service. This operation must
      be completed before invoking any of the <code>ActiveResolverOps</code>
      operations. The operation is completed by a successful call to
      <code>assessLocalise</code>. The user will be called back when the
      operation may be successfully completed. This method may be invoked by
      callback methods.

      @param   my_addr  the address of this resolver, possibly null or partial
      @param   node     the leaf node representing the location service
      @param   location the location of the resolver server
      @param   note     The callback interface to use for notification, or null
                        if no notification should take place.
      @param   userInfo a user-defined cookie to use for notification.
      @return  the object's registration id.
   */
   ActiveRID localise (TypedAddress my_addr, NodeID node, LocationID location,
            Notifiable note, ActiveUID userInfo) throws IOException;

   /**
      Checks the progress of the <code>localise</code> operation identified by
      a registration ID. This method is carried out without delaying, and may
      be invoked by callback methods.
 
      @param regID   The registration id obtained when the <code>localise</code>
                     operation was initiated.
      @return  true iff the operation has completed.
      @exception LocatorException
         if the location service node could not complete the operation
   */
   boolean assessLocalise (ActiveRID regID) throws IOException;

   /**
      Retrieves the local domain ID. This method is carried out without
      delaying, and may be invoked by callback methods.

      @return     the local domain ID, or null if the resolver has not yet
                  been localised.
   */
   DomainID getDomain ();
   
   /**
      Retrieves the local location ID. This method is carried out without
      delaying, and may be invoked by callback methods.

      @return     the local location ID, or null if the resolver has not yet
                  been localised.
   */
   LocationID getLocation ();


   /**
      Unhooks the resolver from the location service. Note: does not guarantee
      that on return all callbacks from this object will have ceased. This
      method may be invoked by callback methods.
   */
   void unlocalise ();
}
