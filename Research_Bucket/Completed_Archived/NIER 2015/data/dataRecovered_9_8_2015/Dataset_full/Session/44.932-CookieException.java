/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// CookieException.java - cookie exception

package vu.globe.svcs.gns.lib.namesrvce;


/**
 * This exception signals a problem with a Globe cookie.
 */
public class CookieException extends Exception
{
  /**
   * Construct a CookieException without a detail message.
   */
  public CookieException()
  {
  }


  /**
   * Construct a CookieException with a detail message.
   */
  public CookieException(String msg)
  {
    super(msg);
  }
}
