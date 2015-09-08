package com.rincon.flashviewer;

import com.rincon.flashviewer.messages.ViewerMsg;
import com.rincon.flashviewer.send.FlashViewerSender;

public class FlashViewer {

	/** Object to run the commands */
	private CommandRunner runner = new CommandRunner();

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
	
	
	/**
	 * Constructor
	 * @param args
	 */
	public FlashViewer(String[] argv) {
		if (argv.length < 1) {
			reportError("No arguments found");
		}

		int index = 0;
		String cmd = argv[0];
		

		long startAddress = 0;
		int moteID = 0;
		long actualRange = 0;
		short volume = 0;
		short[] data = new short[ViewerMsg.totalSize_data()];
		
		// Find and set the default mote id
		if((moteID = parseInt(argv[0])) == -1) {
			moteID = 1;
		} else {
			if(argv.length > 1) {
				cmd = argv[1];
			} else {
				reportError("No command given");
			}
		}
		
		if(cmd.matches("-read")) {
			// Get the start address
			if (argv.length > ++index) {
				startAddress = parseLong(argv[index]);
			} else {
				reportError("Missing [start address]");
			}
			
			// Get the range
			if (argv.length > ++index) {
				actualRange = parseLong(argv[index]);
			} else {
				reportError("Missing [range]");
			}
			
			runner.read(startAddress, actualRange, moteID);
			

		} else if(cmd.matches("-write")) {

			// Get the start address
			if (argv.length > ++index) {
				startAddress = parseLong(argv[index]);
			} else {
				reportError("Missing [start address]");
			}
			
			char[] rawdata = null;
			if (argv.length > ++index) {
				rawdata = argv[index].toCharArray();
				data = new short[ViewerMsg.totalSize_data()];

				System.out.println("Writing data");
				for (int i = 0; i < data.length && i < rawdata.length; i++) {
					data[i] = (short) rawdata[i];
					System.out.print("0x" + Integer.toHexString((int) data[i])
							+ " ");
				}
				System.out.println();

			} else {
				reportError("Missing [data]");
			}
			
			runner.write(startAddress, data, rawdata.length, moteID);

			
		} else if(cmd.matches("-erase")) {
			runner.erase(moteID);
			
			
		} else if(cmd.matches("-commit")) {
			runner.commit(moteID);
			
		} else if(cmd.matches("-mount")) {
			if (argv.length > ++index) {
				volume = (short) parseInt(argv[index]);
			} else {
				reportError("Missing [mote]");
			}
			
			runner.mount(volume, moteID);
			
			
		} else if(cmd.matches("-ping")) {
			runner.ping(moteID);
			
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
			return -1;
		}
	}

	/**
	 * Attempt to decode the long value, and deal with any illegible remarks.
	 * 
	 * @param longString
	 * @return
	 */
	public long parseLong(String longString) {
		try {			return Long.decode(longString).longValue();
		} catch (NumberFormatException e) {
			reportError(e.getMessage());
		}

		return -1;
	}

	

	/**
	 * Takes a filename string and converts it to a 14 element
	 * filename short array
	 * @param s
	 * @return
	 */
	public short[] stringToData(String s) {
		int filenameLength = 14;
		short[] returnData = new short[filenameLength];
		char[] charData = s.toCharArray();
		
		for(int i = 0; i < charData.length && i < filenameLength; i++) {
			returnData[i] = (short) charData[i];
		}
		
		for(int i = charData.length; i < filenameLength; i++) {
			returnData[i] = 0;
		}
		
		return returnData;
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
		System.err.println("Usage: java com.rincon.flashviewer [mote] [command]");
		System.err.println("  COMMANDS");
		System.err.println("    -read [start address] [range]");
		System.err.println("    -write [start address] [" + ViewerMsg.totalSize_data() + " characters]");
		System.err.println("    -erase");
		System.err.println("    -commit");
		System.err.println("    -mount [volume id]");
		System.err.println("    -ping");
		System.err.println();
	}
	
	/**
	 * Main method
	 * @param args
	 */
	public static void main(String[] args) {
		new FlashViewer(args);
	}
}
package com.rincon.flashbridgeviewer;

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

import com.rincon.flashbridgeviewer.messages.ViewerMsg;
import com.rincon.flashbridgeviewer.send.FlashViewerSender;

public class FlashViewer {

	/** Object to run the commands */
	private CommandRunner runner = new CommandRunner();

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
	
	
	/**
	 * Constructor
	 * @param args
	 */
	public FlashViewer(String[] argv) {
		if (argv.length < 1) {
			reportError("No arguments found");
		}

		int index = 0;
		String cmd = argv[0];
		

		long startAddress = 0;
		int moteID = 0;
		long actualRange = 0;
		short volume = 0;
		short[] data = new short[ViewerMsg.totalSize_data()];
		
		// Find and set the default mote id
		if((moteID = parseInt(argv[0])) == -1) {
			moteID = 1;
		} else {
			if(argv.length > 1) {
				cmd = argv[1];
			} else {
				reportError("No command given");
			}
		}
		
		if(cmd.matches("-read")) {
			// Get the start address
			if (argv.length > ++index) {
				startAddress = parseLong(argv[index]);
			} else {
				reportError("Missing [start address]");
			}
			
			// Get the range
			if (argv.length > ++index) {
				actualRange = parseLong(argv[index]);
			} else {
				reportError("Missing [range]");
			}
			
			runner.read(startAddress, actualRange, moteID);
			

		} else if(cmd.matches("-write")) {

			// Get the start address
			if (argv.length > ++index) {
				startAddress = parseLong(argv[index]);
			} else {
				reportError("Missing [start address]");
			}
			
			char[] rawdata = null;
			if (argv.length > ++index) {
				rawdata = argv[index].toCharArray();
				data = new short[ViewerMsg.totalSize_data()];

				System.out.println("Writing data");
				for (int i = 0; i < data.length && i < rawdata.length; i++) {
					data[i] = (short) rawdata[i];
					System.out.print("0x" + Integer.toHexString((int) data[i])
							+ " ");
				}
				System.out.println();

			} else {
				reportError("Missing [data]");
			}
			
			runner.write(startAddress, data, rawdata.length, moteID);

			
		} else if(cmd.matches("-erase")) {
			// Get the start address
			if (argv.length > ++index) {
				startAddress = parseLong(argv[index]);
			} else {
				reportError("Missing [sector]");
			}
			
			runner.erase((int) startAddress, moteID);
			
			
		} else if(cmd.matches("-flush")) {
			runner.flush(moteID);
			
		} else if(cmd.matches("-crc")) {
			// Get the start address
			if (argv.length > ++index) {
				startAddress = parseLong(argv[index]);
			} else {
				reportError("Missing [start address]");
			}
			
			// Get the range
			if (argv.length > ++index) {
				actualRange = parseLong(argv[index]);
			} else {
				reportError("Missing [range]");
			}
			
			runner.crc(startAddress, (int)actualRange, moteID);
			
			
		} else if(cmd.matches("-ping")) {
			runner.ping(moteID);
			
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
			return -1;
		}
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
	 * Takes a filename string and converts it to a 14 element
	 * filename short array
	 * @param s
	 * @return
	 */
	public short[] stringToData(String s) {
		int filenameLength = 14;
		short[] returnData = new short[filenameLength];
		char[] charData = s.toCharArray();
		
		for(int i = 0; i < charData.length && i < filenameLength; i++) {
			returnData[i] = (short) charData[i];
		}
		
		for(int i = charData.length; i < filenameLength; i++) {
			returnData[i] = 0;
		}
		
		return returnData;
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
		System.err.println("Usage: java com.rincon.flashbridgeviewer.FlashViewer [command]");
		System.err.println("  COMMANDS");
		System.err.println("    -read [start address] [range]");
		System.err.println("    -write [start address] [" + ViewerMsg.totalSize_data() + " characters]");
		System.err.println("    -erase [sector]");
		System.err.println("    -flush");
		System.err.println("    -crc [start address] [range]");
		System.err.println("    -ping");
		System.err.println();
	}
	
	/**
	 * Main method
	 * @param args
	 */
	public static void main(String[] args) {
		new FlashViewer(args);
	}
}
