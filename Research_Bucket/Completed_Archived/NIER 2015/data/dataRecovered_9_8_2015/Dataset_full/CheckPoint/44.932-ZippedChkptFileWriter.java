/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// ZippedChkptFileWriter.java

package vu.globe.svcs.objsvr.perstm.chkptfile;


import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.*;

import vu.globe.util.comm.RawOps;
import vu.globe.util.comm.RawUtil;
import vu.globe.util.types.ByteArrayLib;
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
 * This class represents a writer for zipped checkpoint files.
 */

/*
 * A zipped checkpoint file is structured as follows:
 *
 *   zip entry 1: checkpoint_file_header
 *   zip entry 2: pm_state_record
 *   zip entry 3: po_state_record
 *              :
 *   zip entry n: po_state_record
 *
 * See <code>ChkptFile.java</code> for the format of checkpoint_file_header,
 * pm_state_record and po_state_record.
 *
 * @see vu.globe.svcs.objsvr.perstm.chkptfile.ChkptFile
 */
public class ZippedChkptFileWriter
  implements ChkptFileWriter
{
  /**
   * The file name under which the checkpoint file header will be stored in
   * the zip file.
   */
  public static final String HEADER_FILENAME = "header";

  /**
   * The file name under which the state of the persistence manager
   * will be stored in the zip file.
   */
  public static final String PMSTATE_FILENAME = "pmstate";

  private File _file = null;             // output file
  private ZipOutputStream _zos = null;   // output stream
  private int _numEntriesWritten;        // current number of entries written


  /**
   * Instance creation. Create or overwrite the named file.
   *
   * @param  fname  name of the output file
   *
   * @exception  IOException  if an I/O error occurs
   */
  public ZippedChkptFileWriter(String fname)
    throws IOException
  {
    this(new File(fname));
  }


  /**
   * Instance creation. Create or overwrite the output file.
   *
   * @param  file  output file
   *
   * @exception  IOException  if an I/O error occurs
   */
  public ZippedChkptFileWriter(File file)
    throws IOException
  {
    _file = file;
    _zos = new ZipOutputStream(new FileOutputStream(_file));
    _numEntriesWritten = 0;
  }


  /**
   * Close this writer and release any system resources associated
   * with it.
   *
   * @exception  IOException  if an I/O error occurs
   */
  public void close()
    throws IOException
  {
    if (_zos == null) {
      return;
    }

    try {
      sync();
    }
    catch(IOException e) {
      DebugOutput.println(DebugOutput.DBG_DEBUG, "Checkpont file: cannot sync "
                          + "checkpoint data to disk");
    }

    try {
      _zos.close();
    }
    finally {
      _file = null;
      _zos = null;
    }
  }


  /**
   * Force all system buffers associated with the checkpoint file to
   * be written to disk.
   *
   * @exception  IOException  if an I/O error occurs
   */
  public void sync()
    throws IOException
  {
    _zos.flush();
  }


  /**
   * Write the passivation state of the object server to the zip file.
   *
   * @param  gosPstate  object server's passivation state
   *
   * @exception  IOException  if an I/O error occurs
   */
  public void write(ObjSvrPassivationState gosPstate)
    throws IOException
  {
    ArrayList ar = gosPstate.poStates;
    int n = (ar == null) ? 1 : 1 + ar.size();           // 1 = pm state

    writeHeader(n);

    writePmState(gosPstate.pmState.buf, gosPstate.pmState.length);

    if (ar != null) {
      for (int i = 0; i < ar.size(); i++) {
        writePoState((passivationState)ar.get(i));
      }
    }
  }


  /**
   * Write the header to the zip file.
   *
   * @param  numEntries  number of pm state and po state entries
   *
   * @exception  IOException  if an I/O error occurs
   */
  private void writeHeader(int numEntries)
    throws IOException
  {
    String fname;
    ZipEntry zipEntry;
    ChkptFileHeader hdr;
    byte buf[];

    fname = HEADER_FILENAME;
    zipEntry = new ZipEntry(fname);

    try {
      _zos.putNextEntry(zipEntry);
    }
    catch(IOException e) {
      throw new IOException("cannot add entry to zip file: " + e.getMessage());
    }

    hdr = new ChkptFileHeader(numEntries, new Date());
    buf = hdr.toByteArray();

    DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
                        "Writing header to zip file: entry name " + fname
                        + ", size " + buf.length);

    _zos.write(buf, 0, buf.length);
  }


  /**
   * Write the state of the persistence manager to the zip file.
   *
   * @param  state   persistence manager state
   * @param  length  size of the state in bytes
   *
   * @exception  IOException  if an I/O error occurs
   */
  private void writePmState(byte state[], int length)
    throws IOException
  {
    String fname;
    ZipEntry zipEntry;

    fname = PMSTATE_FILENAME;
    zipEntry = new ZipEntry(fname);

    try {
      _zos.putNextEntry(zipEntry);
    }
    catch(IOException e) {
      throw new IOException("cannot add entry to zip file: " + e.getMessage());
    }

    DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
                        "Writing persistence manager state to zip file: "
                        + "entry name " + fname + ", size " + length);

    _zos.write(state, 0, length);
    _numEntriesWritten++;
  }


  /**
   * Write the state of one or more persistent objects to the zip file.
   *
   * @param  ar  array of <code>passivationStateObjects</code> holding
   *             the states to be written
   *
   * @exception  IOException  if an I/O error occurs
   */
  private void writePoStates(ArrayList ar)
    throws IOException
  {
    for (int i = 0; i < ar.size(); i++) {
      writePoState((passivationState)ar.get(i));
    }
  }


  /**
   * Write the state of a persistent object to the zip file.
   *
   * @param  pState  state to be written
   *
   * @exception  IOException  if an I/O error occurs
   */
  private void writePoState(passivationState pState)
    throws IOException
  {
    int sz;
    String fname;
    ZipEntry zipEntry;
    byte buf[];

    /*
     * Create unique name for the zip entry.
     */
    fname = pState.perst_id + "." + _numEntriesWritten;

    zipEntry = new ZipEntry(fname);

    try {
      _zos.putNextEntry(zipEntry);
    }
    catch(IOException e) {
      throw new IOException("cannot add entry to zip file: " + e.getMessage());
    }

    /*
     * Write the persistence id.
     */
    buf = ByteArrayLib.int64ToByte(pState.perst_id);
    _zos.write(buf, 0, 8);

    sz = (pState.state != null) ? RawOps.sizeOfRaw(pState.state) : 0;
 
    DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
                        "Writing persistent object state to zip file: "
                        + "entry name " + fname + ", rid " + pState.perst_id
                        + ", size " + sz);

    /*
     * Write the length of the state.
     */
    buf = ByteArrayLib.int32ToByte(sz);
    _zos.write(buf, 0, 4);

    /*
     * Write the state (if present).
     */
    if (sz > 0) {
      // buf = RawOps.getRaw(pState.state);
      // _zos.write(buf, 0, buf.length);
      RawUtil.outputRaw(pState.state, _zos, 0, sz);
    }

    _numEntriesWritten++;
  }
}
