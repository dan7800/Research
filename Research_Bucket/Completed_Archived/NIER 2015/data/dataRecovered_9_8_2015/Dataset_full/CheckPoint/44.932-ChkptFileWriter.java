/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// ChkptFileWriter.java

package vu.globe.svcs.objsvr.perstm.chkptfile;


import java.io.IOException;

import vu.globe.svcs.objsvr.perstm.ObjSvrPassivationState;


/**
 * This interface provides for writing checkpoint files. A checkpoint
 * file is a binary file that holds the passivation state of an object
 * server.
 *
 * @see vu.globe.svcs.objsvr.perstm.chkptfile.ChkptFile
 */
public interface ChkptFileWriter
{
  /**
   * Close this writer and release any system resources associated
   * with it.
   *
   * @exception  IOException  if an I/O error occurs
   */
  public void close()
    throws IOException;


  /**
   * Force all system buffers associated with the checkpoint file to
   * be written to disk.
   *
   * @exception  IOException  if an I/O error occurs
   */
  public void sync()
    throws IOException;


  /**
   * Write the passivation state of the object server to the
   * checkpoint file.
   *
   * @param  gosPstate  object server's passivation state
   *
   * @exception  IOException  if an I/O error occurs
   */
  public void write(ObjSvrPassivationState gosPstate)
    throws IOException;
}
