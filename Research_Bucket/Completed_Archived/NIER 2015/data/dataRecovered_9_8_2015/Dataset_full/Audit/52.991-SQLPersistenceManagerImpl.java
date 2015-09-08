/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * ====================================================================
 */

package com.sun.ebxml.registry.persistence.rdb;

import com.sun.ebxml.registry.RegistryException;
import com.sun.ebxml.registry.util.RegistryProperties;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.oasis.ebxml.registry.bindings.query.ResponseOption;
import org.oasis.ebxml.registry.bindings.query.types.ReturnTypeType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.User;



/**
 * Class Declaration for Class1
 * @see
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 */
public class SQLPersistenceManagerImpl implements com.sun.ebxml.registry.persistence.PersistenceManager {
    
    private final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(this.getClass());
    
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */
    /*# private SQLPersistenceManagerImpl _sqlPersistenceManagerImpl; */
    private static SQLPersistenceManagerImpl instance = null;
    
    /**
     *
     * @associates <{com.sun.ebxml.registry.persistence.rdb.ExtrinsicObjectDAO}>
     */
    String databaseURL = null;
    
    java.sql.DatabaseMetaData metaData = null;
    
    RegistryObjectDAO roDAO = new RegistryObjectDAO();
    RegistryEntryDAO reDAO = new RegistryEntryDAO();
    private ClassificationSchemeDAO classificationSchemeDAO = new
    ClassificationSchemeDAO();
    private String driver;
    private String user;
    private String password;
    private boolean useConnectionPool;
    private ConnectionPool connectionPool;
    
    private static HashMap tableNameMap = new HashMap();
    
    static {
        tableNameMap.put("user", "user_");
        tableNameMap.put("name", "name_");
        tableNameMap.put("classificationscheme", "ClassScheme");
    }
    
    private static HashMap columnNameMap = new HashMap();
    
    static {
        columnNameMap.put("number", "number_");
        columnNameMap.put("name", "name_");
        columnNameMap.put("user", "user_");
        columnNameMap.put("timestamp", "timestamp_");
    }
    
    private SQLPersistenceManagerImpl() {
        loadUsernamePassword();
        loadDatabaseDriver();
        constructDatabaseURL();
        if (RegistryProperties.getInstance().getProperty("ebxmlrr.persistence.rdb.useConnectionPooling")
        .equalsIgnoreCase("true")) {
            useConnectionPool = true;
            createConnectionPool();
        }
        else {
            useConnectionPool = false;
        }
    }
    
    /** Look up the driver name and load the database driver */
    private void loadDatabaseDriver() {
        
        try {
            driver = RegistryProperties.getInstance().getProperty("ebxmlrr.persistence.rdb.databaseDriver");
            Class.forName(driver);
            
            log.debug("Loaded jdbc driver: " + driver);
            
        } catch (ClassNotFoundException e) {
            log.error(e);
        }
    }
    
    /** Lookup up the db URL fragments and form the complete URL */
    private void constructDatabaseURL() {
        databaseURL = RegistryProperties.getInstance().getProperty("ebxmlrr.persistence.rdb.databaseURL");
        
        log.debug("dbURL = '" + databaseURL + "'");
    }
    
    /**Load the username and password for database access*/
    private void loadUsernamePassword() {
        user = RegistryProperties.getInstance().getProperty("ebxmlrr.persistence.rdb.databaseUser");
        password = RegistryProperties.getInstance().getProperty("ebxmlrr.persistence.rdb.databaseUserPassword");
    }
    
    private void createConnectionPool() {
        RegistryProperties registryProperties = RegistryProperties.getInstance();
        String initialSize = registryProperties.getProperty("ebxmlrr.persistence.rdb.pool.initialSize");
        int initConns = 1;
        if (initialSize != null) {
            initConns = Integer.parseInt(initialSize);
        }
        String maxSize = registryProperties.getProperty("ebxmlrr.persistence.rdb.pool.maxSize");
        int maxConns = 1;
        if (maxSize != null) {
            maxConns = Integer.parseInt(maxSize);
        }
        String connectionTimeOut = registryProperties.getProperty("ebxmlrr.persistence.rdb.pool.connectionTimeOut");
        int timeOut = 0;
        if (connectionTimeOut != null) {
            timeOut = Integer.parseInt(connectionTimeOut);
        }
        connectionPool = new ConnectionPool("ConnectionPool", databaseURL, user,
        password, maxConns, initConns, timeOut);
    }
    
    /**
     * Get a database connection. The connection is of autocommit off and with
     * transaction isolation level "transaction read committed"
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection connection = null;
            
            if (useConnectionPool) {
                connection = connectionPool.getConnection();
            }
            else {
                // create connection directly
                connection = java.sql.DriverManager.getConnection(databaseURL, user
                , password);
            }
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            return connection;
        }
        catch (SQLException e) {
            System.err.println("Error: Could not create Connection to database. Check if database is already running in another program (e.g. registry servlet in tomcat)");
            throw e;
        }
    }
    
    private void releaseConnection(Connection conn) throws SQLException {
        if (!conn.isClosed() && !useConnectionPool) {
            conn.close();
        }
        else if(useConnectionPool) {
            connectionPool.freeConnection(conn);
        }
    }
    
    public static SQLPersistenceManagerImpl getInstance() {
        if (instance == null) {
            synchronized(SQLPersistenceManagerImpl.class) {
                if (instance == null) {
                    instance = new SQLPersistenceManagerImpl();
                }
            }
        }
        return instance;
    }
    
    //Sort objects by their type.
    private void sortRegistryObjects(ArrayList registryObjects,
    ArrayList associations,
    ArrayList auditableEvents,
    ArrayList classifications,
    ArrayList schemes,
    ArrayList classificationNodes,
    ArrayList externalIds,
    ArrayList externalLinks,
    ArrayList extrinsicObjects,
    ArrayList organizations,
    ArrayList packages,
    ArrayList serviceBindings,
    ArrayList services,
    ArrayList specificationLinks,
    ArrayList users
    ) throws RegistryException {
        
        associations.clear();
        auditableEvents.clear();
        classifications.clear();
        schemes.clear();
        classificationNodes.clear();
        externalIds.clear();
        externalLinks.clear();
        extrinsicObjects.clear();
        organizations.clear();
        packages.clear();
        serviceBindings.clear();
        services.clear();
        specificationLinks.clear();
        users.clear();
        
        java.util.Iterator objIter = registryObjects.iterator();
        while (objIter.hasNext()) {
            RegistryObjectType obj = (RegistryObjectType)objIter.next();
            
            if (obj instanceof org.oasis.ebxml.registry.bindings.rim.Association) {
                associations.add(obj);
            }
            else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.AuditableEvent) {
                auditableEvents.add(obj);
            }
            else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.Classification) {
                classifications.add(obj);
            }
            else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ClassificationScheme) {
                schemes.add(obj);
            }
            else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ClassificationNode) {
                classificationNodes.add(obj);
            }
            else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ExternalIdentifier) {
                externalIds.add(obj);
            }
            else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ExternalLink) {
                externalLinks.add(obj);
            }
            else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ExtrinsicObject) {
                extrinsicObjects.add(obj);
            }
            else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.Organization) {
                organizations.add(obj);
            }
            else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.RegistryPackage) {
                packages.add(obj);
            }
            else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ServiceBinding) {
                serviceBindings.add(obj);
            }
            else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.Service) {
                services.add(obj);
            }
            else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.SpecificationLink) {
                specificationLinks.add(obj);
            }
            else if (obj instanceof User) {
                users.add(obj);
            }
            else {
                throw new RegistryException("Enexpected object type: " + obj.getClass().getName());
            }
        }
        
    }
    
    /**
     * Does a bulk insert of a heterogeneous Collection of RegistrObjects.
     *
     */
    public void insert(User user, ArrayList registryObjects) throws RegistryException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            insert(user, conn, registryObjects, true);
            releaseConnection(conn);
            
        } catch (SQLException e) {
            log.error(e);
            try	{
                if (!conn.isClosed()) {
                    conn.rollback();
                }
                releaseConnection(conn);
                throw new RegistryException(e);
            }
            catch(SQLException se) {
                throw new RegistryException(se);
            }
        } catch(RegistryException e) {
            log.error(e);
            try {
                if (!conn.isClosed()) {
                    conn.rollback();
                }
                releaseConnection(conn);
                throw e;
            }
            catch(SQLException se) {
                throw new RegistryException(se);
            }
        }
    }
    
    /**
     * Does a bulk insert of a heterogeneous Collection of RegistrObjects.
     *
     */
    public void insert(User user, Connection conn, ArrayList registryObjects, boolean doCommit) throws RegistryException {
        
        
        try {
            conn.setAutoCommit(false);
            
            ArrayList associations = new ArrayList();
            ArrayList auditableEvents = new ArrayList();
            ArrayList classifications = new ArrayList();
            ArrayList schemes = new ArrayList();
            ArrayList classificationNodes = new ArrayList();
            ArrayList externalIds = new ArrayList();
            ArrayList externalLinks = new ArrayList();
            ArrayList extrinsicObjects = new ArrayList();
            ArrayList organizations = new ArrayList();
            ArrayList packages = new ArrayList();
            ArrayList serviceBindings = new ArrayList();
            ArrayList services = new ArrayList();
            ArrayList specificationLinks = new ArrayList();
            ArrayList users = new ArrayList();
            
            sortRegistryObjects(registryObjects,
            associations,
            auditableEvents,
            classifications,
            schemes,
            classificationNodes,
            externalIds,
            externalLinks,
            extrinsicObjects,
            organizations,
            packages,
            serviceBindings,
            services,
            specificationLinks,
            users
            );
            
            if (associations.size() > 0) {
                AssociationDAO associationDAO = new AssociationDAO();
                associationDAO.insert(user, conn, associations);
            }
            
            if (auditableEvents.size() > 0) {
                AuditableEventDAO auditableEventDAO = new AuditableEventDAO();
                auditableEventDAO.insert(conn, auditableEvents);
            }
            
            if (classifications.size() > 0) {
                ClassificationDAO classificationDAO = new ClassificationDAO();
                classificationDAO.insert(user, conn, classifications);
            }
            
            if (schemes.size() > 0) {
                ClassificationSchemeDAO classificationSchemeDAO = new
                ClassificationSchemeDAO();
                classificationSchemeDAO.insert(user, conn, schemes);
            }
            
            if (classificationNodes.size() > 0) {
                ClassificationNodeDAO classificationNodeDAO = new ClassificationNodeDAO();
                classificationNodeDAO.insert(user, conn, classificationNodes);
            }
            
            if (externalIds.size() > 0) {
                ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO();
                externalIdentifierDAO.insert(user, conn, externalIds);
            }
            
            if (externalLinks.size() > 0) {
                ExternalLinkDAO externalLinkDAO = new ExternalLinkDAO();
                externalLinkDAO.insert(user, conn, externalLinks);
            }
            
            if (extrinsicObjects.size() > 0) {
                ExtrinsicObjectDAO extrinsicObjectDAO = new ExtrinsicObjectDAO();
                extrinsicObjectDAO.insert(user, conn, extrinsicObjects);
            }
            
            if (organizations.size() > 0) {
                OrganizationDAO organizationDAO = new OrganizationDAO();
                organizationDAO.insert(user, conn, organizations);
            }
            
            if (packages.size() > 0) {
                RegistryPackageDAO registryPackageDAO = new RegistryPackageDAO();
                registryPackageDAO.insert(user, conn, packages);
            }
            
            if (serviceBindings.size() > 0) {
                ServiceBindingDAO serviceBindingDAO = new ServiceBindingDAO();
                serviceBindingDAO.insert(user, conn, serviceBindings);
            }
            
            if (services.size() > 0) {
                ServiceDAO serviceDAO = new ServiceDAO();
                serviceDAO.insert(user, conn, services);
            }
            
            if (specificationLinks.size() > 0) {
                SpecificationLinkDAO specificationLinkDAO = new SpecificationLinkDAO();
                specificationLinkDAO.insert(user, conn, specificationLinks);
            }
            
            if (users.size() > 0) {
                UserDAO userDAO = new UserDAO();
                userDAO.insert(user, conn, users);
            }
            
            if (doCommit) {
                System.err.println("Now committing transaction");
                conn.commit();
                releaseConnection(conn);
            }
            
            
        }
        catch (SQLException e) {
            try	{
                if (!conn.isClosed()) {
                    conn.rollback();
                }
                releaseConnection(conn);
                throw new RegistryException(e);
            }
            catch(SQLException se) {
                throw new RegistryException(se);
            }
        }
    }
    
    /**
     * Does a bulk update of a heterogeneous Collection of RegistrObjects.
     *
     */
    public void update(User user, ArrayList registryObjects) throws RegistryException{
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            ArrayList associations = new ArrayList();
            ArrayList auditableEvents = new ArrayList();
            ArrayList classifications = new ArrayList();
            ArrayList schemes = new ArrayList();
            ArrayList classificationNodes = new ArrayList();
            ArrayList externalIds = new ArrayList();
            ArrayList externalLinks = new ArrayList();
            ArrayList extrinsicObjects = new ArrayList();
            ArrayList organizations = new ArrayList();
            ArrayList packages = new ArrayList();
            ArrayList serviceBindings = new ArrayList();
            ArrayList services = new ArrayList();
            ArrayList specificationLinks = new ArrayList();
            ArrayList users = new ArrayList();
            
            sortRegistryObjects(registryObjects,
            associations,
            auditableEvents,
            classifications,
            schemes,
            classificationNodes,
            externalIds,
            externalLinks,
            extrinsicObjects,
            organizations,
            packages,
            serviceBindings,
            services,
            specificationLinks,
            users
            );
            
            if (associations.size() > 0) {
                AssociationDAO associationDAO = new AssociationDAO();
                associationDAO.update(user, conn, associations);
            }
            if (auditableEvents.size() > 0) {
                // Should we allow update AuditableEvent?
                // AuditableEventDAO auditableEventDAO = new AuditableEventDAO();
                //auditableEventDAO.update(user, conn, auditableEvents);
            }
            if (classifications.size() > 0) {
                ClassificationDAO classificationDAO = new ClassificationDAO();
                classificationDAO.update(user, conn, classifications);
            }
            if (schemes.size() > 0) {
                ClassificationSchemeDAO classificationSchemeDAO = new ClassificationSchemeDAO();
                classificationSchemeDAO.update(user, conn, schemes);
            }
            if (classificationNodes.size() > 0) {
                ClassificationNodeDAO classificationNodeDAO = new ClassificationNodeDAO();
                classificationNodeDAO.update(user, conn, classificationNodes);
            }
            if (externalIds.size() > 0) {
                // ExternalId is no longer the first level, right?
                ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO();
                externalIdentifierDAO.update(user, conn, externalIds);
            }
            if (externalLinks.size() > 0) {
                ExternalLinkDAO externalLinkDAO = new ExternalLinkDAO();
                externalLinkDAO.update(user, conn, externalLinks);
            }
            if (extrinsicObjects.size() > 0) {
                ExtrinsicObjectDAO extrinsicObjectDAO = new ExtrinsicObjectDAO();
                extrinsicObjectDAO.update(user, conn, extrinsicObjects);
            }
            if (organizations.size() > 0) {
                OrganizationDAO organizationDAO = new OrganizationDAO();
                organizationDAO.update(user, conn, organizations);
            }
            if (packages.size() > 0) {
                RegistryPackageDAO registryPackageDAO = new RegistryPackageDAO();
                registryPackageDAO.update(user, conn, packages);
            }
            if (serviceBindings.size() > 0) {
                ServiceBindingDAO serviceBindingDAO = new ServiceBindingDAO();
                serviceBindingDAO.update(user, conn, serviceBindings);
            }
            if (services.size() > 0) {
                ServiceDAO serviceDAO = new ServiceDAO();
                serviceDAO.update(user, conn, services);
            }
            if (specificationLinks.size() > 0) {
                SpecificationLinkDAO specificationLinkDAO = new SpecificationLinkDAO();
                specificationLinkDAO.update(user, conn, specificationLinks);
            }
            if (users.size() > 0) {
                UserDAO userDAO = new UserDAO();
                userDAO.update(user, conn, users);
            }
            
            conn.commit();
            releaseConnection(conn);
            
        } catch (SQLException e) {
            try	{
                if (!conn.isClosed()) {
                    conn.rollback();
                }
                releaseConnection(conn);
                throw new RegistryException(e);
            }
            catch(SQLException se) {
                throw new RegistryException(se);
            }
        } catch(RegistryException e) {
            try {
                if (!conn.isClosed()) {
                    conn.rollback();
                }
                releaseConnection(conn);
                throw e;
            }
            catch(SQLException se) {
                throw new RegistryException(se);
            }
        }
    }
    
    /**
     * Update the status of specified objects to the specified status.
     *
     */
    public void updateStatus(User user, ArrayList registryObjectsIds, org.oasis.ebxml.registry.bindings.rim.types.StatusType status, org.oasis.ebxml.registry.bindings.rs.RegistryErrorList el) throws RegistryException {
        Connection connection = null;
        
        try {
            connection = getConnection();
            reDAO.updateStatusForHeterogeneousObjects(user, connection, registryObjectsIds, status, el);
            connection.commit();
            releaseConnection(connection);
        }
        catch (SQLException e) {
            try	{
                if (!connection.isClosed()) {
                    connection.rollback();
                }
                releaseConnection(connection);
                throw new RegistryException(e);
            }
            catch(SQLException se) {
                throw new RegistryException(se);
            }
        } catch(RegistryException e) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }
                releaseConnection(connection);
                throw e;
            }
            catch(SQLException se) {
                throw new RegistryException(se);
            }
        }
    }
    
    /**
     * Does a bulk delete of a heterogeneous Collection of RegistrObjects. If
     * any RegistryObject cannot be found, it will make no change to the
     * database and throw RegistryException
     *
     */
    public void delete(User user, ArrayList registryObjectIds) throws RegistryException{
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            
            if (registryObjectIds.size() > 0) {
                if (!roDAO.registryObjectsExist(connection, registryObjectIds)
                .isEmpty())	{
                    throw new RegistryException("Some objects not found, do nothing");
                }
                HashMap map = roDAO.sortIdsByObjectType(connection, registryObjectIds);
                java.util.Set objectTypes = map.keySet();
                java.util.Iterator iter = objectTypes.iterator();
                // System.err.println(registryObjectIds.size());
                AssociationDAO associationDAO = new AssociationDAO();
                AuditableEventDAO auditableEventDAO = new AuditableEventDAO();
                ClassificationDAO classificationDAO = new ClassificationDAO();
                ClassificationSchemeDAO classificationSchemeDAO = new ClassificationSchemeDAO();
                ClassificationNodeDAO classificationNodeDAO = new ClassificationNodeDAO();
                ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO();
                ExternalLinkDAO externalLinkDAO = new ExternalLinkDAO();
                ExtrinsicObjectDAO extrinsicObjectDAO = new ExtrinsicObjectDAO();
                OrganizationDAO organizationDAO = new OrganizationDAO();
                RegistryPackageDAO registryPackageDAO = new RegistryPackageDAO();
                ServiceBindingDAO serviceBindingDAO = new ServiceBindingDAO();
                ServiceDAO serviceDAO = new ServiceDAO();
                SpecificationLinkDAO specificationLinkDAO = new SpecificationLinkDAO();
                UserDAO userDAO = new UserDAO();
                
                while(iter.hasNext()) {
                    String objectType = (String)iter.next();
                    System.err.println("objectType: " + objectType);
                    if (objectType.equalsIgnoreCase("Association")) {
                        associationDAO.delete(user, connection, (ArrayList)(map.get(objectType)));
                    }
                    else if (objectType.equalsIgnoreCase("AuditableEvent")) {
                        auditableEventDAO.delete(user, connection, (ArrayList)(map.get(objectType)));
                    }
                    else if (objectType.equalsIgnoreCase("Classification")) {
                        classificationDAO.delete(user, connection, (ArrayList)(map.get(objectType)));
                    }
                    else if (objectType.equalsIgnoreCase("ClassificationScheme")) {
                        classificationSchemeDAO.delete(user, connection, (ArrayList)(map.get(objectType)));
                    }
                    else if (objectType.equalsIgnoreCase("ClassificationNode")) {
                        classificationNodeDAO.delete(user, connection, (ArrayList)(map.get(objectType)));
                    }
                    else if (objectType.equalsIgnoreCase("ExtrinsicObject")) {
                        extrinsicObjectDAO.delete(user, connection, (ArrayList)(map.get(objectType)));
                    }
                    else if (objectType.equalsIgnoreCase("ExternalIdentifier")) {
                        externalIdentifierDAO.delete(user, connection, (ArrayList)(map.get(objectType)));
                    }
                    else if (objectType.equalsIgnoreCase("ExternalLink")) {
                        externalLinkDAO.delete(user, connection, (ArrayList)(map.get(objectType)));
                    }
                    else if (objectType.equalsIgnoreCase("Organization")) {
                        organizationDAO.delete(user, connection, (ArrayList)(map.get(objectType)));
                    }
                    else if (objectType.equalsIgnoreCase("RegistryPackage")) {
                        registryPackageDAO.delete(user, connection, (ArrayList)(map.get(objectType)));
                    }
                    else if (objectType.equalsIgnoreCase("ServiceBinding")) {
                        serviceBindingDAO.delete(user, connection, (ArrayList)(map.get(objectType)));
                    }
                    else if (objectType.equalsIgnoreCase("Service")) {
                        serviceDAO.delete(user, connection, (ArrayList)(map.get(objectType)));
                    }
                    else if (objectType.equalsIgnoreCase("SpecificationLink")) {
                        specificationLinkDAO.delete(user, connection, (ArrayList)(map.get(objectType)));
                    }
                    else if (objectType.equalsIgnoreCase("User")) {
                        userDAO.delete(user, connection, (ArrayList)(map.get(objectType)));
                    }
                    else {
                        throw new RegistryException("Unexpected objectType "
                        + objectType);
                    }
                    
                } // end while
            }
            
            connection.commit();
            releaseConnection(connection);
            
        } catch (SQLException e) {
            try	{
                if (!connection.isClosed()) {
                    connection.rollback();
                }
                releaseConnection(connection);
                throw new RegistryException(e);
            }
            catch(SQLException se) {
                throw new RegistryException(se);
            }
        } catch(RegistryException e) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }
                releaseConnection(connection);
                throw e;
            }
            catch(SQLException se) {
                throw new RegistryException(se);
            }
        }
    }
    
    /**
     * Adds specified slots to specified object.
     *
     */
    public void addSlots(String objectId, ArrayList slots) throws RegistryException {
        Connection connection = null;
        try {
            connection = getConnection();
            //Now insert Slots for this object
            
            SlotDAO slotDAO = new SlotDAO();
            if (slots.size() > 0) {
                slotDAO.insert(connection, objectId, slots, false);
                connection.commit();
                releaseConnection(connection);
            }
            
        }
        catch (SQLException e) {
            try	{
                if (!connection.isClosed()) {
                    connection.rollback();
                }
                releaseConnection(connection);
                throw new RegistryException(e);
            }
            catch(SQLException se) {
                throw new RegistryException(se);
            }
        } catch(RegistryException e) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }
                releaseConnection(connection);
                throw e;
            }
            catch(SQLException se) {
                throw new RegistryException(se);
            }
        }
    }
    
    /**
     * Removes specified slots from specified object.
     *
     */
    public void removeSlots(String objectId, ArrayList slots) throws RegistryException {
        Connection connection = null;
        try {
            connection = getConnection();
            SlotDAO slotDAO = new SlotDAO();
            slotDAO.deleteByParentIdAndSlots(connection, objectId, slots);
            connection.commit();
            releaseConnection(connection);
        } catch (SQLException e) {
            try	{
                if (!connection.isClosed()) {
                    connection.rollback();
                }
                releaseConnection(connection);
                throw new RegistryException(e);
            }
            catch(SQLException se) {
                throw new RegistryException(se);
            }
        } catch(RegistryException e) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }
                releaseConnection(connection);
                throw e;
            }
            catch(SQLException se) {
                throw new RegistryException(se);
            }
        }
    }
    
    
    private java.sql.DatabaseMetaData getMetaData(Connection conn) throws SQLException {
        if (metaData == null) {
            metaData = conn.getMetaData();
        }
        return metaData;
    }
    
    /**
     * Executes an SQL Query.
     */
    public ArrayList executeSQLQuery(String sqlQuery, ResponseOption responseOption, String tableName, ArrayList objectRefs) throws RegistryException {
        ArrayList res=null;
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            java.sql.ResultSet rs = null;
            
            tableName = mapTableName(tableName);
            ReturnTypeType returnType = responseOption.getReturnType();
            boolean returnComposedObjects = responseOption.getReturnComposedObjects();
            
            //Check if user may have set an incorrect returnType.
            //If so fix silently per spec to the next less specific returnType.
            if (returnType.getType() == ReturnTypeType.REGISTRYENTRY_TYPE) {
                rs = getMetaData(conn).getColumns(null, null, tableName, "expiration");
                
                if (!(rs.next())) {
                    //Not a RegistryEntry. Make it a RegistryObject
                    returnType = ReturnTypeType.REGISTRYOBJECT;
                }
                rs.close();
            }
            
            java.sql.Statement stmt = conn.createStatement();
            
            System.err.println("Executing query: '" + sqlQuery + "'");
            rs = stmt.executeQuery(sqlQuery);
            
            
            java.util.Iterator iter = null;
            switch (returnType.getType()) {
                case ReturnTypeType.OBJECTREF_TYPE:
                    res = new ArrayList();
                    while(rs.next()) {
                        org.oasis.ebxml.registry.bindings.rim.ObjectRef or = new org.oasis.ebxml.registry.bindings.rim.ObjectRef();
                        String id = rs.getString(1);
                        or.setId(id);
                        res.add(or);
                    }
                    break;
                case ReturnTypeType.REGISTRYOBJECT_TYPE:
                    res = roDAO.getRegistryObjectList(conn, rs, responseOption);
                    break;
                case ReturnTypeType.REGISTRYENTRY_TYPE:
                    res = reDAO.getRegistryEntryList(conn, rs, responseOption);
                    break;
                case ReturnTypeType.LEAFCLASS_TYPE:
                case ReturnTypeType.LEAFCLASSWITHREPOSITORYITEM_TYPE:
                    /* left for debugging rs in future
                    while(rs.next()) {
                        String id = rs.getString("id");
                        System.err.println(id);
                    }
                     
                    rs.beforeFirst();
                     */
                    
                    res = getLeafObjectList(conn, rs, tableName, responseOption, objectRefs);
                    
                    /* left for debugging rs in future
                    rs.beforeFirst();
                    while(rs.next()) {
                        String id = rs.getString("id");
                        System.err.println(id);
                    }
                     */
                    break;
                default:
                    throw new RegistryException("Invalid returnType: " + returnType);
            }
            stmt.close();
            releaseConnection(conn);
        } catch (SQLException e) {
            try	{
                //Leave this in for now to help faster debugging during development.
                e.printStackTrace();
                
                if (conn != null) {
                    if (!conn.isClosed()) {
                        conn.rollback();
                    }
                    releaseConnection(conn);
                }
                throw new RegistryException(e);
            }
            catch(SQLException se) {
                throw new RegistryException(se);
            }
        } catch(RegistryException e) {
            try {
                if (!conn.isClosed()) {
                    conn.rollback();
                }
                releaseConnection(conn);
                throw e;
            }
            catch(SQLException se) {
                throw new RegistryException(se);
            }
        }
        return res;
    }
    
    private ArrayList getLeafObjectList(Connection conn, java.sql.ResultSet rs, String tableName, ResponseOption responseOption, ArrayList objectRefs) throws RegistryException {
        ArrayList res = null;
        
        if (tableName.equalsIgnoreCase(AssociationDAO.getTableNameStatic())) {
            AssociationDAO associationDAO = new AssociationDAO();
            res = associationDAO.getLeafObjectList(conn, rs, responseOption, objectRefs);
        }
        else if (tableName.equalsIgnoreCase(AuditableEventDAO.getTableNameStatic())) {
            AuditableEventDAO auditableEventDAO = new AuditableEventDAO();
            res = auditableEventDAO.getLeafObjectList(conn, rs, responseOption, objectRefs);
        }
        else if (tableName.equalsIgnoreCase(ClassificationDAO.getTableNameStatic())) {
            ClassificationDAO classificationDAO = new ClassificationDAO();
            res = classificationDAO.getLeafObjectList(conn, rs, responseOption, objectRefs);
        }
        else if (tableName.equalsIgnoreCase(ClassificationSchemeDAO.getTableNameStatic())) {
            ClassificationSchemeDAO classificationSchemeDAO = new ClassificationSchemeDAO();
            res = classificationSchemeDAO.getLeafObjectList(conn, rs, responseOption, objectRefs);
        }
        else if (tableName.equalsIgnoreCase(ClassificationNodeDAO.getTableNameStatic())) {
            ClassificationNodeDAO classificationNodeDAO = new ClassificationNodeDAO();
            res = classificationNodeDAO.getLeafObjectList(conn, rs, responseOption, objectRefs);
        }
        else if (tableName.equalsIgnoreCase(ExternalIdentifierDAO.getTableNameStatic())) {
            ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO();
            res = externalIdentifierDAO.getLeafObjectList(conn, rs, responseOption, objectRefs);
        }
        else if (tableName.equalsIgnoreCase(ExternalLinkDAO.getTableNameStatic())) {
            ExternalLinkDAO externalLinkDAO = new ExternalLinkDAO();
            res = externalLinkDAO.getLeafObjectList(conn, rs, responseOption, objectRefs);
        }
        else if (tableName.equalsIgnoreCase(ExtrinsicObjectDAO.getTableNameStatic())) {
            ExtrinsicObjectDAO extrinsicObjectDAO = new ExtrinsicObjectDAO();
            res = extrinsicObjectDAO.getLeafObjectList(conn, rs, responseOption, objectRefs);
        }
        else if (tableName.equalsIgnoreCase(OrganizationDAO.getTableNameStatic())) {
            OrganizationDAO organizationDAO = new OrganizationDAO();
            res = organizationDAO.getLeafObjectList(conn, rs, responseOption, objectRefs);
        }
        else if (tableName.equalsIgnoreCase(RegistryObjectDAO.getTableNameStatic())) {
            RegistryObjectDAO registryObjectDAO = new RegistryObjectDAO();
            res = registryObjectDAO.getLeafObjectList(conn, rs, responseOption, objectRefs);
        }
        else if (tableName.equalsIgnoreCase(RegistryPackageDAO.getTableNameStatic())) {
            RegistryPackageDAO registryPackageDAO = new RegistryPackageDAO();
            res = registryPackageDAO.getLeafObjectList(conn, rs, responseOption, objectRefs);
        }
        else if (tableName.equalsIgnoreCase(ServiceBindingDAO.getTableNameStatic())) {
            ServiceBindingDAO serviceBindingDAO = new ServiceBindingDAO();
            res = serviceBindingDAO.getLeafObjectList(conn, rs, responseOption, objectRefs);
        }
        else if (tableName.equalsIgnoreCase(ServiceDAO.getTableNameStatic())) {
            ServiceDAO serviceDAO = new ServiceDAO();
            res = serviceDAO.getLeafObjectList(conn, rs, responseOption, objectRefs);
        }
        else if (tableName.equalsIgnoreCase(SpecificationLinkDAO.getTableNameStatic())) {
            SpecificationLinkDAO specificationLinkDAO = new SpecificationLinkDAO();
            res = specificationLinkDAO.getLeafObjectList(conn, rs, responseOption, objectRefs);
        }
        else if (tableName.equalsIgnoreCase(UserDAO.getTableNameStatic())) {
            UserDAO userDAO = new UserDAO();
            res = userDAO.getLeafObjectList(conn, rs, responseOption, objectRefs);
        }
        
        return res;
    }
    
    /**
     * Returns ArrayList of ids of non-existent RegistryObject
     */
    public ArrayList registryObjectsExist(ArrayList ids) throws
    RegistryException {
        Connection conn = null;
        try {
            conn = getConnection();
            ArrayList notExist = roDAO.registryObjectsExist(conn, ids);
            releaseConnection(conn);
            return notExist;
        }
        catch (SQLException e) {
            log.error(e);
            try	{
                if (!conn.isClosed()) {
                    conn.rollback();
                }
                releaseConnection(conn);
                throw new RegistryException(e);
            }
            catch(SQLException se) {
                throw new RegistryException(se);
            }
        }
        catch(RegistryException e) {
            log.error(e);
            try {
                if (!conn.isClosed()) {
                    conn.rollback();
                }
                releaseConnection(conn);
                throw e;
            }
            catch(SQLException se) {
                throw new RegistryException(se);
            }
        }
    }
    
    /**
     * Gets the specified object using specified id and className
     *
     */
    public RegistryObjectType getRegistryObject(String id, String className)  throws RegistryException {
        RegistryObjectType ro = null;
        
        String tableName = mapTableName(className);
        String sqlQuery = "Select * from " + tableName + " WHERE id = '" + id + "' ";
        
        ResponseOption responseOption = new ResponseOption();
        responseOption.setReturnType(ReturnTypeType.LEAFCLASS);
        responseOption.setReturnComposedObjects(true);
        
        ArrayList objectRefs = new ArrayList();
        ArrayList al = executeSQLQuery(sqlQuery, responseOption, tableName, objectRefs);
        if (al.size() == 1) {
            ro = (RegistryObjectType)al.get(0);
        }
        
        return ro;
    }
    
    public String mapTableName(String name) {
        String newName = (String)tableNameMap.get(name.toLowerCase().trim());
        if (newName == null) {
            newName = name;
        }
        return newName;
    }
    
    public String mapColumnName(String name) {
        String newName = (String)columnNameMap.get(name.toLowerCase().trim());
        if (newName == null) {
            newName = name;
        }
        return newName;
    }
    
    /**
     * Get a HashMap with registry object id as key and owner id as value
     */
    public HashMap getOwnersMap(ArrayList ids) throws RegistryException {
        
        Connection conn = null;
        try {
            conn = getConnection();
            HashMap ownersMap =  roDAO.getOwnersMap(conn, ids);
            releaseConnection(conn);
            return ownersMap;
        }
        catch (SQLException e) {
            log.error(e);
            try	{
                if (!conn.isClosed()) {
                    conn.rollback();
                }
                releaseConnection(conn);
                throw new RegistryException(e);
            }
            catch(SQLException se) {
                throw new RegistryException(se);
            }
        }
        catch(RegistryException e) {
            log.error(e);
            try {
                if (!conn.isClosed()) {
                    conn.rollback();
                }
                releaseConnection(conn);
                throw e;
            }
            catch(SQLException se) {
                throw new RegistryException(se);
            }
        }
        
    }
    
    public static void main(String[] args) {
        SQLPersistenceManagerImpl impl = SQLPersistenceManagerImpl.getInstance();
        
        String sqlQuery = "SELECT *  FROM RegistryPackage WHERE id = 'urn:uuid:4e71dd00-05e0-41b0-b30d-9f3b49b8098c'";
        ResponseOption responseOption = new ResponseOption();
        responseOption.setReturnComposedObjects(true);
        responseOption.setReturnType(ReturnTypeType.LEAFCLASS);
        
        String tableName = "RegistryObject";
        ArrayList objectRefs = new ArrayList();
        try {
            long time1 = System.currentTimeMillis();
            objectRefs = impl.executeSQLQuery(sqlQuery,
            responseOption,
            tableName,
            objectRefs);
            long time2 = System.currentTimeMillis();
            System.err.println("Elapsed time in millis: " + (time2-time1));
        }
        catch (RegistryException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);
    }
}
/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/persistence/rdb/SQLPersistenceManagerImpl.java,v 1.30 2004/03/22 03:22:50 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.persistence.rdb;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.RegistryException;
import org.freebxml.omar.server.common.RegistryProperties;
import org.oasis.ebxml.registry.bindings.query.ResponseOption;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryType;
import org.oasis.ebxml.registry.bindings.rim.UserType;


/**
 * Class Declaration for Class1
 * @see
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 */
public class SQLPersistenceManagerImpl
    implements org.freebxml.omar.server.persistence.PersistenceManager {
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */

    /*# private SQLPersistenceManagerImpl _sqlPersistenceManagerImpl; */
    private static SQLPersistenceManagerImpl instance = null;
    private final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(this.getClass());

    /**
     *
     * @associates <{org.freebxml.omar.server.persistence.rdb.ExtrinsicObjectDAO}>
     */
    String databaseURL = null;
    java.sql.DatabaseMetaData metaData = null;
    private String driver;
    private String user;
    private String password;
    private boolean useConnectionPool;
    private ConnectionPool connectionPool;

    private SQLPersistenceManagerImpl() {
        loadUsernamePassword();
        loadDatabaseDriver();
        constructDatabaseURL();

        if (RegistryProperties.getInstance()
                                  .getProperty("omar.persistence.rdb.useConnectionPooling")
                                  .equalsIgnoreCase("true")) {
            useConnectionPool = true;
            createConnectionPool();
        } else {
            useConnectionPool = false;
        }
    }

    /** Look up the driver name and load the database driver */
    private void loadDatabaseDriver() {
        try {
            driver = RegistryProperties.getInstance().getProperty("omar.persistence.rdb.databaseDriver");
            Class.forName(driver);

            log.debug("Loaded jdbc driver: " + driver);
        } catch (ClassNotFoundException e) {
            log.error(e);
        }
    }

    /** Lookup up the db URL fragments and form the complete URL */
    private void constructDatabaseURL() {
        databaseURL = RegistryProperties.getInstance().getProperty("omar.persistence.rdb.databaseURL");

        log.info("dbURL = '" + databaseURL + "'");
    }

    /**Load the username and password for database access*/
    private void loadUsernamePassword() {
        user = RegistryProperties.getInstance().getProperty("omar.persistence.rdb.databaseUser");
        password = RegistryProperties.getInstance().getProperty("omar.persistence.rdb.databaseUserPassword");
    }

    private void createConnectionPool() {
        RegistryProperties registryProperties = RegistryProperties.getInstance();
        String initialSize = registryProperties.getProperty(
                "omar.persistence.rdb.pool.initialSize");
        int initConns = 1;

        if (initialSize != null) {
            initConns = Integer.parseInt(initialSize);
        }

        String maxSize = registryProperties.getProperty(
                "omar.persistence.rdb.pool.maxSize");
        int maxConns = 1;

        if (maxSize != null) {
            maxConns = Integer.parseInt(maxSize);
        }

        String connectionTimeOut = registryProperties.getProperty(
                "omar.persistence.rdb.pool.connectionTimeOut");
        int timeOut = 0;

        if (connectionTimeOut != null) {
            timeOut = Integer.parseInt(connectionTimeOut);
        }

        connectionPool = new ConnectionPool("ConnectionPool", databaseURL,
                user, password, maxConns, initConns, timeOut);
    }

    /**
     * Get a database connection. The connection is of autocommit off and with
     * transaction isolation level "transaction read committed"
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection connection = null;

            if (useConnectionPool) {
                connection = connectionPool.getConnection();
            } else {
                // create connection directly
                connection = java.sql.DriverManager.getConnection(databaseURL,
                        user, password);
            }

            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            return connection;
        } catch (SQLException e) {
            log.error(
                "Error: Could not create Connection to database. Check if database is already running in another program (e.g. registry servlet in tomcat)");
            throw e;
        }
    }

    private void releaseConnection(Connection connection) throws SQLException {
        if (!connection.isClosed() && !useConnectionPool) {
            connection.close();
        } else if (useConnectionPool) {
            connectionPool.freeConnection(connection);
        }
    }

    public static SQLPersistenceManagerImpl getInstance() {
        if (instance == null) {
            synchronized (SQLPersistenceManagerImpl.class) {
                if (instance == null) {
                    instance = new SQLPersistenceManagerImpl();
                }
            }
        }

        return instance;
    }

    //Sort objects by their type.
    private void sortRegistryObjects(List registryObjects, List associations,
        List auditableEvents, List classifications, List schemes,
        List classificationNodes, List externalIds, List externalLinks,
        List extrinsicObjects, 
        List federations, 
        List objectRefs, List organizations, List packages,
        List serviceBindings, List services, List specificationLinks, 
        List adhocQuerys,
        List subscriptions,
        List users,
        List registrys
        )
        throws RegistryException {
        associations.clear();
        auditableEvents.clear();
        classifications.clear();
        schemes.clear();
        classificationNodes.clear();
        externalIds.clear();
        externalLinks.clear();
        extrinsicObjects.clear();
        federations.clear();
        objectRefs.clear();
        organizations.clear();
        packages.clear();
        serviceBindings.clear();
        services.clear();
        specificationLinks.clear();
        subscriptions.clear();
        users.clear();
        registrys.clear();

        java.util.Iterator objIter = registryObjects.iterator();

        while (objIter.hasNext()) {
            IdentifiableType obj = (IdentifiableType) objIter.next();

            if (obj instanceof org.oasis.ebxml.registry.bindings.rim.AssociationType1) {
                associations.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.AuditableEventType) {
                auditableEvents.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ClassificationType) {
                classifications.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType) {
                schemes.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType) {
                classificationNodes.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType) {
                externalIds.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ExternalLinkType) {
                externalLinks.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType) {
                extrinsicObjects.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.FederationType) {
                federations.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ObjectRefType) {
                objectRefs.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.OrganizationType) {
                organizations.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.RegistryPackageType) {
                packages.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ServiceBindingType) {
                serviceBindings.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ServiceType) {
                services.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.query.SQLQueryType) {
                adhocQuerys.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.query.FilterQueryType) {
                adhocQuerys.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType) {
                specificationLinks.add(obj);
            } else if (obj instanceof org.oasis.ebxml.registry.bindings.rim.SubscriptionType) {
                subscriptions.add(obj);
            } else if (obj instanceof UserType) {
                users.add(obj);
            } else if (obj instanceof RegistryType) {
                registrys.add(obj);
            } 
            else {
                throw new RegistryException("Enexpected object type: " +
                    obj.getClass().getName());
            }
        }
    }

    /**
     * Does a bulk insert of a heterogeneous Collection of RegistrObjects.
     *
     */
    public void insert(UserType user, List registryObjects)
        throws RegistryException {
        Connection connection = null;

        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            
            DAOContext context = new DAOContext(user, connection);

            insert(context, registryObjects, true);
            releaseConnection(connection);
        } catch (SQLException e) {
            log.error(e);

            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }

                releaseConnection(connection);
                throw new RegistryException(e);
            } catch (SQLException se) {
                throw new RegistryException(se);
            }
        } catch (RegistryException e) {
            log.error(e);

            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }

                releaseConnection(connection);
                throw e;
            } catch (SQLException se) {
                throw new RegistryException(se);
            }
        }
    }

    /**
     * Does a bulk insert of a heterogeneous Collection of RegistrObjects.
     *
     */
    public void insert(DAOContext context, List registryObjects,
        boolean doCommit) throws RegistryException {
            
            
        Connection connection = context.getConnection();
        try {
            connection.setAutoCommit(false);

            List associations = new java.util.ArrayList();
            List auditableEvents = new java.util.ArrayList();
            List classifications = new java.util.ArrayList();
            List schemes = new java.util.ArrayList();
            List classificationNodes = new java.util.ArrayList();
            List externalIds = new java.util.ArrayList();
            List externalLinks = new java.util.ArrayList();
            List extrinsicObjects = new java.util.ArrayList();
            List federations = new java.util.ArrayList();
            List objectRefs = new java.util.ArrayList();
            List organizations = new java.util.ArrayList();
            List packages = new java.util.ArrayList();
            List serviceBindings = new java.util.ArrayList();
            List services = new java.util.ArrayList();
            List specificationLinks = new java.util.ArrayList();
            List adhocQuerys = new java.util.ArrayList();
            List subscriptions = new java.util.ArrayList();
            List users = new java.util.ArrayList();
            List registrys = new java.util.ArrayList();

            sortRegistryObjects(registryObjects, associations, auditableEvents,
                classifications, schemes, classificationNodes, externalIds,
                externalLinks, extrinsicObjects, federations, objectRefs, organizations, packages,
                serviceBindings, services, specificationLinks, adhocQuerys, subscriptions, users, registrys);

            if (associations.size() > 0) {
                AssociationDAO associationDAO = new AssociationDAO(context);
                associationDAO.insert(associations);
            }

            if (classifications.size() > 0) {
                ClassificationDAO classificationDAO = new ClassificationDAO(context);
                classificationDAO.insert(classifications);
            }

            if (schemes.size() > 0) {
                ClassificationSchemeDAO classificationSchemeDAO = new ClassificationSchemeDAO(context);
                classificationSchemeDAO.insert(schemes);
            }

            if (classificationNodes.size() > 0) {
                ClassificationNodeDAO classificationNodeDAO = new ClassificationNodeDAO(context);
                classificationNodeDAO.insert(classificationNodes);
            }

            if (externalIds.size() > 0) {
                ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO(context);
                externalIdentifierDAO.insert(externalIds);
            }

            if (externalLinks.size() > 0) {
                ExternalLinkDAO externalLinkDAO = new ExternalLinkDAO(context);
                externalLinkDAO.insert(externalLinks);
            }

            if (extrinsicObjects.size() > 0) {
                ExtrinsicObjectDAO extrinsicObjectDAO = new ExtrinsicObjectDAO(context);
                extrinsicObjectDAO.insert(extrinsicObjects);
            }

            if (federations.size() > 0) {
                FederationDAO federationDAO = new FederationDAO(context);
                federationDAO.insert(federations);
            }

            if (objectRefs.size() > 0) {
                ObjectRefDAO objectRefDAO = new ObjectRefDAO(context);
                objectRefDAO.insert(objectRefs);
            }

            if (organizations.size() > 0) {
                OrganizationDAO organizationDAO = new OrganizationDAO(context);
                organizationDAO.insert(organizations);
            }

            if (packages.size() > 0) {
                RegistryPackageDAO registryPackageDAO = new RegistryPackageDAO(context);
                registryPackageDAO.insert(packages);
            }

            if (serviceBindings.size() > 0) {
                ServiceBindingDAO serviceBindingDAO = new ServiceBindingDAO(context);
                serviceBindingDAO.insert(serviceBindings);
            }

            if (services.size() > 0) {
                ServiceDAO serviceDAO = new ServiceDAO(context);
                serviceDAO.insert(services);
            }

            if (specificationLinks.size() > 0) {
                SpecificationLinkDAO specificationLinkDAO = new SpecificationLinkDAO(context);
                specificationLinkDAO.insert(specificationLinks);
            }

            if (adhocQuerys.size() > 0) {
                AdhocQueryDAO adhocQueryDAO = new AdhocQueryDAO(context);
                adhocQueryDAO.insert(adhocQuerys);
            }

            if (subscriptions.size() > 0) {
                SubscriptionDAO subscriptionDAO = new SubscriptionDAO(context);
                subscriptionDAO.insert(subscriptions);
            }

            if (users.size() > 0) {
                UserDAO userDAO = new UserDAO(context);
                userDAO.insert(users);
            }

            if (registrys.size() > 0) {
                RegistryDAO registryDAO = new RegistryDAO(context);
                registryDAO.insert(registrys);
            }

            //Special case handling for AuditableEvents as they can only be created 
            //by this class and not by clients of this class.
            
            //Ignore any client supplied AuditableEvents 
            auditableEvents.clear();
            
            if (doCommit) {
                log.info("Now committing transaction");
                context.commit();
                releaseConnection(connection);
            }
        } catch (SQLException e) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }

                releaseConnection(connection);
                throw new RegistryException(e);
            } catch (SQLException se) {
                throw new RegistryException(se);
            }
        }
    }

    /**
     * Does a bulk update of a heterogeneous Collection of RegistrObjects.
     *
     */
    public void update(UserType user, List registryObjects)
        throws RegistryException {
        Connection connection = null;
        DAOContext context = null;
        
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            context = new DAOContext(user, connection);

            List associations = new java.util.ArrayList();
            List auditableEvents = new java.util.ArrayList();
            List classifications = new java.util.ArrayList();
            List schemes = new java.util.ArrayList();
            List classificationNodes = new java.util.ArrayList();
            List externalIds = new java.util.ArrayList();
            List externalLinks = new java.util.ArrayList();
            List extrinsicObjects = new java.util.ArrayList();
            List federations = new java.util.ArrayList();
            List objectRefs = new java.util.ArrayList();
            List organizations = new java.util.ArrayList();
            List packages = new java.util.ArrayList();
            List serviceBindings = new java.util.ArrayList();
            List services = new java.util.ArrayList();
            List specificationLinks = new java.util.ArrayList();
            List adhocQuerys = new java.util.ArrayList();
            List subscriptions = new java.util.ArrayList();
            List users = new java.util.ArrayList();
            List registrys = new java.util.ArrayList();

            sortRegistryObjects(registryObjects, associations, auditableEvents,
                classifications, schemes, classificationNodes, externalIds,
                externalLinks, extrinsicObjects, federations, objectRefs, organizations, packages,
                serviceBindings, services, specificationLinks, adhocQuerys, subscriptions, users, registrys);

            if (associations.size() > 0) {
                AssociationDAO associationDAO = new AssociationDAO(context);
                associationDAO.update(associations);
            }

            if (classifications.size() > 0) {
                ClassificationDAO classificationDAO = new ClassificationDAO(context);
                classificationDAO.update(classifications);
            }

            if (schemes.size() > 0) {
                ClassificationSchemeDAO classificationSchemeDAO = new ClassificationSchemeDAO(context);
                classificationSchemeDAO.update(schemes);
            }

            if (classificationNodes.size() > 0) {
                ClassificationNodeDAO classificationNodeDAO = new ClassificationNodeDAO(context);
                classificationNodeDAO.update(classificationNodes);
            }

            if (externalIds.size() > 0) {
                // ExternalId is no longer the first level, right?
                ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO(context);
                externalIdentifierDAO.update(externalIds);
            }

            if (externalLinks.size() > 0) {
                ExternalLinkDAO externalLinkDAO = new ExternalLinkDAO(context);
                externalLinkDAO.update(externalLinks);
            }

            if (extrinsicObjects.size() > 0) {
                ExtrinsicObjectDAO extrinsicObjectDAO = new ExtrinsicObjectDAO(context);
                extrinsicObjectDAO.update(extrinsicObjects);
            }

            if (objectRefs.size() > 0) {
                ObjectRefDAO objectRefDAO = new ObjectRefDAO(context);
                objectRefDAO.update(objectRefs);
            }

            if (organizations.size() > 0) {
                OrganizationDAO organizationDAO = new OrganizationDAO(context);
                organizationDAO.update(organizations);
            }

            if (packages.size() > 0) {
                RegistryPackageDAO registryPackageDAO = new RegistryPackageDAO(context);
                registryPackageDAO.update(packages);
            }

            if (serviceBindings.size() > 0) {
                ServiceBindingDAO serviceBindingDAO = new ServiceBindingDAO(context);
                serviceBindingDAO.update(serviceBindings);
            }

            if (services.size() > 0) {
                ServiceDAO serviceDAO = new ServiceDAO(context);
                serviceDAO.update(services);
            }

            if (specificationLinks.size() > 0) {
                SpecificationLinkDAO specificationLinkDAO = new SpecificationLinkDAO(context);
                specificationLinkDAO.update(specificationLinks);
            }

            if (adhocQuerys.size() > 0) {
                AdhocQueryDAO adhocQueryDAO = new AdhocQueryDAO(context);
                adhocQueryDAO.update(adhocQuerys);
            }

            if (subscriptions.size() > 0) {
                SubscriptionDAO subscriptionDAO = new SubscriptionDAO(context);
                subscriptionDAO.update(subscriptions);
            }

            if (users.size() > 0) {
                UserDAO userDAO = new UserDAO(context);
                userDAO.update(users);
            }

            if (registrys.size() > 0) {
                RegistryDAO registryDAO = new RegistryDAO(context);
                registryDAO.insert(registrys);
            }

            //Special case handling for AuditableEvents as they can only be created 
            //by this class and not by clients of this class.
            
            //Ignore any client supplied AuditableEvents 
            auditableEvents.clear();
                        
            context.commit();
            releaseConnection(connection);
        } catch (SQLException e) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }

                releaseConnection(connection);
                throw new RegistryException(e);
            } catch (SQLException se) {
                throw new RegistryException(se);
            }
        } catch (RegistryException e) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }

                releaseConnection(connection);
                throw e;
            } catch (SQLException se) {
                throw new RegistryException(se);
            }
        }
    }

    /**
     * Update the status of specified objects to the specified status.
     *
     */
    public void updateStatus(UserType user, List registryObjectsIds,
        org.oasis.ebxml.registry.bindings.rim.Status status,
        org.oasis.ebxml.registry.bindings.rs.RegistryErrorListType el)
        throws RegistryException {
        Connection connection = null;
        DAOContext context = null;

        try {
            connection = getConnection();
            context = new DAOContext(user, connection);
            RegistryObjectDAO roDAO = new RegistryObjectDAO(context);
            
            List statusUnchanged = new ArrayList();
            roDAO.updateStatus(registryObjectsIds, status.toString(), statusUnchanged);
            context.commit();
            releaseConnection(connection);
        } catch (SQLException e) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }

                releaseConnection(connection);
                throw new RegistryException(e);
            } catch (SQLException se) {
                throw new RegistryException(se);
            }
        } catch (RegistryException e) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }

                releaseConnection(connection);
                throw e;
            } catch (SQLException se) {
                throw new RegistryException(se);
            }
        }
    }
    
    /**
     * Given a Binding object returns the OMARDAO for that object.
     *
     */
    private OMARDAO getDAOForObject(RegistryObjectType ro, DAOContext context) throws RegistryException {
        OMARDAO dao = null;
        
        try {
            String bindingClassName = ro.getClass().getName();
            String daoClassName = bindingClassName.substring(bindingClassName.lastIndexOf('.')+1, bindingClassName.length()-4);

            //Construct the corresonding DAO instance using reflections
            Class daoClazz = Class.forName("org.freebxml.omar.server.persistence.rdb." + daoClassName + "DAO");

            Class[] conParameterTypes = new Class[1];
            conParameterTypes[0] = context.getClass();
            Object[] conParameterValues = new Object[1];
            conParameterValues[0] = context;
            Constructor[] cons = daoClazz.getDeclaredConstructors();
            
            //Find the constructor that takes DAOContext as its only arg
            Constructor con = null;
            for (int i=0; i<cons.length; i++) {
                con = cons[i];
                if ((con.getParameterTypes().length == 1) && (con.getParameterTypes()[0] == conParameterTypes[0])) {
                    dao = (OMARDAO)con.newInstance(conParameterValues);
                    break;
                }
            }
            
        }
        catch (Exception e) {
            throw new RegistryException(e);
        }
        
        return dao;
        
    }

    /**
     * Does a bulk delete of a heterogeneous Collection of RegistrObjects. If
     * any RegistryObject cannot be found, it will make no change to the
     * database and throw RegistryException
     *
     */
    public void delete(UserType user, List registryObjectIds)
        throws RegistryException {
        Connection connection = null;
        DAOContext context = null;

        try {
            //Return if nothing specified to delete
            if (registryObjectIds.size() == 0) {
                return;
            }
            
            //First fetch the objects and then delete them
            String query = "SELECT * FROM RegistryObject ro WHERE ro.id IN ( " +
                BindingUtility.getInstance().getIdListFromIds(registryObjectIds) +
                " ) ";
            List objs = getRegistryObjectsMatchingQuery(query, "RegistryObject");

            connection = getConnection();
            connection.setAutoCommit(false);
            
            context = new DAOContext(user, connection);
            
            Iterator iter = objs.iterator();
            while (iter.hasNext()) {
                RegistryObjectType ro = (RegistryObjectType)iter.next();
                OMARDAO dao = getDAOForObject(ro, context);
                
                //Now call delete method
                List objectsToDelete = new ArrayList();
                objectsToDelete.add(ro);
                dao.delete(objectsToDelete);                
            }            
            
            context.commit();
            releaseConnection(connection);
        } catch (Exception e) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }

                releaseConnection(connection);
                throw new RegistryException(e);
            } catch (SQLException se) {
                throw new RegistryException(se);
            }
        } 
    }

    /**
     * Adds specified slots to specified object.
     *
     */
    public void addSlots(UserType user, String objectId, List slots) throws RegistryException {
        Connection connection = null;
        DAOContext context = null;

        try {
            connection = getConnection();
            context = new DAOContext(user, connection);

            //Now insert Slots for this object
            SlotDAO slotDAO = new SlotDAO(context);
            slotDAO.setParent(objectId);
            
            if (slots.size() > 0) {
                slotDAO.insert(slots, false);
                
                context.commit();
                releaseConnection(connection);
            }
        } catch (SQLException e) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }

                releaseConnection(connection);
                throw new RegistryException(e);
            } catch (SQLException se) {
                throw new RegistryException(se);
            }
        } catch (RegistryException e) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }

                releaseConnection(connection);
                throw e;
            } catch (SQLException se) {
                throw new RegistryException(se);
            }
        }
    }

    /**
     * Removes specified slots from specified object.
     *
     */
    public void removeSlots(UserType user, String objectId, List slots)
        throws RegistryException {
        Connection connection = null;
        DAOContext context = null;

        try {
            connection = getConnection();
            context = new DAOContext(user, connection);

            SlotDAO slotDAO = new SlotDAO(context);
            slotDAO.deleteByParentIdAndSlots(objectId, slots);
            context.commit();
            releaseConnection(connection);
        } catch (SQLException e) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }

                releaseConnection(connection);
                throw new RegistryException(e);
            } catch (SQLException se) {
                throw new RegistryException(se);
            }
        } catch (RegistryException e) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }

                releaseConnection(connection);
                throw e;
            } catch (SQLException se) {
                throw new RegistryException(se);
            }
        }
    }

    private java.sql.DatabaseMetaData getMetaData(Connection connection)
        throws SQLException {
        if (metaData == null) {
            metaData = connection.getMetaData();
        }

        return metaData;
    }

    /**
     * Executes an SQL Query.
     */
    public List executeSQLQuery(String sqlQuery,
        ResponseOptionType responseOption, String tableName, List objectRefs)
        throws RegistryException {
        List res = null;
        Connection connection = null;

        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            java.sql.ResultSet rs = null;

            tableName = org.freebxml.omar.server.common.Utility.getInstance()
                                                               .mapTableName(tableName);

            ReturnType returnType = responseOption.getReturnType();
            boolean returnComposedObjects = responseOption.isReturnComposedObjects();

            //Check if user may have set an incorrect returnType.
            //If so fix silently per spec to the next less specific returnType.
            if (returnType == ReturnType.REGISTRY_ENTRY) {
                rs = getMetaData(connection).getColumns(null, null, tableName,
                        "expiration");

                if (!(rs.next())) {
                    //Not a RegistryEntry. Make it a RegistryObject
                    returnType = ReturnType.REGISTRY_OBJECT;
                }

                rs.close();
            }

            java.sql.Statement stmt = connection.createStatement();

            System.err.println("Executing query: '" + sqlQuery + "'");
            log.debug("Executing query: '" + sqlQuery + "'");
            rs = stmt.executeQuery(sqlQuery);

            java.util.Iterator iter = null;

            if (returnType == ReturnType.OBJECT_REF) {
                res = new java.util.ArrayList();

                while (rs.next()) {
                    org.oasis.ebxml.registry.bindings.rim.ObjectRef or = BindingUtility.getInstance().rimFac.createObjectRef();
                    String id = rs.getString(1);
                    or.setId(id);
                    res.add(or);
                }
            } else if (returnType == ReturnType.REGISTRY_OBJECT) {
                DAOContext context =  new DAOContext(null, connection);
                context.setResponseOption(responseOption);
                RegistryObjectDAO roDAO = new RegistryObjectDAO(context);
                res = roDAO.getObjects(rs);
            } else if (returnType == ReturnType.REGISTRY_ENTRY) {
                DAOContext context =  new DAOContext(null, connection);                
                context.setResponseOption(responseOption);
                RegistryEntryDAO reDAO = new RegistryEntryDAO(context);
                res = reDAO.getRegistryEntryList(rs, responseOption);
            } else if ((returnType == ReturnType.LEAF_CLASS) ||
                    (returnType == ReturnType.LEAF_CLASS_WITH_REPOSITORY_ITEM)) {
                res = getObjects(connection, rs, tableName, responseOption,
                        objectRefs);
            } else {
                throw new RegistryException("Invalid returnType: " +
                    returnType);
            }

            stmt.close();
            releaseConnection(connection);
        } catch (SQLException e) {
            try {
                //Leave this in for now to help faster debugging during development.
                e.printStackTrace();

                if (connection != null) {
                    if (!connection.isClosed()) {
                        connection.rollback();
                    }

                    releaseConnection(connection);
                }

                throw new RegistryException(e);
            } catch (SQLException se) {
                throw new RegistryException(se);
            }
        } catch (RegistryException e) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }

                releaseConnection(connection);
                throw e;
            } catch (SQLException se) {
                throw new RegistryException(se);
            }
        } catch (javax.xml.bind.JAXBException e) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }

                releaseConnection(connection);
                throw new RegistryException(e);
            } catch (SQLException se) {
                throw new RegistryException(se);
            }
        }

        return res;
    }

    private List getObjects(Connection connection, java.sql.ResultSet rs,
        String tableName, ResponseOptionType responseOption, List objectRefs)
        throws RegistryException {
        List res = null;

        DAOContext context =  new DAOContext(null, connection);
        context.setResponseOption(responseOption);

        if (tableName.equalsIgnoreCase(AdhocQueryDAO.getTableNameStatic())) {
            AdhocQueryDAO adhocQueryDAO = new AdhocQueryDAO(context);
            res = adhocQueryDAO.getObjects(rs);
        } else if (tableName.equalsIgnoreCase(AssociationDAO.getTableNameStatic())) {
            AssociationDAO associationDAO = new AssociationDAO(context);
            res = associationDAO.getObjects(rs);
        } else if (tableName.equalsIgnoreCase(
                    AuditableEventDAO.getTableNameStatic())) {
            AuditableEventDAO auditableEventDAO = new AuditableEventDAO(context);
            res = auditableEventDAO.getObjects(rs);
        } else if (tableName.equalsIgnoreCase(
                    ClassificationDAO.getTableNameStatic())) {
            ClassificationDAO classificationDAO = new ClassificationDAO(context);
            res = classificationDAO.getObjects(rs);
        } else if (tableName.equalsIgnoreCase(
                    ClassificationSchemeDAO.getTableNameStatic())) {
            ClassificationSchemeDAO classificationSchemeDAO = new ClassificationSchemeDAO(context);
            res = classificationSchemeDAO.getObjects(rs);
        } else if (tableName.equalsIgnoreCase(
                    ClassificationNodeDAO.getTableNameStatic())) {
            ClassificationNodeDAO classificationNodeDAO = new ClassificationNodeDAO(context);
            res = classificationNodeDAO.getObjects(rs);
        } else if (tableName.equalsIgnoreCase(
                    ExternalIdentifierDAO.getTableNameStatic())) {
            ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO(context);
            res = externalIdentifierDAO.getObjects(rs);
        } else if (tableName.equalsIgnoreCase(
                    ExternalLinkDAO.getTableNameStatic())) {
            ExternalLinkDAO externalLinkDAO = new ExternalLinkDAO(context);
            res = externalLinkDAO.getObjects(rs);
        } else if (tableName.equalsIgnoreCase(
                    ExtrinsicObjectDAO.getTableNameStatic())) {
            ExtrinsicObjectDAO extrinsicObjectDAO = new ExtrinsicObjectDAO(context);
            res = extrinsicObjectDAO.getObjects(rs);
        } else if (tableName.equalsIgnoreCase(
                    FederationDAO.getTableNameStatic())) {
            FederationDAO federationDAO = new FederationDAO(context);
            res = federationDAO.getObjects(rs);
        } else if (tableName.equalsIgnoreCase(
                    ObjectRefDAO.getTableNameStatic())) {
            ObjectRefDAO objectRefDAO = new ObjectRefDAO(context);
            res = objectRefDAO.getObjects(rs);
        } else if (tableName.equalsIgnoreCase(
                    OrganizationDAO.getTableNameStatic())) {
            OrganizationDAO organizationDAO = new OrganizationDAO(context);
            res = organizationDAO.getObjects(rs);
        } else if (tableName.equalsIgnoreCase(
                    RegistryObjectDAO.getTableNameStatic())) {
            RegistryObjectDAO registryObjectDAO = new RegistryObjectDAO(context);
            //TODO: Use reflection instead in future
            res = registryObjectDAO.getObjectsHetero(rs);
        } else if (tableName.equalsIgnoreCase(
                    RegistryEntryDAO.getTableNameStatic())) {
            RegistryObjectDAO registryObjectDAO = new RegistryObjectDAO(context);
            //TODO: Use reflection instead in future
            res = registryObjectDAO.getObjectsHetero(rs);
        } else if (tableName.equalsIgnoreCase(
                    RegistryPackageDAO.getTableNameStatic())) {
            RegistryPackageDAO registryPackageDAO = new RegistryPackageDAO(context);
            res = registryPackageDAO.getObjects(rs);
        } else if (tableName.equalsIgnoreCase(
                    ServiceBindingDAO.getTableNameStatic())) {
            ServiceBindingDAO serviceBindingDAO = new ServiceBindingDAO(context);
            res = serviceBindingDAO.getObjects(rs);
        } else if (tableName.equalsIgnoreCase(ServiceDAO.getTableNameStatic())) {
            ServiceDAO serviceDAO = new ServiceDAO(context);
            res = serviceDAO.getObjects(rs);
        } else if (tableName.equalsIgnoreCase(
                    SpecificationLinkDAO.getTableNameStatic())) {
            SpecificationLinkDAO specificationLinkDAO = new SpecificationLinkDAO(context);
            res = specificationLinkDAO.getObjects(rs);
        } else if (tableName.equalsIgnoreCase(
                    SubscriptionDAO.getTableNameStatic())) {
            SubscriptionDAO subscriptionDAO = new SubscriptionDAO(context);
            res = subscriptionDAO.getObjects(rs);
        } else if (tableName.equalsIgnoreCase(UserDAO.getTableNameStatic())) {
            UserDAO userDAO = new UserDAO(context);
            res = userDAO.getObjects(rs);
        } else if (tableName.equalsIgnoreCase(
                    RegistryDAO.getTableNameStatic())) {
            RegistryDAO registryDAO = new RegistryDAO(context);
            res = registryDAO.getObjects(rs);
        }
        
        return res;
    }

    /**
     * Returns List of ids of non-existent RegistryObject
     */
    public List registryObjectsExist(UserType user, List ids) throws RegistryException {
        Connection connection = null;        
        
        try {
            connection = getConnection();
            DAOContext context = new DAOContext(user, connection);
            RegistryObjectDAO roDAO = new RegistryObjectDAO(context);

            List notExist = roDAO.registryObjectsExist(ids);
            releaseConnection(connection);

            return notExist;
        } catch (SQLException e) {
            log.error(e);

            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }

                releaseConnection(connection);
                throw new RegistryException(e);
            } catch (SQLException se) {
                throw new RegistryException(se);
            }
        } catch (RegistryException e) {
            log.error(e);

            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }

                releaseConnection(connection);
                throw e;
            } catch (SQLException se) {
                throw new RegistryException(se);
            }
        }
    }

    /**
     * Gets the specified objects using specified query and className
     *
     */
    public List getRegistryObjectsMatchingQuery(String query, String tableName)
        throws RegistryException {
        List objects = null;

        try {
            ResponseOption responseOption = BindingUtility.getInstance().queryFac.createResponseOption();
            responseOption.setReturnType(ReturnType.LEAF_CLASS);
            responseOption.setReturnComposedObjects(true);

            List objectRefs = new java.util.ArrayList();
            objects = executeSQLQuery(query, responseOption, tableName,
                    objectRefs);

        } catch (javax.xml.bind.JAXBException e) {
            throw new RegistryException(e);
        }

        return objects;
    }

    /**
     * Gets the specified object using specified id and className
     *
     */
    public RegistryObjectType getRegistryObjectMatchingQuery(String query, String tableName)
        throws RegistryException {
        RegistryObjectType ro = null;

        List al = getRegistryObjectsMatchingQuery(query, tableName);

        if (al.size() == 1) {
            ro = (RegistryObjectType) al.get(0);
        }

        return ro;
    }

    /**
     * Gets the specified object using specified id and className
     *
     */
    public RegistryObjectType getRegistryObject(String id, String className)
        throws RegistryException {
        RegistryObjectType ro = null;

        String tableName = org.freebxml.omar.server.common.Utility.getInstance()
                                                                  .mapTableName(className);
        String sqlQuery = "Select * from " + tableName + " WHERE id = '" +
            id + "' ";
        
        ro = getRegistryObjectMatchingQuery(sqlQuery, tableName);


        return ro;
    }

    /**
     * Gets the specified object using specified ObjectRef
     *
     */
    public RegistryObjectType getRegistryObject(ObjectRefType ref)
        throws RegistryException {
            
        return getRegistryObject(ref.getId(), "RegistryObject");
    }
    
    /**
     * Get a HashMap with registry object id as key and owner id as value
     */
    public HashMap getOwnersMap(List ids) throws RegistryException {
        Connection connection = null;

        try {
            connection = getConnection();
            DAOContext context = new DAOContext(null, connection);
            RegistryObjectDAO roDAO = new RegistryObjectDAO(context);

            HashMap ownersMap = roDAO.getOwnersMap(ids);
            releaseConnection(connection);

            return ownersMap;
        } catch (SQLException e) {
            log.error(e);

            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }

                releaseConnection(connection);
                throw new RegistryException(e);
            } catch (SQLException se) {
                throw new RegistryException(se);
            }
        } catch (RegistryException e) {
            log.error(e);

            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }

                releaseConnection(connection);
                throw e;
            } catch (SQLException se) {
                throw new RegistryException(se);
            }
        }
    }
}
