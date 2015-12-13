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

public class DocumentProcessingItemBean implements EntityBean
{
	private static String strClassName = "DocumentProcessingItemBean";
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String MODULE = "module";
	public static final String PROCESS_TYPE = "process_type";
	public static final String CATEGORY = "category";
	public static final String AUDIT_LEVEL = "audit_level";
	public static final String PROCESS_ID = "process_id";
	public static final String USERID = "userid";
	public static final String DOC_REF = "doc_ref";
	public static final String DOC_ID = "doc_id";
	public static final String ENTITY_REF = "entity_ref";
	public static final String ENTITY_ID = "entity_id";
	public static final String DESCRIPTION1 = "description1";
	public static final String DESCRIPTION2 = "description2";
	public static final String REMARKS = "remarks";
	public static final String TIME = "time";
	public static final String STATE = "state";
	public static final String STATUS = "status";
	// Constants for MODULE
	public static final String MODULE_DEFAULT = DocumentProcessingBean.MODULE_DEFAULT;
	public static final String MODULE_DISTRIBUTION = DocumentProcessingBean.MODULE_DISTRIBUTION;
	// Constants for STATUS
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	public static final String STATUS_CANCELLED = "cancelled";
	// Constants for STATE
	public static final String STATE_CREATED = "CREATED";
	public static final String STATE_COMPLETE = "COMPLETE";
	public static final String STATE_CANCEL = "CANCEL";
	// / constants for MODULE
	public static final String PROCESS_DEFAULT = "";
	public static final String PROCESS_DEBT_COLLECTION = "DEBT_COLL";
	// Attributes of Object
	private DocumentProcessingItemObject valObj;
	// DB Connection attributes
	private Connection con = null;
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_document_processing_item";
	// Other params
	private static final String strObjectName = "DocumentProcessingItemBean: ";
	public static final String MODULENAME = "acc";
	// EntityContext
	private EntityContext context = null;

	public DocumentProcessingItemObject getObject()
	{
		return this.valObj;
	}

	public void setObject(DocumentProcessingItemObject newObj)
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
	public Long ejbCreate(DocumentProcessingItemObject newObj) throws CreateException
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
		this.valObj = new DocumentProcessingItemObject();
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
	public void ejbPostCreate(DocumentProcessingItemObject newObj)
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

	private Long insertObject(DocumentProcessingItemObject newObj) throws NamingException, SQLException
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
				+ CATEGORY + ", " + AUDIT_LEVEL + ", " + PROCESS_ID + ", " + USERID + ", " + DOC_REF + ", " + DOC_ID
				+ ", " + ENTITY_REF + ", " + ENTITY_ID + ", " + DESCRIPTION1 + ", " + DESCRIPTION2 + ", " + REMARKS
				+ ", " + TIME + ", " + STATE + ", " + STATUS + // 17
				") values (" + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," + " ?, ?, ?, ?, ?, ?, ? )";
		PreparedStatement prepStmt = con.prepareStatement(insertStatement);
		try
		{
			prepStmt.setLong(1, nextPKId.longValue());
			prepStmt.setString(2, newObj.module);
			prepStmt.setString(3, newObj.processType);
			prepStmt.setString(4, newObj.category);
			prepStmt.setInt(5, newObj.auditLevel.intValue());
			prepStmt.setLong(6, newObj.processId.longValue());
			prepStmt.setInt(7, newObj.userid.intValue());
			prepStmt.setString(8, newObj.docRef);
			prepStmt.setLong(9, newObj.docId.longValue());
			prepStmt.setString(10, newObj.entityRef);
			prepStmt.setInt(11, newObj.entityId.intValue());
			prepStmt.setString(12, newObj.description1);
			prepStmt.setString(13, newObj.description2);
			prepStmt.setString(14, newObj.remarks);
			prepStmt.setTimestamp(15, newObj.time);
			prepStmt.setString(16, newObj.state);
			prepStmt.setString(17, newObj.status);
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
		String selectStatement = " SELECT " + PKID + " FROM " + TABLENAME + " WHERE " + PKID + " = ? ";
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
					+ PROCESS_TYPE + " = ?, " + CATEGORY + " = ?, " + AUDIT_LEVEL + " = ?, " + PROCESS_ID + " = ?, "
					+ USERID + " = ?, " + DOC_REF + " = ?, " + DOC_ID + " = ?, " + ENTITY_REF + " = ?, " + // 10
					ENTITY_ID + " = ?, " + DESCRIPTION1 + " = ?, " + DESCRIPTION2 + " = ?, " + REMARKS + " = ?, "
					+ TIME + " = ?, " + STATE + " = ?, " + STATUS + " = ? " + " WHERE " + PKID + " = ?";
			Log.printVerbose("updateStatement = " + updateStatement);
			PreparedStatement prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setLong(1, this.valObj.pkid.longValue());
			prepStmt.setString(2, this.valObj.module);
			prepStmt.setString(3, this.valObj.processType);
			prepStmt.setString(4, this.valObj.category);
			prepStmt.setInt(5, this.valObj.auditLevel.intValue());
			prepStmt.setLong(6, this.valObj.processId.longValue());
			prepStmt.setInt(7, this.valObj.userid.intValue());
			prepStmt.setString(8, this.valObj.docRef);
			prepStmt.setLong(9, this.valObj.docId.longValue());
			prepStmt.setString(10, this.valObj.entityRef);
			prepStmt.setInt(11, this.valObj.entityId.intValue());
			prepStmt.setString(12, this.valObj.description1);
			prepStmt.setString(13, this.valObj.description2);
			prepStmt.setString(14, this.valObj.remarks);
			prepStmt.setTimestamp(15, this.valObj.time);
			prepStmt.setString(16, this.valObj.state);
			prepStmt.setString(17, this.valObj.status);
			prepStmt.setLong(18, this.valObj.pkid.longValue());
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
		String selectStatement = " SELECT " + PKID + " from " + TABLENAME;
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
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		while (rs.next())
		{
			try
			{
				DocumentProcessingItemObject catObj = getObject(rs, "");
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

	public static DocumentProcessingItemObject getObject(ResultSet rs, String prefix) throws Exception
	{
		DocumentProcessingItemObject theObj = null;
		try
		{
			theObj = new DocumentProcessingItemObject();
			theObj.pkid = new Long(rs.getLong(prefix + PKID)); // primary key
			theObj.module = rs.getString(prefix + MODULE);
			theObj.processType = rs.getString(prefix + PROCESS_TYPE);
			theObj.category = rs.getString(prefix + CATEGORY);
			theObj.auditLevel = new Integer(rs.getInt(prefix + AUDIT_LEVEL));
			theObj.processId = new Long(rs.getLong(prefix + PROCESS_ID));
			theObj.userid = new Integer(rs.getInt(prefix + USERID));
			theObj.docRef = rs.getString(prefix + DOC_REF);
			theObj.docId = new Long(rs.getLong(prefix + DOC_ID));
			theObj.entityRef = rs.getString(prefix + ENTITY_REF); // 10
			theObj.entityId = new Integer(rs.getInt(prefix + ENTITY_ID));
			theObj.description1 = rs.getString(prefix + DESCRIPTION1);
			theObj.description2 = rs.getString(prefix + DESCRIPTION2);
			theObj.remarks = rs.getString(prefix + REMARKS);
			theObj.time = rs.getTimestamp(prefix + TIME);
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

public class DocumentProcessingItemBean implements EntityBean
{
	private static String strClassName = "DocumentProcessingItemBean";
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String MODULE = "module";
	public static final String PROCESS_TYPE = "process_type";
	public static final String CATEGORY = "category";
	public static final String AUDIT_LEVEL = "audit_level";
	public static final String PROCESS_ID = "process_id";
	public static final String USERID = "userid";
	public static final String DOC_REF = "doc_ref";
	public static final String DOC_ID = "doc_id";
	public static final String ENTITY_REF = "entity_ref";
	public static final String ENTITY_ID = "entity_id";
	public static final String DESCRIPTION1 = "description1";
	public static final String DESCRIPTION2 = "description2";
	public static final String REMARKS = "remarks";
	public static final String TIME = "time";
	public static final String STATE = "state";
	public static final String STATUS = "status";
	// Constants for MODULE
	public static final String MODULE_DEFAULT = DocumentProcessingBean.MODULE_DEFAULT;
	public static final String MODULE_DISTRIBUTION = DocumentProcessingBean.MODULE_DISTRIBUTION;
	// Constants for STATUS
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	public static final String STATUS_CANCELLED = "cancelled";
	// Constants for STATE
	public static final String STATE_CREATED = "CREATED";
	public static final String STATE_COMPLETE = "COMPLETE";
	public static final String STATE_CANCEL = "CANCEL";
	// / constants for MODULE
	public static final String PROCESS_DEFAULT = "";
	public static final String PROCESS_DEBT_COLLECTION = "DEBT_COLL";
	// Attributes of Object
	private DocumentProcessingItemObject valObj;
	// DB Connection attributes
	private Connection con = null;
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_document_processing_item";
	// Other params
	private static final String strObjectName = "DocumentProcessingItemBean: ";
	public static final String MODULENAME = "acc";
	// EntityContext
	private EntityContext context = null;

	public DocumentProcessingItemObject getObject()
	{
		return this.valObj;
	}

	public void setObject(DocumentProcessingItemObject newObj)
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
	public Long ejbCreate(DocumentProcessingItemObject newObj) throws CreateException
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
		this.valObj = new DocumentProcessingItemObject();
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
	public void ejbPostCreate(DocumentProcessingItemObject newObj)
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

	private Long insertObject(DocumentProcessingItemObject newObj) throws NamingException, SQLException
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
				+ CATEGORY + ", " + AUDIT_LEVEL + ", " + PROCESS_ID + ", " + USERID + ", " + DOC_REF + ", " + DOC_ID
				+ ", " + ENTITY_REF + ", " + ENTITY_ID + ", " + DESCRIPTION1 + ", " + DESCRIPTION2 + ", " + REMARKS
				+ ", " + TIME + ", " + STATE + ", " + STATUS + // 17
				") values (" + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," + " ?, ?, ?, ?, ?, ?, ? )";
		PreparedStatement prepStmt = con.prepareStatement(insertStatement);
		try
		{
			prepStmt.setLong(1, nextPKId.longValue());
			prepStmt.setString(2, newObj.module);
			prepStmt.setString(3, newObj.processType);
			prepStmt.setString(4, newObj.category);
			prepStmt.setInt(5, newObj.auditLevel.intValue());
			prepStmt.setLong(6, newObj.processId.longValue());
			prepStmt.setInt(7, newObj.userid.intValue());
			prepStmt.setString(8, newObj.docRef);
			prepStmt.setLong(9, newObj.docId.longValue());
			prepStmt.setString(10, newObj.entityRef);
			prepStmt.setInt(11, newObj.entityId.intValue());
			prepStmt.setString(12, newObj.description1);
			prepStmt.setString(13, newObj.description2);
			prepStmt.setString(14, newObj.remarks);
			prepStmt.setTimestamp(15, newObj.time);
			prepStmt.setString(16, newObj.state);
			prepStmt.setString(17, newObj.status);
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
		String selectStatement = " SELECT " + PKID + " FROM " + TABLENAME + " WHERE " + PKID + " = ? ";
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
					+ PROCESS_TYPE + " = ?, " + CATEGORY + " = ?, " + AUDIT_LEVEL + " = ?, " + PROCESS_ID + " = ?, "
					+ USERID + " = ?, " + DOC_REF + " = ?, " + DOC_ID + " = ?, " + ENTITY_REF + " = ?, " + // 10
					ENTITY_ID + " = ?, " + DESCRIPTION1 + " = ?, " + DESCRIPTION2 + " = ?, " + REMARKS + " = ?, "
					+ TIME + " = ?, " + STATE + " = ?, " + STATUS + " = ? " + " WHERE " + PKID + " = ?";
			Log.printVerbose("updateStatement = " + updateStatement);
			PreparedStatement prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setLong(1, this.valObj.pkid.longValue());
			prepStmt.setString(2, this.valObj.module);
			prepStmt.setString(3, this.valObj.processType);
			prepStmt.setString(4, this.valObj.category);
			prepStmt.setInt(5, this.valObj.auditLevel.intValue());
			prepStmt.setLong(6, this.valObj.processId.longValue());
			prepStmt.setInt(7, this.valObj.userid.intValue());
			prepStmt.setString(8, this.valObj.docRef);
			prepStmt.setLong(9, this.valObj.docId.longValue());
			prepStmt.setString(10, this.valObj.entityRef);
			prepStmt.setInt(11, this.valObj.entityId.intValue());
			prepStmt.setString(12, this.valObj.description1);
			prepStmt.setString(13, this.valObj.description2);
			prepStmt.setString(14, this.valObj.remarks);
			prepStmt.setTimestamp(15, this.valObj.time);
			prepStmt.setString(16, this.valObj.state);
			prepStmt.setString(17, this.valObj.status);
			prepStmt.setLong(18, this.valObj.pkid.longValue());
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
		String selectStatement = " SELECT " + PKID + " from " + TABLENAME;
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
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		while (rs.next())
		{
			try
			{
				DocumentProcessingItemObject catObj = getObject(rs, "");
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

	public static DocumentProcessingItemObject getObject(ResultSet rs, String prefix) throws Exception
	{
		DocumentProcessingItemObject theObj = null;
		try
		{
			theObj = new DocumentProcessingItemObject();
			theObj.pkid = new Long(rs.getLong(prefix + PKID)); // primary key
			theObj.module = rs.getString(prefix + MODULE);
			theObj.processType = rs.getString(prefix + PROCESS_TYPE);
			theObj.category = rs.getString(prefix + CATEGORY);
			theObj.auditLevel = new Integer(rs.getInt(prefix + AUDIT_LEVEL));
			theObj.processId = new Long(rs.getLong(prefix + PROCESS_ID));
			theObj.userid = new Integer(rs.getInt(prefix + USERID));
			theObj.docRef = rs.getString(prefix + DOC_REF);
			theObj.docId = new Long(rs.getLong(prefix + DOC_ID));
			theObj.entityRef = rs.getString(prefix + ENTITY_REF); // 10
			theObj.entityId = new Integer(rs.getInt(prefix + ENTITY_ID));
			theObj.description1 = rs.getString(prefix + DESCRIPTION1);
			theObj.description2 = rs.getString(prefix + DESCRIPTION2);
			theObj.remarks = rs.getString(prefix + REMARKS);
			theObj.time = rs.getTimestamp(prefix + TIME);
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

public class DocumentProcessingItemBean implements EntityBean
{
	private static String strClassName = "DocumentProcessingItemBean";
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String MODULE = "module";
	public static final String PROCESS_TYPE = "process_type";
	public static final String CATEGORY = "category";
	public static final String AUDIT_LEVEL = "audit_level";
	public static final String PROCESS_ID = "process_id";
	public static final String USERID = "userid";
	public static final String DOC_REF = "doc_ref";
	public static final String DOC_ID = "doc_id";
	public static final String ENTITY_REF = "entity_ref";
	public static final String ENTITY_ID = "entity_id";
	public static final String DESCRIPTION1 = "description1";
	public static final String DESCRIPTION2 = "description2";
	public static final String REMARKS = "remarks";
	public static final String TIME = "time";
	public static final String STATE = "state";
	public static final String STATUS = "status";
	// Constants for MODULE
	public static final String MODULE_DEFAULT = DocumentProcessingBean.MODULE_DEFAULT;
	public static final String MODULE_DISTRIBUTION = DocumentProcessingBean.MODULE_DISTRIBUTION;
	// Constants for STATUS
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	public static final String STATUS_CANCELLED = "cancelled";
	// Constants for STATE
	public static final String STATE_CREATED = "CREATED";
	public static final String STATE_COMPLETE = "COMPLETE";
	public static final String STATE_CANCEL = "CANCEL";
	// / constants for MODULE
	public static final String PROCESS_DEFAULT = "";
	public static final String PROCESS_DEBT_COLLECTION = "DEBT_COLL";
	// Attributes of Object
	private DocumentProcessingItemObject valObj;
	// DB Connection attributes
	private Connection con = null;
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_document_processing_item";
	// Other params
	private static final String strObjectName = "DocumentProcessingItemBean: ";
	public static final String MODULENAME = "acc";
	// EntityContext
	private EntityContext context = null;

	public DocumentProcessingItemObject getObject()
	{
		return this.valObj;
	}

	public void setObject(DocumentProcessingItemObject newObj)
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
	public Long ejbCreate(DocumentProcessingItemObject newObj) throws CreateException
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
		this.valObj = new DocumentProcessingItemObject();
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
	public void ejbPostCreate(DocumentProcessingItemObject newObj)
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

	private Long insertObject(DocumentProcessingItemObject newObj) throws NamingException, SQLException
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
				+ CATEGORY + ", " + AUDIT_LEVEL + ", " + PROCESS_ID + ", " + USERID + ", " + DOC_REF + ", " + DOC_ID
				+ ", " + ENTITY_REF + ", " + ENTITY_ID + ", " + DESCRIPTION1 + ", " + DESCRIPTION2 + ", " + REMARKS
				+ ", " + TIME + ", " + STATE + ", " + STATUS + // 17
				") values (" + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," + " ?, ?, ?, ?, ?, ?, ? )";
		PreparedStatement prepStmt = con.prepareStatement(insertStatement);
		try
		{
			prepStmt.setLong(1, nextPKId.longValue());
			prepStmt.setString(2, newObj.module);
			prepStmt.setString(3, newObj.processType);
			prepStmt.setString(4, newObj.category);
			prepStmt.setInt(5, newObj.auditLevel.intValue());
			prepStmt.setLong(6, newObj.processId.longValue());
			prepStmt.setInt(7, newObj.userid.intValue());
			prepStmt.setString(8, newObj.docRef);
			prepStmt.setLong(9, newObj.docId.longValue());
			prepStmt.setString(10, newObj.entityRef);
			prepStmt.setInt(11, newObj.entityId.intValue());
			prepStmt.setString(12, newObj.description1);
			prepStmt.setString(13, newObj.description2);
			prepStmt.setString(14, newObj.remarks);
			prepStmt.setTimestamp(15, newObj.time);
			prepStmt.setString(16, newObj.state);
			prepStmt.setString(17, newObj.status);
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
		String selectStatement = " SELECT " + PKID + " FROM " + TABLENAME + " WHERE " + PKID + " = ? ";
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
					+ PROCESS_TYPE + " = ?, " + CATEGORY + " = ?, " + AUDIT_LEVEL + " = ?, " + PROCESS_ID + " = ?, "
					+ USERID + " = ?, " + DOC_REF + " = ?, " + DOC_ID + " = ?, " + ENTITY_REF + " = ?, " + // 10
					ENTITY_ID + " = ?, " + DESCRIPTION1 + " = ?, " + DESCRIPTION2 + " = ?, " + REMARKS + " = ?, "
					+ TIME + " = ?, " + STATE + " = ?, " + STATUS + " = ? " + " WHERE " + PKID + " = ?";
			Log.printVerbose("updateStatement = " + updateStatement);
			PreparedStatement prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setLong(1, this.valObj.pkid.longValue());
			prepStmt.setString(2, this.valObj.module);
			prepStmt.setString(3, this.valObj.processType);
			prepStmt.setString(4, this.valObj.category);
			prepStmt.setInt(5, this.valObj.auditLevel.intValue());
			prepStmt.setLong(6, this.valObj.processId.longValue());
			prepStmt.setInt(7, this.valObj.userid.intValue());
			prepStmt.setString(8, this.valObj.docRef);
			prepStmt.setLong(9, this.valObj.docId.longValue());
			prepStmt.setString(10, this.valObj.entityRef);
			prepStmt.setInt(11, this.valObj.entityId.intValue());
			prepStmt.setString(12, this.valObj.description1);
			prepStmt.setString(13, this.valObj.description2);
			prepStmt.setString(14, this.valObj.remarks);
			prepStmt.setTimestamp(15, this.valObj.time);
			prepStmt.setString(16, this.valObj.state);
			prepStmt.setString(17, this.valObj.status);
			prepStmt.setLong(18, this.valObj.pkid.longValue());
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
		String selectStatement = " SELECT " + PKID + " from " + TABLENAME;
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
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		while (rs.next())
		{
			try
			{
				DocumentProcessingItemObject catObj = getObject(rs, "");
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

	public static DocumentProcessingItemObject getObject(ResultSet rs, String prefix) throws Exception
	{
		DocumentProcessingItemObject theObj = null;
		try
		{
			theObj = new DocumentProcessingItemObject();
			theObj.pkid = new Long(rs.getLong(prefix + PKID)); // primary key
			theObj.module = rs.getString(prefix + MODULE);
			theObj.processType = rs.getString(prefix + PROCESS_TYPE);
			theObj.category = rs.getString(prefix + CATEGORY);
			theObj.auditLevel = new Integer(rs.getInt(prefix + AUDIT_LEVEL));
			theObj.processId = new Long(rs.getLong(prefix + PROCESS_ID));
			theObj.userid = new Integer(rs.getInt(prefix + USERID));
			theObj.docRef = rs.getString(prefix + DOC_REF);
			theObj.docId = new Long(rs.getLong(prefix + DOC_ID));
			theObj.entityRef = rs.getString(prefix + ENTITY_REF); // 10
			theObj.entityId = new Integer(rs.getInt(prefix + ENTITY_ID));
			theObj.description1 = rs.getString(prefix + DESCRIPTION1);
			theObj.description2 = rs.getString(prefix + DESCRIPTION2);
			theObj.remarks = rs.getString(prefix + REMARKS);
			theObj.time = rs.getTimestamp(prefix + TIME);
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

public class DocumentProcessingItemBean implements EntityBean
{
	private static String strClassName = "DocumentProcessingItemBean";
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String MODULE = "module";
	public static final String PROCESS_TYPE = "process_type";
	public static final String CATEGORY = "category";
	public static final String AUDIT_LEVEL = "audit_level";
	public static final String PROCESS_ID = "process_id";
	public static final String USERID = "userid";
	public static final String DOC_REF = "doc_ref";
	public static final String DOC_ID = "doc_id";
	public static final String ENTITY_REF = "entity_ref";
	public static final String ENTITY_ID = "entity_id";
	public static final String DESCRIPTION1 = "description1";
	public static final String DESCRIPTION2 = "description2";
	public static final String REMARKS = "remarks";
	public static final String TIME = "time";
	public static final String STATE = "state";
	public static final String STATUS = "status";
	// Constants for MODULE
	public static final String MODULE_DEFAULT = DocumentProcessingBean.MODULE_DEFAULT;
	public static final String MODULE_DISTRIBUTION = DocumentProcessingBean.MODULE_DISTRIBUTION;
	// Constants for STATUS
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	public static final String STATUS_CANCELLED = "cancelled";
	// Constants for STATE
	public static final String STATE_CREATED = "CREATED";
	public static final String STATE_COMPLETE = "COMPLETE";
	public static final String STATE_CANCEL = "CANCEL";
	// / constants for MODULE
	public static final String PROCESS_DEFAULT = "";
	public static final String PROCESS_DEBT_COLLECTION = "DEBT_COLL";
	// Attributes of Object
	private DocumentProcessingItemObject valObj;
	// DB Connection attributes
	private Connection con = null;
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_document_processing_item";
	// Other params
	private static final String strObjectName = "DocumentProcessingItemBean: ";
	public static final String MODULENAME = "acc";
	// EntityContext
	private EntityContext context = null;

	public DocumentProcessingItemObject getObject()
	{
		return this.valObj;
	}

	public void setObject(DocumentProcessingItemObject newObj)
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
	public Long ejbCreate(DocumentProcessingItemObject newObj) throws CreateException
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
		this.valObj = new DocumentProcessingItemObject();
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
	public void ejbPostCreate(DocumentProcessingItemObject newObj)
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

	private Long insertObject(DocumentProcessingItemObject newObj) throws NamingException, SQLException
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
				+ CATEGORY + ", " + AUDIT_LEVEL + ", " + PROCESS_ID + ", " + USERID + ", " + DOC_REF + ", " + DOC_ID
				+ ", " + ENTITY_REF + ", " + ENTITY_ID + ", " + DESCRIPTION1 + ", " + DESCRIPTION2 + ", " + REMARKS
				+ ", " + TIME + ", " + STATE + ", " + STATUS + // 17
				") values (" + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," + " ?, ?, ?, ?, ?, ?, ? )";
		PreparedStatement prepStmt = con.prepareStatement(insertStatement);
		try
		{
			prepStmt.setLong(1, nextPKId.longValue());
			prepStmt.setString(2, newObj.module);
			prepStmt.setString(3, newObj.processType);
			prepStmt.setString(4, newObj.category);
			prepStmt.setInt(5, newObj.auditLevel.intValue());
			prepStmt.setLong(6, newObj.processId.longValue());
			prepStmt.setInt(7, newObj.userid.intValue());
			prepStmt.setString(8, newObj.docRef);
			prepStmt.setLong(9, newObj.docId.longValue());
			prepStmt.setString(10, newObj.entityRef);
			prepStmt.setInt(11, newObj.entityId.intValue());
			prepStmt.setString(12, newObj.description1);
			prepStmt.setString(13, newObj.description2);
			prepStmt.setString(14, newObj.remarks);
			prepStmt.setTimestamp(15, newObj.time);
			prepStmt.setString(16, newObj.state);
			prepStmt.setString(17, newObj.status);
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
		String selectStatement = " SELECT " + PKID + " FROM " + TABLENAME + " WHERE " + PKID + " = ? ";
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
					+ PROCESS_TYPE + " = ?, " + CATEGORY + " = ?, " + AUDIT_LEVEL + " = ?, " + PROCESS_ID + " = ?, "
					+ USERID + " = ?, " + DOC_REF + " = ?, " + DOC_ID + " = ?, " + ENTITY_REF + " = ?, " + // 10
					ENTITY_ID + " = ?, " + DESCRIPTION1 + " = ?, " + DESCRIPTION2 + " = ?, " + REMARKS + " = ?, "
					+ TIME + " = ?, " + STATE + " = ?, " + STATUS + " = ? " + " WHERE " + PKID + " = ?";
			Log.printVerbose("updateStatement = " + updateStatement);
			PreparedStatement prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setLong(1, this.valObj.pkid.longValue());
			prepStmt.setString(2, this.valObj.module);
			prepStmt.setString(3, this.valObj.processType);
			prepStmt.setString(4, this.valObj.category);
			prepStmt.setInt(5, this.valObj.auditLevel.intValue());
			prepStmt.setLong(6, this.valObj.processId.longValue());
			prepStmt.setInt(7, this.valObj.userid.intValue());
			prepStmt.setString(8, this.valObj.docRef);
			prepStmt.setLong(9, this.valObj.docId.longValue());
			prepStmt.setString(10, this.valObj.entityRef);
			prepStmt.setInt(11, this.valObj.entityId.intValue());
			prepStmt.setString(12, this.valObj.description1);
			prepStmt.setString(13, this.valObj.description2);
			prepStmt.setString(14, this.valObj.remarks);
			prepStmt.setTimestamp(15, this.valObj.time);
			prepStmt.setString(16, this.valObj.state);
			prepStmt.setString(17, this.valObj.status);
			prepStmt.setLong(18, this.valObj.pkid.longValue());
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
		String selectStatement = " SELECT " + PKID + " from " + TABLENAME;
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
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		while (rs.next())
		{
			try
			{
				DocumentProcessingItemObject catObj = getObject(rs, "");
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

	public static DocumentProcessingItemObject getObject(ResultSet rs, String prefix) throws Exception
	{
		DocumentProcessingItemObject theObj = null;
		try
		{
			theObj = new DocumentProcessingItemObject();
			theObj.pkid = new Long(rs.getLong(prefix + PKID)); // primary key
			theObj.module = rs.getString(prefix + MODULE);
			theObj.processType = rs.getString(prefix + PROCESS_TYPE);
			theObj.category = rs.getString(prefix + CATEGORY);
			theObj.auditLevel = new Integer(rs.getInt(prefix + AUDIT_LEVEL));
			theObj.processId = new Long(rs.getLong(prefix + PROCESS_ID));
			theObj.userid = new Integer(rs.getInt(prefix + USERID));
			theObj.docRef = rs.getString(prefix + DOC_REF);
			theObj.docId = new Long(rs.getLong(prefix + DOC_ID));
			theObj.entityRef = rs.getString(prefix + ENTITY_REF); // 10
			theObj.entityId = new Integer(rs.getInt(prefix + ENTITY_ID));
			theObj.description1 = rs.getString(prefix + DESCRIPTION1);
			theObj.description2 = rs.getString(prefix + DESCRIPTION2);
			theObj.remarks = rs.getString(prefix + REMARKS);
			theObj.time = rs.getTimestamp(prefix + TIME);
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
