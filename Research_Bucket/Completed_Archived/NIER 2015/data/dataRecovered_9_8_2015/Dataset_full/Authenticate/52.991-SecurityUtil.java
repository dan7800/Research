/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/ui/thin/security/SecurityUtil.java,v 1.2 2004/04/01 18:30:35 psterk Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.thin.security;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.User;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.Query;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Connection;

import org.freebxml.omar.client.xml.registry.util.ProviderProperties;
import org.freebxml.omar.client.xml.registry.ConnectionImpl;
import org.freebxml.omar.client.xml.registry.util.KeystoreUtil;

import org.apache.commons.logging.LogFactory;

/**
 *
 * Some utility methods related to XML security
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/ui/thin/security/SecurityUtil.java,v 1.2 2004/04/01 18:30:35 psterk Exp $
 *
 */
public class SecurityUtil {
    
    private ConnectionImpl connection = null;
    private static SecurityUtil instance = null;
    private org.apache.commons.logging.Log log = LogFactory.getLog(this.getClass());

    protected SecurityUtil() throws JAXRException {        
        ProviderProperties props = ProviderProperties.getInstance();
        String registryUrl = props.getProperty("jaxr-ebxml.soap.url");
        if (registryUrl == null) {
            registryUrl = "http://localhost:8080/omar/registry/soap";
        }
        props.put("javax.xml.registry.queryManagerURL", registryUrl);
            System.setProperty("javax.xml.registry.ConnectionFactoryClass",
                "org.freebxml.omar.client.xml.registry.ConnectionFactoryImpl");
        ConnectionFactory connFactory = ConnectionFactory.newInstance();
        connection = (ConnectionImpl)connFactory.createConnection();
    }

    /** 
     * The purpose of this method to determine if a <code>java.util.Set</code>
     * of credentials needs to be set on the <code>javax.registry.Connection
     * </code> object. This determination considers whether or not the web
     * container or policy is providing authentication services.
     * 
     * @param principal
     *    A <code>java.security.Principal</code> object
     * @param connection
     *    A <code>java.security.Connection</code> object
     */    
    public void handleCredentials(Principal principal, Connection connection)
        throws JAXRException {
        boolean isAuthenticated = (principal != null);
        if (isAuthenticated) {
            // This servlet is protected by the web contianer or policy
            // agent. To have gotten here, the user must have logged in
            // successfully.
            String principalName = principal.getName();
            String guestPrincipalName = ProviderProperties.getInstance().getProperty
                ("jaxr-ebxml.security.guestPrincipalName");
            if (! principalName.equals(guestPrincipalName)) {
                Set credentials = getCredentials(principalName);
                if ((credentials == null) || credentials.isEmpty()) {
                    credentials = generateCredentials(principalName);
                }
                setCredentials(credentials, connection);
            }
        }
        else {
            // This servlet is not being protected by the web container
            // or policy agent.
            String principalName = ProviderProperties.getInstance().getProperty
                ("jaxr-ebxml.security.anonymousUserKeystoreAlias");
            if (principalName != null) {
                Set credentials = getCredentials(principalName);
                if ((credentials != null) && !credentials.isEmpty()) {
                    setCredentials(credentials, connection);
                }
            }
        }
    }

     /** 
      * The purpose of this method is to determine the return status based on 
      * the principal.  The return status is a <code>java.lang.String</code>.
      * Web app frameworks, such as JSF, use the status to determine page
      * navigation. 
      *
      * @param principal
      *     A <code>java.security.Principal</code> object
      * @return
      *     A <code>java.lang.String</code> representing the status.  Typically,
      *     the status is 'success' or 'failure', but it can have other values.
      */
    public String getStatus(Principal principal) throws JAXRException {
        String status = null;
        boolean isAuthenticated = (principal != null);
        if (isAuthenticated) {
            // This servlet is protected by the web contianer or policy
            // agent. To have gotten here, the user must have logged in
            // successfully.
            String principalName = principal.getName();
            String guestPrincipalName = ProviderProperties.getInstance().getProperty
                ("jaxr-ebxml.security.guestPrincipalName");
            if (principalName.equals(guestPrincipalName)) {
                status = "success";
            } else {
                boolean isRegistered = (findUserByPrincipalName(principalName) != null);
                if (isRegistered) {
                    status = "success";
                }
                else {
                    // Return the self-registration form to the browser.
                    status = "failure";
                }
            }
        } else {
            status = "success";
        }
        
        return status;    
    }
    
     /**
      *
      * @param principalName
      * @throws JAXRException
      * @return
      */    
    public User findUserByPrincipalName(String principalName) throws JAXRException {
        User user = null;
        DeclarativeQueryManager dqm = connection.getRegistryService().getDeclarativeQueryManager();
        String queryString = 
            "SELECT * " + 
            "FROM user_ u, slot s " +
            "WHERE u.id = s.parent AND s.name_='PrincipalName' AND value='" + principalName + "'";
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryString);
        BulkResponse br = dqm.executeQuery(query);
        Iterator results = br.getCollection().iterator();
        while (results.hasNext()) {
            user = (User)results.next();
            break;
        }
        return user;
    }
    
     /** Get the credentials for the specified principal.
      *
      * @param alias
      *     The principal of the user making the request on the registry.
      *     This value will be obtained from the web container hosting the
      *     registry client by calling HttpServletRequest.getUserPrincipal().
      *     If this value is <code>null</code>, or if <code>principal.getName()</code>
      *     is <code>null</code>, then the default principal name, as specified
      *     by the <i>jaxr-ebxml.security.defaultPrincipalName</i> property
      *     will be used. If this property is not set, then an empty Set will
      *     be returned.
      * @return
      *     A Set of X500PrivateCredential objects representing the user's
      *     credentials. If this set is empty or null, no credentials will
      *     be passed to the registry with the request. The registry treats
      *     such requests as coming from the Registry Guest user.
      * @throws JAXRException
      *     Thrown if an error occurs while trying to map the principal
      *     to its credentials. An exception should not be thrown if there are
      *     no credentials associated with the principal. In this case, an
      *     empty Set should be returned.
      */
    public Set getCredentials(String alias) throws JAXRException {

        HashSet credentials = new HashSet();
        
        if (alias == null) {
            return credentials;
        }
        
        log.debug("Getting credentials for '" + alias + "'");

        try {
            credentials.add(org.freebxml.omar.client.xml.registry.util.SecurityUtil.
                getInstance().aliasToX500PrivateCredential(alias));
        }
        catch (JAXRException je) {
            // aliasToX500PrivateCredential() throws an exception if no certificate
            // can be found for the specified alias. For our purposes, this
            // is not an exception, so we just ignore such exceptions and
            // propogate all others.
            if (!je.getMessage().equals("Unknown alias in keystore") &&
                !je.getMessage().startsWith("KeyStore file not found")) 
            {
                throw je;
            }
            else {
                log.warn("Failed to get credentials for '" + alias + "'.\n" +
                    "Exception: " + je.getMessage());
            }
        }

        return credentials;
    }
    
     /** Wrapper for ConnectionImpl.setCredentials() that ignores null or
      * empty credential sets.
      */
    public void setCredentials(Set credentials, Connection connection) throws JAXRException {
        if ((credentials != null) && !credentials.isEmpty()) {
            connection.setCredentials(credentials);
        }
    }
    
    /** Generate a key pair and add it to the keystore.
      *
      * @param alias
      * @return
      *     A HashSet of X500PrivateCredential objects.
      * @throws Exception
      */    
    private Set generateCredentials(String alias) throws JAXRException {
        
        try {
            HashSet credentials = new HashSet();

            // The keystore file is at ${jaxr-ebxml.home}/security/keystore.jks. If
            // the 'jaxr-ebxml.home' property is not set, ${user.home}/jaxr-ebxml/ is
            // used.
            File keyStoreFile = KeystoreUtil.getKeystoreFile();
            String storepass = ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.storepass");
            String keypass = ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.keypass");

            log.debug("Generating key pair for '" + alias + "' in '" + keyStoreFile.getAbsolutePath() + "'");

// When run in S1WS 6.0, this caused some native library errors. It appears that S1WS
// uses different encryption spis than those in the jdk. 
//            String[] args = {
//                "-genkey", "-alias", uid, "-keypass", "keypass",
//                "-keystore", keyStoreFile.getAbsolutePath(), "-storepass",
//                new String(storepass), "-dname", "uid=" + uid + ",ou=People,dc=sun,dc=com"
//            };
//            KeyTool keytool = new KeyTool();
//            ByteArrayOutputStream keytoolOutput = new ByteArrayOutputStream();
//            try {
//                keytool.run(args, new PrintStream(keytoolOutput));
//            }
//            finally {
//                log.info(keytoolOutput.toString());
//            }
// To work around this problem, generate the key pair using keytool (which executes
// in its own vm. Note that all the parameters must be specified, or keytool prompts
// for their values and this 'hangs'
            String[] cmdarray = {
                "keytool", 
                "-genkey", "-alias", alias, "-keypass", keypass,
                "-keystore", keyStoreFile.getAbsolutePath(), 
                "-storepass", storepass, "-dname", "cn=" + alias
            };
            Process keytool = Runtime.getRuntime().exec(cmdarray);
            try {
                keytool.waitFor();
            }
            catch (InterruptedException ie) {
            }
            if (keytool.exitValue() != 0) {
                throw new JAXRException("keytool command failed. Exit status: " + keytool.exitValue());
            }
            log.debug("Key pair generated successfully.");

            // After generating the keypair in the keystore file, we have to reload
            // SecurityUtil's KeyStore object.
            KeyStore keyStore = org.freebxml.omar.client.xml.registry.util.SecurityUtil.
                getInstance().getKeyStore();
            keyStore.load(new FileInputStream(keyStoreFile), storepass.toCharArray());

            credentials.add(org.freebxml.omar.client.xml.registry.util.SecurityUtil.
                getInstance().aliasToX500PrivateCredential(alias));

            return credentials;
        }
        catch (Exception e) {
            if (e instanceof JAXRException) {
                throw (JAXRException)e;
            }
            else {
                throw new JAXRException(e);
            }
        }
    }
    
    /**
     * Method main
     *
     * @param unused
     * @throws Exception
     */
    public static void main(String[] unused) throws Exception {
    }

    public static SecurityUtil getInstance() throws JAXRException {
        if (instance == null) {
            synchronized (SecurityUtil.class) {
                if (instance == null) {
                    instance = new SecurityUtil();
                }
            }
        }

        return instance;
    }
}
