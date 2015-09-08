/********************************************************************************
 *
 * jMule - a Java massive parallel file sharing client
 *
 * Copyright (C) by the jMuleGroup ( see the CREDITS file )
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Id: Tag.java,v 1.18 2003/10/21 19:29:17 lydna Exp $
 *
 ********************************************************************************/
package org.jmule.core.protocol.donkey;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import org.jmule.util.Convert;


/** Common conivience methods to handle the eDonkey MET tags structures.
 * @version $Revision: 1.18 $
 * <br>Last changed by $Author: lydna $ on $Date: 2003/10/21 19:29:17 $
 */
public class Tag {
    final static Logger log = Logger.getLogger(Tag.class.getName());
    
	/** Tag indicating hash type. */
	public static final int TYPE_HASH = 0x01;
	/** Tag indicating string type. */
	public static final int TYPE_STRING = 0x02;
	/** Tag indicating integer (32 bit) type. */
	public static final int TYPE_INT = 0x03;
	/** Tag indicating float (32 bit) type. */
	public static final int TYPE_FLOAT = 0x04;
	/** Tag indicating word (16 bit) type. */
	public static final int TYPE_WORD = 0x08;
	/** Tag indicating byte (8 bit) type. */
	public static final int TYPE_BYTE = 0x09;
	/** Server.met special tag containg the server's name */
	public final static byte ST_Servername = 0x01;
	/** Server.met special tag containg the server's description. */
	public final static byte ST_Description = 0x0B;
	/** Server.met special tag containg the server's last ping. */
	public final static byte ST_Ping = 0x0C;
	/** Server.met special tag containg *history* tag. */
	public final static byte ST_Fail = 0x0D;
	/** Server.met special tag containg the server's priority. */
	public final static byte ST_Preference = 0x0E;

	// part.met or file tags
	/** part.met special tag containg the filename. */
	public final static byte FT_Filename = 0x01;
	/** part.met special tag containg the filesize. */
	public final static byte FT_Filesize = 0x02;
	/** part.met special tag containg the filetype like audio or video // CHECK: Is this correct ? ... */
	public final static byte FT_Filetype = 0x03;
	/** part.met special tag containg the fileformat like mp3, wave or avi, mpg. */
	public final static byte FT_Fileformat = 0x04;
    
	/**
	* part.met special tag containg information about last time seen complete in seconds since 1st January 1970; 
    * may ignore it if not form *.met
	*/
    public final static byte FT_Lastseencomplete = 0x05;
        
    /** search result - sources for a file known by server */        
	public final static byte FT_Sources = 0x15;
       
	public final static byte FT_Transfered = 0x08;     
    /** part.met special tag for gap list entry - gap start*/        
	public final static byte FT_GapStart = 0x09;    
    /** part.met special tag for gap list entry - gap end*/        
	public final static byte FT_GapEnd = 0x0A;     
	public final static byte FT_Priority = 0x13;       
	public final static byte FT_Status = 0x14;     
    /** part.met special tag containg the filename for partfile. */        
	public final static byte FT_Partfilename = 0x12;
     
    /** user nick name tag in ed2k hello packet */       
	public static final byte CT_Username = (byte) 0x01;	       
    /** ed2k version tag in ed2k hello packet */       
	public static final byte CT_Version = (byte) 0x11;      
    /** old - ed2k tcp port tag in ed2k hello packet */       
	public static final byte CT_Port = (byte) 0x0f;
    /** emule udp ports tag in ed2k hello packet.
    *  low word emule extension port<br>
    *  hidh word emuleDHT port
    */       
    public static final byte CT_Emule_udpports = (byte) 0xf9;
    /** emule misc. options1 tag in ed2k hello packet.
    * <table border=1 padding="2">
    * <thead><tr><th align="center"> nible </th><th align="center">option</th>
    *  <tr><td align="center">0 (bits 0-3)</td><td> supports preview</td></tr>
    *  <tr><td align="center">1 (bits 4-7)</td><td> accepts comment version</td></tr>
    *  <tr><td align="center">2 (bits 8-11)</td><td> extended requests version</td></tr>
    *  <tr><td align="center">3 (bits 12-15)</td><td> source exchange version</td></tr>
    *  <tr><td align="center">4 (bits 16-19)</td><td> support secure identification</td></tr>
    *  <tr><td align="center">5 (bits 20-23)</td><td> data compression version</td></tr>
    *  <tr><td align="center">6 (bits 24-27)</td><td> udp version</td></tr>
    * </table>
    */       
    public static final byte CT_Emule_miscoptions1 = (byte) 0xfa;
    /** emule version tag in ed2k hello packet.
     * int value<br>
     * version printed is major.minor.<code>numbertochar(</code>update<code>)</code><br>
     * <code>numbertochar</code> translates 0 to a, 1 to b and so on.
     * <table border=1 padding="2">
     *  <thead><tr><th align="center"> bit range </th><th align="center">version part</th></tr></thead>
     *  <tr><td align="center">bits 0-6 </td><td> reserved (build?)</td></tr>
     *  <tr><td align="center">bits 7-9</td><td> update</td></tr>
     *  <tr><td align="center">bits 10-16</td><td> minor</td></tr>
     *  <tr><td align="center">bits 17-23</td><td> major</td></tr>
     *  <tr><td align="center">bits 24..</td><td> (compartible client ID)</td></tr>
     * </table>
     */   
	public static final byte CT_Emule_version = (byte) 0xfb;
	
    
    byte type;
    byte specialValue;
    String tagName;
    String stringValue;
    int intValue;
    float floatValue;
    
    public boolean isIntTag() {
        return type == TYPE_INT;
    }
    
    public boolean isStringTag() {
        return type == 0x02;
    }
    
    public boolean isSpecialTag() {
        return ( specialValue!=0);
    }
    
    public boolean isFloatTag() {
        return type ==  TYPE_FLOAT;
    }
    
    public boolean isWordTag() {
        return type == TYPE_WORD;
    }
    
    public boolean isByteTag() {
        return type == TYPE_BYTE;
    }
    
    public boolean isNumIntTag() {
        return isByteTag()||isWordTag()||isIntTag();
    }

    public String getTagName() {
        if( isSpecialTag() ) return specialTagAsStr( specialValue );
        else return tagName;
    }
    
    public int getSpecialValue() {
        assert isSpecialTag();
        return specialValue;
    }
    
    public String getStringValue() {
        assert isStringTag();
        return stringValue;
    }
    
    public int getIntValue() {
        assert isIntTag();
        return intValue;
    }
    
    public float getFloatValue() {
        assert isFloatTag();
        return floatValue;
    }
    
    public int getType() {
        return type;
    }
    
        /** Appendes a Tag in a Buffer with size 8. The Tag represents a 32-bit integer.
         * @param buffer a ByteBuffer with 8 bytes remaining. (The method uses the buffer's byteorder)
         */        
	public static void append(ByteBuffer buffer, byte type, int value) {
		buffer.put(Convert.intToByte(TYPE_INT));
		// This is a special tag, so we append 1 as lenght
		buffer.putShort((short) 1);	
		buffer.put(type);
		buffer.putInt(value);
	}
   
	public static void append(ByteBuffer buffer, String type, int value) {
		buffer.put(Convert.intToByte(TYPE_INT));
		buffer.putShort((short)type.length());
		buffer.put(type.getBytes());
		buffer.putInt(value);
	}
    
	public static void append(ByteBuffer buffer, byte type, String string) {
		buffer.put(Convert.intToByte(TYPE_STRING));
		// This is a special tag, so we append 1 as lenght
		buffer.putShort((short) 1);	
		buffer.put(type);
		buffer.putShort((short)string.length());
		buffer.put(string.getBytes());
	}
      
	public static void append(ByteBuffer buffer, String type, String string) {
		buffer.put(Convert.intToByte(TYPE_STRING));
		buffer.putShort((short)type.length());
		buffer.put(type.getBytes());
		buffer.putShort((short)string.length());
		buffer.put(string.getBytes());
	}
    
    /**
    * Append only tag if type known.
    * You can get size in bytes used with getSizeInByteBufferCompressInteger().
    * @return false if binary representation of tag is not known, otherwise true
    * @throws BufferOverflowException If there are fewer than getSizeInByteBuffer() bytes remaining in this buffer.
    */
	public boolean appendCompressInteger(ByteBuffer buffer) {
        if (isIntTag()) {
            buffer.put(Convert.intToByte(TYPE_INT));
            appendName(buffer);
            buffer.putInt(intValue);
        } else if (isWordTag()) {
            buffer.put(Convert.intToByte(TYPE_WORD));
            appendName(buffer);
            buffer.putShort((short)intValue);
        } else if (isIntTag()) {
            buffer.put(Convert.intToByte(TYPE_BYTE));
            appendName(buffer);
            buffer.put((byte)intValue);
        } else {
            return append(buffer);
        }
        return true;
	}
    /**
    * Append only tag if type known.
    * You can get size in bytes used with getSizeInByteBuffer().
    * @return false if binary representation of tag is not known, otherwise true
    * @throws BufferOverflowException If there are fewer than getSizeInByteBuffer() bytes remaining in this buffer.
    */
	public boolean  append(ByteBuffer buffer) {
        if (isFloatTag()) {
            buffer.put(Convert.intToByte(TYPE_FLOAT));
            appendName(buffer);
            buffer.putFloat(floatValue);
        } else if (isNumIntTag()) {
            buffer.put(Convert.intToByte(TYPE_INT));
            appendName(buffer);
            buffer.putInt(intValue);
        } else if (isStringTag()) {
            buffer.put(Convert.intToByte(TYPE_STRING));
            appendName(buffer);
            buffer.putShort((short)stringValue.length());
            buffer.put(stringValue.getBytes());
        } else {
            return false;
        }
        return true;
	}
    
    private void appendName(ByteBuffer buffer) {
        if (isSpecialTag()) {
            buffer.putShort((short)1);
            buffer.put(type);
        } else {
            buffer.putShort((short)tagName.length());
            buffer.put(tagName.getBytes());
        }
    }
    
    /**
    * Bytes remaining in a ByteBuffer needed to append this tag to it.
    * @return size in bytes used by this.append(ByteBuffer buffer).
    */
    public int getSizeInByteBufferCompressInteger(){
        int result = 0;
        if (isIntTag()|| isFloatTag()){
            result += 4;
        } else if (isWordTag()) {
            result += 2;
        } else if (isByteTag()) {
            result += 1;
        } else if (isStringTag()) {
                result +=  stringValue.length()+2;
        } else { //unkowm´n tag can't be stored and get will not get deliver back if someone ask for this info
            return result;
        }
        if (isSpecialTag()) {
            result += 4;
        } else {
            result += tagName.length()+3;
        }
        return result;
    }
    /**
    * Bytes remaining in a ByteBuffer needed to append this tag to it.
    * @return size in bytes used by this.append(ByteBuffer buffer).
    */
    public int getSizeInByteBuffer(){
        int result = 0;
        if (isIntTag()|| isFloatTag()){
            result += 4;
        } else if (isStringTag()) {
                result +=  stringValue.length()+2;
        } else { //unkowm´n tag can't be stored and get will not get deliver back if someone ask for this info
            return result;
        }
        if (isSpecialTag()) {
            result += 4;
        } else {
            result += tagName.length()+3;
        }
        return result;
    }
    
    public static Tag readFrom( ByteBuffer bb ) throws java.io.UnsupportedEncodingException {
        Tag tag = new Tag();
        return readFrom( bb, tag );
    }
    
    public static Tag buildStringTag(String name, String value){
        Tag tag = new Tag();
        tag.stringValue = value;
        tag.tagName = name;
        tag.type = TYPE_STRING;
        return tag;
    }
    
    /*
     <Meta tag>:
      0x00 Undefined
      0x01 <Meta tag name> <Hash>
      0x02 <Meta tag name> DWORD
      0x03 <Meta tag name> <String>
      0x04 <Meta tag name> FLOAT
      0x05 <Meta tag name> BOOL
      0x06 <Meta tag name> BOOL Array
      0x07 <Meta tag name> BLOB
      0x08 <Meta tag name> WORD
      0x09 <Meta tag name> BYTE
     <Meta tag name>
     */
    public static Tag readFrom( ByteBuffer bb, Tag tag ) throws java.io.UnsupportedEncodingException {
        tag.type = bb.get();
        byte[] byteArray;
        short length = bb.getShort();
        
        if( length > 1 ) {
            // What a pain in the ass !!! is there really no other way ?
            byteArray = new byte[ length ];
            bb.get( byteArray );
            if( tag.isIntTag() ) {
                tag.specialValue = byteArray[0];
            }
            tag.tagName = new String( byteArray/*, "US-ASCII" */);
        } else {
            tag.specialValue = bb.get();
        }
        if( tag.isStringTag() ) {
            length = bb.getShort();
            // What a pain in the ass !!! is there really no other way ?
            byteArray = new byte[ length ];
            bb.get( byteArray );
            tag.stringValue = new String( byteArray/*, "US-ASCII" */);
        } else if( tag.isIntTag() ) {
            tag.intValue = bb.getInt();
        } else if(tag.isFloatTag()) {
            tag.floatValue = bb.getFloat();
        } else if(tag.isWordTag()) {
            tag.intValue = ((int)bb.getShort())&0xFFFF;
        } else if(tag.isByteTag()) {
            tag.intValue = ((int)bb.get())&0xFF;
        } else {
            log.severe("unhandled metattg type: "+tag.type+ " "+
                    (length>1?" name"+tag.tagName:" special=0x"+Convert.byteToHex(tag.specialValue))
                    +" following bytes: 0x"+Convert.byteBufferToHexString(bb, bb.position(), Math.min(bb.position()+128, bb.limit())));
        }
        
        return tag;
    }
    
    public String toString() {
        String str = "type=" + getType();
        str += " name=" + getTagName();
        if( isStringTag() ) str += " str=" + getStringValue();
        else if( isNumIntTag() ) str += " int=" + getIntValue();
        else if( isFloatTag() ) str += " float=" + getFloatValue();
        else if( isSpecialTag() ) str += " special=" + getSpecialValue();
        
        return Tag.class.getName() +"(" + str + ")";
    }
    
    // TODO: Hym, helper macro which enumerates over all vars with the given MOdifiers and type and name.startsWith
    //
    // private String
    private String specialTagAsStr( byte special ) {
        //if( CompileOptions.isVerboseObjTracingEnabled ) {
            switch( special ) {
                //case ST_Servername: return "Servername"; // abhängig wo wir gerade sind ...
                case ST_Description: return "Description";
                case ST_Ping: return "Ping";
                case ST_Preference: return "Preference";
                
                // part.met or file tags
                case FT_Filename: return "Name";// servername, filename, username ...
                case FT_Filesize: return "Filesize";
                case FT_Filetype: return "Filetype";
                case FT_Fileformat: return "Fileformat";
                
                case FT_Sources: return "Sources";
                
                case FT_Transfered: return "Transfered";
                case FT_GapStart: return "Gapstart";
                case FT_GapEnd: return "Gapend";
                case FT_Priority: return "Priority";
                case FT_Status: return "Status";
                case FT_Partfilename: return "Partfilename";
                
                case CT_Port: return "Port";
                case CT_Version: return "Version";
                case CT_Emule_udpports : return "eMule udp Port";
                case CT_Emule_miscoptions1: return "eMule misc. opt.1";
                case CT_Emule_version: return "emule version";
            }
      if (( isIntTag() || isStringTag() || isFloatTag() )&& tagName!=null){
          return tagName;
      }
      return "Unknown!(0x" + Convert.byteToHex(special) + ")";
    }

}
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

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.logging.Logger;

/**
* Provides various methods to deal with the eDonkey specific tags.
*
* A tag is simple a union of two value types: string or integer. A tag can have a tagName. 
* If the tag have not a tagName so it is a specialTag, which means the tag name is predefined
* ( see the ST_xx and FT_xx class constants )
* @fixme use new MemFileWriter MemFileReader implementatiosn based on ByteBuffer.
* @fixme in some code there are the tags create without this class: HelloPackat.java.
*/
public class Tag {
  
  final static Logger log = Logger.getLogger( Tag.class.getName() );
    
  /* tag representation in a met file
  emule src: packets.cpp::CTag(FILE * )
  ubyte type
  uint16 namelength
  if( length == 1 ) isSpecialtag()=true;
  
  Special tags -> emule src: packet.h
  */
  
  /**
  * Server.met special tag containg the server's name
  */
  public final static byte ST_Servername = 0x01;
  /**
  * Server.met special tag containg the server's description.
  */
  public final static byte ST_Description = 0x0B;
  /**
  * Server.met special tag containg the server's last ping.
  */
  public final static byte ST_Ping = 0x0C;
  /**
  * Server.met special tag containg WHAT ??
  */  
  public final static byte ST_Preference = 0x0D;
   
  // part.met or file tags
  /**
  * part.met special tag containg the filename.
  */  
  public final static byte FT_Filename = 0x01;
  /**
  * part.met special tag containg the filesize.
  */  
  public final static byte FT_Filesize = 0x02;
  /**
  * part.met special tag containg the filetype like audio or video // CHECK: Is this correct ? ...
  */  
  public final static byte FT_Filetype = 0x03;
  /**
  * part.met special tag containg the fileformat like mp3, wave or avi, mpg.
  */  
  public final static byte FT_Fileformat = 0x04;
    
  public final static byte FT_Sources = 0x15;
    
  public final static byte FT_Transfered = 0x08;
  public final static byte FT_GapStart = 0x09;
  public final static byte FT_GapEnd = 0x0A;
  public final static byte FT_Priority = 0x13;
  public final static byte FT_Status = 0x14;
  public final static byte FT_Partfilename = 0x12;
  
  public static final byte CT_Version = (byte ) 0x11;
  public static final byte CT_Port = (byte ) 0x0f;
  
  public final static int minIntByteBufferLength = 1 + 2 + 1 + 4;
  public final static int minStrByteBufferLength = 1 + 2 + 1 + 2;
    
  byte type;
  byte specialValue;
  String tagName;
  String stringValue;
  int intValue;
  
  public Tag() {
    type = -1; // FIXME: Unknown
    specialValue =0;
    stringValue = null;
    tagName=null;
  }
  
  public Tag(byte pSpecial, String pStringValue) {
    initialize( pSpecial, pStringValue );
  }
  
  public Tag(byte pSpecial, int pIntValue) {
    initialize( pSpecial, pIntValue );
  }
      
  public void initialize( byte pSpecial, String pStringValue ) {
    specialValue= pSpecial;
    stringValue = pStringValue;
    type = 2;
  }
  
  public void initialize( byte pSpecial, int pIntValue ) {
    specialValue= pSpecial;
    intValue = pIntValue;
    type = 3;
  }
  
  public boolean isSpecialTag() {
    return !( isStringTag() && isIntTag() );
  };
  
  public boolean isStringTag() {
    return type == 2;
  };
  
  public boolean isIntTag() {
    return type == 3;
  };
  
  public String getTagName() {
    if( isSpecialTag() ) return specialTagAsStr( specialValue );
    else return tagName;
  };
  
  public int getSpecialValue() {
    //assert isSpecialTag()
    
    return type;
  };
  
  public int getType() {
    return type;
  };
  
  public boolean hasName() {
    return !MiscUtil.isStrEmpty( tagName );
  };
  
  public String getStringValue() {
    assert isStringTag();
    
    return stringValue;
  };
  
  
  int getIntValue() {
    assert isIntTag();
    
    return intValue;
  };
  
/**
 * Reads one tag from the given file.
* @param    dis  the MemFileReader we read from. 
                      This reader will be internally modified. The current stream position moves further
                      as we read the data.
* @return the created tag
* @fixme this should be better a part of the servermetfile class.
* @fixme rename to readFrom( ... ).
*/
  public static Tag parseFromFile( MemFileReader dis ) throws IOException {
    Tag tag = new Tag();
    tag.type = dis.readUByte();
    int length = dis.readUInt16();
    //System.out.println( "namelength=" + length );
    if( length != 1 ) tag.tagName = dis.readASCIIString( length );
    else tag.specialValue = dis.readUByte();
    if( tag.isStringTag() ) {
      length = dis.readUInt16();
      tag.stringValue = dis.readASCIIString( length );
    } else if( tag.isIntTag() ) {
      tag.intValue = dis.readUInt32();
    };
    
    return tag;
  }
  
  public static Tag readFrom( ByteBuffer bb ) throws java.io.UnsupportedEncodingException {
    Tag tag = new Tag();
      return readFrom( bb, tag );
  }
  
  public static Tag readFrom( ByteBuffer bb, Tag tag ) throws java.io.UnsupportedEncodingException {
    tag.type = bb.get();
    byte[] byteArray;
    short length = bb.getShort();
    if( length != 1 ) {
      // What a pain in the ass !!! is there really no other way ?
      byteArray = new byte[ length ];
      bb.get( byteArray );
      tag.tagName = new String( byteArray, "US-ASCII" );
    } else tag.specialValue = bb.get();
    if( tag.isStringTag() ) {
      length = bb.getShort();
      // What a pain in the ass !!! is there really no other way ?
      byteArray = new byte[ length ];
      bb.get( byteArray );
      tag.stringValue = new String( byteArray, "US-ASCII" );
    } else if( tag.isIntTag() ) {
      tag.intValue = bb.getInt();
    };
    
    return tag;
  }
  
  /**   * Writes the tag to the given ByteBuffer.
  */
  public void writeTo( ByteBuffer buffer )  {
    MiscUtil.entering( log, buffer );

    try {
    byte[] byteArray = null;
    buffer.put( type );
    if( hasName() ) {
      byteArray = tagName.getBytes( "US-ASCII" );
      buffer.putShort( (short)byteArray.length);
      buffer.put( byteArray );
    } else {
      buffer.putShort( (short)1);
      buffer.put( specialValue );
    };
    if(type == 2) {
      byteArray = stringValue.getBytes( "US-ASCII" );
      buffer.putShort( (short)byteArray.length);
      buffer.put( byteArray );
    }
    else if(type == 3)  {
        buffer.putInt( intValue);
    };
  } catch ( Exception err ) {
    err.printStackTrace();
  };
    MiscUtil.exiting( log );
  }
  
  /**
  * mark deprecated ??
  */
    public void writeToMemFileWriter(MemFileWriter pFileWriter) throws IOException
  {
    System.out.println( "\nCreating tag = " + this);
    byte[] byteArray = null;
    pFileWriter.write(type);
    if (tagName != null)
    {
        byteArray = tagName.getBytes();
        pFileWriter.writeUInt16( byteArray.length);         // tagname size:       2 bytes
        pFileWriter.write( byteArray, 0, byteArray.length); // tagname
    }
    else
    {
        int taglen = 1;  // I suposse this is specialValue lenght
        pFileWriter.writeUInt16( taglen);                   // specialValue lenght 2 bytes
        pFileWriter.write( specialValue);                   // specialValue        1 byte
    }
    if(type == 2)
    {
        byteArray = stringValue.getBytes();
        pFileWriter.writeUInt16( byteArray.length);         // String size:        2 bytes
        pFileWriter.write( byteArray, 0, byteArray.length); // String
    }
    else if(type == 3)  // * BUT in the c++ there is "=" instead of "==" !!!
    {
        pFileWriter.writeUInt32( intValue);                 // intValue            4 bytes 
    }
  }
  
  public String toString() {
    String str = "type=" + getType();
    str += " name=" + getTagName();
    if( isStringTag() ) str += " str=" + getStringValue();
    else if( isIntTag() ) str += " int=" + getIntValue();
    else if( isSpecialTag() ) str += " special=" + getSpecialValue();
      
    return Tag.class.getName() +"(" + str + ")";
  };
  
  // TODO: Hym, helper macro which enumerates over all vars with the given MOdifiers and type and name.startsWith
  // 
  // private String 
  private String specialTagAsStr( byte special ) {
    //if( CompileOptions.isVerboseObjTracingEnabled ) {
      switch( special ) {
        //case ST_Servername: return "Servername"; // abhängig wo wir gerade sind ...
        case ST_Description: return "Description";
        case ST_Ping: return "Ping";
        case ST_Preference: return "Preference";
   
        // part.met or file tags
        case FT_Filename: return "Name"; 
        case FT_Filesize: return "Filesize";
        case FT_Filetype: return "Filetype";
        case FT_Fileformat: return "Fileformat";
    
        case FT_Sources: return "Sources";
    
        case FT_Transfered: return "Transfered";
        case FT_GapStart: return "Gapstart";
        case FT_GapEnd: return "Gabend";
        case FT_Priority: return "Priority";
        case FT_Status: return "Status";
        case FT_Partfilename: return "Partfilename";
        
        case CT_Port: return "Port";
        case CT_Version: return "Version";
      };
      return "Unknown!(" + special + ")";

  }
}
