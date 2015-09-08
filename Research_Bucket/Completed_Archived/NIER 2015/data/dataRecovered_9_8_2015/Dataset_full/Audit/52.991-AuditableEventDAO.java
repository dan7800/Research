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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import org.oasis.ebxml.registry.bindings.query.ResponseOption;
import org.oasis.ebxml.registry.bindings.rim.AuditableEvent;
import org.oasis.ebxml.registry.bindings.rim.ObjectRef;
import org.oasis.ebxml.registry.bindings.rim.User;
import org.oasis.ebxml.registry.bindings.rim.types.EventTypeType;

import com.sun.ebxml.registry.RegistryException;


public class AuditableEventDAO extends RegistryObjectDAO {
protected AuditableEventDAO(){}

	public static String getTableNameStatic() {
		return "AuditableEvent";
	}

        public String getTableName() {
            return getTableNameStatic();
        }
    
	/**
	@deprecated
	*/
	public void insert(java.sql.Statement stmt
		, ArrayList auditableEvents) throws RegistryException {
		try {
			//Statement stmt = connection.createStatement();
			Iterator iter = auditableEvents.iterator();
			while (iter.hasNext()) {
				AuditableEvent auditableEvent = (AuditableEvent) iter.next();
                                
                                java.sql.Timestamp timestamp = new java.sql.Timestamp(auditableEvent.getTimestamp().getTime());
                                //??The timestamp is being truncated to work around a bug in PostgreSQL 7.2.2 JDBC driver
                                String timestampStr = timestamp.toString().substring(0,19);
                               // System.err.println("*********************************************timestamp = " + timestampStr);
                                
				//System.err.println("ae id: " + auditableEvent.getId());
				String sql = "insert into " + getTableName() + " values ("
				+ "null" //  accessControlPolicy
				+ ",'"+ auditableEvent.getId() +"'"  // id
				+ ",'"+ auditableEvent.getObjectType() +"'"  // objectType
				+ ",'"+ auditableEvent.getEventType() +"'"  // eventType
				+ ",'"+ auditableEvent.getRegistryObject() +"'"  // registryObject
				+ ",'"+ timestampStr +"'"  // timeStamp_
				+ ",'"+ ((User)auditableEvent.getUser()).getId() +"'"  // user_
				+ ")";
				//Log.print(Log.TRACE, 5, "Inserting AuditableEvent..."); 
				//Log.print(Log.TRACE, 5, sql);
				stmt.addBatch(sql);
			}
			if (auditableEvents.size() > 0) {
				stmt.executeBatch();
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			RegistryException exception = new RegistryException(e);
			throw exception;
		}	
	}
	
	public void insert(java.sql.Connection conn
		, ArrayList auditableEvents) throws RegistryException {
		String auditableEventId = null;
    Statement stmt = null;
        if (auditableEvents.size()==0) {
            return;
        }
		try {
			stmt = conn.createStatement();
			Iterator iter = auditableEvents.iterator();
			while (iter.hasNext()) {
				AuditableEvent auditableEvent = (AuditableEvent) iter.next();
				auditableEventId = auditableEvent.getId();

                                java.sql.Timestamp timestamp = new java.sql.Timestamp(auditableEvent.getTimestamp().getTime());
                                //??The timestamp is being truncated to work around a bug in PostgreSQL 7.2.2 JDBC driver
                                String timestampStr = timestamp.toString().substring(0,19);
                                //System.err.println("*********************************************timestamp = " + timestampStr);

                                //System.err.println("ae id: " + auditableEvent.getId());
				String sql = "insert into " + getTableName() + " values ("
				+ "null" //  accessControlPolicy
				+ ",'"+ auditableEvent.getId() +"'"  // id
				+ ",'"+ auditableEvent.getObjectType() +"'"  // objectType
				+ ",'"+ auditableEvent.getEventType() +"'"  // eventType
				+ ",'"+ auditableEvent.getRegistryObject() +"'"  // registryObject
				+ ",'"+  timestampStr +"'"  // timeStamp_
				+ ",'"+ ((User)auditableEvent.getUser()).getId() +"'"  // user_
				+ ")";
				//Log.print(Log.TRACE, 5, "Inserting AuditableEvent..."); 
				//Log.print(Log.TRACE, 5, sql);
				stmt.addBatch(sql);
			}
			if (auditableEvents.size() > 0) {
				stmt.executeBatch();
                //stmt.close();
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			/* It is trying to find out whether the SQLException is because
			the AuditableEvent already exist. This error is because the registry
			generate a AuditableEvent with a Id owned by another AuditableEvent.
			It is very rare. It happens when UUIDFactory can't generate a unique
			id
			*/
			if (auditableEventId != null && registryObjectExist(conn, auditableEventId)) {
				throw new RegistryException("Internal error: The registry " + 
				"cannot generate AuditableEvent with unique id");
			}  				    
			RegistryException exception = new RegistryException(e);
			throw exception;
		}	finally {
        try {
            if (stmt != null)
                stmt.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
	}
        		
	public void update(java.sql.Connection conn, ArrayList auditableEvents) {
	
	}
				
	public void loadObjectFromResultSet(java.sql.Connection conn, Object obj, ResultSet rs, ResponseOption responseOption, ArrayList objectRefs) throws RegistryException {
		try {
			if (!(obj instanceof org.oasis.ebxml.registry.bindings.rim.AuditableEvent)) {
				throw new RegistryException("Unexpected object " + obj + ". Was expecting org.oasis.ebxml.registry.bindings.rim.AuditableEvent.");
			}
					
			AuditableEvent ae = (AuditableEvent)obj;
			super.loadObjectFromResultSet(conn, obj, rs, responseOption, objectRefs);

			String eventType = rs.getString("eventType");
                        EventTypeType ett = EventTypeType.valueOf(eventType);
			ae.setEventType(ett);
                        
			String registryObjectId = rs.getString("registryObject");
			ObjectRef ro = new ObjectRef();
			objectRefs.add(ro);						
			ro.setId(registryObjectId);
			ae.setRegistryObject(ro);
                        
                        //Workaround for bug in PostgreSQL 7.2.2 JDBC driver
 			//java.sql.Timestamp timeStamp = rs.getTimestamp("timeStamp_");
                        String timestampStr = rs.getString("timeStamp_").substring(0,19);
 			java.sql.Timestamp timeStamp = java.sql.Timestamp.valueOf(timestampStr);
			ae.setTimestamp(timeStamp);

                        String userId = rs.getString("user_");                        
			ro = new ObjectRef();
			objectRefs.add(ro);						
			ro.setId(userId);
			ae.setUser(ro);
                        
                       
                        
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new RegistryException(e);
		}
	}	

	public ArrayList getLeafObjectList(java.sql.Connection conn, ResultSet rs, ResponseOption responseOption, ArrayList objectRefs) throws RegistryException {
		ArrayList res = new ArrayList();

		try {
			while(rs.next()) {
				AuditableEvent obj = new AuditableEvent();
				loadObjectFromResultSet(conn, obj, rs, responseOption, objectRefs);
				
				res.add(obj);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new RegistryException(e);
		}
		

		return res;
	}	

    public static AuditableEventDAO getInstance(){
            if (instance == null) {
                synchronized(AuditableEventDAO.class) {
                    if (instance == null) {
                        instance = new AuditableEventDAO();
                    }
                }
            }
            return instance;
        }

    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory 
     */
    /*# private AuditableEventDAO _auditableEventDAO; */
    private static AuditableEventDAO instance = null;
}
/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/sino/omar/src/java/org/freebxml/omar/server/persistence/rdb/AuditableEventDAO.java,v 1.21 2003/11/13 02:38:52 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.persistence.rdb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.AuditableEvent;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRef;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;


class AuditableEventDAO extends RegistryObjectDAO {

    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */


    /**
     * Use this constructor only.
     */
    AuditableEventDAO(DAOContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "AuditableEvent";
    }

    public String getTableName() {
        return getTableNameStatic();
    }

    /**
     * Delete composed objects that have the specified registryObject
     * as parent.
     */
    protected void deleteComposedObjects(Object object) throws RegistryException {
        super.deleteComposedObjects(object);
        AuditableEventType ae = (AuditableEventType)object;
        
        AffectedObjectDAO affectedObjectDAO = new AffectedObjectDAO(context);
        affectedObjectDAO.setParent(object);        
        
        //Delete affectedObjects
        affectedObjectDAO.deleteByParent();
        
    }
    
    /**
     * Insert the composed objects for the specified registryObject
     */
    protected void insertComposedObjects(Object object) throws RegistryException {
        super.insertComposedObjects(object);
        AuditableEventType ae = (AuditableEventType)object;
        String id = ae.getId();
        
        
        AffectedObjectDAO affectedObjectDAO = new AffectedObjectDAO(context);
        affectedObjectDAO.setParent(object);        
        List affectedObjects = ae.getAffectedObject().getObjectRef();
        
        //Insert affectedObjects
        affectedObjectDAO.insert(affectedObjects);        
    }
    
    /**
     * Returns the SQL fragment string needed by insert or update statements 
     * within insert or update method of sub-classes. This is done to avoid code
     * duplication.
     */
    protected String getSQLStatementFragment(Object ro) throws RegistryException {

        AuditableEventType auditableEvent = (AuditableEventType)ro;
            
        String stmtFragment = null;
               
        String requestId = auditableEvent.getRequestId();
        String eventType = auditableEvent.getEventType();
        
        Timestamp timestamp = new Timestamp(auditableEvent.getTimestamp()
                                                          .getTimeInMillis());

        //??The timestamp is being truncated to work around a bug in PostgreSQL 7.2.2 JDBC driver
        String timestampStr = timestamp.toString().substring(0, 19);
        
        String aeUser = auditableEvent.getUser();
        
        if (action == DAO_ACTION_INSERT) {
            stmtFragment = "INSERT INTO AuditableEvent " +
                super.getSQLStatementFragment(ro) +
                    ", '" + requestId + 
                    "', '" + eventType + 
                    "', '" + timestampStr + 
                    "', '" + aeUser + 
                    "' ) ";            
        }
        else if (action == DAO_ACTION_UPDATE) {
            stmtFragment = "UPDATE AuditableEvent SET " +
                super.getSQLStatementFragment(ro) +
                    ", requestId='" + requestId + 
                    "', eventType='" + eventType + 
                    "', timestamp='" + timestampStr + 
                    "', user='" + aeUser + 
                    "' WHERE id = '" + ((RegistryObjectType)ro).getId() + "' ";
        }
        else if (action == DAO_ACTION_DELETE) {
            stmtFragment = super.getSQLStatementFragment(ro);
        }
        
        return stmtFragment;
    }
    
    protected void loadObject( Object obj, ResultSet rs) throws RegistryException {
        try {
            if (!(obj instanceof AuditableEvent)) {
                throw new RegistryException("Unexpected object " + obj +
                    ". Was expecting org.oasis.ebxml.registry.bindings.rim.AuditableEvent.");
            }

            AuditableEvent ae = (AuditableEvent) obj;
            super.loadObject( obj, rs);

            //TODO: Fix so requestId is properly supported
            String requestId = rs.getString("requestId");
            if (requestId == null) {
                requestId = "Unknown";
            }
            ae.setRequestId(requestId);
            
            String eventType = rs.getString("eventType");
            ae.setEventType(eventType);
            
            //Workaround for bug in PostgreSQL 7.2.2 JDBC driver
            //java.sql.Timestamp timeStamp = rs.getTimestamp("timeStamp_"); --old
            String timestampStr = rs.getString("timeStamp_").substring(0,19);
            Timestamp timeStamp = Timestamp.valueOf(timestampStr);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timeStamp.getTime());
            ae.setTimestamp(cal);
            
            String userId = rs.getString("user_");
            ObjectRef ref = bu.rimFac.createObjectRef();
            ref.setId(userId);
            context.getObjectRefs().add(ref);
            ae.setUser(userId);
            
            AffectedObjectDAO affectedDAO = new AffectedObjectDAO(context);
            affectedDAO.setParent(ae);
            List affectedObjects = affectedDAO.getByParent();
            ObjectRefListType orefList = BindingUtility.getInstance().rimFac.createObjectRefListType();
            orefList.getObjectRef().addAll(affectedObjects);
            ae.setAffectedObject(orefList);
            
        } catch (SQLException e) {
            log.error("Caught exception", e);
            throw new RegistryException(e);
        } catch (JAXBException e) {
            log.error("Caught exception", e);
            throw new RegistryException(e);
        }
    }

    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    Object createObject() throws JAXBException {
        AuditableEvent obj = bu.rimFac.createAuditableEvent();
        Calendar cal = Calendar.getInstance();
        obj.setTimestamp(cal);
        
        return obj;
    }
    
}
