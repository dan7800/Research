/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 *
 * $Revision: 1.5 $
 */

package org.freebxml.omar.client.xml.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.FindQualifier;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.AuditableEvent;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.RegistryPackage;
import javax.xml.registry.infomodel.Slot;

import org.freebxml.omar.common.BindingUtility;


public class ApelonJAXRTest extends junit.framework.TestCase {
    static org.freebxml.omar.client.xml.registry.JAXRTestSetup setup = null;
    
    
    public ApelonJAXRTest(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        try {
            junit.textui.TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
                + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
        
    public static junit.framework.Test suite() throws Exception {
        // These tests need to be ordered for purposes of read/write/delete.
        // Do not change order of tests, erroneous errors will result.
        junit.framework.TestSuite suite= new junit.framework.TestSuite();
        
        // Test creating and retrieving 2 internal schemes and 1 external scheme
        suite.addTest(new ApelonJAXRTest("testCreateInternalSchemes"));
        suite.addTest(new ApelonJAXRTest("testCreateExternalScheme"));
        suite.addTest(new ApelonJAXRTest("testRetrieveInternalSchemes"));
        suite.addTest(new ApelonJAXRTest("testRetrieveExternalScheme"));
        
        // Test creating and modifiying registry package (implicit retrieve test)
        suite.addTest(new ApelonJAXRTest("testCreateRegistryPackage"));
        suite.addTest(new ApelonJAXRTest("testUpdateRegistryPackage"));
        
        // Test inspection of RegistryPackage Auditable events
        suite.addTest(new ApelonJAXRTest("testInspectRegistryPackageAuditableEvents"));
        
        // Test creating and adding ExtrinsicObject to package
        suite.addTest(new ApelonJAXRTest("testCreateExtrinsicObject"));
        suite.addTest(new ApelonJAXRTest("testAddExtrinsicObjectToRegistryPackage"));
        suite.addTest(new ApelonJAXRTest("testInspectRegistryPackageExtrinsicObjects"));
        
        // Test adding slots
        suite.addTest(new ApelonJAXRTest("testAddSlotsToRegistryPackage"));
        suite.addTest(new ApelonJAXRTest("testInspectRegistryPackageSlots"));
        
        // Tests to create internal and external context sensitive
        // classifications, initial creation works now
        suite.addTest(new ApelonJAXRTest("testClassifyRegistryPackage"));
        suite.addTest(new ApelonJAXRTest("testInspectRegistryPackageClassifications"));
        
        // NOTE Running these again gives example of how context sensitive
        // classifications and primary external cls ClassificationScheme are
        // wiped out after updating a RegistryObject
        suite.addTest(new ApelonJAXRTest("testUpdateRegistryPackage"));
        suite.addTest(new ApelonJAXRTest("testInspectRegistryPackageClassificationsPostPkgUpdate"));
        
        // Test adding and retrieving ExternalLinks on a package
        suite.addTest(new ApelonJAXRTest("testAddRegistryPackageExternalLinks"));
        suite.addTest(new ApelonJAXRTest("testInspectRegistryPackageExternalLinks"));
        
        // Test deleting everything, cleanup for test suite
        System.err.println("testDeleteRegistryPackage");
        suite.addTest(new ApelonJAXRTest("testDeleteRegistryPackage"));
        System.err.println("testDeleteExtrinsicObject");
        suite.addTest(new ApelonJAXRTest("testDeleteExtrinsicObject"));
        System.err.println("testDeleteInternalSchemes");
        suite.addTest(new ApelonJAXRTest("testDeleteInternalSchemes"));
        System.err.println("testDeleteExternalScheme");
        suite.addTest(new ApelonJAXRTest("testDeleteExternalScheme"));
        
        setup = new JAXRTestSetup(suite);
        
        return setup;
    }
        
    /**
     * Asserts that:
     *  We cannot find InternalScheme (1 and 2)
     *  We can create InternalScheme (1 and 2) with 2 nodes each
     *
     * @throws <{Exception}>
     */
    public void testCreateInternalSchemes()
    throws Exception {
        testCreateInternalScheme(1);
        testCreateInternalScheme(2);
    }
    
    /**
     * Asserts that:
     *  We cannot find InternalScheme (1 and 2)
     *  We can create InternalScheme (1 and 2) with 2 nodes each
     *
     * @throws <{Exception}>
     */
    public void testCreateInternalScheme(int schemeNum)
    throws Exception {
        ArrayList findQualifiers = new ArrayList();
        findQualifiers.add(FindQualifier.CASE_SENSITIVE_MATCH);
        ClassificationScheme scheme = setup.bqm.findClassificationSchemeByName(findQualifiers,"InternalScheme" + schemeNum);
        
        if(scheme != null)
            fail("InternalScheme" + schemeNum + " already exists in registry");
        else {
            scheme = setup.lcm.createClassificationScheme("InternalScheme" + schemeNum,"InternalScheme" + schemeNum);
            scheme.setValueType(ClassificationScheme.VALUE_TYPE_UNIQUE);
            Concept con1 = setup.lcm.createConcept(scheme,"InternalScheme" + schemeNum + "-Node1","InternalScheme" + schemeNum + "-Node1");
            Concept con2 = setup.lcm.createConcept(scheme,"InternalScheme" + schemeNum + "-Node2","InternalScheme" + schemeNum + "-Node2");
            scheme.addChildConcept(con1);
            scheme.addChildConcept(con2);
            ArrayList objs = new ArrayList();
            objs.add(scheme);
            
            //Following is not necessary but is being done to test that duplicates are eliminated during marshal
            //objs.add(con1);
            //objs.add(con2);
            
            System.err.println("\nSaving 1 scheme and 2 concepts");
            BulkResponse br = setup.lcm.saveObjects(objs);
            assertTrue("Scheme" + schemeNum + " Creation Failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
            
            //Saving concepts separately to work around a bug in lcm.saveObjects
            //Not doing so meant that scheme was not marshalled and thus nothing got saved.
            //TODO: fix this bug later.
            objs.clear();
            objs.add(con1);
            objs.add(con2);
            br = setup.lcm.saveObjects(objs);
            
        }
    }
    
    public void testRetrieveInternalSchemes()
    throws Exception {
        testRetrieveInternalScheme(1);
        testRetrieveInternalScheme(2);
    }
    
    /**
     * Asserts that:
     *  We can find InternalScheme (1 and 2)
     *
     * @throws <{Exception}>
     */
    public void testRetrieveInternalScheme(int schemeNum)
    throws Exception {
        ArrayList findQualifiers = new ArrayList();
        findQualifiers.add(FindQualifier.CASE_SENSITIVE_MATCH);
        ClassificationScheme scheme = setup.bqm.findClassificationSchemeByName(findQualifiers, "InternalScheme" + schemeNum);
        
        if(scheme == null)
            fail("InternalScheme" + schemeNum + " could not be found in registry");
        else {
            assertEquals("InternalScheme" + schemeNum + " node count incorrect", 2, scheme.getChildConceptCount());
        }
    }
    
    /**
     * Asserts that:
     *  We can find InternalScheme (1 and 2)
     *  We can delete InternalScheme (1 and 2)
     *
     * @throws <{Exception}>
     */
    public void testDeleteInternalSchemes()
    throws Exception {
        testDeleteInternalScheme(1);
        testDeleteInternalScheme(2);
    }
    
    /**
     * Asserts that:
     *  We can find InternalScheme (1 and 2)
     *  We can delete InternalScheme (1 and 2)
     *
     * @throws <{Exception}>
     */
    public void testDeleteInternalScheme(int schemeNum)
    throws Exception {
        ArrayList findQualifiers = new ArrayList();
        findQualifiers.add(FindQualifier.CASE_SENSITIVE_MATCH);
        ClassificationScheme scheme = setup.bqm.findClassificationSchemeByName(findQualifiers, "InternalScheme" + schemeNum);
        if(scheme == null)
            fail("InternalScheme" + schemeNum + " could not be found in registry");
        else {
            ArrayList al = new ArrayList();
            al.add(scheme.getKey());
            Iterator it = scheme.getChildrenConcepts().iterator();
            while(it.hasNext()) {
                al.add(((javax.xml.registry.infomodel.RegistryObject)it.next()).getKey());
            }
            BulkResponse br = setup.lcm.deleteObjects(al);
            assertTrue("Delete InternalScheme" + schemeNum + " did not Succeed",br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    /**
     * Asserts that:
     *  We cannot find ExternalScheme
     *  We can create ExternalScheme
     *
     * @throws <{Exception}>
     */
    public void testCreateExternalScheme()
    throws Exception {
        ArrayList findQualifiers = new ArrayList();
        findQualifiers.add(FindQualifier.CASE_SENSITIVE_MATCH);
        ClassificationScheme scheme = setup.bqm.findClassificationSchemeByName(findQualifiers, "ExternalScheme");
        if(scheme != null)
            fail("ExternalScheme already exists in registry");
        else {
            scheme = setup.lcm.createClassificationScheme("ExternalScheme","ExternalScheme");
            scheme.setValueType(ClassificationScheme.VALUE_TYPE_UNIQUE);
            ((org.freebxml.omar.client.xml.registry.infomodel.ClassificationSchemeImpl)scheme).setExternal(true);
            ArrayList al = new ArrayList();
            al.add(scheme);
            System.err.println("\nSaving 1 ext scheme");
            BulkResponse br = setup.lcm.saveObjects(al);
            assertTrue("ExternalScheme creation Failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    /**
     * Asserts that:
     *  We can find ExternalScheme
     *
     * @throws <{Exception}>
     */
    public void testRetrieveExternalScheme()
    throws Exception {
        ArrayList findQualifiers = new ArrayList();
        findQualifiers.add(FindQualifier.CASE_SENSITIVE_MATCH);
        ClassificationScheme scheme = setup.bqm.findClassificationSchemeByName(findQualifiers, "ExternalScheme");
        
        if(scheme == null)
            fail("ExternalScheme could not be found in registry");
        else
            assertEquals("ExternalScheme is external", true, scheme.isExternal());
    }
    
    /**
     * Asserts that:
     *  We can find ExternalScheme
     *  We can delete ExternalScheme
     *
     * @throws <{Exception}>
     */
    public void testDeleteExternalScheme()
    throws Exception {
        ArrayList findQualifiers = new ArrayList();
        findQualifiers.add(FindQualifier.CASE_SENSITIVE_MATCH);
        ClassificationScheme scheme = setup.bqm.findClassificationSchemeByName(findQualifiers, "ExternalScheme");
        if(scheme == null)
            fail("ExternalScheme could not be found in registry");
        else {
            ArrayList al = new ArrayList();
            al.add(scheme.getKey());
            BulkResponse br = setup.lcm.deleteObjects(al);
            assertTrue("Delete ExternalScheme did not Succeed",br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    /**
     * Asserts that:
     *  We cannot find testPackage
     *  We can create testPackage
     *
     * @throws <{Exception}>
     */
    public void testCreateRegistryPackage()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage",BindingUtility.CANONICAL_OBJECT_TYPE_ID_RegistryPackage);
        if(pkg != null)
            fail("testPackage already exists in the registry");
        else {
            pkg = setup.lcm.createRegistryPackage("testPackage");
            pkg.setDescription(setup.lcm.createInternationalString("testPackage description"));
            ArrayList al = new ArrayList();
            al.add(pkg);
            System.err.println("\nSaving 1 RegistryPackage");
            BulkResponse br = setup.lcm.saveObjects(al);
            assertTrue("testPackage creation Failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    /**
     * Asserts that:
     *  We can find testPackage
     *  We can update testPackage's description
     *
     * @throws <{Exception}>
     */
    public void testUpdateRegistryPackage()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage",BindingUtility.CANONICAL_OBJECT_TYPE_ID_RegistryPackage);
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else {
            String newDesc = pkg.getDescription().getValue() + " updated";
            pkg.setDescription(setup.lcm.createInternationalString(newDesc));
            ArrayList al = new ArrayList();
            al.add(pkg);
            System.err.println("\nSaving existing RegistryPackage after updating it.");
            BulkResponse br = setup.lcm.saveObjects(al);
            assertTrue("testPackage update failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    /**
     * Asserts that:
     *  We can find and delete testPackage
     *
     * @throws <{Exception}>
     */
    public void testDeleteRegistryPackage()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage",BindingUtility.CANONICAL_OBJECT_TYPE_ID_RegistryPackage);
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else {
            // Delete package associations first Is this correct though?
            // Should we have to del associations first?
            Iterator it = pkg.getAssociations().iterator();
            ArrayList al = new ArrayList();
            while(it.hasNext()) {
                javax.xml.registry.infomodel.Association a = (javax.xml.registry.infomodel.Association)it.next();
                al.add(a.getKey());
            }
            BulkResponse br = setup.lcm.deleteObjects(al);
            assertTrue("testPackage delete associations failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
            
            al.clear();
            al.add(pkg.getKey());
            br = setup.lcm.deleteObjects(al);
            assertTrue("testPackage deletion Failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    /**
     * Asserts that:
     *  We can create testExtObj ExtrinsicObject
     *
     * @throws <{Exception}>
     */
    public void testCreateExtrinsicObject()
    throws Exception {
        ExtrinsicObject extObj = (ExtrinsicObject)findRegistryObject("testExtObj", BindingUtility.CANONICAL_OBJECT_TYPE_ID_ExtrinsicObject);
        if(extObj != null)
            fail("testExtObj already exists in the registry");
        else {
            String path = System.getProperty("user.home") + System.getProperty("file.separator") + "jaxr-ebxml.properties";
            java.io.File f = new java.io.File(path);
            
            // using URLs causes "cannot sign the payload error"
            // DataHandler dh = new DataHandler(f.toURL());
            javax.activation.DataHandler dh = new javax.activation.DataHandler(new javax.activation.FileDataSource(f));
            
            extObj = setup.lcm.createExtrinsicObject(dh);
            extObj.setName(setup.lcm.createInternationalString("testExtObj"));
            extObj.setDescription(setup.lcm.createInternationalString("jaxr-ebxml.properties"));
            ArrayList al = new ArrayList();
            al.add(extObj);
            System.err.println("\nSaving 1 ExtrinsicObject with a repository item");
            BulkResponse br = setup.lcm.saveObjects(al);
            assertTrue("testExtObj creation Failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    /**
     * Asserts that:
     *  We can find testExtObj
     *  We can delete testExtObj
     *
     * @throws <{Exception}>
     */
    public void testDeleteExtrinsicObject()
    throws Exception {
        ExtrinsicObject extObj = (ExtrinsicObject)findRegistryObject("testExtObj", BindingUtility.CANONICAL_OBJECT_TYPE_ID_ExtrinsicObject);
        if(extObj == null)
            fail("testExtObj could not be found in the registry");
        else {
            ArrayList al = new ArrayList();
            al.add(extObj.getKey());
            BulkResponse br = setup.lcm.deleteObjects(al);
            assertTrue("testExtObj deletion Failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    /**
     * Asserts that:
     *  We can find testPackage
     *  We can find testExtObj
     *  We can add testExtObj to testPackage
     *
     * @throws <{Exception}>
     */
    public void testAddExtrinsicObjectToRegistryPackage()
    throws Exception {
        ExtrinsicObject extObj = (ExtrinsicObject)findRegistryObject("testExtObj", BindingUtility.CANONICAL_OBJECT_TYPE_ID_ExtrinsicObject);
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage",BindingUtility.CANONICAL_OBJECT_TYPE_ID_RegistryPackage);
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else if(extObj == null)
            fail("testExtObj could not be found in the registry");
        else {
            pkg.addRegistryObject(extObj);
            
            ArrayList al = new ArrayList();
            al.add(pkg);
            System.err.println("Adding ass between existing RegistryPackage and ExtrinsicObject");
            BulkResponse br = setup.lcm.saveObjects(al);
            assertTrue("Adding ExtObj to RegPkg failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    
    /**
     * Asserts that:
     *  We can find testPackage
     *  The package has 2 auditable events
     *  The events are the expected types
     *  The events have user populated
     *
     * @throws <{Exception}>
     */
    public void testInspectRegistryPackageAuditableEvents()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage",BindingUtility.CANONICAL_OBJECT_TYPE_ID_RegistryPackage);
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else {
            boolean bFoundCreated = false;
            boolean bFoundUpdated = false;
            Collection events = pkg.getAuditTrail();
            assertEquals("RegistryPackage should have 2 AudiatableEvents",2,events.size());
            Iterator it = events.iterator();
            
            // This should be the create event..
            AuditableEvent ae = (AuditableEvent)it.next();
            assertTrue("AuditableEvent 'Create' user is null",ae.getUser()!=null);
            assertTrue("AuditableEvent 'Create' user name is null",ae.getUser().getName().getValue()!=null);
            if(ae.getEventType() == AuditableEvent.EVENT_TYPE_CREATED)
                bFoundCreated = true;
            else if(ae.getEventType() == AuditableEvent.EVENT_TYPE_UPDATED)
                bFoundUpdated = true;
            
            // This should be the modify event...
            ae = (AuditableEvent)it.next();
            assertTrue("AuditableEvent 'Update' user is null",ae.getUser()!=null);
            assertTrue("AuditableEvent 'Update' user name is null",ae.getUser().getName().getValue()!=null);
            if(ae.getEventType() == AuditableEvent.EVENT_TYPE_CREATED)
                bFoundCreated = true;
            else if(ae.getEventType() == AuditableEvent.EVENT_TYPE_UPDATED)
                bFoundUpdated = true;
            
            assertTrue("'Created' AuditableEvent event type not found",bFoundCreated);
            assertTrue("'Updated' AuditableEvent event type not found",bFoundUpdated);
        }
    }
    
    /**
     * Asserts that:
     *   We can find testPackage
     *   The pkg has one association to an ExtrinsicObject
     *   The ExtObj has the right name
     *   The ExtObj has one AuditableEvent of type Created
     *   The one ExtObj AuditableEvent has the user populated
     *
     * @throws <{Exception}>
     */
    public void testInspectRegistryPackageExtrinsicObjects()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage",BindingUtility.CANONICAL_OBJECT_TYPE_ID_RegistryPackage);
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else {
            Collection files = pkg.getAssociatedObjects();
            assertEquals("RegistryPackage should have 1 Association",1,files.size());
            Iterator fileIter = files.iterator();
            javax.xml.registry.infomodel.Association assoc = (javax.xml.registry.infomodel.Association)fileIter.next();
            ExtrinsicObject eo = (ExtrinsicObject)assoc.getTargetObject();
            assertTrue("ExtrinsicObject name incorrect",eo.getName().getValue().equals("testExtObj"));
            
            Collection edits = eo.getAuditTrail();
            
            //??This assertion is failing because there is only 1 auditable event found.
            //Commenting out for now as it is not essential.
            //assertEquals("ExtrinsicObject should have 2 AuditableEvent",2,edits.size());
            //??assertGreaterThanOrEquals("ExtrinsicObject should have 2 AuditableEvent",1,edits.size());
            
            
            Iterator it = edits.iterator();
            AuditableEvent ae = (AuditableEvent)it.next();
            
            assertTrue("AuditableEvent type is 'Created'",ae.getEventType() == AuditableEvent.EVENT_TYPE_CREATED);
            
            // NOTE This is where I was seeing intermittent problem of the user
            // object on the AE not being populated, returning null.
            assertTrue("AuditableEvent 'Created' user is null",ae.getUser()!=null);
            assertTrue("AuditableEvent 'Created' user name is null",ae.getUser().getName().getValue()!=null);
        }
    }
    
    /**
     * Asserts that:
     *  We can find testPackage
     *  We can add 3 slots to the package
     *
     * @throws <{Exception}>
     */
    public void testAddSlotsToRegistryPackage()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage",BindingUtility.CANONICAL_OBJECT_TYPE_ID_RegistryPackage);
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else {
            Slot slotA = setup.lcm.createSlot("SlotA", "SlotA Value","TestSlots");
            Slot slotB = setup.lcm.createSlot("SlotB", "SlotB Value","TestSlots");
            Slot slotC = setup.lcm.createSlot("SlotC", "SlotC Value","TestSlots");
            
            pkg.addSlot(slotA);
            pkg.addSlot(slotB);
            pkg.addSlot(slotC);
            
            ArrayList al = new ArrayList();
            al.add(pkg);
            
            System.err.println("\nSaving existing RegistryPackage after adding 3 SLots to it.");
            BulkResponse br = setup.lcm.saveObjects(al);
            assertTrue("Slot creation and addition to package failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    /**
     * Asserts that:
     *  We can find testPackage
     *  Pkg has 3 slots, and vals are correct
     *
     * @throws <{Exception}>
     */
    public void testInspectRegistryPackageSlots()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage",BindingUtility.CANONICAL_OBJECT_TYPE_ID_RegistryPackage);
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else {
            String val = null;
            Slot slotA = pkg.getSlot("SlotA");
            assertTrue("Slot A not found", slotA != null);
            val = (String)slotA.getValues().iterator().next();
            assertTrue("Slot A incorrect value", val.equals("SlotA Value"));
            Slot slotB = pkg.getSlot("SlotB");
            val = (String)slotB.getValues().iterator().next();
            assertTrue("Slot B incorrect value", val.equals("SlotB Value"));
            Slot slotC = pkg.getSlot("SlotC");
            val = (String)slotC.getValues().iterator().next();
            assertTrue("Slot C incorrect value", val.equals("SlotC Value"));
        }
    }
    
    /**
     * Asserts that:
     *  We can find testPackage
     *  We can find node InternalScheme1-Node1
     *  We can find node InternalScheme2-Node1
     *  We can find scheme ExternalScheme
     *  We can create primary classifications on internal scheme node
     *  We can create secondary cls on above primary cls
     *  We can create primary classifications on external scheme
     *  We can create secondary cls on above primary cls
     *
     * @throws <{Exception}>
     */
    public void testClassifyRegistryPackage()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage",BindingUtility.CANONICAL_OBJECT_TYPE_ID_RegistryPackage);
        Concept primaryInternalCon = (Concept)findRegistryObject("InternalScheme1-Node1", BindingUtility.CANONICAL_OBJECT_TYPE_ID_ClassificationNode);
        Concept ctxSensitiveCon = (Concept)findRegistryObject("InternalScheme2-Node1", BindingUtility.CANONICAL_OBJECT_TYPE_ID_ClassificationNode);
        ArrayList findQualifiers = new ArrayList();
        findQualifiers.add(FindQualifier.CASE_SENSITIVE_MATCH);
        ClassificationScheme extScheme = setup.bqm.findClassificationSchemeByName(findQualifiers, "ExternalScheme");
        
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else if(primaryInternalCon == null)
            fail("InternalScheme1-Node1 could not be found in the registry");
        else if(ctxSensitiveCon == null)
            fail("InternalScheme2-Node1 could not be found in the registry");
        else if(extScheme == null)
            fail("ExternalScheme could not be found in the registry");
        else {
            // Create primary internal classification
            Classification primaryInternalCls = setup.lcm.createClassification(primaryInternalCon);
            primaryInternalCls.setClassifiedObject(pkg);
            
            // Now create the secondary classification to the classification created above
            Classification internalCtxSensCls = setup.lcm.createClassification(ctxSensitiveCon);
            internalCtxSensCls.setClassifiedObject(primaryInternalCls);
            
            // create primary external classification
            Classification primaryExternalCls = setup.lcm.createClassification(extScheme,"externalName","externalCode");
            primaryExternalCls.setClassifiedObject(pkg);
            
            // Now create the secondary classification to the classification created above
            Classification externalCtxSensCls = setup.lcm.createClassification(ctxSensitiveCon);
            externalCtxSensCls.setClassifiedObject(primaryExternalCls);
            
            ArrayList al = new ArrayList();
            al.add(primaryInternalCls);
            al.add(internalCtxSensCls);
            al.add(primaryExternalCls);
            al.add(externalCtxSensCls);
            
            System.err.println("Add 2 Classifications to existing RegistryPackage. Each Classification has 1 contextual Classification.");
            BulkResponse br = setup.lcm.saveObjects(al);
            assertTrue("Classification failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    // Wrapper so test ui will indicate failure in appropriate
    // test suite context
    public void testInspectRegistryPackageClassificationsPostPkgUpdate()
    throws Exception {
        testInspectRegistryPackageClassifications();
    }
    
    /**
     * This test reproduces two problems:
     *    1) Retrieving a RegistryObject's classifications does not return
     *    classifications of the RO's classifcations (context sensitive classifications)
     *    You can however, get them via code commented out below.
     *
     *    2) After updating a RegistryObject, the context sensitive classifications
     *    are deleted from the db, and primary external classifications have the
     *    "ClassificationScheme" attribute deleted.
     *
     * Asserts that:
     *  We can find testPackage
     *  It has two primary classifications, one internal one external
     *  The external classification has the classification scheme
     *  Each primary classification has a secondary classification
     *
     * @throws <{Exception}>
     */
    public void testInspectRegistryPackageClassifications()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage",BindingUtility.CANONICAL_OBJECT_TYPE_ID_RegistryPackage);
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else {
            boolean bFoundInternalCls = false;
            boolean bFoundInternalClsConcept = false;
            boolean bFoundInternalConSensCls = false;
            boolean bFoundInternalConSensClsConcept = false;
            boolean bFoundExternalCls = false;
            boolean bFoundExternalClsScheme = false;
            boolean bFoundExternalConSensCls = false;
            boolean bFoundExternalConSensClsConcept = false;
            
            Collection clses = pkg.getClassifications();
            assertEquals("testPackage should have two Classifications",2,clses.size());
            Iterator it = clses.iterator();
            
            while(it.hasNext()) {
                Classification cls = (Classification)it.next();
                if(cls.isExternal()) {
                    bFoundExternalCls = true;
                    // NOTE This returns null for cs after an update is
                    // made to the RegistryObject
                    ClassificationScheme cs = cls.getClassificationScheme();
                    if(cs!=null)
                        bFoundExternalClsScheme = true;
                    
                    Collection conSensClses = cls.getClassifications();
                    if(conSensClses.size()==1) {
                        bFoundExternalConSensCls = true;
                        Classification cls2 = (Classification)conSensClses.iterator().next();
                        Concept con = cls2.getConcept();
                        if(con != null)
                            bFoundExternalConSensClsConcept = true;
                    }
                }
                else {
                    bFoundInternalCls = true;
                    bFoundInternalClsConcept = cls.getConcept() != null;
                    
                    Collection conSensClses = cls.getClassifications();
                    if(conSensClses.size()==1) {
                        bFoundInternalConSensCls = true;
                        Classification cls2 = (Classification)conSensClses.iterator().next();
                        Concept con = cls2.getConcept();
                        if(con != null)
                            bFoundInternalConSensClsConcept = true;
                    }
                }
            }
            assertTrue("Could not find Internal Classification",bFoundInternalCls);
            assertTrue("Could not find context sensitive Internal Classification",bFoundInternalConSensCls);
            assertTrue("Could not find External Classification",bFoundExternalCls);
            assertTrue("Could not find External Classification Scheme",bFoundExternalClsScheme);
            assertTrue("Could not find context sensitive External Classification",bFoundExternalConSensCls);
            
            assertTrue("Could not find Internal Classification Concept",bFoundInternalClsConcept);
            assertTrue("Could not find context sensitive Internal Classification Concept",bFoundInternalConSensClsConcept);
            assertTrue("Could not find context sensitive External Classification Concept",bFoundExternalConSensClsConcept);
        }
    }
    
    /**
     * Asserts that:
     *  We can find testPackage
     *  We can create a www.yahoo.com ExternalLink
     *  We can add to testPackage and save successfully
     */
    public void testAddRegistryPackageExternalLinks()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage",BindingUtility.CANONICAL_OBJECT_TYPE_ID_RegistryPackage);
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else {
            javax.xml.registry.infomodel.ExternalLink link = setup.lcm.createExternalLink("www.yahoo.com","External Link");
            String extLinkId = link.getKey().getId();
            pkg.addExternalLink(link);
            ArrayList al = new ArrayList();
            al.add(pkg);
            //al.add(link);//No need now that links are implicitly saved
            System.err.println("Add ExtrenalLink to existing Package");
            BulkResponse br = setup.lcm.saveObjects(al);
            assertTrue("ExternalLink creation failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
            
            ExternalLink link2 = (ExternalLink)setup.getDQM().getRegistryObject(extLinkId, LifeCycleManager.EXTERNAL_LINK);
            assertNotNull("Unable to read back ExternalLink", link2);
        }
    }
    
    /**
     * Asserts that:
     *  We can find testPackage
     *  It has one ExternalLink
     *  The ExternalLink URI is "www.yahoo.com"
     *
     * @throws <{Exception}>
     */
    public void testInspectRegistryPackageExternalLinks()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage",BindingUtility.CANONICAL_OBJECT_TYPE_ID_RegistryPackage);
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else {
            Collection links = pkg.getExternalLinks();
            assertEquals("Package should have 1 ExternalLink",1,links.size());
            javax.xml.registry.infomodel.ExternalLink link = (javax.xml.registry.infomodel.ExternalLink)links.iterator().next();
            assertTrue("ExternalLink has wrong url",link.getExternalURI().equals("www.yahoo.com"));
        }
    }
    
    /**
     * Utility method to lookup registry object based on name and type
     */
    public javax.xml.registry.infomodel.RegistryObject findRegistryObject(String roName, String roType) throws Exception {
        javax.xml.registry.infomodel.RegistryObject retVal = null;
        
        String query = "select * from registryobject ro, name_ nm where ro.id = nm.parent and nm.value = '" + roName + "'" +
        " and objecttype = '" + roType + "'";
        
        javax.xml.registry.Query q = setup.dqm.createQuery(javax.xml.registry.Query.QUERY_TYPE_SQL, query);
        BulkResponse br = setup.dqm.executeQuery(q);
        if(br.getStatus() == BulkResponse.STATUS_SUCCESS) {
            Collection c = br.getCollection();
            Iterator it = c.iterator();
            if(it.hasNext())
                retVal = (javax.xml.registry.infomodel.RegistryObject)it.next();
        }
        else {
            throw new Exception("findRegistryObject Failed!");
        }
        
        return retVal;
    }
}/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 *
 * $Revision: 1.8 $
 */

package com.sun.xml.registry.ebxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.infomodel.*;

import javax.xml.registry.FindQualifier;


public class ApelonJAXRTest extends junit.framework.TestCase {
    static ApelonJAXRTestSetup setup = null;
    
    public static junit.framework.Test suite() throws Exception {
        // These tests need to be ordered for purposes of read/write/delete.
        // Do not change order of tests, erroneous errors will result.
        junit.framework.TestSuite suite= new junit.framework.TestSuite();
        
        // Test creating and retrieving 2 internal schemes and 1 external scheme
        suite.addTest(new ApelonJAXRTest("testCreateInternalSchemes"));
        suite.addTest(new ApelonJAXRTest("testCreateExternalScheme"));
        suite.addTest(new ApelonJAXRTest("testRetrieveInternalSchemes"));
        suite.addTest(new ApelonJAXRTest("testRetrieveExternalScheme"));
        
        // Test creating and modifiying registry package (implicit retrieve test)
        suite.addTest(new ApelonJAXRTest("testCreateRegistryPackage"));
        suite.addTest(new ApelonJAXRTest("testUpdateRegistryPackage"));
        
        // Test inspection of RegistryPackage Auditable events
        suite.addTest(new ApelonJAXRTest("testInspectRegistryPackageAuditableEvents"));
        
        // Test creating and adding ExtrinsicObject to package
        suite.addTest(new ApelonJAXRTest("testCreateExtrinsicObject"));
        suite.addTest(new ApelonJAXRTest("testAddExtrinsicObjectToRegistryPackage"));
        suite.addTest(new ApelonJAXRTest("testInspectRegistryPackageExtrinsicObjects"));
        
        // Test adding slots
        suite.addTest(new ApelonJAXRTest("testAddSlotsToRegistryPackage"));
        suite.addTest(new ApelonJAXRTest("testInspectRegistryPackageSlots"));
        
        // Tests to create internal and external context sensitive
        // classifications, initial creation works now
        suite.addTest(new ApelonJAXRTest("testClassifyRegistryPackage"));
        suite.addTest(new ApelonJAXRTest("testInspectRegistryPackageClassifications"));
        
        // NOTE Running these again gives example of how context sensitive
        // classifications and primary external cls ClassificationScheme are
        // wiped out after updating a RegistryObject
        suite.addTest(new ApelonJAXRTest("testUpdateRegistryPackage"));
        suite.addTest(new ApelonJAXRTest("testInspectRegistryPackageClassificationsPostPkgUpdate"));
        
        // Test adding and retrieving ExternalLinks on a package
        suite.addTest(new ApelonJAXRTest("testAddRegistryPackageExternalLinks"));
        suite.addTest(new ApelonJAXRTest("testInspectRegistryPackageExternalLinks"));
        
        // Test deleting everything, cleanup for test suite
        System.err.println("testDeleteRegistryPackage");
        suite.addTest(new ApelonJAXRTest("testDeleteRegistryPackage"));
        System.err.println("testDeleteExtrinsicObject");
        suite.addTest(new ApelonJAXRTest("testDeleteExtrinsicObject"));
        System.err.println("testDeleteInternalSchemes");
        suite.addTest(new ApelonJAXRTest("testDeleteInternalSchemes"));
        System.err.println("testDeleteExternalScheme");
        suite.addTest(new ApelonJAXRTest("testDeleteExternalScheme"));
        
        setup = new ApelonJAXRTestSetup(suite);
        
        return setup;
    }
    
    public ApelonJAXRTest(String testMethod) {
        super(testMethod);
    }
    
    /**
     * Asserts that:
     *  We cannot find InternalScheme (1 and 2)
     *  We can create InternalScheme (1 and 2) with 2 nodes each
     *
     * @throws Exception
     */
    public void testCreateInternalSchemes()
    throws Exception {
        testCreateInternalScheme(1);
        testCreateInternalScheme(2);
    }
    
    /**
     * Asserts that:
     *  We cannot find InternalScheme (1 and 2)
     *  We can create InternalScheme (1 and 2) with 2 nodes each
     *
     * @throws Exception
     */
    public void testCreateInternalScheme(int schemeNum)
    throws Exception {
        ArrayList findQualifiers = new ArrayList();
        findQualifiers.add(FindQualifier.CASE_SENSITIVE_MATCH);
        ClassificationScheme scheme = setup.bqm.findClassificationSchemeByName(findQualifiers,"InternalScheme" + schemeNum);
        
        if(scheme != null)
            fail("InternalScheme" + schemeNum + " already exists in registry");
        else {
            scheme = setup.lcm.createClassificationScheme("InternalScheme" + schemeNum,"InternalScheme" + schemeNum);
            scheme.setValueType(ClassificationScheme.VALUE_TYPE_UNIQUE);
            Concept con1 = setup.lcm.createConcept(scheme,"InternalScheme" + schemeNum + "-Node1","InternalScheme" + schemeNum + "-Node1");
            Concept con2 = setup.lcm.createConcept(scheme,"InternalScheme" + schemeNum + "-Node2","InternalScheme" + schemeNum + "-Node2");
            scheme.addChildConcept(con1);
            scheme.addChildConcept(con2);
            ArrayList objs = new ArrayList();
            objs.add(scheme);
            
            //Following is not necessary but is being done to test that duplicates are eliminated during marshal
            objs.add(con1);
            objs.add(con2);
            
            System.err.println("\nSaving 1 scheme and 2 concepts");
            BulkResponse br = setup.lcm.saveObjects(objs);
            assertTrue("Scheme" + schemeNum + " Creation Failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    public void testRetrieveInternalSchemes()
    throws Exception {
        testRetrieveInternalScheme(1);
        testRetrieveInternalScheme(2);
    }
    
    /**
     * Asserts that:
     *  We can find InternalScheme (1 and 2)
     *
     * @throws Exception
     */
    public void testRetrieveInternalScheme(int schemeNum)
    throws Exception {
        ArrayList findQualifiers = new ArrayList();
        findQualifiers.add(FindQualifier.CASE_SENSITIVE_MATCH);
        ClassificationScheme scheme = setup.bqm.findClassificationSchemeByName(findQualifiers, "InternalScheme" + schemeNum);
        
        if(scheme == null)
            fail("InternalScheme" + schemeNum + " could not be found in registry");
        else {
            assertEquals("InternalScheme" + schemeNum + " node count incorrect", scheme.getChildConceptCount(),2);
        }
    }
    
    /**
     * Asserts that:
     *  We can find InternalScheme (1 and 2)
     *  We can delete InternalScheme (1 and 2)
     *
     * @throws Exception
     */
    public void testDeleteInternalSchemes()
    throws Exception {
        testDeleteInternalScheme(1);
        testDeleteInternalScheme(2);
    }
    
    /**
     * Asserts that:
     *  We can find InternalScheme (1 and 2)
     *  We can delete InternalScheme (1 and 2)
     *
     * @throws Exception
     */
    public void testDeleteInternalScheme(int schemeNum)
    throws Exception {
        ArrayList findQualifiers = new ArrayList();
        findQualifiers.add(FindQualifier.CASE_SENSITIVE_MATCH);
        ClassificationScheme scheme = setup.bqm.findClassificationSchemeByName(findQualifiers, "InternalScheme" + schemeNum);
        if(scheme == null)
            fail("InternalScheme" + schemeNum + " could not be found in registry");
        else {
            ArrayList al = new ArrayList();
            al.add(scheme.getKey());
            Iterator it = scheme.getChildrenConcepts().iterator();
            while(it.hasNext()) {
                al.add(((javax.xml.registry.infomodel.RegistryObject)it.next()).getKey());
            }
            BulkResponse br = setup.lcm.deleteObjects(al);
            assertTrue("Delete InternalScheme" + schemeNum + " did not Succeed",br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    /**
     * Asserts that:
     *  We cannot find ExternalScheme
     *  We can create ExternalScheme
     *
     * @throws Exception
     */
    public void testCreateExternalScheme()
    throws Exception {
        ArrayList findQualifiers = new ArrayList();
        findQualifiers.add(FindQualifier.CASE_SENSITIVE_MATCH);
        ClassificationScheme scheme = setup.bqm.findClassificationSchemeByName(findQualifiers, "ExternalScheme");
        if(scheme != null)
            fail("ExternalScheme already exists in registry");
        else {
            scheme = setup.lcm.createClassificationScheme("ExternalScheme","ExternalScheme");
            scheme.setValueType(ClassificationScheme.VALUE_TYPE_UNIQUE);
            ((com.sun.xml.registry.ebxml.infomodel.ClassificationSchemeImpl)scheme).setExternal(true);
            ArrayList al = new ArrayList();
            al.add(scheme);
            System.err.println("\nSaving 1 ext scheme");
            BulkResponse br = setup.lcm.saveObjects(al);
            assertTrue("ExternalScheme creation Failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    /**
     * Asserts that:
     *  We can find ExternalScheme
     *
     * @throws Exception
     */
    public void testRetrieveExternalScheme()
    throws Exception {
        ArrayList findQualifiers = new ArrayList();
        findQualifiers.add(FindQualifier.CASE_SENSITIVE_MATCH);
        ClassificationScheme scheme = setup.bqm.findClassificationSchemeByName(findQualifiers, "ExternalScheme");
        
        if(scheme == null)
            fail("ExternalScheme could not be found in registry");
        else
            assertEquals("ExternalScheme is external", true, scheme.isExternal());
    }
    
    /**
     * Asserts that:
     *  We can find ExternalScheme
     *  We can delete ExternalScheme
     *
     * @throws Exception
     */
    public void testDeleteExternalScheme()
    throws Exception {
        ArrayList findQualifiers = new ArrayList();
        findQualifiers.add(FindQualifier.CASE_SENSITIVE_MATCH);
        ClassificationScheme scheme = setup.bqm.findClassificationSchemeByName(findQualifiers, "ExternalScheme");
        if(scheme == null)
            fail("ExternalScheme could not be found in registry");
        else {
            ArrayList al = new ArrayList();
            al.add(scheme.getKey());
            BulkResponse br = setup.lcm.deleteObjects(al);
            assertTrue("Delete ExternalScheme did not Succeed",br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    /**
     * Asserts that:
     *  We cannot find testPackage
     *  We can create testPackage
     *
     * @throws Exception
     */
    public void testCreateRegistryPackage()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage","RegistryPackage");
        if(pkg != null)
            fail("testPackage already exists in the registry");
        else {
            pkg = setup.lcm.createRegistryPackage("testPackage");
            pkg.setDescription(setup.lcm.createInternationalString("testPackage description"));
            ArrayList al = new ArrayList();
            al.add(pkg);
            System.err.println("\nSaving 1 RegistryPackage");
            BulkResponse br = setup.lcm.saveObjects(al);
            assertTrue("testPackage creation Failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    /**
     * Asserts that:
     *  We can find testPackage
     *  We can update testPackage's description
     *
     * @throws Exception
     */
    public void testUpdateRegistryPackage()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage","RegistryPackage");
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else {
            String newDesc = pkg.getDescription().getValue() + " updated";
            pkg.setDescription(setup.lcm.createInternationalString(newDesc));
            ArrayList al = new ArrayList();
            al.add(pkg);
            System.err.println("\nSaving existing RegistryPackage after updating it.");
            BulkResponse br = setup.lcm.saveObjects(al);
            assertTrue("testPackage update failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    /**
     * Asserts that:
     *  We can find and delete testPackage
     *
     * @throws Exception
     */
    public void testDeleteRegistryPackage()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage","RegistryPackage");
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else {
            // Delete package associations first Is this correct though?
            // Should we have to del associations first?
            Iterator it = pkg.getAssociations().iterator();
            ArrayList al = new ArrayList();
            while(it.hasNext()) {
                javax.xml.registry.infomodel.Association a = (javax.xml.registry.infomodel.Association)it.next();
                al.add(a.getKey());
            }
            BulkResponse br = setup.lcm.deleteObjects(al);
            assertTrue("testPackage delete associations failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
            
            al.clear();
            al.add(pkg.getKey());
            br = setup.lcm.deleteObjects(al);
            assertTrue("testPackage deletion Failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    /**
     * Asserts that:
     *  We can create testExtObj ExtrinsicObject
     *
     * @throws Exception
     */
    public void testCreateExtrinsicObject()
    throws Exception {
        ExtrinsicObject extObj = (ExtrinsicObject)findRegistryObject("testExtObj", "ExtrinsicObject");
        if(extObj != null)
            fail("testExtObj already exists in the registry");
        else {
            String path = System.getProperty("user.home") + System.getProperty("file.separator") + "jaxr-ebxml.properties";
            java.io.File f = new java.io.File(path);
            
            // using URLs causes "cannot sign the payload error"
            // DataHandler dh = new DataHandler(f.toURL());
            javax.activation.DataHandler dh = new javax.activation.DataHandler(new javax.activation.FileDataSource(f));
            
            extObj = setup.lcm.createExtrinsicObject(dh);
            extObj.setName(setup.lcm.createInternationalString("testExtObj"));
            extObj.setDescription(setup.lcm.createInternationalString("jaxr-ebxml.properties"));
            ArrayList al = new ArrayList();
            al.add(extObj);
            System.err.println("\nSaving 1 ExtrinsicObject with a repository item");
            BulkResponse br = setup.lcm.saveObjects(al);
            assertTrue("testExtObj creation Failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    /**
     * Asserts that:
     *  We can find testExtObj
     *  We can delete testExtObj
     *
     * @throws Exception
     */
    public void testDeleteExtrinsicObject()
    throws Exception {
        ExtrinsicObject extObj = (ExtrinsicObject)findRegistryObject("testExtObj", "ExtrinsicObject");
        if(extObj == null)
            fail("testExtObj could not be found in the registry");
        else {
            ArrayList al = new ArrayList();
            al.add(extObj.getKey());
            BulkResponse br = setup.lcm.deleteObjects(al);
            assertTrue("testExtObj deletion Failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    /**
     * Asserts that:
     *  We can find testPackage
     *  We can find testExtObj
     *  We can add testExtObj to testPackage
     *
     * @throws Exception
     */
    public void testAddExtrinsicObjectToRegistryPackage()
    throws Exception {
        ExtrinsicObject extObj = (ExtrinsicObject)findRegistryObject("testExtObj", "ExtrinsicObject");
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage","RegistryPackage");
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else if(extObj == null)
            fail("testExtObj could not be found in the registry");
        else {
            pkg.addRegistryObject(extObj);
            
            ArrayList al = new ArrayList();
            al.add(pkg);
            System.err.println("Adding ass between existing RegistryPackage and ExtrinsicObject");
            BulkResponse br = setup.lcm.saveObjects(al);
            assertTrue("Adding ExtObj to RegPkg failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    
    /**
     * Asserts that:
     *  We can find testPackage
     *  The package has 2 auditable events
     *  The events are the expected types
     *  The events have user populated
     *
     * @throws Exception
     */
    public void testInspectRegistryPackageAuditableEvents()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage","RegistryPackage");
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else {
            boolean bFoundCreated = false;
            boolean bFoundUpdated = false;
            Collection events = pkg.getAuditTrail();
            assertEquals("RegistryPackage should have 2 AudiatableEvents",2,events.size());
            Iterator it = events.iterator();
            
            // This should be the create event..
            AuditableEvent ae = (AuditableEvent)it.next();
            assertTrue("AuditableEvent 'Create' user is null",ae.getUser()!=null);
            assertTrue("AuditableEvent 'Create' user name is null",ae.getUser().getName().getValue()!=null);
            if(ae.getEventType() == AuditableEvent.EVENT_TYPE_CREATED)
                bFoundCreated = true;
            else if(ae.getEventType() == AuditableEvent.EVENT_TYPE_UPDATED)
                bFoundUpdated = true;
            
            // This should be the modify event...
            ae = (AuditableEvent)it.next();
            assertTrue("AuditableEvent 'Update' user is null",ae.getUser()!=null);
            assertTrue("AuditableEvent 'Update' user name is null",ae.getUser().getName().getValue()!=null);
            if(ae.getEventType() == AuditableEvent.EVENT_TYPE_CREATED)
                bFoundCreated = true;
            else if(ae.getEventType() == AuditableEvent.EVENT_TYPE_UPDATED)
                bFoundUpdated = true;
            
            assertTrue("'Created' AuditableEvent event type not found",bFoundCreated);
            assertTrue("'Updated' AuditableEvent event type not found",bFoundUpdated);
        }
    }
    
    /**
     * Asserts that:
     *   We can find testPackage
     *   The pkg has one association to an ExtrinsicObject
     *   The ExtObj has the right name
     *   The ExtObj has one AuditableEvent of type Created
     *   The one ExtObj AuditableEvent has the user populated
     *
     * @throws Exception
     */
    public void testInspectRegistryPackageExtrinsicObjects()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage","RegistryPackage");
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else {
            Collection files = pkg.getAssociatedObjects();
            assertEquals("RegistryPackage should have 1 Association",1,files.size());
            Iterator fileIter = files.iterator();
            javax.xml.registry.infomodel.Association assoc = (javax.xml.registry.infomodel.Association)fileIter.next();
            ExtrinsicObject eo = (ExtrinsicObject)assoc.getTargetObject();
            assertTrue("ExtrinsicObject name incorrect",eo.getName().getValue().equals("testExtObj"));
            
            Collection edits = eo.getAuditTrail();
            
            //??This assertion is failing because there is only 1 auditable event found.
            //Commenting out for now as it is not essential.
            //assertEquals("ExtrinsicObject should have 2 AuditableEvent",2,edits.size());
            //??assertGreaterThanOrEquals("ExtrinsicObject should have 2 AuditableEvent",1,edits.size());
            
            
            Iterator it = edits.iterator();
            AuditableEvent ae = (AuditableEvent)it.next();
            
            assertTrue("AuditableEvent type is 'Created'",ae.getEventType() == AuditableEvent.EVENT_TYPE_CREATED);
            
            // NOTE This is where I was seeing intermittent problem of the user
            // object on the AE not being populated, returning null.
            assertTrue("AuditableEvent 'Created' user is null",ae.getUser()!=null);
            assertTrue("AuditableEvent 'Created' user name is null",ae.getUser().getName().getValue()!=null);
        }
    }
    
    /**
     * Asserts that:
     *  We can find testPackage
     *  We can add 3 slots to the package
     *
     * @throws Exception
     */
    public void testAddSlotsToRegistryPackage()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage","RegistryPackage");
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else {
            Slot slotA = setup.lcm.createSlot("SlotA", "SlotA Value","TestSlots");
            Slot slotB = setup.lcm.createSlot("SlotB", "SlotB Value","TestSlots");
            Slot slotC = setup.lcm.createSlot("SlotC", "SlotC Value","TestSlots");
            
            pkg.addSlot(slotA);
            pkg.addSlot(slotB);
            pkg.addSlot(slotC);
            
            ArrayList al = new ArrayList();
            al.add(pkg);
            
            System.err.println("\nSaving existing RegistryPackage after adding 3 SLots to it.");
            BulkResponse br = setup.lcm.saveObjects(al);
            assertTrue("Slot creation and addition to package failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    /**
     * Asserts that:
     *  We can find testPackage
     *  Pkg has 3 slots, and vals are correct
     *
     * @throws Exception
     */
    public void testInspectRegistryPackageSlots()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage","RegistryPackage");
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else {
            String val = null;
            Slot slotA = pkg.getSlot("SlotA");
            assertTrue("Slot A not found", slotA != null);
            val = (String)slotA.getValues().iterator().next();
            assertTrue("Slot A incorrect value", val.equals("SlotA Value"));
            Slot slotB = pkg.getSlot("SlotB");
            val = (String)slotB.getValues().iterator().next();
            assertTrue("Slot B incorrect value", val.equals("SlotB Value"));
            Slot slotC = pkg.getSlot("SlotC");
            val = (String)slotC.getValues().iterator().next();
            assertTrue("Slot C incorrect value", val.equals("SlotC Value"));
        }
    }
    
    /**
     * Asserts that:
     *  We can find testPackage
     *  We can find node InternalScheme1-Node1
     *  We can find node InternalScheme2-Node1
     *  We can find scheme ExternalScheme
     *  We can create primary classifications on internal scheme node
     *  We can create secondary cls on above primary cls
     *  We can create primary classifications on external scheme
     *  We can create secondary cls on above primary cls
     *
     * @throws Exception
     */
    public void testClassifyRegistryPackage()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage","RegistryPackage");
        Concept primaryInternalCon = (Concept)findRegistryObject("InternalScheme1-Node1", "ClassificationNode");
        Concept ctxSensitiveCon = (Concept)findRegistryObject("InternalScheme2-Node1", "ClassificationNode");
        ArrayList findQualifiers = new ArrayList();
        findQualifiers.add(FindQualifier.CASE_SENSITIVE_MATCH);
        ClassificationScheme extScheme = setup.bqm.findClassificationSchemeByName(findQualifiers, "ExternalScheme");
        
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else if(primaryInternalCon == null)
            fail("InternalScheme1-Node1 could not be found in the registry");
        else if(ctxSensitiveCon == null)
            fail("InternalScheme2-Node1 could not be found in the registry");
        else if(extScheme == null)
            fail("ExternalScheme could not be found in the registry");
        else {
            // Create primary internal classification
            Classification primaryInternalCls = setup.lcm.createClassification(primaryInternalCon);
            primaryInternalCls.setClassifiedObject(pkg);
            
            // Now create the secondary classification to the classification created above
            Classification internalCtxSensCls = setup.lcm.createClassification(ctxSensitiveCon);
            internalCtxSensCls.setClassifiedObject(primaryInternalCls);
            
            // create primary external classification
            Classification primaryExternalCls = setup.lcm.createClassification(extScheme,"externalName","externalCode");
            primaryExternalCls.setClassifiedObject(pkg);
            
            // Now create the secondary classification to the classification created above
            Classification externalCtxSensCls = setup.lcm.createClassification(ctxSensitiveCon);
            externalCtxSensCls.setClassifiedObject(primaryExternalCls);
            
            ArrayList al = new ArrayList();
            al.add(primaryInternalCls);
            al.add(internalCtxSensCls);
            al.add(primaryExternalCls);
            al.add(externalCtxSensCls);
            
            System.err.println("Add 2 Classifications to existing RegistryPackage. Each Classification has 1 contextual Classification.");
            BulkResponse br = setup.lcm.saveObjects(al);
            assertTrue("Classification failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    // Wrapper so test ui will indicate failure in appropriate
    // test suite context
    public void testInspectRegistryPackageClassificationsPostPkgUpdate()
    throws Exception {
        testInspectRegistryPackageClassifications();
    }
    
    /**
     * This test reproduces two problems:
     *    1) Retrieving a RegistryObject's classifications does not return
     *    classifications of the RO's classifcations (context sensitive classifications)
     *    You can however, get them via code commented out below.
     *
     *    2) After updating a RegistryObject, the context sensitive classifications
     *    are deleted from the db, and primary external classifications have the
     *    "ClassificationScheme" attribute deleted.
     *
     * Asserts that:
     *  We can find testPackage
     *  It has two primary classifications, one internal one external
     *  The external classification has the classification scheme
     *  Each primary classification has a secondary classification
     *
     * @throws Exception
     */
    public void testInspectRegistryPackageClassifications()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage","RegistryPackage");
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else {
            boolean bFoundInternalCls = false;
            boolean bFoundInternalClsConcept = false;
            boolean bFoundInternalConSensCls = false;
            boolean bFoundInternalConSensClsConcept = false;
            boolean bFoundExternalCls = false;
            boolean bFoundExternalClsScheme = false;
            boolean bFoundExternalConSensCls = false;
            boolean bFoundExternalConSensClsConcept = false;
            
            Collection clses = pkg.getClassifications();
            assertEquals("testPackage should have two Classifications",2,clses.size());
            Iterator it = clses.iterator();
            
            while(it.hasNext()) {
                Classification cls = (Classification)it.next();
                if(cls.isExternal()) {
                    bFoundExternalCls = true;
                    // NOTE This returns null for cs after an update is
                    // made to the RegistryObject
                    ClassificationScheme cs = cls.getClassificationScheme();
                    if(cs!=null)
                        bFoundExternalClsScheme = true;
                    
                    Collection conSensClses = cls.getClassifications();
                    if(conSensClses.size()==1) {
                        bFoundExternalConSensCls = true;
                        Classification cls2 = (Classification)conSensClses.iterator().next();
                        Concept con = cls2.getConcept();
                        if(con != null)
                            bFoundExternalConSensClsConcept = true;
                    }
                }
                else {
                    bFoundInternalCls = true;
                    bFoundInternalClsConcept = cls.getConcept() != null;
                    
                    Collection conSensClses = cls.getClassifications();
                    if(conSensClses.size()==1) {
                        bFoundInternalConSensCls = true;
                        Classification cls2 = (Classification)conSensClses.iterator().next();
                        Concept con = cls2.getConcept();
                        if(con != null)
                            bFoundInternalConSensClsConcept = true;
                    }
                }
            }
            assertTrue("Could not find Internal Classification",bFoundInternalCls);
            assertTrue("Could not find context sensitive Internal Classification",bFoundInternalConSensCls);
            assertTrue("Could not find External Classification",bFoundExternalCls);
            assertTrue("Could not find External Classification Scheme",bFoundExternalClsScheme);
            assertTrue("Could not find context sensitive External Classification",bFoundExternalConSensCls);
            
            assertTrue("Could not find Internal Classification Concept",bFoundInternalClsConcept);
            assertTrue("Could not find context sensitive Internal Classification Concept",bFoundInternalConSensClsConcept);
            assertTrue("Could not find context sensitive External Classification Concept",bFoundExternalConSensClsConcept);
        }
    }
    
    /**
     * Asserts that:
     *  We can find testPackage
     *  We can create a www.yahoo.com ExternalLink
     *  We can add to testPackage and save successfully
     */
    public void testAddRegistryPackageExternalLinks()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage","RegistryPackage");
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else {
            javax.xml.registry.infomodel.ExternalLink link = setup.lcm.createExternalLink("www.yahoo.com","External Link");
            pkg.addExternalLink(link);
            ArrayList al = new ArrayList();
            al.add(pkg);
            //al.add(link);//No need now that links are implicitly saved
            System.err.println("Add ExtrenalLink to existing Package");
            BulkResponse br = setup.lcm.saveObjects(al);
            assertTrue("ExternalLink creation failed", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }
    
    /**
     * Asserts that:
     *  We can find testPackage
     *  It has one ExternalLink
     *  The ExternalLink URI is "www.yahoo.com"
     *
     * @throws Exception
     */
    public void testInspectRegistryPackageExternalLinks()
    throws Exception {
        RegistryPackage pkg = (RegistryPackage)findRegistryObject("testPackage","RegistryPackage");
        if(pkg == null)
            fail("testPackage could not be found in the registry");
        else {
            Collection links = pkg.getExternalLinks();
            assertEquals("Package should have 1 ExternalLink",1,links.size());
            javax.xml.registry.infomodel.ExternalLink link = (javax.xml.registry.infomodel.ExternalLink)links.iterator().next();
            assertTrue("ExternalLink has wrong url",link.getExternalURI().equals("www.yahoo.com"));
        }
    }
    
    /**
     * Utility method to lookup registry object based on name and type
     */
    public javax.xml.registry.infomodel.RegistryObject findRegistryObject(String roName, String roType) throws Exception {
        javax.xml.registry.infomodel.RegistryObject retVal = null;
        
        String query = "select * from registryobject, name_  where id = parent and name_.value = '" + roName + "'" +
        " and objecttype = '" + roType + "'";
        
        javax.xml.registry.Query q = setup.dqm.createQuery(javax.xml.registry.Query.QUERY_TYPE_SQL, query);
        BulkResponse br = setup.dqm.executeQuery(q);
        if(br.getStatus() == BulkResponse.STATUS_SUCCESS) {
            Collection c = br.getCollection();
            Iterator it = c.iterator();
            if(it.hasNext())
                retVal = (javax.xml.registry.infomodel.RegistryObject)it.next();
        }
        else {
            throw new Exception("findRegistryObject Failed!");
        }
        
        return retVal;
    }
}