/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// RedirectorCookieFactory.java

package vu.globe.svcs.gred;


import java.util.Date;

import vu.globe.util.http.HTTPDateFormat;


/**
 * This class represents a factory to create the value of a HTTP
 * redirector cookie. The Path and Domain attributes of such a cookie
 * are fixed.
 */
public class RedirectorCookieFactory
{
  /** Names of the redirector-specific cookie attributes. */
  public static final String COOKIE_GAP_ATTRIB = "NEARESTGAP";
  public static final String COOKIE_COORDS_ATTRIB = "LOCATION";

  // `Factory' for dates in the HTTP date format.
  private static HTTPDateFormat _httpDateFormatter = new HTTPDateFormat();

  private String _path;               // Path attribute
  private String _domain;             // Domain attribute
  private String _cookieTail;         // affix string


  /**
   * Instance creation.
   *
   * @param  path    the value of the Path attribute 
   * @param  domain  the value of the Domain attribute
   */
  public RedirectorCookieFactory(String path, String domain)
  {
    _path = path;
    _domain = domain;

    _cookieTail = "; Path=" + path + "; Domain=" + domain;
  }

              
  /**
   * Create the value of a Set-Cookie header. This value defines the
   * Path, Domain, <code>COOKIE_GAP_ATTRIB</code>, and the Expires attributes.
   *
   * @param  gap          the nearest GAP, e.g. "katka.acme.org:4000"
   * @param  expiresDate  the value to be used for the Expires attribute
   * @return the value
   */
  public String getGAPValue(String gap, Date expiresDate)
  {
    return COOKIE_GAP_ATTRIB + "=" + gap + "; "
           + "Expires=" + _httpDateFormatter.format(expiresDate)
           + _cookieTail;
  }


  /**
   * Create the value of a Set-Cookie header. This value defines the
   * Path, Domain, <code>COOKIE_COORDS_ATTRIB</code>, and the Expires
   * attributes.
   *
   * @param  coords       the geographical coordinates
   * @param  expiresDate  the value to be used for the Expires attribute
   * @return the value
   */
  public String getLocationValue(FloatCoordinate coords, Date expiresDate)
  {
    return COOKIE_COORDS_ATTRIB  + "=" + coords.marshallToString() + "; "
           + "Expires=" + _httpDateFormatter.format(expiresDate)
           + _cookieTail;
  }
}
