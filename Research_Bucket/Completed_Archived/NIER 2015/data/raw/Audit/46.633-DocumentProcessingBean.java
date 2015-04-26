/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.accounting;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.math.BigDecimal;
import javax.ejb.*;
import javax.naming.*;
import com.vlee.local.*;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.customer.*;

public class DocumentProcessingBean implements EntityBean
{
	private static String strClassName = "DocumentProcessingBean";
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String MODULE = "module";
	public static final String PROCESS_TYPE = "process_type";
	public static final String CATEGORY = "category";
	public static final String AUDIT_LEVEL = "audit_level";
	public static final String USER_CREATE = "user_create";
	public static final String USER_PERFORM = "user_perform";
	public static final String USER_CONFIRM = "user_confirm";
	public static final String DESCRIPTION1 = "description1";
	public static final String DESCRIPTION2 = "description2";
	public static final String REMARKS = "remarks";
	public static final String TIME_CREATED = "time_created";
	public static final String TIME_SCHEDULED = "time_scheduled";
	public static final String TIME_COMPLETED = "time_completed";
	public static final String STATE = "state";
	public static final String STATUS = "status";
	// Constants for STATUS
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	public static final String STATUS_CANCELLED = "cancelled";
	// Constants for STATE
	public static final String STATE_CREATED = "CREATED";
	public static final String STATE_SCHEDULED = "SCHEDULED";
	public static final String STATE_PENDING = "PENDING";
	public static final String STATE_CLOSED = "CLOSED";
	public static final String STATE_CANCEL = "CANCEL";
	public static final String MODULE_DEFAULT = "";
	public static final String MODULE_DISTRIBUTION = "DISTRI";
	public static final String PROCESS_DEFAULT = "";
	public static final String PROCESS_DEBT_COLLECTION = "DEBT_COLL";
	// Attributes of Object
	private DocumentProcessingObject valObj;
	// DB Connection attributes
	private Connection con = null;
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_document_processing";
	// Other params
	private static final String strObjectName = "DocumentProcessingBean: ";
	public static final String MODULENAME = "acc";
	// EntityContext
	private EntityContext context = null;

	public DocumentProcessingObject getObject()
	{
		return this.valObj;
	}

	public void setObject(DocumentProcessingObject newObj)
	{
		// / must preserve the primary key
		Long pkid = this.valObj.pkid;
		this.valObj = newObj;
		this.valObj.pkid = pkid;
	}

	public Long getPkid()
	{
		return this.valObj.pkid;
	}

	public void setPkid(Long pkid)
	{
		this.valObj.pkid = pkid;
	}

	/***************************************************************************
	 * ejbCreate
	 **************************************************************************/
	public Long ejbCreate(DocumentProcessingObject newObj) throws CreateException
	{
		Long newKey = null;
		Log.printVerbose(strObjectName + "in ejbCreate");
		try
		{
			newKey = insertObject(newObj);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (newKey != null)
		{
			this.valObj = newObj;
			this.valObj.pkid = newKey;
		}
		Log.printVerbose(strObjectName + "about to leave ejbCreate");
		return newKey;
	}

	/***************************************************************************
	 * ejbFindByPrimaryKey
	 **************************************************************************/
	public Long ejbFindByPrimaryKey(Long primaryKey) throws FinderException
	{
		Log.printVerbose(strObjectName + "in ejbFindByPrimaryKey");
		boolean result;
		try
		{
			result = selectByPrimaryKey(primaryKey);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		if (result)
		{
			return primaryKey;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + primaryKey.toString() + "not found.");
		}
	}

	/***************************************************************************
	 * ejbFindAllObjects
	 **************************************************************************/
	public Collection ejbFindAllObjects() throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindAllObjects");
		Collection result;
		try
		{
			result = selectAll();
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindAllObjects: " + ex.getMessage());
		}
		return result;
	}

	/***************************************************************************
	 * ejbRemove
	 **************************************************************************/
	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		try
		{
			deleteObject(this.valObj.pkid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
	}

	/***************************************************************************
	 * setEntityContext
	 **************************************************************************/
	public void setEntityContext(EntityContext context)
	{
		Log.printVerbose(strObjectName + " In setEntityContext");
		this.context = context;
	}

	/***************************************************************************
	 * unsetEntityContext
	 **************************************************************************/
	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.context = null;
	}

	/***************************************************************************
	 * ejbActivate
	 **************************************************************************/
	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.valObj = new DocumentProcessingObject();
		this.valObj.pkid = (Long) context.getPrimaryKey();
	}

	/***************************************************************************
	 * ejbPassivate
	 **************************************************************************/
	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.valObj = null;
	}

	/***************************************************************************
	 * ejbLoad
	 **************************************************************************/
	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadObject();
		} catch (Exception ex)
		{
			throw new EJBException("ejbLoad: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbLoad");
	}

	/***************************************************************************
	 * ejbStore
	 **************************************************************************/
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

	/***************************************************************************
	 * ejbPostCreate
	 **************************************************************************/
	public void ejbPostCreate(DocumentProcessingObject newObj)
	{
	}

	public Collection ejbHomeGetObjects(QueryObject query)
	{
		Collection result = new Vector();
		try
		{
			result = selectObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}

	public int ejbHomeGetCount(QueryObject query)
	{
		int result = 0;
		try
		{
			result = selectCount(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}

	/** ********************* Database Routines ************************ */
	private void makeConnection() throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			con = ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void closeConnection() throws NamingException, SQLException
	{
		try
		{
			con.close();
		} catch (SQLException ex)
		{
			throw new EJBException("closeConnection: " + ex.getMessage());
		}
	}

	private Long insertObject(DocumentProcessingObject newObj) throws NamingException, SQLException
	{
		Long nextPKId = null;
		Log.printVerbose(strObjectName + " insertObject: ");
		makeConnection();
		try
		{
			nextPKId = getNextPKId(con);
		} catch (Exception ex)
		{
			throw new EJBException(strObjectName + ex.getMessage());
		}
		// makeConnection();
		newObj.pkid = nextPKId;
		String insertStatement = "insert into " + TABLENAME + "(" + PKID + ", " + MODULE + ", " + PROCESS_TYPE + ", "
				+ CATEGORY + ", " + AUDIT_LEVEL + ", " + USER_CREATE + ", " + USER_PERFORM + ", " + USER_CONFIRM + ", "
				+ DESCRIPTION1 + ", " + DESCRIPTION2 + ", " + // 10
				REMARKS + ", " + TIME_CREATED + ", " + TIME_SCHEDULED + ", " + TIME_COMPLETED + ", " + STATE + ", "
				+ STATUS + // 16
				") values (" + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," + " ?, ?, ?, ?, ?, ? )";
		PreparedStatement prepStmt = con.prepareStatement(insertStatement);
		try
		{
			prepStmt.setLong(1, nextPKId.longValue());
			prepStmt.setString(2, newObj.module);
			prepStmt.setString(3, newObj.processType);
			prepStmt.setString(4, newObj.category);
			prepStmt.setInt(5, newObj.auditLevel.intValue());
			prepStmt.setInt(6, newObj.userCreate.intValue());
			prepStmt.setInt(7, newObj.userPerform.intValue());
			prepStmt.setInt(8, newObj.userConfirm.intValue());
			prepStmt.setString(9, newObj.description1);
			prepStmt.setString(10, newObj.description2);
			prepStmt.setString(11, newObj.remarks);
			prepStmt.setTimestamp(12, newObj.timeCreated);
			prepStmt.setTimestamp(13, newObj.timeScheduled);
			prepStmt.setTimestamp(14, newObj.timeCompleted);
			prepStmt.setString(15, newObj.state);
			prepStmt.setString(16, newObj.status);
			prepStmt.executeUpdate();
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving insertObject: ");
		return nextPKId;
	}

	private boolean selectByPrimaryKey(Long primaryKey) throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " selectByPrimaryKey: ");
		makeConnection();
		String selectStatement = "select " + PKID + " from " + TABLENAME + " where " + PKID + " = ?";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setLong(1, primaryKey.longValue());
		ResultSet rs = prepStmt.executeQuery();
		boolean result = rs.next();
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving selectByPrimaryKey:");
		return result;
	}

	private void deleteObject(Long pkid) throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " deleteObject: ");
		makeConnection();
		String deleteStatement = " DELETE FROM " + TABLENAME + "  WHERE " + PKID + " = ?";
		PreparedStatement prepStmt = con.prepareStatement(deleteStatement);
		prepStmt.setLong(1, pkid.longValue());
		prepStmt.executeUpdate();
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving deleteObject: ");
	}

	// ///////////////////////////////////////////////////////////////////
	private void loadObject() throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " loadObject: ");
		makeConnection();
		String selectStatement = "	SELECT * " + " FROM " + TABLENAME + " WHERE " + PKID + " = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setLong(1, this.valObj.pkid.longValue());
		ResultSet rs = prepStmt.executeQuery();
		if (rs.next())
		{
			try
			{
				this.valObj = getObject(rs, "");
			} catch (Exception ex)
			{
				ex.printStackTrace();
			} finally
			{
				prepStmt.close();
			}
		} else
		{
			prepStmt.close();
			throw new NoSuchEntityException("Row for pkid " + this.valObj.pkid.toString() + " not found in database.");
		}
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving loadObject: ");
	}

	private void storeObject() throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " storeObject ");
		makeConnection();
		int rowCount = 0;
		try
		{
			String updateStatement = " UPDATE " + TABLENAME + " SET " + PKID + " = ?, " + MODULE + " = ?, "
					+ PROCESS_TYPE + " = ?, " + CATEGORY + " = ?, " + AUDIT_LEVEL + " = ?, " + USER_CREATE + " = ?, "
					+ USER_PERFORM + " = ?, " + USER_CONFIRM + " = ?, " + DESCRIPTION1 + " = ?, " + DESCRIPTION2
					+ " = ?, " + // 10
					REMARKS + " = ?, " + TIME_CREATED + " = ?, " + TIME_SCHEDULED + " = ?, " + TIME_COMPLETED
					+ " = ?, " + STATE + " = ?, " + STATUS + " = ? " + " WHERE " + PKID + " = ?";
			Log.printVerbose("updateStatement = " + updateStatement);
			PreparedStatement prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setLong(1, this.valObj.pkid.longValue());
			prepStmt.setString(2, this.valObj.module);
			prepStmt.setString(3, this.valObj.processType);
			prepStmt.setString(4, this.valObj.category);
			prepStmt.setInt(5, this.valObj.auditLevel.intValue());
			prepStmt.setInt(6, this.valObj.userCreate.intValue());
			prepStmt.setInt(7, this.valObj.userPerform.intValue());
			prepStmt.setInt(8, this.valObj.userConfirm.intValue());
			prepStmt.setString(9, this.valObj.description1);
			prepStmt.setString(10, this.valObj.description2);
			prepStmt.setString(11, this.valObj.remarks);
			prepStmt.setTimestamp(12, this.valObj.timeCreated);
			prepStmt.setTimestamp(13, this.valObj.timeScheduled);
			prepStmt.setTimestamp(14, this.valObj.timeCompleted);
			prepStmt.setString(15, this.valObj.state);
			prepStmt.setString(16, this.valObj.status);
			prepStmt.setLong(17, this.valObj.pkid.longValue());
			rowCount = prepStmt.executeUpdate();
			prepStmt.close();
		} catch (SQLException ex2)
		{
			ex2.printStackTrace();
			throw ex2;
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		closeConnection();
		if (rowCount == 0)
		{
			throw new EJBException("Storing row for pkid " + this.valObj.pkid + " failed.");
		}
		Log.printVerbose(strObjectName + " Leaving storeObject: ");
	}

	private Collection selectAll() throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " selectAll: ");
		makeConnection();
		String selectStatement = "select " + PKID + " from " + TABLENAME;
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		ArrayList pkIdList = new ArrayList();
		while (rs.next())
		{
			pkIdList.add(new Long(rs.getLong(1)));
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving selectAll: ");
		return pkIdList;
	}

	// ///////////////////////////////////////////////////////////////////
	private Collection selectObjects(QueryObject query) throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " loadObject: ");
		Collection result = new Vector();
		makeConnection();
		String selectStatement = "	SELECT * " + " FROM " + TABLENAME;
		selectStatement = query.appendQuery(selectStatement);
		Log.printVerbose("select stmt = " + selectStatement);
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		while (rs.next())
		{
			try
			{
				DocumentProcessingObject catObj = getObject(rs, "");
				result.add(catObj);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving loadObject: ");
		return result;
	}

	// ///////////////////////////////////////////////////////////////////
	private int selectCount(QueryObject query) throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " loadObject: ");
		int result = 0;
		makeConnection();
		String selectStatement = " SELECT COUNT(" + PKID + ") AS count " + " FROM " + TABLENAME;
		selectStatement = query.appendCount(selectStatement);
		Log.printVerbose("selectStatement = " + selectStatement);
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		if (rs.next())
		{
			try
			{
				result = rs.getInt("count");
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving loadObject: ");
		return result;
	}

	public static DocumentProcessingObject getObject(ResultSet rs, String prefix) throws Exception
	{
		DocumentProcessingObject theObj = null;
		try
		{
			theObj = new DocumentProcessingObject();
			theObj.pkid = new Long(rs.getLong(prefix + PKID)); // primary key
			theObj.module = rs.getString(prefix + MODULE);
			theObj.processType = rs.getString(prefix + PROCESS_TYPE);
			theObj.category = rs.getString(prefix + CATEGORY);
			theObj.auditLevel = new Integer(rs.getInt(prefix + AUDIT_LEVEL));
			theObj.userCreate = new Integer(rs.getInt(prefix + USER_CREATE));
			theObj.userPerform = new Integer(rs.getInt(prefix + USER_PERFORM));
			theObj.userConfirm = new Integer(rs.getInt(prefix + USER_CONFIRM));
			theObj.description1 = rs.getString(prefix + DESCRIPTION1);
			theObj.description2 = rs.getString(prefix + DESCRIPTION2);
			theObj.remarks = rs.getString(prefix + REMARKS);
			theObj.timeCreated = rs.getTimestamp(prefix + TIME_CREATED);
			theObj.timeScheduled = rs.getTimestamp(prefix + TIME_SCHEDULED);
			theObj.timeCompleted = rs.getTimestamp(prefix + TIME_COMPLETED);
			theObj.state = rs.getString(prefix + STATE);
			theObj.status = rs.getString(prefix + STATUS);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		return theObj;
	}

	// ============ Private methods ==========================
	private static synchronized Long getNextPKId(Connection con) throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, PKID, TABLENAME, MODULENAME, (Long) null);
	}
} // ObjectBean
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.accounting;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.math.BigDecimal;
import javax.ejb.*;
import javax.naming.*;
import com.vlee.local.*;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.customer.*;

public class DocumentProcessingBean implements EntityBean
{
	private static String strClassName = "DocumentProcessingBean";
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String MODULE = "module";
	public static final String PROCESS_TYPE = "process_type";
	public static final String CATEGORY = "category";
	public static final String AUDIT_LEVEL = "audit_level";
	public static final String USER_CREATE = "user_create";
	public static final String USER_PERFORM = "user_perform";
	public static final String USER_CONFIRM = "user_confirm";
	public static final String DESCRIPTION1 = "description1";
	public static final String DESCRIPTION2 = "description2";
	public static final String REMARKS = "remarks";
	public static final String TIME_CREATED = "time_created";
	public static final String TIME_SCHEDULED = "time_scheduled";
	public static final String TIME_COMPLETED = "time_completed";
	public static final String STATE = "state";
	public static final String STATUS = "status";
	// Constants for STATUS
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	public static final String STATUS_CANCELLED = "cancelled";
	// Constants for STATE
	public static final String STATE_CREATED = "CREATED";
	public static final String STATE_SCHEDULED = "SCHEDULED";
	public static final String STATE_PENDING = "PENDING";
	public static final String STATE_CLOSED = "CLOSED";
	public static final String STATE_CANCEL = "CANCEL";
	public static final String MODULE_DEFAULT = "";
	public static final String MODULE_DISTRIBUTION = "DISTRI";
	public static final String PROCESS_DEFAULT = "";
	public static final String PROCESS_DEBT_COLLECTION = "DEBT_COLL";
	// Attributes of Object
	private DocumentProcessingObject valObj;
	// DB Connection attributes
	private Connection con = null;
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_document_processing";
	// Other params
	private static final String strObjectName = "DocumentProcessingBean: ";
	public static final String MODULENAME = "acc";
	// EntityContext
	private EntityContext context = null;

	public DocumentProcessingObject getObject()
	{
		return this.valObj;
	}

	public void setObject(DocumentProcessingObject newObj)
	{
		// / must preserve the primary key
		Long pkid = this.valObj.pkid;
		this.valObj = newObj;
		this.valObj.pkid = pkid;
	}

	public Long getPkid()
	{
		return this.valObj.pkid;
	}

	public void setPkid(Long pkid)
	{
		this.valObj.pkid = pkid;
	}

	/***************************************************************************
	 * ejbCreate
	 **************************************************************************/
	public Long ejbCreate(DocumentProcessingObject newObj) throws CreateException
	{
		Long newKey = null;
		Log.printVerbose(strObjectName + "in ejbCreate");
		try
		{
			newKey = insertObject(newObj);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (newKey != null)
		{
			this.valObj = newObj;
			this.valObj.pkid = newKey;
		}
		Log.printVerbose(strObjectName + "about to leave ejbCreate");
		return newKey;
	}

	/***************************************************************************
	 * ejbFindByPrimaryKey
	 **************************************************************************/
	public Long ejbFindByPrimaryKey(Long primaryKey) throws FinderException
	{
		Log.printVerbose(strObjectName + "in ejbFindByPrimaryKey");
		boolean result;
		try
		{
			result = selectByPrimaryKey(primaryKey);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		if (result)
		{
			return primaryKey;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + primaryKey.toString() + "not found.");
		}
	}

	/***************************************************************************
	 * ejbFindAllObjects
	 **************************************************************************/
	public Collection ejbFindAllObjects() throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindAllObjects");
		Collection result;
		try
		{
			result = selectAll();
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindAllObjects: " + ex.getMessage());
		}
		return result;
	}

	/***************************************************************************
	 * ejbRemove
	 **************************************************************************/
	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		try
		{
			deleteObject(this.valObj.pkid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
	}

	/***************************************************************************
	 * setEntityContext
	 **************************************************************************/
	public void setEntityContext(EntityContext context)
	{
		Log.printVerbose(strObjectName + " In setEntityContext");
		this.context = context;
	}

	/***************************************************************************
	 * unsetEntityContext
	 **************************************************************************/
	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.context = null;
	}

	/***************************************************************************
	 * ejbActivate
	 **************************************************************************/
	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.valObj = new DocumentProcessingObject();
		this.valObj.pkid = (Long) context.getPrimaryKey();
	}

	/***************************************************************************
	 * ejbPassivate
	 **************************************************************************/
	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.valObj = null;
	}

	/***************************************************************************
	 * ejbLoad
	 **************************************************************************/
	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadObject();
		} catch (Exception ex)
		{
			throw new EJBException("ejbLoad: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbLoad");
	}

	/***************************************************************************
	 * ejbStore
	 **************************************************************************/
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

	/***************************************************************************
	 * ejbPostCreate
	 **************************************************************************/
	public void ejbPostCreate(DocumentProcessingObject newObj)
	{
	}

	public Collection ejbHomeGetObjects(QueryObject query)
	{
		Collection result = new Vector();
		try
		{
			result = selectObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}

	public int ejbHomeGetCount(QueryObject query)
	{
		int result = 0;
		try
		{
			result = selectCount(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}

	/** ********************* Database Routines ************************ */
	private void makeConnection() throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			con = ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void closeConnection() throws NamingException, SQLException
	{
		try
		{
			con.close();
		} catch (SQLException ex)
		{
			throw new EJBException("closeConnection: " + ex.getMessage());
		}
	}

	private Long insertObject(DocumentProcessingObject newObj) throws NamingException, SQLException
	{
		Long nextPKId = null;
		Log.printVerbose(strObjectName + " insertObject: ");
		makeConnection();
		try
		{
			nextPKId = getNextPKId(con);
		} catch (Exception ex)
		{
			throw new EJBException(strObjectName + ex.getMessage());
		}
		// makeConnection();
		newObj.pkid = nextPKId;
		String insertStatement = "insert into " + TABLENAME + "(" + PKID + ", " + MODULE + ", " + PROCESS_TYPE + ", "
				+ CATEGORY + ", " + AUDIT_LEVEL + ", " + USER_CREATE + ", " + USER_PERFORM + ", " + USER_CONFIRM + ", "
				+ DESCRIPTION1 + ", " + DESCRIPTION2 + ", " + // 10
				REMARKS + ", " + TIME_CREATED + ", " + TIME_SCHEDULED + ", " + TIME_COMPLETED + ", " + STATE + ", "
				+ STATUS + // 16
				") values (" + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," + " ?, ?, ?, ?, ?, ? )";
		PreparedStatement prepStmt = con.prepareStatement(insertStatement);
		try
		{
			prepStmt.setLong(1, nextPKId.longValue());
			prepStmt.setString(2, newObj.module);
			prepStmt.setString(3, newObj.processType);
			prepStmt.setString(4, newObj.category);
			prepStmt.setInt(5, newObj.auditLevel.intValue());
			prepStmt.setInt(6, newObj.userCreate.intValue());
			prepStmt.setInt(7, newObj.userPerform.intValue());
			prepStmt.setInt(8, newObj.userConfirm.intValue());
			prepStmt.setString(9, newObj.description1);
			prepStmt.setString(10, newObj.description2);
			prepStmt.setString(11, newObj.remarks);
			prepStmt.setTimestamp(12, newObj.timeCreated);
			prepStmt.setTimestamp(13, newObj.timeScheduled);
			prepStmt.setTimestamp(14, newObj.timeCompleted);
			prepStmt.setString(15, newObj.state);
			prepStmt.setString(16, newObj.status);
			prepStmt.executeUpdate();
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving insertObject: ");
		return nextPKId;
	}

	private boolean selectByPrimaryKey(Long primaryKey) throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " selectByPrimaryKey: ");
		makeConnection();
		String selectStatement = "select " + PKID + " from " + TABLENAME + " where " + PKID + " = ?";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setLong(1, primaryKey.longValue());
		ResultSet rs = prepStmt.executeQuery();
		boolean result = rs.next();
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving selectByPrimaryKey:");
		return result;
	}

	private void deleteObject(Long pkid) throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " deleteObject: ");
		makeConnection();
		String deleteStatement = " DELETE FROM " + TABLENAME + "  WHERE " + PKID + " = ?";
		PreparedStatement prepStmt = con.prepareStatement(deleteStatement);
		prepStmt.setLong(1, pkid.longValue());
		prepStmt.executeUpdate();
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving deleteObject: ");
	}

	// ///////////////////////////////////////////////////////////////////
	private void loadObject() throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " loadObject: ");
		makeConnection();
		String selectStatement = "	SELECT * " + " FROM " + TABLENAME + " WHERE " + PKID + " = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setLong(1, this.valObj.pkid.longValue());
		ResultSet rs = prepStmt.executeQuery();
		if (rs.next())
		{
			try
			{
				this.valObj = getObject(rs, "");
			} catch (Exception ex)
			{
				ex.printStackTrace();
			} finally
			{
				prepStmt.close();
			}
		} else
		{
			prepStmt.close();
			throw new NoSuchEntityException("Row for pkid " + this.valObj.pkid.toString() + " not found in database.");
		}
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving loadObject: ");
	}

	private void storeObject() throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " storeObject ");
		makeConnection();
		int rowCount = 0;
		try
		{
			String updateStatement = " UPDATE " + TABLENAME + " SET " + PKID + " = ?, " + MODULE + " = ?, "
					+ PROCESS_TYPE + " = ?, " + CATEGORY + " = ?, " + AUDIT_LEVEL + " = ?, " + USER_CREATE + " = ?, "
					+ USER_PERFORM + " = ?, " + USER_CONFIRM + " = ?, " + DESCRIPTION1 + " = ?, " + DESCRIPTION2
					+ " = ?, " + // 10
					REMARKS + " = ?, " + TIME_CREATED + " = ?, " + TIME_SCHEDULED + " = ?, " + TIME_COMPLETED
					+ " = ?, " + STATE + " = ?, " + STATUS + " = ? " + " WHERE " + PKID + " = ?";
			Log.printVerbose("updateStatement = " + updateStatement);
			PreparedStatement prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setLong(1, this.valObj.pkid.longValue());
			prepStmt.setString(2, this.valObj.module);
			prepStmt.setString(3, this.valObj.processType);
			prepStmt.setString(4, this.valObj.category);
			prepStmt.setInt(5, this.valObj.auditLevel.intValue());
			prepStmt.setInt(6, this.valObj.userCreate.intValue());
			prepStmt.setInt(7, this.valObj.userPerform.intValue());
			prepStmt.setInt(8, this.valObj.userConfirm.intValue());
			prepStmt.setString(9, this.valObj.description1);
			prepStmt.setString(10, this.valObj.description2);
			prepStmt.setString(11, this.valObj.remarks);
			prepStmt.setTimestamp(12, this.valObj.timeCreated);
			prepStmt.setTimestamp(13, this.valObj.timeScheduled);
			prepStmt.setTimestamp(14, this.valObj.timeCompleted);
			prepStmt.setString(15, this.valObj.state);
			prepStmt.setString(16, this.valObj.status);
			prepStmt.setLong(17, this.valObj.pkid.longValue());
			rowCount = prepStmt.executeUpdate();
			prepStmt.close();
		} catch (SQLException ex2)
		{
			ex2.printStackTrace();
			throw ex2;
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		closeConnection();
		if (rowCount == 0)
		{
			throw new EJBException("Storing row for pkid " + this.valObj.pkid + " failed.");
		}
		Log.printVerbose(strObjectName + " Leaving storeObject: ");
	}

	private Collection selectAll() throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " selectAll: ");
		makeConnection();
		String selectStatement = "select " + PKID + " from " + TABLENAME;
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		ArrayList pkIdList = new ArrayList();
		while (rs.next())
		{
			pkIdList.add(new Long(rs.getLong(1)));
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving selectAll: ");
		return pkIdList;
	}

	// ///////////////////////////////////////////////////////////////////
	private Collection selectObjects(QueryObject query) throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " loadObject: ");
		Collection result = new Vector();
		makeConnection();
		String selectStatement = "	SELECT * " + " FROM " + TABLENAME;
		selectStatement = query.appendQuery(selectStatement);
		Log.printVerbose("select stmt = " + selectStatement);
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		while (rs.next())
		{
			try
			{
				DocumentProcessingObject catObj = getObject(rs, "");
				result.add(catObj);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving loadObject: ");
		return result;
	}

	// ///////////////////////////////////////////////////////////////////
	private int selectCount(QueryObject query) throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " loadObject: ");
		int result = 0;
		makeConnection();
		String selectStatement = " SELECT COUNT(" + PKID + ") AS count " + " FROM " + TABLENAME;
		selectStatement = query.appendCount(selectStatement);
		Log.printVerbose("selectStatement = " + selectStatement);
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		if (rs.next())
		{
			try
			{
				result = rs.getInt("count");
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving loadObject: ");
		return result;
	}

	public static DocumentProcessingObject getObject(ResultSet rs, String prefix) throws Exception
	{
		DocumentProcessingObject theObj = null;
		try
		{
			theObj = new DocumentProcessingObject();
			theObj.pkid = new Long(rs.getLong(prefix + PKID)); // primary key
			theObj.module = rs.getString(prefix + MODULE);
			theObj.processType = rs.getString(prefix + PROCESS_TYPE);
			theObj.category = rs.getString(prefix + CATEGORY);
			theObj.auditLevel = new Integer(rs.getInt(prefix + AUDIT_LEVEL));
			theObj.userCreate = new Integer(rs.getInt(prefix + USER_CREATE));
			theObj.userPerform = new Integer(rs.getInt(prefix + USER_PERFORM));
			theObj.userConfirm = new Integer(rs.getInt(prefix + USER_CONFIRM));
			theObj.description1 = rs.getString(prefix + DESCRIPTION1);
			theObj.description2 = rs.getString(prefix + DESCRIPTION2);
			theObj.remarks = rs.getString(prefix + REMARKS);
			theObj.timeCreated = rs.getTimestamp(prefix + TIME_CREATED);
			theObj.timeScheduled = rs.getTimestamp(prefix + TIME_SCHEDULED);
			theObj.timeCompleted = rs.getTimestamp(prefix + TIME_COMPLETED);
			theObj.state = rs.getString(prefix + STATE);
			theObj.status = rs.getString(prefix + STATUS);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		return theObj;
	}

	// ============ Private methods ==========================
	private static synchronized Long getNextPKId(Connection con) throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, PKID, TABLENAME, MODULENAME, (Long) null);
	}
} // ObjectBean
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.accounting;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.math.BigDecimal;
import javax.ejb.*;
import javax.naming.*;
import com.vlee.local.*;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.customer.*;

public class DocumentProcessingBean implements EntityBean
{
	private static String strClassName = "DocumentProcessingBean";
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String MODULE = "module";
	public static final String PROCESS_TYPE = "process_type";
	public static final String CATEGORY = "category";
	public static final String AUDIT_LEVEL = "audit_level";
	public static final String USER_CREATE = "user_create";
	public static final String USER_PERFORM = "user_perform";
	public static final String USER_CONFIRM = "user_confirm";
	public static final String DESCRIPTION1 = "description1";
	public static final String DESCRIPTION2 = "description2";
	public static final String REMARKS = "remarks";
	public static final String TIME_CREATED = "time_created";
	public static final String TIME_SCHEDULED = "time_scheduled";
	public static final String TIME_COMPLETED = "time_completed";
	public static final String STATE = "state";
	public static final String STATUS = "status";
	// Constants for STATUS
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	public static final String STATUS_CANCELLED = "cancelled";
	// Constants for STATE
	public static final String STATE_CREATED = "CREATED";
	public static final String STATE_SCHEDULED = "SCHEDULED";
	public static final String STATE_PENDING = "PENDING";
	public static final String STATE_CLOSED = "CLOSED";
	public static final String STATE_CANCEL = "CANCEL";
	public static final String MODULE_DEFAULT = "";
	public static final String MODULE_DISTRIBUTION = "DISTRI";
	public static final String PROCESS_DEFAULT = "";
	public static final String PROCESS_DEBT_COLLECTION = "DEBT_COLL";
	// Attributes of Object
	private DocumentProcessingObject valObj;
	// DB Connection attributes
	private Connection con = null;
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_document_processing";
	// Other params
	private static final String strObjectName = "DocumentProcessingBean: ";
	public static final String MODULENAME = "acc";
	// EntityContext
	private EntityContext context = null;

	public DocumentProcessingObject getObject()
	{
		return this.valObj;
	}

	public void setObject(DocumentProcessingObject newObj)
	{
		// / must preserve the primary key
		Long pkid = this.valObj.pkid;
		this.valObj = newObj;
		this.valObj.pkid = pkid;
	}

	public Long getPkid()
	{
		return this.valObj.pkid;
	}

	public void setPkid(Long pkid)
	{
		this.valObj.pkid = pkid;
	}

	/***************************************************************************
	 * ejbCreate
	 **************************************************************************/
	public Long ejbCreate(DocumentProcessingObject newObj) throws CreateException
	{
		Long newKey = null;
		Log.printVerbose(strObjectName + "in ejbCreate");
		try
		{
			newKey = insertObject(newObj);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (newKey != null)
		{
			this.valObj = newObj;
			this.valObj.pkid = newKey;
		}
		Log.printVerbose(strObjectName + "about to leave ejbCreate");
		return newKey;
	}

	/***************************************************************************
	 * ejbFindByPrimaryKey
	 **************************************************************************/
	public Long ejbFindByPrimaryKey(Long primaryKey) throws FinderException
	{
		Log.printVerbose(strObjectName + "in ejbFindByPrimaryKey");
		boolean result;
		try
		{
			result = selectByPrimaryKey(primaryKey);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		if (result)
		{
			return primaryKey;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + primaryKey.toString() + "not found.");
		}
	}

	/***************************************************************************
	 * ejbFindAllObjects
	 **************************************************************************/
	public Collection ejbFindAllObjects() throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindAllObjects");
		Collection result;
		try
		{
			result = selectAll();
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindAllObjects: " + ex.getMessage());
		}
		return result;
	}

	/***************************************************************************
	 * ejbRemove
	 **************************************************************************/
	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		try
		{
			deleteObject(this.valObj.pkid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
	}

	/***************************************************************************
	 * setEntityContext
	 **************************************************************************/
	public void setEntityContext(EntityContext context)
	{
		Log.printVerbose(strObjectName + " In setEntityContext");
		this.context = context;
	}

	/***************************************************************************
	 * unsetEntityContext
	 **************************************************************************/
	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.context = null;
	}

	/***************************************************************************
	 * ejbActivate
	 **************************************************************************/
	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.valObj = new DocumentProcessingObject();
		this.valObj.pkid = (Long) context.getPrimaryKey();
	}

	/***************************************************************************
	 * ejbPassivate
	 **************************************************************************/
	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.valObj = null;
	}

	/***************************************************************************
	 * ejbLoad
	 **************************************************************************/
	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadObject();
		} catch (Exception ex)
		{
			throw new EJBException("ejbLoad: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbLoad");
	}

	/***************************************************************************
	 * ejbStore
	 **************************************************************************/
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

	/***************************************************************************
	 * ejbPostCreate
	 **************************************************************************/
	public void ejbPostCreate(DocumentProcessingObject newObj)
	{
	}

	public Collection ejbHomeGetObjects(QueryObject query)
	{
		Collection result = new Vector();
		try
		{
			result = selectObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}

	public int ejbHomeGetCount(QueryObject query)
	{
		int result = 0;
		try
		{
			result = selectCount(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}

	/** ********************* Database Routines ************************ */
	private void makeConnection() throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			con = ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void closeConnection() throws NamingException, SQLException
	{
		try
		{
			con.close();
		} catch (SQLException ex)
		{
			throw new EJBException("closeConnection: " + ex.getMessage());
		}
	}

	private Long insertObject(DocumentProcessingObject newObj) throws NamingException, SQLException
	{
		Long nextPKId = null;
		Log.printVerbose(strObjectName + " insertObject: ");
		makeConnection();
		try
		{
			nextPKId = getNextPKId(con);
		} catch (Exception ex)
		{
			throw new EJBException(strObjectName + ex.getMessage());
		}
		// makeConnection();
		newObj.pkid = nextPKId;
		String insertStatement = "insert into " + TABLENAME + "(" + PKID + ", " + MODULE + ", " + PROCESS_TYPE + ", "
				+ CATEGORY + ", " + AUDIT_LEVEL + ", " + USER_CREATE + ", " + USER_PERFORM + ", " + USER_CONFIRM + ", "
				+ DESCRIPTION1 + ", " + DESCRIPTION2 + ", " + // 10
				REMARKS + ", " + TIME_CREATED + ", " + TIME_SCHEDULED + ", " + TIME_COMPLETED + ", " + STATE + ", "
				+ STATUS + // 16
				") values (" + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," + " ?, ?, ?, ?, ?, ? )";
		PreparedStatement prepStmt = con.prepareStatement(insertStatement);
		try
		{
			prepStmt.setLong(1, nextPKId.longValue());
			prepStmt.setString(2, newObj.module);
			prepStmt.setString(3, newObj.processType);
			prepStmt.setString(4, newObj.category);
			prepStmt.setInt(5, newObj.auditLevel.intValue());
			prepStmt.setInt(6, newObj.userCreate.intValue());
			prepStmt.setInt(7, newObj.userPerform.intValue());
			prepStmt.setInt(8, newObj.userConfirm.intValue());
			prepStmt.setString(9, newObj.description1);
			prepStmt.setString(10, newObj.description2);
			prepStmt.setString(11, newObj.remarks);
			prepStmt.setTimestamp(12, newObj.timeCreated);
			prepStmt.setTimestamp(13, newObj.timeScheduled);
			prepStmt.setTimestamp(14, newObj.timeCompleted);
			prepStmt.setString(15, newObj.state);
			prepStmt.setString(16, newObj.status);
			prepStmt.executeUpdate();
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving insertObject: ");
		return nextPKId;
	}

	private boolean selectByPrimaryKey(Long primaryKey) throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " selectByPrimaryKey: ");
		makeConnection();
		String selectStatement = "select " + PKID + " from " + TABLENAME + " where " + PKID + " = ?";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setLong(1, primaryKey.longValue());
		ResultSet rs = prepStmt.executeQuery();
		boolean result = rs.next();
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving selectByPrimaryKey:");
		return result;
	}

	private void deleteObject(Long pkid) throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " deleteObject: ");
		makeConnection();
		String deleteStatement = " DELETE FROM " + TABLENAME + "  WHERE " + PKID + " = ?";
		PreparedStatement prepStmt = con.prepareStatement(deleteStatement);
		prepStmt.setLong(1, pkid.longValue());
		prepStmt.executeUpdate();
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving deleteObject: ");
	}

	// ///////////////////////////////////////////////////////////////////
	private void loadObject() throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " loadObject: ");
		makeConnection();
		String selectStatement = "	SELECT * " + " FROM " + TABLENAME + " WHERE " + PKID + " = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setLong(1, this.valObj.pkid.longValue());
		ResultSet rs = prepStmt.executeQuery();
		if (rs.next())
		{
			try
			{
				this.valObj = getObject(rs, "");
			} catch (Exception ex)
			{
				ex.printStackTrace();
			} finally
			{
				prepStmt.close();
			}
		} else
		{
			prepStmt.close();
			throw new NoSuchEntityException("Row for pkid " + this.valObj.pkid.toString() + " not found in database.");
		}
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving loadObject: ");
	}

	private void storeObject() throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " storeObject ");
		makeConnection();
		int rowCount = 0;
		try
		{
			String updateStatement = " UPDATE " + TABLENAME + " SET " + PKID + " = ?, " + MODULE + " = ?, "
					+ PROCESS_TYPE + " = ?, " + CATEGORY + " = ?, " + AUDIT_LEVEL + " = ?, " + USER_CREATE + " = ?, "
					+ USER_PERFORM + " = ?, " + USER_CONFIRM + " = ?, " + DESCRIPTION1 + " = ?, " + DESCRIPTION2
					+ " = ?, " + // 10
					REMARKS + " = ?, " + TIME_CREATED + " = ?, " + TIME_SCHEDULED + " = ?, " + TIME_COMPLETED
					+ " = ?, " + STATE + " = ?, " + STATUS + " = ? " + " WHERE " + PKID + " = ?";
			Log.printVerbose("updateStatement = " + updateStatement);
			PreparedStatement prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setLong(1, this.valObj.pkid.longValue());
			prepStmt.setString(2, this.valObj.module);
			prepStmt.setString(3, this.valObj.processType);
			prepStmt.setString(4, this.valObj.category);
			prepStmt.setInt(5, this.valObj.auditLevel.intValue());
			prepStmt.setInt(6, this.valObj.userCreate.intValue());
			prepStmt.setInt(7, this.valObj.userPerform.intValue());
			prepStmt.setInt(8, this.valObj.userConfirm.intValue());
			prepStmt.setString(9, this.valObj.description1);
			prepStmt.setString(10, this.valObj.description2);
			prepStmt.setString(11, this.valObj.remarks);
			prepStmt.setTimestamp(12, this.valObj.timeCreated);
			prepStmt.setTimestamp(13, this.valObj.timeScheduled);
			prepStmt.setTimestamp(14, this.valObj.timeCompleted);
			prepStmt.setString(15, this.valObj.state);
			prepStmt.setString(16, this.valObj.status);
			prepStmt.setLong(17, this.valObj.pkid.longValue());
			rowCount = prepStmt.executeUpdate();
			prepStmt.close();
		} catch (SQLException ex2)
		{
			ex2.printStackTrace();
			throw ex2;
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		closeConnection();
		if (rowCount == 0)
		{
			throw new EJBException("Storing row for pkid " + this.valObj.pkid + " failed.");
		}
		Log.printVerbose(strObjectName + " Leaving storeObject: ");
	}

	private Collection selectAll() throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " selectAll: ");
		makeConnection();
		String selectStatement = "select " + PKID + " from " + TABLENAME;
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		ArrayList pkIdList = new ArrayList();
		while (rs.next())
		{
			pkIdList.add(new Long(rs.getLong(1)));
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving selectAll: ");
		return pkIdList;
	}

	// ///////////////////////////////////////////////////////////////////
	private Collection selectObjects(QueryObject query) throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " loadObject: ");
		Collection result = new Vector();
		makeConnection();
		String selectStatement = "	SELECT * " + " FROM " + TABLENAME;
		selectStatement = query.appendQuery(selectStatement);
		Log.printVerbose("select stmt = " + selectStatement);
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		while (rs.next())
		{
			try
			{
				DocumentProcessingObject catObj = getObject(rs, "");
				result.add(catObj);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving loadObject: ");
		return result;
	}

	// ///////////////////////////////////////////////////////////////////
	private int selectCount(QueryObject query) throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " loadObject: ");
		int result = 0;
		makeConnection();
		String selectStatement = " SELECT COUNT(" + PKID + ") AS count " + " FROM " + TABLENAME;
		selectStatement = query.appendCount(selectStatement);
		Log.printVerbose("selectStatement = " + selectStatement);
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		if (rs.next())
		{
			try
			{
				result = rs.getInt("count");
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving loadObject: ");
		return result;
	}

	public static DocumentProcessingObject getObject(ResultSet rs, String prefix) throws Exception
	{
		DocumentProcessingObject theObj = null;
		try
		{
			theObj = new DocumentProcessingObject();
			theObj.pkid = new Long(rs.getLong(prefix + PKID)); // primary key
			theObj.module = rs.getString(prefix + MODULE);
			theObj.processType = rs.getString(prefix + PROCESS_TYPE);
			theObj.category = rs.getString(prefix + CATEGORY);
			theObj.auditLevel = new Integer(rs.getInt(prefix + AUDIT_LEVEL));
			theObj.userCreate = new Integer(rs.getInt(prefix + USER_CREATE));
			theObj.userPerform = new Integer(rs.getInt(prefix + USER_PERFORM));
			theObj.userConfirm = new Integer(rs.getInt(prefix + USER_CONFIRM));
			theObj.description1 = rs.getString(prefix + DESCRIPTION1);
			theObj.description2 = rs.getString(prefix + DESCRIPTION2);
			theObj.remarks = rs.getString(prefix + REMARKS);
			theObj.timeCreated = rs.getTimestamp(prefix + TIME_CREATED);
			theObj.timeScheduled = rs.getTimestamp(prefix + TIME_SCHEDULED);
			theObj.timeCompleted = rs.getTimestamp(prefix + TIME_COMPLETED);
			theObj.state = rs.getString(prefix + STATE);
			theObj.status = rs.getString(prefix + STATUS);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		return theObj;
	}

	// ============ Private methods ==========================
	private static synchronized Long getNextPKId(Connection con) throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, PKID, TABLENAME, MODULENAME, (Long) null);
	}
} // ObjectBean
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.wavelet.info)
 *
 * This software is the proprietary information of
 * Wavelet Solutions Sdn. Bhd.,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.accounting;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.math.BigDecimal;
import javax.ejb.*;
import javax.naming.*;
import com.vlee.local.*;
import com.vlee.util.*;
import com.vlee.ejb.supplier.*;
import com.vlee.ejb.customer.*;

public class DocumentProcessingBean implements EntityBean
{
	private static String strClassName = "DocumentProcessingBean";
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String MODULE = "module";
	public static final String PROCESS_TYPE = "process_type";
	public static final String CATEGORY = "category";
	public static final String AUDIT_LEVEL = "audit_level";
	public static final String USER_CREATE = "user_create";
	public static final String USER_PERFORM = "user_perform";
	public static final String USER_CONFIRM = "user_confirm";
	public static final String DESCRIPTION1 = "description1";
	public static final String DESCRIPTION2 = "description2";
	public static final String REMARKS = "remarks";
	public static final String TIME_CREATED = "time_created";
	public static final String TIME_SCHEDULED = "time_scheduled";
	public static final String TIME_COMPLETED = "time_completed";
	public static final String STATE = "state";
	public static final String STATUS = "status";
	// Constants for STATUS
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	public static final String STATUS_CANCELLED = "cancelled";
	// Constants for STATE
	public static final String STATE_CREATED = "CREATED";
	public static final String STATE_SCHEDULED = "SCHEDULED";
	public static final String STATE_PENDING = "PENDING";
	public static final String STATE_CLOSED = "CLOSED";
	public static final String STATE_CANCEL = "CANCEL";
	public static final String MODULE_DEFAULT = "";
	public static final String MODULE_DISTRIBUTION = "DISTRI";
	public static final String PROCESS_DEFAULT = "";
	public static final String PROCESS_DEBT_COLLECTION = "DEBT_COLL";
	// Attributes of Object
	private DocumentProcessingObject valObj;
	// DB Connection attributes
	private Connection con = null;
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_document_processing";
	// Other params
	private static final String strObjectName = "DocumentProcessingBean: ";
	public static final String MODULENAME = "acc";
	// EntityContext
	private EntityContext context = null;

	public DocumentProcessingObject getObject()
	{
		return this.valObj;
	}

	public void setObject(DocumentProcessingObject newObj)
	{
		// / must preserve the primary key
		Long pkid = this.valObj.pkid;
		this.valObj = newObj;
		this.valObj.pkid = pkid;
	}

	public Long getPkid()
	{
		return this.valObj.pkid;
	}

	public void setPkid(Long pkid)
	{
		this.valObj.pkid = pkid;
	}

	/***************************************************************************
	 * ejbCreate
	 **************************************************************************/
	public Long ejbCreate(DocumentProcessingObject newObj) throws CreateException
	{
		Long newKey = null;
		Log.printVerbose(strObjectName + "in ejbCreate");
		try
		{
			newKey = insertObject(newObj);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (newKey != null)
		{
			this.valObj = newObj;
			this.valObj.pkid = newKey;
		}
		Log.printVerbose(strObjectName + "about to leave ejbCreate");
		return newKey;
	}

	/***************************************************************************
	 * ejbFindByPrimaryKey
	 **************************************************************************/
	public Long ejbFindByPrimaryKey(Long primaryKey) throws FinderException
	{
		Log.printVerbose(strObjectName + "in ejbFindByPrimaryKey");
		boolean result;
		try
		{
			result = selectByPrimaryKey(primaryKey);
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindByPrimaryKey: " + ex.getMessage());
		}
		if (result)
		{
			return primaryKey;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + primaryKey.toString() + "not found.");
		}
	}

	/***************************************************************************
	 * ejbFindAllObjects
	 **************************************************************************/
	public Collection ejbFindAllObjects() throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindAllObjects");
		Collection result;
		try
		{
			result = selectAll();
		} catch (Exception ex)
		{
			throw new EJBException("ejbFindAllObjects: " + ex.getMessage());
		}
		return result;
	}

	/***************************************************************************
	 * ejbRemove
	 **************************************************************************/
	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		try
		{
			deleteObject(this.valObj.pkid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
	}

	/***************************************************************************
	 * setEntityContext
	 **************************************************************************/
	public void setEntityContext(EntityContext context)
	{
		Log.printVerbose(strObjectName + " In setEntityContext");
		this.context = context;
	}

	/***************************************************************************
	 * unsetEntityContext
	 **************************************************************************/
	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.context = null;
	}

	/***************************************************************************
	 * ejbActivate
	 **************************************************************************/
	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.valObj = new DocumentProcessingObject();
		this.valObj.pkid = (Long) context.getPrimaryKey();
	}

	/***************************************************************************
	 * ejbPassivate
	 **************************************************************************/
	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.valObj = null;
	}

	/***************************************************************************
	 * ejbLoad
	 **************************************************************************/
	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		try
		{
			loadObject();
		} catch (Exception ex)
		{
			throw new EJBException("ejbLoad: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + " Leaving ejbLoad");
	}

	/***************************************************************************
	 * ejbStore
	 **************************************************************************/
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

	/***************************************************************************
	 * ejbPostCreate
	 **************************************************************************/
	public void ejbPostCreate(DocumentProcessingObject newObj)
	{
	}

	public Collection ejbHomeGetObjects(QueryObject query)
	{
		Collection result = new Vector();
		try
		{
			result = selectObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}

	public int ejbHomeGetCount(QueryObject query)
	{
		int result = 0;
		try
		{
			result = selectCount(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}

	/** ********************* Database Routines ************************ */
	private void makeConnection() throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			con = ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void closeConnection() throws NamingException, SQLException
	{
		try
		{
			con.close();
		} catch (SQLException ex)
		{
			throw new EJBException("closeConnection: " + ex.getMessage());
		}
	}

	private Long insertObject(DocumentProcessingObject newObj) throws NamingException, SQLException
	{
		Long nextPKId = null;
		Log.printVerbose(strObjectName + " insertObject: ");
		makeConnection();
		try
		{
			nextPKId = getNextPKId(con);
		} catch (Exception ex)
		{
			throw new EJBException(strObjectName + ex.getMessage());
		}
		// makeConnection();
		newObj.pkid = nextPKId;
		String insertStatement = "insert into " + TABLENAME + "(" + PKID + ", " + MODULE + ", " + PROCESS_TYPE + ", "
				+ CATEGORY + ", " + AUDIT_LEVEL + ", " + USER_CREATE + ", " + USER_PERFORM + ", " + USER_CONFIRM + ", "
				+ DESCRIPTION1 + ", " + DESCRIPTION2 + ", " + // 10
				REMARKS + ", " + TIME_CREATED + ", " + TIME_SCHEDULED + ", " + TIME_COMPLETED + ", " + STATE + ", "
				+ STATUS + // 16
				") values (" + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," + " ?, ?, ?, ?, ?, ? )";
		PreparedStatement prepStmt = con.prepareStatement(insertStatement);
		try
		{
			prepStmt.setLong(1, nextPKId.longValue());
			prepStmt.setString(2, newObj.module);
			prepStmt.setString(3, newObj.processType);
			prepStmt.setString(4, newObj.category);
			prepStmt.setInt(5, newObj.auditLevel.intValue());
			prepStmt.setInt(6, newObj.userCreate.intValue());
			prepStmt.setInt(7, newObj.userPerform.intValue());
			prepStmt.setInt(8, newObj.userConfirm.intValue());
			prepStmt.setString(9, newObj.description1);
			prepStmt.setString(10, newObj.description2);
			prepStmt.setString(11, newObj.remarks);
			prepStmt.setTimestamp(12, newObj.timeCreated);
			prepStmt.setTimestamp(13, newObj.timeScheduled);
			prepStmt.setTimestamp(14, newObj.timeCompleted);
			prepStmt.setString(15, newObj.state);
			prepStmt.setString(16, newObj.status);
			prepStmt.executeUpdate();
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving insertObject: ");
		return nextPKId;
	}

	private boolean selectByPrimaryKey(Long primaryKey) throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " selectByPrimaryKey: ");
		makeConnection();
		String selectStatement = "select " + PKID + " from " + TABLENAME + " where " + PKID + " = ?";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setLong(1, primaryKey.longValue());
		ResultSet rs = prepStmt.executeQuery();
		boolean result = rs.next();
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving selectByPrimaryKey:");
		return result;
	}

	private void deleteObject(Long pkid) throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " deleteObject: ");
		makeConnection();
		String deleteStatement = " DELETE FROM " + TABLENAME + "  WHERE " + PKID + " = ?";
		PreparedStatement prepStmt = con.prepareStatement(deleteStatement);
		prepStmt.setLong(1, pkid.longValue());
		prepStmt.executeUpdate();
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving deleteObject: ");
	}

	// ///////////////////////////////////////////////////////////////////
	private void loadObject() throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " loadObject: ");
		makeConnection();
		String selectStatement = "	SELECT * " + " FROM " + TABLENAME + " WHERE " + PKID + " = ? ";
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		prepStmt.setLong(1, this.valObj.pkid.longValue());
		ResultSet rs = prepStmt.executeQuery();
		if (rs.next())
		{
			try
			{
				this.valObj = getObject(rs, "");
			} catch (Exception ex)
			{
				ex.printStackTrace();
			} finally
			{
				prepStmt.close();
			}
		} else
		{
			prepStmt.close();
			throw new NoSuchEntityException("Row for pkid " + this.valObj.pkid.toString() + " not found in database.");
		}
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving loadObject: ");
	}

	private void storeObject() throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " storeObject ");
		makeConnection();
		int rowCount = 0;
		try
		{
			String updateStatement = " UPDATE " + TABLENAME + " SET " + PKID + " = ?, " + MODULE + " = ?, "
					+ PROCESS_TYPE + " = ?, " + CATEGORY + " = ?, " + AUDIT_LEVEL + " = ?, " + USER_CREATE + " = ?, "
					+ USER_PERFORM + " = ?, " + USER_CONFIRM + " = ?, " + DESCRIPTION1 + " = ?, " + DESCRIPTION2
					+ " = ?, " + // 10
					REMARKS + " = ?, " + TIME_CREATED + " = ?, " + TIME_SCHEDULED + " = ?, " + TIME_COMPLETED
					+ " = ?, " + STATE + " = ?, " + STATUS + " = ? " + " WHERE " + PKID + " = ?";
			Log.printVerbose("updateStatement = " + updateStatement);
			PreparedStatement prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setLong(1, this.valObj.pkid.longValue());
			prepStmt.setString(2, this.valObj.module);
			prepStmt.setString(3, this.valObj.processType);
			prepStmt.setString(4, this.valObj.category);
			prepStmt.setInt(5, this.valObj.auditLevel.intValue());
			prepStmt.setInt(6, this.valObj.userCreate.intValue());
			prepStmt.setInt(7, this.valObj.userPerform.intValue());
			prepStmt.setInt(8, this.valObj.userConfirm.intValue());
			prepStmt.setString(9, this.valObj.description1);
			prepStmt.setString(10, this.valObj.description2);
			prepStmt.setString(11, this.valObj.remarks);
			prepStmt.setTimestamp(12, this.valObj.timeCreated);
			prepStmt.setTimestamp(13, this.valObj.timeScheduled);
			prepStmt.setTimestamp(14, this.valObj.timeCompleted);
			prepStmt.setString(15, this.valObj.state);
			prepStmt.setString(16, this.valObj.status);
			prepStmt.setLong(17, this.valObj.pkid.longValue());
			rowCount = prepStmt.executeUpdate();
			prepStmt.close();
		} catch (SQLException ex2)
		{
			ex2.printStackTrace();
			throw ex2;
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		closeConnection();
		if (rowCount == 0)
		{
			throw new EJBException("Storing row for pkid " + this.valObj.pkid + " failed.");
		}
		Log.printVerbose(strObjectName + " Leaving storeObject: ");
	}

	private Collection selectAll() throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " selectAll: ");
		makeConnection();
		String selectStatement = "select " + PKID + " from " + TABLENAME;
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		ArrayList pkIdList = new ArrayList();
		while (rs.next())
		{
			pkIdList.add(new Long(rs.getLong(1)));
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving selectAll: ");
		return pkIdList;
	}

	// ///////////////////////////////////////////////////////////////////
	private Collection selectObjects(QueryObject query) throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " loadObject: ");
		Collection result = new Vector();
		makeConnection();
		String selectStatement = "	SELECT * " + " FROM " + TABLENAME;
		selectStatement = query.appendQuery(selectStatement);
		Log.printVerbose("select stmt = " + selectStatement);
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		while (rs.next())
		{
			try
			{
				DocumentProcessingObject catObj = getObject(rs, "");
				result.add(catObj);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving loadObject: ");
		return result;
	}

	// ///////////////////////////////////////////////////////////////////
	private int selectCount(QueryObject query) throws NamingException, SQLException
	{
		Log.printVerbose(strObjectName + " loadObject: ");
		int result = 0;
		makeConnection();
		String selectStatement = " SELECT COUNT(" + PKID + ") AS count " + " FROM " + TABLENAME;
		selectStatement = query.appendCount(selectStatement);
		Log.printVerbose("selectStatement = " + selectStatement);
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		if (rs.next())
		{
			try
			{
				result = rs.getInt("count");
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		prepStmt.close();
		closeConnection();
		Log.printVerbose(strObjectName + " Leaving loadObject: ");
		return result;
	}

	public static DocumentProcessingObject getObject(ResultSet rs, String prefix) throws Exception
	{
		DocumentProcessingObject theObj = null;
		try
		{
			theObj = new DocumentProcessingObject();
			theObj.pkid = new Long(rs.getLong(prefix + PKID)); // primary key
			theObj.module = rs.getString(prefix + MODULE);
			theObj.processType = rs.getString(prefix + PROCESS_TYPE);
			theObj.category = rs.getString(prefix + CATEGORY);
			theObj.auditLevel = new Integer(rs.getInt(prefix + AUDIT_LEVEL));
			theObj.userCreate = new Integer(rs.getInt(prefix + USER_CREATE));
			theObj.userPerform = new Integer(rs.getInt(prefix + USER_PERFORM));
			theObj.userConfirm = new Integer(rs.getInt(prefix + USER_CONFIRM));
			theObj.description1 = rs.getString(prefix + DESCRIPTION1);
			theObj.description2 = rs.getString(prefix + DESCRIPTION2);
			theObj.remarks = rs.getString(prefix + REMARKS);
			theObj.timeCreated = rs.getTimestamp(prefix + TIME_CREATED);
			theObj.timeScheduled = rs.getTimestamp(prefix + TIME_SCHEDULED);
			theObj.timeCompleted = rs.getTimestamp(prefix + TIME_COMPLETED);
			theObj.state = rs.getString(prefix + STATE);
			theObj.status = rs.getString(prefix + STATUS);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		return theObj;
	}

	// ============ Private methods ==========================
	private static synchronized Long getNextPKId(Connection con) throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, PKID, TABLENAME, MODULENAME, (Long) null);
	}
} // ObjectBean
