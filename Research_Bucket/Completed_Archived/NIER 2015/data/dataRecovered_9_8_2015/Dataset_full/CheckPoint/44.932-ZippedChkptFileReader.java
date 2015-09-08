/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// ZippedChkptFileReader.java

package vu.globe.svcs.objsvr.perstm.chkptfile;


import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.*;

import vu.globe.util.comm.RawOps;
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
 * This class represents a reader for zipped checkpoint files.
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
public class ZippedChkptFileReader
  implements ChkptFileReader
{
  private File _file = null;               // input file
  private ZipInputStream _zis = null;      // input stream
  private byte _buf[] = null;              // read buffer
  private ByteArrayOutputStream _baos;     // read buffer
  private ChkptFileHeader _hdr;            // header of the checkpoint file
  private int _numEntriesRead;             // #entries read so far


  /**
   * Instance creation.
   *
   * @param  fname  name of the zipped checkpoint file to be read
   *
   * @exception  ChkptFileException  if the checkpoint file is corrupted
   * @exception  IOException         if an I/O error occurs
   */
  public ZippedChkptFileReader(String fname)
    throws ChkptFileException, IOException
  {
    this(new File(fname));
  }


  /**
   * Instance creation.
   *
   * @param  fname  zipped checkpoint file to be read
   *
   * @exception  ChkptFileException  if the checkpoint file is corrupted
   * @exception  IOException         if an I/O error occurs
   */
  public ZippedChkptFileReader(File file)
    throws ChkptFileException, IOException
  {
    _file = file;
    _zis = new ZipInputStream(new FileInputStream(_file));
    _buf = new byte[102400];
    _baos = new ByteArrayOutputStream(10240);
    _numEntriesRead = 0;

    readHeader();

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
    if (_zis == null) {
      return;
    }

    try {
      _zis.close();
    }
    finally {
      _file = null;
      _zis = null;
      _buf = null;
      _baos = null;
    }
  }


  /**
   * Read the checkpoint file and return its contents in a
   * <code>ObjSvrPassivationState</code> object.
   *
   * @return  the state
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
   * Read the header of the checkpoint file. Assumes that the next entry
   * returned by the zipped input stream contains the data to be read.
   *
   * @return  the persistence manager state
   *
   * @exception  ChkptFileException  if the checkpoint file is corrupted
   * @exception  IOException         if an I/O error occurs
   */
  private void readHeader()
    throws ChkptFileException, IOException
  {
    ZipEntry entry = null;
    DataInputStream dis;
    byte buf[];

    try {
      entry = _zis.getNextEntry();
    }
    catch(IOException e) {
      throw new IOException("cannot read header: " + e.getMessage());
    }

    if (entry == null) {
      throw new ChkptFileException("missing header entry");
    }

    if (_zis.available() == 0) {
      throw new ChkptFileException("header is malformed: no data");
    }

    DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
                        "reading zip entry " + entry.getName());

    buf = readData();

    if (buf.length != ChkptFile.HDR_SIZE) {
      throw new ChkptFileException("invalid header size: " + buf.length);
    }

    dis = new DataInputStream(new ByteArrayInputStream(buf));
    
    try {
      _hdr = new ChkptFileHeader(dis);
    }
    finally {
      dis.close();
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
   * Read the state of the persistence manager from the checkpoint file.
   * Assumes that the next entry returned by the zipped input stream
   * contains the data to be read.
   *
   * @return  the persistence manager state
   *
   * @exception  ChkptFileException  if the checkpoint file is corrupted
   * @exception  IOException         if an I/O error occurs
   */
  private byte[] readPmState()
    throws ChkptFileException, IOException
  {
    ZipEntry entry = null;

    try {
      entry = _zis.getNextEntry();
    }
    catch(IOException e) {
      throw new IOException("cannot read pm state entry: " + e.getMessage());
    }

    if (entry == null) {
      throw new ChkptFileException("missing pm state entry");
    }

    if (_zis.available() == 0) {
      throw new ChkptFileException("pm state entry is malformed: no data");
    }

    DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
                        "reading zip entry " + entry.getName());

    _numEntriesRead++;

    return readData();
  }


  /**
   * Read the states of the persistent objects from the checkpoint file.
   * The state of each persistent object is put as a
   * <code>passivationState</code> object in the specified array.
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
   * @return a <code>passivationState</code> object holding the state;
   *         <code>null</code> if the EOF is reached
   *
   * @exception  ChkptFileException  if the checkpoint file is corrupted
   * @exception  IOException         if an I/O error occurs
   */
  private passivationState readPoState()
    throws ChkptFileException, IOException
  {
    ZipEntry entry = null;
    passivationState pState;
    int n;

    if (_numEntriesRead >= _hdr.getNumEntries()) {              // done
      return null;
    }

    try {
      entry = _zis.getNextEntry();
    }
    catch(IOException e) {
      throw new IOException("cannot read po state entry: " + e.getMessage());
    }

    if (entry == null) {
      return null;
    }

    if (_zis.available() == 0) {
      throw new ChkptFileException("po state entry has no data");
    }

    DebugOutput.println(DebugOutput.DBG_DEBUGPLUS,
                        "reading zip entry " + entry.getName());

    pState = new passivationState();

    /*
     * Read the perst_id field.
     */
    n = readDataFully(_buf, 0, 8);
    
    if (n != 8) {
      throw new ChkptFileException("po state entry is malformed: undersized "
                                   + "persistence id field");
    }
    pState.perst_id = ByteArrayLib.byteToInt64(_buf, 0);

    /*
     * Read the state length field.
     */
    n = readDataFully(_buf, 0, 4);

    if (n != 4) {
      throw new ChkptFileException("po state entry is malformed: undersized "
                                   + "state length field");
    }
    int stateLength = ByteArrayLib.byteToInt32(_buf, 0);

    if (stateLength < 0) {
      throw new ChkptFileException("po state entry is malformed: range error "
                                   + "state length field");
    }
    else if (stateLength == 0) {                      // no state
      pState.state = null;
      return pState;
    }

    long sz = entry.getSize();

    if (sz != -1 && sz != stateLength) {
      throw new ChkptFileException("po state entry is malformed: state "
                            + "length field is not equal to data length "
                            + "field of zip entry");
    }

    /*
     * Read state.
     */
    try {
      byte buf[] = readDataFully(stateLength);

      rawDef state = RawOps.createRaw();
      RawOps.setRaw(state, buf, 0, buf.length);

      pState.state = state;
    }
    catch(EOFException e) {
      throw new ChkptFileException("po state entry is malformed: undersized "
                                   + "state data");
    }

    _numEntriesRead++;

    return pState;
  }


  /**
   * Read the specified number of bytes of the data of the current zip
   * entry.
   *
   * @param  buf     buffer to store the data in
   * @param  off     start offset in <code>buf</code>
   * @param  length  number of bytes to read
   * @return the actual number of bytes read; if smaller than
   *         <code>length</code>, the EOF has been reached
   *
   * @exception  IOException  if an I/O error occurs
   */
  private int readDataFully(byte buf[], int off, int length)
    throws IOException
  {
    int n = 0, startoff = off;

    while (length > 0) {
      if ( (n = _zis.read(buf, off, length)) == -1) {
        return off - startoff;
      }
      off += n;
      length -= n;
    }
    return off - startoff;
  }


  /**
   * Read the specified number of bytes of the data of the current zip
   * entry.
   *
   * @param  length  number of bytes to read
   *
   * @exception  EOFxception  if the data size is not equal to the
   *                          specified size
   * @exception  IOException  if an I/O error occurs
   */
  private byte[] readDataFully(long length)
    throws EOFException, IOException
  {
    int len = (int)length;              // MIRA: potential long->int overflow
    byte buf[] = new byte[len];
    int off = 0;
    int n;

    while (len > 0) {
      if ( (n = _zis.read(buf, off, len)) == -1) {
        throw new EOFException();
      }
      off += n;
      len -= n;
    }
    return buf;
  }


  /**
   * Read the data of the current zip entry.
   *
   * @return the data
   *
   * @exception  IOException  if an I/O error occurs
   */
  private byte[] readData()
    throws EOFException, IOException
  {
    int n;

    _baos.reset();

    /*
     * Buffer the data in a byte array output stream.
     */
    for ( ; ; ) {
      n = _zis.read(_buf, 0, _buf.length);
      if (n == -1) {
        break;
      }
      _baos.write(_buf, 0, n);
    }
    return _baos.toByteArray();
  }
}
