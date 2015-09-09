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

public class GenericEntityAccountBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_generic_entity_account";
	protected final String strObjectName = "GenericEntityAccountBean: ";
	private EntityContext mContext;
	public static final String STATUS = "status";
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	// // ONE-TIME ENTITY PKID
	public static final Integer PKID_ONETIME = new Integer("500");
	private Integer pkId;
	private String accCode;
	private String name;
	private String entityType;
	private String identityNumber;
	private String entityContactPerson;
	private String add1;
	private String add2;
	private String add3;
	private String telephone;
	private String email;
	private String description;
	private String status;
	private Timestamp lastEdit;
	private Integer userIdEdit;
	private Integer accType;

	public GenericEntityAccountObject getObject()
	{
		GenericEntityAccountObject valObj = new GenericEntityAccountObject();
		valObj.pkId = this.pkId;
		valObj.accCode = this.accCode;
		valObj.name = this.name;
		valObj.entityType = this.entityType;
		valObj.identityNumber = this.identityNumber;
		valObj.entityContactPerson = this.entityContactPerson;
		valObj.add1 = this.add1;
		valObj.add2 = this.add2;
		valObj.add3 = this.add3;
		valObj.telephone = this.telephone;
		valObj.email = this.email;
		valObj.description = this.description;
		valObj.status = this.status;
		valObj.lastEdit = this.lastEdit;
		valObj.userIdEdit = this.userIdEdit;
		valObj.accType = this.accType;
		return valObj;
	}

	public void setObject(GenericEntityAccountObject valObj)
	{
		this.pkId = valObj.pkId;
		this.accCode = valObj.accCode;
		this.name = valObj.name;
		this.entityType = valObj.entityType;
		this.identityNumber = valObj.identityNumber;
		this.entityContactPerson = valObj.entityContactPerson;
		this.add1 = valObj.add1;
		this.add2 = valObj.add2;
		this.add3 = valObj.add3;
		this.telephone = valObj.telephone;
		this.email = valObj.email;
		this.description = valObj.description;
		this.status = valObj.status;
		this.lastEdit = valObj.lastEdit;
		this.userIdEdit = valObj.userIdEdit;
		this.accType = valObj.accType;
	}

	public Integer getPkId()
	{
		return this.pkId;
	}

	public String getAccCode()
	{
		return this.accCode;
	}

	public String getName()
	{
		return this.name;
	}

	public String getEntityType()
	{
		return this.entityType;
	}

	public String getIdentityNumber()
	{
		return this.identityNumber;
	}

	public String getEntityContactPerson()
	{
		return this.entityContactPerson;
	}

	public String getAdd1()
	{
		return this.add1;
	}

	public String getAdd2()
	{
		return this.add2;
	}

	public String getAdd3()
	{
		return this.add3;
	}

	public String getTelephone()
	{
		return this.telephone;
	}

	public String getEmail()
	{
		return this.email;
	}

	public String getDescription()
	{
		return this.description;
	}

	public String getStatus()
	{
		return this.status;
	}

	public Timestamp getLastEdit()
	{
		return this.lastEdit;
	}

	public Integer getUserIdEdit()
	{
		return this.userIdEdit;
	}

	public Integer getAccType()
	{
		return this.accType;
	}

	public void setPkId(Integer pkid)
	{
		this.pkId = pkid;
	}

	public void setAccCode(String accCode)
	{
		this.accCode = accCode;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setEntityType(String entityType)
	{
		this.entityType = entityType;
	}

	public void setIdentityNumber(String identityNumber)
	{
		this.identityNumber = identityNumber;
	}

	public void setEntityContactPerson(String entityContactPerson)
	{
		this.entityContactPerson = entityContactPerson;
	}

	public void setAdd1(String add1)
	{
		this.add1 = add1;
	}

	public void setAdd2(String add2)
	{
		this.add2 = add2;
	}

	public void setAdd3(String add3)
	{
		this.add3 = add3;
	}

	public void setTelephone(String telephone)
	{
		this.telephone = telephone;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public void setLastEdit(Timestamp lastEdit)
	{
		this.lastEdit = lastEdit;
	}

	public void setUserIdEdit(Integer userIdEdit)
	{
		this.userIdEdit = userIdEdit;
	}

	public void setAccType(Integer accType)
	{
		this.accType = accType;
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

	public Integer ejbCreate(String accCode, String name, String entityType, String identityNumber,
			String entityContactPerson, String add1, String add2, String add3, String telephone, String email,
			String description, Timestamp lastEdit, Integer userIdEdit, Integer accType) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(accCode, name, entityType, identityNumber, entityContactPerson, add1, add2, add3,
				telephone, email, description, lastEdit, userIdEdit, accType);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.accCode = accCode;
			this.name = name;
			this.entityType = entityType;
			this.identityNumber = identityNumber;
			this.entityContactPerson = entityContactPerson;
			this.add1 = add1;
			this.add2 = add2;
			this.add3 = add3;
			this.telephone = telephone;
			this.email = email;
			this.description = description;
			this.status = this.ACTIVE;
			this.lastEdit = lastEdit;
			this.userIdEdit = userIdEdit;
			this.accType = accType;
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

	public void ejbPostCreate(String accCode, String name, String entityType, String identityNumber,
			String entityContactPerson, String add1, String add2, String add3, String telephone, String email,
			String description, Timestamp lastEdit, Integer userIdEdit, Integer accType)
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

	public Vector ejbHomeGetValueObjectsILike(String keyword)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsILike(keyword);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
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

	private Integer insertNewRow(String accCode, String name, String entityType, String identityNumber,
			String entityContactPerson, String add1, String add2, String add3, String telephone, String email,
			String description, Timestamp lastEdit, Integer userIdEdit, Integer accType)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO "
					+ TABLENAME
					+ " (pkid, acc_code, name, entity_type, identity_number, entity_contact_person, add1, add2, add3, telephone, email, description, status,"
					+ " last_edit, userid_edit, acc_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, accCode);
			ps.setString(3, name);
			ps.setString(4, entityType);
			ps.setString(5, identityNumber);
			ps.setString(6, entityContactPerson);
			ps.setString(7, add1);
			ps.setString(8, add2);
			ps.setString(9, add3);
			ps.setString(10, telephone);
			ps.setString(11, email);
			ps.setString(12, description);
			ps.setString(13, this.ACTIVE);
			ps.setTimestamp(14, lastEdit);
			ps.setInt(15, userIdEdit.intValue());
			ps.setInt(16, accType.intValue());
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
			String sqlStatement = "SELECT pkid, acc_code, name, entity_type, identity_number, entity_contact_person, add1, add2, add3, telephone, email, description, status,"
					+ " last_edit, userid_edit, acc_type FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.accCode = rs.getString("acc_code");
				this.name = rs.getString("name");
				this.entityType = rs.getString("entity_type");
				this.identityNumber = rs.getString("identity_number");
				this.entityContactPerson = rs.getString("entity_contact_person");
				this.add1 = rs.getString("add1");
				this.add2 = rs.getString("add2");
				this.add3 = rs.getString("add3");
				this.telephone = rs.getString("telephone");
				this.email = rs.getString("email");
				this.description = rs.getString("description");
				this.status = rs.getString("status");
				this.lastEdit = rs.getTimestamp("last_edit");
				this.userIdEdit = new Integer(rs.getInt("userid_edit"));
				this.accType = new Integer(rs.getInt("acc_type"));
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

	private Vector selectValueObjectsILike(String keyword)
	{
		Vector vecValObj = new Vector();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT pkid, acc_code, name, entity_type, identity_number, entity_contact_person, add1, add2, add3, telephone, email, description, status,"
					+ " last_edit, userid_edit, acc_type FROM "
					+ TABLENAME
					+ " WHERE pkid ~* ? "
					+ " OR acc_code ~* ? "
					+ " OR name ~* ? "
					+ " OR entity_type ~* ? "
					+ " OR identity_number ~* ? "
					+ " OR entity_contact_person ~* ? "
					+ " OR add1 ~* ? "
					+ " OR add2 ~* ? "
					+ " OR add3 ~* ? "
					+ " OR telephone ~* ? "
					+ " OR email ~* ? "
					+ " OR description ~* ? "
					+ " OR acc_type ~* ? "
					+ " OR status ~* ? ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, keyword);
			ps.setString(2, keyword);
			ps.setString(3, keyword);
			ps.setString(4, keyword);
			ps.setString(5, keyword);
			ps.setString(6, keyword);
			ps.setString(7, keyword);
			ps.setString(8, keyword);
			ps.setString(9, keyword);
			ps.setString(10, keyword);
			ps.setString(11, keyword);
			ps.setString(12, keyword);
			ps.setString(13, keyword);
			ps.setString(14, keyword);
			ResultSet rs = ps.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				GenericEntityAccountObject valObj = new GenericEntityAccountObject();
				valObj.pkId = new Integer(rs.getInt("pkid"));
				valObj.accCode = rs.getString("acc_code");
				valObj.name = rs.getString("name");
				valObj.entityType = rs.getString("entity_type");
				valObj.identityNumber = rs.getString("identity_number");
				valObj.entityContactPerson = rs.getString("entity_contact_person");
				valObj.add1 = rs.getString("add1");
				valObj.add2 = rs.getString("add2");
				valObj.add3 = rs.getString("add3");
				valObj.telephone = rs.getString("telephone");
				valObj.email = rs.getString("email");
				valObj.description = rs.getString("description");
				valObj.status = rs.getString("status");
				valObj.lastEdit = rs.getTimestamp("last_edit");
				valObj.userIdEdit = new Integer(rs.getInt("userid_edit"));
				valObj.accType = new Integer(rs.getInt("acc_type"));
				vecValObj.add(valObj);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printVerbose(" ERROR : " + ex.getMessage());
			throw new EJBException(ex);
		} finally
		{
			cleanup(cn, ps);
		}
		return vecValObj;
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
					+ " SET acc_code = ?, name = ?, entity_type = ?, identity_number = ?, entity_contact_person = ?, add1 = ?, add2 = ?,"
					+ " add3 = ?, telephone = ?, email = ?, description = ?, status = ?, last_edit = ?, userid_edit = ?, acc_type = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.accCode);
			ps.setString(2, this.name);
			ps.setString(3, this.entityType);
			ps.setString(4, this.identityNumber);
			ps.setString(5, this.entityContactPerson);
			ps.setString(6, this.add1);
			ps.setString(7, this.add2);
			ps.setString(8, this.add3);
			ps.setString(9, this.telephone);
			ps.setString(10, this.email);
			ps.setString(11, this.description);
			ps.setString(12, this.status);
			ps.setTimestamp(13, this.lastEdit);
			ps.setInt(14, this.userIdEdit.intValue());
			ps.setInt(15, this.accType.intValue());
			ps.setInt(16, this.pkId.intValue());
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

public class GenericEntityAccountBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_generic_entity_account";
	protected final String strObjectName = "GenericEntityAccountBean: ";
	private EntityContext mContext;
	public static final String STATUS = "status";
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	// // ONE-TIME ENTITY PKID
	public static final Integer PKID_ONETIME = new Integer("500");
	private Integer pkId;
	private String accCode;
	private String name;
	private String entityType;
	private String identityNumber;
	private String entityContactPerson;
	private String add1;
	private String add2;
	private String add3;
	private String telephone;
	private String email;
	private String description;
	private String status;
	private Timestamp lastEdit;
	private Integer userIdEdit;
	private Integer accType;

	public GenericEntityAccountObject getObject()
	{
		GenericEntityAccountObject valObj = new GenericEntityAccountObject();
		valObj.pkId = this.pkId;
		valObj.accCode = this.accCode;
		valObj.name = this.name;
		valObj.entityType = this.entityType;
		valObj.identityNumber = this.identityNumber;
		valObj.entityContactPerson = this.entityContactPerson;
		valObj.add1 = this.add1;
		valObj.add2 = this.add2;
		valObj.add3 = this.add3;
		valObj.telephone = this.telephone;
		valObj.email = this.email;
		valObj.description = this.description;
		valObj.status = this.status;
		valObj.lastEdit = this.lastEdit;
		valObj.userIdEdit = this.userIdEdit;
		valObj.accType = this.accType;
		return valObj;
	}

	public void setObject(GenericEntityAccountObject valObj)
	{
		this.pkId = valObj.pkId;
		this.accCode = valObj.accCode;
		this.name = valObj.name;
		this.entityType = valObj.entityType;
		this.identityNumber = valObj.identityNumber;
		this.entityContactPerson = valObj.entityContactPerson;
		this.add1 = valObj.add1;
		this.add2 = valObj.add2;
		this.add3 = valObj.add3;
		this.telephone = valObj.telephone;
		this.email = valObj.email;
		this.description = valObj.description;
		this.status = valObj.status;
		this.lastEdit = valObj.lastEdit;
		this.userIdEdit = valObj.userIdEdit;
		this.accType = valObj.accType;
	}

	public Integer getPkId()
	{
		return this.pkId;
	}

	public String getAccCode()
	{
		return this.accCode;
	}

	public String getName()
	{
		return this.name;
	}

	public String getEntityType()
	{
		return this.entityType;
	}

	public String getIdentityNumber()
	{
		return this.identityNumber;
	}

	public String getEntityContactPerson()
	{
		return this.entityContactPerson;
	}

	public String getAdd1()
	{
		return this.add1;
	}

	public String getAdd2()
	{
		return this.add2;
	}

	public String getAdd3()
	{
		return this.add3;
	}

	public String getTelephone()
	{
		return this.telephone;
	}

	public String getEmail()
	{
		return this.email;
	}

	public String getDescription()
	{
		return this.description;
	}

	public String getStatus()
	{
		return this.status;
	}

	public Timestamp getLastEdit()
	{
		return this.lastEdit;
	}

	public Integer getUserIdEdit()
	{
		return this.userIdEdit;
	}

	public Integer getAccType()
	{
		return this.accType;
	}

	public void setPkId(Integer pkid)
	{
		this.pkId = pkid;
	}

	public void setAccCode(String accCode)
	{
		this.accCode = accCode;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setEntityType(String entityType)
	{
		this.entityType = entityType;
	}

	public void setIdentityNumber(String identityNumber)
	{
		this.identityNumber = identityNumber;
	}

	public void setEntityContactPerson(String entityContactPerson)
	{
		this.entityContactPerson = entityContactPerson;
	}

	public void setAdd1(String add1)
	{
		this.add1 = add1;
	}

	public void setAdd2(String add2)
	{
		this.add2 = add2;
	}

	public void setAdd3(String add3)
	{
		this.add3 = add3;
	}

	public void setTelephone(String telephone)
	{
		this.telephone = telephone;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public void setLastEdit(Timestamp lastEdit)
	{
		this.lastEdit = lastEdit;
	}

	public void setUserIdEdit(Integer userIdEdit)
	{
		this.userIdEdit = userIdEdit;
	}

	public void setAccType(Integer accType)
	{
		this.accType = accType;
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

	public Integer ejbCreate(String accCode, String name, String entityType, String identityNumber,
			String entityContactPerson, String add1, String add2, String add3, String telephone, String email,
			String description, Timestamp lastEdit, Integer userIdEdit, Integer accType) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(accCode, name, entityType, identityNumber, entityContactPerson, add1, add2, add3,
				telephone, email, description, lastEdit, userIdEdit, accType);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.accCode = accCode;
			this.name = name;
			this.entityType = entityType;
			this.identityNumber = identityNumber;
			this.entityContactPerson = entityContactPerson;
			this.add1 = add1;
			this.add2 = add2;
			this.add3 = add3;
			this.telephone = telephone;
			this.email = email;
			this.description = description;
			this.status = this.ACTIVE;
			this.lastEdit = lastEdit;
			this.userIdEdit = userIdEdit;
			this.accType = accType;
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

	public void ejbPostCreate(String accCode, String name, String entityType, String identityNumber,
			String entityContactPerson, String add1, String add2, String add3, String telephone, String email,
			String description, Timestamp lastEdit, Integer userIdEdit, Integer accType)
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

	public Vector ejbHomeGetValueObjectsILike(String keyword)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsILike(keyword);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
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

	private Integer insertNewRow(String accCode, String name, String entityType, String identityNumber,
			String entityContactPerson, String add1, String add2, String add3, String telephone, String email,
			String description, Timestamp lastEdit, Integer userIdEdit, Integer accType)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO "
					+ TABLENAME
					+ " (pkid, acc_code, name, entity_type, identity_number, entity_contact_person, add1, add2, add3, telephone, email, description, status,"
					+ " last_edit, userid_edit, acc_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, accCode);
			ps.setString(3, name);
			ps.setString(4, entityType);
			ps.setString(5, identityNumber);
			ps.setString(6, entityContactPerson);
			ps.setString(7, add1);
			ps.setString(8, add2);
			ps.setString(9, add3);
			ps.setString(10, telephone);
			ps.setString(11, email);
			ps.setString(12, description);
			ps.setString(13, this.ACTIVE);
			ps.setTimestamp(14, lastEdit);
			ps.setInt(15, userIdEdit.intValue());
			ps.setInt(16, accType.intValue());
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
			String sqlStatement = "SELECT pkid, acc_code, name, entity_type, identity_number, entity_contact_person, add1, add2, add3, telephone, email, description, status,"
					+ " last_edit, userid_edit, acc_type FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.accCode = rs.getString("acc_code");
				this.name = rs.getString("name");
				this.entityType = rs.getString("entity_type");
				this.identityNumber = rs.getString("identity_number");
				this.entityContactPerson = rs.getString("entity_contact_person");
				this.add1 = rs.getString("add1");
				this.add2 = rs.getString("add2");
				this.add3 = rs.getString("add3");
				this.telephone = rs.getString("telephone");
				this.email = rs.getString("email");
				this.description = rs.getString("description");
				this.status = rs.getString("status");
				this.lastEdit = rs.getTimestamp("last_edit");
				this.userIdEdit = new Integer(rs.getInt("userid_edit"));
				this.accType = new Integer(rs.getInt("acc_type"));
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

	private Vector selectValueObjectsILike(String keyword)
	{
		Vector vecValObj = new Vector();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT pkid, acc_code, name, entity_type, identity_number, entity_contact_person, add1, add2, add3, telephone, email, description, status,"
					+ " last_edit, userid_edit, acc_type FROM "
					+ TABLENAME
					+ " WHERE pkid ~* ? "
					+ " OR acc_code ~* ? "
					+ " OR name ~* ? "
					+ " OR entity_type ~* ? "
					+ " OR identity_number ~* ? "
					+ " OR entity_contact_person ~* ? "
					+ " OR add1 ~* ? "
					+ " OR add2 ~* ? "
					+ " OR add3 ~* ? "
					+ " OR telephone ~* ? "
					+ " OR email ~* ? "
					+ " OR description ~* ? "
					+ " OR acc_type ~* ? "
					+ " OR status ~* ? ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, keyword);
			ps.setString(2, keyword);
			ps.setString(3, keyword);
			ps.setString(4, keyword);
			ps.setString(5, keyword);
			ps.setString(6, keyword);
			ps.setString(7, keyword);
			ps.setString(8, keyword);
			ps.setString(9, keyword);
			ps.setString(10, keyword);
			ps.setString(11, keyword);
			ps.setString(12, keyword);
			ps.setString(13, keyword);
			ps.setString(14, keyword);
			ResultSet rs = ps.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				GenericEntityAccountObject valObj = new GenericEntityAccountObject();
				valObj.pkId = new Integer(rs.getInt("pkid"));
				valObj.accCode = rs.getString("acc_code");
				valObj.name = rs.getString("name");
				valObj.entityType = rs.getString("entity_type");
				valObj.identityNumber = rs.getString("identity_number");
				valObj.entityContactPerson = rs.getString("entity_contact_person");
				valObj.add1 = rs.getString("add1");
				valObj.add2 = rs.getString("add2");
				valObj.add3 = rs.getString("add3");
				valObj.telephone = rs.getString("telephone");
				valObj.email = rs.getString("email");
				valObj.description = rs.getString("description");
				valObj.status = rs.getString("status");
				valObj.lastEdit = rs.getTimestamp("last_edit");
				valObj.userIdEdit = new Integer(rs.getInt("userid_edit"));
				valObj.accType = new Integer(rs.getInt("acc_type"));
				vecValObj.add(valObj);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printVerbose(" ERROR : " + ex.getMessage());
			throw new EJBException(ex);
		} finally
		{
			cleanup(cn, ps);
		}
		return vecValObj;
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
					+ " SET acc_code = ?, name = ?, entity_type = ?, identity_number = ?, entity_contact_person = ?, add1 = ?, add2 = ?,"
					+ " add3 = ?, telephone = ?, email = ?, description = ?, status = ?, last_edit = ?, userid_edit = ?, acc_type = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.accCode);
			ps.setString(2, this.name);
			ps.setString(3, this.entityType);
			ps.setString(4, this.identityNumber);
			ps.setString(5, this.entityContactPerson);
			ps.setString(6, this.add1);
			ps.setString(7, this.add2);
			ps.setString(8, this.add3);
			ps.setString(9, this.telephone);
			ps.setString(10, this.email);
			ps.setString(11, this.description);
			ps.setString(12, this.status);
			ps.setTimestamp(13, this.lastEdit);
			ps.setInt(14, this.userIdEdit.intValue());
			ps.setInt(15, this.accType.intValue());
			ps.setInt(16, this.pkId.intValue());
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

public class GenericEntityAccountBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_generic_entity_account";
	protected final String strObjectName = "GenericEntityAccountBean: ";
	private EntityContext mContext;
	public static final String STATUS = "status";
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	// // ONE-TIME ENTITY PKID
	public static final Integer PKID_ONETIME = new Integer("500");
	private Integer pkId;
	private String accCode;
	private String name;
	private String entityType;
	private String identityNumber;
	private String entityContactPerson;
	private String add1;
	private String add2;
	private String add3;
	private String telephone;
	private String email;
	private String description;
	private String status;
	private Timestamp lastEdit;
	private Integer userIdEdit;
	private Integer accType;

	public GenericEntityAccountObject getObject()
	{
		GenericEntityAccountObject valObj = new GenericEntityAccountObject();
		valObj.pkId = this.pkId;
		valObj.accCode = this.accCode;
		valObj.name = this.name;
		valObj.entityType = this.entityType;
		valObj.identityNumber = this.identityNumber;
		valObj.entityContactPerson = this.entityContactPerson;
		valObj.add1 = this.add1;
		valObj.add2 = this.add2;
		valObj.add3 = this.add3;
		valObj.telephone = this.telephone;
		valObj.email = this.email;
		valObj.description = this.description;
		valObj.status = this.status;
		valObj.lastEdit = this.lastEdit;
		valObj.userIdEdit = this.userIdEdit;
		valObj.accType = this.accType;
		return valObj;
	}

	public void setObject(GenericEntityAccountObject valObj)
	{
		this.pkId = valObj.pkId;
		this.accCode = valObj.accCode;
		this.name = valObj.name;
		this.entityType = valObj.entityType;
		this.identityNumber = valObj.identityNumber;
		this.entityContactPerson = valObj.entityContactPerson;
		this.add1 = valObj.add1;
		this.add2 = valObj.add2;
		this.add3 = valObj.add3;
		this.telephone = valObj.telephone;
		this.email = valObj.email;
		this.description = valObj.description;
		this.status = valObj.status;
		this.lastEdit = valObj.lastEdit;
		this.userIdEdit = valObj.userIdEdit;
		this.accType = valObj.accType;
	}

	public Integer getPkId()
	{
		return this.pkId;
	}

	public String getAccCode()
	{
		return this.accCode;
	}

	public String getName()
	{
		return this.name;
	}

	public String getEntityType()
	{
		return this.entityType;
	}

	public String getIdentityNumber()
	{
		return this.identityNumber;
	}

	public String getEntityContactPerson()
	{
		return this.entityContactPerson;
	}

	public String getAdd1()
	{
		return this.add1;
	}

	public String getAdd2()
	{
		return this.add2;
	}

	public String getAdd3()
	{
		return this.add3;
	}

	public String getTelephone()
	{
		return this.telephone;
	}

	public String getEmail()
	{
		return this.email;
	}

	public String getDescription()
	{
		return this.description;
	}

	public String getStatus()
	{
		return this.status;
	}

	public Timestamp getLastEdit()
	{
		return this.lastEdit;
	}

	public Integer getUserIdEdit()
	{
		return this.userIdEdit;
	}

	public Integer getAccType()
	{
		return this.accType;
	}

	public void setPkId(Integer pkid)
	{
		this.pkId = pkid;
	}

	public void setAccCode(String accCode)
	{
		this.accCode = accCode;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setEntityType(String entityType)
	{
		this.entityType = entityType;
	}

	public void setIdentityNumber(String identityNumber)
	{
		this.identityNumber = identityNumber;
	}

	public void setEntityContactPerson(String entityContactPerson)
	{
		this.entityContactPerson = entityContactPerson;
	}

	public void setAdd1(String add1)
	{
		this.add1 = add1;
	}

	public void setAdd2(String add2)
	{
		this.add2 = add2;
	}

	public void setAdd3(String add3)
	{
		this.add3 = add3;
	}

	public void setTelephone(String telephone)
	{
		this.telephone = telephone;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public void setLastEdit(Timestamp lastEdit)
	{
		this.lastEdit = lastEdit;
	}

	public void setUserIdEdit(Integer userIdEdit)
	{
		this.userIdEdit = userIdEdit;
	}

	public void setAccType(Integer accType)
	{
		this.accType = accType;
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

	public Integer ejbCreate(String accCode, String name, String entityType, String identityNumber,
			String entityContactPerson, String add1, String add2, String add3, String telephone, String email,
			String description, Timestamp lastEdit, Integer userIdEdit, Integer accType) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(accCode, name, entityType, identityNumber, entityContactPerson, add1, add2, add3,
				telephone, email, description, lastEdit, userIdEdit, accType);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.accCode = accCode;
			this.name = name;
			this.entityType = entityType;
			this.identityNumber = identityNumber;
			this.entityContactPerson = entityContactPerson;
			this.add1 = add1;
			this.add2 = add2;
			this.add3 = add3;
			this.telephone = telephone;
			this.email = email;
			this.description = description;
			this.status = this.ACTIVE;
			this.lastEdit = lastEdit;
			this.userIdEdit = userIdEdit;
			this.accType = accType;
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

	public void ejbPostCreate(String accCode, String name, String entityType, String identityNumber,
			String entityContactPerson, String add1, String add2, String add3, String telephone, String email,
			String description, Timestamp lastEdit, Integer userIdEdit, Integer accType)
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

	public Vector ejbHomeGetValueObjectsILike(String keyword)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsILike(keyword);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
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

	private Integer insertNewRow(String accCode, String name, String entityType, String identityNumber,
			String entityContactPerson, String add1, String add2, String add3, String telephone, String email,
			String description, Timestamp lastEdit, Integer userIdEdit, Integer accType)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO "
					+ TABLENAME
					+ " (pkid, acc_code, name, entity_type, identity_number, entity_contact_person, add1, add2, add3, telephone, email, description, status,"
					+ " last_edit, userid_edit, acc_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, accCode);
			ps.setString(3, name);
			ps.setString(4, entityType);
			ps.setString(5, identityNumber);
			ps.setString(6, entityContactPerson);
			ps.setString(7, add1);
			ps.setString(8, add2);
			ps.setString(9, add3);
			ps.setString(10, telephone);
			ps.setString(11, email);
			ps.setString(12, description);
			ps.setString(13, this.ACTIVE);
			ps.setTimestamp(14, lastEdit);
			ps.setInt(15, userIdEdit.intValue());
			ps.setInt(16, accType.intValue());
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
			String sqlStatement = "SELECT pkid, acc_code, name, entity_type, identity_number, entity_contact_person, add1, add2, add3, telephone, email, description, status,"
					+ " last_edit, userid_edit, acc_type FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.accCode = rs.getString("acc_code");
				this.name = rs.getString("name");
				this.entityType = rs.getString("entity_type");
				this.identityNumber = rs.getString("identity_number");
				this.entityContactPerson = rs.getString("entity_contact_person");
				this.add1 = rs.getString("add1");
				this.add2 = rs.getString("add2");
				this.add3 = rs.getString("add3");
				this.telephone = rs.getString("telephone");
				this.email = rs.getString("email");
				this.description = rs.getString("description");
				this.status = rs.getString("status");
				this.lastEdit = rs.getTimestamp("last_edit");
				this.userIdEdit = new Integer(rs.getInt("userid_edit"));
				this.accType = new Integer(rs.getInt("acc_type"));
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

	private Vector selectValueObjectsILike(String keyword)
	{
		Vector vecValObj = new Vector();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT pkid, acc_code, name, entity_type, identity_number, entity_contact_person, add1, add2, add3, telephone, email, description, status,"
					+ " last_edit, userid_edit, acc_type FROM "
					+ TABLENAME
					+ " WHERE pkid ~* ? "
					+ " OR acc_code ~* ? "
					+ " OR name ~* ? "
					+ " OR entity_type ~* ? "
					+ " OR identity_number ~* ? "
					+ " OR entity_contact_person ~* ? "
					+ " OR add1 ~* ? "
					+ " OR add2 ~* ? "
					+ " OR add3 ~* ? "
					+ " OR telephone ~* ? "
					+ " OR email ~* ? "
					+ " OR description ~* ? "
					+ " OR acc_type ~* ? "
					+ " OR status ~* ? ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, keyword);
			ps.setString(2, keyword);
			ps.setString(3, keyword);
			ps.setString(4, keyword);
			ps.setString(5, keyword);
			ps.setString(6, keyword);
			ps.setString(7, keyword);
			ps.setString(8, keyword);
			ps.setString(9, keyword);
			ps.setString(10, keyword);
			ps.setString(11, keyword);
			ps.setString(12, keyword);
			ps.setString(13, keyword);
			ps.setString(14, keyword);
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			while (rs.next())
			{
				GenericEntityAccountObject valObj = new GenericEntityAccountObject();
				valObj.pkId = new Integer(rs.getInt("pkid"));
				valObj.accCode = rs.getString("acc_code");
				valObj.name = rs.getString("name");
				valObj.entityType = rs.getString("entity_type");
				valObj.identityNumber = rs.getString("identity_number");
				valObj.entityContactPerson = rs.getString("entity_contact_person");
				valObj.add1 = rs.getString("add1");
				valObj.add2 = rs.getString("add2");
				valObj.add3 = rs.getString("add3");
				valObj.telephone = rs.getString("telephone");
				valObj.email = rs.getString("email");
				valObj.description = rs.getString("description");
				valObj.status = rs.getString("status");
				valObj.lastEdit = rs.getTimestamp("last_edit");
				valObj.userIdEdit = new Integer(rs.getInt("userid_edit"));
				valObj.accType = new Integer(rs.getInt("acc_type"));
				vecValObj.add(valObj);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printVerbose(" ERROR : " + ex.getMessage());
			throw new EJBException(ex);
		} finally
		{
			cleanup(cn, ps);
		}
		return vecValObj;
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
					+ " SET acc_code = ?, name = ?, entity_type = ?, identity_number = ?, entity_contact_person = ?, add1 = ?, add2 = ?,"
					+ " add3 = ?, telephone = ?, email = ?, description = ?, status = ?, last_edit = ?, userid_edit = ?, acc_type = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.accCode);
			ps.setString(2, this.name);
			ps.setString(3, this.entityType);
			ps.setString(4, this.identityNumber);
			ps.setString(5, this.entityContactPerson);
			ps.setString(6, this.add1);
			ps.setString(7, this.add2);
			ps.setString(8, this.add3);
			ps.setString(9, this.telephone);
			ps.setString(10, this.email);
			ps.setString(11, this.description);
			ps.setString(12, this.status);
			ps.setTimestamp(13, this.lastEdit);
			ps.setInt(14, this.userIdEdit.intValue());
			ps.setInt(15, this.accType.intValue());
			ps.setInt(16, this.pkId.intValue());
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

public class GenericEntityAccountBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	public static final String TABLENAME = "acc_generic_entity_account";
	protected final String strObjectName = "GenericEntityAccountBean: ";
	private EntityContext mContext;
	public static final String STATUS = "status";
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	// // ONE-TIME ENTITY PKID
	public static final Integer PKID_ONETIME = new Integer("500");
	private Integer pkId;
	private String accCode;
	private String name;
	private String entityType;
	private String identityNumber;
	private String entityContactPerson;
	private String add1;
	private String add2;
	private String add3;
	private String telephone;
	private String email;
	private String description;
	private String status;
	private Timestamp lastEdit;
	private Integer userIdEdit;
	private Integer accType;

	public GenericEntityAccountObject getObject()
	{
		GenericEntityAccountObject valObj = new GenericEntityAccountObject();
		valObj.pkId = this.pkId;
		valObj.accCode = this.accCode;
		valObj.name = this.name;
		valObj.entityType = this.entityType;
		valObj.identityNumber = this.identityNumber;
		valObj.entityContactPerson = this.entityContactPerson;
		valObj.add1 = this.add1;
		valObj.add2 = this.add2;
		valObj.add3 = this.add3;
		valObj.telephone = this.telephone;
		valObj.email = this.email;
		valObj.description = this.description;
		valObj.status = this.status;
		valObj.lastEdit = this.lastEdit;
		valObj.userIdEdit = this.userIdEdit;
		valObj.accType = this.accType;
		return valObj;
	}

	public void setObject(GenericEntityAccountObject valObj)
	{
		this.pkId = valObj.pkId;
		this.accCode = valObj.accCode;
		this.name = valObj.name;
		this.entityType = valObj.entityType;
		this.identityNumber = valObj.identityNumber;
		this.entityContactPerson = valObj.entityContactPerson;
		this.add1 = valObj.add1;
		this.add2 = valObj.add2;
		this.add3 = valObj.add3;
		this.telephone = valObj.telephone;
		this.email = valObj.email;
		this.description = valObj.description;
		this.status = valObj.status;
		this.lastEdit = valObj.lastEdit;
		this.userIdEdit = valObj.userIdEdit;
		this.accType = valObj.accType;
	}

	public Integer getPkId()
	{
		return this.pkId;
	}

	public String getAccCode()
	{
		return this.accCode;
	}

	public String getName()
	{
		return this.name;
	}

	public String getEntityType()
	{
		return this.entityType;
	}

	public String getIdentityNumber()
	{
		return this.identityNumber;
	}

	public String getEntityContactPerson()
	{
		return this.entityContactPerson;
	}

	public String getAdd1()
	{
		return this.add1;
	}

	public String getAdd2()
	{
		return this.add2;
	}

	public String getAdd3()
	{
		return this.add3;
	}

	public String getTelephone()
	{
		return this.telephone;
	}

	public String getEmail()
	{
		return this.email;
	}

	public String getDescription()
	{
		return this.description;
	}

	public String getStatus()
	{
		return this.status;
	}

	public Timestamp getLastEdit()
	{
		return this.lastEdit;
	}

	public Integer getUserIdEdit()
	{
		return this.userIdEdit;
	}

	public Integer getAccType()
	{
		return this.accType;
	}

	public void setPkId(Integer pkid)
	{
		this.pkId = pkid;
	}

	public void setAccCode(String accCode)
	{
		this.accCode = accCode;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setEntityType(String entityType)
	{
		this.entityType = entityType;
	}

	public void setIdentityNumber(String identityNumber)
	{
		this.identityNumber = identityNumber;
	}

	public void setEntityContactPerson(String entityContactPerson)
	{
		this.entityContactPerson = entityContactPerson;
	}

	public void setAdd1(String add1)
	{
		this.add1 = add1;
	}

	public void setAdd2(String add2)
	{
		this.add2 = add2;
	}

	public void setAdd3(String add3)
	{
		this.add3 = add3;
	}

	public void setTelephone(String telephone)
	{
		this.telephone = telephone;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public void setLastEdit(Timestamp lastEdit)
	{
		this.lastEdit = lastEdit;
	}

	public void setUserIdEdit(Integer userIdEdit)
	{
		this.userIdEdit = userIdEdit;
	}

	public void setAccType(Integer accType)
	{
		this.accType = accType;
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

	public Integer ejbCreate(String accCode, String name, String entityType, String identityNumber,
			String entityContactPerson, String add1, String add2, String add3, String telephone, String email,
			String description, Timestamp lastEdit, Integer userIdEdit, Integer accType) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(accCode, name, entityType, identityNumber, entityContactPerson, add1, add2, add3,
				telephone, email, description, lastEdit, userIdEdit, accType);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.accCode = accCode;
			this.name = name;
			this.entityType = entityType;
			this.identityNumber = identityNumber;
			this.entityContactPerson = entityContactPerson;
			this.add1 = add1;
			this.add2 = add2;
			this.add3 = add3;
			this.telephone = telephone;
			this.email = email;
			this.description = description;
			this.status = this.ACTIVE;
			this.lastEdit = lastEdit;
			this.userIdEdit = userIdEdit;
			this.accType = accType;
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

	public void ejbPostCreate(String accCode, String name, String entityType, String identityNumber,
			String entityContactPerson, String add1, String add2, String add3, String telephone, String email,
			String description, Timestamp lastEdit, Integer userIdEdit, Integer accType)
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

	public Vector ejbHomeGetValueObjectsILike(String keyword)
	{
		Vector vecValObj = null;
		try
		{
			vecValObj = selectValueObjectsILike(keyword);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
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

	private Integer insertNewRow(String accCode, String name, String entityType, String identityNumber,
			String entityContactPerson, String add1, String add2, String add3, String telephone, String email,
			String description, Timestamp lastEdit, Integer userIdEdit, Integer accType)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO "
					+ TABLENAME
					+ " (pkid, acc_code, name, entity_type, identity_number, entity_contact_person, add1, add2, add3, telephone, email, description, status,"
					+ " last_edit, userid_edit, acc_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, accCode);
			ps.setString(3, name);
			ps.setString(4, entityType);
			ps.setString(5, identityNumber);
			ps.setString(6, entityContactPerson);
			ps.setString(7, add1);
			ps.setString(8, add2);
			ps.setString(9, add3);
			ps.setString(10, telephone);
			ps.setString(11, email);
			ps.setString(12, description);
			ps.setString(13, this.ACTIVE);
			ps.setTimestamp(14, lastEdit);
			ps.setInt(15, userIdEdit.intValue());
			ps.setInt(16, accType.intValue());
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
			String sqlStatement = "SELECT pkid, acc_code, name, entity_type, identity_number, entity_contact_person, add1, add2, add3, telephone, email, description, status,"
					+ " last_edit, userid_edit, acc_type FROM " + TABLENAME + " WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.accCode = rs.getString("acc_code");
				this.name = rs.getString("name");
				this.entityType = rs.getString("entity_type");
				this.identityNumber = rs.getString("identity_number");
				this.entityContactPerson = rs.getString("entity_contact_person");
				this.add1 = rs.getString("add1");
				this.add2 = rs.getString("add2");
				this.add3 = rs.getString("add3");
				this.telephone = rs.getString("telephone");
				this.email = rs.getString("email");
				this.description = rs.getString("description");
				this.status = rs.getString("status");
				this.lastEdit = rs.getTimestamp("last_edit");
				this.userIdEdit = new Integer(rs.getInt("userid_edit"));
				this.accType = new Integer(rs.getInt("acc_type"));
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

	private Vector selectValueObjectsILike(String keyword)
	{
		Vector vecValObj = new Vector();
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			String sqlStatement = "SELECT pkid, acc_code, name, entity_type, identity_number, entity_contact_person, add1, add2, add3, telephone, email, description, status,"
					+ " last_edit, userid_edit, acc_type FROM "
					+ TABLENAME
					+ " WHERE pkid ~* ? "
					+ " OR acc_code ~* ? "
					+ " OR name ~* ? "
					+ " OR entity_type ~* ? "
					+ " OR identity_number ~* ? "
					+ " OR entity_contact_person ~* ? "
					+ " OR add1 ~* ? "
					+ " OR add2 ~* ? "
					+ " OR add3 ~* ? "
					+ " OR telephone ~* ? "
					+ " OR email ~* ? "
					+ " OR description ~* ? "
					+ " OR acc_type ~* ? "
					+ " OR status ~* ? ";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, keyword);
			ps.setString(2, keyword);
			ps.setString(3, keyword);
			ps.setString(4, keyword);
			ps.setString(5, keyword);
			ps.setString(6, keyword);
			ps.setString(7, keyword);
			ps.setString(8, keyword);
			ps.setString(9, keyword);
			ps.setString(10, keyword);
			ps.setString(11, keyword);
			ps.setString(12, keyword);
			ps.setString(13, keyword);
			ps.setString(14, keyword);
			ResultSet rs = ps.executeQuery();
			//rs.beforeTheFirstRecord();
			while (rs.next())
			{
				GenericEntityAccountObject valObj = new GenericEntityAccountObject();
				valObj.pkId = new Integer(rs.getInt("pkid"));
				valObj.accCode = rs.getString("acc_code");
				valObj.name = rs.getString("name");
				valObj.entityType = rs.getString("entity_type");
				valObj.identityNumber = rs.getString("identity_number");
				valObj.entityContactPerson = rs.getString("entity_contact_person");
				valObj.add1 = rs.getString("add1");
				valObj.add2 = rs.getString("add2");
				valObj.add3 = rs.getString("add3");
				valObj.telephone = rs.getString("telephone");
				valObj.email = rs.getString("email");
				valObj.description = rs.getString("description");
				valObj.status = rs.getString("status");
				valObj.lastEdit = rs.getTimestamp("last_edit");
				valObj.userIdEdit = new Integer(rs.getInt("userid_edit"));
				valObj.accType = new Integer(rs.getInt("acc_type"));
				vecValObj.add(valObj);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
			Log.printVerbose(" ERROR : " + ex.getMessage());
			throw new EJBException(ex);
		} finally
		{
			cleanup(cn, ps);
		}
		return vecValObj;
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
					+ " SET acc_code = ?, name = ?, entity_type = ?, identity_number = ?, entity_contact_person = ?, add1 = ?, add2 = ?,"
					+ " add3 = ?, telephone = ?, email = ?, description = ?, status = ?, last_edit = ?, userid_edit = ?, acc_type = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.accCode);
			ps.setString(2, this.name);
			ps.setString(3, this.entityType);
			ps.setString(4, this.identityNumber);
			ps.setString(5, this.entityContactPerson);
			ps.setString(6, this.add1);
			ps.setString(7, this.add2);
			ps.setString(8, this.add3);
			ps.setString(9, this.telephone);
			ps.setString(10, this.email);
			ps.setString(11, this.description);
			ps.setString(12, this.status);
			ps.setTimestamp(13, this.lastEdit);
			ps.setInt(14, this.userIdEdit.intValue());
			ps.setInt(15, this.accType.intValue());
			ps.setInt(16, this.pkId.intValue());
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
