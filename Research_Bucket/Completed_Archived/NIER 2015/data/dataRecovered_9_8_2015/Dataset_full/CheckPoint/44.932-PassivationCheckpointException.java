/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// PassivationCheckpointException.java

package vu.globe.svcs.objsvr.perstm;


/**
 * This exception signals that something went wrong during a passivation
 * checkpoint.
 */
public class PassivationCheckpointException
  extends Exception
{
  /**
   * Construct a PassivationCheckpointException without a detail message.
   */
  public PassivationCheckpointException()
  {
  }


  /**
   * Construct a PassivationCheckpointException with a detail message.
   */
  public PassivationCheckpointException(String msg)
  {
    super(msg);
  }
}
