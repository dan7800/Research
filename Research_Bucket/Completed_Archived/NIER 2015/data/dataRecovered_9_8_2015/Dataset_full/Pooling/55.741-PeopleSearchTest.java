/*
  $Id: PeopleSearchTest.java 296 2009-07-01 15:12:00Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 296 $
  Updated: $Date: 2009-07-01 08:12:00 -0700 (Wed, 01 Jul 2009) $
*/
package edu.vt.middleware.ldap.search;

import java.util.HashMap;
import java.util.Map;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.bean.LdapResult;
import edu.vt.middleware.ldap.search.PeopleSearch.OutputFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link PeopleSearch}.
 *
 * @author  Middleware Services
 * @version  $Revision: 296 $
 */
public class PeopleSearchTest
{

  /** Entries for pool tests. */
  private static Map<String, LdapEntry[]> entries =
    new HashMap<String, LdapEntry[]>();

  /**
   * Initialize the map of entries.
   */
  static {
    for (int i = 2; i <= 9; i++) {
      entries.put(String.valueOf(i), new LdapEntry[2]);
    }
  }

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());

  /** PeopleSearch to test. */
  private PeopleSearch search = new PeopleSearch();

  /** PeopleSearch to test. */
  private PeopleSearch saslSearch = new PeopleSearch();


  /**
   * Default constructor.
   *
   * @throws  Exception  On test failure.
   */
  public PeopleSearchTest()
    throws Exception
  {
    // setup the people search object
    this.search.setProxySaslAuthorization(false);

    final SearchExecutor s1 = new SearchExecutor(1);
    s1.setAdditive(true);
    s1.getQueryTemplates().put(
      new Integer(1),
      "(|(givenName=@@@QUERY_1@@@)(sn=@@@QUERY_1@@@))");
    s1.getQueryTemplates().put(
      new Integer(2),
      "(|(givenName=@@@QUERY_1@@@*)(sn=@@@QUERY_1@@@*))");
    s1.getQueryTemplates().put(
      new Integer(3),
      "(|(givenName=*@@@QUERY_1@@@*)(sn=*@@@QUERY_1@@@*))");
    s1.getQueryTemplates().put(
      new Integer(4),
      "(|(departmentNumber=@@@QUERY_1@@@)(mail=@@@QUERY_1@@@))");
    s1.getQueryTemplates().put(
      new Integer(5),
      "(|(departmentNumber=@@@QUERY_1@@@*)(mail=@@@QUERY_1@@@*))");
    s1.getQueryTemplates().put(
      new Integer(6),
      "(|(departmentNumber=*@@@QUERY_1@@@*)(mail=*@@@QUERY_1@@@*))");
    this.search.getSearchExecutors().put(new Integer(1), s1);

    final LdapPoolManager lpm1 = new LdapPoolManager();
    lpm1.setLdapProperties("/ldap.properties");
    this.search.setLdapPoolManager(lpm1);

    // setup the sasl people search object
    this.saslSearch.setProxySaslAuthorization(true);

    final SearchExecutor s2 = new SearchExecutor(1);
    s2.setAdditive(true);
    s2.getQueryTemplates().put(
      new Integer(1),
      "(|(givenName=@@@QUERY_1@@@)(sn=@@@QUERY_1@@@))");
    s2.getQueryTemplates().put(
      new Integer(2),
      "(|(givenName=@@@QUERY_1@@@*)(sn=@@@QUERY_1@@@*))");
    s2.getQueryTemplates().put(
      new Integer(3),
      "(|(givenName=*@@@QUERY_1@@@*)(sn=*@@@QUERY_1@@@*))");
    s2.getQueryTemplates().put(
      new Integer(4),
      "(|(departmentNumber=@@@QUERY_1@@@)(mail=@@@QUERY_1@@@))");
    s2.getQueryTemplates().put(
      new Integer(5),
      "(|(departmentNumber=@@@QUERY_1@@@*)(mail=@@@QUERY_1@@@*))");
    s2.getQueryTemplates().put(
      new Integer(6),
      "(|(departmentNumber=*@@@QUERY_1@@@*)(mail=*@@@QUERY_1@@@*))");
    this.saslSearch.getSearchExecutors().put(new Integer(1), s2);

    final LdapPoolManager lpm2 = new LdapPoolManager();
    lpm2.setLdapProperties("/ldap.digest-md5.properties");
    lpm2.setLdapPoolProperties("/ldap.pool.properties");
    this.saslSearch.setLdapPoolManager(lpm2);
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
      "createEntry9"
    }
  )
  @BeforeClass(groups = {"searchtest"})
  public void createEntry(
    final String ldifFile2,
    final String ldifFile3,
    final String ldifFile4,
    final String ldifFile5,
    final String ldifFile6,
    final String ldifFile7,
    final String ldifFile8,
    final String ldifFile9)
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


  /**
   * @param  ldifFile2  to load.
   * @param  ldifFile3  to load.
   * @param  ldifFile4  to load.
   * @param  ldifFile5  to load.
   * @param  ldifFile6  to load.
   * @param  ldifFile7  to load.
   * @param  ldifFile8  to load.
   * @param  ldifFile9  to load.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "searchResults2",
      "searchResults3",
      "searchResults4",
      "searchResults5",
      "searchResults6",
      "searchResults7",
      "searchResults8",
      "searchResults9"
    }
  )
  @BeforeClass(groups = {"searchtest"})
  public void loadSearchResults(
    final String ldifFile2,
    final String ldifFile3,
    final String ldifFile4,
    final String ldifFile5,
    final String ldifFile6,
    final String ldifFile7,
    final String ldifFile8,
    final String ldifFile9)
    throws Exception
  {
    entries.get("2")[1] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile2));
    entries.get("3")[1] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile3));
    entries.get("4")[1] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile4));
    entries.get("5")[1] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile5));
    entries.get("6")[1] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile6));
    entries.get("7")[1] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile7));
    entries.get("8")[1] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile8));
    entries.get("9")[1] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile9));
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"searchtest"})
  public void deletePoolEntry()
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
    ldap.close();
  }


  /**
   * Sample user data.
   *
   * @return  user data
   */
  @DataProvider(name = "search-data")
  public Object[][] createTestData()
  {
    final LdapResult all1600 = new LdapResult();
    all1600.addEntry(entries.get("5")[1]);
    all1600.addEntry(entries.get("6")[1]);
    all1600.addEntry(entries.get("7")[1]);
    all1600.addEntry(entries.get("8")[1]);
    all1600.addEntry(entries.get("9")[1]);

    final LdapResult allJames = new LdapResult();
    allJames.addEntry(entries.get("6")[1]);
    allJames.addEntry(entries.get("7")[1]);
    allJames.addEntry(entries.get("8")[1]);

    final LdapResult allRoosevelt = new LdapResult();
    allRoosevelt.addEntry(entries.get("5")[1]);
    allRoosevelt.addEntry(entries.get("9")[1]);

    return
      new Object[][] {
        {
          "1600",
          "departmentNumber|givenName|sn",
          all1600,
        },
        {
          "james",
          "departmentNumber|givenName|sn",
          allJames,
        },
        {
          "roosevelt",
          "departmentNumber|givenName|sn",
          allRoosevelt,
        },
        {
          "fdr",
          "departmentNumber|givenName|sn",
          new LdapResult(entries.get("5")[1]),
        },
        {
          "jm",
          "departmentNumber|givenName|sn",
          new LdapResult(entries.get("6")[1]),
        },
        {
          "jag",
          "departmentNumber|givenName|sn",
          new LdapResult(entries.get("7")[1]),
        },
        {
          "jec",
          "departmentNumber|givenName|sn",
          new LdapResult(entries.get("8")[1]),
        },
        {
          "tdr",
          "departmentNumber|givenName|sn",
          new LdapResult(entries.get("9")[1]),
        },
      };
  }


  /**
   * @param  query  to search with.
   * @param  returnAttrs  to search for.
   * @param  result  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"searchtest"},
    dataProvider = "search-data",
    threadPoolSize = 3,
    invocationCount = 50,
    timeOut = 60000
  )
  public void search(
    final String query,
    final String returnAttrs,
    final LdapResult result)
    throws Exception
  {
    final Query q = new Query();
    q.setRawQuery(query);
    q.setQueryAttributes(returnAttrs.split("\\|"));

    final String searchResult = this.search.searchToString(
      q,
      OutputFormat.LDIF);
    AssertJUnit.assertEquals(
      result,
      TestUtil.convertLdifToResult(searchResult));
  }


  /**
   * Sample user data.
   *
   * @return  user data
   */
  @DataProvider(name = "sasl-search-data")
  public Object[][] createSaslTestData()
  {
    return
      new Object[][] {
        {
          "Harry",
          "departmentNumber|givenName|sn|mail",
          "dn:uid=101,ou=test,dc=vt,dc=edu",
          new LdapResult(entries.get("2")[1]),
        },
        {
          "dwight",
          "departmentNumber|givenName|sn|mail",
          "dn:uid=102,ou=test,dc=vt,dc=edu",
          new LdapResult(entries.get("3")[1]),
        },
        {
          "john",
          "departmentNumber|givenName|sn|mail",
          "dn:uid=103,ou=test,dc=vt,dc=edu",
          new LdapResult(entries.get("4")[1]),
        },
      };
  }


  /**
   * @param  query  to search with.
   * @param  returnAttrs  to search for.
   * @param  saslAuthz  ID to authorize as.
   * @param  result  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"searchtest"},
    dataProvider = "sasl-search-data",
    threadPoolSize = 3,
    invocationCount = 50,
    timeOut = 60000
  )
  public void saslSearch(
    final String query,
    final String returnAttrs,
    final String saslAuthz,
    final LdapResult result)
    throws Exception
  {
    final Query q = new Query();
    q.setRawQuery(query);
    q.setQueryAttributes(returnAttrs.split("\\|"));
    q.setSaslAuthorizationId(saslAuthz);

    final String searchResult = this.saslSearch.searchToString(
      q,
      OutputFormat.LDIF);
    AssertJUnit.assertEquals(
      result,
      TestUtil.convertLdifToResult(searchResult));
  }
}
