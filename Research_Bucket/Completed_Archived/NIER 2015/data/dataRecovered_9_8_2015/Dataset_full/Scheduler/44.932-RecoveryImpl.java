/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.node.recovery.proto;

import vu.globe.svcs.gls.active.*;
import vu.globe.svcs.gls.active.proto.ActiveQueue;
import vu.globe.svcs.gls.comm.*;
import vu.globe.svcs.gls.comm.rpcx.demux.RpcDemux;
import vu.globe.svcs.gls.comm.rpcx.demux.RpcDemuxMessage;
import vu.globe.svcs.gls.config.*;
import vu.globe.svcs.gls.config.pnodedef.PNodeSpecMap;
import vu.globe.svcs.gls.debug.Debug;
import vu.globe.svcs.gls.node.recovery.*;
import vu.globe.svcs.gls.protocol.*;
import vu.globe.svcs.gls.stats.NodeStatistics;
import vu.globe.svcs.gls.typedaddress.*;
import vu.globe.svcs.gls.typedaddress.locatorcomps.LocatorLib;
import vu.globe.svcs.gls.typedaddress.stdcomps.*;
import vu.globe.svcs.gls.types.*;
import vu.globe.util.time.Stopwatch;

import vu.globe.util.exc.NotSupportedException;
import vu.globe.util.debug.DebugOutput;

import vu.globe.util.comm.idl.rawData.rawDef;
import vu.globe.util.comm.RawOps;

import java.util.*;
import java.io.IOException;
import java.net.ProtocolException;

/**

   <code>Recovery</code> implements the LS crash recovery algorithms. It is 
   only used in nodes. When a node first starts up, it is considered to be in 
   its 'recovery mode'. 
   
   Recovery's initialization takes place in the Invoker. The node can only 
   leave recovery mode when a specific set of conditions have been met (see LS 
   Crash Recovery papers).

   Since <code>Recovery</code> may be accessed by more than one layer or part 
   of the node at the same time, it has been made thread-safe by serializing 
   access to all public methods.

   <code>Recovery</code> uses the <code>RpcExecutor</code> to resend outstand 
   RPC requests to a parent node. It also uses <code>Mapper</code> for node 
   address translation (physical and logical). Furthermore, 
   <code>Recovery</code> activates the <code>Messenger</code>'s crash queue in 
   order to learn about peer (parent) crashes.

   <code>Recovery</code> is activated by the Invoker for replaying of the 
   stable-storage rpc log.

   @author Dan Crawl
*/

public class RecoveryImpl implements Recovery
{
   // whether we're in recovery
   private boolean in_recovery = true;

   // used to send 'end recovery' messages
   private Messenger msger;

   // used to receive 'end recovery' messages
   private RpcDemux dmx;

   // use mapper for notification of peer crashes
   private Mapper mapper;

   // use of out rpc to resend pending requests
   private OutRpcOriented outrpc;

   // registration id returned by rpc demux
   private ActiveRID demux_reg;  // recovery service

   // registration id returned by mapper
   private ActiveRID crash_reg;  // crash service
   
   // parent node peer
   private Peer parent;

   // list of peers we're still dependent on to recover/resend
   private HashSet dependent_peers;
  
   // the rpcs our dependent peers have resent us.
   private HashSet redo_rpcs;

   // stable storage to write rpcs to
   private RpcLog log = null;

   // whether we actually use the stable storage log
   private boolean use_log = false;

   // a queue to send replay rpcs to InRpc
   private ActiveQueue inrpc_queue;

   // use of PNodeSpecMap to convert logical nodes into physical ones
   private PNodeSpecMap pnodespecs;

   // factory to send 'end recovery' RPCs
   private ProtRpcMessageFact prpc_fact;

   // Epoch of this RPC layer.
   private long my_epoch;

   // 16 bit RPC version number
   private static final int VERSION = 3;

   // our environment
   private ObjEnv env;

   // Use of Statistics
   private NodeStatistics stats;

   // Use of Stopwatch for statistics
   private Stopwatch stopwatch;

   // Constructor

   public RecoveryImpl(ObjEnv env)
   {
      this.env = env;

      dependent_peers = new HashSet();
      redo_rpcs = new HashSet();

      Scheduler sched = env.getScheduler();
      inrpc_queue = new ActiveQueue(sched);
   
      // get the environment
      pnodespecs = env.getPNodeSpecMap();
      prpc_fact = env.getProtRpcX();
      my_epoch = env.getEpoch();
      stats = (NodeStatistics) env.getStatistics();
      
      if(Debug.statsTime())
         stopwatch = new Stopwatch ();

      stats.startNodeRecovery();
   }

   // Active interface

   public synchronized ActiveRID activate (int service, Notifiable note,
                                             ActiveUID userInfo)
   {
      throw new NotSupportedException("activate() with service id");
   }

   public synchronized ActiveRID activate (Notifiable note, ActiveUID userInfo)
   {
      // activate InRpc's queue so we can replay requests from RpcLog.
      inrpc_queue.activate(note, userInfo); 
             
      // activate the messenger's crash queue so we can learn about peer 
      // crashes
      Notifiable messengerCrashNote = new Notifiable() {
         public void notifyEvent(ActiveUID ui)
         {
            notifyMessengerCrashEvents(ui);
         }
      };
      crash_reg = msger.activate(MessengerService.MSGER_CRASH, 
         messengerCrashNote, null);

      // activate rpc demux recovery queue so we can learn about received
      // 'end recovery' rpcs
      Notifiable rpcdmxCrashNote = new Notifiable() {
         public void notifyEvent(ActiveUID ui)
         {
            notifyRpcRecoveryEvents(ui);
         }
      };
      demux_reg = dmx.activate(RpcService.SERVICE_RECOVERY, 
         rpcdmxCrashNote, null);

      // No registration id required.
      return null;
   }

   public synchronized void deactivate (ActiveRID regID)
   {
      inrpc_queue.deactivate(null);
      msger.deactivate(crash_reg);
      dmx.deactivate(demux_reg);
   }

   // Recovery interface

   // see if we're still in recovery mode.
   public synchronized boolean inRecovery()
   {
      Debug.println("IN RECOVERY : " + in_recovery);

      return in_recovery;
   }

   // set the communication interfaces
   public synchronized void setComm(Messenger msger, Mapper mapper, 
      RpcDemux dmx, OutRpcOriented outrpc)
   {
      this.msger = msger;
      this.mapper = mapper;
      this.dmx = dmx;
      this.outrpc = outrpc;
   }

   public synchronized void setParentPeer(Peer parent)
   {
      this.parent = parent;

      Debug.println("RECOVERY addParentPeer: " + parent);
   }
   
   // add a node peer that we're dependent upon to resend us their outstanding 
   // RPC requests.
   public synchronized void addDependentPeer(Peer dep)
   {
      Debug.println("RECOVERY addDependentPeer (logical): " + dep);

      // convert this logical node into its physical ones
      TypedAddress addr = dep.getAddress();
      NodeID node_id = LocatorLib.getNodeID((StackedAddress)addr);
      Enumeration pnodes;
      try 
      {
         pnodes = getPhysicalNodes(node_id);
      }
      catch (IOException e)
      {
         System.err.println("error getting pnodes: " + e.getMessage());
         return;
      }

      while(pnodes.hasMoreElements())
      {
         PNodeID pnode_id = (PNodeID) pnodes.nextElement();
         Peer pnode_peer = mapper.createPeer(new StackedAddressImpl(pnode_id));
         dependent_peers.add(pnode_peer); 
      
         Debug.println("RECOVERY addDependentPeer (physical): " + pnode_peer);
      }
   }

   // add a resent RPC (request) from a dependent node
   public synchronized void addRedoRpc(RpcHandle handle, Peer sender)
   {
      // see if it's a replayed RPC
      if(handle.getRpcLogID() != null)
      {
         Debug.println("YES: was replayed");
         redo_rpcs.add(handle);
      }
      else
      {
         // see if we're still dependent on that child
         if(dependent_peers.contains(sender))
         {
            Debug.println("YES: adding resent from " + sender);
            redo_rpcs.add(handle);
         }
         else
            Debug.println("NO: not waiting on " + sender);
      }

      return;
   }

   // remove a resent RPC (request) from a dependent node since we just sent 
   // the response.
   public synchronized void removeRedoRpc(RpcHandle handle)
   {
      Debug.println("RECOVERY removeRedoRpc: " + handle);

      // remove the RPC; ignore it if it wasn't resent.
      redo_rpcs.remove(handle);

      // see if the resent rpc we just removed brought us out of recovery.
      checkRecovery();

      return;
   }

   public synchronized RpcLogID saveRpcToLog(Message message)
   {
      if(! use_log)
         return null;
      else 
         return log.addNewRpc(message);
   }

   // remove a particular rpc from the log.
   public synchronized void removeRpcFromLog(RpcLogID id)
   {
      if(use_log)
         log.removeRpc(id);
   }

   // replay the entire rpc log.
   public synchronized void replayRpcLog()
   {
      use_log = true;
      log = new RpcLog(env, mapper);
      Debug.println("RECOVERY starting playback");

      Enumeration keys = log.getReplayLogIDs();

      // if there are none to be replayed, send 'end recovery' now, otherwise 
      // we'll send it when we've sent the responses to the replayed requests.
      if(! keys.hasMoreElements())
      {
         // this assumes we don't have any dependent peers and don't worry 
         // about resent requests.
         in_recovery = false;

         System.out.println ("Node leaving crash recovery mode.");

         try
         {
            sendEndRecovery(parent);
         }
         catch (IOException e)
         {
            System.err.println("unable to send end recovery " + e.getMessage());
         }

         stats.stopNodeRecovery();

         Debug.println("RECOVERY HAD NO playback");
         return;
      }

      while(keys.hasMoreElements())
      {
         RpcLogID id = (RpcLogID) keys.nextElement();
         Message message = log.getReplayRpc(id);

         Debug.println("RECOVERY Replaying RpcLogID #" + id.toString() + " from " + message.getSender());

         // set the log id and payload
         RecoveryMessage recovery_message = new RecoveryMessageImpl(message, id); 
         // put the message on the InRpc queue.
         inrpc_queue.enqueue(recovery_message);

         stats.addReplayRpcLogRPC(1);
      }
      
      Debug.println("RECOVERY finished playback");
   }

   public void sendCrashNotification(Peer dest) throws IOException
   {
      TypedAddress addr = dest.getAddress();

      // see if it's a logical or physical peer
      if(LocatorLib.isPLocatorAddress((StackedAddress)addr))
      {
         Debug.println("sending crash notification to: " + dest);

         msger.sendCrashNotification(dest); 
      }
      else if(LocatorLib.isLocatorAddress((StackedAddress)addr))
      {
         NodeID node = LocatorLib.getNodeID((StackedAddress)addr);

         Enumeration pnodes = getPhysicalNodes(node);
         try
         {
            pnodes = getPhysicalNodes(node);
         }
         catch (IOException e)
         {
            System.err.println("error getting pnodes: " + e.getMessage());
            return;
         }
 
         while(pnodes.hasMoreElements())
         {
            PNodeID pnode_id = (PNodeID) pnodes.nextElement();
            Peer pnode = msger.createPeer(new StackedAddressImpl(pnode_id));
   
            Debug.println("sending crash notification to: " + pnode);
   
            msger.sendCrashNotification(pnode);
         }
      }
      else
         throw new IllegalArgumentException("unkown address type " + addr);
   }

   public synchronized RecoveryMessage pollRecovery() throws IOException
   {
      if(inrpc_queue.size() == 0)
         return null;

      return (RecoveryMessage) inrpc_queue.dequeue();
   }

   // notification from RpcDemux about received 'end recover' RPCs.
   private void notifyRpcRecoveryEvents(ActiveUID userInfo)
   {
      try { pollRpcDemuxRecovery(); }
      catch (IOException exc) {
         System.err.println("Recovery ignoring exception:");
         exc.printStackTrace();
      }
   }

   // polls the RpcDemux recovery queue.
   private synchronized boolean pollRpcDemuxRecovery() throws IOException
   {
      RpcDemuxMessage demux_msg = dmx.pollRecovery();
      ProtRpcMessage prpc = demux_msg.getRpcMessage();
      
      // Check the correctness of the incoming RPC request.
      if (prpc.getVersion () != VERSION)
         throw new ProtocolException ("RPC request with unknown version");
 
      int qos = prpc.getQoS ();
      if (qos < 0 || qos > RpcHandle.MAX_QOS)
         throw new ProtocolException ("RPC request's QoS");

      // make sure it's an END_RECOVERY message.
      if (prpc.getKind() == ProtRpcMessage.KIND_END_RECOVERY)
      {
         Message msg = demux_msg.getMessage();
         removeDependentPeer(msg.getSender());
 
         // see if we're suppose to acknowledge this
         if(msg.userMustAcknowledge())
            msger.acknowledge(msg);
 
         return true;
      }
      else
         throw new ProtocolException ("RPC request's type");
   }

   // a node just told us it resent all its outstanding requests and/or it 
   // recovered.
   private void removeDependentPeer(Peer dep)
   {
      Debug.println("RECOVERY removeDependentPeer: " + dep);

      // remove the node; ignore it if we're not dependent on it.
      dependent_peers.remove(dep);
  
      // see if the recovered peer just removed brought us out of recovery.
      checkRecovery();

      return;
   }

   // notification from Messenger about peer crashes.
   private void notifyMessengerCrashEvents(ActiveUID userInfo)
   {
      try { pollMessengerCrash(); }
      catch (IOException exc) {
         System.err.println("Recovery ignoring exception:");
         exc.printStackTrace();
      }
   }

   // polls the Messenger crash queue.
   // returns false if the Messenger had no crash messages to offer.
   private synchronized boolean pollMessengerCrash() throws IOException
   {
      Message message = msger.pollCrash();
 
      if(message == null)
         return false;

      Peer sender = message.getSender();

      Debug.println("RECOVERY peer has crashed: " + sender);

      // see if the crashed peer is our logical parent
      Peer logical_sender = mapper.physicalToLogical(sender);
      if(logical_sender.equals(parent))
      {
         outrpc.resendPendingRequests(sender);

         // if we're not in recovery ourselves, send 'end recovery' message
         if(! in_recovery)
            sendEndRecovery(sender);
      }

      return true;
   }

   public synchronized void closeRpcLog()
   {
      if(use_log)
         log.close();
   }

   // see if we still need to be in recovery.
   private void checkRecovery()
   {
      boolean were_in_recovery = in_recovery;

      in_recovery = ! (redo_rpcs.isEmpty() && dependent_peers.isEmpty());

      //XXXDAN DEBUG
      Iterator i = redo_rpcs.iterator();
      Object o;
      while(i.hasNext())
      {
         o = i.next();
         Debug.println("CHECK RECOVERY PENDING RPCS");
      }
 
      i = dependent_peers.iterator();
      while(i.hasNext())
         Debug.println("CHECK RECOVERY PENDING DEPENDENTS: " + i.next());
 
      Debug.println("CHECK RECOVERY = " + in_recovery);
      //XXXDAN DEBUG
      
      if(were_in_recovery && ! in_recovery)
      {
         System.out.println ("Node leaving crash recovery mode.");

         try  
         {
            sendEndRecovery(parent);
         }
         catch (IOException e)
         {
            System.err.println("unable to send end recovery " + e.getMessage());
         }

         stats.stopNodeRecovery();
      }
 
   }

   // send the 'end recovery' RPC to a logical or physical peer
   private void sendEndRecovery(Peer dest) throws IOException
   {
      // see if we have no parent to send it to
      if(parent == null)
         return;

      Debug.println("RPC sending END_RECOVERY to " + dest);
 
      ProtRpcMessage end_rcv_prpc = makeProtRpc (VERSION, my_epoch, -1, 
         RpcHandle.QOS_ORDERED, ProtRpcMessage.KIND_END_RECOVERY, null, 
         new rawDef());
 
      rawDef packet = end_rcv_prpc.marshall ();
 
      Message message = new MessageImpl ();
      message.setConnectionNumber (Message.CONN_ORDERED_MIN);
      message.setPayload (packet);

      // see if destination is a logical or physical node
      TypedAddress addr = dest.getAddress();

      // see if it's a physical or logical node address
      if(LocatorLib.isPLocatorAddress((StackedAddress) addr))
      {
         message.setReceiver (dest);
         msger.send(message, null);
         stats.addEndRecoveryRPCSent(1);
      }
      else
      {
         NodeID node = LocatorLib.getNodeID((StackedAddress) addr);
         Enumeration pnodes =  getPhysicalNodes(node);
         while(pnodes.hasMoreElements())
         {
            Peer pnode = msger.createPeer(new StackedAddressImpl((PNodeID)pnodes.nextElement()));
            message.setReceiver(pnode);
            msger.send(message, null);
            stats.addEndRecoveryRPCSent(1);
         }
      }
     
      return;
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
      ProtRpcMessage prpc;
 
      prpc = prpc_fact.createProtRpcMessage ();
      prpc.setVersion (version);
      prpc.setRequestorEpoch (epoch);
      prpc.setRpcID (id);
      prpc.setQoS (qos);
      prpc.setKind (kind);
      prpc.setObjectHandle (obj);
      prpc.setPayload (payload);
 
      return prpc;
   }

   /**
      Convenience routine to return an enumeration of physical node ids that 
      make up a logical one.
   */
   private Enumeration getPhysicalNodes(NodeID node) throws IOException
   {
      Vector pnodes = pnodespecs.getPNodes(node);
      return pnodes.elements();
   }
}
