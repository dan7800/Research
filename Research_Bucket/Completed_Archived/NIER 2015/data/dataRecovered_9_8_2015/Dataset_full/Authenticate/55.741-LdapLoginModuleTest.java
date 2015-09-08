/*
  $Id: LdapLoginModuleTest.java 639 2009-09-18 17:55:42Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 639 $
  Updated: $Date: 2009-09-18 10:55:42 -0700 (Fri, 18 Sep 2009) $
*/
package edu.vt.middleware.ldap.jaas;

import java.util.Iterator;
import java.util.Set;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.SearchFilter;
import edu.vt.middleware.ldap.TestUtil;
import edu.vt.middleware.ldap.bean.LdapEntry;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link LdapLoginModule}.
 *
 * @author  Middleware Services
 * @version  $Revision: 639 $
 */
public class LdapLoginModuleTest
{

  /** Invalid password test data. */
  public static final String INVALID_PASSWD = "not-a-password";

  /** Entry created for auth tests. */
  private static LdapEntry testLdapEntry;


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "createEntry7" })
  @BeforeClass(groups = {"jaastest"})
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

    System.setProperty(
      "java.security.auth.login.config",
      "src/test/resources/ldap_jaas.config");
    System.setProperty(
      "javax.net.ssl.trustStore",
      "src/test/resources/ed.truststore");
    System.setProperty("javax.net.ssl.trustStoreType", "BKS");
    System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"jaastest"})
  public void deleteAuthEntry()
    throws Exception
  {
    System.clearProperty("java.security.auth.login.config");
    System.clearProperty("javax.net.ssl.trustStore");
    System.clearProperty("javax.net.ssl.trustStoreType");
    System.clearProperty("javax.net.ssl.trustStorePassword");

    final Ldap ldap = TestUtil.createSetupLdap();
    ldap.delete(testLdapEntry.getDn());
    ldap.close();
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasUserRole", "jaasCredential" })
  @Test(
    groups = {"jaastest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void contextTest(
    final String dn,
    final String user,
    final String role,
    final String credential)
    throws Exception
  {
    this.doContextTest("vt-ldap", dn, user, role, credential);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasUserRole", "jaasCredential" })
  @Test(
    groups = {"jaastest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void authzContextTest(
    final String dn,
    final String user,
    final String role,
    final String credential)
    throws Exception
  {
    this.doContextTest("vt-ldap-authz", dn, user, role, credential);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasUserRole", "jaasCredential" })
  @Test(
    groups = {"jaastest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void filterContextTest(
    final String dn,
    final String user,
    final String role,
    final String credential)
    throws Exception
  {
    this.doContextTest("vt-ldap-filter", dn, user, role, credential);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasRoleCombined", "jaasCredential" })
  @Test(
    groups = {"jaastest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void rolesContextTest(
    final String dn,
    final String user,
    final String role,
    final String credential)
    throws Exception
  {
    this.doContextTest("vt-ldap-roles", dn, user, role, credential);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasUserRole", "jaasCredential" })
  @Test(groups = {"jaastest"})
  public void useFirstContextTest(
    final String dn,
    final String user,
    final String role,
    final String credential)
    throws Exception
  {
    this.doContextTest("vt-ldap-use-first", dn, user, role, credential);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasRoleCombined", "jaasCredential" })
  @Test(groups = {"jaastest"})
  public void tryFirstContextTest(
    final String dn,
    final String user,
    final String role,
    final String credential)
    throws Exception
  {
    this.doContextTest("vt-ldap-try-first", dn, user, role, credential);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasUserRole", "jaasCredential" })
  @Test(groups = {"jaastest"})
  public void oldContextTest(
    final String dn,
    final String user,
    final String role,
    final String credential)
    throws Exception
  {
    this.doContextTest("vt-ldap-deprecated", dn, user, role, credential);
  }


  /**
   * @param  name  of the jaas configuration
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  private void doContextTest(
    final String name,
    final String dn,
    final String user,
    final String role,
    final String credential)
    throws Exception
  {
    final TestCallbackHandler callback = new TestCallbackHandler();
    callback.setName(user);
    callback.setPassword(INVALID_PASSWD);

    LoginContext lc = new LoginContext(name, callback);
    try {
      lc.login();
      AssertJUnit.fail("Invalid password, login should have failed");
    } catch (Exception e) {
      AssertJUnit.assertEquals(e.getClass(), LoginException.class);
    }

    callback.setPassword(credential);
    lc = new LoginContext(name, callback);
    try {
      lc.login();
    } catch (Exception e) {
      AssertJUnit.fail(e.getMessage());
    }

    final Set<LdapPrincipal> principals = lc.getSubject().getPrincipals(
      LdapPrincipal.class);
    AssertJUnit.assertEquals(1, principals.size());

    final LdapPrincipal p = principals.iterator().next();
    AssertJUnit.assertEquals(p.getName(), user);

    final Set<LdapDnPrincipal> dnPrincipals = lc.getSubject().getPrincipals(
      LdapDnPrincipal.class);
    AssertJUnit.assertEquals(1, dnPrincipals.size());

    final LdapDnPrincipal dnP = dnPrincipals.iterator().next();
    AssertJUnit.assertEquals(dnP.getName(), dn);

    final Set<LdapRole> roles = lc.getSubject().getPrincipals(LdapRole.class);

    final Iterator<LdapRole> roleIter = roles.iterator();
    final String[] checkRoles = role.split("\\|");
    AssertJUnit.assertEquals(checkRoles.length, roles.size());
    while (roleIter.hasNext()) {
      final LdapRole r = roleIter.next();
      boolean match = false;
      for (String s : checkRoles) {
        if (s.equals(r.getName())) {
          match = true;
        }
      }
      AssertJUnit.assertTrue(match);
    }

    final Set<LdapCredential> credentials = lc.getSubject()
        .getPrivateCredentials(LdapCredential.class);
    AssertJUnit.assertEquals(1, credentials.size());

    final LdapCredential c = credentials.iterator().next();
    AssertJUnit.assertEquals(
      new String((char[]) c.getCredential()),
      credential);

    try {
      lc.logout();
    } catch (Exception e) {
      AssertJUnit.fail(e.getMessage());
    }

    AssertJUnit.assertEquals(0, lc.getSubject().getPrincipals().size());
    AssertJUnit.assertEquals(0, lc.getSubject().getPrivateCredentials().size());
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasRoleCombined" })
  @Test(
    groups = {"jaastest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void rolesOnlyContextTest(
    final String dn,
    final String user,
    final String role)
    throws Exception
  {
    this.doRolesContextTest("vt-ldap-roles-only", dn, user, role);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasRoleCombined" })
  @Test(
    groups = {"jaastest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void dnRolesOnlyContextTest(
    final String dn,
    final String user,
    final String role)
    throws Exception
  {
    this.doRolesContextTest("vt-ldap-dn-roles-only", dn, user, role);
  }


  /**
   * @param  name  of the jaas configuration
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   *
   * @throws  Exception  On test failure.
   */
  private void doRolesContextTest(
    final String name,
    final String dn,
    final String user,
    final String role)
    throws Exception
  {
    final TestCallbackHandler callback = new TestCallbackHandler();
    callback.setName(user);

    final LoginContext lc = new LoginContext(name, callback);
    try {
      lc.login();
    } catch (Exception e) {
      AssertJUnit.fail(e.getMessage());
    }

    final Set<LdapRole> roles = lc.getSubject().getPrincipals(LdapRole.class);

    final Iterator<LdapRole> roleIter = roles.iterator();
    final String[] checkRoles = role.split("\\|");
    AssertJUnit.assertEquals(checkRoles.length, roles.size());
    while (roleIter.hasNext()) {
      final LdapRole r = roleIter.next();
      boolean match = false;
      for (String s : checkRoles) {
        if (s.equals(r.getName())) {
          match = true;
        }
      }
      AssertJUnit.assertTrue(match);
    }

    final Set<?> credentials = lc.getSubject().getPrivateCredentials();
    AssertJUnit.assertEquals(0, credentials.size());

    try {
      lc.logout();
    } catch (Exception e) {
      AssertJUnit.fail(e.getMessage());
    }

    AssertJUnit.assertEquals(0, lc.getSubject().getPrincipals().size());
    AssertJUnit.assertEquals(0, lc.getSubject().getPrivateCredentials().size());
  }
}
/*
  $Id: LdapLoginModuleTest.java 282 2009-05-29 21:57:32Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 282 $
  Updated: $Date: 2009-05-29 14:57:32 -0700 (Fri, 29 May 2009) $
*/
package edu.vt.middleware.ldap.jaas;

import java.util.Iterator;
import java.util.Set;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.TestUtil;
import edu.vt.middleware.ldap.bean.LdapEntry;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link LdapLoginModule}.
 *
 * @author  Middleware Services
 * @version  $Revision: 282 $
 */
public class LdapLoginModuleTest
{

  /** Invalid password test data. */
  public static final String INVALID_PASSWD = "not-a-password";

  /** Entry created for auth tests. */
  private static LdapEntry testLdapEntry;


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "createEntry7" })
  @BeforeClass(groups = {"jaastest"})
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

    System.setProperty(
      "java.security.auth.login.config",
      "src/test/resources/ldap_jaas.config");
    System.setProperty(
      "javax.net.ssl.trustStore",
      "src/test/resources/ed.truststore");
    System.setProperty("javax.net.ssl.trustStoreType", "BKS");
    System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"jaastest"})
  public void deleteAuthEntry()
    throws Exception
  {
    System.clearProperty("java.security.auth.login.config");
    System.clearProperty("javax.net.ssl.trustStore");
    System.clearProperty("javax.net.ssl.trustStoreType");
    System.clearProperty("javax.net.ssl.trustStorePassword");

    final Ldap ldap = TestUtil.createSetupLdap();
    ldap.delete(testLdapEntry.getDn());
    ldap.close();
  }


  /**
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasUser", "jaasRole", "jaasCredential" })
  @Test(
    groups = {"jaastest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void contextTest(
    final String user,
    final String role,
    final String credential)
    throws Exception
  {
    this.doContextTest(user, role, credential);
  }


  /**
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasUser", "jaasRole", "jaasCredential" })
  @Test(
    groups = {"jaastest"},
    dependsOnMethods = {"contextTest"}
  )
  public void oldContextTest(
    final String user,
    final String role,
    final String credential)
    throws Exception
  {
    System.setProperty(
      "java.security.auth.login.config",
      "src/test/resources/ldap_jaas.deprecated.config");
    this.doContextTest(user, role, credential);
  }


  /**
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  private void doContextTest(
    final String user,
    final String role,
    final String credential)
    throws Exception
  {
    final TestCallbackHandler callback = new TestCallbackHandler();
    callback.setName(user);
    callback.setPassword(INVALID_PASSWD);

    LoginContext lc = new LoginContext("vt-ldap", callback);
    try {
      lc.login();
      AssertJUnit.fail("Invalid password, login should have failed");
    } catch (Exception e) {
      AssertJUnit.assertEquals(e.getClass(), LoginException.class);
    }

    callback.setPassword(credential);
    lc = new LoginContext("vt-ldap", callback);
    try {
      lc.login();
    } catch (Exception e) {
      AssertJUnit.fail(e.getMessage());
    }

    final Set<LdapPrincipal> principals = lc.getSubject().getPrincipals(
      LdapPrincipal.class);
    AssertJUnit.assertEquals(principals.size(), 1);

    final LdapPrincipal p = principals.iterator().next();
    AssertJUnit.assertEquals(p.getName(), user);

    final Set<LdapRole> roles = lc.getSubject().getPrincipals(LdapRole.class);

    final Iterator<LdapRole> roleIter = roles.iterator();
    final String[] checkRoles = role.split("\\|");
    AssertJUnit.assertEquals(checkRoles.length, roles.size());
    while (roleIter.hasNext()) {
      final LdapRole r = roleIter.next();
      boolean match = false;
      for (String s : checkRoles) {
        if (s.equals(r.getName())) {
          match = true;
        }
      }
      AssertJUnit.assertTrue(match);
    }

    final Set<LdapCredential> credentials = lc.getSubject()
        .getPrivateCredentials(LdapCredential.class);
    AssertJUnit.assertEquals(credentials.size(), 1);

    final LdapCredential c = credentials.iterator().next();
    AssertJUnit.assertEquals(
      new String((char[]) c.getCredential()),
      credential);

    try {
      lc.logout();
    } catch (Exception e) {
      AssertJUnit.fail(e.getMessage());
    }

    AssertJUnit.assertEquals(lc.getSubject().getPrincipals().size(), 0);
    AssertJUnit.assertEquals(lc.getSubject().getPrivateCredentials().size(), 0);
  }
}
/*
  $Id: LdapLoginModuleTest.java 466 2009-08-20 18:05:31Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 466 $
  Updated: $Date: 2009-08-20 11:05:31 -0700 (Thu, 20 Aug 2009) $
*/
package edu.vt.middleware.ldap.jaas;

import java.util.Iterator;
import java.util.Set;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.TestUtil;
import edu.vt.middleware.ldap.bean.LdapEntry;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link LdapLoginModule}.
 *
 * @author  Middleware Services
 * @version  $Revision: 466 $
 */
public class LdapLoginModuleTest
{

  /** Invalid password test data. */
  public static final String INVALID_PASSWD = "not-a-password";

  /** Entry created for auth tests. */
  private static LdapEntry testLdapEntry;


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "createEntry7" })
  @BeforeClass(groups = {"jaastest"})
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

    System.setProperty(
      "java.security.auth.login.config",
      "src/test/resources/ldap_jaas.config");
    System.setProperty(
      "javax.net.ssl.trustStore",
      "src/test/resources/ed.truststore");
    System.setProperty("javax.net.ssl.trustStoreType", "BKS");
    System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"jaastest"})
  public void deleteAuthEntry()
    throws Exception
  {
    System.clearProperty("java.security.auth.login.config");
    System.clearProperty("javax.net.ssl.trustStore");
    System.clearProperty("javax.net.ssl.trustStoreType");
    System.clearProperty("javax.net.ssl.trustStorePassword");

    final Ldap ldap = TestUtil.createSetupLdap();
    ldap.delete(testLdapEntry.getDn());
    ldap.close();
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasUserRole", "jaasCredential" })
  @Test(
    groups = {"jaastest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void contextTest(
    final String dn,
    final String user,
    final String role,
    final String credential)
    throws Exception
  {
    this.doContextTest("vt-ldap", dn, user, role, credential);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasRoleCombined", "jaasCredential" })
  @Test(
    groups = {"jaastest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void rolesContextTest(
    final String dn,
    final String user,
    final String role,
    final String credential)
    throws Exception
  {
    this.doContextTest("vt-ldap-roles", dn, user, role, credential);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasUserRole", "jaasCredential" })
  @Test(groups = {"jaastest"})
  public void useFirstContextTest(
    final String dn,
    final String user,
    final String role,
    final String credential)
    throws Exception
  {
    this.doContextTest("vt-ldap-use-first", dn, user, role, credential);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasRoleCombined", "jaasCredential" })
  @Test(groups = {"jaastest"})
  public void tryFirstContextTest(
    final String dn,
    final String user,
    final String role,
    final String credential)
    throws Exception
  {
    this.doContextTest("vt-ldap-try-first", dn, user, role, credential);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasUserRole", "jaasCredential" })
  @Test(groups = {"jaastest"})
  public void oldContextTest(
    final String dn,
    final String user,
    final String role,
    final String credential)
    throws Exception
  {
    this.doContextTest("vt-ldap-deprecated", dn, user, role, credential);
  }


  /**
   * @param  name  of the jaas configuration
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  private void doContextTest(
    final String name,
    final String dn,
    final String user,
    final String role,
    final String credential)
    throws Exception
  {
    final TestCallbackHandler callback = new TestCallbackHandler();
    callback.setName(user);
    callback.setPassword(INVALID_PASSWD);

    LoginContext lc = new LoginContext(name, callback);
    try {
      lc.login();
      AssertJUnit.fail("Invalid password, login should have failed");
    } catch (Exception e) {
      AssertJUnit.assertEquals(e.getClass(), LoginException.class);
    }

    callback.setPassword(credential);
    lc = new LoginContext(name, callback);
    try {
      lc.login();
    } catch (Exception e) {
      AssertJUnit.fail(e.getMessage());
    }

    final Set<LdapPrincipal> principals = lc.getSubject().getPrincipals(
      LdapPrincipal.class);
    AssertJUnit.assertEquals(1, principals.size());

    final LdapPrincipal p = principals.iterator().next();
    AssertJUnit.assertEquals(p.getName(), user);

    final Set<LdapDnPrincipal> dnPrincipals = lc.getSubject().getPrincipals(
      LdapDnPrincipal.class);
    AssertJUnit.assertEquals(1, dnPrincipals.size());

    final LdapDnPrincipal dnP = dnPrincipals.iterator().next();
    AssertJUnit.assertEquals(dnP.getName(), dn);

    final Set<LdapRole> roles = lc.getSubject().getPrincipals(LdapRole.class);

    final Iterator<LdapRole> roleIter = roles.iterator();
    final String[] checkRoles = role.split("\\|");
    AssertJUnit.assertEquals(checkRoles.length, roles.size());
    while (roleIter.hasNext()) {
      final LdapRole r = roleIter.next();
      boolean match = false;
      for (String s : checkRoles) {
        if (s.equals(r.getName())) {
          match = true;
        }
      }
      AssertJUnit.assertTrue(match);
    }

    final Set<LdapCredential> credentials = lc.getSubject()
        .getPrivateCredentials(LdapCredential.class);
    AssertJUnit.assertEquals(1, credentials.size());

    final LdapCredential c = credentials.iterator().next();
    AssertJUnit.assertEquals(
      new String((char[]) c.getCredential()),
      credential);

    try {
      lc.logout();
    } catch (Exception e) {
      AssertJUnit.fail(e.getMessage());
    }

    AssertJUnit.assertEquals(0, lc.getSubject().getPrincipals().size());
    AssertJUnit.assertEquals(0, lc.getSubject().getPrivateCredentials().size());
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasRoleCombined" })
  @Test(
    groups = {"jaastest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void rolesOnlyContextTest(
    final String dn,
    final String user,
    final String role)
    throws Exception
  {
    this.doRolesContextTest("vt-ldap-roles-only", dn, user, role);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasRoleCombined" })
  @Test(
    groups = {"jaastest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void dnRolesOnlyContextTest(
    final String dn,
    final String user,
    final String role)
    throws Exception
  {
    this.doRolesContextTest("vt-ldap-dn-roles-only", dn, user, role);
  }


  /**
   * @param  name  of the jaas configuration
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   *
   * @throws  Exception  On test failure.
   */
  private void doRolesContextTest(
    final String name,
    final String dn,
    final String user,
    final String role)
    throws Exception
  {
    final TestCallbackHandler callback = new TestCallbackHandler();
    callback.setName(user);

    final LoginContext lc = new LoginContext(name, callback);
    try {
      lc.login();
    } catch (Exception e) {
      AssertJUnit.fail(e.getMessage());
    }

    final Set<LdapRole> roles = lc.getSubject().getPrincipals(LdapRole.class);

    final Iterator<LdapRole> roleIter = roles.iterator();
    final String[] checkRoles = role.split("\\|");
    AssertJUnit.assertEquals(checkRoles.length, roles.size());
    while (roleIter.hasNext()) {
      final LdapRole r = roleIter.next();
      boolean match = false;
      for (String s : checkRoles) {
        if (s.equals(r.getName())) {
          match = true;
        }
      }
      AssertJUnit.assertTrue(match);
    }

    final Set<?> credentials = lc.getSubject().getPrivateCredentials();
    AssertJUnit.assertEquals(0, credentials.size());

    try {
      lc.logout();
    } catch (Exception e) {
      AssertJUnit.fail(e.getMessage());
    }

    AssertJUnit.assertEquals(0, lc.getSubject().getPrincipals().size());
    AssertJUnit.assertEquals(0, lc.getSubject().getPrivateCredentials().size());
  }
}
