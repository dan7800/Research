/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.lenya.ac.ldap;

import java.io.File;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.lenya.ac.AccessControlException;
import org.apache.lenya.ac.User;
import org.apache.lenya.ac.UserType;
import org.apache.lenya.ac.file.FileAccreditableManager;
import org.apache.lenya.ac.file.FileGroup;
import org.apache.lenya.ac.file.FileRole;
import org.apache.lenya.ac.file.FileUserManager;
import org.apache.lenya.ac.impl.AbstractAccessControlTest;
import org.apache.lenya.ac.ldap.LDAPUser;
import org.apache.lenya.cms.publication.Publication;
import org.apache.lenya.cms.publication.PublicationException;

/**
 * LDAP user test. The name "Abstract..." is used to prevent it from being
 * executed.
 * 
 * @version $Id: AbstractLDAPUserTest.java 485769 2006-12-11 17:41:23Z andreas $
 */
public class AbstractLDAPUserTest extends AbstractAccessControlTest {

    /**
     * get a publication
     * 
     * @return a <code>Publication</code>
     * 
     * @throws PublicationException if an error occurs
     */
    final public Publication getPublication() throws PublicationException {
        return getPublication("test");
    }

    /**
     * Create and save an ldap user
     * 
     * @param userName name of the user
     * @param email of the user
     * @param ldapId ldap id of the user
     * @throws AccessControlException if the creating or the saving fails
     * @throws ConfigurationException if the creating or the saving fails
     */
    final public void createAndSaveUser(String userName, String email, String ldapId)
            throws AccessControlException, ConfigurationException {
        String editorGroupName = "editorGroup";
        String adminGroupName = "adminGroup";
        String editorRoleName = "editorRole";
        String adminRoleName = "adminRole";

        FileRole editorRole = new FileRole(getAccreditableManager().getRoleManager(), getLogger());
        editorRole.setName(editorRoleName);

        FileRole adminRole = new FileRole(getAccreditableManager().getRoleManager(), getLogger());
        adminRole.setName(adminRoleName);

        FileGroup editorGroup = new FileGroup(getAccreditableManager().getGroupManager(),
                getLogger(), editorGroupName);
        FileGroup adminGroup = new FileGroup(getAccreditableManager().getGroupManager(),
                getLogger(), adminGroupName);

        LDAPUser user = new LDAPUser(getAccreditableManager().getUserManager(), getLogger(),
                userName, email, ldapId, getLogger());

        editorRole.save();
        adminRole.save();

        /*
         * editorGroup.addRole(editorRole); user.addGroup(editorGroup);
         * adminGroup.addRole(editorRole); adminGroup.addRole(adminRole);
         */
        editorGroup.save();
        adminGroup.save();

        adminGroup.add(user);
        user.save();
    }

    /**
     * Test loading an LDAPUser
     * 
     * @param userName the name of the user
     * @return an <code>LDAPUser</code>
     * @throws AccessControlException of the loading fails
     */
    final public LDAPUser loadUser(String userName) throws AccessControlException {
        UserType[] userTypes = { FileAccreditableManager.getDefaultUserType() };
        FileUserManager _manager = FileUserManager.instance(getAccreditableManager(),
                getAccreditablesDirectory(), userTypes, getLogger());

        return (LDAPUser) _manager.getUser(userName);
    }

    /**
     * Test the ldap id getter
     * 
     * @throws AccessControlException if the test fails
     * @throws ConfigurationException if the creating or the saving fails
     */
    final public void testGetLdapId() throws ConfigurationException, AccessControlException {
        String userName = "felix";
        String ldapId = "m400032";
        createAndSaveUser(userName, "felix@wyona.com", ldapId);

        LDAPUser user = null;
        user = loadUser(userName);
        assertNotNull(user);
        assertEquals(ldapId, user.getLdapId());
    }

    /**
     * Test settinf the ldap id
     * 
     * @throws AccessControlException if the test fails
     * @throws ConfigurationException if the creating or the saving fails
     */
    final public void testSetLdapId() throws ConfigurationException, AccessControlException {
        String userName = "felix";
        String newLdapId = "foo";
        createAndSaveUser(userName, "felix@wyona.com", "bar");

        LDAPUser user = null;
        user = loadUser(userName);
        assertNotNull(user);
        user.setLdapId(newLdapId);
        user.save();
        user = null;
        user = loadUser(userName);
        assertNotNull(user);
        assertEquals(newLdapId, user.getLdapId());
    }

    /**
     * Test save
     * 
     * @throws AccessControlException if the test fails
     * @throws ConfigurationException if the creating or the saving fails
     */
    final public void testSave() throws ConfigurationException, AccessControlException {
        String userName = "felix";
        createAndSaveUser(userName, "felix@wyona.com", "m400032");

        User user = null;
        user = loadUser(userName);
        assertNotNull(user);
    }

    /**
     * Test the deletion of a ldap user
     * 
     */
    final public void testDelete() {
        // TODO Implement delete().
    }
}
