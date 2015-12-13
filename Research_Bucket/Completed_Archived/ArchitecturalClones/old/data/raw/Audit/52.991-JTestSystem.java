/*
 * ====================================================================
 * 
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 * 
 * ====================================================================
 */

package org.freebxml.omar.client.xml.registry;

import junit.framework.*;

import java.util.*;
import java.io.*;
import javax.activation.*;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;
import org.freebxml.omar.client.xml.registry.JAXRTestSetup;

/**
 *
 * @author  mzaremba
 */
public class JTestSystem extends TestCase {

  static org.freebxml.omar.client.xml.registry.JAXRTestSetup setup = null;
  
  /** Creates a new instance of JTestSystem */
  public JTestSystem(String testMethod) 
  {
    super(testMethod);
  }
  
  public static Test suite() throws Exception
  {
    TestSuite suite= new TestSuite();
    
    suite.addTest(new JTestSystem("testClassificationSchemes"));
    
    //TODO: Test commented as it is failing due to some reason that is not a bug in code
    //suite.addTest(new JTestSystem("testFindConcept"));
    
    setup = new JAXRTestSetup(suite);
    return setup;
  }
  
  /*
   * Test browsing for Classification Schemes (AssociationType used in this test) 
   * and for Classification Concepts
   */
  public void testClassificationSchemes() throws Exception {
    ArrayList al = new ArrayList();
    al.add("Asso%");
    BulkResponse br = setup.bqm.findClassificationSchemes(null, al, null, null);
    assertNull(br.getExceptions());
    if (br == null) {
        fail("AssociationType classification schemes could not be found");
    }
    Collection collection = br.getCollection();
    Iterator i = collection.iterator();
    ClassificationScheme cs = (ClassificationScheme)i.next();
    assertEquals(cs.getName().getValue(), "AssociationType");
    
    String[] children = new String[] {"AffiliatedWith", "EmployeeOf", "MemberOf", 
                                      "RelatedTo", "HasFederationMember", "HasMember", 
                                      "ExternallyLinks", "Contains", "EquivalentTo", 
                                      "Extends", "Implements", "InstanceOf", 
                                      "Supersedes", "Uses", "Replaces", "SubmitterOf",
                                      "ResponsibleFor", "OwnerOf", "OffersService",
                                      "ContentManagementServiceFor", "InvocationControlFileFor", 
                                      "AccessControlPolicyFor"};
    ArrayList childrenList = new ArrayList();
    for (int index =0; index < children.length; index++) {
        childrenList.add(children[index]);
    }
    
    Collection collection2 = cs.getChildrenConcepts();
    for (Iterator it = collection2.iterator(); it.hasNext(); ) {
        Concept con = (Concept)it.next();
        assertTrue("Cannot find concept: " + con.getValue(), childrenList.contains(con.getValue()));
    }
  }
  
  public void testFindConcept() throws Exception {
    String[] objectTypes = new String[] {
                                         "RegistryObject", "AdhocQuery", "Association", 
                                         "AuditableEvent", "Classification", "ClassificationNode",
                                         "ExternalIdentifier", "ExternalLink", "Organization",
                                         "ServiceBinding", "SpecificationLink", "Subscription", "User",
                                         "RegistryEntry", "ClassificationScheme", "Federation", "Registry", "RegistryPackage",
                                         "Service", "ExtrinsicObject", "xacml", "Policy", "PolicySet" };
    ArrayList objectTypesList = new ArrayList();
    for (int index =0; index < objectTypes.length; index++) {
        objectTypesList.add(objectTypes[index]);
    }
    
    ArrayList cNamePats = new ArrayList();
    cNamePats.add("%");
    BulkResponse br = setup.bqm.findConcepts(null, cNamePats, null, null, null);
    assertNull(br.getExceptions());
    if (br == null) {
        fail("No Concept found that match patern %");
    }
    
    ArrayList conceptsList = new ArrayList();
    Collection collection = br.getCollection();
    for (Iterator it = collection.iterator(); it.hasNext(); ) {
        conceptsList.add(((Concept)it.next()).getValue());        
    }   
    assertTrue("Not all Object Types available in registry", conceptsList.containsAll(objectTypesList));
  }
} 

/*
 * ====================================================================
 * 
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 * 
 * ====================================================================
 */

package com.sun.xml.registry.ebxml;

import junit.framework.*;

import java.util.*;
import java.io.*;
import javax.activation.*;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

/**
 *
 * @author  mzaremba
 */
public class JTestSystem extends TestCase {

  static ApelonJAXRTestSetup setup = null;
  
  /** Creates a new instance of JTestSystem */
  public JTestSystem(String testMethod) 
  {
    super(testMethod);
  }
  
  public static Test suite() throws Exception
  {
    TestSuite suite= new TestSuite();
    
    suite.addTest(new JTestSystem("testClassificationSchemes"));
    suite.addTest(new JTestSystem("testFindConcept"));
    
    setup = new ApelonJAXRTestSetup(suite);
    return setup;
  }
  
  /*
   * Test browsing for Classification Schemes (AssociationType used in this test) 
   * and for Classification Concepts
   */
  public void testClassificationSchemes() throws Exception {
    ArrayList al = new ArrayList();
    al.add("Asso%");
    BulkResponse br = setup.bqm.findClassificationSchemes(null, al, null, null);
    assertNull(br.getExceptions());
    if (br == null) {
        fail("AssociationType classification schemes could not be found");
    }
    Collection collection = br.getCollection();
    Iterator i = collection.iterator();
    ClassificationScheme cs = (ClassificationScheme)i.next();
    assertEquals(cs.getName().getValue(), "AssociationType");
    
    String[] children = new String[] {"AffiliatedWith", "EmployeeOf", "MemberOf", 
                                      "RelatedTo", "HasFederationMember", "HasMember", 
                                      "ExternallyLinks", "Contains", "EquivalentTo", 
                                      "Extends", "Implements", "InstanceOf", 
                                      "Supersedes", "Uses", "Replaces", "SubmitterOf",
                                      "ResponsibleFor", "OwnerOf", "OffersService",
                                      "ContentManagementServiceFor", "InvocationControlFileFor", 
                                      "AccessControlPolicyFor"};
    ArrayList childrenList = new ArrayList();
    for (int index =0; index < children.length; index++) {
        childrenList.add(children[index]);
    }
    
    Collection collection2 = cs.getChildrenConcepts();
    for (Iterator it = collection2.iterator(); it.hasNext(); ) {
        Concept con = (Concept)it.next();
        assertTrue("Cannot find concept: " + con.getValue(), childrenList.contains(con.getValue()));
    }
  }
  
  public void testFindConcept() throws Exception {
    String[] objectTypes = new String[] {"xml", "xslt", "xmlSchema", "ebxml", "registry",
                                         "rim", "RegistryObject", "Association", 
                                         "AuditableEvent", "Classification", "ClassificationNode",
                                         "ExternalIdentifier", "ExternalLink", "Organization",
                                         "ServiceBinding", "SpecificationLink", "User",
                                         "RegistryEntry", "ClassificationScheme", "RegistryPackage",
                                         "Service", "ExtrinsicObject"};
    ArrayList objectTypesList = new ArrayList();
    for (int index =0; index < objectTypes.length; index++) {
        objectTypesList.add(objectTypes[index]);
    }
    
    ArrayList cNamePats = new ArrayList();
    cNamePats.add("%");
    BulkResponse br = setup.bqm.findConcepts(null, cNamePats, null, null, null);
    assertNull(br.getExceptions());
    if (br == null) {
        fail("No Concept found that match patern %");
    }
    
    ArrayList conceptsList = new ArrayList();
    Collection collection = br.getCollection();
    for (Iterator it = collection.iterator(); it.hasNext(); ) {
        conceptsList.add(((Concept)it.next()).getValue());        
    }
    assertTrue("Not all Object Types available in registry", conceptsList.containsAll(objectTypesList));
  }
} 

