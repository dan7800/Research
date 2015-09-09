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
package com.vlee.ejb.customer;

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

public class MemberCardAuditTrailBean implements EntityBean
{
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String AUDIT_LEVEL = "audit_level";
	public static final String AUDIT_TYPE = "audit_type";
	public static final String CARD_PKID = "card_pkid";
	public static final String CARD_NO = "card_no";
	public static final String CARD_NAME = "card_name";
	public static final String BRANCH = "branch";
	public static final String PCCENTER = "pccenter";
	public static final String USER_TXN = "user_txn";
	public static final String DATE_TXN = "date_txn";
	public static final String DATE_CREATE = "date_create";
	public static final String DOC_REF1 = "doc_ref1";
	public static final String DOC_KEY1 = "doc_key1";
	public static final String DOC_REF2 = "doc_ref2";
	public static final String DOC_KEY2 = "doc_key2";
	public static final String AMOUNT_TXN = "amount_txn";
	public static final String AMOUNT_DELTA = "amount_delta";
	public static final String INFO1 = "info1";
	public static final String INFO2 = "info2";
	public static final String WARNING = "warning";
	public static final String STATE = "state";
	public static final String STATUS = "status";




	//// constants
	public static final String AUDIT_TYPE_CREATE = "CREATE";
	public static final String AUDIT_TYPE_RENEW = "RENEW";
	public static final String AUDIT_TYPE_REPLACE = "REPLACE";
	
	//// constants
	public static final String STATUS_ACTIVE = "act";
	public static final String STATUS_DELETED = "del";
	public static final String STATUS_INACTIVE = "ina";
	public static final String STATE_CREATED = "cre";
	public static final String STATE_CLOSED = "closed";
	
	public static final String MODULENAME = "customer";
	public static final Long PKID_START = new Long(100001);
	// Attributes of Object
	MemberCardAuditTrailObject valObj;
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "cust_membercard_audit_trail";
	private static final String strObjectName = "MemberCardAuditTrailBean: ";
	// EntityContext
	private EntityContext context = null;

	/***************************************************************************
	 * Getters
	 **************************************************************************/
	public MemberCardAuditTrailObject getObject()
	{
		return this.valObj;
	}

	public void setObject(MemberCardAuditTrailObject newVal)
	{
		Long pkid = this.valObj.pkid;
		this.valObj = newVal;
		this.valObj.pkid = pkid;
	}

	public Long getPkid()
	{
		return this.valObj.pkid;
	}

	public Long getPrimaryKey()
	{
		return this.valObj.pkid;
	}

	public void setPrimaryKey(Long pkid)
	{
		this.valObj.pkid = pkid;
	}

	/***************************************************************************
	 * ejbCreate
	 **************************************************************************/
	public Long ejbCreate(MemberCardAuditTrailObject newObj) throws CreateException
	{
		Log.printVerbose(strObjectName + "in ejbCreate");
		try
		{
			this.valObj = insertObject(newObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + "about to leave ejbCreate");
		return this.valObj.pkid;
	}

	/***************************************************************************
	 * ejbFindByPrimaryKey
	 **************************************************************************/
	public Long ejbFindByPrimaryKey(Long primaryKey) throws FinderException
	{
		Log.printVerbose(strObjectName + "in ejbFindByPrimaryKey");
		boolean result = false;
		try
		{
			result = selectByPrimaryKey(primaryKey);
		} catch (Exception ex)
		{
			ex.printStackTrace();
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
		try
		{
			this.valObj = new MemberCardAuditTrailObject();
			this.valObj.pkid = (Long) context.getPrimaryKey();
		} catch (Exception ex)
		{
			Log.printVerbose("the getmsg returns:" + ex.getMessage());
			ex.printStackTrace();
		}
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
			ex.printStackTrace();
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
	 * ejbPostCreate (1)
	 **************************************************************************/
	public void ejbPostCreate(MemberCardAuditTrailObject newObj)
	{
		// nothing
	}

	public Collection ejbHomeGetObjects(QueryObject query)
	{
		Collection col = null;
		try
		{
			col = selectObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return col;
	}

	/** ********************* Database Routines ************************ */
	private Connection makeConnection() throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			return ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void closeConnection(Connection con) throws NamingException, SQLException
	{
		try
		{
			if (!con.isClosed())
			{
				con.close();
			}
		} catch (SQLException ex)
		{
			throw new EJBException("closeConnection: " + ex.getMessage());
		}
	}

	private MemberCardAuditTrailObject insertObject(MemberCardAuditTrailObject newObj) throws NamingException,
			SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " insertObject: ");
			con = makeConnection();
			try
			{
				newObj.pkid = getNextPKId(con);
			} catch (Exception ex)
			{
				ex.printStackTrace();
				throw new EJBException(strObjectName + ex.getMessage());
			}
			try
			{
				String insertStatement = " INSERT INTO " + TABLENAME + "( " + PKID + ", " + AUDIT_LEVEL + ", "
						+ AUDIT_TYPE + ", " + CARD_PKID + ", " + CARD_NO + ", " + CARD_NAME + ", " + BRANCH + ", "
						+ PCCENTER + ", " + USER_TXN + ", " + DATE_TXN + ", " // 10
						+ DATE_CREATE + ", " + DOC_REF1 + ", " + DOC_KEY1 + ", " + DOC_REF2 + ", " + DOC_KEY2 + ", "
						+ AMOUNT_TXN + ", " + AMOUNT_DELTA + ", " + INFO1 + ", " + INFO2 + ", " + WARNING + ", " // 20
						+ STATE + ", " + STATUS + ") values ( " + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
						+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," + " ?, ? ) ";
				prepStmt = con.prepareStatement(insertStatement);
				prepStmt.setLong(1, newObj.pkid.longValue());
				prepStmt.setInt(2, newObj.auditLevel.intValue());
				prepStmt.setString(3, newObj.auditType);
				prepStmt.setLong(4, newObj.cardPkid.longValue());
				prepStmt.setString(5, newObj.cardNo);
				prepStmt.setString(6, newObj.cardName);
				prepStmt.setInt(7, newObj.branch.intValue());
				prepStmt.setInt(8, newObj.pcCenter.intValue());
				prepStmt.setInt(9, newObj.userTxn.intValue());
				prepStmt.setTimestamp(10, newObj.dateTxn);
				prepStmt.setTimestamp(11, newObj.dateCreate);
				prepStmt.setString(12, newObj.docRef1);
				prepStmt.setLong(13, newObj.docKey1.longValue());
				prepStmt.setString(14, newObj.docRef2);
				prepStmt.setLong(15, newObj.docKey2.longValue());
				prepStmt.setBigDecimal(16, newObj.amountTxn);
				prepStmt.setBigDecimal(17, newObj.amountDelta);
				prepStmt.setString(18, newObj.info1);
				prepStmt.setString(19, newObj.info2);
				prepStmt.setString(20, newObj.warning);
				prepStmt.setString(21, newObj.state);
				prepStmt.setString(22, newObj.status);
				prepStmt.executeUpdate();
				// prepStmt.close();
				// closeConnection(con);
				Log.printVerbose(strObjectName + " Leaving insertObject: ");
			} catch (SQLException ex)
			{
				// Rethrow exception
				ex.printStackTrace();
				throw ex;
			}
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			ex.printStackTrace();
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			closeConnection(con);
		}
		return newObj;
	}

	private boolean selectByPrimaryKey(Long primaryKey) throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectByPrimaryKey: ");
			con = makeConnection();
			String selectStmt = " SELECT " + PKID + " FROM " + TABLENAME + " WHERE " + PKID + " = ? ";
			prepStmt = con.prepareStatement(selectStmt);
			prepStmt.setLong(1, primaryKey.longValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = rs.next();
			Log.printVerbose(strObjectName + " Leaving selectByPrimaryKey:");
			return result;
		} catch (SQLException ex)
		{
			throw ex;
		} catch (Exception ex)
		{
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			if (prepStmt != null)
			{
				prepStmt.close();
			}
			closeConnection(con);
		}
	}

	private void deleteObject(Long pkid) throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " deleteObject: ");
			con = makeConnection();
			String deleteStatement = " DELETE FROM " + TABLENAME + " WHERE " + PKID + " = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setLong(1, pkid.longValue());
			prepStmt.executeUpdate();
		} catch (SQLException ex)
		{
			throw ex;
		} catch (Exception ex)
		{
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			if (prepStmt != null)
			{
				prepStmt.close();
			}
			closeConnection(con);
		}
	}

	// ////////////////////////////////////////////////
	private void loadObject() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " loadObject: ");
			con = makeConnection();
			String selectStmt = " SELECT * FROM " + TABLENAME + " WHERE " + PKID + " = ? ";
			prepStmt = con.prepareStatement(selectStmt);
			prepStmt.setLong(1, this.valObj.pkid.longValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.valObj = getObject(rs, "");
			} else
			{
				throw new NoSuchEntityException("Row for pkid " + this.valObj.pkid.toString()
						+ " not found in database.");
			}
			Log.printVerbose(strObjectName + " Leaving loadObject: ");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				prepStmt.close();
			}
			closeConnection(con);
		}
	}

	// ///////////////////////////////////////////////////////////////
	private void storeObject() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " storeObject ");
			con = makeConnection();
			String updateStatement = " UPDATE " + TABLENAME + " SET " + PKID + " = ?," + AUDIT_LEVEL + " = ?,"
					+ AUDIT_TYPE + " = ?," + CARD_PKID + " = ?," + CARD_NO + " = ?," + CARD_NAME + " = ?," + BRANCH
					+ " = ?," + PCCENTER + " = ?," + USER_TXN + " = ?," + DATE_TXN + " = ?," // 10
					+ DATE_CREATE + " = ?," + DOC_REF1 + " = ?," + DOC_KEY1 + " = ?," + DOC_REF2 + " = ?," + DOC_KEY2
					+ " = ?," + AMOUNT_TXN + " = ?," + AMOUNT_DELTA + " = ?," + INFO1 + " = ?," + INFO2 + " = ?,"
					+ WARNING + " = ?," // 20
					+ STATE + " = ?," + STATUS + " = ? " + " WHERE " + PKID + " = ? ";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setLong(1, this.valObj.pkid.longValue());
			prepStmt.setInt(2, this.valObj.auditLevel.intValue());
			prepStmt.setString(3, this.valObj.auditType);
			prepStmt.setLong(4, this.valObj.cardPkid.longValue());
			prepStmt.setString(5, this.valObj.cardNo);
			prepStmt.setString(6, this.valObj.cardName);
			prepStmt.setInt(7, this.valObj.branch.intValue());
			prepStmt.setInt(8, this.valObj.pcCenter.intValue());
			prepStmt.setInt(9, this.valObj.userTxn.intValue());
			prepStmt.setTimestamp(10, this.valObj.dateTxn);
			prepStmt.setTimestamp(11, this.valObj.dateCreate);
			prepStmt.setString(12, this.valObj.docRef1);
			prepStmt.setLong(13, this.valObj.docKey1.longValue());
			prepStmt.setString(14, this.valObj.docRef2);
			prepStmt.setLong(15, this.valObj.docKey2.longValue());
			prepStmt.setBigDecimal(16, this.valObj.amountTxn);
			prepStmt.setBigDecimal(17, this.valObj.amountDelta);
			prepStmt.setString(18, this.valObj.info1);
			prepStmt.setString(19, this.valObj.info2);
			prepStmt.setString(20, this.valObj.warning);
			prepStmt.setString(21, this.valObj.state);
			prepStmt.setString(22, this.valObj.status);
			prepStmt.setLong(23, this.valObj.pkid.longValue());
			int rowCount = prepStmt.executeUpdate();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row for pkid " + this.valObj.pkid + " failed.");
			}
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			if (prepStmt != null)
			{
				prepStmt.close();
			}
			closeConnection(con);
		}
	}




	private static synchronized Long getNextPKId(Connection con) throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, PKID, TABLENAME, MODULENAME, PKID_START);
	}

	private static synchronized Long getNextStmtNo(Connection con, String stmtType, Long pcCenter)
			throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, pcCenter.toString(), TABLENAME, stmtType, PKID_START);
	}

	public static MemberCardAuditTrailObject getObject(ResultSet rs, String prefix) throws Exception
	{
		MemberCardAuditTrailObject theObj = null;
		try
		{
			theObj = new MemberCardAuditTrailObject();
			theObj.pkid = new Long(rs.getLong(prefix + PKID)); // primary key
			theObj.auditLevel = new Integer(rs.getInt(prefix + AUDIT_LEVEL));
			theObj.auditType = rs.getString(prefix + AUDIT_TYPE);
			theObj.cardPkid = new Long(rs.getLong(prefix + CARD_PKID));
			theObj.cardNo = rs.getString(prefix + CARD_NO);
			theObj.cardName = rs.getString(prefix + CARD_NAME);
			theObj.branch = new Integer(rs.getInt(prefix + BRANCH));
			theObj.pcCenter = new Integer(rs.getInt(prefix + PCCENTER));
			theObj.userTxn = new Integer(rs.getInt(prefix + USER_TXN));
			theObj.dateTxn = rs.getTimestamp(prefix + DATE_TXN);
			theObj.dateCreate = rs.getTimestamp(prefix + DATE_CREATE);
			theObj.docRef1 = rs.getString(prefix + DOC_REF1);
			theObj.docKey1 = new Long(rs.getLong(prefix + DOC_KEY1));
			theObj.docRef2 = rs.getString(prefix + DOC_REF2);
			theObj.docKey2 = new Long(rs.getLong(prefix + DOC_KEY2));
			theObj.amountTxn = rs.getBigDecimal(prefix + AMOUNT_TXN);
			theObj.amountDelta = rs.getBigDecimal(prefix + AMOUNT_DELTA);
			theObj.info1 = rs.getString(prefix + INFO1);
			theObj.info2 = rs.getString(prefix + INFO2);
			theObj.warning = rs.getString(prefix + WARNING);
			theObj.state = rs.getString(prefix + STATE);
			theObj.status = rs.getString(prefix + STATUS);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception(ex.getMessage());
		}
		return theObj;
	}

	private Collection selectObjects(QueryObject query) throws NamingException, SQLException
	{
		Collection result = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjects: ");
			con = makeConnection();
			String selectStmt = " SELECT * FROM " + TABLENAME;
			selectStmt = query.appendQuery(selectStmt);
			Log.printVerbose(selectStmt);
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			while (rs.next())
			{
				MemberCardAuditTrailObject theObj = getObject(rs, "");
				if (theObj != null)
				{
					result.add(theObj);
				}
			}
			Log.printVerbose(strObjectName + " Leaving selectObjects: ");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				prepStmt.close();
			}
			closeConnection(con);
		}
		return result;
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
package com.vlee.ejb.customer;

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

public class MemberCardAuditTrailBean implements EntityBean
{
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String AUDIT_LEVEL = "audit_level";
	public static final String AUDIT_TYPE = "audit_type";
	public static final String CARD_PKID = "card_pkid";
	public static final String CARD_NO = "card_no";
	public static final String CARD_NAME = "card_name";
	public static final String BRANCH = "branch";
	public static final String PCCENTER = "pccenter";
	public static final String USER_TXN = "user_txn";
	public static final String DATE_TXN = "date_txn";
	public static final String DATE_CREATE = "date_create";
	public static final String DOC_REF1 = "doc_ref1";
	public static final String DOC_KEY1 = "doc_key1";
	public static final String DOC_REF2 = "doc_ref2";
	public static final String DOC_KEY2 = "doc_key2";
	public static final String AMOUNT_TXN = "amount_txn";
	public static final String AMOUNT_DELTA = "amount_delta";
	public static final String INFO1 = "info1";
	public static final String INFO2 = "info2";
	public static final String WARNING = "warning";
	public static final String STATE = "state";
	public static final String STATUS = "status";
	public static final String STATUS_ACTIVE = "act";
	public static final String STATUS_DELETED = "del";
	public static final String STATUS_INACTIVE = "ina";
	public static final String STATE_CREATED = "cre";
	public static final String STATE_CLOSED = "closed";
	public static final String MODULENAME = "customer";
	public static final Long PKID_START = new Long(100001);
	// Attributes of Object
	MemberCardAuditTrailObject valObj;
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "cust_membercard_audit_trail";
	private static final String strObjectName = "MemberCardAuditTrailBean: ";
	// EntityContext
	private EntityContext context = null;

	/***************************************************************************
	 * Getters
	 **************************************************************************/
	public MemberCardAuditTrailObject getObject()
	{
		return this.valObj;
	}

	public void setObject(MemberCardAuditTrailObject newVal)
	{
		Long pkid = this.valObj.pkid;
		this.valObj = newVal;
		this.valObj.pkid = pkid;
	}

	public Long getPkid()
	{
		return this.valObj.pkid;
	}

	public Long getPrimaryKey()
	{
		return this.valObj.pkid;
	}

	public void setPrimaryKey(Long pkid)
	{
		this.valObj.pkid = pkid;
	}

	/***************************************************************************
	 * ejbCreate
	 **************************************************************************/
	public Long ejbCreate(MemberCardAuditTrailObject newObj) throws CreateException
	{
		Log.printVerbose(strObjectName + "in ejbCreate");
		try
		{
			this.valObj = insertObject(newObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + "about to leave ejbCreate");
		return this.valObj.pkid;
	}

	/***************************************************************************
	 * ejbFindByPrimaryKey
	 **************************************************************************/
	public Long ejbFindByPrimaryKey(Long primaryKey) throws FinderException
	{
		Log.printVerbose(strObjectName + "in ejbFindByPrimaryKey");
		boolean result = false;
		try
		{
			result = selectByPrimaryKey(primaryKey);
		} catch (Exception ex)
		{
			ex.printStackTrace();
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
		try
		{
			this.valObj = new MemberCardAuditTrailObject();
			this.valObj.pkid = (Long) context.getPrimaryKey();
		} catch (Exception ex)
		{
			Log.printVerbose("the getmsg returns:" + ex.getMessage());
			ex.printStackTrace();
		}
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
			ex.printStackTrace();
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
	 * ejbPostCreate (1)
	 **************************************************************************/
	public void ejbPostCreate(MemberCardAuditTrailObject newObj)
	{
		// nothing
	}

	public Collection ejbHomeGetObjects(QueryObject query)
	{
		Collection col = null;
		try
		{
			col = selectObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return col;
	}

	/** ********************* Database Routines ************************ */
	private Connection makeConnection() throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			return ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void closeConnection(Connection con) throws NamingException, SQLException
	{
		try
		{
			if (!con.isClosed())
			{
				con.close();
			}
		} catch (SQLException ex)
		{
			throw new EJBException("closeConnection: " + ex.getMessage());
		}
	}

	private MemberCardAuditTrailObject insertObject(MemberCardAuditTrailObject newObj) throws NamingException,
			SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " insertObject: ");
			con = makeConnection();
			try
			{
				newObj.pkid = getNextPKId(con);
			} catch (Exception ex)
			{
				ex.printStackTrace();
				throw new EJBException(strObjectName + ex.getMessage());
			}
			try
			{
				String insertStatement = " INSERT INTO " + TABLENAME + "( " + PKID + ", " + AUDIT_LEVEL + ", "
						+ AUDIT_TYPE + ", " + CARD_PKID + ", " + CARD_NO + ", " + CARD_NAME + ", " + BRANCH + ", "
						+ PCCENTER + ", " + USER_TXN + ", " + DATE_TXN + ", " // 10
						+ DATE_CREATE + ", " + DOC_REF1 + ", " + DOC_KEY1 + ", " + DOC_REF2 + ", " + DOC_KEY2 + ", "
						+ AMOUNT_TXN + ", " + AMOUNT_DELTA + ", " + INFO1 + ", " + INFO2 + ", " + WARNING + ", " // 20
						+ STATE + ", " + STATUS + ") values ( " + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
						+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," + " ?, ? ) ";
				prepStmt = con.prepareStatement(insertStatement);
				prepStmt.setLong(1, newObj.pkid.longValue());
				prepStmt.setInt(2, newObj.auditLevel.intValue());
				prepStmt.setString(3, newObj.auditType);
				prepStmt.setLong(4, newObj.cardPkid.longValue());
				prepStmt.setString(5, newObj.cardNo);
				prepStmt.setString(6, newObj.cardName);
				prepStmt.setInt(7, newObj.branch.intValue());
				prepStmt.setInt(8, newObj.pcCenter.intValue());
				prepStmt.setInt(9, newObj.userTxn.intValue());
				prepStmt.setTimestamp(10, newObj.dateTxn);
				prepStmt.setTimestamp(11, newObj.dateCreate);
				prepStmt.setString(12, newObj.docRef1);
				prepStmt.setLong(13, newObj.docKey1.longValue());
				prepStmt.setString(14, newObj.docRef2);
				prepStmt.setLong(15, newObj.docKey2.longValue());
				prepStmt.setBigDecimal(16, newObj.amountTxn);
				prepStmt.setBigDecimal(17, newObj.amountDelta);
				prepStmt.setString(18, newObj.info1);
				prepStmt.setString(19, newObj.info2);
				prepStmt.setString(20, newObj.warning);
				prepStmt.setString(21, newObj.state);
				prepStmt.setString(22, newObj.status);
				prepStmt.executeUpdate();
				// prepStmt.close();
				// closeConnection(con);
				Log.printVerbose(strObjectName + " Leaving insertObject: ");
			} catch (SQLException ex)
			{
				// Rethrow exception
				ex.printStackTrace();
				throw ex;
			}
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			ex.printStackTrace();
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			closeConnection(con);
		}
		return newObj;
	}

	private boolean selectByPrimaryKey(Long primaryKey) throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectByPrimaryKey: ");
			con = makeConnection();
			String selectStmt = " SELECT " + PKID + " FROM " + TABLENAME + " WHERE " + PKID + " = ? ";
			prepStmt = con.prepareStatement(selectStmt);
			prepStmt.setLong(1, primaryKey.longValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = rs.next();
			Log.printVerbose(strObjectName + " Leaving selectByPrimaryKey:");
			return result;
		} catch (SQLException ex)
		{
			throw ex;
		} catch (Exception ex)
		{
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			if (prepStmt != null)
			{
				prepStmt.close();
			}
			closeConnection(con);
		}
	}

	private void deleteObject(Long pkid) throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " deleteObject: ");
			con = makeConnection();
			String deleteStatement = " DELETE FROM " + TABLENAME + " WHERE " + PKID + " = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setLong(1, pkid.longValue());
			prepStmt.executeUpdate();
		} catch (SQLException ex)
		{
			throw ex;
		} catch (Exception ex)
		{
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			if (prepStmt != null)
			{
				prepStmt.close();
			}
			closeConnection(con);
		}
	}

	// ////////////////////////////////////////////////
	private void loadObject() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " loadObject: ");
			con = makeConnection();
			String selectStmt = " SELECT * FROM " + TABLENAME + " WHERE " + PKID + " = ? ";
			prepStmt = con.prepareStatement(selectStmt);
			prepStmt.setLong(1, this.valObj.pkid.longValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.valObj = getObject(rs, "");
			} else
			{
				throw new NoSuchEntityException("Row for pkid " + this.valObj.pkid.toString()
						+ " not found in database.");
			}
			Log.printVerbose(strObjectName + " Leaving loadObject: ");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				prepStmt.close();
			}
			closeConnection(con);
		}
	}

	// ///////////////////////////////////////////////////////////////
	private void storeObject() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " storeObject ");
			con = makeConnection();
			String updateStatement = " UPDATE " + TABLENAME + " SET " + PKID + " = ?," + AUDIT_LEVEL + " = ?,"
					+ AUDIT_TYPE + " = ?," + CARD_PKID + " = ?," + CARD_NO + " = ?," + CARD_NAME + " = ?," + BRANCH
					+ " = ?," + PCCENTER + " = ?," + USER_TXN + " = ?," + DATE_TXN + " = ?," // 10
					+ DATE_CREATE + " = ?," + DOC_REF1 + " = ?," + DOC_KEY1 + " = ?," + DOC_REF2 + " = ?," + DOC_KEY2
					+ " = ?," + AMOUNT_TXN + " = ?," + AMOUNT_DELTA + " = ?," + INFO1 + " = ?," + INFO2 + " = ?,"
					+ WARNING + " = ?," // 20
					+ STATE + " = ?," + STATUS + " = ? " + " WHERE " + PKID + " = ? ";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setLong(1, this.valObj.pkid.longValue());
			prepStmt.setInt(2, this.valObj.auditLevel.intValue());
			prepStmt.setString(3, this.valObj.auditType);
			prepStmt.setLong(4, this.valObj.cardPkid.longValue());
			prepStmt.setString(5, this.valObj.cardNo);
			prepStmt.setString(6, this.valObj.cardName);
			prepStmt.setInt(7, this.valObj.branch.intValue());
			prepStmt.setInt(8, this.valObj.pcCenter.intValue());
			prepStmt.setInt(9, this.valObj.userTxn.intValue());
			prepStmt.setTimestamp(10, this.valObj.dateTxn);
			prepStmt.setTimestamp(11, this.valObj.dateCreate);
			prepStmt.setString(12, this.valObj.docRef1);
			prepStmt.setLong(13, this.valObj.docKey1.longValue());
			prepStmt.setString(14, this.valObj.docRef2);
			prepStmt.setLong(15, this.valObj.docKey2.longValue());
			prepStmt.setBigDecimal(16, this.valObj.amountTxn);
			prepStmt.setBigDecimal(17, this.valObj.amountDelta);
			prepStmt.setString(18, this.valObj.info1);
			prepStmt.setString(19, this.valObj.info2);
			prepStmt.setString(20, this.valObj.warning);
			prepStmt.setString(21, this.valObj.state);
			prepStmt.setString(22, this.valObj.status);
			prepStmt.setLong(23, this.valObj.pkid.longValue());
			int rowCount = prepStmt.executeUpdate();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row for pkid " + this.valObj.pkid + " failed.");
			}
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			if (prepStmt != null)
			{
				prepStmt.close();
			}
			closeConnection(con);
		}
	}




	private static synchronized Long getNextPKId(Connection con) throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, PKID, TABLENAME, MODULENAME, PKID_START);
	}

	private static synchronized Long getNextStmtNo(Connection con, String stmtType, Long pcCenter)
			throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, pcCenter.toString(), TABLENAME, stmtType, PKID_START);
	}

	public static MemberCardAuditTrailObject getObject(ResultSet rs, String prefix) throws Exception
	{
		MemberCardAuditTrailObject theObj = null;
		try
		{
			theObj = new MemberCardAuditTrailObject();
			theObj.pkid = new Long(rs.getLong(prefix + PKID)); // primary key
			theObj.auditLevel = new Integer(rs.getInt(prefix + AUDIT_LEVEL));
			theObj.auditType = rs.getString(prefix + AUDIT_TYPE);
			theObj.cardPkid = new Long(rs.getLong(prefix + CARD_PKID));
			theObj.cardNo = rs.getString(prefix + CARD_NO);
			theObj.cardName = rs.getString(prefix + CARD_NAME);
			theObj.branch = new Integer(rs.getInt(prefix + BRANCH));
			theObj.pcCenter = new Integer(rs.getInt(prefix + PCCENTER));
			theObj.userTxn = new Integer(rs.getInt(prefix + USER_TXN));
			theObj.dateTxn = rs.getTimestamp(prefix + DATE_TXN);
			theObj.dateCreate = rs.getTimestamp(prefix + DATE_CREATE);
			theObj.docRef1 = rs.getString(prefix + DOC_REF1);
			theObj.docKey1 = new Long(rs.getLong(prefix + DOC_KEY1));
			theObj.docRef2 = rs.getString(prefix + DOC_REF2);
			theObj.docKey2 = new Long(rs.getLong(prefix + DOC_KEY2));
			theObj.amountTxn = rs.getBigDecimal(prefix + AMOUNT_TXN);
			theObj.amountDelta = rs.getBigDecimal(prefix + AMOUNT_DELTA);
			theObj.info1 = rs.getString(prefix + INFO1);
			theObj.info2 = rs.getString(prefix + INFO2);
			theObj.warning = rs.getString(prefix + WARNING);
			theObj.state = rs.getString(prefix + STATE);
			theObj.status = rs.getString(prefix + STATUS);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception(ex.getMessage());
		}
		return theObj;
	}

	private Collection selectObjects(QueryObject query) throws NamingException, SQLException
	{
		Collection result = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjects: ");
			con = makeConnection();
			String selectStmt = " SELECT * FROM " + TABLENAME;
			selectStmt = query.appendQuery(selectStmt);
			Log.printVerbose(selectStmt);
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			while (rs.next())
			{
				MemberCardAuditTrailObject theObj = getObject(rs, "");
				if (theObj != null)
				{
					result.add(theObj);
				}
			}
			Log.printVerbose(strObjectName + " Leaving selectObjects: ");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				prepStmt.close();
			}
			closeConnection(con);
		}
		return result;
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
package com.vlee.ejb.customer;

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

public class MemberCardAuditTrailBean implements EntityBean
{
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String AUDIT_LEVEL = "audit_level";
	public static final String AUDIT_TYPE = "audit_type";
	public static final String CARD_PKID = "card_pkid";
	public static final String CARD_NO = "card_no";
	public static final String CARD_NAME = "card_name";
	public static final String BRANCH = "branch";
	public static final String PCCENTER = "pccenter";
	public static final String USER_TXN = "user_txn";
	public static final String DATE_TXN = "date_txn";
	public static final String DATE_CREATE = "date_create";
	public static final String DOC_REF1 = "doc_ref1";
	public static final String DOC_KEY1 = "doc_key1";
	public static final String DOC_REF2 = "doc_ref2";
	public static final String DOC_KEY2 = "doc_key2";
	public static final String AMOUNT_TXN = "amount_txn";
	public static final String AMOUNT_DELTA = "amount_delta";
	public static final String INFO1 = "info1";
	public static final String INFO2 = "info2";
	public static final String WARNING = "warning";
	public static final String STATE = "state";
	public static final String STATUS = "status";
	public static final String STATUS_ACTIVE = "act";
	public static final String STATUS_DELETED = "del";
	public static final String STATUS_INACTIVE = "ina";
	public static final String STATE_CREATED = "cre";
	public static final String STATE_CLOSED = "closed";
	public static final String MODULENAME = "customer";
	public static final Long PKID_START = new Long(100001);
	// Attributes of Object
	MemberCardAuditTrailObject valObj;
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "cust_membercard_audit_trail";
	private static final String strObjectName = "MemberCardAuditTrailBean: ";
	// EntityContext
	private EntityContext context = null;

	/***************************************************************************
	 * Getters
	 **************************************************************************/
	public MemberCardAuditTrailObject getObject()
	{
		return this.valObj;
	}

	public void setObject(MemberCardAuditTrailObject newVal)
	{
		Long pkid = this.valObj.pkid;
		this.valObj = newVal;
		this.valObj.pkid = pkid;
	}

	public Long getPkid()
	{
		return this.valObj.pkid;
	}

	public Long getPrimaryKey()
	{
		return this.valObj.pkid;
	}

	public void setPrimaryKey(Long pkid)
	{
		this.valObj.pkid = pkid;
	}

	/***************************************************************************
	 * ejbCreate
	 **************************************************************************/
	public Long ejbCreate(MemberCardAuditTrailObject newObj) throws CreateException
	{
		Log.printVerbose(strObjectName + "in ejbCreate");
		try
		{
			this.valObj = insertObject(newObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + "about to leave ejbCreate");
		return this.valObj.pkid;
	}

	/***************************************************************************
	 * ejbFindByPrimaryKey
	 **************************************************************************/
	public Long ejbFindByPrimaryKey(Long primaryKey) throws FinderException
	{
		Log.printVerbose(strObjectName + "in ejbFindByPrimaryKey");
		boolean result = false;
		try
		{
			result = selectByPrimaryKey(primaryKey);
		} catch (Exception ex)
		{
			ex.printStackTrace();
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
		try
		{
			this.valObj = new MemberCardAuditTrailObject();
			this.valObj.pkid = (Long) context.getPrimaryKey();
		} catch (Exception ex)
		{
			Log.printVerbose("the getmsg returns:" + ex.getMessage());
			ex.printStackTrace();
		}
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
			ex.printStackTrace();
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
	 * ejbPostCreate (1)
	 **************************************************************************/
	public void ejbPostCreate(MemberCardAuditTrailObject newObj)
	{
		// nothing
	}

	public Collection ejbHomeGetObjects(QueryObject query)
	{
		Collection col = null;
		try
		{
			col = selectObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return col;
	}

	/** ********************* Database Routines ************************ */
	private Connection makeConnection() throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			return ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void closeConnection(Connection con) throws NamingException, SQLException
	{
		try
		{
			if (!con.isClosed())
			{
				con.close();
			}
		} catch (SQLException ex)
		{
			throw new EJBException("closeConnection: " + ex.getMessage());
		}
	}

	private MemberCardAuditTrailObject insertObject(MemberCardAuditTrailObject newObj) throws NamingException,
			SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " insertObject: ");
			con = makeConnection();
			try
			{
				newObj.pkid = getNextPKId(con);
			} catch (Exception ex)
			{
				ex.printStackTrace();
				throw new EJBException(strObjectName + ex.getMessage());
			}
			try
			{
				String insertStatement = " INSERT INTO " + TABLENAME + "( " + PKID + ", " + AUDIT_LEVEL + ", "
						+ AUDIT_TYPE + ", " + CARD_PKID + ", " + CARD_NO + ", " + CARD_NAME + ", " + BRANCH + ", "
						+ PCCENTER + ", " + USER_TXN + ", " + DATE_TXN + ", " // 10
						+ DATE_CREATE + ", " + DOC_REF1 + ", " + DOC_KEY1 + ", " + DOC_REF2 + ", " + DOC_KEY2 + ", "
						+ AMOUNT_TXN + ", " + AMOUNT_DELTA + ", " + INFO1 + ", " + INFO2 + ", " + WARNING + ", " // 20
						+ STATE + ", " + STATUS + ") values ( " + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
						+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," + " ?, ? ) ";
				prepStmt = con.prepareStatement(insertStatement);
				prepStmt.setLong(1, newObj.pkid.longValue());
				prepStmt.setInt(2, newObj.auditLevel.intValue());
				prepStmt.setString(3, newObj.auditType);
				prepStmt.setLong(4, newObj.cardPkid.longValue());
				prepStmt.setString(5, newObj.cardNo);
				prepStmt.setString(6, newObj.cardName);
				prepStmt.setInt(7, newObj.branch.intValue());
				prepStmt.setInt(8, newObj.pcCenter.intValue());
				prepStmt.setInt(9, newObj.userTxn.intValue());
				prepStmt.setTimestamp(10, newObj.dateTxn);
				prepStmt.setTimestamp(11, newObj.dateCreate);
				prepStmt.setString(12, newObj.docRef1);
				prepStmt.setLong(13, newObj.docKey1.longValue());
				prepStmt.setString(14, newObj.docRef2);
				prepStmt.setLong(15, newObj.docKey2.longValue());
				prepStmt.setBigDecimal(16, newObj.amountTxn);
				prepStmt.setBigDecimal(17, newObj.amountDelta);
				prepStmt.setString(18, newObj.info1);
				prepStmt.setString(19, newObj.info2);
				prepStmt.setString(20, newObj.warning);
				prepStmt.setString(21, newObj.state);
				prepStmt.setString(22, newObj.status);
				prepStmt.executeUpdate();
				// prepStmt.close();
				// closeConnection(con);
				Log.printVerbose(strObjectName + " Leaving insertObject: ");
			} catch (SQLException ex)
			{
				// Rethrow exception
				ex.printStackTrace();
				throw ex;
			}
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			ex.printStackTrace();
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			closeConnection(con);
		}
		return newObj;
	}

	private boolean selectByPrimaryKey(Long primaryKey) throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectByPrimaryKey: ");
			con = makeConnection();
			String selectStmt = " SELECT " + PKID + " FROM " + TABLENAME + " WHERE " + PKID + " = ? ";
			prepStmt = con.prepareStatement(selectStmt);
			prepStmt.setLong(1, primaryKey.longValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = rs.next();
			Log.printVerbose(strObjectName + " Leaving selectByPrimaryKey:");
			return result;
		} catch (SQLException ex)
		{
			throw ex;
		} catch (Exception ex)
		{
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			if (prepStmt != null)
			{
				prepStmt.close();
			}
			closeConnection(con);
		}
	}

	private void deleteObject(Long pkid) throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " deleteObject: ");
			con = makeConnection();
			String deleteStatement = " DELETE FROM " + TABLENAME + " WHERE " + PKID + " = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setLong(1, pkid.longValue());
			prepStmt.executeUpdate();
		} catch (SQLException ex)
		{
			throw ex;
		} catch (Exception ex)
		{
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			if (prepStmt != null)
			{
				prepStmt.close();
			}
			closeConnection(con);
		}
	}

	// ////////////////////////////////////////////////
	private void loadObject() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " loadObject: ");
			con = makeConnection();
			String selectStmt = " SELECT * FROM " + TABLENAME + " WHERE " + PKID + " = ? ";
			prepStmt = con.prepareStatement(selectStmt);
			prepStmt.setLong(1, this.valObj.pkid.longValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.valObj = getObject(rs, "");
			} else
			{
				throw new NoSuchEntityException("Row for pkid " + this.valObj.pkid.toString()
						+ " not found in database.");
			}
			Log.printVerbose(strObjectName + " Leaving loadObject: ");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				prepStmt.close();
			}
			closeConnection(con);
		}
	}

	// ///////////////////////////////////////////////////////////////
	private void storeObject() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " storeObject ");
			con = makeConnection();
			String updateStatement = " UPDATE " + TABLENAME + " SET " + PKID + " = ?," + AUDIT_LEVEL + " = ?,"
					+ AUDIT_TYPE + " = ?," + CARD_PKID + " = ?," + CARD_NO + " = ?," + CARD_NAME + " = ?," + BRANCH
					+ " = ?," + PCCENTER + " = ?," + USER_TXN + " = ?," + DATE_TXN + " = ?," // 10
					+ DATE_CREATE + " = ?," + DOC_REF1 + " = ?," + DOC_KEY1 + " = ?," + DOC_REF2 + " = ?," + DOC_KEY2
					+ " = ?," + AMOUNT_TXN + " = ?," + AMOUNT_DELTA + " = ?," + INFO1 + " = ?," + INFO2 + " = ?,"
					+ WARNING + " = ?," // 20
					+ STATE + " = ?," + STATUS + " = ? " + " WHERE " + PKID + " = ? ";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setLong(1, this.valObj.pkid.longValue());
			prepStmt.setInt(2, this.valObj.auditLevel.intValue());
			prepStmt.setString(3, this.valObj.auditType);
			prepStmt.setLong(4, this.valObj.cardPkid.longValue());
			prepStmt.setString(5, this.valObj.cardNo);
			prepStmt.setString(6, this.valObj.cardName);
			prepStmt.setInt(7, this.valObj.branch.intValue());
			prepStmt.setInt(8, this.valObj.pcCenter.intValue());
			prepStmt.setInt(9, this.valObj.userTxn.intValue());
			prepStmt.setTimestamp(10, this.valObj.dateTxn);
			prepStmt.setTimestamp(11, this.valObj.dateCreate);
			prepStmt.setString(12, this.valObj.docRef1);
			prepStmt.setLong(13, this.valObj.docKey1.longValue());
			prepStmt.setString(14, this.valObj.docRef2);
			prepStmt.setLong(15, this.valObj.docKey2.longValue());
			prepStmt.setBigDecimal(16, this.valObj.amountTxn);
			prepStmt.setBigDecimal(17, this.valObj.amountDelta);
			prepStmt.setString(18, this.valObj.info1);
			prepStmt.setString(19, this.valObj.info2);
			prepStmt.setString(20, this.valObj.warning);
			prepStmt.setString(21, this.valObj.state);
			prepStmt.setString(22, this.valObj.status);
			prepStmt.setLong(23, this.valObj.pkid.longValue());
			int rowCount = prepStmt.executeUpdate();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row for pkid " + this.valObj.pkid + " failed.");
			}
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			if (prepStmt != null)
			{
				prepStmt.close();
			}
			closeConnection(con);
		}
	}

	private static synchronized Long getNextPKId(Connection con) throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, PKID, TABLENAME, MODULENAME, PKID_START);
	}

	private static synchronized Long getNextStmtNo(Connection con, String stmtType, Long pcCenter)
			throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, pcCenter.toString(), TABLENAME, stmtType, PKID_START);
	}

	public static MemberCardAuditTrailObject getObject(ResultSet rs, String prefix) throws Exception
	{
		MemberCardAuditTrailObject theObj = null;
		try
		{
			theObj = new MemberCardAuditTrailObject();
			theObj.pkid = new Long(rs.getLong(prefix + PKID)); // primary key
			theObj.auditLevel = new Integer(rs.getInt(prefix + AUDIT_LEVEL));
			theObj.auditType = rs.getString(prefix + AUDIT_TYPE);
			theObj.cardPkid = new Long(rs.getLong(prefix + CARD_PKID));
			theObj.cardNo = rs.getString(prefix + CARD_NO);
			theObj.cardName = rs.getString(prefix + CARD_NAME);
			theObj.branch = new Integer(rs.getInt(prefix + BRANCH));
			theObj.pcCenter = new Integer(rs.getInt(prefix + PCCENTER));
			theObj.userTxn = new Integer(rs.getInt(prefix + USER_TXN));
			theObj.dateTxn = rs.getTimestamp(prefix + DATE_TXN);
			theObj.dateCreate = rs.getTimestamp(prefix + DATE_CREATE);
			theObj.docRef1 = rs.getString(prefix + DOC_REF1);
			theObj.docKey1 = new Long(rs.getLong(prefix + DOC_KEY1));
			theObj.docRef2 = rs.getString(prefix + DOC_REF2);
			theObj.docKey2 = new Long(rs.getLong(prefix + DOC_KEY2));
			theObj.amountTxn = rs.getBigDecimal(prefix + AMOUNT_TXN);
			theObj.amountDelta = rs.getBigDecimal(prefix + AMOUNT_DELTA);
			theObj.info1 = rs.getString(prefix + INFO1);
			theObj.info2 = rs.getString(prefix + INFO2);
			theObj.warning = rs.getString(prefix + WARNING);
			theObj.state = rs.getString(prefix + STATE);
			theObj.status = rs.getString(prefix + STATUS);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception(ex.getMessage());
		}
		return theObj;
	}

	private Collection selectObjects(QueryObject query) throws NamingException, SQLException
	{
		Collection result = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjects: ");
			con = makeConnection();
			String selectStmt = " SELECT * FROM " + TABLENAME;
			selectStmt = query.appendQuery(selectStmt);
			Log.printVerbose(selectStmt);
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			while (rs.next())
			{
				MemberCardAuditTrailObject theObj = getObject(rs, "");
				if (theObj != null)
				{
					result.add(theObj);
				}
			}
			Log.printVerbose(strObjectName + " Leaving selectObjects: ");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				prepStmt.close();
			}
			closeConnection(con);
		}
		return result;
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
package com.vlee.ejb.customer;

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

public class MemberCardAuditTrailBean implements EntityBean
{
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String AUDIT_LEVEL = "audit_level";
	public static final String AUDIT_TYPE = "audit_type";
	public static final String CARD_PKID = "card_pkid";
	public static final String CARD_NO = "card_no";
	public static final String CARD_NAME = "card_name";
	public static final String BRANCH = "branch";
	public static final String PCCENTER = "pccenter";
	public static final String USER_TXN = "user_txn";
	public static final String DATE_TXN = "date_txn";
	public static final String DATE_CREATE = "date_create";
	public static final String DOC_REF1 = "doc_ref1";
	public static final String DOC_KEY1 = "doc_key1";
	public static final String DOC_REF2 = "doc_ref2";
	public static final String DOC_KEY2 = "doc_key2";
	public static final String AMOUNT_TXN = "amount_txn";
	public static final String AMOUNT_DELTA = "amount_delta";
	public static final String INFO1 = "info1";
	public static final String INFO2 = "info2";
	public static final String WARNING = "warning";
	public static final String STATE = "state";
	public static final String STATUS = "status";
	public static final String STATUS_ACTIVE = "act";
	public static final String STATUS_DELETED = "del";
	public static final String STATUS_INACTIVE = "ina";
	public static final String STATE_CREATED = "cre";
	public static final String STATE_CLOSED = "closed";
	public static final String MODULENAME = "customer";
	public static final Long PKID_START = new Long(100001);
	// Attributes of Object
	MemberCardAuditTrailObject valObj;
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "cust_membercard_audit_trail";
	private static final String strObjectName = "MemberCardAuditTrailBean: ";
	// EntityContext
	private EntityContext context = null;

	/***************************************************************************
	 * Getters
	 **************************************************************************/
	public MemberCardAuditTrailObject getObject()
	{
		return this.valObj;
	}

	public void setObject(MemberCardAuditTrailObject newVal)
	{
		Long pkid = this.valObj.pkid;
		this.valObj = newVal;
		this.valObj.pkid = pkid;
	}

	public Long getPkid()
	{
		return this.valObj.pkid;
	}

	public Long getPrimaryKey()
	{
		return this.valObj.pkid;
	}

	public void setPrimaryKey(Long pkid)
	{
		this.valObj.pkid = pkid;
	}

	/***************************************************************************
	 * ejbCreate
	 **************************************************************************/
	public Long ejbCreate(MemberCardAuditTrailObject newObj) throws CreateException
	{
		Log.printVerbose(strObjectName + "in ejbCreate");
		try
		{
			this.valObj = insertObject(newObj);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		Log.printVerbose(strObjectName + "about to leave ejbCreate");
		return this.valObj.pkid;
	}

	/***************************************************************************
	 * ejbFindByPrimaryKey
	 **************************************************************************/
	public Long ejbFindByPrimaryKey(Long primaryKey) throws FinderException
	{
		Log.printVerbose(strObjectName + "in ejbFindByPrimaryKey");
		boolean result = false;
		try
		{
			result = selectByPrimaryKey(primaryKey);
		} catch (Exception ex)
		{
			ex.printStackTrace();
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
		try
		{
			this.valObj = new MemberCardAuditTrailObject();
			this.valObj.pkid = (Long) context.getPrimaryKey();
		} catch (Exception ex)
		{
			Log.printVerbose("the getmsg returns:" + ex.getMessage());
			ex.printStackTrace();
		}
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
			ex.printStackTrace();
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
	 * ejbPostCreate (1)
	 **************************************************************************/
	public void ejbPostCreate(MemberCardAuditTrailObject newObj)
	{
		// nothing
	}

	public Collection ejbHomeGetObjects(QueryObject query)
	{
		Collection col = null;
		try
		{
			col = selectObjects(query);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return col;
	}

	/** ********************* Database Routines ************************ */
	private Connection makeConnection() throws NamingException, SQLException
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			return ds.getConnection();
		} catch (Exception ex)
		{
			throw new EJBException("Unable to connect to database. " + ex.getMessage());
		}
	}

	private void closeConnection(Connection con) throws NamingException, SQLException
	{
		try
		{
			if (!con.isClosed())
			{
				con.close();
			}
		} catch (SQLException ex)
		{
			throw new EJBException("closeConnection: " + ex.getMessage());
		}
	}

	private MemberCardAuditTrailObject insertObject(MemberCardAuditTrailObject newObj) throws NamingException,
			SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " insertObject: ");
			con = makeConnection();
			try
			{
				newObj.pkid = getNextPKId(con);
			} catch (Exception ex)
			{
				ex.printStackTrace();
				throw new EJBException(strObjectName + ex.getMessage());
			}
			try
			{
				String insertStatement = " INSERT INTO " + TABLENAME + "( " + PKID + ", " + AUDIT_LEVEL + ", "
						+ AUDIT_TYPE + ", " + CARD_PKID + ", " + CARD_NO + ", " + CARD_NAME + ", " + BRANCH + ", "
						+ PCCENTER + ", " + USER_TXN + ", " + DATE_TXN + ", " // 10
						+ DATE_CREATE + ", " + DOC_REF1 + ", " + DOC_KEY1 + ", " + DOC_REF2 + ", " + DOC_KEY2 + ", "
						+ AMOUNT_TXN + ", " + AMOUNT_DELTA + ", " + INFO1 + ", " + INFO2 + ", " + WARNING + ", " // 20
						+ STATE + ", " + STATUS + ") values ( " + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
						+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," + " ?, ? ) ";
				prepStmt = con.prepareStatement(insertStatement);
				prepStmt.setLong(1, newObj.pkid.longValue());
				prepStmt.setInt(2, newObj.auditLevel.intValue());
				prepStmt.setString(3, newObj.auditType);
				prepStmt.setLong(4, newObj.cardPkid.longValue());
				prepStmt.setString(5, newObj.cardNo);
				prepStmt.setString(6, newObj.cardName);
				prepStmt.setInt(7, newObj.branch.intValue());
				prepStmt.setInt(8, newObj.pcCenter.intValue());
				prepStmt.setInt(9, newObj.userTxn.intValue());
				prepStmt.setTimestamp(10, newObj.dateTxn);
				prepStmt.setTimestamp(11, newObj.dateCreate);
				prepStmt.setString(12, newObj.docRef1);
				prepStmt.setLong(13, newObj.docKey1.longValue());
				prepStmt.setString(14, newObj.docRef2);
				prepStmt.setLong(15, newObj.docKey2.longValue());
				prepStmt.setBigDecimal(16, newObj.amountTxn);
				prepStmt.setBigDecimal(17, newObj.amountDelta);
				prepStmt.setString(18, newObj.info1);
				prepStmt.setString(19, newObj.info2);
				prepStmt.setString(20, newObj.warning);
				prepStmt.setString(21, newObj.state);
				prepStmt.setString(22, newObj.status);
				prepStmt.executeUpdate();
				// prepStmt.close();
				// closeConnection(con);
				Log.printVerbose(strObjectName + " Leaving insertObject: ");
			} catch (SQLException ex)
			{
				// Rethrow exception
				ex.printStackTrace();
				throw ex;
			}
		} catch (Exception ex)
		{
			// Repackage exception to SQLException
			ex.printStackTrace();
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				// Log.printVerbose("Closing prepStmt ...");
				prepStmt.close();
			}
			closeConnection(con);
		}
		return newObj;
	}

	private boolean selectByPrimaryKey(Long primaryKey) throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectByPrimaryKey: ");
			con = makeConnection();
			String selectStmt = " SELECT " + PKID + " FROM " + TABLENAME + " WHERE " + PKID + " = ? ";
			prepStmt = con.prepareStatement(selectStmt);
			prepStmt.setLong(1, primaryKey.longValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = rs.next();
			Log.printVerbose(strObjectName + " Leaving selectByPrimaryKey:");
			return result;
		} catch (SQLException ex)
		{
			throw ex;
		} catch (Exception ex)
		{
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			if (prepStmt != null)
			{
				prepStmt.close();
			}
			closeConnection(con);
		}
	}

	private void deleteObject(Long pkid) throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " deleteObject: ");
			con = makeConnection();
			String deleteStatement = " DELETE FROM " + TABLENAME + " WHERE " + PKID + " = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setLong(1, pkid.longValue());
			prepStmt.executeUpdate();
		} catch (SQLException ex)
		{
			throw ex;
		} catch (Exception ex)
		{
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			if (prepStmt != null)
			{
				prepStmt.close();
			}
			closeConnection(con);
		}
	}

	// ////////////////////////////////////////////////
	private void loadObject() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " loadObject: ");
			con = makeConnection();
			String selectStmt = " SELECT * FROM " + TABLENAME + " WHERE " + PKID + " = ? ";
			prepStmt = con.prepareStatement(selectStmt);
			prepStmt.setLong(1, this.valObj.pkid.longValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.valObj = getObject(rs, "");
			} else
			{
				throw new NoSuchEntityException("Row for pkid " + this.valObj.pkid.toString()
						+ " not found in database.");
			}
			Log.printVerbose(strObjectName + " Leaving loadObject: ");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				prepStmt.close();
			}
			closeConnection(con);
		}
	}

	// ///////////////////////////////////////////////////////////////
	private void storeObject() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " storeObject ");
			con = makeConnection();
			String updateStatement = " UPDATE " + TABLENAME + " SET " + PKID + " = ?," + AUDIT_LEVEL + " = ?,"
					+ AUDIT_TYPE + " = ?," + CARD_PKID + " = ?," + CARD_NO + " = ?," + CARD_NAME + " = ?," + BRANCH
					+ " = ?," + PCCENTER + " = ?," + USER_TXN + " = ?," + DATE_TXN + " = ?," // 10
					+ DATE_CREATE + " = ?," + DOC_REF1 + " = ?," + DOC_KEY1 + " = ?," + DOC_REF2 + " = ?," + DOC_KEY2
					+ " = ?," + AMOUNT_TXN + " = ?," + AMOUNT_DELTA + " = ?," + INFO1 + " = ?," + INFO2 + " = ?,"
					+ WARNING + " = ?," // 20
					+ STATE + " = ?," + STATUS + " = ? " + " WHERE " + PKID + " = ? ";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setLong(1, this.valObj.pkid.longValue());
			prepStmt.setInt(2, this.valObj.auditLevel.intValue());
			prepStmt.setString(3, this.valObj.auditType);
			prepStmt.setLong(4, this.valObj.cardPkid.longValue());
			prepStmt.setString(5, this.valObj.cardNo);
			prepStmt.setString(6, this.valObj.cardName);
			prepStmt.setInt(7, this.valObj.branch.intValue());
			prepStmt.setInt(8, this.valObj.pcCenter.intValue());
			prepStmt.setInt(9, this.valObj.userTxn.intValue());
			prepStmt.setTimestamp(10, this.valObj.dateTxn);
			prepStmt.setTimestamp(11, this.valObj.dateCreate);
			prepStmt.setString(12, this.valObj.docRef1);
			prepStmt.setLong(13, this.valObj.docKey1.longValue());
			prepStmt.setString(14, this.valObj.docRef2);
			prepStmt.setLong(15, this.valObj.docKey2.longValue());
			prepStmt.setBigDecimal(16, this.valObj.amountTxn);
			prepStmt.setBigDecimal(17, this.valObj.amountDelta);
			prepStmt.setString(18, this.valObj.info1);
			prepStmt.setString(19, this.valObj.info2);
			prepStmt.setString(20, this.valObj.warning);
			prepStmt.setString(21, this.valObj.state);
			prepStmt.setString(22, this.valObj.status);
			prepStmt.setLong(23, this.valObj.pkid.longValue());
			int rowCount = prepStmt.executeUpdate();
			if (rowCount == 0)
			{
				throw new EJBException("Storing row for pkid " + this.valObj.pkid + " failed.");
			}
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			if (prepStmt != null)
			{
				prepStmt.close();
			}
			closeConnection(con);
		}
	}




	private static synchronized Long getNextPKId(Connection con) throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, PKID, TABLENAME, MODULENAME, PKID_START);
	}

	private static synchronized Long getNextStmtNo(Connection con, String stmtType, Long pcCenter)
			throws NamingException, SQLException
	{
		return AppTableCounterUtil.getNextPKId(con, pcCenter.toString(), TABLENAME, stmtType, PKID_START);
	}

	public static MemberCardAuditTrailObject getObject(ResultSet rs, String prefix) throws Exception
	{
		MemberCardAuditTrailObject theObj = null;
		try
		{
			theObj = new MemberCardAuditTrailObject();
			theObj.pkid = new Long(rs.getLong(prefix + PKID)); // primary key
			theObj.auditLevel = new Integer(rs.getInt(prefix + AUDIT_LEVEL));
			theObj.auditType = rs.getString(prefix + AUDIT_TYPE);
			theObj.cardPkid = new Long(rs.getLong(prefix + CARD_PKID));
			theObj.cardNo = rs.getString(prefix + CARD_NO);
			theObj.cardName = rs.getString(prefix + CARD_NAME);
			theObj.branch = new Integer(rs.getInt(prefix + BRANCH));
			theObj.pcCenter = new Integer(rs.getInt(prefix + PCCENTER));
			theObj.userTxn = new Integer(rs.getInt(prefix + USER_TXN));
			theObj.dateTxn = rs.getTimestamp(prefix + DATE_TXN);
			theObj.dateCreate = rs.getTimestamp(prefix + DATE_CREATE);
			theObj.docRef1 = rs.getString(prefix + DOC_REF1);
			theObj.docKey1 = new Long(rs.getLong(prefix + DOC_KEY1));
			theObj.docRef2 = rs.getString(prefix + DOC_REF2);
			theObj.docKey2 = new Long(rs.getLong(prefix + DOC_KEY2));
			theObj.amountTxn = rs.getBigDecimal(prefix + AMOUNT_TXN);
			theObj.amountDelta = rs.getBigDecimal(prefix + AMOUNT_DELTA);
			theObj.info1 = rs.getString(prefix + INFO1);
			theObj.info2 = rs.getString(prefix + INFO2);
			theObj.warning = rs.getString(prefix + WARNING);
			theObj.state = rs.getString(prefix + STATE);
			theObj.status = rs.getString(prefix + STATUS);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new Exception(ex.getMessage());
		}
		return theObj;
	}

	private Collection selectObjects(QueryObject query) throws NamingException, SQLException
	{
		Collection result = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjects: ");
			con = makeConnection();
			String selectStmt = " SELECT * FROM " + TABLENAME;
			selectStmt = query.appendQuery(selectStmt);
			Log.printVerbose(selectStmt);
			prepStmt = con.prepareStatement(selectStmt);
			ResultSet rs = prepStmt.executeQuery();
			while (rs.next())
			{
				MemberCardAuditTrailObject theObj = getObject(rs, "");
				if (theObj != null)
				{
					result.add(theObj);
				}
			}
			Log.printVerbose(strObjectName + " Leaving selectObjects: ");
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			throw new SQLException("Repackaged Exception: " + ex.getMessage());
		} finally
		{
			// ensure that prepStmt and con are closed
			if (prepStmt != null)
			{
				prepStmt.close();
			}
			closeConnection(con);
		}
		return result;
	}
} // ObjectBean
