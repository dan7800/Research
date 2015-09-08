/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: User.java,v 1.27 2009/04/22 06:29:19 bastafidli Exp $
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License. 
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
 */

package org.opensubsystems.security.data;

import java.security.Principal;
import java.sql.Timestamp;

import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.data.impl.ModifiableDataObjectImpl;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.CryptoUtils;
import org.opensubsystems.core.util.GlobalConstants;

/**
 * User of the system including some basic information. 
 *
 * @version $Id: User.java,v 1.27 2009/04/22 06:29:19 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public class User extends    ModifiableDataObjectImpl
                  implements Principal
{
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = -7281113227350531837L;

   /**
    * First name of the user.
    */
   protected String m_strFirstName;

   /**
    * Last name of the user.
    */
   protected String m_strLastName;

   /**
    * Phone number of the user.
    */
   protected String m_strPhone;

   /**
    * Fax number of the user.
    */
   protected String m_strFax;

   /**
    * Postal address of the user.
    */
   protected String m_strAddress;

   /**
    * Email address of the user.
    */
   protected String m_strEmail;

   /**
    * Unique login name of the user.
    */
   protected String m_strLoginName;

   /**
    * Password of the user.
    */
   protected String m_strPassword;

   /**
    * Authentication flag checking if the user can log into the system.
    */
   protected boolean m_bLoginEnabled;

   /**
    * Authentication flag checking if the user can enter the system without 
    * entering password.
    */
   protected boolean m_bGuestAccessEnabled;

   /**
    * Authorization flag checking if the user is superuser, it means that it can 
    * perform any action in the system regardless of the authorization provided 
    * by roles.
    */
   protected boolean m_bSuperUser;

   /**
    * Authorization flag checking if the user is internal or external user. Internal
    * user is usually employee of company or business who owns/runs this system.
    * External user can login to the system but doesn't work with system as part
    * of his/her duties.
    */
   protected boolean m_bInternalUser;

   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Empty user initialized to default parameters
    * @throws OSSException - error has occurred during password encryption 
    */
   public User(
   ) throws OSSException
   {
      this(DataObject.NEW_ID, DataObject.NEW_ID, "", "", "", "", "", "", "", "",
           true, false, false, false, null, null, true);
   }

   /**
    * Empty user for a specified domain initialized to default parameters
    *
    * @param iDomainId - Id of the domain this user belongs to
    * @throws OSSException - error has occurred during password encryption 
    */
   public User(
      int iDomainId
   ) throws OSSException
   {
      this(DataObject.NEW_ID, iDomainId, "", "", "", "", "", "", "", "",
           true, false, false, false, null, null, true);
   }

   /**
    * Create user from a given parameters.
    *
    * @param iId - Unique ID identifying this user
    * @param iDomainId - ID of the domain this user belongs to.
    * @param strFirstName - First name of the user.
    * @param strLastName - Last name of the user.
    * @param strPhone - Phone number of the user.
    * @param strFax - Fax number of the user.
    * @param strAddress - Postal address of the user.
    * @param strEmail - E-mail address of the user.
    * @param strLoginName - Login name of the user.
    * @param strPassword - Password of the user.
    * @param bLoginEnabled - Flag checking if the user is enable to log into the system.
    * @param bGuestAccessEnabled - if true then user can login without password
    * @param bSuperUser - Flag checking if the user is superuser.
    * @param bInternalUser - Flag checking if the user is internal or external user.
    * @param creationTimestamp - Timestamp when the user was created
    * @param modificationTimestamp - Timestamp when the user was last time modified
    * @param bEncryptPassword - encrypt password. The password will be encrypted
    *                           only if it is not empty, since empty password
    *                           is used to signal no password (or no password change)
    * @throws OSSException - error has occurred during password encryption 
    */
   public User(
      int       iId,
      int       iDomainId,
      String    strFirstName,
      String    strLastName,
      String    strPhone,
      String    strFax,
      String    strAddress,
      String    strEmail,
      String    strLoginName,
      String    strPassword,
      boolean   bLoginEnabled,
      boolean   bGuestAccessEnabled,
      boolean   bSuperUser,
      boolean   bInternalUser,
      Timestamp creationTimestamp,
      Timestamp modificationTimestamp,
      boolean   bEncryptPassword
   ) throws OSSException
   {
      super(iId, UserDataDescriptor.class, iDomainId, creationTimestamp, 
            modificationTimestamp);

      if (GlobalConstants.ERROR_CHECKING)
      {
         assert strPassword != null : "Password cannot be null";
      }

      m_strFirstName          = strFirstName;
      m_strLastName           = strLastName;
      m_strPhone              = (strPhone == null) ? "" : strPhone;
      m_strFax                = (strFax == null) ? "" : strFax;
      m_strAddress            = (strAddress == null) ? "" : strAddress;
      m_strEmail              = strEmail;
      m_strLoginName          = strLoginName; 
      m_strPassword = (strPassword == null) ? "" : ((bEncryptPassword && (strPassword.length() > 0))
                      ? encryptPassword(strPassword) : strPassword);
      m_bLoginEnabled         = bLoginEnabled;
      m_bGuestAccessEnabled   = bGuestAccessEnabled;
      m_bSuperUser            = bSuperUser;
      m_bInternalUser         = bInternalUser;
   }

   /**
    * Copy constructor
    *
    * @param iDomainId - Id of the domain this user belongs to
    * @param user - user object the attributes will be used from
    * 
    * @throws OSSException - error has occurred during password encryption 
    */
   public User(
      int iDomainId,
      User user
   ) throws OSSException
   {
      this(user.getId(), iDomainId, user.getFirstName(), user.getLastName(),
           user.getPhone(), user.getFax(), user.getAddress(), user.getEmail(),
           user.getLoginName(), user.getPassword(), user.isLoginEnabled(),
           user.isGuestAccessEnabled(), user.isSuperUser(), user.isInternalUser(),
           user.getCreationTimestamp(), user.getModificationTimestamp(),
           // don't allow encrypting password since it is already encrypted
           // within the input user parameter
           false);
   }
   
   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * This method is defined here so that User class contains all information 
    * about password encryption.
    * 
    * @param  strPassword - password to encrypt
    * @return String - encrypted password
    * @throws OSSException - an error has occurred
    */
   public static String encryptPassword(
      String strPassword
   ) throws OSSException
   {
      String strReturn;
      
      strReturn = CryptoUtils.getMessageDigest(strPassword);
      
      return strReturn;
   }

   /**
    * @return boolean
    */
   public boolean isInternalUser(
   )
   {
      return m_bInternalUser;
   }

   /**
    * @param internalUser - new internalUser flag
    */
   public void setInternalUser(
      boolean internalUser
   )
   {
      m_bInternalUser = internalUser;
   }

   /**
    * @return boolean
    */
   public boolean isLoginEnabled(
   )
   {
      return m_bLoginEnabled;
   }

   /**
    * @param loginEnabled - new loginEnabled flag
    */
   public void setLoginEnabled(
      boolean loginEnabled
   )
   {
      m_bLoginEnabled = loginEnabled;
   }

   /**
    * @return boolean
    */
   public boolean isSuperUser(
   )
   {
      return m_bSuperUser;
   }

   /**
    * @param superUser - new superUser flag
    */
   public void setSuperUser(
      boolean superUser
   )
   {
      m_bSuperUser = superUser;
   }

   /**
    * @return String
    */
   public String getAddress(
   )
   {
      return m_strAddress;
   }

   /**
    * @param address - new addresss
    */
   public void setAddress(
      String address
   )
   {
      m_strAddress = (address == null) ? "" : address;
   }

   /**
    * @return String
    */
   public String getEmail(
   )
   {
      return m_strEmail;
   }

   /**
    * @param email - new email
    */
   public void setEmail(
      String email
   )
   {
      m_strEmail = email;
   }

   /**
    * @return String
    */
   public String getFax(
   )
   {
      return m_strFax;
   }

   /**
    * @param fax - new fax
    */
   public void setFax(
      String fax
   )
   {
      m_strFax = (fax == null) ? "" : fax;
   }

   /**
    * @return String
    */
   public String getFirstName(
   )
   {
      return m_strFirstName;
   }

   /**
    * @param strFirstName - new first name
    */
   public void setFirstName(
      String strFirstName
   )
   {
      m_strFirstName = strFirstName;
   }

   /**
    * @return String
    */
   public String getLastName(
   )
   {
      return m_strLastName;
   }

   /**
    * @param strLastName - new last name
    */
   public void setLastName(
      String strLastName
   )
   {
      m_strLastName = strLastName;
   }

   /**
    * @return String
    */
   public String getLoginName(
   )
   {
      return m_strLoginName;
   }

   /**
    * @param loginName - new login name
    */
   public void setLoginName(
      String loginName
   )
   {
      m_strLoginName = loginName;
   }

   /**
    * @return String
    */
   public String getPassword(
   )
   {
      return m_strPassword;
   }
   
   /**
    * @param password - new not encrypted password
    * @throws OSSException - error during encrypting password 
    */
   public void setPassword(
      String password
   ) throws OSSException
   {
      m_strPassword = (password == null) ? "" : encryptPassword(password);
   }

   /**
    * @return String
    */
   public String getPhone(
   )
   {
      return m_strPhone;
   }

   /**
    * @param phone - new phone
    */
   public void setPhone(
      String phone
   )
   {
      m_strPhone = (phone == null) ? "" : phone;
   }

   /**
    * Check authentification flag checking if the user can enter the system 
    * without entering password.
    * 
    * @return boolean - true if this user can login to the system without password
    */
   public boolean isGuestAccessEnabled()
   {
      return m_bGuestAccessEnabled;
   }

   /**
    * Set authentification flag checking if the user can enter the system 
    * without entering password.
    * 
    * @param bGuestAccessEnabled - true if this user can login to the system 
    *                              without password false otherwise
    */
   public void setGuestAccessEnabled(
      boolean bGuestAccessEnabled
   )
   {
      m_bGuestAccessEnabled = bGuestAccessEnabled;
   }
   
   /**
    * {@inheritDoc}
    */
   public String getName(
   )
   {
      // Return unique name
      return getLoginName();
   }

   /**
    * {@inheritDoc}
    */
   public boolean isSame(
      Object oObject
   )
   {
      boolean bReturn = false;
      User uHelp;

      if (oObject == this)
      {
         bReturn = true;
      }
      else
      {
         if (oObject != null && oObject instanceof User)
         {
            // cast Object to User
            uHelp = (User) oObject;
            // check all data attributes for equals
            bReturn = (((m_strFirstName == null) && (uHelp.m_strFirstName == null))
                         || ((m_strFirstName != null) 
                            && (m_strFirstName.equals(uHelp.m_strFirstName))))
                      && (((m_strLastName == null) && (uHelp.m_strLastName == null))
                         || ((m_strLastName != null) 
                            && (m_strLastName.equals(uHelp.m_strLastName))))
                      && (((m_strLoginName == null) && (uHelp.m_strLoginName == null))
                         || ((m_strLoginName != null) 
                            && (m_strLoginName.equals(uHelp.m_strLoginName))))
                      && (((m_strPassword == null) && (uHelp.m_strPassword == null))
                         || ((m_strPassword != null) 
                            && (m_strPassword.equals(uHelp.m_strPassword))))
                      && (((m_strAddress == null) && (uHelp.m_strAddress == null))
                         || ((m_strAddress != null) 
                            && (m_strAddress.equals(uHelp.m_strAddress))))
                      && (((m_strEmail == null) && (uHelp.m_strEmail == null))
                         || ((m_strEmail != null) 
                            && (m_strEmail.equals(uHelp.m_strEmail))))
                      && (((m_strFax == null) && (uHelp.m_strFax == null))
                         || ((m_strFax != null) 
                            && (m_strFax.equals(uHelp.m_strFax))))
                      && (((m_strPhone == null) && (uHelp.m_strPhone == null))
                         || ((m_strPhone != null) 
                            && (m_strPhone.equals(uHelp.m_strPhone))))
                      && uHelp.isLoginEnabled() == m_bLoginEnabled
                      && uHelp.isGuestAccessEnabled() == m_bGuestAccessEnabled
                      && uHelp.isInternalUser() == m_bInternalUser
                      && uHelp.isSuperUser() == m_bSuperUser;
         }
      }
      return bReturn;
   }
   
   /**
    * Get the full name of the user starting with the last name first.
    * The formatting is locale dependent, e.g. in US it is 
    * "Last Name, First Name".
    *
    * @return String - full name of the user starting with the last name
    */
   public String getFullNameLastFirst(
   )
   {
      return getFullNameLastFirst(m_strFirstName, m_strLastName);
   }
   
   /**
    * Get the full name of the user starting with the last name first.
    * The formatting is locale dependent, e.g. in US it is 
    * "Last Name, First Name".
    *
    * @param strFirstName - first name of the user
    * @param strLastName - last name of the user
    * @return String - full name of the user starting with the last name
    */
   public static String getFullNameLastFirst(
      String strFirstName,
      String strLastName
   )
   {
      StringBuffer sbBuffer = new StringBuffer();
      
      if ((strLastName != null) && (strLastName.length() > 0))
      {   
         sbBuffer.append(strLastName);
         if ((strFirstName != null) && (strFirstName.length() > 0))
         {   
            sbBuffer.append(", ");
         }
      }
      if ((strFirstName != null) && (strFirstName.length() > 0))
      {
         sbBuffer.append(strFirstName);
      }
      
      return sbBuffer.toString();
   }   

   /**
    * Get the full name of the user starting with the first name.
    *
    * @return String - full name of the user starting with the first name
    */
   public String getFullNameFirstFirst(
   )
   {
      return getFullNameFirstFirst(m_strFirstName, m_strLastName);
   }
   
   /**
    * Get the full name of the user starting with the first name.
    *
    * @param strFirstName - first name of the user
    * @param strLastName - last name of the user
    * @return String - full name of the user starting with the first name
    */
   public static String getFullNameFirstFirst(
      String strFirstName,
      String strLastName
   )
   {
      StringBuffer sbBuffer = new StringBuffer();
      
      if ((strFirstName != null) && (strFirstName.length() > 0))
      {
         sbBuffer.append(strFirstName);
         if ((strLastName != null) && (strLastName.length() > 0))
         {   
            sbBuffer.append(" ");
         }
      }
      if ((strLastName != null) && (strLastName.length() > 0))
      {   
         sbBuffer.append(strLastName);
      }
      
      return sbBuffer.toString();
   }   
}
/* Copyright (c) 1995-2000, The Hypersonic SQL Group.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the Hypersonic SQL Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HYPERSONIC SQL GROUP,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many individuals 
 * on behalf of the Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2005, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb;

import org.hsqldb.HsqlNameManager.HsqlName;
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.IntValueHashMap;

// fredt@users 20021103 - patch 1.7.2 - fix bug in revokeAll()
// fredt@users 20021103 - patch 1.7.2 - allow for drop table, etc.
// when tables are dropped or renamed, changes are reflected in the
// permissions held in User objects.
// boucherb@users 200208-200212 - doc 1.7.2 - update
// boucherb@users 200208-200212 - patch 1.7.2 - metadata
// unsaved@users - patch 1.8.0 moved right managament to new classes

/**
 * A User Object holds the name, password for a
 * particular database user.<p>
 *
 * Enhanced in successive versions of HSQLDB.
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version 1.8.0
 * @since Hypersonic SQL
 */
public class User {

    /** true if this user is the sys user. */
    private boolean isSys;

    /** true if this user is the public user. */
    private boolean isPublic;

    /** user name. */
    private String sName;

    /** password. */
    private String sPassword;

    /** grantee object. */
    private Grantee grantee;

    /**
     * Constructor
     */
    User(String name, String password,
            Grantee inGrantee) throws HsqlException {

        sName   = name;
        grantee = inGrantee;

        boolean granteeOk = grantee != null
                            || GranteeManager.isReserved(name);

        if (!granteeOk) {
            Trace.doAssert(false,
                           Trace.getMessage(Trace.MISSING_GRANTEE) + ": "
                           + name);
        }

        setPassword(password);

        isSys    = name.equals(GranteeManager.SYSTEM_AUTHORIZATION_NAME);
        isPublic = name.equals(GranteeManager.PUBLIC_USER_NAME);
    }

    String getName() {
        return sName;
    }

    void setPassword(String password) throws HsqlException {

        // TODO:
        // checkComplexity(password);
        // requires: UserManager.createSAUser(), UserManager.createPublicUser()
        sPassword = password;
    }

    /**
     * Checks if this object's password attibute equals
     * specified argument, else throws.
     */
    void checkPassword(String test) throws HsqlException {
        Trace.check(test.equals(sPassword), Trace.ACCESS_IS_DENIED);
    }

    /**
     * Returns true if this User object is for a user with the
     * database administrator role.
     */
    boolean isSys() {
        return isSys;
    }

    /**
     * Returns true if this User object represents the PUBLIC user
     */
    boolean isPublic() {
        return isPublic;
    }

    /**
     * Returns the ALTER USER DDL character sequence that preserves the
     * this user's current password value and mode. <p>
     *
     * @return  the DDL
     */
    String getAlterUserDDL() {

        StringBuffer sb = new StringBuffer();

        sb.append(Token.T_ALTER).append(' ');
        sb.append(Token.T_USER).append(' ');
        sb.append(sName).append(' ');
        sb.append(Token.T_SET).append(' ');
        sb.append(Token.T_PASSWORD).append(' ');
        sb.append('"').append(sPassword).append('"');

        return sb.toString();
    }

    /**
     * returns the DDL string
     * sequence that creates this user.
     *
     */
    String getCreateUserDDL() {

        StringBuffer sb = new StringBuffer(64);

        sb.append(Token.T_CREATE).append(' ');
        sb.append(Token.T_USER).append(' ');
        sb.append(sName).append(' ');
        sb.append(Token.T_PASSWORD).append(' ');
        sb.append('"').append(sPassword).append('"');

        return sb.toString();
    }

    /**
     * Retrieves the redo log character sequence for connecting
     * this user
     *
     * @return the redo log character sequence for connecting
     *      this user
     */
    public String getConnectStatement() {

        StringBuffer sb = new StringBuffer();

        sb.append(Token.T_CONNECT).append(' ');
        sb.append(Token.T_USER).append(' ');
        sb.append(sName);

        return sb.toString();
    }

    /**
     * Retrieves the Grantee object for this User.
     */
    Grantee getGrantee() {
        return grantee;
    }

    /**
     * Sets the Grantee object for this User.
     * This is done in the constructor for all users except the special
     * users SYSTEM and PUBLIC, which have to be set up before the
     * Managers are initialized.
     */
    void setGrantee(Grantee inGrantee) throws HsqlException {

        if (grantee != null) {
            Trace.doAssert(false,
                           Trace.getMessage(Trace.CHANGE_GRANTEE) + ": "
                           + sName);
        }

        grantee = inGrantee;
    }

    // Legacy wrappers

    /**
     * Returns true if this User object is for a user with the
     * database administrator role.
     */
    boolean isAdmin() {
        return grantee.isAdmin();
    }

    /**
     * Retrieves a string[] whose elements are the names, of the rights
     * explicitly granted with the GRANT command to this <code>User</code>
     * object on the <code>Table</code> object identified by the
     * <code>name</code> argument.
     * * @return array of Strings naming the rights granted to this
     *        <code>User</code> object on the <code>Table</code> object
     *        identified by the <code>name</code> argument.
     * @param name a <code>Table</code> object identifier
     *
     */
    String[] listGrantedTablePrivileges(HsqlName name) {
        return grantee.listGrantedTablePrivileges(name);
    }

    /**
     * Retrieves the distinct set of Java <code>Class</code> FQNs
     * for which this <code>User</code> object has been
     * granted <code>ALL</code> (the Class execution privilege). <p>
     * @param andToPublic if <code>true</code>, then the set includes the
     *        names of classes accessible to this <code>User</code> object
     *        through grants to its <code>PUBLIC</code> <code>User</code>
     *        object attribute, else only direct grants are inlcuded.
     * @return the distinct set of Java Class FQNs for which this
     *        this <code>User</code> object has been granted
     *        <code>ALL</code>.
     *
     */
    HashSet getGrantedClassNames(boolean andToPublic) throws HsqlException {
        return grantee.getGrantedClassNames(andToPublic);
    }

    /**
     * Retrieves the map object that represents the rights that have been
     * granted on database objects.  <p>
     *
     * The map has keys and values with the following interpretation: <P>
     *
     * <UL>
     * <LI> The keys are generally (but not limited to) objects having
     *      an attribute or value equal to the name of an actual database
     *      object.
     *
     * <LI> Specifically, the keys act as database object identifiers.
     *
     * <LI> The values are always Integer objects, each formed by combining
     *      a set of flags, one for each of the access rights defined in
     *      UserManager: {SELECT, INSERT, UPDATE and DELETE}.
     * </UL>
     */
    IntValueHashMap getRights() {
        return grantee.getRights();
    }

    /**
     * Checks that this User object is for a user with the
     * database administrator role. Otherwise it throws.
     */
    void checkAdmin() throws HsqlException {
        grantee.checkAdmin();
    }

    /**
     * Checks if any of the rights represented by the rights
     * argument have been granted on the specified database object. <p>
     *
     * This is done by checking that a mapping exists in the rights map
     * from the dbobject argument for at least one of the rights
     * contained in the rights argument. Otherwise, it throws.
     */
    void check(Object dbobject, int rights) throws HsqlException {
        grantee.check(dbobject, rights);
    }

    /**
     * Returns true if any of the rights represented by the
     * rights argument has been granted on the database object identified
     * by the dbobject argument. <p>
     *
     * This is done by checking that a mapping exists in the rights map
     * from the dbobject argument for at least one of the rights
     * contained in the rights argument.
     */
    boolean isAccessible(Object dbobject, int rights) throws HsqlException {
        return grantee.isAccessible(dbobject, rights);
    }

    /**
     * Returns true if any right at all has been granted to this User object
     * on the database object identified by the dbobject argument.
     */
    boolean isAccessible(Object dbobject) throws HsqlException {
        return grantee.isAccessible(dbobject);
    }
}
