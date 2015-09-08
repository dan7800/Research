/*
 * Copyright (c) 2003 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: MSSQLRoleDatabaseSchema.java,v 1.19 2009/04/22 06:29:40 bastafidli Exp $
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

package org.opensubsystems.security.persist.db.mssql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;
import org.opensubsystems.patterns.listdata.persist.db.ListDatabaseUtils;
import org.opensubsystems.security.persist.db.DomainDatabaseSchema;
import org.opensubsystems.security.persist.db.RoleDatabaseSchema;
import org.opensubsystems.security.persist.db.UserDatabaseSchema;
import org.opensubsystems.security.util.RoleConstants;

/**
 * Database specific operations related to persistence of roles in MS SQL Server 2000.
 *
 * @version $Id: MSSQLRoleDatabaseSchema.java,v 1.19 2009/04/22 06:29:40 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer
 * @code.reviewed TODO: Review this code
 */
public class MSSQLRoleDatabaseSchema extends RoleDatabaseSchema
{   
   /*
      Use autogenerated numbers for IDs using sequence
      Name all constraints to easily identify them later.

      create table BF_ROLE (
         ID INTEGER IDENTITY(1,1) NOT NULL,
         DOMAIN_ID INTEGER NOT NULL,
         NAME VARCHAR(50) NOT NULL,
         DESCRIPTION VARCHAR(1024) NOT NULL,
         ENABLED INTEGER NOT NULL,
         USER_ID INTEGER DEFAULT NULL,
         UNMODIFIABLE SMALLINT NOT NULL,
         CREATION_DATE DATETIME NOT NULL,
         MODIFICATION_DATE DATETIME NOT NULL,
         CONSTRAINT BF_ROLE_PK PRIMARY KEY (ID),
         CONSTRAINT BF_ROLE_UQ UNIQUE (DOMAIN_ID, NAME),
         CONSTRAINT BF_ROLE_FK FOREIGN KEY (DOMAIN_ID) 
         REFERENCES BF_DOMAIN (ID) ON DELETE NO ACTION,
         CONSTRAINT BF_ROLE_USER_FK FOREIGN KEY (USER_ID)
         REFERENCES BF_USER (ID) ON DELETE CASCADE
      )

      Create combined index DOMAIN_ID with COLUMN that can be 
      used for sorting in the list. There columns are specified by
      DEFAULT_LIST_COLUMNS constant and they are not disabled for 
      sorting within the RoleListTag class.
      
      CREATE INDEX LST_ROLENAME ON BF_ROLE (DOMAIN_ID, NAME);
      CREATE INDEX LST_ROLEENABLED ON BF_ROLE (DOMAIN_ID, ENABLED);
      CREATE INDEX LST_ROLEMODDATE ON BF_ROLE (DOMAIN_ID, MODIFICATION_DATE);

      CREATE PROCEDURE INSERT_BF_ROLE
         @IN_DOMAIN_ID INTEGER,         
         @IN_NAME VARCHAR(50),
         @IN_DESCRIPTION VARCHAR(1024),
         @IN_ENABLED INTEGER,
         @IN_USER_ID INTEGER,
         @IN_UNMODIFIABLE SMALLINT,
         @OUT_KEY INTEGER OUTPUT,
         @OUT_TIMESTAMP DATETIME OUTPUT
      AS
         SELECT @OUT_TIMESTAMP = CURRENT_TIMESTAMP;
         INSERT INTO " + strUserName + ".BF_ROLE(
            DOMAIN_ID, NAME, DESCRIPTION, ENABLED, USER_ID, UNMODIFIABLE,
            CREATION_DATE, MODIFICATION_DATE)
            VALUES (@IN_DOMAIN_ID, @IN_NAME, @IN_DESCRIPTION, @IN_ENABLED,
            @IN_USER_ID, @IN_UNMODIFIABLE, @OUT_TIMESTAMP, @OUT_TIMESTAMP);
         SET @OUT_KEY = @@IDENTITY;
      RETURN

      CREATE PROCEDURE UPDATE_BF_ROLE 
      (
         @IN_NAME VARCHAR(50),
         @IN_DESCRIPTION VARCHAR(1024),
         @IN_ENABLED INTEGER,
         @IN_ROLE_ID INTEGER,
         @IN_DOMAIN_ID INTEGER,
         @IN_MODIFICATION_DATE DATETIME,
         @IN_UNMODIFIABLE SMALLINT,
         @IN_PERSONAL_ROLE_FLAG INTEGER,
         @OUT_TIMESTAMP DATETIME OUTPUT
      )
      AS
         SELECT @OUT_TIMESTAMP = CURRENT_TIMESTAMP;
         IF (@IN_UNMODIFIABLE = 1) 
         BEGIN
           UPDATE " + strUserName + ".BF_ROLE SET NAME = @IN_NAME,  
              MODIFICATION_DATE = @OUT_TIMESTAMP WHERE ID = @IN_ROLE_ID AND  
              DOMAIN_ID = @IN_DOMAIN_ID AND UNMODIFIABLE = @IN_UNMODIFIABLE
              AND MODIFICATION_DATE = @IN_MODIFICATION_DATE;
         END
         ELSE
         BEGIN
           IF (@IN_PERSONAL_ROLE_FLAG = " + RoleConstants.SAVE_ROLE_NONPERSONAL + ")
           BEGIN
              UPDATE " + strUserName + ".BF_ROLE SET
                 NAME = @IN_NAME, DESCRIPTION = @IN_DESCRIPTION, 
                 ENABLED = @IN_ENABLED, MODIFICATION_DATE = @OUT_TIMESTAMP
                 WHERE UNMODIFIABLE = @IN_UNMODIFIABLE AND
                 ID = @IN_ROLE_ID AND DOMAIN_ID = @IN_DOMAIN_ID AND 
                 MODIFICATION_DATE = @IN_MODIFICATION_DATE AND
                 USER_ID IS NULL;
           END
           IF (@IN_PERSONAL_ROLE_FLAG = " + RoleConstants.SAVE_ROLE_PERSONAL + ")
           BEGIN
              UPDATE " + strUserName + ".BF_ROLE SET
                 MODIFICATION_DATE = @OUT_TIMESTAMP
                 WHERE UNMODIFIABLE = @IN_UNMODIFIABLE AND
                 ID = @IN_ROLE_ID AND DOMAIN_ID = @IN_DOMAIN_ID AND 
                 MODIFICATION_DATE = @IN_MODIFICATION_DATE AND
                 USER_ID IS NOT NULL;
           END
           IF (@IN_PERSONAL_ROLE_FLAG = " + RoleConstants.SAVE_ROLE_ALWAYS + ")
           BEGIN
              UPDATE " + strUserName + ".BF_ROLE SET
                 NAME = @IN_NAME, DESCRIPTION = @IN_DESCRIPTION, 
                 ENABLED = @IN_ENABLED, MODIFICATION_DATE = @OUT_TIMESTAMP
                 WHERE UNMODIFIABLE = @IN_UNMODIFIABLE AND
                 ID = @IN_ROLE_ID AND DOMAIN_ID = @IN_DOMAIN_ID AND 
                 MODIFICATION_DATE = @IN_MODIFICATION_DATE;
           END
        END
      RETURN

      create table BF_ACCESS_RIGHT 
      (
         ID INTEGER IDENTITY(1,1) NOT NULL,
         ROLE_ID INTEGER NOT NULL,
         DOMAIN_ID INTEGER NOT NULL,
         ACTION INTEGER NOT NULL,
         DATA_TYPE INTEGER NOT NULL,
         RIGHT_TYPE INTEGER NOT NULL,
         CATEGORY INTEGER NOT NULL,
         IDENTIFIER INTEGER NOT NULL,
         CREATION_DATE DATETIME NOT NULL,
         MODIFICATION_DATE DATETIME NOT NULL,
         CONSTRAINT BF_ACCESS_RIGHT_PK PRIMARY KEY (ID),
         CONSTRAINT BF_ACCESS_RIGHT_ROLE_FK FOREIGN KEY (ROLE_ID) 
         REFERENCES BF_ROLE (ID) ON DELETE CASCADE,
         CONSTRAINT BF_ACCESS_RIGHT_DOMAIN_FK FOREIGN KEY (DOMAIN_ID) 
         REFERENCES BF_DOMAIN (ID) ON DELETE NO ACTION
      )

      CREATE PROCEDURE INSERT_BF_ACCESS_RIGHT
         @IN_ROLE_ID INTEGER,
         @IN_DOMAIN_ID INTEGER,
         @IN_ACTION INTEGER,
         @IN_DATA_TYPE INTEGER,
         @IN_RIGHT_TYPE INTEGER,
         @IN_CATEGORY INTEGER,                             
         @IN_IDENTIFIER INTEGER,
         @OUT_KEY INTEGER OUTPUT, 
         @OUT_TIMESTAMP DATETIME OUTPUT
      AS
         SELECT @OUT_TIMESTAMP = CURRENT_TIMESTAMP;
         INSERT INTO " + strUserName + ".BF_ACCESS_RIGHT(ROLE_ID, DOMAIN_ID, 
            ACTION, DATA_TYPE, RIGHT_TYPE, CATEGORY, IDENTIFIER, 
            CREATION_DATE, MODIFICATION_DATE) 
            VALUES (@IN_ROLE_ID, @IN_DOMAIN_ID, @IN_ACTION, @IN_DATA_TYPE,
            @IN_RIGHT_TYPE, @IN_CATEGORY, @IN_IDENTIFIER, @OUT_TIMESTAMP,
            @OUT_TIMESTAMP);
         SET @OUT_KEY = @@IDENTITY;
      RETURN

      create table BF_USER_ROLE_MAP
      (
         USER_ID INTEGER NOT NULL,
         ROLE_ID INTEGER NOT NULL,
         CONSTRAINT BF_USER_ROLE_MAP_UQ UNIQUE (USER_ID,ROLE_ID),
         CONSTRAINT BF_USER_ROLE_MAP_FK1 FOREIGN KEY (USER_ID) 
            REFERENCES BF_USER (ID) ON DELETE CASCADE,
         CONSTRAINT BF_USER_ROLE_MAP_FK2 FOREIGN KEY (ROLE_ID) 
            REFERENCES BF_ROLE (ID) ON DELETE NO ACTION
      )

      create table BF_DOMAIN_ROLE_MAP
      (
         DOMAIN_ID INTEGER NOT NULL,
         ROLE_ID INTEGER NOT NULL,
         CONSTRAINT BF_DOM_ROL_MAP_UQ UNIQUE (DOMAIN_ID,ROLE_ID),
         CONSTRAINT BF_DOM_ROL_MAP_FK1 FOREIGN KEY (DOMAIN_ID) 
            REFERENCES BF_DOMAIN (ID) ON DELETE CASCADE,
         CONSTRAINT BF_DOM_ROL_MAP_FK2 FOREIGN KEY (ROLE_ID) 
            REFERENCES BF_ROLE (ID) ON DELETE NO ACTION
      )
   */
   
   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Maximal length of role name.
    */
   public static final int ROLE_NAME_MAXLENGTH = 50;

   /**
    * Maximal length of role description.
    */
   public static final int ROLE_DESCRIPTION_MAXLENGTH = 1024;

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(MSSQLRoleDatabaseSchema.class);

   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Default constructor.
    * 
    * @throws OSSException - error occurred.
    */
   public MSSQLRoleDatabaseSchema(
   ) throws OSSException
   {
      super();

      // Setup maximal length of individual fields for entities
      m_roleDescriptor.setNameMaxLength(ROLE_NAME_MAXLENGTH);
      m_roleDescriptor.setDescriptionMaxLength(ROLE_DESCRIPTION_MAXLENGTH);
   }   

   // Logic ////////////////////////////////////////////////////////////////////
   
   /**
    * {@inheritDoc}
    */
   public void create(
      Connection cntDBConnection,
      String     strUserName
   ) throws SQLException, OSSException
   {
      Statement stmQuery = null;
      try
      {        
         stmQuery = cntDBConnection.createStatement();

         if (stmQuery.execute(
            "create table " + ROLE_TABLE_NAME + NL +
            "(" + NL +
            "   ID INTEGER IDENTITY(1,1) NOT NULL," + NL +
            "   DOMAIN_ID INTEGER NOT NULL," + NL +
            "   NAME VARCHAR(" + ROLE_NAME_MAXLENGTH + ") NOT NULL," + NL +
            "   DESCRIPTION VARCHAR(" + ROLE_DESCRIPTION_MAXLENGTH + ") NOT NULL," + NL +
            "   ENABLED INTEGER NOT NULL," + NL +
            "   USER_ID INTEGER DEFAULT NULL," + NL +
            "   CREATION_DATE DATETIME NOT NULL," + NL +
            "   UNMODIFIABLE SMALLINT NOT NULL," + NL +
            "   MODIFICATION_DATE DATETIME NOT NULL," + NL +
            "   CONSTRAINT " + ROLE_TABLE_NAME + "_PK PRIMARY KEY (ID)," + NL +
            "   CONSTRAINT " + ROLE_TABLE_NAME + "_UQ UNIQUE (DOMAIN_ID, NAME)," + NL +
            "   CONSTRAINT " + ROLE_TABLE_NAME + "_FK FOREIGN KEY (DOMAIN_ID) " + NL + 
            "      REFERENCES " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + " (ID) ON DELETE NO ACTION, " + NL +
            "   CONSTRAINT " + ROLE_TABLE_NAME + "_USER_FK FOREIGN KEY (USER_ID) " + NL +
            "   REFERENCES " + UserDatabaseSchema.USER_TABLE_NAME + " (ID) ON DELETE CASCADE" + NL +
            ")"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table " + ROLE_TABLE_NAME + " created.");

         createInsertRoleStoredProc(stmQuery, strUserName);
         createUpdateRoleStoredProc(stmQuery, strUserName);

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute(
            "create table " + ACCESSRIGHT_TABLE_NAME + NL +
            "(" +
            "   ID INTEGER IDENTITY(1,1) NOT NULL," + NL +
            "   ROLE_ID INTEGER NOT NULL," + NL +
            "   DOMAIN_ID INTEGER NOT NULL," + NL +                              
            "   ACTION INTEGER NOT NULL," + NL +
            "   DATA_TYPE INTEGER NOT NULL," + NL +
            "   RIGHT_TYPE INTEGER NOT NULL," + NL +
            "   CATEGORY INTEGER NOT NULL," + NL +
            "   IDENTIFIER INTEGER NOT NULL," + NL +
            "   CREATION_DATE DATETIME NOT NULL," + NL +
            "   MODIFICATION_DATE DATETIME NOT NULL," + NL +
            "   CONSTRAINT " + ACCESSRIGHT_TABLE_NAME + "_PK PRIMARY KEY (ID)," + NL +
            "   CONSTRAINT " + ACCESSRIGHT_TABLE_NAME + "_ROLE_FK FOREIGN KEY (ROLE_ID) " + NL +
            "      REFERENCES " + ROLE_TABLE_NAME + " (ID) ON DELETE CASCADE," + NL +
            "   CONSTRAINT " + ACCESSRIGHT_TABLE_NAME + "_DOMAIN_FK FOREIGN KEY (DOMAIN_ID) " + NL +
            "      REFERENCES " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + " (ID) ON DELETE NO ACTION" + NL +
            ")"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         } 
         s_logger.log(Level.FINEST, "Table " + ACCESSRIGHT_TABLE_NAME + " created.");

         ///////////////////////////////////////////////////////////////////////

         // create all combined indexes used for speeding up retrieving data into the list
         createListIndexes(cntDBConnection);

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute(
            "CREATE PROCEDURE INSERT_" + ACCESSRIGHT_TABLE_NAME + NL +
            "   @IN_ROLE_ID INTEGER," + NL +
            "   @IN_DOMAIN_ID INTEGER," + NL +
            "   @IN_ACTION INTEGER," + NL +
            "   @IN_DATA_TYPE INTEGER," + NL +
            "   @IN_RIGHT_TYPE INTEGER," + NL +
            "   @IN_CATEGORY INTEGER," + NL +                             
            "   @IN_IDENTIFIER INTEGER," + NL +
            "   @OUT_KEY INTEGER OUTPUT, " + NL +
            "   @OUT_TIMESTAMP DATETIME OUTPUT " + NL +
            "AS " + NL +
            "   SELECT @OUT_TIMESTAMP = CURRENT_TIMESTAMP;" + NL + 
            "   INSERT INTO " + strUserName + "." + ACCESSRIGHT_TABLE_NAME + "(ROLE_ID, DOMAIN_ID, " + NL +
            "      ACTION, DATA_TYPE, RIGHT_TYPE, CATEGORY, IDENTIFIER, " + NL +
            "      CREATION_DATE, MODIFICATION_DATE) " + NL +
            "      VALUES (@IN_ROLE_ID, @IN_DOMAIN_ID, @IN_ACTION, @IN_DATA_TYPE," + NL +
            "      @IN_RIGHT_TYPE, @IN_CATEGORY, @IN_IDENTIFIER, @OUT_TIMESTAMP," + NL +
            "      @OUT_TIMESTAMP);" + NL +
            "   SET @OUT_KEY = @@IDENTITY; " + NL +
            "RETURN"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         } 
         s_logger.log(Level.FINEST, "Procedure INSERT_" + ACCESSRIGHT_TABLE_NAME + " created.");

         ///////////////////////////////////////////////////////////////////////
         
         if (stmQuery.execute(
            "create table " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP" + NL +
            "(" + NL +
            "   USER_ID INTEGER NOT NULL," + NL +
            "   ROLE_ID INTEGER NOT NULL," + NL +
            "   CONSTRAINT " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP_UQ UNIQUE (USER_ID,ROLE_ID)," + NL +
            "   CONSTRAINT " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP_FK1 FOREIGN KEY (USER_ID) " + NL +
            "      REFERENCES " + UserDatabaseSchema.USER_TABLE_NAME + " (ID) ON DELETE NO ACTION," + NL +
            "   CONSTRAINT " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP_FK2 FOREIGN KEY (ROLE_ID) " + NL +
            "      REFERENCES " + ROLE_TABLE_NAME + " (ID) ON DELETE CASCADE" + NL +
            ")"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table " + UserDatabaseSchema.USER_TABLE_NAME + "_ROLE_MAP created.");

         ///////////////////////////////////////////////////////////////////////
         
         if (stmQuery.execute(
            "create table " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + "_ROLE_MAP" + NL +
            "(" + NL +
            "   DOMAIN_ID INTEGER NOT NULL," + NL +
            "   ROLE_ID INTEGER NOT NULL," + NL +
            "   CONSTRAINT BF_DOM_ROL_MAP_UQ UNIQUE (DOMAIN_ID,ROLE_ID)," + NL +
            "   CONSTRAINT BF_DOM_ROL_MAP_FK1 FOREIGN KEY (DOMAIN_ID) " + NL +
            "      REFERENCES " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + " (ID) ON DELETE NO ACTION," + NL +
            "   CONSTRAINT BF_DOM_ROL_MAP_FK2 FOREIGN KEY (ROLE_ID) " + NL +
            "      REFERENCES " + ROLE_TABLE_NAME + " (ID) ON DELETE CASCADE" + NL +
            ")"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + "_ROLE_MAP created.");

         ///////////////////////////////////////////////////////////////////////
      }
      catch (SQLException sqleExc)
      {
         s_logger.log(Level.WARNING, "Failed to create role schema.", sqleExc);
         throw sqleExc;
      }
      finally
      {
         DatabaseUtils.closeStatement(stmQuery);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void createListIndexes(
      Connection cntDBConnection
   ) throws SQLException, 
            OSSException
   {
      Statement stmQuery = null;
      String strIndexName = null;

      try
      {
         stmQuery = cntDBConnection.createStatement();
         // This index causes problem for Oracle because it is automatically indexed 
         // for unique column - therefore we have to specify it within each DB specific 
         // class.
         strIndexName = ListDatabaseUtils.getInstance().generateListIndexName(
                           ROLE_TABLE_NAME, "NAME");
         if (stmQuery.execute("CREATE INDEX " + strIndexName + " ON " + ROLE_TABLE_NAME + " (DOMAIN_ID, NAME)"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Index " + strIndexName + " created.");
      }
      catch (SQLException sqleExc)
      {
         s_logger.log(Level.WARNING, 
                      "Failed to create schema LST indexes for Role data object", sqleExc);
         throw sqleExc;
      }
      finally
      {
         DatabaseUtils.closeStatement(stmQuery);
      }

      super.createListIndexes(cntDBConnection);
   }

   /**
    * {@inheritDoc}
    */
   public void upgrade(
      Connection cntDBConnection,
      String     strUserName,
      int        iOriginalVersion
   ) throws SQLException
   {
      Statement stmQuery = null;
      try
      {        
         int iVersion = iOriginalVersion;
         
         stmQuery = cntDBConnection.createStatement();
         
         if (iVersion < 2)
         {
            // Changes from version 1 to version 2
            
            // First drop old procedures so in case the table is not updated
            // at least the data cannot be modified
            if (stmQuery.execute("drop dbprocedure INSERT_" + ROLE_TABLE_NAME))
            {
               // Close any results
               stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);      
            }
            s_logger.log(Level.FINEST, "Procedure INSERT_" + ROLE_TABLE_NAME + " deleted.");
            
            if (stmQuery.execute("drop dbprocedure UPDATE_" + ROLE_TABLE_NAME))
            {
               // Close any results
               stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);      
            }
            s_logger.log(Level.FINEST, "Procedure UPDATE_" + ROLE_TABLE_NAME + " deleted.");
            
            // Now add new column to the table
            if (stmQuery.execute("alter table " + ROLE_TABLE_NAME + " add USER_ID INTEGER DEFAULT NULL"))
            {
               // Close any results
               stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);      
            }
            s_logger.log(Level.FINEST, "New columns added to table " + ROLE_TABLE_NAME + ".");
            
            // Now add new constraints to the table
            if (stmQuery.execute("alter table " + ROLE_TABLE_NAME + " add" +
                                 " FOREIGN KEY " + ROLE_TABLE_NAME + "_USER_FK (USER_ID)" +
                                 " REFERENCES " + UserDatabaseSchema.USER_TABLE_NAME + " (ID) ON DELETE CASCADE"))
            {
               // Close any results
               stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);      
            }
            s_logger.log(Level.FINEST, "New constraints added to table " + ROLE_TABLE_NAME + ".");
            
            // Now recreate the new procedures 
            createInsertRoleStoredProc(stmQuery, strUserName);
            createUpdateRoleStoredProc(stmQuery, strUserName);
            
            // Now the schema is upgraded to next new version
            iVersion++;
         }
         
         if (iVersion < 3)
         {
            // Changes from version 2 to version 3
            
            // First drop old procedures so in case the table is not updated
            // at least the data cannot be modified
            if (stmQuery.execute("drop dbprocedure INSERT_" + ROLE_TABLE_NAME))
            {
               // Close any results
               stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);      
            }
            s_logger.log(Level.FINEST, "Procedure INSERT_" + ROLE_TABLE_NAME + " deleted.");
            
            if (stmQuery.execute("drop dbprocedure UPDATE_" + ROLE_TABLE_NAME))
            {
               // Close any results
               stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);      
            }
            s_logger.log(Level.FINEST, "Procedure UPDATE_" + ROLE_TABLE_NAME + " deleted.");
            
            // Now add new column to the table
            if (stmQuery.execute("alter table " + ROLE_TABLE_NAME + " add UNMODIFIABLE smallint"))
            {
               // Close any results
               stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);      
            }
            s_logger.log(Level.FINEST, "New columns added to table " + ROLE_TABLE_NAME + ".");
            
            if (stmQuery.execute("update " + ROLE_TABLE_NAME + " set UNMODIFIABLE = 0"))
            {
               // Close any results
               stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);      
            }
            s_logger.log(Level.FINEST, "All unmodifiable values were set to 0.");
            
            // Now set unmodifiable to not null
            if (stmQuery.execute("alter table " + ROLE_TABLE_NAME + " modify UNMODIFIABLE smallint not null"))
            {
               // Close any results
               stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);      
            }
            s_logger.log(Level.FINEST, "Column unmodifiable was set to null.");
            
            // Now recreate the new procedures 
            createInsertRoleStoredProc(stmQuery, strUserName);
            createUpdateRoleStoredProc(stmQuery, strUserName);
            
            // Now the schema is upgraded to next new version
            iVersion++;
         }
         // if (iVersion < 4)...
         
         if (iVersion != getVersion())
         {
            throw new SQLException("Role database schema upgraded only to version "
                                   + iVersion + " while the current version is "
                                   + getVersion()); 
         }
      }
      catch (SQLException sqleExc)
      {
         s_logger.log(Level.WARNING, "Failed to upgrade role schema" +
               " from version " + iOriginalVersion + " to version " + getVersion(), 
               sqleExc);
         throw sqleExc;
      }
      finally
      {
         DatabaseUtils.closeStatement(stmQuery);
      }
   }

   /**
    * {@inheritDoc}
    */
   public String getInsertRoleAndFetchGeneratedValues(
   ) throws OSSException
   {
      return "EXEC INSERT_" + ROLE_TABLE_NAME + " ?, ?, ?, ?, ?, ?, ?, ?";  
   }

   /**
    * {@inheritDoc}
    */
   public String getUpdateRoleAndFetchGeneratedValues(
      boolean unmodifiable,
      int     roleType
   ) throws OSSException
   {
      return "EXEC UPDATE_" + ROLE_TABLE_NAME + " ?, ?, ?, ?, ?, ?, ?, ?, ?";  
   }
   
   /**
    * {@inheritDoc}
    */
   public String getInsertAccessRightAndFetchGeneratedValues(
   ) throws OSSException
   {
      return "EXEC INSERT_" + ACCESSRIGHT_TABLE_NAME + " ?, ?, ?, ?, ?, ?, ?, ?, ?";
   }

   // Helper methods ///////////////////////////////////////////////////////////

   /**
    * Helper method to create stored procedure to insert role.
    * 
    * @param stmQuery - statement to use to create the procedure
    * @param strUserName - name of user who the tables procedure accesses belongs to
    * @throws SQLException - an error has occurred
    */
   protected void createInsertRoleStoredProc(
      Statement stmQuery,
      String    strUserName
   ) throws SQLException
   {
      if (stmQuery.execute(
         "CREATE PROCEDURE INSERT_" + ROLE_TABLE_NAME + NL +
         "   @IN_DOMAIN_ID INTEGER," + NL +         
         "   @IN_NAME VARCHAR(" + ROLE_NAME_MAXLENGTH + ")," + NL +
         "   @IN_DESCRIPTION VARCHAR(" + ROLE_DESCRIPTION_MAXLENGTH + ")," + NL +
         "   @IN_ENABLED INTEGER," + NL +
         "   @IN_USER_ID INTEGER," + NL +
         "   @IN_UNMODIFIABLE SMALLINT," + NL +
         "   @OUT_KEY INTEGER OUTPUT," + NL +
         "   @OUT_TIMESTAMP DATETIME OUTPUT " + NL +
         "AS " + NL +
         "   SELECT @OUT_TIMESTAMP = CURRENT_TIMESTAMP;" + NL + 
         "   INSERT INTO " + strUserName + "." + ROLE_TABLE_NAME + "(" + NL +
         "      DOMAIN_ID, NAME, DESCRIPTION, ENABLED, USER_ID, UNMODIFIABLE," + NL +
         "      CREATION_DATE, MODIFICATION_DATE)" + NL +
         "      VALUES (@IN_DOMAIN_ID, @IN_NAME, @IN_DESCRIPTION, @IN_ENABLED," + NL +
         "      @IN_USER_ID, @IN_UNMODIFIABLE, @OUT_TIMESTAMP, @OUT_TIMESTAMP);" + NL +
         "   SET @OUT_KEY = @@IDENTITY; " + NL +
         "RETURN"))
      {
         // Close any results
         stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
      }
      s_logger.log(Level.FINEST, "Procedure INSERT_" + ROLE_TABLE_NAME + " created.");      
   }
   
   /**
    * Helper method to create stored procedure to update role.
    * 
    * @param stmQuery - statement to use to create the procedure
    * @param strUserName - name of user who the tables procedure accesses belongs to
    * @throws SQLException - an error has occurred
    */
   protected void createUpdateRoleStoredProc(
      Statement stmQuery,
      String    strUserName
   ) throws SQLException
   {
      if (stmQuery.execute(
         "CREATE PROCEDURE UPDATE_" + ROLE_TABLE_NAME + NL +
         "   @IN_NAME VARCHAR(" + ROLE_NAME_MAXLENGTH + ")," + NL +
         "   @IN_DESCRIPTION VARCHAR(" + ROLE_DESCRIPTION_MAXLENGTH + ")," + NL +
         "   @IN_ENABLED INTEGER," + NL +
         "   @IN_ROLE_ID INTEGER," + NL +
         "   @IN_DOMAIN_ID INTEGER," + NL +
         "   @IN_MODIFICATION_DATE DATETIME," + NL +
         "   @IN_UNMODIFIABLE SMALLINT," + NL +
         "   @IN_PERSONAL_ROLE_FLAG INTEGER," + NL +
         "   @OUT_TIMESTAMP DATETIME OUTPUT " +
         "AS " + NL +
         "   SELECT @OUT_TIMESTAMP = CURRENT_TIMESTAMP;" + NL +
         "   IF (@IN_UNMODIFIABLE = 1) " + NL +
         "   BEGIN " + NL +
         "     UPDATE " + strUserName + "." + ROLE_TABLE_NAME + " SET NAME = @IN_NAME, " + NL + 
         "        MODIFICATION_DATE = @OUT_TIMESTAMP WHERE ID = @IN_ROLE_ID AND " + NL + 
         "        DOMAIN_ID = @IN_DOMAIN_ID AND UNMODIFIABLE = @IN_UNMODIFIABLE " + NL +
         "        AND MODIFICATION_DATE = @IN_MODIFICATION_DATE;" + NL +
         "   END " + NL +
         "   ELSE " + NL +
         "   BEGIN " + NL +
         "     IF (@IN_PERSONAL_ROLE_FLAG = " + RoleConstants.SAVE_ROLE_NONPERSONAL + ") " + NL +
         "     BEGIN " + NL +
         "        UPDATE " + strUserName + "." + ROLE_TABLE_NAME + " SET" + NL +
         "           NAME = @IN_NAME, DESCRIPTION = @IN_DESCRIPTION," + NL + 
         "           ENABLED = @IN_ENABLED, MODIFICATION_DATE = @OUT_TIMESTAMP" + NL +
         "           WHERE UNMODIFIABLE = @IN_UNMODIFIABLE AND " + NL +
         "           ID = @IN_ROLE_ID AND DOMAIN_ID = @IN_DOMAIN_ID AND" + NL + 
         "           MODIFICATION_DATE = @IN_MODIFICATION_DATE AND" + NL +
         "           USER_ID IS NULL;" + NL +
         "     END " + NL +
         "     IF (@IN_PERSONAL_ROLE_FLAG = " + RoleConstants.SAVE_ROLE_PERSONAL + ") " + NL +
         "     BEGIN " + NL +
         "        UPDATE " + strUserName + "." + ROLE_TABLE_NAME + " SET" + NL +
         "           MODIFICATION_DATE = @OUT_TIMESTAMP" + NL +
         "           WHERE UNMODIFIABLE = @IN_UNMODIFIABLE AND " + NL +
         "           ID = @IN_ROLE_ID AND DOMAIN_ID = @IN_DOMAIN_ID AND" + NL + 
         "           MODIFICATION_DATE = @IN_MODIFICATION_DATE AND" + NL +
         "           USER_ID IS NOT NULL;" + NL +
         "     END " + NL +
         "     IF (@IN_PERSONAL_ROLE_FLAG = " + RoleConstants.SAVE_ROLE_ALWAYS + ") " + NL +
         "     BEGIN " + NL +
         "        UPDATE " + strUserName + "." + ROLE_TABLE_NAME + " SET" + NL +
         "           NAME = @IN_NAME, DESCRIPTION = @IN_DESCRIPTION," + NL + 
         "           ENABLED = @IN_ENABLED, MODIFICATION_DATE = @OUT_TIMESTAMP" + NL +
         "           WHERE UNMODIFIABLE = @IN_UNMODIFIABLE AND " + NL +
         "           ID = @IN_ROLE_ID AND DOMAIN_ID = @IN_DOMAIN_ID AND" + NL + 
         "           MODIFICATION_DATE = @IN_MODIFICATION_DATE;" + NL +
         "     END " + NL +
         "   END " + NL +
         "RETURN"))
      {
         // Close any results
         stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
      }
      s_logger.log(Level.FINEST, "Procedure UPDATE_" + ROLE_TABLE_NAME + " created.");         
   }
}
