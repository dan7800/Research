/*
 * Copyright (c) 2004-2006 Rincon Research Corporation.  
 * All rights reserved.
 * 
 * Rincon Research will permit distribution and use by others subject to
 * the restrictions of a licensing agreement which contains (among other things)
 * the following restrictions:
 * 
 *  1. No credit will be taken for the Work of others.
 *  2. It will not be resold for a price in excess of reproduction and 
 *      distribution costs.
 *  3. Others are not restricted from copying it or using it except as 
 *      set forward in the licensing agreement.
 *  4. Commented source code of any modifications or additions will be 
 *      made available to Rincon Research on the same terms.
 *  5. This notice will remain intact and displayed prominently.
 * 
 * Copies of the complete licensing agreement may be obtained by contacting 
 * Rincon Research, 101 N. Wilmot, Suite 101, Tucson, AZ 85711.
 * 
 * There is no warranty with this product, either expressed or implied.  
 * Use at your own risk.  Rincon Research is not liable or responsible for 
 * damage or loss incurred or resulting from the use or misuse of this software.
 */

/**
 * @author David Moss (dmm@rincon.com)
 */

package com.rincon.testharness;

import com.rincon.testharness.messages.TestMsg;
import com.rincon.testharness.stickcomm.Comm;


public class TestHarness {

	/** Broadcast Address */
	public static final short TOS_BCAST_ADDR = (short) 0xffff;

	/** Argument Parser */
	private Parser parser;
	
	/** Communication capabilities */
	private Comm comm = new Comm();
	
	
	/**
	 * Constructor
	 * 
	 * @param argv
	 */
	public TestHarness(String[] argv) {
	    parser = new Parser(argv);
	    double startTime;
	    double endTime;
	    double totalTime_millis;
	    
	    startTime = System.currentTimeMillis();
	    TestMsg reply = comm.send(parser.getPacket());
		endTime = System.currentTimeMillis();
		
		totalTime_millis = endTime - startTime;
		
		
		if(reply.get_cmd() == TestHarnessCommands.CMD_DONE) {
			System.out.println("Done");
		    if(reply.get_result() == 1) {
		    	System.out.println("Success");
		    } else {
		    	System.out.println("Fail");
		    }
		    System.out.println("Test Return Value = " + reply.get_param());
		} else if(reply.get_cmd() == TestHarnessCommands.CMD_PING) {
			System.out.println("Pong!");
		} else if(reply.get_cmd() == TestHarnessCommands.CMD_BLOCKED) {
			System.out.println("Blocked");
		}
		
		
		System.out.println("Total time: " + totalTime_millis + "[ms]; " + totalTime_millis/1000 + "[s]");
		System.exit(0);
	}

	
	
	/**
	 * Main Method
	 * 
	 * @param argv
	 */
	public static void main(String[] argv) {
		new TestHarness(argv);
	}
}
