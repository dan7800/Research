/*
 * Copyright(c) 2002 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the Academic Free License Version 1.0
 *
 * Academic Free License
 * Version 1.0
 *
 * This Academic Free License applies to any software and associated 
 * documentation (the "Software") whose owner (the "Licensor") has placed the 
 * statement "Licensed under the Academic Free License Version 1.0" immediately 
 * after the copyright notice that applies to the Software. 
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of the Software (1) to use, copy, modify, merge, publish, perform, 
 * distribute, sublicense, and/or sell copies of the Software, and to permit 
 * persons to whom the Software is furnished to do so, and (2) under patent 
 * claims owned or controlled by the Licensor that are embodied in the Software 
 * as furnished by the Licensor, to make, use, sell and offer for sale the 
 * Software and derivative works thereof, subject to the following conditions: 
 *
 * - Redistributions of the Software in source code form must retain all 
 *   copyright notices in the Software as furnished by the Licensor, this list 
 *   of conditions, and the following disclaimers. 
 * - Redistributions of the Software in executable form must reproduce all 
 *   copyright notices in the Software as furnished by the Licensor, this list 
 *   of conditions, and the following disclaimers in the documentation and/or 
 *   other materials provided with the distribution. 
 * - Neither the names of Licensor, nor the names of any contributors to the 
 *   Software, nor any of their trademarks or service marks, may be used to 
 *   endorse or promote products derived from this Software without express 
 *   prior written permission of the Licensor. 
 *
 * DISCLAIMERS: LICENSOR WARRANTS THAT THE COPYRIGHT IN AND TO THE SOFTWARE IS 
 * OWNED BY THE LICENSOR OR THAT THE SOFTWARE IS DISTRIBUTED BY LICENSOR UNDER 
 * A VALID CURRENT LICENSE. EXCEPT AS EXPRESSLY STATED IN THE IMMEDIATELY 
 * PRECEDING SENTENCE, THE SOFTWARE IS PROVIDED BY THE LICENSOR, CONTRIBUTORS 
 * AND COPYRIGHT OWNERS "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE 
 * LICENSOR, CONTRIBUTORS OR COPYRIGHT OWNERS BE LIABLE FOR ANY CLAIM, DAMAGES 
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE. 
 *
 * This license is Copyright (C) 2002 Lawrence E. Rosen. All rights reserved. 
 * Permission is hereby granted to copy and distribute this license without 
 * modification. This license may not be modified without the express written 
 * permission of its copyright owner. 
 */

/* ===== 
 *
 * $Header: /cvsroot/sino/ebxmlms/src/hk/hku/cecid/phoenix/message/handler/DbConnectionPool.java,v 1.27 2003/12/11 06:41:29 bobpykoon Exp $
 *
 * Code authored by:
 *
 * tslam [2002-07-04]
 *
 * Code reviewed by:
 *
 * username [YYYY-MM-DD]
 *
 * Remarks:
 *
 * =====
 */

package hk.hku.cecid.phoenix.message.handler;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.log4j.Logger;
/**
 * Utility class for database connection pooling.
 *
 * @author tslam
 * @version $Revision: 1.27 $
 */
class DbConnectionPool {

    static Logger logger = Logger.getLogger(DbConnectionPool.class);

    /** {@link #initialConnections} number of <code>java.sql.Connection</code> 
     * objects are available for checkout. Strong references are used here to 
     * prevent them from being collected by the garbage collector.
    */
    final Connection[] freeConnections;

    /** Extra number of <code>java.sql.Connection</code> objects up to
        {@link #maximumConnections} are created on demand. Weak references are
        used for these objects so that they may be garbage collected.
    */
    final WeakReference[] extraConnections;

    /** 
     * Last time when a free connection has been checked out.
     */
    final long[] freeConnectionLastTime;

    /**
     * Last time when an extra connection has been checked out.
     */
    final long[] extraConnectionLastTime;

    /** 
     * JDBC-conformant database connection URL. It should start with "jdbc:".
     */
    final String databaseURL;

    /** 
     * Name of the user connecting the database.
     */
    final String databaseUser;

    /** 
     * Password of the user connecting the database.
     */
    final String databasePassword;

    /** 
     * Number of connections that should be set up in the initialization.
     */
    final int initialConnections;

    /** 
     * Maximum number of database connections allowed.
     */
    final int maximumConnections;

    /**
     * Maximum period of time in milliseconds to wait for an available
     * connection.
     */
    final int maximumWait;

    /** 
     * Maximum period of time in milliseconds for an idle connection object
     * to be reused.
     */
    final int maximumIdle;

    /** 
     * Number of connections available in the initial database connection pool.
     */
    int initialCount;

    /** 
     * Number of extra connections available on the extra connection pool.
     */
    int extraCount;

    /** 
     * Total number of connections available in the pool.
     */
    int totalCount;

    /**
     * Number of connections created.
     */
    int createdCount;

    /**
     * Number of connections in use
     */
    int usedCount;

    /**
     * Transaction isolation level.
     */
    int transactionIsolationLevel;

    class CloseConnectionThread extends Thread {
        final Connection connection;

        public CloseConnectionThread(Connection connection) {
            this.connection = connection;
        }

        public void run() {
            try {
                if (connection != null) {
                    connection.close();
                }
            }
            catch (SQLException e) {
                logger.error(ErrorMessages.getMessage(
                    ErrorMessages.ERR_DB_CANNOT_CLOSE_CONN, e));
            }
        }
    }

    /**
     * Constructor.
     *
     * @param databaseURL         URL of the database to be connected to in
     *                            the form jdbc:subprotocol:subname.
     * @param databaseUser        User name the database user on whose behalf
     *                            the connection is being made.
     * @param databasePassword    Password of the database user.
     * @param initialConnections  Number of database connections in the database
     *                            connection pool after initialization
     * @param maximumConnections  Maximum number of concurrent database
     *                            connections allowed in the connection pool.
     * @param maximumWait         Maximum number of milliseconds to wait for an
     *                            free connection.
     * @param maximumIdle     Maximum period of time in milliseconds for an 
     *                            idle connection object to be reused.
     * @throws DbConnectionPoolException 
     */
    DbConnectionPool(String databaseURL, String databaseUser,
                     String databasePassword, int initialConnections,
                     int maximumConnections, int maximumWait,
                     int maximumIdle, int transactionIsolationLevel)
        throws DbConnectionPoolException {

        logger.debug("=> DbConnectionPool.DbConnectionPool");
 
        if (initialConnections < 0) {
            String err = "Initial database connections "
                + "must be greater than zero";
            err = ErrorMessages.getMessage(
                ErrorMessages.ERR_DB_INVALID_PARAM, err);
            logger.error(err);
            throw new DbConnectionPoolException(err);
        }
        if (initialConnections > maximumConnections) {
            String err = "Initial database connections "
                + "must be smaller than maximum connections";
            err = ErrorMessages.getMessage(
                ErrorMessages.ERR_DB_INVALID_PARAM, err);
            logger.error(err);
            throw new DbConnectionPoolException(err);
        }

        this.databaseURL = databaseURL;
        this.databaseUser = databaseUser;
        this.databasePassword = databasePassword;
        this.initialConnections = initialConnections;
        this.maximumConnections = maximumConnections;
        this.maximumWait = maximumWait;
        this.maximumIdle = maximumIdle;
        this.transactionIsolationLevel = transactionIsolationLevel;

        // Initializes the connection pool
        this.createdCount = 0;
        int numExtraConnections = maximumConnections - initialConnections;
        freeConnections = new Connection[initialConnections];
        extraConnections = new WeakReference[numExtraConnections];
        freeConnectionLastTime = new long[initialConnections];
        extraConnectionLastTime = new long[numExtraConnections];
        init();
        this.usedCount = 0;

        logger.debug("<= DbConnectionPool.DbConnectionPool");
    }

    /** 
     * Get database connection directly without going through connection pool. 
     * 
     * @return Database connection.
     * @throws DbConnectionPoolException 
     */
    synchronized Connection getRawConnection() 
        throws DbConnectionPoolException {
        logger.debug("=> DbConnectionPool.getRawConnection");
        Connection conn = createConnection();
        logger.debug("<= DbConnectionPool.getRawConnection");
        return conn;
    }

    /** 
     * Return a connection created by <code>getRawConnection()</code>.
     * 
     * @param connection    Database connection previously obtained through
     *                      getRawConnection() function.
     * @throws DbConnectionPoolException 
     */
    synchronized void freeRawConnection(Connection connection)
        throws DbConnectionPoolException {
        logger.debug("=> DbConnectionPool.freeRawConnection");
        new CloseConnectionThread(connection).start();
        logger.debug("<= DbConnectionPool.freeRawConnection");
    }        

    /** 
     * Get database connection from the connection pool. If all the connections
     * in the connection pool have been used, new connections will be 
     * established as long as the limit imposed by maximumConnections is 
     * observed. Otherwise <code>DbConnectionPoolException</code> will be 
     * thrown.
     * 
     * @return Database connection.
     * @throws DbConnectionPoolException 
     */
    synchronized Connection getConnection() throws DbConnectionPoolException {
        return getConnection(false);
    }

    /** 
     * Get database connection from the connection pool. If all the connections
     * in the connection pool have been used, new connections will be 
     * established as long as the limit imposed by maximumConnections is 
     * observed. Otherwise <code>DbConnectionPoolException</code> will be 
     * thrown.
     * 
     * @param requiresNew Specify true to create new connection; false to 
     *                    retrieve from connection pool.
     * @return Database connection.
     * @throws DbConnectionPoolException 
     */
    synchronized Connection getConnection(boolean requiresNew) 
        throws DbConnectionPoolException {

        logger.debug("=> DbConnectionPool.getConnection");

        final long startTime = System.currentTimeMillis();
        long endTime = startTime;

        while ((endTime - startTime) < maximumWait) {
            if (initialCount > 0) {
                Connection connection;
                initialCount--;
                if (freeConnectionLastTime[initialCount] >=
                    System.currentTimeMillis() - maximumIdle) {
                    connection = freeConnections[initialCount];
                    if (requiresNew) {
                        new CloseConnectionThread(connection).start();
                        connection = createConnection();
                    }
                }
                else {
                    connection = createConnection();
                    new CloseConnectionThread
                        (freeConnections[initialCount]).start();
                }
                freeConnections[initialCount] = null;
                usedCount++;
                logger.debug("<= DbConnectionPool.getConnection");
                return connection;
            }
            else {
                while (extraCount > 0) {
                    extraCount--;
                    final WeakReference reference =
                        extraConnections[extraCount];
                    Connection connection = (Connection)reference.get();
                    if (connection != null) {
                        // the referenced object is still valid, check idle time
                        if (extraConnectionLastTime[extraCount] <
                            System.currentTimeMillis() - maximumIdle) {
                            logger.debug(
                                "Connection expired, creating a new one.");
                            new CloseConnectionThread(connection).start();
                            connection = createConnection();
                        }
                        else if (requiresNew) {
                            logger.debug(
                                "New connection requsted, creating a new one.");
                            new CloseConnectionThread(connection).start();
                            connection = createConnection();
                        }
                        usedCount++;
                        logger.debug("<= DbConnectionPool.getConnection");
                        return connection;
                    }
                    else {
                        // the referenced object has been garbage-collected
                        totalCount--;
                    }
                }

                if (totalCount < maximumConnections) {
                    totalCount++;
                    Connection conn = createConnection();
                    usedCount++;
                    logger.debug("<= DbConnectionPool.getConnection");
                    return conn;
                }

                try {
                    wait(maximumWait);
                }
                catch (InterruptedException ie) {
                    endTime = System.currentTimeMillis();
                    while ((endTime - startTime) < maximumWait) {
                        try {
                            wait(maximumWait - (endTime - startTime));
                            break;
                        }
                        catch (InterruptedException ie2) {}
                        endTime = System.currentTimeMillis();
                    }
                }
            }
            endTime = System.currentTimeMillis();
        }

        String err = ErrorMessages.getMessage(
            ErrorMessages.ERR_DB_CANNOT_ALLOCATE_CONN, 
            "give up after " + String.valueOf(maximumWait) + " milliseconds");
        logger.error(err);
        throw new DbConnectionPoolException(err);
    }

    /** 
     * Return a connection back to the database pool.
     * 
     * @param connection    Database connection previously obtained through
     *                      getConnection() function.
     * @param isGood        true if the connection has been used to execute
     *                      queries successfully; false if any exceptions
     *                      occurred.
     * @throws DbConnectionPoolException 
     */
    synchronized void freeConnection(Connection connection, boolean isGood)
        throws DbConnectionPoolException {

        logger.debug("=> DbConnectionPool.freeConnection");
        
        final long lastTime = (isGood ? System.currentTimeMillis() : 0);
        if (initialCount < initialConnections) {
            freeConnections[initialCount] = connection;
            freeConnectionLastTime[initialCount] = lastTime;
            ++initialCount;
        }
        else {
            if ((extraCount + initialConnections) < maximumConnections) {
                extraConnections[extraCount] = new WeakReference(connection);
                extraConnectionLastTime[extraCount] = lastTime;
                ++extraCount;
            }
            else {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_DB_POOL_OVERFLOW);
                logger.error(err);
                throw new DbConnectionPoolException(err);
            }
        }
        notify();
        usedCount--;

        logger.debug("<= DbConnectionPool.freeConnection");
    }

    /** 
     * Initializes the connection pool by establishing a specified number of
     * database connections.
     * 
     * @throws DbConnectionPoolException 
     */
    synchronized void init() throws DbConnectionPoolException {
        logger.debug("=> DbConnectionPool.init");
        logger.info("Initializing DB connection pool: "
            + "creating database connections");
    
        for (int i=0 ; i<initialConnections ; i++) {
            freeConnections[i] = createConnection();
            freeConnectionLastTime[i] = System.currentTimeMillis();
        }

        logger.info(String.valueOf(initialConnections) 
            + " initial connections are created");

        totalCount = initialCount = initialConnections;
        extraCount = 0;

        logger.debug("<= DbConnectionPool.init");
    }

    /** 
     * Closes all database connections.
     * 
     * @throws DbConnectionPoolException 
     */
    synchronized void shutDown() throws DbConnectionPoolException {

        logger.debug("=> DbConnectionPool.shutDown");
        logger.info("Clearing up DB connection pool: "
            + "closing database connections");

        int closed = 0;
        for (int i=0 ; i<initialConnections ; i++) {
            if (freeConnections[i] == null) {
                continue;
            }

            new CloseConnectionThread(freeConnections[i]).start();
            freeConnections[i] = null;
            closed++;
        }
        logger.info(String.valueOf(closed) + " initial connections are "
                     + "explicitly closed");
        closed = 0;
        int garbageCollected = 0;
        for (int i=0 ; i<(maximumConnections - initialConnections) ; i++) {
            final WeakReference reference = extraConnections[i];
            extraConnections[i] = null;
            if (reference == null) {
                continue;
            }
            final Object connection = reference.get();
            if (connection == null) {
                garbageCollected++;
                continue;
            }

            new CloseConnectionThread((Connection)connection).start();
            closed++;
        }
        logger.info(String.valueOf(closed) + " extra connections are "
                     + "explicitly closed while " + String.valueOf
                     (garbageCollected) + " are garbage collected");

        logger.debug("<= DbConnectionPool.shutDown");
    }

    /** 
     * Create a new database connection.
     * 
     * @return Database connection object.
     * @throws DbConnectionPoolException 
     */
    private Connection createConnection() throws DbConnectionPoolException {
        logger.debug("=> DbConnectionPool.createConnection");
        try {
            final Connection connection = DriverManager.
                getConnection(databaseURL, databaseUser, databasePassword);
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(transactionIsolationLevel);
            createdCount++;
            logger.debug("<= DbConnectionPool.createConnection");
            return connection;
        }
        catch (SQLException sqle) {
            logger.error(ErrorMessages.getMessage(
                ErrorMessages.ERR_DB_CANNOT_CREATE_CONN, sqle));
            throw new DbConnectionPoolException(ErrorMessages.getMessage(
                ErrorMessages.ERR_DB_CANNOT_CREATE_CONN));
        }
    }

    /** 
     * Closes all the current connection objects and initializes the connection
     * pool again.
     * 
     * @throws DbConnectionPoolException 
     */
    synchronized void reset() throws DbConnectionPoolException {
        logger.debug("=> DbConnectionPool.reset");
        shutDown();
        init();
        logger.debug("<= DbConnectionPool.reset");
    }
}
