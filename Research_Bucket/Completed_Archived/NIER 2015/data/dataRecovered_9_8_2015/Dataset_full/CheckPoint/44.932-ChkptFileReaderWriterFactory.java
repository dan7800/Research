/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// ChkptFileReaderWriterFactory.java

package vu.globe.svcs.objsvr.perstm.chkptfile;


import java.io.*;

import vu.globe.svcs.objsvr.perstm.chkptfile.ChkptFile.*;


/**
 * This interface provides for creating checkpoint file readers and writers.
 *
 * @see vu.globe.svcs.objsvr.perstm.chkptfile.ChkptFile
 */
public interface ChkptFileReaderWriterFactory
{
  /**
   * Create a checkpoint file reader.
   *
   * @param  fname  name of the checkpoint file to be read
   * @return the reader
   *
   * @exception  ChkptFileException  if the checkpoint file is corrupted
   * @exception  IOException         if an I/O error occurs
   */
  public ChkptFileReader createReader(String fname)
    throws ChkptFileException, IOException;


  /**
   * Create a checkpoint file reader.
   *
   * @param  file  the checkpoint file to be read
   * @return the reader
   *
   * @exception  ChkptFileException  if the checkpoint file is corrupted
   * @exception  IOException         if an I/O error occurs
   */
  public ChkptFileReader createReader(File file)
    throws ChkptFileException, IOException;


  /**
   * Create a checkpoint file writer.
   *
   * @param  fname  name of the output checkpoint file (created if necessary)
   *
   * @exception  IOException  if an I/O error occurs
   */
  public ChkptFileWriter createWriter(String fname)
    throws IOException;


  /**
   * Create a checkpoint file writer.
   *
   * @param  file  output file (created if necessary)
   *
   * @exception  IOException  if an I/O error occurs
   */
  public ChkptFileWriter createWriter(File file)
    throws IOException;
}
