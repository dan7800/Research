/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/security/authentication/AuthenticationServiceImpl.java,v 1.19 2004/03/26 21:41:47 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.security.authentication;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.QueryManager;
import org.freebxml.omar.common.QueryManagerFactory;
import org.freebxml.omar.common.RegistryException;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.persistence.PersistenceManagerFactory;
import org.freebxml.omar.server.security.UserRegistrationException;
import org.oasis.ebxml.registry.bindings.rim.User;
import org.oasis.ebxml.registry.bindings.rim.UserType;


/**
 * Manages authentication functionality for the registry.
 * This includes managemnet of user public keys in the server key store.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class AuthenticationServiceImpl /*implements AuthenticationService*/ {
    public static final String CANONICAL_ID_NODE_REGISTRY_ADMINISTRATOR = "urn:uuid:970eeed9-1e58-4e97-bd82-eff3651998c2";

    /* Aliases/ids for pre-defined Users.*/
    public static String ALIAS_REGISTRY_OPERATOR = "urn:uuid:921284f0-bbed-4a4c-9342-ecaf0625f9d7";
    public static String ALIAS_REGISTRY_GUEST = "urn:uuid:abfa78d5-605e-4dbc-b9ee-a42e99d5f7cf";

    /* Aliases/ids for test Users */
    public static String ALIAS_FARRUKH = "urn:uuid:977d9380-00e2-4ce8-9cdc-d8bf6a4157be";
    public static String ALIAS_NIKOLA = "urn:uuid:85428d8e-1bd5-473b-a8c8-b9d595f82728";
    
    QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
    
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */

    /*# private AuthenticationServiceImpl _authenticationServiceImpl; */
    private static AuthenticationServiceImpl instance = null;
    private org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(this.getClass());
    KeyStore keyStore = null;

    /* A lock object to prevent another thread to  access the keystore when
     * there is a thread adding certificate to it and writing it to disk
     */
    Object keyStoreWriteLock = new Object();
    KeyStore trustAnchorsKeyStore;
    public User registryGuest = null;
    public User registryOperator = null;
    public User farrukh = null;
    public User nikola = null;
    java.util.List userCache = new java.util.ArrayList();
    java.util.HashMap userMap = new java.util.HashMap();
    java.util.HashSet adminIdSet = new java.util.HashSet();
    final int cacheSize;
    RegistryProperties propsReader = RegistryProperties.getInstance();

    protected AuthenticationServiceImpl() {
        String userCacheSize = RegistryProperties.getInstance().getProperty("omar.security.userCacheSize");
        loadPredefinedUsers();
        loadRegistryAdministrators();
        cacheSize = Integer.parseInt(userCacheSize);
    }

    public KeyStore getTrustAnchorsKeyStore() throws RegistryException {
        try {
            if (trustAnchorsKeyStore == null) {
                synchronized (AuthenticationServiceImpl.class) {
                    if (trustAnchorsKeyStore == null) {
                        String keyStoreFile = propsReader.getProperty(
                                "omar.security.trustAnchors.keystoreFile");
                        String keystorePassword = propsReader.getProperty(
                                "omar.security.trustAnchors.keystorePassword");
                        String keystoreType = propsReader.getProperty(
                                "omar.security.trustAnchors.keystoreType");
                        trustAnchorsKeyStore = KeyStore.getInstance(keystoreType);
                        trustAnchorsKeyStore.load(new java.io.FileInputStream(
                                keyStoreFile), keystorePassword.toCharArray());
                    }
                }
            }

            return trustAnchorsKeyStore;
        } catch (NoSuchAlgorithmException e) {
            throw new RegistryException("Cannot load the trust anchors keystore",
                e);
        } catch (KeyStoreException e) {
            throw new RegistryException("Cannot load the trust anchors keystore",
                e);
        } catch (java.security.cert.CertificateException e) {
            throw new RegistryException("Cannot load the trust anchors keystore",
                e);
        } catch (java.io.FileNotFoundException e) {
            throw new RegistryException("Cannot load the trust anchors keystore",
                e);
        } catch (IOException e) {
            throw new RegistryException("Cannot load the trust anchors keystore",
                e);
        }
    }

    /**
     * Get the keystore whose path is specified  by {@link #getKeyStoreFileName()}.
     * Note that all the methods that access the keystore MUST access the keystore
     * via this method. Do not access the keystore directly by accessing the keystore
     * field. Otherwise the checking the write lock to keystore will be bypassed.
     */
    public KeyStore getKeyStore() throws RegistryException {
        synchronized (keyStoreWriteLock) {
            if (keyStore == null) {
                java.io.FileInputStream fis = null;

                try {
                    keyStore = KeyStore.getInstance("JKS");

                    String keystoreFile = getKeyStoreFileName();
                    fis = new java.io.FileInputStream(keystoreFile);

                    String keystorePass = getKeyStorePassword();
                    keyStore.load(fis, keystorePass.toCharArray());
                } catch (java.security.cert.CertificateException e) {
                    throw new RegistryException(e);
                } catch (KeyStoreException e) {
                    throw new RegistryException(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new RegistryException(e);
                } catch (java.io.FileNotFoundException e) {
                    throw new RegistryException(e);
                } catch (IOException e) {
                    throw new RegistryException(e);
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            return keyStore;
        }
    }

    public java.security.PrivateKey getPrivateKey(String alias, String password)
        throws RegistryException {
        java.security.PrivateKey privateKey = null;

        try {
            privateKey = (java.security.PrivateKey) getKeyStore().getKey(alias,
                    password.toCharArray());
        } catch (KeyStoreException e) {
            throw new RegistryException("Error getting private key", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RegistryException("Error getting private key", e);
        } catch (java.security.UnrecoverableKeyException e) {
            throw new RegistryException("Error getting private key", e);
        }

        return privateKey;
    }

    public X509Certificate getCertificate(String alias)
        throws RegistryException {
        X509Certificate cert = null;

        try {
            cert = (X509Certificate) getKeyStore().getCertificate(alias);

            if (cert == null) {
                throw new RegistryException("Certificate not found for alias '" +
                    alias + "'");
            }
        } catch (KeyStoreException e) {
            throw new RegistryException("Error getting certificate", e);
        }

        return cert;
    }

    public java.security.cert.Certificate[] getCertificateChain(String alias)
        throws RegistryException {
        try {
            return getKeyStore().getCertificateChain(alias);
        } catch (KeyStoreException e) {
            throw new RegistryException("Error getting certificate chain", e);
        }
    }

    public static AuthenticationServiceImpl getInstance() {
        if (instance == null) {
            synchronized (org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl.class) {
                if (instance == null) {
                    instance = new org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl();
                }
            }
        }

        return instance;
    }

    public String getKeyStoreFileName() throws RegistryException {
        String fileName = RegistryProperties.getInstance().getProperty("omar.security.keystoreFile");

        return fileName;
    }

    public String getKeyStorePassword() throws RegistryException {
        String pw = RegistryProperties.getInstance().getProperty("omar.security.keystorePassword");

        return pw;
    }

    /**
     * Check if the signatures CA is trusted by the registry.
     *
     * @throws UserRegistrationException if the certificate issuing CA is not trusted.
     * @throws RegistryException if the certificates cannot be verified for some other reasons, such as unable to load
     * trust anchors keystore
     */
    public void validateCertificate(
        org.apache.xml.security.signature.XMLSignature signature)
        throws UserRegistrationException, RegistryException {
        try {
            java.security.cert.CertPathBuilder certPathBuilder = java.security.cert.CertPathBuilder.getInstance(
                    "PKIX");
            java.security.cert.X509CertSelector targetConstraints = new java.security.cert.X509CertSelector();

            org.apache.xml.security.keys.KeyInfo keyInfo = signature.getKeyInfo();
            int lengthX509Data = keyInfo.lengthX509Data();

            for (int i = 0; i < lengthX509Data; i++) {
                org.apache.xml.security.keys.content.X509Data x509Data = keyInfo.itemX509Data(i);
                X509Certificate x509Certificate = x509Data.itemCertificate(0)
                                                          .getX509Certificate();
                targetConstraints.setSubject(x509Certificate.getSubjectX500Principal()
                                                            .getEncoded());
            }

            java.security.cert.PKIXBuilderParameters params = new java.security.cert.PKIXBuilderParameters(getTrustAnchorsKeyStore(),
                    targetConstraints);
            java.security.cert.CollectionCertStoreParameters ccsp = new java.security.cert.CollectionCertStoreParameters();
            java.security.cert.CertStore store = java.security.cert.CertStore.getInstance("Collection",
                    ccsp);
            params.addCertStore(store);
            certPathBuilder.build(params).getCertPath();
        } catch (NoSuchAlgorithmException e) {
            throw new RegistryException("Cannot verify the certificates", e);
        } catch (org.apache.xml.security.exceptions.XMLSecurityException e) {
            throw new RegistryException("Cannot verify the certificates", e);
        } catch (IOException e) {
            throw new RegistryException("Cannot verify the certificates", e);
        } catch (KeyStoreException e) {
            throw new RegistryException("Cannot verify the certificates", e);
        } catch (java.security.InvalidAlgorithmParameterException e) {
            throw new RegistryException("Cannot verify the certificates", e);
        } catch (java.security.cert.CertPathBuilderException e) {
            throw new UserRegistrationException(
                "User registration fails. The certificate is not " +
                "issued by the trusted certificate authority", e);
        }
    }

    /**
     * Gets the alias within the KeyStore for a User
     */
    public String getAliasFromUser(User user) throws RegistryException {
        return user.getId();
    }

    /**
     * Gets the alias within the KeyStore for a User
     */
    public X509Certificate getCertificateFromUser(User user)
        throws RegistryException {
        X509Certificate cert = null;

        try {
            String alias = getAliasFromUser(user);

            cert = (X509Certificate) (getKeyStore().getCertificate(alias));
        } catch (KeyStoreException e) {
            throw new RegistryException(e);
        }

        return cert;
    }

    /**
     * Gets the User that is associated with the KeyInfo provided within the XMLSignature signature.
     *
     * @throws RegistryException no matching User is found. May need more specific Exception??
     */
    public UserType getUserFromAlias(String alias) throws RegistryException {
        UserType user = null;

        try {
            String userId = alias;
            
            org.oasis.ebxml.registry.bindings.query.ResponseOption responseOption =
                BindingUtility.getInstance().queryFac.createResponseOption();

            responseOption.setReturnType(org.oasis.ebxml.registry.bindings.query.ReturnType.LEAF_CLASS);
            responseOption.setReturnComposedObjects(true);

            String sqlQuery = "SELECT * FROM User_ WHERE id='" + userId + "'";
            List users = PersistenceManagerFactory.getInstance()
                                                  .getPersistenceManager()
                                                  .executeSQLQuery(sqlQuery,
                    responseOption, "User_", new ArrayList());

            if (users.size() > 0) {
                user = (UserType) users.get(0);
            }

            if (user == null) {
                throw new org.freebxml.omar.server.security.UserNotFoundException(userId);
            }

            //See if User need to be auto-classified as RegistryAdministrator
            boolean isAdmin = isRegistryAdministratorInPropFile(user);

            if (isAdmin) {
                //Make sure that the user is classified with the RegistryAdministrator role
                makeRegistryAdministrator(user);
            }
        } catch (javax.xml.bind.JAXBException e) {
            throw new RegistryException(e);
        }

        return user;
    }

    /**
     * See if User is declared as a RegistryAdministrator in prop file.
     */
    private boolean isRegistryAdministratorInPropFile(UserType user)
        throws RegistryException {
        boolean isAdmin = false;

        if (user != null) {
            String id = user.getId();

            if (adminIdSet.contains(id)) {
                isAdmin = true;
            }
        }

        return isAdmin;
    }

    public boolean isRegistryAdministrator(UserType user)
        throws RegistryException {
        boolean isAdmin = false;

        log.info("isRegistryAdministrator: user=" + user.getId());

        List classifications = user.getClassification();
        java.util.Iterator iter = classifications.iterator();

        while (iter.hasNext()) {
            org.oasis.ebxml.registry.bindings.rim.Classification classification = (org.oasis.ebxml.registry.bindings.rim.Classification) iter.next();
            String classificationNodeId = classification.getClassificationNode();
            log.info("isRegistryAdministrator: classificationNodeId=" +
                classificationNodeId);

            if (classificationNodeId.equals(
                        CANONICAL_ID_NODE_REGISTRY_ADMINISTRATOR)) {
                isAdmin = true;

                break;
            }
        }

        log.info("isRegistryAdministrator: isAdmin=" + isAdmin);

        return isAdmin;
    }

    /**
     * Make sure user gets auto-classified as RegistryAdministrator if not so already.
     */
    private void makeRegistryAdministrator(UserType user)
        throws RegistryException {
        try {
            if (user != null) {
                boolean isAdmin = isRegistryAdministrator(user);

                if (!isAdmin) {
                    org.oasis.ebxml.registry.bindings.rim.Classification classification =
                        BindingUtility.getInstance().rimFac.createClassification();
                    classification.setId(org.freebxml.omar.common.Utility.getInstance().createId());
                    classification.setClassificationNode(CANONICAL_ID_NODE_REGISTRY_ADMINISTRATOR);
                    classification.setClassifiedObject(user.getId());
                    user.getClassification().add(classification);

                    //Now persists updated User 
                    org.freebxml.omar.server.persistence.PersistenceManager pm = PersistenceManagerFactory.getInstance()
                                                                                                          .getPersistenceManager();

                    java.util.List al = new java.util.ArrayList();
                    al.add(user);
                    pm.update(user, al);
                }
            }
        } catch (javax.xml.bind.JAXBException e) {
            throw new RegistryException(e);
        }
    }
    
    

    /*
     * Loads and caches the predefined users during startup.
     */
    private void loadPredefinedUsers() {
        try {
            registryOperator = (User)qm.getRegistryObject(ALIAS_REGISTRY_OPERATOR);
            registryGuest = (User)qm.getRegistryObject(ALIAS_REGISTRY_GUEST);
            farrukh = (User)qm.getRegistryObject(ALIAS_FARRUKH);
            nikola = (User)qm.getRegistryObject(ALIAS_NIKOLA);
        }
        catch (RegistryException e) {
            log.error("Internal error. Could not load predefined users.", e);
        }
    }    
    
    /*
     * Loads the list of RegistryAdministrators from the property file during startup.
     */
    private void loadRegistryAdministrators() {
        String adminList = RegistryProperties.getInstance().getProperty("omar.security.authorization.registryAdministrators");

        if (adminList != null) {
            java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(adminList,
                    "|");

            while (tokenizer.hasMoreTokens()) {
                try {
                    String adminId = tokenizer.nextToken();

                    log.info("getRegistryAdministrators: adding admin '" +
                        adminId + "'");
                    adminIdSet.add(adminId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.err.println(
                "Registry has not defined RegistryAdministrators yet. This can be done by setting the ebxmlrr.security.authorization.registryAdministrators property in ebxmlrr.properties file.");
        }
    }

    /**
     * Gets the User that is associated with the KeyInfo provided within the XMLSignature signature.
     *
     * @throws RegistryException no matching User is found. May need more specific Exception??
     */
    public UserType getUserFromXMLSignature(
        org.apache.xml.security.signature.XMLSignature signature)
        throws RegistryException {
        UserType user = null;

        //The registry expects the KeyInfo to either have the PublicKey or the DN from the public key
        //In case of DN the registry can lookup the public key based on the DN
        java.security.PublicKey publicKey = null;
        X509Certificate cert = null;
        String alias = null;

        try {
            org.apache.xml.security.keys.KeyInfo keyInfo = signature.getKeyInfo();

            for (int i = 0; i < keyInfo.lengthKeyName(); i++) {
                String keyName = keyInfo.itemKeyName(i).getKeyName();
                log.info("getUserFromXMLSignature: KeyName=" + keyName);

                if (keyName.startsWith("urn:uuid:")) {
                    alias = keyName;

                    break;
                }
            }

            log.info("getUserFromXMLSignature: alias=" + alias);

            if (alias == null) {
                publicKey = keyInfo.getPublicKey();
                cert = keyInfo.getX509Certificate();                

                alias = getKeyStore().getCertificateAlias(cert);
            }

            log.info("getUserFromXMLSignature: alias from publicKey=" + alias);
        } catch (KeyStoreException e) {
            throw new RegistryException(e);
        } catch (org.apache.xml.security.keys.keyresolver.KeyResolverException e) {
            throw new RegistryException(e);
        } catch (org.apache.xml.security.exceptions.XMLSecurityException e) {
            throw new RegistryException(e);
        }

        user = getUserFromAlias(alias);
        log.info("getUserFromXMLSignature: user=" + user);

        return user;
    }
    
    /**
     * Gets the User that is associated with the specified certificate.
     *
     * @throws RegistryException no matching User is found. May need more specific Exception??
     */
    public UserType getUserFromCertificate(X509Certificate cert)throws RegistryException {
        UserType user = null;
        
        System.err.println("getUserFromCertificate cert=" + cert);
        if (cert == null) {
            return registryGuest;
        }

        //The registry expects the KeyInfo to either have the PublicKey or the DN from the public key
        //In case of DN the registry can lookup the public key based on the DN
        java.security.PublicKey publicKey = null;
        String alias = null;

        try {
            alias = getKeyStore().getCertificateAlias(cert);
            if (alias == null) {
                System.err.println("Unknown certificate");
                throw new RegistryException("Unknown certificate"); 
            }
        } catch (KeyStoreException e) {
            throw new RegistryException(e);
        }

        user = getUserFromAlias(alias);

        return user;
        
    }
    
    /**
     * Compares two certificates. It will compare the issuerUniqueID and subjectUniqueID
     * fields of the certificates. If either certificate does not contain either
     * field, it will return false.
     */
    private boolean certificatesAreSame(X509Certificate cert,
        X509Certificate oldCert) throws RegistryException {
        boolean[] certIssuerID = cert.getIssuerUniqueID();
        boolean[] oldCertIssuerID = oldCert.getIssuerUniqueID();

        if ((certIssuerID == null) || (oldCertIssuerID == null) ||
                (certIssuerID.length != oldCertIssuerID.length)) {
            return false;
        }

        for (int i = 0; i < certIssuerID.length; i++) {
            if (certIssuerID[i] != oldCertIssuerID[i]) {
                return false;
            }
        }

        boolean[] certSubjectID = cert.getSubjectUniqueID();
        boolean[] oldCertSubjectID = oldCert.getSubjectUniqueID();

        if ((certSubjectID == null) || (oldCertSubjectID == null) ||
                (certSubjectID.length != oldCertSubjectID.length)) {
            return false;
        }

        for (int i = 0; i < certSubjectID.length; i++) {
            if (certSubjectID[i] != oldCertSubjectID[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Add a certificate entry in the keystore.
     * @param userId The alias of the certificate
     * @param signature The XMLSignature containing the certificate
     * @throws UserRegistration fails if the keystore already contrains the entry
     * whose alias is equal to userId
     */
    protected void registerUserCertificate(String userId,
        org.apache.xml.security.signature.XMLSignature signature)
        throws RegistryException {
        java.io.FileOutputStream fos = null;

        try {
            org.apache.xml.security.keys.KeyInfo keyInfo = signature.getKeyInfo();
            java.security.PublicKey publicKey = keyInfo.getPublicKey();

            // The first certificate is assumed as target certificate
            X509Certificate cert = keyInfo.getX509Certificate();

            KeyStore keyStore = getKeyStore();

            // Check if already in store
            X509Certificate oldCert = null;

            try {
                oldCert = getCertificate(userId);
            } catch (Exception e) {
            }

            //System.err.println("Checking the certificates are the same...");
            if ((oldCert != null) && !certificatesAreSame(cert, oldCert)) {
                throw new UserRegistrationException("User registration fails. " +
                    "The user with id '" + userId + "' already exists. " +
                    "If the certificate is recently updatd, replacement of the " +
                    "old certificate in last registration is not allowed");
            }

            /*
            Add the cert. to the keystore if the cert. does not exist yet
             */
            if (oldCert == null) {
                if (propsReader.getProperty(
                            "omar.security.validateCertificates").trim().equals("true")) {
                    validateCertificate(signature);
                }

                synchronized (keyStoreWriteLock) {
                    keyStore.setCertificateEntry(userId, cert);

                    String keystoreFile = getKeyStoreFileName();
                    fos = new java.io.FileOutputStream(keystoreFile);

                    String keystorePass = getKeyStorePassword();
                    keyStore.store(fos, keystorePass.toCharArray());
                    fos.flush();
                    fos.close();
                    this.keyStore = null;
                }
            }
        } catch (KeyStoreException e) {
            throw new UserRegistrationException(e);
        } catch (org.apache.xml.security.keys.keyresolver.KeyResolverException e) {
            throw new UserRegistrationException(e);
        } catch (org.apache.xml.security.exceptions.XMLSecurityException e) {
            throw new UserRegistrationException(e);
        } catch (IOException e) {
            throw new UserRegistrationException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new UserRegistrationException(e);
        } catch (java.security.cert.CertificateException e) {
            throw new UserRegistrationException(e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
/*
 * $Header: /cvsroot/sino/ebxmlrr/src/share/com/sun/ebxml/registry/security/authentication/AuthenticationServiceImpl.java,v 1.27 2003/06/20 14:35:05 farrukh_najmi Exp $
 *
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 */

package com.sun.ebxml.registry.security.authentication;

import com.sun.ebxml.registry.RegistryException;
import com.sun.ebxml.registry.security.UserRegistrationException;
import com.sun.ebxml.registry.util.RegistryProperties;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import org.oasis.ebxml.registry.bindings.rim.User;

/**
 * Manages authentication functionality for the registry.
 * This includes managemnet of user public keys in the server key store.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class AuthenticationServiceImpl /*implements AuthenticationService*/ {
    
    /* Aliases/ids for pre-defined Users.*/
    public static String ALIAS_REGISTRY_OPERATOR = "urn:uuid:921284f0-bbed-4a4c-9342-ecaf0625f9d7";
    public static String ALIAS_REGISTRY_GUEST = "urn:uuid:abfa78d5-605e-4dbc-b9ee-a42e99d5f7cf";
    
    /* Aliases/ids for test Users */
    public static String ALIAS_FARRUKH = "urn:uuid:977d9380-00e2-4ce8-9cdc-d8bf6a4157be";
    public static String ALIAS_NIKOLA = "urn:uuid:85428d8e-1bd5-473b-a8c8-b9d595f82728";
    public static String ALIAS_CY = "urn:uuid:b2691323-4aad-46da-9dc7-a842b7e4b1ae";
    public static String ALIAS_ADRIAN = "urn:uuid:bab82b84-7d63-44dd-b914-e72e0476c043";

    private org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(this.getClass());    
    
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */
    /*# private AuthenticationServiceImpl _authenticationServiceImpl; */
    private static AuthenticationServiceImpl instance = null;
    
    KeyStore keyStore = null;
    /* A lock object to prevent another thread to  access the keystore when
     * there is a thread adding certificate to it and writing it to disk
     */
    Object keyStoreWriteLock = new Object();
    KeyStore trustAnchorsKeyStore;
    User registryGuest = null;
    User registryOperator = null;
    java.util.ArrayList userCache = new java.util.ArrayList();
    java.util.HashMap userMap = new java.util.HashMap();
    java.util.HashSet adminIdSet = new java.util.HashSet();
    
    final int cacheSize;
    RegistryProperties propsReader = RegistryProperties.getInstance();
    
    protected AuthenticationServiceImpl() {
        String userCacheSize = RegistryProperties.getInstance().getProperty("ebxmlrr.security.userCacheSize");
        loadRegistryAdministrators();
        cacheSize = Integer.parseInt(userCacheSize);
    }
    
    public KeyStore getTrustAnchorsKeyStore() throws RegistryException {
        try {
            if (trustAnchorsKeyStore==null) {
                synchronized(AuthenticationServiceImpl.class) {
                    if (trustAnchorsKeyStore==null) {
                        String keyStoreFile = propsReader.getProperty("ebxmlrr.security.trustAnchors.keystoreFile");
                        String keystorePassword = propsReader.getProperty("ebxmlrr.security.trustAnchors.keystorePassword");
                        String keystoreType = propsReader.getProperty("ebxmlrr.security.trustAnchors.keystoreType");
                        trustAnchorsKeyStore = KeyStore.getInstance(keystoreType);
                        trustAnchorsKeyStore.load(new java.io.FileInputStream(keyStoreFile), keystorePassword.toCharArray());
                    }
                }
            }
            return trustAnchorsKeyStore;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RegistryException("Cannot load the trust anchors keystore", e);
        }
        catch (KeyStoreException e) {
            throw new RegistryException("Cannot load the trust anchors keystore", e);
        }
        catch (java.security.cert.CertificateException e) {
            throw new RegistryException("Cannot load the trust anchors keystore", e);
        }
        catch (java.io.FileNotFoundException e) {
            throw new RegistryException("Cannot load the trust anchors keystore", e);
        }
        catch (IOException e) {
            throw new RegistryException("Cannot load the trust anchors keystore", e);
        }
    }
    
    /**
     * Get the keystore whose path is specified  by {@link #getKeyStoreFileName()}.
     * Note that all the methods that access the keystore MUST access the keystore
     * via this method. Do not access the keystore directly by accessing the keystore
     * field. Otherwise the checking the write lock to keystore will be bypassed.
     */
    public KeyStore getKeyStore() throws RegistryException {
        synchronized(keyStoreWriteLock) {
            if (keyStore == null) {
                
                java.io.FileInputStream fis = null;
                try {
                    keyStore = KeyStore.getInstance("JKS");
                    
                    String keystoreFile = getKeyStoreFileName();
                    fis = new java.io.FileInputStream(keystoreFile);
                    
                    String keystorePass = getKeyStorePassword();
                    keyStore.load(fis, keystorePass.toCharArray());
                }
                catch (java.security.cert.CertificateException e) {
                    throw new RegistryException(e);
                }
                catch (KeyStoreException e) {
                    throw new RegistryException(e);
                }
                catch (NoSuchAlgorithmException e) {
                    throw new RegistryException(e);
                }
                catch (java.io.FileNotFoundException e) {
                    throw new RegistryException(e);
                }
                catch (IOException e) {
                    throw new RegistryException(e);
                }
                finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    
                }
            }
            return keyStore;
        }
    }
    
    public java.security.PrivateKey getPrivateKey(String alias, String password) throws RegistryException {
        java.security.PrivateKey privateKey =null;
        
        try {
            privateKey = (java.security.PrivateKey) getKeyStore().getKey(alias, password.toCharArray());
        }
        catch (KeyStoreException e) {
            throw new RegistryException("Error getting private key", e);
        }
        catch (NoSuchAlgorithmException e) {
            throw new RegistryException("Error getting private key", e);
        }
        catch (java.security.UnrecoverableKeyException e) {
            throw new RegistryException("Error getting private key", e);
        }
        return privateKey;
    }
    
    public X509Certificate getCertificate(String alias) throws RegistryException {
        X509Certificate cert =null;
        
        try {
            cert = (X509Certificate) getKeyStore().getCertificate(alias);
            if (cert == null) {
                throw new RegistryException("Certificate not found for alias '" + alias + "'");
            }
        }
        catch (KeyStoreException e) {
            throw new RegistryException("Error getting certificate", e);
        }
        return cert;
    }
    
    public java.security.cert.Certificate [] getCertificateChain(String alias) throws RegistryException {
        try {
            return getKeyStore().getCertificateChain(alias);
        }
        catch (KeyStoreException e) {
            throw new RegistryException("Error getting certificate chain", e);
        }
    }
    
    public static AuthenticationServiceImpl getInstance() {
        if (instance == null) {
            synchronized(com.sun.ebxml.registry.security.authentication.AuthenticationServiceImpl.class) {
                if (instance == null) {
                    instance = new com.sun.ebxml.registry.security.authentication.AuthenticationServiceImpl();
                }
            }
        }
        return instance;
    }
    
    public String getKeyStoreFileName() throws RegistryException {
        String fileName = RegistryProperties.getInstance().getProperty("ebxmlrr.security.keystoreFile");
        return fileName;
    }
    
    public String getKeyStorePassword() throws RegistryException {
        String pw = RegistryProperties.getInstance().getProperty("ebxmlrr.security.keystorePassword");
        return pw;
    }
    
    /**
     * Check if the signatures CA is trusted by the registry.
     *
     * @throws UserRegistrationException if the certificate issuing CA is not trusted.
     * @throws RegistryException if the certificates cannot be verified for some other reasons, such as unable to load
     * trust anchors keystore
     */    
    public void validateCertificate(org.apache.xml.security.signature.XMLSignature signature) throws UserRegistrationException, RegistryException	{
        try {
            java.security.cert.CertPathBuilder certPathBuilder = java.security.cert.CertPathBuilder.getInstance("PKIX");
            java.security.cert.X509CertSelector targetConstraints = new java.security.cert.X509CertSelector();
            
            org.apache.xml.security.keys.KeyInfo keyInfo = signature.getKeyInfo();
            int lengthX509Data = keyInfo.lengthX509Data();
            for (int i=0; i < lengthX509Data; i++) {
                org.apache.xml.security.keys.content.X509Data x509Data = keyInfo.itemX509Data(i);
                X509Certificate x509Certificate = x509Data.itemCertificate(0).getX509Certificate();
                targetConstraints.setSubject(x509Certificate.getSubjectX500Principal().getEncoded());
            }
            java.security.cert.PKIXBuilderParameters params = new java.security.cert.PKIXBuilderParameters(getTrustAnchorsKeyStore()
            , targetConstraints);
            java.security.cert.CollectionCertStoreParameters ccsp = new java.security.cert.CollectionCertStoreParameters();
            java.security.cert.CertStore store = java.security.cert.CertStore.getInstance("Collection", ccsp);
            params.addCertStore(store);
            certPathBuilder.build(params).getCertPath();
        }
        catch (NoSuchAlgorithmException e) {
            throw new RegistryException("Cannot verify the certificates", e);
        }
        catch (org.apache.xml.security.exceptions.XMLSecurityException e) {
            throw new RegistryException("Cannot verify the certificates", e);
        }
        catch (IOException e) {
            throw new RegistryException("Cannot verify the certificates", e);
        }
        catch (KeyStoreException e) {
            throw new RegistryException("Cannot verify the certificates", e);
        }
        catch(java.security.InvalidAlgorithmParameterException e) {
            throw new RegistryException("Cannot verify the certificates", e);
        }
        catch(java.security.cert.CertPathBuilderException e) {
            throw new UserRegistrationException("User registration fails. The certificate is not " +
            "issued by the trusted certificate authority", e);
        }
    }
    
    /**
     * Gets the alias within the KeyStore for a User
     */
    public String getAliasFromUser(User user) throws RegistryException {
        return user.getId();
    }
    
    /**
     * Gets the alias within the KeyStore for a User
     */
    public X509Certificate getCertificateFromUser(User user) throws RegistryException {
        X509Certificate cert = null;
        try {
            String alias = getAliasFromUser(user);
            
            cert = (X509Certificate)(getKeyStore().getCertificate(alias));
        }
        catch (KeyStoreException e) {
            throw new RegistryException(e);
        }
        
        return cert;
    }
    
    /**
     * Gets the User that is associated with the KeyInfo provided within the XMLSignature signature.
     *
     * @throws RegistryException no matching User is found. May need more specific Exception??
     */
    public User getUserFromAlias(String alias) throws RegistryException {
        User user = null;
        
        String userId = alias;
        
        /*
        if (alias.equals(ALIAS_REGISTRY_GUEST) && registryGuest != null) {
                return registryGuest;
        }
        else if (alias.equals(ALIAS_REGISTRY_OPERATOR) && registryOperator != null) {
                return registryOperator;
        }
        else {
                int index = userCache.indexOf(alias);
                // cache hit
                if (index != -1) {
                        userCache.add((String) userCache.remove(index));
                        return (User) userMap.get(alias);
                }
        }
         */
        
        com.sun.ebxml.registry.persistence.rdb.UserDAO userDAO = new com.sun.ebxml.registry.persistence.rdb.UserDAO();
        String sqlQuery = "SELECT id FROM " +
        userDAO.getTableName() + " WHERE id='" +
        userId + "'";
        
        org.oasis.ebxml.registry.bindings.query.ResponseOption responseOption = new org.oasis.ebxml.registry.bindings.query.ResponseOption();
        
        responseOption.setReturnType(org.oasis.ebxml.registry.bindings.query.types.ReturnTypeType.LEAFCLASS);
        responseOption.setReturnComposedObjects(true);

        
        org.oasis.ebxml.registry.bindings.query.SQLQueryResult sqlQueryResult = com.sun.ebxml.registry.query.sql.SQLQueryProcessor.
        getInstance().executeQuery(null, sqlQuery, responseOption).
        getSQLQueryResult();
        
        if (sqlQueryResult.getLeafRegistryObjectListTypeItemCount() > 0) {
            user = sqlQueryResult.getLeafRegistryObjectListTypeItem(0).getUser();
        }
        
        if (user == null) {
            throw new com.sun.ebxml.registry.security.UserNotFoundException(userId);
        }
        
        //See if User need to be auto-classified as RegistryAdministrator
        boolean isAdmin = isRegistryAdministrator(user);
        
        if (isAdmin) {
            //Make sure that the user is classified with the RegistryAdministrator role
            makeRegistryAdministrator(user);
        }
        
        /*
        if (alias.equals(ALIAS_REGISTRY_GUEST)) {
                registryGuest = user;
        }
        else if (alias.equals(ALIAS_REGISTRY_OPERATOR)) {
                registryOperator = user;
        }
        else {
                if (userCache.size() == cacheSize) {
                        userMap.remove((String) userCache.get(0));
                        userCache.remove(0);
                }
                userCache.add(alias);
                userMap.put(alias, user);
        }
         */
        
        return user;
    }
        
    /**
     * See if User is declared as a RegistryAdministrator in prop file.
     */
    private boolean isRegistryAdministrator(User user) throws RegistryException {
        boolean isAdmin = false;
        
        if (user != null) {
            String id = user.getId();
            if (adminIdSet.contains(id)) {
                isAdmin = true;
            }
        }
        
        return isAdmin;
    }

    /**
     * Make sure user gets auto-classified as RegistryAdministrator if not so already.
     */
    private void makeRegistryAdministrator(User user) throws RegistryException {        
        if (user != null) {
            com.sun.ebxml.registry.security.authorization.AuthorizationServiceImpl az = 
                com.sun.ebxml.registry.security.authorization.AuthorizationServiceImpl.getInstance();

            boolean isAdmin = az.isRegistryAdministrator(user);
            if (!isAdmin) {
                org.oasis.ebxml.registry.bindings.rim.Classification classification = new org.oasis.ebxml.registry.bindings.rim.Classification();
                classification.setClassificationNode(az.CANONICAL_ID_NODE_REGISTRY_ADMINISTRATOR);
                classification.setClassifiedObject(user);
                user.addClassification(classification);
                
                //Now persists updated User 
                com.sun.ebxml.registry.persistence.PersistenceManagerImpl pm = 
                    com.sun.ebxml.registry.persistence.PersistenceManagerImpl.getInstance();
                
                java.util.ArrayList al = new java.util.ArrayList();
                al.add(user);
                pm.update(user, al);
            }            
        }        
    }
    
    
    /*
     * Loads the list of RegistryAdministrators from the property file during startup.
     */
    private void loadRegistryAdministrators() {
        String adminList = RegistryProperties.getInstance().getProperty("ebxmlrr.security.authorization.registryAdministrators");
        
        if (adminList != null) {
            java.util.StringTokenizer tokenizer =
            new java.util.StringTokenizer(adminList, "|" );
            
            while ( tokenizer.hasMoreTokens() ) {
                try {
                    String adminId = tokenizer.nextToken();
                    
                    log.info("getRegistryAdministrators: adding admin '" + adminId + "'");
                    adminIdSet.add(adminId);
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            }
        }
        else {
            System.err.println("Registry has not defined RegistryAdministrators yet. This can be done by setting the ebxmlrr.security.authorization.registryAdministrators property in ebxmlrr.properties file.");
        }        
    }
    
    /**
     * Gets the User that is associated with the KeyInfo provided within the XMLSignature signature.
     *
     * @throws RegistryException no matching User is found. May need more specific Exception??
     */
    public User getUserFromXMLSignature(org.apache.xml.security.signature.XMLSignature signature) throws RegistryException {
        User user = null;
        
        //The registry expects the KeyInfo to either have the PublicKey or the DN from the public key
        //In case of DN the registry can lookup the public key based on the DN
        
        java.security.PublicKey publicKey = null;
        X509Certificate cert = null;
        String alias = null;
        
        try {
            org.apache.xml.security.keys.KeyInfo keyInfo = signature.getKeyInfo();
            for (int i=0 ; i<keyInfo.lengthKeyName() ; i++) {
                String keyName = keyInfo.itemKeyName(i).getKeyName();
                log.info("getUserFromXMLSignature: KeyName=" + keyName);
                if (keyName.startsWith("urn:uuid:")) {
                    alias = keyName;
                    break;
                }
            }
            
            log.info("getUserFromXMLSignature: alias=" + alias);
            
            if (alias == null) {
                publicKey = keyInfo.getPublicKey();
                cert = keyInfo.getX509Certificate();
                
                alias = getKeyStore().getCertificateAlias(cert);
            }
            log.info("getUserFromXMLSignature: alias from publicKey=" + alias);

        }
        catch (KeyStoreException e) {
            throw new RegistryException(e);
        }
        catch (org.apache.xml.security.keys.keyresolver.KeyResolverException e) {
            throw new RegistryException(e);
        }
        catch (org.apache.xml.security.exceptions.XMLSecurityException e) {
            throw new RegistryException(e);
        }
        
        
        user = getUserFromAlias(alias);
        log.info("getUserFromXMLSignature: user=" + user);
        return user;
    }
    
    /**
     * Compares two certificates. It will compare the issuerUniqueID and subjectUniqueID
     * fields of the certificates. If either certificate does not contain either
     * field, it will return false.
     */
    private boolean certificatesAreSame(X509Certificate cert, X509Certificate oldCert) throws RegistryException {
        
        boolean [] certIssuerID = cert.getIssuerUniqueID();
        boolean [] oldCertIssuerID = oldCert.getIssuerUniqueID();
        
        if (certIssuerID == null || oldCertIssuerID == null ||
        certIssuerID.length != oldCertIssuerID.length) {
            return false;
        }
        
        for(int i=0; i < certIssuerID.length; i++) {
            if (certIssuerID[i] != oldCertIssuerID[i]) {
                return false;
            }
        }
        
        boolean [] certSubjectID = cert.getSubjectUniqueID();
        boolean [] oldCertSubjectID = oldCert.getSubjectUniqueID();
        
        if (certSubjectID == null || oldCertSubjectID == null || certSubjectID.length !=
        oldCertSubjectID.length) {
            return false;
        }
        
        for(int i=0; i < certSubjectID.length; i++) {
            if (certSubjectID[i] != oldCertSubjectID[i]) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Add a certificate entry in the keystore.
     * @param userId The alias of the certificate
     * @param signature The XMLSignature containing the certificate
     * @throws UserRegistration fails if the keystore already contrains the entry
     * whose alias is equal to userId
     */
    protected void registerUserCertificate(String userId, org.apache.xml.security.signature.XMLSignature signature) throws RegistryException {
        
        java.io.FileOutputStream fos = null;
        try {
            org.apache.xml.security.keys.KeyInfo keyInfo = signature.getKeyInfo();
            java.security.PublicKey publicKey = keyInfo.getPublicKey();
            // The first certificate is assumed as target certificate
            X509Certificate cert = keyInfo.getX509Certificate();
            
            KeyStore keyStore = getKeyStore();
            
            // Check if already in store
            X509Certificate oldCert = null;
            try {
                oldCert = getCertificate(userId);
            }
            catch (Exception e) {
            }
            //System.err.println("Checking the certificates are the same...");
            
            if (oldCert != null && !certificatesAreSame(cert, oldCert)) {
                throw new UserRegistrationException("User registration fails. "
                + "The user with id '" + userId + "' already exists. "
                + "If the certificate is recently updatd, replacement of the "
                + "old certificate in last registration is not allowed");
            }
            /*
            Add the cert. to the keystore if the cert. does not exist yet
             */
            if (oldCert == null) {
                if (propsReader.getProperty("ebxmlrr.security.validateCertificates").trim().equals("true")) {
                    validateCertificate(signature);
                }
                synchronized (keyStoreWriteLock) {
                    keyStore.setCertificateEntry(userId, cert);
                    String keystoreFile = getKeyStoreFileName();
                    fos = new java.io.FileOutputStream(keystoreFile);
                    
                    String keystorePass = getKeyStorePassword();
                    keyStore.store(fos, keystorePass.toCharArray());
                    fos.flush();
                    fos.close();
                    this.keyStore = null;
                }
            }
        }
        catch (KeyStoreException e) {
            throw new UserRegistrationException(e);
        }
        catch (org.apache.xml.security.keys.keyresolver.KeyResolverException e) {
            throw new UserRegistrationException(e);
        }
        catch (org.apache.xml.security.exceptions.XMLSecurityException e) {
            throw new UserRegistrationException(e);
        }
        catch (IOException e) {
            throw new UserRegistrationException(e);
        }
        catch (NoSuchAlgorithmException e) {
            throw new UserRegistrationException(e);
        }
        catch (java.security.cert.CertificateException e) {
            throw new UserRegistrationException(e);
        }
        finally {
            if (fos != null) {
                try {
                    fos.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
        }
        
    }
    
    
    public static void main(String[] args) throws Exception {
        
        com.sun.ebxml.registry.security.authorization.AuthorizationServiceImpl az = 
            com.sun.ebxml.registry.security.authorization.AuthorizationServiceImpl.getInstance();
        AuthenticationServiceImpl service = AuthenticationServiceImpl.getInstance();
        String alias = "urn:uuid:85428d8e-1bd5-473b-a8c8-b9d595f82728"; //service.ALIAS_REGISTRY_GUEST;
        
        User user = service.getUserFromAlias(alias);
        boolean isAdmin = az.isRegistryAdministrator(user);
        
        System.err.println("isAdmin = " + isAdmin);
        //java.security.PrivateKey key = service.getPrivateKey(alias, alias);
        //X509Certificate cert = (X509Certificate)service.getKeyStore().getCertificate(alias);
        //service.validateCertificate(cert);                
        
        //System.err.println(key);        
    }
    
}
