/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// NameResolver.java - name resolver

package vu.globe.svcs.gns.lib.namesrvce;


import java.net.*;
import java.io.*;
import java.util.Vector;

import vu.globe.util.base64.Base64;

import vu.globe.svcs.gns.lib.DNS.*;
import vu.globe.svcs.gns.lib.objname.*;


/**
 * This class represents a DNS-based name resolver. Although its name
 * suggests that it can only resolve object names, it is in fact the
 * client part of the Globe name service. The name resolver provides
 * the following operations: resolving object names, (de-)registering,
 * and updating {object name, object handle} pairs, creating, removing
 * and listing the contents of directories, and finally, finding
 * object names.
 */
public class NameResolver
{
  private static final boolean DEBUG = true;    // debug flag

  private String   _nameSrv;         // address of known name server
  private Resolver _nameSrvResv;     // DNS resolver for resolving names
  private int      _debugLevel;      // debug level


  /**
   * Create a NameResolver object. The resolver sends all queries to
   * a known name server, all the updates are sent to an update-specific
   * Globe naming authority.
   *
   * @param  nameSrv  host name (or IP address) of the known name server
   * @param  gnsDns   the root of the Globe name space in DNS
   *
   * @exception  UnknownHostException  if the name server's host is unknown
   * @exception  MalformedDnameException  if the name space root is invalid
   */
  public NameResolver(String nameSrv, String gnsDns)
    throws UnknownHostException, MalformedDnameException
  {
    if (DEBUG && _debugLevel > 0) {
      System.out.println(";; NameResolver(" + nameSrv + ")");
    }

    _nameSrv = nameSrv;
    _debugLevel = 0;

    /*
     * Create a resolver to communicate with the name server. This resolver
     * is only used to resolve names.
     */
    _nameSrvResv = new Resolver(nameSrv, NameSrvceConfig.NAMESRVPORT);
    _nameSrvResv.setDebugLevel(_debugLevel);

    _nameSrvResv.useTCP();                    // MIRA let resolver decide?

    ObjName.setGnsDns (gnsDns);
  }

  /**
   * Set debug level. Level 0 suppresses any debugging output. This method
   * has no effect if <code>DEBUG</code> is set to <code>false</code>.
   *
   * @param  level  debug level
   */
  public void setDebugLevel(int level)
  {
    _nameSrvResv.setDebugLevel(level);

    _debugLevel = level;
  }


  /**
   * Resolve an object name to an object handle.
   *
   * @param  objName  object name to be resolved
   * @return the object handle bytes
   *
   * @exception  ServFailException      if a server failure occurs
   * @exception  RefusedException       if the operation was refused
   * @exception  NameNotExistException  if the name does not exist
   * @exception  IsDirException         if the name refers to a directory
   * @exception  NameSrvceException     if another error occurs
   */
  public byte[] resolve(String objName)
    throws ServFailException, RefusedException, NameNotExistException,
           IsDirException, NameSrvceException
  {
    DnsMsg reply;
    int n, i;
    byte oh[];
    Dname dname;
    ResourceRecord rr;

    if (DEBUG && _debugLevel > 0) {
      System.out.println(";; resolve(" + objName + ")");
    }

    dname = objNameToDname(objName);
    reply = null;

    /*
     * Send a query for the domain name's TXT records and wait for the
     * reply.
     */
    try {
      reply = _nameSrvResv.query(dname, Dclass.IN, Dtype.TXT);
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

    switch(reply.getRcode().getValue()) {
      case Rcode.R_NOERROR:
        break;

      case Rcode.R_SERVFAIL:
        throw new ServFailException();

      case Rcode.R_REFUSED:
        throw new RefusedException();

      case Rcode.R_NXDOMAIN:                             // FALLTHROUGH

      case Rcode.R_NXRRSET:
        throw new NameNotExistException();

      default:
        throw new NameSrvceException("operation failed (rcode "
                                     + reply.getRcode().getValue() + ")");
    }

    /*
     * BIND bug? If the name has just been removed from the domain name
     * space, the name server still responds with a NOERROR code, although
     * the reply does not contain an answer section. Obviously, you would
     * expect a NXDOMAIN or NXRRSET response code in this situation.
     */
    if ( (n = reply.getAnCount()) <= 0) {
      throw new NameNotExistException();
      // throw new NameSrvceException("empty answer section");
    }

    /*
     * If we get here, we have got a set of one or more TXT RRs. Examine
     * this set and return the first object handle found.
     */
    for (i = 0; i < n; i++) {
      rr = reply.getAnRR(i);

      try {
        if ( (oh = ObjHandleRR.getObjHandle(rr)) != null) {

          if (DEBUG && _debugLevel > 1) {
            System.out.print(";; resolve(" + objName + ") --> ");
            printbytes(oh);
          }

          return oh;
        }
        else if (DirRR.isDirRR(rr)) {                 // it is a directory
          throw new IsDirException();
        }
      }
      catch(CookieException e) {
        throw new NameSrvceException(e.getMessage());
      }
    }
    throw new NameSrvceException("not an object name or missing "
                                 + "object handle");
  }


  /**
   * Add an {object name, object handle} pair to a Globe domain. The
   * operation is not performed if the object name is already used by,
   * for example, a directory or another name tuple.
   *
   * @param  objName  object name
   * @param  oh       object handle bytes
   *
   * @exception  ServFailException     if a server failure occurs
   * @exception  RefusedException      if the operation was refused
   * @exception  NameExistException    if the name already exists
   * @exception  NotDirExistException  if the parent is not a directory
   * @exception  StatDirException      if the parent dir. cannot be stat'ed
   * @exception  NameSrvceException    if another error occurs
   */
  public void register(String objName, byte oh[])
    throws ServFailException, RefusedException, NameExistException,
           NotDirException, IsDirException, StatDirException,
           NameSrvceException
  {
    DnsUpdateMsg update;
    Dname dname;
    GlobeDirCookie gdc;
    ObjHandleRR objHandleRR;

    if (DEBUG && _debugLevel > 0) {
      System.out.print(";; register(" + objName + ", ");
      printbytes(oh);
    }

    dname = objNameToDname(objName);

    try {
      gdc = getGlobeDirCookie(dname.getParent());
    }
    catch(NameNotExistException e) {
      throw new NotDirException("parent directory does not exist");
    }

    update = new DnsUpdateMsg(gdc.getZone());
    objHandleRR = new ObjHandleRR(dname, oh);

    objHandleRR.setType(GlobeDtype.GLOBE_REG);

    update.addAddUpdate(objHandleRR);

    /*
     * Send request to naming authority.
     */
    try {
      nameAuthTrans(gdc.getNameAuthAddr(), update);
    }
    catch(NameNotExistException e) {             // should not occur
      throw new NameSrvceException("internal error: object name does "
                                   + "not exist");
    }
    catch(DirNotEmptyException e) {              // should not occur
      throw new NameSrvceException("internal error: directory not empty");
    }
  }


  /**
   * Add an {object name, object handle} pair to a Globe domain. The
   * operation either results in the creation of a a new name tuple, or
   * the replacement of an existing one.
   *
   * @param  objName  object name
   * @param  oh       object handle bytes
   *
   * @exception  ServFailException     if a server failure occurs
   * @exception  RefusedException      if the operation was refused
   * @exception  NotDirExistException  if the parent is not a directory
   * @exception  IsDirException        if the name refers to a directory
   * @exception  StatDirException      if the parent dir. cannot be stat'ed
   * @exception  NameSrvceException    if another error occurs
   */
  public void update(String objName, byte oh[])
    throws ServFailException, RefusedException, NotDirException,
           IsDirException, StatDirException, NameSrvceException
  {
    DnsUpdateMsg update;
    Dname dname;
    GlobeDirCookie gdc;
    ObjHandleRR objHandleRR;

    if (DEBUG && _debugLevel > 0) {
      System.out.print(";; update(" + objName + ", ");
      printbytes(oh);
    }

    dname = objNameToDname(objName);

    try {
      gdc = getGlobeDirCookie(dname.getParent());
    }
    catch(NameNotExistException e) {
      throw new NotDirException("parent directory does not exist");
    }

    update = new DnsUpdateMsg(gdc.getZone());
    objHandleRR = new ObjHandleRR(dname, oh);

    objHandleRR.setType(GlobeDtype.GLOBE_UPDATE);

    update.addAddUpdate(objHandleRR);

    /*
     * Send request to naming authority.
     */
    try {
      nameAuthTrans(gdc.getNameAuthAddr(), update);
    }
    catch(NameExistException e) {                // should not occur
      throw new NameSrvceException("internal error: object name exists");
    }
    catch(NameNotExistException e) {             // should not occur
      throw new NameSrvceException("internal error: object name doe " +
                                   "not exist");
    }
    catch(DirNotEmptyException e) {              // should not occur
      throw new NameSrvceException("internal error: directory not empty");
    }
  }


  /**
   * Remove an {object name, object handle} pair from a Globe domain.
   * Note that each object name is bound to exactly one object handle,
   * so the object name is sufficient to identify the pair to be removed.
   *
   * @param  objName  object name to be removed
   *
   * @exception  ServFailException     if a server failure occurs
   * @exception  RefusedException      if the operation was refused
   * @exception  NameNotExistException if the name does not exist
   * @exception  NotDirExistException  if the parent is not a directory
   * @exception  IsDirException        if the name refers to a directory
   * @exception  StatDirException      if the parent dir. cannot be stat'ed
   * @exception  NameSrvceException    if another error occurs
   */
  public void deregister(String objName)
    throws ServFailException, RefusedException, NameNotExistException,
           NotDirException, IsDirException, StatDirException,
           NameSrvceException
  {
    DnsUpdateMsg update;
    Dname dname;
    GlobeDirCookie gdc;

    if (DEBUG && _debugLevel > 0) {
      System.out.println(";; deregister(" + objName + ")");
    }

    dname = objNameToDname(objName);

    try {
      gdc = getGlobeDirCookie(dname.getParent());
    }
    catch(NameNotExistException e) {
      throw new NotDirException("parent directory does not exist");
    }

    update = new DnsUpdateMsg(gdc.getZone());

    update.addDelUpdate(dname, GlobeDtype.GLOBE_DEREG);

    /*
     * Send request to naming authority.
     */
    try {
      nameAuthTrans(gdc.getNameAuthAddr(), update);
    }
    catch(NameExistException e) {             // should not occur
      throw new NameSrvceException("internal error: object name exists");
    }
    catch(DirNotEmptyException e) {           // should not occur
      throw new NameSrvceException("internal error: directory not empty");
    }
  }


  /**
   * Create a directory.
   *
   * @param  dirName  name of the directory to be created
   *
   * @exception  ServFailException     if a server failure occurs
   * @exception  RefusedException      if the operation was refused
   * @exception  NameExistException    if the name already exists
   * @exception  NotDirExistException  if the parent is not a directory
   * @exception  StatDirException      if the parent dir. cannot be stat'ed
   * @exception  NameSrvceException    if another error occurs
   */
  public void mkdir(String dirName)
    throws ServFailException, RefusedException, NameExistException,
           NotDirException, StatDirException, NameSrvceException
  {
    DnsUpdateMsg update;
    Dname dname;
    GlobeDirCookie gdc;

    if (DEBUG && _debugLevel > 0) {
      System.out.println(";; mkdir(" + dirName + ")");
    }

    dname = objNameToDname(dirName);

    try {
      gdc = getGlobeDirCookie(dname.getParent());
    }
    catch(NameNotExistException e) {
      throw new NotDirException("parent directory does not exist");
    }

    update = new DnsUpdateMsg(gdc.getZone());

    update.addAddUpdate(dname, Dclass.IN, GlobeDtype.GLOBE_MKDIR,
                        NameSrvceConfig.DIR_TTL, (short)0, null);

    /*
     * Send request to naming authority.
     */
    try {
      nameAuthTrans(gdc.getNameAuthAddr(), update);
    }
    catch(NameNotExistException e) {             // should not occur
      throw new NameSrvceException("internal error: object name does " +
                                   "not exist");
    }
    catch(DirNotEmptyException e) {              // should not occur
      throw new NameSrvceException("internal error: directory not empty");
    }
    catch(IsDirException e) {                    // should not occur
      throw new NameSrvceException("internal error: is directory");
    }
  }


  /**
   * Remove a directory. This directory must be empty.
   *
   * @param  dirName  name of the directory to be removed
   *
   * @exception  ServFailException      if a server failure occurs
   * @exception  RefusedException       if the operation was refused
   * @exception  NameNotExistException  if the name does not exist
   * @exception  DirNotEmptyException   if the directory is not empty
   * @exception  NotDirExistException   if the name does not refer to a dir.
   * @exception  StatDirException       if the directory cannot be stat'ed
   * @exception  NameSrvceException     if another error occurs
   */
  public void rmdir(String dirName)
    throws ServFailException, RefusedException, NameNotExistException,
           DirNotEmptyException, NotDirException, StatDirException,
           NameSrvceException
  {
    DnsUpdateMsg update;
    Dname dname;
    GlobeDirCookie gdc;

    if (DEBUG && _debugLevel > 0) {
      System.out.println(";; rmdir(" + dirName + ")");
    }

    dname = objNameToDname(dirName);
    gdc = getGlobeDirCookie(dname);

    update = new DnsUpdateMsg(gdc.getZone());

    update.addDelUpdate(dname, GlobeDtype.GLOBE_RMDIR);

    /*
     * Send request to naming authority.
     */
    try {
      nameAuthTrans(gdc.getNameAuthAddr(), update);
    }
    catch(NameExistException e) {                // should not occur
      throw new NameSrvceException("internal error: object name exists");
    }
    catch(IsDirException e) {                    // should not occur
      throw new NameSrvceException("internal error: is directory");
    }
  }


  /**
   * List (i.e., get) the contents of a directory.
   *
   * @param  dirName     object name of the directory to be listed
   * @param  recursive   if set, traverse directories recursively
   * @return a DirEntryList with the directory entries
   *
   * @exception  ServFailException      if a server failure occurs
   * @exception  RefusedException       if the operation was refused
   * @exception  NameNotExistException  if the name does not exist
   * @exception  NotDirExistException   if the name does not refer to a dir.
   * @exception  StatDirException       if the directory cannot be stat'ed
   * @exception  NameSrvceException     if another error occurs
   */
  public DirEntryList lsdir(String dirName, boolean recursive)
    throws ServFailException, RefusedException, NameNotExistException,
           NotDirException, StatDirException, NameSrvceException
  {
    if (DEBUG && _debugLevel > 0) {
      System.out.println(";; lsdir(" + dirName + ")");
    }

    return getDirEntries(dirName, null, recursive);
  }


  /**
   * Find files.
   *
   * @param  dirName  directory to start with
   * @param  pattern     the substring to look for
   * @param  recursive   if set, traverse directories recursively
   * @return a DirEntryList with the directory entries
   *
   * @exception  ServFailException      if a server failure occurs
   * @exception  RefusedException       if the operation was refused
   * @exception  NameNotExistException  if the name does not exist
   * @exception  NotDirExistException   if the name does not refer to a dir.
   * @exception  StatDirException       if the directory cannot be stat'ed
   * @exception  NameSrvceException     if another error occurs
   */
  public DirEntryList find(String startDirObjName, String pattern,
                           boolean recursive)
    throws ServFailException, RefusedException, NameNotExistException,
           NotDirException, StatDirException, NameSrvceException
  {
    if (DEBUG && _debugLevel > 0) {
      System.out.println(";; find(" + startDirObjName + ", " + pattern + ")");
    }

    return getDirEntries(startDirObjName, pattern, recursive);
  }


  /**
   * Resolve a domain name to an IP address.
   *
   * @param  domainName  domain name to be resolved
   * @return the IP address
   *
   * @exception  UnknownHostException     if the host is unknown
   * @exception  MalformedDnameException  if the domain name is invalid
   * @exception  NameSrvceException       if another error occurs
   */
  public InetAddress resolveDname(String domainName)
    throws UnknownHostException,
           MalformedDnameException, NameSrvceException
  {
    if (DEBUG && _debugLevel > 0) {
      System.out.println(";; resolveDname(" + domainName + ")");
    }

    try {
      return _nameSrvResv.resolveDname(domainName);
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
      throw new NameSrvceException("DNS error"
                                   + getExceptionMessage(e));
    }
  }


  /**
   * Get the entries from a directory, or multiple directories if
   * recursion is desired. If a pattern is defined, then only those
   * entries that satisfy this pattern are returned.
   *
   * @param  dirName     object name of the directory to be listed
   * @param  pattern     substring to look for
   * @param  recursive   if set, traverse directories recursively
   * @return a DirEntryList with the directory entries
   *
   * @exception  ServFailException      if a server failure occurs
   * @exception  RefusedException       if the operation was refused
   * @exception  NameNotExistException  if the name does not exist
   * @exception  NotDirExistException   if the name does not refer to a dir.
   * @exception  StatDirException       if the directory cannot be stat'ed
   * @exception  NameSrvceException     if another error occurs
   */
  private DirEntryList getDirEntries(String dirName, String pattern,
                                     boolean recursive)
    throws ServFailException, RefusedException, NameNotExistException,
           NotDirException, StatDirException, NameSrvceException
  {
    DirEntryList deList;
    int i;
    DirEntry de, result[];
    Dname dname;
    GlobeDirCookie gdc;
    Resolver resv;

    dname = objNameToDname(dirName);
    gdc = getGlobeDirCookie(dname);

    /*
     * Use the naming authority's address to send the zone-transfer-request
     * to. By convenction, the naming authority must run on the same machine
     * as the name server that is authoritative for the designated domain.
     */
    resv = new Resolver(gdc.getNameAuthAddr(), NameSrvceConfig.NAMESRVPORT);

    resv.setDebugLevel(_debugLevel);
    resv.useTCP();                      // zone transfers require TCP

    if (pattern == null) {
      deList = gdc.getDirEntries(resv, 0, recursive);
    }
    else {
      deList = gdc.getDirEntries(resv, 0, recursive, pattern);
    }

    if (DEBUG && _debugLevel > 0) {
      System.out.println(";; no. of directory entries "
                         + deList.numEntries());
    }

    return deList;
  }


  /**
   * Send a message to a naming authority and wait for the reply. If the
   * reply indicates an error condition, an exception is thrown.
   *
   * @param  nameAuthAddr  address of the naming authority
   * @param  update        message to be sent
   *
   * @exception  ServFailException          if a server failure occurs
   * @exception  RefusedException           if the operation was refused
   * @exception  NameExistException         if a name does exists
   * @exception  NameNotExistException      if a name does not exist
   * @exception  DirNotEmptyExistException  if a directory is not empty
   * @exception  NotDirExistException       if a name does not refer to a dir.
   * @exception  IsDirExistException        if a name refers to a directory
   * @exception  StatDirException           if a directory cannot be stat'ed
   * @exception  NameSrvceException         if another error occurs
   */
  private void nameAuthTrans(InetAddress nameAuthAddr, DnsUpdateMsg update)
    throws ServFailException, RefusedException, NameExistException,
           NameNotExistException, DirNotEmptyException, NotDirException,
           IsDirException, StatDirException, NameSrvceException
  {
    Resolver resv;
    DnsMsg reply;

    resv = new Resolver(nameAuthAddr, NameSrvceConfig.NAMEAUTHPORT);

    resv.setDebugLevel(_debugLevel);
    resv.useTCP();                    // naming authority requires TCP

    try {
      reply = resv.sendmsg(update);

      switch(reply.getRcode().getValue()) {

        case Rcode.R_NOERROR:
          break;

        case Rcode.R_SERVFAIL:
          throw new ServFailException();

        case Rcode.R_REFUSED:
          throw new RefusedException();

        case Rcode.R_YXDOMAIN:                             // FALLTHROUGH

        case Rcode.R_YXRRSET:
          throw new NameExistException();

        case Rcode.R_NXDOMAIN:                             // FALLTHROUGH

        case Rcode.R_NXRRSET:
          throw new NameNotExistException();

        /*
         * The naming authority responded with a Globe-specific error.
         */
        case GlobeRcode.R_GLOBEERROR:
          throwGlobeError(reply);
                                               // NOTREACHED, FALLTHROUGH

        default:
          throw new NameSrvceException("operation failed (rcode "
                                       + reply.getRcode().getValue() + ")");
      }
    }
    catch(TimedOutException e) {
      throw new NameSrvceException("DNS query timed out");
    }
    catch(MalformedDnsMsgException e) {
      throw new NameSrvceException("DNS reply is corrupt");
    }
    catch(IOException e) {
      throw new NameSrvceException("DNS send/receive error"
                                   + getExceptionMessage(e));
    }
  }


  /**
   * Map a Globe name service error onto an exception, and throw it.
   * This error is stored in the TXT RR inside the answer section of a
   * DNS message.
   *
   * @param  msg  DNS message with the error
   *
   * @exception  ServFailException          if a server failure occurs
   * @exception  RefusedException           if the operation was refused
   * @exception  NameExistException         if a name does exists
   * @exception  NameNotExistException      if a name does not exist
   * @exception  DirNotEmptyExistException  if a directory is not empty
   * @exception  NotDirExistException       if a name does not refer to a dir.
   * @exception  IsDirExistException        if a name refers to a directory
   * @exception  StatDirException           if a directory cannot be stat'ed
   * @exception  NameSrvceException         if another error occurs
   */
  private void throwGlobeError(DnsMsg msg)
    throws ServFailException, RefusedException, NameExistException,
           NameNotExistException, DirNotEmptyException, NotDirException,
           IsDirException, StatDirException, NameSrvceException
  {
    ResourceRecord rr;
    int errCode;
    String errStr, data;

    if (msg.getAnCount() != 1) {
      throw new NameSrvceException("unknown error");
    }

    rr = msg.getAnRR(0);

    if (rr.getRdLength() < NameSrvceError.codeLength
        || rr.getType().getValue() != Dtype.T_TXT) {
      throw new NameSrvceException("unknown error");
    }

    data = new String(rr.getRdata());

    try {
      errCode = Integer.parseInt(data.substring(0, NameSrvceError.codeLength));
    }
    catch(NumberFormatException e) {
      throw new NameSrvceException("unknown error code");
    } 

    if (data.length() > NameSrvceError.codeLength) {
      errStr = data.substring(NameSrvceError.codeLength + 1);  // plus 1 SP
    }
    else {
      errStr = null;
    }

    switch(errCode) {
      case NameSrvceError.ERR_GENERIC:
        throw new NameSrvceException(errStr);

      case NameSrvceError.ERR_NOENT:
        throw new NotDirException(errStr);

      case NameSrvceError.ERR_NOTDIR:
        throw new NotDirException(errStr);

      case NameSrvceError.ERR_ISDIR:
        throw new IsDirException(errStr);

      case NameSrvceError.ERR_STATDIR:
        throw new StatDirException(errStr);

      case NameSrvceError.ERR_DIRNOTEMPTY:
        throw new DirNotEmptyException(errStr);

      case NameSrvceError.ERR_BADREQ:
        throw new NameSrvceException("bad request: " + errStr);

      case NameSrvceError.ERR_IO:
        throw new NameSrvceException("I/O failure: " + errStr);

      case NameSrvceError.ERR_SERVFAIL:
        throw new NameSrvceException("server failure: " + errStr);

      case NameSrvceError.ERR_NAMEAUTHFAIL:
        throw new NameSrvceException("naming authority failure: " + errStr);

      case NameSrvceError.ERR_REFUSED:
        throw new NameSrvceException("refused: " + errStr);

      default :
        throw new NameSrvceException("unknown Globe error: error code: "
                                     + errCode + ", " + errStr);
    }
  }


  /**
   * Convert an object name to a domain name.
   *
   * @param  objName  the object name in String format
   * @return the domain name
   *
   * @exception  NameSrvceException  if an error occurs
   */
  private Dname objNameToDname(String objName)
    throws NameSrvceException
  {
    ObjName oname;

    try {
      oname = new ObjName(objName);

      return oname.toDname();
    }
    catch(MalformedObjNameException e) {
      throw new NameSrvceException("invalid object name: " + e.getMessage());
    }
    catch(MalformedDnameException e) {
      throw new NameSrvceException("cannot convert object name to domain name");
    }
  }


  /**
   * Get the cookie of a directory.
   *
   * @param  dirName  the name of the directory
   *
   * @exception  ServFailException      if a server failure occurs
   * @exception  NameNotExistException  if the name does not exist
   * @exception  NotDirExistException   if the name does not refer to a dir.
   * @exception  StatDirException       if the directory cannot be stat'ed
   * @exception  NameSrvceException     if another error occurs
   */
  private GlobeDirCookie getGlobeDirCookie(Dname dirName)
    throws ServFailException, NameNotExistException, NotDirException,
           StatDirException, NameSrvceException
  {
    try {
      return GlobeDirCookie.getCookie(_nameSrvResv, dirName);
    }
    catch(CookieNotFoundException e) {
      throw new NotDirException("not a directory");
    }
    catch(CookieException e) {
      System.err.println("cannot stat directory " + dirName
                         + getExceptionMessage(e));
      throw new StatDirException("cannot stat directory " + dirName
                                 + getExceptionMessage(e));
    }
  }


  /**
   * Print a sequence of bytes to standard output in hexadecimal format.
   */
  private void printbytes(byte buf[])
  {
    for (int i = 0; i < buf.length; i++) {
      if (i > 0) {
        System.out.print(",");
      }
      System.out.print(Integer.toHexString((int)buf[i] & 0xff));
    }
    System.out.println(" (" + buf.length + " bytes)");
  }


  /**
   * Return the detail message of an exception.
   *
   * @param  e  the exception
   * @return the detail message, prefixed by ": ";
   *         <code>""</code> if there is no detail message
   */
  private String getExceptionMessage(Exception e)
  {
    String s = e.getMessage();

    return (s == null) ? "" : ": " + s;
  }
}
