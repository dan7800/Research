/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// CookieNotFoundException.java - cookie not found exception

package vu.globe.svcs.gns.lib.namesrvce;


/**
 * This exception signals that a cookie cannot be found.
 */
public class CookieNotFoundException
  extends Exception
{
  /**
   * Construct a CookieNotFoundException without a detail message.
   */
  public CookieNotFoundException()
  {
  }


  /**
   * Construct a CookieNotFoundException with a detail message.
   */
  public CookieNotFoundException(String msg)
  {
    super(msg);
  }
}
