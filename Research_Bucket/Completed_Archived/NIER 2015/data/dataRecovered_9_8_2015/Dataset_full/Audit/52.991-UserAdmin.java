/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 */

package com.sun.xml.registry.client.admin;

import com.sun.xml.registry.ebxml.util.ProviderProperties;
import com.sun.xml.registry.client.util.RegistryObjectUtil;
import com.sun.xml.registry.ebxml.util.SecurityUtil;
import com.sun.xml.registry.ebxml.util.KeystoreUtil;
import com.sun.xml.registry.ebxml.util.ProviderProperties;

import java.util.ArrayList;
import java.util.HashSet;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.RegistryPackage;
import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.User;


/*
 * Program to administer users for ebxmlrr server.  Note: this program is
 * specific to a ebXML JAXR provider and to the ebxmlrr implementation of a
 * registry as it depends upon implementation specific aspects of user
 * registration.  See http://ebxmlrr.sourceforge.net for more info.
 *
 * $Header: /cvsroot/sino/jaxr/src/com/sun/xml/registry/client/admin/UserAdmin.java,v 1.8 2003/07/18 02:16:16 farrukh_najmi Exp $
 *
 * @author Edwin Goei
 *
 */
public class UserAdmin {

    /** DOCUMENT ME! */
    private static final SecurityUtil su = SecurityUtil.getInstance();

    /** DOCUMENT ME! */
    BusinessQueryManager     bqm;

    /** DOCUMENT ME! */
    BusinessLifeCycleManager lcm;

    /** DOCUMENT ME! */
    DeclarativeQueryManager dqm;

    /**
     * DOCUMENT ME!
     *
     * @param serverUrl DOCUMENT ME!
     * @param alias DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public void init(String serverUrl, String alias)
      throws JAXRException {

        ConnectionFactory connFactory = ConnectionFactory.newInstance();

        ProviderProperties.getInstance().put("javax.xml.registry.queryManagerURL",
                                             serverUrl);

        Connection connection = connFactory.createConnection();

        HashSet    creds = new HashSet();
        creds.add(su.aliasToX500PrivateCredential(alias));
        connection.setCredentials(creds);

        RegistryService service = connection.getRegistryService();
        bqm     = service.getBusinessQueryManager();
        lcm     = service.getBusinessLifeCycleManager();
        dqm     = service.getDeclarativeQueryManager();
    }

    /**
     * DOCUMENT ME!
     *
     * @return null if User is not registered, else User object
     *         corresponding to Connection credentials
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public User getUser() throws JAXRException {

        RegistryPackage pkg1 =
            lcm.createRegistryPackage("Dummy Package 1");
        ArrayList       al = new ArrayList();
        al.add(pkg1);

        BulkResponse br = lcm.saveObjects(al);

        if (br.getExceptions() != null) {

            // Assume credentials are unknown to server so therefore User
            // is not registered
            return null;
        }

        User user = RegistryObjectUtil.getOwner(pkg1);

        // Now clean up by removing dummy objects
        al.clear();
        al.add(pkg1.getKey());
        br = lcm.deleteObjects(al);

        if (br.getStatus() != BulkResponse.STATUS_SUCCESS) {

            // Not able to clean up
            System.err.println("Warning: unable clean up dummy objects created on server");
        }

        if (user == null) {

            // Unable to get AuditTrail for some reason
            throw new JAXRException("Unable to obtain User object corresponding to credentials");
        }

        return user;
    }

    /**
     * DOCUMENT ME!
     *
     * @param user DOCUMENT ME!
     * @param personName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    private User populateUser(User user, String personName)
      throws JAXRException {

        // Set the name of the User object itself (so it shows up in the
        // current browser)
        InternationalString isName =
            lcm.createInternationalString(personName);
        user.setName(isName);

        user.setPersonName(lcm.createPersonName(personName));

        ArrayList al = new ArrayList();
        al.add(lcm.createEmailAddress("jaxr-discussion@yahoogroups.com"));
        user.setEmailAddresses(al);

        al.clear();
        al.add(lcm.createPostalAddress("1234", "Test Blvd",
                                       "Santa Clara", "CA", "USA",
                                       "95054", null));
        user.setPostalAddresses(al);

        TelephoneNumber phone = lcm.createTelephoneNumber();
        phone.setAreaCode("408");
        phone.setNumber("555-1212");
        al.clear();
        al.add(phone);
        user.setTelephoneNumbers(al);

        return user;
    }

    /**
     * Register a new user
     *
     * @param personName DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public void registerUser(String personName) throws JAXRException {

        if (getUser() != null) {
            System.out.println("Error: User is already registered,"
                               + " use -update command instead");

            return;
        }

        User newUser = lcm.createUser();
        populateUser(newUser, personName);

        ArrayList al = new ArrayList();
        al.add(newUser);

        BulkResponse br = lcm.saveObjects(al);
        RegistryObjectUtil.checkBulkResponse(br);
    }

    /**
     * Update an existing User
     *
     * @param personName DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    public void updateUser(String personName) throws JAXRException {

        User user = getUser();

        if (user == null) {
            System.out.println("Error: User is not registered");

            return;
        }

        populateUser(user, personName);

        ArrayList al = new ArrayList();
        al.add(user);

        BulkResponse br = lcm.saveObjects(al);
        RegistryObjectUtil.checkBulkResponse(br);
    }

    /**
     */
    public void removeUser() throws JAXRException {

        User user = getUser();

        if (user == null) {
            System.out.println("Error: User is not registered");

            return;
        }

        ArrayList al = new ArrayList();
        al.add(user.getKey());

        BulkResponse br = lcm.deleteObjects(al);
        RegistryObjectUtil.checkBulkResponse(br);
    }

    /**
     * DOCUMENT ME!
     */
    private static void usage() {
        System.err.println("Usage: UserAdmin -<command> [parameters]");
        System.err.println("    -getUser                     Query the server for a User");
        System.err.println("    -register personName=<>      Register a new User");
        System.err.println("    -update personName=<>        Update an existing User");
        System.err.println("    -remove                      Remove an existing User");
        System.err.println("    alias=<keystore alias> used to link to credentials");
        System.err.println("    personName=<person name>");
        System.err.println("    url=<server url>");
        System.exit(-1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static void main(String[] args) throws Exception {

        String alias =
            ProviderProperties.getInstance().getProperty("UserAdmin.default.keystore.alias");
        String serverUrl  =
            ProviderProperties.getInstance().getProperty("UserAdmin.server.url");
        String personName = null;
        String command    = null;

        for (int i = 0; i < args.length; i++) {

            if (args[i].equalsIgnoreCase("-help")) {
                usage();
            } else if (args[i].equalsIgnoreCase("-usage")) {
                usage();
            } else if (args[i].equalsIgnoreCase("-getUser")) {
                command = "getUser";
            } else if (args[i].equalsIgnoreCase("-register")) {
                command = "register";
            } else if (args[i].equalsIgnoreCase("-update")) {
                command = "update";
            } else if (args[i].equalsIgnoreCase("-remove")) {
                command = "remove";
            } else if (args[i].startsWith("alias=")) {
                alias = args[i].substring(6);
            } else if (args[i].startsWith("personName=")) {
                personName = args[i].substring(11);
            } else if (args[i].startsWith("url=")) {
                serverUrl = args[i].substring(4);
            } else {
                usage();
            }
        }

        if (alias == null) {
            throw new Exception("KeyStore alias is required");
        } else {

            if (!su.getKeyStore().containsAlias(alias)) {
                System.err.println("Unknown alias in KeyStore: alias="
                                   + alias + ", KeyStore file="
                                   + KeystoreUtil.getKeystoreFile());
                usage();
            }
        }

        if (command == null) {
            command = "getUser";
        }

        UserAdmin userAdmin = new UserAdmin();
        userAdmin.init(serverUrl, alias);

        System.out.println("Executing command: -" + command);

        if ("getUser".equals(command)) {
            System.out.println("Getting User linked to credentials with alias="
                               + alias + "...");

            User user = userAdmin.getUser();
            System.out.println("userId="
                               + RegistryObjectUtil.toId(user));
        } else if ("register".equals(command)) {

            if (personName == null) {
                usage();
            }

            System.out.println("Registering new User linked to credentials with alias="
                               + alias + " using personName="
                               + personName + "...");
            userAdmin.registerUser(personName);
        } else if ("update".equals(command)) {

            if (personName == null) {
                usage();
            }

            System.out.println("Updating User linked to credentials with alias="
                               + alias + " to personName=" + personName
                               + "...");
            userAdmin.updateUser(personName);
        } else if ("remove".equals(command)) {
            System.out.println("Removing User linked to credentials with alias="
                               + alias + "...");

            // ??eeg finish this
            userAdmin.removeUser();
        }
    }
}
