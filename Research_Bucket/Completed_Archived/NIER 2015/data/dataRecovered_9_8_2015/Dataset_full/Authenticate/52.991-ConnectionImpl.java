/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/xml/registry/ConnectionImpl.java,v 1.6 2004/03/25 12:53:48 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.xml.registry;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.x500.X500PrivateCredential;
import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.signature.XMLSignature;
import org.freebxml.omar.client.xml.registry.jaas.LoginModuleManager;
import org.freebxml.omar.common.CredentialInfo;


/**
 * ConnectionImpl
 */
public class ConnectionImpl implements Connection {
    /** DOCUMENT ME! */
    private final Log log = LogFactory.getLog(this.getClass());
    private ConnectionFactoryImpl factory = null;
    private RegistryServiceImpl service = null;
    private String queryManagerURL;
    private String lifeCycleManagerURL;
    private X500PrivateCredential x500Cred;
    private boolean closed = false;
    private boolean synchronous = true;
    private CallbackHandler handler = null;
    private LoginModuleManager loginModuleMgr = null;

    private static final org.freebxml.omar.client.xml.registry.util.SecurityUtil su =
        org.freebxml.omar.client.xml.registry.util.SecurityUtil.getInstance();
    
    /**
     * Creates a new ConnectionImpl object.
     *
     * @param factory DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    ConnectionImpl(ConnectionFactoryImpl factory) throws JAXRException {
        this.factory = factory;

        Properties props = factory.getProperties();
        queryManagerURL = props.getProperty(
                "javax.xml.registry.queryManagerURL");
        lifeCycleManagerURL = props.getProperty(
                "javax.xml.registry.lifeCycleManagerURL");
        loginModuleMgr = new LoginModuleManager();        
    }

    /**
     * Gets the RegistryService interface associated with the
     * Connection. If a Connection property (e.g. credentials) is set
     * after the client calls getRegistryService then the newly set
     * Connection property is visible to the RegistryService
     * previously returned by this call.
     *
     * <p>
     * <DL>
     * <dt>
     * <B>Capability Level: 0 </B>
     * </dt>
     * </dl>
     * </p>
     *
     * @see javax.xml.registry.RegistryService
     */
    public RegistryService getRegistryService() throws JAXRException {
        if (service == null) {
            service = new RegistryServiceImpl(this);
        }

        return service;
    }

    /**
     * Since a provider typically allocates significant resources
     * outside  the JVM on behalf of a Connection, clients should
     * close them when they are not needed.
     *
     * <p>
     *
     * <DL>
     * <dt>
     * <B>Capability Level: 0 </B>
     * </dt>
     * </dl>
     * </p>
     *
     * @exception JAXRException if a JARR error occurs.
     */
    public void close() throws JAXRException {
        // ??eeg Do we need to do anything to reduce resources here?
        closed = true;
    }

    /**
     * Return true if this Connection has been closed.
     *
     * <p>
     * <DL>
     * <dt>
     * <B>Capability Level: 0 </B>
     * </dt>
     * </dl>
     * </p>
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public boolean isClosed() throws JAXRException {
        return closed;
    }

    /**
     * Return true if client uses synchronous communication with JAXR
     * provider. Note that a JAXR provider must support both modes of
     * communication, while the client can choose which mode it wants
     * to use. Default is a return value of true (synchronous
     * communication).
     *
     * <p>
     * <DL>
     * <dt>
     * <B>Capability Level: 0</B>
     * </dt>
     * </dl>
     * </p>
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public boolean isSynchronous() throws JAXRException {
        return synchronous;
    }

    /**
     * Sets whether the client uses synchronous communication or not.
     * A JAXR client may dynamically change its communication style
     * preference.
     *
     * <p>
     * <DL>
     * <dt>
     * <B>Capability Level: 0 </B>
     * </dt>
     * </dl>
     * </p>
     *
     * @param sync DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public void setSynchronous(boolean sync) throws JAXRException {
        //??eeg A value of false is not implemented yet!
        synchronous = sync;
    }

    /**
     * Sets the Credentials associated with this client. The
     * credentials is used to authenticate the client with the JAXR
     * provider.  A JAXR client may dynamically change its identity by
     * changing the credentials associated with it.
     *
     * <p>
     * <DL>
     * <dt>
     * <B>Capability Level: 0 </B>
     * </dt>
     * </dl>
     * </p>
     *
     * @param credentials a Collection oj java.lang.Objects which
     *        provide identity related information for the caller.
     *
     * @throws JAXRException If the JAXR provider encounters an
     *         internal error
     */
    public void setCredentials(Set credentials) throws JAXRException {
        for (Iterator it = credentials.iterator(); it.hasNext();) {
            Object obj = it.next();

            if (obj instanceof X500PrivateCredential) {
                x500Cred = (X500PrivateCredential) obj;
                ((RegistryServiceImpl)getRegistryService()).setCredentialInfo(getCredentialInfo());

                return;
            }            
        }

        throw new JAXRException("No instance of X500PrivateCredential found");
    }

    /**
     * Gets the credentials associated with this client.
     *
     * <p>
     * <DL>
     * <dt>
     * <B>Capability Level: 0 </B>
     * </dt>
     * </dl>
     * </p>
     *
     * @return Set of java.lang.Object instances. The Collection may be
     *         empty but not null.
     *
     * @throws JAXRException If the JAXR provider encounters an
     *         internal error
     */
    public Set getCredentials() throws JAXRException {
        HashSet ret = new HashSet();

        if (x500Cred != null) {
            ret.add(x500Cred);
        }

        return ret;
    }

    /**
     * DOCUMENT ME!
     *
     * @return The X500PrivateCredential or null if not set
     */
    X500PrivateCredential getX500PrivateCredential() {
        if (x500Cred == null) {
            try {
                authenticate();
            } catch (JAXRException e) {
                log.error(e);
            }
        }

        return x500Cred;
    }
    
    public CredentialInfo getCredentialInfo() 
        throws JAXRException 
    {
        if (x500Cred == null) {
            return null;
        }
        if (x500Cred.isDestroyed()) {
            throw new JAXRException("Credential has been destroyed");
        }

        X509Certificate cert = x500Cred.getCertificate();
        if (cert == null) {
            throw new JAXRException("X509Certificate not found in X500PrivateCredential");
        }

        PrivateKey privateKey = x500Cred.getPrivateKey();
        if (privateKey == null) {
            throw new JAXRException("PrivateKey not found in X500PrivateCredential");
        }

        Certificate[] certChain = su.getCertificateChain(cert);

        String signingAlgo = privateKey.getAlgorithm();
        if (signingAlgo.equalsIgnoreCase("DSA")) {
            signingAlgo = XMLSignature.ALGO_ID_SIGNATURE_DSA;
        } 
        else if (signingAlgo.equalsIgnoreCase("RSA")) {
            signingAlgo = XMLSignature.ALGO_ID_SIGNATURE_RSA;
        } 
        else {
            throw new JAXRException("Signature algorithm not supported");
        }
        
        return new CredentialInfo(cert, certChain, privateKey, signingAlgo);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    String getQueryManagerURL() {
        return queryManagerURL;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    String getLifeCycleManagerURL() {
        return lifeCycleManagerURL;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ConnectionFactoryImpl getConnectionFactory() {
        return factory;
    }

    /**
     * This method is used to get the reference to the LoginModuleManager
     * With this reference, references to a parent Frame and Log can be
     * passed to the LoginModuleManager.
     *
     * @return
     *  A reference to the LoginModuleManager
     */
    public LoginModuleManager getLoginModuleManager() {
        return loginModuleMgr;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public CallbackHandler getCallbackHandler() throws JAXRException {
        if (handler == null) {
            handler = loginModuleMgr.getCallbackHandler();
        }

        return handler;
    }

    /**
     * DOCUMENT ME!
     *
     * @param handler DOCUMENT ME!
     */
    public void setCallbackHandler(CallbackHandler handler) {
        loginModuleMgr.setDefaultCallbackHandler(handler);
    }

    /**
     * Determine whether the user has already authenticated and setCredentials
     * on the Connection or not.
     * Add to JAXR 2.0??
     *
     * @param handler DOCUMENT ME!
     */
    public boolean isAuthenticated() throws JAXRException {
        boolean authenticated = false;

        if (x500Cred != null) {
            authenticated = true;
        }

        return authenticated;
    }

    /**
     * Forces authentication to occur.
     ** Add to JAXR 2.0??
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public void authenticate() throws JAXRException {
        // Obtain a LoginContext, needed for authentication. Tell it 
        // to use the LoginModule implementation specified by the 
        // entry named "Sample" in the JAAS login configuration 
        // file and to also use the specified CallbackHandler.
        LoginContext lc = null;

        try {
            loginModuleMgr.createLoginConfigFile();

            String applicationName = loginModuleMgr.getApplicationName();
            handler = loginModuleMgr.getCallbackHandler();

            lc = new LoginContext(applicationName, handler);

            // attempt authentication
            lc.login();

            //Get teh authenticated Subject.
            Subject subject = lc.getSubject();
            Set privateCredentials = subject.getPrivateCredentials();

            //Set credentials on JAXR Connections
            setCredentials(privateCredentials);

            log.info("Set credentials on connection.");
        } catch (LoginException le) {
            String msg = le.getMessage();

            if ((msg != null) && (!(msg.equalsIgnoreCase("Login cancelled")))) {
                throw new JAXRException(le);
            }
        } catch (SecurityException se) {
            throw new JAXRException(se);
        }
    }

    /**
     * Logout current user if any.
     *
     */
    public void logoff() throws JAXRException {
        boolean authenticated = isAuthenticated();

        if (authenticated) {
            x500Cred = null;
        }
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

import com.sun.security.auth.callback.DialogCallbackHandler;

import com.sun.xml.registry.ebxml.infomodel.RegistryPackageImpl;
import com.sun.xml.registry.ebxml.util.I18nUtil;
import com.sun.xml.registry.ebxml.util.KeystoreUtil;
import com.sun.xml.registry.ebxml.jaas.LoginModuleManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.x500.X500PrivateCredential;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.User;


/**
 * ConnectionImpl
 */
public class ConnectionImpl implements Connection {

    /** DOCUMENT ME! */
    private final Log log = LogFactory.getLog(this.getClass());
    private ConnectionFactoryImpl factory  = null;
    private RegistryServiceImpl service    = null;
    private String queryManagerURL;
    private String lifeCycleManagerURL;
    private X500PrivateCredential x500Cred;
    private boolean closed                 = false;
    private boolean synchronous            = true;
    private CallbackHandler handler        = null;
    private LoginModuleManager loginModuleMgr = null;

    /**
     * Creates a new ConnectionImpl object.
     *
     * @param factory DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    ConnectionImpl(ConnectionFactoryImpl factory) throws JAXRException {
        this.factory = factory;

        Properties props = factory.getProperties();
        queryManagerURL =
            props.getProperty("javax.xml.registry.queryManagerURL");
        lifeCycleManagerURL =
            props.getProperty("javax.xml.registry.lifeCycleManagerURL");
        loginModuleMgr = new LoginModuleManager();
    }

    /**
     * Gets the RegistryService interface associated with the
     * Connection. If a Connection property (e.g. credentials) is set
     * after the client calls getRegistryService then the newly set
     * Connection property is visible to the RegistryService
     * previously returned by this call.
     * 
     * <p>
     * <DL>
     * <dt>
     * <B>Capability Level: 0 </B>
     * </dt>
     * </dl>
     * </p>
     *
     * @see javax.xml.registry.RegistryService
     */
    public RegistryService getRegistryService() throws JAXRException {

        if (service == null) {
            service = new RegistryServiceImpl(this);
        }

        return service;
    }

    /**
     * Since a provider typically allocates significant resources
     * outside  the JVM on behalf of a Connection, clients should
     * close them when they are not needed.
     * 
     * <p>
     * 
     * <DL>
     * <dt>
     * <B>Capability Level: 0 </B>
     * </dt>
     * </dl>
     * </p>
     *
     * @exception JAXRException if a JARR error occurs.
     */
    public void close() throws JAXRException {

        // ??eeg Do we need to do anything to reduce resources here?
        closed = true;
    }

    /**
     * Return true if this Connection has been closed.
     * 
     * <p>
     * <DL>
     * <dt>
     * <B>Capability Level: 0 </B>
     * </dt>
     * </dl>
     * </p>
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public boolean isClosed() throws JAXRException {

        return closed;
    }

    /**
     * Return true if client uses synchronous communication with JAXR
     * provider. Note that a JAXR provider must support both modes of
     * communication, while the client can choose which mode it wants
     * to use. Default is a return value of true (synchronous
     * communication).
     * 
     * <p>
     * <DL>
     * <dt>
     * <B>Capability Level: 0</B>
     * </dt>
     * </dl>
     * </p>
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public boolean isSynchronous() throws JAXRException {

        return synchronous;
    }

    /**
     * Sets whether the client uses synchronous communication or not.
     * A JAXR client may dynamically change its communication style
     * preference.
     * 
     * <p>
     * <DL>
     * <dt>
     * <B>Capability Level: 0 </B>
     * </dt>
     * </dl>
     * </p>
     *
     * @param sync DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public void setSynchronous(boolean sync) throws JAXRException {

        //??eeg A value of false is not implemented yet!
        synchronous = sync;
    }

    /**
     * Sets the Credentials associated with this client. The
     * credentials is used to authenticate the client with the JAXR
     * provider.  A JAXR client may dynamically change its identity by
     * changing the credentials associated with it.
     * 
     * <p>
     * <DL>
     * <dt>
     * <B>Capability Level: 0 </B>
     * </dt>
     * </dl>
     * </p>
     *
     * @param credentials a Collection oj java.lang.Objects which
     *        provide identity related information for the caller.
     *
     * @throws JAXRException If the JAXR provider encounters an
     *         internal error
     */
    public void setCredentials(Set credentials) throws JAXRException {

        for (Iterator it = credentials.iterator(); it.hasNext();) {

            Object obj = it.next();

            if (obj instanceof X500PrivateCredential) {
                x500Cred     = (X500PrivateCredential)obj;
                return;
            }
        }

        throw new JAXRException("No instance of X500PrivateCredential found");
    }

    /**
     * Gets the credentials associated with this client.
     * 
     * <p>
     * <DL>
     * <dt>
     * <B>Capability Level: 0 </B>
     * </dt>
     * </dl>
     * </p>
     *
     * @return Set of java.lang.Object instances. The Collection may be
     *         empty but not null.
     *
     * @throws JAXRException If the JAXR provider encounters an
     *         internal error
     */
    public Set getCredentials() throws JAXRException {

        HashSet ret = new HashSet();

        if (x500Cred != null) {
            ret.add(x500Cred);
        }

        return ret;
    }

    /**
     * DOCUMENT ME!
     *
     * @return The X500PrivateCredential or null if not set
     */
    X500PrivateCredential getX500PrivateCredential() {

        if (x500Cred == null) {

            try {
                authenticate();
            } catch (JAXRException e) {
                log.error(e);
            }
        }

        return x500Cred;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    String getQueryManagerURL() {

        return queryManagerURL;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    String getLifeCycleManagerURL() {

        return lifeCycleManagerURL;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ConnectionFactoryImpl getConnectionFactory() {

        return factory;
    }

    /**
     * This method is used to get the reference to the LoginModuleManager
     * With this reference, references to a parent Frame and Log can be 
     * passed to the LoginModuleManager.
     *
     * @return
     *  A reference to the LoginModuleManager
     */
     public LoginModuleManager getLoginModuleManager()
     {
         return loginModuleMgr;
     }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public CallbackHandler getCallbackHandler() throws JAXRException {

        if (handler == null) {
            handler = loginModuleMgr.getCallbackHandler();
        }
        return handler;
    }

    /**
     * DOCUMENT ME!
     *
     * @param handler DOCUMENT ME!
     */
    public void setCallbackHandler(CallbackHandler handler) {
        loginModuleMgr.setDefaultCallbackHandler(handler);
    }

    /**
     * Determine whether the user has already authenticated and setCredentials 
     * on the Connection or not.
     * Add to JAXR 2.0??
     *
     * @param handler DOCUMENT ME!
     */
    public boolean isAuthenticated() throws JAXRException {
        boolean authenticated = false;
        if (x500Cred != null) {
            authenticated = true;
        }
        
        return authenticated;
    }
    
    /**
     * Forces authentication to occur.
     ** Add to JAXR 2.0??
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public void authenticate() throws JAXRException {

        // Obtain a LoginContext, needed for authentication. Tell it 
        // to use the LoginModule implementation specified by the 
        // entry named "Sample" in the JAAS login configuration 
        // file and to also use the specified CallbackHandler.
        LoginContext lc = null;

        try {

            loginModuleMgr.createLoginConfigFile();
            String applicationName = loginModuleMgr.getApplicationName();
            handler = loginModuleMgr.getCallbackHandler();

            lc = new LoginContext(applicationName, handler);

            // attempt authentication
            lc.login();

            //Get teh authenticated Subject.
            Subject subject        = lc.getSubject();
            Set privateCredentials = subject.getPrivateCredentials();

            //Set credentials on JAXR Connections
            setCredentials(privateCredentials);

            log.info("Set credentials on connection.");
        } catch (LoginException le) {

            String msg = le.getMessage();

            if ((msg != null)
                && (!(msg.equalsIgnoreCase("Login cancelled")))) {
                throw new JAXRException(le);
            }
        } catch (SecurityException se) {
            throw new JAXRException(se);
        }
    }
    
    /**
     * Logout current user if any.
     *
     */
    public void logoff() throws JAXRException {
        boolean authenticated = isAuthenticated();
        if (authenticated) {
             x500Cred = null;
        }        
    }
    
}
