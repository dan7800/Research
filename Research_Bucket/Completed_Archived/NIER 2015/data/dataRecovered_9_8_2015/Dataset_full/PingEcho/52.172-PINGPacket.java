/*									tab:2
 *
 *
 * "Copyright (c) 2000 and The Regents of the University 
 * of California.  All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice and the following
 * two paragraphs appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS."
 *
 * Authors:		Phil Levis
 *
 *
 */

package net.tinyos.packet;


public class PINGPacket extends AMPacket {

    public short packetField_PING_dest;
    public short packetField_PING_source;
    public byte packetField_PING_sequence;
    public byte packetField_PING_response;

    public PINGPacket() {
	super();
	try {
	    setOneByteField("AM_type", (byte)1);
	}
	catch (Exception e) {
	    System.err.println("Failed to initialize AM type of PING packet to 0x07.");
	}
    }

    public int headerLength() {return super.headerLength() + 6;}

    public short pingSource() {return packetField_PING_source;}
    public short pingDest() {return packetField_PING_dest;}
    public byte pingSequence() {return packetField_PING_sequence;}
    public byte pingIsResponse() {return packetField_PING_response;}
    
    
    public void initialize(byte[] packet) {
	super.initialize(packet);
	int offset = super.headerLength();

	packetField_PING_source = ArrayPackerLE.getShort(packet, offset + 0);
	packetField_PING_dest = ArrayPackerLE.getShort(packet, offset + 2);
	packetField_PING_sequence = ArrayPackerLE.getByte(packet, offset + 4);
	packetField_PING_response = ArrayPackerLE.getByte(packet, offset + 5);

	int headerLen = headerLength();

	try {
	    setOneByteField("AM_type", (byte)1);
	}
	catch (Exception exception) {
	    exception.printStackTrace();
	}
    }
	   

    public byte[] toByteArray() {
	byte[] packet = super.toByteArray();
	int offset = super.headerLength();

	ArrayPackerLE.putShort(packet, offset + 0, packetField_PING_source);
	ArrayPackerLE.putShort(packet, offset + 2, packetField_PING_dest);
	ArrayPackerLE.putByte(packet, offset + 4, packetField_PING_sequence);
	ArrayPackerLE.putByte(packet, offset + 5, packetField_PING_response);

	return packet;
    }

    public static void main(String[] args) throws Exception {
	byte[] msg  = {
		
	    (byte)0xfa, (byte)0xce, (byte)0x07, (byte)0x13,
	    (byte)0x01, (byte)0x00, (byte)0x03, (byte)0x00,
	    (byte)0x00, (byte)0x07, (byte)0x0a, (byte)0xef,
	    (byte)0xde, (byte)0xad, (byte)0xbe, (byte)0xef,
	    (byte)0xde, (byte)0xad, (byte)0xbe, (byte)0xef,
	    (byte)0xde, (byte)0xad, (byte)0xbe, (byte)0xef,
	    (byte)0xde, (byte)0xad, (byte)0xbe, (byte)0xef,
	    (byte)0xde, (byte)0xad, (byte)0xbe, (byte)0xef,
	    (byte)0xde, (byte)0xad, (byte)0x00, (byte)0x00};


	PINGPacket packet = new PINGPacket();
	packet.initialize(msg);

	String[] fields;
	
	System.out.println("Byte fields:");
	fields = packet.getByteFieldNames();
	for (int i = 0; i < fields.length; i++) {
	    System.out.print(" o ");
	    System.out.println(fields[i]);
	}

	packet.setOneByteField("AM_group", (byte)0x66);
	System.out.println("Set AM group to 0x66.");
	System.out.println("Checking: group is 0x" + Integer.toHexString(packet.group()));

	System.out.println("Checking: AM dest is 0x" + Integer.toHexString(packet.destination() & 0xffff));
	
	packet.setTwoByteField("PING_source", (short)0xe1fe);
	System.out.println("Set ping source to 0xe1fe.");
	System.out.println("Checking: source is 0x" + Integer.toHexString((packet.pingSource()) & 0xffff));

	
	System.out.println("Two byte fields:");
	fields = packet.getTwoByteFieldNames();
	for (int i = 0; i < fields.length; i++) {
	    System.out.print(" o ");
	    System.out.println(fields[i]);
	}


	System.out.println("Checking binary structure of packet.");
	byte[] data = packet.toByteArray();
	System.out.println("Packet:");
	System.out.println(TOSPacket.dataToString(data));
    }
}
