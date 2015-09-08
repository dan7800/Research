/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// PlainChkptFileReader.java

package vu.globe.svcs.objsvr.perstm.chkptfile;


import java.io.*;
import java.util.ArrayList;
import java.util.Date;

import vu.globe.util.comm.RawOps;
import vu.globe.util.debug.DebugOutput;
import vu.globe.util.comm.idl.rawData.*;

import vu.globe.svcs.objsvr.types.ResourceIdent;

import vu.globe.svcs.objsvr.idl.resource;       // resource.idl
import vu.globe.svcs.objsvr.idl.resource.*;     // resource.idl
import vu.globe.svcs.objsvr.idl.persistence.*;  // persistence.idl
 
import vu.globe.svcs.objsvr.perstm.chkptfile.ChkptFile.*;
import vu.globe.svcs.objsvr.perstm.ObjSvrPassivationState;
import vu.globe.svcs.objsvr.perstm.util.*;


/**
 * This class represents a reader for checkpoint files. A checkpoint
 * file is a binary file that holds the passivation state of an
 * object server.
 *
 * @see vu.globe.svcs.objsvr.perstm.chkptfile.ChkptFile
 */
public class PlainChkptFileReader
  implements ChkptFileReader
{
  private File _file = null;               // input file
  private DataInputStream _is = null;      // input stream
  private ChkptFileHeader _hdr;            // header of the checkpoint file
  private int _numEntriesRead;             // #entries read so far


  /**
   * Instance creation.
   *
   * @param  fname  name of the checkpoint file to be read
   *
   * @exception  ChkptFileException  if the checkpoint file is corrupted
   * @exception  IOException         if an I/O error occurs
   */
  public PlainChkptFileReader(String fname)
    throws ChkptFileException, IOException
  {
    this(new File(fname));
  }


  /**
   * Instance creation.
   *
   * @param  file  the checkpoint file to be read
   *
   * @exception  ChkptFileException  if the checkpoint file is corrupted
   * @exception  IOException         if an I/O error occurs
   */
  public PlainChkptFileReader(File file)
    throws ChkptFileException, IOException
  {
    _file = file;
    _numEntriesRead = 0;

    _is = new DataInputStream(new FileInputStream(_file));

    _hdr = new ChkptFileHeader(_is);

    DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
                        "Chkpt file: creation date: " + _hdr.getCreationDate()
                        + ", #entries: " + _hdr.getNumEntries());
  }


  /**
   * Close this reader and release any system resources associated
   * with it.
   *
   * @exception  IOException  if an I/O error occurs
   */
  public void close()
    throws IOException
  {
    if (_file == null) {
      return;
    }

    try {
      _is.close();
    }
    finally {
      _file = null;
      _is = null;
      _hdr = null;
    }
  }


  /**
   * Return the creation date of the checkpoint file.
   */
  public Date getCreationDate()
  {
    return _hdr.getCreationDate();
  }


  /**
   * Read the checkpoint file and return its contents in a
   * <code>ObjSvrPassivationState</code> object.
   *
   * @exception  ChkptFileException  if the checkpoint file is corrupted
   * @exception  IOException         if an I/O error occurs
   */
  public ObjSvrPassivationState read()
    throws ChkptFileException, IOException
  {
    ObjSvrPassivationState pState = new ObjSvrPassivationState();
    ArrayList ar = new ArrayList();

    byte buf[] = readPmState();
    pState.pmState = new ByteArray(buf);

    readPoStates(ar);
    pState.poStates = ar;
    return pState;
  }


  /**
   * Read the state of the persistence manager from the checkpoint file.
   * Assumes that the file pointer is at the start of the data to be read.
   *
   * @return  the persistence manager state
   *
   * @exception  ChkptFileException  if the checkpoint file is corrupted
   * @exception  IOException         if an I/O error occurs
   */
  private byte[] readPmState()
    throws ChkptFileException, IOException
  {
    int sz = _is.readInt();

    if (sz <= 0) {
      throw new ChkptFileException("invalid pm state size: " + sz);
    }

    DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
                        "Reading persistence manager state: size " + sz);

    byte buf[] = new byte[sz];

    try {
      _is.readFully(buf);
    }
    catch(EOFException e) {
      throw new ChkptFileException("undersized pm state data");
    }
    _numEntriesRead++;

    return buf;
  }


  /**
   * Read the remaining persistent objects' state entries and add
   * these entries as passivationState objects to the given array.
   * Assumes that the file pointer is at the start of the data to be read.
   *
   * @param  ar  array to put the states in
   * @return the number of entries added to the array
   *
   * @exception  ChkptFileException  if the checkpoint file is corrupted
   * @exception  IOException         if an I/O error occurs
   */
  private int readPoStates(ArrayList ar)
    throws ChkptFileException, IOException
  {
    passivationState psState;
    int n = 0;

    while ( (psState = readPoState()) != null) {
      ar.add(psState);
      n++;
    }
    return n;
  }


  /**
   * Read the state of the next persistent object.
   *
   * @return an <code>passivationState</code> object holding the state;
   *         <code>null</code> if the EOF is reached
   *
   * @exception  ChkptFileException  if the checkpoint file is corrupted
   * @exception  IOException         if an I/O error occurs
   */
  private passivationState readPoState()
    throws ChkptFileException, IOException
  {
    if (_numEntriesRead >= _hdr.getNumEntries()) {             // done
      return null;
    }

    passivationState pState = new passivationState();
    int sz;

    pState.perst_id = _is.readLong();
    sz = _is.readInt();

    DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
                       "Reading persistent object state: rid "
                       + pState.perst_id + ", size " + sz);

    if (sz < 0) {
      throw new ChkptFileException("malformed po state entry: "
                                   + "invalid state size: " + sz);
    }
    else if (sz == 0) {
      pState.state = null;                               // no state
    } 
    else {
      byte buf[] = new byte[sz];

      try {
        _is.readFully(buf);
      }
      catch(EOFException e) {
        throw new ChkptFileException("malformed po state entry: "
                                     + "undersized data");
      }

      rawDef data = RawOps.createRaw();
      RawOps.setRaw(data, buf, 0, sz);
      pState.state = data;
    }

    _numEntriesRead++;

    return pState;
  }
}
