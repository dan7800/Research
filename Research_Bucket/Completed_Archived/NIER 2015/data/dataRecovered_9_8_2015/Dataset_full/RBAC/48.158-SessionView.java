/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: SessionView.java,v 1.14 2009/04/22 06:29:19 bastafidli Exp $
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

import java.sql.Timestamp;

import org.opensubsystems.core.data.DataObject;
import org.opensubsystems.core.data.impl.BasicDataObjectImpl;
import org.opensubsystems.core.error.OSSException;

/**
 * View representing logged in users. The view is combination of internal 
 * session and user objects. Since this is a combination of these two objects 
 * and cannot be modified on its own, we will use internal session data type to 
 * represent this object rather then define new data type. In this case it is 
 * critical to reuse the constants from the original objects especially for the
 * id attribute since the combination of the data type (internal session) and 
 * id (which then has to be internal session id) is being used for acccess 
 * rights checking.
 *
 * This object is immutable, meaning that after it is created in never changes.
 *
 * @version $Id: SessionView.java,v 1.14 2009/04/22 06:29:19 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
// TODO: Improve: Embed User object here instead of duplicating columns
public class SessionView extends BasicDataObjectImpl
{
   // Constants ////////////////////////////////////////////////////////////////
   
   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = -6521539124038008122L;

   /**
    * User ID this session belongs to.
    */
   protected int m_iUserId;

   /**
    * First name of the user.
    */
   protected String m_strFirstName;

   /**
    * Last name of the user.
    */
   protected String m_strLastName;

   /**
    * Unique login name of the user.
    */
   protected String m_strLoginName;

   /**
    * Phone number of the user.
    */
   protected String m_strPhone;

   /**
    * Fax number of the user.
    */
   protected String m_strFax;

   /**
    * Email address of the user.
    */
   protected String m_strEmail;

   /**
    * Authentication flag checking if the user is enable to log into the system.
    */
   protected boolean m_bLoginEnabled;

   /**
    * Authorization flag checking if the user is superuser, it means that has
    * assigned no role and owns access rights to provide all operations within
    * the system.
    */
   protected boolean m_bSuperUser;

   /**
    * Unique generated string identifying user's session.
    */
   protected String m_strInternalSession;

   /**
    * Client IP address.
    */
   protected String m_strClientIP;

   /**
    * Client browser type.
    */
   protected String m_strClientType;

   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Empty InternalSession view initialized to default parameters
    * 
    * @throws OSSException - an error has occurred
    */
   public SessionView(
   ) throws OSSException
   {
      this(DataObject.NEW_ID, DataObject.NEW_ID, DataObject.NEW_ID,
           "", "", "", "", "", "", false, false, "", "", "", null);
   }

   /**
    * Create InternalSession view from a given parameters.
    *
    * @param iId - Unique ID identifying user's InternalSession
    * @param iDomainId - ID of the domain user's InternalSession belongs to
    * @param iUserId - ID of the user his InternalSession belongs to
    * @param strFirstName - First name of the user.
    * @param strLastName - Last name of the user.
    * @param strLoginName - Login name of the user.
    * @param strPhone - Phone number of the user.
    * @param strFax - Fax number of the user.
    * @param strEmail - E-mail address of the user.
    * @param bLoginEnabled - Flag checking if the user is enable to log into the system.
    * @param bSuperUser - Flag checking if the user is superuser.
    * @param strInternalSession - generated user's session code
    * @param strClientIP - Client IP address of the logged user
    * @param strClientType - Client type of the logged user
    * @param creationTimestamp - Timestamp when the user was logged in
    * @throws OSSException - an error has occurred
    */
   public SessionView(
      int       iId,
      int       iDomainId,
      int       iUserId,
      String    strFirstName,
      String    strLastName,
      String    strLoginName,
      String    strPhone,
      String    strFax,
      String    strEmail,
      boolean   bLoginEnabled,
      boolean   bSuperUser,
      String    strInternalSession,
      String    strClientIP,
      String    strClientType,
      Timestamp creationTimestamp
   ) throws OSSException
   {
      super(iId, SessionViewDataDescriptor.class, iDomainId, creationTimestamp);

      m_iUserId               = iUserId;
      m_strFirstName          = strFirstName;
      m_strLastName           = strLastName;
      m_strLoginName          = strLoginName;
      m_strPhone              = strPhone;
      m_strFax                = strFax;
      m_strEmail              = strEmail;
      m_bLoginEnabled         = bLoginEnabled;
      m_bSuperUser            = bSuperUser;
      m_strInternalSession    = strInternalSession;
      m_strClientIP           = strClientIP;
      m_strClientType        = strClientType;
   }

   /**
    * @return int
    */
   public int getUserId()
   {
      return m_iUserId;
   }

   /**
    * @return String
    */
   public String getFirstName()
   {
      return m_strFirstName;
   }

   /**
    * @return String
    */
   public String getLastName()
   {
      return m_strLastName;
   }

   /**
    * @return String
    */
   public String getLoginName()
   {
      return m_strLoginName;
   }

   /**
    * @return String
    */
   public String getPhone()
   {
      return m_strPhone;
   }

   /**
    * @return String
    */
   public String getFax()
   {
      return m_strFax;
   }

   /**
    * @return String
    */
   public String getEmail()
   {
      return m_strEmail;
   }

   /**
    * @return boolean
    */
   public boolean isLoginEnabled()
   {
      return m_bLoginEnabled;
   }

   /**
    * @return boolean
    */
   public boolean isSuperUser()
   {
      return m_bSuperUser;
   }

   /**
    * @return String
    */
   public String getInternalSession()
   {
      return m_strInternalSession;
   }

   /**
    * @return String
    */
   public String getClientIP()
   {
      return m_strClientIP;
   }

   /**
    * @return String
    */
   public String getClientType()
   {
      return m_strClientType;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isSame(
      Object oObject
   )
   {
      boolean bReturn = false;
      SessionView ssHelp;

      if (oObject == this)
      {
         bReturn = true;
      }
      else
      {
         if (oObject != null && oObject instanceof SessionView)
         {
            // cast Object to SessionView
            ssHelp = (SessionView) oObject;
            // check all data attributes for equals
            bReturn = (((m_strInternalSession == null) && (ssHelp.m_strInternalSession == null))
                         || ((m_strInternalSession != null) 
                               && (m_strInternalSession.equals(ssHelp.m_strInternalSession))))
                      && (((m_strClientType == null) && (ssHelp.m_strClientType == null))
                         || ((m_strClientType != null) 
                                && (m_strClientType.equals(ssHelp.m_strClientType))))
                      && (((m_strClientIP == null) && (ssHelp.m_strClientIP == null))
                         || ((m_strClientIP != null) 
                                 && (m_strClientIP.equals(ssHelp.m_strClientIP))))
                      &&  (((m_strEmail == null) && (ssHelp.m_strEmail == null))
                         || ((m_strEmail != null) 
                                 && (m_strEmail.equals(ssHelp.m_strEmail)))) 
                      &&  (((m_strFax == null) && (ssHelp.m_strFax == null))
                         || ((m_strFax != null) 
                                 && (m_strFax.equals(ssHelp.m_strFax)))) 
                      &&  (((m_strFirstName == null) && (ssHelp.m_strFirstName == null))
                         || ((m_strFirstName != null) 
                                 && (m_strFirstName.equals(ssHelp.m_strFirstName)))) 
                      &&  (((m_strLastName == null) && (ssHelp.m_strLastName == null))
                         || ((m_strLastName != null) 
                                 && (m_strLastName.equals(ssHelp.m_strLastName)))) 
                      &&  (((m_strLoginName == null) && (ssHelp.m_strLoginName == null))
                         || ((m_strLoginName != null) 
                                 && (m_strLoginName.equals(ssHelp.m_strLoginName)))) 
                      &&  (((m_strPhone == null) && (ssHelp.m_strPhone == null))
                         || ((m_strPhone != null) 
                                 && (m_strPhone.equals(ssHelp.m_strPhone)))) 
                      && m_bLoginEnabled == ssHelp.m_bLoginEnabled
                      && m_bSuperUser == ssHelp.m_bSuperUser;
         }
      }
      return bReturn;
   }
}
