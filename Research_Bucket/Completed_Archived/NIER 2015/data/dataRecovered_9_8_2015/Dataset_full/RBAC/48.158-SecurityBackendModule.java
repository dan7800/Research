/*
 * Copyright (c) 2007 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: SecurityBackendModule.java,v 1.6 2009/07/18 04:53:22 bastafidli Exp $
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

package org.opensubsystems.security.application;

import org.opensubsystems.core.application.impl.BackendDatabaseModuleImpl;
import org.opensubsystems.core.data.DataDescriptor;
import org.opensubsystems.core.data.DataDescriptorManager;
import org.opensubsystems.core.error.OSSConfigException;
import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.logic.ControllerManager;
import org.opensubsystems.core.logic.StatelessController;
import org.opensubsystems.core.persist.DataFactory;
import org.opensubsystems.core.persist.DataFactoryManager;
import org.opensubsystems.core.persist.db.DatabaseSchema;
import org.opensubsystems.core.persist.db.DatabaseSchemaManager;
import org.opensubsystems.patterns.listdata.logic.ListController;
import org.opensubsystems.security.data.AccessRightDataDescriptor;
import org.opensubsystems.security.data.DomainDataDescriptor;
import org.opensubsystems.security.data.ExternalSessionDataDescriptor;
import org.opensubsystems.security.data.InternalSessionDataDescriptor;
import org.opensubsystems.security.data.RoleDataDescriptor;
import org.opensubsystems.security.data.SessionViewDataDescriptor;
import org.opensubsystems.security.data.UserDataDescriptor;
import org.opensubsystems.security.logic.AuthenticationController;
import org.opensubsystems.security.logic.AuthorizationController;
import org.opensubsystems.security.logic.DomainController;
import org.opensubsystems.security.logic.RoleController;
import org.opensubsystems.security.logic.SessionController;
import org.opensubsystems.security.logic.UserController;
import org.opensubsystems.security.logic.UserExtrasController;
import org.opensubsystems.security.patterns.listdata.logic.SecureListController;
import org.opensubsystems.security.persist.AccessRightFactory;
import org.opensubsystems.security.persist.DomainFactory;
import org.opensubsystems.security.persist.ExternalSessionFactory;
import org.opensubsystems.security.persist.InternalSessionFactory;
import org.opensubsystems.security.persist.RoleFactory;
import org.opensubsystems.security.persist.SessionViewFactory;
import org.opensubsystems.security.persist.UserFactory;
import org.opensubsystems.security.persist.db.DomainDatabaseSchema;
import org.opensubsystems.security.persist.db.RoleDatabaseSchema;
import org.opensubsystems.security.persist.db.SessionDatabaseSchema;
import org.opensubsystems.security.persist.db.SessionViewDatabaseSchema;
import org.opensubsystems.security.persist.db.UserDatabaseSchema;

/**
 * Class describing backend components of the security subsystem.
 * 
 * @version $Id: SecurityBackendModule.java,v 1.6 2009/07/18 04:53:22 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision
 */
public class SecurityBackendModule extends BackendDatabaseModuleImpl
{
   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Name identifying this module. 
    */
   public static final String SECURITY_MODULE_NAME = "Security Backend";

   /**
    * Version of this module. The version should change when new items are added.
    */
   public static final int SECURITY_MODULE_VERSION = 1;

   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Constructor
    */
   public SecurityBackendModule(
   )
   {
      super(SECURITY_MODULE_NAME, SECURITY_MODULE_VERSION, null,
            new String[][] 
               {{"org.opensubsystems.patterns.listdata.logic.ListController",
                 "org.opensubsystems.security.patterns.listdata.logic.impl.SecureListControllerImpl"},
               }
           );
   }

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   public void init(
   ) throws OSSException
   {
      initModule(
         new DataDescriptor[]
            {DataDescriptorManager.getInstance(DomainDataDescriptor.class),
             DataDescriptorManager.getInstance(RoleDataDescriptor.class),
             DataDescriptorManager.getInstance(AccessRightDataDescriptor.class),
             DataDescriptorManager.getInstance(UserDataDescriptor.class),
             DataDescriptorManager.getInstance(InternalSessionDataDescriptor.class),
             DataDescriptorManager.getInstance(ExternalSessionDataDescriptor.class),
             DataDescriptorManager.getInstance(SessionViewDataDescriptor.class),
            },
         new StatelessController[]
            {ControllerManager.getInstance(DomainController.class),
             ControllerManager.getInstance(UserController.class),
             ControllerManager.getInstance(UserExtrasController.class),             
             ControllerManager.getInstance(RoleController.class),
             ControllerManager.getInstance(AuthenticationController.class),
             ControllerManager.getInstance(AuthorizationController.class),
             ControllerManager.getInstance(SessionController.class),
            },
         new DataFactory[]
            {DataFactoryManager.getInstance(DomainFactory.class),
             DataFactoryManager.getInstance(UserFactory.class),             
             DataFactoryManager.getInstance(RoleFactory.class),
             DataFactoryManager.getInstance(AccessRightFactory.class),
             DataFactoryManager.getInstance(InternalSessionFactory.class),
             DataFactoryManager.getInstance(ExternalSessionFactory.class),
             DataFactoryManager.getInstance(SessionViewFactory.class),
            },
         new DatabaseSchema[] 
            {DatabaseSchemaManager.getInstance(DomainDatabaseSchema.class),
             DatabaseSchemaManager.getInstance(UserDatabaseSchema.class),            
             DatabaseSchemaManager.getInstance(SessionDatabaseSchema.class),
             DatabaseSchemaManager.getInstance(RoleDatabaseSchema.class),
             DatabaseSchemaManager.getInstance(SessionViewDatabaseSchema.class),
            }
        );
      
      // Also verify that the classes that were overridden by this module
      // are correctly configured in the system  as active classes
      ListController controller;
      
      controller = (ListController)ControllerManager.getInstance(
                                                        ListController.class);
      // We check only interfaces heres and not an implementation classes. This 
      // check is here just to remind the developer that security module 
      // provides extended functionality for the list controller, background 
      // task and others  and that it should be used but if user decides to use
      // different implementation, he is free to do so by just implementing the
      // correct interface to get around this check
      if (!(controller instanceof SecureListController))
      {
         throw new OSSConfigException(
                      "The list controller class is not correctly configured"
                      + " using property " + ListController.class.getName()
                      + ". It should point to an instance that implements "
                      + SecureListController.class.getName() + " interface."
                      + " The current configured class is " 
                      + controller.getClass().getName());
      }
   }
}
