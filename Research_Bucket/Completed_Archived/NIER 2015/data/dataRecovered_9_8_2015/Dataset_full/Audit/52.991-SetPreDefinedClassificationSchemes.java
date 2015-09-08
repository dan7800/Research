/*
 * $Header: /cvsroot/sino/omar/test/org/freebxml/ebxmlrr/conformance/SetPreDefinedClassificationSchemes.java,v 1.1.1.1 2003/09/24 03:56:03 farrukh_najmi Exp $
*/

package org.freebxml.ebxmlrr.conformance;

import junit.framework.*;
import org.freebxml.ebxmlrr.interfaces.soap.*;
import javax.xml.soap.*;
import java.io.*;

/**
 *
 * 3. Structure population and verification
 * 3.1 Pre-defined classification schemes
 * 
 * RIM requires the pre-defined auditable event types to be supported by the Registry as 
 * a specific ClassificationSchemes  instance with "EventType", and also requires 
 * that each ExternalIdentifier instance reference a pre-existing classification scheme
 *
 * @author  mzaremba
 */

public class SetPreDefinedClassificationSchemes extends TestCase {
    
    private static String url = null;
    
    private SOAPSender sender;
    private SOAPMessage reply;
    private TestSupport supportMethods;
    private static String[] fileNames = {"SubmitEventTypeCS.xml", "SubmitEXTIDCS.xml",
                                  "SubmitRIMObjectTypeCS.xml", "SubmitRIMStabilityTypesCS.xml",
                                  "SubmitRIMStatusTypesCS.xml", "SubmitRIMAssocTypesCS.xml",
                                  "SubmitRIMDataTypesCS.xml"};
    private String fileName ="";
    
    public SetPreDefinedClassificationSchemes(String name) {
        super(name);
        supportMethods = new TestSupport();
        try {
            sender = supportMethods.connectRegistry(url);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Tests failed during Set up phase");
        }
    }
    
        public void setUp() {

    }
        
    protected void tearDown() throws Exception {
        System.gc();
    }
    
    /** 
     * 1.3. Predefined classification schemes 
     */
    public void testPreDefinedClassificationSchemes() {
        try {
            supportMethods.printMessage("SUBMIT CLASSIFICATION: " + fileName);
            supportMethods.submitFileToRegistry(sender, fileName);
        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception has been thrown during setting" +
                    " Pre-defined classification schemes by " + fileName);
            }
    }
    
   public void setTestName(String name){
        fileName = name;
    }
        
    /**
     * Add tests to the suite
     */
    public static Test suite() {
	TestSuite suite = new TestSuite();
	for (int i=0; i < fileNames.length; i++) {
            SetPreDefinedClassificationSchemes test = 
                new SetPreDefinedClassificationSchemes("testPreDefinedClassificationSchemes");
            test.setTestName(fileNames[i]);
            suite.addTest(test);
        }
        return suite;
    }

    public void runMyTests() {
        TestSuite suite = new TestSuite();
        suite.addTest(suite());
        TestResult result = new TestResult();
        suite.run(result);
        supportMethods.processResults("Set Pre-Defined Classification Schemes", result);
    }
    
    public static void main(String[] args) {
        url = TestSupport.getURL(args);
        new SetPreDefinedClassificationSchemes("SetPreDefinedClassificationSchemes").runMyTests();
    }
}
/*
 * $Header: /cvsroot/sino/ebxmlrr/test/com/sun/ebxml/registry/conformance/SetPreDefinedClassificationSchemes.java,v 1.7 2002/12/27 13:32:54 ritzmann Exp $
*/

package com.sun.ebxml.registry.conformance;

import junit.framework.*;
import com.sun.ebxml.registry.interfaces.soap.*;
import javax.xml.soap.*;
import java.io.*;

/**
 *
 * 3. Structure population and verification
 * 3.1 Pre-defined classification schemes
 * 
 * RIM requires the pre-defined auditable event types to be supported by the Registry as 
 * a specific ClassificationSchemes  instance with "EventType", and also requires 
 * that each ExternalIdentifier instance reference a pre-existing classification scheme
 *
 * @author  mzaremba
 */

public class SetPreDefinedClassificationSchemes extends TestCase {
    
    private static String url = null;
    
    private SOAPSender sender;
    private SOAPMessage reply;
    private TestSupport supportMethods;
    private static String[] fileNames = {"SubmitEventTypeCS.xml", "SubmitEXTIDCS.xml",
                                  "SubmitRIMObjectTypeCS.xml", "SubmitRIMStabilityTypesCS.xml",
                                  "SubmitRIMStatusTypesCS.xml", "SubmitRIMAssocTypesCS.xml",
                                  "SubmitRIMDataTypesCS.xml"};
    private String fileName ="";
    
    public SetPreDefinedClassificationSchemes(String name) {
        super(name);
        supportMethods = new TestSupport();
        try {
            sender = supportMethods.connectRegistry(url);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Tests failed during Set up phase");
        }
    }
    
        public void setUp() {

    }
        
    protected void tearDown() throws Exception {
        System.gc();
    }
    
    /** 
     * 1.3. Predefined classification schemes 
     */
    public void testPreDefinedClassificationSchemes() {
        try {
            supportMethods.printMessage("SUBMIT CLASSIFICATION: " + fileName);
            supportMethods.submitFileToRegistry(sender, fileName);
        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception has been thrown during setting" +
                    " Pre-defined classification schemes by " + fileName);
            }
    }
    
   public void setTestName(String name){
        fileName = name;
    }
        
    /**
     * Add tests to the suite
     */
    public static Test suite() {
	TestSuite suite = new TestSuite();
	for (int i=0; i < fileNames.length; i++) {
            SetPreDefinedClassificationSchemes test = 
                new SetPreDefinedClassificationSchemes("testPreDefinedClassificationSchemes");
            test.setTestName(fileNames[i]);
            suite.addTest(test);
        }
        return suite;
    }

    public void runMyTests() {
        TestSuite suite = new TestSuite();
        suite.addTest(suite());
        TestResult result = new TestResult();
        suite.run(result);
        supportMethods.processResults("Set Pre-Defined Classification Schemes", result);
    }
    
    public static void main(String[] args) {
        url = TestSupport.getURL(args);
        new SetPreDefinedClassificationSchemes("SetPreDefinedClassificationSchemes").runMyTests();
    }
}
