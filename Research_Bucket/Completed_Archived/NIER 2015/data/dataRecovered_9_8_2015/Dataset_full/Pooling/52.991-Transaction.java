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
 * $Header: /cvsroot/sino/ebxmlms/src/hk/hku/cecid/phoenix/message/handler/Transaction.java,v 1.18 2004/01/05 11:24:09 bobpykoon Exp $
 *
 * Code authored by:
 *
 * frankielam [2003-01-10]
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

import hk.hku.cecid.phoenix.message.packaging.EbxmlMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.log4j.Logger;
/** 
 * Transaction class representing a group of operations as an atomic unit.
 * 
 * @author   Frankie Lam
 * @version  1.0
 */
public class Transaction {

    static Logger logger = Logger.getLogger(Transaction.class); 

    // Monitor for locking access to Object-associated information. Possible
    // objects include message IDs and ApplicationContexts
    static final class ObjectMonitor {
        /* Hash map storing Object-Message monitor mapping.
           This is used as a substitute of the first phase lock in 2-phase
           locking model and it must be obtained before accessing the database
           for the information on this row (specifically MessageStore table).
        */
        private static final HashMap objectTable = new HashMap();

        private boolean locked;

        private int lockCount;

        ObjectMonitor() {
            locked = false;
            lockCount = 0;
        }

        static void lock(Object id) {
            for (boolean locked=false ; !locked ; ) {
                ObjectMonitor monitor = (ObjectMonitor)objectTable.get(id);
                if (monitor == null) {
                    synchronized (ObjectMonitor.class) {
                        monitor = (ObjectMonitor)objectTable.get(id);
                        if (monitor == null) {
                            monitor = new ObjectMonitor();
                            objectTable.put(id, monitor);
                            locked = true;
                        }
                    }
                }
                locked = monitor.lock(locked);
            }
            return;
        }

        static void unlock(Object id) {
            ObjectMonitor monitor = (ObjectMonitor)objectTable.get(id);
            if (monitor == null) {
                logger.error(ErrorMessages.getMessage(
                    ErrorMessages.ERR_TX_INCONSISTENT_LOCK, "program bug"));
                return;
            }
            monitor.unlockAndRemove(id);
        }

        synchronized boolean lock(boolean isNewMonitor) {
            if (!isNewMonitor && lockCount == 0) {
                return false;
            }

            lockCount++;
            while (locked) {
                try {
                    this.wait();
                }
                catch (InterruptedException ie) {}
            }
            return (locked = true);
        }

        synchronized void unlockAndRemove(Object id) {
            locked = false;
            lockCount--;
            if (lockCount > 0) {
                this.notify();
            }
            else {
                objectTable.remove(id);
            }
        }
    }

    private static Object txCounterLock = new Object();
    private static int txCounter = 0;

    private final DbConnectionPool dbConnectionPool;
    private final ArrayList filenameList;
    private final ArrayList persistenceList;
    private final ArrayList lockList;
    private final ArrayList threadList;
    private final ArrayList deliveryRecordList;
    private final int txID;
    private boolean requiresNew = false;
    private Connection connection = null;

    public Transaction(DbConnectionPool dbConnectionPool) {
        synchronized (txCounterLock) {
            this.txID = txCounter++;
        }
        this.dbConnectionPool = dbConnectionPool;
        this.filenameList = new ArrayList();
        this.lockList = new ArrayList();
        this.threadList = new ArrayList();
        this.deliveryRecordList = new ArrayList();
        this.persistenceList = new ArrayList();
    }

    public void finalize() {
        if (connection == null) {
            return;
        }

        try {
            logger.error(ErrorMessages.getMessage(
                ErrorMessages.ERR_TX_INCONSISTENT_LOCK, "program bug: "
                    + "transaction not committed or rolled back properly"));
            rollback();
        }
        catch (TransactionException e) {
            logger.error(ErrorMessages.getMessage(
                ErrorMessages.ERR_TX_CANNOT_ROLLBACK, e));
        }

    }

    /** 
     * Compile SQL statements using JDBC driver.
     * 
     * @param sql   SQL statement in string.
     * @return <code>PreparedStatement</code> object.
     */
    public PreparedStatement prepareStatement(String sql) 
        throws SQLException, TransactionException {
        checkConnection();
        return connection.prepareStatement(sql);
    }

    /** 
     * Get the <code>Connection</code> object in this transaction. 
     * 
     * @return <code>Connection</code> object.
     * @throws TransactionException 
     */
    public Connection getConnection() throws TransactionException {
        checkConnection();
        return connection;
    }

    /** 
     * Executes changes to the database. 
     * 
     * @param updates
     * @throws TransactionException 
     */
    public void executeUpdate(PreparedStatement [] updates) 
        throws TransactionException {

        checkConnection();

        // Execute the statements
        try {
            for (int i = 0; i < updates.length; i++) {
                updates[i].executeUpdate();
            }
        }
        catch (SQLException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_DB_CANNOT_WRITE, e);
            logger.warn(err);
            throw new TransactionException(err);
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new TransactionException(err);
        }
    }

    /** 
     * Place a lock on a specific message Id.
     * 
     * @param id Object to be locked.
     */
    public synchronized void lock(Object id) {
        if (lockList.contains(id)) {
            return;
        }
        ObjectMonitor.lock(id);
        lockList.add(id);
    }
    
    /**
     * store the persistence object to the transaction,
     * will be removed if the transaction rollback.
     * @param name the name of the persistence object
     * @param handler the persistence handler of the persistence object
     */
    public void storePersistenceObject(String name,
            PersistenceHandler handler) {
        persistenceList.add(new PersistenceObject(handler, name));
    }
    
    /**
     * remove the persistence object from the transaction,
     * the object will not be affect if the transaction is rollback or commit.
     * @param name the name of the persistence object
     * @param handler the persistence handler of the persistence object
     */
    public void removePersistenceObject(String name,
            PersistenceHandler handler) {
        persistenceList.remove(new PersistenceObject(handler, name));
    }

    /** 
     * Store an ebxml message to the file system.
     * 
     * @param ebxmlMessage <code>EbxmlMessage</code> object to be written.
     * @return The full path of the file.
     * @throws TransactionException 
     */
    public String store(EbxmlMessage ebxmlMessage) throws TransactionException {
        String filename = null;
        try {
            filename = DirectoryManager.store(ebxmlMessage);
            filenameList.add(filename);
            return filename;
        }
        catch (MessageServerException e) {
            // error already logged
            throw new TransactionException(e.getMessage());
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            throw new TransactionException(err);
        }
    }

    /** 
     * Store an object to the file system.
     * 
     * @param obj <code>Object</code> to be written to file system.
     * @throws TransactionException 
     */
    public void storeObject(Object obj, String filename) 
        throws TransactionException {

        try {
            final FileOutputStream out = new FileOutputStream(filename);
            final ObjectOutputStream objectStream = new ObjectOutputStream(out);
            objectStream.writeObject(obj);
            objectStream.flush();
            out.flush();
            objectStream.close();
            out.close();

            filenameList.add(filename);
        }
        catch (IOException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_FILE_IO_ERROR, e,
                "cannot store object to file <" + filename + ">");
            throw new TransactionException(err);
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            throw new TransactionException(err);
        }
    }

    public void storeFileName(String filename) {
        filenameList.add(filename);
    }

    public void removeFileName(String filename) {
        filenameList.remove(filename);
    }

    public Object readObject(String filename) throws TransactionException {
        try {
            final FileInputStream in = new FileInputStream(filename);
            final ObjectInputStream objectStream =
                new ObjectInputStream(in);
            final Object obj = objectStream.readObject();
            objectStream.close();
            in.close();

            return obj;
        }
        catch (IOException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_FILE_IO_ERROR, e,
                "cannot read object from file <" + filename + ">");
            logger.error(err);
            throw new TransactionException(err);
        }
        catch (ClassNotFoundException e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_FILE_IO_ERROR, e,
                "cannot read object from file <" + filename + ">");
            logger.error(err);
            throw new TransactionException(err);
        }
        catch (Exception e) {
            String err = ErrorMessages.getMessage(
                ErrorMessages.ERR_HERMES_UNKNOWN_ERROR, e);
            logger.error(err);
            throw new TransactionException(err);
        }
    }

    public void addThread(Thread t) {
        threadList.add(t);
    }

    public void addDeliveryRecord(DeliveryRecord record, int seqNo) {
        if (seqNo < 0) {
            deliveryRecordList.add(record);
        }
        else {
            ArrayList list = new ArrayList();
            list.add(record);
            list.add(new Integer(seqNo));
            deliveryRecordList.add(list);
        }
    }

    /** 
     * Commit all the changes. Since file-system changes are immediate, only
     * database-level commit is carried out. If error occurs during commit, all
     * the changes will be rolled back automatically.
     * 
     * @throws TransactionException 
     */
    public void commit() throws TransactionException {
        logger.debug("=> Transaction.commit (txID: #" + txID + ")");

        TransactionException exception = null;
        if (connection != null) {
            try {
                connection.commit();
            }
            catch (SQLException e) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_DB_CANNOT_COMMIT_CHANGE, e);
                logger.error(err);
                try {
                    rollback();
                    logger.debug("changes rolled back");
                }
                catch (TransactionException e2) {
                    String err2 = ErrorMessages.getMessage(
                        ErrorMessages.ERR_DB_CANNOT_ROLLBACK_CHANGE, e2);
                    logger.error(err2);
                }
                logger.debug("<= Transaction.commit");
                throw new TransactionException(err);
            }
            
            if (!threadList.isEmpty()) {
                Iterator it = threadList.iterator();
                while (it.hasNext()) {
                    ((Thread)it.next()).start();
                }
                threadList.clear();
            }

            if (!lockList.isEmpty()) {
                Iterator it = lockList.iterator();
                while (it.hasNext()) {
                    ObjectMonitor.unlock(it.next());
                }
                lockList.clear();
            }

            try {
                dbConnectionPool.freeConnection(connection, true);
            }
            catch (DbConnectionPoolException e) {
                // error already logged
                exception = new TransactionException(e.getMessage());
            }
            finally {
                connection = null;
                filenameList.clear();
                deliveryRecordList.clear();
                persistenceList.clear();
            }
        }

        logger.debug("<= Transaction.commit");
        if (exception != null) {
            throw exception;
        }
    }

    /** 
     * Rollback all database and file system changes. The connection object is 
     * returned to the connection pool before this function exits.
     * 
     * @throws TransactionException 
     */
    public void rollback() throws TransactionException {
        logger.debug("=> Transaction.rollback (txID: #" + txID + ")");

        TransactionException exception = null;
        if (connection != null) {
            String msg = "";
            try {
                connection.rollback();
            }
            catch (SQLException e) {
                String err = ErrorMessages.getMessage(
                    ErrorMessages.ERR_DB_CANNOT_ROLLBACK_CHANGE, e);
                logger.error(err);
                exception = new TransactionException(err);
            }
            finally {
                try {
                    dbConnectionPool.freeConnection(connection, false);
                }
                catch (DbConnectionPoolException e2) {
                    // error already logged
                }
                finally {
                    connection = null;
                    requiresNew = true;
                }
            }
        }

        threadList.clear();

        for (Iterator i=lockList.iterator() ; i.hasNext() ; ) {
            ObjectMonitor.unlock(i.next());
        }
        lockList.clear();

        for (Iterator i=deliveryRecordList.iterator() ; i.hasNext() ; ) {
            Object obj = i.next();
            if (obj instanceof DeliveryRecord) {
                ((DeliveryRecord) obj).decLastDelivered();
            }
            else if (obj instanceof ArrayList) {
                DeliveryRecord record = (DeliveryRecord)
                    ((ArrayList) obj).get(0);
                int seqNo = ((Integer) ((ArrayList) obj).get(1)).
                    intValue();
                record.removeUndelivered(seqNo);
            }
        }
        deliveryRecordList.clear();

        // Delete the files
        for (Iterator i=filenameList.iterator() ; i.hasNext() ; ) {
            String filename = (String) i.next();
            File file = new File(filename);
            if (file.delete() == false) {
                file.deleteOnExit();
            }
        }
        filenameList.clear();
        
        // Delete the persistence objects
        for (Iterator i=filenameList.iterator() ; i.hasNext() ; ) {
            PersistenceObject obj = (PersistenceObject) i.next();
            try {
                obj.getPersistenceHandler().removeObject(obj.getName());
            } catch (PersistenceException e) {
                String err = ErrorMessages.getMessage(
                        ErrorMessages.ERR_TX_CANNOT_ROLLBACK, e);
                logger.error(err);
                exception = new TransactionException(err);
                
            }
        }

        logger.debug("<= Transaction.rollback");
        if (exception != null) {
            throw exception;
        }
    }

    /** 
     * Check if current connection object is valid and construct / check out a
     * new one from connection pool if necessary.
     * 
     * @throws TransactionException 
     */
    private void checkConnection() throws TransactionException {
        if (requiresNew) {
            requiresNew = false;
            try {
                if (connection != null) {
                    dbConnectionPool.freeConnection(connection, false);
                }
            }
            catch (DbConnectionPoolException e) {}

            try {
                connection = dbConnectionPool.getConnection(true);
            }
            catch (DbConnectionPoolException e) {
                throw new TransactionException(e.getMessage());
            }
        }
        else if (connection == null) {
            try {
                connection = dbConnectionPool.getConnection();
            }
            catch (DbConnectionPoolException e) {
                throw new TransactionException(e.getMessage());
            }
        }
    }
    
    /**
     * PersistenceObject is the class to store on the persistence list
     * 
     * @author pykoon
     */
    private class PersistenceObject {
        
        /**
         * The PersistenceHandler associate with the Persistence object
         */
        private PersistenceHandler handler;
        
        /**
         * The name of the Persistence object
         */
        private String name;
        
        /**
         * Construct the Persistence Object
         * @param handler the PersistecneHandler
         * @param name the name of the Persistence Object
         */
        public PersistenceObject(PersistenceHandler handler, String name) {
            this.name = name;
            this.handler = handler;
        }
        
        /**
         * get the PersistenceHandler
         * @return the PersistenceHandler
         */
        public PersistenceHandler getPersistenceHandler() {
            return handler;
        }
        
        /**
         * get the name of Persistence Object 
         * @return the name of the Persistence Object
         */
        public String getName() {
            return name;
        }
        
        public boolean equals(Object a) {
            if (a instanceof PersistenceObject) {
                PersistenceObject obj = (PersistenceObject) a;
                return obj.handler == handler && name.equals(obj.name);
            }
            return false;
        }
    }
}
