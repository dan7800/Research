/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/persistence/rdb/AffectedObjectDAO.java,v 1.5 2003/11/13 02:38:52 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.persistence.rdb;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.bind.JAXBException;

import org.freebxml.omar.common.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;


/**
 *
 * @see <{RegistryEntry}>
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 */
class AffectedObjectDAO extends AbstractDAO {

    /**
     * Use this constructor only.
     */
    AffectedObjectDAO(DAOContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "AffectedObject";
    }

    public String getTableName() {
        return getTableNameStatic();
    }
                        
    /**
     * Returns the SQL fragment string needed by insert or update statements 
     * within insert or update method of sub-classes. This is done to avoid code
     * duplication.
     */
    protected String getSQLStatementFragment(Object object) throws RegistryException {
        //object must be an ObjectRef for an objects affected by parent event
        ObjectRefType affectedObject = (ObjectRefType)object;
            
        String stmtFragment = null;
        
        
        String id = affectedObject.getId();
        String home = affectedObject.getHome();
        
        if (home != null) {
            home = "'" + home + "'";
        }
        
        String eventId = ((AuditableEventType)parent).getId();
        
        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO AffectedObject VALUES(" +
                    " '" + id + 
                    "', " + home + 
                    ", '" + eventId + 
                    "' ) ";
        }
        else if (action == DAO_ACTION_UPDATE) {
            throw new RegistryException("Cannot update a composed object.");
        }
        else if (action == DAO_ACTION_DELETE) {
            stmtFragment = super.getSQLStatementFragment(object);
        }
        
        return stmtFragment;
    }
    

    protected void loadObject( Object obj, ResultSet rs) throws RegistryException {
        try {
            if (!(obj instanceof org.oasis.ebxml.registry.bindings.rim.ObjectRefType)) {
                throw new RegistryException("Unexpected object " + obj +
                    ". Was expecting org.oasis.ebxml.registry.bindings.rim.ObjectRefType.");
            }

            ObjectRefType ro = (ObjectRefType) obj;
            ro.setId(rs.getString("id"));
            
            String home = rs.getString("home");
            if (home != null) {
                ro.setHome(home);
            }
            
        } catch (SQLException e) {
            log.error("Caught exception", e);
            throw new RegistryException(e);
        }
    }
    
    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    Object createObject() throws JAXBException {
        ObjectRefType obj = bu.rimFac.createObjectRef();
        
        return obj;
    }
    
    /*
     * Gets the column name that is foreign key ref into parent table.
     * Must be overridden by derived class if it is not 'parent'
     */
    protected String getParentAttribute() {
        return "id";
    }
        
}
