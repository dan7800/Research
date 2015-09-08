/*
 * $Header: /cvsroot/sino/ebxmlrr/src/share/com/sun/ebxml/registry/persistence/rdb/RegistryObjectDAO.java,v 1.56 2003/07/24 01:40:58 farrukh_najmi Exp $
 */

package com.sun.ebxml.registry.persistence.rdb;

import com.sun.ebxml.registry.RegistryException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.oasis.ebxml.registry.bindings.query.ResponseOption;
import org.oasis.ebxml.registry.bindings.rim.RegistryObject;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.User;

/**
 *
 * @see <{RegistryEntry}>
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 */
public class RegistryObjectDAO {
    
    public static String getTableNameStatic() {
        return "RegistryObject";
    }
    
    public String getTableName() {
        return getTableNameStatic();
    }
    
    /**
     * Generate AuditabEvent
     */
    
        /*
    public void generateAuditbleEvent(java.sql.Statement stmt,
        RegistryObjectType roType, String eventType, User user) throws
        RegistryException {
                AuditableEvent ae = new AuditableEvent();
                ae.setId("urn:uuid:" + UUIDFactory.getInstance().newUUID().toString());
                ae.setUser(user);
                ae.setObjectType("AuditableEvent");
                ae.setEventType(EventTypeType.valueOf(eventType));
                ae.setRegistryObject(roType.getId());
                ae.setTimestamp(new Timestamp( (new java.util.Date()).getTime()));
                ArrayList aes = new ArrayList();
                aes.add(ae);
                AuditableEventDAO.getInstance().insert(stmt, aes);
        }
         */
    
    /**
     * Generate AuditabEvent
     */
    
    public void generateAuditbleEvent(Connection conn, RegistryObjectType roType, String eventType, User user) throws RegistryException {
        org.oasis.ebxml.registry.bindings.rim.AuditableEvent ae = new org.oasis.ebxml.registry.bindings.rim.AuditableEvent();
        ae.setId("urn:uuid:" + com.sun.ebxml.registry.util.UUIDFactory.getInstance().newUUID().toString());
        ae.setUser(user);
        ae.setObjectType("AuditableEvent");
        ae.setEventType(org.oasis.ebxml.registry.bindings.rim.types.EventTypeType.valueOf(eventType));
        ae.setRegistryObject(roType.getId());
        ae.setTimestamp(new java.sql.Timestamp( (new java.util.Date()).getTime()));
        ArrayList aes = new ArrayList();
        aes.add(ae);
        AuditableEventDAO.getInstance().insert(conn, aes);
    }
    
    /**
     * Generate AuditabEvent
     */
    
    public void generateAuditbleEvent(Connection conn, ArrayList ros, String eventType, User user) throws RegistryException {
        if (ros.size()==0) {
            return;
        }
        AuditableEventDAO aeDAO = AuditableEventDAO.getInstance();
        ArrayList aes = new ArrayList();
        Iterator iter = ros.iterator();
        while(iter.hasNext()) {
            RegistryObjectType roType = (RegistryObjectType)iter.next();
            org.oasis.ebxml.registry.bindings.rim.AuditableEvent ae = new org.oasis.ebxml.registry.bindings.rim.AuditableEvent();
            ae.setId("urn:uuid:" + com.sun.ebxml.registry.util.UUIDFactory.getInstance().newUUID().toString());
            ae.setUser(user);
            ae.setObjectType("AuditableEvent");
            ae.setEventType(org.oasis.ebxml.registry.bindings.rim.types.EventTypeType.valueOf(eventType));
            ae.setRegistryObject(roType.getId());
            ae.setTimestamp(new java.sql.Timestamp( (new java.util.Date()).getTime()));
            aes.add(ae);
        }
        aeDAO.insert(conn, aes);
    }
    
    // Should we make index on sourceObject/targetObject in Association????
    protected String checkAssociationReferences(Connection conn, String roId) throws RegistryException {
        String assId = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            String sql = "SELECT sourceObject, targetObject, id FROM Association WHERE "
            + "sourceObject='" + roId + "' OR targetObject='" +  roId + "'";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                assId = rs.getString("id");
            }
            //stmt.close();
            return assId;
        }
        catch (SQLException e){
            throw new RegistryException(e);
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }
    
    // Should we make index on classifiedObject in Classification????
    protected String checkClassificationReferences(Connection conn, String roId) throws RegistryException {
        String classId = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            String sql = "SELECT id FROM Classification WHERE "
            + "classifiedObject='" + roId + "'";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                classId = rs.getString("id");
            }
            //stmt.close();
            return classId;
        }
        catch (SQLException e){
            throw new RegistryException(e);
        }  finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }
    
    protected void checkReferences(Connection conn, String roId) throws RegistryException {
        /*
         * There is a bug where Associations are not being deleted.
         * Until the bug is fixed we will disable this check.
        String assId = checkAssociationReferences(conn, roId);
        if (assId != null) {
            throw new ReferencesExistException("Association " + assId + " is referencing " + roId);
        }
         
        String classId = checkClassificationReferences(conn, roId);
        if (classId != null) {
            throw new ReferencesExistException("Classification " + classId + " is classifying " + roId);
        }
         */
    }
    
    /**
     * Get the objectType of a submitted object in persistence layer
     * @return null if the object does not exist or the objectType field is null
     */
    
    public String getObjectType(Connection conn, String id, String tableName)
    throws RegistryException {
        String sql = "select objectType from " + tableName + " where id='"
        + id + "'";
        System.err.println(sql);
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            boolean hasNextRecord = rs.next();
            if (!hasNextRecord) {
                return null;
            }
            else {
                return rs.getString("objectType");
            }
        }
        catch (SQLException e){
            throw new RegistryException(e);
        }  finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }
    
    /**
     * Does a bulk insert of a Collection of objects that match the type for this persister.
     * It is for inserting the nested objects, i.e., Name, Description, Slot, Classification
     * and ExternalIdentifier
     */
    public void insert(User user, Connection connection, ArrayList registryObjects) throws RegistryException {
        Iterator iter = registryObjects.iterator();
        ClassificationDAO classificationDAO = new ClassificationDAO();
        DescriptionDAO descriptionDAO = new DescriptionDAO();
        ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO();
        NameDAO nameDAO = new NameDAO();
        SlotDAO slotDAO = new SlotDAO();
        while (iter.hasNext()) {
            RegistryObjectType obj = (RegistryObjectType)iter.next();
            
            //Process name atribute
            org.oasis.ebxml.registry.bindings.rim.InternationalStringType name = obj.getName();
            
            if (name != null) {
                nameDAO.insert(user, connection, obj.getId()
                , name);
            }
            
            
            //Process description atribute
            org.oasis.ebxml.registry.bindings.rim.InternationalStringType desc = obj.getDescription();
            
            if (desc != null) {
                descriptionDAO.insert(user, connection, obj
                .getId(), desc);
            }
            
            //Now insert Slots for this object
            org.oasis.ebxml.registry.bindings.rim.Slot[] slots = obj.getSlot();
            
            if (slots.length > 0) {
                ArrayList slots1 = new ArrayList();
                for (int i=0; i<slots.length; i++) {
                    slots1.add(slots[i]);
                }
                slotDAO.insert(connection, obj.getId(), slots1, true);
            }
            
            //Now insert any composed ExternalIdentifiers
            org.oasis.ebxml.registry.bindings.rim.ExternalIdentifier[] extIds = obj.getExternalIdentifier();
            ArrayList extIdsAL = new ArrayList();
            for (int i=0; i<extIds.length; i++) {
                extIdsAL.add(extIds[i]);
            }
            if (extIdsAL.size() > 0) {
                externalIdentifierDAO.insert(user, connection, obj.getId(), extIdsAL);
            }
            
            //Now insert any composed Classifications
            org.oasis.ebxml.registry.bindings.rim.Classification[] classifications = obj.getClassification();
            ArrayList classificationsAL = new ArrayList();
            for (int i=0; i<classifications.length; i++) {
                classificationsAL.add(classifications[i]);
            }
            if (classificationsAL.size() > 0) {
                classificationDAO.insert(user, connection, classificationsAL);
            }
        } // end looping registryObjects
    }
    
    /**
     * Does a bulk update of a Collection of objects that match the type for this persister.
     * It is for updating the nested objects (i.e. Name, Description, Slot, Classification
     * and ExternalIdentifier. It will firstly delete all the nested objects of each objects
     * in the ArrayList registryObjects, and then inserts the nested objects again.
     */
    
    public void update(User user, Connection connection, ArrayList registryObjects) throws RegistryException {
        if (registryObjects.size()==0) {
            return;
        }
        
        Iterator iter = registryObjects.iterator();
        ArrayList registryObjectIds = new ArrayList();
        
        // Get the ids of all RegistryObject in registryObjects and fix the ids of
        // nested Classification and ExternalIdentifier
        com.sun.ebxml.registry.lcm.LifeCycleManagerImpl lcm = com.sun.ebxml.registry.lcm.LifeCycleManagerImpl.getInstance();
        
        HashMap idMap = new HashMap();
        
        while(iter.hasNext()) {
            Object obj = iter.next();
            if (obj instanceof RegistryObjectType) {
                RegistryObjectType ro = (RegistryObjectType) obj;
                registryObjectIds.add(ro.getId());
                // Fix the id of nested ExternalIdentifier and also its nested objects
                org.oasis.ebxml.registry.bindings.rim.ExternalIdentifier[] extIds = ro.getExternalIdentifier();
                ArrayList extIdsAL = new ArrayList();
                for (int i=0; i<extIds.length; i++) {
                    extIdsAL.add(extIds[i]);
                }
                if (extIdsAL.size() > 0) {
                    lcm.fixTemporaryIds(extIdsAL, idMap);
                }
                
                // Fix the id of nested Classification and also its nested objects
                org.oasis.ebxml.registry.bindings.rim.Classification[] classifications = ro.getClassification();
                ArrayList classificationsAL = new ArrayList();
                for (int i=0; i < classifications.length; i++) {
                    classificationsAL.add(classifications[i]);
                }
                if (classificationsAL.size() > 0) {
                    lcm.fixTemporaryIds(classificationsAL, idMap);
                }
            }
            else {
                throw new RegistryException("Unknown RegistryObjectType!");
            }
        }
        
        //Delete name atribute for the specified objects
        NameDAO nameDAO = new NameDAO();
        nameDAO.deleteByParentIds(connection, registryObjectIds);
        
        //Delete description atribute for the specified objects
        DescriptionDAO descriptionDAO = new DescriptionDAO();
        descriptionDAO.deleteByParentIds(connection, registryObjectIds);
        
        SlotDAO slotDAO = new SlotDAO();
        slotDAO.deleteByParentIds(connection, registryObjectIds);
        
        ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO();
        externalIdentifierDAO.deleteByParentIds(user, connection, registryObjectIds);
        
        ClassificationDAO classificationDAO = new ClassificationDAO();
        classificationDAO.deleteByParentIds(user, connection, registryObjectIds);
        
        // Inserting the composed Name, Description, Slot, Classification and ExternalIdentifier of the ROs
                /* Now we have to instantiate another RegistryObjectDAO otherwise the
                insert() of DAO for various types of RO will be called instead if this
                update() method is being called by update() of DAOs
                 */
        RegistryObjectDAO roDAO = new RegistryObjectDAO();
        roDAO.insert(user, connection, registryObjects);
    }
    
    private ArrayList getRegistryObjectsIds(ArrayList registryObjects) throws RegistryException {
        //System.err.println("size: "  + registryObjects.size());
        Iterator iter = registryObjects.iterator();
        ArrayList ids = new ArrayList();
        while(iter.hasNext()) {
            //String id = ((RegistryObject)iter.next()).getId();
            String id = com.sun.ebxml.registry.util.BindingUtility.getInstance().getObjectId(iter.next());
            System.err.println("id!!!!=" + id);
            ids.add(id);
        }
        return ids;
    }
    
    
    /**
     * Sort registryObjectIds by their objectType.
     * @return The HashMap storing the objectType String as keys and ArrayList of ids
     * as values. For ExtrinsicObject, the objectType key is stored as "ExtrinsicObject"
     * rather than the objectType of the repository items.
     */
    public HashMap sortIdsByObjectType(Connection connection, ArrayList registryObjectIds) throws RegistryException {
        HashMap map = new HashMap();
        Statement stmt = null;
        
        try	{
            
            if (registryObjectIds.size() > 0) {
                stmt = connection.createStatement();
                
                String str = "SELECT id, objectType FROM " + getTableName() +
                " WHERE id IN ( ";
                
                Iterator iter = registryObjectIds.iterator();
                while (iter.hasNext()) {
                    String id = (String)iter.next();
                    
                    if (iter.hasNext()) {
                        str = str + "'" + id + "', ";
                    }
                    else {
                        str = str + "'" + id + "' )";
                    }
                }
                
                System.err.println("stmt = " + str);
                ResultSet rs = stmt.executeQuery(str);
                
                ArrayList associationsIds = new ArrayList();
                ArrayList auditableEventsIds = new ArrayList();
                ArrayList classificationsIds = new ArrayList();
                ArrayList classificationSchemesIds = new ArrayList();
                ArrayList classificationNodesIds = new ArrayList();
                ArrayList externalIdentifiersIds = new ArrayList();
                ArrayList externalLinksIds = new ArrayList();
                ArrayList extrinsicObjectsIds = new ArrayList();
                ArrayList organizationsIds = new ArrayList();
                ArrayList registryPackagesIds = new ArrayList();
                ArrayList serviceBindingsIds = new ArrayList();
                ArrayList servicesIds = new ArrayList();
                ArrayList specificationLinksIds = new ArrayList();
                ArrayList usersIds = new ArrayList();
                
                
                while(rs.next()) {
                    String id = rs.getString(1);
                    String objectType = rs.getString(2);
                    // System.err.println("objectType: " + objectType + "!!!!!!!");
                    if (objectType.equalsIgnoreCase("Association")) {
                        associationsIds.add(id);
                    }
                    else if (objectType.equalsIgnoreCase("AuditableEvent")) {
                        auditableEventsIds.add(id);
                    }
                    else if (objectType.equalsIgnoreCase("Classification")) {
                        classificationsIds.add(id);
                    }
                    else if (objectType.equalsIgnoreCase("ClassificationScheme")) {
                        classificationSchemesIds.add(id);
                    }
                    else if (objectType.equalsIgnoreCase("ClassificationNode")) {
                        classificationNodesIds.add(id);
                    }
                    else if (objectType.equalsIgnoreCase("ExternalIdentifier")) {
                        externalIdentifiersIds.add(id);
                    }
                    else if (objectType.equalsIgnoreCase("ExternalLink")) {
                        externalLinksIds.add(id);
                    }
                    else if (objectType.equalsIgnoreCase("Organization")) {
                        organizationsIds.add(id);
                    }
                    else if (objectType.equalsIgnoreCase("RegistryPackage")) {
                        registryPackagesIds.add(id);
                    }
                    else if (objectType.equalsIgnoreCase("ServiceBinding")) {
                        serviceBindingsIds.add(id);
                    }
                    else if (objectType.equalsIgnoreCase("Service")) {
                        servicesIds.add(id);
                    }
                    else if (objectType.equalsIgnoreCase("SpecificationLink")) {
                        specificationLinksIds.add(id);
                    }
                    else if (objectType.equalsIgnoreCase("User")) {
                        usersIds.add(id);
                    }
                    else {
                        // ExtrinsicObject
                        extrinsicObjectsIds.add(id);
                    }
                } // end looping ResultSet
                
                // Now put the ArrayList of id of varios RO type into the HashMap
                if ( associationsIds.size() > 0) {
                    map.put("Association", associationsIds);
                }
                if (auditableEventsIds.size() > 0) {
                    map.put("AuditableEvent", auditableEventsIds);
                }
                if (classificationsIds.size() > 0) {
                    map.put("Classification", classificationsIds);
                }
                if (classificationSchemesIds.size() > 0) {
                    map.put("ClassificationScheme", classificationSchemesIds);
                }
                if (classificationNodesIds.size() > 0) {
                    map.put("ClassificationNode", classificationNodesIds);
                }
                if (externalIdentifiersIds.size() > 0) {
                    map.put("ExternalIdentifer", externalIdentifiersIds);
                }
                if (externalLinksIds.size() > 0) {
                    map.put("ExternalLink", externalLinksIds);
                }
                if (organizationsIds.size() > 0) {
                    map.put("Organization", organizationsIds);
                }
                if (registryPackagesIds.size() > 0) {
                    map.put("RegistryPackage", registryPackagesIds);
                }
                if (serviceBindingsIds.size() > 0) {
                    map.put("ServiceBinding", serviceBindingsIds);
                }
                if (servicesIds.size() > 0) {
                    map.put("Service", servicesIds);
                }
                if (specificationLinksIds.size() > 0) {
                    map.put("SpecificationLink", specificationLinksIds);
                }
                if (usersIds.size() > 0) {
                    map.put("User", usersIds);
                }
                if (extrinsicObjectsIds.size() > 0) {
                    map.put("ExtrinsicObject", extrinsicObjectsIds);
                }
            } // end if checking the size of registryObjectsIds
        }
        catch (SQLException e)	{
            e.printStackTrace();
            throw new RegistryException(e);
        }  finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
        return map;
    }
    
    /**
     * Does a bulk delete of a Collection of objects that match the type for this persister.
     *
     */
    public void delete(User user, Connection connection, ArrayList registryObjectIds) throws RegistryException {
        if (registryObjectIds.size()==0) {
            return;
        }
        
        System.err.println("Deleting " + registryObjectIds.size() + " " + getTableName());
        
        //Delete name atribute for the specified objects
        NameDAO nameDAO = new NameDAO();
        nameDAO.deleteByParentIds(connection, registryObjectIds);
        
        //Delete description atribute for the specified objects
        DescriptionDAO descriptionDAO = new DescriptionDAO();
        descriptionDAO.deleteByParentIds(connection, registryObjectIds);
        
        SlotDAO slotDAO = new SlotDAO();
        slotDAO.deleteByParentIds(connection, registryObjectIds);
        
        ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO();
        externalIdentifierDAO.deleteByParentIds(user, connection, registryObjectIds);
        
        ClassificationDAO classificationDAO = new ClassificationDAO();
        classificationDAO.deleteByParentIds(user, connection, registryObjectIds);
        Statement stmt = null;
        
        try {
            stmt = connection.createStatement();
            
            Iterator iter = registryObjectIds.iterator();
            ArrayList ros = new ArrayList();
            while (iter.hasNext())	{
                String registryObjectId = (String)iter.next();
                // Check any other references
                checkReferences(connection, registryObjectId);
                stmt.addBatch("DELETE from " + getTableName() + " WHERE id = '" +
                registryObjectId + "' ");
                RegistryObject ro = new RegistryObject();
                ro.setId(registryObjectId);
                ros.add(ro);
            }
            
            int [] updateCounts = stmt.executeBatch();
            //stmt.close();
            // Now generate the AE for deleting RO
            generateAuditbleEvent(connection, ros, "Deleted", user);
        }
        catch (SQLException e)	{
            RegistryException exception = new RegistryException(e);
            throw exception;
        }  finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
        
    }
    
    /**
     * Return true if the RegistryObject exist
     */
    public boolean registryObjectExist(Connection conn, String id) throws RegistryException{
        
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            String sql = "SELECT id from RegistryObject where id='"+ id +"'";
            ResultSet rs = stmt.executeQuery(sql);
            boolean result = false;
            if (rs.next()) {
                result = true;
            }
            //stmt.close();
            return result;
            
        }
        catch(SQLException e) {
            throw new RegistryException(e);
        }  finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }
    
    /**
     * Check whether the object exists in the specified table.
     */
    public boolean registryObjectExist(Connection conn, String id, String tableName) throws RegistryException{
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            String sql = "SELECT id from " + tableName + " where id='"+ id +"'";
            ResultSet rs = stmt.executeQuery(sql);
            boolean result = false;
            if (rs.next()) {
                result = true;
            }
            //stmt.close();
            return result;
        }
        catch(SQLException e) {
            throw new RegistryException(e);
        }  finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }
    
    /**
     * Returns ArrayList of ids of non-existent RegistryObject.
     */
    public ArrayList registryObjectsExist(Connection conn, ArrayList ids) throws RegistryException {
        ArrayList notExistIdList = new ArrayList();
        Iterator iter = ids.iterator();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            while (iter.hasNext()) {
                String id = (String)iter.next();
                ResultSet rs = stmt.executeQuery("select id from RegistryObject where " +
                "id = '" + id+ "'");
                if (!rs.next()) {
                    // the RegistryObject does not exist
                    notExistIdList.add(id);
                }
            }
            //stmt.close();
        }
        catch(SQLException e) {
            throw new RegistryException(e);
        }  finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
        return notExistIdList;
    }
    
    /**
     * Returns ArrayList of ids of non-existent RegistryObject.
     */
    public ArrayList registryObjectsExist(Connection conn, ArrayList ids, String tableName) throws RegistryException {
        ArrayList notExistIdList = new ArrayList();
        if (ids.size()==0) {
            return notExistIdList;
        }
        Iterator iter = ids.iterator();
        Statement stmt = null;
        try {
            
            stmt = conn.createStatement();
            String sql = "SELECT id FROM " + tableName + " WHERE id IN (";
            ArrayList existingIdList = new ArrayList();
                        /* We need to count the number of item in "IN" list. We need to split the a single
            SQL Strings if it is too long. Some database such as Oracle, does not
            allow the IN list is too long*/
            int listCounter = 0;
            while (iter.hasNext()) {
                String id = (String)iter.next();
                if (iter.hasNext() && listCounter < 99 ) {
                    sql += "'" + id + "',";
                }
                else {
                    sql += "'" + id + "')";
                    //System.err.println("!!!!!!!!!!!!!!!!!!!" + sql);
                    ResultSet rs = stmt.executeQuery(sql);
                    while (rs.next()) {
                        existingIdList.add(rs.getString("id"));
                    }
                    sql = "SELECT id FROM " + tableName + " WHERE id IN (";
                    listCounter = 0;
                }
                listCounter++;
            }
            
            for(int i=0; i < ids.size(); i++) {
                String id = (String) ids.get(i);
                if (!existingIdList.contains(id)) {
                    notExistIdList.add(id);
                }
            }
            //stmt.close();
        }
        catch(SQLException e){
            throw new RegistryException(e);
        }  finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
        return notExistIdList;
    }
    
    /**
     * It is to update exisitng Registry Objects when any existing objects can be
     * found within SubmitObjectsRequest. It should be called by the method
     * insert() of subclass DAO.
     * @return ArrayList of RegistryObjects that are not existing
     */
    protected ArrayList updateExistingObjects(User user, Connection conn, ArrayList ros) throws RegistryException {
        
        //System.err.println("Updating!!!");
        com.sun.ebxml.registry.util.BindingUtility bindingUtility = com.sun.ebxml.registry.util.BindingUtility.getInstance();
        //System.err.println(getTableName() + "!!!");
        ArrayList notExistIds = registryObjectsExist(conn, bindingUtility
        .getIdsFromRegistryObjects(ros), getTableName()); // getTableName() is the one which is the overidding one of subclass DAO
        ArrayList notExistROs = bindingUtility.getRegistryObjectsFromIds(ros, notExistIds);
        ArrayList existingROs = new ArrayList();
        Iterator rosIter = ros.iterator();
        while(rosIter.hasNext()) {
            RegistryObjectType ro = (RegistryObjectType)rosIter.next();
            if (!notExistROs.contains(ro)) {
                existingROs.add(ro);
            }
        }
        //System.err.println(existingROs.size() + "!!!!");
        /*
        We do not check the existence because we are sure all the objects passed to this method are all existing.
        But we re-check the authorization again. The first authorization checking is not done in persistence layer.
        SHOULD WE MAKE THE AUTHORIZATION ALWAYS IN PERSISTENCE LAYER???? IT IS MORE CONSISTENT
         */
        update(user, conn, existingROs, false, true);
        return notExistROs;
        
    }
    
    /**
     * This should be overrided by subclass DAO. It is called by updateExistingObjects() methods.
     */
    protected void update(User user, Connection conn, ArrayList ros, boolean checkExistence, boolean checkAuthorization) throws RegistryException {
        throw new RegistryException("Implementation Error: method should be implemented by sub-class but is not");
    }
    
    public static ArrayList getByIds(Connection conn, ArrayList ids, boolean getComposedObjects) throws RegistryException {
        ArrayList res = new ArrayList();
        Statement stmt = null;
        
        try {
            stmt = conn.createStatement();
            ResultSet rs = null;
            
            Iterator iter = ids.iterator();
            StringBuffer idsSB = new StringBuffer();
            while (iter.hasNext()) {
                String id = (String)iter.next();
                idsSB.append("'");
                idsSB.append(id);
                idsSB.append("'");
                if (iter.hasNext()) {
                    idsSB.append(", ");
                }
            }
            
            while (iter.hasNext()) {
                String id = (String)iter.next();
                RegistryObject ro = new RegistryObject();
                ro.setId(id);
                
                rs = stmt.executeQuery("select * from RegistryObject where id IN (" + idsSB + ")");
                while(rs.next()) {
                    ro.setAccessControlPolicy(null);
                    ro.setObjectType(rs.getString("objectType"));
                }
                rs.close();
                
                rs = stmt.executeQuery("select * from Name where parent = '" + id + "'");
                org.oasis.ebxml.registry.bindings.rim.Name name = new org.oasis.ebxml.registry.bindings.rim.Name();
                ro.setName(name);
                while(rs.next()) {
                    org.oasis.ebxml.registry.bindings.rim.LocalizedString ls = new org.oasis.ebxml.registry.bindings.rim.LocalizedString();
                    ls.setCharset(rs.getString("charset"));
                    ls.setLang(rs.getString("lang"));
                    ls.setValue(rs.getString("value"));
                    org.oasis.ebxml.registry.bindings.rim.InternationalStringTypeItem isi = new org.oasis.ebxml.registry.bindings.rim.InternationalStringTypeItem();
                    isi.setLocalizedString(ls);
                    name.addInternationalStringTypeItem(isi);
                }
                rs.close();
                
                res.add(ro);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(e.toString());
            throw new RegistryException(e);
        }  finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
        
        return res;
    }
    
    ArrayList getRegistryObjectList(Connection conn, ResultSet rs, ResponseOption responseOption) throws RegistryException {
        ArrayList res = new ArrayList();
        
        try {
            while(rs.next()) {
                RegistryObject ro = new RegistryObject();
                loadObjectFromResultSet(conn, ro, rs, responseOption, new ArrayList());
                
                res.add(ro);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RegistryException(e);
        }
        
        return res;
    }
    
    public void loadObjectFromResultSet(Connection conn, Object obj, ResultSet rs, ResponseOption responseOption, ArrayList objectRefs) throws RegistryException {
        try {
            if (!(obj instanceof RegistryObjectType)) {
                throw new RegistryException("Unexpected object " + obj + ". Was expecting org.oasis.ebxml.registry.bindings.rim.RegistryObjectType.");
            }
            
            RegistryObjectType ro = (RegistryObjectType)obj;
            ro.setAccessControlPolicy(null);
            
            String id = rs.getString("id");
            ro.setId(id);
            
            String objectType = rs.getString("objectType");
            ro.setObjectType(objectType);
            
            NameDAO nameDAO = new NameDAO();
            org.oasis.ebxml.registry.bindings.rim.Name name = nameDAO.getNameByParent(conn, id);
            ro.setName(name);
            
            DescriptionDAO descriptionDAO = new DescriptionDAO();
            org.oasis.ebxml.registry.bindings.rim.Description desc = descriptionDAO.getDescriptionByParent(conn, id);
            ro.setDescription(desc);
            
            SlotDAO slotDAO = new SlotDAO();
            ArrayList slots = slotDAO.getSlotsByParent(conn, id);
            org.oasis.ebxml.registry.bindings.rim.Slot[] slots1 = new org.oasis.ebxml.registry.bindings.rim.Slot[slots.size()];
            
            Iterator iter = slots.iterator();
            int i=0;
            while (iter.hasNext()) {
                slots1[i++] = (org.oasis.ebxml.registry.bindings.rim.Slot) iter.next();
            }
            ro.setSlot(slots1);
            
            boolean returnComposedObjects = responseOption.getReturnComposedObjects();
            if (returnComposedObjects) {
                ClassificationDAO classificationDAO = new ClassificationDAO();
                ArrayList classifications = classificationDAO.getByParentId(conn, id, responseOption, objectRefs);
                iter = classifications.iterator();
                while (iter.hasNext()) {
                    org.oasis.ebxml.registry.bindings.rim.Classification c = (org.oasis.ebxml.registry.bindings.rim.Classification)iter.next();
                    objectRefs.add(c.getClassifiedObject());
                    org.oasis.ebxml.registry.bindings.rim.ObjectRef refClassScheme = (org.oasis.ebxml.registry.bindings.rim.ObjectRef) c.getClassificationScheme();
                    org.oasis.ebxml.registry.bindings.rim.ObjectRef refClassNode = (org.oasis.ebxml.registry.bindings.rim.ObjectRef) c.getClassificationNode();
                    if (refClassScheme != null) {
                        objectRefs.add(refClassScheme);
                    }
                    if (refClassNode != null) {
                        objectRefs.add(refClassNode);
                    }
                    ro.addClassification(c);
                }
                ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO();
                ArrayList extIds = externalIdentifierDAO.getByParentId(conn, id, objectRefs);
                iter = extIds.iterator();
                while (iter.hasNext()) {
                    org.oasis.ebxml.registry.bindings.rim.ExternalIdentifier ei = (org.oasis.ebxml.registry.bindings.rim.ExternalIdentifier)iter.next();
                    ro.addExternalIdentifier(ei);
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RegistryException(e);
        }
    }
    
    private void getRegistryObjectsIdsFromResultSet(ResultSet rs
    , StringBuffer associations
    , StringBuffer auEvents
    , StringBuffer classifications
    , StringBuffer schemes
    , StringBuffer classificationNodes
    , StringBuffer externalIds
    , StringBuffer externalLinks
    , StringBuffer extrinsicObjects
    , StringBuffer organizations
    , StringBuffer packages
    , StringBuffer serviceBindings
    , StringBuffer services
    , StringBuffer specificationLinks
    , StringBuffer users
    ) throws SQLException {
        
        
        while(rs.next()) {
            String id = rs.getString("id");
            String type = rs.getString("objectType");
            
            //System.err.println("objectType = '" + type + "'");
            
            if (type.equalsIgnoreCase("Association")) {
                if (associations.length()==0) {
                    associations.append("'" + id + "'");
                }
                else {
                    associations.append(",'" + id + "'");
                }
            }
            else if (type.equalsIgnoreCase("AuditableEvent")) {
                if (auEvents.length()==0) {
                    auEvents.append("'" + id + "'");
                }
                else {
                    auEvents.append(",'" + id + "'");
                }
            }
            else if (type.equalsIgnoreCase("Classification")) {
                if (classifications.length()==0) {
                    classifications.append("'" + id + "'");
                }
                else {
                    classifications.append(",'" + id + "'");
                }
            }
            else if (type.equalsIgnoreCase("ClassificationNode")) {
                if (classificationNodes.length()==0) {
                    classificationNodes.append("'" + id + "'");
                }
                else {
                    classificationNodes.append(",'" + id + "'");
                }
            }
            else if (type.equalsIgnoreCase("ClassificationScheme")) {
                if (schemes.length()==0) {
                    schemes.append("'" + id + "'");
                }
                else {
                    schemes.append(",'" + id + "'");
                }
            }
            else if (type.equalsIgnoreCase("ExternalIdentifier")) {
                if (externalIds.length()==0) {
                    externalIds.append("'" + id + "'");
                }
                else {
                    externalIds.append(",'" + id + "'");
                }
            }
            else if (type.equalsIgnoreCase("ExternalLink")) {
                if (externalLinks.length()==0) {
                    externalLinks.append("'" + id + "'");
                }
                else {
                    externalLinks.append(",'" + id + "'");
                }
            }
            else if (type.equalsIgnoreCase("ExtrinsicObject")) {
                if (extrinsicObjects.length()==0) {
                    extrinsicObjects.append("'" + id + "'");
                }
                else {
                    extrinsicObjects.append(",'" + id + "'");
                }
            }
            else if (type.equalsIgnoreCase("Organization")) {
                if (organizations.length()==0) {
                    organizations.append("'" + id + "'");
                }
                else {
                    organizations.append(",'" + id + "'");
                }
            }
            else if (type.equalsIgnoreCase("RegistryPackage")) {
                if (packages.length()==0) {
                    packages.append("'" + id + "'");
                }
                else {
                    packages.append(",'" + id + "'");
                }
            }
            else if (type.equalsIgnoreCase("ServiceBinding")) {
                if (serviceBindings.length()==0) {
                    serviceBindings.append("'" + id + "'");
                }
                else {
                    serviceBindings.append(",'" + id + "'");
                }
            }
            else if (type.equalsIgnoreCase("Service")) {
                if (services.length()==0) {
                    services.append("'" + id + "'");
                }
                else {
                    services.append(",'" + id + "'");
                }
            }
            else if (type.equalsIgnoreCase("SpecificationLink")) {
                if (specificationLinks.length()==0) {
                    specificationLinks.append("'" + id + "'");
                }
                else {
                    specificationLinks.append(",'" + id + "'");
                }
            }
            else if (type.equalsIgnoreCase("User")) {
                if (users.length()==0) {
                    users.append("'" + id + "'");
                }
                else {
                    users.append(",'" + id + "'");
                }
            }
            else {
                //??This is dangerous. Need a better way to tell ExtrinsicObjects
                if (extrinsicObjects.length()==0) {
                    extrinsicObjects.append("'" + id + "'");
                }
                else {
                    extrinsicObjects.append(",'" + id + "'");
                }
            }
            
        }
    }
    
    public ArrayList getLeafObjectList(Connection conn, ResultSet rs, ResponseOption responseOption, ArrayList objectRefs) throws RegistryException {
        
        ArrayList res = new ArrayList();
        String sql = null;
        
        StringBuffer associationsIds = new StringBuffer();
        StringBuffer auditableEventsIds = new StringBuffer();
        StringBuffer classificationsIds = new StringBuffer();
        StringBuffer schemesIds = new StringBuffer();
        StringBuffer classificationNodesIds = new StringBuffer();
        StringBuffer externalIdsIds = new StringBuffer();
        StringBuffer externalLinksIds = new StringBuffer();
        StringBuffer extrinsicObjectsIds = new StringBuffer();
        StringBuffer organizationsIds = new StringBuffer();
        StringBuffer packagesIds = new StringBuffer();
        StringBuffer serviceBindingsIds = new StringBuffer();
        StringBuffer servicesIds = new StringBuffer();
        StringBuffer specificationLinksIds = new StringBuffer();
        StringBuffer usersIds = new StringBuffer();
        
        Statement stmt = null;
        
        
        try {
            stmt = conn.createStatement();
            
            getRegistryObjectsIdsFromResultSet(rs, associationsIds, auditableEventsIds
            , classificationsIds, schemesIds, classificationNodesIds, externalIdsIds
            , externalLinksIds, extrinsicObjectsIds, organizationsIds, packagesIds
            , serviceBindingsIds, servicesIds, specificationLinksIds, usersIds);
            
            ResultSet leafObjectsRs = null;
            if (associationsIds.length() > 0) {
                AssociationDAO assDAO = new AssociationDAO();
                sql = "SELECT * FROM " + assDAO.getTableName() + " WHERE id IN (" + associationsIds + ")";
                //System.out.println(sql);
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(assDAO.getLeafObjectList(conn, leafObjectsRs, responseOption, objectRefs));
            }
            if (auditableEventsIds.length() > 0) {
                AuditableEventDAO aeDAO = new AuditableEventDAO();
                sql = "SELECT * FROM " + aeDAO.getTableName() + " WHERE id IN (" + auditableEventsIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(aeDAO.getLeafObjectList(conn, leafObjectsRs, responseOption, objectRefs));
            }
            if (classificationsIds.length() > 0) {
                ClassificationDAO classDAO = new ClassificationDAO();
                sql = "SELECT * FROM " + classDAO.getTableName() + " WHERE id IN (" + classificationsIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(classDAO.getLeafObjectList(conn, leafObjectsRs, responseOption, objectRefs));
            }
            if (schemesIds.length() > 0) {
                ClassificationSchemeDAO schemeDAO = new ClassificationSchemeDAO();
                sql = "SELECT * FROM " + schemeDAO.getTableName() + " WHERE id IN (" + schemesIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(schemeDAO.getLeafObjectList(conn, leafObjectsRs, responseOption, objectRefs));
            }
            if (classificationNodesIds.length() > 0) {
                ClassificationNodeDAO nodeDAO = new ClassificationNodeDAO();
                sql = "SELECT * FROM " + nodeDAO.getTableName() + " WHERE id IN (" + classificationNodesIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(nodeDAO.getLeafObjectList(conn, leafObjectsRs, responseOption, objectRefs));
            }
            if (externalIdsIds.length() > 0) {
                ExternalIdentifierDAO externalIdDAO = new ExternalIdentifierDAO();
                sql = "SELECT * FROM " + externalIdDAO.getTableName() + " WHERE id IN (" + externalIdsIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(externalIdDAO.getLeafObjectList(conn, leafObjectsRs, responseOption, objectRefs));
            }
            if (externalLinksIds.length() > 0) {
                ExternalLinkDAO externalLinkDAO = new ExternalLinkDAO();
                sql = "SELECT * FROM " + externalLinkDAO.getTableName() + " WHERE id IN (" + externalLinksIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(externalLinkDAO.getLeafObjectList(conn, leafObjectsRs, responseOption, objectRefs));
            }
            if (extrinsicObjectsIds.length() > 0) {
                ExtrinsicObjectDAO extrinsicObjectDAO = new ExtrinsicObjectDAO();
                sql = "SELECT * FROM " + extrinsicObjectDAO.getTableName() + " WHERE id IN (" + extrinsicObjectsIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(extrinsicObjectDAO.getLeafObjectList(conn, leafObjectsRs, responseOption, objectRefs));
            }
            if (organizationsIds.length() > 0) {
                OrganizationDAO organizationDAO = new OrganizationDAO();
                sql = "SELECT * FROM " + organizationDAO.getTableName() + " WHERE id IN (" + organizationsIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(organizationDAO.getLeafObjectList(conn, leafObjectsRs, responseOption, objectRefs));
            }
            if (packagesIds.length() > 0) {
                RegistryPackageDAO pkgDAO = new RegistryPackageDAO();
                sql = "SELECT * FROM " + pkgDAO.getTableName() + " WHERE id IN (" + packagesIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(pkgDAO.getLeafObjectList(conn, leafObjectsRs, responseOption, objectRefs));
            }
            if (serviceBindingsIds.length() > 0) {
                ServiceBindingDAO serviceBindingDAO = new ServiceBindingDAO();
                sql = "SELECT * FROM " + serviceBindingDAO.getTableName() + " WHERE id IN (" + serviceBindingsIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(serviceBindingDAO.getLeafObjectList(conn, leafObjectsRs, responseOption, objectRefs));
            }
            if (servicesIds.length() > 0) {
                ServiceDAO serviceDAO = new ServiceDAO();
                sql = "SELECT * FROM " + serviceDAO.getTableName() + " WHERE id IN (" + servicesIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(serviceDAO.getLeafObjectList(conn, leafObjectsRs, responseOption, objectRefs));
            }
            if (specificationLinksIds.length() > 0) {
                SpecificationLinkDAO specLinkDAO = new SpecificationLinkDAO();
                sql = "SELECT * FROM " + specLinkDAO.getTableName() + " WHERE id IN (" + specificationLinksIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(specLinkDAO.getLeafObjectList(conn, leafObjectsRs, responseOption, objectRefs));
            }
            if (usersIds.length() > 0) {
                UserDAO userDAO = new UserDAO();
                sql = "SELECT * FROM " + userDAO.getTableName() + " WHERE id IN (" + usersIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(userDAO.getLeafObjectList(conn, leafObjectsRs, responseOption, objectRefs));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RegistryException(e);
        }  finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
        
        return res;
    }
    
    /**
     * Get the User that is the owner for the object with specified objectId
     */
    public static User getOwner(Connection connection, String objectId) throws RegistryException, SQLException {
        User owner = null;
        
        Statement stmt = connection.createStatement();
        
        String query = "SELECT * from User_ u WHERE u.id IN (SELECT user_ from AuditableEvent where registryObject = '" + objectId + "' AND eventType = 'Created') ";
        System.err.println("query=" + query);
        ResultSet rs = stmt.executeQuery(query);
        
        UserDAO userDAO = new UserDAO();
        if (rs.next()) {
            
            owner = new User();
            ResponseOption responseOption = new ResponseOption();
            responseOption.setReturnComposedObjects(false);
            userDAO.loadObjectFromResultSet(connection, owner, rs, responseOption, new ArrayList());
        }
        else {
            throw new com.sun.ebxml.registry.security.OwnerNotFoundException(objectId);
        }
        
        stmt.close();
        
        return owner;
    }
    
    /**
     * Get a HashMap with registry object id as key and owner id as value
     */
    public HashMap getOwnersMap(Connection connection, ArrayList ids) throws RegistryException {
        Statement stmt = null;
        HashMap ownersMap = new HashMap();
        if (ids.size() == 0) {
            return ownersMap;
        }
        try {
            stmt = connection.createStatement();
            StringBuffer idsList = com.sun.ebxml.registry.util.BindingUtility.getInstance().getIdListFromIds(ids);
            String query = "SELECT ae.registryObject, ae.user_ FROM AuditableEvent ae WHERE ae.registryObject IN (" +
            idsList + ") AND ae.eventType = 'Created'";
            System.err.println(query);
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                ownersMap.put(rs.getString(1), rs.getString(2));
            }
            return ownersMap;
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RegistryException(e);
        }  finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }
}
/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/persistence/rdb/RegistryObjectDAO.java,v 1.24 2003/12/07 17:30:23 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.persistence.rdb;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.OMARException;
import org.freebxml.omar.common.RegistryException;
import org.freebxml.omar.server.persistence.OwnerNotFoundException;
import org.oasis.ebxml.registry.bindings.query.ResponseOption;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.Description;
import org.oasis.ebxml.registry.bindings.rim.ExternalLinkType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;
import org.oasis.ebxml.registry.bindings.rim.Name;
import org.oasis.ebxml.registry.bindings.rim.ObjectRef;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObject;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.Status;
import org.oasis.ebxml.registry.bindings.rim.User;


/**
 *
 * @see <{RegistryEntry}>
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 */
class RegistryObjectDAO extends AbstractDAO {
        
    /**
     * Use this constructor only.
     */
    RegistryObjectDAO(DAOContext context) {
        super(context);
    }
    
    
    public static String getTableNameStatic() {
        return "RegistryObject";
    }
    
    public String getTableName() {
        return getTableNameStatic();
    }
    
    protected void prepareToInsert(Object object) throws RegistryException {
        super.prepareToInsert(object);
        RegistryObjectType ro = (RegistryObjectType)object;
        
        try {
            //Add to affectedObjects of createEvent
            AuditableEventType ae = context.getCreateEvent();
            ObjectRefType ref = BindingUtility.getInstance().rimFac.createObjectRef();
            ref.setId(ro.getId());
            ae.getAffectedObject().getObjectRef().add(ref);
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
    }    
    
    protected void prepareToUpdate(Object object) throws RegistryException{
        super.prepareToUpdate(object);
        RegistryObjectType ro = (RegistryObjectType)object;
        
        try {
            if (!(ro instanceof AuditableEventType)) {
                //Add to affectedObjects of updateEvent
                //But careful not to include event as effected by itself
                AuditableEventType ae = context.getUpdateEvent();
                ObjectRefType ref = BindingUtility.getInstance().rimFac.createObjectRef();
                ref.setId(ro.getId());
                ae.getAffectedObject().getObjectRef().add(ref);
            }
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
    }    
    
    protected void prepareToDelete(Object object) throws RegistryException{
        super.prepareToDelete(object);
        RegistryObjectType ro = (RegistryObjectType)object;
        
        try {
            //Add to affectedObjects of deleteEvent
            AuditableEventType ae = context.getDeleteEvent();
            ObjectRefType ref = BindingUtility.getInstance().rimFac.createObjectRef();
            ref.setId(ro.getId());
            ae.getAffectedObject().getObjectRef().add(ref);
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
    }    
    
    
    protected String checkAssociationReferences(String roId)
    throws RegistryException {
        String assId = null;
        Statement stmt = null;
        
        try {
            stmt = context.getConnection().createStatement();
            
            String sql =
            "SELECT sourceObject, targetObject, id FROM Association WHERE " +
            "sourceObject='" + roId + "' OR targetObject='" + roId + "'";
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                assId = rs.getString("id");
            }
            
            //stmt.close();
            return assId;
        } catch (SQLException e) {
            throw new RegistryException(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
                log.error("Caught exception", sqle);
            }
        }
    }
    
    protected String checkClassificationReferences(String roId)
    throws RegistryException {
        String classId = null;
        Statement stmt = null;
        
        try {
            stmt = context.getConnection().createStatement();
            
            String sql = "SELECT id FROM Classification WHERE " +
            "classifiedObject='" + roId + "'";
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                classId = rs.getString("id");
            }
            
            //stmt.close();
            return classId;
        } catch (SQLException e) {
            throw new RegistryException(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
                log.error("Caught exception", sqle);
            }
        }
    }
    
    /**
     * Get the objectType fpr specified object.
     * If it is an ExtrinsicObject then get it from the object.
     * Otherwise ignote the value in the object and get from
     * the DAO the hardwired value.
     */
    protected String getObjectType(RegistryObjectType ro) throws RegistryException {
        String objectType = null;
        
        try {
            String roClassName = ro.getClass().getName();
            String rimName = roClassName.substring(roClassName.lastIndexOf('.')+1, roClassName.length()-4);
            Field field = BindingUtility.getInstance().getClass().getDeclaredField("CANONICAL_OBJECT_TYPE_ID_" + rimName);
            objectType = field.get(null).toString();
        }
        catch (Exception e) {
            throw new RegistryException(e);
        }
                
        //TODO Get object type from leaf DAO if not ExtrinsicObject
        if ((ro instanceof ExtrinsicObjectType) || (ro instanceof ExternalLinkType) ) {
            String _objectType = ro.getObjectType();
            if (_objectType != null) {
                objectType = _objectType;
            }
        }
        
        return objectType;
    }
    
    /**
     * Checks and makes sure that there are no references to an object as
     * identified by registryObjectId.
     * Called when an object is being deleted.
     */
    protected void checkReferences(String registryObjectId) throws RegistryException {
        //TODO: Need to check and throw exception if any reference exists to this object
        /*
         * There is a bug where Associations are not being deleted.
         * Until the bug is fixed we will disable this check.
        String assId = checkAssociationReferences(conn, roId);
        if (assId != null) {
            throw new ReferencesExistException("Association " + assId + " is referencing " + roId);
        }
         
        String classId = checkClassificationReferences(conn, roId);
        if (classId != null) {
            throw new ReferencesExistException("Classification " + classId + " is classifying " + roId);
        }
         */
    }
    
    // Should we make index on specificationObject in SpecificationLink????
    protected String checkSpecLinkReferences(
        String parentId) throws RegistryException {
        String specLinkId = null;
        Statement stmt = null;

        try {
            stmt = context.getConnection().createStatement();

            String sql = "SELECT id FROM SpecificationLink WHERE " +
                "specificationObject='" + parentId + "'";
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                specLinkId = rs.getString("id");
            }

            //stmt.close();
            return specLinkId;
        } catch (SQLException e) {
            throw new RegistryException(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
                log.error("Caught exception", sqle);
            }
        }
    }

    /*
     * Indicate whether the type for this DAO has composed objects or not.
     * Used in deciding whether to deleteComposedObjects or not during delete.
     *
     */
    protected boolean hasComposedObject() {
        return true;
    }
    
    /**
     * Delete composed objects that have the specified registryObject
     * as parent.
     */
    protected void deleteComposedObjects(Object object) throws RegistryException {
        super.deleteComposedObjects(object);
        
        if (object instanceof RegistryObjectType) {
            RegistryObjectType registryObject = (RegistryObjectType)object;
            String id = registryObject.getId();

            ClassificationDAO classificationDAO = new ClassificationDAO(context);
            classificationDAO.setParent(registryObject);        
            DescriptionDAO descriptionDAO = new DescriptionDAO(context);
            descriptionDAO.setParent(registryObject);        
            ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO(context);
            externalIdentifierDAO.setParent(registryObject);        
            NameDAO nameDAO = new NameDAO(context);
            nameDAO.setParent(registryObject);        
            SlotDAO slotDAO = new SlotDAO(context);
            slotDAO.setParent(registryObject);

            //Delete name
            nameDAO.deleteByParent();

            //Delete description
            descriptionDAO.deleteByParent();

            //Delete Slots
            slotDAO.deleteByParent();

            //Delete ExternalIdentifier
            externalIdentifierDAO.deleteByParent();

            //Delete Classifications
            classificationDAO.deleteByParent();
        }
        else {
            int i=0;
        }
    }
    
    /**
     * Insert the composed objects for the specified registryObject
     */
    protected void insertComposedObjects(Object object) throws RegistryException {
        super.insertComposedObjects(object);

        if (object instanceof RegistryObjectType) {
            RegistryObjectType registryObject= (RegistryObjectType)object;
            ClassificationDAO classificationDAO = new ClassificationDAO(context);
            classificationDAO.setParent(registryObject);        
            DescriptionDAO descriptionDAO = new DescriptionDAO(context);
            descriptionDAO.setParent(registryObject);        
            ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO(context);
            externalIdentifierDAO.setParent(registryObject);        
            NameDAO nameDAO = new NameDAO(context);
            nameDAO.setParent(registryObject);        
            SlotDAO slotDAO = new SlotDAO(context);
            slotDAO.setParent(registryObject.getId());

            //Insert name
            InternationalStringType name = registryObject.getName();
            ArrayList list = new ArrayList();

            String id = registryObject.getId();

            if (name != null) {
                nameDAO.insert(id, name);
            }

            //Insert description
            InternationalStringType desc = registryObject.getDescription();

            if (desc != null) {
                descriptionDAO.insert(id, desc);
            }

            //Now insert Slots for this object
            List slots = registryObject.getSlot();

            if (slots.size() > 0) {            
                slotDAO.insert(slots, true);
            }

            //Insert ExternalIdentifiers
            List extIds = registryObject.getExternalIdentifier();

            if (extIds.size() > 0) {
                externalIdentifierDAO.insert(extIds);
            }

            //Insert Classifications
            List classifications = registryObject.getClassification();

            if (classifications.size() > 0) {
                classificationDAO.insert(classifications);
            }
        }
        else {
            int i=0;
        }
    }
            
    /**
     * Returns the SQL fragment string needed by insert or update statements
     * within insert or update method of sub-classes. This is done to avoid code
     * duplication.
     */
    protected String getSQLStatementFragment(Object object)
    throws RegistryException {
        
        RegistryObjectType ro = (RegistryObjectType)object;
        
        String stmtFragment = null;
        
        String id = ro.getId();
        String home = ro.getHome();
        
        if (home != null) {
            home = "'" + home + "'";
        }
        
        String objectType = getObjectType(ro);
        
        String status = Status._SUBMITTED;
        
        if (action == DAO_ACTION_INSERT) {
            stmtFragment =
            " VALUES('" + id + "', " + home + ", '" + objectType +
            "', '" + status + "' ";
        }
        else if (action == DAO_ACTION_UPDATE) {
            stmtFragment = " id='" + id + "', " + " home=" + home +
            ", objectType='" + objectType + "', status='" + status + "' ";
        }
        else if (action == DAO_ACTION_DELETE) {
            stmtFragment = "DELETE from " + getTableName() +
                " WHERE id = '" + id + "' ";
        }
        
        return stmtFragment;
    }
    
    private List getRegistryObjectsIds(List registryObjects)
    throws RegistryException {
        List ids = new ArrayList();
        
        try {
            //log.info("size: "  + registryObjects.size());
            Iterator iter = registryObjects.iterator();
            
            while (iter.hasNext()) {
                //String id = ((RegistryObject)iter.next()).getId();
                String id = BindingUtility.getInstance().getObjectId(iter.next());
                log.trace("id!!!!=" + id);
                ids.add(id);
            }
        } catch (OMARException e) {
            throw new RegistryException(e);
        }
        
        return ids;
    }
    
    /**
     * Sort registryObjectIds by their objectType.
     * @return The HashMap storing the objectType String as keys and List of ids
     * as values. For ExtrinsicObject, the objectType key is stored as "ExtrinsicObject"
     * rather than the objectType of the repository items.
     */
    public HashMap sortIdsByObjectType(List registryObjectIds) throws RegistryException {
        HashMap map = new HashMap();
        Statement stmt = null;
        
        try {
            if (registryObjectIds.size() > 0) {
                stmt = context.getConnection().createStatement();
                
                String str = "SELECT id, objectType FROM " + getTableName() +
                " WHERE id IN ( ";
                
                Iterator iter = registryObjectIds.iterator();
                
                while (iter.hasNext()) {
                    String id = (String) iter.next();
                    
                    if (iter.hasNext()) {
                        str = str + "'" + id + "', ";
                    } else {
                        str = str + "'" + id + "' )";
                    }
                }
                
                log.info("stmt = " + str);
                
                ResultSet rs = stmt.executeQuery(str);
                
                List associationsIds = new ArrayList();
                List auditableEventsIds = new ArrayList();
                List classificationsIds = new ArrayList();
                List classificationSchemesIds = new ArrayList();
                List classificationNodesIds = new ArrayList();
                List externalIdentifiersIds = new ArrayList();
                List externalLinksIds = new ArrayList();
                List extrinsicObjectsIds = new ArrayList();
                List organizationsIds = new ArrayList();
                List registryPackagesIds = new ArrayList();
                List serviceBindingsIds = new ArrayList();
                List servicesIds = new ArrayList();
                List specificationLinksIds = new ArrayList();
                List usersIds = new ArrayList();
                
                while (rs.next()) {
                    String id = rs.getString(1);
                    String objectType = rs.getString(2);
                    
                    // log.info("objectType: " + objectType + "!!!!!!!");
                    if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Association)) {
                        associationsIds.add(id);
                    } else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_AuditableEvent)) {
                        auditableEventsIds.add(id);
                    } else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Classification)) {
                        classificationsIds.add(id);
                    } else if (objectType.equalsIgnoreCase(
                    BindingUtility.CANONICAL_OBJECT_TYPE_ID_ClassificationScheme)) {
                        classificationSchemesIds.add(id);
                    } else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ClassificationNode)) {
                        classificationNodesIds.add(id);
                    } else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ExternalIdentifier)) {
                        externalIdentifiersIds.add(id);
                    } else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ExternalLink)) {
                        externalLinksIds.add(id);
                    } else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Organization)) {
                        organizationsIds.add(id);
                    } else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_RegistryPackage)) {
                        registryPackagesIds.add(id);
                    } else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ServiceBinding)) {
                        serviceBindingsIds.add(id);
                    } else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Service)) {
                        servicesIds.add(id);
                    } else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_SpecificationLink)) {
                        specificationLinksIds.add(id);
                    } else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_User)) {
                        usersIds.add(id);
                    } else {
                        //TODO: Fix dangerous assumption that is is an ExtrinsicObject
                        //Need to compare if objectType is a subType of ExtrinsicObject or not
                        extrinsicObjectsIds.add(id);
                    }
                }
                
                // end looping ResultSet
                // Now put the List of id of varios RO type into the HashMap
                if (associationsIds.size() > 0) {
                    map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Association, associationsIds);
                }
                
                if (auditableEventsIds.size() > 0) {
                    map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_AuditableEvent, auditableEventsIds);
                }
                
                if (classificationsIds.size() > 0) {
                    map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Classification, classificationsIds);
                }
                
                if (classificationSchemesIds.size() > 0) {
                    map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ClassificationScheme, classificationSchemesIds);
                }
                
                if (classificationNodesIds.size() > 0) {
                    map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ClassificationNode, classificationNodesIds);
                }
                
                if (externalIdentifiersIds.size() > 0) {
                    map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ExternalIdentifier, externalIdentifiersIds);
                }
                
                if (externalLinksIds.size() > 0) {
                    map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ExternalLink, externalLinksIds);
                }
                
                if (organizationsIds.size() > 0) {
                    map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Organization, organizationsIds);
                }
                
                if (registryPackagesIds.size() > 0) {
                    map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_RegistryPackage, registryPackagesIds);
                }
                
                if (serviceBindingsIds.size() > 0) {
                    map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ServiceBinding, serviceBindingsIds);
                }
                
                if (servicesIds.size() > 0) {
                    map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Service, servicesIds);
                }
                
                if (specificationLinksIds.size() > 0) {
                    map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_SpecificationLink, specificationLinksIds);
                }
                
                if (usersIds.size() > 0) {
                    map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_User, usersIds);
                }
                
                if (extrinsicObjectsIds.size() > 0) {
                    map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ExtrinsicObject, extrinsicObjectsIds);
                }
            }
            
            // end if checking the size of registryObjectsIds
        } catch (SQLException e) {
            log.error("Caught exception", e);
            throw new RegistryException(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
                log.error("Caught exception", sqle);
            }
        }
        
        return map;
    }
    
    /**
     * Return true if the RegistryObject exist
     */
    public boolean registryObjectExist(String id)
    throws RegistryException {
        Statement stmt = null;
        
        try {
            stmt = context.getConnection().createStatement();
            
            String sql = "SELECT id from RegistryObject where id='" + id + "'";
            ResultSet rs = stmt.executeQuery(sql);
            boolean result = false;
            
            if (rs.next()) {
                result = true;
            }
            
            //stmt.close();
            return result;
        } catch (SQLException e) {
            throw new RegistryException(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
                log.error("Caught exception", sqle);
            }
        }
    }
    
    /**
     * Check whether the object exists in the specified table.
     */
    public boolean registryObjectExist(String id,
    String tableName) throws RegistryException {
        Statement stmt = null;
        
        try {
            stmt = context.getConnection().createStatement();
            
            String sql = "SELECT id from " + tableName + " where id='" + id +
            "'";
            ResultSet rs = stmt.executeQuery(sql);
            boolean result = false;
            
            if (rs.next()) {
                result = true;
            }
            
            //stmt.close();
            return result;
        } catch (SQLException e) {
            throw new RegistryException(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
                log.error("Caught exception", sqle);
            }
        }
    }
    
    /**
     * Returns List of ids of non-existent RegistryObject.
     */
    public List registryObjectsExist(List ids)
    throws RegistryException {
        List notExistIdList = new ArrayList();
        Iterator iter = ids.iterator();
        Statement stmt = null;
        
        try {
            stmt = context.getConnection().createStatement();
            
            while (iter.hasNext()) {
                String id = (String) iter.next();
                ResultSet rs = stmt.executeQuery(
                "select id from RegistryObject where " + "id = '" + id +
                "'");
                
                if (!rs.next()) {
                    // the RegistryObject does not exist
                    notExistIdList.add(id);
                }
            }
            
            //stmt.close();
        } catch (SQLException e) {
            throw new RegistryException(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
                log.error("Caught exception", sqle);
            }
        }
        
        return notExistIdList;
    }
    
    /**
     * Returns List of ids of non-existent RegistryObject.
     */
    public List registryObjectsExist(List ids, String tableName)
    throws RegistryException {
        List notExistIdList = new ArrayList();
        
        if (ids.size() == 0) {
            return notExistIdList;
        }
        
        Iterator iter = ids.iterator();
        Statement stmt = null;
        
        try {
            stmt = context.getConnection().createStatement();
            
            String sql = "SELECT id FROM " + tableName + " WHERE id IN (";
            List existingIdList = new ArrayList();
            
            /* We need to count the number of item in "IN" list. We need to split the a single
            SQL Strings if it is too long. Some database such as Oracle, does not
            allow the IN list is too long*/
            int listCounter = 0;
            
            while (iter.hasNext()) {
                String id = (String) iter.next();
                
                if (iter.hasNext() && (listCounter < 99)) {
                    sql += ("'" + id + "',");
                } else {
                    sql += ("'" + id + "')");
                    
                    //log.info("!!!!!!!!!!!!!!!!!!!" + sql);
                    ResultSet rs = stmt.executeQuery(sql);
                    
                    while (rs.next()) {
                        existingIdList.add(rs.getString("id"));
                    }
                    
                    sql = "SELECT id FROM " + tableName + " WHERE id IN (";
                    listCounter = 0;
                }
                
                listCounter++;
            }
            
            for (int i = 0; i < ids.size(); i++) {
                String id = (String) ids.get(i);
                
                if (!existingIdList.contains(id)) {
                    notExistIdList.add(id);
                }
            }
            
            //stmt.close();
        } catch (SQLException e) {
            throw new RegistryException(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
                log.error("Caught exception", sqle);
            }
        }
        
        return notExistIdList;
    }
    
    /**
     * Updates any passed RegistryObjects if they already exists.
     * Called by insert to handle implcit update of existing objects.
     *
     * @return List of RegistryObjects that are not existing
     */
    protected List processExistingObjects(List ros) throws RegistryException {
        BindingUtility bindingUtility = BindingUtility.getInstance();
        
        List notExistIds = registryObjectsExist(
        bindingUtility.getIdsFromRegistryObjects(ros), getTableName()); // getTableName() is the one which is the overidding one of subclass DAO
        List notExistROs = bindingUtility.getRegistryObjectsFromIds(ros,
        notExistIds);
        List existingROs = new ArrayList();
        Iterator rosIter = ros.iterator();
        
        while (rosIter.hasNext()) {
            RegistryObjectType ro = (RegistryObjectType) rosIter.next();
            
            if (!notExistROs.contains(ro)) {
                existingROs.add(ro);
            }
        }
        
        update(existingROs);
        
        return notExistROs;
    }
    
    
    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    Object createObject() throws JAXBException {
        RegistryObject obj = BindingUtility.getInstance().rimFac.createRegistryObject();
        
        return obj;
    }
    
    
    protected void loadObject(Object obj, ResultSet rs)
    throws RegistryException {
        try {
            if (!(obj instanceof RegistryObjectType)) {
                throw new RegistryException("Unexpected object " + obj +
                ". Was expecting org.oasis.ebxml.registry.bindings.rim.RegistryObjectType.");
            }
            
            RegistryObjectType ro = (RegistryObjectType) obj;
            
            ClassificationDAO classificationDAO = new ClassificationDAO(context);
            classificationDAO.setParent(ro);
            DescriptionDAO descriptionDAO = new DescriptionDAO(context);
            descriptionDAO.setParent(ro);            
            ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO(context);
            externalIdentifierDAO.setParent(ro);
            NameDAO nameDAO = new NameDAO(context);
            nameDAO.setParent(ro);
            SlotDAO slotDAO = new SlotDAO(context);
            slotDAO.setParent(ro);
        
            String id = rs.getString("id");
            ro.setId(id);
            
            String home = rs.getString("home");
            if (home != null) {
                ro.setHome(home);
            }
            
            String objectType = rs.getString("objectType");
            ro.setObjectType(objectType);
            
            Name name = nameDAO.getNameByParent(id);
            ro.setName(name);
            
            Description desc = descriptionDAO.getDescriptionByParent(id);
            ro.setDescription(desc);
            
            List slots = slotDAO.getSlotsByParent(id);
            ro.getSlot().addAll(slots);
            
            boolean returnComposedObjects = context.getResponseOption().isReturnComposedObjects();
            
            if (returnComposedObjects) {
                List classifications = classificationDAO.getByParent();
                ro.getClassification().addAll(classifications);
                
                List extIds = externalIdentifierDAO.getByParent();
                ro.getExternalIdentifier().addAll(extIds);
            }
        } catch (SQLException e) {
            log.error("Caught exception", e);
            throw new RegistryException(e);
        }
    }
    
    private void getRegistryObjectsIdsFromResultSet(ResultSet rs,
    StringBuffer associations, StringBuffer auEvents,
    StringBuffer classifications, StringBuffer schemes,
    StringBuffer classificationNodes, StringBuffer externalIds,
    StringBuffer externalLinks, StringBuffer extrinsicObjects,
    StringBuffer organizations, StringBuffer packages,
    StringBuffer serviceBindings, StringBuffer services,
    StringBuffer specificationLinks, StringBuffer subscriptions, StringBuffer users)
    throws SQLException, RegistryException {
        while (rs.next()) {
            String id = rs.getString("id");
            String type = rs.getString("objectType");            
            
            //log.info("objectType = '" + type + "'");
            if (type.equalsIgnoreCase(BindingUtility.getInstance().CANONICAL_OBJECT_TYPE_ID_Association)) {
                if (associations.length() == 0) {
                    associations.append("'" + id + "'");
                } else {
                    associations.append(",'" + id + "'");
                }
            } else if (type.equalsIgnoreCase(BindingUtility.getInstance().CANONICAL_OBJECT_TYPE_ID_AuditableEvent)) {
                if (auEvents.length() == 0) {
                    auEvents.append("'" + id + "'");
                } else {
                    auEvents.append(",'" + id + "'");
                }
            } else if (type.equalsIgnoreCase(BindingUtility.getInstance().CANONICAL_OBJECT_TYPE_ID_Classification)) {
                if (classifications.length() == 0) {
                    classifications.append("'" + id + "'");
                } else {
                    classifications.append(",'" + id + "'");
                }
            } else if (type.equalsIgnoreCase(BindingUtility.getInstance().CANONICAL_OBJECT_TYPE_ID_ClassificationNode)) {
                if (classificationNodes.length() == 0) {
                    classificationNodes.append("'" + id + "'");
                } else {
                    classificationNodes.append(",'" + id + "'");
                }
            } else if (type.equalsIgnoreCase(BindingUtility.getInstance().CANONICAL_OBJECT_TYPE_ID_ClassificationScheme)) {
                if (schemes.length() == 0) {
                    schemes.append("'" + id + "'");
                } else {
                    schemes.append(",'" + id + "'");
                }
            } else if (type.equalsIgnoreCase(BindingUtility.getInstance().CANONICAL_OBJECT_TYPE_ID_ExternalIdentifier)) {
                if (externalIds.length() == 0) {
                    externalIds.append("'" + id + "'");
                } else {
                    externalIds.append(",'" + id + "'");
                }
            } else if (type.equalsIgnoreCase(BindingUtility.getInstance().CANONICAL_OBJECT_TYPE_ID_ExternalLink)) {
                if (externalLinks.length() == 0) {
                    externalLinks.append("'" + id + "'");
                } else {
                    externalLinks.append(",'" + id + "'");
                }
            } else if (type.equalsIgnoreCase(BindingUtility.getInstance().CANONICAL_OBJECT_TYPE_ID_ExtrinsicObject)) {
                if (extrinsicObjects.length() == 0) {
                    extrinsicObjects.append("'" + id + "'");
                } else {
                    extrinsicObjects.append(",'" + id + "'");
                }
            } else if (type.equalsIgnoreCase(BindingUtility.getInstance().CANONICAL_OBJECT_TYPE_ID_Organization)) {
                if (organizations.length() == 0) {
                    organizations.append("'" + id + "'");
                } else {
                    organizations.append(",'" + id + "'");
                }
            } else if (type.equalsIgnoreCase(BindingUtility.getInstance().CANONICAL_OBJECT_TYPE_ID_RegistryPackage)) {
                if (packages.length() == 0) {
                    packages.append("'" + id + "'");
                } else {
                    packages.append(",'" + id + "'");
                }
            } else if (type.equalsIgnoreCase(BindingUtility.getInstance().CANONICAL_OBJECT_TYPE_ID_ServiceBinding)) {
                if (serviceBindings.length() == 0) {
                    serviceBindings.append("'" + id + "'");
                } else {
                    serviceBindings.append(",'" + id + "'");
                }
            } else if (type.equalsIgnoreCase(BindingUtility.getInstance().CANONICAL_OBJECT_TYPE_ID_Service)) {
                if (services.length() == 0) {
                    services.append("'" + id + "'");
                } else {
                    services.append(",'" + id + "'");
                }
            } else if (type.equalsIgnoreCase(BindingUtility.getInstance().CANONICAL_OBJECT_TYPE_ID_SpecificationLink)) {
                if (specificationLinks.length() == 0) {
                    specificationLinks.append("'" + id + "'");
                } else {
                    specificationLinks.append(",'" + id + "'");
                }
            } else if (type.equalsIgnoreCase(BindingUtility.getInstance().CANONICAL_OBJECT_TYPE_ID_Subscription)) {
                if (subscriptions.length() == 0) {
                    subscriptions.append("'" + id + "'");
                } else {
                    subscriptions.append(",'" + id + "'");
                }
            } else if (type.equalsIgnoreCase(BindingUtility.getInstance().CANONICAL_OBJECT_TYPE_ID_User)) {
                if (users.length() == 0) {
                    users.append("'" + id + "'");
                } else {
                    users.append(",'" + id + "'");
                }
            } else {
                //Type is user defined. Table could be either ExtrinsicObject or ExternalLink
                SQLPersistenceManagerImpl pm = SQLPersistenceManagerImpl.getInstance();
                ExtrinsicObjectType eo = (ExtrinsicObjectType)pm.
                    getRegistryObjectMatchingQuery("SELECT * from ExtrinsicObject where id = '"+id+"'", "ExtrinsicObject");
                
                if (eo != null) {                       
                    if (extrinsicObjects.length() == 0) {
                        extrinsicObjects.append("'" + id + "'");
                    } else {
                        extrinsicObjects.append(",'" + id + "'");
                    }
                }
                else {
                    ExternalLinkType el = (ExternalLinkType)pm.
                        getRegistryObjectMatchingQuery("SELECT * from ExternalLink where id = '"+id+"'", "ExternalLink");
                    
                    if (el != null) {                       
                        if (externalLinks.length() == 0) {
                            externalLinks.append("'" + id + "'");
                        } else {
                            externalLinks.append(",'" + id + "'");
                        }
                    }
                    else {
                        throw new RegistryException("Unknown objectType: '" + type + "'");
                    }                                         
                }
            }
        }
    }
    
    /**
     * Gets the List of binding objects for specified ResultSet.
     * This method return leaf object types while the base class
     * version returns RegistryObjects. 
     *
     */
    public List getObjectsHetero(ResultSet rs) throws RegistryException {
        List res = new ArrayList();
        String sql = null;
        
        StringBuffer associationsIds = new StringBuffer();
        StringBuffer auditableEventsIds = new StringBuffer();
        StringBuffer classificationsIds = new StringBuffer();
        StringBuffer schemesIds = new StringBuffer();
        StringBuffer classificationNodesIds = new StringBuffer();
        StringBuffer externalIdsIds = new StringBuffer();
        StringBuffer externalLinksIds = new StringBuffer();
        StringBuffer extrinsicObjectsIds = new StringBuffer();
        StringBuffer organizationsIds = new StringBuffer();
        StringBuffer packagesIds = new StringBuffer();
        StringBuffer serviceBindingsIds = new StringBuffer();
        StringBuffer servicesIds = new StringBuffer();
        StringBuffer specificationLinksIds = new StringBuffer();
        StringBuffer subscriptionIds = new StringBuffer();
        StringBuffer usersIds = new StringBuffer();
        
        Statement stmt = null;
        
        try {
            stmt = context.getConnection().createStatement();
            
            getRegistryObjectsIdsFromResultSet(rs, associationsIds,
            auditableEventsIds, classificationsIds, schemesIds,
            classificationNodesIds, externalIdsIds, externalLinksIds,
            extrinsicObjectsIds, organizationsIds, packagesIds,
            serviceBindingsIds, servicesIds, specificationLinksIds, subscriptionIds, usersIds);
            
            ResultSet leafObjectsRs = null;
            
            if (associationsIds.length() > 0) {
                AssociationDAO assDAO = new AssociationDAO(context);
                sql = "SELECT * FROM " + assDAO.getTableName() +
                " WHERE id IN (" + associationsIds + ")";
                
                //System.out.println(sql);
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(assDAO.getObjects(leafObjectsRs));
            }
            
            if (auditableEventsIds.length() > 0) {
                AuditableEventDAO aeDAO = new AuditableEventDAO(context);
                sql = "SELECT * FROM " + aeDAO.getTableName() +
                " WHERE id IN (" + auditableEventsIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(aeDAO.getObjects(leafObjectsRs));
            }
            
            if (classificationsIds.length() > 0) {
                ClassificationDAO classificationDAO = new ClassificationDAO(context);
                sql = "SELECT * FROM " + classificationDAO.getTableName() +
                " WHERE id IN (" + classificationsIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(classificationDAO.getObjects(leafObjectsRs));
            }
            
            if (schemesIds.length() > 0) {
                ClassificationSchemeDAO schemeDAO = new ClassificationSchemeDAO(context);
                sql = "SELECT * FROM " + schemeDAO.getTableName() +
                " WHERE id IN (" + schemesIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(schemeDAO.getObjects(leafObjectsRs));
            }
            
            if (classificationNodesIds.length() > 0) {
                ClassificationNodeDAO nodeDAO = new ClassificationNodeDAO(context);
                sql = "SELECT * FROM " + nodeDAO.getTableName() +
                " WHERE id IN (" + classificationNodesIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(nodeDAO.getObjects(leafObjectsRs));
            }
            
            if (externalIdsIds.length() > 0) {
                ExternalIdentifierDAO externalIdDAO = new ExternalIdentifierDAO(context);
                sql = "SELECT * FROM " + externalIdDAO.getTableName() +
                " WHERE id IN (" + externalIdsIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(externalIdDAO.getObjects(leafObjectsRs));
            }
            
            if (externalLinksIds.length() > 0) {
                ExternalLinkDAO externalLinkDAO = new ExternalLinkDAO(context);
                sql = "SELECT * FROM " + externalLinkDAO.getTableName() +
                " WHERE id IN (" + externalLinksIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(externalLinkDAO.getObjects(leafObjectsRs));
            }
            
            if (extrinsicObjectsIds.length() > 0) {
                ExtrinsicObjectDAO extrinsicObjectDAO = new ExtrinsicObjectDAO(context);
                sql = "SELECT * FROM " + extrinsicObjectDAO.getTableName() +
                " WHERE id IN (" + extrinsicObjectsIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(extrinsicObjectDAO.getObjects(leafObjectsRs));
            }
            
            if (organizationsIds.length() > 0) {
                OrganizationDAO organizationDAO = new OrganizationDAO(context);
                sql = "SELECT * FROM " + organizationDAO.getTableName() +
                " WHERE id IN (" + organizationsIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(organizationDAO.getObjects(leafObjectsRs));
            }
            
            if (packagesIds.length() > 0) {
                RegistryPackageDAO pkgDAO = new RegistryPackageDAO(context);
                sql = "SELECT * FROM " + pkgDAO.getTableName() +
                " WHERE id IN (" + packagesIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(pkgDAO.getObjects(leafObjectsRs));
            }
            
            if (serviceBindingsIds.length() > 0) {
                ServiceBindingDAO serviceBindingDAO = new ServiceBindingDAO(context);
                sql = "SELECT * FROM " + serviceBindingDAO.getTableName() +
                " WHERE id IN (" + serviceBindingsIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(serviceBindingDAO.getObjects(leafObjectsRs));
            }
            
            if (servicesIds.length() > 0) {
                ServiceDAO serviceDAO = new ServiceDAO(context);
                sql = "SELECT * FROM " + serviceDAO.getTableName() +
                " WHERE id IN (" + servicesIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(serviceDAO.getObjects(leafObjectsRs));
            }
            
            if (specificationLinksIds.length() > 0) {
                SpecificationLinkDAO specLinkDAO = new SpecificationLinkDAO(context);
                sql = "SELECT * FROM " + specLinkDAO.getTableName() +
                " WHERE id IN (" + specificationLinksIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(specLinkDAO.getObjects(leafObjectsRs));
            }
            
            if (usersIds.length() > 0) {
                UserDAO userDAO = new UserDAO(context);
                sql = "SELECT * FROM " + userDAO.getTableName() +
                " WHERE id IN (" + usersIds + ")";
                leafObjectsRs = stmt.executeQuery(sql);
                res.addAll(userDAO.getObjects(leafObjectsRs));
            }
        } catch (SQLException e) {
            log.error("Caught exception", e);
            throw new RegistryException(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
                log.error("Caught exception", sqle);
            }
        }
        
        return res;
    }
    
    /**
     * Get the User that is the owner for the object with specified objectId
     */
    public User getOwner(String objectId)
    throws RegistryException, SQLException {
        User owner = null;
        
        try {
            Statement stmt = context.getConnection().createStatement();
            
            String query =
            "SELECT * from User_ u WHERE u.id IN (SELECT user_ from AuditableEvent where registryObject = '" +
            objectId + "' AND eventType = '" + BindingUtility.CANONICAL_EVENT_TYPE_Created + "' )";
            log.trace("query=" + query);
            
            ResultSet rs = stmt.executeQuery(query);
            
            UserDAO userDAO = new UserDAO(context);
            
            if (rs.next()) {
                owner = BindingUtility.getInstance().rimFac.createUser();
                
                ResponseOption responseOption = BindingUtility.getInstance().queryFac.createResponseOption();
                responseOption.setReturnComposedObjects(false);
                userDAO.loadObject( owner, rs);
            } else {
                throw new OwnerNotFoundException(objectId);
            }
            
            stmt.close();
        } catch (JAXBException e) {
            throw new RegistryException(e);
        }
        
        return owner;
    }
    
    /**
     * Get a HashMap with registry object id as key and owner id as value
     */
    public HashMap getOwnersMap(List ids)
    throws RegistryException {
        Statement stmt = null;
        HashMap ownersMap = new HashMap();
        
        if (ids.size() == 0) {
            return ownersMap;
        }
        
        try {
            stmt = context.getConnection().createStatement();
            
            StringBuffer idsList = BindingUtility.getInstance()
            .getIdListFromIds(ids);
                        
            String query =
            "SELECT ao.id, ae.user_ FROM AuditableEvent ae, AffectedObject ao WHERE ao.eventId = ae.id AND ao.id IN (" +
            idsList + ") AND ae.eventType = '" + BindingUtility.CANONICAL_EVENT_TYPE_Created + "'";
            log.trace(query);
            
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                ownersMap.put(rs.getString(1), rs.getString(2));
            }
            
            return ownersMap;
        } catch (SQLException e) {
            log.error("Caught exception", e);
            throw new RegistryException(e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
                log.error("Caught exception", sqle);
            }
        }
    }
    
    /**
     * Update the status of specified objects (homogenous collection) to the specified status.
     * @param statusUnchanged if an id in registryObjectIds is in this ArrayList, no AuditableEvent
     * generated for that RegistryEntry
     */
    public void updateStatus(List registryObjectIds, String status, List statusUnchanged)
        throws RegistryException {
        Statement stmt = null;
                                                                                
        if (registryObjectIds.size() == 0) {
            return;
        }
                                                                                
        try {
            stmt = context.getConnection().createStatement();
                                                                                
            String str = "UPDATE " + getTableName() + " SET status = '" +
                status + "' WHERE id IN ( ";
                                                                                
            Iterator iter = registryObjectIds.iterator();
                                                                                
            while (iter.hasNext()) {
                String id = (String) iter.next();
                                                                                
                // Generate AuditableEvents
                ObjectRef ro = bu.rimFac.createObjectRef();
                ro.setId(id);

                if (status.equals(Status._APPROVED)) {
                    /*It is incorrect! We should generate Approved event.
                    But EventTypeType.valueOf("Approved") throws IllegalArgumentException
                     */
                    if (!statusUnchanged.contains(id)) {
                        //generateAuditbleEvent(ro, "Updated", user);
                    }
                } else if (status.equals(Status._DEPRECATED)) {
                    if (!statusUnchanged.contains(id)) {
                        //generateAuditbleEvent(ro, "Deprecated", user);
                    }
                }
                                                                                
                if (iter.hasNext()) {
                    str += ("'" + id + "', ");
                } else {
                    str += ("'" + id + "' ) ");
                }
            }
                                                                                
            log.trace("stmt = " + str);
            stmt.execute(str);
                                                                                
            //stmt.close();
        } catch (SQLException e) {
            log.error("Caught exception", e);
            throw new RegistryException(e);
        } catch (JAXBException j) {
            log.error("Caught exception", j);
            throw new RegistryException(j);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
                log.error("Caught exception", sqle);
            }
        }
    }
                                                                                                         
}
