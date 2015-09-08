/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.comm;

import vu.globe.util.comm.idl.rawData.rawDef;

import vu.globe.util.time.Stopwatch;

/**
   The message passed to a <code>MessageOriented<code> object.
   <p>
   A <code>Message</code> has a sender, a receiver, a connection number
   and some payload.
   <p>
   Connections are identified by (sender, receiver, connection number).
   A message can be sent on any positive connection number. All positive
   connection numbers are open, reliable, ordered connections. In addition,
   the connection numbers <code>CONN_RELIABLE</code> and
   <code>CONN_UNRELIABLE</code> are open,
   unordered connections which send messages reliably and unreliably,
   respectively.
   <p>
   A <code>Message</code> is a container of the sender and receiver
   <code>Peer</code>s and of a packet.
 
   @author Patrick Verkaik
*/
 
public interface Message
{
   /**
      Smallest legal connection number. A connection number <code>conn</code>
      with <code>conn != CONN_NONE && conn >= CONN_MIN</code> is a legal
      connection number.
   */
   static final int CONN_MIN = -2;

   /** Predefined connection number of unreliable, unordered connection. */
   static final int CONN_UNRELIABLE = -2;

   /** Predefined connection number of reliable, unordered connection. */
   static final int CONN_RELIABLE = -1;

   /** Invalid connection number. */
   static final int CONN_NONE = 0;

   /**
      Smallest reliable, ordered connection number. All connection numbers
      <code>conn >= CONN_ORDERED_MIN</code> are reliable, ordered connections.
   */
   static final int CONN_ORDERED_MIN = 1;

   /**
      Places a sender <code>Peer</code> in this message.
   */
   void setSender (Peer peer);

   /**
      Retrieves the sender <code>Peer</code> of this message.
   */
   Peer getSender ();

   /**
      Places a receiver <code>Peer</code> in this message.
   */
   void setReceiver (Peer peer);

   /**
      Retrieves the receiver <code>Peer</code> of this message.
   */
   Peer getReceiver ();

   /**
      Sets the connection number of this message.

      @exception IllegalArgumentException
         if the connection number was invalid.
   */
   void setConnectionNumber (int connNo);

   /** Retrieves the connection number of this message. */
   int getConnectionNumber ();

   /**
      Places a payload packet in this message.
   */
   void setPayload (rawDef payload);
          
   /**
      Retrieves the payload packet of this message.
   */
   rawDef getPayload ();

   /**
      Whether the user should acknowledge this message or
      the Messenger will automatically do it.
   */
   boolean userMustAcknowledge();

   /**
      Sets a cookie for this Message. Used by the Messenger when
      the user passes the Message back down to send the acknowledgement
      and used by the user when the Messenger notifies him/her that
      the Message has been acknowledged.
   */
   void setAckCookie(Object cookie);

   /**
      Retrieves the cookie.
   */
   Object getAckCookie();

   //XXXDAN for timing
   Stopwatch stopwatch = new Stopwatch();
}
