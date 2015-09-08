/*
  $Id: LdapPoolTest.java 639 2009-09-18 17:55:42Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 639 $
  Updated: $Date: 2009-09-18 10:55:42 -0700 (Fri, 18 Sep 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.naming.directory.SearchResult;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;
import edu.vt.middleware.ldap.SearchFilter;
import edu.vt.middleware.ldap.TestUtil;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.ldif.Ldif;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Load test for ldap pools.
 *
 * @author  Middleware Services
 * @version  $Revision: 639 $
 */
public class LdapPoolTest
{

  /** Entries for pool tests. */
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

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());

  /** LdapPool instance for concurrency testing. */
  private SoftLimitLdapPool softLimitPool;

  /** LdapPool instance for concurrency testing. */
  private BlockingLdapPool blockingPool;

  /** LdapPool instance for concurrency testing. */
  private BlockingLdapPool blockingTimeoutPool;

  /** LdapPool instance for concurrency testing. */
  private SharedLdapPool sharedPool;

  /** Time in millis it takes the pool test to run. */
  private long softLimitRuntime;

  /** Time in millis it takes the pool test to run. */
  private long blockingRuntime;

  /** Time in millis it takes the pool test to run. */
  private long blockingTimeoutRuntime;

  /** Time in millis it takes the pool test to run. */
  private long sharedRuntime;


  /**
   * Default constructor.
   *
   * @throws  Exception  On test failure.
   */
  public LdapPoolTest()
    throws Exception
  {
    final DefaultLdapFactory factory = new DefaultLdapFactory(
      TestUtil.createLdap().getLdapConfig());
    factory.setLdapValidator(
      new CompareLdapValidator(
        "ou=test,dc=vt,dc=edu",
        new SearchFilter("ou=test")));

    final LdapPoolConfig softLimitLpc = new LdapPoolConfig();
    softLimitLpc.setValidateOnCheckIn(true);
    softLimitLpc.setValidateOnCheckOut(true);
    softLimitLpc.setValidatePeriodically(true);
    softLimitLpc.setPruneTimerPeriod(5000L);
    softLimitLpc.setExpirationTime(1000L);
    softLimitLpc.setValidateTimerPeriod(5000L);
    this.softLimitPool = new SoftLimitLdapPool(softLimitLpc, factory);

    final LdapPoolConfig blockingLpc = new LdapPoolConfig();
    blockingLpc.setValidateOnCheckIn(true);
    blockingLpc.setValidateOnCheckOut(true);
    blockingLpc.setValidatePeriodically(true);
    blockingLpc.setPruneTimerPeriod(5000L);
    blockingLpc.setExpirationTime(1000L);
    blockingLpc.setValidateTimerPeriod(5000L);
    this.blockingPool = new BlockingLdapPool(blockingLpc, factory);

    final LdapPoolConfig blockingTimeoutLpc = new LdapPoolConfig();
    blockingTimeoutLpc.setValidateOnCheckIn(true);
    blockingTimeoutLpc.setValidateOnCheckOut(true);
    blockingTimeoutLpc.setValidatePeriodically(true);
    blockingTimeoutLpc.setPruneTimerPeriod(5000L);
    blockingTimeoutLpc.setExpirationTime(1000L);
    blockingTimeoutLpc.setValidateTimerPeriod(5000L);
    this.blockingTimeoutPool = new BlockingLdapPool(blockingLpc, factory);
    this.blockingTimeoutPool.setBlockWaitTime(1000L);

    final LdapPoolConfig sharedLpc = new LdapPoolConfig();
    sharedLpc.setValidateOnCheckIn(true);
    sharedLpc.setValidateOnCheckOut(true);
    sharedLpc.setValidatePeriodically(true);
    sharedLpc.setPruneTimerPeriod(5000L);
    sharedLpc.setExpirationTime(1000L);
    sharedLpc.setValidateTimerPeriod(5000L);
    this.sharedPool = new SharedLdapPool(sharedLpc, factory);
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
  @BeforeClass(
    groups = {
      "queuepooltest",
      "softlimitpooltest",
      "blockingpooltest",
      "sharedpooltest"
    }
  )
  public void createPoolEntry(
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

    this.softLimitPool.initialize();
    this.blockingPool.initialize();
    this.blockingTimeoutPool.initialize();
    this.sharedPool.initialize();
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
   * @param  ldifFile10  to load.
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
      "searchResults9",
      "searchResults10"
    }
  )
  @BeforeClass(
    groups = {
      "queuepooltest",
      "softlimitpooltest",
      "blockingpooltest",
      "sharedpooltest"
    }
  )
  public void loadPoolSearchResults(
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
    entries.get("10")[1] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile10));
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(
    groups = {
      "queuepooltest",
      "softlimitpooltest",
      "blockingpooltest",
      "sharedpooltest"
    }
  )
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
    ldap.delete(entries.get("10")[0].getDn());
    ldap.close();

    this.softLimitPool.close();
    AssertJUnit.assertEquals(this.softLimitPool.availableCount(), 0);
    AssertJUnit.assertEquals(this.softLimitPool.activeCount(), 0);
    this.blockingPool.close();
    AssertJUnit.assertEquals(this.blockingPool.availableCount(), 0);
    AssertJUnit.assertEquals(this.blockingPool.activeCount(), 0);
    this.blockingTimeoutPool.close();
    AssertJUnit.assertEquals(this.blockingTimeoutPool.availableCount(), 0);
    AssertJUnit.assertEquals(this.blockingTimeoutPool.activeCount(), 0);
    this.sharedPool.close();
    AssertJUnit.assertEquals(this.sharedPool.availableCount(), 0);
    AssertJUnit.assertEquals(this.sharedPool.activeCount(), 0);
  }


  /**
   * Sample user data.
   *
   * @return  user data
   */
  @DataProvider(name = "pool-data")
  public Object[][] createPoolData()
  {
    return
      new Object[][] {
        {
          new SearchFilter("mail=jdoe2@vt.edu"),
          "departmentNumber|givenName|sn",
          entries.get("2")[1],
        },
        {
          new SearchFilter("mail=jdoe3@vt.edu"),
          "departmentNumber|givenName|sn",
          entries.get("3")[1],
        },
        {
          new SearchFilter("mail=jdoe4@vt.edu"),
          "departmentNumber|givenName|sn",
          entries.get("4")[1],
        },
        {
          new SearchFilter("mail=jdoe5@vt.edu"),
          "departmentNumber|givenName|sn",
          entries.get("5")[1],
        },
        {
          new SearchFilter("mail=jdoe6@vt.edu"),
          "departmentNumber|givenName|sn",
          entries.get("6")[1],
        },
        {
          new SearchFilter("mail=jdoe7@vt.edu"),
          "departmentNumber|givenName|sn",
          entries.get("7")[1],
        },
        {
          new SearchFilter("mail=jdoe8@vt.edu"),
          "departmentNumber|givenName|sn|jpegPhoto",
          entries.get("8")[1],
        },
        {
          new SearchFilter("mail=jdoe9@vt.edu"),
          "departmentNumber|givenName|sn",
          entries.get("9")[1],
        },
        {
          new SearchFilter("mail=jdoe10@vt.edu"),
          "departmentNumber|givenName|sn",
          entries.get("10")[1],
        },
      };
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"softlimitpooltest"})
  public void checkSoftLimitPoolImmutable()
    throws Exception
  {
    try {
      this.softLimitPool.getLdapPoolConfig().setMinPoolSize(8);
      AssertJUnit.fail("Expected illegalstateexception to be thrown");
    } catch (IllegalStateException e) {
      AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
    }

    Ldap ldap = null;
    try {
      ldap = this.softLimitPool.checkOut();
      try {
        ldap.setLdapConfig(new LdapConfig());
        AssertJUnit.fail("Expected illegalstateexception to be thrown");
      } catch (IllegalStateException e) {
        AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
      }
      try {
        ldap.getLdapConfig().setTimeout(10000);
        AssertJUnit.fail("Expected illegalstateexception to be thrown");
      } catch (IllegalStateException e) {
        AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
      }
    } finally {
      this.softLimitPool.checkIn(ldap);
    }
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "softlimitpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 3,
    invocationCount = 50,
    timeOut = 60000
  )
  public void softLimitSmallSearch(
    final SearchFilter filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.softLimitRuntime += this.search(
      this.softLimitPool,
      filter,
      returnAttrs,
      results);
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "softlimitpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000,
    dependsOnMethods = {"softLimitSmallSearch"}
  )
  public void softLimitMediumSearch(
    final SearchFilter filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.softLimitRuntime += this.search(
      this.softLimitPool,
      filter,
      returnAttrs,
      results);
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"queuepooltest", "softlimitpooltest"},
    dependsOnMethods = {"softLimitMediumSearch"}
  )
  public void softLimitMaxClean()
    throws Exception
  {
    Thread.sleep(10000);
    AssertJUnit.assertEquals(0, this.softLimitPool.activeCount());
    AssertJUnit.assertEquals(
      LdapPoolConfig.DEFAULT_MIN_POOL_SIZE,
      this.softLimitPool.availableCount());
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"blockingpooltest"})
  public void checkBlockingPoolImmutable()
    throws Exception
  {
    try {
      this.blockingPool.getLdapPoolConfig().setMinPoolSize(8);
      AssertJUnit.fail("Expected illegalstateexception to be thrown");
    } catch (IllegalStateException e) {
      AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
    }

    Ldap ldap = null;
    try {
      ldap = this.blockingPool.checkOut();
      try {
        ldap.setLdapConfig(new LdapConfig());
        AssertJUnit.fail("Expected illegalstateexception to be thrown");
      } catch (IllegalStateException e) {
        AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
      }
      try {
        ldap.getLdapConfig().setTimeout(10000);
        AssertJUnit.fail("Expected illegalstateexception to be thrown");
      } catch (IllegalStateException e) {
        AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
      }
    } finally {
      this.blockingPool.checkIn(ldap);
    }
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "blockingpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 3,
    invocationCount = 50,
    timeOut = 60000
  )
  public void blockingSmallSearch(
    final SearchFilter filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.blockingRuntime += this.search(
      this.blockingPool,
      filter,
      returnAttrs,
      results);
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "blockingpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000,
    dependsOnMethods = {"blockingSmallSearch"}
  )
  public void blockingMediumSearch(
    final SearchFilter filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.blockingRuntime += this.search(
      this.blockingPool,
      filter,
      returnAttrs,
      results);
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "blockingpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 50,
    invocationCount = 1000,
    timeOut = 60000,
    dependsOnMethods = {"blockingMediumSearch"}
  )
  public void blockingLargeSearch(
    final SearchFilter filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.blockingRuntime += this.search(
      this.blockingPool,
      filter,
      returnAttrs,
      results);
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"queuepooltest", "blockingpooltest"},
    dependsOnMethods = {"blockingLargeSearch"}
  )
  public void blockingMaxClean()
    throws Exception
  {
    Thread.sleep(10000);
    AssertJUnit.assertEquals(0, this.blockingPool.activeCount());
    AssertJUnit.assertEquals(
      LdapPoolConfig.DEFAULT_MIN_POOL_SIZE,
      this.blockingPool.availableCount());
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "blockingtimeoutpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 3,
    invocationCount = 50,
    timeOut = 60000
  )
  public void blockingTimeoutSmallSearch(
    final SearchFilter filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    try {
      this.blockingTimeoutRuntime += this.search(
        this.blockingTimeoutPool,
        filter,
        returnAttrs,
        results);
    } catch (BlockingTimeoutException e) {
      this.logger.info("block timeout exceeded");
    }
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "blockingtimeoutpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000,
    dependsOnMethods = {"blockingTimeoutSmallSearch"}
  )
  public void blockingTimeoutMediumSearch(
    final SearchFilter filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    try {
      this.blockingTimeoutRuntime += this.search(
        this.blockingTimeoutPool,
        filter,
        returnAttrs,
        results);
    } catch (BlockingTimeoutException e) {
      this.logger.info("block timeout exceeded");
    }
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "blockingtimeoutpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 50,
    invocationCount = 1000,
    timeOut = 60000,
    dependsOnMethods = {"blockingTimeoutMediumSearch"}
  )
  public void blockingoTimeoutLargeSearch(
    final SearchFilter filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    try {
      this.blockingTimeoutRuntime += this.search(
        this.blockingTimeoutPool,
        filter,
        returnAttrs,
        results);
    } catch (BlockingTimeoutException e) {
      this.logger.info("block timeout exceeded");
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"queuepooltest", "blockingtimeoutpooltest"},
    dependsOnMethods = {"blockingLargeSearch"}
  )
  public void blockingTimeoutMaxClean()
    throws Exception
  {
    Thread.sleep(10000);
    AssertJUnit.assertEquals(0, this.blockingTimeoutPool.activeCount());
    AssertJUnit.assertEquals(
      LdapPoolConfig.DEFAULT_MIN_POOL_SIZE,
      this.blockingTimeoutPool.availableCount());
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"sharedpooltest"})
  public void checkShredPoolImmutable()
    throws Exception
  {
    try {
      this.sharedPool.getLdapPoolConfig().setMinPoolSize(8);
      AssertJUnit.fail("Expected illegalstateexception to be thrown");
    } catch (IllegalStateException e) {
      AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
    }

    Ldap ldap = null;
    try {
      ldap = this.sharedPool.checkOut();
      try {
        ldap.setLdapConfig(new LdapConfig());
        AssertJUnit.fail("Expected illegalstateexception to be thrown");
      } catch (IllegalStateException e) {
        AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
      }
      try {
        ldap.getLdapConfig().setTimeout(10000);
        AssertJUnit.fail("Expected illegalstateexception to be thrown");
      } catch (IllegalStateException e) {
        AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
      }
    } finally {
      this.sharedPool.checkIn(ldap);
    }
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "sharedpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 3,
    invocationCount = 50,
    timeOut = 60000
  )
  public void sharedSmallSearch(
    final SearchFilter filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.sharedRuntime += this.search(
      this.sharedPool,
      filter,
      returnAttrs,
      results);
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "sharedpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000,
    dependsOnMethods = {"sharedSmallSearch"}
  )
  public void sharedMediumSearch(
    final SearchFilter filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.sharedRuntime += this.search(
      this.sharedPool,
      filter,
      returnAttrs,
      results);
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "sharedpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 50,
    invocationCount = 1000,
    timeOut = 60000,
    dependsOnMethods = {"sharedMediumSearch"}
  )
  public void sharedLargeSearch(
    final SearchFilter filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.sharedRuntime += this.search(
      this.sharedPool,
      filter,
      returnAttrs,
      results);
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"queuepooltest", "sharedpooltest"},
    dependsOnMethods = {"sharedLargeSearch"}
  )
  public void sharedMaxClean()
    throws Exception
  {
    Thread.sleep(10000);
    AssertJUnit.assertEquals(0, this.sharedPool.activeCount());
    AssertJUnit.assertEquals(
      LdapPoolConfig.DEFAULT_MIN_POOL_SIZE,
      this.sharedPool.availableCount());
  }


  /**
   * @param  pool  to get ldap object from.
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @return  time it takes to checkout/search/checkin from the pool
   *
   * @throws  Exception  On test failure.
   */
  private long search(
    final LdapPool<Ldap> pool,
    final SearchFilter filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    final long startTime = System.currentTimeMillis();
    Ldap ldap = null;
    Iterator<SearchResult> iter = null;
    try {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("waiting for pool checkout");
      }
      ldap = pool.checkOut();
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("performing search");
      }
      iter = ldap.search(filter, returnAttrs.split("\\|"));
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("search completed");
      }
    } finally {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("returning ldap to pool");
      }
      pool.checkIn(ldap);
    }
    AssertJUnit.assertEquals(
      results,
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));
    return System.currentTimeMillis() - startTime;
  }
}
/*
  $Id: LdapPoolTest.java 268 2009-05-28 14:21:40Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 268 $
  Updated: $Date: 2009-05-28 07:21:40 -0700 (Thu, 28 May 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.naming.directory.SearchResult;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;
import edu.vt.middleware.ldap.TestUtil;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.ldif.Ldif;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Load test for ldap pools.
 *
 * @author  Middleware Services
 * @version  $Revision: 268 $
 */
public class LdapPoolTest
{

  /** Entries for pool tests. */
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

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());

  /** LdapPool instance for concurrency testing. */
  private SoftLimitLdapPool softLimitPool;

  /** LdapPool instance for concurrency testing. */
  private BlockingLdapPool blockingPool;

  /** LdapPool instance for concurrency testing. */
  private BlockingLdapPool blockingTimeoutPool;

  /** LdapPool instance for concurrency testing. */
  private SharedLdapPool sharedPool;

  /** Time in millis it takes the pool test to run. */
  private long softLimitRuntime;

  /** Time in millis it takes the pool test to run. */
  private long blockingRuntime;

  /** Time in millis it takes the pool test to run. */
  private long blockingTimeoutRuntime;

  /** Time in millis it takes the pool test to run. */
  private long sharedRuntime;


  /**
   * Default constructor.
   *
   * @throws  Exception  On test failure.
   */
  public LdapPoolTest()
    throws Exception
  {
    final DefaultLdapFactory factory = new DefaultLdapFactory(
      TestUtil.createLdap().getLdapConfig());
    factory.setLdapValidator(
      new CompareLdapValidator("ou=test,dc=vt,dc=edu", "ou=test"));

    final LdapPoolConfig softLimitLpc = new LdapPoolConfig();
    softLimitLpc.setValidateOnCheckIn(true);
    softLimitLpc.setValidateOnCheckOut(true);
    softLimitLpc.setValidatePeriodically(true);
    softLimitLpc.setPruneTimerPeriod(5000L);
    softLimitLpc.setExpirationTime(1000L);
    softLimitLpc.setValidateTimerPeriod(5000L);
    this.softLimitPool = new SoftLimitLdapPool(softLimitLpc, factory);

    final LdapPoolConfig blockingLpc = new LdapPoolConfig();
    blockingLpc.setValidateOnCheckIn(true);
    blockingLpc.setValidateOnCheckOut(true);
    blockingLpc.setValidatePeriodically(true);
    blockingLpc.setPruneTimerPeriod(5000L);
    blockingLpc.setExpirationTime(1000L);
    blockingLpc.setValidateTimerPeriod(5000L);
    this.blockingPool = new BlockingLdapPool(blockingLpc, factory);

    final LdapPoolConfig blockingTimeoutLpc = new LdapPoolConfig();
    blockingTimeoutLpc.setValidateOnCheckIn(true);
    blockingTimeoutLpc.setValidateOnCheckOut(true);
    blockingTimeoutLpc.setValidatePeriodically(true);
    blockingTimeoutLpc.setPruneTimerPeriod(5000L);
    blockingTimeoutLpc.setExpirationTime(1000L);
    blockingTimeoutLpc.setValidateTimerPeriod(5000L);
    this.blockingTimeoutPool = new BlockingLdapPool(blockingLpc, factory);
    this.blockingTimeoutPool.setBlockWaitTime(1000L);

    final LdapPoolConfig sharedLpc = new LdapPoolConfig();
    sharedLpc.setValidateOnCheckIn(true);
    sharedLpc.setValidateOnCheckOut(true);
    sharedLpc.setValidatePeriodically(true);
    sharedLpc.setPruneTimerPeriod(5000L);
    sharedLpc.setExpirationTime(1000L);
    sharedLpc.setValidateTimerPeriod(5000L);
    this.sharedPool = new SharedLdapPool(sharedLpc, factory);
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
  @BeforeClass(
    groups = {
      "queuepooltest",
      "softlimitpooltest",
      "blockingpooltest",
      "sharedpooltest"
      }
  )
  public void createPoolEntry(
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

    this.softLimitPool.initialize();
    this.blockingPool.initialize();
    this.blockingTimeoutPool.initialize();
    this.sharedPool.initialize();
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
   * @param  ldifFile10  to load.
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
      "searchResults9",
      "searchResults10"
    }
  )
  @BeforeClass(
    groups = {
      "queuepooltest",
      "softlimitpooltest",
      "blockingpooltest",
      "sharedpooltest"
      }
  )
  public void loadPoolSearchResults(
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
    entries.get("10")[1] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile10));
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(
    groups = {
      "queuepooltest",
      "softlimitpooltest",
      "blockingpooltest",
      "sharedpooltest"
      }
  )
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
    ldap.delete(entries.get("10")[0].getDn());
    ldap.close();

    this.softLimitPool.close();
    AssertJUnit.assertEquals(this.softLimitPool.availableCount(), 0);
    AssertJUnit.assertEquals(this.softLimitPool.activeCount(), 0);
    this.blockingPool.close();
    AssertJUnit.assertEquals(this.blockingPool.availableCount(), 0);
    AssertJUnit.assertEquals(this.blockingPool.activeCount(), 0);
    this.blockingTimeoutPool.close();
    AssertJUnit.assertEquals(this.blockingTimeoutPool.availableCount(), 0);
    AssertJUnit.assertEquals(this.blockingTimeoutPool.activeCount(), 0);
    this.sharedPool.close();
    AssertJUnit.assertEquals(this.sharedPool.availableCount(), 0);
    AssertJUnit.assertEquals(this.sharedPool.activeCount(), 0);
  }


  /**
   * Sample user data.
   *
   * @return  user data
   */
  @DataProvider(name = "pool-data")
  public Object[][] createPoolData()
  {
    return
      new Object[][] {
        {
          "mail=jdoe2@vt.edu",
          "departmentNumber|givenName|sn",
          entries.get("2")[1],
        },
        {
          "mail=jdoe3@vt.edu",
          "departmentNumber|givenName|sn",
          entries.get("3")[1],
        },
        {
          "mail=jdoe4@vt.edu",
          "departmentNumber|givenName|sn",
          entries.get("4")[1],
        },
        {
          "mail=jdoe5@vt.edu",
          "departmentNumber|givenName|sn",
          entries.get("5")[1],
        },
        {
          "mail=jdoe6@vt.edu",
          "departmentNumber|givenName|sn",
          entries.get("6")[1],
        },
        {
          "mail=jdoe7@vt.edu",
          "departmentNumber|givenName|sn",
          entries.get("7")[1],
        },
        {
          "mail=jdoe8@vt.edu",
          "departmentNumber|givenName|sn",
          entries.get("8")[1],
        },
        {
          "mail=jdoe9@vt.edu",
          "departmentNumber|givenName|sn",
          entries.get("9")[1],
        },
        {
          "mail=jdoe10@vt.edu",
          "departmentNumber|givenName|sn",
          entries.get("10")[1],
        },
      };
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"softlimitpooltest"})
  public void checkSoftLimitPoolImmutable()
    throws Exception
  {
    try {
      this.softLimitPool.getLdapPoolConfig().setMinPoolSize(8);
      AssertJUnit.fail("Expected illegalstateexception to be thrown");
    } catch (IllegalStateException e) {
      AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
    }

    Ldap ldap = null;
    try {
      ldap = this.softLimitPool.checkOut();
      try {
        ldap.setLdapConfig(new LdapConfig());
        AssertJUnit.fail("Expected illegalstateexception to be thrown");
      } catch (IllegalStateException e) {
        AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
      }
      try {
        ldap.getLdapConfig().setTimeout(10000);
        AssertJUnit.fail("Expected illegalstateexception to be thrown");
      } catch (IllegalStateException e) {
        AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
      }
    } finally {
      this.softLimitPool.checkIn(ldap);
    }
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "softlimitpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 3,
    invocationCount = 50,
    timeOut = 60000
  )
  public void softLimitSmallSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.softLimitRuntime += this.search(
      this.softLimitPool,
      filter,
      returnAttrs,
      results);
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "softlimitpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000,
    dependsOnMethods = {"softLimitSmallSearch"}
  )
  public void softLimitMediumSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.softLimitRuntime += this.search(
      this.softLimitPool,
      filter,
      returnAttrs,
      results);
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"queuepooltest", "softlimitpooltest"},
    dependsOnMethods = {"softLimitMediumSearch"}
  )
  public void softLimitMaxClean()
    throws Exception
  {
    Thread.sleep(10000);
    AssertJUnit.assertEquals(0, this.softLimitPool.activeCount());
    AssertJUnit.assertEquals(
      LdapPoolConfig.DEFAULT_MIN_POOL_SIZE,
      this.softLimitPool.availableCount());
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"blockingpooltest"})
  public void checkBlockingPoolImmutable()
    throws Exception
  {
    try {
      this.blockingPool.getLdapPoolConfig().setMinPoolSize(8);
      AssertJUnit.fail("Expected illegalstateexception to be thrown");
    } catch (IllegalStateException e) {
      AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
    }

    Ldap ldap = null;
    try {
      ldap = this.blockingPool.checkOut();
      try {
        ldap.setLdapConfig(new LdapConfig());
        AssertJUnit.fail("Expected illegalstateexception to be thrown");
      } catch (IllegalStateException e) {
        AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
      }
      try {
        ldap.getLdapConfig().setTimeout(10000);
        AssertJUnit.fail("Expected illegalstateexception to be thrown");
      } catch (IllegalStateException e) {
        AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
      }
    } finally {
      this.blockingPool.checkIn(ldap);
    }
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "blockingpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 3,
    invocationCount = 50,
    timeOut = 60000
  )
  public void blockingSmallSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.blockingRuntime += this.search(
      this.blockingPool,
      filter,
      returnAttrs,
      results);
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "blockingpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000,
    dependsOnMethods = {"blockingSmallSearch"}
  )
  public void blockingMediumSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.blockingRuntime += this.search(
      this.blockingPool,
      filter,
      returnAttrs,
      results);
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "blockingpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 50,
    invocationCount = 1000,
    timeOut = 60000,
    dependsOnMethods = {"blockingMediumSearch"}
  )
  public void blockingLargeSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.blockingRuntime += this.search(
      this.blockingPool,
      filter,
      returnAttrs,
      results);
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"queuepooltest", "blockingpooltest"},
    dependsOnMethods = {"blockingLargeSearch"}
  )
  public void blockingMaxClean()
    throws Exception
  {
    Thread.sleep(10000);
    AssertJUnit.assertEquals(0, this.blockingPool.activeCount());
    AssertJUnit.assertEquals(
      LdapPoolConfig.DEFAULT_MIN_POOL_SIZE,
      this.blockingPool.availableCount());
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "blockingtimeoutpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 3,
    invocationCount = 50,
    timeOut = 60000
  )
  public void blockingTimeoutSmallSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    try {
      this.blockingTimeoutRuntime += this.search(
        this.blockingTimeoutPool,
        filter,
        returnAttrs,
        results);
    } catch (BlockingTimeoutException e) {
      this.logger.info("block timeout exceeded");
    }
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "blockingtimeoutpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000,
    dependsOnMethods = {"blockingTimeoutSmallSearch"}
  )
  public void blockingTimeoutMediumSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    try {
      this.blockingTimeoutRuntime += this.search(
        this.blockingTimeoutPool,
        filter,
        returnAttrs,
        results);
    } catch (BlockingTimeoutException e) {
      this.logger.info("block timeout exceeded");
    }
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "blockingtimeoutpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 50,
    invocationCount = 1000,
    timeOut = 60000,
    dependsOnMethods = {"blockingTimeoutMediumSearch"}
  )
  public void blockingoTimeoutLargeSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    try {
      this.blockingTimeoutRuntime += this.search(
        this.blockingTimeoutPool,
        filter,
        returnAttrs,
        results);
    } catch (BlockingTimeoutException e) {
      this.logger.info("block timeout exceeded");
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"queuepooltest", "blockingtimeoutpooltest"},
    dependsOnMethods = {"blockingLargeSearch"}
  )
  public void blockingTimeoutMaxClean()
    throws Exception
  {
    Thread.sleep(10000);
    AssertJUnit.assertEquals(0, this.blockingTimeoutPool.activeCount());
    AssertJUnit.assertEquals(
      LdapPoolConfig.DEFAULT_MIN_POOL_SIZE,
      this.blockingTimeoutPool.availableCount());
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"sharedpooltest"})
  public void checkShredPoolImmutable()
    throws Exception
  {
    try {
      this.sharedPool.getLdapPoolConfig().setMinPoolSize(8);
      AssertJUnit.fail("Expected illegalstateexception to be thrown");
    } catch (IllegalStateException e) {
      AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
    }

    Ldap ldap = null;
    try {
      ldap = this.sharedPool.checkOut();
      try {
        ldap.setLdapConfig(new LdapConfig());
        AssertJUnit.fail("Expected illegalstateexception to be thrown");
      } catch (IllegalStateException e) {
        AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
      }
      try {
        ldap.getLdapConfig().setTimeout(10000);
        AssertJUnit.fail("Expected illegalstateexception to be thrown");
      } catch (IllegalStateException e) {
        AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
      }
    } finally {
      this.sharedPool.checkIn(ldap);
    }
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "sharedpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 3,
    invocationCount = 50,
    timeOut = 60000
  )
  public void sharedSmallSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.sharedRuntime += this.search(
      this.sharedPool,
      filter,
      returnAttrs,
      results);
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "sharedpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000,
    dependsOnMethods = {"sharedSmallSearch"}
  )
  public void sharedMediumSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.sharedRuntime += this.search(
      this.sharedPool,
      filter,
      returnAttrs,
      results);
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "sharedpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 50,
    invocationCount = 1000,
    timeOut = 60000,
    dependsOnMethods = {"sharedMediumSearch"}
  )
  public void sharedLargeSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.sharedRuntime += this.search(
      this.sharedPool,
      filter,
      returnAttrs,
      results);
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"queuepooltest", "sharedpooltest"},
    dependsOnMethods = {"sharedLargeSearch"}
  )
  public void sharedMaxClean()
    throws Exception
  {
    Thread.sleep(10000);
    AssertJUnit.assertEquals(0, this.sharedPool.activeCount());
    AssertJUnit.assertEquals(
      LdapPoolConfig.DEFAULT_MIN_POOL_SIZE,
      this.sharedPool.availableCount());
  }


  /**
   * @param  pool  to get ldap object from.
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @return  time it takes to checkout/search/checkin from the pool
   *
   * @throws  Exception  On test failure.
   */
  private long search(
    final LdapPool<Ldap> pool,
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    final long startTime = System.currentTimeMillis();
    Ldap ldap = null;
    Iterator<SearchResult> iter = null;
    try {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("waiting for pool checkout");
      }
      ldap = pool.checkOut();
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("performing search");
      }
      iter = ldap.search(filter, returnAttrs.split("\\|"));
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("search completed");
      }
    } finally {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("returning ldap to pool");
      }
      pool.checkIn(ldap);
    }
    AssertJUnit.assertEquals(
      results,
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));
    return System.currentTimeMillis() - startTime;
  }
}
/*
  $Id: LdapPoolTest.java 316 2009-07-02 22:06:46Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 316 $
  Updated: $Date: 2009-07-02 15:06:46 -0700 (Thu, 02 Jul 2009) $
*/
package edu.vt.middleware.ldap.pool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.naming.directory.SearchResult;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;
import edu.vt.middleware.ldap.TestUtil;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.ldif.Ldif;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Load test for ldap pools.
 *
 * @author  Middleware Services
 * @version  $Revision: 316 $
 */
public class LdapPoolTest
{

  /** Entries for pool tests. */
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

  /** Log for this class. */
  protected final Log logger = LogFactory.getLog(this.getClass());

  /** LdapPool instance for concurrency testing. */
  private SoftLimitLdapPool softLimitPool;

  /** LdapPool instance for concurrency testing. */
  private BlockingLdapPool blockingPool;

  /** LdapPool instance for concurrency testing. */
  private BlockingLdapPool blockingTimeoutPool;

  /** LdapPool instance for concurrency testing. */
  private SharedLdapPool sharedPool;

  /** Time in millis it takes the pool test to run. */
  private long softLimitRuntime;

  /** Time in millis it takes the pool test to run. */
  private long blockingRuntime;

  /** Time in millis it takes the pool test to run. */
  private long blockingTimeoutRuntime;

  /** Time in millis it takes the pool test to run. */
  private long sharedRuntime;


  /**
   * Default constructor.
   *
   * @throws  Exception  On test failure.
   */
  public LdapPoolTest()
    throws Exception
  {
    final DefaultLdapFactory factory = new DefaultLdapFactory(
      TestUtil.createLdap().getLdapConfig());
    factory.setLdapValidator(
      new CompareLdapValidator("ou=test,dc=vt,dc=edu", "ou=test"));

    final LdapPoolConfig softLimitLpc = new LdapPoolConfig();
    softLimitLpc.setValidateOnCheckIn(true);
    softLimitLpc.setValidateOnCheckOut(true);
    softLimitLpc.setValidatePeriodically(true);
    softLimitLpc.setPruneTimerPeriod(5000L);
    softLimitLpc.setExpirationTime(1000L);
    softLimitLpc.setValidateTimerPeriod(5000L);
    this.softLimitPool = new SoftLimitLdapPool(softLimitLpc, factory);

    final LdapPoolConfig blockingLpc = new LdapPoolConfig();
    blockingLpc.setValidateOnCheckIn(true);
    blockingLpc.setValidateOnCheckOut(true);
    blockingLpc.setValidatePeriodically(true);
    blockingLpc.setPruneTimerPeriod(5000L);
    blockingLpc.setExpirationTime(1000L);
    blockingLpc.setValidateTimerPeriod(5000L);
    this.blockingPool = new BlockingLdapPool(blockingLpc, factory);

    final LdapPoolConfig blockingTimeoutLpc = new LdapPoolConfig();
    blockingTimeoutLpc.setValidateOnCheckIn(true);
    blockingTimeoutLpc.setValidateOnCheckOut(true);
    blockingTimeoutLpc.setValidatePeriodically(true);
    blockingTimeoutLpc.setPruneTimerPeriod(5000L);
    blockingTimeoutLpc.setExpirationTime(1000L);
    blockingTimeoutLpc.setValidateTimerPeriod(5000L);
    this.blockingTimeoutPool = new BlockingLdapPool(blockingLpc, factory);
    this.blockingTimeoutPool.setBlockWaitTime(1000L);

    final LdapPoolConfig sharedLpc = new LdapPoolConfig();
    sharedLpc.setValidateOnCheckIn(true);
    sharedLpc.setValidateOnCheckOut(true);
    sharedLpc.setValidatePeriodically(true);
    sharedLpc.setPruneTimerPeriod(5000L);
    sharedLpc.setExpirationTime(1000L);
    sharedLpc.setValidateTimerPeriod(5000L);
    this.sharedPool = new SharedLdapPool(sharedLpc, factory);
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
  @BeforeClass(
    groups = {
      "queuepooltest",
      "softlimitpooltest",
      "blockingpooltest",
      "sharedpooltest"
    }
  )
  public void createPoolEntry(
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

    this.softLimitPool.initialize();
    this.blockingPool.initialize();
    this.blockingTimeoutPool.initialize();
    this.sharedPool.initialize();
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
   * @param  ldifFile10  to load.
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
      "searchResults9",
      "searchResults10"
    }
  )
  @BeforeClass(
    groups = {
      "queuepooltest",
      "softlimitpooltest",
      "blockingpooltest",
      "sharedpooltest"
    }
  )
  public void loadPoolSearchResults(
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
    entries.get("10")[1] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile10));
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(
    groups = {
      "queuepooltest",
      "softlimitpooltest",
      "blockingpooltest",
      "sharedpooltest"
    }
  )
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
    ldap.delete(entries.get("10")[0].getDn());
    ldap.close();

    this.softLimitPool.close();
    AssertJUnit.assertEquals(this.softLimitPool.availableCount(), 0);
    AssertJUnit.assertEquals(this.softLimitPool.activeCount(), 0);
    this.blockingPool.close();
    AssertJUnit.assertEquals(this.blockingPool.availableCount(), 0);
    AssertJUnit.assertEquals(this.blockingPool.activeCount(), 0);
    this.blockingTimeoutPool.close();
    AssertJUnit.assertEquals(this.blockingTimeoutPool.availableCount(), 0);
    AssertJUnit.assertEquals(this.blockingTimeoutPool.activeCount(), 0);
    this.sharedPool.close();
    AssertJUnit.assertEquals(this.sharedPool.availableCount(), 0);
    AssertJUnit.assertEquals(this.sharedPool.activeCount(), 0);
  }


  /**
   * Sample user data.
   *
   * @return  user data
   */
  @DataProvider(name = "pool-data")
  public Object[][] createPoolData()
  {
    return
      new Object[][] {
        {
          "mail=jdoe2@vt.edu",
          "departmentNumber|givenName|sn",
          entries.get("2")[1],
        },
        {
          "mail=jdoe3@vt.edu",
          "departmentNumber|givenName|sn",
          entries.get("3")[1],
        },
        {
          "mail=jdoe4@vt.edu",
          "departmentNumber|givenName|sn",
          entries.get("4")[1],
        },
        {
          "mail=jdoe5@vt.edu",
          "departmentNumber|givenName|sn",
          entries.get("5")[1],
        },
        {
          "mail=jdoe6@vt.edu",
          "departmentNumber|givenName|sn",
          entries.get("6")[1],
        },
        {
          "mail=jdoe7@vt.edu",
          "departmentNumber|givenName|sn",
          entries.get("7")[1],
        },
        {
          "mail=jdoe8@vt.edu",
          "departmentNumber|givenName|sn|jpegPhoto",
          entries.get("8")[1],
        },
        {
          "mail=jdoe9@vt.edu",
          "departmentNumber|givenName|sn",
          entries.get("9")[1],
        },
        {
          "mail=jdoe10@vt.edu",
          "departmentNumber|givenName|sn",
          entries.get("10")[1],
        },
      };
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"softlimitpooltest"})
  public void checkSoftLimitPoolImmutable()
    throws Exception
  {
    try {
      this.softLimitPool.getLdapPoolConfig().setMinPoolSize(8);
      AssertJUnit.fail("Expected illegalstateexception to be thrown");
    } catch (IllegalStateException e) {
      AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
    }

    Ldap ldap = null;
    try {
      ldap = this.softLimitPool.checkOut();
      try {
        ldap.setLdapConfig(new LdapConfig());
        AssertJUnit.fail("Expected illegalstateexception to be thrown");
      } catch (IllegalStateException e) {
        AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
      }
      try {
        ldap.getLdapConfig().setTimeout(10000);
        AssertJUnit.fail("Expected illegalstateexception to be thrown");
      } catch (IllegalStateException e) {
        AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
      }
    } finally {
      this.softLimitPool.checkIn(ldap);
    }
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "softlimitpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 3,
    invocationCount = 50,
    timeOut = 60000
  )
  public void softLimitSmallSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.softLimitRuntime += this.search(
      this.softLimitPool,
      filter,
      returnAttrs,
      results);
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "softlimitpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000,
    dependsOnMethods = {"softLimitSmallSearch"}
  )
  public void softLimitMediumSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.softLimitRuntime += this.search(
      this.softLimitPool,
      filter,
      returnAttrs,
      results);
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"queuepooltest", "softlimitpooltest"},
    dependsOnMethods = {"softLimitMediumSearch"}
  )
  public void softLimitMaxClean()
    throws Exception
  {
    Thread.sleep(10000);
    AssertJUnit.assertEquals(0, this.softLimitPool.activeCount());
    AssertJUnit.assertEquals(
      LdapPoolConfig.DEFAULT_MIN_POOL_SIZE,
      this.softLimitPool.availableCount());
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"blockingpooltest"})
  public void checkBlockingPoolImmutable()
    throws Exception
  {
    try {
      this.blockingPool.getLdapPoolConfig().setMinPoolSize(8);
      AssertJUnit.fail("Expected illegalstateexception to be thrown");
    } catch (IllegalStateException e) {
      AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
    }

    Ldap ldap = null;
    try {
      ldap = this.blockingPool.checkOut();
      try {
        ldap.setLdapConfig(new LdapConfig());
        AssertJUnit.fail("Expected illegalstateexception to be thrown");
      } catch (IllegalStateException e) {
        AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
      }
      try {
        ldap.getLdapConfig().setTimeout(10000);
        AssertJUnit.fail("Expected illegalstateexception to be thrown");
      } catch (IllegalStateException e) {
        AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
      }
    } finally {
      this.blockingPool.checkIn(ldap);
    }
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "blockingpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 3,
    invocationCount = 50,
    timeOut = 60000
  )
  public void blockingSmallSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.blockingRuntime += this.search(
      this.blockingPool,
      filter,
      returnAttrs,
      results);
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "blockingpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000,
    dependsOnMethods = {"blockingSmallSearch"}
  )
  public void blockingMediumSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.blockingRuntime += this.search(
      this.blockingPool,
      filter,
      returnAttrs,
      results);
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "blockingpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 50,
    invocationCount = 1000,
    timeOut = 60000,
    dependsOnMethods = {"blockingMediumSearch"}
  )
  public void blockingLargeSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.blockingRuntime += this.search(
      this.blockingPool,
      filter,
      returnAttrs,
      results);
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"queuepooltest", "blockingpooltest"},
    dependsOnMethods = {"blockingLargeSearch"}
  )
  public void blockingMaxClean()
    throws Exception
  {
    Thread.sleep(10000);
    AssertJUnit.assertEquals(0, this.blockingPool.activeCount());
    AssertJUnit.assertEquals(
      LdapPoolConfig.DEFAULT_MIN_POOL_SIZE,
      this.blockingPool.availableCount());
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "blockingtimeoutpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 3,
    invocationCount = 50,
    timeOut = 60000
  )
  public void blockingTimeoutSmallSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    try {
      this.blockingTimeoutRuntime += this.search(
        this.blockingTimeoutPool,
        filter,
        returnAttrs,
        results);
    } catch (BlockingTimeoutException e) {
      this.logger.info("block timeout exceeded");
    }
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "blockingtimeoutpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000,
    dependsOnMethods = {"blockingTimeoutSmallSearch"}
  )
  public void blockingTimeoutMediumSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    try {
      this.blockingTimeoutRuntime += this.search(
        this.blockingTimeoutPool,
        filter,
        returnAttrs,
        results);
    } catch (BlockingTimeoutException e) {
      this.logger.info("block timeout exceeded");
    }
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "blockingtimeoutpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 50,
    invocationCount = 1000,
    timeOut = 60000,
    dependsOnMethods = {"blockingTimeoutMediumSearch"}
  )
  public void blockingoTimeoutLargeSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    try {
      this.blockingTimeoutRuntime += this.search(
        this.blockingTimeoutPool,
        filter,
        returnAttrs,
        results);
    } catch (BlockingTimeoutException e) {
      this.logger.info("block timeout exceeded");
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"queuepooltest", "blockingtimeoutpooltest"},
    dependsOnMethods = {"blockingLargeSearch"}
  )
  public void blockingTimeoutMaxClean()
    throws Exception
  {
    Thread.sleep(10000);
    AssertJUnit.assertEquals(0, this.blockingTimeoutPool.activeCount());
    AssertJUnit.assertEquals(
      LdapPoolConfig.DEFAULT_MIN_POOL_SIZE,
      this.blockingTimeoutPool.availableCount());
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"sharedpooltest"})
  public void checkShredPoolImmutable()
    throws Exception
  {
    try {
      this.sharedPool.getLdapPoolConfig().setMinPoolSize(8);
      AssertJUnit.fail("Expected illegalstateexception to be thrown");
    } catch (IllegalStateException e) {
      AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
    }

    Ldap ldap = null;
    try {
      ldap = this.sharedPool.checkOut();
      try {
        ldap.setLdapConfig(new LdapConfig());
        AssertJUnit.fail("Expected illegalstateexception to be thrown");
      } catch (IllegalStateException e) {
        AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
      }
      try {
        ldap.getLdapConfig().setTimeout(10000);
        AssertJUnit.fail("Expected illegalstateexception to be thrown");
      } catch (IllegalStateException e) {
        AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
      }
    } finally {
      this.sharedPool.checkIn(ldap);
    }
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "sharedpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 3,
    invocationCount = 50,
    timeOut = 60000
  )
  public void sharedSmallSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.sharedRuntime += this.search(
      this.sharedPool,
      filter,
      returnAttrs,
      results);
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "sharedpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000,
    dependsOnMethods = {"sharedSmallSearch"}
  )
  public void sharedMediumSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.sharedRuntime += this.search(
      this.sharedPool,
      filter,
      returnAttrs,
      results);
  }


  /**
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"queuepooltest", "sharedpooltest"},
    dataProvider = "pool-data",
    threadPoolSize = 50,
    invocationCount = 1000,
    timeOut = 60000,
    dependsOnMethods = {"sharedMediumSearch"}
  )
  public void sharedLargeSearch(
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    this.sharedRuntime += this.search(
      this.sharedPool,
      filter,
      returnAttrs,
      results);
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"queuepooltest", "sharedpooltest"},
    dependsOnMethods = {"sharedLargeSearch"}
  )
  public void sharedMaxClean()
    throws Exception
  {
    Thread.sleep(10000);
    AssertJUnit.assertEquals(0, this.sharedPool.activeCount());
    AssertJUnit.assertEquals(
      LdapPoolConfig.DEFAULT_MIN_POOL_SIZE,
      this.sharedPool.availableCount());
  }


  /**
   * @param  pool  to get ldap object from.
   * @param  filter  to search with.
   * @param  returnAttrs  to search for.
   * @param  results  to expect from the search.
   *
   * @return  time it takes to checkout/search/checkin from the pool
   *
   * @throws  Exception  On test failure.
   */
  private long search(
    final LdapPool<Ldap> pool,
    final String filter,
    final String returnAttrs,
    final LdapEntry results)
    throws Exception
  {
    final long startTime = System.currentTimeMillis();
    Ldap ldap = null;
    Iterator<SearchResult> iter = null;
    try {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("waiting for pool checkout");
      }
      ldap = pool.checkOut();
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("performing search");
      }
      iter = ldap.search(filter, returnAttrs.split("\\|"));
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("search completed");
      }
    } finally {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace("returning ldap to pool");
      }
      pool.checkIn(ldap);
    }
    AssertJUnit.assertEquals(
      results,
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));
    return System.currentTimeMillis() - startTime;
  }
}
