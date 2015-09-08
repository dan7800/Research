//Source file: E:\\Projects\\monica\\src\\org\\jMule\\OpCodes.java



/********************************************************************************



    jMule - A java eMule port

    

    Copyright (C) 2002 myon, gos, andyl



    This program is free software; you can redistribute it and/or modify

    it under the terms of the GNU General Public License as published by

    the Free Software Foundation; either version 2 of the License.



    This program is distributed in the hope that it will be useful,

    but WITHOUT ANY WARRANTY; without even the implied warranty of

    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the

    GNU General Public License for more details ( see the LICENSE file ).



    You should have received a copy of the GNU General Public License

    along with this program; if not, write to the Free Software

    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

	

********************************************************************************/



package org.jMule;





/**

 * Constants

 */

public class OpCodes {

   public static final int CURRENT_VERSION_SHORT = 0x18;

   public static final int EMULE_PROTOCOL_VERSION = 0x01;

   public static final int EDONKEYVERSION = 0x3c;

   public static final int PREFFILE_VERSION = 0x04;

   public static final int PARTFILE_VERSION = 0xe0;

   

   /**

    * public static final int COMPILE_DATE__DATE__

    * public static final int COMPILE_TIME__TIME__

    */

   public static final int UDPSEARCHSPEED = 1000;

   

   /**

    * if this value is too low you will miss sources

    */

   public static final int MAX_RESULTS = 100;

   

   /**

    * max global search results

    */

   public static final int MAX_CLIENTCONNECTIONTRY = 3;

   public static final int CONNECTION_TIMEOUT = 100000;

   

   /**

    * set his lower if you want less connections at once, set it higher if you have enough sockets (edonkey has its own 

    * timout too, so a very high value won't effect this)

    */

   public static final int FILEREASKTIME = 1100000;

   public static final int SERVERREASKTIME = 800000;

   

   /**

    * don't set this too low, it wont speed up anything, but it could kill emule or your final internetconnection

    */

   public static final int UDPSERVERREASKTIME = 1300000;

   public static final int UDPSERVERPORT = 4665;

   public static final int UPLOAD_CLIENT_DATARATE = 3000;

   

   /**

    * uploadspeed per client in bytes - you may want to adjust this if you have a slow connection or T1-T3 ;)

    */

   public static final int MIN_UP_CLIENTS_ALLOWED = 3;

   

   /**

    * min. clients allowed to download regardless UPLOAD_CLIENT_DATARATE or any other factors. Don't set this too high

    */

   public static final int MAX_UP_CLIENTS_ALLOWED = 100;

   

   /**

    * max. clients allowed regardless UPLOAD_CLIENT_DATARATE or any other factors. Don't set this too low, use DATARATE to 

    * adjust uploadspeed per client

    */

   public static final int DOWNLOADTIMEOUT = 80000;

   

   /**

    * you shouldn't change anything here if you are not really sure, or emule will probaly not work

    */

   public static final int PARTSIZE = 9728000;

   public static final int BLOCKSIZE = 184320;

   public static final byte OP_EDONKEYHEADER = (byte ) 0xE3;

   public static final byte OP_EDONKEYPROT = OP_EDONKEYHEADER;

   public static final int OP_EXTENDEDPROT = 0xD4;

   public static final int OP_EMULEPROT = 0xC5;

   public static final int OP_MLDONKEYPROT = 0x00;

   public static final int MET_HEADER = 0x0E;

   public static final int UNLIMITED = 0xFFFF;

   

   /**

    * client <-> server

    */

   public static final byte OP_LOGINREQUEST = 0x01;

   public static final byte OP_SERVERMESSAGE = 0x38;

   public static final byte OP_IDCHANGE = 0x40;

   public static final byte OP_GETSERVERLIST = 0x14;

   public static final byte OP_OFFERFILES = 0x15;

   public static final byte OP_SEARCHREQUEST = 0x16;

   public static final byte OP_SERVERLIST = 0x32;

   public static final byte OP_SEARCHRESULT = 0x33;

   public static final byte OP_GETSOURCES = 0x19;

   public static final byte OP_FOUNDSOURCES = 0x42;

   public static final byte OP_CALLBACKREQUEST = 0x1C;

   public static final byte OP_SERVERSTATUS = 0x34;

   

   /**

    * client <-> UDP server

    */

   public static final byte OP_GLOBSEARCHREQ = (byte ) 0x98;

   public static final byte OP_GLOBSEARCHRES = (byte ) 0x99;

   public static final byte OP_GLOBGETSOURCES = (byte ) 0x9A;

   public static final byte OP_GLOBFOUNDSORUCES = (byte ) 0x9B;

   public static final byte OP_GLOBCALLBACKREQ = (byte ) 0x9C;

   

   /**

    * client <-> client

    */

   public static final byte OP_HELLO = (byte ) 0x01;

   public static final byte OP_HELLOANSWER = (byte ) 0x4c;

   public static final byte OP_FILEREQUEST = (byte ) 0x58;

   public static final byte OP_FILEREQANSWER = (byte ) 0x59;

   public static final byte OP_FILESTATUS = (byte ) 0x50;

   public static final byte OP_STARTUPLOADREQ = (byte ) 0x54;

   public static final byte OP_ACCEPTUPLOADREQ = (byte ) 0x55;

   public static final byte OP_CANCELTRANSFER = (byte ) 0x56;

   public static final byte OP_OUTOFPARTREQS = (byte ) 0x57;

   public static final byte OP_REQUESTPARTS = (byte ) 0x47;

   public static final byte OP_SENDINGPART = (byte ) 0x46;

   public static final byte OP_SETREQFILEID = (byte ) 0x4f;

   

   /**

    * ?

    */

   public static final byte OP_HASHSETREQUEST = (byte ) 0x51;

   public static final byte OP_HASHSETANSWER = (byte ) 0x52;

   public static final byte OP_MESSAGE = (byte ) 0x4E;

   

   /**

    * extened prot client <-> extened prot client

    */

   public static final byte OP_EMULEINFO = (byte ) 0x01;

   public static final byte OP_EMULEINFOANSWER = (byte ) 0x02;

   public static final byte OP_COMPRESSEDPART = (byte ) 0x40;

   public static final byte OP_QUEUERANKING = (byte ) 0x60;

   

   /**

    * server.met

    */

   public static final byte ST_SERVERNAME = (byte ) 0x01;

   public static final byte ST_DESCRIPTION = (byte ) 0x0B;

   public static final byte ST_PING = (byte ) 0x0C;

   public static final byte ST_PREFERENCE = (byte ) 0x0D;

   

   /**

    * file tags

    */

   public static final byte FT_FILENAME = (byte ) 0x01;

   public static final byte FT_FILESIZE = (byte ) 0x02;

   public static final byte FT_FILETYPE = (byte ) 0x03;

   public static final byte FT_FILEFORMAT = (byte ) 0x04;

   public static final byte FT_SOURCES = (byte ) 0x15;

   public static final byte FT_TRANSFERED = (byte ) 0x08;

   public static final byte FT_GAPSTART = (byte ) 0x09;

   public static final byte FT_GAPEND = (byte ) 0x0A;

   public static final byte FT_PRIORITY = (byte ) 0x13;

   public static final byte FT_STATUS = (byte ) 0x14;

   public static final byte FT_PARTFILENAME = (byte ) 0x12;

   public static final byte CT_NAME = (byte ) 0x01;

   public static final byte CT_VERSION = (byte ) 0x11;

   public static final byte CT_PORT = (byte ) 0x0f;

   public static final int MP_MESSAGE = 10102;

   public static final int MP_DETAIL = 10103;

   public static final int MP_CANCEL = 10201;

   public static final int MP_STOP = 10202;

   public static final int MP_RESUME = 10204;

   public static final int MP_PAUSE = 10203;

   public static final int MP_CLEARCOMPLETED = 10205;

   public static final int MP_PRIOLOW = 10300;

   public static final int MP_PRIONORMAL = 10301;

   public static final int MP_PRIOHIGH = 10302;

   

   /**

    * emule tagnames

    */

   public static final int ET_COMPRESSION = 0x20;

   

   /**

    * emuleapp <-> emuleapp

    */

   public static final int OP_ED2KLINK = 12000;

   /**

    * thread messages

    * public static final int	TM_FINISHEDHASHING = WM_USER+1450;

    */

}

