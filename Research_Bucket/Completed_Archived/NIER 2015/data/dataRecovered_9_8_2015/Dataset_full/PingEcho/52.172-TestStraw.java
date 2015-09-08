// $Id: TestStraw.java,v 1.3 2006/12/01 05:34:56 binetude Exp $

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
 * File: TestStraw.java
 *
 * @author <a href="mailto:binetude@cs.berkeley.edu">Sukun Kim</a>
 */

package net.tinyos.straw;

import java.io.*;
import java.util.*;
import net.tinyos.util.*;
import net.tinyos.message.*;
import net.tinyos.straw.*;

public class TestStraw {

  StrawCmdP cmdP = new StrawCmdP();
  StrawNodeList nodeList = new StrawNodeList("Sentri_NodeList.txt");
  Straw straw = new Straw();

  private int toPositive(byte inByte) {
    return inByte < 0 ? inByte + 256 : (int)inByte;
  }



  private int pingNode() {
    if (cmdP.broadcasting) {
      nodeList.loadNodeList();
    } else {
      nodeList.noOfNode = 1;
      nodeList.nodeNo[0]  = cmdP.dest;
      nodeList.nodeVld[0] = true;
    }

    for (int i = 0; i < nodeList.noOfNode; i++) {
    
      cmdP.dest = nodeList.nodeNo[i];

      straw.toUART = cmdP.toUART;
      straw.verbose = (short) (cmdP.verbose ? 5 : 2);

      int tmpResult = straw.ping(cmdP.dest, (short)2);
      if (tmpResult != 0) {
        System.out.println("\nNode " + cmdP.dest + " does not respond");
	return tmpResult + 10;
      }
      System.out.println("\nNode " + cmdP.dest + " responds");

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

      straw.toUART = cmdP.toUART;
      straw.verbose = (short) (cmdP.verbose ? 5 : 2);
      long rawDataSize = 1000;
      byte[] rawData = new byte[(int)rawDataSize];
      int tmpResult = straw.read(cmdP.dest, (short)2, 0,
        rawDataSize, rawData);
      if (tmpResult != 0)
	return tmpResult + 10;

      int chnlIndex = 0;
      for (int j = 0; j < rawDataSize; j++) {
        System.out.print(toPositive(rawData[j]) + " ");
      }
      System.out.println("");

    }
    if (cmdP.broadcasting) cmdP.dest = TosP.TOS_BCAST_ADDR;
    return 0;
  }



  public int execute(String[] args) {
    if (cmdP.parse(args) != 0) return -1;
 
    switch (cmdP.cmd) {

      case Cmd.PINGNODE:
        return pingNode();

      case Cmd.READDATA:
        return readData();

      case Cmd.HELP:
        return 0;

      default:
        System.out.println("ERROR: DataCenter.execute");
	return 0;
    }
  }

  public static void main(String[] args) {
    TestStraw dc = new TestStraw();
    System.exit(dc.execute(args));
  }
 
};
 
