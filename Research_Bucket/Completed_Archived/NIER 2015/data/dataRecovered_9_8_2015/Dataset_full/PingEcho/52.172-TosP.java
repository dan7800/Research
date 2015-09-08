// $Id: TosP.java,v 1.1 2006/12/01 00:57:00 binetude Exp $

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
 * File: TosP.java
 *
 * @author <a href="mailto:binetude@cs.berkeley.edu">Sukun Kim</a>
 */

package net.tinyos.sentri;

class TosP {

  //  TOS related constants  //
  static final long TICKS_PER_SECOND = 921600;
  static final short TOS_BCAST_ADDR = (short)0xffff;



  class readProfile1Reply {
    int seqNo;
    long nSamples;
    long intrv;
    short chnlSelect;
    int samplesToAvg;
    long startTime;
    short integrity;
    
    int noOfChnl;
    boolean[] chnlSelectVector = new boolean[SentriConsts.MAX_CHANNEL];
  };

  class readProfile2Reply {
    short lenOfNm;
    String nm;
  };

  class timesyncInfoReply {
    long sysTime;
    long localTime;
    long globalTime;
  };

  class networkInfoReply {
    int parent;
    int treeParent;
    short depth;
    short treeDepth;
    short occupancy;
    short quality;
    short fixedRoute;
  };

  class forDebugReply {
    short type;
    int[] data = new int[SentriConsts.MAX_FOR_DEBUG_REPLY_DATA];
  };



  int src;
  int type;

  readProfile1Reply rp1r = new readProfile1Reply();
  readProfile2Reply rp2r = new readProfile2Reply();

  timesyncInfoReply tir = new timesyncInfoReply();
  networkInfoReply nir = new networkInfoReply();

  forDebugReply fdr = new forDebugReply();



  int parse(ReplyMsg reply) {
    src = reply.get_src();
    type = reply.get_type();
    switch (type) {
    case SentriConsts.PING_NODE_REPLY:
    case SentriConsts.FIND_NODE_REPLY:
      break;
      
    case SentriConsts.READ_PROFILE1_REPLY:
      rp1r.seqNo = reply.get_args_rp1r_seqNo();
      rp1r.nSamples = reply.get_args_rp1r_nSamples();
      rp1r.intrv = reply.get_args_rp1r_intrv();
      rp1r.chnlSelect = reply.get_args_rp1r_chnlSelect();
      rp1r.samplesToAvg = reply.get_args_rp1r_samplesToAvg();
      rp1r.startTime = reply.get_args_rp1r_startTime();
      rp1r.integrity = reply.get_args_rp1r_integrity();
      rp1r.noOfChnl = 0;
      short tempChnlSelect = rp1r.chnlSelect;
      for (int i = 0; i < SentriConsts.MAX_CHANNEL; i++) {
        if (tempChnlSelect % 2 == 1) {
	  rp1r.chnlSelectVector[i] = true;
	  ++rp1r.noOfChnl;
	} else {
          rp1r.chnlSelectVector[i] = false;
	}
	tempChnlSelect >>= 1;
      }
      break;
    case SentriConsts.READ_PROFILE2_REPLY:
      rp2r.lenOfNm = reply.get_args_rp2r_lenOfNm();
      if (rp2r.lenOfNm > SentriConsts.MAX_READ_PROFILE2_REPLY_NAME)
        rp2r.lenOfNm = SentriConsts.MAX_READ_PROFILE2_REPLY_NAME;
      rp2r.nm = "";
      for (int i = 0; i < rp2r.lenOfNm; i++)
        rp2r.nm += "" + (char)reply.getElement_args_rp2r_nm(i);
      break;
      
    case SentriConsts.TIMESYNC_INFO_REPLY:
      tir.sysTime = reply.get_args_tir_sysTime();
      tir.localTime = reply.get_args_tir_localTime();
      tir.globalTime = reply.get_args_tir_globalTime();
      break;
    case SentriConsts.NETWORK_INFO_REPLY:
      nir.parent = reply.get_args_nir_parent();
      nir.treeParent = reply.get_args_nir_treeParent();
      nir.depth = reply.get_args_nir_depth();
      nir.treeDepth = reply.get_args_nir_treeDepth();
      nir.occupancy = reply.get_args_nir_occupancy();
      nir.quality = reply.get_args_nir_quality();
      nir.fixedRoute = reply.get_args_nir_fixedRoute();
      break;

    case SentriConsts.FOR_DEBUG_REPLY:
      fdr.type = reply.get_args_fdr_type();
      for (int i = 0; i < SentriConsts.MAX_FOR_DEBUG_REPLY_DATA; i++)
        fdr.data[i] = reply.getElement_args_fdr_data(i);
      break;
    
    case SentriConsts.ERROR_REPLY:
      break;
      
    default:
      System.out.println("ERROR: TosP.parse");
      break;
    }
    return 0;
  }

  public String toString() {
    String outString = "src = " + src + ", ";
    switch (type) {
      case SentriConsts.PING_NODE_REPLY:
        outString += "PING_NODE_REPLY";
	break;
      case SentriConsts.FIND_NODE_REPLY:
        outString += "FIND_NODE_REPLY";
        break;
	
      case SentriConsts.READ_PROFILE1_REPLY:
        outString += "READ_PROFILE1_REPLY\n";
	outString += "seqNo = " + rp1r.seqNo
	  + ", nSamples = " + rp1r.nSamples
	  + ", intrv = " + rp1r.intrv + "\n"
	  + "chnlSelect = " + rp1r.chnlSelect
	  + ", samplesToAvg = " + rp1r.samplesToAvg + "\n"
	  + "startTime = " + rp1r.startTime
	  + ", integrity = " + rp1r.integrity;
        break;
      case SentriConsts.READ_PROFILE2_REPLY:
        outString += "READ_PROFILE2_REPLY\n";
	outString += "lenOfNm = " + rp2r.lenOfNm
	  + ", nm= " + rp2r.nm;
        break;
	
      case SentriConsts.TIMESYNC_INFO_REPLY:
        outString += "TIMESYNC_INFO_REPLY\n";
	outString += "sysTime = " + tir.sysTime
	  + ", localTime = " + tir.localTime
	  + ", globalTime = " + tir.globalTime;
        break;
      case SentriConsts.NETWORK_INFO_REPLY:
        outString += "NETWORK_INFO_REPLY\n";
	outString += "parent = " + nir.parent
	  + ", treeParent = " + nir.treeParent
	  + ", depth = " + nir.depth + ", treeDepth = " + nir.treeDepth + "\n"
	  + "occupancy = " + nir.occupancy + ", quality = " + nir.quality
	  + ", fixedRoute = " + nir.fixedRoute;
        break;
	
      case SentriConsts.FOR_DEBUG_REPLY:
        outString += "FOR_DEBUG_REPLY\n";
	outString += "type = " + fdr.type + "\n";
	outString += "data = ";
	for (int i = 0; i < SentriConsts.MAX_FOR_DEBUG_REPLY_DATA; i++)
	  outString += fdr.data[i] + " ";
        break;
      case SentriConsts.ERROR_REPLY:
        outString += "ERROR_REPLY";
        break;
      default:
        outString = "Invalid type: " + type;
        break;
    }
    outString += "\n";
    return outString;
  }



  static String getString(CmdMsg cmdMsg) {
    String outString = "dest = ";
    outString += ((short)cmdMsg.get_dest() == TOS_BCAST_ADDR)
      ? "BC" : "" + cmdMsg.get_dest();
    outString += ", seqNo = " + cmdMsg.get_seqNo() + ", type = ";
    switch (cmdMsg.get_type()) {
    case SentriConsts.LED_ON: outString += "LED_ON"; break;
    case SentriConsts.LED_OFF: outString += "LED_OFF"; break;
  
    case SentriConsts.PING_NODE: outString += "PING_NODE"; break;
    case SentriConsts.FIND_NODE: outString += "FIND_NODE"; break;
  
    case SentriConsts.RESET: outString += "RESET"; break;
    case SentriConsts.ERASE_FLASH: outString += "ERASE_FLASH"; break;
    case SentriConsts.START_SENSING: outString += "START_SENSING"; break;
  
    case SentriConsts.READ_PROFILE1: outString += "READ_PROFILE1"; break;
    case SentriConsts.READ_PROFILE2: outString += "READ_PROFILE2"; break;
  
    case SentriConsts.TIMESYNC_INFO: outString += "TIMESYNC_INFO"; break;
    case SentriConsts.NETWORK_INFO: outString += "NETWORK_INFO"; break;
  
    case SentriConsts.FIX_ROUTE: outString += "FIX_ROUTE"; break;
    case SentriConsts.RELEASE_ROUTE: outString += "RELEASE_ROUTE"; break;
    case SentriConsts.TIMESYNC_ON: outString += "TIMESYNC_ON"; break;
    case SentriConsts.TIMESYNC_OFF: outString += "TIMESYNC_OFF"; break;
    
    case SentriConsts.FOR_DEBUG: outString += "FOR_DEBUG"; break;

    default: outString += "invalid'"; break;
    }
    return outString;
  }

};

