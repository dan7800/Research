// Copyright of VLEE (http://vlee.net/)
// All rights reserved.
package com.vlee.ejb.accounting;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import java.math.BigDecimal;
import com.vlee.local.*;
import com.vlee.util.*;

public class RetainedEarningsBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	private final String TABLENAME = "acc_retained_earnings";
	private final String strObjectName = "RetainedEarningsBean: ";
	private EntityContext mContext;
	private Integer pkId;
	private Integer pcCenterId;
	private Integer batchId;
	private Integer fiscalYearId;
	private BigDecimal retainedEarnings;
	private String status;

	public Integer getPkId()
	{
		return this.pkId;
	}

	public Integer getPCCenterId()
	{
		return this.pcCenterId;
	}

	public Integer getBatchId()
	{
		return this.batchId;
	}

	public Integer getFiscalYearId()
	{
		return this.fiscalYearId;
	}

	public BigDecimal getRetainedEarnings()
	{
		return this.retainedEarnings;
	}

	public String getStatus()
	{
		return this.status;
	}

	public void setPkId(Integer jentid)
	{
		this.pkId = jentid;
	}

	public void setPCCenterId(Integer pcCenterId)
	{
		this.pcCenterId = pcCenterId;
	}

	public void setBatchId(Integer batchId)
	{
		this.batchId = batchId;
	}

	public void setFiscalYearId(Integer fiscalYearId)
	{
		this.fiscalYearId = fiscalYearId;
	}

	public void setRetainedEarnings(BigDecimal retainedEarnings)
	{
		this.retainedEarnings = retainedEarnings;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public RetainedEarningsObject getValueObject()
	{
		RetainedEarningsObject reo = new RetainedEarningsObject();
		reo.pkId = this.pkId;
		reo.pcCenterId = this.pcCenterId;
		reo.batchId = this.batchId;
		reo.fiscalYearId = this.fiscalYearId;
		reo.retainedEarnings = this.retainedEarnings;
		reo.status = this.status;
		return reo;
	}

	public void setValueObject(RetainedEarningsObject reo) throws Exception
	{
		if (reo == null)
		{
			throw new Exception("Object undefined");
		}
		this.pcCenterId = reo.pcCenterId;
		this.batchId = reo.batchId;
		this.fiscalYearId = reo.fiscalYearId;
		this.retainedEarnings = reo.retainedEarnings;
		this.status = reo.status;
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

	public Integer ejbCreate(Integer pcCenterId, Integer batchId, Integer fiscalYearId, BigDecimal retainedEarnings,
			String status) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(pcCenterId, batchId, fiscalYearId, retainedEarnings, status);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.pcCenterId = pcCenterId;
			this.batchId = batchId;
			this.fiscalYearId = fiscalYearId;
			this.retainedEarnings = retainedEarnings;
			this.status = status;
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

	public void ejbPostCreate(Integer pcCenterId, Integer batchId, Integer fiscalYearId, BigDecimal retainedEarnings,
			String status) throws CreateException
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

	public Collection ejbFindObjectsGiven(String fieldName1, String value1, String fieldName2, String value2,
			String fieldName3, String value3)
	{
		Log.printVerbose(strObjectName + " In ejbFindObjectsGiven");
		Collection col = selectObjectsGiven(fieldName1, value1, fieldName2, value2, fieldName3, value3);
		Log.printVerbose(strObjectName + " Leaving ejbFindObjectsGiven");
		return col;
	}

	public Vector ejbHomeGetValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2,
			String fieldName3, String value3)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsGiven");
		Vector vecValObj = selectValueObjectsGiven(fieldName1, value1, fieldName2, value2, fieldName3, value3);
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

	private Integer insertNewRow(Integer pcCenterId, Integer batchId, Integer fiscalYearId,
			BigDecimal retainedEarnings, String status)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO " + TABLENAME
					+ " (pkid, pccenterid, batchid, fiscalyearid, retainedearnings, status) VALUES "
					+ " (?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setInt(2, pcCenterId.intValue());
			ps.setInt(3, batchId.intValue());
			ps.setInt(4, fiscalYearId.intValue());
			ps.setBigDecimal(5, retainedEarnings);
			ps.setString(6, status);
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
			String sqlStatement = "SELECT pkid, pccenterid, batchid, fiscalyearid, retainedearnings, status FROM "
					+ TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.pcCenterId = new Integer(rs.getInt("pccenterid"));
				this.batchId = new Integer(rs.getInt("batchid"));
				this.fiscalYearId = new Integer(rs.getInt("fiscalyearid"));
				this.retainedEarnings = rs.getBigDecimal("retainedearnings");
				this.status = rs.getString("status");
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
					+ " SET pccenterid = ?, batchid = ?, fiscalyearid = ?, retainedearnings = ?, status = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pcCenterId.intValue());
			ps.setInt(2, this.batchId.intValue());
			ps.setInt(3, this.fiscalYearId.intValue());
			ps.setBigDecimal(4, this.retainedEarnings);
			ps.setString(5, this.status);
			ps.setInt(6, this.pkId.intValue());
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

	private Collection selectObjectsGiven(String fieldName1, String value1, String fieldName2, String value2,
			String fieldName3, String value3)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " WHERE " + fieldName1 + " = ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ?";
			}
			if (fieldName3 != null && value3 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName3 + " = ?";
			}
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(2, value2);
			}
			if (fieldName3 != null && value3 != null)
			{
				ps.setString(3, value3);
			}
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

	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2,
			String fieldName3, String value3)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT pkid, pccenterid, batchid, fiscalyearid, retainedearnings, status FROM "
					+ TABLENAME + " WHERE " + fieldName1 + " = ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ?";
			}
			if (fieldName3 != null && value3 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName3 + " = ?";
			}
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(2, value2);
			}
			if (fieldName3 != null && value3 != null)
			{
				ps.setString(3, value3);
			}
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				RetainedEarningsObject reo = new RetainedEarningsObject();
				reo.pkId = new Integer(rs.getInt("pkid"));
				reo.pcCenterId = new Integer(rs.getInt("pccenterid"));
				reo.batchId = new Integer(rs.getInt("batchid"));
				reo.fiscalYearId = new Integer(rs.getInt("fiscalyearid"));
				reo.retainedEarnings = rs.getBigDecimal("retainedearnings");
				reo.status = rs.getString("status");
				vecValObj.add(reo);
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
}// Copyright of VLEE (http://vlee.net/)
// All rights reserved.
package com.vlee.ejb.accounting;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import java.math.BigDecimal;
import com.vlee.local.*;
import com.vlee.util.*;

public class RetainedEarningsBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	private final String TABLENAME = "acc_retained_earnings";
	private final String strObjectName = "RetainedEarningsBean: ";
	private EntityContext mContext;
	private Integer pkId;
	private Integer pcCenterId;
	private Integer batchId;
	private Integer fiscalYearId;
	private BigDecimal retainedEarnings;
	private String status;

	public Integer getPkId()
	{
		return this.pkId;
	}

	public Integer getPCCenterId()
	{
		return this.pcCenterId;
	}

	public Integer getBatchId()
	{
		return this.batchId;
	}

	public Integer getFiscalYearId()
	{
		return this.fiscalYearId;
	}

	public BigDecimal getRetainedEarnings()
	{
		return this.retainedEarnings;
	}

	public String getStatus()
	{
		return this.status;
	}

	public void setPkId(Integer jentid)
	{
		this.pkId = jentid;
	}

	public void setPCCenterId(Integer pcCenterId)
	{
		this.pcCenterId = pcCenterId;
	}

	public void setBatchId(Integer batchId)
	{
		this.batchId = batchId;
	}

	public void setFiscalYearId(Integer fiscalYearId)
	{
		this.fiscalYearId = fiscalYearId;
	}

	public void setRetainedEarnings(BigDecimal retainedEarnings)
	{
		this.retainedEarnings = retainedEarnings;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public RetainedEarningsObject getValueObject()
	{
		RetainedEarningsObject reo = new RetainedEarningsObject();
		reo.pkId = this.pkId;
		reo.pcCenterId = this.pcCenterId;
		reo.batchId = this.batchId;
		reo.fiscalYearId = this.fiscalYearId;
		reo.retainedEarnings = this.retainedEarnings;
		reo.status = this.status;
		return reo;
	}

	public void setValueObject(RetainedEarningsObject reo) throws Exception
	{
		if (reo == null)
		{
			throw new Exception("Object undefined");
		}
		this.pcCenterId = reo.pcCenterId;
		this.batchId = reo.batchId;
		this.fiscalYearId = reo.fiscalYearId;
		this.retainedEarnings = reo.retainedEarnings;
		this.status = reo.status;
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

	public Integer ejbCreate(Integer pcCenterId, Integer batchId, Integer fiscalYearId, BigDecimal retainedEarnings,
			String status) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(pcCenterId, batchId, fiscalYearId, retainedEarnings, status);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.pcCenterId = pcCenterId;
			this.batchId = batchId;
			this.fiscalYearId = fiscalYearId;
			this.retainedEarnings = retainedEarnings;
			this.status = status;
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

	public void ejbPostCreate(Integer pcCenterId, Integer batchId, Integer fiscalYearId, BigDecimal retainedEarnings,
			String status) throws CreateException
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

	public Collection ejbFindObjectsGiven(String fieldName1, String value1, String fieldName2, String value2,
			String fieldName3, String value3)
	{
		Log.printVerbose(strObjectName + " In ejbFindObjectsGiven");
		Collection col = selectObjectsGiven(fieldName1, value1, fieldName2, value2, fieldName3, value3);
		Log.printVerbose(strObjectName + " Leaving ejbFindObjectsGiven");
		return col;
	}

	public Vector ejbHomeGetValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2,
			String fieldName3, String value3)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsGiven");
		Vector vecValObj = selectValueObjectsGiven(fieldName1, value1, fieldName2, value2, fieldName3, value3);
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

	private Integer insertNewRow(Integer pcCenterId, Integer batchId, Integer fiscalYearId,
			BigDecimal retainedEarnings, String status)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO " + TABLENAME
					+ " (pkid, pccenterid, batchid, fiscalyearid, retainedearnings, status) VALUES "
					+ " (?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setInt(2, pcCenterId.intValue());
			ps.setInt(3, batchId.intValue());
			ps.setInt(4, fiscalYearId.intValue());
			ps.setBigDecimal(5, retainedEarnings);
			ps.setString(6, status);
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
			String sqlStatement = "SELECT pkid, pccenterid, batchid, fiscalyearid, retainedearnings, status FROM "
					+ TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.pcCenterId = new Integer(rs.getInt("pccenterid"));
				this.batchId = new Integer(rs.getInt("batchid"));
				this.fiscalYearId = new Integer(rs.getInt("fiscalyearid"));
				this.retainedEarnings = rs.getBigDecimal("retainedearnings");
				this.status = rs.getString("status");
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
					+ " SET pccenterid = ?, batchid = ?, fiscalyearid = ?, retainedearnings = ?, status = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pcCenterId.intValue());
			ps.setInt(2, this.batchId.intValue());
			ps.setInt(3, this.fiscalYearId.intValue());
			ps.setBigDecimal(4, this.retainedEarnings);
			ps.setString(5, this.status);
			ps.setInt(6, this.pkId.intValue());
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

	private Collection selectObjectsGiven(String fieldName1, String value1, String fieldName2, String value2,
			String fieldName3, String value3)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " WHERE " + fieldName1 + " = ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ?";
			}
			if (fieldName3 != null && value3 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName3 + " = ?";
			}
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(2, value2);
			}
			if (fieldName3 != null && value3 != null)
			{
				ps.setString(3, value3);
			}
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

	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2,
			String fieldName3, String value3)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT pkid, pccenterid, batchid, fiscalyearid, retainedearnings, status FROM "
					+ TABLENAME + " WHERE " + fieldName1 + " = ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ?";
			}
			if (fieldName3 != null && value3 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName3 + " = ?";
			}
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(2, value2);
			}
			if (fieldName3 != null && value3 != null)
			{
				ps.setString(3, value3);
			}
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				RetainedEarningsObject reo = new RetainedEarningsObject();
				reo.pkId = new Integer(rs.getInt("pkid"));
				reo.pcCenterId = new Integer(rs.getInt("pccenterid"));
				reo.batchId = new Integer(rs.getInt("batchid"));
				reo.fiscalYearId = new Integer(rs.getInt("fiscalyearid"));
				reo.retainedEarnings = rs.getBigDecimal("retainedearnings");
				reo.status = rs.getString("status");
				vecValObj.add(reo);
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
}// Copyright of VLEE (http://vlee.net/)
// All rights reserved.
package com.vlee.ejb.accounting;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import java.math.BigDecimal;
import com.vlee.local.*;
import com.vlee.util.*;

public class RetainedEarningsBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	private final String TABLENAME = "acc_retained_earnings";
	private final String strObjectName = "RetainedEarningsBean: ";
	private EntityContext mContext;
	private Integer pkId;
	private Integer pcCenterId;
	private Integer batchId;
	private Integer fiscalYearId;
	private BigDecimal retainedEarnings;
	private String status;

	public Integer getPkId()
	{
		return this.pkId;
	}

	public Integer getPCCenterId()
	{
		return this.pcCenterId;
	}

	public Integer getBatchId()
	{
		return this.batchId;
	}

	public Integer getFiscalYearId()
	{
		return this.fiscalYearId;
	}

	public BigDecimal getRetainedEarnings()
	{
		return this.retainedEarnings;
	}

	public String getStatus()
	{
		return this.status;
	}

	public void setPkId(Integer jentid)
	{
		this.pkId = jentid;
	}

	public void setPCCenterId(Integer pcCenterId)
	{
		this.pcCenterId = pcCenterId;
	}

	public void setBatchId(Integer batchId)
	{
		this.batchId = batchId;
	}

	public void setFiscalYearId(Integer fiscalYearId)
	{
		this.fiscalYearId = fiscalYearId;
	}

	public void setRetainedEarnings(BigDecimal retainedEarnings)
	{
		this.retainedEarnings = retainedEarnings;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public RetainedEarningsObject getValueObject()
	{
		RetainedEarningsObject reo = new RetainedEarningsObject();
		reo.pkId = this.pkId;
		reo.pcCenterId = this.pcCenterId;
		reo.batchId = this.batchId;
		reo.fiscalYearId = this.fiscalYearId;
		reo.retainedEarnings = this.retainedEarnings;
		reo.status = this.status;
		return reo;
	}

	public void setValueObject(RetainedEarningsObject reo) throws Exception
	{
		if (reo == null)
		{
			throw new Exception("Object undefined");
		}
		this.pcCenterId = reo.pcCenterId;
		this.batchId = reo.batchId;
		this.fiscalYearId = reo.fiscalYearId;
		this.retainedEarnings = reo.retainedEarnings;
		this.status = reo.status;
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

	public Integer ejbCreate(Integer pcCenterId, Integer batchId, Integer fiscalYearId, BigDecimal retainedEarnings,
			String status) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(pcCenterId, batchId, fiscalYearId, retainedEarnings, status);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.pcCenterId = pcCenterId;
			this.batchId = batchId;
			this.fiscalYearId = fiscalYearId;
			this.retainedEarnings = retainedEarnings;
			this.status = status;
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

	public void ejbPostCreate(Integer pcCenterId, Integer batchId, Integer fiscalYearId, BigDecimal retainedEarnings,
			String status) throws CreateException
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

	public Collection ejbFindObjectsGiven(String fieldName1, String value1, String fieldName2, String value2,
			String fieldName3, String value3)
	{
		Log.printVerbose(strObjectName + " In ejbFindObjectsGiven");
		Collection col = selectObjectsGiven(fieldName1, value1, fieldName2, value2, fieldName3, value3);
		Log.printVerbose(strObjectName + " Leaving ejbFindObjectsGiven");
		return col;
	}

	public Vector ejbHomeGetValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2,
			String fieldName3, String value3)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsGiven");
		Vector vecValObj = selectValueObjectsGiven(fieldName1, value1, fieldName2, value2, fieldName3, value3);
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

	private Integer insertNewRow(Integer pcCenterId, Integer batchId, Integer fiscalYearId,
			BigDecimal retainedEarnings, String status)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO " + TABLENAME
					+ " (pkid, pccenterid, batchid, fiscalyearid, retainedearnings, status) VALUES "
					+ " (?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setInt(2, pcCenterId.intValue());
			ps.setInt(3, batchId.intValue());
			ps.setInt(4, fiscalYearId.intValue());
			ps.setBigDecimal(5, retainedEarnings);
			ps.setString(6, status);
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
			String sqlStatement = "SELECT pkid, pccenterid, batchid, fiscalyearid, retainedearnings, status FROM "
					+ TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.pcCenterId = new Integer(rs.getInt("pccenterid"));
				this.batchId = new Integer(rs.getInt("batchid"));
				this.fiscalYearId = new Integer(rs.getInt("fiscalyearid"));
				this.retainedEarnings = rs.getBigDecimal("retainedearnings");
				this.status = rs.getString("status");
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
					+ " SET pccenterid = ?, batchid = ?, fiscalyearid = ?, retainedearnings = ?, status = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pcCenterId.intValue());
			ps.setInt(2, this.batchId.intValue());
			ps.setInt(3, this.fiscalYearId.intValue());
			ps.setBigDecimal(4, this.retainedEarnings);
			ps.setString(5, this.status);
			ps.setInt(6, this.pkId.intValue());
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

	private Collection selectObjectsGiven(String fieldName1, String value1, String fieldName2, String value2,
			String fieldName3, String value3)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " WHERE " + fieldName1 + " = ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ?";
			}
			if (fieldName3 != null && value3 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName3 + " = ?";
			}
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(2, value2);
			}
			if (fieldName3 != null && value3 != null)
			{
				ps.setString(3, value3);
			}
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

	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2,
			String fieldName3, String value3)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT pkid, pccenterid, batchid, fiscalyearid, retainedearnings, status FROM "
					+ TABLENAME + " WHERE " + fieldName1 + " = ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ?";
			}
			if (fieldName3 != null && value3 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName3 + " = ?";
			}
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(2, value2);
			}
			if (fieldName3 != null && value3 != null)
			{
				ps.setString(3, value3);
			}
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				RetainedEarningsObject reo = new RetainedEarningsObject();
				reo.pkId = new Integer(rs.getInt("pkid"));
				reo.pcCenterId = new Integer(rs.getInt("pccenterid"));
				reo.batchId = new Integer(rs.getInt("batchid"));
				reo.fiscalYearId = new Integer(rs.getInt("fiscalyearid"));
				reo.retainedEarnings = rs.getBigDecimal("retainedearnings");
				reo.status = rs.getString("status");
				vecValObj.add(reo);
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
}// Copyright of VLEE (http://vlee.net/)
// All rights reserved.
package com.vlee.ejb.accounting;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import javax.ejb.*;
import javax.naming.*;
import java.math.BigDecimal;
import com.vlee.local.*;
import com.vlee.util.*;

public class RetainedEarningsBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	private final String TABLENAME = "acc_retained_earnings";
	private final String strObjectName = "RetainedEarningsBean: ";
	private EntityContext mContext;
	private Integer pkId;
	private Integer pcCenterId;
	private Integer batchId;
	private Integer fiscalYearId;
	private BigDecimal retainedEarnings;
	private String status;

	public Integer getPkId()
	{
		return this.pkId;
	}

	public Integer getPCCenterId()
	{
		return this.pcCenterId;
	}

	public Integer getBatchId()
	{
		return this.batchId;
	}

	public Integer getFiscalYearId()
	{
		return this.fiscalYearId;
	}

	public BigDecimal getRetainedEarnings()
	{
		return this.retainedEarnings;
	}

	public String getStatus()
	{
		return this.status;
	}

	public void setPkId(Integer jentid)
	{
		this.pkId = jentid;
	}

	public void setPCCenterId(Integer pcCenterId)
	{
		this.pcCenterId = pcCenterId;
	}

	public void setBatchId(Integer batchId)
	{
		this.batchId = batchId;
	}

	public void setFiscalYearId(Integer fiscalYearId)
	{
		this.fiscalYearId = fiscalYearId;
	}

	public void setRetainedEarnings(BigDecimal retainedEarnings)
	{
		this.retainedEarnings = retainedEarnings;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public RetainedEarningsObject getValueObject()
	{
		RetainedEarningsObject reo = new RetainedEarningsObject();
		reo.pkId = this.pkId;
		reo.pcCenterId = this.pcCenterId;
		reo.batchId = this.batchId;
		reo.fiscalYearId = this.fiscalYearId;
		reo.retainedEarnings = this.retainedEarnings;
		reo.status = this.status;
		return reo;
	}

	public void setValueObject(RetainedEarningsObject reo) throws Exception
	{
		if (reo == null)
		{
			throw new Exception("Object undefined");
		}
		this.pcCenterId = reo.pcCenterId;
		this.batchId = reo.batchId;
		this.fiscalYearId = reo.fiscalYearId;
		this.retainedEarnings = reo.retainedEarnings;
		this.status = reo.status;
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

	public Integer ejbCreate(Integer pcCenterId, Integer batchId, Integer fiscalYearId, BigDecimal retainedEarnings,
			String status) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(pcCenterId, batchId, fiscalYearId, retainedEarnings, status);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.pcCenterId = pcCenterId;
			this.batchId = batchId;
			this.fiscalYearId = fiscalYearId;
			this.retainedEarnings = retainedEarnings;
			this.status = status;
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

	public void ejbPostCreate(Integer pcCenterId, Integer batchId, Integer fiscalYearId, BigDecimal retainedEarnings,
			String status) throws CreateException
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

	public Collection ejbFindObjectsGiven(String fieldName1, String value1, String fieldName2, String value2,
			String fieldName3, String value3)
	{
		Log.printVerbose(strObjectName + " In ejbFindObjectsGiven");
		Collection col = selectObjectsGiven(fieldName1, value1, fieldName2, value2, fieldName3, value3);
		Log.printVerbose(strObjectName + " Leaving ejbFindObjectsGiven");
		return col;
	}

	public Vector ejbHomeGetValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2,
			String fieldName3, String value3)
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetValueObjectsGiven");
		Vector vecValObj = selectValueObjectsGiven(fieldName1, value1, fieldName2, value2, fieldName3, value3);
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

	private Integer insertNewRow(Integer pcCenterId, Integer batchId, Integer fiscalYearId,
			BigDecimal retainedEarnings, String status)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO " + TABLENAME
					+ " (pkid, pccenterid, batchid, fiscalyearid, retainedearnings, status) VALUES "
					+ " (?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setInt(2, pcCenterId.intValue());
			ps.setInt(3, batchId.intValue());
			ps.setInt(4, fiscalYearId.intValue());
			ps.setBigDecimal(5, retainedEarnings);
			ps.setString(6, status);
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
			String sqlStatement = "SELECT pkid, pccenterid, batchid, fiscalyearid, retainedearnings, status FROM "
					+ TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.pcCenterId = new Integer(rs.getInt("pccenterid"));
				this.batchId = new Integer(rs.getInt("batchid"));
				this.fiscalYearId = new Integer(rs.getInt("fiscalyearid"));
				this.retainedEarnings = rs.getBigDecimal("retainedearnings");
				this.status = rs.getString("status");
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
					+ " SET pccenterid = ?, batchid = ?, fiscalyearid = ?, retainedearnings = ?, status = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pcCenterId.intValue());
			ps.setInt(2, this.batchId.intValue());
			ps.setInt(3, this.fiscalYearId.intValue());
			ps.setBigDecimal(4, this.retainedEarnings);
			ps.setString(5, this.status);
			ps.setInt(6, this.pkId.intValue());
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

	private Collection selectObjectsGiven(String fieldName1, String value1, String fieldName2, String value2,
			String fieldName3, String value3)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			ArrayList objectSet = new ArrayList();
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " WHERE " + fieldName1 + " = ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ?";
			}
			if (fieldName3 != null && value3 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName3 + " = ?";
			}
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(2, value2);
			}
			if (fieldName3 != null && value3 != null)
			{
				ps.setString(3, value3);
			}
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

	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2,
			String fieldName3, String value3)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT pkid, pccenterid, batchid, fiscalyearid, retainedearnings, status FROM "
					+ TABLENAME + " WHERE " + fieldName1 + " = ?";
			if (fieldName2 != null && value2 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName2 + " = ?";
			}
			if (fieldName3 != null && value3 != null)
			{
				sqlStatement = sqlStatement + " AND " + fieldName3 + " = ?";
			}
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, value1);
			if (fieldName2 != null && value2 != null)
			{
				ps.setString(2, value2);
			}
			if (fieldName3 != null && value3 != null)
			{
				ps.setString(3, value3);
			}
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				RetainedEarningsObject reo = new RetainedEarningsObject();
				reo.pkId = new Integer(rs.getInt("pkid"));
				reo.pcCenterId = new Integer(rs.getInt("pccenterid"));
				reo.batchId = new Integer(rs.getInt("batchid"));
				reo.fiscalYearId = new Integer(rs.getInt("fiscalyearid"));
				reo.retainedEarnings = rs.getBigDecimal("retainedearnings");
				reo.status = rs.getString("status");
				vecValObj.add(reo);
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