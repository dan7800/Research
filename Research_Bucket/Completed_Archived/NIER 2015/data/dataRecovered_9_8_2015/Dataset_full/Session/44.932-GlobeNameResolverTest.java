/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// GlobeNameResolverTest.java 


import java.net.*;

import vu.globe.util.base64.Base64;

import vu.globe.util.comm.*;
import vu.globe.util.comm.idl.rawData;
import vu.globe.svcs.gns.idl.*;                 // nameResolver.dirEntries
import vu.globe.svcs.gns.lib.namesrvce.*;   // GlobeDirCookie
import vu.globe.svcs.gns.gresv.*;           // name resolver (Globe object)


class GlobeNameResolverTest
{
  static final String progName = "GlobeNameResolverTest";

  
  public static void main(String argv[])
  {
    int n, i, debugLevel, sortMethod;
    String server, op, s, tp, objName;
    GlobeNameResolver resv;
    boolean recursive;

    if (argv.length < 3) {
      usage();
    }

    debugLevel = 0;
    recursive = false;
    sortMethod = DirEntryList.SORT_ASCENDING;

    for (i = 0; i < argv.length; i++) {

      if (argv[i].charAt(0) != '-') {
        break;
      }

      if (argv[i].length() < 2) {
        usage();
      }

      switch(argv[i].charAt(1)) {

        case 'd' :
          try {
            debugLevel = Integer.parseInt(argv[i].substring(2));
          }
          catch(NumberFormatException e) {
            fatal("invalid debug level: " + e.getMessage());
          }
          break;

        case 'r' :
          recursive = true;
          break;

        case 's' :
          try {
            n = Integer.parseInt(argv[i].substring(2));

            switch(n) {
              case 0 :
                sortMethod = DirEntryList.SORT_NONE;
                break;

              case 1 :
                sortMethod = DirEntryList.SORT_ASCENDING;
                break;

              case 2 :
                sortMethod = DirEntryList.SORT_REVERSE;
                break;

              default :
                fatal("invalid sort method");
            }
          }
          catch(NumberFormatException e) {
            fatal("invalid sort method: " + e.getMessage());
          }
          break;

        default :
          usage();
      }
    }

    if (i > argv.length - 3) {
      usage();
    }

    server  = argv[i];
    op = argv[i + 1];
    objName = argv[i + 2];
    resv = null;

    try {
      resv = new GlobeNameResolver();

      resv.debuglevel(debugLevel);
//    resv.init(server);
    }
    catch(Exception e) {
      fatal("cannot create resolver: unknown name server: " + e.getMessage());
    }

    if (op.equals("reg")) {
      if (i != argv.length - 4) {
        usage();
      }
      do_register(resv, objName, argv[i + 3].getBytes());
    }
    else if (op.equals("upd")) {
      if (i != argv.length - 4) {
        usage();
      }
      do_update(resv, objName, argv[i + 3].getBytes());
    }
    else if (op.equals("dereg")) {
      do_deregister(resv, objName);
    }
    else if (op.equals("mkdir")) {
      do_mkdir(resv, objName);
    }
    else if (op.equals("rmdir")) {
      do_rmdir(resv, objName);
    }
    else if (op.equals("resv")) {
      do_resolve(resv, objName);
    }
    else if (op.equals("lsdir")) {
      do_lsdir(resv, objName, recursive, sortMethod);
    }
    else if (op.equals("find")) {
      if (i != argv.length - 4) {
        usage();
      }
      do_find(resv, objName, argv[i + 3], recursive, sortMethod);
    }
    else {
      usage();
    }
  }


  /**
   * Display a usage message, then exit.
   */
  public static void usage()
  {
    System.out.println("Usage:");
    System.out.println("  " + progName + " [options] <name server> "
                       + "<operation>");
    System.out.println();
    System.out.println("Options:");
    System.out.println("  -d<n> : set debug level to n (0..2)");
    System.out.println("  -r    : enable recursive mode (lsdir, find)");
    System.out.println("  -s<0|1|2> : sort method (0=none, 1=asc, 2=rev)");
    System.out.println();
    System.out.println("Operation:");
    System.out.println("  reg <obj name> <obj handle> : register object name");
    System.out.println("  upd <obj name> <obj handle> : update object name");
    System.out.println("  dereg <obj name>            : "
                       + "deregister object handle");
    System.out.println("  resv <obj name>             : resolve object name");
    System.out.println("  mkdir <dir>                 : create directory");
    System.out.println("  rmdir <dir>                 : remove directory");
    System.out.println("  lsdir <dir>                 : "
                       + "list directory contents");
    System.out.println("  find <dir> <pattern>        : find files");
    System.exit(-1);
  }


  /**
   * Print an error message, then exit.
   */
  private static void fatal(String s)
  {
    System.err.println(progName + ": " + s);
    System.exit(-1);
  }


  /**
   * Resolve an object name to an object handle.
   */
  public static void do_resolve(GlobeNameResolver resv, String objName)
  {
    rawData.rawDef oh;
    String s;

    try {
      oh = resv.resolve(objName);

      System.out.println("object handle: " + new String(RawOps.getRaw(oh)));
    }
    catch(nameResolver.nameResolverError_servFailError e) {
      System.err.println("Failed: name server failure");
    }
    catch(nameResolver.nameResolverError_refusedError e) {
      System.err.println("Failed: operation refused");
    }
    catch(nameResolver.nameResolverError_notExistError e) {
      System.err.println("Failed: object name does not exist");
    }
    catch(nameResolver.nameResolverError_unknownServError e) {
      System.err.println("Failed: unknown name server");
    }
    catch(Exception e) {
      System.out.println("Failed: " + e.getMessage());
    }
  }


  /**
   * Register an {object name, object handle} pair.
   */
  public static void do_register(GlobeNameResolver resv, String objName,
                                 byte objHandle[])
  {
    rawData.rawDef oh;

    oh = RawOps.createRaw();
    RawOps.setRaw(oh, objHandle, 0, objHandle.length);

    try {
      resv.register(objName, oh);
    }
    catch(nameResolver.nameResolverError_servFailError e) {
      System.err.println("Failed: name server failure");
    }
    catch(nameResolver.nameResolverError_refusedError e) {
      System.err.println("Failed: operation refused");
    }
    catch(nameResolver.nameResolverError_existError e) {
      System.err.println("Failed: name exists");
    }
    catch(nameResolver.nameResolverError_notDirError e) {
      System.err.println("Failed: not a directory");
    }
    catch(nameResolver.nameResolverError_isDirError e) {
      System.err.println("Failed: is a directory");
    }
    catch(nameResolver.nameResolverError_statDirError e) {
      System.err.println("Failed: cannot stat directory");
    }
    catch(nameResolver.nameResolverError_unknownServError e) {
      System.err.println("Failed: unknown name server");
    }
    catch(Exception e) {
      System.out.println("Failed: " + e.getMessage());
    }
  }


  /**
   * Update an {object name, object handle} pair.
   */
  public static void do_update(GlobeNameResolver resv, String objName,
                               byte objHandle[])
  {
    rawData.rawDef oh;

    oh = RawOps.createRaw();
    RawOps.setRaw(oh, objHandle, 0, objHandle.length);

    try {
      resv.update(objName, oh);
    }
    catch(nameResolver.nameResolverError_servFailError e) {
      System.err.println("Failed: name server failure");
    }
    catch(nameResolver.nameResolverError_refusedError e) {
      System.err.println("Failed: operation refused");
    }
    catch(nameResolver.nameResolverError_notDirError e) {
      System.err.println("Failed: not a directory");
    }
    catch(nameResolver.nameResolverError_isDirError e) {
      System.err.println("Failed: is a directory");
    }
    catch(nameResolver.nameResolverError_statDirError e) {
      System.err.println("Failed: cannot stat directory");
    }
    catch(nameResolver.nameResolverError_unknownServError e) {
      System.err.println("Failed: unknown name server");
    }
    catch(Exception e) {
      System.out.println("Failed: " + e.getMessage());
    }
  }


  /**
   * Deregister an {object name, object handle} pair.
   */
  public static void do_deregister(GlobeNameResolver resv, String objName)
  {
    try {
      resv.deregister(objName);
    }
    catch(nameResolver.nameResolverError_servFailError e) {
      System.err.println("Failed: name server failure");
    }
    catch(nameResolver.nameResolverError_notExistError e) {
      System.err.println("Failed: directory does not exist");
    }
    catch(nameResolver.nameResolverError_refusedError e) {
      System.err.println("Failed: operation refused");
    }
    catch(nameResolver.nameResolverError_notDirError e) {
      System.err.println("Failed: not a directory");
    }
    catch(nameResolver.nameResolverError_isDirError e) {
      System.err.println("Failed: is a directory");
    }
    catch(nameResolver.nameResolverError_statDirError e) {
      System.err.println("Failed: cannot stat directory");
    }
    catch(nameResolver.nameResolverError_unknownServError e) {
      System.err.println("Failed: unknown name server");
    }
    catch(Exception e) {
      System.out.println("Failed: " + e.getMessage());
    }
  }


  /**
   * Create a directory.
   */
  public static void do_mkdir(GlobeNameResolver resv, String objName)
  {
    try {
      resv.mkdir(objName);
    }
    catch(nameResolver.nameResolverError_servFailError e) {
      System.err.println("Failed: name server failure");
    }
    catch(nameResolver.nameResolverError_existError e) {
      System.err.println("Failed: name exists");
    }
    catch(nameResolver.nameResolverError_refusedError e) {
      System.err.println("Failed: operation refused");
    }
    catch(nameResolver.nameResolverError_notDirError e) {
      System.err.println("Failed: not a directory");
    }
    catch(nameResolver.nameResolverError_statDirError e) {
      System.err.println("Failed: cannot stat directory");
    }
    catch(nameResolver.nameResolverError_unknownServError e) {
      System.err.println("Failed: unknown name server");
    }
    catch(Exception e) {
      System.out.println("Failed: " + e.getMessage());
    }
  }


  /**
   * Remove a directory.
   */
  public static void do_rmdir(GlobeNameResolver resv, String objName)
  {
    try {
      resv.rmdir(objName);
    }
    catch(nameResolver.nameResolverError_servFailError e) {
      System.err.println("Failed: name server failure");
    }
    catch(nameResolver.nameResolverError_notExistError e) {
      System.err.println("Failed: directory does not exist");
    }
    catch(nameResolver.nameResolverError_refusedError e) {
      System.err.println("Failed: operation refused");
    }
    catch(nameResolver.nameResolverError_notDirError e) {
      System.err.println("Failed: not a directory");
    }
    catch(nameResolver.nameResolverError_statDirError e) {
      System.err.println("Failed: cannot stat directory");
    }
    catch(nameResolver.nameResolverError_unknownServError e) {
      System.err.println("Failed: unknown name server");
    }
    catch(Exception e) {
      System.out.println("Failed: " + e.getMessage());
    }
  }


  /**
   * List directory contents.
   */
  public static void do_lsdir(GlobeNameResolver resv, String objName,
                              boolean recursive, int sortMethod)
  {
    nameResolver.dirEntries de;
    int i;

    try {
      de = resv.lsdir(objName, (recursive) ? (short)1 : (short)0, sortMethod);

      for (i = 0; i < de.v.length; i++) {
        System.out.println(de.v[i].toString());
      }
    }
    catch(nameResolver.nameResolverError_servFailError e) {
      System.err.println("Failed: name server failure");
    }
    catch(nameResolver.nameResolverError_notExistError e) {
      System.err.println("Failed: directory does not exist");
    }
    catch(nameResolver.nameResolverError_refusedError e) {
      System.err.println("Failed: operation refused");
    }
    catch(nameResolver.nameResolverError_unknownServError e) {
      System.err.println("Failed: unknown name server");
    }
    catch(Exception e) {
      System.out.println("Failed: " + e.getMessage());
    }
  }


  /**
   * Find files.
   */
  public static void do_find(GlobeNameResolver resv, String objName,
                             String pattern, boolean recursive, int sortMethod)
  {
    nameResolver.dirEntries de;
    int i;

    try {
      de = resv.find(objName, pattern, (recursive) ? (short)1 : (short)0,
                     sortMethod);

      for (i = 0; i < de.v.length; i++) {
        System.out.println(de.v[i].toString());
      }
    }
    catch(nameResolver.nameResolverError_servFailError e) {
      System.err.println("Failed: name server failure");
    }
    catch(nameResolver.nameResolverError_notExistError e) {
      System.err.println("Failed: directory does not exist");
    }
    catch(nameResolver.nameResolverError_refusedError e) {
      System.err.println("Failed: operation refused");
    }
    catch(nameResolver.nameResolverError_unknownServError e) {
      System.err.println("Failed: unknown name server");
    }
    catch(Exception e) {
      System.out.println("Failed: " + e.getMessage());
    }
  }

}
