/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.comm.rpcx.demux.proto;

import vu.globe.svcs.gls.config.*;
import vu.globe.svcs.gls.protocol.ProtRpcMessage;
import vu.globe.svcs.gls.protocol.ProtRpcMessageFact;
import vu.globe.util.comm.idl.rawData.rawDef;
import vu.globe.util.comm.RawOps;
import vu.globe.svcs.gls.comm.*;
import vu.globe.svcs.gls.comm.rpcx.*;
import vu.globe.svcs.gls.comm.rpcx.demux.*;
import vu.globe.svcs.gls.active.*;
import vu.globe.svcs.gls.active.proto.ActiveQueue;
import vu.globe.svcs.gls.active.proto.DebugActiveQueue;
import vu.globe.svcs.gls.types.*;
import vu.globe.svcs.gls.stats.Statistics;
import vu.globe.svcs.gls.stats.NodeStatistics;
import vu.globe.util.time.Stopwatch;

import vu.globe.util.exc.NotSupportedException;
import java.io.IOException;
import java.net.ProtocolException;

import vu.globe.util.types.Queue;

import vu.globe.svcs.gls.debug.Debug;

/**
   A component of the RPC layer which demultiplexes incoming data. Three kinds 
   of users are supported: those that are interested in RPC requests, those
   interested in RPC responses, and those interested in 'end recovery' RPCs. 
   RPC messages are demultiplexed accordingly.
   <p>
   The demultiplexer assumes address management and end-point behaviour
   are performed by the Messenger.
 
   @author Patrick Verkaik
*/

/*
   Each callback user has an active queue in which events are placed
   of which the user should be notified. Scheduling of events in a queue is
   performed by the queue itself. The queue is able to notify the user without
   going through a demux Notifiable interface.

   If we are a node, and in 'recovery' mode, then the 'end recovery' RPCs 
   Recovery must be treated specially. They must not be scheduled before any
   RPC requests that came from the same host. To accomplish this, received
   'end recovery' RPCs are places in a non-active queue if the request
   active queue is not empty. When the latter queue becomes empty, the
   former queue's contents are transferred to the active queue for Recovery.

   Events are represented by an RpcDemuxEvent. Events representing RPC messages
   are placed in the queue handling that particular type of RPC message. Events
   corresponding to exceptions have no particular destination. They are
   printed, but otherwise ignored.

   Each time Messenger is polled, the result is enqueued. This guarantees that 
   the order of messages is preserved in the order of queued RPC messages.  
   pollMessenger () is where this takes place.

   The Messenger is polled in two cases:
   -  a Messenger notification arrives. The Messenger needs to be polled to
      determine in which queue the unmarshalled RPC message should be placed.
   -  The demux is polled by the user, and the corresponding event queue is
      empty (i.e. the user is polling without notification). The Messenger is
      polled until the requested RPC message kind arrives (or the Messenger
      is exhausted). Messages for the other service are just queued.

   Note that RPC messages need never be thrown away. RPC messages arriving
   for a particular service which has not been activated will be queued for
   later use.

   Locking
   -------

   Messenger activation:
   The messenger is activated when n_users > 0, and deactivated once
   n_users == 0. This is protected by the lock.

   Message polling:
   pollMessenger () needs to poll the Messenger and queue a message atomically.
*/

public class RpcDemuxImpl implements RpcDemux
{
   /* Environment */

   // Use of the Messenger layer.
   private Messenger msger;

   // Use of the scheduler.
   private Scheduler sched;

   private ProtRpcMessageFact prpc_fact;

   // RpcRequestor has at most two users.
   // Each has an activation record and an event queue.
   private int n_users = 0;      // Current number of users.

   private ActiveQueue request_queue;
   private ActiveQueue response_queue;
   private ActiveQueue recovery_queue;
   
   // registration ids returned by messenger
   private ActiveRID user_reg;   // user service
  
   // queue to hold recovery rpcs until request queue becomes empty
   private Queue holding_recovery_queue;

   // 16 bit version number
   private static final int VERSION = 3;

   // log incoming rpcs with statistics
   private Statistics stats;
   private NodeStatistics node_stats;

   // Use of Stopwatch for statistics
   private Stopwatch stopwatch;
   private Stopwatch layer_stopwatch;

   /**
      Initialises this object with the given environment.
 
      @param msger  the downstairs neighbour
      @param env        the environment
   */
   public RpcDemuxImpl (Messenger msger, ObjEnv env)
   {
      // Set the environment.
      this.msger = msger;
      sched  = env.getScheduler();
      prpc_fact = env.getProtRpcX();
      stats = env.getStatistics();

      if (stats instanceof NodeStatistics)
         node_stats = (NodeStatistics) stats;

      request_queue = new ActiveQueue (sched);
      response_queue = new ActiveQueue (sched);
      recovery_queue = new ActiveQueue (sched);
      // request_queue = new DebugActiveQueue (sched, "dmx reqs", 100);
      // response_queue = new DebugActiveQueue (sched, "dmx resps", 100);
      // recovery_queue = new DebugActiveQueue (sched, "dmx recovery", 100);

      holding_recovery_queue = new Queue();

      if(Debug.statsTime())
      {
         stopwatch = new Stopwatch();
         layer_stopwatch = new Stopwatch();
      }
   }


   /*
      Active interface.
   */

   public ActiveRID activate (Notifiable note, ActiveUID userInfo)
   {
      throw new NotSupportedException ("no service identifier");
   }

   public ActiveRID activate (int service, Notifiable note, ActiveUID userInfo)
   {
      // First activate a queue, and install the user callback.

      switch (service) {
         case SERVICE_REQUESTS:
            request_queue.activate (note, userInfo);
            break;
         case SERVICE_RESPONSES:
            response_queue.activate (note, userInfo);
            break;
         case SERVICE_RECOVERY:
            recovery_queue.activate (note, userInfo);
            break;
         default:
            throw new IllegalArgumentException ("unknown service id");
      }

      // activate mesger on first activation of this object

      synchronized (this) {
         if (n_users++ == 0) {
            Notifiable msgerNote = new Notifiable () {
               public void notifyEvent (ActiveUID ui)
               {
                  notifyMessengerEvents (ui);
               }
            };
            user_reg = msger.activate (MessengerService.MSGER_USER, 
                           msgerNote, null);
         }
      }
 
      /* Return registration id. */
      return new RpcDemuxRID (service);
   }

   public void deactivate (ActiveRID regID)
   {
      RpcDemuxRID demux_rid = (RpcDemuxRID) regID;
      int service = demux_rid.getService ();

      switch (service) {
         // atomic execution protected by lock
         case SERVICE_REQUESTS:
            if(Debug.statsTime())
            {
               node_stats.setRPCRequestCallbackQueueTotalTime(
                  request_queue.timeQueueWaitTime());
               node_stats.setRPCRequestCallbackQueueAvgTime(
                  request_queue.avgQueueWaitTime());
            }
            request_queue.flush ();
            request_queue.deactivate (null);
            break;
         case SERVICE_RESPONSES:
            if(Debug.statsTime())
            {
               stats.setRPCReplyCallbackQueueTotalTime(
                  response_queue.timeQueueWaitTime());
               stats.setRPCReplyCallbackQueueAvgTime(
                  response_queue.avgQueueWaitTime());
            }
            response_queue.flush ();
            response_queue.deactivate (null);
            break;
         case SERVICE_RECOVERY:
            recovery_queue.flush ();
            recovery_queue.deactivate (null);
            break;
         default:
            throw new IllegalArgumentException ("unknown registration id");
      }
 
      // deactivate mesger on last deactivation of this object

      synchronized (this) {
         if (--n_users == 0)
            msger.deactivate (user_reg);
      }
   }

   // RpcDemuxReceiver interface

   public RpcDemuxMessage pollRequest () throws IOException
   {
      if(Debug.statsTime())
         layer_stopwatch.start();

      pollUntil (SERVICE_REQUESTS);

      RpcDemuxEvent event = (RpcDemuxEvent) request_queue.dequeue ();

      // see if we've just emptied the request queue. if so, transfer
      // the all received 'end recovery' rpcs to recovery.
      if(request_queue.isEmpty() && ! holding_recovery_queue.isEmpty())
      {
         Debug.println("TRANSFERRING SAVED END_RECOVERY RPCs TO RECOVERY");

         int size = holding_recovery_queue.size();

         for(int i = 0; i < size; i++)
         {
            RpcDemuxEvent saved_event = 
               (RpcDemuxEvent) holding_recovery_queue.dequeue();
            recovery_queue.enqueue (saved_event);
         }
         
         Debug.println("MOVED " + size + " end recovery RPCs to Recovery queue");
      }

      if (event == null)
         return null;
      
      IOException exc = event.getException ();
      if (exc != null)
         throw exc;

      if(Debug.statsTime())
      {
         layer_stopwatch.stop();
         stats.addRPCLayerTime(layer_stopwatch);
      }

      return event.getRpcDemuxMessage ();
   }

   public RpcDemuxMessage pollResponse () throws IOException
   {
      if(Debug.statsTime())
         layer_stopwatch.start();

      pollUntil (SERVICE_RESPONSES);

      RpcDemuxEvent event = (RpcDemuxEvent) response_queue.dequeue ();
      if (event == null)
         return null;
      
      IOException exc = event.getException ();
      if (exc != null)
         throw exc;

      if(Debug.statsTime())
      {
         layer_stopwatch.stop();
         stats.addRPCLayerTime(layer_stopwatch);
      }

      return event.getRpcDemuxMessage ();
   }

   public RpcDemuxMessage pollRecovery () throws IOException
   {
      if(Debug.statsTime())
         layer_stopwatch.start();

      pollUntil (SERVICE_RECOVERY);

      RpcDemuxEvent event = (RpcDemuxEvent) recovery_queue.dequeue ();
      if (event == null)
         return null;
      
      IOException exc = event.getException ();
      if (exc != null)
         throw exc;

      if(Debug.statsTime())
      {
         layer_stopwatch.stop();
         stats.addRPCLayerTime(layer_stopwatch);
      }

      return event.getRpcDemuxMessage ();
   }

   // Non-public methods.

   /*
      Notification from Messenger.
   */
   private void notifyMessengerEvents (ActiveUID userInfo)
   {
      /*
         Poll the msger and enqueue. Rely on the active queue itself to
         pass the callback to the user.
      */
      try { pollMessenger (); }
      catch (IOException exc) {
         System.err.println ("RPC demux ignoring exception: " + exc);
         exc.printStackTrace ();
      }
   }

   /**
      Keeps polling the Messenger and queuing messages until the queue
      for the specified service is found to be non-empty, or the Messenger has
      run out of messages.
      <p>
      The order of messages when queued is preserved. Note that even after the
      queue has been found to be non-empty, it is still possible for a
      concurrent thread to empty the queue.
   */
   private void pollUntil (int service) throws IOException
   {
      // Determine the event queue to use.

      ActiveQueue queue;

      switch (service) {
         case SERVICE_REQUESTS:
            queue = request_queue;
            break;
         case SERVICE_RESPONSES:
            queue = response_queue;
            break;
         case SERVICE_RECOVERY:
            queue = recovery_queue;
            break;
         default:
            throw new IllegalArgumentException ("unknown service id");
      }

      while (queue.peekHead () == null && pollMessenger ())
         ;
   }

   /**
      Atomically polls the Messenger and queues a message (if any).
      Returns false if the Messenger had no messages to offer.
   */
   private synchronized boolean pollMessenger () throws IOException
   {
      Message message = msger.pollMessage ();

      if (message == null)
         return false; // Messenger exhausted.
 
      // the time on the stopwatch is how long it waited on the queue
      if(Debug.statsTime())
         stats.addMessageOrderQueueTime(message.stopwatch);

      if(Debug.statsTime())
         layer_stopwatch.start();

      // must duplicate the headers (via dupRaw()) since when we unmarshall the 
      // Rpc, we cut it's payload out, and we might need to save it to RpcLog.
      ProtRpcMessage prpc = unmarshallRpc(RawOps.dupRaw(message.getPayload()));
      RpcDemuxMessage demux_msg = new RpcDemuxMessageImpl(message, prpc);

      // Demultiplex on the RPC message type.
   
      switch (prpc.getKind ()) {
         case ProtRpcMessage.KIND_REQUEST:
            // enqueue a request
            request_queue.enqueue (new RpcDemuxEvent (demux_msg));
            node_stats.addRequestRPCReceived(1);
            break;
         case ProtRpcMessage.KIND_REPLY: // response
         case ProtRpcMessage.KIND_STATUS: // response
            // enqueue a response
            response_queue.enqueue (new RpcDemuxEvent (demux_msg));
            stats.addReplyRPCReceived(1);
            break;
         case ProtRpcMessage.KIND_END_RECOVERY:
            // enqueue an end recovery
            
            // if request queue is not empty, save the end recovery
            if(! request_queue.isEmpty())
               holding_recovery_queue.enqueue (new RpcDemuxEvent (demux_msg));
            else
               recovery_queue.enqueue (new RpcDemuxEvent (demux_msg));
            node_stats.addEndRecoveryRPCReceived(1);
            break;
         default:
            throw new ProtocolException ("unknown RPC message kind");
      }
      
      if(Debug.statsTime())
      {
         layer_stopwatch.stop();
         stats.addRPCLayerTime(layer_stopwatch);
      }

      return true;
   }

   /**
      Reconstructs an RPC protocol message from a packet conforming to the
      protocol. Also checks the version number.

      @exception ProtocolException
           if an error occurred while decoding the protocol.
   */
   private ProtRpcMessage unmarshallRpc (rawDef marsh_rpc)
               throws ProtocolException
   {
      if(Debug.statsTime())
         stopwatch.start();

      ProtRpcMessage prpc;

      prpc = prpc_fact.createProtRpcMessage ();
 
      if (prpc.unmarshall(marsh_rpc) != null)
         throw new ProtocolException ("RPC message had trailing bytes");

      if (prpc.getVersion () != VERSION)
         throw new ProtocolException ("wrong protocol version number");

      if(Debug.statsTime())
      {
         stopwatch.stop();
         stats.addRPCMarshallTime(stopwatch);
      }

      return prpc;
   }
}
