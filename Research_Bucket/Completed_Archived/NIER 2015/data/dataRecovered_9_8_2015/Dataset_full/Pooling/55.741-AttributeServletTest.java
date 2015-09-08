/*
  $Id: AttributeServletTest.java 639 2009-09-18 17:55:42Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 639 $
  Updated: $Date: 2009-09-18 10:55:42 -0700 (Fri, 18 Sep 2009) $
*/
package edu.vt.middleware.ldap.servlets;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapUtil;
import edu.vt.middleware.ldap.SearchFilter;
import edu.vt.middleware.ldap.TestUtil;
import edu.vt.middleware.ldap.bean.LdapEntry;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link AttributeServlet}.
 *
 * @author  Middleware Services
 * @version  $Revision: 639 $
 */
public class AttributeServletTest
{

  /** Entry created for tests. */
  private static LdapEntry testLdapEntry;

  /** To test servlets with. */
  private ServletRunner servletRunner;


  /**
   * @param  ldifFile  to create.
   * @param  webXml  web.xml for queries
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "createEntry9", "webXml" })
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

    this.servletRunner = new ServletRunner(new File(webXml));
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
   * @param  attr  attribute to return from search
   * @param  attributeValue  to compare
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "attributeServletQuery",
      "attributeServletAttr",
      "attributeServletValue"
    }
  )
  @Test(groups = {"servlettest"})
  public void attributeServlet(
    final String query,
    final String attr,
    final String attributeValue)
    throws Exception
  {
    final ServletUnitClient sc = this.servletRunner.newClient();
    final WebRequest request = new PostMethodWebRequest(
      "http://servlets.ldap.middleware.vt.edu/AttributeSearch");
    request.setParameter("query", query);
    request.setParameter("attr", attr);
    request.setParameter("content-type", "octet");

    final WebResponse response = sc.getResponse(request);

    AssertJUnit.assertNotNull(response);
    AssertJUnit.assertEquals(
      "application/octet-stream",
      response.getContentType());
    AssertJUnit.assertEquals(
      "attachment; filename=\"" + attr + ".bin\"",
      response.getHeaderField("Content-Disposition"));

    final InputStream input = response.getInputStream();
    final ByteArrayOutputStream data = new ByteArrayOutputStream();
    if (input != null) {
      try {
        final byte[] buffer = new byte[128];
        int length;
        while ((length = input.read(buffer)) != -1) {
          data.write(buffer, 0, length);
        }
      } finally {
        data.close();
      }
    }
    AssertJUnit.assertEquals(
      attributeValue,
      LdapUtil.base64Encode(data.toByteArray()));
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"servlettest"},
    dependsOnMethods = {"attributeServlet"}
  )
  public void prunePools()
    throws Exception
  {
    Thread.sleep(10000);
  }
}
/*
  $Id: AttributeServletTest.java 268 2009-05-28 14:21:40Z dfisher $

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
import edu.vt.middleware.ldap.LdapUtil;
import edu.vt.middleware.ldap.TestUtil;
import edu.vt.middleware.ldap.bean.LdapEntry;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link AttributeServlet}.
 *
 * @author  Middleware Services
 * @version  $Revision: 268 $
 */
public class AttributeServletTest
{

  /** Entry created for tests. */
  private static LdapEntry testLdapEntry;

  /** To test servlets with. */
  private ServletRunner servletRunner;


  /**
   * @param  ldifFile  to create.
   * @param  webXml  web.xml for queries
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "createEntry9", "webXml" })
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

    this.servletRunner = new ServletRunner(new File(webXml));
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
   * @param  attr  attribute to return from search
   * @param  attributeValue  to compare
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "attributeServletQuery",
      "attributeServletAttr",
      "attributeServletValue"
    }
  )
  @Test(groups = {"servlettest"})
  public void attributeServlet(
    final String query,
    final String attr,
    final String attributeValue)
    throws Exception
  {
    final ServletUnitClient sc = this.servletRunner.newClient();
    final WebRequest request = new PostMethodWebRequest(
      "http://servlets.ldap.middleware.vt.edu/AttributeSearch");
    request.setParameter("query", query);
    request.setParameter("attr", attr);
    request.setParameter("content-type", "octet");

    final WebResponse response = sc.getResponse(request);

    AssertJUnit.assertNotNull(response);
    AssertJUnit.assertEquals(
      "application/octet-stream",
      response.getContentType());
    AssertJUnit.assertEquals(
      "attachment; filename=\"" + attr + ".bin\"",
      response.getHeaderField("Content-Disposition"));
    AssertJUnit.assertEquals(
      attributeValue,
      LdapUtil.base64Encode(response.getText().getBytes()));
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"servlettest"},
    dependsOnMethods = {"attributeServlet"}
  )
  public void prunePools()
    throws Exception
  {
    Thread.sleep(10000);
  }
}
/*
  $Id: AttributeServletTest.java 298 2009-07-02 19:33:14Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 298 $
  Updated: $Date: 2009-07-02 12:33:14 -0700 (Thu, 02 Jul 2009) $
*/
package edu.vt.middleware.ldap.servlets;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapUtil;
import edu.vt.middleware.ldap.TestUtil;
import edu.vt.middleware.ldap.bean.LdapEntry;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link AttributeServlet}.
 *
 * @author  Middleware Services
 * @version  $Revision: 298 $
 */
public class AttributeServletTest
{

  /** Entry created for tests. */
  private static LdapEntry testLdapEntry;

  /** To test servlets with. */
  private ServletRunner servletRunner;


  /**
   * @param  ldifFile  to create.
   * @param  webXml  web.xml for queries
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "createEntry9", "webXml" })
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

    this.servletRunner = new ServletRunner(new File(webXml));
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
   * @param  attr  attribute to return from search
   * @param  attributeValue  to compare
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "attributeServletQuery",
      "attributeServletAttr",
      "attributeServletValue"
    }
  )
  @Test(groups = {"servlettest"})
  public void attributeServlet(
    final String query,
    final String attr,
    final String attributeValue)
    throws Exception
  {
    final ServletUnitClient sc = this.servletRunner.newClient();
    final WebRequest request = new PostMethodWebRequest(
      "http://servlets.ldap.middleware.vt.edu/AttributeSearch");
    request.setParameter("query", query);
    request.setParameter("attr", attr);
    request.setParameter("content-type", "octet");

    final WebResponse response = sc.getResponse(request);

    AssertJUnit.assertNotNull(response);
    AssertJUnit.assertEquals(
      "application/octet-stream",
      response.getContentType());
    AssertJUnit.assertEquals(
      "attachment; filename=\"" + attr + ".bin\"",
      response.getHeaderField("Content-Disposition"));

    final InputStream input = response.getInputStream();
    final ByteArrayOutputStream data = new ByteArrayOutputStream();
    if (input != null) {
      try {
        final byte[] buffer = new byte[128];
        int length;
        while ((length = input.read(buffer)) != -1) {
          data.write(buffer, 0, length);
        }
      } finally {
        data.close();
      }
    }
    AssertJUnit.assertEquals(
      attributeValue,
      LdapUtil.base64Encode(data.toByteArray()));
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"servlettest"},
    dependsOnMethods = {"attributeServlet"}
  )
  public void prunePools()
    throws Exception
  {
    Thread.sleep(10000);
  }
}
