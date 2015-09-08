/*
  $Id: TestUtil.java 184 2009-05-06 02:56:33Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 184 $
  Updated: $Date: 2009-05-05 19:56:33 -0700 (Tue, 05 May 2009) $
*/
package edu.vt.middleware.ldap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import edu.vt.middleware.ldap.bean.LdapAttribute;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.bean.LdapResult;
import org.testng.annotations.DataProvider;

/**
 * Common methods for ldap tests.
 *
 * @author  Middleware Services
 * @version  $Revision: 184 $
 */
public final class TestUtil
{

  /** Location of the hostname in the output of netstat. */
  public static final int NETSTAT_HOST_INDEX = 4;


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "setup-ldap")
  public static Ldap createSetupLdap()
    throws Exception
  {
    final Ldap l = new Ldap();
    l.loadFromProperties("/ldap.setup.properties");
    return l;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "ldap")
  public static Ldap createLdap()
    throws Exception
  {
    final Ldap l = new Ldap();
    l.loadFromProperties("/ldap.properties");
    return l;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "sasl-external-ldap")
  public static Ldap createSaslExternalLdap()
    throws Exception
  {
    final Ldap l = new Ldap();
    l.loadFromProperties("/ldap.sasl.properties");

    final LdapTLSSocketFactory sf = new LdapTLSSocketFactory();
    sf.setTrustStoreName("/ed.truststore");
    sf.setTrustStoreType("BKS");
    sf.setKeyStoreName("/ed.keystore");
    sf.setKeyStoreType("BKS");
    sf.initialize();
    l.getLdapConfig().setSslSocketFactory(sf);
    return l;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "gss-api-ldap")
  public static Ldap createGssApiLdap()
    throws Exception
  {
    final Ldap l = new Ldap();
    l.loadFromProperties("/ldap.gssapi.properties");
    return l;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "ssl-auth")
  public static Authenticator createSSLAuthenticator()
    throws Exception
  {
    final Authenticator a = new Authenticator();
    a.loadFromProperties("/ldap.ssl.properties");
    return a;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "tls-auth")
  public static Authenticator createTLSAuthenticator()
    throws Exception
  {
    final Authenticator a = new Authenticator();
    a.loadFromProperties("/ldap.tls.properties");

    final LdapTLSSocketFactory sf = new LdapTLSSocketFactory();
    sf.setTrustStoreName("/ed.truststore");
    sf.setTrustStoreType("BKS");
    sf.initialize();
    a.getAuthenticatorConfig().setSslSocketFactory(sf);
    return a;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "digest-md5-auth")
  public static Authenticator createDigestMD5Authenticator()
    throws Exception
  {
    final Authenticator a = new Authenticator();
    a.loadFromProperties("/ldap.digest-md5.properties");
    return a;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "cram-md5-auth")
  public static Authenticator createCramMD5Authenticator()
    throws Exception
  {
    final Authenticator a = new Authenticator();
    a.loadFromProperties("/ldap.cram-md5.properties");
    return a;
  }


  /**
   * Reads a file on the classpath into a reader.
   *
   * @param  filename  to open.
   *
   * @return  reader.
   *
   * @throws  Exception  If file cannot be read.
   */
  public static BufferedReader readFile(final String filename)
    throws Exception
  {
    return
      new BufferedReader(
        new InputStreamReader(TestUtil.class.getResourceAsStream(filename)));
  }


  /**
   * Reads a file on the classpath into a string.
   *
   * @param  filename  to open.
   *
   * @return  string.
   *
   * @throws  Exception  If file cannot be read.
   */
  public static String readFileIntoString(final String filename)
    throws Exception
  {
    final StringBuffer result = new StringBuffer();
    final BufferedReader br = readFile(filename);
    try {
      String line;
      while ((line = br.readLine()) != null) {
        result.append(line).append(System.getProperty("line.separator"));
      }
    } finally {
      br.close();
    }
    return result.toString();
  }


  /**
   * Converts a ldif to a <code>LdapResult</code>.
   *
   * @param  ldif  to convert.
   *
   * @return  LdapResult.
   */
  public static LdapResult convertLdifToResult(final String ldif)
  {
    final LdapResult result = new LdapResult();
    final String[] entries = ldif.split(
      System.getProperty("line.separator") +
      System.getProperty("line.separator"));
    for (int i = 0; i < entries.length; i++) {
      result.addEntry(convertLdifToEntry(entries[i]));
    }
    return result;
  }


  /**
   * Converts a ldif to a <code>LdapEntry</code>.
   *
   * @param  ldif  to convert.
   *
   * @return  LdapEntry.
   */
  public static LdapEntry convertLdifToEntry(final String ldif)
  {
    final LdapEntry entry = new LdapEntry();
    final String[] lines = ldif.split(System.getProperty("line.separator"));
    for (int i = 0; i < lines.length; i++) {
      boolean isBinary = false;
      if (lines[i].indexOf("::") != -1) {
        isBinary = true;
      }

      final String[] parts = lines[i].trim().split(":* ", 2);
      if (parts[0] != null && !parts[0].equals("")) {
        if (parts[0].equalsIgnoreCase("dn")) {
          entry.setDn(parts[1]);
        } else {
          LdapAttribute la = entry.getLdapAttributes().getAttribute(parts[0]);
          if (la == null) {
            la = new LdapAttribute();
            la.setName(parts[0]);
            entry.getLdapAttributes().addAttribute(la);
          }
          if (isBinary) {
            la.getValues().add(LdapUtil.base64Decode(parts[1]));
          } else {
            la.getValues().add(parts[1]);
          }
        }
      }
    }
    return entry;
  }


  /**
   * Converts a string of the form: givenName=John|sn=Doe into a ldap attributes
   * object.
   *
   * @param  attrs  to convert.
   *
   * @return  LdapAttributes.
   */
  public static LdapAttributes convertStringToAttributes(final String attrs)
  {
    final LdapAttributes la = new LdapAttributes();
    final String[] s = attrs.split("\\|");
    for (int i = 0; i < s.length; i++) {
      final String[] nameValuePairs = s[i].trim().split("=", 2);
      if (la.getAttribute(nameValuePairs[0]) != null) {
        la.getAttribute(nameValuePairs[0]).getValues().add(nameValuePairs[1]);
      } else {
        la.addAttribute(nameValuePairs[0], nameValuePairs[1]);
      }
    }
    return la;
  }


  /**
   * Returns the number of open connections to the supplied host. Uses 'netstat
   * -al' to uncover open sockets.
   *
   * @param  host  host to look for.
   *
   * @return  number of open connections.
   *
   * @throws  IOException  if the process cannot be run
   */
  public static int countOpenConnections(final String host)
    throws IOException
  {
    final String[] cmd = new String[] {"netstat", "-al"};
    final Process p = new ProcessBuilder(cmd).start();
    final BufferedReader br = new BufferedReader(
      new InputStreamReader(p.getInputStream()));
    String line;
    final List<String> openConns = new ArrayList<String>();
    while ((line = br.readLine()) != null) {
      if (line.matches(".*ESTABLISHED$")) {
        final String s = line.split("\\s+")[NETSTAT_HOST_INDEX];
        openConns.add(s.substring(0, s.lastIndexOf(".")));
      }
    }

    int count = 0;
    for (String o : openConns) {
      if (o.contains(host)) {
        count++;
      }
    }
    return count;
  }
}
/*
  $Id: TestUtil.java 493 2009-08-28 02:21:35Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 493 $
  Updated: $Date: 2009-08-27 19:21:35 -0700 (Thu, 27 Aug 2009) $
*/
package edu.vt.middleware.ldap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import edu.vt.middleware.ldap.bean.LdapAttribute;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.bean.LdapResult;
import org.testng.annotations.DataProvider;

/**
 * Common methods for ldap tests.
 *
 * @author  Middleware Services
 * @version  $Revision: 493 $
 */
public final class TestUtil
{

  /** Location of the hostname in the output of netstat. */
  public static final int NETSTAT_HOST_INDEX = 4;


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "setup-ldap")
  public static Ldap createSetupLdap()
    throws Exception
  {
    final Ldap l = new Ldap();
    l.loadFromProperties(
      TestUtil.class.getResourceAsStream("/ldap.setup.properties"));
    return l;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "ldap")
  public static Ldap createLdap()
    throws Exception
  {
    final Ldap l = new Ldap();
    l.loadFromProperties();
    return l;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "sasl-external-ldap")
  public static Ldap createSaslExternalLdap()
    throws Exception
  {
    final Ldap l = new Ldap();
    l.loadFromProperties(
      TestUtil.class.getResourceAsStream("/ldap.sasl.properties"));

    final LdapTLSSocketFactory sf = new LdapTLSSocketFactory();
    sf.setTrustStoreName("/ed.truststore");
    sf.setTrustStoreType("BKS");
    sf.setKeyStoreName("/ed.keystore");
    sf.setKeyStoreType("BKS");
    sf.initialize();
    l.getLdapConfig().setSslSocketFactory(sf);
    return l;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "gss-api-ldap")
  public static Ldap createGssApiLdap()
    throws Exception
  {
    final Ldap l = new Ldap();
    l.loadFromProperties(
      TestUtil.class.getResourceAsStream("/ldap.gssapi.properties"));
    return l;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "ssl-auth")
  public static Authenticator createSSLAuthenticator()
    throws Exception
  {
    final Authenticator a = new Authenticator();
    a.loadFromProperties(
      TestUtil.class.getResourceAsStream("/ldap.ssl.properties"));
    return a;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "tls-auth")
  public static Authenticator createTLSAuthenticator()
    throws Exception
  {
    final Authenticator a = new Authenticator();
    a.loadFromProperties(
      TestUtil.class.getResourceAsStream("/ldap.tls.properties"));

    final LdapTLSSocketFactory sf = new LdapTLSSocketFactory();
    sf.setTrustStoreName("/ed.truststore");
    sf.setTrustStoreType("BKS");
    sf.initialize();
    a.getAuthenticatorConfig().setSslSocketFactory(sf);
    return a;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "digest-md5-auth")
  public static Authenticator createDigestMD5Authenticator()
    throws Exception
  {
    final Authenticator a = new Authenticator();
    a.loadFromProperties(
      TestUtil.class.getResourceAsStream("/ldap.digest-md5.properties"));
    return a;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "cram-md5-auth")
  public static Authenticator createCramMD5Authenticator()
    throws Exception
  {
    final Authenticator a = new Authenticator();
    a.loadFromProperties(
      TestUtil.class.getResourceAsStream("/ldap.cram-md5.properties"));
    return a;
  }


  /**
   * Reads a file on the classpath into a reader.
   *
   * @param  filename  to open.
   *
   * @return  reader.
   *
   * @throws  Exception  If file cannot be read.
   */
  public static BufferedReader readFile(final String filename)
    throws Exception
  {
    return
      new BufferedReader(
        new InputStreamReader(TestUtil.class.getResourceAsStream(filename)));
  }


  /**
   * Reads a file on the classpath into a string.
   *
   * @param  filename  to open.
   *
   * @return  string.
   *
   * @throws  Exception  If file cannot be read.
   */
  public static String readFileIntoString(final String filename)
    throws Exception
  {
    final StringBuffer result = new StringBuffer();
    final BufferedReader br = readFile(filename);
    try {
      String line;
      while ((line = br.readLine()) != null) {
        result.append(line).append(System.getProperty("line.separator"));
      }
    } finally {
      br.close();
    }
    return result.toString();
  }


  /**
   * Converts a ldif to a <code>LdapResult</code>.
   *
   * @param  ldif  to convert.
   *
   * @return  LdapResult.
   */
  public static LdapResult convertLdifToResult(final String ldif)
  {
    final LdapResult result = new LdapResult();
    final String[] entries = ldif.split(
      System.getProperty("line.separator") +
      System.getProperty("line.separator"));
    for (int i = 0; i < entries.length; i++) {
      result.addEntry(convertLdifToEntry(entries[i]));
    }
    return result;
  }


  /**
   * Converts a ldif to a <code>LdapEntry</code>.
   *
   * @param  ldif  to convert.
   *
   * @return  LdapEntry.
   */
  public static LdapEntry convertLdifToEntry(final String ldif)
  {
    final LdapEntry entry = new LdapEntry();
    final String[] lines = ldif.split(System.getProperty("line.separator"));
    for (int i = 0; i < lines.length; i++) {
      boolean isBinary = false;
      if (lines[i].indexOf("::") != -1) {
        isBinary = true;
      }

      final String[] parts = lines[i].trim().split(":* ", 2);
      if (parts[0] != null && !parts[0].equals("")) {
        if (parts[0].equalsIgnoreCase("dn")) {
          entry.setDn(parts[1]);
        } else {
          LdapAttribute la = entry.getLdapAttributes().getAttribute(parts[0]);
          if (la == null) {
            la = new LdapAttribute();
            la.setName(parts[0]);
            entry.getLdapAttributes().addAttribute(la);
          }
          if (isBinary) {
            la.getValues().add(LdapUtil.base64Decode(parts[1]));
          } else {
            la.getValues().add(parts[1]);
          }
        }
      }
    }
    return entry;
  }


  /**
   * Converts a string of the form: givenName=John|sn=Doe into a ldap attributes
   * object.
   *
   * @param  attrs  to convert.
   *
   * @return  LdapAttributes.
   */
  public static LdapAttributes convertStringToAttributes(final String attrs)
  {
    final LdapAttributes la = new LdapAttributes();
    final String[] s = attrs.split("\\|");
    for (int i = 0; i < s.length; i++) {
      final String[] nameValuePairs = s[i].trim().split("=", 2);
      if (la.getAttribute(nameValuePairs[0]) != null) {
        la.getAttribute(nameValuePairs[0]).getValues().add(nameValuePairs[1]);
      } else {
        la.addAttribute(nameValuePairs[0], nameValuePairs[1]);
      }
    }
    return la;
  }


  /**
   * Returns the number of open connections to the supplied host. Uses 'netstat
   * -al' to uncover open sockets.
   *
   * @param  host  host to look for.
   *
   * @return  number of open connections.
   *
   * @throws  IOException  if the process cannot be run
   */
  public static int countOpenConnections(final String host)
    throws IOException
  {
    final String[] cmd = new String[] {"netstat", "-al"};
    final Process p = new ProcessBuilder(cmd).start();
    final BufferedReader br = new BufferedReader(
      new InputStreamReader(p.getInputStream()));
    String line;
    final List<String> openConns = new ArrayList<String>();
    while ((line = br.readLine()) != null) {
      if (line.matches(".*ESTABLISHED$")) {
        final String s = line.split("\\s+")[NETSTAT_HOST_INDEX];
        openConns.add(s.substring(0, s.lastIndexOf(".")));
      }
    }

    int count = 0;
    for (String o : openConns) {
      if (o.contains(host)) {
        count++;
      }
    }
    return count;
  }
}
/*
  $Id: TestUtil.java 494 2009-08-28 02:31:50Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 494 $
  Updated: $Date: 2009-08-27 19:31:50 -0700 (Thu, 27 Aug 2009) $
*/
package edu.vt.middleware.ldap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import edu.vt.middleware.ldap.bean.LdapAttribute;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.bean.LdapResult;
import org.testng.annotations.DataProvider;

/**
 * Common methods for ldap tests.
 *
 * @author  Middleware Services
 * @version  $Revision: 494 $
 */
public final class TestUtil
{

  /** Location of the hostname in the output of netstat. */
  public static final int NETSTAT_HOST_INDEX = 4;


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "setup-ldap")
  public static Ldap createSetupLdap()
    throws Exception
  {
    final Ldap l = new Ldap();
    l.loadFromProperties(
      TestUtil.class.getResourceAsStream("/ldap.setup.properties"));
    return l;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "ldap")
  public static Ldap createLdap()
    throws Exception
  {
    final Ldap l = new Ldap();
    l.loadFromProperties();
    return l;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "sasl-external-ldap")
  public static Ldap createSaslExternalLdap()
    throws Exception
  {
    final Ldap l = new Ldap();
    l.loadFromProperties(
      TestUtil.class.getResourceAsStream("/ldap.sasl.properties"));

    final LdapTLSSocketFactory sf = new LdapTLSSocketFactory();
    sf.setTrustStoreName("/ed.truststore");
    sf.setTrustStoreType("BKS");
    sf.setKeyStoreName("/ed.keystore");
    sf.setKeyStoreType("BKS");
    sf.initialize();
    l.getLdapConfig().setSslSocketFactory(sf);
    return l;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "gss-api-ldap")
  public static Ldap createGssApiLdap()
    throws Exception
  {
    final Ldap l = new Ldap();
    l.loadFromProperties(
      TestUtil.class.getResourceAsStream("/ldap.gssapi.properties"));
    return l;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "ssl-auth")
  public static Authenticator createSSLAuthenticator()
    throws Exception
  {
    final Authenticator a = new Authenticator();
    a.loadFromProperties(
      TestUtil.class.getResourceAsStream("/ldap.ssl.properties"));
    return a;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "tls-auth")
  public static Authenticator createTLSAuthenticator()
    throws Exception
  {
    final Authenticator a = new Authenticator();
    a.loadFromProperties(
      TestUtil.class.getResourceAsStream("/ldap.tls.properties"));

    final LdapTLSSocketFactory sf = new LdapTLSSocketFactory();
    sf.setTrustStoreName("/ed.truststore");
    sf.setTrustStoreType("BKS");
    sf.initialize();
    a.getAuthenticatorConfig().setSslSocketFactory(sf);
    return a;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "digest-md5-auth")
  public static Authenticator createDigestMD5Authenticator()
    throws Exception
  {
    final Authenticator a = new Authenticator();
    a.loadFromProperties(
      TestUtil.class.getResourceAsStream("/ldap.digest-md5.properties"));
    return a;
  }


  /**
   * @return  Test configuration.
   *
   * @throws  Exception  On test data generation failure.
   */
  @DataProvider(name = "cram-md5-auth")
  public static Authenticator createCramMD5Authenticator()
    throws Exception
  {
    final Authenticator a = new Authenticator();
    a.loadFromProperties(
      TestUtil.class.getResourceAsStream("/ldap.cram-md5.properties"));
    return a;
  }


  /**
   * Reads a file on the classpath into a reader.
   *
   * @param  filename  to open.
   *
   * @return  reader.
   *
   * @throws  Exception  If file cannot be read.
   */
  public static BufferedReader readFile(final String filename)
    throws Exception
  {
    return
      new BufferedReader(
        new InputStreamReader(TestUtil.class.getResourceAsStream(filename)));
  }


  /**
   * Reads a file on the classpath into a string.
   *
   * @param  filename  to open.
   *
   * @return  string.
   *
   * @throws  Exception  If file cannot be read.
   */
  public static String readFileIntoString(final String filename)
    throws Exception
  {
    final StringBuffer result = new StringBuffer();
    final BufferedReader br = readFile(filename);
    try {
      String line;
      while ((line = br.readLine()) != null) {
        result.append(line).append(System.getProperty("line.separator"));
      }
    } finally {
      br.close();
    }
    return result.toString();
  }


  /**
   * Converts a ldif to a <code>LdapResult</code>.
   *
   * @param  ldif  to convert.
   *
   * @return  LdapResult.
   */
  public static LdapResult convertLdifToResult(final String ldif)
  {
    final LdapResult result = new LdapResult();
    final String[] entries = ldif.split(
      System.getProperty("line.separator") +
      System.getProperty("line.separator"));
    for (int i = 0; i < entries.length; i++) {
      result.addEntry(convertLdifToEntry(entries[i]));
    }
    return result;
  }


  /**
   * Converts a ldif to a <code>LdapEntry</code>.
   *
   * @param  ldif  to convert.
   *
   * @return  LdapEntry.
   */
  public static LdapEntry convertLdifToEntry(final String ldif)
  {
    final LdapEntry entry = new LdapEntry();
    final String[] lines = ldif.split(System.getProperty("line.separator"));
    for (int i = 0; i < lines.length; i++) {
      boolean isBinary = false;
      if (lines[i].indexOf("::") != -1) {
        isBinary = true;
      }

      final String[] parts = lines[i].trim().split(":* ", 2);
      if (parts[0] != null && !parts[0].equals("")) {
        if (parts[0].equalsIgnoreCase("dn")) {
          entry.setDn(parts[1]);
        } else {
          LdapAttribute la = entry.getLdapAttributes().getAttribute(parts[0]);
          if (la == null) {
            la = new LdapAttribute();
            la.setName(parts[0]);
            entry.getLdapAttributes().addAttribute(la);
          }
          if (isBinary) {
            la.getValues().add(LdapUtil.base64Decode(parts[1]));
          } else {
            la.getValues().add(parts[1]);
          }
        }
      }
    }
    return entry;
  }


  /**
   * Converts a string of the form: givenName=John|sn=Doe into a ldap attributes
   * object.
   *
   * @param  attrs  to convert.
   *
   * @return  LdapAttributes.
   */
  public static LdapAttributes convertStringToAttributes(final String attrs)
  {
    final LdapAttributes la = new LdapAttributes();
    final String[] s = attrs.split("\\|");
    for (int i = 0; i < s.length; i++) {
      final String[] nameValuePairs = s[i].trim().split("=", 2);
      if (la.getAttribute(nameValuePairs[0]) != null) {
        la.getAttribute(nameValuePairs[0]).getValues().add(nameValuePairs[1]);
      } else {
        la.addAttribute(nameValuePairs[0], nameValuePairs[1]);
      }
    }
    return la;
  }


  /**
   * Returns the number of open connections to the supplied host. Uses 'netstat
   * -al' to uncover open sockets.
   *
   * @param  host  host to look for.
   *
   * @return  number of open connections.
   *
   * @throws  IOException  if the process cannot be run
   */
  public static int countOpenConnections(final String host)
    throws IOException
  {
    final String[] cmd = new String[] {"netstat", "-al"};
    final Process p = new ProcessBuilder(cmd).start();
    final BufferedReader br = new BufferedReader(
      new InputStreamReader(p.getInputStream()));
    String line;
    final List<String> openConns = new ArrayList<String>();
    while ((line = br.readLine()) != null) {
      if (line.matches(".*ESTABLISHED$")) {
        final String s = line.split("\\s+")[NETSTAT_HOST_INDEX];
        openConns.add(s.substring(0, s.lastIndexOf(".")));
      }
    }

    int count = 0;
    for (String o : openConns) {
      if (o.contains(host)) {
        count++;
      }
    }
    return count;
  }
}
