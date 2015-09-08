package com.rincon.testharness;

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

import com.rincon.testharness.messages.TestMsg;


public class Parser {

	
	private TestMsg packet = new TestMsg();
	
	/**
	 * Get the packet after it's parsed.
	 * @return
	 */
	public TestMsg getPacket() {
		return packet;
	}
	
	/**
	 * Constructor
	 * @param args
	 */
	public Parser(String[] argv) {
		if (argv.length < 1) {
			reportError("No arguments found");
		}

		int index = 0;
	
		String cmd = argv[0];
		
		/*
		 * 	// Read the starting address
			if (argv.length > ++index) {
				startingAddress = parseLong(argv[index]);
			} else {
				reportError("Read requires a start address");
			}
			
			// Read the length
			if (argv.length > ++index) {
				packet.set_len(parseInt(argv[index]));
			} else {
				reportError("Data length required");
			}
			
			// Read the filename
			if (argv.length > ++index) {
				packet.set_data(stringToData(argv[index]));
			} else {
				reportError("Filename required");
			}
		 */
		
		if(cmd.matches("-start")) {
			packet.set_cmd(TestHarnessCommands.CMD_START);
			
			// Read the length
			if (argv.length > ++index) {
				packet.set_param(parseInt(argv[index]));
			} else {
				System.out.println("No parameters given - setting to 0");
				packet.set_param(0);
			}
			
		} else if(cmd.matches("-ping")) {
			packet.set_cmd(TestHarnessCommands.CMD_PING);
			
		} else {
			reportError("No command given");
		}
	}
	
	/**
	 * Attempt to decode the int value, and deal with any illegible remarks.
	 * 
	 * @param intString
	 * @return
	 */
	public int parseInt(String intString) {
		try {
			return Integer.decode(intString).intValue();
		} catch (NumberFormatException e) {
			reportError(e.getMessage());
		}

		return -1;
	}

	/**
	 * Attempt to decode the long value, and deal with any illegible remarks.
	 * 
	 * @param longString
	 * @return
	 */
	public long parseLong(String longString) {
		try {
			return Long.decode(longString).longValue();
		} catch (NumberFormatException e) {
			reportError(e.getMessage());
		}

		return -1;
	}
	
	/**
	 * Report the syntax error, print the usage, and exit.
	 * 
	 * @param error
	 */
	private void reportError(String error) {
		System.err.println(error);
		usage();
		System.exit(1);
	}


	/**
	 * Prints the usage for this application
	 * 
	 */
	private static void usage() {
		System.err.println("Usage: java com.rincon.testharness.TestHarness [command]");
		System.err.println("  COMMANDS");
		System.err.println("    -start <test specific parameter>");
		System.err.println("    -ping");
	}
	
}
