/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// PlainChkptFileWriter.java

package vu.globe.svcs.objsvr.perstm.chkptfile;


import java.io.*;
import java.util.ArrayList;
import java.util.Date;

import vu.globe.util.comm.RawOps;
import vu.globe.util.comm.RawUtil;
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
 * This class represents a writer for checkpoint files. A checkpoint file is
 * a binary file that holds the passivation checkpoint of an object server.
 *
 * @see vu.globe.svcs.objsvr.perstm.chkptfile.ChkptFile
 */
public class PlainChkptFileWriter
  implements ChkptFileWriter
{
  private RandomAccessFile _file = null;       // output file
  private int _numEntriesWritten;              // #entries written so far


  /**
   * Instance creation. Create or overwrite the output file.
   *
   * @param  file  output checkpoint file
   *
   * @exception  IOException  if an I/O error occurs
   */
  public PlainChkptFileWriter(File file)
    throws IOException
  {
    this(file.getPath());
  }


  /**
   * Instance creation. Create or overwrite the output file.
   *
   * @param  fname  name of the output checkpoint file
   *
   * @exception  IOException  if an I/O error occurs
   */
  public PlainChkptFileWriter(String fname)
    throws IOException
  {
    _file = new RandomAccessFile(fname, "rw");
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
    if (_file == null) {
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
      _file.close();
    }
    finally {
      _file = null;
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
    FileDescriptor fd = _file.getFD();
    fd.sync();
  }


  /**
   * Write the passivation state of the object server to the checkpoint file.
   *
   * @param  gosPstate  object server's passivation state
   *
   * @exception  IOException  if an I/O error occurs
   */
  public void write(ObjSvrPassivationState gosPstate)
    throws IOException
  {
    ArrayList ar = gosPstate.poStates;
    int n = (ar == null) ? 1 : 1 + ar.size();         // 1 = pm state

    writeHeader(n);

    writePmState(gosPstate.pmState.buf, gosPstate.pmState.length);

    if (ar != null) {
      for (int i = 0; i < ar.size(); i++) {
        writePoState((passivationState)ar.get(i));
      }
    }
  }


  /**
   * Write the header to the checkpoint file.
   *
   * @param  numEntries  number of pm state and po state entries
   *
   * @exception  IOException  if an I/O error occurs
   */
  private void writeHeader(int numEntries)
    throws IOException
  {
    ChkptFileHeader hdr = new ChkptFileHeader(numEntries, new Date());
    byte buf[] = hdr.toByteArray();

    _file.seek(0);
    _file.write(buf);
  }


  /**
   * Write the state of the persistence manager to the checkpoint file.
   * Assumes that the file pointer is at the right offset.
   *
   * @param  state   persistence manager state
   * @param  length  the size of the state in bytes
   *
   * @exception  IOException  if an I/O error occurs
   */
  private void writePmState(byte state[], int length)
    throws IOException
  {
    DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
                        "Writing perstistence manager state: size " + length);

    _file.writeInt(length);
    _file.write(state, 0, length);
    _numEntriesWritten++;
  }


  /**
   * Write the state of a persistent object to the checkpoint file.
   * Assumes that the file position is at the right offset.
   *
   * @param  pState  persistent object's passivation state
   *
   * @exception  IOException  if an I/O error occurs
   */
  private void writePoState(passivationState pState)
    throws IOException
  {
    int sz;

    _file.writeLong(pState.perst_id);

    sz = (pState.state != null) ? RawOps.sizeOfRaw(pState.state) : 0;

    DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
                        "Writing persistent object state: rid "
                         + pState.perst_id + ", size " + sz);

    _file.writeInt(sz);

    if (sz > 0) {
      RawUtil.outputRaw(pState.state, _file, 0, sz);
    }

    _numEntriesWritten++;
  }
}
