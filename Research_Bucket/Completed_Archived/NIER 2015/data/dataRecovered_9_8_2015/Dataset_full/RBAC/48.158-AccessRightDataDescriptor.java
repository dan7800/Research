/*
 * Copyright (c) 2003 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: AccessRightDataDescriptor.java,v 1.2 2009/04/22 06:29:19 bastafidli Exp $
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

import java.util.ArrayList;
import java.util.List;

import org.opensubsystems.patterns.listdata.data.DataCondition;
import org.opensubsystems.patterns.listdata.data.ListColumnDefinition;
import org.opensubsystems.patterns.listdata.data.ListDataDescriptor;
import org.opensubsystems.patterns.listdata.data.ListOrder;

/**
 * Collection of metadata elements that describe or define behavior of Access 
 * Right objects. 
 * 
 * The metadata include
 * - constant (data type) identifying the data object 
 * - constants identifying each attribute of the data object
 * - constants identifying various sets of attributes, such as all attributes, 
 *   filterable attributes, attributes to display in list
 * - constants identifying default values of attributes 
 *
 * @version $Id: AccessRightDataDescriptor.java,v 1.2 2009/04/22 06:29:19 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed Initial revision.
 */
public class AccessRightDataDescriptor extends ListDataDescriptor
{
   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Desired value for the data type code. This can be reconfigured if there 
    * are multiple data objects which desire the same value. The rest of the 
    * constants in this class can safely use the desired value since they are 
    * valid only in the context of the data type and therefore it doesn't matter
    * what the real value is. 
    * Protected since it can be reconfigured by the framework and the real value
    * can be different.
    */
   protected static final int ACCESSRIGHT_DATA_TYPE_DESIRED_VALUE = 400;

   /**
    * Displayable name for specified data type code object. 
    * Protected since it can be customized and therefore code should use method
    * exposing it rather than the constants.
    */
   protected static final String ACCESSRIGHT_DATA_TYPE_NAME = "Access right";

   /**
    * Logical name identifying the default view for the specified data 
    * type object. Data type objects can be displayed in multiple various ways
    * called views. This constant identifies the default one. This constant
    * should have a value, that can be used to construct various identifiers, 
    * which means no special characters, no spaces, etc.
    * Protected since it can be customized and therefore code should use method
    * exposing it rather than the constants.
    */
   protected static final String ACCESSRIGHT_DATA_TYPE_VIEW = "accessright";

   /**
    * Code for table column.
    */
   public static final int COL_ACCESSRIGHT_ID 
                              = ACCESSRIGHT_DATA_TYPE_DESIRED_VALUE + 1;

   /**
    * Code for table column.
    */
   public static final int COL_ACCESSRIGHT_ROLE_ID 
                              = ACCESSRIGHT_DATA_TYPE_DESIRED_VALUE + 2;

   /**
    * Code for table column.
    */
   public static final int COL_ACCESSRIGHT_DOMAIN_ID 
                              = ACCESSRIGHT_DATA_TYPE_DESIRED_VALUE + 3;

   /**
    * Code for table column.
    */
   public static final int COL_ACCESSRIGHT_ACTION 
                              = ACCESSRIGHT_DATA_TYPE_DESIRED_VALUE + 4;

   /**
    * Code for table column.
    */
   public static final int COL_ACCESSRIGHT_DATA_TYPE 
                              = ACCESSRIGHT_DATA_TYPE_DESIRED_VALUE + 5;

   /**
    * Code for table column.
    */
   public static final int COL_ACCESSRIGHT_RIGHT_TYPE 
                              = ACCESSRIGHT_DATA_TYPE_DESIRED_VALUE + 6;

   /**
    * Code for table column.
    */
   public static final int COL_ACCESSRIGHT_CATEGORY 
                              = ACCESSRIGHT_DATA_TYPE_DESIRED_VALUE + 7;

   /**
    * Code for table column.
    */
   public static final int COL_ACCESSRIGHT_IDENTIFIER 
                              = ACCESSRIGHT_DATA_TYPE_DESIRED_VALUE + 8;

   /**
    * Code for table column.
    */
   public static final int COL_ACCESSRIGHT_CREATION_DATE 
                              = ACCESSRIGHT_DATA_TYPE_DESIRED_VALUE + 9;

   /**
    * Code for table column.
    */
   public static final int COL_ACCESSRIGHT_MODIFICATION_DATE 
                              = ACCESSRIGHT_DATA_TYPE_DESIRED_VALUE + 10;

   /**
    * Static variable for array of all columns codes.
    * The order is important since it is used to retrieve all data from the
    * persistence store efficiently so do not modify it unless you make
    * changes to other places as well.
    * Protected since derived classes can add more attributes and therefore code 
    * should use method exposing it rather than the constants.
    */
   protected static final int[] ACCESSRIGHT_ALL_COLUMNS 
                                   = {COL_ACCESSRIGHT_ID, 
                                      COL_ACCESSRIGHT_ROLE_ID, 
                                      COL_ACCESSRIGHT_DOMAIN_ID,
                                      COL_ACCESSRIGHT_ACTION, 
                                      COL_ACCESSRIGHT_DATA_TYPE,
                                      COL_ACCESSRIGHT_RIGHT_TYPE, 
                                      COL_ACCESSRIGHT_CATEGORY,
                                      COL_ACCESSRIGHT_IDENTIFIER, 
                                      COL_ACCESSRIGHT_CREATION_DATE,
                                      COL_ACCESSRIGHT_MODIFICATION_DATE,
                                     };

   /**
    * Default columns to retrieve when asked for list of objects. 
    * These should be only columns visible to user on the screen and not any
    * internal columns. Also the columns should be retrievable efficiently so
    * that the default view is very quick. 
    * Protected since derived classes can add more attributes and therefore code 
    * should use method exposing it rather than the constants.
    */
   protected static final int[] ACCESSRIGHT_DEFAULT_LIST_COLUMNS
                                = {COL_ACCESSRIGHT_DATA_TYPE,
                                   COL_ACCESSRIGHT_ACTION,
                                   COL_ACCESSRIGHT_CATEGORY,
                                   COL_ACCESSRIGHT_IDENTIFIER,
                                  };

   /**
    * Default columns to sort by when asked for list of objects. 
    * Protected since derived classes can add more attributes and therefore code 
    * should use method exposing it rather than the constants.
    */
   protected static final int[] ACCESSRIGHT_DEFAULT_LIST_SORT_COLUMNS 
                                = {COL_ACCESSRIGHT_DATA_TYPE,
                                   COL_ACCESSRIGHT_ACTION,
                                  };

   /**
    * Default order in which the columns will be sorted. 
    * Protected since derived classes can add more attributes and therefore code 
    * should use method exposing it rather than the constants.
    */
   protected static final String[] ACCESSRIGHT_DEFAULT_LIST_SORT_ORDER
                                      = ListOrder.ORDER_ASCENDING2_ARRAY;

   /**
    * List defining what columns are available to be displayed in the list. 
    * Elements are ColumnDefinition objects describing the columns to display in 
    * the list. 
    * Protected since derived classes can add more attributes and therefore code 
    * should use method exposing it rather than the constants.
    */
   protected static List ACCESSRIGHT_COLUMN_DEFINITIONS;
   
   // Constructors /////////////////////////////////////////////////////////////
   
   /**
    * Static initializer
    */
   static
   {
      ACCESSRIGHT_COLUMN_DEFINITIONS = new ArrayList();
      ACCESSRIGHT_COLUMN_DEFINITIONS.add(
         new ListColumnDefinition(AccessRightDataDescriptor.COL_ACCESSRIGHT_ID,
                                  "Id", "Internal Id",
                                  DataCondition.VALUE_TYPE_ID, 0,
                                  false));
      ACCESSRIGHT_COLUMN_DEFINITIONS.add(
         new ListColumnDefinition(AccessRightDataDescriptor.COL_ACCESSRIGHT_DATA_TYPE,
                                  "Data type", "Type of data to grant access to",
                                  DataCondition.VALUE_TYPE_INTEGER, 1, false));
      ACCESSRIGHT_COLUMN_DEFINITIONS.add(
         new ListColumnDefinition(AccessRightDataDescriptor.COL_ACCESSRIGHT_ACTION, "Action", 
                                  "Action granted to perform on the data",
                                  DataCondition.VALUE_TYPE_INTEGER, 1, false));
      ACCESSRIGHT_COLUMN_DEFINITIONS.add(
         new ListColumnDefinition(AccessRightDataDescriptor.COL_ACCESSRIGHT_CATEGORY,
                                  "Category", 
                                  "Category of data to grant access to",
                                  DataCondition.VALUE_TYPE_INTEGER, 1, false));
      ACCESSRIGHT_COLUMN_DEFINITIONS.add(
         new ListColumnDefinition(AccessRightDataDescriptor.COL_ACCESSRIGHT_CATEGORY,
                                  "Value", 
                                  "Value for category of data to grant access to",
                                  DataCondition.VALUE_TYPE_INTEGER, 1, false));
   }
   
   /**
    * Default constructor
    */
   public AccessRightDataDescriptor(
   )
   {
      super(ACCESSRIGHT_DATA_TYPE_DESIRED_VALUE, 
            ACCESSRIGHT_DATA_TYPE_NAME, 
            ACCESSRIGHT_DATA_TYPE_VIEW,
            ACCESSRIGHT_ALL_COLUMNS,
            ACCESSRIGHT_DEFAULT_LIST_COLUMNS,
            ACCESSRIGHT_DEFAULT_LIST_SORT_COLUMNS,
            ACCESSRIGHT_DEFAULT_LIST_SORT_ORDER,
            null,
            ACCESSRIGHT_COLUMN_DEFINITIONS);
   }
}
