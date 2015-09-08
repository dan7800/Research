/// to be DEPRECATED!!! DO NOT USE!!
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
import javax.ejb.*;
import javax.naming.*;
import java.math.BigDecimal;
import com.vlee.local.*;
import com.vlee.util.*;

public class GeneralLedgerOpeningBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String TABLENAME = "acc_general_ledger_opening";
	protected final String strObjectName = "GeneralLedgerOpeningBean: ";
	private EntityContext mContext;
	public static final String STATUS = "status";
	public static final String GLID = "glid";
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	private Integer pkId;
	private Integer glId;
	private Integer fiscalYearId;
	private BigDecimal balance;
	private Timestamp openTime;
	private String status;
	private Integer userIdEdit;
	private Timestamp lastUpdate;

	public Integer getPkId()
	{
		return this.pkId;
	}

	public Integer getGlId()
	{
		return this.glId;
	}

	public Integer getFiscalYearId()
	{
		return this.fiscalYearId;
	}

	public BigDecimal getBalance()
	{
		return this.balance;
	}

	public Timestamp getOpenTime()
	{
		return this.openTime;
	}

	public String getStatus()
	{
		return this.status;
	}

	public Integer getUserIdEdit()
	{
		return this.userIdEdit;
	}

	public Timestamp getLastUpdate()
	{
		return this.lastUpdate;
	}

	public void setPkId(Integer pkid)
	{
		this.pkId = pkid;
	}

	public void setGlId(Integer glid)
	{
		this.glId = glid;
	}

	public void setFiscalYearId(Integer fiscalYearId)
	{
		this.fiscalYearId = fiscalYearId;
	}

	public void setBalance(BigDecimal balance)
	{
		this.balance = balance;
	}

	public void setOpenTime(Timestamp opentime)
	{
		this.openTime = opentime;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public void setUserIdEdit(Integer useridedit)
	{
		this.userIdEdit = useridedit;
	}

	public void setLastUpdate(Timestamp lastupdate)
	{
		this.lastUpdate = lastupdate;
	}

	public GeneralLedgerOpeningObject getValueObject()
	{
		GeneralLedgerOpeningObject gloo = new GeneralLedgerOpeningObject();
		gloo.pkId = this.pkId;
		gloo.glId = this.glId;
		gloo.fiscalYearId = this.fiscalYearId;
		gloo.balance = this.balance;
		gloo.openTime = this.openTime;
		gloo.status = this.status;
		gloo.userIdEdit = this.userIdEdit;
		gloo.lastUpdate = this.lastUpdate;
		return gloo;
	}

	public void setValueObject(GeneralLedgerOpeningObject gloo) throws Exception
	{
		if (gloo == null)
		{
			throw new Exception("Object undefined");
		}
		this.glId = gloo.glId;
		this.fiscalYearId = gloo.fiscalYearId;
		this.balance = gloo.balance;
		this.openTime = gloo.openTime;
		this.status = gloo.status;
		this.userIdEdit = gloo.userIdEdit;
		this.lastUpdate = gloo.lastUpdate;
	}

	public void setEntityContext(EntityContext mContext)
	{
		Log.printVerbose(strObjectName + " In setEntityContext");
		this.mContext = mContext;
		Log.printVerbose(strObjectName + " Leaving setEntityContext");
	}

	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.mContext = null;
		Log.printVerbose(strObjectName + " Leaving unsetEntityContext");
	}

	public Integer ejbCreate(Integer glId, Integer fiscalYearId, BigDecimal balance, Timestamp openTime, String status,
			Integer userIdEdit, Timestamp lastUpdate) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(glId, fiscalYearId, balance, openTime, status, userIdEdit, lastUpdate);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.glId = glId;
			this.fiscalYearId = fiscalYearId;
			this.balance = balance;
			this.openTime = openTime;
			this.status = status;
			this.userIdEdit = userIdEdit;
			this.lastUpdate = lastUpdate;
		}
		Log.printVerbose(strObjectName + " Leaving ejbCreate");
		return newPkId;
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		deleteObject(this.pkId);
		Log.printVerbose(strObjectName + " Leaving ejbRemove");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.pkId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.pkId = null;
		Log.printVerbose(strObjectName + " Leaving ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		loadObject();
		Log.printVerbose(strObjectName + " Leaving ejbLoad");
	}

	public void ejbStore()
	{
		Log.printVerbose(strObjectName + " In ejbStore");
		storeObject();
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(Integer glId, Integer fiscalYearId, BigDecimal balance, Timestamp openTime,
			String status, Integer userIdEdit, Timestamp lastUpdate)
	{
	}

	public Integer ejbFindByPrimaryKey(Integer pkid) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByPrimaryKey");
		boolean result;
		result = selectByPrimaryKey(pkid);
		Log.printVerbose(strObjectName + " Leaving ejbFindByPrimaryKey");
		if (result)
		{
			return pkid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + pkid.toString() + " not found.");
		}
	}

	public Collection ejbFindAllObjects() throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindAllObjects");
		Collection col = selectAllObjects();
		Log.printVerbose(strObjectName + " Leaving ejbFindAllObjects");
		return col;
	}

	public Collection ejbFindObjectsGiven(String fieldName, String value) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindObjectsGiven");
		Collection col = selectObjectsGiven(fieldName, value);
		Log.printVerbose(strObjectName + " Leaving ejbFindObjectsGiven");
		return col;
	}

	public Vector ejbHomeGetValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsGiven");
		Vector vecValObj = selectValueObjectsGiven(fieldName1, value1, fieldName2, value2);
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetValueObjectsGiven");
		return vecValObj;
	}

	/** ***************** Database Routines ************************ */
	private DataSource getDataSource()
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			return ds;
		} catch (NamingException ne)
		{
			throw new EJBException("Naming lookup failure: " + ne.getMessage());
		}
	}

	private void cleanup(Connection cn, PreparedStatement ps)
	{
		try
		{
			if (ps != null)
				ps.close();
		} catch (Exception e)
		{
			throw new EJBException(e);
		}
		try
		{
			if (cn != null)
				cn.close();
		} catch (Exception e)
		{
			throw new EJBException(e);
		}
	}

	private Integer insertNewRow(Integer glId, Integer fiscalYearId, BigDecimal balance, Timestamp openTime,
			String status, Integer userIdEdit, Timestamp lastUpdate)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO "
					+ TABLENAME
					+ " (pkid, glid, fiscalyearid, balance, opentime, status, userid_edit, lastupdate) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setInt(2, glId.intValue());
			ps.setInt(3, fiscalYearId.intValue());
			ps.setBigDecimal(4, balance);
			ps.setTimestamp(5, openTime);
			ps.setString(6, status);
			ps.setInt(7, userIdEdit.intValue());
			ps.setTimestamp(8, lastUpdate);
			ps.executeUpdate();
			Log.printAudit(strObjectName + " Created New Row:" + newPkId.toString());
			return newPkId;
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private Integer getNextPKId()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT MAX(pkid) as max_pkid FROM " + TABLENAME;
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			rs.next();
			int max = rs.getInt("max_pkid");
			if (max == 0)
			{
				max = 1000;
			} else
			{
				max += 1;
			}
			return new Integer(max);
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private void deleteObject(Integer pkid)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "DELETE FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, pkid.intValue());
			ps.executeUpdate();
			Log.printAudit(strObjectName + " Deleted Object: " + pkid.toString());
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private void loadObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT pkid, glid, fiscalyearid, balance, opentime, status, userid_edit, lastupdate FROM "
					+ TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.glId = new Integer(rs.getInt("glid"));
				this.fiscalYearId = new Integer(rs.getInt("fiscalyearid"));
				this.balance = rs.getBigDecimal("balance");
				this.openTime = rs.getTimestamp("opentime");
				this.status = rs.getString("status");
				this.userIdEdit = new Integer(rs.getInt("userid_edit"));
				this.lastUpdate = rs.getTimestamp("lastupdate");
			} else
			{
				throw new NoSuchEntityException("Row for this EJB Object is not found in database.");
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private void storeObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "UPDATE "
					+ TABLENAME
					+ " SET glid = ?, fiscalyearid = ?, balance = ?, opentime = ?, status = ?, userid_edit = ?, lastupdate = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.glId.intValue());
			ps.setInt(2, this.fiscalYearId.intValue());
			ps.setBigDecimal(3, this.balance);
			ps.setTimestamp(4, this.openTime);
			ps.setString(5, this.status);
			ps.setInt(6, this.userIdEdit.intValue());
			ps.setTimestamp(7, this.lastUpdate);
			ps.setInt(8, this.pkId.intValue());
			int rowCount = ps.executeUpdate();
			if (rowCount == 0)
			{
				throw new EJBException("Storing ejb object " + this.pkId.toString() + " failed.");
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private boolean selectByPrimaryKey(Integer pkid)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, pkid.intValue());
			ResultSet rs = ps.executeQuery();
			boolean result = rs.next();
			return result;
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private Collection selectAllObjects()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " ORDER BY pkid";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				//rs.beforeTheFirstRecord();
				while (rs.next())
				{
					objectSet.add(new Integer(rs.getInt(1)));
				}
				return objectSet;
			} else
			{
				return null;
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private Collection selectObjectsGiven(String fieldName, String value)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " WHERE " + fieldName + " = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				//rs.beforeTheFirstRecord();
				while (rs.next())
				{
					objectSet.add(new Integer(rs.getInt(1)));
				}
				return objectSet;
			} else
			{
				return null;
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT pkid, glid, fiscalyearid, balance, opentime, status, userid_edit, lastupdate FROM "
					+ TABLENAME + " WHERE " + fieldName1 + " = ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ?";
			}
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(2, value2);
			}
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GeneralLedgerOpeningObject gloo = new GeneralLedgerOpeningObject();
				gloo.pkId = new Integer(rs.getInt("pkid"));
				gloo.glId = new Integer(rs.getInt("glid"));
				gloo.fiscalYearId = new Integer(rs.getInt("fiscalyearid"));
				gloo.balance = rs.getBigDecimal("balance");
				gloo.openTime = rs.getTimestamp("opentime");
				gloo.status = rs.getString("status");
				gloo.userIdEdit = new Integer(rs.getInt("userid_edit"));
				gloo.lastUpdate = rs.getTimestamp("lastupdate");
				vecValObj.add(gloo);
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return vecValObj;
	}
}
/// to be DEPRECATED!!! DO NOT USE!!
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
import javax.ejb.*;
import javax.naming.*;
import java.math.BigDecimal;
import com.vlee.local.*;
import com.vlee.util.*;

public class GeneralLedgerOpeningBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String TABLENAME = "acc_general_ledger_opening";
	protected final String strObjectName = "GeneralLedgerOpeningBean: ";
	private EntityContext mContext;
	public static final String STATUS = "status";
	public static final String GLID = "glid";
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	private Integer pkId;
	private Integer glId;
	private Integer fiscalYearId;
	private BigDecimal balance;
	private Timestamp openTime;
	private String status;
	private Integer userIdEdit;
	private Timestamp lastUpdate;

	public Integer getPkId()
	{
		return this.pkId;
	}

	public Integer getGlId()
	{
		return this.glId;
	}

	public Integer getFiscalYearId()
	{
		return this.fiscalYearId;
	}

	public BigDecimal getBalance()
	{
		return this.balance;
	}

	public Timestamp getOpenTime()
	{
		return this.openTime;
	}

	public String getStatus()
	{
		return this.status;
	}

	public Integer getUserIdEdit()
	{
		return this.userIdEdit;
	}

	public Timestamp getLastUpdate()
	{
		return this.lastUpdate;
	}

	public void setPkId(Integer pkid)
	{
		this.pkId = pkid;
	}

	public void setGlId(Integer glid)
	{
		this.glId = glid;
	}

	public void setFiscalYearId(Integer fiscalYearId)
	{
		this.fiscalYearId = fiscalYearId;
	}

	public void setBalance(BigDecimal balance)
	{
		this.balance = balance;
	}

	public void setOpenTime(Timestamp opentime)
	{
		this.openTime = opentime;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public void setUserIdEdit(Integer useridedit)
	{
		this.userIdEdit = useridedit;
	}

	public void setLastUpdate(Timestamp lastupdate)
	{
		this.lastUpdate = lastupdate;
	}

	public GeneralLedgerOpeningObject getValueObject()
	{
		GeneralLedgerOpeningObject gloo = new GeneralLedgerOpeningObject();
		gloo.pkId = this.pkId;
		gloo.glId = this.glId;
		gloo.fiscalYearId = this.fiscalYearId;
		gloo.balance = this.balance;
		gloo.openTime = this.openTime;
		gloo.status = this.status;
		gloo.userIdEdit = this.userIdEdit;
		gloo.lastUpdate = this.lastUpdate;
		return gloo;
	}

	public void setValueObject(GeneralLedgerOpeningObject gloo) throws Exception
	{
		if (gloo == null)
		{
			throw new Exception("Object undefined");
		}
		this.glId = gloo.glId;
		this.fiscalYearId = gloo.fiscalYearId;
		this.balance = gloo.balance;
		this.openTime = gloo.openTime;
		this.status = gloo.status;
		this.userIdEdit = gloo.userIdEdit;
		this.lastUpdate = gloo.lastUpdate;
	}

	public void setEntityContext(EntityContext mContext)
	{
		Log.printVerbose(strObjectName + " In setEntityContext");
		this.mContext = mContext;
		Log.printVerbose(strObjectName + " Leaving setEntityContext");
	}

	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.mContext = null;
		Log.printVerbose(strObjectName + " Leaving unsetEntityContext");
	}

	public Integer ejbCreate(Integer glId, Integer fiscalYearId, BigDecimal balance, Timestamp openTime, String status,
			Integer userIdEdit, Timestamp lastUpdate) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(glId, fiscalYearId, balance, openTime, status, userIdEdit, lastUpdate);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.glId = glId;
			this.fiscalYearId = fiscalYearId;
			this.balance = balance;
			this.openTime = openTime;
			this.status = status;
			this.userIdEdit = userIdEdit;
			this.lastUpdate = lastUpdate;
		}
		Log.printVerbose(strObjectName + " Leaving ejbCreate");
		return newPkId;
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		deleteObject(this.pkId);
		Log.printVerbose(strObjectName + " Leaving ejbRemove");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.pkId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.pkId = null;
		Log.printVerbose(strObjectName + " Leaving ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		loadObject();
		Log.printVerbose(strObjectName + " Leaving ejbLoad");
	}

	public void ejbStore()
	{
		Log.printVerbose(strObjectName + " In ejbStore");
		storeObject();
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(Integer glId, Integer fiscalYearId, BigDecimal balance, Timestamp openTime,
			String status, Integer userIdEdit, Timestamp lastUpdate)
	{
	}

	public Integer ejbFindByPrimaryKey(Integer pkid) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByPrimaryKey");
		boolean result;
		result = selectByPrimaryKey(pkid);
		Log.printVerbose(strObjectName + " Leaving ejbFindByPrimaryKey");
		if (result)
		{
			return pkid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + pkid.toString() + " not found.");
		}
	}

	public Collection ejbFindAllObjects() throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindAllObjects");
		Collection col = selectAllObjects();
		Log.printVerbose(strObjectName + " Leaving ejbFindAllObjects");
		return col;
	}

	public Collection ejbFindObjectsGiven(String fieldName, String value) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindObjectsGiven");
		Collection col = selectObjectsGiven(fieldName, value);
		Log.printVerbose(strObjectName + " Leaving ejbFindObjectsGiven");
		return col;
	}

	public Vector ejbHomeGetValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsGiven");
		Vector vecValObj = selectValueObjectsGiven(fieldName1, value1, fieldName2, value2);
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetValueObjectsGiven");
		return vecValObj;
	}

	/** ***************** Database Routines ************************ */
	private DataSource getDataSource()
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			return ds;
		} catch (NamingException ne)
		{
			throw new EJBException("Naming lookup failure: " + ne.getMessage());
		}
	}

	private void cleanup(Connection cn, PreparedStatement ps)
	{
		try
		{
			if (ps != null)
				ps.close();
		} catch (Exception e)
		{
			throw new EJBException(e);
		}
		try
		{
			if (cn != null)
				cn.close();
		} catch (Exception e)
		{
			throw new EJBException(e);
		}
	}

	private Integer insertNewRow(Integer glId, Integer fiscalYearId, BigDecimal balance, Timestamp openTime,
			String status, Integer userIdEdit, Timestamp lastUpdate)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO "
					+ TABLENAME
					+ " (pkid, glid, fiscalyearid, balance, opentime, status, userid_edit, lastupdate) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setInt(2, glId.intValue());
			ps.setInt(3, fiscalYearId.intValue());
			ps.setBigDecimal(4, balance);
			ps.setTimestamp(5, openTime);
			ps.setString(6, status);
			ps.setInt(7, userIdEdit.intValue());
			ps.setTimestamp(8, lastUpdate);
			ps.executeUpdate();
			Log.printAudit(strObjectName + " Created New Row:" + newPkId.toString());
			return newPkId;
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private Integer getNextPKId()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT MAX(pkid) as max_pkid FROM " + TABLENAME;
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			rs.next();
			int max = rs.getInt("max_pkid");
			if (max == 0)
			{
				max = 1000;
			} else
			{
				max += 1;
			}
			return new Integer(max);
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private void deleteObject(Integer pkid)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "DELETE FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, pkid.intValue());
			ps.executeUpdate();
			Log.printAudit(strObjectName + " Deleted Object: " + pkid.toString());
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private void loadObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT pkid, glid, fiscalyearid, balance, opentime, status, userid_edit, lastupdate FROM "
					+ TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.glId = new Integer(rs.getInt("glid"));
				this.fiscalYearId = new Integer(rs.getInt("fiscalyearid"));
				this.balance = rs.getBigDecimal("balance");
				this.openTime = rs.getTimestamp("opentime");
				this.status = rs.getString("status");
				this.userIdEdit = new Integer(rs.getInt("userid_edit"));
				this.lastUpdate = rs.getTimestamp("lastupdate");
			} else
			{
				throw new NoSuchEntityException("Row for this EJB Object is not found in database.");
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private void storeObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "UPDATE "
					+ TABLENAME
					+ " SET glid = ?, fiscalyearid = ?, balance = ?, opentime = ?, status = ?, userid_edit = ?, lastupdate = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.glId.intValue());
			ps.setInt(2, this.fiscalYearId.intValue());
			ps.setBigDecimal(3, this.balance);
			ps.setTimestamp(4, this.openTime);
			ps.setString(5, this.status);
			ps.setInt(6, this.userIdEdit.intValue());
			ps.setTimestamp(7, this.lastUpdate);
			ps.setInt(8, this.pkId.intValue());
			int rowCount = ps.executeUpdate();
			if (rowCount == 0)
			{
				throw new EJBException("Storing ejb object " + this.pkId.toString() + " failed.");
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private boolean selectByPrimaryKey(Integer pkid)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, pkid.intValue());
			ResultSet rs = ps.executeQuery();
			boolean result = rs.next();
			return result;
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private Collection selectAllObjects()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " ORDER BY pkid";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				rs.beforeFirst();
				while (rs.next())
				{
					objectSet.add(new Integer(rs.getInt(1)));
				}
				return objectSet;
			} else
			{
				return null;
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private Collection selectObjectsGiven(String fieldName, String value)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " WHERE " + fieldName + " = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				rs.beforeFirst();
				while (rs.next())
				{
					objectSet.add(new Integer(rs.getInt(1)));
				}
				return objectSet;
			} else
			{
				return null;
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT pkid, glid, fiscalyearid, balance, opentime, status, userid_edit, lastupdate FROM "
					+ TABLENAME + " WHERE " + fieldName1 + " = ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ?";
			}
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(2, value2);
			}
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GeneralLedgerOpeningObject gloo = new GeneralLedgerOpeningObject();
				gloo.pkId = new Integer(rs.getInt("pkid"));
				gloo.glId = new Integer(rs.getInt("glid"));
				gloo.fiscalYearId = new Integer(rs.getInt("fiscalyearid"));
				gloo.balance = rs.getBigDecimal("balance");
				gloo.openTime = rs.getTimestamp("opentime");
				gloo.status = rs.getString("status");
				gloo.userIdEdit = new Integer(rs.getInt("userid_edit"));
				gloo.lastUpdate = rs.getTimestamp("lastupdate");
				vecValObj.add(gloo);
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return vecValObj;
	}
}
/// to be DEPRECATED!!! DO NOT USE!!
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
import javax.ejb.*;
import javax.naming.*;
import java.math.BigDecimal;
import com.vlee.local.*;
import com.vlee.util.*;

public class GeneralLedgerOpeningBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String TABLENAME = "acc_general_ledger_opening";
	protected final String strObjectName = "GeneralLedgerOpeningBean: ";
	private EntityContext mContext;
	public static final String STATUS = "status";
	public static final String GLID = "glid";
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	private Integer pkId;
	private Integer glId;
	private Integer fiscalYearId;
	private BigDecimal balance;
	private Timestamp openTime;
	private String status;
	private Integer userIdEdit;
	private Timestamp lastUpdate;

	public Integer getPkId()
	{
		return this.pkId;
	}

	public Integer getGlId()
	{
		return this.glId;
	}

	public Integer getFiscalYearId()
	{
		return this.fiscalYearId;
	}

	public BigDecimal getBalance()
	{
		return this.balance;
	}

	public Timestamp getOpenTime()
	{
		return this.openTime;
	}

	public String getStatus()
	{
		return this.status;
	}

	public Integer getUserIdEdit()
	{
		return this.userIdEdit;
	}

	public Timestamp getLastUpdate()
	{
		return this.lastUpdate;
	}

	public void setPkId(Integer pkid)
	{
		this.pkId = pkid;
	}

	public void setGlId(Integer glid)
	{
		this.glId = glid;
	}

	public void setFiscalYearId(Integer fiscalYearId)
	{
		this.fiscalYearId = fiscalYearId;
	}

	public void setBalance(BigDecimal balance)
	{
		this.balance = balance;
	}

	public void setOpenTime(Timestamp opentime)
	{
		this.openTime = opentime;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public void setUserIdEdit(Integer useridedit)
	{
		this.userIdEdit = useridedit;
	}

	public void setLastUpdate(Timestamp lastupdate)
	{
		this.lastUpdate = lastupdate;
	}

	public GeneralLedgerOpeningObject getValueObject()
	{
		GeneralLedgerOpeningObject gloo = new GeneralLedgerOpeningObject();
		gloo.pkId = this.pkId;
		gloo.glId = this.glId;
		gloo.fiscalYearId = this.fiscalYearId;
		gloo.balance = this.balance;
		gloo.openTime = this.openTime;
		gloo.status = this.status;
		gloo.userIdEdit = this.userIdEdit;
		gloo.lastUpdate = this.lastUpdate;
		return gloo;
	}

	public void setValueObject(GeneralLedgerOpeningObject gloo) throws Exception
	{
		if (gloo == null)
		{
			throw new Exception("Object undefined");
		}
		this.glId = gloo.glId;
		this.fiscalYearId = gloo.fiscalYearId;
		this.balance = gloo.balance;
		this.openTime = gloo.openTime;
		this.status = gloo.status;
		this.userIdEdit = gloo.userIdEdit;
		this.lastUpdate = gloo.lastUpdate;
	}

	public void setEntityContext(EntityContext mContext)
	{
		Log.printVerbose(strObjectName + " In setEntityContext");
		this.mContext = mContext;
		Log.printVerbose(strObjectName + " Leaving setEntityContext");
	}

	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.mContext = null;
		Log.printVerbose(strObjectName + " Leaving unsetEntityContext");
	}

	public Integer ejbCreate(Integer glId, Integer fiscalYearId, BigDecimal balance, Timestamp openTime, String status,
			Integer userIdEdit, Timestamp lastUpdate) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(glId, fiscalYearId, balance, openTime, status, userIdEdit, lastUpdate);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.glId = glId;
			this.fiscalYearId = fiscalYearId;
			this.balance = balance;
			this.openTime = openTime;
			this.status = status;
			this.userIdEdit = userIdEdit;
			this.lastUpdate = lastUpdate;
		}
		Log.printVerbose(strObjectName + " Leaving ejbCreate");
		return newPkId;
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		deleteObject(this.pkId);
		Log.printVerbose(strObjectName + " Leaving ejbRemove");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.pkId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.pkId = null;
		Log.printVerbose(strObjectName + " Leaving ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		loadObject();
		Log.printVerbose(strObjectName + " Leaving ejbLoad");
	}

	public void ejbStore()
	{
		Log.printVerbose(strObjectName + " In ejbStore");
		storeObject();
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(Integer glId, Integer fiscalYearId, BigDecimal balance, Timestamp openTime,
			String status, Integer userIdEdit, Timestamp lastUpdate)
	{
	}

	public Integer ejbFindByPrimaryKey(Integer pkid) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByPrimaryKey");
		boolean result;
		result = selectByPrimaryKey(pkid);
		Log.printVerbose(strObjectName + " Leaving ejbFindByPrimaryKey");
		if (result)
		{
			return pkid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + pkid.toString() + " not found.");
		}
	}

	public Collection ejbFindAllObjects() throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindAllObjects");
		Collection col = selectAllObjects();
		Log.printVerbose(strObjectName + " Leaving ejbFindAllObjects");
		return col;
	}

	public Collection ejbFindObjectsGiven(String fieldName, String value) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindObjectsGiven");
		Collection col = selectObjectsGiven(fieldName, value);
		Log.printVerbose(strObjectName + " Leaving ejbFindObjectsGiven");
		return col;
	}

	public Vector ejbHomeGetValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsGiven");
		Vector vecValObj = selectValueObjectsGiven(fieldName1, value1, fieldName2, value2);
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetValueObjectsGiven");
		return vecValObj;
	}

	/** ***************** Database Routines ************************ */
	private DataSource getDataSource()
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			return ds;
		} catch (NamingException ne)
		{
			throw new EJBException("Naming lookup failure: " + ne.getMessage());
		}
	}

	private void cleanup(Connection cn, PreparedStatement ps)
	{
		try
		{
			if (ps != null)
				ps.close();
		} catch (Exception e)
		{
			throw new EJBException(e);
		}
		try
		{
			if (cn != null)
				cn.close();
		} catch (Exception e)
		{
			throw new EJBException(e);
		}
	}

	private Integer insertNewRow(Integer glId, Integer fiscalYearId, BigDecimal balance, Timestamp openTime,
			String status, Integer userIdEdit, Timestamp lastUpdate)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO "
					+ TABLENAME
					+ " (pkid, glid, fiscalyearid, balance, opentime, status, userid_edit, lastupdate) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setInt(2, glId.intValue());
			ps.setInt(3, fiscalYearId.intValue());
			ps.setBigDecimal(4, balance);
			ps.setTimestamp(5, openTime);
			ps.setString(6, status);
			ps.setInt(7, userIdEdit.intValue());
			ps.setTimestamp(8, lastUpdate);
			ps.executeUpdate();
			Log.printAudit(strObjectName + " Created New Row:" + newPkId.toString());
			return newPkId;
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private Integer getNextPKId()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT MAX(pkid) as max_pkid FROM " + TABLENAME;
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			rs.next();
			int max = rs.getInt("max_pkid");
			if (max == 0)
			{
				max = 1000;
			} else
			{
				max += 1;
			}
			return new Integer(max);
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private void deleteObject(Integer pkid)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "DELETE FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, pkid.intValue());
			ps.executeUpdate();
			Log.printAudit(strObjectName + " Deleted Object: " + pkid.toString());
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private void loadObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT pkid, glid, fiscalyearid, balance, opentime, status, userid_edit, lastupdate FROM "
					+ TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.glId = new Integer(rs.getInt("glid"));
				this.fiscalYearId = new Integer(rs.getInt("fiscalyearid"));
				this.balance = rs.getBigDecimal("balance");
				this.openTime = rs.getTimestamp("opentime");
				this.status = rs.getString("status");
				this.userIdEdit = new Integer(rs.getInt("userid_edit"));
				this.lastUpdate = rs.getTimestamp("lastupdate");
			} else
			{
				throw new NoSuchEntityException("Row for this EJB Object is not found in database.");
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private void storeObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "UPDATE "
					+ TABLENAME
					+ " SET glid = ?, fiscalyearid = ?, balance = ?, opentime = ?, status = ?, userid_edit = ?, lastupdate = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.glId.intValue());
			ps.setInt(2, this.fiscalYearId.intValue());
			ps.setBigDecimal(3, this.balance);
			ps.setTimestamp(4, this.openTime);
			ps.setString(5, this.status);
			ps.setInt(6, this.userIdEdit.intValue());
			ps.setTimestamp(7, this.lastUpdate);
			ps.setInt(8, this.pkId.intValue());
			int rowCount = ps.executeUpdate();
			if (rowCount == 0)
			{
				throw new EJBException("Storing ejb object " + this.pkId.toString() + " failed.");
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private boolean selectByPrimaryKey(Integer pkid)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, pkid.intValue());
			ResultSet rs = ps.executeQuery();
			boolean result = rs.next();
			return result;
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private Collection selectAllObjects()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " ORDER BY pkid";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				//rs.beforeTheFirstRecord();
				while (rs.next())
				{
					objectSet.add(new Integer(rs.getInt(1)));
				}
				return objectSet;
			} else
			{
				return null;
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private Collection selectObjectsGiven(String fieldName, String value)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " WHERE " + fieldName + " = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				//rs.beforeTheFirstRecord();
				while (rs.next())
				{
					objectSet.add(new Integer(rs.getInt(1)));
				}
				return objectSet;
			} else
			{
				return null;
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT pkid, glid, fiscalyearid, balance, opentime, status, userid_edit, lastupdate FROM "
					+ TABLENAME + " WHERE " + fieldName1 + " = ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ?";
			}
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(2, value2);
			}
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GeneralLedgerOpeningObject gloo = new GeneralLedgerOpeningObject();
				gloo.pkId = new Integer(rs.getInt("pkid"));
				gloo.glId = new Integer(rs.getInt("glid"));
				gloo.fiscalYearId = new Integer(rs.getInt("fiscalyearid"));
				gloo.balance = rs.getBigDecimal("balance");
				gloo.openTime = rs.getTimestamp("opentime");
				gloo.status = rs.getString("status");
				gloo.userIdEdit = new Integer(rs.getInt("userid_edit"));
				gloo.lastUpdate = rs.getTimestamp("lastupdate");
				vecValObj.add(gloo);
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return vecValObj;
	}
}
/// to be DEPRECATED!!! DO NOT USE!!
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
import javax.ejb.*;
import javax.naming.*;
import java.math.BigDecimal;
import com.vlee.local.*;
import com.vlee.util.*;

public class GeneralLedgerOpeningBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String TABLENAME = "acc_general_ledger_opening";
	protected final String strObjectName = "GeneralLedgerOpeningBean: ";
	private EntityContext mContext;
	public static final String STATUS = "status";
	public static final String GLID = "glid";
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	private Integer pkId;
	private Integer glId;
	private Integer fiscalYearId;
	private BigDecimal balance;
	private Timestamp openTime;
	private String status;
	private Integer userIdEdit;
	private Timestamp lastUpdate;

	public Integer getPkId()
	{
		return this.pkId;
	}

	public Integer getGlId()
	{
		return this.glId;
	}

	public Integer getFiscalYearId()
	{
		return this.fiscalYearId;
	}

	public BigDecimal getBalance()
	{
		return this.balance;
	}

	public Timestamp getOpenTime()
	{
		return this.openTime;
	}

	public String getStatus()
	{
		return this.status;
	}

	public Integer getUserIdEdit()
	{
		return this.userIdEdit;
	}

	public Timestamp getLastUpdate()
	{
		return this.lastUpdate;
	}

	public void setPkId(Integer pkid)
	{
		this.pkId = pkid;
	}

	public void setGlId(Integer glid)
	{
		this.glId = glid;
	}

	public void setFiscalYearId(Integer fiscalYearId)
	{
		this.fiscalYearId = fiscalYearId;
	}

	public void setBalance(BigDecimal balance)
	{
		this.balance = balance;
	}

	public void setOpenTime(Timestamp opentime)
	{
		this.openTime = opentime;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public void setUserIdEdit(Integer useridedit)
	{
		this.userIdEdit = useridedit;
	}

	public void setLastUpdate(Timestamp lastupdate)
	{
		this.lastUpdate = lastupdate;
	}

	public GeneralLedgerOpeningObject getValueObject()
	{
		GeneralLedgerOpeningObject gloo = new GeneralLedgerOpeningObject();
		gloo.pkId = this.pkId;
		gloo.glId = this.glId;
		gloo.fiscalYearId = this.fiscalYearId;
		gloo.balance = this.balance;
		gloo.openTime = this.openTime;
		gloo.status = this.status;
		gloo.userIdEdit = this.userIdEdit;
		gloo.lastUpdate = this.lastUpdate;
		return gloo;
	}

	public void setValueObject(GeneralLedgerOpeningObject gloo) throws Exception
	{
		if (gloo == null)
		{
			throw new Exception("Object undefined");
		}
		this.glId = gloo.glId;
		this.fiscalYearId = gloo.fiscalYearId;
		this.balance = gloo.balance;
		this.openTime = gloo.openTime;
		this.status = gloo.status;
		this.userIdEdit = gloo.userIdEdit;
		this.lastUpdate = gloo.lastUpdate;
	}

	public void setEntityContext(EntityContext mContext)
	{
		Log.printVerbose(strObjectName + " In setEntityContext");
		this.mContext = mContext;
		Log.printVerbose(strObjectName + " Leaving setEntityContext");
	}

	public void unsetEntityContext()
	{
		Log.printVerbose(strObjectName + " In unsetEntityContext");
		this.mContext = null;
		Log.printVerbose(strObjectName + " Leaving unsetEntityContext");
	}

	public Integer ejbCreate(Integer glId, Integer fiscalYearId, BigDecimal balance, Timestamp openTime, String status,
			Integer userIdEdit, Timestamp lastUpdate) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(glId, fiscalYearId, balance, openTime, status, userIdEdit, lastUpdate);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.glId = glId;
			this.fiscalYearId = fiscalYearId;
			this.balance = balance;
			this.openTime = openTime;
			this.status = status;
			this.userIdEdit = userIdEdit;
			this.lastUpdate = lastUpdate;
		}
		Log.printVerbose(strObjectName + " Leaving ejbCreate");
		return newPkId;
	}

	public void ejbRemove()
	{
		Log.printVerbose(strObjectName + " In ejbRemove");
		deleteObject(this.pkId);
		Log.printVerbose(strObjectName + " Leaving ejbRemove");
	}

	public void ejbActivate()
	{
		Log.printVerbose(strObjectName + " In ejbActivate");
		this.pkId = (Integer) mContext.getPrimaryKey();
		Log.printVerbose(strObjectName + " Leaving ejbActivate");
	}

	public void ejbPassivate()
	{
		Log.printVerbose(strObjectName + " In ejbPassivate");
		this.pkId = null;
		Log.printVerbose(strObjectName + " Leaving ejbPassivate");
	}

	public void ejbLoad()
	{
		Log.printVerbose(strObjectName + " In ejbLoad");
		loadObject();
		Log.printVerbose(strObjectName + " Leaving ejbLoad");
	}

	public void ejbStore()
	{
		Log.printVerbose(strObjectName + " In ejbStore");
		storeObject();
		Log.printVerbose(strObjectName + " Leaving ejbStore");
	}

	public void ejbPostCreate(Integer glId, Integer fiscalYearId, BigDecimal balance, Timestamp openTime,
			String status, Integer userIdEdit, Timestamp lastUpdate)
	{
	}

	public Integer ejbFindByPrimaryKey(Integer pkid) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindByPrimaryKey");
		boolean result;
		result = selectByPrimaryKey(pkid);
		Log.printVerbose(strObjectName + " Leaving ejbFindByPrimaryKey");
		if (result)
		{
			return pkid;
		} else
		{
			throw new ObjectNotFoundException("Row for id " + pkid.toString() + " not found.");
		}
	}

	public Collection ejbFindAllObjects() throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindAllObjects");
		Collection col = selectAllObjects();
		Log.printVerbose(strObjectName + " Leaving ejbFindAllObjects");
		return col;
	}

	public Collection ejbFindObjectsGiven(String fieldName, String value) throws FinderException
	{
		Log.printVerbose(strObjectName + " In ejbFindObjectsGiven");
		Collection col = selectObjectsGiven(fieldName, value);
		Log.printVerbose(strObjectName + " Leaving ejbFindObjectsGiven");
		return col;
	}

	public Vector ejbHomeGetValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsGiven");
		Vector vecValObj = selectValueObjectsGiven(fieldName1, value1, fieldName2, value2);
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetValueObjectsGiven");
		return vecValObj;
	}

	/** ***************** Database Routines ************************ */
	private DataSource getDataSource()
	{
		try
		{
			InitialContext ic = new InitialContext();
			DataSource ds = (DataSource) ic.lookup(dsName);
			return ds;
		} catch (NamingException ne)
		{
			throw new EJBException("Naming lookup failure: " + ne.getMessage());
		}
	}

	private void cleanup(Connection cn, PreparedStatement ps)
	{
		try
		{
			if (ps != null)
				ps.close();
		} catch (Exception e)
		{
			throw new EJBException(e);
		}
		try
		{
			if (cn != null)
				cn.close();
		} catch (Exception e)
		{
			throw new EJBException(e);
		}
	}

	private Integer insertNewRow(Integer glId, Integer fiscalYearId, BigDecimal balance, Timestamp openTime,
			String status, Integer userIdEdit, Timestamp lastUpdate)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO "
					+ TABLENAME
					+ " (pkid, glid, fiscalyearid, balance, opentime, status, userid_edit, lastupdate) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setInt(2, glId.intValue());
			ps.setInt(3, fiscalYearId.intValue());
			ps.setBigDecimal(4, balance);
			ps.setTimestamp(5, openTime);
			ps.setString(6, status);
			ps.setInt(7, userIdEdit.intValue());
			ps.setTimestamp(8, lastUpdate);
			ps.executeUpdate();
			Log.printAudit(strObjectName + " Created New Row:" + newPkId.toString());
			return newPkId;
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private Integer getNextPKId()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT MAX(pkid) as max_pkid FROM " + TABLENAME;
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			rs.next();
			int max = rs.getInt("max_pkid");
			if (max == 0)
			{
				max = 1000;
			} else
			{
				max += 1;
			}
			return new Integer(max);
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private void deleteObject(Integer pkid)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "DELETE FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, pkid.intValue());
			ps.executeUpdate();
			Log.printAudit(strObjectName + " Deleted Object: " + pkid.toString());
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private void loadObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT pkid, glid, fiscalyearid, balance, opentime, status, userid_edit, lastupdate FROM "
					+ TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.glId = new Integer(rs.getInt("glid"));
				this.fiscalYearId = new Integer(rs.getInt("fiscalyearid"));
				this.balance = rs.getBigDecimal("balance");
				this.openTime = rs.getTimestamp("opentime");
				this.status = rs.getString("status");
				this.userIdEdit = new Integer(rs.getInt("userid_edit"));
				this.lastUpdate = rs.getTimestamp("lastupdate");
			} else
			{
				throw new NoSuchEntityException("Row for this EJB Object is not found in database.");
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private void storeObject()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "UPDATE "
					+ TABLENAME
					+ " SET glid = ?, fiscalyearid = ?, balance = ?, opentime = ?, status = ?, userid_edit = ?, lastupdate = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.glId.intValue());
			ps.setInt(2, this.fiscalYearId.intValue());
			ps.setBigDecimal(3, this.balance);
			ps.setTimestamp(4, this.openTime);
			ps.setString(5, this.status);
			ps.setInt(6, this.userIdEdit.intValue());
			ps.setTimestamp(7, this.lastUpdate);
			ps.setInt(8, this.pkId.intValue());
			int rowCount = ps.executeUpdate();
			if (rowCount == 0)
			{
				throw new EJBException("Storing ejb object " + this.pkId.toString() + " failed.");
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private boolean selectByPrimaryKey(Integer pkid)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT * FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, pkid.intValue());
			ResultSet rs = ps.executeQuery();
			boolean result = rs.next();
			return result;
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private Collection selectAllObjects()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " ORDER BY pkid";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				//rs.beforeTheFirstRecord();
				while (rs.next())
				{
					objectSet.add(new Integer(rs.getInt(1)));
				}
				return objectSet;
			} else
			{
				return null;
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private Collection selectObjectsGiven(String fieldName, String value)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " WHERE " + fieldName + " = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				//rs.beforeTheFirstRecord();
				while (rs.next())
				{
					objectSet.add(new Integer(rs.getInt(1)));
				}
				return objectSet;
			} else
			{
				return null;
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
	}

	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT pkid, glid, fiscalyearid, balance, opentime, status, userid_edit, lastupdate FROM "
					+ TABLENAME + " WHERE " + fieldName1 + " = ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ?";
			}
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(2, value2);
			}
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				GeneralLedgerOpeningObject gloo = new GeneralLedgerOpeningObject();
				gloo.pkId = new Integer(rs.getInt("pkid"));
				gloo.glId = new Integer(rs.getInt("glid"));
				gloo.fiscalYearId = new Integer(rs.getInt("fiscalyearid"));
				gloo.balance = rs.getBigDecimal("balance");
				gloo.openTime = rs.getTimestamp("opentime");
				gloo.status = rs.getString("status");
				gloo.userIdEdit = new Integer(rs.getInt("userid_edit"));
				gloo.lastUpdate = rs.getTimestamp("lastupdate");
				vecValObj.add(gloo);
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		} finally
		{
			cleanup(cn, ps);
		}
		return vecValObj;
	}
}
