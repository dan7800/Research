/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.resolver.stdclient;

import vu.globe.svcs.gls.resolver.StdResolverOps;
import vu.globe.svcs.gls.resolver.server.ResolverServer;
import vu.globe.svcs.gls.active.*;

/**
   <code>StdClient</code> is a default resolver client representing the
   location service to a Java application.
   A <code>StdClient</code> will provide consistency of its look-up
   operations with respect to its *previous* insertions of *local* addresses.
   <p>
   The default behaviour of a <code>StdClient</code> is that it need not wait
   for its update operations to finish completely (i.e. for its updates to be
   processed throughout the location service). In particular, a
   <code>StdClient</code> might not wait for update operations at all (i.e.
   updates are performed asynchronously) or it might wait for an update
   operation to update the local LS domain only. However, there are two
   mechanisms available to the user to find out an update has been processed
   throughout the location service:
   <ul>
   <le><code>StdClient</code> is able to notify the user of globally completed
       update operations.
   <le>The default behaviour described can be overridden by calling
       <code>blockUpdatesUntilCompletion<code>. In this case,
       <code>StdClient</code> does wait for its update operations to finish
       completely.
   </ul>
   These two mechanisms are mutually exclusive. Only one can be used.
   <p>
   The <code>Active</code>
   interface can be used to register a <code>Notifiable</code> interface.
   A notification is linked to the corresponding update by the user
   cookie passed to the <code>StdResolverOps</code> update operation.
   A single user is assumed. The registration ID returned by
   <code>activate</code> can be ignored. This interface is used to implement
   the former of the above mechanisms.
   <p>
   As an <code>IDManager</code>, a <code>StdClient</code> is able to create
   instances of the object handle and contact address of types
   <code>CltObjHandle</code> and <code>CltCAddr</code>. It will also create
   instances of these types when returning the results of a <code>lookup</code>
   operation.
   <p>
   A <code>StdClient</code> is only required to accept (as update input, or
   in a look-up <code>Query</code>) object handles and contact addresses which
   it has itself created.
 
   @author Patrick Verkaik
*/

public interface StdClient extends StdResolverOps, IDManager, Active
{
   /**
      Specifies the resolver server this client should use. Must be
      called before operations in the <code>StdResolverOps</code>,
      <code>IDManager</code> and <code>Active</code> interfaces.

      @param   server   a localised resolver server
   */
   void setServer (ResolverServer server);

   /**
      Overrides the default <code>StdClient</code> behaviour: after calling
      this method <code>StdClient</code> waits for its update operations to
      finish completely. Must be called before operations in the
      <code>StdResolverOps</code>, <code>IDManager</code> and
      <code>Active</code> interfaces.
    */
   void blockUpdatesUntilCompletion();
}
