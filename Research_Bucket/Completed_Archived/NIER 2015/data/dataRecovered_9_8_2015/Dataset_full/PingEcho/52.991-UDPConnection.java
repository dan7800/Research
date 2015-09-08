package org.jmule.core.protocol.donkey.search;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.omg.CORBA.ByteHolder;
import org.jmule.core.protocol.donkey.DonkeyServer;
import org.jmule.nio.ByteBufferHelper;
import org.jmule.nio.channels.Connection;
import org.jmule.nio.channels.TimedSelectionKey;

/**
 * @author carlo
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 * 
 * ToDo: 
 * - combine with ServerConnection
 * - distill the protocol to another class
 */
class UDPConnection implements Connection, DonkeyPacketConstants {
	private static Logger log = Logger.getLogger(UDPConnection.class.getName());
	
	//private Application		app; jDonkey legacy
	private DonkeySearchEngine	engine;
	private Selector			selector; // pre-jDonkey returns :-)
	
	private DatagramChannel channel;
	
	private Queue	queue;
	
	private long last = 0;
	private double burst = 100; // bytes/sec
	private double bandwidth = 200;
	private double score = burst;
	
	private long numPacketsSend = 0;
	private long numPacketsReceived = 0;
	
	private static class QueueItem
	{
		SocketAddress address;
		ByteBuffer buf;
	}
	
	public UDPConnection(DonkeySearchEngine engine, Selector selector)
		throws IOException
	{
		//this.app = app; legacy
		this.engine = engine;
		this.selector = selector;
		this.queue = new Queue();
		
		//Selector selector = app.getSelector();
		SelectorProvider provider = selector.provider();
		
		this.channel = provider.openDatagramChannel();
		InetSocketAddress addr = new InetSocketAddress(10000);
		channel.socket().bind(addr);
		channel.socket().setTrafficClass(0x04);
		//log.info("receiveBufferSize = " + channel.socket().getReceiveBufferSize());
		channel.socket().setReceiveBufferSize(64 * 1024);
		//log.info("receiveBufferSize = " + channel.socket().getReceiveBufferSize());
		
		channel.configureBlocking(false);
		
		register(SelectionKey.OP_READ);
	}
	
	// ToDo: Wolf: why isn't this synchronized?
	private void add(SocketAddress address, ByteBuffer buf)
		throws ClosedChannelException
	{
		assert buf.remaining() > 0;
		
		QueueItem item = new QueueItem();
		item.address = address;
		item.buf = buf;
		
		//log.finest("adding item");
		
		queue.add(item);
		
		if(getScore() < buf.capacity())
		{
			// ToDo:
			log.warning("not enough score to output!");
			register(SelectionKey.OP_WRITE | SelectionKey.OP_READ);	
		}
		else
			register(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
		
		// ToDo: Wolf: needed for DatagramChannel's ?
		if(queue.size() == 1)
			getSelector().wakeup();
	}
	
	/* jDonkey
	private void add(Server server, ByteBuffer buf)
		throws ClosedChannelException
	{
		add(new InetSocketAddress(server.getSocketAddress().getAddress(), server.getSocketAddress().getPort() + 4), buf);
	}
	*/
	
	/* jMule */
	private final void add(DonkeyServer server, ByteBuffer buf)
		throws ClosedChannelException
	{
		add(new InetSocketAddress(server.getSocketAddress().getAddress(), server.getSocketAddress().getPort() + 4), buf);
	}
	
	private double getScore()
	{
		long now = System.currentTimeMillis();
		double diff = now - last;
		score = Math.min(score + (diff * bandwidth) / 1000, burst);
		//log.finest("score = " + score);
		last = now;
		return score;
	}
	
	private Selector getSelector()
	{
		//return app.getSelector();
		return this.selector;
	}
	
	/**
	 * @see wolf.jdonkey.Connection#process(SelectionKey)
	 */
	public void process(SelectionKey k) throws IOException {
		TimedSelectionKey key = (TimedSelectionKey) k;
		if(key.isReadable())
		{
			readPacket();
		}
		// ToDo: better selection keys (with timeout)
		if(key.isWritable() || key.isTimedout())
		{
			writeQueue();
		}
		// Wolf: this makes me puke, should be better structured
		if(key.isTimedout())
		{
			if(!queue.isEmpty())
				register(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		}
		/*
		if(!queue.isEmpty())
			writeQueue();
		*/
	}

	private void processSearchResult(ByteBuffer buf)
		throws UnknownHostException
	{
		/* Old
		byte hash[] = new byte[16];
		buf.get(hash);
		byte ipaddress[] = new byte[4];
		buf.get(ipaddress); // ToDo: check
		if(log.isLoggable(Level.FINEST))
		{
			log.finest("hash = " + toHexString(hash));
			log.finest("ipaddress = " + Inet4Address.getByAddress(ipaddress));
		}
		short port = buf.getShort();
		// ToDo: uint32
		long numTags = buf.getInt();
		log.finest("numTags = " + numTags);
		for(int j = 0; j < numTags; j++)
		{
			Tag tag = new Tag(buf);
			log.finest("tag ident = " + tag.getIdent());
			if(tag.getIdent() == TagFactory.FT_Filename)
			{
				log.finest(tag.getStringValue());
			}
		}
		*/
		SearchResult result = new SearchResult(buf);
		
		// ToDo: pending investigation
		//app.addSearchResult(result);
		log.fine("result = " + result);
		engine.foundResult(result);
	}
	
	private void processFoundSources(InetSocketAddress udpServerAddr, ByteBuffer buf)
		throws UnknownHostException
	{
		InetSocketAddress server = new InetSocketAddress(udpServerAddr.getAddress(), udpServerAddr.getPort() - 4);
		byte hash[] = new byte[16];
		buf.get(hash);
		int numSources = ByteBufferHelper.getUnsignedByte(buf);
		//log.finest("numSources = " + numSources);
		for(int i = 0; i < numSources; i++)
		{
			InetSocketAddress client = ByteBufferHelper.getInetSocketAddress(buf);
			//long userId = ByteBufferHelper.getUnsignedInt(buf);
			//int port = ByteBufferHelper.getUnsignedShort(buf);
			//log.finest("userId = " + userId + ", port = " + port);
			engine.foundSource(hash, client, server);
		}
		assert buf.remaining() == 0 : "remaining = " + buf.remaining();
	}
	
	/**
	 * Method readPacket.
	 */
	private void readPacket() 
		throws IOException
	{
		// ToDo:
		ByteBuffer buf = ByteBuffer.allocateDirect(5000).order(ByteOrder.LITTLE_ENDIAN);
		
		InetSocketAddress addr = (InetSocketAddress) channel.receive(buf);
		
		// ToDo:
		if(addr == null) return;
		assert addr != null;
		
		numPacketsReceived++;
		log.finest("received packet on UDP (numPacketsReceived = " + numPacketsReceived + ")");
		
		buf.flip();
		
		byte protocol = buf.get();
		if(protocol != OP_EDONKEYHEADER)
		{
			log.warning("ignoring bogus UDP packet (protocol = 0x" + Integer.toHexString(protocol) + ")");
			return;
		}
		
		byte opcode = buf.get();
		switch(opcode)
		{
			case OP_GLOBFOUNDSOURCES:
				processFoundSources(addr, buf);
				break;
			case OP_GLOBSEARCHRESULT:
				processSearchResult(buf);
				break;
			/*
			 * ToDo: there is not enough functionality visible in jMule to support this
			case OP_GLOBSERVERSTATUS:
				//int time = buf.getInt();
				//byte ipaddress[] = new byte[4];
				//buf.get(ipaddress);
				//InetAddress a = Inet4Address.getByAddress(ipaddress);
				long then = buf.getInt();
				int ping = (int) (System.currentTimeMillis() - then);
				int numUsers = buf.getInt();
				int numFiles = buf.getInt();
				//log.finest("a = " + a + ", numUsers = " + numUsers + ", numFiles = " + numFiles);
				
				// ToDo: this is just weird, but needed for communicating with a LAN server :-)
				//ServerList list = ServerList.getInstance();
				ServerList list = app.getServerList();
				SocketAddress findAddress = new InetSocketAddress(addr.getAddress(), addr.getPort() - 4);
				Server server = list.findBySocketAddress(findAddress);
				if(server != null)
				{
					server.setPing(ping);
					server.setStatus(numUsers, numFiles);
				}
				else
					log.warning("server " + findAddress + " not found");
				break;
			*/
			default:
				log.warning("unknown opcode = 0x" + Integer.toHexString(opcode));
				break;
		}
		if(buf.remaining() > 0)
			log.finest("remaining = " + buf.remaining());
	}

	protected void register(int ops)
		throws ClosedChannelException
	{
		channel.register(getSelector(), ops, this);
	}
	
	protected void register(int ops, long when)
		throws ClosedChannelException
	{
		TimedSelectionKey key = (TimedSelectionKey) channel.register(getSelector(), ops, this);
		//log.finest("key = " + key);
		// ToDo: ! Q&D
		if(when > 0)
		{
			key.interestOps(ops | TimedSelectionKey.OP_TIMEOUT);
			key.setTimeout(when);
		}
	}
	
	public void requestSources(DonkeyServer server, byte hash[])
		throws ClosedChannelException
	{
		assert hash.length == 16;
		
		ByteBuffer buf = ByteBuffer.allocateDirect(2 + 16).order(ByteOrder.LITTLE_ENDIAN);
		
		buf.put(OP_EDONKEYHEADER);
		buf.put(OP_GLOBGETSOURCES);
		buf.put(hash);
		
		buf.flip();
		
		add(server, buf);
	}
	
	public void requestStatus(DonkeyServer server)
		throws ClosedChannelException
	{
		ByteBuffer buf = ByteBuffer.allocateDirect(6).order(ByteOrder.LITTLE_ENDIAN);
		
		buf.put(OP_EDONKEYHEADER);
		buf.put(OP_GLOBSERVERSTATUSREQ);
		
		InetSocketAddress dest = new InetSocketAddress(server.getSocketAddress().getAddress(), server.getSocketAddress().getPort() + 4);
		
		// 4 bytes extra data
		buf.put(dest.getAddress().getAddress());
		// ToDo: ehr, the timestamp is put in right before we write.
		// The address is never received :-)
		
		buf.flip();
		
		add(server, buf);
	}
	
	/**
	 * @deprecated
	 */
	public void requestStatus(SocketAddress address)
		throws ClosedChannelException
	{
		ByteBuffer buf = ByteBuffer.allocateDirect(6).order(ByteOrder.LITTLE_ENDIAN);
		
		buf.put(OP_EDONKEYHEADER);
		buf.put(OP_GLOBSERVERSTATUSREQ);
		//buf.putInt(111);
		buf.put(((InetSocketAddress) address).getAddress().getAddress());
		
		buf.flip();
		
		add(address, buf);
	}
	
	public void search(DonkeyServer server, String query)
		throws ClosedChannelException
	{
		ByteBuffer buf = ByteBuffer.allocateDirect(5 + query.length()).order(ByteOrder.LITTLE_ENDIAN);
		
		buf.put(OP_EDONKEYHEADER);
		//buf.putInt(buf.capacity() - 5);
		buf.put(OP_GLOBSEARCHREQUEST);
		
		buf.put((byte) 0x01);
		buf.putShort((short) query.length());
		buf.put(query.getBytes());
		
		buf.flip();

		add(server, buf);
	}
	
	/**
	 * Method writeQueue.
	 */
	private void writeQueue() 
		throws IOException
	{
		assert !queue.isEmpty();
		
		// ToDo: optimize by writing multiple buffers
		QueueItem item = (QueueItem) queue.peek();
		
		//log.finest("sending UDP packet to " + item.address);
		
		double d = getScore() - item.buf.capacity();
		if(d < 0)
		{
			long millis = (long) (-d / bandwidth * 1000.0);
			register(SelectionKey.OP_READ, System.currentTimeMillis() + millis);
			log.finer("not enough score to send (d = " + d + ", millis = " + millis + ")");
			return;
		}
		
		// ToDo: not Q&D
		if(item.buf.get(1) == OP_GLOBSERVERSTATUSREQ)
		{
			//log.finest("is server status");
			item.buf.putInt(2, (int) System.currentTimeMillis());
		}
		
		int numSend = channel.send(item.buf, item.address);
		numPacketsSend++;
		
		// ToDo:
		int MAC_HEADER_SIZE = 0; // unknown
		// but then again, it doesn't count against because that's max bandwidth on
		// ip level (most of the time :-) )
		int IP_HEADER_SIZE = 20; // about
		int UDP_HEADER_SIZE = 8;
		
		score -= (numSend + MAC_HEADER_SIZE + IP_HEADER_SIZE + UDP_HEADER_SIZE);
		
		if(!item.buf.hasRemaining())
			queue.pop();
		
		// ToDo: selection key ops synch
		if(queue.isEmpty())
		{
			log.finest("UDP queue is empty (numPacketsSend = " + numPacketsSend + ")");
			register(SelectionKey.OP_READ);
		}
	}
}
