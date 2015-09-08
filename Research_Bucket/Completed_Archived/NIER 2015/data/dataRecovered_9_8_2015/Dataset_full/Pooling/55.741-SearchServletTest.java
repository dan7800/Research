/*
  $Id: SearchServletTest.java 639 2009-09-18 17:55:42Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 639 $
  Updated: $Date: 2009-09-18 10:55:42 -0700 (Fri, 18 Sep 2009) $
*/
package edu.vt.middleware.ldap.servlets;

import java.io.File;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.SearchFilter;
import edu.vt.middleware.ldap.TestUtil;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.dsml.DsmlResult;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SearchServlet}.
 *
 * @author  Middleware Services
 * @version  $Revision: 639 $
 */
public class SearchServletTest
{

  /** Entry created for tests. */
  private static LdapEntry testLdapEntry;

  /** To test servlets with. */
  private ServletRunner ldifServletRunner;

  /** To test servlets with. */
  private ServletRunner dsmlServletRunner;


  /**
   * @param  ldifFile  to create.
   * @param  webXml  web.xml for queries
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "createEntry8", "webXml" })
  @BeforeClass(groups = {"servlettest"})
  public void createLdapEntry(final String ldifFile, final String webXml)
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

    this.ldifServletRunner = new ServletRunner(new File(webXml));
    this.dsmlServletRunner = new ServletRunner(new File(webXml));
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"servlettest"})
  public void deleteLdapEntry()
    throws Exception
  {
    final Ldap ldap = TestUtil.createSetupLdap();
    ldap.delete(testLdapEntry.getDn());
    ldap.close();
  }


  /**
   * @param  query  to search for.
   * @param  attrs  attributes to return from search
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "ldifSearchServletQuery",
      "ldifSearchServletAttrs",
      "ldifSearchServletLdif"
    }
  )
  @Test(groups = {"servlettest"})
  public void ldifSearchServlet(
    final String query,
    final String attrs,
    final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtil.readFileIntoString(ldifFile);
    final LdapEntry entry = TestUtil.convertLdifToEntry(ldif);

    final ServletUnitClient sc = this.ldifServletRunner.newClient();
    final WebRequest request = new PostMethodWebRequest(
      "http://servlets.ldap.middleware.vt.edu/LdifSearch");
    request.setParameter("query", query);
    request.setParameter("attrs", attrs.split("\\|"));

    final WebResponse response = sc.getResponse(request);

    AssertJUnit.assertNotNull(response);
    AssertJUnit.assertEquals("text/plain", response.getContentType());

    final LdapEntry result = TestUtil.convertLdifToEntry(response.getText());
    AssertJUnit.assertEquals(entry, result);
  }


  /**
   * @param  query  to search for.
   * @param  attrs  attributes to return from search
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "dsmlSearchServletQuery",
      "dsmlSearchServletAttrs",
      "dsmlSearchServletLdif"
    }
  )
  @Test(groups = {"servlettest"})
  public void dsmlSearchServlet(
    final String query,
    final String attrs,
    final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtil.readFileIntoString(ldifFile);
    final DsmlResult entry = new DsmlResult(TestUtil.convertLdifToEntry(ldif));

    final ServletUnitClient sc = this.dsmlServletRunner.newClient();
    // test basic dsml query
    WebRequest request = new PostMethodWebRequest(
      "http://servlets.ldap.middleware.vt.edu/DsmlSearch");
    request.setParameter("query", query);
    request.setParameter("attrs", attrs.split("\\|"));

    WebResponse response = sc.getResponse(request);

    AssertJUnit.assertNotNull(response);
    AssertJUnit.assertEquals("text/xml", response.getContentType());
    AssertJUnit.assertEquals(entry.toDsmlv1(), response.getText());

    // test plain text
    request = new PostMethodWebRequest(
      "http://servlets.ldap.middleware.vt.edu/DsmlSearch");
    request.setParameter("content-type", "text");
    request.setParameter("query", query);
    request.setParameter("attrs", attrs.split("\\|"));
    response = sc.getResponse(request);

    AssertJUnit.assertNotNull(response);
    AssertJUnit.assertEquals("text/plain", response.getContentType());
    AssertJUnit.assertEquals(entry.toDsmlv1(), response.getText());

    // test dsmlv2
    request = new PostMethodWebRequest(
      "http://servlets.ldap.middleware.vt.edu/DsmlSearch");
    request.setParameter("dsml-version", "2");
    request.setParameter("query", query);
    request.setParameter("attrs", attrs.split("\\|"));
    response = sc.getResponse(request);

    AssertJUnit.assertNotNull(response);
    AssertJUnit.assertEquals("text/xml", response.getContentType());
    AssertJUnit.assertEquals(entry.toDsmlv2(), response.getText());
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"servlettest"},
    dependsOnMethods = {"ldifSearchServlet", "dsmlSearchServlet"}
  )
  public void prunePools()
    throws Exception
  {
    Thread.sleep(10000);
  }
}
/*
  $Id: SearchServletTest.java 268 2009-05-28 14:21:40Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 268 $
  Updated: $Date: 2009-05-28 07:21:40 -0700 (Thu, 28 May 2009) $
*/
package edu.vt.middleware.ldap.servlets;

import java.io.File;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.TestUtil;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.dsml.DsmlResult;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SearchServlet}.
 *
 * @author  Middleware Services
 * @version  $Revision: 268 $
 */
public class SearchServletTest
{

  /** Entry created for tests. */
  private static LdapEntry testLdapEntry;

  /** To test servlets with. */
  private ServletRunner ldifServletRunner;

  /** To test servlets with. */
  private ServletRunner dsmlServletRunner;


  /**
   * @param  ldifFile  to create.
   * @param  webXml  web.xml for queries
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "createEntry8", "webXml" })
  @BeforeClass(groups = {"servlettest"})
  public void createLdapEntry(final String ldifFile, final String webXml)
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

    this.ldifServletRunner = new ServletRunner(new File(webXml));
    this.dsmlServletRunner = new ServletRunner(new File(webXml));
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"servlettest"})
  public void deleteLdapEntry()
    throws Exception
  {
    final Ldap ldap = TestUtil.createSetupLdap();
    ldap.delete(testLdapEntry.getDn());
    ldap.close();
  }


  /**
   * @param  query  to search for.
   * @param  attrs  attributes to return from search
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "ldifSearchServletQuery",
      "ldifSearchServletAttrs",
      "ldifSearchServletLdif"
    }
  )
  @Test(groups = {"servlettest"})
  public void ldifSearchServlet(
    final String query,
    final String attrs,
    final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtil.readFileIntoString(ldifFile);
    final LdapEntry entry = TestUtil.convertLdifToEntry(ldif);

    final ServletUnitClient sc = this.ldifServletRunner.newClient();
    final WebRequest request = new PostMethodWebRequest(
      "http://servlets.ldap.middleware.vt.edu/LdifSearch");
    request.setParameter("query", query);
    request.setParameter("attrs", attrs.split("\\|"));

    final WebResponse response = sc.getResponse(request);

    AssertJUnit.assertNotNull(response);
    AssertJUnit.assertEquals("text/plain", response.getContentType());

    final LdapEntry result = TestUtil.convertLdifToEntry(response.getText());
    AssertJUnit.assertEquals(entry, result);
  }


  /**
   * @param  query  to search for.
   * @param  attrs  attributes to return from search
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "dsmlSearchServletQuery",
      "dsmlSearchServletAttrs",
      "dsmlSearchServletLdif"
    }
  )
  @Test(groups = {"servlettest"})
  public void dsmlSearchServlet(
    final String query,
    final String attrs,
    final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtil.readFileIntoString(ldifFile);
    final DsmlResult entry = new DsmlResult(TestUtil.convertLdifToEntry(ldif));

    final ServletUnitClient sc = this.dsmlServletRunner.newClient();
    // test basic dsml query
    WebRequest request = new PostMethodWebRequest(
      "http://servlets.ldap.middleware.vt.edu/DsmlSearch");
    request.setParameter("query", query);
    request.setParameter("attrs", attrs.split("\\|"));

    WebResponse response = sc.getResponse(request);

    AssertJUnit.assertNotNull(response);
    AssertJUnit.assertEquals("text/xml", response.getContentType());
    AssertJUnit.assertEquals(entry.toDsmlv1(), response.getText());

    // test plain text
    request = new PostMethodWebRequest(
      "http://servlets.ldap.middleware.vt.edu/DsmlSearch");
    request.setParameter("content-type", "text");
    request.setParameter("query", query);
    request.setParameter("attrs", attrs.split("\\|"));
    response = sc.getResponse(request);

    AssertJUnit.assertNotNull(response);
    AssertJUnit.assertEquals("text/plain", response.getContentType());
    AssertJUnit.assertEquals(entry.toDsmlv1(), response.getText());

    // test dsmlv2
    request = new PostMethodWebRequest(
      "http://servlets.ldap.middleware.vt.edu/DsmlSearch");
    request.setParameter("dsml-version", "2");
    request.setParameter("query", query);
    request.setParameter("attrs", attrs.split("\\|"));
    response = sc.getResponse(request);

    AssertJUnit.assertNotNull(response);
    AssertJUnit.assertEquals("text/xml", response.getContentType());
    AssertJUnit.assertEquals(entry.toDsmlv2(), response.getText());
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"servlettest"},
    dependsOnMethods = {"ldifSearchServlet", "dsmlSearchServlet"}
  )
  public void prunePools()
    throws Exception
  {
    Thread.sleep(10000);
  }
}
/*
  $Id: SearchServletTest.java 268 2009-05-28 14:21:40Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 268 $
  Updated: $Date: 2009-05-28 07:21:40 -0700 (Thu, 28 May 2009) $
*/
package edu.vt.middleware.ldap.servlets;

import java.io.File;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.TestUtil;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.dsml.DsmlResult;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SearchServlet}.
 *
 * @author  Middleware Services
 * @version  $Revision: 268 $
 */
public class SearchServletTest
{

  /** Entry created for tests. */
  private static LdapEntry testLdapEntry;

  /** To test servlets with. */
  private ServletRunner ldifServletRunner;

  /** To test servlets with. */
  private ServletRunner dsmlServletRunner;


  /**
   * @param  ldifFile  to create.
   * @param  webXml  web.xml for queries
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "createEntry8", "webXml" })
  @BeforeClass(groups = {"servlettest"})
  public void createLdapEntry(final String ldifFile, final String webXml)
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

    this.ldifServletRunner = new ServletRunner(new File(webXml));
    this.dsmlServletRunner = new ServletRunner(new File(webXml));
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"servlettest"})
  public void deleteLdapEntry()
    throws Exception
  {
    final Ldap ldap = TestUtil.createSetupLdap();
    ldap.delete(testLdapEntry.getDn());
    ldap.close();
  }


  /**
   * @param  query  to search for.
   * @param  attrs  attributes to return from search
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "ldifSearchServletQuery",
      "ldifSearchServletAttrs",
      "ldifSearchServletLdif"
    }
  )
  @Test(groups = {"servlettest"})
  public void ldifSearchServlet(
    final String query,
    final String attrs,
    final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtil.readFileIntoString(ldifFile);
    final LdapEntry entry = TestUtil.convertLdifToEntry(ldif);

    final ServletUnitClient sc = this.ldifServletRunner.newClient();
    final WebRequest request = new PostMethodWebRequest(
      "http://servlets.ldap.middleware.vt.edu/LdifSearch");
    request.setParameter("query", query);
    request.setParameter("attrs", attrs.split("\\|"));

    final WebResponse response = sc.getResponse(request);

    AssertJUnit.assertNotNull(response);
    AssertJUnit.assertEquals("text/plain", response.getContentType());

    final LdapEntry result = TestUtil.convertLdifToEntry(response.getText());
    AssertJUnit.assertEquals(entry, result);
  }


  /**
   * @param  query  to search for.
   * @param  attrs  attributes to return from search
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "dsmlSearchServletQuery",
      "dsmlSearchServletAttrs",
      "dsmlSearchServletLdif"
    }
  )
  @Test(groups = {"servlettest"})
  public void dsmlSearchServlet(
    final String query,
    final String attrs,
    final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtil.readFileIntoString(ldifFile);
    final DsmlResult entry = new DsmlResult(TestUtil.convertLdifToEntry(ldif));

    final ServletUnitClient sc = this.dsmlServletRunner.newClient();
    // test basic dsml query
    WebRequest request = new PostMethodWebRequest(
      "http://servlets.ldap.middleware.vt.edu/DsmlSearch");
    request.setParameter("query", query);
    request.setParameter("attrs", attrs.split("\\|"));

    WebResponse response = sc.getResponse(request);

    AssertJUnit.assertNotNull(response);
    AssertJUnit.assertEquals("text/xml", response.getContentType());
    AssertJUnit.assertEquals(entry.toDsmlv1(), response.getText());

    // test plain text
    request = new PostMethodWebRequest(
      "http://servlets.ldap.middleware.vt.edu/DsmlSearch");
    request.setParameter("content-type", "text");
    request.setParameter("query", query);
    request.setParameter("attrs", attrs.split("\\|"));
    response = sc.getResponse(request);

    AssertJUnit.assertNotNull(response);
    AssertJUnit.assertEquals("text/plain", response.getContentType());
    AssertJUnit.assertEquals(entry.toDsmlv1(), response.getText());

    // test dsmlv2
    request = new PostMethodWebRequest(
      "http://servlets.ldap.middleware.vt.edu/DsmlSearch");
    request.setParameter("dsml-version", "2");
    request.setParameter("query", query);
    request.setParameter("attrs", attrs.split("\\|"));
    response = sc.getResponse(request);

    AssertJUnit.assertNotNull(response);
    AssertJUnit.assertEquals("text/xml", response.getContentType());
    AssertJUnit.assertEquals(entry.toDsmlv2(), response.getText());
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"servlettest"},
    dependsOnMethods = {"ldifSearchServlet", "dsmlSearchServlet"}
  )
  public void prunePools()
    throws Exception
  {
    Thread.sleep(10000);
  }
}
