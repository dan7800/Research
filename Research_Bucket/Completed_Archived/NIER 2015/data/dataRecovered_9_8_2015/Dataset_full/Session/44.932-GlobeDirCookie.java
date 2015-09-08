/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// GlobeDirCookie.java - Globe directory cookie

package vu.globe.svcs.gns.lib.namesrvce;


import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
import java.util.Vector;

import vu.globe.svcs.gns.lib.DNS.*;
import vu.globe.svcs.gns.lib.objname.*;
import vu.globe.svcs.gns.lib.util.*;



/**
 * This class represents a Globe directory cookie. A globe directory cookie
 * is a DNS TXT resource record with the following syntax:
 *
 *   "GLOBEDIR" zone
 *
 */
public class GlobeDirCookie
{
  private static final boolean DEBUG = false;   // debug flag
  private static final int  MAXTRIES = 3;       // max transfer attempts
  private static final int  TIMEOUT  = 20000;   // timeout interval (ms)

  private ObjName     _dirObjName;     // directory name in ObjName format
  private Dname       _dirName;        // directory name in ObjName format
  private Dname       _zname;          // zone to which directory belongs
  private InetAddress _nameAuthAddr;   // address of naming authority


  /**
   * Create a GlobeDirCookie object. Note that this method is only
   * called from within the <code>getCookie</code> method.
   *
   * @param  dirObjName    directory name in ObjName format
   * @param  dirName       directory name in domain name format
   * @param  zname         zone to which this directory belongs
   * @param  nameAuthAddr  address of naming authority
   */
  private GlobeDirCookie(ObjName dirObjName, Dname dirName, Dname zname,
                         InetAddress nameAuthAddr)
  {
    _dirObjName = dirObjName;
    _dirName = dirName;
    _zname = zname;
    _nameAuthAddr = nameAuthAddr;
  }


  /**
   * Return the ObjName representation of the directory name.
   */
  public ObjName getDirObjName()
  {
    return _dirObjName;
  }


  /**
   * Return the Dname representation of the directory name.
   */
  public Dname getDirName()
  {
    return _dirName;
  }


  /**
   * Return the zone to which this directory belongs.
   */
  public Dname getZone()
  {
    return _zname;
  }


  /**
   * Return the address of the naming authority of this directory.
   */
  public InetAddress getNameAuthAddr()
  {
    return _nameAuthAddr;
  }


  /**
   * Add a GLOBEDIR resource record (a TXT RR) to a domain name. This
   * effectively creates a new directory.
   *
   * @param  resv       DNS resolver to use to transfer queries
   * @param  dirName    name of the directory
   * @param  zname      zone to which the directory belongs
   * @param  overwrite  if set, overwrite the TXT RR
   * @return the cookie
   *
   * @exception  ServFailException   if a server failure occurs
   * @exception  NameSrvceException  if a name service error occurs
   */
  public static GlobeDirCookie setCookie(Resolver resv, Dname dirName,
                                         Dname zname, boolean overwrite)
    throws ServFailException, NameSrvceException
  {
    ObjName dirObjName;
    DnsUpdateMsg update;
    DnsMsg reply;
    byte rdata[];
    InetAddress inaddr;

    /*
     * Convert the Dname represenation of the directory name to an ObjName
     * representation.
     */
    try {
      dirObjName = new ObjName(dirName);

      /*
       * Make sure that dirObjName is absolute.
       */
      dirObjName.makeAbsolute();
    }
    catch(MalformedObjNameException e) {
      throw new NameSrvceException("cannot convert directory name to an "
                                   + "object name: " + e.getMessage());
    }

    /*
     * Check that the zone name can be resolved to an IP address.
     */
    try {
      inaddr = resolveDomainName(resv, zname.toString());
    }
    catch(NameSrvceException e) {
      throw new NameSrvceException("cannot get IP address of naming "
                                   + "authority host " + zname.toString()
                                   + ": " + e.getMessage());
    }

    /*
     * Create the update message.
     */

    update = new DnsUpdateMsg(zname);

    // Create GLOBEDIR cookie.
    rdata = DirRR.createData(zname.toString());



    // Add "Add RR" update.
    update.addAddUpdate(dirName, Dclass.IN, Dtype.TXT,
                        NameSrvceConfig.DIR_TTL, (short)rdata.length, rdata);

    if (!overwrite) {
      // Add "RRset Does Not Exist" prerequisite.
      update.addNXPrereq(dirName, Dtype.TXT);
    }

    /*
     * Send the update to the name server.
     */
    try {
      reply = resv.sendmsg(update);
    }
    catch(TimedOutException e) {
      throw new NameSrvceException("DNS query timed out");
    }
    catch(MalformedDnsMsgException e) {
      throw new NameSrvceException("DNS reply is corrupt: " + e.getMessage());
    }
    catch(IOException e) {
      throw new NameSrvceException("DNS send/receive error: " + e.getMessage());
    }

    /*
     * Throw exception if an error occurred.
     */
    if (reply.getRcode().getValue() != Rcode.R_NOERROR) {

      if (DEBUG) {
        System.out.println("update failed:");
        reply.print();
        System.out.println();
      }

      switch(reply.getRcode().getValue()) {
        case Rcode.R_SERVFAIL:
          throw new ServFailException();

        case Rcode.R_REFUSED:
          throw new NameSrvceException("operation refused by name server");

        case Rcode.R_NOTIMPL:
          throw new NameSrvceException("operation not implemented by "
                                       + "name server");

        case Rcode.R_NOAUTH:
          throw new NameSrvceException("name server is not authoritative "
                                       + "for zone " + zname);

        default:
          throw new NameSrvceException("operation failed");
      }
    }
    return new GlobeDirCookie(dirObjName, dirName, zname, inaddr);
  }


  /**
   * Check if a domain name refers to a directory (i.e., if it has a
   * GLOBEDIR resource record). If so, create and return the GlobeDirCookie
   * to access the directory.
   *
   * @param  resv     DNS resolver to use to transfer queries
   * @param  dirName  name of the directory
   * @return the cookie
   *
   * @exception  CookieException          if the GLOBEDIR RR is corrupt
   * @exception  CookieNotFoundException  if it has no GLOBEDIR RR
   * @exception  ServFailException        if a server failure occurs
   * @exception  NameNotExistException    if the name does not exist
   * @exception  NameSrvceException       if a name service error occurs
   */
  public static GlobeDirCookie getCookie(Resolver resv, Dname dirName)
    throws CookieException, CookieNotFoundException, ServFailException,
           NameNotExistException, NameSrvceException
  {
    DnsMsg reply;
    int n, i, j, lengthOctet;
    String s, cookie, rdataStr;
    byte buf[];
    ResourceRecord  rr;
    Dname zname;
    ObjName dirObjName;
    StringTokenizer st;
    InetAddress inaddr;

    if (DEBUG) {
      System.out.println("GlobeDirCookie::getCookie: checking "
                         + dirName.toString());
    }

    /*
     * Convert the Dname represenation of the directory name to an ObjName
     * representation.
     */
    try {
      dirObjName = new ObjName(dirName);

      /*
       * Make sure that dirObjName is absolute.
       */
      dirObjName.makeAbsolute();
    }
    catch(MalformedObjNameException e) {
      throw new CookieException("cannot convert directory name to an "
                                + "object name: " + e.getMessage());
    }

    /*
     * Request the TXT RRs associated with the directory name.
     */
    try {
      reply = resv.query(dirName, Dclass.IN, Dtype.TXT);
    }
    catch(TimedOutException e) {
      throw new NameSrvceException("DNS query timed out");
    }
    catch(MalformedDnsMsgException e) {
      throw new NameSrvceException("DNS reply is corrupt: " + e.getMessage());
    }
    catch(IOException e) {
      throw new NameSrvceException("DNS send/receive error: " + e.getMessage());
    }

    /*
     * Throw exception if an error occurred.
     */
    if (reply.getRcode().getValue() != Rcode.R_NOERROR) {

      if (DEBUG) {
        System.out.println("query failed:");
        reply.print();
        System.out.println();
      }

      switch(reply.getRcode().getValue()) {
        case Rcode.R_SERVFAIL:
          throw new ServFailException();

        case Rcode.R_REFUSED:
          throw new NameSrvceException("operation refused");

        case Rcode.R_NXDOMAIN:                             // FALLTHROUGH

        case Rcode.R_NXRRSET:
          throw new NameNotExistException("unknown directory");

        default:
          throw new NameSrvceException("operation failed");
      }
    }

    /*
     * BIND bug? If the name has just been removed from the domain name
     * space, the name server still responds with a NOERROR code, although
     * the reply does not contain an answer section. Obviously, you would
     * expect a NXDOMAIN or NXRRSET response code in this situation.
     */
    if ( (n = reply.getAnCount()) <= 0) {
      throw new CookieNotFoundException();
    }

    /*
     * If we get here, we have got a set of one or more TXT RRs. Examine
     * this set until the first GLOBEDIR RR is found.
     */
    for (i = 0; i < n; i++) {
      rr = reply.getAnRR(i);

      if ( (zname = DirRR.getZone(rr)) == null) {
        continue;
      }

      if (DEBUG) {
        System.out.println(";; getCookie: getting IP addr of zone "
                           + zname.toString());
      }

      /*
       * Resolve the zone name to an IP address. Note that zname must be
       * fully qualified.
       */
      try {
        inaddr = resolveDomainName(resv, zname.toString());
      }
      catch(NameSrvceException e) {
        throw new NameSrvceException("cannot get IP address of naming "
                                     + "authority host " + zname.toString()
                                     + " (as indicated by the GLOBEDIR RR of "
                                     + zname.toString()
                                     + "): " + e.getMessage());
      }

      return new GlobeDirCookie(dirObjName, dirName, zname, inaddr);
    }

    throw new CookieNotFoundException();
  }


  /**
   * Determine if this directory is empty.
   *
   * Note that this method call can be quite heavy because it involves
   * a DNS zone transfer.
   *
   * @param  resv  DNS resolver to use
   * @return <code>true</code>;
   *         <code>false</code> if the directory is not empty
   *
   * @exception  NameSrvceException  if an error occurs
   */
  public boolean isEmpty(Resolver resv)
    throws NameSrvceException
  {
    DirEntryList deList;

    deList = getDirEntries(resv, 1, false, null);

    return (deList.numEntries() == 0) ? true : false;
  }


  /**
   * Get the entries of this directory.
   *
   * Note that this method call can be quite heavy because it involves
   * a DNS zone transfer.
   *
   * @param  resv       DNS resolver to use
   * @param  recursive  if set, traverse subdirectories recursively
   * @return a DirEntryList that holds the entries
   *
   * @exception  NameSrvceException  if an error occurs
   */
  public DirEntryList getDirEntries(Resolver resv, boolean recursive)
    throws NameSrvceException
  {
    return getDirEntries(resv, 0, recursive, null);
  }


  /**
   * Get the entries of this directory.
   *
   * Note that this method call can be quite heavy because it involves
   * a complete zone transfer.
   *
   * @param  resv        DNS resolver to use
   * @param  maxEntries  max #entries to get (0 = unlimited)
   * @param  recursive   if set, traverse subdirectories recursively
   * @return a DirEntryList that holds the entries
   *
   * @exception  NameSrvceException  if an error occurs
   */
  public DirEntryList getDirEntries(Resolver resv, int maxEntries,
                                    boolean recursive)
    throws NameSrvceException
  {
    return getDirEntries(resv, maxEntries, recursive, null);
  }


  /**
   * Get the entries of a directory.
   *
   * Note that this method call can be quite heavy because it involves
   * a complete zone transfer.
   *
   * @param  resv        DNS resolver to use
   * @param  maxEntries  max #entries to get (0 = unlimited)
   * @param  recursive   if set, traverse subdirectories recursively
   * @param  pattern     a pattern that each entry name must match
   *                     (<code>null</code> disables pattern matching)
   * @return a DirEntryList that holds the entries
   *
   * @exception  NameSrvceException  if an error occurs
   */
  public DirEntryList getDirEntries(Resolver resv, int maxEntries,
                                    boolean recursive, String pattern)
    throws NameSrvceException
  {
    String s, cookie;
    int n, attempt, j, lengthOctet;
    byte buf[];
    Dname dname;
    ResourceRecord rr;
    InetAddress addr;
    int port;
    Socket sock;
    DataOutputStream out;
    DataInputStream in;
    int len, soaCount;
    byte qryBuf[], ansBuf[];
    DnsMsg reply;
    DnsQueryMsg qry;
    Rcode rcode;
    DirEntryList deList;

    /*
     * Create a zone transfer query.
     */
    qry = new DnsQueryMsg(Opcode.QUERY, _zname, Dclass.IN, Dtype.AXFR);

    addr = resv.getInetAddress();
    port = resv.getPort();

    in = null;
    out = null;
    sock = null;

    if (DEBUG) {
      System.out.println("------");
      System.out.println(";; zone transfer, querying server: ["
                         + addr + "]." + port);
      qry.print();
      System.out.println();
    }

    try {
      sock = new Socket(addr, port);
      in = new DataInputStream(sock.getInputStream());
      out = new DataOutputStream(sock.getOutputStream());

      qryBuf = qry.getBytes(true);
      deList = new DirEntryList();
      soaCount = 0;

      /*
       * Send query and wait for the responses.
       */
      for (attempt = 0; attempt < MAXTRIES; attempt++) {

        /*
         * Write 2-byte message length followed by the message.
         */
        out.writeShort(qryBuf.length);
        out.write(qryBuf);

        /*
         * Wait for responses or timeout.
         */
        for ( ; ; ) {
          try {

            sock.setSoTimeout(TIMEOUT);

            ansBuf = null;

            /*
             * Read 2-byte message length and the message.
             */
            try {
              len = in.readUnsignedShort();

              if (len < Dns.HDRSIZE) {         // undersized message
                if (DEBUG) {
                  System.out.println(";; undersized DNS message: " + len
                                     + " bytes");
                }
                throw new NameSrvceException("undersized DNS message");
              }

              ansBuf = new byte[len];
              in.readFully(ansBuf);
            }
            catch(EOFException e) {
              throw new NameSrvceException("EOF exception");
            }

            try {
              reply = new DnsMsg(ansBuf);
              rcode = reply.getRcode();

              if (rcode.getValue() != Rcode.R_NOERROR
                  || reply.getAnCount() == 0) {

                if (DEBUG) {
                  System.out.println(";; server failed: " + rcode.toString());
                }
                throw new NameSrvceException("server failed: "
                                             + rcode.toString());
              }

              // System.out.println("zone trans reply:  ");

              /*
               * Examine RRs in the answer section.
               */
              for (j = 0; j < reply.getAnCount(); j++) {
                rr = reply.getAnRR(j);

                if (rr.getType().getValue() == Dtype.T_SOA) {
                  /*
                   * Return if 'end-of-zone' has been reached, i.e. if the
                   * SOA RR appears for the second time.
                   */
                  if (soaCount > 0) {
                    if (_zname.equalsIgnoreAbs(rr.getDname())) {
                      return deList;
                    }
                  }
                  else {
                    soaCount++;
                  }
                }

                /*
                 * Add potential entry.
                 */
                addDirEntry(deList, rr, recursive, pattern);

                /*
                 * Return if we have got the requested number of entries.
                 * Note that this is likely to cause a "broken pipe" error
                 * at the server side.
                 */
                if (maxEntries > 0 && deList.numEntries() == maxEntries) {
                  return deList;
                }
              }
            }
            catch(MalformedDnameException e) {
              if (DEBUG) {
                System.out.println(";; invalid domain name in DNS message: "
                                   + e.getMessage());
              }
              continue;
            }
            catch(MalformedDnsMsgException e) {
              if (DEBUG) {
                System.out.println(";; bad DNS message: " + e.getMessage());
              }
              continue;
            }
          }
          catch(InterruptedIOException e) {
            if (DEBUG) {
              int m = attempt + 1;
              System.out.println(";; query timed out ("
                                 + m + "/" + MAXTRIES + ")");
            }

            /*
             * Throw exception if we have got interrupted during the zone
             * transfer (we do not want to bother the server with another
             * zone transfer).
             */
            if (deList.numEntries() > 0) {
              throw new NameSrvceException("DNS query timed out");
            }

            break;
          }
        }
 
        // Loopback and wait for next response.
      }
      throw new NameSrvceException("DNS query timed out");
    }
    catch(IOException e) {
      throw new NameSrvceException("IOException: " + e.getMessage());
    }
    finally {
      if (in != null) {
        try {
          in.close();
        }
        catch(IOException e) {
        }
      }

      if (out != null) {
        try {
          out.close();
        }
        catch(IOException e) {
        }
      }

      if (sock != null) {
        try {
          sock.close();
        }
        catch(IOException e) {
        }
      }

      in = null;
      out = null;
      sock = null; 
    }
  }

  /**
   * Resolve a domain name to an IP address.
   *
   * @param  resv   DNS resolver to use to transfer queries
   * @param  dname  name to be resolved
   * @return the domain name
   *
   * @exception  NameSrvceException  if an error occurs
   */
  private static InetAddress resolveDomainName(Resolver resv, String dname)
    throws NameSrvceException
  {
    try {
//      return InetAddress.getByName(dname.toString());
      return resv.resolveDname(dname.toString());
    }
    catch(UnknownHostException e) {
      throw new NameSrvceException("unknown host");
    }  
    catch(TimedOutException e) {
      throw new NameSrvceException("DNS query timed out");
    }
    catch(MalformedDnsMsgException e) {
      throw new NameSrvceException("DNS reply is corrupt"
                                   + getExceptionMessage(e));
    }
    catch(IOException e) {
      throw new NameSrvceException("DNS send/receive error"
                                   + getExceptionMessage(e));
    }
    catch(Exception e) {
      throw new NameSrvceException("DNS error" + getExceptionMessage(e));
    }
  }


  /**
   * Add an entry to the directory if it belongs to that directory.
   *
   * @param  deList     directory entry list to hold the entry
   * @param  rr         the entry to be added
   * @param  recursive  if set, entry is allowed to belong to a subdirectory
   * @param  pattern    if not <code>null</code>, entry name must pattern
   * @return <code>true</code>;
   *         <code>false</code> if the entry was not added
   */
  private boolean addDirEntry(DirEntryList deList, ResourceRecord rr,
                              boolean recursive, String pattern)
  {
    ObjName objName = null;
    int n, m;

    if (rr.getType().getValue() != Dtype.T_TXT) {
      return false;
    }

    try {
      objName = new ObjName(rr.getDname());

      /*
       * Make sure that objName is absolute.
       */
      objName.makeAbsolute();
    }
    catch(MalformedObjNameException e) {
      if (DEBUG) {
        System.out.println(";; addDirEntry: invalid object name: "
                           + objName.toString());
      }
      return false;
    }

    if (_dirObjName.isRoot() == false
        && (objName.startsWith(_dirObjName) == false
            || objName.equals(_dirObjName))) {
      return false;
    }

    n = objName.numParentDir();
    m = _dirObjName.numParentDir();

    /*
     * If recursion is not desired, ignore entries that are two or more
     * directory levels below.
     */ 
    if ((recursive == false) && (n != m + 1)) {
      return false;
    }

    /*
     * Do a simple pattern search if required.
     */
    if (pattern != null && objName.toString().lastIndexOf(pattern) == -1) {
      return false;
    }

    if (DEBUG) {
      System.out.println("addDirEntry: obj " + objName.toString()
                         + ", dir " + _dirObjName.toString());
    }

    /*
     * Add the entry to the list if it is a directory entry or an object
     * handle entry.
     */
    if (DirRR.isDirRR(rr)) {
      deList.addEntry(new DirDirEntry(objName));
      return true;
    }
    else if (ObjHandleRR.isObjHandleRR(rr)) {
      deList.addEntry(new ObjHandleDirEntry(objName,
                                  ObjHandleRR.getEncodedObjHandle(rr)));
      return true;
    }
    return false;
  }


  /**
   * Return the detail message of an exception.
   *
   * @param  e  the exception
   * @return the detail message, prefixed by ": ";
   *         <code>""</code> if there is no detail message
   */
  private static String getExceptionMessage(Exception e)
  {
    String s = e.getMessage();

    return (s == null) ? "" : ": " + s;
  }
}
