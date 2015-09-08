/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.comm.messenger.proto;

import vu.globe.svcs.gls.active.*;
import vu.globe.svcs.gls.active.proto.*;
import vu.globe.svcs.gls.active.ActiveTimerFact.ActiveTimer;
import vu.globe.util.types.Queue;
import vu.globe.util.types.DebugQueue;
import vu.globe.util.exc.NotSupportedException;
import vu.globe.svcs.gls.comm.Datagram;
import vu.globe.svcs.gls.comm.Message;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
   The state of a Messenger's outgoing connection to some peer. The state
   comprises a set of unacknowledged messages.
 
   @author Patrick Verkaik
*/

/*
   The set of unacked messages is represented by a queue rather than a hash
   table. A queue allows us to generate sequence numbers of unacked messages
   in order more easily. A hashtable on the other hand would be more
   efficient at processing acknowledgments. A combination might be feasible.
*/

class OutConnection implements ActiveUID
{
   /** The timer interval in milliseconds. */
   public static final long RETRANS_TIMEVAL = 6000;

   /** The highest sequence set in outgoing messages. */
   private long max_seq_sent = -1L;

   /**
      A queue of outgoing marshalled messages, each of type SentMessage.
   */
   private UnackedFilter sent_messages;

   /** The retransmission timer */
   private ActiveTimer retrans_timer;

   /**
      Constructor.
   */
   public OutConnection (ActiveTimer retrans_timer)
   {
      this.retrans_timer = retrans_timer;
      sent_messages = new UnackedFilter ();
   }

   /**
      Generates a new sequence number.
   */
   public long nextSequence ()
   {
      return ++max_seq_sent;
   }

   /**
      Registers as 'unacked' a message marshalled into a datagram.

      @param seq
         the message's sequence number
      @param dgram
         the marshalled message
      @param msg
         the unmarshalled message
   */
   public void register (long seq, Datagram dgram, Message msg)
   {
      sent_messages.enqueue (new SentMessage (seq, dgram, msg));
   }

   /**
      Marks the data message with sequence number <code>seq</code> as 'acked'.
      A message may be acknowledged more than once. If <code>ack_queue</code>
      is not null, it is used to notify the user that the message
      has been acknowledged.

      @return
         true iff a message was acknowledged for the first time
   */
   public boolean acknowledge (long seq, ActiveQueue ack_queue)
   {
      boolean newly_acked = false;     // the result to be returned

      // search the sequence number
      Enumeration enum = sent_messages.elements ();
      while (enum.hasMoreElements ()) {

         // note: there is no guarantee that elements are in perfect sequence
         // order;  we have to traverse the entire queue. If not found then
         // must have been acked before.

         SentMessage sent_msg = (SentMessage) enum.nextElement ();
         if (sent_msg.sequence == seq) {
            newly_acked = true;
            sent_msg.acked = true;
            if(sent_msg.message.getAckCookie() != null)
               ack_queue.enqueue(sent_msg.message);
            break;
         }
      }
      
      return newly_acked;
   }

   /**
      Returns an enumeration of currently 'unacked' data messages.

      @return
         an enumeration of <code>Datagram</code> in the order they were
         registered
   */
   public Enumeration getUnackedDgram ()
   {
      return sent_messages.datagramElements ();
   }

   /**
      Returns an enumeration of currently 'unacked' data messages.

      @return
         an enumeration of <code>SentMessage</code> in the order they were
         registered
   */
   public Enumeration getUnacked ()
   {
      return sent_messages.elements ();
   }

   /**
      Checks whether the retransmission timer needs to be on or off. The timer
      is kept alive (only) as long as unacknowledged messages remain.  When
      the timer expires, the ensuing notification will pass the
      <code>OutConnection</code> as a user cookie.

   */
   public void checkRetransTimer ()
   {
      boolean old_armed = retrans_timer.armed ();
      boolean new_armed = ! sent_messages.isEmpty ();

      if (new_armed && ! old_armed)
         retrans_timer.set (RETRANS_TIMEVAL, this);
      else if (! new_armed && old_armed)
         retrans_timer.cancel ();
   }

   /**
      Resets this connection. Removes all unacked data messages, and resets the
      sequence number. Also resets the retransmission timer.

      @param retrans_timer
         the retransmission timer
   */
   public void reset ()
   {
      max_seq_sent = -1L;
      sent_messages.flush();
      checkRetransTimer ();
      
   }

   /**
      A sent message.
   */
   public class SentMessage
   {
      /**
         The sequence number.
      */
      public long sequence;
   
      /**
         The message in marshalled form.
      */
      public Datagram datagram;
   
      /**
         The message in unmarshalled form (given back to user so she can 
         retrieve her cookie).
      */
      public Message message;
   
      /**
         Unacknowledged until set to true.
      */
      public boolean acked = false;
   
      public SentMessage (long seq, Datagram dgram, Message msg)
      {
         sequence = seq;
         datagram = dgram;
         message = msg;
      }
   }
   
   /**
      A queue of <code>SentMessage</code>. Sent messages that have been
      acknowledged are silently filtered from the queue.
      The following methods are not supported:
      <ul>
      <li>peekTail
      <li>size
      </ul>
   */
   class UnackedFilter extends Queue // or: extends DebugQueue
   {
      public UnackedFilter ()
      {
         super ();
         // if DebugQueue is extended:     super ("unacked queue", 100);
   
      }
   
      public Object dequeue ()
      {
         dequeueAcked ();
         return super.dequeue ();
      }
   
      public Object peekHead ()
      {
         dequeueAcked ();
         return super.peekHead ();
      }
   
      public Object peekTail ()
      {
         // cannot traverse back to front
         throw new NotSupportedException ();
      }
   
      public int size ()
      {
         // cannot determine the size without enumerating every element
         throw new NotSupportedException ();
      }
   
      public boolean isEmpty ()
      {
         dequeueAcked ();
         return super.isEmpty ();
      }
      
      public Enumeration elements ()
      {
         dequeueAcked ();
         return new UnackedEnumeration (super.elements ());
      }
   
      /**
         Returns an enumeration of the marshalled messages, each of type
         <code>Datagram</code>.
      */
      public Enumeration datagramElements ()
      {
         dequeueAcked ();
         return new DatagramEnumeration (super.elements ());
      }
   
      /**
         Removes all acked messages at the head of the queue.
      */
      private void dequeueAcked ()
      {
         while (true) {
            SentMessage head = (SentMessage) super.peekHead ();
            if (head == null || head.acked == false)
               break;
            super.dequeue ();
         }
      }
   }
   
   /**
      UnackedFilter's standard enumerator. Given an enumeration of sent messages,
      implements an enumeration of only unacked messages.
   */
   class UnackedEnumeration implements Enumeration
   {
      // enumeration of all messages
      private Enumeration all_enum;
   
      // look-ahead buffer. Set by lookAhead(). Cleared when returned to user.
      private SentMessage look_ahead;
   
      /**
         Constructor.
   
         @param sent_messages
            the enumeration of all messages
      */
      public UnackedEnumeration (Enumeration sent_messages)
      {
         all_enum = sent_messages;
      }
   
      public boolean hasMoreElements ()
      {
         lookAhead ();
         return look_ahead != null;
      }
   
      public Object nextElement ()
      {
         lookAhead ();
         if (look_ahead == null)
            throw new NoSuchElementException ();
         SentMessage next = look_ahead;
         look_ahead = null;
         return next;
      }
   
      /**
         If the look-ahead buffer is null, and there are more elements then
         fills the look-ahead buffer. Otherwise the buffer remains null.
      */
      private void lookAhead ()
      {
         if (look_ahead != null)
            return;
   
         while (all_enum.hasMoreElements ()) {
            SentMessage current = (SentMessage) all_enum.nextElement ();
            if (! current.acked) {
               look_ahead = current;
               break;
            }
         }
      }
   }
   
   /**
      As <code>UnackedEnumeration</code> except returns marshalled messages of
      type <code>Datagram</code>.
   */
   class DatagramEnumeration extends UnackedEnumeration
   {
      public DatagramEnumeration (Enumeration sent_messages)
      {
         super (sent_messages);
      }
   
      public Object nextElement ()
      {
         SentMessage next_sent_msg = (SentMessage) super.nextElement ();
         return next_sent_msg.datagram;
      }
   }
   
}
