/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.comm.messenger.proto;

import vu.globe.svcs.gls.comm.*;
import vu.globe.svcs.gls.protocol.*;
import vu.globe.svcs.gls.config.*;
import vu.globe.svcs.gls.config.nodeaddr.NodeAddrMapClient;
import vu.globe.svcs.gls.comm.*;
import vu.globe.svcs.gls.stats.Statistics;
import vu.globe.svcs.gls.protocol.*;
import vu.globe.svcs.gls.types.PNodeID;
import vu.globe.svcs.gls.active.*;
import vu.globe.svcs.gls.active.proto.*;
import vu.globe.svcs.gls.active.ActiveTimerFact.ActiveTimer;
import vu.globe.util.exc.NotSupportedException;
import vu.globe.svcs.gls.typedaddress.*;
import vu.globe.svcs.gls.typedaddress.locatorcomps.LocatorLib;
import vu.globe.svcs.gls.alert.AlertID;
import vu.globe.svcs.gls.debug.Debug;
import vu.globe.util.time.Stopwatch;

import vu.globe.util.comm.idl.rawData.rawDef;
import vu.globe.util.comm.RawOps;

import java.util.Enumeration;
import java.io.IOException;
import java.net.ProtocolException;

/**
   A message communication object. No batching of multiple messages occurs nor
   are batched multiple messages accepted. All user connections to a peer,
   whether reliable or sequenced, are mapped to a single reliable, ordered
   internal connection to the peer. Other messengers are assumed to do the
   same. 
   <p>
   This layer recognises typed address with the following protocol stack: a
   'PLocator' protocol, optionally layered on top of the datagram layer
   protocol stack (typically UDP/IP or TCP/IP). For example: 'PLocator/TCP/IP',
   'PLocator/UDP/IP', or just 'PLocator'. The protocol-specific address of the
   PLocator part is an optional PNodeID (as defined by the typed address
   (sub)packages). As defined by <code>PNodeID</code> it contains a node number.
   <p>
   The typed address of a node may leave out the datagram layer address
   components entirely, thus consisting only of the 'PLocator' component. If
   they are nevertheless present, they may be ignored by the Messenger. Examples
   of a node's typed address: [<globe>:23] and
   [ip:name][tcp:30000][<globe>:23]
   <p>
   The typed address of a resolver must have the full protocol stack but must
   omit the node id within the 'PLocator' component. Example of a
   resolver's typed address: [ip:name][udp:30000][<globe>:<-1>]
   <p>
   A <code>locator.config.nodeaddr.NodeAddrMapClient</code> in the object
   environment
   is used to install and look-up node ids. Node ids must be looked up if the
   datagram layer protocol components are omitted from a node's typed address.
   Other messengers are assumed to behave in the same way.
   <p>
   The statistics object in the object environment is updated.
 
   @author Patrick Verkaik
*/

/*
   -  General notes for a Messenger implementation are in the separate LS 
      design documentation.

   -  The communication peers are represented by MessengerPeer objects.
      Each messenger peer must correspond uniquely with a Datagram layer peer.
      Messenger peers come in two flavours: 'node peers' and 'resolver peers'.
      Both kinds are created as communication is first received from peers or
      as the user first defines them. The former have an (obligatory) node id
      field. The latter do not.

   -  To properly identify the remote peer from (a) incoming communication (b)
      specification by the user, two tables are used:
      -  a mapping of datagram peers to resolver peers
      -  a mapping of node ids to node peers

      The reason that, unlike resolver peers, node peers are not mapped from
      datagram peers is that the datagram peer of a node is not always initially
      known (see below).

   -  The nodeaddr package is a mapping of node ids to network addresses. This
      package is actually a 'proxy' for a separate process which maintains a
      centralised mapping: the configuration server. Nodes register their
      network address with the configuration server, and look up the addresses
      of other nodes.
      
      The network address of a node is stable, in order that a messenger need 
      not contact a config server more than once for the same node id.
      When a node looks up another node's network address, it will store the
      address inside the MessengerPeer object for that node.

      Stable network addresses are implemented as follows. The first time a
      node is run it will register an entry with the config server. If a node
      is restarted, it will look up its previous network address in the config
      server and reuse that.

      Notes:
      o A node's network address is carried by incoming messages from that
        node. Therefore, when an incoming message arrives, we can learn a
	node's network address without going to the configuration server (at
	some later point).
      o The nodeaddrmap client's cache is not used. Instead, network addresses
        of nodes are stored inside MessengerPeer objects.
      o While accessing the config server it is possible that the messenger
        will delay a callback thread.

   -  To send a message to a peer, send() needs to know its network address (aka
      'datagramx peer'). This information is kept inside a MessengerPeer. If
      send() discovers that the address of the peer is still unknown, it
      asks the configuration server for it (and additionally stores the address
      returned). However, if the configuration server doesn't have the address
      either, the message cannot be sent, and is queued instead. A timer is
      used to periodically test whether the address has become known yet. Once
      it has, outstanding messages to that node can be transmitted.

   -  I/O Exceptions thrown by the datagram layer or incurred while processing
      datagrams are not passed to the user. Instead they may cause datagrams to
      be ignored.
   
   -  Thread-safety is ensured simply by protecting all entry points into a
      Messenger object. This includes some private notification methods.
      Note that the user is notified using an ActiveQueue. ActiveQueue
      specifies that it consists of downcalling methods only.
      Therefore it is safe to keep our notification methods locked while events
      are queued. (See the LS documentation about this.)

   -  The statistics object is updated as follows. A message is considered sent
      once it has been acknowledged. Duplicate acks are ignored here. Similarly
      duplicate incoming messages only count towards one received message in
      the statistics.

   -  To support the LS crash recovery algorithms, the user can (optionally) be 
      notified of peer crashes. We know peers have crashed and restarted when 
      we look at their epochs in messages sent to us. When we learn of a peer 
      crashing (and restarting), and if the user wants to be notified of peer 
      crashes, we give the user a new Message with the Sender field set to the 
      crashed peer.  The rest of the Message is left uninitialized.

   -  Some messages contain important data that must be handle by the user 
      before they can be acknowledged. To support this, the Messenger installs 
      a cookie, which contains information required to send the 
      acknowledgement, in the Message it passes to the user. Later, when the 
      user wishes the Message to be acknowledged, it passes the original 
      Message back to the Messenger and the cookie is retrieved.

   -  The user can also (optionally) be notified of acknowledgements of 
      Messages sent. This is done by activating the acknowledgement queue and 
      setting the user acknowledgement cookie in the Message the user gives the 
      Messenger to send. This cookie is used to identify (to the user) which 
      Message was successfully acknowledged.

*/

public class MessengerImpl implements EnvObject, Messenger, MessengerService
{
   private ProtMessageFact pmsg_fact;

   // timer for statistics
   protected Statistics stats;
   private Stopwatch epoch_stopwatch;
   private Stopwatch ack_stopwatch;
   private Stopwatch marshall_stopwatch;
   private Stopwatch layer_stopwatch;

   // Use of the DatagramX layer.
   private DatagramX dgramx;

   // the mapping of node ids to datgram layer addresses
   private NodeAddrMapClient nodeaddrmap;

   // delivery queue with messages (of type Message) for the user.
   private ActiveQueue user_queue;

   // crash queue with peers that have crashed for the user.
   private ActiveQueue crash_queue;

   // whether to notify users of crashed peers
   private boolean notify_crashes = false;

   // ack queue to notify users about received acknowledgements
   private ActiveQueue ack_queue;

   // info to be able to deactivate timers later
   private ActiveTimerFact timer_fact;  // timer factory

   // Tables that map to unique messenger peers.
   private ResolverPeerMap resolvers = new ResolverPeerMap ();
   private NodePeerMap nodes = new NodePeerMap ();

   // Peer bound to.
   private MessengerPeer local_peer;

   // Epoch of this Messenger.
   private long my_epoch;

   // 16 bit version number
   private static final int VERSION = 3;

   // The single internal connection that Messengers communicate through
   private static final int MY_INTERNAL_CONN = Message.CONN_ORDERED_MIN;

   // the maximum number of retransmissions per retrans timer expiry to a peer
   private static final int MAX_RETRANSMIT = 50;

   public void initObject (ObjEnv env)
   {
      Scheduler sched = env.getScheduler ();

      // set environment.
      dgramx = env.getDatagramX();
      stats = env.getStatistics ();
      pmsg_fact = env.getProtMessenger ();
      nodeaddrmap = env.getNodeAddrMap ();
      my_epoch = env.getEpoch();

      // user_queue = new DebugActiveQueue (sched, "msger delivery", 100);
      user_queue = new ActiveQueue (sched);
      crash_queue = new ActiveQueue(sched);
      ack_queue = new ActiveQueue(sched);
      timer_fact = new ActiveTimerFactImpl (sched, stats);

      if(Debug.statsTime())
      {
         epoch_stopwatch = new Stopwatch();
         ack_stopwatch = new Stopwatch(); 
         marshall_stopwatch = new Stopwatch();
         layer_stopwatch = new Stopwatch();
      }
   }

   // PeerManager interface
   public synchronized Peer createPeer (TypedAddress addr)
   {
      /*
        A messenger peer may
        subsequently be used to bind to (in which case it must represent a local
        address) or to send messages to. Depending on the intended use, an entry
        may have to be created in the configuration server (first case) or read
        from the configuration server (second case). Since createPeer() has no
        knowledge as to the intended use, these actions are delayed until later.
        In the meantime the datagram address field of a node peer is left open.

        We could see if the optional datagram layers are present in a node's
        typed address and create a datagram layer peer from that, but we
        don't.
      */

      StackedAddress sa = (StackedAddress) addr;

      MessengerPeer mesgr_peer;
      PNodeID node_id = LocatorLib.getPNodeID (sa);
      if (node_id != null)
         mesgr_peer = nodes.getOrCreate (node_id);
      else {
         // copy the address , but without the locator component
         StackedAddress dgram_sa = (StackedAddress) sa.clone ();
         dgram_sa.remove ();

         Peer dgram_peer = dgramx.createPeer (dgram_sa);
         
         mesgr_peer = resolvers.getOrCreate (dgram_peer);
      }

      return mesgr_peer;
   }

   // EndPoint interface
   public synchronized void bindAddress (Peer local) throws IOException
   {
      MessengerPeer msger_peer = (MessengerPeer) local;
      Peer dgram_to_bind = msger_peer.getDatagramPeer ();
      PNodeID node_id = msger_peer.getNodeID ();

      if (node_id == null) {
         // bind as a resolver

         // bind the datagram layer => dgram_bound
         if (dgram_to_bind != null)
            dgramx.bindAddress (dgram_to_bind);
         else
            dgramx.bindAddress ();

         Peer dgram_bound = dgramx.getBoundAddress ();
         local_peer = resolvers.getOrCreate (dgram_bound);
         return;
      }

      // Bind as a node. Check with the nodeaddrmap: if a dgramx address was
      // prevly defined, then that must be used. Otherwise, the dgramx address
      // is now being defined  is used, and nodeaddrmap is updated.

      // note: this is where a callback thread can be delayed in an RMI
      TypedAddress prev_dgram = nodeaddrmap.getAddress (node_id);
      if (prev_dgram != null)
         if (dgram_to_bind == null)
            dgram_to_bind = dgramx.createPeer (prev_dgram);
         else
            throw new IllegalArgumentException ("node id cannot be remapped " +
                                       "to new network address");

      // bind the datagram layer => dgram_bound
      if (dgram_to_bind != null)
         dgramx.bindAddress (dgram_to_bind);
      else
         dgramx.bindAddress ();
      Peer dgram_bound = dgramx.getBoundAddress ();

      local_peer = nodes.getOrCreate (node_id, dgram_bound);
      if (prev_dgram == null) {
         // note: this is where a callback thread can be delayed in an RMI
         nodeaddrmap.setAddress (node_id, dgram_bound.getAddress ());
      }
   }

   /**
      Binds as a resolver.
   */
   public synchronized void bindAddress () throws IOException
   {
      dgramx.bindAddress ();
      Peer dgram_peer = dgramx.getBoundAddress ();
      local_peer = resolvers.getOrCreate (dgram_peer);
   }

   public synchronized Peer getBoundAddress ()
   {
      return local_peer;
   }

   // Active interface
   public synchronized ActiveRID activate (Notifiable note, ActiveUID userInfo)
   {
      throw new NotSupportedException ("no service identifier");
   }

   public synchronized ActiveRID activate (int service, Notifiable note,
                                             ActiveUID userInfo)
   {
      switch(service)
      {
         case MSGER_CRASH:
            notify_crashes = true;
            crash_queue.activate(note, userInfo);
            break;
         case MSGER_ACK:
            ack_queue.activate(note, userInfo);
            break;
         case MSGER_USER: 
            // delivery queue
            user_queue.activate (note, userInfo);

            // datagram layer
            Notifiable dgram_note = new Notifiable () {
               public void notifyEvent (ActiveUID ui) {
                  notifyDgramEvent (ui);
               }
            };
            dgramx.activate (dgram_note, null);

            // for retransmission timers
            Notifiable retrans_note = new Notifiable () {
               public void notifyEvent (ActiveUID ui) {
                  notifyRetransTimerEvent (ui);
               }
            };
            ActiveRID retrans_timer_reg = timer_fact.activate (retrans_note, null);

            // for epoch timers
            Notifiable epoch_note = new Notifiable () {
               public void notifyEvent (ActiveUID ui) {
                  notifyEpochTimerEvent (ui);
               }
            };
            ActiveRID epoch_stopwatch_reg = timer_fact.activate (epoch_note, null);
           
            resolvers.activateTimers(timer_fact, retrans_timer_reg, epoch_stopwatch_reg);
            nodes.activateTimers(timer_fact, retrans_timer_reg, epoch_stopwatch_reg);

            break;
         default: 
            throw new IllegalArgumentException ("unknown service id");
 
      }

      return new MessengerRID(service);
   }

   public synchronized void deactivate (ActiveRID regID)
   {
      MessengerRID msger_rid = (MessengerRID) regID;
      int service = msger_rid.getService();

      switch(service)
      {
         case MSGER_CRASH:
            if(notify_crashes == false)
               throw new IllegalArgumentException("crash service not activated");
            crash_queue.deactivate(null);
            crash_queue = null;
            break;
         case MSGER_ACK:
            ack_queue.deactivate(null);
            ack_queue = null;
            break;
         case MSGER_USER:
            resolvers.deactivateTimers();
            nodes.deactivateTimers();
            dgramx.deactivate (null);
            if(Debug.statsTime())
            {
               stats.setMessengerCallbackQueueTotalTime
                  (user_queue.timeQueueWaitTime());
               stats.setMessengerCallbackQueueAvgTime
                  (user_queue.avgQueueWaitTime());
            }
            user_queue.deactivate (null);
            break;
         default:
            throw new IllegalArgumentException ("unknown registration id");
      }
   }

   // MessageOriented interface
   public synchronized void send (Message message, AlertID id)
                                                            throws IOException
   {
      if(Debug.statsTime())
         layer_stopwatch.start();

      // make a copy of the message in case the user wants to reuse it.
      Message msg_copy = new MessageImpl();
      msg_copy.setSender(message.getSender());
      msg_copy.setReceiver(message.getReceiver());
      msg_copy.setConnectionNumber(message.getConnectionNumber());
      msg_copy.setPayload(message.getPayload());
      msg_copy.setAckCookie(message.getAckCookie());

      MessengerPeer rcv_peer = (MessengerPeer) msg_copy.getReceiver ();

      // To send a message, the network address and the epoch must be known.

      if (rcv_peer.getEpoch() != ProtMessage.UNKNOWN_EPOCH) {

         // remote epoch is known, therefore network address must also be known
         sendDataMessage (rcv_peer, msg_copy);
      }
      else {

         // Epoch still unknown. Queue the data message, and send an
         // Epoch message. Set a timer, so that Epoch messages are repeatedly
         // sent while the Epoch remains unknown. Once the epoch is known, the
         // data message will be sent.
         if(rcv_peer.queueForEpoch (msg_copy))
         {
            System.out.println ("    location service messenger: epoch unknown " + rcv_peer);
            rcv_peer.checkEpochTimer ();
	    if (mapNodeID (rcv_peer)) {
              sendEpoch (rcv_peer);
	    }
            else {
	      // Gosh! The network address isn't known either... The epoch
	      // timer will periodically check whether the address is known.
	      System.out.println ("    location service messenger: address unknown " + rcv_peer);
	    }
         }
	 // else: an epoch timer is already running
      }

      if(Debug.statsTime())
      {
         layer_stopwatch.stop();
         stats.addMessengerLayerTime(layer_stopwatch);
      }
   }

   public synchronized void sendCrashNotification (Peer recv) throws IOException
   {
      if(Debug.statsTime())
         layer_stopwatch.start();

      // Note: this crash notification can be sent unreliably.

      if (mapNodeID ((MessengerPeer) recv)) {
        sendEpoch((MessengerPeer)recv); 
      }
      else {
        // Network address unknown. Let's not waste any more effort on an
        // unreliable transmission.
        System.out.println ("    location service messenger: address unknown " + recv);
      }
      
      if(Debug.statsTime())
      {
         layer_stopwatch.stop();
         stats.addMessengerLayerTime(layer_stopwatch);
      }
   }

   /**
      Transmits a data message. The data message is given the peer's epoch 
      number, and is placed in the queue of unacked messages. The queue's
      retransmission timer is set.
      <p>
      As a pre-condition, the peer's network address must be known.
      
      @param dest
         The peer to send to
      @param pmsg
         The message to send. The peer epoch field will be overwritten.
   */
   private void sendDataMessage (MessengerPeer dest, Message message)
   					throws IOException
   {
      OutConnection out_conn = dest.getOutConnection ();
      Peer rcv_dgram_peer = dest.getDatagramPeer ();

      ProtMessage pmsg = makeProtDataMessage (message);
      long seq = out_conn.nextSequence ();
      pmsg.setSequence (seq);

      long peer_epoch = dest.getEpoch();
      pmsg.setReceiverEpoch (peer_epoch);

      // make Datagram. The 'sender' field can be left empty.
      Datagram dgram = new DatagramImpl ();

      dgram.setReceiver (rcv_dgram_peer);

      if(Debug.statsTime())
         marshall_stopwatch.start();

      dgram.setPacket (pmsg.marshall ());

      if(Debug.statsTime())
      {
         marshall_stopwatch.stop();
         stats.addMessageMarshallTime(marshall_stopwatch);
      }

      // Register the outgoing message as unacked, and possibly set a timer.
      out_conn.register (pmsg.getSequence(), dgram, message);
      out_conn.checkRetransTimer ();

      debugSendData (pmsg, dest);

      if(Debug.statsTime())
         layer_stopwatch.stop();

      sendDatagram (dgram);
      
      if(Debug.statsTime())
         layer_stopwatch.resume();

      stats.addDataMessageSent(1);
   }

   /**
      Sends a datagram (whether data, epoch, ack, etc.) through the datagram
      layer. Any I/O exceptions are ignored.
   */
   private void sendDatagram (Datagram dgram)
   {
      try {
        dgramx.send (dgram, null);
      }
      catch (IOException exc) {
         System.err.println ("location service messenger ignoring dgram layer exception:");
         exc.printStackTrace ();
      }
   }
   
   /**
      Creates the protocol container to send a given message with. All fields
      are filled in except for the sequence number, and the peer's epoch.
   */
   private ProtMessage makeProtDataMessage (Message message)
   {
      ProtMessage pmsg = pmsg_fact.createProtMessage ();
      pmsg.setVersion (VERSION);
      pmsg.setKind (ProtMessage.MSGKIND_DATA);

      pmsg.setSenderEpoch (my_epoch);

      if (local_peer instanceof NodePeer)
         pmsg.setSenderNode (((NodePeer) local_peer).getNodeID ());
      else
         pmsg.setSenderNode (null);

      MessengerPeer rcv_peer = (MessengerPeer) message.getReceiver ();
      if (rcv_peer instanceof NodePeer)
         pmsg.setReceiverNode (((NodePeer) rcv_peer).getNodeID ());
      else
         pmsg.setReceiverNode (null);

      pmsg.setUserConnection (message.getConnectionNumber ());
      pmsg.setInternalConnection (MY_INTERNAL_CONN);
      pmsg.setPayload (message.getPayload ());

      return pmsg;
   }

   /**
      Performs the delayed node id mapping for node peers.
      Maps a node peer's node id to a datagram peer by looking it up in the
      nodeaddrmap. The result of this mapping is assigned to the node peer's
      datagram peer field. If nodeaddrmap does not have an entry for the node
      id, the datagram peer field is left open.
      <p>
      This is a no-op if the datagram peer field was already filled in. Note
      that this is always the case if a resolver peer is passed.

      @return true 	iff on return the datagram peer field is defined
   */
   private boolean mapNodeID (MessengerPeer peer) throws IOException
   {
      Peer dgram_peer = peer.getDatagramPeer();
      if (dgram_peer != null) {
         // a resolver peer, or a node peer which has been mapped already
         return true;
      }

      // dgram_peer == null => 'peer' must be a node peer
      NodePeer node_peer = (NodePeer) peer;
      PNodeID node = node_peer.getNodeID ();

      // note: this is where a callback thread can be delayed in an RMI
      TypedAddress dgram_ta = nodeaddrmap.getAddress (node);
      if (dgram_ta == null)
         return false;

      dgram_peer = dgramx.createPeer (dgram_ta);
      node_peer.setDatagramPeer (dgram_peer);
      return true;
   }

   public synchronized Message pollMessage () throws IOException
   {
      if (user_queue.size () == 0)
         pollDatagramX (); // user is polling without having been notified

      return (Message) user_queue.dequeue (); // note: may still be null
   }

   public synchronized Message pollCrash() throws IOException
   {
	  if(crash_queue.size() == 0)
	  	return null; // user is polling without having been notified
	
	  return (Message) crash_queue.dequeue(); // note: may still be null
   }

   public synchronized Message pollAck() throws IOException
   {
      if(ack_queue.size() == 0)
         return null; // user is polling without having been notified

      return (Message) ack_queue.dequeue(); // note: may still be null
   }

   /**
      Notification from the datagram layer.
   */
   private synchronized void notifyDgramEvent (ActiveUID user_info)
   {
      // the user info is ignored
      try { pollDatagramX (); }
      catch (IOException exc) {
         System.err.println ("location service messenger ignoring dgram layer exception:");
         exc.printStackTrace ();
      }
   }

   /**
      Polls the datagram layer, and processes a datagram, if any.
   */
   private void pollDatagramX () throws IOException
   {
      Datagram dgram = dgramx.pollDatagram ();

      if (dgram == null)
         return;

      if(Debug.statsTime())
         layer_stopwatch.start();

      // unmarshall a message

      if(Debug.statsTime())
         marshall_stopwatch.start();

      rawDef marsh_msg = dgram.getPacket ();
      ProtMessage pmsg = pmsg_fact.createProtMessage ();

      rawDef packet = pmsg.unmarshall (marsh_msg);

      if(Debug.statsTime())
      {
         marshall_stopwatch.stop();
         stats.addMessageMarshallTime(marshall_stopwatch);
      }

      // some integrity checks
      if (packet != null)
         throw new ProtocolException ("Datagram contained several messages");

      if (pmsg.getVersion () != VERSION)
         throw new ProtocolException ("wrong protocol version number");

      // determine the sender
      PNodeID sender_node = pmsg.getSenderNode ();

      MessengerPeer sender_peer;
      if (sender_node == null) {
         // a resolver
         sender_peer = resolvers.getOrCreate (dgram.getSender ());
      }
      else {
         // a node
         sender_peer = nodes.getOrCreate (sender_node, dgram.getSender ());
      }
      // we could check the receiver field but why bother?

      debugRcvAny (pmsg, sender_peer);

      // process epoch fields
      boolean msg_valid = processEpochs (pmsg, sender_peer);

      if (! msg_valid)
         return;

      switch (pmsg.getKind ()) {
      case ProtMessage.MSGKIND_DATA:
         stats.addDataMessageReceived(1);
         processDataMessage (pmsg, sender_peer);
         break;
      case ProtMessage.MSGKIND_ACK:
         stats.addAckMessageReceived(1);
         processAcknowledgement (pmsg, sender_peer);
         break;
      case ProtMessage.MSGKIND_EPOCH:
         stats.addEpochMessageReceived(1);
         processEpochMessage (pmsg, sender_peer);
         break;
      default:
         throw new ProtocolException ("unknown message kind");
      }

      if(Debug.statsTime())
      {
         layer_stopwatch.stop();
         stats.addMessengerLayerTime(layer_stopwatch);
      }
   }

   /**
      Does all general processing related to the epoch fields of an incoming
      message. No processing is performed that is specific to a message type.
      Processing may include:
      <ul>
      <li>transmitting data messages in the epoch queue
      <li>setting/clearing timers
      <li>flushing messages from in or out queues
      <li>resetting sequence numbers
      </ul>
      The result of epoch checking may be that the message should be
      discarded. In that case, false is returned.

      @return false
         if the message should be discarded
   */
   private boolean processEpochs (ProtMessage pmsg, MessengerPeer sender_peer)
                                       throws IOException
   {
      if(Debug.statsTime())
         epoch_stopwatch.start();

      long sender_epoch = pmsg.getSenderEpoch();
      long receiver_epoch = pmsg.getReceiverEpoch();

      long peer_epoch = sender_peer.getEpoch();

      // sender_epoch processing

      if (peer_epoch == ProtMessage.UNKNOWN_EPOCH) {

         Debug.println ("    location service messenger: got epoch from " + sender_peer);

         // Peer epoch only now known. Disarm timer and send queued messages.
         sender_peer.setEpoch (sender_epoch);
         sender_peer.checkEpochTimer ();

         // transmit messages in the epoch queue
         for (Message msg = sender_peer.unqueueForEpoch(); msg != null;
               msg = sender_peer.unqueueForEpoch()) {
            sendDataMessage (sender_peer, msg);
         }
      }
      else {

         // Peer epoch already known

         if (sender_epoch < peer_epoch) {
            // ignore a message from before the sender crashed

            System.out.println ("    location service messenger: ignoring message from before "
                              + "peer " + sender_peer + " crashed.");

            if(Debug.statsTime())
            {
               epoch_stopwatch.stop();
               stats.addEpochProcessingTime(epoch_stopwatch);
            }

            return false;
         }

         else if (sender_epoch > peer_epoch) {
            // sender has crashed. Reset connections.

            System.out.println ("    location service messenger: peer " + sender_peer 
               + " has crashed: resetting.");

            sender_peer.setEpoch (sender_epoch);
            sender_peer.resetConnections();
            
            // see if the user wanted to be notified of crashes
            if(notify_crashes)
            {
               Message crash_message = new MessageImpl ();
               crash_message.setSender (sender_peer);
               crash_queue.enqueue(crash_message);
            }
         }
      }

      // receiver_epoch processing

      if (receiver_epoch == ProtMessage.UNKNOWN_EPOCH) {

         // receiver_epoch 'unknown'

         if (pmsg.getKind() != ProtMessage.MSGKIND_EPOCH)
            throw new ProtocolException ("'unknown' receiver epoch");
            
         if(Debug.statsTime())
         {
            epoch_stopwatch.stop();
            stats.addEpochProcessingTime(epoch_stopwatch);
         }

         return true; // further processing is epoch msg-specific
      }
      else {
         // receiver_epoch known

         if (receiver_epoch > my_epoch)
            throw new ProtocolException ("Some idiot (" + sender_peer + ") says I have crashed!");

         if (receiver_epoch < my_epoch) {

            // I *have* crashed...

            System.out.println ("    location service messenger: telling peer " + sender_peer + " I have crashed.");

            sendEpoch (sender_peer);
         
            if(Debug.statsTime())
            {
               epoch_stopwatch.stop();
               stats.addEpochProcessingTime(epoch_stopwatch);
            }

            return false;     // ignore the message
         }
      }
            
      if(Debug.statsTime())
      {
         epoch_stopwatch.stop();
         stats.addEpochProcessingTime(epoch_stopwatch);
      }

      return true;
   }


   /**
      Sends an epoch message. As a pre-condition, the peer's network address
      must be known.

      @param peer
         the peer to send the epoch message to
   */
   private void sendEpoch (MessengerPeer peer) throws IOException
   {
      ProtMessage pmsg = makeEpochMessage (peer);

      // make Datagram. The 'sender' field can be left empty.
      Datagram dgram = new DatagramImpl ();

      dgram.setReceiver (peer.getDatagramPeer ());
      
      if(Debug.statsTime())
         marshall_stopwatch.start();

      dgram.setPacket (pmsg.marshall ());

      if(Debug.statsTime())
      {
         marshall_stopwatch.stop();
         stats.addMessageMarshallTime(marshall_stopwatch);
      }

      debugSendEpoch (pmsg, peer);

      if(Debug.statsTime())
         layer_stopwatch.stop();

      sendDatagram (dgram);
      
      if(Debug.statsTime())
         layer_stopwatch.resume();


      stats.addEpochMessageSent(1);
   }

   /**
      Creates the protocol container of an epoch message. All required fields
      are filled in.

      @param peer
         the peer to which the epoch message will be sent
   */
   private ProtMessage makeEpochMessage (MessengerPeer peer)
                                       throws ProtocolException
   {
      ProtMessage epoch_msg = pmsg_fact.createProtMessage ();
      epoch_msg.setVersion (VERSION);
      epoch_msg.setKind (ProtMessage.MSGKIND_EPOCH);

      epoch_msg.setSenderEpoch (my_epoch);
      epoch_msg.setReceiverEpoch (peer.getEpoch()); // possibly 'unknown'

      if (local_peer instanceof NodePeer)
         epoch_msg.setSenderNode (((NodePeer) local_peer).getNodeID ());
      else
         epoch_msg.setSenderNode (null);
 
      if (peer instanceof NodePeer)
         epoch_msg.setReceiverNode (((NodePeer) peer).getNodeID ());
      else
         epoch_msg.setReceiverNode (null);
      
      return epoch_msg;
   }


   /**
      Processes an incoming data message, and updates the 'messages received'
      statistics. Assumes that the epoch fields have been dealt with already.

      @param pmsg
         the data message
      @param sender_peer
         the sender of the message
   */
   private void processDataMessage (ProtMessage pmsg, MessengerPeer sender_peer)
                                             throws IOException
   {
      InConnection in_conn = sender_peer.getInConnection ();
      long sequence = pmsg.getSequence ();

      // a possible optimisation is to check at this point (before conversion
      // to a Message type) whether a message with the same sequence number has
      // been seen before

      // Create a Message
      Message message = new MessageImpl ();
      message.setSender (sender_peer);
      message.setReceiver (local_peer);
      message.setConnectionNumber (pmsg.getUserConnection ());
      rawDef payload = pmsg.getPayload ();
      if (payload != null)
         message.setPayload (payload);
      else
         message.setPayload (RawOps.createRaw ());

      boolean retrans = false;      // whether this was retransmitted or not

      // moves deliverable messages to the delivery queue, which generates
      // callback events
      if (! in_conn.accept (message, sequence, user_queue)) {
         System.out.println ("    location service messenger ignoring a retransmission with seq " + sequence + " from " + sender_peer);
         stats.addDataMessageReceivedRetrans(1);
         retrans = true;
      }

      // see if the user should acknowledge it, or acknowledge it if it was a 
      // retransmission since we won't give it to the user more than once.
      if(! message.userMustAcknowledge() || retrans)
         sendAcknowledgement(pmsg, sender_peer);
      else
         message.setAckCookie(pmsg);

   }

   /**
      Sends an acknowledgement. This routine is call by the user of the 
      Messenger to acknowledge ordered Messages.

      @param msg
         the message to acknowledge
   */
   public void acknowledge(Message msg) throws IOException
   {
      if(Debug.statsTime())
         layer_stopwatch.start();

      MessengerPeer msger_peer = (MessengerPeer) msg.getSender();
      ProtMessage prot_msg = (ProtMessage) msg.getAckCookie();
      sendAcknowledgement(prot_msg, msger_peer);
      
      if(Debug.statsTime())
      {
         layer_stopwatch.stop();
         stats.addMessengerLayerTime(layer_stopwatch);
      }

   }

   /**
      Sends an acknowledgement.

      @param data_msg
         the data message to acknowledge
      @param peer
         the peer to send the acknowledgement to
   */
   private void sendAcknowledgement (ProtMessage data_msg, MessengerPeer peer)
                                                            throws IOException
   {
   
   /*
      try { Thread.sleep(1000); }
      catch (Exception e) { System.err.println(e.getMessage()); }
   */
      if(Debug.statsTime())
         ack_stopwatch.start();

      ProtMessage ack = makeAckMessage (data_msg, peer);

      // make Datagram. The 'sender' field can be left empty.
      Datagram dgram = new DatagramImpl ();

      dgram.setReceiver (peer.getDatagramPeer ());
      
      if(Debug.statsTime())
         marshall_stopwatch.start();

      dgram.setPacket (ack.marshall ());

      if(Debug.statsTime())
      {
         marshall_stopwatch.stop();
         stats.addMessageMarshallTime(marshall_stopwatch);
      }

      debugSendAck (ack, peer);

      if(Debug.statsTime())
         layer_stopwatch.stop();

      sendDatagram (dgram);

      
      if(Debug.statsTime())
         layer_stopwatch.resume();

      if(Debug.statsTime())
      {
         ack_stopwatch.stop();
         stats.addMessengerAckTime(ack_stopwatch);
      }
      stats.addAckMessageSent(1);
   }

   /**
      Creates the protocol container of an acknowledgment. All required fields
      are filled in.

      @param data_msg
         the data message to acknowledge
      @param peer
         the peer to which acknowledgement will be sent
   */
   private ProtMessage makeAckMessage (ProtMessage data_msg, MessengerPeer peer)
                                                throws ProtocolException
   {
      ProtMessage ack = pmsg_fact.createProtMessage ();
      ack.setVersion (VERSION);
      ack.setKind (ProtMessage.MSGKIND_ACK);

      ack.setSenderEpoch (my_epoch);
      ack.setReceiverEpoch (peer.getEpoch());

      // reverse sender and receiver. assume receiver has been set correctly
      // in data_msg
      ack.setSenderNode (data_msg.getReceiverNode ());
      ack.setReceiverNode (data_msg.getSenderNode ());
      ack.setUserConnection (data_msg.getUserConnection ());
      ack.setInternalConnection (data_msg.getInternalConnection ());
      ack.setSequence (data_msg.getSequence ());

      return ack;
   }

   /**
      Processes an incoming acknowledgement, and updates the 'messages sent'
      statistics. Assumes that the epoch fields have been dealt with already.

      @param ack
         the acknowledgement
      @param sender_peer
         the sender of the acknowledgement
   */
   private void processAcknowledgement (ProtMessage ack,
                     MessengerPeer sender_peer) throws IOException
   {
      OutConnection out_conn = sender_peer.getOutConnection ();

      out_conn.acknowledge (ack.getSequence(), ack_queue);
      out_conn.checkRetransTimer ();
   }

   /**
      Processes an incoming epoch message, and updates the 'messages sent'
      statistics. Assumes that general processing (i.e. not specifically
      related to the Epoch message kind) of the epoch fields has been performed
      already.

      @param epoch_msg
         the epoch message
      @param sender_peer
         the sender of the epoch message
   */
   private void processEpochMessage (ProtMessage epoch_msg,
                     MessengerPeer sender_peer) throws IOException
   {
      if (epoch_msg.getReceiverEpoch() == ProtMessage.UNKNOWN_EPOCH) {
          System.out.println ("    location service messenger: informing peer " 
            + sender_peer + " of my epoch.");
         sendEpoch (sender_peer);
      }
   }

   /**
      Notification from the retransmission timer.
   */
   private synchronized void notifyRetransTimerEvent (ActiveUID user_info)
   {
      if(Debug.statsTime())
         layer_stopwatch.start();

      OutConnection out_conn = (OutConnection) user_info;

      // send unacked messages. currently we don't check the age of these
      // messages.
      Enumeration unacked = out_conn.getUnacked ();

      // retransmit a maximum of MAX_RETRANS messages
      int count;
      Datagram dgram = null;
      for (count = 0; count < MAX_RETRANSMIT && unacked.hasMoreElements ();
            count++) {

         OutConnection.SentMessage snt_msg = (OutConnection.SentMessage)
	 				unacked.nextElement ();
         dgram = snt_msg.datagram;

   	  	 System.out.println ("    location service messenger: retransmitting message with seq " + snt_msg.sequence + " to " + dgram.getReceiver());

         if(Debug.statsTime())
            layer_stopwatch.stop();

         sendDatagram (dgram);
         
         if(Debug.statsTime())
            layer_stopwatch.resume();

         stats.addDataMessageSentRetrans(1);
      }
      
      if (dgram != null)
         debugRetransmit (dgram.getReceiver (), count);

      // reset the timer
      out_conn.checkRetransTimer ();

      if(Debug.statsTime())
      {
         layer_stopwatch.stop();
         stats.addMessengerLayerTime(layer_stopwatch);
      }
   }

   /**
      Notification from the epoch timer.
   */
   private synchronized void notifyEpochTimerEvent (ActiveUID user_info)
   {
      if(Debug.statsTime())
         layer_stopwatch.start();

      MessengerPeer peer = (MessengerPeer) user_info;

      try {
         if (! mapNodeID (peer))
            System.out.println ("    location service messenger: address still unknown " + peer);
         else {

            System.out.println ("    location service messenger: transmitting epoch to " + peer);

            sendEpoch (peer);
            stats.addEpochMessageSentRetrans(1);
         }
      }
      catch (IOException exc) {
         System.err.println ("location service messenger ignoring I/O exception:");
         exc.printStackTrace ();
      }

      peer.checkEpochTimer ();

      if(Debug.statsTime())
      {
         layer_stopwatch.stop();
         stats.addMessengerLayerTime(layer_stopwatch);
      }
   }

   // debugging hooks. These can be overridden by a debugging subclass.

   /**
      Empty debugging hook. This method is called by <code>send</code> at the
      point a data message is sent. A debugging subclass can override this hook
      to print some info.
   */
   protected void debugSendData (ProtMessage pmsg, MessengerPeer dest)
   {
   }

   /**
      Empty debugging hook. This method is called at the point an
      acknowledgement is sent. A debugging subclass can override this hook to
      print some info.
   */
   protected void debugSendAck (ProtMessage ack, MessengerPeer dest)
   {
   }

   /**
      Empty debugging hook. This method is called by at the point an epoch
      message is sent. A debugging subclass can override this hook to print
      some info.
   */
   protected void debugSendEpoch (ProtMessage pmsg, MessengerPeer dest)
   {
   }


   /**
      Empty debugging hook. This method is called at the point a message
      has arrived. A debugging subclass can override this hook to print some
      info.
   */
   protected void debugRcvAny (ProtMessage pmsg, MessengerPeer src)
   {
   }

   /**
      Empty debugging hook. This method is called whenever retransmissions
      are sent. A debugging subclass can override this hook to print some info.

      @param dgram_dest
         the datagram layer peer destination
      @param msg_count
         the number of retransmissions
   */
   protected void debugRetransmit (Peer dgram_dest, int msg_count)
   {
   }
}
