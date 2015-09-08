/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// ReqMsgFactory.java

package vu.globe.svcs.gns.nameauth;


import java.io.*;
import java.net.*;

import vu.globe.svcs.gns.lib.DNS.*;
import vu.globe.svcs.gns.lib.namesrvce.*;


/**
 * This class represents a factory to create DNS update messages to be
 * sent to the name server.
 */
class ReqMsgFactory
{
  private static final boolean REMOVEALL = true;   // MIRA test


  /**
   * Create a request to register a name.
   *
   * @param  rr     resource record with the name to be registered
   * @param  zname  zone to be updated
   * @return the request
   */
  public static DnsUpdateMsg createRegisterNameRequest(
                               ResourceRecord rr, Dname zname)
  {
    DnsUpdateMsg update;
    Dname dname;

    // ASSERT ObjHandleRR.isObjHandleRR(rr) == true

    dname = rr.getDname();

    update = new DnsUpdateMsg(zname);

    // Add "Add RR" update.
    update.addAddUpdate(dname, Dclass.IN, Dtype.TXT,
                        NameSrvceConfig.OBJNAME_TTL, rr.getRdLength(),
                        rr.getRdata());

    // Add "RRset Does Not Exist" prerequisite.
    update.addNXPrereq(dname, Dtype.TXT);

    return update;
  }


  /**
   * Create a request to de-register (remove) a name.
   *
   * @param  rr  resource record containing the name to be de-registered
   * @param  zname  zone to be updated
   * @return the request
   */
  public static DnsUpdateMsg createDeregisterNameRequest(
                               ResourceRecord rr, Dname zname)
  {
    DnsUpdateMsg update;
    Dname dname;

    dname = rr.getDname();

    update = new DnsUpdateMsg(zname);

    if (REMOVEALL) {
      update.addDelFromNameUpdate(dname);
    }
    else {
      // Add "Remove all TXT RRs from a name" update.
      update.addDelUpdate(dname, Dtype.TXT);
    }

    // Add "RRset Exists" prerequisite.
    update.addYXPrereq(dname, Dtype.TXT);

    return update;
  }


  /**
   * Create a request to update a name.
   *
   * @param  rr  resource record containing the name to be updated
   * @param  zname  zone to be updated
   * @return the request
   */
  public static DnsUpdateMsg createUpdateNameRequest(
                               ResourceRecord rr, Dname zname)
  {
    DnsUpdateMsg update;
    Dname dname;

    // ASSERT ObjHandleRR.isObjHandleRR(rr) == truei

    dname = rr.getDname();

    update = new DnsUpdateMsg(zname);

    if (REMOVEALL) {
      update.addDelFromNameUpdate(dname);
    }
    else {
      // Add "Remove all TXT RRs from a name" update.
      update.addDelUpdate(dname, Dtype.TXT);
    }

    // Add "Add RR" update.
    update.addAddUpdate(dname, Dclass.IN, Dtype.TXT,
                        NameSrvceConfig.OBJNAME_TTL, rr.getRdLength(),
                        rr.getRdata());

    return update;
  }


  /**
   * Create a request to make a directory.
   *
   * @param  rr  resource record containing the directory to be created
   * @param  zname  zone to be updated
   * @return the request
   */
  public static DnsUpdateMsg createMakeDirectoryRequest(
                               ResourceRecord rr, Dname zname)
  {
    DnsUpdateMsg update;
    Dname dname;
    byte rdata[];

    dname = rr.getDname();

    update = new DnsUpdateMsg(zname);

    // Create GLOBEDIR cookie.
    rdata = DirRR.createData(zname.toString());

    // Add "Add RR" update.
    update.addAddUpdate(dname, Dclass.IN, Dtype.TXT, NameSrvceConfig.DIR_TTL,
                        (short)rdata.length, rdata);

    // Add "RRset Does Not Exist" prerequisite.
    update.addNXPrereq(dname, Dtype.TXT);

    return update;
  }


  /**
   * Create a request to remove a directory.
   *
   * @param  rr  resource record containing the directory to be removed
   * @param  zname  zone to be updated
   * @return the request
   */
  public static DnsUpdateMsg createRemoveDirectoryRequest(
                               ResourceRecord rr, Dname zname)
  {
    DnsUpdateMsg update;
    Dname dname;

    dname = rr.getDname();

    update = new DnsUpdateMsg(zname);

    if (REMOVEALL) {
      update.addDelFromNameUpdate(dname);
    }
    else {
      // Add "Remove all TXT RRs from a name" update.
      update.addDelUpdate(dname, Dtype.TXT);
    }

    // Add "RRset Exists" prerequisite.
    update.addYXPrereq(dname, Dtype.TXT);

    return update;
  }
}
