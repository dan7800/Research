// $Id: Downloader.java,v 1.1 2005/07/22 17:52:37 jwhui Exp $

/*									tab:2
 *
 *
 * "Copyright (c) 2000-2005 The Regents of the University  of California.  
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice, the following
 * two paragraphs and the author appear in all copies of this software.
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
 */

/**
 * @author Jonathan Hui <jwhui@cs.berkeley.edu>
 */

package net.tinyos.deluge;

import net.tinyos.message.*;
import java.io.*;

public class Downloader implements MessageListener {

  private static final int PAGE_SIZE = DelugeConsts.DELUGE_PKTS_PER_PAGE*DelugeConsts.DELUGE_PKT_PAYLOAD_SIZE;
  private static final int MAX_REQ_ATTEMPTS = 4;

  private Pinger pinger;
  private DelugeAdvMsg pingReply;
  private MoteIF moteif;
  private TOSBootImage tosBootImage;
  private boolean verbose;
  private short pktsToReceive[] = new short[DelugeReqMsg.totalSize_requestedPkts()];
  private int pktsReceived;
  private short imageData[] = new short[60*PAGE_SIZE];
  private short curPage;
  private String outfile;

  private int reqAttempts;
  private int reqDest;

  public Downloader(Pinger pinger, int imageNum,
		    MoteIF moteif, boolean verbose,
		    String outfile) {

    if (imageNum < 0 || imageNum >= pinger.getNumImages()) {
      throw new IllegalArgumentException( "invalid image number" );
    }
      
    if (outfile == "") {
      throw new IllegalArgumentException(
	"No outfile specified.");
    }
    
    this.pinger = pinger;
    this.pingReply = pinger.getPingReply(imageNum);
    this.moteif = moteif;
    this.tosBootImage = pinger.getImage(imageNum);
    this.verbose = verbose;
    this.outfile = outfile;

  }

  public void extract() {

    if (pingReply.get_imgDesc_numPgsComplete() == 0) {
      throw new IllegalArgumentException(
	"Image " + pingReply.get_imgDesc_imgNum() + " is empty.");
    }

    System.out.println("Download image:");
    System.out.println("  Image: " + pingReply.get_imgDesc_imgNum());
    System.out.println(tosBootImage);
    
    setupNextPage();
    reqDest = pingReply.get_sourceAddr();
    reqAttempts = MAX_REQ_ATTEMPTS;
    curPage = 0;

    moteif.registerListener(new DelugeAdvMsg(), this);
    moteif.registerListener(new DelugeDataMsg(), this);

    for (;;) {
      try {
	Thread.currentThread().sleep(600);

	if ( curPage >= pingReply.get_imgDesc_numPgs() )
	  break;

	System.out.print("\rDownloading page [" + (curPage+1) + "] of [" + pingReply.get_imgDesc_numPgs() + "] ...");

	if ( reqAttempts == 0 ) {
	  DelugeAdvMsg advMsg = (DelugeAdvMsg)pingReply.clone();
	  advMsg.set_sourceAddr(pinger.getPCAddr());
	  advMsg.set_version(DelugeConsts.DELUGE_VERSION);
	  advMsg.set_type(DelugeConsts.DELUGE_ADV_PING);
	  advMsg = DelugeCrc.computeAdvCrc(advMsg);
	  if (verbose) System.out.print(advMsg);
	  send(advMsg);
	  reqDest = MoteIF.TOS_BCAST_ADDR;
	}
	else {
	  DelugeReqMsg reqMsg = new DelugeReqMsg();
	  reqMsg.set_sourceAddr(pinger.getPCAddr());
	  reqMsg.set_dest(reqDest);
	  reqMsg.set_vNum(pingReply.get_imgDesc_vNum());
	  reqMsg.set_imgNum(pingReply.get_imgDesc_imgNum());
	  reqMsg.set_pgNum(curPage);
	  reqMsg.set_requestedPkts(pktsToReceive);
	  if (verbose) System.out.print(reqMsg);
	  send(reqMsg);
	  reqAttempts--;
	}
      } catch (Exception e) {
	e.printStackTrace();
      }
    }

    System.out.println();
    
    byte[] bytes = new byte[TOSBootImage.METADATA_SIZE];
    for ( int i = 0; i < bytes.length; i++ )
      bytes[i] = (byte)(imageData[i+256] & 0xff);
    TOSBootImage receivedImage = new TOSBootImage(bytes);
    
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(outfile));
      out.write("<tos_image>\n");
      out.write("  <ident>\n");
      out.write("    <program_name>" + receivedImage.getName() + "</program_name>\n");
      out.write("    <unix_time>" + Long.toHexString(receivedImage.getUnixTime()).toUpperCase() + "L</unix_time>\n");
      out.write("    <platform>" + receivedImage.getPlatform() + "</platform>\n");
      out.write("    <deluge_support>" + (receivedImage.getDelugeSupport() ? "yes" : "no") + "</deluge_support>\n");
      out.write("    <user_id>" + receivedImage.getUserID() + "</user_id>\n");
      out.write("    <hostname>" + receivedImage.getHostname() + "</hostname>\n");
      out.write("    <user_hash>" + Long.toHexString(receivedImage.getUserHash()).toUpperCase() + "L</user_hash>\n");
      out.write("    <uid_hash>" + Long.toHexString(receivedImage.getUIDHash()).toUpperCase() + "L</uid_hash>\n");
      out.write("  </ident>\n");
      out.write("  <image format=\"ihex\">\n");
      
      int curOffset = 256 + TOSBootImage.METADATA_SIZE;
      int addr = 0;
      int length = 0;
      
      for ( int i = 0; i < 4; i++ )
	addr |= (imageData[curOffset++] & 0xff) << i*8;
      for ( int i = 0; i < 4; i++ )
	length |= (imageData[curOffset++] & 0xff) << i*8;
      
      byte record[] = new byte[21];
      
      while ( length > 0 ) {
	if (length >= 16)
	  record[0] = 16;
	else
	  record[0] = (byte)length;
	
	record[1] = (byte)((addr >> 8) & 0xff);
	record[2] = (byte)(addr & 0xff);
	record[3] = 0;
	for ( int i = 0; i < record[0]; i++, curOffset++ )
	  record[4+i] = (byte)imageData[curOffset];
	int checkSum = 0;
	for ( int i = 0; i < 4 + record[0]; i++ )
	  checkSum += record[i];
	record[4+record[0]] = (byte)((~(checkSum & 0xff) + 1) & 0xff);
	
	out.write(":");
	for ( int i = 0; i < 5 + record[0]; i++ )
	  printByte(out, record[i], true);
	out.write("\n");
	
	addr += record[0];
	length -= record[0];
	
	if (length == 0) {
	  addr = length = 0;
	  for ( int i = 0; i < 4; i++ )
	    addr |= (imageData[curOffset++] & 0xff) << i*8;
	  for ( int i = 0; i < 4; i++ )
	    length |= (imageData[curOffset++] & 0xff) << i*8;
	}
      }
      
      out.write(":0400000300005000A9\n");
      out.write(":00000001FF\n");

      out.write("  </image>\n");
      
      addr = length = 0;
      for ( int i = 0; i < 4; i++ )
	addr |= (imageData[curOffset++] & 0xff) << i*8;
      for ( int i = 0; i < 4; i++ )
	length |= (imageData[curOffset++] & 0xff) << i*8;

      if ( length > 0 ) {
	out.write("  <supplement format=\"hex\">\n");
	int hexOffset = 0;
	for ( int i = 0; i < length; i++ ) {
	  printByte(out, (byte)imageData[curOffset++], false);
	  if ((++hexOffset % 32) == 0)
	    out.write("\n");
	}
	if ((++hexOffset % 32) != 0)
	  out.write("\n");
	out.write("  </supplement>\n");
      }

      out.write("</tos_image>\n");
      out.close();
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    
  }

  private void printByte(BufferedWriter out, byte byteVal, boolean uppercase) {
    try {
      if (byteVal >= 0 && byteVal < 16)
	out.write("0");
      String tmpStr = Integer.toHexString(byteVal & 0xff);
      if (uppercase)
	tmpStr = tmpStr.toUpperCase();
      out.write( tmpStr );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setupNextPage() {

    for ( int i = 0; i < pktsToReceive.length; i++ )
      pktsToReceive[i] = 0xff;
    pktsReceived = 0;

  }

  private void send(Message m) {
    try {
      moteif.send(MoteIF.TOS_BCAST_ADDR, m);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void messageReceived(int to, Message m) {

    switch(m.amType()) {

    case DelugeAdvMsg.AM_TYPE:

      DelugeAdvMsg adv = (DelugeAdvMsg)m;

      if ( verbose ) System.out.print(adv);

      if ( adv.get_type() != DelugeConsts.DELUGE_ADV_NORMAL )
	return;

      if ( reqDest == MoteIF.TOS_BCAST_ADDR
	   && adv.get_imgDesc_vNum() == pingReply.get_imgDesc_vNum()
	   && adv.get_imgDesc_numPgsComplete() > curPage ) {
	reqDest = adv.get_sourceAddr();
	reqAttempts = MAX_REQ_ATTEMPTS;
      }
      
      break;

    case DelugeDataMsg.AM_TYPE:

      DelugeDataMsg data = (DelugeDataMsg)m;
      short pgNum = data.get_pgNum();
      short pktNum = data.get_pktNum();

      if ( data.get_imgNum() == pingReply.get_imgDesc_imgNum()
	   && data.get_vNum() == pingReply.get_imgDesc_vNum()
	   && data.get_pgNum() == curPage ) {

	reqAttempts = MAX_REQ_ATTEMPTS;

	if (verbose) System.out.print(data);
	
	if ((pktsToReceive[pktNum/8] & (0x1 << (pktNum%8))) != 0) {
	  
	  pktsToReceive[pktNum/8] &= ~(0x1 << (pktNum%8));
	  pktsReceived++;
	  
	  System.arraycopy(data.get_data(), 0, imageData,
			   pgNum*PAGE_SIZE + pktNum*DelugeConsts.DELUGE_PKT_PAYLOAD_SIZE,
			   DelugeConsts.DELUGE_PKT_PAYLOAD_SIZE);
	  
	  if (pktsReceived >= DelugeConsts.DELUGE_PKTS_PER_PAGE) {
	    setupNextPage();
	    curPage++;
	  }
	}

      }

      break;

    }

  }

}