/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// ChkptFileReader.java

package vu.globe.svcs.objsvr.perstm.chkptfile;


import java.io.IOException;
import java.util.Date;

import vu.globe.svcs.objsvr.perstm.ObjSvrPassivationState;


/**
 * This interface provides for reading checkpoint files. A checkpoint
 * file is a binary file that holds the passivation state of an object
 * server.
 *
 * @see vu.globe.svcs.objsvr.perstm.chkptfile.ChkptFile
 */
public interface ChkptFileReader
{
  /**
   * Close this reader and release any system resources associated
   * with it.
   *
   * @exception  IOException  if an I/O error occurs
   */
  public void close()
    throws IOException;


  /**
   * Read the checkpoint file and return its contents in a
   * <code>ObjSvrPassivationState</code> object.
   *
   * @exception  ChkptFileException  if the checkpoint file is corrupted
   * @exception  IOException         if an I/O error occurs
   */
  public ObjSvrPassivationState read()
    throws ChkptFileException, IOException;


  /**
   * Return the creation date of the checkpoint file.
   */
  public Date getCreationDate();
}
