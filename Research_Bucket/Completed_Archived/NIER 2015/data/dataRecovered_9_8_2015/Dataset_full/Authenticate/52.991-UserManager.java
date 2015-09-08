/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/client/ui/swing/registration/UserManager.java,v 1.4 2004/03/12 21:42:04 dhilder Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing.registration;

import org.apache.commons.logging.Log;

import org.freebxml.omar.client.ui.swing.JAXRClient;
import org.freebxml.omar.client.ui.swing.RegistryBrowser;
import org.freebxml.omar.client.xml.registry.ConnectionImpl;
import org.freebxml.omar.client.xml.registry.RegistryServiceImpl;
import org.freebxml.omar.client.xml.registry.util.KeystoreUtil;

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.User;


/**
 * User registration tool.
 */
public class UserManager {
    /** DOCUMENT ME! */
    private static final UserManager instance = new UserManager();

    /** Create a static reference to the logging service. */
    private static Log log = null;

    static {
        try {
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();
            log = ((RegistryServiceImpl) (lcm.getRegistryService())).getConnection()
                   .getConnectionFactory().getLog();
        } catch (JAXRException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new UserManager object.
     */
    private UserManager() {
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static UserManager getInstance() {
        return instance;
    }

    /*
     * Register a new user
     *
     */
    public void registerNewUser() throws Exception {
        try {
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();

            UserModel userModel = new UserModel(lcm.createUser());
            UserRegistrationPanel userRegPanel = new UserRegistrationPanel(userModel);
            UserRegistrationDialog dialog = new UserRegistrationDialog(userRegPanel,
                    userModel);

            dialog.setVisible(true);

            if (dialog.getStatus() != UserRegistrationDialog.OK_STATUS) {
                return;
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    /** First check if certificate already exists in client keystore. If it does,
      * use it. If not then create a self signed certificate for the user and use it to
      * authenticate with the ebxmlrr server.
      * If the authentication is sucessful, save the user model to the server.
      *
      * @throw Exception
      *     An exception could indicate either a communications problem or an
      *     authentication error.
      */
    public static void authenticateAndSaveUser(UserModel userModel)
        throws Exception 
    {
        try {
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();
            RegistryServiceImpl rs = (RegistryServiceImpl) lcm.getRegistryService();
            ConnectionImpl connection = (ConnectionImpl) rs.getConnection();

            if (!certificateExists(userModel.getAlias(), userModel.getStorePassword())) {
                UserManager.generateSelfSignedCertificate
                    (userModel.getUser(), userModel.getAlias(), 
                    userModel.getKeyPassword(), userModel.getStorePassword());
            }

            // Force re-authentication in case credentials are already set
            connection.authenticate();

            RegistryBrowser.setWaitCursor();

            // Now save the User
            ArrayList objects = new ArrayList();
            objects.add(userModel.getUser());
            client.saveObjects(objects);

            // saveObjects uses XML-Security which overwrites the log4j
            // configuration and we never get to see this:
            log.info("Saved user \"" +
                userModel.getUser().getPersonName().getFullName() +
                "\" on server.");
        } 
        catch (Exception e) {
            // Remove the self-signed certificate from the keystore, if one
            // was created during the self-registration process
            try {
                if (userModel != null) {
                    String alias = userModel.getAlias();

                    if (alias != null) {
                        UserManager.removeCertificate(alias,
                            userModel.getStorePassword());
                    }
                }
            } catch (Exception removeCertException) {
                log.warn("Failed to remove the certificate from the keystore " +
                    "that was generated during the self-registration process.",
                    removeCertException);
            }

            throw e;
        } finally {
            RegistryBrowser.setDefaultCursor();
        }
    }

    /*
     * Return true if certificate exists in client keystore, false if not.
     *
     */
    public static boolean certificateExists(String alias, char[] storePass)
        throws Exception {
        boolean exists = false;

        try {
            File keystoreFile = KeystoreUtil.getKeystoreFile();
            KeystoreUtil.createKeystoreDirectory(keystoreFile);

            String[] args = {
                "-list", "-alias", alias, "-keystore",
                keystoreFile.getAbsolutePath(), "-storepass",
                new String(storePass)
            };

            KeyTool keytool = new KeyTool();
            keytool.run(args, System.out);

            exists = true;

            log.info("Alias exists \"" + alias + "\" in keystore \"" +
                keystoreFile.getAbsolutePath() + "\"");
        } catch (Exception e) {
            //Cert does not exists.
        }

        return exists;
    }

    /** Generate a self signed certificate and store it in the keystore.
      *
      * @param user
      * @param alias
      * @param keyPass
      * @param storePass
      * @throws Exception
      */
    public static void generateSelfSignedCertificate(User user, 
                                                     String alias,
                                                     char[] keyPass,
                                                     char[] storePass) 
        throws Exception 
    {
        String dname = getDNameFromUser(user);
        File keystoreFile = KeystoreUtil.getKeystoreFile();
        KeystoreUtil.createKeystoreDirectory(keystoreFile);

        String[] args = {
            "-genkey", "-alias", alias, "-keypass", new String(keyPass),
            "-keystore", keystoreFile.getAbsolutePath(), "-storepass",
            new String(storePass), "-dname", dname
        };

        KeyTool keytool = new KeyTool();
        keytool.run(args, System.out);

        log.info("Stored user \"" + alias + "\" in keystore \"" +
            keystoreFile.getAbsolutePath() + "\"");
    }

    /** Remove an alias from the keystore.
      * <p>
      * Currently, this is only used to "backout" a generated key when self
      * registration fails.
      */
    public static void removeCertificate(String alias, char[] storePass)
        throws Exception {
        File keystoreFile = KeystoreUtil.getKeystoreFile();
        String[] args = {
            "-delete", "-alias", alias, "-keystore",
            keystoreFile.getAbsolutePath(), "-storepass", new String(storePass)
        };
        KeyTool keytool = new KeyTool();
        keytool.run(args, System.out);
        log.info("Removed user \"" + alias + "\" from keystore \"" +
            keystoreFile.getAbsolutePath() + "\"");
    }

    /**
     * DOCUMENT ME!
     *
     * @param user DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    private static String getDNameFromUser(User user) throws JAXRException {
        String dname = "CN=";

        JAXRClient client = RegistryBrowser.getInstance().getClient();
        BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();

        Collection addresses = user.getPostalAddresses();
        PostalAddress address;
        PersonName personName = user.getPersonName();

        //CN=Farrukh Najmi, OU=freebxml.org, O=ebxmlrr, L=Islamabad, ST=Punjab, C=PK
        if (personName == null) {
            personName = lcm.createPersonName("firstName", "middleName",
                    "lastName");
        }

        if ((addresses != null) && (addresses.size() > 0)) {
            address = (PostalAddress) (addresses.iterator().next());
        } else {
            address = lcm.createPostalAddress("number", "street", "city",
                    "state", "country", "postalCode", "Office");
        }

        String city = address.getCity();

        if ((city == null) || (city.length() == 0)) {
            city = "Unknown";
        }

        String state = address.getStateOrProvince();

        if ((state == null) || (state.length() == 0)) {
            state = "Unknown";
        }

        String country = address.getCountry();

        if ((country == null) || (country.length() == 0)) {
            country = "US";
        }

        if (country.length() > 0) {
            country = country.substring(0, 2);
        }

        dname += (personName.getFirstName() + " " + personName.getMiddleName() +
        " " + personName.getLastName() + ", OU=Unknown, O=Unknown, L=" + city +
        ", ST=" + state + ", C=" + country);

        return dname;
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

package com.sun.xml.registry.client.browser.registration;

import com.sun.xml.registry.client.browser.JAXRClient;
import com.sun.xml.registry.client.browser.RegistryBrowser;
import com.sun.xml.registry.ebxml.ConnectionImpl;
import com.sun.xml.registry.ebxml.RegistryServiceImpl;
import com.sun.xml.registry.ebxml.util.KeystoreUtil;

import org.apache.commons.logging.Log;

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.User;


/**
 * User registration tool.
 */
public class UserManager {

    /** DOCUMENT ME! */
    private static final UserManager instance = new UserManager();
	
    /** Create a static reference to the logging service. */
    private static Log log = null;
    static {
        try {
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();
            log = ((RegistryServiceImpl)(lcm.getRegistryService())).getConnection()
                                                                   .getConnectionFactory()
                                                                   .getLog();
        } 
        catch (JAXRException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new UserManager object.
     */
    private UserManager() {
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static UserManager getInstance() {

        return instance;
    }

    /*
     * Register a new user
     *
     */
    public void registerNewUser() throws Exception {

        try {

            JAXRClient client = RegistryBrowser.getInstance().getClient();
            BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();

            UserModel userModel = new UserModel(lcm.createUser());
            UserRegistrationPanel userRegPanel =
                new UserRegistrationPanel(userModel);
            UserRegistrationDialog dialog =
                new UserRegistrationDialog(userRegPanel, userModel);

            dialog.setVisible(true);

            if (dialog.getStatus() != UserRegistrationDialog.OK_STATUS) {
                return;
            }
        }
		catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    /** 
      * First check if certificate already exists in client keystore. If it does
      * use it. If not then create a self signed certificate for the user and use it to
      * authenticate with the ebxmlrr server. 
      * If the authentication is sucessful, save the
      * user model to the server.
      *
      * @throw Exception
      *		An exception could indicate either a communications problem or an
      *		authentication error.
      */
    public static void authenticateAndSaveUser(UserModel userModel) 
        throws Exception
    {
        try {
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();
            RegistryServiceImpl rs = (RegistryServiceImpl)lcm.getRegistryService();
            ConnectionImpl connection = (ConnectionImpl)rs.getConnection();

            if (!certificateExists(userModel.getAlias(), 
                userModel.getStorePassword())) {
            
                UserManager.generateSelfSignedCertificate(userModel.getUser(),
                                                          userModel.getAlias(),
                                                        userModel.getKeyPassword(),
                                                        userModel.getStorePassword());
            }

            // Force re-authentication in case credentials are already set
            connection.authenticate();

            RegistryBrowser.setWaitCursor();

            // Now save the User
            ArrayList objects = new ArrayList();
            objects.add(userModel.getUser());
            client.saveObjects(objects);

            // saveObjects uses XML-Security which overwrites the log4j
            // configuration and we never get to see this:
            log.info("Saved user \""
                + userModel.getUser().getPersonName().getFullName()
                + "\" on server.");
        } 
        catch (Exception e) {

            // Remove the self-signed certificate from the keystore, if one
            // was created during the self-registration process
            try {
                if (userModel != null) {
                    String alias = userModel.getAlias();
                    if (alias != null) {
                        UserManager.removeCertificate(alias, userModel.getStorePassword());
                    }
                }
            }
            catch (Exception removeCertException) {
                log.warn("Failed to remove the certificate from the keystore " +
                "that was generated during the self-registration process.", 
                removeCertException);
            }

            throw e;
        }
        finally {
            RegistryBrowser.setDefaultCursor();
        }
    }
    
    /*
     * Return true if certificate exists in client keystore, false if not.
     *
     */
    public static boolean certificateExists(String alias,
                                             char[] storePass)
        throws Exception 
    {
        boolean exists = false;

        try {
            File keystoreFile = KeystoreUtil.getKeystoreFile();
            KeystoreUtil.createKeystoreDirectory(keystoreFile);

            String[] args =
            {
                "-list", "-alias", alias,
                "-keystore", keystoreFile.getAbsolutePath(), "-storepass",
                new String(storePass)
            };

            KeyTool keytool = new KeyTool();
            keytool.run(args, System.out);
            
            exists = true;

            log.info("Alias exists \"" + alias + "\" in keystore \""
                + keystoreFile.getAbsolutePath() + "\"");
        }
        catch (Exception e) {
            //Cert does not exists.
        }
        return exists;
    }
    
    /*
     * Generate a self signed certificate
     *
     */
    public static void generateSelfSignedCertificate(User user, 
                                                     String alias,
                                                     char[] keyPass,
                                                     char[] storePass)
        throws Exception 
    {
        String dname      = getDNameFromUser(user);
        File keystoreFile = KeystoreUtil.getKeystoreFile();
        KeystoreUtil.createKeystoreDirectory(keystoreFile);

        String[] args =
        {
            "-genkey", "-alias", alias, "-keypass", new String(keyPass),
            "-keystore", keystoreFile.getAbsolutePath(), "-storepass",
            new String(storePass), "-dname", dname
        };

        KeyTool keytool = new KeyTool();
        keytool.run(args, System.out);

        log.info("Stored user \"" + alias + "\" in keystore \""
            + keystoreFile.getAbsolutePath() + "\"");
    }

    /** Remove an alias from the keystore.
      * <p>
      * Currently, this is only used to "backout" a generated key when self
      * registration fails.
      */
    public static void removeCertificate(String alias, char[] storePass) 
        throws Exception
    {
        File keystoreFile = KeystoreUtil.getKeystoreFile();
        String[] args = {
            "-delete", "-alias", alias, "-keystore", keystoreFile.getAbsolutePath(), 
            "-storepass", new String(storePass)
        };
        KeyTool keytool = new KeyTool();
        keytool.run(args, System.out);
        log.info("Removed user \"" + alias + "\" from keystore \""
            + keystoreFile.getAbsolutePath() + "\"");
    }

    /**
     * DOCUMENT ME!
     *
     * @param user DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    private static String getDNameFromUser(User user)
      throws JAXRException {

        String dname = "CN=";

        JAXRClient client            =
            RegistryBrowser.getInstance().getClient();
        BusinessLifeCycleManager lcm =
            client.getBusinessLifeCycleManager();

        Collection addresses  = user.getPostalAddresses();
        PostalAddress address;
        PersonName personName = user.getPersonName();

        //CN=Farrukh Najmi, OU=freebxml.org, O=ebxmlrr, L=Islamabad, ST=Punjab, C=PK
        if (personName == null) {
            personName =
                lcm.createPersonName("firstName", "middleName",
                                     "lastName");
        }

        if ((addresses != null) && (addresses.size() > 0)) {
            address = (PostalAddress)(addresses.iterator().next());
        } else {
            address =
                lcm.createPostalAddress("number", "street", "city",
                                        "state", "country",
                                        "postalCode", "Office");
        }

        String city = address.getCity();

        if ((city == null) || (city.length() == 0)) {
            city = "Unknown";
        }

        String state = address.getStateOrProvince();

        if ((state == null) || (state.length() == 0)) {
            state = "Unknown";
        }

        String country = address.getCountry();

        if ((country == null) || (country.length() == 0)) {
            country = "US";
        }

        if (country.length() > 0) {
            country = country.substring(0, 2);
        }

        dname += (personName.getFirstName() + " "
        + personName.getMiddleName() + " " + personName.getLastName()
        + ", OU=Unknown, O=Unknown, L=" + city + ", ST=" + state
        + ", C=" + country);

        return dname;
    }
}
