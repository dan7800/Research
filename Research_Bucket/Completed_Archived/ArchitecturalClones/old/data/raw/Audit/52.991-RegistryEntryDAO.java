/*
 * $Header: /cvsroot/sino/ebxmlrr/src/share/com/sun/ebxml/registry/persistence/rdb/RegistryEntryDAO.java,v 1.15 2003/05/30 22:42:37 waikei Exp $
 */

package com.sun.ebxml.registry.persistence.rdb;

import java.util.*;
import java.sql.*;

import org.oasis.ebxml.registry.bindings.rs.RegistryError;
import org.oasis.ebxml.registry.bindings.rs.RegistryErrorList;
import org.oasis.ebxml.registry.bindings.rs.types.ErrorType;
import org.oasis.ebxml.registry.bindings.rim.*;
import org.oasis.ebxml.registry.bindings.rim.types.*;
import org.oasis.ebxml.registry.bindings.query.*;
import org.oasis.ebxml.registry.bindings.query.types.*;

import com.sun.ebxml.registry.*;
import com.sun.ebxml.registry.lcm.*;

/**
 *
 * @author Farrukh S. Najmi
 */
public class RegistryEntryDAO extends RegistryObjectDAO {
	
	public static String getTableNameStatic() {
		return "RegistryEntry";
	}
	
        public String getTableName() {
            return getTableNameStatic();
        }
    
	/**
	 * Update the status of specified objects (homogenous collection) to the specified status.
	 * @param statusUnchanged if an id in registryObjectIds is in this ArrayList, no AuditableEvent 
	 * generated for that RegistryEntry
	 */
	public void updateStatus(User user, java.sql.Connection connection, ArrayList registryObjectIds
	, StatusType status, ArrayList statusUnchanged) throws RegistryException {

    Statement stmt = null;
		if (registryObjectIds.size()==0) {
			return;
		}
		try
		{
			stmt = connection.createStatement();

			String str = "UPDATE " + getTableName() +
					" SET status = '" + status + "' WHERE id IN ( ";
					
			Iterator iter = registryObjectIds.iterator();
			while (iter.hasNext())
			{
				String id = (String)iter.next();
				// Generate AuditableEvents
				RegistryObject ro = new RegistryObject();
				ro.setId(id);
				if (status.getType() == StatusType.APPROVED.getType()) {
					/*It is incorrect! We should generate Approved event.
					But EventTypeType.valueOf("Approved") throws IllegalArgumentException
					*/
					if (!statusUnchanged.contains(id)) {
						generateAuditbleEvent(connection, ro, "Updated", user);
					}
				}
				else if (status.getType() == StatusType.DEPRECATED.getType()) {
					if (!statusUnchanged.contains(id)) {
						generateAuditbleEvent(connection, ro, "Deprecated", user);
					}
				}
				if (iter.hasNext()) {
					str += "'" + id + "', ";
				}
				else {
					str += "'" + id + "' ) ";
				}
			}

			System.err.println("stmt = " + str);
			stmt.execute(str);
            //stmt.close();
		}
		catch (java.sql.SQLException e)	{
			RegistryException exception = new RegistryException(e);
			throw exception;
		} finally {
        try {
            if (stmt != null)
                stmt.close();
        } catch (SQLException sqle) {
		        sqle.printStackTrace();
        }
	  }
	}
	
	/**
	@param idStatus The HashMap storing id String as key and Status String as value
	@param objectTypeIds The HashMap storing objectType as key and ArrayList of Id as value.
	All the objectType in database not ClassificationScheme or RegistryPackage the
	key is stored as "ExtrinsicObject"
	*/
	private void getIdStatusAndObjectTypeIdsMap(java.sql.Connection connection, ArrayList ids
	, HashMap idStatus, HashMap objectTypeIds) throws RegistryException {
		if (ids.size()==0) {
			return;
		}
		ArrayList classSchemesIds = new ArrayList();
		ArrayList pkgsIds = new ArrayList();
		ArrayList extObjectsIds = new ArrayList();
    Statement stmt = null;
		try {
			stmt = connection.createStatement();
			String sql = "SELECT id, objectType, status FROM RegistryEntry WHERE id IN (";
			Iterator idsIter = ids.iterator();
			while (idsIter.hasNext()) {
				String id = (String)idsIter.next();
				if (idsIter.hasNext()) {
					sql += "'" + id + "', ";
				}
				else {
					sql += "'" + id + "' ) ";
				}
			}
			ResultSet rs = stmt.executeQuery(sql);
			String id = null;
			String objectType = null;
			String status = null;
			while(rs.next()) {
				id = rs.getString("id");
				objectType = rs.getString("objectType");
				status = rs.getString("status");
				idStatus.put(id, status);
				if (objectType.equalsIgnoreCase("ClassificationScheme")) {
					classSchemesIds.add(id);
				}
				else if (objectType.equalsIgnoreCase("RegistryPackage")) {
					pkgsIds.add(id);
				}
				else {
					// All the remaining objectType are considered as ExtrinsicObject
					extObjectsIds.add(id);
				}
			} // end looping the ResultSet
			if (classSchemesIds.size() > 0) {
				objectTypeIds.put("ClassificationScheme", classSchemesIds);
			}
			if (pkgsIds.size() > 0) {
				objectTypeIds.put("RegistryPackage", pkgsIds);
			}
			if (extObjectsIds.size() > 0) {
				objectTypeIds.put("ExtrinsicObject", extObjectsIds);
			}
		}
		catch(SQLException e) {
			RegistryException exception = new RegistryException(e);
			throw exception;
		} finally {
        try {
            if (stmt != null)
                stmt.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

	} 
	
	/**
	 * Update the status of specified objects (heterogeneous collection) to the specified status.
	 *
	 */
	public void updateStatusForHeterogeneousObjects(User user, java.sql.Connection connection, ArrayList registryObjectsIds
	, org.oasis.ebxml.registry.bindings.rim.types.StatusType status, RegistryErrorList el) throws RegistryException {
		
		if (registryObjectsIds.size() > 0) {

			HashMap idStatus = new HashMap();
			HashMap objectTypeIds = new HashMap();
			getIdStatusAndObjectTypeIdsMap(connection, registryObjectsIds, idStatus, objectTypeIds);

			Iterator idsIter = registryObjectsIds.iterator();
			// Storing the id of RE whose status kept unchanged
			ArrayList statusUnchanged = new ArrayList();
			while(idsIter.hasNext()) {
				String id = (String)idsIter.next();
				if (!idStatus.containsKey(id)) {
 					// Either object not exist or the object not a RE
					if (registryObjectExist(connection, id)) {
						throw new NonRegistryEntryFoundException(id);
					}
					else {
						ArrayList notExistIds = new ArrayList();
						notExistIds.add(id);
						// Should we make ObjectNotFoundException later???
						throw new ObjectsNotFoundException(notExistIds);
					}
				}

				String oldStatus = (String) idStatus.get(id);
				if (oldStatus != null) {
					if (StatusType.valueOf(oldStatus).getType() == status.getType()) { 
						// we are going to approve/deprecated a RE already approved/deprecated
						statusUnchanged.add(id);
						RegistryError re = new RegistryError();
						re.setSeverity(ErrorType.WARNING);
						re.setErrorCode("unknown");
						if (status.getType()==StatusType.APPROVED.getType()) {
							re.setContent("The RegistryEntry " + id + " has been already approved");
							re.setCodeContext("LifeCycleManagerImpl.approveObjects");
						}
						if (status.getType()==StatusType.DEPRECATED.getType()) {
							re.setContent("The RegistryEntry " + id + " has been already deprecated");
							re.setCodeContext("LifeCycleManagerImpl.deprecateObjects");
						}
						el.addRegistryError(re);
					}
				}
			} // end looping all the ids
			
			Iterator objectTypesIter = objectTypeIds.keySet().iterator();
			while(objectTypesIter.hasNext()) {
				String objectType = (String)objectTypesIter.next();
				if (objectType.equalsIgnoreCase("ClassificationScheme")) {
					ClassificationSchemeDAO classificationSchemeDAO = new ClassificationSchemeDAO();
					classificationSchemeDAO.updateStatus(user, connection, (ArrayList)(objectTypeIds.get(objectType)), status, statusUnchanged);
				}
				else if (objectType.equalsIgnoreCase("ExtrinsicObject")) {
					ExtrinsicObjectDAO extrinsicObjectDAO = new ExtrinsicObjectDAO();
					extrinsicObjectDAO.updateStatus(user, connection, (ArrayList)(objectTypeIds.get(objectType)), status, statusUnchanged);
				}
				else if (objectType.equalsIgnoreCase("RegistryPackage")) {
					RegistryPackageDAO registryPackageDAO = new RegistryPackageDAO();
					registryPackageDAO.updateStatus(user, connection, (ArrayList)(objectTypeIds.get(objectType)), status, statusUnchanged);
				}
				else {
					throw new RegistryException("Unexpected objectType " + objectType);
				}
			} // end looping all objectTypes
		}
	}       
	
	ArrayList getRegistryEntryList(java.sql.Connection conn, ResultSet rs, ResponseOption responseOption) throws RegistryException {
		ArrayList res = new ArrayList();
		
		try {
			while(rs.next()) {
				RegistryEntry re = new RegistryEntry();
				loadObjectFromResultSet(conn, re, rs, responseOption, new ArrayList());
				
				res.add(re);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new RegistryException(e);
		}
		
		return res;
	}
	
	public void loadObjectFromResultSet(java.sql.Connection conn, Object obj, ResultSet rs, ResponseOption responseOption, ArrayList objectRefs) throws RegistryException {
		try {
			if (!(obj instanceof org.oasis.ebxml.registry.bindings.rim.RegistryEntryType)) {
				throw new RegistryException("Unexpected object " + obj + ". Was expecting org.oasis.ebxml.registry.bindings.rim.RegistryEntryType.");
			}
					
			RegistryEntryType re = (RegistryEntryType)obj;

			super.loadObjectFromResultSet(conn, re, rs, responseOption, objectRefs);
			
			java.sql.Timestamp expiration = null;
                        
                        //Need to work around a bug in PostgreSQL and loading of ClassificationScheme data from NIST tests
                        try {
                            rs.getTimestamp("expiration");
                            re.setExpiration(expiration);
                        }
                        catch (StringIndexOutOfBoundsException e) {
                            e.printStackTrace();
                            String id = rs.getString("id");
                            System.err.println("RegistryEntry id = '" + id + "'");
                        }
			
			int majorVersion = rs.getInt("majorVersion");
			re.setMajorVersion(majorVersion);
			
			int minorVersion = rs.getInt("minorVersion");
			re.setMinorVersion(minorVersion);
			
			String stabilityStr = rs.getString("stability");
			if (stabilityStr != null) {
				re.setStability(StabilityType.valueOf(stabilityStr));
			}
			
			String statusStr = rs.getString("status");
			re.setStatus(StatusType.valueOf(statusStr));				
			
			String userVersion = rs.getString("userVersion");
			re.setUserVersion(userVersion);
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new RegistryException(e);
		}														
	}
}
