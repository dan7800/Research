// $Id: Straw.java,v 1.6 2006/12/01 05:34:56 binetude Exp $

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
 * File: Straw.java
 *
 * @author <a href="mailto:binetude@cs.berkeley.edu">Sukun Kim</a>
 */

package net.tinyos.straw;

import java.io.*;
import java.util.*;
import net.tinyos.util.*;
import net.tinyos.message.*;

public class Straw implements MessageListener {

  //  StrawM.nc  // 
  private static final short RADIUS_OF_INTERFERENCE = 5;
  private static short UART_ONLY_DELAY
    = ((StrawConsts.STRAWREPLYMSG_LENGTH + 7) * 7 + 157) / 36;
  private static short UART_DELAY
    = ((StrawConsts.STRAWREPLYMSG_LENGTH + 7) * 7 + 85) / 36;
  private static short RADIO_DELAY
    //= ((StrawConsts.STRAWREPLYMSG_LENGTH + 7) * 7 + 234) / 19;//65;
    = ((StrawConsts.STRAWREPLYMSG_LENGTH + 7) * 7 + 234) / 50;



  private static final short MAX_SEND_TRY = 10;
  private static final short UNCERTAINTY = 1;
  private static final short MAX_HOP_CNT = 50;
  private static final short SFTY_FCTR = 1000;

  private static final short PAUSE_BETWEEN_STATES
    = MAX_HOP_CNT * 30 * (2 + UNCERTAINTY) + SFTY_FCTR;

  static final short TOS_BCAST_ADDR = (short)0xffff;


  //  Arguments  //
  private int dest;
  private short portId;
  private long start;
  private long size;
  private byte[] bffr;
  private int seqSize;

  public short toUART = 0; // You can directly access a mote
                    // (like through testbed)
  public short verbose = 5; // Set the amount of information for debugging
 

  //  Communication  //
  private MoteIF mote = new MoteIF(PrintStreamMessenger.err); {
    mote.registerListener(new StrawReplyMsg(), this);
    mote.registerListener(new StrawUARTMsg(), this);
  }
  private StrawBcastMsg bcastMsg = new StrawBcastMsg(); // only for Bcast
  private StrawCmdMsg cmdMsg = new StrawCmdMsg(bcastMsg,
    StrawConsts.DIVERGE_HEADER_LENGTH);
  
  private boolean msgArrvd;
  private int maxRTT;
  private int pktIntrv;
  
  //  For checking missing packets  //
  private boolean rcvdSeqNo[];
  private int lastRcvdSeqNo;
  
  private int msngSeqNoIndex;
  private int rrSeqNo[] = new int[StrawConsts.MAX_RANDOM_READ_SEQNO_SIZE];
  private int sizeOfRrSeqNo;

  //  For checking error  //
  private int checksum;
  private int tmpCheckSum;


  //  For statistics  //
  private long msgSent;
  private long msgRcvd;
  private Date timeOfMoment;
  private long endOfInit;
  private long endOfNi;
  private long endOfTd;
  private long endOfRr;
  private long endOfEc;
  private long successRate;
  
  //  Et Cetra  //
  private short state = StrawConsts.STRAW_IDLE_STATE;
  private int symbolChar;
  private int symbolLine;



  private int easyWait(int dur) {
    synchronized (this) {
      try {
        wait(dur);
      } catch (InterruptedException e) {
        System.out.println("EXCEPTION: Straw.easyWait");
	return 1;
      }
    }
    return 0;
  }



  private int sendMsg(short type) {
    cmdMsg.set_dest(dest);
    
    switch (type) {
    case StrawConsts.STRAW_NETWORK_INFO:
      cmdMsg.set_arg_ni_type(type);
      cmdMsg.set_arg_ni_toUART(toUART);
      break;
    case StrawConsts.STRAW_TRANSFER_DATA:
      cmdMsg.set_arg_td_type(type);
      cmdMsg.set_arg_td_portId(portId);
      cmdMsg.set_arg_td_start(start);
      cmdMsg.set_arg_td_size(size);
      
      cmdMsg.set_arg_td_uartOnlyDelay(UART_ONLY_DELAY);
      cmdMsg.set_arg_td_uartDelay(UART_DELAY);
      cmdMsg.set_arg_td_radioDelay(RADIO_DELAY);
      cmdMsg.set_arg_td_toUART(toUART);
      break;
    case StrawConsts.STRAW_RANDOM_READ:
      for (int i = 0; i < StrawConsts.MAX_RANDOM_READ_SEQNO_SIZE; i++) {
        if (rrSeqNo[i] == 0xffff) {
          cmdMsg.setElement_arg_rr_seqNo(i, StrawConsts.STRAW_RANDOM_READ);
	  break;
	}
        cmdMsg.setElement_arg_rr_seqNo(i, rrSeqNo[i]
	  + StrawConsts.STRAW_TYPE_SHIFT);
      }
      break;
    case StrawConsts.STRAW_ERR_CHK:
      cmdMsg.set_arg_ec_type(type);
      cmdMsg.set_arg_ec_toUART(toUART);
      break;
    default:
      System.out.println("ERROR: Straw.sendMsg");
      return 1;
    }

    bcastMsg.set_seqno((short)0);
    if (verbose >= 6) { System.out.print(cmdMsg); }
    else if (verbose >= 1) {
      printSymbol("S");
      if (verbose >= 4) System.out.print(cmdMsg.get_arg_cd_type() + "");
    }
    try {
      mote.send(TOS_BCAST_ADDR, bcastMsg);
      ++msgSent;
    } catch (IOException e) {
      System.out.println("EXCEPTION: Straw.sendMsg - mote.send failed");
      return 2;
    }
    return 0;
  }

  private boolean sendMsgGetReply(short type, int dur) {
    msgArrvd = false;
    sendMsg(type);
    easyWait(dur);
    if (!msgArrvd) {
      if (verbose >= 1) {
        printSymbol(".");
        if (verbose >= 4)
          System.out.println("Node " + dest + " does not respond");
      }
    }
    return msgArrvd;
  }

  private boolean sendMsgGetReplyRlb(short type) {
    int i;
    for (i = 0; i < MAX_SEND_TRY; i++)
      if (sendMsgGetReply(type, maxRTT)) break;
    if (i == MAX_SEND_TRY) {
      for (i = 0; i < MAX_SEND_TRY; i++)
        if (sendMsgGetReply(type, 2 * maxRTT)) break;
    }
    if (i == MAX_SEND_TRY) {
      for (i = 0; i < MAX_SEND_TRY; i++)
        if (sendMsgGetReply(type, 8 * maxRTT)) break;
    }
    if (i == MAX_SEND_TRY) {
      if (verbose >= 4)
        System.out.println("[Rlb] Node " + dest + " does not respond");
      return false;
    } else {
      return true;
    }
  }


  public void messageReceived(int src_node, Message msg) {
    ++msgRcvd;
    StrawReplyMsg reply;
    reply = new StrawReplyMsg(msg, StrawConsts.CONVERGE_HEADER_LENGTH);
    if (verbose >= 6) {
      System.out.print(reply);
    } else if (verbose >= 1) {
      printSymbol("R");
      if (verbose >= 4) System.out.print(reply.get_arg_cdr_type() + "");
    }
    
    if (reply.get_arg_cdr_type() == StrawConsts.STRAW_NETWORK_INFO_REPLY &&
      state == StrawConsts.STRAW_NETWORK_INFO) {
      //UART_ONLY_DELAY = (short)reply.get_arg_nir_uartOnlyDelay();
      //UART_DELAY = (short)reply.get_arg_nir_uartDelay();
      //RADIO_DELAY = (short)reply.get_arg_nir_radioDelay();
      if (toUART != 0) {
        maxRTT = UART_ONLY_DELAY * (2 + UNCERTAINTY)
	  + RADIUS_OF_INTERFERENCE * RADIO_DELAY + 10;
	pktIntrv = UART_ONLY_DELAY;
      } else {
        int depth = reply.get_arg_nir_depth();
        maxRTT = (UART_DELAY + depth * RADIO_DELAY) * (2 + UNCERTAINTY)
	  + RADIUS_OF_INTERFERENCE * RADIO_DELAY + 10;
        pktIntrv = depth < RADIUS_OF_INTERFERENCE
	  ? UART_DELAY + depth * RADIO_DELAY
	  : RADIUS_OF_INTERFERENCE * RADIO_DELAY;
      }
	  
    } else if (reply.get_arg_cdr_type() >= StrawConsts.STRAW_TYPE_SHIFT &&
      (state == StrawConsts.STRAW_TRANSFER_DATA
        || state == StrawConsts.STRAW_RANDOM_READ)) {
      lastRcvdSeqNo = reply.get_arg_dr_seqNo() - StrawConsts.STRAW_TYPE_SHIFT;
      int writingSize
        = (lastRcvdSeqNo + 1) * StrawConsts.MAX_DATA_REPLY_DATA_SIZE > size
        ? (int)(size - lastRcvdSeqNo * StrawConsts.MAX_DATA_REPLY_DATA_SIZE)
        : StrawConsts.MAX_DATA_REPLY_DATA_SIZE;
      for (int i = 0; i < writingSize; i++)
        bffr[lastRcvdSeqNo * StrawConsts.MAX_DATA_REPLY_DATA_SIZE + i]
	  = (byte)reply.getElement_arg_dr_data(i);
      rcvdSeqNo[lastRcvdSeqNo] = true;
      
    } else if (reply.get_arg_cdr_type() == StrawConsts.STRAW_ERR_CHK_REPLY &&
      state == StrawConsts.STRAW_ERR_CHK) {
      checksum = reply.get_arg_ecr_checksum();
    
    } else {
      System.out.println("\n" + "ERROR: Straw.messageReceived - invalid type");
      if (verbose >= 3) System.out.println("state = " + state
        + ", type = " + reply.get_arg_cdr_type());
      return;
    }
    
    msgArrvd = true;
    synchronized (this) {
      notifyAll();
    }
  }



  public int ping(int dest, short portId) {
    System.out.println("****  Ping  ****");
    if (state != StrawConsts.STRAW_IDLE_STATE) return 1;
    state = StrawConsts.STRAW_NETWORK_INFO;

    this.dest = dest;
    this.portId = portId;
    maxRTT = (UART_DELAY + 3 * RADIO_DELAY) * (2 + UNCERTAINTY)
      + RADIUS_OF_INTERFERENCE * RADIO_DELAY + 10 
      + 500;
    if (!sendMsgGetReply(StrawConsts.STRAW_NETWORK_INFO, maxRTT)) return 2;

    state = StrawConsts.STRAW_IDLE_STATE;
    return 0;
  }


  
  public int read(int dest, short portId, long start, long size, byte[] bffr) {
    System.out.println("****  Straw  ****");
    if (size == 0) { System.out.println("Straw Success: size = 0");
      return 0; }
    if (state != StrawConsts.STRAW_IDLE_STATE) return rptFail(1);

    this.dest = dest;
    this.portId = portId;
    this.start = start;
    this.size = size;
    this.bffr = bffr;
    seqSize = (int)((size + StrawConsts.MAX_DATA_REPLY_DATA_SIZE - 1)
      / StrawConsts.MAX_DATA_REPLY_DATA_SIZE);
    maxRTT = (UART_DELAY + 3 * RADIO_DELAY) * (2 + UNCERTAINTY)
      + RADIUS_OF_INTERFERENCE * RADIO_DELAY + 10 
      + 500;
 
    rcvdSeqNo = new boolean[seqSize];
    for (int i = 0; i < seqSize; i++) rcvdSeqNo[i] = false;

    msgSent = 0;
    msgRcvd = 0;

    symbolChar = 0;
    symbolLine = 1;
    
    timeOfMoment = new Date();
    endOfInit = timeOfMoment.getTime();


    //  Get network info  // 
    state = StrawConsts.STRAW_NETWORK_INFO;
    easyWait(PAUSE_BETWEEN_STATES);
    if (!sendMsgGetReplyRlb(StrawConsts.STRAW_NETWORK_INFO)) return rptFail(2);
    timeOfMoment = new Date();
    endOfNi = timeOfMoment.getTime();
    successRate = msgRcvd;


    //  Ask transfer of data  //
    state = StrawConsts.STRAW_TRANSFER_DATA;
    easyWait(PAUSE_BETWEEN_STATES);
    lastRcvdSeqNo = 0;
    
    if (!sendMsgGetReplyRlb(StrawConsts.STRAW_TRANSFER_DATA)) return rptFail(3);
    msgArrvd = true;
    while ((lastRcvdSeqNo < seqSize - 1) && msgArrvd) { // !last && !timeout
      msgArrvd = false;
      easyWait((seqSize - lastRcvdSeqNo - 1) * pktIntrv + pktIntrv / 2);
    }
    successRate = msgRcvd - successRate;
    if (verbose >= 2) System.out.println("\n" + getStatString());
    timeOfMoment = new Date();
    endOfTd = timeOfMoment.getTime();


    //  Fill missing holes using random read //
    state = StrawConsts.STRAW_RANDOM_READ;
    easyWait(PAUSE_BETWEEN_STATES);
    msngSeqNoIndex = 0;
    rrSeqNo[0] = 0xffff;
    
    while(hasMore()) {
      if(!sendMsgGetReplyRlb(StrawConsts.STRAW_RANDOM_READ)) return rptFail(4);
      int rrSeqNoIndex = 0;
      msgArrvd = true;
      while(!rcvdSeqNo[rrSeqNo[sizeOfRrSeqNo - 1]]
        && msgArrvd) { // !last && !timeout
        msgArrvd = false;
        easyWait((sizeOfRrSeqNo -  rrSeqNoIndex) * pktIntrv + pktIntrv / 2);
	if (msgArrvd) {
      	  for (rrSeqNoIndex = 0; rrSeqNoIndex < sizeOfRrSeqNo; rrSeqNoIndex++)
	    if (rrSeqNo[rrSeqNoIndex] == lastRcvdSeqNo) {
	      ++rrSeqNoIndex;
	      break;
	    }
	  if (rrSeqNoIndex == sizeOfRrSeqNo) {
	    rrSeqNoIndex = 0;
	  }
	}
      }
    }
    timeOfMoment = new Date();
    endOfRr = timeOfMoment.getTime();


    //  Error Check  //
    state = StrawConsts.STRAW_ERR_CHK;
    easyWait(PAUSE_BETWEEN_STATES);
    if (!sendMsgGetReplyRlb(StrawConsts.STRAW_ERR_CHK)) return rptFail(5);
    tmpCheckSum = 0;
    for (int i = 0; i < size; i++) {
      tmpCheckSum += (int)bffr[i] < 0 ? (int)bffr[i] + 256 : (int)bffr[i];
      tmpCheckSum %= 65536;
    }
    if (checksum != tmpCheckSum) return rptFail(6);
    timeOfMoment = new Date();
    endOfEc = timeOfMoment.getTime();
 

    //  Print Statistics  //
    state = StrawConsts.STRAW_IDLE_STATE;
    if (verbose >= 1) System.out.println("\n" + getPerfString());
    if (verbose >= 2) System.out.println(getStatString());
    System.out.println("Straw Success");
    return 0;
  }



  private boolean hasMore() {
    
    //  Compact rrSeqNo  //
    sizeOfRrSeqNo = 0;
    for (int i = 0; i < StrawConsts.MAX_RANDOM_READ_SEQNO_SIZE; i++) {
      if (rrSeqNo[i] == 0xffff) {
        break;
      } else if (!rcvdSeqNo[rrSeqNo[i]]) {
        rrSeqNo[sizeOfRrSeqNo] = rrSeqNo[i];
        ++sizeOfRrSeqNo;
      }
    }
    
    //  Fill rrSeqNo  //
    while (true) {
      if (sizeOfRrSeqNo == StrawConsts.MAX_RANDOM_READ_SEQNO_SIZE)
        // full rrSeqNo
        break;
      
      //  Find a new hole  //
      for (; msngSeqNoIndex < seqSize; msngSeqNoIndex++)
        if (!rcvdSeqNo[msngSeqNoIndex]) break;
	
      if (msngSeqNoIndex == seqSize) { // no more new hole
        break;
      } else { // found a new hole
        rrSeqNo[sizeOfRrSeqNo] = msngSeqNoIndex;
        ++msngSeqNoIndex;
	++sizeOfRrSeqNo;
      }
    }

    //  Wrap up and return  //
    if (verbose >= 3 && sizeOfRrSeqNo > 0) {
        System.out.println("#" + rrSeqNo[sizeOfRrSeqNo - 1] + "#");
    }
    if (sizeOfRrSeqNo == 0) { // no missing hole
      return false;
    } else if (sizeOfRrSeqNo < StrawConsts.MAX_RANDOM_READ_SEQNO_SIZE) {
      // Partial rrSeqNo
      rrSeqNo[sizeOfRrSeqNo] = 0xffff;
      return true;
    } else { // full rrSeqNo
      return true;
    }
  }



  private String getStatString() {
    return "msgSent = " + msgSent + ", msgRcvd = " + msgRcvd
      + ", successRate = " + ((double)successRate / (double)seqSize)
      + " (" + successRate + " / " + seqSize + ")" + "\n"
	    
      + "checksum = " + checksum
      + ", tmpCheckSum = " + tmpCheckSum + "\n"
      
      + "Ni = " + (endOfNi - endOfInit)
      + ", Td = " + (endOfTd - endOfNi) + ", Rr = " + (endOfRr - endOfTd)
      + ", Ec = " + (endOfEc - endOfRr)
      + ", total = " + (endOfEc - endOfInit) + "\n";
  }
  
  private String getPerfString() {
    double latency = (double)(endOfEc - endOfInit) / 1000;
    double bandwidth = (double)size / latency;
    double chnlCpcty = ((double)seqSize / (double)(endOfTd - endOfNi)) * 1000
      * ((double)successRate / (double)seqSize)
      * StrawReplyMsg.DEFAULT_MESSAGE_SIZE;
    chnlCpcty = 106 * 22;
    return "Bandwidth = " + bandwidth + " (B/s)\n"
      + "Latency = " + latency + " (s)\n";
      //+ "Channel Utilization = " + (bandwidth * 100 / chnlCpcty) + " (%)\n";
  }

  private int rptFail(int errCd) {
    state = StrawConsts.STRAW_IDLE_STATE;
    System.out.println("Straw Fail: " + errCd);
    if (verbose >= 3) System.out.println("\n" + getPerfString());
    if (verbose >= 3) System.out.println(getStatString());
    return errCd;
  }

  private void printSymbol(String outSymbol) {
    if (symbolChar == 0) {
      if (symbolLine < 100) System.out.print(" ");
      if (symbolLine < 10) System.out.print(" ");
      System.out.print(symbolLine + ":");
    }
    System.out.print(outSymbol);
    symbolChar++;
    if (symbolChar >= 70) {
      symbolChar = 0;
      symbolLine++;
      System.out.println("");
    }
  }

};

