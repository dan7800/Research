/*
  $Id: AuthenticatorLoadTest.java 652 2009-09-21 21:21:03Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 652 $
  Updated: $Date: 2009-09-21 14:21:03 -0700 (Mon, 21 Sep 2009) $
*/
package edu.vt.middleware.ldap;

import java.util.HashMap;
import java.util.Map;
import javax.naming.directory.Attributes;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.bean.LdapEntry;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Load test for {@link Authenticator}.
 *
 * @author  Middleware Services
 * @version  $Revision: 652 $
 */
public class AuthenticatorLoadTest
{

  /** Invalid password test data. */
  public static final String INVALID_PASSWD = "not-a-password";

  /** Invalid filter test data. */
  public static final String INVALID_FILTER = "departmentNumber=1111";

  /** Entries for auth tests. */
  private static Map<String, LdapEntry[]> entries =
    new HashMap<String, LdapEntry[]>();

  /**
   * Initialize the map of entries.
   */
  static {
    for (int i = 2; i <= 10; i++) {
      entries.put(String.valueOf(i), new LdapEntry[2]);
    }
  }

  /** Ldap instance for concurrency testing. */
  private Authenticator singleTLSAuth;


  /**
   * Default constructor.
   *
   * @throws  Exception  On test failure.
   */
  public AuthenticatorLoadTest()
    throws Exception
  {
    this.singleTLSAuth = new Authenticator();
    this.singleTLSAuth.loadFromProperties(
      AuthenticatorLoadTest.class.getResourceAsStream(
        "/ldap.tls.load.properties"));

    final LdapTLSSocketFactory sf = new LdapTLSSocketFactory();
    sf.setTrustStoreName("/ed.truststore");
    sf.setTrustStoreType("BKS");
    sf.initialize();
    this.singleTLSAuth.getAuthenticatorConfig().setSslSocketFactory(sf);
  }


  /**
   * @param  ldifFile2  to create.
   * @param  ldifFile3  to create.
   * @param  ldifFile4  to create.
   * @param  ldifFile5  to create.
   * @param  ldifFile6  to create.
   * @param  ldifFile7  to create.
   * @param  ldifFile8  to create.
   * @param  ldifFile9  to create.
   * @param  ldifFile10  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "createEntry2",
      "createEntry3",
      "createEntry4",
      "createEntry5",
      "createEntry6",
      "createEntry7",
      "createEntry8",
      "createEntry9",
      "createEntry10"
    }
  )
  @BeforeClass(groups = {"authloadtest"})
  public void createAuthEntry(
    final String ldifFile2,
    final String ldifFile3,
    final String ldifFile4,
    final String ldifFile5,
    final String ldifFile6,
    final String ldifFile7,
    final String ldifFile8,
    final String ldifFile9,
    final String ldifFile10)
    throws Exception
  {
    entries.get("2")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile2));
    entries.get("3")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile3));
    entries.get("4")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile4));
    entries.get("5")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile5));
    entries.get("6")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile6));
    entries.get("7")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile7));
    entries.get("8")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile8));
    entries.get("9")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile9));
    entries.get("10")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile10));

    Ldap ldap = TestUtil.createSetupLdap();
    for (Map.Entry<String, LdapEntry[]> e : entries.entrySet()) {
      ldap.create(
        e.getValue()[0].getDn(),
        e.getValue()[0].getLdapAttributes().toAttributes());
    }
    ldap.close();

    ldap = TestUtil.createLdap();
    for (Map.Entry<String, LdapEntry[]> e : entries.entrySet()) {
      while (
        !ldap.compare(
            e.getValue()[0].getDn(),
            new SearchFilter(e.getValue()[0].getDn().split(",")[0]))) {
        Thread.sleep(100);
      }
    }
    ldap.close();
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"authloadtest"})
  public void deleteAuthEntry()
    throws Exception
  {
    final Ldap ldap = TestUtil.createSetupLdap();
    ldap.delete(entries.get("2")[0].getDn());
    ldap.delete(entries.get("3")[0].getDn());
    ldap.delete(entries.get("4")[0].getDn());
    ldap.delete(entries.get("5")[0].getDn());
    ldap.delete(entries.get("6")[0].getDn());
    ldap.delete(entries.get("7")[0].getDn());
    ldap.delete(entries.get("8")[0].getDn());
    ldap.delete(entries.get("9")[0].getDn());
    ldap.delete(entries.get("10")[0].getDn());
    ldap.close();
  }


  /**
   * Sample authentication data.
   *
   * @return  user authentication data
   */
  @DataProvider(name = "auth-data")
  public Object[][] createAuthData()
  {
    return
      new Object[][] {
        {
          "jdoe2@vt.edu",
          "password2",
          "departmentNumber={0}",
          "0822",
          "givenName|sn",
          "givenName=John|sn=Doe",
        },
        {
          "jdoe3@vt.edu",
          "password3",
          "departmentNumber={0}",
          "0823",
          "givenName|sn",
          "givenName=Joho|sn=Dof",
        },
        {
          "jdoe4@vt.edu",
          "password4",
          "departmentNumber={0}",
          "0824",
          "givenName|sn",
          "givenName=Johp|sn=Dog",
        },
        {
          "jdoe5@vt.edu",
          "password5",
          "departmentNumber={0}",
          "0825",
          "givenName|sn",
          "givenName=Johq|sn=Doh",
        },
        {
          "jdoe6@vt.edu",
          "password6",
          "departmentNumber={0}",
          "0826",
          "givenName|sn",
          "givenName=Johr|sn=Doi",
        },
        {
          "jdoe7@vt.edu",
          "password7",
          "departmentNumber={0}",
          "0827",
          "givenName|sn",
          "givenName=Johs|sn=Doj",
        },
        {
          "jdoe8@vt.edu",
          "password8",
          "departmentNumber={0}",
          "0828",
          "givenName|sn",
          "givenName=Joht|sn=Dok",
        },
        {
          "jdoe9@vt.edu",
          "password9",
          "departmentNumber={0}",
          "0829",
          "givenName|sn",
          "givenName=Johu|sn=Dol",
        },
        {
          "jdoe10@vt.edu",
          "password10",
          "departmentNumber={0}",
          "0830",
          "givenName|sn",
          "givenName=Johv|sn=Dom",
        },
      };
  }


  /**
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   * @param  filter  to authorize with.
   * @param  filterArgs  to authorize with
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"authloadtest"},
    dataProvider = "auth-data",
    threadPoolSize = 50,
    invocationCount = 1000,
    timeOut = 60000
  )
  public void authenticateAndAuthorize(
    final String user,
    final String credential,
    final String filter,
    final String filterArgs,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    // test auth with return attributes
    final Attributes attrs = this.singleTLSAuth.authenticate(
      user,
      credential,
      new SearchFilter(filter, filterArgs.split("\\|")),
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }
}
/*
  $Id: AuthenticatorLoadTest.java 268 2009-05-28 14:21:40Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 268 $
  Updated: $Date: 2009-05-28 07:21:40 -0700 (Thu, 28 May 2009) $
*/
package edu.vt.middleware.ldap;

import java.util.HashMap;
import java.util.Map;
import javax.naming.directory.Attributes;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.bean.LdapEntry;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Load test for {@link Authenticator}.
 *
 * @author  Middleware Services
 * @version  $Revision: 268 $
 */
public class AuthenticatorLoadTest
{

  /** Invalid password test data. */
  public static final String INVALID_PASSWD = "not-a-password";

  /** Invalid filter test data. */
  public static final String INVALID_FILTER = "departmentNumber=1111";

  /** Entries for auth tests. */
  private static Map<String, LdapEntry[]> entries =
    new HashMap<String, LdapEntry[]>();

  /**
   * Intialize the map of entries.
   */
  static {
    for (int i = 2; i <= 10; i++) {
      entries.put(String.valueOf(i), new LdapEntry[2]);
    }
  }

  /** Ldap instance for concurrency testing. */
  private Authenticator singleTLSAuth;


  /**
   * Default constructor.
   *
   * @throws  Exception  On test failure.
   */
  public AuthenticatorLoadTest()
    throws Exception
  {
    this.singleTLSAuth = new Authenticator();
    this.singleTLSAuth.loadFromProperties("/ldap.tls.load.properties");

    final LdapTLSSocketFactory sf = new LdapTLSSocketFactory();
    sf.setTrustStoreName("/ed.truststore");
    sf.setTrustStoreType("BKS");
    sf.initialize();
    this.singleTLSAuth.getAuthenticatorConfig().setSslSocketFactory(sf);
  }


  /**
   * @param  ldifFile2  to create.
   * @param  ldifFile3  to create.
   * @param  ldifFile4  to create.
   * @param  ldifFile5  to create.
   * @param  ldifFile6  to create.
   * @param  ldifFile7  to create.
   * @param  ldifFile8  to create.
   * @param  ldifFile9  to create.
   * @param  ldifFile10  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "createEntry2",
      "createEntry3",
      "createEntry4",
      "createEntry5",
      "createEntry6",
      "createEntry7",
      "createEntry8",
      "createEntry9",
      "createEntry10"
    }
  )
  @BeforeClass(groups = {"authloadtest"})
  public void createAuthEntry(
    final String ldifFile2,
    final String ldifFile3,
    final String ldifFile4,
    final String ldifFile5,
    final String ldifFile6,
    final String ldifFile7,
    final String ldifFile8,
    final String ldifFile9,
    final String ldifFile10)
    throws Exception
  {
    entries.get("2")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile2));
    entries.get("3")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile3));
    entries.get("4")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile4));
    entries.get("5")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile5));
    entries.get("6")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile6));
    entries.get("7")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile7));
    entries.get("8")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile8));
    entries.get("9")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile9));
    entries.get("10")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile10));

    Ldap ldap = TestUtil.createSetupLdap();
    for (Map.Entry<String, LdapEntry[]> e : entries.entrySet()) {
      ldap.create(
        e.getValue()[0].getDn(),
        e.getValue()[0].getLdapAttributes().toAttributes());
    }
    ldap.close();

    ldap = TestUtil.createLdap();
    for (Map.Entry<String, LdapEntry[]> e : entries.entrySet()) {
      while (
        !ldap.compare(
            e.getValue()[0].getDn(),
            e.getValue()[0].getDn().split(",")[0])) {
        Thread.sleep(100);
      }
    }
    ldap.close();
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"authloadtest"})
  public void deleteAuthEntry()
    throws Exception
  {
    final Ldap ldap = TestUtil.createSetupLdap();
    ldap.delete(entries.get("2")[0].getDn());
    ldap.delete(entries.get("3")[0].getDn());
    ldap.delete(entries.get("4")[0].getDn());
    ldap.delete(entries.get("5")[0].getDn());
    ldap.delete(entries.get("6")[0].getDn());
    ldap.delete(entries.get("7")[0].getDn());
    ldap.delete(entries.get("8")[0].getDn());
    ldap.delete(entries.get("9")[0].getDn());
    ldap.delete(entries.get("10")[0].getDn());
    ldap.close();
  }


  /**
   * Sample authentication data.
   *
   * @return  user authentication data
   */
  @DataProvider(name = "auth-data")
  public Object[][] createAuthData()
  {
    return
      new Object[][] {
        {
          "jdoe2@vt.edu",
          "password2",
          "departmentNumber=0822",
          "givenName|sn",
          "givenName=John|sn=Doe",
        },
        {
          "jdoe3@vt.edu",
          "password3",
          "departmentNumber=0823",
          "givenName|sn",
          "givenName=Joho|sn=Dof",
        },
        {
          "jdoe4@vt.edu",
          "password4",
          "departmentNumber=0824",
          "givenName|sn",
          "givenName=Johp|sn=Dog",
        },
        {
          "jdoe5@vt.edu",
          "password5",
          "departmentNumber=0825",
          "givenName|sn",
          "givenName=Johq|sn=Doh",
        },
        {
          "jdoe6@vt.edu",
          "password6",
          "departmentNumber=0826",
          "givenName|sn",
          "givenName=Johr|sn=Doi",
        },
        {
          "jdoe7@vt.edu",
          "password7",
          "departmentNumber=0827",
          "givenName|sn",
          "givenName=Johs|sn=Doj",
        },
        {
          "jdoe8@vt.edu",
          "password8",
          "departmentNumber=0828",
          "givenName|sn",
          "givenName=Joht|sn=Dok",
        },
        {
          "jdoe9@vt.edu",
          "password9",
          "departmentNumber=0829",
          "givenName|sn",
          "givenName=Johu|sn=Dol",
        },
        {
          "jdoe10@vt.edu",
          "password10",
          "departmentNumber=0830",
          "givenName|sn",
          "givenName=Johv|sn=Dom",
        },
      };
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
  @Test(
    groups = {"authloadtest"},
    dataProvider = "auth-data",
    threadPoolSize = 50,
    invocationCount = 1000,
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
    // test auth with return attributes
    final Attributes attrs = this.singleTLSAuth.authenticate(
      user,
      credential,
      filter,
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }
}
/*
  $Id: AuthenticatorLoadTest.java 326 2009-07-15 20:58:45Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 326 $
  Updated: $Date: 2009-07-15 13:58:45 -0700 (Wed, 15 Jul 2009) $
*/
package edu.vt.middleware.ldap;

import java.util.HashMap;
import java.util.Map;
import javax.naming.directory.Attributes;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.bean.LdapEntry;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Load test for {@link Authenticator}.
 *
 * @author  Middleware Services
 * @version  $Revision: 326 $
 */
public class AuthenticatorLoadTest
{

  /** Invalid password test data. */
  public static final String INVALID_PASSWD = "not-a-password";

  /** Invalid filter test data. */
  public static final String INVALID_FILTER = "departmentNumber=1111";

  /** Entries for auth tests. */
  private static Map<String, LdapEntry[]> entries =
    new HashMap<String, LdapEntry[]>();

  /**
   * Intialize the map of entries.
   */
  static {
    for (int i = 2; i <= 10; i++) {
      entries.put(String.valueOf(i), new LdapEntry[2]);
    }
  }

  /** Ldap instance for concurrency testing. */
  private Authenticator singleTLSAuth;


  /**
   * Default constructor.
   *
   * @throws  Exception  On test failure.
   */
  public AuthenticatorLoadTest()
    throws Exception
  {
    this.singleTLSAuth = new Authenticator();
    this.singleTLSAuth.loadFromProperties(
      AuthenticatorLoadTest.class.getResourceAsStream(
        "/ldap.tls.load.properties"));

    final LdapTLSSocketFactory sf = new LdapTLSSocketFactory();
    sf.setTrustStoreName("/ed.truststore");
    sf.setTrustStoreType("BKS");
    sf.initialize();
    this.singleTLSAuth.getAuthenticatorConfig().setSslSocketFactory(sf);
  }


  /**
   * @param  ldifFile2  to create.
   * @param  ldifFile3  to create.
   * @param  ldifFile4  to create.
   * @param  ldifFile5  to create.
   * @param  ldifFile6  to create.
   * @param  ldifFile7  to create.
   * @param  ldifFile8  to create.
   * @param  ldifFile9  to create.
   * @param  ldifFile10  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "createEntry2",
      "createEntry3",
      "createEntry4",
      "createEntry5",
      "createEntry6",
      "createEntry7",
      "createEntry8",
      "createEntry9",
      "createEntry10"
    }
  )
  @BeforeClass(groups = {"authloadtest"})
  public void createAuthEntry(
    final String ldifFile2,
    final String ldifFile3,
    final String ldifFile4,
    final String ldifFile5,
    final String ldifFile6,
    final String ldifFile7,
    final String ldifFile8,
    final String ldifFile9,
    final String ldifFile10)
    throws Exception
  {
    entries.get("2")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile2));
    entries.get("3")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile3));
    entries.get("4")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile4));
    entries.get("5")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile5));
    entries.get("6")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile6));
    entries.get("7")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile7));
    entries.get("8")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile8));
    entries.get("9")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile9));
    entries.get("10")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile10));

    Ldap ldap = TestUtil.createSetupLdap();
    for (Map.Entry<String, LdapEntry[]> e : entries.entrySet()) {
      ldap.create(
        e.getValue()[0].getDn(),
        e.getValue()[0].getLdapAttributes().toAttributes());
    }
    ldap.close();

    ldap = TestUtil.createLdap();
    for (Map.Entry<String, LdapEntry[]> e : entries.entrySet()) {
      while (
        !ldap.compare(
            e.getValue()[0].getDn(),
            e.getValue()[0].getDn().split(",")[0])) {
        Thread.sleep(100);
      }
    }
    ldap.close();
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"authloadtest"})
  public void deleteAuthEntry()
    throws Exception
  {
    final Ldap ldap = TestUtil.createSetupLdap();
    ldap.delete(entries.get("2")[0].getDn());
    ldap.delete(entries.get("3")[0].getDn());
    ldap.delete(entries.get("4")[0].getDn());
    ldap.delete(entries.get("5")[0].getDn());
    ldap.delete(entries.get("6")[0].getDn());
    ldap.delete(entries.get("7")[0].getDn());
    ldap.delete(entries.get("8")[0].getDn());
    ldap.delete(entries.get("9")[0].getDn());
    ldap.delete(entries.get("10")[0].getDn());
    ldap.close();
  }


  /**
   * Sample authentication data.
   *
   * @return  user authentication data
   */
  @DataProvider(name = "auth-data")
  public Object[][] createAuthData()
  {
    return
      new Object[][] {
        {
          "jdoe2@vt.edu",
          "password2",
          "departmentNumber=0822",
          "givenName|sn",
          "givenName=John|sn=Doe",
        },
        {
          "jdoe3@vt.edu",
          "password3",
          "departmentNumber=0823",
          "givenName|sn",
          "givenName=Joho|sn=Dof",
        },
        {
          "jdoe4@vt.edu",
          "password4",
          "departmentNumber=0824",
          "givenName|sn",
          "givenName=Johp|sn=Dog",
        },
        {
          "jdoe5@vt.edu",
          "password5",
          "departmentNumber=0825",
          "givenName|sn",
          "givenName=Johq|sn=Doh",
        },
        {
          "jdoe6@vt.edu",
          "password6",
          "departmentNumber=0826",
          "givenName|sn",
          "givenName=Johr|sn=Doi",
        },
        {
          "jdoe7@vt.edu",
          "password7",
          "departmentNumber=0827",
          "givenName|sn",
          "givenName=Johs|sn=Doj",
        },
        {
          "jdoe8@vt.edu",
          "password8",
          "departmentNumber=0828",
          "givenName|sn",
          "givenName=Joht|sn=Dok",
        },
        {
          "jdoe9@vt.edu",
          "password9",
          "departmentNumber=0829",
          "givenName|sn",
          "givenName=Johu|sn=Dol",
        },
        {
          "jdoe10@vt.edu",
          "password10",
          "departmentNumber=0830",
          "givenName|sn",
          "givenName=Johv|sn=Dom",
        },
      };
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
  @Test(
    groups = {"authloadtest"},
    dataProvider = "auth-data",
    threadPoolSize = 50,
    invocationCount = 1000,
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
    // test auth with return attributes
    final Attributes attrs = this.singleTLSAuth.authenticate(
      user,
      credential,
      filter,
      returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }
}
