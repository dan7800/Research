/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.comm.rpcx.out.proto;

import vu.globe.svcs.gls.active.*;
import vu.globe.svcs.gls.active.proto.ActiveQueue;
import vu.globe.svcs.gls.active.proto.DebugActiveQueue;
import vu.globe.svcs.gls.active.proto.ActiveTimerFactImpl;
import vu.globe.svcs.gls.active.ActiveTimerFact.ActiveTimer;
import vu.globe.svcs.gls.alert.AlertID;
import vu.globe.svcs.gls.comm.*;
import vu.globe.svcs.gls.comm.rpcx.demux.*;
import vu.globe.svcs.gls.config.*;
import vu.globe.svcs.gls.protocol.ProtRpcMessage;
import vu.globe.svcs.gls.protocol.ProtRpcMessageFact;
import vu.globe.svcs.gls.types.*;
import vu.globe.svcs.gls.types.ObjHandleRep;
import vu.globe.svcs.gls.stats.Statistics;
import vu.globe.svcs.gls.stats.NodeStatistics;
import vu.globe.svcs.gls.debug.Debug;
import vu.globe.util.time.Stopwatch;

import vu.globe.util.comm.idl.rawData.rawDef;

import vu.globe.util.exc.*;

import java.io.IOException;
import java.net.ProtocolException;
import java.util.*;


/**
   A component of the RPC layer which manages outgoing RPCs by implementing
   the <code>OutRpcOriented</code> interface. This component assumes that
   address management and end-point behaviour are performed by the Mapper,
   which converts between logical and physical node addresses.
   <p>
   An <code>OutRpcOrientImpl</code> is <code>Active</code>, and can be made to
   notify the user of incoming responses and pending exceptions. A single user
   can be supported. The user is not required to pass a registration ID on
   deactivation.
 
   @author Patrick Verkaik
*/

/*
   Data structures
   ---------------

   There is a table of outstanding outbound RPCs indexed by an RPC identifier.
   RPCs are entered by sendRequest() when an RPC is sent. They are removed
   when their reply is enqueued onto the event queue by scheduleOrderedRPC()
   and scheduleUnorderedRPC() (see below).

   Ordered RPCs are additionally placed on a queue of outstanding outbound
   RPC's, one for each object handle. (A 'pseudo object handle' queue is used
   for ordered RPCs that are not associated by the user with an object handle).
   RPCs are enqueued by sendRequest(). They are dequeued by scheduleOrderedRPC()
   for reply scheduling.
   
   We have an event queue for user-bound events. After a successful Demux
   poll, status responses and unordered replies (replies to unordered RPCs)
   are moved onto this queue immediately. Replies to ordered RPCs first go
   through their respective object handle queues.
   The user event queue is able to notify the user directly, without going
   through a Notifiable interface in this object.

   In the event that a parent node crashes, we (if we're a node) need to 
   resend all pending RPC requests to that parent. (See the LS Crash Recovery 
   papers.) This is done by iterating the outstanding outbound queues (in the 
   order the requests were added) for each object handle and resending
   the request whose destination is the node that crashed.

   Epochs
   ------

   An RPC requestor places the epoch of the process in each request it sends.
   When an RPC executor responds to a request, it will copy the epoch of the
   request into the response. The requestor will ignore any responses that have
   old epoch numbers. See the documentation about the why's and wherefores.

   Locking
   -------

   First note: the Demux is polled and subsequent message processing is
   performed not only on event processing but also through pollResponse().
   Therefore, both the callback thread and user threads may be involved.

   More importantly, transmission by the Messenger and queuing of the RPC
   in the object handle queue must be atomic: RPC requests must be
   transmitted by the Messenger in the same order as the eventual scheduling
   of their replies.

   For simplicity all this is protected by the object lock.
   Note that the user is notified using an ActiveQueue. The ActiveQueue
   interface specifies that it consists of downcalling methods only.
   Therefore it is safe to keep our notification methods locked while events
   are queued. (See the LS documentation about this.)

   [
      What follows is the beginning of an outline for a more fine grained object
      locking approach, not currently implemented:

      When creating new outbound RPCs, there are two data structures that need
      to be updated: the table of outbound RPCs, and object handle queue of
      outbound RPCs.
      
      The existence of an RPC is not known to the outside world until its
      request is sent. Note however that we need to protect against illegal
      RPC ids from outside.
   
      We preserve the following invariants:
         RPC in object handle queue => RPC in RPC table
         RPC in RPC table and not in object handle queue <=> unordered RPC
         RPC in object handle queue <=> ordered RPC
         Existing RPC entries in table and queues are only modified to add a
         reply.
   
      *Ordered RPC Replies*
   
      Dequeuing an ordered RPC from an object handle queue is sufficient to
      protect its protect all usage of the RPC
      Once an ordered RPC is dequeued from
   
      The table entries are produced when an RPC is sent in sendRequest(). They
      are consumed by scheduleOrderedRPC() where scheduling takes place by the
      transfer to the event queue.
   
      *Status response wrt reply*
   
      Status responses may only be delivered to the user before the reply is.
      A status response may be ignored if a reply has been, or is about to be
      delivered. To ensure this, the decision to enqueue a status response as 
      an event is accompanied by an atomic check that a reply has not been 
      received.
   ]

*/

public class OutRpcOrientImpl implements OutRpcOriented, Active
{
   /* Environment */

   // Use of the Messenger layer.
   private Messenger msger;

   // Use of the Mapper layer.
   private Mapper mapper;
   
   // Use of the demux
   private RpcDemux demux;
   private ActiveRID demux_reg;     // Set when activated.

   private ProtRpcMessageFact prpc_fact;

   // User info
   private ActiveQueue user_events;

   // Timeouts
   private ActiveQueue timeout_events;

   // whether the user wants to know about requests that timeout
   private boolean notify_timeouts = false;

   // timer factory to time out requests
   private ActiveTimerFact timer_fact;
   private ActiveRID timeout_timer_reg;     // Set when activated.

   // 16 bit version number
   private static final int VERSION = 3;

   // Epoch of this Rpc layer.
   private long my_epoch;
   
   // have at most 2 users: one for responses, and one for request timeouts
   private int n_users = 0;      // Current number of users.

   // use of statistics
   private Statistics stats;
   private NodeStatistics node_stats;

   // timer for statistics
   private Stopwatch marshall_stopwatch;
   private Stopwatch layer_stopwatch;

   /* RPC data structures */
   
   // Table of outbound RPCs
   OutRpcTable out_rpcs;

   // Table of outbound object handles.
   OutObjectHandleTable out_ohandles;

   // A special pseudo object handle for reliable RPCs without object handle
   // (see RpcHandle.java)
   OutObjectHandle pseudo_out_ohandle;

   /**
      Initialises this object with the given environment.

      @param msger      the message sender
      @param mapper     the address mapper
      @param dmx        the demultiplexer
      @param env        the environment
   */
   public OutRpcOrientImpl (Messenger msger, Mapper mapper, RpcDemux dmx, 
      ObjEnv env)
   {
      // Set the environment.
      this.msger = msger;
      this.mapper = mapper;
      demux = dmx;
      Scheduler scheduler  = env.getScheduler();
      prpc_fact = env.getProtRpcX();
      my_epoch = env.getEpoch();
      stats = env.getStatistics();
      timer_fact = new ActiveTimerFactImpl (scheduler, stats);

      if (stats instanceof NodeStatistics)
         node_stats = (NodeStatistics) stats;

      user_events = new ActiveQueue (scheduler);
      // user_events = new DebugActiveQueue (scheduler, "outrpc delivery",
      //                                         100);
      timeout_events = new ActiveQueue (scheduler);
      // timeout_events = new DebugActiveQueue (scheduler, "outrpc timeouts",
      //                                         100);


      out_rpcs = new OutRpcTable ();
      out_ohandles = new OutObjectHandleTable ();
      pseudo_out_ohandle = new OutObjectHandle ();

      if(Debug.statsTime())
      {
         marshall_stopwatch = new Stopwatch();
         layer_stopwatch = new Stopwatch();
      }
   }


   // Active interface
   public ActiveRID activate (int service, Notifiable note, ActiveUID userInfo)
   {
      switch(service)
      {
         case RpcService.SERVICE_RESPONSES:    
            // Activate the event queue
            user_events.activate (note, userInfo);

            // Activate the demultiplexer

            Notifiable demux_note = new Notifiable ()
            {
               public void notifyEvent (ActiveUID ui) {
                  notifyDemuxEvents (ui);
               }
            };

            demux_reg = demux.activate (RpcDemux.SERVICE_RESPONSES, demux_note, null);
            break;
         case RpcService.SERVICE_REQUEST_TIMEOUTS:
            timeout_events.activate(note, userInfo);
            notify_timeouts = true;
            break;
         default:
            throw new IllegalArgumentException ("unknown service id");
      }
     
      // activate timer registration id on first activation of this object
      synchronized (this)
      {
         if (n_users++ == 0) 
         {
            Notifiable timeout_note = new Notifiable ()
            {
               public void notifyEvent (ActiveUID ui) {
                  notifyTimeoutEvents (ui);
               }
            };
            timeout_timer_reg = timer_fact.activate(timeout_note, null);
         }
      }


      return new OutRpcOrientRID(service);
   }

   public ActiveRID activate (Notifiable note, ActiveUID userInfo)
   {
      throw new NotSupportedException ("activate without service id");
   }

   public void deactivate (ActiveRID regID)
   {
      OutRpcOrientRID out_rid = (OutRpcOrientRID) regID;
      int service = out_rid.getService();

      switch(service)
      {
         case RpcService.SERVICE_RESPONSES: 
            demux.deactivate (demux_reg);
            user_events.deactivate (null);
            break;
         case RpcService.SERVICE_REQUEST_TIMEOUTS:
            timeout_events.deactivate (null);
            break;
         default:
            throw new IllegalArgumentException ("unknown registration id");
      }

      // deactivate timers on last deactivation of this object
      synchronized (this)
      {
         if(--n_users == 0)
            timer_fact.deactivate (timeout_timer_reg); 
      }
   }

   // OutRpcOriented interface
   public synchronized void sendRequest (RpcRequest request, AlertID id) 
                                                            throws IOException
   {
      if(Debug.statsTime())
         layer_stopwatch.start();

      RpcHandle rpc_handle = request.getRpcHandle ();
      ObjHandleRep ohandle = rpc_handle.getObjectHandle ();
      int qos = rpc_handle.getQoS ();

      // Allocate a new outbound RPC and enter it into the table.

      RpcIdentifier rpc_id = new RpcIdentifier ();
      OutRpc orpc = new OutRpc (rpc_id, request);
      out_rpcs.put (rpc_id, orpc);

      // An ordered outbound RPC needs an OutObjectHandle. If necessary,
      // use the pseudo_out_ohandle.
      OutObjectHandle out_ohandle = null;

      int conn;
      switch (qos) {       // Find a connection number, and enqueue in 
                           // an object handle queue if necessary.
      case RpcHandle.QOS_ORDERED :


         // Determine a positive connection number from the optional object
         // handle. If the object handle is not provided, then use a special
         // connection number.

         if (ohandle == null) {
            // Any positive integer. Clashes are harmless.
            conn = Message.CONN_ORDERED_MIN;
            out_ohandle = pseudo_out_ohandle;
         }
         else {
            // Map hash code to a positive integer. Avoiding clashes is a
            // performance matter and is not attempted.
            int hash = ohandle.hashCode ();
            conn = hash > 0 ? hash : (hash < 0 ? -hash : 1);
            out_ohandle = out_ohandles.provide (ohandle);
         }

         orpc.setOutObjectHandle (out_ohandle);
         out_ohandle.enqueue (orpc);
         break;
      case RpcHandle.QOS_UNRELIABLE :
         conn = Message.CONN_UNRELIABLE;
         // No queueing in object handle queue, since unordered.

         break;
      default :
         throw new AssertionFailedException ("RPC handle's QoS");
      }

      ProtRpcMessage prpc = makeProtRpc (VERSION, my_epoch, rpc_id.getID (),
            qos, ProtRpcMessage.KIND_REQUEST, ohandle, request.getPayload ());
      rawDef packet = prpc.marshall ();

      Message message = new MessageImpl ();
      message.setConnectionNumber (conn);
      message.setPayload (packet);

      // set the physical destination
      Peer receiver = mapper.logicalToPhysical(rpc_handle.getExecutor(), 
         ohandle.getLocationID());
      message.setReceiver (receiver);

      Debug.println("RPC sending REQUEST rpc #" + rpc_id.getID() + " to " +
        receiver);

      if(Debug.statsTime())
         layer_stopwatch.stop();

      msger.send(message, null);

      if(Debug.statsTime())
         layer_stopwatch.resume();

      // finally, set the timeout (if any) for this request.
      if (request.getTimeout() != RpcRequest.TIMEOUT_NONE)
      {
         ActiveTimer timeout_timer = timer_fact.createTimer(timeout_timer_reg);
         timeout_timer.set(request.getTimeout(), orpc);
         request.setTimer(timeout_timer);
      }

      debugSendRequest (orpc);

      stats.addRequestRPCSent(1);

      if(Debug.statsTime())
      {
         layer_stopwatch.stop();
         stats.addRPCLayerTime(layer_stopwatch);
      }
   }
   
   public RpcResponse pollResponse () throws IOException
   {
      // Dequeue a user event, and return that.
      // If necessary, poll the Demux once.

      OutRpcEvent event = (OutRpcEvent) user_events.dequeue ();
      if (event == null) {
         pollDemux ();
         event = (OutRpcEvent) user_events.dequeue ();

         if (event == null)
            return null;
      }

      if (event.exc != null)
         throw event.exc;
      return event.response;
   }

   // Implementation of demultiplexer Notifiable interface
   private void notifyDemuxEvents (ActiveUID userInfo)
   {
      try {
         pollDemux ();
      }
      catch (IOException exc) {
         user_events.enqueue (new OutRpcEvent (exc));
      }
   }

   /**
      Polls the demux and, if successful, enqueues an event.
   */
   private synchronized void pollDemux () throws IOException
   {
      RpcDemuxMessage demux_msg = demux.pollResponse ();
      if (demux_msg == null)
         return;

      if(Debug.statsTime())
         layer_stopwatch.start();

      Message msg = demux_msg.getMessage ();
      ProtRpcMessage prpc = demux_msg.getRpcMessage ();

      // Check the correctness of the incoming RPC response.
      if (prpc.getVersion () != VERSION)
         throw new ProtocolException ("RPC response with unknown version");

      // check epoch
      long epoch = prpc.getRequestorEpoch ();

      if (epoch != my_epoch) {
         System.err.println ("ignoring RPC response with old epoch ("
               + epoch + ")");

         if(Debug.statsTime())
         {
            layer_stopwatch.stop();
            stats.addRPCLayerTime(layer_stopwatch);
         }

         return;
      }

      RpcIdentifier id = new RpcIdentifier (prpc.getRpcID ());
      OutRpc orpc = out_rpcs.get (id);

      if (orpc == null) {
         System.err.println ("ignoring unexpected RPC response (unknown id: " +
               + id.getID () + ")");
      
         if(Debug.statsTime())
         {
            layer_stopwatch.stop();
            stats.addRPCLayerTime(layer_stopwatch);
         }

         return;
      }

      // cancel the timeout timer if set.
      RpcRequest req = orpc.getRequest();
      ActiveTimer timer = req.getTimer();
      if(timer != null)
         timer.cancel();

      RpcHandle rpc_handle = req.getRpcHandle ();
      int request_qos = rpc_handle.getQoS ();

      // see if we should acknowledge the message
      if(msg.userMustAcknowledge())
      {
         if(Debug.statsTime())
            layer_stopwatch.stop();

         msger.acknowledge(msg); 

         if(Debug.statsTime())
            layer_stopwatch.resume();
      }

      if (orpc.getReply () != null) {
         // a reply has been received earlier, therefore the RPC is 'closed'

         // An unexpected response is a protocol error only in the case of
         // QOS_ORDERED RPC.

         switch (request_qos) {
            case RpcHandle.QOS_UNRELIABLE:
               System.err.println ("ignoring unexpected RPC response");
               return;
            case RpcHandle.QOS_ORDERED:
               throw new ProtocolException ("unexpected RPC response");
            default:
               throw new AssertionFailedException ("Outbound RPC's QoS");
         }
      }

      if (prpc.getQoS () != request_qos)
         throw new ProtocolException ("RPC response's QoS");

      switch (prpc.getKind ()) {
         case ProtRpcMessage.KIND_REPLY:
            Debug.println("RPC received RESPONSE REPLY rpc #" + id.getID());
            processIncomingReply (prpc, orpc);
            break;
         case ProtRpcMessage.KIND_STATUS:
            Debug.println("RPC received RESPONSE STATUS rpc #" + id.getID());
            processIncomingStatus (prpc, orpc);
            break;
         default:
            throw new ProtocolException ("RPC response's type");
      }
      debugIncoming (orpc);

      if(Debug.statsTime())
      {
         layer_stopwatch.stop();
         stats.addRPCLayerTime(layer_stopwatch);
      }
   }

   /**
      Processes an incoming reply for a currently unresolved outbound RPC.
   
      @param prpc    an incoming reply
      @param orpc    the associated outbound RPC, currently unresolved
   */
   private synchronized void processIncomingReply (ProtRpcMessage prpc,
            OutRpc orpc) throws ProtocolException
   {
      // construct a user-level reply
      RpcHandle rpc_handle = orpc.getRequest ().getRpcHandle ();
      RpcReply reply = new RpcReplyImpl (rpc_handle, prpc.getPayload ());
      orpc.setReply (reply);

      switch (prpc.getQoS ()) {
         case RpcHandle.QOS_UNRELIABLE:
            // Unordered RPCs can be scheduled at any time.
            scheduleUnorderedRPC (orpc);
            break;
         case RpcHandle.QOS_ORDERED:
            // Ordered RPCs have to be scheduled in turn.
            OutObjectHandle out_ohandle = orpc.getOutObjectHandle ();
            if (out_ohandle == null)
               out_ohandle = pseudo_out_ohandle;
            schedOHandleQueue (out_ohandle);
            break;
         default:
            throw new ProtocolException ("RPC reply's QoS");
      }
   }

   /**
      Processes an incoming status response for a currently unresolved
      outbound RPC.
   
      @param prpc    an incoming status response
      @param orpc    the associated outbound RPC, currently unresolved
   */
   private synchronized void processIncomingStatus (ProtRpcMessage prpc,
            OutRpc orpc) throws ProtocolException
   {
      // construct a user-level status response
      RpcHandle rpc_handle = orpc.getRequest ().getRpcHandle ();
      RpcStatus status = new RpcStatusImpl (rpc_handle, prpc.getPayload ());

      // Statuses can be scheduled immediately.
      user_events.enqueue (new OutRpcEvent (status));
   }
   
   /**
      Polls timeout queue and, if successful, enqueues a timeout event.
   */
   public RpcRequest pollTimeout () throws IOException
   {
      // Dequeue a timeout event, and return that.

      OutRpcTimeoutEvent event = (OutRpcTimeoutEvent) timeout_events.dequeue ();
      if (event == null)
         return null;

      if (event.exc != null)
         throw event.exc;
      return event.request;
   }

   // Implementation of timeout Notifiable interface
   private void notifyTimeoutEvents (ActiveUID userInfo)
   {
      if(Debug.statsTime())
         layer_stopwatch.start();

      try {
         OutRpc orpc = (OutRpc) userInfo;
         processTimeoutEvent (orpc);
      }
      catch (IOException exc) {
         timeout_events.enqueue (new OutRpcTimeoutEvent (exc));
      }
      
      if(Debug.statsTime())
      {
         layer_stopwatch.stop();
         stats.addRPCLayerTime(layer_stopwatch);
      }
   }

   /**
      Processes a timeout event for a RPC request that has timed out.
   
      @param prpc    the request that has timed out.
   */
   private synchronized void processTimeoutEvent (OutRpc orpc) 
                                                            throws IOException
   {
      // delete the request from the out rpc table
      RpcIdentifier rpc_id = orpc.getID();
      out_rpcs.remove(rpc_id);

      // remove from object handle queue
      OutObjectHandle ohandle = orpc.getOutObjectHandle();

      if(ohandle != null)
      {
         // delete this request from queue.
         ohandle.remove(orpc);

         // if queue is now empty, delete from table.
         if(ohandle.isEmpty())
         {
            out_ohandles.remove
               (orpc.getRequest().getRpcHandle().getObjectHandle());
         }
      }

      // finally, if the user wants to know about timed-out requests,
      // let them know.
      if(notify_timeouts)
         timeout_events.enqueue(new OutRpcTimeoutEvent(orpc.getRequest()));

      stats.addRequestRPCTimeout(1);
   }

   /**
      Processes an crash resend notice.
   
      @param dest    the Peer to resend pending requests to
   */
   public synchronized void resendPendingRequests(Peer dest) 
                                          throws ProtocolException, IOException
   {
      if(Debug.statsTime())
         layer_stopwatch.start();

      Debug.println("RPC received RESEND");
	
      RpcIdentifier rpc_id;
      OutRpc orpc;
      RpcRequest request;
      RpcHandle rpc_handle;
      int conn;
      int qos;
      OutObjectHandle out_ohandle;
      ObjHandleRep ohandle;
      Peer logical_dest;
      Peer physical_dest;
      LocationID location;
      int hash;

      // resend all pending requests to 'dest' in the order they were 
      // originally sent.

      Enumeration out_queues = out_ohandles.elements();
      while(out_queues.hasMoreElements())
      {
         out_ohandle = (OutObjectHandle) out_queues.nextElement();

         // see if the crashed peer is the same as the destination of this 
         // object handle.

         // first get the logical destination and location id
         orpc = (OutRpc) out_ohandle.peekHead(); 
         request = orpc.getRequest();
         rpc_handle = request.getRpcHandle();
         ohandle = rpc_handle.getObjectHandle();
         location = ohandle.getLocationID();
         logical_dest = rpc_handle.getExecutor();

         // now convert to a physical destination
         physical_dest = mapper.logicalToPhysical(logical_dest, location);

         if(dest.equals(physical_dest))
         {
            // iterate over this object handle queue in the order that elements 
            // were added to it
            ListIterator object_queue = out_ohandle.listIterator();
            while(object_queue.hasNext())
            {
               orpc = (OutRpc) object_queue.next();
               request = orpc.getRequest();
               rpc_handle = request.getRpcHandle();
               ohandle = rpc_handle.getObjectHandle();
               rpc_id = orpc.getID();
               qos = rpc_handle.getQoS();
		   
               switch (qos) {  // Find a connection number, and enqueue in
                     // an object handle queue if necessary.
                     case RpcHandle.QOS_ORDERED :
   
                     // Determine a positive connection number from the 
                     // optional object handle. If the object handle is not 
                     // provided, then use a special connection number.
   
                     // An ordered outbound RPC needs an OutObjectHandle. If 
                     // necessary, use the pseudo_out_ohandle.
   
                        if (ohandle == null) {
                              // Any positive integer. Clashes are harmless.
                              conn = Message.CONN_ORDERED_MIN;
                              out_ohandle = pseudo_out_ohandle;
                        }
                        else {
                           // Map hash code to a positive integer. Avoiding 
                           // clashes is a performance matter and is not 
                           // attempted.
                              hash = ohandle.hashCode ();
                              conn = hash > 0 ? hash : (hash < 0 ? -hash : 1);
                        }
                        break;
                     case RpcHandle.QOS_UNRELIABLE :
                        conn = Message.CONN_UNRELIABLE;
                        break;
                     default :
                        throw new AssertionFailedException ("RPC handle's QoS");
               }
   
               ProtRpcMessage new_prpc = makeProtRpc (VERSION, my_epoch, 
                  rpc_id.getID(), qos, ProtRpcMessage.KIND_REQUEST, ohandle, 
                  request.getPayload ());
               rawDef packet = new_prpc.marshall ();
   
               Message message = new MessageImpl ();
               message.setConnectionNumber (conn);
               message.setPayload (packet);
               message.setReceiver (dest);

               Debug.println("RPC RESENDING REQUEST rpc #" + rpc_id.getID() +
	         " to " + dest);
  
               if(Debug.statsTime())
                  layer_stopwatch.stop();

               msger.send (message, null);
   
               if(Debug.statsTime())
                  layer_stopwatch.resume();

               debugSendRequest (orpc);

               node_stats.addRequestRPCResent(1);
            }
         }
      }	

      Debug.println("RPC finished RESEND");
      
      if(Debug.statsTime())
      {
         layer_stopwatch.stop();
         stats.addRPCLayerTime(layer_stopwatch);
      }
   }
  
   /**
      Transfers any resolved outstanding RPCs at the head of out_ohandle's
      queue to the event queue. Also removes them from the RPC table.
   */
   private synchronized void schedOHandleQueue (OutObjectHandle out_ohandle)
   {
      while (scheduleOrderedRPC (out_ohandle))
         ;
   }

   /**
      Attempts to process a resolved RPC at the head of out_ohandle's queue.
      The event is dequeued, removed from the RPC table, and queued as a user
      event.

      @return
         false if no resolved RPC could be scheduled
   */
   private synchronized boolean scheduleOrderedRPC (OutObjectHandle out_ohandle)
   {
      if (out_ohandle.isEmpty())
         return false;
      OutRpc orpc = out_ohandle.peekHead ();
      RpcReply reply = orpc.getReply ();
      if (reply == null)
         return false;

      // Remove from table and queue
      out_ohandle.dequeue ();
      out_rpcs.remove (orpc.getID ());

      if(out_ohandle.isEmpty())
         out_ohandles.remove(orpc.getRequest().getRpcHandle().getObjectHandle());
      user_events.enqueue (new OutRpcEvent (reply));
      return true;
   }

   /**
      Processes a resolved RPC. The event is removed from the RPC table, and
      queued as a user event.
   */
   private synchronized void scheduleUnorderedRPC (OutRpc orpc)
   {
      RpcReply reply = orpc.getReply ();
      out_rpcs.remove (orpc.getID ());
      user_events.enqueue (new OutRpcEvent (reply));
   }

   /**
      Constructs an RPC protocol container. The protocol input fields consist
      of the arguments. The protocol allows its optional input fields to have
      invalid values (e.g. null pointers, special constants).

      @exception IllegalArgumentException
         if one of the non-optional protocol input fields was invalid.
   */
   private ProtRpcMessage makeProtRpc (int version, long epoch, int id, int qos,
               int kind, ObjHandleRep obj, rawDef payload)
   {
      if(Debug.statsTime())
         marshall_stopwatch.start();

      ProtRpcMessage prpc;

      prpc = prpc_fact.createProtRpcMessage ();
      prpc.setVersion (version);
      prpc.setRequestorEpoch (epoch);
      prpc.setRpcID (id);
      prpc.setQoS (qos);
      prpc.setKind (kind);
      prpc.setObjectHandle (obj);
      prpc.setPayload (payload);
      
      if(Debug.statsTime())
      {
         marshall_stopwatch.stop();
         stats.addRPCMarshallTime(marshall_stopwatch);
      }

      return prpc;
   }

   // debugging hooks. These can be overridden by a debugging subclass.
   
   /**
      Empty debugging hook. This method is called by <code>sendRequest</code>
      at the point a message is sent. A debugging subclass can override this
      hook to print some info.
   */
   protected void debugSendRequest (OutRpc out_rpc)
   {
   }

   /**
      Empty debugging hook. This method is called at the point a response
      has arrived. The reply field of the <code>OutRpc</code> is filled in
      iff the response is a reply. A debugging subclass can override this hook
      to print some info.
   */
   protected void debugIncoming (OutRpc out_rpc)
   {
   }
}
