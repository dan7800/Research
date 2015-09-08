package com.rincon.directflash.send;

/*
 * Copyright (c) 2005-2006 Rincon Research Corporation
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the
 *   distribution.
 * - Neither the name of the Rincon Research Corporation nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE
 * ARCHED ROCK OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE
 */

public interface FlashViewerCommands {

	/**
	 * Read a block of data from flash
	 * @param address
	 * @param length
	 */
	public void read(long address, int length, int moteID);
	
	/**
	 * Write some bytes to flash
	 * @param address
	 * @param buffer
	 * @param length
	 */
	public void write(long address, short[] buffer, int length, int moteID);
	
	/**
	 * Erase the currently mounted sector in flash
	 *
	 */
	public void erase(int sector, int moteID);
	
	/**
	 * Mount BlockStorage to the given volume ID
	 * @param id
	 */
	public void crc(long address, int length, int moteID);
	
	/**
	 * Commit changes to flash 
	 *
	 */
	public void flush(int moteID);
	
	/**
	 * Ping the FlashViewer on a mote
	 * @param moteID
	 */
	public void ping(int moteID);
}
package com.rincon.flashviewer.send;

public interface FlashViewerCommands {

	/**
	 * Read a block of data from flash
	 * @param address
	 * @param length
	 */
	public void read(long address, int length, int moteID);
	
	/**
	 * Write some bytes to flash
	 * @param address
	 * @param buffer
	 * @param length
	 */
	public void write(long address, short[] buffer, int length, int moteID);
	
	/**
	 * Erase the currently mounted sector in flash
	 *
	 */
	public void erase(int moteID);
	
	/**
	 * Mount BlockStorage to the given volume ID
	 * @param id
	 */
	public void mount(short id, int moteID);
	
	/**
	 * Commit changes to flash 
	 *
	 */
	public void commit(int moteID);
	
	/**
	 * Ping the FlashViewer on a mote
	 * @param moteID
	 */
	public void ping(int moteID);
}
package com.rincon.flashbridgeviewer.send;

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

public interface FlashViewerCommands {

	/**
	 * Read a block of data from flash
	 * @param address
	 * @param length
	 */
	public void read(long address, int length, int moteID);
	
	/**
	 * Write some bytes to flash
	 * @param address
	 * @param buffer
	 * @param length
	 */
	public void write(long address, short[] buffer, int length, int moteID);
	
	/**
	 * Erase the currently mounted sector in flash
	 *
	 */
	public void erase(int sector, int moteID);
	
	/**
	 * Mount BlockStorage to the given volume ID
	 * @param id
	 */
	public void crc(long address, int length, int moteID);
	
	/**
	 * Commit changes to flash 
	 *
	 */
	public void flush(int moteID);
	
	/**
	 * Ping the FlashViewer on a mote
	 * @param moteID
	 */
	public void ping(int moteID);
}
