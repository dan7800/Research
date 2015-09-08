/*
  $Id: LdapTest.java 663 2009-09-24 02:06:06Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 663 $
  Updated: $Date: 2009-09-23 19:06:06 -0700 (Wed, 23 Sep 2009) $
*/
package edu.vt.middleware.ldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.naming.Binding;
import javax.naming.LimitExceededException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.TimeLimitExceededException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;
import edu.vt.middleware.ldap.Ldap.AttributeModification;
import edu.vt.middleware.ldap.bean.LdapAttribute;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.bean.LdapResult;
import edu.vt.middleware.ldap.handler.AttributeHandler;
import edu.vt.middleware.ldap.handler.BinaryAttributeHandler;
import edu.vt.middleware.ldap.handler.EntryDnSearchResultHandler;
import edu.vt.middleware.ldap.handler.FqdnSearchResultHandler;
import edu.vt.middleware.ldap.handler.RecursiveAttributeHandler;
import edu.vt.middleware.ldap.handler.SearchResultHandler;
import edu.vt.middleware.ldap.ldif.Ldif;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link Ldap}.
 *
 * @author  Middleware Services
 * @version  $Revision: 663 $
 */
public class LdapTest
{

  /** Invalid search filter. */
  public static final String INVALID_FILTER = "(cn=not-a-name)";

  /** Entry created for ldap tests. */
  private static LdapEntry testLdapEntry;

  /** Entries for group tests. */
  private static Map<String, LdapEntry[]> groupEntries =
    new HashMap<String, LdapEntry[]>();

  /** Ldap instance for concurrency testing. */
  private Ldap singleLdap;

  /**
   * Initialize the map of group entries.
   */
  static {
    for (int i = 2; i <= 5; i++) {
      groupEntries.put(String.valueOf(i), new LdapEntry[2]);
    }
  }


  /**
   * Default constructor.
   *
   * @throws  Exception  if ldap cannot be constructed
   */
  public LdapTest()
    throws Exception
  {
    this.singleLdap = TestUtil.createLdap();
  }


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "createEntry2" })
  @BeforeClass(groups = {"ldaptest"})
  public void createLdapEntry(final String ldifFile)
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


  /**
   * @param  ldifFile2  to create.
   * @param  ldifFile3  to create.
   * @param  ldifFile4  to create.
   * @param  ldifFile5  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "createGroup2",
    "createGroup3",
    "createGroup4",
    "createGroup5" })
  @BeforeClass(groups = {"ldaptest"})
  public void createGroupEntry(
    final String ldifFile2,
    final String ldifFile3,
    final String ldifFile4,
    final String ldifFile5)
    throws Exception
  {
    groupEntries.get("2")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile2));
    groupEntries.get("3")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile3));
    groupEntries.get("4")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile4));
    groupEntries.get("5")[0] = TestUtil.convertLdifToEntry(
      TestUtil.readFileIntoString(ldifFile5));

    Ldap ldap = TestUtil.createSetupLdap();
    for (Map.Entry<String, LdapEntry[]> e : groupEntries.entrySet()) {
      ldap.create(
        e.getValue()[0].getDn(),
        e.getValue()[0].getLdapAttributes().toAttributes());
    }
    ldap.close();

    ldap = TestUtil.createLdap();
    for (Map.Entry<String, LdapEntry[]> e : groupEntries.entrySet()) {
      while (
        !ldap.compare(
          e.getValue()[0].getDn(),
          new SearchFilter(e.getValue()[0].getDn().split(",")[0]))) {
        Thread.sleep(100);
      }
    }

    // setup group relationships
    ldap.modifyAttributes(
      groupEntries.get("2")[0].getDn(),
      AttributeModification.ADD,
      AttributesFactory.createAttributes(
        "member", "uugid=group3,ou=test,dc=vt,dc=edu"));
    ldap.modifyAttributes(
      groupEntries.get("3")[0].getDn(),
      AttributeModification.ADD,
      AttributesFactory.createAttributes(
        "member",
        new String[]{
          "uugid=group4,ou=test,dc=vt,dc=edu",
          "uugid=group5,ou=test,dc=vt,dc=edu", }));
    ldap.modifyAttributes(
      groupEntries.get("4")[0].getDn(),
      AttributeModification.ADD,
      AttributesFactory.createAttributes(
        "member",
        "uugid=group3,ou=test,dc=vt,dc=edu"));
    ldap.close();
  }


  /**
   * @param  oldDn  to rename.
   * @param  newDn  to rename to.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "renameOldDn", "renameNewDn" })
  @AfterClass(groups = {"ldaptest"})
  public void renameLdapEntry(final String oldDn, final String newDn)
    throws Exception
  {
    final Ldap ldap = this.createLdap(true);
    AssertJUnit.assertNotNull(ldap.getAttributes(oldDn));
    ldap.rename(oldDn, newDn);
    AssertJUnit.assertNotNull(ldap.getAttributes(newDn));
    try {
      ldap.getAttributes(oldDn);
      AssertJUnit.fail(
        "Should have thrown NameNotFoundException, no exception thrown");
    } catch (NameNotFoundException e) {
      AssertJUnit.assertEquals(NameNotFoundException.class, e.getClass());
    } catch (Exception e) {
      AssertJUnit.fail("Should have thrown NameNotFoundException, threw " + e);
    }
    ldap.rename(newDn, oldDn);
    AssertJUnit.assertNotNull(ldap.getAttributes(oldDn));
    try {
      ldap.getAttributes(newDn);
      AssertJUnit.fail(
        "Should have thrown NameNotFoundException, no exception thrown");
    } catch (NameNotFoundException e) {
      AssertJUnit.assertEquals(NameNotFoundException.class, e.getClass());
    } catch (Exception e) {
      AssertJUnit.fail("Should have thrown NameNotFoundException, threw " + e);
    }
    ldap.close();
  }



  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"ldaptest"}, dependsOnMethods = {"renameLdapEntry"})
  public void deleteLdapEntry()
    throws Exception
  {
    final Ldap ldap = TestUtil.createSetupLdap();
    ldap.delete(testLdapEntry.getDn());
    ldap.delete(groupEntries.get("2")[0].getDn());
    ldap.delete(groupEntries.get("3")[0].getDn());
    ldap.delete(groupEntries.get("4")[0].getDn());
    ldap.delete(groupEntries.get("5")[0].getDn());
    ldap.close();
  }


  /**
   * @param  createNew  whether to construct a new ldap instance.
   *
   * @return  <code>Ldap</code>
   *
   * @throws  Exception  On ldap construction failure.
   */
  public Ldap createLdap(final boolean createNew)
    throws Exception
  {
    if (createNew) {
      return TestUtil.createLdap();
    }
    return singleLdap;
  }


  /**
   * @param  dn  to compare.
   * @param  filter  to compare with.
   * @param  filterArgs  to replace args in filter with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "compareDn", "compareFilter", "compareFilterArgs" })
  @Test(
    groups = {"ldaptest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void compare(
    final String dn, final String filter, final String filterArgs)
    throws Exception
  {
    final Ldap ldap = this.createLdap(false);
    AssertJUnit.assertFalse(
      ldap.compare(dn, INVALID_FILTER, filterArgs.split("\\|")));
    AssertJUnit.assertTrue(ldap.compare(dn, filter, filterArgs.split("\\|")));
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  filterArgs  to replace args in filter with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "searchDn",
      "searchFilter",
      "searchFilterArgs",
      "searchReturnAttrs",
      "searchResults"
    }
  )
  @Test(
    groups = {"ldaptest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void search(
    final String dn,
    final String filter,
    final String filterArgs,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final Ldap ldap = this.createLdap(false);

    final String expected = TestUtil.readFileIntoString(ldifFile);
    final LdapEntry entry = TestUtil.convertLdifToEntry(expected);
    final LdapEntry shortDnEntry = TestUtil.convertLdifToEntry(expected);
    shortDnEntry.setDn(
      shortDnEntry.getDn().substring(0, shortDnEntry.getDn().indexOf(",")));

    final LdapEntry entryDnEntry = TestUtil.convertLdifToEntry(expected);
    entryDnEntry.getLdapAttributes().addAttribute(
      "entryDN",
      entryDnEntry.getDn());

    // test searching
    Iterator<SearchResult> iter = ldap.search(
      dn,
      new SearchFilter(filter, filterArgs.split("\\|")),
      returnAttrs.split("\\|"));
    AssertJUnit.assertEquals(
      entry,
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));

    // test searching without handler
    iter = ldap.search(
      dn,
      new SearchFilter(filter, filterArgs.split("\\|")),
      returnAttrs.split("\\|"),
      new SearchResultHandler[0]);
    AssertJUnit.assertEquals(
      shortDnEntry,
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));

    // test searching with multiple handlers
    final EntryDnSearchResultHandler srh = new EntryDnSearchResultHandler();
    iter = ldap.search(
      dn,
      new SearchFilter(filter, filterArgs.split("\\|")),
      returnAttrs.split("\\|"),
      new FqdnSearchResultHandler(),
      srh);
    AssertJUnit.assertEquals(
      entryDnEntry,
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));

    // test that entry dn handler is no-op if attribute name conflicts
    srh.setDnAttributeName("givenName");
    iter = ldap.search(
      dn,
      new SearchFilter(filter, filterArgs.split("\\|")),
      returnAttrs.split("\\|"),
      new FqdnSearchResultHandler(),
      srh);
    AssertJUnit.assertEquals(
      entry,
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  filterArgs  to replace args in filter with.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "recursiveSearchDn",
      "recursiveSearchFilter",
      "recursiveSearchFilterArgs",
      "recursiveSearchResults"
    }
  )
  @Test(groups = {"ldaptest"})
  public void recursiveSearch(
    final String dn,
    final String filter,
    final String filterArgs,
    final String ldifFile)
    throws Exception
  {
    final Ldap ldap = this.createLdap(false);

    final String expected = TestUtil.readFileIntoString(ldifFile);
    final LdapEntry entry = TestUtil.convertLdifToEntry(expected);

    // test recursive searching
    final FqdnSearchResultHandler handler = new FqdnSearchResultHandler();
    handler.setAttributeHandler(
      new AttributeHandler[]{new RecursiveAttributeHandler(ldap, "member")});

    final Iterator<SearchResult> iter = ldap.search(
      dn,
      new SearchFilter(filter, filterArgs.split("\\|")),
      null,
      handler);
    AssertJUnit.assertEquals(
      entry,
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  resultsSize  of search results.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "searchExceptionDn",
      "searchExceptionFilter",
      "searchExceptionResultsSize"
    }
  )
  @Test(groups = {"ldaptest"})
  public void searchWithException(
    final String dn,
    final String filter,
    final int resultsSize)
    throws Exception
  {
    final Ldap ldap = this.createLdap(true);

    // test exception searching
    ldap.getLdapConfig().setCountLimit(resultsSize);
    ldap.getLdapConfig().setSearchIgnoreExceptions(null);

    try {
      ldap.search(dn, new SearchFilter("(uugid=*)"));
      AssertJUnit.fail("Should have thrown SizeLimitExceededException");
    } catch (NamingException e) {
      AssertJUnit.assertEquals(SizeLimitExceededException.class, e.getClass());
    }

    ldap.getLdapConfig().setSearchIgnoreExceptions(
      new NamingException[]{new TimeLimitExceededException()});
    try {
      ldap.search(dn, new SearchFilter("(uugid=*)"));
      AssertJUnit.fail("Should have thrown SizeLimitExceededException");
    } catch (NamingException e) {
      AssertJUnit.assertEquals(SizeLimitExceededException.class, e.getClass());
    }

    ldap.getLdapConfig().setSearchIgnoreExceptions(
      new NamingException[]{new LimitExceededException()});
    final Iterator<SearchResult> iter = ldap.search(
      dn, new SearchFilter(filter));
    AssertJUnit.assertEquals(resultsSize, (new LdapResult(iter)).size());

    ldap.close();
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "searchAttributesDn",
      "searchAttributesFilter",
      "searchAttributesReturnAttrs",
      "searchAttributesResults"
    }
  )
  @Test(
    groups = {"ldaptest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void searchAttributes(
    final String dn,
    final String filter,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final String[] matchAttrs = filter.split("=");
    final Ldap ldap = this.createLdap(false);
    // test searching
    Iterator<SearchResult> iter = ldap.searchAttributes(
      dn,
      AttributesFactory.createAttributes(matchAttrs[0], matchAttrs[1]),
      returnAttrs.split("\\|"));
    final String expected = TestUtil.readFileIntoString(ldifFile);
    AssertJUnit.assertEquals(
      TestUtil.convertLdifToEntry(expected),
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));
    // test searching without handler
    iter = ldap.searchAttributes(
      dn,
      AttributesFactory.createAttributes(matchAttrs[0], matchAttrs[1]),
      returnAttrs.split("\\|"),
      new SearchResultHandler[0]);

    final LdapEntry entry = TestUtil.convertLdifToEntry(expected);
    entry.setDn(entry.getDn().substring(0, entry.getDn().indexOf(",")));
    AssertJUnit.assertEquals(
      entry,
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));
  }


  /**
   * @param  dn  to search on.
   * @param  results  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "listDn", "listResults" })
  @Test(groups = {"ldaptest"})
  public void list(final String dn, final String results)
    throws Exception
  {
    final Ldap ldap = this.createLdap(true);
    final Iterator<NameClassPair> iter = ldap.list(dn);
    final List<String> l = new ArrayList<String>();
    while (iter.hasNext()) {
      final NameClassPair ncp = iter.next();
      l.add(ncp.getName());
    }

    final List<String> expected = Arrays.asList(results.split("\\|"));
    AssertJUnit.assertTrue(l.containsAll(expected));
    ldap.close();
  }


  /**
   * @param  dn  to search on.
   * @param  results  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "listBindingsDn", "listBindingsResults" })
  @Test(groups = {"ldaptest"})
  public void listBindings(final String dn, final String results)
    throws Exception
  {
    final Ldap ldap = this.createLdap(true);
    final Iterator<Binding> iter = ldap.listBindings(dn);
    final List<String> l = new ArrayList<String>();
    while (iter.hasNext()) {
      final Binding b = iter.next();
      l.add(b.getName());
    }

    final List<String> expected = Arrays.asList(results.split("\\|"));
    AssertJUnit.assertTrue(l.containsAll(expected));
    ldap.close();
  }


  /**
   * @param  dn  to search on.
   * @param  returnAttrs  to return from search.
   * @param  results  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "getAttributesDn",
      "getAttributesReturnAttrs",
      "getAttributesResults"
    }
  )
  @Test(
    groups = {"ldaptest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void getAttributes(
    final String dn,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    final Ldap ldap = this.createLdap(false);
    final Attributes attrs = ldap.getAttributes(dn, returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  dn  to search on.
   * @param  returnAttrs  to return from search.
   * @param  results  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "getAttributesBase64Dn",
      "getAttributesBase64ReturnAttrs",
      "getAttributesBase64Results"
    }
  )
  @Test(groups = {"ldaptest"})
  public void getAttributesBase64(
    final String dn,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    final Ldap ldap = this.createLdap(true);
    final Attributes attrs = ldap.getAttributes(
      dn,
      returnAttrs.split("\\|"),
      new BinaryAttributeHandler());
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
    ldap.close();
  }


  /**
   * @param  dn  to search on.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "getSchemaDn", "getSchemaResults" })
  @Test(groups = {"ldaptest"})
  public void getSchema(final String dn, final String ldifFile)
    throws Exception
  {
    final Ldap ldap = this.createLdap(true);
    final Iterator<SearchResult> iter = ldap.getSchema(dn);
    final String expected = TestUtil.readFileIntoString(ldifFile);
    AssertJUnit.assertEquals(
      TestUtil.convertLdifToResult(expected),
      TestUtil.convertLdifToResult((new Ldif()).createLdif(iter)));
    ldap.close();
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"ldaptest"},
    enabled = false
  )
  public void getSaslMechanisms()
    throws Exception
  {
    final Ldap ldap = this.createLdap(true);
    ldap.getSaslMechanisms();
    ldap.close();
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"ldaptest"},
    enabled = false
  )
  public void getSupportedControls()
    throws Exception
  {
    final Ldap ldap = this.createLdap(true);
    ldap.getSupportedControls();
    ldap.close();
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to add.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "addAttributeDn", "addAttributeAttribute" })
  @Test(groups = {"ldaptest"})
  public void addAttribute(final String dn, final String attrs)
    throws Exception
  {
    final LdapAttribute expected = TestUtil.convertStringToAttributes(attrs)
        .getAttributes().iterator().next();
    final Ldap ldap = this.createLdap(true);
    ldap.modifyAttributes(
      dn,
      AttributeModification.ADD,
      AttributesFactory.createAttributes(
        expected.getName(), expected.getValues().toArray()));

    final Attributes a = ldap.getAttributes(
      dn,
      new String[] {expected.getName()});
    AssertJUnit.assertEquals(
      expected,
      new LdapAttribute(a.get(expected.getName())));
    ldap.close();
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to add.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "addAttributesDn", "addAttributesAttributes" })
  @Test(groups = {"ldaptest"})
  public void addAttributes(final String dn, final String attrs)
    throws Exception
  {
    final LdapAttributes expected = TestUtil.convertStringToAttributes(attrs);
    final Ldap ldap = this.createLdap(true);
    final ModificationItem[] mods = new ModificationItem[expected.size()];
    int i = 0;
    for (LdapAttribute la : expected.getAttributes()) {
      mods[i] = new ModificationItem(
        DirContext.ADD_ATTRIBUTE, la.toAttribute());
      i++;
    }
    ldap.modifyAttributes(dn, mods);

    final Attributes a = ldap.getAttributes(dn, expected.getAttributeNames());
    AssertJUnit.assertEquals(expected, new LdapAttributes(a));
    ldap.close();
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to replace.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "replaceAttributeDn", "replaceAttributeAttribute" })
  @Test(
    groups = {"ldaptest"},
    dependsOnMethods = {"addAttribute"}
  )
  public void replaceAttribute(final String dn, final String attrs)
    throws Exception
  {
    final LdapAttribute expected = TestUtil.convertStringToAttributes(attrs)
        .getAttributes().iterator().next();
    final Ldap ldap = this.createLdap(true);
    ldap.modifyAttributes(
      dn,
      AttributeModification.REPLACE,
      AttributesFactory.createAttributes(
        expected.getName(), expected.getValues().toArray()));

    final Attributes a = ldap.getAttributes(
      dn,
      new String[] {expected.getName()});
    AssertJUnit.assertEquals(
      expected,
      new LdapAttribute(a.get(expected.getName())));
    ldap.close();
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to replace.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "replaceAttributesDn", "replaceAttributesAttributes" })
  @Test(
    groups = {"ldaptest"},
    dependsOnMethods = {"addAttributes"}
  )
  public void replaceAttributes(final String dn, final String attrs)
    throws Exception
  {
    final LdapAttributes expected = TestUtil.convertStringToAttributes(attrs);
    final Ldap ldap = this.createLdap(true);
    final ModificationItem[] mods = new ModificationItem[expected.size()];
    int i = 0;
    for (LdapAttribute la : expected.getAttributes()) {
      mods[i] = new ModificationItem(
        DirContext.REPLACE_ATTRIBUTE, la.toAttribute());
      i++;
    }
    ldap.modifyAttributes(dn, mods);

    final Attributes a = ldap.getAttributes(dn, expected.getAttributeNames());
    AssertJUnit.assertEquals(expected, new LdapAttributes(a));
    ldap.close();
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to remove.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "removeAttributeDn", "removeAttributeAttribute" })
  @Test(
    groups = {"ldaptest"},
    dependsOnMethods = {"replaceAttribute"}
  )
  public void removeAttribute(final String dn, final String attrs)
    throws Exception
  {
    final LdapAttribute expected = TestUtil.convertStringToAttributes(attrs)
        .getAttributes().iterator().next();
    final LdapAttribute remove = new LdapAttribute(expected);
    remove.getValues().remove(0);
    expected.getValues().remove(1);

    final Ldap ldap = this.createLdap(true);
    ldap.modifyAttributes(
      dn,
      AttributeModification.REMOVE,
      AttributesFactory.createAttributes(
        remove.getName(), remove.getValues().toArray()));

    final Attributes a = ldap.getAttributes(
      dn,
      new String[] {expected.getName()});
    AssertJUnit.assertEquals(
      expected,
      new LdapAttribute(a.get(expected.getName())));
    ldap.close();
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to remove.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "removeAttributesDn", "removeAttributesAttributes" })
  @Test(
    groups = {"ldaptest"},
    dependsOnMethods = {"replaceAttributes"}
  )
  public void removeAttributes(final String dn, final String attrs)
    throws Exception
  {
    final LdapAttributes expected = TestUtil.convertStringToAttributes(attrs);
    final LdapAttributes remove = new LdapAttributes(expected);

    final String[] attrsName = remove.getAttributeNames();
    remove.getAttributes().remove(remove.getAttribute(attrsName[0]));
    expected.getAttributes().remove(expected.getAttribute(attrsName[1]));

    final Ldap ldap = this.createLdap(true);
    final ModificationItem[] mods = new ModificationItem[expected.size()];
    int i = 0;
    for (LdapAttribute la : remove.getAttributes()) {
      mods[i] = new ModificationItem(
        DirContext.REMOVE_ATTRIBUTE, la.toAttribute());
      i++;
    }
    ldap.modifyAttributes(dn, mods);

    final Attributes a = ldap.getAttributes(dn, expected.getAttributeNames());
    AssertJUnit.assertEquals(expected, new LdapAttributes(a));
    ldap.close();
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"ldaptest"})
  public void saslExternalConnect()
    throws Exception
  {
    final Ldap ldap = TestUtil.createSaslExternalLdap();
    AssertJUnit.assertTrue(ldap.connect());
    ldap.close();
  }


  /**
   * @param  krb5Realm  kerberos realm
   * @param  krb5Kdc  kerberos kdc
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  filterArgs  to replace args in filter with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "krb5Realm",
      "krb5Kdc",
      "gssApiSearchDn",
      "gssApiSearchFilter",
      "gssApiSearchFilterArgs",
      "gssApiSearchReturnAttrs",
      "gssApiSearchResults"
    }
  )
  @Test(groups = {"ldaptest"})
  public void gssApiSearch(
    final String krb5Realm,
    final String krb5Kdc,
    final String dn,
    final String filter,
    final String filterArgs,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    System.setProperty(
      "java.security.auth.login.config",
      "src/test/resources/ldap_jaas.config");
    System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
    System.setProperty("java.security.krb5.realm", krb5Realm);
    System.setProperty("java.security.krb5.kdc", krb5Kdc);

    final Ldap ldap = TestUtil.createGssApiLdap();
    final Iterator<SearchResult> iter = ldap.search(
      dn,
      new SearchFilter(filter, filterArgs.split("\\|")),
      returnAttrs.split("\\|"));
    final String expected = TestUtil.readFileIntoString(ldifFile);
    AssertJUnit.assertEquals(
      TestUtil.convertLdifToEntry(expected),
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));
    ldap.close();

    System.clearProperty("java.security.auth.login.config");
    System.clearProperty("javax.security.auth.useSubjectCredsOnly");
    System.clearProperty("java.security.krb5.realm");
    System.clearProperty("java.security.krb5.kdc");
  }
}
/*
  $Id: LdapTest.java 268 2009-05-28 14:21:40Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 268 $
  Updated: $Date: 2009-05-28 07:21:40 -0700 (Thu, 28 May 2009) $
*/
package edu.vt.middleware.ldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.naming.Binding;
import javax.naming.NameClassPair;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;
import edu.vt.middleware.ldap.bean.LdapAttribute;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.handler.BinaryAttributeHandler;
import edu.vt.middleware.ldap.ldif.Ldif;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link Ldap}.
 *
 * @author  Middleware Services
 * @version  $Revision: 268 $
 */
public class LdapTest
{

  /** Invalid search filter. */
  public static final String INVALID_FILTER = "(cn=not-a-name)";

  /** Entry created for ldap tests. */
  private static LdapEntry testLdapEntry;

  /** Ldap instance for concurrency testing. */
  private Ldap singleLdap;


  /**
   * Default constructor.
   *
   * @throws  Exception  if ldap cannot be constructed
   */
  public LdapTest()
    throws Exception
  {
    this.singleLdap = TestUtil.createLdap();
  }


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "createEntry2" })
  @BeforeClass(groups = {"ldaptest"})
  public void createLdapEntry(final String ldifFile)
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
  @AfterClass(groups = {"ldaptest"})
  public void deleteLdapEntry()
    throws Exception
  {
    final Ldap ldap = TestUtil.createSetupLdap();
    ldap.delete(testLdapEntry.getDn());
    ldap.close();
  }


  /**
   * @param  createNew  whether to construct a new ldap instance.
   *
   * @return  <code>Ldap</code>
   *
   * @throws  Exception  On ldap construction failure.
   */
  public Ldap createLdap(final boolean createNew)
    throws Exception
  {
    if (createNew) {
      return TestUtil.createLdap();
    }
    return singleLdap;
  }


  /**
   * @param  dn  to compare.
   * @param  filter  to compare with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "compareDn", "compareFilter" })
  @Test(
    groups = {"ldaptest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void compare(final String dn, final String filter)
    throws Exception
  {
    final Ldap ldap = this.createLdap(false);
    AssertJUnit.assertFalse(ldap.compare(dn, INVALID_FILTER));
    AssertJUnit.assertTrue(ldap.compare(dn, filter));
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  filterArgs  to replace args in filter with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "searchDn",
      "searchFilter",
      "searchFilterArgs",
      "searchReturnAttrs",
      "searchResults"
    }
  )
  @Test(
    groups = {"ldaptest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void search(
    final String dn,
    final String filter,
    final String filterArgs,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final Ldap ldap = this.createLdap(false);
    // test searching
    Iterator<SearchResult> iter = ldap.search(
      dn,
      filter,
      filterArgs.split("\\|"),
      returnAttrs.split("\\|"));
    final String expected = TestUtil.readFileIntoString(ldifFile);
    AssertJUnit.assertEquals(
      TestUtil.convertLdifToEntry(expected),
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));
    // test searching without handler
    iter = ldap.search(
      dn,
      filter,
      filterArgs.split("\\|"),
      returnAttrs.split("\\|"),
      null);

    final LdapEntry entry = TestUtil.convertLdifToEntry(expected);
    entry.setDn(entry.getDn().substring(0, entry.getDn().indexOf(",")));
    AssertJUnit.assertEquals(
      entry,
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "searchAttributesDn",
      "searchAttributesFilter",
      "searchAttributesReturnAttrs",
      "searchAttributesResults"
    }
  )
  @Test(
    groups = {"ldaptest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void searchAttributes(
    final String dn,
    final String filter,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final String[] matchAttrs = filter.split("=");
    final Ldap ldap = this.createLdap(false);
    // test searching
    Iterator<SearchResult> iter = ldap.searchAttributes(
      dn,
      new BasicAttributes(matchAttrs[0], matchAttrs[1]),
      returnAttrs.split("\\|"));
    final String expected = TestUtil.readFileIntoString(ldifFile);
    AssertJUnit.assertEquals(
      TestUtil.convertLdifToEntry(expected),
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));
    // test searching without handler
    iter = ldap.searchAttributes(
      dn,
      new BasicAttributes(matchAttrs[0], matchAttrs[1]),
      returnAttrs.split("\\|"),
      null);

    final LdapEntry entry = TestUtil.convertLdifToEntry(expected);
    entry.setDn(entry.getDn().substring(0, entry.getDn().indexOf(",")));
    AssertJUnit.assertEquals(
      entry,
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));
  }


  /**
   * @param  dn  to search on.
   * @param  results  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "listDn", "listResults" })
  @Test(groups = {"ldaptest"})
  public void list(final String dn, final String results)
    throws Exception
  {
    final Ldap ldap = this.createLdap(true);
    final Iterator<NameClassPair> iter = ldap.list(dn);
    final List<String> l = new ArrayList<String>();
    while (iter.hasNext()) {
      final NameClassPair ncp = iter.next();
      l.add(ncp.getName());
    }

    final List<String> expected = Arrays.asList(results.split("\\|"));
    AssertJUnit.assertTrue(l.containsAll(expected));
    ldap.close();
  }


  /**
   * @param  dn  to search on.
   * @param  results  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "listBindingsDn", "listBindingsResults" })
  @Test(groups = {"ldaptest"})
  public void listBindings(final String dn, final String results)
    throws Exception
  {
    final Ldap ldap = this.createLdap(true);
    final Iterator<Binding> iter = ldap.listBindings(dn);
    final List<String> l = new ArrayList<String>();
    while (iter.hasNext()) {
      final Binding b = iter.next();
      l.add(b.getName());
    }

    final List<String> expected = Arrays.asList(results.split("\\|"));
    AssertJUnit.assertTrue(l.containsAll(expected));
    ldap.close();
  }


  /**
   * @param  dn  to search on.
   * @param  returnAttrs  to return from search.
   * @param  results  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "getAttributesDn",
      "getAttributesReturnAttrs",
      "getAttributesResults"
    }
  )
  @Test(
    groups = {"ldaptest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void getAttributes(
    final String dn,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    final Ldap ldap = this.createLdap(false);
    final Attributes attrs = ldap.getAttributes(dn, returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  dn  to search on.
   * @param  returnAttrs  to return from search.
   * @param  results  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "getAttributesBase64Dn",
      "getAttributesBase64ReturnAttrs",
      "getAttributesBase64Results"
    }
  )
  @Test(groups = {"ldaptest"})
  public void getAttributesBase64(
    final String dn,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    final Ldap ldap = this.createLdap(true);
    final Attributes attrs = ldap.getAttributes(
      dn,
      returnAttrs.split("\\|"),
      new BinaryAttributeHandler());
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
    ldap.close();
  }


  /**
   * @param  dn  to search on.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "getSchemaDn", "getSchemaResults" })
  @Test(groups = {"ldaptest"})
  public void getSchema(final String dn, final String ldifFile)
    throws Exception
  {
    final Ldap ldap = this.createLdap(true);
    final Iterator<SearchResult> iter = ldap.getSchema(dn);
    final String expected = TestUtil.readFileIntoString(ldifFile);
    AssertJUnit.assertEquals(
      TestUtil.convertLdifToResult(expected),
      TestUtil.convertLdifToResult((new Ldif()).createLdif(iter)));
    ldap.close();
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"ldaptest"},
    enabled = false
  )
  public void getSaslMechanisms()
    throws Exception
  {
    final Ldap ldap = this.createLdap(true);
    final String[] results = ldap.getSaslMechanisms();
    ldap.close();
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"ldaptest"},
    enabled = false
  )
  public void getSupportedControls()
    throws Exception
  {
    final Ldap ldap = this.createLdap(true);
    final String[] results = ldap.getSupportedControls();
    ldap.close();
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to add.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "addAttributeDn", "addAttributeAttribute" })
  @Test(groups = {"ldaptest"})
  public void addAttribute(final String dn, final String attrs)
    throws Exception
  {
    final LdapAttribute expected = TestUtil.convertStringToAttributes(attrs)
        .getAttributes().iterator().next();
    final Ldap ldap = this.createLdap(true);
    ldap.addAttribute(dn, expected.getName(), expected.getValues().toArray());

    final Attributes a = ldap.getAttributes(
      dn,
      new String[] {expected.getName()});
    AssertJUnit.assertEquals(
      expected,
      new LdapAttribute(a.get(expected.getName())));
    ldap.close();
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to add.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "addAttributesDn", "addAttributesAttributes" })
  @Test(groups = {"ldaptest"})
  public void addAttributes(final String dn, final String attrs)
    throws Exception
  {
    final LdapAttributes expected = TestUtil.convertStringToAttributes(attrs);
    final Ldap ldap = this.createLdap(true);
    ldap.addAttributes(dn, expected.toAttributes());

    final Attributes a = ldap.getAttributes(dn, expected.getAttributeNames());
    AssertJUnit.assertEquals(expected, new LdapAttributes(a));
    ldap.close();
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to replace.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "replaceAttributeDn", "replaceAttributeAttribute" })
  @Test(
    groups = {"ldaptest"},
    dependsOnMethods = {"addAttribute"}
  )
  public void replaceAttribute(final String dn, final String attrs)
    throws Exception
  {
    final LdapAttribute expected = TestUtil.convertStringToAttributes(attrs)
        .getAttributes().iterator().next();
    final Ldap ldap = this.createLdap(true);
    ldap.replaceAttribute(
      dn,
      expected.getName(),
      expected.getValues().toArray());

    final Attributes a = ldap.getAttributes(
      dn,
      new String[] {expected.getName()});
    AssertJUnit.assertEquals(
      expected,
      new LdapAttribute(a.get(expected.getName())));
    ldap.close();
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to replace.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "replaceAttributesDn", "replaceAttributesAttributes" })
  @Test(
    groups = {"ldaptest"},
    dependsOnMethods = {"addAttributes"}
  )
  public void replaceAttributes(final String dn, final String attrs)
    throws Exception
  {
    final LdapAttributes expected = TestUtil.convertStringToAttributes(attrs);
    final Ldap ldap = this.createLdap(true);
    ldap.replaceAttributes(dn, expected.toAttributes());

    final Attributes a = ldap.getAttributes(dn, expected.getAttributeNames());
    AssertJUnit.assertEquals(expected, new LdapAttributes(a));
    ldap.close();
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to remove.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "removeAttributeDn", "removeAttributeAttribute" })
  @Test(
    groups = {"ldaptest"},
    dependsOnMethods = {"replaceAttribute"}
  )
  public void removeAttribute(final String dn, final String attrs)
    throws Exception
  {
    final LdapAttribute expected = TestUtil.convertStringToAttributes(attrs)
        .getAttributes().iterator().next();
    final LdapAttribute remove = new LdapAttribute(expected);
    remove.getValues().remove(0);
    expected.getValues().remove(1);

    final Ldap ldap = this.createLdap(true);
    ldap.removeAttribute(dn, remove.getName(), remove.getValues().toArray());

    final Attributes a = ldap.getAttributes(
      dn,
      new String[] {expected.getName()});
    AssertJUnit.assertEquals(
      expected,
      new LdapAttribute(a.get(expected.getName())));
    ldap.close();
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to remove.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "removeAttributesDn", "removeAttributesAttributes" })
  @Test(
    groups = {"ldaptest"},
    dependsOnMethods = {"replaceAttributes"}
  )
  public void removeAttributes(final String dn, final String attrs)
    throws Exception
  {
    final LdapAttributes expected = TestUtil.convertStringToAttributes(attrs);
    final LdapAttributes remove = new LdapAttributes(expected);

    final String[] attrsName = remove.getAttributeNames();
    remove.getAttributes().remove(remove.getAttribute(attrsName[0]));
    expected.getAttributes().remove(expected.getAttribute(attrsName[1]));

    final Ldap ldap = this.createLdap(true);
    ldap.removeAttributes(dn, remove.toAttributes());

    final Attributes a = ldap.getAttributes(dn, expected.getAttributeNames());
    AssertJUnit.assertEquals(expected, new LdapAttributes(a));
    ldap.close();
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"ldaptest"})
  public void saslExternalConnect()
    throws Exception
  {
    final Ldap ldap = TestUtil.createSaslExternalLdap();
    AssertJUnit.assertTrue(ldap.connect());
    ldap.close();
  }


  /**
   * @param  krb5Realm  kerberos realm
   * @param  krb5Kdc  kerberos kdc
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  filterArgs  to replace args in filter with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "krb5Realm",
      "krb5Kdc",
      "gssApiSearchDn",
      "gssApiSearchFilter",
      "gssApiSearchFilterArgs",
      "gssApiSearchReturnAttrs",
      "gssApiSearchResults"
    }
  )
  @Test(groups = {"ldaptest"})
  public void gssApiSearch(
    final String krb5Realm,
    final String krb5Kdc,
    final String dn,
    final String filter,
    final String filterArgs,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    System.setProperty(
      "java.security.auth.login.config",
      "src/test/resources/ldap_jaas.config");
    System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
    System.setProperty("java.security.krb5.realm", krb5Realm);
    System.setProperty("java.security.krb5.kdc", krb5Kdc);

    final Ldap ldap = TestUtil.createGssApiLdap();
    final Iterator<SearchResult> iter = ldap.search(
      dn,
      filter,
      filterArgs.split("\\|"),
      returnAttrs.split("\\|"));
    final String expected = TestUtil.readFileIntoString(ldifFile);
    AssertJUnit.assertEquals(
      TestUtil.convertLdifToEntry(expected),
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));
    ldap.close();

    System.clearProperty("java.security.auth.login.config");
    System.clearProperty("javax.security.auth.useSubjectCredsOnly");
    System.clearProperty("java.security.krb5.realm");
    System.clearProperty("java.security.krb5.kdc");
  }
}
/*
  $Id: LdapTest.java 316 2009-07-02 22:06:46Z dfisher $

  Copyright (C) 2003-2008 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 316 $
  Updated: $Date: 2009-07-02 15:06:46 -0700 (Thu, 02 Jul 2009) $
*/
package edu.vt.middleware.ldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.naming.Binding;
import javax.naming.NameClassPair;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;
import edu.vt.middleware.ldap.bean.LdapAttribute;
import edu.vt.middleware.ldap.bean.LdapAttributes;
import edu.vt.middleware.ldap.bean.LdapEntry;
import edu.vt.middleware.ldap.handler.BinaryAttributeHandler;
import edu.vt.middleware.ldap.handler.EntryDnSearchResultHandler;
import edu.vt.middleware.ldap.handler.FqdnSearchResultHandler;
import edu.vt.middleware.ldap.handler.SearchResultHandler;
import edu.vt.middleware.ldap.ldif.Ldif;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link Ldap}.
 *
 * @author  Middleware Services
 * @version  $Revision: 316 $
 */
public class LdapTest
{

  /** Invalid search filter. */
  public static final String INVALID_FILTER = "(cn=not-a-name)";

  /** Entry created for ldap tests. */
  private static LdapEntry testLdapEntry;

  /** Ldap instance for concurrency testing. */
  private Ldap singleLdap;


  /**
   * Default constructor.
   *
   * @throws  Exception  if ldap cannot be constructed
   */
  public LdapTest()
    throws Exception
  {
    this.singleLdap = TestUtil.createLdap();
  }


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "createEntry2" })
  @BeforeClass(groups = {"ldaptest"})
  public void createLdapEntry(final String ldifFile)
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
  @AfterClass(groups = {"ldaptest"})
  public void deleteLdapEntry()
    throws Exception
  {
    final Ldap ldap = TestUtil.createSetupLdap();
    ldap.delete(testLdapEntry.getDn());
    ldap.close();
  }


  /**
   * @param  createNew  whether to construct a new ldap instance.
   *
   * @return  <code>Ldap</code>
   *
   * @throws  Exception  On ldap construction failure.
   */
  public Ldap createLdap(final boolean createNew)
    throws Exception
  {
    if (createNew) {
      return TestUtil.createLdap();
    }
    return singleLdap;
  }


  /**
   * @param  dn  to compare.
   * @param  filter  to compare with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "compareDn", "compareFilter" })
  @Test(
    groups = {"ldaptest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void compare(final String dn, final String filter)
    throws Exception
  {
    final Ldap ldap = this.createLdap(false);
    AssertJUnit.assertFalse(ldap.compare(dn, INVALID_FILTER));
    AssertJUnit.assertTrue(ldap.compare(dn, filter));
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  filterArgs  to replace args in filter with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "searchDn",
      "searchFilter",
      "searchFilterArgs",
      "searchReturnAttrs",
      "searchResults"
    }
  )
  @Test(
    groups = {"ldaptest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void search(
    final String dn,
    final String filter,
    final String filterArgs,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final Ldap ldap = this.createLdap(false);

    final String expected = TestUtil.readFileIntoString(ldifFile);
    final LdapEntry entry = TestUtil.convertLdifToEntry(expected);
    final LdapEntry shortDnEntry = TestUtil.convertLdifToEntry(expected);
    shortDnEntry.setDn(
      shortDnEntry.getDn().substring(0, shortDnEntry.getDn().indexOf(",")));

    final LdapEntry entryDnEntry = TestUtil.convertLdifToEntry(expected);
    entryDnEntry.getLdapAttributes().addAttribute(
      "entryDN",
      entryDnEntry.getDn());

    // test searching
    Iterator<SearchResult> iter = ldap.search(
      dn,
      filter,
      filterArgs.split("\\|"),
      returnAttrs.split("\\|"));
    AssertJUnit.assertEquals(
      entry,
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));

    // test searching without handler
    iter = ldap.search(
      dn,
      filter,
      filterArgs.split("\\|"),
      returnAttrs.split("\\|"),
      new SearchResultHandler[0]);
    AssertJUnit.assertEquals(
      shortDnEntry,
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));

    // test searching with multiple handlers
    final EntryDnSearchResultHandler srh = new EntryDnSearchResultHandler();
    iter = ldap.search(
      dn,
      filter,
      filterArgs.split("\\|"),
      returnAttrs.split("\\|"),
      new FqdnSearchResultHandler(),
      srh);
    AssertJUnit.assertEquals(
      entryDnEntry,
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));

    // test that entry dn handler is no-op if attribute name conflicts
    srh.setDnAttributeName("givenName");
    iter = ldap.search(
      dn,
      filter,
      filterArgs.split("\\|"),
      returnAttrs.split("\\|"),
      new FqdnSearchResultHandler(),
      srh);
    AssertJUnit.assertEquals(
      entry,
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "searchAttributesDn",
      "searchAttributesFilter",
      "searchAttributesReturnAttrs",
      "searchAttributesResults"
    }
  )
  @Test(
    groups = {"ldaptest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void searchAttributes(
    final String dn,
    final String filter,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final String[] matchAttrs = filter.split("=");
    final Ldap ldap = this.createLdap(false);
    // test searching
    Iterator<SearchResult> iter = ldap.searchAttributes(
      dn,
      new BasicAttributes(matchAttrs[0], matchAttrs[1]),
      returnAttrs.split("\\|"));
    final String expected = TestUtil.readFileIntoString(ldifFile);
    AssertJUnit.assertEquals(
      TestUtil.convertLdifToEntry(expected),
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));
    // test searching without handler
    iter = ldap.searchAttributes(
      dn,
      new BasicAttributes(matchAttrs[0], matchAttrs[1]),
      returnAttrs.split("\\|"),
      new SearchResultHandler[0]);

    final LdapEntry entry = TestUtil.convertLdifToEntry(expected);
    entry.setDn(entry.getDn().substring(0, entry.getDn().indexOf(",")));
    AssertJUnit.assertEquals(
      entry,
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));
  }


  /**
   * @param  dn  to search on.
   * @param  results  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "listDn", "listResults" })
  @Test(groups = {"ldaptest"})
  public void list(final String dn, final String results)
    throws Exception
  {
    final Ldap ldap = this.createLdap(true);
    final Iterator<NameClassPair> iter = ldap.list(dn);
    final List<String> l = new ArrayList<String>();
    while (iter.hasNext()) {
      final NameClassPair ncp = iter.next();
      l.add(ncp.getName());
    }

    final List<String> expected = Arrays.asList(results.split("\\|"));
    AssertJUnit.assertTrue(l.containsAll(expected));
    ldap.close();
  }


  /**
   * @param  dn  to search on.
   * @param  results  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "listBindingsDn", "listBindingsResults" })
  @Test(groups = {"ldaptest"})
  public void listBindings(final String dn, final String results)
    throws Exception
  {
    final Ldap ldap = this.createLdap(true);
    final Iterator<Binding> iter = ldap.listBindings(dn);
    final List<String> l = new ArrayList<String>();
    while (iter.hasNext()) {
      final Binding b = iter.next();
      l.add(b.getName());
    }

    final List<String> expected = Arrays.asList(results.split("\\|"));
    AssertJUnit.assertTrue(l.containsAll(expected));
    ldap.close();
  }


  /**
   * @param  dn  to search on.
   * @param  returnAttrs  to return from search.
   * @param  results  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "getAttributesDn",
      "getAttributesReturnAttrs",
      "getAttributesResults"
    }
  )
  @Test(
    groups = {"ldaptest"},
    threadPoolSize = 10,
    invocationCount = 100,
    timeOut = 60000
  )
  public void getAttributes(
    final String dn,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    final Ldap ldap = this.createLdap(false);
    final Attributes attrs = ldap.getAttributes(dn, returnAttrs.split("\\|"));
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
  }


  /**
   * @param  dn  to search on.
   * @param  returnAttrs  to return from search.
   * @param  results  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "getAttributesBase64Dn",
      "getAttributesBase64ReturnAttrs",
      "getAttributesBase64Results"
    }
  )
  @Test(groups = {"ldaptest"})
  public void getAttributesBase64(
    final String dn,
    final String returnAttrs,
    final String results)
    throws Exception
  {
    final Ldap ldap = this.createLdap(true);
    final Attributes attrs = ldap.getAttributes(
      dn,
      returnAttrs.split("\\|"),
      new BinaryAttributeHandler());
    final LdapAttributes expected = TestUtil.convertStringToAttributes(results);
    AssertJUnit.assertEquals(expected, new LdapAttributes(attrs));
    ldap.close();
  }


  /**
   * @param  dn  to search on.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "getSchemaDn", "getSchemaResults" })
  @Test(groups = {"ldaptest"})
  public void getSchema(final String dn, final String ldifFile)
    throws Exception
  {
    final Ldap ldap = this.createLdap(true);
    final Iterator<SearchResult> iter = ldap.getSchema(dn);
    final String expected = TestUtil.readFileIntoString(ldifFile);
    AssertJUnit.assertEquals(
      TestUtil.convertLdifToResult(expected),
      TestUtil.convertLdifToResult((new Ldif()).createLdif(iter)));
    ldap.close();
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"ldaptest"},
    enabled = false
  )
  public void getSaslMechanisms()
    throws Exception
  {
    final Ldap ldap = this.createLdap(true);
    final String[] results = ldap.getSaslMechanisms();
    ldap.close();
  }


  /** @throws  Exception  On test failure. */
  @Test(
    groups = {"ldaptest"},
    enabled = false
  )
  public void getSupportedControls()
    throws Exception
  {
    final Ldap ldap = this.createLdap(true);
    final String[] results = ldap.getSupportedControls();
    ldap.close();
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to add.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "addAttributeDn", "addAttributeAttribute" })
  @Test(groups = {"ldaptest"})
  public void addAttribute(final String dn, final String attrs)
    throws Exception
  {
    final LdapAttribute expected = TestUtil.convertStringToAttributes(attrs)
        .getAttributes().iterator().next();
    final Ldap ldap = this.createLdap(true);
    ldap.addAttribute(dn, expected.getName(), expected.getValues().toArray());

    final Attributes a = ldap.getAttributes(
      dn,
      new String[] {expected.getName()});
    AssertJUnit.assertEquals(
      expected,
      new LdapAttribute(a.get(expected.getName())));
    ldap.close();
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to add.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "addAttributesDn", "addAttributesAttributes" })
  @Test(groups = {"ldaptest"})
  public void addAttributes(final String dn, final String attrs)
    throws Exception
  {
    final LdapAttributes expected = TestUtil.convertStringToAttributes(attrs);
    final Ldap ldap = this.createLdap(true);
    ldap.addAttributes(dn, expected.toAttributes());

    final Attributes a = ldap.getAttributes(dn, expected.getAttributeNames());
    AssertJUnit.assertEquals(expected, new LdapAttributes(a));
    ldap.close();
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to replace.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "replaceAttributeDn", "replaceAttributeAttribute" })
  @Test(
    groups = {"ldaptest"},
    dependsOnMethods = {"addAttribute"}
  )
  public void replaceAttribute(final String dn, final String attrs)
    throws Exception
  {
    final LdapAttribute expected = TestUtil.convertStringToAttributes(attrs)
        .getAttributes().iterator().next();
    final Ldap ldap = this.createLdap(true);
    ldap.replaceAttribute(
      dn,
      expected.getName(),
      expected.getValues().toArray());

    final Attributes a = ldap.getAttributes(
      dn,
      new String[] {expected.getName()});
    AssertJUnit.assertEquals(
      expected,
      new LdapAttribute(a.get(expected.getName())));
    ldap.close();
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to replace.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "replaceAttributesDn", "replaceAttributesAttributes" })
  @Test(
    groups = {"ldaptest"},
    dependsOnMethods = {"addAttributes"}
  )
  public void replaceAttributes(final String dn, final String attrs)
    throws Exception
  {
    final LdapAttributes expected = TestUtil.convertStringToAttributes(attrs);
    final Ldap ldap = this.createLdap(true);
    ldap.replaceAttributes(dn, expected.toAttributes());

    final Attributes a = ldap.getAttributes(dn, expected.getAttributeNames());
    AssertJUnit.assertEquals(expected, new LdapAttributes(a));
    ldap.close();
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to remove.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "removeAttributeDn", "removeAttributeAttribute" })
  @Test(
    groups = {"ldaptest"},
    dependsOnMethods = {"replaceAttribute"}
  )
  public void removeAttribute(final String dn, final String attrs)
    throws Exception
  {
    final LdapAttribute expected = TestUtil.convertStringToAttributes(attrs)
        .getAttributes().iterator().next();
    final LdapAttribute remove = new LdapAttribute(expected);
    remove.getValues().remove(0);
    expected.getValues().remove(1);

    final Ldap ldap = this.createLdap(true);
    ldap.removeAttribute(dn, remove.getName(), remove.getValues().toArray());

    final Attributes a = ldap.getAttributes(
      dn,
      new String[] {expected.getName()});
    AssertJUnit.assertEquals(
      expected,
      new LdapAttribute(a.get(expected.getName())));
    ldap.close();
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to remove.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "removeAttributesDn", "removeAttributesAttributes" })
  @Test(
    groups = {"ldaptest"},
    dependsOnMethods = {"replaceAttributes"}
  )
  public void removeAttributes(final String dn, final String attrs)
    throws Exception
  {
    final LdapAttributes expected = TestUtil.convertStringToAttributes(attrs);
    final LdapAttributes remove = new LdapAttributes(expected);

    final String[] attrsName = remove.getAttributeNames();
    remove.getAttributes().remove(remove.getAttribute(attrsName[0]));
    expected.getAttributes().remove(expected.getAttribute(attrsName[1]));

    final Ldap ldap = this.createLdap(true);
    ldap.removeAttributes(dn, remove.toAttributes());

    final Attributes a = ldap.getAttributes(dn, expected.getAttributeNames());
    AssertJUnit.assertEquals(expected, new LdapAttributes(a));
    ldap.close();
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"ldaptest"})
  public void saslExternalConnect()
    throws Exception
  {
    final Ldap ldap = TestUtil.createSaslExternalLdap();
    AssertJUnit.assertTrue(ldap.connect());
    ldap.close();
  }


  /**
   * @param  krb5Realm  kerberos realm
   * @param  krb5Kdc  kerberos kdc
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  filterArgs  to replace args in filter with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "krb5Realm",
      "krb5Kdc",
      "gssApiSearchDn",
      "gssApiSearchFilter",
      "gssApiSearchFilterArgs",
      "gssApiSearchReturnAttrs",
      "gssApiSearchResults"
    }
  )
  @Test(groups = {"ldaptest"})
  public void gssApiSearch(
    final String krb5Realm,
    final String krb5Kdc,
    final String dn,
    final String filter,
    final String filterArgs,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    System.setProperty(
      "java.security.auth.login.config",
      "src/test/resources/ldap_jaas.config");
    System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
    System.setProperty("java.security.krb5.realm", krb5Realm);
    System.setProperty("java.security.krb5.kdc", krb5Kdc);

    final Ldap ldap = TestUtil.createGssApiLdap();
    final Iterator<SearchResult> iter = ldap.search(
      dn,
      filter,
      filterArgs.split("\\|"),
      returnAttrs.split("\\|"));
    final String expected = TestUtil.readFileIntoString(ldifFile);
    AssertJUnit.assertEquals(
      TestUtil.convertLdifToEntry(expected),
      TestUtil.convertLdifToEntry((new Ldif()).createLdif(iter)));
    ldap.close();

    System.clearProperty("java.security.auth.login.config");
    System.clearProperty("javax.security.auth.useSubjectCredsOnly");
    System.clearProperty("java.security.krb5.realm");
    System.clearProperty("java.security.krb5.kdc");
  }
}
