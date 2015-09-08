/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.comm;

import vu.globe.util.comm.idl.rawData.rawDef;

/**
   @author Patrick Verkaik
*/

public class MessageImpl implements Message
{
   protected Peer sender;
   protected Peer receiver;
   protected int conn = CONN_NONE;
   protected rawDef payload;
   protected Object ack_cookie = null;

   public void setSender (Peer peer)
   {
      sender = peer;
   }

   public Peer getSender ()
   {
      return sender;
   }

   public void setReceiver (Peer peer)
   {
      receiver = peer;
   }

   public Peer getReceiver ()
   {
      return receiver;
   }

   public void setConnectionNumber (int connNo)
   {
      if (conn < CONN_MIN)
         throw new
            IllegalArgumentException ("illegal Message connection number");
      conn = connNo;
   }

   public int getConnectionNumber ()
   {
      return conn;
   }

   public void setPayload (rawDef payl)
   {
      payload = payl;
   }
          
   public rawDef getPayload ()
   {
      return payload;
   }

   public boolean userMustAcknowledge()
   {
      return (conn == CONN_RELIABLE || conn > CONN_ORDERED_MIN);
   }

   public void setAckCookie(Object cookie)
   {
      ack_cookie = cookie;
   }

   public Object getAckCookie()
   {
      return ack_cookie;
   }
}
