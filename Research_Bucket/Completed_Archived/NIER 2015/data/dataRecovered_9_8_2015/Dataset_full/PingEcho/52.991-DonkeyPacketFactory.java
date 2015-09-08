/********************************************************************************

    jMule - a Java massive parallel file sharing client
    
    Copyright (C) by the jMuleGroup ( see the CREDITS file )

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details ( see the LICENSE file ).

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
	
********************************************************************************/
package org.jmule.core.protocol.donkey;

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.jmule.core.CoreManager;
import org.jmule.core.SearchQuery;
import org.jmule.core.SharedFile;
import org.jmule.core.SharesManager;
import org.jmule.util.Convert;
import org.jmule.util.VectorNotifier;

/** FIXME: a class deserves a javadoc
 * @version $Revision: 1.44 $
 * <br>Last changed by $Author: emarant $ on $Date: 2004/01/03 12:52:48 $
 */
public class DonkeyPacketFactory implements DonkeyPacketConstants {
	static Logger log = Logger.getLogger(DonkeyPacketFactory.class.getName());
    
	public static DonkeyPacket fileOffer() {
		byte command = Convert.intToByte(OP_OFFERFILES);
		SharesManager shares = SharesManager.getInstance();
		int totalFiles = shares.numFiles();
		if (totalFiles < 1)
			return null;

		int neededBytes = 0;

		LinkedList filesToShare = new LinkedList();
		VectorNotifier sharedFiles = shares.getSharedFiles();

		for (int i = 0; i < totalFiles; i++) {
      // actFile == current File
            int addbytes = 0;
			SharedFile actFile = (SharedFile) sharedFiles.get(i);
			String fileName = actFile.toString();
			boolean shareInDonkey = false;
            int ext = fileName.lastIndexOf('.');
            if (ext>-1&&ext<fileName.length()) {
                addbytes =  6 + fileName.length() - ext; //tag head + stringlength + string value
            }
			if (actFile.getSize() > PARTSIZE ) {
				shareInDonkey = (actFile.extendedInfo().containsKey("Donkey") && actFile.extendedInfo().containsKey("DonkeyHashes"));
			}
			else {
				shareInDonkey = (actFile.extendedInfo().containsKey("Donkey") && actFile.extendedInfo().containsKey("DonkeyHash"));
			}
			if (shareInDonkey) {
                neededBytes += fileName.length() + 40 + addbytes;
				filesToShare.add(actFile);
			}
		}

		if (filesToShare.size() < 1)
			return null; // no files to share

		log.fine("Sending file list to server... Number of shared files: " + filesToShare.size());

		DonkeyPacket packet = new DonkeyPacket(neededBytes + 6 +4);
		ByteBuffer packetBuffer = packet.getBuffer();
		packet.setCommandId(command);
		packetBuffer.position(6);
		packetBuffer.putInt(filesToShare.size());

		Iterator it = filesToShare.iterator();
		while (it.hasNext()) {
			SharedFile actFile = (SharedFile) it.next();

			long fileSize = actFile.getSize();
			String fileName = actFile.toString();
			byte[] hash = ((DonkeyFileHash)actFile.extendedInfo().get("DonkeyHash")).getBytes();
            int ext = fileName.lastIndexOf('.');
            String fileNameExt = null;
            if (ext>-1&&ext<fileName.length()) {
                 fileNameExt = fileName.substring(ext+1).toLowerCase();
            }

			packetBuffer.put(hash);

            int[] iarray = { 0, 0, 0, 0, 0, 0, fileNameExt==null?0x2:0x3, 0, 0, 0, };

			packetBuffer.put(Convert.intArrayToBytes(iarray));

			Tag.append(packetBuffer, Tag.FT_Filename, fileName);
			Tag.append(packetBuffer, Tag.FT_Filesize, (int) fileSize);
            //Tag.append(packetBuffer, Tag.FT_Filetype, )
            if (fileNameExt!=null){
                Tag.append(packetBuffer, Tag.FT_Fileformat, fileNameExt);
		}
        }

		packetBuffer.rewind();
		log.finer(Convert.byteBufferToHexString(packetBuffer));
        DonkeyProtocol dContext = DonkeyProtocol.getInstance();
        if (dContext.isEmuleEnabled && dContext.getCurrentServer().isTCPEmuleCompressionEnabled()) {
            packet = EmuleCompressor.commpressPacket(packet);
        }
        
		return packet;
	}
    
    /** Generates a DonkeyPacket with the list of all shred files for send to an other client
    * @fixme make option to configure behaviour: show all/show nothing to other clients; or show allway.
    * @todo add code for generation of the list
    * @return a DonkeyPacket with <tt>0</tt> shared files
    */
    public static DonkeyPacket filelist(){
        /*Packet:
        * head,body
        * head: ed2k header
        * body: int number of files, [data].
        * data: 16byte hashcode, int = 0x00000000, short = 0x0000, int tagcount, [tag].
        */
        DonkeyPacket packet = new DonkeyPacket(10);
        ByteBuffer packetBuffer = packet.getBuffer();
        packet.setCommandId((byte)OP_FILELISTANSWER);
        packetBuffer.position(6);
        packetBuffer.putInt(0); //empty list
        packetBuffer.rewind();
        return packet; 
    }
    
        /** Provides a Hello packet for login on donkeyserve.
         * @return DonkeyPacket for login on donkeyserve.
         */        
	public static DonkeyPacket hello() {
		DonkeyPacket packet = new DonkeyPacket();
        
		String userName = CoreManager.getInstance().getUserNick();
        DonkeyProtocol dContext = DonkeyProtocol.getInstance();        
		ByteBuffer buffer = ByteBuffer.allocateDirect((dContext.isEmuleEnabled?62:54) + userName.length()).order(ByteOrder.LITTLE_ENDIAN);
		packet.setBuffer(buffer);
		packet.setCommandId(Convert.intToByte(OP_LOGINREQUEST));
        
		buffer.position(6);

		buffer.put(dContext.getUserHash());
        
		buffer.putInt((int)dContext.getUserID());
		buffer.putShort((short)dContext.usedTCPPort);
        
        buffer.putInt(dContext.isEmuleEnabled?4:3); // three tags to come
		Tag.append(buffer, Tag.CT_Username, userName);
		Tag.append(buffer, Tag.CT_Version, dContext.getClientVersion() );
		Tag.append(buffer, Tag.CT_Port, dContext.usedTCPPort);
        if (dContext.isEmuleEnabled) {
            Tag.append(buffer, (byte)0x20 , 1); //16.40 compressed
        }
		buffer.rewind();
		return (packet);
	}

       /** Provides Hello-Packets for Client to Client communication.
         * @return a DonkeyPacket ready for send to a Client
         */        
	public static DonkeyPacket hello_download() {
        String userName = CoreManager.getInstance().getUserNick();
        
		DonkeyPacket packet = new DonkeyPacket(61 + userName.length());
		ByteBuffer buffer = packet.getBuffer();
        buffer.put((byte)OP_HELLO);
        
		buffer.put((byte)0x10); // This is the hash size: 0x10==16
        DonkeyProtocol dContext = DonkeyProtocol.getInstance();
		buffer.put(dContext.getUserHash());
        
		buffer.putInt((int)dContext.getUserID());
		buffer.putShort((short)dContext.usedTCPPort);

		buffer.putInt(3); // three tags to come
		Tag.append(buffer, Tag.CT_Username, userName);
		Tag.append(buffer, Tag.CT_Version, dContext.getClientVersion() );
		Tag.append(buffer, Tag.CT_Port, dContext.usedTCPPort);
        
        if (dContext.getCurrentServer()!=null){
            buffer.put(dContext.getCurrentServer().getSocketAddress().getAddress().getAddress());
            buffer.putShort((short) dContext.getCurrentServer().getSocketAddress().getPort());
        } else {
            buffer.putInt(0);
            buffer.putShort((short)0);
        }
        
		buffer.rewind();
        
		return (packet);
	}

       /** Provides Hello-Answer-Packets for Client to Client communication.
         * @return a DonkeyPacket ready for send a hello response to a Client send a Hello
         */        
	public static DonkeyPacket hello_client() {	
        String userName = CoreManager.getInstance().getUserNick();
        
        DonkeyPacket packet = new DonkeyPacket(60 + userName.length());
        ByteBuffer buffer = packet.getBuffer();
        buffer.put((byte)OP_HELLOANSWER);
        
        DonkeyProtocol dContext = DonkeyProtocol.getInstance();
        buffer.put(dContext.getUserHash());
        
		buffer.putInt((int)dContext.getUserID());
        buffer.putShort((short)dContext.usedTCPPort);
        
		buffer.putInt(3); // three tags to come
        Tag.append(buffer, Tag.CT_Username, userName);
        Tag.append(buffer, Tag.CT_Version, dContext.getClientVersion() );
        Tag.append(buffer, Tag.CT_Port, dContext.usedTCPPort);
        
        if (dContext.getCurrentServer()!=null){
            buffer.put(dContext.getCurrentServer().getSocketAddress().getAddress().getAddress());
            buffer.putShort((short) dContext.getCurrentServer().getSocketAddress().getPort());
        } else {
            buffer.putInt(0);
            buffer.putShort((short)0);
        }
        buffer.rewind();
        
        return (packet);
	}

     
	public static DonkeyPacket fileName(byte[] hash, String fileName) {
		DonkeyPacket packet = new DonkeyPacket(24 + fileName.length());

		packet.setCommandId(Convert.intToByte(OP_FILEREQANSWER));

		ByteBuffer buffer = packet.getBuffer();
		buffer.position(6);
		buffer.put(hash);
		buffer.putShort((short) fileName.length());
		buffer.put(fileName.getBytes());

		buffer.rewind();

		return packet;
	}
    
    /**
     * Use this for pinging  a server via UDP.
     *@param mark used to identify a answers to this packet
     *@return the packet as ByteBufer
     */
    public static ByteBuffer serverstatusrequst(int mark){
		ByteBuffer packetBytes = DonkeyPacket.allocatePlainByteBuffer(6);
		packetBytes.put((byte)OP_EDONKEYHEADER);
		packetBytes.put((byte)OP_GLOBSERVERSTATUSREQ);
		packetBytes.putInt(mark);
        packetBytes.rewind();
        return packetBytes;
    }
    
    public static ByteBuffer serverinforequest(){
		ByteBuffer packetBytes = DonkeyPacket.allocatePlainByteBuffer(2);
		packetBytes.put((byte)OP_EDONKEYHEADER);
		packetBytes.put((byte)OP_GLOBSERVERINFOREQ);
        packetBytes.rewind();
        return packetBytes;
    }
    
    public static ByteBuffer globalcallbackrequest(byte[] id){
        if (id.length!=4) 
            return null;
        DonkeyProtocol dContext = DonkeyProtocol.getInstance();
		ByteBuffer packetBytes = DonkeyPacket.allocatePlainByteBuffer(12);
		packetBytes.put((byte)OP_EDONKEYHEADER);
		packetBytes.put((byte)OP_GLOBCALLBACKREQ);
        packetBytes.putInt((int)dContext.getUserID());
        packetBytes.putShort((short)dContext.usedTCPPort);
		packetBytes.put(id);
        packetBytes.rewind();
        return packetBytes;
    }
    
    
    public static DonkeyPacket callbackrequest(byte[] id){
        if (id.length!=4) return null;
        
        DonkeyPacket packet = new DonkeyPacket(10);
        
        packet.getBuffer().put((byte)OP_CALLBACKREQUEST).put(id).rewind();
        
        return packet;
    }
          
	public static DonkeyPacket search(String searchString) {
		DonkeyPacket packet = new DonkeyPacket(9 + searchString.length());

		packet.setCommandId(Convert.intToByte(OP_SEARCHREQUEST));

		ByteBuffer buffer = packet.getBuffer();
		buffer.position(6);
		buffer.put(Convert.intToByte(0x01));
		buffer.putShort((short) searchString.length());
		buffer.put(searchString.getBytes());

		buffer.rewind();

		return (packet);
	}
     
	public static DonkeyPacket searchSources(byte[] hash) {
		return (command(OP_GETSOURCES, hash));
	}
        
	public static DonkeyPacket fileRequest(byte[] hash) {
		return (command(OP_FILEREQUEST, hash));
	}
      
	public static DonkeyPacket transferEnd(byte[] hash) {
		return (command(OP_ENDTRANSFER, hash));
	}
       
	public static DonkeyPacket partRequest(byte[] hash) {
		return (command(OP_SETREQFILEID, hash));
	}

    /// CHECK: not needed?
 	public static DonkeyPacket fileSize(byte[] hash, long fileSize) {
		DonkeyPacket packet = new DonkeyPacket(40);

		packet.setCommandId(Convert.intToByte(OP_REQUESTPARTS));

		ByteBuffer buffer = packet.getBuffer();
		buffer.position(6);

		buffer.put(hash);

		for (int i = 0; i <= 10; i++) {
			buffer.put(Convert.intToByte(0));
		}

		buffer.putInt((int) fileSize);

		for (int i = 1; i <= 8; i++) {
			buffer.put(Convert.intToByte(0));
		}

		buffer.rewind();

		return (packet);
	}
    /**
    *logs emule workaround use
    */
    public static DonkeyPacket hashSet(byte[][] hashes) {
        DonkeyPacket packet = null;
        if (hashes !=null ){
            packet = new DonkeyPacket( 8+hashes.length*16 );
            ByteBuffer buffer = packet.getBuffer();
            buffer.put((byte)OP_HASHSETANSWER);
            buffer.put(hashes[0]);
            buffer.putShort((short)(hashes.length-1));
            for (int i=1;i<hashes.length;i++){
                buffer.put(hashes[i]);
            }
            buffer.rewind();
        }
        return packet;
    }
        /** Provides partlist packets.
         * @param chunks shareable parts of file
         * @param complete if file complete a special version of partlist is created
         * @param size file size - because BitSet lacks of part count
         * @param fileHash ed2k hash of file the partlist is for
         * @return DonkeyPacket ready for send
         */        
    public static DonkeyPacket fileStatus(BitSet chunks, boolean complete, long size, byte[] fileHash) {
		DonkeyPacket packet;
		ByteBuffer buffer;
        if (!complete) {
            int numChunks = DonkeyProtocol.calcNumChunks( size );
            byte[] parts = Convert.bitSetToBytes(chunks, numChunks);
			packet = new DonkeyPacket(24 + parts.length);
			buffer = packet.getBuffer();
			buffer.position(6);
			buffer.put(fileHash);
			buffer.putShort((short)numChunks);
			buffer.put(parts);
		} else {
			packet = new DonkeyPacket(25);
			buffer = packet.getBuffer();
			buffer.position(6);
			buffer.put(fileHash);
			buffer.put(Convert.intToByte(0));
			buffer.put(Convert.intToByte(0));
			buffer.put(Convert.intToByte(0));
		}

		packet.setCommandId(Convert.intToByte(OP_FILESTATUS));
		buffer.rewind();
		return (packet);
	}
    
    /** Provide queuePosition Packet. 
     * @param queueposition queueposition for the peer on our uploadqueue.
     * @return DonkeyPacket ready for send.
     */        
	public static DonkeyPacket queuePosition(long queueposition) {
		DonkeyPacket packet = new DonkeyPacket(10);
        packet.setCommandId(Convert.intToByte(OP_QUEUEPOSITION));
        ByteBuffer buffer = packet.getBuffer();
        buffer.position(6);
        buffer.putInt((int) queueposition);
        buffer.rewind();
        return packet;
	}
       
	public static DonkeyPacket fileBlock(ByteBuffer block, byte[] hash, long start) {
		int blockSize = block.remaining();
		DonkeyPacket packet = new DonkeyPacket(30 + blockSize);

		packet.setCommandId(Convert.intToByte(OP_SENDINGPART));

		ByteBuffer buffer = packet.getBuffer();
		buffer.position(6);

		buffer.put(hash);
		buffer.putInt((int) start);
		buffer.putInt((int) start + blockSize);
		buffer.put(block);
		buffer.rewind();
		return (packet);
	}
      
    public static DonkeyPacket fileBlock(SharedFile sf, byte[] hash, long start, int blockSize) throws IOException {
        DonkeyPacket packet = new DonkeyPacket(30 + blockSize);
        
        packet.setCommandId(Convert.intToByte(OP_SENDINGPART));
        
        ByteBuffer buffer = packet.getBuffer();
        buffer.position(6);
        
        buffer.put(hash);
        buffer.putInt((int) start);
        buffer.putInt((int) start + blockSize);
        long a = sf.getBytes(start, buffer);
        if (a<blockSize) {
            log.warning("read problems read "+a+ " bytes but "+blockSize+" bytes expected from "+sf.getPath() );
            throw new IOException("read problems "+a+"<"+blockSize+" "+sf.getPath());
        }
        log.finer("read at:" +start+ " " +a+ " bytes from "+sf.getPath());
        buffer.rewind();
        
        return (packet);
	}
        /** Provides packets for request data (chunks) of a file from a client.
         * @param hash the edonkey2000 hash of the file
         * @param one startoffset of the first chunk
         * @param two startoffset of the second chunk
         * @param three startoffset of the third chunk
         * @param four endoffset of the first chunk
         * @param five endoffset of the second chunk
         * @param six endoffset of the third chunk
         * @return packet ready for send to a client to request up to 3 chunks of a file
         */        
	public static DonkeyPacket partRequest(byte[] hash, long one, long two, long three, long four, long five, long six) {
		DonkeyPacket packet = new DonkeyPacket(46);

		packet.setCommandId(Convert.intToByte(OP_REQUESTPARTS));

		ByteBuffer buffer = packet.getBuffer();
		buffer.position(6);

		buffer.put(hash);
		buffer.putInt((int) one);
		buffer.putInt((int) two);
		buffer.putInt((int) three);
		buffer.putInt((int) four);
		buffer.putInt((int) five);
		buffer.putInt((int) six);

		buffer.rewind();

		return (packet);
	}
    
    /** Provides an UDP request packet as ByteBuffer to search a ed2k server for source with the ed2k hash.
     * @param hash the ed2k hash to search for.
     * @return a ByteBuffer with the requested hash.
     */
	public static ByteBuffer udpSearchSources(byte[] hash) {
		ByteBuffer packetBytes = DonkeyPacket.allocatePlainByteBuffer(18);
		packetBytes.put((byte)OP_EDONKEYHEADER);
		packetBytes.put((byte)OP_GLOBGETSOURCES);
		packetBytes.put(hash);
        packetBytes.rewind();
		return packetBytes;
	}
      
    /** Provides an UDP request packet as ByteBuffer to search a ed2k server for multiple sources with the ed2k hash 
     * - only understood by some servers (16.40+) sending multiple results in one UDP packet.
     * @param list LinkedList with the ed2k hashes as <code>byte[]</code> to search for.
     * @return a ByteBuffer with the requested hashes.
     */
	public static ByteBuffer udpSearchSources(LinkedList list) {
        int count = list.size();
        count = count>30?30:count;
		ByteBuffer packetBytes = DonkeyPacket.allocatePlainByteBuffer(2+16*count);
		packetBytes.put((byte)OP_EDONKEYHEADER);
		packetBytes.put((byte)OP_GLOBGETSOURCES);
        while((!list.isEmpty())&&count>0) {
            packetBytes.put((byte[])list.removeFirst());
            count--;
        }
        packetBytes.rewind();
		return packetBytes;
	}
    
	public static DonkeyPacket command(int command) {
		DonkeyPacket packet = new DonkeyPacket(6);

		packet.setCommandId(Convert.intToByte(command));

		ByteBuffer buffer = packet.getBuffer();
		buffer.rewind();
		return packet;
	}
       
	public static DonkeyPacket command(int command, byte[] hash) {
		DonkeyPacket packet = new DonkeyPacket(22);

		packet.setCommandId(Convert.intToByte(command));

		ByteBuffer buffer = packet.getBuffer();
		buffer.position(6);
		buffer.put(hash);

		buffer.rewind();

		return packet;
	}
    
    /** Provides a request packet to search for complex expressions on ed2k servern.
     * @param searchQuery the query containing the expression (infix notaion) to search for.
     * @return a DonkeyPacket with the requested ed2k search tree (prefix notaion).
     */   
     
    public static DonkeyPacket search( SearchQuery searchQuery ) {
        LinkedList atomicQueryElements = new LinkedList();
        int querylength = createDonkeySearchQueryList(atomicQueryElements, searchQuery);
		DonkeyPacket packet = new DonkeyPacket(6+querylength);
		ByteBuffer buffer = packet.getBuffer();
        packet.setCommandId(Convert.intToByte(OP_SEARCHREQUEST));
		buffer.position(6);
        fillSearch(buffer, atomicQueryElements);
		buffer.rewind();
		return packet;
	}
    
    /** Provides an UDP request packet as ByteBuffer to search for complex expressions on ed2k servern.
     * @param searchQuery the query containing the expression (infix notaion) to search for.
     * @param advancedResultPackets if <code>true</code> use the extended searchrequest - only understood by some servers (16.40+) sending multiple results in one UDP packet.
     * @return a ByteBuffer with the requested ed2k search tree (prefix notaion).
     */
    public static ByteBuffer UDPsearch( SearchQuery searchQuery, boolean advancedResultPackets ) {
        LinkedList atomicQueryElements = new LinkedList();
        int querylength = createDonkeySearchQueryList(atomicQueryElements, searchQuery);
        ByteBuffer buffer = DonkeyPacket.allocatePlainByteBuffer(2 + querylength);
        
		buffer.put((byte)OP_EDONKEYHEADER);
        if (advancedResultPackets) {
            buffer.put((byte)OP_GLOBEXTENDEDSEARCHREQ);
        } else {
            buffer.put((byte)OP_GLOBSEARCHREQ);
        }
        fillSearch(buffer, atomicQueryElements);
        buffer.rewind();
        
        return buffer;
    }
    
    /** Generates a (sub)tree from the input String. 
     * An expresion in infix notation is transformed into an equal expresion in prefix notation.
     * <p> currently supported syntax: <ol>
     * <ul> spaces outside from quotes seperates subexpressions</ul>
     * <ul> <b> A (B) </b> B is evaluated before A </ul>
     * <ul> <b>&quot;A&quot;</b> A is take as it is as a String</ul>
     * <ul> <b>A * B</b> * is one of the binary operators: -and -or -andnot</ul>
     * <ul> <b>-S*N[M]</b> S is special data name like size for file size or  sources for number of known sources for the searched file(s) * is = , &gt;= , &lt;= , != , > or < N is non negative figure optional M is k for 1 000 or m for 1 000 000 or g for 1 000 000 000</ul>
     * <ul> <b>S</b> S is any other String </ul>
     * <ul> a -and is added if operator between subexpresions is missing</ul>
     *</ol>
     * logical and relational operators tested with ed2k server version 16.38 and lugdunum patched server 16.38.p68
     * @param input e.g. <code>("milk" -or tea) -and bread</code> 
     * @return LinkedList from binary-tree from root to leafs and left to right in every node e.g.<br> <code>[operator AND, operator OR, string: milk, string: tea, string: bread]</code>
     */
    private static LinkedList searchqueryresolver(String input) {
        if (log.isLoggable(Level.FINEST)) {
            log.finest("parse: "+input);
        }
        // A = (...)|"..."|word [OP A]
        // b op1 c op2 d => op2 op1 b c d
        // (a op1 b) op2 c => op2 op1 a b c
        // a op1 (b op2 c) => op1 a op2 b c
        // a b c => a AND b AND c => AND a AND b c  ! ed2k, other may interpret concatination as OR
        //operand scanner: skip not word not ( not " get something like word or \([^)]*\) or "([^"]*)"  (B)? 
        // second step: find operator or operand in B
        // append operator or AND if only an operand follows in to result
        // if nothing found append first to result
        // if any operand (...) resolve ... and append it for the element to result
        // if any operand "..." append ... to  result
        // if any thing follows second operand replace first by the element that would be otherwise appended to result
        // and call remaining term B and go to second step
        //Problems: word can be any data like "size>200k" and what is operator ?
        // 1. solution: use - to mark operators and special data start e.g. -size>2000 -and -size<4000
        // 2. solution: normal string have to be inside " " any other is operator or special
        //                                       skip             +operator/spec        + "..."           +(...)              +word      skip          +next oper.
        Pattern superpat = Pattern.compile("[^\\w\"\\-\\(]*(?:(?:(?:\\-([^ \\(\"]*))|(?:\"([^\"]*)\"))|(?:(?:\\(([^\\)]*)\\))|([\\w]*)))[^\\w\\-\\(\"]*([\\-\\w\\(\"].*)?");
        //current design also allows querys like  -or basic java  for  basic -or java
        Matcher m; 
        int k= 0;
        LinkedList result = new LinkedList();
        Object first = null;
        SearchElement operator = null;
        Object second = null;
        while(k++<6&&input!=null){ //limit length?
            String data = null;
            int j=0;
            m = superpat.matcher(input);
            if (m.matches()){
                if (log.isLoggable(Level.FINEST)) {
                    log.finest("group(0) : "+m.group(0));
                }
                for (int i=1; i<m.groupCount();i++){
                    if (log.isLoggable(Level.FINEST)) {
                        log.finest("group("+i+"): "+m.group(i));
                    }
                    if (m.group(i)!=null) {
                        j = i;
                        data = m.group(i);
                    }
                }
                if (log.isLoggable(Level.FINEST)) {
                    log.finest("group("+m.groupCount()+"): "+m.group(m.groupCount()));
                }
                input = m.group(m.groupCount());
                switch(j) {
                    case 1 : {
                        boolean opinplace = operator!=null;
                        if (data.equalsIgnoreCase("and")) {
                            operator = new SearchElement();
                            operator.type=0;
                            operator.operator=0;
                        } else if (data.equalsIgnoreCase("or")) {
                            operator = new SearchElement();
                            operator.type=0;
                            operator.operator=1;
                        } else if (data.equalsIgnoreCase("andnot")) {
                            operator = new SearchElement();
                            operator.type=0;
                            operator.operator=2;
                        } else if (data.equalsIgnoreCase("xor")) {
                            operator = new SearchElement();
                            operator.type=0;
                            operator.operator=3;
                        } else if (data.equalsIgnoreCase("notand")) {
                            operator = new SearchElement();
                            operator.type=0;
                            operator.operator=4;
                        } else {
                            opinplace = false;
                            SearchElement se = new SearchElement();
                            se.type=3;
                            Pattern intmetatag = Pattern.compile("([\\w]+)([<>=!]+)([0-9]+)([kmgKMG])?");
                            Matcher metatagm = intmetatag.matcher(data);
                            if (metatagm.matches()) {
                                if (metatagm.group(1).equalsIgnoreCase("size")) {
                                    byte[] meta = {Tag.FT_Filesize};
                                    se.metadata = meta;
                                } else if (metatagm.group(1).equalsIgnoreCase("sources"))  {
                                    byte[] meta = {Tag.FT_Sources};
                                    se.metadata = meta;
                                } else {
                                    log.fine("unhandled metatag "+metatagm.group(0));
                                    break;
                                }
                                se.relation = metatagm.group(2).equals(">")?1:metatagm.group(2).equals("<")?2:
                                        metatagm.group(2).equals("=")?0:metatagm.group(2).equals(">=")?3:
                                        metatagm.group(2).equals("<=")?4:metatagm.group(2).equals("!=")?5:0;
                                
                                se.intvalue = Integer.parseInt(metatagm.group(3));
                                if (metatagm.group(4)!=null) {
                                    se.intvalue *= metatagm.group(4).equalsIgnoreCase("k")?
                                            1000:metatagm.group(4).equalsIgnoreCase("m")?
                                            1000000:1000000000;
                                }
                                if (first==null) {
                                    first = se;
                                } else if (second==null){
                                    second = se;
                                }
                            } else {
                                log.fine("no metatag match "+data);
                            }
                        }
                        if (opinplace) {
                            log.warning("syntax error");
                        }
                        break;
                        
                    } 
                    case 2 :
                    case 4 : {
                        SearchElement se = new SearchElement();
                        se.type = 1;
                        se.string = data;
                        if (first==null) {
                            first = se;
                        } else if (second==null){
                            second = se;
                        }
                        break;
                    }
                    case 3 : {
                        if (first==null) {
                            first = searchqueryresolver(data); 
                        } else if (second==null){
                            second = searchqueryresolver(data); 
                        }
                        break;
                    } 
                    default : {
                        log.warning("error parsing query "+j);
                    }
                }
                if (first!=null&&second!=null) {
                    if (operator==null) {
                        operator = new SearchElement();
                        operator.type=0;
                        operator.operator=0;
                    }
                    if (input==null) {
                        result.add(operator);
                        if (first instanceof SearchElement) {
                            result.add(first);
                        } else {
                            result.addAll((Collection)first);
                        }
                        if (second instanceof SearchElement) {
                            result.add(second);
                        } else {
                            result.addAll((Collection)second);
                        }
                    } else {
                        LinkedList list = new LinkedList();
                        list.add(operator);
                        if (first instanceof SearchElement) {
                            list.add(first);
                        } else {
                            list.addAll((Collection)first);
                        }
                        if (second instanceof SearchElement) {
                            list.add(second);
                        } else {
                            list.addAll((Collection)second);
                        }
                        operator = null;
                        first = list;
                        second = null;
                    }
                } else if (input==null&&first!=null){
                    if (operator!=null) {
                        result.add(operator);
                    }
                    if (first instanceof SearchElement) {
                        result.add(first);
                    } else {
                        result.addAll((Collection)first);
                    }
                }
            } else {
                log.finer("problem parsing (expr. between \"s): \""+input+"\"");
                break;
            }
        }
        return result;
    }
    /** Prepares and the searchQuery to by send to a ed2k server.
    * @param searchQuery
    * @param atomicQueryElements empty LinkedList - is filled on return with SearchElement<tt>s</tt> generated by {@link org.jmule.core.protocol.donkey.DonkeyPacketFactory#searchqueryresolver(String input)  searchqueryresolver }.
    * @return length of searchQuery inside the packet send to an ed2k server
    */
    private static int createDonkeySearchQueryList(LinkedList atomicQueryElements, SearchQuery searchQuery) {
        int result = 0;
        String term = searchQuery.getQuery();
        log.fine("search for {"+term+"}");
        String logstring="search send to server: ";
        /*
        SearchElement se = new SearchElement();
        se.type=1;
        se.string="";
        atomicQueryElements.add(se);
        result+=se.length();
        */
        atomicQueryElements.addAll(searchqueryresolver(term));
        Iterator it = atomicQueryElements.iterator();
        if (it.hasNext()) {
            while(it.hasNext()) {
                SearchElement se = (SearchElement)it.next();
                result += se.length();
                if (log.isLoggable(Level.FINE)) {
                    logstring +=" "+se.toString();
                }
            }
        } else {
            SearchElement se = new SearchElement();
            se.type=1;
            se.string="";
            atomicQueryElements.add(se);
            result+=se.length();
        }
        if (log.isLoggable(Level.FINE)) {
                log.fine(logstring);
        }
        return result;
    }
    
    static class SearchElement{
        int type; //0 operator 1 string 2 metatag 3 int relation
        int operator; // - 0 and  1 or  2 and not
        int relation; // - 1 > 2 <
        String string;
        int intvalue;
        Tag tag;
        byte[] metadata;
        int length(){
            switch(type){
                case 0:
                    return 1+1;
                case 1:
                    return 1+2+string.length();
                case 3:
                    return 1+4+1+2+metadata.length;
            }
            return 0;
        }
        
        public String toString(){
            String result = "unkown";
            switch(type){
                case 0: {
                    result = "operator: ";
                    switch(operator){
                        case 0: {
                            result += "and";
                            break;
                        }
                        case 1: {
                            result += "or";
                            break;
                        }
                        case 2: {
                            result += "and not";
                            break;
                        }
                        case 3: {
                            result += "xor";
                            break;
                        }
                        case 4: {
                            result += "not and";
                            break;
                        }
                        default : {
                            result += "unkown ("+operator+")";
                        }
                    }
                    break;
                }
                case 1: {
                    result = "string: "+string;
                    break;
                }
                case 3: {
                    result = "integer metatag: value: "+ intvalue+" name/special 0x"+Convert.bytesToHexString(metadata);
                    break;
                }
            }
            return result;
        }
    }
    
    private static void fillSearch( ByteBuffer buffer, LinkedList atomicQueryElements ) {
        log.finest("fill");
		while(!atomicQueryElements.isEmpty()){
            SearchElement se = (SearchElement)atomicQueryElements.removeFirst();
            log.finest(se.toString());
            buffer.put((byte)se.type);
            switch(se.type){
                case 0 : {
                    buffer.put((byte)se.operator);
                    break;
                }
                case 1 : {
                    buffer.putShort((short)se.string.length());
                    buffer.put(se.string.getBytes());
                    break;
                }
                case 3: {
                    buffer.putInt(se.intvalue);
                    buffer.put((byte)se.relation);
                    buffer.putShort((short)se.metadata.length);
                    buffer.put(se.metadata);
                }
            /*
            //seq:={operator seq seq}|{data}
            //eg. or or string1 string2 string3
            //operators:
            //and
            buffer.put((byte)0);
            buffer.put((byte)0);
            //or
            buffer.put((byte)0);
            buffer.put((byte)1);
            //and not
            buffer.put((byte)0);
            buffer.put((byte)2);
            //data:
            //String
            buffer.put((byte)1);
            buffer.putShort((short)string.length());
            buffer.put(string.getBytes());
            //int metadata
            buffer.put((byte)3);
            buffer.putInt(value); //relation and metadataname follows!
            //relation := 1 for > (morethan) value := 2 for < (lessthan) value
            buffer.put((byte)relation);
            //metadataname if special length=1 and metadata only special TAG byte value
            buffer.putShort((short)metadata.length()); 
            buffer.put(metadata);
            //non int Meta tag
            buffer.put((byte)2);
            // and ?? Tag.append(buffer, byte type , String value);
            */
            }
        }
    }
    
}
