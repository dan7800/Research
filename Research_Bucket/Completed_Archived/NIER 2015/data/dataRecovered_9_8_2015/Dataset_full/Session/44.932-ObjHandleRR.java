/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// ObjHandleRR.java -  Object Handle Resource Record

package vu.globe.svcs.gns.lib.namesrvce;


import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

import vu.globe.util.base64.Base64;
import vu.globe.svcs.gns.lib.DNS.*;


/**
 * This class represents a so-called OBJHANDLE DNS resource record. If
 * a domain name has an OBJHANDLE RR, it means that the domain name is
 * an object name. An OBJHANDLE RR is a TXT RR in which a magic cookie
 * followed by an object handle is stored (in base64 format). When an
 * object name is resolved, the receiver (i.e., resolver) requests all
 * TXT RRs associated with the corresponding domain name. If one of these
 * records is an OBJHANDLE RR, then the object-name resolution is
 * considered successful.
 */
public class ObjHandleRR
  extends ResourceRecord
{
  /** Magic cookie */
  public static final String COOKIE = "GLOBEOBJHANDLE";

  private static final boolean DEBUG = false;           // debug flag

  private byte _objHandle[];                   // object handle bytes


  /**
   * Instance creation.
   *
   * @param  objName    the object name
   * @param  objHandle  the bytes of the object handle associated with
   *                    the object name
   */
  public ObjHandleRR(Dname objName, byte objHandle[])
  {
    super(objName, Dclass.IN, Dtype.TXT, NameSrvceConfig.OBJNAME_TTL);

    String s;
    byte buf[];

    /*
     * Create OBJHANDLE RR data.
     */
    s = COOKIE + " " + new String(Base64.encode(objHandle));

    _objHandle  = new byte[objHandle.length];
    System.arraycopy(objHandle, 0, _objHandle, 0, objHandle.length);

    rr_rdata = addLengthOctet(s);
    rr_rdlength = (short)rr_rdata.length;         
  }


  /**
   * Return a string representation of the object handle.
   */
  public String getObjHandle()
  {
    return new String(_objHandle);
  }

 
  /**
   * Create the data of an OBJHANDLE RR. Note that the actual data is
   * preceded by a length octet.
   *
   * @param  objHandle  the object handle bytes
   * @return a byte representation of the data
   */ 
  public static byte[] createData(byte objHandle[])
  {
    return addLengthOctet(COOKIE + " " + new String(Base64.encode(objHandle)));
  }


  /**
   * Check if a resource record is an OBJHANDLE RR.
   *
   * @param  rr  resource record to check
   * @return <code>true</code>;
   *         <code>false</code> if it is not an OBJHANDLE RR
   */
  public static boolean isObjHandleRR(ResourceRecord rr)
  {
    byte buf[];
    String s;
    int lengthOctet;

    /*
     * Do not check RR type! It should be T_GLOBE_REG, or T_GLOBE_UPDDATE,
     * or T_TXT.
     */

    if ( rr.getRdLength() <= COOKIE.length()) {
      return false;
    }

    buf = rr.getRdata();
    s = new String(buf);

    /*
     * Test if the data starts with a length octet. For some reason,
     * BIND inserts a length octet in the data field of a TXT RR.
     */
    lengthOctet = toUnsignedInt(buf[0]);
    if (lengthOctet + 1 != rr.getRdLength()) {
      if (s.startsWith(COOKIE)) {
        return true;                 // MIRA check if objhandle is present
      }
    }
    else {
      if (s.startsWith(COOKIE, 1)) {
        return true;                 // MIRA check if objhandle is present
      }
    }
    return false;
  }


  /**
   * Get the object handle that is stored inside an OBJHANDLE resource record.
   *
   * @param  rr  the resource record
   * @return the object handle bytes;
   *         <code>null</code> if rr is not an OBJHANDLE resource record
   *
   * @exception  CookieException  if the OBJHANDLE RR is corrupted
   */
  public static byte[] getObjHandle(ResourceRecord rr)
    throws CookieException
  {
    String s;

    if ( (s = doGetEncodedObjHandle(rr)) == null) {
      return null;
    }

    try {
      return Base64.decode(s.getBytes());
    }
    catch(NumberFormatException e) {
      throw new CookieException("cannot decode object handle: "
                                + e.getMessage());
    }
  }


  /**
   * Get the encoded object handle (i.e., in base64 format) that is stored
   * inside an OBJHANDLE resource record.
   *
   * @param  rr  the resource record
   * @return the encoded object handle;
   *         <code>null</code> if rr is not an OBJHANDLE RR or if it
   *         is corrupt
   */
  public static String getEncodedObjHandle(ResourceRecord rr)
  {
    try {
      return doGetEncodedObjHandle(rr);
    }
    catch(CookieException e) {
      return null;
    }
  }


  /**
   * Get the encoded object handle (i.e., in base64 format) that is stored
   * inside an OBJHANDLE resource record.
   *
   * @param  rr  the resource record
   * @return the object handle bytes;
   *         <code>null</code> if rr is not an OBJHANDLE resource record
   *
   * @exception  CookieException  if the OBJHANDLE RR is corrupted
   */
  private static String doGetEncodedObjHandle(ResourceRecord rr)
    throws CookieException
  {
    byte buf[];
    String s;
    int lengthOctet, j;
    StringTokenizer st;

    /*
     * Do not check RR type! It should be T_GLOBE_REG, or T_GLOBE_UPDDATE.
     */

    if (rr.getRdLength() <= COOKIE.length()) {
      return null;
    }

    buf = rr.getRdata();

    /*
     * Test if the data starts with a length octet. For some reason,
     * BIND inserts a length octet in the data field of a TXT RR.
     */
    lengthOctet = toUnsignedInt(buf[0]);
    j = (lengthOctet + 1 != rr.getRdLength()) ? 0 : 1;

    st = new StringTokenizer(new String(buf, j, buf.length - j));
 
    /*
     * Return if there is no cookie present.
     */
    if ((st.hasMoreTokens() == false)
        || (st.nextToken().equals(COOKIE) == false)) {
      return null;
    }
 
    /*
     * Get the object handle.
     */
 
    if (st.hasMoreTokens() == false) {
      throw new CookieException("missing object handle");
    }

    s = st.nextToken();

    if (DEBUG) {
      System.out.println("ObjHandleRR::getEncodedObjHandle: objhandle " + s);
    }

    if (st.hasMoreTokens()) {
      System.err.println("warning: OBJHANDLE RR contains trailing trash: "
                         + st.nextToken());
    }
 
    return s;
  }


  /**
   * Convert a byte value to an unsigned integer value.
   */
  public static int toUnsignedInt(byte x)
  {
    return (int)(x) & 0xff;
  }


  /**
   * Convert a string to a byte array and put a length octet at index 0.
   * The value of the length octet is equal to the length of the byte
   * array plus 1.
   */
  private static byte[] addLengthOctet(String s)
  {
    byte buf[], buf2[];

    buf = s.getBytes();
    buf2 = new byte[buf.length + 1];
    System.arraycopy(buf, 0, buf2, 1, buf.length);
    buf2[0] = (byte)buf.length;

    return buf2;
  }
}
