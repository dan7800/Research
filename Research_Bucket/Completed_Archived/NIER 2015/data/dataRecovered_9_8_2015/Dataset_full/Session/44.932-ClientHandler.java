/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// ClientHandler.java

package vu.globe.svcs.gns.nameauth;


import java.io.*;
import java.net.*;

import vu.globe.svcs.gns.lib.DNS.*;
import vu.globe.svcs.gns.lib.namesrvce.*;
import vu.globe.svcs.gns.lib.objname.*;

import vu.globe.util.log.Log;
import vu.globe.util.net.*;


/**
 * This class represents the naming authority's client handler. The
 * purpose of the handler is to handle a client that has connected
 * to the naming authority. When a client connects, the handler checks if
 * the client is trusted, reads the requested operation stored inside the
 * DNS message, and checks if all preconditions are met. Example of a
 * precondition: if a directory should be removed, the handler checks if
 * the directory is empty. If the client is trusted, and all preconditions
 * hold, a DNS update message is created and sent to the
 * name server for which the naming authority is responsible. Finally,
 * the response from the server is forwarded to the client.
 *
 * Note that the handler is not a popup-thread because that would
 * require a locking mechanism to access the name server.
 */
class ClientHandler
{
  private static final boolean DEBUG = true;       // debug flag
  private static final boolean STRICT = false;     // debug flag

  private DataInputStream  _cliIn;         // client input stream
  private DataOutputStream _cliOut;        // client output stream
  private DnsMsg           _cliMsg;        // client DNS message
  private InetAddress      _srvAddr;       // name server address
  private Dname            _znames[];      // zones for which server is auth.
  private IPSelector[] 	   _trusted;  	   // per zone: trusted IP addresses
  private int              _srvPort;       // name server listening port
  private Resolver         _resv;          // DNS resolver
  private Log              _log;           // error logger
  private int              _debugLevel;    // debug level


  /**
   * Instance creation.
   *
   * @param  srvAddr     name server address
   * @param  srvPort     name server listening port
   * @param  znames      zones for which name server is authoritative
   * @param  trusted     for each zone znames[i], trusted[i] has the set of
   *			 IP addresses from which the zone may be updated
   * @param  log         message logger
   * @param  debugLevel  debug level (level 0 suppresses debug output)
   */
  public ClientHandler(InetAddress srvAddr, int srvPort, Dname znames[],
                       IPSelector[] trusted, Log log, int debugLevel)
  {
    _srvAddr = srvAddr;
    _srvPort = srvPort;
    _znames = znames;
    _trusted = trusted;
    _log = log;
    _debugLevel = debugLevel;

    /*
     * Create a resolver to communicate with the name server.
     */
    _resv = new Resolver(srvAddr, srvPort);

    _resv.setDebugLevel(_debugLevel);
  }


  /**
   * Read a DNS message from the client and perform the requested
   * operation. If all preconditions hold, a DNS update message is sent
   * to the name server, and the reply is forwarded to the client;
   * otherwise an error message is sent to the client.
   *
   * @param  sock  client socket (to be closed by the caller)
   * @return <code>true</code>;
   *         <code>false</code> if the request was not processed successfully
   */
  public boolean process(Socket sock)
  {
    ResourceRecord rr;
    Dtype type;
    Dname zname = null;
    InetAddress peer;

    try {
      peer = sock.getInetAddress();

      if (DEBUG && _debugLevel > 0) {
        System.out.println();
        System.out.println(";; received msg from " + peer);
      }

      _cliIn = new DataInputStream(sock.getInputStream());
      _cliOut = new DataOutputStream(sock.getOutputStream());

      /*
       * Read the DNS message from the client.
       */
      _cliMsg = readMsg(_cliIn);

      if (DEBUG && _debugLevel > 1) {
        _cliMsg.print();
        System.out.println();
      }

      zname = _cliMsg.getQuestion(0).getName();

      /*
       * Check if the request is valid.
       */
      try {
        checkCliReq();
      }
      catch(MalformedReqException e) {
        sendCliErrResp(NameSrvceError.BADREQ, e.getMessage(), zname);
        return false;
      }

      /*
       * Check if the peer is trusted.
       */
      if (! _trusted[getZoneIndex (zname)].selects (peer)) {
        sendCliErrResp(NameSrvceError.REFUSED, "no permission to update " +
			zname, zname);
        return false;
      }

      rr = _cliMsg.getNsRR(0);                       // the update
      type = rr.getType();

      /*
       * Perform the update operation.
       */
      switch(type.getValue()) {
        case GlobeDtype.T_GLOBE_REG:
          return regName(rr, zname);
  
        case GlobeDtype.T_GLOBE_DEREG:
          return deregName(rr, zname);

        case GlobeDtype.T_GLOBE_UPDATE:
          return updateName(rr, zname);

        case GlobeDtype.T_GLOBE_MKDIR:
          return mkDir(rr, zname);

        case GlobeDtype.T_GLOBE_RMDIR:
          return rmDir(rr, zname);
  
        default:
          sendCliErrResp(NameSrvceError.BADREQ, "unknown Globe opcode", zname);
          return false;
      }
    }
    catch(MalformedDnsMsgException e) {
      /*
       * DNS message is corrupted, use _znames[0] as zone name in the
       * error reply.
       */
      sendCliErrResp(NameSrvceError.BADREQ, e.getMessage(), _znames[0]);
      return false;
    }
    catch(IOException e) {
      if (_cliOut != null) {
        sendCliErrResp(NameSrvceError.IO, e.getMessage(),
                       (zname == null) ? _znames[0] : zname);
      }
      return false;
    }
    finally {
      if (_cliIn != null) {
        try {
          _cliIn.close();
        }
        catch(IOException e) {
        }
      }

      if (_cliOut != null) {
        try {
          _cliOut.close();
        }
        catch(IOException e) {
        }
      }

      _cliIn = null;
      _cliOut = null;
    }
  }

  /**
   * Read a DNS message from an input stream (TCP).
   *
   * @param  in  input stream to read from
   * @return the DNS message
   *
   * @exception  IOException               if an I/O error occurs
   * @exception  MalformedDnsMsgException  if the DNS message is bad
   */
  private DnsMsg readMsg(DataInputStream in)
    throws IOException, MalformedDnsMsgException
  {
    int len;
    byte msgBuf[], buf[];
    DnsMsg dnsmsg;

    /*
     * Read the message size.
     */
    len = in.readUnsignedShort();

    if (len < Dns.HDRSIZE) {
      _log.write(this.getClass().getName(), "readMsg",
                 "undersized message from client (" + len + " bytes)");
      throw new MalformedDnsMsgException("undersized message");
    }

    /*
     * Read the DNS message.
     */
    msgBuf = new byte[len];
    in.readFully(msgBuf);

    try {
      dnsmsg = new DnsMsg(msgBuf);

      /*
       * Small test to see if the DNS message was read completely.
       */
      if (STRICT) {
        buf = dnsmsg.getBytes(true);

        if (buf.length != len) {
          _log.write(this.getClass().getName(), "readMsg",
                     "length not equal: " + buf.length
                     + " (should be " + len + ")");
        }
      }
      return dnsmsg;
    }
    catch(MalformedDnameException e) {
      _log.write(this.getClass().getName(), "readMsg",
                 "bad DNS message" + getExceptionMessage(e));
      throw new MalformedDnsMsgException("bad DNS message"
                                         + getExceptionMessage(e));
    }
  }


  /**
   * Perform some checks on the request from the client.
   *
   * @exception MalformedReqException  if the request is invalid
   */
  private void checkCliReq()
    throws MalformedReqException
  {
    Opcode opcode;

    opcode = _cliMsg.getOpcode();
   
    /*
     * Only process update messages.
     */
    if (opcode.getValue() != Opcode.OP_UPDATE) {
      _log.write(this.getClass().getName(), "checkCliReq",
                 "bad request: not an update message: opcode "
                 + opcode.toString());
      throw new MalformedReqException("bad opcode");
    }

    /*
     * The message must contain exactly one question, one update and
     * zero prerequisites. It is up to the naming authority to create
     * the update message (including prerequisites) that is sent to the
     * name server.
     */
    if (_cliMsg.getQdCount() != 1
        || _cliMsg.getNsCount() != 1
        || _cliMsg.getAnCount() != 0) {

      if (_cliMsg.getQdCount() != 1) {
        _log.write(this.getClass().getName(), "checkCliReq",
                   "bad request: qd count " + _cliMsg.getQdCount());
        throw new MalformedReqException("bad qd count");
      }
      if (_cliMsg.getNsCount() != 1) {
        _log.write(this.getClass().getName(), "checkCliReq",
                   "bad request: ns count " + _cliMsg.getNsCount());
        throw new MalformedReqException("bad ns count");
      }
      else {
        _log.write(this.getClass().getName(), "checkCliReq",
                   "bad request: an count " + _cliMsg.getAnCount());
        throw new MalformedReqException("bad an count");
      }
    }

    /*
     * Check if the zone name in the zone section (aka question section) 
     * is similar to the zone for which the name server is authoritative.
     */
    if (getZoneIndex(_cliMsg.getQuestion(0).getName()) == -1) {
      _log.write(this.getClass().getName(), "checkCliReq",
                 "update for unknown zone "
                 + _cliMsg.getQuestion(0).getName().toString());
      throw new MalformedReqException("update for unknown zone");
    }
  }


  /**
   * Register a name. In case of a failure, an error response is
   * sent to the client.
   *
   * @param  rr     resource record with the name to be registered
   * @param  zname  zone to be updated
   * @return <code>true</code> if successful;
   *         <code>false</code> otherwise
   */
  public boolean regName(ResourceRecord rr, Dname zname)
  {
    DnsUpdateMsg update;
    Dname dname;

    dname = rr.getDname();

    if (DEBUG && _debugLevel > 0) {
      System.out.println(";; register(" + dname.toString() + ")");
    }

    if (ObjHandleRR.isObjHandleRR(rr) == false) {
      sendCliErrResp(NameSrvceError.BADREQ, "missing object handle", zname);
      return false;
    }

    if (getDirectory(dname.getParent(), zname) == null) {
      return false;                                   // error already sent
    }

    update = ReqMsgFactory.createRegisterNameRequest(rr, zname);

    return forwardMsg(update, zname);
  }


  /**
   * De-register a name. In case of a failure, an error response is
   * sent to the client.
   *
   * @param  rr  resource record containing the name to be de-registered
   * @param  zname  zone to be updated
   * @return <code>true</code> if successful;
   *         <code>false</code> otherwise
   */
  public boolean deregName(ResourceRecord rr, Dname zname)
  {
    DnsUpdateMsg update;
    Dname dname;

    dname = rr.getDname();

    if (DEBUG && _debugLevel > 0) {
      System.out.println(";; deregister(" + dname.toString() + ")");
    }

    if (getDirectory(dname.getParent(), zname) == null) {
      return false;                                   // error already sent
    }

    /*
     * Test if dname is not a directory.
     */
    try {
      if (GlobeDirCookie.getCookie(_resv, dname) != null) {
        sendCliErrResp(NameSrvceError.ISDIR, zname);
        return false;
      }
    }
    catch(NameNotExistException e) {
      // OK, it is not a directory.
    }
    catch(CookieNotFoundException e) {
      // OK, it is not a directory.
    }
    catch(CookieException e) {
      sendCliErrResp(NameSrvceError.STATDIR, zname);
      return false;
    }
    catch(ServFailException e) {
      sendCliErrResp(NameSrvceError.SERVFAIL, zname);
      return false;
    }
    catch(NameSrvceException e) {
      sendCliErrResp(NameSrvceError.GENERIC, e.getMessage(), zname);
      return false;
    }

    update = ReqMsgFactory.createDeregisterNameRequest(rr, zname);

    return forwardMsg(update, zname);
  }


  /**
   * Update (create or overwrite) a name. In case of a failure, an
   * error response is sent to the client.
   *
   * @param  rr  resource record containing the name to be updated
   * @param  zname  zone to be updated
   * @return <code>true</code> if successful;
   *         <code>false</code> otherwise
   */
  public boolean updateName(ResourceRecord rr, Dname zname)
  {
    DnsUpdateMsg update;
    Dname dname;

    dname = rr.getDname();

    if (DEBUG && _debugLevel > 0) {
      System.out.println(";; update(" + dname.toString() + ")");
    }

    if (ObjHandleRR.isObjHandleRR(rr) == false) {
      sendCliErrResp(NameSrvceError.BADREQ, "missing object handle", zname);
      return false;
    }

    if (getDirectory(dname.getParent(), zname) == null) {
      return false;                                   // error already sent
    }

    /*
     * Test if dname is not a directory.
     */
    try {
      if (GlobeDirCookie.getCookie(_resv, dname) != null) {
        sendCliErrResp(NameSrvceError.ISDIR, zname);
        return false;
      }
    }
    catch(NameNotExistException e) {
      // OK, it is not a directory.
    }
    catch(CookieNotFoundException e) {
      // OK, it is not a directory.
    }
    catch(CookieException e) {
      sendCliErrResp(NameSrvceError.STATDIR, zname);
      return false;
    }
    catch(ServFailException e) {
      sendCliErrResp(NameSrvceError.SERVFAIL, zname);
      return false;
    }
    catch(NameSrvceException e) {
      sendCliErrResp(NameSrvceError.GENERIC, e.getMessage(), zname);
      return false;
    }

    update = ReqMsgFactory.createUpdateNameRequest(rr, zname);

    return forwardMsg(update, zname);
  }


  /**
   * Create a directory. In case of a failure, an error response is sent
   * to the client.
   *
   * @param  rr  resource record containing the directory to be created
   * @param  zname  zone to be updated
   * @return <code>true</code> if successful;
   *         <code>false</code> otherwise
   */
  public boolean mkDir(ResourceRecord rr, Dname zname)
  {
    DnsUpdateMsg update;
    Dname dname;
    byte rdata[];

    dname = rr.getDname();

    if (DEBUG && _debugLevel > 0) {
      System.out.println(";; mkdir(" + dname.toString() + ")");
    }

    if (getDirectory(dname.getParent(), zname) == null) {
      return false;                                   // error already sent
    }

    update = ReqMsgFactory.createMakeDirectoryRequest(rr, zname);

    return forwardMsg(update, zname);
  }


  /**
   * Remove a directory. In case of a failure, an error response is sent
   * to the client.
   *
   * @param  rr  resource record containing the directory to be removed
   * @param  zname  zone to be updated
   * @return <code>true</code> if successful;
   *         <code>false</code> otherwise
   */
  public boolean rmDir(ResourceRecord rr, Dname zname)
  {
    DnsUpdateMsg update;
    Dname dname;
    GlobeDirCookie gdc;

    dname = rr.getDname();

    if (DEBUG && _debugLevel > 0) {
      System.out.println(";; rmdir(" + dname.toString() + ")");
    }

    /*
     * By definition, a directory has a parent directory, so there is no
     * need to check that here.
     */
    if ( (gdc = getDirectory(dname, zname)) == null) {
      return false;                                   // error already sent
    }

    /*
     * Test if the directory is empty. This can take a while because it
     * involves a zone transfer.
     */
    try {
      if (gdc.isEmpty(_resv) == false) {
        sendCliErrResp(NameSrvceError.DIRNOTEMPTY, zname);
        return false;
      }
    }
    catch(NameSrvceException e) {
      sendCliErrResp(NameSrvceError.GENERIC, e.getMessage(), zname);
      return false;
    }

    update = ReqMsgFactory.createRemoveDirectoryRequest(rr, zname);

    return forwardMsg(update, zname);
  }


  /**
   * Finds the index of a zone in _znames.
   *
   * @param zname	the zone to look up
   * @return	the index of zname, or -1 if zname was not found in _znames
   */
  private int getZoneIndex(Dname zname)
  {
    int i;

    for (i = 0; i < _znames.length; i++) {
      if (_znames[i].equals(zname)) {
        return i;
      }
    }
    return -1;
  }


  /**
   * Return the cookie of a directory. In case of a failure, an error
   * message is sent to the client.
   *
   * @param  dirName  name of the directory
   * @param  zname    zone name to put in an error message
   * @return the cookie if successful;
   *         <code>null</code> otherwise
   */    
  private GlobeDirCookie getDirectory(Dname dirName, Dname zname)
  {
    try {
      return GlobeDirCookie.getCookie(_resv, dirName);
    }
    catch(NameNotExistException e) {
      sendCliErrResp(NameSrvceError.NOENT, zname);
      return null;
    }
    catch(CookieNotFoundException e) {
      sendCliErrResp(NameSrvceError.NOTDIR, zname);
      return null;
    }
    catch(CookieException e) {
      sendCliErrResp(NameSrvceError.STATDIR, zname);
      return null;
    }
    catch(ServFailException e) {
      sendCliErrResp(NameSrvceError.SERVFAIL, zname);
      return null;
    }
    catch(NameSrvceException e) {
      sendCliErrResp(NameSrvceError.GENERIC, e.getMessage(), zname);
      return null;
    }
  }


  /**
   * Send a DNS message to the name server and forward the reply to the
   * client. If an error occurs, an error message is sent to the client.
   *
   * @param  dnsmsg  DNS message to be send
   * @param  zname   zone name to put in the message
   * @return <code>true</code> if successful;
   *         <code>false</code> otherwise  
   */
  private boolean forwardMsg(DnsMsg dnsmsg, Dname zname)
  {
    DnsMsg reply;
    byte buf[];

    /*
     * Send message to server and wait for reply.
     */
    try {
      if (DEBUG && _debugLevel > 0) {
        System.out.println(";; querying server");

        if (_debugLevel > 1) {
          dnsmsg.print();
          System.out.println();
        }
      }

      reply = _resv.sendmsg(dnsmsg);

      if (DEBUG && _debugLevel > 0) {
        System.out.println(";; reply from server (" + reply.size()
                           + " bytes)");

        if (_debugLevel > 1) {
          reply.print();
          System.out.println();
        }
      }
    }
    catch(TimedOutException e) {
      _log.write(this.getClass().getName(), "forwardMsg", "query timed out");
      sendCliErrResp(NameSrvceError.SERVFAIL, "query timed out", zname);
      return false;
    }
    catch(MalformedDnsMsgException e) {
      _log.write(this.getClass().getName(), "forwardMsg",
                 "invalid reply from server");
      sendCliErrResp(NameSrvceError.SERVFAIL, "invalid reply from server"
                     + getExceptionMessage(e), zname);
      return false;
    }
    catch(IOException e) {
      _log.write(this.getClass().getName(), "forwardMsg",
                 "cannot send message to name server"
                 + getExceptionMessage(e));
      sendCliErrResp(NameSrvceError.SERVFAIL, "cannot send update to server",
                     zname);
      return false;
    }

    /*
     * Read the reply code. If the name server responded with an error
     * that is specific between authority-server communication, an error
     * message is sent to the client indicating what went wrong.
     */
    switch(reply.getRcode().getValue()) {

      case Rcode.R_NOERROR:
        break;

      case Rcode.R_SERVFAIL:
        break;

      case Rcode.R_YXDOMAIN:
        break;

      case Rcode.R_YXRRSET:
        break;

      case Rcode.R_NXDOMAIN:
        break;

      case Rcode.R_NXRRSET:
        break;

      /*
       * This error generally indicates that the naming authority is not
       * authorized to send update messages.
       */
      case Rcode.R_REFUSED:
        _log.write(this.getClass().getName(), "forwardMsg",
                   "not authorized to send update messages");
        sendCliErrResp(NameSrvceError.NAMEAUTHFAIL,
                       "internal error: naming authority is not "
                       + "authorized to send update messages", zname);
        return false;

      case Rcode.R_NOTIMPL:
        _log.write(this.getClass().getName(), "forwardMsg",
                   "operation not implemented by name server");
        sendCliErrResp(NameSrvceError.NAMEAUTHFAIL,
                       "internal error: naming authority corrupted - "
                       + "operation not implemented by name server", zname);
        return false;

      case Rcode.R_NOAUTH:
        _log.write(this.getClass().getName(), "forwardMsg",
                   "name server is not authoritative");
        sendCliErrResp(NameSrvceError.SERVFAIL,
                       "internal error: name server is not "
                       + "authoritative", zname);
        return false;

      case Rcode.R_NOTZONE:
        _log.write(this.getClass().getName(), "forwardMsg",
                   "bad update sent: zone of record differs from "
                   + "zone section");
        sendCliErrResp(NameSrvceError.NAMEAUTHFAIL,
                       "internal error: naming authority sent bad update "
                       + "(NOTZONE)", zname);
        return false;

      default:
        _log.write(this.getClass().getName(), "forwardMsg",
                   "bad update sent: (rcode "
                   + reply.getRcode().getValue() + ")");
        sendCliErrResp(NameSrvceError.SERVFAIL,
                       "internal error: operation failed (rcode "
                       + reply.getRcode().getValue() + ")", zname);
        return false;
    }

    /*
     * Make the ID of the reply equal to the ID of the request.
     */
    reply.setID(_cliMsg.getID());

    /*
     * When we get here, the response code is not authority-server specific.
     * If the code indicates an error, the client should put the blame
     * on itself (or the user).
     */

    buf = reply.getBytes(true);
 
    /*
     * Forward reply to client.
     */
    try {
      _cliOut.writeShort(buf.length);
      _cliOut.write(buf);
      return true;
    }
    catch(IOException e) {
      _log.write(this.getClass().getName(), "forwardMsg",
                 "cannot write response to client" + getExceptionMessage(e));
      // do not bother to send an error message to the client
      return false;
    }
  }


  /**
   * Create a DNS 'error' message and send it to the client (via TCP).
   *
   * @param  err    the name service error
   * @param  zname  zone name to put in the message
   * @return <code>true</code> if successful;
   *         <code>false</code> otherwise
   */
  private boolean sendCliErrResp(NameSrvceError err, Dname zname)
  {
    return sendCliErrResp(err, null, zname);
  }


  /**
   * Create a DNS 'error' message and send it to the client (via TCP).
   *
   * @param  err    the name service error
   * @param  s      optional detail message
   * @param  zname  zone name to put in the message
   * @return <code>true</code> if successful;
   *         <code>false</code> otherwise
   */
  private boolean sendCliErrResp(NameSrvceError err, String s, Dname zname)
  {
    DnsMsg msg;
    String rdata;
    byte buf[];

    if (DEBUG && _debugLevel > 0) {
      if (s == null) {
        System.out.println(";; sending error " + err.toString()
                           + " to client");
      }
      else {
        System.out.println(";; sending error " + err.toString()
                           + " (" + s + ") to client");
      }
    }

    msg = new DnsMsg(_cliMsg.getID(), false, _cliMsg.getOpcode());

    msg.setRD(_cliMsg.getRD());
    msg.setRcode(GlobeRcode.GLOBEERROR);
    
    if (s == null) {
      rdata = Integer.toString(err.getValue());
    }
    else {
      rdata = Integer.toString(err.getValue()) + " " + s;
    }

    msg.addAnswerRR(zname, Dclass.IN, Dtype.TXT, NameSrvceConfig.DIR_TTL,
                    (short)rdata.length(), rdata.getBytes());

    buf = msg.getBytes(true);

    try {
      _cliOut.writeShort(buf.length);
      _cliOut.write(buf);
    }
    catch(IOException e) {
      _log.write(this.getClass().getName(), "sendCliErrResp",
                 "cannot send DNS error to client" + getExceptionMessage(e));
      return false;
    }
    return true;
  }

  /**
   * Return the detail message of an exception.
   *
   * @param  e  the exception
   * @return the detail message;
   *         <code>""</code> if there is no detail message
   */
  public String getExceptionMessage(Exception e)
  {
    String s = e.getMessage();

    return (s == null) ? "" : ": " + s;
  }

}
