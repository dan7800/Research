/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.user;

import java.io.*;
import java.sql.*;
import java.rmi.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.*;
import com.vlee.local.*;
import com.vlee.util.*;
import java.math.*;

public class AuditTrailBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "user_audit_trail";
	protected final String strObjectName = "AuditTrailBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String STATE_NONE = "none";
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	public static final String TC_ACTION_CREATE = "create";
	public static final String TC_ACTION_UPDATE = "update";
	public static final String TC_ACTION_DELETE = "delete";
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String USERID = "userid";
	public static final String NAMESPACE = "namespace";
	public static final String AUDIT_TYPE = "audit_type";
	public static final String AUDIT_LEVEL = "audit_level";
	public static final String FOREIGN_TABLE1 = "foreign_table1";
	public static final String FOREIGN_KEY1 = "foreign_key1";
	public static final String FOREIGN_TABLE2 = "foreign_table2";
	public static final String FOREIGN_KEY2 = "foreign_key2";
	public static final String REMARKS = "remarks";
	public static final String TIME = "time";
	public static final String STATE = "state";
	public static final String STATUS = "status";
	public static final Integer TYPE_ACCESS = new Integer("1");
	public static final Integer TYPE_DEVELOPER = new Integer("200");
	public static final Integer TYPE_SYSADMIN = new Integer("300");
	public static final Integer TYPE_USER = new Integer("400");
	public static final Integer TYPE_TXN = new Integer("500");
	public static final Integer TYPE_ACC = new Integer("600");
	public static final Integer TYPE_USAGE = new Integer("700");
	public static final Integer TYPE_CONFIG = new Integer("800");
	public static final Integer TYPE_CHANGE_CREDIT_TERMS = new Integer("900");
	public static final String MODULENAME = "user";
	public static final String TC_ENTITY_ID = "tc_entity_id";
	public static final String TC_ENTITY_TABLE  = "tc_entity_table";
	public static final String TC_ACTION = "tc_action";
	// members ----------------------------------------------
	private AuditTrailObject mObject = new AuditTrailObject();

	public AuditTrailObject getObject()
	{
		return this.mObject;
	}

	public void setObject(AuditTrailObject mObject)
	{
		this.mObject = mObject;
	}

	public Long getPrimaryKey()
	{
		return this.mObject.pkid;
	}

	public void setPrimaryKey(Long pkid)
	{
		this.mObject.pkid = pkid;
	}

	public Long ejbCreate(AuditTrailObject mObject) throws CreateException
	{
		Long pkid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			if (mObject.remarks.length() > 200)
			{
				mObject.remarks = mObject.remarks.substring(0, 199);
			}
			pkid = insertObject(mObject);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (pkid != null)
		{
			this.mObject = mObject;
			this.mObject.pkid = new Long(pkid.longValue());
			// primary key!!!
			ejbStore();
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return pkid;
	}

	public Long ejbFindByPrimaryKey(Long pkid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(pkid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return pkid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + pkid.toString() + " not found.");
		}
	}

	public void ejbRemove()
	{
		try
		{
			deleteObject(this.mObject.pkid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
	}

	public void setEntityContext(EntityContext lContext)
	{
		Log.printVerbose(strObjectName + " in setEntityContext");
		this.mContext = lContext;
		Log.printVerbose(strObjectName + " Leaving setEntityContext");
	}

	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.mContext = null;
		Log.printVerbose(strObjectName + " Leaving unsetEntityContext");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.mObject.pkid = (Long) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.mObject.pkid = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadObject();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug(strObjectName + "ejbLoad: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbLoad");
	}

	public void ejbStore()
	{
		Log.printVerbose(strObjectName + " In ejbStore");
		try
		{
			storeObject();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(AuditTrailObject mObject)
	{
	}

	public Vector ejbHomeGetValueObjectsGiven(Integer iUserId, String namespace, Integer iAuditType,
			Integer iAuditLevel, String keyword, Timestamp dateFrom, Timestamp dateTo, String state, String status)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(iUserId, namespace, iAuditType, iAuditLevel, keyword, dateFrom, dateTo,
					state, status);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	//20080109 Jimmy - add branch and filter
	public Vector ejbHomeGetAuditTrailReport(Integer iUserId, String namespace, Integer iAuditType, Integer iAuditLevel,
			String keyword, Timestamp dateFrom, Timestamp dateTo, String state, String status, 
			String checkUserBranch, Integer iBranch)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectAuditTrailReport(iUserId, namespace, iAuditType, iAuditLevel, keyword, dateFrom, dateTo, state, status, checkUserBranch, iBranch);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}
	
	/** ***************** Database Routines ************************ */
	private void makeConnection()
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			con = ds.getConnection();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void releaseConnection()
	{
		try
		{
			con.close();
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw new EJBException("releaseConnection: " + ex.getMessage());
		}
	} // releaseConnection

	// ////////////////////////////////////////////////////////
	/*
	 * private Collection getValObjectsGiven(String strCat, String strNamespace,
	 * String strValue1, String strValue2, String strOptions) throws
	 * SQLException {
	 *  // warning.. this function is not fully tested yet makeConnection();
	 * Log.printVerbose("in getValObjectsGiven()");
	 * 
	 * Collection colObj = new Vector(); String selectStatement = "select " + "
	 * pkid , " + " category , " + " namespace , " + " value1 , " + " value2 , " + "
	 * value3 , " + " value4 , " + " value5 , " + " binary_data , " + " status , " + "
	 * userid_edit , " + " time_edit , " + " time_effective " + " from " +
	 * TABLENAME + " where category = ? " + " and namespace = ? " + " and value1 = ? " + "
	 * and value2 = ? " + " order by pkid ";
	 * 
	 * PreparedStatement prepStmt = con.prepareStatement(selectStatement);
	 * prepStmt.setString(1,strCat); prepStmt.setString(2,strNamespace);
	 * prepStmt.setString(3,strValue1); prepStmt.setString(4,strValue2);
	 * 
	 * ResultSet rs = prepStmt.executeQuery(); //rs.beforeTheFirstRecord(); while
	 * (rs.next()) { AuditTrailObject laro = new AuditTrailObject(); laro.mPkid =
	 * new Integer(rs.getInt("pkid")); laro.mCategory =
	 * rs.getString("category"); laro.mNamespace = rs.getString("namespace");
	 * laro.mValue1 = rs.getString("value1"); laro.mValue2 =
	 * rs.getString("value2"); laro.mValue3 = rs.getString("value3");
	 * laro.mValue4 = rs.getString("value4"); laro.mValue5 =
	 * rs.getString("value5"); laro.mBinary = rs.getBytes("binary_data");
	 * laro.mStatus = rs.getString("status"); laro.mUserIdEdit = new
	 * Integer(rs.getInt("userid_edit")); laro.mTimeEdit =
	 * rs.getTimestamp("time_edit"); laro.mTimeEffective =
	 * rs.getTimestamp("time_effective");
	 * 
	 * Log.printVerbose(laro.toString()); colObj.add(laro);
	 *  } prepStmt.close(); releaseConnection();
	 * 
	 * return colObj;
	 *  }
	 * 
	 */
	// ////////////////////////////////////////////////////////
	private Long insertObject(AuditTrailObject mObject) throws SQLException
	{
		// Long newPkid = getNextPkid();
		Long newPkid = null;
		makeConnection();
		try
		{
			newPkid = getNextPKId(con);
		} catch (Exception ex)
		{
			throw new EJBException(strObjectName + ex.getMessage());
		}
		Log.printVerbose(strObjectName + "insertNewRow: ");
		Log.printVerbose("The new objectid is :" + newPkid.toString());
		String insertStatement = "INSERT INTO " + TABLENAME + " ( " + PKID + ", " + USERID + ", " + NAMESPACE + ", "
				+ AUDIT_TYPE + ", " + AUDIT_LEVEL + ", " + FOREIGN_TABLE1 + ", " + FOREIGN_KEY1 + ", " + FOREIGN_TABLE2
				+ ", " + FOREIGN_KEY2 + ", " + REMARKS + ", " + TIME + ", " + STATE + ", " + STATUS + ", " + TC_ENTITY_ID + ", " + TC_ENTITY_TABLE + ", " + TC_ACTION + ") VALUES ("
				+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,? ) ";
		PreparedStatement prepStmt = con.prepareStatement(insertStatement);
		prepStmt.setLong(1, newPkid.longValue());
		prepStmt.setInt(2, mObject.userId.intValue());
		prepStmt.setString(3, mObject.namespace);
		prepStmt.setInt(4, mObject.auditType.intValue());
		prepStmt.setInt(5, mObject.auditLevel.intValue());
		prepStmt.setString(6, mObject.foreignTable1);
		prepStmt.setLong(7, mObject.foreignKey1.longValue());
		prepStmt.setString(8, mObject.foreignTable2);
		prepStmt.setInt(9, mObject.foreignKey2.intValue());
		prepStmt.setString(10, mObject.remarks);
		prepStmt.setTimestamp(11, mObject.time);
		prepStmt.setString(12, mObject.state);
		prepStmt.setString(13, mObject.status);
		prepStmt.setInt(14, mObject.tc_entity_id.intValue());
		prepStmt.setString(15, mObject.tc_entity_table);
		prepStmt.setString(16, mObject.tc_action);
		
		prepStmt.executeUpdate();
		// ResultSet rs = prepStmt.executeQuery();
		prepStmt.close();
		Log.printAudit(strObjectName + "Created New Row:" + newPkid.toString());
		Log.printVerbose(strObjectName + "leaving insertNewRow");
		releaseConnection();
		return newPkid;
	}

	// stop here
	private boolean selectByPrimaryKey(Long pkid) throws SQLException
	{
		makeConnection();
		String selectStatement = "select * " + "from " + TABLENAME + " where pkid = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setLong(1, pkid.longValue());
		ResultSet rs = prepStmt.executeQuery();
		boolean result = false;
		result = rs.next();
		prepStmt.close();
		releaseConnection();
		return result;
	}

	private void deleteObject(Long pkid) throws SQLException
	{
		makeConnection();
		String deleteStatement = "delete from " + TABLENAME + "  " + "where pkid = ?";
		PreparedStatement prepStmt = con.prepareStatement(deleteStatement);
		prepStmt.setLong(1, pkid.longValue());
		prepStmt.executeUpdate();
		prepStmt.close();
		Log.printAudit(strObjectName + "Deleted Object : " + pkid.toString());
		releaseConnection();
	}

	private Vector selectValueObjectsGiven(Integer iUserId, String namespace, Integer iAuditType, Integer iAuditLevel,
			String keyword, Timestamp dateFrom, Timestamp dateTo, String state, String status) throws SQLException
	{
		Vector vecValObj = new Vector();
		makeConnection();
		String selectStmt = "SELECT " + PKID + ", " + USERID + ", " + NAMESPACE + ", " + AUDIT_TYPE + ", "
				+ AUDIT_LEVEL + ", " + FOREIGN_TABLE1 + ", " + FOREIGN_KEY1 + ", " + FOREIGN_TABLE2 + ", "
				+ FOREIGN_KEY2 + ", " + REMARKS + ", " + TIME + ", " + STATE + ", " + STATUS + "  FROM " + TABLENAME
				+ " WHERE " + PKID + " != '-1' ";
		if (iUserId != null)
		{
			selectStmt += " AND " + USERID + "='" + iUserId.toString() + "' ";
		}
		if (namespace != null)
		{
			selectStmt += " AND " + NAMESPACE + "='" + namespace + "' ";
		}
		if (iAuditType != null)
		{
			selectStmt += " AND " + AUDIT_TYPE + "='" + iAuditType.toString() + "' ";
		}
		if (iAuditLevel != null)
		{
			selectStmt += " AND " + AUDIT_LEVEL + "='" + iAuditLevel.toString() + "' ";
		}
		if (keyword != null)
		{
			selectStmt += " AND " + REMARKS + "~*'" + keyword + "' ";
		}
		if (dateFrom != null)
		{
			selectStmt += " AND " + TIME + ">= '" + dateFrom + "' ";
		}
		if (dateTo != null)
		{
			selectStmt += " AND " + TIME + "< '" + dateTo + "' ";
		}
		if (state != null)
		{
			selectStmt += " AND " + STATE + " ='" + state + "' ";
		}
		if (status != null)
		{
			selectStmt += " AND " + STATUS + " ='" + status + "' ";
		}
		selectStmt += " ORDER BY " + TIME;
		PreparedStatement prepStmt = con.prepareStatement(selectStmt);
		ResultSet rs = prepStmt.executeQuery();
		while (rs.next())
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.pkid = new Long(rs.getLong(PKID));
			atObj.userId = new Integer(rs.getInt(USERID));
			atObj.namespace = rs.getString(NAMESPACE);
			atObj.auditType = new Integer(rs.getInt(AUDIT_TYPE));
			atObj.auditLevel = new Integer(rs.getInt(AUDIT_LEVEL));
			atObj.foreignTable1 = rs.getString(FOREIGN_TABLE1);
			atObj.foreignKey1 = new Long(rs.getLong(FOREIGN_KEY1));
			atObj.foreignTable2 = rs.getString(FOREIGN_TABLE2);
			atObj.foreignKey2 = new Integer(rs.getInt(FOREIGN_KEY2));
			atObj.remarks = rs.getString(REMARKS);
			atObj.time = rs.getTimestamp(TIME);
			atObj.state = rs.getString(STATE);
			atObj.status = rs.getString(STATUS);
			vecValObj.add(atObj);
		}
		prepStmt.close();
		releaseConnection();
		return vecValObj;
	}
	
	// 20080108 Jimmy - Add branch and filter
	private Vector selectAuditTrailReport(Integer iUserId, String namespace, Integer iAuditType, Integer iAuditLevel,
			String keyword, Timestamp dateFrom, Timestamp dateTo, String state, String status, 
			String checkUserBranch, Integer iBranch) throws SQLException
	{
		Vector vecValObj = new Vector();
		makeConnection();
		String selectStmt = "SELECT " + PKID + ", " + USERID + ", " + NAMESPACE + ", " + AUDIT_TYPE + ", "
				+ AUDIT_LEVEL + ", " + FOREIGN_TABLE1 + ", " + FOREIGN_KEY1 + ", " + FOREIGN_TABLE2 + ", "
				+ FOREIGN_KEY2 + ", " + REMARKS + ", " + TIME + ", " + STATE + ", " + STATUS + "  FROM " + TABLENAME
				+ " WHERE " + PKID + " != '-1' ";
		
		if (checkUserBranch.equals("User"))
		{
			if (iUserId != null)
			{
				selectStmt += " AND " + USERID + "='" + iUserId.toString() + "' ";
			}
		}
		else
		{
			if (iBranch != null)
			{
				selectStmt += " AND " + USERID + " IN (SELECT userid FROM user_config_registry where namespace = 'CUSTSVCCTR' AND value1 ='" + iBranch.toString() + "') ";

			}
		}
		if (namespace != null)
		{
			selectStmt += " AND " + NAMESPACE + "='" + namespace + "' ";
		}
		if (iAuditType != null)
		{
			selectStmt += " AND " + AUDIT_TYPE + "='" + iAuditType.toString() + "' ";
		}
		if (iAuditLevel != null)
		{
			selectStmt += " AND " + AUDIT_LEVEL + "='" + iAuditLevel.toString() + "' ";
		}
		if (keyword != null)
		{
			selectStmt += " AND " + REMARKS + "~*'" + keyword + "' ";
		}
		if (dateFrom != null)
		{
			selectStmt += " AND " + TIME + ">= '" + dateFrom + "' ";
		}
		if (dateTo != null)
		{
			selectStmt += " AND " + TIME + "< '" + dateTo + "' ";
		}
		if (state != null)
		{
			selectStmt += " AND " + STATE + " ='" + state + "' ";
		}
		if (status != null)
		{
			selectStmt += " AND " + STATUS + " ='" + status + "' ";
		}
		selectStmt += " ORDER BY " + TIME;
		PreparedStatement prepStmt = con.prepareStatement(selectStmt);
		ResultSet rs = prepStmt.executeQuery();
		while (rs.next())
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.pkid = new Long(rs.getLong(PKID));
			atObj.userId = new Integer(rs.getInt(USERID));
			atObj.namespace = rs.getString(NAMESPACE);
			atObj.auditType = new Integer(rs.getInt(AUDIT_TYPE));
			atObj.auditLevel = new Integer(rs.getInt(AUDIT_LEVEL));
			atObj.foreignTable1 = rs.getString(FOREIGN_TABLE1);
			atObj.foreignKey1 = new Long(rs.getLong(FOREIGN_KEY1));
			atObj.foreignTable2 = rs.getString(FOREIGN_TABLE2);
			atObj.foreignKey2 = new Integer(rs.getInt(FOREIGN_KEY2));
			atObj.remarks = rs.getString(REMARKS);
			atObj.time = rs.getTimestamp(TIME);
			atObj.state = rs.getString(STATE);
			atObj.status = rs.getString(STATUS);
			vecValObj.add(atObj);
		}
		prepStmt.close();
		releaseConnection();
		return vecValObj;
	}
	
	private void loadObject() throws SQLException
	{
		makeConnection();
		String selectStatement = "SELECT " + PKID + ", " + USERID + ", " + NAMESPACE + ", " + AUDIT_TYPE + ", "
				+ AUDIT_LEVEL + ", " + FOREIGN_TABLE1 + ", " + FOREIGN_KEY1 + ", " + FOREIGN_TABLE2 + ", "
				+ FOREIGN_KEY2 + ", " + REMARKS + ", " + TIME + ", " + STATE + ", " + STATUS + ",*  FROM " + TABLENAME
				+ " WHERE " + PKID + " = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setLong(1, this.mObject.pkid.longValue());
		ResultSet rs = prepStmt.executeQuery();
		if (rs.next())
		{
			this.mObject.pkid = new Long(rs.getLong(PKID));
			this.mObject.userId = new Integer(rs.getInt(USERID));
			this.mObject.namespace = rs.getString(NAMESPACE);
			this.mObject.auditType = new Integer(rs.getInt(AUDIT_TYPE));
			this.mObject.auditLevel = new Integer(rs.getInt(AUDIT_LEVEL));
			this.mObject.foreignTable1 = rs.getString(FOREIGN_TABLE1);
			this.mObject.foreignKey1 = new Long(rs.getLong(FOREIGN_KEY1));
			this.mObject.foreignTable2 = rs.getString(FOREIGN_TABLE2);
			this.mObject.foreignKey2 = new Integer(rs.getInt(FOREIGN_KEY2));
			this.mObject.remarks = rs.getString(REMARKS);
			this.mObject.time = rs.getTimestamp(TIME);
			this.mObject.state = rs.getString(STATE);
			this.mObject.status = rs.getString(STATUS);
			this.mObject.tc_action = rs.getString(TC_ACTION);
			this.mObject.tc_entity_id = new Integer(rs.getInt(TC_ENTITY_ID));
			this.mObject.tc_entity_table = rs.getString(TC_ENTITY_TABLE);
			prepStmt.close();
		} else
		{
			prepStmt.close();
			throw new NoSuchEntityException("Row for this EJB Object" + " is not found in database.");
		}
		releaseConnection();
	}

	private void storeObject() throws SQLException
	{
		makeConnection();
		String updateStatement = "UPDATE " + TABLENAME + " SET " + PKID + " = ? , " + USERID + " = ? , " + NAMESPACE
				+ " = ? , " + AUDIT_TYPE + " = ? , " + AUDIT_LEVEL + " = ? , " + FOREIGN_TABLE1 + " = ? , "
				+ FOREIGN_KEY1 + " = ? , " + FOREIGN_TABLE2 + " = ? , " + FOREIGN_KEY2 + " = ? , " + REMARKS
				+ " = ? , " + TIME + " = ? , " + STATE + " = ? , " + STATUS + " = ?  , " + TC_ENTITY_ID + " = ?  , " + TC_ENTITY_TABLE + " = ?  , " + TC_ACTION + " = ?  " + " WHERE " + PKID + " = ?;";
		PreparedStatement prepStmt = con.prepareStatement(updateStatement);
		prepStmt.setLong(1, this.mObject.pkid.longValue());
		prepStmt.setInt(2, this.mObject.userId.intValue());
		prepStmt.setString(3, this.mObject.namespace);
		prepStmt.setInt(4, this.mObject.auditType.intValue());
		prepStmt.setInt(5, this.mObject.auditLevel.intValue());
		prepStmt.setString(6, this.mObject.foreignTable1);
		prepStmt.setLong(7, this.mObject.foreignKey1.longValue());
		prepStmt.setString(8, this.mObject.foreignTable2);
		prepStmt.setInt(9, this.mObject.foreignKey2.intValue());
		prepStmt.setString(10, this.mObject.remarks);
		prepStmt.setTimestamp(11, this.mObject.time);
		prepStmt.setString(12, this.mObject.state);
		prepStmt.setString(13, this.mObject.status);
		prepStmt.setInt(14, this.mObject.tc_entity_id.intValue());
		prepStmt.setString(15, this.mObject.tc_entity_table);
		prepStmt.setString(16, this.mObject.tc_action);
		prepStmt.setLong(17, this.mObject.pkid.longValue());
		int rowCount = prepStmt.executeUpdate();
		prepStmt.close();
		if (rowCount == 0)
		{
			throw new EJBException("Storing ejb object " + this.mObject.pkid.toString() + " failed.");
		}
		releaseConnection();
	}

	private static synchronized Long getNextPKId(Connection con) throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, PKID, TABLENAME, MODULENAME, (Long) null);
	}
	/*
	 * private Long getNextPkid() { Long pkid = new Long("1001"); try {
	 * makeConnection(); String selectStatement = "select max(pkid) as maxid " +
	 * "from "+ TABLENAME ; PreparedStatement prepStmt =
	 * con.prepareStatement(selectStatement); ResultSet rs =
	 * prepStmt.executeQuery(); //rs.beforeTheFirstRecord(); long buffer=1000; if(rs!=null &&
	 * rs.next()) { buffer = rs.getLong("maxid"); if(buffer>0) { pkid = new
	 * Long(buffer+1);} else { buffer=1001; } } if(prepStmt!=null){
	 * prepStmt.close();} } // end try catch(Exception ex) {
	 * ex.printStackTrace(); pkid = new Long("1001"); } releaseConnection();
	 * return pkid; }
	 */
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.user;

import java.io.*;
import java.sql.*;
import java.rmi.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.*;
import com.vlee.local.*;
import com.vlee.util.*;
import java.math.*;

public class AuditTrailBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "user_audit_trail";
	protected final String strObjectName = "AuditTrailBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String STATE_NONE = "none";
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String USERID = "userid";
	public static final String NAMESPACE = "namespace";
	public static final String AUDIT_TYPE = "audit_type";
	public static final String AUDIT_LEVEL = "audit_level";
	public static final String FOREIGN_TABLE1 = "foreign_table1";
	public static final String FOREIGN_KEY1 = "foreign_key1";
	public static final String FOREIGN_TABLE2 = "foreign_table2";
	public static final String FOREIGN_KEY2 = "foreign_key2";
	public static final String REMARKS = "remarks";
	public static final String TIME = "time";
	public static final String STATE = "state";
	public static final String STATUS = "status";
	public static final Integer TYPE_ACCESS = new Integer("1");
	public static final Integer TYPE_DEVELOPER = new Integer("200");
	public static final Integer TYPE_SYSADMIN = new Integer("300");
	public static final Integer TYPE_USER = new Integer("400");
	public static final Integer TYPE_TXN = new Integer("500");
	public static final Integer TYPE_ACC = new Integer("600");
	public static final Integer TYPE_USAGE = new Integer("700");
	public static final Integer TYPE_CONFIG = new Integer("800");
	public static final String MODULENAME = "user";
	// members ----------------------------------------------
	private AuditTrailObject mObject = new AuditTrailObject();

	public AuditTrailObject getObject()
	{
		return this.mObject;
	}

	public void setObject(AuditTrailObject mObject)
	{
		this.mObject = mObject;
	}

	public Long getPrimaryKey()
	{
		return this.mObject.pkid;
	}

	public void setPrimaryKey(Long pkid)
	{
		this.mObject.pkid = pkid;
	}

	public Long ejbCreate(AuditTrailObject mObject) throws CreateException
	{
		Long pkid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			if (mObject.remarks.length() > 200)
			{
				mObject.remarks = mObject.remarks.substring(0, 199);
			}
			pkid = insertObject(mObject);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (pkid != null)
		{
			this.mObject = mObject;
			this.mObject.pkid = new Long(pkid.longValue());
			// primary key!!!
			ejbStore();
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return pkid;
	}

	public Long ejbFindByPrimaryKey(Long pkid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(pkid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return pkid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + pkid.toString() + " not found.");
		}
	}

	public void ejbRemove()
	{
		try
		{
			deleteObject(this.mObject.pkid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
	}

	public void setEntityContext(EntityContext lContext)
	{
		Log.printVerbose(strObjectName + " in setEntityContext");
		this.mContext = lContext;
		Log.printVerbose(strObjectName + " Leaving setEntityContext");
	}

	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.mContext = null;
		Log.printVerbose(strObjectName + " Leaving unsetEntityContext");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.mObject.pkid = (Long) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.mObject.pkid = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadObject();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug(strObjectName + "ejbLoad: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbLoad");
	}

	public void ejbStore()
	{
		Log.printVerbose(strObjectName + " In ejbStore");
		try
		{
			storeObject();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(AuditTrailObject mObject)
	{
	}

	public Vector ejbHomeGetValueObjectsGiven(Integer iUserId, String namespace, Integer iAuditType,
			Integer iAuditLevel, String keyword, Timestamp dateFrom, Timestamp dateTo, String state, String status)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(iUserId, namespace, iAuditType, iAuditLevel, keyword, dateFrom, dateTo,
					state, status);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	/** ***************** Database Routines ************************ */
	private void makeConnection()
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			con = ds.getConnection();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void releaseConnection()
	{
		try
		{
			con.close();
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw new EJBException("releaseConnection: " + ex.getMessage());
		}
	} // releaseConnection

	// ////////////////////////////////////////////////////////
	/*
	 * private Collection getValObjectsGiven(String strCat, String strNamespace,
	 * String strValue1, String strValue2, String strOptions) throws
	 * SQLException {
	 *  // warning.. this function is not fully tested yet makeConnection();
	 * Log.printVerbose("in getValObjectsGiven()");
	 * 
	 * Collection colObj = new Vector(); String selectStatement = "select " + "
	 * pkid , " + " category , " + " namespace , " + " value1 , " + " value2 , " + "
	 * value3 , " + " value4 , " + " value5 , " + " binary_data , " + " status , " + "
	 * userid_edit , " + " time_edit , " + " time_effective " + " from " +
	 * TABLENAME + " where category = ? " + " and namespace = ? " + " and value1 = ? " + "
	 * and value2 = ? " + " order by pkid ";
	 * 
	 * PreparedStatement prepStmt = con.prepareStatement(selectStatement);
	 * prepStmt.setString(1,strCat); prepStmt.setString(2,strNamespace);
	 * prepStmt.setString(3,strValue1); prepStmt.setString(4,strValue2);
	 * 
	 * ResultSet rs = prepStmt.executeQuery(); //rs.beforeTheFirstRecord(); while
	 * (rs.next()) { AuditTrailObject laro = new AuditTrailObject(); laro.mPkid =
	 * new Integer(rs.getInt("pkid")); laro.mCategory =
	 * rs.getString("category"); laro.mNamespace = rs.getString("namespace");
	 * laro.mValue1 = rs.getString("value1"); laro.mValue2 =
	 * rs.getString("value2"); laro.mValue3 = rs.getString("value3");
	 * laro.mValue4 = rs.getString("value4"); laro.mValue5 =
	 * rs.getString("value5"); laro.mBinary = rs.getBytes("binary_data");
	 * laro.mStatus = rs.getString("status"); laro.mUserIdEdit = new
	 * Integer(rs.getInt("userid_edit")); laro.mTimeEdit =
	 * rs.getTimestamp("time_edit"); laro.mTimeEffective =
	 * rs.getTimestamp("time_effective");
	 * 
	 * Log.printVerbose(laro.toString()); colObj.add(laro);
	 *  } prepStmt.close(); releaseConnection();
	 * 
	 * return colObj;
	 *  }
	 * 
	 */
	// ////////////////////////////////////////////////////////
	private Long insertObject(AuditTrailObject mObject) throws SQLException
	{
		// Long newPkid = getNextPkid();
		Long newPkid = null;
		makeConnection();
		try
		{
			newPkid = getNextPKId(con);
		} catch (Exception ex)
		{
			throw new EJBException(strObjectName + ex.getMessage());
		}
		Log.printVerbose(strObjectName + "insertNewRow: ");
		Log.printVerbose("The new objectid is :" + newPkid.toString());
		String insertStatement = "INSERT INTO " + TABLENAME + " ( " + PKID + ", " + USERID + ", " + NAMESPACE + ", "
				+ AUDIT_TYPE + ", " + AUDIT_LEVEL + ", " + FOREIGN_TABLE1 + ", " + FOREIGN_KEY1 + ", " + FOREIGN_TABLE2
				+ ", " + FOREIGN_KEY2 + ", " + REMARKS + ", " + TIME + ", " + STATE + ", " + STATUS + ") VALUES ("
				+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ";
		PreparedStatement prepStmt = con.prepareStatement(insertStatement);
		prepStmt.setLong(1, newPkid.longValue());
		prepStmt.setInt(2, mObject.userId.intValue());
		prepStmt.setString(3, mObject.namespace);
		prepStmt.setInt(4, mObject.auditType.intValue());
		prepStmt.setInt(5, mObject.auditLevel.intValue());
		prepStmt.setString(6, mObject.foreignTable1);
		prepStmt.setLong(7, mObject.foreignKey1.longValue());
		prepStmt.setString(8, mObject.foreignTable2);
		prepStmt.setInt(9, mObject.foreignKey2.intValue());
		prepStmt.setString(10, mObject.remarks);
		prepStmt.setTimestamp(11, mObject.time);
		prepStmt.setString(12, mObject.state);
		prepStmt.setString(13, mObject.status);
		prepStmt.executeUpdate();
		// ResultSet rs = prepStmt.executeQuery();
		prepStmt.close();
		Log.printAudit(strObjectName + "Created New Row:" + newPkid.toString());
		Log.printVerbose(strObjectName + "leaving insertNewRow");
		releaseConnection();
		return newPkid;
	}

	// stop here
	private boolean selectByPrimaryKey(Long pkid) throws SQLException
	{
		makeConnection();
		String selectStatement = "select * " + "from " + TABLENAME + " where pkid = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setLong(1, pkid.longValue());
		ResultSet rs = prepStmt.executeQuery();
		boolean result = false;
		result = rs.next();
		prepStmt.close();
		releaseConnection();
		return result;
	}

	private void deleteObject(Long pkid) throws SQLException
	{
		makeConnection();
		String deleteStatement = "delete from " + TABLENAME + "  " + "where pkid = ?";
		PreparedStatement prepStmt = con.prepareStatement(deleteStatement);
		prepStmt.setLong(1, pkid.longValue());
		prepStmt.executeUpdate();
		prepStmt.close();
		Log.printAudit(strObjectName + "Deleted Object : " + pkid.toString());
		releaseConnection();
	}

	private Vector selectValueObjectsGiven(Integer iUserId, String namespace, Integer iAuditType, Integer iAuditLevel,
			String keyword, Timestamp dateFrom, Timestamp dateTo, String state, String status) throws SQLException
	{
		Vector vecValObj = new Vector();
		makeConnection();
		String selectStmt = "SELECT " + PKID + ", " + USERID + ", " + NAMESPACE + ", " + AUDIT_TYPE + ", "
				+ AUDIT_LEVEL + ", " + FOREIGN_TABLE1 + ", " + FOREIGN_KEY1 + ", " + FOREIGN_TABLE2 + ", "
				+ FOREIGN_KEY2 + ", " + REMARKS + ", " + TIME + ", " + STATE + ", " + STATUS + "  FROM " + TABLENAME
				+ " WHERE " + PKID + " != '-1' ";
		if (iUserId != null)
		{
			selectStmt += " AND " + USERID + "='" + iUserId.toString() + "' ";
		}
		if (namespace != null)
		{
			selectStmt += " AND " + NAMESPACE + "='" + namespace + "' ";
		}
		if (iAuditType != null)
		{
			selectStmt += " AND " + AUDIT_TYPE + "='" + iAuditType.toString() + "' ";
		}
		if (iAuditLevel != null)
		{
			selectStmt += " AND " + AUDIT_LEVEL + "='" + iAuditLevel.toString() + "' ";
		}
		if (keyword != null)
		{
			selectStmt += " AND " + REMARKS + "~*'" + keyword + "' ";
		}
		if (dateFrom != null)
		{
			selectStmt += " AND " + TIME + ">= '" + dateFrom + "' ";
		}
		if (dateTo != null)
		{
			selectStmt += " AND " + TIME + "< '" + dateTo + "' ";
		}
		if (state != null)
		{
			selectStmt += " AND " + STATE + " ='" + state + "' ";
		}
		if (status != null)
		{
			selectStmt += " AND " + STATUS + " ='" + status + "' ";
		}
		selectStmt += " ORDER BY " + TIME;
		PreparedStatement prepStmt = con.prepareStatement(selectStmt);
		ResultSet rs = prepStmt.executeQuery();
		while (rs.next())
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.pkid = new Long(rs.getLong(PKID));
			atObj.userId = new Integer(rs.getInt(USERID));
			atObj.namespace = rs.getString(NAMESPACE);
			atObj.auditType = new Integer(rs.getInt(AUDIT_TYPE));
			atObj.auditLevel = new Integer(rs.getInt(AUDIT_LEVEL));
			atObj.foreignTable1 = rs.getString(FOREIGN_TABLE1);
			atObj.foreignKey1 = new Long(rs.getLong(FOREIGN_KEY1));
			atObj.foreignTable2 = rs.getString(FOREIGN_TABLE2);
			atObj.foreignKey2 = new Integer(rs.getInt(FOREIGN_KEY2));
			atObj.remarks = rs.getString(REMARKS);
			atObj.time = rs.getTimestamp(TIME);
			atObj.state = rs.getString(STATE);
			atObj.status = rs.getString(STATUS);
			vecValObj.add(atObj);
		}
		prepStmt.close();
		releaseConnection();
		return vecValObj;
	}

	private void loadObject() throws SQLException
	{
		makeConnection();
		String selectStatement = "SELECT " + PKID + ", " + USERID + ", " + NAMESPACE + ", " + AUDIT_TYPE + ", "
				+ AUDIT_LEVEL + ", " + FOREIGN_TABLE1 + ", " + FOREIGN_KEY1 + ", " + FOREIGN_TABLE2 + ", "
				+ FOREIGN_KEY2 + ", " + REMARKS + ", " + TIME + ", " + STATE + ", " + STATUS + "  FROM " + TABLENAME
				+ " WHERE " + PKID + " = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setLong(1, this.mObject.pkid.longValue());
		ResultSet rs = prepStmt.executeQuery();
		if (rs.next())
		{
			this.mObject.pkid = new Long(rs.getLong(PKID));
			this.mObject.userId = new Integer(rs.getInt(USERID));
			this.mObject.namespace = rs.getString(NAMESPACE);
			this.mObject.auditType = new Integer(rs.getInt(AUDIT_TYPE));
			this.mObject.auditLevel = new Integer(rs.getInt(AUDIT_LEVEL));
			this.mObject.foreignTable1 = rs.getString(FOREIGN_TABLE1);
			this.mObject.foreignKey1 = new Long(rs.getLong(FOREIGN_KEY1));
			this.mObject.foreignTable2 = rs.getString(FOREIGN_TABLE2);
			this.mObject.foreignKey2 = new Integer(rs.getInt(FOREIGN_KEY2));
			this.mObject.remarks = rs.getString(REMARKS);
			this.mObject.time = rs.getTimestamp(TIME);
			this.mObject.state = rs.getString(STATE);
			this.mObject.status = rs.getString(STATUS);
			prepStmt.close();
		} else
		{
			prepStmt.close();
			throw new NoSuchEntityException("Row for this EJB Object" + " is not found in database.");
		}
		releaseConnection();
	}

	private void storeObject() throws SQLException
	{
		makeConnection();
		String updateStatement = "UPDATE " + TABLENAME + " SET " + PKID + " = ? , " + USERID + " = ? , " + NAMESPACE
				+ " = ? , " + AUDIT_TYPE + " = ? , " + AUDIT_LEVEL + " = ? , " + FOREIGN_TABLE1 + " = ? , "
				+ FOREIGN_KEY1 + " = ? , " + FOREIGN_TABLE2 + " = ? , " + FOREIGN_KEY2 + " = ? , " + REMARKS
				+ " = ? , " + TIME + " = ? , " + STATE + " = ? , " + STATUS + " = ?  " + " WHERE " + PKID + " = ?;";
		PreparedStatement prepStmt = con.prepareStatement(updateStatement);
		prepStmt.setLong(1, this.mObject.pkid.longValue());
		prepStmt.setInt(2, this.mObject.userId.intValue());
		prepStmt.setString(3, this.mObject.namespace);
		prepStmt.setInt(4, this.mObject.auditType.intValue());
		prepStmt.setInt(5, this.mObject.auditLevel.intValue());
		prepStmt.setString(6, this.mObject.foreignTable1);
		prepStmt.setLong(7, this.mObject.foreignKey1.longValue());
		prepStmt.setString(8, this.mObject.foreignTable2);
		prepStmt.setInt(9, this.mObject.foreignKey2.intValue());
		prepStmt.setString(10, this.mObject.remarks);
		prepStmt.setTimestamp(11, this.mObject.time);
		prepStmt.setString(12, this.mObject.state);
		prepStmt.setString(13, this.mObject.status);
		prepStmt.setLong(14, this.mObject.pkid.longValue());
		int rowCount = prepStmt.executeUpdate();
		prepStmt.close();
		if (rowCount == 0)
		{
			throw new EJBException("Storing ejb object " + this.mObject.pkid.toString() + " failed.");
		}
		releaseConnection();
	}

	private static synchronized Long getNextPKId(Connection con) throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, PKID, TABLENAME, MODULENAME, (Long) null);
	}
	/*
	 * private Long getNextPkid() { Long pkid = new Long("1001"); try {
	 * makeConnection(); String selectStatement = "select max(pkid) as maxid " +
	 * "from "+ TABLENAME ; PreparedStatement prepStmt =
	 * con.prepareStatement(selectStatement); ResultSet rs =
	 * prepStmt.executeQuery(); //rs.beforeTheFirstRecord(); long buffer=1000; if(rs!=null &&
	 * rs.next()) { buffer = rs.getLong("maxid"); if(buffer>0) { pkid = new
	 * Long(buffer+1);} else { buffer=1001; } } if(prepStmt!=null){
	 * prepStmt.close();} } // end try catch(Exception ex) {
	 * ex.printStackTrace(); pkid = new Long("1001"); } releaseConnection();
	 * return pkid; }
	 */
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.user;

import java.io.*;
import java.sql.*;
import java.rmi.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.*;
import com.vlee.local.*;
import com.vlee.util.*;
import java.math.*;

public class AuditTrailBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "user_audit_trail";
	protected final String strObjectName = "AuditTrailBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String STATE_NONE = "none";
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String USERID = "userid";
	public static final String NAMESPACE = "namespace";
	public static final String AUDIT_TYPE = "audit_type";
	public static final String AUDIT_LEVEL = "audit_level";
	public static final String FOREIGN_TABLE1 = "foreign_table1";
	public static final String FOREIGN_KEY1 = "foreign_key1";
	public static final String FOREIGN_TABLE2 = "foreign_table2";
	public static final String FOREIGN_KEY2 = "foreign_key2";
	public static final String REMARKS = "remarks";
	public static final String TIME = "time";
	public static final String STATE = "state";
	public static final String STATUS = "status";
	public static final Integer TYPE_ACCESS = new Integer("1");
	public static final Integer TYPE_DEVELOPER = new Integer("200");
	public static final Integer TYPE_SYSADMIN = new Integer("300");
	public static final Integer TYPE_USER = new Integer("400");
	public static final Integer TYPE_TXN = new Integer("500");
	public static final Integer TYPE_ACC = new Integer("600");
	public static final Integer TYPE_USAGE = new Integer("700");
	public static final Integer TYPE_CONFIG = new Integer("800");
	public static final String MODULENAME = "user";
	// members ----------------------------------------------
	private AuditTrailObject mObject = new AuditTrailObject();

	public AuditTrailObject getObject()
	{
		return this.mObject;
	}

	public void setObject(AuditTrailObject mObject)
	{
		this.mObject = mObject;
	}

	public Long getPrimaryKey()
	{
		return this.mObject.pkid;
	}

	public void setPrimaryKey(Long pkid)
	{
		this.mObject.pkid = pkid;
	}

	public Long ejbCreate(AuditTrailObject mObject) throws CreateException
	{
		Long pkid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			if (mObject.remarks.length() > 200)
			{
				mObject.remarks = mObject.remarks.substring(0, 199);
			}
			pkid = insertObject(mObject);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (pkid != null)
		{
			this.mObject = mObject;
			this.mObject.pkid = new Long(pkid.longValue());
			// primary key!!!
			ejbStore();
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return pkid;
	}

	public Long ejbFindByPrimaryKey(Long pkid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(pkid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return pkid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + pkid.toString() + " not found.");
		}
	}

	public void ejbRemove()
	{
		try
		{
			deleteObject(this.mObject.pkid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
	}

	public void setEntityContext(EntityContext lContext)
	{
		Log.printVerbose(strObjectName + " in setEntityContext");
		this.mContext = lContext;
		Log.printVerbose(strObjectName + " Leaving setEntityContext");
	}

	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.mContext = null;
		Log.printVerbose(strObjectName + " Leaving unsetEntityContext");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.mObject.pkid = (Long) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.mObject.pkid = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadObject();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug(strObjectName + "ejbLoad: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbLoad");
	}

	public void ejbStore()
	{
		Log.printVerbose(strObjectName + " In ejbStore");
		try
		{
			storeObject();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(AuditTrailObject mObject)
	{
	}

	public Vector ejbHomeGetValueObjectsGiven(Integer iUserId, String namespace, Integer iAuditType,
			Integer iAuditLevel, String keyword, Timestamp dateFrom, Timestamp dateTo, String state, String status)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(iUserId, namespace, iAuditType, iAuditLevel, keyword, dateFrom, dateTo,
					state, status);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	/** ***************** Database Routines ************************ */
	private void makeConnection()
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			con = ds.getConnection();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void releaseConnection()
	{
		try
		{
			con.close();
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw new EJBException("releaseConnection: " + ex.getMessage());
		}
	} // releaseConnection

	// ////////////////////////////////////////////////////////
	/*
	 * private Collection getValObjectsGiven(String strCat, String strNamespace,
	 * String strValue1, String strValue2, String strOptions) throws
	 * SQLException {
	 *  // warning.. this function is not fully tested yet makeConnection();
	 * Log.printVerbose("in getValObjectsGiven()");
	 * 
	 * Collection colObj = new Vector(); String selectStatement = "select " + "
	 * pkid , " + " category , " + " namespace , " + " value1 , " + " value2 , " + "
	 * value3 , " + " value4 , " + " value5 , " + " binary_data , " + " status , " + "
	 * userid_edit , " + " time_edit , " + " time_effective " + " from " +
	 * TABLENAME + " where category = ? " + " and namespace = ? " + " and value1 = ? " + "
	 * and value2 = ? " + " order by pkid ";
	 * 
	 * PreparedStatement prepStmt = con.prepareStatement(selectStatement);
	 * prepStmt.setString(1,strCat); prepStmt.setString(2,strNamespace);
	 * prepStmt.setString(3,strValue1); prepStmt.setString(4,strValue2);
	 * 
	 * ResultSet rs = prepStmt.executeQuery(); rs.beforeFirst(); while
	 * (rs.next()) { AuditTrailObject laro = new AuditTrailObject(); laro.mPkid =
	 * new Integer(rs.getInt("pkid")); laro.mCategory =
	 * rs.getString("category"); laro.mNamespace = rs.getString("namespace");
	 * laro.mValue1 = rs.getString("value1"); laro.mValue2 =
	 * rs.getString("value2"); laro.mValue3 = rs.getString("value3");
	 * laro.mValue4 = rs.getString("value4"); laro.mValue5 =
	 * rs.getString("value5"); laro.mBinary = rs.getBytes("binary_data");
	 * laro.mStatus = rs.getString("status"); laro.mUserIdEdit = new
	 * Integer(rs.getInt("userid_edit")); laro.mTimeEdit =
	 * rs.getTimestamp("time_edit"); laro.mTimeEffective =
	 * rs.getTimestamp("time_effective");
	 * 
	 * Log.printVerbose(laro.toString()); colObj.add(laro);
	 *  } prepStmt.close(); releaseConnection();
	 * 
	 * return colObj;
	 *  }
	 * 
	 */
	// ////////////////////////////////////////////////////////
	private Long insertObject(AuditTrailObject mObject) throws SQLException
	{
		// Long newPkid = getNextPkid();
		Long newPkid = null;
		makeConnection();
		try
		{
			newPkid = getNextPKId(con);
		} catch (Exception ex)
		{
			throw new EJBException(strObjectName + ex.getMessage());
		}
		Log.printVerbose(strObjectName + "insertNewRow: ");
		Log.printVerbose("The new objectid is :" + newPkid.toString());
		String insertStatement = "INSERT INTO " + TABLENAME + " ( " + PKID + ", " + USERID + ", " + NAMESPACE + ", "
				+ AUDIT_TYPE + ", " + AUDIT_LEVEL + ", " + FOREIGN_TABLE1 + ", " + FOREIGN_KEY1 + ", " + FOREIGN_TABLE2
				+ ", " + FOREIGN_KEY2 + ", " + REMARKS + ", " + TIME + ", " + STATE + ", " + STATUS + ") VALUES ("
				+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ";
		PreparedStatement prepStmt = con.prepareStatement(insertStatement);
		prepStmt.setLong(1, newPkid.longValue());
		prepStmt.setInt(2, mObject.userId.intValue());
		prepStmt.setString(3, mObject.namespace);
		prepStmt.setInt(4, mObject.auditType.intValue());
		prepStmt.setInt(5, mObject.auditLevel.intValue());
		prepStmt.setString(6, mObject.foreignTable1);
		prepStmt.setLong(7, mObject.foreignKey1.longValue());
		prepStmt.setString(8, mObject.foreignTable2);
		prepStmt.setInt(9, mObject.foreignKey2.intValue());
		prepStmt.setString(10, mObject.remarks);
		prepStmt.setTimestamp(11, mObject.time);
		prepStmt.setString(12, mObject.state);
		prepStmt.setString(13, mObject.status);
		prepStmt.executeUpdate();
		// ResultSet rs = prepStmt.executeQuery();
		prepStmt.close();
		Log.printAudit(strObjectName + "Created New Row:" + newPkid.toString());
		Log.printVerbose(strObjectName + "leaving insertNewRow");
		releaseConnection();
		return newPkid;
	}

	// stop here
	private boolean selectByPrimaryKey(Long pkid) throws SQLException
	{
		makeConnection();
		String selectStatement = "select * " + "from " + TABLENAME + " where pkid = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setLong(1, pkid.longValue());
		ResultSet rs = prepStmt.executeQuery();
		boolean result = false;
		result = rs.next();
		prepStmt.close();
		releaseConnection();
		return result;
	}

	private void deleteObject(Long pkid) throws SQLException
	{
		makeConnection();
		String deleteStatement = "delete from " + TABLENAME + "  " + "where pkid = ?";
		PreparedStatement prepStmt = con.prepareStatement(deleteStatement);
		prepStmt.setLong(1, pkid.longValue());
		prepStmt.executeUpdate();
		prepStmt.close();
		Log.printAudit(strObjectName + "Deleted Object : " + pkid.toString());
		releaseConnection();
	}

	private Vector selectValueObjectsGiven(Integer iUserId, String namespace, Integer iAuditType, Integer iAuditLevel,
			String keyword, Timestamp dateFrom, Timestamp dateTo, String state, String status) throws SQLException
	{
		Vector vecValObj = new Vector();
		makeConnection();
		String selectStmt = "SELECT " + PKID + ", " + USERID + ", " + NAMESPACE + ", " + AUDIT_TYPE + ", "
				+ AUDIT_LEVEL + ", " + FOREIGN_TABLE1 + ", " + FOREIGN_KEY1 + ", " + FOREIGN_TABLE2 + ", "
				+ FOREIGN_KEY2 + ", " + REMARKS + ", " + TIME + ", " + STATE + ", " + STATUS + "  FROM " + TABLENAME
				+ " WHERE " + PKID + " != '-1' ";
		if (iUserId != null)
		{
			selectStmt += " AND " + USERID + "='" + iUserId.toString() + "' ";
		}
		if (namespace != null)
		{
			selectStmt += " AND " + NAMESPACE + "='" + namespace + "' ";
		}
		if (iAuditType != null)
		{
			selectStmt += " AND " + AUDIT_TYPE + "='" + iAuditType.toString() + "' ";
		}
		if (iAuditLevel != null)
		{
			selectStmt += " AND " + AUDIT_LEVEL + "='" + iAuditLevel.toString() + "' ";
		}
		if (keyword != null)
		{
			selectStmt += " AND " + REMARKS + "~*'" + keyword + "' ";
		}
		if (dateFrom != null)
		{
			selectStmt += " AND " + TIME + ">= '" + dateFrom + "' ";
		}
		if (dateTo != null)
		{
			selectStmt += " AND " + TIME + "< '" + dateTo + "' ";
		}
		if (state != null)
		{
			selectStmt += " AND " + STATE + " ='" + state + "' ";
		}
		if (status != null)
		{
			selectStmt += " AND " + STATUS + " ='" + status + "' ";
		}
		selectStmt += " ORDER BY " + TIME;
		PreparedStatement prepStmt = con.prepareStatement(selectStmt);
		ResultSet rs = prepStmt.executeQuery();
		while (rs.next())
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.pkid = new Long(rs.getLong(PKID));
			atObj.userId = new Integer(rs.getInt(USERID));
			atObj.namespace = rs.getString(NAMESPACE);
			atObj.auditType = new Integer(rs.getInt(AUDIT_TYPE));
			atObj.auditLevel = new Integer(rs.getInt(AUDIT_LEVEL));
			atObj.foreignTable1 = rs.getString(FOREIGN_TABLE1);
			atObj.foreignKey1 = new Long(rs.getLong(FOREIGN_KEY1));
			atObj.foreignTable2 = rs.getString(FOREIGN_TABLE2);
			atObj.foreignKey2 = new Integer(rs.getInt(FOREIGN_KEY2));
			atObj.remarks = rs.getString(REMARKS);
			atObj.time = rs.getTimestamp(TIME);
			atObj.state = rs.getString(STATE);
			atObj.status = rs.getString(STATUS);
			vecValObj.add(atObj);
		}
		prepStmt.close();
		releaseConnection();
		return vecValObj;
	}

	private void loadObject() throws SQLException
	{
		makeConnection();
		String selectStatement = "SELECT " + PKID + ", " + USERID + ", " + NAMESPACE + ", " + AUDIT_TYPE + ", "
				+ AUDIT_LEVEL + ", " + FOREIGN_TABLE1 + ", " + FOREIGN_KEY1 + ", " + FOREIGN_TABLE2 + ", "
				+ FOREIGN_KEY2 + ", " + REMARKS + ", " + TIME + ", " + STATE + ", " + STATUS + "  FROM " + TABLENAME
				+ " WHERE " + PKID + " = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setLong(1, this.mObject.pkid.longValue());
		ResultSet rs = prepStmt.executeQuery();
		if (rs.next())
		{
			this.mObject.pkid = new Long(rs.getLong(PKID));
			this.mObject.userId = new Integer(rs.getInt(USERID));
			this.mObject.namespace = rs.getString(NAMESPACE);
			this.mObject.auditType = new Integer(rs.getInt(AUDIT_TYPE));
			this.mObject.auditLevel = new Integer(rs.getInt(AUDIT_LEVEL));
			this.mObject.foreignTable1 = rs.getString(FOREIGN_TABLE1);
			this.mObject.foreignKey1 = new Long(rs.getLong(FOREIGN_KEY1));
			this.mObject.foreignTable2 = rs.getString(FOREIGN_TABLE2);
			this.mObject.foreignKey2 = new Integer(rs.getInt(FOREIGN_KEY2));
			this.mObject.remarks = rs.getString(REMARKS);
			this.mObject.time = rs.getTimestamp(TIME);
			this.mObject.state = rs.getString(STATE);
			this.mObject.status = rs.getString(STATUS);
			prepStmt.close();
		} else
		{
			prepStmt.close();
			throw new NoSuchEntityException("Row for this EJB Object" + " is not found in database.");
		}
		releaseConnection();
	}

	private void storeObject() throws SQLException
	{
		makeConnection();
		String updateStatement = "UPDATE " + TABLENAME + " SET " + PKID + " = ? , " + USERID + " = ? , " + NAMESPACE
				+ " = ? , " + AUDIT_TYPE + " = ? , " + AUDIT_LEVEL + " = ? , " + FOREIGN_TABLE1 + " = ? , "
				+ FOREIGN_KEY1 + " = ? , " + FOREIGN_TABLE2 + " = ? , " + FOREIGN_KEY2 + " = ? , " + REMARKS
				+ " = ? , " + TIME + " = ? , " + STATE + " = ? , " + STATUS + " = ?  " + " WHERE " + PKID + " = ?;";
		PreparedStatement prepStmt = con.prepareStatement(updateStatement);
		prepStmt.setLong(1, this.mObject.pkid.longValue());
		prepStmt.setInt(2, this.mObject.userId.intValue());
		prepStmt.setString(3, this.mObject.namespace);
		prepStmt.setInt(4, this.mObject.auditType.intValue());
		prepStmt.setInt(5, this.mObject.auditLevel.intValue());
		prepStmt.setString(6, this.mObject.foreignTable1);
		prepStmt.setLong(7, this.mObject.foreignKey1.longValue());
		prepStmt.setString(8, this.mObject.foreignTable2);
		prepStmt.setInt(9, this.mObject.foreignKey2.intValue());
		prepStmt.setString(10, this.mObject.remarks);
		prepStmt.setTimestamp(11, this.mObject.time);
		prepStmt.setString(12, this.mObject.state);
		prepStmt.setString(13, this.mObject.status);
		prepStmt.setLong(14, this.mObject.pkid.longValue());
		int rowCount = prepStmt.executeUpdate();
		prepStmt.close();
		if (rowCount == 0)
		{
			throw new EJBException("Storing ejb object " + this.mObject.pkid.toString() + " failed.");
		}
		releaseConnection();
	}

	private static synchronized Long getNextPKId(Connection con) throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, PKID, TABLENAME, MODULENAME, (Long) null);
	}
	/*
	 * private Long getNextPkid() { Long pkid = new Long("1001"); try {
	 * makeConnection(); String selectStatement = "select max(pkid) as maxid " +
	 * "from "+ TABLENAME ; PreparedStatement prepStmt =
	 * con.prepareStatement(selectStatement); ResultSet rs =
	 * prepStmt.executeQuery(); rs.beforeFirst(); long buffer=1000; if(rs!=null &&
	 * rs.next()) { buffer = rs.getLong("maxid"); if(buffer>0) { pkid = new
	 * Long(buffer+1);} else { buffer=1001; } } if(prepStmt!=null){
	 * prepStmt.close();} } // end try catch(Exception ex) {
	 * ex.printStackTrace(); pkid = new Long("1001"); } releaseConnection();
	 * return pkid; }
	 */
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.user;

import java.io.*;
import java.sql.*;
import java.rmi.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import javax.rmi.*;
import com.vlee.local.*;
import com.vlee.util.*;
import java.math.*;

public class AuditTrailBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "user_audit_trail";
	protected final String strObjectName = "AuditTrailBean: ";
	private Connection con;
	private EntityContext mContext;
	// public constants
	public static final String STATE_NONE = "none";
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String USERID = "userid";
	public static final String NAMESPACE = "namespace";
	public static final String AUDIT_TYPE = "audit_type";
	public static final String AUDIT_LEVEL = "audit_level";
	public static final String FOREIGN_TABLE1 = "foreign_table1";
	public static final String FOREIGN_KEY1 = "foreign_key1";
	public static final String FOREIGN_TABLE2 = "foreign_table2";
	public static final String FOREIGN_KEY2 = "foreign_key2";
	public static final String REMARKS = "remarks";
	public static final String TIME = "time";
	public static final String STATE = "state";
	public static final String STATUS = "status";
	public static final Integer TYPE_ACCESS = new Integer("1");
	public static final Integer TYPE_DEVELOPER = new Integer("200");
	public static final Integer TYPE_SYSADMIN = new Integer("300");
	public static final Integer TYPE_USER = new Integer("400");
	public static final Integer TYPE_TXN = new Integer("500");
	public static final Integer TYPE_ACC = new Integer("600");
	public static final Integer TYPE_USAGE = new Integer("700");
	public static final Integer TYPE_CONFIG = new Integer("800");
	public static final String MODULENAME = "user";
	// members ----------------------------------------------
	private AuditTrailObject mObject = new AuditTrailObject();

	public AuditTrailObject getObject()
	{
		return this.mObject;
	}

	public void setObject(AuditTrailObject mObject)
	{
		this.mObject = mObject;
	}

	public Long getPrimaryKey()
	{
		return this.mObject.pkid;
	}

	public void setPrimaryKey(Long pkid)
	{
		this.mObject.pkid = pkid;
	}

	public Long ejbCreate(AuditTrailObject mObject) throws CreateException
	{
		Long pkid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			if (mObject.remarks.length() > 200)
			{
				mObject.remarks = mObject.remarks.substring(0, 199);
			}
			pkid = insertObject(mObject);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (pkid != null)
		{
			this.mObject = mObject;
			this.mObject.pkid = new Long(pkid.longValue());
			// primary key!!!
			ejbStore();
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return pkid;
	}

	public Long ejbFindByPrimaryKey(Long pkid) throws FinderException
	{
		Log.printVerbose(strObjectName + " in ejbFindByPrimaryKey ");
		boolean result;
		try
		{
			result = selectByPrimaryKey(pkid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " leaving ejbFindByPrimaryKey ");
		if (result)
		{
			return pkid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + pkid.toString() + " not found.");
		}
	}

	public void ejbRemove()
	{
		try
		{
			deleteObject(this.mObject.pkid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
	}

	public void setEntityContext(EntityContext lContext)
	{
		Log.printVerbose(strObjectName + " in setEntityContext");
		this.mContext = lContext;
		Log.printVerbose(strObjectName + " Leaving setEntityContext");
	}

	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.mContext = null;
		Log.printVerbose(strObjectName + " Leaving unsetEntityContext");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.mObject.pkid = (Long) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.mObject.pkid = null;
		Log.printVerbose(strObjectName + " In ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadObject();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug(strObjectName + "ejbLoad: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbLoad");
	}

	public void ejbStore()
	{
		Log.printVerbose(strObjectName + " In ejbStore");
		try
		{
			storeObject();
		} catch (Exception ex)
		{
			throw new EJBException("ejbStore: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(AuditTrailObject mObject)
	{
	}

	public Vector ejbHomeGetValueObjectsGiven(Integer iUserId, String namespace, Integer iAuditType,
			Integer iAuditLevel, String keyword, Timestamp dateFrom, Timestamp dateTo, String state, String status)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(iUserId, namespace, iAuditType, iAuditLevel, keyword, dateFrom, dateTo,
					state, status);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	/** ***************** Database Routines ************************ */
	private void makeConnection()
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			con = ds.getConnection();
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void releaseConnection()
	{
		try
		{
			con.close();
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw new EJBException("releaseConnection: " + ex.getMessage());
		}
	} // releaseConnection

	// ////////////////////////////////////////////////////////
	/*
	 * private Collection getValObjectsGiven(String strCat, String strNamespace,
	 * String strValue1, String strValue2, String strOptions) throws
	 * SQLException {
	 *  // warning.. this function is not fully tested yet makeConnection();
	 * Log.printVerbose("in getValObjectsGiven()");
	 * 
	 * Collection colObj = new Vector(); String selectStatement = "select " + "
	 * pkid , " + " category , " + " namespace , " + " value1 , " + " value2 , " + "
	 * value3 , " + " value4 , " + " value5 , " + " binary_data , " + " status , " + "
	 * userid_edit , " + " time_edit , " + " time_effective " + " from " +
	 * TABLENAME + " where category = ? " + " and namespace = ? " + " and value1 = ? " + "
	 * and value2 = ? " + " order by pkid ";
	 * 
	 * PreparedStatement prepStmt = con.prepareStatement(selectStatement);
	 * prepStmt.setString(1,strCat); prepStmt.setString(2,strNamespace);
	 * prepStmt.setString(3,strValue1); prepStmt.setString(4,strValue2);
	 * 
	 * ResultSet rs = prepStmt.executeQuery(); //rs.beforeTheFirstRecord(); while
	 * (rs.next()) { AuditTrailObject laro = new AuditTrailObject(); laro.mPkid =
	 * new Integer(rs.getInt("pkid")); laro.mCategory =
	 * rs.getString("category"); laro.mNamespace = rs.getString("namespace");
	 * laro.mValue1 = rs.getString("value1"); laro.mValue2 =
	 * rs.getString("value2"); laro.mValue3 = rs.getString("value3");
	 * laro.mValue4 = rs.getString("value4"); laro.mValue5 =
	 * rs.getString("value5"); laro.mBinary = rs.getBytes("binary_data");
	 * laro.mStatus = rs.getString("status"); laro.mUserIdEdit = new
	 * Integer(rs.getInt("userid_edit")); laro.mTimeEdit =
	 * rs.getTimestamp("time_edit"); laro.mTimeEffective =
	 * rs.getTimestamp("time_effective");
	 * 
	 * Log.printVerbose(laro.toString()); colObj.add(laro);
	 *  } prepStmt.close(); releaseConnection();
	 * 
	 * return colObj;
	 *  }
	 * 
	 */
	// ////////////////////////////////////////////////////////
	private Long insertObject(AuditTrailObject mObject) throws SQLException
	{
		// Long newPkid = getNextPkid();
		Long newPkid = null;
		makeConnection();
		try
		{
			newPkid = getNextPKId(con);
		} catch (Exception ex)
		{
			throw new EJBException(strObjectName + ex.getMessage());
		}
		Log.printVerbose(strObjectName + "insertNewRow: ");
		Log.printVerbose("The new objectid is :" + newPkid.toString());
		String insertStatement = "INSERT INTO " + TABLENAME + " ( " + PKID + ", " + USERID + ", " + NAMESPACE + ", "
				+ AUDIT_TYPE + ", " + AUDIT_LEVEL + ", " + FOREIGN_TABLE1 + ", " + FOREIGN_KEY1 + ", " + FOREIGN_TABLE2
				+ ", " + FOREIGN_KEY2 + ", " + REMARKS + ", " + TIME + ", " + STATE + ", " + STATUS + ") VALUES ("
				+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ";
		PreparedStatement prepStmt = con.prepareStatement(insertStatement);
		prepStmt.setLong(1, newPkid.longValue());
		prepStmt.setInt(2, mObject.userId.intValue());
		prepStmt.setString(3, mObject.namespace);
		prepStmt.setInt(4, mObject.auditType.intValue());
		prepStmt.setInt(5, mObject.auditLevel.intValue());
		prepStmt.setString(6, mObject.foreignTable1);
		prepStmt.setLong(7, mObject.foreignKey1.longValue());
		prepStmt.setString(8, mObject.foreignTable2);
		prepStmt.setInt(9, mObject.foreignKey2.intValue());
		prepStmt.setString(10, mObject.remarks);
		prepStmt.setTimestamp(11, mObject.time);
		prepStmt.setString(12, mObject.state);
		prepStmt.setString(13, mObject.status);
		prepStmt.executeUpdate();
		// ResultSet rs = prepStmt.executeQuery();
		prepStmt.close();
		Log.printAudit(strObjectName + "Created New Row:" + newPkid.toString());
		Log.printVerbose(strObjectName + "leaving insertNewRow");
		releaseConnection();
		return newPkid;
	}

	// stop here
	private boolean selectByPrimaryKey(Long pkid) throws SQLException
	{
		makeConnection();
		String selectStatement = "select * " + "from " + TABLENAME + " where pkid = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setLong(1, pkid.longValue());
		ResultSet rs = prepStmt.executeQuery();
		boolean result = false;
		result = rs.next();
		prepStmt.close();
		releaseConnection();
		return result;
	}

	private void deleteObject(Long pkid) throws SQLException
	{
		makeConnection();
		String deleteStatement = "delete from " + TABLENAME + "  " + "where pkid = ?";
		PreparedStatement prepStmt = con.prepareStatement(deleteStatement);
		prepStmt.setLong(1, pkid.longValue());
		prepStmt.executeUpdate();
		prepStmt.close();
		Log.printAudit(strObjectName + "Deleted Object : " + pkid.toString());
		releaseConnection();
	}

	private Vector selectValueObjectsGiven(Integer iUserId, String namespace, Integer iAuditType, Integer iAuditLevel,
			String keyword, Timestamp dateFrom, Timestamp dateTo, String state, String status) throws SQLException
	{
		Vector vecValObj = new Vector();
		makeConnection();
		String selectStmt = "SELECT " + PKID + ", " + USERID + ", " + NAMESPACE + ", " + AUDIT_TYPE + ", "
				+ AUDIT_LEVEL + ", " + FOREIGN_TABLE1 + ", " + FOREIGN_KEY1 + ", " + FOREIGN_TABLE2 + ", "
				+ FOREIGN_KEY2 + ", " + REMARKS + ", " + TIME + ", " + STATE + ", " + STATUS + "  FROM " + TABLENAME
				+ " WHERE " + PKID + " != '-1' ";
		if (iUserId != null)
		{
			selectStmt += " AND " + USERID + "='" + iUserId.toString() + "' ";
		}
		if (namespace != null)
		{
			selectStmt += " AND " + NAMESPACE + "='" + namespace + "' ";
		}
		if (iAuditType != null)
		{
			selectStmt += " AND " + AUDIT_TYPE + "='" + iAuditType.toString() + "' ";
		}
		if (iAuditLevel != null)
		{
			selectStmt += " AND " + AUDIT_LEVEL + "='" + iAuditLevel.toString() + "' ";
		}
		if (keyword != null)
		{
			selectStmt += " AND " + REMARKS + "~*'" + keyword + "' ";
		}
		if (dateFrom != null)
		{
			selectStmt += " AND " + TIME + ">= '" + dateFrom + "' ";
		}
		if (dateTo != null)
		{
			selectStmt += " AND " + TIME + "< '" + dateTo + "' ";
		}
		if (state != null)
		{
			selectStmt += " AND " + STATE + " ='" + state + "' ";
		}
		if (status != null)
		{
			selectStmt += " AND " + STATUS + " ='" + status + "' ";
		}
		selectStmt += " ORDER BY " + TIME;
		PreparedStatement prepStmt = con.prepareStatement(selectStmt);
		ResultSet rs = prepStmt.executeQuery();
		while (rs.next())
		{
			AuditTrailObject atObj = new AuditTrailObject();
			atObj.pkid = new Long(rs.getLong(PKID));
			atObj.userId = new Integer(rs.getInt(USERID));
			atObj.namespace = rs.getString(NAMESPACE);
			atObj.auditType = new Integer(rs.getInt(AUDIT_TYPE));
			atObj.auditLevel = new Integer(rs.getInt(AUDIT_LEVEL));
			atObj.foreignTable1 = rs.getString(FOREIGN_TABLE1);
			atObj.foreignKey1 = new Long(rs.getLong(FOREIGN_KEY1));
			atObj.foreignTable2 = rs.getString(FOREIGN_TABLE2);
			atObj.foreignKey2 = new Integer(rs.getInt(FOREIGN_KEY2));
			atObj.remarks = rs.getString(REMARKS);
			atObj.time = rs.getTimestamp(TIME);
			atObj.state = rs.getString(STATE);
			atObj.status = rs.getString(STATUS);
			vecValObj.add(atObj);
		}
		prepStmt.close();
		releaseConnection();
		return vecValObj;
	}

	private void loadObject() throws SQLException
	{
		makeConnection();
		String selectStatement = "SELECT " + PKID + ", " + USERID + ", " + NAMESPACE + ", " + AUDIT_TYPE + ", "
				+ AUDIT_LEVEL + ", " + FOREIGN_TABLE1 + ", " + FOREIGN_KEY1 + ", " + FOREIGN_TABLE2 + ", "
				+ FOREIGN_KEY2 + ", " + REMARKS + ", " + TIME + ", " + STATE + ", " + STATUS + "  FROM " + TABLENAME
				+ " WHERE " + PKID + " = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setLong(1, this.mObject.pkid.longValue());
		ResultSet rs = prepStmt.executeQuery();
		if (rs.next())
		{
			this.mObject.pkid = new Long(rs.getLong(PKID));
			this.mObject.userId = new Integer(rs.getInt(USERID));
			this.mObject.namespace = rs.getString(NAMESPACE);
			this.mObject.auditType = new Integer(rs.getInt(AUDIT_TYPE));
			this.mObject.auditLevel = new Integer(rs.getInt(AUDIT_LEVEL));
			this.mObject.foreignTable1 = rs.getString(FOREIGN_TABLE1);
			this.mObject.foreignKey1 = new Long(rs.getLong(FOREIGN_KEY1));
			this.mObject.foreignTable2 = rs.getString(FOREIGN_TABLE2);
			this.mObject.foreignKey2 = new Integer(rs.getInt(FOREIGN_KEY2));
			this.mObject.remarks = rs.getString(REMARKS);
			this.mObject.time = rs.getTimestamp(TIME);
			this.mObject.state = rs.getString(STATE);
			this.mObject.status = rs.getString(STATUS);
			prepStmt.close();
		} else
		{
			prepStmt.close();
			throw new NoSuchEntityException("Row for this EJB Object" + " is not found in database.");
		}
		releaseConnection();
	}

	private void storeObject() throws SQLException
	{
		makeConnection();
		String updateStatement = "UPDATE " + TABLENAME + " SET " + PKID + " = ? , " + USERID + " = ? , " + NAMESPACE
				+ " = ? , " + AUDIT_TYPE + " = ? , " + AUDIT_LEVEL + " = ? , " + FOREIGN_TABLE1 + " = ? , "
				+ FOREIGN_KEY1 + " = ? , " + FOREIGN_TABLE2 + " = ? , " + FOREIGN_KEY2 + " = ? , " + REMARKS
				+ " = ? , " + TIME + " = ? , " + STATE + " = ? , " + STATUS + " = ?  " + " WHERE " + PKID + " = ?;";
		PreparedStatement prepStmt = con.prepareStatement(updateStatement);
		prepStmt.setLong(1, this.mObject.pkid.longValue());
		prepStmt.setInt(2, this.mObject.userId.intValue());
		prepStmt.setString(3, this.mObject.namespace);
		prepStmt.setInt(4, this.mObject.auditType.intValue());
		prepStmt.setInt(5, this.mObject.auditLevel.intValue());
		prepStmt.setString(6, this.mObject.foreignTable1);
		prepStmt.setLong(7, this.mObject.foreignKey1.longValue());
		prepStmt.setString(8, this.mObject.foreignTable2);
		prepStmt.setInt(9, this.mObject.foreignKey2.intValue());
		prepStmt.setString(10, this.mObject.remarks);
		prepStmt.setTimestamp(11, this.mObject.time);
		prepStmt.setString(12, this.mObject.state);
		prepStmt.setString(13, this.mObject.status);
		prepStmt.setLong(14, this.mObject.pkid.longValue());
		int rowCount = prepStmt.executeUpdate();
		prepStmt.close();
		if (rowCount == 0)
		{
			throw new EJBException("Storing ejb object " + this.mObject.pkid.toString() + " failed.");
		}
		releaseConnection();
	}

	private static synchronized Long getNextPKId(Connection con) throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, PKID, TABLENAME, MODULENAME, (Long) null);
	}
	/*
	 * private Long getNextPkid() { Long pkid = new Long("1001"); try {
	 * makeConnection(); String selectStatement = "select max(pkid) as maxid " +
	 * "from "+ TABLENAME ; PreparedStatement prepStmt =
	 * con.prepareStatement(selectStatement); ResultSet rs =
	 * prepStmt.executeQuery(); //rs.beforeTheFirstRecord(); long buffer=1000; if(rs!=null &&
	 * rs.next()) { buffer = rs.getLong("maxid"); if(buffer>0) { pkid = new
	 * Long(buffer+1);} else { buffer=1001; } } if(prepStmt!=null){
	 * prepStmt.close();} } // end try catch(Exception ex) {
	 * ex.printStackTrace(); pkid = new Long("1001"); } releaseConnection();
	 * return pkid; }
	 */
}
