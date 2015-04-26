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
import java.math.*;
import com.vlee.local.*;
import com.vlee.util.*;

public class BankAccountBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String TABLENAME = "acc_bank_account";
	protected final String strObjectName = "BankAccountBean: ";
	private EntityContext mContext;
	public static final String STATUS = "status";
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	public static final String STATUS_DELETED = "deleted";
	private Integer pkId;
	private String bankCode;
	private String bankName;
	private String accountNumber;
	private String currency;
	private BigDecimal overdraftLimit;
	private String signatory1;
	private String signatory2;
	private String signatory3;
	private String signatory4;
	private String signatory5;
	private byte[] signature;
	private Integer pcCenter;
	private String add1;
	private String add2;
	private String add3;
	private String state;
	private String country;
	private String phone;
	private String contactPerson;
	private String fax;
	private Integer userIdCreate;
	private Integer userIdUpdate;
	private Timestamp createTime;
	private Timestamp lastUpdate;
	private String status;

	public Integer getPkId()
	{
		return this.pkId;
	}

	public String getBankCode()
	{
		return this.bankCode;
	}

	public String getBankName()
	{
		return this.bankName;
	}

	public String getAccountNumber()
	{
		return this.accountNumber;
	}

	public String getCurrency()
	{
		return this.currency;
	}

	public BigDecimal getOverdraftLimit()
	{
		return this.overdraftLimit;
	}

	public String getSignatory1()
	{
		return this.signatory1;
	}

	public String getSignatory2()
	{
		return this.signatory2;
	}

	public String getSignatory3()
	{
		return this.signatory3;
	}

	public String getSignatory4()
	{
		return this.signatory4;
	}

	public String getSignatory5()
	{
		return this.signatory5;
	}

	public byte[] getSignature()
	{
		return this.signature;
	}

	public Integer getPCCenter()
	{
		return this.pcCenter;
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

	public String getState()
	{
		return this.state;
	}

	public String getCountry()
	{
		return this.country;
	}

	public String getPhone()
	{
		return this.phone;
	}

	public String getContactPerson()
	{
		return this.contactPerson;
	}

	public String getFax()
	{
		return this.fax;
	}

	public Integer getUserIdCreate()
	{
		return this.userIdCreate;
	}

	public Integer getUserIdUpdate()
	{
		return this.userIdUpdate;
	}

	public Timestamp getCreateTime()
	{
		return this.createTime;
	}

	public Timestamp getLastUpdate()
	{
		return this.lastUpdate;
	}

	public String getStatus()
	{
		return this.status;
	}

	public void setPkId(Integer pkid)
	{
		this.pkId = pkid;
	}

	public void setBankCode(String bc)
	{
		this.bankCode = bc;
	}

	public void setBankName(String bn)
	{
		this.bankName = bn;
	}

	public void setAccountNumber(String an)
	{
		this.accountNumber = an;
	}

	public void setCurrency(String ccy)
	{
		this.currency = ccy;
	}

	public void setOverdraftLimit(BigDecimal ol)
	{
		this.overdraftLimit = ol;
	}

	public void setSignatory1(String s1)
	{
		this.signatory1 = s1;
	}

	public void setSignatory2(String s2)
	{
		this.signatory2 = s2;
	}

	public void setSignatory3(String s3)
	{
		this.signatory3 = s3;
	}

	public void setSignatory4(String s4)
	{
		this.signatory4 = s4;
	}

	public void setSignatory5(String s5)
	{
		this.signatory5 = s5;
	}

	public void setSignature(byte[] s)
	{
		this.signature = s;
	}

	public void setPCCenter(Integer be)
	{
		this.pcCenter = be;
	}

	public void setAdd1(String a1)
	{
		this.add1 = a1;
	}

	public void setAdd2(String a2)
	{
		this.add2 = a2;
	}

	public void setAdd3(String a3)
	{
		this.add3 = a3;
	}

	public void setState(String st)
	{
		this.state = st;
	}

	public void setCountry(String c)
	{
		this.country = c;
	}

	public void setPhone(String p)
	{
		this.phone = p;
	}

	public void setContactPerson(String cp)
	{
		this.contactPerson = cp;
	}

	public void setFax(String f)
	{
		this.fax = f;
	}

	public void setUserIdCreate(Integer uc)
	{
		this.userIdCreate = uc;
	}

	public void setUserIdUpdate(Integer uu)
	{
		this.userIdUpdate = uu;
	}

	public void setCreateTime(Timestamp ct)
	{
		this.createTime = ct;
	}

	public void setLastUpdate(Timestamp lu)
	{
		this.lastUpdate = lu;
	}

	public void setStatus(String status)
	{
		this.status = status;
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

	public Integer ejbCreate(String bankCode, String bankName, String accountNumber, String currency,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			String signatory5, byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state,
			String country, String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(bankCode, bankName, accountNumber, currency, overdraftLimit, signatory1, signatory2,
				signatory3, signatory4, signatory5, signature, pcCenter, add1, add2, add3, state, country, phone,
				contactPerson, fax, userIdCreate, userIdUpdate, createTime, lastUpdate);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.bankCode = bankCode;
			this.bankName = bankName;
			this.accountNumber = accountNumber;
			this.currency = currency;
			this.overdraftLimit = overdraftLimit;
			this.signatory1 = signatory1;
			this.signatory2 = signatory2;
			this.signatory3 = signatory3;
			this.signatory4 = signatory4;
			this.signatory5 = signatory5;
			this.signature = signature;
			this.pcCenter = pcCenter;
			this.add1 = add1;
			this.add2 = add2;
			this.add3 = add3;
			this.state = state;
			this.country = country;
			this.phone = phone;
			this.contactPerson = contactPerson;
			this.fax = fax;
			this.userIdCreate = userIdCreate;
			this.userIdUpdate = userIdUpdate;
			this.createTime = createTime;
			this.lastUpdate = lastUpdate;
			this.status = STATUS_ACTIVE;
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

	public void ejbPostCreate(String bankCode, String bankName, String accountNumber, String currency,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			String signatory5, byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state,
			String country, String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate)
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

	private Integer insertNewRow(String bankCode, String bankName, String accountNumber, String currency,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			String signatory5, byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state,
			String country, String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO "
					+ TABLENAME
					+ " (pkid, bank_code, bank_name, account_number, currency, overdraft_limit, signatory1, signatory2, signatory3, signatory4, signatory5,"
					+ " signature, pc_center, add1, add2, add3, state, country, phone, contact_person, fax, userid_create, userid_update, createtime, lastupdate,"
					+ " status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, bankCode);
			ps.setString(3, bankName);
			ps.setString(4, accountNumber);
			ps.setString(5, currency);
			ps.setBigDecimal(6, overdraftLimit);
			ps.setString(7, signatory1);
			ps.setString(8, signatory2);
			ps.setString(9, signatory3);
			ps.setString(10, signatory4);
			ps.setString(11, signatory5);
			ps.setBytes(12, signature);
			ps.setInt(13, pcCenter.intValue());
			ps.setString(14, add1);
			ps.setString(15, add2);
			ps.setString(16, add3);
			ps.setString(17, state);
			ps.setString(18, country);
			ps.setString(19, phone);
			ps.setString(20, contactPerson);
			ps.setString(21, fax);
			ps.setInt(22, userIdCreate.intValue());
			ps.setInt(23, userIdUpdate.intValue());
			ps.setTimestamp(24, createTime);
			ps.setTimestamp(25, lastUpdate);
			ps.setString(26, STATUS_ACTIVE);
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
			String sqlStatement = "SELECT pkid, bank_code, bank_name, account_number, currency, overdraft_limit, signatory1, signatory2, signatory3, signatory4, signatory5,"
					+ " signature, pc_center, add1, add2, add3, state, country, phone, contact_person, fax, userid_create, userid_update, createtime,"
					+ " lastupdate, status FROM " + TABLENAME + " WHERE pkid =?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.bankCode = rs.getString("bank_code");
				this.bankName = rs.getString("bank_name");
				this.accountNumber = rs.getString("account_number");
				this.currency = rs.getString("currency");
				this.overdraftLimit = rs.getBigDecimal("overdraft_limit");
				this.signatory1 = rs.getString("signatory1");
				this.signatory2 = rs.getString("signatory2");
				this.signatory3 = rs.getString("signatory3");
				this.signatory4 = rs.getString("signatory4");
				this.signatory5 = rs.getString("signatory5");
				this.signature = rs.getBytes("signature");
				this.pcCenter = new Integer(rs.getString("pc_center"));
				this.add1 = rs.getString("add1");
				this.add2 = rs.getString("add2");
				this.add3 = rs.getString("add3");
				this.state = rs.getString("state");
				this.country = rs.getString("country");
				this.phone = rs.getString("phone");
				this.contactPerson = rs.getString("contact_person");
				this.fax = rs.getString("fax");
				this.userIdCreate = new Integer(rs.getString("userid_create"));
				this.userIdUpdate = new Integer(rs.getString("userid_update"));
				this.createTime = rs.getTimestamp("createtime");
				this.lastUpdate = rs.getTimestamp("lastupdate");
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
					+ " SET bank_code = ?, bank_name = ?, account_number = ?, currency = ?, overdraft_limit = ?,  signatory1 = ?, signatory2 = ?, signatory3 = ?,"
					+ " signatory4 = ?, signatory5 = ?, signature = ?, pc_center = ?, add1 = ?, add2 = ?, add3 = ?, state = ?, country = ?, phone = ?,"
					+ " contact_person = ?, fax = ?, userid_create = ?, userid_update = ?, createtime = ?, lastupdate = ?, status = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.bankCode);
			ps.setString(2, this.bankName);
			ps.setString(3, this.accountNumber);
			ps.setString(4, this.currency);
			ps.setBigDecimal(5, this.overdraftLimit);
			ps.setString(6, this.signatory1);
			ps.setString(7, this.signatory2);
			ps.setString(8, this.signatory3);
			ps.setString(9, this.signatory4);
			ps.setString(10, this.signatory5);
			ps.setBytes(11, this.signature);
			ps.setInt(12, this.pcCenter.intValue());
			ps.setString(13, this.add1);
			ps.setString(14, this.add2);
			ps.setString(15, this.add3);
			ps.setString(16, this.state);
			ps.setString(17, this.country);
			ps.setString(18, this.phone);
			ps.setString(19, this.contactPerson);
			ps.setString(20, this.fax);
			ps.setInt(21, this.userIdCreate.intValue());
			ps.setInt(22, this.userIdUpdate.intValue());
			ps.setTimestamp(23, this.createTime);
			ps.setTimestamp(24, this.lastUpdate);
			ps.setString(25, this.status);
			ps.setInt(26, this.pkId.intValue());
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
import java.math.*;
import com.vlee.local.*;
import com.vlee.util.*;

public class BankAccountBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String TABLENAME = "acc_bank_account";
	protected final String strObjectName = "BankAccountBean: ";
	private EntityContext mContext;
	public static final String STATUS = "status";
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	public static final String STATUS_DELETED = "deleted";
	private Integer pkId;
	private String bankCode;
	private String bankName;
	private String accountNumber;
	private String currency;
	private BigDecimal overdraftLimit;
	private String signatory1;
	private String signatory2;
	private String signatory3;
	private String signatory4;
	private String signatory5;
	private byte[] signature;
	private Integer pcCenter;
	private String add1;
	private String add2;
	private String add3;
	private String state;
	private String country;
	private String phone;
	private String contactPerson;
	private String fax;
	private Integer userIdCreate;
	private Integer userIdUpdate;
	private Timestamp createTime;
	private Timestamp lastUpdate;
	private String status;

	public Integer getPkId()
	{
		return this.pkId;
	}

	public String getBankCode()
	{
		return this.bankCode;
	}

	public String getBankName()
	{
		return this.bankName;
	}

	public String getAccountNumber()
	{
		return this.accountNumber;
	}

	public String getCurrency()
	{
		return this.currency;
	}

	public BigDecimal getOverdraftLimit()
	{
		return this.overdraftLimit;
	}

	public String getSignatory1()
	{
		return this.signatory1;
	}

	public String getSignatory2()
	{
		return this.signatory2;
	}

	public String getSignatory3()
	{
		return this.signatory3;
	}

	public String getSignatory4()
	{
		return this.signatory4;
	}

	public String getSignatory5()
	{
		return this.signatory5;
	}

	public byte[] getSignature()
	{
		return this.signature;
	}

	public Integer getPCCenter()
	{
		return this.pcCenter;
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

	public String getState()
	{
		return this.state;
	}

	public String getCountry()
	{
		return this.country;
	}

	public String getPhone()
	{
		return this.phone;
	}

	public String getContactPerson()
	{
		return this.contactPerson;
	}

	public String getFax()
	{
		return this.fax;
	}

	public Integer getUserIdCreate()
	{
		return this.userIdCreate;
	}

	public Integer getUserIdUpdate()
	{
		return this.userIdUpdate;
	}

	public Timestamp getCreateTime()
	{
		return this.createTime;
	}

	public Timestamp getLastUpdate()
	{
		return this.lastUpdate;
	}

	public String getStatus()
	{
		return this.status;
	}

	public void setPkId(Integer pkid)
	{
		this.pkId = pkid;
	}

	public void setBankCode(String bc)
	{
		this.bankCode = bc;
	}

	public void setBankName(String bn)
	{
		this.bankName = bn;
	}

	public void setAccountNumber(String an)
	{
		this.accountNumber = an;
	}

	public void setCurrency(String ccy)
	{
		this.currency = ccy;
	}

	public void setOverdraftLimit(BigDecimal ol)
	{
		this.overdraftLimit = ol;
	}

	public void setSignatory1(String s1)
	{
		this.signatory1 = s1;
	}

	public void setSignatory2(String s2)
	{
		this.signatory2 = s2;
	}

	public void setSignatory3(String s3)
	{
		this.signatory3 = s3;
	}

	public void setSignatory4(String s4)
	{
		this.signatory4 = s4;
	}

	public void setSignatory5(String s5)
	{
		this.signatory5 = s5;
	}

	public void setSignature(byte[] s)
	{
		this.signature = s;
	}

	public void setPCCenter(Integer be)
	{
		this.pcCenter = be;
	}

	public void setAdd1(String a1)
	{
		this.add1 = a1;
	}

	public void setAdd2(String a2)
	{
		this.add2 = a2;
	}

	public void setAdd3(String a3)
	{
		this.add3 = a3;
	}

	public void setState(String st)
	{
		this.state = st;
	}

	public void setCountry(String c)
	{
		this.country = c;
	}

	public void setPhone(String p)
	{
		this.phone = p;
	}

	public void setContactPerson(String cp)
	{
		this.contactPerson = cp;
	}

	public void setFax(String f)
	{
		this.fax = f;
	}

	public void setUserIdCreate(Integer uc)
	{
		this.userIdCreate = uc;
	}

	public void setUserIdUpdate(Integer uu)
	{
		this.userIdUpdate = uu;
	}

	public void setCreateTime(Timestamp ct)
	{
		this.createTime = ct;
	}

	public void setLastUpdate(Timestamp lu)
	{
		this.lastUpdate = lu;
	}

	public void setStatus(String status)
	{
		this.status = status;
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

	public Integer ejbCreate(String bankCode, String bankName, String accountNumber, String currency,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			String signatory5, byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state,
			String country, String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(bankCode, bankName, accountNumber, currency, overdraftLimit, signatory1, signatory2,
				signatory3, signatory4, signatory5, signature, pcCenter, add1, add2, add3, state, country, phone,
				contactPerson, fax, userIdCreate, userIdUpdate, createTime, lastUpdate);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.bankCode = bankCode;
			this.bankName = bankName;
			this.accountNumber = accountNumber;
			this.currency = currency;
			this.overdraftLimit = overdraftLimit;
			this.signatory1 = signatory1;
			this.signatory2 = signatory2;
			this.signatory3 = signatory3;
			this.signatory4 = signatory4;
			this.signatory5 = signatory5;
			this.signature = signature;
			this.pcCenter = pcCenter;
			this.add1 = add1;
			this.add2 = add2;
			this.add3 = add3;
			this.state = state;
			this.country = country;
			this.phone = phone;
			this.contactPerson = contactPerson;
			this.fax = fax;
			this.userIdCreate = userIdCreate;
			this.userIdUpdate = userIdUpdate;
			this.createTime = createTime;
			this.lastUpdate = lastUpdate;
			this.status = STATUS_ACTIVE;
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

	public void ejbPostCreate(String bankCode, String bankName, String accountNumber, String currency,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			String signatory5, byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state,
			String country, String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate)
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

	private Integer insertNewRow(String bankCode, String bankName, String accountNumber, String currency,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			String signatory5, byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state,
			String country, String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO "
					+ TABLENAME
					+ " (pkid, bank_code, bank_name, account_number, currency, overdraft_limit, signatory1, signatory2, signatory3, signatory4, signatory5,"
					+ " signature, pc_center, add1, add2, add3, state, country, phone, contact_person, fax, userid_create, userid_update, createtime, lastupdate,"
					+ " status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, bankCode);
			ps.setString(3, bankName);
			ps.setString(4, accountNumber);
			ps.setString(5, currency);
			ps.setBigDecimal(6, overdraftLimit);
			ps.setString(7, signatory1);
			ps.setString(8, signatory2);
			ps.setString(9, signatory3);
			ps.setString(10, signatory4);
			ps.setString(11, signatory5);
			ps.setBytes(12, signature);
			ps.setInt(13, pcCenter.intValue());
			ps.setString(14, add1);
			ps.setString(15, add2);
			ps.setString(16, add3);
			ps.setString(17, state);
			ps.setString(18, country);
			ps.setString(19, phone);
			ps.setString(20, contactPerson);
			ps.setString(21, fax);
			ps.setInt(22, userIdCreate.intValue());
			ps.setInt(23, userIdUpdate.intValue());
			ps.setTimestamp(24, createTime);
			ps.setTimestamp(25, lastUpdate);
			ps.setString(26, STATUS_ACTIVE);
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
			String sqlStatement = "SELECT pkid, bank_code, bank_name, account_number, currency, overdraft_limit, signatory1, signatory2, signatory3, signatory4, signatory5,"
					+ " signature, pc_center, add1, add2, add3, state, country, phone, contact_person, fax, userid_create, userid_update, createtime,"
					+ " lastupdate, status FROM " + TABLENAME + " WHERE pkid =?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.bankCode = rs.getString("bank_code");
				this.bankName = rs.getString("bank_name");
				this.accountNumber = rs.getString("account_number");
				this.currency = rs.getString("currency");
				this.overdraftLimit = rs.getBigDecimal("overdraft_limit");
				this.signatory1 = rs.getString("signatory1");
				this.signatory2 = rs.getString("signatory2");
				this.signatory3 = rs.getString("signatory3");
				this.signatory4 = rs.getString("signatory4");
				this.signatory5 = rs.getString("signatory5");
				this.signature = rs.getBytes("signature");
				this.pcCenter = new Integer(rs.getString("pc_center"));
				this.add1 = rs.getString("add1");
				this.add2 = rs.getString("add2");
				this.add3 = rs.getString("add3");
				this.state = rs.getString("state");
				this.country = rs.getString("country");
				this.phone = rs.getString("phone");
				this.contactPerson = rs.getString("contact_person");
				this.fax = rs.getString("fax");
				this.userIdCreate = new Integer(rs.getString("userid_create"));
				this.userIdUpdate = new Integer(rs.getString("userid_update"));
				this.createTime = rs.getTimestamp("createtime");
				this.lastUpdate = rs.getTimestamp("lastupdate");
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
					+ " SET bank_code = ?, bank_name = ?, account_number = ?, currency = ?, overdraft_limit = ?,  signatory1 = ?, signatory2 = ?, signatory3 = ?,"
					+ " signatory4 = ?, signatory5 = ?, signature = ?, pc_center = ?, add1 = ?, add2 = ?, add3 = ?, state = ?, country = ?, phone = ?,"
					+ " contact_person = ?, fax = ?, userid_create = ?, userid_update = ?, createtime = ?, lastupdate = ?, status = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.bankCode);
			ps.setString(2, this.bankName);
			ps.setString(3, this.accountNumber);
			ps.setString(4, this.currency);
			ps.setBigDecimal(5, this.overdraftLimit);
			ps.setString(6, this.signatory1);
			ps.setString(7, this.signatory2);
			ps.setString(8, this.signatory3);
			ps.setString(9, this.signatory4);
			ps.setString(10, this.signatory5);
			ps.setBytes(11, this.signature);
			ps.setInt(12, this.pcCenter.intValue());
			ps.setString(13, this.add1);
			ps.setString(14, this.add2);
			ps.setString(15, this.add3);
			ps.setString(16, this.state);
			ps.setString(17, this.country);
			ps.setString(18, this.phone);
			ps.setString(19, this.contactPerson);
			ps.setString(20, this.fax);
			ps.setInt(21, this.userIdCreate.intValue());
			ps.setInt(22, this.userIdUpdate.intValue());
			ps.setTimestamp(23, this.createTime);
			ps.setTimestamp(24, this.lastUpdate);
			ps.setString(25, this.status);
			ps.setInt(26, this.pkId.intValue());
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
import java.math.*;
import com.vlee.local.*;
import com.vlee.util.*;

public class BankAccountBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String TABLENAME = "acc_bank_account";
	protected final String strObjectName = "BankAccountBean: ";
	private EntityContext mContext;
	public static final String STATUS = "status";
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	public static final String STATUS_DELETED = "deleted";
	private Integer pkId;
	private String bankCode;
	private String bankName;
	private String accountNumber;
	private String currency;
	private BigDecimal overdraftLimit;
	private String signatory1;
	private String signatory2;
	private String signatory3;
	private String signatory4;
	private String signatory5;
	private byte[] signature;
	private Integer pcCenter;
	private String add1;
	private String add2;
	private String add3;
	private String state;
	private String country;
	private String phone;
	private String contactPerson;
	private String fax;
	private Integer userIdCreate;
	private Integer userIdUpdate;
	private Timestamp createTime;
	private Timestamp lastUpdate;
	private String status;

	public Integer getPkId()
	{
		return this.pkId;
	}

	public String getBankCode()
	{
		return this.bankCode;
	}

	public String getBankName()
	{
		return this.bankName;
	}

	public String getAccountNumber()
	{
		return this.accountNumber;
	}

	public String getCurrency()
	{
		return this.currency;
	}

	public BigDecimal getOverdraftLimit()
	{
		return this.overdraftLimit;
	}

	public String getSignatory1()
	{
		return this.signatory1;
	}

	public String getSignatory2()
	{
		return this.signatory2;
	}

	public String getSignatory3()
	{
		return this.signatory3;
	}

	public String getSignatory4()
	{
		return this.signatory4;
	}

	public String getSignatory5()
	{
		return this.signatory5;
	}

	public byte[] getSignature()
	{
		return this.signature;
	}

	public Integer getPCCenter()
	{
		return this.pcCenter;
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

	public String getState()
	{
		return this.state;
	}

	public String getCountry()
	{
		return this.country;
	}

	public String getPhone()
	{
		return this.phone;
	}

	public String getContactPerson()
	{
		return this.contactPerson;
	}

	public String getFax()
	{
		return this.fax;
	}

	public Integer getUserIdCreate()
	{
		return this.userIdCreate;
	}

	public Integer getUserIdUpdate()
	{
		return this.userIdUpdate;
	}

	public Timestamp getCreateTime()
	{
		return this.createTime;
	}

	public Timestamp getLastUpdate()
	{
		return this.lastUpdate;
	}

	public String getStatus()
	{
		return this.status;
	}

	public void setPkId(Integer pkid)
	{
		this.pkId = pkid;
	}

	public void setBankCode(String bc)
	{
		this.bankCode = bc;
	}

	public void setBankName(String bn)
	{
		this.bankName = bn;
	}

	public void setAccountNumber(String an)
	{
		this.accountNumber = an;
	}

	public void setCurrency(String ccy)
	{
		this.currency = ccy;
	}

	public void setOverdraftLimit(BigDecimal ol)
	{
		this.overdraftLimit = ol;
	}

	public void setSignatory1(String s1)
	{
		this.signatory1 = s1;
	}

	public void setSignatory2(String s2)
	{
		this.signatory2 = s2;
	}

	public void setSignatory3(String s3)
	{
		this.signatory3 = s3;
	}

	public void setSignatory4(String s4)
	{
		this.signatory4 = s4;
	}

	public void setSignatory5(String s5)
	{
		this.signatory5 = s5;
	}

	public void setSignature(byte[] s)
	{
		this.signature = s;
	}

	public void setPCCenter(Integer be)
	{
		this.pcCenter = be;
	}

	public void setAdd1(String a1)
	{
		this.add1 = a1;
	}

	public void setAdd2(String a2)
	{
		this.add2 = a2;
	}

	public void setAdd3(String a3)
	{
		this.add3 = a3;
	}

	public void setState(String st)
	{
		this.state = st;
	}

	public void setCountry(String c)
	{
		this.country = c;
	}

	public void setPhone(String p)
	{
		this.phone = p;
	}

	public void setContactPerson(String cp)
	{
		this.contactPerson = cp;
	}

	public void setFax(String f)
	{
		this.fax = f;
	}

	public void setUserIdCreate(Integer uc)
	{
		this.userIdCreate = uc;
	}

	public void setUserIdUpdate(Integer uu)
	{
		this.userIdUpdate = uu;
	}

	public void setCreateTime(Timestamp ct)
	{
		this.createTime = ct;
	}

	public void setLastUpdate(Timestamp lu)
	{
		this.lastUpdate = lu;
	}

	public void setStatus(String status)
	{
		this.status = status;
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

	public Integer ejbCreate(String bankCode, String bankName, String accountNumber, String currency,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			String signatory5, byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state,
			String country, String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(bankCode, bankName, accountNumber, currency, overdraftLimit, signatory1, signatory2,
				signatory3, signatory4, signatory5, signature, pcCenter, add1, add2, add3, state, country, phone,
				contactPerson, fax, userIdCreate, userIdUpdate, createTime, lastUpdate);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.bankCode = bankCode;
			this.bankName = bankName;
			this.accountNumber = accountNumber;
			this.currency = currency;
			this.overdraftLimit = overdraftLimit;
			this.signatory1 = signatory1;
			this.signatory2 = signatory2;
			this.signatory3 = signatory3;
			this.signatory4 = signatory4;
			this.signatory5 = signatory5;
			this.signature = signature;
			this.pcCenter = pcCenter;
			this.add1 = add1;
			this.add2 = add2;
			this.add3 = add3;
			this.state = state;
			this.country = country;
			this.phone = phone;
			this.contactPerson = contactPerson;
			this.fax = fax;
			this.userIdCreate = userIdCreate;
			this.userIdUpdate = userIdUpdate;
			this.createTime = createTime;
			this.lastUpdate = lastUpdate;
			this.status = STATUS_ACTIVE;
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

	public void ejbPostCreate(String bankCode, String bankName, String accountNumber, String currency,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			String signatory5, byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state,
			String country, String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate)
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

	private Integer insertNewRow(String bankCode, String bankName, String accountNumber, String currency,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			String signatory5, byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state,
			String country, String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO "
					+ TABLENAME
					+ " (pkid, bank_code, bank_name, account_number, currency, overdraft_limit, signatory1, signatory2, signatory3, signatory4, signatory5,"
					+ " signature, pc_center, add1, add2, add3, state, country, phone, contact_person, fax, userid_create, userid_update, createtime, lastupdate,"
					+ " status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, bankCode);
			ps.setString(3, bankName);
			ps.setString(4, accountNumber);
			ps.setString(5, currency);
			ps.setBigDecimal(6, overdraftLimit);
			ps.setString(7, signatory1);
			ps.setString(8, signatory2);
			ps.setString(9, signatory3);
			ps.setString(10, signatory4);
			ps.setString(11, signatory5);
			ps.setBytes(12, signature);
			ps.setInt(13, pcCenter.intValue());
			ps.setString(14, add1);
			ps.setString(15, add2);
			ps.setString(16, add3);
			ps.setString(17, state);
			ps.setString(18, country);
			ps.setString(19, phone);
			ps.setString(20, contactPerson);
			ps.setString(21, fax);
			ps.setInt(22, userIdCreate.intValue());
			ps.setInt(23, userIdUpdate.intValue());
			ps.setTimestamp(24, createTime);
			ps.setTimestamp(25, lastUpdate);
			ps.setString(26, STATUS_ACTIVE);
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
			String sqlStatement = "SELECT pkid, bank_code, bank_name, account_number, currency, overdraft_limit, signatory1, signatory2, signatory3, signatory4, signatory5,"
					+ " signature, pc_center, add1, add2, add3, state, country, phone, contact_person, fax, userid_create, userid_update, createtime,"
					+ " lastupdate, status FROM " + TABLENAME + " WHERE pkid =?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.bankCode = rs.getString("bank_code");
				this.bankName = rs.getString("bank_name");
				this.accountNumber = rs.getString("account_number");
				this.currency = rs.getString("currency");
				this.overdraftLimit = rs.getBigDecimal("overdraft_limit");
				this.signatory1 = rs.getString("signatory1");
				this.signatory2 = rs.getString("signatory2");
				this.signatory3 = rs.getString("signatory3");
				this.signatory4 = rs.getString("signatory4");
				this.signatory5 = rs.getString("signatory5");
				this.signature = rs.getBytes("signature");
				this.pcCenter = new Integer(rs.getString("pc_center"));
				this.add1 = rs.getString("add1");
				this.add2 = rs.getString("add2");
				this.add3 = rs.getString("add3");
				this.state = rs.getString("state");
				this.country = rs.getString("country");
				this.phone = rs.getString("phone");
				this.contactPerson = rs.getString("contact_person");
				this.fax = rs.getString("fax");
				this.userIdCreate = new Integer(rs.getString("userid_create"));
				this.userIdUpdate = new Integer(rs.getString("userid_update"));
				this.createTime = rs.getTimestamp("createtime");
				this.lastUpdate = rs.getTimestamp("lastupdate");
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
					+ " SET bank_code = ?, bank_name = ?, account_number = ?, currency = ?, overdraft_limit = ?,  signatory1 = ?, signatory2 = ?, signatory3 = ?,"
					+ " signatory4 = ?, signatory5 = ?, signature = ?, pc_center = ?, add1 = ?, add2 = ?, add3 = ?, state = ?, country = ?, phone = ?,"
					+ " contact_person = ?, fax = ?, userid_create = ?, userid_update = ?, createtime = ?, lastupdate = ?, status = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.bankCode);
			ps.setString(2, this.bankName);
			ps.setString(3, this.accountNumber);
			ps.setString(4, this.currency);
			ps.setBigDecimal(5, this.overdraftLimit);
			ps.setString(6, this.signatory1);
			ps.setString(7, this.signatory2);
			ps.setString(8, this.signatory3);
			ps.setString(9, this.signatory4);
			ps.setString(10, this.signatory5);
			ps.setBytes(11, this.signature);
			ps.setInt(12, this.pcCenter.intValue());
			ps.setString(13, this.add1);
			ps.setString(14, this.add2);
			ps.setString(15, this.add3);
			ps.setString(16, this.state);
			ps.setString(17, this.country);
			ps.setString(18, this.phone);
			ps.setString(19, this.contactPerson);
			ps.setString(20, this.fax);
			ps.setInt(21, this.userIdCreate.intValue());
			ps.setInt(22, this.userIdUpdate.intValue());
			ps.setTimestamp(23, this.createTime);
			ps.setTimestamp(24, this.lastUpdate);
			ps.setString(25, this.status);
			ps.setInt(26, this.pkId.intValue());
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
import java.math.*;
import com.vlee.local.*;
import com.vlee.util.*;

public class BankAccountBean implements EntityBean
{
	private String dsName = ServerConfig.DATA_SOURCE;
	protected final String TABLENAME = "acc_bank_account";
	protected final String strObjectName = "BankAccountBean: ";
	private EntityContext mContext;
	public static final String STATUS = "status";
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";
	public static final String STATUS_DELETED = "deleted";
	private Integer pkId;
	private String bankCode;
	private String bankName;
	private String accountNumber;
	private String currency;
	private BigDecimal overdraftLimit;
	private String signatory1;
	private String signatory2;
	private String signatory3;
	private String signatory4;
	private String signatory5;
	private byte[] signature;
	private Integer pcCenter;
	private String add1;
	private String add2;
	private String add3;
	private String state;
	private String country;
	private String phone;
	private String contactPerson;
	private String fax;
	private Integer userIdCreate;
	private Integer userIdUpdate;
	private Timestamp createTime;
	private Timestamp lastUpdate;
	private String status;

	public Integer getPkId()
	{
		return this.pkId;
	}

	public String getBankCode()
	{
		return this.bankCode;
	}

	public String getBankName()
	{
		return this.bankName;
	}

	public String getAccountNumber()
	{
		return this.accountNumber;
	}

	public String getCurrency()
	{
		return this.currency;
	}

	public BigDecimal getOverdraftLimit()
	{
		return this.overdraftLimit;
	}

	public String getSignatory1()
	{
		return this.signatory1;
	}

	public String getSignatory2()
	{
		return this.signatory2;
	}

	public String getSignatory3()
	{
		return this.signatory3;
	}

	public String getSignatory4()
	{
		return this.signatory4;
	}

	public String getSignatory5()
	{
		return this.signatory5;
	}

	public byte[] getSignature()
	{
		return this.signature;
	}

	public Integer getPCCenter()
	{
		return this.pcCenter;
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

	public String getState()
	{
		return this.state;
	}

	public String getCountry()
	{
		return this.country;
	}

	public String getPhone()
	{
		return this.phone;
	}

	public String getContactPerson()
	{
		return this.contactPerson;
	}

	public String getFax()
	{
		return this.fax;
	}

	public Integer getUserIdCreate()
	{
		return this.userIdCreate;
	}

	public Integer getUserIdUpdate()
	{
		return this.userIdUpdate;
	}

	public Timestamp getCreateTime()
	{
		return this.createTime;
	}

	public Timestamp getLastUpdate()
	{
		return this.lastUpdate;
	}

	public String getStatus()
	{
		return this.status;
	}

	public void setPkId(Integer pkid)
	{
		this.pkId = pkid;
	}

	public void setBankCode(String bc)
	{
		this.bankCode = bc;
	}

	public void setBankName(String bn)
	{
		this.bankName = bn;
	}

	public void setAccountNumber(String an)
	{
		this.accountNumber = an;
	}

	public void setCurrency(String ccy)
	{
		this.currency = ccy;
	}

	public void setOverdraftLimit(BigDecimal ol)
	{
		this.overdraftLimit = ol;
	}

	public void setSignatory1(String s1)
	{
		this.signatory1 = s1;
	}

	public void setSignatory2(String s2)
	{
		this.signatory2 = s2;
	}

	public void setSignatory3(String s3)
	{
		this.signatory3 = s3;
	}

	public void setSignatory4(String s4)
	{
		this.signatory4 = s4;
	}

	public void setSignatory5(String s5)
	{
		this.signatory5 = s5;
	}

	public void setSignature(byte[] s)
	{
		this.signature = s;
	}

	public void setPCCenter(Integer be)
	{
		this.pcCenter = be;
	}

	public void setAdd1(String a1)
	{
		this.add1 = a1;
	}

	public void setAdd2(String a2)
	{
		this.add2 = a2;
	}

	public void setAdd3(String a3)
	{
		this.add3 = a3;
	}

	public void setState(String st)
	{
		this.state = st;
	}

	public void setCountry(String c)
	{
		this.country = c;
	}

	public void setPhone(String p)
	{
		this.phone = p;
	}

	public void setContactPerson(String cp)
	{
		this.contactPerson = cp;
	}

	public void setFax(String f)
	{
		this.fax = f;
	}

	public void setUserIdCreate(Integer uc)
	{
		this.userIdCreate = uc;
	}

	public void setUserIdUpdate(Integer uu)
	{
		this.userIdUpdate = uu;
	}

	public void setCreateTime(Timestamp ct)
	{
		this.createTime = ct;
	}

	public void setLastUpdate(Timestamp lu)
	{
		this.lastUpdate = lu;
	}

	public void setStatus(String status)
	{
		this.status = status;
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

	public Integer ejbCreate(String bankCode, String bankName, String accountNumber, String currency,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			String signatory5, byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state,
			String country, String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate) throws CreateException
	{
		Integer newPkId = null;
		Log.printVerbose(strObjectName + " In ejbCreate");
		newPkId = insertNewRow(bankCode, bankName, accountNumber, currency, overdraftLimit, signatory1, signatory2,
				signatory3, signatory4, signatory5, signature, pcCenter, add1, add2, add3, state, country, phone,
				contactPerson, fax, userIdCreate, userIdUpdate, createTime, lastUpdate);
		if (newPkId != null)
		{
			this.pkId = newPkId;
			this.bankCode = bankCode;
			this.bankName = bankName;
			this.accountNumber = accountNumber;
			this.currency = currency;
			this.overdraftLimit = overdraftLimit;
			this.signatory1 = signatory1;
			this.signatory2 = signatory2;
			this.signatory3 = signatory3;
			this.signatory4 = signatory4;
			this.signatory5 = signatory5;
			this.signature = signature;
			this.pcCenter = pcCenter;
			this.add1 = add1;
			this.add2 = add2;
			this.add3 = add3;
			this.state = state;
			this.country = country;
			this.phone = phone;
			this.contactPerson = contactPerson;
			this.fax = fax;
			this.userIdCreate = userIdCreate;
			this.userIdUpdate = userIdUpdate;
			this.createTime = createTime;
			this.lastUpdate = lastUpdate;
			this.status = STATUS_ACTIVE;
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

	public void ejbPostCreate(String bankCode, String bankName, String accountNumber, String currency,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			String signatory5, byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state,
			String country, String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate)
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

	private Integer insertNewRow(String bankCode, String bankName, String accountNumber, String currency,
			BigDecimal overdraftLimit, String signatory1, String signatory2, String signatory3, String signatory4,
			String signatory5, byte[] signature, Integer pcCenter, String add1, String add2, String add3, String state,
			String country, String phone, String contactPerson, String fax, Integer userIdCreate, Integer userIdUpdate,
			Timestamp createTime, Timestamp lastUpdate)
	{
		DataSource ds = getDataSource();
		Connection cn = null;
		PreparedStatement ps = null;
		try
		{
			Integer newPkId = getNextPKId();
			String sqlStatement = "INSERT INTO "
					+ TABLENAME
					+ " (pkid, bank_code, bank_name, account_number, currency, overdraft_limit, signatory1, signatory2, signatory3, signatory4, signatory5,"
					+ " signature, pc_center, add1, add2, add3, state, country, phone, contact_person, fax, userid_create, userid_update, createtime, lastupdate,"
					+ " status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, newPkId.intValue());
			ps.setString(2, bankCode);
			ps.setString(3, bankName);
			ps.setString(4, accountNumber);
			ps.setString(5, currency);
			ps.setBigDecimal(6, overdraftLimit);
			ps.setString(7, signatory1);
			ps.setString(8, signatory2);
			ps.setString(9, signatory3);
			ps.setString(10, signatory4);
			ps.setString(11, signatory5);
			ps.setBytes(12, signature);
			ps.setInt(13, pcCenter.intValue());
			ps.setString(14, add1);
			ps.setString(15, add2);
			ps.setString(16, add3);
			ps.setString(17, state);
			ps.setString(18, country);
			ps.setString(19, phone);
			ps.setString(20, contactPerson);
			ps.setString(21, fax);
			ps.setInt(22, userIdCreate.intValue());
			ps.setInt(23, userIdUpdate.intValue());
			ps.setTimestamp(24, createTime);
			ps.setTimestamp(25, lastUpdate);
			ps.setString(26, STATUS_ACTIVE);
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
			String sqlStatement = "SELECT pkid, bank_code, bank_name, account_number, currency, overdraft_limit, signatory1, signatory2, signatory3, signatory4, signatory5,"
					+ " signature, pc_center, add1, add2, add3, state, country, phone, contact_person, fax, userid_create, userid_update, createtime,"
					+ " lastupdate, status FROM " + TABLENAME + " WHERE pkid =?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setInt(1, this.pkId.intValue());
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				this.pkId = new Integer(rs.getInt("pkid"));
				this.bankCode = rs.getString("bank_code");
				this.bankName = rs.getString("bank_name");
				this.accountNumber = rs.getString("account_number");
				this.currency = rs.getString("currency");
				this.overdraftLimit = rs.getBigDecimal("overdraft_limit");
				this.signatory1 = rs.getString("signatory1");
				this.signatory2 = rs.getString("signatory2");
				this.signatory3 = rs.getString("signatory3");
				this.signatory4 = rs.getString("signatory4");
				this.signatory5 = rs.getString("signatory5");
				this.signature = rs.getBytes("signature");
				this.pcCenter = new Integer(rs.getString("pc_center"));
				this.add1 = rs.getString("add1");
				this.add2 = rs.getString("add2");
				this.add3 = rs.getString("add3");
				this.state = rs.getString("state");
				this.country = rs.getString("country");
				this.phone = rs.getString("phone");
				this.contactPerson = rs.getString("contact_person");
				this.fax = rs.getString("fax");
				this.userIdCreate = new Integer(rs.getString("userid_create"));
				this.userIdUpdate = new Integer(rs.getString("userid_update"));
				this.createTime = rs.getTimestamp("createtime");
				this.lastUpdate = rs.getTimestamp("lastupdate");
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
					+ " SET bank_code = ?, bank_name = ?, account_number = ?, currency = ?, overdraft_limit = ?,  signatory1 = ?, signatory2 = ?, signatory3 = ?,"
					+ " signatory4 = ?, signatory5 = ?, signature = ?, pc_center = ?, add1 = ?, add2 = ?, add3 = ?, state = ?, country = ?, phone = ?,"
					+ " contact_person = ?, fax = ?, userid_create = ?, userid_update = ?, createtime = ?, lastupdate = ?, status = ? WHERE pkid = ?";
			cn = ds.getConnection();
			ps = cn.prepareStatement(sqlStatement);
			ps.setString(1, this.bankCode);
			ps.setString(2, this.bankName);
			ps.setString(3, this.accountNumber);
			ps.setString(4, this.currency);
			ps.setBigDecimal(5, this.overdraftLimit);
			ps.setString(6, this.signatory1);
			ps.setString(7, this.signatory2);
			ps.setString(8, this.signatory3);
			ps.setString(9, this.signatory4);
			ps.setString(10, this.signatory5);
			ps.setBytes(11, this.signature);
			ps.setInt(12, this.pcCenter.intValue());
			ps.setString(13, this.add1);
			ps.setString(14, this.add2);
			ps.setString(15, this.add3);
			ps.setString(16, this.state);
			ps.setString(17, this.country);
			ps.setString(18, this.phone);
			ps.setString(19, this.contactPerson);
			ps.setString(20, this.fax);
			ps.setInt(21, this.userIdCreate.intValue());
			ps.setInt(22, this.userIdUpdate.intValue());
			ps.setTimestamp(23, this.createTime);
			ps.setTimestamp(24, this.lastUpdate);
			ps.setString(25, this.status);
			ps.setInt(26, this.pkId.intValue());
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
