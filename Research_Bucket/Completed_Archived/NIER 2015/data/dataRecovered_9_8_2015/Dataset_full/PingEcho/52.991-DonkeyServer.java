/*
 * jDonkey (c) 2002 by Kaspar Schleiser (casper at stammheim dot org)
 * Released under the GPL
 */
 
package donkey;

import java.net.*;

/**
 * @author kresh
 *
 * Copyright by Kaspar Schleiser (2002)
 */
public class DonkeyServer {
	public DonkeyServer(InetSocketAddress address) {
		this.socketAddress = address;
	}

	private String name = "no name";
	private String description = "no description";
	//	private int ping=0;
	private int files = 0;
	private int users = 0;
	private int priority = 0;
	private boolean staticDns = false;

	private InetSocketAddress socketAddress;
	/**
	 * Returns the description.
	 * @return String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the files.
	 * @return int
	 */
	public int getFiles() {
		return files;
	}

	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the socketAddress.
	 * @return InetSocketAddress
	 */
	public InetSocketAddress getSocketAddress() {
		return socketAddress;
	}

	/**
	 * Returns the users.
	 * @return int
	 */
	public int getUsers() {
		return users;
	}

	/**
	 * Sets the description.
	 * @param description The description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets the files.
	 * @param files The files to set
	 */
	public void setFiles(int files) {
		this.files = files;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the users.
	 * @param users The users to set
	 */
	public void setUsers(int users) {
		this.users = users;
	}

	/**
	 * Returns the priority.
	 * @return int
	 * 	0 = high
	 *	1 = normal
	 *	2= low
	 * 
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Sets the priority.
	 * @param priority The priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * Returns the staticDns.
	 * @return boolean
	 */
	public boolean isStaticDns() {
		return staticDns;
	}

	/**
	 * Sets the staticDns.
	 * @param staticDns The staticDns to set
	 */
	public void setStaticDns(boolean staticDns) {
		this.staticDns = staticDns;
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object arg0) {
		if (arg0 instanceof DonkeyServer) {
			DonkeyServer server = (DonkeyServer) arg0;
			if (server.getSocketAddress().equals(socketAddress))
				return true;
			else
				if (staticDns)
					if (server.getName().equals(name)) {
						return true;
					}

		}
		return false;
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
 * $Id: DonkeyServer.java,v 1.22 2003/12/12 02:55:55 emarant Exp $
 *
 ********************************************************************************/
package org.jmule.core.protocol.donkey;

import java.nio.ByteBuffer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jmule.core.SearchQuery;
import org.jmule.core.ConnectionManager;
import org.jmule.util.Convert;
import org.jmule.util.MiscUtil;

/** This class represents a ed2k server.
 * @author casper
 * @version $Revision: 1.22 $
 * <br>Last changed by $Author: emarant $ on $Date: 2003/12/12 02:55:55 $
 */
public class DonkeyServer implements org.jmule.util.XMLnode {
    final static Logger log = Logger.getLogger(DonkeyServer.class.getName());
    /**
    * Want to filter server with private ip's form public serverlists?
    * Uses system property org.jmule.core.protocol.donkey.FILTER_NON_PUBLIC_SERVER to set value at runtime - default value is true.
    */
    private static boolean filterNonPublicServer =
        MiscUtil.getBoolProperty("org.jmule.core.protocol.donkey.FILTER_NON_PUBLIC_SERVER", true);

    public DonkeyServer(InetSocketAddress address) {
        this.socketAddress = address;
    }

    protected void setServerLisServer(DonkeyServerList dsl) {
        this.dsl = dsl;
    }

    private DonkeyServerList dsl;

    private String name = "no name";
    private String description = "no description";
    private String staticDnsName = "";

    private int files = 0;
    private int softLimitForFiles = 0;
    private int hardLimitForFiles = 0;
    private int users = 0;
    private String message = "";
    private int state = 0;
    private int maxUsers = 0;
    private int priority = 0;
    private long lasttimeknown = System.currentTimeMillis();
    private long lastresolvetry = 0;
    private boolean staticDns = false; // for most servers from net we don't know if they have a static dns
    private boolean immortal = false; // all servers from net are *mortals*
    private boolean autoConnectAllowed = true; //server can be used for autoconnect
    private boolean TCPEmuleCompression = false;
    private boolean UDPExtendedGetSources = false;
    private boolean UDPExtendedGetFiles = false;
    private LinkedList sourceSearchList = new LinkedList();
    private LinkedList packetList = new LinkedList();
    private boolean requestServerInfo = false;
    private boolean requestServerStatus = false;
    private boolean sendreservation = false;

    private InetSocketAddress socketAddress;

    private void fireChange() {
        if (dsl != null) {
            dsl.fireServerChange(this);
        }
    }

    protected void setState(int state) {
        this.state = state;
    }
    
    public int getState() {
        return state;
    }
    
    public String getDescription() {
        return description;
    }

    /**
     * Returns the number of files on this server.
     * @return int
     */
    public int getFiles() {
        return files;
    }

    public String getName() {
        return name;
    }

    public InetSocketAddress getSocketAddress() {
        if (isStaticDns() && Math.max(lastresolvetry, lasttimeknown) + 900000L < System.currentTimeMillis()) {
            resolve();
        }
        return socketAddress;
    }

    /**bean helper providing HostAddress textual representation of the IP.
    * @return IP as String
    */
    public String getIPString() {
        return getSocketAddress().getAddress().getHostAddress();
    }

    /**bean helper providing port base number of this servers InetSocketAddress.
    * @return port base number as int
    */
    public int getPort() {
        return getSocketAddress().getPort();
    }

    protected boolean resolve() {
        log.info("tring to resolve the hostname "+staticDnsName);
        lastresolvetry = System.currentTimeMillis();
        try {
            InetAddress inetadd = InetAddress.getByName(staticDnsName);
            InetSocketAddress oldsocketAddress = socketAddress;
            socketAddress = new InetSocketAddress(inetadd, socketAddress.getPort());
            lasttimeknown = lastresolvetry; //weak: machine may up, but donkey server possibly down
            if (!socketAddress.equals(oldsocketAddress)) {
                fireChange();
            }
            return true;
        } catch (UnknownHostException uhe) {
            //ups... dns - service down or wrong/invalid dns
            log.info("Problem resolving hostname for" + staticDnsName + " got: " + uhe.getMessage());
        }
        return false;
    }

    protected void callback(byte[] id) {
        ByteBuffer packet = DonkeyPacketFactory.globalcallbackrequest(id);
        packetList.add(packet);
        if (!sendreservation) {
            DonkeyProtocol.getInstance().getUDPListener().add(this);
            sendreservation = true;
        }
    }

    public void requestServerStatus() {
        requestServerStatus = true;
        if (!sendreservation) {
            DonkeyProtocol.getInstance().getUDPListener().add(this);
            sendreservation = true;
        }
    }

    public void requestServerInfo() {
        requestServerInfo = true;
        if (!sendreservation) {
            DonkeyProtocol.getInstance().getUDPListener().add(this);
            sendreservation = true;
        }
    }

    public void sourceSearch(byte[] hash) {
        sourceSearchList.add(hash);
        log.fine(" sourceSearch " + getSocketAddress() + " " + Convert.bytesToHexString(hash));
        if (!sendreservation) {
            DonkeyProtocol.getInstance().getUDPListener().add(this);
            sendreservation = true;
        }
    }

    public void metaSearch(SearchQuery search) {
        ByteBuffer packet = DonkeyPacketFactory.UDPsearch(search, isUDPExtendedGetFilesEnabled());
        packetList.add(packet);
        if (!sendreservation) {
            DonkeyProtocol.getInstance().getUDPListener().add(this);
            sendreservation = true;
        }
    }

    protected ByteBuffer getNextPacket() {
        ByteBuffer packet = null;
        if (requestServerInfo) {
            packet = DonkeyPacketFactory.serverinforequest();
            requestServerInfo = false;
        } else if (requestServerStatus) {
            int timestamp = 0x55AA0000 | (int) (System.currentTimeMillis() & 0xFFffL);
            log.fine(
                "request status from " + getSocketAddress() + " with timestamp: " + Integer.toHexString(timestamp));
            packet = DonkeyPacketFactory.serverstatusrequst(timestamp);
            requestServerStatus = false;
        } else if (!packetList.isEmpty()) {
            packet = (ByteBuffer)packetList.removeFirst();
        } else if (!sourceSearchList.isEmpty()) {
            if (isUDPExtendedGetSourcesEnabled()) {
                packet = DonkeyPacketFactory.udpSearchSources(sourceSearchList);
            } else {
                packet = DonkeyPacketFactory.udpSearchSources((byte[])sourceSearchList.removeFirst());
            }
        }
        sendreservation = (!packetList.isEmpty()) || (!sourceSearchList.isEmpty());
        if (sendreservation) {
            DonkeyProtocol.getInstance().getUDPListener().add(this);
        }
        return packet;
    }

    /**
     * Returns the number of users on this server.
     * @return int
     */
    public int getUsers() {
        return users;
    }

    /**
     * Sets the description.
     * @param description The description to set
     */
    protected void setDescription(String description) {
        if (!this.description.equals(description)) {
            if (!staticDns) {
                testForDns(description);
            }
            this.description = description;
            fireChange();
        }

    }

    /**
     * Sets the files.
     * @param files The number of files on this server
     */
    protected void setFiles(int files) {
        if (this.files != files) {
            this.files = files;
            fireChange();
        }

    }

    /**
     * Sets the soft limit for files per client on this server.
     * @param softLimitForFiles The number of files this server add max to its index per client
     */
    protected void setSoftLimitForFiles(int softLimitForFiles) {
        if (this.softLimitForFiles != softLimitForFiles) {
            this.softLimitForFiles = softLimitForFiles;
            fireChange();
        }

    }

    /**
     * Sets the hard limit for files per client on this server.
     * @param hardLimitForFiles If a client tries to publish more than hardLimitForFiles files connected to this server this server will disconnect.
     */
    protected void setHardLimitForFiles(int hardLimitForFiles) {
        if (this.hardLimitForFiles != hardLimitForFiles) {
            this.hardLimitForFiles = hardLimitForFiles;
            fireChange();
        }
    }

    /**
     * Sets the name.
     * @param name The name of this server
     */
    protected void setName(String name) {
        if (!this.name.equals(name)) {
            if (!staticDns) {
                testForDns(name);
            }
            this.name = name;
            fireChange();
        }
    }

    /**
     * Sets the users.
     * @param users The number of users on this server
     */
    protected void setUsers(int users) {
        if (this.users != users) {
            this.users = users;
            fireChange();
        }
    }

    /**
     * Sets the maximum number of users for this server.
     * @param maxUsers The number of users this server will except.
     */
    protected void setMaxUsers(int maxUsers) {
        if (this.maxUsers != maxUsers) {
            this.maxUsers = maxUsers;
            fireChange();
        }
    }

    /**
     * @param message the server message recieved from the server.
     */
    protected void setMessage(String message) {
        if (!this.message.equals(message)) {
            this.message = message;
            fireChange();
        }
    }

    public String getMessage() {
        return message;
    }
    
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
    * Updates lasttimeknown.
    */
    protected void setAlive() {
        lasttimeknown = System.currentTimeMillis();
    }
    /**
    * Returns last time this server was to be *known*.
    * @return time in ms.
    */
    public long getLastTimeKnown() {
        return lasttimeknown;
    }

    /**
    * The user can decids if a server should never get removed.
    * @return <tt>true</tt> if this server should never be removed from global serverlist, otherwise <tt>false</ff>
    */
    public boolean isImmortal() {
        return immortal;
    }

    /** Test for none valid internet address. ({@linkplain #filterNonPublicServer Private addresses may treaded as bad.})
    * ignores immortals always if they have a private or public host ip and a port!=0
    */
    public boolean hasBadInternetAddress() {
        if (socketAddress.getPort() == 0)
            return true;
        int[] ip = Convert.unsingedByteArrayToInts(socketAddress.getAddress().getAddress());
        // XXX: introduce isClassA,B;C;InetAddress
        if (ip[0] > 223) { //group and reserved
            return true;
        }
        int n = 2;
        if (ip[0] < 192)
            n = 1;
        if (ip[0] < 127)
            n = 0;
        int t = ip[3 - n];
        for (int j = 2; j > n; j--) {
            if (ip[j] != t)
                t = -1;
        }
        //net and broadcast
        if ((t == 0 || t == 255) && ip[0] != 127)
            return true;

        if (immortal)
            return false;
        // private ip check, check allways in public net!
        if (!filterNonPublicServer)
            return false;
        if (ip[0] == 127
            || ip[0] == 10
            || (ip[0] == 172 && ip[1] > 15 && ip[1] < 32)
            || (ip[0] == 192 && ip[1] == 168))
            return true;
        return false;
    }

    /**
    * The user decids if a server gets never removed.
    */
    public void setImmortal(boolean immortal) {
        this.immortal = immortal;
    }

    /** Set the autoconnecting mode for this server.
    * @param autoConnectAllowed if true this server will be used for automatic connecting, otherwise it will be used for udp querrys and only excplicte connect.
    */
    public void setAutoConnectAllowed(boolean autoConnectAllowed) {
        this.autoConnectAllowed = autoConnectAllowed;
    }

    /** Getthe autoconnecting mode for this server.
    * @return true if this server will be used for automatic connecting, otherwise it will be used for udp querrys and only excplicte connect.
    */
    public boolean isAutoConnectAllowed() {
        return autoConnectAllowed;
    }

    /**
     * Returns the staticDns.
     * @return null if !isStaticDns(), otherwise the server's hostaddress as domainname.
     */
    public String getStaticDns() {
        return staticDnsName;
    }

    /**
     * Sets the staticDns.
     * @param staticDns The staticDnsName to set
     */
    public void setStaticDns(String staticDns) {
        this.staticDnsName = staticDns;
    }

    /**
     * Returns the staticDns.
     * @return true if <b>this</b> has a domain name for the server
     */
    public boolean isStaticDns() {
        return staticDns;
    }

    /**
     * Sets the staticDns.
     * @param staticDns The staticDnsName to set
     */
    public void setStaticDns(boolean staticDns) {
        this.staticDns = staticDns;
    }

    /**
     * Returns TCPEmuleCompression support state of server.
     * @return true if <b>this</b> supports use of eMule client like compressed TCP packets in server connections
     */
    public boolean isTCPEmuleCompressionEnabled() {
        return TCPEmuleCompression;
    }

    /**
    * Sets TCPEmuleCompression support of server.
    * @param TCPEmuleCompression - if support flag present in OP_IDCHANGE packet; <code>TCPEmuleCompression = (flag&1) == 1;</code>
    */
    protected void setTCPEmuleCompression(boolean TCPEmuleCompression) {
        this.TCPEmuleCompression = TCPEmuleCompression;
    }

    /**
     * Returns UDPExtendedGetFiles support state of server.
     * @return true if <b>this</b> supports send of multiple search result packets in one UDP packet
     */
    public boolean isUDPExtendedGetFilesEnabled() {
        return UDPExtendedGetFiles;
    }

    /**
    * Sets UDPExtendedGetFiles support of server.
    * @param UDPExtendedGetFiles - if support flag present in OP_GLOBSERVERSTATUS packet; <code>UDPExtendedGetFiles = (flag&2) == 2;</code>
    */
    protected void setUDPExtendedGetFiles(boolean UDPExtendedGetFiles) {
        this.UDPExtendedGetFiles = UDPExtendedGetFiles;
    }

    /**
     * Returns UDPExtendedGetSources support state of server.
     * @return true if <b>this</b> supports send of multiple source packets in one UDP packet
     */
    public boolean isUDPExtendedGetSourcesEnabled() {
        return UDPExtendedGetSources;
    }

    /**
    * Sets UDPExtendedGetSources support of server.
    * @param UDPExtendedGetSources - if support flag present in OP_GLOBSERVERSTATUS packet; <code>UDPExtendedGetSources = (flag&1) == 1;</code>
    */
    protected void setUDPExtendedGetSources(boolean UDPExtendedGetSources) {
        this.UDPExtendedGetSources = UDPExtendedGetSources;
    }

    public static String TAG_ip = "ip";
    public static String TAG_tcpPort = "tcpPort";
    public static String TAG_dns = "dns";
    public static String TAG_method = "method";
    public static String Tag_name = "name";
    public static String Tag_description = "description";
    public org.jmule.util.XMLnode fromXml(String xmlData) throws org.jmule.core.InvalidXmlStructureException {
        String[] splitSet;
        splitSet = org.jmule.util.XMLReader.parseAndSplitXMLData(xmlData);
        try {
            while (splitSet[0] != null) {
                String tagname = splitSet[0];
                if (tagname.equals(Tag_name)) {
                    this.name = org.jmule.util.XMLReader.unescape(splitSet[1].trim());
                } else if (tagname.equals(Tag_description)) {
                    this.description = org.jmule.util.XMLReader.unescape(splitSet[1].trim());
                } else if (tagname.equals(TAG_dns)) {
                    this.staticDnsName = splitSet[1];
                    this.staticDns = true;
                    this.resolve();
                } else if (tagname.equals(TAG_ip)) {
                    this.socketAddress =
                        new InetSocketAddress(InetAddress.getByName(splitSet[1].trim()), this.socketAddress.getPort());
                } else if (tagname.equals(TAG_tcpPort)) {
                    this.socketAddress =
                        new InetSocketAddress(this.socketAddress.getAddress(), Integer.parseInt(splitSet[1].trim()));
                } else if (tagname.equals(TAG_method)) {
                    if (splitSet[1].indexOf("immortal") != -1) {
                        this.setImmortal(true);
                    }
                    if (splitSet[1].indexOf("manual") != -1) {
                        this.setAutoConnectAllowed(false);
                    }
                }
                splitSet = org.jmule.util.XMLReader.parseAndSplitXMLData(splitSet[2]);
            }
        } catch (java.net.UnknownHostException ue) {
            throw new org.jmule.core.InvalidXmlStructureException("tag ip wrong: " + ue.getMessage());
        } catch (NumberFormatException nfe) {
            throw new org.jmule.core.InvalidXmlStructureException("tag tcpPort wrong: " + nfe.getMessage());
        }
        //only on option?
        if (!staticDns) {
            testForDns(this.name);
            if (!staticDns) {
                testForDns(this.description);
            }
        }
        return this;
    }

    /** Tells if another DonkeyServer equals to this.
     * @param arg0 DonkeyServer
     * @return true if arg0 is a DonkeyServer and has same internetaddress ip:port or dns are equal if both staticDns.
     */
    public boolean equals(Object arg0) {
        if (arg0 instanceof DonkeyServer) {
            DonkeyServer server = (DonkeyServer)arg0;
            if (server.getSocketAddress().equals(socketAddress))
                // FIXME: here should be better checking for equality, i.e. by name ...
                return true;
            else if (staticDns)
                if (server.getStaticDns().equals(staticDnsName)) {
                    return true;
                }
        }
        return false;
    }

    protected void testForDns(String line) {
        // XXX(performance): This seems to be quite popular test, so make the patternset static and create it only once
        Pattern[] patternset =
            new Pattern[] {
                Pattern.compile(".*\\[emDynIP:\\s*([0-9a-zA-Z\\.\\-]+)\\](.*)"),
                Pattern.compile(".*ping\\s+([0-9a-zA-Z\\.\\-]+)(.*)"),
                Pattern.compile("(?:.*[^0-9a-zA-Z\\.\\-])?([0-9a-zA-Z\\.\\-]+.dyndns.org)(.*)"),
                };
        String data = line;
        boolean match = data != null;
        while (match) {
            match = false;
            for (int j = 0; j < patternset.length; j++) {
                Matcher m = patternset[j].matcher(data);
                if (m.matches()) {
                    for (int i = 0; i <= m.groupCount(); i++) {
                        log.finer("parse for staticDns " + i + " " + m.group(1));
                    }
                    try {
                        InetAddress inetadd = InetAddress.getByName(m.group(1));
                        if (inetadd.equals(getSocketAddress().getAddress())) {
                            log.fine(m.group(1) + " points to " + getSocketAddress().getAddress());
                            setStaticDns(m.group(1));
                            setStaticDns(true);
                            return;
                        }
                    } catch (UnknownHostException uhe) {
                        //dns - service down or wrong/invalid/not a dns
                    }
                    data = m.group(2);
                    match = data != null;
                    break; //for
                } else if (log.isLoggable(Level.FINEST)) {
                    log.finest("parse for staticDns " + data + " does not match " + patternset[j].pattern());
                }
            }
        }
        return;
    }

    /**
     * Provides the hard limit for files per client on this server.
     * @return The number of files published to this server do this server disconnect.
     */
    public int getHardLimitForFiles() {
        return hardLimitForFiles;
    }

    /**
     * Provides the maximum number of users for this server.
     * @return The number of users this server will except.
     */
    public int getMaxUsers() {
        return maxUsers;
    }

    /**
     * Provides the soft limit for files per client on this server.
     * @return The number of files this server maximum add to its index per client
     */
    public int getSoftLimitForFiles() {
        return softLimitForFiles;
    }

    // bean/ui methods
    /**
    * Removes this server from its serverlist.
    */
    public void removeFromList() {
        if (dsl != null) {
            dsl.remove(this);
            dsl = null;
        }
    }

    /**
    * Connects to this server if not already connected to this server.
    */
    public void connectTo() {
        DonkeyServerConnection donkeyServerConnection = DonkeyProtocol.getInstance().getServerConnection();
        if (donkeyServerConnection == null) {
            ConnectionManager.getInstance().addConnection(new DonkeyServerConnection(this));
        } else if (donkeyServerConnection.getServer() != this) {
            if (donkeyServerConnection.getState() != DonkeyProtocol.SERVER_DISCONNECTED) {
                donkeyServerConnection.close();
            }
            ConnectionManager.getInstance().addConnection(new DonkeyServerConnection(this));
        } else if (donkeyServerConnection.getState() == DonkeyProtocol.SERVER_DISCONNECTED) {
            ConnectionManager.getInstance().addConnection(new DonkeyServerConnection(this));
        }
    }

    /**
    * Negates the current AutoConnectAllowed state.
    */
    public void toogleAutoConnect() {
        setAutoConnectAllowed(!isAutoConnectAllowed());
    }

    /**
    * Negates the current Immortal state.
    */
    public void toogleRemoveable() {
        setImmortal(!isImmortal());
    }

}
