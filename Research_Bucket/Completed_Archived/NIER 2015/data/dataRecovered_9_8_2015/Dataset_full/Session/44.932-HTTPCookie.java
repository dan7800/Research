/*
  Copyright (c) 1996-2003, Vrije Universiteit
  
  This file is part of a distribution. The conditions and disclaimer
  contained in the file LICENSE (which should be included in the
  distribution) are applicable to the contents of this file.
*/

// HTTPCookie.java

package vu.globe.util.http;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;


/**
 * This class represents a HTTP cookie.
 */
public class HTTPCookie
{
  private Hashtable _attribs;


  /**
   * Instance creation. Creates an empty cookie.
   */
  public HTTPCookie()
  {
    _attribs = new Hashtable();
  }


  /**
   * Instance creation.
   *
   * @param  attribs  cookie attribute string
   *
   * @exception  IllegalArgumentException  if the attribute string is malformed
   */
  public HTTPCookie(String attribs)
    throws IllegalArgumentException
  {
    _attribs = new Hashtable();
    parseCookie(attribs);
  }


  /**
   * (Re)initialize this object for object reuse.
   *
   * @param  attribs  cookie attribute string
   *
   * @exception  IllegalArgumentException  if the attribute string is malformed
   */
  public void init(String attribs)
    throws IllegalArgumentException
  {
    _attribs.clear();
    parseCookie(attribs);
  }


  /**
   * Set (update) the value of the named attribute.
   */
  public void setAttribute(String name, String value)
  {
    _attribs.put(name, value);
  }


  /**
   * Get the value of the named attribute; <code>null</code> if the cookie
   * does not contain the attribute.
   */
  public String getAttribute(String name)
  {
    return (String)_attribs.get(name);
  }


  /**
   * Remove the named attribute and return its value or <code>null</code>
   * if the cookie does not contain the attribute.
   */
  public String removeAttribute(String name)
  {
    return (String)_attribs.remove(name);
  }


  /**
   * Remove all attributes from this cookie.
   */
  public void clear()
  {
    _attribs.clear();
  }


  /**
   * Parse a cookie attribute string and store the attributes in this cookie.
   * All previously defined attributes are lost.
   *
   * @param  attribs  cookie attribute string
   *
   * @exception  IllegalArgumentException  if the attribute string is malformed
   */
  public void parseCookie(String attribs)
    throws IllegalArgumentException
  {
    StringTokenizer st = new StringTokenizer(attribs, "=;", true);
    String name, value, delim;

    _attribs.clear();

    while (st.hasMoreTokens()) {
      name = st.nextToken().trim();
      if (name.equals("=") || name.equals(";")) {
        throw new IllegalArgumentException("missing attribute name");
      }

      if (st.hasMoreTokens() == false) {
        throw new IllegalArgumentException("missing ``='' character");
      }

      delim = st.nextToken();
      if (delim.equals("=") == false) {
        throw new IllegalArgumentException("missing ``='' character");
      }

      if (st.hasMoreTokens() == false) {
        throw new IllegalArgumentException("missing attribute value");
      }

      value = st.nextToken().trim();
      if (value.equals("=") || value.equals(";")) {
        throw new IllegalArgumentException("missing attribute value");
      }

      if (st.hasMoreTokens() != false) {
        delim = st.nextToken();
        if (delim.equals(";") == false) {
          throw new IllegalArgumentException("missing ``;'' character");
        }
      }

      _attribs.put(name, value);
    }
  }


  /**
   * Return a string representation of this cookie.
   */
  public String toString()
  {
    Enumeration enum = _attribs.keys();
    String name, value;
    StringBuffer sb = new StringBuffer();

    while (enum.hasMoreElements()) {
      name = (String)enum.nextElement();

      if ( (value = (String)_attribs.get(name)) == null) {
        System.err.println("[HTTPCookie.toString] internal error: "
                           + name + " attribute has no value");
      }
      if (enum.hasMoreElements()) {
        sb.append(name + "=" + value + "; ");
      }
      else {
        sb.append(name + "=" + value);
      }
    }
    return sb.toString();
  }
}
