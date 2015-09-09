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
import javax.ejb.*;
import javax.naming.*;
import com.vlee.local.*;
import com.vlee.util.*;

public class BatchBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	private final String TABLENAME = "acc_batch_index";
	private final String strObjectName = "BatchBean: ";
	public static final Integer PKID_DEFAULT = new Integer("1000");
	private EntityContext mContext;
	public static final String PKID = "pkid";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String DATE_START = "date_start";
	public static final String DATE_END = "date_end";
	public static final String OPTION1 = "option1";
	public static final String OPTION2 = "option2";
	public static final String OPTION3 = "option3";
	public static final String OPTION4 = "option4";
	public static final String OPTION5 = "option5";
	private Integer pkId;
	private String name;
	private String description;
	private Timestamp dateStart;
	private Timestamp dateEnd;
	private String option1;
	private String option2;
	private String option3;
	private String option4;
	private String option5;

	public Integer getPkId()
	{
		return this.pkId;
	}

	public String getName()
	{
		return this.name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public Timestamp getDateStart()
	{
		return this.dateStart;
	}

	public Timestamp getDateEnd()
	{
		return this.dateEnd;
	}

	public String getOption1()
	{
		return this.option1;
	}

	public String getOption2()
	{
		return this.option2;
	}

	public String getOption3()
	{
		return this.option3;
	}

	public String getOption4()
	{
		return this.option4;
	}

	public String getOption5()
	{
		return this.option5;
	}

	public void setPkId(Integer jentid)
	{
		this.pkId = jentid;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setDateStart(Timestamp dateStart)
	{
		this.dateStart = dateStart;
	}

	public void setDateEnd(Timestamp dateEnd)
	{
		this.dateEnd = dateEnd;
	}

	public void setOption1(String option1)
	{
		this.option1 = option1;
	}

	public void setOption2(String option2)
	{
		this.option2 = option2;
	}

	public void setOption3(String option3)
	{
		this.option3 = option3;
	}

	public void setOption4(String option4)
	{
		this.option4 = option4;
	}

	public void setOption5(String option5)
	{
		this.option5 = option5;
	}

	public BatchObject getValueObject()
	{
		BatchObject bo = new BatchObject();
		bo.pkId = this.pkId;
		bo.name = this.name;
		bo.description = this.description;
		bo.dateStart = this.dateStart;
		bo.dateEnd = this.dateEnd;
		bo.option1 = this.option1;
		bo.option2 = this.option2;
		bo.option3 = this.option3;
		bo.option4 = this.option4;
		bo.option5 = this.option5;
		return bo;
	}

	public void setValueObject(BatchObject bo) throws Exception
	{
		if (bo == null)
		{
			throw new Exception("Object undefined");
		}
		this.name = bo.name;
		this.description = bo.description;
		this.dateStart = bo.dateStart;
		this.dateEnd = bo.dateEnd;
		this.option1 = bo.option1;
		this.option2 = bo.option2;
		this.option3 = bo.option3;
		this.option4 = bo.option4;
		this.option5 = bo.option5;
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

	public Integer ejbCreate(String name, String description, Timestamp dateStart, Timestamp dateEnd, String option1,
			String option2, String option3, String option4, String option5) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(name, description, dateStart, dateEnd, option1, option2, option3, option4, option5);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.name = name;
			this.description = description;
			this.dateStart = dateStart;
			this.dateEnd = dateEnd;
			this.option1 = option1;
			this.option2 = option2;
			this.option3 = option3;
			this.option4 = option4;
			this.option5 = option5;
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

	public void ejbPostCreate(String name, String description, Timestamp dateStart, Timestamp dateEnd, String option1,
			String option2, String option3, String option4, String option5) throws CreateException
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

	public Collection ejbFindObjectsGiven(String fieldName, String value)
	{
		Log.printVerbose(strObjectName + " In ejbFindObjectsGiven");
		Collection col = selectObjectsGiven(fieldName, value);
		Log.printVerbose(strObjectName + " Leaving ejbFindObjectsGiven");
		return col;
	}

	public Vector ejbHomeGetAllValueObjects()
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetAllValueObjects");
		Vector vecValObj = selectAllValueObjects();
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetAllValueObjects");
		return vecValObj;
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

	private Integer insertNewRow(String name, String description, Timestamp dateStart, Timestamp dateEnd,
			String option1, String option2, String option3, String option4, String option5)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO "
					+ TABLENAME
					+ " (pkid, name, description, date_start, date_end, option1, option2, option3, option4, option5) VALUES "
					+ " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, name);
			ps.setString(3, description);
			ps.setTimestamp(4, dateStart);
			ps.setTimestamp(5, dateEnd);
			ps.setString(6, option1);
			ps.setString(7, option2);
			ps.setString(8, option3);
			ps.setString(9, option4);
			ps.setString(10, option5);
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
			String sqlStatement = "SELECT pkid, name, description, date_start, date_end, option1, option2, option3, option4, option5 FROM "
					+ TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.name = rs.getString("name");
				this.description = rs.getString("description");
				this.dateStart = rs.getTimestamp("date_start");
				this.dateEnd = rs.getTimestamp("date_end");
				this.option1 = rs.getString("option1");
				this.option2 = rs.getString("option2");
				this.option3 = rs.getString("option3");
				this.option4 = rs.getString("option4");
				this.option5 = rs.getString("option5");
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
			String sqlStatement = "UPDATE " + TABLENAME
					+ " SET name = ?, description = ?, date_start = ?, date_end = ?,"
					+ " option1 = ?, option2 = ?, option3 = ?, option4 = ?, option5 = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.name);
			ps.setString(2, this.description);
			ps.setTimestamp(3, this.dateStart);
			ps.setTimestamp(4, this.dateEnd);
			ps.setString(5, this.option1);
			ps.setString(6, this.option2);
			ps.setString(7, this.option3);
			ps.setString(8, this.option4);
			ps.setString(9, this.option5);
			ps.setInt(10, this.pkId.intValue());
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

	private Vector selectAllValueObjects()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT pkid, name, description, date_start, date_end, option1, option2, option3, option4, option5 FROM "
					+ TABLENAME + " ORDER BY pkid";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				BatchObject bo = new BatchObject();
				bo.pkId = new Integer(rs.getInt("pkid"));
				bo.name = rs.getString("name");
				bo.description = rs.getString("description");
				bo.dateStart = rs.getTimestamp("date_start");
				bo.dateEnd = rs.getTimestamp("date_end");
				bo.option1 = rs.getString("option1");
				bo.option2 = rs.getString("option2");
				bo.option3 = rs.getString("option3");
				bo.option4 = rs.getString("option4");
				bo.option5 = rs.getString("option5");
				vecValObj.add(bo);
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

	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			/*
			 * String sqlStatement = "SELECT pkid, name, description,
			 * date_start, " + " date_end, option1, option2, option3, option4,
			 * option5 FROM " + TABLENAME + " WHERE " + fieldName1 + " = ?";
			 * if(fieldName2 != null && value2 != null) { sqlStatement =
			 * sqlStatement + " AND " + fieldName2 + " = ?"; }
			 */
			String[] conditions = new String[] {};
			if (fieldName1 != null && value1 != null && fieldName2 == null && value2 == null)
			{
				conditions = new String[] { fieldName1 + " ='" + value1 + "' " };
			}
			if (fieldName1 != null && value1 != null && fieldName2 != null && value2 != null)
			{
				conditions = new String[] { fieldName1 + " ='" + value1 + "' ", fieldName2 + "='" + value2 + "' " };
			}
			QueryObject query = new QueryObject(conditions);
			query.setOrder(" ORDER BY " + NAME);
			String sqlStatement = " SELECT * FROM " + TABLENAME;
			sqlStatement = query.appendQuery(sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			/*
			 * ps.setString(1, value1); if(fieldName2 != null && value2 != null) {
			 * ps.setString(2, value2); }
			 */
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				BatchObject bo = new BatchObject();
				bo.pkId = new Integer(rs.getInt("pkid"));
				bo.name = rs.getString("name");
				bo.description = rs.getString("description");
				bo.dateStart = rs.getTimestamp("date_start");
				bo.dateEnd = rs.getTimestamp("date_end");
				bo.option1 = rs.getString("option1");
				bo.option2 = rs.getString("option2");
				bo.option3 = rs.getString("option3");
				bo.option4 = rs.getString("option4");
				bo.option5 = rs.getString("option5");
				vecValObj.add(bo);
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
import javax.ejb.*;
import javax.naming.*;
import com.vlee.local.*;
import com.vlee.util.*;

public class BatchBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	private final String TABLENAME = "acc_batch_index";
	private final String strObjectName = "BatchBean: ";
	public static final Integer PKID_DEFAULT = new Integer("1000");
	private EntityContext mContext;
	public static final String PKID = "pkid";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String DATE_START = "date_start";
	public static final String DATE_END = "date_end";
	public static final String OPTION1 = "option1";
	public static final String OPTION2 = "option2";
	public static final String OPTION3 = "option3";
	public static final String OPTION4 = "option4";
	public static final String OPTION5 = "option5";
	private Integer pkId;
	private String name;
	private String description;
	private Timestamp dateStart;
	private Timestamp dateEnd;
	private String option1;
	private String option2;
	private String option3;
	private String option4;
	private String option5;

	public Integer getPkId()
	{
		return this.pkId;
	}

	public String getName()
	{
		return this.name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public Timestamp getDateStart()
	{
		return this.dateStart;
	}

	public Timestamp getDateEnd()
	{
		return this.dateEnd;
	}

	public String getOption1()
	{
		return this.option1;
	}

	public String getOption2()
	{
		return this.option2;
	}

	public String getOption3()
	{
		return this.option3;
	}

	public String getOption4()
	{
		return this.option4;
	}

	public String getOption5()
	{
		return this.option5;
	}

	public void setPkId(Integer jentid)
	{
		this.pkId = jentid;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setDateStart(Timestamp dateStart)
	{
		this.dateStart = dateStart;
	}

	public void setDateEnd(Timestamp dateEnd)
	{
		this.dateEnd = dateEnd;
	}

	public void setOption1(String option1)
	{
		this.option1 = option1;
	}

	public void setOption2(String option2)
	{
		this.option2 = option2;
	}

	public void setOption3(String option3)
	{
		this.option3 = option3;
	}

	public void setOption4(String option4)
	{
		this.option4 = option4;
	}

	public void setOption5(String option5)
	{
		this.option5 = option5;
	}

	public BatchObject getValueObject()
	{
		BatchObject bo = new BatchObject();
		bo.pkId = this.pkId;
		bo.name = this.name;
		bo.description = this.description;
		bo.dateStart = this.dateStart;
		bo.dateEnd = this.dateEnd;
		bo.option1 = this.option1;
		bo.option2 = this.option2;
		bo.option3 = this.option3;
		bo.option4 = this.option4;
		bo.option5 = this.option5;
		return bo;
	}

	public void setValueObject(BatchObject bo) throws Exception
	{
		if (bo == null)
		{
			throw new Exception("Object undefined");
		}
		this.name = bo.name;
		this.description = bo.description;
		this.dateStart = bo.dateStart;
		this.dateEnd = bo.dateEnd;
		this.option1 = bo.option1;
		this.option2 = bo.option2;
		this.option3 = bo.option3;
		this.option4 = bo.option4;
		this.option5 = bo.option5;
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

	public Integer ejbCreate(String name, String description, Timestamp dateStart, Timestamp dateEnd, String option1,
			String option2, String option3, String option4, String option5) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(name, description, dateStart, dateEnd, option1, option2, option3, option4, option5);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.name = name;
			this.description = description;
			this.dateStart = dateStart;
			this.dateEnd = dateEnd;
			this.option1 = option1;
			this.option2 = option2;
			this.option3 = option3;
			this.option4 = option4;
			this.option5 = option5;
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

	public void ejbPostCreate(String name, String description, Timestamp dateStart, Timestamp dateEnd, String option1,
			String option2, String option3, String option4, String option5) throws CreateException
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

	public Collection ejbFindObjectsGiven(String fieldName, String value)
	{
		Log.printVerbose(strObjectName + " In ejbFindObjectsGiven");
		Collection col = selectObjectsGiven(fieldName, value);
		Log.printVerbose(strObjectName + " Leaving ejbFindObjectsGiven");
		return col;
	}

	public Vector ejbHomeGetAllValueObjects()
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetAllValueObjects");
		Vector vecValObj = selectAllValueObjects();
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetAllValueObjects");
		return vecValObj;
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

	private Integer insertNewRow(String name, String description, Timestamp dateStart, Timestamp dateEnd,
			String option1, String option2, String option3, String option4, String option5)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO "
					+ TABLENAME
					+ " (pkid, name, description, date_start, date_end, option1, option2, option3, option4, option5) VALUES "
					+ " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, name);
			ps.setString(3, description);
			ps.setTimestamp(4, dateStart);
			ps.setTimestamp(5, dateEnd);
			ps.setString(6, option1);
			ps.setString(7, option2);
			ps.setString(8, option3);
			ps.setString(9, option4);
			ps.setString(10, option5);
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
			String sqlStatement = "SELECT pkid, name, description, date_start, date_end, option1, option2, option3, option4, option5 FROM "
					+ TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.name = rs.getString("name");
				this.description = rs.getString("description");
				this.dateStart = rs.getTimestamp("date_start");
				this.dateEnd = rs.getTimestamp("date_end");
				this.option1 = rs.getString("option1");
				this.option2 = rs.getString("option2");
				this.option3 = rs.getString("option3");
				this.option4 = rs.getString("option4");
				this.option5 = rs.getString("option5");
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
			String sqlStatement = "UPDATE " + TABLENAME
					+ " SET name = ?, description = ?, date_start = ?, date_end = ?,"
					+ " option1 = ?, option2 = ?, option3 = ?, option4 = ?, option5 = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.name);
			ps.setString(2, this.description);
			ps.setTimestamp(3, this.dateStart);
			ps.setTimestamp(4, this.dateEnd);
			ps.setString(5, this.option1);
			ps.setString(6, this.option2);
			ps.setString(7, this.option3);
			ps.setString(8, this.option4);
			ps.setString(9, this.option5);
			ps.setInt(10, this.pkId.intValue());
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

	private Vector selectAllValueObjects()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT pkid, name, description, date_start, date_end, option1, option2, option3, option4, option5 FROM "
					+ TABLENAME + " ORDER BY pkid";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				BatchObject bo = new BatchObject();
				bo.pkId = new Integer(rs.getInt("pkid"));
				bo.name = rs.getString("name");
				bo.description = rs.getString("description");
				bo.dateStart = rs.getTimestamp("date_start");
				bo.dateEnd = rs.getTimestamp("date_end");
				bo.option1 = rs.getString("option1");
				bo.option2 = rs.getString("option2");
				bo.option3 = rs.getString("option3");
				bo.option4 = rs.getString("option4");
				bo.option5 = rs.getString("option5");
				vecValObj.add(bo);
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

	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			/*
			 * String sqlStatement = "SELECT pkid, name, description,
			 * date_start, " + " date_end, option1, option2, option3, option4,
			 * option5 FROM " + TABLENAME + " WHERE " + fieldName1 + " = ?";
			 * if(fieldName2 != null && value2 != null) { sqlStatement =
			 * sqlStatement + " AND " + fieldName2 + " = ?"; }
			 */
			String[] conditions = new String[] {};
			if (fieldName1 != null && value1 != null && fieldName2 == null && value2 == null)
			{
				conditions = new String[] { fieldName1 + " ='" + value1 + "' " };
			}
			if (fieldName1 != null && value1 != null && fieldName2 != null && value2 != null)
			{
				conditions = new String[] { fieldName1 + " ='" + value1 + "' ", fieldName2 + "='" + value2 + "' " };
			}
			QueryObject query = new QueryObject(conditions);
			query.setOrder(" ORDER BY " + NAME);
			String sqlStatement = " SELECT * FROM " + TABLENAME;
			sqlStatement = query.appendQuery(sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			/*
			 * ps.setString(1, value1); if(fieldName2 != null && value2 != null) {
			 * ps.setString(2, value2); }
			 */
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				BatchObject bo = new BatchObject();
				bo.pkId = new Integer(rs.getInt("pkid"));
				bo.name = rs.getString("name");
				bo.description = rs.getString("description");
				bo.dateStart = rs.getTimestamp("date_start");
				bo.dateEnd = rs.getTimestamp("date_end");
				bo.option1 = rs.getString("option1");
				bo.option2 = rs.getString("option2");
				bo.option3 = rs.getString("option3");
				bo.option4 = rs.getString("option4");
				bo.option5 = rs.getString("option5");
				vecValObj.add(bo);
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
import javax.ejb.*;
import javax.naming.*;
import com.vlee.local.*;
import com.vlee.util.*;

public class BatchBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	private final String TABLENAME = "acc_batch_index";
	private final String strObjectName = "BatchBean: ";
	public static final Integer PKID_DEFAULT = new Integer("1000");
	private EntityContext mContext;
	public static final String PKID = "pkid";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String DATE_START = "date_start";
	public static final String DATE_END = "date_end";
	public static final String OPTION1 = "option1";
	public static final String OPTION2 = "option2";
	public static final String OPTION3 = "option3";
	public static final String OPTION4 = "option4";
	public static final String OPTION5 = "option5";
	private Integer pkId;
	private String name;
	private String description;
	private Timestamp dateStart;
	private Timestamp dateEnd;
	private String option1;
	private String option2;
	private String option3;
	private String option4;
	private String option5;

	public Integer getPkId()
	{
		return this.pkId;
	}

	public String getName()
	{
		return this.name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public Timestamp getDateStart()
	{
		return this.dateStart;
	}

	public Timestamp getDateEnd()
	{
		return this.dateEnd;
	}

	public String getOption1()
	{
		return this.option1;
	}

	public String getOption2()
	{
		return this.option2;
	}

	public String getOption3()
	{
		return this.option3;
	}

	public String getOption4()
	{
		return this.option4;
	}

	public String getOption5()
	{
		return this.option5;
	}

	public void setPkId(Integer jentid)
	{
		this.pkId = jentid;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setDateStart(Timestamp dateStart)
	{
		this.dateStart = dateStart;
	}

	public void setDateEnd(Timestamp dateEnd)
	{
		this.dateEnd = dateEnd;
	}

	public void setOption1(String option1)
	{
		this.option1 = option1;
	}

	public void setOption2(String option2)
	{
		this.option2 = option2;
	}

	public void setOption3(String option3)
	{
		this.option3 = option3;
	}

	public void setOption4(String option4)
	{
		this.option4 = option4;
	}

	public void setOption5(String option5)
	{
		this.option5 = option5;
	}

	public BatchObject getValueObject()
	{
		BatchObject bo = new BatchObject();
		bo.pkId = this.pkId;
		bo.name = this.name;
		bo.description = this.description;
		bo.dateStart = this.dateStart;
		bo.dateEnd = this.dateEnd;
		bo.option1 = this.option1;
		bo.option2 = this.option2;
		bo.option3 = this.option3;
		bo.option4 = this.option4;
		bo.option5 = this.option5;
		return bo;
	}

	public void setValueObject(BatchObject bo) throws Exception
	{
		if (bo == null)
		{
			throw new Exception("Object undefined");
		}
		this.name = bo.name;
		this.description = bo.description;
		this.dateStart = bo.dateStart;
		this.dateEnd = bo.dateEnd;
		this.option1 = bo.option1;
		this.option2 = bo.option2;
		this.option3 = bo.option3;
		this.option4 = bo.option4;
		this.option5 = bo.option5;
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

	public Integer ejbCreate(String name, String description, Timestamp dateStart, Timestamp dateEnd, String option1,
			String option2, String option3, String option4, String option5) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(name, description, dateStart, dateEnd, option1, option2, option3, option4, option5);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.name = name;
			this.description = description;
			this.dateStart = dateStart;
			this.dateEnd = dateEnd;
			this.option1 = option1;
			this.option2 = option2;
			this.option3 = option3;
			this.option4 = option4;
			this.option5 = option5;
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

	public void ejbPostCreate(String name, String description, Timestamp dateStart, Timestamp dateEnd, String option1,
			String option2, String option3, String option4, String option5) throws CreateException
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

	public Collection ejbFindObjectsGiven(String fieldName, String value)
	{
		Log.printVerbose(strObjectName + " In ejbFindObjectsGiven");
		Collection col = selectObjectsGiven(fieldName, value);
		Log.printVerbose(strObjectName + " Leaving ejbFindObjectsGiven");
		return col;
	}

	public Vector ejbHomeGetAllValueObjects()
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetAllValueObjects");
		Vector vecValObj = selectAllValueObjects();
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetAllValueObjects");
		return vecValObj;
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

	private Integer insertNewRow(String name, String description, Timestamp dateStart, Timestamp dateEnd,
			String option1, String option2, String option3, String option4, String option5)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO "
					+ TABLENAME
					+ " (pkid, name, description, date_start, date_end, option1, option2, option3, option4, option5) VALUES "
					+ " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, name);
			ps.setString(3, description);
			ps.setTimestamp(4, dateStart);
			ps.setTimestamp(5, dateEnd);
			ps.setString(6, option1);
			ps.setString(7, option2);
			ps.setString(8, option3);
			ps.setString(9, option4);
			ps.setString(10, option5);
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
			String sqlStatement = "SELECT pkid, name, description, date_start, date_end, option1, option2, option3, option4, option5 FROM "
					+ TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.name = rs.getString("name");
				this.description = rs.getString("description");
				this.dateStart = rs.getTimestamp("date_start");
				this.dateEnd = rs.getTimestamp("date_end");
				this.option1 = rs.getString("option1");
				this.option2 = rs.getString("option2");
				this.option3 = rs.getString("option3");
				this.option4 = rs.getString("option4");
				this.option5 = rs.getString("option5");
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
			String sqlStatement = "UPDATE " + TABLENAME
					+ " SET name = ?, description = ?, date_start = ?, date_end = ?,"
					+ " option1 = ?, option2 = ?, option3 = ?, option4 = ?, option5 = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.name);
			ps.setString(2, this.description);
			ps.setTimestamp(3, this.dateStart);
			ps.setTimestamp(4, this.dateEnd);
			ps.setString(5, this.option1);
			ps.setString(6, this.option2);
			ps.setString(7, this.option3);
			ps.setString(8, this.option4);
			ps.setString(9, this.option5);
			ps.setInt(10, this.pkId.intValue());
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

	private Vector selectAllValueObjects()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT pkid, name, description, date_start, date_end, option1, option2, option3, option4, option5 FROM "
					+ TABLENAME + " ORDER BY pkid";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				BatchObject bo = new BatchObject();
				bo.pkId = new Integer(rs.getInt("pkid"));
				bo.name = rs.getString("name");
				bo.description = rs.getString("description");
				bo.dateStart = rs.getTimestamp("date_start");
				bo.dateEnd = rs.getTimestamp("date_end");
				bo.option1 = rs.getString("option1");
				bo.option2 = rs.getString("option2");
				bo.option3 = rs.getString("option3");
				bo.option4 = rs.getString("option4");
				bo.option5 = rs.getString("option5");
				vecValObj.add(bo);
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

	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			/*
			 * String sqlStatement = "SELECT pkid, name, description,
			 * date_start, " + " date_end, option1, option2, option3, option4,
			 * option5 FROM " + TABLENAME + " WHERE " + fieldName1 + " = ?";
			 * if(fieldName2 != null && value2 != null) { sqlStatement =
			 * sqlStatement + " AND " + fieldName2 + " = ?"; }
			 */
			String[] conditions = new String[] {};
			if (fieldName1 != null && value1 != null && fieldName2 == null && value2 == null)
			{
				conditions = new String[] { fieldName1 + " ='" + value1 + "' " };
			}
			if (fieldName1 != null && value1 != null && fieldName2 != null && value2 != null)
			{
				conditions = new String[] { fieldName1 + " ='" + value1 + "' ", fieldName2 + "='" + value2 + "' " };
			}
			QueryObject query = new QueryObject(conditions);
			query.setOrder(" ORDER BY " + NAME);
			String sqlStatement = " SELECT * FROM " + TABLENAME;
			sqlStatement = query.appendQuery(sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			/*
			 * ps.setString(1, value1); if(fieldName2 != null && value2 != null) {
			 * ps.setString(2, value2); }
			 */
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				BatchObject bo = new BatchObject();
				bo.pkId = new Integer(rs.getInt("pkid"));
				bo.name = rs.getString("name");
				bo.description = rs.getString("description");
				bo.dateStart = rs.getTimestamp("date_start");
				bo.dateEnd = rs.getTimestamp("date_end");
				bo.option1 = rs.getString("option1");
				bo.option2 = rs.getString("option2");
				bo.option3 = rs.getString("option3");
				bo.option4 = rs.getString("option4");
				bo.option5 = rs.getString("option5");
				vecValObj.add(bo);
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
import javax.ejb.*;
import javax.naming.*;
import com.vlee.local.*;
import com.vlee.util.*;

public class BatchBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	private final String TABLENAME = "acc_batch_index";
	private final String strObjectName = "BatchBean: ";
	public static final Integer PKID_DEFAULT = new Integer("1000");
	private EntityContext mContext;
	public static final String PKID = "pkid";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String DATE_START = "date_start";
	public static final String DATE_END = "date_end";
	public static final String OPTION1 = "option1";
	public static final String OPTION2 = "option2";
	public static final String OPTION3 = "option3";
	public static final String OPTION4 = "option4";
	public static final String OPTION5 = "option5";
	private Integer pkId;
	private String name;
	private String description;
	private Timestamp dateStart;
	private Timestamp dateEnd;
	private String option1;
	private String option2;
	private String option3;
	private String option4;
	private String option5;

	public Integer getPkId()
	{
		return this.pkId;
	}

	public String getName()
	{
		return this.name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public Timestamp getDateStart()
	{
		return this.dateStart;
	}

	public Timestamp getDateEnd()
	{
		return this.dateEnd;
	}

	public String getOption1()
	{
		return this.option1;
	}

	public String getOption2()
	{
		return this.option2;
	}

	public String getOption3()
	{
		return this.option3;
	}

	public String getOption4()
	{
		return this.option4;
	}

	public String getOption5()
	{
		return this.option5;
	}

	public void setPkId(Integer jentid)
	{
		this.pkId = jentid;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setDateStart(Timestamp dateStart)
	{
		this.dateStart = dateStart;
	}

	public void setDateEnd(Timestamp dateEnd)
	{
		this.dateEnd = dateEnd;
	}

	public void setOption1(String option1)
	{
		this.option1 = option1;
	}

	public void setOption2(String option2)
	{
		this.option2 = option2;
	}

	public void setOption3(String option3)
	{
		this.option3 = option3;
	}

	public void setOption4(String option4)
	{
		this.option4 = option4;
	}

	public void setOption5(String option5)
	{
		this.option5 = option5;
	}

	public BatchObject getValueObject()
	{
		BatchObject bo = new BatchObject();
		bo.pkId = this.pkId;
		bo.name = this.name;
		bo.description = this.description;
		bo.dateStart = this.dateStart;
		bo.dateEnd = this.dateEnd;
		bo.option1 = this.option1;
		bo.option2 = this.option2;
		bo.option3 = this.option3;
		bo.option4 = this.option4;
		bo.option5 = this.option5;
		return bo;
	}

	public void setValueObject(BatchObject bo) throws Exception
	{
		if (bo == null)
		{
			throw new Exception("Object undefined");
		}
		this.name = bo.name;
		this.description = bo.description;
		this.dateStart = bo.dateStart;
		this.dateEnd = bo.dateEnd;
		this.option1 = bo.option1;
		this.option2 = bo.option2;
		this.option3 = bo.option3;
		this.option4 = bo.option4;
		this.option5 = bo.option5;
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

	public Integer ejbCreate(String name, String description, Timestamp dateStart, Timestamp dateEnd, String option1,
			String option2, String option3, String option4, String option5) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(name, description, dateStart, dateEnd, option1, option2, option3, option4, option5);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.name = name;
			this.description = description;
			this.dateStart = dateStart;
			this.dateEnd = dateEnd;
			this.option1 = option1;
			this.option2 = option2;
			this.option3 = option3;
			this.option4 = option4;
			this.option5 = option5;
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

	public void ejbPostCreate(String name, String description, Timestamp dateStart, Timestamp dateEnd, String option1,
			String option2, String option3, String option4, String option5) throws CreateException
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

	public Collection ejbFindObjectsGiven(String fieldName, String value)
	{
		Log.printVerbose(strObjectName + " In ejbFindObjectsGiven");
		Collection col = selectObjectsGiven(fieldName, value);
		Log.printVerbose(strObjectName + " Leaving ejbFindObjectsGiven");
		return col;
	}

	public Vector ejbHomeGetAllValueObjects()
	{
		Log.printVerbose(strObjectName + " In ejbHomeGetAllValueObjects");
		Vector vecValObj = selectAllValueObjects();
		Log.printVerbose(strObjectName + " Leaving ejbHomeGetAllValueObjects");
		return vecValObj;
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

	private Integer insertNewRow(String name, String description, Timestamp dateStart, Timestamp dateEnd,
			String option1, String option2, String option3, String option4, String option5)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO "
					+ TABLENAME
					+ " (pkid, name, description, date_start, date_end, option1, option2, option3, option4, option5) VALUES "
					+ " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, name);
			ps.setString(3, description);
			ps.setTimestamp(4, dateStart);
			ps.setTimestamp(5, dateEnd);
			ps.setString(6, option1);
			ps.setString(7, option2);
			ps.setString(8, option3);
			ps.setString(9, option4);
			ps.setString(10, option5);
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
			String sqlStatement = "SELECT pkid, name, description, date_start, date_end, option1, option2, option3, option4, option5 FROM "
					+ TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.name = rs.getString("name");
				this.description = rs.getString("description");
				this.dateStart = rs.getTimestamp("date_start");
				this.dateEnd = rs.getTimestamp("date_end");
				this.option1 = rs.getString("option1");
				this.option2 = rs.getString("option2");
				this.option3 = rs.getString("option3");
				this.option4 = rs.getString("option4");
				this.option5 = rs.getString("option5");
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
			String sqlStatement = "UPDATE " + TABLENAME
					+ " SET name = ?, description = ?, date_start = ?, date_end = ?,"
					+ " option1 = ?, option2 = ?, option3 = ?, option4 = ?, option5 = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.name);
			ps.setString(2, this.description);
			ps.setTimestamp(3, this.dateStart);
			ps.setTimestamp(4, this.dateEnd);
			ps.setString(5, this.option1);
			ps.setString(6, this.option2);
			ps.setString(7, this.option3);
			ps.setString(8, this.option4);
			ps.setString(9, this.option5);
			ps.setInt(10, this.pkId.intValue());
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

	private Vector selectAllValueObjects()
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			String sqlStatement = "SELECT pkid, name, description, date_start, date_end, option1, option2, option3, option4, option5 FROM "
					+ TABLENAME + " ORDER BY pkid";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				BatchObject bo = new BatchObject();
				bo.pkId = new Integer(rs.getInt("pkid"));
				bo.name = rs.getString("name");
				bo.description = rs.getString("description");
				bo.dateStart = rs.getTimestamp("date_start");
				bo.dateEnd = rs.getTimestamp("date_end");
				bo.option1 = rs.getString("option1");
				bo.option2 = rs.getString("option2");
				bo.option3 = rs.getString("option3");
				bo.option4 = rs.getString("option4");
				bo.option5 = rs.getString("option5");
				vecValObj.add(bo);
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

	private Vector selectValueObjectsGiven(String fieldName1, String value1, String fieldName2, String value2)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		Vector vecValObj = new Vector();
		try
		{
			/*
			 * String sqlStatement = "SELECT pkid, name, description,
			 * date_start, " + " date_end, option1, option2, option3, option4,
			 * option5 FROM " + TABLENAME + " WHERE " + fieldName1 + " = ?";
			 * if(fieldName2 != null && value2 != null) { sqlStatement =
			 * sqlStatement + " AND " + fieldName2 + " = ?"; }
			 */
			String[] conditions = new String[] {};
			if (fieldName1 != null && value1 != null && fieldName2 == null && value2 == null)
			{
				conditions = new String[] { fieldName1 + " ='" + value1 + "' " };
			}
			if (fieldName1 != null && value1 != null && fieldName2 != null && value2 != null)
			{
				conditions = new String[] { fieldName1 + " ='" + value1 + "' ", fieldName2 + "='" + value2 + "' " };
			}
			QueryObject query = new QueryObject(conditions);
			query.setOrder(" ORDER BY " + NAME);
			String sqlStatement = " SELECT * FROM " + TABLENAME;
			sqlStatement = query.appendQuery(sqlStatement);
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			/*
			 * ps.setString(1, value1); if(fieldName2 != null && value2 != null) {
			 * ps.setString(2, value2); }
			 */
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				BatchObject bo = new BatchObject();
				bo.pkId = new Integer(rs.getInt("pkid"));
				bo.name = rs.getString("name");
				bo.description = rs.getString("description");
				bo.dateStart = rs.getTimestamp("date_start");
				bo.dateEnd = rs.getTimestamp("date_end");
				bo.option1 = rs.getString("option1");
				bo.option2 = rs.getString("option2");
				bo.option3 = rs.getString("option3");
				bo.option4 = rs.getString("option4");
				bo.option5 = rs.getString("option5");
				vecValObj.add(bo);
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
