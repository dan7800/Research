/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.comm.messenger.proto;

import vu.globe.svcs.gls.comm.Peer;
import vu.globe.svcs.gls.types.PNodeID;
import vu.globe.svcs.gls.protocol.ProtMessage;
import vu.globe.svcs.gls.comm.Message;
import vu.globe.svcs.gls.active.*;
import vu.globe.svcs.gls.active.ActiveTimerFact.ActiveTimer;
import vu.globe.util.types.Queue;

/**
   The messenger's peer implementation. Defines a node id, a datagram peer,
   epoch state and reliability state. A full typed address for a messenger peer
   can be constructed from the node id and the datagram peer's typed address.
   <p>
   Only messenger peers that represent nodes contain a node id. If so,
   identification is based on the node id. If not, it is based on the datagram
   peer. Subtypes must ensure that the way of identification cannot change.
   <p>
   The epoch state consists of the epoch of the peer, and a queue of messages
   waiting to be sent to that peer. Messages are queued if the peer epoch is
   still unknown. The messages should be sent once the epoch becomes known. The
   Messenger is responsible for this.

   @author Patrick Verkaik
*/

abstract class MessengerPeer implements Peer, ActiveUID
{
   /** The datagram peer. May be null if not the identifying part. */
   private Peer datagram_peer;

   // reliability and ordering.
   private InConnection in_conn;    // set when first required
   private OutConnection out_conn;  // set when first required

   // epoch state

   /** The epoch of the peer. UNKNOWN_EPOCH if still unknown. */
   private long epoch = ProtMessage.UNKNOWN_EPOCH;

   /** Outgoing messages waiting for the epoch to be known. */
   private Queue epoch_queue;       // set when first used

   /** The epoch timer interval in milliseconds. */
   public static final long EPOCH_TIMEVAL = 6000;

   /** The timers */
   private ActiveTimer retrans_timer;
   private ActiveTimer epoch_timer;
   
   /**
      Constructs a messenger peer.

   */
   protected MessengerPeer ()
   {
   }

   /**
      Constructs a messenger peer with a datagram peer.

      @param datagram_peer
         the datagram peer to associate with
   */
   protected MessengerPeer (Peer datagram_peer)
   {
      this.datagram_peer = datagram_peer;
   }

   protected void setTimers(ActiveTimer retrans_timer, ActiveTimer epoch_timer)
   {
      this.retrans_timer = retrans_timer;
      this.epoch_timer = epoch_timer;
   }

   /**
      Associates this messenger peer with a datagram peer. This should only
      be used if the datagram peer is not the identifying element, i.e. a node
      id is present.
   */
   protected void setDatagramPeer (Peer datagram_peer)
   {
      this.datagram_peer = datagram_peer;
   }

   /**
      Returns the datagram peer with which this messenger peer is associated.

      @return
         null, if no datagram peer has been set.
   */
   public Peer getDatagramPeer ()
   {
      return datagram_peer;
   }

   /**
      Returns the node id which this messenger peer is associated.

      @return
         null if this messenger peer does not represent a node
   */
   public abstract PNodeID getNodeID ();

   /**
      Returns the incoming connection from this messenger peer.
   */
   public InConnection getInConnection ()
   {
      if (in_conn == null)
         in_conn = new InConnection ();
      return in_conn;
   }

   /**
      Returns the outgoing connection to this messenger peer.
   */
   public OutConnection getOutConnection ()
   {
      if (out_conn == null)
         out_conn = new OutConnection (retrans_timer);
      return out_conn;
   }

   /**
      Returns the peer's epoch.

      @return
         the peer's epoch, or UNKNOWN_EPOCH if unknown
   */
   public long getEpoch()
   {
      return epoch;
   }

   /**
      Sets the peer's epoch.

      @param epoch
         the peer's epoch, or UNKNOWN_EPOCH to clear
   */
   public void setEpoch (long epoch)
   {
      if (this.epoch != ProtMessage.UNKNOWN_EPOCH && this.epoch >= epoch)
         throw new IllegalArgumentException ("epoch can only increase");
      this.epoch = epoch;
   }

   /**
      Queues a data message in the epoch queue.

      @return     whether or not this is first message to be queued for the 
                  epoch.
   */
   public boolean queueForEpoch (Message msg)
   {
      boolean first_time = false;

      if (epoch_queue == null)
      {
         epoch_queue = new Queue();
         first_time = true; 
      }
      epoch_queue.enqueue (msg);

      return first_time;
   }

   /**
      Unqueues a data message from the epoch queue.

      @return
         the message, or null if none remaining
   */
   public Message unqueueForEpoch ()
   {
      if (epoch_queue == null)
         return null;

      return (Message) epoch_queue.dequeue();
   }

   /**
      Checks whether the epoch timer needs to be on or off. The timer
      is kept alive (only) as long as the epoch remains unknown. When the timer
      expires, the ensuing notification will pass the <code>MessengerPeer</code>
      as a user cookie.
   */
   public void checkEpochTimer ()
   {
      boolean old_armed = epoch_timer.armed ();
      boolean new_armed = epoch == ProtMessage.UNKNOWN_EPOCH;
 
      if (new_armed && ! old_armed)
         epoch_timer.set (EPOCH_TIMEVAL, this);
      else if (! new_armed && old_armed)
         epoch_timer.cancel ();
   }

   /**
      Resets all connections.
      Removes all unacked data messages from the outgoing connection, and all
      undeliverable data messages from the incoming connection. Resets the
      sequence numbers. Resets the retransmission timer outgoing for data
      messages. Does not affect the epoch queue or the epoch timer.

   */
   public void resetConnections ()
   {
      if (in_conn != null)
         in_conn.reset();

      if (out_conn != null)
         out_conn.reset ();
   }

   // Peer interface is left abstract
}

