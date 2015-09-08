/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// DirRR.java - Directory Resource Record

package vu.globe.svcs.gns.lib.namesrvce;


import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
import java.util.Vector;

import vu.globe.svcs.gns.lib.DNS.*;
import vu.globe.svcs.gns.lib.objname.*;

import vu.globe.svcs.gns.lib.util.*;


/**
 * This class represents a so-called GLOBEDIR DNS resource record. The
 * presence of this RR indicates that the domain name in question is a
 * directory. A GLOBEDIR RR is a TXT RR in which a magic cookie followed
 * by a fully-qualified zone name is stored in the data field. For example,
 * "GLOBEDIR globe.cs.vu.nl." The zone name is used as a loopback pointer
 * to the zone to which the directory belongs. By convention, resolving the
 * zone name results in the IP address of the name server (and naming
 * authority) that is authoritive for that zone.
 */
public class DirRR
  extends ResourceRecord
{
  /** Magic cookie */
  public static final String COOKIE = "GLOBEDIR";

  private static final boolean DEBUG = false;         // debug flag

  private Dname   _dirName;       // directory name (Dname representation)
  private ObjName _dirObjName;    // directory name (ObjName representation)
  private Dname   _zname;         // zone to which this directory belongs


  /**
   * Instance creation.
   *
   * @param  dirObjName    directory name in ObjName format
   * @param  dirName       directory name in domain name format
   * @param  zname         zone to which this directory belongs
   */
  public DirRR(ObjName dirObjName, Dname dirName, Dname zname)
  {
    super(dirName, Dclass.IN, Dtype.TXT, NameSrvceConfig.DIR_TTL);

    _dirObjName = dirObjName;
    _dirName = dirName;
    _zname = zname;

    rr_rdata = addLengthOctet(COOKIE + " " + _zname.toString());
    rr_rdlength = (short)rr_rdata.length;
  }


  /**
   * Return the zone to which this directory belongs.
   */
  public Dname getZone()
  {
    return _zname;
  }


  /**
   * Create the resource data of a GLOBEDIR RR. Note that the actual data
   * is preceded by a length octet.
   *
   * @param  zname  zone name
   * @return the byte representation of the data
   */
  public static byte[] createData(String zname)
  {
    return addLengthOctet(COOKIE + " " + zname);
  }


  /**
   * Check if a resource record is a GLOBEDIR RR.
   *
   * @param  rr  resource record to check
   * @return <code>true</code>;
   *         <code>false</code> if it is not a GLOBEDIR RR
   */
  public static boolean isDirRR(ResourceRecord rr)
  {
    byte buf[];
    String s;
    int lengthOctet;

    if (rr.getType().getValue() != Dtype.T_TXT
        || rr.getRdLength() <= COOKIE.length()) {
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
        return true;                    // MIRA check if zone is present
      }
    }
    else {
      if (s.startsWith(COOKIE, 1)) {
        return true;                    // MIRA check if zone is present
      }
    }
    return false;
  }


  /**
   * Get the zone name that is stored inside a GLOBEDIR resource record.
   *
   * @param  rr  the resource record
   * @return the zone name;
   *         <code>null</code> if rr is not a GLOBEDIR resource record
   *
   * @exception  CookieException  if the GLOBEDIR RR is corrupted
   */
  public static Dname getZone(ResourceRecord rr)
    throws CookieException
  {
    byte buf[];
    String s;
    int lengthOctet, j;
    Dname zname;
    StringTokenizer st;

    if (rr.getType().getValue() != Dtype.T_TXT
        || rr.getRdLength() <= COOKIE.length()) {
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
     * Get the zone name.
     */

    if (st.hasMoreTokens() == false) {
      throw new CookieException("missing zone name");
    }

    s = st.nextToken();

    try {
      zname = new Dname(s);

      if (st.hasMoreTokens()) {
        System.err.println("warning: GLOBEDIR RR contains trailing trash: "
                           + st.nextToken());
      }

      return zname;
    }
    catch(MalformedDnameException e) {
      throw new CookieException("invalid zone name: " + s);
    }
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
