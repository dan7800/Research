/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.comm.datagramx.tcp;

import java.net.*;
import java.io.*;

import vu.globe.svcs.gls.active.*;
import vu.globe.svcs.gls.active.net.PassiveServerSocket;
import vu.globe.svcs.gls.active.proto.PassiveActivatorImpl;
import vu.globe.svcs.gls.types.*;
import vu.globe.util.exc.*;
import vu.globe.svcs.gls.comm.*;
import vu.globe.svcs.gls.protocol.*;
import vu.globe.svcs.gls.alert.AlertID;

import vu.globe.svcs.gls.typedaddress.*;
import vu.globe.svcs.gls.typedaddress.stdcomps.InternetLib;

import vu.globe.svcs.gls.comm.*;
import vu.globe.svcs.gls.config.*;
import vu.globe.svcs.gls.protocol.*;
import vu.globe.svcs.gls.comm.DatagramX;

import vu.globe.svcs.gls.stats.Statistics;

import vu.globe.util.comm.idl.rawData.rawDef;
import vu.globe.util.comm.RawOps;
import vu.globe.util.comm.RawUtil;

/**
   <code>TcpDatagramX</code> is a <code>DatagramX</code> based on TCP. A
   TCP connection is used for the transmission of each single datagram.
   Warning: this datagram layer is no longer being maintained.
 
   @author Patrick Verkaik
*/


/**
   Further object-defined behaviour:

   <ul>
   <li>This layer manages TCP/IP addresses.
   <li>The addresses visible to the user are TCP listen addresses.
   <li>Fragmented datagrams are never produced. Nor are they accepted.
   </ul>
*/

/*
   -  A PassiveActivator is used to activate a listen socket. Since a separate
      TCP connection is used for each datagram, we do not activate connection
      sockets. Instead, we block pollDatagram() while waiting for the
      datagram to arrive. This would certainly have to be improved.

      The advantage is that now we can call accept() in pollDatagram(), and
      don't need to remember what connection sockets are ready. Nor do we
      need to buffer partially arrived datagrams.
   -  The object requires the protocol to have sender address argument (normally
      'optional'). This is because the address at which a sender of a datagram
      can be contacted is its listen address, rather than the address through
      which it sent the datagram.
   -  A Notifiable is implemented for use by a PassiveActivator.
   -  The same registration id is always returned to the user: null
   -  The same user info is always passed to the PassiveActivator: null
   -  bindAddress() may only be called while a TcpDatagramX is deactivated.
      This implies that the user mustn't call activate() concurrently with
      bindAddress(), which allows for a thread-unsafe use of the listen
      socket. A thread-safe implementation could place bindAddress(),
      activate() and deactivate() in a monitor.
   -  Peers are managed using PeerImpl. No attempt is made to unify Peers
      with the same address.
   -  Datagrams are sent in one fragment: fragment 0.
   -  To conform to more advanced (fragmenting) datagram implementations,
      datagram ids are properly assigned.
*/

public class TcpDatagramX implements EnvObject, DatagramX, Notifiable
{
   /* Environment */
   protected ProtDatagramFact pdgram_fact;

   // Use of the Scheduler
   protected Scheduler scheduler;
   protected PassiveActivator activator; // set while activated.

   protected MyPeer listenPeer;        // Set when bound. Used while activated.
   protected PassiveServerSocket listenSock; // Set when bound

   // TcpDatagramX has a single user.
   protected Notifiable user_notifiable;
   protected ActiveUID user_cookie;

   // Datagram identifier.
   protected Counter dgram_id = new Counter (0);

   // Maximum queue length on a listen socket.
   protected static final int LISTEN_BACKLOG = 50;

   // 16 bit version number
   protected static final int VERSION = 1;

   private Statistics stats;

   public void initObject (ObjEnv env)
   {
      System.out.println ("warning: the TCP datagram layer is no longer " +
                           "being maintained");
      // set environment.
      pdgram_fact = env.getProtDatagramX ();
      scheduler = env.getScheduler ();
      stats = env.getStatistics ();
   }


   public void send (Datagram datagram, AlertID id) throws IOException
   {
      // Use listenPeer as the sender.
      rawDef marsh_dgrm = marshallDatagram (datagram, listenPeer);
      sendPacket (datagram.getReceiver (), marsh_dgrm);
   }

   public Datagram pollDatagram () throws IOException
   {
      // we don't need a sender from recvPacket(). It is filled in during
      // unmarshalling.
      rawDef marsh_dgrm = recvPacket ();
      if (marsh_dgrm == null)
         return null;

      Datagram dgram = unmarshallDatagram (marsh_dgrm);
      dgram.setReceiver (listenPeer);

      return dgram;
   }

   // EndPoint interface
   public void bindAddress (Peer local_peer) throws IOException
   {
      if (! (local_peer instanceof MyPeer))
         throw new WrongFactoryException ("unknown Peer");
      MyPeer local = (MyPeer) local_peer;

      StackedAddress sa = (StackedAddress) local.getAddress ();
      bindAddress (InternetLib.getIp (sa), InternetLib.getTcp (sa));
   }

   public void bindAddress () throws IOException
   {
      bindAddress (null, -1);
   }

   /**
      Binds this object to the given address and port. Either may be left
      unspecified by passing null and -1 resp.
   */
   protected void bindAddress (InetAddress inet, int port) throws IOException
   {
      if (inet == null) {
         if (port == -1)
            listenSock = new PassiveServerSocket (0, LISTEN_BACKLOG);
         else
            listenSock = new PassiveServerSocket (port, LISTEN_BACKLOG);
      }
      else {
         if (port == -1) {
            // not my fault. blame java.net.ServerSocket
            throw new IllegalArgumentException ("unspecified port with" +
                                                   " specified address");
         }
         else
            listenSock = new PassiveServerSocket (port, LISTEN_BACKLOG, inet);
      }
 
      // create listenPeer
 
      // listenSock.getInetAddress() returns an invalid address if inet==null,
      // so don't use it.

      InetAddress local_inet = (inet == null ?
                              InetAddress.getLocalHost () : inet);
      listenPeer = new MyPeer (new StackedAddressImpl (local_inet,
                     listenSock.getLocalPort (), StackedAddressImpl.TCP_PORT));
   }

   public Peer getBoundAddress ()
   {
      return listenPeer;      // null if unbound
   }

   // Active interface
   public ActiveRID activate (int service, Notifiable note, ActiveUID userInfo)
   {
      throw new NotSupportedException ("activate() with service");
   }

   public ActiveRID activate (Notifiable note, ActiveUID userInfo)
   {
      if (listenSock == null)
         throw new IllegalStateException ("not yet bound to an address");

      user_notifiable = note;
      user_cookie = userInfo;

      activator = new PassiveActivatorImpl (scheduler, stats);
      activator.activate (listenSock, this, null);

      // No registration id required.
      return null;
   }

   public void deactivate (ActiveRID regID)
   {
      activator.deactivate ();
   }

   // PeerManager interface
   public Peer createPeer (TypedAddress user_addr)
   {
      return new MyPeer (user_addr);
   }

   // Notifiable interface
   public void notifyEvent (ActiveUID userInfo)
   {
      /*
         The PassiveActivator notifies when a connection is waiting, one
         callback with one event per callback.
         Each incoming connection denotes the arrival of a single datagram.
      */
      user_notifiable.notifyEvent (user_cookie);
   }

   // Non-public methods.

   /**
      Constructs a packet conforming to the protocol. The protocol input
      fields consist of the fields in 'dgram' plus the other arguments.

      @param dgram      the datagram
      @param local      the end-point address to use as the sender.

      @exception IllegalArgumentException
         if one of the protocol input fields was invalid.
      @exception ProtocolException
         if an error occurred while encoding the protocol.
   */
   protected rawDef marshallDatagram (Datagram dgram, MyPeer local)
                                       throws ProtocolException
   {
      // No fragmentation, i.e. use fragment 0 to carry the whole datagram.

      ProtDatagram pdgram;
      rawDef payload;

      payload = dgram.getPacket ();
 
      pdgram = pdgram_fact.createProtDatagram ();
      pdgram.setVersion (VERSION);
      pdgram.setDatagramID (dgram_id.increase ());
      pdgram.setDatagramLength (RawOps.sizeOfRaw (payload));
      pdgram.setFragmentNumber (0);
      pdgram.setFragment (payload);
      pdgram.setSender (local.getAddress ());

      return pdgram.marshall ();
   }

   /**
      Reconstructs a datagram from a packet conforming to the protocol.
      The receiver field is not filled in.

      @exception ProtocolException
           if an error occurred while decoding the protocol.
   */
   protected Datagram unmarshallDatagram (rawDef marsh_dgrm)
                                             throws ProtocolException
   {
      ProtDatagram pdgram;
      Datagram dgram;
      rawDef payload;

      pdgram = pdgram_fact.createProtDatagram ();
      marsh_dgrm = pdgram.unmarshall (marsh_dgrm);
 
      if (marsh_dgrm != null)
         throw new ProtocolException ("datagram had trailing bytes");

      // Note that the MissingValueExceptions thrown are also ProtocolExceptions
      payload = pdgram.getFragment ();

      if (pdgram.getVersion () != VERSION)
         throw new ProtocolException ("wrong protocol version number");

      if (pdgram.getFragmentNumber () != 0 ||
          RawOps.sizeOfRaw (payload) != pdgram.getDatagramLength ())

         throw new ProtocolException ("payload length != header field");

      dgram = new DatagramImpl ();
      TypedAddress sender_addr = pdgram.getSender ();
      dgram.setSender (new MyPeer (sender_addr));
      dgram.setPacket (payload);

      return dgram;
   }

   /**
      Transmits a packet to the specified destination, by connecting
      to the 'dest'.
   */
   protected void sendPacket (Peer peer_dest, rawDef packet) throws IOException
   {
      if (! (peer_dest instanceof MyPeer))
         throw new WrongFactoryException ("unknown Peer");
      MyPeer peer = (MyPeer) peer_dest;

      StackedAddress sa = (StackedAddress) peer.getAddress ();

      InetAddress inet = InternetLib.getIp (sa);
      int port = InternetLib.getTcp (sa);

      if (inet == null || port == -1)
         throw new IllegalArgumentException ("partial TCP/IP address");

      Socket conn = new Socket (inet, port);
      OutputStream stream = conn.getOutputStream ();
      RawUtil.outputRaw (packet, stream, 0, RawOps.sizeOfRaw (packet));
      conn.close ();
   }

   /**
      Tries to receive a packet from the first available connection without
      waiting for such a connection to arrive. If no connection is available,
      then returns null, otherwise returns the packet.

      If an available connection is found, this method will block while
      waiting for the data to come in. (See notes)
   */
   protected rawDef recvPacket () throws IOException
   {
      // Note listenSock accepts connections without blocking.
      Socket conn = listenSock.accept ();
      if (conn == null)
         return null;

      // read everything into a packet while blocking
      InputStream stream = conn.getInputStream ();
      rawDef packet = RawOps.createRaw ();
      RawUtil.inputRawFully (packet, stream, 0, -1);

      conn.close ();
      return packet;
   }
}

/**
   A thread-safe increasing counter.
*/
class Counter
{
   protected int count;

   public Counter ()
   {
      this (0);
   }

   public Counter (int start)
   {
      count = start;
   }

   public synchronized int increase ()
   {
      return count++;
   }
}


/**
   We use MyPeer instead of PeerImpl, so that we can check that user-provided
   Peers were made by our PeerManager. 
*/
class MyPeer extends PeerImpl
{
   public MyPeer (TypedAddress typed_addr)
   {
      super (typed_addr);
   }
}
