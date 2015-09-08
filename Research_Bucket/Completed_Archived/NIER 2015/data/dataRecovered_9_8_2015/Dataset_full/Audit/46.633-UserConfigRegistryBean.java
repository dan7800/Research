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

public class UserConfigRegistryBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strObjectTable = "user_config_registry";
	protected final String strObjectName = "UserConfigRegistryBean: ";
	private Connection con;
	private EntityContext mContext;
	protected final String strTimeBegin = "0000-01-01 00:00:00.000000000";
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String NODE_PARENT = "node_parent";
	public static final String USERID = "userid";
	public static final String CATEGORY = "category";
	public static final String NAMESPACE = "namespace";
	public static final String VALUE1 = "value1";
	public static final String VALUE2 = "value2";
	public static final String VALUE3 = "value3";
	public static final String VALUE4 = "value4";
	public static final String VALUE5 = "value5";
	public static final String STATUS = "status";
	public static final String USERID_EDIT = "userid_edit";
	public static final String TIME_EDIT = "time_edit";
	public static final String TIME_EFFECTIVE = "time_effective";
	// constants ----------------------------------------------
	public static final String CAT_DEFAULT = "DEFAULT";
	public static final String NS_CUSTSVCCTR = "CUSTSVCCTR";
	public static final String NS_PROCCTR = "PROCCTR";
	public static final String NS_PCCENTER = "PCCENTER";
	// members ----------------------------------------------
	private Integer mPKid; // primary key!!!
	private Integer mNodeParent;
	private Integer mUserId;
	private String mCategory;
	private String mNamespace;
	private String mValue1;
	private String mValue2;
	private String mValue3;
	private String mValue4;
	private String mValue5;
	private String mStatus;
	private Integer mUserIdEdit;
	private Timestamp mTimeEdit;
	private Timestamp mTimeEffective;

	public Integer getPKid()
	{
		return this.mPKid;
	}

	public void setPKid(Integer pkid)
	{
		this.mPKid = pkid;
	}

	public Integer getNodeParent()
	{
		return this.mNodeParent;
	}

	public void setNodeParent(Integer iNodeParent)
	{
		this.mNodeParent = iNodeParent;
	}

	public Integer getUserId()
	{
		return this.mUserId;
	}

	public void setUserId(Integer userid)
	{
		this.mUserId = userid;
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
		return this.mPKid;
	}

	public void setPrimaryKey(Integer jtid)
	{
		this.mPKid = jtid;
	}

	public Integer ejbCreate(Integer iNodeParent, Integer userid, String strCategory, String strNamespace,
			String strValue1, String strValue2, String strValue3, String strValue4, String strValue5,
			Integer useridEdit, Timestamp tsEdit, Timestamp tsEffective) throws CreateException
	{
		Integer pkid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			pkid = insertNewRow(iNodeParent, userid, strCategory, strNamespace, strValue1, strValue2, strValue3,
					strValue4, strValue5, useridEdit, tsEdit, tsEffective);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (pkid != null)
		{
			this.mPKid = new Integer(pkid.intValue()); // primary key!!!
			this.mNodeParent = iNodeParent;
			this.mUserId = userid;
			this.mCategory = strCategory;
			this.mNamespace = strNamespace;
			this.mValue1 = strValue1;
			this.mValue2 = strValue2;
			this.mValue3 = strValue3;
			this.mValue4 = strValue4;
			this.mValue5 = strValue5;
			this.mUserIdEdit = useridEdit;
			this.mStatus = UserConfigRegistryBean.ACTIVE;
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
			deleteObject(this.mPKid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
	}

	public void setEntityContext(EntityContext mContext)
	{
		Log.printVerbose(strObjectName + " in setEntityContext");
		this.mContext = mContext;
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
		this.mPKid = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.mPKid = null;
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

	public void ejbPostCreate(Integer iNodeParent, Integer userid, String strCategory, String strNamespace,
			String strValue1, String strValue2, String strValue3, String strValue4, String strValue5,
			Integer useridEdit, Timestamp tsEdit, Timestamp tsEffective)
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

	public Collection ejbFindObjectsGiven(String strConditions) throws FinderException
	{
		try
		{
			Collection bufAL = selectObjectsGiven(strConditions);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindObjectsGiven: " + ex.getMessage());
			return null;
		}
	}

	public Vector ejbHomeGetValueObjectsGiven(Integer node, Integer userId, String strCat, String strNS, String value1,
			String value2, String value3, String value4, String value5, String status, Integer userIdEdit,
			Timestamp tsEffFrom, Timestamp tsEffTo)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(node, userId, strCat, strNS, value1, value2, value3, value4, value5,
					status, userIdEdit, tsEffFrom, tsEffTo);
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
			if (!con.isClosed())
			{
				con.close();
			}
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw new EJBException("releaseConnection: " + ex.getMessage());
		}
	} // releaseConnection

	private Integer insertNewRow(Integer iNodeParent, Integer userid, String strCategory, String strNamespace,
			String strValue1, String strValue2, String strValue3, String strValue4, String strValue5,
			Integer useridEdit, Timestamp tsEdit, Timestamp tsEffective) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + "insertNewRow: ");
			String findMaxPKIdStmt = " select max(" + PKID + ") as max_pkid from " + strObjectTable + " ";
			selectStmt = con.prepareStatement(findMaxPKIdStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("max_pkid"))
				{
					bufInt = rs.getInt("max_pkid");
				}
				bufInt = (bufInt < 10000) ? 10000 : bufInt;
			}
			Integer newPkid = new Integer(bufInt + 1);
			// new Integer(rs.getInt(1) + 1);
			Log.printVerbose("The new objectid is :" + newPkid.toString());
			String insertStatement = "insert into " + strObjectTable + " (pkid, node_parent, userid, category,   "
					+ " namespace , value1, " + " value2 , value3, " + " value4 , value5, " + " status, userid_edit, "
					+ " time_edit , time_effective)  " + " values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," + " ?, ?, ?, ? )";
			if (strValue1 == null)
				strValue1 = new String(" ");
			if (strValue2 == null)
				strValue2 = new String(" ");
			if (strValue3 == null)
				strValue3 = new String(" ");
			if (strValue4 == null)
				strValue4 = new String(" ");
			if (strValue5 == null)
				strValue5 = new String(" ");
			// Log.printVerbose("checkpoint 1:ok");
			insertStmt = con.prepareStatement(insertStatement);
			int lZero = 0;
			// Log.printVerbose("checkpoint 2:ok");
			insertStmt.setInt(1, newPkid.intValue());
			// Log.printVerbose("checkpoint 3:ok");
			insertStmt.setInt(2, iNodeParent.intValue());
			// Log.printVerbose("checkpoint 4:ok");
			// Log.printVerbose(" userid is " + userid.toString());
			insertStmt.setInt(3, userid.intValue());
			// Log.printVerbose("checkpoint 5:ok");
			insertStmt.setString(4, strCategory);
			// Log.printVerbose("checkpoint 6:ok");
			insertStmt.setString(5, strNamespace);
			// Log.printVerbose("checkpoint 7:ok");
			insertStmt.setString(6, strValue1);
			// Log.printVerbose("checkpoint 8:ok");
			insertStmt.setString(7, strValue2);
			// Log.printVerbose("checkpoint 9:ok");
			insertStmt.setString(8, strValue3);
			// Log.printVerbose("checkpoint 10:ok");
			insertStmt.setString(9, strValue4);
			// Log.printVerbose("checkpoint 11:ok");
			insertStmt.setString(10, strValue5);
			// Log.printVerbose("checkpoint 12:ok");
			insertStmt.setString(11, UserConfigRegistryBean.ACTIVE);
			// Log.printVerbose("checkpoint 13:ok");
			insertStmt.setInt(12, useridEdit.intValue());
			// Log.printVerbose("checkpoint 14:ok");
			insertStmt.setTimestamp(13, tsEdit);
			// Log.printVerbose("checkpoint 15:ok");
			insertStmt.setTimestamp(14, tsEffective);
			insertStmt.executeUpdate();
			// Log.printVerbose("checkpoint 16:ok");
			// insertStmt.close();;
			Log.printAudit(strObjectName + "Created New Row:" + newPkid.toString());
			Log.printVerbose(strObjectName + "leaving insertNewRow");
			// Log.printVerbose("checkpoint 18:ok");
			// releaseConnection();
			return newPkid;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (selectStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				selectStmt.close();
			}
			if (insertStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				insertStmt.close();
			}
			releaseConnection();
		}
	}

	// stop here
	private boolean selectByPrimaryKey(Integer pkid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select * " + "from " + strObjectTable + " where pkid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = false;
			result = rs.next();
			// prepStmt.close();;
			// releaseConnection();
			return result;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private void deleteObject(Integer pkid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String deleteStatement = "delete from " + strObjectTable + "  " + "where pkid = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setInt(1, pkid.intValue());
			prepStmt.executeUpdate();
			// prepStmt.close();;
			Log.printAudit(strObjectName + "Deleted Object : " + pkid.toString());
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	// /////////////////////////////////////////////////////////////
	private Vector selectValueObjectsGiven(Integer node, Integer userId, String strCat, String strNS, String value1,
			String value2, String value3, String value4, String value5, String status, Integer userIdEdit,
			Timestamp tsEffFrom, Timestamp tsEffTo) throws SQLException
	{
		Vector vecValObj = new Vector();
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStmt = "select pkid , node_parent, userid, category, " + " namespace, value1, "
					+ " value2, value3, value4, value5, " + " status, userid_edit, " + " time_edit, time_effective "
					+ " from " + strObjectTable + " where status != '*' ";
			if (node != null)
			{
				selectStmt += " AND " + NODE_PARENT + "='" + node.toString() + "' ";
			}
			if (userId != null)
			{
				selectStmt += " AND " + USERID + "='" + userId.toString() + "' ";
			}
			if (strCat != null)
			{
				selectStmt += " AND " + CATEGORY + "='" + strCat + "' ";
			}
			if (strNS != null)
			{
				selectStmt += " AND " + NAMESPACE + "='" + strNS + "' ";
			}
			if (value1 != null)
			{
				selectStmt += " AND " + VALUE1 + "='" + value1 + "' ";
			}
			if (value2 != null)
			{
				selectStmt += " AND " + VALUE2 + "='" + value2 + "' ";
			}
			if (value3 != null)
			{
				selectStmt += " AND " + VALUE3 + "='" + value3 + "' ";
			}
			if (value4 != null)
			{
				selectStmt += " AND " + VALUE4 + "='" + value4 + "' ";
			}
			if (value5 != null)
			{
				selectStmt += " AND " + VALUE5 + "='" + value5 + "' ";
			}
			if (status != null)
			{
				selectStmt += " AND " + STATUS + "='" + status + "' ";
			}
			if (userIdEdit != null)
			{
				selectStmt += " AND " + USERID_EDIT + "='" + userIdEdit.toString() + "' ";
			}
			if (tsEffFrom != null)
			{
				selectStmt += " AND " + TIME_EFFECTIVE + ">='" + TimeFormat.strDisplayDate(tsEffFrom) + "' ";
			}
			if (tsEffTo != null)
			{
				selectStmt += " AND " + TIME_EFFECTIVE + "<'" + TimeFormat.strDisplayDate(tsEffTo) + "' ";
			}
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			while (rs.next())
			{
				UserConfigRegistryObject ucrObj = new UserConfigRegistryObject();
				ucrObj.mPKid = new Integer(rs.getInt("pkid"));
				ucrObj.mNodeParent = new Integer(rs.getInt("node_parent"));
				ucrObj.mUserId = new Integer(rs.getInt("userid"));
				ucrObj.mCategory = rs.getString("category");
				ucrObj.mNamespace = rs.getString("namespace");
				ucrObj.mValue1 = rs.getString("value1");
				ucrObj.mValue2 = rs.getString("value2");
				ucrObj.mValue3 = rs.getString("value3");
				ucrObj.mValue4 = rs.getString("value4");
				ucrObj.mValue5 = rs.getString("value5");
				ucrObj.mStatus = rs.getString("status");
				ucrObj.mUserIdEdit = new Integer(rs.getInt("userid_edit"));
				ucrObj.mTimeEdit = rs.getTimestamp("time_edit");
				ucrObj.mTimeEffective = rs.getTimestamp("time_effective");
				vecValObj.add(ucrObj);
			}
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
		return vecValObj;
	}

	// /////////////////////////////////////////////////////////////
	private void loadObject() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select pkid , node_parent, userid, category, " + " namespace, value1, "
					+ " value2, value3, value4, value5, " + " status, userid_edit, " + " time_edit, time_effective "
					+ " from " + strObjectTable + " where pkid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.mPKid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.mPKid = new Integer(rs.getInt("pkid"));
				this.mNodeParent = new Integer(rs.getInt("node_parent"));
				this.mUserId = new Integer(rs.getInt("userid"));
				this.mCategory = rs.getString("category");
				this.mNamespace = rs.getString("namespace");
				this.mValue1 = rs.getString("value1");
				this.mValue2 = rs.getString("value2");
				this.mValue3 = rs.getString("value3");
				this.mValue4 = rs.getString("value4");
				this.mValue5 = rs.getString("value5");
				this.mStatus = rs.getString("status");
				this.mUserIdEdit = new Integer(rs.getInt("userid_edit"));
				this.mTimeEdit = rs.getTimestamp("time_edit");
				this.mTimeEffective = rs.getTimestamp("time_effective");
				// prepStmt.close();;
			} else
			{
				// prepStmt.close();;
				throw new NoSuchEntityException("Row for this EJB Object" + " is not found in database.");
			}
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private Collection selectAllObjects() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			makeConnection();
			String selectStatement = " select pkid from " + strObjectTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();;
			// releaseConnection();
			return objectSet;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private Collection selectObjectsGiven(String fieldName, String value) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + value);
			ArrayList objectSet = new ArrayList();
			makeConnection();
			String selectStatement = " select pkid from " + strObjectTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt("pkid")));
			}
			// prepStmt.close();;
			// releaseConnection();
			return objectSet;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private Collection selectObjectsGiven(String strConditions) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" conditions: " + strConditions);
			ArrayList objectSet = new ArrayList();
			makeConnection();
			String selectStatement = " select pkid from " + strObjectTable + "  where " + strConditions;
			prepStmt = con.prepareStatement(selectStatement);
			// prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt("pkid")));
			}
			// prepStmt.close();;
			// releaseConnection();
			return objectSet;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private void storeObject() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String updateStatement = "update " + strObjectTable + " set pkid = ? , " + " node_parent = ?, "
					+ " userid =  ? , " + " category = ? , " + " namespace = ? , " + " value1 = ? , "
					+ " value2 = ? , " + " value3 = ? , " + " value4 = ? , " + " value5 = ? , " + " status = ? , "
					+ " userid_edit = ? , " + " time_edit = ? , " + " time_effective = ?  " + " where pkid = ?;";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, this.mPKid.intValue());
			prepStmt.setInt(2, this.mNodeParent.intValue());
			prepStmt.setInt(3, this.mUserId.intValue());
			prepStmt.setString(4, this.mCategory);
			prepStmt.setString(5, this.mNamespace);
			prepStmt.setString(6, this.mValue1);
			prepStmt.setString(7, this.mValue2);
			prepStmt.setString(8, this.mValue3);
			prepStmt.setString(9, this.mValue4);
			prepStmt.setString(10, this.mValue5);
			prepStmt.setString(11, this.mStatus);
			prepStmt.setInt(12, this.mUserIdEdit.intValue());
			prepStmt.setTimestamp(13, this.mTimeEdit);
			prepStmt.setTimestamp(14, this.mTimeEffective);
			prepStmt.setInt(15, this.mPKid.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();;
			if (rowCount == 0)
			{
				throw new EJBException("Storing ejb object " + this.mPKid.toString() + " failed.");
			}
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}
} // UserConfigRegistryBean
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

public class UserConfigRegistryBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strObjectTable = "user_config_registry";
	protected final String strObjectName = "UserConfigRegistryBean: ";
	private Connection con;
	private EntityContext mContext;
	protected final String strTimeBegin = "0000-01-01 00:00:00.000000000";
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String NODE_PARENT = "node_parent";
	public static final String USERID = "userid";
	public static final String CATEGORY = "category";
	public static final String NAMESPACE = "namespace";
	public static final String VALUE1 = "value1";
	public static final String VALUE2 = "value2";
	public static final String VALUE3 = "value3";
	public static final String VALUE4 = "value4";
	public static final String VALUE5 = "value5";
	public static final String STATUS = "status";
	public static final String USERID_EDIT = "userid_edit";
	public static final String TIME_EDIT = "time_edit";
	public static final String TIME_EFFECTIVE = "time_effective";
	// constants ----------------------------------------------
	public static final String CAT_DEFAULT = "DEFAULT";
	public static final String NS_CUSTSVCCTR = "CUSTSVCCTR";
	public static final String NS_PROCCTR = "PROCCTR";
	public static final String NS_PCCENTER = "PCCENTER";
	// members ----------------------------------------------
	private Integer mPKid; // primary key!!!
	private Integer mNodeParent;
	private Integer mUserId;
	private String mCategory;
	private String mNamespace;
	private String mValue1;
	private String mValue2;
	private String mValue3;
	private String mValue4;
	private String mValue5;
	private String mStatus;
	private Integer mUserIdEdit;
	private Timestamp mTimeEdit;
	private Timestamp mTimeEffective;

	public Integer getPKid()
	{
		return this.mPKid;
	}

	public void setPKid(Integer pkid)
	{
		this.mPKid = pkid;
	}

	public Integer getNodeParent()
	{
		return this.mNodeParent;
	}

	public void setNodeParent(Integer iNodeParent)
	{
		this.mNodeParent = iNodeParent;
	}

	public Integer getUserId()
	{
		return this.mUserId;
	}

	public void setUserId(Integer userid)
	{
		this.mUserId = userid;
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
		return this.mPKid;
	}

	public void setPrimaryKey(Integer jtid)
	{
		this.mPKid = jtid;
	}

	public Integer ejbCreate(Integer iNodeParent, Integer userid, String strCategory, String strNamespace,
			String strValue1, String strValue2, String strValue3, String strValue4, String strValue5,
			Integer useridEdit, Timestamp tsEdit, Timestamp tsEffective) throws CreateException
	{
		Integer pkid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			pkid = insertNewRow(iNodeParent, userid, strCategory, strNamespace, strValue1, strValue2, strValue3,
					strValue4, strValue5, useridEdit, tsEdit, tsEffective);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (pkid != null)
		{
			this.mPKid = new Integer(pkid.intValue()); // primary key!!!
			this.mNodeParent = iNodeParent;
			this.mUserId = userid;
			this.mCategory = strCategory;
			this.mNamespace = strNamespace;
			this.mValue1 = strValue1;
			this.mValue2 = strValue2;
			this.mValue3 = strValue3;
			this.mValue4 = strValue4;
			this.mValue5 = strValue5;
			this.mUserIdEdit = useridEdit;
			this.mStatus = UserConfigRegistryBean.ACTIVE;
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
			deleteObject(this.mPKid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
	}

	public void setEntityContext(EntityContext mContext)
	{
		Log.printVerbose(strObjectName + " in setEntityContext");
		this.mContext = mContext;
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
		this.mPKid = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.mPKid = null;
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

	public void ejbPostCreate(Integer iNodeParent, Integer userid, String strCategory, String strNamespace,
			String strValue1, String strValue2, String strValue3, String strValue4, String strValue5,
			Integer useridEdit, Timestamp tsEdit, Timestamp tsEffective)
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

	public Collection ejbFindObjectsGiven(String strConditions) throws FinderException
	{
		try
		{
			Collection bufAL = selectObjectsGiven(strConditions);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindObjectsGiven: " + ex.getMessage());
			return null;
		}
	}

	public Vector ejbHomeGetValueObjectsGiven(Integer node, Integer userId, String strCat, String strNS, String value1,
			String value2, String value3, String value4, String value5, String status, Integer userIdEdit,
			Timestamp tsEffFrom, Timestamp tsEffTo)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(node, userId, strCat, strNS, value1, value2, value3, value4, value5,
					status, userIdEdit, tsEffFrom, tsEffTo);
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
			if (!con.isClosed())
			{
				con.close();
			}
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw new EJBException("releaseConnection: " + ex.getMessage());
		}
	} // releaseConnection

	private Integer insertNewRow(Integer iNodeParent, Integer userid, String strCategory, String strNamespace,
			String strValue1, String strValue2, String strValue3, String strValue4, String strValue5,
			Integer useridEdit, Timestamp tsEdit, Timestamp tsEffective) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + "insertNewRow: ");
			String findMaxPKIdStmt = " select max(" + PKID + ") as max_pkid from " + strObjectTable + " ";
			selectStmt = con.prepareStatement(findMaxPKIdStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("max_pkid"))
				{
					bufInt = rs.getInt("max_pkid");
				}
				bufInt = (bufInt < 10000) ? 10000 : bufInt;
			}
			Integer newPkid = new Integer(bufInt + 1);
			// new Integer(rs.getInt(1) + 1);
			Log.printVerbose("The new objectid is :" + newPkid.toString());
			String insertStatement = "insert into " + strObjectTable + " (pkid, node_parent, userid, category,   "
					+ " namespace , value1, " + " value2 , value3, " + " value4 , value5, " + " status, userid_edit, "
					+ " time_edit , time_effective)  " + " values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," + " ?, ?, ?, ? )";
			if (strValue1 == null)
				strValue1 = new String(" ");
			if (strValue2 == null)
				strValue2 = new String(" ");
			if (strValue3 == null)
				strValue3 = new String(" ");
			if (strValue4 == null)
				strValue4 = new String(" ");
			if (strValue5 == null)
				strValue5 = new String(" ");
			// Log.printVerbose("checkpoint 1:ok");
			insertStmt = con.prepareStatement(insertStatement);
			int lZero = 0;
			// Log.printVerbose("checkpoint 2:ok");
			insertStmt.setInt(1, newPkid.intValue());
			// Log.printVerbose("checkpoint 3:ok");
			insertStmt.setInt(2, iNodeParent.intValue());
			// Log.printVerbose("checkpoint 4:ok");
			// Log.printVerbose(" userid is " + userid.toString());
			insertStmt.setInt(3, userid.intValue());
			// Log.printVerbose("checkpoint 5:ok");
			insertStmt.setString(4, strCategory);
			// Log.printVerbose("checkpoint 6:ok");
			insertStmt.setString(5, strNamespace);
			// Log.printVerbose("checkpoint 7:ok");
			insertStmt.setString(6, strValue1);
			// Log.printVerbose("checkpoint 8:ok");
			insertStmt.setString(7, strValue2);
			// Log.printVerbose("checkpoint 9:ok");
			insertStmt.setString(8, strValue3);
			// Log.printVerbose("checkpoint 10:ok");
			insertStmt.setString(9, strValue4);
			// Log.printVerbose("checkpoint 11:ok");
			insertStmt.setString(10, strValue5);
			// Log.printVerbose("checkpoint 12:ok");
			insertStmt.setString(11, UserConfigRegistryBean.ACTIVE);
			// Log.printVerbose("checkpoint 13:ok");
			insertStmt.setInt(12, useridEdit.intValue());
			// Log.printVerbose("checkpoint 14:ok");
			insertStmt.setTimestamp(13, tsEdit);
			// Log.printVerbose("checkpoint 15:ok");
			insertStmt.setTimestamp(14, tsEffective);
			insertStmt.executeUpdate();
			// Log.printVerbose("checkpoint 16:ok");
			// insertStmt.close();;
			Log.printAudit(strObjectName + "Created New Row:" + newPkid.toString());
			Log.printVerbose(strObjectName + "leaving insertNewRow");
			// Log.printVerbose("checkpoint 18:ok");
			// releaseConnection();
			return newPkid;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (selectStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				selectStmt.close();
			}
			if (insertStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				insertStmt.close();
			}
			releaseConnection();
		}
	}

	// stop here
	private boolean selectByPrimaryKey(Integer pkid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select * " + "from " + strObjectTable + " where pkid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = false;
			result = rs.next();
			// prepStmt.close();;
			// releaseConnection();
			return result;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private void deleteObject(Integer pkid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String deleteStatement = "delete from " + strObjectTable + "  " + "where pkid = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setInt(1, pkid.intValue());
			prepStmt.executeUpdate();
			// prepStmt.close();;
			Log.printAudit(strObjectName + "Deleted Object : " + pkid.toString());
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	// /////////////////////////////////////////////////////////////
	private Vector selectValueObjectsGiven(Integer node, Integer userId, String strCat, String strNS, String value1,
			String value2, String value3, String value4, String value5, String status, Integer userIdEdit,
			Timestamp tsEffFrom, Timestamp tsEffTo) throws SQLException
	{
		Vector vecValObj = new Vector();
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStmt = "select pkid , node_parent, userid, category, " + " namespace, value1, "
					+ " value2, value3, value4, value5, " + " status, userid_edit, " + " time_edit, time_effective "
					+ " from " + strObjectTable + " where status != '*' ";
			if (node != null)
			{
				selectStmt += " AND " + NODE_PARENT + "='" + node.toString() + "' ";
			}
			if (userId != null)
			{
				selectStmt += " AND " + USERID + "='" + userId.toString() + "' ";
			}
			if (strCat != null)
			{
				selectStmt += " AND " + CATEGORY + "='" + strCat + "' ";
			}
			if (strNS != null)
			{
				selectStmt += " AND " + NAMESPACE + "='" + strNS + "' ";
			}
			if (value1 != null)
			{
				selectStmt += " AND " + VALUE1 + "='" + value1 + "' ";
			}
			if (value2 != null)
			{
				selectStmt += " AND " + VALUE2 + "='" + value2 + "' ";
			}
			if (value3 != null)
			{
				selectStmt += " AND " + VALUE3 + "='" + value3 + "' ";
			}
			if (value4 != null)
			{
				selectStmt += " AND " + VALUE4 + "='" + value4 + "' ";
			}
			if (value5 != null)
			{
				selectStmt += " AND " + VALUE5 + "='" + value5 + "' ";
			}
			if (status != null)
			{
				selectStmt += " AND " + STATUS + "='" + status + "' ";
			}
			if (userIdEdit != null)
			{
				selectStmt += " AND " + USERID_EDIT + "='" + userIdEdit.toString() + "' ";
			}
			if (tsEffFrom != null)
			{
				selectStmt += " AND " + TIME_EFFECTIVE + ">='" + TimeFormat.strDisplayDate(tsEffFrom) + "' ";
			}
			if (tsEffTo != null)
			{
				selectStmt += " AND " + TIME_EFFECTIVE + "<'" + TimeFormat.strDisplayDate(tsEffTo) + "' ";
			}
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			while (rs.next())
			{
				UserConfigRegistryObject ucrObj = new UserConfigRegistryObject();
				ucrObj.mPKid = new Integer(rs.getInt("pkid"));
				ucrObj.mNodeParent = new Integer(rs.getInt("node_parent"));
				ucrObj.mUserId = new Integer(rs.getInt("userid"));
				ucrObj.mCategory = rs.getString("category");
				ucrObj.mNamespace = rs.getString("namespace");
				ucrObj.mValue1 = rs.getString("value1");
				ucrObj.mValue2 = rs.getString("value2");
				ucrObj.mValue3 = rs.getString("value3");
				ucrObj.mValue4 = rs.getString("value4");
				ucrObj.mValue5 = rs.getString("value5");
				ucrObj.mStatus = rs.getString("status");
				ucrObj.mUserIdEdit = new Integer(rs.getInt("userid_edit"));
				ucrObj.mTimeEdit = rs.getTimestamp("time_edit");
				ucrObj.mTimeEffective = rs.getTimestamp("time_effective");
				vecValObj.add(ucrObj);
			}
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
		return vecValObj;
	}

	// /////////////////////////////////////////////////////////////
	private void loadObject() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select pkid , node_parent, userid, category, " + " namespace, value1, "
					+ " value2, value3, value4, value5, " + " status, userid_edit, " + " time_edit, time_effective "
					+ " from " + strObjectTable + " where pkid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.mPKid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.mPKid = new Integer(rs.getInt("pkid"));
				this.mNodeParent = new Integer(rs.getInt("node_parent"));
				this.mUserId = new Integer(rs.getInt("userid"));
				this.mCategory = rs.getString("category");
				this.mNamespace = rs.getString("namespace");
				this.mValue1 = rs.getString("value1");
				this.mValue2 = rs.getString("value2");
				this.mValue3 = rs.getString("value3");
				this.mValue4 = rs.getString("value4");
				this.mValue5 = rs.getString("value5");
				this.mStatus = rs.getString("status");
				this.mUserIdEdit = new Integer(rs.getInt("userid_edit"));
				this.mTimeEdit = rs.getTimestamp("time_edit");
				this.mTimeEffective = rs.getTimestamp("time_effective");
				// prepStmt.close();;
			} else
			{
				// prepStmt.close();;
				throw new NoSuchEntityException("Row for this EJB Object" + " is not found in database.");
			}
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private Collection selectAllObjects() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			makeConnection();
			String selectStatement = " select pkid from " + strObjectTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();;
			// releaseConnection();
			return objectSet;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private Collection selectObjectsGiven(String fieldName, String value) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + value);
			ArrayList objectSet = new ArrayList();
			makeConnection();
			String selectStatement = " select pkid from " + strObjectTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt("pkid")));
			}
			// prepStmt.close();;
			// releaseConnection();
			return objectSet;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private Collection selectObjectsGiven(String strConditions) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" conditions: " + strConditions);
			ArrayList objectSet = new ArrayList();
			makeConnection();
			String selectStatement = " select pkid from " + strObjectTable + "  where " + strConditions;
			prepStmt = con.prepareStatement(selectStatement);
			// prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt("pkid")));
			}
			// prepStmt.close();;
			// releaseConnection();
			return objectSet;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private void storeObject() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String updateStatement = "update " + strObjectTable + " set pkid = ? , " + " node_parent = ?, "
					+ " userid =  ? , " + " category = ? , " + " namespace = ? , " + " value1 = ? , "
					+ " value2 = ? , " + " value3 = ? , " + " value4 = ? , " + " value5 = ? , " + " status = ? , "
					+ " userid_edit = ? , " + " time_edit = ? , " + " time_effective = ?  " + " where pkid = ?;";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, this.mPKid.intValue());
			prepStmt.setInt(2, this.mNodeParent.intValue());
			prepStmt.setInt(3, this.mUserId.intValue());
			prepStmt.setString(4, this.mCategory);
			prepStmt.setString(5, this.mNamespace);
			prepStmt.setString(6, this.mValue1);
			prepStmt.setString(7, this.mValue2);
			prepStmt.setString(8, this.mValue3);
			prepStmt.setString(9, this.mValue4);
			prepStmt.setString(10, this.mValue5);
			prepStmt.setString(11, this.mStatus);
			prepStmt.setInt(12, this.mUserIdEdit.intValue());
			prepStmt.setTimestamp(13, this.mTimeEdit);
			prepStmt.setTimestamp(14, this.mTimeEffective);
			prepStmt.setInt(15, this.mPKid.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();;
			if (rowCount == 0)
			{
				throw new EJBException("Storing ejb object " + this.mPKid.toString() + " failed.");
			}
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}
} // UserConfigRegistryBean
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

public class UserConfigRegistryBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strObjectTable = "user_config_registry";
	protected final String strObjectName = "UserConfigRegistryBean: ";
	private Connection con;
	private EntityContext mContext;
	protected final String strTimeBegin = "0000-01-01 00:00:00.000000000";
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String NODE_PARENT = "node_parent";
	public static final String USERID = "userid";
	public static final String CATEGORY = "category";
	public static final String NAMESPACE = "namespace";
	public static final String VALUE1 = "value1";
	public static final String VALUE2 = "value2";
	public static final String VALUE3 = "value3";
	public static final String VALUE4 = "value4";
	public static final String VALUE5 = "value5";
	public static final String STATUS = "status";
	public static final String USERID_EDIT = "userid_edit";
	public static final String TIME_EDIT = "time_edit";
	public static final String TIME_EFFECTIVE = "time_effective";
	// constants ----------------------------------------------
	public static final String CAT_DEFAULT = "DEFAULT";
	public static final String NS_CUSTSVCCTR = "CUSTSVCCTR";
	public static final String NS_PROCCTR = "PROCCTR";
	public static final String NS_PCCENTER = "PCCENTER";
	// members ----------------------------------------------
	private Integer mPKid; // primary key!!!
	private Integer mNodeParent;
	private Integer mUserId;
	private String mCategory;
	private String mNamespace;
	private String mValue1;
	private String mValue2;
	private String mValue3;
	private String mValue4;
	private String mValue5;
	private String mStatus;
	private Integer mUserIdEdit;
	private Timestamp mTimeEdit;
	private Timestamp mTimeEffective;

	public Integer getPKid()
	{
		return this.mPKid;
	}

	public void setPKid(Integer pkid)
	{
		this.mPKid = pkid;
	}

	public Integer getNodeParent()
	{
		return this.mNodeParent;
	}

	public void setNodeParent(Integer iNodeParent)
	{
		this.mNodeParent = iNodeParent;
	}

	public Integer getUserId()
	{
		return this.mUserId;
	}

	public void setUserId(Integer userid)
	{
		this.mUserId = userid;
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
		return this.mPKid;
	}

	public void setPrimaryKey(Integer jtid)
	{
		this.mPKid = jtid;
	}

	public Integer ejbCreate(Integer iNodeParent, Integer userid, String strCategory, String strNamespace,
			String strValue1, String strValue2, String strValue3, String strValue4, String strValue5,
			Integer useridEdit, Timestamp tsEdit, Timestamp tsEffective) throws CreateException
	{
		Integer pkid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			pkid = insertNewRow(iNodeParent, userid, strCategory, strNamespace, strValue1, strValue2, strValue3,
					strValue4, strValue5, useridEdit, tsEdit, tsEffective);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (pkid != null)
		{
			this.mPKid = new Integer(pkid.intValue()); // primary key!!!
			this.mNodeParent = iNodeParent;
			this.mUserId = userid;
			this.mCategory = strCategory;
			this.mNamespace = strNamespace;
			this.mValue1 = strValue1;
			this.mValue2 = strValue2;
			this.mValue3 = strValue3;
			this.mValue4 = strValue4;
			this.mValue5 = strValue5;
			this.mUserIdEdit = useridEdit;
			this.mStatus = UserConfigRegistryBean.ACTIVE;
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
			deleteObject(this.mPKid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
	}

	public void setEntityContext(EntityContext mContext)
	{
		Log.printVerbose(strObjectName + " in setEntityContext");
		this.mContext = mContext;
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
		this.mPKid = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.mPKid = null;
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

	public void ejbPostCreate(Integer iNodeParent, Integer userid, String strCategory, String strNamespace,
			String strValue1, String strValue2, String strValue3, String strValue4, String strValue5,
			Integer useridEdit, Timestamp tsEdit, Timestamp tsEffective)
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

	public Collection ejbFindObjectsGiven(String strConditions) throws FinderException
	{
		try
		{
			Collection bufAL = selectObjectsGiven(strConditions);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindObjectsGiven: " + ex.getMessage());
			return null;
		}
	}

	public Vector ejbHomeGetValueObjectsGiven(Integer node, Integer userId, String strCat, String strNS, String value1,
			String value2, String value3, String value4, String value5, String status, Integer userIdEdit,
			Timestamp tsEffFrom, Timestamp tsEffTo)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(node, userId, strCat, strNS, value1, value2, value3, value4, value5,
					status, userIdEdit, tsEffFrom, tsEffTo);
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
			if (!con.isClosed())
			{
				con.close();
			}
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw new EJBException("releaseConnection: " + ex.getMessage());
		}
	} // releaseConnection

	private Integer insertNewRow(Integer iNodeParent, Integer userid, String strCategory, String strNamespace,
			String strValue1, String strValue2, String strValue3, String strValue4, String strValue5,
			Integer useridEdit, Timestamp tsEdit, Timestamp tsEffective) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + "insertNewRow: ");
			String findMaxPKIdStmt = " select max(" + PKID + ") as max_pkid from " + strObjectTable + " ";
			selectStmt = con.prepareStatement(findMaxPKIdStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("max_pkid"))
				{
					bufInt = rs.getInt("max_pkid");
				}
				bufInt = (bufInt < 10000) ? 10000 : bufInt;
			}
			Integer newPkid = new Integer(bufInt + 1);
			// new Integer(rs.getInt(1) + 1);
			Log.printVerbose("The new objectid is :" + newPkid.toString());
			String insertStatement = "insert into " + strObjectTable + " (pkid, node_parent, userid, category,   "
					+ " namespace , value1, " + " value2 , value3, " + " value4 , value5, " + " status, userid_edit, "
					+ " time_edit , time_effective)  " + " values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," + " ?, ?, ?, ? )";
			if (strValue1 == null)
				strValue1 = new String(" ");
			if (strValue2 == null)
				strValue2 = new String(" ");
			if (strValue3 == null)
				strValue3 = new String(" ");
			if (strValue4 == null)
				strValue4 = new String(" ");
			if (strValue5 == null)
				strValue5 = new String(" ");
			// Log.printVerbose("checkpoint 1:ok");
			insertStmt = con.prepareStatement(insertStatement);
			int lZero = 0;
			// Log.printVerbose("checkpoint 2:ok");
			insertStmt.setInt(1, newPkid.intValue());
			// Log.printVerbose("checkpoint 3:ok");
			insertStmt.setInt(2, iNodeParent.intValue());
			// Log.printVerbose("checkpoint 4:ok");
			// Log.printVerbose(" userid is " + userid.toString());
			insertStmt.setInt(3, userid.intValue());
			// Log.printVerbose("checkpoint 5:ok");
			insertStmt.setString(4, strCategory);
			// Log.printVerbose("checkpoint 6:ok");
			insertStmt.setString(5, strNamespace);
			// Log.printVerbose("checkpoint 7:ok");
			insertStmt.setString(6, strValue1);
			// Log.printVerbose("checkpoint 8:ok");
			insertStmt.setString(7, strValue2);
			// Log.printVerbose("checkpoint 9:ok");
			insertStmt.setString(8, strValue3);
			// Log.printVerbose("checkpoint 10:ok");
			insertStmt.setString(9, strValue4);
			// Log.printVerbose("checkpoint 11:ok");
			insertStmt.setString(10, strValue5);
			// Log.printVerbose("checkpoint 12:ok");
			insertStmt.setString(11, UserConfigRegistryBean.ACTIVE);
			// Log.printVerbose("checkpoint 13:ok");
			insertStmt.setInt(12, useridEdit.intValue());
			// Log.printVerbose("checkpoint 14:ok");
			insertStmt.setTimestamp(13, tsEdit);
			// Log.printVerbose("checkpoint 15:ok");
			insertStmt.setTimestamp(14, tsEffective);
			insertStmt.executeUpdate();
			// Log.printVerbose("checkpoint 16:ok");
			// insertStmt.close();;
			Log.printAudit(strObjectName + "Created New Row:" + newPkid.toString());
			Log.printVerbose(strObjectName + "leaving insertNewRow");
			// Log.printVerbose("checkpoint 18:ok");
			// releaseConnection();
			return newPkid;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (selectStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				selectStmt.close();
			}
			if (insertStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				insertStmt.close();
			}
			releaseConnection();
		}
	}

	// stop here
	private boolean selectByPrimaryKey(Integer pkid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select * " + "from " + strObjectTable + " where pkid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = false;
			result = rs.next();
			// prepStmt.close();;
			// releaseConnection();
			return result;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private void deleteObject(Integer pkid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String deleteStatement = "delete from " + strObjectTable + "  " + "where pkid = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setInt(1, pkid.intValue());
			prepStmt.executeUpdate();
			// prepStmt.close();;
			Log.printAudit(strObjectName + "Deleted Object : " + pkid.toString());
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	// /////////////////////////////////////////////////////////////
	private Vector selectValueObjectsGiven(Integer node, Integer userId, String strCat, String strNS, String value1,
			String value2, String value3, String value4, String value5, String status, Integer userIdEdit,
			Timestamp tsEffFrom, Timestamp tsEffTo) throws SQLException
	{
		Vector vecValObj = new Vector();
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStmt = "select pkid , node_parent, userid, category, " + " namespace, value1, "
					+ " value2, value3, value4, value5, " + " status, userid_edit, " + " time_edit, time_effective "
					+ " from " + strObjectTable + " where status != '*' ";
			if (node != null)
			{
				selectStmt += " AND " + NODE_PARENT + "='" + node.toString() + "' ";
			}
			if (userId != null)
			{
				selectStmt += " AND " + USERID + "='" + userId.toString() + "' ";
			}
			if (strCat != null)
			{
				selectStmt += " AND " + CATEGORY + "='" + strCat + "' ";
			}
			if (strNS != null)
			{
				selectStmt += " AND " + NAMESPACE + "='" + strNS + "' ";
			}
			if (value1 != null)
			{
				selectStmt += " AND " + VALUE1 + "='" + value1 + "' ";
			}
			if (value2 != null)
			{
				selectStmt += " AND " + VALUE2 + "='" + value2 + "' ";
			}
			if (value3 != null)
			{
				selectStmt += " AND " + VALUE3 + "='" + value3 + "' ";
			}
			if (value4 != null)
			{
				selectStmt += " AND " + VALUE4 + "='" + value4 + "' ";
			}
			if (value5 != null)
			{
				selectStmt += " AND " + VALUE5 + "='" + value5 + "' ";
			}
			if (status != null)
			{
				selectStmt += " AND " + STATUS + "='" + status + "' ";
			}
			if (userIdEdit != null)
			{
				selectStmt += " AND " + USERID_EDIT + "='" + userIdEdit.toString() + "' ";
			}
			if (tsEffFrom != null)
			{
				selectStmt += " AND " + TIME_EFFECTIVE + ">='" + TimeFormat.strDisplayDate(tsEffFrom) + "' ";
			}
			if (tsEffTo != null)
			{
				selectStmt += " AND " + TIME_EFFECTIVE + "<'" + TimeFormat.strDisplayDate(tsEffTo) + "' ";
			}
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			while (rs.next())
			{
				UserConfigRegistryObject ucrObj = new UserConfigRegistryObject();
				ucrObj.mPKid = new Integer(rs.getInt("pkid"));
				ucrObj.mNodeParent = new Integer(rs.getInt("node_parent"));
				ucrObj.mUserId = new Integer(rs.getInt("userid"));
				ucrObj.mCategory = rs.getString("category");
				ucrObj.mNamespace = rs.getString("namespace");
				ucrObj.mValue1 = rs.getString("value1");
				ucrObj.mValue2 = rs.getString("value2");
				ucrObj.mValue3 = rs.getString("value3");
				ucrObj.mValue4 = rs.getString("value4");
				ucrObj.mValue5 = rs.getString("value5");
				ucrObj.mStatus = rs.getString("status");
				ucrObj.mUserIdEdit = new Integer(rs.getInt("userid_edit"));
				ucrObj.mTimeEdit = rs.getTimestamp("time_edit");
				ucrObj.mTimeEffective = rs.getTimestamp("time_effective");
				vecValObj.add(ucrObj);
			}
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
		return vecValObj;
	}

	// /////////////////////////////////////////////////////////////
	private void loadObject() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select pkid , node_parent, userid, category, " + " namespace, value1, "
					+ " value2, value3, value4, value5, " + " status, userid_edit, " + " time_edit, time_effective "
					+ " from " + strObjectTable + " where pkid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.mPKid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.mPKid = new Integer(rs.getInt("pkid"));
				this.mNodeParent = new Integer(rs.getInt("node_parent"));
				this.mUserId = new Integer(rs.getInt("userid"));
				this.mCategory = rs.getString("category");
				this.mNamespace = rs.getString("namespace");
				this.mValue1 = rs.getString("value1");
				this.mValue2 = rs.getString("value2");
				this.mValue3 = rs.getString("value3");
				this.mValue4 = rs.getString("value4");
				this.mValue5 = rs.getString("value5");
				this.mStatus = rs.getString("status");
				this.mUserIdEdit = new Integer(rs.getInt("userid_edit"));
				this.mTimeEdit = rs.getTimestamp("time_edit");
				this.mTimeEffective = rs.getTimestamp("time_effective");
				// prepStmt.close();;
			} else
			{
				// prepStmt.close();;
				throw new NoSuchEntityException("Row for this EJB Object" + " is not found in database.");
			}
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private Collection selectAllObjects() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			makeConnection();
			String selectStatement = " select pkid from " + strObjectTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();;
			// releaseConnection();
			return objectSet;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private Collection selectObjectsGiven(String fieldName, String value) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + value);
			ArrayList objectSet = new ArrayList();
			makeConnection();
			String selectStatement = " select pkid from " + strObjectTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt("pkid")));
			}
			// prepStmt.close();;
			// releaseConnection();
			return objectSet;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private Collection selectObjectsGiven(String strConditions) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" conditions: " + strConditions);
			ArrayList objectSet = new ArrayList();
			makeConnection();
			String selectStatement = " select pkid from " + strObjectTable + "  where " + strConditions;
			prepStmt = con.prepareStatement(selectStatement);
			// prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt("pkid")));
			}
			// prepStmt.close();;
			// releaseConnection();
			return objectSet;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private void storeObject() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String updateStatement = "update " + strObjectTable + " set pkid = ? , " + " node_parent = ?, "
					+ " userid =  ? , " + " category = ? , " + " namespace = ? , " + " value1 = ? , "
					+ " value2 = ? , " + " value3 = ? , " + " value4 = ? , " + " value5 = ? , " + " status = ? , "
					+ " userid_edit = ? , " + " time_edit = ? , " + " time_effective = ?  " + " where pkid = ?;";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, this.mPKid.intValue());
			prepStmt.setInt(2, this.mNodeParent.intValue());
			prepStmt.setInt(3, this.mUserId.intValue());
			prepStmt.setString(4, this.mCategory);
			prepStmt.setString(5, this.mNamespace);
			prepStmt.setString(6, this.mValue1);
			prepStmt.setString(7, this.mValue2);
			prepStmt.setString(8, this.mValue3);
			prepStmt.setString(9, this.mValue4);
			prepStmt.setString(10, this.mValue5);
			prepStmt.setString(11, this.mStatus);
			prepStmt.setInt(12, this.mUserIdEdit.intValue());
			prepStmt.setTimestamp(13, this.mTimeEdit);
			prepStmt.setTimestamp(14, this.mTimeEffective);
			prepStmt.setInt(15, this.mPKid.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();;
			if (rowCount == 0)
			{
				throw new EJBException("Storing ejb object " + this.mPKid.toString() + " failed.");
			}
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}
} // UserConfigRegistryBean
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

public class UserConfigRegistryBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String strObjectTable = "user_config_registry";
	protected final String strObjectName = "UserConfigRegistryBean: ";
	private Connection con;
	private EntityContext mContext;
	protected final String strTimeBegin = "0000-01-01 00:00:00.000000000";
	// public constants
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String NODE_PARENT = "node_parent";
	public static final String USERID = "userid";
	public static final String CATEGORY = "category";
	public static final String NAMESPACE = "namespace";
	public static final String VALUE1 = "value1";
	public static final String VALUE2 = "value2";
	public static final String VALUE3 = "value3";
	public static final String VALUE4 = "value4";
	public static final String VALUE5 = "value5";
	public static final String STATUS = "status";
	public static final String USERID_EDIT = "userid_edit";
	public static final String TIME_EDIT = "time_edit";
	public static final String TIME_EFFECTIVE = "time_effective";
	// constants ----------------------------------------------
	public static final String CAT_DEFAULT = "DEFAULT";
	public static final String NS_CUSTSVCCTR = "CUSTSVCCTR";
	public static final String NS_PROCCTR = "PROCCTR";
	public static final String NS_PCCENTER = "PCCENTER";
	// members ----------------------------------------------
	private Integer mPKid; // primary key!!!
	private Integer mNodeParent;
	private Integer mUserId;
	private String mCategory;
	private String mNamespace;
	private String mValue1;
	private String mValue2;
	private String mValue3;
	private String mValue4;
	private String mValue5;
	private String mStatus;
	private Integer mUserIdEdit;
	private Timestamp mTimeEdit;
	private Timestamp mTimeEffective;

	public Integer getPKid()
	{
		return this.mPKid;
	}

	public void setPKid(Integer pkid)
	{
		this.mPKid = pkid;
	}

	public Integer getNodeParent()
	{
		return this.mNodeParent;
	}

	public void setNodeParent(Integer iNodeParent)
	{
		this.mNodeParent = iNodeParent;
	}

	public Integer getUserId()
	{
		return this.mUserId;
	}

	public void setUserId(Integer userid)
	{
		this.mUserId = userid;
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
		return this.mPKid;
	}

	public void setPrimaryKey(Integer jtid)
	{
		this.mPKid = jtid;
	}

	public Integer ejbCreate(Integer iNodeParent, Integer userid, String strCategory, String strNamespace,
			String strValue1, String strValue2, String strValue3, String strValue4, String strValue5,
			Integer useridEdit, Timestamp tsEdit, Timestamp tsEffective) throws CreateException
	{
		Integer pkid = null;
		Log.printVerbose(strObjectName + " in ejbCreate ");
		try
		{
			pkid = insertNewRow(iNodeParent, userid, strCategory, strNamespace, strValue1, strValue2, strValue3,
					strValue4, strValue5, useridEdit, tsEdit, tsEffective);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (pkid != null)
		{
			this.mPKid = new Integer(pkid.intValue()); // primary key!!!
			this.mNodeParent = iNodeParent;
			this.mUserId = userid;
			this.mCategory = strCategory;
			this.mNamespace = strNamespace;
			this.mValue1 = strValue1;
			this.mValue2 = strValue2;
			this.mValue3 = strValue3;
			this.mValue4 = strValue4;
			this.mValue5 = strValue5;
			this.mUserIdEdit = useridEdit;
			this.mStatus = UserConfigRegistryBean.ACTIVE;
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
			deleteObject(this.mPKid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
	}

	public void setEntityContext(EntityContext mContext)
	{
		Log.printVerbose(strObjectName + " in setEntityContext");
		this.mContext = mContext;
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
		this.mPKid = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.mPKid = null;
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

	public void ejbPostCreate(Integer iNodeParent, Integer userid, String strCategory, String strNamespace,
			String strValue1, String strValue2, String strValue3, String strValue4, String strValue5,
			Integer useridEdit, Timestamp tsEdit, Timestamp tsEffective)
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

	public Collection ejbFindObjectsGiven(String strConditions) throws FinderException
	{
		try
		{
			Collection bufAL = selectObjectsGiven(strConditions);
			return bufAL;
		} catch (Exception ex)
		{
			Log.printDebug(strObjectName + "ejbFindObjectsGiven: " + ex.getMessage());
			return null;
		}
	}

	public Vector ejbHomeGetValueObjectsGiven(Integer node, Integer userId, String strCat, String strNS, String value1,
			String value2, String value3, String value4, String value5, String status, Integer userIdEdit,
			Timestamp tsEffFrom, Timestamp tsEffTo)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(node, userId, strCat, strNS, value1, value2, value3, value4, value5,
					status, userIdEdit, tsEffFrom, tsEffTo);
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
			if (!con.isClosed())
			{
				con.close();
			}
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw new EJBException("releaseConnection: " + ex.getMessage());
		}
	} // releaseConnection

	private Integer insertNewRow(Integer iNodeParent, Integer userid, String strCategory, String strNamespace,
			String strValue1, String strValue2, String strValue3, String strValue4, String strValue5,
			Integer useridEdit, Timestamp tsEdit, Timestamp tsEffective) throws SQLException
	{
		PreparedStatement selectStmt = null;
		PreparedStatement insertStmt = null;
		try
		{
			makeConnection();
			Log.printVerbose(strObjectName + "insertNewRow: ");
			String findMaxPKIdStmt = " select max(" + PKID + ") as max_pkid from " + strObjectTable + " ";
			selectStmt = con.prepareStatement(findMaxPKIdStmt);
			ResultSet rs = selectStmt.executeQuery();
			int bufInt = 0;
			while (rs.next())
			{
				if (bufInt < rs.getInt("max_pkid"))
				{
					bufInt = rs.getInt("max_pkid");
				}
				bufInt = (bufInt < 10000) ? 10000 : bufInt;
			}
			Integer newPkid = new Integer(bufInt + 1);
			// new Integer(rs.getInt(1) + 1);
			Log.printVerbose("The new objectid is :" + newPkid.toString());
			String insertStatement = "insert into " + strObjectTable + " (pkid, node_parent, userid, category,   "
					+ " namespace , value1, " + " value2 , value3, " + " value4 , value5, " + " status, userid_edit, "
					+ " time_edit , time_effective)  " + " values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," + " ?, ?, ?, ? )";
			if (strValue1 == null)
				strValue1 = new String(" ");
			if (strValue2 == null)
				strValue2 = new String(" ");
			if (strValue3 == null)
				strValue3 = new String(" ");
			if (strValue4 == null)
				strValue4 = new String(" ");
			if (strValue5 == null)
				strValue5 = new String(" ");
			// Log.printVerbose("checkpoint 1:ok");
			insertStmt = con.prepareStatement(insertStatement);
			int lZero = 0;
			// Log.printVerbose("checkpoint 2:ok");
			insertStmt.setInt(1, newPkid.intValue());
			// Log.printVerbose("checkpoint 3:ok");
			insertStmt.setInt(2, iNodeParent.intValue());
			// Log.printVerbose("checkpoint 4:ok");
			// Log.printVerbose(" userid is " + userid.toString());
			insertStmt.setInt(3, userid.intValue());
			// Log.printVerbose("checkpoint 5:ok");
			insertStmt.setString(4, strCategory);
			// Log.printVerbose("checkpoint 6:ok");
			insertStmt.setString(5, strNamespace);
			// Log.printVerbose("checkpoint 7:ok");
			insertStmt.setString(6, strValue1);
			// Log.printVerbose("checkpoint 8:ok");
			insertStmt.setString(7, strValue2);
			// Log.printVerbose("checkpoint 9:ok");
			insertStmt.setString(8, strValue3);
			// Log.printVerbose("checkpoint 10:ok");
			insertStmt.setString(9, strValue4);
			// Log.printVerbose("checkpoint 11:ok");
			insertStmt.setString(10, strValue5);
			// Log.printVerbose("checkpoint 12:ok");
			insertStmt.setString(11, UserConfigRegistryBean.ACTIVE);
			// Log.printVerbose("checkpoint 13:ok");
			insertStmt.setInt(12, useridEdit.intValue());
			// Log.printVerbose("checkpoint 14:ok");
			insertStmt.setTimestamp(13, tsEdit);
			// Log.printVerbose("checkpoint 15:ok");
			insertStmt.setTimestamp(14, tsEffective);
			insertStmt.executeUpdate();
			// Log.printVerbose("checkpoint 16:ok");
			// insertStmt.close();;
			Log.printAudit(strObjectName + "Created New Row:" + newPkid.toString());
			Log.printVerbose(strObjectName + "leaving insertNewRow");
			// Log.printVerbose("checkpoint 18:ok");
			// releaseConnection();
			return newPkid;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (selectStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				selectStmt.close();
			}
			if (insertStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				insertStmt.close();
			}
			releaseConnection();
		}
	}

	// stop here
	private boolean selectByPrimaryKey(Integer pkid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select * " + "from " + strObjectTable + " where pkid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = false;
			result = rs.next();
			// prepStmt.close();;
			// releaseConnection();
			return result;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private void deleteObject(Integer pkid) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String deleteStatement = "delete from " + strObjectTable + "  " + "where pkid = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setInt(1, pkid.intValue());
			prepStmt.executeUpdate();
			// prepStmt.close();;
			Log.printAudit(strObjectName + "Deleted Object : " + pkid.toString());
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	// /////////////////////////////////////////////////////////////
	private Vector selectValueObjectsGiven(Integer node, Integer userId, String strCat, String strNS, String value1,
			String value2, String value3, String value4, String value5, String status, Integer userIdEdit,
			Timestamp tsEffFrom, Timestamp tsEffTo) throws SQLException
	{
		Vector vecValObj = new Vector();
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStmt = "select pkid , node_parent, userid, category, " + " namespace, value1, "
					+ " value2, value3, value4, value5, " + " status, userid_edit, " + " time_edit, time_effective "
					+ " from " + strObjectTable + " where status != '*' ";
			if (node != null)
			{
				selectStmt += " AND " + NODE_PARENT + "='" + node.toString() + "' ";
			}
			if (userId != null)
			{
				selectStmt += " AND " + USERID + "='" + userId.toString() + "' ";
			}
			if (strCat != null)
			{
				selectStmt += " AND " + CATEGORY + "='" + strCat + "' ";
			}
			if (strNS != null)
			{
				selectStmt += " AND " + NAMESPACE + "='" + strNS + "' ";
			}
			if (value1 != null)
			{
				selectStmt += " AND " + VALUE1 + "='" + value1 + "' ";
			}
			if (value2 != null)
			{
				selectStmt += " AND " + VALUE2 + "='" + value2 + "' ";
			}
			if (value3 != null)
			{
				selectStmt += " AND " + VALUE3 + "='" + value3 + "' ";
			}
			if (value4 != null)
			{
				selectStmt += " AND " + VALUE4 + "='" + value4 + "' ";
			}
			if (value5 != null)
			{
				selectStmt += " AND " + VALUE5 + "='" + value5 + "' ";
			}
			if (status != null)
			{
				selectStmt += " AND " + STATUS + "='" + status + "' ";
			}
			if (userIdEdit != null)
			{
				selectStmt += " AND " + USERID_EDIT + "='" + userIdEdit.toString() + "' ";
			}
			if (tsEffFrom != null)
			{
				selectStmt += " AND " + TIME_EFFECTIVE + ">='" + TimeFormat.strDisplayDate(tsEffFrom) + "' ";
			}
			if (tsEffTo != null)
			{
				selectStmt += " AND " + TIME_EFFECTIVE + "<'" + TimeFormat.strDisplayDate(tsEffTo) + "' ";
			}
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			while (rs.next())
			{
				UserConfigRegistryObject ucrObj = new UserConfigRegistryObject();
				ucrObj.mPKid = new Integer(rs.getInt("pkid"));
				ucrObj.mNodeParent = new Integer(rs.getInt("node_parent"));
				ucrObj.mUserId = new Integer(rs.getInt("userid"));
				ucrObj.mCategory = rs.getString("category");
				ucrObj.mNamespace = rs.getString("namespace");
				ucrObj.mValue1 = rs.getString("value1");
				ucrObj.mValue2 = rs.getString("value2");
				ucrObj.mValue3 = rs.getString("value3");
				ucrObj.mValue4 = rs.getString("value4");
				ucrObj.mValue5 = rs.getString("value5");
				ucrObj.mStatus = rs.getString("status");
				ucrObj.mUserIdEdit = new Integer(rs.getInt("userid_edit"));
				ucrObj.mTimeEdit = rs.getTimestamp("time_edit");
				ucrObj.mTimeEffective = rs.getTimestamp("time_effective");
				vecValObj.add(ucrObj);
			}
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
		return vecValObj;
	}

	// /////////////////////////////////////////////////////////////
	private void loadObject() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String selectStatement = "select pkid , node_parent, userid, category, " + " namespace, value1, "
					+ " value2, value3, value4, value5, " + " status, userid_edit, " + " time_edit, time_effective "
					+ " from " + strObjectTable + " where pkid = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.mPKid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.mPKid = new Integer(rs.getInt("pkid"));
				this.mNodeParent = new Integer(rs.getInt("node_parent"));
				this.mUserId = new Integer(rs.getInt("userid"));
				this.mCategory = rs.getString("category");
				this.mNamespace = rs.getString("namespace");
				this.mValue1 = rs.getString("value1");
				this.mValue2 = rs.getString("value2");
				this.mValue3 = rs.getString("value3");
				this.mValue4 = rs.getString("value4");
				this.mValue5 = rs.getString("value5");
				this.mStatus = rs.getString("status");
				this.mUserIdEdit = new Integer(rs.getInt("userid_edit"));
				this.mTimeEdit = rs.getTimestamp("time_edit");
				this.mTimeEffective = rs.getTimestamp("time_effective");
				// prepStmt.close();;
			} else
			{
				// prepStmt.close();;
				throw new NoSuchEntityException("Row for this EJB Object" + " is not found in database.");
			}
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private Collection selectAllObjects() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			makeConnection();
			String selectStatement = " select pkid from " + strObjectTable + "  ";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();;
			// releaseConnection();
			return objectSet;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private Collection selectObjectsGiven(String fieldName, String value) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + value);
			ArrayList objectSet = new ArrayList();
			makeConnection();
			String selectStatement = " select pkid from " + strObjectTable + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt("pkid")));
			}
			// prepStmt.close();;
			// releaseConnection();
			return objectSet;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private Collection selectObjectsGiven(String strConditions) throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" conditions: " + strConditions);
			ArrayList objectSet = new ArrayList();
			makeConnection();
			String selectStatement = " select pkid from " + strObjectTable + "  where " + strConditions;
			prepStmt = con.prepareStatement(selectStatement);
			// prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt("pkid")));
			}
			// prepStmt.close();;
			// releaseConnection();
			return objectSet;
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}

	private void storeObject() throws SQLException
	{
		PreparedStatement prepStmt = null;
		try
		{
			makeConnection();
			String updateStatement = "update " + strObjectTable + " set pkid = ? , " + " node_parent = ?, "
					+ " userid =  ? , " + " category = ? , " + " namespace = ? , " + " value1 = ? , "
					+ " value2 = ? , " + " value3 = ? , " + " value4 = ? , " + " value5 = ? , " + " status = ? , "
					+ " userid_edit = ? , " + " time_edit = ? , " + " time_effective = ?  " + " where pkid = ?;";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, this.mPKid.intValue());
			prepStmt.setInt(2, this.mNodeParent.intValue());
			prepStmt.setInt(3, this.mUserId.intValue());
			prepStmt.setString(4, this.mCategory);
			prepStmt.setString(5, this.mNamespace);
			prepStmt.setString(6, this.mValue1);
			prepStmt.setString(7, this.mValue2);
			prepStmt.setString(8, this.mValue3);
			prepStmt.setString(9, this.mValue4);
			prepStmt.setString(10, this.mValue5);
			prepStmt.setString(11, this.mStatus);
			prepStmt.setInt(12, this.mUserIdEdit.intValue());
			prepStmt.setTimestamp(13, this.mTimeEdit);
			prepStmt.setTimestamp(14, this.mTimeEffective);
			prepStmt.setInt(15, this.mPKid.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();;
			if (rowCount == 0)
			{
				throw new EJBException("Storing ejb object " + this.mPKid.toString() + " failed.");
			}
			// releaseConnection();
		} catch (SQLException ex)
		{
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			releaseConnection();
		}
	}
} // UserConfigRegistryBean
