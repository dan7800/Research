/*
  $Id: AuthenticatorTest.java 639 2009-09-18 17:55:42Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 639 $
  Updated: $Date: 2009-09-18 10:55:42 -0700 (Fri, 18 Sep 2009) $
*/
package edu.vt.middleware.ldap;

import javax.naming.AuthenticationException;
import javax.naming.directory.Attributes;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.handler.AuthenticationResultHandler;
import edu.vt.middleware.ldap.handler.TestAuthenticationResultHandler;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link Authenticator}.
 *
 * @author  Middleware Services
 * @version  $Revision: 639 $
 */
public class AuthenticatorTest
{

  /** Invalid password test data. */
  public static final String INVALID_PASSWD = "not-a-password";

  /** Invalid filter test data. */
  public static final String INVALID_FILTER = "departmentNumber=1111";

  /** Entry created for auth tests. */
  private static LdapEntry testLdapEntry;

  /** Ldap instance for concurrency testing. */
  private Authenticator singleTLSAuth;

  /** Ldap instance for concurrency testing. */
  private Authenticator singleSSLAuth;


  /**
   * Default constructor.
   *
   * @throws  Exception  if ldap cannot be constructed
   */
  public AuthenticatorTest()
    throws Exception
  {
    this.singleTLSAuth = TestUtil.createTLSAuthenticator();
    this.singleSSLAuth = TestUtil.createSSLAuthenticator();
  }


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "createEntry3" })
  @BeforeClass(groups = {"authtest"})
  public void createAuthEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtil.readFileIntoString(ldifFile);
    testLdapEntry = TestUtil.convertLdifToEntry(ldif);

    Ldap ldap = TestUtil.createSetupLdap();
    ldap.create(
      testLdapEntry.getDn(),
      testLdapEntry.getLdapAttributes().toAttributes());
    ldap.close();
    ldap = TestUtil.createLdap();
    while (
      !ldap.compare(
          testLdapEntry.getDn(),
          new SearchFilter(testLdapEntry.getDn().split(",")[0]))) {
      Thread.sleep(100);
    }
    ldap.close();
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"authtest"})
  public void deleteAuthEntry()
    throws Exception
  {
    final Ldap ldap = TestUtil.createSetupLdap();
    ldap.delete(testLdapEntry.getDn());
    ldap.close();
  }


  /**
   * @param  createNew  whether to construct a new ldap instance.
   *
   * @return  <code>Authenticator</code>
   *
   * @throws  Exception  On ldap construction failure.
   */
  public Authenticator createTLSAuthenticator(final boolean createNew)
    throws Exception
  {
    if (createNew) {
      return TestUtil.createTLSAuthenticator();
    }
    return singleTLSAuth;
  }


  /**
   * @param  createNew  whether to construct a new ldap instance.
   *
   * @return  <code>Authenticator</code>
   *
   * @throws  Exception  On ldap construction failure.
   */
  public Authenticator createSSLAuthenticator(final boolean createNew)
    throws Exception
  {
    if (createNew) {
      return TestUtil.createSSLAuthenticator();
    }
    return singleSSLAuth;
  }


  /**
   * @param  ldapUrl  to check
   * @param  base  to check
   */
  @Parameters({ "loadPropertiesUrl", "loadPropertiesBase" })
  @Test(groups = {"authtest"})
  public void loadProperties(final String ldapUrl, final String base)
  {
    final Authenticator a = new Authenticator();
    a.loadFromProperties(
      TestUtil.class.getResourceAsStream("/ldap.tls.properties"));
    AssertJUnit.assertEquals(ldapUrl, a.getAuthenticatorConfig().getLdapUrl());
    AssertJUnit.assertEquals(base, a.getAuthenticatorConfig().getBase());
  }


  /**
   * @param  uid  to get dn for.
   * @param  user  to get dn for.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "getDnUid", "getDnUser" })
  @Test(groups = {"authtest"})
  public void getDn(final String uid, final String user)
    throws Exception
  {
    final Authenticator ldap = this.createTLSAuthenticator(true);

    // test input
    AssertJUnit.assertNull(ldap.getDn(null));
    AssertJUnit.assertNull(ldap.getDn(""));

    // test empty user field
    final String[] userField = ldap.getAuthenticatorConfig().getUserField();
    ldap.getAuthenticatorConfig().setUserField(new String[] {});
    AssertJUnit.assertNull(ldap.getDn(user));
    ldap.getAuthenticatorConfig().setUserField(userField);

    // test construct dn
    ldap.getAuthenticatorConfig().setConstructDn(true);
    AssertJUnit.assertEquals(ldap.getDn(uid), testLdapEntry.getDn());
    ldap.getAuthenticatorConfig().setConstructDn(false);

    // test subtree searching
    ldap.getAuthenticatorConfig().setSubtreeSearch(true);

    final String base = ldap.getAuthenticatorConfig().getBase();
    ldap.getAuthenticatorConfig().setBase(
      base.substring(base.indexOf(",") + 1));
    AssertJUnit.assertEquals(ldap.getDn(user), testLdapEntry.getDn());
    ldap.getAuthenticatorConfig().setBase(base);
    ldap.getAuthenticatorConfig().setSubtreeSearch(false);

    // test one level searching
    AssertJUnit.assertEquals(ldap.getDn(user), testLdapEntry.getDn());
    ldap.close();
  }


  /**
   * @param  dn  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateDn",
      "authenticateDnCredential",
      "authenticateDnReturnAttrs",
      "authenticateDnResults"
    }
  )
  @Test(
    groups = {"authtest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void authenticateDn(
    final String dn,
    final String credential,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    // test plain auth
    final Authenticator ldap = this.createTLSAuthenticator(false);
    AssertJUnit.assertFalse(ldap.authenticateDn(dn, INVALID_PASSWD));
    AssertJUnit.assertTrue(ldap.authenticateDn(dn, credential));

    // test auth with return attributes
    final Attributes attrs = ldap.authenticateDn(
      dn,
      credential,
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  dn  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateDn",
      "authenticateDnCredential",
      "authenticateDnReturnAttrs",
      "authenticateDnResults"
    }
  )
  @Test(
    groups = {"authtest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void authenticateDnSsl(
    final String dn,
    final String credential,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    // test plain auth
    final Authenticator ldap = this.createSSLAuthenticator(false);
    AssertJUnit.assertFalse(ldap.authenticateDn(dn, INVALID_PASSWD));
    AssertJUnit.assertTrue(ldap.authenticateDn(dn, credential));

    // test auth with return attributes
    final Attributes attrs = ldap.authenticateDn(
      dn,
      credential,
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  dn  to authenticate.
   * @param  credential  to authenticate with.
   * @param  filter  to authorize with.
   * @param  filterArgs  to authorize with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateDn",
      "authenticateDnCredential",
      "authenticateDnFilter",
      "authenticateDnFilterArgs",
      "authenticateDnReturnAttrs",
      "authenticateDnResults"
    }
  )
  @Test(
    groups = {"authtest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void authenticateDnAndAuthorize(
    final String dn,
    final String credential,
    final String filter,
    final String filterArgs,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    final Authenticator ldap = this.createTLSAuthenticator(false);

    // test plain auth
    AssertJUnit.assertFalse(
      ldap.authenticateDn(dn, INVALID_PASSWD, new SearchFilter(filter)));
    AssertJUnit.assertFalse(
      ldap.authenticateDn(
        dn, credential, new SearchFilter(INVALID_FILTER)));
    AssertJUnit.assertTrue(
      ldap.authenticateDn(
        dn,
        credential,
        new SearchFilter(filter, filterArgs.split("\\|"))));

    // test auth with return attributes
    final Attributes attrs = ldap.authenticateDn(
      dn,
      credential,
      new SearchFilter(filter, filterArgs.split("\\|")),
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  dn  to authenticate.
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
      "authenticateDn",
      "authenticateDnCredential"
    })
  @Test(groups = {"authtest"})
  public void authenticateDnHandler(final String dn, final String credential)
    throws Exception
  {
    // test authenticator handler
    final Authenticator ldap = this.createTLSAuthenticator(true);
    final TestAuthenticationResultHandler ah =
      new TestAuthenticationResultHandler();
    ldap.getAuthenticatorConfig().setAuthenticationResultHandlers(
      new AuthenticationResultHandler[] {ah});
    AssertJUnit.assertFalse(ldap.authenticateDn(dn, INVALID_PASSWD));
    AssertJUnit.assertFalse(ah.getResults().get(dn).booleanValue());
    AssertJUnit.assertTrue(ldap.authenticateDn(dn, credential));
    AssertJUnit.assertTrue(ah.getResults().get(dn).booleanValue());
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "digestMd5User", "digestMd5Credential" })
  @Test(groups = {"authtest"})
  public void authenticateDigestMd5(final String user, final String credential)
    throws Exception
  {
    final Authenticator ldap = TestUtil.createDigestMD5Authenticator();
    AssertJUnit.assertFalse(ldap.authenticateDn(user, INVALID_PASSWD));
    AssertJUnit.assertTrue(ldap.authenticateDn(user, credential));
    ldap.close();
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "cramMd5User", "cramMd5Credential" })
  @Test(groups = {"authtest"})
  public void authenticateCramMd5(final String user, final String credential)
    throws Exception
  {
    final Authenticator ldap = TestUtil.createCramMD5Authenticator();
    AssertJUnit.assertFalse(ldap.authenticateDn(user, INVALID_PASSWD));
    AssertJUnit.assertTrue(ldap.authenticateDn(user, credential));
    ldap.close();
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateUser",
      "authenticateCredential",
      "authenticateReturnAttrs",
      "authenticateResults"
    }
  )
  @Test(
    groups = {"authtest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void authenticate(
    final String user,
    final String credential,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    final Authenticator ldap = this.createTLSAuthenticator(false);

    // test plain auth
    AssertJUnit.assertFalse(ldap.authenticate(user, INVALID_PASSWD));
    AssertJUnit.assertTrue(ldap.authenticate(user, credential));

    // test auth with return attributes
    final Attributes attrs = ldap.authenticate(
      user,
      credential,
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateUser",
      "authenticateCredential",
      "authenticateReturnAttrs",
      "authenticateResults"
    }
  )
  @Test(
    groups = {"authtest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void authenticateSsl(
    final String user,
    final String credential,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    final Authenticator ldap = this.createSSLAuthenticator(false);

    // test plain auth
    AssertJUnit.assertFalse(ldap.authenticate(user, INVALID_PASSWD));
    AssertJUnit.assertTrue(ldap.authenticate(user, credential));

    // test auth with return attributes
    final Attributes attrs = ldap.authenticate(
      user,
      credential,
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  filter  to authorize with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateUser",
      "authenticateCredential",
      "authenticateFilter",
      "authenticateReturnAttrs",
      "authenticateResults"
    }
  )
  @Test(
    groups = {"authtest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void authenticateAndAuthorize(
    final String user,
    final String credential,
    final String filter,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    final Authenticator ldap = this.createTLSAuthenticator(false);

    // test plain auth
    AssertJUnit.assertFalse(
      ldap.authenticate(user, INVALID_PASSWD, new SearchFilter(filter)));
    AssertJUnit.assertFalse(
      ldap.authenticate(
        user, credential, new SearchFilter(INVALID_FILTER)));
    AssertJUnit.assertTrue(
      ldap.authenticate(user, credential, new SearchFilter(filter)));

    // test auth with return attributes
    final Attributes attrs = ldap.authenticate(
      user,
      credential,
      new SearchFilter(filter),
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateUser",
      "authenticateCredential",
      "authenticateReturnAttrs"
    }
  )
  @Test(groups = {"authtest"})
  public void authenticateExceptions(
    final String user,
    final String credential,
    final String returnAttrs)
    throws Exception
  {
    final Authenticator ldap = this.createTLSAuthenticator(true);

    try {
      ldap.authenticate(user, new Object(), returnAttrs.split("\\|"));
    } catch (Exception e) {
      AssertJUnit.assertEquals(e.getClass(), AuthenticationException.class);
    }

    try {
      ldap.authenticate(null, credential, returnAttrs.split("\\|"));
    } catch (Exception e) {
      AssertJUnit.assertEquals(e.getClass(), AuthenticationException.class);
    }

    try {
      ldap.authenticate("", credential, returnAttrs.split("\\|"));
    } catch (Exception e) {
      AssertJUnit.assertEquals(e.getClass(), AuthenticationException.class);
    }

    // must do this test after the search connection has been setup or
    // the anon auth will block the search
    ldap.getAuthenticatorConfig().setAuthtype(LdapConstants.NONE_AUTHTYPE);
    try {
      ldap.authenticate(user, credential, returnAttrs.split("\\|"));
    } catch (Exception e) {
      AssertJUnit.assertEquals(e.getClass(), AuthenticationException.class);
    }
    ldap.close();
  }
}
/*
  $Id: AuthenticatorTest.java 268 2009-05-28 14:21:40Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 268 $
  Updated: $Date: 2009-05-28 07:21:40 -0700 (Thu, 28 May 2009) $
*/
package edu.vt.middleware.ldap;

import javax.naming.AuthenticationException;
import javax.naming.directory.Attributes;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.bean.LdapEntry;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link Authenticator}.
 *
 * @author  Middleware Services
 * @version  $Revision: 268 $
 */
public class AuthenticatorTest
{

  /** Invalid password test data. */
  public static final String INVALID_PASSWD = "not-a-password";

  /** Invalid filter test data. */
  public static final String INVALID_FILTER = "departmentNumber=1111";

  /** Entry created for auth tests. */
  private static LdapEntry testLdapEntry;

  /** Ldap instance for concurrency testing. */
  private Authenticator singleTLSAuth;

  /** Ldap instance for concurrency testing. */
  private Authenticator singleSSLAuth;


  /**
   * Default constructor.
   *
   * @throws  Exception  if ldap cannot be constructed
   */
  public AuthenticatorTest()
    throws Exception
  {
    this.singleTLSAuth = TestUtil.createTLSAuthenticator();
    this.singleSSLAuth = TestUtil.createSSLAuthenticator();
  }


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "createEntry3" })
  @BeforeClass(groups = {"authtest"})
  public void createAuthEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtil.readFileIntoString(ldifFile);
    testLdapEntry = TestUtil.convertLdifToEntry(ldif);

    Ldap ldap = TestUtil.createSetupLdap();
    ldap.create(
      testLdapEntry.getDn(),
      testLdapEntry.getLdapAttributes().toAttributes());
    ldap.close();
    ldap = TestUtil.createLdap();
    while (
      !ldap.compare(
          testLdapEntry.getDn(),
          testLdapEntry.getDn().split(",")[0])) {
      Thread.sleep(100);
    }
    ldap.close();
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"authtest"})
  public void deleteAuthEntry()
    throws Exception
  {
    final Ldap ldap = TestUtil.createSetupLdap();
    ldap.delete(testLdapEntry.getDn());
    ldap.close();
  }


  /**
   * @param  createNew  whether to construct a new ldap instance.
   *
   * @return  <code>Authenticator</code>
   *
   * @throws  Exception  On ldap construction failure.
   */
  public Authenticator createTLSAuthenticator(final boolean createNew)
    throws Exception
  {
    if (createNew) {
      return TestUtil.createTLSAuthenticator();
    }
    return singleTLSAuth;
  }


  /**
   * @param  createNew  whether to construct a new ldap instance.
   *
   * @return  <code>Authenticator</code>
   *
   * @throws  Exception  On ldap construction failure.
   */
  public Authenticator createSSLAuthenticator(final boolean createNew)
    throws Exception
  {
    if (createNew) {
      return TestUtil.createSSLAuthenticator();
    }
    return singleSSLAuth;
  }


  /**
   * @param  uid  to get dn for.
   * @param  user  to get dn for.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "getDnUid", "getDnUser" })
  @Test(groups = {"authtest"})
  public void getDn(final String uid, final String user)
    throws Exception
  {
    final Authenticator ldap = this.createTLSAuthenticator(true);

    // test input
    AssertJUnit.assertNull(ldap.getDn(null));
    AssertJUnit.assertNull(ldap.getDn(""));

    // test empty user field
    final String[] userField = ldap.getAuthenticatorConfig().getUserField();
    ldap.getAuthenticatorConfig().setUserField(new String[] {});
    AssertJUnit.assertNull(ldap.getDn(user));
    ldap.getAuthenticatorConfig().setUserField(userField);

    // test construct dn
    ldap.getAuthenticatorConfig().setConstructDn(true);
    AssertJUnit.assertEquals(ldap.getDn(uid), testLdapEntry.getDn());
    ldap.getAuthenticatorConfig().setConstructDn(false);

    // test subtree searching
    ldap.getAuthenticatorConfig().setSubtreeSearch(true);

    final String base = ldap.getAuthenticatorConfig().getBase();
    ldap.getAuthenticatorConfig().setBase(
      base.substring(base.indexOf(",") + 1));
    AssertJUnit.assertEquals(ldap.getDn(user), testLdapEntry.getDn());
    ldap.getAuthenticatorConfig().setBase(base);
    ldap.getAuthenticatorConfig().setSubtreeSearch(false);

    // test one level searching
    AssertJUnit.assertEquals(ldap.getDn(user), testLdapEntry.getDn());
    ldap.close();
  }


  /**
   * @param  dn  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateDn",
      "authenticateDnCredential",
      "authenticateDnReturnAttrs",
      "authenticateDnResults"
    }
  )
  @Test(
    groups = {"authtest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void authenticateDn(
    final String dn,
    final String credential,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    // test plain auth
    final Authenticator ldap = this.createTLSAuthenticator(false);
    AssertJUnit.assertFalse(ldap.authenticateDn(dn, INVALID_PASSWD));
    AssertJUnit.assertTrue(ldap.authenticateDn(dn, credential));

    // test auth with return attributes
    final Attributes attrs = ldap.authenticateDn(
      dn,
      credential,
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  dn  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateDn",
      "authenticateDnCredential",
      "authenticateDnReturnAttrs",
      "authenticateDnResults"
    }
  )
  @Test(
    groups = {"authtest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void authenticateDnSsl(
    final String dn,
    final String credential,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    // test plain auth
    final Authenticator ldap = this.createSSLAuthenticator(false);
    AssertJUnit.assertFalse(ldap.authenticateDn(dn, INVALID_PASSWD));
    AssertJUnit.assertTrue(ldap.authenticateDn(dn, credential));

    // test auth with return attributes
    final Attributes attrs = ldap.authenticateDn(
      dn,
      credential,
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  dn  to authenticate.
   * @param  credential  to authenticate with.
   * @param  filter  to authorize with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateDn",
      "authenticateDnCredential",
      "authenticateDnFilter",
      "authenticateDnReturnAttrs",
      "authenticateDnResults"
    }
  )
  @Test(
    groups = {"authtest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void authenticateDnAndAuthorize(
    final String dn,
    final String credential,
    final String filter,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    final Authenticator ldap = this.createTLSAuthenticator(false);

    // test plain auth
    AssertJUnit.assertFalse(ldap.authenticateDn(dn, INVALID_PASSWD, filter));
    AssertJUnit.assertFalse(
      ldap.authenticateDn(dn, credential, INVALID_FILTER));
    AssertJUnit.assertTrue(ldap.authenticateDn(dn, credential, filter));

    // test auth with return attributes
    final Attributes attrs = ldap.authenticateDn(
      dn,
      credential,
      filter,
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "digestMd5User", "digestMd5Credential" })
  @Test(groups = {"authtest"})
  public void authenticateDigestMd5(final String user, final String credential)
    throws Exception
  {
    final Authenticator ldap = TestUtil.createDigestMD5Authenticator();
    AssertJUnit.assertFalse(ldap.authenticateDn(user, INVALID_PASSWD));
    AssertJUnit.assertTrue(ldap.authenticateDn(user, credential));
    ldap.close();
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "cramMd5User", "cramMd5Credential" })
  @Test(groups = {"authtest"})
  public void authenticateCramMd5(final String user, final String credential)
    throws Exception
  {
    final Authenticator ldap = TestUtil.createCramMD5Authenticator();
    AssertJUnit.assertFalse(ldap.authenticateDn(user, INVALID_PASSWD));
    AssertJUnit.assertTrue(ldap.authenticateDn(user, credential));
    ldap.close();
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateUser",
      "authenticateCredential",
      "authenticateReturnAttrs",
      "authenticateResults"
    }
  )
  @Test(
    groups = {"authtest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void authenticate(
    final String user,
    final String credential,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    final Authenticator ldap = this.createTLSAuthenticator(false);

    // test plain auth
    AssertJUnit.assertFalse(ldap.authenticate(user, INVALID_PASSWD));
    AssertJUnit.assertTrue(ldap.authenticate(user, credential));

    // test auth with return attributes
    final Attributes attrs = ldap.authenticate(
      user,
      credential,
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateUser",
      "authenticateCredential",
      "authenticateReturnAttrs",
      "authenticateResults"
    }
  )
  @Test(
    groups = {"authtest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void authenticateSsl(
    final String user,
    final String credential,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    final Authenticator ldap = this.createSSLAuthenticator(false);

    // test plain auth
    AssertJUnit.assertFalse(ldap.authenticate(user, INVALID_PASSWD));
    AssertJUnit.assertTrue(ldap.authenticate(user, credential));

    // test auth with return attributes
    final Attributes attrs = ldap.authenticate(
      user,
      credential,
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  filter  to authorize with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateUser",
      "authenticateCredential",
      "authenticateFilter",
      "authenticateReturnAttrs",
      "authenticateResults"
    }
  )
  @Test(
    groups = {"authtest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void authenticateAndAuthorize(
    final String user,
    final String credential,
    final String filter,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    final Authenticator ldap = this.createTLSAuthenticator(false);

    // test plain auth
    AssertJUnit.assertFalse(ldap.authenticate(user, INVALID_PASSWD, filter));
    AssertJUnit.assertFalse(
      ldap.authenticate(user, credential, INVALID_FILTER));
    AssertJUnit.assertTrue(ldap.authenticate(user, credential, filter));

    // test auth with return attributes
    final Attributes attrs = ldap.authenticate(
      user,
      credential,
      filter,
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateUser",
      "authenticateCredential",
      "authenticateReturnAttrs"
    }
  )
  @Test(groups = {"authtest"})
  public void authenticateExceptions(
    final String user,
    final String credential,
    final String returnAttrs)
    throws Exception
  {
    final Authenticator ldap = this.createTLSAuthenticator(true);

    try {
      ldap.authenticate(user, new Object(), returnAttrs.split("\\|"));
    } catch (Exception e) {
      AssertJUnit.assertEquals(e.getClass(), AuthenticationException.class);
    }

    try {
      ldap.authenticate(null, credential, returnAttrs.split("\\|"));
    } catch (Exception e) {
      AssertJUnit.assertEquals(e.getClass(), AuthenticationException.class);
    }

    try {
      ldap.authenticate("", credential, returnAttrs.split("\\|"));
    } catch (Exception e) {
      AssertJUnit.assertEquals(e.getClass(), AuthenticationException.class);
    }

    // must do this test after the search connection has been setup or
    // the anon auth will block the search
    ldap.getAuthenticatorConfig().setAuthtype(LdapConstants.NONE_AUTHTYPE);
    try {
      ldap.authenticate(user, credential, returnAttrs.split("\\|"));
    } catch (Exception e) {
      AssertJUnit.assertEquals(e.getClass(), AuthenticationException.class);
    }
    ldap.close();
  }
}
/*
  $Id: AuthenticatorTest.java 420 2009-08-05 21:41:21Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 420 $
  Updated: $Date: 2009-08-05 14:41:21 -0700 (Wed, 05 Aug 2009) $
*/
package edu.vt.middleware.ldap;

import javax.naming.AuthenticationException;
import javax.naming.directory.Attributes;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.handler.AuthenticationResultHandler;
import edu.vt.middleware.ldap.handler.TestAuthenticationResultHandler;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link Authenticator}.
 *
 * @author  Middleware Services
 * @version  $Revision: 420 $
 */
public class AuthenticatorTest
{

  /** Invalid password test data. */
  public static final String INVALID_PASSWD = "not-a-password";

  /** Invalid filter test data. */
  public static final String INVALID_FILTER = "departmentNumber=1111";

  /** Entry created for auth tests. */
  private static LdapEntry testLdapEntry;

  /** Ldap instance for concurrency testing. */
  private Authenticator singleTLSAuth;

  /** Ldap instance for concurrency testing. */
  private Authenticator singleSSLAuth;


  /**
   * Default constructor.
   *
   * @throws  Exception  if ldap cannot be constructed
   */
  public AuthenticatorTest()
    throws Exception
  {
    this.singleTLSAuth = TestUtil.createTLSAuthenticator();
    this.singleSSLAuth = TestUtil.createSSLAuthenticator();
  }


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "createEntry3" })
  @BeforeClass(groups = {"authtest"})
  public void createAuthEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtil.readFileIntoString(ldifFile);
    testLdapEntry = TestUtil.convertLdifToEntry(ldif);

    Ldap ldap = TestUtil.createSetupLdap();
    ldap.create(
      testLdapEntry.getDn(),
      testLdapEntry.getLdapAttributes().toAttributes());
    ldap.close();
    ldap = TestUtil.createLdap();
    while (
      !ldap.compare(
          testLdapEntry.getDn(),
          testLdapEntry.getDn().split(",")[0])) {
      Thread.sleep(100);
    }
    ldap.close();
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"authtest"})
  public void deleteAuthEntry()
    throws Exception
  {
    final Ldap ldap = TestUtil.createSetupLdap();
    ldap.delete(testLdapEntry.getDn());
    ldap.close();
  }


  /**
   * @param  createNew  whether to construct a new ldap instance.
   *
   * @return  <code>Authenticator</code>
   *
   * @throws  Exception  On ldap construction failure.
   */
  public Authenticator createTLSAuthenticator(final boolean createNew)
    throws Exception
  {
    if (createNew) {
      return TestUtil.createTLSAuthenticator();
    }
    return singleTLSAuth;
  }


  /**
   * @param  createNew  whether to construct a new ldap instance.
   *
   * @return  <code>Authenticator</code>
   *
   * @throws  Exception  On ldap construction failure.
   */
  public Authenticator createSSLAuthenticator(final boolean createNew)
    throws Exception
  {
    if (createNew) {
      return TestUtil.createSSLAuthenticator();
    }
    return singleSSLAuth;
  }


  /**
   * @param  uid  to get dn for.
   * @param  user  to get dn for.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "getDnUid", "getDnUser" })
  @Test(groups = {"authtest"})
  public void getDn(final String uid, final String user)
    throws Exception
  {
    final Authenticator ldap = this.createTLSAuthenticator(true);

    // test input
    AssertJUnit.assertNull(ldap.getDn(null));
    AssertJUnit.assertNull(ldap.getDn(""));

    // test empty user field
    final String[] userField = ldap.getAuthenticatorConfig().getUserField();
    ldap.getAuthenticatorConfig().setUserField(new String[] {});
    AssertJUnit.assertNull(ldap.getDn(user));
    ldap.getAuthenticatorConfig().setUserField(userField);

    // test construct dn
    ldap.getAuthenticatorConfig().setConstructDn(true);
    AssertJUnit.assertEquals(ldap.getDn(uid), testLdapEntry.getDn());
    ldap.getAuthenticatorConfig().setConstructDn(false);

    // test subtree searching
    ldap.getAuthenticatorConfig().setSubtreeSearch(true);

    final String base = ldap.getAuthenticatorConfig().getBase();
    ldap.getAuthenticatorConfig().setBase(
      base.substring(base.indexOf(",") + 1));
    AssertJUnit.assertEquals(ldap.getDn(user), testLdapEntry.getDn());
    ldap.getAuthenticatorConfig().setBase(base);
    ldap.getAuthenticatorConfig().setSubtreeSearch(false);

    // test one level searching
    AssertJUnit.assertEquals(ldap.getDn(user), testLdapEntry.getDn());
    ldap.close();
  }


  /**
   * @param  dn  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateDn",
      "authenticateDnCredential",
      "authenticateDnReturnAttrs",
      "authenticateDnResults"
    }
  )
  @Test(
    groups = {"authtest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void authenticateDn(
    final String dn,
    final String credential,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    // test plain auth
    final Authenticator ldap = this.createTLSAuthenticator(false);
    AssertJUnit.assertFalse(ldap.authenticateDn(dn, INVALID_PASSWD));
    AssertJUnit.assertTrue(ldap.authenticateDn(dn, credential));

    // test auth with return attributes
    final Attributes attrs = ldap.authenticateDn(
      dn,
      credential,
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  dn  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateDn",
      "authenticateDnCredential",
      "authenticateDnReturnAttrs",
      "authenticateDnResults"
    }
  )
  @Test(
    groups = {"authtest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void authenticateDnSsl(
    final String dn,
    final String credential,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    // test plain auth
    final Authenticator ldap = this.createSSLAuthenticator(false);
    AssertJUnit.assertFalse(ldap.authenticateDn(dn, INVALID_PASSWD));
    AssertJUnit.assertTrue(ldap.authenticateDn(dn, credential));

    // test auth with return attributes
    final Attributes attrs = ldap.authenticateDn(
      dn,
      credential,
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  dn  to authenticate.
   * @param  credential  to authenticate with.
   * @param  filter  to authorize with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateDn",
      "authenticateDnCredential",
      "authenticateDnFilter",
      "authenticateDnReturnAttrs",
      "authenticateDnResults"
    }
  )
  @Test(
    groups = {"authtest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void authenticateDnAndAuthorize(
    final String dn,
    final String credential,
    final String filter,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    final Authenticator ldap = this.createTLSAuthenticator(false);

    // test plain auth
    AssertJUnit.assertFalse(ldap.authenticateDn(dn, INVALID_PASSWD, filter));
    AssertJUnit.assertFalse(
      ldap.authenticateDn(dn, credential, INVALID_FILTER));
    AssertJUnit.assertTrue(ldap.authenticateDn(dn, credential, filter));

    // test auth with return attributes
    final Attributes attrs = ldap.authenticateDn(
      dn,
      credential,
      filter,
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  dn  to authenticate.
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
      "authenticateDn",
      "authenticateDnCredential"
    })
  @Test(groups = {"authtest"})
  public void authenticateDnHandler(final String dn, final String credential)
    throws Exception
  {
    // test authenticator handler
    final Authenticator ldap = this.createTLSAuthenticator(true);
    final TestAuthenticationResultHandler ah =
      new TestAuthenticationResultHandler();
    ldap.getAuthenticatorConfig().setAuthenticationResultHandlers(
      new AuthenticationResultHandler[] {ah});
    AssertJUnit.assertFalse(ldap.authenticateDn(dn, INVALID_PASSWD));
    AssertJUnit.assertFalse(ah.getResults().get(dn).booleanValue());
    AssertJUnit.assertTrue(ldap.authenticateDn(dn, credential));
    AssertJUnit.assertTrue(ah.getResults().get(dn).booleanValue());
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "digestMd5User", "digestMd5Credential" })
  @Test(groups = {"authtest"})
  public void authenticateDigestMd5(final String user, final String credential)
    throws Exception
  {
    final Authenticator ldap = TestUtil.createDigestMD5Authenticator();
    AssertJUnit.assertFalse(ldap.authenticateDn(user, INVALID_PASSWD));
    AssertJUnit.assertTrue(ldap.authenticateDn(user, credential));
    ldap.close();
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "cramMd5User", "cramMd5Credential" })
  @Test(groups = {"authtest"})
  public void authenticateCramMd5(final String user, final String credential)
    throws Exception
  {
    final Authenticator ldap = TestUtil.createCramMD5Authenticator();
    AssertJUnit.assertFalse(ldap.authenticateDn(user, INVALID_PASSWD));
    AssertJUnit.assertTrue(ldap.authenticateDn(user, credential));
    ldap.close();
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateUser",
      "authenticateCredential",
      "authenticateReturnAttrs",
      "authenticateResults"
    }
  )
  @Test(
    groups = {"authtest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void authenticate(
    final String user,
    final String credential,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    final Authenticator ldap = this.createTLSAuthenticator(false);

    // test plain auth
    AssertJUnit.assertFalse(ldap.authenticate(user, INVALID_PASSWD));
    AssertJUnit.assertTrue(ldap.authenticate(user, credential));

    // test auth with return attributes
    final Attributes attrs = ldap.authenticate(
      user,
      credential,
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateUser",
      "authenticateCredential",
      "authenticateReturnAttrs",
      "authenticateResults"
    }
  )
  @Test(
    groups = {"authtest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void authenticateSsl(
    final String user,
    final String credential,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    final Authenticator ldap = this.createSSLAuthenticator(false);

    // test plain auth
    AssertJUnit.assertFalse(ldap.authenticate(user, INVALID_PASSWD));
    AssertJUnit.assertTrue(ldap.authenticate(user, credential));

    // test auth with return attributes
    final Attributes attrs = ldap.authenticate(
      user,
      credential,
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  filter  to authorize with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateUser",
      "authenticateCredential",
      "authenticateFilter",
      "authenticateReturnAttrs",
      "authenticateResults"
    }
  )
  @Test(
    groups = {"authtest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void authenticateAndAuthorize(
    final String user,
    final String credential,
    final String filter,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    final Authenticator ldap = this.createTLSAuthenticator(false);

    // test plain auth
    AssertJUnit.assertFalse(ldap.authenticate(user, INVALID_PASSWD, filter));
    AssertJUnit.assertFalse(
      ldap.authenticate(user, credential, INVALID_FILTER));
    AssertJUnit.assertTrue(ldap.authenticate(user, credential, filter));

    // test auth with return attributes
    final Attributes attrs = ldap.authenticate(
      user,
      credential,
      filter,
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  returnAttrs  to search for.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "authenticateUser",
      "authenticateCredential",
      "authenticateReturnAttrs"
    }
  )
  @Test(groups = {"authtest"})
  public void authenticateExceptions(
    final String user,
    final String credential,
    final String returnAttrs)
    throws Exception
  {
    final Authenticator ldap = this.createTLSAuthenticator(true);

    try {
      ldap.authenticate(user, new Object(), returnAttrs.split("\\|"));
    } catch (Exception e) {
      AssertJUnit.assertEquals(e.getClass(), AuthenticationException.class);
    }

    try {
      ldap.authenticate(null, credential, returnAttrs.split("\\|"));
    } catch (Exception e) {
      AssertJUnit.assertEquals(e.getClass(), AuthenticationException.class);
    }

    try {
      ldap.authenticate("", credential, returnAttrs.split("\\|"));
    } catch (Exception e) {
      AssertJUnit.assertEquals(e.getClass(), AuthenticationException.class);
    }

    // must do this test after the search connection has been setup or
    // the anon auth will block the search
    ldap.getAuthenticatorConfig().setAuthtype(LdapConstants.NONE_AUTHTYPE);
    try {
      ldap.authenticate(user, credential, returnAttrs.split("\\|"));
    } catch (Exception e) {
      AssertJUnit.assertEquals(e.getClass(), AuthenticationException.class);
    }
    ldap.close();
  }
}
