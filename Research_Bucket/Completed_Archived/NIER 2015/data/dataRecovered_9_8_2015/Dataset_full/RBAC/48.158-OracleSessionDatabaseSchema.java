/*
 * Copyright (c) 2003 - 2008 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: OracleSessionDatabaseSchema.java,v 1.14 2009/04/22 06:29:35 bastafidli Exp $
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

package org.opensubsystems.security.persist.db.oracle;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.persist.db.impl.DatabaseImpl;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.jdbc.DatabaseUtils;
import org.opensubsystems.security.data.InternalSessionDataDescriptor;
import org.opensubsystems.security.persist.db.DomainDatabaseSchema;
import org.opensubsystems.security.persist.db.SessionDatabaseSchema;
import org.opensubsystems.security.persist.db.UserDatabaseSchema;

/**
 * Database specific operations related to persistence of sessions in Oracle 9i.
 *
 * @version $Id: OracleSessionDatabaseSchema.java,v 1.14 2009/04/22 06:29:35 bastafidli Exp $
 * @author Julo Legeny
 * @code.reviewer
 * @code.reviewed TODO: Review this code
 */
public class OracleSessionDatabaseSchema extends SessionDatabaseSchema
{
   /*
      Database tables bfsession and srvsession database factories works with.
      Use sequence for ID
      Name all constraints to easily identify them later.

      CREATE SEQUENCE INTSESS_ID_SEQ INCREMENT BY 1 START WITH 1 NOCYCLE


      CREATE TABLE BF_INTERNAL_SESSION
      (
         ID INTEGER NOT NULL,
         DOMAIN_ID INTEGER NOT NULL,
         USER_ID INTEGER NOT NULL,
         GEN_CODE VARCHAR(50) NOT NULL,
         CLIENT_IP VARCHAR(20) NOT NULL,
         CLIENT_TYPE VARCHAR(100) NULL,
         CREATION_DATE TIMESTAMP{9} NOT NULL,
         GUEST_ACCESS SMALLINT NOT NULL,
         CONSTRAINT BF_INTSES_PK PRIMARY KEY (ID),
         CONSTRAINT BF_INTSES_UQ UNIQUE (GEN_CODE),
         CONSTRAINT BF_INTSES_DID_FK FOREIGN KEY (DOMAIN_ID)
            REFERENCES BF_DOMAIN (ID) ON DELETE CASCADE,
         CONSTRAINT BF_INTSES_UID_FK FOREIGN KEY (USER_ID)
            REFERENCES BF_USER (ID) ON DELETE CASCADE
       );


      Create combined index DOMAIN_ID with COLUMN that can be 
      used for sorting in the list. There columns are specified by
      DEFAULT_LIST_COLUMNS constant and they are not disabled for 
      sorting within the RoleListTag class.
      
      CREATE INDEX LST_INTSIONCLINTIP ON BF_INTERNAL_SESSION (DOMAIN_ID, CLIENT_IP);
      CREATE INDEX LST_INTSIONCLITYPE ON BF_INTERNAL_SESSION (DOMAIN_ID, CLIENT_TYPE);
      CREATE INDEX LST_INTSIONCREDATE ON BF_INTERNAL_SESSION (DOMAIN_ID, CREATION_DATE);


       CREATE OR REPLACE PROCEDURE INSERT_BF_INTERNAL_SESSION 
       (
          IN_DOMAIN_ID IN INTEGER, 
          IN_USER_ID IN INTEGER,
          IN_GEN_CODE IN VARCHAR,
          IN_CLIENT_IP IN VARCHAR,
          IN_CLIENT_TYPE IN VARCHAR,
          IN_GUEST_ACCESS IN SMALLINT, 
          OUT_KEY OUT INTEGER, 
          OUT_TIMESTAMP OUT TIMESTAMP
       )
       IS
          NEW_OUT_KEY INTEGER;
          NEW_OUT_TIMESTAMP TIMESTAMP(9);
       BEGIN
          SELECT INTSESS_ID_SEQ.NEXTVAL INTO NEW_OUT_KEY FROM DUAL;
          OUT_KEY := NEW_OUT_KEY;
          SELECT SYSDATE INTO NEW_OUT_TIMESTAMP FROM DUAL;
          OUT_TIMESTAMP := NEW_OUT_TIMESTAMP;
          INSERT INTO " + strUserName + ".BF_INTERNAL_SESSION(ID, DOMAIN_ID, USER_ID,  
             GEN_CODE, CLIENT_IP, CLIENT_TYPE, CREATION_DATE, GUEST_ACCESS)
              VALUES (OUT_KEY, IN_DOMAIN_ID, IN_USER_ID, IN_GEN_CODE, IN_CLIENT_IP, 
              IN_CLIENT_TYPE, OUT_TIMESTAMP, IN_GUEST_ACCESS);
       END;


      CREATE SEQUENCE EXTSESS_ID_SEQ INCREMENT BY 1 START WITH 1 NOCYCLE

            
      CREATE TABLE BF_EXTERNAL_SESSION
      (
         ID INTEGER NOT NULL,
         DOMAIN_ID INTEGER NOT NULL,
         INTERNAL_SESSION_ID INTEGER NOT NULL,
         GEN_CODE VARCHAR(100) NOT NULL,
         SERVER VARCHAR(100) NOT NULL,
         CREATION_DATE TIMESTAMP(9) NOT NULL,
         CONSTRAINT BF_EXTSES_PK PRIMARY KEY (ID),
         CONSTRAINT BF_EXTSES_UQ UNIQUE (GEN_CODE, SERVER),
         CONSTRAINT BF_INTSES_DID_FK FOREIGN KEY (DOMAIN_ID)
         REFERENCES BF_DOMAIN (ID) ON DELETE CASCADE,
         CONSTRAINT BF_EXTSES_FK FOREIGN KEY (INTERNAL_SESSION_ID)
         REFERENCES BF_INTERNAL_SESSION (ID) ON DELETE CASCADE
      );

       CREATE OR REPLACE PROCEDURE INSERT_BF_EXTERNAL_SESSION 
       (
          IN_DOMAIN_ID IN INTEGER, 
          IN_INTERNAL_SESSION_ID IN INTEGER,
          IN_GEN_CODE IN VARCHAR,
          IN_SERVER IN VARCHAR,
          OUT_KEY OUT INTEGER,
          OUT_TIMESTAMP OUT TIMESTAMP
       )
       IS
          NEW_OUT_TIMESTAMP TIMESTAMP(9);
       BEGIN
          SELECT SYSDATE INTO NEW_OUT_TIMESTAMP FROM DUAL;
          OUT_TIMESTAMP := NEW_OUT_TIMESTAMP;
          INSERT INTO " + strUserName + ".BF_EXTERNAL_SESSION(ID, DOMAIN_ID, 
              INTERNAL_SESSION_ID, GEN_CODE, SERVER, CREATION_DATE)
              VALUES (OUT_KEY, IN_DOMAIN_ID, IN_INTERNAL_SESSION_ID, IN_GEN_CODE,  
              IN_SERVER, OUT_TIMESTAMP);
       END;
   */

   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Maximal length of internal session generated code.
    */
   public static final int INTSESSION_GEN_CODE_MAXLENGTH = InternalSessionDataDescriptor.INTSESSION_GENCODE_MAXLENGTH;

   /**
    * Maximal length of client IP.
    */
   public static final int INTSESSION_CLIENT_IP_MAXLENGTH = 20;

   /**
    * Maximal length of client type.
    */
   public static final int INTSESSION_CLIENT_TYPE_MAXLENGTH = 100;

   /**
    * Maximal length of external session generated code.
    */
   public static final int EXTSESSION_GEN_CODE_MAXLENGTH = 100;

   /**
    * Maximal length of server identification.
    * Server identification has to be long since we can be storing there IP
    * toether with machine name and port which could have different length
    */
   public static final int EXTSESSION_SERVER_MAXLENGTH = 100;

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(OracleSessionDatabaseSchema.class);

   // Constructors /////////////////////////////////////////////////////////////

   /**
    * Default constructor.
    * 
    * @throws OSSException - an error has occurred
    */
   public OracleSessionDatabaseSchema(
   ) throws OSSException
   {
      super();

      // Setup maximal length of individual fields for entities
      m_intSessionDescriptor.setInternalSessionMaxLength(INTSESSION_GEN_CODE_MAXLENGTH);
      m_intSessionDescriptor.setClientIPMaxLength(INTSESSION_CLIENT_IP_MAXLENGTH);
      m_intSessionDescriptor.setClientTypeMaxLength(INTSESSION_CLIENT_TYPE_MAXLENGTH);
      m_extSessionDescriptor.setServerSessionGenMaxLength(EXTSESSION_GEN_CODE_MAXLENGTH);
      m_extSessionDescriptor.setServerMaxLength(EXTSESSION_SERVER_MAXLENGTH);
   }

   // Logic /////////////////////////////////////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   public void create(
      Connection cntDBConnection,
      String strUserName
   ) throws SQLException, OSSException
   {
      Statement stmQuery = null;
      try
      {
         stmQuery = cntDBConnection.createStatement();

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute(
            "CREATE SEQUENCE INTSESS_ID_SEQ INCREMENT BY 1 START WITH 1 NOCYCLE"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Sequence INTSESS_ID_SEQ created.");

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute(
            "create table " + INTSESSION_TABLE_NAME + NL +
            "(" + NL +
            "   ID INTEGER NOT NULL," + NL +
            "   DOMAIN_ID INTEGER NOT NULL," + NL +
            "   USER_ID INTEGER NOT NULL," + NL +
            "   GEN_CODE VARCHAR(" + INTSESSION_GEN_CODE_MAXLENGTH + ") NOT NULL," +
            "   CLIENT_IP VARCHAR(" + INTSESSION_CLIENT_IP_MAXLENGTH + ") NULL," + NL +
            "   CLIENT_TYPE VARCHAR(" + INTSESSION_CLIENT_TYPE_MAXLENGTH + ") NULL," + NL +
            "   CREATION_DATE TIMESTAMP(9) NOT NULL," + NL +
            "   GUEST_ACCESS SMALLINT NOT NULL," + NL +
            "   CONSTRAINT BF_INTSES_PK PRIMARY KEY (ID)," + NL +
            "   CONSTRAINT BF_INTSES_UQ UNIQUE (GEN_CODE)," + NL +
            "   CONSTRAINT BF_INTSES_DID_FK FOREIGN KEY (DOMAIN_ID) " + NL +
            "      REFERENCES " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + " (ID) ON DELETE CASCADE," + NL +
            "   CONSTRAINT BF_INTSES_FK FOREIGN KEY (USER_ID) " + NL +
            "      REFERENCES " + UserDatabaseSchema.USER_TABLE_NAME + " (ID) ON DELETE CASCADE" + NL +
            ")"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table " + INTSESSION_TABLE_NAME + " created.");

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute(
            "CREATE OR REPLACE PROCEDURE INSERT_" + INTSESSION_TABLE_NAME + NL +
            "(" + NL +
            "   IN_DOMAIN_ID IN INTEGER, " + NL +
            "   IN_USER_ID IN INTEGER," + NL +
            "   IN_GEN_CODE IN VARCHAR," + NL +
            "   IN_CLIENT_IP IN VARCHAR," + NL +
            "   IN_CLIENT_TYPE IN VARCHAR," + NL +
            "   IN_GUEST_ACCESS IN SMALLINT, " + NL +
            "   OUT_KEY OUT INTEGER, " + NL +
            "   OUT_TIMESTAMP OUT TIMESTAMP" + NL +
            ") " + NL +
            "IS " + NL +
            "   NEW_OUT_KEY INTEGER; " + NL +
            "   NEW_OUT_TIMESTAMP TIMESTAMP(9); " + NL +
            "BEGIN " + NL +
            "   SELECT INTSESS_ID_SEQ.NEXTVAL INTO NEW_OUT_KEY FROM DUAL; " + NL +
            "   OUT_KEY := NEW_OUT_KEY; " + NL +
            "   SELECT SYSDATE INTO NEW_OUT_TIMESTAMP FROM DUAL; " + NL +
            "   OUT_TIMESTAMP := NEW_OUT_TIMESTAMP; " + NL +
            "   INSERT INTO " + strUserName + "." + INTSESSION_TABLE_NAME + "(ID, DOMAIN_ID, USER_ID, " + NL +
            "      GEN_CODE, CLIENT_IP, CLIENT_TYPE, CREATION_DATE, GUEST_ACCESS)" + NL +
            "       VALUES (OUT_KEY, IN_DOMAIN_ID, IN_USER_ID, IN_GEN_CODE, IN_CLIENT_IP, " + NL +
            "       IN_CLIENT_TYPE, OUT_TIMESTAMP, IN_GUEST_ACCESS); " + NL +
            "END;"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Procedure INSERT_" + INTSESSION_TABLE_NAME + " created.");

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute(
            "CREATE SEQUENCE EXTSESS_ID_SEQ INCREMENT BY 1 START WITH 1 NOCYCLE"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Sequence EXTSESS_ID_SEQ created.");

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute(
            "create table " + EXTSESSION_TABLE_NAME + NL +
            "(" + NL +
            "   ID INTEGER NOT NULL," + NL +
            "   DOMAIN_ID INTEGER NOT NULL," + NL +
            "   INTERNAL_SESSION_ID INTEGER NOT NULL," + NL +
            "   GEN_CODE VARCHAR(" + EXTSESSION_GEN_CODE_MAXLENGTH + ") NOT NULL," + NL +
            "   SERVER VARCHAR(" + EXTSESSION_SERVER_MAXLENGTH + ") NOT NULL," + NL +
            "   CREATION_DATE TIMESTAMP(9) NOT NULL," + NL +
            "   CONSTRAINT BF_EXTSES_PK PRIMARY KEY (ID)," + NL +
            "   CONSTRAINT BF_EXTSES_UQ UNIQUE (GEN_CODE, SERVER)," + NL +
            "   CONSTRAINT BF_EXTSES_DID_FK FOREIGN KEY (DOMAIN_ID) " +  NL +
            "      REFERENCES " + DomainDatabaseSchema.DOMAIN_TABLE_NAME + " (ID) ON DELETE CASCADE," + NL +
            "   CONSTRAINT BF_EXTSES_FK FOREIGN KEY (INTERNAL_SESSION_ID) " + NL + 
            "      REFERENCES " + INTSESSION_TABLE_NAME + " (ID) ON DELETE CASCADE" + NL +
            ")"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Table " + EXTSESSION_TABLE_NAME + " created.");

         ///////////////////////////////////////////////////////////////////////

         if (stmQuery.execute(
            "CREATE OR REPLACE PROCEDURE INSERT_" + EXTSESSION_TABLE_NAME + NL +
            "(" + NL +
            "   IN_DOMAIN_ID IN INTEGER, " + NL +
            "   IN_INTERNAL_SESSION_ID IN INTEGER," + NL +
            "   IN_GEN_CODE IN VARCHAR," + NL +
            "   IN_SERVER IN VARCHAR," + NL +
            "   OUT_KEY OUT INTEGER," + NL +
            "   OUT_TIMESTAMP OUT TIMESTAMP" + NL +
            ") " + NL +
            "IS" + NL +
            "   NEW_OUT_KEY INTEGER; " + NL +
            "   NEW_OUT_TIMESTAMP TIMESTAMP(9); " + NL +
            "BEGIN " + NL +
            "   SELECT EXTSESS_ID_SEQ.NEXTVAL INTO NEW_OUT_KEY FROM DUAL; " + NL +
            "   OUT_KEY := NEW_OUT_KEY; " + NL +
            "   SELECT SYSDATE INTO NEW_OUT_TIMESTAMP FROM DUAL; " + NL +
            "   OUT_TIMESTAMP := NEW_OUT_TIMESTAMP; " + NL +
            "   INSERT INTO " + strUserName + "." + EXTSESSION_TABLE_NAME + "(ID, DOMAIN_ID, " + NL +
            "       INTERNAL_SESSION_ID, GEN_CODE, SERVER, CREATION_DATE)" + NL +
            "      VALUES (OUT_KEY, IN_DOMAIN_ID, IN_INTERNAL_SESSION_ID, IN_GEN_CODE,  " + NL +
            "      IN_SERVER, OUT_TIMESTAMP);" + NL +
            "END;"))
         {
            // Close any results
            stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
         }
         s_logger.log(Level.FINEST, "Procedure INSERT_" + EXTSESSION_TABLE_NAME + " created.");

         ///////////////////////////////////////////////////////////////////////

         // create all combined indexes used for speeding up retrieving data into the list
         createListIndexes(cntDBConnection);

         ///////////////////////////////////////////////////////////////////////
      }
      catch (SQLException sqleExc)
      {
         s_logger.log(Level.WARNING, 
                      "Failed to create schema " + SESSION_SCHEMA_NAME, sqleExc);
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
   public String getInsertInternalSession(
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();
      
      buffer.append("insert into " + INTSESSION_TABLE_NAME + "(ID, DOMAIN_ID, USER_ID, GEN_CODE, " +
                    "CLIENT_IP, CLIENT_TYPE, GUEST_ACCESS, CREATION_DATE) " +
                    "values (INTSESS_ID_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ");
      buffer.append(DatabaseImpl.getInstance().getSQLCurrentTimestampFunctionCall());
      buffer.append(")");

      return buffer.toString();
   }

   /**
    * {@inheritDoc}
    */
   public String getInsertInternalSessionAndFetchGeneratedValues(
   ) throws OSSException
   {
       return "{call INSERT_" + INTSESSION_TABLE_NAME + " (?, ?, ?, ?, ?, ?, ?, ?)}";
   }

   /**
    * {@inheritDoc}
    */
   public String getInsertExternalSessionAndFetchGeneratedValues(
   ) throws OSSException
   {
       return "{call INSERT_" + EXTSESSION_TABLE_NAME + " (?, ?, ?, ?, ?, ?)}";
   }
   
   /**
    * {@inheritDoc}
    */
   public String getInsertExternalSession(
   ) throws OSSException
   {
      StringBuffer buffer = new StringBuffer();
      
      buffer.append("insert into " + EXTSESSION_TABLE_NAME + "(ID, DOMAIN_ID, INTERNAL_SESSION_ID, " +
                    "GEN_CODE, SERVER, CREATION_DATE) " +
                    "values (EXTSESS_ID_SEQ.NEXTVAL, ?, ?, ?, ?, ");
      buffer.append(DatabaseImpl.getInstance().getSQLCurrentTimestampFunctionCall());
      buffer.append(")");

      return buffer.toString();
   }
}
