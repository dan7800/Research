/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

package vu.globe.svcs.gls.comm.datagramx.udp;

import java.net.*;
import java.io.*;

import vu.globe.svcs.gls.active.*;
import vu.globe.svcs.gls.active.net.PassiveDatagramSocket;
import vu.globe.svcs.gls.active.proto.PassiveActivatorImpl;
import vu.globe.util.exc.*;
import vu.globe.svcs.gls.types.*;
import vu.globe.svcs.gls.comm.*;
import vu.globe.svcs.gls.protocol.*;
import vu.globe.svcs.gls.alert.AlertID;

import vu.globe.svcs.gls.typedaddress.*;
import vu.globe.svcs.gls.typedaddress.stdcomps.InternetLib;

import vu.globe.svcs.gls.comm.*;
import vu.globe.svcs.gls.stats.Statistics;
import vu.globe.svcs.gls.config.*;
import vu.globe.svcs.gls.protocol.*;
import vu.globe.svcs.gls.comm.DatagramX;

import vu.globe.svcs.gls.debug.Debug;
import vu.globe.util.time.Stopwatch;

import vu.globe.util.comm.idl.rawData.rawDef;
import vu.globe.util.comm.RawOps;
import vu.globe.util.comm.RawUtil;

/**
   <code>UdpDatagramX</code> is a <code>DatagramX</code> based on UDP. This
   implementation does not perform fragmentation and therefore cannot handle
   large datagrams.
   <p>
   Further object-defined behaviour:
   <ul>
   <li>This layer manages UDP/IP addresses.
   <li>Fragmented datagrams are never produced. Nor are they accepted.
   <li>The statistics object in the object environment is updated.
   <li>The 'localIP' and 'localPort' configuration properties are used by the
       bindAddress methods.
   </ul>
 
   @author Patrick Verkaik
*/

/*
   -  bindAddress() may only be called while a UdpDatagramX is deactivated.
      This implies that the user mustn't call activate() concurrently with
      bindAddress(), which allows for a thread-unsafe use of the UDP
      socket. A thread-safe implementation could place bindAddress(),
      activate() and deactivate() in a monitor.
   -  Peers are managed using PeerImpl. No attempt is made to unify Peers
      with the same address.
   -  Datagrams are sent in a single fragment: fragment 0.
   -  To conform to more advanced (fragmenting) datagram implementations,
      datagram ids are properly assigned.
*/

public class UdpDatagramX implements EnvObject, DatagramX, Notifiable
{
   /* Environment */
   protected ProtDatagramFact pdgram_fact;

   // Use of the Scheduler
   protected Scheduler scheduler;
   protected PassiveActivator activator; // set while activated.

   protected Statistics stats;

   protected MyPeer udp_peer;        // Set when bound. Used while activated.
   protected PassiveDatagramSocket udp_sock; // Set when bound

   // UdpDatagramX has a single user.
   protected Notifiable user_notifiable;
   protected ActiveUID user_cookie;

   // use of Stopwatch for statistics
   private Stopwatch layer_stopwatch;
   private Stopwatch under_layer_stopwatch;
   private Stopwatch marshall_stopwatch;

   // Datagram identifier.
   protected Counter dgram_id = new Counter (0);

   // 16 bit version number
   protected static final int VERSION = 1;

   public void initObject (ObjEnv env)
   {
      // set environment.
      pdgram_fact = env.getProtDatagramX ();
      scheduler = env.getScheduler ();
      stats = env.getStatistics ();

      if(Debug.statsTime())
      {
         layer_stopwatch = new Stopwatch();
         under_layer_stopwatch = new Stopwatch();
         marshall_stopwatch = new Stopwatch(); 
      }
   }


   public void send (Datagram datagram, AlertID id) throws IOException
   {
      if(Debug.statsTime())
         layer_stopwatch.start();

      rawDef marsh_dgrm = marshallDatagram (datagram);
      sendPacket (datagram.getReceiver (), marsh_dgrm);
      
      if(Debug.statsTime())
      {
         layer_stopwatch.stop();
         stats.addDatagramLayerTime(layer_stopwatch);
      }
   }

   public Datagram pollDatagram () throws IOException
   {
      if(Debug.statsTime())
         layer_stopwatch.start();

      Peer [] sender_array = new Peer [1];

      rawDef marsh_dgrm = recvPacket (sender_array);
      if (marsh_dgrm == null)
         return null;

      Datagram dgram = unmarshallDatagram (marsh_dgrm);
      dgram.setSender (sender_array [0]);
      dgram.setReceiver (udp_peer);

      if(Debug.statsTime())
      {
         layer_stopwatch.stop();
         stats.addDatagramLayerTime(layer_stopwatch);
      }

      return dgram;
   }

   // EndPoint interface
   public void bindAddress (Peer local_peer) throws IOException
   {
      if (! (local_peer instanceof MyPeer))
         throw new WrongFactoryException ("unknown Peer");
      MyPeer local = (MyPeer) local_peer;

      StackedAddress sa = (StackedAddress) local.getAddress();

      InetAddress inet = InternetLib.getIp (sa);
      int port = InternetLib.getUdp (sa);

      bindAddress (inet, port);
   }

   public void bindAddress () throws IOException
   {
      bindAddress (null, -1);
   }

   /**
      Binds this object to the given address and port, and places the address
      and port in 'udp_peer'.
      <p>
      The caller may leave the address unspecified by passing null. In that
      case, bindAddress will use the
      'local ip' configuration property, or, if the property is not set,
      request a local ip address from the operating system.
      <p>
      Similarly, the caller may leave the port unspecified by passing -1. In
      that case, bindAddress will use the 'local port' configuration property,
      or, if the property is not set, request an available local port from the
      operating system.
   */
   protected void bindAddress (InetAddress inet, int port) throws IOException
   {
      // create udp_sock

      // first check whether we need to use the configuration properties
      if (inet == null) {
        String config_address = ConfigProperties.localIP();
        if (! config_address.equals (ConfigProperties.LOCAL_IP_DEF))
           inet = InetAddress.getByName (config_address);
      }

      if (port == -1) {
        String config_port = ConfigProperties.localPort();
        if (! config_port.equals (ConfigProperties.LOCAL_PORT_DEF))
           port = Integer.parseInt (config_port);
      }

      if (inet == null) {
         if (port == -1)
            udp_sock = new PassiveDatagramSocket (stats);
         else
            udp_sock = new PassiveDatagramSocket (port, stats);
      }
      else {
         if (port == -1) {
            // not my fault. blame java.net.DatagramSocket
            throw new IllegalArgumentException ("unspecified port with" +
                                                   " specified address");
         }
         else
            udp_sock = new PassiveDatagramSocket (port, inet, stats);
      }

      // create udp_peer
      // first set inet and port to udp_peer's intended values

      if (inet == null) {
         // Use the address allocated by the operating system. However
         // udp_sock.getLocalAddress() returns an invalid address if
         // inet==null, so don't use that.
         inet = InetAddress.getLocalHost ();
      }
      // else: leave as is

      if (port == -1) {
         // Use the port allocated by the operating system.
	 port = udp_sock.getLocalPort ();
      }
      // else: leave as is

      udp_peer = new MyPeer (new StackedAddressImpl (inet, port,
      				StackedAddressImpl.UDP_PORT));
   }

   public Peer getBoundAddress ()
   {
      return udp_peer;      // null if unbound
   }

   // Active interface
   public ActiveRID activate (int service, Notifiable note, ActiveUID userInfo)
   {
      throw new NotSupportedException ("activate() with service");
   }

   public ActiveRID activate (Notifiable note, ActiveUID userInfo)
   {
      if (udp_sock == null)
         throw new IllegalStateException ("not yet bound to an address");

      user_notifiable = note;
      user_cookie = userInfo;

      activator = new PassiveActivatorImpl (scheduler, stats);
      activator.activate (udp_sock, this, null);

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
         The PassiveActivator notifies when a UDP datagram is waiting, one
         callback with one event per callback.
         Each incoming UDP datagram denotes the arrival of a single datagramx
         datagram.
      */
      user_notifiable.notifyEvent (user_cookie);
   }

   // Non-public methods.

   /**
      Constructs a packet conforming to the protocol. The protocol input
      fields consist of the fields in 'dgram'.

      @param dgram      the datagram

      @exception IllegalArgumentException
         if one of the protocol input fields was invalid.
      @exception ProtocolException
         if an error occurred while encoding the protocol.
   */
   protected rawDef marshallDatagram (Datagram dgram) throws ProtocolException
   {
      if(Debug.statsTime())
         marshall_stopwatch.start();

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

      rawDef marshalled = pdgram.marshall ();

      if(Debug.statsTime())
      {
         marshall_stopwatch.stop();
         stats.addDatagramMarshallTime(marshall_stopwatch);
      }

      return marshalled;
   }

   /**
      Reconstructs a datagram from a packet conforming to the protocol.
      The sender and receiver fields are not filled in.

      @exception ProtocolException
           if an error occurred while decoding the protocol.
   */
   protected Datagram unmarshallDatagram (rawDef marsh_dgrm)
               throws ProtocolException
   {
      if(Debug.statsTime())
         marshall_stopwatch.start();

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

      if (pdgram.getFragmentNumber () != 0)
         throw new ProtocolException ("unexpected fragment number: " +
                                       pdgram.getFragmentNumber ());

      if (RawOps.sizeOfRaw (payload) != pdgram.getDatagramLength ())
         throw new ProtocolException ("payload length != header field: " +
                     RawOps.sizeOfRaw (payload) + "/" +
                     pdgram.getDatagramLength () + ", resp.");

      dgram = new DatagramImpl ();
      dgram.setPacket (payload);

      if(Debug.statsTime())
      {
         marshall_stopwatch.stop();
         stats.addDatagramMarshallTime(marshall_stopwatch);
      }

      return dgram;
   }

   /**
      Transmits a packet to the specified destination.
   */
   protected void sendPacket (Peer peer_dest, rawDef packet) throws IOException
   {
      if (! (peer_dest instanceof MyPeer))
         throw new WrongFactoryException ("unknown Peer");
      MyPeer peer = (MyPeer) peer_dest;

      StackedAddress sa = (StackedAddress) peer.getAddress ();

      InetAddress inet = InternetLib.getIp (sa);
      int port = InternetLib.getUdp (sa);

      if (inet == null || port == -1)
         throw new IllegalArgumentException ("partial UDP/IP address" + sa);

      DatagramPacket udp_packet = RawUtil.getRawUDP (packet, inet, port);

      stats.addDatagramSent (udp_packet.getLength());

      if(Debug.statsTime())
      {
         layer_stopwatch.stop();
         under_layer_stopwatch.start();
      }

      udp_sock.send (udp_packet);

      if(Debug.statsTime())
      {
         under_layer_stopwatch.stop();
         stats.addUnderlyingSendTime(under_layer_stopwatch);
         layer_stopwatch.resume();
      }

   }

   /**
      Tries to receive the first available packet without blocking. If no
      packet is available, then returns null, otherwise returns the packet.
      The sender of the packet is returned through 'sender'.
   */
   protected rawDef recvPacket (Peer [] sender) throws IOException
   {
      if(Debug.statsTime())
      {
         layer_stopwatch.stop();
         under_layer_stopwatch.start();
      }

      // Note udp_sock accepts packets without blocking.
      DatagramPacket udp_packet = udp_sock.receive ();
      
      if(Debug.statsTime())
      {
         under_layer_stopwatch.stop();
         stats.addUnderlyingReceiveTime(under_layer_stopwatch);
         layer_stopwatch.resume();
      }

      if (udp_packet == null)
         return null;

      // find the Peer for the remote end-point
      sender [0] = new MyPeer (new StackedAddressImpl (udp_packet.getAddress (),
                     udp_packet.getPort (), StackedAddressImpl.UDP_PORT));
      
      rawDef packet = RawOps.createRaw ();
      RawUtil.setRawUDP (packet, udp_packet);
      packet = RawOps.reallocRaw (packet);  // avoid excessive space occupation

      stats.addDatagramReceived (udp_packet.getLength());

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
