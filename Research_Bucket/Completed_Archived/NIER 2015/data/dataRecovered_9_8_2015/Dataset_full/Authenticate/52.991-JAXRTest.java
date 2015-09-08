/*
 * JAXRTest.java
 *
 * Created on April 8, 2002, 10:53 AM
 *
 * $Header: /cvsroot/sino/omar/test/org/freebxml/omar/client/xml/registry/JAXRTest.java,v 1.1 2004/01/06 16:47:43 farrukh_najmi Exp $
 *
 */

package org.freebxml.omar.client.xml.registry;

import javax.xml.registry.*;

import java.util.*;


/**
 * Common base class for all JAXR tests
 *
 * To use the JAAS authentication mechanisms you must create a file ~/.java.login.config with following content
 *
 *  JAXRTest {
 *   com.sun.security.auth.module.KeyStoreLoginModule required debug=true keyStoreURL="file://c:/Docume~1/najmi/jaxr-ebxml/security/keystore.jks";
 *  };
 *
 * Note that the keyStoreURL must point to wherever your keySTore file is. The ~ home directory is teh one pointed to the 
 * user.home System property. On windows 2000 it is file://c:/Docume~1/<uour login>.
 *
 * The password dialog usually takes a little while to pop up and does not always appear on top of other windows.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public abstract class JAXRTest {
    
    Connection connection = null;
    RegistryService service = null;
    BusinessLifeCycleManager lcm = null;
    BusinessQueryManager bqm = null;
    DeclarativeQueryManager dqm = null;
    
    private JAXRTest() {
        //Not allowed to be used
    }
    
    /** Creates new JAXRTest */
    public JAXRTest(Properties connectionProps) throws JAXRException {        
        createConnection(connectionProps);
    }
    
    /**
     * Makes a connection to a JAXR Registry.
     *
     * @param url The URL of the registry.
     */
    public void createConnection(Properties connectionProps) throws JAXRException {        
        if (connectionProps == null) {
            connectionProps = new Properties();
            connectionProps.put("javax.xml.registry.queryManagerURL", 
                "http://localhost:8080/ebxmlrr/registry/soap"); //http://registry.csis.hku.hk:8201/ebxmlrr/registry/soap
        }
        
        ConnectionFactory connFactory = getConnectionFactory(connectionProps);
        connFactory.setProperties(connectionProps);
        connection = connFactory.createConnection();
        service = connection.getRegistryService();
        
        bqm = service.getBusinessQueryManager();
        dqm = service.getDeclarativeQueryManager();
        lcm = service.getBusinessLifeCycleManager();        
    }
    
    private ConnectionFactory getConnectionFactory(Properties connectionProps) throws JAXRException {
        //Get factory class
        String factoryClass = System.getProperty("javax.xml.registry.ConnectionFactoryClass");
        if (factoryClass == null) {
            String url = (String)connectionProps.get("javax.xml.registry.queryManagerURL");
            if (url == null) {
                throw new JAXRException("Connection property javax.xml.registry.queryManagerURL not defined.");
            }
            
            //Choose provider based on hack for now.
            if (url.toLowerCase().indexOf("uddi") >=0 ) {
                System.setProperty("javax.xml.registry.ConnectionFactoryClass", "com.sun.xml.registry.uddi.ConnectionFactoryImpl");
            }
            else {
                System.setProperty("javax.xml.registry.ConnectionFactoryClass", "org.freebxml.omar.client.xml.registry.ConnectionFactoryImpl");
            }
        }
               
        ConnectionFactory   connFactory = ConnectionFactory.newInstance();
        
        return connFactory;
    }
        
}
/*
 * JAXRTest.java
 *
 * Created on April 8, 2002, 10:53 AM
 *
 * $Header: /cvsroot/sino/jaxr/test/com/sun/xml/registry/ebxml/JAXRTest.java,v 1.5 2002/11/09 01:44:09 jasilva Exp $
 *
 */

package com.sun.xml.registry.ebxml;

import javax.xml.registry.*;

import java.util.*;


/**
 * Common base class for all JAXR tests
 *
 * To use the JAAS authentication mechanisms you must create a file ~/.java.login.config with following content
 *
 *  JAXRTest {
 *   com.sun.security.auth.module.KeyStoreLoginModule required debug=true keyStoreURL="file://c:/Docume~1/najmi/jaxr-ebxml/security/keystore.jks";
 *  };
 *
 * Note that the keyStoreURL must point to wherever your keySTore file is. The ~ home directory is teh one pointed to the 
 * user.home System property. On windows 2000 it is file://c:/Docume~1/<uour login>.
 *
 * The password dialog usually takes a little while to pop up and does not always appear on top of other windows.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public abstract class JAXRTest {
    
    Connection connection = null;
    RegistryService service = null;
    BusinessLifeCycleManager lcm = null;
    BusinessQueryManager bqm = null;
    DeclarativeQueryManager dqm = null;
    
    private JAXRTest() {
        //Not allowed to be used
    }
    
    /** Creates new JAXRTest */
    public JAXRTest(Properties connectionProps) throws JAXRException {        
        createConnection(connectionProps);
    }
    
    /**
     * Makes a connection to a JAXR Registry.
     *
     * @param url The URL of the registry.
     */
    public void createConnection(Properties connectionProps) throws JAXRException {        
        if (connectionProps == null) {
            connectionProps = new Properties();
            connectionProps.put("javax.xml.registry.queryManagerURL", 
                "http://localhost:8080/ebxmlrr/registry/soap"); //http://registry.csis.hku.hk:8201/ebxmlrr/registry/soap
        }
        
        ConnectionFactory connFactory = getConnectionFactory(connectionProps);
        connFactory.setProperties(connectionProps);
        connection = connFactory.createConnection();
        service = connection.getRegistryService();
        
        bqm = service.getBusinessQueryManager();
        dqm = service.getDeclarativeQueryManager();
        lcm = service.getBusinessLifeCycleManager();        
    }
    
    private ConnectionFactory getConnectionFactory(Properties connectionProps) throws JAXRException {
        //Get factory class
        String factoryClass = System.getProperty("javax.xml.registry.ConnectionFactoryClass");
        if (factoryClass == null) {
            String url = (String)connectionProps.get("javax.xml.registry.queryManagerURL");
            if (url == null) {
                throw new JAXRException("Connection property javax.xml.registry.queryManagerURL not defined.");
            }
            
            //Choose provider based on hack for now.
            if (url.toLowerCase().indexOf("uddi") >=0 ) {
                System.setProperty("javax.xml.registry.ConnectionFactoryClass", "com.sun.xml.registry.uddi.ConnectionFactoryImpl");
            }
            else {
                System.setProperty("javax.xml.registry.ConnectionFactoryClass", "com.sun.xml.registry.ebxml.ConnectionFactoryImpl");
            }
        }
               
        ConnectionFactory   connFactory = ConnectionFactory.newInstance();
        
        return connFactory;
    }
        
}
