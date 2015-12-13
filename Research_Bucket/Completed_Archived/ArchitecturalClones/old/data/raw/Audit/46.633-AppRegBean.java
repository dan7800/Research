/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.application;

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

public class AppRegBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strObjectTable = "app_registry_index";
	protected final String strObjectName = "AppRegBean: ";
	private Connection con;
	private EntityContext mContext;
	protected final String strTimeBegin = "0000-01-01 00:00:00.000000000";
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String STATUS = "status";
	// Scheme
	public static final String CAT_IMAGE_URL = "image_url";
	public static final String NS_SERVICE_CTR = "service_center";
	public static final String BIZTYPE_AUTOSVC = "autoService";
	public static final String BIZTYPE_TRADING = "trading";
	public static final String TOPBAR_LOGO_DEFAULT = "templates/default/images/wavelet_emp.gif";
	// members ----------------------------------------------
	private Integer mPkid; // primary key !!!
	private String mCategory;
	private String mNamespace;
	private String mValue1;
	private String mValue2;
	private String mValue3;
	private String mValue4;
	private String mValue5;
	private byte[] mBinary;
	private String mStatus;
	private Integer mUserIdEdit;
	private Timestamp mTimeEdit;
	private Timestamp mTimeEffective;

	public Integer getPkid()
	{
		return this.mPkid;
	}

	public void setPkid(Integer pkid)
	{
		this.mPkid = pkid;
	}

	public String getCategory()
	{
		return this.mCategory;
	}

	public void setCategory(String strCategory)
	{
		this.mCategory = strCategory;
	}

	public String getNamespace()
	{
		return this.mNamespace;
	}

	public void setNamespace(String strNamespace)
	{
		this.mNamespace = strNamespace;
	}

	public String getValue1()
	{
		return this.mValue1;
	}

	public void setValue1(String strValue1)
	{
		this.mValue1 = strValue1;
	}

	public String getValue2()
	{
		return this.mValue2;
	}

	public void setValue2(String strValue2)
	{
		this.mValue2 = strValue2;
	}

	public String getValue3()
	{
		return this.mValue3;
	}

	public void setValue3(String strValue3)
	{
		this.mValue3 = strValue3;
	}

	public String getValue4()
	{
		return this.mValue4;
	}

	public void setValue4(String strValue4)
	{
		this.mValue4 = strValue4;
	}

	public String getValue5()
	{
		return this.mValue5;
	}

	public void setValue5(String strValue5)
	{
		this.mValue5 = strValue5;
	}

	public byte[] getBinary()
	{
		return this.mBinary;
	}

	public void setBinary(byte[] lBinary)
	{
		this.mBinary = lBinary;
	}

	public String getStatus()
	{
		return this.mStatus;
	}

	public void setStatus(String stts)
	{
		this.mStatus = stts;
	}

	public Integer getUserIdEdit()
	{
		return this.mUserIdEdit;
	}

	public void setUserIdEdit(Integer intUserId)
	{
		this.mUserIdEdit = intUserId;
	}

	public Timestamp getTimeEdit()
	{
		return this.mTimeEdit;
	}

	public void setTimeEdit(Timestamp tsTime)
	{
		this.mTimeEdit = tsTime;
	}

	public Timestamp getTimeEffective()
	{
		return this.mTimeEffective;
	}

	public void setTimeEffective(Timestamp tsTime)
	{
		this.mTimeEffective = tsTime;
	}

	public Integer getPrimaryKey()
	{
		return this.mPkid;
	}

	public void setPrimaryKey(Integer regid)
	{
		this.mPkid = regid;
	}

	public Integer ejbCreate(String strCategory, String strNamespace, String strValue1, String strValue2,
			String strValue3, String strValue4, String strValue5, Integer useridEdit, Timestamp tsEdit,
			Timestamp tsEffective) throws CreateException
	{
		Integer pkid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			pkid = insertNewRow(strCategory, strNamespace, strValue1, strValue2, strValue3, strValue4, strValue5,
					useridEdit, tsEdit, tsEffective);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (pkid != null)
		{
			this.mPkid = new Integer(pkid.intValue()); // primary key!!!
			this.mCategory = strCategory;
			this.mNamespace = strNamespace;
			this.mValue1 = strValue1;
			this.mValue2 = strValue2;
			this.mValue3 = strValue3;
			this.mValue4 = strValue4;
			this.mValue5 = strValue5;
			this.mUserIdEdit = useridEdit;
			this.mStatus = AppRegBean.ACTIVE;
			this.mTimeEdit = tsEdit;
			this.mTimeEffective = Timestamp.valueOf(strTimeBegin);
			ejbStore();
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return pkid;
	}

	public Integer ejbFindByPrimaryKey(Integer pkid) throws FinderException
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
			deleteObject(this.mPkid);
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
		this.mPkid = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.mPkid = null;
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

	public void ejbPostCreate(String strCategory, String strNamespace, String strValue1, String strValue2,
			String strValue3, String strValue4, String strValue5, Integer useridEdit, Timestamp tsEdit,
			Timestamp tsEffective)
	{
	}

	public Collection ejbFindAllObjects() throws FinderException
	{
		try
		{
			Collection bufAL = selectAllObjects();
			return bufAL;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug(strObjectName + "ejbFindAllObjects: " + ex);
			return null;
		}
	}

	public Collection ejbFindObjectsGiven(String fieldName, String value) throws FinderException
	{
		try
		{
			Collection bufAL = selectObjectsGiven(fieldName, value);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindObjectsGiven: " + ex.getMessage());
			return null;
		}
	}

	public Collection ejbFindEJBObjects(String strCat, String strNamespace, String strValue1, String strValue2,
			String strValue3, String strValue4, String strValue5)
	{
		Log.printVerbose("In ejbFindObjectsGiven()");
		try
		{
			return getEJBObjectsGiven(strCat, strNamespace, strValue1, strValue2, strValue3, strValue4, strValue5);
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbHomeGetActiveValObjects: " + ex.getMessage());
			return null;
		}
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
	private Collection getValObjectsGiven(String strCat, String strNamespace, String strValue1, String strValue2,
			String strOptions) throws SQLException
	{
		// warning.. this function is not fully tested yet
		makeConnection();
		Log.printVerbose("in getValObjectsGiven()");
		Collection colObj = new Vector();
		String selectStatement = "select " + " pkid , " + " category , " + " namespace , " + " value1 , "
				+ " value2 , " + " value3 , " + " value4 , " + " value5 , " + " binary_data , " + " status , "
				+ " userid_edit , " + " time_edit , " + " time_effective " + " from " + strObjectTable
				+ " where  category = ? " + " and namespace = ? " + " and value1 = ? " + " and value2 = ? "
				+ " order by pkid ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, strCat);
		prepStmt.setString(2, strNamespace);
		prepStmt.setString(3, strValue1);
		prepStmt.setString(4, strValue2);
		ResultSet rs = prepStmt.executeQuery();
		//rs.beforeTheFirstRecord();
		while (rs.next())
		{
			AppRegObject laro = new AppRegObject();
			laro.mPkid = new Integer(rs.getInt("pkid"));
			laro.mCategory = rs.getString("category");
			laro.mNamespace = rs.getString("namespace");
			laro.mValue1 = rs.getString("value1");
			laro.mValue2 = rs.getString("value2");
			laro.mValue3 = rs.getString("value3");
			laro.mValue4 = rs.getString("value4");
			laro.mValue5 = rs.getString("value5");
			laro.mBinary = rs.getBytes("binary_data");
			laro.mStatus = rs.getString("status");
			laro.mUserIdEdit = new Integer(rs.getInt("userid_edit"));
			laro.mTimeEdit = rs.getTimestamp("time_edit");
			laro.mTimeEffective = rs.getTimestamp("time_effective");
			Log.printVerbose(laro.toString());
			colObj.add(laro);
		}
		prepStmt.close();
		releaseConnection();
		return colObj;
	}

	// ////////////////////////////////////////////////////////
	private Collection getEJBObjectsGiven(String strCat, String strNamespace, String strValue1, String strValue2,
			String strValue3, String strValue4, String strValue5) throws SQLException
	{
		makeConnection();
		ArrayList objectSet = new ArrayList();
		String selectStatement = " select pkid from " + strObjectTable + " where  category = ? "
				+ " and namespace = ? ";
		if (strValue1 != null)
		{
			selectStatement += " and value1 like '" + strValue1 + "' ";
		}
		if (strValue2 != null)
		{
			selectStatement += " and value2 like '" + strValue2 + "' ";
		}
		if (strValue3 != null)
		{
			selectStatement += " and value3 like '" + strValue3 + "' ";
		}
		if (strValue4 != null)
		{
			selectStatement += " and value4 like '" + strValue4 + "' ";
		}
		if (strValue5 != null)
		{
			selectStatement += " and value5 like '" + strValue5 + "' ";
		}
		selectStatement += " order by pkid ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, strCat);
		prepStmt.setString(2, strNamespace);
		// prepStmt.setString(3,strValue1);
		// prepStmt.setString(4,strValue2);
		ResultSet rs = prepStmt.executeQuery();
		//rs.beforeTheFirstRecord();
		while (rs.next())
		{
			objectSet.add(new Integer(rs.getInt(1)));
		}
		prepStmt.close();
		releaseConnection();
		return objectSet;
	}

	private Integer insertNewRow(String strCategory, String strNamespace, String strValue1, String strValue2,
			String strValue3, String strValue4, String strValue5, Integer useridEdit, Timestamp tsEdit,
			Timestamp tsEffective) throws SQLException
	{
		String strBinary = new String("null");
		this.mBinary = strBinary.getBytes();
		makeConnection();
		Log.printVerbose(strObjectName + "insertNewRow: ");
		String findMaxPkidStmt = " select pkid from " + strObjectTable + " ";
		PreparedStatement prepStmt = con.prepareStatement(findMaxPkidStmt);
		ResultSet rs = prepStmt.executeQuery();
		int bufInt = 0;
		while (rs.next())
		{
			if (bufInt < rs.getInt("pkid"))
			{
				bufInt = rs.getInt("pkid");
			}
		}
		Integer newPkid = new Integer(bufInt + 1); // new Integer(rs.getInt(1)
													// + 1);
		if (newPkid.intValue() < 1000)
			newPkid = new Integer("1000");
		Log.printVerbose("The new objectid is :" + newPkid.toString());
		String insertStatement = "insert into " + strObjectTable + " (pkid, category,   " + " namespace , value1, "
				+ " value2, value3, value4, value5, binary_data ," + " status, userid_edit, "
				+ " time_edit , time_effective )  " + " values ( ?, ?, ? , ?, ?, ?, ?, ?, ?, ?, " + " ?, ?, ? )";
		prepStmt = con.prepareStatement(insertStatement);
		int lZero = 0;
		prepStmt.setInt(1, newPkid.intValue());
		prepStmt.setString(2, strCategory);
		prepStmt.setString(3, strNamespace);
		prepStmt.setString(4, strValue1);
		prepStmt.setString(5, strValue1);
		prepStmt.setString(6, strValue1);
		prepStmt.setString(7, strValue1);
		prepStmt.setString(8, strValue1);
		prepStmt.setBytes(9, this.mBinary);
		prepStmt.setString(10, AppRegBean.ACTIVE);
		prepStmt.setInt(11, useridEdit.intValue());
		prepStmt.setTimestamp(12, tsEdit);
		prepStmt.setTimestamp(13, tsEffective);
		prepStmt.executeUpdate();
		prepStmt.close();
		Log.printAudit(strObjectName + "Created New Row:" + newPkid.toString());
		Log.printVerbose(strObjectName + "leaving insertNewRow");
		releaseConnection();
		return newPkid;
	}

	// stop here
	private boolean selectByPrimaryKey(Integer pkid) throws SQLException
	{
		makeConnection();
		String selectStatement = "select * " + "from " + strObjectTable + " where pkid = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setInt(1, pkid.intValue());
		ResultSet rs = prepStmt.executeQuery();
		boolean result = false;
		result = rs.next();
		prepStmt.close();
		releaseConnection();
		return result;
	}

	private void deleteObject(Integer pkid) throws SQLException
	{
		makeConnection();
		String deleteStatement = "delete from " + strObjectTable + "  " + "where pkid = ?";
		PreparedStatement prepStmt = con.prepareStatement(deleteStatement);
		prepStmt.setInt(1, pkid.intValue());
		prepStmt.executeUpdate();
		prepStmt.close();
		Log.printAudit(strObjectName + "Deleted Object : " + pkid.toString());
		releaseConnection();
	}

	private void loadObject() throws SQLException
	{
		makeConnection();
		String selectStatement = "select pkid , category, " + " namespace, value1, "
				+ " value2, value3, value4, value5, binary_data, " + " status, userid_edit , "
				+ " time_edit, time_effective " + " from " + strObjectTable + " where pkid = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setInt(1, this.mPkid.intValue());
		ResultSet rs = prepStmt.executeQuery();
		if (rs.next())
		{
			this.mPkid = new Integer(rs.getInt("pkid"));
			this.mCategory = rs.getString("category");
			this.mNamespace = rs.getString("namespace");
			this.mValue1 = rs.getString("value1");
			this.mValue2 = rs.getString("value2");
			this.mValue3 = rs.getString("value3");
			this.mValue4 = rs.getString("value4");
			this.mValue5 = rs.getString("value5");
			this.mBinary = rs.getBytes("binary_data");
			this.mStatus = rs.getString("status");
			this.mUserIdEdit = new Integer(rs.getInt("userid_edit"));
			this.mTimeEdit = rs.getTimestamp("time_edit");
			this.mTimeEffective = rs.getTimestamp("time_effective");
			prepStmt.close();
		} else
		{
			prepStmt.close();
			throw new NoSuchEntityException("Row for this EJB Object" + " is not found in database.");
		}
		releaseConnection();
	}

	private Collection selectAllObjects() throws SQLException
	{
		ArrayList objectSet = new ArrayList();
		makeConnection();
		String selectStatement = " select pkid from " + strObjectTable + "  ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		//rs.beforeTheFirstRecord();
		while (rs.next())
		{
			objectSet.add(new Integer(rs.getInt(1)));
		}
		prepStmt.close();
		releaseConnection();
		return objectSet;
	}

	private Collection selectObjectsGiven(String fieldName, String value) throws SQLException
	{
		Log.printVerbose(" criteria : " + fieldName + " " + value);
		ArrayList objectSet = new ArrayList();
		makeConnection();
		String selectStatement = " select pkid from " + strObjectTable + "  where " + fieldName + " = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, value);
		ResultSet rs = prepStmt.executeQuery();
		//rs.beforeTheFirstRecord();
		while (rs.next())
		{
			objectSet.add(new Integer(rs.getInt(1)));
		}
		prepStmt.close();
		releaseConnection();
		return objectSet;
	}

	private void storeObject() throws SQLException
	{
		makeConnection();
		String updateStatement = "update " + strObjectTable + " set pkid = ? , " + " category = ? , "
				+ " namespace = ? , " + " value1 = ? , " + " value2 = ? , " + " value3 = ? , " + " value4 = ? , "
				+ " value5 = ? , " + " binary_data = ? , " + " status = ? , " + " userid_edit = ? , "
				+ " time_edit = ? , " + " time_effective = ?  " + " where pkid = ?;";
		PreparedStatement prepStmt = con.prepareStatement(updateStatement);
		prepStmt.setInt(1, this.mPkid.intValue());
		prepStmt.setString(2, this.mCategory);
		prepStmt.setString(3, this.mNamespace);
		prepStmt.setString(4, this.mValue1);
		prepStmt.setString(5, this.mValue2);
		prepStmt.setString(6, this.mValue3);
		prepStmt.setString(7, this.mValue4);
		prepStmt.setString(8, this.mValue5);
		prepStmt.setBytes(9, this.mBinary);
		prepStmt.setString(10, this.mStatus);
		prepStmt.setInt(11, this.mUserIdEdit.intValue());
		prepStmt.setTimestamp(12, this.mTimeEdit);
		prepStmt.setTimestamp(13, this.mTimeEffective);
		prepStmt.setInt(14, this.mPkid.intValue());
		int rowCount = prepStmt.executeUpdate();
		prepStmt.close();
		if (rowCount == 0)
		{
			throw new EJBException("Storing ejb object " + this.mPkid.toString() + " failed.");
		}
		releaseConnection();
	}
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
package com.vlee.ejb.application;

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

public class AppRegBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strObjectTable = "app_registry_index";
	protected final String strObjectName = "AppRegBean: ";
	private Connection con;
	private EntityContext mContext;
	protected final String strTimeBegin = "0000-01-01 00:00:00.000000000";
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String STATUS = "status";
	// Scheme
	public static final String CAT_IMAGE_URL = "image_url";
	public static final String NS_SERVICE_CTR = "service_center";
	public static final String BIZTYPE_AUTOSVC = "autoService";
	public static final String BIZTYPE_TRADING = "trading";
	public static final String TOPBAR_LOGO_DEFAULT = "templates/default/images/wavelet_emp.gif";
	// members ----------------------------------------------
	private Integer mPkid; // primary key !!!
	private String mCategory;
	private String mNamespace;
	private String mValue1;
	private String mValue2;
	private String mValue3;
	private String mValue4;
	private String mValue5;
	private byte[] mBinary;
	private String mStatus;
	private Integer mUserIdEdit;
	private Timestamp mTimeEdit;
	private Timestamp mTimeEffective;

	public Integer getPkid()
	{
		return this.mPkid;
	}

	public void setPkid(Integer pkid)
	{
		this.mPkid = pkid;
	}

	public String getCategory()
	{
		return this.mCategory;
	}

	public void setCategory(String strCategory)
	{
		this.mCategory = strCategory;
	}

	public String getNamespace()
	{
		return this.mNamespace;
	}

	public void setNamespace(String strNamespace)
	{
		this.mNamespace = strNamespace;
	}

	public String getValue1()
	{
		return this.mValue1;
	}

	public void setValue1(String strValue1)
	{
		this.mValue1 = strValue1;
	}

	public String getValue2()
	{
		return this.mValue2;
	}

	public void setValue2(String strValue2)
	{
		this.mValue2 = strValue2;
	}

	public String getValue3()
	{
		return this.mValue3;
	}

	public void setValue3(String strValue3)
	{
		this.mValue3 = strValue3;
	}

	public String getValue4()
	{
		return this.mValue4;
	}

	public void setValue4(String strValue4)
	{
		this.mValue4 = strValue4;
	}

	public String getValue5()
	{
		return this.mValue5;
	}

	public void setValue5(String strValue5)
	{
		this.mValue5 = strValue5;
	}

	public byte[] getBinary()
	{
		return this.mBinary;
	}

	public void setBinary(byte[] lBinary)
	{
		this.mBinary = lBinary;
	}

	public String getStatus()
	{
		return this.mStatus;
	}

	public void setStatus(String stts)
	{
		this.mStatus = stts;
	}

	public Integer getUserIdEdit()
	{
		return this.mUserIdEdit;
	}

	public void setUserIdEdit(Integer intUserId)
	{
		this.mUserIdEdit = intUserId;
	}

	public Timestamp getTimeEdit()
	{
		return this.mTimeEdit;
	}

	public void setTimeEdit(Timestamp tsTime)
	{
		this.mTimeEdit = tsTime;
	}

	public Timestamp getTimeEffective()
	{
		return this.mTimeEffective;
	}

	public void setTimeEffective(Timestamp tsTime)
	{
		this.mTimeEffective = tsTime;
	}

	public Integer getPrimaryKey()
	{
		return this.mPkid;
	}

	public void setPrimaryKey(Integer regid)
	{
		this.mPkid = regid;
	}

	public Integer ejbCreate(String strCategory, String strNamespace, String strValue1, String strValue2,
			String strValue3, String strValue4, String strValue5, Integer useridEdit, Timestamp tsEdit,
			Timestamp tsEffective) throws CreateException
	{
		Integer pkid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			pkid = insertNewRow(strCategory, strNamespace, strValue1, strValue2, strValue3, strValue4, strValue5,
					useridEdit, tsEdit, tsEffective);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (pkid != null)
		{
			this.mPkid = new Integer(pkid.intValue()); // primary key!!!
			this.mCategory = strCategory;
			this.mNamespace = strNamespace;
			this.mValue1 = strValue1;
			this.mValue2 = strValue2;
			this.mValue3 = strValue3;
			this.mValue4 = strValue4;
			this.mValue5 = strValue5;
			this.mUserIdEdit = useridEdit;
			this.mStatus = AppRegBean.ACTIVE;
			this.mTimeEdit = tsEdit;
			this.mTimeEffective = Timestamp.valueOf(strTimeBegin);
			ejbStore();
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return pkid;
	}

	public Integer ejbFindByPrimaryKey(Integer pkid) throws FinderException
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
			deleteObject(this.mPkid);
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
		this.mPkid = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.mPkid = null;
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

	public void ejbPostCreate(String strCategory, String strNamespace, String strValue1, String strValue2,
			String strValue3, String strValue4, String strValue5, Integer useridEdit, Timestamp tsEdit,
			Timestamp tsEffective)
	{
	}

	public Collection ejbFindAllObjects() throws FinderException
	{
		try
		{
			Collection bufAL = selectAllObjects();
			return bufAL;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug(strObjectName + "ejbFindAllObjects: " + ex);
			return null;
		}
	}

	public Collection ejbFindObjectsGiven(String fieldName, String value) throws FinderException
	{
		try
		{
			Collection bufAL = selectObjectsGiven(fieldName, value);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindObjectsGiven: " + ex.getMessage());
			return null;
		}
	}

	public Collection ejbFindEJBObjects(String strCat, String strNamespace, String strValue1, String strValue2,
			String strValue3, String strValue4, String strValue5)
	{
		Log.printVerbose("In ejbFindObjectsGiven()");
		try
		{
			return getEJBObjectsGiven(strCat, strNamespace, strValue1, strValue2, strValue3, strValue4, strValue5);
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbHomeGetActiveValObjects: " + ex.getMessage());
			return null;
		}
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
	private Collection getValObjectsGiven(String strCat, String strNamespace, String strValue1, String strValue2,
			String strOptions) throws SQLException
	{
		// warning.. this function is not fully tested yet
		makeConnection();
		Log.printVerbose("in getValObjectsGiven()");
		Collection colObj = new Vector();
		String selectStatement = "select " + " pkid , " + " category , " + " namespace , " + " value1 , "
				+ " value2 , " + " value3 , " + " value4 , " + " value5 , " + " binary_data , " + " status , "
				+ " userid_edit , " + " time_edit , " + " time_effective " + " from " + strObjectTable
				+ " where  category = ? " + " and namespace = ? " + " and value1 = ? " + " and value2 = ? "
				+ " order by pkid ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, strCat);
		prepStmt.setString(2, strNamespace);
		prepStmt.setString(3, strValue1);
		prepStmt.setString(4, strValue2);
		ResultSet rs = prepStmt.executeQuery();
		rs.beforeFirst();
		while (rs.next())
		{
			AppRegObject laro = new AppRegObject();
			laro.mPkid = new Integer(rs.getInt("pkid"));
			laro.mCategory = rs.getString("category");
			laro.mNamespace = rs.getString("namespace");
			laro.mValue1 = rs.getString("value1");
			laro.mValue2 = rs.getString("value2");
			laro.mValue3 = rs.getString("value3");
			laro.mValue4 = rs.getString("value4");
			laro.mValue5 = rs.getString("value5");
			laro.mBinary = rs.getBytes("binary_data");
			laro.mStatus = rs.getString("status");
			laro.mUserIdEdit = new Integer(rs.getInt("userid_edit"));
			laro.mTimeEdit = rs.getTimestamp("time_edit");
			laro.mTimeEffective = rs.getTimestamp("time_effective");
			Log.printVerbose(laro.toString());
			colObj.add(laro);
		}
		prepStmt.close();
		releaseConnection();
		return colObj;
	}

	// ////////////////////////////////////////////////////////
	private Collection getEJBObjectsGiven(String strCat, String strNamespace, String strValue1, String strValue2,
			String strValue3, String strValue4, String strValue5) throws SQLException
	{
		makeConnection();
		ArrayList objectSet = new ArrayList();
		String selectStatement = " select pkid from " + strObjectTable + " where  category = ? "
				+ " and namespace = ? ";
		if (strValue1 != null)
		{
			selectStatement += " and value1 like '" + strValue1 + "' ";
		}
		if (strValue2 != null)
		{
			selectStatement += " and value2 like '" + strValue2 + "' ";
		}
		if (strValue3 != null)
		{
			selectStatement += " and value3 like '" + strValue3 + "' ";
		}
		if (strValue4 != null)
		{
			selectStatement += " and value4 like '" + strValue4 + "' ";
		}
		if (strValue5 != null)
		{
			selectStatement += " and value5 like '" + strValue5 + "' ";
		}
		selectStatement += " order by pkid ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, strCat);
		prepStmt.setString(2, strNamespace);
		// prepStmt.setString(3,strValue1);
		// prepStmt.setString(4,strValue2);
		ResultSet rs = prepStmt.executeQuery();
		rs.beforeFirst();
		while (rs.next())
		{
			objectSet.add(new Integer(rs.getInt(1)));
		}
		prepStmt.close();
		releaseConnection();
		return objectSet;
	}

	private Integer insertNewRow(String strCategory, String strNamespace, String strValue1, String strValue2,
			String strValue3, String strValue4, String strValue5, Integer useridEdit, Timestamp tsEdit,
			Timestamp tsEffective) throws SQLException
	{
		String strBinary = new String("null");
		this.mBinary = strBinary.getBytes();
		makeConnection();
		Log.printVerbose(strObjectName + "insertNewRow: ");
		String findMaxPkidStmt = " select pkid from " + strObjectTable + " ";
		PreparedStatement prepStmt = con.prepareStatement(findMaxPkidStmt);
		ResultSet rs = prepStmt.executeQuery();
		int bufInt = 0;
		while (rs.next())
		{
			if (bufInt < rs.getInt("pkid"))
			{
				bufInt = rs.getInt("pkid");
			}
		}
		Integer newPkid = new Integer(bufInt + 1); // new Integer(rs.getInt(1)
													// + 1);
		if (newPkid.intValue() < 1000)
			newPkid = new Integer("1000");
		Log.printVerbose("The new objectid is :" + newPkid.toString());
		String insertStatement = "insert into " + strObjectTable + " (pkid, category,   " + " namespace , value1, "
				+ " value2, value3, value4, value5, binary_data ," + " status, userid_edit, "
				+ " time_edit , time_effective )  " + " values ( ?, ?, ? , ?, ?, ?, ?, ?, ?, ?, " + " ?, ?, ? )";
		prepStmt = con.prepareStatement(insertStatement);
		int lZero = 0;
		prepStmt.setInt(1, newPkid.intValue());
		prepStmt.setString(2, strCategory);
		prepStmt.setString(3, strNamespace);
		prepStmt.setString(4, strValue1);
		prepStmt.setString(5, strValue1);
		prepStmt.setString(6, strValue1);
		prepStmt.setString(7, strValue1);
		prepStmt.setString(8, strValue1);
		prepStmt.setBytes(9, this.mBinary);
		prepStmt.setString(10, AppRegBean.ACTIVE);
		prepStmt.setInt(11, useridEdit.intValue());
		prepStmt.setTimestamp(12, tsEdit);
		prepStmt.setTimestamp(13, tsEffective);
		prepStmt.executeUpdate();
		prepStmt.close();
		Log.printAudit(strObjectName + "Created New Row:" + newPkid.toString());
		Log.printVerbose(strObjectName + "leaving insertNewRow");
		releaseConnection();
		return newPkid;
	}

	// stop here
	private boolean selectByPrimaryKey(Integer pkid) throws SQLException
	{
		makeConnection();
		String selectStatement = "select * " + "from " + strObjectTable + " where pkid = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setInt(1, pkid.intValue());
		ResultSet rs = prepStmt.executeQuery();
		boolean result = false;
		result = rs.next();
		prepStmt.close();
		releaseConnection();
		return result;
	}

	private void deleteObject(Integer pkid) throws SQLException
	{
		makeConnection();
		String deleteStatement = "delete from " + strObjectTable + "  " + "where pkid = ?";
		PreparedStatement prepStmt = con.prepareStatement(deleteStatement);
		prepStmt.setInt(1, pkid.intValue());
		prepStmt.executeUpdate();
		prepStmt.close();
		Log.printAudit(strObjectName + "Deleted Object : " + pkid.toString());
		releaseConnection();
	}

	private void loadObject() throws SQLException
	{
		makeConnection();
		String selectStatement = "select pkid , category, " + " namespace, value1, "
				+ " value2, value3, value4, value5, binary_data, " + " status, userid_edit , "
				+ " time_edit, time_effective " + " from " + strObjectTable + " where pkid = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setInt(1, this.mPkid.intValue());
		ResultSet rs = prepStmt.executeQuery();
		if (rs.next())
		{
			this.mPkid = new Integer(rs.getInt("pkid"));
			this.mCategory = rs.getString("category");
			this.mNamespace = rs.getString("namespace");
			this.mValue1 = rs.getString("value1");
			this.mValue2 = rs.getString("value2");
			this.mValue3 = rs.getString("value3");
			this.mValue4 = rs.getString("value4");
			this.mValue5 = rs.getString("value5");
			this.mBinary = rs.getBytes("binary_data");
			this.mStatus = rs.getString("status");
			this.mUserIdEdit = new Integer(rs.getInt("userid_edit"));
			this.mTimeEdit = rs.getTimestamp("time_edit");
			this.mTimeEffective = rs.getTimestamp("time_effective");
			prepStmt.close();
		} else
		{
			prepStmt.close();
			throw new NoSuchEntityException("Row for this EJB Object" + " is not found in database.");
		}
		releaseConnection();
	}

	private Collection selectAllObjects() throws SQLException
	{
		ArrayList objectSet = new ArrayList();
		makeConnection();
		String selectStatement = " select pkid from " + strObjectTable + "  ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		rs.beforeFirst();
		while (rs.next())
		{
			objectSet.add(new Integer(rs.getInt(1)));
		}
		prepStmt.close();
		releaseConnection();
		return objectSet;
	}

	private Collection selectObjectsGiven(String fieldName, String value) throws SQLException
	{
		Log.printVerbose(" criteria : " + fieldName + " " + value);
		ArrayList objectSet = new ArrayList();
		makeConnection();
		String selectStatement = " select pkid from " + strObjectTable + "  where " + fieldName + " = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, value);
		ResultSet rs = prepStmt.executeQuery();
		rs.beforeFirst();
		while (rs.next())
		{
			objectSet.add(new Integer(rs.getInt(1)));
		}
		prepStmt.close();
		releaseConnection();
		return objectSet;
	}

	private void storeObject() throws SQLException
	{
		makeConnection();
		String updateStatement = "update " + strObjectTable + " set pkid = ? , " + " category = ? , "
				+ " namespace = ? , " + " value1 = ? , " + " value2 = ? , " + " value3 = ? , " + " value4 = ? , "
				+ " value5 = ? , " + " binary_data = ? , " + " status = ? , " + " userid_edit = ? , "
				+ " time_edit = ? , " + " time_effective = ?  " + " where pkid = ?;";
		PreparedStatement prepStmt = con.prepareStatement(updateStatement);
		prepStmt.setInt(1, this.mPkid.intValue());
		prepStmt.setString(2, this.mCategory);
		prepStmt.setString(3, this.mNamespace);
		prepStmt.setString(4, this.mValue1);
		prepStmt.setString(5, this.mValue2);
		prepStmt.setString(6, this.mValue3);
		prepStmt.setString(7, this.mValue4);
		prepStmt.setString(8, this.mValue5);
		prepStmt.setBytes(9, this.mBinary);
		prepStmt.setString(10, this.mStatus);
		prepStmt.setInt(11, this.mUserIdEdit.intValue());
		prepStmt.setTimestamp(12, this.mTimeEdit);
		prepStmt.setTimestamp(13, this.mTimeEffective);
		prepStmt.setInt(14, this.mPkid.intValue());
		int rowCount = prepStmt.executeUpdate();
		prepStmt.close();
		if (rowCount == 0)
		{
			throw new EJBException("Storing ejb object " + this.mPkid.toString() + " failed.");
		}
		releaseConnection();
	}
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
package com.vlee.ejb.application;

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

public class AppRegBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strObjectTable = "app_registry_index";
	protected final String strObjectName = "AppRegBean: ";
	private Connection con;
	private EntityContext mContext;
	protected final String strTimeBegin = "0000-01-01 00:00:00.000000000";
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String STATUS = "status";
	// Scheme
	public static final String CAT_IMAGE_URL = "image_url";
	public static final String NS_SERVICE_CTR = "service_center";
	public static final String BIZTYPE_AUTOSVC = "autoService";
	public static final String BIZTYPE_TRADING = "trading";
	public static final String TOPBAR_LOGO_DEFAULT = "templates/default/images/wavelet_emp.gif";
	// members ----------------------------------------------
	private Integer mPkid; // primary key !!!
	private String mCategory;
	private String mNamespace;
	private String mValue1;
	private String mValue2;
	private String mValue3;
	private String mValue4;
	private String mValue5;
	private byte[] mBinary;
	private String mStatus;
	private Integer mUserIdEdit;
	private Timestamp mTimeEdit;
	private Timestamp mTimeEffective;

	public Integer getPkid()
	{
		return this.mPkid;
	}

	public void setPkid(Integer pkid)
	{
		this.mPkid = pkid;
	}

	public String getCategory()
	{
		return this.mCategory;
	}

	public void setCategory(String strCategory)
	{
		this.mCategory = strCategory;
	}

	public String getNamespace()
	{
		return this.mNamespace;
	}

	public void setNamespace(String strNamespace)
	{
		this.mNamespace = strNamespace;
	}

	public String getValue1()
	{
		return this.mValue1;
	}

	public void setValue1(String strValue1)
	{
		this.mValue1 = strValue1;
	}

	public String getValue2()
	{
		return this.mValue2;
	}

	public void setValue2(String strValue2)
	{
		this.mValue2 = strValue2;
	}

	public String getValue3()
	{
		return this.mValue3;
	}

	public void setValue3(String strValue3)
	{
		this.mValue3 = strValue3;
	}

	public String getValue4()
	{
		return this.mValue4;
	}

	public void setValue4(String strValue4)
	{
		this.mValue4 = strValue4;
	}

	public String getValue5()
	{
		return this.mValue5;
	}

	public void setValue5(String strValue5)
	{
		this.mValue5 = strValue5;
	}

	public byte[] getBinary()
	{
		return this.mBinary;
	}

	public void setBinary(byte[] lBinary)
	{
		this.mBinary = lBinary;
	}

	public String getStatus()
	{
		return this.mStatus;
	}

	public void setStatus(String stts)
	{
		this.mStatus = stts;
	}

	public Integer getUserIdEdit()
	{
		return this.mUserIdEdit;
	}

	public void setUserIdEdit(Integer intUserId)
	{
		this.mUserIdEdit = intUserId;
	}

	public Timestamp getTimeEdit()
	{
		return this.mTimeEdit;
	}

	public void setTimeEdit(Timestamp tsTime)
	{
		this.mTimeEdit = tsTime;
	}

	public Timestamp getTimeEffective()
	{
		return this.mTimeEffective;
	}

	public void setTimeEffective(Timestamp tsTime)
	{
		this.mTimeEffective = tsTime;
	}

	public Integer getPrimaryKey()
	{
		return this.mPkid;
	}

	public void setPrimaryKey(Integer regid)
	{
		this.mPkid = regid;
	}

	public Integer ejbCreate(String strCategory, String strNamespace, String strValue1, String strValue2,
			String strValue3, String strValue4, String strValue5, Integer useridEdit, Timestamp tsEdit,
			Timestamp tsEffective) throws CreateException
	{
		Integer pkid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			pkid = insertNewRow(strCategory, strNamespace, strValue1, strValue2, strValue3, strValue4, strValue5,
					useridEdit, tsEdit, tsEffective);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (pkid != null)
		{
			this.mPkid = new Integer(pkid.intValue()); // primary key!!!
			this.mCategory = strCategory;
			this.mNamespace = strNamespace;
			this.mValue1 = strValue1;
			this.mValue2 = strValue2;
			this.mValue3 = strValue3;
			this.mValue4 = strValue4;
			this.mValue5 = strValue5;
			this.mUserIdEdit = useridEdit;
			this.mStatus = AppRegBean.ACTIVE;
			this.mTimeEdit = tsEdit;
			this.mTimeEffective = Timestamp.valueOf(strTimeBegin);
			ejbStore();
		}
		Log.printVerbose(strObjectName + " leaving ejbCreate ");
		return pkid;
	}

	public Integer ejbFindByPrimaryKey(Integer pkid) throws FinderException
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
			deleteObject(this.mPkid);
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
		this.mPkid = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.mPkid = null;
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

	public void ejbPostCreate(String strCategory, String strNamespace, String strValue1, String strValue2,
			String strValue3, String strValue4, String strValue5, Integer useridEdit, Timestamp tsEdit,
			Timestamp tsEffective)
	{
	}

	public Collection ejbFindAllObjects() throws FinderException
	{
		try
		{
			Collection bufAL = selectAllObjects();
			return bufAL;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug(strObjectName + "ejbFindAllObjects: " + ex);
			return null;
		}
	}

	public Collection ejbFindObjectsGiven(String fieldName, String value) throws FinderException
	{
		try
		{
			Collection bufAL = selectObjectsGiven(fieldName, value);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindObjectsGiven: " + ex.getMessage());
			return null;
		}
	}

	public Collection ejbFindEJBObjects(String strCat, String strNamespace, String strValue1, String strValue2,
			String strValue3, String strValue4, String strValue5)
	{
		Log.printVerbose("In ejbFindObjectsGiven()");
		try
		{
			return getEJBObjectsGiven(strCat, strNamespace, strValue1, strValue2, strValue3, strValue4, strValue5);
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbHomeGetActiveValObjects: " + ex.getMessage());
			return null;
		}
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
	private Collection getValObjectsGiven(String strCat, String strNamespace, String strValue1, String strValue2,
			String strOptions) throws SQLException
	{
		// warning.. this function is not fully tested yet
		makeConnection();
		Log.printVerbose("in getValObjectsGiven()");
		Collection colObj = new Vector();
		String selectStatement = "select " + " pkid , " + " category , " + " namespace , " + " value1 , "
				+ " value2 , " + " value3 , " + " value4 , " + " value5 , " + " binary_data , " + " status , "
				+ " userid_edit , " + " time_edit , " + " time_effective " + " from " + strObjectTable
				+ " where  category = ? " + " and namespace = ? " + " and value1 = ? " + " and value2 = ? "
				+ " order by pkid ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, strCat);
		prepStmt.setString(2, strNamespace);
		prepStmt.setString(3, strValue1);
		prepStmt.setString(4, strValue2);
		ResultSet rs = prepStmt.executeQuery();
		//rs.beforeTheFirstRecord();
		while (rs.next())
		{
			AppRegObject laro = new AppRegObject();
			laro.mPkid = new Integer(rs.getInt("pkid"));
			laro.mCategory = rs.getString("category");
			laro.mNamespace = rs.getString("namespace");
			laro.mValue1 = rs.getString("value1");
			laro.mValue2 = rs.getString("value2");
			laro.mValue3 = rs.getString("value3");
			laro.mValue4 = rs.getString("value4");
			laro.mValue5 = rs.getString("value5");
			laro.mBinary = rs.getBytes("binary_data");
			laro.mStatus = rs.getString("status");
			laro.mUserIdEdit = new Integer(rs.getInt("userid_edit"));
			laro.mTimeEdit = rs.getTimestamp("time_edit");
			laro.mTimeEffective = rs.getTimestamp("time_effective");
			Log.printVerbose(laro.toString());
			colObj.add(laro);
		}
		prepStmt.close();
		releaseConnection();
		return colObj;
	}

	// ////////////////////////////////////////////////////////
	private Collection getEJBObjectsGiven(String strCat, String strNamespace, String strValue1, String strValue2,
			String strValue3, String strValue4, String strValue5) throws SQLException
	{
		makeConnection();
		ArrayList objectSet = new ArrayList();
		String selectStatement = " select pkid from " + strObjectTable + " where  category = ? "
				+ " and namespace = ? ";
		if (strValue1 != null)
		{
			selectStatement += " and value1 like '" + strValue1 + "' ";
		}
		if (strValue2 != null)
		{
			selectStatement += " and value2 like '" + strValue2 + "' ";
		}
		if (strValue3 != null)
		{
			selectStatement += " and value3 like '" + strValue3 + "' ";
		}
		if (strValue4 != null)
		{
			selectStatement += " and value4 like '" + strValue4 + "' ";
		}
		if (strValue5 != null)
		{
			selectStatement += " and value5 like '" + strValue5 + "' ";
		}
		selectStatement += " order by pkid ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, strCat);
		prepStmt.setString(2, strNamespace);
		// prepStmt.setString(3,strValue1);
		// prepStmt.setString(4,strValue2);
		ResultSet rs = prepStmt.executeQuery();
		//rs.beforeTheFirstRecord();
		while (rs.next())
		{
			objectSet.add(new Integer(rs.getInt(1)));
		}
		prepStmt.close();
		releaseConnection();
		return objectSet;
	}

	private Integer insertNewRow(String strCategory, String strNamespace, String strValue1, String strValue2,
			String strValue3, String strValue4, String strValue5, Integer useridEdit, Timestamp tsEdit,
			Timestamp tsEffective) throws SQLException
	{
		String strBinary = new String("null");
		this.mBinary = strBinary.getBytes();
		makeConnection();
		Log.printVerbose(strObjectName + "insertNewRow: ");
		String findMaxPkidStmt = " select pkid from " + strObjectTable + " ";
		PreparedStatement prepStmt = con.prepareStatement(findMaxPkidStmt);
		ResultSet rs = prepStmt.executeQuery();
		int bufInt = 0;
		while (rs.next())
		{
			if (bufInt < rs.getInt("pkid"))
			{
				bufInt = rs.getInt("pkid");
			}
		}
		Integer newPkid = new Integer(bufInt + 1); // new Integer(rs.getInt(1)
													// + 1);
		if (newPkid.intValue() < 1000)
			newPkid = new Integer("1000");
		Log.printVerbose("The new objectid is :" + newPkid.toString());
		String insertStatement = "insert into " + strObjectTable + " (pkid, category,   " + " namespace , value1, "
				+ " value2, value3, value4, value5, binary_data ," + " status, userid_edit, "
				+ " time_edit , time_effective )  " + " values ( ?, ?, ? , ?, ?, ?, ?, ?, ?, ?, " + " ?, ?, ? )";
		prepStmt = con.prepareStatement(insertStatement);
		int lZero = 0;
		prepStmt.setInt(1, newPkid.intValue());
		prepStmt.setString(2, strCategory);
		prepStmt.setString(3, strNamespace);
		prepStmt.setString(4, strValue1);
		prepStmt.setString(5, strValue1);
		prepStmt.setString(6, strValue1);
		prepStmt.setString(7, strValue1);
		prepStmt.setString(8, strValue1);
		prepStmt.setBytes(9, this.mBinary);
		prepStmt.setString(10, AppRegBean.ACTIVE);
		prepStmt.setInt(11, useridEdit.intValue());
		prepStmt.setTimestamp(12, tsEdit);
		prepStmt.setTimestamp(13, tsEffective);
		prepStmt.executeUpdate();
		prepStmt.close();
		Log.printAudit(strObjectName + "Created New Row:" + newPkid.toString());
		Log.printVerbose(strObjectName + "leaving insertNewRow");
		releaseConnection();
		return newPkid;
	}

	// stop here
	private boolean selectByPrimaryKey(Integer pkid) throws SQLException
	{
		makeConnection();
		String selectStatement = "select * " + "from " + strObjectTable + " where pkid = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setInt(1, pkid.intValue());
		ResultSet rs = prepStmt.executeQuery();
		boolean result = false;
		result = rs.next();
		prepStmt.close();
		releaseConnection();
		return result;
	}

	private void deleteObject(Integer pkid) throws SQLException
	{
		makeConnection();
		String deleteStatement = "delete from " + strObjectTable + "  " + "where pkid = ?";
		PreparedStatement prepStmt = con.prepareStatement(deleteStatement);
		prepStmt.setInt(1, pkid.intValue());
		prepStmt.executeUpdate();
		prepStmt.close();
		Log.printAudit(strObjectName + "Deleted Object : " + pkid.toString());
		releaseConnection();
	}

	private void loadObject() throws SQLException
	{
		makeConnection();
		String selectStatement = "select pkid , category, " + " namespace, value1, "
				+ " value2, value3, value4, value5, binary_data, " + " status, userid_edit , "
				+ " time_edit, time_effective " + " from " + strObjectTable + " where pkid = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setInt(1, this.mPkid.intValue());
		ResultSet rs = prepStmt.executeQuery();
		if (rs.next())
		{
			this.mPkid = new Integer(rs.getInt("pkid"));
			this.mCategory = rs.getString("category");
			this.mNamespace = rs.getString("namespace");
			this.mValue1 = rs.getString("value1");
			this.mValue2 = rs.getString("value2");
			this.mValue3 = rs.getString("value3");
			this.mValue4 = rs.getString("value4");
			this.mValue5 = rs.getString("value5");
			this.mBinary = rs.getBytes("binary_data");
			this.mStatus = rs.getString("status");
			this.mUserIdEdit = new Integer(rs.getInt("userid_edit"));
			this.mTimeEdit = rs.getTimestamp("time_edit");
			this.mTimeEffective = rs.getTimestamp("time_effective");
			prepStmt.close();
		} else
		{
			prepStmt.close();
			throw new NoSuchEntityException("Row for this EJB Object" + " is not found in database.");
		}
		releaseConnection();
	}

	private Collection selectAllObjects() throws SQLException
	{
		ArrayList objectSet = new ArrayList();
		makeConnection();
		String selectStatement = " select pkid from " + strObjectTable + "  ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		//rs.beforeTheFirstRecord();
		while (rs.next())
		{
			objectSet.add(new Integer(rs.getInt(1)));
		}
		prepStmt.close();
		releaseConnection();
		return objectSet;
	}

	private Collection selectObjectsGiven(String fieldName, String value) throws SQLException
	{
		Log.printVerbose(" criteria : " + fieldName + " " + value);
		ArrayList objectSet = new ArrayList();
		makeConnection();
		String selectStatement = " select pkid from " + strObjectTable + "  where " + fieldName + " = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setString(1, value);
		ResultSet rs = prepStmt.executeQuery();
		//rs.beforeTheFirstRecord();
		while (rs.next())
		{
			objectSet.add(new Integer(rs.getInt(1)));
		}
		prepStmt.close();
		releaseConnection();
		return objectSet;
	}

	private void storeObject() throws SQLException
	{
		makeConnection();
		String updateStatement = "update " + strObjectTable + " set pkid = ? , " + " category = ? , "
				+ " namespace = ? , " + " value1 = ? , " + " value2 = ? , " + " value3 = ? , " + " value4 = ? , "
				+ " value5 = ? , " + " binary_data = ? , " + " status = ? , " + " userid_edit = ? , "
				+ " time_edit = ? , " + " time_effective = ?  " + " where pkid = ?;";
		PreparedStatement prepStmt = con.prepareStatement(updateStatement);
		prepStmt.setInt(1, this.mPkid.intValue());
		prepStmt.setString(2, this.mCategory);
		prepStmt.setString(3, this.mNamespace);
		prepStmt.setString(4, this.mValue1);
		prepStmt.setString(5, this.mValue2);
		prepStmt.setString(6, this.mValue3);
		prepStmt.setString(7, this.mValue4);
		prepStmt.setString(8, this.mValue5);
		prepStmt.setBytes(9, this.mBinary);
		prepStmt.setString(10, this.mStatus);
		prepStmt.setInt(11, this.mUserIdEdit.intValue());
		prepStmt.setTimestamp(12, this.mTimeEdit);
		prepStmt.setTimestamp(13, this.mTimeEffective);
		prepStmt.setInt(14, this.mPkid.intValue());
		int rowCount = prepStmt.executeUpdate();
		prepStmt.close();
		if (rowCount == 0)
		{
			throw new EJBException("Storing ejb object " + this.mPkid.toString() + " failed.");
		}
		releaseConnection();
	}
}
