/*
 * jDonkey (c) 2002 by Kaspar Schleiser (casper at stammheim dot org)
 * Released under the GPL
 */
 
package donkey;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import javax.xml.transform.Source;

import donkey.util.*;

abstract class DonkeyConnectionSkeleton implements DonkeyConnection {
	public DonkeyConnectionSkeleton(SocketChannel channel, donkeyInfo dInfo, int rnd) {
		this.channel = channel;
		this.dInfo = dInfo;
		this.rnd = rnd;
		init();
	}

	public DonkeyConnectionSkeleton(donkeyInfo dInfo, int rnd) {
		this.dInfo = dInfo;
		this.rnd = rnd;
		init();
	}

	private void init() {
		dSendQueue = new LinkedList();
		dReceiveQueue = new LinkedList();
		inPackets = new decodeDonkeyPacketStream(this);
		connected = false;
		doClose = false;
		transferStart = System.currentTimeMillis();
		timeOut = (int) dInfo.getConnectionTimeOut();
		connectTimeOut = (int) dInfo.getConnectTimeOut();
		state = STATE_CONNECTING;

		sendSpeeds = new int[40];
		receiveSpeeds = new int[40];

		lastActivity = transferStart;
		heartBeat = transferStart;
	}

	protected int connectTimeOut;
	protected boolean connected;
	protected boolean doClose;
	protected boolean isOutbound;
	protected donkeyInfo dInfo;
	protected LinkedList dSendQueue;
	protected LinkedList dReceiveQueue;
	protected decodeDonkeyPacketStream inPackets;
	protected SocketChannel channel;
	protected long lastActivity;
	protected boolean waiting = false;
	protected int rnd = 0;
	protected boolean hasOutBuffer = false;
	protected ByteBuffer outBuffer;

	protected long totalBytesSent = 0;
	protected long totalBytesReceived = 0;

	protected int thisSecBytesSent = 0;
	protected int thisSecReceived = 0;

	protected long heartBeat = 0;
	protected long transferStart = 0;

	protected int actReceiveSpeed = 0;
	protected int actSendSpeed = 0;

	protected int timeOut;

	protected int state = 0;

	protected SelectionKey selectionKey;

	static final int STATE_UNKNOWN = 0;
	static final int STATE_CONNECTING = 10;
	static final int STATE_HANDSHAKING = 20;
	static final int STATE_CONNECTED = 30;
	static final int STATE_TRANSFERING = 40;
	static final int STATE_WAITING = 50;
	static final int STATE_ENQUEUE = 60;
	static final int STATE_UPLOADING = 70;
	static final int STATE_CLOSING = 70;

	public synchronized void addInPacket(donkeyPacket inPacket) {
		lastActivity = System.currentTimeMillis();
		dReceiveQueue.add(inPacket);
	}

	public synchronized void addOutPacket(donkeyPacket outPacket) {
		lastActivity = System.currentTimeMillis();
		dSendQueue.add(outPacket);
	}

	public synchronized boolean hasInput() {
		if (dReceiveQueue.size() > 0)
			return true;
		return false;
	}

	public synchronized boolean hasOutput() {
		nextFileBlock();
		if (dSendQueue.size() > 0) {
			return true;
		}
		return false;
	}

	public synchronized void processInput() {
	}

	public synchronized void process(int count) {
		if (doClose)
			close();
		if (getChannel().socket().isClosed()) {
			System.out.println(rnd + " " + (System.currentTimeMillis() - transferStart) + " socket closed!");
			close();
		}
			

		if (count % 10 == 0) {
			processInput();
		}
		if (count % 25 == 0)
			calcSpeed();
		if (count == 100)
			if (!waiting) {
				if ((System.currentTimeMillis() - lastActivity) > (timeOut * 1000)) {
					System.out.println(rnd + " Connection timeout. State: " + state);
					close();
				}
			}
	}

	public synchronized void processOutput() {
		while ((hasOutput() || hasOutBuffer()) && getChannel().isOpen()) {

			if (!hasOutBuffer()) {
				setOutBuffer(ByteBuffer.wrap(getNextOutPacket().getBytes()));
				setHasOutBuffer(true);
				removeOutPacket();
			}

			ByteBuffer buf = getOutBuffer();
			try {
				int nbytes = getChannel().write(buf);
				addSentBytesNum(nbytes);

				//				System.out.println(rnd + " sent " + nbytes + " bytes.");

				if (buf.remaining() == 0) {
					setHasOutBuffer(false);
					setOutBuffer(null);
				} else {
					//					System.out.println(rnd + " " + buf.remaining() + "bytes left in buffer.");
					break;
				}

			} catch (IOException e) {
				System.out.println(rnd + " Error sending...: " + e.getMessage());
				try {
					getChannel().close();
				} catch (IOException e2) {
				}
				doClose = true;
			}
		}
	}

	/**
	 * Method nextFileBlock.
	 */
	public void nextFileBlock() {
	}

	public synchronized donkeyPacket getNextPacket() {
		return ((donkeyPacket) dReceiveQueue.removeFirst());
	}

	public synchronized donkeyPacket getNextOutPacket() {
		return ((donkeyPacket) dSendQueue.getFirst());
	}

	public synchronized donkeyPacket removeOutPacket() {
		return ((donkeyPacket) dSendQueue.removeFirst());
	}

	public boolean isConnected() {
		return connected;
	}

	public boolean doClose() {
		return doClose;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public void close() {
		state = STATE_CLOSING;
		doClose = true;

		//		dSendQueue.clear();
		//		dReceiveQueue.clear();
		//		addOutPacket(CreateDonkeyPacket.command(convert.intToByte(0x57)));
		//		addOutPacket(CreateDonkeyPacket.command(convert.intToByte(0x57)));
		addOutPacket(CreateDonkeyPacket.command(convert.intToByte(0x56)));
		setConnected(false);
		thisSecBytesSent = 0;
		thisSecReceived = 0;
		try {
			getChannel().close();
		} catch (IOException e) {
			System.out.println(rnd + " Error closing channel: " + e.toString());
		}
	}

	public boolean isOutbound() {
		return isOutbound;
	}

	public void setOutbound(boolean isOutbound) {
		this.isOutbound = isOutbound;
	}

	public SocketChannel getChannel() {
		return channel;
	}

	public decodeDonkeyPacketStream getInPackets() {
		return inPackets;
	}

	public void setChannel(SocketChannel channel) {
		this.channel = channel;
	}

	/**
	 * Returns the lastActivity.
	 * @return long
	 */
	public long getLastActivity() {
		return lastActivity;
	}

	/**
	 * Sets the lastActivity.
	 * @param lastLastActivity The lastLastActivity to set
	 */
	public void setLastActivity(long lastActivity) {
		this.lastActivity = lastActivity;
	}

	/**
	 * @see donkey.DonkeyConnection#addReceivedBytesNum(int)
	 */
	public synchronized void addReceivedBytesNum(int nbytes) {
		thisSecReceived += nbytes;
		totalBytesReceived += nbytes;
	}

	/**
	 * @see donkey.DonkeyConnection#addSentBytesNum(int)
	 */
	public synchronized void addSentBytesNum(int nbytes) {
		thisSecBytesSent += nbytes;
		totalBytesSent += nbytes;
	}

	private int[] sendSpeeds;
	private int[] receiveSpeeds;
	int speedpos = 0;
	int numHeartBeats = 0;

	public void calcSpeed() {
		long currentTime = System.currentTimeMillis();
		long millis = (currentTime - heartBeat);
		if (millis > 0) {
			sendSpeeds[speedpos] = (int) ((thisSecBytesSent * 1000) / millis);
			receiveSpeeds[speedpos] = (int) ((thisSecReceived * 1000) / millis);
			thisSecBytesSent = 0;
			thisSecReceived = 0;
			heartBeat = currentTime;
			speedpos++;
			if (numHeartBeats < sendSpeeds.length)
				numHeartBeats++;
			if (speedpos == sendSpeeds.length - 1)
				speedpos = 0;
		}
	}

	private int clcspd(int[] speeds, int heartbeats) {

		int nbeats = heartbeats;

		if (nbeats > speeds.length)
			nbeats = speeds.length;
		if (nbeats > numHeartBeats)
			nbeats = numHeartBeats;

		int speed = 0;
		int pos = speedpos;
		for (int i = 0; i < nbeats; i++) {
			speed += speeds[pos - i];
			if (pos - i == 0)
				pos += speeds.length;
		}
		if (nbeats > 0)
			return speed / nbeats;
		else
			return 0;
	}
	/**
	 * Returns ReceiveSpeed.
	 * @return int
	 */
	public int getReceiveSpeed(int heartbeats) {
		return clcspd(receiveSpeeds, heartbeats);
	}

	/**
	 * Returns the sendSpeed.
	 * @return int
	 */
	public int getSendSpeed(int heartbeats) {
		return clcspd(sendSpeeds, heartbeats);
	}

	/**
	 * Returns the connectTimeOut.
	 * @return int
	 */
	public int getConnectTimeOut() {
		return connectTimeOut;
	}

	/**
	 * Sets the connectTimeOut.
	 * @param connectTimeOut The connectTimeOut to set
	 */
	public void setConnectTimeOut(int connectTimeOut) {
		this.connectTimeOut = connectTimeOut;
	}
	/**
	 * Returns the rnd.
	 * @return int
	 */
	public int getRnd() {
		return rnd;
	}

	/**
	 * @see donkey.DonkeyConnection#receiveQueueSize()
	 */
	public int receiveQueueSize() {
		return dReceiveQueue.size();
	}

	/**
	 * @see donkey.DonkeyConnection#sendQueueSize()
	 */
	public int sendQueueSize() {
		return dSendQueue.size();
	}

	/**
	 * @see donkey.DonkeyConnection#getState()
	 */
	public int getState() {
		return state;
	}

	/**
	 * Returns the outBuffer.
	 * @return ByteBuffer
	 */
	public ByteBuffer getOutBuffer() {
		return outBuffer;
	}

	/**
	 * Sets the outBuffer.
	 * @param outBuffer The outBuffer to set
	 */
	public void setOutBuffer(ByteBuffer outBuffer) {
		this.outBuffer = outBuffer;
	}

	/**
	 * Returns the hasOutBuffer.
	 * @return boolean
	 */
	public boolean hasOutBuffer() {
		return hasOutBuffer;
	}

	/**
	 * Sets the hasOutBuffer.
	 * @param hasOutBuffer The hasOutBuffer to set
	 */
	public void setHasOutBuffer(boolean hasOutBuffer) {
		this.hasOutBuffer = hasOutBuffer;
	}

	/**
	 * Returns the selectionKey.
	 * @return SelectionKey
	 */
	public SelectionKey getSelectionKey() {
		return selectionKey;
	}

	/**
	 * Sets the selectionKey.
	 * @param selectionKey The selectionKey to set
	 */
	public void setSelectionKey(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
	}

}
/********************************************************************************
 *
 * jMule - a Java massive parallel file sharing client
 *
 * Copyright (C) by the jMuleGroup ( see the CREDITS file )
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Id: DonkeyConnectionSkeleton.java,v 1.53 2004/01/03 12:51:07 emarant Exp $
 *
 ********************************************************************************/
package org.jmule.core.protocol.donkey;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.jmule.core.ConnectionManager;
import org.jmule.util.MiscUtil;
import org.jmule.util.Convert;

/** The base class of DonkeyClientConection
 * @version $Revision: 1.53 $
 * <br>Last changed by $Author: emarant $ on $Date: 2004/01/03 12:51:07 $
 */
public abstract class DonkeyConnectionSkeleton implements DonkeyConnection, DonkeyPacketConstants {
	final static Logger log = Logger.getLogger( DonkeyConnectionSkeleton.class.getName() );
    
	public DonkeyConnectionSkeleton( SocketChannel channel) {
		this.channel = channel;
        this.dContext = DonkeyProtocol.getInstance();
        init();
	}

	public DonkeyConnectionSkeleton() {
		this(null);
    }

    protected void initPacketDecoder(){
        packetDecoder = new DonkeyPacketReceiver(this);
    }
    
	private void init() {
		dSendQueue = new LinkedList();
		dReceiveQueue = new LinkedList();
		initPacketDecoder();
		connected = false; // how it differs from state >= STATE_CONNECTED ?
		doClose = false;
		transferStart = System.currentTimeMillis();
		
		connectTimeOut = 20; // FIXME: make configurable
		state = STATE_CONNECTING;

		sendSpeeds = new int[40];
		receiveSpeeds = new int[40];

		lastActivity = transferStart;
		heartBeat = transferStart;
        
        if (channel != null) {
            setPeerAddress((InetSocketAddress)channel.socket().getRemoteSocketAddress());
        }
        
	}
    /**in seconds*/
	protected int connectTimeOut;
	protected boolean connected;
	protected boolean doClose;
    private boolean closed = false;
	protected boolean isOutbound = false;
	protected DonkeyProtocol dContext;
	protected LinkedList dSendQueue;
	protected LinkedList dReceiveQueue;
	protected DonkeyPacketReceiver packetDecoder;
	protected SocketChannel channel;
	protected long lastActivity;
	protected boolean hasOutBuffer = false;
	protected ByteBuffer outBuffer;

	protected long totalBytesSent = 0;
	protected long totalBytesReceived = 0;

	protected int thisSecBytesSent = 0;
	protected int thisSecReceived = 0;

	protected long heartBeat = 0;
	protected long transferStart = 0;

	protected int actReceiveSpeed = 0;
	protected int actSendSpeed = 0;
    /**in seconds*/
   // protected int timeOut;

	

	protected SelectionKey selectionKey;

	protected InetSocketAddress peerAddress;

    protected int state = 0;

    // maybe it should be some binary field so isConnection() { return state | STATE_CONNECTED );
	static final int STATE_UNKNOWN = 0; // this will newer get used: the initial state is STATE_CONNECTING ( set in init() ), but this is obviosly misleading as there can pass some time between create/init()ialize the connection and actually the connect() try in the ConnectionManager
	static final int STATE_CONNECTING = 10;
	static final int STATE_HANDSHAKING = 20;
	static final int STATE_CONNECTED = 30;
	static final int STATE_TRANSFERING = 40; // this is opposite to STATE_UPLOADING ? if yes, then express it so: STATE_DOWNLOADING
	static final int STATE_WAITING = 50;
	static final int STATE_ENQUEUE = 60;
	static final int STATE_UPLOADING = 70;
	static final int STATE_CLOSING = 70;
    
    /* Some note at the status methods doClose(), isConnected(), setConnected() 
    
     the DonkeyConnectionSkeleteon should know about only one way how to manage the internal status. so the doClose() could be expressed as one of the Connection stati, the same
    would be true for isConnected() ( so at least the connected private boolean could be discarded ); the setState() could than check for some internal state transition consistency.
    
    
     - think about it: oo: no one knows about states, only some package clases, also we can up- and download same time or upload and be enqueued ...! 
    */
    
	protected int[] sendSpeeds;
	protected int[] receiveSpeeds;
	protected int speedpos = 0;
	protected int numHeartBeats = 0;
	protected boolean writeSelected = false;

	public void calcSpeed() {
		long currentTime = System.currentTimeMillis();
		long millis = (currentTime - heartBeat);
		if (millis > 0) {
			sendSpeeds[speedpos] = (int) ((thisSecBytesSent * MiscUtil.TU_MillisPerSecond ) / millis);
			receiveSpeeds[speedpos] = (int) ((thisSecReceived * MiscUtil.TU_MillisPerSecond ) / millis);
			thisSecBytesSent = 0;
			thisSecReceived = 0;
			heartBeat = currentTime;
			speedpos++;
			if (numHeartBeats < sendSpeeds.length)
				numHeartBeats++;
			if (speedpos == sendSpeeds.length - 1)
				speedpos = 0;
		}
	}

    // clcspd == calculateSpeed( ? )
	private int clcspd(int[] speeds, int heartbeats) {

		int nbeats = heartbeats;

		if (nbeats > speeds.length)
			nbeats = speeds.length;
		if (nbeats > numHeartBeats)
			nbeats = numHeartBeats;

		int speed = 0;
		int pos = speedpos;
		for (int i = 0; i < nbeats; i++) {
			speed += speeds[pos - i];
			if (pos - i == 0)
				pos += speeds.length;
		}
		if (nbeats > 0)
			return speed / nbeats;
		else
			return 0;
	}

	public synchronized void addInPacket(DonkeyPacket inPacket) {
        if (log.isLoggable(Level.FINEST)) {
            log.finest(getConnectionNumber()+" received packet " + Convert.byteBufferToHexString(inPacket.getBuffer(), inPacket.getBuffer().position(),  Math.min(128, inPacket.getBuffer().remaining()) ));
        }
		dReceiveQueue.add(inPacket);
        processIncomingPacket();
	}

	public synchronized void addOutPacket(DonkeyPacket outPacket) {
        if (log.isLoggable(Level.FINEST)) {
            log.finest(getConnectionNumber()+" sending packet " + Convert.byteBufferToHexString(outPacket.getBuffer(), outPacket.getBuffer().position(), Math.min(128, outPacket.getBuffer().remaining()) ));
        }
		lastActivity = System.currentTimeMillis();
		dSendQueue.add(outPacket);
        //log.finest(writeOn+" "+writeOff);
        if (!writeOff&&selectionKey!=null&&!doClose) {
            if (getChannel().socket().isClosed()) {
                log.log(Level.FINE, getConnectionNumber()+" channel closed! ", new Exception());
            } else {
                try{
                    selectionKey.interestOps(SelectionKey.OP_WRITE | selectionKey.interestOps());
                }catch(java.nio.channels.CancelledKeyException cke){
                    log.log(Level.WARNING, getConnectionNumber()+" unexpected exception: ", cke);
                }
            }
        }
		
	}
    public int getPreferedReceivingBufferSize(){
        return 20480;
    }
    public boolean prepareOutput(){
        DonkeyUpLoadLimiter.Limiter.incConnections();
        return true;
    }
    public boolean prepareInput(){
        DonkeyDownLoadLimiter.Limiter.incConnections();
        return true;
    }

	public synchronized boolean hasInput() {
		return dReceiveQueue.size() > 0;
	}
    
	public synchronized boolean hasOutput() {
		return ((dSendQueue.size() > 0) || hasOutBuffer());
	}
    
	public synchronized void processIncomingPacket() {
	}
    
    // how about replace the int count by long currentTime, so this parameter become some meaning and we save some calls to a OS method.
	public boolean check(int count) {
		if (doClose)
			close();
		if (getChannel().socket().isClosed()) {
			log.log(Level.FINEST, getConnectionNumber() + " " + (System.currentTimeMillis() - transferStart) + " socket closed!",new Exception());
			close();
			return false;
		}
		if ((count << 1) % 5 == 0) {
			calcSpeed();
            if (count % 20 == 0) {
                if ((System.currentTimeMillis() - lastActivity) > (getTimeOut() * MiscUtil.TU_MillisPerSecond )) {
                    log.finest(getConnectionNumber() + " Connection timeout. State: " + state);
                    close();
                    return false;
                }
            }
        }
        /*
		if(hasOutput())
			selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		else
			selectionKey.interestOps(SelectionKey.OP_READ);
		*/
        //log.finest(writeOn+" "+writeOff+" "+hasOutput());
        if(readOn){
            readOn = false;
            log.fine(getConnectionNumber() + " read on "+ ConnectionManager.getInstance().getLoopCount());
            selectionKey.interestOps(SelectionKey.OP_READ | selectionKey.interestOps());
        }
        if(writeOn && hasOutput()){
            writeOn = false;
            //log.fine(getConnectionNumber() + " write on " + ConnectionManager.getInstance().getLoopCount());
            selectionKey.interestOps(SelectionKey.OP_WRITE | selectionKey.interestOps());
        }
		return (true);
	}
    private long dataOutTraffic;
    private long dataInTraffic;
    private long otherInTraffic;
    private long otherOutTraffic;
    static final int PACKETHEADER_TRAFIC = 0x10000;
    static final boolean DIRECTION_IN = true;
    static final boolean DIRECTION_OUT = false;
    public long getStatistic(int opcode, boolean direction){
        switch(opcode){
            case OP_SENDINGPART: return DIRECTION_IN?dataInTraffic:dataOutTraffic;
        }
        return DIRECTION_IN?otherInTraffic:otherOutTraffic;
    }
    
    public void addStatistic(int opcode, boolean direction, int amount){
        if (direction) switch(opcode) {
            case OP_SENDINGPART:{
                dataInTraffic += amount;
                break;
            }
            default:{
                log.fine(getConnectionNumber()+" other in traffictype "+opcode);
                otherInTraffic += amount;
            }
        } else switch(opcode) {
            case OP_SENDINGPART:{
                dataOutTraffic += amount;
                break;
            }
            default:{
                log.fine(getConnectionNumber()+" other out traffictype "+opcode);
                otherOutTraffic += amount;
            }
        }
    }
    private DonkeyPacket outPacket = null;
    
	private long startOfStartuploadReq = 0;
	public static long callCounterOut = 0;
	public static long callCounterIn = 0;
    int currentOpcode = 0;
	public synchronized void processOutput() {
        callCounterOut++;
        //log.fine("nn "+getConnectionNumber());
        DonkeyUpLoadLimiter.Limiter.decConnections();
        int sendBytesInWhile = DonkeyUpLoadLimiter.Limiter.getMaxBytesPerConnection();
        writeOff = sendBytesInWhile==0;
        
		while ((hasOutput() || hasOutBuffer()) && getChannel().isOpen() && 
                 sendBytesInWhile>0 && !writeOff) {
            
			if (!hasOutBuffer()) {
                
                outPacket = getNextOutPacket();
                currentOpcode = outPacket.getBuffer().remaining()>5?outPacket.getCommandId():0;
				setOutBuffer(outPacket.getBuffer());
				setHasOutBuffer(true);
				removeOutPacket();
			}
            
			ByteBuffer buf = getOutBuffer();
            int limit = buf.limit();
            int maxwrite = buf.position() + sendBytesInWhile;
            buf.limit(maxwrite>limit?limit:maxwrite);
			try {
				int nbytes = getChannel().write(buf);
                sendBytesInWhile -= nbytes;
                if (nbytes>0) {
                    lastActivity = System.currentTimeMillis();
                    addStatistic(currentOpcode, DIRECTION_OUT, nbytes);
                }
				addSentBytesNum(nbytes);
                
				//log.finer(getConnectionNumber() + " sent " + nbytes + " bytes.");
                buf.limit(limit);
                
				if (buf.remaining() == 0) {
                    log.finest(getConnectionNumber() + " sent " + Convert.byteToHex(outPacket.getCommandId()));
					
					setHasOutBuffer(false);
					setOutBuffer(null);
                    outPacket.disposePacketByteBuffer();
				} else {
	//				log.finer(getConnectionNumber() + " " + buf.remaining() + " bytes left in buffer.");
                    
					break;
				}

			} catch (IOException e) {
				log.fine(getConnectionNumber() + " Error sending...: " + e.getMessage());
				try {
					getChannel().close();
				} catch (IOException e2) {
				}
				doClose = true;
                return;// don't set keys!
			}
		}
        if (hasOutput()) {
            if (writeOff) {
                //log.fine(getConnectionNumber() + " write off "+ ConnectionManager.getInstance().getLoopCount());
                selectionKey.interestOps(SELECTION_MASK_WRITE_OFF & selectionKey.interestOps());
                writeOff = false; // whats this ???!!!
                writeOn = true;
            }
        } else {
            if (writeOff) {
                writeOn = true;
            }
            selectionKey.interestOps(SELECTION_MASK_WRITE_OFF & selectionKey.interestOps());
        }
        
	}
    
    private boolean readOn = false;
    private boolean readOff = false;
    private boolean writeOn = false;
    private boolean writeOff = false;
    private static final int SELECTION_MASK_READ_OFF = ~SelectionKey.OP_READ;
    private static final int SELECTION_MASK_WRITE_OFF = ~SelectionKey.OP_WRITE;
    
    public boolean processInput() throws IOException  {
        if (doClose)
            return !doClose;
        callCounterIn++;
        DonkeyDownLoadLimiter.Limiter.decConnections();
        int limit = DonkeyDownLoadLimiter.Limiter.getMaxBytesPerConnection();
        if (limit==0){
             log.fine(getConnectionNumber() + " read off "+ ConnectionManager.getInstance().getLoopCount());
                selectionKey.interestOps(SELECTION_MASK_READ_OFF & selectionKey.interestOps());
                readOff = false;  // whats this ???!!!
                readOn = true;
                return !doClose;
        }
        int num = packetDecoder.append(limit);
        if (num>0) {
              lastActivity = System.currentTimeMillis();
        } else if (num==0) { // kill connection to out-of-band data sender!
            doClose = true;
            return !doClose;
        }
        addReceivedBytesNum(num);
        log.finest(getConnectionNumber() + " bytes read: "+num+" "+limit+" "+readOff+" "+readOn+(num>0?"":" "+System.currentTimeMillis()+" "+ConnectionManager.getInstance().getLoopCount()));
        if (readOff && !doClose) {
             log.fine(getConnectionNumber() + " read off "+ ConnectionManager.getInstance().getLoopCount());
                selectionKey.interestOps(SELECTION_MASK_READ_OFF & selectionKey.interestOps());
                readOff = false;  // whats this ???!!!
                readOn = true;
                
        }      
        return !doClose;
    }

    /** Provide timeout for this connection.
     * @return timeout in seconds.
     */
    abstract protected int getTimeOut();
    
	public synchronized DonkeyPacket getNextPacket() {
		return ((DonkeyPacket) dReceiveQueue.removeFirst());
	}

	public synchronized DonkeyPacket getNextOutPacket() {
		return ((DonkeyPacket) dSendQueue.getFirst());
	}

	public synchronized DonkeyPacket removeOutPacket() {
		return ((DonkeyPacket) dSendQueue.removeFirst());
	}

	public boolean isConnected() {
		return connected;
	}

	public boolean doClose() {
		return doClose;
	}

	public void setConnected(boolean connected) {
        if (connected&&state==STATE_CONNECTING) {
            state = STATE_CONNECTED;
            setLastActivity(System.currentTimeMillis());
        }
		this.connected = connected;
	}

    // 
	public synchronized void close() {
        if (closed) {
            return;
        }
		state = STATE_CLOSING;
		closed = true;
        doClose = true;
        log.fine("close "+getConnectionNumber()+" packets in send queue: "+dSendQueue.size()+" packets in receive queue: "+dReceiveQueue.size());
        while(!dSendQueue.isEmpty()){
            removeOutPacket().disposePacketByteBuffer();
        }
        while(!dReceiveQueue.isEmpty()){
            getNextPacket().disposePacketByteBuffer();
        }
		this.connected = false;
		thisSecBytesSent = 0;
		thisSecReceived = 0;
		try {
			getChannel().close();
		} catch (IOException e) {
			log.warning(getConnectionNumber() + " Error closing channel: " + e.toString());
		}
		packetDecoder.cleanup();
	}

	public boolean isOutbound() {
		return isOutbound;
	}
    	public void setOutbound(boolean isOutbound) {
		this.isOutbound = isOutbound;
	}

	public SocketChannel getChannel() {
		return channel;
	}

	public DonkeyPacketReceiver getPacketDecoder() {
		return packetDecoder;
	}

	public void setChannel(SocketChannel channel) {
		this.channel = channel;
	}

	/**
	 * Returns the lastActivity.
	 * @return long
	 */
	public long getLastActivity() {
		return lastActivity;
	}

	/**
	 * Sets the lastActivity.
	 * @param lastActivity The lastActivity to set
	 */
	public void setLastActivity(long lastActivity) {
		this.lastActivity = lastActivity;
	}

	/**
	 * @see org.jmule.core.protocol.donkey.DonkeyConnection#addReceivedBytesNum(int)
	 */
	public void addReceivedBytesNum(int nbytes) {
        ConnectionManager.getInstance().addBytesRecievedfromInternet(nbytes);
        readOff = !DonkeyDownLoadLimiter.Limiter.canGoOnWithTransfer(nbytes);
		thisSecReceived += nbytes;
		totalBytesReceived += nbytes;
	}

	/**
	 * @see org.jmule.core.protocol.donkey.DonkeyConnection#addSentBytesNum(int)
	 */
	public void addSentBytesNum(int nbytes) {
        ConnectionManager.getInstance().addBytesSendtoInternet(nbytes);
        writeOff = (!DonkeyUpLoadLimiter.Limiter.canGoOnWithTransfer(nbytes));
		thisSecBytesSent += nbytes;
		totalBytesSent += nbytes;
	}

	/**
	 * Returns ReceiveSpeed.
	 * @return int
	 */
	public int getReceiveSpeed(int heartbeats) {
		return clcspd(receiveSpeeds, heartbeats);
	}

	/**
	 * Returns the sendSpeed.
	 * @return int
	 */
	public int getSendSpeed(int heartbeats) {
		return clcspd(sendSpeeds, heartbeats);
	}

	/**
	 * Returns the connectTimeOut.
	 * @return int
	 */
	public int getConnectTimeoutAt() {
		return connectTimeOut;
	}

	/**
	 * Sets the connectTimeOut.
	 * @param connectTimeOut The connectTimeOut to set
	 */
	public void setConnectTimeOut(int connectTimeOut) {
		this.connectTimeOut = connectTimeOut;
	}
	/**
	 * Returns the getConnectionNumber().
	 * @return int an unique value to identify this connection 
	 */
	public int getConnectionNumber() {
		return this.hashCode();
	}

	/**
	 * @see org.jmule.core.protocol.donkey.DonkeyConnection#receiveQueueSize()
	 */
	public int receiveQueueSize() {
		return dReceiveQueue.size();
	}

	/**
	 * @see org.jmule.core.protocol.donkey.DonkeyConnection#sendQueueSize()
	 */
	public int sendQueueSize() {
		return dSendQueue.size();
	}

	/**
	 * @see org.jmule.core.protocol.donkey.DonkeyConnection#getState()
	 */
	public int getState() {
		return state;
	}

	/**
	 * Returns the outBuffer.
	 * @return ByteBuffer
	 */
	public ByteBuffer getOutBuffer() {
		return outBuffer;
	}

	/**
	 * Sets the outBuffer.
	 * @param outBuffer The outBuffer to set
	 */
	public void setOutBuffer(ByteBuffer outBuffer) {
		this.outBuffer = outBuffer;
	}

	/**
	 * Returns the hasOutBuffer.
	 * @return boolean
	 */
	public boolean hasOutBuffer() {
		return hasOutBuffer;
	}

	/**
	 * Sets the hasOutBuffer.
	 * @param hasOutBuffer The hasOutBuffer to set
	 */
	public void setHasOutBuffer(boolean hasOutBuffer) {
		this.hasOutBuffer = hasOutBuffer;
	}

	/**
	 * Returns the selectionKey.
	 * @return SelectionKey
	 */
	public SelectionKey getSelectionKey() {
		return selectionKey;
	}

	/**
	 * Sets the selectionKey.
	 * @param selectionKey The selectionKey to set
	 */
	public void setSelectionKey(SelectionKey selectionKey) {
        assert this.selectionKey==null;
		this.selectionKey = selectionKey;
        if (hasOutput()) {
            this.selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
	}

	/**
	 * Returns the peerAddress.
	 * @return InetSocketAddress
	 */
	public InetSocketAddress getPeerAddress() {
		return peerAddress;
	}

	/**
	 * Sets the peerAddress.
	 * @param peerAddress The peerAddress to set
	 */
	public void setPeerAddress(InetSocketAddress peerAddress) {
		this.peerAddress = peerAddress;
	}

}
