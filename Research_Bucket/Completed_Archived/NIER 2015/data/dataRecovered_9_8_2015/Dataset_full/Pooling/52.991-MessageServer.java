/*
 *  Copyright(c) 2002 Center for E-Commerce Infrastructure Development, The
 *  University of Hong Kong (HKU). All Rights Reserved.
 *
 *  This software is licensed under the Academic Free License Version 1.0
 *
 *  Academic Free License
 *  Version 1.0
 *
 *  This Academic Free License applies to any software and associated
 *  documentation (the "Software") whose owner (the "Licensor") has placed the
 *  statement "Licensed under the Academic Free License Version 1.0" immediately
 *  after the copyright notice that applies to the Software.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of the Software (1) to use, copy, modify, merge, publish, perform,
 *  distribute, sublicense, and/or sell copies of the Software, and to permit
 *  persons to whom the Software is furnished to do so, and (2) under patent
 *  claims owned or controlled by the Licensor that are embodied in the Software
 *  as furnished by the Licensor, to make, use, sell and offer for sale the
 *  Software and derivative works thereof, subject to the following conditions:
 *
 *  - Redistributions of the Software in source code form must retain all
 *  copyright notices in the Software as furnished by the Licensor, this list
 *  of conditions, and the following disclaimers.
 *  - Redistributions of the Software in executable form must reproduce all
 *  copyright notices in the Software as furnished by the Licensor, this list
 *  of conditions, and the following disclaimers in the documentation and/or
 *  other materials provided with the distribution.
 *  - Neither the names of Licensor, nor the names of any contributors to the
 *  Software, nor any of their trademarks or service marks, may be used to
 *  endorse or promote products derived from this Software without express
 *  prior written permission of the Licensor.
 *
 *  DISCLAIMERS: LICENSOR WARRANTS THAT THE COPYRIGHT IN AND TO THE SOFTWARE IS
 *  OWNED BY THE LICENSOR OR THAT THE SOFTWARE IS DISTRIBUTED BY LICENSOR UNDER
 *  A VALID CURRENT LICENSE. EXCEPT AS EXPRESSLY STATED IN THE IMMEDIATELY
 *  PRECEDING SENTENCE, THE SOFTWARE IS PROVIDED BY THE LICENSOR, CONTRIBUTORS
 *  AND COPYRIGHT OWNERS "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 *  LICENSOR, CONTRIBUTORS OR COPYRIGHT OWNERS BE LIABLE FOR ANY CLAIM, DAMAGES
 *  OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 *  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE.
 *
 *  This license is Copyright (C) 2002 Lawrence E. Rosen. All rights reserved.
 *  Permission is hereby granted to copy and distribute this license without
 *  modification. This license may not be modified without the express written
 *  permission of its copyright owner.
 */
/*
 *  =====
 *
 *  $Header: /cvsroot/sino/ebxmlms/src/hk/hku/cecid/phoenix/message/handler/MessageServer.java,v 1.150 2004/04/02 06:02:41 bobpykoon Exp $
 *
 *  Code authored by:
 *
 *  cyng [2002-03-21]
 *
 *  Code reviewed by:
 *
 *  username [YYYY-MM-DD]
 *
 *  Remarks:
 *
 *  =====
 */
package hk.hku.cecid.phoenix.message.handler;

import hk.hku.cecid.phoenix.common.util.Property;
import hk.hku.cecid.phoenix.message.packaging.Acknowledgment;
import hk.hku.cecid.phoenix.message.packaging.AttachmentDataSource;
import hk.hku.cecid.phoenix.message.packaging.EbxmlMessage;
import hk.hku.cecid.phoenix.message.packaging.ErrorList;
import hk.hku.cecid.phoenix.message.packaging.MessageHeader;
import hk.hku.cecid.phoenix.message.packaging.MessageOrder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.activation.DataSource;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import org.apache.log4j.Logger;

/**
 * A multithread-safe server that receives message requests from
 * <code>MessageServiceHandler</code> and stores the messages properly
 * in the <code>MessageStore</code>. This <code>MessageServer</code> is
 * aware of the <code>MessageServiceHandler</code> that it is acting on
 * behalf of.
 *
 * @author cyng
 * @version $Revision: 1.150 $
 */
public class MessageServer {

    static Logger logger = Logger.getLogger(MessageServer.class);

//    static Logger logger = Logger.getLogger(MessageServer.class.getName());

    // Note: the order of the following states are important.
    // Sent / received messages can be classified as follows:
    //
    // [RECEIVED_FROM_RANGE, RECEIVED_TO_RANGE] : messages received.
    //
    // [*, SENT_TO_RANGE] : messages sent.
    //
    // For states that overlaps in these two ranges, they are
    // loopback messages.

    final static int STATE_SENT_STARTED = 0;

    final static int STATE_ACKNOWLEDGED = -1;

    final static int STATE_SENT_FAILED = -2;

    final static int STATE_DELETED = -3;

    final static int STATE_SENT = -4;

    final static int STATE_SENT_RECEIVED = -5;

    final static int STATE_RECEIVED_ACKNOWLEDGED = -6;

    final static int STATE_RECEIVED = -7;

    final static int RECEIVED_FROM_RANGE = STATE_RECEIVED;

    final static int RECEIVED_TO_RANGE = STATE_SENT_RECEIVED;

    final static int SENT_FROM_RANGE = STATE_RECEIVED_ACKNOWLEDGED;

    final static int SENT_TO_RANGE = 99999999;

    private final static String SEPARATOR_MESSAGE_REPOSITORY =
            "messageRepository";

    private final static String SEPARATOR_OBJECT_STORE = "objectStore";

    private final static int MAX_FILE_NAME_LENGTH = 255;

    /*
     *  The organization of sequence number is as follows:
     *  + >=0            : sequence number in MessageOrder element, undelivered.
     *  + -9998 to -1    : this number, added by 9999, means the number of times
     *  an unordered message is undelivered. e.g. -9998 means
     *  the message is undelivered for one time and so on.
     *  + -9999          : unordered message is delivered.
     *  + <= -10000      : this number, added by 10000 and negated, means the
     *  sequence number of the message and it is delivered.
     *
     *  This organization of sequence number imples:
     *  + Messages having sequence number >= -9998 are undelivered.
     *  + Messages having sequence number <= -9999 are delivered.
     *  + Messages having sequence number -1 <= n <= -9999 are unordered
     *  messages. Others are ordered messages.
     *
     *  It should be noted that ordered messages must be used with
     *  once-and-only-once messaging, so it must be delivered once and only once
     *  and thus no counter is kept for its delivery.
     */
    // First message order sequence number that is undelivered
    // (including unordered messages)
    final static int FIRST_MESSAGE_ORDER_UNDELIVERED = 0;

    // First message order sequence number that is undelivered
    // (including unordered messages)
    final static int FIRST_MESSAGE_UNDELIVERED = -9998;

    // First message order sequence number that is delivered
    // (excluding unordered messages)
    final static int FIRST_MESSAGE_ORDER_DELIVERED = -10000;

    // First message order sequence number that is delivered
    // (including unordered messages)
    final static int FIRST_MESSAGE_DELIVERED = -9999;

    final static int MESSAGE_ORDER_DISABLED = FIRST_MESSAGE_UNDELIVERED;

    final static int MIN_MESSAGE_ORDER = 0;

    final static int MAX_MESSAGE_ORDER = 99999999;

    static DbConnectionPool dbConnectionPool;

    private static String messageRepository;

    private static String objectStore;

    private static String backupFile;

    private static String archiveDirectory;

    private static MessageServer messageServer = null;

    private static MessageFactory messageFactory;

    /**
     * Flag indicating if the class has been configured.
     */
    protected static boolean isConfigured = false;

    /**
     * Configure the class if and only if it has not been configured.
     *
     * @param prop <code>Property</code> object.
     * @throws InitializationException Description of the Exception
     */
    static synchronized void configure(Property prop)
             throws InitializationException {

        if (isConfigured) {
            return;
        }

        try {
            messageFactory = MessageFactory.newInstance();
        } catch (SOAPException e) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_SOAP_MESSAGE_FACTORY, e);
            logger.error(err);
            throw new InitializationException(err);
        }

        final String databaseDriver =
                prop.get(Constants.PROPERTY_DATABASE_DRIVER);
        if (databaseDriver == null) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_INIT_ERROR,
                    "Missing " + Constants.PROPERTY_DATABASE_DRIVER + " property");
            logger.error(err);
            throw new InitializationException(err);
        }
        final String databaseUser =
                prop.get(Constants.PROPERTY_DATABASE_USER);
        if (databaseUser == null) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_INIT_ERROR,
                    "Missing " + Constants.PROPERTY_DATABASE_USER + " property");
            logger.error(err);
            throw new InitializationException(err);
        }
        final String databasePassword =
                prop.get(Constants.PROPERTY_DATABASE_PASSWORD);
        if (databasePassword == null) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_INIT_ERROR,
                    "Missing " + Constants.PROPERTY_DATABASE_PASSWORD
                     + " property");
            logger.error(err);
            throw new InitializationException(err);
        }
        final String databaseURL = prop.get(Constants.PROPERTY_DATABASE_URL);
        if (databaseURL == null) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_INIT_ERROR,
                    "Missing " + Constants.PROPERTY_DATABASE_URL + " property");
            logger.error(err);
            throw new InitializationException(err);
        }
        String initialValue =
                prop.get(Constants.PROPERTY_DATABASE_INITIAL_CONNECTIONS);
        if (initialValue == null) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_INIT_ERROR,
                    "Missing " + Constants.PROPERTY_DATABASE_INITIAL_CONNECTIONS
                     + " property");
            logger.error(err);
            throw new InitializationException(err);
        }
        final int initialConnections;
        try {
            initialConnections = Integer.parseInt(initialValue);
        } catch (NumberFormatException nfe) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_INIT_ERROR, nfe,
                    "Invalid " + Constants.PROPERTY_DATABASE_INITIAL_CONNECTIONS
                     + " property");
            logger.error(err);
            throw new InitializationException(err);
        }
        initialValue = prop.get
                (Constants.PROPERTY_DATABASE_MAXIMUM_CONNECTIONS);
        if (initialValue == null) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_INIT_ERROR,
                    "Missing " + Constants.PROPERTY_DATABASE_MAXIMUM_CONNECTIONS
                     + " property");
            logger.error(err);
            throw new InitializationException(err);
        }
        final int maximumConnections;
        try {
            maximumConnections = Integer.parseInt(initialValue);
        } catch (NumberFormatException nfe) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_INIT_ERROR, nfe,
                    "Invalid " + Constants.PROPERTY_DATABASE_MAXIMUM_CONNECTIONS
                     + " property");
            logger.error(err);
            throw new InitializationException(err);
        }
        initialValue = prop.get(Constants.PROPERTY_DATABASE_MAXIMUM_WAIT);
        if (initialValue == null) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_INIT_ERROR,
                    "Missing " + Constants.PROPERTY_DATABASE_MAXIMUM_WAIT
                     + " property");
            logger.error(err);
            throw new InitializationException(err);
        }
        final int maximumWait;
        try {
            maximumWait = Integer.parseInt(initialValue);
        } catch (NumberFormatException nfe) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_INIT_ERROR, nfe,
                    "Invalid " + Constants.PROPERTY_DATABASE_MAXIMUM_WAIT
                     + " property");
            logger.error(err);
            throw new InitializationException(err);
        }
        initialValue = prop.get(Constants.PROPERTY_DATABASE_MAXIMUM_IDLE);
        if (initialValue == null) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_INIT_ERROR,
                    "Missing " + Constants.PROPERTY_DATABASE_MAXIMUM_IDLE
                     + " property");
            logger.error(err);
            throw new InitializationException(err);
        }
        final int maximumIdle;
        try {
            maximumIdle = Integer.parseInt(initialValue);
        } catch (NumberFormatException nfe) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_INIT_ERROR, nfe,
                    "Invalid " + Constants.PROPERTY_DATABASE_MAXIMUM_IDLE
                     + " property");
            logger.error(err);
            throw new InitializationException(err);
        }

        messageRepository = prop.get(Constants.PROPERTY_MESSAGE_REPOSITORY);
        if (messageRepository == null) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_INIT_ERROR,
                    "Missing " + Constants.PROPERTY_MESSAGE_REPOSITORY
                     + " property");
            logger.error(err);
            throw new InitializationException(err);
        }
        objectStore = prop.get
                (Constants.PROPERTY_MESSAGE_LISTENER_OBJECT_STORE);
        if (objectStore == null) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_INIT_ERROR,
                    "Missing " + Constants.PROPERTY_MESSAGE_LISTENER_OBJECT_STORE
                     + " property");
            logger.error(err);
            throw new InitializationException(err);
        }
        backupFile = prop.get(Constants.PROPERTY_MSH_BACKUP_FILE);
        if (backupFile == null) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_INIT_ERROR,
                    "Missing " + Constants.PROPERTY_MSH_BACKUP_FILE + " property");
            logger.error(err);
            throw new InitializationException(err);
        }
        archiveDirectory = prop.get(Constants.PROPERTY_MSH_ARCHIVE_DIRECTORY);
        if (archiveDirectory == null) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_INIT_ERROR,
                    "Missing " + Constants.PROPERTY_MSH_ARCHIVE_DIRECTORY
                     + " property");
            logger.error(err);
            throw new InitializationException(err);
        }

        IsolationLevelSelector.configure(prop);
        final String databaseTransactionIsolationLevel = prop.get(
                Constants.MSH_PROPERTY_DATABASE_TRANSACTION_ISOLATION_LEVEL);
        final int transactionIsolationLevel =
                IsolationLevelSelector.getIsolationLevel(
                databaseTransactionIsolationLevel);

        try {
            Class.forName(databaseDriver);
        } catch (ClassNotFoundException cnfe) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_DB_CANNOT_LOAD_DRIVER, cnfe, databaseDriver);
            logger.error(err);
            throw new InitializationException(err);
        }

        try {
            dbConnectionPool = new DbConnectionPool
                    (databaseURL, databaseUser, databasePassword,
                    initialConnections, maximumConnections, maximumWait,
                    maximumIdle, transactionIsolationLevel);
        } catch (DbConnectionPoolException e) {
            // error already logged
            throw new InitializationException(e.getMessage());
        }

        DirectoryManager.configure(prop);

        logger.info("Persistent service configuration:");
        logger.info("  DB driver: " + databaseDriver);
        logger.info("  DB user: " + databaseUser);
        logger.info("  DB password: <not shown>");
        logger.info("  DB URL: " + databaseURL);
        logger.info("  Pool initial connections: " + initialConnections);
        logger.info("  Pool maximum connections: " + maximumConnections);
        logger.info("  Pool access maximum wait: " + maximumWait);
        logger.info("  Pooled connection maximum idle time: " + maximumIdle);
        logger.info("  Message repository root: " + messageRepository);
        logger.info("  Message listener store: " + objectStore);
        logger.info("  Hermes backup file: " + backupFile);
        logger.info("  Hermes archive directory: " + archiveDirectory);
        logger.info("  Transaction isoloation level: "
                 + transactionIsolationLevel);

        // bootstrap the database
        try {
            checkAndInstallDBTable();
        } catch (MessageServerException e) {
            // error already logged
            throw new InitializationException(e.getMessage());
        }

        isConfigured = true;
    }

    /**
     *Constructor for the MessageServer object
     *
     * @throws MessageServerException Description of the Exception
     */
    private MessageServer() throws MessageServerException {

        logger.debug("=> MessageServer.MessageServer");

        if (messageFactory == null) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_SOAP_MESSAGE_FACTORY);
            throw new MessageServerException(err);
        }

        logger.debug("<= MessageServer.MessageServer");
    }

    /**
     * Gets the instance attribute of the MessageServer class
     *
     * @return The instance value
     * @throws MessageServerException Description of the Exception
     */
    static MessageServer getInstance() throws MessageServerException {

        if (messageServer == null) {
            synchronized (MessageServer.class) {
                if (messageServer == null) {
                    messageServer = new MessageServer();
                }
            }
        }
        return messageServer;
    }

    /**
     * Description of the Method
     *
     * @param startFile Description of the Parameter
     * @param zos Description of the Parameter
     * @throws IOException Description of the Exception
     */
    private void backupFiles(File startFile, ZipOutputStream zos)
             throws IOException {

        if (startFile.isDirectory()) {
            final File[] subDirectories = startFile.listFiles();
            for (int i = 0; i < subDirectories.length; i++) {
                backupFiles(subDirectories[i], zos);
            }
        } else if (startFile.isFile()) {
            String startFilePath = null;
            try {
                final String canonicalRepository = new File(messageRepository).
                        getCanonicalPath().toLowerCase();
                final String canonicalStore = new File(objectStore).
                        getCanonicalPath().toLowerCase();
                startFilePath = startFile.getCanonicalPath();
                String relativePath = startFilePath;
                if (relativePath.toLowerCase()
                        .startsWith(canonicalRepository)) {
                    relativePath = SEPARATOR_MESSAGE_REPOSITORY + File.separator
                             + relativePath.substring(canonicalRepository.length()
                             + 1);
                } else if (relativePath.toLowerCase()
                        .startsWith(canonicalStore)) {
                    relativePath = SEPARATOR_OBJECT_STORE + File.separator
                             + relativePath.substring(canonicalStore.length() + 1);
                }
                zos.putNextEntry(new ZipEntry(relativePath));
                final FileInputStream fis = new FileInputStream(startFile);
                for (int c = fis.read(); c != -1; c = fis.read()) {
                    zos.write(c);
                }
                fis.close();
                zos.closeEntry();
            } catch (IOException e) {
                String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_FILE_IO_ERROR, e, startFilePath);
                logger.error(err);
                throw e;
            } catch (Exception e) {
                String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
                logger.error(err);
                throw new IOException(err);
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param zis Description of the Parameter
     * @throws IOException Description of the Exception
     */
    private void restoreFiles(ZipInputStream zis) throws IOException {

        File startFile = new File(messageRepository);
        if (startFile.exists() == false) {
            startFile.mkdirs();
        }
        startFile = new File(objectStore);
        if (startFile.exists() == false) {
            startFile.mkdirs();
        }

        try {
            for (ZipEntry entry = zis.getNextEntry(); entry != null;
                    entry = zis.getNextEntry()) {
                String entryName = entry.getName();
                final String restoreDir;
                if (entryName.startsWith(SEPARATOR_MESSAGE_REPOSITORY)) {
                    restoreDir = messageRepository;
                    entryName = entryName.substring
                            (SEPARATOR_MESSAGE_REPOSITORY.length() + 1);
                } else if (entryName.startsWith(SEPARATOR_OBJECT_STORE)) {
                    restoreDir = objectStore;
                    entryName = entryName.substring
                            (SEPARATOR_OBJECT_STORE.length() + 1);
                } else {
                    restoreDir = "";
                }

                int index = entryName.lastIndexOf(File.separatorChar);
                if (index != -1) {
                    final File dir = new File(restoreDir + File.separator
                             + entryName.substring(0, index));
                    if (dir.exists() == false) {
                        dir.mkdirs();
                    }
                }

                final FileOutputStream fos = new FileOutputStream
                        (restoreDir + File.separator + entryName);
                for (int c = zis.read(); c != -1; c = zis.read()) {
                    fos.write(c);
                }
                fos.close();
            }
        } catch (IOException e) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_FILE_IO_ERROR, e);
            logger.error(err);
            throw new IOException(err);
        } catch (Exception e) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new IOException(err);
        }
    }

    /**
     * Description of the Method
     *
     * @throws MessageServerException Description of the Exception
     */
    void shutDown() throws MessageServerException {
        logger.debug("=> MessageServer.shutDown");

        try {
            dbConnectionPool.shutDown();
        } catch (DbConnectionPoolException e) {
            // error already logged
            throw new MessageServerException(e.getMessage());
        }

        logger.debug("<= MessageServer.shutDown");
    }

    /**
     * Description of the Method
     *
     * @param startDate Description of the Parameter
     * @param endDate Description of the Parameter
     * @param appContext Description of the Parameter
     * @return Description of the Return Value
     * @throws MessageServerException Description of the Exception
     */
    Map[] archive(Date startDate, Date endDate,
            ApplicationContext[] appContext)
             throws MessageServerException {

        logger.debug(
                "=> MessageServer.archive(Date, Date, ApplicationContext[])");

        Connection connection = null;
        MessageServerException exception = null;
        Map[] resultMap = null;
        try {
            connection = dbConnectionPool.getConnection();
            File srcDir = new File(messageRepository);
            File destDir = new File(archiveDirectory);
            Set filenameSet = new HashSet();
            resultMap = Export.archiveDatabase(connection, srcDir, destDir,
                    startDate, endDate, appContext, filenameSet);
            Export.archiveRepository(connection, srcDir, destDir, filenameSet);
        } catch (ExportException e) {
            // error already logged
            exception = new MessageServerException(e.getMessage());
        } catch (Exception e) {
            String err = ErrorMessages.getMessage
                    (ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            exception = new MessageServerException(err);
        } finally {
            if (connection != null) {
                if (exception == null) {
                    try {
                        connection.commit();
                    } catch (SQLException e) {
                        String err = ErrorMessages.getMessage(
                                ErrorMessages.ERR_DB_CANNOT_COMMIT_CHANGE, e,
                                "archive flag");
                        logger.error(err);
                        exception = new MessageServerException(err);
                        try {
                            connection.rollback();
                        } catch (SQLException e2) {}
                        ;
                    }
                }
                try {
                    dbConnectionPool.freeConnection
                            (connection, exception == null);
                } catch (DbConnectionPoolException e) {}
                ;
            }
            logger.debug(
                    "<= MessageServer.archive(Date, Date, ApplicationContext [])");
            if (exception != null) {
                throw exception;
            }
        }
        return resultMap;
    }

    /**
     * Description of the Method
     *
     * @param startDate Description of the Parameter
     * @param endDate Description of the Parameter
     * @return Description of the Return Value
     * @throws MessageServerException Description of the Exception
     */
    Map[] archive(Date startDate, Date endDate)
             throws MessageServerException {

        logger.debug("=> MessageServer.archive(Date, Date)");

        Connection connection = null;
        MessageServerException exception = null;
        Map[] resultMap = null;
        try {
            connection = dbConnectionPool.getConnection();
            File srcDir = new File(messageRepository);
            File destDir = new File(archiveDirectory);
            Set filenameSet = new HashSet();
            resultMap = Export.archiveDatabase(connection, srcDir, destDir,
                    startDate, endDate, filenameSet);
            Export.archiveRepository(connection, srcDir, destDir, filenameSet);
        } catch (ExportException e) {
            // error already logged
            exception = new MessageServerException(e.getMessage());
        } catch (Exception e) {
            String err = ErrorMessages.getMessage
                    (ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            exception = new MessageServerException(err);
        } finally {
            if (connection != null) {
                if (exception == null) {
                    try {
                        connection.commit();
                    } catch (SQLException e) {
                        String err = ErrorMessages.getMessage(
                                ErrorMessages.ERR_DB_CANNOT_COMMIT_CHANGE, e,
                                "archive flag");
                        logger.error(err);
                        exception = new MessageServerException(err);
                        try {
                            connection.rollback();
                        } catch (SQLException e2) {}
                        ;
                    }
                }
                try {
                    dbConnectionPool.freeConnection
                            (connection, exception == null);
                } catch (DbConnectionPoolException e) {}
                ;
            }
            logger.debug("<= MessageServer.archive(Date, Date)");
            if (exception != null) {
                throw exception;
            }
        }
        return resultMap;
    }

    /**
     * Description of the Method
     *
     * @param appContexts Description of the Parameter
     * @return Description of the Return Value
     * @throws MessageServerException Description of the Exception
     */
    Map[] archive(ApplicationContext[] appContexts)
             throws MessageServerException {

        logger.debug("=> MessageServer.archive(ApplicationContext [])");

        Connection connection = null;
        MessageServerException exception = null;
        Map[] resultMap = null;
        try {
            connection = dbConnectionPool.getConnection();
            File srcDir = new File(messageRepository);
            File destDir = new File(archiveDirectory);
            Set filenameSet = new HashSet();
            resultMap = Export.archiveDatabase(connection, srcDir, destDir,
                    appContexts, filenameSet);
            Export.archiveRepository(connection, srcDir, destDir, filenameSet);
        } catch (ExportException e) {
            // error already logged
            exception = new MessageServerException(e.getMessage());
        } catch (Exception e) {
            String err = ErrorMessages.getMessage
                    (ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            exception = new MessageServerException(err);
        } finally {
            if (connection != null) {
                if (exception == null) {
                    try {
                        connection.commit();
                    } catch (SQLException e) {
                        String err = ErrorMessages.getMessage(
                                ErrorMessages.ERR_DB_CANNOT_COMMIT_CHANGE, e,
                                "archive flag");
                        logger.error(err);
                        exception = new MessageServerException(err);
                        try {
                            connection.rollback();
                        } catch (SQLException e2) {}
                        ;
                    }
                }
                try {
                    dbConnectionPool.freeConnection
                            (connection, exception == null);
                } catch (DbConnectionPoolException e) {}
                ;
            }
            logger.debug("<= MessageServer.archive");
            if (exception != null) {
                throw exception;
            }
        }
        return resultMap;
    }

    /**
     * Description of the Method
     *
     * @throws MessageServerException Description of the Exception
     */
    void backup() throws MessageServerException {

        logger.debug("=> MessageServer.backup");

        final Connection connection = dbConnectionPool.getConnection();
        final ZipOutputStream zos;
        try {
            zos = new ZipOutputStream(new FileOutputStream(backupFile));
        } catch (FileNotFoundException fnfe) {
            dbConnectionPool.freeConnection(connection, true);
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_FILE_IO_ERROR, fnfe, backupFile);
            logger.error(err);
            throw new MessageServerException(err);
        }
        zos.setLevel(9);
        Exception exception = null;
        try {
            DbTableManager.backup(connection, zos,
                    DbTableManager.getTableList(), DbTableManager.getDDLList());
        } catch (DbTableManagerException dtme) {
            // error already logged
            exception = dtme;
        } finally {
            dbConnectionPool.freeConnection(connection, exception == null);
            if (exception != null) {
                try {
                    zos.close();
                } catch (IOException ioe) {}
                throw (DbTableManagerException) exception;
            }
        }

        exception = null;
        PersistenceHandler repositoryHandler
                = PersistenceManager.getRepositoryPersistenceHandler();
        PersistenceHandler objectStoreHandler
                = PersistenceManager.getObjectStorePersistenceHandler();
        try {
            if (!(repositoryHandler instanceof BackupablePersistenceHandler)) {
                throw new IOException("backup not supported on "
                        + "Repository Persistence handler");
            }
            BackupablePersistenceHandler backupRepositoryHandler
                    = (BackupablePersistenceHandler) repositoryHandler;
            backupRepositoryHandler.setBackupDirectory(
                    SEPARATOR_MESSAGE_REPOSITORY);
            backupRepositoryHandler.backup(zos);
            if (!(objectStoreHandler instanceof BackupablePersistenceHandler)) {
                throw new IOException("backup not supported on "
                        + "Object store Persistence handler");
            }
            BackupablePersistenceHandler backupObjectStoreHandler
                    = (BackupablePersistenceHandler) objectStoreHandler;
            backupObjectStoreHandler.setBackupDirectory(SEPARATOR_OBJECT_STORE);
            backupObjectStoreHandler.backup(zos);
        } catch (IOException ioe) {
            // error already logged
            logger.error(exception.getMessage());
            exception = ioe;
        } finally {
            try {
                zos.close();
            } catch (IOException ioe) {}

            if (exception != null) {
                throw new MessageServerException(exception.getMessage());
            }
        }
        /*
        try {
            backupFiles(new File(messageRepository), zos);
            backupFiles(new File(objectStore), zos);
        } catch (IOException ioe) {
            // error already logged
            exception = ioe;
        } finally {
            try {
                zos.close();
            } catch (IOException ioe) {}

            if (exception != null) {
                throw new MessageServerException(exception.getMessage());
            }
        }
        */

        logger.debug("<= MessageServer.backup");
    }

    /**
     * Description of the Method
     *
     * @throws MessageServerException Description of the Exception
     */
    void restore() throws MessageServerException {

        logger.debug("=> MessageServer.restore");

        final Connection connection = dbConnectionPool.getConnection();
        final ZipInputStream zis;
        try {
            zis = new ZipInputStream(new FileInputStream(backupFile));
        } catch (FileNotFoundException fnfe) {
            dbConnectionPool.freeConnection(connection, true);
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_FILE_IO_ERROR, fnfe, backupFile);
            logger.error(err);
            throw new MessageServerException(err);
        }
        Exception exception = null;
        try {
            DbTableManager.restore(connection, zis,
                    DbTableManager.getTableList());
        } catch (DbTableManagerException dtme) {
            // log already logged
            exception = dtme;
        } finally {
            dbConnectionPool.freeConnection(connection, exception == null);
            if (exception != null) {
                throw (DbTableManagerException) exception;
            }
        }

        exception = null;
        PersistenceHandler repositoryHandler
                = PersistenceManager.getRepositoryPersistenceHandler();
        PersistenceHandler objectStoreHandler
                = PersistenceManager.getObjectStorePersistenceHandler();
        try {
            if (!(repositoryHandler instanceof BackupablePersistenceHandler)) {
                throw new IOException("backup not supported on "
                        + "Repository Persistence handler");
            }
            BackupablePersistenceHandler backupRepositoryHandler
                    = (BackupablePersistenceHandler) repositoryHandler;
            backupRepositoryHandler.setBackupDirectory(
                    SEPARATOR_MESSAGE_REPOSITORY);
            backupRepositoryHandler.restore(zis);
            if (!(objectStoreHandler instanceof BackupablePersistenceHandler)) {
                throw new IOException("backup not supported on "
                        + "Object store Persistence handler");
            }
            BackupablePersistenceHandler backupObjectStoreHandler
                    = (BackupablePersistenceHandler) objectStoreHandler;
            backupObjectStoreHandler.setBackupDirectory(SEPARATOR_OBJECT_STORE);
            backupObjectStoreHandler.restore(zis);
        } catch (IOException ioe) {
            // error already logged
            logger.error(exception.getMessage());
            exception = ioe;
        } finally {
            try {
                zis.close();
            } catch (IOException ioe) {}

            if (exception != null) {
                throw new MessageServerException(exception.getMessage());
            }
        }
        /*
        try {
            restoreFiles(zis);
        } catch (IOException ioe) {
            // error already logged
            exception = ioe;
        } finally {
            try {
                zis.close();
            } catch (IOException ioe) {}

            if (exception != null) {
                throw new MessageServerException(exception.getMessage());
            }
        }
        */
        logger.debug("<= MessageServer.restore");
    }

    /**
     * Description of the Method
     *
     * @param refToMessageId Description of the Parameter
     * @param ebxmlMessage Description of the Parameter
     * @param appContext Description of the Parameter
     * @param tx Description of the Parameter
     * @return Description of the Return Value
     * @throws MessageServerException Description of the Exception
     */
    boolean ackReceived(String refToMessageId, EbxmlMessage ebxmlMessage,
            ApplicationContext appContext, Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.ackReceived");

        tx.lock(refToMessageId);
        int state = STATE_SENT;
        PreparedStatement query = null;
        ResultSet resultSet = null;
        Exception exception = null;
        try {
            query = DbTableManager.DBTABLE_MESSAGE_STORE.getSelectQuery
                    (tx.getConnection(),
                    new String[]{DbTableManager.ATTRIBUTE_STATE},
                    new String[]{DbTableManager.ATTRIBUTE_MESSAGE_ID},
                    new int[]{DbTableManager.EQUAL},
                    new String[]{refToMessageId});
            resultSet = query.executeQuery();
            if (!resultSet.next()) {
                String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_DB_CANNOT_FIND_MESSAGE,
                        "id: <" + refToMessageId + "> in <"
                         + DbTableManager.TABLE_MESSAGE_STORE + ">");
                logger.warn(err);
                throw new MessageServerException(err);
            }
            state = resultSet.getInt(DbTableManager.ATTRIBUTE_STATE);

            if (resultSet.next()) {
                String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_DB_DATA_INCONSISTENT,
                        "more than one message with id: <" + refToMessageId
                         + "> in <" + DbTableManager.TABLE_MESSAGE_STORE + ">");
                logger.warn(err);
                throw new MessageServerException(err);
            }
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException sqle) {}
            }
            if (query != null) {
                try {
                    query.close();
                } catch (SQLException sqle) {}
            }

            if (exception != null) {
                if (exception instanceof MessageServerException) {
                    logger.debug("<= MessageServer.ackReceived");
                    return false;
                } else {
                    throw new MessageServerException(exception.getMessage());
                }
            }
        }

        if (state == STATE_ACKNOWLEDGED ||
                state == STATE_RECEIVED_ACKNOWLEDGED) {
            logger.debug("<= MessageServer.ackReceived");
            return true;
        } else if (state == STATE_DELETED) {
            logger.warn("acknowledgment received for a deleted message.");
            logger.debug("<= MessageServer.ackReceived");
            return true;
        }

        final int targetState =
                ((state == STATE_RECEIVED || state == STATE_SENT_RECEIVED ||
                state == STATE_RECEIVED_ACKNOWLEDGED) ?
                STATE_RECEIVED_ACKNOWLEDGED : STATE_ACKNOWLEDGED);
        PreparedStatement update = null;
        PreparedStatement sendingStateUpdate = null;
        exception = null;
        try {
            store(ebxmlMessage, appContext, STATE_RECEIVED,
                    FIRST_MESSAGE_DELIVERED, tx);
            update = DbTableManager.DBTABLE_MESSAGE_STORE.getUpdateQuery
                    (tx.getConnection(),
                    new String[]{DbTableManager.ATTRIBUTE_STATE},
                    new String[]{String.valueOf(targetState)},
                    new String[]{DbTableManager.ATTRIBUTE_MESSAGE_ID},
                    new int[]{DbTableManager.EQUAL},
                    new String[]{refToMessageId});
            sendingStateUpdate = tx.prepareStatement("DELETE FROM "
                     + DbTableManager.DBTABLE_SENDING_STATE.getName()
                     + " WHERE " + DbTableManager.ATTRIBUTE_MESSAGE_ID + "='"
                     + refToMessageId + "'");
            logger.debug("set acknowledgment to state <"
                     + getStateName(STATE_RECEIVED) + "> and acknowledged"
                     + " message to <" + getStateName(targetState) + ">");
            tx.executeUpdate(
                    new PreparedStatement[]{update, sendingStateUpdate});
        } catch (TransactionException e) {
            exception = e;
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            if (update != null) {
                try {
                    update.close();
                } catch (SQLException sqle) {}
            }
            if (sendingStateUpdate != null) {
                try {
                    sendingStateUpdate.close();
                } catch (SQLException sqle) {}
            }

            if (exception != null &&
                    (exception instanceof MessageServerException)) {
                throw (MessageServerException) exception;
            }

            if (exception != null) {
                throw new MessageServerException(exception.getMessage());
            }
            logger.debug("<= MessageServer.ackReceived");
        }
        return true;
    }

    /**
     * Gets the refToMessage attribute of the MessageServer object
     *
     * @param refToMessageId Description of the Parameter
     * @param tx Description of the Parameter
     * @return The refToMessage value
     * @throws MessageServerException Description of the Exception
     */
    EbxmlMessage getRefToMessage(String refToMessageId, Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.MessageServer");

        PreparedStatement query = null;
        ResultSet resultSet = null;
        Exception exception = null;
        EbxmlMessage refToMessage = null;
        try {
            query = DbTableManager.DBTABLE_REF_TO_MESSAGE.getSelectQuery
                    (tx.getConnection(), null,
                    new String[]{DbTableManager.ATTRIBUTE_REF_TO_MESSAGE_ID},
                    new int[]{DbTableManager.EQUAL},
                    new String[]{refToMessageId});
            resultSet = query.executeQuery();
            if (!resultSet.next()) {
                String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_DB_CANNOT_FIND_MESSAGE,
                        "id: <" + refToMessageId + "> in <"
                         + DbTableManager.TABLE_REF_TO_MESSAGE + ">");
                logger.warn(err);
                throw new MessageServerException("");
            }
            final String messageId = resultSet.getString
                    (DbTableManager.ATTRIBUTE_MESSAGE_ID);

            if (resultSet.next()) {
                String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_DB_DATA_INCONSISTENT,
                        "more than one message with id: <" + refToMessageId
                         + "> in <" + DbTableManager.TABLE_REF_TO_MESSAGE + ">");
                logger.warn(err);
                throw new MessageServerException(err);
            }
            resultSet.close();
            query.close();

            query =
                    DbTableManager.DBTABLE_MESSAGE_STORE.getSelectQuery(tx.getConnection(),
                    new String[]{DbTableManager.ATTRIBUTE_FILE_NAME},
                    new String[]{DbTableManager.ATTRIBUTE_MESSAGE_ID},
                    new int[]{DbTableManager.EQUAL},
                    new String[]{messageId}
                    );

            resultSet = query.executeQuery();
            resultSet.next();
            String fileName = resultSet.getString
                    (DbTableManager.ATTRIBUTE_FILE_NAME);
            /*
            fileName = fileName.replace('\\', File.separatorChar);
            fileName = fileName.replace('/', File.separatorChar);
            final File pathName = new File(messageRepository + File.separator
                     + fileName);
            if (!pathName.exists()) {
                String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_DB_DATA_INCONSISTENT,
                        "file for message with reftomessageid <" + refToMessageId
                         + "> does not exist");
                logger.warn(err);
                throw new MessageServerException(err);
            }
            logger.debug("get the old ack message from file system");

            refToMessage = getMessageFromFile(pathName);
            */
            PersistenceHandler handler
                    = PersistenceManager.getRepositoryPersistenceHandler();
            refToMessage = (EbxmlMessage) getMessageFromDataSource(
                    handler.getObject(fileName), true);;
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException sqle) {}
            }
            if (query != null) {
                try {
                    query.close();
                } catch (SQLException sqle) {}
            }

            if (exception != null &&
                    ((exception instanceof MessageServerException) == false ||
                    exception.getMessage().equals("") == false)) {
                throw new MessageServerException(exception.getMessage());
            }
        }
        return refToMessage;
    }

    /**
     * <p>Set the delivery status of the message. If the message does not
     * contain messageOrder element, it simply marks the database so that it is
     * delivered once more / less. </p>
     * <p>If the message is ordered, then the state is simply set according
     * to the <code>isDelivered</code> flag. The resulting database is always
     * consistent.</p>
     *
     * @param messageId Message ID of the <code>EbxmlMessage</code>
     * @param isDelivered true if the message should be marked as delivered;
     *                      false otherwise.
     * @param tx The new deliveryStatus value
     * @return New sequence number.
     * @throws MessageServerException
     */
    int setDeliveryStatus(String messageId, boolean isDelivered, Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.setDeliveryStatus");

        PreparedStatement query = null;
        PreparedStatement update = null;
        ResultSet resultSet = null;
        Exception exception = null;
        int newSeqNo = 0;
        try {
            tx.lock(messageId);
            query = DbTableManager.DBTABLE_MESSAGE_STORE.getSelectQuery
                    (tx.getConnection(),
                    new String[]{DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER},
                    new String[]{DbTableManager.ATTRIBUTE_MESSAGE_ID},
                    new int[]{DbTableManager.EQUAL}, new String[]{messageId});
            resultSet = query.executeQuery();
            if (!resultSet.next()) {
                String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_DB_CANNOT_FIND_MESSAGE,
                        "id: <" + messageId + "> in <"
                         + DbTableManager.TABLE_MESSAGE_STORE + ">");
                logger.warn(err);
                throw new MessageServerException(err);
            }

            final int seqNo = resultSet.getInt(1);

            if (seqNo >= FIRST_MESSAGE_DELIVERED &&
                    seqNo < FIRST_MESSAGE_ORDER_UNDELIVERED) {
                if (isDelivered) {
                    if (seqNo == FIRST_MESSAGE_DELIVERED) {
                        logger.info("messageId=<" + messageId +
                                "> is delivered again");
                        newSeqNo = seqNo;
                        throw new MessageServerException("");
                    } else {
                        newSeqNo = seqNo - 1;
                    }
                } else {
                    newSeqNo = seqNo + 1;
                }
            } else {
                if (seqNo >= FIRST_MESSAGE_ORDER_UNDELIVERED && isDelivered) {
                    newSeqNo = FIRST_MESSAGE_ORDER_DELIVERED - seqNo;
                } else if (seqNo <= FIRST_MESSAGE_ORDER_DELIVERED &&
                        !isDelivered) {
                    newSeqNo = FIRST_MESSAGE_ORDER_DELIVERED - seqNo;
                } else {
                    // Don't need to update the sequence number
                    newSeqNo = seqNo;
                    throw new MessageServerException("");
                }
            }

            update = DbTableManager.DBTABLE_MESSAGE_STORE.getUpdateQuery
                    (tx.getConnection(),
                    new String[]{DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER},
                    new String[]{String.valueOf(newSeqNo)},
                    new String[]{DbTableManager.ATTRIBUTE_MESSAGE_ID},
                    new int[]{DbTableManager.EQUAL}, new String[]{messageId});
            tx.executeUpdate(new PreparedStatement[]{update});
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException sqle) {}
            }
            if (query != null) {
                try {
                    query.close();
                } catch (SQLException sqle) {}
            }
            if (update != null) {
                try {
                    update.close();
                } catch (SQLException sqle) {}
            }

            if (exception != null &&
                    exception instanceof MessageServerException &&
                    exception.getMessage().equals("") == false) {
                throw (MessageServerException) exception;
            }

            if (exception != null &&
                    (exception instanceof MessageServerException) == false) {
                throw new MessageServerException(exception.getMessage());
            }

            logger.debug("<= MessageServer.setDeliveryStatus");
        }
        return newSeqNo;
    }

    /**
     * <p>Set the delivery status of the message. If the message does not
     * contain messageOrder element, it simply marks the database so that it is
     * delivered once more / less.</p>
     *
     * <p>If the message is ordered, then the state is simply set according
     * to the <code>isDelivered</code> flag. The resulting database is always
     * consistent.</p>
     *
     * @param filename name of the file in which the message is stored.
     * @param isDelivered true if the message should be marked as delivered;
     *                      false otherwise.
     * @param tx The new fileDeliveryStatus value
     * @return Description of the Return Value
     * @throws MessageServerException
     */
    int setFileDeliveryStatus(String filename, boolean isDelivered,
            Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.setFileDeliveryStatus");

        PreparedStatement query = null;
        PreparedStatement update = null;
        ResultSet resultSet = null;
        Exception exception = null;
        int newSeqNo = 0;
        try {
            query = DbTableManager.DBTABLE_MESSAGE_STORE.getSelectQuery
                    (tx.getConnection(),
                    new String[]{DbTableManager.ATTRIBUTE_MESSAGE_ID,
                    DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER},
                    new String[]{DbTableManager.ATTRIBUTE_FILE_NAME},
                    new int[]{DbTableManager.EQUAL}, new String[]{filename});
            resultSet = query.executeQuery();
            if (!resultSet.next()) {
                String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_DB_CANNOT_FIND_MESSAGE,
                        "filename: <" + filename + "> in <"
                         + DbTableManager.TABLE_MESSAGE_STORE + ">");
                logger.warn(err);
                throw new MessageServerException(err);
            }

            final String messageId = resultSet.getString(1);
            final int seqNo = resultSet.getInt(2);

            if (seqNo >= FIRST_MESSAGE_DELIVERED &&
                    seqNo < FIRST_MESSAGE_ORDER_UNDELIVERED) {
                if (isDelivered) {
                    if (seqNo == FIRST_MESSAGE_DELIVERED) {
                        logger.info("filename=<" + filename +
                                "> is delivered again");
                        newSeqNo = seqNo;
                        throw new MessageServerException("");
                    } else {
                        newSeqNo = seqNo - 1;
                    }
                } else {
                    newSeqNo = seqNo + 1;
                }
            } else {
                if (seqNo >= FIRST_MESSAGE_ORDER_UNDELIVERED && isDelivered) {
                    newSeqNo = FIRST_MESSAGE_ORDER_DELIVERED - seqNo;
                } else if (seqNo <= FIRST_MESSAGE_ORDER_DELIVERED &&
                        !isDelivered) {
                    newSeqNo = FIRST_MESSAGE_ORDER_DELIVERED - seqNo;
                } else {
                    // Don't need to update the sequence number
                    newSeqNo = seqNo;
                    throw new MessageServerException("");
                }
            }

            update =
                    DbTableManager.DBTABLE_MESSAGE_STORE.getUpdateQuery(tx.getConnection(),
                    new String[]{DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER},
                    new String[]{String.valueOf(newSeqNo)},
                    new String[]{DbTableManager.ATTRIBUTE_MESSAGE_ID},
                    new int[]{DbTableManager.EQUAL},
                    new String[]{messageId}
                    );
            tx.executeUpdate(new PreparedStatement[]{update});
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException sqle) {}
            }
            if (query != null) {
                try {
                    query.close();
                } catch (SQLException sqle) {}
            }
            if (update != null) {
                try {
                    update.close();
                } catch (SQLException sqle) {}
            }

            if (exception != null &&
                    exception instanceof MessageServerException &&
                    exception.getMessage().equals("") == false) {
                throw (MessageServerException) exception;
            }

            if (exception != null &&
                    (exception instanceof MessageServerException) == false) {
                throw new MessageServerException(exception.getMessage());
            }

            logger.debug("<= MessageServer.setFileDeliveryStatus");
        }
        return newSeqNo;
    }

    /**
     * Description of the Method
     *
     * @param messageId Description of the Parameter
     * @param appContext Description of the Parameter
     * @param tx Description of the Parameter
     * @return Description of the Return Value
     * @throws MessageServerException Description of the Exception
     */
    boolean hasDelivered(String messageId, ApplicationContext appContext,
            Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.hasDelivered");

        PreparedStatement query = null;
        ResultSet resultSet = null;
        Exception exception = null;
        boolean delivered = false;
        try {
            query = DbTableManager.DBTABLE_MESSAGE_STORE.getSelectQuery
                    (tx.getConnection(),
                    new String[]{DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER},
                    new String[]{DbTableManager.ATTRIBUTE_CPA_ID,
                    DbTableManager.ATTRIBUTE_CONVERSATION_ID,
                    DbTableManager.ATTRIBUTE_SERVICE,
                    DbTableManager.ATTRIBUTE_ACTION,
                    DbTableManager.ATTRIBUTE_MESSAGE_ID},
                    new int[]{DbTableManager.EQUAL, DbTableManager.EQUAL,
                    DbTableManager.EQUAL, DbTableManager.EQUAL,
                    DbTableManager.EQUAL},
                    new String[]{appContext.getCpaId(),
                    appContext.getConversationId(), appContext.getService(),
                    appContext.getAction(), messageId});
            resultSet = query.executeQuery();
            resultSet.next();
            int seqNo = resultSet.getInt(1);
            delivered = (seqNo <= FIRST_MESSAGE_DELIVERED);
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException sqle) {}
            }
            if (query != null) {
                try {
                    query.close();
                } catch (SQLException sqle) {}
            }

            if (exception != null) {
                // error already logged
                throw new MessageServerException(exception.getMessage());
            }

            logger.debug("<= MessageServer.hasDelivered");
        }
        return delivered;
    }

    /**
     * Description of the Method
     *
     * @param ebxmlMessage Description of the Parameter
     * @param appContext Description of the Parameter
     * @param tx Description of the Parameter
     * @return Description of the Return Value
     * @throws MessageServerException Description of the Exception
     */
    boolean hasReceived(EbxmlMessage ebxmlMessage,
            ApplicationContext appContext, Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.hasReceived");

        String messageId = ebxmlMessage.getMessageId();
        PreparedStatement query = null;
        String fileName = null;
        ResultSet resultSet = null;
        Exception exception = null;
        boolean received = true;
        try {
            query = DbTableManager.DBTABLE_MESSAGE_STORE.getSelectQuery
                    (tx.getConnection(),
                    new String[]{DbTableManager.ATTRIBUTE_FILE_NAME,
                    DbTableManager.ATTRIBUTE_STATE},
                    new String[]{DbTableManager.ATTRIBUTE_CPA_ID,
                    DbTableManager.ATTRIBUTE_CONVERSATION_ID,
                    DbTableManager.ATTRIBUTE_SERVICE,
                    DbTableManager.ATTRIBUTE_ACTION,
                    DbTableManager.ATTRIBUTE_MESSAGE_ID},
                    new int[]{DbTableManager.EQUAL, DbTableManager.EQUAL,
                    DbTableManager.EQUAL, DbTableManager.EQUAL,
                    DbTableManager.EQUAL},
                    new String[]{appContext.getCpaId(),
                    appContext.getConversationId(),
                    appContext.getService(),
                    appContext.getAction(), messageId});
            resultSet = query.executeQuery();

            if (resultSet.next()) {
                fileName = resultSet.getString(1);
                final int state = resultSet.getInt(2);
                if (state != STATE_RECEIVED &&
                        state != STATE_RECEIVED_ACKNOWLEDGED) {
                    throw new MessageServerException("");
                }
                if (resultSet.next()) {
                    String err = ErrorMessages.getMessage(
                            ErrorMessages.ERR_DB_DATA_INCONSISTENT,
                            "more than one message with id: <" + messageId
                             + "> in <" + DbTableManager.TABLE_MESSAGE_STORE + ">");
                    logger.warn(err);
                    throw new MessageServerException(err);
                }
            } else {
                throw new MessageServerException("");
            }
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException sqle) {}
            }
            if (query != null) {
                try {
                    query.close();
                } catch (SQLException sqle) {}
            }

            if (exception != null) {
                if (exception instanceof MessageServerException) {
                    received = false;
                } else {
                    throw new MessageServerException(exception.getMessage());
                }
            }

            if (received == true) {
                ebxmlMessage.setFileName(fileName);
            }

            logger.debug("<= MessageServer.hasReceived");
        }
        return received;
    }

    /**
     * Gets the applicationContext attribute of the MessageServer object
     *
     * @param messageId Description of the Parameter
     * @param tx Description of the Parameter
     * @return The applicationContext value
     * @throws MessageServerException Description of the Exception
     */
    ApplicationContext getApplicationContext(String messageId, Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.getApplicationContext");

        PreparedStatement query = null;
        ResultSet resultSet = null;
        Exception exception = null;
        ApplicationContext appContext = null;
        try {
            query = DbTableManager.DBTABLE_MESSAGE_STORE.getSelectQuery
                    (tx.getConnection(),
                    new String[]{
                    DbTableManager.ATTRIBUTE_CPA_ID,
                    DbTableManager.ATTRIBUTE_CONVERSATION_ID,
                    DbTableManager.ATTRIBUTE_SERVICE,
                    DbTableManager.ATTRIBUTE_ACTION},
                    new String[]{DbTableManager.ATTRIBUTE_MESSAGE_ID},
                    new int[]{DbTableManager.EQUAL},
                    new String[]{messageId});
            resultSet = query.executeQuery();

            if (!resultSet.next()) {
                String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_DB_CANNOT_FIND_MESSAGE,
                        "id: <" + messageId + "> in <"
                         + DbTableManager.TABLE_MESSAGE_STORE + ">");
                logger.warn(err);
                throw new MessageServerException("");
            }

            appContext = new ApplicationContext
                    (resultSet.getString(DbTableManager.ATTRIBUTE_CPA_ID),
                    resultSet.getString(DbTableManager.ATTRIBUTE_CONVERSATION_ID),
                    resultSet.getString(DbTableManager.ATTRIBUTE_SERVICE),
                    resultSet.getString(DbTableManager.ATTRIBUTE_ACTION));

            if (resultSet.next()) {
                String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_DB_DATA_INCONSISTENT,
                        "more than one message with id: <" + messageId
                         + "> in <" + DbTableManager.TABLE_MESSAGE_STORE + ">");
                logger.warn(err);
                throw new MessageServerException(err);
            }
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException sqle) {}
            }
            if (query != null) {
                try {
                    query.close();
                } catch (SQLException sqle) {}
            }

            if (exception != null &&
                    exception instanceof MessageServerException &&
                    exception.getMessage().equals("") == false) {
                throw (MessageServerException) exception;
            }

            if (exception != null &&
                    (exception instanceof MessageServerException) == false) {
                throw new MessageServerException(exception.getMessage());
            }

            logger.debug("<= MessageServer.getApplicationContext");
        }
        return appContext;
    }

    /**
     * Gets the messageStatus attribute of the MessageServer object
     *
     * @param messageMap Description of the Parameter
     * @param tx Description of the Parameter
     * @return The messageStatus value
     * @throws MessageServerException Description of the Exception
     */
    HashMap getMessageStatus(Map messageMap, Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.getMessageStatus");

        PreparedStatement query = null;
        PreparedStatement sentquery = null;
        PreparedStatement receivedquery = null;
        ResultSet resultSet = null;
        Exception exception = null;

        Iterator iter = messageMap.keySet().iterator();
        HashMap resultMap = new HashMap();
        try {
            query = DbTableManager.DBTABLE_MESSAGE_STORE.getSelectQuery
                    (tx.getConnection(),
                    new String[]{DbTableManager.ATTRIBUTE_STATE},
                    new String[]{DbTableManager.ATTRIBUTE_MESSAGE_ID},
                    new int[]{DbTableManager.EQUAL}, null);

            sentquery = DbTableManager.DBTABLE_SENT_MESSAGE.getSelectQuery
                    (tx.getConnection(), new String[]{
                    DbTableManager.ATTRIBUTE_TIME,
                    DbTableManager.ATTRIBUTE_STATUS},
                    new String[]{DbTableManager.ATTRIBUTE_MESSAGE_ID},
                    new int[]{DbTableManager.EQUAL}, null);

            receivedquery =
                    DbTableManager.DBTABLE_RECEIVED_MESSAGE.getSelectQuery(tx.getConnection(), new String[]{
                    DbTableManager.ATTRIBUTE_TIME,
                    DbTableManager.ATTRIBUTE_STATUS,
                    DbTableManager.ATTRIBUTE_REMOTE_HOST,
                    DbTableManager.ATTRIBUTE_REMOTE_ADDRESS},
                    new String[]{DbTableManager.ATTRIBUTE_MESSAGE_ID},
                    new int[]{DbTableManager.EQUAL}, null);

            while (iter.hasNext()) {
                String msgId = (String) iter.next();
                HashMap contentMap = new HashMap();
                ArrayList nameList = new ArrayList();
                ArrayList valueList = new ArrayList();

                // Message store table
                query.setString(1, msgId);
                resultSet = query.executeQuery();
                if (resultSet.next()) {
                    HashMap valueMap = new HashMap();
                    int state = resultSet.getInt
                            (DbTableManager.ATTRIBUTE_STATE);
                    nameList.add(Constants.MESSAGE_STATUS_STATE);
                    valueList.add(getStateName(state));
                }
                resultSet.close();

                // Sent message table
                sentquery.setString(1, msgId);
                resultSet = sentquery.executeQuery();
                while (resultSet.next()) {
                    HashMap valueMap = new HashMap();
                    String time = Utility.toUTCString
                            (new Date(resultSet.getLong(1)));
                    String description = resultSet.getString(2);
                    valueMap.put(Constants.MESSAGE_STATUS_DESCRIPTION,
                            description);
                    valueMap.put(Constants.MESSAGE_STATUS_TIMESTAMP, time);
                    valueMap.put(Constants.MESSAGE_STATUS_REMOTE_HOST, "");
                    valueMap.put(Constants.MESSAGE_STATUS_REMOTE_ADDRESS, "");
                    nameList.add(Constants.MESSAGE_STATUS_STATUS);
                    valueList.add(valueMap);
                }
                resultSet.close();

                // Received message table
                receivedquery.setString(1, msgId);
                resultSet = receivedquery.executeQuery();
                while (resultSet.next()) {
                    HashMap valueMap = new HashMap();
                    String time = Utility.toUTCString
                            (new Date(resultSet.getLong(1)));
                    String description = resultSet.getString(2);
                    String host = resultSet.getString(3);
                    String address = resultSet.getString(4);
                    valueMap.put(Constants.MESSAGE_STATUS_DESCRIPTION,
                            description);
                    valueMap.put(Constants.MESSAGE_STATUS_TIMESTAMP, time);
                    valueMap.put(Constants.MESSAGE_STATUS_REMOTE_HOST, host);
                    valueMap.put(Constants.MESSAGE_STATUS_REMOTE_ADDRESS,
                            address);
                    nameList.add(Constants.MESSAGE_STATUS_STATUS);
                    valueList.add(valueMap);
                }
                contentMap.put(nameList, valueList);
                resultMap.put(msgId, contentMap);
                resultSet.close();
            }
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException sqle) {}
            }
            if (sentquery != null) {
                try {
                    sentquery.close();
                } catch (SQLException sqle) {}
            }
            if (receivedquery != null) {
                try {
                    receivedquery.close();
                } catch (SQLException sqle) {}
            }
            if (query != null) {
                try {
                    query.close();
                } catch (SQLException sqle) {}
            }

            if (exception != null) {
                throw new MessageServerException(exception.getMessage());
            }

            logger.debug("<= MessageServer.getMessageStatus");
        }
        return resultMap;
    }

    /**
     * Gets the messageStatus attribute of the MessageServer object
     *
     * @param messageId Description of the Parameter
     * @param tx Description of the Parameter
     * @return The messageStatus value
     * @throws MessageServerException Description of the Exception
     */
    String[] getMessageStatus(String messageId, Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.getMessageStatus");

        PreparedStatement query = null;
        ResultSet resultSet = null;
        Exception exception = null;
        String status = null;
        String timestamp = null;
        String cpaId = null;
        String conversationId = null;
        String service = null;
        String action = null;
        int dbState;
        try {
            query = DbTableManager.DBTABLE_MESSAGE_STORE.getSelectQuery
                    (tx.getConnection(),
                    new String[]{DbTableManager.ATTRIBUTE_STATE,
                    DbTableManager.ATTRIBUTE_CPA_ID,
                    DbTableManager.ATTRIBUTE_CONVERSATION_ID,
                    DbTableManager.ATTRIBUTE_SERVICE,
                    DbTableManager.ATTRIBUTE_ACTION},
                    new String[]{DbTableManager.ATTRIBUTE_MESSAGE_ID},
                    new int[]{DbTableManager.EQUAL},
                    new String[]{messageId});
            resultSet = query.executeQuery();
            if (!resultSet.next()) {
                String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_DB_CANNOT_FIND_MESSAGE,
                        "id: <" + messageId + "> in <"
                         + DbTableManager.TABLE_MESSAGE_STORE + ">");
                logger.warn(err);
                status = Constants.STATUS_NOT_RECOGNIZED;
            } else {
                dbState = resultSet.getInt(DbTableManager.ATTRIBUTE_STATE);
                cpaId = resultSet.getString(DbTableManager.ATTRIBUTE_CPA_ID);
                conversationId = resultSet.getString
                        (DbTableManager.ATTRIBUTE_CONVERSATION_ID);
                service = resultSet.getString(DbTableManager.ATTRIBUTE_SERVICE);
                action = resultSet.getString(DbTableManager.ATTRIBUTE_ACTION);

                if (resultSet.next()) {
                    String err = ErrorMessages.getMessage(
                            ErrorMessages.ERR_DB_DATA_INCONSISTENT,
                            "more than one message with id: <" + messageId
                             + "> in <" + DbTableManager.TABLE_MESSAGE_STORE + ">");
                    logger.warn(err);
                    throw new MessageServerException(err);
                }

                if (dbState == STATE_RECEIVED ||
                        dbState == STATE_RECEIVED_ACKNOWLEDGED) {
                    // Get time stamp
                    resultSet.close();
                    query.close();
                    query = DbTableManager.DBTABLE_MESSAGE_INFO.getSelectQuery(
                            tx.getConnection(),
                            new String[]{DbTableManager.ATTRIBUTE_ROW_TIMESTAMP},
                            new String[]{DbTableManager.ATTRIBUTE_MESSAGE_ID},
                            new int[]{DbTableManager.EQUAL},
                            new String[]{messageId});
                    resultSet = query.executeQuery();

                    if (!resultSet.next()) {
                        logger.debug("cannot get timestamp of the "
                                 + "original message");
                    } else {
                        timestamp = resultSet.getString
                                (DbTableManager.ATTRIBUTE_ROW_TIMESTAMP);
                    }

                    status = Constants.STATUS_PROCESSED;
                } else {
                    status = Constants.STATUS_UN_AUTHORIZED;
                }
            }
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException sqle) {}
            }
            if (query != null) {
                try {
                    query.close();
                } catch (SQLException sqle) {}
            }

            if (exception != null) {
                throw new MessageServerException(exception.getMessage());
            }

            logger.debug("<= MessageServer.getMessageStatus");
        }
        return new String[]{status, timestamp, cpaId, conversationId,
                service, action};
    }

    /**
     * Description of the Method
     *
     * @param messageId Description of the Parameter
     * @param state Description of the Parameter
     * @param retryInterval Description of the Parameter
     * @param tx Description of the Parameter
     * @throws MessageServerException Description of the Exception
     */
    void retry(String messageId, int state, long retryInterval, Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.retry");

        PreparedStatement query = null;
        PreparedStatement update = null;
        PreparedStatement sendingStateUpdate = null;
        ResultSet resultSet = null;
        Exception exception = null;
        try {
            query = DbTableManager.DBTABLE_MESSAGE_STORE.getSelectQuery
                    (tx.getConnection(),
                    new String[]{DbTableManager.ATTRIBUTE_STATE},
                    new String[]{DbTableManager.ATTRIBUTE_MESSAGE_ID},
                    new int[]{DbTableManager.EQUAL}, new String[]{messageId});
            resultSet = query.executeQuery();

            if (!resultSet.next()) {
                String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_DB_CANNOT_FIND_MESSAGE,
                        "id: <" + messageId + "> in <"
                         + DbTableManager.TABLE_MESSAGE_STORE + ">");
                logger.warn(err);
                throw new MessageServerException(err);
            }

            final int dbState =
                    resultSet.getInt(DbTableManager.ATTRIBUTE_STATE);
            logger.debug("current state: <" + getStateName(dbState)
                     + "> specified state: <" + getStateName(state) + ">");
            if (dbState == STATE_DELETED) {
                // Message is deleted. No action needed as the cleanup works
                // should have been done already.
                throw new MessageServerException("");
            } else if (state == STATE_SENT) {
                if (dbState == STATE_SENT_RECEIVED ||
                        dbState == STATE_ACKNOWLEDGED ||
                        dbState == STATE_RECEIVED_ACKNOWLEDGED) {
                    // "Acknowledged" state implies "sent", no action needed.
                    throw new MessageServerException("");
                } else if (dbState == STATE_RECEIVED) {
                    state = STATE_SENT_RECEIVED;
                }
            } else if (state == STATE_SENT_FAILED) {
                if (dbState == STATE_ACKNOWLEDGED ||
                        dbState == STATE_RECEIVED_ACKNOWLEDGED) {
                    // The message is acknowleged, so the "sent_failed" must
                    // be invalid even in reliable messaging.
                    throw new MessageServerException("");
                }
            } else if (state == STATE_ACKNOWLEDGED) {
                if (dbState == STATE_RECEIVED ||
                        dbState == STATE_SENT_RECEIVED) {
                    state = STATE_RECEIVED_ACKNOWLEDGED;
                } else if (dbState == STATE_RECEIVED_ACKNOWLEDGED) {
                    // Received_Acknowledged implies acknowledged.
                    throw new MessageServerException("");
                }
            }

            if ((state != STATE_SENT_STARTED) && !isFinalState(state)) {
                /*
                 *  dbState = String.valueOf(dbIntState + 1);
                 *  if (!dbState.equals(state)) {
                 *  throw new SQLException("Internal state of messageid: "
                 *  + messageId + " in database does not synchronize with "
                 *  + "that of sending thread: database state = "
                 *  + dbState + " while sending thread state = " + state);
                 *  }
                 */
                sendingStateUpdate =
                        DbTableManager.DBTABLE_SENDING_STATE.getUpdateQuery(tx.getConnection(), new String[]
                        {DbTableManager.ATTRIBUTE_CURRENT_RETRY,
                        DbTableManager.ATTRIBUTE_NEXT_RETRY_TIME},
                        new String[]{String.valueOf(state), String.valueOf
                        (System.currentTimeMillis() + retryInterval)},
                        new String[]{DbTableManager.ATTRIBUTE_MESSAGE_ID},
                        new int[]{DbTableManager.EQUAL},
                        new String[]{messageId});
            } else if (state != STATE_SENT_STARTED) {
                sendingStateUpdate = tx.prepareStatement("DELETE FROM "
                         + DbTableManager.DBTABLE_SENDING_STATE.getName()
                         + " WHERE " + DbTableManager.ATTRIBUTE_MESSAGE_ID + "='"
                         + messageId + "'");
            }
            logger.debug("Update state to become <" + getStateName(state)
                     + ">");

            if (resultSet.next()) {
                String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_DB_DATA_INCONSISTENT,
                        "more than one message with id: <" + messageId
                         + "> in <" + DbTableManager.TABLE_MESSAGE_STORE + ">");
                logger.warn(err);
                throw new MessageServerException(err);
            }

            update = DbTableManager.DBTABLE_MESSAGE_STORE.getUpdateQuery
                    (tx.getConnection(),
                    new String[]{DbTableManager.ATTRIBUTE_STATE},
                    new String[]{String.valueOf(state)},
                    new String[]{DbTableManager.ATTRIBUTE_MESSAGE_ID},
                    new int[]{DbTableManager.EQUAL}, new String[]{messageId});
            final PreparedStatement[] updates = (sendingStateUpdate == null ?
                    new PreparedStatement[]{update} : new PreparedStatement[]
                    {update, sendingStateUpdate});
            tx.executeUpdate(updates);
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException sqle) {}
            }
            if (query != null) {
                try {
                    query.close();
                } catch (SQLException sqle) {}
            }
            if (update != null) {
                try {
                    update.close();
                } catch (SQLException sqle) {}
            }
            if (sendingStateUpdate != null) {
                try {
                    sendingStateUpdate.close();
                } catch (SQLException sqle) {}
            }

            if (exception != null &&
                    exception instanceof MessageServerException &&
                    exception.getMessage().equals("") == false) {
                throw (MessageServerException) exception;
            }

            if (exception != null &&
                    (exception instanceof MessageServerException) == false) {
                throw new MessageServerException(exception.getMessage());
            }
        }

        logger.debug("<= MessageServer.retry");
    }

    /**
     * Description of the Method
     *
     * @param messageId Description of the Parameter
     * @param tx Description of the Parameter
     * @throws MessageServerException Description of the Exception
     */
    void resend(String messageId, Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.resend");

        PreparedStatement update = null;
        PreparedStatement sendingStateInsert = null;
        Exception exception = null;
        try {
            update = DbTableManager.DBTABLE_MESSAGE_STORE.getUpdateQuery
                    (tx.getConnection(),
                    new String[]{DbTableManager.ATTRIBUTE_STATE},
                    new String[]{String.valueOf(STATE_SENT_STARTED)},
                    new String[]{DbTableManager.ATTRIBUTE_MESSAGE_ID},
                    new int[]{DbTableManager.EQUAL},
                    new String[]{messageId});
            sendingStateInsert =
                    DbTableManager.DBTABLE_SENDING_STATE.getInsertQuery(tx.getConnection(), new String[]
                    {DbTableManager.ATTRIBUTE_MESSAGE_ID,
                    DbTableManager.ATTRIBUTE_CURRENT_RETRY,
                    DbTableManager.ATTRIBUTE_NEXT_RETRY_TIME},
                    new String[]{messageId, String.valueOf(STATE_SENT_STARTED),
                    String.valueOf(System.currentTimeMillis())});
            tx.executeUpdate
                    (new PreparedStatement[]{update, sendingStateInsert});
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            if (update != null) {
                try {
                    update.close();
                } catch (SQLException sqle) {}
            }
            if (sendingStateInsert != null) {
                try {
                    sendingStateInsert.close();
                } catch (SQLException sqle) {}
            }

            if (exception != null &&
                    (exception instanceof MessageServerException)) {
                throw (MessageServerException) exception;
            }

            if (exception != null) {
                throw new MessageServerException(exception.getMessage());
            }
        }

        logger.debug("<= MessageServer.resend");
    }

    /**
     * Store the states for the EbxmlMessage
     *
     * @param ebxmlMessage Description of the Parameter
     * @param appContext Description of the Parameter
     * @param state Description of the Parameter
     * @param isDelivered Description of the Parameter
     * @param tx Description of the Parameter
     * @return Description of the Return Value
     * @throws MessageServerException Description of the Exception
     */
    void store(EbxmlMessage ebxmlMessage, ApplicationContext appContext,
            int state, boolean isDelivered, Transaction tx)
             throws MessageServerException {
        int seqNo;
        MessageOrder messageOrder = ebxmlMessage.getMessageOrder();
        if (messageOrder == null) {
            seqNo = (isDelivered ? FIRST_MESSAGE_DELIVERED
                     : MESSAGE_ORDER_DISABLED);
        } else {
            seqNo = messageOrder.getSequenceNumber();
            seqNo = (isDelivered ? toDelivered(seqNo) : toUndelivered(seqNo));
        }
        logger.debug("final sequence number in store: " + seqNo);
        store(ebxmlMessage, appContext, state, seqNo, tx);
    }

    /**
     * Store the states for the EbxmlMessage
     *
     * @param ebxmlMessage Description of the Parameter
     * @param appContext Description of the Parameter
     * @param state Description of the Parameter
     * @param sequenceNumber Description of the Parameter
     * @param tx Description of the Parameter
     * @return Description of the Return Value
     * @throws MessageServerException Description of the Exception
     */
    void store(EbxmlMessage ebxmlMessage, ApplicationContext appContext,
            int state, int sequenceNumber, Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.store");
        final String messageId = ebxmlMessage.getMessageId();

        final Acknowledgment acknowledgment = ebxmlMessage.getAcknowledgment();
        final ErrorList errorList = ebxmlMessage.getErrorList();
        final String refToMessageId;
        if (acknowledgment != null) {
            refToMessageId = acknowledgment.getRefToMessageId();
        } else if (errorList != null) {
            refToMessageId =
                    ebxmlMessage.getMessageHeader().getRefToMessageId();
        } else {
            refToMessageId = null;
        }

        String fileName = null;
        PreparedStatement refToMessageInsert = null;
        PreparedStatement insert = null;
        PreparedStatement infoinsert = null;
        PreparedStatement query = null;
        PreparedStatement update = null;
        PreparedStatement sendingStateInsert = null;
        ResultSet resultSet = null;
        Exception exception = null;
        PersistenceHandler handler
                = PersistenceManager.getRepositoryPersistenceHandler();
        try {
            if (refToMessageId != null) {
                logger.debug("insert into reftomessage database");
                refToMessageInsert =
                        DbTableManager.DBTABLE_REF_TO_MESSAGE.getInsertQuery(tx.getConnection(),
                        new String[]{
                        DbTableManager.ATTRIBUTE_MESSAGE_ID,
                        DbTableManager.ATTRIBUTE_REF_TO_MESSAGE_ID
                        },
                        new String[]{messageId, refToMessageId});
            }

            query = DbTableManager.DBTABLE_MESSAGE_STORE.getSelectQuery
                    (tx.getConnection(),
                    new String[]{DbTableManager.ATTRIBUTE_FILE_NAME},
                    new String[]{DbTableManager.ATTRIBUTE_MESSAGE_ID},
                    new int[]{DbTableManager.EQUAL},
                    new String[]{messageId});
            resultSet = query.executeQuery();
            PreparedStatement[] actions;
            if (!resultSet.next()) {
                /*
                fileName = ebxmlMessage.getFileName();
                if (fileName == null) {
                    fileName = tx.store(ebxmlMessage);
                }
                File messageRepoFile = new File(messageRepository);
                String messageRepoPath = messageRepoFile.getCanonicalPath();
                if (!fileName.startsWith(messageRepoPath)) {
                    String err = ErrorMessages.getMessage(
                            ErrorMessages.ERR_HERMES_FILE_NOT_FOUND_ERROR, fileName
                             + ": EbxmlMessage is not stored in MessageRepository");
                    logger.warn(err);
                    throw new MessageServerException(err);
                }
                String fileNameInDB = fileName.substring(
                        messageRepoPath.length());
                */
                fileName = ebxmlMessage.getPersistenceName();
                if (fileName == null) {
                    logger.debug("Message haven't persisted");
                    DataSource dataSource = handler.createNewObject();
                    fileName = dataSource.getName();
                    logger.debug("Try to persist to " + fileName);
                    OutputStream ostream = null;
                    try {
                        ostream = dataSource.getOutputStream();
                        ebxmlMessage.writeTo(ostream);
                    } catch (Exception e) {
                        throw e;
                    } finally {
                        if (ostream != null) {
                            ostream.close();
                        }
                    }
                    logger.debug("Message Persisted to " + fileName
                            + " on persitence handler");
                    ebxmlMessage.setPersistenceInfo(fileName, handler);
                }
                String fileNameInDB = fileName;
                logger.debug("insert into messagestore database");
                insert = DbTableManager.DBTABLE_MESSAGE_STORE.getInsertQuery
                        (tx.getConnection(), new String[]
                        {DbTableManager.ATTRIBUTE_MESSAGE_ID,
                        DbTableManager.ATTRIBUTE_CPA_ID,
                        DbTableManager.ATTRIBUTE_CONVERSATION_ID,
                        DbTableManager.ATTRIBUTE_SERVICE,
                        DbTableManager.ATTRIBUTE_ACTION,
                        DbTableManager.ATTRIBUTE_FILE_NAME,
                        DbTableManager.ATTRIBUTE_STATE,
                        DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER,
                        DbTableManager.ATTRIBUTE_IS_ARCHIVED},
                        new String[]{messageId, appContext.getCpaId(),
                        appContext.getConversationId(),
                        appContext.getService(),
                        appContext.getAction(), fileNameInDB,
                        String.valueOf(state),
                        String.valueOf(sequenceNumber),
                        String.valueOf(0)});
                // Get the number of payloads
                int numPayload = 0;
                for (Iterator it = ebxmlMessage.getPayloadContainers();
                        it.hasNext(); it.next(), ++numPayload) {
                }
                ;

                // Get message timestamp
                String timestamp = ebxmlMessage.getTimestamp();
                long time;
                if (timestamp == null) {
                    time = 0;
                } else {
                    Date parsedDate = Utility.fromUTCString(timestamp);
                    if (parsedDate != null) {
                        time = parsedDate.getTime();
                    } else {
                        time = 0;
                    }
                }

                infoinsert =
                        DbTableManager.DBTABLE_MESSAGE_INFO.getInsertQuery(
                        tx.getConnection(),
                        new String[]{
                        DbTableManager.ATTRIBUTE_MESSAGE_ID,
                        DbTableManager.ATTRIBUTE_CONVERSATION_ID,
                        DbTableManager.ATTRIBUTE_NUMBER_OF_PAYLOAD,
                        DbTableManager.ATTRIBUTE_TIMESTAMP,
                        DbTableManager.ATTRIBUTE_ROW_TIMESTAMP
                        }, new String[]{
                        messageId, ebxmlMessage.getConversationId(),
                        String.valueOf(numPayload), String.valueOf(time),
                        String.valueOf(new Date().getTime())
                        });

                if (state == STATE_SENT_STARTED) {
                    sendingStateInsert =
                            DbTableManager.DBTABLE_SENDING_STATE.getInsertQuery(
                            tx.getConnection(), new String[]
                            {DbTableManager.ATTRIBUTE_MESSAGE_ID,
                            DbTableManager.ATTRIBUTE_CURRENT_RETRY,
                            DbTableManager.ATTRIBUTE_NEXT_RETRY_TIME},
                            new String[]{messageId,
                            String.valueOf(STATE_SENT_STARTED),
                            String.valueOf(System.currentTimeMillis())});
                }

                if (refToMessageInsert == null) {
                    if (sendingStateInsert == null) {
                        actions = new PreparedStatement[]
                                {insert, infoinsert};
                    } else {
                        actions = new PreparedStatement[]
                                {insert, infoinsert, sendingStateInsert};
                    }
                } else {
                    if (sendingStateInsert == null) {
                        actions = new PreparedStatement[]
                                {refToMessageInsert, insert, infoinsert};
                    } else {
                        actions = new PreparedStatement[]
                                {refToMessageInsert, insert, infoinsert,
                                sendingStateInsert};
                    }
                }
            } else {
                // If the message exists and the state is being set to
                // STATE_SENT_STARTED, then it means that a message having the
                // same message ID is already in the database. Such message
                // should only be sent during retry, but not as a new message.
                if (state == STATE_SENT_STARTED) {
                    String err = ErrorMessages.getMessage(
                            ErrorMessages.ERR_DB_DATA_INCONSISTENT,
                            "message already exists in the database");
                    logger.warn(err);
                    throw new MessageServerException(err);
                }

                // Acknowledgment always exist locally if this is a loop back
                // message; it does not need to be inserted into database
                fileName = resultSet.getString
                        (DbTableManager.ATTRIBUTE_FILE_NAME);
                logger.debug("loopback message; update messagestore table");
                update = DbTableManager.DBTABLE_MESSAGE_STORE.getUpdateQuery
                        (tx.getConnection(), new String[]
                        {DbTableManager.ATTRIBUTE_CPA_ID,
                        DbTableManager.ATTRIBUTE_CONVERSATION_ID,
                        DbTableManager.ATTRIBUTE_SERVICE,
                        DbTableManager.ATTRIBUTE_ACTION,
                        DbTableManager.ATTRIBUTE_FILE_NAME,
                        DbTableManager.ATTRIBUTE_STATE,
                        DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER},
                        new String[]{appContext.getCpaId(),
                        appContext.getConversationId(),
                        appContext.getService(),
                        appContext.getAction(), fileName,
                        String.valueOf(state),
                        String.valueOf(sequenceNumber)},
                        new String[]{DbTableManager.ATTRIBUTE_MESSAGE_ID},
                        new int[]{DbTableManager.EQUAL},
                        new String[]{messageId});
                actions = new PreparedStatement[]{update};
                //fileName = messageRepository + File.separator + fileName;
            }
            tx.executeUpdate(actions);
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException sqle) {}
            }
            if (refToMessageInsert != null) {
                try {
                    refToMessageInsert.close();
                } catch (SQLException sqle) {}
            }
            if (infoinsert != null) {
                try {
                    infoinsert.close();
                } catch (SQLException sqle) {}
            }
            if (insert != null) {
                try {
                    insert.close();
                } catch (SQLException sqle) {}
            }
            if (query != null) {
                try {
                    query.close();
                } catch (SQLException sqle) {}
            }
            if (update != null) {
                try {
                    update.close();
                } catch (SQLException sqle) {}
            }

            if (exception != null &&
                    (exception instanceof MessageServerException)) {
                throw (MessageServerException) exception;
            }

            if (exception != null) {
                throw new MessageServerException(exception.getMessage());
            }
            /*
            if (ebxmlMessage.getFileName() == null) {
                ebxmlMessage.setFileName(fileName);
            }*/
            if (ebxmlMessage.getPersistenceName() == null) {
                ebxmlMessage.setPersistenceInfo(fileName, handler);
            }
        }

        logger.debug("<= MessageServer.store");
    }
    
    void remove(MessageServiceHandlerConfig mshConfig, Transaction tx) throws MessageServerException {
        logger.debug("=> MessageServer.store");

        final ApplicationContext appContext =
                mshConfig.getApplicationContext();
        final String toMshUrl = mshConfig.getToMshUrl() == null ?
                Constants.DEFAULT_TO_MSH_URL : mshConfig.getToMshUrl().toString();
        final MessageListener messageListener = mshConfig.getMessageListener();
        final String transportType = mshConfig.getTransportType();
        final String retries = String.valueOf(mshConfig.getRetries());
        final String retryInterval = mshConfig.getRetryInterval();
        final String syncReply = String.valueOf(mshConfig.getSyncReply());
        final String messageOrder = mshConfig.isMessageOrdered() ? "1" : "0";
        final String persistDuration = mshConfig.getPersistDuration();
        final String ackRequested = String.valueOf(mshConfig.getAckRequested());
        final String enabled = mshConfig.isEnabled() ? "1" : "0";
        PreparedStatement query = null;
        PreparedStatement update = null;
        ResultSet resultSet = null;
        Exception exception = null;
        try {
            query = DbTableManager.DBTABLE_MSH_CONFIG.getSelectQuery(tx.getConnection(),
                new String[]{DbTableManager.ATTRIBUTE_MESSAGE_LISTENER},
                new String[]{DbTableManager.ATTRIBUTE_CPA_ID,
                    DbTableManager.ATTRIBUTE_CONVERSATION_ID,
                    DbTableManager.ATTRIBUTE_SERVICE,
                    DbTableManager.ATTRIBUTE_ACTION},
                new int[]{
                    DbTableManager.EQUAL, DbTableManager.EQUAL,
                    DbTableManager.EQUAL, DbTableManager.EQUAL},
                new String[]{appContext.getCpaId(),
                    appContext.getConversationId(),
                    appContext.getService(), appContext.getAction()});
            resultSet = query.executeQuery();
            final String objectName;
            final DataSource dataSource;
            PersistenceHandler handler
                    = PersistenceManager.getObjectStorePersistenceHandler();
            if (!resultSet.next()) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR,
                    "Specify MSHConfig not registered");
                throw new MessageServerException(err);
            } else {
                objectName = resultSet.getString
                        (DbTableManager.ATTRIBUTE_MESSAGE_LISTENER);
                if (resultSet.next()) {
                    String err = ErrorMessages.getMessage(
                            ErrorMessages.ERR_DB_DATA_INCONSISTENT,
                            "more than one record in "
                             + DbTableManager.TABLE_MSH_CONFIG
                             + " have the same AppContext");
                    logger.warn(err);
                    throw new MessageServerException(err);
                }
                query = DbTableManager.DBTABLE_MSH_CONFIG.getDeleteQuery(
                    tx.getConnection(),
                    new String[]{DbTableManager.ATTRIBUTE_CPA_ID,
                        DbTableManager.ATTRIBUTE_CONVERSATION_ID,
                        DbTableManager.ATTRIBUTE_SERVICE,
                        DbTableManager.ATTRIBUTE_ACTION},
                    new int[]{
                        DbTableManager.EQUAL, DbTableManager.EQUAL,
                        DbTableManager.EQUAL, DbTableManager.EQUAL},
                    new String[]{appContext.getCpaId(),
                        appContext.getConversationId(),
                        appContext.getService(), appContext.getAction()});
                tx.executeUpdate(new PreparedStatement[]{query});
                handler.removeObject(objectName);
            }
        } catch (PersistenceException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR,
                "Specify MSHConfig not registered");
            throw new MessageServerException(err);
        } catch (SQLException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR,
                "Specify MSHConfig not registered");
            throw new MessageServerException(err);
        }
    }

    /**
     * Update the database status on the msh config.
     *
     * @param mshConfig The message service handler config
     * @param tx the transaction for this updating
     * @throws MessageServerException throw if there is error on udpating
     * the msh config
     */
    void store(MessageServiceHandlerConfig mshConfig, Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.store");

        final ApplicationContext appContext =
                mshConfig.getApplicationContext();
        final String toMshUrl = mshConfig.getToMshUrl() == null ?
                Constants.DEFAULT_TO_MSH_URL : mshConfig.getToMshUrl().toString();
        final MessageListener messageListener = mshConfig.getMessageListener();
        final String transportType = mshConfig.getTransportType();
        final String retries = String.valueOf(mshConfig.getRetries());
        final String retryInterval = mshConfig.getRetryInterval();
        final String syncReply = String.valueOf(mshConfig.getSyncReply());
        final String messageOrder = mshConfig.isMessageOrdered() ? "1" : "0";
        final String persistDuration = mshConfig.getPersistDuration();
        final String ackRequested = String.valueOf(mshConfig.getAckRequested());
        final String enabled = mshConfig.isEnabled() ? "1" : "0";

        /*
         *  Store MessageListener object persistently
         */
        /*
        final File storeDir = new File(objectStore);
        if (!storeDir.exists() && !storeDir.mkdirs()) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_FILE_IO_ERROR,
                    "cannot create object store:" + objectStore);
            logger.error(err);
            throw new MessageServerException(err);
        } else if (storeDir.isFile()) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_FILE_IO_ERROR,
                    "object store is a file: " + objectStore);
            logger.error(err);
            throw new MessageServerException(err);
        }*/

        PreparedStatement query = null;
        PreparedStatement update = null;
        ResultSet resultSet = null;
        Exception exception = null;
        try {
            query = DbTableManager.DBTABLE_MSH_CONFIG.getSelectQuery(tx.getConnection(),
                new String[]{DbTableManager.ATTRIBUTE_MESSAGE_LISTENER},
                new String[]{DbTableManager.ATTRIBUTE_CPA_ID,
                    DbTableManager.ATTRIBUTE_CONVERSATION_ID,
                    DbTableManager.ATTRIBUTE_SERVICE,
                    DbTableManager.ATTRIBUTE_ACTION},
                new int[]{
                    DbTableManager.EQUAL, DbTableManager.EQUAL,
                    DbTableManager.EQUAL, DbTableManager.EQUAL},
                new String[]{appContext.getCpaId(),
                    appContext.getConversationId(),
                    appContext.getService(), appContext.getAction()});
            resultSet = query.executeQuery();
            final String objectName;
            final DataSource dataSource;
            PersistenceHandler handler
                    = PersistenceManager.getObjectStorePersistenceHandler();
            if (!resultSet.next()) {
                dataSource = handler.createNewObject();
                objectName = dataSource.getName();
                /*
                objectName = DirectoryManager.getObjectStoreFileName
                        (appContext.getCpaId() + appContext.getConversationId()
                         + appContext.getService() + appContext.getAction());
                */
                update =
                        DbTableManager.DBTABLE_MSH_CONFIG.getInsertQuery(tx.getConnection(),
                        new String[]{
                        DbTableManager.ATTRIBUTE_CPA_ID,
                        DbTableManager.ATTRIBUTE_CONVERSATION_ID,
                        DbTableManager.ATTRIBUTE_SERVICE,
                        DbTableManager.ATTRIBUTE_ACTION,
                        DbTableManager.ATTRIBUTE_TO_MSH_URL,
                        DbTableManager.ATTRIBUTE_MESSAGE_LISTENER,
                        DbTableManager.ATTRIBUTE_TRANSPORT_TYPE,
                        DbTableManager.ATTRIBUTE_RETRIES,
                        DbTableManager.ATTRIBUTE_RETRY_INTERVAL,
                        DbTableManager.ATTRIBUTE_SYNC_REPLY,
                        DbTableManager.ATTRIBUTE_MESSAGE_ORDER,
                        DbTableManager.ATTRIBUTE_PERSIST_DURATION,
                        DbTableManager.ATTRIBUTE_ACK_REQUESTED,
                        DbTableManager.ATTRIBUTE_ENABLED
                        }, new String[]{
                        appContext.getCpaId(),
                        appContext.getConversationId(),
                        appContext.getService(),
                        appContext.getAction(),
                        toMshUrl, objectName, transportType, retries,
                        retryInterval, syncReply, messageOrder,
                        persistDuration, ackRequested, enabled
                        });
            } else {
                objectName = resultSet.getString
                        (DbTableManager.ATTRIBUTE_MESSAGE_LISTENER);
                if (resultSet.next()) {
                    String err = ErrorMessages.getMessage(
                            ErrorMessages.ERR_DB_DATA_INCONSISTENT,
                            "more than one record in "
                             + DbTableManager.TABLE_MSH_CONFIG
                             + " have the same AppContext");
                    logger.warn(err);
                    throw new MessageServerException(err);
                }
                dataSource = handler.getObject(objectName);
                logger.debug("update MSHConfig");
                update =
                        DbTableManager.DBTABLE_MSH_CONFIG.getUpdateQuery(tx.getConnection(),
                        new String[]{
                        DbTableManager.ATTRIBUTE_TO_MSH_URL,
                        DbTableManager.ATTRIBUTE_MESSAGE_LISTENER,
                        DbTableManager.ATTRIBUTE_TRANSPORT_TYPE,
                        DbTableManager.ATTRIBUTE_RETRIES,
                        DbTableManager.ATTRIBUTE_RETRY_INTERVAL,
                        DbTableManager.ATTRIBUTE_SYNC_REPLY,
                        DbTableManager.ATTRIBUTE_MESSAGE_ORDER,
                        DbTableManager.ATTRIBUTE_PERSIST_DURATION,
                        DbTableManager.ATTRIBUTE_ACK_REQUESTED,
                        DbTableManager.ATTRIBUTE_ENABLED},
                        new String[]{toMshUrl, objectName,
                        transportType, retries, retryInterval, syncReply,
                        messageOrder, persistDuration, ackRequested,
                        enabled},
                        new String[]{DbTableManager.ATTRIBUTE_CPA_ID,
                        DbTableManager.ATTRIBUTE_CONVERSATION_ID,
                        DbTableManager.ATTRIBUTE_SERVICE,
                        DbTableManager.ATTRIBUTE_ACTION},
                        new int[]{
                        DbTableManager.EQUAL, DbTableManager.EQUAL,
                        DbTableManager.EQUAL, DbTableManager.EQUAL},
                        new String[]{appContext.getCpaId(),
                        appContext.getConversationId(),
                        appContext.getService(), appContext.getAction()
                        }
                        );
            }
            /**
            String path = objectStore + File.separator + objectName;
            tx.storeObject(messageListener, path);
            */
            OutputStream stream = null;
            ObjectOutputStream objStream = null;
            try {
                stream = dataSource.getOutputStream();
                objStream = new ObjectOutputStream(stream);
                objStream.writeObject(messageListener);
                objStream.flush();
            } catch (IOException e) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_FILE_IO_ERROR, e,
                    "cannot store object to Persistence Handler "
                    + "using name : " + objectName);
                throw new TransactionException(err);
            } catch (Exception e) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
                throw new TransactionException(err);
            } finally {
                if (stream != null) {
                    stream.close();
                }
                if (objStream != null) {
                    objStream.close();
                }
            }
            tx.storePersistenceObject(objectName, handler);
            tx.executeUpdate(new PreparedStatement[]{update});
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {}
            try {
                if (query != null) {
                    query.close();
                }
            } catch (SQLException e) {}
            try {
                if (update != null) {
                    update.close();
                }
            } catch (SQLException e) {}

            if (exception != null) {
                throw new MessageServerException(exception.getMessage());
            }
        }

        logger.debug("<= MessageServer.store");
    }

    /**
     * Gets the messageServiceHandlerConfig attribute of the MessageServer object
     *
     * @param tx Description of the Parameter
     * @return The messageServiceHandlerConfig value
     * @throws MessageServerException Description of the Exception
     */
    Iterator getMessageServiceHandlerConfig(Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.getMessageServiceHandlerConfig");

        PreparedStatement query = null;
        ResultSet resultSet = null;
        Exception exception = null;
        final ArrayList mshConfig = new ArrayList();
        try {
            query =
                    DbTableManager.DBTABLE_MSH_CONFIG.getSelectQuery(tx.getConnection(), null);
            resultSet = query.executeQuery();
            while (resultSet.next()) {
                final ApplicationContext appContext = new ApplicationContext
                        (resultSet.getString(DbTableManager.ATTRIBUTE_CPA_ID),
                        resultSet.getString
                        (DbTableManager.ATTRIBUTE_CONVERSATION_ID),
                        resultSet.getString(DbTableManager.ATTRIBUTE_SERVICE),
                        resultSet.getString(DbTableManager.ATTRIBUTE_ACTION));
                String urlStr = resultSet.getString(
                        DbTableManager.ATTRIBUTE_TO_MSH_URL);
                final URL toMshUrl =
                        urlStr.equals(Constants.DEFAULT_TO_MSH_URL) ?
                        null : new URL(urlStr);

                final String objectName = resultSet.getString(
                        DbTableManager.ATTRIBUTE_MESSAGE_LISTENER);
                /*
                final MessageListener messageListener = (MessageListener)
                    tx.readObject(objectStore + File.separator + objectName);
                */
                PersistenceHandler handler
                        = PersistenceManager.getObjectStorePersistenceHandler();
                InputStream stream = null;
                ObjectInputStream objStream = null;
                final MessageListener messageListener;
                try {
                    logger.debug("loading object store with object name : "
                            + objectName);
                    DataSource source = handler.getObject(objectName);
                    if (source == null) {
                        throw new Exception("Cannot load the objectName : "
                                + objectName);
                    }
                    stream = handler.getObject(objectName).getInputStream();
                    objStream = new ObjectInputStream(stream);
                    messageListener = (MessageListener) objStream.readObject();
                } catch (IOException e) {
                    String err = ErrorMessages.getMessage(
                            ErrorMessages.ERR_HERMES_FILE_IO_ERROR, e,
                            "cannot read object to Persistence Handler "
                            + "using name : " + objectName);
                    logger.error(err);
                    throw new TransactionException(err);
                } catch (ClassNotFoundException e) {
                    String err = ErrorMessages.getMessage(
                            ErrorMessages.ERR_HERMES_FILE_IO_ERROR, e,
                            "cannot read object to Persistence Handler "
                            + "using name : " + objectName);
                    logger.error(err);
                    throw new TransactionException(err);
                } catch (Exception e) {
                    String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
                    logger.error(err);
                    throw new TransactionException(err);
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                    if (objStream != null) {
                        objStream.close();
                    }
                }

                final MessageServiceHandlerConfig config =
                        new MessageServiceHandlerConfig(appContext, toMshUrl,
                        messageListener,
                        resultSet.getString(
                        DbTableManager.ATTRIBUTE_TRANSPORT_TYPE),
                        resultSet.getInt(
                        DbTableManager.ATTRIBUTE_RETRIES),
                        resultSet.getString(
                        DbTableManager.ATTRIBUTE_RETRY_INTERVAL),
                        resultSet.getInt(
                        DbTableManager.ATTRIBUTE_SYNC_REPLY),
                        resultSet.getString(
                        DbTableManager.ATTRIBUTE_MESSAGE_ORDER).equals("1"),
                        resultSet.getString(
                        DbTableManager.ATTRIBUTE_PERSIST_DURATION),
                        resultSet.getInt(
                        DbTableManager.ATTRIBUTE_ACK_REQUESTED),
                        resultSet.getString(
                        DbTableManager.ATTRIBUTE_ENABLED).equals("1"));
                mshConfig.add(config);
            }
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {}
            try {
                if (query != null) {
                    query.close();
                }
            } catch (SQLException e) {}

            if (exception != null) {
                throw new MessageServerException(exception.getMessage());
            }
        }

        logger.debug("<= MessageServer.getMessageServiceHandlerConfig");

        return mshConfig.iterator();
    }

    /**
     * Gets the messageById attribute of the MessageServer object
     *
     * @param messageId Description of the Parameter
     * @param tx Description of the Parameter
     * @return The messageById value
     * @throws MessageServerException Description of the Exception
     */
    String getMessageById(String messageId, Transaction tx)
             throws MessageServerException {
        logger.debug("=> MessageServer.getMessageById");
        String fileName = null;
        PreparedStatement query = null;
        ResultSet resultSet = null;
        Exception exception = null;
        try {
            query = DbTableManager.DBTABLE_MESSAGE_STORE.getSelectQuery
                    (tx.getConnection(), new String[]{
                    DbTableManager.ATTRIBUTE_FILE_NAME,
                    DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER},
                    new String[]{
                    DbTableManager.ATTRIBUTE_MESSAGE_ID,
                    DbTableManager.ATTRIBUTE_STATE,
                    DbTableManager.ATTRIBUTE_STATE},
                    new int[]{
                    DbTableManager.EQUAL, DbTableManager.GREATER_EQUAL,
                    DbTableManager.LESS_EQUAL},
                    new String[]{
                    messageId, String.valueOf(RECEIVED_FROM_RANGE),
                    String.valueOf(RECEIVED_TO_RANGE)});
            resultSet = query.executeQuery();

            if (!resultSet.next()) {
                String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_DB_CANNOT_FIND_MESSAGE,
                        "id: <" + messageId + "> in <"
                         + DbTableManager.TABLE_MESSAGE_STORE + ">");
                logger.warn(err);
                throw new MessageServerException(err);
            }

            int seqNo = resultSet.getInt
                    (DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER);
            fileName = resultSet.getString(DbTableManager.ATTRIBUTE_FILE_NAME);

            if (resultSet.next()) {
                String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_DB_DATA_INCONSISTENT,
                        "more than one message with id: <" + messageId
                         + "> in <" + DbTableManager.TABLE_MESSAGE_STORE + ">");
                logger.warn(err);
                throw new MessageServerException(err);
            }
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {}
            try {
                if (query != null) {
                    query.close();
                }
            } catch (SQLException e) {}

            if (exception != null) {
                throw new MessageServerException(exception.getMessage());
            }

            logger.debug("<= MessageServer.getMessageById");
        }
        return fileName;
    }

    /**
     * Gets the undeliveredMessages attribute of the MessageServer object
     *
     * @param appContext Description of the Parameter
     * @param tx Description of the Parameter
     * @return The undeliveredMessages value
     * @throws MessageServerException Description of the Exception
     */
    Map getUndeliveredMessages(ApplicationContext appContext, Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.getUndeliveredMessages");

        Map messageMap = new HashMap();
        PreparedStatement query = null;
        ResultSet resultSet = null;
        Exception exception = null;
        try {
            query = tx.getConnection().prepareStatement("SELECT " +
                    DbTableManager.DBTABLE_MESSAGE_STORE.getName() + "." +
                    DbTableManager.ATTRIBUTE_MESSAGE_ID + ", " +
                    DbTableManager.DBTABLE_MESSAGE_INFO.getName() + "." +
                    DbTableManager.ATTRIBUTE_CONVERSATION_ID + ", " +
                    DbTableManager.DBTABLE_MESSAGE_STORE.getName() + "." +
                    DbTableManager.ATTRIBUTE_FILE_NAME + ", " +
                    DbTableManager.DBTABLE_MESSAGE_STORE.getName() + "." +
                    DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER + " FROM " +
                    DbTableManager.DBTABLE_MESSAGE_STORE.getName() + ", " +
                    DbTableManager.DBTABLE_MESSAGE_INFO.getName() + " WHERE " +
                    DbTableManager.DBTABLE_MESSAGE_STORE.getName() + "." +
                    DbTableManager.ATTRIBUTE_MESSAGE_ID + "=" +
                    DbTableManager.DBTABLE_MESSAGE_INFO.getName() + "." +
                    DbTableManager.ATTRIBUTE_MESSAGE_ID + " AND " +
                    DbTableManager.DBTABLE_MESSAGE_STORE.getName() + "." +
                    DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER + ">=" +
                    String.valueOf(FIRST_MESSAGE_UNDELIVERED) + " AND " +
                    DbTableManager.DBTABLE_MESSAGE_STORE.getName() + "." +
                    DbTableManager.ATTRIBUTE_CPA_ID + "='" +
                    appContext.getCpaId() + "' AND " +
                    DbTableManager.DBTABLE_MESSAGE_STORE.getName() + "." +
                    DbTableManager.ATTRIBUTE_CONVERSATION_ID + "='" +
                    appContext.getConversationId() + "' AND " +
                    DbTableManager.DBTABLE_MESSAGE_STORE.getName() + "." +
                    DbTableManager.ATTRIBUTE_SERVICE + "='" +
                    appContext.getService() + "' AND " +
                    DbTableManager.DBTABLE_MESSAGE_STORE.getName() + "." +
                    DbTableManager.ATTRIBUTE_ACTION + "='" +
                    appContext.getAction() + "' AND " +
                    DbTableManager.DBTABLE_MESSAGE_STORE.getName() + "." +
                    DbTableManager.ATTRIBUTE_STATE + ">=" +
                    String.valueOf(RECEIVED_FROM_RANGE) + " AND " +
                    DbTableManager.DBTABLE_MESSAGE_STORE.getName() + "." +
                    DbTableManager.ATTRIBUTE_STATE + "<=" +
                    String.valueOf(RECEIVED_TO_RANGE));
            resultSet = query.executeQuery();

            while (resultSet.next()) {
                String messageId =
                        resultSet.getString(DbTableManager.ATTRIBUTE_MESSAGE_ID);
                String conversationId =
                        resultSet.getString(DbTableManager.ATTRIBUTE_CONVERSATION_ID);
                String filename =
                        resultSet.getString(DbTableManager.ATTRIBUTE_FILE_NAME);
                Integer seqNo = new Integer(
                        resultSet.getInt(DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER));
                final ArrayList list = new ArrayList();
                list.add(conversationId);
                list.add(seqNo);
                list.add(filename);
                messageMap.put(messageId, list);
            }
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {}
            try {
                if (query != null) {
                    query.close();
                }
            } catch (SQLException e) {}

            if (exception != null) {
                throw new MessageServerException(exception.getMessage());
            }

            logger.debug("<= MessageServer.getUndeliveredMessages");
        }
        return messageMap;
    }

    /**
     * Gets the sentSequenceMap attribute of the MessageServer object
     *
     * @param tx Description of the Parameter
     * @return The sentSequenceMap value
     * @throws MessageServerException Description of the Exception
     */
    Map getSentSequenceMap(Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.getSentSequenceMap");

        logger.debug("get the sequence number of sent messages");

        final Map sentMap = new HashMap();
        PreparedStatement sentQuery = null;
        ResultSet resultSet = null;
        Exception exception = null;
        try {
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT ").append("mi.")
                    .append(DbTableManager.ATTRIBUTE_CONVERSATION_ID).append(", ")
                    .append("MIN(").append(DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER)
                    .append(")").append(", ").append("MAX(")
                    .append(DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER).append(")")
                    .append(" FROM ").append(DbTableManager.TABLE_MESSAGE_STORE)
                    .append(" ms, ").append(DbTableManager.TABLE_MESSAGE_INFO)
                    .append(" mi").append(" WHERE ").append("ms.")
                    .append(DbTableManager.ATTRIBUTE_MESSAGE_ID).append("=")
                    .append("mi.").append(DbTableManager.ATTRIBUTE_MESSAGE_ID)
                    .append(" AND ").append(DbTableManager.ATTRIBUTE_STATE)
                    .append(">=").append(SENT_FROM_RANGE).append(" AND ")
                    .append(DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER).append("<=")
                    .append(FIRST_MESSAGE_ORDER_DELIVERED).append(" AND ")
                    .append(DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER).append(">=")
                    .append(FIRST_MESSAGE_ORDER_UNDELIVERED).append(" GROUP BY ")
                    .append("mi.").append(DbTableManager.ATTRIBUTE_CONVERSATION_ID);
            sentQuery = tx.getConnection().prepareStatement(sql.toString());
            resultSet = sentQuery.executeQuery();
            while (resultSet.next()) {
                String conversationId = resultSet.getString(1);
                int minSeqNo = resultSet.getInt(2);
                int maxSeqNo = resultSet.getInt(3);
                if (minSeqNo <= FIRST_MESSAGE_ORDER_DELIVERED) {
                    minSeqNo = toUndelivered(minSeqNo);
                }
                maxSeqNo = ((maxSeqNo > minSeqNo) ? maxSeqNo : minSeqNo);
                logger.debug("  ConversationID: " + conversationId
                         + ", Max Seq: " + maxSeqNo);
                sentMap.put(conversationId, new Integer(maxSeqNo));
            }
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {}
            try {
                if (sentQuery != null) {
                    sentQuery.close();
                }
            } catch (SQLException e) {}

            if (exception != null) {
                throw new MessageServerException(exception.getMessage());
            }

            logger.debug("<= MessageServer.getSentSequenceMap");
        }
        return sentMap;
    }

    /**
     * Gets the deliveryMap attribute of the MessageServer object
     *
     * @param tx Description of the Parameter
     * @return The deliveryMap value
     * @throws MessageServerException Description of the Exception
     */
    Map getDeliveryMap(Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.getDeliveryMap");

        logger.debug("get delivery status of messages");

        final Map deliveryMap = new HashMap();
        PreparedStatement deliveredQuery = null;
        PreparedStatement pendingQuery = null;
        ResultSet resultSet = null;
        Exception exception = null;
        try {
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT ").append("mi.")
                    .append(DbTableManager.ATTRIBUTE_CONVERSATION_ID).append(", ")
                    .append("MIN(").append(DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER)
                    .append(")").append(" FROM ")
                    .append(DbTableManager.TABLE_MESSAGE_STORE).append(" ms, ")
                    .append(DbTableManager.TABLE_MESSAGE_INFO).append(" mi")
                    .append(" WHERE ")
                    .append("ms.").append(DbTableManager.ATTRIBUTE_MESSAGE_ID)
                    .append("=")
                    .append("mi.").append(DbTableManager.ATTRIBUTE_MESSAGE_ID)
                    .append(" AND ")
                    .append(DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER).append("<=")
                    .append(FIRST_MESSAGE_ORDER_DELIVERED)
                    .append(" GROUP BY ")
                    .append("mi.").append(DbTableManager.ATTRIBUTE_CONVERSATION_ID);
            deliveredQuery = tx.getConnection().prepareStatement(
                    sql.toString());
            /*
             *  deliveredQuery = DbTableManager.
             *  DBTABLE_MESSAGE_STORE.getSelectQuery(tx.getConnection(),
             *  new String [] {
             *  DbTableManager.ATTRIBUTE_CONVERSATION_ID,
             *  "MIN(" + DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER + ")"
             *  }, new String [] {
             *  DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER
             *  },
             *  new int [] { DbTableManager.LESS_EQUAL },
             *  new String [] {
             *  String.valueOf(FIRST_MESSAGE_ORDER_DELIVERED) },
             *  null,
             *  new String [] { DbTableManager.ATTRIBUTE_CONVERSATION_ID }
             *  );
             */
            resultSet = deliveredQuery.executeQuery();
            while (resultSet.next()) {
                final String conversationId = resultSet.getString(1);
                final int minSeqNo = resultSet.getInt(2);
                final DeliveryRecord deliveryRecord = new DeliveryRecord();
                deliveryRecord.setLastDelivered(toUndelivered(minSeqNo));
                deliveryMap.put(conversationId, deliveryRecord);
            }

            resultSet.close();
            resultSet = null;

            sql = new StringBuffer();
            sql.append("SELECT ").append("mi.")
                    .append(DbTableManager.ATTRIBUTE_CONVERSATION_ID).append(", ")
                    .append(DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER)
                    .append(" FROM ")
                    .append(DbTableManager.TABLE_MESSAGE_STORE).append(" ms, ")
                    .append(DbTableManager.TABLE_MESSAGE_INFO).append(" mi")
                    .append(" WHERE ")
                    .append("ms.").append(DbTableManager.ATTRIBUTE_MESSAGE_ID)
                    .append("=")
                    .append("mi.").append(DbTableManager.ATTRIBUTE_MESSAGE_ID)
                    .append(" AND ")
                    .append(DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER).append(">=")
                    .append(FIRST_MESSAGE_ORDER_UNDELIVERED);
            pendingQuery = tx.getConnection().prepareStatement(sql.toString());
            /*
             *  pendingQuery = DbTableManager.
             *  DBTABLE_MESSAGE_STORE.getSelectQuery(tx.getConnection(),
             *  new String [] {
             *  DbTableManager.ATTRIBUTE_CONVERSATION_ID,
             *  DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER },
             *  new String [] { DbTableManager.ATTRIBUTE_SEQUENCE_NUMBER },
             *  new int [] { DbTableManager.GREATER_EQUAL },
             *  new String [] {
             *  String.valueOf(FIRST_MESSAGE_ORDER_UNDELIVERED) }
             *  );
             */
            resultSet = pendingQuery.executeQuery();
            while (resultSet.next()) {
                final String conversationId = resultSet.getString(1);
                final int seqNo = resultSet.getInt(2);
                DeliveryRecord deliveryRecord =
                        (DeliveryRecord) deliveryMap.get(conversationId);
                if (deliveryRecord == null) {
                    deliveryRecord = new DeliveryRecord();
                    deliveryMap.put(conversationId, deliveryRecord);
                }
                logger.debug("conversation id: " + conversationId);
                deliveryRecord.addUndelivered(seqNo);
            }
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {}
            try {
                if (deliveredQuery != null) {
                    deliveredQuery.close();
                }
            } catch (SQLException e) {}
            try {
                if (pendingQuery != null) {
                    pendingQuery.close();
                }
            } catch (SQLException e) {}

            if (exception != null) {
                throw new MessageServerException(exception.getMessage());
            }

            logger.debug("<= MessageServer.getDeliveryMap");
        }
        return deliveryMap;
    }

    /**
     * Description of the Method
     *
     * @param msh Description of the Parameter
     * @param appContextToMshConfigMap Description of the Parameter
     * @param tx Description of the Parameter
     * @return Description of the Return Value
     * @throws MessageServerException Description of the Exception
     */
    Map restartSendThread(MessageServiceHandler msh,
            Map appContextToMshConfigMap, Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.restartSendThread");
        logger.info("Restart previous sending threads");

        final Map sendThreadMap = new HashMap();
        PreparedStatement query = null;
        ResultSet resultSet = null;
        Exception exception = null;
        try {
            query = tx.prepareStatement
                    ("SELECT * FROM " +
                    DbTableManager.DBTABLE_MESSAGE_STORE.getName() + ", " +
                    DbTableManager.DBTABLE_SENDING_STATE.getName() + " WHERE " +
                    DbTableManager.DBTABLE_MESSAGE_STORE.getName() + "." + DbTableManager.ATTRIBUTE_MESSAGE_ID + " = "
                     + DbTableManager.DBTABLE_SENDING_STATE.getName() + "."
                     + DbTableManager.ATTRIBUTE_MESSAGE_ID);
            resultSet = query.executeQuery();
            while (resultSet.next()) {
                final String cpaId =
                        resultSet.getString(DbTableManager.ATTRIBUTE_CPA_ID);
                final String conversationId =
                        resultSet.getString(DbTableManager.ATTRIBUTE_CONVERSATION_ID);
                final String service =
                        resultSet.getString(DbTableManager.ATTRIBUTE_SERVICE);
                final String action =
                        resultSet.getString(DbTableManager.ATTRIBUTE_ACTION);
                final ApplicationContext appContext = new ApplicationContext
                        (cpaId, conversationId, service, action);
                final Object obj = appContextToMshConfigMap.get(appContext);
                if (obj == null) {
                    String err = ErrorMessages.getMessage(
                            ErrorMessages.ERR_DB_DATA_INCONSISTENT,
                            "unknown application context: "
                             + appContext.toString());
                    logger.error(err);
                    throw new MessageServerException(err);
                }
                final MessageServiceHandlerConfig mshConfig;
                if (obj instanceof MessageServiceHandlerConnection) {
                    mshConfig = ((MessageServiceHandlerConnection) obj).
                            getMessageServiceHandlerConfig();
                } else {
                    mshConfig = (MessageServiceHandlerConfig) obj;
                }
                final String messageId =
                        resultSet.getString(DbTableManager.ATTRIBUTE_MESSAGE_ID);
                String fileName =
                        resultSet.getString(DbTableManager.ATTRIBUTE_FILE_NAME);
                /*
                fileName = fileName.replace('\\', File.separatorChar);
                fileName = fileName.replace('/', File.separatorChar);
                final File pathName = new File
                        (messageRepository + File.separator + fileName);
                if (!pathName.exists()) {
                    String err = ErrorMessages.getMessage(
                            ErrorMessages.ERR_HERMES_FILE_NOT_FOUND_ERROR,
                            "file for message <" + messageId + ">");
                    logger.error(err);
                    throw new MessageServerException(err);
                }
                final EbxmlMessage ebxmlMessage = getMessageFromFile(pathName);
                */
                PersistenceHandler handler
                        = PersistenceManager.getRepositoryPersistenceHandler();
                DataSource dataSource = handler.getObject(fileName);
                if (dataSource == null) {
                    String err = ErrorMessages.getMessage(
                            ErrorMessages.ERR_HERMES_UNKNOWN_ERROR,
                            "Cannot restore persisted  message "
                            + "with messsage id <" + messageId + ">");
                    logger.error(err);
                    throw new MessageServerException(err);
                }
                final EbxmlMessage ebxmlMessage = (EbxmlMessage)
                        getMessageFromDataSource(dataSource, true);

                int currentRetry = resultSet.getInt(
                        DbTableManager.ATTRIBUTE_CURRENT_RETRY);
                final long nextRetryTime = resultSet.getLong(
                        DbTableManager.ATTRIBUTE_NEXT_RETRY_TIME);
                final int retries = mshConfig.getRetries();
                final long retryInterval =
                        Long.parseLong(mshConfig.getRetryInterval());
                final long currentTime = System.currentTimeMillis();
                logger.info("  Last retry = " + String.valueOf(currentRetry)
                         + " ; retry interval = " + mshConfig.getRetryInterval()
                         + " ; next retry time = " + String.valueOf(nextRetryTime)
                         + " ; current time = " + String.valueOf(currentTime));
                final long latency;
                if ((currentRetry > 0 && nextRetryTime > currentTime) ||
                        (currentRetry == 0 && (nextRetryTime + retryInterval) >
                        currentTime)) {
                    if (currentRetry > 0) {
                        latency = nextRetryTime - currentTime;
                    } else {
                        latency = nextRetryTime + retryInterval - currentTime;
                    }
                    logger.info("  This sending thread will wait for "
                             + String.valueOf(latency) + " milliseconds before "
                             + "attempting the next retry");
                } else {
                    latency = 0;
                    final long skipRetry = (currentTime - nextRetryTime) /
                            retryInterval;
                    final long slipTime = (currentTime - nextRetryTime) %
                            retryInterval;
                    currentRetry += skipRetry;
                    logger.info("  " + String.valueOf(skipRetry) + " retry "
                             + " interval(s) has passed with "
                             + String.valueOf(slipTime) + " milliseconds slipped");
                }
                final MessageProcessor messageProcessor = new MessageProcessor
                        (ebxmlMessage, mshConfig, msh, currentRetry, latency);
                logger.info("  Resume sending messageId: " + messageId
                         + " ; appContext = " + appContext.toString()
                         + " ; currentRetry = " + String.valueOf(currentRetry));
                synchronized (sendThreadMap) {
                    sendThreadMap.put(messageId, messageProcessor);
                }
            }

            synchronized (sendThreadMap) {
                for (Iterator i = sendThreadMap.values().iterator();
                        i.hasNext(); ) {
                    final MessageProcessor messageProcessor =
                            (MessageProcessor) i.next();
                    messageProcessor.start();
                }
            }
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {}
            try {
                if (query != null) {
                    query.close();
                }
            } catch (SQLException e) {}

            if (exception != null) {
                throw new MessageServerException(exception.getMessage());
            }

            logger.debug("<= MessageServer.restartSendThread");
        }
        return sendThreadMap;
    }

    /**
     * Description of the Method
     *
     * @param ebxmlMessage Description of the Parameter
     * @param status Description of the Parameter
     * @param tx Description of the Parameter
     * @throws MessageServerException Description of the Exception
     */
    void logSentMessage(EbxmlMessage ebxmlMessage, String status,
            Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.logSentMessage");

        final String hasAckRequested =
                (ebxmlMessage.getAckRequested() != null ? "1" : "0");
        final String hasDuplicateElimination =
                (ebxmlMessage.getDuplicateElimination() ? "1" : "0");
        String newStatus = status;
        if (newStatus == null) {
            newStatus = "NULL";
        } else {
            if (newStatus.length() > 255) {
                newStatus = newStatus.substring(0, 255);
            }
        }

        PreparedStatement insert = null;
        Exception exception = null;
        try {
            insert =
                    DbTableManager.DBTABLE_SENT_MESSAGE.getInsertQuery(tx.getConnection(), new String[]{
                    DbTableManager.ATTRIBUTE_TIME,
                    DbTableManager.ATTRIBUTE_MESSAGE_ID,
                    DbTableManager.ATTRIBUTE_STATUS,
                    DbTableManager.ATTRIBUTE_FROM_PARTY_ID,
                    DbTableManager.ATTRIBUTE_TO_PARTY_ID,
                    DbTableManager.ATTRIBUTE_CPA_ID,
                    DbTableManager.ATTRIBUTE_CONVERSATION_ID,
                    DbTableManager.ATTRIBUTE_SERVICE,
                    DbTableManager.ATTRIBUTE_ACTION,
                    DbTableManager.ATTRIBUTE_ACK_REQUESTED,
                    DbTableManager.ATTRIBUTE_DUPLICATE_ELIMINATION
                    }, new String[]{
                    String.valueOf(System.currentTimeMillis()),
                    ebxmlMessage.getMessageId(),
                    newStatus,
                    ((MessageHeader.PartyId) ebxmlMessage.getFromPartyIds().
                    next()).getId(),
                    ((MessageHeader.PartyId) ebxmlMessage.getToPartyIds().
                    next()).getId(),
                    ebxmlMessage.getCpaId(),
                    ebxmlMessage.getConversationId(),
                    ebxmlMessage.getService(),
                    ebxmlMessage.getAction(),
                    hasAckRequested,
                    hasDuplicateElimination
                    });
            tx.executeUpdate(new PreparedStatement[]{insert});
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            try {
                if (insert != null) {
                    insert.close();
                }
            } catch (SQLException e) {}

            if (exception != null) {
                throw new MessageServerException(exception.getMessage());
            }
        }

        logger.debug("<= MessageServer.logSentMessage");
    }

    /**
     * Description of the Method
     *
     * @param ebxmlMessage Description of the Parameter
     * @param requestProperty Description of the Parameter
     * @param tx Description of the Parameter
     * @throws MessageServerException Description of the Exception
     */
    void logReceivedMessage(EbxmlMessage ebxmlMessage, Map requestProperty,
            Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.logReceivedMessage");

        final String hasAckRequested =
                (ebxmlMessage.getAckRequested() != null ? "1" : "0");
        final String hasDuplicateElimination =
                (ebxmlMessage.getDuplicateElimination() ? "1" : "0");
        final String remoteAddress = (String)
                requestProperty.get(DbTableManager.ATTRIBUTE_REMOTE_ADDRESS);
        final String remoteHost = (String)
                requestProperty.get(DbTableManager.ATTRIBUTE_REMOTE_HOST);
        String status = (String) requestProperty.get
                (DbTableManager.ATTRIBUTE_STATUS);
        if (status == null) {
            status = "NULL";
        } else {
            if (status.length() > 255) {
                status = status.substring(0, 255);
            }
        }

        PreparedStatement insert = null;
        Exception exception = null;
        try {
            insert =
                    DbTableManager.DBTABLE_RECEIVED_MESSAGE.getInsertQuery(tx.getConnection(),
                    new String[]{
                    DbTableManager.ATTRIBUTE_TIME,
                    DbTableManager.ATTRIBUTE_REMOTE_ADDRESS,
                    DbTableManager.ATTRIBUTE_REMOTE_HOST,
                    DbTableManager.ATTRIBUTE_MESSAGE_ID,
                    DbTableManager.ATTRIBUTE_STATUS,
                    DbTableManager.ATTRIBUTE_FROM_PARTY_ID,
                    DbTableManager.ATTRIBUTE_TO_PARTY_ID,
                    DbTableManager.ATTRIBUTE_CPA_ID,
                    DbTableManager.ATTRIBUTE_CONVERSATION_ID,
                    DbTableManager.ATTRIBUTE_SERVICE,
                    DbTableManager.ATTRIBUTE_ACTION,
                    DbTableManager.ATTRIBUTE_ACK_REQUESTED,
                    DbTableManager.ATTRIBUTE_DUPLICATE_ELIMINATION
                    }, new String[]{
                    String.valueOf(System.currentTimeMillis()),
                    remoteAddress, remoteHost, ebxmlMessage.getMessageId(),
                    status,
                    ((MessageHeader.PartyId) ebxmlMessage.getFromPartyIds().
                    next()).getId(),
                    ((MessageHeader.PartyId) ebxmlMessage.getToPartyIds().
                    next()).getId(),
                    ebxmlMessage.getCpaId(), ebxmlMessage.getConversationId(),
                    ebxmlMessage.getService(), ebxmlMessage.getAction(),
                    hasAckRequested, hasDuplicateElimination
                    });
            tx.executeUpdate(new PreparedStatement[]{insert});
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            try {
                if (insert != null) {
                    insert.close();
                }
            } catch (SQLException e) {}

            if (exception != null) {
                throw new MessageServerException(exception.getMessage());
            }
        }

        logger.debug("<= MessageServer.logReceivedMessage");
    }

    /**
     * Description of the Method
     *
     * @param description Description of the Parameter
     * @param tx Description of the Parameter
     * @throws MessageServerException Description of the Exception
     */
    void logMSHLifeCycle(String description, Transaction tx)
             throws MessageServerException {

        logger.debug("=> MessageServer.logMSHLifeCycle");

        Exception exception = null;
        PreparedStatement insert = null;

        try {
            insert =
                    DbTableManager.DBTABLE_MSH_LOG.getInsertQuery(tx.getConnection(), new String[]{
                    DbTableManager.ATTRIBUTE_TIME,
                    DbTableManager.ATTRIBUTE_ACTION
                    }, new String[]{
                    String.valueOf(System.currentTimeMillis()),
                    description
                    });

            tx.executeUpdate(new PreparedStatement[]{insert});
        } catch (MessageServerException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            try {
                if (insert != null) {
                    insert.close();
                }
            } catch (SQLException e) {}

            if (exception != null) {
                throw new MessageServerException(exception.getMessage());
            }
        }

        logger.debug("<= MessageServer.logMSHLifeCycle");
    }

    /**
     * Gets the numRecordsInDB attribute of the MessageServer object
     *
     * @param table Description of the Parameter
     * @param tx Description of the Parameter
     * @return The numRecordsInDB value
     * @throws MessageServerException Description of the Exception
     */
    int getNumRecordsInDB(DbTableManager.DbTable table, Transaction tx)
             throws MessageServerException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        int ret = -1;
        try {
            statement = table.getCountQuery(tx.getConnection(),
                    null, null, null);
            resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                ret = -1;
            } else {
                ret = resultSet.getInt(1);
            }
        } catch (MessageServerException e) {
        } catch (Exception e) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {}
        }
        return ret;
    }

    /**
     * parse the message from file.
     *
     * @param file the file that contain the message.
     * @return the ebxmlmessage deserialized from the file.
     * @throws MessageServerException throw if there is error on parsing
     */
    static EbxmlMessage getMessageFromFile(File file)
             throws MessageServerException {
        try {
            return new EbxmlMessage(file);
        } catch (SOAPException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e.toString());
            logger.warn(err);
            throw new MessageServerException(err);
        } catch (IOException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e.toString());
            logger.warn(err);
            throw new MessageServerException(err);
        }
    }
    
    /**
     * parse the message from InputStream and get the byte array of SOAP
     * Envelope
     * @param stream the InputStream contains the message
     * @return the byte array of the Soap Envelope
     * @throws MessageServerException throw if there is error on parsing
     */
    public static byte[] getSoapEnvelopeBytesFromStream(InputStream stream)
        throws MessageServerException {
        try {
            return EbxmlMessage.getSoapEnvelopeBytesFromStream(stream);
        } catch (SOAPException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e.toString());
            logger.warn(err);
            throw new MessageServerException(err);
        }
    }
    
    /**
     * parse the message from file.
     *
     * @param file the file contain the message
     * @param withAttachments whether we should parse the attachment.
     * @return SOAPMessage if withAttachment is false, EbxmlMessage
     * if withAttachment is true
     * @throws MessageServerException Description of the Exception
     */
    static Object getMessageFromFile(File file, boolean withAttachments)
             throws MessageServerException {
        //InputStream fileInputStream = null;
        try {
            //fileInputStream = new FileInputStream(file);
            DataSource source = new AttachmentDataSource(
                    file.getCanonicalPath(), 0, file.length(),
                    Constants.SERIALIZABLE_OBJECT);
            Object resultObj = getMessageFromDataSource(source,
                    withAttachments);
            if (withAttachments) {
                EbxmlMessage message = (EbxmlMessage) resultObj;
                message.setFileName(file.getCanonicalPath());
            }
            return resultObj;
        } catch (IOException e) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new MessageServerException(err);
        }
    }

    /**
     * Gets the messageFromDataSource attribute of the MessageServer class
     *
     * @param dataSource Description of the Parameter
     * @param withAttachments Description of the Parameter
     * @return The messageFromDataSource value
     * @throws MessageServerException Description of the Exception
     */
    public static Object getMessageFromDataSource(DataSource dataSource,
            boolean withAttachments) throws MessageServerException {
        logger.debug("=> MessageServer.getMessageFromDataSource");
        try {
            return EbxmlMessage.getMessageFromDataSource(dataSource,
                withAttachments);
        } catch (SOAPException e) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err, e);
            throw new MessageServerException(err);
        } catch (IOException e) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err, e);
            throw new MessageServerException(err);
        }
    }

    /**
     * Description of the Method
     *
     * @param connection Description of the Parameter
     * @param updates Description of the Parameter
     * @throws MessageServerException Description of the Exception
     */
    private void executeUpdate(Connection connection,
            PreparedStatement[] updates) throws MessageServerException {
        try {
            for (int i = 0; i < updates.length; i++) {
                updates[i].executeUpdate();
            }
            connection.commit();
        } catch (SQLException sqle) {
            String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_DB_CANNOT_WRITE, sqle);
            logger.warn(err);
            try {
                if (connection.isClosed() == false) {
                    connection.rollback();
                }
            } catch (SQLException sqle2) {} finally {
                try {
                    dbConnectionPool.freeConnection(connection, false);
                } catch (DbConnectionPoolException e) {}
            }
            throw new MessageServerException(err);
        }
    }

    /**
     * Description of the Method
     *
     * @throws MessageServerException Description of the Exception
     */
    private static void checkAndInstallDBTable() throws MessageServerException {

        logger.debug("=> MessageServer.checkAndInstallDBTable");

        logger.info("Verify the database tables are properly installed");

        Connection connection = null;
        MessageServerException exception = null;
        try {
            connection = dbConnectionPool.getRawConnection();
            // Check and install all the tables
            DbTableManager.checkAndInstallDBTable(connection);
        } catch (DbConnectionPoolException e) {
            exception = new MessageServerException(e.getMessage());
        } catch (DbTableManagerException dtme) {
            exception = new MessageServerException(dtme.getMessage());
            exception.fillInStackTrace();
        } finally {
            try {
                if (connection != null) {
                    dbConnectionPool.freeRawConnection(connection);
                }
            } catch (DbConnectionPoolException dcpe) {
                if (exception == null) {
                    exception = new MessageServerException(dcpe.getMessage());
                    exception.fillInStackTrace();
                }
            }

            if (exception != null) {
                // error already logged
                throw exception;
            }
        }

        logger.info("Looks good.");
        logger.debug("<= MessageServer.checkAndInstallDBTable");
    }

    /**
     * Reset database connection pool.
     *
     * @throws MessageServerException
     */
    void resetConnectionPool() throws MessageServerException {
        logger.debug("=> MessageServer.resetConnectionPool");

        try {
            dbConnectionPool.reset();
        } catch (DbConnectionPoolException e) {
            MessageServerException ex =
                    new MessageServerException(e.getMessage());
            ex.fillInStackTrace();
            throw ex;
        }

        logger.debug("<= MessageServer.resetConnectionPool");
    }

    /**
     * Check if database and connection pool is functioning properly.
     *
     * @return null if all operations were carried out successfully; otherwise
     *         a string stating the reason of failure would be returned.
     * @throws MessageServerException
     */
    String checkDatabase() throws MessageServerException {

        logger.debug("=> MessageServer.checkDatabase");

        Connection connection = null;
        Exception exception = null;
        String ret = null;
        try {
            // Try to get a connection and query all tables
            connection = dbConnectionPool.getConnection();
            if (connection.isClosed()) {
                ret = "A closed connection object is obtained.";
            } else {
                for (int i = 0; i < DbTableManager.DBTABLE_LIST.length; i++) {
                    if (DbTableManager.DBTABLE_LIST[i].check(connection) != 2) {
                        ret = "Cannot query " +
                                DbTableManager.DBTABLE_LIST[i].getName();
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            exception = new Exception(ErrorMessages.getMessage(
                    ErrorMessages.ERR_DB_CANNOT_READ, e));
        } catch (DbConnectionPoolException e) {
            exception = e;
        } finally {
            try {
                if (connection != null) {
                    dbConnectionPool.freeConnection
                            (connection, exception == null);
                }
            } catch (DbConnectionPoolException dbe) {
                throw new MessageServerException(dbe.getMessage());
            }

            if (exception != null) {
                ret = exception.getMessage();
            }
        }

        logger.debug("<= MessageServer.checkDatabase");
        return ret;
    }

    // Convert the sequence number to delivered state
    /**
     * Description of the Method
     *
     * @param seqNo Description of the Parameter
     * @return Description of the Return Value
     */
    public static int toDelivered(int seqNo) {
        if (seqNo >= FIRST_MESSAGE_UNDELIVERED) {
            if (seqNo == MESSAGE_ORDER_DISABLED) {
                return FIRST_MESSAGE_DELIVERED;
            } else {
                return FIRST_MESSAGE_ORDER_DELIVERED - seqNo;
            }
        } else {
            return seqNo;
        }
    }

    // Convert the sequence number to undelivered state
    /**
     * Description of the Method
     *
     * @param seqNo Description of the Parameter
     * @return Description of the Return Value
     */
    public static int toUndelivered(int seqNo) {
        if (seqNo == FIRST_MESSAGE_DELIVERED) {
            return FIRST_MESSAGE_UNDELIVERED;
        } else if (seqNo <= FIRST_MESSAGE_ORDER_DELIVERED) {
            return FIRST_MESSAGE_ORDER_DELIVERED - seqNo;
        } else {
            return seqNo;
        }
    }

    /**
     * Gets the stateName attribute of the MessageServer object
     *
     * @param state Description of the Parameter
     * @return The stateName value
     */
    private String getStateName(int state) {
        if (state == STATE_SENT_STARTED) {
            return Constants.MESSAGE_STATUS_SENT_STARTED;
        } else if (state == STATE_ACKNOWLEDGED) {
            return Constants.MESSAGE_STATUS_ACKNOWLEDGED;
        } else if (state == STATE_SENT_FAILED) {
            return Constants.MESSAGE_STATUS_SENT_FAILED;
        } else if (state == STATE_SENT) {
            return Constants.MESSAGE_STATUS_SENT;
        } else if (state == STATE_SENT_RECEIVED) {
            return Constants.MESSAGE_STATUS_SENT_RECEIVED;
        } else if (state == STATE_RECEIVED_ACKNOWLEDGED) {
            return Constants.MESSAGE_STATUS_RECEIVED_ACKNOWLEDGED;
        } else if (state == STATE_RECEIVED) {
            return Constants.MESSAGE_STATUS_RECEIVED;
        } else if (state == STATE_DELETED) {
            return Constants.MESSAGE_STATUS_DELETED;
        } else {
            if (state > 0) {
                return Constants.MESSAGE_STATUS_RETRYING + "#" + state;
            } else {
                return Constants.MESSAGE_STATUS_UNKNOWN;
            }
        }
    }

    /**
     * Gets the finalState attribute of the MessageServer class
     *
     * @param state Description of the Parameter
     * @return The finalState value
     */
    public static boolean isFinalState(int state) {
        return state == STATE_ACKNOWLEDGED || state == STATE_SENT_FAILED ||
                state == STATE_DELETED || state == STATE_SENT ||
                state == STATE_SENT_RECEIVED ||
                state == STATE_RECEIVED_ACKNOWLEDGED || state == STATE_RECEIVED;
    }

    /**
     * Gets the delivered attribute of the MessageServer class
     *
     * @param sequenceNumber Description of the Parameter
     * @return The delivered value
     */
    public static boolean isDelivered(int sequenceNumber) {
        return sequenceNumber <= FIRST_MESSAGE_DELIVERED;
    }
    
}

