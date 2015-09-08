/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// ChkptFileException.java

package vu.globe.svcs.objsvr.perstm.chkptfile;


/**
 * This exception signals that something is wrong with a checkpoint file.
 */
public class ChkptFileException
  extends Exception
{
  /**
   * Construct a ChkptFileException without a detail message.
   */
  public ChkptFileException()
  {
  }


  /**
   * Construct a ChkptFileException with a detail message.
   */
  public ChkptFileException(String msg)
  {
    super(msg);
  }
}
