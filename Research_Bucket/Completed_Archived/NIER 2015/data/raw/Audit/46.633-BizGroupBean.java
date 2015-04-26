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
import com.vlee.local.*;
import com.vlee.util.*;

public class BizGroupBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String TABLENAME = "acc_bizgroup_index";
	protected final String strObjectName = "BizGroupBean: ";
	protected final String strTimeBegin = "0000-01-01 00:00:00.000000000";
	private EntityContext mContext;
	public static final String STATUS = "status";
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	private Integer pkId;
	private String bizGroupCode;
	private String name;
	private String description;
	private String status;
	private Timestamp createTime;
	private Timestamp lastUpdate;
	private Timestamp freezeTime;
	private Integer userIdCreate;
	private Integer userIdUpdate;
	private Integer userIdFreeze;

	public Integer getPkId()
	{
		return this.pkId;
	}

	public String getBizGroupCode()
	{
		return this.bizGroupCode;
	}

	public String getName()
	{
		return this.name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public String getStatus()
	{
		return this.status;
	}

	public Timestamp getCreateTime()
	{
		return this.createTime;
	}

	public Timestamp getLastUpdate()
	{
		return this.lastUpdate;
	}

	public Timestamp getFreezeTime()
	{
		return this.freezeTime;
	}

	public Integer getUserIdCreate()
	{
		return this.userIdCreate;
	}

	public Integer getUserIdUpdate()
	{
		return this.userIdUpdate;
	}

	public Integer getUserIdFreeze()
	{
		return this.userIdFreeze;
	}

	public void setPkId(Integer pccid)
	{
		this.pkId = pccid;
	}

	public void setBizGroupCode(String strBUCode)
	{
		this.bizGroupCode = strBUCode;
	}

	public void setName(String strName)
	{
		this.name = strName;
	}

	public void setDescription(String strDesc)
	{
		this.description = strDesc;
	}

	public void setStatus(String stts)
	{
		this.status = stts;
	}

	public void setCreateTime(Timestamp tsTime)
	{
		this.createTime = tsTime;
	}

	public void setLastUpdate(Timestamp tsTime)
	{
		this.lastUpdate = tsTime;
	}

	public void setFreezeTime(Timestamp tsTime)
	{
		this.freezeTime = tsTime;
	}

	public void setUserIdCreate(Integer intUserId)
	{
		this.userIdCreate = intUserId;
	}

	public void setUserIdUpdate(Integer intUserId)
	{
		this.userIdUpdate = intUserId;
	}

	public void setUserIdFreeze(Integer intUserId)
	{
		this.userIdFreeze = intUserId;
	}

	public BizGroupObject getValueObject()
	{
		BizGroupObject bgo = new BizGroupObject();
		bgo.pkId = this.pkId;
		bgo.bizGroupCode = this.bizGroupCode;
		bgo.name = this.name;
		bgo.description = this.description;
		bgo.status = this.status;
		bgo.createTime = this.createTime;
		bgo.lastUpdate = this.lastUpdate;
		bgo.freezeTime = this.freezeTime;
		bgo.userIdCreate = this.userIdCreate;
		bgo.userIdUpdate = this.userIdUpdate;
		bgo.userIdFreeze = this.userIdFreeze;
		return bgo;
	}

	public void setValueObject(BizGroupObject bgo) throws Exception
	{
		if (bgo == null)
		{
			throw new Exception("Object undefined");
		}
		this.bizGroupCode = bgo.bizGroupCode;
		this.name = bgo.name;
		this.description = bgo.description;
		this.status = bgo.status;
		this.createTime = bgo.createTime;
		this.lastUpdate = bgo.lastUpdate;
		this.freezeTime = bgo.freezeTime;
		this.userIdCreate = bgo.userIdCreate;
		this.userIdUpdate = bgo.userIdUpdate;
		this.userIdFreeze = bgo.userIdFreeze;
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

	public Integer ejbCreate(String bizGroupCode, String name, String description, Timestamp tsCreate,
			Integer userIdCreate) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(bizGroupCode, name, description, tsCreate, userIdCreate);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.bizGroupCode = bizGroupCode;
			this.name = name;
			this.description = description;
			this.status = BizGroupBean.ACTIVE;
			this.createTime = tsCreate;
			this.lastUpdate = tsCreate;
			this.freezeTime = Timestamp.valueOf(strTimeBegin);
			this.userIdCreate = userIdCreate;
			this.userIdUpdate = userIdCreate;
			this.userIdFreeze = new Integer(0);
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

	public void ejbPostCreate(String bizGroupCode, String name, String description, Timestamp tsCreate,
			Integer userIdCreate)
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

	private Integer insertNewRow(String bizGroupCode, String name, String description, Timestamp tsCreate,
			Integer userIdCreate)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO "
					+ TABLENAME
					+ " (pkid , bizgroupcode, name, description, status, createtime, lastupdate, freezetime, userid_create, userid_update, userid_freeze)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, bizGroupCode);
			ps.setString(3, name);
			ps.setString(4, description);
			ps.setString(5, BizGroupBean.ACTIVE);
			ps.setTimestamp(6, tsCreate);
			ps.setTimestamp(7, tsCreate);
			ps.setTimestamp(8, Timestamp.valueOf(strTimeBegin));
			ps.setInt(9, userIdCreate.intValue());
			ps.setInt(10, 0);
			ps.setInt(11, 0);
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
			String sqlStatement = "SELECT pkid, bizgroupcode, name, description, status, createtime, lastupdate, freezetime, userid_create, userid_update, userid_freeze"
					+ " FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.bizGroupCode = rs.getString("bizgroupcode");
				this.name = rs.getString("name");
				this.description = rs.getString("description");
				this.status = rs.getString("status");
				this.createTime = rs.getTimestamp("createtime");
				this.lastUpdate = rs.getTimestamp("lastupdate");
				this.freezeTime = rs.getTimestamp("freezetime");
				this.userIdCreate = new Integer(rs.getInt("userid_create"));
				this.userIdUpdate = new Integer(rs.getInt("userid_update"));
				this.userIdFreeze = new Integer(rs.getInt("userid_freeze"));
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
					+ " SET bizgroupcode =  ?, name = ?, description = ?, status = ?, createtime = ?, lastupdate = ?, freezetime = ?,"
					+ " userid_create = ?, userid_update = ?, userid_freeze = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.bizGroupCode);
			ps.setString(2, this.name);
			ps.setString(3, this.description);
			ps.setString(4, this.status);
			ps.setTimestamp(5, this.createTime);
			ps.setTimestamp(6, this.lastUpdate);
			ps.setTimestamp(7, this.freezeTime);
			ps.setInt(8, this.userIdCreate.intValue());
			ps.setInt(9, this.userIdUpdate.intValue());
			ps.setInt(10, this.userIdFreeze.intValue());
			ps.setInt(11, this.pkId.intValue());
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
		ArrayList objectSet = new ArrayList();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT pkid FROM " + TABLENAME + " ORDER BY pkid";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				objectSet.add(new Integer(rs.getInt(1)));
			}
		} catch (Exception e)
		{
			throw new EJBException(e);
		}
		finally
		{
			cleanup(cn, ps);
		}
		return objectSet;
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
}
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
import com.vlee.local.*;
import com.vlee.util.*;

public class BizGroupBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String TABLENAME = "acc_bizgroup_index";
	protected final String strObjectName = "BizGroupBean: ";
	protected final String strTimeBegin = "0000-01-01 00:00:00.000000000";
	private EntityContext mContext;
	public static final String STATUS = "status";
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	private Integer pkId;
	private String bizGroupCode;
	private String name;
	private String description;
	private String status;
	private Timestamp createTime;
	private Timestamp lastUpdate;
	private Timestamp freezeTime;
	private Integer userIdCreate;
	private Integer userIdUpdate;
	private Integer userIdFreeze;

	public Integer getPkId()
	{
		return this.pkId;
	}

	public String getBizGroupCode()
	{
		return this.bizGroupCode;
	}

	public String getName()
	{
		return this.name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public String getStatus()
	{
		return this.status;
	}

	public Timestamp getCreateTime()
	{
		return this.createTime;
	}

	public Timestamp getLastUpdate()
	{
		return this.lastUpdate;
	}

	public Timestamp getFreezeTime()
	{
		return this.freezeTime;
	}

	public Integer getUserIdCreate()
	{
		return this.userIdCreate;
	}

	public Integer getUserIdUpdate()
	{
		return this.userIdUpdate;
	}

	public Integer getUserIdFreeze()
	{
		return this.userIdFreeze;
	}

	public void setPkId(Integer pccid)
	{
		this.pkId = pccid;
	}

	public void setBizGroupCode(String strBUCode)
	{
		this.bizGroupCode = strBUCode;
	}

	public void setName(String strName)
	{
		this.name = strName;
	}

	public void setDescription(String strDesc)
	{
		this.description = strDesc;
	}

	public void setStatus(String stts)
	{
		this.status = stts;
	}

	public void setCreateTime(Timestamp tsTime)
	{
		this.createTime = tsTime;
	}

	public void setLastUpdate(Timestamp tsTime)
	{
		this.lastUpdate = tsTime;
	}

	public void setFreezeTime(Timestamp tsTime)
	{
		this.freezeTime = tsTime;
	}

	public void setUserIdCreate(Integer intUserId)
	{
		this.userIdCreate = intUserId;
	}

	public void setUserIdUpdate(Integer intUserId)
	{
		this.userIdUpdate = intUserId;
	}

	public void setUserIdFreeze(Integer intUserId)
	{
		this.userIdFreeze = intUserId;
	}

	public BizGroupObject getValueObject()
	{
		BizGroupObject bgo = new BizGroupObject();
		bgo.pkId = this.pkId;
		bgo.bizGroupCode = this.bizGroupCode;
		bgo.name = this.name;
		bgo.description = this.description;
		bgo.status = this.status;
		bgo.createTime = this.createTime;
		bgo.lastUpdate = this.lastUpdate;
		bgo.freezeTime = this.freezeTime;
		bgo.userIdCreate = this.userIdCreate;
		bgo.userIdUpdate = this.userIdUpdate;
		bgo.userIdFreeze = this.userIdFreeze;
		return bgo;
	}

	public void setValueObject(BizGroupObject bgo) throws Exception
	{
		if (bgo == null)
		{
			throw new Exception("Object undefined");
		}
		this.bizGroupCode = bgo.bizGroupCode;
		this.name = bgo.name;
		this.description = bgo.description;
		this.status = bgo.status;
		this.createTime = bgo.createTime;
		this.lastUpdate = bgo.lastUpdate;
		this.freezeTime = bgo.freezeTime;
		this.userIdCreate = bgo.userIdCreate;
		this.userIdUpdate = bgo.userIdUpdate;
		this.userIdFreeze = bgo.userIdFreeze;
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

	public Integer ejbCreate(String bizGroupCode, String name, String description, Timestamp tsCreate,
			Integer userIdCreate) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(bizGroupCode, name, description, tsCreate, userIdCreate);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.bizGroupCode = bizGroupCode;
			this.name = name;
			this.description = description;
			this.status = BizGroupBean.ACTIVE;
			this.createTime = tsCreate;
			this.lastUpdate = tsCreate;
			this.freezeTime = Timestamp.valueOf(strTimeBegin);
			this.userIdCreate = userIdCreate;
			this.userIdUpdate = userIdCreate;
			this.userIdFreeze = new Integer(0);
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

	public void ejbPostCreate(String bizGroupCode, String name, String description, Timestamp tsCreate,
			Integer userIdCreate)
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

	private Integer insertNewRow(String bizGroupCode, String name, String description, Timestamp tsCreate,
			Integer userIdCreate)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO "
					+ TABLENAME
					+ " (pkid , bizgroupcode, name, description, status, createtime, lastupdate, freezetime, userid_create, userid_update, userid_freeze)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, bizGroupCode);
			ps.setString(3, name);
			ps.setString(4, description);
			ps.setString(5, BizGroupBean.ACTIVE);
			ps.setTimestamp(6, tsCreate);
			ps.setTimestamp(7, tsCreate);
			ps.setTimestamp(8, Timestamp.valueOf(strTimeBegin));
			ps.setInt(9, userIdCreate.intValue());
			ps.setInt(10, 0);
			ps.setInt(11, 0);
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
			String sqlStatement = "SELECT pkid, bizgroupcode, name, description, status, createtime, lastupdate, freezetime, userid_create, userid_update, userid_freeze"
					+ " FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.bizGroupCode = rs.getString("bizgroupcode");
				this.name = rs.getString("name");
				this.description = rs.getString("description");
				this.status = rs.getString("status");
				this.createTime = rs.getTimestamp("createtime");
				this.lastUpdate = rs.getTimestamp("lastupdate");
				this.freezeTime = rs.getTimestamp("freezetime");
				this.userIdCreate = new Integer(rs.getInt("userid_create"));
				this.userIdUpdate = new Integer(rs.getInt("userid_update"));
				this.userIdFreeze = new Integer(rs.getInt("userid_freeze"));
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
					+ " SET bizgroupcode =  ?, name = ?, description = ?, status = ?, createtime = ?, lastupdate = ?, freezetime = ?,"
					+ " userid_create = ?, userid_update = ?, userid_freeze = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.bizGroupCode);
			ps.setString(2, this.name);
			ps.setString(3, this.description);
			ps.setString(4, this.status);
			ps.setTimestamp(5, this.createTime);
			ps.setTimestamp(6, this.lastUpdate);
			ps.setTimestamp(7, this.freezeTime);
			ps.setInt(8, this.userIdCreate.intValue());
			ps.setInt(9, this.userIdUpdate.intValue());
			ps.setInt(10, this.userIdFreeze.intValue());
			ps.setInt(11, this.pkId.intValue());
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
}
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
import com.vlee.local.*;
import com.vlee.util.*;

public class BizGroupBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String TABLENAME = "acc_bizgroup_index";
	protected final String strObjectName = "BizGroupBean: ";
	protected final String strTimeBegin = "0000-01-01 00:00:00.000000000";
	private EntityContext mContext;
	public static final String STATUS = "status";
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	private Integer pkId;
	private String bizGroupCode;
	private String name;
	private String description;
	private String status;
	private Timestamp createTime;
	private Timestamp lastUpdate;
	private Timestamp freezeTime;
	private Integer userIdCreate;
	private Integer userIdUpdate;
	private Integer userIdFreeze;

	public Integer getPkId()
	{
		return this.pkId;
	}

	public String getBizGroupCode()
	{
		return this.bizGroupCode;
	}

	public String getName()
	{
		return this.name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public String getStatus()
	{
		return this.status;
	}

	public Timestamp getCreateTime()
	{
		return this.createTime;
	}

	public Timestamp getLastUpdate()
	{
		return this.lastUpdate;
	}

	public Timestamp getFreezeTime()
	{
		return this.freezeTime;
	}

	public Integer getUserIdCreate()
	{
		return this.userIdCreate;
	}

	public Integer getUserIdUpdate()
	{
		return this.userIdUpdate;
	}

	public Integer getUserIdFreeze()
	{
		return this.userIdFreeze;
	}

	public void setPkId(Integer pccid)
	{
		this.pkId = pccid;
	}

	public void setBizGroupCode(String strBUCode)
	{
		this.bizGroupCode = strBUCode;
	}

	public void setName(String strName)
	{
		this.name = strName;
	}

	public void setDescription(String strDesc)
	{
		this.description = strDesc;
	}

	public void setStatus(String stts)
	{
		this.status = stts;
	}

	public void setCreateTime(Timestamp tsTime)
	{
		this.createTime = tsTime;
	}

	public void setLastUpdate(Timestamp tsTime)
	{
		this.lastUpdate = tsTime;
	}

	public void setFreezeTime(Timestamp tsTime)
	{
		this.freezeTime = tsTime;
	}

	public void setUserIdCreate(Integer intUserId)
	{
		this.userIdCreate = intUserId;
	}

	public void setUserIdUpdate(Integer intUserId)
	{
		this.userIdUpdate = intUserId;
	}

	public void setUserIdFreeze(Integer intUserId)
	{
		this.userIdFreeze = intUserId;
	}

	public BizGroupObject getValueObject()
	{
		BizGroupObject bgo = new BizGroupObject();
		bgo.pkId = this.pkId;
		bgo.bizGroupCode = this.bizGroupCode;
		bgo.name = this.name;
		bgo.description = this.description;
		bgo.status = this.status;
		bgo.createTime = this.createTime;
		bgo.lastUpdate = this.lastUpdate;
		bgo.freezeTime = this.freezeTime;
		bgo.userIdCreate = this.userIdCreate;
		bgo.userIdUpdate = this.userIdUpdate;
		bgo.userIdFreeze = this.userIdFreeze;
		return bgo;
	}

	public void setValueObject(BizGroupObject bgo) throws Exception
	{
		if (bgo == null)
		{
			throw new Exception("Object undefined");
		}
		this.bizGroupCode = bgo.bizGroupCode;
		this.name = bgo.name;
		this.description = bgo.description;
		this.status = bgo.status;
		this.createTime = bgo.createTime;
		this.lastUpdate = bgo.lastUpdate;
		this.freezeTime = bgo.freezeTime;
		this.userIdCreate = bgo.userIdCreate;
		this.userIdUpdate = bgo.userIdUpdate;
		this.userIdFreeze = bgo.userIdFreeze;
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

	public Integer ejbCreate(String bizGroupCode, String name, String description, Timestamp tsCreate,
			Integer userIdCreate) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(bizGroupCode, name, description, tsCreate, userIdCreate);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.bizGroupCode = bizGroupCode;
			this.name = name;
			this.description = description;
			this.status = BizGroupBean.ACTIVE;
			this.createTime = tsCreate;
			this.lastUpdate = tsCreate;
			this.freezeTime = Timestamp.valueOf(strTimeBegin);
			this.userIdCreate = userIdCreate;
			this.userIdUpdate = userIdCreate;
			this.userIdFreeze = new Integer(0);
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

	public void ejbPostCreate(String bizGroupCode, String name, String description, Timestamp tsCreate,
			Integer userIdCreate)
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

	private Integer insertNewRow(String bizGroupCode, String name, String description, Timestamp tsCreate,
			Integer userIdCreate)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO "
					+ TABLENAME
					+ " (pkid , bizgroupcode, name, description, status, createtime, lastupdate, freezetime, userid_create, userid_update, userid_freeze)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, bizGroupCode);
			ps.setString(3, name);
			ps.setString(4, description);
			ps.setString(5, BizGroupBean.ACTIVE);
			ps.setTimestamp(6, tsCreate);
			ps.setTimestamp(7, tsCreate);
			ps.setTimestamp(8, Timestamp.valueOf(strTimeBegin));
			ps.setInt(9, userIdCreate.intValue());
			ps.setInt(10, 0);
			ps.setInt(11, 0);
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
			String sqlStatement = "SELECT pkid, bizgroupcode, name, description, status, createtime, lastupdate, freezetime, userid_create, userid_update, userid_freeze"
					+ " FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.bizGroupCode = rs.getString("bizgroupcode");
				this.name = rs.getString("name");
				this.description = rs.getString("description");
				this.status = rs.getString("status");
				this.createTime = rs.getTimestamp("createtime");
				this.lastUpdate = rs.getTimestamp("lastupdate");
				this.freezeTime = rs.getTimestamp("freezetime");
				this.userIdCreate = new Integer(rs.getInt("userid_create"));
				this.userIdUpdate = new Integer(rs.getInt("userid_update"));
				this.userIdFreeze = new Integer(rs.getInt("userid_freeze"));
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
					+ " SET bizgroupcode =  ?, name = ?, description = ?, status = ?, createtime = ?, lastupdate = ?, freezetime = ?,"
					+ " userid_create = ?, userid_update = ?, userid_freeze = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.bizGroupCode);
			ps.setString(2, this.name);
			ps.setString(3, this.description);
			ps.setString(4, this.status);
			ps.setTimestamp(5, this.createTime);
			ps.setTimestamp(6, this.lastUpdate);
			ps.setTimestamp(7, this.freezeTime);
			ps.setInt(8, this.userIdCreate.intValue());
			ps.setInt(9, this.userIdUpdate.intValue());
			ps.setInt(10, this.userIdFreeze.intValue());
			ps.setInt(11, this.pkId.intValue());
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
}
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
import com.vlee.local.*;
import com.vlee.util.*;

public class BizGroupBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String TABLENAME = "acc_bizgroup_index";
	protected final String strObjectName = "BizGroupBean: ";
	protected final String strTimeBegin = "0000-01-01 00:00:00.000000000";
	private EntityContext mContext;
	public static final String STATUS = "status";
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	private Integer pkId;
	private String bizGroupCode;
	private String name;
	private String description;
	private String status;
	private Timestamp createTime;
	private Timestamp lastUpdate;
	private Timestamp freezeTime;
	private Integer userIdCreate;
	private Integer userIdUpdate;
	private Integer userIdFreeze;

	public Integer getPkId()
	{
		return this.pkId;
	}

	public String getBizGroupCode()
	{
		return this.bizGroupCode;
	}

	public String getName()
	{
		return this.name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public String getStatus()
	{
		return this.status;
	}

	public Timestamp getCreateTime()
	{
		return this.createTime;
	}

	public Timestamp getLastUpdate()
	{
		return this.lastUpdate;
	}

	public Timestamp getFreezeTime()
	{
		return this.freezeTime;
	}

	public Integer getUserIdCreate()
	{
		return this.userIdCreate;
	}

	public Integer getUserIdUpdate()
	{
		return this.userIdUpdate;
	}

	public Integer getUserIdFreeze()
	{
		return this.userIdFreeze;
	}

	public void setPkId(Integer pccid)
	{
		this.pkId = pccid;
	}

	public void setBizGroupCode(String strBUCode)
	{
		this.bizGroupCode = strBUCode;
	}

	public void setName(String strName)
	{
		this.name = strName;
	}

	public void setDescription(String strDesc)
	{
		this.description = strDesc;
	}

	public void setStatus(String stts)
	{
		this.status = stts;
	}

	public void setCreateTime(Timestamp tsTime)
	{
		this.createTime = tsTime;
	}

	public void setLastUpdate(Timestamp tsTime)
	{
		this.lastUpdate = tsTime;
	}

	public void setFreezeTime(Timestamp tsTime)
	{
		this.freezeTime = tsTime;
	}

	public void setUserIdCreate(Integer intUserId)
	{
		this.userIdCreate = intUserId;
	}

	public void setUserIdUpdate(Integer intUserId)
	{
		this.userIdUpdate = intUserId;
	}

	public void setUserIdFreeze(Integer intUserId)
	{
		this.userIdFreeze = intUserId;
	}

	public BizGroupObject getValueObject()
	{
		BizGroupObject bgo = new BizGroupObject();
		bgo.pkId = this.pkId;
		bgo.bizGroupCode = this.bizGroupCode;
		bgo.name = this.name;
		bgo.description = this.description;
		bgo.status = this.status;
		bgo.createTime = this.createTime;
		bgo.lastUpdate = this.lastUpdate;
		bgo.freezeTime = this.freezeTime;
		bgo.userIdCreate = this.userIdCreate;
		bgo.userIdUpdate = this.userIdUpdate;
		bgo.userIdFreeze = this.userIdFreeze;
		return bgo;
	}

	public void setValueObject(BizGroupObject bgo) throws Exception
	{
		if (bgo == null)
		{
			throw new Exception("Object undefined");
		}
		this.bizGroupCode = bgo.bizGroupCode;
		this.name = bgo.name;
		this.description = bgo.description;
		this.status = bgo.status;
		this.createTime = bgo.createTime;
		this.lastUpdate = bgo.lastUpdate;
		this.freezeTime = bgo.freezeTime;
		this.userIdCreate = bgo.userIdCreate;
		this.userIdUpdate = bgo.userIdUpdate;
		this.userIdFreeze = bgo.userIdFreeze;
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

	public Integer ejbCreate(String bizGroupCode, String name, String description, Timestamp tsCreate,
			Integer userIdCreate) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(bizGroupCode, name, description, tsCreate, userIdCreate);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.bizGroupCode = bizGroupCode;
			this.name = name;
			this.description = description;
			this.status = BizGroupBean.ACTIVE;
			this.createTime = tsCreate;
			this.lastUpdate = tsCreate;
			this.freezeTime = Timestamp.valueOf(strTimeBegin);
			this.userIdCreate = userIdCreate;
			this.userIdUpdate = userIdCreate;
			this.userIdFreeze = new Integer(0);
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

	public void ejbPostCreate(String bizGroupCode, String name, String description, Timestamp tsCreate,
			Integer userIdCreate)
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

	private Integer insertNewRow(String bizGroupCode, String name, String description, Timestamp tsCreate,
			Integer userIdCreate)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO "
					+ TABLENAME
					+ " (pkid , bizgroupcode, name, description, status, createtime, lastupdate, freezetime, userid_create, userid_update, userid_freeze)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, bizGroupCode);
			ps.setString(3, name);
			ps.setString(4, description);
			ps.setString(5, BizGroupBean.ACTIVE);
			ps.setTimestamp(6, tsCreate);
			ps.setTimestamp(7, tsCreate);
			ps.setTimestamp(8, Timestamp.valueOf(strTimeBegin));
			ps.setInt(9, userIdCreate.intValue());
			ps.setInt(10, 0);
			ps.setInt(11, 0);
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
			String sqlStatement = "SELECT pkid, bizgroupcode, name, description, status, createtime, lastupdate, freezetime, userid_create, userid_update, userid_freeze"
					+ " FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.bizGroupCode = rs.getString("bizgroupcode");
				this.name = rs.getString("name");
				this.description = rs.getString("description");
				this.status = rs.getString("status");
				this.createTime = rs.getTimestamp("createtime");
				this.lastUpdate = rs.getTimestamp("lastupdate");
				this.freezeTime = rs.getTimestamp("freezetime");
				this.userIdCreate = new Integer(rs.getInt("userid_create"));
				this.userIdUpdate = new Integer(rs.getInt("userid_update"));
				this.userIdFreeze = new Integer(rs.getInt("userid_freeze"));
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
					+ " SET bizgroupcode =  ?, name = ?, description = ?, status = ?, createtime = ?, lastupdate = ?, freezetime = ?,"
					+ " userid_create = ?, userid_update = ?, userid_freeze = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.bizGroupCode);
			ps.setString(2, this.name);
			ps.setString(3, this.description);
			ps.setString(4, this.status);
			ps.setTimestamp(5, this.createTime);
			ps.setTimestamp(6, this.lastUpdate);
			ps.setTimestamp(7, this.freezeTime);
			ps.setInt(8, this.userIdCreate.intValue());
			ps.setInt(9, this.userIdUpdate.intValue());
			ps.setInt(10, this.userIdFreeze.intValue());
			ps.setInt(11, this.pkId.intValue());
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
}
