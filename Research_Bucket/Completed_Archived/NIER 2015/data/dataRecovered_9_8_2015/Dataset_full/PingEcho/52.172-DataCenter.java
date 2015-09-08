// $Id: DataCenter.java,v 1.1 2006/12/01 00:57:00 binetude Exp $

/*									tab:4
 * "Copyright (c) 2000-2003 The Regents of the University  of California.  
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
 * Copyright (c) 2002-2003 Intel Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached INTEL-LICENSE     
 * file. If you do not find these files, copies can be found by writing to
 * Intel Research Berkeley, 2150 Shattuck Avenue, Suite 1300, Berkeley, CA, 
 * 94704.  Attention:  Intel License Inquiry.
 */

/**
 * File: DataCenter.java
 *
 * @author <a href="mailto:binetude@cs.berkeley.edu">Sukun Kim</a>
 */

package net.tinyos.sentri;

import java.io.*;
import java.util.*;
import net.tinyos.util.*;
import net.tinyos.message.*;
import net.tinyos.straw.*;

public class DataCenter implements MessageListener {

  //  All in millisecond  //
  private static final short RADIUS_OF_INTERFERENCE = 5;
  private static final long MAX_RLB_TRY = 10;
  private static final long MAX_HOP_CNT = 50;
  private static final long SFTY_FCTR = 1000;

  private static final long RTT = (MAX_HOP_CNT * (2 + 1)
    + RADIUS_OF_INTERFERENCE) * 30 + 100;
  private static final long TIME_TO_ERASE_FLASH
    = SentriConsts.MAX_EEPROM_USAGE / 33 + SFTY_FCTR;
  private static final long TIME_BETWEEN_ERASE_START = SFTY_FCTR;
  private static final long TIME_UNTIL_START = MAX_RLB_TRY * RTT + SFTY_FCTR;
  private static final long TIME_AFTER_START = SFTY_FCTR;
 


  CmdP cmdP = new CmdP();
  TosP tosP = new TosP();
  Counter seqNo = new Counter("Sentri_seqNo.txt");
  int actlSeqNo;
  NodeList nodeList = new NodeList("Sentri_NodeList.txt");

  private MoteIF mote = new MoteIF(PrintStreamMessenger.err); {
    mote.registerListener(new ReplyMsg(), this);
    mote.registerListener(new UARTMsg(), this);
  }
  private CmdBcastMsg bcastMsg = new CmdBcastMsg();
  private CmdMsg cmdMsg = new CmdMsg(bcastMsg,
    SentriConsts.DIVERGENCE_HEADER_LENGTH);
  private boolean msgArrvd = true;

  Straw straw = new Straw();



  private int toPositive(byte inByte) {
    return inByte < 0 ? inByte + 256 : (int)inByte;
  }
 
  private int easyWait(long dur) {
    synchronized (this) {
      try {
        wait(dur);
      } catch (InterruptedException e) {
        System.out.println("EXCEPTION: DataCenter.easyWait");
	return 1;
      }
    }
    return 0;
  }



  private int sendMsg(short type) {
    cmdMsg.set_dest(cmdP.dest);
    cmdMsg.set_seqNo(0);
    cmdMsg.set_type(type);

    switch (type) {
      //  Empty  //
      case SentriConsts.LED_ON:
      case SentriConsts.LED_OFF:
      case SentriConsts.RESET:
      case SentriConsts.ERASE_FLASH:
      case SentriConsts.FIX_ROUTE:
      case SentriConsts.RELEASE_ROUTE:
      case SentriConsts.TIMESYNC_ON:
      case SentriConsts.TIMESYNC_OFF:
        break;

      //  elementaryShared  //
      case SentriConsts.PING_NODE:
      case SentriConsts.READ_PROFILE1:
      case SentriConsts.READ_PROFILE2:
      case SentriConsts.TIMESYNC_INFO:
      case SentriConsts.NETWORK_INFO:
        cmdMsg.set_args_es_toUART(cmdP.toUART);
        break;

      case SentriConsts.FIND_NODE:
        cmdMsg.set_args_fn_noOfNode((short)nodeList.noOfNode);
        for (int i = 0; i < nodeList.noOfNode; i++)
          cmdMsg.setElement_args_fn_nodes(i, nodeList.nodeNo[i]);
        break;
 
      //  sensingShared  //
      case SentriConsts.START_SENSING:
        cmdMsg.set_seqNo(actlSeqNo);
        cmdMsg.set_args_ss_nSamples(cmdP.nSamples);
        cmdMsg.set_args_ss_intrv(cmdP.intrv);
	cmdMsg.set_args_ss_chnlSelect(cmdP.chnlSelect);
	cmdMsg.set_args_ss_samplesToAvg(cmdP.samplesToAvg);
	cmdMsg.set_args_ss_startTime(tosP.tir.globalTime
	  + TIME_UNTIL_START * TosP.TICKS_PER_SECOND / 1000);
 
	String tempNn = null;
	if (cmdP.spclNm) tempNn = cmdP.nm;
	else tempNn = "s" + actlSeqNo;
	short tempLenOfNn = (short)tempNn.length();
        if (tempLenOfNn > SentriConsts.MAX_START_SENSING_NAME)
	  tempLenOfNn = SentriConsts.MAX_START_SENSING_NAME;

  	cmdMsg.set_args_ss_lenOfNm(tempLenOfNn);
        for (int i = 0; i < tempLenOfNn; i++)
          cmdMsg.setElement_args_ss_nm(i, (short)tempNn.charAt(i));
        break;

      case SentriConsts.FOR_DEBUG:
        cmdMsg.set_args_fd_toUART(cmdP.toUART);
        break;

      default:
        System.out.println("ERROR: DataCenter.sendMsg");
	return 1;
    }

    bcastMsg.set_seqno((short)0);

    System.out.println("[Send Msg] " + TosP.getString(cmdMsg));
    if (cmdP.verbose) {
      System.out.println("" + cmdMsg);
      System.out.println("" + bcastMsg);
    }
    try {
      mote.send(TosP.TOS_BCAST_ADDR, bcastMsg);
    } catch (IOException e) {
      System.out.println("EXCEPTION: DataCenter.sendMsg - mote.send failed");
      return 2;
    }
    return 0;
  }

  private boolean sendMsgGetReply(short type, long dur) {
    msgArrvd = false;
    sendMsg(type);
    easyWait(dur);
    if (!msgArrvd)
      System.out.println("Node " + cmdP.dest + " does not respond");
    else if ((cmdP.dest != TosP.TOS_BCAST_ADDR)
      && (tosP.src != cmdP.dest))
      System.out.println("ERROR: DataCenter.sendMsgGetReply"
        + " - dest = " + cmdP.dest + ", src = " + tosP.src);
    return msgArrvd;
  }

  private boolean sendMsgGetReplyRlb(short type) {
    int i;
    for (i = 0; i < MAX_RLB_TRY; i++)
      if (sendMsgGetReply(type, RTT)) break;
    if (i == MAX_RLB_TRY) {
      for (i = 0; i < MAX_RLB_TRY; i++)
        if (sendMsgGetReply(type, 2 * RTT)) break;
    }
    if (i == MAX_RLB_TRY) {
      for (i = 0; i < MAX_RLB_TRY; i++)
        if (sendMsgGetReply(type, 8 * RTT)) break;
    }
    if (i == MAX_RLB_TRY) {
      System.out.println("[Rlb] Node " + cmdP.dest + " does not respond");
      return false;
    } else {
      return true;
    }
  }
  
  //  only for Bcast  //
  private int sendMsgRlb(short type) {
    int returnResult = 1;
    for (int i = 0; i < MAX_RLB_TRY; i++) {
      if (sendMsg(type) == 0) returnResult = 0;
      easyWait(RTT / 2);
    }
    return returnResult;
  }



  public void messageReceived(int src_node, Message msg) {
    ReplyMsg rm = new ReplyMsg(msg, SentriConsts.CONVERGENCE_HEADER_LENGTH);
    tosP.parse(rm);
    System.out.print("[Receive Msg] ");
    System.out.println(tosP);
    if (cmdP.verbose) {
      System.out.println("" + rm);
//      System.out.println("" + (MultihopMsg)msg);
    }
    if (msgArrvd) System.out.println("ERROR: DataCenter.messageReceived"
      + " - unexpected packet");
    msgArrvd = true;
    synchronized (this) {
      notifyAll();
    }
  }



  private int nodeList() {
    nodeList.loadNodeList();
    for (int i = 0; i < nodeList.noOfNode; i++) {
      cmdP.dest = nodeList.nodeNo[i];
      nodeList.nodeVld[i] = sendMsgGetReplyRlb(SentriConsts.PING_NODE);
    }

    nodeList.compactNodeList();
    cmdP.dest = TosP.TOS_BCAST_ADDR;
    while (true) {
      if (sendMsgGetReplyRlb(SentriConsts.FIND_NODE)) {
        nodeList.nodeNo[nodeList.noOfNode] = tosP.src;
        nodeList.nodeVld[nodeList.noOfNode] = true;
        ++nodeList.noOfNode;
      } else {
        break;
      }
    }
    nodeList.saveNodeList();
    nodeList.dumpLodeList();
    return 0;
  }

  private int startSensing() {
    if (cmdP.broadcasting) {
      cmdP.dest = 0;
    }
    if (!sendMsgGetReplyRlb(SentriConsts.TIMESYNC_INFO)) return 1;
    if (cmdP.broadcasting) cmdP.dest = TosP.TOS_BCAST_ADDR;

    actlSeqNo = seqNo.get();
    seqNo.incr();
    sendMsgRlb(SentriConsts.START_SENSING);
    Date sampleTime = new Date();
    System.out.println(sampleTime);
    easyWait((long)(TIME_UNTIL_START
	  + cmdP.nSamples * cmdP.intrv * cmdP.samplesToAvg / 1000
	  + TIME_AFTER_START));
    return 0;
  }

  private int eraseStart() {
    sendMsgRlb(SentriConsts.ERASE_FLASH);
    easyWait(TIME_TO_ERASE_FLASH);
    easyWait(TIME_BETWEEN_ERASE_START);
    return startSensing(); 
  }

  private int readProfile() {
    if (cmdP.broadcasting) {
      nodeList.loadNodeList();
    } else {
      nodeList.noOfNode = 1;
      nodeList.nodeNo[0]  = cmdP.dest;
      nodeList.nodeVld[0] = true;
    }
    for (int i = 0; i < nodeList.noOfNode; i++) {
      cmdP.dest = nodeList.nodeNo[i];
      if (sendMsgGetReplyRlb(SentriConsts.READ_PROFILE1))
        sendMsgGetReplyRlb(SentriConsts.READ_PROFILE2);
    }
    if (cmdP.broadcasting) cmdP.dest = TosP.TOS_BCAST_ADDR;
    return 0;
  }

  private int readData() {
    if (cmdP.broadcasting) {
      nodeList.loadNodeList();
    } else {
      nodeList.noOfNode = 1;
      nodeList.nodeNo[0]  = cmdP.dest;
      nodeList.nodeVld[0] = true;
    }

    for (int i = 0; i < nodeList.noOfNode; i++) {
    
      cmdP.dest = nodeList.nodeNo[i];
      if (!sendMsgGetReplyRlb(SentriConsts.READ_PROFILE1) ||
        !sendMsgGetReplyRlb(SentriConsts.READ_PROFILE2)) {
	return 1;
      }

      if (tosP.rp1r.integrity != 2) return 2;


      //sendMsgRlb(SentriConsts.TIMESYNC_OFF);
      straw.toUART = cmdP.toUART;
      straw.verbose = (short) (cmdP.verbose ? 5 : 2);
      long rawDataSize = tosP.rp1r.nSamples * tosP.rp1r.noOfChnl * 2;
      byte[] rawData = new byte[(int)rawDataSize];
//      if (cmdP.toUART != 0 || rawDataSize <= (long)600000) {
      int tmpResult = straw.read(cmdP.dest, SentriConsts.STRAW_DATA_ID, 0,
        rawDataSize, rawData);
      if (tmpResult != 0)
	return tmpResult + 10;
/*      } else {  //  To prevent blacking out routing layer  //
        straw.read(cmdP.dest, SentriConsts.STRAW_DATA_ID, rawDataSize / 2,
	  rawDataSize / 2, rawData);
        for (int j = 0; j < rawDataSize / 2; j++) {
          rawData[(int)(rawDataSize / 2 + j)] = rawData[j];
        }
	
	for (int j = 0; j < 6; j++) {
	  easyWait(30000);
          sendMsgGetReply(SentriConsts.PING_NODE);
	}
	
        straw.read(cmdP.dest, SentriConsts.STRAW_DATA_ID, 0, rawDataSize / 2,
	  rawData);
      }*/
      //for (int l = 0; l < rawDataSize; l++) {
      //  System.out.print((int)(rawData[l]) + " ");
      //}
      //System.out.println("");
      //sendMsgRlb(SentriConsts.TIMESYNC_ON);


      int chnlIndex = 0;
      for (int j = 0; j < SentriConsts.MAX_CHANNEL; j++) {
        if (!tosP.rp1r.chnlSelectVector[j]) continue;
	FileOutputStream fos = null;
	PrintWriter pr = null;
	try {
  	  fos = new FileOutputStream(tosP.rp2r.nm + "_n" + cmdP.dest
	    + "_c" + (j + 1) + ".txt");
	  pr = new PrintWriter(fos);
	} catch (IOException e) {
          System.out.println("EXCEPTION: DataCenter.transferData - open");
	}

	for (int k = 0; k < tosP.rp1r.nSamples; k++) {
	  int dataIndex = (k * tosP.rp1r.noOfChnl + chnlIndex) * 2;
	  int intprdData = toPositive(rawData[dataIndex])
	    + (toPositive(rawData[dataIndex + 1]) << 8);
	  pr.println((k + 1) + " " + intprdData);
	}

	try {
	  pr.close();
	  fos.close();
	} catch (IOException e) {
          System.out.println("EXCEPTION: DataCenter.transferData - close");
	}
	++chnlIndex;
      }

    }
    if (cmdP.broadcasting) cmdP.dest = TosP.TOS_BCAST_ADDR;
    return 0;
  }

  private int forDebug() {
    return sendMsgGetReply(SentriConsts.FOR_DEBUG, RTT) ? 0 : 1;
  }



  public int execute(String[] args) {
    if (cmdP.parse(args) != 0) return -1;
 
    switch (cmdP.cmd) {
      case Cmd.LEDON:
        return sendMsg(SentriConsts.LED_ON);
      case Cmd.LEDOFF:
        return sendMsg(SentriConsts.LED_OFF);

      case Cmd.PINGNODE:
        return sendMsgGetReply(SentriConsts.PING_NODE, RTT) ? 0 : 1;
      case Cmd.NODELIST:
        return nodeList();

      case Cmd.RESET:
        return sendMsgRlb(SentriConsts.RESET);
      case Cmd.ERASEFLASH:
        sendMsgRlb(SentriConsts.ERASE_FLASH);
        easyWait(TIME_TO_ERASE_FLASH);
	return 0;
      case Cmd.STARTSENSING:
        return startSensing();
      case Cmd.ERASESTART:
        return eraseStart();

      case Cmd.READPROFILE:
        return readProfile();
      case Cmd.READDATA:
        return readData();

      case Cmd.RANDOMREAD:
        System.out.println("Deprecated");
	return 0;
      case Cmd.TIMESYNCINFO:
        return sendMsgGetReplyRlb(SentriConsts.TIMESYNC_INFO) ? 0 : 1;
      case Cmd.NETWORKINFO:
        return sendMsgGetReplyRlb(SentriConsts.NETWORK_INFO) ? 0 : 1;

      case Cmd.FIXROUTE:
        return sendMsgRlb(SentriConsts.FIX_ROUTE);
      case Cmd.RELEASEROUTE:
        return sendMsgRlb(SentriConsts.RELEASE_ROUTE);
	
      case Cmd.FORDEBUG:
        return forDebug();

      case Cmd.RESETBCSEQNO:
        System.out.println("Deprecated");
	return 0;
      case Cmd.HELP:
        return 0;

      default:
        System.out.println("ERROR: DataCenter.execute");
	return 0;
    }
  }

  public static void main(String[] args) {
    DataCenter dc = new DataCenter();
    System.exit(dc.execute(args));
  }
 
};
 
