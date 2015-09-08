/*==========================================================
 *
 * Copyright  of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
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
import com.vlee.ejb.customer.*;
import com.vlee.ejb.supplier.*;
import com.vlee.bean.reports.*;

public class NominalAccountBean implements EntityBean
{
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String CODE = "code";
	public static final String NAMESPACE = "namespace";
	public static final String FOREIGN_TABLE = "foreign_table";
	public static final String FOREIGN_KEY = "foreign_key";
	public static final String ACC_TYPE = "acc_type";
	public static final String CURRENCY = "currency";
	public static final String AMOUNT = "amount";
	public static final String REMARKS = "remarks";
	public static final String ACC_PCCENTER_ID = "pc_center_id";
	public static final String STATE = "state";
	public static final String STATUS = "status";
	public static final String LASTUPDATE = "lastupdate";
	public static final String USERID_EDIT = "userid_edit";
	// Constants for GLCODE
	public static final String GLCODE_NOMINAL = "nominal";
	// Constants for STATUS
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	// Constants for STATE
	public static final String STATE_CREATED = "created";
	// Constants for NAMESPACE
	public static final String NS_CUSTOMER = "customer";
	public static final String NS_SUPPLIER = "supplier";
	public static final String NS_GENERAL = "general";
	public static final String NS_FINANCE = "finance";
	public static final String NS_SHAREHOLDER = "shareholder";
	public static final String NS_ADMIN = "admin";
	public static final String NS_EMPLOYEE = "employee";
	public static final String NS_MISC = "miscellaneous";
	// Constants for FOREIGN_TABLE
	public static final String FT_CUSTOMER = com.vlee.ejb.customer.CustAccountBean.TABLENAME;
	public static final String FT_SUPPLIER = com.vlee.ejb.supplier.SuppAccountBean.TABLENAME;
	public static final String FT_GENERAL = com.vlee.ejb.accounting.GenericEntityAccountBean.TABLENAME;
	// Constants for ACC_TYPE
	public static final String ACC_TYPE_PAYABLE = "accPayable";
	public static final String ACC_TYPE_RECEIVABLE = "accReceivable";
	public static final String ACC_TYPE_NEUTRAL = "accNeutral";
	// Defaults
	public static final Integer DEF_ACC_PCCENTER_ID = new Integer(0);
	// Attributes of Object
	private Integer pkid; // Primary Key
	private String code;
	private String namespace;
	private String foreignTable;
	private Integer foreignKey;
	private String accountType;
	private String currency;
	private BigDecimal amount;
	private String remarks;
	private Integer accPCCenterId;
	private String state;
	private String status;
	private Timestamp lastUpdate;
	private Integer userIdUpdate;
	// DB Connection attributes
	// private Connection con = null;
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_nominal_account";
	// Other params
	private static final String strObjectName = "NominalAccountBean: ";
	// Constants
	public static final int MAX_LEN_REMARKS = 200;
	// EntityContext
	private EntityContext context = null;
/*//	 Attributes of Object
	private NominalAccountObject valObj;*/

	/***************************************************************************
	 * Getters
	 **************************************************************************/
	public NominalAccountObject getObject()
	{
		NominalAccountObject naObj = new NominalAccountObject();
		naObj.pkid = this.pkid;
		naObj.code = this.code;
		naObj.namespace = this.namespace;
		naObj.foreignTable = this.foreignTable;
		naObj.foreignKey = this.foreignKey;
		naObj.accountType = this.accountType;
		naObj.currency = this.currency;
		naObj.amount = this.amount;
		naObj.remarks = this.remarks;
		naObj.accPCCenterId = this.accPCCenterId;
		naObj.state = this.state;
		naObj.status = this.status;
		naObj.lastUpdate = this.lastUpdate;
		naObj.userIdUpdate = this.userIdUpdate;
		return naObj;
	}

	public Integer getPkid()
	{
		return pkid;
	}

	public String getCode()
	{
		return code;
	}

	public String getNamespace()
	{
		return namespace;
	}

	public String getForeignTable()
	{
		return foreignTable;
	}

	public Integer getForeignKey()
	{
		return foreignKey;
	}

	public String getAccountType()
	{
		return accountType;
	}

	public String getCurrency()
	{
		return currency;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public String getRemarks()
	{
		return remarks;
	}

	public Integer getPCCenterId()
	{
		return accPCCenterId;
	}

	public String getState()
	{
		return state;
	}

	public String getStatus()
	{
		return status;
	}

	public Timestamp getLastUpdate()
	{
		return lastUpdate;
	}

	public Integer getUserIdUpdate()
	{
		return userIdUpdate;
	}

	/***************************************************************************
	 * Setters
	 **************************************************************************/
/*	public void setObject(NominalAccountObject newVal)
	{
		Integer pkid = this.valObj.pkid;
		this.valObj = newVal;
		this.valObj.pkid = pkid;
	}*/
	
	public void setPkid(Integer pkid)
	{
		this.pkid = pkid;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public void setNamespace(String namespace)
	{
		this.namespace = namespace;
	}

	public void setForeignTable(String foreignTable)
	{
		this.foreignTable = foreignTable;
	}

	public void setForeignKey(Integer foreignKey)
	{
		this.foreignKey = foreignKey;
	}

	public void setAccountType(String accountType)
	{
		this.accountType = accountType;
	}

	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
	}

	public void addAmount(BigDecimal delta)
	{
		this.amount = this.amount.add(delta);
	}

	public void setRemarks(String remarks)
	{
		this.remarks = remarks;
	}

	public void setPCCenterId(Integer accPCCenterId)
	{
		this.accPCCenterId = accPCCenterId;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public void setLastUpdate(Timestamp lastUpdate)
	{
		this.lastUpdate = lastUpdate;
	}

	public void setUserIdUpdate(Integer userIdUpdate)
	{
		this.userIdUpdate = userIdUpdate;
	}

	/***************************************************************************
	 * ejbCreate (1)
	 **************************************************************************/
	public Integer ejbCreate(String namespace, String code, String foreignTable, Integer foreignKey,
			String accountType, String currency, BigDecimal amount, String remarks, Integer accPCCenterId,
			String state, Timestamp tsCreate, Integer userIdUpdate) throws CreateException
	{
		Integer newKey = null;
		Log.printVerbose(strObjectName + "in ejbCreate");
		try
		{
			newKey = insertObject(namespace, code, foreignTable, foreignKey, accountType, currency, amount, remarks,
					accPCCenterId, state, STATUS_ACTIVE, tsCreate, userIdUpdate);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (newKey != null)
		{
			this.pkid = newKey; // primary key
			this.namespace = namespace;
			this.code = code;
			this.foreignTable = foreignTable;
			this.foreignKey = foreignKey;
			this.accountType = accountType;
			this.currency = currency;
			this.amount = amount;
			this.remarks = remarks;
			this.accPCCenterId = accPCCenterId;
			this.state = state;
			this.status = STATUS_ACTIVE;
			this.lastUpdate = tsCreate;
			this.userIdUpdate = userIdUpdate;
		}
		Log.printVerbose(strObjectName + "about to leave ejbCreate");
		return pkid;
	}

	/***************************************************************************
	 * ejbFindByPrimaryKey
	 **************************************************************************/
	public Integer ejbFindByPrimaryKey(Integer primaryKey) throws FinderException
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
	 * ejbFindObjectsGiven
	 **************************************************************************/
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

	/***************************************************************************
	 * ejbHomeGetObject
	 **************************************************************************/
	public NominalAccountObject ejbHomeGetObject(String foreignTable, Integer accPCCenterId, Integer foreignKey,
			String currency)
	{
		NominalAccountObject naObj = null;
		try
		{
			naObj = selectObjectGiven(foreignTable, accPCCenterId, foreignKey, currency);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("ERROR: " + ex.getMessage());
		}
		return naObj;
	}

	public NominalAccountObject ejbHomeGetObject(Integer pkid)
	{
		NominalAccountObject naObj = null;
		try
		{
			naObj = selectObjectGiven(pkid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("ERROR: " + ex.getMessage());
		}
		return naObj;
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
	
	public Vector ejbHomeGetObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(fieldName1, value1, fieldName2, value2);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("ERROR: " + ex.getMessage());
		}
		return vecValObj;
	}

	public Vector ejbHomeGetValueObjectsGiven(Integer pcCenter, String naForeignTable, // Customer,
																						// Supplier
			Integer naForeignKey, // nullable
			String currency, String natForeignTable, Long natForeignKey, Timestamp dateFrom, Timestamp dateTo,
			String strOption)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(pcCenter, naForeignTable, naForeignKey, currency, natForeignTable,
					natForeignKey, dateFrom, dateTo, strOption);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	public Vector ejbHomeGetValueObjectsGiven(Integer pcCenter, String naForeignTable, // Customer,
																						// Supplier
			Integer naForeignKey, // nullable
			String currency, BigDecimal bdMoreThan, BigDecimal bdLessTan, String status)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(pcCenter, naForeignTable, // Customer,
																			// Supplier
					naForeignKey, // nullable
					currency, bdMoreThan, bdLessTan, status);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	public Vector ejbHomeGetValueObjectsOrderBy(Integer pcCenter, String naForeignTable, // Customer,
																							// Supplier
			Integer naForeignKey, // nullable
			String currency, BigDecimal bdMoreThan, BigDecimal bdLessTan, boolean negate, String status, String orderBy)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsOrderBy(pcCenter, naForeignTable, // Customer,
																			// Supplier
					naForeignKey, // nullable
					currency, bdMoreThan, bdLessTan, negate, status, orderBy);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	public Vector ejbHomeGetHistoricalARBalance(Integer pcCenter, Timestamp date)
	{
		Vector vecRow = new Vector();
		try
		{
			vecRow = selectHistoricalARBalance(pcCenter, date);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecRow;
	}

   public Vector ejbHomeGetHistoricalAPBalance(Integer pcCenter, Timestamp date)
   {
      Vector vecRow = new Vector();
      try
      {
         vecRow = selectHistoricalAPBalance(pcCenter, date);
      } catch (Exception ex)
      {
         ex.printStackTrace();
      }
      return vecRow;
   }


	/***************************************************************************
	 * ejbHomeGetActiveObj
	 **************************************************************************/
	/*
	 * public Collection ejbHomeGetActiveObj() { try { Collection bufAL =
	 * getActiveObjSQL(); return bufAL; } catch( Exception ex) {
	 * Log.printDebug(strObjectName + "ejbHomeGetActiveObj: " +
	 * ex.getMessage()); return null; } }
	 */
	/***************************************************************************
	 * ejbRemove
	 **************************************************************************/
	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		try
		{
			deleteObject(this.pkid);
		} catch (Exception ex)
		{
			throw new EJBException("ejbRemove: " + ex.getMessage());
		}
	}
	
	public void remove()
	{
		Log.printVerbose(strObjectName + " In remove");
		try
		{
			ejbRemove();
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
		this.pkid = (Integer) context.getPrimaryKey();
	}

	/***************************************************************************
	 * ejbPassivate
	 **************************************************************************/
	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.pkid = null;
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
	 * ejbPostCreate (1)
	 **************************************************************************/
	public void ejbPostCreate(String namespace, String code, String foreignTable, Integer foreignKey,
			String accountType, String currency, BigDecimal amount, String remarks, Integer accPCCenterId,
			String state, Timestamp tsCreate, Integer userIdUpdate)
	{
		// nothing
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
				// Log.printVerbose("Closing connection ...");
				con.close();
			}
		} catch (SQLException ex)
		{
			throw new EJBException("closeConnection: " + ex.getMessage());
		}
	}

	public Integer insertObject(String namespace, String code, String foreignTable, Integer foreignKey,
			String accountType, String currency, BigDecimal amount, String remarks, Integer accPCCenterId,
			String state, String status, Timestamp tsCreate, Integer userIdUpdate) throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Integer nextPKId = null;
			Log.printVerbose(strObjectName + " insertObject: ");
			con = makeConnection();
			try
			{
				nextPKId = getNextPKId();
			} catch (Exception ex)
			{
				throw new EJBException(strObjectName + ex.getMessage());
			}
			// con = makeConnection();
			String insertStatement = "insert into " + TABLENAME + "(" + PKID + ", " + NAMESPACE + ", " + CODE + ", "
					+ FOREIGN_TABLE + ", " + FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", "
					+ REMARKS + ", " + ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", "
					+ USERID_EDIT + ") values ( ?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ? ) ";
			prepStmt = con.prepareStatement(insertStatement);
			prepStmt.setInt(1, nextPKId.intValue());
			prepStmt.setString(2, namespace);
			prepStmt.setString(3, code);
			prepStmt.setString(4, foreignTable);
			prepStmt.setInt(5, foreignKey.intValue());
			prepStmt.setString(6, accountType);
			prepStmt.setString(7, currency);
			prepStmt.setBigDecimal(8, amount);
			prepStmt.setString(9, remarks);
			prepStmt.setInt(10, accPCCenterId.intValue());
			prepStmt.setString(11, state);
			prepStmt.setString(12, STATUS_ACTIVE);
			prepStmt.setTimestamp(13, tsCreate);
			prepStmt.setInt(14, userIdUpdate.intValue());
			prepStmt.executeUpdate();
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving insertObject: ");
			return nextPKId;
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
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
			closeConnection(con);
		}
	}

	private boolean selectByPrimaryKey(Integer primaryKey) throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectByPrimaryKey: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + " from " + TABLENAME + " where " + PKID + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, primaryKey.intValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = rs.next();
			// prepStmt.close();
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectByPrimaryKey:");
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
			closeConnection(con);
		}
	}

	private void deleteObject(Integer pkid) throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " deleteObject: ");
			con = makeConnection();
			String deleteStatement = "delete from " + TABLENAME + " where " + PKID + " = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setInt(1, pkid.intValue());
			prepStmt.executeUpdate();
			// prepStmt.close();
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving deleteObject: ");
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
			closeConnection(con);
		}
	}

	private void loadObject() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " loadObject: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + ", " + CODE + ", " + NAMESPACE + ", " + FOREIGN_TABLE + ", "
					+ FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", " + REMARKS + ", "
					+ ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", " + USERID_EDIT
					+ " from " + TABLENAME + " where " + PKID + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.pkid = new Integer(rs.getInt(PKID)); // primary key
				this.code = rs.getString(CODE);
				this.namespace = rs.getString(NAMESPACE);
				this.foreignTable = rs.getString(FOREIGN_TABLE);
				this.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
				this.accountType = rs.getString(ACC_TYPE);
				this.currency = rs.getString(CURRENCY);
				this.amount = rs.getBigDecimal(AMOUNT);
				this.remarks = rs.getString(REMARKS);
				this.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
				this.state = rs.getString(STATE);
				this.status = rs.getString(STATUS);
				this.lastUpdate = rs.getTimestamp(LASTUPDATE);
				this.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("Row for pkid " + this.pkid.toString() + " not found in database.");
			}
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving loadObject: ");
		} catch (SQLException ex)
		{
			// Rethrow exception
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
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
			closeConnection(con);
		}
	}

	// ///////////////////////////////////////////////////////////////////
	private NominalAccountObject selectObjectGiven(Integer pkid) throws NamingException, SQLException
	{
		NominalAccountObject naObj = null;
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjectGiven: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + ", " + CODE + ", " + NAMESPACE + ", " + FOREIGN_TABLE + ", "
					+ FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", " + REMARKS + ", "
					+ ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", " + USERID_EDIT
					+ " from " + TABLENAME + " where " + PKID + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			if (rs.next())
			{
				naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt(PKID)); // primary key
				naObj.code = rs.getString(CODE);
				naObj.namespace = rs.getString(NAMESPACE);
				naObj.foreignTable = rs.getString(FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
				naObj.accountType = rs.getString(ACC_TYPE);
				naObj.currency = rs.getString(CURRENCY);
				naObj.amount = rs.getBigDecimal(AMOUNT);
				naObj.remarks = rs.getString(REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
				naObj.state = rs.getString(STATE);
				naObj.status = rs.getString(STATUS);
				naObj.lastUpdate = rs.getTimestamp(LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
				// prepStmt.close();
			}
			/*
			 * else { //prepStmt.close(); // throw new
			 * NoSuchEntityException("Row for pkid " + // this.pkid.toString() + "
			 * not found in database."); }
			 */
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectObjectGiven: ");
		}
		/*
		 * catch(SQLException ex) { // Rethrow exception ex.printStackTrace();
		 * throw ex; }
		 */catch (Exception ex)
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
		return naObj;
	}

	// ///////////////////////////////////////////////////////////////////
	private NominalAccountObject selectObjectGiven(String foreignTable, Integer accPCCenterId, Integer foreignKey,
			String currency) throws NamingException, SQLException
	{
		Log.printVerbose(" Params = " + foreignTable + " : " + accPCCenterId.toString() + " : " + foreignKey.toString()
				+ " : " + currency);
		NominalAccountObject naObj = null;
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjectGiven: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + ", " + CODE + ", " + NAMESPACE + ", " + FOREIGN_TABLE + ", "
					+ FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", " + REMARKS + ", "
					+ ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", " + USERID_EDIT
					+ " from " + TABLENAME + " where " + FOREIGN_TABLE + " = ? AND " + ACC_PCCENTER_ID + " = ? AND "
					+ CURRENCY + " = ? AND " + FOREIGN_KEY + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, foreignTable);
			prepStmt.setInt(2, accPCCenterId.intValue());
			prepStmt.setString(3, currency);
			prepStmt.setInt(4, foreignKey.intValue());
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			if (rs.next())
			{
				naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt(PKID)); // primary key
				naObj.code = rs.getString(CODE);
				naObj.namespace = rs.getString(NAMESPACE);
				naObj.foreignTable = rs.getString(FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
				naObj.accountType = rs.getString(ACC_TYPE);
				naObj.currency = rs.getString(CURRENCY);
				naObj.amount = rs.getBigDecimal(AMOUNT);
				naObj.remarks = rs.getString(REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
				naObj.state = rs.getString(STATE);
				naObj.status = rs.getString(STATUS);
				naObj.lastUpdate = rs.getTimestamp(LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
				// prepStmt.close();
			}
			/*
			 * else { //prepStmt.close(); // throw new
			 * NoSuchEntityException("Row for pkid " + // this.pkid.toString() + "
			 * not found in database."); }
			 */
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectObjectGiven: ");
		}
		/*
		 * catch(SQLException ex) { // Rethrow exception ex.printStackTrace();
		 * throw ex; }
		 */catch (Exception ex)
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
		return naObj;
	}

	private void storeObject() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " storeObject ");
			con = makeConnection();
			String updateStatement = "update " + TABLENAME + " set " + PKID + " = ?, " + CODE + " = ?, " + NAMESPACE
					+ " = ?, " + FOREIGN_TABLE + " = ?, " + FOREIGN_KEY + " = ?, " + ACC_TYPE + " = ?, " + CURRENCY
					+ " = ?, " + AMOUNT + " = ?, " + REMARKS + " = ?, " + ACC_PCCENTER_ID + " = ?, " + STATE + " = ?, "
					+ STATUS + " = ?, " + LASTUPDATE + " = ?, " + USERID_EDIT + " = ? " + "where " + PKID + " = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, this.pkid.intValue());
			prepStmt.setString(2, this.namespace);
			prepStmt.setString(3, this.code);
			prepStmt.setString(4, this.foreignTable);
			prepStmt.setInt(5, this.foreignKey.intValue());
			prepStmt.setString(6, this.accountType);
			prepStmt.setString(7, this.currency);
			prepStmt.setBigDecimal(8, this.amount);
			prepStmt.setString(9, this.remarks);
			prepStmt.setInt(10, this.accPCCenterId.intValue());
			prepStmt.setString(11, this.state);
			prepStmt.setString(12, this.status);
			prepStmt.setTimestamp(13, this.lastUpdate);
			prepStmt.setInt(14, this.userIdUpdate.intValue());
			prepStmt.setInt(15, this.pkid.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			// closeConnection(con);
			if (rowCount == 0)
			{
				throw new EJBException("Storing row for pkid " + pkid + " failed.");
			}
			Log.printVerbose(strObjectName + " Leaving storeObject: ");
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
			closeConnection(con);
		}
	}

	private Collection selectAll() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectAll: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + " from " + TABLENAME;
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			ArrayList pkIdList = new ArrayList();
			while (rs.next())
			{
				pkIdList.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectAll: ");
			return pkIdList;
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
			closeConnection(con);
		}
	}

	// ///////////////////////////////////////////////////////////
	private Vector selectValueObjectsOrderBy(Integer pcCenter, String naForeignTable, // Customer,
																						// Supplier
			Integer naForeignKey, // nullable
			String currency, BigDecimal bdMoreThan, BigDecimal bdLessThan, boolean negate, String status, String orderBy)
			throws NamingException, SQLException
	{
		Vector vecValObj = new Vector();
		NominalAccountObject naObj = null;
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjectGiven: ");
			con = makeConnection();
			String selectStatement = "SELECT " + "na." + PKID + " AS na_" + PKID + " , " + "na." + CODE + " AS na_"
					+ CODE + " , " + "na." + NAMESPACE + " AS na_" + NAMESPACE + " , " + "na." + FOREIGN_TABLE
					+ " AS na_" + FOREIGN_TABLE + " , " + "na." + FOREIGN_KEY + " AS na_" + FOREIGN_KEY + " , " + "na."
					+ ACC_TYPE + " AS na_" + ACC_TYPE + " , " + "na." + CURRENCY + " AS na_" + CURRENCY + " , " + "na."
					+ AMOUNT + " AS na_" + AMOUNT + " , " + "na." + REMARKS + " AS na_" + REMARKS + " , " + "na."
					+ ACC_PCCENTER_ID + " AS na_" + ACC_PCCENTER_ID + " , " + "na." + STATE + " AS na_" + STATE + " , "
					+ "na." + STATUS + " AS na_" + STATUS + " , " + "na." + LASTUPDATE + " AS na_" + LASTUPDATE + " , "
					+ "na." + USERID_EDIT + " AS na_" + USERID_EDIT + ", " + "ca." + PKID + " AS ca_"
					+ CustAccountBean.PKID + " , " + "ca." + CustAccountBean.CUSTCODE + " AS ca_"
					+ CustAccountBean.CUSTCODE + ", " + "ca." + CustAccountBean.NAME + " AS ca_" + CustAccountBean.NAME
					+ "  "
					// + "ca."+ CustAccountBean.DESCRIPTION +" AS ca_"
					// +CustAccountBean.DESCRIPTION + ", "
					// + "ca."+ CustAccountBean.ACCTYPE+ " AS ca_"+
					// CustAccountBean.ACCTYPE + ", "
					// + "ca."+ CustAccountBean.STATUS + " AS ca_"+
					// CustAccountBean.STATUS + ", "
					// + "ca."+ CustAccountBean.LASTUPDATE + " AS
					// ca_"+CustAccountBean.LASTUPDATE+" , "
					// + "ca."+ CustAccountBean.USERID_EDIT+" AS
					// ca_"+CustAccountBean.USERID_EDIT
					+ " FROM " + TABLENAME + " AS na INNER JOIN " + CustAccountBean.TABLENAME + " AS ca ON ( na."
					+ FOREIGN_KEY + "= ca." + CustAccountBean.PKID + " AND na." + FOREIGN_TABLE + "='" + FT_CUSTOMER
					+ "') " + " WHERE " + "na." + PKID + " != '-1' ";
			if (pcCenter != null)
			{
				selectStatement += " AND na." + ACC_PCCENTER_ID + " = '" + pcCenter.toString() + "' ";
			}
			if (naForeignTable != null)
			{
				selectStatement += " AND na." + FOREIGN_TABLE + " = '" + naForeignTable + "' ";
			}
			if (naForeignKey != null)
			{
				selectStatement += " AND na." + FOREIGN_KEY + " = '" + naForeignKey.toString() + "' ";
			}
			if (currency != null)
			{
				selectStatement += " AND na." + CURRENCY + " = '" + currency + "' ";
			}
			if (negate)
			{
				selectStatement += " AND NOT (";
				if (bdMoreThan != null)
				{
					selectStatement += " na." + AMOUNT + " > '" + bdMoreThan.toString() + "' ";
				}
				if (bdLessThan != null)
				{
					selectStatement += (bdMoreThan == null) ? "" : " AND ";
					selectStatement += " na." + AMOUNT + " < '" + bdLessThan.toString() + "' ";
				}
				selectStatement += ")";
			} else
			{
				if (bdMoreThan != null)
				{
					selectStatement += " AND na." + AMOUNT + " > '" + bdMoreThan.toString() + "' ";
				}
				if (bdLessThan != null)
				{
					selectStatement += " AND na." + AMOUNT + " < '" + bdLessThan.toString() + "' ";
				}
			}
			if (status != null)
			{
				selectStatement += " AND na." + STATUS + " = '" + status + "' ";
			}
			if (orderBy != null)
			{
				selectStatement += " ORDER BY ca." + orderBy + " ";
			}
			/*
			 * if(pcCenter!=null || naForeignTable!=null || naForeignKey!=null ||
			 * currency!=null || bdMoreThan!=null || bdLessThan!=null ||
			 * status!=null) { selectStatement = selectStatement + " where "; }
			 */
			Log.printVerbose(selectStatement);
			prepStmt = con.prepareStatement(selectStatement);
			// prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt("na_" + PKID)); // primary
																	// key
				naObj.code = rs.getString("na_" + CODE);
				naObj.namespace = rs.getString("na_" + NAMESPACE);
				naObj.foreignTable = rs.getString("na_" + FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt("na_" + FOREIGN_KEY));
				naObj.accountType = rs.getString("na_" + ACC_TYPE);
				naObj.currency = rs.getString("na_" + CURRENCY);
				naObj.amount = rs.getBigDecimal("na_" + AMOUNT);
				naObj.remarks = rs.getString("na_" + REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt("na_" + ACC_PCCENTER_ID));
				naObj.state = rs.getString("na_" + STATE);
				naObj.status = rs.getString("na_" + STATUS);
				naObj.lastUpdate = rs.getTimestamp("na_" + LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt("na_" + USERID_EDIT));
				vecValObj.add(naObj);
				// prepStmt.close();
			}
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectObjectGiven: ");
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
		return vecValObj;
	}

	// ///////////////////////////////////////////////////////////
	private Vector selectValueObjectsGiven(Integer pcCenter, String naForeignTable, // Customer,
																					// Supplier
			Integer naForeignKey, // nullable
			String currency, BigDecimal bdMoreThan, BigDecimal bdLessThan, String status) throws NamingException,
			SQLException
	{
		Vector vecValObj = new Vector();
		NominalAccountObject naObj = null;
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjectGiven: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + ", " + CODE + ", " + NAMESPACE + ", " + FOREIGN_TABLE + ", "
					+ FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", " + REMARKS + ", "
					+ ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", " + USERID_EDIT
					+ " from " + TABLENAME + " where " + PKID + " != '-1' ";
			if (pcCenter != null)
			{
				selectStatement += " AND " + ACC_PCCENTER_ID + " = '" + pcCenter.toString() + "' ";
			}
			if (naForeignTable != null)
			{
				selectStatement += " AND " + FOREIGN_TABLE + " = '" + naForeignTable + "' ";
			}
			if (naForeignKey != null)
			{
				selectStatement += " AND " + FOREIGN_KEY + " = '" + naForeignKey.toString() + "' ";
			}
			if (currency != null)
			{
				selectStatement += " AND " + CURRENCY + " = '" + currency + "' ";
			}
			if (bdMoreThan != null)
			{
				selectStatement += " AND " + AMOUNT + " > '" + bdMoreThan.toString() + "' ";
			}
			if (bdLessThan != null)
			{
				selectStatement += " AND " + AMOUNT + " < '" + bdLessThan.toString() + "' ";
			}
			if (status != null)
			{
				selectStatement += " AND " + STATUS + " = '" + status + "' ";
			}
			/*
			 * if(pcCenter!=null || naForeignTable!=null || naForeignKey!=null ||
			 * currency!=null || bdMoreThan!=null || bdLessThan!=null ||
			 * status!=null) { selectStatement = selectStatement + " where "; }
			 */
			prepStmt = con.prepareStatement(selectStatement);
			// prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt(PKID)); // primary key
				naObj.code = rs.getString(CODE);
				naObj.namespace = rs.getString(NAMESPACE);
				naObj.foreignTable = rs.getString(FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
				naObj.accountType = rs.getString(ACC_TYPE);
				naObj.currency = rs.getString(CURRENCY);
				naObj.amount = rs.getBigDecimal(AMOUNT);
				naObj.remarks = rs.getString(REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
				naObj.state = rs.getString(STATE);
				naObj.status = rs.getString(STATUS);
				naObj.lastUpdate = rs.getTimestamp(LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
				vecValObj.add(naObj);
				// prepStmt.close();
			}
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectObjectGiven: ");
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
		return vecValObj;
	}

	// ///////////////////////////////////////////////////////////
	private Vector selectValueObjectsGiven(Integer pcCenter, String NominalAccForeignTable,
	// Customer, Supplier
			Integer NominalAccForeignKey, // nullable
			String currency, // nullable
			String natForeignTable, Long natForeignKey, Timestamp dateFrom, Timestamp dateTo, String strOption)
			throws NamingException, SQLException
	{
		Vector vecValObj = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String selectStatement = " select " + "na." + PKID + " AS na_" + PKID + ", " + "na." + CODE + " AS na_"
					+ CODE + ", " + "na." + NAMESPACE + " AS na_" + NAMESPACE + ", " + "na." + FOREIGN_TABLE
					+ " AS na_" + FOREIGN_TABLE + ", " + "na." + FOREIGN_KEY + " AS na_" + FOREIGN_KEY + ", " + "na."
					+ ACC_TYPE + " AS na_" + ACC_TYPE + ", " + "na." + CURRENCY + " AS na_" + CURRENCY + ", " + "na."
					+ AMOUNT + " AS na_" + AMOUNT + ", " + "na." + REMARKS + " AS na_" + REMARKS + ", " + "na."
					+ ACC_PCCENTER_ID + " AS na_" + ACC_PCCENTER_ID + ", " + "na." + STATE + " AS na_" + STATE + ", "
					+ "na." + STATUS + " AS na_" + STATUS + ", " + "na." + LASTUPDATE + " AS na_" + LASTUPDATE + ", "
					+ "na." + USERID_EDIT + " AS na_" + USERID_EDIT + ", " + "nat." + NominalAccountTxnBean.PKID
					+ " AS nat_" + NominalAccountTxnBean.PKID + ", " + "nat." + NominalAccountTxnBean.NOMINAL_ACCOUNT
					+ " AS nat_" + NominalAccountTxnBean.NOMINAL_ACCOUNT + ", " + "nat."
					+ NominalAccountTxnBean.FOREIGN_TABLE + " AS nat_" + NominalAccountTxnBean.FOREIGN_TABLE + ", "
					+ "nat." + NominalAccountTxnBean.FOREIGN_KEY + " AS nat_" + NominalAccountTxnBean.FOREIGN_KEY
					+ ", " + "nat." + NominalAccountTxnBean.CODE + " AS nat_" + NominalAccountTxnBean.CODE + ", "
					+ "nat." + NominalAccountTxnBean.INFO1 + " AS nat_" + NominalAccountTxnBean.INFO1 + ", " + "nat."
					+ NominalAccountTxnBean.DESCRIPTION + " AS nat_" + NominalAccountTxnBean.DESCRIPTION + ", "
					+ "nat." + NominalAccountTxnBean.TXN_TYPE + " AS nat_" + NominalAccountTxnBean.TXN_TYPE + ", "
					+ "nat." + NominalAccountTxnBean.GLCODE_DEBIT + " AS nat_" + NominalAccountTxnBean.GLCODE_DEBIT
					+ ", " + "nat." + NominalAccountTxnBean.GLCODE_CREDIT + " AS nat_"
					+ NominalAccountTxnBean.GLCODE_CREDIT + ", " + "nat." + NominalAccountTxnBean.CURRENCY + " AS nat_"
					+ NominalAccountTxnBean.CURRENCY + ", " + "nat." + NominalAccountTxnBean.AMOUNT + " AS nat_"
					+ NominalAccountTxnBean.AMOUNT + ", " + "nat." + NominalAccountTxnBean.TIME_OPTION1 + " AS nat_"
					+ NominalAccountTxnBean.TIME_OPTION1 + ", " + "nat." + NominalAccountTxnBean.TIME_PARAM1
					+ " AS nat_" + NominalAccountTxnBean.TIME_PARAM1 + ", " + "nat."
					+ NominalAccountTxnBean.TIME_OPTION2 + " AS nat_" + NominalAccountTxnBean.TIME_OPTION2 + ", "
					+ "nat." + NominalAccountTxnBean.TIME_PARAM2 + " AS nat_" + NominalAccountTxnBean.TIME_PARAM2
					+ ", " + "nat." + NominalAccountTxnBean.STATE + " AS nat_" + NominalAccountTxnBean.STATE + ", "
					+ "nat." + NominalAccountTxnBean.STATUS + " AS nat_" + NominalAccountTxnBean.STATUS + ", " + "nat."
					+ NominalAccountTxnBean.LASTUPDATE + " AS nat_" + NominalAccountTxnBean.LASTUPDATE + ", " + "nat."
					+ NominalAccountTxnBean.USERID_EDIT + " AS nat_" + NominalAccountTxnBean.USERID_EDIT + " FROM "
					+ TABLENAME + " na " + " INNER JOIN " + NominalAccountTxnBean.TABLENAME + " nat " + " ON ( na."
					+ PKID + " = nat." + NominalAccountTxnBean.NOMINAL_ACCOUNT + " ) " + " where nat." + STATUS
					+ " != 'xxx' ";
			if (natForeignTable != null)
			{
				selectStatement += " AND nat." + NominalAccountTxnBean.FOREIGN_TABLE + " = '" + natForeignTable + "' ";
			}
			if (pcCenter != null)
			{
				selectStatement += " AND na." + ACC_PCCENTER_ID + " = '" + pcCenter.toString() + "' ";
			}
			if (NominalAccForeignKey != null)
			{
				selectStatement += " AND na." + FOREIGN_KEY + " = '" + NominalAccForeignKey.toString() + "' ";
			}
			if (natForeignKey != null)
			{
				selectStatement += " AND nat." + NominalAccountTxnBean.FOREIGN_KEY + " = '" + natForeignKey.toString()
						+ "' ";
			}
			if (strOption != null && strOption.equals("active"))
			{
				selectStatement += " AND nat." + NominalAccountTxnBean.STATUS + " = '"
						+ NominalAccountTxnBean.STATUS_ACTIVE + "' ";
			}
			if (NominalAccForeignTable != null)
			{
				selectStatement += " AND na." + FOREIGN_TABLE + " = '" + NominalAccForeignTable + "' ";
			}
			if (currency != null)
			{
				selectStatement += " AND na." + CURRENCY + " = '" + currency + "' ";
			}
			if (dateFrom != null)
			{
				selectStatement += " AND nat." + NominalAccountTxnBean.TIME_PARAM1 + " >= '"
						+ TimeFormat.strDisplayDate(dateFrom) + "' ";
			}
			if (dateTo != null)
			{
				selectStatement += " AND nat." + NominalAccountTxnBean.TIME_PARAM1 + " < '"
						+ TimeFormat.strDisplayDate(dateTo) + "' ";
			}
			/*
			 * +" na." + FOREIGN_TABLE + " = ? " +" AND na." + CURRENCY + " = ? " +"
			 * AND nat."+ NominalAccountTxnBean.TIME_PARAM1+ " >= ? " +" AND
			 * nat."+ NominalAccountTxnBean.TIME_PARAM1+ " < ? ";
			 */
			selectStatement += " ORDER BY nat." + NominalAccountTxnBean.TIME_PARAM1;
			prepStmt = con.prepareStatement(selectStatement);
			/*
			 * prepStmt.setString(1, NominalAccForeignTable);
			 * prepStmt.setString(2, currency); prepStmt.setTimestamp(3,
			 * dateFrom); prepStmt.setTimestamp(4, dateTo);
			 */
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				NominalAccountObject naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt("na_" + PKID)); // primary
																	// key
				naObj.code = rs.getString("na_" + CODE);
				naObj.namespace = rs.getString("na_" + NAMESPACE);
				naObj.foreignTable = rs.getString("na_" + FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt("na_" + FOREIGN_KEY));
				naObj.accountType = rs.getString("na_" + ACC_TYPE);
				naObj.currency = rs.getString("na_" + CURRENCY);
				naObj.amount = rs.getBigDecimal("na_" + AMOUNT);
				naObj.remarks = rs.getString("na_" + REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt("na_" + ACC_PCCENTER_ID));
				naObj.state = rs.getString("na_" + STATE);
				naObj.status = rs.getString("na_" + STATUS);
				naObj.lastUpdate = rs.getTimestamp("na_" + LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt("na_" + USERID_EDIT));
				naObj.vecNominalAccountTxn = new Vector();
				NominalAccountTxnObject natObj = new NominalAccountTxnObject();
				natObj.pkid = new Long(rs.getLong("nat_" + NominalAccountTxnBean.PKID)); // primary
																							// key
				natObj.nominalAccount = new Integer(rs.getInt("nat_" + NominalAccountTxnBean.NOMINAL_ACCOUNT));
				natObj.foreignTable = rs.getString("nat_" + NominalAccountTxnBean.FOREIGN_TABLE);
				natObj.foreignKey = new Long(rs.getLong("nat_" + NominalAccountTxnBean.FOREIGN_KEY));
				natObj.code = rs.getString("nat_" + NominalAccountTxnBean.CODE);
				natObj.info1 = rs.getString("nat_" + NominalAccountTxnBean.INFO1);
				natObj.description = rs.getString("nat_" + NominalAccountTxnBean.DESCRIPTION);
				natObj.txnType = rs.getString("nat_" + NominalAccountTxnBean.TXN_TYPE);
				natObj.glCodeDebit = rs.getString("nat_" + NominalAccountTxnBean.GLCODE_DEBIT);
				natObj.glCodeCredit = rs.getString("nat_" + NominalAccountTxnBean.GLCODE_CREDIT);
				natObj.currency = rs.getString("nat_" + NominalAccountTxnBean.CURRENCY);
				natObj.amount = rs.getBigDecimal("nat_" + NominalAccountTxnBean.AMOUNT);
				natObj.timeOption1 = rs.getString("nat_" + NominalAccountTxnBean.TIME_OPTION1);
				natObj.timeParam1 = rs.getTimestamp("nat_" + NominalAccountTxnBean.TIME_PARAM1);
				natObj.timeOption2 = rs.getString("nat_" + NominalAccountTxnBean.TIME_OPTION2);
				natObj.timeParam2 = rs.getTimestamp("nat_" + NominalAccountTxnBean.TIME_PARAM2);
				natObj.state = rs.getString("nat_" + NominalAccountTxnBean.STATE);
				natObj.status = rs.getString("nat_" + NominalAccountTxnBean.STATUS);
				natObj.lastUpdate = rs.getTimestamp("nat_" + NominalAccountTxnBean.LASTUPDATE);
				natObj.userIdUpdate = new Integer(rs.getInt("nat_" + NominalAccountTxnBean.USERID_EDIT));
				naObj.vecNominalAccountTxn.add(natObj);
				vecValObj.add(naObj);
				Log.printVerbose(" checkpoint 9 ");
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
			closeConnection(con);
		}
		Log.printVerbose(" checkpoint 10 ");
		return vecValObj;
	}

	// ///////////////////////////////////////////////////////////
	private Vector selectHistoricalARBalance(Integer pcCenter, Timestamp date) throws NamingException, SQLException
	{
		Vector vecRow = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			date = TimeFormat.add(date, 0, 0, 1);
			String selectStatement = "SELECT nadetails.*, cust.name, cust.acc_code, cust.telephone1, "
					+ " cust.credit_limit, cust.credit_terms FROM (SELECT na.pkid AS na_pkid, "
					+ " na.foreign_key AS acc_pkid, na.pc_center_id, nat.balance FROM acc_nominal_account "
					+ " AS na INNER JOIN ( SELECT nominal_account,sum(amount) AS balance FROM acc_nominal_account_txn "
					+ " WHERE time_param1 < '" + TimeFormat.strDisplayDate(date)
					+ "' GROUP BY nominal_account ) AS nat ON (na.pkid = nat.nominal_account) "
					+ " WHERE na.pc_center_id = '" + pcCenter.toString()
					+ "' AND na.foreign_table = 'cust_account_index') AS "
					+ " nadetails INNER JOIN cust_account_index AS cust ON "
					+ " (nadetails.acc_pkid = cust.pkid) ORDER BY cust.name;";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				HistoricalARBalanceSession.Row theRow = new HistoricalARBalanceSession.Row();
				theRow.naPkid = rs.getInt("na_pkid");
				theRow.accPkid = rs.getInt("acc_pkid");
				theRow.pcCenter = rs.getInt("pc_center_id");
				theRow.balance = rs.getBigDecimal("balance");
				theRow.name = rs.getString("name");
				theRow.accCode = rs.getString("acc_code");
				theRow.phone = rs.getString("telephone1");
				theRow.creditLimit = rs.getBigDecimal("credit_limit");
				theRow.creditTerms = rs.getBigDecimal("credit_terms");
				vecRow.add(theRow);
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
			closeConnection(con);
		}
		return vecRow;
	}

	private Vector selectHistoricalAPBalance(Integer pcCenter, Timestamp date) throws NamingException, SQLException
	{
		Vector vecRow = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			date = TimeFormat.add(date, 0, 0, 1);
			String selectStatement = "SELECT nadetails.*, supp.name, supp.acc_code, supp.telephone1, "
					+ " supp.credit_limit, supp.credit_terms FROM (SELECT na.pkid AS na_pkid, "
					+ " na.foreign_key AS acc_pkid, na.pc_center_id, nat.balance FROM acc_nominal_account "
					+ " AS na INNER JOIN ( SELECT nominal_account,sum(amount) AS balance FROM acc_nominal_account_txn "
					+ " WHERE time_param1 < '" + TimeFormat.strDisplayDate(date)
					+ "' GROUP BY nominal_account ) AS nat ON (na.pkid = nat.nominal_account) "
					+ " WHERE na.pc_center_id = '" + pcCenter.toString()
					+ "' AND na.foreign_table = 'supp_account_index') AS "
					+ " nadetails INNER JOIN supp_account_index AS supp ON "
					+ " (nadetails.acc_pkid = supp.pkid) ORDER BY supp.name;";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				HistoricalAPBalanceSession.Row theRow = new HistoricalAPBalanceSession.Row();
				theRow.naPkid = rs.getInt("na_pkid");
				theRow.accPkid = rs.getInt("acc_pkid");
				theRow.pcCenter = rs.getInt("pc_center_id");
				theRow.balance = rs.getBigDecimal("balance");
				theRow.name = rs.getString("name");
				theRow.accCode = rs.getString("acc_code");
				theRow.phone = rs.getString("telephone1");
				theRow.creditLimit = rs.getBigDecimal("credit_limit");
				theRow.creditTerms = rs.getBigDecimal("credit_terms");
				vecRow.add(theRow);
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
			closeConnection(con);
		}
		return vecRow;
	}

	private Collection selectObjects(QueryObject query) throws NamingException, SQLException, Exception
	{
		Collection col = new Vector();
		Log.printVerbose(strObjectName + " selectObjects: ");
		Connection con = null;
		con = makeConnection();
		String selectStatement = "SELECT * FROM " + TABLENAME;
		selectStatement = query.appendQuery(selectStatement);
		Log.printVerbose(" selectObjects.... " + selectStatement);
		PreparedStatement prepStmt = con.prepareStatement(selectStatement);
		ResultSet rs = prepStmt.executeQuery();
		while (rs.next())
		{
			try
			{
				NominalAccountObject oro = getObject(rs, "");
				col.add(oro);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		prepStmt.close();
		closeConnection(con);
		return col;
	}
	
	public static NominalAccountObject getObject(ResultSet rs, String prefix) throws Exception
	{
		NominalAccountObject naObj = null;
		try
		{
			naObj = new NominalAccountObject();
			naObj.pkid = new Integer(rs.getInt(PKID)); // primary key
			naObj.code = rs.getString(CODE);
			naObj.namespace = rs.getString(NAMESPACE);
			naObj.foreignTable = rs.getString(FOREIGN_TABLE);
			naObj.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
			naObj.accountType = rs.getString(ACC_TYPE);
			naObj.currency = rs.getString(CURRENCY);
			naObj.amount = rs.getBigDecimal(AMOUNT);
			naObj.remarks = rs.getString(REMARKS);
			naObj.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
			naObj.state = rs.getString(STATE);
			naObj.status = rs.getString(STATUS);
			naObj.lastUpdate = rs.getTimestamp(LASTUPDATE);
			naObj.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return naObj;
	}
	
	// ///////////////////////////////////////////////////////////
	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
			throws NamingException, SQLException
	{
		Vector vecValObj = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String selectStatement = " select " + PKID + ", " + CODE + ", " + NAMESPACE + ", " + FOREIGN_TABLE + ", "
					+ FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", " + REMARKS + ", "
					+ ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", " + USERID_EDIT
					+ " from " + TABLENAME + " where " + fieldName1 + " = ? ";
			if (fieldName2 != null && value2 != null)
			{
				selectStatement += " AND " + fieldName2 + " = ? ";
			}
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				prepStmt.setString(2, value2);
			}
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				NominalAccountObject naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt(PKID)); // primary key
				naObj.code = rs.getString(CODE);
				naObj.namespace = rs.getString(NAMESPACE);
				naObj.foreignTable = rs.getString(FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
				naObj.accountType = rs.getString(ACC_TYPE);
				naObj.currency = rs.getString(CURRENCY);
				naObj.amount = rs.getBigDecimal(AMOUNT);
				naObj.remarks = rs.getString(REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
				naObj.state = rs.getString(STATE);
				naObj.status = rs.getString(STATUS);
				naObj.lastUpdate = rs.getTimestamp(LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
				vecValObj.add(naObj);
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
			closeConnection(con);
		}
		return vecValObj;
	}

	private Collection selectObjectsGiven(String fieldName, String value) throws NamingException, SQLException
	{
		ArrayList objectSet = new ArrayList();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + value);
			con = makeConnection();
			String selectStatement = " select " + PKID + " from " + TABLENAME + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// closeConnection(con);
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
			closeConnection(con);
		}
		return objectSet;
	}

	private Integer getNextPKId() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + "In getNextPKId()");
			con = makeConnection();
			String findMaxPKIdStmt = " select max(" + PKID + ") as max_pkid from " + TABLENAME + " ";
			prepStmt = con.prepareStatement(findMaxPKIdStmt);
			ResultSet rs = prepStmt.executeQuery();
			int nextId = 0;
			if (rs.next())
			{
				nextId = rs.getInt("max_pkid") + 1;
				Log.printVerbose(strObjectName + "next pkid = " + nextId);
			} else
				throw new EJBException(strObjectName + "Error while retrieving max(pkid)");
			// prepStmt.close();
			// closeConnection(con);
			Log.printVerbose(strObjectName + "Leaving  getNextPKId()");
			return new Integer(nextId);
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
			closeConnection(con);
		}
	}
} // ObjectBean
/*==========================================================
 *
 * Copyright  of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
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
import com.vlee.ejb.customer.*;
import com.vlee.bean.reports.*;

public class NominalAccountBean implements EntityBean
{
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String CODE = "code";
	public static final String NAMESPACE = "namespace";
	public static final String FOREIGN_TABLE = "foreign_table";
	public static final String FOREIGN_KEY = "foreign_key";
	public static final String ACC_TYPE = "acc_type";
	public static final String CURRENCY = "currency";
	public static final String AMOUNT = "amount";
	public static final String REMARKS = "remarks";
	public static final String ACC_PCCENTER_ID = "pc_center_id";
	public static final String STATE = "state";
	public static final String STATUS = "status";
	public static final String LASTUPDATE = "lastupdate";
	public static final String USERID_EDIT = "userid_edit";
	// Constants for GLCODE
	public static final String GLCODE_NOMINAL = "nominal";
	// Constants for STATUS
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	// Constants for STATE
	public static final String STATE_CREATED = "created";
	// Constants for NAMESPACE
	public static final String NS_CUSTOMER = "customer";
	public static final String NS_SUPPLIER = "supplier";
	public static final String NS_GENERAL = "general";
	public static final String NS_FINANCE = "finance";
	public static final String NS_SHAREHOLDER = "shareholder";
	public static final String NS_ADMIN = "admin";
	public static final String NS_EMPLOYEE = "employee";
	public static final String NS_MISC = "miscellaneous";
	// Constants for FOREIGN_TABLE
	public static final String FT_CUSTOMER = com.vlee.ejb.customer.CustAccountBean.TABLENAME;
	public static final String FT_SUPPLIER = com.vlee.ejb.supplier.SuppAccountBean.TABLENAME;
	public static final String FT_GENERAL = com.vlee.ejb.accounting.GenericEntityAccountBean.TABLENAME;
	// Constants for ACC_TYPE
	public static final String ACC_TYPE_PAYABLE = "accPayable";
	public static final String ACC_TYPE_RECEIVABLE = "accReceivable";
	public static final String ACC_TYPE_NEUTRAL = "accNeutral";
	// Defaults
	public static final Integer DEF_ACC_PCCENTER_ID = new Integer(0);
	// Attributes of Object
	private Integer pkid; // Primary Key
	private String code;
	private String namespace;
	private String foreignTable;
	private Integer foreignKey;
	private String accountType;
	private String currency;
	private BigDecimal amount;
	private String remarks;
	private Integer accPCCenterId;
	private String state;
	private String status;
	private Timestamp lastUpdate;
	private Integer userIdUpdate;
	// DB Connection attributes
	// private Connection con = null;
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_nominal_account";
	// Other params
	private static final String strObjectName = "NominalAccountBean: ";
	// Constants
	public static final int MAX_LEN_REMARKS = 200;
	// EntityContext
	private EntityContext context = null;

	/***************************************************************************
	 * Getters
	 **************************************************************************/
	public NominalAccountObject getObject()
	{
		NominalAccountObject naObj = new NominalAccountObject();
		naObj.pkid = this.pkid;
		naObj.code = this.code;
		naObj.namespace = this.namespace;
		naObj.foreignTable = this.foreignTable;
		naObj.foreignKey = this.foreignKey;
		naObj.accountType = this.accountType;
		naObj.currency = this.currency;
		naObj.amount = this.amount;
		naObj.remarks = this.remarks;
		naObj.accPCCenterId = this.accPCCenterId;
		naObj.state = this.state;
		naObj.status = this.status;
		naObj.lastUpdate = this.lastUpdate;
		naObj.userIdUpdate = this.userIdUpdate;
		return naObj;
	}

	public Integer getPkid()
	{
		return pkid;
	}

	public String getCode()
	{
		return code;
	}

	public String getNamespace()
	{
		return namespace;
	}

	public String getForeignTable()
	{
		return foreignTable;
	}

	public Integer getForeignKey()
	{
		return foreignKey;
	}

	public String getAccountType()
	{
		return accountType;
	}

	public String getCurrency()
	{
		return currency;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public String getRemarks()
	{
		return remarks;
	}

	public Integer getPCCenterId()
	{
		return accPCCenterId;
	}

	public String getState()
	{
		return state;
	}

	public String getStatus()
	{
		return status;
	}

	public Timestamp getLastUpdate()
	{
		return lastUpdate;
	}

	public Integer getUserIdUpdate()
	{
		return userIdUpdate;
	}

	/***************************************************************************
	 * Setters
	 **************************************************************************/
	public void setPkid(Integer pkid)
	{
		this.pkid = pkid;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public void setNamespace(String namespace)
	{
		this.namespace = namespace;
	}

	public void setForeignTable(String foreignTable)
	{
		this.foreignTable = foreignTable;
	}

	public void setForeignKey(Integer foreignKey)
	{
		this.foreignKey = foreignKey;
	}

	public void setAccountType(String accountType)
	{
		this.accountType = accountType;
	}

	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
	}

	public void addAmount(BigDecimal delta)
	{
		this.amount = this.amount.add(delta);
	}

	public void setRemarks(String remarks)
	{
		this.remarks = remarks;
	}

	public void setPCCenterId(Integer accPCCenterId)
	{
		this.accPCCenterId = accPCCenterId;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public void setLastUpdate(Timestamp lastUpdate)
	{
		this.lastUpdate = lastUpdate;
	}

	public void setUserIdUpdate(Integer userIdUpdate)
	{
		this.userIdUpdate = userIdUpdate;
	}

	/***************************************************************************
	 * ejbCreate (1)
	 **************************************************************************/
	public Integer ejbCreate(String namespace, String code, String foreignTable, Integer foreignKey,
			String accountType, String currency, BigDecimal amount, String remarks, Integer accPCCenterId,
			String state, Timestamp tsCreate, Integer userIdUpdate) throws CreateException
	{
		Integer newKey = null;
		Log.printVerbose(strObjectName + "in ejbCreate");
		try
		{
			newKey = insertObject(namespace, code, foreignTable, foreignKey, accountType, currency, amount, remarks,
					accPCCenterId, state, STATUS_ACTIVE, tsCreate, userIdUpdate);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (newKey != null)
		{
			this.pkid = newKey; // primary key
			this.namespace = namespace;
			this.code = code;
			this.foreignTable = foreignTable;
			this.foreignKey = foreignKey;
			this.accountType = accountType;
			this.currency = currency;
			this.amount = amount;
			this.remarks = remarks;
			this.accPCCenterId = accPCCenterId;
			this.state = state;
			this.status = STATUS_ACTIVE;
			this.lastUpdate = tsCreate;
			this.userIdUpdate = userIdUpdate;
		}
		Log.printVerbose(strObjectName + "about to leave ejbCreate");
		return pkid;
	}

	/***************************************************************************
	 * ejbFindByPrimaryKey
	 **************************************************************************/
	public Integer ejbFindByPrimaryKey(Integer primaryKey) throws FinderException
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
	 * ejbFindObjectsGiven
	 **************************************************************************/
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

	/***************************************************************************
	 * ejbHomeGetObject
	 **************************************************************************/
	public NominalAccountObject ejbHomeGetObject(String foreignTable, Integer accPCCenterId, Integer foreignKey,
			String currency)
	{
		NominalAccountObject naObj = null;
		try
		{
			naObj = selectObjectGiven(foreignTable, accPCCenterId, foreignKey, currency);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("ERROR: " + ex.getMessage());
		}
		return naObj;
	}

	public NominalAccountObject ejbHomeGetObject(Integer pkid)
	{
		NominalAccountObject naObj = null;
		try
		{
			naObj = selectObjectGiven(pkid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("ERROR: " + ex.getMessage());
		}
		return naObj;
	}

	public Vector ejbHomeGetObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(fieldName1, value1, fieldName2, value2);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("ERROR: " + ex.getMessage());
		}
		return vecValObj;
	}

	public Vector ejbHomeGetValueObjectsGiven(Integer pcCenter, String naForeignTable, // Customer,
																						// Supplier
			Integer naForeignKey, // nullable
			String currency, String natForeignTable, Long natForeignKey, Timestamp dateFrom, Timestamp dateTo,
			String strOption)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(pcCenter, naForeignTable, naForeignKey, currency, natForeignTable,
					natForeignKey, dateFrom, dateTo, strOption);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	public Vector ejbHomeGetValueObjectsGiven(Integer pcCenter, String naForeignTable, // Customer,
																						// Supplier
			Integer naForeignKey, // nullable
			String currency, BigDecimal bdMoreThan, BigDecimal bdLessTan, String status)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(pcCenter, naForeignTable, // Customer,
																			// Supplier
					naForeignKey, // nullable
					currency, bdMoreThan, bdLessTan, status);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	public Vector ejbHomeGetValueObjectsOrderBy(Integer pcCenter, String naForeignTable, // Customer,
																							// Supplier
			Integer naForeignKey, // nullable
			String currency, BigDecimal bdMoreThan, BigDecimal bdLessTan, boolean negate, String status, String orderBy)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsOrderBy(pcCenter, naForeignTable, // Customer,
																			// Supplier
					naForeignKey, // nullable
					currency, bdMoreThan, bdLessTan, negate, status, orderBy);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	public Vector ejbHomeGetHistoricalARBalance(Integer pcCenter, Timestamp date)
	{
		Vector vecRow = new Vector();
		try
		{
			vecRow = selectHistoricalARBalance(pcCenter, date);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecRow;
	}

	/***************************************************************************
	 * ejbHomeGetActiveObj
	 **************************************************************************/
	/*
	 * public Collection ejbHomeGetActiveObj() { try { Collection bufAL =
	 * getActiveObjSQL(); return bufAL; } catch( Exception ex) {
	 * Log.printDebug(strObjectName + "ejbHomeGetActiveObj: " +
	 * ex.getMessage()); return null; } }
	 */
	/***************************************************************************
	 * ejbRemove
	 **************************************************************************/
	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		try
		{
			deleteObject(this.pkid);
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
		this.pkid = (Integer) context.getPrimaryKey();
	}

	/***************************************************************************
	 * ejbPassivate
	 **************************************************************************/
	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.pkid = null;
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
	 * ejbPostCreate (1)
	 **************************************************************************/
	public void ejbPostCreate(String namespace, String code, String foreignTable, Integer foreignKey,
			String accountType, String currency, BigDecimal amount, String remarks, Integer accPCCenterId,
			String state, Timestamp tsCreate, Integer userIdUpdate)
	{
		// nothing
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
				// Log.printVerbose("Closing connection ...");
				con.close();
			}
		} catch (SQLException ex)
		{
			throw new EJBException("closeConnection: " + ex.getMessage());
		}
	}

	public Integer insertObject(String namespace, String code, String foreignTable, Integer foreignKey,
			String accountType, String currency, BigDecimal amount, String remarks, Integer accPCCenterId,
			String state, String status, Timestamp tsCreate, Integer userIdUpdate) throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Integer nextPKId = null;
			Log.printVerbose(strObjectName + " insertObject: ");
			con = makeConnection();
			try
			{
				nextPKId = getNextPKId();
			} catch (Exception ex)
			{
				throw new EJBException(strObjectName + ex.getMessage());
			}
			// con = makeConnection();
			String insertStatement = "insert into " + TABLENAME + "(" + PKID + ", " + NAMESPACE + ", " + CODE + ", "
					+ FOREIGN_TABLE + ", " + FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", "
					+ REMARKS + ", " + ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", "
					+ USERID_EDIT + ") values ( ?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ? ) ";
			prepStmt = con.prepareStatement(insertStatement);
			prepStmt.setInt(1, nextPKId.intValue());
			prepStmt.setString(2, namespace);
			prepStmt.setString(3, code);
			prepStmt.setString(4, foreignTable);
			prepStmt.setInt(5, foreignKey.intValue());
			prepStmt.setString(6, accountType);
			prepStmt.setString(7, currency);
			prepStmt.setBigDecimal(8, amount);
			prepStmt.setString(9, remarks);
			prepStmt.setInt(10, accPCCenterId.intValue());
			prepStmt.setString(11, state);
			prepStmt.setString(12, STATUS_ACTIVE);
			prepStmt.setTimestamp(13, tsCreate);
			prepStmt.setInt(14, userIdUpdate.intValue());
			prepStmt.executeUpdate();
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving insertObject: ");
			return nextPKId;
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
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
			closeConnection(con);
		}
	}

	private boolean selectByPrimaryKey(Integer primaryKey) throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectByPrimaryKey: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + " from " + TABLENAME + " where " + PKID + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, primaryKey.intValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = rs.next();
			// prepStmt.close();
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectByPrimaryKey:");
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
			closeConnection(con);
		}
	}

	private void deleteObject(Integer pkid) throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " deleteObject: ");
			con = makeConnection();
			String deleteStatement = "delete from " + TABLENAME + " where " + PKID + " = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setInt(1, pkid.intValue());
			prepStmt.executeUpdate();
			// prepStmt.close();
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving deleteObject: ");
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
			closeConnection(con);
		}
	}

	private void loadObject() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " loadObject: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + ", " + CODE + ", " + NAMESPACE + ", " + FOREIGN_TABLE + ", "
					+ FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", " + REMARKS + ", "
					+ ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", " + USERID_EDIT
					+ " from " + TABLENAME + " where " + PKID + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.pkid = new Integer(rs.getInt(PKID)); // primary key
				this.code = rs.getString(CODE);
				this.namespace = rs.getString(NAMESPACE);
				this.foreignTable = rs.getString(FOREIGN_TABLE);
				this.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
				this.accountType = rs.getString(ACC_TYPE);
				this.currency = rs.getString(CURRENCY);
				this.amount = rs.getBigDecimal(AMOUNT);
				this.remarks = rs.getString(REMARKS);
				this.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
				this.state = rs.getString(STATE);
				this.status = rs.getString(STATUS);
				this.lastUpdate = rs.getTimestamp(LASTUPDATE);
				this.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("Row for pkid " + this.pkid.toString() + " not found in database.");
			}
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving loadObject: ");
		} catch (SQLException ex)
		{
			// Rethrow exception
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
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
			closeConnection(con);
		}
	}

	// ///////////////////////////////////////////////////////////////////
	private NominalAccountObject selectObjectGiven(Integer pkid) throws NamingException, SQLException
	{
		NominalAccountObject naObj = null;
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjectGiven: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + ", " + CODE + ", " + NAMESPACE + ", " + FOREIGN_TABLE + ", "
					+ FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", " + REMARKS + ", "
					+ ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", " + USERID_EDIT
					+ " from " + TABLENAME + " where " + PKID + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			if (rs.next())
			{
				naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt(PKID)); // primary key
				naObj.code = rs.getString(CODE);
				naObj.namespace = rs.getString(NAMESPACE);
				naObj.foreignTable = rs.getString(FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
				naObj.accountType = rs.getString(ACC_TYPE);
				naObj.currency = rs.getString(CURRENCY);
				naObj.amount = rs.getBigDecimal(AMOUNT);
				naObj.remarks = rs.getString(REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
				naObj.state = rs.getString(STATE);
				naObj.status = rs.getString(STATUS);
				naObj.lastUpdate = rs.getTimestamp(LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
				// prepStmt.close();
			}
			/*
			 * else { //prepStmt.close(); // throw new
			 * NoSuchEntityException("Row for pkid " + // this.pkid.toString() + "
			 * not found in database."); }
			 */
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectObjectGiven: ");
		}
		/*
		 * catch(SQLException ex) { // Rethrow exception ex.printStackTrace();
		 * throw ex; }
		 */catch (Exception ex)
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
		return naObj;
	}

	// ///////////////////////////////////////////////////////////////////
	private NominalAccountObject selectObjectGiven(String foreignTable, Integer accPCCenterId, Integer foreignKey,
			String currency) throws NamingException, SQLException
	{
		Log.printVerbose(" Params = " + foreignTable + " : " + accPCCenterId.toString() + " : " + foreignKey.toString()
				+ " : " + currency);
		NominalAccountObject naObj = null;
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjectGiven: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + ", " + CODE + ", " + NAMESPACE + ", " + FOREIGN_TABLE + ", "
					+ FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", " + REMARKS + ", "
					+ ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", " + USERID_EDIT
					+ " from " + TABLENAME + " where " + FOREIGN_TABLE + " = ? AND " + ACC_PCCENTER_ID + " = ? AND "
					+ CURRENCY + " = ? AND " + FOREIGN_KEY + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, foreignTable);
			prepStmt.setInt(2, accPCCenterId.intValue());
			prepStmt.setString(3, currency);
			prepStmt.setInt(4, foreignKey.intValue());
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			if (rs.next())
			{
				naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt(PKID)); // primary key
				naObj.code = rs.getString(CODE);
				naObj.namespace = rs.getString(NAMESPACE);
				naObj.foreignTable = rs.getString(FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
				naObj.accountType = rs.getString(ACC_TYPE);
				naObj.currency = rs.getString(CURRENCY);
				naObj.amount = rs.getBigDecimal(AMOUNT);
				naObj.remarks = rs.getString(REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
				naObj.state = rs.getString(STATE);
				naObj.status = rs.getString(STATUS);
				naObj.lastUpdate = rs.getTimestamp(LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
				// prepStmt.close();
			}
			/*
			 * else { //prepStmt.close(); // throw new
			 * NoSuchEntityException("Row for pkid " + // this.pkid.toString() + "
			 * not found in database."); }
			 */
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectObjectGiven: ");
		}
		/*
		 * catch(SQLException ex) { // Rethrow exception ex.printStackTrace();
		 * throw ex; }
		 */catch (Exception ex)
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
		return naObj;
	}

	private void storeObject() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " storeObject ");
			con = makeConnection();
			String updateStatement = "update " + TABLENAME + " set " + PKID + " = ?, " + CODE + " = ?, " + NAMESPACE
					+ " = ?, " + FOREIGN_TABLE + " = ?, " + FOREIGN_KEY + " = ?, " + ACC_TYPE + " = ?, " + CURRENCY
					+ " = ?, " + AMOUNT + " = ?, " + REMARKS + " = ?, " + ACC_PCCENTER_ID + " = ?, " + STATE + " = ?, "
					+ STATUS + " = ?, " + LASTUPDATE + " = ?, " + USERID_EDIT + " = ? " + "where " + PKID + " = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, this.pkid.intValue());
			prepStmt.setString(2, this.namespace);
			prepStmt.setString(3, this.code);
			prepStmt.setString(4, this.foreignTable);
			prepStmt.setInt(5, this.foreignKey.intValue());
			prepStmt.setString(6, this.accountType);
			prepStmt.setString(7, this.currency);
			prepStmt.setBigDecimal(8, this.amount);
			prepStmt.setString(9, this.remarks);
			prepStmt.setInt(10, this.accPCCenterId.intValue());
			prepStmt.setString(11, this.state);
			prepStmt.setString(12, this.status);
			prepStmt.setTimestamp(13, this.lastUpdate);
			prepStmt.setInt(14, this.userIdUpdate.intValue());
			prepStmt.setInt(15, this.pkid.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			// closeConnection(con);
			if (rowCount == 0)
			{
				throw new EJBException("Storing row for pkid " + pkid + " failed.");
			}
			Log.printVerbose(strObjectName + " Leaving storeObject: ");
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
			closeConnection(con);
		}
	}

	private Collection selectAll() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectAll: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + " from " + TABLENAME;
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			ArrayList pkIdList = new ArrayList();
			while (rs.next())
			{
				pkIdList.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectAll: ");
			return pkIdList;
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
			closeConnection(con);
		}
	}

	// ///////////////////////////////////////////////////////////
	private Vector selectValueObjectsOrderBy(Integer pcCenter, String naForeignTable, // Customer,
																						// Supplier
			Integer naForeignKey, // nullable
			String currency, BigDecimal bdMoreThan, BigDecimal bdLessThan, boolean negate, String status, String orderBy)
			throws NamingException, SQLException
	{
		Vector vecValObj = new Vector();
		NominalAccountObject naObj = null;
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjectGiven: ");
			con = makeConnection();
			String selectStatement = "SELECT " + "na." + PKID + " AS na_" + PKID + " , " + "na." + CODE + " AS na_"
					+ CODE + " , " + "na." + NAMESPACE + " AS na_" + NAMESPACE + " , " + "na." + FOREIGN_TABLE
					+ " AS na_" + FOREIGN_TABLE + " , " + "na." + FOREIGN_KEY + " AS na_" + FOREIGN_KEY + " , " + "na."
					+ ACC_TYPE + " AS na_" + ACC_TYPE + " , " + "na." + CURRENCY + " AS na_" + CURRENCY + " , " + "na."
					+ AMOUNT + " AS na_" + AMOUNT + " , " + "na." + REMARKS + " AS na_" + REMARKS + " , " + "na."
					+ ACC_PCCENTER_ID + " AS na_" + ACC_PCCENTER_ID + " , " + "na." + STATE + " AS na_" + STATE + " , "
					+ "na." + STATUS + " AS na_" + STATUS + " , " + "na." + LASTUPDATE + " AS na_" + LASTUPDATE + " , "
					+ "na." + USERID_EDIT + " AS na_" + USERID_EDIT + ", " + "ca." + PKID + " AS ca_"
					+ CustAccountBean.PKID + " , " + "ca." + CustAccountBean.CUSTCODE + " AS ca_"
					+ CustAccountBean.CUSTCODE + ", " + "ca." + CustAccountBean.NAME + " AS ca_" + CustAccountBean.NAME
					+ "  "
					// + "ca."+ CustAccountBean.DESCRIPTION +" AS ca_"
					// +CustAccountBean.DESCRIPTION + ", "
					// + "ca."+ CustAccountBean.ACCTYPE+ " AS ca_"+
					// CustAccountBean.ACCTYPE + ", "
					// + "ca."+ CustAccountBean.STATUS + " AS ca_"+
					// CustAccountBean.STATUS + ", "
					// + "ca."+ CustAccountBean.LASTUPDATE + " AS
					// ca_"+CustAccountBean.LASTUPDATE+" , "
					// + "ca."+ CustAccountBean.USERID_EDIT+" AS
					// ca_"+CustAccountBean.USERID_EDIT
					+ " FROM " + TABLENAME + " AS na INNER JOIN " + CustAccountBean.TABLENAME + " AS ca ON ( na."
					+ FOREIGN_KEY + "= ca." + CustAccountBean.PKID + " AND na." + FOREIGN_TABLE + "='" + FT_CUSTOMER
					+ "') " + " WHERE " + "na." + PKID + " != '-1' ";
			if (pcCenter != null)
			{
				selectStatement += " AND na." + ACC_PCCENTER_ID + " = '" + pcCenter.toString() + "' ";
			}
			if (naForeignTable != null)
			{
				selectStatement += " AND na." + FOREIGN_TABLE + " = '" + naForeignTable + "' ";
			}
			if (naForeignKey != null)
			{
				selectStatement += " AND na." + FOREIGN_KEY + " = '" + naForeignKey.toString() + "' ";
			}
			if (currency != null)
			{
				selectStatement += " AND na." + CURRENCY + " = '" + currency + "' ";
			}
			if (negate)
			{
				selectStatement += " AND NOT (";
				if (bdMoreThan != null)
				{
					selectStatement += " na." + AMOUNT + " > '" + bdMoreThan.toString() + "' ";
				}
				if (bdLessThan != null)
				{
					selectStatement += (bdMoreThan == null) ? "" : " AND ";
					selectStatement += " na." + AMOUNT + " < '" + bdLessThan.toString() + "' ";
				}
				selectStatement += ")";
			} else
			{
				if (bdMoreThan != null)
				{
					selectStatement += " AND na." + AMOUNT + " > '" + bdMoreThan.toString() + "' ";
				}
				if (bdLessThan != null)
				{
					selectStatement += " AND na." + AMOUNT + " < '" + bdLessThan.toString() + "' ";
				}
			}
			if (status != null)
			{
				selectStatement += " AND na." + STATUS + " = '" + status + "' ";
			}
			if (orderBy != null)
			{
				selectStatement += " ORDER BY ca." + orderBy + " ";
			}
			/*
			 * if(pcCenter!=null || naForeignTable!=null || naForeignKey!=null ||
			 * currency!=null || bdMoreThan!=null || bdLessThan!=null ||
			 * status!=null) { selectStatement = selectStatement + " where "; }
			 */
			Log.printVerbose(selectStatement);
			prepStmt = con.prepareStatement(selectStatement);
			// prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt("na_" + PKID)); // primary
																	// key
				naObj.code = rs.getString("na_" + CODE);
				naObj.namespace = rs.getString("na_" + NAMESPACE);
				naObj.foreignTable = rs.getString("na_" + FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt("na_" + FOREIGN_KEY));
				naObj.accountType = rs.getString("na_" + ACC_TYPE);
				naObj.currency = rs.getString("na_" + CURRENCY);
				naObj.amount = rs.getBigDecimal("na_" + AMOUNT);
				naObj.remarks = rs.getString("na_" + REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt("na_" + ACC_PCCENTER_ID));
				naObj.state = rs.getString("na_" + STATE);
				naObj.status = rs.getString("na_" + STATUS);
				naObj.lastUpdate = rs.getTimestamp("na_" + LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt("na_" + USERID_EDIT));
				vecValObj.add(naObj);
				// prepStmt.close();
			}
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectObjectGiven: ");
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
		return vecValObj;
	}

	// ///////////////////////////////////////////////////////////
	private Vector selectValueObjectsGiven(Integer pcCenter, String naForeignTable, // Customer,
																					// Supplier
			Integer naForeignKey, // nullable
			String currency, BigDecimal bdMoreThan, BigDecimal bdLessThan, String status) throws NamingException,
			SQLException
	{
		Vector vecValObj = new Vector();
		NominalAccountObject naObj = null;
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjectGiven: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + ", " + CODE + ", " + NAMESPACE + ", " + FOREIGN_TABLE + ", "
					+ FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", " + REMARKS + ", "
					+ ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", " + USERID_EDIT
					+ " from " + TABLENAME + " where " + PKID + " != '-1' ";
			if (pcCenter != null)
			{
				selectStatement += " AND " + ACC_PCCENTER_ID + " = '" + pcCenter.toString() + "' ";
			}
			if (naForeignTable != null)
			{
				selectStatement += " AND " + FOREIGN_TABLE + " = '" + naForeignTable + "' ";
			}
			if (naForeignKey != null)
			{
				selectStatement += " AND " + FOREIGN_KEY + " = '" + naForeignKey.toString() + "' ";
			}
			if (currency != null)
			{
				selectStatement += " AND " + CURRENCY + " = '" + currency + "' ";
			}
			if (bdMoreThan != null)
			{
				selectStatement += " AND " + AMOUNT + " > '" + bdMoreThan.toString() + "' ";
			}
			if (bdLessThan != null)
			{
				selectStatement += " AND " + AMOUNT + " < '" + bdLessThan.toString() + "' ";
			}
			if (status != null)
			{
				selectStatement += " AND " + STATUS + " = '" + status + "' ";
			}
			/*
			 * if(pcCenter!=null || naForeignTable!=null || naForeignKey!=null ||
			 * currency!=null || bdMoreThan!=null || bdLessThan!=null ||
			 * status!=null) { selectStatement = selectStatement + " where "; }
			 */
			prepStmt = con.prepareStatement(selectStatement);
			// prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt(PKID)); // primary key
				naObj.code = rs.getString(CODE);
				naObj.namespace = rs.getString(NAMESPACE);
				naObj.foreignTable = rs.getString(FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
				naObj.accountType = rs.getString(ACC_TYPE);
				naObj.currency = rs.getString(CURRENCY);
				naObj.amount = rs.getBigDecimal(AMOUNT);
				naObj.remarks = rs.getString(REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
				naObj.state = rs.getString(STATE);
				naObj.status = rs.getString(STATUS);
				naObj.lastUpdate = rs.getTimestamp(LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
				vecValObj.add(naObj);
				// prepStmt.close();
			}
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectObjectGiven: ");
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
		return vecValObj;
	}

	// ///////////////////////////////////////////////////////////
	private Vector selectValueObjectsGiven(Integer pcCenter, String NominalAccForeignTable,
	// Customer, Supplier
			Integer NominalAccForeignKey, // nullable
			String currency, // nullable
			String natForeignTable, Long natForeignKey, Timestamp dateFrom, Timestamp dateTo, String strOption)
			throws NamingException, SQLException
	{
		Vector vecValObj = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String selectStatement = " select " + "na." + PKID + " AS na_" + PKID + ", " + "na." + CODE + " AS na_"
					+ CODE + ", " + "na." + NAMESPACE + " AS na_" + NAMESPACE + ", " + "na." + FOREIGN_TABLE
					+ " AS na_" + FOREIGN_TABLE + ", " + "na." + FOREIGN_KEY + " AS na_" + FOREIGN_KEY + ", " + "na."
					+ ACC_TYPE + " AS na_" + ACC_TYPE + ", " + "na." + CURRENCY + " AS na_" + CURRENCY + ", " + "na."
					+ AMOUNT + " AS na_" + AMOUNT + ", " + "na." + REMARKS + " AS na_" + REMARKS + ", " + "na."
					+ ACC_PCCENTER_ID + " AS na_" + ACC_PCCENTER_ID + ", " + "na." + STATE + " AS na_" + STATE + ", "
					+ "na." + STATUS + " AS na_" + STATUS + ", " + "na." + LASTUPDATE + " AS na_" + LASTUPDATE + ", "
					+ "na." + USERID_EDIT + " AS na_" + USERID_EDIT + ", " + "nat." + NominalAccountTxnBean.PKID
					+ " AS nat_" + NominalAccountTxnBean.PKID + ", " + "nat." + NominalAccountTxnBean.NOMINAL_ACCOUNT
					+ " AS nat_" + NominalAccountTxnBean.NOMINAL_ACCOUNT + ", " + "nat."
					+ NominalAccountTxnBean.FOREIGN_TABLE + " AS nat_" + NominalAccountTxnBean.FOREIGN_TABLE + ", "
					+ "nat." + NominalAccountTxnBean.FOREIGN_KEY + " AS nat_" + NominalAccountTxnBean.FOREIGN_KEY
					+ ", " + "nat." + NominalAccountTxnBean.CODE + " AS nat_" + NominalAccountTxnBean.CODE + ", "
					+ "nat." + NominalAccountTxnBean.INFO1 + " AS nat_" + NominalAccountTxnBean.INFO1 + ", " + "nat."
					+ NominalAccountTxnBean.DESCRIPTION + " AS nat_" + NominalAccountTxnBean.DESCRIPTION + ", "
					+ "nat." + NominalAccountTxnBean.TXN_TYPE + " AS nat_" + NominalAccountTxnBean.TXN_TYPE + ", "
					+ "nat." + NominalAccountTxnBean.GLCODE_DEBIT + " AS nat_" + NominalAccountTxnBean.GLCODE_DEBIT
					+ ", " + "nat." + NominalAccountTxnBean.GLCODE_CREDIT + " AS nat_"
					+ NominalAccountTxnBean.GLCODE_CREDIT + ", " + "nat." + NominalAccountTxnBean.CURRENCY + " AS nat_"
					+ NominalAccountTxnBean.CURRENCY + ", " + "nat." + NominalAccountTxnBean.AMOUNT + " AS nat_"
					+ NominalAccountTxnBean.AMOUNT + ", " + "nat." + NominalAccountTxnBean.TIME_OPTION1 + " AS nat_"
					+ NominalAccountTxnBean.TIME_OPTION1 + ", " + "nat." + NominalAccountTxnBean.TIME_PARAM1
					+ " AS nat_" + NominalAccountTxnBean.TIME_PARAM1 + ", " + "nat."
					+ NominalAccountTxnBean.TIME_OPTION2 + " AS nat_" + NominalAccountTxnBean.TIME_OPTION2 + ", "
					+ "nat." + NominalAccountTxnBean.TIME_PARAM2 + " AS nat_" + NominalAccountTxnBean.TIME_PARAM2
					+ ", " + "nat." + NominalAccountTxnBean.STATE + " AS nat_" + NominalAccountTxnBean.STATE + ", "
					+ "nat." + NominalAccountTxnBean.STATUS + " AS nat_" + NominalAccountTxnBean.STATUS + ", " + "nat."
					+ NominalAccountTxnBean.LASTUPDATE + " AS nat_" + NominalAccountTxnBean.LASTUPDATE + ", " + "nat."
					+ NominalAccountTxnBean.USERID_EDIT + " AS nat_" + NominalAccountTxnBean.USERID_EDIT + " FROM "
					+ TABLENAME + " na " + " INNER JOIN " + NominalAccountTxnBean.TABLENAME + " nat " + " ON ( na."
					+ PKID + " = nat." + NominalAccountTxnBean.NOMINAL_ACCOUNT + " ) " + " where nat." + STATUS
					+ " != 'xxx' ";
			if (natForeignTable != null)
			{
				selectStatement += " AND nat." + NominalAccountTxnBean.FOREIGN_TABLE + " = '" + natForeignTable + "' ";
			}
			if (pcCenter != null)
			{
				selectStatement += " AND na." + ACC_PCCENTER_ID + " = '" + pcCenter.toString() + "' ";
			}
			if (NominalAccForeignKey != null)
			{
				selectStatement += " AND na." + FOREIGN_KEY + " = '" + NominalAccForeignKey.toString() + "' ";
			}
			if (natForeignKey != null)
			{
				selectStatement += " AND nat." + NominalAccountTxnBean.FOREIGN_KEY + " = '" + natForeignKey.toString()
						+ "' ";
			}
			if (strOption != null && strOption.equals("active"))
			{
				selectStatement += " AND nat." + NominalAccountTxnBean.STATUS + " = '"
						+ NominalAccountTxnBean.STATUS_ACTIVE + "' ";
			}
			if (NominalAccForeignTable != null)
			{
				selectStatement += " AND na." + FOREIGN_TABLE + " = '" + NominalAccForeignTable + "' ";
			}
			if (currency != null)
			{
				selectStatement += " AND na." + CURRENCY + " = '" + currency + "' ";
			}
			if (dateFrom != null)
			{
				selectStatement += " AND nat." + NominalAccountTxnBean.TIME_PARAM1 + " >= '"
						+ TimeFormat.strDisplayDate(dateFrom) + "' ";
			}
			if (dateTo != null)
			{
				selectStatement += " AND nat." + NominalAccountTxnBean.TIME_PARAM1 + " < '"
						+ TimeFormat.strDisplayDate(dateTo) + "' ";
			}
			/*
			 * +" na." + FOREIGN_TABLE + " = ? " +" AND na." + CURRENCY + " = ? " +"
			 * AND nat."+ NominalAccountTxnBean.TIME_PARAM1+ " >= ? " +" AND
			 * nat."+ NominalAccountTxnBean.TIME_PARAM1+ " < ? ";
			 */
			selectStatement += " ORDER BY nat." + NominalAccountTxnBean.TIME_PARAM1;
			prepStmt = con.prepareStatement(selectStatement);
			/*
			 * prepStmt.setString(1, NominalAccForeignTable);
			 * prepStmt.setString(2, currency); prepStmt.setTimestamp(3,
			 * dateFrom); prepStmt.setTimestamp(4, dateTo);
			 */
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				NominalAccountObject naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt("na_" + PKID)); // primary
																	// key
				naObj.code = rs.getString("na_" + CODE);
				naObj.namespace = rs.getString("na_" + NAMESPACE);
				naObj.foreignTable = rs.getString("na_" + FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt("na_" + FOREIGN_KEY));
				naObj.accountType = rs.getString("na_" + ACC_TYPE);
				naObj.currency = rs.getString("na_" + CURRENCY);
				naObj.amount = rs.getBigDecimal("na_" + AMOUNT);
				naObj.remarks = rs.getString("na_" + REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt("na_" + ACC_PCCENTER_ID));
				naObj.state = rs.getString("na_" + STATE);
				naObj.status = rs.getString("na_" + STATUS);
				naObj.lastUpdate = rs.getTimestamp("na_" + LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt("na_" + USERID_EDIT));
				naObj.vecNominalAccountTxn = new Vector();
				NominalAccountTxnObject natObj = new NominalAccountTxnObject();
				natObj.pkid = new Long(rs.getLong("nat_" + NominalAccountTxnBean.PKID)); // primary
																							// key
				natObj.nominalAccount = new Integer(rs.getInt("nat_" + NominalAccountTxnBean.NOMINAL_ACCOUNT));
				natObj.foreignTable = rs.getString("nat_" + NominalAccountTxnBean.FOREIGN_TABLE);
				natObj.foreignKey = new Long(rs.getLong("nat_" + NominalAccountTxnBean.FOREIGN_KEY));
				natObj.code = rs.getString("nat_" + NominalAccountTxnBean.CODE);
				natObj.info1 = rs.getString("nat_" + NominalAccountTxnBean.INFO1);
				natObj.description = rs.getString("nat_" + NominalAccountTxnBean.DESCRIPTION);
				natObj.txnType = rs.getString("nat_" + NominalAccountTxnBean.TXN_TYPE);
				natObj.glCodeDebit = rs.getString("nat_" + NominalAccountTxnBean.GLCODE_DEBIT);
				natObj.glCodeCredit = rs.getString("nat_" + NominalAccountTxnBean.GLCODE_CREDIT);
				natObj.currency = rs.getString("nat_" + NominalAccountTxnBean.CURRENCY);
				natObj.amount = rs.getBigDecimal("nat_" + NominalAccountTxnBean.AMOUNT);
				natObj.timeOption1 = rs.getString("nat_" + NominalAccountTxnBean.TIME_OPTION1);
				natObj.timeParam1 = rs.getTimestamp("nat_" + NominalAccountTxnBean.TIME_PARAM1);
				natObj.timeOption2 = rs.getString("nat_" + NominalAccountTxnBean.TIME_OPTION2);
				natObj.timeParam2 = rs.getTimestamp("nat_" + NominalAccountTxnBean.TIME_PARAM2);
				natObj.state = rs.getString("nat_" + NominalAccountTxnBean.STATE);
				natObj.status = rs.getString("nat_" + NominalAccountTxnBean.STATUS);
				natObj.lastUpdate = rs.getTimestamp("nat_" + NominalAccountTxnBean.LASTUPDATE);
				natObj.userIdUpdate = new Integer(rs.getInt("nat_" + NominalAccountTxnBean.USERID_EDIT));
				naObj.vecNominalAccountTxn.add(natObj);
				vecValObj.add(naObj);
				Log.printVerbose(" checkpoint 9 ");
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
			closeConnection(con);
		}
		Log.printVerbose(" checkpoint 10 ");
		return vecValObj;
	}

	// ///////////////////////////////////////////////////////////
	private Vector selectHistoricalARBalance(Integer pcCenter, Timestamp date) throws NamingException, SQLException
	{
		Vector vecRow = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			date = TimeFormat.add(date, 0, 0, 1);
			String selectStatement = "SELECT nadetails.*, cust.name, cust.acc_code, cust.telephone1, "
					+ " cust.credit_limit, cust.credit_terms FROM (SELECT na.pkid AS na_pkid, "
					+ " na.foreign_key AS acc_pkid, na.pc_center_id, nat.balance FROM acc_nominal_account "
					+ " AS na INNER JOIN ( SELECT nominal_account,sum(amount) AS balance FROM acc_nominal_account_txn "
					+ " WHERE time_param1 < '" + TimeFormat.strDisplayDate(date)
					+ "' GROUP BY nominal_account ) AS nat ON (na.pkid = nat.nominal_account) "
					+ " WHERE na.pc_center_id = '" + pcCenter.toString()
					+ "' AND na.foreign_table = 'cust_account_index') AS "
					+ " nadetails INNER JOIN cust_account_index AS cust ON "
					+ " (nadetails.acc_pkid = cust.pkid) ORDER BY cust.name;";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				HistoricalARBalanceSession.Row theRow = new HistoricalARBalanceSession.Row();
				theRow.naPkid = rs.getInt("na_pkid");
				theRow.accPkid = rs.getInt("acc_pkid");
				theRow.pcCenter = rs.getInt("pc_center_id");
				theRow.balance = rs.getBigDecimal("balance");
				theRow.name = rs.getString("name");
				theRow.accCode = rs.getString("acc_code");
				theRow.phone = rs.getString("telephone1");
				theRow.creditLimit = rs.getBigDecimal("credit_limit");
				theRow.creditTerms = rs.getBigDecimal("credit_terms");
				vecRow.add(theRow);
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
			closeConnection(con);
		}
		return vecRow;
	}

	// ///////////////////////////////////////////////////////////
	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
			throws NamingException, SQLException
	{
		Vector vecValObj = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String selectStatement = " select " + PKID + ", " + CODE + ", " + NAMESPACE + ", " + FOREIGN_TABLE + ", "
					+ FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", " + REMARKS + ", "
					+ ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", " + USERID_EDIT
					+ " from " + TABLENAME + " where " + fieldName1 + " = ? ";
			if (fieldName2 != null && value2 != null)
			{
				selectStatement += " AND " + fieldName2 + " = ? ";
			}
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				prepStmt.setString(2, value2);
			}
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				NominalAccountObject naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt(PKID)); // primary key
				naObj.code = rs.getString(CODE);
				naObj.namespace = rs.getString(NAMESPACE);
				naObj.foreignTable = rs.getString(FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
				naObj.accountType = rs.getString(ACC_TYPE);
				naObj.currency = rs.getString(CURRENCY);
				naObj.amount = rs.getBigDecimal(AMOUNT);
				naObj.remarks = rs.getString(REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
				naObj.state = rs.getString(STATE);
				naObj.status = rs.getString(STATUS);
				naObj.lastUpdate = rs.getTimestamp(LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
				vecValObj.add(naObj);
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
			closeConnection(con);
		}
		return vecValObj;
	}

	private Collection selectObjectsGiven(String fieldName, String value) throws NamingException, SQLException
	{
		ArrayList objectSet = new ArrayList();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + value);
			con = makeConnection();
			String selectStatement = " select " + PKID + " from " + TABLENAME + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// closeConnection(con);
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
			closeConnection(con);
		}
		return objectSet;
	}

	private Integer getNextPKId() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + "In getNextPKId()");
			con = makeConnection();
			String findMaxPKIdStmt = " select max(" + PKID + ") as max_pkid from " + TABLENAME + " ";
			prepStmt = con.prepareStatement(findMaxPKIdStmt);
			ResultSet rs = prepStmt.executeQuery();
			int nextId = 0;
			if (rs.next())
			{
				nextId = rs.getInt("max_pkid") + 1;
				Log.printVerbose(strObjectName + "next pkid = " + nextId);
			} else
				throw new EJBException(strObjectName + "Error while retrieving max(pkid)");
			// prepStmt.close();
			// closeConnection(con);
			Log.printVerbose(strObjectName + "Leaving  getNextPKId()");
			return new Integer(nextId);
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
			closeConnection(con);
		}
	}
} // ObjectBean
/*==========================================================
 *
 * Copyright  of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
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
import com.vlee.ejb.customer.*;
import com.vlee.bean.reports.*;

public class NominalAccountBean implements EntityBean
{
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String CODE = "code";
	public static final String NAMESPACE = "namespace";
	public static final String FOREIGN_TABLE = "foreign_table";
	public static final String FOREIGN_KEY = "foreign_key";
	public static final String ACC_TYPE = "acc_type";
	public static final String CURRENCY = "currency";
	public static final String AMOUNT = "amount";
	public static final String REMARKS = "remarks";
	public static final String ACC_PCCENTER_ID = "pc_center_id";
	public static final String STATE = "state";
	public static final String STATUS = "status";
	public static final String LASTUPDATE = "lastupdate";
	public static final String USERID_EDIT = "userid_edit";
	// Constants for GLCODE
	public static final String GLCODE_NOMINAL = "nominal";
	// Constants for STATUS
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	// Constants for STATE
	public static final String STATE_CREATED = "created";
	// Constants for NAMESPACE
	public static final String NS_CUSTOMER = "customer";
	public static final String NS_SUPPLIER = "supplier";
	public static final String NS_GENERAL = "general";
	public static final String NS_FINANCE = "finance";
	public static final String NS_SHAREHOLDER = "shareholder";
	public static final String NS_ADMIN = "admin";
	public static final String NS_EMPLOYEE = "employee";
	public static final String NS_MISC = "miscellaneous";
	// Constants for FOREIGN_TABLE
	public static final String FT_CUSTOMER = com.vlee.ejb.customer.CustAccountBean.TABLENAME;
	public static final String FT_SUPPLIER = com.vlee.ejb.supplier.SuppAccountBean.TABLENAME;
	public static final String FT_GENERAL = com.vlee.ejb.accounting.GenericEntityAccountBean.TABLENAME;
	// Constants for ACC_TYPE
	public static final String ACC_TYPE_PAYABLE = "accPayable";
	public static final String ACC_TYPE_RECEIVABLE = "accReceivable";
	public static final String ACC_TYPE_NEUTRAL = "accNeutral";
	// Defaults
	public static final Integer DEF_ACC_PCCENTER_ID = new Integer(0);
	// Attributes of Object
	private Integer pkid; // Primary Key
	private String code;
	private String namespace;
	private String foreignTable;
	private Integer foreignKey;
	private String accountType;
	private String currency;
	private BigDecimal amount;
	private String remarks;
	private Integer accPCCenterId;
	private String state;
	private String status;
	private Timestamp lastUpdate;
	private Integer userIdUpdate;
	// DB Connection attributes
	// private Connection con = null;
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_nominal_account";
	// Other params
	private static final String strObjectName = "NominalAccountBean: ";
	// Constants
	public static final int MAX_LEN_REMARKS = 200;
	// EntityContext
	private EntityContext context = null;

	/***************************************************************************
	 * Getters
	 **************************************************************************/
	public NominalAccountObject getObject()
	{
		NominalAccountObject naObj = new NominalAccountObject();
		naObj.pkid = this.pkid;
		naObj.code = this.code;
		naObj.namespace = this.namespace;
		naObj.foreignTable = this.foreignTable;
		naObj.foreignKey = this.foreignKey;
		naObj.accountType = this.accountType;
		naObj.currency = this.currency;
		naObj.amount = this.amount;
		naObj.remarks = this.remarks;
		naObj.accPCCenterId = this.accPCCenterId;
		naObj.state = this.state;
		naObj.status = this.status;
		naObj.lastUpdate = this.lastUpdate;
		naObj.userIdUpdate = this.userIdUpdate;
		return naObj;
	}

	public Integer getPkid()
	{
		return pkid;
	}

	public String getCode()
	{
		return code;
	}

	public String getNamespace()
	{
		return namespace;
	}

	public String getForeignTable()
	{
		return foreignTable;
	}

	public Integer getForeignKey()
	{
		return foreignKey;
	}

	public String getAccountType()
	{
		return accountType;
	}

	public String getCurrency()
	{
		return currency;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public String getRemarks()
	{
		return remarks;
	}

	public Integer getPCCenterId()
	{
		return accPCCenterId;
	}

	public String getState()
	{
		return state;
	}

	public String getStatus()
	{
		return status;
	}

	public Timestamp getLastUpdate()
	{
		return lastUpdate;
	}

	public Integer getUserIdUpdate()
	{
		return userIdUpdate;
	}

	/***************************************************************************
	 * Setters
	 **************************************************************************/
	public void setPkid(Integer pkid)
	{
		this.pkid = pkid;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public void setNamespace(String namespace)
	{
		this.namespace = namespace;
	}

	public void setForeignTable(String foreignTable)
	{
		this.foreignTable = foreignTable;
	}

	public void setForeignKey(Integer foreignKey)
	{
		this.foreignKey = foreignKey;
	}

	public void setAccountType(String accountType)
	{
		this.accountType = accountType;
	}

	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
	}

	public void addAmount(BigDecimal delta)
	{
		this.amount = this.amount.add(delta);
	}

	public void setRemarks(String remarks)
	{
		this.remarks = remarks;
	}

	public void setPCCenterId(Integer accPCCenterId)
	{
		this.accPCCenterId = accPCCenterId;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public void setLastUpdate(Timestamp lastUpdate)
	{
		this.lastUpdate = lastUpdate;
	}

	public void setUserIdUpdate(Integer userIdUpdate)
	{
		this.userIdUpdate = userIdUpdate;
	}

	/***************************************************************************
	 * ejbCreate (1)
	 **************************************************************************/
	public Integer ejbCreate(String namespace, String code, String foreignTable, Integer foreignKey,
			String accountType, String currency, BigDecimal amount, String remarks, Integer accPCCenterId,
			String state, Timestamp tsCreate, Integer userIdUpdate) throws CreateException
	{
		Integer newKey = null;
		Log.printVerbose(strObjectName + "in ejbCreate");
		try
		{
			newKey = insertObject(namespace, code, foreignTable, foreignKey, accountType, currency, amount, remarks,
					accPCCenterId, state, STATUS_ACTIVE, tsCreate, userIdUpdate);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (newKey != null)
		{
			this.pkid = newKey; // primary key
			this.namespace = namespace;
			this.code = code;
			this.foreignTable = foreignTable;
			this.foreignKey = foreignKey;
			this.accountType = accountType;
			this.currency = currency;
			this.amount = amount;
			this.remarks = remarks;
			this.accPCCenterId = accPCCenterId;
			this.state = state;
			this.status = STATUS_ACTIVE;
			this.lastUpdate = tsCreate;
			this.userIdUpdate = userIdUpdate;
		}
		Log.printVerbose(strObjectName + "about to leave ejbCreate");
		return pkid;
	}

	/***************************************************************************
	 * ejbFindByPrimaryKey
	 **************************************************************************/
	public Integer ejbFindByPrimaryKey(Integer primaryKey) throws FinderException
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
	 * ejbFindObjectsGiven
	 **************************************************************************/
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

	/***************************************************************************
	 * ejbHomeGetObject
	 **************************************************************************/
	public NominalAccountObject ejbHomeGetObject(String foreignTable, Integer accPCCenterId, Integer foreignKey,
			String currency)
	{
		NominalAccountObject naObj = null;
		try
		{
			naObj = selectObjectGiven(foreignTable, accPCCenterId, foreignKey, currency);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("ERROR: " + ex.getMessage());
		}
		return naObj;
	}

	public NominalAccountObject ejbHomeGetObject(Integer pkid)
	{
		NominalAccountObject naObj = null;
		try
		{
			naObj = selectObjectGiven(pkid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("ERROR: " + ex.getMessage());
		}
		return naObj;
	}

	public Vector ejbHomeGetObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(fieldName1, value1, fieldName2, value2);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("ERROR: " + ex.getMessage());
		}
		return vecValObj;
	}

	public Vector ejbHomeGetValueObjectsGiven(Integer pcCenter, String naForeignTable, // Customer,
																						// Supplier
			Integer naForeignKey, // nullable
			String currency, String natForeignTable, Long natForeignKey, Timestamp dateFrom, Timestamp dateTo,
			String strOption)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(pcCenter, naForeignTable, naForeignKey, currency, natForeignTable,
					natForeignKey, dateFrom, dateTo, strOption);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	public Vector ejbHomeGetValueObjectsGiven(Integer pcCenter, String naForeignTable, // Customer,
																						// Supplier
			Integer naForeignKey, // nullable
			String currency, BigDecimal bdMoreThan, BigDecimal bdLessTan, String status)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(pcCenter, naForeignTable, // Customer,
																			// Supplier
					naForeignKey, // nullable
					currency, bdMoreThan, bdLessTan, status);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	public Vector ejbHomeGetValueObjectsOrderBy(Integer pcCenter, String naForeignTable, // Customer,
																							// Supplier
			Integer naForeignKey, // nullable
			String currency, BigDecimal bdMoreThan, BigDecimal bdLessTan, boolean negate, String status, String orderBy)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsOrderBy(pcCenter, naForeignTable, // Customer,
																			// Supplier
					naForeignKey, // nullable
					currency, bdMoreThan, bdLessTan, negate, status, orderBy);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	public Vector ejbHomeGetHistoricalARBalance(Integer pcCenter, Timestamp date)
	{
		Vector vecRow = new Vector();
		try
		{
			vecRow = selectHistoricalARBalance(pcCenter, date);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecRow;
	}

	/***************************************************************************
	 * ejbHomeGetActiveObj
	 **************************************************************************/
	/*
	 * public Collection ejbHomeGetActiveObj() { try { Collection bufAL =
	 * getActiveObjSQL(); return bufAL; } catch( Exception ex) {
	 * Log.printDebug(strObjectName + "ejbHomeGetActiveObj: " +
	 * ex.getMessage()); return null; } }
	 */
	/***************************************************************************
	 * ejbRemove
	 **************************************************************************/
	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		try
		{
			deleteObject(this.pkid);
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
		this.pkid = (Integer) context.getPrimaryKey();
	}

	/***************************************************************************
	 * ejbPassivate
	 **************************************************************************/
	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.pkid = null;
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
	 * ejbPostCreate (1)
	 **************************************************************************/
	public void ejbPostCreate(String namespace, String code, String foreignTable, Integer foreignKey,
			String accountType, String currency, BigDecimal amount, String remarks, Integer accPCCenterId,
			String state, Timestamp tsCreate, Integer userIdUpdate)
	{
		// nothing
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
				// Log.printVerbose("Closing connection ...");
				con.close();
			}
		} catch (SQLException ex)
		{
			throw new EJBException("closeConnection: " + ex.getMessage());
		}
	}

	public Integer insertObject(String namespace, String code, String foreignTable, Integer foreignKey,
			String accountType, String currency, BigDecimal amount, String remarks, Integer accPCCenterId,
			String state, String status, Timestamp tsCreate, Integer userIdUpdate) throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Integer nextPKId = null;
			Log.printVerbose(strObjectName + " insertObject: ");
			con = makeConnection();
			try
			{
				nextPKId = getNextPKId();
			} catch (Exception ex)
			{
				throw new EJBException(strObjectName + ex.getMessage());
			}
			// con = makeConnection();
			String insertStatement = "insert into " + TABLENAME + "(" + PKID + ", " + NAMESPACE + ", " + CODE + ", "
					+ FOREIGN_TABLE + ", " + FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", "
					+ REMARKS + ", " + ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", "
					+ USERID_EDIT + ") values ( ?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ? ) ";
			prepStmt = con.prepareStatement(insertStatement);
			prepStmt.setInt(1, nextPKId.intValue());
			prepStmt.setString(2, namespace);
			prepStmt.setString(3, code);
			prepStmt.setString(4, foreignTable);
			prepStmt.setInt(5, foreignKey.intValue());
			prepStmt.setString(6, accountType);
			prepStmt.setString(7, currency);
			prepStmt.setBigDecimal(8, amount);
			prepStmt.setString(9, remarks);
			prepStmt.setInt(10, accPCCenterId.intValue());
			prepStmt.setString(11, state);
			prepStmt.setString(12, STATUS_ACTIVE);
			prepStmt.setTimestamp(13, tsCreate);
			prepStmt.setInt(14, userIdUpdate.intValue());
			prepStmt.executeUpdate();
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving insertObject: ");
			return nextPKId;
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
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
			closeConnection(con);
		}
	}

	private boolean selectByPrimaryKey(Integer primaryKey) throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectByPrimaryKey: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + " from " + TABLENAME + " where " + PKID + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, primaryKey.intValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = rs.next();
			// prepStmt.close();
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectByPrimaryKey:");
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
			closeConnection(con);
		}
	}

	private void deleteObject(Integer pkid) throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " deleteObject: ");
			con = makeConnection();
			String deleteStatement = "delete from " + TABLENAME + " where " + PKID + " = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setInt(1, pkid.intValue());
			prepStmt.executeUpdate();
			// prepStmt.close();
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving deleteObject: ");
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
			closeConnection(con);
		}
	}

	private void loadObject() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " loadObject: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + ", " + CODE + ", " + NAMESPACE + ", " + FOREIGN_TABLE + ", "
					+ FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", " + REMARKS + ", "
					+ ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", " + USERID_EDIT
					+ " from " + TABLENAME + " where " + PKID + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.pkid = new Integer(rs.getInt(PKID)); // primary key
				this.code = rs.getString(CODE);
				this.namespace = rs.getString(NAMESPACE);
				this.foreignTable = rs.getString(FOREIGN_TABLE);
				this.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
				this.accountType = rs.getString(ACC_TYPE);
				this.currency = rs.getString(CURRENCY);
				this.amount = rs.getBigDecimal(AMOUNT);
				this.remarks = rs.getString(REMARKS);
				this.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
				this.state = rs.getString(STATE);
				this.status = rs.getString(STATUS);
				this.lastUpdate = rs.getTimestamp(LASTUPDATE);
				this.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("Row for pkid " + this.pkid.toString() + " not found in database.");
			}
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving loadObject: ");
		} catch (SQLException ex)
		{
			// Rethrow exception
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
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
			closeConnection(con);
		}
	}

	// ///////////////////////////////////////////////////////////////////
	private NominalAccountObject selectObjectGiven(Integer pkid) throws NamingException, SQLException
	{
		NominalAccountObject naObj = null;
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjectGiven: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + ", " + CODE + ", " + NAMESPACE + ", " + FOREIGN_TABLE + ", "
					+ FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", " + REMARKS + ", "
					+ ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", " + USERID_EDIT
					+ " from " + TABLENAME + " where " + PKID + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			if (rs.next())
			{
				naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt(PKID)); // primary key
				naObj.code = rs.getString(CODE);
				naObj.namespace = rs.getString(NAMESPACE);
				naObj.foreignTable = rs.getString(FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
				naObj.accountType = rs.getString(ACC_TYPE);
				naObj.currency = rs.getString(CURRENCY);
				naObj.amount = rs.getBigDecimal(AMOUNT);
				naObj.remarks = rs.getString(REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
				naObj.state = rs.getString(STATE);
				naObj.status = rs.getString(STATUS);
				naObj.lastUpdate = rs.getTimestamp(LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
				// prepStmt.close();
			}
			/*
			 * else { //prepStmt.close(); // throw new
			 * NoSuchEntityException("Row for pkid " + // this.pkid.toString() + "
			 * not found in database."); }
			 */
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectObjectGiven: ");
		}
		/*
		 * catch(SQLException ex) { // Rethrow exception ex.printStackTrace();
		 * throw ex; }
		 */catch (Exception ex)
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
		return naObj;
	}

	// ///////////////////////////////////////////////////////////////////
	private NominalAccountObject selectObjectGiven(String foreignTable, Integer accPCCenterId, Integer foreignKey,
			String currency) throws NamingException, SQLException
	{
		Log.printVerbose(" Params = " + foreignTable + " : " + accPCCenterId.toString() + " : " + foreignKey.toString()
				+ " : " + currency);
		NominalAccountObject naObj = null;
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjectGiven: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + ", " + CODE + ", " + NAMESPACE + ", " + FOREIGN_TABLE + ", "
					+ FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", " + REMARKS + ", "
					+ ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", " + USERID_EDIT
					+ " from " + TABLENAME + " where " + FOREIGN_TABLE + " = ? AND " + ACC_PCCENTER_ID + " = ? AND "
					+ CURRENCY + " = ? AND " + FOREIGN_KEY + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, foreignTable);
			prepStmt.setInt(2, accPCCenterId.intValue());
			prepStmt.setString(3, currency);
			prepStmt.setInt(4, foreignKey.intValue());
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			if (rs.next())
			{
				naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt(PKID)); // primary key
				naObj.code = rs.getString(CODE);
				naObj.namespace = rs.getString(NAMESPACE);
				naObj.foreignTable = rs.getString(FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
				naObj.accountType = rs.getString(ACC_TYPE);
				naObj.currency = rs.getString(CURRENCY);
				naObj.amount = rs.getBigDecimal(AMOUNT);
				naObj.remarks = rs.getString(REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
				naObj.state = rs.getString(STATE);
				naObj.status = rs.getString(STATUS);
				naObj.lastUpdate = rs.getTimestamp(LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
				// prepStmt.close();
			}
			/*
			 * else { //prepStmt.close(); // throw new
			 * NoSuchEntityException("Row for pkid " + // this.pkid.toString() + "
			 * not found in database."); }
			 */
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectObjectGiven: ");
		}
		/*
		 * catch(SQLException ex) { // Rethrow exception ex.printStackTrace();
		 * throw ex; }
		 */catch (Exception ex)
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
		return naObj;
	}

	private void storeObject() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " storeObject ");
			con = makeConnection();
			String updateStatement = "update " + TABLENAME + " set " + PKID + " = ?, " + CODE + " = ?, " + NAMESPACE
					+ " = ?, " + FOREIGN_TABLE + " = ?, " + FOREIGN_KEY + " = ?, " + ACC_TYPE + " = ?, " + CURRENCY
					+ " = ?, " + AMOUNT + " = ?, " + REMARKS + " = ?, " + ACC_PCCENTER_ID + " = ?, " + STATE + " = ?, "
					+ STATUS + " = ?, " + LASTUPDATE + " = ?, " + USERID_EDIT + " = ? " + "where " + PKID + " = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, this.pkid.intValue());
			prepStmt.setString(2, this.namespace);
			prepStmt.setString(3, this.code);
			prepStmt.setString(4, this.foreignTable);
			prepStmt.setInt(5, this.foreignKey.intValue());
			prepStmt.setString(6, this.accountType);
			prepStmt.setString(7, this.currency);
			prepStmt.setBigDecimal(8, this.amount);
			prepStmt.setString(9, this.remarks);
			prepStmt.setInt(10, this.accPCCenterId.intValue());
			prepStmt.setString(11, this.state);
			prepStmt.setString(12, this.status);
			prepStmt.setTimestamp(13, this.lastUpdate);
			prepStmt.setInt(14, this.userIdUpdate.intValue());
			prepStmt.setInt(15, this.pkid.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			// closeConnection(con);
			if (rowCount == 0)
			{
				throw new EJBException("Storing row for pkid " + pkid + " failed.");
			}
			Log.printVerbose(strObjectName + " Leaving storeObject: ");
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
			closeConnection(con);
		}
	}

	private Collection selectAll() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectAll: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + " from " + TABLENAME;
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			ArrayList pkIdList = new ArrayList();
			while (rs.next())
			{
				pkIdList.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectAll: ");
			return pkIdList;
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
			closeConnection(con);
		}
	}

	// ///////////////////////////////////////////////////////////
	private Vector selectValueObjectsOrderBy(Integer pcCenter, String naForeignTable, // Customer,
																						// Supplier
			Integer naForeignKey, // nullable
			String currency, BigDecimal bdMoreThan, BigDecimal bdLessThan, boolean negate, String status, String orderBy)
			throws NamingException, SQLException
	{
		Vector vecValObj = new Vector();
		NominalAccountObject naObj = null;
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjectGiven: ");
			con = makeConnection();
			String selectStatement = "SELECT " + "na." + PKID + " AS na_" + PKID + " , " + "na." + CODE + " AS na_"
					+ CODE + " , " + "na." + NAMESPACE + " AS na_" + NAMESPACE + " , " + "na." + FOREIGN_TABLE
					+ " AS na_" + FOREIGN_TABLE + " , " + "na." + FOREIGN_KEY + " AS na_" + FOREIGN_KEY + " , " + "na."
					+ ACC_TYPE + " AS na_" + ACC_TYPE + " , " + "na." + CURRENCY + " AS na_" + CURRENCY + " , " + "na."
					+ AMOUNT + " AS na_" + AMOUNT + " , " + "na." + REMARKS + " AS na_" + REMARKS + " , " + "na."
					+ ACC_PCCENTER_ID + " AS na_" + ACC_PCCENTER_ID + " , " + "na." + STATE + " AS na_" + STATE + " , "
					+ "na." + STATUS + " AS na_" + STATUS + " , " + "na." + LASTUPDATE + " AS na_" + LASTUPDATE + " , "
					+ "na." + USERID_EDIT + " AS na_" + USERID_EDIT + ", " + "ca." + PKID + " AS ca_"
					+ CustAccountBean.PKID + " , " + "ca." + CustAccountBean.CUSTCODE + " AS ca_"
					+ CustAccountBean.CUSTCODE + ", " + "ca." + CustAccountBean.NAME + " AS ca_" + CustAccountBean.NAME
					+ "  "
					// + "ca."+ CustAccountBean.DESCRIPTION +" AS ca_"
					// +CustAccountBean.DESCRIPTION + ", "
					// + "ca."+ CustAccountBean.ACCTYPE+ " AS ca_"+
					// CustAccountBean.ACCTYPE + ", "
					// + "ca."+ CustAccountBean.STATUS + " AS ca_"+
					// CustAccountBean.STATUS + ", "
					// + "ca."+ CustAccountBean.LASTUPDATE + " AS
					// ca_"+CustAccountBean.LASTUPDATE+" , "
					// + "ca."+ CustAccountBean.USERID_EDIT+" AS
					// ca_"+CustAccountBean.USERID_EDIT
					+ " FROM " + TABLENAME + " AS na INNER JOIN " + CustAccountBean.TABLENAME + " AS ca ON ( na."
					+ FOREIGN_KEY + "= ca." + CustAccountBean.PKID + " AND na." + FOREIGN_TABLE + "='" + FT_CUSTOMER
					+ "') " + " WHERE " + "na." + PKID + " != '-1' ";
			if (pcCenter != null)
			{
				selectStatement += " AND na." + ACC_PCCENTER_ID + " = '" + pcCenter.toString() + "' ";
			}
			if (naForeignTable != null)
			{
				selectStatement += " AND na." + FOREIGN_TABLE + " = '" + naForeignTable + "' ";
			}
			if (naForeignKey != null)
			{
				selectStatement += " AND na." + FOREIGN_KEY + " = '" + naForeignKey.toString() + "' ";
			}
			if (currency != null)
			{
				selectStatement += " AND na." + CURRENCY + " = '" + currency + "' ";
			}
			if (negate)
			{
				selectStatement += " AND NOT (";
				if (bdMoreThan != null)
				{
					selectStatement += " na." + AMOUNT + " > '" + bdMoreThan.toString() + "' ";
				}
				if (bdLessThan != null)
				{
					selectStatement += (bdMoreThan == null) ? "" : " AND ";
					selectStatement += " na." + AMOUNT + " < '" + bdLessThan.toString() + "' ";
				}
				selectStatement += ")";
			} else
			{
				if (bdMoreThan != null)
				{
					selectStatement += " AND na." + AMOUNT + " > '" + bdMoreThan.toString() + "' ";
				}
				if (bdLessThan != null)
				{
					selectStatement += " AND na." + AMOUNT + " < '" + bdLessThan.toString() + "' ";
				}
			}
			if (status != null)
			{
				selectStatement += " AND na." + STATUS + " = '" + status + "' ";
			}
			if (orderBy != null)
			{
				selectStatement += " ORDER BY ca." + orderBy + " ";
			}
			/*
			 * if(pcCenter!=null || naForeignTable!=null || naForeignKey!=null ||
			 * currency!=null || bdMoreThan!=null || bdLessThan!=null ||
			 * status!=null) { selectStatement = selectStatement + " where "; }
			 */
			Log.printVerbose(selectStatement);
			prepStmt = con.prepareStatement(selectStatement);
			// prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt("na_" + PKID)); // primary
																	// key
				naObj.code = rs.getString("na_" + CODE);
				naObj.namespace = rs.getString("na_" + NAMESPACE);
				naObj.foreignTable = rs.getString("na_" + FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt("na_" + FOREIGN_KEY));
				naObj.accountType = rs.getString("na_" + ACC_TYPE);
				naObj.currency = rs.getString("na_" + CURRENCY);
				naObj.amount = rs.getBigDecimal("na_" + AMOUNT);
				naObj.remarks = rs.getString("na_" + REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt("na_" + ACC_PCCENTER_ID));
				naObj.state = rs.getString("na_" + STATE);
				naObj.status = rs.getString("na_" + STATUS);
				naObj.lastUpdate = rs.getTimestamp("na_" + LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt("na_" + USERID_EDIT));
				vecValObj.add(naObj);
				// prepStmt.close();
			}
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectObjectGiven: ");
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
		return vecValObj;
	}

	// ///////////////////////////////////////////////////////////
	private Vector selectValueObjectsGiven(Integer pcCenter, String naForeignTable, // Customer,
																					// Supplier
			Integer naForeignKey, // nullable
			String currency, BigDecimal bdMoreThan, BigDecimal bdLessThan, String status) throws NamingException,
			SQLException
	{
		Vector vecValObj = new Vector();
		NominalAccountObject naObj = null;
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjectGiven: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + ", " + CODE + ", " + NAMESPACE + ", " + FOREIGN_TABLE + ", "
					+ FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", " + REMARKS + ", "
					+ ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", " + USERID_EDIT
					+ " from " + TABLENAME + " where " + PKID + " != '-1' ";
			if (pcCenter != null)
			{
				selectStatement += " AND " + ACC_PCCENTER_ID + " = '" + pcCenter.toString() + "' ";
			}
			if (naForeignTable != null)
			{
				selectStatement += " AND " + FOREIGN_TABLE + " = '" + naForeignTable + "' ";
			}
			if (naForeignKey != null)
			{
				selectStatement += " AND " + FOREIGN_KEY + " = '" + naForeignKey.toString() + "' ";
			}
			if (currency != null)
			{
				selectStatement += " AND " + CURRENCY + " = '" + currency + "' ";
			}
			if (bdMoreThan != null)
			{
				selectStatement += " AND " + AMOUNT + " > '" + bdMoreThan.toString() + "' ";
			}
			if (bdLessThan != null)
			{
				selectStatement += " AND " + AMOUNT + " < '" + bdLessThan.toString() + "' ";
			}
			if (status != null)
			{
				selectStatement += " AND " + STATUS + " = '" + status + "' ";
			}
			/*
			 * if(pcCenter!=null || naForeignTable!=null || naForeignKey!=null ||
			 * currency!=null || bdMoreThan!=null || bdLessThan!=null ||
			 * status!=null) { selectStatement = selectStatement + " where "; }
			 */
			prepStmt = con.prepareStatement(selectStatement);
			// prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt(PKID)); // primary key
				naObj.code = rs.getString(CODE);
				naObj.namespace = rs.getString(NAMESPACE);
				naObj.foreignTable = rs.getString(FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
				naObj.accountType = rs.getString(ACC_TYPE);
				naObj.currency = rs.getString(CURRENCY);
				naObj.amount = rs.getBigDecimal(AMOUNT);
				naObj.remarks = rs.getString(REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
				naObj.state = rs.getString(STATE);
				naObj.status = rs.getString(STATUS);
				naObj.lastUpdate = rs.getTimestamp(LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
				vecValObj.add(naObj);
				// prepStmt.close();
			}
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectObjectGiven: ");
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
		return vecValObj;
	}

	// ///////////////////////////////////////////////////////////
	private Vector selectValueObjectsGiven(Integer pcCenter, String NominalAccForeignTable,
	// Customer, Supplier
			Integer NominalAccForeignKey, // nullable
			String currency, // nullable
			String natForeignTable, Long natForeignKey, Timestamp dateFrom, Timestamp dateTo, String strOption)
			throws NamingException, SQLException
	{
		Vector vecValObj = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String selectStatement = " select " + "na." + PKID + " AS na_" + PKID + ", " + "na." + CODE + " AS na_"
					+ CODE + ", " + "na." + NAMESPACE + " AS na_" + NAMESPACE + ", " + "na." + FOREIGN_TABLE
					+ " AS na_" + FOREIGN_TABLE + ", " + "na." + FOREIGN_KEY + " AS na_" + FOREIGN_KEY + ", " + "na."
					+ ACC_TYPE + " AS na_" + ACC_TYPE + ", " + "na." + CURRENCY + " AS na_" + CURRENCY + ", " + "na."
					+ AMOUNT + " AS na_" + AMOUNT + ", " + "na." + REMARKS + " AS na_" + REMARKS + ", " + "na."
					+ ACC_PCCENTER_ID + " AS na_" + ACC_PCCENTER_ID + ", " + "na." + STATE + " AS na_" + STATE + ", "
					+ "na." + STATUS + " AS na_" + STATUS + ", " + "na." + LASTUPDATE + " AS na_" + LASTUPDATE + ", "
					+ "na." + USERID_EDIT + " AS na_" + USERID_EDIT + ", " + "nat." + NominalAccountTxnBean.PKID
					+ " AS nat_" + NominalAccountTxnBean.PKID + ", " + "nat." + NominalAccountTxnBean.NOMINAL_ACCOUNT
					+ " AS nat_" + NominalAccountTxnBean.NOMINAL_ACCOUNT + ", " + "nat."
					+ NominalAccountTxnBean.FOREIGN_TABLE + " AS nat_" + NominalAccountTxnBean.FOREIGN_TABLE + ", "
					+ "nat." + NominalAccountTxnBean.FOREIGN_KEY + " AS nat_" + NominalAccountTxnBean.FOREIGN_KEY
					+ ", " + "nat." + NominalAccountTxnBean.CODE + " AS nat_" + NominalAccountTxnBean.CODE + ", "
					+ "nat." + NominalAccountTxnBean.INFO1 + " AS nat_" + NominalAccountTxnBean.INFO1 + ", " + "nat."
					+ NominalAccountTxnBean.DESCRIPTION + " AS nat_" + NominalAccountTxnBean.DESCRIPTION + ", "
					+ "nat." + NominalAccountTxnBean.TXN_TYPE + " AS nat_" + NominalAccountTxnBean.TXN_TYPE + ", "
					+ "nat." + NominalAccountTxnBean.GLCODE_DEBIT + " AS nat_" + NominalAccountTxnBean.GLCODE_DEBIT
					+ ", " + "nat." + NominalAccountTxnBean.GLCODE_CREDIT + " AS nat_"
					+ NominalAccountTxnBean.GLCODE_CREDIT + ", " + "nat." + NominalAccountTxnBean.CURRENCY + " AS nat_"
					+ NominalAccountTxnBean.CURRENCY + ", " + "nat." + NominalAccountTxnBean.AMOUNT + " AS nat_"
					+ NominalAccountTxnBean.AMOUNT + ", " + "nat." + NominalAccountTxnBean.TIME_OPTION1 + " AS nat_"
					+ NominalAccountTxnBean.TIME_OPTION1 + ", " + "nat." + NominalAccountTxnBean.TIME_PARAM1
					+ " AS nat_" + NominalAccountTxnBean.TIME_PARAM1 + ", " + "nat."
					+ NominalAccountTxnBean.TIME_OPTION2 + " AS nat_" + NominalAccountTxnBean.TIME_OPTION2 + ", "
					+ "nat." + NominalAccountTxnBean.TIME_PARAM2 + " AS nat_" + NominalAccountTxnBean.TIME_PARAM2
					+ ", " + "nat." + NominalAccountTxnBean.STATE + " AS nat_" + NominalAccountTxnBean.STATE + ", "
					+ "nat." + NominalAccountTxnBean.STATUS + " AS nat_" + NominalAccountTxnBean.STATUS + ", " + "nat."
					+ NominalAccountTxnBean.LASTUPDATE + " AS nat_" + NominalAccountTxnBean.LASTUPDATE + ", " + "nat."
					+ NominalAccountTxnBean.USERID_EDIT + " AS nat_" + NominalAccountTxnBean.USERID_EDIT + " FROM "
					+ TABLENAME + " na " + " INNER JOIN " + NominalAccountTxnBean.TABLENAME + " nat " + " ON ( na."
					+ PKID + " = nat." + NominalAccountTxnBean.NOMINAL_ACCOUNT + " ) " + " where nat." + STATUS
					+ " != 'xxx' ";
			if (natForeignTable != null)
			{
				selectStatement += " AND nat." + NominalAccountTxnBean.FOREIGN_TABLE + " = '" + natForeignTable + "' ";
			}
			if (pcCenter != null)
			{
				selectStatement += " AND na." + ACC_PCCENTER_ID + " = '" + pcCenter.toString() + "' ";
			}
			if (NominalAccForeignKey != null)
			{
				selectStatement += " AND na." + FOREIGN_KEY + " = '" + NominalAccForeignKey.toString() + "' ";
			}
			if (natForeignKey != null)
			{
				selectStatement += " AND nat." + NominalAccountTxnBean.FOREIGN_KEY + " = '" + natForeignKey.toString()
						+ "' ";
			}
			if (strOption != null && strOption.equals("active"))
			{
				selectStatement += " AND nat." + NominalAccountTxnBean.STATUS + " = '"
						+ NominalAccountTxnBean.STATUS_ACTIVE + "' ";
			}
			if (NominalAccForeignTable != null)
			{
				selectStatement += " AND na." + FOREIGN_TABLE + " = '" + NominalAccForeignTable + "' ";
			}
			if (currency != null)
			{
				selectStatement += " AND na." + CURRENCY + " = '" + currency + "' ";
			}
			if (dateFrom != null)
			{
				selectStatement += " AND nat." + NominalAccountTxnBean.TIME_PARAM1 + " >= '"
						+ TimeFormat.strDisplayDate(dateFrom) + "' ";
			}
			if (dateTo != null)
			{
				selectStatement += " AND nat." + NominalAccountTxnBean.TIME_PARAM1 + " < '"
						+ TimeFormat.strDisplayDate(dateTo) + "' ";
			}
			/*
			 * +" na." + FOREIGN_TABLE + " = ? " +" AND na." + CURRENCY + " = ? " +"
			 * AND nat."+ NominalAccountTxnBean.TIME_PARAM1+ " >= ? " +" AND
			 * nat."+ NominalAccountTxnBean.TIME_PARAM1+ " < ? ";
			 */
			selectStatement += " ORDER BY nat." + NominalAccountTxnBean.TIME_PARAM1;
			prepStmt = con.prepareStatement(selectStatement);
			/*
			 * prepStmt.setString(1, NominalAccForeignTable);
			 * prepStmt.setString(2, currency); prepStmt.setTimestamp(3,
			 * dateFrom); prepStmt.setTimestamp(4, dateTo);
			 */
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				NominalAccountObject naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt("na_" + PKID)); // primary
																	// key
				naObj.code = rs.getString("na_" + CODE);
				naObj.namespace = rs.getString("na_" + NAMESPACE);
				naObj.foreignTable = rs.getString("na_" + FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt("na_" + FOREIGN_KEY));
				naObj.accountType = rs.getString("na_" + ACC_TYPE);
				naObj.currency = rs.getString("na_" + CURRENCY);
				naObj.amount = rs.getBigDecimal("na_" + AMOUNT);
				naObj.remarks = rs.getString("na_" + REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt("na_" + ACC_PCCENTER_ID));
				naObj.state = rs.getString("na_" + STATE);
				naObj.status = rs.getString("na_" + STATUS);
				naObj.lastUpdate = rs.getTimestamp("na_" + LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt("na_" + USERID_EDIT));
				naObj.vecNominalAccountTxn = new Vector();
				NominalAccountTxnObject natObj = new NominalAccountTxnObject();
				natObj.pkid = new Long(rs.getLong("nat_" + NominalAccountTxnBean.PKID)); // primary
																							// key
				natObj.nominalAccount = new Integer(rs.getInt("nat_" + NominalAccountTxnBean.NOMINAL_ACCOUNT));
				natObj.foreignTable = rs.getString("nat_" + NominalAccountTxnBean.FOREIGN_TABLE);
				natObj.foreignKey = new Long(rs.getLong("nat_" + NominalAccountTxnBean.FOREIGN_KEY));
				natObj.code = rs.getString("nat_" + NominalAccountTxnBean.CODE);
				natObj.info1 = rs.getString("nat_" + NominalAccountTxnBean.INFO1);
				natObj.description = rs.getString("nat_" + NominalAccountTxnBean.DESCRIPTION);
				natObj.txnType = rs.getString("nat_" + NominalAccountTxnBean.TXN_TYPE);
				natObj.glCodeDebit = rs.getString("nat_" + NominalAccountTxnBean.GLCODE_DEBIT);
				natObj.glCodeCredit = rs.getString("nat_" + NominalAccountTxnBean.GLCODE_CREDIT);
				natObj.currency = rs.getString("nat_" + NominalAccountTxnBean.CURRENCY);
				natObj.amount = rs.getBigDecimal("nat_" + NominalAccountTxnBean.AMOUNT);
				natObj.timeOption1 = rs.getString("nat_" + NominalAccountTxnBean.TIME_OPTION1);
				natObj.timeParam1 = rs.getTimestamp("nat_" + NominalAccountTxnBean.TIME_PARAM1);
				natObj.timeOption2 = rs.getString("nat_" + NominalAccountTxnBean.TIME_OPTION2);
				natObj.timeParam2 = rs.getTimestamp("nat_" + NominalAccountTxnBean.TIME_PARAM2);
				natObj.state = rs.getString("nat_" + NominalAccountTxnBean.STATE);
				natObj.status = rs.getString("nat_" + NominalAccountTxnBean.STATUS);
				natObj.lastUpdate = rs.getTimestamp("nat_" + NominalAccountTxnBean.LASTUPDATE);
				natObj.userIdUpdate = new Integer(rs.getInt("nat_" + NominalAccountTxnBean.USERID_EDIT));
				naObj.vecNominalAccountTxn.add(natObj);
				vecValObj.add(naObj);
				Log.printVerbose(" checkpoint 9 ");
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
			closeConnection(con);
		}
		Log.printVerbose(" checkpoint 10 ");
		return vecValObj;
	}

	// ///////////////////////////////////////////////////////////
	private Vector selectHistoricalARBalance(Integer pcCenter, Timestamp date) throws NamingException, SQLException
	{
		Vector vecRow = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			date = TimeFormat.add(date, 0, 0, 1);
			String selectStatement = "SELECT nadetails.*, cust.name, cust.acc_code, cust.telephone1, "
					+ " cust.credit_limit, cust.credit_terms FROM (SELECT na.pkid AS na_pkid, "
					+ " na.foreign_key AS acc_pkid, na.pc_center_id, nat.balance FROM acc_nominal_account "
					+ " AS na INNER JOIN ( SELECT nominal_account,sum(amount) AS balance FROM acc_nominal_account_txn "
					+ " WHERE time_param1 < '" + TimeFormat.strDisplayDate(date)
					+ "' GROUP BY nominal_account ) AS nat ON (na.pkid = nat.nominal_account) "
					+ " WHERE na.pc_center_id = '" + pcCenter.toString()
					+ "' AND na.foreign_table = 'cust_account_index') AS "
					+ " nadetails INNER JOIN cust_account_index AS cust ON "
					+ " (nadetails.acc_pkid = cust.pkid) ORDER BY cust.name;";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				HistoricalARBalanceSession.Row theRow = new HistoricalARBalanceSession.Row();
				theRow.naPkid = rs.getInt("na_pkid");
				theRow.accPkid = rs.getInt("acc_pkid");
				theRow.pcCenter = rs.getInt("pc_center_id");
				theRow.balance = rs.getBigDecimal("balance");
				theRow.name = rs.getString("name");
				theRow.accCode = rs.getString("acc_code");
				theRow.phone = rs.getString("telephone1");
				theRow.creditLimit = rs.getBigDecimal("credit_limit");
				theRow.creditTerms = rs.getBigDecimal("credit_terms");
				vecRow.add(theRow);
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
			closeConnection(con);
		}
		return vecRow;
	}

	// ///////////////////////////////////////////////////////////
	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
			throws NamingException, SQLException
	{
		Vector vecValObj = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String selectStatement = " select " + PKID + ", " + CODE + ", " + NAMESPACE + ", " + FOREIGN_TABLE + ", "
					+ FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", " + REMARKS + ", "
					+ ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", " + USERID_EDIT
					+ " from " + TABLENAME + " where " + fieldName1 + " = ? ";
			if (fieldName2 != null && value2 != null)
			{
				selectStatement += " AND " + fieldName2 + " = ? ";
			}
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				prepStmt.setString(2, value2);
			}
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				NominalAccountObject naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt(PKID)); // primary key
				naObj.code = rs.getString(CODE);
				naObj.namespace = rs.getString(NAMESPACE);
				naObj.foreignTable = rs.getString(FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
				naObj.accountType = rs.getString(ACC_TYPE);
				naObj.currency = rs.getString(CURRENCY);
				naObj.amount = rs.getBigDecimal(AMOUNT);
				naObj.remarks = rs.getString(REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
				naObj.state = rs.getString(STATE);
				naObj.status = rs.getString(STATUS);
				naObj.lastUpdate = rs.getTimestamp(LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
				vecValObj.add(naObj);
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
			closeConnection(con);
		}
		return vecValObj;
	}

	private Collection selectObjectsGiven(String fieldName, String value) throws NamingException, SQLException
	{
		ArrayList objectSet = new ArrayList();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + value);
			con = makeConnection();
			String selectStatement = " select " + PKID + " from " + TABLENAME + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// closeConnection(con);
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
			closeConnection(con);
		}
		return objectSet;
	}

	private Integer getNextPKId() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + "In getNextPKId()");
			con = makeConnection();
			String findMaxPKIdStmt = " select max(" + PKID + ") as max_pkid from " + TABLENAME + " ";
			prepStmt = con.prepareStatement(findMaxPKIdStmt);
			ResultSet rs = prepStmt.executeQuery();
			int nextId = 0;
			if (rs.next())
			{
				nextId = rs.getInt("max_pkid") + 1;
				Log.printVerbose(strObjectName + "next pkid = " + nextId);
			} else
				throw new EJBException(strObjectName + "Error while retrieving max(pkid)");
			// prepStmt.close();
			// closeConnection(con);
			Log.printVerbose(strObjectName + "Leaving  getNextPKId()");
			return new Integer(nextId);
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
			closeConnection(con);
		}
	}
} // ObjectBean
/*==========================================================
 *
 * Copyright  of Vincent Lee (vlee@vlee.net,
 *      vincent@wavelet.biz). All Rights Reserved.
 * (http://www.wavelet.biz)
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
import com.vlee.ejb.customer.*;
import com.vlee.bean.reports.*;

public class NominalAccountBean implements EntityBean
{
	// Constants for Table field mappings
	public static final String PKID = "pkid";
	public static final String CODE = "code";
	public static final String NAMESPACE = "namespace";
	public static final String FOREIGN_TABLE = "foreign_table";
	public static final String FOREIGN_KEY = "foreign_key";
	public static final String ACC_TYPE = "acc_type";
	public static final String CURRENCY = "currency";
	public static final String AMOUNT = "amount";
	public static final String REMARKS = "remarks";
	public static final String ACC_PCCENTER_ID = "pc_center_id";
	public static final String STATE = "state";
	public static final String STATUS = "status";
	public static final String LASTUPDATE = "lastupdate";
	public static final String USERID_EDIT = "userid_edit";
	// Constants for GLCODE
	public static final String GLCODE_NOMINAL = "nominal";
	// Constants for STATUS
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	// Constants for STATE
	public static final String STATE_CREATED = "created";
	// Constants for NAMESPACE
	public static final String NS_CUSTOMER = "customer";
	public static final String NS_SUPPLIER = "supplier";
	public static final String NS_GENERAL = "general";
	public static final String NS_FINANCE = "finance";
	public static final String NS_SHAREHOLDER = "shareholder";
	public static final String NS_ADMIN = "admin";
	public static final String NS_EMPLOYEE = "employee";
	public static final String NS_MISC = "miscellaneous";
	// Constants for FOREIGN_TABLE
	public static final String FT_CUSTOMER = com.vlee.ejb.customer.CustAccountBean.TABLENAME;
	public static final String FT_SUPPLIER = com.vlee.ejb.supplier.SuppAccountBean.TABLENAME;
	public static final String FT_GENERAL = com.vlee.ejb.accounting.GenericEntityAccountBean.TABLENAME;
	// Constants for ACC_TYPE
	public static final String ACC_TYPE_PAYABLE = "accPayable";
	public static final String ACC_TYPE_RECEIVABLE = "accReceivable";
	public static final String ACC_TYPE_NEUTRAL = "accNeutral";
	// Defaults
	public static final Integer DEF_ACC_PCCENTER_ID = new Integer(0);
	// Attributes of Object
	private Integer pkid; // Primary Key
	private String code;
	private String namespace;
	private String foreignTable;
	private Integer foreignKey;
	private String accountType;
	private String currency;
	private BigDecimal amount;
	private String remarks;
	private Integer accPCCenterId;
	private String state;
	private String status;
	private Timestamp lastUpdate;
	private Integer userIdUpdate;
	// DB Connection attributes
	// private Connection con = null;
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_nominal_account";
	// Other params
	private static final String strObjectName = "NominalAccountBean: ";
	// Constants
	public static final int MAX_LEN_REMARKS = 200;
	// EntityContext
	private EntityContext context = null;

	/***************************************************************************
	 * Getters
	 **************************************************************************/
	public NominalAccountObject getObject()
	{
		NominalAccountObject naObj = new NominalAccountObject();
		naObj.pkid = this.pkid;
		naObj.code = this.code;
		naObj.namespace = this.namespace;
		naObj.foreignTable = this.foreignTable;
		naObj.foreignKey = this.foreignKey;
		naObj.accountType = this.accountType;
		naObj.currency = this.currency;
		naObj.amount = this.amount;
		naObj.remarks = this.remarks;
		naObj.accPCCenterId = this.accPCCenterId;
		naObj.state = this.state;
		naObj.status = this.status;
		naObj.lastUpdate = this.lastUpdate;
		naObj.userIdUpdate = this.userIdUpdate;
		return naObj;
	}

	public Integer getPkid()
	{
		return pkid;
	}

	public String getCode()
	{
		return code;
	}

	public String getNamespace()
	{
		return namespace;
	}

	public String getForeignTable()
	{
		return foreignTable;
	}

	public Integer getForeignKey()
	{
		return foreignKey;
	}

	public String getAccountType()
	{
		return accountType;
	}

	public String getCurrency()
	{
		return currency;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public String getRemarks()
	{
		return remarks;
	}

	public Integer getPCCenterId()
	{
		return accPCCenterId;
	}

	public String getState()
	{
		return state;
	}

	public String getStatus()
	{
		return status;
	}

	public Timestamp getLastUpdate()
	{
		return lastUpdate;
	}

	public Integer getUserIdUpdate()
	{
		return userIdUpdate;
	}

	/***************************************************************************
	 * Setters
	 **************************************************************************/
	public void setPkid(Integer pkid)
	{
		this.pkid = pkid;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public void setNamespace(String namespace)
	{
		this.namespace = namespace;
	}

	public void setForeignTable(String foreignTable)
	{
		this.foreignTable = foreignTable;
	}

	public void setForeignKey(Integer foreignKey)
	{
		this.foreignKey = foreignKey;
	}

	public void setAccountType(String accountType)
	{
		this.accountType = accountType;
	}

	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
	}

	public void addAmount(BigDecimal delta)
	{
		this.amount = this.amount.add(delta);
	}

	public void setRemarks(String remarks)
	{
		this.remarks = remarks;
	}

	public void setPCCenterId(Integer accPCCenterId)
	{
		this.accPCCenterId = accPCCenterId;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public void setLastUpdate(Timestamp lastUpdate)
	{
		this.lastUpdate = lastUpdate;
	}

	public void setUserIdUpdate(Integer userIdUpdate)
	{
		this.userIdUpdate = userIdUpdate;
	}

	/***************************************************************************
	 * ejbCreate (1)
	 **************************************************************************/
	public Integer ejbCreate(String namespace, String code, String foreignTable, Integer foreignKey,
			String accountType, String currency, BigDecimal amount, String remarks, Integer accPCCenterId,
			String state, Timestamp tsCreate, Integer userIdUpdate) throws CreateException
	{
		Integer newKey = null;
		Log.printVerbose(strObjectName + "in ejbCreate");
		try
		{
			newKey = insertObject(namespace, code, foreignTable, foreignKey, accountType, currency, amount, remarks,
					accPCCenterId, state, STATUS_ACTIVE, tsCreate, userIdUpdate);
		} catch (Exception ex)
		{
			throw new EJBException("ejbCreate: " + ex.getMessage());
		}
		if (newKey != null)
		{
			this.pkid = newKey; // primary key
			this.namespace = namespace;
			this.code = code;
			this.foreignTable = foreignTable;
			this.foreignKey = foreignKey;
			this.accountType = accountType;
			this.currency = currency;
			this.amount = amount;
			this.remarks = remarks;
			this.accPCCenterId = accPCCenterId;
			this.state = state;
			this.status = STATUS_ACTIVE;
			this.lastUpdate = tsCreate;
			this.userIdUpdate = userIdUpdate;
		}
		Log.printVerbose(strObjectName + "about to leave ejbCreate");
		return pkid;
	}

	/***************************************************************************
	 * ejbFindByPrimaryKey
	 **************************************************************************/
	public Integer ejbFindByPrimaryKey(Integer primaryKey) throws FinderException
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
	 * ejbFindObjectsGiven
	 **************************************************************************/
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

	/***************************************************************************
	 * ejbHomeGetObject
	 **************************************************************************/
	public NominalAccountObject ejbHomeGetObject(String foreignTable, Integer accPCCenterId, Integer foreignKey,
			String currency)
	{
		NominalAccountObject naObj = null;
		try
		{
			naObj = selectObjectGiven(foreignTable, accPCCenterId, foreignKey, currency);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("ERROR: " + ex.getMessage());
		}
		return naObj;
	}

	public NominalAccountObject ejbHomeGetObject(Integer pkid)
	{
		NominalAccountObject naObj = null;
		try
		{
			naObj = selectObjectGiven(pkid);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("ERROR: " + ex.getMessage());
		}
		return naObj;
	}

	public Vector ejbHomeGetObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(fieldName1, value1, fieldName2, value2);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printDebug("ERROR: " + ex.getMessage());
		}
		return vecValObj;
	}

	public Vector ejbHomeGetValueObjectsGiven(Integer pcCenter, String naForeignTable, // Customer,
																						// Supplier
			Integer naForeignKey, // nullable
			String currency, String natForeignTable, Long natForeignKey, Timestamp dateFrom, Timestamp dateTo,
			String strOption)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(pcCenter, naForeignTable, naForeignKey, currency, natForeignTable,
					natForeignKey, dateFrom, dateTo, strOption);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	public Vector ejbHomeGetValueObjectsGiven(Integer pcCenter, String naForeignTable, // Customer,
																						// Supplier
			Integer naForeignKey, // nullable
			String currency, BigDecimal bdMoreThan, BigDecimal bdLessTan, String status)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsGiven(pcCenter, naForeignTable, // Customer,
																			// Supplier
					naForeignKey, // nullable
					currency, bdMoreThan, bdLessTan, status);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	public Vector ejbHomeGetValueObjectsOrderBy(Integer pcCenter, String naForeignTable, // Customer,
																							// Supplier
			Integer naForeignKey, // nullable
			String currency, BigDecimal bdMoreThan, BigDecimal bdLessTan, boolean negate, String status, String orderBy)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsOrderBy(pcCenter, naForeignTable, // Customer,
																			// Supplier
					naForeignKey, // nullable
					currency, bdMoreThan, bdLessTan, negate, status, orderBy);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecValObj;
	}

	public Vector ejbHomeGetHistoricalARBalance(Integer pcCenter, Timestamp date)
	{
		Vector vecRow = new Vector();
		try
		{
			vecRow = selectHistoricalARBalance(pcCenter, date);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return vecRow;
	}

	/***************************************************************************
	 * ejbHomeGetActiveObj
	 **************************************************************************/
	/*
	 * public Collection ejbHomeGetActiveObj() { try { Collection bufAL =
	 * getActiveObjSQL(); return bufAL; } catch( Exception ex) {
	 * Log.printDebug(strObjectName + "ejbHomeGetActiveObj: " +
	 * ex.getMessage()); return null; } }
	 */
	/***************************************************************************
	 * ejbRemove
	 **************************************************************************/
	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		try
		{
			deleteObject(this.pkid);
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
		this.pkid = (Integer) context.getPrimaryKey();
	}

	/***************************************************************************
	 * ejbPassivate
	 **************************************************************************/
	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.pkid = null;
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
	 * ejbPostCreate (1)
	 **************************************************************************/
	public void ejbPostCreate(String namespace, String code, String foreignTable, Integer foreignKey,
			String accountType, String currency, BigDecimal amount, String remarks, Integer accPCCenterId,
			String state, Timestamp tsCreate, Integer userIdUpdate)
	{
		// nothing
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
				// Log.printVerbose("Closing connection ...");
				con.close();
			}
		} catch (SQLException ex)
		{
			throw new EJBException("closeConnection: " + ex.getMessage());
		}
	}

	public Integer insertObject(String namespace, String code, String foreignTable, Integer foreignKey,
			String accountType, String currency, BigDecimal amount, String remarks, Integer accPCCenterId,
			String state, String status, Timestamp tsCreate, Integer userIdUpdate) throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Integer nextPKId = null;
			Log.printVerbose(strObjectName + " insertObject: ");
			con = makeConnection();
			try
			{
				nextPKId = getNextPKId();
			} catch (Exception ex)
			{
				throw new EJBException(strObjectName + ex.getMessage());
			}
			// con = makeConnection();
			String insertStatement = "insert into " + TABLENAME + "(" + PKID + ", " + NAMESPACE + ", " + CODE + ", "
					+ FOREIGN_TABLE + ", " + FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", "
					+ REMARKS + ", " + ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", "
					+ USERID_EDIT + ") values ( ?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ? ) ";
			prepStmt = con.prepareStatement(insertStatement);
			prepStmt.setInt(1, nextPKId.intValue());
			prepStmt.setString(2, namespace);
			prepStmt.setString(3, code);
			prepStmt.setString(4, foreignTable);
			prepStmt.setInt(5, foreignKey.intValue());
			prepStmt.setString(6, accountType);
			prepStmt.setString(7, currency);
			prepStmt.setBigDecimal(8, amount);
			prepStmt.setString(9, remarks);
			prepStmt.setInt(10, accPCCenterId.intValue());
			prepStmt.setString(11, state);
			prepStmt.setString(12, STATUS_ACTIVE);
			prepStmt.setTimestamp(13, tsCreate);
			prepStmt.setInt(14, userIdUpdate.intValue());
			prepStmt.executeUpdate();
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving insertObject: ");
			return nextPKId;
		} catch (SQLException ex)
		{
			ex.printStackTrace();
			// Rethrow exception
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
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
			closeConnection(con);
		}
	}

	private boolean selectByPrimaryKey(Integer primaryKey) throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectByPrimaryKey: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + " from " + TABLENAME + " where " + PKID + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, primaryKey.intValue());
			ResultSet rs = prepStmt.executeQuery();
			boolean result = rs.next();
			// prepStmt.close();
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectByPrimaryKey:");
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
			closeConnection(con);
		}
	}

	private void deleteObject(Integer pkid) throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " deleteObject: ");
			con = makeConnection();
			String deleteStatement = "delete from " + TABLENAME + " where " + PKID + " = ?";
			prepStmt = con.prepareStatement(deleteStatement);
			prepStmt.setInt(1, pkid.intValue());
			prepStmt.executeUpdate();
			// prepStmt.close();
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving deleteObject: ");
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
			closeConnection(con);
		}
	}

	private void loadObject() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " loadObject: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + ", " + CODE + ", " + NAMESPACE + ", " + FOREIGN_TABLE + ", "
					+ FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", " + REMARKS + ", "
					+ ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", " + USERID_EDIT
					+ " from " + TABLENAME + " where " + PKID + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, this.pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			if (rs.next())
			{
				this.pkid = new Integer(rs.getInt(PKID)); // primary key
				this.code = rs.getString(CODE);
				this.namespace = rs.getString(NAMESPACE);
				this.foreignTable = rs.getString(FOREIGN_TABLE);
				this.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
				this.accountType = rs.getString(ACC_TYPE);
				this.currency = rs.getString(CURRENCY);
				this.amount = rs.getBigDecimal(AMOUNT);
				this.remarks = rs.getString(REMARKS);
				this.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
				this.state = rs.getString(STATE);
				this.status = rs.getString(STATUS);
				this.lastUpdate = rs.getTimestamp(LASTUPDATE);
				this.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
				// prepStmt.close();
			} else
			{
				// prepStmt.close();
				throw new NoSuchEntityException("Row for pkid " + this.pkid.toString() + " not found in database.");
			}
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving loadObject: ");
		} catch (SQLException ex)
		{
			// Rethrow exception
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex)
		{
			ex.printStackTrace();
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
			closeConnection(con);
		}
	}

	// ///////////////////////////////////////////////////////////////////
	private NominalAccountObject selectObjectGiven(Integer pkid) throws NamingException, SQLException
	{
		NominalAccountObject naObj = null;
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjectGiven: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + ", " + CODE + ", " + NAMESPACE + ", " + FOREIGN_TABLE + ", "
					+ FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", " + REMARKS + ", "
					+ ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", " + USERID_EDIT
					+ " from " + TABLENAME + " where " + PKID + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			if (rs.next())
			{
				naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt(PKID)); // primary key
				naObj.code = rs.getString(CODE);
				naObj.namespace = rs.getString(NAMESPACE);
				naObj.foreignTable = rs.getString(FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
				naObj.accountType = rs.getString(ACC_TYPE);
				naObj.currency = rs.getString(CURRENCY);
				naObj.amount = rs.getBigDecimal(AMOUNT);
				naObj.remarks = rs.getString(REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
				naObj.state = rs.getString(STATE);
				naObj.status = rs.getString(STATUS);
				naObj.lastUpdate = rs.getTimestamp(LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
				// prepStmt.close();
			}
			/*
			 * else { //prepStmt.close(); // throw new
			 * NoSuchEntityException("Row for pkid " + // this.pkid.toString() + "
			 * not found in database."); }
			 */
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectObjectGiven: ");
		}
		/*
		 * catch(SQLException ex) { // Rethrow exception ex.printStackTrace();
		 * throw ex; }
		 */catch (Exception ex)
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
		return naObj;
	}

	// ///////////////////////////////////////////////////////////////////
	private NominalAccountObject selectObjectGiven(String foreignTable, Integer accPCCenterId, Integer foreignKey,
			String currency) throws NamingException, SQLException
	{
		Log.printVerbose(" Params = " + foreignTable + " : " + accPCCenterId.toString() + " : " + foreignKey.toString()
				+ " : " + currency);
		NominalAccountObject naObj = null;
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjectGiven: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + ", " + CODE + ", " + NAMESPACE + ", " + FOREIGN_TABLE + ", "
					+ FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", " + REMARKS + ", "
					+ ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", " + USERID_EDIT
					+ " from " + TABLENAME + " where " + FOREIGN_TABLE + " = ? AND " + ACC_PCCENTER_ID + " = ? AND "
					+ CURRENCY + " = ? AND " + FOREIGN_KEY + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, foreignTable);
			prepStmt.setInt(2, accPCCenterId.intValue());
			prepStmt.setString(3, currency);
			prepStmt.setInt(4, foreignKey.intValue());
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			if (rs.next())
			{
				naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt(PKID)); // primary key
				naObj.code = rs.getString(CODE);
				naObj.namespace = rs.getString(NAMESPACE);
				naObj.foreignTable = rs.getString(FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
				naObj.accountType = rs.getString(ACC_TYPE);
				naObj.currency = rs.getString(CURRENCY);
				naObj.amount = rs.getBigDecimal(AMOUNT);
				naObj.remarks = rs.getString(REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
				naObj.state = rs.getString(STATE);
				naObj.status = rs.getString(STATUS);
				naObj.lastUpdate = rs.getTimestamp(LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
				// prepStmt.close();
			}
			/*
			 * else { //prepStmt.close(); // throw new
			 * NoSuchEntityException("Row for pkid " + // this.pkid.toString() + "
			 * not found in database."); }
			 */
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectObjectGiven: ");
		}
		/*
		 * catch(SQLException ex) { // Rethrow exception ex.printStackTrace();
		 * throw ex; }
		 */catch (Exception ex)
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
		return naObj;
	}

	private void storeObject() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " storeObject ");
			con = makeConnection();
			String updateStatement = "update " + TABLENAME + " set " + PKID + " = ?, " + CODE + " = ?, " + NAMESPACE
					+ " = ?, " + FOREIGN_TABLE + " = ?, " + FOREIGN_KEY + " = ?, " + ACC_TYPE + " = ?, " + CURRENCY
					+ " = ?, " + AMOUNT + " = ?, " + REMARKS + " = ?, " + ACC_PCCENTER_ID + " = ?, " + STATE + " = ?, "
					+ STATUS + " = ?, " + LASTUPDATE + " = ?, " + USERID_EDIT + " = ? " + "where " + PKID + " = ?";
			prepStmt = con.prepareStatement(updateStatement);
			prepStmt.setInt(1, this.pkid.intValue());
			prepStmt.setString(2, this.namespace);
			prepStmt.setString(3, this.code);
			prepStmt.setString(4, this.foreignTable);
			prepStmt.setInt(5, this.foreignKey.intValue());
			prepStmt.setString(6, this.accountType);
			prepStmt.setString(7, this.currency);
			prepStmt.setBigDecimal(8, this.amount);
			prepStmt.setString(9, this.remarks);
			prepStmt.setInt(10, this.accPCCenterId.intValue());
			prepStmt.setString(11, this.state);
			prepStmt.setString(12, this.status);
			prepStmt.setTimestamp(13, this.lastUpdate);
			prepStmt.setInt(14, this.userIdUpdate.intValue());
			prepStmt.setInt(15, this.pkid.intValue());
			int rowCount = prepStmt.executeUpdate();
			// prepStmt.close();
			// closeConnection(con);
			if (rowCount == 0)
			{
				throw new EJBException("Storing row for pkid " + pkid + " failed.");
			}
			Log.printVerbose(strObjectName + " Leaving storeObject: ");
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
			closeConnection(con);
		}
	}

	private Collection selectAll() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectAll: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + " from " + TABLENAME;
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			ArrayList pkIdList = new ArrayList();
			while (rs.next())
			{
				pkIdList.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectAll: ");
			return pkIdList;
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
			closeConnection(con);
		}
	}

	// ///////////////////////////////////////////////////////////
	private Vector selectValueObjectsOrderBy(Integer pcCenter, String naForeignTable, // Customer,
																						// Supplier
			Integer naForeignKey, // nullable
			String currency, BigDecimal bdMoreThan, BigDecimal bdLessThan, boolean negate, String status, String orderBy)
			throws NamingException, SQLException
	{
		Vector vecValObj = new Vector();
		NominalAccountObject naObj = null;
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjectGiven: ");
			con = makeConnection();
			String selectStatement = "SELECT " + "na." + PKID + " AS na_" + PKID + " , " + "na." + CODE + " AS na_"
					+ CODE + " , " + "na." + NAMESPACE + " AS na_" + NAMESPACE + " , " + "na." + FOREIGN_TABLE
					+ " AS na_" + FOREIGN_TABLE + " , " + "na." + FOREIGN_KEY + " AS na_" + FOREIGN_KEY + " , " + "na."
					+ ACC_TYPE + " AS na_" + ACC_TYPE + " , " + "na." + CURRENCY + " AS na_" + CURRENCY + " , " + "na."
					+ AMOUNT + " AS na_" + AMOUNT + " , " + "na." + REMARKS + " AS na_" + REMARKS + " , " + "na."
					+ ACC_PCCENTER_ID + " AS na_" + ACC_PCCENTER_ID + " , " + "na." + STATE + " AS na_" + STATE + " , "
					+ "na." + STATUS + " AS na_" + STATUS + " , " + "na." + LASTUPDATE + " AS na_" + LASTUPDATE + " , "
					+ "na." + USERID_EDIT + " AS na_" + USERID_EDIT + ", " + "ca." + PKID + " AS ca_"
					+ CustAccountBean.PKID + " , " + "ca." + CustAccountBean.CUSTCODE + " AS ca_"
					+ CustAccountBean.CUSTCODE + ", " + "ca." + CustAccountBean.NAME + " AS ca_" + CustAccountBean.NAME
					+ "  "
					// + "ca."+ CustAccountBean.DESCRIPTION +" AS ca_"
					// +CustAccountBean.DESCRIPTION + ", "
					// + "ca."+ CustAccountBean.ACCTYPE+ " AS ca_"+
					// CustAccountBean.ACCTYPE + ", "
					// + "ca."+ CustAccountBean.STATUS + " AS ca_"+
					// CustAccountBean.STATUS + ", "
					// + "ca."+ CustAccountBean.LASTUPDATE + " AS
					// ca_"+CustAccountBean.LASTUPDATE+" , "
					// + "ca."+ CustAccountBean.USERID_EDIT+" AS
					// ca_"+CustAccountBean.USERID_EDIT
					+ " FROM " + TABLENAME + " AS na INNER JOIN " + CustAccountBean.TABLENAME + " AS ca ON ( na."
					+ FOREIGN_KEY + "= ca." + CustAccountBean.PKID + " AND na." + FOREIGN_TABLE + "='" + FT_CUSTOMER
					+ "') " + " WHERE " + "na." + PKID + " != '-1' ";
			if (pcCenter != null)
			{
				selectStatement += " AND na." + ACC_PCCENTER_ID + " = '" + pcCenter.toString() + "' ";
			}
			if (naForeignTable != null)
			{
				selectStatement += " AND na." + FOREIGN_TABLE + " = '" + naForeignTable + "' ";
			}
			if (naForeignKey != null)
			{
				selectStatement += " AND na." + FOREIGN_KEY + " = '" + naForeignKey.toString() + "' ";
			}
			if (currency != null)
			{
				selectStatement += " AND na." + CURRENCY + " = '" + currency + "' ";
			}
			if (negate)
			{
				selectStatement += " AND NOT (";
				if (bdMoreThan != null)
				{
					selectStatement += " na." + AMOUNT + " > '" + bdMoreThan.toString() + "' ";
				}
				if (bdLessThan != null)
				{
					selectStatement += (bdMoreThan == null) ? "" : " AND ";
					selectStatement += " na." + AMOUNT + " < '" + bdLessThan.toString() + "' ";
				}
				selectStatement += ")";
			} else
			{
				if (bdMoreThan != null)
				{
					selectStatement += " AND na." + AMOUNT + " > '" + bdMoreThan.toString() + "' ";
				}
				if (bdLessThan != null)
				{
					selectStatement += " AND na." + AMOUNT + " < '" + bdLessThan.toString() + "' ";
				}
			}
			if (status != null)
			{
				selectStatement += " AND na." + STATUS + " = '" + status + "' ";
			}
			if (orderBy != null)
			{
				selectStatement += " ORDER BY ca." + orderBy + " ";
			}
			/*
			 * if(pcCenter!=null || naForeignTable!=null || naForeignKey!=null ||
			 * currency!=null || bdMoreThan!=null || bdLessThan!=null ||
			 * status!=null) { selectStatement = selectStatement + " where "; }
			 */
			Log.printVerbose(selectStatement);
			prepStmt = con.prepareStatement(selectStatement);
			// prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt("na_" + PKID)); // primary
																	// key
				naObj.code = rs.getString("na_" + CODE);
				naObj.namespace = rs.getString("na_" + NAMESPACE);
				naObj.foreignTable = rs.getString("na_" + FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt("na_" + FOREIGN_KEY));
				naObj.accountType = rs.getString("na_" + ACC_TYPE);
				naObj.currency = rs.getString("na_" + CURRENCY);
				naObj.amount = rs.getBigDecimal("na_" + AMOUNT);
				naObj.remarks = rs.getString("na_" + REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt("na_" + ACC_PCCENTER_ID));
				naObj.state = rs.getString("na_" + STATE);
				naObj.status = rs.getString("na_" + STATUS);
				naObj.lastUpdate = rs.getTimestamp("na_" + LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt("na_" + USERID_EDIT));
				vecValObj.add(naObj);
				// prepStmt.close();
			}
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectObjectGiven: ");
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
		return vecValObj;
	}

	// ///////////////////////////////////////////////////////////
	private Vector selectValueObjectsGiven(Integer pcCenter, String naForeignTable, // Customer,
																					// Supplier
			Integer naForeignKey, // nullable
			String currency, BigDecimal bdMoreThan, BigDecimal bdLessThan, String status) throws NamingException,
			SQLException
	{
		Vector vecValObj = new Vector();
		NominalAccountObject naObj = null;
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + " selectObjectGiven: ");
			con = makeConnection();
			String selectStatement = "select " + PKID + ", " + CODE + ", " + NAMESPACE + ", " + FOREIGN_TABLE + ", "
					+ FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", " + REMARKS + ", "
					+ ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", " + USERID_EDIT
					+ " from " + TABLENAME + " where " + PKID + " != '-1' ";
			if (pcCenter != null)
			{
				selectStatement += " AND " + ACC_PCCENTER_ID + " = '" + pcCenter.toString() + "' ";
			}
			if (naForeignTable != null)
			{
				selectStatement += " AND " + FOREIGN_TABLE + " = '" + naForeignTable + "' ";
			}
			if (naForeignKey != null)
			{
				selectStatement += " AND " + FOREIGN_KEY + " = '" + naForeignKey.toString() + "' ";
			}
			if (currency != null)
			{
				selectStatement += " AND " + CURRENCY + " = '" + currency + "' ";
			}
			if (bdMoreThan != null)
			{
				selectStatement += " AND " + AMOUNT + " > '" + bdMoreThan.toString() + "' ";
			}
			if (bdLessThan != null)
			{
				selectStatement += " AND " + AMOUNT + " < '" + bdLessThan.toString() + "' ";
			}
			if (status != null)
			{
				selectStatement += " AND " + STATUS + " = '" + status + "' ";
			}
			/*
			 * if(pcCenter!=null || naForeignTable!=null || naForeignKey!=null ||
			 * currency!=null || bdMoreThan!=null || bdLessThan!=null ||
			 * status!=null) { selectStatement = selectStatement + " where "; }
			 */
			prepStmt = con.prepareStatement(selectStatement);
			// prepStmt.setInt(1, pkid.intValue());
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt(PKID)); // primary key
				naObj.code = rs.getString(CODE);
				naObj.namespace = rs.getString(NAMESPACE);
				naObj.foreignTable = rs.getString(FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
				naObj.accountType = rs.getString(ACC_TYPE);
				naObj.currency = rs.getString(CURRENCY);
				naObj.amount = rs.getBigDecimal(AMOUNT);
				naObj.remarks = rs.getString(REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
				naObj.state = rs.getString(STATE);
				naObj.status = rs.getString(STATUS);
				naObj.lastUpdate = rs.getTimestamp(LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
				vecValObj.add(naObj);
				// prepStmt.close();
			}
			// closeConnection(con);
			Log.printVerbose(strObjectName + " Leaving selectObjectGiven: ");
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
		return vecValObj;
	}

	// ///////////////////////////////////////////////////////////
	private Vector selectValueObjectsGiven(Integer pcCenter, String NominalAccForeignTable,
	// Customer, Supplier
			Integer NominalAccForeignKey, // nullable
			String currency, // nullable
			String natForeignTable, Long natForeignKey, Timestamp dateFrom, Timestamp dateTo, String strOption)
			throws NamingException, SQLException
	{
		Vector vecValObj = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String selectStatement = " select " + "na." + PKID + " AS na_" + PKID + ", " + "na." + CODE + " AS na_"
					+ CODE + ", " + "na." + NAMESPACE + " AS na_" + NAMESPACE + ", " + "na." + FOREIGN_TABLE
					+ " AS na_" + FOREIGN_TABLE + ", " + "na." + FOREIGN_KEY + " AS na_" + FOREIGN_KEY + ", " + "na."
					+ ACC_TYPE + " AS na_" + ACC_TYPE + ", " + "na." + CURRENCY + " AS na_" + CURRENCY + ", " + "na."
					+ AMOUNT + " AS na_" + AMOUNT + ", " + "na." + REMARKS + " AS na_" + REMARKS + ", " + "na."
					+ ACC_PCCENTER_ID + " AS na_" + ACC_PCCENTER_ID + ", " + "na." + STATE + " AS na_" + STATE + ", "
					+ "na." + STATUS + " AS na_" + STATUS + ", " + "na." + LASTUPDATE + " AS na_" + LASTUPDATE + ", "
					+ "na." + USERID_EDIT + " AS na_" + USERID_EDIT + ", " + "nat." + NominalAccountTxnBean.PKID
					+ " AS nat_" + NominalAccountTxnBean.PKID + ", " + "nat." + NominalAccountTxnBean.NOMINAL_ACCOUNT
					+ " AS nat_" + NominalAccountTxnBean.NOMINAL_ACCOUNT + ", " + "nat."
					+ NominalAccountTxnBean.FOREIGN_TABLE + " AS nat_" + NominalAccountTxnBean.FOREIGN_TABLE + ", "
					+ "nat." + NominalAccountTxnBean.FOREIGN_KEY + " AS nat_" + NominalAccountTxnBean.FOREIGN_KEY
					+ ", " + "nat." + NominalAccountTxnBean.CODE + " AS nat_" + NominalAccountTxnBean.CODE + ", "
					+ "nat." + NominalAccountTxnBean.INFO1 + " AS nat_" + NominalAccountTxnBean.INFO1 + ", " + "nat."
					+ NominalAccountTxnBean.DESCRIPTION + " AS nat_" + NominalAccountTxnBean.DESCRIPTION + ", "
					+ "nat." + NominalAccountTxnBean.TXN_TYPE + " AS nat_" + NominalAccountTxnBean.TXN_TYPE + ", "
					+ "nat." + NominalAccountTxnBean.GLCODE_DEBIT + " AS nat_" + NominalAccountTxnBean.GLCODE_DEBIT
					+ ", " + "nat." + NominalAccountTxnBean.GLCODE_CREDIT + " AS nat_"
					+ NominalAccountTxnBean.GLCODE_CREDIT + ", " + "nat." + NominalAccountTxnBean.CURRENCY + " AS nat_"
					+ NominalAccountTxnBean.CURRENCY + ", " + "nat." + NominalAccountTxnBean.AMOUNT + " AS nat_"
					+ NominalAccountTxnBean.AMOUNT + ", " + "nat." + NominalAccountTxnBean.TIME_OPTION1 + " AS nat_"
					+ NominalAccountTxnBean.TIME_OPTION1 + ", " + "nat." + NominalAccountTxnBean.TIME_PARAM1
					+ " AS nat_" + NominalAccountTxnBean.TIME_PARAM1 + ", " + "nat."
					+ NominalAccountTxnBean.TIME_OPTION2 + " AS nat_" + NominalAccountTxnBean.TIME_OPTION2 + ", "
					+ "nat." + NominalAccountTxnBean.TIME_PARAM2 + " AS nat_" + NominalAccountTxnBean.TIME_PARAM2
					+ ", " + "nat." + NominalAccountTxnBean.STATE + " AS nat_" + NominalAccountTxnBean.STATE + ", "
					+ "nat." + NominalAccountTxnBean.STATUS + " AS nat_" + NominalAccountTxnBean.STATUS + ", " + "nat."
					+ NominalAccountTxnBean.LASTUPDATE + " AS nat_" + NominalAccountTxnBean.LASTUPDATE + ", " + "nat."
					+ NominalAccountTxnBean.USERID_EDIT + " AS nat_" + NominalAccountTxnBean.USERID_EDIT + " FROM "
					+ TABLENAME + " na " + " INNER JOIN " + NominalAccountTxnBean.TABLENAME + " nat " + " ON ( na."
					+ PKID + " = nat." + NominalAccountTxnBean.NOMINAL_ACCOUNT + " ) " + " where nat." + STATUS
					+ " != 'xxx' ";
			if (natForeignTable != null)
			{
				selectStatement += " AND nat." + NominalAccountTxnBean.FOREIGN_TABLE + " = '" + natForeignTable + "' ";
			}
			if (pcCenter != null)
			{
				selectStatement += " AND na." + ACC_PCCENTER_ID + " = '" + pcCenter.toString() + "' ";
			}
			if (NominalAccForeignKey != null)
			{
				selectStatement += " AND na." + FOREIGN_KEY + " = '" + NominalAccForeignKey.toString() + "' ";
			}
			if (natForeignKey != null)
			{
				selectStatement += " AND nat." + NominalAccountTxnBean.FOREIGN_KEY + " = '" + natForeignKey.toString()
						+ "' ";
			}
			if (strOption != null && strOption.equals("active"))
			{
				selectStatement += " AND nat." + NominalAccountTxnBean.STATUS + " = '"
						+ NominalAccountTxnBean.STATUS_ACTIVE + "' ";
			}
			if (NominalAccForeignTable != null)
			{
				selectStatement += " AND na." + FOREIGN_TABLE + " = '" + NominalAccForeignTable + "' ";
			}
			if (currency != null)
			{
				selectStatement += " AND na." + CURRENCY + " = '" + currency + "' ";
			}
			if (dateFrom != null)
			{
				selectStatement += " AND nat." + NominalAccountTxnBean.TIME_PARAM1 + " >= '"
						+ TimeFormat.strDisplayDate(dateFrom) + "' ";
			}
			if (dateTo != null)
			{
				selectStatement += " AND nat." + NominalAccountTxnBean.TIME_PARAM1 + " < '"
						+ TimeFormat.strDisplayDate(dateTo) + "' ";
			}
			/*
			 * +" na." + FOREIGN_TABLE + " = ? " +" AND na." + CURRENCY + " = ? " +"
			 * AND nat."+ NominalAccountTxnBean.TIME_PARAM1+ " >= ? " +" AND
			 * nat."+ NominalAccountTxnBean.TIME_PARAM1+ " < ? ";
			 */
			selectStatement += " ORDER BY nat." + NominalAccountTxnBean.TIME_PARAM1;
			prepStmt = con.prepareStatement(selectStatement);
			/*
			 * prepStmt.setString(1, NominalAccForeignTable);
			 * prepStmt.setString(2, currency); prepStmt.setTimestamp(3,
			 * dateFrom); prepStmt.setTimestamp(4, dateTo);
			 */
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				NominalAccountObject naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt("na_" + PKID)); // primary
																	// key
				naObj.code = rs.getString("na_" + CODE);
				naObj.namespace = rs.getString("na_" + NAMESPACE);
				naObj.foreignTable = rs.getString("na_" + FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt("na_" + FOREIGN_KEY));
				naObj.accountType = rs.getString("na_" + ACC_TYPE);
				naObj.currency = rs.getString("na_" + CURRENCY);
				naObj.amount = rs.getBigDecimal("na_" + AMOUNT);
				naObj.remarks = rs.getString("na_" + REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt("na_" + ACC_PCCENTER_ID));
				naObj.state = rs.getString("na_" + STATE);
				naObj.status = rs.getString("na_" + STATUS);
				naObj.lastUpdate = rs.getTimestamp("na_" + LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt("na_" + USERID_EDIT));
				naObj.vecNominalAccountTxn = new Vector();
				NominalAccountTxnObject natObj = new NominalAccountTxnObject();
				natObj.pkid = new Long(rs.getLong("nat_" + NominalAccountTxnBean.PKID)); // primary
																							// key
				natObj.nominalAccount = new Integer(rs.getInt("nat_" + NominalAccountTxnBean.NOMINAL_ACCOUNT));
				natObj.foreignTable = rs.getString("nat_" + NominalAccountTxnBean.FOREIGN_TABLE);
				natObj.foreignKey = new Long(rs.getLong("nat_" + NominalAccountTxnBean.FOREIGN_KEY));
				natObj.code = rs.getString("nat_" + NominalAccountTxnBean.CODE);
				natObj.info1 = rs.getString("nat_" + NominalAccountTxnBean.INFO1);
				natObj.description = rs.getString("nat_" + NominalAccountTxnBean.DESCRIPTION);
				natObj.txnType = rs.getString("nat_" + NominalAccountTxnBean.TXN_TYPE);
				natObj.glCodeDebit = rs.getString("nat_" + NominalAccountTxnBean.GLCODE_DEBIT);
				natObj.glCodeCredit = rs.getString("nat_" + NominalAccountTxnBean.GLCODE_CREDIT);
				natObj.currency = rs.getString("nat_" + NominalAccountTxnBean.CURRENCY);
				natObj.amount = rs.getBigDecimal("nat_" + NominalAccountTxnBean.AMOUNT);
				natObj.timeOption1 = rs.getString("nat_" + NominalAccountTxnBean.TIME_OPTION1);
				natObj.timeParam1 = rs.getTimestamp("nat_" + NominalAccountTxnBean.TIME_PARAM1);
				natObj.timeOption2 = rs.getString("nat_" + NominalAccountTxnBean.TIME_OPTION2);
				natObj.timeParam2 = rs.getTimestamp("nat_" + NominalAccountTxnBean.TIME_PARAM2);
				natObj.state = rs.getString("nat_" + NominalAccountTxnBean.STATE);
				natObj.status = rs.getString("nat_" + NominalAccountTxnBean.STATUS);
				natObj.lastUpdate = rs.getTimestamp("nat_" + NominalAccountTxnBean.LASTUPDATE);
				natObj.userIdUpdate = new Integer(rs.getInt("nat_" + NominalAccountTxnBean.USERID_EDIT));
				naObj.vecNominalAccountTxn.add(natObj);
				vecValObj.add(naObj);
				Log.printVerbose(" checkpoint 9 ");
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
			closeConnection(con);
		}
		Log.printVerbose(" checkpoint 10 ");
		return vecValObj;
	}

	// ///////////////////////////////////////////////////////////
	private Vector selectHistoricalARBalance(Integer pcCenter, Timestamp date) throws NamingException, SQLException
	{
		Vector vecRow = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			date = TimeFormat.add(date, 0, 0, 1);
			String selectStatement = "SELECT nadetails.*, cust.name, cust.acc_code, cust.telephone1, "
					+ " cust.credit_limit, cust.credit_terms FROM (SELECT na.pkid AS na_pkid, "
					+ " na.foreign_key AS acc_pkid, na.pc_center_id, nat.balance FROM acc_nominal_account "
					+ " AS na INNER JOIN ( SELECT nominal_account,sum(amount) AS balance FROM acc_nominal_account_txn "
					+ " WHERE time_param1 < '" + TimeFormat.strDisplayDate(date)
					+ "' GROUP BY nominal_account ) AS nat ON (na.pkid = nat.nominal_account) "
					+ " WHERE na.pc_center_id = '" + pcCenter.toString()
					+ "' AND na.foreign_table = 'cust_account_index') AS "
					+ " nadetails INNER JOIN cust_account_index AS cust ON "
					+ " (nadetails.acc_pkid = cust.pkid) ORDER BY cust.name;";
			prepStmt = con.prepareStatement(selectStatement);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				HistoricalARBalanceSession.Row theRow = new HistoricalARBalanceSession.Row();
				theRow.naPkid = rs.getInt("na_pkid");
				theRow.accPkid = rs.getInt("acc_pkid");
				theRow.pcCenter = rs.getInt("pc_center_id");
				theRow.balance = rs.getBigDecimal("balance");
				theRow.name = rs.getString("name");
				theRow.accCode = rs.getString("acc_code");
				theRow.phone = rs.getString("telephone1");
				theRow.creditLimit = rs.getBigDecimal("credit_limit");
				theRow.creditTerms = rs.getBigDecimal("credit_terms");
				vecRow.add(theRow);
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
			closeConnection(con);
		}
		return vecRow;
	}

	// ///////////////////////////////////////////////////////////
	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
			throws NamingException, SQLException
	{
		Vector vecValObj = new Vector();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			con = makeConnection();
			String selectStatement = " select " + PKID + ", " + CODE + ", " + NAMESPACE + ", " + FOREIGN_TABLE + ", "
					+ FOREIGN_KEY + ", " + ACC_TYPE + ", " + CURRENCY + ", " + AMOUNT + ", " + REMARKS + ", "
					+ ACC_PCCENTER_ID + ", " + STATE + ", " + STATUS + ", " + LASTUPDATE + ", " + USERID_EDIT
					+ " from " + TABLENAME + " where " + fieldName1 + " = ? ";
			if (fieldName2 != null && value2 != null)
			{
				selectStatement += " AND " + fieldName2 + " = ? ";
			}
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				prepStmt.setString(2, value2);
			}
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				NominalAccountObject naObj = new NominalAccountObject();
				naObj.pkid = new Integer(rs.getInt(PKID)); // primary key
				naObj.code = rs.getString(CODE);
				naObj.namespace = rs.getString(NAMESPACE);
				naObj.foreignTable = rs.getString(FOREIGN_TABLE);
				naObj.foreignKey = new Integer(rs.getInt(FOREIGN_KEY));
				naObj.accountType = rs.getString(ACC_TYPE);
				naObj.currency = rs.getString(CURRENCY);
				naObj.amount = rs.getBigDecimal(AMOUNT);
				naObj.remarks = rs.getString(REMARKS);
				naObj.accPCCenterId = new Integer(rs.getInt(ACC_PCCENTER_ID));
				naObj.state = rs.getString(STATE);
				naObj.status = rs.getString(STATUS);
				naObj.lastUpdate = rs.getTimestamp(LASTUPDATE);
				naObj.userIdUpdate = new Integer(rs.getInt(USERID_EDIT));
				vecValObj.add(naObj);
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
			closeConnection(con);
		}
		return vecValObj;
	}

	private Collection selectObjectsGiven(String fieldName, String value) throws NamingException, SQLException
	{
		ArrayList objectSet = new ArrayList();
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(" criteria : " + fieldName + " " + value);
			con = makeConnection();
			String selectStatement = " select " + PKID + " from " + TABLENAME + "  where " + fieldName + " = ? ";
			prepStmt = con.prepareStatement(selectStatement);
			prepStmt.setString(1, value);
			// prepStmt.setString(2, value);
			ResultSet rs = prepStmt.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt(1)));
			}
			// prepStmt.close();
			// closeConnection(con);
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
			closeConnection(con);
		}
		return objectSet;
	}

	private Integer getNextPKId() throws NamingException, SQLException
	{
		Connection con = null;
		PreparedStatement prepStmt = null;
		try
		{
			Log.printVerbose(strObjectName + "In getNextPKId()");
			con = makeConnection();
			String findMaxPKIdStmt = " select max(" + PKID + ") as max_pkid from " + TABLENAME + " ";
			prepStmt = con.prepareStatement(findMaxPKIdStmt);
			ResultSet rs = prepStmt.executeQuery();
			int nextId = 0;
			if (rs.next())
			{
				nextId = rs.getInt("max_pkid") + 1;
				Log.printVerbose(strObjectName + "next pkid = " + nextId);
			} else
				throw new EJBException(strObjectName + "Error while retrieving max(pkid)");
			// prepStmt.close();
			// closeConnection(con);
			Log.printVerbose(strObjectName + "Leaving  getNextPKId()");
			return new Integer(nextId);
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
			closeConnection(con);
		}
	}
} // ObjectBean
