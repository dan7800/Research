package com.rincon.blackbook.bfilewrite;

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

public interface BFileWriteEvents {


	/**
	 * Signaled when a file has been opened, with the results
	 * @param fileName - the name of the opened write file
	 * @param len - The total reserved length of the file
	 * @param result - SUCCSES if the file was opened successfully
	 */
	public void opened(String fileName, long len, boolean result);

	/** 
	 * Signaled when the opened file has been closed
	 * @param result - SUCCESS if the file was closed properly
	 */
	public void closed(boolean result);

	/**
	 * Signaled when this file has been saved.
	 * This does not require the save() command to be called
	 * before being signaled - this would happen if another
	 * file was open for writing and that file was saved, but
	 * the behavior of the checkpoint file required all files
	 * on the system to be saved as well.
	 * @param fileName - name of the open write file that was saved
	 * @param result - SUCCESS if the file was saved successfully
	 */
	public void saved(boolean result);

	/**
	 * Signaled when data is written to flash. On some media,
	 * the data is not guaranteed to be written to non-volatile memory
	 * until save() or close() is called.
	 * @param fileName
	 * @param data The buffer of data appended to flash
	 * @param amountWritten The amount written to flash
	 * @param result
	 */
	public void appended(int amountWritten, boolean result);

}
